package com.ckac10.ckaddons.mixin;

import com.ckac10.ckaddons.NoRender;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Hide recipe book: evita erro quando isVisible() é chamado antes do recipe book terminar de ser criado
@Mixin(RecipeBookComponent.class)
public abstract class MixinRecipeBookComponent<T extends RecipeBookMenu> {

	@Shadow
	@Final
	protected T menu;

	@Unique
	private boolean ckaddons$initialized = false;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void ckaddons$markInitialized(CallbackInfo ci) {
		this.ckaddons$initialized = true;
	}

	@Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
	private void ckaddons$safeIsVisible(CallbackInfoReturnable<Boolean> cir) {
		if (!NoRender.recipeBook()) return;
		if (!this.ckaddons$initialized || this.menu == null) {
			cir.setReturnValue(false);
		}
	}
}
