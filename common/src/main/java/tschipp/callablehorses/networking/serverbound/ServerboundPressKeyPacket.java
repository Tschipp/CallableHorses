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

package tschipp.callablehorses.networking.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.common.HorseManager;
import tschipp.callablehorses.networking.PacketBase;

public record ServerboundPressKeyPacket(int key) implements PacketBase
{
	public static final int KEY_CALL_HORSE = 0;
	public static final int KEY_SET_HORSE = 1;
	public static final int KEY_SHOW_STATS = 2;

	public static final Type<ServerboundPressKeyPacket> TYPE = new Type<>(Constants.PACKET_ID_PRESS_KEY);

	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPressKeyPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, ServerboundPressKeyPacket::key,
			ServerboundPressKeyPacket::new
	);

	@Override
	public Type<? extends PacketBase> type()
	{
		return TYPE;
	}

	@Override
	public void handle(Player player)
	{
		if (!(player instanceof ServerPlayer serverPlayer))
			return;

		switch (key)
		{
			case KEY_CALL_HORSE -> HorseManager.callHorse(serverPlayer);
			case KEY_SET_HORSE -> HorseManager.setHorse(serverPlayer);
			case KEY_SHOW_STATS -> HorseManager.showHorseStats(serverPlayer);
			default -> Constants.LOG.warn("Received unknown key press id {} from {}", key, serverPlayer.getGameProfile().name());
		}
	}
}
