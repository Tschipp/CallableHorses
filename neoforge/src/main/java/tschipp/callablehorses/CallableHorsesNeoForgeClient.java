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

package tschipp.callablehorses;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import tschipp.callablehorses.client.CallableHorsesClientTick;
import tschipp.callablehorses.client.keybinds.KeybindManager;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class CallableHorsesNeoForgeClient
{
	@SubscribeEvent
	public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event)
	{
		KeybindManager.ALL.forEach(event::register);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event)
	{
		CallableHorsesClientTick.onClientTick(CallableHorsesCommonClient.getPlayer());
	}
}
