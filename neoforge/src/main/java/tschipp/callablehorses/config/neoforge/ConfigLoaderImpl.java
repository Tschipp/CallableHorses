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

package tschipp.callablehorses.config.neoforge;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.config.AnnotationData;
import tschipp.callablehorses.config.BuiltCategory;
import tschipp.callablehorses.config.BuiltConfig;
import tschipp.callablehorses.config.PropertyData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class ConfigLoaderImpl
{
	public static final Map<ModConfigSpec, BuiltConfig> CONFIGS = new HashMap<>();

	public static void initialize(ModContainer container)
	{
		CONFIGS.forEach((spec, config) -> {
			if (config.fileName.contains("client"))
				container.registerConfig(ModConfig.Type.CLIENT, spec, config.fileName + ".toml");
			else
				container.registerConfig(ModConfig.Type.COMMON, spec, config.fileName + ".toml");
		});
	}

	@SubscribeEvent
	public static void onConfigLoad(ModConfigEvent.Loading event)
	{
		loadConfig((ModConfigSpec) event.getConfig().getSpec());
	}

	@SubscribeEvent
	public static void onConfigReload(ModConfigEvent.Reloading event)
	{
		loadConfig((ModConfigSpec) event.getConfig().getSpec());
	}

	private static void loadConfig(ModConfigSpec spec)
	{
		BuiltConfig builtConfig = CONFIGS.get(spec);
		if (builtConfig == null)
			return;

		loadConfig(builtConfig, spec.getValues());
	}

	private static void loadConfig(BuiltCategory category, UnmodifiableConfig config)
	{
		config.valueMap().forEach((id, value) -> {
			if (value instanceof ModConfigSpec.ConfigValue<?> configValue)
			{
				category.getProperty(id).ifPresent(data -> {
					if (configValue instanceof ModConfigSpec.BooleanValue booleanValue)
						data.setBoolean(booleanValue.get());
					if (configValue instanceof ModConfigSpec.IntValue intValue)
						data.setInt(intValue.get());
					if (configValue instanceof ModConfigSpec.DoubleValue doubleValue)
						data.setDouble(doubleValue.get());
					if (configValue.get() instanceof List<?> listVal)
						data.setStringArray(listVal.toArray(new String[0]));
				});
			}
			else if (value instanceof AbstractConfig subConfig)
			{
				category.getCategory(id).ifPresent(cat -> loadConfig(cat, subConfig));
			}
		});
	}

	public static void registerConfig(BuiltConfig config)
	{
		try
		{
			ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
			for (PropertyData property : config.properties)
				buildProperty(builder, property);
			for (BuiltCategory category : config.categories)
				buildCategory(builder, category);
			CONFIGS.put(builder.build(), config);
		}
		catch (Exception e)
		{
			Constants.LOG.error("Failed to build config {}", config.fileName, e);
		}
	}

	private static void buildCategory(ModConfigSpec.Builder builder, BuiltCategory category) throws IllegalAccessException
	{
		builder.push(category.category);
		if (category.categoryDesc != null)
			builder.comment(category.categoryDesc);
		for (PropertyData property : category.properties)
			buildProperty(builder, property);
		for (BuiltCategory builtCategory : category.categories)
			buildCategory(builder, builtCategory);
		builder.pop();
	}

	private static void buildProperty(ModConfigSpec.Builder builder, PropertyData data) throws IllegalAccessException
	{
		AnnotationData annotationData = data.data();
		builder.comment(annotationData.description());

		switch (annotationData.type())
		{
			case BOOLEAN -> builder.define(data.getId(), data.getBoolean());
			case INT -> builder.defineInRange(data.getId(), data.getInt(), annotationData.min(), annotationData.max());
			case DOUBLE -> builder.defineInRange(data.getId(), data.getDouble(), annotationData.minD(), annotationData.maxD());
			case STRING_ARRAY -> builder.defineListAllowEmpty(List.of(data.getId()), () -> {
				try
				{
					return Arrays.asList(data.getStringArray());
				}
				catch (IllegalAccessException e)
				{
					Constants.LOG.error("Failed to read default for config property {}", data.getId(), e);
				}
				return new ArrayList<>();
			}, obj -> obj instanceof String);
			default -> throw new IllegalAccessException("Unknown property type.");
		}
	}
}
