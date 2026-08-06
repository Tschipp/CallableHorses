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

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tschipp.callablehorses.client.gui.GuiStatViewer;

import org.jetbrains.annotations.Nullable;

public class CallableHorsesCommonClient
{
	@Nullable
	public static LocalPlayer getPlayer()
	{
		return Minecraft.getInstance().player;
	}

	@Nullable
	public static Level getLevel()
	{
		return Minecraft.getInstance().level;
	}

	public static void displayStatViewer(Player player)
	{
		Minecraft.getInstance().setScreen(new GuiStatViewer(player));
	}
}
