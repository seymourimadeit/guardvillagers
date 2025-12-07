package tallestegg.guardvillagers;

import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import tallestegg.guardvillagers.client.GuardSounds;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;

@Mod(GuardVillagers.MODID)
public class GuardVillagers {
    public static final String MODID = "guardvillagers";

    public GuardVillagers() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GuardConfig.COMMON_SPEC);
        GuardConfig.loadConfig(GuardConfig.COMMON_SPEC, FMLPaths.CONFIGDIR.get().resolve(MODID + "-common.toml").toString());
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, GuardConfig.CLIENT_SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        GuardEntityType.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        GuardItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        GuardSounds.SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        GuardStats.STATS.register(FMLJavaModLoadingContext.get().getModEventBus());
        GuardLootTables.LOOT_ITEM_FUNCTION_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addAttributes);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addCreativeTabs);
        GuardPacketHandler.registerPackets();
    }

    public static boolean hotvChecker(Player player, Guard guard) {
        return player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.giveGuardStuffHOTV
                || !GuardConfig.giveGuardStuffHOTV || guard.getPlayerReputation(player) > GuardConfig.reputationRequirement && !player.level().isClientSide();
    }

    private void addCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(GuardItems.GUARD_SPAWN_EGG.get());
            event.accept(GuardItems.ILLUSIONER_SPAWN_EGG.get());
        }
    }

    public static boolean canFollow(Player player) {
        return GuardConfig.COMMON.followHero.get() && player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || !GuardConfig.COMMON.followHero.get();
    }

    private void setup(final FMLCommonSetupEvent event) {
        Stats.CUSTOM.get(GuardStats.GUARDS_MADE.get(), StatFormatter.DEFAULT);
        if (GuardConfig.IllusionerRaids)
            Raid.RaiderType.create("thebluemengroup", EntityType.ILLUSIONER, new int[]{0, 0, 0, 0, 0, 1, 1, 2});
    }

    private void addAttributes(final EntityAttributeCreationEvent event) {
        event.put(GuardEntityType.GUARD.get(), Guard.createAttributes().build());
    }

    // Turns something like modid:john to simply john
    public static String removeModIdFromVillagerType(String stringWithModId) {
        String[] parts = stringWithModId.split(":");
        if (parts.length <= 1)
            return parts[0];
        else
            return parts[1];
    }
}
