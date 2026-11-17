package com.ckac10.ckaddons.mixin;

import com.ckac10.ckaddons.NoRender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class MixinHudNoRender {

	// No Render > Effect display: tira os ícones de efeito do canto superior direito
	@Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
	private void ckaddons$noEffectIcons(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (NoRender.effectDisplay()) ci.cancel();
	}

	// No Render > Vignette: tira o escurecimento das bordas da tela (e o vermelho da borda do mundo)
	@Inject(method = "extractVignette", at = @At("HEAD"), cancellable = true)
	private void ckaddons$noVignette(GuiGraphicsExtractor graphics, Entity entity, CallbackInfo ci) {
		if (NoRender.vignette()) ci.cancel();
	}
}
