package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.util.GardenPlots;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PestTrackerTrail {
    private final List<Vec3> points = Collections.synchronizedList(new ArrayList<>());
    private long requestedAt = 0;

    public void begin(Vec3 origin, long now) {
        points.clear();
        if (origin != null) {
            points.add(origin);
        }
        this.requestedAt = now;
    }

    public void add(Vec3 point) {
        if (point != null) {
            points.add(point);
        }
    }

    public void reset() {
        points.clear();
        requestedAt = 0;
    }

    public List<Vec3> getPoints() {
        return points;
    }

    public long getRequestedAt() {
        return requestedAt;
    }

    public boolean isFresh(long maxAgeMs) {
        return requestedAt > 0 && (System.currentTimeMillis() - requestedAt) <= maxAgeMs && points.size() >= 2;
    }

    public Vec3 getLatestDirection() {
        if (points.size() < 2) return null;
        Vec3 first = points.get(0);
        Vec3 last = points.get(points.size() - 1);
        double dx = last.xCoord - first.xCoord;
        double dy = last.yCoord - first.yCoord;
        double dz = last.zCoord - first.zCoord;
        double len = MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz);
        if (len < 0.001) return null;
        return new Vec3(dx / len, dy / len, dz / len);
    }

    public Vec3 projectWaypoint(Vec3 origin, double distance, GardenPlots.Bounds bounds, double targetY) {
        Vec3 dir = getLatestDirection();
        if (dir == null || origin == null) return null;

        double targetX = origin.xCoord + dir.xCoord * distance;
        double targetZ = origin.posZ + dir.zCoord * distance;

        if (bounds != null) {
            targetX = bounds.clampX(targetX, 6.0);
            targetZ = bounds.clampZ(targetZ, 6.0);
        }

        return new Vec3(targetX, targetY, targetZ);
    }
}
