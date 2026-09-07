package com.jelly.farmhelperv2.config.struct;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    public FarmingProfile(String name) {
        this.name = name;
    }
}
