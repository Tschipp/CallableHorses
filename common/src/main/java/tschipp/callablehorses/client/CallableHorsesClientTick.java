/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tschipp.callablehorses.client;

import net.minecraft.world.entity.player.Player;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.client.keybinds.KeybindManager;
import tschipp.callablehorses.networking.serverbound.ServerboundPressKeyPacket;
import tschipp.callablehorses.platform.Services;

public class CallableHorsesClientTick
{
	private static final long PRESS_COOLDOWN_MS = 500;

	private static long lastPressTime = 0;

	public static void onClientTick(Player player)
	{
		if (player == null)
			return;

		if (KeybindManager.CALL_HORSE.isDown())
			send(ServerboundPressKeyPacket.KEY_CALL_HORSE);
		else if (KeybindManager.SET_HORSE.isDown())
			send(ServerboundPressKeyPacket.KEY_SET_HORSE);
		else if (Constants.COMMON_CONFIG.settings.enableStatsViewer && KeybindManager.SHOW_STATS.isDown())
			send(ServerboundPressKeyPacket.KEY_SHOW_STATS);
	}

	private static void send(int key)
	{
		long now = System.currentTimeMillis();
		if (now - lastPressTime <= PRESS_COOLDOWN_MS)
			return;

		lastPressTime = now;
		Services.PLATFORM.sendPacketToServer(new ServerboundPressKeyPacket(key));
	}
}
