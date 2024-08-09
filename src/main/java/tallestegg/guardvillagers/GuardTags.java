package tallestegg.guardvillagers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class GuardTags {
    public static final TagKey<Item> GUARD_CONVERT = TagKey.create(Registries.ITEM, new ResourceLocation(GuardVillagers.MODID, "convertible_guard_items"));
}
