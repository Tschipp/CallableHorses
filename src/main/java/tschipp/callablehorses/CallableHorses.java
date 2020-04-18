package tschipp.callablehorses;

import java.io.File;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber
@Mod(modid = CallableHorses.MODID, name = CallableHorses.NAME, version = CallableHorses.VERSION, guiFactory = "tschipp.callablehorses.client.gui.GuiFactoryCallableHorses", dependencies = CallableHorses.DEPENDENCIES, acceptedMinecraftVersions = CallableHorses.ACCEPTED_VERSIONS)
public class CallableHorses
{

	@SidedProxy(clientSide = "tschipp.callablehorses.ClientProxy", serverSide = "tschipp.callablehorses.CommonProxy")
	public static CommonProxy proxy;

	// Instance
	@Instance(CallableHorses.MODID)
	public static CallableHorses instance;

	public static final String MODID = "callablehorses";
	public static final String VERSION = "1.1";
	public static final String NAME = "Callable Horses";
	public static final String ACCEPTED_VERSIONS = "[1.12.2,1.13)";
	public static final Logger LOGGER = LogManager.getFormatterLogger("CallableHorses");
	public static final String DEPENDENCIES = "required-after:forge@[13.20.1.2386,);";
	public static File CONFIGURATION_FILE;

	public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		CallableHorses.proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		CallableHorses.proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		CallableHorses.proxy.postInit(event);
	}

}
