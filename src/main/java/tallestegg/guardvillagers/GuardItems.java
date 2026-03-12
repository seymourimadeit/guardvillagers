package tallestegg.guardvillagers;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GuardItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GuardVillagers.MODID);

    public static final DeferredItem<SpawnEggItem> GUARD_SPAWN_EGG =
            ITEMS.registerItem("guard_spawn_egg",
            properties -> new SpawnEggItem(
                    properties.spawnEgg(GuardEntityType.GUARD.get())
            )
    );

    public static final DeferredItem<SpawnEggItem> ILLUSIONER_SPAWN_EGG =
            ITEMS.registerItem("illusioner_spawn_egg",
            properties -> new SpawnEggItem(
                    properties.spawnEgg(EntityType.ILLUSIONER)
            )
    );

    private GuardItems() {}
}