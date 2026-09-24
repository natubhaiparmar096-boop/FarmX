package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;

import java.util.Random;

public final class PestDefeatEffects {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random random = new Random();

    private PestDefeatEffects() {}

    public static void onPestDefeated(Entity pest) {
        if (!GameStateHandler.getInstance().inGarden() || mc.theWorld == null || pest == null) return;

        Vec3 pos = pest.getPositionVector().addVector(0, pest.getEyeHeight() * 0.5, 0);
        spawnDefeatParticles(pos);
    }

    public static void spawnDefeatParticles(Vec3 pos) {
        if (mc.theWorld == null || pos == null) return;

        for (int i = 0; i < 20; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 1.2;
            double offsetY = random.nextDouble() * 1.0;
            double offsetZ = (random.nextDouble() - 0.5) * 1.2;

            double speedX = (random.nextDouble() - 0.5) * 0.2;
            double speedY = random.nextDouble() * 0.2 + 0.1;
            double speedZ = (random.nextDouble() - 0.5) * 0.2;

            mc.theWorld.spawnParticle(
                    EnumParticleTypes.SPELL_WITCH,
                    pos.xCoord + offsetX,
                    pos.yCoord + offsetY,
                    pos.zCoord + offsetZ,
                    speedX, speedY, speedZ
            );
            mc.theWorld.spawnParticle(
                    EnumParticleTypes.VILLAGER_HAPPY,
                    pos.xCoord + offsetX,
                    pos.yCoord + offsetY,
                    pos.zCoord + offsetZ,
                    speedX, speedY, speedZ
            );
        }
    }
}
