package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;

public final class PestLoadoutHelper {
    private PestLoadoutHelper() {}

    public static boolean isVacuum(ItemStack stack) {
        if (stack == null) return false;
        String name = StringUtils.stripControlCodes(stack.getDisplayName()).toLowerCase();
        return name.contains("vacuum") || name.contains("dyson") || name.contains("infini");
    }

    public static boolean isAOTV(ItemStack stack) {
        if (stack == null) return false;
        String name = StringUtils.stripControlCodes(stack.getDisplayName()).toLowerCase();
        return name.contains("aspect of the void") || name.contains("aspect of the end") || name.contains("aotv") || name.contains("aote");
    }

    public static int findVacuumSlot() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (isVacuum(stack)) {
                return i;
            }
        }
        return -1;
    }

    public static int findAotvSlot() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (isAOTV(stack)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean equipSlot(int slot) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || slot < 0 || slot >= 9) return false;
        if (mc.thePlayer.inventory.currentItem != slot) {
            mc.thePlayer.inventory.currentItem = slot;
        }
        return true;
    }
}
