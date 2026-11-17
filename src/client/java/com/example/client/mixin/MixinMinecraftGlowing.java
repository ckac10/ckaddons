package com.example.client.mixin;

import com.example.client.HighlightBeacons;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Highlight Beacons: faz o jogo desenhar o contorno de glowing nos end crystals
@Mixin(Minecraft.class)
public class MixinMinecraftGlowing {

	@Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
	private void ckaddons$highlightBeacons(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (HighlightBeacons.applies(entity)) {
			cir.setReturnValue(true);
		}
	}
}
