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

package tschipp.callablehorses.common;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import tschipp.callablehorses.Constants;

/**
 * The instance is built here so common code can reference it directly, but registration is left
 * to each loader, because Forge and NeoForge freeze their registries and need their own event.
 */
public class WhistleSounds
{
	public static final ResourceLocation WHISTLE_ID = Constants.id("whistle");

	public static final SoundEvent WHISTLE = SoundEvent.createVariableRangeEvent(WHISTLE_ID);

	public static void registerVanilla()
	{
		Registry.register(BuiltInRegistries.SOUND_EVENT, WHISTLE_ID, WHISTLE);
	}
}
