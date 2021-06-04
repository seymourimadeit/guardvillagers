package tallestegg.guardvillagers;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.world.raid.Raid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tallestegg.guardvillagers.client.renderer.GuardRenderer;
import tallestegg.guardvillagers.client.renderer.GuardSteveRenderer;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.GuardEntity;
import tallestegg.guardvillagers.items.DeferredSpawnEggItem;

@Mod(GuardVillagers.MODID)
public class GuardVillagers {
    public static final String MODID = "guardvillagers";

    public GuardVillagers() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GuardConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, GuardConfig.CLIENT_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        GuardEntityType.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        GuardItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        GuardPacketHandler.registerPackets();
        // GuardSpawner.inject();
    }

    @SuppressWarnings("deprecation")
    private void setup(final FMLCommonSetupEvent event) {
        if (GuardConfig.IllusionerRaids) {
            Raid.WaveMember.create("thebluemengroup", EntityType.ILLUSIONER, new int[] { 0, 0, 0, 0, 0, 1, 1, 2 });
        }
        event.enqueueWork(() -> {
            GlobalEntityTypeAttributes.put(GuardEntityType.GUARD.get(), GuardEntity.createAttributes().create());
        });
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        if (GuardConfig.guardSteve) {
            RenderingRegistry.registerEntityRenderingHandler(GuardEntityType.GUARD.get(), GuardSteveRenderer::new);
        } else {
            RenderingRegistry.registerEntityRenderingHandler(GuardEntityType.GUARD.get(), GuardRenderer::new);
        }
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    private void processIMC(final InterModProcessEvent event) {
    }

    public static boolean hotvChecker(PlayerEntity player) {
        return player.isPotionActive(Effects.HERO_OF_THE_VILLAGE) && GuardConfig.giveGuardStuffHOTV || !GuardConfig.giveGuardStuffHOTV;
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onEntitiesRegistered(RegistryEvent.Register<EntityType<?>> event) {
            DeferredSpawnEggItem.initUnaddedEggs();
        }
    }
}
