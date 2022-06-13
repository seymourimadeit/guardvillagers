package tallestegg.guardvillagers.configuration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import tallestegg.guardvillagers.GuardVillagers;

@EventBusSubscriber(modid = GuardVillagers.MODID, bus = EventBusSubscriber.Bus.MOD)
public class GuardConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT;
    static {
        {
            final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
            COMMON = specPair.getLeft();
            COMMON_SPEC = specPair.getRight();
        }
        {
            final Pair<ClientConfig, ForgeConfigSpec> specPair1 = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
            CLIENT = specPair1.getLeft();
            CLIENT_SPEC = specPair1.getRight();
        }
    }

    public static boolean RaidAnimals;
    public static boolean WitchesVillager;
    public static boolean IllusionerRaids;
    public static boolean AttackAllMobs;
    public static boolean VillagersRunFromPolarBears;
    public static boolean IllagersRunFromPolarBears;
    public static boolean GuardsRunFromPolarBears;
    public static boolean GuardsOpenDoors;
    public static boolean GuardAlwaysShield;
    public static boolean GuardFormation;
    public static boolean FriendlyFire;
    public static boolean ConvertVillagerIfHaveHOTV;
    public static boolean BlackSmithHealing;
    public static boolean ClericHealing;
    public static double GuardVillagerHelpRange;
    public static float amountOfHealthRegenerated;
    public static boolean guardArrowsHurtVillagers;
    public static boolean armorerRepairGuardArmor;
    public static boolean giveGuardStuffHOTV;
    public static boolean setGuardPatrolHotv;
    public static boolean guardSteve;
    public static int reputationRequirement;
    public static List<String> MobBlackList;

    public static void bakeCommonConfig() {
        RaidAnimals = COMMON.RaidAnimals.get();
        WitchesVillager = COMMON.WitchesVillager.get();
        IllusionerRaids = COMMON.IllusionerRaids.get();
        AttackAllMobs = COMMON.AttackAllMobs.get();
        VillagersRunFromPolarBears = COMMON.VillagersRunFromPolarBears.get();
        IllagersRunFromPolarBears = COMMON.IllagersRunFromPolarBears.get();
        GuardsRunFromPolarBears = COMMON.GuardsRunFromPolarBears.get();
        GuardsOpenDoors = COMMON.GuardsOpenDoors.get();
        GuardAlwaysShield = COMMON.GuardRaiseShield.get();
        GuardFormation = COMMON.GuardFormation.get();
        FriendlyFire = COMMON.FriendlyFire.get();
        MobBlackList = COMMON.MobBlackList.get();
        ConvertVillagerIfHaveHOTV = COMMON.ConvertVillagerIfHaveHOTV.get();
        BlackSmithHealing = COMMON.BlacksmithHealing.get();
        ClericHealing = COMMON.ClericHealing.get();
        GuardVillagerHelpRange = COMMON.GuardVillagerHelpRange.get();
        amountOfHealthRegenerated = COMMON.amountOfHealthRegenerated.get().floatValue();
        guardArrowsHurtVillagers = COMMON.guardArrowsHurtVillagers.get();
        armorerRepairGuardArmor = COMMON.armorersRepairGuardArmor.get();
        giveGuardStuffHOTV = COMMON.giveGuardStuffHOTV.get();
        setGuardPatrolHotv = COMMON.setGuardPatrolHotv.get();
        reputationRequirement = COMMON.reputationRequirement.get();

    }

    public static void bakeClientConfig() {
        guardSteve = CLIENT.GuardSteve.get();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent.Loading configEvent) {
        if (configEvent.getConfig().getSpec() == GuardConfig.COMMON_SPEC) {
            bakeCommonConfig();
        } else if (configEvent.getConfig().getSpec() == GuardConfig.CLIENT_SPEC) {
            bakeClientConfig();
        }
    }

    public static class CommonConfig {
        public final ForgeConfigSpec.BooleanValue RaidAnimals;
        public final ForgeConfigSpec.BooleanValue WitchesVillager;
        public final ForgeConfigSpec.BooleanValue IllusionerRaids;
        public final ForgeConfigSpec.BooleanValue AttackAllMobs;
        public final ForgeConfigSpec.BooleanValue VillagersRunFromPolarBears;
        public final ForgeConfigSpec.BooleanValue IllagersRunFromPolarBears;
        public final ForgeConfigSpec.BooleanValue GuardsRunFromPolarBears;
        public final ForgeConfigSpec.BooleanValue GuardsOpenDoors;
        public final ForgeConfigSpec.BooleanValue GuardRaiseShield;
        public final ForgeConfigSpec.BooleanValue GuardFormation;
        public final ForgeConfigSpec.BooleanValue FriendlyFire;
        public final ForgeConfigSpec.BooleanValue ConvertVillagerIfHaveHOTV;
        public final ForgeConfigSpec.BooleanValue BlacksmithHealing;
        public final ForgeConfigSpec.BooleanValue ClericHealing;
        public final ForgeConfigSpec.DoubleValue GuardVillagerHelpRange;
        public final ForgeConfigSpec.DoubleValue amountOfHealthRegenerated;
        public final ForgeConfigSpec.BooleanValue guardArrowsHurtVillagers;
        public final ForgeConfigSpec.BooleanValue armorersRepairGuardArmor;
        public final ForgeConfigSpec.ConfigValue<List<String>> MobBlackList;
        public final ForgeConfigSpec.BooleanValue giveGuardStuffHOTV;
        public final ForgeConfigSpec.BooleanValue setGuardPatrolHotv;
        public final ForgeConfigSpec.IntValue reputationRequirement;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.push("raids and illagers");
            RaidAnimals = builder.comment("Illagers In Raids Attack Animals?").translation(GuardVillagers.MODID + ".config.RaidAnimals").define("Illagers in raids attack animals?", false);
            WitchesVillager = builder.comment("Witches Attack Villagers?").translation(GuardVillagers.MODID + ".config.WitchesVillager").define("Witches attack villagers?", true);
            IllusionerRaids = builder.comment("This will make Illusioners get involved in raids").translation(GuardVillagers.MODID + ".config.IllusionerRaids").define("Have Illusioners in raids?", false);
            IllagersRunFromPolarBears = builder.comment("This makes Illagers run from polar bears, as anyone with common sense would.").translation(GuardVillagers.MODID + ".config.IllagersRunFromPolarBears").define("Have Illagers have some common sense?", true);
            builder.pop();
            builder.push("mob ai in general");
            AttackAllMobs = builder.comment("Guards will attack all hostiles with this option").translation(GuardVillagers.MODID + ".config.AttackAllMobs").define("Guards attack all mobs?", false);
            MobBlackList = builder.comment("Guards won't attack mobs in this list if AttackAllMobs is enabled, for example, putting minecraft:creeper in this list will make guards ignore creepers.").define("Mob BlackList", new ArrayList<>());
            builder.pop();
            builder.push("villager stuff");
            armorersRepairGuardArmor = builder.translation(GuardVillagers.MODID + ".config.armorvillager").define("Allow armorers and weaponsmiths repair guard items when down below half durability?", true);
            ConvertVillagerIfHaveHOTV = builder.comment("This will make it so villagers will only be converted into guards if the player has hero of the village").translation(GuardVillagers.MODID + ".config.hotv")
                    .define("Make it so players have to have hero of the village to convert villagers into guards?", false);
            BlacksmithHealing = builder.translation(GuardVillagers.MODID + ".config.blacksmith").define("Have it so blacksmiths heal golems under 60 health?", true);
            ClericHealing = builder.translation(GuardVillagers.MODID + ".config.cleric").define("Have it so clerics heal guards and players with hero of the village?", true);
            VillagersRunFromPolarBears = builder.comment("This makes villagers run from polar bears, as anyone with common sense would.").translation(GuardVillagers.MODID + ".config.VillagersRunFromPolarBears").define("Have Villagers have some common sense?", true);
            builder.pop();
            builder.push("guard stuff");
            GuardsRunFromPolarBears = builder.comment("This makes Guards run from polar bears, as anyone with common sense would.").translation(GuardVillagers.MODID + ".config.IllagersRunFromPolarBears").define("Have Guards have some common sense?", false);
            GuardsOpenDoors = builder.comment("This lets Guards open doors.").translation(GuardVillagers.MODID + ".config.GuardsOpenDoors").define("Have Guards open doors?", true);
            GuardRaiseShield = builder.comment("This will make guards raise their shields all the time, on default they will only raise their shields under certain conditions").translation(GuardVillagers.MODID + ".config.GuardRaiseShield").define("Have Guards raise their shield all the time?",
                    false);
            GuardFormation = builder.comment("This makes guards form a phalanx").translation(GuardVillagers.MODID + ".config.GuardFormation").define("Have guards form a phalanx?", true);
            FriendlyFire = builder.comment("This will make guards attempt to avoid friendly fire.").translation(GuardVillagers.MODID + ".config.FriendlyFire").define("Have guards attempt to avoid firing into other friendlies? (Experimental)", false);
            GuardVillagerHelpRange = builder.translation(GuardVillagers.MODID + ".config.range").comment("This is the range in which the guards will be aggroed to mobs that are attacking villagers. Higher values are more resource intensive, and setting this to zero will disable the goal.")
                    .defineInRange("Range", 50.0D, -500.0D, 500.0D);
            amountOfHealthRegenerated = builder.translation(GuardVillagers.MODID + ".config.amountofHealthRegenerated").comment("How much health a guard regenerates.").defineInRange("Guard health regeneration amount", 1.0D, -500.0D, 500.0D);
            guardArrowsHurtVillagers = builder.translation(GuardVillagers.MODID + ".config.guardArrows").define("Allow guard arrows to damage villagers, iron golems, or other guards? The i-frames will still be shown for them but they won't lose any health if this is set to false", true);
            giveGuardStuffHOTV = builder.translation(GuardVillagers.MODID + ".config.hotvArmor").define("Allow players to give guards stuff only if they have the hero of the village effect?", false);
            setGuardPatrolHotv = builder.translation(GuardVillagers.MODID + ".config.hotvPatrolPoint").define("Allow players to set guard patrol points only if they have hero of the village", false);
            reputationRequirement = builder.defineInRange("Minimum reputation requirement for guards to give you access to their inventories", 15, Integer.MIN_VALUE, Integer.MAX_VALUE);
            builder.pop();
        }
    }

    public static class ClientConfig {
        public final ForgeConfigSpec.BooleanValue GuardSteve;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            GuardSteve = builder.comment("Textures not included, make your own textures by making a resource pack that adds guard_steve_0 - 6").translation(GuardVillagers.MODID + ".config.steveModel").define("Have guards use the steve model?", false);
        }
    }
}