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

    private final Queue<String> plotQueue = new LinkedList<>();
    private String currentPlot = null;
    private PestPlotNavigator plotNavigator = null;
    private Entity currentTarget = null;
    private final Set<Entity> killedEntities = new HashSet<>();
    private int sweepCount = 0;

    public enum State {
        IDLE,
        TELEPORT_TO_PLOT,
        WAIT_TELEPORT,
        BALLSACK_SHREDDER,
        SCAN_PESTS,
        APPROACH_PEST,
        KILL_PEST,
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
            LogUtils.sendWarning("[Pest] No infested plots detected in queue.");
            stop();
            return;
        }

        currentPlot = plotQueue.poll();
        plotNavigator = new PestPlotNavigator(currentPlot);
        sweepCount = 0;
        state = State.TELEPORT_TO_PLOT;
        stateClock.schedule(1000);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !enabled || mc.thePlayer == null || mc.theWorld == null) return;
        if (!stateClock.passed()) return;

        switch (state) {
            case TELEPORT_TO_PLOT:
                if (currentPlot == null) {
                    state = State.CHECK_NEXT_PLOT;
                    return;
                }
                mc.thePlayer.sendChatMessage("/plottp " + currentPlot);
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
                currentTarget = PestTargetTracker.findClosestPest(killedEntities);
                if (currentTarget != null) {
                    FlyPathFinderExecutor.getInstance().stop();
                    state = State.APPROACH_PEST;
                    stateClock.schedule(50);
                    return;
                }
                PestTabSnapshot scanTab = PestTabSnapshot.read();
                if (scanTab.getAliveCount() == 0) {
                    state = State.FINISH;
                    stateClock.schedule(50);
                    return;
                }
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
                double distSq = mc.thePlayer.getDistanceSqToEntity(currentTarget);
                if (distSq <= 16.0) { // Within 4 blocks (attack range)
                    FlyPathFinderExecutor.getInstance().stop();
                    state = State.KILL_PEST;
                    killTimeout.schedule(6000);
                    PestCombatCoordinator.startVacuum();
                    stateClock.schedule(50);
                } else if (FarmHelperConfig.pestAotvHops && distSq > 36.0 && PestCombatCoordinator.performAotvHop(currentTarget.getPositionVector())) {
                    stateClock.schedule(200);
                } else {
                    if (!FlyPathFinderExecutor.getInstance().isRunning() || FlyPathFinderExecutor.getInstance().getState() == FlyPathFinderExecutor.State.FAILED) {
                        FlyPathFinderExecutor.getInstance().setSprinting(true);
                        FlyPathFinderExecutor.getInstance().findPath(currentTarget, true, true);
                    }
                    stateClock.schedule(100);
                }
                break;

            case KILL_PEST:
                if (currentTarget == null || currentTarget.isDead || killTimeout.passed()) {
                    if (currentTarget != null) {
                        killedEntities.add(currentTarget);
                        if (mc.theWorld != null) {
                            for (Entity e : mc.theWorld.loadedEntityList) {
                                if (e != null && e.getDistanceSqToEntity(currentTarget) <= 16.0) {
                                    killedEntities.add(e);
                                }
                            }
                        }
                    }
                    PestCombatCoordinator.stopVacuum();
                    currentTarget = null;
                    state = State.SCAN_PESTS;
                    stateClock.schedule(150);
                    return;
                }
                double currentDistSq = mc.thePlayer.getDistanceSqToEntity(currentTarget);
                if (currentDistSq > 25.0) { // Pest moved away (> 5 blocks)
                    PestCombatCoordinator.stopVacuum();
                    state = State.APPROACH_PEST;
                    stateClock.schedule(50);
                    return;
                }
                PestCombatCoordinator.aimAtPest(currentTarget, 80);
                PestCombatCoordinator.startVacuum();
                stateClock.schedule(50);
                break;

            case SWEEP_WAYPOINTS:
                Vec3 wp = plotNavigator.nextWaypoint(mc.thePlayer.posY);
                if (wp != null) {
                    double flightY = Math.max(74.0, mc.thePlayer.posY + 2.0);
                    Vec3 sweepWp = new Vec3(wp.xCoord, flightY, wp.zCoord);
                    FlyPathFinderExecutor.getInstance().setSprinting(true);
                    FlyPathFinderExecutor.getInstance().findPath(sweepWp, false, true);
                    sweepWaypointTimeout.schedule(10000);
                    state = State.SWEEPING;
                    stateClock.schedule(100);
                } else {
                    state = State.CHECK_NEXT_PLOT;
                    stateClock.schedule(200);
                }
                break;

            case SWEEPING:
                currentTarget = PestTargetTracker.findClosestPest(killedEntities);
                if (currentTarget != null) {
                    FlyPathFinderExecutor.getInstance().stop();
                    state = State.APPROACH_PEST;
                    stateClock.schedule(50);
                    return;
                }
                PestTabSnapshot sweepTab = PestTabSnapshot.read();
                if (sweepTab.getAliveCount() == 0) {
                    FlyPathFinderExecutor.getInstance().stop();
                    state = State.FINISH;
                    stateClock.schedule(50);
                    return;
                }
                if (!FlyPathFinderExecutor.getInstance().isRunning() || sweepWaypointTimeout.passed()) {
                    state = State.SWEEP_WAYPOINTS;
                    stateClock.schedule(100);
                } else {
                    stateClock.schedule(100);
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
                for (String p : currentlyInfested) {
                    String norm = PestPlotId.normalize(p);
                    if (!plotQueue.contains(norm) && !PestPlotId.equals(norm, currentPlot)) {
                        plotQueue.add(norm);
                    }
                }
                if (!plotQueue.isEmpty()) {
                    currentPlot = plotQueue.poll();
                    plotNavigator = new PestPlotNavigator(currentPlot);
                    sweepCount = 0;
                    state = State.TELEPORT_TO_PLOT;
                    stateClock.schedule(500);
                } else {
                    state = State.FINISH;
                    stateClock.schedule(200);
                }
                break;

            case FINISH:
                LogUtils.sendSuccess("[Pest] All queued plots cleared!");
                stop();
                break;
        }
    }
}
