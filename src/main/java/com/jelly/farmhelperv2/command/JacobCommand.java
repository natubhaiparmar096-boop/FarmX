package com.jelly.farmhelperv2.command;

import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.hud.JacobsContestHUD;
import com.jelly.farmhelperv2.util.LogUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JacobCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "fhjacob";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/fhjacob [toggle|info]";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("jacob", "fhcontest");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("toggle")) {
            FarmHelperConfig.showJacobsContestHud = !FarmHelperConfig.showJacobsContestHud;
            if (FarmHelper.config != null) FarmHelper.config.save();
            LogUtils.sendSuccess("Jacob's Contest HUD: " + (FarmHelperConfig.showJacobsContestHud ? "ON" : "OFF"));
            return;
        }

        LogUtils.sendSuccess("--- Jacob's Contest Status ---");
        for (String line : JacobsContestHUD.getInstance().buildHudLines()) {
            LogUtils.sendSuccess(line);
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "toggle", "info");
        }
        return new ArrayList<>();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(@NotNull ICommand o) {
        return 0;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return -1;
    }
}
