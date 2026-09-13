package com.mortylovelly.skylimitless;

import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.densityfunction.DensityFunction;

/**
 * Stretches the vanilla overworld terrain vertically above sea level without
 * changing the underlying horizontal noise or biome selection.
 */
public final class MountainHeightDensityFunction implements DensityFunction {
    private static final int SEA_LEVEL = 64;
    private static final int VANILLA_TOP_Y = 320;

    private final DensityFunction delegate;
    private final double verticalScale;

    public MountainHeightDensityFunction(DensityFunction delegate, double verticalScale) {
        this.delegate = delegate;
        this.verticalScale = verticalScale;
    }

    @Override
    public double sample(NoisePos pos) {
        int y = pos.blockY();
        if (y <= SEA_LEVEL || verticalScale <= 1.0D) {
            return delegate.sample(pos);
        }

        int transformedY = transformY(y);
        return delegate.sample(new ScaledNoisePos(pos, transformedY));
    }

    private int transformY(int y) {
        double transformed = SEA_LEVEL + (y - SEA_LEVEL) / verticalScale;
        return (int) Math.floor(transformed + 0.5D);
    }

    @Override
    public double minValue() {
        return delegate.minValue();
    }

    @Override
    public double maxValue() {
        return delegate.maxValue();
    }

    @Override
    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        return delegate.getCodecHolder();
    }

    @Override
    public void fill(double[] densities, EachApplier applier) {
        for (int i = 0; i < densities.length; i++) {
            densities[i] = sample(applier.at(i));
        }
    }

    @Override
    public DensityFunction apply(DensityFunctionVisitor visitor) {
        return new MountainHeightDensityFunction(delegate.apply(visitor), verticalScale);
    }

    public static double getScaleForMountainHeight(int mountainHeight) {
        if (mountainHeight <= VANILLA_TOP_Y) {
            return 1.0D;
        }
        return (double) (mountainHeight - SEA_LEVEL) / (VANILLA_TOP_Y - SEA_LEVEL);
    }

    private record ScaledNoisePos(NoisePos original, int blockY) implements NoisePos {
        @Override
        public int blockX() {
            return original.blockX();
        }

        @Override
        public int blockZ() {
            return original.blockZ();
        }

        @Override
        public Blender getBlender() {
            return original.getBlender();
        }
    }
}
