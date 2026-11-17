package com.example.client.mixin;

import com.example.client.HideRecipeBook;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Hide recipe book: impede que o botão do recipe book seja adicionado na tela
@Mixin(Screen.class)
public abstract class MixinScreen {

	@Inject(method = "addRenderableWidget", at = @At("HEAD"), cancellable = true)
	private <T extends GuiEventListener & Renderable & NarratableEntry> void ckaddons$hideRecipeButton(T widget, CallbackInfoReturnable<T> cir) {
		if (HideRecipeBook.isRecipeButton((Screen) (Object) this, widget)) {
			cir.setReturnValue(widget); // devolve o botão sem adicionar na tela
		}
	}
}
