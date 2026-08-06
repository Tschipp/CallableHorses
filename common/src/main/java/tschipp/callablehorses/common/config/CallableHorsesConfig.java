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

package tschipp.callablehorses.common.config;

import tschipp.callablehorses.config.PropertyType;
import tschipp.callablehorses.config.annotations.Category;
import tschipp.callablehorses.config.annotations.Config;
import tschipp.callablehorses.config.annotations.Property;

@Config("callablehorses-common")
public class CallableHorsesConfig
{
	@Property(type = PropertyType.CATEGORY, description = "General settings")
	public Settings settings = new Settings();

	@Category("settings")
	public static class Settings
	{
		@Property(type = PropertyType.BOOLEAN, description = "Enable debug mode (Developers only) WARNING! Will override all horse names!")
		public boolean enableDebug = false;

		@Property(type = PropertyType.BOOLEAN, description = "Personal Horses get deleted if they're killed. If this is disabled, mounts that aren't horses (a saddled pig, for example) drop nothing at all when they die, because whatever they carry comes back with them.")
		public boolean deathIsPermanent = true;

		@Property(type = PropertyType.BOOLEAN, description = "If the horse can be called in every dimension")
		public boolean callableInEveryDimension = true;

		@Property(type = PropertyType.STRING_ARRAY, description = "Whitelist for dimensions where horses can be called. callableInEveryDimension needs to be false!")
		public String[] callableDimsWhitelist = new String[]{"minecraft:overworld"};

		@Property(type = PropertyType.INT, description = "Maximum block distance from last horse where new horse can be called. Set to -1 to disable range.", min = -1, max = 30_000_000)
		public int maxCallingDistance = -1;

		@Property(type = PropertyType.BOOLEAN, description = "Enable/disable the horse stat viewer GUI")
		public boolean enableStatsViewer = true;

		@Property(type = PropertyType.DOUBLE, description = "Range in which the horse will not teleport, but walk to you. Set to 0 to force the horse to always teleport.", minD = 0d, maxD = 64d)
		public double horseWalkRange = 30d;

		@Property(type = PropertyType.DOUBLE, description = "Speed with which the horse walks when you call it. Vanilla horse walk speed is 1.2", minD = 0d, maxD = 10d)
		public double horseWalkSpeed = 1.8d;

		@Property(type = PropertyType.BOOLEAN, description = "Check against duplicate horses every game tick (20 times per second) instead of only on load. Only use this if you're experiencing problem with duplicate horses! THIS COULD CAUSE LAG!")
		public boolean continuousAntiDupeChecking = false;

		@Property(type = PropertyType.BOOLEAN, description = "Prevents personal horses from dropping items (like leather). This won't prevent armor or saddles from dropping. If this is disabled in combination with deathIsPermanent, it can become a really easy way to farm/duplicate leather, so keep that in mind.")
		public boolean disableHorseDrops = true;

		@Property(type = PropertyType.BOOLEAN, description = "If you need a 3x3x3 space to call your horse")
		public boolean checkForSpace = true;
	}
}
