package tschipp.callablehorses.common.capabilities.horseowner;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.storedhorse.HorseProvider;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;

public class HorseOwner implements IHorseOwner
{

	private int horseNum = 0;
	private CompoundNBT horseNBT = new CompoundNBT();
	private String storageUUID = "";
	private RegistryKey<World> lastSeenDim = RegistryKey.func_240903_a_(Registry.WORLD_KEY, new ResourceLocation("overworld"));
	private Vector3d lastSeenPos = Vector3d.ZERO;

	@Override
	public AbstractHorseEntity createHorseEntity(World world)
	{
		Optional<EntityType<?>> type = EntityType.readEntityType(horseNBT);

		if (type.isPresent())
		{
			Entity entity = type.get().create(world);
			if (entity instanceof AbstractHorseEntity)
			{
				entity.read(horseNBT);

				horseNum++;

				LazyOptional<IStoredHorse> cap = entity.getCapability(HorseProvider.HORSE_CAPABILITY, null);
				if (cap.isPresent())
				{
					cap.resolve().get().setHorseNum(horseNum);

					entity.setUniqueId(UUID.randomUUID());
					entity.extinguish();
					((AbstractHorseEntity) entity).hurtTime = 0;
				}

				return (AbstractHorseEntity) entity;
			}

			CallableHorses.LOGGER.error("The entity with NBT " + horseNBT.toString() + " wasn't a horse somehow?...");
		}
		return null;
	}

	@Override
	public CompoundNBT getHorseNBT()
	{
		return horseNBT;
	}

	@Override
	public void setHorse(AbstractHorseEntity horse, PlayerEntity player)
	{
		storageUUID = UUID.randomUUID().toString();

		LazyOptional<IStoredHorse> cap = horse.getCapability(HorseProvider.HORSE_CAPABILITY, null);

		cap.ifPresent(storedHorse -> {
			storedHorse.setHorseNum(horseNum);
			storedHorse.setOwned(true);
			storedHorse.setOwnerUUID(player.getGameProfile().getId().toString());
			storedHorse.setStorageUUID(storageUUID);

			CompoundNBT tag = horse.serializeNBT();

			horseNBT = tag;
		});
	}

	@Override
	public void clearHorse()
	{
		horseNum = 0;
		horseNBT = new CompoundNBT();
		storageUUID = "";
		lastSeenDim = RegistryKey.func_240903_a_(Registry.WORLD_KEY, new ResourceLocation("overworld"));
		lastSeenPos = Vector3d.ZERO;
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
	public void setHorseNBT(CompoundNBT nbt)
	{
		this.horseNBT = nbt;
	}

	@Override
	public void setLastSeenPosition(Vector3d pos)
	{
		this.lastSeenPos = pos;
	}

	@Override
	public Vector3d getLastSeenPosition()
	{
		return this.lastSeenPos;
	}

	@Override
	public RegistryKey<World> getLastSeenDim()
	{
		return this.lastSeenDim;
	}

	@Override
	public void setLastSeenDim(RegistryKey<World> dim)
	{
		this.lastSeenDim = dim;
	}

}
