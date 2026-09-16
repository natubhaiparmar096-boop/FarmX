package com.jelly.farmhelperv2.event;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SpawnParticleEvent extends Event {

    EnumParticleTypes particleTypes;
    boolean isLongDistance;
    double xCoord;
    double yCoord;
    double zCoord;

    double xOffset;
    double yOffset;
    double zOffset;
    int[] params;

    public EnumParticleTypes getParticleTypes() { return particleTypes; }
    public boolean isLongDistance() { return isLongDistance; }
    public double getXCoord() { return xCoord; }
    public double getYCoord() { return yCoord; }
    public double getZCoord() { return zCoord; }
    public double getXOffset() { return xOffset; }
    public double getYOffset() { return yOffset; }
    public double getZOffset() { return zOffset; }
    public int[] getParams() { return params; }

    public SpawnParticleEvent(
            EnumParticleTypes particleTypes,
            boolean isLongDistance,
            double xCoord, double yCoord, double zCoord,
            double xOffset, double yOffset, double zOffset,
            int[] params
    ) {
        this.particleTypes = particleTypes;
        this.isLongDistance = isLongDistance;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.zCoord = zCoord;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.params = params;
    }

    public Vec3 getPos() {
        return new Vec3(xCoord, yCoord, zCoord);
    }
}
