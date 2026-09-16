package com.jelly.farmhelperv2.feature.pest;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.feature.IFeature;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.handler.RotationHandler;
import com.jelly.farmhelperv2.pathfinder.FlyPathFinderExecutor;
import com.jelly.farmhelperv2.util.InventoryUtils;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.RenderUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import com.jelly.farmhelperv2.util.helper.RotationConfiguration;
import com.jelly.farmhelperv2.util.helper.Target;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import com.jelly.farmhelperv2.event.SpawnObjectEvent;
import com.jelly.farmhelperv2.event.SpawnParticleEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FakePixelPestController implements IFeature {

    private static FakePixelPestController instance;

    public static FakePixelPestController getInstance() {
        if (instance == null) {
            instance = new FakePixelPestController();
        }
        return instance;
    }

    public enum State {
        IDLE,
        FARMING,
        PEST_DETECTED,
        SET_HOME_BEFORE_HUNT,
        WAIT_FOR_SET_HOME,
        FIND_PEST,
        LOOK_FOR_FIREWORK,
        WAIT_FOR_FIREWORK,
        FLY_TO_FIREWORK,
        MOVE_TO_PEST,
        ATTACK_VACUUM,
        CONFIRM_PEST_DEAD,
        CHECK_NEXT_PEST,
        RETURN_TO_HOME,
        WAIT_FOR_RETURN,
        RESUME_FARMING,
        FAILED,
        COOLDOWN
    }

    private final Minecraft mc = Minecraft.getMinecraft();
    private final PestPlatformAdapter adapter = new FakePixelPestAdapter();
    private final PestDetector detector = new PestDetector(adapter);

    private State currentState = State.IDLE;
    private boolean enabled = false;
    private boolean manualMode = false;
    private PestInfo targetPest = null;
    private int originalHotbarSlot = -1;
    private int retryCount = 0;

    private final List<Entity> killedEntities = new ArrayList<>();
    private final Clock stateTimer = new Clock();
    private final Clock attackTimer = new Clock();
    private final Clock cooldownTimer = new Clock();

    // Firework direction tracking
    private Vec3 fireworkWaypoint = null;
    private Vec3 firstParticleLocation = null;
    private Vec3 lastParticleLocation = null;
    private Vec3 targetWaypoint = null;
    private int fireworkTries = 0;
    private boolean pathfinderFailed = false;

    public void resetFireworkInfo() {
        fireworkWaypoint = null;
        firstParticleLocation = null;
        lastParticleLocation = null;
    }

    @Override
    public String getName() {
        return "FakePixel Pest Controller";
    }

    @Override
    public boolean isRunning() {
        return enabled && currentState != State.IDLE;
    }

    @Override
    public boolean shouldPauseMacroExecution() {
        return FarmHelperConfig.pauseFarmingWhileHandlingPest;
    }

    @Override
    public boolean shouldStartAtMacroStart() {
        return false;
    }

    @Override
    public boolean isToggled() {
        return FarmHelperConfig.enableFakePixelPestFarming;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return currentState != State.IDLE && currentState != State.FAILED;
    }

    @Override
    public void start() {
        if (enabled) return;
        enabled = true;
        manualMode = false;
        currentState = State.IDLE;
        retryCount = 0;
        targetPest = null;
        killedEntities.clear();
        resetFireworkInfo();
        targetWaypoint = null;
        pathfinderFailed = false;
        originalHotbarSlot = mc.thePlayer != null ? mc.thePlayer.inventory.currentItem : -1;

        int vacuumSlot = adapter.findPestVacuumSlot();
        if (vacuumSlot != -1 && mc.thePlayer != null) {
            mc.thePlayer.inventory.currentItem = vacuumSlot;
            logDebug("Equipped vacuum in hotbar slot " + (vacuumSlot + 1));
        }

        logDebug("Pest Controller started.");
        IFeature.super.start();
    }

    public void startManual() {
        if (isRunning()) stop();
        enabled = true;
        manualMode = true;
        retryCount = 0;
        targetPest = null;
        killedEntities.clear();
        resetFireworkInfo();
        targetWaypoint = null;
        pathfinderFailed = false;
        originalHotbarSlot = mc.thePlayer != null ? mc.thePlayer.inventory.currentItem : -1;

        if (MacroHandler.getInstance().isMacroToggled()) {
            MacroHandler.getInstance().pauseMacro();
        }
        KeyBindUtils.stopMovement();

        int vacuumSlot = adapter.findPestVacuumSlot();
        if (vacuumSlot != -1 && mc.thePlayer != null) {
            mc.thePlayer.inventory.currentItem = vacuumSlot;
            logDebug("Equipped vacuum in hotbar slot " + (vacuumSlot + 1));
        } else {
            LogUtils.sendWarning("[Pest Controller] Vacuum not found in hotbar! Please ensure your vacuum is in hotbar.");
        }

        LogUtils.sendWarning("[Pest Controller] Starting manual pest elimination!");
        logDebug("Manual Pest Hunt initiated.");

        if (FarmHelperConfig.fakePixelSetHomeBeforeHunt) {
            currentState = State.SET_HOME_BEFORE_HUNT;
            stateTimer.schedule(300);
        } else {
            currentState = State.FIND_PEST;
            stateTimer.schedule(300);
        }
        IFeature.super.start();
    }

    @Override
    public void stop() {
        if (!enabled && currentState == State.IDLE) return;
        logDebug("Pest Controller stopping.");
        restoreState();
        enabled = false;
        manualMode = false;
        currentState = State.IDLE;
        targetPest = null;
        killedEntities.clear();
        detector.clearCache();
        IFeature.super.stop();
    }

    @Override
    public void resetStatesAfterMacroDisabled() {
        stop();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (!isToggled() && !manualMode) {
            if (isRunning()) stop();
            return;
        }

        // Failsafe checks
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) {
            if (isRunning()) handleFailure("Failsafe triggered");
            return;
        }

        if (!adapter.isInGarden()) {
            if (isRunning()) handleFailure("Player left Garden");
            return;
        }

        // Trigger check when farming (Farming Mode)
        if (!isRunning() && MacroHandler.getInstance().isMacroToggled() && MacroHandler.getInstance().isCurrentMacroEnabled()) {
            List<PestInfo> pests = detector.scanPests(false);
            int count = Math.max(pests.size(), GameStateHandler.getInstance().getPestsCount());
            if (count >= FarmHelperConfig.fakePixelStartHuntingPestsAt && !pests.isEmpty()) {
                // Tier 1 deference: if inline killer is active and all detected pests are within vacuum range, let it handle
                boolean inlineActive = FarmHelperConfig.fakePixelInlinePestKiller;
                boolean allInRange = pests.stream().allMatch(p -> p.getDistance() <= FarmHelperConfig.pestVacuumRange);
                if (inlineActive && allInRange) {
                    return;
                }
                manualMode = false;
                start();
                currentState = State.PEST_DETECTED;
                stateTimer.schedule(FarmHelperConfig.pestInteractionTimeoutMs);
            }
            return;
        }

        if (!isRunning()) return;

        // State Machine Processing
        switch (currentState) {
            case PEST_DETECTED:
                logDebug("Pest threshold reached! Pausing farming macro.");
                if (MacroHandler.getInstance().isMacroToggled()) {
                    MacroHandler.getInstance().pauseMacro();
                }
                KeyBindUtils.stopMovement();
                if (FarmHelperConfig.fakePixelSetHomeBeforeHunt) {
                    currentState = State.SET_HOME_BEFORE_HUNT;
                    stateTimer.schedule(300);
                } else {
                    currentState = State.FIND_PEST;
                    stateTimer.schedule(500);
                }
                break;

            case SET_HOME_BEFORE_HUNT:
                if (mc.thePlayer != null) {
                    mc.thePlayer.sendChatMessage("/sethome");
                    logDebug("Sent /sethome before starting pest hunt.");
                }
                currentState = State.WAIT_FOR_SET_HOME;
                stateTimer.schedule(800);
                break;

            case WAIT_FOR_SET_HOME:
                if (stateTimer.passed()) {
                    currentState = State.FIND_PEST;
                }
                break;

            case FIND_PEST:
                // Ensure vacuum is held
                int curVacSlot = adapter.findPestVacuumSlot();
                if (curVacSlot != -1 && mc.thePlayer != null && mc.thePlayer.inventory.currentItem != curVacSlot) {
                    mc.thePlayer.inventory.currentItem = curVacSlot;
                }

                List<PestInfo> livePests = detector.scanPests(true);
                livePests.removeIf(p -> p.getEntity() == null || p.getEntity().isDead || killedEntities.contains(p.getEntity()) || !p.isAlive());

                if (!livePests.isEmpty()) {
                    livePests.sort(Comparator.comparingDouble(PestInfo::getDistance));
                    targetPest = livePests.get(0);
                    logDebug("Target pest selected: " + targetPest.getPestType() + " at dist " + String.format("%.2f", targetPest.getDistance()));
                    if (mc.thePlayer != null && originalHotbarSlot == -1) {
                        originalHotbarSlot = mc.thePlayer.inventory.currentItem;
                    }
                    pathfinderFailed = false;
                    currentState = State.MOVE_TO_PEST;
                    retryCount = 0;
                    stateTimer.schedule(FarmHelperConfig.pestInteractionTimeoutMs + 7000);
                } else {
                    // No pests in immediate render distance — left-click vacuum to trigger firework tracking
                    logDebug("No pests in immediate loaded chunks. Using vacuum firework to locate distant pests...");
                    fireworkTries = 0;
                    currentState = State.LOOK_FOR_FIREWORK;
                    stateTimer.schedule(200);
                }
                break;

            case LOOK_FOR_FIREWORK:
                if (mc.thePlayer == null) break;
                int vac = adapter.findPestVacuumSlot();
                if (vac != -1) {
                    mc.thePlayer.inventory.currentItem = vac;
                }
                if (mc.thePlayer.capabilities.allowFlying && !mc.thePlayer.capabilities.isFlying) {
                    mc.thePlayer.capabilities.isFlying = true;
                }
                resetFireworkInfo();
                // Aim up into open sky so left click doesn't hit a block
                mc.thePlayer.rotationPitch = -30.0F;

                logDebug("Left-clicking vacuum to trigger pest firework...");
                KeyBindUtils.leftClick();

                currentState = State.WAIT_FOR_FIREWORK;
                stateTimer.schedule(2000); // 2 seconds to receive particle/firework
                break;

            case WAIT_FOR_FIREWORK:
                if (fireworkWaypoint != null) {
                    logDebug("Firework detected! Heading to waypoint: " + fireworkWaypoint);
                    targetWaypoint = fireworkWaypoint;
                    fireworkTries = 0;
                    currentState = State.FLY_TO_FIREWORK;
                    stateTimer.schedule(10000);
                    break;
                }
                if (stateTimer.passed()) {
                    fireworkTries++;
                    if (fireworkTries < 3) {
                        logDebug("No firework detected yet, retrying (" + fireworkTries + "/3)...");
                        currentState = State.LOOK_FOR_FIREWORK;
                        stateTimer.schedule(400);
                    } else {
                        logDebug("No firework produced after retries. All pests eliminated.");
                        fireworkTries = 0;
                        currentState = State.RETURN_TO_HOME;
                    }
                }
                break;

            case FLY_TO_FIREWORK:
                if (targetWaypoint == null || mc.thePlayer == null) {
                    currentState = State.FIND_PEST;
                    break;
                }
                // Direct fly towards firework waypoint at safe open sky Y=85
                directFlyTo(targetWaypoint.xCoord, 85.0, targetWaypoint.zCoord, 3.0);

                // Continuously scan for pests as chunks load
                List<PestInfo> scannedDuringFlight = detector.scanPests(true);
                scannedDuringFlight.removeIf(p -> p.getEntity() == null || p.getEntity().isDead || killedEntities.contains(p.getEntity()) || !p.isAlive());
                if (!scannedDuringFlight.isEmpty()) {
                    KeyBindUtils.stopMovement();
                    scannedDuringFlight.sort(Comparator.comparingDouble(PestInfo::getDistance));
                    targetPest = scannedDuringFlight.get(0);
                    logDebug("Pest sighted during flight! Targeting: " + targetPest.getPestType());
                    pathfinderFailed = false;
                    currentState = State.MOVE_TO_PEST;
                    stateTimer.schedule(FarmHelperConfig.pestInteractionTimeoutMs + 7000);
                    break;
                }

                double distToWp = mc.thePlayer.getDistance(targetWaypoint.xCoord, mc.thePlayer.posY, targetWaypoint.zCoord);
                if (distToWp < 5.0 || stateTimer.passed()) {
                    KeyBindUtils.stopMovement();
                    logDebug("Reached firework waypoint area. Rescanning for pests...");
                    targetWaypoint = null;
                    currentState = State.FIND_PEST;
                    stateTimer.schedule(500);
                }
                break;

            case MOVE_TO_PEST:
                if (targetPest == null || adapter.isPestRemoved(targetPest) || targetPest.getEntity().isDead) {
                    FlyPathFinderExecutor.getInstance().stop();
                    KeyBindUtils.stopMovement();
                    currentState = State.CHECK_NEXT_PEST;
                    break;
                }
                double dist = mc.thePlayer.getDistanceToEntity(targetPest.getEntity());
                if (dist <= FarmHelperConfig.pestVacuumRange) {
                    FlyPathFinderExecutor.getInstance().stop();
                    KeyBindUtils.stopMovement();
                    currentState = State.ATTACK_VACUUM;
                    attackTimer.schedule(100);
                    stateTimer.schedule(FarmHelperConfig.pestInteractionTimeoutMs);
                } else {
                    int vSlotMove = adapter.findPestVacuumSlot();
                    if (vSlotMove != -1 && mc.thePlayer.inventory.currentItem != vSlotMove) {
                        mc.thePlayer.inventory.currentItem = vSlotMove;
                    }
                    rotateAndMoveToPest(targetPest.getEntity());
                    if (stateTimer.passed()) {
                        FlyPathFinderExecutor.getInstance().stop();
                        KeyBindUtils.stopMovement();
                        retryCount++;
                        if (retryCount >= FarmHelperConfig.pestMaxRetryCount) {
                            logDebug("Timeout moving to pest after retries, skipping to next.");
                            if (targetPest.getEntity() != null) killedEntities.add(targetPest.getEntity());
                            currentState = State.CHECK_NEXT_PEST;
                        } else {
                            stateTimer.schedule(FarmHelperConfig.pestInteractionTimeoutMs);
                        }
                    }
                }
                break;

            case ATTACK_VACUUM:
                if (targetPest == null || adapter.isPestRemoved(targetPest) || targetPest.getEntity().isDead) {
                    KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindUseItem, false);
                    KeyBindUtils.stopMovement();
                    currentState = State.CONFIRM_PEST_DEAD;
                    stateTimer.schedule(400);
                    break;
                }
                int vacuumSlot = adapter.findPestVacuumSlot();
                if (vacuumSlot != -1 && mc.thePlayer.inventory.currentItem != vacuumSlot) {
                    mc.thePlayer.inventory.currentItem = vacuumSlot;
                }

                // Only start a new rotation when RotationHandler is idle — avoids constant restart glitch
                if (!RotationHandler.getInstance().isRotating()) {
                    rotateToPest(targetPest.getEntity());
                }
                // Hold right-click to vacuum the pest
                KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindUseItem, true);

                if (stateTimer.passed()) {
                    KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindUseItem, false);
                    logDebug("Attack timer passed on pest, confirming removal...");
                    currentState = State.CONFIRM_PEST_DEAD;
                    stateTimer.schedule(400);
                }
                break;

            case CONFIRM_PEST_DEAD:
                KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindUseItem, false);
                if (stateTimer.passed()) {
                    if (targetPest != null && targetPest.getEntity() != null) {
                        killedEntities.add(targetPest.getEntity());
                        LogUtils.sendSuccess("[Pest Controller] Eliminated pest: " + targetPest.getPestType() + " (Total: " + killedEntities.size() + ")");
                    }
                    currentState = State.CHECK_NEXT_PEST;
                }
                break;

            case CHECK_NEXT_PEST:
                targetPest = null;
                retryCount = 0;
                pathfinderFailed = false;
                List<PestInfo> remaining = detector.scanPests(true);
                remaining.removeIf(p -> p.getEntity() == null || p.getEntity().isDead || killedEntities.contains(p.getEntity()) || !p.isAlive());

                if (!remaining.isEmpty()) {
                    logDebug(remaining.size() + " pests remaining in local chunks. Targeting next pest!");
                    currentState = State.FIND_PEST;
                } else {
                    logDebug("No remaining pests in local area. Checking other plots via firework...");
                    fireworkTries = 0;
                    currentState = State.LOOK_FOR_FIREWORK;
                    stateTimer.schedule(300);
                }
                break;

            case RETURN_TO_HOME:
                logDebug("Pest hunt complete! Returning to spawn point.");
                restoreState();
                if (FarmHelperConfig.fakePixelRewarpAfterHunt) {
                    String cmd = FarmHelperConfig.fakePixelRewarpCommand != null ? FarmHelperConfig.fakePixelRewarpCommand.trim() : "/warp garden";
                    if (!cmd.isEmpty() && mc.thePlayer != null) {
                        mc.thePlayer.sendChatMessage(cmd);
                        logDebug("Sent rewarp command: " + cmd);
                    }
                    currentState = State.WAIT_FOR_RETURN;
                    stateTimer.schedule(2500);
                } else {
                    currentState = State.RESUME_FARMING;
                    stateTimer.schedule(500);
                }
                break;

            case WAIT_FOR_RETURN:
                if (stateTimer.passed()) {
                    currentState = State.RESUME_FARMING;
                }
                break;

            case RESUME_FARMING:
                restoreState();
                if (manualMode) {
                    if (killedEntities.isEmpty()) {
                        LogUtils.sendWarning("[Pest Controller] Manual pest hunt ended — no pests could be detected in loaded chunks.");
                    } else {
                        LogUtils.sendSuccess("[Pest Controller] Manual pest hunt completed successfully! Eliminated " + killedEntities.size() + " pests.");
                    }
                    stop();
                } else {
                    if (MacroHandler.getInstance().isMacroToggled()) {
                        MacroHandler.getInstance().resumeMacro();
                    }
                    currentState = State.COOLDOWN;
                    cooldownTimer.schedule(2000);
                }
                break;

            case COOLDOWN:
                if (cooldownTimer.passed()) {
                    stop();
                }
                break;

            case FAILED:
                restoreState();
                logDebug("Entering failed state recovery.");
                if (manualMode) {
                    LogUtils.sendError("[Pest Controller] Pest hunt aborted or failed.");
                } else if (MacroHandler.getInstance().isMacroToggled()) {
                    MacroHandler.getInstance().resumeMacro();
                }
                stop();
                break;

            case IDLE:
            default:
                break;
        }
    }

    private void rotateAndMoveToPest(Entity target) {
        if (target == null || mc.thePlayer == null) return;

        // Only start a new rotation when previous is done — avoids constant-restart jitter
        if (!RotationHandler.getInstance().isRotating()) {
            rotateToPest(target);
        }

        // If pathfinder previously failed, use direct flight
        if (pathfinderFailed || FlyPathFinderExecutor.getInstance().getState() == FlyPathFinderExecutor.State.FAILED) {
            pathfinderFailed = true;
            FlyPathFinderExecutor.getInstance().stop();
            directFlyTo(target.posX, target.posY + 1.0, target.posZ, FarmHelperConfig.pestVacuumRange - 1.0);
            return;
        }

        // Try FlyPathFinderExecutor
        if (!FlyPathFinderExecutor.getInstance().isRunning()) {
            if (mc.thePlayer.capabilities.allowFlying && !mc.thePlayer.capabilities.isFlying) {
                mc.thePlayer.capabilities.isFlying = true;
            }
            FlyPathFinderExecutor.getInstance().setSprinting(FarmHelperConfig.sprintWhileFlying);
            FlyPathFinderExecutor.getInstance().setUseAOTV(InventoryUtils.hasItemInHotbar("Aspect of the Void", "Aspect of the End"));
            FlyPathFinderExecutor.getInstance().findPath(target, true, true, 1.5f, true);
        }
    }

    private void directFlyTo(double targetX, double targetY, double targetZ, double stopDistance) {
        if (mc.thePlayer == null) return;
        double dx = targetX - mc.thePlayer.posX;
        double dy = targetY - mc.thePlayer.posY;
        double dz = targetZ - mc.thePlayer.posZ;
        double distTotal = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (mc.thePlayer.capabilities.allowFlying && !mc.thePlayer.capabilities.isFlying) {
            mc.thePlayer.capabilities.isFlying = true;
        }

        if (distTotal <= stopDistance) {
            KeyBindUtils.stopMovement();
            return;
        }

        // Smooth horizontal yaw via RotationHandler — avoids fighting with RotationHandler's own interpolation
        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        com.jelly.farmhelperv2.util.helper.Rotation targetRot = new com.jelly.farmhelperv2.util.helper.Rotation(targetYaw, 0.0F);
        if (!RotationHandler.getInstance().isRotating()) {
            RotationHandler.getInstance().easeTo(new RotationConfiguration(targetRot, 150L, null));
        }

        // Hold forward to fly in the horizontal direction we are facing
        KeyBindUtils.holdThese(mc.gameSettings.keyBindForward);
        if (FarmHelperConfig.sprintWhileFlying) {
            mc.thePlayer.setSprinting(true);
        }

        // Vertical movement via jump/sneak (creative flight does NOT follow pitch for forward movement)
        if (dy > 3.0) {
            KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindJump, true);
            KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindSneak, false);
        } else if (dy < -3.0) {
            KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindSneak, true);
            KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindJump, false);
        } else {
            KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindJump, false);
            KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindSneak, false);
        }
    }

    @SubscribeEvent
    public void onSpawnObject(SpawnObjectEvent event) {
        if (mc.thePlayer == null || !isRunning()) return;
        if (event.type != 76) return; // 76 = Firework Rocket entity
        if (currentState != State.WAIT_FOR_FIREWORK) return;

        double dist = mc.thePlayer.getDistance(event.pos.xCoord, event.pos.yCoord, event.pos.zCoord);
        if (dist < 10.0) {
            double speed = Math.sqrt(event.speedX * event.speedX + event.speedZ * event.speedZ);
            if (speed > 0.05) {
                double normX = event.speedX / speed;
                double normZ = event.speedZ / speed;
                fireworkWaypoint = new Vec3(mc.thePlayer.posX + normX * 45.0, 85.0, mc.thePlayer.posZ + normZ * 45.0);
            } else {
                fireworkWaypoint = new Vec3(event.pos.xCoord, 85.0, event.pos.zCoord);
            }
            logDebug("Caught firework rocket from vacuum! Waypoint: " + fireworkWaypoint);
        }
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    public void onSpawnParticle(SpawnParticleEvent event) {
        if (mc.thePlayer == null || !isRunning()) return;
        if (currentState != State.WAIT_FOR_FIREWORK) return;

        EnumParticleTypes type = event.getParticleTypes();
        if (type != EnumParticleTypes.VILLAGER_ANGRY && type != EnumParticleTypes.FIREWORKS_SPARK && type != EnumParticleTypes.CRIT) {
            return;
        }

        if (firstParticleLocation == null) {
            if (mc.thePlayer.getPositionVector().distanceTo(event.getPos()) < 5.0) {
                firstParticleLocation = event.getPos();
                lastParticleLocation = firstParticleLocation;
            }
            return;
        }

        double dist = lastParticleLocation.distanceTo(event.getPos());
        if (dist > 0.3 && dist < 3.0) {
            lastParticleLocation = event.getPos();
            Vec3 dir = lastParticleLocation.subtract(firstParticleLocation).normalize();
            fireworkWaypoint = new Vec3(mc.thePlayer.posX + dir.xCoord * 45.0, 85.0, mc.thePlayer.posZ + dir.zCoord * 45.0);
            logDebug("Caught firework particle trail! Waypoint: " + fireworkWaypoint);
        }
    }

    private void rotateToPest(Entity target) {
        if (target == null || mc.thePlayer == null) return;
        RotationHandler.getInstance().easeTo(new RotationConfiguration(
                new Target(target),
                180L,
                null
        ).followTarget(true));
    }

    private void restoreState() {
        FlyPathFinderExecutor.getInstance().stop();
        KeyBindUtils.stopMovement();
        KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindUseItem, false);
        resetFireworkInfo();
        targetWaypoint = null;
        pathfinderFailed = false;
        if (originalHotbarSlot != -1 && mc.thePlayer != null) {
            mc.thePlayer.inventory.currentItem = originalHotbarSlot;
            originalHotbarSlot = -1;
        }
    }

    private void handleFailure(String reason) {
        logDebug("FakePixel Pest Controller failure: " + reason);
        currentState = State.FAILED;
    }

    private void logDebug(String message) {
        if (FarmHelperConfig.pestDebugLogging) {
            LogUtils.sendDebug("[Pest] " + message);
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!FarmHelperConfig.fakePixelMode) return;
        if (!FarmHelperConfig.pestsESP && !isRunning()) return;

        double d0 = mc.getRenderManager().viewerPosX;
        double d1 = mc.getRenderManager().viewerPosY;
        double d2 = mc.getRenderManager().viewerPosZ;

        List<PestInfo> cached = detector.getCachedPests();
        for (PestInfo pest : cached) {
            if (pest == null || pest.getEntity() == null || pest.getEntity().isDead) continue;
            if (killedEntities.contains(pest.getEntity())) continue;

            Entity e = pest.getEntity();
            double dist = mc.thePlayer.getDistanceToEntity(e);
            boolean inRange = dist <= FarmHelperConfig.pestVacuumRange;

            AxisAlignedBB bb = new AxisAlignedBB(
                    e.posX - 0.4, e.posY, e.posZ - 0.4,
                    e.posX + 0.4, e.posY + 1.0, e.posZ + 0.4
            ).offset(-d0, -d1, -d2);

            Color col = inRange ? new Color(0, 255, 100, 80) : new Color(255, 60, 60, 80);
            RenderUtils.drawBox(bb, col);
            RenderUtils.drawText(
                    pest.getPestType() + String.format(" (%.1fm)", dist),
                    (float) e.posX, (float) (e.posY + 1.2), (float) e.posZ, 1.0f
            );

            if (FarmHelperConfig.pestsTracers) {
                RenderUtils.drawTracer(
                        new Vec3(e.posX, e.posY + 0.5, e.posZ),
                        FarmHelperConfig.pestsTracersColor.toJavaColor()
                );
            }
        }
    }

    public State getCurrentState() {
        return currentState;
    }
}
