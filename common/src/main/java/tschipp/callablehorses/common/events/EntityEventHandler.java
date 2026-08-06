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

package tschipp.callablehorses.common.events;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.common.HorseManager;
import tschipp.callablehorses.common.data.HorseOwnerData;
import tschipp.callablehorses.common.data.StoredHorseData;
import tschipp.callablehorses.common.helper.HorseHelper;
import tschipp.callablehorses.common.worlddata.StoredHorsesWorldData;


public class EntityEventHandler
{
	// Returns true if the entity is a stale duplicate and should not exist.
	public static boolean onEntityJoinLevel(Entity joiningEntity, Level world)
	{
		if (world.isClientSide)
			return false;

		if (joiningEntity instanceof Player player)
		{
			onPlayerJoinLevel(player, world);
			return false;
		}

		StoredHorseData horse = HorseHelper.getHorseData(joiningEntity);
		if (horse == null || !horse.isOwned())
			return false;

		StoredHorsesWorldData data = HorseHelper.getWorldData((ServerLevel) world);
		int globalNum = HorseHelper.getHorseNum((ServerLevel) joiningEntity.level(), horse.getStorageUUID());

		if (globalNum > horse.getHorseNum())
		{
			Constants.LOG.debug("{} was instantly despawned because its number is {} and the global num is {}",
					joiningEntity, horse.getHorseNum(), globalNum);
			return true;
		}

		if (data.isDisbanded(horse.getStorageUUID()))
		{
			HorseManager.clearHorse(horse);
			HorseHelper.clearDisbanded((ServerLevel) world, horse.getStorageUUID());
		}

		return false;
	}

	private static void onPlayerJoinLevel(Player player, Level world)
	{
		HorseOwnerData owner = HorseHelper.getOwnerData(player);
		if (owner == null)
			return;

		String ownedHorse = owner.getStorageUUID();
		if (ownedHorse.isEmpty())
			return;

		StoredHorsesWorldData data = HorseHelper.getWorldData((ServerLevel) world);

		if (data.wasKilled(ownedHorse))
		{
			HorseHelper.clearKilled((ServerLevel) world, ownedHorse);

			HorseHelper.clearOfflineSavedHorse((ServerLevel) world, ownedHorse);

			// A horse that cannot be rebuilt is treated as permanently dead. Keeping the old data
			// would hand back gear that already dropped where it died.
			boolean respawned = !Constants.COMMON_CONFIG.settings.deathIsPermanent
					&& respawnStoredHorse(owner, world);

			if (!respawned)
			{
				owner.clearHorse();
				player.displayClientMessage(Component.translatable("callablehorses.alert.offlinedeath").withStyle(ChatFormatting.RED), false);
			}
		}

		if (data.wasOfflineSaved(ownedHorse))
		{
			CompoundTag newNBT = data.getOfflineSavedHorse(ownedHorse);
			owner.setHorseNBT(newNBT);
			HorseHelper.clearOfflineSavedHorse((ServerLevel) world, ownedHorse);
		}
	}

	/**
	 * Replaces the owner's stored horse with a stripped, healed copy of it.
	 * @return false if no such copy could be produced, in which case nothing was written and the
	 *         caller must treat the death as permanent.
	 */
	private static boolean respawnStoredHorse(HorseOwnerData owner, Level level)
	{
		Mob deadHorse = owner.createHorseEntity(level);
		if (deadHorse == null || !HorseHelper.canSerialize(deadHorse))
			return false;

		HorseManager.prepDeadHorseForRespawning(deadHorse);

		CompoundTag respawned = HorseHelper.serializeEntity(deadHorse);
		if (respawned.isEmpty())
			return false;

		owner.setHorseNBT(respawned);
		owner.setLastSeenPosition(Vec3.ZERO);
		return true;
	}

	public static void onPlayerClone(HorseOwnerData oldHorse, HorseOwnerData newHorse)
	{
		if (oldHorse == null || newHorse == null)
			return;

		newHorse.setHorseNBT(oldHorse.getHorseNBT());
		newHorse.setHorseNum(oldHorse.getHorseNum());
		newHorse.setStorageUUID(oldHorse.getStorageUUID());

		newHorse.setLastSeenPosition(oldHorse.getLastSeenPosition());
		newHorse.setLastSeenDim(oldHorse.getLastSeenDim());
	}

	public static void onEntityLeaveLevel(Entity entity, Level world)
	{
		if (!world.isClientSide)
			HorseManager.saveHorse(entity);
	}

	public static void onStopTracking(Player player, Entity target)
	{
		if (!player.level().isClientSide && target.isAlive())
			HorseManager.saveHorse(target);
	}

	public static void onStartTracking(Player player, Entity target)
	{
		if (!player.level().isClientSide)
			HorseHelper.sendHorseUpdateToClient(target, player);
	}

	public static void onLivingTick(LivingEntity entity)
	{
		if (entity.level().isClientSide)
			return;

		boolean debug = Constants.COMMON_CONFIG.settings.enableDebug;
		boolean antiDupe = Constants.COMMON_CONFIG.settings.continuousAntiDupeChecking;
		if (!debug && !antiDupe)
			return;

		StoredHorseData horse = HorseHelper.getHorseData(entity);
		if (horse == null)
			return;

		if (debug)
		{
			entity.setCustomName(Component.literal("Is Owned: " + horse.isOwned()
					+ ", Storage UUID: " + horse.getStorageUUID()
					+ ", Horse Number: " + horse.getHorseNum()
					+ ", Horse UUID: " + entity.getUUID()));
		}

		if (antiDupe)
		{
			int globalNum = HorseHelper.getHorseNum((ServerLevel) entity.level(), horse.getStorageUUID());
			if (globalNum > horse.getHorseNum())
				entity.discard();
		}
	}

	public static void onLivingDeath(LivingEntity entity)
	{
		if (entity.level().isClientSide)
			return;

		StoredHorseData horse = HorseHelper.getHorseData(entity);
		if (horse == null || !horse.isOwned())
			return;

		Player owner = HorseHelper.getPlayerFromUUID(horse.getOwnerUUID(), entity.level());
		if (owner != null)
		{
			HorseOwnerData horseOwner = HorseHelper.getOwnerData(owner);

			if (!Constants.COMMON_CONFIG.settings.deathIsPermanent)
			{
				HorseManager.saveHorse(entity, true);

				if (respawnStoredHorse(horseOwner, owner.level()))
					return;

				// The gear is lost: drops were suppressed for this mount, and a stored copy that
				// cannot be rebuilt cannot hand them back either.
				Constants.LOG.warn("Could not rebuild {}'s personal horse after death, clearing it instead",
						owner.getGameProfile().getName());
			}

			horseOwner.clearHorse();
			owner.displayClientMessage(Component.translatable("callablehorses.alert.death").withStyle(ChatFormatting.RED), false);
		}
		else
		{
			Constants.LOG.debug("{} was marked as killed.", entity);
			HorseHelper.markKilled((ServerLevel) entity.level(), horse.getStorageUUID());
		}
	}
}
