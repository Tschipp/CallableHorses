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

package tschipp.callablehorses.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tschipp.callablehorses.Constants;
import tschipp.callablehorses.common.data.HorseOwnerData;
import tschipp.callablehorses.common.helper.HorseHelper;
import tschipp.callablehorses.mixin.AbstractHorseAccessor;

import org.jetbrains.annotations.Nullable;

public class GuiStatViewer extends Screen
{
	private static final ResourceLocation TEXTURE = Constants.id("textures/gui/horse_stat_viewer.png");

	private final int xSize = 176;
	private final int ySize = 138;

	@Nullable
	private final Mob horse;

	private float speed;
	private float jumpHeight;
	private float health;
	private float maxHealth;
	private final Vec3 lastPos;
	private final ResourceKey<Level> lastDim;

	private final Minecraft mc = Minecraft.getInstance();

	public GuiStatViewer(Player player)
	{
		super(Component.literal("Horse Stat Viewer"));

		HorseOwnerData owner = HorseHelper.getOwnerData(player);
		this.horse = owner.createHorseEntity(player.level());

		if (this.horse != null)
		{
			// Attributes are not part of the regular entity load, so they are applied first.
			this.horse.getAttributes().load(owner.getUpgradedHorseNBT().getList("Attributes", 10));
			this.horse.load(owner.getUpgradedHorseNBT());


			if (this.horse instanceof AbstractHorse)
			{
				AbstractHorseAccessor accessor = (AbstractHorseAccessor) this.horse;
				boolean saddled = !accessor.callablehorses$getInventory().getItem(AbstractHorse.INV_SLOT_SADDLE).isEmpty();
				accessor.callablehorses$setFlag(Constants.HORSE_FLAG_SADDLE, saddled);
			}

			this.health = (float) Math.floor(horse.getHealth());
			this.maxHealth = (float) (Math.floor(horse.getMaxHealth() * 10) / 10);
			this.speed = (float) (Math.floor(horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * 100) / 10);

			if (horse.getAttribute(Attributes.JUMP_STRENGTH) != null)
				this.jumpHeight = (float) (Math.floor(horse.getAttributeValue(Attributes.JUMP_STRENGTH) * 100) / 10);
		}

		this.lastPos = owner.getLastSeenPosition();
		this.lastDim = owner.getLastSeenDim();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(graphics, mouseX, mouseY, partialTicks);

		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		graphics.blit(TEXTURE, i, j, 0, 0, this.xSize, this.ySize, 256, 256);


		if (this.horse != null)
		{
			InventoryScreen.renderEntityInInventoryFollowsMouse(graphics,
					i + 8, j + 18, i + 78, j + 68,
					20, 0.0625F, mouseX, mouseY, this.horse);

			graphics.drawString(mc.font, this.horse.getName(), i + 84, j + 10, DyeColor.WHITE.getTextColor());
		}

		graphics.drawString(mc.font, "Health:", i + 84, j + 30, DyeColor.GRAY.getTextColor());
		graphics.drawString(mc.font, health + "/" + maxHealth, i + 120, j + 30, DyeColor.WHITE.getTextColor());

		graphics.drawString(mc.font, "Speed:", i + 84, j + 45, DyeColor.GRAY.getTextColor());
		graphics.drawString(mc.font, speed + "", i + 120, j + 45, DyeColor.WHITE.getTextColor());

		graphics.drawString(mc.font, "Jump Height:", i + 84, j + 60, DyeColor.GRAY.getTextColor());
		graphics.drawString(mc.font, jumpHeight + "", i + 148, j + 60, DyeColor.WHITE.getTextColor());

		graphics.drawString(mc.font, "Last known position:", i + 8, j + 84, DyeColor.GRAY.getTextColor());
		graphics.drawString(mc.font, lastPos.equals(Vec3.ZERO) ? "Unknown" : "xyz = " + lastPos.x() + " " + lastPos.y() + " " + lastPos.z(), i + 8, j + 94, DyeColor.WHITE.getTextColor());

		graphics.drawString(mc.font, "Last known dimension:", i + 8, j + 110, DyeColor.GRAY.getTextColor());
		graphics.drawString(mc.font, this.lastDim.location().toString(), i + 8, j + 120, DyeColor.WHITE.getTextColor());
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc()
	{
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if (this.mc.options.keyInventory.matches(keyCode, scanCode))
		{
			this.onClose();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
