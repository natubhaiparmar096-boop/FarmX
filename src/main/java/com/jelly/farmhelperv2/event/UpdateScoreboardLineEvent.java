package com.jelly.farmhelperv2.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public class UpdateScoreboardLineEvent extends Event {
    private final String line;

    public String getLine() { return line; }

    public UpdateScoreboardLineEvent(String line) {
        this.line = line;
    }
}
