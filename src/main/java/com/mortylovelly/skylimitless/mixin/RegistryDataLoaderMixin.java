package com.mortylovelly.skylimitless.mixin;

import com.mortylovelly.skylimitless.SkyLimitlessConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.OptionalLong;

@Mixin(RegistryDataLoader.class)
public abstract class RegistryDataLoaderMixin {
    private static final Logger SKY_LIMITLESS_LOGGER = LogUtils.getLogger();

    @ModifyArgs(
            method = "loadElementFromResource",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/WritableRegistry;register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;"
            )
    )
    private static void skylimitless$changeOverworldHeight(Args args) {
        Object keyObject = args.get(0);
        Object valueObject = args.get(1);

        if (!(keyObject instanceof ResourceKey<?> key)) {
            return;
        }

        if (!(valueObject instanceof DimensionType original)) {
            return;
        }

        if (!key.equals(BuiltinDimensionTypes.OVERWORLD)) {
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
                original.ultraWarm(),
                original.natural(),
                original.coordinateScale(),
                original.bedWorks(),
                original.respawnAnchorWorks(),
                original.minY(),
                height,
                logicalHeight,
                original.infiniburn(),
                original.effectsLocation(),
                original.ambientLight(),
                original.monsterSettings()
        );

        args.set(1, adjusted);
        SKY_LIMITLESS_LOGGER.info(
                "Expanded Overworld height: minY={}, height={}, logicalHeight={}, highestPlaceableY={}",
                adjusted.minY(),
                adjusted.height(),
                adjusted.logicalHeight(),
                adjusted.minY() + adjusted.height() - 1
        );
    }
}
