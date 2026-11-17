package com.example.client.mixin;

import com.example.client.NoRender;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Hide recipe book: se o recipe book abriria aberto (estado salvo pelo jogo), fecha e recentraliza o inventário
@Mixin(Gui.class)
public abstract class MixinGui {

	@Inject(method = "setScreen", at = @At("TAIL"))
	private void ckaddons$closeRecipeBook(Screen screen, CallbackInfo ci) {
		if (!NoRender.recipeBook()) return;
		if (!(screen instanceof AbstractRecipeBookScreen<?> recipeBookScreen)) return;

		RecipeBookComponent<?> recipeBook = ((AbstractRecipeBookScreenAccessor) recipeBookScreen).ckaddons$getRecipeBook();
		AbstractContainerScreenAccessor container = (AbstractContainerScreenAccessor) screen;

		if (recipeBook.isVisible()) {
			recipeBook.toggleVisibility();
			container.ckaddons$setLeftPos(recipeBook.updateScreenPosition(screen.width, container.ckaddons$getImageWidth()));
		}
	}
}
