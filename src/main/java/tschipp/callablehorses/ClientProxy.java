package tschipp.callablehorses;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
	public World getWorld()
	{
		return Minecraft.getInstance().world;
	}

	@Override
	public PlayerEntity getPlayer()
	{
		return Minecraft.getInstance().player;
	}

	@Override
	public void displayGui(Object gui)
	{
		Minecraft.getInstance().displayGuiScreen((Screen)gui);		
	}

}
