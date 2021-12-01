package tallestegg.guardvillagers;

import com.google.common.base.Predicate;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestegg.guardvillagers.items.DeferredSpawnEggItem;

@Mod.EventBusSubscriber(modid = GuardVillagers.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GuardItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GuardVillagers.MODID);
    public static final RegistryObject<DeferredSpawnEggItem> GUARD_SPAWN_EGG = ITEMS.register("guard_spawn_egg", () -> new DeferredSpawnEggItem(GuardEntityType.GUARD, 5651507, 9804699, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<SpawnEggItem> IRON_GOLEM_SPAWN_EGG = ITEMS.register("iron_golem_spawn_egg", () -> new SpawnEggItem(EntityType.IRON_GOLEM, 12960449, 16769484, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<SpawnEggItem> SNOW_GOLEM_SPAWN_EGG = ITEMS.register("snow_golem_spawn_egg", () -> new SpawnEggItem(EntityType.SNOW_GOLEM, 15663103, 16753185, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<SpawnEggItem> ILLUSIONER_SPAWN_EGG = ITEMS.register("illusioner_spawn_egg", () -> new SpawnEggItem(EntityType.ILLUSIONER, 9804699, 4547222, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static InteractionHand getHandWith(LivingEntity livingEntity, Predicate<Item> itemPredicate) {
        return itemPredicate.test(livingEntity.getMainHandItem().getItem()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }
}