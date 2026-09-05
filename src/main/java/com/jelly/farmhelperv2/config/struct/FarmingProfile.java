package com.jelly.farmhelperv2.config.struct;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FarmingProfile {
    private String name = "Default";
    private int macroType = 0;
    private int sugarcaneControlMode = 0;
    private int sugarcaneClassicRowKey = 1;
    private int sugarcaneClassicLaneLeftKey = 2;
    private int sugarcaneClassicLaneRightKey = 3;
    private int sugarcaneGoKey = 3;
    private int sugarcaneReturnKey = 1;
    private int sugarcaneLaneKey = 1;
    private boolean sugarcaneStartOnGoLeg = true;
    private boolean sugarcaneInvertLaneSide = false;
    private boolean customPitch = false;
    private float customPitchLevel = 0f;
    private boolean customYaw = false;
    private float customYawLevel = 0f;
    private int fastBreakSpeed = 1;

    public FarmingProfile(String name) {
        this.name = name;
    }
}
