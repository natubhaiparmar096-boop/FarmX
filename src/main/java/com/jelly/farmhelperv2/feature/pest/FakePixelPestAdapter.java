package com.jelly.farmhelperv2.feature.pest;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.util.InventoryUtils;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlotUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FakePixelPestAdapter implements PestPlatformAdapter {

    private final Minecraft mc = Minecraft.getMinecraft();

    private static final List<String> KNOWN_PEST_NAMES = Arrays.asList(
            "beetle", "cricket", "earthworm", "fly", "locust", "mite",
            "mosquito", "moth", "rat", "slug", "praying mantis", "firefly", "dragonfly", "pest"
    );

    @Override
    public boolean isSupportedServer() {
        if (!FarmHelperConfig.fakePixelMode) {
            return true;
        }
        return true;
    }

    @Override
    public boolean isInGarden() {
        if (FarmHelperConfig.fakePixelMode) {
            return true;
        }
        return GameStateHandler.getInstance().inGarden();
    }

    @Override
    public List<PestInfo> detectPests() {
        List<PestInfo> detected = new ArrayList<>();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return detected;
        }

        long now = System.currentTimeMillis();
        double maxDist = FarmHelperConfig.pestMaxDetectionDistance;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == null || entity.isDead) continue;

            double dist = mc.thePlayer.getDistanceToEntity(entity);
            if (dist > maxDist) continue;

            String entityName = entity.getCustomNameTag();
            String nameLower = entityName != null ? entityName.toLowerCase(Locale.ENGLISH) : "";

            boolean isPest = false;
            String detectedType = "Pest";

            if (entity instanceof EntityArmorStand) {
                if (entityName != null && !entityName.isEmpty()) {
                    for (String pestName : KNOWN_PEST_NAMES) {
                        if (nameLower.contains(pestName)) {
                            isPest = true;
                            detectedType = capitalize(pestName);
                            break;
                        }
                    }
                    if (!isPest && (entityName.contains("ൠ") || nameLower.contains("hp") || nameLower.contains("lvl"))) {
                        isPest = true;
                    }
                }
            } else {
                String className = entity.getClass().getSimpleName().toLowerCase(Locale.ENGLISH);
                if (className.contains("silverfish") || className.contains("bat")) {
                    isPest = true;
                    detectedType = className.contains("silverfish") ? "Mite" : "Fly";
                }
            }

            if (isPest) {
                BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
                PlotUtils.Plot plot = PlotUtils.getPlotNumberBasedOnLocation(pos);
                int plotNum = plot != null && plot.number != null ? plot.number : -1;

                PestInfo info = new PestInfo(
                        entity,
                        detectedType,
                        pos,
                        dist,
                        entity.isEntityAlive(),
                        now,
                        plotNum
                );
                detected.add(info);
            }
        }
        return detected;
    }

    @Override
    public BlockPos getPestLocation(PestInfo pest) {
        if (pest == null || pest.getEntity() == null) return null;
        Entity e = pest.getEntity();
        return new BlockPos(e.posX, e.posY, e.posZ);
    }

    @Override
    public int findPestVacuumSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (isPestVacuumItem(stack)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isPestVacuumItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasDisplayName()) {
            return false;
        }
        String name = itemStack.getDisplayName().toLowerCase(Locale.ENGLISH);
        return name.contains("vacuum") || name.contains("hooverius") || name.contains("pest") || name.contains("destroyer");
    }

    @Override
    public boolean collectPest(PestInfo pest) {
        if (pest == null || pest.getEntity() == null || mc.thePlayer == null) return false;
        int vacuumSlot = findPestVacuumSlot();
        if (vacuumSlot != -1 && mc.thePlayer.inventory.currentItem != vacuumSlot) {
            mc.thePlayer.inventory.currentItem = vacuumSlot;
        }
        return true;
    }

    @Override
    public boolean isPestRemoved(PestInfo pest) {
        if (pest == null || pest.getEntity() == null) return true;
        Entity e = pest.getEntity();
        return e.isDead || !mc.theWorld.loadedEntityList.contains(e);
    }

    @Override
    public int getCurrentPlotNumber() {
        if (mc.thePlayer == null) return -1;
        PlotUtils.Plot plot = PlotUtils.getPlotNumberBasedOnLocation();
        return plot != null && plot.number != null ? plot.number : -1;
    }

    @Override
    public boolean handleSpray() {
        return false;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase(Locale.ENGLISH) + str.substring(1);
    }
}
