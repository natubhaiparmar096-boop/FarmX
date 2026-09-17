package com.jelly.farmhelperv2.macro;

import cc.polyfrost.oneconfig.utils.Multithreading;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.event.ReceivePacketEvent;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.failsafe.impl.RotationFailsafe;
import com.jelly.farmhelperv2.feature.FeatureManager;
import com.jelly.farmhelperv2.failsafe.impl.RotationFailsafe;
import com.jelly.farmhelperv2.feature.impl.DesyncChecker;
import com.jelly.farmhelperv2.feature.impl.LagDetector;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.handler.RotationHandler;
import com.jelly.farmhelperv2.util.*;
import com.jelly.farmhelperv2.util.helper.Clock;
import com.jelly.farmhelperv2.util.helper.Rotation;
import com.jelly.farmhelperv2.util.helper.RotationConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public abstract class AbstractMacro {
    public static final Minecraft mc = Minecraft.getMinecraft();
    private final RotationHandler rotation = RotationHandler.getInstance();
    private final Clock rewarpDelay = new Clock();
    private final Clock delayBeforeBreakTime = new Clock();
    private final Clock breakTime = new Clock();

    public State currentState = State.NONE;
    public State previousState = State.NONE;
    private boolean enabled = false;
    private Optional<SavedState> savedState = Optional.empty();
    private boolean restoredState = false;
    private int layerY = 0;
    private Optional<Float> yaw = Optional.empty();
    private Optional<Float> pitch = Optional.empty();
    private Optional<Float> closest90Deg = Optional.empty();
    private boolean rotated = false;
    private RewarpState rewarpState = RewarpState.NONE;
    private WalkingDirection walkingDirection = WalkingDirection.X;
    private int previousWalkingCoord = 0;

    // Explicit getters (replacing class-level @Getter)
    public RotationHandler getRotation() { return rotation; }
    public Clock getRewarpDelay() { return rewarpDelay; }
    public Clock getDelayBeforeBreakTime() { return delayBeforeBreakTime; }
    public Clock getBreakTime() { return breakTime; }
    public State getCurrentState() { return currentState; }
    public State getPreviousState() { return previousState; }
    public boolean isEnabled() { return enabled; }
    public Optional<SavedState> getSavedState() { return savedState; }
    public boolean isRestoredState() { return restoredState; }
    public int getLayerY() { return layerY; }
    public Optional<Float> getClosest90Deg() { return closest90Deg; }
    public boolean isRotated() { return rotated; }
    public RewarpState getRewarpState() { return rewarpState; }
    public WalkingDirection getWalkingDirection() { return walkingDirection; }
    public int getPreviousWalkingCoord() { return previousWalkingCoord; }
    public Clock getCheckOnSpawnClock() { return checkOnSpawnClock; }

    // Explicit setters (replacing field-level @Setter)
    public void setCurrentState(State currentState) { this.currentState = currentState; }
    public void setPreviousState(State previousState) { this.previousState = previousState; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setSavedState(Optional<SavedState> savedState) { this.savedState = savedState; }
    public void setRestoredState(boolean restoredState) { this.restoredState = restoredState; }
    public void setLayerY(int layerY) { this.layerY = layerY; }
    public void setClosest90Deg(Optional<Float> closest90Deg) { this.closest90Deg = closest90Deg; }
    public void setRotated(boolean rotated) { this.rotated = rotated; }
    public void setRewarpState(RewarpState rewarpState) { this.rewarpState = rewarpState; }
    public void setWalkingDirection(WalkingDirection walkingDirection) { this.walkingDirection = walkingDirection; }
    public void setPreviousWalkingCoord(int previousWalkingCoord) { this.previousWalkingCoord = previousWalkingCoord; }

    public boolean isEnabledAndNoFeature() {
        return enabled && !FeatureManager.getInstance().shouldPauseMacroExecution();
    }

    public boolean isPaused() {
        return !enabled;
    }

    private final Clock checkOnSpawnClock = new Clock();

    private boolean sentWarning = false;

    public boolean isYawSet() {
        return FarmHelperConfig.customYaw || yaw.isPresent();
    }

    public boolean isPitchSet() {
        return FarmHelperConfig.customPitch || pitch.isPresent();
    }

    public float getYaw() {
        if (FarmHelperConfig.customYaw) {
            return FarmHelperConfig.customYawLevel;
        }
        return yaw.orElse(0f);
    }

    public float getPitch() {
        if (FarmHelperConfig.customPitch) {
            return FarmHelperConfig.customPitchLevel;
        }
        return pitch.orElse(0f);
    }

    public void setYaw(float yaw) {
        this.yaw = Optional.of(yaw);
    }

    public void setPitch(float pitch) {
        this.pitch = Optional.of(pitch);
    }

    public void onTick() {
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent() || FailsafeManager.getInstance().getChooseEmergencyDelay().isScheduled()) {
            if (!sentWarning) {
                LogUtils.sendWarning("Failsafe is running! Blocking main onTick event!");
                sentWarning = true;
            }
            return;
        }
        if (!delayBeforeBreakTime.passed()) {
            LogUtils.sendDebug("Delay before break time: " + delayBeforeBreakTime.getRemainingTime());
            return;
        }
        if (!breakTime.passed()) {
            System.out.println(breakTime.getRemainingTime());
            KeyBindUtils.stopMovement();
            LogUtils.sendDebug("Blocking movement due to break time!");
            GameStateHandler.getInstance().scheduleNotMoving();
            return;
        }
        if (mc.thePlayer.capabilities.isFlying) {
            LogUtils.sendDebug("Flying");
            KeyBindUtils.holdThese(mc.gameSettings.keyBindSneak);
            return;
        }
        if (!PlayerUtils.isRewarpLocationSet()) {
            LogUtils.sendError("Your rewarp position is not set!");
            MacroHandler.getInstance().disableMacro();
            return;
        }

        if (PlayerUtils.isStandingOnRewarpLocation() && !FailsafeManager.getInstance().triggeredFailsafe.isPresent()) {
            if (GameStateHandler.getInstance().notMoving() && KeyBindUtils.getHoldingKeybinds().length > 0) {
                KeyBindUtils.stopMovement();
            }
            if (!GameStateHandler.getInstance().notMoving()) {
                return;
            }
            if (GameStateHandler.getInstance().canRewarp()) {
                MacroHandler.getInstance().triggerWarpGarden(false, true);
                checkOnSpawnClock.schedule(5000);
            }
            return;
        }

        if (PlayerUtils.isStandingOnSpawnPoint() && !FailsafeManager.getInstance().triggeredFailsafe.isPresent() && GameStateHandler.getInstance().notMoving() && checkOnSpawnClock.passed()) {
            if (MacroHandler.getInstance().canTriggerFeatureAfterWarp(true)) {
                rotated = false;
                return;
            }
            checkOnSpawnClock.schedule(5000);
        }

        if (mc.thePlayer.getPosition().getY() < 0) {
            LogUtils.sendError("Build a wall between the rewarp point and the void to prevent falling out of the garden! If it's there already, then you've been macro checked, good luck.");
            MacroHandler.getInstance().triggerWarpGarden(true, false);
            FailsafeManager.getInstance().possibleDetection(RotationFailsafe.getInstance());
            return;
        }

        if (getRotation().isRotating()) {
            if (!mc.gameSettings.keyBindSneak.isKeyDown())
                KeyBindUtils.stopMovement();
            GameStateHandler.getInstance().scheduleNotMoving();
            return;
        }

        if (FarmHelperConfig.autoSwitchTool) {
            FarmHelperConfig.CropEnum crop = PlayerUtils.getCropBasedOnMouseOver();
            if (crop != FarmHelperConfig.CropEnum.NONE && crop != MacroHandler.getInstance().getCrop()) {
                LogUtils.sendWarning("Crop changed from " + MacroHandler.getInstance().getCrop() + " to " + crop);
                MacroHandler.getInstance().setCrop(crop);
            }
        }

        if (rewarpDelay.isScheduled() && !rewarpDelay.passed()) {
            return;
        }

        if (rewarpState == RewarpState.TELEPORTED) {
            // rotate
            if (rotated && !LagDetector.getInstance().isLagging() && mc.thePlayer.onGround) {
                LogUtils.sendDebug("Rotated after warping.");
                rewarpState = RewarpState.POST_REWARP;
                Multithreading.schedule(() -> rewarpState = RewarpState.NONE, 600, TimeUnit.MILLISECONDS);
                rewarpDelay.schedule(FarmHelperConfig.getRandomTimeBetweenChangingRows());
                return;
            } else if (rotated && (LagDetector.getInstance().isLagging() || !mc.thePlayer.onGround)) {
                LogUtils.sendDebug("Rotated after warping, but player is not on ground or lagging.");
                KeyBindUtils.holdThese(mc.gameSettings.keyBindSneak);
                return;
            } else if (rotated) {
                LogUtils.sendDebug("Rotated after warping, but player is not on ground or lagging.");
                return;
            }

            if (FarmHelperConfig.rotateAfterWarped) {
                doAfterRewarpRotation();
            }

            if (shouldRotateAfterWarp()) {
                Rotation newRotation = new Rotation(getYaw(), getPitch());
                Rotation curretnRotation = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
                Rotation neededChange = RotationHandler.getInstance().getNeededChange(newRotation, curretnRotation);
                if (FarmHelperConfig.dontFixAfterWarping) {
                    float pythagoras = (float) Math.sqrt(Math.pow(neededChange.getYaw(), 2) + Math.pow(neededChange.getPitch(), 2));
                    if (pythagoras < 1) {
                        LogUtils.sendDebug("Not rotating after warping.");
                        rotated = true;
                        setLayerY(mc.thePlayer.getPosition().getY());
                        return;
                    }
                }
                rotation.easeTo(new RotationConfiguration(
                        newRotation, FarmHelperConfig.getRandomRotationTime() * (Math.abs(neededChange.getYaw()) > 90 ? 2 : 1), null
                ).easeOutBack(true));
                LogUtils.sendDebug("Rotating");
            }
            rotated = true;
            setLayerY(mc.thePlayer.getPosition().getY());
            return;
        } else if (rewarpState == RewarpState.TELEPORTING) {
            // teleporting
            return;
        }

        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) {
            LogUtils.sendDebug("Blocking changing movement due to emergency!");
            return;
        }

        PlayerUtils.getTool();

        // Update or invoke state, based on if player is moving or not
        if (GameStateHandler.getInstance().canChangeDirection()) {
            KeyBindUtils.stopMovement(FarmHelperConfig.holdLeftClickWhenChangingRow);
            if (getCurrentState().equals(State.DROPPING) && !mc.thePlayer.onGround) {
                return;
            }
            updateState();
            if (getCurrentState() == State.NONE) {
                return;
            }
            GameStateHandler.getInstance().setUpdatedState(true);
        } else {
            if (!mc.thePlayer.onGround && Math.abs(layerY - mc.thePlayer.posY) > 0.75 && mc.thePlayer.posY < 80 && !getCurrentState().equals(State.DROPPING)) {
                changeState(State.DROPPING);
                GameStateHandler.getInstance().setUpdatedState(true);
                GameStateHandler.getInstance().scheduleNotMoving();
            }
            invokeState();
        }
    }

    public void onLastRender() {
    }

    public void onChatMessageReceived(String msg) {
    }

    public void onOverlayRender(RenderGameOverlayEvent.Post event) {
    }

    public void onPacketReceived(ReceivePacketEvent event) {
        /*
        if (!(event.packet instanceof S08PacketPlayerPosLook)) return;
        if (!MacroHandler.getInstance().isTeleporting()) return;

        S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) event.packet;
        Rotation packetRotation = new Rotation(packet.getYaw(), packet.getPitch());
        Rotation currentRotation = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        Rotation neededChange = RotationHandler.getInstance().getNeededChange(packetRotation, currentRotation);
        double diff = Math.abs(neededChange.getYaw()) + Math.abs(neededChange.getPitch());
        if (diff > 5) {
            LogUtils.sendWarning("Your rotation hasn't been changed after rewarp! Disable any mod that blocks rotation packets or fix your /setspawn so look directly at the crops!");
        }
        */
    }

    public abstract void updateState();

    public abstract void invokeState();

    public void onEnable() {

        GameStateHandler.getInstance().scheduleRewarp();
        if (!savedState.isPresent()) {
            yaw = Optional.empty();
            pitch = Optional.empty();
        }
        if (FarmHelperConfig.customPitch) {
            setPitch(FarmHelperConfig.customPitchLevel);
        }
        if (FarmHelperConfig.customYaw) {
            setYaw(FarmHelperConfig.customYawLevel);
        }
        FarmHelperConfig.CropEnum crop;
        if (savedState.isPresent()) {
            LogUtils.sendDebug("Restoring state: " + savedState.get());
            changeState(savedState.get().getState());
            setYaw(savedState.get().getYaw());
            setPitch(savedState.get().getPitch());
            setClosest90Deg(savedState.get().getClosest90Deg());
            crop = savedState.get().getCrop();
            restoredState = true;
            savedState = Optional.empty();
            GameStateHandler.getInstance().setUpdatedState(true);
            float randomTime = FarmHelperConfig.getRandomTimeBetweenChangingRows();
            GameStateHandler.getInstance().scheduleNotMoving((int) Math.max(randomTime, 150));
        } else {
            crop = PlayerUtils.getFarmingCrop();
        }
        LogUtils.sendDebug("Crop: " + crop);
        MacroHandler.getInstance().setCrop(crop);
        PlayerUtils.getTool();
        if (!closest90Deg.isPresent())
            setClosest90Deg(Optional.of(AngleUtils.getClosest(getYaw())));
        setEnabled(true);
        setLayerY(mc.thePlayer.getPosition().getY());
        if (getCurrentState() == null)
            changeState(State.NONE);
    }

    public void onDisable() {
        DesyncChecker.getInstance().getClickedBlocks().clear();
        KeyBindUtils.stopMovement();
        changeState(State.NONE);
        setRewarpState(RewarpState.NONE);
        setClosest90Deg(Optional.empty());
        rotation.reset();
        rewarpDelay.reset();
        sentWarning = false;
        yaw = Optional.empty();
        pitch = Optional.empty();
        setEnabled(false);
    }

    public void saveState() {
        if (!savedState.isPresent()) {
            LogUtils.sendDebug("Saving state: " + currentState);
            savedState = Optional.of(new SavedState(currentState, getYaw(), getPitch(), closest90Deg.orElse(AngleUtils.getClosest()), MacroHandler.getInstance().getCrop()));
        }
    }

    public void changeState(State state) {
        LogUtils.sendDebug("Changing state from " + currentState + " to " + state);
        setPreviousState(currentState);
        setCurrentState(state);
    }

    public abstract void actionAfterTeleport();

    public State calculateDirection() {
        if (BlockUtils.getRelativeBlock(-1, 0, 0).equals(Blocks.air) && BlockUtils.getRelativeBlock(-1, -1, 0).equals(Blocks.air)) {
            return State.RIGHT;
        }
        if (BlockUtils.getRelativeBlock(1, 0, 0).equals(Blocks.air) && BlockUtils.getRelativeBlock(1, -1, 0).equals(Blocks.air)) {
            return State.LEFT;
        }
        return State.NONE;
    }

    public void doAfterRewarpRotation() {
        setYaw(AngleUtils.get360RotationYaw(getYaw() + 180));
        setClosest90Deg(Optional.of(AngleUtils.getClosest(getYaw())));
    }

    protected void setWalkingDirection() {
        int currentX = BlockUtils.getRelativeBlockPos(0, 0, 0, getYaw()).getX();
        int leftX = BlockUtils.getRelativeBlockPos(1, 0, 0, getYaw()).getX();
        int rightX = BlockUtils.getRelativeBlockPos(-1, 0, 0, getYaw()).getX();
        if (currentX == leftX || currentX == rightX) {
            setWalkingDirection(WalkingDirection.Z);
        } else {
            setWalkingDirection(WalkingDirection.X);
        }
        LogUtils.sendDebug("Walking direction: " + getWalkingDirection());
        setPreviousWalkingCoord(getWalkingDirection() == WalkingDirection.X ? mc.thePlayer.getPosition().getZ() : currentX);
    }

    public boolean shouldRotateAfterWarp() {
        return true;
    }

    public void setBreakTime(double time, double timeBefore) {
        breakTime.schedule((long) time);
        delayBeforeBreakTime.schedule((long) timeBefore);
    }

    public enum State {
        // Add default values like NONE and DROPPING
        // DO NOT REARRAGE, IT WILL BREAK PEST FARMER IF YOU DO - osama
        NONE,
        DROPPING,
        SWITCHING_SIDE,
        SWITCHING_LANE,
        LEFT,
        RIGHT,
        BACKWARD,
        FORWARD,

        A,
        D,
        S,
        W
    }

    public enum RewarpState {
        NONE,
        TELEPORTING,
        TELEPORTED,
        POST_REWARP
    }

    public enum WalkingDirection {
        X,
        Z,
    }

    public static class SavedState {
        private State state;
        private float yaw;
        private float pitch;
        private Optional<Float> closest90Deg;
        private FarmHelperConfig.CropEnum crop;

        public State getState() { return state; }
        public void setState(State state) { this.state = state; }
        public float getYaw() { return yaw; }
        public void setYaw(float yaw) { this.yaw = yaw; }
        public float getPitch() { return pitch; }
        public void setPitch(float pitch) { this.pitch = pitch; }
        public Optional<Float> getClosest90Deg() { return closest90Deg; }
        public void setClosest90Deg(Optional<Float> closest90Deg) { this.closest90Deg = closest90Deg; }
        public FarmHelperConfig.CropEnum getCrop() { return crop; }
        public void setCrop(FarmHelperConfig.CropEnum crop) { this.crop = crop; }

        public SavedState(State state, float yaw, float pitch, float closest90Deg, FarmHelperConfig.CropEnum crop) {
            this.state = state;
            this.yaw = yaw;
            this.pitch = pitch;
            this.closest90Deg = Optional.of(closest90Deg);
            this.crop = crop;
        }

        @Override
        public String toString() {
            return "SavedState{" +
                    "state=" + state +
                    ", yaw=" + yaw +
                    ", pitch=" + pitch +
                    ", closest90Deg=" + closest90Deg +
                    ", crop=" + crop +
                    '}';
        }
    }
}
