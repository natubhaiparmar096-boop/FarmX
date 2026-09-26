package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import net.minecraft.client.Minecraft;

/**
 * Centralized command scheduler for the pest system.
 * Enforces a minimum delay between consecutive chat commands
 * to avoid triggering Hypixel's anti-spam filter.
 */
public final class PestCommandScheduler {
    private static final Clock lastCommandClock = new Clock();
    private static final long MIN_GAP_MS = 1200;

    private PestCommandScheduler() {}

    /**
     * @return true if enough time has passed since the last command.
     */
    public static boolean canSend() {
        return !lastCommandClock.isScheduled() || lastCommandClock.passed();
    }

    /**
     * Sends a chat command if the cooldown has elapsed.
     *
     * @param command the command string (e.g. "/plottp 5")
     * @return true if the command was sent, false if blocked by cooldown
     */
    public static boolean send(String command) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return false;
        if (!canSend()) {
            LogUtils.sendDebug("[PestCmd] Command blocked (cooldown): " + command);
            return false;
        }
        mc.thePlayer.sendChatMessage(command);
        lastCommandClock.schedule(MIN_GAP_MS);
        LogUtils.sendDebug("[PestCmd] Sent: " + command);
        return true;
    }

    /**
     * @return milliseconds remaining before next command can be sent, or 0 if ready.
     */
    public static long getRemainingCooldownMs() {
        if (!lastCommandClock.isScheduled() || lastCommandClock.passed()) return 0;
        return lastCommandClock.getRemainingTime();
    }

    public static void reset() {
        lastCommandClock.reset();
    }
}
