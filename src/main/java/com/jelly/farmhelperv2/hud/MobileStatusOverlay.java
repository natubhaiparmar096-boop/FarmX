package com.jelly.farmhelperv2.hud;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.util.PlatformUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

/**
 * Vanilla font-renderer status lines for Android / GL4ES (no OneConfig NanoVG).
 */
public class MobileStatusOverlay {
    private static MobileStatusOverlay instance;

    public static MobileStatusOverlay getInstance() {
        if (instance == null) {
            instance = new MobileStatusOverlay();
        }
        return instance;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (!PlatformUtils.isMobile()) {
            return;
        }
        if (FarmHelperConfig.streamerMode) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        if (!FarmHelperConfig.showStatusHudOutsideGarden
                && !com.jelly.farmhelperv2.handler.GameStateHandler.getInstance().inGarden()) {
            return;
        }

        List<String> lines = FarmHelperConfig.statusHUD.getStatusString();
        FontRenderer fr = mc.fontRendererObj;
        int x = 4;
        int y = 4;
        for (String line : lines) {
            fr.drawStringWithShadow(line, x, y, 0xFFFFFF);
            y += fr.FONT_HEIGHT + 2;
        }
    }
}
