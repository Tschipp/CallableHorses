package tschipp.callablehorses.common.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.TargetPoint;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.CapabilityHandler;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.worlddata.StoredHorsesWorldData;
import tschipp.callablehorses.network.HorseCapSyncPacket;

public class HorseHelper
{
	private static Map<Entity, LazyOptional<IStoredHorse>> cachedHorses = new HashMap<Entity, LazyOptional<IStoredHorse>>();

	public static IHorseOwner getOwnerCap(Player player)
	{
		LazyOptional<IHorseOwner> cap = player.getCapability(CapabilityHandler.OWNER_CAPABILITY, null);
		if (cap.isPresent())
			return cap.resolve().get();

		return null;
	}

	public static IStoredHorse getHorseCap(Entity horse)
	{
		LazyOptional<IStoredHorse> cap = cachedHorses.get(horse);
		if (cap == null)
		{
			cap = horse.getCapability(CapabilityHandler.HORSE_CAPABILITY, null);
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
		CallableHorses.network.send(PacketDistributor.NEAR.with(() -> new TargetPoint(horse.getX(), horse.getZ(), horse.getZ(), 32, horse.level.dimension())), new HorseCapSyncPacket(horse.getId(), storedHorse));
	}

	public static void sendHorseUpdateToClient(Entity horse, Player player)
	{
		IStoredHorse storedHorse = getHorseCap(horse);
		CallableHorses.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new HorseCapSyncPacket(horse.getId(), storedHorse));
	}

	@Nullable
	public static Player getPlayerFromUUID(String uuid, Level world)
	{
		MinecraftServer server = world.getServer();
		ServerPlayer owner = server.getPlayerList().getPlayer(UUID.fromString(uuid));

		return owner;
	}

	public static void setHorseNum(ServerLevel world, String storageid, int num)
	{
		world.getServer().getAllLevels().forEach(serverworld -> {
			StoredHorsesWorldData storedHorses = StoredHorsesWorldData.getInstance(serverworld);
			storedHorses.addHorseNum(storageid, num);
		});
	}

	public static int getHorseNum(ServerLevel world, String storageid)
	{
		StoredHorsesWorldData storedHorses = StoredHorsesWorldData.getInstance(world);
		return storedHorses.getHorseNum(storageid);
	}

	public static void setHorseLastSeen(Player player)
	{
		IHorseOwner owner = getOwnerCap(player);
		owner.setLastSeenPosition(player.position());
		owner.setLastSeenDim(player.level.dimension());
	}

	public static StoredHorsesWorldData getWorldData(ServerLevel world)
	{
		return StoredHorsesWorldData.getInstance(world);
	}
}
