package tschipp.callablehorses;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tschipp.callablehorses.client.keybinds.KeybindManager;

@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientProxy
{
	public static void setup(FMLClientSetupEvent event)
	{
		KeybindManager.init();
	}

}
