package com.jelly.farmhelperv2.gui;

import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Multi-page vanilla settings for Android / GL4ES. Same setup actions as OneConfig
 * (rewarp, spawn, farming toggles) without NanoVG.
 */
public class FarmXMobileGui extends GuiScreen {
    private static final String[] PAGE_NAMES = {"Farming", "Rewarp & Spawn", "Failsafe", "Misc"};
    private int page = 0;

    private static final int ID_PREV = 100;
    private static final int ID_NEXT = 101;
    private static final int ID_SAVE_CLOSE = 102;

    // Farming
    private static final int ID_MACRO_TYPE = 1;
    private static final int ID_ALWAYS_W = 2;
    private static final int ID_HOLD_LMB = 3;
    private static final int ID_ROTATE_WARP = 5;
    private static final int ID_ROTATE_DROP = 6;

    // Rewarp & Spawn
    private static final int ID_SET_SPAWN = 10;
    private static final int ID_RESET_SPAWN = 11;
    private static final int ID_ADD_REWARP = 12;
    private static final int ID_REMOVE_REWARP = 13;
    private static final int ID_REMOVE_ALL_REWARPS = 14;

    // Failsafe
    private static final int ID_FAILSAFE_SOUND = 20;
    private static final int ID_RESTART_AFTER = 21;
    private static final int ID_FAILSAFE_MESSAGES = 22;
    private static final int ID_AUTO_WARP_WORLD = 23;

    // Misc
    private static final int ID_ANTI_STUCK = 30;
    private static final int ID_FAST_BREAK = 31;
    private static final int ID_DESYNC = 32;
    private static final int ID_BPS_CHECK = 33;
    private static final int ID_MUTE_GAME = 34;
    private static final int ID_DEBUG = 35;

    private static final String[] MACRO_LABELS = {
            "S Vert Crops",
            "S Pumpkin/Melon",
            "S Melongkingde",
            "S Default Plot",
            "S Sugar Cane",
            "S Cactus",
            "S Cactus SunTzu",
            "S Cocoa",
            "S Cocoa Trapdoors",
            "S Cocoa L/R",
            "S Mushroom 45",
            "S Mushroom 30",
            "S Mushroom SDS",
            "Circle Crops"
    };

    @Override
    public void initGui() {
        this.buttonList.clear();
        int cx = this.width / 2 - 100;
        int y = 36;
        int gap = 22;
        int half = 98;

        this.buttonList.add(new GuiButton(ID_PREV, this.width / 2 - 105, this.height - 48, 70, 20, "< Prev"));
        this.buttonList.add(new GuiButton(ID_NEXT, this.width / 2 - 30, this.height - 48, 70, 20, "Next >"));
        this.buttonList.add(new GuiButton(ID_SAVE_CLOSE, this.width / 2 + 45, this.height - 48, 60, 20, "Save"));

        switch (page) {
            case 0:
                this.buttonList.add(new GuiButton(ID_MACRO_TYPE, cx, y, 200, 20, macroTypeLabel()));
                y += gap;
                this.buttonList.add(new GuiButton(ID_ALWAYS_W, cx, y, 200, 20, toggleLabel("Always Hold W", FarmHelperConfig.alwaysHoldW)));
                y += gap;
                this.buttonList.add(new GuiButton(ID_HOLD_LMB, cx, y, 200, 20, toggleLabel("Hold LMB on Row Change", FarmHelperConfig.holdLeftClickWhenChangingRow)));
                y += gap;
                this.buttonList.add(new GuiButton(ID_ROTATE_WARP, cx, y, 200, 20, toggleLabel("Rotate After Warp", FarmHelperConfig.rotateAfterWarped)));
                y += gap;
                this.buttonList.add(new GuiButton(ID_ROTATE_DROP, cx, y, 200, 20, toggleLabel("Rotate After Drop", FarmHelperConfig.rotateAfterDrop)));
                break;
            case 1:
                this.buttonList.add(new GuiButton(ID_SET_SPAWN, cx, y, 200, 20, "Set Spawn (current pos)"));
                y += gap;
                this.buttonList.add(new GuiButton(ID_RESET_SPAWN, cx, y, 200, 20, "Reset Spawn"));
                y += gap + 4;
                this.buttonList.add(new GuiButton(ID_ADD_REWARP, cx, y, 200, 20, "Add Rewarp Here"));
                y += gap;
                this.buttonList.add(new GuiButton(ID_REMOVE_REWARP, this.width / 2 - 105, y, half, 20, "Remove Closest"));
                this.buttonList.add(new GuiButton(ID_REMOVE_ALL_REWARPS, this.width / 2 + 7, y, half, 20, "Remove All"));
                break;
            case 2:
                this.buttonList.add(new GuiButton(ID_FAILSAFE_SOUND, cx, y, 200, 20, toggleLabel("Failsafe Sound", FarmHelperConfig.enableFailsafeSound)));
                y += gap;
                this.buttonList.add(new GuiButton(ID_RESTART_AFTER, cx, y, 200, 20, toggleLabel("Restart After Failsafe", FarmHelperConfig.enableRestartAfterFailSafe)));
                y += gap;
                this.buttonList.add(new GuiButton(ID_FAILSAFE_MESSAGES, cx, y, 200, 20, toggleLabel("Failsafe Chat Messages", FarmHelperConfig.sendFailsafeMessage)));
                y += gap;
                this.buttonList.add(new GuiButton(ID_AUTO_WARP_WORLD, cx, y, 200, 20, toggleLabel("Auto Warp Garden", FarmHelperConfig.alwaysTeleportToGarden)));
                break;
            case 3:
            default:
                this.buttonList.add(new GuiButton(ID_ANTI_STUCK, cx, y, 200, 20, toggleLabel("Anti Stuck", FarmHelperConfig.tmpAntiStuckEnabled)));
                y += gap;
                this.buttonList.add(new GuiButton(ID_FAST_BREAK, cx, y, 200, 20, toggleLabel("Fast Break", FarmHelperConfig.fastBreak)));
                y += gap;
                this.buttonList.add(new GuiButton(ID_DESYNC, cx, y, 200, 20, toggleLabel("Desync Check", FarmHelperConfig.checkDesync)));
                y += gap;
                this.buttonList.add(new GuiButton(ID_BPS_CHECK, cx, y, 200, 20, toggleLabel("Lower BPS Failsafe", FarmHelperConfig.enableBpsCheck)));
                y += gap;
                this.buttonList.add(new GuiButton(ID_MUTE_GAME, cx, y, 200, 20, toggleLabel("Mute While Farming", FarmHelperConfig.muteTheGame)));
                y += gap;
                this.buttonList.add(new GuiButton(ID_DEBUG, cx, y, 200, 20, toggleLabel("Debug Mode", FarmHelperConfig.debugMode)));
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "FarmX — " + PAGE_NAMES[page], this.width / 2, 12, 0xFFFFFF);
        if (page == 1) {
            String spawn = PlayerUtils.isSpawnLocationSet()
                    ? ("Spawn: " + FarmHelperConfig.spawnPosX + ", " + FarmHelperConfig.spawnPosY + ", " + FarmHelperConfig.spawnPosZ
                    + "  yaw=" + (int) FarmHelperConfig.spawnYaw)
                    : "Spawn: not set";
            this.drawCenteredString(this.fontRendererObj, spawn, this.width / 2, this.height - 72, 0x55FF55);
            this.drawCenteredString(this.fontRendererObj, "Rewarps: " + FarmHelperConfig.rewarpList.size()
                            + "  |  chat: /fhrewarp  /fhspawn",
                    this.width / 2, this.height - 60, 0xAAAAAA);
        } else {
            this.drawCenteredString(this.fontRendererObj, "Page " + (page + 1) + "/" + PAGE_NAMES.length
                            + "  |  also: /fhrewarp  /fhspawn",
                    this.width / 2, this.height - 64, 0xAAAAAA);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case ID_PREV:
                page = (page + PAGE_NAMES.length - 1) % PAGE_NAMES.length;
                initGui();
                return;
            case ID_NEXT:
                page = (page + 1) % PAGE_NAMES.length;
                initGui();
                return;
            case ID_SAVE_CLOSE:
                saveAndClose();
                return;
            case ID_MACRO_TYPE:
                FarmHelperConfig.macroType = (FarmHelperConfig.macroType + 1) % MACRO_LABELS.length;
                button.displayString = macroTypeLabel();
                break;
            case ID_ALWAYS_W:
                FarmHelperConfig.alwaysHoldW = !FarmHelperConfig.alwaysHoldW;
                button.displayString = toggleLabel("Always Hold W", FarmHelperConfig.alwaysHoldW);
                break;
            case ID_HOLD_LMB:
                FarmHelperConfig.holdLeftClickWhenChangingRow = !FarmHelperConfig.holdLeftClickWhenChangingRow;
                button.displayString = toggleLabel("Hold LMB on Row Change", FarmHelperConfig.holdLeftClickWhenChangingRow);
                break;
            case ID_ROTATE_WARP:
                FarmHelperConfig.rotateAfterWarped = !FarmHelperConfig.rotateAfterWarped;
                button.displayString = toggleLabel("Rotate After Warp", FarmHelperConfig.rotateAfterWarped);
                break;
            case ID_ROTATE_DROP:
                FarmHelperConfig.rotateAfterDrop = !FarmHelperConfig.rotateAfterDrop;
                button.displayString = toggleLabel("Rotate After Drop", FarmHelperConfig.rotateAfterDrop);
                break;
            case ID_SET_SPAWN:
                PlayerUtils.setSpawnLocation();
                if (PlayerUtils.isSpawnLocationSet()) {
                    LogUtils.sendSuccess("Spawn set to " + FarmHelperConfig.spawnPosX + ", "
                            + FarmHelperConfig.spawnPosY + ", " + FarmHelperConfig.spawnPosZ);
                }
                initGui();
                break;
            case ID_RESET_SPAWN:
                FarmHelperConfig.spawnPosX = 0;
                FarmHelperConfig.spawnPosY = 0;
                FarmHelperConfig.spawnPosZ = 0;
                FarmHelperConfig.spawnYaw = 0;
                FarmHelperConfig.spawnPitch = 0;
                FarmHelperConfig.spawnPlot = 0;
                if (FarmHelper.config != null) {
                    FarmHelper.config.save();
                }
                LogUtils.sendSuccess("Spawn position has been reset!");
                initGui();
                break;
            case ID_ADD_REWARP:
                FarmHelperConfig.addRewarp();
                initGui();
                break;
            case ID_REMOVE_REWARP:
                FarmHelperConfig.removeRewarp();
                initGui();
                break;
            case ID_REMOVE_ALL_REWARPS:
                FarmHelperConfig.removeAllRewarps();
                initGui();
                break;
            case ID_FAILSAFE_SOUND:
                FarmHelperConfig.enableFailsafeSound = !FarmHelperConfig.enableFailsafeSound;
                button.displayString = toggleLabel("Failsafe Sound", FarmHelperConfig.enableFailsafeSound);
                break;
            case ID_RESTART_AFTER:
                FarmHelperConfig.enableRestartAfterFailSafe = !FarmHelperConfig.enableRestartAfterFailSafe;
                button.displayString = toggleLabel("Restart After Failsafe", FarmHelperConfig.enableRestartAfterFailSafe);
                break;
            case ID_FAILSAFE_MESSAGES:
                FarmHelperConfig.sendFailsafeMessage = !FarmHelperConfig.sendFailsafeMessage;
                button.displayString = toggleLabel("Failsafe Chat Messages", FarmHelperConfig.sendFailsafeMessage);
                break;
            case ID_AUTO_WARP_WORLD:
                FarmHelperConfig.alwaysTeleportToGarden = !FarmHelperConfig.alwaysTeleportToGarden;
                button.displayString = toggleLabel("Auto Warp Garden", FarmHelperConfig.alwaysTeleportToGarden);
                break;
            case ID_ANTI_STUCK:
                FarmHelperConfig.tmpAntiStuckEnabled = !FarmHelperConfig.tmpAntiStuckEnabled;
                button.displayString = toggleLabel("Anti Stuck", FarmHelperConfig.tmpAntiStuckEnabled);
                break;
            case ID_FAST_BREAK:
                FarmHelperConfig.fastBreak = !FarmHelperConfig.fastBreak;
                button.displayString = toggleLabel("Fast Break", FarmHelperConfig.fastBreak);
                break;
            case ID_DESYNC:
                FarmHelperConfig.checkDesync = !FarmHelperConfig.checkDesync;
                button.displayString = toggleLabel("Desync Check", FarmHelperConfig.checkDesync);
                break;
            case ID_BPS_CHECK:
                FarmHelperConfig.enableBpsCheck = !FarmHelperConfig.enableBpsCheck;
                button.displayString = toggleLabel("Lower BPS Failsafe", FarmHelperConfig.enableBpsCheck);
                break;
            case ID_MUTE_GAME:
                FarmHelperConfig.muteTheGame = !FarmHelperConfig.muteTheGame;
                button.displayString = toggleLabel("Mute While Farming", FarmHelperConfig.muteTheGame);
                break;
            case ID_DEBUG:
                FarmHelperConfig.debugMode = !FarmHelperConfig.debugMode;
                button.displayString = toggleLabel("Debug Mode", FarmHelperConfig.debugMode);
                break;
            default:
                break;
        }
    }

    private void saveAndClose() {
        if (FarmHelper.config != null) {
            FarmHelper.config.save();
        }
        this.mc.displayGuiScreen(null);
        if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static String macroTypeLabel() {
        int idx = FarmHelperConfig.macroType;
        if (idx < 0 || idx >= MACRO_LABELS.length) {
            idx = 0;
        }
        return "Macro: " + MACRO_LABELS[idx];
    }

    private static String toggleLabel(String name, boolean on) {
        return name + ": " + (on ? "ON" : "OFF");
    }
}
