package tschipp.callablehorses.common;

import static tschipp.callablehorses.common.config.CallableHorsesConfig.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.config.CallableHorsesConfig;
import tschipp.callablehorses.common.helper.HorseHelper;
import tschipp.callablehorses.common.worlddata.StoredHorsesWorldData;
import tschipp.callablehorses.network.OwnerSyncShowStatsPacket;

public class HorseManager
{

	public static boolean callHorse(EntityPlayer player)
	{
		if (player != null)
		{
			IHorseOwner horseOwner = HorseHelper.getOwnerCap(player);
			if (horseOwner != null)
			{
				if (horseOwner.getHorseNBT().hasNoTags())
				{
					player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("callablehorses.error.nohorse")), true);
					return false;
				}

				if (!canCallHorse(player))
					return false;
				Random rand = new Random();
				player.world.playSound(null, player.posX, player.posY, player.posZ, WhistleSounds.getRandomWhistle(), SoundCategory.PLAYERS, 1f, (float) (1.4 + rand.nextGaussian() / 3));

				AbstractHorse e = findHorseWithStorageID(horseOwner.getStorageUUID(), player.world);
				if (e != null)
				{
					IStoredHorse horse = HorseHelper.getHorseCap(e);
					if (horse.getStorageUUID().equals(horseOwner.getStorageUUID()))
					{
						if (e.world.provider.getDimension() == player.world.provider.getDimension())
						{
							e.removePassengers();

							if (e.getPosition().distanceSq(player.getPosition()) <= settings.horseWalkRange * settings.horseWalkRange)
							{
								// Horse walks
								e.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(settings.horseWalkRange);
								e.getNavigator().tryMoveToEntityLiving(player, settings.horseSpeed);
							}
							else
							{
								// TP-ing the horse
								e.setPosition(player.posX, player.posY, player.posZ);
							}
							HorseHelper.sendHorseUpdateInRange(e);
							HorseHelper.setHorseLastSeen(player);
							return true;
						}
						else
						{
							// Removing any loaded horses in other dims when a
							// new one is spawned
							HorseManager.saveHorse(e);
							e.setPosition(e.posX, -200, e.posZ);
							e.setDead();
						}

					}
				}

				// Spawning a new horse with a new num
				AbstractHorse newHorse = horseOwner.getHorseEntity(player.world);
				newHorse.setPosition(player.posX, player.posY, player.posZ);
				player.world.spawnEntity(newHorse);
				IStoredHorse h = HorseHelper.getHorseCap(newHorse);
				HorseHelper.setHorseNum(newHorse.world, h.getStorageUUID(), h.getHorseNum());
				HorseHelper.sendHorseUpdateInRange(newHorse);
				HorseHelper.setHorseLastSeen(player);
				return true;

			}

		}

		return false;
	}

	public static void setHorse(EntityPlayer player)
	{
		if (player != null)
		{
			if (!player.isRiding())
			{
				player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("callablehorses.error.notriding")), true);
				return;
			}

			Entity e = player.getRidingEntity();
			if (e instanceof AbstractHorse)
			{
				if (!canSetHorse(player, e))
					return;

				IStoredHorse storedHorse = HorseHelper.getHorseCap(e);

				String owner = storedHorse.getOwnerUUID();
				String playerID = player.getGameProfile().getId().toString();
				boolean owned = storedHorse.isOwned();

				if (owned && !owner.equals(playerID))
				{
					player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("callablehorses.error.alreadyowned")), true);
					return;
				}

				if (owned && owner.equals(playerID))
				{
					player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("callablehorses.error.alreadypersonal")), true);
					return;
				}

				IHorseOwner horseOwner = HorseHelper.getOwnerCap(player);
				String ownedID = horseOwner.getStorageUUID();

				// Marking any old horses as disbanded
				if (!ownedID.isEmpty())
				{
					Entity ent = findHorseWithStorageID(horseOwner.getStorageUUID(), player.world);
					if (ent != null)
					{
						clearHorse(HorseHelper.getHorseCap(ent));
					}
					else
					{
						StoredHorsesWorldData data = HorseHelper.getWorldData(player.world);
						data.disbandHorse(ownedID);
					}

				}
				horseOwner.clearHorse();

				// Setting the new horse
				horseOwner.setHorse((AbstractHorse) e, player);
				HorseHelper.sendHorseUpdateInRange(e);
				HorseHelper.setHorseLastSeen(player);
				HorseHelper.setHorseNum(e.world, storedHorse.getStorageUUID(), storedHorse.getHorseNum());
				player.sendStatusMessage(new TextComponentString(I18n.translateToLocal("callablehorses.success")), true);

			}
		}
	}

	public static void showHorseStats(EntityPlayerMP player)
	{
		IHorseOwner owner = HorseHelper.getOwnerCap(player);

		if (owner.getHorseNBT().hasNoTags())
		{
			player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("callablehorses.error.nohorse")), true);
			return;
		}

		Entity e = findHorseWithStorageID(owner.getStorageUUID(), player.world);
		if (e != null)
		{
			HorseManager.saveHorse(e);
		}

		CallableHorses.network.sendTo(new OwnerSyncShowStatsPacket(owner), player);
	}

	public static void clearHorse(IStoredHorse horse)
	{
		horse.setOwned(false);
		horse.setHorseNum(0);
		horse.setOwnerUUID("");
		horse.setStorageUUID("");
	}

	@Nullable
	public static AbstractHorse findHorseWithStorageID(String id, World world)
	{
		MinecraftServer server = ((WorldServer) world).getMinecraftServer();
		List<Entity> entities = new ArrayList<Entity>();

		for (World w : server.worlds)
			entities.addAll(w.loadedEntityList);

		for (Entity e : entities)
		{
			if (e instanceof AbstractHorse)
			{
				IStoredHorse horse = HorseHelper.getHorseCap(e);
				if (horse.getStorageUUID().equals(id))
					return (AbstractHorse) e;

			}
		}

		return null;
	}

	// Clear armor, saddle, and any chest items
	public static void prepDeadHorseForRespawning(Entity e)
	{
		IItemHandler itemHandler = e.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if (itemHandler != null)
		{
			for (int i = 0; i < itemHandler.getSlots(); i++)
			{
				itemHandler.extractItem(i, 64, false);
			}
		}

		if (e instanceof AbstractChestHorse)
		{
			((AbstractChestHorse) e).setChested(false);
		}

		e.extinguish();
		((EntityLivingBase) e).setHealth(((EntityLivingBase) e).getMaxHealth());

	}

	public static boolean canCallHorse(EntityPlayer player)
	{
		if (isAreaProtected(player, null))
		{
			player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("callablehorses.error.area")), true);
			return false;
		}

		if (player.isRiding())
		{
			player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("callablehorses.error.riding")), true);
			return false;
		}

		if (CallableHorsesConfig.settings.checkForSpace)
		{
			double startX, startY, startZ;
			double endX, endY, endZ;
			
			startX = player.posX - 1;
			startY = player.posY;
			startZ = player.posZ - 1;
			
			endX = player.posX + 1;
			endY = player.posY + 2;
			endZ = player.posZ + 1;
			
			World world = player.world;

			for(double x = startX; x <= endX; x++)
			{
				for(double y = startY; y <= endY; y++)
				{
					for(double z = startZ; z <= endZ; z++)
					{
						BlockPos pos = new BlockPos(x,y,z);
						IBlockState state = world.getBlockState(pos);
						if(state.getBlock().getCollisionBoundingBox(state, world, pos) != null)
						{
							player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("callablehorses.error.nospace")), true);
							return false;
						}
					}
				}
			}
		}

		if (!settings.callableInEveryDimension)
		{
			int[] allowedDims = settings.callableDimsWhitelist;
			int playerDim = player.world.provider.getDimension();

			for (int i = 0; i < allowedDims.length; i++)
			{
				if (i == playerDim)
					return true;
			}
			player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("callablehorses.error.dim")), true);
			return false;
		}

		int maxDistance = settings.maxCallingDistance;
		if (maxDistance != -1)
		{
			IHorseOwner owner = HorseHelper.getOwnerCap(player);
			BlockPos lastSeenPos = owner.getLastSeenPosition();
			int lastSeenDim = owner.getLastSeenDim();

			if (lastSeenPos.equals(BlockPos.ORIGIN))
				return true;

			MinecraftServer server = player.world.getMinecraftServer();

			Entity livingHorse = findHorseWithStorageID(owner.getStorageUUID(), player.world);
			if (livingHorse != null)
			{
				lastSeenPos = livingHorse.getPosition();
				lastSeenDim = livingHorse.world.provider.getDimension();
			}

			double movementFactorHorse = server.getWorld(lastSeenDim).provider.getMovementFactor();
			double movementFactorOwner = player.world.provider.getMovementFactor();

			double movementFactorTotal = movementFactorHorse > movementFactorOwner ? movementFactorHorse / movementFactorOwner : movementFactorOwner / movementFactorHorse;

			double distance = Math.sqrt(lastSeenPos.distanceSq(player.getPosition())) / movementFactorTotal;
			if (distance <= maxDistance)
				return true;

			player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("callablehorses.error.range")), true);
			return false;
		}

		return true;
	}

	public static boolean canSetHorse(EntityPlayer player, Entity entity)
	{
		if (isAreaProtected(player, entity))
		{
			player.sendStatusMessage(new TextComponentString(TextFormatting.RED + I18n.translateToLocal("callablehorses.error.setarea")), true);
			return false;
		}

		return true;
	}

	public static void saveHorse(Entity e)
	{
		if (e instanceof AbstractHorse)
		{
			if (((AbstractHorse) e).hurtTime != 0)
				return;

			World world = e.world;
			IStoredHorse horse = HorseHelper.getHorseCap(e);
			if (horse.isOwned())
			{
				String ownerid = horse.getOwnerUUID();
				EntityPlayer owner = HorseHelper.getPlayerFromUUID(ownerid, world);

				if (owner != null)
				{
					// Owner is online
					IHorseOwner horseOwner = HorseHelper.getOwnerCap(owner);
					NBTTagCompound nbt = e.serializeNBT();
					horseOwner.setHorseNBT(nbt);
					horseOwner.setLastSeenDim(e.dimension);
					horseOwner.setLastSeenPosition(e.getPosition());
				}
				else
				{
					StoredHorsesWorldData data = HorseHelper.getWorldData(world);
					data.addOfflineSavedHorse(horse.getStorageUUID(), e.serializeNBT());
				}
			}
		}
	}

	private static boolean isAreaProtected(EntityPlayer player, @Nullable Entity fakeHorse)
	{
		IHorseOwner owner = HorseHelper.getOwnerCap(player);
		if (fakeHorse == null)
			fakeHorse = owner.getHorseEntity(player.world);
		fakeHorse.setPosition(player.posX, player.posY, player.posZ);
		PlayerInteractEvent.EntityInteract interactEvent = new EntityInteract(player, EnumHand.MAIN_HAND, fakeHorse);
		AttackEntityEvent attackEvent = new AttackEntityEvent(player, fakeHorse);

		MinecraftForge.EVENT_BUS.post(interactEvent);
		MinecraftForge.EVENT_BUS.post(attackEvent);

		return interactEvent.isCanceled() || attackEvent.isCanceled();
	}

}
