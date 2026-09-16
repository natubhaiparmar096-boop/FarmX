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
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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
        originalHotbarSlot = mc.thePlayer != null ? mc.thePlayer.inventory.currentItem : -1;
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
        originalHotbarSlot = mc.thePlayer != null ? mc.thePlayer.inventory.currentItem : -1;

        if (MacroHandler.getInstance().isMacroToggled()) {
            MacroHandler.getInstance().pauseMacro();
        }
        KeyBindUtils.stopMovement();

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
                List<PestInfo> livePests = detector.scanPests(true);
                livePests.removeIf(p -> p.getEntity() == null || p.getEntity().isDead || killedEntities.contains(p.getEntity()) || !p.isAlive());

                if (livePests.isEmpty()) {
                    logDebug("No valid target pests remaining.");
                    currentState = State.RETURN_TO_HOME;
                } else {
                    livePests.sort(Comparator.comparingDouble(PestInfo::getDistance));
                    targetPest = livePests.get(0);
                    logDebug("Target pest selected: " + targetPest.getPestType() + " at dist " + String.format("%.2f", targetPest.getDistance()));
                    if (mc.thePlayer != null && originalHotbarSlot == -1) {
                        originalHotbarSlot = mc.thePlayer.inventory.currentItem;
                    }
                    currentState = State.MOVE_TO_PEST;
                    retryCount = 0;
                    stateTimer.schedule(FarmHelperConfig.pestInteractionTimeoutMs + 7000);
                }
                break;

            case MOVE_TO_PEST:
                if (targetPest == null || adapter.isPestRemoved(targetPest) || targetPest.getEntity().isDead) {
                    FlyPathFinderExecutor.getInstance().stop();
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
                    rotateAndMoveToPest(targetPest.getEntity());
                    if (stateTimer.passed()) {
                        FlyPathFinderExecutor.getInstance().stop();
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
                    FlyPathFinderExecutor.getInstance().stop();
                    currentState = State.CONFIRM_PEST_DEAD;
                    break;
                }
                int vacuumSlot = adapter.findPestVacuumSlot();
                if (vacuumSlot != -1) {
                    mc.thePlayer.inventory.currentItem = vacuumSlot;
                }

                rotateToPest(targetPest.getEntity());

                if (attackTimer.passed()) {
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                    attackTimer.schedule(200);
                    logDebug("Vacuuming pest: " + targetPest.getPestType());
                }

                if (stateTimer.passed()) {
                    if (retryCount < FarmHelperConfig.pestMaxRetryCount) {
                        retryCount++;
                        logDebug("Retrying pest vacuuming (attempt " + retryCount + ")");
                        stateTimer.schedule(FarmHelperConfig.pestInteractionTimeoutMs);
                    } else {
                        logDebug("Pest collection retry limit reached, skipping.");
                        currentState = State.CONFIRM_PEST_DEAD;
                    }
                }
                break;

            case CONFIRM_PEST_DEAD:
                logDebug("Confirming pest removal...");
                if (targetPest != null && targetPest.getEntity() != null) {
                    killedEntities.add(targetPest.getEntity());
                    LogUtils.sendSuccess("[Pest Controller] Killed pest: " + targetPest.getPestType());
                }
                currentState = State.CHECK_NEXT_PEST;
                stateTimer.schedule(400);
                break;

            case CHECK_NEXT_PEST:
                targetPest = null;
                retryCount = 0;
                List<PestInfo> remaining = detector.scanPests(true);
                remaining.removeIf(p -> p.getEntity() == null || p.getEntity().isDead || killedEntities.contains(p.getEntity()) || !p.isAlive());

                if (!remaining.isEmpty()) {
                    logDebug(remaining.size() + " pests remaining. Targeting next pest!");
                    currentState = State.FIND_PEST;
                } else {
                    logDebug("All pests eliminated!");
                    LogUtils.sendSuccess("[Pest Controller] All pests eliminated!");
                    currentState = State.RETURN_TO_HOME;
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
                    LogUtils.sendSuccess("[Pest Controller] Manual pest hunt completed successfully!");
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
        rotateToPest(target);

        // Fallback: If pathfinder fails / retried, move directly using keyboard controls
        if (retryCount > 0) {
            FlyPathFinderExecutor.getInstance().stop();
            KeyBindUtils.holdThese(mc.gameSettings.keyBindForward);
            if (mc.thePlayer.capabilities.allowFlying && !mc.thePlayer.capabilities.isFlying) {
                mc.thePlayer.capabilities.isFlying = true;
            }
            if (target.posY > mc.thePlayer.posY + 0.5) {
                KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindJump, true);
            } else if (target.posY < mc.thePlayer.posY - 0.5) {
                KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindSneak, true);
            }
            return;
        }

        if (!FlyPathFinderExecutor.getInstance().isRunning()) {
            if (mc.thePlayer.capabilities.allowFlying && !mc.thePlayer.capabilities.isFlying) {
                mc.thePlayer.capabilities.isFlying = true;
            }
            FlyPathFinderExecutor.getInstance().setSprinting(FarmHelperConfig.sprintWhileFlying);
            FlyPathFinderExecutor.getInstance().setUseAOTV(InventoryUtils.hasItemInHotbar("Aspect of the Void", "Aspect of the End"));
            FlyPathFinderExecutor.getInstance().findPath(target, true, true, 1.5f, true);
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
