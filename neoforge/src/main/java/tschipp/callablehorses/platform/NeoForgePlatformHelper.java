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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import tschipp.callablehorses.CallableHorsesNeoForge;
import tschipp.callablehorses.common.data.HorseOwnerData;
import tschipp.callablehorses.common.data.StoredHorseData;
import tschipp.callablehorses.config.BuiltConfig;
import tschipp.callablehorses.config.neoforge.ConfigLoaderImpl;
import tschipp.callablehorses.networking.PacketBase;
import tschipp.callablehorses.platform.services.IPlatformHelper;

import org.jetbrains.annotations.Nullable;
import java.util.function.BiConsumer;

public class NeoForgePlatformHelper implements IPlatformHelper
{
	@Override
	public String getPlatformName()
	{
		return "NeoForge";
	}

	@Override
	public boolean isModLoaded(String modId)
	{
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment()
	{
		return !FMLLoader.isProduction();
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
		CallableHorsesNeoForge.NeoForgePlatformHelperHolder.REGISTRAR.playToServer(
				type,
				(StreamCodec<RegistryFriendlyByteBuf, T>) codec,
				(packet, context) -> handler.accept(packet, context.player()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends PacketBase, B extends FriendlyByteBuf> void registerClientboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler)
	{
		CallableHorsesNeoForge.NeoForgePlatformHelperHolder.REGISTRAR.playToClient(
				type,
				(StreamCodec<RegistryFriendlyByteBuf, T>) codec,
				(packet, context) -> handler.accept(packet, context.player()));
	}

	@Override
	public void sendPacketToServer(PacketBase packet)
	{
		PacketDistributor.sendToServer(packet);
	}

	@Override
	public void sendPacketToPlayer(PacketBase packet, ServerPlayer player)
	{
		PacketDistributor.sendToPlayer(player, packet);
	}

	@Override
	public void sendPacketToPlayersTracking(PacketBase packet, Entity entity)
	{
		PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
	}

	@Override
	public HorseOwnerData getOwnerData(Player player)
	{
		return player.getData(CallableHorsesNeoForge.HORSE_OWNER);
	}

	@Override
	public void setOwnerData(Player player, HorseOwnerData data)
	{
		player.setData(CallableHorsesNeoForge.HORSE_OWNER, data);
	}

	@Nullable
	@Override
	public StoredHorseData getHorseData(Entity horse)
	{
		// getData would attach a default, so presence is checked first.
		return horse.hasData(CallableHorsesNeoForge.STORED_HORSE)
				? horse.getData(CallableHorsesNeoForge.STORED_HORSE)
				: null;
	}

	@Override
	public StoredHorseData getOrCreateHorseData(Entity horse)
	{
		return horse.getData(CallableHorsesNeoForge.STORED_HORSE);
	}

	@Override
	public void setHorseData(Entity horse, StoredHorseData data)
	{
		horse.setData(CallableHorsesNeoForge.STORED_HORSE, data);
	}

	@Override
	public boolean isAreaProtected(Player player, Entity probe)
	{
		PlayerInteractEvent.EntityInteract interactEvent = new PlayerInteractEvent.EntityInteract(player, InteractionHand.MAIN_HAND, probe);
		AttackEntityEvent attackEvent = new AttackEntityEvent(player, probe);

		NeoForge.EVENT_BUS.post(interactEvent);
		NeoForge.EVENT_BUS.post(attackEvent);

		return interactEvent.isCanceled() || attackEvent.isCanceled();
	}
}
