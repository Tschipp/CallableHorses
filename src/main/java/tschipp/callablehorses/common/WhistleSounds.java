package tschipp.callablehorses.common;

import java.util.ArrayList;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import tschipp.callablehorses.CallableHorses;

@EventBusSubscriber(bus = Bus.FORGE, modid = CallableHorses.MODID)
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
		sounds.add(s);
		return s;
	}
	
	public static SoundEvent getRandomWhistle()
	{
		return whistle;
	}
	
	@SubscribeEvent
	public static void onRegistry(RegistryEvent.Register<SoundEvent> event)
	{
		event.getRegistry().registerAll(sounds.toArray(new SoundEvent[sounds.size()]));
	}

}
