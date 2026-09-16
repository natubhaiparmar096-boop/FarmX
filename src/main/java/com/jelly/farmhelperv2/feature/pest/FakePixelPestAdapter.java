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

    // Full base64 skin values (same as PestsDestroyer) — these appear inside getTagCompound().toString()
    public static final List<net.minecraft.util.Tuple<String, String>> PEST_TEXTURES = Arrays.asList(
            new net.minecraft.util.Tuple<>("Beetle",
                    "ewogICJ0aW1lc3RhbXAiIDogMTcyMzE3OTc4OTkzNCwKICAicHJvZmlsZUlkIiA6ICJlMjc5NjliODYyNWY0NDg1YjkyNmM5NTBhMDljMWMwMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLRVZJTktFTE9LRSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83MGExZTgzNmJmMTk2OGIyZWFhNDgzNzIyN2ExOTIwNGYxNzI5NWQ4NzBlZTllNzU0YmQ2YjZkNjBkZGJlZDNjIgogICAgfQogIH0KfQ"),
            new net.minecraft.util.Tuple<>("Cricket",
                    "ewogICJ0aW1lc3RhbXAiIDogMTcyMzE3OTgxMTI2NCwKICAicHJvZmlsZUlkIiA6ICJjZjc4YzFkZjE3ZTI0Y2Q5YTIxYmU4NWQ0NDk5ZWE4ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNYXR0c0FybW9yU3RhbmRzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2EyNGM2OWY5NmNlNTU2MjIxZTE5NWM4ZWYyYmZhZDcxZWJmN2Y5NWY1YWU5MTRhNDg0YThkMGVjMjE2NzI2NzQiCiAgICB9CiAgfQp9"),
            new net.minecraft.util.Tuple<>("Earthworm",
                    "ewogICJ0aW1lc3RhbXAiIDogMTY5NzQ3MDQ1OTc0NywKICAicHJvZmlsZUlkIiA6ICIyNTBlNzc5MjZkNDM0ZDIyYWM2MTQ4N2EyY2M3YzAwNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJMdW5hMTIxMDUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjQwM2JhNDAyN2EzMzNkOGQyZmQzMmFiNTlkMWNmZGJhYTdkOTA4ZDgwZDIzODFkYjJhNjljYmU2NTQ1MGFkOCIKICAgIH0KICB9Cn0"),
            new net.minecraft.util.Tuple<>("Fly",
                    "ewogICJ0aW1lc3RhbXAiIDogMTY5Njk0NTA2MzI4MSwKICAicHJvZmlsZUlkIiA6ICJjN2FmMWNkNjNiNTE0Y2YzOGY4NWQ2ZDUxNzhjYThlNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJtb25zdGVyZ2FtZXIzMTUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ5MGU3Nzc4MjZhNTI0NjEzNjhlMjZkMWIyZTE5YmZhMWJhNTgyZDYwMjQ4M2U1NDVmNDEyNGQwZjczMTg0MiIKICAgIH0KICB9Cn0"),
            new net.minecraft.util.Tuple<>("Locust",
                    "ewogICJ0aW1lc3RhbXAiIDogMTY5NzU1NzA3NzAzNywKICAicHJvZmlsZUlkIiA6ICI0YjJlMGM1ODliZjU0ZTk1OWM1ZmJlMzg5MjQ1MzQzZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJfTmVvdHJvbl8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGIyNGE0ODJhMzJkYjFlYTc4ZmI5ODA2MGIwYzJmYTRhMzczY2JkMThhNjhlZGRkZWI3NDE5NDU1YTU5Y2RhOSIKICAgIH0KICB9Cn0"),
            new net.minecraft.util.Tuple<>("Mite",
                    "ewogICJ0aW1lc3RhbXAiIDogMTY5Njg3MDQxOTcyNSwKICAicHJvZmlsZUlkIiA6ICJkYjYzNWE3MWI4N2U0MzQ5YThhYTgwOTMwOWFhODA3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbmdlbHMxNzQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmU2YmFmNjQzMWE5ZGFhMmNhNjA0ZDVhM2MyNmU5YTc2MWQ1OTUyZjA4MTcxNzRhNGZlMGI3NjQ2MTZlMjFmZiIKICAgIH0KICB9Cn0"),
            new net.minecraft.util.Tuple<>("Mosquito",
                    "ewogICJ0aW1lc3RhbXAiIDogMTY5Njk0NTAyOTQ2MSwKICAicHJvZmlsZUlkIiA6ICI3NTE0NDQ4MTkxZTY0NTQ2OGM5NzM5YTZlMzk1N2JlYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGFua3NNb2phbmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTJhOWZlMDViYzY2M2VmY2QxMmU1NmEzY2NjNWVjMDM1YmY1NzdiNzg3MDg1NDhiNmY0ZmZjZjFkMzBlY2NmZSIKICAgIH0KICB9Cn0"),
            new net.minecraft.util.Tuple<>("Moth",
                    "ewogICJ0aW1lc3RhbXAiIDogMTY5Njg3MDQwNTk1NCwKICAicHJvZmlsZUlkIiA6ICJiMTUyZDlhZTE1MTM0OWNmOWM2NmI0Y2RjMTA5NTZjOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaXNxdW90aCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82NTQ4NWM0YjM0ZTViNTQ3MGJlOTRkZTEwMGU2MWY3ODE2ZjgxYmM1YTExZGZkZjBlY2NmODkwMTcyZGE1ZDBhIgogICAgfQogIH0KfQ"),
            new net.minecraft.util.Tuple<>("Rat",
                    "ewogICJ0aW1lc3RhbXAiIDogMTYxODQxOTcwMTc1MywKICAicHJvZmlsZUlkIiA6ICI3MzgyZGRmYmU0ODU0NTVjODI1ZjkwMGY4OGZkMzJmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJCdUlJZXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYThhYmI0NzFkYjBhYjc4NzAzMDExOTc5ZGM4YjQwNzk4YTk0MWYzYTRkZWMzZWM2MWNiZWVjMmFmOGNmZmU4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="),
            new net.minecraft.util.Tuple<>("Slug",
                    "ewogICJ0aW1lc3RhbXAiIDogMTY5NzQ3MDQ0MzA4MiwKICAicHJvZmlsZUlkIiA6ICJkOGNkMTNjZGRmNGU0Y2IzODJmYWZiYWIwOGIyNzQ4OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJaYWNoeVphY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E3OWQwZmQ2NzdiNTQ1MzA5NjExMTdlZjg0YWRjMjA2ZTJjYzUwNDVjMTM0NGQ2MWQ3NzZiZjhhYzJmZTFiYSIKICAgIH0KICB9Cn0"),
            new net.minecraft.util.Tuple<>("Praying Mantis",
                    "ewogICJ0aW1lc3RhbXAiIDogMTc2MDQ1MDQxOTYxMiwKICAicHJvZmlsZUlkIiA6ICI0OWIzODUyNDdhMWY0NTM3YjBmN2MwZTFmMTVjMTc2NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJiY2QyMDMzYzYzZWM0YmY4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFlMDRiYjYzNjdjYWE0ZTg4ZjVmZDBlZTgwZjA3NDVkMTM3YTYwNjAyMjNkYmJjNDJhMTY0NzFmZGY2NGJiODMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="),
            new net.minecraft.util.Tuple<>("Firefly",
                    "ewogICJ0aW1lc3RhbXAiIDogMTc2MDQ1MDQyMjEzNiwKICAicHJvZmlsZUlkIiA6ICIzNDY4Y2VjMWFlOTY0YWRmYWQyNjEzMGEwZGQ0NjRkYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJzdXJlZWxta18iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNlNzllOTBhZGYzNDcxOGYzMTNlYzI0ZDZjNjEzNWI2OWIzNzg4YzYxODQ5ODQ0NmNjYzgzY2E2NDBjMGIxNCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"),
            new net.minecraft.util.Tuple<>("Dragonfly",
                    "ewogICJ0aW1lc3RhbXAiIDogMTc2MDQ1MDQxODQzNywKICAicHJvZmlsZUlkIiA6ICIwNjY5Y2E1MGYyZWU0NTQxODhlYWQ3YTM3NTkzNDRlMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDcjR6eWNsb3duVFYiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjU0YWZmNGMwYjJkY2UzYTY3MjM0OWNjMGVlOWU2ZjNhOWRlZWJlNGIzNTU2ZTg0NjExZWNhMjUwYTc4MjFiZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9")
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
        // Check all loaded entities across the entire garden (up to 256 blocks)
        double maxDist = Math.max(FarmHelperConfig.pestMaxDetectionDistance, 256.0);

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == null || entity.isDead) continue;

            double dist = mc.thePlayer.getDistanceToEntity(entity);
            if (dist > maxDist) continue;

            String entityName = entity.getCustomNameTag();
            String cleanName = entityName != null ? net.minecraft.util.StringUtils.stripControlCodes(entityName).toLowerCase(Locale.ENGLISH) : "";

            boolean isPest = false;
            String detectedType = "Pest";

            // Check if PestsDestroyer already identified this entity
            if (com.jelly.farmhelperv2.feature.impl.PestsDestroyer.getInstance().getPestsLocations().contains(entity)) {
                isPest = true;
            }

            if (!isPest && entity instanceof EntityArmorStand) {
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
                if (!isPest && !cleanName.isEmpty()) {
                    for (String pestName : KNOWN_PEST_NAMES) {
                        if (cleanName.contains(pestName)) {
                            isPest = true;
                            detectedType = capitalize(pestName);
                            break;
                        }
                    }
                    if (!isPest && (cleanName.contains("ൠ") || cleanName.contains("pest"))) {
                        isPest = true;
                        detectedType = "Pest";
                    }
                }
            } else if (!isPest) {
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
                    if (realEntity != null && !realEntity.isDead) {
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
                        !targetEntity.isDead,
                        now,
                        plotNum
                );
                detected.add(info);
            }
        }
        if (FarmHelperConfig.pestDebugLogging) {
            com.jelly.farmhelperv2.util.LogUtils.sendDebug("[Pest Adapter] Scanned " + mc.theWorld.loadedEntityList.size() + " loaded entities, found " + detected.size() + " pests.");
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
        if (mc.thePlayer == null) return -1;
        // 1. Check current held item first
        ItemStack current = mc.thePlayer.getHeldItem();
        if (isPestVacuumItem(current)) {
            return mc.thePlayer.inventory.currentItem;
        }
        // 2. Scan hotbar slots 0 to 8
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (isPestVacuumItem(stack)) {
                return i;
            }
        }
        // 3. Fallback: check slot 3 (hotbar key 4) if it contains an item
        ItemStack slot3 = mc.thePlayer.inventory.getStackInSlot(3);
        if (slot3 != null && slot3.getItem() != null) {
            String name = net.minecraft.util.StringUtils.stripControlCodes(slot3.getDisplayName()).toLowerCase(Locale.ENGLISH);
            if (name.contains("vacuum") || name.contains("infini") || name.contains("skymart") || name.contains("pest") || name.contains("destroyer")) {
                return 3;
            }
        }
        return -1;
    }

    @Override
    public boolean isPestVacuumItem(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        String name = net.minecraft.util.StringUtils.stripControlCodes(itemStack.getDisplayName()).toLowerCase(Locale.ENGLISH);
        // Built-in keywords
        if (name.contains("vacuum") || name.contains("hooverius") || name.contains("pest") || name.contains("destroyer")
                || name.contains("skymart") || name.contains("infini")) {
            return true;
        }
        // Check unlocalized / registry name
        if (itemStack.getItem() != null) {
            String unloc = itemStack.getItem().getUnlocalizedName().toLowerCase(Locale.ENGLISH);
            if (unloc.contains("vacuum") || unloc.contains("pest")) return true;
        }
        // Check full NBT tag (lore, extra attributes ID, etc.)
        if (itemStack.hasTagCompound()) {
            String tag = itemStack.getTagCompound().toString().toLowerCase(Locale.ENGLISH);
            if (tag.contains("vacuum") || tag.contains("hooverius") || tag.contains("pest")) {
                return true;
            }
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
