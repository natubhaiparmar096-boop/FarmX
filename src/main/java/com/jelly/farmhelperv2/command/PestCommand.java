package com.jelly.farmhelperv2.command;

import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.feature.impl.pest.ManualPestManager;
import com.jelly.farmhelperv2.feature.impl.pest.PestDestroyer;
import com.jelly.farmhelperv2.feature.impl.pest.PestManager;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestExchangeManager;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestTabSnapshot;
import com.jelly.farmhelperv2.feature.impl.pest.helpers.PestTrapManager;
import com.jelly.farmhelperv2.hud.PestHUD;
import com.jelly.farmhelperv2.util.LogUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PestCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "pest";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/pest [start|stop|exchange|traps|toggle|status|threshold|ballsack]";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("pestdestroyer", "fhpest");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            LogUtils.sendSuccess("--- Pest Destroyer Status ---");
            for (String line : PestHUD.getInstance().buildHudLines()) {
                LogUtils.sendSuccess(line);
            }
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "start":
                ManualPestManager.triggerManualCleanup();
                break;
            case "stop":
                ManualPestManager.stop();
                break;
            case "exchange":
                PestExchangeManager.start();
                break;
            case "traps":
                PestTrapManager.start();
                break;
            case "toggle":
                FarmHelperConfig.enablePestDestroyer = !FarmHelperConfig.enablePestDestroyer;
                if (FarmHelper.config != null) FarmHelper.config.save();
                LogUtils.sendSuccess("Pest Destroyer: " + (FarmHelperConfig.enablePestDestroyer ? "§aENABLED" : "§cDISABLED"));
                break;
            case "threshold":
                if (args.length > 1) {
                    try {
                        int val = Integer.parseInt(args[1]);
                        FarmHelperConfig.pestThreshold = Math.max(1, Math.min(8, val));
                        if (FarmHelper.config != null) FarmHelper.config.save();
                        LogUtils.sendSuccess("Pest Threshold set to: " + FarmHelperConfig.pestThreshold);
                    } catch (NumberFormatException e) {
                        LogUtils.sendError("Invalid number: " + args[1]);
                    }
                } else {
                    LogUtils.sendSuccess("Current Pest Threshold: " + FarmHelperConfig.pestThreshold);
                }
                break;
            case "ballsack":
                if (args.length > 1) {
                    FarmHelperConfig.pestBallsackShredder = Boolean.parseBoolean(args[1]);
                } else {
                    FarmHelperConfig.pestBallsackShredder = !FarmHelperConfig.pestBallsackShredder;
                }
                if (FarmHelper.config != null) FarmHelper.config.save();
                LogUtils.sendSuccess("Ballsack Shredder: " + (FarmHelperConfig.pestBallsackShredder ? "§aENABLED" : "§cDISABLED"));
                break;
            default:
                LogUtils.sendError("Unknown subcommand: " + args[0]);
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
            return getListOfStringsMatchingLastWord(args, "start", "stop", "exchange", "traps", "toggle", "status", "threshold", "ballsack");
        }
        return new ArrayList<>();
    }
}
