package tschipp.callablehorses.common;

import static tschipp.callablehorses.common.config.Configs.SERVER;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.helper.HorseHelper;
import tschipp.callablehorses.common.worlddata.StoredHorsesWorldData;
import tschipp.callablehorses.network.OwnerSyncShowStatsPacket;
import tschipp.callablehorses.network.PlayWhistlePacket;

public class HorseManager
{

	public static boolean callHorse(PlayerEntity player)
	{
		if (player != null)
		{
			IHorseOwner horseOwner = HorseHelper.getOwnerCap(player);
			if (horseOwner != null)
			{
				if (horseOwner.getHorseNBT().isEmpty())
				{
					player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + I18n.format("callablehorses.error.nohorse")), true);
					return false;
				}

				if (!canCallHorse(player))
					return false;
				Random rand = new Random();
				player.world.playSound(player, player.getPosition(), WhistleSounds.getRandomWhistle(), SoundCategory.PLAYERS, 1f, (float) (1.4 + rand.nextGaussian() / 3));
				CallableHorses.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new PlayWhistlePacket());

				AbstractHorseEntity e = findHorseWithStorageID(horseOwner.getStorageUUID(), player.world);
				if (e != null)
				{
					IStoredHorse horse = HorseHelper.getHorseCap(e);
					if (horse.getStorageUUID().equals(horseOwner.getStorageUUID()))
					{
						if (e.world.func_230315_m_() == player.world.func_230315_m_())
						{
							e.removePassengers();

							if (e.getPositionVec().distanceTo(player.getPositionVec()) <= SERVER.horseWalkRange.get())
							{
								// Horse walks //Follow range attribute
								e.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(SERVER.horseWalkRange.get());
								e.getNavigator().tryMoveToEntityLiving(player, SERVER.horseWalkSpeed.get());
							}
							else
							{
								// TP-ing the horse
								e.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
							}
							HorseHelper.setHorseLastSeen(player);
							HorseHelper.sendHorseUpdateInRange(e);
							return true;
						}
						else
						{
							// Removing any loaded horses in other dims when a
							// new one is spawned
							HorseManager.saveHorse(e);
							e.setPosition(e.getPosX(), -200, e.getPosZ());
							e.remove();
						}

					}
				}

				// Spawning a new horse with a new num
				AbstractHorseEntity newHorse = horseOwner.createHorseEntity(player.world);
				newHorse.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
				player.world.addEntity(newHorse);
				IStoredHorse h = HorseHelper.getHorseCap(newHorse);
				HorseHelper.setHorseNum((ServerWorld) newHorse.world, h.getStorageUUID(), h.getHorseNum());
				HorseHelper.sendHorseUpdateInRange(newHorse);
				HorseHelper.setHorseLastSeen(player);
				return true;

			}

		}

		return false;
	}

	public static void setHorse(PlayerEntity player)
	{
		if (player != null)
		{
			if (player.getRidingEntity() == null)
			{
				player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + I18n.format("callablehorses.error.notriding")), true);
				return;
			}

			Entity e = player.getRidingEntity();
			if (e instanceof AbstractHorseEntity)
			{
				if (!canSetHorse(player, e))
					return;

				IStoredHorse storedHorse = HorseHelper.getHorseCap(e);

				String owner = storedHorse.getOwnerUUID();
				String playerID = player.getGameProfile().getId().toString();
				boolean owned = storedHorse.isOwned();

				if (owned && !owner.equals(playerID))
				{
					player.sendStatusMessage(new TranslationTextComponent("callablehorses.error.alreadyowned").mergeStyle(TextFormatting.RED), true);
					return;
				}

				if (owned && owner.equals(playerID))
				{
					player.sendStatusMessage(new TranslationTextComponent("callablehorses.error.alreadypersonal").mergeStyle(TextFormatting.RED), true);
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
						player.world.getServer().getWorlds().forEach(serverworld -> {
							StoredHorsesWorldData data = HorseHelper.getWorldData(serverworld);
							data.disbandHorse(ownedID);
						});
					}

				}
				horseOwner.clearHorse();

				// Setting the new horse
				horseOwner.setHorse((AbstractHorseEntity) e, player);
				HorseHelper.setHorseLastSeen(player);
				HorseHelper.setHorseNum((ServerWorld) e.world, storedHorse.getStorageUUID(), storedHorse.getHorseNum());
				player.sendStatusMessage(new TranslationTextComponent("callablehorses.success"), true);
				HorseHelper.sendHorseUpdateInRange(e);

			}
		}
	}

	public static void showHorseStats(ServerPlayerEntity player)
	{
		IHorseOwner owner = HorseHelper.getOwnerCap(player);

		if (owner.getHorseNBT().isEmpty())
		{
			player.sendStatusMessage(new TranslationTextComponent("callablehorses.error.nohorse").mergeStyle(TextFormatting.RED), true);
			return;
		}

		Entity e = findHorseWithStorageID(owner.getStorageUUID(), player.world);
		if (e != null)
		{
			HorseManager.saveHorse(e);
		}

		CallableHorses.network.send(PacketDistributor.PLAYER.with(() -> player), new OwnerSyncShowStatsPacket(owner));
	}

	public static void clearHorse(IStoredHorse horse)
	{
		horse.setOwned(false);
		horse.setHorseNum(0);
		horse.setOwnerUUID("");
		horse.setStorageUUID("");
	}

	@Nullable
	public static AbstractHorseEntity findHorseWithStorageID(String id, World world)
	{
		MinecraftServer server = world.getServer();
		List<Entity> entities = new ArrayList<Entity>();

		for (ServerWorld w : server.getWorlds())
			entities.addAll(w.getEntities().collect(Collectors.toList()));

		for (Entity e : entities)
		{
			if (e instanceof AbstractHorseEntity)
			{
				IStoredHorse horse = HorseHelper.getHorseCap(e);
				if (horse.getStorageUUID().equals(id))
					return (AbstractHorseEntity) e;

			}
		}

		return null;
	}

	// Clear armor, saddle, and any chest items
	public static void prepDeadHorseForRespawning(Entity e)
	{
		LazyOptional<IItemHandler> cap = e.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		cap.ifPresent(itemHandler -> {
			for (int i = 0; i < itemHandler.getSlots(); i++)
			{
				itemHandler.extractItem(i, 64, false);
			}
		});

		if (e instanceof AbstractChestedHorseEntity)
		{
			((AbstractChestedHorseEntity) e).setChested(false);
		}

		e.extinguish();
		((LivingEntity) e).setHealth(((LivingEntity) e).getMaxHealth());

	}

	@SuppressWarnings("deprecation")
	public static boolean canCallHorse(PlayerEntity player)
	{
		if (isAreaProtected(player, null))
		{
			player.sendStatusMessage(new TranslationTextComponent("callablehorses.error.area").mergeStyle(TextFormatting.RED), true);
			return false;
		}

		if (player.getRidingEntity() != null)
		{
			player.sendStatusMessage(new TranslationTextComponent("callablehorses.error.riding").mergeStyle(TextFormatting.RED), true);
			return false;
		}

		if (SERVER.checkForSpace.get())
		{
			double startX, startY, startZ;
			double endX, endY, endZ;

			startX = player.getPosX() - 1;
			startY = player.getPosY();
			startZ = player.getPosZ() - 1;

			endX = player.getPosX() + 1;
			endY = player.getPosY() + 2;
			endZ = player.getPosZ() + 1;

			World world = player.world;

			for (double x = startX; x <= endX; x++)
			{
				for (double y = startY; y <= endY; y++)
				{
					for (double z = startZ; z <= endZ; z++)
					{
						BlockPos pos = new BlockPos(x, y, z);
						BlockState state = world.getBlockState(pos);
						if (state.getBlock().getCollisionShape(state, world, pos, null) != VoxelShapes.empty())
						{
							player.sendStatusMessage(new TranslationTextComponent("callablehorses.error.nospace").mergeStyle(TextFormatting.RED), true);
							return false;
						}
					}
				}
			}
		}

		if (!SERVER.callableInEveryDimension.get())
		{
			List<? extends String> allowedDims = SERVER.callableDimsWhitelist.get();
			RegistryKey<World> playerDim = player.world.func_234923_W_();

			for (int i = 0; i < allowedDims.size(); i++)
			{
				if (allowedDims.get(i).equals(playerDim.func_240901_a_().toString()))
					return true;
			}
			player.sendStatusMessage(new TranslationTextComponent("callablehorses.error.dim").mergeStyle(TextFormatting.RED), true);
			return false;
		}

		int maxDistance = SERVER.maxCallingDistance.get();
		if (maxDistance != -1)
		{
			IHorseOwner owner = HorseHelper.getOwnerCap(player);
			Vector3d lastSeenPos = owner.getLastSeenPosition();
			RegistryKey<World> lastSeenDim = owner.getLastSeenDim();

			if (lastSeenPos.equals(Vector3d.ZERO))
				return true;

			MinecraftServer server = player.world.getServer();

			Entity livingHorse = findHorseWithStorageID(owner.getStorageUUID(), player.world);
			if (livingHorse != null)
			{
				lastSeenPos = livingHorse.getPositionVec();
				lastSeenDim = livingHorse.world.func_234923_W_(); // Dimension
																	// registry
																	// key
			}

			double movementFactorHorse = server.getWorld(lastSeenDim).func_230315_m_().func_242724_f(); // getDimensionType,
																										// getMovementFactor
			double movementFactorOwner = player.world.func_230315_m_().func_242724_f();

			double movementFactorTotal = movementFactorHorse > movementFactorOwner ? movementFactorHorse / movementFactorOwner : movementFactorOwner / movementFactorHorse;

			double distance = lastSeenPos.distanceTo(player.getPositionVec()) / movementFactorTotal;
			if (distance <= maxDistance)
				return true;

			player.sendStatusMessage(new TranslationTextComponent("callablehorses.error.range").mergeStyle(TextFormatting.RED), true);
			return false;
		}

		return true;
	}

	public static boolean canSetHorse(PlayerEntity player, Entity entity)
	{
		if (isAreaProtected(player, entity))
		{
			player.sendStatusMessage(new TranslationTextComponent("callablehorses.error.setarea").mergeStyle(TextFormatting.RED), true);
			return false;
		}

		return true;
	}

	public static void saveHorse(Entity e)
	{
		if (e instanceof AbstractHorseEntity)
		{
			if (((AbstractHorseEntity) e).hurtTime != 0)
				return;

			World world = e.world;
			IStoredHorse horse = HorseHelper.getHorseCap(e);
			if (horse != null && horse.isOwned())
			{
				String ownerid = horse.getOwnerUUID();
				PlayerEntity owner = HorseHelper.getPlayerFromUUID(ownerid, world);

				if (owner != null)
				{
					// Owner is online
					IHorseOwner horseOwner = HorseHelper.getOwnerCap(owner);
					if (horseOwner != null)
					{
						CompoundNBT nbt = e.serializeNBT();
						horseOwner.setHorseNBT(nbt);
						horseOwner.setLastSeenDim(e.world.func_234923_W_());
						horseOwner.setLastSeenPosition(e.getPositionVec());
					}
					else
					{
						world.getServer().getWorlds().forEach(serverworld -> {
							StoredHorsesWorldData data = HorseHelper.getWorldData(serverworld);
							data.addOfflineSavedHorse(horse.getStorageUUID(), e.serializeNBT());
						});
					}
				}
				else
				{
					StoredHorsesWorldData data = HorseHelper.getWorldData((ServerWorld) world);
					data.addOfflineSavedHorse(horse.getStorageUUID(), e.serializeNBT());
				}
			}
		}
	}

	private static boolean isAreaProtected(PlayerEntity player, @Nullable Entity fakeHorse)
	{
		IHorseOwner owner = HorseHelper.getOwnerCap(player);
		if (fakeHorse == null)
			fakeHorse = owner.createHorseEntity(player.world);
		fakeHorse.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
		PlayerInteractEvent.EntityInteract interactEvent = new EntityInteract(player, Hand.MAIN_HAND, fakeHorse);
		AttackEntityEvent attackEvent = new AttackEntityEvent(player, fakeHorse);

		MinecraftForge.EVENT_BUS.post(interactEvent);
		MinecraftForge.EVENT_BUS.post(attackEvent);

		return interactEvent.isCanceled() || attackEvent.isCanceled();
	}

}
