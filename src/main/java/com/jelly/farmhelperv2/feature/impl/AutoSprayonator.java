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

public class AutoSprayonator implements IFeature {

  private static AutoSprayonator instance;

  public static AutoSprayonator getInstance() {
    if (instance == null) {
      instance = new AutoSprayonator();
    }
    return instance;
  }

  private final String[] SPRAY_MATERIAL = {"Fine Flour", "Compost", "Honey Jar", "Dung", "Plant Matter", "Tasty Cheese"};
  private final Minecraft mc = Minecraft.getMinecraft();
  private boolean enabled = false;
  private boolean pause = false;
  private State state = State.STARTING;
  private final Clock timer = new Clock();

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

  private String getSprayMaterial() {
    return this.SPRAY_MATERIAL[FarmHelperConfig.autoSprayonatorSprayMaterial];
  }

  @Override
  public void start() {
    if (this.enabled) {
      return;
    }

    if (!InventoryUtils.hasItemInHotbar("Sprayonator")) {
      LogUtils.sendError("Cannot find sprayonator in hotbar. Pausing until restart.");
      this.pause = true;
      return;
    }

    boolean correctMaterialSelected = false;

    for (String lore : InventoryUtils.getLoreOfItemInContainer(InventoryUtils.getSlotIdOfItemInContainer("Sprayonator"))) {
      if (lore.startsWith("Selected Material")) {
        correctMaterialSelected = lore.endsWith(this.getSprayMaterial());
        break;
      }
    }

    if (!correctMaterialSelected) {
      LogUtils.sendError("Please select " + this.getSprayMaterial() + " as your spray material. Pausing until restart.");
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
    if (!Scheduler.getInstance().isFarming()) {
      return;
    }
    if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) {
      return;
    }
    if (FeatureManager.getInstance().isAnyOtherFeatureEnabled(this)) {
      return;
    }
    if (GameStateHandler.getInstance().getSprayonatorState() != BuffState.NOT_ACTIVE) {
      if (this.timer.isScheduled()) this.timer.reset();
      return;
    }

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
    if (message.startsWith("SPRAYONATOR!") || message.equals("This plot was sprayed with that item recently! Try again soon!")) {
      this.swapState(State.END, FarmHelperConfig.autoSprayonatorAdditionalDelay);
      return;
    }

    if (message.startsWith("You don't have any ")) {
      LogUtils.sendError("Don't have any spray item. Auto-buy from Bazaar was removed. Pausing until restart.");
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
        if (this.isTimerRunning()) {
          break;
        }
        if (!InventoryUtils.holdItem("Sprayonator")) {
          LogUtils.sendError("Cannot hold sprayonator. Pausing until restart");
          this.pause = true;
          this.stop();
          break;
        }
        this.swapState(State.SPRAYING, FarmHelperConfig.autoSprayonatorAdditionalDelay);
        break;
      case SPRAYING:
        if (this.isTimerRunning()) {
          break;
        }
        KeyBindUtils.rightClick();
        this.swapState(State.WAITING, 5000);
        break;
      case WAITING:
        if (!this.isTimerRunning()) {
          LogUtils.sendError("Could not verify spray before time ended.");
          this.stop();
        }
        break;
      case END:
        if (this.isTimerRunning()) {
          break;
        }
        this.timer.schedule(2000);
        this.stop();
        break;
    }
  }

  public void swapState(State swapTo, int delay) {
    this.state = swapTo;
    this.timer.schedule(delay);
  }

  public boolean isTimerRunning() {
    return this.timer.isScheduled() && !this.timer.passed();
  }

  enum State {
    STARTING, SPRAYING, WAITING, END
}
}
