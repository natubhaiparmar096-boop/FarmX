package com.jelly.farmhelperv2.feature.pest;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.feature.FeatureManager;
import com.jelly.farmhelperv2.feature.IFeature;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.handler.RotationHandler;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import com.jelly.farmhelperv2.util.PlotUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import com.jelly.farmhelperv2.util.helper.RotationConfiguration;
import com.jelly.farmhelperv2.util.helper.Target;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
        STOP_FARMING,
        FIND_PEST,
        MOVE_TO_PEST,
        ATTACK_VACUUM,
        CONFIRM_PEST_DEAD,
        RETURN_TO_FARM,
        RESUME_FARMING,
        FAILED,
        COOLDOWN
    }

    private final Minecraft mc = Minecraft.getMinecraft();
    private final PestPlatformAdapter adapter = new FakePixelPestAdapter();
    private final PestDetector detector = new PestDetector(adapter);

    private State currentState = State.IDLE;
    private boolean enabled = false;
    private PestInfo targetPest = null;
    private int originalHotbarSlot = -1;
    private int retryCount = 0;

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
    public void start() {
        if (enabled) return;
        enabled = true;
        currentState = State.IDLE;
        retryCount = 0;
        targetPest = null;
        originalHotbarSlot = mc.thePlayer != null ? mc.thePlayer.inventory.currentItem : -1;
        logDebug("Pest Controller started.");
        IFeature.super.start();
    }

    @Override
    public void stop() {
        if (!enabled) return;
        logDebug("Pest Controller stopping.");
        restoreState();
        enabled = false;
        currentState = State.IDLE;
        targetPest = null;
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

        if (!isToggled()) {
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

        // Trigger check when farming
        if (!isRunning() && MacroHandler.getInstance().isMacroToggled() && MacroHandler.getInstance().isCurrentMacroEnabled()) {
            List<PestInfo> pests = detector.scanPests(false);
            if (!pests.isEmpty()) {
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
                logDebug("Pest detected! Pausing farming macro.");
                if (MacroHandler.getInstance().isMacroToggled()) {
                    MacroHandler.getInstance().pauseMacro();
                }
                KeyBindUtils.stopMovement();
                currentState = State.STOP_FARMING;
                stateTimer.schedule(1000);
                break;

            case STOP_FARMING:
                if (stateTimer.passed()) {
                    currentState = State.FIND_PEST;
                }
                break;

            case FIND_PEST:
                targetPest = detector.selectTargetPest();
                if (targetPest == null || !targetPest.isAlive()) {
                    logDebug("No valid target pest found.");
                    currentState = State.RETURN_TO_FARM;
                } else {
                    logDebug("Target pest selected: " + targetPest.getPestType() + " at dist " + String.format("%.2f", targetPest.getDistance()));
                    if (mc.thePlayer != null) {
                        originalHotbarSlot = mc.thePlayer.inventory.currentItem;
                    }
                    currentState = State.MOVE_TO_PEST;
                    stateTimer.schedule(FarmHelperConfig.pestInteractionTimeoutMs);
                }
                break;

            case MOVE_TO_PEST:
                if (targetPest == null || adapter.isPestRemoved(targetPest)) {
                    currentState = State.CONFIRM_PEST_DEAD;
                    break;
                }
                double dist = mc.thePlayer.getDistanceToEntity(targetPest.getEntity());
                if (dist <= FarmHelperConfig.pestVacuumRange) {
                    KeyBindUtils.stopMovement();
                    currentState = State.ATTACK_VACUUM;
                    attackTimer.schedule(100);
                    stateTimer.schedule(FarmHelperConfig.pestInteractionTimeoutMs);
                } else {
                    rotateAndMoveToPest(targetPest.getEntity());
                    if (stateTimer.passed()) {
                        handleFailure("Timeout moving to pest");
                    }
                }
                break;

            case ATTACK_VACUUM:
                if (targetPest == null || adapter.isPestRemoved(targetPest)) {
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
                    attackTimer.schedule(300);
                    logDebug("Interacting/Vacuuming pest: " + targetPest.getPestType());
                }

                if (stateTimer.passed()) {
                    if (retryCount < FarmHelperConfig.pestMaxRetryCount) {
                        retryCount++;
                        logDebug("Retrying pest collection (attempt " + retryCount + ")");
                        stateTimer.schedule(FarmHelperConfig.pestInteractionTimeoutMs);
                    } else {
                        handleFailure("Pest collection retry limit reached");
                    }
                }
                break;

            case CONFIRM_PEST_DEAD:
                logDebug("Confirming pest removal...");
                if (targetPest == null || adapter.isPestRemoved(targetPest)) {
                    logDebug("Pest confirmed removed!");
                    currentState = State.RETURN_TO_FARM;
                } else {
                    currentState = State.ATTACK_VACUUM;
                }
                break;

            case RETURN_TO_FARM:
                logDebug("Restoring state and returning to farming.");
                restoreState();
                currentState = State.RESUME_FARMING;
                stateTimer.schedule(500);
                break;

            case RESUME_FARMING:
                if (MacroHandler.getInstance().isMacroToggled()) {
                    MacroHandler.getInstance().resumeMacro();
                }
                currentState = State.COOLDOWN;
                cooldownTimer.schedule(2000);
                break;

            case COOLDOWN:
                if (cooldownTimer.passed()) {
                    stop();
                }
                break;

            case FAILED:
                restoreState();
                logDebug("Entering failed state recovery.");
                stop();
                break;

            case IDLE:
            default:
                break;
        }
    }

    private void rotateAndMoveToPest(Entity target) {
        if (target == null) return;
        rotateToPest(target);
        KeyBindUtils.setKeyBindState(mc.gameSettings.keyBindForward, true);
    }

    private void rotateToPest(Entity target) {
        if (target == null || mc.thePlayer == null) return;
        RotationHandler.getInstance().easeTo(new RotationConfiguration(
                new Target(target),
                200L,
                null
        ).followTarget(true));
    }

    private void restoreState() {
        KeyBindUtils.stopMovement();
        if (originalHotbarSlot != -1 && mc.thePlayer != null) {
            mc.thePlayer.inventory.currentItem = originalHotbarSlot;
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

    public State getCurrentState() {
        return currentState;
    }
}
