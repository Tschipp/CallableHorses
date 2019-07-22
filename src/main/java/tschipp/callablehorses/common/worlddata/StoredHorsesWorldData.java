package tschipp.callablehorses.common.worlddata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import tschipp.callablehorses.CallableHorses;

public class StoredHorsesWorldData extends WorldSavedData
{
	private static String name = CallableHorses.MODID + "_stored_horses";
	
	private Map<String, Integer> entries = new HashMap<String, Integer>();
	private List<String> killedHorses = new ArrayList<String>();
	private List<String> disbandedHorses = new ArrayList<String>();
	private Map<String, NBTTagCompound> offlineSavedHorses = new HashMap<String, NBTTagCompound>();

	private int i = 0;

	public StoredHorsesWorldData()
	{
		super(name);
	}
	
	public StoredHorsesWorldData(String name)
	{
		super(name);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		int i = 0;
		while(nbt.hasKey("" + i))
		{
			NBTTagCompound subTag = nbt.getCompoundTag("" + i);
			String storageID = subTag.getString("id");
			int num = subTag.getInteger("num");
			
			entries.put(storageID, num);
			
			i++;
		}
		
		i = 0;
		NBTTagCompound killed = nbt.getCompoundTag("killed");
		while(killed.hasKey("" + i))
		{
			killedHorses.add(killed.getCompoundTag("" + i).getString("id"));
			i++;
		}
		
		i = 0;
		NBTTagCompound disbanded = nbt.getCompoundTag("disbanded");
		while(disbanded.hasKey("" + i))
		{
			disbandedHorses.add(disbanded.getCompoundTag("" + i).getString("id"));
			i++;
		}
		
	}


	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		NBTTagCompound tag = new NBTTagCompound();
		entries.forEach((storageID, num) -> {
			NBTTagCompound subTag = new NBTTagCompound();
			subTag.setString("id", storageID);
			subTag.setInteger("num", num);
			tag.setTag("" + i, subTag);
			i++;
		});
		
		i = 0;
		
		NBTTagCompound killed = new NBTTagCompound();
		for(int k = 0; k < killedHorses.size(); k++)
		{
			NBTTagCompound subTag = new NBTTagCompound();
			subTag.setString("id", killedHorses.get(k));
			killed.setTag("" + k, subTag);
		}
		tag.setTag("killed", killed);
		
		NBTTagCompound disbanded = new NBTTagCompound();
		for(int k = 0; k < disbandedHorses.size(); k++)
		{
			NBTTagCompound subTag = new NBTTagCompound();
			subTag.setString("id", disbandedHorses.get(k));
			disbanded.setTag("" + k, subTag);
		}
		tag.setTag("disbanded", disbanded);
		
		return tag;
	}
	
	public void addHorseNum(String id, int num)
	{
		entries.put(id, num);
		this.markDirty();
	}
	
	public int getHorseNum(String id)
	{
		Integer i = entries.get(id);
		if(i == null)
			return 0;
		return i;
	}
	
	public void disbandHorse(String id)
	{
		disbandedHorses.add(id);
		this.markDirty();
	}
	
	public boolean isDisbanded(String id)
	{
		return disbandedHorses.contains(id);
	}
	
	public void clearDisbanded(String id)
	{
		disbandedHorses.remove(id);
		this.markDirty();
	}
	
	public void markKilled(String id)
	{
		killedHorses.add(id);
		this.markDirty();
	}
	
	public boolean wasKilled(String id)
	{
		return killedHorses.contains(id);
		
	}
	
	public void clearKilled(String id)
	{
		killedHorses.remove(id);
		this.markDirty();
	}
	
	public void addOfflineSavedHorse(String id, NBTTagCompound nbt)
	{
		offlineSavedHorses.put(id, nbt);
		this.markDirty();
	}
	
	public boolean wasOfflineSaved(String id)
	{
		return offlineSavedHorses.containsKey(id);
	}
	
	public NBTTagCompound getOfflineSavedHorse(String id)
	{
		return offlineSavedHorses.get(id);
	}
	
	public void clearOfflineSavedHorse(String id)
	{
		offlineSavedHorses.remove(id);
		this.markDirty();
	}
	
	public static StoredHorsesWorldData getInstance(World world)
	{
		MapStorage storage = world.getMapStorage();
		StoredHorsesWorldData instance = (StoredHorsesWorldData) storage.getOrLoadData(StoredHorsesWorldData.class, name);

		if (instance == null)
		{
			instance = new StoredHorsesWorldData();
			storage.setData(name, instance);
		}
		return instance;
	}

}
