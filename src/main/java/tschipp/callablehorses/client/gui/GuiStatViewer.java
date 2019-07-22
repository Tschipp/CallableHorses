package tschipp.callablehorses.client.gui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.horseowner.IHorseOwner;
import tschipp.callablehorses.common.helper.HorseHelper;

public class GuiStatViewer extends GuiScreen
{

	private int xSize = 176;
	private int ySize = 138;

	private static final ResourceLocation TEXTURE = new ResourceLocation(CallableHorses.MODID, "textures/gui/horse_stat_viewer.png");
	private EntityPlayer player;
	private IHorseOwner owner;
	private AbstractHorse horse;

	private float speed;
	private float jumpHeight;
	private float health;
	private float maxHealth;
	private BlockPos lastPos;
	private int lastDim;
	private String lastDimString;

	public GuiStatViewer(EntityPlayer player)
	{
		this.player = player;
		this.owner = HorseHelper.getOwnerCap(player);
		this.horse = owner.getHorseEntity(player.world);
		SharedMonsterAttributes.setAttributeModifiers(horse.getAttributeMap(), owner.getHorseNBT().getTagList("Attributes", 10));

		this.horse.readFromNBT(owner.getHorseNBT());

		IItemHandler horseInventory = horse.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		horse.setHorseSaddled(!horseInventory.getStackInSlot(0).isEmpty() && horse.canBeSaddled());

		if (horse instanceof EntityLlama)
		{
			// TODO: Use ObfuscationReflectionHelper
			Method setColor = ReflectionHelper.findMethod(EntityLlama.class, "setColor", "func_190711_a", EnumDyeColor.class);
			
			if (setColor != null)
			{
				try
				{
					ItemStack stack = horseInventory.getStackInSlot(1);
					if (horse.isArmor(stack))
						setColor.invoke(horse, EnumDyeColor.byMetadata(stack.getMetadata()));
					else
						setColor.invoke(horse, (EnumDyeColor)null);

				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}

		}

		this.health = (float) (Math.floor(horse.getHealth()));
		this.maxHealth = (float) (Math.floor(horse.getMaxHealth() * 10) / 10);
		this.speed = (float) (Math.floor(horse.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 100) / 10);
		this.jumpHeight = (float) (Math.floor(horse.getHorseJumpStrength() * 100) / 10);
		this.lastPos = owner.getLastSeenPosition();
		this.lastDim = owner.getLastSeenDim();
		this.lastDimString = DimensionManager.getProviderType(lastDim).getName();
	}

	public void initGui()
	{
		super.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		this.drawDefaultBackground();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

		super.drawScreen(mouseX, mouseY, partialTicks);

		GuiInventory.drawEntityOnScreen(i + 43, j + 68, 25, (float) (i + 51) - mouseX, (float) (j + 75 - 50) - mouseY, this.horse);

		this.drawString(this.fontRenderer, this.horse.getName(), i + 84, j + 10, EnumDyeColor.WHITE.getColorValue());

		this.drawString(this.fontRenderer, "Health:", i + 84, j + 30, EnumDyeColor.SILVER.getColorValue());
		this.drawString(this.fontRenderer, health + "/" + maxHealth, i + 120, j + 30, EnumDyeColor.WHITE.getColorValue());

		this.drawString(this.fontRenderer, "Speed:", i + 84, j + 45, EnumDyeColor.SILVER.getColorValue());
		this.drawString(this.fontRenderer, speed + "", i + 120, j + 45, EnumDyeColor.WHITE.getColorValue());

		this.drawString(this.fontRenderer, "Jump Height:", i + 84, j + 60, EnumDyeColor.SILVER.getColorValue());
		this.drawString(this.fontRenderer, jumpHeight + "", i + 148, j + 60, EnumDyeColor.WHITE.getColorValue());

		this.drawString(this.fontRenderer, "Last known position:" + "", i + 8, j + 84, EnumDyeColor.SILVER.getColorValue());
		this.drawString(this.fontRenderer, lastPos.equals(BlockPos.ORIGIN) ? "Unknown" : "xyz = " + lastPos.getX() + " " + lastPos.getY() + " " + lastPos.getZ(), i + 8, j + 94, EnumDyeColor.WHITE.getColorValue());

		this.drawString(this.fontRenderer, "Last known dimension:" + "", i + 8, j + 110, EnumDyeColor.SILVER.getColorValue());
		this.drawString(this.fontRenderer, this.lastDimString, i + 8, j + 120, EnumDyeColor.WHITE.getColorValue());

	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException
	{
		super.keyTyped(typedChar, keyCode);

		if (this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode))
		{
			this.mc.player.closeScreen();
		}
	}

}
