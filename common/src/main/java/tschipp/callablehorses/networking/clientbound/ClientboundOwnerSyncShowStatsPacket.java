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

package tschipp.callablehorses.networking.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import tschipp.callablehorses.CallableHorsesCommonClient;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.common.data.HorseOwnerData;
import tschipp.callablehorses.networking.PacketBase;
import tschipp.callablehorses.platform.Services;

public record ClientboundOwnerSyncShowStatsPacket(HorseOwnerData owner) implements PacketBase
{
	public static final Type<ClientboundOwnerSyncShowStatsPacket> TYPE = new Type<>(Constants.PACKET_ID_SYNC_OWNER_SHOW_STATS);

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOwnerSyncShowStatsPacket> CODEC = StreamCodec.composite(
			HorseOwnerData.STREAM_CODEC, ClientboundOwnerSyncShowStatsPacket::owner,
			ClientboundOwnerSyncShowStatsPacket::new
	);

	@Override
	public Type<? extends PacketBase> type()
	{
		return TYPE;
	}

	@Override
	public void handle(Player player)
	{
		if (player == null)
			return;

		Services.PLATFORM.setOwnerData(player, owner);

		// Clientbound only, so this never class-loads client code on a dedicated server.
		CallableHorsesCommonClient.displayStatViewer(player);
	}
}
