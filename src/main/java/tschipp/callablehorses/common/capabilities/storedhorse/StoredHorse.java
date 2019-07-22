package tschipp.callablehorses.common.capabilities.storedhorse;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import tschipp.callablehorses.CallableHorses;

public class StoredHorse implements IStoredHorse
{

	private String storageUUID = "";
	private String ownerUUID = "";
	private int horseNum = 0;
	private boolean owned = false;
	
	@Override
	public String getStorageUUID()
	{
		return storageUUID;
	}

	@Override
	public void setStorageUUID(String uuid)
	{
		storageUUID = uuid;
	}

	@Override
	public String getOwnerUUID()
	{
		return ownerUUID;
	}

	@Override
	public void setOwnerUUID(String uuid)
	{
		ownerUUID = uuid;
	}

	@Override
	public void setHorseNum(int num)
	{
		this.horseNum = num;
	}

	@Override
	public int getHorseNum()
	{
		return horseNum;
	}

	@Override
	public void setOwned(boolean bool)
	{
		this.owned = bool;
	}

	@Override
	public boolean isOwned()
	{
		return owned;
	}


}
