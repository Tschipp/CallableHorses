package tschipp.callablehorses.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tschipp.callablehorses.CallableHorses;

public class WhistleSounds
{
	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CallableHorses.MODID);

	public static final RegistryObject<SoundEvent> WHISTLE = SOUND_EVENTS.register("whistle", () ->
			SoundEvent.createVariableRangeEvent(new ResourceLocation(CallableHorses.MODID, "whistle")));
}
