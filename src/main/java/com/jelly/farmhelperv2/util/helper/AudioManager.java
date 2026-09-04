package com.jelly.farmhelperv2.util.helper;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AudioManager {
    private final Minecraft mc = Minecraft.getMinecraft();
    private static AudioManager instance;

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    @Getter
    private boolean minecraftSoundEnabled = false;

    private final Clock delayBetweenPings = new Clock();
    private int numSounds = 15;
    @Setter
    private float soundBeforeChange = 0;

    public void resetSound() {
        minecraftSoundEnabled = false;
        if (FarmHelperConfig.maxOutMinecraftSounds) {
            mc.gameSettings.setSoundLevel(SoundCategory.MASTER, soundBeforeChange);
        }
    }

    public void playSound() {
        if (minecraftSoundEnabled) return;
        numSounds = 15;
        minecraftSoundEnabled = true;
        if (FarmHelperConfig.maxOutMinecraftSounds) {
            mc.gameSettings.setSoundLevel(SoundCategory.MASTER, 1.0f);
        }
    }

    public boolean isSoundPlaying() {
        return minecraftSoundEnabled;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!minecraftSoundEnabled) return;
        if (delayBetweenPings.isScheduled() && !delayBetweenPings.passed()) return;
        if (numSounds <= 0) {
            minecraftSoundEnabled = false;
            if (FarmHelperConfig.maxOutMinecraftSounds) {
                mc.gameSettings.setSoundLevel(SoundCategory.MASTER, soundBeforeChange);
            }
            return;
        }

        String sound = FarmHelperConfig.failsafeMcSoundSelected == 0 ? "random.orb" : "random.anvil_land";
        mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, sound, 10.0F, 1.0F, false);
        delayBetweenPings.schedule(100);
        numSounds--;
    }
}
