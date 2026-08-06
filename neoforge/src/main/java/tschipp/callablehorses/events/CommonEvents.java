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

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.common.events.EntityEventHandler;
import tschipp.callablehorses.common.helper.HorseHelper;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class CommonEvents
{
	@SubscribeEvent
	public static void onEntityJoinLevel(EntityJoinLevelEvent event)
	{
		if (EntityEventHandler.onEntityJoinLevel(event.getEntity(), event.getLevel()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onEntityLeaveLevel(EntityLeaveLevelEvent event)
	{
		EntityEventHandler.onEntityLeaveLevel(event.getEntity(), event.getLevel());
	}

	/**
	 * The attachment is declared copyOnDeath, but dimension changes also fire this event and
	 * are not covered by that, so the copy is done explicitly for both.
	 */
	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event)
	{
		EntityEventHandler.onPlayerClone(
				HorseHelper.getOwnerData(event.getOriginal()),
				HorseHelper.getOwnerData(event.getEntity()));
	}

	@SubscribeEvent
	public static void onStopTracking(PlayerEvent.StopTracking event)
	{
		EntityEventHandler.onStopTracking(event.getEntity(), event.getTarget());
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event)
	{
		EntityEventHandler.onStartTracking(event.getEntity(), event.getTarget());
	}

	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent.Post event)
	{
		if (event.getEntity() instanceof LivingEntity living)
			EntityEventHandler.onLivingTick(living);
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event)
	{
		EntityEventHandler.onLivingDeath(event.getEntity());
	}
}
