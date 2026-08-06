/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tschipp.callablehorses.config.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.config.AnnotationData;
import tschipp.callablehorses.config.BuiltCategory;
import tschipp.callablehorses.config.BuiltConfig;
import tschipp.callablehorses.config.PropertyData;
import tschipp.callablehorses.config.PropertyType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConfigLoaderImpl
{
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	public static final Map<JsonObject, BuiltConfig> CONFIGS = new HashMap<>();

	public static void initialize() throws IOException
	{
		Path cfgDir = FabricLoader.getInstance().getConfigDir();

		for (Map.Entry<JsonObject, BuiltConfig> entry : CONFIGS.entrySet())
		{
			Path cfgFile = cfgDir.resolve(entry.getValue().fileName + ".json");

			JsonObject existing = null;
			if (Files.exists(cfgFile))
			{
				try
				{
					existing = GSON.fromJson(Files.readString(cfgFile, StandardCharsets.UTF_8), JsonObject.class);
				}
				catch (Exception e)
				{
					Constants.LOG.error("Could not parse {}, regenerating it from defaults", cfgFile, e);
				}
			}

			Files.createDirectories(cfgDir);

			JsonObject defaults = entry.getKey();
			JsonObject toWrite;

			if (existing == null)
			{
				toWrite = defaults;
			}
			else
			{
				loadConfig(entry.getValue(), existing);
				toWrite = mergeDefaults(defaults, existing);
			}

			Files.writeString(cfgFile, GSON.toJson(toWrite), StandardCharsets.UTF_8);
		}
	}

	/**
	 * Every key the current config declares, taking the user's value wherever it has the right
	 * shape. Writing the existing file straight back would strand properties added or removed
	 * in a later version.
	 */
	private static JsonObject mergeDefaults(JsonObject defaults, JsonObject existing)
	{
		JsonObject merged = new JsonObject();

		for (Map.Entry<String, JsonElement> entry : defaults.entrySet())
		{
			String id = entry.getKey();
			JsonElement def = entry.getValue();
			JsonElement present = existing.get(id);

			if (id.startsWith("//") || present == null)
				merged.add(id, def);
			else if (def.isJsonObject() && present.isJsonObject())
				merged.add(id, mergeDefaults(def.getAsJsonObject(), present.getAsJsonObject()));
			else if (sameShape(def, present))
				merged.add(id, present);
			else
				merged.add(id, def);
		}

		return merged;
	}

	private static boolean sameShape(JsonElement def, JsonElement present)
	{
		if (def.isJsonArray())
			return present.isJsonArray();

		if (!def.isJsonPrimitive() || !present.isJsonPrimitive())
			return false;

		JsonPrimitive expected = def.getAsJsonPrimitive();
		JsonPrimitive actual = present.getAsJsonPrimitive();

		return (expected.isBoolean() && actual.isBoolean())
				|| (expected.isNumber() && actual.isNumber())
				|| (expected.isString() && actual.isString());
	}

	private static JsonObject loadConfig(BuiltCategory category, JsonObject config)
	{
		config.entrySet().forEach(entry -> {
			String id = entry.getKey();
			if (id.startsWith("//"))
				return;

			JsonElement value = entry.getValue();

			if (value instanceof JsonPrimitive configValue)
			{
				category.getProperty(id).ifPresent(data -> readPrimitive(config, id, configValue, data));
			}
			else if (value instanceof JsonObject subConfig)
			{
				category.getCategory(id).ifPresent(cat -> loadConfig(cat, subConfig));
			}
			else if (value instanceof JsonArray list)
			{
				category.getProperty(id).ifPresent(data -> {
					if (data.data().type() != PropertyType.STRING_ARRAY)
						return;

					List<String> values = new ArrayList<>();
					for (JsonElement element : list)
					{
						if (element instanceof JsonPrimitive p && p.isString())
							values.add(p.getAsString());
					}
					data.setStringArray(values.toArray(new String[0]));
				});
			}
		});

		return config;
	}

	private static void readPrimitive(JsonObject config, String id, JsonPrimitive configValue, PropertyData data)
	{
		AnnotationData annotation = data.data();

		try
		{
			if (configValue.isBoolean() && annotation.type() == PropertyType.BOOLEAN)
			{
				data.setBoolean(configValue.getAsBoolean());
			}
			else if (configValue.isNumber() && annotation.type() == PropertyType.INT)
			{
				int configInt = configValue.getAsInt();
				if (configInt > annotation.max() || configInt < annotation.min())
					config.addProperty(id, data.getInt());
				else
					data.setInt(configInt);
			}
			else if (configValue.isNumber() && annotation.type() == PropertyType.DOUBLE)
			{
				double configDouble = configValue.getAsDouble();
				if (configDouble > annotation.maxD() || configDouble < annotation.minD())
					config.addProperty(id, data.getDouble());
				else
					data.setDouble(configDouble);
			}
		}
		catch (IllegalAccessException e)
		{
			Constants.LOG.error("Could not read config property {}", id, e);
		}
	}

	public static void registerConfig(BuiltConfig config)
	{
		try
		{
			JsonObject configJson = new JsonObject();
			for (PropertyData property : config.properties)
				buildProperty(configJson, property);
			for (BuiltCategory category : config.categories)
				buildCategory(configJson, category);
			CONFIGS.put(configJson, config);
		}
		catch (Exception e)
		{
			Constants.LOG.error("Failed to build config {}", config.fileName, e);
		}
	}

	private static void buildCategory(JsonObject builder, BuiltCategory category) throws IllegalAccessException
	{
		JsonObject categoryJson = new JsonObject();
		if (category.categoryDesc != null)
			categoryJson.addProperty("//" + category.category, category.categoryDesc);
		for (PropertyData property : category.properties)
			buildProperty(categoryJson, property);
		for (BuiltCategory builtCategory : category.categories)
			buildCategory(categoryJson, builtCategory);
		builder.add(category.category, categoryJson);
	}

	private static void buildProperty(JsonObject builder, PropertyData data) throws IllegalAccessException
	{
		AnnotationData annotationData = data.data();
		builder.addProperty("//" + data.getId(), annotationData.description());

		switch (annotationData.type())
		{
			case BOOLEAN -> builder.addProperty(data.getId(), data.getBoolean());
			case INT -> builder.addProperty(data.getId(), data.getInt());
			case DOUBLE -> builder.addProperty(data.getId(), data.getDouble());
			case STRING_ARRAY -> {
				JsonArray arr = new JsonArray();
				for (String s : data.getStringArray())
					arr.add(s);
				builder.add(data.getId(), arr);
			}
			default -> throw new IllegalAccessException("Unknown property type.");
		}
	}
}
