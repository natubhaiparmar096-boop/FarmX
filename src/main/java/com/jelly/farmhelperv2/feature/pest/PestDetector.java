package com.jelly.farmhelperv2.feature.pest;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PestDetector {

    private final PestPlatformAdapter adapter;
    private final List<PestInfo> cachedPests = new ArrayList<>();
    private long lastScanTime = 0L;

    public PestDetector(PestPlatformAdapter adapter) {
        this.adapter = adapter;
    }

    public List<PestInfo> scanPests(boolean force) {
        long now = System.currentTimeMillis();
        long scanInterval = FarmHelperConfig.pestScanThrottleMs;

        if (!force && (now - lastScanTime < scanInterval)) {
            return new ArrayList<>(cachedPests);
        }

        lastScanTime = now;
        cachedPests.clear();

        if (adapter.isInGarden()) {
            List<PestInfo> scanned = adapter.detectPests();
            cachedPests.addAll(scanned);
        }

        return new ArrayList<>(cachedPests);
    }

    public PestInfo selectTargetPest() {
        List<PestInfo> pests = scanPests(false);
        if (pests.isEmpty()) {
            return null;
        }

        int currentPlot = adapter.getCurrentPlotNumber();
        int priorityMode = FarmHelperConfig.pestPriorityMode;

        switch (priorityMode) {
            case 1: // CURRENT_PLOT
                for (PestInfo p : pests) {
                    if (p.getPlotNumber() != -1 && p.getPlotNumber() == currentPlot) {
                        return p;
                    }
                }
                // Fallback to nearest if no pest on current plot
                pests.sort(Comparator.comparingDouble(PestInfo::getDistance));
                return pests.get(0);

            case 2: // FIRST_DETECTED
                return pests.get(0);

            case 0: // NEAREST
            default:
                pests.sort(Comparator.comparingDouble(PestInfo::getDistance));
                return pests.get(0);
        }
    }

    public List<PestInfo> getCachedPests() {
        return new ArrayList<>(cachedPests);
    }

    public void clearCache() {
        cachedPests.clear();
        lastScanTime = 0L;
    }

    public PestPlatformAdapter getAdapter() {
        return adapter;
    }
}
