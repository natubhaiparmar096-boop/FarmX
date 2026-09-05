package com.jelly.farmhelperv2.hud;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.util.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class JacobsContestHUD {
    private static JacobsContestHUD instance;

    public static JacobsContestHUD getInstance() {
        if (instance == null) {
            instance = new JacobsContestHUD();
        }
        return instance;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (FarmHelperConfig.streamerMode || !FarmHelperConfig.showJacobsContestHud) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        List<String> lines = getContestHudLines();
        FontRenderer fr = mc.fontRendererObj;

        int x = 4;
        int y = mc.currentScreen != null ? 30 : 60; // Offset down so it doesn't overlap top status lines

        for (String line : lines) {
            fr.drawStringWithShadow(line, x, y, 0xFFFFFF);
            y += fr.FONT_HEIGHT + 2;
        }
    }

    public List<String> getContestHudLines() {
        List<String> lines = new ArrayList<>();
        lines.add("§e§lJacob's Contest Tracker");

        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(now);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        boolean isActive = (minute >= 15 && minute < 35) || GameStateHandler.getInstance().inJacobContest();

        if (isActive) {
            int secondsLeft;
            if (minute >= 15 && minute < 35) {
                secondsLeft = (35 - minute) * 60 - second;
            } else {
                secondsLeft = 1200; // fallback default 20m
            }
            if (secondsLeft < 0) secondsLeft = 0;

            lines.add("§fStatus: §aACTIVE §7(" + formatSeconds(secondsLeft) + " left)");

            FarmHelperConfig.CropEnum crop = GameStateHandler.getInstance().getJacobsContestCrop().orElse(MacroHandler.getInstance().getCrop());
            lines.add("§fCrop: §b" + (crop != null ? crop.getLocalizedName() : "Unknown"));

            int harvested = GameStateHandler.getInstance().getJacobsContestCropNumber();
            if (harvested > 0) {
                lines.add("§fHarvested: §a" + String.format("%,d", harvested));
            } else {
                lines.add("§fMacro: §a" + LogUtils.capitalize(MacroHandler.getInstance().getCrop().getLocalizedName()));
            }
        } else {
            int secondsUntilStart;
            if (minute < 15) {
                secondsUntilStart = (15 - minute) * 60 - second;
            } else {
                secondsUntilStart = (75 - minute) * 60 - second;
            }
            if (secondsUntilStart < 0) secondsUntilStart = 0;

            lines.add("§fStatus: §eUPCOMING §7(in " + formatSeconds(secondsUntilStart) + ")");
            lines.add("§fNext Contest: §b:15 Past the Hour");

            FarmHelperConfig.CropEnum targetCrop = MacroHandler.getInstance().getCrop();
            lines.add("§fTarget Crop: §b" + (targetCrop != null ? targetCrop.getLocalizedName() : "None"));
        }

        return lines;
    }

    private String formatSeconds(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%dm %02ds", minutes, seconds);
    }
}
