package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.event.SpawnParticleEvent;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class PestTrackerAbility {
    private static final PestTrackerAbility INSTANCE = new PestTrackerAbility();
    private static final PestTrackerTrail TRAIL = new PestTrackerTrail();
    private static long nextUseAt = 0;

    private PestTrackerAbility() {}

    public static PestTrackerAbility getInstance() {
        return INSTANCE;
    }

    public static void onLeftClick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || !GameStateHandler.getInstance().inGarden()) return;
        if (!PestLoadoutHelper.isVacuum(mc.thePlayer.getHeldItem())) return;

        long now = System.currentTimeMillis();
        if (now < nextUseAt) return;
        nextUseAt = now + 1000L;

        TRAIL.begin(mc.thePlayer.getPositionVector(), now);
    }

    @SubscribeEvent
    public void onMouse(net.minecraftforge.client.event.MouseEvent event) {
        if (event.button == 0 && event.buttonstate) {
            onLeftClick();
        }
    }

    @SubscribeEvent
    public void onInteract(net.minecraftforge.event.entity.player.PlayerInteractEvent event) {
        if (event.action == net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            onLeftClick();
        }
    }

    @SubscribeEvent
    public void onSpawnParticle(SpawnParticleEvent event) {
        if (!GameStateHandler.getInstance().inGarden()) return;
        if (event.getParticleTypes() == EnumParticleTypes.VILLAGER_ANGRY) {
            TRAIL.add(event.getPos());
        }
    }

    public static PestTrackerTrail getTrail() {
        return TRAIL;
    }

    public static void clear() {
        TRAIL.reset();
    }
}
