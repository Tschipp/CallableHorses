package tschipp.callablehorses.network;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.WhistleSounds;

public class PlayWhistlePacket
{
	public PlayWhistlePacket()
	{
	}

	public PlayWhistlePacket(PacketBuffer buf)
	{
	}

	public void toBytes(PacketBuffer buf)
	{
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {

				PlayerEntity player = CallableHorses.proxy.getPlayer();

				if (player != null)
				{
					Random rand = new Random();
					player.world.playSound(player, player.getPosition(), WhistleSounds.getRandomWhistle(), SoundCategory.PLAYERS, 1f, (float) (1.4 + rand.nextGaussian() / 3));
				}

			});
		}
	}

}
