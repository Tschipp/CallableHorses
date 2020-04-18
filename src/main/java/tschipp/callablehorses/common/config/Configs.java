package tschipp.callablehorses.common.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;

public class Configs {
	
	public static class Settings
	{
		
		@Comment("Enable debug mode (Developers only) WARNING! Will override all horse names!")
		public boolean enableDebug = false;
		
		@Comment("Personal Horses get deleted if they're killed")
		public boolean deathIsPermanent = true;
		
		@Comment("If the horse can be called in every dimension")
		public boolean callableInEveryDimension = true;
	
		@Comment("Whitelist for dimensions where horses can be called. callableInEveryDimension needs to be false!")
		public int[] callableDimsWhitelist = new int[]{
			0
		};
		
		@Comment("Maximum block distance from last horse where new horse can be called. Set to -1 to disable range.")
		@Config.RangeInt(min = -1, max = 30000000)
		public int maxCallingDistance = -1;
		
		@Comment("Enable/disable the horse stat viewer GUI")
		public boolean enableStatsViewer = true;
		
		@Comment("Range in which the horse will not teleport, but walk to you. Set to 0 to force the horse to always teleport.")
		public double horseWalkRange = 30;
		
		@Comment("Speed with which the horse walks when you call it. Vanilla horse walk speed is 1.2")
		public double horseSpeed = 1.8;
	
		@Comment("Check against duplicate horses every game tick (20 times per second) instead of only on load. Only use this if you're experiencing problem with duplicate horses! THIS COULD CAUSE LAG!")
		public boolean continuousAntiDupeChecking = false;
	
		@Comment("Prevents personal horses from dropping items (like leather). This won't prevent armor or saddles from dropping. If this is disabled in combination with deathIsPermanent, it can become a really easy way to farm/duplicate leather, so keep that in mind.")
		public boolean disableHorseDrops = true;
		
		@Comment("If you need a 3x3x3 space to call your horse")
		public boolean checkForSpace = true;
	}

}
