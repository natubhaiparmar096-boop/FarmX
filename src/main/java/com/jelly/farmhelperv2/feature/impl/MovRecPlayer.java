package com.jelly.farmhelperv2.feature.impl;

import com.jelly.farmhelperv2.feature.IFeature;
import lombok.Getter;

/** No-op stub — movement recordings removed for simple FarmX. */
public class MovRecPlayer implements IFeature {
    private static MovRecPlayer instance;

    public static MovRecPlayer getInstance() {
        if (instance == null) instance = new MovRecPlayer();
        return instance;
    }

    @Getter
    private static float yawDifference = 0;

    public static void setYawDifference(float yaw) {
        yawDifference = yaw;
    }

    public void playRandomRecording(String ignoredPrefix) {}

    @Override
    public String getName() { return "MovRec Player"; }

    @Override
    public boolean isRunning() { return false; }

    @Override
    public boolean shouldPauseMacroExecution() { return false; }

    @Override
    public boolean shouldStartAtMacroStart() { return false; }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void resetStatesAfterMacroDisabled() {}

    @Override
    public boolean isToggled() { return false; }

    @Override
    public boolean shouldCheckForFailsafes() { return true; }
}
