package tschipp.callablehorses.network;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
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

	public PressKeyPacket(PacketBuffer buf)
	{
		this.key = buf.readInt();
	}

	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(key);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isServer())
		{
			ctx.get().enqueueWork(() -> {

				ServerPlayerEntity player = ctx.get().getSender();

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
