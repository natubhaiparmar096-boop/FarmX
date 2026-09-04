package com.jelly.farmhelperv2.gui;

import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Vanilla settings screen for Android / GL4ES (Zalith). Avoids OneConfig NanoVG.
 */
public class FarmXMobileGui extends GuiScreen {
    private static final int ID_MACRO_TYPE = 1;
    private static final int ID_ALWAYS_W = 2;
    private static final int ID_FAILSAFE_SOUND = 3;
    private static final int ID_FAST_BREAK = 4;
    private static final int ID_ANTI_STUCK = 5;
    private static final int ID_SAVE_CLOSE = 6;

    private static final String[] MACRO_LABELS = {
            "S Vert Crops",
            "S Pumpkin/Melon",
            "S Melongkingde",
            "S Default Plot",
            "S Sugar Cane",
            "S Cactus",
            "S Cactus SunTzu",
            "S Cocoa",
            "S Cocoa Trapdoors",
            "S Cocoa L/R",
            "S Mushroom 45",
            "S Mushroom 30",
            "S Mushroom SDS",
            "Circle Crops"
    };

    @Override
    public void initGui() {
        this.buttonList.clear();
        int cx = this.width / 2 - 100;
        int y = 40;
        int gap = 24;

        this.buttonList.add(new GuiButton(ID_MACRO_TYPE, cx, y, 200, 20, macroTypeLabel()));
        y += gap;
        this.buttonList.add(new GuiButton(ID_ALWAYS_W, cx, y, 200, 20, toggleLabel("Always Hold W", FarmHelperConfig.alwaysHoldW)));
        y += gap;
        this.buttonList.add(new GuiButton(ID_FAILSAFE_SOUND, cx, y, 200, 20, toggleLabel("Failsafe Sound", FarmHelperConfig.enableFailsafeSound)));
        y += gap;
        this.buttonList.add(new GuiButton(ID_FAST_BREAK, cx, y, 200, 20, toggleLabel("Fast Break", FarmHelperConfig.fastBreak)));
        y += gap;
        this.buttonList.add(new GuiButton(ID_ANTI_STUCK, cx, y, 200, 20, toggleLabel("Anti Stuck", FarmHelperConfig.tmpAntiStuckEnabled)));
        y += gap + 8;
        this.buttonList.add(new GuiButton(ID_SAVE_CLOSE, cx, y, 200, 20, "Save & Close"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "FarmX (Mobile / GL4ES)", this.width / 2, 15, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Avoid OneConfig menu on this device", this.width / 2, this.height - 20, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case ID_MACRO_TYPE:
                FarmHelperConfig.macroType = (FarmHelperConfig.macroType + 1) % MACRO_LABELS.length;
                button.displayString = macroTypeLabel();
                break;
            case ID_ALWAYS_W:
                FarmHelperConfig.alwaysHoldW = !FarmHelperConfig.alwaysHoldW;
                button.displayString = toggleLabel("Always Hold W", FarmHelperConfig.alwaysHoldW);
                break;
            case ID_FAILSAFE_SOUND:
                FarmHelperConfig.enableFailsafeSound = !FarmHelperConfig.enableFailsafeSound;
                button.displayString = toggleLabel("Failsafe Sound", FarmHelperConfig.enableFailsafeSound);
                break;
            case ID_FAST_BREAK:
                FarmHelperConfig.fastBreak = !FarmHelperConfig.fastBreak;
                button.displayString = toggleLabel("Fast Break", FarmHelperConfig.fastBreak);
                break;
            case ID_ANTI_STUCK:
                FarmHelperConfig.tmpAntiStuckEnabled = !FarmHelperConfig.tmpAntiStuckEnabled;
                button.displayString = toggleLabel("Anti Stuck", FarmHelperConfig.tmpAntiStuckEnabled);
                break;
            case ID_SAVE_CLOSE:
                if (FarmHelper.config != null) {
                    FarmHelper.config.save();
                }
                this.mc.displayGuiScreen(null);
                if (this.mc.currentScreen == null) {
                    this.mc.setIngameFocus();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static String macroTypeLabel() {
        int idx = FarmHelperConfig.macroType;
        if (idx < 0 || idx >= MACRO_LABELS.length) {
            idx = 0;
        }
        return "Macro: " + MACRO_LABELS[idx];
    }

    private static String toggleLabel(String name, boolean on) {
        return name + ": " + (on ? "ON" : "OFF");
    }
}
