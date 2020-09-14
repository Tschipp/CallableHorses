package tschipp.callablehorses;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.forgespi.language.IModInfo;
import tschipp.callablehorses.common.config.Configs;

@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(value = CallableHorses.MODID)
public class CallableHorses
{
	public static final String MODID = "callablehorses";
	public static final String NAME = "Callable Horses";
	public static final Logger LOGGER = LogManager.getFormatterLogger("CallableHorses");

	public static SimpleChannel network;

	public static IModInfo info;

	public CallableHorses()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(CommonProxy::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientProxy::setup);
		
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Configs.SERVER_SPEC);

		info = ModLoadingContext.get().getActiveContainer().getModInfo();
	}
}
