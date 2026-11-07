package com.mortylovelly.skylimitless.mixin;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor {
    @Mutable
    @Accessor("minY")
    void skylimitless$setMinY(int minY);

    @Mutable
    @Accessor("height")
    void skylimitless$setHeight(int height);

    @Mutable
    @Accessor("logicalHeight")
    void skylimitless$setLogicalHeight(int logicalHeight);
}
