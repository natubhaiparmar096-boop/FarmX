package com.jelly.farmhelperv2.command;

import com.jelly.farmhelperv2.FarmHelper;
import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.util.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RotationCommand extends CommandBase {
    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public String getCommandName() {
        return "fhrot";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/fhrot [set|info|pitch <n>|yaw <n>|togglepitch|toggleyaw]";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("farmxrot", "fhrotation");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            LogUtils.sendError("Invalid arguments. Use " + getCommandUsage(sender));
            return;
        }
        switch (args[0].toLowerCase()) {
            case "set":
                if (mc.thePlayer == null) {
                    LogUtils.sendError("Not in a world.");
                    return;
                }
                FarmHelperConfig.customPitchLevel = MathHelper.clamp_float(mc.thePlayer.rotationPitch, -90f, 90f);
                FarmHelperConfig.customYawLevel = normalizeYaw(mc.thePlayer.rotationYaw);
                FarmHelperConfig.customPitch = true;
                FarmHelperConfig.customYaw = true;
                save();
                LogUtils.sendSuccess(String.format("Custom rot from look: pitch %.1f, yaw %.1f (both ON)",
                        FarmHelperConfig.customPitchLevel, FarmHelperConfig.customYawLevel));
                break;
            case "info":
                LogUtils.sendSuccess(String.format("Pitch %s = %.1f | Yaw %s = %.1f",
                        FarmHelperConfig.customPitch ? "ON" : "OFF", FarmHelperConfig.customPitchLevel,
                        FarmHelperConfig.customYaw ? "ON" : "OFF", FarmHelperConfig.customYawLevel));
                break;
            case "pitch":
                if (args.length < 2) {
                    LogUtils.sendError("Usage: /fhrot pitch <number>");
                    return;
                }
                try {
                    FarmHelperConfig.customPitchLevel = MathHelper.clamp_float(Float.parseFloat(args[1]), -90f, 90f);
                    FarmHelperConfig.customPitch = true;
                    save();
                    LogUtils.sendSuccess("Custom pitch set to " + FarmHelperConfig.customPitchLevel + " (ON)");
                } catch (NumberFormatException e) {
                    LogUtils.sendError("Invalid pitch number.");
                }
                break;
            case "yaw":
                if (args.length < 2) {
                    LogUtils.sendError("Usage: /fhrot yaw <number>");
                    return;
                }
                try {
                    FarmHelperConfig.customYawLevel = normalizeYaw(Float.parseFloat(args[1]));
                    FarmHelperConfig.customYaw = true;
                    save();
                    LogUtils.sendSuccess("Custom yaw set to " + FarmHelperConfig.customYawLevel + " (ON)");
                } catch (NumberFormatException e) {
                    LogUtils.sendError("Invalid yaw number.");
                }
                break;
            case "togglepitch":
                FarmHelperConfig.customPitch = !FarmHelperConfig.customPitch;
                save();
                LogUtils.sendSuccess("Custom pitch: " + (FarmHelperConfig.customPitch ? "ON" : "OFF"));
                break;
            case "toggleyaw":
                FarmHelperConfig.customYaw = !FarmHelperConfig.customYaw;
                save();
                LogUtils.sendSuccess("Custom yaw: " + (FarmHelperConfig.customYaw ? "ON" : "OFF"));
                break;
            default:
                LogUtils.sendError("Invalid argument. Use " + getCommandUsage(sender));
                break;
        }
    }

    private void save() {
        if (FarmHelper.config != null) {
            FarmHelper.config.save();
        }
    }

    private static float normalizeYaw(float yaw) {
        yaw = yaw % 360f;
        if (yaw > 180f) yaw -= 360f;
        if (yaw < -180f) yaw += 360f;
        return yaw;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "set", "info", "pitch", "yaw", "togglepitch", "toggleyaw");
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
