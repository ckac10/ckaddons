package com.example.client.mixin;

import com.example.client.NoRender;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// No Render > Effect display: tira a lista de efeitos ao lado do inventário
// (canSeeEffects = false também faz o inventário voltar para o centro)
@Mixin(EffectsInInventory.class)
public class MixinEffectsInInventory {

	@Inject(method = "canSeeEffects", at = @At("HEAD"), cancellable = true)
	private void ckaddons$noEffectDisplay(CallbackInfoReturnable<Boolean> cir) {
		if (NoRender.effectDisplay()) cir.setReturnValue(false);
	}
}
