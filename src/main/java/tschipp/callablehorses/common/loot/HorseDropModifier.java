package tschipp.callablehorses.common.loot;

import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.helper.HorseHelper;

@EventBusSubscriber(modid = CallableHorses.MODID, bus = Bus.MOD)
public class HorseDropModifier extends LootModifier
{
	private ILootCondition[] conditions;
	
	protected HorseDropModifier(ILootCondition[] conditionsIn)
	{
		super(conditionsIn);
		this.conditions = conditionsIn;
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
	{
		Entity entity = context.get(LootParameters.THIS_ENTITY);

		if (entity instanceof AbstractHorseEntity)
		{
			IStoredHorse horse = HorseHelper.getHorseCap(entity);
			if (horse != null && horse.isOwned())
			{
				generatedLoot.clear();
			}
		}
		return generatedLoot;
	}

	@SubscribeEvent
	public static void registerModifierSerializers(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event)
	{
		event.getRegistry().register(new HorseDropModifier.Serializer().setRegistryName(new ResourceLocation(CallableHorses.MODID, "horse_drop")));
	}

	private static class Serializer extends GlobalLootModifierSerializer<HorseDropModifier>
	{
		@Override
		public HorseDropModifier read(ResourceLocation location, JsonObject object, ILootCondition[] ailootcondition)
		{
			return new HorseDropModifier(ailootcondition);
		}

		@Override
		public JsonObject write(HorseDropModifier instance)
		{
			return this.makeConditions(instance.conditions);
		}
	}

}
