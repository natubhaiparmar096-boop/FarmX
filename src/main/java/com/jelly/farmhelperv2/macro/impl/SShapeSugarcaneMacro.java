package com.jelly.farmhelperv2.macro.impl;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.macro.AbstractMacro;
import com.jelly.farmhelperv2.util.AngleUtils;
import com.jelly.farmhelperv2.util.BlockUtils;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import com.jelly.farmhelperv2.util.helper.Rotation;
import com.jelly.farmhelperv2.util.helper.RotationConfiguration;
import net.minecraft.client.settings.KeyBinding;

import java.util.Optional;

/**
 * Sugar cane S-shape with three control modes:
 * <ul>
 *   <li>0 Classic — S along row, A/D lane</li>
 *   <li>1 Strafe — Go/Return legs + lane key</li>
 *   <li>2 Two-key — Go until wall, idle transit (no move key), then Return (e.g. D then S)</li>
 * </ul>
 * Keys are remappable; works for any facing (N/S/E/W) via yaw + Go/Return key choice.
 */
public class SShapeSugarcaneMacro extends AbstractMacro {

    public double rowStartX = 0;
    public double rowStartZ = 0;
    private final Clock transitClock = new Clock();

    private int mode() {
        return FarmHelperConfig.sugarcaneControlMode;
    }

    @Override
    public void updateState() {
        if (currentState == null)
            changeState(State.NONE);
        switch (mode()) {
            case 1:
                updateStateStrafe();
                break;
            case 2:
                updateStateTwoKey();
                break;
            default:
                updateStateClassic();
                break;
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
            case DROPPING:
                handleDropping();
                break;
            case NONE:
                changeState(calculateDirectionClassic());
                break;
            default:
                changeState(State.NONE);
                break;
        }
    }

    /** Go = State.A, Return = State.D, Lane = State.S */
    private void updateStateStrafe() {
        switch (currentState) {
            case A:
                if (blockedForKey(FarmHelperConfig.sugarcaneGoKey)) {
                    changeState(State.S);
                }
                break;
            case D:
                if (blockedForKey(FarmHelperConfig.sugarcaneReturnKey)) {
                    changeState(State.S);
                }
                break;
            case S: {
                State prev = getPreviousState();
                if (prev == State.A) {
                    changeState(State.D);
                } else if (prev == State.D) {
                    changeState(State.A);
                } else {
                    changeState(startLeg());
                }
                break;
            }
            case DROPPING:
                handleDropping();
                break;
            case NONE:
                changeState(startLeg());
                break;
            default:
                changeState(State.NONE);
                break;
        }
    }

    /**
     * Go until wall → switch directly to Return → Return until wall → switch directly to Go.
     * Continuously holds the active leg key (e.g. W then D) so movement through transition is seamless,
     * even when sugarcanes or blocks are present in the transition turn.
     */
    private void updateStateTwoKey() {
        switch (currentState) {
            case A: // Go leg
                if (blockedForKey(FarmHelperConfig.sugarcaneGoKey)) {
                    changeState(State.SWITCHING_LANE);
                }
                break;
            case D: // Return leg
                if (blockedForKey(FarmHelperConfig.sugarcaneReturnKey)) {
                    changeState(State.SWITCHING_LANE);
                }
                break;
            case SWITCHING_LANE: // Transit turn
                if (getPreviousState() == State.A) {
                    // Came from Go leg (W) -> holding Return key (D) in transit until Return direction (D) is unblocked
                    if (!blockedForKey(FarmHelperConfig.sugarcaneReturnKey)) {
                        changeState(State.D);
                    }
                } else if (getPreviousState() == State.D) {
                    // Came from Return leg (D) -> holding Go key (W) in transit until Go direction (W) is unblocked
                    if (!blockedForKey(FarmHelperConfig.sugarcaneGoKey)) {
                        changeState(State.A);
                    }
                } else {
                    changeState(startLeg());
                }
                break;
            case DROPPING:
                handleDropping();
                break;
            case NONE:
                changeState(startLeg());
                break;
            default:
                changeState(State.NONE);
                break;
        }
    }

    private State startLeg() {
        return FarmHelperConfig.sugarcaneStartOnGoLeg ? State.A : State.D;
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

    /** True if movement in that WASD direction is blocked relative to current yaw. */
    private boolean blockedForKey(int wasdIndex) {
        float yaw = getYaw();
        switch (wasdIndex) {
            case 0: // W
                return hasWall(0, 1, yaw);
            case 1: // S
                return hasWall(0, -1, yaw);
            case 2: // A
                return hasWall(-1, 0, yaw);
            case 3: // D
                return hasWall(1, 0, yaw);
            default:
                return hasWall(0, -1, yaw);
        }
    }

    @Override
    public void invokeState() {
        if (currentState == null) return;
        switch (currentState) {
            case NONE:
                break;
            case A: // Go (or classic lane-left)
                if (mode() == 0) {
                    holdMove(KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneClassicLaneLeftKey));
                } else {
                    holdMove(KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneGoKey));
                }
                break;
            case D: // Return (or classic lane-right)
                if (mode() == 0) {
                    holdMove(KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneClassicLaneRightKey));
                } else {
                    holdMove(KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneReturnKey));
                }
                break;
            case S: // Classic row / Strafe lane key
                if (mode() == 0) {
                    holdMove(KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneClassicRowKey));
                } else if (mode() == 1) {
                    holdMove(KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneLaneKey));
                }
                break;
            case SWITCHING_LANE:
                if (mode() == 2) {
                    if (getPreviousState() == State.A) {
                        holdMove(KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneReturnKey));
                    } else {
                        holdMove(KeyBindUtils.wasdFromIndex(FarmHelperConfig.sugarcaneGoKey));
                    }
                } else {
                    KeyBindUtils.stopMovement(FarmHelperConfig.holdLeftClickWhenChangingRow);
                    if (FarmHelperConfig.holdLeftClickWhenChangingRow) {
                        KeyBindUtils.holdThese(mc.gameSettings.keyBindAttack);
                    }
                }
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
        transitClock.reset();
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
        if (mode() == 1 || mode() == 2) {
            return startLeg();
        }
        return calculateDirectionClassic();
    }

    private State calculateDirectionClassic() {
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
