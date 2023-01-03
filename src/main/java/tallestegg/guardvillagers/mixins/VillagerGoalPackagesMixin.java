package tallestegg.guardvillagers.mixins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.entities.ai.tasks.ShareGossipWithGuard;

import java.util.ArrayList;
import java.util.List;

@Mixin(VillagerGoalPackages.class)
public abstract class VillagerGoalPackagesMixin {
    @Inject(method = "getMeetPackage", cancellable = true, at = @At("RETURN"))
    private static void getMeetPackage(VillagerProfession pProfession, float pSpeedModifier, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>>> cir) {
        List villagerList = new ArrayList<>(cir.getReturnValue());
        villagerList.add(Pair.of(2, new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, ImmutableList.of(Pair.of(new ShareGossipWithGuard(), 1)))));
        cir.setReturnValue(ImmutableList.copyOf(villagerList));
    }
    @Inject(method = "getIdlePackage", cancellable = true, at = @At("RETURN"))
    private static void getIdlePackage(VillagerProfession pProfession, float pSpeedModifier, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>>> cir) {
        List villagerList = new ArrayList<>(cir.getReturnValue());
        villagerList.add(Pair.of(2, new RunOne<>(ImmutableList.of(Pair.of(InteractWith.of(GuardEntityType.GUARD.get(), 8, MemoryModuleType.INTERACTION_TARGET, pSpeedModifier, 2), 3), Pair.of(new DoNothing(30, 60), 1)))));
        villagerList.add(Pair.of(2, new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, ImmutableList.of(Pair.of(new ShareGossipWithGuard(), 1)))));
        cir.setReturnValue(ImmutableList.copyOf(villagerList));
    }
}
