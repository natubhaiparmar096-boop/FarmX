package com.jelly.farmhelperv2.config.page;

import cc.polyfrost.oneconfig.config.annotations.Switch;

public class FailsafeNotificationsPage {
    @Switch(
            name = "Rotation Check Notifications",
            description = "Whether or not to send a notification when the rotation check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnRotationFailsafe = true;

    @Switch(
            name = "Teleportation Check Notifications",
            description = "Whether or not to send a notification when the teleportation check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnTeleportationFailsafe = true;

    @Switch(
            name = "Lag Back Notifications",
            description = "Whether or not to send a notification when the lag back failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnLagBackFailsafe = true;

    @Switch(
            name = "Knockback Check Notifications",
            description = "Whether or not to send a notification when the knockback check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnKnockbackFailsafe = true;

    @Switch(
            name = "Dirt Check Notifications",
            description = "Whether or not to send a notification when the dirt check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnDirtFailsafe = true;

    @Switch(
            name = "Cobweb Check Notifications",
            description = "Whether or not to send a notification when the cobweb check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnCobwebFailsafe = true;

    @Switch(
            name = "Item Change Check Notifications",
            description = "Whether or not to send a notification when the item change check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnItemChangeFailsafe = true;

    @Switch(
            name = "World Change Check Notifications",
            description = "Whether or not to send a notification when the world change check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnWorldChangeFailsafe = true;

    @Switch(
            name = "Bedrock Cage Check Notifications",
            description = "Whether or not to send a notification when the bedrock cage check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnBedrockCageFailsafe = true;

    @Switch(
            name = "Bad Effects Check Notifications",
            description = "Whether or not to send a notification when the bad effects check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnBadEffectsFailsafe = true;

    @Switch(
            name = "Disconnect Notifications",
            description = "Whether or not to send a notification when the disconnect failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnDisconnectFailsafe = true;

    @Switch(
            name = "Lower Average BPS Notifications",
            description = "Whether or not to send a notification when the average BPS is lower than the specified value.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnLowerAverageBPS = true;

    @Switch(
            name = "Full Inventory Notifications",
            description = "Whether or not to send a notification when your inventory is full.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnInventoryFull = true;

    @Switch(
            name = "Rotation Check Sound Alert",
            description = "Whether or not to play a sound when the rotation check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnRotationFailsafe = true;

    @Switch(
            name = "Teleportation Check Sound Alert",
            description = "Whether or not to play a sound when the teleportation check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnTeleportationFailsafe = true;

    @Switch(
            name = "Knockback Check Sound Alert",
            description = "Whether or not to play a sound when the knockback check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnKnockbackFailsafe = true;

    @Switch(
            name = "Dirt Check Sound Alert",
            description = "Whether or not to play a sound when the dirt check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnDirtFailsafe = true;

    @Switch(
            name = "Cobweb Check Sound Alert",
            description = "Whether or not to play a sound when the cobweb check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnCobwebFailsafe = true;

    @Switch(
            name = "Item Change Check Sound Alert",
            description = "Whether or not to play a sound when the item change check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnItemChangeFailsafe = true;

    @Switch(
            name = "World Change Check Sound Alert",
            description = "Whether or not to play a sound when the world change check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnWorldChangeFailsafe = false;

    @Switch(
            name = "Bedrock Cage Check Sound Alert",
            description = "Whether or not to play a sound when the bedrock cage check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnBedrockCageFailsafe = true;

    @Switch(
            name = "Bad Effects Check Sound Alert",
            description = "Whether or not to play a sound when the bad effects check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnBadEffectsFailsafe = true;

    @Switch(
            name = "Disconnect Alert",
            description = "Whether or not to play a sound when the disconnect failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnDisconnectFailsafe = false;

    @Switch(
            name = "Full Inventory Alert",
            description = "Whether or not to play a sound when your inventory is full.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnFullInventory = false;

    @Switch(
            name = "Lower Average BPS Alert",
            description = "Whether or not to play a sound when the average BPS is lower than the specified value.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnLowerAverageBPS = true;
}
