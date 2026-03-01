package tallestegg.guardvillagers;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GuardItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GuardVillagers.MODID);

    public static final DeferredItem<SpawnEggItem> GUARD_SPAWN_EGG =
            ITEMS.registerItem("guard_spawn_egg",
                    props -> new SpawnEggItem(
                            GuardEntityType.GUARD.get(),
                            5651507,
                            9804699,
                            props
                    )
            );

    public static final DeferredItem<SpawnEggItem> ILLUSIONER_SPAWN_EGG =
            ITEMS.registerItem("illusioner_spawn_egg",
                    props -> new SpawnEggItem(
                            EntityType.ILLUSIONER,
                            9804699,
                            4547222,
                            props
                    )
            );
}