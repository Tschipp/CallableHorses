package tschipp.callablehorses.network;

import java.util.function.Supplier;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.storedhorse.HorseProvider;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.helper.HorseHelper;

public class HorseCapSyncPacket
{
	private int entityID = 0;
	private CompoundNBT horseNBT = null;

	public HorseCapSyncPacket()
	{
	}

	public HorseCapSyncPacket(int entityID, IStoredHorse horse)
	{
		this.entityID = entityID;
		this.horseNBT = (CompoundNBT) HorseProvider.HORSE_CAPABILITY.getStorage().writeNBT(HorseProvider.HORSE_CAPABILITY, horse, null);
	}

	public HorseCapSyncPacket(PacketBuffer buf)
	{
		this.entityID = buf.readInt();
		this.horseNBT = buf.readCompoundTag();
	}

	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(entityID);
		buf.writeCompoundTag(horseNBT);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {

				World world = CallableHorses.proxy.getWorld();

				Entity e = world.getEntityByID(entityID);
				if (e != null)
				{
					IStoredHorse horse = HorseHelper.getHorseCap(e);
					HorseProvider.HORSE_CAPABILITY.getStorage().readNBT(HorseProvider.HORSE_CAPABILITY, horse, null, horseNBT);
				}

			});
		}
	}

}
