package tschipp.callablehorses.common.capabilities.storedhorse;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tschipp.callablehorses.common.capabilities.CapabilityHandler;

public class StoredHorse implements IStoredHorse, ICapabilitySerializable<CompoundTag>, ICapabilityProvider
{

	private String storageUUID = "";
	private String ownerUUID = "";
	private int horseNum = 0;
	private boolean owned = false;
	
	@Override
	public String getStorageUUID()
	{
		return storageUUID;
	}

	@Override
	public void setStorageUUID(String uuid)
	{
		storageUUID = uuid;
	}

	@Override
	public String getOwnerUUID()
	{
		return ownerUUID;
	}

	@Override
	public void setOwnerUUID(String uuid)
	{
		ownerUUID = uuid;
	}

	@Override
	public void setHorseNum(int num)
	{
		this.horseNum = num;
	}

	@Override
	public int getHorseNum()
	{
		return horseNum;
	}

	@Override
	public void setOwned(boolean bool)
	{
		this.owned = bool;
	}

	@Override
	public boolean isOwned()
	{
		return owned;
	}

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return CapabilityHandler.HORSE_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this));
	}

	@Override
	public CompoundTag serializeNBT() {
		return writeNBT(this);
	}

	@Override
	public void deserializeNBT(CompoundTag tag) {
		readNBT(this, tag);
	}

	public static CompoundTag writeNBT(IStoredHorse instance) {
		if (instance == null) {
			return null;
		}
		CompoundTag tag = new CompoundTag();

		tag.putString("owner", instance.getOwnerUUID());
		tag.putInt("horseNum", instance.getHorseNum());
		tag.putString("storage", instance.getStorageUUID());
		tag.putBoolean("owned", instance.isOwned());

		return tag;
	}

	public static void readNBT(IStoredHorse instance, CompoundTag tag) {
		instance.setOwnerUUID(tag.getString("owner"));
		instance.setHorseNum(tag.getInt("horseNum"));
		instance.setStorageUUID(tag.getString("storage"));
		instance.setOwned(tag.getBoolean("owned"));
	}
}
