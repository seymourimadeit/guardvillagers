package tallestegg.guardvillagers.loot_tables.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class ArmorSlotFunction extends LootItemConditionalFunction {
    final EquipmentSlot slot;
    public static final MapCodec<ArmorSlotFunction> CODEC = RecordCodecBuilder.mapCodec(
            p_298087_ -> commonFields(p_298087_)
                    .and(EquipmentSlot.CODEC.fieldOf("slot").forGetter(p_298086_ -> p_298086_.slot))
                    .apply(p_298087_, ArmorSlotFunction::new)
    );

    ArmorSlotFunction(List<LootItemCondition> pConditions, EquipmentSlot slot) {
        super(pConditions);
        this.slot = slot;
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

    @Override
    protected ItemStack run(ItemStack pStack, LootContext pContext) {
        LivingEntity livingEntity = (LivingEntity) pContext.getOptionalParameter(LootContextParams.THIS_ENTITY);
        livingEntity.setItemSlot(slot, pStack);
        return pStack;
    }


    public static LootItemConditionalFunction.Builder<?> armorSlotFunction(EquipmentSlot slot) {
        return simpleBuilder(conditions -> new ArmorSlotFunction(conditions, slot));
    }
}

