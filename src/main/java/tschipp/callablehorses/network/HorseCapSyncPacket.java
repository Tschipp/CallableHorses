package tschipp.callablehorses.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.capabilities.storedhorse.StoredHorse;
import tschipp.callablehorses.common.helper.HorseHelper;

import java.util.function.Supplier;

public class HorseCapSyncPacket
{
	private int entityID = 0;
	private CompoundTag horseNBT = null;

	public HorseCapSyncPacket()
	{
	}

	public HorseCapSyncPacket(int entityID, IStoredHorse horse)
	{
		this.entityID = entityID;
		this.horseNBT = (CompoundTag) StoredHorse.writeNBT(horse);
	}

	public HorseCapSyncPacket(FriendlyByteBuf buf)
	{
		this.entityID = buf.readInt();
		this.horseNBT = buf.readNbt();
	}

	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeInt(entityID);
		buf.writeNbt(horseNBT);
	}

	public void handle(Supplier<NetworkEvent.Context> context)
	{
		NetworkEvent.Context ctx = context.get();
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isClient())
			{
				Level world = CallableHorses.proxy.getWorld();

				Entity e = world.getEntity(entityID);
				if (e != null)
				{
					IStoredHorse horse = HorseHelper.getHorseCap(e);
					StoredHorse.readNBT(horse, horseNBT);
				}
			}
		});
		ctx.setPacketHandled(true);
	}

}
