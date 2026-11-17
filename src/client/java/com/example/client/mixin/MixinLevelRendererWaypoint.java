package com.example.client.mixin;

import com.example.client.CoordsWaypoint;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Render Coords Waypoint: desenha o waypoint junto com as outras coisas do mundo (igual o IQ faz). */
@Mixin(LevelRenderer.class)
public class MixinLevelRendererWaypoint {

	@Inject(method = "submitFeatures", at = @At("TAIL"))
	private void ckaddons$renderWaypoint(LevelRenderState state, SubmitNodeCollector collector,
										 boolean renderBlockOutline, CallbackInfo ci) {
		CoordsWaypoint.render(state.cameraRenderState, collector);
	}
}
