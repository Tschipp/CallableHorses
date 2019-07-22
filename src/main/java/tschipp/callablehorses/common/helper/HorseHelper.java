package tschipp.callablehorses.common.helper;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.horseowner.HorseOwnerProvider;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.capabilities.storedhorse.HorseProvider;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.worlddata.StoredHorsesWorldData;
import tschipp.callablehorses.network.HorseCapSyncPacket;

public class HorseHelper
{
	public static IHorseOwner getOwnerCap(EntityPlayer player)
	{
		return player.getCapability(HorseOwnerProvider.OWNER_CAPABILITY, null);
	}
	
	public static IStoredHorse getHorseCap(Entity horse)
	{
		return horse.getCapability(HorseProvider.HORSE_CAPABILITY, null);
	}
	
	public static void sendHorseUpdateInRange(Entity horse)
	{
		IStoredHorse storedHorse = getHorseCap(horse);
		CallableHorses.network.sendToAllAround(new HorseCapSyncPacket(horse.getEntityId(), storedHorse), new TargetPoint(horse.world.provider.getDimension(), horse.posX, horse.posZ, horse.posZ, 32));
	}
	
	public static void sendHorseUpdateToClient(Entity horse, EntityPlayer player)
	{
		IStoredHorse storedHorse = getHorseCap(horse);
		CallableHorses.network.sendTo(new HorseCapSyncPacket(horse.getEntityId(), storedHorse), (EntityPlayerMP) player);
	}
	
	@Nullable
	public static EntityPlayer getPlayerFromUUID(String uuid, World world)
	{
		MinecraftServer server = ((WorldServer)world).getMinecraftServer();
		EntityPlayerMP owner = server.getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
		
		return owner;
	}
	
	public static void setHorseNum(World world, String storageid, int num)
	{
		StoredHorsesWorldData storedHorses = StoredHorsesWorldData.getInstance(world);
		storedHorses.addHorseNum(storageid, num);
	}
	
	public static int getHorseNum(World world, String storageid)
	{
		StoredHorsesWorldData storedHorses = StoredHorsesWorldData.getInstance(world);
		return storedHorses.getHorseNum(storageid);
	}
	
	public static void setHorseLastSeen(EntityPlayer player)
	{
		IHorseOwner owner = getOwnerCap(player);
		owner.setLastSeenPosition(player.getPosition());
		owner.setLastSeenDim(player.world.provider.getDimension());
	}
	
	public static StoredHorsesWorldData getWorldData(World world)
	{
		return StoredHorsesWorldData.getInstance(world);
	}
}
