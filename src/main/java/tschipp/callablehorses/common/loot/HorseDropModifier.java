package tschipp.callablehorses.common.loot;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import tschipp.callablehorses.CallableHorses;
import tschipp.callablehorses.common.capabilities.storedhorse.IStoredHorse;
import tschipp.callablehorses.common.helper.HorseHelper;

public class HorseDropModifier extends LootModifier
{
	public static final Supplier<Codec<HorseDropModifier>> CODEC = Suppliers.memoize(() ->
			RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, HorseDropModifier::new)));

	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CallableHorses.MODID);

	public static final RegistryObject<Codec<? extends IGlobalLootModifier>> HORSE_DROP = GLM.register("horse_drop", HorseDropModifier.CODEC);


	private LootItemCondition[] conditions;
	
	protected HorseDropModifier(LootItemCondition[] conditionsIn)
	{
		super(conditionsIn);
		this.conditions = conditionsIn;
	}

	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
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

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return CODEC.get();
	}
}
