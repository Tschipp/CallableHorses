package tschipp.callablehorses.common.capabilities.horseowner;

import java.util.UUID;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class HorseOwnerStorage implements IStorage<IHorseOwner> {

	@Override
	public NBTBase writeNBT(Capability<IHorseOwner> capability, IHorseOwner instance, EnumFacing side) {

		NBTTagCompound tag = new NBTTagCompound();

		tag.setTag("horse", instance.getHorseNBT());
		tag.setInteger("horseNum", instance.getHorseNum());
		tag.setString("uuid", instance.getStorageUUID());
		tag.setTag("lastSeenPos", NBTUtil.createPosTag(instance.getLastSeenPosition()));
		tag.setInteger("lastSeenDim", instance.getLastSeenDim());
		return tag;

	}

	@Override
	public void readNBT(Capability<IHorseOwner> capability, IHorseOwner instance, EnumFacing side, NBTBase nbt) {

		NBTTagCompound tag = (NBTTagCompound) nbt;

		instance.setHorseNBT(tag.getCompoundTag("horse"));
		instance.setHorseNum(tag.getInteger("horseNum"));
		instance.setStorageUUID(tag.getString("uuid"));
		instance.setLastSeenPosition(NBTUtil.getPosFromTag(tag.getCompoundTag("lastSeenPos")));
		instance.setLastSeenDim(tag.getInteger("lastSeenDim"));
	}

}
