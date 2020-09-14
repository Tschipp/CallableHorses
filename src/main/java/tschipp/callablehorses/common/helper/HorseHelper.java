package tschipp.callablehorses.common.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.horseowner.HorseOwnerProvider;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.capabilities.storedhorse.HorseProvider;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.worlddata.StoredHorsesWorldData;
import tschipp.callablehorses.network.HorseCapSyncPacket;

public class HorseHelper
{
	private static Map<Entity, LazyOptional<IStoredHorse>> cachedHorses = new HashMap<Entity, LazyOptional<IStoredHorse>>();

	public static IHorseOwner getOwnerCap(PlayerEntity player)
	{
		LazyOptional<IHorseOwner> cap = player.getCapability(HorseOwnerProvider.OWNER_CAPABILITY, null);
		if (cap.isPresent())
			return cap.resolve().get();

		return null;
	}

	public static IStoredHorse getHorseCap(Entity horse)
	{
		LazyOptional<IStoredHorse> cap = cachedHorses.get(horse);
		if (cap == null)
		{
			cap = horse.getCapability(HorseProvider.HORSE_CAPABILITY, null);
			cachedHorses.put(horse, cap);
			cap.addListener(optional -> {
				cachedHorses.remove(horse);
			});
		}
		if (cap.isPresent())
			return cap.resolve().get();

		return null;
	}

	public static void sendHorseUpdateInRange(Entity horse)
	{
		IStoredHorse storedHorse = getHorseCap(horse);
		CallableHorses.network.send(PacketDistributor.NEAR.with(() -> new TargetPoint(horse.getPosX(), horse.getPosZ(), horse.getPosZ(), 32, horse.world.func_234923_W_())), new HorseCapSyncPacket(horse.getEntityId(), storedHorse));
	}

	public static void sendHorseUpdateToClient(Entity horse, PlayerEntity player)
	{
		IStoredHorse storedHorse = getHorseCap(horse);
		CallableHorses.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new HorseCapSyncPacket(horse.getEntityId(), storedHorse));
	}

	@Nullable
	public static PlayerEntity getPlayerFromUUID(String uuid, World world)
	{
		MinecraftServer server = world.getServer();
		ServerPlayerEntity owner = server.getPlayerList().getPlayerByUUID(UUID.fromString(uuid));

		return owner;
	}

	public static void setHorseNum(ServerWorld world, String storageid, int num)
	{
		world.getServer().getWorlds().forEach(serverworld -> {
			StoredHorsesWorldData storedHorses = StoredHorsesWorldData.getInstance(serverworld);
			storedHorses.addHorseNum(storageid, num);
		});
	}

	public static int getHorseNum(ServerWorld world, String storageid)
	{
		StoredHorsesWorldData storedHorses = StoredHorsesWorldData.getInstance(world);
		return storedHorses.getHorseNum(storageid);
	}

	public static void setHorseLastSeen(PlayerEntity player)
	{
		IHorseOwner owner = getOwnerCap(player);
		owner.setLastSeenPosition(player.getPositionVec());
		owner.setLastSeenDim(player.world.func_234923_W_());
	}

	public static StoredHorsesWorldData getWorldData(ServerWorld world)
	{
		return StoredHorsesWorldData.getInstance(world);
	}
}
