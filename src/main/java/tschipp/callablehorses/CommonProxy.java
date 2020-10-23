package tschipp.callablehorses;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import tschipp.callablehorses.common.WhistleSounds;
import tschipp.callablehorses.common.capabilities.horseowner.HorseOwner;
import tschipp.callablehorses.common.capabilities.horseowner.HorseOwnerStorage;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.capabilities.storedhorse.HorseStorage;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.capabilities.storedhorse.StoredHorse;
import tschipp.callablehorses.network.HorseCapSyncPacket;
import tschipp.callablehorses.network.OwnerSyncShowStatsPacket;
import tschipp.callablehorses.network.PlayWhistlePacket;
import tschipp.callablehorses.network.PressKeyPacket;

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

		WhistleSounds.registerSounds();

		// Caps
		CapabilityManager.INSTANCE.register(IHorseOwner.class, new HorseOwnerStorage(), HorseOwner::new);
		CapabilityManager.INSTANCE.register(IStoredHorse.class, new HorseStorage(), StoredHorse::new);
	}

	@Override
	public World getWorld()
	{
		return null;
	}

	@Override
	public PlayerEntity getPlayer()
	{
		return null;
	}

	@Override
	public void displayStatViewer()
	{		
	}

}
