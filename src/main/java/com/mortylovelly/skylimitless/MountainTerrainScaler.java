package com.mortylovelly.skylimitless;

import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;

public final class MountainTerrainScaler {
    private static final int SEA_LEVEL = 63;
    private static final int VANILLA_MOUNTAIN_REFERENCE_Y = 300;

    private MountainTerrainScaler() {
    }

    public static Chunk scaleAndReturn(Chunk chunk) {
        scale(chunk);
        return chunk;
    }

    public static void scale(Chunk chunk) {
        // Disabled intentionally. The implementation remains in the project,
        // but mountain generation must stay completely vanilla for now.
        return;

        /*
        int targetHeight = SkyLimitlessConfig.getMountainHeight();
        if (targetHeight <= SkyLimitlessConfig.VANILLA_TOP_Y) {
            return;
        }

        int topY = chunk.getTopY();
        if (topY <= SkyLimitlessConfig.VANILLA_TOP_Y) {
            return;
        }

        double scale = (double) (targetHeight - SEA_LEVEL)
                / (double) (VANILLA_MOUNTAIN_REFERENCE_Y - SEA_LEVEL);
        if (scale <= 1.0D) {
            return;
        }

        int bottomY = Math.max(SEA_LEVEL + 1, chunk.getBottomY());
        int chunkStartX = chunk.getPos().getStartX();
        int chunkStartZ = chunk.getPos().getStartZ();

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                if (!chunk.getBiomeForNoiseGen(localX >> 2, 0, localZ >> 2).isIn(BiomeTags.IS_MOUNTAIN)) {
                    continue;
                }

                int originalTopY = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, localX, localZ) - 1;
                if (originalTopY <= SEA_LEVEL + 16 || originalTopY >= SkyLimitlessConfig.VANILLA_TOP_Y) {
                    continue;
                }

                int stretchedTopY = SEA_LEVEL + (int) Math.round((originalTopY - SEA_LEVEL) * scale);
                stretchedTopY = Math.min(stretchedTopY, topY - 1);

                if (stretchedTopY <= originalTopY) {
                    continue;
                }

                int sourceHeight = originalTopY - bottomY + 1;
                if (sourceHeight <= 0) {
                    continue;
                }

                BlockState[] sourceStates = new BlockState[sourceHeight];
                BlockPos.Mutable sourcePos = new BlockPos.Mutable(chunkStartX + localX, bottomY, chunkStartZ + localZ);
                for (int index = 0; index < sourceHeight; index++) {
                    sourcePos.setY(bottomY + index);
                    sourceStates[index] = chunk.getBlockState(sourcePos);
                }

                BlockPos.Mutable targetPos = new BlockPos.Mutable(chunkStartX + localX, SEA_LEVEL + 1, chunkStartZ + localZ);
                for (int y = SEA_LEVEL + 1; y <= stretchedTopY; y++) {
                    double sourceY = SEA_LEVEL + ((double) (y - SEA_LEVEL) / scale);
                    int sourceIndex = (int) Math.floor(sourceY - bottomY + 0.5D);
                    if (sourceIndex < 0 || sourceIndex >= sourceStates.length) {
                        continue;
                    }

                    BlockState state = sourceStates[sourceIndex];
                    targetPos.setY(y);
                    chunk.setBlockState(targetPos, state, false);
                }
            }
        }

        chunk.refreshSurfaceY();
        */
    }
}
