package com.jelly.farmhelperv2.feature.impl.pest;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.util.helper.Clock;

public final class DynamicPestsManager {
    private static final Clock updateClock = new Clock();

    private DynamicPestsManager() {}

    public static void onTick() {
        if (!GameStateHandler.getInstance().inGarden() || !MacroHandler.getInstance().isMacroToggled()) return;
        if (!updateClock.passed()) return;
        updateClock.schedule(5000);

        // Can dynamically adjust spray material or vinyl disk when Jacob contest changes
    }
}
