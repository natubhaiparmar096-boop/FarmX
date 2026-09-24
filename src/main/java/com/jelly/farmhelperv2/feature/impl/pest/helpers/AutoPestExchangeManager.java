package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.util.helper.Clock;

public final class AutoPestExchangeManager {
    private static final Clock cooldown = new Clock();

    private AutoPestExchangeManager() {}

    public static void onTick() {
        if (!FarmHelperConfig.autoPestExchange || !GameStateHandler.getInstance().inGarden()) return;
        if (PestExchangeManager.isActive()) return;
        if (!cooldown.passed()) return;

        PestTabSnapshot tab = PestTabSnapshot.read();
        if (tab.getBonusInactive() != null && tab.getBonusInactive()) {
            PestExchangeManager.start();
            cooldown.schedule(60_000);
        }
    }
}
