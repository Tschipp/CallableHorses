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

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.caps.CallableHorsesCapabilities;
import tschipp.callablehorses.common.events.EntityEventHandler;
import tschipp.callablehorses.common.helper.HorseHelper;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class CommonEvents
{
	@SubscribeEvent
	public static void onAttachCaps(AttachCapabilitiesEvent.Entities event)
	{
		if (event.getObject() instanceof Player)
			event.addCapability(Constants.HORSE_OWNER_ID, new CallableHorsesCapabilities.OwnerProvider());

		if (HorseHelper.isCallable(event.getObject()))
			event.addCapability(Constants.STORED_HORSE_ID, new CallableHorsesCapabilities.StoredHorseProvider());
	}

	@SubscribeEvent
	public static boolean onEntityJoinLevel(EntityJoinLevelEvent event)
	{
		return EntityEventHandler.onEntityJoinLevel(event.getEntity(), event.getLevel());
	}

	@SubscribeEvent
	public static void onEntityLeaveLevel(EntityLeaveLevelEvent event)
	{
		EntityEventHandler.onEntityLeaveLevel(event.getEntity(), event.getLevel());
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event)
	{
		Player original = event.getOriginal();

		original.reviveCaps();
		EntityEventHandler.onPlayerClone(
				HorseHelper.getOwnerData(original),
				HorseHelper.getOwnerData(event.getEntity()));
		original.invalidateCaps();
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
	public static void onLivingTick(LivingEvent.LivingTickEvent event)
	{
		EntityEventHandler.onLivingTick(event.getEntity());
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event)
	{
		EntityEventHandler.onLivingDeath(event.getEntity());
	}
}
