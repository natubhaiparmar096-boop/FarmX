package com.jelly.farmhelperv2.config;

import com.google.gson.reflect.TypeToken;
import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.struct.FarmingProfile;
import com.jelly.farmhelperv2.util.LogUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {
    private static File profilesFile = null;

    private static File getProfilesFile() {
        if (profilesFile == null) {
            profilesFile = new File(net.minecraft.client.Minecraft.getMinecraft().mcDataDir, "config/farmhelper_profiles.json");
        }
        return profilesFile;
    }

    public static List<FarmingProfile> profiles = new ArrayList<>();
    public static int activeProfileIndex = 0;

    public static void loadProfiles() {
        profiles.clear();
        File f = getProfilesFile();
        if (f.exists()) {
            try (FileReader reader = new FileReader(f)) {
                List<FarmingProfile> loaded = FarmHelper.gson.fromJson(reader, new TypeToken<List<FarmingProfile>>() {}.getType());
                if (loaded != null && !loaded.isEmpty()) {
                    profiles.addAll(loaded);
                }
            } catch (Exception e) {
                LogUtils.sendError("Failed to load farming profiles: " + e.getMessage());
            }
        }
        if (profiles.isEmpty()) {
            initDefaultProfiles();
            saveProfiles();
        }
    }

    private static void initDefaultProfiles() {
        FarmingProfile p1 = new FarmingProfile();
        p1.setName("Sugarcane D+S (Continuous)");
        p1.setMacroType(4); // S_SUGAR_CANE
        p1.setSugarcaneControlMode(2); // Two-key
        p1.setSugarcaneGoKey(3); // D
        p1.setSugarcaneReturnKey(1); // S
        p1.setSugarcaneStartOnGoLeg(true);

        FarmingProfile p2 = new FarmingProfile();
        p2.setName("Melon / Pumpkin 45 deg");
        p2.setMacroType(1); // S_PUMPKIN_MELON
        p2.setCustomPitch(true);
        p2.setCustomPitchLevel(50f);
        p2.setCustomYaw(true);
        p2.setCustomYawLevel(45f);

        FarmingProfile p3 = new FarmingProfile();
        p3.setName("Vertical Crop 0 deg");
        p3.setMacroType(0); // S_V_NORMAL_TYPE
        p3.setCustomPitch(true);
        p3.setCustomPitchLevel(0f);
        p3.setCustomYaw(true);
        p3.setCustomYawLevel(0f);

        profiles.add(p1);
        profiles.add(p2);
        profiles.add(p3);
    }

    public static void saveProfiles() {
        try {
            File f = getProfilesFile();
            if (f.getParentFile() != null && !f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            if (!f.exists()) {
                Files.createFile(f.toPath());
            }
            Files.write(f.toPath(), FarmHelper.gson.toJson(profiles).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LogUtils.sendError("Failed to save profiles: " + e.getMessage());
        }
    }

    public static void applyProfile(int index) {
        if (profiles.isEmpty()) loadProfiles();
        if (index < 0 || index >= profiles.size()) index = 0;
        activeProfileIndex = index;
        FarmingProfile p = profiles.get(index);

        FarmHelperConfig.macroType = p.getMacroType();
        FarmHelperConfig.sugarcaneControlMode = p.getSugarcaneControlMode();
        FarmHelperConfig.sugarcaneClassicRowKey = p.getSugarcaneClassicRowKey();
        FarmHelperConfig.sugarcaneClassicLaneLeftKey = p.getSugarcaneClassicLaneLeftKey();
        FarmHelperConfig.sugarcaneClassicLaneRightKey = p.getSugarcaneClassicLaneRightKey();
        FarmHelperConfig.sugarcaneGoKey = p.getSugarcaneGoKey();
        FarmHelperConfig.sugarcaneReturnKey = p.getSugarcaneReturnKey();
        FarmHelperConfig.sugarcaneLaneKey = p.getSugarcaneLaneKey();
        FarmHelperConfig.sugarcaneStartOnGoLeg = p.isSugarcaneStartOnGoLeg();
        FarmHelperConfig.sugarcaneInvertLaneSide = p.isSugarcaneInvertLaneSide();
        FarmHelperConfig.customPitch = p.isCustomPitch();
        FarmHelperConfig.customPitchLevel = p.getCustomPitchLevel();
        FarmHelperConfig.customYaw = p.isCustomYaw();
        FarmHelperConfig.customYawLevel = p.getCustomYawLevel();
        FarmHelperConfig.fastBreakSpeed = p.getFastBreakSpeed();

        if (FarmHelper.config != null) {
            FarmHelper.config.save();
        }
        LogUtils.sendSuccess("Applied profile: " + p.getName());
    }

    public static void saveCurrentAsProfile(String name) {
        FarmingProfile p = new FarmingProfile();
        p.setName(name);
        p.setMacroType(FarmHelperConfig.macroType);
        p.setSugarcaneControlMode(FarmHelperConfig.sugarcaneControlMode);
        p.setSugarcaneClassicRowKey(FarmHelperConfig.sugarcaneClassicRowKey);
        p.setSugarcaneClassicLaneLeftKey(FarmHelperConfig.sugarcaneClassicLaneLeftKey);
        p.setSugarcaneClassicLaneRightKey(FarmHelperConfig.sugarcaneClassicLaneRightKey);
        p.setSugarcaneGoKey(FarmHelperConfig.sugarcaneGoKey);
        p.setSugarcaneReturnKey(FarmHelperConfig.sugarcaneReturnKey);
        p.setSugarcaneLaneKey(FarmHelperConfig.sugarcaneLaneKey);
        p.setSugarcaneStartOnGoLeg(FarmHelperConfig.sugarcaneStartOnGoLeg);
        p.setSugarcaneInvertLaneSide(FarmHelperConfig.sugarcaneInvertLaneSide);
        p.setCustomPitch(FarmHelperConfig.customPitch);
        p.setCustomPitchLevel(FarmHelperConfig.customPitchLevel);
        p.setCustomYaw(FarmHelperConfig.customYaw);
        p.setCustomYawLevel(FarmHelperConfig.customYawLevel);
        p.setFastBreakSpeed(FarmHelperConfig.fastBreakSpeed);

        int existingIndex = -1;
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getName().equalsIgnoreCase(name)) {
                existingIndex = i;
                break;
            }
        }
        if (existingIndex >= 0) {
            profiles.set(existingIndex, p);
            activeProfileIndex = existingIndex;
        } else {
            profiles.add(p);
            activeProfileIndex = profiles.size() - 1;
        }
        saveProfiles();
        LogUtils.sendSuccess("Saved profile: " + name);
    }

    public static boolean deleteProfile(String name) {
        if (profiles.isEmpty()) loadProfiles();
        int target = -1;
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getName().equalsIgnoreCase(name)) {
                target = i;
                break;
            }
        }
        if (target >= 0) {
            profiles.remove(target);
            if (activeProfileIndex >= profiles.size()) {
                activeProfileIndex = Math.max(0, profiles.size() - 1);
            }
            saveProfiles();
            return true;
        }
        return false;
    }

    public static void deleteActiveProfile() {
        if (profiles.isEmpty()) loadProfiles();
        if (profiles.size() <= 1) {
            LogUtils.sendError("Cannot delete the only remaining profile!");
            return;
        }
        String name = getActiveProfileName();
        if (deleteProfile(name)) {
            applyProfile(activeProfileIndex);
            LogUtils.sendSuccess("Deleted profile: " + name);
        }
    }

    public static void cycleProfile() {
        if (profiles.isEmpty()) loadProfiles();
        activeProfileIndex = (activeProfileIndex + 1) % profiles.size();
        applyProfile(activeProfileIndex);
    }

    public static String getActiveProfileName() {
        if (profiles.isEmpty()) loadProfiles();
        if (activeProfileIndex < 0 || activeProfileIndex >= profiles.size()) activeProfileIndex = 0;
        return profiles.get(activeProfileIndex).getName();
    }
}
