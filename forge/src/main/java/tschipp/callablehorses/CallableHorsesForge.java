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
import net.minecraft.resources.Identifier;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.registries.RegisterEvent;
import tschipp.callablehorses.caps.CallableHorsesCapabilities;
import tschipp.callablehorses.common.WhistleSounds;
import tschipp.callablehorses.config.forge.ConfigLoaderImpl;

@Mod(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class CallableHorsesForge
{
	public static SimpleChannel network;

	public CallableHorsesForge(FMLJavaModLoadingContext context)
	{
		CallableHorsesCommon.registerConfig();
		ConfigLoaderImpl.initialize(context);
	}

	@SubscribeEvent
	public static void registerCapabilities(final RegisterCapabilitiesEvent event)
	{
		CallableHorsesCapabilities.register(event);
	}

	@SubscribeEvent
	public static void registerSounds(final RegisterEvent event)
	{
		event.register(Registries.SOUND_EVENT, helper -> helper.register(WhistleSounds.WHISTLE_ID, WhistleSounds.WHISTLE));
	}

	@SubscribeEvent
	public static void setup(final FMLCommonSetupEvent event)
	{
		network = ChannelBuilder
				.named(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "callablehorseschannel"))
				.simpleChannel();

		CallableHorsesCommon.registerServerPackets();
		CallableHorsesCommon.registerClientPackets();
	}
}
