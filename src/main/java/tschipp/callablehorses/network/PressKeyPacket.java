package tschipp.callablehorses.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import tschipp.callablehorses.common.HorseManager;

import java.util.function.Supplier;

public class PressKeyPacket
{
	private int key;

	public PressKeyPacket()
	{
	}

	public PressKeyPacket(int key)
	{
		this.key = key;
	}

	public PressKeyPacket(FriendlyByteBuf buf)
	{
		this.key = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeInt(key);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		Context context = ctx.get();
		if (context.getDirection().getReceptionSide().isServer())
		{
			context.setPacketHandled(true);
			context.enqueueWork(() -> {

				ServerPlayer player = context.getSender();

				if (player != null)
				{
					switch (key)
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
		}
	}

}
