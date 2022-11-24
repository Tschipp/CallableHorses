package tschipp.callablehorses;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tschipp.callablehorses.client.gui.GuiStatViewer;
import tschipp.callablehorses.client.keybinds.KeybindManager;

@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientProxy implements IProxy
{
	@SubscribeEvent
	public static void setup(FMLClientSetupEvent event)
	{
		KeybindManager.init();
	}

	@Override
	public Level getWorld()
	{
		return Minecraft.getInstance().level;
	}

	@Override
	public Player getPlayer()
	{
		return Minecraft.getInstance().player;
	}

	@Override
	public void displayStatViewer()
	{
		Minecraft.getInstance().setScreen(new GuiStatViewer(Minecraft.getInstance().player));		
	}

}
