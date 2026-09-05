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
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.common.helper.HorseHelper;
import tschipp.callablehorses.platform.Services;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * Ownership data held by every {@code Player}. Attached via data attachments on
 * Fabric/NeoForge and via a capability on Forge.
 */
public class HorseOwnerData
{
	private static final ResourceKey<Level> DEFAULT_DIM =
			ResourceKey.create(Registries.DIMENSION, Identifier.withDefaultNamespace("overworld"));

	private static final Codec<Vec3> LAST_SEEN_POS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("X").forGetter(pos -> (int) pos.x),
			Codec.INT.fieldOf("Y").forGetter(pos -> (int) pos.y),
			Codec.INT.fieldOf("Z").forGetter(pos -> (int) pos.z)
	).apply(instance, (x, y, z) -> new Vec3(x, y, z)));

	private static final Codec<ResourceKey<Level>> DIMENSION_CODEC = Codec.STRING.xmap(
			id -> {
				Identifier location = Identifier.tryParse(id);
				return location == null ? DEFAULT_DIM : ResourceKey.create(Registries.DIMENSION, location);
			},
			key -> key.identifier().toString());

	public static final Codec<HorseOwnerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CompoundTag.CODEC.optionalFieldOf("horse").forGetter(data -> Optional.of(data.horseNBT)),
			Codec.INT.optionalFieldOf("horseNum", 0).forGetter(HorseOwnerData::getHorseNum),
			Codec.STRING.optionalFieldOf("uuid", "").forGetter(HorseOwnerData::getStorageUUID),
			LAST_SEEN_POS_CODEC.optionalFieldOf("lastSeenPos", Vec3.ZERO).forGetter(HorseOwnerData::getLastSeenPosition),
			DIMENSION_CODEC.optionalFieldOf("lastSeenDim", DEFAULT_DIM).forGetter(HorseOwnerData::getLastSeenDim)
	).apply(instance, HorseOwnerData::new));

	public static final StreamCodec<ByteBuf, HorseOwnerData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

	private int horseNum = 0;
	private CompoundTag horseNBT = new CompoundTag();
	private String storageUUID = "";
	private ResourceKey<Level> lastSeenDim = DEFAULT_DIM;
	private Vec3 lastSeenPos = Vec3.ZERO;

	//Never saved
	private long lastCallTick = -1;

	public HorseOwnerData()
	{
	}

	private HorseOwnerData(Optional<CompoundTag> horseNBT, int horseNum, String storageUUID, Vec3 lastSeenPos,
			ResourceKey<Level> lastSeenDim)
	{
		this.horseNBT = horseNBT.orElseGet(CompoundTag::new);
		this.horseNum = horseNum;
		this.storageUUID = storageUUID;
		this.lastSeenPos = lastSeenPos;
		this.lastSeenDim = lastSeenDim;
	}

	/**
	 * The stored tag, brought up to the running game's data version first.
	 *
	 * Anything that reads the stored horse has to go through here rather than
	 * {@link #getHorseNBT}, or it works off pre-1.21 keys, including the entity id itself, which
	 * a datafixer may have renamed. Written back so the upgrade only runs once per binding.
	 */
	public CompoundTag getUpgradedHorseNBT()
	{
		horseNBT = HorseHelper.upgradeStoredEntity(horseNBT);
		return horseNBT;
	}

	@Nullable
	public Mob createHorseEntity(Level world)
	{
		if (horseNBT.isEmpty())
			return null;

		Optional<EntityType<?>> type = HorseHelper.getStoredType(getUpgradedHorseNBT());

		if (type.isPresent())
		{
			Entity entity = EntityType.loadEntityRecursive(horseNBT, world, EntitySpawnReason.LOAD, EntityProcessor.NOP);
			if (entity instanceof Mob horse)
			{
				horseNum++;

				// Unconditional: a fresh identity is what stops this copy colliding with the one
				// it was built from, whether or not binding data could be attached. On Forge it
				// cannot be, for a mount whose type left the tag before the entity was built, and
				// reusing the old UUID would make addFreshEntity reject the copy outright.
				entity.setUUID(UUID.randomUUID());
				entity.clearFire();
				horse.hurtTime = 0;

				StoredHorseData stored = Services.PLATFORM.getOrCreateHorseData(entity);
				if (stored != null)
					stored.setHorseNum(horseNum);

				return horse;
			}

			Constants.LOG.error("The entity with NBT {} wasn't a mob somehow?...", horseNBT);
		}
		return null;
	}

	/**
	 * @return false if the mount could not be bound, in which case nothing was written.
	 */
	public boolean setHorse(Mob horse, Player player)
	{
		StoredHorseData stored = Services.PLATFORM.getOrCreateHorseData(horse);
		if (stored == null || !HorseHelper.canSerialize(horse))
			return false;

		storageUUID = UUID.randomUUID().toString();

		stored.setHorseNum(horseNum);
		stored.setOwned(true);
		stored.setOwnerUUID(player.getGameProfile().id().toString());
		stored.setStorageUUID(storageUUID);

		// Serialised only now, because the binding above is part of the entity's saved data. Taking
		// the snapshot any earlier stores a copy that still reads as unowned, which then comes back
		// unowned on respawn and stops being handled at all.
		horseNBT = HorseHelper.serializeEntity(horse);
		return true;
	}

	public void clearHorse()
	{
		horseNum = 0;
		horseNBT = new CompoundTag();
		storageUUID = "";
		lastSeenDim = DEFAULT_DIM;
		lastSeenPos = Vec3.ZERO;
	}

	public CompoundTag getHorseNBT()
	{
		return horseNBT;
	}

	public void setHorseNBT(CompoundTag nbt)
	{
		this.horseNBT = nbt;
	}

	public int getHorseNum()
	{
		return horseNum;
	}

	public void setHorseNum(int num)
	{
		this.horseNum = num;
	}

	public String getStorageUUID()
	{
		return storageUUID;
	}

	public void setStorageUUID(String id)
	{
		this.storageUUID = id;
	}

	public long getLastCallTick()
	{
		return lastCallTick;
	}

	public void setLastCallTick(long tick)
	{
		this.lastCallTick = tick;
	}

	public Vec3 getLastSeenPosition()
	{
		return lastSeenPos;
	}

	public void setLastSeenPosition(Vec3 pos)
	{
		this.lastSeenPos = pos;
	}

	public ResourceKey<Level> getLastSeenDim()
	{
		return lastSeenDim;
	}

	public void setLastSeenDim(ResourceKey<Level> dim)
	{
		this.lastSeenDim = dim;
	}

	public CompoundTag toNbt()
	{
		Tag encoded = CODEC.encodeStart(NbtOps.INSTANCE, this)
				.resultOrPartial(error -> Constants.LOG.error("Failed to write horse owner data: {}", error))
				.orElseGet(CompoundTag::new);

		return encoded instanceof CompoundTag tag ? tag : new CompoundTag();
	}

	public static HorseOwnerData fromNbt(CompoundTag tag)
	{
		return CODEC.parse(NbtOps.INSTANCE, tag)
				.resultOrPartial(error -> Constants.LOG.error("Failed to read horse owner data: {}", error))
				.orElseGet(HorseOwnerData::new);
	}

	public void readNbt(CompoundTag tag)
	{
		copyFrom(fromNbt(tag));
	}

	public void copyFrom(HorseOwnerData other)
	{
		this.horseNBT = other.horseNBT;
		this.horseNum = other.horseNum;
		this.storageUUID = other.storageUUID;
		this.lastSeenPos = other.lastSeenPos;
		this.lastSeenDim = other.lastSeenDim;
	}

	public HorseOwnerData copy()
	{
		HorseOwnerData copy = new HorseOwnerData();
		copy.copyFrom(this);
		return copy;
	}
}
