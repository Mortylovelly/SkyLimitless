package com.mortylovelly.skylimitless.mixin;

import com.mortylovelly.skylimitless.SkyLimitlessConfig;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(RegistryLoader.class)
public abstract class RegistryLoaderMixin {
    @ModifyArgs(
            method = "load(Lnet/minecraft/registry/RegistryOps$RegistryInfoGetter;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/registry/MutableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/registry/MutableRegistry;add(Lnet/minecraft/registry/RegistryKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/registry/entry/RegistryEntry$Reference;"
            )
    )
    private static void skylimitless$changeOverworldHeight(Args args) {
        Object keyObject = args.get(0);
        Object valueObject = args.get(1);

        if (!(keyObject instanceof RegistryKey<?> key)) {
            return;
        }

        if (key.equals(DimensionTypes.OVERWORLD) && valueObject instanceof DimensionType original) {
            int height = SkyLimitlessConfig.getEffectiveHeight();
            int logicalHeight = Math.max(original.logicalHeight(), height);

            if (original.height() == height && original.logicalHeight() == logicalHeight) {
                return;
            }

            DimensionType adjusted = new DimensionType(
                    original.fixedTime(),
                    original.hasSkyLight(),
                    original.hasCeiling(),
                    original.ultrawarm(),
                    original.natural(),
                    original.coordinateScale(),
                    original.bedWorks(),
                    original.respawnAnchorWorks(),
                    original.minY(),
                    height,
                    logicalHeight,
                    original.infiniburn(),
                    original.effects(),
                    original.ambientLight(),
                    original.monsterSettings()
            );

            args.set(1, adjusted);
            return;
        }

        if (!(valueObject instanceof ChunkGeneratorSettings originalSettings)) {
            return;
        }

        if (!key.equals(ChunkGeneratorSettings.OVERWORLD)
                && !key.equals(ChunkGeneratorSettings.LARGE_BIOMES)
                && !key.equals(ChunkGeneratorSettings.AMPLIFIED)) {
            return;
        }

        GenerationShapeConfig originalShape = originalSettings.generationShapeConfig();
        int targetTopY = SkyLimitlessConfig.getEffectiveTopY();
        int targetHeight = targetTopY - originalShape.minimumY();

        if (targetHeight <= originalShape.height()) {
            return;
        }

        GenerationShapeConfig adjustedShape = GenerationShapeConfig.create(
                originalShape.minimumY(),
                targetHeight,
                originalShape.horizontalSize(),
                originalShape.verticalSize()
        );

        ChunkGeneratorSettings adjustedSettings = new ChunkGeneratorSettings(
                adjustedShape,
                originalSettings.defaultBlock(),
                originalSettings.defaultFluid(),
                originalSettings.noiseRouter(),
                originalSettings.surfaceRule(),
                originalSettings.spawnTarget(),
                originalSettings.seaLevel(),
                originalSettings.mobGenerationDisabled(),
                originalSettings.hasAquifers(),
                originalSettings.oreVeins(),
                originalSettings.usesLegacyRandom()
        );

        args.set(1, adjustedSettings);
    }
}
