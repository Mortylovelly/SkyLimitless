package com.mortylovelly.skylimitless.mixin;

import com.mortylovelly.skylimitless.SkyLimitlessConfig;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
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

        if (!key.equals(DimensionTypes.OVERWORLD) || !(valueObject instanceof DimensionType original)) {
            return;
        }

        DimensionType adjusted = SkyLimitlessConfig.createAdjustedDimensionType(original);
        if (adjusted == original) {
            return;
        }

        args.set(1, adjusted);
    }
}
