package com.jelly.farmhelperv2.gui;

import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;

import java.io.IOException;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Full FarmX settings for Android / GL4ES — every important config option without OneConfig.
 */
public class FarmXMobileGui extends GuiScreen {
    private static final String[] PAGE_NAMES = {
            "Farming", "Rotation", "Crop Utils", "Rewarp & Spawn",
            "Failsafe", "Detection", "Delays Rows", "Delays Rot/Rewarp", "Misc"
    };
    private int page = 0;

    private static final int ID_PREV = 900;
    private static final int ID_NEXT = 901;
    private static final int ID_SAVE = 902;

    private static final String[] MACRO_LABELS = {
            "S Vert Crops", "S Pumpkin/Melon", "S Melongkingde", "S Default Plot",
            "S Sugar Cane", "S Cactus", "S Cactus SunTzu", "S Cocoa",
            "S Cocoa Trapdoors", "S Cocoa L/R", "S Mushroom 45", "S Mushroom 30",
            "S Mushroom SDS", "Circle Crops"
    };
    private static final String[] SOUND_LABELS = {"Sound: Orb", "Sound: Anvil"};

    // Button IDs
    private static final int ID_MACRO = 1;
    private static final int ID_ALWAYS_W = 2;
    private static final int ID_HOLD_LMB = 3;
    private static final int ID_ROT_WARP = 4;
    private static final int ID_ROT_DROP = 5;
    private static final int ID_DONT_FIX = 6;
    private static final int ID_AUTO_TOOL = 7;

    private static final int ID_C_PITCH = 10;
    private static final int ID_C_YAW = 11;
    private static final int ID_SET_BOTH = 12;
    private static final int ID_SET_PITCH = 13;
    private static final int ID_SET_YAW = 14;
    private static final int ID_P_M = 15;
    private static final int ID_P_P = 16;
    private static final int ID_Y_M = 17;
    private static final int ID_Y_P = 18;

    private static final int ID_HB_CROP = 20;
    private static final int ID_HB_NW = 21;
    private static final int ID_HB_COCOA = 22;
    private static final int ID_HB_MUSH = 23;
    private static final int ID_PING_CACTUS = 24;

    private static final int ID_SET_SPAWN = 30;
    private static final int ID_RESET_SPAWN = 31;
    private static final int ID_ADD_RW = 32;
    private static final int ID_REM_RW = 33;
    private static final int ID_REM_ALL_RW = 34;

    private static final int ID_FS_ACTION = 40;
    private static final int ID_FS_SOUND = 41;
    private static final int ID_FS_SOUND_TYPE = 42;
    private static final int ID_FS_MAX_SND = 43;
    private static final int ID_FS_RESTART = 44;
    private static final int ID_FS_WARP_WORLD = 45;
    private static final int ID_FS_WARP_GARDEN = 46;
    private static final int ID_FS_MSG = 47;
    private static final int ID_FS_POPUP = 48;
    private static final int ID_FS_STOP_M = 49;
    private static final int ID_FS_STOP_P = 50;
    private static final int ID_FS_RDELAY_M = 51;
    private static final int ID_FS_RDELAY_P = 52;

    private static final int ID_DET_LAG_M = 60;
    private static final int ID_DET_LAG_P = 61;
    private static final int ID_DET_WIN_M = 62;
    private static final int ID_DET_WIN_P = 63;
    private static final int ID_DET_PITCH_M = 64;
    private static final int ID_DET_PITCH_P = 65;
    private static final int ID_DET_YAW_M = 66;
    private static final int ID_DET_YAW_P = 67;
    private static final int ID_DET_TP_M = 68;
    private static final int ID_DET_TP_P = 69;
    private static final int ID_DET_KB_M = 70;
    private static final int ID_DET_KB_P = 71;
    private static final int ID_BPS_ON = 72;
    private static final int ID_BPS_M = 73;
    private static final int ID_BPS_P = 74;
    private static final int ID_DESYNC_ON = 75;
    private static final int ID_DESYNC_M = 76;
    private static final int ID_DESYNC_P = 77;

    private static final int ID_ROW_T_M = 80;
    private static final int ID_ROW_T_P = 81;
    private static final int ID_ROW_R_M = 82;
    private static final int ID_ROW_R_P = 83;
    private static final int ID_ROW_JACOB = 84;
    private static final int ID_ROW_JT_M = 85;
    private static final int ID_ROW_JT_P = 86;
    private static final int ID_ROW_JR_M = 87;
    private static final int ID_ROW_JR_P = 88;

    private static final int ID_ROT_T_M = 90;
    private static final int ID_ROT_T_P = 91;
    private static final int ID_ROT_R_M = 92;
    private static final int ID_ROT_R_P = 93;
    private static final int ID_ROT_JACOB = 94;
    private static final int ID_ROT_JT_M = 95;
    private static final int ID_ROT_JT_P = 96;
    private static final int ID_ROT_JR_M = 97;
    private static final int ID_ROT_JR_P = 98;
    private static final int ID_FLY_T_M = 110;
    private static final int ID_FLY_T_P = 111;
    private static final int ID_FLY_R_M = 112;
    private static final int ID_FLY_R_P = 113;
    private static final int ID_GUI_T_M = 114;
    private static final int ID_GUI_T_P = 115;
    private static final int ID_GUI_R_M = 116;
    private static final int ID_GUI_R_P = 117;
    private static final int ID_RW_T_M = 118;
    private static final int ID_RW_T_P = 119;
    private static final int ID_RW_R_M = 120;
    private static final int ID_RW_R_P = 121;

    private static final int ID_ANTISTUCK = 130;
    private static final int ID_AS_TRIES_M = 131;
    private static final int ID_AS_TRIES_P = 132;
    private static final int ID_FB = 133;
    private static final int ID_FB_SP_M = 134;
    private static final int ID_FB_SP_P = 135;
    private static final int ID_FB_RAND = 136;
    private static final int ID_FB_CH_M = 137;
    private static final int ID_FB_CH_P = 138;
    private static final int ID_FB_JACOB = 139;
    private static final int ID_MUTE = 140;
    private static final int ID_STREAMER = 141;
    private static final int ID_HUD_OUT = 142;
    private static final int ID_RESET_STATS = 143;
    private static final int ID_DEBUG = 144;
    private static final int ID_DEBUG_FLY = 145;
    private static final int ID_PROFIT_CULT = 146;
    private static final int ID_JACOB_CROPS = 147;
    private static final int ID_PDOTT = 148;

    @Override
    public void initGui() {
        this.buttonList.clear();
        int cx = this.width / 2 - 100;
        int y = 32;
        int g = 18;
        int half = 98;
        int third = 64;

        this.buttonList.add(new GuiButton(ID_PREV, this.width / 2 - 105, this.height - 44, 70, 20, "< Prev"));
        this.buttonList.add(new GuiButton(ID_NEXT, this.width / 2 - 30, this.height - 44, 70, 20, "Next >"));
        this.buttonList.add(new GuiButton(ID_SAVE, this.width / 2 + 45, this.height - 44, 60, 20, "Save"));

        switch (page) {
            case 0:
                btn(ID_MACRO, cx, y, 200, macroLabel()); y += g;
                btn(ID_ALWAYS_W, cx, y, 200, on("Always Hold W", FarmHelperConfig.alwaysHoldW)); y += g;
                btn(ID_HOLD_LMB, cx, y, 200, on("Hold LMB Row Change", FarmHelperConfig.holdLeftClickWhenChangingRow)); y += g;
                btn(ID_ROT_WARP, cx, y, 200, on("Rotate After Warp", FarmHelperConfig.rotateAfterWarped)); y += g;
                btn(ID_ROT_DROP, cx, y, 200, on("Rotate After Drop", FarmHelperConfig.rotateAfterDrop)); y += g;
                btn(ID_DONT_FIX, cx, y, 200, on("Don't Fix After Warp", FarmHelperConfig.dontFixAfterWarping)); y += g;
                btn(ID_AUTO_TOOL, cx, y, 200, on("Auto Switch Tool", FarmHelperConfig.autoSwitchTool));
                break;
            case 1:
                btn(ID_C_PITCH, cx, y, 200, on("Custom Pitch", FarmHelperConfig.customPitch)); y += g;
                btn(ID_C_YAW, cx, y, 200, on("Custom Yaw", FarmHelperConfig.customYaw)); y += g;
                btn(ID_SET_BOTH, cx, y, 200, "Set Both From Look"); y += g;
                btn(ID_SET_PITCH, this.width / 2 - 105, y, half, "Pitch From Look");
                btn(ID_SET_YAW, this.width / 2 + 7, y, half, "Yaw From Look"); y += g;
                btn(ID_P_M, this.width / 2 - 105, y, half, "Pitch -1");
                btn(ID_P_P, this.width / 2 + 7, y, half, "Pitch +1"); y += g;
                btn(ID_Y_M, this.width / 2 - 105, y, half, "Yaw -5");
                btn(ID_Y_P, this.width / 2 + 7, y, half, "Yaw +5");
                break;
            case 2:
                btn(ID_HB_CROP, cx, y, 200, on("Bigger Crop Hitboxes", FarmHelperConfig.increasedCrops)); y += g;
                btn(ID_HB_NW, cx, y, 200, on("Bigger NW Hitboxes", FarmHelperConfig.increasedNetherWarts)); y += g;
                btn(ID_HB_COCOA, cx, y, 200, on("Bigger Cocoa Hitboxes", FarmHelperConfig.increasedCocoaBeans)); y += g;
                btn(ID_HB_MUSH, cx, y, 200, on("Bigger Mushroom Hitboxes", FarmHelperConfig.increasedMushrooms)); y += g;
                btn(ID_PING_CACTUS, cx, y, 200, on("Pingless Cactus", FarmHelperConfig.pinglessCactus));
                break;
            case 3:
                btn(ID_SET_SPAWN, cx, y, 200, "Set Spawn (current pos)"); y += g;
                btn(ID_RESET_SPAWN, cx, y, 200, "Reset Spawn"); y += g + 2;
                btn(ID_ADD_RW, cx, y, 200, "Add Rewarp Here"); y += g;
                btn(ID_REM_RW, this.width / 2 - 105, y, half, "Remove Closest");
                btn(ID_REM_ALL_RW, this.width / 2 + 7, y, half, "Remove All");
                break;
            case 4:
                btn(ID_FS_ACTION, cx, y, 200, FarmHelperConfig.failsafeAction ? "Failsafe: Disable" : "Failsafe: React"); y += g;
                btn(ID_FS_SOUND, cx, y, 200, on("Failsafe Sound", FarmHelperConfig.enableFailsafeSound)); y += g;
                btn(ID_FS_SOUND_TYPE, cx, y, 200, soundLabel()); y += g;
                btn(ID_FS_MAX_SND, cx, y, 200, on("Max Out MC Sounds", FarmHelperConfig.maxOutMinecraftSounds)); y += g;
                btn(ID_FS_RESTART, cx, y, 200, on("Restart After Failsafe", FarmHelperConfig.enableRestartAfterFailSafe)); y += g;
                btn(ID_FS_WARP_WORLD, cx, y, 200, on("Auto Warp World Change", FarmHelperConfig.autoWarpOnWorldChange)); y += g;
                btn(ID_FS_WARP_GARDEN, cx, y, 200, on("Always TP Garden", FarmHelperConfig.alwaysTeleportToGarden)); y += g;
                btn(ID_FS_MSG, cx, y, 200, on("Failsafe Chat Msgs", FarmHelperConfig.sendFailsafeMessage)); y += g;
                btn(ID_FS_POPUP, cx, y, 200, on("Pop-up Notifications", FarmHelperConfig.popUpNotifications)); y += g;
                pair(ID_FS_STOP_M, ID_FS_STOP_P, y, "StopDelay " + FarmHelperConfig.failsafeStopDelay + "ms"); y += g;
                pair(ID_FS_RDELAY_M, ID_FS_RDELAY_P, y, "RestartDelay " + FarmHelperConfig.restartAfterFailSafeDelay + "m");
                break;
            case 5:
                pair(ID_DET_LAG_M, ID_DET_LAG_P, y, "TP Lag Tol " + fmt(FarmHelperConfig.teleportLagTolerance)); y += g;
                pair(ID_DET_WIN_M, ID_DET_WIN_P, y, "Detect Window " + FarmHelperConfig.detectionTimeWindow + "ms"); y += g;
                pair(ID_DET_PITCH_M, ID_DET_PITCH_P, y, "Pitch Sens " + fmt(FarmHelperConfig.pitchSensitivity)); y += g;
                pair(ID_DET_YAW_M, ID_DET_YAW_P, y, "Yaw Sens " + fmt(FarmHelperConfig.yawSensitivity)); y += g;
                pair(ID_DET_TP_M, ID_DET_TP_P, y, "TP Dist " + fmt(FarmHelperConfig.teleportDistanceThreshold)); y += g;
                pair(ID_DET_KB_M, ID_DET_KB_P, y, "Vert KB " + fmt(FarmHelperConfig.verticalKnockbackThreshold)); y += g;
                btn(ID_BPS_ON, cx, y, 200, on("BPS Check", FarmHelperConfig.enableBpsCheck)); y += g;
                pair(ID_BPS_M, ID_BPS_P, y, "Min BPS " + fmt(FarmHelperConfig.minBpsThreshold)); y += g;
                btn(ID_DESYNC_ON, cx, y, 200, on("Desync Check", FarmHelperConfig.checkDesync)); y += g;
                pair(ID_DESYNC_M, ID_DESYNC_P, y, "Desync Pause " + FarmHelperConfig.desyncPauseDelay + "ms");
                break;
            case 6:
                pair(ID_ROW_T_M, ID_ROW_T_P, y, "Row Delay " + fmt(FarmHelperConfig.timeBetweenChangingRows) + "ms"); y += g;
                pair(ID_ROW_R_M, ID_ROW_R_P, y, "Row Random +" + fmt(FarmHelperConfig.randomTimeBetweenChangingRows) + "ms"); y += g;
                btn(ID_ROW_JACOB, cx, y, 200, on("Custom Row Delays Jacob", FarmHelperConfig.customRowChangeDelaysDuringJacob)); y += g;
                pair(ID_ROW_JT_M, ID_ROW_JT_P, y, "Jacob Row " + fmt(FarmHelperConfig.timeBetweenChangingRowsDuringJacob) + "ms"); y += g;
                pair(ID_ROW_JR_M, ID_ROW_JR_P, y, "Jacob Row Rand +" + fmt(FarmHelperConfig.randomTimeBetweenChangingRowsDuringJacob) + "ms");
                break;
            case 7:
                pair(ID_ROT_T_M, ID_ROT_T_P, y, "Rot Time " + fmt(FarmHelperConfig.rotationTime) + "ms"); y += g;
                pair(ID_ROT_R_M, ID_ROT_R_P, y, "Rot Random +" + fmt(FarmHelperConfig.rotationTimeRandomness) + "ms"); y += g;
                btn(ID_ROT_JACOB, cx, y, 200, on("Custom Rot Delays Jacob", FarmHelperConfig.customRotationDelaysDuringJacob)); y += g;
                pair(ID_ROT_JT_M, ID_ROT_JT_P, y, "Jacob Rot " + fmt(FarmHelperConfig.rotationTimeDuringJacob) + "ms"); y += g;
                pair(ID_ROT_JR_M, ID_ROT_JR_P, y, "Jacob Rot Rand +" + fmt(FarmHelperConfig.rotationTimeRandomnessDuringJacob) + "ms"); y += g;
                pair(ID_FLY_T_M, ID_FLY_T_P, y, "Fly Rot " + fmt(FarmHelperConfig.flyPathExecutionerRotationTime) + "ms"); y += g;
                pair(ID_FLY_R_M, ID_FLY_R_P, y, "Fly Rot Rand +" + fmt(FarmHelperConfig.flyPathExecutionerRotationTimeRandomness) + "ms"); y += g;
                pair(ID_GUI_T_M, ID_GUI_T_P, y, "GUI Delay " + fmt(FarmHelperConfig.macroGuiDelay) + "ms"); y += g;
                pair(ID_GUI_R_M, ID_GUI_R_P, y, "GUI Rand +" + fmt(FarmHelperConfig.macroGuiDelayRandomness) + "ms"); y += g;
                pair(ID_RW_T_M, ID_RW_T_P, y, "Rewarp Delay " + fmt(FarmHelperConfig.rewarpDelay) + "ms"); y += g;
                pair(ID_RW_R_M, ID_RW_R_P, y, "Rewarp Rand +" + fmt(FarmHelperConfig.rewarpDelayRandomness) + "ms");
                break;
            case 8:
            default:
                btn(ID_ANTISTUCK, cx, y, 200, on("Anti Stuck", FarmHelperConfig.tmpAntiStuckEnabled)); y += g;
                pair(ID_AS_TRIES_M, ID_AS_TRIES_P, y, "AntiStuck Tries " + FarmHelperConfig.antiStuckTriesUntilRewarp); y += g;
                btn(ID_FB, cx, y, 200, on("Fast Break", FarmHelperConfig.fastBreak)); y += g;
                pair(ID_FB_SP_M, ID_FB_SP_P, y, "FB Speed " + FarmHelperConfig.fastBreakSpeed); y += g;
                btn(ID_FB_RAND, cx, y, 200, on("FB Randomization", FarmHelperConfig.fastBreakRandomization)); y += g;
                pair(ID_FB_CH_M, ID_FB_CH_P, y, "FB Chance " + FarmHelperConfig.fastBreakRandomizationChance + "%"); y += g;
                btn(ID_FB_JACOB, cx, y, 200, on("Disable FB in Jacob", FarmHelperConfig.disableFastBreakDuringJacobsContest)); y += g;
                btn(ID_MUTE, this.width / 2 - 105, y, half, on("Mute", FarmHelperConfig.muteTheGame));
                btn(ID_STREAMER, this.width / 2 + 7, y, half, on("Streamer", FarmHelperConfig.streamerMode)); y += g;
                btn(ID_HUD_OUT, this.width / 2 - 105, y, half, on("HUD Out", FarmHelperConfig.showStatusHudOutsideGarden));
                btn(ID_RESET_STATS, this.width / 2 + 7, y, half, on("Reset Stats", FarmHelperConfig.resetStatsBetweenDisabling)); y += g;
                btn(ID_DEBUG, this.width / 2 - 105, y, half, on("Debug", FarmHelperConfig.debugMode));
                btn(ID_DEBUG_FLY, this.width / 2 + 7, y, half, on("New Fly", FarmHelperConfig.debugNewFly)); y += g;
                btn(ID_PROFIT_CULT, cx, y, 200, on("Profit via Cultivating", FarmHelperConfig.profitCalculatorCultivatingEnchant)); y += g;
                btn(ID_JACOB_CROPS, cx, y, 200, on("Jacob Current Crops Only", FarmHelperConfig.jacobContestCurrentCropsOnly)); y += g;
                btn(ID_PDOTT, cx, y, 200, on("PD OTT Debug Logs", FarmHelperConfig.showDebugLogsAboutPDOTT));
                break;
        }
    }

    private void btn(int id, int x, int y, int w, String text) {
        this.buttonList.add(new GuiButton(id, x, y, w, 20, text));
    }

    private void pair(int idMinus, int idPlus, int y, String label) {
        this.buttonList.add(new GuiButton(idMinus, this.width / 2 - 105, y, 20, 20, "-"));
        this.buttonList.add(new GuiButton(idPlus, this.width / 2 + 85, y, 20, 20, "+"));
        this.buttonList.add(new GuiButton(-1, this.width / 2 - 82, y, 164, 20, label));
        // center label button is non-functional (id -1 ignored)
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "FarmX — " + PAGE_NAMES[page], this.width / 2, 8, 0xFFFFFF);
        if (page == 1) {
            this.drawCenteredString(this.fontRendererObj,
                    String.format(Locale.US, "Pitch %s %.1f | Yaw %s %.1f",
                            FarmHelperConfig.customPitch ? "ON" : "OFF", FarmHelperConfig.customPitchLevel,
                            FarmHelperConfig.customYaw ? "ON" : "OFF", FarmHelperConfig.customYawLevel),
                    this.width / 2, this.height - 66, 0x55FF55);
        } else if (page == 3) {
            String spawn = PlayerUtils.isSpawnLocationSet()
                    ? ("Spawn " + FarmHelperConfig.spawnPosX + "," + FarmHelperConfig.spawnPosY + "," + FarmHelperConfig.spawnPosZ)
                    : "Spawn not set";
            this.drawCenteredString(this.fontRendererObj, spawn + " | Rewarps " + FarmHelperConfig.rewarpList.size(),
                    this.width / 2, this.height - 66, 0x55FF55);
        }
        this.drawCenteredString(this.fontRendererObj,
                (page + 1) + "/" + PAGE_NAMES.length + "  /fh /fhrewarp /fhspawn /fhrot",
                this.width / 2, this.height - 56, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id < 0) return;
        switch (button.id) {
            case ID_PREV:
                page = (page + PAGE_NAMES.length - 1) % PAGE_NAMES.length;
                initGui();
                return;
            case ID_NEXT:
                page = (page + 1) % PAGE_NAMES.length;
                initGui();
                return;
            case ID_SAVE:
                saveAndClose();
                return;

            case ID_MACRO:
                FarmHelperConfig.macroType = (FarmHelperConfig.macroType + 1) % MACRO_LABELS.length;
                button.displayString = macroLabel();
                break;
            case ID_ALWAYS_W: FarmHelperConfig.alwaysHoldW = !FarmHelperConfig.alwaysHoldW; button.displayString = on("Always Hold W", FarmHelperConfig.alwaysHoldW); break;
            case ID_HOLD_LMB: FarmHelperConfig.holdLeftClickWhenChangingRow = !FarmHelperConfig.holdLeftClickWhenChangingRow; button.displayString = on("Hold LMB Row Change", FarmHelperConfig.holdLeftClickWhenChangingRow); break;
            case ID_ROT_WARP: FarmHelperConfig.rotateAfterWarped = !FarmHelperConfig.rotateAfterWarped; button.displayString = on("Rotate After Warp", FarmHelperConfig.rotateAfterWarped); break;
            case ID_ROT_DROP: FarmHelperConfig.rotateAfterDrop = !FarmHelperConfig.rotateAfterDrop; button.displayString = on("Rotate After Drop", FarmHelperConfig.rotateAfterDrop); break;
            case ID_DONT_FIX: FarmHelperConfig.dontFixAfterWarping = !FarmHelperConfig.dontFixAfterWarping; button.displayString = on("Don't Fix After Warp", FarmHelperConfig.dontFixAfterWarping); break;
            case ID_AUTO_TOOL: FarmHelperConfig.autoSwitchTool = !FarmHelperConfig.autoSwitchTool; button.displayString = on("Auto Switch Tool", FarmHelperConfig.autoSwitchTool); break;

            case ID_C_PITCH: FarmHelperConfig.customPitch = !FarmHelperConfig.customPitch; button.displayString = on("Custom Pitch", FarmHelperConfig.customPitch); break;
            case ID_C_YAW: FarmHelperConfig.customYaw = !FarmHelperConfig.customYaw; button.displayString = on("Custom Yaw", FarmHelperConfig.customYaw); break;
            case ID_SET_BOTH: setLook(true, true); initGui(); break;
            case ID_SET_PITCH: setLook(true, false); initGui(); break;
            case ID_SET_YAW: setLook(false, true); initGui(); break;
            case ID_P_M: FarmHelperConfig.customPitchLevel = clampPitch(FarmHelperConfig.customPitchLevel - 1); FarmHelperConfig.customPitch = true; initGui(); break;
            case ID_P_P: FarmHelperConfig.customPitchLevel = clampPitch(FarmHelperConfig.customPitchLevel + 1); FarmHelperConfig.customPitch = true; initGui(); break;
            case ID_Y_M: FarmHelperConfig.customYawLevel = normYaw(FarmHelperConfig.customYawLevel - 5); FarmHelperConfig.customYaw = true; initGui(); break;
            case ID_Y_P: FarmHelperConfig.customYawLevel = normYaw(FarmHelperConfig.customYawLevel + 5); FarmHelperConfig.customYaw = true; initGui(); break;

            case ID_HB_CROP: FarmHelperConfig.increasedCrops = !FarmHelperConfig.increasedCrops; button.displayString = on("Bigger Crop Hitboxes", FarmHelperConfig.increasedCrops); break;
            case ID_HB_NW: FarmHelperConfig.increasedNetherWarts = !FarmHelperConfig.increasedNetherWarts; button.displayString = on("Bigger NW Hitboxes", FarmHelperConfig.increasedNetherWarts); break;
            case ID_HB_COCOA: FarmHelperConfig.increasedCocoaBeans = !FarmHelperConfig.increasedCocoaBeans; button.displayString = on("Bigger Cocoa Hitboxes", FarmHelperConfig.increasedCocoaBeans); break;
            case ID_HB_MUSH: FarmHelperConfig.increasedMushrooms = !FarmHelperConfig.increasedMushrooms; button.displayString = on("Bigger Mushroom Hitboxes", FarmHelperConfig.increasedMushrooms); break;
            case ID_PING_CACTUS: FarmHelperConfig.pinglessCactus = !FarmHelperConfig.pinglessCactus; button.displayString = on("Pingless Cactus", FarmHelperConfig.pinglessCactus); break;

            case ID_SET_SPAWN: PlayerUtils.setSpawnLocation(); initGui(); break;
            case ID_RESET_SPAWN:
                FarmHelperConfig.spawnPosX = FarmHelperConfig.spawnPosY = FarmHelperConfig.spawnPosZ = 0;
                FarmHelperConfig.spawnYaw = FarmHelperConfig.spawnPitch = 0;
                FarmHelperConfig.spawnPlot = 0;
                save(); LogUtils.sendSuccess("Spawn reset"); initGui(); break;
            case ID_ADD_RW: FarmHelperConfig.addRewarp(); initGui(); break;
            case ID_REM_RW: FarmHelperConfig.removeRewarp(); initGui(); break;
            case ID_REM_ALL_RW: FarmHelperConfig.removeAllRewarps(); initGui(); break;

            case ID_FS_ACTION: FarmHelperConfig.failsafeAction = !FarmHelperConfig.failsafeAction; button.displayString = FarmHelperConfig.failsafeAction ? "Failsafe: Disable" : "Failsafe: React"; break;
            case ID_FS_SOUND: FarmHelperConfig.enableFailsafeSound = !FarmHelperConfig.enableFailsafeSound; button.displayString = on("Failsafe Sound", FarmHelperConfig.enableFailsafeSound); break;
            case ID_FS_SOUND_TYPE: FarmHelperConfig.failsafeMcSoundSelected = (FarmHelperConfig.failsafeMcSoundSelected + 1) % SOUND_LABELS.length; button.displayString = soundLabel(); break;
            case ID_FS_MAX_SND: FarmHelperConfig.maxOutMinecraftSounds = !FarmHelperConfig.maxOutMinecraftSounds; button.displayString = on("Max Out MC Sounds", FarmHelperConfig.maxOutMinecraftSounds); break;
            case ID_FS_RESTART: FarmHelperConfig.enableRestartAfterFailSafe = !FarmHelperConfig.enableRestartAfterFailSafe; button.displayString = on("Restart After Failsafe", FarmHelperConfig.enableRestartAfterFailSafe); break;
            case ID_FS_WARP_WORLD: FarmHelperConfig.autoWarpOnWorldChange = !FarmHelperConfig.autoWarpOnWorldChange; button.displayString = on("Auto Warp World Change", FarmHelperConfig.autoWarpOnWorldChange); break;
            case ID_FS_WARP_GARDEN: FarmHelperConfig.alwaysTeleportToGarden = !FarmHelperConfig.alwaysTeleportToGarden; button.displayString = on("Always TP Garden", FarmHelperConfig.alwaysTeleportToGarden); break;
            case ID_FS_MSG: FarmHelperConfig.sendFailsafeMessage = !FarmHelperConfig.sendFailsafeMessage; button.displayString = on("Failsafe Chat Msgs", FarmHelperConfig.sendFailsafeMessage); break;
            case ID_FS_POPUP: FarmHelperConfig.popUpNotifications = !FarmHelperConfig.popUpNotifications; button.displayString = on("Pop-up Notifications", FarmHelperConfig.popUpNotifications); break;
            case ID_FS_STOP_M: adjI(() -> FarmHelperConfig.failsafeStopDelay, v -> FarmHelperConfig.failsafeStopDelay = v, -100, 1000, 7500); break;
            case ID_FS_STOP_P: adjI(() -> FarmHelperConfig.failsafeStopDelay, v -> FarmHelperConfig.failsafeStopDelay = v, 100, 1000, 7500); break;
            case ID_FS_RDELAY_M: adjI(() -> FarmHelperConfig.restartAfterFailSafeDelay, v -> FarmHelperConfig.restartAfterFailSafeDelay = v, -1, 0, 20); break;
            case ID_FS_RDELAY_P: adjI(() -> FarmHelperConfig.restartAfterFailSafeDelay, v -> FarmHelperConfig.restartAfterFailSafeDelay = v, 1, 0, 20); break;

            case ID_DET_LAG_M: adjF(() -> FarmHelperConfig.teleportLagTolerance, v -> FarmHelperConfig.teleportLagTolerance = (float) v, -0.1, 0, 2); break;
            case ID_DET_LAG_P: adjF(() -> FarmHelperConfig.teleportLagTolerance, v -> FarmHelperConfig.teleportLagTolerance = (float) v, 0.1, 0, 2); break;
            case ID_DET_WIN_M: adjI(() -> FarmHelperConfig.detectionTimeWindow, v -> FarmHelperConfig.detectionTimeWindow = v, -50, 50, 4000); break;
            case ID_DET_WIN_P: adjI(() -> FarmHelperConfig.detectionTimeWindow, v -> FarmHelperConfig.detectionTimeWindow = v, 50, 50, 4000); break;
            case ID_DET_PITCH_M: adjF(() -> FarmHelperConfig.pitchSensitivity, v -> FarmHelperConfig.pitchSensitivity = (float) v, -1, 1, 30); break;
            case ID_DET_PITCH_P: adjF(() -> FarmHelperConfig.pitchSensitivity, v -> FarmHelperConfig.pitchSensitivity = (float) v, 1, 1, 30); break;
            case ID_DET_YAW_M: adjF(() -> FarmHelperConfig.yawSensitivity, v -> FarmHelperConfig.yawSensitivity = (float) v, -1, 1, 30); break;
            case ID_DET_YAW_P: adjF(() -> FarmHelperConfig.yawSensitivity, v -> FarmHelperConfig.yawSensitivity = (float) v, 1, 1, 30); break;
            case ID_DET_TP_M: adjF(() -> FarmHelperConfig.teleportDistanceThreshold, v -> FarmHelperConfig.teleportDistanceThreshold = (float) v, -0.5, 0.5, 20); break;
            case ID_DET_TP_P: adjF(() -> FarmHelperConfig.teleportDistanceThreshold, v -> FarmHelperConfig.teleportDistanceThreshold = (float) v, 0.5, 0.5, 20); break;
            case ID_DET_KB_M: adjF(() -> FarmHelperConfig.verticalKnockbackThreshold, v -> FarmHelperConfig.verticalKnockbackThreshold = (float) v, -500, 2000, 10000); break;
            case ID_DET_KB_P: adjF(() -> FarmHelperConfig.verticalKnockbackThreshold, v -> FarmHelperConfig.verticalKnockbackThreshold = (float) v, 500, 2000, 10000); break;
            case ID_BPS_ON: FarmHelperConfig.enableBpsCheck = !FarmHelperConfig.enableBpsCheck; button.displayString = on("BPS Check", FarmHelperConfig.enableBpsCheck); break;
            case ID_BPS_M: adjF(() -> FarmHelperConfig.minBpsThreshold, v -> FarmHelperConfig.minBpsThreshold = (float) v, -0.5, 5, 15); break;
            case ID_BPS_P: adjF(() -> FarmHelperConfig.minBpsThreshold, v -> FarmHelperConfig.minBpsThreshold = (float) v, 0.5, 5, 15); break;
            case ID_DESYNC_ON: FarmHelperConfig.checkDesync = !FarmHelperConfig.checkDesync; button.displayString = on("Desync Check", FarmHelperConfig.checkDesync); break;
            case ID_DESYNC_M: adjI(() -> FarmHelperConfig.desyncPauseDelay, v -> FarmHelperConfig.desyncPauseDelay = v, -250, 3000, 10000); break;
            case ID_DESYNC_P: adjI(() -> FarmHelperConfig.desyncPauseDelay, v -> FarmHelperConfig.desyncPauseDelay = v, 250, 3000, 10000); break;

            case ID_ROW_T_M: adjF(() -> FarmHelperConfig.timeBetweenChangingRows, v -> FarmHelperConfig.timeBetweenChangingRows = (float) v, -50, 0, 2000); break;
            case ID_ROW_T_P: adjF(() -> FarmHelperConfig.timeBetweenChangingRows, v -> FarmHelperConfig.timeBetweenChangingRows = (float) v, 50, 0, 2000); break;
            case ID_ROW_R_M: adjF(() -> FarmHelperConfig.randomTimeBetweenChangingRows, v -> FarmHelperConfig.randomTimeBetweenChangingRows = (float) v, -50, 0, 2000); break;
            case ID_ROW_R_P: adjF(() -> FarmHelperConfig.randomTimeBetweenChangingRows, v -> FarmHelperConfig.randomTimeBetweenChangingRows = (float) v, 50, 0, 2000); break;
            case ID_ROW_JACOB: FarmHelperConfig.customRowChangeDelaysDuringJacob = !FarmHelperConfig.customRowChangeDelaysDuringJacob; button.displayString = on("Custom Row Delays Jacob", FarmHelperConfig.customRowChangeDelaysDuringJacob); break;
            case ID_ROW_JT_M: adjF(() -> FarmHelperConfig.timeBetweenChangingRowsDuringJacob, v -> FarmHelperConfig.timeBetweenChangingRowsDuringJacob = (float) v, -50, 0, 2000); break;
            case ID_ROW_JT_P: adjF(() -> FarmHelperConfig.timeBetweenChangingRowsDuringJacob, v -> FarmHelperConfig.timeBetweenChangingRowsDuringJacob = (float) v, 50, 0, 2000); break;
            case ID_ROW_JR_M: adjF(() -> FarmHelperConfig.randomTimeBetweenChangingRowsDuringJacob, v -> FarmHelperConfig.randomTimeBetweenChangingRowsDuringJacob = (float) v, -50, 0, 2000); break;
            case ID_ROW_JR_P: adjF(() -> FarmHelperConfig.randomTimeBetweenChangingRowsDuringJacob, v -> FarmHelperConfig.randomTimeBetweenChangingRowsDuringJacob = (float) v, 50, 0, 2000); break;

            case ID_ROT_T_M: adjF(() -> FarmHelperConfig.rotationTime, v -> FarmHelperConfig.rotationTime = (float) v, -50, 200, 2000); break;
            case ID_ROT_T_P: adjF(() -> FarmHelperConfig.rotationTime, v -> FarmHelperConfig.rotationTime = (float) v, 50, 200, 2000); break;
            case ID_ROT_R_M: adjF(() -> FarmHelperConfig.rotationTimeRandomness, v -> FarmHelperConfig.rotationTimeRandomness = (float) v, -50, 0, 2000); break;
            case ID_ROT_R_P: adjF(() -> FarmHelperConfig.rotationTimeRandomness, v -> FarmHelperConfig.rotationTimeRandomness = (float) v, 50, 0, 2000); break;
            case ID_ROT_JACOB: FarmHelperConfig.customRotationDelaysDuringJacob = !FarmHelperConfig.customRotationDelaysDuringJacob; button.displayString = on("Custom Rot Delays Jacob", FarmHelperConfig.customRotationDelaysDuringJacob); break;
            case ID_ROT_JT_M: adjF(() -> FarmHelperConfig.rotationTimeDuringJacob, v -> FarmHelperConfig.rotationTimeDuringJacob = (float) v, -50, 200, 2000); break;
            case ID_ROT_JT_P: adjF(() -> FarmHelperConfig.rotationTimeDuringJacob, v -> FarmHelperConfig.rotationTimeDuringJacob = (float) v, 50, 200, 2000); break;
            case ID_ROT_JR_M: adjF(() -> FarmHelperConfig.rotationTimeRandomnessDuringJacob, v -> FarmHelperConfig.rotationTimeRandomnessDuringJacob = (float) v, -50, 0, 2000); break;
            case ID_ROT_JR_P: adjF(() -> FarmHelperConfig.rotationTimeRandomnessDuringJacob, v -> FarmHelperConfig.rotationTimeRandomnessDuringJacob = (float) v, 50, 0, 2000); break;
            case ID_FLY_T_M: adjF(() -> FarmHelperConfig.flyPathExecutionerRotationTime, v -> FarmHelperConfig.flyPathExecutionerRotationTime = (float) v, -50, 200, 2000); break;
            case ID_FLY_T_P: adjF(() -> FarmHelperConfig.flyPathExecutionerRotationTime, v -> FarmHelperConfig.flyPathExecutionerRotationTime = (float) v, 50, 200, 2000); break;
            case ID_FLY_R_M: adjF(() -> FarmHelperConfig.flyPathExecutionerRotationTimeRandomness, v -> FarmHelperConfig.flyPathExecutionerRotationTimeRandomness = (float) v, -50, 0, 2000); break;
            case ID_FLY_R_P: adjF(() -> FarmHelperConfig.flyPathExecutionerRotationTimeRandomness, v -> FarmHelperConfig.flyPathExecutionerRotationTimeRandomness = (float) v, 50, 0, 2000); break;
            case ID_GUI_T_M: adjF(() -> FarmHelperConfig.macroGuiDelay, v -> FarmHelperConfig.macroGuiDelay = (float) v, -50, 50, 2000); break;
            case ID_GUI_T_P: adjF(() -> FarmHelperConfig.macroGuiDelay, v -> FarmHelperConfig.macroGuiDelay = (float) v, 50, 50, 2000); break;
            case ID_GUI_R_M: adjF(() -> FarmHelperConfig.macroGuiDelayRandomness, v -> FarmHelperConfig.macroGuiDelayRandomness = (float) v, -50, 0, 2000); break;
            case ID_GUI_R_P: adjF(() -> FarmHelperConfig.macroGuiDelayRandomness, v -> FarmHelperConfig.macroGuiDelayRandomness = (float) v, 50, 0, 2000); break;
            case ID_RW_T_M: adjF(() -> FarmHelperConfig.rewarpDelay, v -> FarmHelperConfig.rewarpDelay = (float) v, -50, 250, 2000); break;
            case ID_RW_T_P: adjF(() -> FarmHelperConfig.rewarpDelay, v -> FarmHelperConfig.rewarpDelay = (float) v, 50, 250, 2000); break;
            case ID_RW_R_M: adjF(() -> FarmHelperConfig.rewarpDelayRandomness, v -> FarmHelperConfig.rewarpDelayRandomness = (float) v, -50, 0, 2000); break;
            case ID_RW_R_P: adjF(() -> FarmHelperConfig.rewarpDelayRandomness, v -> FarmHelperConfig.rewarpDelayRandomness = (float) v, 50, 0, 2000); break;

            case ID_ANTISTUCK: FarmHelperConfig.tmpAntiStuckEnabled = !FarmHelperConfig.tmpAntiStuckEnabled; button.displayString = on("Anti Stuck", FarmHelperConfig.tmpAntiStuckEnabled); break;
            case ID_AS_TRIES_M: adjI(() -> FarmHelperConfig.antiStuckTriesUntilRewarp, v -> FarmHelperConfig.antiStuckTriesUntilRewarp = v, -1, 3, 10); break;
            case ID_AS_TRIES_P: adjI(() -> FarmHelperConfig.antiStuckTriesUntilRewarp, v -> FarmHelperConfig.antiStuckTriesUntilRewarp = v, 1, 3, 10); break;
            case ID_FB: FarmHelperConfig.fastBreak = !FarmHelperConfig.fastBreak; button.displayString = on("Fast Break", FarmHelperConfig.fastBreak); break;
            case ID_FB_SP_M: adjI(() -> FarmHelperConfig.fastBreakSpeed, v -> FarmHelperConfig.fastBreakSpeed = v, -1, 1, 3); break;
            case ID_FB_SP_P: adjI(() -> FarmHelperConfig.fastBreakSpeed, v -> FarmHelperConfig.fastBreakSpeed = v, 1, 1, 3); break;
            case ID_FB_RAND: FarmHelperConfig.fastBreakRandomization = !FarmHelperConfig.fastBreakRandomization; button.displayString = on("FB Randomization", FarmHelperConfig.fastBreakRandomization); break;
            case ID_FB_CH_M: adjI(() -> FarmHelperConfig.fastBreakRandomizationChance, v -> FarmHelperConfig.fastBreakRandomizationChance = v, -1, 1, 100); break;
            case ID_FB_CH_P: adjI(() -> FarmHelperConfig.fastBreakRandomizationChance, v -> FarmHelperConfig.fastBreakRandomizationChance = v, 1, 1, 100); break;
            case ID_FB_JACOB: FarmHelperConfig.disableFastBreakDuringJacobsContest = !FarmHelperConfig.disableFastBreakDuringJacobsContest; button.displayString = on("Disable FB in Jacob", FarmHelperConfig.disableFastBreakDuringJacobsContest); break;
            case ID_MUTE: FarmHelperConfig.muteTheGame = !FarmHelperConfig.muteTheGame; button.displayString = on("Mute", FarmHelperConfig.muteTheGame); break;
            case ID_STREAMER: FarmHelperConfig.streamerMode = !FarmHelperConfig.streamerMode; button.displayString = on("Streamer", FarmHelperConfig.streamerMode); break;
            case ID_HUD_OUT: FarmHelperConfig.showStatusHudOutsideGarden = !FarmHelperConfig.showStatusHudOutsideGarden; button.displayString = on("HUD Out", FarmHelperConfig.showStatusHudOutsideGarden); break;
            case ID_RESET_STATS: FarmHelperConfig.resetStatsBetweenDisabling = !FarmHelperConfig.resetStatsBetweenDisabling; button.displayString = on("Reset Stats", FarmHelperConfig.resetStatsBetweenDisabling); break;
            case ID_DEBUG: FarmHelperConfig.debugMode = !FarmHelperConfig.debugMode; button.displayString = on("Debug", FarmHelperConfig.debugMode); break;
            case ID_DEBUG_FLY: FarmHelperConfig.debugNewFly = !FarmHelperConfig.debugNewFly; button.displayString = on("New Fly", FarmHelperConfig.debugNewFly); break;
            case ID_PROFIT_CULT: FarmHelperConfig.profitCalculatorCultivatingEnchant = !FarmHelperConfig.profitCalculatorCultivatingEnchant; button.displayString = on("Profit via Cultivating", FarmHelperConfig.profitCalculatorCultivatingEnchant); break;
            case ID_JACOB_CROPS: FarmHelperConfig.jacobContestCurrentCropsOnly = !FarmHelperConfig.jacobContestCurrentCropsOnly; button.displayString = on("Jacob Current Crops Only", FarmHelperConfig.jacobContestCurrentCropsOnly); break;
            case ID_PDOTT: FarmHelperConfig.showDebugLogsAboutPDOTT = !FarmHelperConfig.showDebugLogsAboutPDOTT; button.displayString = on("PD OTT Debug Logs", FarmHelperConfig.showDebugLogsAboutPDOTT); break;
            default:
                break;
        }
    }

    private void adjF(DoubleSupplier get, DoubleConsumer set, double delta, double min, double max) {
        set.accept(MathHelper.clamp_float((float) (get.getAsDouble() + delta), (float) min, (float) max));
        initGui();
    }

    private void adjI(IntSupplier get, IntConsumer set, int delta, int min, int max) {
        set.accept(MathHelper.clamp_int(get.getAsInt() + delta, min, max));
        initGui();
    }

    private void setLook(boolean pitch, boolean yaw) {
        if (mc.thePlayer == null) {
            LogUtils.sendError("Not in world.");
            return;
        }
        if (pitch) {
            FarmHelperConfig.customPitchLevel = clampPitch(mc.thePlayer.rotationPitch);
            FarmHelperConfig.customPitch = true;
        }
        if (yaw) {
            FarmHelperConfig.customYawLevel = normYaw(mc.thePlayer.rotationYaw);
            FarmHelperConfig.customYaw = true;
        }
        save();
        LogUtils.sendSuccess(String.format(Locale.US, "Pitch %.1f Yaw %.1f",
                FarmHelperConfig.customPitchLevel, FarmHelperConfig.customYawLevel));
    }

    private static float clampPitch(float v) { return MathHelper.clamp_float(v, -90f, 90f); }

    private static float normYaw(float yaw) {
        yaw %= 360f;
        if (yaw > 180f) yaw -= 360f;
        if (yaw < -180f) yaw += 360f;
        return yaw;
    }

    private void save() {
        if (FarmHelper.config != null) FarmHelper.config.save();
    }

    private void saveAndClose() {
        save();
        mc.displayGuiScreen(null);
        if (mc.currentScreen == null) mc.setIngameFocus();
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    private static String on(String name, boolean v) { return name + ": " + (v ? "ON" : "OFF"); }

    private static String macroLabel() {
        int i = FarmHelperConfig.macroType;
        if (i < 0 || i >= MACRO_LABELS.length) i = 0;
        return "Macro: " + MACRO_LABELS[i];
    }

    private static String soundLabel() {
        int i = FarmHelperConfig.failsafeMcSoundSelected;
        if (i < 0 || i >= SOUND_LABELS.length) i = 0;
        return SOUND_LABELS[i];
    }

    private static String fmt(float v) {
        if (Math.abs(v - Math.round(v)) < 0.001f) return String.valueOf(Math.round(v));
        return String.format(Locale.US, "%.1f", v);
    }
}
