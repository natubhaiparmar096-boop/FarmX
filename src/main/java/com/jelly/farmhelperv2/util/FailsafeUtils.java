package com.jelly.farmhelperv2.util;

import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Desktop failsafe notifications / window focus. Fully no-op on mobile (GL4ES / Zalith / Pojav).
 * Windows WinAPI is loaded only via reflection so Android class-loading never touches JNA.
 */
public class FailsafeUtils {
    private static FailsafeUtils instance;
    private final TrayIcon trayIcon;

    public FailsafeUtils() {
        if (PlatformUtils.isMobile() || !SystemUtils.IS_OS_WINDOWS) {
            trayIcon = null;
            return;
        }
        BufferedImage image;
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/farmhelper/icon-mod/icon.png")));
        } catch (IOException e) {
            trayIcon = null;
            return;
        }
        TrayIcon icon = new TrayIcon(image, "Farm Helper Failsafe Notification");
        icon.setImageAutoSize(true);
        icon.setToolTip("Farm Helper Failsafe Notification");
        try {
            if (SystemTray.isSupported()) {
                SystemTray.getSystemTray().add(icon);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            trayIcon = null;
            return;
        }
        trayIcon = icon;
    }

    public static FailsafeUtils getInstance() {
        if (instance == null) {
            instance = new FailsafeUtils();
        }
        return instance;
    }

    public static void bringWindowToFront() {
        if (PlatformUtils.isMobile()) {
            return;
        }
        if (SystemUtils.IS_OS_WINDOWS) {
            bringWindowToFrontUsingWinApi();
        } else {
            bringWindowToFrontUsingRobot();
        }
    }

    public static void bringWindowToFrontUsingWinApi() {
        if (PlatformUtils.isMobile() || !SystemUtils.IS_OS_WINDOWS) {
            return;
        }
        try {
            Class<?> user32Class = Class.forName("com.sun.jna.platform.win32.User32");
            Field instanceField = user32Class.getField("INSTANCE");
            Object user32 = instanceField.get(null);
            Method findWindow = user32Class.getMethod("FindWindow", String.class, String.class);
            Method isWindowVisible = user32Class.getMethod("IsWindowVisible", Class.forName("com.sun.jna.platform.win32.WinDef$HWND"));
            Method showWindow = user32Class.getMethod("ShowWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND"), int.class);
            Method setForegroundWindow = user32Class.getMethod("SetForegroundWindow", Class.forName("com.sun.jna.platform.win32.WinDef$HWND"));
            Method setFocus = user32Class.getMethod("SetFocus", Class.forName("com.sun.jna.platform.win32.WinDef$HWND"));

            String title = safeDisplayTitle();
            Object hWnd = findWindow.invoke(user32, null, title);
            if (hWnd == null) {
                bringWindowToFrontUsingRobot();
                return;
            }
            Class<?> winUser = Class.forName("com.sun.jna.platform.win32.WinUser");
            int swRestore = winUser.getField("SW_RESTORE").getInt(null);
            int swShow = winUser.getField("SW_SHOW").getInt(null);
            if (!(Boolean) isWindowVisible.invoke(user32, hWnd)) {
                showWindow.invoke(user32, hWnd, swRestore);
            }
            showWindow.invoke(user32, hWnd, swShow);
            setForegroundWindow.invoke(user32, hWnd);
            setFocus.invoke(user32, hWnd);
        } catch (Throwable e) {
            System.out.println("Failed to restore the game window via WinAPI: " + e.getMessage());
            bringWindowToFrontUsingRobot();
        }
    }

    public static void bringWindowToFrontUsingRobot() {
        if (PlatformUtils.isMobile()) {
            return;
        }
        try {
            java.awt.EventQueue.invokeLater(() -> {
                int tabKey = Minecraft.isRunningOnMac ? KeyEvent.VK_META : KeyEvent.VK_ALT;
                try {
                    Robot robot = new Robot();
                    int i = 0;
                    while (!safeDisplayIsActive()) {
                        i++;
                        robot.keyPress(tabKey);
                        for (int j = 0; j < i; j++) {
                            robot.keyPress(KeyEvent.VK_TAB);
                            robot.delay(100);
                            robot.keyRelease(KeyEvent.VK_TAB);
                        }
                        robot.keyRelease(tabKey);
                        robot.delay(100);
                        if (i > 25) {
                            return;
                        }
                    }
                } catch (Throwable e) {
                    System.out.println("Failed to use Robot: " + e.getMessage());
                }
            });
        } catch (Throwable e) {
            System.out.println("Failed to schedule Robot: " + e.getMessage());
        }
    }

    public static void captureClip() {
        // Clip capturing removed for simple FarmX
    }

    public void sendNotification(String text, TrayIcon.MessageType type) {
        if (PlatformUtils.isMobile()) {
            return;
        }
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                windows(text, type);
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                mac(text);
            } else if (SystemUtils.IS_OS_LINUX) {
                linux(text);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void windows(String text, TrayIcon.MessageType type) {
        if (trayIcon != null && SystemTray.isSupported()) {
            try {
                trayIcon.displayMessage("Farm Helper", text, type);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void mac(String text) {
        try {
            new ProcessBuilder("osascript", "-e", "display notification \"" + text + "\" with title \"FarmHelper\"").start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void linux(String text) {
        try {
            new ProcessBuilder("notify-send", "-a", "Farm Helper", text).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String safeDisplayTitle() {
        try {
            Class<?> display = Class.forName("org.lwjgl.opengl.Display");
            return (String) display.getMethod("getTitle").invoke(null);
        } catch (Throwable e) {
            return "Minecraft 1.8.9";
        }
    }

    private static boolean safeDisplayIsActive() {
        try {
            Class<?> display = Class.forName("org.lwjgl.opengl.Display");
            return (Boolean) display.getMethod("isActive").invoke(null);
        } catch (Throwable e) {
            return true;
        }
    }
}
