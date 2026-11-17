package com.ckac10.ckaddons.mixin;

import com.ckac10.ckaddons.HighlightBeacons;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Highlight Beacons: a cor do contorno de glowing vem da cor do time; nos end crystals vira azul
@Mixin(Entity.class)
public class MixinEntityTeamColor {

	@Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
	private void ckaddons$beaconColor(CallbackInfoReturnable<Integer> cir) {
		if (HighlightBeacons.applies((Entity) (Object) this)) {
			cir.setReturnValue(HighlightBeacons.COLOR);
		}
	}
}
