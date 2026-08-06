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

package tschipp.callablehorses.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import tschipp.callablehorses.Constants;

/**
 * Binding data held by every callable mount. Attached via data attachments on
 * Fabric/NeoForge and via a capability on Forge.
 */
public class StoredHorseData
{
	public static final Codec<StoredHorseData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("storage", "").forGetter(StoredHorseData::getStorageUUID),
			Codec.STRING.optionalFieldOf("owner", "").forGetter(StoredHorseData::getOwnerUUID),
			Codec.INT.optionalFieldOf("horseNum", 0).forGetter(StoredHorseData::getHorseNum),
			Codec.BOOL.optionalFieldOf("owned", false).forGetter(StoredHorseData::isOwned)
	).apply(instance, StoredHorseData::new));

	public static final StreamCodec<ByteBuf, StoredHorseData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

	private String storageUUID = "";
	private String ownerUUID = "";
	private int horseNum = 0;
	private boolean owned = false;

	public StoredHorseData()
	{
	}

	private StoredHorseData(String storageUUID, String ownerUUID, int horseNum, boolean owned)
	{
		this.storageUUID = storageUUID;
		this.ownerUUID = ownerUUID;
		this.horseNum = horseNum;
		this.owned = owned;
	}

	public String getStorageUUID()
	{
		return storageUUID;
	}

	public void setStorageUUID(String uuid)
	{
		this.storageUUID = uuid;
	}

	public String getOwnerUUID()
	{
		return ownerUUID;
	}

	public void setOwnerUUID(String uuid)
	{
		this.ownerUUID = uuid;
	}

	public int getHorseNum()
	{
		return horseNum;
	}

	public void setHorseNum(int num)
	{
		this.horseNum = num;
	}

	public boolean isOwned()
	{
		return owned;
	}

	public void setOwned(boolean owned)
	{
		this.owned = owned;
	}

	public CompoundTag toNbt()
	{
		Tag encoded = CODEC.encodeStart(NbtOps.INSTANCE, this)
				.resultOrPartial(error -> Constants.LOG.error("Failed to write stored horse data: {}", error))
				.orElseGet(CompoundTag::new);

		return encoded instanceof CompoundTag tag ? tag : new CompoundTag();
	}

	public static StoredHorseData fromNbt(CompoundTag tag)
	{
		return CODEC.parse(NbtOps.INSTANCE, tag)
				.resultOrPartial(error -> Constants.LOG.error("Failed to read stored horse data: {}", error))
				.orElseGet(StoredHorseData::new);
	}

	public void readNbt(CompoundTag tag)
	{
		copyFrom(fromNbt(tag));
	}

	public void copyFrom(StoredHorseData other)
	{
		this.storageUUID = other.storageUUID;
		this.ownerUUID = other.ownerUUID;
		this.horseNum = other.horseNum;
		this.owned = other.owned;
	}

	public StoredHorseData copy()
	{
		StoredHorseData copy = new StoredHorseData();
		copy.copyFrom(this);
		return copy;
	}
}
