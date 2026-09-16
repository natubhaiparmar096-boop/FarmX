package com.jelly.farmhelperv2.feature.impl;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.event.UpdateTablistEvent;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.feature.FeatureManager;
import com.jelly.farmhelperv2.feature.IFeature;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.GameStateHandler.BuffState;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.util.InventoryUtils;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

/**
 * AutoSprayonator - Automatically uses the Sprayonator item when its buff expires.
 * Ported from FarmHelper (JellyLabScripts) and adapted for FarmX.
 * Note: AutoBazaar (auto-buy from bazaar) is not supported in FarmX; the feature
 * will pause and log an error if spray material runs out.
 */
public class AutoSprayonator implements IFeature {

    private static AutoSprayonator instance;

    public static AutoSprayonator getInstance() {
        if (instance == null) {
            instance = new AutoSprayonator();
        }
        return instance;
    }

    private static final String[] SPRAY_MATERIAL = {
        "Fine Flour", "Compost", "Honey Jar", "Dung", "Plant Matter", "Tasty Cheese"
    };

    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean enabled = false;
    private boolean pause = false;
    private State state = State.STARTING;
    private final Clock timer = new Clock();

    // ------------------------------------------------------------------ IFeature

    @Override
    public String getName() {
        return "AutoSprayonator";
    }

    @Override
    public boolean isRunning() {
        return this.enabled;
    }

    @Override
    public boolean shouldPauseMacroExecution() {
        return true;
    }

    @Override
    public boolean shouldStartAtMacroStart() {
        return false;
    }

    @Override
    public void resetStatesAfterMacroDisabled() {
        this.pause = false;
        this.state = State.STARTING;
    }

    @Override
    public boolean isToggled() {
        return FarmHelperConfig.autoSprayonator;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return false;
    }

    // ------------------------------------------------------------------ helpers

    private String getSprayMaterial() {
        return SPRAY_MATERIAL[FarmHelperConfig.autoSprayonatorSprayMaterial];
    }

    public boolean isTimerRunning() {
        return this.timer.isScheduled() && !this.timer.passed();
    }

    public void swapState(State swapTo, int delay) {
        this.state = swapTo;
        this.timer.schedule(delay);
    }

    // ------------------------------------------------------------------ lifecycle

    @Override
    public void start() {
        if (this.enabled) {
            return;
        }

        if (!InventoryUtils.hasItemInHotbar("Sprayonator")) {
            LogUtils.sendError("[AutoSprayonator] Cannot find Sprayonator in hotbar. Pausing until restart.");
            this.pause = true;
            return;
        }

        // Verify the correct spray material is selected in the Sprayonator lore
        boolean correctMaterialSelected = false;
        int slot = InventoryUtils.getSlotIdOfItemInContainer("Sprayonator");
        for (String lore : InventoryUtils.getLoreOfItemInContainer(slot)) {
            if (lore.startsWith("Selected Material")) {
                correctMaterialSelected = lore.endsWith(this.getSprayMaterial());
                break;
            }
        }

        if (!correctMaterialSelected) {
            LogUtils.sendError("[AutoSprayonator] Please select " + this.getSprayMaterial()
                    + " as your spray material. Pausing until restart.");
            this.pause = true;
            return;
        }

        MacroHandler.getInstance().pauseMacro();
        this.timer.schedule(FarmHelperConfig.autoSprayonatorAdditionalDelay);
        this.enabled = true;
    }

    @Override
    public void stop() {
        this.enabled = false;
        this.state = State.STARTING;
        if (MacroHandler.getInstance().isMacroToggled()) {
            MacroHandler.getInstance().resumeMacro();
        }
    }

    // ------------------------------------------------------------------ events

    @SubscribeEvent
    public void onTablistUpdate(UpdateTablistEvent event) {
        if (!this.isToggled() || !MacroHandler.getInstance().isCurrentMacroEnabled() || this.enabled || this.pause) {
            return;
        }
        if (this.isTimerRunning()) {
            return;
        }
        if (!GameStateHandler.getInstance().inGarden()) {
            return;
        }
        if (GameStateHandler.getInstance().getServerClosingSeconds().isPresent()) {
            return;
        }
        // Replace Scheduler.getInstance().isFarming() — require macro to be actively running
        if (!MacroHandler.getInstance().isMacroToggled()) {
            return;
        }
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) {
            return;
        }
        if (FeatureManager.getInstance().isAnyOtherFeatureEnabled(this)) {
            return;
        }
        // Sprayonator buff still active — reset/skip
        if (GameStateHandler.getInstance().getSprayonatorState() != BuffState.NOT_ACTIVE) {
            if (this.timer.isScheduled()) this.timer.reset();
            return;
        }

        // Start delay before activating
        if (!this.timer.isScheduled()) {
            this.timer.schedule(FarmHelperConfig.autoSprayonatorStartDelay);
        } else if (this.timer.passed()) {
            this.start();
        }
    }

    @SubscribeEvent
    public void onChatEvent(ClientChatReceivedEvent event) {
        if (!this.enabled || this.state != State.WAITING) {
            return;
        }

        final String message = event.message.getUnformattedText();

        if (message.startsWith("SPRAYONATOR!")
                || message.equals("This plot was sprayed with that item recently! Try again soon!")) {
            this.swapState(State.END, FarmHelperConfig.autoSprayonatorAdditionalDelay);
            return;
        }

        if (message.startsWith("You don't have any ")) {
            // AutoBazaar is not available in FarmX — just pause
            LogUtils.sendError("[AutoSprayonator] Out of spray material (" + this.getSprayMaterial()
                    + "). Pausing until restart. (AutoBazaar not supported in FarmX)");
            this.pause = true;
            this.swapState(State.END, FarmHelperConfig.autoSprayonatorAdditionalDelay);
        }
    }

    @SubscribeEvent
    public void onTickSpray(ClientTickEvent event) {
        if (!this.enabled) {
            return;
        }

        switch (this.state) {
            case STARTING:
                if (this.isTimerRunning()) break;
                if (!InventoryUtils.holdItem("Sprayonator")) {
                    LogUtils.sendError("[AutoSprayonator] Cannot hold Sprayonator. Pausing until restart.");
                    this.pause = true;
                    this.stop();
                    break;
                }
                this.swapState(State.SPRAYING, FarmHelperConfig.autoSprayonatorAdditionalDelay);
                break;

            case SPRAYING:
                if (this.isTimerRunning()) break;
                KeyBindUtils.rightClick();
                this.swapState(State.WAITING, 5000);
                break;

            case WAITING:
                if (!this.isTimerRunning()) {
                    LogUtils.sendError("[AutoSprayonator] Could not verify spray before time ended.");
                    this.stop();
                }
                break;

            case END:
                if (this.isTimerRunning()) break;
                this.timer.schedule(2000);
                this.stop();
                break;
        }
    }

    // ------------------------------------------------------------------ state

    enum State {
        STARTING, SPRAYING, WAITING, END
    }
}
