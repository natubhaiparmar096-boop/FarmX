package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.util.TablistUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PestTabSnapshot {
    private static final Pattern PESTS_ALIVE_PATTERN =
            Pattern.compile("(?i)(?:Pests|Alive):?\\s*\\(?(\\d+)\\)?");
    private static final Pattern COOLDOWN_PATTERN =
            Pattern.compile("(?i)Cooldown:\\s*\\(?(READY|MAX\\s*PESTS?|(?:(\\d+)m)?\\s*(?:(\\d+)s)?)\\)?");
    private static final Pattern INFESTED_PLOTS_PATTERN =
            Pattern.compile("(?i)(?:Infested )?Plots?:\\s*(.+)");
    // Extracts plot number regardless of prefix (e.g. "Plot 7", "Plot - 7", "Plot #7", "7", "7 (2 pests)")
    private static final Pattern PLOT_NUMBER_EXTRACT =
            Pattern.compile("(?i)(?:Plot\\s*[-#]?\\s*)?(\\d{1,2})");
    private static final Pattern BONUS_PATTERN =
            Pattern.compile("(?i)Bonus:\\s*\\(?(ACTIVE|INACTIVE)\\)?");

    private final int aliveCount;
    private final int cooldownSeconds;
    private final Boolean bonusInactive;
    private final Set<String> infestedPlots;

    public PestTabSnapshot(int aliveCount, int cooldownSeconds, Boolean bonusInactive, Set<String> infestedPlots) {
        this.aliveCount = aliveCount;
        this.cooldownSeconds = cooldownSeconds;
        this.bonusInactive = bonusInactive;
        this.infestedPlots = infestedPlots != null
                ? Collections.unmodifiableSet(new LinkedHashSet<>(infestedPlots))
                : Collections.emptySet();
    }

    public int getAliveCount() { return aliveCount; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public Boolean getBonusInactive() { return bonusInactive; }
    public Set<String> getInfestedPlots() { return infestedPlots; }

    public static PestTabSnapshot empty() {
        return new PestTabSnapshot(-1, -1, null, Collections.emptySet());
    }

    public static PestTabSnapshot read() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            return empty();
        }
        return parse(TablistUtils.getTabList());
    }

    public static PestTabSnapshot parse(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return empty();
        }
        int aliveCount = -1;
        int cooldownSeconds = -1;
        Boolean bonusInactive = null;
        Set<String> infestedPlots = new LinkedHashSet<>();

        for (String raw : lines) {
            if (raw == null) continue;
            String clean = StringUtils.stripControlCodes(raw).trim();

            Matcher aliveMatcher = PESTS_ALIVE_PATTERN.matcher(clean);
            if (aliveMatcher.find()) {
                aliveCount = Math.max(aliveCount, parseInt(aliveMatcher.group(1)));
            }

            Matcher cooldownMatcher = COOLDOWN_PATTERN.matcher(clean);
            if (cooldownMatcher.find()) {
                String value = cooldownMatcher.group(1).toUpperCase();
                if (value.contains("MAX PEST")) {
                    cooldownSeconds = 999;
                } else if (value.equals("READY")) {
                    cooldownSeconds = 0;
                } else {
                    int minutes = parseInt(cooldownMatcher.group(2));
                    int seconds = parseInt(cooldownMatcher.group(3));
                    if (minutes > 0 || seconds > 0) {
                        cooldownSeconds = minutes * 60 + seconds;
                    }
                }
            }

            Matcher plotsMatcher = INFESTED_PLOTS_PATTERN.matcher(clean);
            if (plotsMatcher.find()) {
                String rawGroup = plotsMatcher.group(1).trim();
                if (!rawGroup.toLowerCase().contains("none")) {
                    for (String part : rawGroup.split(",")) {
                        Matcher plotNumMatcher = PLOT_NUMBER_EXTRACT.matcher(part.trim());
                        if (plotNumMatcher.find()) {
                            int plotNum = parseInt(plotNumMatcher.group(1));
                            if (plotNum >= 0 && plotNum <= 24) {
                                infestedPlots.add(String.valueOf(plotNum));
                            }
                        }
                    }
                }
            }

            Matcher bonusMatcher = BONUS_PATTERN.matcher(clean);
            if (bonusMatcher.find()) {
                bonusInactive = "INACTIVE".equalsIgnoreCase(bonusMatcher.group(1));
            }
        }

        return new PestTabSnapshot(aliveCount, cooldownSeconds, bonusInactive, infestedPlots);
    }

    private static int parseInt(String value) {
        return value == null || value.trim().isEmpty() ? 0 : Integer.parseInt(value.trim());
    }
}
