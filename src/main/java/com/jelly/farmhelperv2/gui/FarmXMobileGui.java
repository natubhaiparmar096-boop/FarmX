package com.jelly.farmhelperv2.gui;

import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;

import java.io.IOException;

/**
 * Multi-page vanilla settings for Android / GL4ES — covers the important FarmX options
 * without OneConfig NanoVG.
 */
public class FarmXMobileGui extends GuiScreen {
    private static final String[] PAGE_NAMES = {
            "Farming", "Rotation", "Crop Utils", "Rewarp & Spawn", "Failsafe", "Misc"
    };
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
    private static final int ID_DONT_FIX_WARP = 7;
    private static final int ID_AUTO_SWITCH_TOOL = 8;

    // Rotation
    private static final int ID_CUSTOM_PITCH = 40;
    private static final int ID_CUSTOM_YAW = 41;
    private static final int ID_SET_PITCH_LOOK = 42;
    private static final int ID_SET_YAW_LOOK = 43;
    private static final int ID_SET_BOTH_LOOK = 44;
    private static final int ID_PITCH_MINUS = 45;
    private static final int ID_PITCH_PLUS = 46;
    private static final int ID_YAW_MINUS = 47;
    private static final int ID_YAW_PLUS = 48;

    // Crop utils
    private static final int ID_HITBOX_COCOA = 50;
    private static final int ID_HITBOX_CROPS = 51;
    private static final int ID_HITBOX_NW = 52;
    private static final int ID_HITBOX_MUSHROOM = 53;
    private static final int ID_PINGLESS_CACTUS = 54;

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
    private static final int ID_AUTO_WARP_GARDEN = 23;
    private static final int ID_FAILSAFE_ACTION = 24;
    private static final int ID_AUTO_WARP_WORLD = 25;
    private static final int ID_SOUND_TYPE = 26;
    private static final int ID_MAX_SOUNDS = 27;
    private static final int ID_POPUP_NOTIFS = 28;

    // Misc
    private static final int ID_ANTI_STUCK = 30;
    private static final int ID_FAST_BREAK = 31;
    private static final int ID_DESYNC = 32;
    private static final int ID_BPS_CHECK = 33;
    private static final int ID_MUTE_GAME = 34;
    private static final int ID_DEBUG = 35;
    private static final int ID_ANTISTUCK_TRIES_MINUS = 60;
    private static final int ID_ANTISTUCK_TRIES_PLUS = 61;
    private static final int ID_FB_SPEED_MINUS = 62;
    private static final int ID_FB_SPEED_PLUS = 63;
    private static final int ID_FB_RANDOM = 64;
    private static final int ID_FB_JACOB_OFF = 65;
    private static final int ID_STREAMER = 66;
    private static final int ID_HUD_OUTSIDE = 67;
    private static final int ID_RESET_STATS = 68;

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

    private static final String[] SOUND_LABELS = {"Sound: Orb", "Sound: Anvil"};

    @Override
    public void initGui() {
        this.buttonList.clear();
        int cx = this.width / 2 - 100;
        int y = 34;
        int gap = 20;
        int half = 98;

        this.buttonList.add(new GuiButton(ID_PREV, this.width / 2 - 105, this.height - 46, 70, 20, "< Prev"));
        this.buttonList.add(new GuiButton(ID_NEXT, this.width / 2 - 30, this.height - 46, 70, 20, "Next >"));
        this.buttonList.add(new GuiButton(ID_SAVE_CLOSE, this.width / 2 + 45, this.height - 46, 60, 20, "Save"));

        switch (page) {
            case 0:
                add(ID_MACRO_TYPE, cx, y, 200, macroTypeLabel());
                y += gap;
                add(ID_ALWAYS_W, cx, y, 200, toggleLabel("Always Hold W", FarmHelperConfig.alwaysHoldW));
                y += gap;
                add(ID_HOLD_LMB, cx, y, 200, toggleLabel("Hold LMB on Row Change", FarmHelperConfig.holdLeftClickWhenChangingRow));
                y += gap;
                add(ID_ROTATE_WARP, cx, y, 200, toggleLabel("Rotate After Warp", FarmHelperConfig.rotateAfterWarped));
                y += gap;
                add(ID_ROTATE_DROP, cx, y, 200, toggleLabel("Rotate After Drop", FarmHelperConfig.rotateAfterDrop));
                y += gap;
                add(ID_DONT_FIX_WARP, cx, y, 200, toggleLabel("Don't Fix After Warp", FarmHelperConfig.dontFixAfterWarping));
                y += gap;
                add(ID_AUTO_SWITCH_TOOL, cx, y, 200, toggleLabel("Auto Switch Tool", FarmHelperConfig.autoSwitchTool));
                break;
            case 1:
                add(ID_CUSTOM_PITCH, cx, y, 200, toggleLabel("Custom Pitch", FarmHelperConfig.customPitch));
                y += gap;
                add(ID_CUSTOM_YAW, cx, y, 200, toggleLabel("Custom Yaw", FarmHelperConfig.customYaw));
                y += gap;
                add(ID_SET_BOTH_LOOK, cx, y, 200, "Set Both From Current Look");
                y += gap;
                add(ID_SET_PITCH_LOOK, this.width / 2 - 105, y, half, "Pitch From Look");
                add(ID_SET_YAW_LOOK, this.width / 2 + 7, y, half, "Yaw From Look");
                y += gap;
                add(ID_PITCH_MINUS, this.width / 2 - 105, y, half, "Pitch -1");
                add(ID_PITCH_PLUS, this.width / 2 + 7, y, half, "Pitch +1");
                y += gap;
                add(ID_YAW_MINUS, this.width / 2 - 105, y, half, "Yaw -5");
                add(ID_YAW_PLUS, this.width / 2 + 7, y, half, "Yaw +5");
                break;
            case 2:
                add(ID_HITBOX_CROPS, cx, y, 200, toggleLabel("Bigger Crop Hitboxes", FarmHelperConfig.increasedCrops));
                y += gap;
                add(ID_HITBOX_NW, cx, y, 200, toggleLabel("Bigger Nether Wart Hitboxes", FarmHelperConfig.increasedNetherWarts));
                y += gap;
                add(ID_HITBOX_COCOA, cx, y, 200, toggleLabel("Bigger Cocoa Hitboxes", FarmHelperConfig.increasedCocoaBeans));
                y += gap;
                add(ID_HITBOX_MUSHROOM, cx, y, 200, toggleLabel("Bigger Mushroom Hitboxes", FarmHelperConfig.increasedMushrooms));
                y += gap;
                add(ID_PINGLESS_CACTUS, cx, y, 200, toggleLabel("Pingless Cactus", FarmHelperConfig.pinglessCactus));
                break;
            case 3:
                add(ID_SET_SPAWN, cx, y, 200, "Set Spawn (current pos)");
                y += gap;
                add(ID_RESET_SPAWN, cx, y, 200, "Reset Spawn");
                y += gap + 2;
                add(ID_ADD_REWARP, cx, y, 200, "Add Rewarp Here");
                y += gap;
                add(ID_REMOVE_REWARP, this.width / 2 - 105, y, half, "Remove Closest");
                add(ID_REMOVE_ALL_REWARPS, this.width / 2 + 7, y, half, "Remove All");
                break;
            case 4:
                add(ID_FAILSAFE_ACTION, cx, y, 200, FarmHelperConfig.failsafeAction ? "Failsafe: Disable Macro" : "Failsafe: React");
                y += gap;
                add(ID_FAILSAFE_SOUND, cx, y, 200, toggleLabel("Failsafe Sound", FarmHelperConfig.enableFailsafeSound));
                y += gap;
                add(ID_SOUND_TYPE, cx, y, 200, soundTypeLabel());
                y += gap;
                add(ID_MAX_SOUNDS, cx, y, 200, toggleLabel("Max Out MC Sounds", FarmHelperConfig.maxOutMinecraftSounds));
                y += gap;
                add(ID_RESTART_AFTER, cx, y, 200, toggleLabel("Restart After Failsafe", FarmHelperConfig.enableRestartAfterFailSafe));
                y += gap;
                add(ID_AUTO_WARP_WORLD, cx, y, 200, toggleLabel("Auto Warp on World Change", FarmHelperConfig.autoWarpOnWorldChange));
                y += gap;
                add(ID_AUTO_WARP_GARDEN, cx, y, 200, toggleLabel("Always Teleport Garden", FarmHelperConfig.alwaysTeleportToGarden));
                y += gap;
                add(ID_FAILSAFE_MESSAGES, cx, y, 200, toggleLabel("Failsafe Chat Messages", FarmHelperConfig.sendFailsafeMessage));
                y += gap;
                add(ID_POPUP_NOTIFS, cx, y, 200, toggleLabel("Pop-up Notifications", FarmHelperConfig.popUpNotifications));
                break;
            case 5:
            default:
                add(ID_ANTI_STUCK, cx, y, 200, toggleLabel("Anti Stuck", FarmHelperConfig.tmpAntiStuckEnabled));
                y += gap;
                add(ID_ANTISTUCK_TRIES_MINUS, this.width / 2 - 105, y, half, "Tries - (" + FarmHelperConfig.antiStuckTriesUntilRewarp + ")");
                add(ID_ANTISTUCK_TRIES_PLUS, this.width / 2 + 7, y, half, "Tries +");
                y += gap;
                add(ID_FAST_BREAK, cx, y, 200, toggleLabel("Fast Break", FarmHelperConfig.fastBreak));
                y += gap;
                add(ID_FB_SPEED_MINUS, this.width / 2 - 105, y, half, "FB Speed - (" + FarmHelperConfig.fastBreakSpeed + ")");
                add(ID_FB_SPEED_PLUS, this.width / 2 + 7, y, half, "FB Speed +");
                y += gap;
                add(ID_FB_RANDOM, cx, y, 200, toggleLabel("FB Randomization", FarmHelperConfig.fastBreakRandomization));
                y += gap;
                add(ID_FB_JACOB_OFF, cx, y, 200, toggleLabel("Disable FB in Jacob", FarmHelperConfig.disableFastBreakDuringJacobsContest));
                y += gap;
                add(ID_DESYNC, cx, y, 200, toggleLabel("Desync Check", FarmHelperConfig.checkDesync));
                y += gap;
                add(ID_BPS_CHECK, cx, y, 200, toggleLabel("Lower BPS Failsafe", FarmHelperConfig.enableBpsCheck));
                y += gap;
                add(ID_MUTE_GAME, this.width / 2 - 105, y, half, toggleLabel("Mute Farm", FarmHelperConfig.muteTheGame));
                add(ID_STREAMER, this.width / 2 + 7, y, half, toggleLabel("Streamer", FarmHelperConfig.streamerMode));
                y += gap;
                add(ID_HUD_OUTSIDE, this.width / 2 - 105, y, half, toggleLabel("HUD Outside", FarmHelperConfig.showStatusHudOutsideGarden));
                add(ID_RESET_STATS, this.width / 2 + 7, y, half, toggleLabel("Reset Stats", FarmHelperConfig.resetStatsBetweenDisabling));
                y += gap;
                add(ID_DEBUG, cx, y, 200, toggleLabel("Debug Mode", FarmHelperConfig.debugMode));
                break;
        }
    }

    private void add(int id, int x, int y, int w, String text) {
        this.buttonList.add(new GuiButton(id, x, y, w, 20, text));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "FarmX — " + PAGE_NAMES[page], this.width / 2, 10, 0xFFFFFF);
        if (page == 1) {
            this.drawCenteredString(this.fontRendererObj,
                    String.format("Pitch %s %.1f | Yaw %s %.1f",
                            FarmHelperConfig.customPitch ? "ON" : "OFF", FarmHelperConfig.customPitchLevel,
                            FarmHelperConfig.customYaw ? "ON" : "OFF", FarmHelperConfig.customYawLevel),
                    this.width / 2, this.height - 68, 0x55FF55);
            this.drawCenteredString(this.fontRendererObj, "/fhrot set  |  look then Set From Look",
                    this.width / 2, this.height - 56, 0xAAAAAA);
        } else if (page == 3) {
            String spawn = PlayerUtils.isSpawnLocationSet()
                    ? ("Spawn: " + FarmHelperConfig.spawnPosX + ", " + FarmHelperConfig.spawnPosY + ", " + FarmHelperConfig.spawnPosZ)
                    : "Spawn: not set";
            this.drawCenteredString(this.fontRendererObj, spawn, this.width / 2, this.height - 68, 0x55FF55);
            this.drawCenteredString(this.fontRendererObj, "Rewarps: " + FarmHelperConfig.rewarpList.size()
                            + "  |  /fhrewarp  /fhspawn",
                    this.width / 2, this.height - 56, 0xAAAAAA);
        } else {
            this.drawCenteredString(this.fontRendererObj,
                    "Page " + (page + 1) + "/" + PAGE_NAMES.length + "  |  /fh  /fhrewarp  /fhspawn  /fhrot",
                    this.width / 2, this.height - 60, 0xAAAAAA);
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
                flip(() -> FarmHelperConfig.alwaysHoldW = !FarmHelperConfig.alwaysHoldW,
                        button, "Always Hold W", () -> FarmHelperConfig.alwaysHoldW);
                break;
            case ID_HOLD_LMB:
                flip(() -> FarmHelperConfig.holdLeftClickWhenChangingRow = !FarmHelperConfig.holdLeftClickWhenChangingRow,
                        button, "Hold LMB on Row Change", () -> FarmHelperConfig.holdLeftClickWhenChangingRow);
                break;
            case ID_ROTATE_WARP:
                flip(() -> FarmHelperConfig.rotateAfterWarped = !FarmHelperConfig.rotateAfterWarped,
                        button, "Rotate After Warp", () -> FarmHelperConfig.rotateAfterWarped);
                break;
            case ID_ROTATE_DROP:
                flip(() -> FarmHelperConfig.rotateAfterDrop = !FarmHelperConfig.rotateAfterDrop,
                        button, "Rotate After Drop", () -> FarmHelperConfig.rotateAfterDrop);
                break;
            case ID_DONT_FIX_WARP:
                flip(() -> FarmHelperConfig.dontFixAfterWarping = !FarmHelperConfig.dontFixAfterWarping,
                        button, "Don't Fix After Warp", () -> FarmHelperConfig.dontFixAfterWarping);
                break;
            case ID_AUTO_SWITCH_TOOL:
                flip(() -> FarmHelperConfig.autoSwitchTool = !FarmHelperConfig.autoSwitchTool,
                        button, "Auto Switch Tool", () -> FarmHelperConfig.autoSwitchTool);
                break;

            case ID_CUSTOM_PITCH:
                flip(() -> FarmHelperConfig.customPitch = !FarmHelperConfig.customPitch,
                        button, "Custom Pitch", () -> FarmHelperConfig.customPitch);
                break;
            case ID_CUSTOM_YAW:
                flip(() -> FarmHelperConfig.customYaw = !FarmHelperConfig.customYaw,
                        button, "Custom Yaw", () -> FarmHelperConfig.customYaw);
                break;
            case ID_SET_BOTH_LOOK:
                setFromCurrentLook(true, true);
                initGui();
                break;
            case ID_SET_PITCH_LOOK:
                setFromCurrentLook(true, false);
                initGui();
                break;
            case ID_SET_YAW_LOOK:
                setFromCurrentLook(false, true);
                initGui();
                break;
            case ID_PITCH_MINUS:
                FarmHelperConfig.customPitchLevel = clampPitch(FarmHelperConfig.customPitchLevel - 1f);
                FarmHelperConfig.customPitch = true;
                initGui();
                break;
            case ID_PITCH_PLUS:
                FarmHelperConfig.customPitchLevel = clampPitch(FarmHelperConfig.customPitchLevel + 1f);
                FarmHelperConfig.customPitch = true;
                initGui();
                break;
            case ID_YAW_MINUS:
                FarmHelperConfig.customYawLevel = normalizeYaw(FarmHelperConfig.customYawLevel - 5f);
                FarmHelperConfig.customYaw = true;
                initGui();
                break;
            case ID_YAW_PLUS:
                FarmHelperConfig.customYawLevel = normalizeYaw(FarmHelperConfig.customYawLevel + 5f);
                FarmHelperConfig.customYaw = true;
                initGui();
                break;

            case ID_HITBOX_CROPS:
                flip(() -> FarmHelperConfig.increasedCrops = !FarmHelperConfig.increasedCrops,
                        button, "Bigger Crop Hitboxes", () -> FarmHelperConfig.increasedCrops);
                break;
            case ID_HITBOX_NW:
                flip(() -> FarmHelperConfig.increasedNetherWarts = !FarmHelperConfig.increasedNetherWarts,
                        button, "Bigger Nether Wart Hitboxes", () -> FarmHelperConfig.increasedNetherWarts);
                break;
            case ID_HITBOX_COCOA:
                flip(() -> FarmHelperConfig.increasedCocoaBeans = !FarmHelperConfig.increasedCocoaBeans,
                        button, "Bigger Cocoa Hitboxes", () -> FarmHelperConfig.increasedCocoaBeans);
                break;
            case ID_HITBOX_MUSHROOM:
                flip(() -> FarmHelperConfig.increasedMushrooms = !FarmHelperConfig.increasedMushrooms,
                        button, "Bigger Mushroom Hitboxes", () -> FarmHelperConfig.increasedMushrooms);
                break;
            case ID_PINGLESS_CACTUS:
                flip(() -> FarmHelperConfig.pinglessCactus = !FarmHelperConfig.pinglessCactus,
                        button, "Pingless Cactus", () -> FarmHelperConfig.pinglessCactus);
                break;

            case ID_SET_SPAWN:
                PlayerUtils.setSpawnLocation();
                initGui();
                break;
            case ID_RESET_SPAWN:
                FarmHelperConfig.spawnPosX = 0;
                FarmHelperConfig.spawnPosY = 0;
                FarmHelperConfig.spawnPosZ = 0;
                FarmHelperConfig.spawnYaw = 0;
                FarmHelperConfig.spawnPitch = 0;
                FarmHelperConfig.spawnPlot = 0;
                saveConfig();
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

            case ID_FAILSAFE_ACTION:
                FarmHelperConfig.failsafeAction = !FarmHelperConfig.failsafeAction;
                button.displayString = FarmHelperConfig.failsafeAction ? "Failsafe: Disable Macro" : "Failsafe: React";
                break;
            case ID_FAILSAFE_SOUND:
                flip(() -> FarmHelperConfig.enableFailsafeSound = !FarmHelperConfig.enableFailsafeSound,
                        button, "Failsafe Sound", () -> FarmHelperConfig.enableFailsafeSound);
                break;
            case ID_SOUND_TYPE:
                FarmHelperConfig.failsafeMcSoundSelected = (FarmHelperConfig.failsafeMcSoundSelected + 1) % SOUND_LABELS.length;
                button.displayString = soundTypeLabel();
                break;
            case ID_MAX_SOUNDS:
                flip(() -> FarmHelperConfig.maxOutMinecraftSounds = !FarmHelperConfig.maxOutMinecraftSounds,
                        button, "Max Out MC Sounds", () -> FarmHelperConfig.maxOutMinecraftSounds);
                break;
            case ID_RESTART_AFTER:
                flip(() -> FarmHelperConfig.enableRestartAfterFailSafe = !FarmHelperConfig.enableRestartAfterFailSafe,
                        button, "Restart After Failsafe", () -> FarmHelperConfig.enableRestartAfterFailSafe);
                break;
            case ID_AUTO_WARP_WORLD:
                flip(() -> FarmHelperConfig.autoWarpOnWorldChange = !FarmHelperConfig.autoWarpOnWorldChange,
                        button, "Auto Warp on World Change", () -> FarmHelperConfig.autoWarpOnWorldChange);
                break;
            case ID_AUTO_WARP_GARDEN:
                flip(() -> FarmHelperConfig.alwaysTeleportToGarden = !FarmHelperConfig.alwaysTeleportToGarden,
                        button, "Always Teleport Garden", () -> FarmHelperConfig.alwaysTeleportToGarden);
                break;
            case ID_FAILSAFE_MESSAGES:
                flip(() -> FarmHelperConfig.sendFailsafeMessage = !FarmHelperConfig.sendFailsafeMessage,
                        button, "Failsafe Chat Messages", () -> FarmHelperConfig.sendFailsafeMessage);
                break;
            case ID_POPUP_NOTIFS:
                flip(() -> FarmHelperConfig.popUpNotifications = !FarmHelperConfig.popUpNotifications,
                        button, "Pop-up Notifications", () -> FarmHelperConfig.popUpNotifications);
                break;

            case ID_ANTI_STUCK:
                flip(() -> FarmHelperConfig.tmpAntiStuckEnabled = !FarmHelperConfig.tmpAntiStuckEnabled,
                        button, "Anti Stuck", () -> FarmHelperConfig.tmpAntiStuckEnabled);
                break;
            case ID_ANTISTUCK_TRIES_MINUS:
                FarmHelperConfig.antiStuckTriesUntilRewarp = Math.max(3, FarmHelperConfig.antiStuckTriesUntilRewarp - 1);
                initGui();
                break;
            case ID_ANTISTUCK_TRIES_PLUS:
                FarmHelperConfig.antiStuckTriesUntilRewarp = Math.min(10, FarmHelperConfig.antiStuckTriesUntilRewarp + 1);
                initGui();
                break;
            case ID_FAST_BREAK:
                flip(() -> FarmHelperConfig.fastBreak = !FarmHelperConfig.fastBreak,
                        button, "Fast Break", () -> FarmHelperConfig.fastBreak);
                break;
            case ID_FB_SPEED_MINUS:
                FarmHelperConfig.fastBreakSpeed = Math.max(1, FarmHelperConfig.fastBreakSpeed - 1);
                initGui();
                break;
            case ID_FB_SPEED_PLUS:
                FarmHelperConfig.fastBreakSpeed = Math.min(5, FarmHelperConfig.fastBreakSpeed + 1);
                initGui();
                break;
            case ID_FB_RANDOM:
                flip(() -> FarmHelperConfig.fastBreakRandomization = !FarmHelperConfig.fastBreakRandomization,
                        button, "FB Randomization", () -> FarmHelperConfig.fastBreakRandomization);
                break;
            case ID_FB_JACOB_OFF:
                flip(() -> FarmHelperConfig.disableFastBreakDuringJacobsContest = !FarmHelperConfig.disableFastBreakDuringJacobsContest,
                        button, "Disable FB in Jacob", () -> FarmHelperConfig.disableFastBreakDuringJacobsContest);
                break;
            case ID_DESYNC:
                flip(() -> FarmHelperConfig.checkDesync = !FarmHelperConfig.checkDesync,
                        button, "Desync Check", () -> FarmHelperConfig.checkDesync);
                break;
            case ID_BPS_CHECK:
                flip(() -> FarmHelperConfig.enableBpsCheck = !FarmHelperConfig.enableBpsCheck,
                        button, "Lower BPS Failsafe", () -> FarmHelperConfig.enableBpsCheck);
                break;
            case ID_MUTE_GAME:
                flip(() -> FarmHelperConfig.muteTheGame = !FarmHelperConfig.muteTheGame,
                        button, "Mute Farm", () -> FarmHelperConfig.muteTheGame);
                break;
            case ID_STREAMER:
                flip(() -> FarmHelperConfig.streamerMode = !FarmHelperConfig.streamerMode,
                        button, "Streamer", () -> FarmHelperConfig.streamerMode);
                break;
            case ID_HUD_OUTSIDE:
                flip(() -> FarmHelperConfig.showStatusHudOutsideGarden = !FarmHelperConfig.showStatusHudOutsideGarden,
                        button, "HUD Outside", () -> FarmHelperConfig.showStatusHudOutsideGarden);
                break;
            case ID_RESET_STATS:
                flip(() -> FarmHelperConfig.resetStatsBetweenDisabling = !FarmHelperConfig.resetStatsBetweenDisabling,
                        button, "Reset Stats", () -> FarmHelperConfig.resetStatsBetweenDisabling);
                break;
            case ID_DEBUG:
                flip(() -> FarmHelperConfig.debugMode = !FarmHelperConfig.debugMode,
                        button, "Debug Mode", () -> FarmHelperConfig.debugMode);
                break;
            default:
                break;
        }
    }

    private interface BoolFlip {
        void run();
    }

    private interface BoolGet {
        boolean get();
    }

    private void flip(BoolFlip flip, GuiButton button, String name, BoolGet get) {
        flip.run();
        button.displayString = toggleLabel(name, get.get());
    }

    private void setFromCurrentLook(boolean pitch, boolean yaw) {
        if (this.mc.thePlayer == null) {
            LogUtils.sendError("Cannot read look angle (not in world).");
            return;
        }
        if (pitch) {
            FarmHelperConfig.customPitchLevel = clampPitch(this.mc.thePlayer.rotationPitch);
            FarmHelperConfig.customPitch = true;
        }
        if (yaw) {
            FarmHelperConfig.customYawLevel = normalizeYaw(this.mc.thePlayer.rotationYaw);
            FarmHelperConfig.customYaw = true;
        }
        saveConfig();
        LogUtils.sendSuccess(String.format("Custom rotation — pitch %.1f, yaw %.1f",
                FarmHelperConfig.customPitchLevel, FarmHelperConfig.customYawLevel));
    }

    private static float clampPitch(float pitch) {
        return MathHelper.clamp_float(pitch, -90f, 90f);
    }

    private static float normalizeYaw(float yaw) {
        yaw = yaw % 360f;
        if (yaw > 180f) yaw -= 360f;
        if (yaw < -180f) yaw += 360f;
        return yaw;
    }

    private void saveConfig() {
        if (FarmHelper.config != null) {
            FarmHelper.config.save();
        }
    }

    private void saveAndClose() {
        saveConfig();
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
        if (idx < 0 || idx >= MACRO_LABELS.length) idx = 0;
        return "Macro: " + MACRO_LABELS[idx];
    }

    private static String soundTypeLabel() {
        int idx = FarmHelperConfig.failsafeMcSoundSelected;
        if (idx < 0 || idx >= SOUND_LABELS.length) idx = 0;
        return SOUND_LABELS[idx];
    }

    private static String toggleLabel(String name, boolean on) {
        return name + ": " + (on ? "ON" : "OFF");
    }
}
