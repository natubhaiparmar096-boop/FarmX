package com.jelly.farmhelperv2.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.*;
import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.page.CustomFailsafeMessagesPage;
import com.jelly.farmhelperv2.config.page.FailsafeNotificationsPage;
import com.jelly.farmhelperv2.config.struct.Rewarp;
import com.jelly.farmhelperv2.failsafe.Failsafe;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.failsafe.impl.BedrockCageFailsafe;
import com.jelly.farmhelperv2.failsafe.impl.DirtFailsafe;
import com.jelly.farmhelperv2.gui.FarmXMobileGui;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.GameStateHandler.BuffState;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.hud.DebugHUD;
import com.jelly.farmhelperv2.hud.StatusHUD;
import com.jelly.farmhelperv2.util.BlockUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlatformUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import com.jelly.farmhelperv2.util.helper.AudioManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// THIS IS RAT - CatalizCS
@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class FarmHelperConfig extends Config {

    private transient static final Minecraft mc = Minecraft.getMinecraft();
    private transient static final String GENERAL = "General";
    private transient static final String MISCELLANEOUS = "Miscellaneous";
    private transient static final String FAILSAFE = "Failsafe";
    private transient static final String DELAYS = "Delays";
    private transient static final String HUD = "HUD";
    private transient static final String DEBUG = "Debug";
    private transient static final String EXPERIMENTAL = "Experimental";

    private transient static final File configRewarpFile = new File("farmhelper_rewarp.json");


    public static List<Rewarp> rewarpList = new ArrayList<>();

    
    //<editor-fold desc="GENERAL">
    @Info(
            text = "DO NOT lock slot 7 in the hotbar if you're using any gui related features, such as inventory GUIs",
            category = GENERAL,
            type = InfoType.WARNING,
            size = 2
    )
    public static boolean guiInfo;

    @Dropdown(
            name = "Macro Type", category = GENERAL,
            description = "Farm Types",
            options = {
                    "S Shape / Vertical - Crops (Wheat, Carrot, Potato, NW)", // 0
                    "S Shape - Pumpkin/Melon", // 1
                    "S Shape - Pumpkin/Melon Melongkingde", // 2
                    "S Shape - Pumpkin/Melon Default Plot", // 3
                    "S Shape - Sugar Cane/Wild Roses/Sunflower", // 4
                    "S Shape - Cactus", // 5
                    "S Shape - Cactus SunTzu Black Cat", // 6
                    "S Shape - Cocoa Beans", // 7
                    "S Shape - Cocoa Beans (With Trapdoors)", // 8
                    "S Shape - Cocoa Beans (Left/Right)", // 9
                    "S Shape - Mushroom (45°)", // 10
                    "S Shape - Mushroom (30° with rotations)", // 11
                    "S Shape - Mushroom SDS", // 12
                    "Circle - Crops (Wheat, Carrot, Potato, NW)" // 13
            }, size = 2
    )
    public static int macroType = 0;

    @Switch(
            name = "Always hold W while farming", category = GENERAL,
            description = "Always hold W while farming",
            size = OptionSize.DUAL
    )
    public static boolean alwaysHoldW = false;

    //<editor-fold desc="Sugar Cane Controls">
    @Dropdown(
            name = "Sugar Cane Control Mode", category = GENERAL, subcategory = "Sugar Cane",
            description = "Classic: S along row, A/D lane. Strafe: Go/Return + lane key. Two-key: Hold Go (D) forward, hold Return (S) backward continuously during transit.",
            options = {
                    "Classic (S along row, A/D lane)",
                    "Strafe (3 keys: go / return / lane)",
                    "Two-key (Continuous Go / Return) — D+S style"
            },
            size = 2
    )
    public static int sugarcaneControlMode = 0;

    @Dropdown(
            name = "Classic: Along-Row Key", category = GENERAL, subcategory = "Sugar Cane",
            description = "Key held while moving along the cane row (classic mode).",
            options = {"W", "S", "A", "D"}
    )
    public static int sugarcaneClassicRowKey = 1; // S

    @Dropdown(
            name = "Classic: Lane-Left Key", category = GENERAL, subcategory = "Sugar Cane",
            description = "Key used to switch lane toward the left side (classic mode).",
            options = {"W", "S", "A", "D"}
    )
    public static int sugarcaneClassicLaneLeftKey = 2; // A

    @Dropdown(
            name = "Classic: Lane-Right Key", category = GENERAL, subcategory = "Sugar Cane",
            description = "Key used to switch lane toward the right side (classic mode).",
            options = {"W", "S", "A", "D"}
    )
    public static int sugarcaneClassicLaneRightKey = 3; // D

    @Dropdown(
            name = "Strafe/Two-key: Go Key", category = GENERAL, subcategory = "Sugar Cane",
            description = "First leg key. North-start D/S farm: set to D.",
            options = {"W", "S", "A", "D"}
    )
    public static int sugarcaneGoKey = 3; // D

    @Dropdown(
            name = "Strafe/Two-key: Return Key", category = GENERAL, subcategory = "Sugar Cane",
            description = "Return leg key. North-start D/S farm: set to S.",
            options = {"W", "S", "A", "D"}
    )
    public static int sugarcaneReturnKey = 1; // S

    @Dropdown(
            name = "Strafe: Lane-Switch Key", category = GENERAL, subcategory = "Sugar Cane",
            description = "Only for Strafe (3-key) mode. Two-key mode holds Go/Return continuously during transit.",
            options = {"W", "S", "A", "D"}
    )
    public static int sugarcaneLaneKey = 1; // S

    @Switch(
            name = "Start on Go Key Leg", category = GENERAL, subcategory = "Sugar Cane",
            description = "On: start holding Go (e.g. D from the north). Off: start on Return key."
    )
    public static boolean sugarcaneStartOnGoLeg = true;

    @Switch(
            name = "Invert Sugar Cane Lane Side", category = GENERAL, subcategory = "Sugar Cane",
            description = "Swaps left/right lane decisions in classic mode."
    )
    public static boolean sugarcaneInvertLaneSide = false;
    //</editor-fold>


    //<editor-fold desc="Rotation">
    @Switch(
            name = "Rotate After Warped", category = GENERAL, subcategory = "Rotation",
            description = "Rotates the player after re-warping", size = 1
    )
    public static boolean rotateAfterWarped = false;
    @Switch(
            name = "Rotate After Drop", category = GENERAL, subcategory = "Rotation",
            description = "Rotates after the player falls down", size = 1
    )
    public static boolean rotateAfterDrop = false;
    @Switch(
            name = "Don't fix micro rotations after warp", category = GENERAL, subcategory = "Rotation",
            description = "The macro doesn't do micro-rotations after rewarp if the current yaw and target yaw are the same", size = 2
    )
    public static boolean dontFixAfterWarping = false;
    @Switch(
            name = "Custom Pitch", category = GENERAL, subcategory = "Rotation",
            description = "Set pitch to custom level after starting the macro"
    )
    public static boolean customPitch = false;
    @Number(
            name = "Custom Pitch Level", category = GENERAL, subcategory = "Rotation",
            description = "Set custom pitch level after starting the macro",
            min = -90.0F, max = 90.0F
    )
    public static float customPitchLevel = 0;

    @Switch(
            name = "Custom Yaw", category = GENERAL, subcategory = "Rotation",
            description = "Set yaw to custom level after starting the macro"
    )
    public static boolean customYaw = false;

    @Number(
            name = "Custom Yaw Level", category = GENERAL, subcategory = "Rotation",
            description = "Set custom yaw level after starting the macro",
            min = -180.0F, max = 180.0F
    )
    public static float customYawLevel = 0;
    //</editor-fold>

    //<editor-fold desc="Rewarp">
    @Info(
            text = "Don't forget to add rewarp points!",
            type = InfoType.WARNING,
            category = GENERAL,
            subcategory = "Rewarp"
    )
    public static boolean rewarpWarning;

    @Button(
            name = "Add Rewarp", category = GENERAL, subcategory = "Rewarp",
            description = "Adds a rewarp position",
            text = "Add Rewarp"
    )
    Runnable _addRewarp = FarmHelperConfig::addRewarp;
    @Button(
            name = "Remove Rewarp", category = GENERAL, subcategory = "Rewarp",
            description = "Removes a rewarp position",
            text = "Remove Rewarp"
    )
    Runnable _removeRewarp = FarmHelperConfig::removeRewarp;
    @Button(
            name = "Remove All Rewarps", category = GENERAL, subcategory = "Rewarp",
            description = "Removes all rewarp positions",
            text = "Remove All Rewarps"
    )
    Runnable _removeAllRewarps = FarmHelperConfig::removeAllRewarps;
    //</editor-fold>

    //<editor-fold desc="Spawn">
    @Number(
            name = "SpawnPos X", category = GENERAL, subcategory = "Spawn Position",
            description = "The X coordinate of the spawn",
            min = -30000000, max = 30000000

    )
    public static int spawnPosX = 0;
    @Number(
            name = "SpawnPos Y", category = GENERAL, subcategory = "Spawn Position",
            description = "The Y coordinate of the spawn",
            min = -30000000, max = 30000000
    )
    public static int spawnPosY = 0;
    @Number(
            name = "SpawnPos Z", category = GENERAL, subcategory = "Spawn Position",
            description = "The Z coordinate of the spawn",
            min = -30000000, max = 30000000
    )
    public static int spawnPosZ = 0;

    @Number(
            name = "Yaw", category = GENERAL, subcategory = "Spawn Position",
            description = "The Yaw of the spawn",
            min = -180.0f, max = 180.0f
    )
    public static float spawnYaw = 0;

    @Number(
            name = "Pitch", category = GENERAL, subcategory = "Spawn Position",
            description = "The Pitch of the spawn",
            min = -90.0f, max = 90.0f
    )
    public static float spawnPitch = 0;

    @Number(
            name = "Spawn Plot", category = GENERAL, subcategory = "Spawn Position",
            description = "The Plot that the spawn is in",
            min = 0, max = 24
    )
    public static int spawnPlot = 0;

    @Button(
            name = "Set SpawnPos", category = GENERAL, subcategory = "Spawn Position",
            description = "Sets the spawn position to your current position",
            text = "Set SpawnPos"
    )
    Runnable _setSpawnPos = PlayerUtils::setSpawnLocation;
    @Button(
            name = "Reset SpawnPos", category = GENERAL, subcategory = "Spawn Position",
            description = "Resets the spawn position",
            text = "Reset SpawnPos"
    )
    Runnable _resetSpawnPos = () -> {
        spawnPosX = 0;
        spawnPosY = 0;
        spawnPosZ = 0;
        save();
        LogUtils.sendSuccess("Spawn position has been reset!");
    };

    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="MISC">
    //<editor-fold desc="Keybinds">
    @KeyBind(
            name = "Toggle Farm Helper", category = MISCELLANEOUS, subcategory = "Keybinds",
            description = "Toggles the macro on/off", size = 2
    )
    public static OneKeyBind toggleMacro = new OneKeyBind(Keyboard.KEY_GRAVE);
    @KeyBind(
            name = "Open GUI", category = MISCELLANEOUS, subcategory = "Keybinds",
            description = "Opens Farm Helper configuration menu", size = 2
    )

    public static OneKeyBind openGuiKeybind = new OneKeyBind(Keyboard.KEY_F);
    @KeyBind(
            name = "Cancel failsafe", category = MISCELLANEOUS, subcategory = "Keybinds",
            description = "Cancels failsafe and continues macroing", size = 2
    )
    public static OneKeyBind cancelFailsafeKeybind = new OneKeyBind(Keyboard.KEY_NONE);

    //</editor-fold>

    //<editor-fold desc="Miscellaneous">
    @Switch(
            name = "Mute The Game", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Mutes the game while farming"
    )
    public static boolean muteTheGame = false;

    @Switch(
            name = "Change window's title", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Changes the window's title"
    )
    public static boolean changeWindowTitle = true;

    @Switch(
            name = "Hold left click when changing row", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Hold left click when change row"
    )
    public static boolean holdLeftClickWhenChangingRow = true;

    @Switch(
            name = "Anti Stuck Enabled (Disabled by default for now)", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Enables the anti stuck feature"
    )
    public static boolean tmpAntiStuckEnabled = false;
    @Slider(
            name = "Anti Stuck Tries Until Rewarp", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "The number of tries until rewarp",
            min = 3, max = 10
    )
    public static int antiStuckTriesUntilRewarp = 5;
    //</editor-fold>

    
    //<editor-fold desc="Crop Utils">
    @Switch(
            name = "Increase Cocoa Hitboxes", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm cocoa beans more efficiently at higher speeds by making the hitboxes bigger"
    )
    public static boolean increasedCocoaBeans = true;

    @Switch(
            name = "Increase Crop Hitboxes", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm crops more efficient by making the hitboxes bigger"
    )
    public static boolean increasedCrops = true;

    @Switch(
            name = "Increase Nether Wart Hitboxes", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm nether warts more efficiently at higher speeds by making the hitboxes bigger"
    )
    public static boolean increasedNetherWarts = true;

    @Switch(
            name = "Increase Mushroom Hitboxes", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm mushrooms more efficiently at higher speeds by making the hitboxes bigger"
    )
    public static boolean increasedMushrooms = true;

    @Switch(
            name = "Pingless Cactus", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm cactus more efficiently at higher speeds by making the cactus pingless"
    )
    public static boolean pinglessCactus = true;
    //</editor-fold>


    //</editor-fold>

    //<editor-fold desc="FAILSAFES">

    // General Settings
    @Switch(name = "Pop-up Notifications", category = FAILSAFE, subcategory = "General",
            description = "Enable on-screen failsafe notifications")
    public static boolean popUpNotifications = true;

    @DualOption(
            name = "Failsafe Action",
            category = FAILSAFE, subcategory = "General",
            description = "Decides what the macro should do upon macro check.",
            left = "React", right = "Disable"
    )
    public static boolean failsafeAction = false;

    @Slider(name = "Failsafe Stop Delay", category = FAILSAFE, subcategory = "General",
            description = "Delay before stopping macro after failsafe (ms)",
            min = 1000, max = 7500)
    public static int failsafeStopDelay = 2000;

    // Automatic Actions
    @Switch(name = "Auto Warp on World Change", category = FAILSAFE, subcategory = "Auto Actions",
            description = "Warp to garden after server reboot or update, disconnects if disabled")
    public static boolean autoWarpOnWorldChange = true;

    // Detection Sensitivity
    @Slider(name = "Teleport Lag Tolerance", category = FAILSAFE, subcategory = "Detection",
            description = "Variation in distance between expected and actual positions when lagging",
            min = 0, max = 2)
    public static float teleportLagTolerance = 0.5f;

    @Slider(name = "Detection Time Window", category = FAILSAFE, subcategory = "Detection",
            description = "Time frame for teleport/rotation checks (ms)",
            min = 50, max = 4000, step = 50)
    public static int detectionTimeWindow = 500;

    @Slider(name = "Pitch Sensitivity", category = FAILSAFE, subcategory = "Detection",
            description = "Pitch change sensitivity (lower = stricter)",
            min = 1, max = 30)
    public static float pitchSensitivity = 7;

    @Slider(name = "Yaw Sensitivity", category = FAILSAFE, subcategory = "Detection",
            description = "Yaw change sensitivity (lower = stricter)",
            min = 1, max = 30)
    public static float yawSensitivity = 5;

    @Slider(name = "Teleport Distance Threshold", category = FAILSAFE, subcategory = "Detection",
            description = "Minimum teleport distance to trigger failsafe (blocks)",
            min = 0.5f, max = 20f)
    public static float teleportDistanceThreshold = 4;

    @Slider(name = "Vertical Knockback Threshold", category = FAILSAFE, subcategory = "Detection",
            description = "Minimum vertical knockback to trigger failsafe",
            min = 2000, max = 10000, step = 1000)
    public static float verticalKnockbackThreshold = 4000;

    // BPS Check
    @Switch(name = "Enable BPS Check", category = FAILSAFE, subcategory = "BPS",
            description = "Monitor for drops in blocks per second")
    public static boolean enableBpsCheck = true;

    @Slider(name = "Minimum BPS", category = FAILSAFE, subcategory = "BPS",
            description = "Trigger failsafe if BPS falls below this value",
            min = 5, max = 15)
    public static float minBpsThreshold = 10f;

    // Failsafe Testing
    @Button(name = "Test Failsafe", category = FAILSAFE, subcategory = "Testing",
            description = "Simulate a failsafe trigger",
            text = "Run Test")
    Runnable _testFailsafe = () -> {
        if (!MacroHandler.getInstance().isMacroToggled()) {
            LogUtils.sendError("You need to start the macro first!");
            return;
        }
        LogUtils.sendWarning("Testing failsafe...");
        PlayerUtils.closeScreen();
        Failsafe testingFailsafe = FailsafeManager.getInstance().failsafes.get(testFailsafeType);
        if (testingFailsafe.equals(DirtFailsafe.getInstance()) || testingFailsafe.equals(BedrockCageFailsafe.getInstance())) {
            LogUtils.sendError("You can't test this failsafe because it requires specific conditions to trigger!");
            return;
        }
        FailsafeManager.getInstance().possibleDetection(testingFailsafe);
    };

    @Dropdown(name = "Test Failsafe Type", category = FAILSAFE, subcategory = "Testing",
            description = "Select failsafe scenario to test",
            options = {
                    "Bad Effects",
                    "Bedrock Cage",
                    "Cobweb",
                    "Dirt",
                    "Disconnect",
                    "Full Inventory",
                    "Item Change",
                    "Knockback",
                    "Lower Average BPS",
                    "Rotation",
                    "Teleport",
                    "World Change"
            })
    public static int testFailsafeType = 0;

    //</editor-fold>

    
    //<editor-fold desc="Failsafes conf page">
    @Page(
            name = "Failsafe Notifications", category = FAILSAFE, subcategory = "Failsafe Notifications", location = PageLocation.BOTTOM,
            description = "Click here to customize failsafe notifications"
    )
    public FailsafeNotificationsPage failsafeNotificationsPage = new FailsafeNotificationsPage();
    //</editor-fold>

    //<editor-fold desc="Desync">
    @Switch(
            name = "Check Desync", category = FAILSAFE, subcategory = "Desync",
            description = "If client desynchronization is detected, it activates a failsafe. Turn this off if the network is weak or if it happens frequently."
    )
    public static boolean checkDesync = true;
    @Slider(
            name = "Pause for X milliseconds after desync triggered", category = FAILSAFE, subcategory = "Desync",
            description = "The delay to pause after desync triggered (in milliseconds)",
            min = 3_000, max = 10_000
    )
    public static int desyncPauseDelay = 5_000;

    @Switch(
            name = "Full Inventory Failsafe", category = FAILSAFE, subcategory = "Inventory",
            description = "Triggers a failsafe when your inventory is full. Disable on servers where inventory fills often and is not a staff check."
    )
    public static boolean enableFullInventoryFailsafe = true;
    //</editor-fold>

    //<editor-fold desc="Failsafe Trigger Sound">
    @Switch(
            name = "Enable Failsafe Trigger Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound", size = OptionSize.DUAL,
            description = "Makes a sound when a failsafe has been triggered"
    )
    public static boolean enableFailsafeSound = true;
    @Dropdown(
            name = "Minecraft Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "The Minecraft sound to play when a failsafe has been triggered",
            options = {
                    "Ping",
                    "Anvil"
            }
    )
    public static int failsafeMcSoundSelected = 1;
    @Switch(
            name = "Max out Master category sounds while pinging", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "Maxes out the sounds while failsafe"
    )
    public static boolean maxOutMinecraftSounds = false;

    @Button(
            name = "", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "Plays the selected sound",
            text = "Play"
    )
    Runnable _playFailsafeSoundButton = () -> AudioManager.getInstance().playSound();
    @Button(
            name = "", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "Stops playing the selected sound",
            text = "Stop"
    )
    Runnable _stopFailsafeSoundButton = () -> AudioManager.getInstance().resetSound();

    //</editor-fold>

    //<editor-fold desc="Restart after failsafe">
    @Switch(
            name = "Enable Restart After FailSafe", category = FAILSAFE, subcategory = "Restart After FailSafe",
            description = "Restarts the macro after a while when a failsafe has been triggered"
    )
    public static boolean enableRestartAfterFailSafe = true;
    @Slider(
            name = "Restart Delay", category = FAILSAFE, subcategory = "Restart After FailSafe",
            description = "The delay to restart after failsafe (in minutes)",
            min = 0, max = 20
    )
    public static int restartAfterFailSafeDelay = 0;
    @Info(
            text = "Setting this value to 0 will start the macro a few seconds later, after the failsafe is finished",
            category = FAILSAFE, subcategory = "Restart After FailSafe",
            type = InfoType.INFO, size = 2
    )
    public static boolean restartAfterFailSafeInfo;

    @Switch(
            name = "Always teleport to /warp garden after the failsafe",
            category = FAILSAFE, subcategory = "Restart After FailSafe",
            description = "Always teleports to /warp garden after the failsafe"
    )
    public static boolean alwaysTeleportToGarden = false;

    //</editor-fold>



    //<editor-fold desc="Failsafe Messages">
    @Switch(
            name = "Send Chat Message During Failsafe", category = FAILSAFE, subcategory = "Failsafe Messages",
            description = "Sends a chat message when a failsafe has been triggered"
    )
    public static boolean sendFailsafeMessage = false;
    @Page(
            name = "Custom Failsafe Messages", category = FAILSAFE, subcategory = "Failsafe Messages", location = PageLocation.BOTTOM,
            description = "Click here to edit custom failsafe messages"
    )
    public static CustomFailsafeMessagesPage customFailsafeMessagesPage = new CustomFailsafeMessagesPage();
    //</editor-fold>
    //</editor-fold>

    
    
    
    
    //<editor-fold desc="DELAYS">
    //<editor-fold desc="Changing Rows">
    @Slider(
            name = "Time between changing rows", category = DELAYS, subcategory = "Changing rows",
            description = "The minimum time to wait before changing rows (in milliseconds)",
            min = 0, max = 2000
    )
    public static float timeBetweenChangingRows = 400f;
    @Slider(
            name = "Additional random time between changing rows", category = DELAYS, subcategory = "Changing rows",
            description = "The maximum time to wait before changing rows (in milliseconds)",
            min = 0, max = 2000
    )
    public static float randomTimeBetweenChangingRows = 200f;
    @Switch(
            name = "Custom row change delays during Jacob's Contest", category = DELAYS, subcategory = "Changing rows",
            description = "Custom row change delays during Jacob's Contest"
    )
    public static boolean customRowChangeDelaysDuringJacob = false;
    @Slider(
            name = "Time between changing rows during Jacob's Contest", category = DELAYS, subcategory = "Changing rows",
            description = "The minimum time to wait before changing rows (in milliseconds)",
            min = 0, max = 2000
    )
    public static float timeBetweenChangingRowsDuringJacob = 400f;
    @Slider(
            name = "Additional random time between changing rows during Jacob's Contest", category = DELAYS, subcategory = "Changing rows",
            description = "The maximum time to wait before changing rows (in milliseconds)",
            min = 0, max = 2000
    )
    public static float randomTimeBetweenChangingRowsDuringJacob = 200f;
    //</editor-fold>

    //<editor-fold desc="Rotation Time">
    @Slider(
            name = "Rotation Time", category = DELAYS, subcategory = "Rotations",
            description = "The time it takes to rotate the player",
            min = 200f, max = 2000f
    )
    public static float rotationTime = 500f;
    @Slider(
            name = "Additional random Rotation Time", category = DELAYS, subcategory = "Rotations",
            description = "The maximum random time added to the delay time it takes to rotate the player (in milliseconds)",
            min = 0f, max = 2000f
    )
    public static float rotationTimeRandomness = 300;
    @Switch(
            name = "Custom rotation delays during Jacob's Contest", category = DELAYS, subcategory = "Rotations",
            description = "Custom rotation delays during Jacob's Contest"
    )
    public static boolean customRotationDelaysDuringJacob = false;
    @Slider(
            name = "Rotation Time during Jacob's Contest", category = DELAYS, subcategory = "Rotations",
            description = "The time it takes to rotate the player",
            min = 200f, max = 2000f
    )
    public static float rotationTimeDuringJacob = 500f;
    @Slider(
            name = "Additional random Rotation Time during Jacob's Contest", category = DELAYS, subcategory = "Rotations",
            description = "The maximum random time added to the delay time it takes to rotate the player (in milliseconds)",
            min = 0f, max = 2000f
    )
    public static float rotationTimeRandomnessDuringJacob = 300;
    //</editor-fold>

    //<editor-fold desc="Fly Pathexecutioner Rotation Time">
    @Slider(
            name = "Fly PathExecutioner Rotation Time", category = DELAYS, subcategory = "Fly PathExecutioner",
            description = "The time it takes to rotate the player",
            min = 200f, max = 2000f
    )
    public static float flyPathExecutionerRotationTime = 500f;
    @Slider(
            name = "Fly PathExecutioner Additional random Rotation Time", category = DELAYS, subcategory = "Fly PathExecutioner",
            description = "The maximum random time added to the delay time it takes to rotate the player (in milliseconds)",
            min = 0f, max = 2000f
    )
    public static float flyPathExecutionerRotationTimeRandomness = 300;
    //</editor-fold>


    //<editor-fold desc="Gui Delay">
    @Slider(
            name = "GUI Delay", category = DELAYS, subcategory = "GUI Delays",
            description = "The delay between clicking during GUI macros (in milliseconds)",
            min = 50f, max = 2000f
    )
    public static float macroGuiDelay = 400f;
    @Slider(
            name = "Additional random GUI Delay", category = DELAYS, subcategory = "GUI Delays",
            description = "The maximum random time added to the delay time between clicking during GUI macros (in milliseconds)",
            min = 0f, max = 2000f
    )
    public static float macroGuiDelayRandomness = 350f;
    //</editor-fold>

    
    //<editor-fold desc="Rewarp Time">
    @Slider(
            name = "Rewarp Delay", category = DELAYS, subcategory = "Rewarp",
            description = "The delay between rewarping (in milliseconds)",
            min = 250f, max = 2000f
    )
    public static float rewarpDelay = 400f;
    @Slider(
            name = "Additional random Rewarp Delay", category = DELAYS, subcategory = "Rewarp",
            description = "The maximum random time added to the delay time between rewarping (in milliseconds)",
            min = 0f, max = 2000f
    )
    public static float rewarpDelayRandomness = 350f;
    //</editor-fold>

    //<editor-fold desc="HUD">
    @Switch(
            name = "Streamer mode", category = HUD, subcategory = "Streamer mode",
            description = "Hides everything Farm Helper related from the screen."
    )
    public static boolean streamerMode = false;
    @Info(
            text = "Streamer mode does NOT disable failsafe notifications or sounds! It only hides visual elements.",
            type = InfoType.WARNING,
            category = HUD,
            subcategory = "Streamer mode",
            size = 2
    )
    public static boolean streamerModeInfo;
    @Info(
            text = "You must restart the game if you want to hide the window title after enabling the streamer mode.",
            type = InfoType.WARNING,
            category = HUD,
            subcategory = "Streamer mode"
    )
    public static boolean streamerModeInfo2;
    @HUD(
            name = "Status HUD - Visual Settings", category = HUD, subcategory = "Status"
    )
    public static StatusHUD statusHUD = new StatusHUD();
    @Switch(
            name = "Show Status HUD outside the garden", category = HUD, subcategory = "Status"
    )
    public static boolean showStatusHudOutsideGarden = false;

    @Switch(
            name = "Reset stats between disabling", category = HUD, subcategory = "Status",
            description = "Resets the runtime timer when re-enabling the macro"
    )
    public static boolean resetStatsBetweenDisabling = false;

    //</editor-fold>

    //<editor-fold desc="DEBUG">
    //<editor-fold desc="Debug">

    @KeyBind(
            name = "Debug Keybind", category = DEBUG, subcategory = "Debug"
    )
    public static OneKeyBind debugKeybind = new OneKeyBind(Keyboard.KEY_NONE);
//    @KeyBind(
//            name = "Debug Keybind 2", category = DEBUG
//    )
//    public static OneKeyBind debugKeybind2 = new OneKeyBind(Keyboard.KEY_H);
//    @KeyBind(
//            name = "Debug Keybind 3", category = DEBUG
//    )
//    public static OneKeyBind debugKeybind3 = new OneKeyBind(Keyboard.KEY_J);

    @Switch(
            name = "Debug Mode", category = DEBUG, subcategory = "Debug",
            description = "Prints to chat what the bot is currently executing. Useful if you are having issues."
    )
    public static boolean debugMode = false;

    @Switch(
        name = "New Fly", category = DEBUG
    )
    public static boolean debugNewFly = true;

    //</editor-fold>

    //<editor-fold desc="Debug Hud">
    @HUD(
            name = "Debug HUD", category = DEBUG, subcategory = " "
    )
    public static DebugHUD debugHUD = new DebugHUD();
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="EXPERIMENTAL">
    //<editor-fold desc="Fastbreak">
    @Switch(
            name = "Enable Fast Break (DANGEROUS)", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Fast Break is very risky and using it will most likely result in a ban. Proceed with caution."
    )
    public static boolean fastBreak = false;

    @Switch(
            name = "Enable Fast Break Randomization", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Randomizes the Fast Break chance"
    )
    public static boolean fastBreakRandomization = false;

    @Slider(
            name = "Fast Break Randomization Chance", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "The chance to break the block",
            min = 1, max = 100, step = 1
    )
    public static int fastBreakRandomizationChance = 5;

    @Info(
            text = "Fast Break will most likely ban you. Use at your own risk.",
            type = InfoType.ERROR,
            category = EXPERIMENTAL,
            subcategory = "Fast Break"
    )
    public static boolean fastBreakWarning;
    @Slider(
            name = "Fast Break Speed", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Fast Break speed",
            min = 1, max = 3
    )
    public static int fastBreakSpeed = 1;
    @Switch(
            name = "Disable Fast Break during Jacob's contest", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Disables Fast Break during Jacob's contest"
    )
    public static boolean disableFastBreakDuringJacobsContest = true;
    //</editor-fold>

    @Switch(
            name = "Automatically switch recognized crop", category = EXPERIMENTAL, subcategory = "Auto Switch",
            description = "Macro will be recognizing farming crop, which will lead to auto switching tool to the best one"
    )
    public static boolean autoSwitchTool = true;

    @Switch(
            name = "Count profit based on Cultivating enchant", category = EXPERIMENTAL, subcategory = "Profit Calculator",
            description = "Counts profit based on Cultivating enchant"
    )
    public static boolean profitCalculatorCultivatingEnchant = true;

    @Switch(
            name = "Count only current crops for Jacob's Contest excludes", category = EXPERIMENTAL, subcategory = "Jacob's Contest",
            description = "Counts only current crops for Jacob's Contest excludes"
    )
    public static boolean jacobContestCurrentCropsOnly = true;

    @Switch(
            name = "Show Jacob's Contest HUD", category = EXPERIMENTAL, subcategory = "Jacob's Contest",
            description = "Shows live upcoming and active Jacob's Contest status, crop, time left, and harvested count on screen"
    )
    public static boolean showJacobsContestHud = true;

    @Switch(
            name = "Show Debug logs about PD OTT", category = EXPERIMENTAL, subcategory = "Debug",
            description = "Shows debug logs about PD OTT"
    )
    public static boolean showDebugLogsAboutPDOTT = false;

    //</editor-fold>

    @Number(name = "Config Version", category = EXPERIMENTAL, subcategory = "Experimental", min = 0, max = 1337)
    public static int configVersion = 6;
    @Switch(
            name = "Shown Welcome GUI", category = EXPERIMENTAL, subcategory = "Experimental"
    )
    public static boolean shownWelcomeGUI2 = false;

    public FarmHelperConfig() {
        super(new Mod("Farm Helper", ModType.HYPIXEL, "/farmhelper/icon-mod/icon.png"), "/farmhelper/config.json");
        initialize();

        this.addDependency("macroType", "Macro Type", () -> !MacroHandler.getInstance().isMacroToggled());

        this.hideIf("sugarcaneControlMode", () -> getMacro() != MacroEnum.S_SUGAR_CANE);
        this.hideIf("sugarcaneClassicRowKey", () -> getMacro() != MacroEnum.S_SUGAR_CANE || sugarcaneControlMode != 0);
        this.hideIf("sugarcaneClassicLaneLeftKey", () -> getMacro() != MacroEnum.S_SUGAR_CANE || sugarcaneControlMode != 0);
        this.hideIf("sugarcaneClassicLaneRightKey", () -> getMacro() != MacroEnum.S_SUGAR_CANE || sugarcaneControlMode != 0);
        this.hideIf("sugarcaneGoKey", () -> getMacro() != MacroEnum.S_SUGAR_CANE || sugarcaneControlMode == 0);
        this.hideIf("sugarcaneReturnKey", () -> getMacro() != MacroEnum.S_SUGAR_CANE || sugarcaneControlMode == 0);
        this.hideIf("sugarcaneLaneKey", () -> getMacro() != MacroEnum.S_SUGAR_CANE || sugarcaneControlMode != 1);
        this.hideIf("sugarcaneStartOnGoLeg", () -> getMacro() != MacroEnum.S_SUGAR_CANE || sugarcaneControlMode == 0);
        this.hideIf("sugarcaneInvertLaneSide", () -> getMacro() != MacroEnum.S_SUGAR_CANE || sugarcaneControlMode != 0);

        this.addDependency("customPitchLevel", "customPitch");
        this.addDependency("customYawLevel", "customYaw");

        this.addDependency("desyncPauseDelay", "checkDesync");
        this.addDependency("failsafeMcSoundSelected", "enableFailsafeSound");
        this.addDependency("maxOutMinecraftSounds", "enableFailsafeSound");
        this.addDependency("_playFailsafeSoundButton", "enableFailsafeSound");
        this.addDependency("_stopFailsafeSoundButton", "enableFailsafeSound");
        this.hideIf("_playFailsafeSoundButton", () -> AudioManager.getInstance().isSoundPlaying());
        this.hideIf("_stopFailsafeSoundButton", () -> !AudioManager.getInstance().isSoundPlaying());
        this.addDependency("restartAfterFailSafeDelay", "enableRestartAfterFailSafe");
        this.addDependency("alwaysTeleportToGarden", "enableRestartAfterFailSafe");






        this.hideIf("infoCookieBuffRequired",
                () -> GameStateHandler.getInstance().inGarden() || GameStateHandler.getInstance().getCookieBuffState() == BuffState.NOT_ACTIVE);



        this.addDependency("debugMode", "Streamer Mode", () -> !streamerMode);
        this.addDependency("streamerMode", "Debug Mode", () -> !debugMode);
        this.addDependency("streamerModeInfo", "debugMode");
        this.addDependency("streamerModeInfo2", "debugMode");

        this.addDependency("fastBreakSpeed", "fastBreak");
        this.addDependency("fastBreakRandomization", "fastBreak");
        this.addDependency("fastBreakRandomizationChance", "fastBreak");
        this.addDependency("disableFastBreakDuringJacobsContest", "fastBreak");




        this.addDependency("antiStuckTriesUntilRewarp", "tmpAntiStuckEnabled");




        this.addDependency("averageBPSDrop", "enableBpsCheck");



        this.addDependency("timeBetweenChangingRowsDuringJacob", "customRowChangeDelaysDuringJacob");
        this.addDependency("randomTimeBetweenChangingRowsDuringJacob", "customRowChangeDelaysDuringJacob");
        this.addDependency("rotationTimeDuringJacob", "customRotationDelaysDuringJacob");
        this.addDependency("rotationTimeRandomnessDuringJacob", "customRotationDelaysDuringJacob");





        this.hideIf("shownWelcomeGUI", () -> true);

        this.hideIf("configVersion", () -> true);

        if (PlatformUtils.isMobile()) {
            // OneConfig NanoVG HUDs/GUI crash under GL4ES; use vanilla FarmXMobileGui instead.
            registerKeyBind(openGuiKeybind, () -> Minecraft.getMinecraft().displayGuiScreen(new FarmXMobileGui()));
        } else {
            registerKeyBind(openGuiKeybind, this::openGui);
        }
        registerKeyBind(toggleMacro, () -> MacroHandler.getInstance().toggleMacro());
        registerKeyBind(debugKeybind, () -> {
        });
        registerKeyBind(cancelFailsafeKeybind, () -> {
            if (FailsafeManager.getInstance().getChooseEmergencyDelay().isScheduled()) {
                FailsafeManager.getInstance().stopFailsafes();
                LogUtils.sendWarning("[Failsafe] Emergency has been cancelled!");
            }
        });
//        registerKeyBind(debugKeybind2, () -> {
//            MovingObjectPosition objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
//            if (objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
//                BlockPos blockPos = objectMouseOver.getBlockPos();
//                BlockPos oppositeSide = blockPos.offset(objectMouseOver.sideHit);
//                LogUtils.sendDebug("Block: " + oppositeSide);
//                FlyPathfinder.getInstance().setGoal(new GoalBlock(oppositeSide));
//            }
//        });
//        registerKeyBind(debugKeybind3, () -> {
//                    FlyPathfinder.getInstance().getPathTo(FlyPathfinder.getInstance().getGoal());
//                });
        save();
    }

    public static void addRewarp() {
        if (FarmHelperConfig.rewarpList.stream().anyMatch(rewarp -> rewarp.isTheSameAs(BlockUtils.getRelativeBlockPos(0, 0, 0)))) {
            LogUtils.sendError("Rewarp location has already been set!");
            return;
        }
        Rewarp newRewarp = new Rewarp(BlockUtils.getRelativeBlockPos(0, 0, 0));
        if (newRewarp.getDistance(new BlockPos(PlayerUtils.getSpawnLocation())) < 2) {
            LogUtils.sendError("Rewarp location is too close to the spawn location! You must put it AT THE END OF THE FARM!");
            return;
        }
        rewarpList.add(newRewarp);
        LogUtils.sendSuccess("Added rewarp: " + newRewarp);
        saveRewarpConfig();
    }

    public static void removeRewarp() {
        Rewarp closest = null;
        if (rewarpList.isEmpty()) {
            LogUtils.sendError("No rewarp locations set!");
            return;
        }
        double closestDistance = Double.MAX_VALUE;
        for (Rewarp rewarp : rewarpList) {
            double distance = rewarp.getDistance(BlockUtils.getRelativeBlockPos(0, 0, 0));
            if (distance < closestDistance) {
                closest = rewarp;
                closestDistance = distance;
            }
        }
        if (closest != null) {
            rewarpList.remove(closest);
            LogUtils.sendSuccess("Removed the closest rewarp: " + closest);
            saveRewarpConfig();
        }
    }

    public static void removeAllRewarps() {
        rewarpList.clear();
        LogUtils.sendSuccess("Removed all saved rewarp positions");
        saveRewarpConfig();
    }

    public static void saveRewarpConfig() {
        try {
            if (!configRewarpFile.exists()) {
                Files.createFile(configRewarpFile.toPath());
            }

            Files.write(configRewarpFile.toPath(), FarmHelper.gson.toJson(rewarpList).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MacroEnum getMacro() {
        return MacroEnum.values()[macroType];
    }

    public static long getRandomTimeBetweenChangingRows() {
        if (customRowChangeDelaysDuringJacob && GameStateHandler.getInstance().inJacobContest()) {
            return (long) (timeBetweenChangingRowsDuringJacob + (float) Math.random() * randomTimeBetweenChangingRowsDuringJacob);
        }
        return (long) (timeBetweenChangingRows + (float) Math.random() * randomTimeBetweenChangingRows);
    }

    public static long getMaxTimeBetweenChangingRows() {
        return (long) (timeBetweenChangingRows + randomTimeBetweenChangingRows);
    }

    public static long getRandomRotationTime() {
        if (customRotationDelaysDuringJacob && GameStateHandler.getInstance().inJacobContest()) {
            return (long) (rotationTimeDuringJacob + (float) Math.random() * rotationTimeRandomnessDuringJacob);
        }
        return (long) (rotationTime + (float) Math.random() * rotationTimeRandomness);
    }

    public static long getRandomFlyPathExecutionerRotationTime() {
        return (long) (flyPathExecutionerRotationTime + (float) Math.random() * flyPathExecutionerRotationTimeRandomness);
    }

    public static long getRandomGUIMacroDelay() {
        return (long) (macroGuiDelay + (float) Math.random() * macroGuiDelayRandomness);
    }


    public static long getRandomRewarpDelay() {
        return (long) (rewarpDelay + (float) Math.random() * rewarpDelayRandomness);
    }

    public String getJson() {
        String json = gson.toJson(this);
        if (json == null || json.equals("{}")) {
            json = nonProfileSpecificGson.toJson(this);
        }
        return json;
    }

    public enum MacroEnum {
        S_V_NORMAL_TYPE,
        S_PUMPKIN_MELON,
        S_PUMPKIN_MELON_MELONGKINGDE,
        S_PUMPKIN_MELON_DEFAULT_PLOT,
        S_SUGAR_CANE,
        S_CACTUS,
        S_CACTUS_SUNTZU,
        S_COCOA_BEANS,
        S_COCOA_BEANS_TRAPDOORS,
        S_COCOA_BEANS_LEFT_RIGHT,
        S_MUSHROOM,
        S_MUSHROOM_ROTATE,
        S_MUSHROOM_SDS,
        C_NORMAL_TYPE
    }

    @Getter
    public enum CropEnum {
        NONE("None"),
        CARROT("Carrot"),
        NETHER_WART("Nether Wart"),
        POTATO("Potato"),
        WHEAT("Wheat"),
        SUGAR_CANE("Sugar Cane"),
        MELON("Melon"),
        PUMPKIN("Pumpkin"),
        PUMPKIN_MELON_UNKNOWN("Pumpkin/Melon"),
        CACTUS("Cactus"),
        COCOA_BEANS("Cocoa Beans"),
        MUSHROOM("Mushroom"),
        MUSHROOM_ROTATE("Mushroom"),
        SUNFLOWER("Sunflower"),
        MOONFLOWER("Moonflower"),
        ROSE("Rose"),
        ;

        final String localizedName;

        CropEnum(String localizedName) {
            this.localizedName = localizedName;
        }
    }
}
