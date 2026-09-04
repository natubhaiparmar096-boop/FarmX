package com.jelly.farmhelperv2.util;

import cc.polyfrost.oneconfig.utils.Notifications;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.MacroHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.util.concurrent.TimeUnit;

public class LogUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static String lastDebugMessage;

    public synchronized static void sendLog(ChatComponentText chat) {
        if (mc.thePlayer != null && !FarmHelperConfig.streamerMode)
            mc.thePlayer.addChatMessage(chat);
        else if (mc.thePlayer == null)
            System.out.println("[Farm Helper] " + chat.getUnformattedText());
    }

    public static void sendSuccess(String message) {
        sendLog(new ChatComponentText("§2§lFarm Helper §8» §a" + message));
    }

    public static void sendWarning(String message) {
        sendLog(new ChatComponentText("§6§lFarm Helper §8» §e" + message));
    }

    public static void sendError(String message) {
        sendLog(new ChatComponentText("§4§lFarm Helper §8» §c" + message));
    }

    public static void sendDebug(String message) {
        if (lastDebugMessage != null && lastDebugMessage.equals(message)) {
            return;
        }
        if (FarmHelperConfig.debugMode && mc.thePlayer != null)
            sendLog(new ChatComponentText("§3§lFarm Helper §8» §7" + message));
        else
            System.out.println("[Farm Helper] " + message);
        lastDebugMessage = message;
    }

    public static void sendNotification(String title, String message, float duration) {
        if (FarmHelperConfig.streamerMode) {
            return;
        }
        if (PlatformUtils.isMobile()) {
            sendWarning(title + ": " + message);
            return;
        }
        try {
            Notifications.INSTANCE.send(title, message, duration);
        } catch (Throwable t) {
            System.out.println("[Farm Helper] " + title + ": " + message);
        }
    }

    public static void sendNotification(String title, String message) {
        if (FarmHelperConfig.streamerMode) {
            return;
        }
        if (PlatformUtils.isMobile()) {
            sendWarning(title + ": " + message);
            return;
        }
        try {
            Notifications.INSTANCE.send(title, message);
        } catch (Throwable t) {
            System.out.println("[Farm Helper] " + title + ": " + message);
        }
    }

    public static void sendFailsafeMessage(String message) {
        sendLog(new ChatComponentText("§5§lFarm Helper §8» §d" + message));
    }

    public static void sendFailsafeMessage(String message, boolean ignored) {
        sendFailsafeMessage(message);
    }

    public static String getRuntimeFormat() {
        if (!MacroHandler.getInstance().getMacroingTimer().isScheduled())
            return "0h 0m 0s";
        long millis = MacroHandler.getInstance().getMacroingTimer().getElapsedTime();
        return formatTime(millis) + (MacroHandler.getInstance().getMacroingTimer().paused ? " (Paused)" : "");
    }

    public static String formatTime(long millis) {

        if (TimeUnit.MILLISECONDS.toHours(millis) > 0) {
            return String.format("%dh %dm %ds",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            );
        } else if (TimeUnit.MILLISECONDS.toMinutes(millis) > 0) {
            return String.format("%dm %ds",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            );
        } else {
            return TimeUnit.MILLISECONDS.toSeconds(millis) + "." + (TimeUnit.MILLISECONDS.toMillis(millis) -
                    TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis))) / 100 + "s";
        }
    }

    public static String capitalize(String message) {
        String[] words = message.split("_|\\s");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase()).append(" ");
        }
        return sb.toString().trim();
    }
}
