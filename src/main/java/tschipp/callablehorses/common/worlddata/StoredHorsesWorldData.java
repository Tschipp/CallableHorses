package tschipp.callablehorses.common.worlddata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import tschipp.callablehorses.CallableHorses;

public class StoredHorsesWorldData extends WorldSavedData
{
	private static String name = CallableHorses.MODID + "_stored_horses";

	private Map<String, Integer> entries = new HashMap<String, Integer>();
	private List<String> killedHorses = new ArrayList<String>();
	private List<String> disbandedHorses = new ArrayList<String>();
	private Map<String, CompoundNBT> offlineSavedHorses = new HashMap<String, CompoundNBT>();

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
	public void read(CompoundNBT nbt)
	{
		int i = 0;
		while (nbt.contains("" + i))
		{
			CompoundNBT subTag = nbt.getCompound("" + i);
			String storageID = subTag.getString("id");
			int num = subTag.getInt("num");

			entries.put(storageID, num);

			i++;
		}

		i = 0;
		CompoundNBT killed = nbt.getCompound("killed");
		while (killed.contains("" + i))
		{
			killedHorses.add(killed.getCompound("" + i).getString("id"));
			i++;
		}

		i = 0;
		CompoundNBT disbanded = nbt.getCompound("disbanded");
		while (disbanded.contains("" + i))
		{
			disbandedHorses.add(disbanded.getCompound("" + i).getString("id"));
			i++;
		}

	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		CompoundNBT tag = new CompoundNBT();
		entries.forEach((storageID, num) -> {
			CompoundNBT subTag = new CompoundNBT();
			subTag.putString("id", storageID);
			subTag.putInt("num", num);
			tag.put("" + i, subTag);
			i++;
		});

		i = 0;

		CompoundNBT killed = new CompoundNBT();
		for (int k = 0; k < killedHorses.size(); k++)
		{
			CompoundNBT subTag = new CompoundNBT();
			subTag.putString("id", killedHorses.get(k));
			killed.put("" + k, subTag);
		}
		tag.put("killed", killed);

		CompoundNBT disbanded = new CompoundNBT();
		for (int k = 0; k < disbandedHorses.size(); k++)
		{
			CompoundNBT subTag = new CompoundNBT();
			subTag.putString("id", disbandedHorses.get(k));
			disbanded.put("" + k, subTag);
		}
		tag.put("disbanded", disbanded);

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
		if (i == null)
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

	public void addOfflineSavedHorse(String id, CompoundNBT nbt)
	{
		offlineSavedHorses.put(id, nbt);
		this.markDirty();
	}

	public boolean wasOfflineSaved(String id)
	{
		return offlineSavedHorses.containsKey(id);
	}

	public CompoundNBT getOfflineSavedHorse(String id)
	{
		return offlineSavedHorses.get(id);
	}

	public void clearOfflineSavedHorse(String id)
	{
		offlineSavedHorses.remove(id);
		this.markDirty();
	}

	public static StoredHorsesWorldData getInstance(ServerWorld world)
	{
		DimensionSavedDataManager storage = world.getSavedData();
		StoredHorsesWorldData instance = (StoredHorsesWorldData) storage.getOrCreate(StoredHorsesWorldData::new, name);
		
		storage.set(instance);

		return instance;
	}
}
