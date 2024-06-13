package tallestegg.guardvillagers.client;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistriesSetup;
import tallestegg.guardvillagers.GuardVillagers;

//@Mod.EventBusSubscriber(modid = GuardVillagers.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GuardSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, GuardVillagers.MODID);
    public static final Holder<SoundEvent> GUARD_AMBIENT = SOUNDS.register("entity.guard.ambient", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, "entity.guard.ambient")));
    public static final Holder<SoundEvent> GUARD_DEATH = SOUNDS.register("entity.guard.death", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, "entity.guard.death")));
    public static final Holder<SoundEvent> GUARD_HURT = SOUNDS.register("entity.guard.hurt", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(GuardVillagers.MODID, "entity.guard.hurt")));
}
