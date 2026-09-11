package com.mortylovelly.skylimitless.mixin;

import com.mortylovelly.skylimitless.SkyLimitlessConfig;
import com.mojang.asm.mixin.injection.At;
import com.mojang.asm.mixin.injection.ModifyArgs;
import com.mojang.asm.mixin.injection.invoke.arg.Args;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistryLoader.class)
public abstract class RegistryLoaderMixin {
    @ModifyArgs(
            method = "parseAndAdd",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/registry/MutableRegistry;add(Lnet/minecraft/registry/RegistryKey;Ljava/lang/Object;Lnet/minecraft/registry/entry/RegistryEntryInfo;)Lnet/minecraft/registry/entry/RegistryEntry$Reference;"
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
    }
}
