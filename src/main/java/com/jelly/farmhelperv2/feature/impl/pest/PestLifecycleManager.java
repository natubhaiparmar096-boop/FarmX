package com.jelly.farmhelperv2.feature.impl.pest;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.AutoPestExchangeManager;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestExchangeManager;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestLoadoutHelper;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import net.minecraft.client.Minecraft;

public final class PestLifecycleManager {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static Stage stage = Stage.IDLE;
    private static final Clock stageClock = new Clock();
    private static String targetPlot = null;

    public enum Stage {
        IDLE,
        PRE_SETHOME,
        WAIT_SETHOME,
        CLEANING,
        POST_CHECK_EXCHANGE,
        FINISH
    }

    private PestLifecycleManager() {}

    public static Stage getStage() {
        return stage;
    }

    public static void start(String plot) {
        if (stage != Stage.IDLE || mc.thePlayer == null) return;
        targetPlot = plot;
        stage = Stage.PRE_SETHOME;
        stageClock.schedule(100);

        PestReturnManager.saveFarmingState();
        LogUtils.sendWarning("[Pest] Infestation detected! Initiating pest cleanup routine...");
    }

    public static void reset() {
        stage = Stage.IDLE;
        targetPlot = null;
    }

    public static void onTick() {
        if (stage == Stage.IDLE || mc.thePlayer == null) return;
        if (!stageClock.passed()) return;

        switch (stage) {
            case PRE_SETHOME:
                // Stop movement and pause farming macro before setting home checkpoint
                if (MacroHandler.getInstance().isMacroToggled() && !MacroHandler.getInstance().isCurrentMacroPaused()) {
                    MacroHandler.getInstance().pauseMacro();
                }
                KeyBindUtils.stopMovement();
                mc.thePlayer.sendChatMessage("/sethome");
                stage = Stage.WAIT_SETHOME;
                stageClock.schedule(1500);
                break;

            case WAIT_SETHOME:
                // Equip vacuum
                int vacSlot = PestLoadoutHelper.findVacuumSlot();
                if (vacSlot >= 0) {
                    PestLoadoutHelper.equipSlot(vacSlot);
                }

                // Transition to cleaning
                stage = Stage.CLEANING;
                PestDestroyer.getInstance().startCleaning(targetPlot);
                break;

            case CLEANING:
                if (!PestDestroyer.getInstance().isRunning()) {
                    stage = Stage.POST_CHECK_EXCHANGE;
                    stageClock.schedule(300);
                }
                break;

            case POST_CHECK_EXCHANGE:
                if (FarmHelperConfig.autoPestExchange && !PestExchangeManager.isActive()) {
                    AutoPestExchangeManager.onTick();
                }
                stage = Stage.FINISH;
                stageClock.schedule(300);
                break;

            case FINISH:
                stage = Stage.IDLE;
                PestReturnManager.startReturn();
                break;
        }
    }
}
