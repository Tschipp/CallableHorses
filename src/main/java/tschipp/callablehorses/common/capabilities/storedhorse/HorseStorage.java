package tschipp.callablehorses.common.capabilities.storedhorse;

import java.util.UUID;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class HorseStorage implements IStorage<IStoredHorse> {

	@Override
	public NBTBase writeNBT(Capability<IStoredHorse> capability, IStoredHorse instance, EnumFacing side) {

		NBTTagCompound tag = new NBTTagCompound();

		tag.setString("owner", instance.getOwnerUUID());
		tag.setInteger("horseNum", instance.getHorseNum());
		tag.setString("storage", instance.getStorageUUID());
		tag.setBoolean("owned", instance.isOwned());
		
		return tag;

	}

	@Override
	public void readNBT(Capability<IStoredHorse> capability, IStoredHorse instance, EnumFacing side, NBTBase nbt) {

		NBTTagCompound tag = (NBTTagCompound) nbt;

		instance.setOwnerUUID(tag.getString("owner"));
		instance.setHorseNum(tag.getInteger("horseNum"));
		instance.setStorageUUID(tag.getString("storage"));
		instance.setOwned(tag.getBoolean("owned"));
	}

}
