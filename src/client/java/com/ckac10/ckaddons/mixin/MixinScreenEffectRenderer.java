package com.ckac10.ckaddons.mixin;

import com.ckac10.ckaddons.NoRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// No Render > Fire overlay: tira o fogo da tela quando você está pegando fogo
@Mixin(ScreenEffectRenderer.class)
public class MixinScreenEffectRenderer {

	@Inject(method = "submitFire", at = @At("HEAD"), cancellable = true)
	private static void ckaddons$noFireOverlay(PoseStack poseStack, SubmitNodeCollector collector,
											   TextureAtlasSprite sprite, CallbackInfo ci) {
		if (NoRender.fireOverlay()) ci.cancel();
	}
}
