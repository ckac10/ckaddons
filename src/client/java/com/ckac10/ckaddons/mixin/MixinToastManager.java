package com.ckac10.ckaddons.mixin;

import com.ckac10.ckaddons.NoRender;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Hide recipe book: esconde o aviso "Nova receita desbloqueada" no canto da tela
@Mixin(ToastManager.class)
public abstract class MixinToastManager {

	@Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
	private void ckaddons$hideRecipeToast(Toast toast, CallbackInfo ci) {
		if (NoRender.recipeBook() && toast instanceof RecipeToast) {
			ci.cancel();
		}
	}
}
