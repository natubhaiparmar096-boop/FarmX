package com.jelly.farmhelperv2.feature.impl;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.feature.IFeature;
import com.jelly.farmhelperv2.feature.pest.FakePixelPestAdapter;
import com.jelly.farmhelperv2.feature.pest.PestInfo;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.handler.RotationHandler;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import com.jelly.farmhelperv2.util.helper.RotationConfiguration;
import com.jelly.farmhelperv2.util.helper.Target;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Comparator;
import java.util.List;

public class FakePixelInlinePestKiller implements IFeature {

    private static FakePixelInlinePestKiller instance;

    public static FakePixelInlinePestKiller getInstance() {
        if (instance == null) {
            instance = new FakePixelInlinePestKiller();
        }
        return instance;
    }

    private final Minecraft mc = Minecraft.getMinecraft();
    private final FakePixelPestAdapter adapter = new FakePixelPestAdapter();

    private boolean isRunning = false;
    private int savedSlot = -1;
    private Entity currentTarget = null;

    private final Clock attackTimer = new Clock();
    private final Clock stuckTimer = new Clock();
    private final Clock noPestTimer = new Clock();

    @Override
    public String getName() {
        return "FakePixel Inline Pest Killer";
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public boolean shouldPauseMacroExecution() {
        return false;
    }

    @Override
    public boolean shouldStartAtMacroStart() {
        return false;
    }

    @Override
    public void start() {
        if (isRunning) return;
        isRunning = true;
        currentTarget = null;
        savedSlot = mc.thePlayer != null ? mc.thePlayer.inventory.currentItem : -1;
        stuckTimer.schedule(5000);
        LogUtils.sendDebug("[Inline Pest Killer] Activated!");
        IFeature.super.start();
    }

    @Override
    public void stop() {
        if (!isRunning) return;
        isRunning = false;
        currentTarget = null;
        if (savedSlot != -1 && mc.thePlayer != null) {
            mc.thePlayer.inventory.currentItem = savedSlot;
            savedSlot = -1;
        }
        attackTimer.reset();
        stuckTimer.reset();
        noPestTimer.reset();
        IFeature.super.stop();
    }

    @Override
    public void resetStatesAfterMacroDisabled() {
        stop();
    }

    @Override
    public boolean isToggled() {
        return FarmHelperConfig.fakePixelMode && FarmHelperConfig.fakePixelInlinePestKiller;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (!isToggled() || !MacroHandler.getInstance().isMacroToggled()) {
            if (isRunning) stop();
            return;
        }

        // Check if any pest is within vacuum range
        List<PestInfo> pests = adapter.detectPests();
        PestInfo inRangePest = pests.stream()
                .filter(p -> p.isAlive() && p.getEntity() != null && !p.getEntity().isDead && p.getDistance() <= FarmHelperConfig.pestVacuumRange)
                .min(Comparator.comparingDouble(PestInfo::getDistance))
                .orElse(null);

        if (inRangePest == null) {
            if (isRunning) {
                if (!noPestTimer.isScheduled()) {
                    noPestTimer.schedule(500);
                }
                if (noPestTimer.passed()) {
                    stop();
                }
            }
            return;
        }

        noPestTimer.reset();

        if (!isRunning) {
            start();
        }

        if (stuckTimer.isScheduled() && stuckTimer.passed()) {
            stop();
            return;
        }

        Entity target = inRangePest.getEntity();
        if (currentTarget == null || !currentTarget.equals(target)) {
            currentTarget = target;
            stuckTimer.schedule(5000);
        }

        // Equip vacuum
        int vacuumSlot = adapter.findPestVacuumSlot();
        if (vacuumSlot != -1 && mc.thePlayer.inventory.currentItem != vacuumSlot) {
            mc.thePlayer.inventory.currentItem = vacuumSlot;
        }

        // Aim at pest
        if (!RotationHandler.getInstance().isRotating()) {
            RotationHandler.getInstance().easeTo(new RotationConfiguration(
                    new Target(target),
                    150L,
                    null
            ).followTarget(true));
        }

        // Use vacuum
        if (attackTimer.passed()) {
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
            attackTimer.schedule(200);
        }
    }
}
