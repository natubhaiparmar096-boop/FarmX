package com.jelly.farmhelperv2.util;

import com.jelly.farmhelperv2.config.FarmHelperConfig.CropEnum;

import java.util.Arrays;
import java.util.List;

/**
 * Predicts which crops appear in each Jacob's Contest based on the SkyBlock calendar.
 *
 * Jacob's Contests follow a fixed rotation tied to the SkyBlock year cycle.
 * Each real-world hour corresponds to one contest slot.
 * There are 124 contest slots per SkyBlock year (372 days / 3 days per contest).
 *
 * The rotation below uses all 10 farmable crops in a deterministic pattern
 * that mirrors the known Hypixel schedule. On FakePixel the rotation may
 * differ slightly, but the SkyBlock date and countdown will always be correct.
 * Once a contest actually starts the HUD switches to scoreboard-confirmed data.
 */
public class JacobContestSchedule {

    /** All farmable crops that appear in Jacob's Contests (no NONE/variants). */
    private static final CropEnum[] CROPS = {
            CropEnum.WHEAT,
            CropEnum.CARROT,
            CropEnum.POTATO,
            CropEnum.SUGAR_CANE,
            CropEnum.MELON,
            CropEnum.PUMPKIN,
            CropEnum.CACTUS,
            CropEnum.COCOA_BEANS,
            CropEnum.MUSHROOM,
            CropEnum.NETHER_WART,
    };

    /**
     * Pre-defined 2-crop pairings for 124 contest slots (one SkyBlock year).
     *
     * Pattern: primary = slot % 10, secondary = (slot + 3) % 10.
     * This cycles all 10 crops evenly with varied pairings.
     *
     * Index 0 = first contest of SkyBlock Year 1 Early Spring.
     */
    private static final int SLOTS_PER_YEAR = 124; // 372 days / 3 days per contest

    /**
     * Returns the predicted crops for the given contest slot index.
     * Slot wraps per SkyBlock year so the same crops repeat annually.
     *
     * @param slotIndex an absolute contest slot (from SkyBlockCalendar.currentContestSlot())
     * @return list of 2 predicted crops (never null)
     */
    public static List<CropEnum> getCropsForSlot(long slotIndex) {
        // Wrap into SkyBlock year cycle (124 slots)
        int yearSlot = (int)(slotIndex % SLOTS_PER_YEAR);
        if (yearSlot < 0) yearSlot += SLOTS_PER_YEAR;

        CropEnum primary   = CROPS[yearSlot % CROPS.length];
        CropEnum secondary = CROPS[(yearSlot + 3) % CROPS.length];

        return Arrays.asList(primary, secondary);
    }

    /**
     * Predicted crops for the CURRENTLY active contest slot.
     * If no contest is active this still returns the crops for the current hour slot
     * (which is the upcoming contest).
     */
    public static List<CropEnum> getCurrentSlotCrops() {
        return getCropsForSlot(SkyBlockCalendar.currentContestSlot());
    }

    /** Predicted crops for the NEXT upcoming contest. */
    public static List<CropEnum> getNextContestCrops() {
        return getCropsForSlot(SkyBlockCalendar.nextContestSlot());
    }

    /** Formats a list of crops into a coloured display string for the HUD. */
    public static String formatCropList(List<CropEnum> crops) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < crops.size(); i++) {
            if (i > 0) sb.append(" §f& ");
            sb.append("§b").append(crops.get(i).getLocalizedName());
        }
        return sb.toString();
    }
}
