package com.ckac10.ckaddons.mixin;

import com.ckac10.ckaddons.NoRender;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// No Render > Dead entities: não desenha mobs na animação de morte
@Mixin(LevelExtractor.class)
public class MixinLevelExtractor {

	@Inject(method = "isEntityVisible", at = @At("HEAD"), cancellable = true)
	private void ckaddons$hideDeadEntities(Entity entity, Frustum frustum, double camX, double camY, double camZ,
										   float partialTick, long time, CallbackInfoReturnable<Boolean> cir) {
		if (NoRender.deadEntities() && entity instanceof LivingEntity && !entity.isAlive()) {
			cir.setReturnValue(false);
		}
	}
}
