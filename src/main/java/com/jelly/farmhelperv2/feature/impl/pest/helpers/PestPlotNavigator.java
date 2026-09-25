package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.util.GardenPlots;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.LinkedList;
import java.util.Queue;

public final class PestPlotNavigator {
    // Center-first 5-point mesh: center + 4 quadrants
    private static final double[][] SCAN_OFFSETS = {
            {0.0, 0.0},     // Center first! Eliminates central dead zone
            {28.0, 28.0},   // Quadrant 1
            {-28.0, 28.0},  // Quadrant 2
            {-28.0, -28.0}, // Quadrant 3
            {28.0, -28.0},  // Quadrant 4
            {0.0, 32.0},    // North perimeter
            {0.0, -32.0},   // South perimeter
            {32.0, 0.0},    // East perimeter
            {-32.0, 0.0}    // West perimeter
    };
    private static final double SCAN_EDGE_INSET = 6.0;

    private final String plotNumber;
    private final GardenPlots.Bounds bounds;
    private final Vec3 center;
    private int waypointIndex = 0;
    private final Queue<Vec3> priorityWaypoints = new LinkedList<>();

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

    public void injectPriorityWaypoint(Vec3 target) {
        if (target == null) return;
        priorityWaypoints.clear();
        priorityWaypoints.add(target);
    }

    public boolean hasNextWaypoint() {
        return !priorityWaypoints.isEmpty() || waypointIndex < SCAN_OFFSETS.length;
    }

    public Vec3 nextWaypoint(double fallbackY) {
        if (bounds == null) return null;

        if (!priorityWaypoints.isEmpty()) {
            return priorityWaypoints.poll();
        }

        if (waypointIndex >= SCAN_OFFSETS.length) return null;

        double[] offset = SCAN_OFFSETS[waypointIndex++];
        double x = bounds.clampX(bounds.centerX() + offset[0], SCAN_EDGE_INSET);
        double z = bounds.clampZ(bounds.centerZ() + offset[1], SCAN_EDGE_INSET);
        double y = calculateRoofClearanceY(fallbackY);

        return new Vec3(x, y, z);
    }

    public double calculateRoofClearanceY(double fallbackY) {
        if (!FarmHelperConfig.pestRoofVacuuming) {
            return fallbackY > 0 ? fallbackY : 74.0;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || bounds == null) return Math.max(78.0, fallbackY);

        int cX = (int) bounds.centerX();
        int cZ = (int) bounds.centerZ();
        int highest = 70;

        // Sample center and quadrant corners to find the highest roof block (e.g. glass roof)
        int[][] samples = {
                {cX, cZ},
                {cX + 25, cZ + 25},
                {cX - 25, cZ + 25},
                {cX - 25, cZ - 25},
                {cX + 25, cZ - 25}
        };

        for (int[] s : samples) {
            for (int y = 90; y >= 65; y--) {
                Block b = mc.theWorld.getBlockState(new BlockPos(s[0], y, s[1])).getBlock();
                if (b != null && b != Blocks.air) {
                    if (y > highest) {
                        highest = y;
                    }
                    break;
                }
            }
        }

        // Fly 4 blocks above the highest roof/block for clean open-air flight & downward vacuum suction
        return Math.max(78.0, highest + 3.5);
    }

    public void reset() {
        waypointIndex = 0;
        priorityWaypoints.clear();
    }
}
