package com.jelly.farmhelperv2.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.failsafe.impl.GuestVisitFailsafe;
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
        // No rounded background / border — plain text only
        super(true, 1f, 10f, 0.5f, false, false, 0, 2, 2, new OneColor(0, 0, 0, 0), false, 0, new OneColor(0, 0, 0, 0));
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        if (!FarmHelperConfig.debugMode) return;
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;
        lines.add("§lFarmHelper v" + FarmHelper.VERSION + " Debug HUD");
        lines.add("wasGuestInGarden: " + GuestVisitFailsafe.getInstance().wasGuestInGarden);
        lines.add("Jacob's Contest Collected: " + GameStateHandler.getInstance().getJacobsContestCropNumber());
        if (GameStateHandler.getInstance().getServerClosingSeconds().isPresent())
            lines.add("Server closing in: " + GameStateHandler.getInstance().getServerClosingSeconds().get());
        if (MovRecPlayer.getInstance().isRunning()) {
            lines.add("MovRec Yaw Difference: " + MovRecPlayer.getYawDifference());
        }
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
        if (AutoCookie.getInstance().isRunning()) {
            lines.add("AutoCookie");
            lines.add("   Main State: " + AutoCookie.getInstance().getMainState());
            lines.add("   Movie Cookie State: " + AutoCookie.getInstance().getMoveCookieState());
            lines.add("   Bazaar State: " + AutoCookie.getInstance().getBazaarState());
            lines.add("   Clock: " + AutoCookie.getInstance().getAutoCookieDelay().getRemainingTime());
            lines.add("   Timeout clock: " + AutoCookie.getInstance().getTimeoutClock().getRemainingTime());
        }
        if (AntiStuck.getInstance().isRunning()) {
            lines.add("AntiStuck");
            lines.add("   State: " + AntiStuck.getInstance().getUnstuckState());
            lines.add("   Delay between change state: " + AntiStuck.getInstance().getDelayBetweenMovementsClock().getRemainingTime());
        }
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent())
            lines.add("Emergency: " + (FailsafeManager.getInstance().triggeredFailsafe.map(failsafe -> failsafe.getType().name()).orElse("None")));
        if (AutoGodPot.getInstance().isRunning()) {
            lines.add("AutoGodPot");
            lines.add("   Mode: " + AutoGodPot.getInstance().getGodPotMode());
            lines.add("   AH State: " + AutoGodPot.getInstance().getAhState());
            lines.add("   Going To AH State: " + AutoGodPot.getInstance().getGoingToAHState());
            lines.add("   Consume Pot State: " + AutoGodPot.getInstance().getConsumePotState());
            lines.add("   Clock: " + AutoGodPot.getInstance().getDelayClock().getRemainingTime());
        }
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
            lines.add("   BPS queue size: " + BPSTracker.getInstance().bpsQueue.size());
            lines.add("   Blocks broken: " + BPSTracker.getInstance().blocksBroken);
            lines.add("   Total blocks broken: " + BPSTracker.getInstance().totalBlocksBroken);
            lines.add("   Paused: " + BPSTracker.getInstance().isPaused);
            lines.add("   isResumingScheduled: " + BPSTracker.getInstance().isResumingScheduled);
            lines.add("   Pause start time: " + BPSTracker.getInstance().pauseStartTime);
            lines.add("   Last known BPS: " + BPSTracker.getInstance().lastKnownBPS);
            lines.add("   Elapsed time: " + BPSTracker.getInstance().elapsedTime);
            if (!BPSTracker.getInstance().bpsQueue.isEmpty()
                    && BPSTracker.getInstance().bpsQueue.getFirst() != null
                    && BPSTracker.getInstance().bpsQueue.getLast() != null) {
                lines.add("   First timestamp: " + BPSTracker.getInstance().bpsQueue.getFirst().getSecond());
                lines.add("   Last timestamp: " + BPSTracker.getInstance().bpsQueue.getLast().getSecond());
            }
        }
    }
}
