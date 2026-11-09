package com.example.client.mixin;

import com.example.client.MainClass;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public class MixinLightmapRenderStateExtractor {

    @Inject(method = "extract", at = @At("TAIL"))
    private void applyFullbright(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        if (MainClass.fullbrightAtivo) {
            // valor alto = fullbright de verdade (sem night vision / sem fog estranha)
            renderState.brightness = 15.0F;
            renderState.darknessEffectScale = 0.0F;
            renderState.nightVisionEffectIntensity = 0.0F;
        }
    }
}