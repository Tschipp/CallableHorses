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

package tschipp.callablehorses.client.keybinds;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import tschipp.callablehorses.Constants;

import java.util.List;

public class KeybindManager
{
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Constants.id("category"));

	public static final KeyMapping SET_HORSE = new KeyMapping("key.sethorse.desc", GLFW.GLFW_KEY_O, CATEGORY);
	public static final KeyMapping CALL_HORSE = new KeyMapping("key.callhorse.desc", GLFW.GLFW_KEY_V, CATEGORY);
	public static final KeyMapping SHOW_STATS = new KeyMapping("key.showstats.desc", GLFW.GLFW_KEY_K, CATEGORY);

	public static final List<KeyMapping> ALL = List.of(SET_HORSE, CALL_HORSE, SHOW_STATS);
}
