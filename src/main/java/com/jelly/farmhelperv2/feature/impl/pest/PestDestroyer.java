package com.jelly.farmhelperv2.feature.impl.pest;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.feature.IFeature;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.*;
import com.jelly.farmhelperv2.handler.RotationHandler;
import com.jelly.farmhelperv2.pathfinder.FlyPathFinderExecutor;
import com.jelly.farmhelperv2.util.GardenPlots;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

public class PestDestroyer implements IFeature {
    private static PestDestroyer instance;
    private final Minecraft mc = Minecraft.getMinecraft();

    private boolean enabled = false;
    private State state = State.IDLE;
    private final Clock stateClock = new Clock();
    private final Clock killTimeout = new Clock();
    private final Clock sweepWaypointTimeout = new Clock();
    private final Clock trackerPulseCooldown = new Clock();
    private final Clock acousticInjectionCooldown = new Clock();

    private final Queue<String> plotQueue = new LinkedList<>();
    private String currentPlot = null;
    private PestPlotNavigator plotNavigator = null;
    private Entity currentTarget = null;
    private final Set<Entity> killedEntities = new HashSet<>();
    private int plotRetryAttempts = 0;

    public enum State {
        IDLE,
        TELEPORT_TO_PLOT,
        WAIT_TELEPORT,
        BALLSACK_SHREDDER,
        SCAN_PESTS,
        APPROACH_PEST,
        KILL_PEST,
        REPOSITION_PEST,
        SWEEP_WAYPOINTS,
        SWEEPING,
        CHECK_NEXT_PLOT,
        FINISH
    }

    public static PestDestroyer getInstance() {
        if (instance == null) {
            instance = new PestDestroyer();
        }
        return instance;
    }

    // ------------------------------------------------------------------ IFeature

    @Override
    public String getName() {
        return "Pest Destroyer";
    }

    @Override
    public boolean isRunning() {
        return enabled;
    }

    @Override
    public boolean shouldPauseMacroExecution() {
        return true;
    }

    @Override
    public boolean shouldStartAtMacroStart() {
        return false;
    }

    @Override
    public void start() {
        if (enabled) return;
        enabled = true;
        state = State.IDLE;
    }

    @Override
    public void stop() {
        enabled = false;
        state = State.IDLE;
        currentTarget = null;
        killedEntities.clear();
        plotQueue.clear();
        PestCombatCoordinator.stopVacuum();
        FlyPathFinderExecutor.getInstance().stop();
        PestBallsackShredder.reset();
        sweepWaypointTimeout.reset();
        acousticInjectionCooldown.reset();
        PestSoundTracker.getInstance().clear();
        PestTrackerAbility.clear();
    }

    @Override
    public void resetStatesAfterMacroDisabled() {
        stop();
    }

    @Override
    public boolean isToggled() {
        return FarmHelperConfig.enablePestDestroyer;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return false;
    }

    // ------------------------------------------------------------------ Logic

    public State getState() {
        return state;
    }

    public void startCleaning(String initialPlot) {
        start();
        plotQueue.clear();
        killedEntities.clear();
        plotRetryAttempts = 0;

        Set<String> infested = PestTabSnapshot.read().getInfestedPlots();
        if (PestPlotId.isUsable(initialPlot)) {
            plotQueue.add(PestPlotId.normalize(initialPlot));
        }
        for (String p : infested) {
            String norm = PestPlotId.normalize(p);
            if (!plotQueue.contains(norm)) {
                plotQueue.add(norm);
            }
        }

        if (plotQueue.isEmpty()) {
            PestTabSnapshot tab = PestTabSnapshot.read();
            if (tab.getAliveCount() > 0) {
                // If pests are alive but tablist didn't specify the plot, start from current plot
                com.jelly.farmhelperv2.util.PlotUtils.Plot cur = com.jelly.farmhelperv2.util.PlotUtils.getPlotNumberBasedOnLocation();
                if (cur != null && cur.number != null) {
                    plotQueue.add(String.valueOf(cur.number));
                }
                for (int i = 0; i <= 24; i++) {
                    String pStr = String.valueOf(i);
                    if (!plotQueue.contains(pStr)) {
                        plotQueue.add(pStr);
                    }
                }
            } else {
                LogUtils.sendWarning("[Pest] No infested plots detected in queue.");
                stop();
                return;
            }
        }

        currentPlot = plotQueue.poll();
        plotNavigator = new PestPlotNavigator(currentPlot);
        state = State.TELEPORT_TO_PLOT;
        stateClock.schedule(200);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !enabled || mc.thePlayer == null || mc.theWorld == null) return;
        if (!stateClock.passed()) return;

        GardenPlots.Bounds plotBounds = plotNavigator != null ? plotNavigator.getBounds() : null;

        switch (state) {
            case TELEPORT_TO_PLOT:
                if (currentPlot == null) {
                    state = State.CHECK_NEXT_PLOT;
                    return;
                }
                // If player is already within the target plot, skip /plottp command
                if (plotBounds != null && plotBounds.contains(mc.thePlayer.posX, mc.thePlayer.posZ, 2.0)) {
                    state = State.WAIT_TELEPORT;
                    stateClock.schedule(200);
                    return;
                }
                String tpArg = "0".equals(currentPlot) ? "barn" : currentPlot;
                mc.thePlayer.sendChatMessage("/plottp " + tpArg);
                state = State.WAIT_TELEPORT;
                stateClock.schedule(2500);
                break;

            case WAIT_TELEPORT:
                PestLoadoutHelper.equipSlot(PestLoadoutHelper.findVacuumSlot());
                if (PestBallsackShredder.isConfiguredForPlot(currentPlot)) {
                    state = State.BALLSACK_SHREDDER;
                    PestBallsackShredder.start();
                } else {
                    state = State.SCAN_PESTS;
                    stateClock.schedule(200);
                }
                break;

            case BALLSACK_SHREDDER:
                PestBallsackShredder.onTick();
                if (PestBallsackShredder.isFinished()) {
                    state = State.SCAN_PESTS;
                    stateClock.schedule(300);
                }
                break;

            case SCAN_PESTS:
                // 1. Check physical loaded entities in memory
                currentTarget = PestTargetTracker.findClosestPest(plotBounds, killedEntities);
                if (currentTarget != null) {
                    FlyPathFinderExecutor.getInstance().stop();
                    state = State.APPROACH_PEST;
                    stateClock.schedule(50);
                    return;
                }

                // 2. Check tablist: if alive count is 0, we're done
                PestTabSnapshot scanTab = PestTabSnapshot.read();
                if (scanTab.getAliveCount() == 0) {
                    state = State.FINISH;
                    stateClock.schedule(50);
                    return;
                }

                double roofY = plotNavigator != null ? plotNavigator.calculateRoofClearanceY(mc.thePlayer.posY) : Math.max(78.0, mc.thePlayer.posY);

                // 3. Acoustic Radar check (cooldown prevents infinite re-injection)
                if (acousticInjectionCooldown.passed()) {
                    Vec3 soundWp = PestTargetTracker.getAcousticRadarWaypoint(plotBounds, 3000, roofY);
                    if (soundWp != null && plotNavigator != null) {
                        plotNavigator.injectPriorityWaypoint(soundWp);
                        acousticInjectionCooldown.schedule(8000);
                        state = State.SWEEP_WAYPOINTS;
                        stateClock.schedule(50);
                        return;
                    }
                }

                // 4. Vacuum Tracker Scent Pulse check
                if (trackerPulseCooldown.passed()) {
                    PestTrackerAbility.triggerPulse();
                    trackerPulseCooldown.schedule(1500);
                }
                if (PestTrackerAbility.hasFreshTrail(1200)) {
                    Vec3 projected = PestTrackerAbility.getProjectedWaypoint(45.0, plotBounds, roofY);
                    if (projected != null && plotNavigator != null) {
                        plotNavigator.injectPriorityWaypoint(projected);
                        state = State.SWEEP_WAYPOINTS;
                        stateClock.schedule(50);
                        return;
                    }
                }

                // 5. Normal sweep waypoints
                if (plotNavigator != null && plotNavigator.hasNextWaypoint()) {
                    state = State.SWEEP_WAYPOINTS;
                    stateClock.schedule(50);
                } else {
                    state = State.CHECK_NEXT_PLOT;
                    stateClock.schedule(150);
                }
                break;

            case APPROACH_PEST:
                if (currentTarget == null || currentTarget.isDead) {
                    FlyPathFinderExecutor.getInstance().stop();
                    currentTarget = null;
                    state = State.SCAN_PESTS;
                    stateClock.schedule(100);
                    return;
                }

                double pRoofY = plotNavigator != null ? plotNavigator.calculateRoofClearanceY(mc.thePlayer.posY) : Math.max(78.0, mc.thePlayer.posY);
                double targetY = FarmHelperConfig.pestRoofVacuuming ? pRoofY : currentTarget.posY + 1.5;
                Vec3 approachVec = new Vec3(currentTarget.posX, targetY, currentTarget.posZ);

                double distSq = mc.thePlayer.getDistanceSqToEntity(currentTarget);
                double horizDistSq = (mc.thePlayer.posX - currentTarget.posX) * (mc.thePlayer.posX - currentTarget.posX)
                        + (mc.thePlayer.posZ - currentTarget.posZ) * (mc.thePlayer.posZ - currentTarget.posZ);

                // Attack range: within 16 distSq OR hovering directly above in roof mode with horiz dist <= 20
                if (distSq <= 20.0 || (FarmHelperConfig.pestRoofVacuuming && horizDistSq <= 20.0 && distSq <= 144.0)) {
                    FlyPathFinderExecutor.getInstance().stop();
                    state = State.KILL_PEST;
                    killTimeout.schedule(6000);
                    PestCombatCoordinator.startVacuum();
                    stateClock.schedule(50);
                } else if (FarmHelperConfig.pestAotvHops && distSq > 36.0 && PestCombatCoordinator.performAotvHop(approachVec)) {
                    stateClock.schedule(800);
                } else {
                    if (!FlyPathFinderExecutor.getInstance().isRunning() || FlyPathFinderExecutor.getInstance().getState() == FlyPathFinderExecutor.State.FAILED) {
                        FlyPathFinderExecutor.getInstance().setSprinting(true);
                        FlyPathFinderExecutor.getInstance().findPath(approachVec, true, true);
                    }
                    stateClock.schedule(80);
                }
                break;

            case KILL_PEST:
                if (currentTarget == null || currentTarget.isDead || !mc.theWorld.loadedEntityList.contains(currentTarget)) {
                    if (currentTarget != null) {
                        killedEntities.add(currentTarget);
                    }
                    PestCombatCoordinator.stopVacuum();
                    currentTarget = null;
                    state = State.SCAN_PESTS;
                    stateClock.schedule(100);
                    return;
                }

                // If 6-second kill timeout passed, do NOT blacklist. Reposition!
                if (killTimeout.passed()) {
                    PestCombatCoordinator.stopVacuum();
                    state = State.REPOSITION_PEST;
                    stateClock.schedule(50);
                    return;
                }

                double curDistSq = mc.thePlayer.getDistanceSqToEntity(currentTarget);
                double curHorizSq = (mc.thePlayer.posX - currentTarget.posX) * (mc.thePlayer.posX - currentTarget.posX)
                        + (mc.thePlayer.posZ - currentTarget.posZ) * (mc.thePlayer.posZ - currentTarget.posZ);

                // If pest escaped range
                if (curDistSq > 160.0 || (!FarmHelperConfig.pestRoofVacuuming && curDistSq > 36.0) || (FarmHelperConfig.pestRoofVacuuming && curHorizSq > 36.0)) {
                    PestCombatCoordinator.stopVacuum();
                    state = State.APPROACH_PEST;
                    stateClock.schedule(50);
                    return;
                }

                // Aim and vacuum
                if (FarmHelperConfig.pestRoofVacuuming) {
                    PestCombatCoordinator.aimAtDownward(currentTarget.getPositionVector(), 60);
                } else {
                    PestCombatCoordinator.aimAtPest(currentTarget, 60);
                }
                PestCombatCoordinator.startVacuum();
                stateClock.schedule(50);
                break;

            case REPOSITION_PEST:
                if (currentTarget == null || currentTarget.isDead) {
                    state = State.SCAN_PESTS;
                    stateClock.schedule(50);
                    return;
                }
                // Gain altitude or shift offset to clear glass/wall obstruction
                Vec3 repoPos = new Vec3(
                        currentTarget.posX + (Math.random() - 0.5) * 6.0,
                        Math.max(78.0, mc.thePlayer.posY + 2.5),
                        currentTarget.posZ + (Math.random() - 0.5) * 6.0
                );
                FlyPathFinderExecutor.getInstance().findPath(repoPos, false, true);
                state = State.APPROACH_PEST;
                stateClock.schedule(500);
                break;

            case SWEEP_WAYPOINTS:
                double defaultRoofY = plotNavigator != null ? plotNavigator.calculateRoofClearanceY(mc.thePlayer.posY) : Math.max(78.0, mc.thePlayer.posY);
                Vec3 wp = plotNavigator != null ? plotNavigator.nextWaypoint(defaultRoofY) : null;
                if (wp != null) {
                    double wpDist = mc.thePlayer.getDistance(wp.xCoord, wp.yCoord, wp.zCoord);
                    if (FarmHelperConfig.pestAotvHops && wpDist > 20.0 && PestCombatCoordinator.performAotvHop(wp)) {
                        // AOTV hop succeeded - wait for teleport, don't also pathfind
                        sweepWaypointTimeout.schedule(8000);
                        state = State.SWEEPING;
                        stateClock.schedule(800);
                    } else {
                        // Fly pathfind to waypoint
                        FlyPathFinderExecutor.getInstance().setSprinting(true);
                        FlyPathFinderExecutor.getInstance().findPath(wp, false, true);
                        sweepWaypointTimeout.schedule(8000);
                        state = State.SWEEPING;
                        stateClock.schedule(200);
                    }
                } else {
                    state = State.CHECK_NEXT_PLOT;
                    stateClock.schedule(150);
                }
                break;

            case SWEEPING:
                currentTarget = PestTargetTracker.findClosestPest(plotBounds, killedEntities);
                if (currentTarget != null) {
                    FlyPathFinderExecutor.getInstance().stop();
                    state = State.APPROACH_PEST;
                    stateClock.schedule(50);
                    return;
                }

                // Check tablist: if all alive pests cleared, finish immediately
                PestTabSnapshot sweepTab = PestTabSnapshot.read();
                if (sweepTab.getAliveCount() == 0) {
                    FlyPathFinderExecutor.getInstance().stop();
                    state = State.FINISH;
                    stateClock.schedule(50);
                    return;
                }

                // When pathfinder finishes or times out, go to next waypoint
                if (!FlyPathFinderExecutor.getInstance().isRunning() || sweepWaypointTimeout.passed()) {
                    // After finishing a waypoint, go back to SCAN_PESTS to do a full check
                    // (including acoustic radar with cooldown) before next waypoint
                    state = State.SCAN_PESTS;
                    stateClock.schedule(200);
                } else {
                    stateClock.schedule(150);
                }
                break;

            case CHECK_NEXT_PLOT:
                PestTabSnapshot checkTab = PestTabSnapshot.read();
                if (checkTab.getAliveCount() == 0) {
                    state = State.FINISH;
                    stateClock.schedule(50);
                    return;
                }

                Set<String> currentlyInfested = checkTab.getInfestedPlots();

                // Re-populate queue with remaining infested plots first
                for (String p : currentlyInfested) {
                    String norm = PestPlotId.normalize(p);
                    if (!plotQueue.contains(norm) && !PestPlotId.equals(norm, currentPlot)) {
                        plotQueue.add(norm);
                    }
                }

                // If other plots are queued, visit the next one immediately
                if (!plotQueue.isEmpty()) {
                    // Re-queue current plot at the back if still listed as infested and under retry limit
                    if (currentPlot != null && currentlyInfested.contains(PestPlotId.normalize(currentPlot)) && plotRetryAttempts < 2) {
                        plotRetryAttempts++;
                        plotQueue.add(PestPlotId.normalize(currentPlot));
                    }
                    currentPlot = plotQueue.poll();
                    plotNavigator = new PestPlotNavigator(currentPlot);
                    state = State.TELEPORT_TO_PLOT;
                    stateClock.schedule(500);
                    return;
                }

                // If no other plots and current plot is still reported as infested, do one re-scan
                if (currentPlot != null && currentlyInfested.contains(PestPlotId.normalize(currentPlot)) && plotRetryAttempts < 2) {
                    plotRetryAttempts++;
                    LogUtils.sendWarning("[Pest] Re-scanning plot " + currentPlot + " (Attempt " + plotRetryAttempts + "/2)...");
                    if (plotNavigator != null) {
                        plotNavigator.reset();
                    }
                    PestTrackerAbility.triggerPulse();
                    state = State.SCAN_PESTS;
                    stateClock.schedule(500);
                    return;
                }

                state = State.FINISH;
                stateClock.schedule(200);
                break;

            case FINISH:
                LogUtils.sendSuccess("[Pest] All queued plots cleared!");
                stop();
                break;
        }
    }
}
