package tschipp.callablehorses.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.callablehorses.common.capabilities.storedhorse.HorseProvider;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.helper.HorseHelper;

public class HorseCapSyncPacket implements IMessage, IMessageHandler<HorseCapSyncPacket, IMessage>
{
	private int entityID = 0;
	private NBTTagCompound horseNBT = null;
	
	public HorseCapSyncPacket()
	{
	}
	
	public HorseCapSyncPacket(int entityID, IStoredHorse horse)
	{
		this.entityID = entityID;
		this.horseNBT = (NBTTagCompound) HorseProvider.HORSE_CAPABILITY.getStorage().writeNBT(HorseProvider.HORSE_CAPABILITY, horse, null);
	}
	
	

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.entityID = buf.readInt();
		this.horseNBT = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(entityID);	
		ByteBufUtils.writeTag(buf, horseNBT);
	}
	
	@Override
	public IMessage onMessage(HorseCapSyncPacket message, MessageContext ctx)
	{
		IThreadListener mainThread = Minecraft.getMinecraft();

		mainThread.addScheduledTask(new Runnable()
		{
			World world = Minecraft.getMinecraft().world;

			@Override
			public void run()
			{
				Entity e = world.getEntityByID(message.entityID);
				if(e != null)
				{
					IStoredHorse horse = HorseHelper.getHorseCap(e);
					HorseProvider.HORSE_CAPABILITY.getStorage().readNBT(HorseProvider.HORSE_CAPABILITY, horse, null, message.horseNBT);
				}
			}

		});
		
		return null;
	}

}
