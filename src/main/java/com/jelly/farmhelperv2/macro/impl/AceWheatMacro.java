package com.jelly.farmhelperv2.macro.impl;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.macro.AbstractMacro;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import com.jelly.farmhelperv2.util.helper.Rotation;
import com.jelly.farmhelperv2.util.helper.RotationConfiguration;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

/**
 * Ace Wheat – continuous looping wheat pest-farm macro.
 *
 * State machine:
 *   START -> WD
 *   WD + iron-door collision -> WA
 *   WA + iron-door collision -> WD
 *   (WD <-> WA continuous alternation)
 *
 *   WD/WA + Y reaches 73 -> SD
 *   SD held until Y returns to 69 -> WD
 *   (Resume WD <-> WA ...)
 */
public class AceWheatMacro extends AbstractMacro {

    private final Clock transitionClock = new Clock();
    private boolean doorDebounceActive = false;

    @Override
    public void onEnable() {
        super.onEnable();
        transitionClock.reset();
        doorDebounceActive = false;

        if (!isPitchSet()) {
            setPitch((float) (25f + Math.random() * 5f));
        }

        if (isYawSet() || isPitchSet()) {
            LogUtils.sendDebug("[AceWheat] Applying initial rotation: yaw=" + getYaw() + " pitch=" + getPitch());
            getRotation().easeTo(
                    new RotationConfiguration(
                            new Rotation(getYaw(), getPitch()),
                            FarmHelperConfig.getRandomRotationTime(),
                            null
                    ).easeOutBack(true)
            );
        }

        changeState(State.WD);
        setLayerY(mc.thePlayer.getPosition().getY());
    }

    @Override
    public void onDisable() {
        doorDebounceActive = false;
        super.onDisable();
    }

    @Override
    public boolean requiresRewarp() {
        return false;
    }

    @Override
    public void actionAfterTeleport() {
        setLayerY(mc.thePlayer.getPosition().getY());
    }

    @Override
    public boolean shouldRotateAfterWarp() {
        return false;
    }

    @Override
    public void updateState() {
        processStateTransitions();
    }

    @Override
    public void invokeState() {
        if (mc.thePlayer == null) return;
        setLayerY(mc.thePlayer.getPosition().getY());

        // Process transitions while moving (Y transitions, door collisions)
        processStateTransitions();

        if (currentState == null) return;
        switch (currentState) {
            case WD:
                KeyBindUtils.holdThese(
                        mc.gameSettings.keyBindForward, // W
                        mc.gameSettings.keyBindRight,   // D
                        mc.gameSettings.keyBindAttack
                );
                break;
            case WA:
                KeyBindUtils.holdThese(
                        mc.gameSettings.keyBindForward, // W
                        mc.gameSettings.keyBindLeft,    // A
                        mc.gameSettings.keyBindAttack
                );
                break;
            case SD:
                KeyBindUtils.holdThese(
                        mc.gameSettings.keyBindBack,   // S
                        mc.gameSettings.keyBindRight,  // D
                        mc.gameSettings.keyBindAttack
                );
                break;
            default:
                break;
        }
    }

    private void processStateTransitions() {
        if (mc.thePlayer == null) return;

        if (currentState == null || currentState == State.NONE) {
            KeyBindUtils.stopMovement(true);
            changeState(State.WD);
            doorDebounceActive = false;
            transitionClock.schedule(getTransitionDelay());
            return;
        }

        // Reset door debounce when player is no longer colliding with a door and debounce clock passed
        if (!isCollidingWithIronDoor() && transitionClock.passed()) {
            doorDebounceActive = false;
        }

        double y = mc.thePlayer.posY;
        int blockY = mc.thePlayer.getPosition().getY();

        switch (currentState) {
            case WD: {
                // When the farm's path reaches the upper level and Y reaches 73: WD -> SD
                if (y >= 72.5 || blockY >= 73) {
                    LogUtils.sendDebug("[AceWheat] Upper level reached (Y=" + y + " >= 73) -> SD");
                    KeyBindUtils.stopMovement(true);
                    changeState(State.SD);
                    doorDebounceActive = false;
                    transitionClock.schedule(getTransitionDelay());
                    break;
                }
                // WD + iron door -> WA
                if (isCollidingWithIronDoor() && !doorDebounceActive && transitionClock.passed()) {
                    LogUtils.sendDebug("[AceWheat] WD -> WA (iron door)");
                    KeyBindUtils.stopMovement(true);
                    changeState(State.WA);
                    doorDebounceActive = true;
                    transitionClock.schedule(getTransitionDelay());
                }
                break;
            }

            case WA: {
                // When the farm's path reaches the upper level and Y reaches 73: WA -> SD
                if (y >= 72.5 || blockY >= 73) {
                    LogUtils.sendDebug("[AceWheat] Upper level reached (Y=" + y + " >= 73) -> SD");
                    KeyBindUtils.stopMovement(true);
                    changeState(State.SD);
                    doorDebounceActive = false;
                    transitionClock.schedule(getTransitionDelay());
                    break;
                }
                // WA + iron door -> WD
                if (isCollidingWithIronDoor() && !doorDebounceActive && transitionClock.passed()) {
                    LogUtils.sendDebug("[AceWheat] WA -> WD (iron door)");
                    KeyBindUtils.stopMovement(true);
                    changeState(State.WD);
                    doorDebounceActive = true;
                    transitionClock.schedule(getTransitionDelay());
                }
                break;
            }

            case SD: {
                // Hold SD until player's Y returns to 69: SD -> WD
                if (y <= 69.5 || blockY <= 69) {
                    LogUtils.sendDebug("[AceWheat] Lower level reached (Y=" + y + " <= 69) -> WD");
                    KeyBindUtils.stopMovement(true);
                    changeState(State.WD);
                    doorDebounceActive = false;
                    transitionClock.schedule(getTransitionDelay());
                }
                break;
            }

            case DROPPING: {
                if (mc.thePlayer.onGround) {
                    setLayerY(mc.thePlayer.getPosition().getY());
                    changeState(previousState != null && previousState != State.DROPPING ? previousState : State.WD);
                }
                break;
            }

            default:
                KeyBindUtils.stopMovement(true);
                changeState(State.WD);
                break;
        }
    }

    private boolean isCollidingWithIronDoor() {
        if (mc.thePlayer == null || mc.theWorld == null) return false;
        BlockPos origin = mc.thePlayer.getPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    BlockPos check = origin.add(dx, dy, dz);
                    Block b = mc.theWorld.getBlockState(check).getBlock();
                    if (b == Blocks.iron_door) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private long getTransitionDelay() {
        return Math.max(20L, (long) FarmHelperConfig.aceWheatTransitionDelay);
    }
}
