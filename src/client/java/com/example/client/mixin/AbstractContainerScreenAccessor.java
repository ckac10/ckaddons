package com.example.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Dá acesso a campos protegidos da tela de inventário/baú
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

	// Slot que está embaixo do mouse (copy NBT)
	@Accessor("hoveredSlot")
	Slot ckaddons$getHoveredSlot();

	// Usados para recentralizar o inventário quando o recipe book é fechado
	@Accessor("imageWidth")
	int ckaddons$getImageWidth();

	@Accessor("leftPos")
	void ckaddons$setLeftPos(int value);
}
