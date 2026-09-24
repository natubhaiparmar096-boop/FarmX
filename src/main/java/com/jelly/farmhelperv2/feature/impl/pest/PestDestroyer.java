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
        stateClock.schedule(100);
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
                    state = State.APPROACH_PEST;
                    stateClock.schedule(100);
                } else if (plotNavigator != null && plotNavigator.hasNextWaypoint()) {
                    state = State.SWEEP_WAYPOINTS;
                    stateClock.schedule(100);
                } else {
                    state = State.CHECK_NEXT_PLOT;
                    stateClock.schedule(200);
                }
                break;

            case APPROACH_PEST:
                if (currentTarget == null || currentTarget.isDead) {
                    state = State.SCAN_PESTS;
                    return;
                }
                double distSq = mc.thePlayer.getDistanceSqToEntity(currentTarget);
                if (distSq <= 16.0) { // Within 4 blocks
                    FlyPathFinderExecutor.getInstance().stop();
                    state = State.KILL_PEST;
                    killTimeout.schedule(5000);
                    PestCombatCoordinator.startVacuum();
                } else if (FarmHelperConfig.pestAotvHops && distSq > 36.0 && PestCombatCoordinator.performAotvHop(currentTarget.getPositionVector())) {
                    stateClock.schedule(250);
                } else {
                    FlyPathFinderExecutor.getInstance().findPath(currentTarget.getPositionVector(), true, true);
                    stateClock.schedule(300);
                }
                break;

            case KILL_PEST:
                if (currentTarget == null || currentTarget.isDead || killTimeout.passed()) {
                    if (currentTarget != null) {
                        killedEntities.add(currentTarget);
                    }
                    PestCombatCoordinator.stopVacuum();
                    currentTarget = null;
                    state = State.SCAN_PESTS;
                    stateClock.schedule(200);
                    return;
                }
                PestCombatCoordinator.aimAtPest(currentTarget, 120);
                PestCombatCoordinator.startVacuum();
                break;

            case SWEEP_WAYPOINTS:
                Vec3 wp = plotNavigator.nextWaypoint(mc.thePlayer.posY);
                if (wp != null) {
                    FlyPathFinderExecutor.getInstance().findPath(wp, false, true);
                    state = State.SCAN_PESTS;
                    stateClock.schedule(2000);
                } else {
                    sweepCount++;
                    if (sweepCount < 2) {
                        plotNavigator.reset();
                        state = State.SCAN_PESTS;
                    } else {
                        state = State.CHECK_NEXT_PLOT;
                    }
                    stateClock.schedule(500);
                }
                break;

            case CHECK_NEXT_PLOT:
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
