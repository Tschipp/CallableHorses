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

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import tschipp.callablehorses.common.WhistleSounds;
import tschipp.callablehorses.common.data.HorseOwnerData;
import tschipp.callablehorses.common.data.StoredHorseData;
import tschipp.callablehorses.config.fabric.ConfigLoaderImpl;
import tschipp.callablehorses.events.CommonEvents;

import java.io.IOException;

public class CallableHorsesFabric implements ModInitializer
{
	/** copyOnDeath covers respawns only; dimension changes go through ServerPlayerEvents.COPY_FROM. */
	public static final AttachmentType<HorseOwnerData> HORSE_OWNER_ATTACHMENT = AttachmentRegistry.create(
			Constants.HORSE_OWNER_ID,
			builder -> builder
					.initializer(HorseOwnerData::new)
					.persistent(HorseOwnerData.CODEC)
					.copyOnDeath());

	/** Binding data on horses. Pushed to tracking clients by ClientboundHorseDataSyncPacket. */
	public static final AttachmentType<StoredHorseData> STORED_HORSE_ATTACHMENT = AttachmentRegistry.create(
			Constants.STORED_HORSE_ID,
			builder -> builder
					.initializer(StoredHorseData::new)
					.persistent(StoredHorseData.CODEC));

	@Override
	public void onInitialize()
	{
		CallableHorsesCommon.registerConfig();

		try
		{
			ConfigLoaderImpl.initialize();
		}
		catch (IOException e)
		{
			Constants.LOG.error("Failed to load config", e);
		}

		WhistleSounds.registerVanilla();

		CallableHorsesCommon.registerServerPackets();
		CallableHorsesCommon.registerClientPackets();

		CommonEvents.register();
	}
}
