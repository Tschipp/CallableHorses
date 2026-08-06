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

package tschipp.callablehorses.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import tschipp.callablehorses.networking.PacketBase;

import java.util.function.BiConsumer;

@Environment(EnvType.CLIENT)
public class FabricClientNetworking
{
	public static <T extends PacketBase> void registerReceiver(CustomPacketPayload.Type<T> type, BiConsumer<T, Player> handler)
	{
		ClientPlayNetworking.registerGlobalReceiver(type, (packet, context) -> handler.accept(packet, context.player()));
	}

	public static void sendToServer(PacketBase packet)
	{
		ClientPlayNetworking.send(packet);
	}
}
