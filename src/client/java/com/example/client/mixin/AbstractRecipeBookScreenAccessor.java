package com.example.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Dá acesso ao recipe book das telas que têm um (inventário, crafting table, fornalhas)
@Mixin(AbstractRecipeBookScreen.class)
public interface AbstractRecipeBookScreenAccessor {

	@Accessor("recipeBookComponent")
	RecipeBookComponent<?> ckaddons$getRecipeBook();
}
