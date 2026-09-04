package com.jelly.farmhelperv2.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.feature.FeatureManager;
import com.jelly.farmhelperv2.feature.IFeature;
import com.jelly.farmhelperv2.feature.impl.*;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.List;

public class DebugHUD extends TextHud {
    public DebugHUD() {
        super(true, 1f, 10f, 0.5f, false, false, 0, 2, 2, new OneColor(0, 0, 0, 0), false, 0, new OneColor(0, 0, 0, 0));
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        if (!FarmHelperConfig.debugMode) return;
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;
        lines.add("§lFarmX v" + FarmHelper.VERSION + " Debug HUD");
        lines.add("Jacob's Contest Collected: " + GameStateHandler.getInstance().getJacobsContestCropNumber());
        if (GameStateHandler.getInstance().getServerClosingSeconds().isPresent())
            lines.add("Server closing in: " + GameStateHandler.getInstance().getServerClosingSeconds().get());
        ItemStack heldItem = Minecraft.getMinecraft().thePlayer.getHeldItem();
        lines.add("Cultivating: " + GameStateHandler.getInstance().getCurrentCultivating().getOrDefault(heldItem != null ? heldItem.getDisplayName() : "", 0L));
        lines.add("Purse: " + GameStateHandler.getInstance().getCurrentPurse());
        lines.add("Copper: " + GameStateHandler.getInstance().getCopper());
        lines.add("Buffs:");
        lines.add("   God Pot: " + GameStateHandler.getInstance().getGodPotState());
        lines.add("   Cookie: " + GameStateHandler.getInstance().getCookieBuffState());
        lines.add("Location: " + GameStateHandler.getInstance().getLocation());
        MacroHandler.getInstance().getCurrentMacro().ifPresent(macro -> {
            lines.add("Current state: " + macro.getCurrentState());
            lines.add("Rotating: " + macro.getRotation().isRotating());
        });
        lines.add("Walkable directions: ");
        lines.add("   Forward: " + GameStateHandler.getInstance().isFrontWalkable());
        lines.add("   Backward: " + GameStateHandler.getInstance().isBackWalkable());
        lines.add("   Left: " + GameStateHandler.getInstance().isLeftWalkable());
        lines.add("   Right: " + GameStateHandler.getInstance().isRightWalkable());
        lines.add("   Not moving: " + GameStateHandler.getInstance().notMoving());
        lines.add("   HasPassedSinceStopped: " + GameStateHandler.getInstance().hasPassedSinceStopped());
        if (AntiStuck.getInstance().isRunning()) {
            lines.add("AntiStuck");
            lines.add("   State: " + AntiStuck.getInstance().getUnstuckState());
            lines.add("   Delay between change state: " + AntiStuck.getInstance().getDelayBetweenMovementsClock().getRemainingTime());
        }
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent())
            lines.add("Emergency: " + (FailsafeManager.getInstance().triggeredFailsafe.map(failsafe -> failsafe.getType().name()).orElse("None")));
        if (!FeatureManager.getInstance().getCurrentRunningFeatures().isEmpty()) {
            lines.add("Blocking Main Thread: " + FeatureManager.getInstance().getPauseExecutionFeatures().size());
            for (IFeature feature : FeatureManager.getInstance().getPauseExecutionFeatures()) {
                lines.add("   " + feature.getName());
            }
            lines.add("Running Features:");
            FeatureManager.getInstance().getCurrentRunningFeatures().forEach(feature -> lines.add("   " + feature.getName()));
        }
        if (BPSTracker.getInstance().isRunning()) {
            lines.add("BPSTracker");
            lines.add("   BPS: " + BPSTracker.getInstance().getBPS());
        }
    }
}
