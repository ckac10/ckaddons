package com.example.client.mixin;

import com.example.client.NoRender;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * "Disable all fog": remove fog de terreno, atmosférico (céu/Nether), lava, água, neve em pó, cegueira e escuridão.
 *
 * 1) setupFog: zera o fog direto no FogData, logo depois da última atribuição e ANTES do return.
 *    Isso é o que faz funcionar com o Sodium: ele copia o FogData no RETURN de setupFog e usa essa cópia
 *    nos shaders de terreno e no culling de chunks (sem isso, o terreno ficava com fog e cortado na distância do fog).
 * 2) getBuffer: reforço para o vanilla, usa o buffer de fog "vazio" (o mesmo do toggle de fog do debug).
 */
@Mixin(FogRenderer.class)
public class MixinFogRenderer {

	@Shadow
	@Final
	public static int FOG_UBO_SIZE;

	@Shadow
	@Final
	private GpuBuffer emptyBuffer;

	@Inject(
			method = "setupFog",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F",
					opcode = org.objectweb.asm.Opcodes.PUTFIELD,
					shift = At.Shift.AFTER
			)
	)
	private void ckaddons$disableFogData(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker,
										 float darkenWorldAmount, ClientLevel level,
										 CallbackInfoReturnable<FogData> cir, @Local FogData fog) {
		if (!NoRender.fog()) return;
		fog.environmentalStart = Float.MAX_VALUE;
		fog.environmentalEnd = Float.MAX_VALUE;
		fog.renderDistanceStart = Float.MAX_VALUE;
		fog.renderDistanceEnd = Float.MAX_VALUE;
		fog.skyEnd = Float.MAX_VALUE;
		fog.cloudEnd = Float.MAX_VALUE;
	}

	@Inject(method = "getBuffer", at = @At("HEAD"), cancellable = true)
	private void ckaddons$disableFog(FogRenderer.FogMode mode, CallbackInfoReturnable<GpuBufferSlice> cir) {
		if (NoRender.fog()) {
			cir.setReturnValue(this.emptyBuffer.slice(0L, (long) FOG_UBO_SIZE));
		}
	}
}
