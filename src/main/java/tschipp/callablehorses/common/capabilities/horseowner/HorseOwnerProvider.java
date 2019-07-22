package tschipp.callablehorses.common.capabilities.horseowner;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class HorseOwnerProvider implements ICapabilitySerializable {

	@CapabilityInject(IHorseOwner.class)
	public static final Capability<IHorseOwner> OWNER_CAPABILITY = null;
	
	private IHorseOwner instance = OWNER_CAPABILITY.getDefaultInstance();
	
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == OWNER_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == OWNER_CAPABILITY ? OWNER_CAPABILITY.cast(instance) : null;
	}

	@Override
	public NBTBase serializeNBT() {
		return OWNER_CAPABILITY.getStorage().writeNBT(OWNER_CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		OWNER_CAPABILITY.getStorage().readNBT(OWNER_CAPABILITY, instance, null, nbt);
	}

}
