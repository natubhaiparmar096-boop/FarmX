package com.jelly.farmhelperv2.hud;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.hud.TextHud;
import com.google.common.collect.Lists;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.failsafe.FailsafeManager;
import com.jelly.farmhelperv2.feature.impl.AutoReconnect;
import com.jelly.farmhelperv2.feature.impl.LeaveTimer;
import com.jelly.farmhelperv2.feature.impl.Scheduler;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.handler.MacroHandler;
import com.jelly.farmhelperv2.remote.DiscordBotHandler;
import com.jelly.farmhelperv2.util.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StatusHUD extends TextHud {

    private final boolean jdaDependencyPresent = Loader.isModLoaded("farmhelperjdadependency");
    private final List<String> cachedLines = new ArrayList<>();
    private long lastUpdateMs = 0;

    public StatusHUD() {
        // No rounded background / border — plain text only
        super(true, Minecraft.getMinecraft().displayWidth - 100, Minecraft.getMinecraft().displayHeight - 100, 1, false, false, 0, 2, 2, new OneColor(0, 0, 0, 0), false, 0, new OneColor(0, 0, 0, 0));
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        long now = System.currentTimeMillis();
        if (now - lastUpdateMs < 150 && !cachedLines.isEmpty()) {
            lines.addAll(cachedLines);
            return;
        }
        lastUpdateMs = now;
        cachedLines.clear();
        cachedLines.addAll(getStatusString());

        if (LeaveTimer.getInstance().isRunning()) {
            cachedLines.add("Leave: " + LogUtils.formatTime(Math.max(LeaveTimer.leaveClock.getRemainingTime(), 0)));
        }

        if (FarmHelperConfig.enableRemoteControl && jdaDependencyPresent) {
            String state = DiscordBotHandler.getInstance().getConnectingState();
            if (!Objects.equals(state, "")) {
                cachedLines.add(state);
            }
        }

        lines.addAll(cachedLines);
    }

    @Override
    protected boolean shouldShow() {
        if (!super.shouldShow()) {
            return false;
        }
        if (!FarmHelperConfig.showStatusHudOutsideGarden && !GameStateHandler.getInstance().inGarden()) {
            return false;
        }
        return !FarmHelperConfig.streamerMode;
    }

    public List<String> getStatusString() {
        if (AutoReconnect.getInstance().isRunning()) {
            return Lists.newArrayList("Reconnecting...");
        }
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) {
            return Lists.newArrayList(
                    "Emergency: §5" + LogUtils.capitalize(FailsafeManager.getInstance().triggeredFailsafe.get().getType().name()),
                    "Delay: §5" + LogUtils.formatTime(FailsafeManager.getInstance().getOnTickDelay().getRemainingTime())
            );
        } else if (FailsafeManager.getInstance().getRestartMacroAfterFailsafeDelay().isScheduled()) {
            return Lists.newArrayList(
                    "Restart: " + LogUtils.formatTime(FailsafeManager.getInstance().getRestartMacroAfterFailsafeDelay().getRemainingTime())
            );
        } else if (!MacroHandler.getInstance().isMacroToggled()) {
            return Lists.newArrayList(FarmHelperConfig.schedulerResetOnDisable ? "Idling" : Scheduler.getInstance().getStatusString());
        } else if (Scheduler.getInstance().isRunning()) {
            return Lists.newArrayList(Scheduler.getInstance().getStatusString());
        } else {
            return Lists.newArrayList("Macroing");
        }
    }
}
