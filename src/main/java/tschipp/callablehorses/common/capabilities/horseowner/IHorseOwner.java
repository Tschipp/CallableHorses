package tschipp.callablehorses.common.capabilities.horseowner;

import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHorseOwner {

	public AbstractHorse getHorseEntity(World world);
	
	public NBTTagCompound getHorseNBT();
	
	public void setHorseNBT(NBTTagCompound nbt);

	public void setHorse(AbstractHorse horse, EntityPlayer player);
	
	public void clearHorse();
	
	public int getHorseNum();
	
	public void setHorseNum(int num);
	
	public String getStorageUUID();
	
	public void setStorageUUID(String id);

	public void setLastSeenPosition(BlockPos pos);
	
	public BlockPos getLastSeenPosition();
	
	public int getLastSeenDim();
	
	public void setLastSeenDim(int i);

}
