package tschipp.callablehorses.common.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;

public class CapabilityHandler {
	public static final Capability<IHorseOwner> OWNER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
	});

	public static final Capability<IStoredHorse> HORSE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
	});

	public static void register(RegisterCapabilitiesEvent event) {
		event.register(IHorseOwner.class);
		event.register(IStoredHorse.class);
	}
}
