package com.jelly.farmhelperv2.command;

import com.jelly.farmhelperv2.config.ProfileManager;
import com.jelly.farmhelperv2.config.struct.FarmingProfile;
import com.jelly.farmhelperv2.util.LogUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "fhprofile";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/fhprofile [list|load <name>|save <name>|delete <name>]";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("farmxprofile", "profile");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            ProfileManager.cycleProfile();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                LogUtils.sendSuccess("--- Saved Farming Profiles ---");
                for (int i = 0; i < ProfileManager.profiles.size(); i++) {
                    FarmingProfile p = ProfileManager.profiles.get(i);
                    boolean isActive = (i == ProfileManager.activeProfileIndex);
                    LogUtils.sendSuccess((isActive ? "-> " : "   ") + (i + 1) + ". " + p.getName());
                }
                break;
            case "load":
                if (args.length < 2) {
                    LogUtils.sendError("Usage: /fhprofile load <name>");
                    return;
                }
                String loadName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                int foundIndex = -1;
                try {
                    int parsed = Integer.parseInt(loadName) - 1;
                    if (parsed >= 0 && parsed < ProfileManager.profiles.size()) {
                        foundIndex = parsed;
                    }
                } catch (NumberFormatException ignored) {}

                if (foundIndex < 0) {
                    for (int i = 0; i < ProfileManager.profiles.size(); i++) {
                        if (ProfileManager.profiles.get(i).getName().equalsIgnoreCase(loadName)) {
                            foundIndex = i;
                            break;
                        }
                    }
                }

                if (foundIndex >= 0) {
                    ProfileManager.applyProfile(foundIndex);
                } else {
                    LogUtils.sendError("Profile not found: " + loadName);
                }
                break;
            case "save":
                if (args.length < 2) {
                    LogUtils.sendError("Usage: /fhprofile save <profile_name>");
                    return;
                }
                String saveName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                ProfileManager.saveCurrentAsProfile(saveName);
                break;
            case "delete":
                if (args.length < 2) {
                    LogUtils.sendError("Usage: /fhprofile delete <name>");
                    return;
                }
                String delName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                if (ProfileManager.deleteProfile(delName)) {
                    LogUtils.sendSuccess("Deleted profile: " + delName);
                } else {
                    LogUtils.sendError("Profile not found: " + delName);
                }
                break;
            default:
                LogUtils.sendError("Invalid action. Usage: " + getCommandUsage(sender));
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
            return getListOfStringsMatchingLastWord(args, "list", "load", "save", "delete");
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
