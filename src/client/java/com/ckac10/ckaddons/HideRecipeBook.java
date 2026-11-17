package com.ckac10.ckaddons;

import com.ckac10.ckaddons.mixin.ImageButtonAccessor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Hide recipe book (baseado no mod NoRecipeBook de Grayray75).
 * O botão do recipe book é reconhecido pela textura "recipe_book/button".
 */
public final class HideRecipeBook {

	private static final String RECIPE_BUTTON_TEXTURE = "recipe_book/button";

	private HideRecipeBook() {}

	public static boolean isRecipeButton(Screen screen, Object widget) {
		if (!NoRender.recipeBook()) return false;
		if (!(screen instanceof AbstractContainerScreen<?>) || !(widget instanceof ImageButton)) return false;

		return ((ImageButtonAccessor) widget).ckaddons$getSprites().get(true, false).getPath().equals(RECIPE_BUTTON_TEXTURE);
	}
}
