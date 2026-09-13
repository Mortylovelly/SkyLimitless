package com.mortylovelly.skylimitless;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class SkyLimitlessCommand {
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
                "Active world height is fixed for this server session. Restart the server after changing it."
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
                "Saved SkyLimitless height: requested top Y = " + topY
                        + ", effective top Y after restart = " + SkyLimitlessConfig.getEffectiveTopY()
                        + " (highest placeable Y = " + SkyLimitlessConfig.getHighestPlaceableY() + ")."
        ), false);
        source.sendFeedback(() -> Text.literal(
                "Restart the server/world to apply the new height safely. Do not change it while the world is running."
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
}
