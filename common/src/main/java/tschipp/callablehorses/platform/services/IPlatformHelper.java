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

package tschipp.callablehorses.platform.services;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import tschipp.callablehorses.common.data.HorseOwnerData;
import tschipp.callablehorses.common.data.StoredHorseData;
import tschipp.callablehorses.config.BuiltConfig;
import tschipp.callablehorses.networking.PacketBase;

import org.jetbrains.annotations.Nullable;
import java.util.function.BiConsumer;

public interface IPlatformHelper
{
	/**
	 * Gets the name of the current platform.
	 */
	String getPlatformName();

	/**
	 * Checks if a mod with the given id is loaded.
	 */
	boolean isModLoaded(String modId);

	/**
	 * Check if the game is currently in a development environment.
	 */
	boolean isDevelopmentEnvironment();

	void registerConfig(BuiltConfig cfg);

	<T extends PacketBase, B extends FriendlyByteBuf> void registerServerboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler);

	<T extends PacketBase, B extends FriendlyByteBuf> void registerClientboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler);

	void sendPacketToServer(PacketBase packet);

	void sendPacketToPlayer(PacketBase packet, ServerPlayer player);

	void sendPacketToPlayersTracking(PacketBase packet, Entity entity);

	/**
	 * Per-player horse ownership data. Backed by data attachments on Fabric/NeoForge and
	 * by a capability on Forge.
	 */
	HorseOwnerData getOwnerData(Player player);

	void setOwnerData(Player player, HorseOwnerData data);

	/**
	 * Must never attach data as a side effect: the presence of data is what tells the rest of
	 * the mod that a mount is bound, and this is called for arbitrary entities.
	 */
	@Nullable
	StoredHorseData getHorseData(Entity horse);

	/**
	 * Only for the paths about to write a binding. Still {@code null} on Forge for an entity
	 * constructed without the capability.
	 */
	@Nullable
	StoredHorseData getOrCreateHorseData(Entity horse);

	void setHorseData(Entity horse, StoredHorseData data);

	/**
	 * Asks any installed protection mod whether {@code player} may interact with
	 * {@code probe} at its current position. Used to stop horses being called into, or
	 * bound inside, claimed regions.
	 *
	 * @param probe a horse entity positioned where the interaction would happen
	 */
	boolean isAreaProtected(Player player, Entity probe);
}
