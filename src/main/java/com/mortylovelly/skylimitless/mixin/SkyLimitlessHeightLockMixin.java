package com.mortylovelly.skylimitless.mixin;

import com.mortylovelly.skylimitless.SkyLimitless;
import com.mortylovelly.skylimitless.SkyLimitlessConfig;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class SkyLimitlessHeightLockMixin {
    @Inject(method = "createWorlds", at = @At("HEAD"))
    private void skylimitless$lockOverworldHeight(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo callbackInfo) {
        MinecraftServer server = (MinecraftServer) (Object) this;

        try {
            DynamicRegistryManager.Immutable registryManager = server.getRegistryManager();
            Registry<DimensionOptions> dimensionOptionsRegistry = registryManager.get(RegistryKeys.DIMENSION);
            DimensionOptions overworldOptions = dimensionOptionsRegistry.get(DimensionOptions.OVERWORLD);

            if (overworldOptions == null) {
                SkyLimitless.LOGGER.warn("SkyLimitless could not locate the Overworld DimensionOptions while locking world height.");
                return;
            }

            RegistryEntry<DimensionType> dimensionTypeEntry = overworldOptions.dimensionTypeEntry();
            if (!(dimensionTypeEntry instanceof RegistryEntry.Reference<DimensionType> reference)) {
                SkyLimitless.LOGGER.warn(
                        "SkyLimitless could not lock the Overworld DimensionType because its registry entry is not a reference."
                );
                return;
            }

            DimensionType original = reference.value();
            DimensionType adjusted = SkyLimitlessConfig.createAdjustedDimensionType(original);

            if (adjusted == original) {
                return;
            }

            ((RegistryEntryReferenceAccessor<DimensionType>) (Object) reference).skylimitless$setValue(adjusted);

            SkyLimitless.LOGGER.info(
                    "SkyLimitless locked the Overworld world height at top Y={} (effective height={}), overriding the loaded DimensionType height of {}.",
                    SkyLimitlessConfig.getEffectiveTopY(),
                    SkyLimitlessConfig.getEffectiveHeight(),
                    original.height()
            );
        } catch (RuntimeException exception) {
            SkyLimitless.LOGGER.error(
                    "SkyLimitless failed to lock the Overworld world height. The world may use another dimension height.",
                    exception
            );
        }
    }
}
