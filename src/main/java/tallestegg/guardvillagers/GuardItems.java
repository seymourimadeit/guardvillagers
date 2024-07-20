package tallestegg.guardvillagers;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GuardItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, GuardVillagers.MODID);
    public static final DeferredHolder<Item, DeferredSpawnEggItem> GUARD_SPAWN_EGG = ITEMS.register("guard_spawn_egg", () -> new DeferredSpawnEggItem(GuardEntityType.GUARD, 5651507, 9804699, new Item.Properties()));
    public static final DeferredHolder<Item, SpawnEggItem> ILLUSIONER_SPAWN_EGG = ITEMS.register("illusioner_spawn_egg", () -> new SpawnEggItem(EntityType.ILLUSIONER, 9804699, 4547222, new Item.Properties()));
}