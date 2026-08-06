/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tschipp.callablehorses.caps;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tschipp.callablehorses.common.data.HorseOwnerData;
import tschipp.callablehorses.common.data.StoredHorseData;

public class CallableHorsesCapabilities
{
	public static final Capability<HorseOwnerData> OWNER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<StoredHorseData> HORSE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

	public static void register(RegisterCapabilitiesEvent event)
	{
		event.register(HorseOwnerData.class);
		event.register(StoredHorseData.class);
	}

	public static class OwnerProvider implements ICapabilitySerializable<CompoundTag>
	{
		private final HorseOwnerData data = new HorseOwnerData();
		private final LazyOptional<HorseOwnerData> optional = LazyOptional.of(() -> data);

		@NotNull
		@Override
		public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
		{
			return OWNER_CAPABILITY.orEmpty(cap, optional);
		}

		@Override
		public CompoundTag serializeNBT(HolderLookup.Provider provider)
		{
			return data.toNbt();
		}

		@Override
		public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag)
		{
			data.readNbt(tag);
		}
	}

	public static class StoredHorseProvider implements ICapabilitySerializable<CompoundTag>
	{
		private final StoredHorseData data = new StoredHorseData();
		private final LazyOptional<StoredHorseData> optional = LazyOptional.of(() -> data);

		@NotNull
		@Override
		public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
		{
			return HORSE_CAPABILITY.orEmpty(cap, optional);
		}

		@Override
		public CompoundTag serializeNBT(HolderLookup.Provider provider)
		{
			return data.toNbt();
		}

		@Override
		public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag)
		{
			data.readNbt(tag);
		}
	}
}
