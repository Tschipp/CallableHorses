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
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import tschipp.callablehorses.CallableHorsesFabric;
import tschipp.callablehorses.common.data.HorseOwnerData;
import tschipp.callablehorses.common.data.StoredHorseData;
import tschipp.callablehorses.config.BuiltConfig;
import tschipp.callablehorses.config.fabric.ConfigLoaderImpl;
import tschipp.callablehorses.networking.PacketBase;
import tschipp.callablehorses.platform.services.IPlatformHelper;

import org.jetbrains.annotations.Nullable;
import java.util.function.BiConsumer;

public class FabricPlatformHelper implements IPlatformHelper
{
	@Override
	public String getPlatformName()
	{
		return "Fabric";
	}

	@Override
	public boolean isModLoaded(String modId)
	{
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment()
	{
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	public void registerConfig(BuiltConfig cfg)
	{
		ConfigLoaderImpl.registerConfig(cfg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends PacketBase, B extends FriendlyByteBuf> void registerServerboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler)
	{
		PayloadTypeRegistry.playC2S().register(type, (StreamCodec<RegistryFriendlyByteBuf, T>) codec);
		ServerPlayNetworking.registerGlobalReceiver(type, (packet, context) -> handler.accept(packet, context.player()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends PacketBase, B extends FriendlyByteBuf> void registerClientboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler)
	{
		// The payload type has to exist on both sides so the server can encode it, but only
		// a client can hold a receiver for it.
		PayloadTypeRegistry.playS2C().register(type, (StreamCodec<RegistryFriendlyByteBuf, T>) codec);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
			FabricClientNetworking.registerReceiver(type, handler);
	}

	@Override
	public void sendPacketToServer(PacketBase packet)
	{
		FabricClientNetworking.sendToServer(packet);
	}

	@Override
	public void sendPacketToPlayer(PacketBase packet, ServerPlayer player)
	{
		ServerPlayNetworking.send(player, packet);
	}

	@Override
	public void sendPacketToPlayersTracking(PacketBase packet, Entity entity)
	{
		for (ServerPlayer player : PlayerLookup.tracking(entity))
			ServerPlayNetworking.send(player, packet);
	}

	@Override
	public HorseOwnerData getOwnerData(Player player)
	{
		return player.getAttachedOrCreate(CallableHorsesFabric.HORSE_OWNER_ATTACHMENT);
	}

	@Override
	public void setOwnerData(Player player, HorseOwnerData data)
	{
		player.setAttached(CallableHorsesFabric.HORSE_OWNER_ATTACHMENT, data);
	}

	@Nullable
	@Override
	public StoredHorseData getHorseData(Entity horse)
	{
		return horse.getAttached(CallableHorsesFabric.STORED_HORSE_ATTACHMENT);
	}

	@Override
	public StoredHorseData getOrCreateHorseData(Entity horse)
	{
		return horse.getAttachedOrCreate(CallableHorsesFabric.STORED_HORSE_ATTACHMENT);
	}

	@Override
	public void setHorseData(Entity horse, StoredHorseData data)
	{
		horse.setAttached(CallableHorsesFabric.STORED_HORSE_ATTACHMENT, data);
	}

	@Override
	public boolean isAreaProtected(Player player, Entity probe)
	{
		// Only FAIL counts. These are action callbacks rather than cancellable events, so SUCCESS
		// and CONSUME mean another mod handled the interaction, not that the area is protected.
		// Treating those as a denial would let any mod that reacts to right-clicking a mount block
		// calling entirely. Protection mods return FAIL.
		InteractionResult use = UseEntityCallback.EVENT.invoker()
				.interact(player, player.level(), InteractionHand.MAIN_HAND, probe, null);
		InteractionResult attack = AttackEntityCallback.EVENT.invoker()
				.interact(player, player.level(), InteractionHand.MAIN_HAND, probe, null);

		return use instanceof InteractionResult.Fail || attack instanceof InteractionResult.Fail;
	}
}
