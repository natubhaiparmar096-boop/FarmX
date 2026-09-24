package com.jelly.farmhelperv2.feature.impl.pest.helpers;

public final class PestPlotId {
    private PestPlotId() {}

    public static String normalize(String plot) {
        if (plot == null) return "";
        return plot.replaceAll("\\D", "").trim();
    }

    public static boolean isUsable(String plot) {
        return plot != null && !normalize(plot).isEmpty();
    }

    public static boolean equals(String a, String b) {
        if (a == null || b == null) return false;
        String normA = normalize(a);
        String normB = normalize(b);
        return !normA.isEmpty() && normA.equals(normB);
    }
}
