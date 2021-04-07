package tallestegg.guardvillagers;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tallestegg.guardvillagers.items.DeferredSpawnEggItem;

@Mod.EventBusSubscriber(modid = GuardVillagers.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GuardItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GuardVillagers.MODID);
    public static final RegistryObject<DeferredSpawnEggItem> GUARD_SPAWN_EGG = ITEMS.register("guard_spawn_egg", () -> new DeferredSpawnEggItem(GuardEntityType.GUARD, 5651507, 9804699, new Item.Properties().group(ItemGroup.MISC)));
    public static final RegistryObject<SpawnEggItem> IRON_GOLEM_SPAWN_EGG = ITEMS.register("iron_golem_spawn_egg", () -> new SpawnEggItem(EntityType.IRON_GOLEM, 12960449, 16769484, new Item.Properties().group(ItemGroup.MISC)));
    public static final RegistryObject<SpawnEggItem> SNOW_GOLEM_SPAWN_EGG = ITEMS.register("snow_golem_spawn_egg", () -> new SpawnEggItem(EntityType.SNOW_GOLEM, 15663103, 16753185, new Item.Properties().group(ItemGroup.MISC)));
    public static final RegistryObject<SpawnEggItem> ILLUSIONER_SPAWN_EGG = ITEMS.register("illusioner_spawn_egg", () -> new SpawnEggItem(EntityType.ILLUSIONER, 9804699, 4547222, new Item.Properties().group(ItemGroup.MISC)));

    public static Hand getHandWith(LivingEntity livingEntity, Predicate<Item> itemPredicate) {
        return itemPredicate.test(livingEntity.getHeldItemMainhand().getItem()) ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }
}