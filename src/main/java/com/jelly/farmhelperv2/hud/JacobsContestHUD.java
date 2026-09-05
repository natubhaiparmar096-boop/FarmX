package com.jelly.farmhelperv2.hud;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.config.FarmHelperConfig.CropEnum;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.util.JacobContestSchedule;
import com.jelly.farmhelperv2.util.SkyBlockCalendar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JacobsContestHUD {

    private static JacobsContestHUD instance;

    public static JacobsContestHUD getInstance() {
        if (instance == null) instance = new JacobsContestHUD();
        return instance;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (FarmHelperConfig.streamerMode || !FarmHelperConfig.showJacobsContestHud) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        List<String> lines = buildHudLines();
        FontRenderer fr = mc.fontRendererObj;

        int x = 4;
        int y = 60;
        for (String line : lines) {
            fr.drawStringWithShadow(line, x, y, 0xFFFFFF);
            y += fr.FONT_HEIGHT + 2;
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // HUD content
    // ─────────────────────────────────────────────────────────────────

    public List<String> buildHudLines() {
        List<String> lines = new ArrayList<>();
        lines.add("§e§lJacob's Contest Tracker");

        // SkyBlock date (always shown, useful reference)
        lines.add("§7" + SkyBlockCalendar.getDateString());

        boolean calendarActive  = SkyBlockCalendar.isContestActive();
        boolean scoreboardActive = GameStateHandler.getInstance().inJacobContest();
        boolean active = calendarActive || scoreboardActive;

        if (active) {
            buildActiveLines(lines, calendarActive);
        } else {
            buildUpcomingLines(lines);
        }

        return lines;
    }

    // ── Active contest ────────────────────────────────────────────────

    private void buildActiveLines(List<String> lines, boolean calendarActive) {
        long msLeft = SkyBlockCalendar.msLeftInContest();
        String timeLeft = msLeft > 0 ? fmt(msLeft) : "??:??";
        lines.add("§fStatus: §aACTIVE §7(" + timeLeft + " left)");

        // Crops: prefer confirmed scoreboard crop, fall back to calendar prediction
        Optional<CropEnum> confirmedCrop = GameStateHandler.getInstance().getJacobsContestCrop();
        if (confirmedCrop.isPresent()) {
            // Scoreboard confirmed
            lines.add("§fCrop: §b" + confirmedCrop.get().getLocalizedName() + " §7(confirmed)");
        } else {
            // Calendar prediction
            List<CropEnum> predicted = JacobContestSchedule.getCurrentSlotCrops();
            lines.add("§fCrops: " + JacobContestSchedule.formatCropList(predicted) + " §7(predicted)");
        }

        // Harvested count if available
        int harvested = GameStateHandler.getInstance().getJacobsContestCropNumber();
        if (harvested > 0) {
            lines.add("§fHarvested: §a" + String.format("%,d", harvested));
        }
    }

    // ── Upcoming contest ──────────────────────────────────────────────

    private void buildUpcomingLines(List<String> lines) {
        long msUntil = SkyBlockCalendar.msUntilNextContest();
        lines.add("§fStatus: §eUPCOMING §7(in " + fmt(msUntil) + ")");

        // Calendar-predicted crops for the next slot
        List<CropEnum> next = JacobContestSchedule.getNextContestCrops();
        lines.add("§fPredicted Crops: " + JacobContestSchedule.formatCropList(next));
        lines.add("§7(confirmed when contest starts)");

        // Your current macro target for reference
        CropEnum myTarget = MacroHandler.getInstance().getCrop();
        if (myTarget != null && myTarget != CropEnum.NONE) {
            boolean match = next.contains(myTarget);
            String icon = match ? "§a✔" : "§c✘";
            lines.add("§fYour Crop: §b" + myTarget.getLocalizedName() + " " + icon);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

    /** Formats milliseconds into m:ss */
    private String fmt(long ms) {
        if (ms < 0) ms = 0;
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return min + "m " + String.format("%02d", sec) + "s";
    }
}
