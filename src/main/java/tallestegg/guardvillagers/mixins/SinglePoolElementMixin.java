package tallestegg.guardvillagers.mixins;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;

@Mixin(SinglePoolElement.class)
public abstract class SinglePoolElementMixin {
    @Shadow
    @Final
    protected Either<ResourceLocation, StructureTemplate> template;

    @Inject(at = @At(value = "RETURN"), method = "place", cancellable = true)
    public void place(StructureTemplateManager pStructureTemplateManager, WorldGenLevel pLevel, StructureManager pStructureManager, ChunkGenerator pGenerator, BlockPos pOffset, BlockPos pPos, Rotation pRotation, BoundingBox pBox, RandomSource pRandom, boolean pKeepJigsaws, CallbackInfoReturnable<Boolean> cir) {
        this.template.left().ifPresent(resourceLocation -> {
            if (GuardConfig.COMMON.structuresThatSpawnGuards.get().contains(resourceLocation.toString())) {
                for (int guardCount = 0; guardCount < GuardConfig.COMMON.guardSpawnInVillage.get(); guardCount++) {
                    Guard guard = GuardEntityType.GUARD.get().create(pLevel.getLevel());
                    guard.moveTo(pOffset, 0, 0);
                    guard.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(pOffset), MobSpawnType.STRUCTURE, null, null);
                    pLevel.addFreshEntityWithPassengers(guard);
                }
            }
        });
    }
}