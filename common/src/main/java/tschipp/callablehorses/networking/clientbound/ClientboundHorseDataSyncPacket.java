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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.common.data.StoredHorseData;
import tschipp.callablehorses.networking.PacketBase;
import tschipp.callablehorses.platform.Services;

public record ClientboundHorseDataSyncPacket(int entityId, StoredHorseData horse) implements PacketBase
{
	public static final Type<ClientboundHorseDataSyncPacket> TYPE = new Type<>(Constants.PACKET_ID_SYNC_HORSE);

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundHorseDataSyncPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, ClientboundHorseDataSyncPacket::entityId,
			StoredHorseData.STREAM_CODEC, ClientboundHorseDataSyncPacket::horse,
			ClientboundHorseDataSyncPacket::new
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

		Entity e = player.level().getEntity(entityId);
		if (e == null)
			return;

		Services.PLATFORM.setHorseData(e, horse);
	}
}
