package tschipp.callablehorses.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.util.Random;
import tschipp.callablehorses.common.HorseManager;
import tschipp.callablehorses.common.WhistleSounds;

public class PressKeyPacket implements IMessage, IMessageHandler<PressKeyPacket, IMessage>
{
	private static Random rand = new Random();

	
	private int key;

	public PressKeyPacket()
	{
	}

	public PressKeyPacket(int key)
	{
		this.key = key;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.key = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(key);
	}

	@Override
	public IMessage onMessage(PressKeyPacket message, MessageContext ctx)
	{
		IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;

		mainThread.addScheduledTask(new Runnable() {
			EntityPlayerMP player = ctx.getServerHandler().player;

			@Override
			public void run()
			{
				switch(message.key)
				{
				case 0:
					HorseManager.callHorse(player);
					break;
				case 1:
					HorseManager.setHorse(player);
					break;
				case 2:
					HorseManager.showHorseStats(player);
				
				}
			}

		});

		return null;
	}

}
