package tschipp.callablehorses.common.capabilities.horseowner;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.storedhorse.HorseProvider;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.helper.HorseHelper;
import tschipp.callablehorses.common.worlddata.StoredHorsesWorldData;

public class HorseOwner implements IHorseOwner
{

	private int horseNum = 0;
	private NBTTagCompound horseNBT = new NBTTagCompound();
	private String storageUUID = "";
	private int lastSeenDim = 0;
	private BlockPos lastSeenPos = BlockPos.ORIGIN;
	
	@Override
	public AbstractHorse getHorseEntity(World world)
	{
		Entity entity = EntityList.createEntityFromNBT(horseNBT, world);
		if (entity instanceof AbstractHorse)
		{	
			horseNum++;

			IStoredHorse horse = entity.getCapability(HorseProvider.HORSE_CAPABILITY, null);
			horse.setHorseNum(horseNum);

			entity.setUniqueId(UUID.randomUUID());
			entity.extinguish();
			((AbstractHorse) entity).hurtTime = 0;
			entity.dimension = world.provider.getDimension();
			entity.timeUntilPortal = 0;

			return (AbstractHorse) entity;
		}

		CallableHorses.LOGGER.error("The entity with NBT " + horseNBT.toString() + " wasn't a horse somehow?...");
		return null;
	}

	@Override
	public NBTTagCompound getHorseNBT()
	{
		return horseNBT;
	}

	@Override
	public void setHorse(AbstractHorse horse, EntityPlayer player)
	{
		storageUUID = UUID.randomUUID().toString();

		IStoredHorse storedHorse = horse.getCapability(HorseProvider.HORSE_CAPABILITY, null);
		storedHorse.setHorseNum(horseNum);
		storedHorse.setOwned(true);
		storedHorse.setOwnerUUID(player.getGameProfile().getId().toString());
		storedHorse.setStorageUUID(storageUUID);

		NBTTagCompound tag = horse.serializeNBT();

		horseNBT = tag;
	}

	@Override
	public void clearHorse()
	{
		horseNum = 0;
		horseNBT = new NBTTagCompound();
		storageUUID = "";
		lastSeenDim = 0;
		lastSeenPos = BlockPos.ORIGIN;
	}

	@Override
	public int getHorseNum()
	{
		return horseNum;
	}

	@Override
	public void setHorseNum(int num)
	{
		horseNum = num;
	}

	@Override
	public String getStorageUUID()
	{
		return storageUUID;
	}

	@Override
	public void setStorageUUID(String id)
	{
		this.storageUUID = id;
	}

	@Override
	public void setHorseNBT(NBTTagCompound nbt)
	{
		this.horseNBT = nbt;
	}

	@Override
	public void setLastSeenPosition(BlockPos pos)
	{
		this.lastSeenPos = pos;
	}

	@Override
	public BlockPos getLastSeenPosition()
	{
		return this.lastSeenPos;
	}

	@Override
	public int getLastSeenDim()
	{
		return this.lastSeenDim;
	}

	@Override
	public void setLastSeenDim(int i)
	{
		this.lastSeenDim = i;
	}

}
