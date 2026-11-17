package com.ckac10.ckaddons.mixin;

import com.ckac10.ckaddons.NoRender;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// No Render > Dead poof: tira o "poof" de morte quando o servidor manda como pacote de partícula
@Mixin(ClientPacketListener.class)
public class MixinClientPacketListenerParticles {

	@Inject(
			method = "handleParticleEvent",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
					shift = At.Shift.AFTER
			),
			cancellable = true
	)
	private void ckaddons$noDeathPoofPacket(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
		if (NoRender.deadPoof() && NoRender.isDeathPoofPacket(packet)) ci.cancel();
	}
}
