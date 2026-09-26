package com.jelly.farmhelperv2.feature.impl.pest;

import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.handler.RotationHandler;
import com.jelly.farmhelperv2.macro.AbstractMacro;
import com.jelly.farmhelperv2.macro.impl.AceWheatMacro;
import com.jelly.farmhelperv2.pathfinder.FlyPathFinderExecutor;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import com.jelly.farmhelperv2.util.helper.Rotation;
import com.jelly.farmhelperv2.util.helper.RotationConfiguration;
import net.minecraft.client.Minecraft;

public final class PestReturnManager {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean returning = false;
    private static State state = State.IDLE;
    private static final Clock stateClock = new Clock();
    private static float savedYaw = 0;
    private static float savedPitch = 0;

    public enum State {
        IDLE,
        WARP_GARDEN,
        WAIT_WARP,
        RESTORE_ROTATION,
        RESUME_MACRO
    }

    private PestReturnManager() {}

    public static boolean isReturning() {
        return returning;
    }

    public static void saveFarmingState() {
        if (mc.thePlayer != null) {
            savedYaw = mc.thePlayer.rotationYaw;
            savedPitch = mc.thePlayer.rotationPitch;
        }
    }

    public static void startReturn() {
        returning = true;
        state = State.WARP_GARDEN;
        stateClock.schedule(200);
        FlyPathFinderExecutor.getInstance().stop();
        KeyBindUtils.stopMovement();
        LogUtils.sendSuccess("[Pest] Slaying complete. Returning to farm via /warp garden...");
    }

    public static void reset() {
        returning = false;
        state = State.IDLE;
    }

    public static void onTick() {
        if (!returning || mc.thePlayer == null) return;
        if (!stateClock.passed()) return;

        switch (state) {
            case WARP_GARDEN:
                if (!com.jelly.farmhelperv2.feature.impl.pest.helpers.PestCommandScheduler.canSend()) {
                    stateClock.schedule(200);
                    return;
                }
                com.jelly.farmhelperv2.feature.impl.pest.helpers.PestCommandScheduler.send("/warp garden");
                state = State.WAIT_WARP;
                stateClock.schedule(2500);
                break;

            case WAIT_WARP:
                state = State.RESTORE_ROTATION;
                stateClock.schedule(200);
                break;

            case RESTORE_ROTATION:
                RotationHandler.getInstance().easeTo(new RotationConfiguration(
                        new Rotation(savedYaw, savedPitch),
                        300,
                        null
                ));
                state = State.RESUME_MACRO;
                stateClock.schedule(500);
                break;

            case RESUME_MACRO:
                returning = false;
                state = State.IDLE;
                PlayerUtils.getTool();
                LogUtils.sendSuccess("[Pest] Returned to farm. Resuming macro...");

                if (MacroHandler.getInstance().isMacroToggled()) {
                    MacroHandler.getInstance().getCurrentMacro().ifPresent(macro -> {
                        if (macro instanceof AceWheatMacro) {
                            ((AceWheatMacro) macro).changeState(AbstractMacro.State.WD);
                        }
                    });
                    MacroHandler.getInstance().resumeMacro();
                }
                break;
        }
    }
}
