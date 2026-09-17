package com.jelly.farmhelperv2.feature.pest;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.util.List;

public interface PestPlatformAdapter {
    boolean isSupportedServer();
    boolean isInGarden();
    List<PestInfo> detectPests();
    BlockPos getPestLocation(PestInfo pest);
    int findPestVacuumSlot();
    boolean isPestVacuumItem(ItemStack itemStack);
    boolean collectPest(PestInfo pest);
    boolean isPestRemoved(PestInfo pest);
    int getCurrentPlotNumber();
    boolean handleSpray();
}
