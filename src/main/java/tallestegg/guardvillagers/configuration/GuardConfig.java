package tallestegg.guardvillagers.configuration;

import com.google.common.collect.ImmutableList;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import tallestegg.guardvillagers.GuardVillagers;

import java.util.ArrayList;
import java.util.List;


public class GuardConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT;
    public static final ModConfigSpec STARTUP_SPEC;
    public static final StartUpConfig STARTUP;

    static {
        final Pair<CommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = specPair.getLeft();
        COMMON_SPEC = specPair.getRight();
        final Pair<ClientConfig, ModConfigSpec> specPair1 = new ModConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = specPair1.getLeft();
        CLIENT_SPEC = specPair1.getRight();
        final Pair<StartUpConfig, ModConfigSpec> specPair2 = new ModConfigSpec.Builder().configure(StartUpConfig::new);
        STARTUP = specPair2.getLeft();
        STARTUP_SPEC = specPair2.getRight();
    }


    public static class CommonConfig {
        public final ModConfigSpec.BooleanValue RaidAnimals;
        public final ModConfigSpec.BooleanValue WitchesVillager;
        public final ModConfigSpec.BooleanValue AttackAllMobs;
        public final ModConfigSpec.BooleanValue MobsAttackGuards;
        public final ModConfigSpec.BooleanValue VillagersRunFromPolarBears;
        public final ModConfigSpec.BooleanValue IllagersRunFromPolarBears;
        public final ModConfigSpec.BooleanValue GuardsRunFromPolarBears;
        public final ModConfigSpec.BooleanValue GuardsOpenDoors;
        public final ModConfigSpec.BooleanValue GuardRaiseShield;
        public final ModConfigSpec.BooleanValue GuardFormation;
        public final ModConfigSpec.BooleanValue FriendlyFire;
        public final ModConfigSpec.BooleanValue ConvertVillagerIfHaveHOTV;
        public final ModConfigSpec.BooleanValue guardTeleport;
        public final ModConfigSpec.BooleanValue BlacksmithHealing;
        public final ModConfigSpec.BooleanValue ClericHealing;
        public final ModConfigSpec.DoubleValue GuardVillagerHelpRange;
        public final ModConfigSpec.DoubleValue amountOfHealthRegenerated;
        public final ModConfigSpec.BooleanValue guardArrowsHurtVillagers;
        public final ModConfigSpec.BooleanValue armorersRepairGuardArmor;
        public final ModConfigSpec.ConfigValue<List<? extends String>> MobBlackList;
        public final ModConfigSpec.ConfigValue<List<? extends String>> MobWhiteList;
        public final ModConfigSpec.ConfigValue<List<? extends String>> convertibleProfessions;
        public final ModConfigSpec.ConfigValue<List<? extends String>> structuresThatSpawnGuards;
        public final ModConfigSpec.BooleanValue giveGuardStuffHOTV;
        public final ModConfigSpec.BooleanValue setGuardPatrolHotv;
        public final ModConfigSpec.BooleanValue followHero;
        public final ModConfigSpec.BooleanValue golemFloat;
        public final ModConfigSpec.BooleanValue multiFollow;
        public final ModConfigSpec.BooleanValue guardPatrolVillageAi;
        public final ModConfigSpec.BooleanValue convertGuardOnDeath;
        public final ModConfigSpec.IntValue reputationRequirement;
        public final ModConfigSpec.IntValue reputationRequirementToBeAttacked;
        public final ModConfigSpec.IntValue guardSpawnInVillage;
        public final ModConfigSpec.DoubleValue chanceToDropEquipment;
        public final ModConfigSpec.DoubleValue chanceToBreakEquipment;
        public final ModConfigSpec.DoubleValue guardCrossbowAttackRadius;

        public CommonConfig(ModConfigSpec.Builder builder) {
            builder.push("raids and illagers");
            RaidAnimals = builder.comment("Illagers In Raids Attack Animals?").translation(GuardVillagers.MODID + ".config.RaidAnimals").define("Illagers in raids attack animals?", true);
            WitchesVillager = builder.comment("Witches Attack Villagers?").translation(GuardVillagers.MODID + ".config.WitchesVillager").define("Witches attack villagers?", true);
            IllagersRunFromPolarBears = builder.comment("This makes Illagers run from polar bears, as anyone with common sense would.").translation(GuardVillagers.MODID + ".config.IllagersRunFromPolarBears").define("Have Illagers have some common sense?", true);
            builder.pop();
            builder.push("mob ai in general");
            AttackAllMobs = builder.comment("Guards will attack all hostiles with this option, when set to false guards will only attack zombies and illagers.").translation(GuardVillagers.MODID + ".config.AttackAllMobs").define("Guards attack all mobs?", true);
            MobsAttackGuards = builder.comment("Hostiles attack guards, by default only illagers and zombies will attack guards, the mob blacklist below will effect this option").define("All mobs attack guards", false);
            MobBlackList = builder.comment("Guards won't attack mobs in this list at all, for example, putting \"minecraft:creeper\" in this list will make guards ignore creepers.").defineListAllowEmpty("Mob Blacklist", ImmutableList.of("minecraft:villager", "minecraft:iron_golem", "minecraft:wandering_trader", "guardvillagers:guard", "minecraft:creeper", "minecraft:enderman"), () -> "", obj -> true);
            MobWhiteList = builder.comment("Guards will additionally attack mobs ids put in this list, for example, putting \"minecraft:cow\" in this list will make guards attack cows.").defineListAllowEmpty("Mob Whitelist", new ArrayList<>(), () -> "", obj -> true);
            builder.pop();
            builder.push("villager stuff");
            armorersRepairGuardArmor = builder.translation(GuardVillagers.MODID + ".config.armorvillager").define("Allow armorers and weaponsmiths repair guard items when down below half durability?", true);
            ConvertVillagerIfHaveHOTV = builder.comment("This will make it so villagers will only be converted into guards if the player has hero of the village").translation(GuardVillagers.MODID + ".config.hotv")
                    .define("Make it so players have to have hero of the village to convert villagers into guards?", false);
            BlacksmithHealing = builder.translation(GuardVillagers.MODID + ".config.blacksmith").define("Have it so blacksmiths heal golems under 60 health?", true);
            ClericHealing = builder.translation(GuardVillagers.MODID + ".config.cleric").define("Have it so clerics heal guards and players with hero of the village?", true);
            VillagersRunFromPolarBears = builder.comment("This makes villagers run from polar bears, as anyone with common sense would.").translation(GuardVillagers.MODID + ".config.VillagersRunFromPolarBears").define("Have Villagers have some common sense?", true);
            convertibleProfessions = builder.comment("Professions that can be converted into guards").defineListAllowEmpty("Profession Whitelist for guard conversion", ImmutableList.of("nitwit", "none"), () -> "", obj -> true);
            builder.pop();
            builder.push("golem stuff");
            golemFloat = builder.define("Allow Iron Golems to float on water?", true);
            builder.pop();
            builder.push("guard stuff");
            guardCrossbowAttackRadius = builder.defineInRange("Guard crossbow attack radius", 8.0F, 0.0F, 100000000.0F);
            structuresThatSpawnGuards = builder.defineListAllowEmpty("Structure pieces that spawn guards", ImmutableList.of("minecraft:village/common/iron_golem"), () -> "", obj -> true);
            guardSpawnInVillage = builder.defineInRange("How many guards should spawn in a village?", 6, 0, 100000000);
            convertGuardOnDeath = builder.define("Allow guards to convert to zombie villagers upon being killed by zombies?", true);
            multiFollow = builder.translation(GuardVillagers.MODID + ".config.multifollow").define("Allow the player to right click on bells to mass order guards to follow them?", true);
            chanceToDropEquipment = builder.defineInRange("Chance to drop equipment", 100.0F, -999.9F, 999.0F);
            GuardsRunFromPolarBears = builder.comment("This makes Guards run from polar bears, as anyone with common sense would.").translation(GuardVillagers.MODID + ".config.IllagersRunFromPolarBears").define("Have Guards have some common sense?", false);
            GuardsOpenDoors = builder.comment("This lets Guards open doors.").translation(GuardVillagers.MODID + ".config.GuardsOpenDoors").define("Have Guards open doors?", true);
            GuardRaiseShield = builder.comment("This will make guards raise their shields all the time, on default they will only raise their shields under certain conditions").translation(GuardVillagers.MODID + ".config.GuardRaiseShield").define("Have Guards raise their shield all the time?",
                    false);
            chanceToBreakEquipment = builder.defineInRange("Chance for guards to lose durability", 1.0F, -999.9F, 999.0F);
            guardTeleport = builder.define("Allow guards to teleport if following the player", true);
            GuardFormation = builder.comment("This makes guards form a phalanx").translation(GuardVillagers.MODID + ".config.GuardFormation").define("Have guards form a phalanx?", true);
            FriendlyFire = builder.translation(GuardVillagers.MODID + ".config.FriendlyFire").define("Have guards attempt to avoid firing into other friendlies?", true);
            GuardVillagerHelpRange = builder.translation(GuardVillagers.MODID + ".config.range").comment("This is the range in which the guards will be aggroed to mobs that are attacking villagers. Higher values are more resource intensive, and setting this to zero will disable the goal.")
                    .defineInRange("Range", 50.0D, -500.0D, 500.0D);
            amountOfHealthRegenerated = builder.translation(GuardVillagers.MODID + ".config.amountofHealthRegenerated").comment("How much health a guard regenerates.").defineInRange("Guard health regeneration amount", 1.0D, -500.0D, 500.0D);
            guardArrowsHurtVillagers = builder.translation(GuardVillagers.MODID + ".config.guardArrows").define("Allow guard arrows to damage villagers, iron golems, or other guards? The i-frames will still be shown for them but they won't lose any health if this is set to false", true);
            giveGuardStuffHOTV = builder.translation(GuardVillagers.MODID + ".config.hotvArmor").define("Allow players to give guards stuff only if they have the hero of the village effect?", false);
            setGuardPatrolHotv = builder.translation(GuardVillagers.MODID + ".config.hotvPatrolPoint").define("Allow players to set guard patrol points only if they have hero of the village", false);
            reputationRequirement = builder.defineInRange("Minimum reputation requirement for guards to give you access to their inventories", 15, Integer.MIN_VALUE, Integer.MAX_VALUE);
            followHero = builder.define("Have guards only follow the player if they have hero of the village?", true);
            reputationRequirementToBeAttacked = builder.defineInRange("How low of a reputation of a player should have to be instantly aggroed upon by guards and golems?", -100, -9999, 9999);
            guardPatrolVillageAi = builder.define("Allow guards to naturally patrol villages? This feature can cause lag if a lot of guards are spawned", false);
            builder.pop();
        }
    }

    public static class StartUpConfig {
        public final ModConfigSpec.DoubleValue healthModifier;
        public final ModConfigSpec.DoubleValue speedModifier;
        public final ModConfigSpec.DoubleValue followRangeModifier;

        public StartUpConfig(ModConfigSpec.Builder builder) {
            healthModifier = builder.defineInRange("Guard health", 20.0D, -500.0D, 900.0D);
            speedModifier = builder.defineInRange("Guard speed", 0.5D, -500.0D, 900.0D);
            followRangeModifier = builder.defineInRange("Guard follow range", 20.0D, 0.0D, 900.0D);
        }
    }

    public static class ClientConfig {
        public final ModConfigSpec.BooleanValue GuardSteve;
        public final ModConfigSpec.BooleanValue bigHeadBabyVillager;

        public ClientConfig(ModConfigSpec.Builder builder) {
            GuardSteve = builder.comment("Textures not included, make your own textures by making a resource pack that adds guard_steve_0 - 6").translation(GuardVillagers.MODID + ".config.steveModel").define("Have guards use the steve model?", false);
            bigHeadBabyVillager = builder.define("Have baby villagers have big heads like in bedrock?", true);
        }
    }
}