package com.jelly.farmhelperv2.gui;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class WelcomeGUI extends GuiScreen {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final int CLOSE_BUTTON_ID = 1;

    public static void showGUI() {
        if (!FarmHelperConfig.shownWelcomeGUI2) {
            mc.displayGuiScreen(new WelcomeGUI());
        }
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(CLOSE_BUTTON_ID, this.width / 2 - 75, this.height - 40, 150, 20, "Continue"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "FarmX", this.width / 2, 40, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Open the Farm Helper config to set up farming.", this.width / 2, this.height / 2 - 10, 0xAAAAAA);
        this.drawCenteredString(this.fontRendererObj, "Use the toggle keybind to start/stop the macro.", this.width / 2, this.height / 2 + 6, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == CLOSE_BUTTON_ID) {
            FarmHelperConfig.shownWelcomeGUI2 = true;
            mc.displayGuiScreen(new GuiMainMenu());
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // Block Escape until Continue is clicked
    }
}
