package tallestegg.guardvillagers.mixins;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestegg.guardvillagers.GuardEntityType;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.configuration.GuardConfig;
import net.minecraft.world.entity.EntitySpawnReason;

@Mixin(SinglePoolElement.class)
public abstract class SinglePoolElementMixin {
    @Shadow
    @Final
    protected Either<ResourceLocation, StructureTemplate> template;

    @Inject(at = @At(value = "RETURN"), method = "place", cancellable = true)
    public void place(StructureTemplateManager structureTemplateManager, WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, BlockPos offset, BlockPos pos, Rotation rotation, BoundingBox box, RandomSource random, LiquidSettings liquidSettings, boolean keepJigsaws, CallbackInfoReturnable<Boolean> cir) {
        this.template.left().ifPresent(resourceLocation -> {
            if (GuardConfig.COMMON.structuresThatSpawnGuards.get().contains(resourceLocation.toString())) {
                for (int guardCount = 0; guardCount < GuardConfig.COMMON.guardSpawnInVillage.getAsInt(); guardCount++) {
                    Guard guard = GuardEntityType.GUARD.get().create(level.getLevel(), EntitySpawnReason.EVENT);
                    guard.moveTo(offset, 0, 0);
                    guard.finalizeSpawn(level, level.getCurrentDifficultyAt(offset), EntitySpawnReason.STRUCTURE, null);
                    level.addFreshEntityWithPassengers(guard);
                }
            }
        });
    }
}
