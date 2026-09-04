package com.jelly.farmhelperv2.command;

import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.util.LogUtils;
import com.jelly.farmhelperv2.util.PlayerUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpawnCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "fhspawn";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/fhspawn [set|reset|info]";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("farmxspawn", "fhsetspawn");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1) {
            LogUtils.sendError("Invalid arguments. Use " + getCommandUsage(sender));
            return;
        }
        switch (args[0].toLowerCase()) {
            case "set":
                PlayerUtils.setSpawnLocation();
                if (PlayerUtils.isSpawnLocationSet()) {
                    LogUtils.sendSuccess("Spawn set to " + FarmHelperConfig.spawnPosX + ", "
                            + FarmHelperConfig.spawnPosY + ", " + FarmHelperConfig.spawnPosZ
                            + " (yaw " + (int) FarmHelperConfig.spawnYaw + ")");
                } else {
                    LogUtils.sendError("Could not set spawn (are you in a world?)");
                }
                break;
            case "reset":
                FarmHelperConfig.spawnPosX = 0;
                FarmHelperConfig.spawnPosY = 0;
                FarmHelperConfig.spawnPosZ = 0;
                FarmHelperConfig.spawnYaw = 0;
                FarmHelperConfig.spawnPitch = 0;
                FarmHelperConfig.spawnPlot = 0;
                if (FarmHelper.config != null) {
                    FarmHelper.config.save();
                }
                LogUtils.sendSuccess("Spawn position has been reset!");
                break;
            case "info":
                if (!PlayerUtils.isSpawnLocationSet()) {
                    LogUtils.sendWarning("Spawn is not set.");
                } else {
                    LogUtils.sendSuccess("Spawn: " + FarmHelperConfig.spawnPosX + ", "
                            + FarmHelperConfig.spawnPosY + ", " + FarmHelperConfig.spawnPosZ
                            + " | yaw=" + FarmHelperConfig.spawnYaw
                            + " pitch=" + FarmHelperConfig.spawnPitch
                            + " plot=" + FarmHelperConfig.spawnPlot);
                }
                break;
            default:
                LogUtils.sendError("Invalid argument. Use " + getCommandUsage(sender));
                break;
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "set", "reset", "info");
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
