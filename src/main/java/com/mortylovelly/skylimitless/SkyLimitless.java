package com.mortylovelly.skylimitless;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(SkyLimitless.MOD_ID)
public final class SkyLimitless {
    public static final String MOD_ID = "skylimitless";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SkyLimitless() {
        SkyLimitlessConfig.load();
        NeoForge.EVENT_BUS.addListener(SkyLimitlessCommand::register);

        LOGGER.info(
                "SkyLimitless NeoForge loaded: requested top Y={}, effective top Y={}, highest placeable Y={}",
                SkyLimitlessConfig.getRequestedTopY(),
                SkyLimitlessConfig.getEffectiveTopY(),
                SkyLimitlessConfig.getHighestPlaceableY()
        );
    }
}
