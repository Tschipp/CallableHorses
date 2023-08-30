package tschipp.callablehorses.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.WhistleSounds;

import java.util.Random;
import java.util.function.Supplier;

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

	public void handle(Supplier<NetworkEvent.Context> context)
	{
		NetworkEvent.Context ctx = context.get();
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isClient())
			{
				Player player = CallableHorses.proxy.getPlayer();

				if (player != null)
				{
					Random rand = new Random();
					player.level().playSound(player, player.blockPosition(), WhistleSounds.WHISTLE.get(), SoundSource.PLAYERS, 1f, (float) (1.4 + rand.nextGaussian() / 3));
				}
			}
		});
		ctx.setPacketHandled(true);
	}

}
