package com.example.client.mixin;

import com.example.client.NoRender;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// No Render > Breaking block particles: tira as partículas ao quebrar/minerar blocos (o som continua)
@Mixin(ClientLevel.class)
public class MixinClientLevelParticles {

	// Partículas quando o bloco quebra
	@Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
	private void ckaddons$noDestroyParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (NoRender.breakParticles()) ci.cancel();
	}

	// Partículas enquanto minera (só as partículas; addBreakingBlockEffects também toca o som)
	@Inject(method = "addBreakingParticles", at = @At("HEAD"), cancellable = true)
	private void ckaddons$noBreakingParticles(BlockPos pos, Direction direction, BlockState state, CallbackInfo ci) {
		if (NoRender.breakParticles()) ci.cancel();
	}
}
