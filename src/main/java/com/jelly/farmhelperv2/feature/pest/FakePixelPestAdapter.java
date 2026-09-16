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

    public static final List<net.minecraft.util.Tuple<String, String>> PEST_TEXTURES = Arrays.asList(
            new net.minecraft.util.Tuple<>("Beetle", "70a1e836bf1968b2eaa4837227a19204f17295d870ee9e754bd6b6d60ddeed3c"),
            new net.minecraft.util.Tuple<>("Cricket", "a24c69f96ce556221e195c8ef2bfad71ebf7f95f5ae914a484a8d0ec21672674"),
            new net.minecraft.util.Tuple<>("Earthworm", "6403ba4027a333d8d2fd32ab59d1cfdbaa7d908d80d2381db2a69cbe65450ad8"),
            new net.minecraft.util.Tuple<>("Fly", "9d90e777826a52461368e26d1b2e19bfa1ba582d602483e545f4124d0f731842"),
            new net.minecraft.util.Tuple<>("Locust", "4b274a482a32db1ea78fb98060b0c2fa4a373cbd18a68eddddeea7419455a59cda9"),
            new net.minecraft.util.Tuple<>("Mite", "be6baf6431a9daa2ca604d5a3c26e9a761d5952f0817174a4fe0b764616e21ff"),
            new net.minecraft.util.Tuple<>("Mosquito", "52a9fe05bc663efcd12e56a3ccc5ec035bf577b78708548b6f4ffcf1d30eccfe"),
            new net.minecraft.util.Tuple<>("Moth", "65485c4b34e5b5470be94de100e61f7816f81bc5a11dfdf0eccf890172da5d0a"),
            new net.minecraft.util.Tuple<>("Rat", "a8abb471db0ab78703011979dc8b40798a941f3a4dec3ec61cbeec2af8cffe8"),
            new net.minecraft.util.Tuple<>("Slug", "7a79d0fd677b54530961117ef84adc206e2cc5045c1344d61d776bf8ac2fe1ba"),
            new net.minecraft.util.Tuple<>("Praying Mantis", "1e04bb6367caa4e88f5fd0ee80f0745d137a6060223dbbc42a16471fdf64bb83"),
            new net.minecraft.util.Tuple<>("Firefly", "4ce79e90adf34718f313ec24d6c6135b69b3788c618498446ccc83ca640cb14"),
            new net.minecraft.util.Tuple<>("Dragonfly", "254aff4c0b2dce3a672349cc0ee9e6f3a9deebe4b3556e84611eca250a7821bf")
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
                EntityArmorStand stand = (EntityArmorStand) entity;
                // 1. Check skull texture in helmet slot (slot 4)
                ItemStack helm = stand.getEquipmentInSlot(4);
                if (helm != null && helm.hasTagCompound()) {
                    String tagStr = helm.getTagCompound().toString();
                    for (net.minecraft.util.Tuple<String, String> p : PEST_TEXTURES) {
                        if (tagStr.contains(p.getSecond())) {
                            isPest = true;
                            detectedType = p.getFirst();
                            break;
                        }
                    }
                }

                // 2. Check custom name tag if not already identified
                if (!isPest && entityName != null && !entityName.isEmpty()) {
                    for (String pestName : KNOWN_PEST_NAMES) {
                        if (nameLower.contains(pestName)) {
                            isPest = true;
                            detectedType = capitalize(pestName);
                            break;
                        }
                    }
                    if (!isPest && (entityName.contains("ൠ") || entityName.contains("Pest"))) {
                        isPest = true;
                        detectedType = "Pest";
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
                Entity targetEntity = entity;
                if (entity instanceof EntityArmorStand) {
                    Entity realEntity = com.jelly.farmhelperv2.util.PlayerUtils.getEntityCuttingOtherEntity(
                            entity,
                            (e) -> !(e instanceof EntityArmorStand)
                    );
                    if (realEntity != null) {
                        targetEntity = realEntity;
                    }
                }

                BlockPos pos = new BlockPos(targetEntity.posX, targetEntity.posY, targetEntity.posZ);
                PlotUtils.Plot plot = PlotUtils.getPlotNumberBasedOnLocation(pos);
                int plotNum = plot != null && plot.number != null ? plot.number : -1;
                double realDist = mc.thePlayer.getDistanceToEntity(targetEntity);

                // Deduplicate entities within 1.5 blocks of each other (e.g. name tag + skull stand)
                boolean alreadyAdded = false;
                for (PestInfo pi : detected) {
                    if (pi.getEntity().equals(targetEntity) ||
                            (Math.abs(pi.getLocation().getX() - pos.getX()) <= 1 &&
                             Math.abs(pi.getLocation().getY() - pos.getY()) <= 2 &&
                             Math.abs(pi.getLocation().getZ() - pos.getZ()) <= 1)) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (alreadyAdded) continue;

                PestInfo info = new PestInfo(
                        targetEntity,
                        detectedType,
                        pos,
                        realDist,
                        targetEntity.isEntityAlive(),
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
        // Built-in keywords
        if (name.contains("vacuum") || name.contains("hooverius") || name.contains("pest") || name.contains("destroyer")) {
            return true;
        }
        // User-configured extra keyword for FakePixel custom items
        String extra = FarmHelperConfig.fakePixelVacuumItemName.toLowerCase(Locale.ENGLISH).trim();
        return !extra.isEmpty() && name.contains(extra);
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
