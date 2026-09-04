package com.jelly.farmhelperv2.mixin.gui;

import net.minecraft.client.gui.GuiDisconnected;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiDisconnected.class, priority = Integer.MAX_VALUE)
public class MixinGuiDisconnected {
    @Inject(method = "initGui", at = @At("RETURN"))
    public void initGui(CallbackInfo ci) {
        // Auto-reconnect / scheduler disconnect UI removed for simple FarmX
    }
}
