package com.mortylovelly.skylimitless.mixin;

import com.mortylovelly.skylimitless.MountainTerrainScaler;
import com.mortylovelly.skylimitless.SkyLimitlessConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.chunk.Chunk;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseChunkGenerator.class)
public abstract class NoiseChunkGeneratorMixin {
    @Inject(
            method = "populateNoise(Ljava/util/concurrent/Executor;Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/chunk/Chunk;)Ljava/util/concurrent/CompletableFuture;",
            at = @At("RETURN")
    )
    private void skylimitless$scaleMountainTerrain(
            Executor executor,
            Blender blender,
            NoiseConfig noiseConfig,
            StructureAccessor structureAccessor,
            Chunk chunk,
            CallbackInfoReturnable<CompletableFuture<Chunk>> cir
    ) {
        if (SkyLimitlessConfig.getMountainHeight() <= SkyLimitlessConfig.VANILLA_TOP_Y) {
            return;
        }

        cir.setReturnValue(cir.getReturnValue().thenApply(MountainTerrainScaler::scaleAndReturn));
    }
}
