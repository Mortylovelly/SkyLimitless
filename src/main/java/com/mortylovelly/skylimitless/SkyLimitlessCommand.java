package com.mortylovelly.skylimitless;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class SkyLimitlessCommand {
    private SkyLimitlessCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("skylimitless")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("status")
                                .executes(context -> status(context.getSource())))
                        .then(Commands.literal("height")
                                .then(Commands.argument("top_y", IntegerArgumentType.integer(
                                                SkyLimitlessConfig.MIN_REQUESTED_TOP_Y,
                                                SkyLimitlessConfig.MAX_REQUESTED_TOP_Y))
                                        .executes(context -> setHeight(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "top_y"))))
                                .executes(context -> status(context.getSource()))
        );
    }

    private static int status(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(
                "SkyLimitless: requested top Y = " + SkyLimitlessConfig.getRequestedTopY()
                        + ", effective top Y = " + SkyLimitlessConfig.getEffectiveTopY()
                        + ", highest placeable Y = " + SkyLimitlessConfig.getHighestPlaceableY()
                        + ", height = " + SkyLimitlessConfig.getEffectiveHeight()
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Active world height is fixed for this server session. Restart the server after changing it."
        ), false);
        return 1;
    }

    private static int setHeight(CommandSourceStack source, int topY) {
        if (!SkyLimitlessConfig.setRequestedTopY(topY)) {
            source.sendFailure(Component.literal(
                    "Invalid top Y. Allowed range: "
                            + SkyLimitlessConfig.MIN_REQUESTED_TOP_Y
                            + "-"
                            + SkyLimitlessConfig.MAX_REQUESTED_TOP_Y + "."
            ));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(
                "Saved SkyLimitless height: requested top Y = " + topY
                        + ", effective top Y after restart = " + SkyLimitlessConfig.getEffectiveTopY()
                        + " (highest placeable Y = " + SkyLimitlessConfig.getHighestPlaceableY() + ")."
        ), false);
        source.sendSuccess(() -> Component.literal(
                "Restart the server/world to apply the new height safely."
        ), false);
        return 1;
    }
}
