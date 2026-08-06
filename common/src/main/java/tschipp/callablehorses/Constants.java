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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tschipp.callablehorses.common.config.CallableHorsesConfig;

public class Constants
{
	public static final String MOD_ID = "callablehorses";
	public static final String MOD_NAME = "Callable Horses";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final CallableHorsesConfig COMMON_CONFIG = new CallableHorsesConfig();

	/**
	 * The only thing that decides eligibility. The entity still has to be a Mob, because the
	 * walking, tracking and stat viewer code all need one.
	 */
	public static final TagKey<EntityType<?>> CALLABLE_HORSES = TagKey.create(Registries.ENTITY_TYPE, id("callablehorses"));

	public static final ResourceLocation CALL_FOLLOW_RANGE_MODIFIER = id("call_follow_range");

	public static final int HORSE_FLAG_SADDLE = 4;

	public static final ResourceLocation HORSE_OWNER_ID = id("horse_owner");
	public static final ResourceLocation STORED_HORSE_ID = id("stored_horse");

	public static final ResourceLocation PACKET_ID_SYNC_HORSE = id("sync_horse");
	public static final ResourceLocation PACKET_ID_SYNC_OWNER_SHOW_STATS = id("sync_owner_show_stats");
	public static final ResourceLocation PACKET_ID_PRESS_KEY = id("press_key");
	public static final ResourceLocation PACKET_ID_PLAY_WHISTLE = id("play_whistle");

	public static ResourceLocation id(String path)
	{
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
