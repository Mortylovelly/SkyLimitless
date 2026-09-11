package com.mortylovelly.skylimitless;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkyLimitless implements ModInitializer {
    public static final String MOD_ID = "skylimitless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SkyLimitlessConfig.load();
        SkyLimitlessCommand.register();

        LOGGER.info(
                "SkyLimitless loaded: requested top Y={}, effective top Y={}, highest placeable Y={}",
                SkyLimitlessConfig.getRequestedTopY(),
                SkyLimitlessConfig.getEffectiveTopY(),
                SkyLimitlessConfig.getHighestPlaceableY()
        );
    }
}
