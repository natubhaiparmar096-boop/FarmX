package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class PestTargetTracker {
    private static final List<String> PEST_TEXTURE_FRAGMENTS = Arrays.asList(
            "70a1e836bf1968b2eaa4837227a19204f17295d870ee9e754bd6b6d60ddbed3c",
            "a24c69f96ce5562221e195c8ef2bfad71ebf7f95f5ae914a484a8d0ec21672674",
            "6403ba4027a333d8d2fd32ab59d1cfdbaa7d908d80d2381db2a69cbe65450ad8",
            "9d90e777826a52461368e26d1b2e19bfa1ba582d60248e545f4124d0f731842",
            "4b24a482a32db1ea78fb98060b0c2fa4a373cbd18a68edddeb7419455a59cda9",
            "be6baf6431a9daa2ca604d5a3c26e9a761d5952f0817174a4fe0b764616e21ff",
            "52a9fe05bc663efcd12e56a3ccc5ec035bf577b78708548b6f4ffcf1d30eccfe",
            "6545c4b34e5b5470be94de100e61f7816f81bc5a11dfdf0eccf890172da5d0a",
            "a8abb471db0ab78703011997dc8b40798a941f3a4dec3ec61cbeec2af8cffe8",
            "7a79d0fd677b54530961117ef84adc206e2cc5045c1344d61d776bf8ac2fe1ba",
            "1e04bb6367caa4e88f5fd0ee80f0745d137a604223dbbc42a16471fdf64bb83",
            "4ce69e90adf34718f313ec24d6c6135b69b3788c61849844666ccc83ca640c0b16",
            "254aff4c0b2dce3a672349cc0e99e6f3a9deebe4b3556e84611eca250a7821bf"
    );

    private static final String[] PEST_NAMES = {
            "fly", "cricket", "locust", "rat", "mosquito", "earthworm",
            "mite", "moth", "slug", "beetle", "firefly", "dragonfly", "mantis", "pest"
    };

    private PestTargetTracker() {}

    public static List<Entity> getLoadedPests() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return Collections.emptyList();

        List<Entity> list = new ArrayList<>();
        List<Entity> allEntities = mc.theWorld.loadedEntityList;

        List<Entity> livingPests = new ArrayList<>();
        for (Entity entity : allEntities) {
            if (entity == null || entity.isDead || entity == mc.thePlayer) continue;
            if (entity.posY < 50) continue;
            if (entity instanceof EntitySilverfish || entity instanceof EntityBat) {
                livingPests.add(entity);
            }
        }

        for (Entity entity : allEntities) {
            if (entity == null || entity.isDead || entity == mc.thePlayer) continue;
            if (entity.posY < 50) continue;

            if (entity instanceof EntitySilverfish || entity instanceof EntityBat) {
                list.add(entity);
            } else if (entity instanceof EntityArmorStand) {
                boolean nearLiving = livingPests.stream().anyMatch(lp -> lp.getDistanceSqToEntity(entity) <= 9.0);
                if (!nearLiving && isPestEntity(entity, allEntities)) {
                    list.add(entity);
                }
            }
        }
        return list;
    }

    public static boolean isPestEntity(Entity entity, List<Entity> allEntities) {
        if (entity == null || entity.isDead) return false;

        if (entity instanceof EntitySilverfish || entity instanceof EntityBat) {
            return true;
        }

        if (entity instanceof EntityArmorStand) {
            EntityArmorStand stand = (EntityArmorStand) entity;

            // Check custom name
            if (stand.hasCustomName()) {
                String name = StringUtils.stripControlCodes(stand.getCustomNameTag()).toLowerCase();
                for (String pestName : PEST_NAMES) {
                    if (name.contains(pestName)) {
                        return true;
                    }
                }
            }

            // Check helmet texture
            ItemStack head = stand.getCurrentArmor(3);
            if (head != null && head.getItem() == Items.skull) {
                if (hasKnownPestTexture(head)) {
                    return true;
                }
            }

            // Check if armor stand is riding or within 2 blocks of a silverfish / bat
            if (allEntities != null) {
                for (Entity other : allEntities) {
                    if ((other instanceof EntitySilverfish || other instanceof EntityBat) && !other.isDead) {
                        if (stand.getDistanceSqToEntity(other) <= 4.0) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static boolean hasKnownPestTexture(ItemStack head) {
        if (head == null || !head.hasTagCompound()) return false;
        NBTTagCompound tag = head.getTagCompound();
        if (!tag.hasKey("SkullOwner", 10)) return false;
        NBTTagCompound owner = tag.getCompoundTag("SkullOwner");
        if (!owner.hasKey("Properties", 10)) return false;
        NBTTagCompound props = owner.getCompoundTag("Properties");
        if (!props.hasKey("textures", 9)) return false;
        NBTTagList textures = props.getTagList("textures", 10);
        for (int i = 0; i < textures.tagCount(); i++) {
            NBTTagCompound tex = textures.getCompoundTagAt(i);
            if (tex.hasKey("Value", 8)) {
                String val = tex.getString("Value");
                try {
                    String decoded = new String(Base64.getDecoder().decode(val), StandardCharsets.UTF_8);
                    for (String fragment : PEST_TEXTURE_FRAGMENTS) {
                        if (decoded.contains(fragment)) {
                            return true;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return false;
    }

    public static Entity findClosestPest(Collection<Entity> excluded) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return null;

        List<Entity> pests = getLoadedPests();
        Entity closest = null;
        double bestDistSq = Double.MAX_VALUE;

        for (Entity pest : pests) {
            if (excluded != null && excluded.contains(pest)) continue;
            double distSq = mc.thePlayer.getDistanceSqToEntity(pest);
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                closest = pest;
            }
        }
        return closest;
    }

    public static List<Entity> buildOptimizedRoute(Collection<Entity> excluded) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return Collections.emptyList();

        List<Entity> remaining = new ArrayList<>(getLoadedPests());
        if (excluded != null) {
            remaining.removeAll(excluded);
        }

        List<Entity> route = new ArrayList<>(remaining.size());
        Vec3 cursor = mc.thePlayer.getPositionVector();

        while (!remaining.isEmpty()) {
            final Vec3 pos = cursor;
            Entity next = null;
            double minSq = Double.MAX_VALUE;
            for (Entity candidate : remaining) {
                double dSq = pos.squareDistanceTo(candidate.getPositionVector());
                if (dSq < minSq) {
                    minSq = dSq;
                    next = candidate;
                }
            }
            if (next == null) break;
            route.add(next);
            remaining.remove(next);
            cursor = next.getPositionVector();
        }
        return route;
    }
}
