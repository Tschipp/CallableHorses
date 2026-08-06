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

package tschipp.callablehorses.common;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.common.config.CallableHorsesConfig;
import tschipp.callablehorses.common.data.HorseOwnerData;
import tschipp.callablehorses.common.data.StoredHorseData;
import tschipp.callablehorses.common.helper.HorseHelper;
import tschipp.callablehorses.mixin.AbstractHorseAccessor;
import tschipp.callablehorses.networking.clientbound.ClientboundOwnerSyncShowStatsPacket;
import tschipp.callablehorses.networking.clientbound.ClientboundPlayWhistlePacket;
import tschipp.callablehorses.platform.Services;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HorseManager
{
	private static CallableHorsesConfig.Settings settings()
	{
		return Constants.COMMON_CONFIG.settings;
	}

	public static boolean callHorse(Player player)
	{
		if (player == null)
			return false;

		HorseOwnerData horseOwner = HorseHelper.getOwnerData(player);
		if (horseOwner == null)
			return false;

		if (horseOwner.getHorseNBT().isEmpty())
		{
			player.displayClientMessage(Component.translatable("callablehorses.error.nohorse").withStyle(ChatFormatting.RED), true);
			return false;
		}

		Optional<EntityType<?>> storedType = EntityType.by(horseOwner.getUpgradedHorseNBT());
		if (storedType.isEmpty() || !HorseHelper.isCallableType(storedType.get()))
		{
			player.displayClientMessage(Component.translatable("callablehorses.error.nolongercallable").withStyle(ChatFormatting.RED), true);
			return false;
		}

		if (!canCallHorse(player))
			return false;

		player.level().playSound(player, player.blockPosition(), WhistleSounds.WHISTLE, SoundSource.PLAYERS, 1f,
				(float) (1.4 + player.getRandom().nextGaussian() / 3));

		if (player instanceof ServerPlayer serverPlayer)
			Services.PLATFORM.sendPacketToPlayer(new ClientboundPlayWhistlePacket(), serverPlayer);

		Mob e = findHorseWithStorageID(horseOwner.getStorageUUID(), player.level());
		if (e != null)
		{
			StoredHorseData horse = HorseHelper.getHorseData(e);
			if (horse != null && horse.getStorageUUID().equals(horseOwner.getStorageUUID()))
			{
				if (e.level().dimensionType() == player.level().dimensionType())
				{
					e.ejectPassengers();

					if (e.position().distanceTo(player.position()) <= settings().horseWalkRange)
					{
						AttributeInstance followRange = e.getAttribute(Attributes.FOLLOW_RANGE);
						if (followRange != null)
						{
							followRange.removeModifier(Constants.CALL_FOLLOW_RANGE_MODIFIER);
							double missing = settings().horseWalkRange - followRange.getValue();
							if (missing > 0)
								followRange.addTransientModifier(new AttributeModifier(
										Constants.CALL_FOLLOW_RANGE_MODIFIER, missing,
										AttributeModifier.Operation.ADD_VALUE));
						}
						e.getNavigation().moveTo(player, settings().horseWalkSpeed);
					}
					else
					{
						// TP-ing the horse
						e.setPos(player.getX(), player.getY(), player.getZ());
						e.getNavigation().moveTo(player, settings().horseWalkSpeed);
					}

					HorseHelper.setHorseLastSeen(player);
					HorseHelper.sendHorseUpdateInRange(e);
					return true;
				}
				else
				{
					// Removing any loaded horses in other dims when a new one is spawned
					HorseManager.saveHorse(e);
					e.setPos(e.getX(), -200, e.getZ());
					e.discard();
				}
			}
		}

		// Spawning a new horse with a new num
		Mob newHorse = horseOwner.createHorseEntity(player.level());
		if (newHorse == null)
			return false;

		newHorse.setPos(player.getX(), player.getY(), player.getZ());
		player.level().addFreshEntity(newHorse);

		StoredHorseData h = HorseHelper.getHorseData(newHorse);
		if (h != null)
			HorseHelper.setHorseNum((ServerLevel) newHorse.level(), h.getStorageUUID(), h.getHorseNum());

		HorseHelper.sendHorseUpdateInRange(newHorse);
		HorseHelper.setHorseLastSeen(player);
		return true;
	}

	public static void setHorse(Player player)
	{
		if (player == null)
			return;

		if (player.getVehicle() == null)
		{
			player.displayClientMessage(Component.translatable("callablehorses.error.notriding").withStyle(ChatFormatting.RED), true);
			return;
		}

		Entity e = player.getVehicle();
		if (!HorseHelper.isCallable(e))
		{
			player.displayClientMessage(Component.translatable("callablehorses.error.notcallable").withStyle(ChatFormatting.RED), true);
			return;
		}
		Mob ridden = (Mob) e;

		if (!canSetHorse(player, e))
			return;

		// Binding path, so this attaches the data if the mount has never been claimed before
		StoredHorseData storedHorse = HorseHelper.getOrCreateHorseData(e);
		if (storedHorse == null || !HorseHelper.canSerialize(e))
		{
			player.displayClientMessage(Component.translatable("callablehorses.error.notcallable").withStyle(ChatFormatting.RED), true);
			return;
		}

		String owner = storedHorse.getOwnerUUID();
		String playerID = player.getGameProfile().getId().toString();
		boolean owned = storedHorse.isOwned();

		if (owned && !owner.equals(playerID))
		{
			player.displayClientMessage(Component.translatable("callablehorses.error.alreadyowned").withStyle(ChatFormatting.RED), true);
			return;
		}

		if (owned && owner.equals(playerID))
		{
			player.displayClientMessage(Component.translatable("callablehorses.error.alreadypersonal").withStyle(ChatFormatting.RED), true);
			return;
		}

		HorseOwnerData horseOwner = HorseHelper.getOwnerData(player);
		String ownedID = horseOwner.getStorageUUID();

		// Marking any old horses as disbanded
		if (!ownedID.isEmpty())
		{
			Entity ent = findHorseWithStorageID(ownedID, player.level());
			if (ent != null)
			{
				clearHorse(HorseHelper.getHorseData(ent));
			}
			else
			{
				HorseHelper.disbandHorse((ServerLevel) player.level(), ownedID);
			}
		}
		horseOwner.clearHorse();

		if (!horseOwner.setHorse(ridden, player))
		{
			Constants.LOG.error("Binding {} failed after its previous horse was already disbanded", ridden);
			player.displayClientMessage(Component.translatable("callablehorses.error.notcallable").withStyle(ChatFormatting.RED), true);
			return;
		}

		HorseHelper.setHorseLastSeen(player);
		HorseHelper.setHorseNum((ServerLevel) e.level(), storedHorse.getStorageUUID(), storedHorse.getHorseNum());
		player.displayClientMessage(Component.translatable("callablehorses.success"), true);
		HorseHelper.sendHorseUpdateInRange(e);
	}

	public static void showHorseStats(ServerPlayer player)
	{
		HorseOwnerData owner = HorseHelper.getOwnerData(player);

		if (owner.getHorseNBT().isEmpty())
		{
			player.displayClientMessage(Component.translatable("callablehorses.error.nohorse").withStyle(ChatFormatting.RED), true);
			return;
		}

		Entity e = findHorseWithStorageID(owner.getStorageUUID(), player.level());
		if (e != null)
			HorseManager.saveHorse(e);

		Services.PLATFORM.sendPacketToPlayer(new ClientboundOwnerSyncShowStatsPacket(owner), player);
	}

	public static void clearHorse(@Nullable StoredHorseData horse)
	{
		if (horse == null)
			return;

		horse.setOwned(false);
		horse.setHorseNum(0);
		horse.setOwnerUUID("");
		horse.setStorageUUID("");
	}

	@Nullable
	public static Mob findHorseWithStorageID(String id, Level world)
	{
		MinecraftServer server = world.getServer();
		if (server == null || id == null || id.isEmpty())
			return null;

		List<Entity> entities = new ArrayList<>();
		for (ServerLevel w : server.getAllLevels())
			entities.addAll(ImmutableList.copyOf(w.getAllEntities()));

		for (Entity e : entities)
		{
			if (e instanceof Mob mob)
			{
				StoredHorseData horse = HorseHelper.getHorseData(mob);
				if (horse != null && horse.getStorageUUID().equals(id))
					return mob;
			}
		}

		return null;
	}


	/**
	 * Whether everything this mount carries can be stripped off it through the entity API.
	 *
	 * This one predicate decides the whole death policy. True means the copy can be emptied, so
	 * letting vanilla drop the gear is safe and the two halves agree. False means some of it
	 * cannot be reached - a Saddleable mount keeps its saddle in synched data with no un-equip
	 * method, for one - so nothing may drop and the copy is left whole instead. Either way the
	 * item exists exactly once afterwards.
	 */
	public static boolean canClearCarriedItems(Entity e)
	{
		return e instanceof AbstractHorse;
	}

	/** Clears whatever vanilla is about to drop, and heals the mount back to full. */
	public static void prepDeadHorseForRespawning(Entity e)
	{
		if (!(e instanceof LivingEntity mount))
			return;

		// Only for mounts whose gear vanilla still drops. For the rest the drops are cancelled
		// outright, so clearing these would destroy the items rather than preserve them.
		if (canClearCarriedItems(mount))
		{
			for (EquipmentSlot slot : EquipmentSlot.values())
				mount.setItemSlot(slot, ItemStack.EMPTY);

			AbstractHorse horse = (AbstractHorse) mount;
			((AbstractHorseAccessor) horse).callablehorses$getInventory().clearContent();

			if (horse instanceof AbstractChestedHorse chested)
				chested.setChest(false);
		}

		mount.clearFire();
		mount.setHealth(mount.getMaxHealth());

		// The snapshot is now taken while the mount is dying, so its death and hurt counters come
		// back with it. Harmless once the health is restored, since tickDeath only runs while
		// isDeadOrDying, but they would otherwise persist in the stored copy forever.
		mount.deathTime = 0;
		mount.hurtTime = 0;
	}

	public static boolean canCallHorse(Player player)
	{
		Mob probe = HorseHelper.getOwnerData(player).createHorseEntity(player.level());

		if (isAreaProtected(player, probe))
		{
			player.displayClientMessage(Component.translatable("callablehorses.error.area").withStyle(ChatFormatting.RED), true);
			return false;
		}

		if (player.getVehicle() != null)
		{
			player.displayClientMessage(Component.translatable("callablehorses.error.riding").withStyle(ChatFormatting.RED), true);
			return false;
		}

		if (settings().checkForSpace)
		{
			double startX = player.getX() - 1;
			double startY = player.getY();
			double startZ = player.getZ() - 1;

			double endX = player.getX() + 1;
			double endY = player.getY() + 2;
			double endZ = player.getZ() + 1;

			Level world = player.level();

			for (double x = startX; x <= endX; x++)
			{
				for (double y = startY; y <= endY; y++)
				{
					for (double z = startZ; z <= endZ; z++)
					{
						BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
						BlockState state = world.getBlockState(pos);
						if (state.getCollisionShape(world, pos) != Shapes.empty())
						{
							player.displayClientMessage(Component.translatable("callablehorses.error.nospace").withStyle(ChatFormatting.RED), true);
							return false;
						}
					}
				}
			}

			if (probe != null)
			{
				probe.setPos(player.getX(), player.getY(), player.getZ());
				if (!world.noCollision(probe, probe.getBoundingBox()))
				{
					player.displayClientMessage(Component.translatable("callablehorses.error.nospace").withStyle(ChatFormatting.RED), true);
					return false;
				}
			}
		}

		if (!settings().callableInEveryDimension)
		{
			String playerDim = player.level().dimension().location().toString();

			for (String allowed : settings().callableDimsWhitelist)
			{
				if (allowed.equals(playerDim))
					return true;
			}

			player.displayClientMessage(Component.translatable("callablehorses.error.dim").withStyle(ChatFormatting.RED), true);
			return false;
		}

		int maxDistance = settings().maxCallingDistance;
		if (maxDistance != -1)
		{
			HorseOwnerData owner = HorseHelper.getOwnerData(player);
			Vec3 lastSeenPos = owner.getLastSeenPosition();
			ResourceKey<Level> lastSeenDim = owner.getLastSeenDim();

			if (lastSeenPos.equals(Vec3.ZERO))
				return true;

			MinecraftServer server = player.level().getServer();

			Entity livingHorse = findHorseWithStorageID(owner.getStorageUUID(), player.level());
			if (livingHorse != null)
			{
				lastSeenPos = livingHorse.position();
				lastSeenDim = livingHorse.level().dimension();
			}

			ServerLevel horseLevel = server.getLevel(lastSeenDim);
			double movementFactorHorse = (horseLevel == null ? player.level() : horseLevel).dimensionType().coordinateScale();
			double movementFactorOwner = player.level().dimensionType().coordinateScale();

			double movementFactorTotal = movementFactorHorse > movementFactorOwner
					? movementFactorHorse / movementFactorOwner
					: movementFactorOwner / movementFactorHorse;

			double distance = lastSeenPos.distanceTo(player.position()) / movementFactorTotal;
			if (distance <= maxDistance)
				return true;

			player.displayClientMessage(Component.translatable("callablehorses.error.range").withStyle(ChatFormatting.RED), true);
			return false;
		}

		return true;
	}

	public static boolean canSetHorse(Player player, Entity entity)
	{
		if (isAreaProtected(player, entity))
		{
			player.displayClientMessage(Component.translatable("callablehorses.error.setarea").withStyle(ChatFormatting.RED), true);
			return false;
		}

		return true;
	}

	public static void saveHorse(Entity e)
	{
		saveHorse(e, false);
	}

	/**
	 * @param includeDying store the mount even though it is already dying. Only the death handler
	 *        passes true: the copy it rebuilds is healed by {@link #prepDeadHorseForRespawning}
	 *        anyway, and for a mount whose drops are suppressed the stored snapshot is the only
	 *        surviving record of what it was carrying when it died. Everything else must keep the
	 *        guard, or an ordinary chunk unload during the death tick would store a dead copy.
	 */
	public static void saveHorse(Entity e, boolean includeDying)
	{
		if (!(e instanceof LivingEntity mount) || (!includeDying && mount.isDeadOrDying()))
			return;

		Level world = e.level();
		StoredHorseData horse = HorseHelper.getHorseData(e);
		if (horse == null || !horse.isOwned())
			return;

		CompoundTag nbt = HorseHelper.serializeEntity(e);
		if (nbt.isEmpty())
			return;

		Player owner = HorseHelper.getPlayerFromUUID(horse.getOwnerUUID(), world);

		if (owner != null)
		{
			// Owner is online
			HorseOwnerData horseOwner = HorseHelper.getOwnerData(owner);
			if (horseOwner != null)
			{
				horseOwner.setHorseNBT(nbt);
				horseOwner.setLastSeenDim(e.level().dimension());
				horseOwner.setLastSeenPosition(e.position());
				return;
			}
		}

		// Owner is offline. Recorded in every level's data, because the pickup in
		// onEntityJoinLevel reads whichever level the owner logs back into.
		HorseHelper.addOfflineSavedHorse((ServerLevel) world, horse.getStorageUUID(), nbt);
	}

	private static boolean isAreaProtected(Player player, @Nullable Entity probe)
	{
		if (probe == null)
		{
			HorseOwnerData owner = HorseHelper.getOwnerData(player);
			probe = owner.createHorseEntity(player.level());

			if (probe == null)
				return false;
		}

		// canSetHorse hands in the mount the player is riding, not a throwaway copy. Since the
		// player is a passenger, player.getY() is the mount's own Y plus the attachment offset,
		// so moving it there would shove it about a block upward on every bind attempt. The
		// position is put back either way.
		Vec3 original = probe.position();
		probe.setPos(player.getX(), player.getY(), player.getZ());

		try
		{
			return Services.PLATFORM.isAreaProtected(player, probe);
		}
		finally
		{
			probe.setPos(original.x, original.y, original.z);
		}
	}


	public static boolean shouldSuppressDrops(LivingEntity entity)
	{
		return settings().disableHorseDrops && HorseHelper.isOwnedHorse(entity);
	}

	public static boolean shouldSuppressAllDeathLoot(LivingEntity entity)
	{
		return !settings().deathIsPermanent
				&& !canClearCarriedItems(entity)
				&& HorseHelper.isOwnedHorse(entity);
	}
}
