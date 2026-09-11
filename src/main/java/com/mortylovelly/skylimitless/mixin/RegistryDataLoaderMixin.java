package com.mortylovelly.skylimitless.mixin;

import com.mortylovelly.skylimitless.SkyLimitlessConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.dimension.DimensionType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DimensionType.class)
public abstract class RegistryDataLoaderMixin {
    private static final Logger SKY_LIMITLESS_LOGGER = LogUtils.getLogger();

    @Unique
    private static final ThreadLocal<Boolean> SKY_LIMITLESS_IS_OVERWORLD = new ThreadLocal<>();

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static int skylimitless$captureMinY(int minY) {
        SKY_LIMITLESS_IS_OVERWORLD.set(minY == SkyLimitlessConfig.MIN_WORLD_Y);
        return minY;
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private static int skylimitless$changeHeight(int height) {
        if (!Boolean.TRUE.equals(SKY_LIMITLESS_IS_OVERWORLD.get()) || height != 384) {
            return height;
        }

        int newHeight = SkyLimitlessConfig.getEffectiveHeight();
        if (newHeight <= height) {
            return height;
        }

        SKY_LIMITLESS_LOGGER.info(
                "Expanded Overworld DimensionType height: minY={}, height={}, highestPlaceableY={}",
                SkyLimitlessConfig.MIN_WORLD_Y,
                newHeight,
                SkyLimitlessConfig.getHighestPlaceableY()
        );
        return newHeight;
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private static int skylimitless$changeLogicalHeight(int logicalHeight) {
        if (!Boolean.TRUE.equals(SKY_LIMITLESS_IS_OVERWORLD.get()) || logicalHeight < 384) {
            SKY_LIMITLESS_IS_OVERWORLD.remove();
            return logicalHeight;
        }

        int newLogicalHeight = Math.max(logicalHeight, SkyLimitlessConfig.getEffectiveHeight());
        SKY_LIMITLESS_IS_OVERWORLD.remove();
        return newLogicalHeight;
    }
}
