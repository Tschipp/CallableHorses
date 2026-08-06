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

package tschipp.callablehorses.common.worlddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import tschipp.callablehorses.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoredHorsesWorldData extends SavedData
{
	private static final String NAME = Constants.MOD_ID + "_stored_horses";

	private static final String KEY_ENTRIES = "entries";
	private static final String KEY_KILLED = "killed";
	private static final String KEY_DISBANDED = "disbanded";
	private static final String KEY_OFFLINE_SAVED = "offlineSaved";

	private static final MapCodec<StoredHorsesWorldData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf(KEY_ENTRIES).forGetter(data -> data.entries),
			Codec.STRING.listOf().optionalFieldOf(KEY_KILLED, List.of()).forGetter(data -> data.killedHorses),
			Codec.STRING.listOf().optionalFieldOf(KEY_DISBANDED, List.of()).forGetter(data -> data.disbandedHorses),
			Codec.unboundedMap(Codec.STRING, CompoundTag.CODEC).optionalFieldOf(KEY_OFFLINE_SAVED, Map.of()).forGetter(data -> data.offlineSavedHorses)
	).apply(instance, StoredHorsesWorldData::new));

	/** storage UUID to horse generation counter, used for anti-dupe comparisons. */
	private final Map<String, Integer> entries = new HashMap<>();
	private final List<String> killedHorses = new ArrayList<>();
	private final List<String> disbandedHorses = new ArrayList<>();
	private final Map<String, CompoundTag> offlineSavedHorses = new HashMap<>();

	public StoredHorsesWorldData()
	{
	}

	private StoredHorsesWorldData(Map<String, Integer> entries, List<String> killedHorses, List<String> disbandedHorses,
			Map<String, CompoundTag> offlineSavedHorses)
	{
		this.entries.putAll(entries);
		this.killedHorses.addAll(killedHorses);
		this.disbandedHorses.addAll(disbandedHorses);
		this.offlineSavedHorses.putAll(offlineSavedHorses);
	}

	public static StoredHorsesWorldData load(CompoundTag nbt, HolderLookup.Provider provider)
	{
		if (!nbt.contains(KEY_ENTRIES, Tag.TAG_COMPOUND))
			return loadLegacy(nbt);


		return CODEC.codec().parse(NbtOps.INSTANCE, nbt)
				.resultOrPartial(error -> Constants.LOG.error("Failed to read stored horse data: {}", error))
				.orElseGet(StoredHorsesWorldData::new);
	}

	private static StoredHorsesWorldData loadLegacy(CompoundTag nbt)
	{
		StoredHorsesWorldData data = new StoredHorsesWorldData();

		int i = 0;
		while (nbt.contains("" + i))
		{
			CompoundTag subTag = nbt.getCompound("" + i);
			data.entries.put(subTag.getString("id"), subTag.getInt("num"));
			i++;
		}

		i = 0;
		CompoundTag killed = nbt.getCompound(KEY_KILLED);
		while (killed.contains("" + i))
		{
			data.killedHorses.add(killed.getCompound("" + i).getString("id"));
			i++;
		}

		i = 0;
		CompoundTag disbanded = nbt.getCompound(KEY_DISBANDED);
		while (disbanded.contains("" + i))
		{
			data.disbandedHorses.add(disbanded.getCompound("" + i).getString("id"));
			i++;
		}

		return data;
	}

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag compound, HolderLookup.@NotNull Provider provider)
	{
		CODEC.codec().encodeStart(NbtOps.INSTANCE, this)
				.resultOrPartial(error -> Constants.LOG.error("Failed to write stored horse data: {}", error))
				.ifPresent(encoded -> {
					if (encoded instanceof CompoundTag encodedCompound)
						compound.merge(encodedCompound);
				});

		return compound;
	}

	public void addHorseNum(String id, int num)
	{
		entries.put(id, num);
		this.setDirty();
	}

	public int getHorseNum(String id)
	{
		Integer num = entries.get(id);
		return num == null ? 0 : num;
	}

	public void disbandHorse(String id)
	{
		disbandedHorses.add(id);
		this.setDirty();
	}

	public boolean isDisbanded(String id)
	{
		return disbandedHorses.contains(id);
	}

	public void clearDisbanded(String id)
	{
		disbandedHorses.remove(id);
		this.setDirty();
	}

	public void markKilled(String id)
	{
		killedHorses.add(id);
		this.setDirty();
	}

	public boolean wasKilled(String id)
	{
		return killedHorses.contains(id);
	}

	public void clearKilled(String id)
	{
		killedHorses.remove(id);
		this.setDirty();
	}

	public void addOfflineSavedHorse(String id, CompoundTag nbt)
	{
		offlineSavedHorses.put(id, nbt);
		this.setDirty();
	}

	public boolean wasOfflineSaved(String id)
	{
		return offlineSavedHorses.containsKey(id);
	}

	public CompoundTag getOfflineSavedHorse(String id)
	{
		return offlineSavedHorses.get(id);
	}

	public void clearOfflineSavedHorse(String id)
	{
		offlineSavedHorses.remove(id);
		this.setDirty();
	}

	public static StoredHorsesWorldData getInstance(Level level)
	{
		if (!(level instanceof ServerLevel serverLevel))
			throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");

		return serverLevel.getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(StoredHorsesWorldData::new, StoredHorsesWorldData::load, DataFixTypes.LEVEL),
				NAME);
	}
}
