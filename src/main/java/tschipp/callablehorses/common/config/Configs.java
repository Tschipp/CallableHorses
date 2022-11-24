package tschipp.callablehorses.common.config;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class Configs {

	public static final ServerConfig SERVER;
	public static final ForgeConfigSpec SERVER_SPEC;

	static
	{
		final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
		SERVER_SPEC = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	public static class ServerConfig
	{	
		public final BooleanValue enableDebug;
		public final BooleanValue deathIsPermanent;
		public final BooleanValue callableInEveryDimension;
		public final ConfigValue<List<? extends String>> callableDimsWhitelist;
		public final IntValue maxCallingDistance;
		public final BooleanValue enableStatsViewer;
		public final DoubleValue horseWalkRange;
		public final DoubleValue horseWalkSpeed;
		public final BooleanValue continuousAntiDupeChecking;
		public final BooleanValue disableHorseDrops;
		public final BooleanValue checkForSpace;

		public ServerConfig(ForgeConfigSpec.Builder builder)
		{
			builder.push("settings");
			
			enableDebug = builder
					.comment("Enable debug mode (Developers only) WARNING! Will override all horse names!")
					.define("enableDebug", false);
			
			deathIsPermanent = builder
					.comment("Personal Horses get deleted if they're killed")
					.define("deathIsPermanent", true);
			
			callableInEveryDimension = builder
					.comment("If the horse can be called in every dimension")
					.define("callableInEveryDimension", true);
			
			callableDimsWhitelist = builder
					.comment("Whitelist for dimensions where horses can be called. callableInEveryDimension needs to be false!")
					.defineList("callableDimsWhitelist", List.of("minecraft:overworld"), obj -> {
						return obj instanceof String;
					});
			
			maxCallingDistance = builder
					.comment("Maximum block distance from last horse where new horse can be called. Set to -1 to disable range.")
					.defineInRange("maxCallingDistance", -1, -1, 30_000_000);
			
			enableStatsViewer = builder
					.comment("Enable/disable the horse stat viewer GUI")
					.define("enableStatsViewer", true);
			
			horseWalkRange = builder
					.comment("Range in which the horse will not teleport, but walk to you. Set to 0 to force the horse to always teleport.")
					.defineInRange("horseWalkRange", 30d, 0d, 64d);
			
			horseWalkSpeed = builder
					.comment("Speed with which the horse walks when you call it. Vanilla horse walk speed is 1.2")
					.defineInRange("horseWalkSpeed", 1.8, 0, 10);
			
			continuousAntiDupeChecking = builder
					.comment("Check against duplicate horses every game tick (20 times per second) instead of only on load. Only use this if you're experiencing problem with duplicate horses! THIS COULD CAUSE LAG!")
					.define("continuousAntiDupeChecking", false);
			
			disableHorseDrops = builder
					.comment("Prevents personal horses from dropping items (like leather). This won't prevent armor or saddles from dropping. If this is disabled in combination with deathIsPermanent, it can become a really easy way to farm/duplicate leather, so keep that in mind.")
					.define("disableHorseDrops", true);
			
			checkForSpace = builder
					.comment("If you need a 3x3x3 space to call your horse")
					.define("checkForSpace", true);
			
			builder.pop();
		}
	}
}
