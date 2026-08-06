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

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import tschipp.callablehorses.common.WhistleSounds;
import tschipp.callablehorses.common.data.HorseOwnerData;
import tschipp.callablehorses.common.data.StoredHorseData;
import tschipp.callablehorses.config.neoforge.ConfigLoaderImpl;

import java.util.function.Supplier;

@Mod(Constants.MOD_ID)
public class CallableHorsesNeoForge
{
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
			DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);


	public static final Supplier<AttachmentType<HorseOwnerData>> HORSE_OWNER = ATTACHMENT_TYPES.register(
			Constants.HORSE_OWNER_ID.getPath(),
			() -> AttachmentType.builder(HorseOwnerData::new)
					.serialize(HorseOwnerData.CODEC)
					.copyOnDeath()
					.build());

	public static final Supplier<AttachmentType<StoredHorseData>> STORED_HORSE = ATTACHMENT_TYPES.register(
			Constants.STORED_HORSE_ID.getPath(),
			() -> AttachmentType.builder(StoredHorseData::new)
					.serialize(StoredHorseData.CODEC)
					.build());

	public CallableHorsesNeoForge(IEventBus modBus, ModContainer container)
	{
		CallableHorsesCommon.registerConfig();
		ConfigLoaderImpl.initialize(container);

		ATTACHMENT_TYPES.register(modBus);
		modBus.addListener(this::registerSounds);
		modBus.addListener(this::registerPackets);
	}

	private void registerSounds(final RegisterEvent event)
	{
		event.register(Registries.SOUND_EVENT, helper -> helper.register(WhistleSounds.WHISTLE_ID, WhistleSounds.WHISTLE));
	}

	private void registerPackets(final RegisterPayloadHandlersEvent event)
	{
		PayloadRegistrar registrar = event.registrar("1");
		NeoForgePlatformHelperHolder.REGISTRAR = registrar;

		CallableHorsesCommon.registerServerPackets();
		CallableHorsesCommon.registerClientPackets();

		NeoForgePlatformHelperHolder.REGISTRAR = null;
	}

	/**
	 * The registrar is only valid for the duration of RegisterPayloadHandlersEvent, so it is
	 * parked here for the platform helper to pick up while the common registration runs.
	 */
	public static class NeoForgePlatformHelperHolder
	{
		public static PayloadRegistrar REGISTRAR;
	}
}
