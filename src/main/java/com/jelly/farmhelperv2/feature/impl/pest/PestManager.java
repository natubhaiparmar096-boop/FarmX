package com.jelly.farmhelperv2.feature.impl.pest;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.AutoPestExchangeManager;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestExchangeManager;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestTabSnapshot;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestTrapManager;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PestManager {
    private static final PestManager INSTANCE = new PestManager();
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final Pattern SPAWN_PATTERN_1 =
            Pattern.compile("(?i)(?:Gross! )?A Pest has appeared in Plot (?:(?:#?|plot ))?([0-9]{1,2})!?");
    private static final Pattern SPAWN_PATTERN_2 =
            Pattern.compile("(?i)(?:There are pests in Plot |You feel a strange vibration... A Pest has appeared in Plot )([0-9]{1,2})");
    private static final Pattern KILL_PATTERN =
            Pattern.compile("(?i)(?:You killed a Pest!|You eliminated a Pest!|RARE DROP! .+)");

    private static int totalPestsKilled = 0;
    private static int sessionPestsKilled = 0;
    private static final Clock reentryCooldown = new Clock();
    private static final Clock tabCheckClock = new Clock();

    private PestManager() {}

    public static PestManager getInstance() {
        return INSTANCE;
    }

    public static int getSessionPestsKilled() {
        return sessionPestsKilled;
    }

    public static int getTotalPestsKilled() {
        return totalPestsKilled;
    }

    public static void resetStats() {
        sessionPestsKilled = 0;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!GameStateHandler.getInstance().inGarden()) return;
        String clean = StringUtils.stripControlCodes(event.message.getUnformattedText());

        Matcher m1 = SPAWN_PATTERN_1.matcher(clean);
        Matcher m2 = SPAWN_PATTERN_2.matcher(clean);
        String plot = null;
        boolean matched = false;

        if (m1.find()) {
            plot = m1.group(1);
            matched = true;
        } else if (m2.find()) {
            plot = m2.group(1);
            matched = true;
        }

        if (matched) {
            LogUtils.sendWarning("[Pest] Detected pest spawn in Plot " + (plot != null ? plot : "unknown"));

            if (FarmHelperConfig.enablePestDestroyer && FarmHelperConfig.pestTriggerOnChat) {
                if (reentryCooldown.passed() && !PestDestroyer.getInstance().isRunning() && !PestReturnManager.isReturning()) {
                    if (MacroHandler.getInstance().isMacroToggled() || FarmHelperConfig.manualPestMode) {
                        PestLifecycleManager.start(plot);
                    }
                }
            }
        }

        if (KILL_PATTERN.matcher(clean).find()) {
            sessionPestsKilled++;
            totalPestsKilled++;
            LogUtils.sendSuccess("[Pest] Pest eliminated! (Session: " + sessionPestsKilled + ")");
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || !GameStateHandler.getInstance().inGarden()) return;

        // Lifecycle & return tick
        PestLifecycleManager.onTick();
        PestReturnManager.onTick();
        PestExchangeManager.onTick();
        PestTrapManager.onTick();
        AutoPestExchangeManager.onTick();

        // Check tablist threshold if enabled
        if (FarmHelperConfig.enablePestDestroyer && FarmHelperConfig.pestTriggerOnTabThreshold) {
            if (tabCheckClock.passed()) {
                tabCheckClock.schedule(2000);
                PestTabSnapshot snapshot = PestTabSnapshot.read();
                if (snapshot.getAliveCount() >= FarmHelperConfig.pestThreshold) {
                    if (reentryCooldown.passed() && !PestDestroyer.getInstance().isRunning() && !PestReturnManager.isReturning() && PestLifecycleManager.getStage() == PestLifecycleManager.Stage.IDLE) {
                        if (MacroHandler.getInstance().isMacroToggled() || FarmHelperConfig.manualPestMode) {
                            String firstPlot = snapshot.getInfestedPlots().isEmpty() ? null : snapshot.getInfestedPlots().iterator().next();
                            PestLifecycleManager.start(firstPlot);
                            reentryCooldown.schedule(30_000);
                        }
                    }
                }
            }
        }
    }
}
