package com.mortylovelly.skylimitless.mixin;

import com.mortylovelly.skylimitless.SkyLimitlessConfig;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DimensionType.class)
public abstract class RegistryLoaderMixin {
    @Shadow @Final @Mutable
    private int height;

    @Shadow @Final @Mutable
    private int logicalHeight;

    @Shadow @Final
    private int minY;

    @Shadow @Final
    private boolean hasSkyLight;

    @Shadow @Final
    private boolean hasCeiling;

    @Shadow @Final
    private boolean ultrawarm;

    @Shadow @Final
    private boolean natural;

    @Shadow @Final
    private double coordinateScale;

    @Shadow @Final
    private boolean bedWorks;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void skylimitless$changeOverworldHeight(CallbackInfo ci) {
        if (minY != -64 || height != 384 || !hasSkyLight || hasCeiling || ultrawarm || !natural || coordinateScale != 1.0D || !bedWorks) {
            return;
        }

        int effectiveHeight = SkyLimitlessConfig.getEffectiveHeight();
        if (effectiveHeight == 384) {
            return;
        }

        height = effectiveHeight;
        logicalHeight = Math.max(logicalHeight, effectiveHeight);
    }
}
