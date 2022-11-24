package tschipp.callablehorses.common.events;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.HorseManager;
import tschipp.callablehorses.common.capabilities.horseowner.HorseOwner;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.capabilities.storedhorse.StoredHorse;
import tschipp.callablehorses.common.config.Configs;
import tschipp.callablehorses.common.helper.HorseHelper;
import tschipp.callablehorses.common.worlddata.StoredHorsesWorldData;

@EventBusSubscriber(modid = CallableHorses.MODID)
public class EntityEvents
{

	@SubscribeEvent
	public static void onAttachCaps(AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() instanceof Player)
			event.addCapability(new ResourceLocation(CallableHorses.MODID, "horse_owner"), new HorseOwner());

		if (event.getObject() instanceof AbstractHorse)
			event.addCapability(new ResourceLocation(CallableHorses.MODID, "stored_horse"), new StoredHorse());

	}

	// Remove horses with lower num when they are loaded
	// Notify player of offline horse death
	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event)
	{
		Entity joiningEntity = event.getEntity();
		Level world = event.getWorld();
		if (!world.isClientSide)
		{
			if(joiningEntity instanceof Player player)
			{
				IHorseOwner owner = HorseHelper.getOwnerCap(player);

				String ownedHorse = owner.getStorageUUID();

				if (!ownedHorse.isEmpty())
				{
					StoredHorsesWorldData data = HorseHelper.getWorldData((ServerLevel) world);
					if (data.wasKilled(ownedHorse))
					{
						data.clearKilled(ownedHorse);

						if (Configs.SERVER.deathIsPermanent.get())
						{
							owner.clearHorse();
							player.displayClientMessage(new TranslatableComponent("callablehorses.alert.offlinedeath").withStyle(ChatFormatting.RED), false);
						}
						else
						{
							AbstractHorse deadHorse = owner.createHorseEntity(world);
							HorseManager.prepDeadHorseForRespawning(deadHorse);
							owner.setHorseNBT(deadHorse.serializeNBT());
							owner.setLastSeenPosition(Vec3.ZERO);
						}
					}

					if (data.wasOfflineSaved(ownedHorse))
					{
						CompoundTag newNBT = data.getOfflineSavedHorse(ownedHorse);
						owner.setHorseNBT(newNBT);
						data.clearOfflineSavedHorse(ownedHorse);
					}
				}
			}
			else if (joiningEntity instanceof AbstractHorse)
			{
				IStoredHorse horse = HorseHelper.getHorseCap(joiningEntity);
				if (horse.isOwned())
				{
					StoredHorsesWorldData data = HorseHelper.getWorldData((ServerLevel) world);
					if (data.isDisbanded(horse.getStorageUUID()))
					{
						HorseManager.clearHorse(horse);
						data.clearDisbanded(horse.getStorageUUID());

					}
					else
					{
						int globalNum = HorseHelper.getHorseNum((ServerLevel) joiningEntity.level, horse.getStorageUUID());
						if (globalNum > horse.getHorseNum())
						{
//										e.setPosition(e.getPosX(), -200, e.getPosZ());
							event.setCanceled(true);
							CallableHorses.LOGGER.debug(joiningEntity + " was instantly despawned because its number is " + horse.getHorseNum() + " and the global num is " + globalNum);
						}
					}

				}
			}
		}
	}

	// Remove horses with lower num when they are loaded
	@SubscribeEvent
	public static void onChunkLoad(ChunkEvent.Load event)
	{
//		LevelAccessor world = event.getWorld();
//		if (!world.isClientSide())
//		{
//
//			ChunkAccess chk = event.getChunk();
//
//			if (chk instanceof LevelChunk)
//			{
//				ClassInstanceMultiMap<Entity>[] entitylists = ((LevelChunk) chk).getEntitySections();
//
//				for (ClassInstanceMultiMap<Entity> list : entitylists)
//				{
//					for (Entity e : list)
//					{
//						if (e instanceof AbstractHorse)
//						{
//							IStoredHorse horse = HorseHelper.getHorseCap(e);
//							if (horse.isOwned())
//							{
//								StoredHorsesWorldData data = HorseHelper.getWorldData((ServerLevel) world);
//								if (data.isDisbanded(horse.getStorageUUID()))
//								{
//									HorseManager.clearHorse(horse);
//									data.clearDisbanded(horse.getStorageUUID());
//
//								}
//								else
//								{
//									int globalNum = HorseHelper.getHorseNum((ServerLevel) e.level, horse.getStorageUUID());
//									if (globalNum > horse.getHorseNum())
//									{
////										e.setPosition(e.getPosX(), -200, e.getPosZ());
//										e.discard();
//										CallableHorses.LOGGER.debug(e + " was instantly despawned because its number is " + horse.getHorseNum() + " and the global num is " + globalNum);
//									}
//								}
//
//							}
//						}
//					}
//				}
//			}
//		}

	}

	// Clone player cap on teleport/respawn
	@SubscribeEvent
	public static void onClone(Clone event)
	{
		Player original = event.getOriginal();
		Player newPlayer = event.getPlayer();

		IHorseOwner oldHorse = HorseHelper.getOwnerCap(original);
		IHorseOwner newHorse = HorseHelper.getOwnerCap(newPlayer);

		newHorse.setHorseNBT(oldHorse.getHorseNBT());
		newHorse.setHorseNum(oldHorse.getHorseNum());
		newHorse.setStorageUUID(oldHorse.getStorageUUID());

	}

	// Save Horse to player cap when it unloads
	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event)
	{
//		LevelAccessor world = event.getWorld();
//		if (!world.isClientSide())
//		{
//			ChunkAccess chk = event.getChunk();
//
//			if (chk instanceof LevelChunk)
//			{
//				ClassInstanceMultiMap<Entity>[] entitylists = ((LevelChunk) chk).getEntitySections();
//
//				for (ClassInstanceMultiMap<Entity> list : entitylists)
//				{
//					for (Entity e : list)
//					{
//						HorseManager.saveHorse(e);
//					}
//				}
//			}
//		}

	}

	// Save Horse to player cap when it unloads
	@SubscribeEvent
	public static void onEntityLeaveWorld(EntityLeaveWorldEvent event)
	{
		Level world = event.getWorld();
		if (!world.isClientSide())
		{
			HorseManager.saveHorse(event.getEntity());
		}
	}

	// Save Horse to player cap when it unloads
	@SubscribeEvent
	public static void onStopTracking(PlayerEvent.StopTracking event)
	{
		Player player = event.getPlayer();
		Level world = player.level;
		Entity e = event.getTarget();

		if (!world.isClientSide && e.isAlive())
		{
			HorseManager.saveHorse(e);
		}

	}

	// Send horse update to client when is starting to be tracked
	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event)
	{
		Player player = event.getPlayer();
		if (!player.level.isClientSide)
		{
			Entity target = event.getTarget();
			if (target instanceof AbstractHorse)
			{
				HorseHelper.sendHorseUpdateToClient(target, player);
			}
		}
	}

	// Debug
	@SubscribeEvent
	public static void onLivingUpdate(LivingUpdateEvent event)
	{

		Entity e = event.getEntityLiving();
		if (e instanceof AbstractHorse && !e.level.isClientSide)
		{
			if (Configs.SERVER.enableDebug.get() || Configs.SERVER.continuousAntiDupeChecking.get())
			{
				IStoredHorse horse = HorseHelper.getHorseCap(e);
				if (Configs.SERVER.enableDebug.get())
					e.setCustomName(new TextComponent("Is Owned: " + horse.isOwned() + ", Storage UUID: " + horse.getStorageUUID() + ", Horse Number: " + horse.getHorseNum() + ", Horse UUID: " + e.getUUID()));

				if (Configs.SERVER.continuousAntiDupeChecking.get())
				{
					int thisNum = horse.getHorseNum();
					int globalNum = HorseHelper.getHorseNum((ServerLevel) e.level, horse.getStorageUUID());
					if (globalNum > thisNum)
					{
//						e.setPosition(e.getPosX(), -200, e.getPosZ());
						e.discard();
					}
				}

			}
		}
	}

	// Notify player of horse death
	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event)
	{
		Entity e = event.getEntity();

		if (!e.level.isClientSide && e instanceof AbstractHorse)
		{
			IStoredHorse horse = HorseHelper.getHorseCap(e);
			if (horse.isOwned())
			{
				Player owner = HorseHelper.getPlayerFromUUID(horse.getOwnerUUID(), e.level);
				if (owner != null)
				{
					IHorseOwner horseOwner = HorseHelper.getOwnerCap(owner);
					if (Configs.SERVER.deathIsPermanent.get())
					{
						horseOwner.clearHorse();
						owner.displayClientMessage(new TranslatableComponent("callablehorses.alert.death").withStyle(ChatFormatting.RED), false);
					}
					else
					{
						HorseManager.saveHorse(e);
						AbstractHorse deadHorse = horseOwner.createHorseEntity(owner.level);
						HorseManager.prepDeadHorseForRespawning(deadHorse);
						horseOwner.setHorseNBT(deadHorse.serializeNBT());
						horseOwner.setLastSeenPosition(Vec3.ZERO);
					}

				}
				else
				{
					CallableHorses.LOGGER.debug(e + " was marked as killed.");
					e.level.getServer().getAllLevels().forEach(serverworld -> {
						HorseHelper.getWorldData(serverworld).markKilled(horse.getStorageUUID());
					});
				}
			}
		}
	}
}
