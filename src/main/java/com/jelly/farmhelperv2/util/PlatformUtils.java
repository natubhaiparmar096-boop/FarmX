package com.jelly.farmhelperv2.util;

import java.io.File;

/**
 * Detects Android / Pojav / Zalith / GL4ES so FarmX can avoid NanoVG, AWT, and Display APIs.
 */
public final class PlatformUtils {
    private static final Boolean FORCED = parseForced();
    private static final boolean MOBILE = FORCED != null ? FORCED : detectMobile();

    private PlatformUtils() {
    }

    public static boolean isMobile() {
        return MOBILE;
    }

    private static Boolean parseForced() {
        String prop = System.getProperty("farmx.mobile");
        if (prop == null || prop.isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(prop);
    }

    private static boolean detectMobile() {
        try {
            Class.forName("android.os.Build");
            return true;
        } catch (Throwable ignored) {
        }

        String vendor = safeLower(System.getProperty("java.vendor"));
        String vm = safeLower(System.getProperty("java.vm.name"));
        if (vendor.contains("android") || vm.contains("dalvik") || vm.contains("art")) {
            return true;
        }

        if (System.getenv("POJAV_RENDERER") != null
                || System.getenv("LIBGL_ES") != null
                || System.getenv("ZALITH_RENDERER") != null) {
            return true;
        }

        String glLib = safeLower(System.getProperty("org.lwjgl.opengl.libname"));
        if (glLib.contains("gl4es") || glLib.contains("holygl4es") || glLib.contains("ng-gl4es")) {
            return true;
        }

        if ("gl4es".equals(safeLower(System.getProperty("pojav.renderer")))
                || safeLower(System.getenv("POJAV_RENDERER")).contains("gl4es")) {
            return true;
        }

        // Silence unused warning while keeping a cheap filesystem probe for real Android roots.
        if (new File("/system/build.prop").exists() || new File("/system/bin/app_process").exists()) {
            return true;
        }

        return false;
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
