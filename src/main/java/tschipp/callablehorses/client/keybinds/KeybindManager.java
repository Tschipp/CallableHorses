package tschipp.callablehorses.client.keybinds;

import net.minecraftforge.client.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tschipp.callablehorses.common.config.Configs;

public class KeybindManager
{

	public static KeyMapping setHorse;
	public static KeyMapping callHorse;
	public static KeyMapping showStats;

	@OnlyIn(Dist.CLIENT)
	public static void init()
	{
		setHorse = new KeyMapping("key.sethorse.desc", GLFW.GLFW_KEY_P, "key.callablehorses.category");
		callHorse = new KeyMapping("key.callhorse.desc",GLFW.GLFW_KEY_V, "key.callablehorses.category");
		showStats = new KeyMapping("key.showstats.desc", GLFW.GLFW_KEY_K, "key.callablehorses.category");

		ClientRegistry.registerKeyBinding(setHorse);
		ClientRegistry.registerKeyBinding(callHorse);

		if (Configs.SERVER.enableStatsViewer.get())
			ClientRegistry.registerKeyBinding(showStats);

	}

}
