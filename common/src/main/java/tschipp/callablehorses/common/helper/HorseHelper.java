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

package tschipp.callablehorses.common.helper;

import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.common.data.HorseOwnerData;
import tschipp.callablehorses.common.data.StoredHorseData;
import tschipp.callablehorses.common.worlddata.StoredHorsesWorldData;
import tschipp.callablehorses.networking.clientbound.ClientboundHorseDataSyncPacket;
import tschipp.callablehorses.platform.Services;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

public class HorseHelper
{
	private static final int LEGACY_DATA_VERSION = 3465;

	public static boolean isCallable(Entity entity)
	{
		return entity instanceof Mob && isCallableType(entity.getType());
	}

	public static boolean isCallableType(EntityType<?> type)
	{
		return type.is(Constants.CALLABLE_HORSES);
	}

	public static HorseOwnerData getOwnerData(Player player)
	{
		return Services.PLATFORM.getOwnerData(player);
	}

	@Nullable
	public static StoredHorseData getHorseData(Entity horse)
	{
		return Services.PLATFORM.getHorseData(horse);
	}

	@Nullable
	public static StoredHorseData getOrCreateHorseData(Entity horse)
	{
		return Services.PLATFORM.getOrCreateHorseData(horse);
	}

	public static boolean isOwnedHorse(Entity entity)
	{
		StoredHorseData horse = getHorseData(entity);
		return horse != null && horse.isOwned();
	}

	public static boolean canSerialize(Entity entity)
	{
		EntityType<?> type = entity.getType();
		return type.canSerialize() && EntityType.getKey(type) != null;
	}

	public static CompoundTag serializeEntity(Entity entity)
	{
		CompoundTag tag = new CompoundTag();

		if (!canSerialize(entity))
			return tag;

		tag.putString("id", EntityType.getKey(entity.getType()).toString());
		entity.saveWithoutId(tag);

		NbtUtils.addCurrentDataVersion(tag);
		return tag;
	}

	public static CompoundTag upgradeStoredEntity(CompoundTag tag)
	{
		if (tag.isEmpty())
			return tag;

		int version = NbtUtils.getDataVersion(tag, LEGACY_DATA_VERSION);
		int current = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
		if (version >= current)
			return tag;

		Tag upgraded = DataFixers.getDataFixer()
				.update(References.ENTITY_TREE, new Dynamic<>(NbtOps.INSTANCE, tag), version, current)
				.getValue();

		if (!(upgraded instanceof CompoundTag upgradedTag))
		{
			Constants.LOG.error("Datafixing a stored horse from version {} did not produce a compound", version);
			return tag;
		}

		return NbtUtils.addCurrentDataVersion(upgradedTag);
	}

	public static void sendHorseUpdateInRange(Entity horse)
	{
		StoredHorseData storedHorse = getHorseData(horse);
		if (storedHorse == null)
			return;

		Services.PLATFORM.sendPacketToPlayersTracking(new ClientboundHorseDataSyncPacket(horse.getId(), storedHorse), horse);
	}

	public static void sendHorseUpdateToClient(Entity horse, Player player)
	{
		StoredHorseData storedHorse = getHorseData(horse);
		if (storedHorse == null || !(player instanceof ServerPlayer serverPlayer))
			return;

		Services.PLATFORM.sendPacketToPlayer(new ClientboundHorseDataSyncPacket(horse.getId(), storedHorse), serverPlayer);
	}

	@Nullable
	public static Player getPlayerFromUUID(String uuid, Level world)
	{
		if (uuid == null || uuid.isEmpty())
			return null;

		MinecraftServer server = world.getServer();
		if (server == null)
			return null;

		try
		{
			return server.getPlayerList().getPlayer(UUID.fromString(uuid));
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	private static void forEachWorldData(ServerLevel world, Consumer<StoredHorsesWorldData> action)
	{
		world.getServer().getAllLevels().forEach(serverworld -> action.accept(StoredHorsesWorldData.getInstance(serverworld)));
	}

	public static void setHorseNum(ServerLevel world, String storageid, int num)
	{
		forEachWorldData(world, data -> data.addHorseNum(storageid, num));
	}

	public static void markKilled(ServerLevel world, String storageId)
	{
		forEachWorldData(world, data -> data.markKilled(storageId));
	}

	public static void clearKilled(ServerLevel world, String storageId)
	{
		forEachWorldData(world, data -> data.clearKilled(storageId));
	}

	public static void disbandHorse(ServerLevel world, String storageId)
	{
		forEachWorldData(world, data -> data.disbandHorse(storageId));
	}

	public static void clearDisbanded(ServerLevel world, String storageId)
	{
		forEachWorldData(world, data -> data.clearDisbanded(storageId));
	}

	public static void addOfflineSavedHorse(ServerLevel world, String storageId, CompoundTag nbt)
	{
		forEachWorldData(world, data -> data.addOfflineSavedHorse(storageId, nbt));
	}

	public static void clearOfflineSavedHorse(ServerLevel world, String storageId)
	{
		forEachWorldData(world, data -> data.clearOfflineSavedHorse(storageId));
	}

	public static int getHorseNum(ServerLevel world, String storageid)
	{
		return StoredHorsesWorldData.getInstance(world).getHorseNum(storageid);
	}

	public static void setHorseLastSeen(Player player)
	{
		HorseOwnerData owner = getOwnerData(player);
		owner.setLastSeenPosition(player.position());
		owner.setLastSeenDim(player.level().dimension());
	}

	public static StoredHorsesWorldData getWorldData(ServerLevel world)
	{
		return StoredHorsesWorldData.getInstance(world);
	}
}
