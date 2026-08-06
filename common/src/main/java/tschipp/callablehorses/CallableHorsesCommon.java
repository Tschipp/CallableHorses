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

import tschipp.callablehorses.config.ConfigLoader;
import tschipp.callablehorses.networking.clientbound.ClientboundHorseDataSyncPacket;
import tschipp.callablehorses.networking.clientbound.ClientboundOwnerSyncShowStatsPacket;
import tschipp.callablehorses.networking.clientbound.ClientboundPlayWhistlePacket;
import tschipp.callablehorses.networking.serverbound.ServerboundPressKeyPacket;
import tschipp.callablehorses.platform.Services;

public class CallableHorsesCommon
{
	public static void registerConfig()
	{
		ConfigLoader.registerConfig(Constants.COMMON_CONFIG);
	}

	public static void registerServerPackets()
	{
		Services.PLATFORM.registerServerboundPacket(
				ServerboundPressKeyPacket.TYPE,
				ServerboundPressKeyPacket.class,
				ServerboundPressKeyPacket.CODEC,
				ServerboundPressKeyPacket::handle);
	}

	public static void registerClientPackets()
	{
		Services.PLATFORM.registerClientboundPacket(
				ClientboundHorseDataSyncPacket.TYPE,
				ClientboundHorseDataSyncPacket.class,
				ClientboundHorseDataSyncPacket.CODEC,
				ClientboundHorseDataSyncPacket::handle);

		Services.PLATFORM.registerClientboundPacket(
				ClientboundOwnerSyncShowStatsPacket.TYPE,
				ClientboundOwnerSyncShowStatsPacket.class,
				ClientboundOwnerSyncShowStatsPacket.CODEC,
				ClientboundOwnerSyncShowStatsPacket::handle);

		Services.PLATFORM.registerClientboundPacket(
				ClientboundPlayWhistlePacket.TYPE,
				ClientboundPlayWhistlePacket.class,
				ClientboundPlayWhistlePacket.CODEC,
				ClientboundPlayWhistlePacket::handle);
	}
}
