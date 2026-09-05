package com.jelly.farmhelperv2.util;

/**
 * SkyBlock calendar utility.
 *
 * SkyBlock uses a fixed time system:
 *   20 real minutes = 1 SkyBlock day
 *   372 SkyBlock days (12 months x 31 days) = 1 SkyBlock year ~ 5.16 real days
 *
 * Jacob's Contests happen every real-world hour, starting at :15 past, lasting 20 minutes.
 */
public class SkyBlockCalendar {

    /**
     * Unix timestamp (ms) when SkyBlock Year 1, Early Spring Day 1 began.
     * Community-established epoch: June 6, 2019 ~18:55 UTC.
     */
    public static final long EPOCH_MS = 1559829300000L;

    public static final long MS_PER_SB_DAY   = 1_200_000L;  // 20 real minutes
    public static final int  DAYS_PER_MONTH  = 31;
    public static final int  MONTHS_PER_YEAR = 12;
    public static final int  DAYS_PER_YEAR   = DAYS_PER_MONTH * MONTHS_PER_YEAR; // 372

    /** Jacob's Contest timing (real-world) */
    public static final long MS_PER_REAL_HOUR        = 3_600_000L;
    public static final long CONTEST_START_OFFSET_MS = 900_000L;   // :15 past the hour
    public static final long CONTEST_DURATION_MS     = 1_200_000L; // :15 to :35

    public static final String[] MONTH_NAMES = {
            "Early Spring", "Spring",      "Late Spring",
            "Early Summer", "Summer",      "Late Summer",
            "Early Autumn", "Autumn",      "Late Autumn",
            "Early Winter", "Winter",      "Late Winter"
    };

    // ── SkyBlock date helpers ───────────────────────────────────────────

    public static long totalSkyBlockDays() {
        return (System.currentTimeMillis() - EPOCH_MS) / MS_PER_SB_DAY;
    }

    public static int getYear() {
        return (int)(totalSkyBlockDays() / DAYS_PER_YEAR) + 1;
    }

    public static int getDayOfYear() {
        return (int)(totalSkyBlockDays() % DAYS_PER_YEAR);
    }

    public static int getMonthIndex() {
        return getDayOfYear() / DAYS_PER_MONTH;
    }

    public static String getMonthName() {
        return MONTH_NAMES[getMonthIndex()];
    }

    public static int getDayOfMonth() {
        return (getDayOfYear() % DAYS_PER_MONTH) + 1;
    }

    public static String getDateString() {
        return getMonthName() + " " + getDayOfMonth() + ", Year " + getYear();
    }

    // ── Contest timing ──────────────────────────────────────────────────

    /** True if a Jacob's Contest is currently active (:15–:35 past the hour). */
    public static boolean isContestActive() {
        long msIntoHour = System.currentTimeMillis() % MS_PER_REAL_HOUR;
        long elapsed    = msIntoHour - CONTEST_START_OFFSET_MS;
        return elapsed >= 0 && elapsed < CONTEST_DURATION_MS;
    }

    /** Milliseconds remaining in the current active contest (0 if not active). */
    public static long msLeftInContest() {
        if (!isContestActive()) return 0L;
        long msIntoHour = System.currentTimeMillis() % MS_PER_REAL_HOUR;
        return CONTEST_DURATION_MS - (msIntoHour - CONTEST_START_OFFSET_MS);
    }

    /** Milliseconds until the NEXT contest starts (always > 0). */
    public static long msUntilNextContest() {
        long now        = System.currentTimeMillis();
        long msIntoHour = now % MS_PER_REAL_HOUR;
        long msUntil    = (CONTEST_START_OFFSET_MS - msIntoHour + MS_PER_REAL_HOUR) % MS_PER_REAL_HOUR;
        return msUntil == 0 ? MS_PER_REAL_HOUR : msUntil;
    }

    /**
     * Monotonically increasing index — increments every real hour.
     * Two calls during the same contest window return the same value,
     * so crop prediction is stable for the whole hour.
     */
    public static long currentContestSlot() {
        return System.currentTimeMillis() / MS_PER_REAL_HOUR;
    }

    /** Slot index for the NEXT upcoming contest. */
    public static long nextContestSlot() {
        return isContestActive() ? currentContestSlot() + 1 : currentContestSlot();
    }
}
