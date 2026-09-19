package com.mortylovelly.skylimitless;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.dimension.DimensionType;

public final class SkyLimitlessConfig {
    public static final int MIN_WORLD_Y = -64;
    public static final int VANILLA_TOP_Y = 320;

    /*
     * Minecraft 1.20.1 cannot place the Overworld ceiling above Y=2031
     * while keeping the existing min Y of -64. Y=2032 is therefore the
     * highest valid top boundary (highest placeable block is Y=2031).
     */
    public static final int DEFAULT_REQUESTED_TOP_Y = 2000;
    public static final int MAX_REQUESTED_TOP_Y = 2032;
    public static final int MIN_REQUESTED_TOP_Y = VANILLA_TOP_Y;
    public static final int SECTION_SIZE = 16;

    private static final String REQUESTED_TOP_Y_PROPERTY = "requested_top_y";
    private static final String AUTHORIZED_TOP_Y_PROPERTY = "authorized_top_y";

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("skylimitless.properties");

    private static int requestedTopY = DEFAULT_REQUESTED_TOP_Y;
    private static int effectiveTopY = DEFAULT_REQUESTED_TOP_Y;

    private SkyLimitlessConfig() {
    }

    public static void load() {
        Properties properties = new Properties();

        if (Files.exists(CONFIG_PATH)) {
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                properties.load(input);

                int authorizedTopY = parseRequestedTopY(
                        properties.getProperty(AUTHORIZED_TOP_Y_PROPERTY),
                        DEFAULT_REQUESTED_TOP_Y
                );
                int requestedFromFile = parseRequestedTopY(
                        properties.getProperty(REQUESTED_TOP_Y_PROPERTY),
                        authorizedTopY
                );

                /*
                 * The command writes requested_top_y and authorized_top_y
                 * together. The authorized value is the source of truth.
                 * Invalid/old values above the 1.20.1 engine limit are ignored
                 * and safely restored to the default.
                 */
                if (requestedFromFile != authorizedTopY) {
                    requestedTopY = authorizedTopY;
                    SkyLimitless.LOGGER.warn(
                            "Detected an unauthorized SkyLimitless height change (requested_top_y={}). "
                                    + "Restoring the last command-authorized top Y={}.",
                            requestedFromFile,
                            authorizedTopY
                    );
                } else {
                    requestedTopY = authorizedTopY;
                }
            } catch (IOException exception) {
                SkyLimitless.LOGGER.warn(
                        "Could not read config {}; restoring safe default top Y={}.",
                        CONFIG_PATH,
                        DEFAULT_REQUESTED_TOP_Y,
                        exception
                );
                requestedTopY = DEFAULT_REQUESTED_TOP_Y;
            }
        } else {
            requestedTopY = DEFAULT_REQUESTED_TOP_Y;
        }

        effectiveTopY = roundTopUpToSection(requestedTopY);
        save();
    }

    public static boolean setRequestedTopY(int newTopY) {
        if (newTopY < MIN_REQUESTED_TOP_Y || newTopY > MAX_REQUESTED_TOP_Y) {
            return false;
        }

        /*
         * Lowering an existing world height can hide already generated terrain,
         * so the manual command only allows increases.
         */
        if (newTopY < requestedTopY) {
            return false;
        }

        requestedTopY = newTopY;
        effectiveTopY = roundTopUpToSection(newTopY);
        save();
        return true;
    }

    public static int getRequestedTopY() {
        return requestedTopY;
    }

    public static int getEffectiveTopY() {
        return effectiveTopY;
    }

    public static int getEffectiveHeight() {
        return effectiveTopY - MIN_WORLD_Y;
    }

    public static int getHighestPlaceableY() {
        return effectiveTopY - 1;
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }

    public static DimensionType createAdjustedDimensionType(DimensionType original) {
        int height = getEffectiveHeight();
        int logicalHeight = height;

        if (original.height() == height && original.logicalHeight() == logicalHeight) {
            return original;
        }

        return new DimensionType(
                original.fixedTime(),
                original.hasSkyLight(),
                original.hasCeiling(),
                original.ultrawarm(),
                original.natural(),
                original.coordinateScale(),
                original.bedWorks(),
                original.respawnAnchorWorks(),
                original.minY(),
                height,
                logicalHeight,
                original.infiniburn(),
                original.effects(),
                original.ambientLight(),
                original.monsterSettings()
        );
    }

    private static int parseRequestedTopY(String value, int fallback) {
        if (value == null) {
            return fallback;
        }

        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < MIN_REQUESTED_TOP_Y || parsed > MAX_REQUESTED_TOP_Y) {
                return fallback;
            }
            return parsed;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static int roundTopUpToSection(int topY) {
        int relativeHeight = topY - MIN_WORLD_Y;
        int sections = (relativeHeight + SECTION_SIZE - 1) / SECTION_SIZE;
        return MIN_WORLD_Y + sections * SECTION_SIZE;
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            Properties properties = new Properties();
            properties.setProperty(REQUESTED_TOP_Y_PROPERTY, Integer.toString(requestedTopY));
            properties.setProperty(AUTHORIZED_TOP_Y_PROPERTY, Integer.toString(requestedTopY));
            properties.setProperty(
                    "# Maximum valid top Y for Minecraft 1.20.1 with min Y -64",
                    "2032"
            );
            properties.setProperty(
                    "# Highest placeable block at the maximum is Y=2031",
                    ""
            );
            properties.setProperty(
                    "# Height is command-authorized; other worldgen configs cannot override it",
                    ""
            );
            properties.setProperty(
                    "# Effective top Y is rounded upward to a 16-block section boundary",
                    ""
            );
            properties.setProperty(
                    "# Vanilla min Y is kept at -64 so existing terrain coordinates do not move",
                    ""
            );

            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "SkyLimitless protected world height configuration");
            }
        } catch (IOException exception) {
            SkyLimitless.LOGGER.error("Could not save config {}", CONFIG_PATH, exception);
        }
    }
}
