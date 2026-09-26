package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.util.InventoryUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class PestPetManager {
    private static final PestPetManager INSTANCE = new PestPetManager();
    private static final Minecraft mc = Minecraft.getMinecraft();

    private boolean active = false;
    private State state = State.IDLE;
    private final Clock stateClock = new Clock();
    private final Clock timeoutClock = new Clock();

    private String targetPetName = null;
    private String targetRarity = null;
    private Runnable callback = null;
    private int sendRetries = 0;

    public enum State {
        IDLE,
        SEND_COMMAND,
        WAIT_GUI,
        CLICK_PET,
        WAIT_CLOSE,
        FINISHED
    }

    private PestPetManager() {}

    public static PestPetManager getInstance() {
        return INSTANCE;
    }

    public boolean isActive() {
        return active;
    }

    public State getState() {
        return state;
    }

    public void equipFarmingPet(Runnable onComplete) {
        if (!FarmHelperConfig.autoPetSwap) {
            if (onComplete != null) onComplete.run();
            return;
        }
        String petName = FarmHelperConfig.pestFarmingPet;
        if (petName == null || petName.trim().isEmpty()) petName = "Slug";
        swapPet(petName, "LEGENDARY", onComplete);
    }

    public void equipHuntingPet(Runnable onComplete) {
        if (!FarmHelperConfig.autoPetSwap) {
            if (onComplete != null) onComplete.run();
            return;
        }
        String petName = FarmHelperConfig.pestHuntingPet;
        if (petName == null || petName.trim().isEmpty()) petName = "Hedgehog";
        swapPet(petName, "LEGENDARY", onComplete);
    }

    public void swapPet(String petName, String rarity, Runnable onComplete) {
        if (mc.thePlayer == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        this.targetPetName = petName != null ? petName.toLowerCase().trim() : "";
        this.targetRarity = rarity != null ? rarity.toUpperCase().trim() : null;
        this.callback = onComplete;
        this.active = true;
        this.state = State.SEND_COMMAND;
        this.sendRetries = 0;
        this.stateClock.schedule(100);
        this.timeoutClock.schedule(10000);
        LogUtils.sendDebug("[PetSwapper] Starting pet swap to " + this.targetPetName + " (" + (rarity != null ? rarity : "ANY") + ")...");
    }

    public void reset() {
        active = false;
        state = State.IDLE;
        targetPetName = null;
        targetRarity = null;
        callback = null;
        sendRetries = 0;
    }

    public void onTick() {
        if (!active || mc.thePlayer == null) return;

        if (timeoutClock.passed() && state != State.IDLE && state != State.FINISHED) {
            LogUtils.sendWarning("[PetSwapper] Pet swap timed out.");
            finish();
            return;
        }

        if (!stateClock.passed()) return;

        switch (state) {
            case SEND_COMMAND:
                if (!PestCommandScheduler.canSend()) {
                    stateClock.schedule(200);
                    return;
                }
                PestCommandScheduler.send("/pets");
                state = State.WAIT_GUI;
                stateClock.schedule(FarmHelperConfig.petCommandDelay);
                break;

            case WAIT_GUI:
                if (mc.currentScreen instanceof GuiChest) {
                    GuiChest chest = (GuiChest) mc.currentScreen;
                    ContainerChest container = (ContainerChest) chest.inventorySlots;
                    IInventory lower = container.getLowerChestInventory();
                    String title = lower != null ? lower.getDisplayName().getUnformattedText().toLowerCase() : "";

                    if (title.contains("pets") || title.contains("your pets")) {
                        state = State.CLICK_PET;
                        stateClock.schedule(200);
                        return;
                    }
                }
                // GUI didn't open — retry /pets up to 2 times
                if (sendRetries < 2) {
                    sendRetries++;
                    LogUtils.sendWarning("[PetSwapper] Pets GUI did not open, retrying (" + sendRetries + "/2)...");
                    state = State.SEND_COMMAND;
                    stateClock.schedule(500);
                } else {
                    stateClock.schedule(100);
                }
                break;

            case CLICK_PET:
                if (!(mc.currentScreen instanceof GuiChest)) {
                    state = State.WAIT_GUI;
                    stateClock.schedule(200);
                    return;
                }

                Slot petSlot = findPetSlot();
                if (petSlot == null) {
                    LogUtils.sendWarning("[PetSwapper] Could not find " + targetPetName + " in Pets GUI.");
                    finish();
                    return;
                }

                if (isPetAlreadyActive(petSlot.getStack())) {
                    LogUtils.sendSuccess("[PetSwapper] Pet " + targetPetName + " is already active.");
                    finish();
                    return;
                }

                LogUtils.sendSuccess("[PetSwapper] Equipping " + targetPetName + " (Slot " + petSlot.slotNumber + ")...");
                InventoryUtils.clickContainerSlot(petSlot.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                state = State.WAIT_CLOSE;
                stateClock.schedule(350);
                break;

            case WAIT_CLOSE:
                if (mc.currentScreen != null) {
                    mc.thePlayer.closeScreen();
                }
                state = State.FINISHED;
                stateClock.schedule(200);
                break;

            case FINISHED:
                finish();
                break;
        }
    }

    private Slot findPetSlot() {
        if (!(mc.currentScreen instanceof GuiChest) || mc.thePlayer.openContainer == null) return null;

        Slot bestMatch = null;
        for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
            if (slot == null || !slot.getHasStack()) continue;
            ItemStack stack = slot.getStack();
            String displayName = StringUtils.stripControlCodes(stack.getDisplayName()).toLowerCase();

            if (!displayName.contains(targetPetName)) continue;

            List<String> lore = getLoreLines(stack);
            boolean rarityMatches = (targetRarity == null);
            if (!rarityMatches) {
                for (String line : lore) {
                    if (line.toUpperCase().contains(targetRarity)) {
                        rarityMatches = true;
                        break;
                    }
                }
            }

            if (rarityMatches) {
                return slot; // Exact name & rarity match
            }
            if (bestMatch == null) {
                bestMatch = slot;
            }
        }
        return bestMatch;
    }

    private boolean isPetAlreadyActive(ItemStack stack) {
        if (stack == null) return false;
        List<String> lore = getLoreLines(stack);
        for (String line : lore) {
            String lower = line.toLowerCase();
            if (lower.contains("click to despawn") || lower.contains("currently equipped") || lower.contains("selected") || lower.contains("active")) {
                return true;
            }
        }
        return false;
    }

    private List<String> getLoreLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        if (stack == null || !stack.hasTagCompound()) return lines;
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey("display", 10)) return lines;
        NBTTagCompound display = tag.getCompoundTag("display");
        if (!display.hasKey("Lore", 9)) return lines;
        NBTTagList loreList = display.getTagList("Lore", 8);
        for (int i = 0; i < loreList.tagCount(); i++) {
            lines.add(StringUtils.stripControlCodes(loreList.getStringTagAt(i)));
        }
        return lines;
    }

    private void finish() {
        if (mc.currentScreen != null) {
            mc.thePlayer.closeScreen();
        }
        active = false;
        state = State.IDLE;
        Runnable r = callback;
        callback = null;
        if (r != null) {
            r.run();
        }
    }
}
