package tallestegg.guardvillagers;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import tallestegg.guardvillagers.client.ClientEvents;
import tallestegg.guardvillagers.client.GuardClientEvents;
import tallestegg.guardvillagers.client.GuardSounds;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.loot_tables.GuardLootTables;

@Mod(GuardVillagers.MODID)
public class GuardVillagers {
    public static final String MODID = "guardvillagers";

    public GuardVillagers(ModContainer container, IEventBus modEventBus) {
        container.registerConfig(ModConfig.Type.COMMON, GuardConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, GuardConfig.CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.STARTUP, GuardConfig.STARTUP_SPEC);
        modEventBus.addListener(this::setup);
        NeoForge.EVENT_BUS.register(HandlerEvents.class);
        GuardEntityType.ENTITIES.register(modEventBus);
        GuardItems.ITEMS.register(modEventBus);
        GuardSounds.SOUNDS.register(modEventBus);
        GuardLootTables.LOOT_ITEM_CONDITION_TYPES.register(modEventBus);
        GuardLootTables.LOOT_ITEM_FUNCTION_TYPES.register(modEventBus);
        GuardStats.STATS.register(modEventBus);
        GuardDataAttachments.ATTACHMENT_TYPES.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(this::serverStart);
        modEventBus.addListener(this::addAttributes);
        modEventBus.addListener(this::addCreativeTabs);
        modEventBus.addListener(GuardClientEvents::layerDefinitions);
        modEventBus.addListener(GuardClientEvents::entityRenderers);
        modEventBus.addListener(ClientEvents::registerLayerDefinitions);
    }

    public static boolean hotvChecker(Player player, Guard guard) {
        return player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.COMMON.giveGuardStuffHOTV.get()
                || !GuardConfig.COMMON.giveGuardStuffHOTV.get() || guard.getPlayerReputation(player) > GuardConfig.COMMON.reputationRequirement.get() && !player.level().isClientSide();
    }

    public static boolean canFollow(Player player) {
        return GuardConfig.COMMON.followHero.get() && player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || !GuardConfig.COMMON.followHero.get();
    }

    @SubscribeEvent
    private void addCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(GuardItems.GUARD_SPAWN_EGG.get());
            event.accept(GuardItems.ILLUSIONER_SPAWN_EGG.get());
        }
    }

    @SubscribeEvent
    private void setup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
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

    private void serverStart(final ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> templatePoolRegistry = event.getServer().registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        Registry<StructureProcessorList> processorListRegistry = event.getServer().registryAccess().lookupOrThrow(Registries.PROCESSOR_LIST);
    }


    @Mod(value = GuardVillagers.MODID, dist = Dist.CLIENT)
    public static class GuardVillagersClient {
        public GuardVillagersClient(ModContainer container, IEventBus modEventBus) {
            container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }
}
