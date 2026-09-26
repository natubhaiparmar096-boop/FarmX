package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.RotationHandler;
import com.jelly.farmhelperv2.util.KeyBindUtils;
import com.jelly.farmhelperv2.util.helper.Clock;
import com.jelly.farmhelperv2.util.helper.Rotation;
import com.jelly.farmhelperv2.util.helper.RotationConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public final class PestCombatCoordinator {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Clock aotvCooldown = new Clock();
    private static final Clock vacuumHoldClock = new Clock();

    private PestCombatCoordinator() {}

    public static void aimAtPest(Entity target, int rotationTime) {
        if (target == null || mc.thePlayer == null) return;

        // Velocity-compensated aim point
        double targetX = target.posX + target.motionX * 1.2;
        double targetY = target.posY + target.getEyeHeight() * 0.5 + target.motionY * 1.2;
        double targetZ = target.posZ + target.motionZ * 1.2;

        double dX = targetX - mc.thePlayer.posX;
        double dY = targetY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dZ = targetZ - mc.thePlayer.posZ;
        double dist = MathHelper.sqrt_double(dX * dX + dZ * dZ);

        float targetYaw = (float) (MathHelper.atan2(dZ, dX) * (180.0D / Math.PI)) - 90.0F;
        float targetPitch = (float) (-(MathHelper.atan2(dY, dist) * (180.0D / Math.PI)));

        RotationHandler.getInstance().easeTo(new RotationConfiguration(
                new Rotation(targetYaw, targetPitch),
                rotationTime,
                null
        ));
    }

    public static void aimAtDownward(Vec3 targetPos, int rotationTime) {
        if (targetPos == null || mc.thePlayer == null) return;

        double dX = targetPos.xCoord - mc.thePlayer.posX;
        double dY = targetPos.yCoord - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dZ = targetPos.zCoord - mc.thePlayer.posZ;
        double dist = MathHelper.sqrt_double(dX * dX + dZ * dZ);

        float targetYaw = (float) (MathHelper.atan2(dZ, dX) * (180.0D / Math.PI)) - 90.0F;
        float targetPitch = (float) (-(MathHelper.atan2(dY, dist) * (180.0D / Math.PI)));

        // If directly above, clamp pitch down to 80-90 degrees
        if (dist < 3.0) {
            targetPitch = Math.max(targetPitch, 80.0f);
        }

        RotationHandler.getInstance().easeTo(new RotationConfiguration(
                new Rotation(targetYaw, targetPitch),
                rotationTime,
                null
        ));
    }

    public static void startVacuum() {
        if (mc.thePlayer == null) return;
        int vacSlot = PestLoadoutHelper.findVacuumSlot();
        if (vacSlot >= 0 && mc.thePlayer.inventory.currentItem != vacSlot) {
            PestLoadoutHelper.equipSlot(vacSlot);
        }
        KeyBindUtils.holdThese(mc.gameSettings.keyBindUseItem);
        vacuumHoldClock.schedule(500);
    }

    public static void stopVacuum() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        }
    }

    public static boolean performAotvHop(Vec3 targetPos) {
        if (mc.thePlayer == null || targetPos == null) return false;
        if (!aotvCooldown.passed()) return false;

        int aotvSlot = PestLoadoutHelper.findAotvSlot();
        if (aotvSlot < 0) return false;

        int prevSlot = mc.thePlayer.inventory.currentItem;
        PestLoadoutHelper.equipSlot(aotvSlot);

        // Aim towards hop target
        double dX = targetPos.xCoord - mc.thePlayer.posX;
        double dY = targetPos.yCoord - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dZ = targetPos.zCoord - mc.thePlayer.posZ;
        double dist = MathHelper.sqrt_double(dX * dX + dZ * dZ);

        float yaw = (float) (MathHelper.atan2(dZ, dX) * (180.0D / Math.PI)) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(dY, dist) * (180.0D / Math.PI)));

        mc.thePlayer.rotationYaw = yaw;
        mc.thePlayer.rotationPitch = pitch;

        // Right click AOTV
        KeyBindUtils.rightClick();
        aotvCooldown.schedule(1500);

        // Swap back to vacuum
        int vacSlot = PestLoadoutHelper.findVacuumSlot();
        if (vacSlot >= 0) {
            PestLoadoutHelper.equipSlot(vacSlot);
        } else {
            PestLoadoutHelper.equipSlot(prevSlot);
        }
        return true;
    }
}
