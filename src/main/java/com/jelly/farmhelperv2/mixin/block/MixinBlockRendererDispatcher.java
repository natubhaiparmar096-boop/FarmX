package com.jelly.farmhelperv2.mixin.block;

import net.minecraft.client.renderer.BlockRendererDispatcher;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockRendererDispatcher.class)
public class MixinBlockRendererDispatcher {
    // Performance-mode crop culling removed for simple FarmX
}
