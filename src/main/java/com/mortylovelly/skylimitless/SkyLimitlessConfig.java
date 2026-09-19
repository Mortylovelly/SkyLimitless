package com.mortylovelly.skylimitless;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

public final class SkyLimitlessConfig {
    public static final int MIN_WORLD_Y = -64;
    public static final int VANILLA_TOP_Y = 320;
    public static final int MIN_REQUESTED_TOP_Y = VANILLA_TOP_Y;
    public static final int MAX_REQUESTED_TOP_Y = 10000;
    public static final int SECTION_SIZE = 16;

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("skylimitless.properties");

    private static int requestedTopY = 500;
    private static int effectiveTopY = 512;

    private SkyLimitlessConfig() {
    }

    public static void load() {
        Properties properties = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                properties.load(input);
                requestedTopY = parseRequestedTopY(properties.getProperty("requested_top_y"), 500);
            } catch (IOException exception) {
                SkyLimitless.LOGGER.warn("Could not read config {}; using safe default.", CONFIG_PATH, exception);
                requestedTopY = 500;
            }
        } else {
            requestedTopY = 5000;
        }

        effectiveTopY = roundTopUpToSection(requestedTopY);
        save();
    }

    public static boolean setRequestedTopY(int newTopY) {
        if (newTopY < MIN_REQUESTED_TOP_Y || newTopY > MAX_REQUESTED_TOP_Y) {
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
            properties.setProperty("requested_top_y", Integer.toString(requestedTopY));
            properties.setProperty("# Effective top Y is rounded upward to a 16-block section boundary", "");
            properties.setProperty("# Vanilla min Y is kept at -64 so existing terrain coordinates do not move", "");

            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "SkyLimitless world height configuration");
            }
        } catch (IOException exception) {
            SkyLimitless.LOGGER.error("Could not save config {}", CONFIG_PATH, exception);
        }
    }
}
