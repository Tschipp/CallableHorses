package tschipp.callablehorses.common.loot;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.helper.HorseHelper;

import java.util.List;

public class HorseDropModifier extends LootModifier
{
	public static final DeferredRegister<GlobalLootModifierSerializer<?>> GLM = DeferredRegister.create(ForgeRegistries.Keys.LOOT_MODIFIER_SERIALIZERS, CallableHorses.MODID);

	public static final RegistryObject<Serializer> HORSE_DROP = GLM.register("horse_drop", HorseDropModifier.Serializer::new);

	private LootItemCondition[] conditions;
	
	protected HorseDropModifier(LootItemCondition[] conditionsIn)
	{
		super(conditionsIn);
		this.conditions = conditionsIn;
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
	{
		if (context.hasParam(LootContextParams.THIS_ENTITY))
		{
			Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);

			if (entity instanceof AbstractHorse)
			{
				IStoredHorse horse = HorseHelper.getHorseCap(entity);
				if (horse != null && horse.isOwned())
				{
					generatedLoot.clear();
				}
			}
		}

		return generatedLoot;
	}

	private static class Serializer extends GlobalLootModifierSerializer<HorseDropModifier>
	{
		@Override
		public HorseDropModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] ailootcondition)
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
