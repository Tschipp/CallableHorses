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
	public static void onClientTick(Player player)
	{
		if (player == null)
			return;

		while (KeybindManager.CALL_HORSE.consumeClick())
			send(ServerboundPressKeyPacket.KEY_CALL_HORSE);

		while (KeybindManager.SET_HORSE.consumeClick())
			send(ServerboundPressKeyPacket.KEY_SET_HORSE);

		while (KeybindManager.SHOW_STATS.consumeClick())
		{
			if (Constants.COMMON_CONFIG.settings.enableStatsViewer)
				send(ServerboundPressKeyPacket.KEY_SHOW_STATS);
		}
	}

	private static void send(int key)
	{
		Services.PLATFORM.sendPacketToServer(new ServerboundPressKeyPacket(key));
	}
}
