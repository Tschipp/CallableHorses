package tschipp.callablehorses.common.capabilities.horseowner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.CapabilityHandler;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;

import java.util.Optional;
import java.util.UUID;

public class HorseOwner implements IHorseOwner, ICapabilitySerializable<CompoundTag>, ICapabilityProvider
{

	private int horseNum = 0;
	private CompoundTag horseNBT = new CompoundTag();
	private String storageUUID = "";
	private ResourceKey<Level> lastSeenDim = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("overworld"));
	private Vec3 lastSeenPos = Vec3.ZERO;

	@Override
	public AbstractHorse createHorseEntity(Level world)
	{
		Optional<EntityType<?>> type = EntityType.by(horseNBT);

		if (type.isPresent())
		{
			Entity entity = type.get().create(world);
			if (entity instanceof AbstractHorse)
			{
				entity.load(horseNBT);

				horseNum++;

				LazyOptional<IStoredHorse> cap = entity.getCapability(CapabilityHandler.HORSE_CAPABILITY, null);
				if (cap.isPresent())
				{
					cap.resolve().get().setHorseNum(horseNum);

					entity.setUUID(UUID.randomUUID());
					entity.clearFire();
					((AbstractHorse) entity).hurtTime = 0;
				}

				return (AbstractHorse) entity;
			}

			CallableHorses.LOGGER.error("The entity with NBT " + horseNBT.toString() + " wasn't a horse somehow?...");
		}
		return null;
	}

	@Override
	public CompoundTag getHorseNBT()
	{
		return horseNBT;
	}

	@Override
	public void setHorse(AbstractHorse horse, Player player)
	{
		storageUUID = UUID.randomUUID().toString();

		LazyOptional<IStoredHorse> cap = horse.getCapability(CapabilityHandler.HORSE_CAPABILITY, null);

		cap.ifPresent(storedHorse -> {
			storedHorse.setHorseNum(horseNum);
			storedHorse.setOwned(true);
			storedHorse.setOwnerUUID(player.getGameProfile().getId().toString());
			storedHorse.setStorageUUID(storageUUID);

			CompoundTag tag = horse.serializeNBT();

			horseNBT = tag;
		});
	}

	@Override
	public void clearHorse()
	{
		horseNum = 0;
		horseNBT = new CompoundTag();
		storageUUID = "";
		lastSeenDim = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("overworld"));
		lastSeenPos = Vec3.ZERO;
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
	public void setHorseNBT(CompoundTag nbt)
	{
		this.horseNBT = nbt;
	}

	@Override
	public void setLastSeenPosition(Vec3 pos)
	{
		this.lastSeenPos = pos;
	}

	@Override
	public Vec3 getLastSeenPosition()
	{
		return this.lastSeenPos;
	}

	@Override
	public ResourceKey<Level> getLastSeenDim()
	{
		return this.lastSeenDim;
	}

	@Override
	public void setLastSeenDim(ResourceKey<Level> dim)
	{
		this.lastSeenDim = dim;
	}

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return CapabilityHandler.OWNER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this));
	}

	@Override
	public CompoundTag serializeNBT() {
		return writeNBT(this);
	}

	@Override
	public void deserializeNBT(CompoundTag tag) {
		readNBT(this, tag);
	}

	public static CompoundTag writeNBT(IHorseOwner instance) {
		if (instance == null) {
			return null;
		}
		CompoundTag tag = new CompoundTag();

		tag.put("horse", instance.getHorseNBT());
		tag.putInt("horseNum", instance.getHorseNum());
		tag.putString("uuid", instance.getStorageUUID());
		tag.put("lastSeenPos", NbtUtils.writeBlockPos(new BlockPos(instance.getLastSeenPosition())));
		tag.putString("lastSeenDim", instance.getLastSeenDim().location().toString());
		return tag;

	}

	public static void readNBT(IHorseOwner instance, CompoundTag tag) {
		instance.setHorseNBT(tag.getCompound("horse"));
		instance.setHorseNum(tag.getInt("horseNum"));
		instance.setStorageUUID(tag.getString("uuid"));
		BlockPos temp = NbtUtils.readBlockPos(tag.getCompound("lastSeenPos"));
		instance.setLastSeenPosition(new Vec3(temp.getX(), temp.getY(), temp.getZ()));
		instance.setLastSeenDim(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("lastSeenDim"))));
	}
}
