package com.jelly.farmhelperv2.hud;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.feature.impl.pest.PestDestroyer;
import com.jelly.farmhelperv2.feature.impl.pest.PestLifecycleManager;
import com.jelly.farmhelperv2.feature.impl.pest.PestManager;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestTabSnapshot;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PestHUD {
    private static PestHUD instance;

    public static PestHUD getInstance() {
        if (instance == null) {
            instance = new PestHUD();
        }
        return instance;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (FarmHelperConfig.streamerMode || !FarmHelperConfig.pestShowHud) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null || !GameStateHandler.getInstance().inGarden()) return;

        List<String> lines = buildHudLines();
        FontRenderer fr = mc.fontRendererObj;

        int x = 4;
        int y = 130;
        for (String line : lines) {
            fr.drawStringWithShadow(line, x, y, 0xFFFFFF);
            y += fr.FONT_HEIGHT + 2;
        }
    }

    public List<String> buildHudLines() {
        List<String> lines = new ArrayList<>();
        lines.add("§a§lPest Tracker");

        PestTabSnapshot tab = PestTabSnapshot.read();
        int alive = tab.getAliveCount() >= 0 ? tab.getAliveCount() : 0;
        Set<String> plots = tab.getInfestedPlots();

        lines.add("§7Alive: §f" + alive);
        if (!plots.isEmpty()) {
            lines.add("§7Plots: §b" + String.join(", ", plots));
        }

        if (tab.getCooldownSeconds() >= 0) {
            if (tab.getCooldownSeconds() == 0) {
                lines.add("§7Spawn: §aREADY");
            } else if (tab.getCooldownSeconds() == 999) {
                lines.add("§7Spawn: §cMAX PESTS");
            } else {
                int m = tab.getCooldownSeconds() / 60;
                int s = tab.getCooldownSeconds() % 60;
                lines.add(String.format("§7Cooldown: §e%02d:%02d", m, s));
            }
        }

        lines.add("§7Kills (Session): §6" + PestManager.getSessionPestsKilled());

        if (PestDestroyer.getInstance().isRunning()) {
            lines.add("§7Status: §c" + PestDestroyer.getInstance().getState().name());
        } else if (PestLifecycleManager.getStage() != PestLifecycleManager.Stage.IDLE) {
            lines.add("§7Status: §e" + PestLifecycleManager.getStage().name());
        }

        return lines;
    }
}
