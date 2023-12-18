package tschipp.callablehorses.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.helper.HorseHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GuiStatViewer extends Screen
{

	private int xSize = 176;
	private int ySize = 138;

	private static final ResourceLocation TEXTURE = new ResourceLocation(CallableHorses.MODID, "textures/gui/horse_stat_viewer.png");
	private IHorseOwner owner;
	private AbstractHorse horse;

	private float speed;
	private float jumpHeight;
	private float health;
	private float maxHealth;
	private Vec3 lastPos;
	private ResourceKey<Level> lastDim;

	private static Method setColor = ObfuscationReflectionHelper.findMethod(Llama.class, "setSwag", DyeColor.class);

	private Minecraft mc = Minecraft.getInstance();

	public GuiStatViewer(Player player)
	{
		super(Component.literal("Horse Stat Viewer"));
		this.owner = HorseHelper.getOwnerCap(player);
		this.horse = owner.createHorseEntity(player.level());
		horse.getAttributes().load(owner.getHorseNBT().getList("Attributes", 10)); // Read
					
		// attributes		
		this.horse.load(owner.getHorseNBT());

		LazyOptional<IItemHandler> cap = horse.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
		cap.ifPresent(horseInventory -> {
			if (horseInventory.getStackInSlot(0).isEmpty() && horse.isSaddleable())
				horse.equipSaddle(null);// Set saddled

			if (horse instanceof Llama)
			{
				// TODO: Use ObfuscationReflectionHelper

				if (setColor != null)
				{
					try
					{
						ItemStack stack = horseInventory.getStackInSlot(1);
						if (horse.isArmor(stack))
						{
							Item item = stack.getItem();
							if(item instanceof BlockItem blockItem)
							{
								if(blockItem.getBlock() instanceof WoolCarpetBlock carpet)
									setColor.invoke(horse, carpet.getColor());
							}
						}
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
		this.jumpHeight = (float) (Math.floor(horse.getCustomJump() * 100) / 10);
		this.lastPos = owner.getLastSeenPosition();
		this.lastDim = owner.getLastSeenDim();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(graphics);

		// GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		graphics.blit(TEXTURE, i, j, 0, 0, this.xSize, this.ySize, 256, 256);

		super.render(graphics, mouseX, mouseY, partialTicks);

		InventoryScreen.renderEntityInInventoryFollowsMouse(graphics,i + 43, j + 68, 25, (float) (i + 51) - mouseX, (float) (j + 75 - 50) - mouseY, this.horse);

		graphics.drawString(mc.font, this.horse.getName(), i + 84, j + 10, DyeColor.WHITE.getTextColor());

		graphics.drawString(mc.font, "Health:", i + 84, j + 30, DyeColor.LIGHT_GRAY.getTextColor());
		graphics.drawString(mc.font, health + "/" + maxHealth, i + 120, j + 30, DyeColor.WHITE.getTextColor());

		graphics.drawString(mc.font, "Speed:", i + 84, j + 45, DyeColor.LIGHT_GRAY.getTextColor());
		graphics.drawString(mc.font, speed + "", i + 120, j + 45, DyeColor.WHITE.getTextColor());

		graphics.drawString(mc.font, "Jump Height:", i + 84, j + 60, DyeColor.LIGHT_GRAY.getTextColor());
		graphics.drawString(mc.font, jumpHeight + "", i + 148, j + 60, DyeColor.WHITE.getTextColor());

		graphics.drawString(mc.font, "Last known position:" + "", i + 8, j + 84, DyeColor.LIGHT_GRAY.getTextColor());
		graphics.drawString(mc.font, lastPos.equals(Vec3.ZERO) ? "Unknown" : "xyz = " + lastPos.x() + " " + lastPos.y() + " " + lastPos.z(), i + 8, j + 94, DyeColor.WHITE.getTextColor());

		graphics.drawString(mc.font, "Last known dimension:" + "", i + 8, j + 110, DyeColor.LIGHT_GRAY.getTextColor());
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
		if (this.mc.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, modifiers)))
		{
			this.mc.player.closeContainer();
			return true;
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

}
