package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.util.GardenPlots;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class PestPlotNavigator {
    private static final double[][] SCAN_OFFSETS = {
            {30.0, 30.0},
            {-30.0, 30.0},
            {-30.0, -30.0},
            {30.0, -30.0}
    };
    private static final double SCAN_EDGE_INSET = 8.0;

    private final String plotNumber;
    private final GardenPlots.Bounds bounds;
    private final Vec3 center;
    private int waypointIndex = 0;

    public PestPlotNavigator(String plotNumber) {
        this.plotNumber = PestPlotId.normalize(plotNumber);
        this.bounds = GardenPlots.boundsForPlot(this.plotNumber);
        this.center = bounds != null ? new Vec3(bounds.centerX(), 80, bounds.centerZ()) : null;
        this.waypointIndex = 0;
    }

    public String getPlotNumber() {
        return plotNumber;
    }

    public GardenPlots.Bounds getBounds() {
        return bounds;
    }

    public Vec3 getCenter() {
        return center;
    }

    public boolean hasNextWaypoint() {
        return waypointIndex < SCAN_OFFSETS.length;
    }

    public Vec3 nextWaypoint(double y) {
        if (bounds == null || !hasNextWaypoint()) return null;
        double[] offset = SCAN_OFFSETS[waypointIndex++];
        double x = bounds.clampX(bounds.centerX() + offset[0], SCAN_EDGE_INSET);
        double z = bounds.clampZ(bounds.centerZ() + offset[1], SCAN_EDGE_INSET);
        return new Vec3(x, y, z);
    }

    public void reset() {
        waypointIndex = 0;
    }
}
