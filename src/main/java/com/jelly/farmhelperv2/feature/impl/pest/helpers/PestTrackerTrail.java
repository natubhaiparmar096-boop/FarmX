package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class PestTrackerTrail {
    private final List<Vec3> points = new ArrayList<>();
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

    public Vec3 getLatestDirection() {
        if (points.size() < 2) return null;
        Vec3 first = points.get(0);
        Vec3 last = points.get(points.size() - 1);
        return new Vec3(last.xCoord - first.xCoord, last.yCoord - first.yCoord, last.zCoord - first.zCoord);
    }
}
