package com.jelly.farmhelperv2.command;

import com.jelly.farmhelperv2.handler.MacroHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import java.util.Arrays;
import java.util.List;

public class PauseCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "fhpause";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/fhpause";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("fhp", "farmhelperpause");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        MacroHandler.getInstance().togglePause();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
