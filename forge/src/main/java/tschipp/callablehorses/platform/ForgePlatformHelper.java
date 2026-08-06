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
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import tschipp.callablehorses.CallableHorsesCommonClient;
import tschipp.callablehorses.CallableHorsesForge;
import tschipp.callablehorses.caps.CallableHorsesCapabilities;
import tschipp.callablehorses.common.data.HorseOwnerData;
import tschipp.callablehorses.common.data.StoredHorseData;
import tschipp.callablehorses.config.BuiltConfig;
import tschipp.callablehorses.config.forge.ConfigLoaderImpl;
import tschipp.callablehorses.networking.PacketBase;
import tschipp.callablehorses.platform.services.IPlatformHelper;

import java.util.function.BiConsumer;

public class ForgePlatformHelper implements IPlatformHelper
{
	@Override
	public String getPlatformName()
	{
		return "Forge";
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
		BiConsumer<T, CustomPayloadEvent.Context> serverHandler = (packet, ctx) -> {
			if (!ctx.isServerSide())
				return;

			ctx.setPacketHandled(true);
			ctx.enqueueWork(() -> handler.accept(packet, ctx.getSender()));
		};

		CallableHorsesForge.network.messageBuilder(clazz)
				.codec((StreamCodec<FriendlyByteBuf, T>) codec)
				.consumerMainThread(serverHandler)
				.add();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends PacketBase, B extends FriendlyByteBuf> void registerClientboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler)
	{
		BiConsumer<T, CustomPayloadEvent.Context> clientHandler = (packet, ctx) -> {
			if (!ctx.isClientSide())
				return;

			ctx.setPacketHandled(true);
			ctx.enqueueWork(() -> handler.accept(packet, CallableHorsesCommonClient.getPlayer()));
		};

		CallableHorsesForge.network.messageBuilder(clazz)
				.codec((StreamCodec<FriendlyByteBuf, T>) codec)
				.consumerMainThread(clientHandler)
				.add();
	}

	@Override
	public void sendPacketToServer(PacketBase packet)
	{
		CallableHorsesForge.network.send(packet, PacketDistributor.SERVER.noArg());
	}

	@Override
	public void sendPacketToPlayer(PacketBase packet, ServerPlayer player)
	{
		CallableHorsesForge.network.send(packet, PacketDistributor.PLAYER.with(player));
	}

	@Override
	public void sendPacketToPlayersTracking(PacketBase packet, Entity entity)
	{
		CallableHorsesForge.network.send(packet, PacketDistributor.TRACKING_ENTITY.with(entity));
	}

	@Override
	public HorseOwnerData getOwnerData(Player player)
	{
		return player.getCapability(CallableHorsesCapabilities.OWNER_CAPABILITY, null)
				.resolve()
				.orElseGet(HorseOwnerData::new);
	}

	@Override
	public void setOwnerData(Player player, HorseOwnerData data)
	{
		// Forge capabilities hand out a live instance rather than a value, so the incoming
		// data is copied into the attached one.
		getOwnerData(player).copyFrom(data);
	}

	@Nullable
	@Override
	public StoredHorseData getHorseData(Entity horse)
	{
		// Already null for anything the capability was not attached to, so no tag check is needed.
		return horse.getCapability(CallableHorsesCapabilities.HORSE_CAPABILITY, null).resolve().orElse(null);
	}

	// Capabilities are attached in the entity constructor and cannot be added afterwards.
	@Nullable
	@Override
	public StoredHorseData getOrCreateHorseData(Entity horse)
	{
		return getHorseData(horse);
	}

	@Override
	public void setHorseData(Entity horse, StoredHorseData data)
	{
		StoredHorseData attached = getHorseData(horse);
		if (attached != null)
			attached.copyFrom(data);
	}

	@Override
	public boolean isAreaProtected(Player player, Entity probe)
	{
		PlayerInteractEvent.EntityInteract interactEvent = new PlayerInteractEvent.EntityInteract(player, InteractionHand.MAIN_HAND, probe);
		AttackEntityEvent attackEvent = new AttackEntityEvent(player, probe);

		MinecraftForge.EVENT_BUS.post(interactEvent);
		MinecraftForge.EVENT_BUS.post(attackEvent);

		return interactEvent.isCanceled() || attackEvent.isCanceled();
	}
}
