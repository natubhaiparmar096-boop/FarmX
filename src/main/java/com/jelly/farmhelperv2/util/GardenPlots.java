package com.jelly.farmhelperv2.util;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/**
 * 96x96 Garden plot grid geometry for Hypixel / Fakepixel Skyblock.
 * Plots 0 to 24 arranged in a 5x5 grid around Barn (Plot 0) at (0, 0).
 */
public final class GardenPlots {
    public static final int PLOT_SIZE = 96;
    // Fakepixel garden spans world X/Z 0-479 (480 blocks = 5×96).
    // Barn (plot 0) center is at (240,240). PLOT_OFFSET=-192 so that:
    //   gridIndex(0)=-2, gridIndex(192)=0 (Barn), gridIndex(479)=2
    public static final int PLOT_OFFSET = -192;

    private static final int[][] PLOT_LAYOUT = {
            {21, 13,  9, 14, 22},
            {15,  5,  1,  6, 16},
            {10,  2,  0,  3, 11},
            {17,  7,  4,  8, 18},
            {23, 19, 12, 20, 24}
    };

    private GardenPlots() {}

    public static int[] gridForPlot(int plot) {
        for (int row = 0; row < PLOT_LAYOUT.length; row++) {
            for (int col = 0; col < PLOT_LAYOUT[row].length; col++) {
                if (PLOT_LAYOUT[row][col] == plot) {
                    return new int[] { col - 2, row - 2 };
                }
            }
        }
        return null;
    }

    public static Bounds boundsForPlot(int plot) {
        int[] grid = gridForPlot(plot);
        return grid == null ? null : boundsForGrid(grid[0], grid[1]);
    }

    public static Bounds boundsForPlot(String plot) {
        if (plot == null) return null;
        String digits = plot.replaceAll("\\D", "");
        if (digits.isEmpty()) return null;
        try {
            int n = Integer.parseInt(digits);
            if (n < 0 || n > 24) {
                return null; // reject out-of-range numbers (e.g. "73" from bad tablist parse)
            }
            return boundsForPlot(n);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static Bounds boundsContaining(double x, double z) {
        return boundsForGrid(gridIndex(x), gridIndex(z));
    }

    public static Bounds boundsForGrid(int gridX, int gridZ) {
        int minX = gridX * PLOT_SIZE - PLOT_OFFSET;
        int minZ = gridZ * PLOT_SIZE - PLOT_OFFSET;
        return new Bounds(minX, minZ, minX + PLOT_SIZE, minZ + PLOT_SIZE);
    }

    public static int gridIndex(double coord) {
        return Math.floorDiv(MathHelper.floor_double(coord) + PLOT_OFFSET, PLOT_SIZE);
    }

    public static final class Bounds {
        private final int minX;
        private final int minZ;
        private final int maxX;
        private final int maxZ;

        public Bounds(int minX, int minZ, int maxX, int maxZ) {
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
        }

        public int getMinX() { return minX; }
        public int getMinZ() { return minZ; }
        public int getMaxX() { return maxX; }
        public int getMaxZ() { return maxZ; }

        public boolean contains(double x, double z, double margin) {
            return x >= minX - margin && x < maxX + margin
                    && z >= minZ - margin && z < maxZ + margin;
        }

        public double centerX() {
            return (minX + maxX) / 2.0;
        }

        public double centerZ() {
            return (minZ + maxZ) / 2.0;
        }

        public double clampX(double x, double inset) {
            return MathHelper.clamp_double(x, minX + inset, maxX - inset);
        }

        public double clampZ(double z, double inset) {
            return MathHelper.clamp_double(z, minZ + inset, maxZ - inset);
        }

        public AxisAlignedBB toAABB(double minY, double maxY) {
            return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        }

        public Vec3 getCenter(double y) {
            return new Vec3(centerX(), y, centerZ());
        }
    }
}
