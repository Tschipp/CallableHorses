package tschipp.callablehorses.client.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.client.keybinds.KeybindManager;
import tschipp.callablehorses.common.config.Configs;
import tschipp.callablehorses.network.PressKeyPacket;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = CallableHorses.MODID)
public class KeybindEvents
{
	private static long lastPressTime = 0;

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onPlayerTick(PlayerTickEvent event)
	{
		PlayerEntity player = event.player;

		if (player != null && event.side == LogicalSide.CLIENT)
		{
			boolean callHorse = KeybindManager.callHorse.isKeyDown();
			boolean setHorse = KeybindManager.setHorse.isKeyDown();
			boolean showStats = Configs.SERVER.enableStatsViewer.get() ? KeybindManager.showStats.isKeyDown() : false;

			if (callHorse)
			{
				if (System.currentTimeMillis() - lastPressTime > 500)
				{
					lastPressTime = System.currentTimeMillis();
					CallableHorses.network.sendToServer(new PressKeyPacket(0));
				}
			}

			if (setHorse)
			{
				if (System.currentTimeMillis() - lastPressTime > 500)
				{
					lastPressTime = System.currentTimeMillis();
					CallableHorses.network.sendToServer(new PressKeyPacket(1));
				}
			}
			
			if (showStats)
			{
				if (System.currentTimeMillis() - lastPressTime > 500)
				{
					lastPressTime = System.currentTimeMillis();
					CallableHorses.network.sendToServer(new PressKeyPacket(2));
				}
			}
		}
	}

}
