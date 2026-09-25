package com.jelly.farmhelperv2.feature.impl.pest;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.AutoPestExchangeManager;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestExchangeManager;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestLoadoutHelper;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestPetManager;
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
        PRE_PAUSE,
        SWAP_HUNTING_PET,
        WAIT_PET_HUNT,
        START_CLEANING,
        CLEANING,
        POST_CHECK_EXCHANGE,
        SWAP_FARMING_PET,
        WAIT_PET_FARM,
        FINISH
    }

    private PestLifecycleManager() {}

    public static Stage getStage() {
        return stage;
    }

    public static void start(String plot) {
        if (stage != Stage.IDLE || mc.thePlayer == null) return;
        targetPlot = plot;
        stage = Stage.PRE_PAUSE;
        stageClock.schedule(100);

        PestReturnManager.saveFarmingState();
        LogUtils.sendWarning("[Pest] Infestation detected! Initiating pest cleanup routine...");
    }

    public static void reset() {
        stage = Stage.IDLE;
        targetPlot = null;
        PestPetManager.getInstance().reset();
    }

    public static void onTick() {
        if (stage == Stage.IDLE || mc.thePlayer == null) return;
        if (!stageClock.passed()) return;

        switch (stage) {
            case PRE_PAUSE:
                // Stop movement and pause farming macro
                if (MacroHandler.getInstance().isMacroToggled() && !MacroHandler.getInstance().isCurrentMacroPaused()) {
                    MacroHandler.getInstance().pauseMacro();
                }
                KeyBindUtils.stopMovement();
                stage = Stage.SWAP_HUNTING_PET;
                stageClock.schedule(300);
                break;

            case SWAP_HUNTING_PET:
                if (FarmHelperConfig.autoPetSwap) {
                    stage = Stage.WAIT_PET_HUNT;
                    PestPetManager.getInstance().equipHuntingPet(() -> {
                        stage = Stage.START_CLEANING;
                        stageClock.schedule(300);
                    });
                } else {
                    stage = Stage.START_CLEANING;
                    stageClock.schedule(100);
                }
                break;

            case WAIT_PET_HUNT:
                // PestPetManager processes in its own tick
                break;

            case START_CLEANING:
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
                stage = Stage.SWAP_FARMING_PET;
                stageClock.schedule(300);
                break;

            case SWAP_FARMING_PET:
                if (FarmHelperConfig.autoPetSwap) {
                    stage = Stage.WAIT_PET_FARM;
                    PestPetManager.getInstance().equipFarmingPet(() -> {
                        stage = Stage.FINISH;
                        stageClock.schedule(300);
                    });
                } else {
                    stage = Stage.FINISH;
                    stageClock.schedule(100);
                }
                break;

            case WAIT_PET_FARM:
                // PestPetManager processes in its own tick
                break;

            case FINISH:
                stage = Stage.IDLE;
                PestReturnManager.startReturn();
                break;
        }
    }
}
