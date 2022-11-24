package tschipp.callablehorses.network;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.WhistleSounds;

public class PlayWhistlePacket
{
	public PlayWhistlePacket()
	{
	}

	public PlayWhistlePacket(FriendlyByteBuf buf)
	{
	}

	public void toBytes(FriendlyByteBuf buf)
	{
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {

				Player player = CallableHorses.proxy.getPlayer();

				if (player != null)
				{
					Random rand = new Random();
					player.level.playSound(player, player.blockPosition(), WhistleSounds.WHISTLE.get(), SoundSource.PLAYERS, 1f, (float) (1.4 + rand.nextGaussian() / 3));
				}

			});
		}
	}

}
