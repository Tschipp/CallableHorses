package tschipp.callablehorses.common.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.HorseManager;
import tschipp.callablehorses.common.capabilities.horseowner.HorseOwnerProvider;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.capabilities.storedhorse.HorseProvider;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.config.Configs;
import tschipp.callablehorses.common.helper.HorseHelper;
import tschipp.callablehorses.common.worlddata.StoredHorsesWorldData;

@EventBusSubscriber(modid = CallableHorses.MODID)
public class EntityEvents
{

	@SubscribeEvent
	public static void onAttachCaps(AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() instanceof PlayerEntity)
			event.addCapability(new ResourceLocation(CallableHorses.MODID, "horse_owner"), new HorseOwnerProvider());

		if (event.getObject() instanceof AbstractHorseEntity)
			event.addCapability(new ResourceLocation(CallableHorses.MODID, "stored_horse"), new HorseProvider());

	}

	// Remove horses with lower num when they are loaded
	@SubscribeEvent
	public static void onChunkLoad(ChunkEvent.Load event)
	{
		IWorld world = event.getWorld();
		if (!world.isRemote())
		{

			IChunk chk = event.getChunk();

			if (chk instanceof Chunk)
			{
				ClassInheritanceMultiMap<Entity>[] entitylists = ((Chunk) chk).getEntityLists();

				for (ClassInheritanceMultiMap<Entity> list : entitylists)
				{
					for (Entity e : list)
					{
						if (e instanceof AbstractHorseEntity)
						{
							IStoredHorse horse = HorseHelper.getHorseCap(e);
							if (horse.isOwned())
							{
								StoredHorsesWorldData data = HorseHelper.getWorldData((ServerWorld) world);
								if (data.isDisbanded(horse.getStorageUUID()))
								{
									HorseManager.clearHorse(horse);
									data.clearDisbanded(horse.getStorageUUID());

								}
								else
								{
									int globalNum = HorseHelper.getHorseNum((ServerWorld) e.world, horse.getStorageUUID());
									if (globalNum > horse.getHorseNum())
									{
//										e.setPosition(e.getPosX(), -200, e.getPosZ());
										e.remove();
										CallableHorses.LOGGER.debug(e + " was instantly despawned because its number is " + horse.getHorseNum() + " and the global num is " + globalNum);
									}
								}

							}
						}
					}
				}
			}
		}

	}

	// Clone player cap on teleport/respawn
	@SubscribeEvent
	public static void onClone(Clone event)
	{
		PlayerEntity original = event.getOriginal();
		PlayerEntity newPlayer = event.getPlayer();

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
		IWorld world = event.getWorld();
		if (!world.isRemote())
		{
			IChunk chk = event.getChunk();

			if (chk instanceof Chunk)
			{
				ClassInheritanceMultiMap<Entity>[] entitylists = ((Chunk) chk).getEntityLists();

				for (ClassInheritanceMultiMap<Entity> list : entitylists)
				{
					for (Entity e : list)
					{
						HorseManager.saveHorse(e);
					}
				}
			}
		}

	}

	// Save Horse to player cap when it unloads
	@SubscribeEvent
	public static void onStopTracking(PlayerEvent.StopTracking event)
	{
		PlayerEntity player = event.getPlayer();
		World world = player.world;
		Entity e = event.getTarget();

		if (!world.isRemote && e.isAlive())
		{
			HorseManager.saveHorse(e);
		}

	}

	// Send horse update to client when is starting to be tracked
	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event)
	{
		PlayerEntity player = event.getPlayer();
		if (!player.world.isRemote)
		{
			Entity target = event.getTarget();
			if (target instanceof AbstractHorseEntity)
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
		if (e instanceof AbstractHorseEntity && !e.world.isRemote)
		{
			if (Configs.SERVER.enableDebug.get() || Configs.SERVER.continuousAntiDupeChecking.get())
			{
				IStoredHorse horse = HorseHelper.getHorseCap(e);
				if (Configs.SERVER.enableDebug.get())
					e.setCustomName(new StringTextComponent("Is Owned: " + horse.isOwned() + ", Storage UUID: " + horse.getStorageUUID() + ", Horse Number: " + horse.getHorseNum() + ", Horse UUID: " + e.getUniqueID()));

				if (Configs.SERVER.continuousAntiDupeChecking.get())
				{
					int thisNum = horse.getHorseNum();
					int globalNum = HorseHelper.getHorseNum((ServerWorld) e.world, horse.getStorageUUID());
					if (globalNum > thisNum)
					{
//						e.setPosition(e.getPosX(), -200, e.getPosZ());
						e.remove();
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

		if (!e.world.isRemote && e instanceof AbstractHorseEntity)
		{
			IStoredHorse horse = HorseHelper.getHorseCap(e);
			if (horse.isOwned())
			{
				PlayerEntity owner = HorseHelper.getPlayerFromUUID(horse.getOwnerUUID(), e.world);
				if (owner != null)
				{
					IHorseOwner horseOwner = HorseHelper.getOwnerCap(owner);
					if (Configs.SERVER.deathIsPermanent.get())
					{
						horseOwner.clearHorse();
						owner.sendStatusMessage(new TranslationTextComponent("callablehorses.alert.death").mergeStyle(TextFormatting.RED), false);
					}
					else
					{
						HorseManager.saveHorse(e);
						AbstractHorseEntity deadHorse = horseOwner.createHorseEntity(owner.world);
						HorseManager.prepDeadHorseForRespawning(deadHorse);
						horseOwner.setHorseNBT(deadHorse.serializeNBT());
						horseOwner.setLastSeenPosition(Vector3d.ZERO);
					}

				}
				else
				{
					CallableHorses.LOGGER.debug(e + " was marked as killed.");
					e.world.getServer().getWorlds().forEach(serverworld -> {
						HorseHelper.getWorldData(serverworld).markKilled(horse.getStorageUUID());
					});
				}
			}
		}
	}

	// Notify player of offline horse death
	@SubscribeEvent
	public static void onJoinWorld(EntityJoinWorldEvent event)
	{
		Entity joiningEntity = event.getEntity();
		World world = event.getWorld();
		if (!world.isRemote && joiningEntity instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) joiningEntity;
			IHorseOwner owner = HorseHelper.getOwnerCap(player);

			String ownedHorse = owner.getStorageUUID();

			if (!ownedHorse.isEmpty())
			{
				StoredHorsesWorldData data = HorseHelper.getWorldData((ServerWorld) world);
				if (data.wasKilled(ownedHorse))
				{
					data.clearKilled(ownedHorse);

					if (Configs.SERVER.deathIsPermanent.get())
					{
						owner.clearHorse();
						player.sendStatusMessage(new TranslationTextComponent("callablehorses.alert.offlinedeath").mergeStyle(TextFormatting.RED), false);
					}
					else
					{
						AbstractHorseEntity deadHorse = owner.createHorseEntity(world);
						HorseManager.prepDeadHorseForRespawning(deadHorse);
						owner.setHorseNBT(deadHorse.serializeNBT());
						owner.setLastSeenPosition(Vector3d.ZERO);
					}
				}

				if (data.wasOfflineSaved(ownedHorse))
				{
					CompoundNBT newNBT = data.getOfflineSavedHorse(ownedHorse);
					owner.setHorseNBT(newNBT);
					data.clearOfflineSavedHorse(ownedHorse);
				}
			}
		}
	}
}
