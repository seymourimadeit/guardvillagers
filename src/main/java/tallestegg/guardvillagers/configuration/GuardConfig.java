package tallestegg.guardvillagers.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.Lists;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import tallestegg.guardvillagers.GuardVillagers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class GuardConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT;

    static {
        final Pair<CommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = specPair.getLeft();
        COMMON_SPEC = specPair.getRight();
        final Pair<ClientConfig, ModConfigSpec> specPair1 = new ModConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = specPair1.getLeft();
        CLIENT_SPEC = specPair1.getRight();
    }

    /*
     *Thanks to AzureDoom and Tslat for letting me know that this is possible on the MMD discord
     */
    public static void loadConfig(ModConfigSpec config, String path) {
        final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave()
                .writingMode(WritingMode.REPLACE).build();
        file.load();
        config.setConfig(file);
    }
    public static class CommonConfig {
        public final ModConfigSpec.BooleanValue RaidAnimals;
        public final ModConfigSpec.BooleanValue WitchesVillager;
        public final ModConfigSpec.BooleanValue IllusionerRaids;
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
        public final ModConfigSpec.BooleanValue BlacksmithHealing;
        public final ModConfigSpec.BooleanValue ClericHealing;
        public final ModConfigSpec.DoubleValue GuardVillagerHelpRange;
        public final ModConfigSpec.DoubleValue amountOfHealthRegenerated;
        public final ModConfigSpec.DoubleValue healthModifier;
        public final ModConfigSpec.DoubleValue speedModifier;
        public final ModConfigSpec.DoubleValue followRangeModifier;
        public final ModConfigSpec.BooleanValue guardArrowsHurtVillagers;
        public final ModConfigSpec.BooleanValue armorersRepairGuardArmor;
        public final ModConfigSpec.ConfigValue<List<String>> MobBlackList;
        public final ModConfigSpec.ConfigValue<List<String>> MobWhiteList;
        public final ModConfigSpec.BooleanValue giveGuardStuffHOTV;
        public final ModConfigSpec.BooleanValue setGuardPatrolHotv;
        public final ModConfigSpec.BooleanValue followHero;
        public final ModConfigSpec.IntValue reputationRequirement;
        public final ModConfigSpec.IntValue reputationRequirementToBeAttacked;
        public final ModConfigSpec.DoubleValue chanceToDropEquipment;

        public CommonConfig(ModConfigSpec.Builder builder) {
            builder.push("raids and illagers");
            RaidAnimals = builder.comment("Illagers In Raids Attack Animals?").translation(GuardVillagers.MODID + ".config.RaidAnimals").define("Illagers in raids attack animals?", true);
            WitchesVillager = builder.comment("Witches Attack Villagers?").translation(GuardVillagers.MODID + ".config.WitchesVillager").define("Witches attack villagers?", true);
            IllusionerRaids = builder.comment("This will make Illusioners get involved in raids").translation(GuardVillagers.MODID + ".config.IllusionerRaids").define("Have Illusioners in raids?", false);
            IllagersRunFromPolarBears = builder.comment("This makes Illagers run from polar bears, as anyone with common sense would.").translation(GuardVillagers.MODID + ".config.IllagersRunFromPolarBears").define("Have Illagers have some common sense?", true);
            builder.pop();
            builder.push("mob ai in general");
            AttackAllMobs = builder.comment("Guards will attack all hostiles with this option, when set to false guards will only attack zombies and illagers.").translation(GuardVillagers.MODID + ".config.AttackAllMobs").define("Guards attack all mobs?", true);
            MobsAttackGuards = builder.comment("Hostiles attack guards, by default only illagers and zombies will attack guards, the mob blacklist below will effect this option").define("All mobs attack guards", false);
            MobBlackList = builder.comment("Guards won't attack mobs in this list at all, for example, putting \"minecraft:creeper\" in this list will make guards ignore creepers.").define("Mob Blacklist", Lists.newArrayList("minecraft:villager", "minecraft:iron_golem", "minecraft:wandering_trader", "guardvillagers:guard", "minecraft:creeper", "alexsmobs:komodo_dragon"));
            MobWhiteList = builder.comment("Guards will additionally attack mobs ids put in this list, for example, putting \"minecraft:cow\" in this list will make guards attack cows.").define("Mob Whitelist", new ArrayList<>());
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
            chanceToDropEquipment = builder.defineInRange("Chance to drop equipment", 100.0F, -999.9F, 999.0F);
            GuardsRunFromPolarBears = builder.comment("This makes Guards run from polar bears, as anyone with common sense would.").translation(GuardVillagers.MODID + ".config.IllagersRunFromPolarBears").define("Have Guards have some common sense?", false);
            GuardsOpenDoors = builder.comment("This lets Guards open doors.").translation(GuardVillagers.MODID + ".config.GuardsOpenDoors").define("Have Guards open doors?", true);
            GuardRaiseShield = builder.comment("This will make guards raise their shields all the time, on default they will only raise their shields under certain conditions").translation(GuardVillagers.MODID + ".config.GuardRaiseShield").define("Have Guards raise their shield all the time?",
                    false);
            GuardFormation = builder.comment("This makes guards form a phalanx").translation(GuardVillagers.MODID + ".config.GuardFormation").define("Have guards form a phalanx?", true);
            FriendlyFire = builder.translation(GuardVillagers.MODID + ".config.FriendlyFire").define("Have guards attempt to avoid firing into other friendlies?", true);
            GuardVillagerHelpRange = builder.translation(GuardVillagers.MODID + ".config.range").comment("This is the range in which the guards will be aggroed to mobs that are attacking villagers. Higher values are more resource intensive, and setting this to zero will disable the goal.")
                    .defineInRange("Range", 50.0D, -500.0D, 500.0D);
            amountOfHealthRegenerated = builder.translation(GuardVillagers.MODID + ".config.amountofHealthRegenerated").comment("How much health a guard regenerates.").defineInRange("Guard health regeneration amount", 1.0D, -500.0D, 500.0D);
            guardArrowsHurtVillagers = builder.translation(GuardVillagers.MODID + ".config.guardArrows").define("Allow guard arrows to damage villagers, iron golems, or other guards? The i-frames will still be shown for them but they won't lose any health if this is set to false", true);
            giveGuardStuffHOTV = builder.translation(GuardVillagers.MODID + ".config.hotvArmor").define("Allow players to give guards stuff only if they have the hero of the village effect?", false);
            setGuardPatrolHotv = builder.translation(GuardVillagers.MODID + ".config.hotvPatrolPoint").define("Allow players to set guard patrol points only if they have hero of the village", false);
            reputationRequirement = builder.defineInRange("Minimum reputation requirement for guards to give you access to their inventories", 15, Integer.MIN_VALUE, Integer.MAX_VALUE);
            healthModifier = builder.defineInRange("Guard health", 20.0D, -500.0D, 900.0D);
            speedModifier = builder.defineInRange("Guard speed", 0.5D, -500.0D, 900.0D);
            followRangeModifier = builder.defineInRange("Guard follow range", 20.0D, 0.0D, 900.0D);
            followHero = builder.define("Have guards only follow the player if they have hero of the village?", true);
            reputationRequirementToBeAttacked = builder.defineInRange("How low of a reputation of a player should have to be instantly aggroed upon by guards and golems?", -100, -9999, 9999);
            builder.pop();
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