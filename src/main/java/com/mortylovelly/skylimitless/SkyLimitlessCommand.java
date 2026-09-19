package com.mortylovelly.skylimitless;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

public final class SkyLimitlessCommand {
    private static final int MOUNTAIN_SEARCH_RADIUS_BLOCKS = 128;
    private static final int MOUNTAIN_SAMPLE_STEP = 8;

    private SkyLimitlessCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                CommandManager.literal("skylimitless")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("status")
                                .executes(context -> status(context.getSource())))
                        .then(CommandManager.literal("height")
                                .then(CommandManager.argument("top_y", IntegerArgumentType.integer(
                                                SkyLimitlessConfig.MIN_REQUESTED_TOP_Y,
                                                SkyLimitlessConfig.MAX_REQUESTED_TOP_Y))
                                        .executes(context -> setHeight(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "top_y"))))
                                .executes(context -> status(context.getSource())))
                        .then(CommandManager.literal("mountains")
                                .executes(context -> mountainStatus(context.getSource()))
                                .then(CommandManager.argument("height", IntegerArgumentType.integer(
                                                SkyLimitlessConfig.MIN_MOUNTAIN_HEIGHT,
                                                SkyLimitlessConfig.MAX_MOUNTAIN_HEIGHT))
                                        .executes(context -> setMountainHeight(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "height")))))
                        .then(CommandManager.literal("findmountain")
                                .then(CommandManager.argument("height", IntegerArgumentType.integer(
                                                SkyLimitlessConfig.MIN_MOUNTAIN_HEIGHT,
                                                SkyLimitlessConfig.MAX_MOUNTAIN_HEIGHT))
                                        .executes(context -> findMountain(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "height")))))
        ));
    }

    private static int status(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal(
                "SkyLimitless: requested top Y = " + SkyLimitlessConfig.getRequestedTopY()
                        + ", effective top Y = " + SkyLimitlessConfig.getEffectiveTopY()
                        + ", highest placeable Y = " + SkyLimitlessConfig.getHighestPlaceableY()
                        + ", height = " + SkyLimitlessConfig.getEffectiveHeight()
        ), false);
        source.sendFeedback(() -> Text.literal(
                "Protected world height: it cannot be changed by Tectonic or other worldgen configs. "
                        + "Only /skylimitless height <Y> can change it, and only upward for safety."
        ), false);
        source.sendFeedback(() -> Text.literal(
                "Hard maximum top Y = " + SkyLimitlessConfig.MAX_REQUESTED_TOP_Y
                        + ". Restart the server after changing the height."
        ), false);
        return 1;
    }

    private static int mountainStatus(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal(
                "SkyLimitless mountain height: " + SkyLimitlessConfig.getMountainHeight()
                        + ", world top Y = " + SkyLimitlessConfig.getEffectiveTopY()
        ), false);
        source.sendFeedback(() -> Text.literal(
                "Mountain height changes apply only to newly generated Overworld chunks."
        ), false);
        return 1;
    }

    private static int setHeight(ServerCommandSource source, int topY) {
        int currentTopY = SkyLimitlessConfig.getRequestedTopY();

        if (topY < currentTopY) {
            source.sendError(Text.literal(
                    "SkyLimitless will not lower the world height from "
                            + currentTopY + " to " + topY + " because that can hide or damage existing terrain above the new ceiling. "
                            + "The normal height command can only increase the limit."
            ));
            return 0;
        }

        if (topY < SkyLimitlessConfig.getMountainHeight()) {
            source.sendError(Text.literal(
                    "World top Y cannot be lower than the current mountain height ("
                            + SkyLimitlessConfig.getMountainHeight() + ")."
            ));
            return 0;
        }

        if (!SkyLimitlessConfig.setRequestedTopY(topY)) {
            source.sendError(Text.literal(
                    "Invalid top Y. Allowed range: "
                            + SkyLimitlessConfig.MIN_REQUESTED_TOP_Y
                            + "-"
                            + SkyLimitlessConfig.MAX_REQUESTED_TOP_Y + "."
            ));
            return 0;
        }

        source.sendFeedback(() -> Text.literal(
                "Saved protected SkyLimitless height: requested top Y = " + topY
                        + ", effective top Y after restart = " + SkyLimitlessConfig.getEffectiveTopY()
                        + " (highest placeable Y = " + SkyLimitlessConfig.getHighestPlaceableY() + ")."
        ), false);
        source.sendFeedback(() -> Text.literal(
                "Tectonic's max_y cannot overwrite this world limit. Restart the server/world to apply the new height safely."
        ), false);
        return 1;
    }

    private static int setMountainHeight(ServerCommandSource source, int height) {
        if (height > SkyLimitlessConfig.getEffectiveTopY()) {
            source.sendError(Text.literal(
                    "Cannot set mountain height to " + height + ": it is above the current world height limit ("
                            + SkyLimitlessConfig.getEffectiveTopY() + ")."
            ));
            return 0;
        }

        if (!SkyLimitlessConfig.setMountainHeight(height)) {
            source.sendError(Text.literal(
                    "Invalid mountain height. Allowed range: "
                            + SkyLimitlessConfig.MIN_MOUNTAIN_HEIGHT
                            + "-"
                            + SkyLimitlessConfig.MAX_MOUNTAIN_HEIGHT + "."
            ));
            return 0;
        }

        source.sendFeedback(() -> Text.literal(
                "Saved SkyLimitless mountain height: " + height
                        + ". It will affect newly generated Overworld chunks after the next world restart."
        ), false);
        return 1;
    }

    private static int findMountain(ServerCommandSource source, int minimumHeight) throws CommandSyntaxException {
        if (source.getWorld().getRegistryKey() != World.OVERWORLD) {
            source.sendError(Text.literal("High mountain search is available only in the Overworld."));
            return 0;
        }

        ServerWorld world = source.getWorld();
        BlockPos origin = BlockPos.ofFloored(source.getPosition());
        Pair<BlockPos, net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.biome.Biome>> located =
                world.locateBiome(
                        entry -> entry.isIn(BiomeTags.IS_MOUNTAIN),
                        origin,
                        4096,
                        32,
                        32
                );

        if (located == null) {
            source.sendError(Text.literal(
                    "No mountain biome could be found within 4096 blocks."
            ));
            return 0;
        }

        BlockPos center = located.getFirst();
        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;
        int radiusChunks = MOUNTAIN_SEARCH_RADIUS_BLOCKS >> 4;
        int bestY = Integer.MIN_VALUE;
        int bestX = center.getX();
        int bestZ = center.getZ();

        for (int chunkX = centerChunkX - radiusChunks; chunkX <= centerChunkX + radiusChunks; chunkX++) {
            for (int chunkZ = centerChunkZ - radiusChunks; chunkZ <= centerChunkZ + radiusChunks; chunkZ++) {
                Chunk chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
                for (int localX = 0; localX < 16; localX += MOUNTAIN_SAMPLE_STEP) {
                    for (int localZ = 0; localZ < 16; localZ += MOUNTAIN_SAMPLE_STEP) {
                        int x = (chunkX << 4) + localX;
                        int z = (chunkZ << 4) + localZ;
                        int y = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, localX, localZ);
                        if (y > bestY) {
                            bestY = y;
                            bestX = x;
                            bestZ = z;
                        }
                    }
                }
            }
        }

        if (bestY < minimumHeight) {
            source.sendError(Text.literal(
                    "No mountain at least " + minimumHeight + " blocks high was found near the located mountain biome. Highest terrain found: "
                            + bestY + " Y. Try a new area or generate farther away."
            ));
            return 0;
        }

        final int foundY = bestY;
        final int foundX = bestX;
        final int foundZ = bestZ;
        int teleportY = Math.min(foundY + 2, SkyLimitlessConfig.getHighestPlaceableY() - 1);
        source.sendFeedback(() -> Text.literal(
                "Found a mountain with terrain height " + foundY + " Y at " + foundX + ", " + foundZ + ". Teleporting there."
        ), false);
        source.getPlayerOrThrow().teleport(
                world,
                foundX + 0.5D,
                teleportY,
                foundZ + 0.5D,
                source.getPlayerOrThrow().getYaw(),
                source.getPlayerOrThrow().getPitch()
        );
        return 1;
    }
}
