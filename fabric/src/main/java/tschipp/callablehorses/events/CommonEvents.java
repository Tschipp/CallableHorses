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

package tschipp.callablehorses.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.common.config.CallableHorsesConfig;
import tschipp.callablehorses.common.events.EntityEventHandler;
import tschipp.callablehorses.common.helper.HorseHelper;

import java.util.ArrayList;
import java.util.List;

public class CommonEvents
{
	public static void register()
	{
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			// Fabric has no cancellable join event, so a stale duplicate is discarded
			// instead of being refused. It exists for a tick either way.
			if (EntityEventHandler.onEntityJoinLevel(entity, world))
				entity.discard();
		});

		ServerEntityEvents.ENTITY_UNLOAD.register(EntityEventHandler::onEntityLeaveLevel);

		EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) ->
				EntityEventHandler.onStartTracking(player, trackedEntity));

		EntityTrackingEvents.STOP_TRACKING.register((trackedEntity, player) ->
				EntityEventHandler.onStopTracking(player, trackedEntity));

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) ->
				EntityEventHandler.onLivingDeath(entity));

		// The player attachment is declared copyOnDeath, but dimension changes also route
		// through COPY_FROM and are not covered by that, so the copy is done for both.
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
				EntityEventHandler.onPlayerClone(
						HorseHelper.getOwnerData(oldPlayer),
						HorseHelper.getOwnerData(newPlayer)));

		// Fabric has no per-entity tick event, so the debug naming and anti-dupe sweep are
		// driven from the world tick instead.
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			CallableHorsesConfig.Settings settings = Constants.COMMON_CONFIG.settings;
			if (!settings.enableDebug && !settings.continuousAntiDupeChecking)
				return;

			// Collected first: the anti-dupe branch of onLivingTick discards, which removes the
			// entity from the very map getAllEntities is iterating over.
			List<Mob> mounts = new ArrayList<>();
			for (Entity entity : world.getAllEntities())
			{
				if (entity instanceof Mob mob && HorseHelper.getHorseData(mob) != null)
					mounts.add(mob);
			}

			mounts.forEach(EntityEventHandler::onLivingTick);
		});
	}
}
