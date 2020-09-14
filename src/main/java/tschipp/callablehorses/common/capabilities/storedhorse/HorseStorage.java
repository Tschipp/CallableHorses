package tschipp.callablehorses.common.capabilities.storedhorse;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class HorseStorage implements IStorage<IStoredHorse> {

	@Override
	public INBT writeNBT(Capability<IStoredHorse> capability, IStoredHorse instance, Direction Dist) {

		CompoundNBT tag = new CompoundNBT();

		tag.putString("owner", instance.getOwnerUUID());
		tag.putInt("horseNum", instance.getHorseNum());
		tag.putString("storage", instance.getStorageUUID());
		tag.putBoolean("owned", instance.isOwned());
		
		return tag;

	}

	@Override
	public void readNBT(Capability<IStoredHorse> capability, IStoredHorse instance, Direction Dist, INBT nbt) {

		CompoundNBT tag = (CompoundNBT) nbt;

		instance.setOwnerUUID(tag.getString("owner"));
		instance.setHorseNum(tag.getInt("horseNum"));
		instance.setStorageUUID(tag.getString("storage"));
		instance.setOwned(tag.getBoolean("owned"));
	}

}
