package tschipp.callablehorses.common.capabilities.horseowner;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public interface IHorseOwner {

	public AbstractHorse createHorseEntity(Level world);
	
	public CompoundTag getHorseNBT();
	
	public void setHorseNBT(CompoundTag nbt);

	public void setHorse(AbstractHorse horse, Player player);
	
	public void clearHorse();
	
	public int getHorseNum();
	
	public void setHorseNum(int num);
	
	public String getStorageUUID();
	
	public void setStorageUUID(String id);

	public void setLastSeenPosition(Vec3 pos);
	
	public Vec3 getLastSeenPosition();
	
	public ResourceKey<Level> getLastSeenDim();
	
	public void setLastSeenDim(ResourceKey<Level> dim);

}
