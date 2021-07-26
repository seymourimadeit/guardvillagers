package tallestegg.guardvillagers;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.registry.RenderingRegistry;
import tallestegg.guardvillagers.client.models.GuardArmorModel;
import tallestegg.guardvillagers.client.models.GuardModel;
import tallestegg.guardvillagers.client.renderer.GuardRenderer;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;
import tallestegg.guardvillagers.items.DeferredSpawnEggItem;

@Mod(GuardVillagers.MODID)
public class GuardVillagers {
    public static final String MODID = "guardvillagers";
    public static ModelLayerLocation GUARD = new ModelLayerLocation(new ResourceLocation(MODID + "guard"), "guard");
    public static ModelLayerLocation GUARD_STEVE = new ModelLayerLocation(new ResourceLocation(MODID + "guard_steve"),
            "guard_steve");
    public static ModelLayerLocation GUARD_ARMOR = new ModelLayerLocation(new ResourceLocation(MODID + "guard_armor"),
            "guard_armor");

    public GuardVillagers() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addAttributes);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GuardConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, GuardConfig.CLIENT_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        GuardEntityType.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        GuardItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        GuardPacketHandler.registerPackets();
        // GuardSpawner.inject();
    }

    private void setup(final FMLCommonSetupEvent event) {
        if (GuardConfig.IllusionerRaids) {
            Raid.RaiderType.create("thebluemengroup", EntityType.ILLUSIONER, new int[] { 0, 0, 0, 0, 0, 1, 1, 2 });
        }
    }

    private void addAttributes(final EntityAttributeCreationEvent event) {
        event.put(GuardEntityType.GUARD.get(), Guard.createAttributes().build());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(GuardEntityType.GUARD.get(), GuardRenderer::new);
            RenderingRegistry.registerLayerDefinition(GUARD, GuardModel::createBodyLayer);
            RenderingRegistry.registerLayerDefinition(GUARD_ARMOR, GuardArmorModel::createBodyLayer);
        });
        // RenderingRegistry.registerLayerDefinition(GUARD_STEVE,
        // (Supplier<LayerDefinition>) GuardSteveModel.createMesh(new
        // CubeDeformation(0.0F), false));
    }

    public static boolean hotvChecker(Player player) {
        return player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.giveGuardStuffHOTV
                || !GuardConfig.giveGuardStuffHOTV;
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onEntitiesRegistered(RegistryEvent.Register<EntityType<?>> event) {
            DeferredSpawnEggItem.initUnaddedEggs();
        }
    }
}
