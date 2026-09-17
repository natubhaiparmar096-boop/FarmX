package com.jelly.farmhelperv2.config.struct;

import com.google.gson.annotations.Expose;

public class FarmingProfile {
    @Expose private String name = "Default";
    @Expose private int macroType = 0;
    @Expose private int sugarcaneControlMode = 0;
    @Expose private int sugarcaneClassicRowKey = 1;
    @Expose private int sugarcaneClassicLaneLeftKey = 2;
    @Expose private int sugarcaneClassicLaneRightKey = 3;
    @Expose private int sugarcaneGoKey = 0;
    @Expose private int sugarcaneReturnKey = 3;
    @Expose private int sugarcaneLaneKey = 1;
    @Expose private boolean sugarcaneStartOnGoLeg = true;
    @Expose private boolean sugarcaneInvertLaneSide = false;
    @Expose private boolean customPitch = false;
    @Expose private float customPitchLevel = 0f;
    @Expose private boolean customYaw = false;
    @Expose private float customYawLevel = 0f;
    @Expose private int fastBreakSpeed = 1;

    public FarmingProfile() {}

    public FarmingProfile(String name) {
        this.name = name;
    }

    public FarmingProfile(String name, int macroType, int sugarcaneControlMode, int sugarcaneClassicRowKey, int sugarcaneClassicLaneLeftKey, int sugarcaneClassicLaneRightKey, int sugarcaneGoKey, int sugarcaneReturnKey, int sugarcaneLaneKey, boolean sugarcaneStartOnGoLeg, boolean sugarcaneInvertLaneSide, boolean customPitch, float customPitchLevel, boolean customYaw, float customYawLevel, int fastBreakSpeed) {
        this.name = name;
        this.macroType = macroType;
        this.sugarcaneControlMode = sugarcaneControlMode;
        this.sugarcaneClassicRowKey = sugarcaneClassicRowKey;
        this.sugarcaneClassicLaneLeftKey = sugarcaneClassicLaneLeftKey;
        this.sugarcaneClassicLaneRightKey = sugarcaneClassicLaneRightKey;
        this.sugarcaneGoKey = sugarcaneGoKey;
        this.sugarcaneReturnKey = sugarcaneReturnKey;
        this.sugarcaneLaneKey = sugarcaneLaneKey;
        this.sugarcaneStartOnGoLeg = sugarcaneStartOnGoLeg;
        this.sugarcaneInvertLaneSide = sugarcaneInvertLaneSide;
        this.customPitch = customPitch;
        this.customPitchLevel = customPitchLevel;
        this.customYaw = customYaw;
        this.customYawLevel = customYawLevel;
        this.fastBreakSpeed = fastBreakSpeed;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMacroType() { return macroType; }
    public void setMacroType(int macroType) { this.macroType = macroType; }
    public int getSugarcaneControlMode() { return sugarcaneControlMode; }
    public void setSugarcaneControlMode(int sugarcaneControlMode) { this.sugarcaneControlMode = sugarcaneControlMode; }
    public int getSugarcaneClassicRowKey() { return sugarcaneClassicRowKey; }
    public void setSugarcaneClassicRowKey(int sugarcaneClassicRowKey) { this.sugarcaneClassicRowKey = sugarcaneClassicRowKey; }
    public int getSugarcaneClassicLaneLeftKey() { return sugarcaneClassicLaneLeftKey; }
    public void setSugarcaneClassicLaneLeftKey(int sugarcaneClassicLaneLeftKey) { this.sugarcaneClassicLaneLeftKey = sugarcaneClassicLaneLeftKey; }
    public int getSugarcaneClassicLaneRightKey() { return sugarcaneClassicLaneRightKey; }
    public void setSugarcaneClassicLaneRightKey(int sugarcaneClassicLaneRightKey) { this.sugarcaneClassicLaneRightKey = sugarcaneClassicLaneRightKey; }
    public int getSugarcaneGoKey() { return sugarcaneGoKey; }
    public void setSugarcaneGoKey(int sugarcaneGoKey) { this.sugarcaneGoKey = sugarcaneGoKey; }
    public int getSugarcaneReturnKey() { return sugarcaneReturnKey; }
    public void setSugarcaneReturnKey(int sugarcaneReturnKey) { this.sugarcaneReturnKey = sugarcaneReturnKey; }
    public int getSugarcaneLaneKey() { return sugarcaneLaneKey; }
    public void setSugarcaneLaneKey(int sugarcaneLaneKey) { this.sugarcaneLaneKey = sugarcaneLaneKey; }
    public boolean isSugarcaneStartOnGoLeg() { return sugarcaneStartOnGoLeg; }
    public void setSugarcaneStartOnGoLeg(boolean sugarcaneStartOnGoLeg) { this.sugarcaneStartOnGoLeg = sugarcaneStartOnGoLeg; }
    public boolean isSugarcaneInvertLaneSide() { return sugarcaneInvertLaneSide; }
    public void setSugarcaneInvertLaneSide(boolean sugarcaneInvertLaneSide) { this.sugarcaneInvertLaneSide = sugarcaneInvertLaneSide; }
    public boolean isCustomPitch() { return customPitch; }
    public void setCustomPitch(boolean customPitch) { this.customPitch = customPitch; }
    public float getCustomPitchLevel() { return customPitchLevel; }
    public void setCustomPitchLevel(float customPitchLevel) { this.customPitchLevel = customPitchLevel; }
    public boolean isCustomYaw() { return customYaw; }
    public void setCustomYaw(boolean customYaw) { this.customYaw = customYaw; }
    public float getCustomYawLevel() { return customYawLevel; }
    public void setCustomYawLevel(float customYawLevel) { this.customYawLevel = customYawLevel; }
    public int getFastBreakSpeed() { return fastBreakSpeed; }
    public void setFastBreakSpeed(int fastBreakSpeed) { this.fastBreakSpeed = fastBreakSpeed; }
}
