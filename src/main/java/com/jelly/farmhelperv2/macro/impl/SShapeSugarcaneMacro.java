package com.jelly.farmhelperv2.macro.impl;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.macro.AbstractMacro;
import com.jelly.farmhelperv2.util.AngleUtils;
import com.jelly.farmhelperv2.util.BlockUtils;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.helper.Rotation;
import com.jelly.farmhelperv2.util.helper.RotationConfiguration;
import net.minecraft.client.settings.KeyBinding;

import java.util.Optional;

public class SShapeSugarcaneMacro extends AbstractMacro {

    public double rowStartX = 0;
    public double rowStartZ = 0;

    private boolean strafeMode() {
        return FarmHelperConfig.sugarcaneControlMode == 1;
    }

    @Override
    public void updateState() {
        if (currentState == null)
            changeState(State.NONE);
        if (strafeMode()) {
            updateStateStrafe();
        } else {
            updateStateClassic();
        }
    }

    private void updateStateClassic() {
        switch (currentState) {
            case S: {
                if (hasWall(0, -1, getYaw() - 45f) &&
                        hasWall(0, -1, getYaw() + 45f)) {

                    boolean preferA = getNearestSideWall(getYaw() + 45, -1) == -999;
                    boolean preferD = getNearestSideWall(getYaw() - 45, 1) == -999;
                    if (FarmHelperConfig.sugarcaneInvertLaneSide) {
                        boolean tmp = preferA;
                        preferA = preferD;
                        preferD = tmp;
                    }
                    if (preferA) {
                        changeState(State.A);
                    }
                    if (preferD) {
                        changeState(State.D);
                    }
                }
                break;
            }
            case A:
            case D: {
                changeState(State.S);
                break;
            }
            case DROPPING: {
                handleDropping();
                break;
            }
            case NONE: {
                changeState(calculateDirection());
                break;
            }
            default: {
                LogUtils.sendDebug("This shouldn't happen, but it did...");
                changeState(State.NONE);
            }
        }
    }

    /**
     * A = forward leg, D = back leg, S = lane switch (keys remappable).
     */
    private void updateStateStrafe() {
        switch (currentState) {
            case A: { // forward leg
                if (strafeBlockedLeft()) {
                    changeState(State.S);
                }
                break;
            }
            case D: { // back leg
                if (strafeBlockedRight()) {
                    changeState(State.S);
                }
                break;
            }
            case S: { // lane switch → opposite leg
                State prev = getPreviousState();
                if (prev == State.A) {
                    changeState(State.D);
                } else if (prev == State.D) {
                    changeState(State.A);
                } else {
                    changeState(calculateDirectionStrafe());
                }
                break;
            }
            case DROPPING: {
                handleDropping();
                break;
            }
            case NONE: {
                changeState(calculateDirectionStrafe());
                break;
            }
            default: {
                changeState(State.NONE);
            }
        }
    }

    private void handleDropping() {
        LogUtils.sendDebug("On Ground: " + mc.thePlayer.onGround);
        if (mc.thePlayer.onGround && Math.abs(getLayerY() - mc.thePlayer.getPosition().getY()) > 1.5) {
            if (FarmHelperConfig.rotateAfterDrop && !getRotation().isRotating()) {
                LogUtils.sendDebug("Rotating 180");
                getRotation().reset();
                setYaw(AngleUtils.getClosestDiagonal(getYaw() + 180));
                setClosest90Deg(Optional.of(AngleUtils.getClosest(getYaw())));
                getRotation().easeTo(
                        new RotationConfiguration(
                                new Rotation(getYaw(), getPitch()),
                                (long) (400 + Math.random() * 300), null
                        ).easeOutBack(true)
                );
            }
            KeyBindUtils.stopMovement();
            changeState(State.NONE);
            setLayerY(mc.thePlayer.getPosition().getY());
        } else {
            GameStateHandler.getInstance().scheduleNotMoving();
        }
    }

    private boolean strafeBlockedLeft() {
        return hasWall(-1, 0, getYaw()) || (hasWall(-1, -1, getYaw()) && hasWall(-1, 1, getYaw()));
    }

    private boolean strafeBlockedRight() {
        return hasWall(1, 0, getYaw()) || (hasWall(1, -1, getYaw()) && hasWall(1, 1, getYaw()));
    }

    @Override
    public void invokeState() {
        if (currentState == null) return;
        switch (currentState) {
            case NONE:
                break;
            case A:
                holdMove(strafeMode()
                        ? KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneStrafeForwardKey)
                        : KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneClassicLaneLeftKey));
                break;
            case D:
                holdMove(strafeMode()
                        ? KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneStrafeBackKey)
                        : KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneClassicLaneRightKey));
                break;
            case S:
                holdMove(strafeMode()
                        ? KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneStrafeLaneKey)
                        : KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneClassicRowKey));
                break;
            case DROPPING:
                if (mc.thePlayer.onGround && Math.abs(getLayerY() - mc.thePlayer.getPosition().getY()) <= 1.5) {
                    LogUtils.sendDebug("Dropping done, but didn't drop high enough to rotate!");
                    setLayerY(mc.thePlayer.getPosition().getY());
                    changeState(State.NONE);
                }
                break;
            default:
                break;
        }
    }

    private void holdMove(KeyBinding moveKey) {
        KeyBindUtils.holdThese(moveKey, mc.gameSettings.keyBindAttack);
    }

    @Override
    public void actionAfterTeleport() {
        setLayerY(mc.thePlayer.getPosition().getY());
        rowStartX = mc.thePlayer.posX;
        rowStartZ = mc.thePlayer.posZ;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!isPitchSet()) {
            setPitch((float) (Math.random() * 1) - 0.5f);
        }
        if (!isYawSet()) {
            setYaw(AngleUtils.getClosestDiagonal());
            setClosest90Deg(Optional.of(AngleUtils.getClosest(getYaw())));
        }
        rowStartX = mc.thePlayer.posX;
        rowStartZ = mc.thePlayer.posZ;
        if (MacroHandler.getInstance().isTeleporting()) return;
        setRestoredState(false);
        if (FarmHelperConfig.dontFixAfterWarping && Math.abs(getYaw() - AngleUtils.get360RotationYaw()) < 0.1) return;
        getRotation().easeTo(
                new RotationConfiguration(
                        new Rotation(getYaw(), getPitch()),
                        FarmHelperConfig.getRandomRotationTime(), null
                ).easeOutBack(!MacroHandler.getInstance().isResume())
        );
    }

    @Override
    public State calculateDirection() {
        if (strafeMode()) {
            return calculateDirectionStrafe();
        }
        if (BlockUtils.isWater(BlockUtils.getRelativeBlock(2, -1, 1, getYaw() - 45f)) || BlockUtils.isWater(BlockUtils.getRelativeBlock(2, 0, 1, getYaw() - 45f))
                || BlockUtils.isWater(BlockUtils.getRelativeBlock(-1, -1, 1, getYaw() - 45f)) || BlockUtils.isWater(BlockUtils.getRelativeBlock(-1, 0, 1, getYaw() - 45f)))
            if (!(hasWall(0, 1, getYaw() - 45f) && hasWall(-1, 0, getYaw() - 45f)))
                return FarmHelperConfig.sugarcaneInvertLaneSide ? State.D : State.A;
            else if (BlockUtils.isWater(BlockUtils.getRelativeBlock(2, -1, 1, getYaw() + 45f)) || BlockUtils.isWater(BlockUtils.getRelativeBlock(2, 0, 1, getYaw() + 45f))
                    || BlockUtils.isWater(BlockUtils.getRelativeBlock(-1, -1, 1, getYaw() + 45f)) || BlockUtils.isWater(BlockUtils.getRelativeBlock(-1, 0, 1, getYaw() + 45f)))
                if (!(hasWall(0, 1, getYaw() + 45f) && hasWall(11, 0, getYaw() + 45f)))
                    return FarmHelperConfig.sugarcaneInvertLaneSide ? State.A : State.D;
        return State.S;
    }

    private State calculateDirectionStrafe() {
        if (!strafeBlockedLeft()) {
            return State.A;
        }
        if (!strafeBlockedRight()) {
            return State.D;
        }
        return State.S;
    }

    boolean hasWall(int rightOffset, int frontOffset, float yaw) {
        return !BlockUtils.canWalkThrough(BlockUtils.getRelativeBlockPos(rightOffset, 0, frontOffset, yaw));
    }

    int getNearestSideWall(float yaw, int dir) {
        for (int i = 0; i < 8; i++) {
            if (hasWall(i * dir, 0, yaw)) return i;
        }
        return -999;
    }
}
