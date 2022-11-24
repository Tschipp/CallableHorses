package tschipp.callablehorses.network;

import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tschipp.callablehorses.common.HorseManager;

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
		if (ctx.get().getDirection().getReceptionSide().isServer())
		{
			ctx.get().enqueueWork(() -> {

				ServerPlayer player = ctx.get().getSender();

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
