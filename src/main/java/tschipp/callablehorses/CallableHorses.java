package tschipp.callablehorses;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tschipp.callablehorses.common.WhistleSounds;
import tschipp.callablehorses.common.capabilities.CapabilityHandler;
import tschipp.callablehorses.common.config.Configs;
import tschipp.callablehorses.common.loot.HorseDropModifier;


@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(value = CallableHorses.MODID)
public class CallableHorses
{
	public static IProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	
	public static final String MODID = "callablehorses";
	public static final String NAME = "Callable Horses";
	public static final Logger LOGGER = LogManager.getFormatterLogger("CallableHorses");

	public static SimpleChannel network;

	public static IModInfo info;

	public CallableHorses()
	{
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

		eventBus.addListener(CommonProxy::setup);

		WhistleSounds.SOUND_EVENTS.register(eventBus);
		HorseDropModifier.GLM.register(eventBus);

		ModLoadingContext.get().registerConfig(Type.COMMON, Configs.SERVER_SPEC);

		info = ModLoadingContext.get().getActiveContainer().getModInfo();

		// Caps
		MinecraftForge.EVENT_BUS.addListener(CapabilityHandler::register);
	}
}
