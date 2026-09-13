package com.mortylovelly.skylimitless.mixin;

import com.mortylovelly.skylimitless.MountainHeightDensityFunction;
import com.mortylovelly.skylimitless.SkyLimitlessConfig;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseConfig.class)
public abstract class NoiseConfigMixin {
    @Shadow
    @Final
    @Mutable
    private NoiseRouter noiseRouter;

    @Inject(
            method = "create(Lnet/minecraft/registry/RegistryEntryLookup$RegistryLookup;Lnet/minecraft/registry/RegistryKey;J)Lnet/minecraft/world/gen/noise/NoiseConfig;",
            at = @At("RETURN")
    )
    private static void skylimitless$scaleOverworldMountains(
            Object registryLookup,
            Object chunkGeneratorSettingsKey,
            long legacyWorldSeed,
            CallbackInfoReturnable<NoiseConfig> cir
    ) {
        if (!(chunkGeneratorSettingsKey instanceof net.minecraft.registry.RegistryKey<?> key)) {
            return;
        }

        if (!key.equals(ChunkGeneratorSettings.OVERWORLD)
                && !key.equals(ChunkGeneratorSettings.LARGE_BIOMES)
                && !key.equals(ChunkGeneratorSettings.AMPLIFIED)) {
            return;
        }

        NoiseConfig config = cir.getReturnValue();
        NoiseConfigMixin accessor = (NoiseConfigMixin) (Object) config;
        NoiseRouter router = accessor.noiseRouter;

        int mountainHeight = SkyLimitlessConfig.getMountainHeight();
        if (mountainHeight <= SkyLimitlessConfig.VANILLA_TOP_Y) {
            return;
        }

        double scale = MountainHeightDensityFunction.getScaleForMountainHeight(mountainHeight);
        DensityFunction finalDensity = new MountainHeightDensityFunction(router.finalDensity(), scale);

        accessor.noiseRouter = new NoiseRouter(
                router.barrierNoise(),
                router.fluidLevelFloodednessNoise(),
                router.fluidLevelSpreadNoise(),
                router.lavaNoise(),
                router.temperature(),
                router.vegetation(),
                router.continents(),
                router.erosion(),
                router.depth(),
                router.ridges(),
                router.initialDensityWithoutJaggedness(),
                finalDensity,
                router.veinToggle(),
                router.veinRidged(),
                router.veinGap()
        );
    }
}
