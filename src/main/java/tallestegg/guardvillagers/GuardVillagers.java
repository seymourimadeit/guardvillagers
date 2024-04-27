package tallestegg.guardvillagers;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import tallestegg.guardvillagers.client.GuardSounds;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.networking.GuardFollowPacket;
import tallestegg.guardvillagers.networking.GuardOpenInventoryPacket;
import tallestegg.guardvillagers.networking.GuardSetPatrolPosPacket;

@Mod(GuardVillagers.MODID)
public class GuardVillagers {
    public static final String MODID = "guardvillagers";

    public GuardVillagers(ModContainer container, IEventBus modEventBus) {
        container.registerConfig(ModConfig.Type.COMMON, GuardConfig.COMMON_SPEC);
        GuardConfig.loadConfig(GuardConfig.COMMON_SPEC, FMLPaths.CONFIGDIR.get().resolve(MODID + "-common.toml").toString());
        container.registerConfig(ModConfig.Type.CLIENT, GuardConfig.CLIENT_SPEC);
        modEventBus.addListener(this::setup);
        NeoForge.EVENT_BUS.register(HandlerEvents.class);
        GuardEntityType.ENTITIES.register(modEventBus);
        GuardItems.ITEMS.register(modEventBus);
        GuardSounds.SOUNDS.register(modEventBus);
        modEventBus.addListener(this::addAttributes);
        modEventBus.addListener(this::addCreativeTabs);
        modEventBus.addListener(this::register);
    }


    private void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar(MODID).versioned("2.0.2");
        reg.playToServer(GuardSetPatrolPosPacket.TYPE, GuardSetPatrolPosPacket.STREAM_CODEC, GuardSetPatrolPosPacket::setPatrolPosition);
        reg.playToClient(GuardOpenInventoryPacket.TYPE, GuardOpenInventoryPacket.STREAM_CODEC, GuardOpenInventoryPacket::handle);
        reg.playToServer(GuardFollowPacket.TYPE, GuardFollowPacket.STREAM_CODEC, GuardFollowPacket::handle);
    }

    public static boolean hotvChecker(Player player, Guard guard) {
        return player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.COMMON.giveGuardStuffHOTV.get()
                || !GuardConfig.COMMON.giveGuardStuffHOTV.get() || guard.getPlayerReputation(player) > GuardConfig.COMMON.reputationRequirement.get() && !player.level().isClientSide();
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
        if (GuardConfig.COMMON.IllusionerRaids.get())
            Raid.RaiderType.create("thebluemengroup", EntityType.ILLUSIONER, new int[]{0, 0, 0, 0, 0, 1, 1, 2});
    }

    @SubscribeEvent
    private void addAttributes(final EntityAttributeCreationEvent event) {
        event.put(GuardEntityType.GUARD.get(), Guard.createAttributes().build());
    }
}
