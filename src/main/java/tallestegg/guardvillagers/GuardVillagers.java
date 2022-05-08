package tallestegg.guardvillagers;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;

@Mod(GuardVillagers.MODID)
public class GuardVillagers {
    public static final String MODID = "guardvillagers";

    public GuardVillagers() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addAttributes);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GuardConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, GuardConfig.CLIENT_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        GuardEntityType.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        GuardItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        GuardPacketHandler.registerPackets();
        // GuardSpawner.inject();
    }

    private void setup(final FMLCommonSetupEvent event) {
        if (GuardConfig.IllusionerRaids) 
            Raid.RaiderType.create("thebluemengroup", EntityType.ILLUSIONER, new int[] { 0, 0, 0, 0, 0, 1, 1, 2 });
    }

    private void addAttributes(final EntityAttributeCreationEvent event) {
        event.put(GuardEntityType.GUARD.get(), Guard.createAttributes().build());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
    }

    public static boolean hotvChecker(Player player, Guard guard) {
        return player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && GuardConfig.giveGuardStuffHOTV
                || !GuardConfig.giveGuardStuffHOTV || guard.getPlayerReputation(player) > GuardConfig.reputationRequirement && !player.level.isClientSide();
    }
}
