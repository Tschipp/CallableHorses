package tschipp.callablehorses.common.capabilities.horseowner;

import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public interface IHorseOwner {

	public AbstractHorseEntity createHorseEntity(World world);
	
	public CompoundNBT getHorseNBT();
	
	public void setHorseNBT(CompoundNBT nbt);

	public void setHorse(AbstractHorseEntity horse, PlayerEntity player);
	
	public void clearHorse();
	
	public int getHorseNum();
	
	public void setHorseNum(int num);
	
	public String getStorageUUID();
	
	public void setStorageUUID(String id);

	public void setLastSeenPosition(Vector3d pos);
	
	public Vector3d getLastSeenPosition();
	
	public RegistryKey<World> getLastSeenDim();
	
	public void setLastSeenDim(RegistryKey<World> dim);

}
