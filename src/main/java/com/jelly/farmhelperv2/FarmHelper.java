package com.jelly.farmhelperv2;

import baritone.api.BaritoneAPI;
import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jelly.farmhelperv2.command.FarmHelperMainCommand;
import com.jelly.farmhelperv2.command.RewarpCommand;
import com.jelly.farmhelperv2.command.SpawnCommand;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.event.MillisecondEvent;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.feature.FeatureManager;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.handler.RotationHandler;
import com.jelly.farmhelperv2.hud.MobileStatusOverlay;
import com.jelly.farmhelperv2.pathfinder.FlyPathFinderExecutor;
import com.jelly.farmhelperv2.util.*;
import com.jelly.farmhelperv2.util.helper.AudioManager;
import com.jelly.farmhelperv2.util.helper.BaritoneEventListener;
import com.jelly.farmhelperv2.util.helper.TickTask;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod(modid = "farmhelperv2", useMetadata = true)
public class FarmHelper {
    public static final String VERSION = "%%VERSION%%";
    public static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    public static FarmHelperConfig config;
    public static boolean sentInfoAboutShittyClient = false;
    public static boolean isDebug = false;
    private final Minecraft mc = Minecraft.getMinecraft();
    public static File jarFile = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        jarFile = event.getSourceFile();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        initializeFields();
        initializeListeners();
        initializeCommands();
        FeatureManager.getInstance().fillFeatures().forEach(MinecraftForge.EVENT_BUS::register);

        mc.gameSettings.pauseOnLostFocus = false;
        mc.gameSettings.gammaSetting = 1000;
        isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");
        if (!PlatformUtils.isMobile() && !FarmHelperConfig.streamerMode && FarmHelperConfig.changeWindowTitle) {
            try {
                Display.setTitle("Farm Helper 〔v" + VERSION + "〕 " + (!isDebug ? "Bing Chilling" : "wazzup dev?") + " ☛ " + Minecraft.getMinecraft().getSession().getUsername());
            } catch (Throwable ignored) {
            }
        }
        FailsafeUtils.getInstance();
        PlotUtils.init();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        long msPeriod = PlatformUtils.isMobile() ? 50L : 1L;
        executorService.scheduleAtFixedRate(() -> MinecraftForge.EVENT_BUS.post(new MillisecondEvent()), 0, msPeriod, TimeUnit.MILLISECONDS);
        BaritoneAPI.getProvider().getPrimaryBaritone().getGameEventHandler().registerEventListener(new BaritoneEventListener());
    }

    @SubscribeEvent
    public void onTickSendInfoAboutShittyClient(TickEvent.PlayerTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (sentInfoAboutShittyClient) return;

        if (ReflectionUtils.hasPackageInstalled("feather")) {
            LogUtils.sendNotification("Farm Helper", "You've got Feather Client installed! Be aware, you might have a lot of bugs because of this shitty client!", 15000);
            LogUtils.sendError("You've got §6§lFeather Client §cinstalled! Be aware, you might have a lot of bugs because of this shitty client!");
        }
        if (ReflectionUtils.hasPackageInstalled("cc.woverflow.hytils.HytilsReborn")) {
            LogUtils.sendNotification("Farm Helper", "You've got Hytils installed in your mods folder! This will cause many issues with rewarping as it sends tons of commands every minute.", 15000);
            LogUtils.sendError("You've got §6§lHytils §cinstalled in your mods folder! This will cause many issues with rewarping as it sends tons of commands every minute.");
        }
        if (ReflectionUtils.hasPackageInstalled("com.tterrag.blur")) {
            LogUtils.sendNotification("Farm Helper", "You've got BlurMC installed in your mods folder! This can break features that need to work with inventories!", 15000);
            LogUtils.sendError("You've got §6§lBlurMC §cinstalled in your mods folder! This can break features that need to work with inventories!");
        }
        if (FarmHelperConfig.configVersion == 3 && FarmHelperConfig.macroType > 7) {
            FarmHelperConfig.macroType += 1; // Added cocoa bean macro with trapdoors
        }
        if (FarmHelperConfig.configVersion != 6)
            FarmHelperConfig.configVersion = 6;
        sentInfoAboutShittyClient = true;
    }

    private void initializeListeners() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(FailsafeManager.getInstance());
        MinecraftForge.EVENT_BUS.register(GameStateHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(MacroHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(TickTask.getInstance());
        MinecraftForge.EVENT_BUS.register(AudioManager.getInstance());
        MinecraftForge.EVENT_BUS.register(RotationHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(FlyPathFinderExecutor.getInstance());
        MinecraftForge.EVENT_BUS.register(new TablistUtils());
        MinecraftForge.EVENT_BUS.register(new ScoreboardUtils());
        MinecraftForge.EVENT_BUS.register(MobileStatusOverlay.getInstance());
    }

    private void initializeFields() {
        config = new FarmHelperConfig();
    }

    private void initializeCommands() {
        ClientCommandHandler.instance.registerCommand(new RewarpCommand());
        ClientCommandHandler.instance.registerCommand(new SpawnCommand());
        CommandManager.register(new FarmHelperMainCommand());
    }

}
