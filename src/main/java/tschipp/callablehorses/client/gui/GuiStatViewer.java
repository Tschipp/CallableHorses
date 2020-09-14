package tschipp.callablehorses.client.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.helper.HorseHelper;

public class GuiStatViewer extends Screen
{

	private int xSize = 176;
	private int ySize = 138;

	private static final ResourceLocation TEXTURE = new ResourceLocation(CallableHorses.MODID, "textures/gui/horse_stat_viewer.png");
	private IHorseOwner owner;
	private AbstractHorseEntity horse;

	private float speed;
	private float jumpHeight;
	private float health;
	private float maxHealth;
	private Vector3d lastPos;
	private RegistryKey<World> lastDim;

	private static Method setColor = ObfuscationReflectionHelper.findMethod(LlamaEntity.class, "func_190711_a", DyeColor.class);

	private Minecraft mc = Minecraft.getInstance();

	public GuiStatViewer(PlayerEntity player)
	{
		super(new StringTextComponent("Horse Stat Viewer"));
		this.owner = HorseHelper.getOwnerCap(player);
		this.horse = owner.createHorseEntity(player.world);
		horse.getAttributeManager().deserialize(owner.getHorseNBT().getList("Attributes", 10)); // Read
					
		// attributes		
		this.horse.read(owner.getHorseNBT());

		LazyOptional<IItemHandler> cap = horse.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		cap.ifPresent(horseInventory -> {
			if (horseInventory.getStackInSlot(0).isEmpty() && horse.func_230264_L__())
				horse.func_230266_a_(null);// Set saddled

			if (horse instanceof LlamaEntity)
			{
				// TODO: Use ObfuscationReflectionHelper

				if (setColor != null)
				{
					try
					{
						ItemStack stack = horseInventory.getStackInSlot(1);
						if (horse.isArmor(stack))
							setColor.invoke(horse, DyeColor.byId(stack.getDamage()));
						else
							setColor.invoke(horse, (DyeColor) null);

					}
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
					{
						e.printStackTrace();
					}
				}

			}

		});

		this.health = (float) (Math.floor(horse.getHealth()));
		this.maxHealth = (float) (Math.floor(horse.getMaxHealth() * 10) / 10);
		this.speed = (float) (Math.floor(horse.getAttribute(Attributes.MOVEMENT_SPEED).getValue() * 100) / 10);
		this.jumpHeight = (float) (Math.floor(horse.getHorseJumpStrength() * 100) / 10);
		this.lastPos = owner.getLastSeenPosition();
		this.lastDim = owner.getLastSeenDim();
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(stack);

		// GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		AbstractGui.blit(stack, i, j, 0, 0, this.xSize, this.ySize, 256, 256);

		super.render(stack, mouseX, mouseY, partialTicks);

		InventoryScreen.drawEntityOnScreen(i + 43, j + 68, 25, (float) (i + 51) - mouseX, (float) (j + 75 - 50) - mouseY, this.horse);

		AbstractGui.drawString(stack, mc.fontRenderer, this.horse.getName(), i + 84, j + 10, DyeColor.WHITE.getColorValue());

		AbstractGui.drawString(stack, mc.fontRenderer, "Health:", i + 84, j + 30, DyeColor.LIGHT_GRAY.getColorValue());
		AbstractGui.drawString(stack, mc.fontRenderer, health + "/" + maxHealth, i + 120, j + 30, DyeColor.WHITE.getColorValue());

		AbstractGui.drawString(stack, mc.fontRenderer, "Speed:", i + 84, j + 45, DyeColor.LIGHT_GRAY.getColorValue());
		AbstractGui.drawString(stack, mc.fontRenderer, speed + "", i + 120, j + 45, DyeColor.WHITE.getColorValue());

		AbstractGui.drawString(stack, mc.fontRenderer, "Jump Height:", i + 84, j + 60, DyeColor.LIGHT_GRAY.getColorValue());
		AbstractGui.drawString(stack, mc.fontRenderer, jumpHeight + "", i + 148, j + 60, DyeColor.WHITE.getColorValue());

		AbstractGui.drawString(stack, mc.fontRenderer, "Last known position:" + "", i + 8, j + 84, DyeColor.LIGHT_GRAY.getColorValue());
		AbstractGui.drawString(stack, mc.fontRenderer, lastPos.equals(Vector3d.ZERO) ? "Unknown" : "xyz = " + lastPos.getX() + " " + lastPos.getY() + " " + lastPos.getZ(), i + 8, j + 94, DyeColor.WHITE.getColorValue());

		AbstractGui.drawString(stack, mc.fontRenderer, "Last known dimension:" + "", i + 8, j + 110, DyeColor.LIGHT_GRAY.getColorValue());
		AbstractGui.drawString(stack, mc.fontRenderer, this.lastDim.func_240901_a_().toString(), i + 8, j + 120, DyeColor.WHITE.getColorValue());

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
		if (this.mc.gameSettings.keyBindInventory.isActiveAndMatches(InputMappings.getInputByCode(keyCode, modifiers)))
		{
			this.mc.player.closeScreen();
			return true;
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

}
