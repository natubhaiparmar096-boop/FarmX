package com.jelly.farmhelperv2.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.feature.impl.UsageStatsTracker;
import com.jelly.farmhelperv2.handler.GameStateHandler;

import java.util.List;

public class UsageStatsHUD extends TextHud {

    public UsageStatsHUD() {
        // No rounded background / border — plain text only
        super(true, 1f, 10f, 0.9f, false, false, 0, 2, 2, new OneColor(0, 0, 0, 0), false, 0, new OneColor(0, 0, 0, 0));
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        if (FarmHelperConfig.showStatsTitle) {
            lines.add("Usage");
        }
        if (FarmHelperConfig.showStats24H) {
            lines.add("24h: " + UsageStatsTracker.getInstance().getTodayString());
        }
        if (FarmHelperConfig.showStats7D) {
            lines.add("7d: " + UsageStatsTracker.getInstance().get7dString());
        }
        if (FarmHelperConfig.showStats30D) {
            lines.add("30d: " + UsageStatsTracker.getInstance().get30dString());
        }
        if (FarmHelperConfig.showStatsLifetime) {
            lines.add("Total: " + UsageStatsTracker.getInstance().getTotalString());
        }
        if (!FarmHelperConfig.showStatsTitle && !FarmHelperConfig.showStats24H && !FarmHelperConfig.showStats7D
                && !FarmHelperConfig.showStats30D && !FarmHelperConfig.showStatsLifetime) {
            lines.add("Enable stats in HUD config");
        }
    }

    @Override
    protected boolean shouldShow() {
        return super.shouldShow()
                && !FarmHelperConfig.streamerMode
                && GameStateHandler.getInstance().inGarden();
    }
}
