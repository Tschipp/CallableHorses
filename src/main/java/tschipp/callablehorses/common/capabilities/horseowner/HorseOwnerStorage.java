package tschipp.callablehorses.common.capabilities.horseowner;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class HorseOwnerStorage implements IStorage<IHorseOwner> {

	@Override
	public INBT writeNBT(Capability<IHorseOwner> capability, IHorseOwner instance, Direction Dist) {

		CompoundNBT tag = new CompoundNBT();

		tag.put("horse", instance.getHorseNBT());
		tag.putInt("horseNum", instance.getHorseNum());
		tag.putString("uuid", instance.getStorageUUID());
		tag.put("lastSeenPos", NBTUtil.writeBlockPos(new BlockPos(instance.getLastSeenPosition())));
		tag.putString("lastSeenDim", instance.getLastSeenDim().func_240901_a_().toString());		
		return tag;
		

	}

	@Override
	public void readNBT(Capability<IHorseOwner> capability, IHorseOwner instance, Direction Dist, INBT nbt) {

		CompoundNBT tag = (CompoundNBT) nbt;

		instance.setHorseNBT(tag.getCompound("horse"));
		instance.setHorseNum(tag.getInt("horseNum"));
		instance.setStorageUUID(tag.getString("uuid"));
		BlockPos temp = NBTUtil.readBlockPos(tag.getCompound("lastSeenPos"));
		instance.setLastSeenPosition(new Vector3d(temp.getX(), temp.getY(), temp.getZ()));
		instance.setLastSeenDim(RegistryKey.func_240903_a_(Registry.WORLD_KEY, new ResourceLocation(tag.getString("lastSeenDim"))));
	}
}
