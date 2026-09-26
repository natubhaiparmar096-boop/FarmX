package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.BaritoneHandler;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.util.GardenPlots;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

public final class PestTrapManager {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean running = false;
    private static final Clock clock = new Clock();

    private PestTrapManager() {}

    public static boolean isRunning() {
        return running;
    }

    public static void start() {
        if (running || mc.thePlayer == null) return;
        running = true;
        LogUtils.sendSuccess("[Pest] Starting Pest Traps sequence...");

        String plot = FarmHelperConfig.pestTrapsPlot;
        GardenPlots.Bounds bounds = GardenPlots.boundsForPlot(plot);
        if (bounds != null) {
            PestCommandScheduler.send("/plottp " + PestPlotId.normalize(plot));
        }
        clock.schedule(3000);
    }

    public static void stop() {
        running = false;
        BaritoneHandler.stopPathing();
    }

    public static void onTick() {
        if (!running || mc.thePlayer == null) return;
        if (!clock.passed()) return;

        // Sequence complete after visiting trap plot
        LogUtils.sendSuccess("[Pest] Pest traps checked.");
        stop();
    }
}
