package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.event.ReceivePacketEvent;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.util.GardenPlots;
import com.jelly.farmhelperv2.util.LogUtils;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class PestSoundTracker {
    private static final PestSoundTracker INSTANCE = new PestSoundTracker();
    private final List<AcousticSignal> signals = Collections.synchronizedList(new ArrayList<>());

    public static final class AcousticSignal {
        private final Vec3 position;
        private final String soundName;
        private final long timestamp;

        public AcousticSignal(Vec3 position, String soundName, long timestamp) {
            this.position = position;
            this.soundName = soundName;
            this.timestamp = timestamp;
        }

        public Vec3 getPosition() {
            return position;
        }

        public String getSoundName() {
            return soundName;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isExpired(long maxAgeMs) {
            return (System.currentTimeMillis() - timestamp) > maxAgeMs;
        }
    }

    private PestSoundTracker() {}

    public static PestSoundTracker getInstance() {
        return INSTANCE;
    }

    public void clear() {
        signals.clear();
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!FarmHelperConfig.pestAcousticRadar || !GameStateHandler.getInstance().inGarden()) return;
        if (event.packet == null || !(event.packet instanceof S29PacketSoundEffect)) return;

        S29PacketSoundEffect sound = (S29PacketSoundEffect) event.packet;
        String name = sound.getSoundName();
        if (name == null) return;

        if (isPestSound(name)) {
            Vec3 pos = new Vec3(sound.getX(), sound.getY(), sound.getZ());
            long now = System.currentTimeMillis();
            signals.add(new AcousticSignal(pos, name, now));
            LogUtils.sendDebug("[PestSoundTracker] Detected pest acoustic pulse: " + name + " at " + String.format("(%.1f, %.1f, %.1f)", pos.xCoord, pos.yCoord, pos.zCoord));
            pruneExpiredSignals(15000);
        }
    }

    private boolean isPestSound(String name) {
        String lower = name.toLowerCase();
        return lower.startsWith("mob.silverfish.") ||
                lower.startsWith("mob.bat.") ||
                lower.contains("silverfish") ||
                lower.contains("bat");
    }

    private void pruneExpiredSignals(long maxAgeMs) {
        synchronized (signals) {
            Iterator<AcousticSignal> it = signals.iterator();
            while (it.hasNext()) {
                if (it.next().isExpired(maxAgeMs)) {
                    it.remove();
                }
            }
        }
    }

    public AcousticSignal getLatestSignalWithin(long maxAgeMs, GardenPlots.Bounds bounds) {
        pruneExpiredSignals(maxAgeMs);
        synchronized (signals) {
            for (int i = signals.size() - 1; i >= 0; i--) {
                AcousticSignal signal = signals.get(i);
                if (!signal.isExpired(maxAgeMs)) {
                    if (bounds == null || bounds.contains(signal.getPosition().xCoord, signal.getPosition().zCoord, 6.0)) {
                        return signal;
                    }
                }
            }
        }
        return null;
    }

    public boolean hasFreshSignal(long maxAgeMs, GardenPlots.Bounds bounds) {
        return getLatestSignalWithin(maxAgeMs, bounds) != null;
    }
}
