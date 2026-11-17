package com.ckac10.ckaddons.mixin;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Dá acesso às texturas do botão (usado para reconhecer o botão do recipe book)
@Mixin(ImageButton.class)
public interface ImageButtonAccessor {

	@Accessor("sprites")
	WidgetSprites ckaddons$getSprites();
}
