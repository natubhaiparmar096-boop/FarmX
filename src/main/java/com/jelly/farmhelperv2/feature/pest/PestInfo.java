package com.jelly.farmhelperv2.feature.pest;

import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;

public class PestInfo {
    private final Entity entity;
    private final String pestType;
    private final BlockPos location;
    private final double distance;
    private final boolean alive;
    private final long lastDetectedTime;
    private final int plotNumber;

    public PestInfo(Entity entity, String pestType, BlockPos location, double distance, boolean alive, long lastDetectedTime, int plotNumber) {
        this.entity = entity;
        this.pestType = pestType;
        this.location = location;
        this.distance = distance;
        this.alive = alive;
        this.lastDetectedTime = lastDetectedTime;
        this.plotNumber = plotNumber;
    }

    public Entity getEntity() {
        return entity;
    }

    public String getPestType() {
        return pestType;
    }

    public BlockPos getLocation() {
        return location;
    }

    public double getDistance() {
        return distance;
    }

    public boolean isAlive() {
        return alive;
    }

    public long getLastDetectedTime() {
        return lastDetectedTime;
    }

    public int getPlotNumber() {
        return plotNumber;
    }
}
