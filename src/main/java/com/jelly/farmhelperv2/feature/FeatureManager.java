package com.jelly.farmhelperv2.feature;

import com.jelly.farmhelperv2.feature.impl.*;
import com.jelly.farmhelperv2.util.LogUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class FeatureManager {
    private static FeatureManager instance;
    private final ArrayList<IFeature> features = new ArrayList<>();

    public static FeatureManager getInstance() {
        if (instance == null) {
            instance = new FeatureManager();
        }
        return instance;
    }

    private Set<IFeature> pauseExecutionFeatures = new HashSet<>();

    public Set<IFeature> getPauseExecutionFeatures() { return pauseExecutionFeatures; }
    public void setPauseExecutionFeatures(Set<IFeature> pauseExecutionFeatures) { this.pauseExecutionFeatures = pauseExecutionFeatures; }

    public List<IFeature> fillFeatures() {
        List<IFeature> featuresList = Arrays.asList(
                AntiStuck.getInstance(),
                BPSTracker.getInstance(),
                DesyncChecker.getInstance(),
                LagDetector.getInstance(),
                AutoSprayonator.getInstance(),
                PestsDestroyer.getInstance(),
                PestFarmer.getInstance(),
                PestsDestroyerOnTheTrack.getInstance(),
                com.jelly.farmhelperv2.feature.pest.FakePixelPestController.getInstance(),
                FakePixelInlinePestKiller.getInstance()
        );
        features.addAll(featuresList);
        return features;
    }

    public boolean shouldPauseMacroExecution() {
        return !pauseExecutionFeatures.isEmpty();
    }

    public void disableAll() {
        features.forEach(feature -> {
            if (feature.isToggled() && feature.isRunning()) {
                feature.stop();
                LogUtils.sendDebug("Disabled feature: " + feature.getName());
            }
        });
        pauseExecutionFeatures.clear();
    }

    public void disableAllExcept(IFeature... sender) {
        features.forEach(feature -> {
            if (feature.isToggled() && feature.isRunning() && !Arrays.asList(sender).contains(feature)) {
                feature.stop();
                pauseExecutionFeatures.remove(feature);
                LogUtils.sendDebug("Disabled feature: " + feature.getName());
            }
        });
    }

    public void resetAllStates() {
        features.forEach(IFeature::resetStatesAfterMacroDisabled);
    }

    public boolean isAnyOtherFeatureEnabled(IFeature sender) {
        return pauseExecutionFeatures.stream().anyMatch(f -> f != sender);
    }

    public boolean isAnyOtherFeatureEnabled(IFeature... sender) {
        return pauseExecutionFeatures.stream().anyMatch(f -> !Arrays.asList(sender).contains(f));
    }

    public boolean shouldIgnoreFalseCheck() {
        return false;
    }

    public void enableAll() {
        features.forEach(feature -> {
            if (feature.shouldStartAtMacroStart() && feature.isToggled()) {
                feature.start();
                LogUtils.sendDebug("Enabled feature: " + feature.getName());
            }
        });
    }

    public void resume() {
        features.forEach(feature -> {
            if (feature.shouldStartAtMacroStart() && feature.isToggled()) {
                feature.resume();
                LogUtils.sendDebug("Enabled feature: " + feature.getName());
            }
        });
    }

    public void disableCurrentlyRunning(IFeature sender) {
        features.forEach(feature -> {
            if (feature.isRunning() && feature != sender) {
                feature.stop();
                LogUtils.sendDebug("Disabled feature: " + feature.getName());
            }
        });
    }

    public List<IFeature> getCurrentRunningFeatures() {
        List<IFeature> runningFeatures = new ArrayList<>();
        features.forEach(feature -> {
            if (feature.isRunning() && !feature.shouldStartAtMacroStart()) {
                runningFeatures.add(feature);
            }
        });
        return runningFeatures;
    }
}
