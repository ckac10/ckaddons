package com.ckac10.ckaddons.mixin;

import com.ckac10.ckaddons.NoRender;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// No Render > Dead poof: tira a nuvem de fumaça quando um mob morre (versão vanilla, gerada no cliente)
@Mixin(LivingEntity.class)
public class MixinLivingEntityPoof {

	@Inject(method = "makePoofParticles", at = @At("HEAD"), cancellable = true)
	private void ckaddons$noDeathPoof(CallbackInfo ci) {
		if (NoRender.deadPoof() && ((LivingEntity) (Object) this).level().isClientSide()) {
			ci.cancel();
		}
	}
}
