package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.RotationHandler;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import com.jelly.farmhelperv2.util.helper.Rotation;
import com.jelly.farmhelperv2.util.helper.RotationConfiguration;
import net.minecraft.client.Minecraft;

public final class PestBallsackShredder {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Clock stateClock = new Clock();
    private static State state = State.IDLE;
    private static int warpsDone = 0;

    public enum State {
        IDLE,
        WARPING_UP,
        LOOK_DOWN,
        VACUUMING,
        FINISHED
    }

    private PestBallsackShredder() {}

    public static boolean isConfiguredForPlot(String plot) {
        if (!FarmHelperConfig.pestBallsackShredder) return false;
        String normalized = PestPlotId.normalize(plot);
        if (normalized.isEmpty()) return false;
        String configuredPlots = FarmHelperConfig.pestBallsackPlots;
        if (configuredPlots == null || configuredPlots.trim().isEmpty() || configuredPlots.equals("*")) {
            return true;
        }
        for (String part : configuredPlots.split(",")) {
            if (PestPlotId.normalize(part).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    public static void start() {
        state = State.WARPING_UP;
        warpsDone = 0;
        stateClock.schedule(200);
        LogUtils.sendDebug("[Pest] Starting Ballsack Shredder route...");
    }

    public static State getState() {
        return state;
    }

    public static boolean isFinished() {
        return state == State.FINISHED || state == State.IDLE;
    }

    public static void reset() {
        state = State.IDLE;
        warpsDone = 0;
    }

    public static void onTick() {
        if (state == State.IDLE || state == State.FINISHED || mc.thePlayer == null) return;
        if (!stateClock.passed()) return;

        switch (state) {
            case WARPING_UP:
                int aotvSlot = PestLoadoutHelper.findAotvSlot();
                if (aotvSlot < 0 || warpsDone >= FarmHelperConfig.pestBallsackWarps) {
                    state = State.LOOK_DOWN;
                    stateClock.schedule(150);
                    return;
                }
                PestLoadoutHelper.equipSlot(aotvSlot);
                mc.thePlayer.rotationPitch = -90.0f; // Look straight up
                KeyBindUtils.rightClick();
                warpsDone++;
                stateClock.schedule(250);
                break;

            case LOOK_DOWN:
                RotationHandler.getInstance().easeTo(new RotationConfiguration(
                        new Rotation(mc.thePlayer.rotationYaw, 90.0f),
                        200,
                        null
                ));
                int vacSlot = PestLoadoutHelper.findVacuumSlot();
                if (vacSlot >= 0) {
                    PestLoadoutHelper.equipSlot(vacSlot);
                }
                state = State.VACUUMING;
                stateClock.schedule(FarmHelperConfig.pestBallsackVacuumDurationMs);
                PestCombatCoordinator.startVacuum();
                break;

            case VACUUMING:
                PestCombatCoordinator.stopVacuum();
                state = State.FINISHED;
                LogUtils.sendDebug("[Pest] Ballsack Shredder complete.");
                break;
        }
    }
}
