package tschipp.callablehorses.common.capabilities.storedhorse;

import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public interface IStoredHorse {

	public String getStorageUUID();
	
	public void setStorageUUID(String uuid);
	
	public String getOwnerUUID();
	
	public void setOwnerUUID(String uuid);
	
	public void setHorseNum(int num);
	
	public int getHorseNum();
	
	public void setOwned(boolean bool);
	
	public boolean isOwned();

}
