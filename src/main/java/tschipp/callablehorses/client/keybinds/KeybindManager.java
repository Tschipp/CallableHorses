package tschipp.callablehorses.client.keybinds;

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class KeybindManager
{

	public static KeyMapping setHorse;
	public static KeyMapping callHorse;
	public static KeyMapping showStats;

	@OnlyIn(Dist.CLIENT)
	public static void registerKeyBinding(RegisterKeyMappingsEvent event)
	{
		setHorse = new KeyMapping("key.sethorse.desc", GLFW.GLFW_KEY_P, "key.callablehorses.category");
		callHorse = new KeyMapping("key.callhorse.desc",GLFW.GLFW_KEY_V, "key.callablehorses.category");
		showStats = new KeyMapping("key.showstats.desc", GLFW.GLFW_KEY_K, "key.callablehorses.category");

		event.register(setHorse);
		event.register(callHorse);
		event.register(showStats);
	}

}
