package tschipp.callablehorses.client.keybinds;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import tschipp.callablehorses.common.config.Configs;

public class KeybindManager
{

	public static KeyBinding setHorse;
	public static KeyBinding callHorse;
	public static KeyBinding showStats;

	@OnlyIn(Dist.CLIENT)
	public static void init()
	{
		setHorse = new KeyBinding("key.sethorse.desc", GLFW.GLFW_KEY_P, "key.callablehorses.category");
		callHorse = new KeyBinding("key.callhorse.desc",GLFW.GLFW_KEY_V, "key.callablehorses.category");
		showStats = new KeyBinding("key.showstats.desc", GLFW.GLFW_KEY_K, "key.callablehorses.category");

		ClientRegistry.registerKeyBinding(setHorse);
		ClientRegistry.registerKeyBinding(callHorse);

		if (Configs.SERVER.enableStatsViewer.get())
			ClientRegistry.registerKeyBinding(showStats);

	}

}
