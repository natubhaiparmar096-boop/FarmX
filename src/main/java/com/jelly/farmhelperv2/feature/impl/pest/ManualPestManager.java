package com.jelly.farmhelperv2.feature.impl.pest;

import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestTabSnapshot;
import com.jelly.farmhelperv2.util.LogUtils;

public final class ManualPestManager {
    private ManualPestManager() {}

    public static void triggerManualCleanup() {
        if (PestDestroyer.getInstance().isRunning() || PestLifecycleManager.getStage() != PestLifecycleManager.Stage.IDLE) {
            LogUtils.sendWarning("[Pest] Pest cleaning is already in progress.");
            return;
        }

        PestTabSnapshot tab = PestTabSnapshot.read();
        String initialPlot = tab.getInfestedPlots().isEmpty() ? null : tab.getInfestedPlots().iterator().next();
        LogUtils.sendSuccess("[Pest] Manually starting pest cleanup routine...");
        PestLifecycleManager.start(initialPlot);
    }

    public static void stop() {
        PestDestroyer.getInstance().stop();
        PestLifecycleManager.reset();
        PestReturnManager.reset();
        LogUtils.sendWarning("[Pest] Pest cleaning stopped manually.");
    }
}
