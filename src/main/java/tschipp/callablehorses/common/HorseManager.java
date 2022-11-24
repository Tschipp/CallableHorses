package tschipp.callablehorses.common;

import static tschipp.callablehorses.common.config.Configs.SERVER;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.helper.HorseHelper;
import tschipp.callablehorses.common.worlddata.StoredHorsesWorldData;
import tschipp.callablehorses.network.OwnerSyncShowStatsPacket;
import tschipp.callablehorses.network.PlayWhistlePacket;

public class HorseManager
{

	public static boolean callHorse(Player player)
	{
		if (player != null)
		{
			IHorseOwner horseOwner = HorseHelper.getOwnerCap(player);
			if (horseOwner != null)
			{
				if (horseOwner.getHorseNBT().isEmpty())
				{
					player.displayClientMessage(new TranslatableComponent("callablehorses.error.nohorse").withStyle(ChatFormatting.RED), true);
					return false;
				}

				if (!canCallHorse(player))
					return false;
				Random rand = new Random();
				player.level.playSound(player, player.blockPosition(), WhistleSounds.WHISTLE.get(), SoundSource.PLAYERS, 1f, (float) (1.4 + rand.nextGaussian() / 3));
				CallableHorses.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new PlayWhistlePacket());

				AbstractHorse e = findHorseWithStorageID(horseOwner.getStorageUUID(), player.level);
				if (e != null)
				{
					IStoredHorse horse = HorseHelper.getHorseCap(e);
					if (horse.getStorageUUID().equals(horseOwner.getStorageUUID()))
					{
						if (e.level.dimensionType() == player.level.dimensionType())
						{
							e.ejectPassengers();

							if (e.position().distanceTo(player.position()) <= SERVER.horseWalkRange.get())
							{
								// Horse walks //Follow range attribute
								e.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(SERVER.horseWalkRange.get());
								e.getNavigation().moveTo(player, SERVER.horseWalkSpeed.get());
							}
							else
							{
								// TP-ing the horse
								e.setPos(player.getX(), player.getY(), player.getZ());
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
							e.setPos(e.getX(), -200, e.getZ());
							e.discard();
						}

					}
				}

				// Spawning a new horse with a new num
				AbstractHorse newHorse = horseOwner.createHorseEntity(player.level);
				newHorse.setPos(player.getX(), player.getY(), player.getZ());
				player.level.addFreshEntity(newHorse);
				IStoredHorse h = HorseHelper.getHorseCap(newHorse);
				HorseHelper.setHorseNum((ServerLevel) newHorse.level, h.getStorageUUID(), h.getHorseNum());
				HorseHelper.sendHorseUpdateInRange(newHorse);
				HorseHelper.setHorseLastSeen(player);
				return true;

			}

		}

		return false;
	}

	public static void setHorse(Player player)
	{
		if (player != null)
		{
			if (player.getVehicle() == null)
			{
				player.displayClientMessage(new TranslatableComponent("callablehorses.error.notriding").withStyle(ChatFormatting.RED), true);
				return;
			}

			Entity e = player.getVehicle();
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
					player.displayClientMessage(new TranslatableComponent("callablehorses.error.alreadyowned").withStyle(ChatFormatting.RED), true);
					return;
				}

				if (owned && owner.equals(playerID))
				{
					player.displayClientMessage(new TranslatableComponent("callablehorses.error.alreadypersonal").withStyle(ChatFormatting.RED), true);
					return;
				}

				IHorseOwner horseOwner = HorseHelper.getOwnerCap(player);
				String ownedID = horseOwner.getStorageUUID();

				// Marking any old horses as disbanded
				if (!ownedID.isEmpty())
				{
					Entity ent = findHorseWithStorageID(horseOwner.getStorageUUID(), player.level);
					if (ent != null)
					{
						clearHorse(HorseHelper.getHorseCap(ent));
					}
					else
					{
						player.level.getServer().getAllLevels().forEach(serverworld -> {
							StoredHorsesWorldData data = HorseHelper.getWorldData(serverworld);
							data.disbandHorse(ownedID);
						});
					}

				}
				horseOwner.clearHorse();

				// Setting the new horse
				horseOwner.setHorse((AbstractHorse) e, player);
				HorseHelper.setHorseLastSeen(player);
				HorseHelper.setHorseNum((ServerLevel) e.level, storedHorse.getStorageUUID(), storedHorse.getHorseNum());
				player.displayClientMessage(new TranslatableComponent("callablehorses.success"), true);
				HorseHelper.sendHorseUpdateInRange(e);

			}
		}
	}

	public static void showHorseStats(ServerPlayer player)
	{
		IHorseOwner owner = HorseHelper.getOwnerCap(player);

		if (owner.getHorseNBT().isEmpty())
		{
			player.displayClientMessage(new TranslatableComponent("callablehorses.error.nohorse").withStyle(ChatFormatting.RED), true);
			return;
		}

		Entity e = findHorseWithStorageID(owner.getStorageUUID(), player.level);
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
	public static AbstractHorse findHorseWithStorageID(String id, Level world)
	{
		MinecraftServer server = world.getServer();
		List<Entity> entities = new ArrayList<Entity>();

		for (ServerLevel w : server.getAllLevels())
			entities.addAll(ImmutableList.copyOf(w.getAllEntities()));

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
		LazyOptional<IItemHandler> cap = e.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		cap.ifPresent(itemHandler -> {
			for (int i = 0; i < itemHandler.getSlots(); i++)
			{
				itemHandler.extractItem(i, 64, false);
			}
		});

		if (e instanceof AbstractChestedHorse)
		{
			((AbstractChestedHorse) e).setChest(false);
		}

		e.clearFire();
		((LivingEntity) e).setHealth(((LivingEntity) e).getMaxHealth());

	}

	@SuppressWarnings("deprecation")
	public static boolean canCallHorse(Player player)
	{
		if (isAreaProtected(player, null))
		{
			player.displayClientMessage(new TranslatableComponent("callablehorses.error.area").withStyle(ChatFormatting.RED), true);
			return false;
		}

		if (player.getVehicle() != null)
		{
			player.displayClientMessage(new TranslatableComponent("callablehorses.error.riding").withStyle(ChatFormatting.RED), true);
			return false;
		}

		if (SERVER.checkForSpace.get())
		{
			double startX, startY, startZ;
			double endX, endY, endZ;

			startX = player.getX() - 1;
			startY = player.getY();
			startZ = player.getZ() - 1;

			endX = player.getX() + 1;
			endY = player.getY() + 2;
			endZ = player.getZ() + 1;

			Level world = player.level;

			for (double x = startX; x <= endX; x++)
			{
				for (double y = startY; y <= endY; y++)
				{
					for (double z = startZ; z <= endZ; z++)
					{
						BlockPos pos = new BlockPos(x, y, z);
						BlockState state = world.getBlockState(pos);
						if (state.getBlock().getCollisionShape(state, world, pos, null) != Shapes.empty())
						{
							player.displayClientMessage(new TranslatableComponent("callablehorses.error.nospace").withStyle(ChatFormatting.RED), true);
							return false;
						}
					}
				}
			}
		}

		if (!SERVER.callableInEveryDimension.get())
		{
			List<? extends String> allowedDims = SERVER.callableDimsWhitelist.get();
			ResourceKey<Level> playerDim = player.level.dimension();

			for (int i = 0; i < allowedDims.size(); i++)
			{
				if (allowedDims.get(i).equals(playerDim.location().toString()))
					return true;
			}
			player.displayClientMessage(new TranslatableComponent("callablehorses.error.dim").withStyle(ChatFormatting.RED), true);
			return false;
		}

		int maxDistance = SERVER.maxCallingDistance.get();
		if (maxDistance != -1)
		{
			IHorseOwner owner = HorseHelper.getOwnerCap(player);
			Vec3 lastSeenPos = owner.getLastSeenPosition();
			ResourceKey<Level> lastSeenDim = owner.getLastSeenDim();

			if (lastSeenPos.equals(Vec3.ZERO))
				return true;

			MinecraftServer server = player.level.getServer();

			Entity livingHorse = findHorseWithStorageID(owner.getStorageUUID(), player.level);
			if (livingHorse != null)
			{
				lastSeenPos = livingHorse.position();
				lastSeenDim = livingHorse.level.dimension(); // Dimension
																	// registry
																	// key
			}

			double movementFactorHorse = server.getLevel(lastSeenDim).dimensionType().coordinateScale(); // getDimensionType,
																										// getMovementFactor
			double movementFactorOwner = player.level.dimensionType().coordinateScale();

			double movementFactorTotal = movementFactorHorse > movementFactorOwner ? movementFactorHorse / movementFactorOwner : movementFactorOwner / movementFactorHorse;

			double distance = lastSeenPos.distanceTo(player.position()) / movementFactorTotal;
			if (distance <= maxDistance)
				return true;

			player.displayClientMessage(new TranslatableComponent("callablehorses.error.range").withStyle(ChatFormatting.RED), true);
			return false;
		}

		return true;
	}

	public static boolean canSetHorse(Player player, Entity entity)
	{
		if (isAreaProtected(player, entity))
		{
			player.displayClientMessage(new TranslatableComponent("callablehorses.error.setarea").withStyle(ChatFormatting.RED), true);
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

			Level world = e.level;
			IStoredHorse horse = HorseHelper.getHorseCap(e);
			if (horse != null && horse.isOwned())
			{
				String ownerid = horse.getOwnerUUID();
				Player owner = HorseHelper.getPlayerFromUUID(ownerid, world);

				if (owner != null)
				{
					// Owner is online
					IHorseOwner horseOwner = HorseHelper.getOwnerCap(owner);
					if (horseOwner != null)
					{
						CompoundTag nbt = e.serializeNBT();
						horseOwner.setHorseNBT(nbt);
						horseOwner.setLastSeenDim(e.level.dimension());
						horseOwner.setLastSeenPosition(e.position());
					}
					else
					{
						world.getServer().getAllLevels().forEach(serverworld -> {
							StoredHorsesWorldData data = HorseHelper.getWorldData(serverworld);
							data.addOfflineSavedHorse(horse.getStorageUUID(), e.serializeNBT());
						});
					}
				}
				else
				{
					StoredHorsesWorldData data = HorseHelper.getWorldData((ServerLevel) world);
					data.addOfflineSavedHorse(horse.getStorageUUID(), e.serializeNBT());
				}
			}
		}
	}

	private static boolean isAreaProtected(Player player, @Nullable Entity fakeHorse)
	{
		IHorseOwner owner = HorseHelper.getOwnerCap(player);
		if (fakeHorse == null)
			fakeHorse = owner.createHorseEntity(player.level);
		fakeHorse.setPos(player.getX(), player.getY(), player.getZ());
		PlayerInteractEvent.EntityInteract interactEvent = new EntityInteract(player, InteractionHand.MAIN_HAND, fakeHorse);
		AttackEntityEvent attackEvent = new AttackEntityEvent(player, fakeHorse);

		MinecraftForge.EVENT_BUS.post(interactEvent);
		MinecraftForge.EVENT_BUS.post(attackEvent);

		return interactEvent.isCanceled() || attackEvent.isCanceled();
	}

}
