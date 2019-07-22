package tschipp.callablehorses.common.capabilities.storedhorse;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class HorseProvider implements ICapabilitySerializable {

	@CapabilityInject(IStoredHorse.class)
	public static final Capability<IStoredHorse> HORSE_CAPABILITY = null;
	
	private IStoredHorse instance = HORSE_CAPABILITY.getDefaultInstance();
	
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == HORSE_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == HORSE_CAPABILITY ? HORSE_CAPABILITY.cast(instance) : null;
	}

	@Override
	public NBTBase serializeNBT() {
		return HORSE_CAPABILITY.getStorage().writeNBT(HORSE_CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		HORSE_CAPABILITY.getStorage().readNBT(HORSE_CAPABILITY, instance, null, nbt);
	}

}
