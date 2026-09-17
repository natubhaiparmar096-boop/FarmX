package com.jelly.farmhelperv2.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public class InventoryInputEvent extends Event {
    private final int keyCode;
    private final char typedChar;

    public int getKeyCode() { return keyCode; }
    public char getTypedChar() { return typedChar; }

    public InventoryInputEvent(int keyCode, char typedChar) {
        this.keyCode = keyCode;
        this.typedChar = typedChar;
    }
}
