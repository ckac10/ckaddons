package com.example.client.mixin;

import com.example.client.RarityUnderlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Underlay de raridade nos slots da hotbar (e da mão secundária)
@Mixin(Hud.class)
public class MixinHud {

	@Inject(method = "extractSlot", at = @At("HEAD"))
	private void ckaddons$rarityUnderlay(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker,
										 Player player, ItemStack stack, int seed, CallbackInfo ci) {
		RarityUnderlay.draw(graphics, stack, x, y);
	}
}
