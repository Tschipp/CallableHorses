package tschipp.callablehorses.common.capabilities.horseowner;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class HorseOwnerProvider implements ICapabilitySerializable<CompoundNBT> {

	@CapabilityInject(IHorseOwner.class)
	public static final Capability<IHorseOwner> OWNER_CAPABILITY = null;
	
	private IHorseOwner instance = OWNER_CAPABILITY.getDefaultInstance();

	@Override
	public CompoundNBT serializeNBT() {
		return (CompoundNBT) OWNER_CAPABILITY.getStorage().writeNBT(OWNER_CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		OWNER_CAPABILITY.getStorage().readNBT(OWNER_CAPABILITY, instance, null, nbt);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction Dist)
	{
		return cap == OWNER_CAPABILITY ? (LazyOptional<T>) LazyOptional.of(() -> instance) : LazyOptional.empty();
	}
}
