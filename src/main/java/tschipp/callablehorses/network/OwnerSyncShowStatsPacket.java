package tschipp.callablehorses.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tschipp.callablehorses.client.gui.GuiStatViewer;
import tschipp.callablehorses.common.capabilities.horseowner.HorseOwnerProvider;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.helper.HorseHelper;

public class OwnerSyncShowStatsPacket
{
	private CompoundNBT ownerNBT = null;

	public OwnerSyncShowStatsPacket()
	{
	}

	public OwnerSyncShowStatsPacket(IHorseOwner owner)
	{
		this.ownerNBT = (CompoundNBT) HorseOwnerProvider.OWNER_CAPABILITY.getStorage().writeNBT(HorseOwnerProvider.OWNER_CAPABILITY, owner, null);
	}

	public OwnerSyncShowStatsPacket(PacketBuffer buf)
	{
		this.ownerNBT = buf.readCompoundTag();
	}

	public void toBytes(PacketBuffer buf)
	{
		buf.writeCompoundTag(ownerNBT);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection().getReceptionSide().isClient())
		{
			ctx.get().enqueueWork(() -> {

				PlayerEntity player = Minecraft.getInstance().player;

				if (player != null)
				{
					IHorseOwner owner = HorseHelper.getOwnerCap(player);
					HorseOwnerProvider.OWNER_CAPABILITY.getStorage().readNBT(HorseOwnerProvider.OWNER_CAPABILITY, owner, null, ownerNBT);

					Minecraft.getInstance().displayGuiScreen(new GuiStatViewer(player));
				}

			});
		}
	}

}
