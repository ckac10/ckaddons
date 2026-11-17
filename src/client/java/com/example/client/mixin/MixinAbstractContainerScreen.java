package com.example.client.mixin;

import com.example.client.RarityUnderlay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Underlay de raridade nos slots de inventário, baús e outras telas com slots
@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {

	@Inject(method = "extractSlot", at = @At("HEAD"))
	private void ckaddons$rarityUnderlay(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
		RarityUnderlay.draw(graphics, slot.getItem(), slot.x, slot.y);
	}
}
