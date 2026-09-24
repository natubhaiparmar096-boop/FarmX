package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.BaritoneHandler;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.util.InventoryUtils;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Slot;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;

public final class PestExchangeManager {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean active = false;
    private static State state = State.IDLE;
    private static final Clock stateClock = new Clock();
    private static int retryCount = 0;

    public enum State {
        IDLE,
        TELEPORT_TO_BARN,
        WAIT_TELEPORT,
        INTERACT_PHILLIP,
        CLICK_EXCHANGE_SLOT,
        FINISH
    }

    private PestExchangeManager() {}

    public static boolean isActive() {
        return active;
    }

    public static void start() {
        if (active) return;
        active = true;
        state = State.TELEPORT_TO_BARN;
        retryCount = 0;
        stateClock.schedule(200);
        LogUtils.sendSuccess("[Pest] Starting Pest Exchange with Phillip...");
    }

    public static void stop() {
        active = false;
        state = State.IDLE;
        BaritoneHandler.stopPathing();
    }

    public static void onTick() {
        if (!active || mc.thePlayer == null) return;
        if (!stateClock.passed()) return;

        switch (state) {
            case TELEPORT_TO_BARN:
                mc.thePlayer.sendChatMessage("/plottp barn");
                state = State.WAIT_TELEPORT;
                stateClock.schedule(2500);
                break;

            case WAIT_TELEPORT:
                // Find Phillip NPC near Barn
                Entity phillip = findPhillipNPC();
                if (phillip != null) {
                    double distSq = mc.thePlayer.getDistanceSqToEntity(phillip);
                    if (distSq <= 16.0) {
                        mc.playerController.interactWithEntitySendPacket(mc.thePlayer, phillip);
                        state = State.CLICK_EXCHANGE_SLOT;
                        stateClock.schedule(1500);
                    } else {
                        BaritoneHandler.walkToBlockPos(new BlockPos(phillip.posX, phillip.posY, phillip.posZ));
                        stateClock.schedule(1000);
                    }
                } else {
                    retryCount++;
                    if (retryCount > 5) {
                        LogUtils.sendError("[Pest] Could not find Phillip NPC at Barn.");
                        stop();
                    } else {
                        stateClock.schedule(1000);
                    }
                }
                break;

            case CLICK_EXCHANGE_SLOT:
                if (mc.currentScreen instanceof GuiChest) {
                    for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
                        if (slot.getHasStack()) {
                            String name = StringUtils.stripControlCodes(slot.getStack().getDisplayName()).toLowerCase();
                            if (name.contains("empty vacuum bag") || name.contains("turn in") || name.contains("exchange")) {
                                InventoryUtils.clickContainerSlot(slot.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                                state = State.FINISH;
                                stateClock.schedule(1000);
                                return;
                            }
                        }
                    }
                }
                stateClock.schedule(500);
                break;

            case FINISH:
                if (mc.currentScreen != null) {
                    mc.thePlayer.closeScreen();
                }
                LogUtils.sendSuccess("[Pest] Pest exchange completed.");
                stop();
                break;
        }
    }

    private static Entity findPhillipNPC() {
        if (mc.theWorld == null) return null;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity != mc.thePlayer && entity.hasCustomName()) {
                String name = StringUtils.stripControlCodes(entity.getCustomNameTag()).toLowerCase();
                if (name.contains("phillip") || name.contains("pest hunter")) {
                    return entity;
                }
            }
        }
        return null;
    }
}
