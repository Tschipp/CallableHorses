package tschipp.callablehorses.common;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import tschipp.callablehorses.CallableHorses;

public class WhistleSounds
{

	public static SoundEvent whistle;

	private static ArrayList<SoundEvent> sounds = new ArrayList<SoundEvent>();
	
	public static void registerSounds()
	{
		whistle = registerSound("whistle");
	}

	private static SoundEvent registerSound(String soundName)
	{
		final ResourceLocation soundID = new ResourceLocation(CallableHorses.MODID, soundName);
		SoundEvent s = new SoundEvent(soundID);
		s.setRegistryName(soundID);
		ForgeRegistries.SOUND_EVENTS.register(s);
		sounds.add(s);
		return s;
	}
	
	public static SoundEvent getRandomWhistle()
	{
		return whistle;
	}

}
