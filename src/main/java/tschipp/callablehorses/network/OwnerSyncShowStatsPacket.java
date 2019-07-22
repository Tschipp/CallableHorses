package tschipp.callablehorses.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.horseowner.HorseOwnerProvider;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.capabilities.storedhorse.HorseProvider;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.helper.HorseHelper;

public class OwnerSyncShowStatsPacket implements IMessage, IMessageHandler<OwnerSyncShowStatsPacket, IMessage>
{
	private NBTTagCompound ownerNBT = null;
	
	public OwnerSyncShowStatsPacket()
	{
	}
	
	public OwnerSyncShowStatsPacket(IHorseOwner owner)
	{
		this.ownerNBT = (NBTTagCompound) HorseOwnerProvider.OWNER_CAPABILITY.getStorage().writeNBT(HorseOwnerProvider.OWNER_CAPABILITY, owner, null);
	}
	

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.ownerNBT = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, ownerNBT);
	}
	
	@Override
	public IMessage onMessage(OwnerSyncShowStatsPacket message, MessageContext ctx)
	{
		IThreadListener mainThread = Minecraft.getMinecraft();

		mainThread.addScheduledTask(new Runnable()
		{
			World world = Minecraft.getMinecraft().world;
			EntityPlayer player = Minecraft.getMinecraft().player;
			
			@Override
			public void run()
			{
				if(player != null)
				{
					IHorseOwner owner = HorseHelper.getOwnerCap(player);
					HorseOwnerProvider.OWNER_CAPABILITY.getStorage().readNBT(HorseOwnerProvider.OWNER_CAPABILITY, owner, null, message.ownerNBT);
				
					player.openGui(CallableHorses.instance, 0, player.world, 0, 0, 0);

				}
			}

		});
		
		return null;
	}

}
