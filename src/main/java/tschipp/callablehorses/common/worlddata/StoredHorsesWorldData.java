package tschipp.callablehorses.common.worlddata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.saveddata.SavedData;
import tschipp.callablehorses.CallableHorses;

public class StoredHorsesWorldData extends SavedData
{
	private static String name = CallableHorses.MODID + "_stored_horses";

	private Map<String, Integer> entries = new HashMap<String, Integer>();
	private List<String> killedHorses = new ArrayList<String>();
	private List<String> disbandedHorses = new ArrayList<String>();
	private Map<String, CompoundTag> offlineSavedHorses = new HashMap<String, CompoundTag>();

	private int i = 0;

	public StoredHorsesWorldData()
	{

	}

	public static StoredHorsesWorldData load(CompoundTag nbt)
	{
		StoredHorsesWorldData data = new StoredHorsesWorldData();
		int i = 0;
		while (nbt.contains("" + i))
		{
			CompoundTag subTag = nbt.getCompound("" + i);
			String storageID = subTag.getString("id");
			int num = subTag.getInt("num");

			data.entries.put(storageID, num);

			i++;
		}

		i = 0;
		CompoundTag killed = nbt.getCompound("killed");
		while (killed.contains("" + i))
		{
			data.killedHorses.add(killed.getCompound("" + i).getString("id"));
			i++;
		}

		i = 0;
		CompoundTag disbanded = nbt.getCompound("disbanded");
		while (disbanded.contains("" + i))
		{
			data.disbandedHorses.add(disbanded.getCompound("" + i).getString("id"));
			i++;
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag compound)
	{
		CompoundTag tag = new CompoundTag();
		entries.forEach((storageID, num) -> {
			CompoundTag subTag = new CompoundTag();
			subTag.putString("id", storageID);
			subTag.putInt("num", num);
			tag.put("" + i, subTag);
			i++;
		});

		i = 0;

		CompoundTag killed = new CompoundTag();
		for (int k = 0; k < killedHorses.size(); k++)
		{
			CompoundTag subTag = new CompoundTag();
			subTag.putString("id", killedHorses.get(k));
			killed.put("" + k, subTag);
		}
		tag.put("killed", killed);

		CompoundTag disbanded = new CompoundTag();
		for (int k = 0; k < disbandedHorses.size(); k++)
		{
			CompoundTag subTag = new CompoundTag();
			subTag.putString("id", disbandedHorses.get(k));
			disbanded.put("" + k, subTag);
		}
		tag.put("disbanded", disbanded);

		return tag;
	}

	public void addHorseNum(String id, int num)
	{
		entries.put(id, num);
		this.setDirty();
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
		this.setDirty();
	}

	public boolean isDisbanded(String id)
	{
		return disbandedHorses.contains(id);
	}

	public void clearDisbanded(String id)
	{
		disbandedHorses.remove(id);
		this.setDirty();
	}

	public void markKilled(String id)
	{
		killedHorses.add(id);
		this.setDirty();
	}

	public boolean wasKilled(String id)
	{
		return killedHorses.contains(id);

	}

	public void clearKilled(String id)
	{
		killedHorses.remove(id);
		this.setDirty();
	}

	public void addOfflineSavedHorse(String id, CompoundTag nbt)
	{
		offlineSavedHorses.put(id, nbt);
		this.setDirty();
	}

	public boolean wasOfflineSaved(String id)
	{
		return offlineSavedHorses.containsKey(id);
	}

	public CompoundTag getOfflineSavedHorse(String id)
	{
		return offlineSavedHorses.get(id);
	}

	public void clearOfflineSavedHorse(String id)
	{
		offlineSavedHorses.remove(id);
		this.setDirty();
	}

	public static StoredHorsesWorldData getInstance(Level level)
	{
		if (!(level instanceof ServerLevel)) {
			throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
		}

		DimensionDataStorage storage = ((ServerLevel) level).getDataStorage();
		return storage.computeIfAbsent(StoredHorsesWorldData::load, StoredHorsesWorldData::new, name);
	}
}
