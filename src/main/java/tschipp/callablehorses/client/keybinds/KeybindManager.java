package tschipp.callablehorses.client.keybinds;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import tschipp.callablehorses.common.config.CallableHorsesConfig;

public class KeybindManager
{

	public static KeyBinding setHorse;
	public static KeyBinding callHorse;
	public static KeyBinding showStats;

	@SideOnly(Side.CLIENT)
	public static void init()
	{
		setHorse = new KeyBinding("key.sethorse.desc", Keyboard.KEY_P, "key.callablehorses.category");
		callHorse = new KeyBinding("key.callhorse.desc", Keyboard.KEY_V, "key.callablehorses.category");
		showStats = new KeyBinding("key.showstats.desc", Keyboard.KEY_K, "key.callablehorses.category");

		ClientRegistry.registerKeyBinding(setHorse);
		ClientRegistry.registerKeyBinding(callHorse);

		if (CallableHorsesConfig.settings.enableStatsViewer)
			ClientRegistry.registerKeyBinding(showStats);

	}

}
