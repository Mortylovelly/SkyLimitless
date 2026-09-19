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
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        CommandManager.literal("skylimitless")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.literal("status")
                                        .executes(context -> status(context.getSource())))
                                .then(CommandManager.literal("height")
                                        .then(CommandManager.argument(
                                                        "top_y",
                                                        IntegerArgumentType.integer(
                                                                SkyLimitlessConfig.MIN_REQUESTED_TOP_Y,
                                                                SkyLimitlessConfig.MAX_REQUESTED_TOP_Y
                                                        )
                                                )
                                                .executes(context -> setHeight(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "top_y")
                                                )))
                                )
                )
        );
    }

    private static int status(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal(
                "SkyLimitless: top Y = " + SkyLimitlessConfig.getRequestedTopY()
                        + ", highest placeable Y = " + SkyLimitlessConfig.getHighestPlaceableY()
        ), false);
        return 1;
    }

    private static int setHeight(ServerCommandSource source, int topY) {
        int currentTopY = SkyLimitlessConfig.getRequestedTopY();

        if (topY < currentTopY) {
            source.sendError(Text.literal(
                    "SkyLimitless will not lower the world height from "
                            + currentTopY + " to " + topY + "."
            ));
            return 0;
        }

        if (!SkyLimitlessConfig.setRequestedTopY(topY)) {
            source.sendError(Text.literal(
                    "Invalid top Y. Allowed range: "
                            + SkyLimitlessConfig.MIN_REQUESTED_TOP_Y
                            + "-"
                            + SkyLimitlessConfig.MAX_REQUESTED_TOP_Y
                            + ". Minecraft 1.20.1 cannot safely use Y=5000 with min Y=-64."
            ));
            return 0;
        }

        source.sendFeedback(() -> Text.literal(
                "SkyLimitless height saved: top Y = " + topY
                        + ". Restart the world to apply it."
        ), false);
        return 1;
    }
}
