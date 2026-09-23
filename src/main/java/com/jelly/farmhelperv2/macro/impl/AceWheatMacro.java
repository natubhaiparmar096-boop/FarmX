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
 * Physical trigger design (coordinate-independent):
 *
 *   The farm has:
 *     - Iron doors at the end/start of each horizontal row (both open and closed versions, id 71/64).
 *     - A small cluster of dirt blocks (id 3) at the upper-diagonal transition point.
 *       This cluster is the ONLY dirt in the whole farm; all walkable floor is farmland (id 60).
 *
 * State machine:
 *   START  → SD
 *   SD + iron-door contact  → WD
 *   WD + iron-door contact  → WA
 *   WA + iron-door contact  → WD
 *   WD + dirt underfoot     → SD      (the one special upper-diagonal transition)
 *
 * Transitions are debounced: one physical contact = one state change.
 * No coordinates are used for normal row transitions.
 * Dirt detection is used only to distinguish the special WD→SD case; no other transition uses dirt.
 */
public class AceWheatMacro extends AbstractMacro {

    // ── debounce ────────────────────────────────────────────────────────────
    /**
     * After a transition fires we lock out the same trigger for a short window
     * equal to FarmHelperConfig.aceWheatTransitionDelay (ms, configurable from GUI).
     */
    private final Clock transitionClock = new Clock();

    /** True while the player is touching the door/dirt that last caused a transition. */
    private boolean lastTriggerWasDoor = false;
    private boolean lastTriggerWasDirt = false;

    // ── internal state ───────────────────────────────────────────────────────
    private boolean initialized = false;

    // ────────────────────────────────────────────────────────────────────────
    // AbstractMacro contract
    // ────────────────────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        super.onEnable();
        initialized = false;
        transitionClock.reset();
        lastTriggerWasDoor = false;
        lastTriggerWasDirt = false;
        if (!isPitchSet()) {
            // Fallback downward pitch for wheat harvesting when nothing is configured
            setPitch((float) (25f + Math.random() * 5f));
        }
        // Apply configured yaw/pitch immediately.
        // Ace Wheat bypasses the rewarp flow, so the normal TELEPORTED rotation in
        // AbstractMacro.onTick() never fires. Mirror what working macros do: call
        // easeTo() right here, after super.onEnable() has loaded the config values.
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
    }

    @Override
    public void onDisable() {
        initialized = false;
        super.onDisable();
    }

    /**
     * Ace Wheat is a closed physical loop; it never uses /warp or any rewarp logic.
     * Returning false skips the rewarp-location guard in AbstractMacro.onTick().
     */
    @Override
    public boolean requiresRewarp() {
        return false;
    }

    @Override
    public void actionAfterTeleport() {
        // No post-teleport action needed for Ace Wheat
        setLayerY(mc.thePlayer.getPosition().getY());
    }

    @Override
    public boolean shouldRotateAfterWarp() {
        return false; // Ace Wheat does not use warp/rewarp
    }

    // ────────────────────────────────────────────────────────────────────────
    // State transitions (called when !GameStateHandler.canChangeDirection)
    // ────────────────────────────────────────────────────────────────────────

    @Override
    public void updateState() {
        if (currentState == null || currentState == State.NONE) {
            // Cold start – begin with SD
            changeState(State.SD);
            transitionClock.schedule(getTransitionDelay() + 500L);
            initialized = true;
            lastTriggerWasDoor = false;
            lastTriggerWasDirt = false;
            return;
        }

        if (!transitionClock.passed()) {
            // Still within debounce window – no state change
            return;
        }

        switch (currentState) {
            case SD: {
                // SD → WD when an iron door is touched
                if (isCollidingWithIronDoor()) {
                    if (!lastTriggerWasDoor) {
                        LogUtils.sendDebug("[AceWheat] SD → WD (iron door)");
                        changeState(State.WD);
                        armDebounce(true, false);
                    }
                } else {
                    lastTriggerWasDoor = false;
                }
                break;
            }
            case WD: {
                // Priority: WD → SD when standing on the special diagonal dirt cluster
                if (isStandingOnDirt()) {
                    if (!lastTriggerWasDirt) {
                        LogUtils.sendDebug("[AceWheat] WD → SD (diagonal dirt trigger)");
                        changeState(State.SD);
                        armDebounce(false, true);
                    }
                    break;
                }
                // Otherwise: WD → WA when an iron door is touched
                if (isCollidingWithIronDoor()) {
                    if (!lastTriggerWasDoor) {
                        LogUtils.sendDebug("[AceWheat] WD → WA (iron door)");
                        changeState(State.WA);
                        armDebounce(true, false);
                    }
                } else {
                    lastTriggerWasDoor = false;
                    lastTriggerWasDirt = false;
                }
                break;
            }
            case WA: {
                // WA → WD when an iron door is touched
                if (isCollidingWithIronDoor()) {
                    if (!lastTriggerWasDoor) {
                        LogUtils.sendDebug("[AceWheat] WA → WD (iron door)");
                        changeState(State.WD);
                        armDebounce(true, false);
                    }
                } else {
                    lastTriggerWasDoor = false;
                }
                break;
            }
            default:
                // Any other state → reset to SD
                changeState(State.SD);
                transitionClock.schedule(getTransitionDelay());
                break;
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Key holding (called every tick while state is stable)
    // ────────────────────────────────────────────────────────────────────────

    @Override
    public void invokeState() {
        if (currentState == null) return;
        switch (currentState) {
            case SD:
                KeyBindUtils.holdThese(
                        mc.gameSettings.keyBindBack,   // S
                        mc.gameSettings.keyBindRight,  // D
                        mc.gameSettings.keyBindAttack
                );
                break;
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
            default:
                // NONE or unexpected – don't move
                break;
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Trigger detection helpers
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Returns true if the block at the player's feet position or any of the
     * 4 immediate horizontal neighbours is an iron door (id 71 open or closed).
     *
     * This is completely coordinate-independent; it works regardless of where
     * the schematic was placed.
     *
     * We also accept isCollidedHorizontally as a secondary signal when the
     * block check confirms a door is nearby, to handle server lag gracefully.
     */
    private boolean isCollidingWithIronDoor() {
        // Check a 3x3 footprint at the player's feet and knee level
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

    /**
     * Returns true if the block directly beneath the player (the block they
     * are standing on) is dirt (id 3).
     *
     * In the Ace Wheat schematic the only dirt blocks in the entire farm form
     * a small cluster in the upper-diagonal lane at the WD→SD transition point.
     * All other walkable surfaces are farmland (id 60), so this check cannot
     * fire anywhere else in normal operation.
     */
    private boolean isStandingOnDirt() {
        // Block directly below feet
        BlockPos below = mc.thePlayer.getPosition().add(0, -1, 0);
        Block underfoot = mc.theWorld.getBlockState(below).getBlock();
        if (underfoot == Blocks.dirt) {
            return true;
        }
        // Also check at the exact feet Y in case of partial block geometry
        Block feet = mc.theWorld.getBlockState(mc.thePlayer.getPosition()).getBlock();
        return feet == Blocks.dirt;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Debounce helpers
    // ────────────────────────────────────────────────────────────────────────

    private void armDebounce(boolean door, boolean dirt) {
        // Always clear both flags so they never carry over into the next state.
        // The transitionClock provides the actual debounce; keeping the booleans
        // true after a transition was the root cause of WD → WA being blocked.
        lastTriggerWasDoor = false;
        lastTriggerWasDirt = false;
        transitionClock.schedule(getTransitionDelay());
    }

    private long getTransitionDelay() {
        return Math.max(20L, (long) FarmHelperConfig.aceWheatTransitionDelay);
    }
}
