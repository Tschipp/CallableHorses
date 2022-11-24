package tschipp.callablehorses.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.horseowner.HorseOwner;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.helper.HorseHelper;

import java.util.function.Supplier;

public class OwnerSyncShowStatsPacket
{
	private CompoundTag ownerNBT = null;

	public OwnerSyncShowStatsPacket()
	{
	}

	public OwnerSyncShowStatsPacket(IHorseOwner owner)
	{
		this.ownerNBT = (CompoundTag) HorseOwner.writeNBT(owner);
	}

	public OwnerSyncShowStatsPacket(FriendlyByteBuf buf)
	{
		this.ownerNBT = buf.readNbt();
	}

	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeNbt(ownerNBT);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {

				Player player = CallableHorses.proxy.getPlayer();

				if (player != null)
				{
					IHorseOwner owner = HorseHelper.getOwnerCap(player);
					HorseOwner.readNBT(owner, ownerNBT);

					CallableHorses.proxy.displayStatViewer();
				}

			});
		}
	}

}
