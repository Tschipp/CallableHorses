package tschipp.callablehorses;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import tschipp.callablehorses.network.HorseCapSyncPacket;
import tschipp.callablehorses.network.OwnerSyncShowStatsPacket;
import tschipp.callablehorses.network.PlayWhistlePacket;
import tschipp.callablehorses.network.PressKeyPacket;

import java.util.Optional;

@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonProxy implements IProxy
{
	public static void setup(FMLCommonSetupEvent event)
	{
		String version = CallableHorses.info.getVersion().toString();

		CallableHorses.network = NetworkRegistry.newSimpleChannel(new ResourceLocation(CallableHorses.MODID, "callablehorseschannel"), () -> version, version::equals, version::equals);

		CallableHorses.network.registerMessage(0, HorseCapSyncPacket.class, HorseCapSyncPacket::toBytes, HorseCapSyncPacket::new, HorseCapSyncPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CallableHorses.network.registerMessage(1, OwnerSyncShowStatsPacket.class, OwnerSyncShowStatsPacket::toBytes, OwnerSyncShowStatsPacket::new, OwnerSyncShowStatsPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CallableHorses.network.registerMessage(2, PressKeyPacket.class, PressKeyPacket::toBytes, PressKeyPacket::new, PressKeyPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		CallableHorses.network.registerMessage(3, PlayWhistlePacket.class, PlayWhistlePacket::toBytes, PlayWhistlePacket::new, PlayWhistlePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}

	@Override
	public Level getWorld()
	{
		return null;
	}

	@Override
	public Player getPlayer()
	{
		return null;
	}

	@Override
	public void displayStatViewer()
	{		
	}

}
