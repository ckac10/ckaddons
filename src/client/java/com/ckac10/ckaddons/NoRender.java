package com.ckac10.ckaddons;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;

/**
 * No Render (ideia baseada no NoFrills de WhatYouThing): esconde coisas visuais.
 * Cada opção só vale quando o toggle principal "No Render" está ligado.
 */
public final class NoRender {

	private NoRender() {
	}

	private static boolean on() {
		return ModConfig.get().noRender;
	}

	public static boolean fireOverlay() {
		return on() && ModConfig.get().noRenderFireOverlay;
	}

	public static boolean breakParticles() {
		return on() && ModConfig.get().noRenderBreakParticles;
	}

	public static boolean fog() {
		return on() && ModConfig.get().disableFog;
	}

	public static boolean effectDisplay() {
		return on() && ModConfig.get().noRenderEffectDisplay;
	}

	public static boolean recipeBook() {
		return on() && ModConfig.get().hideRecipeBook;
	}

	public static boolean deadEntities() {
		return on() && ModConfig.get().noRenderDeadEntities;
	}

	public static boolean deadPoof() {
		return on() && ModConfig.get().noRenderDeadPoof;
	}

	public static boolean vignette() {
		return on() && ModConfig.get().noRenderVignette;
	}

	/**
	 * Servidores costumam mandar o "poof" de morte como 1 partícula POOF parada,
	 * com um deslocamento pequeno (diferente de zero) em cada eixo.
	 */
	public static boolean isDeathPoofPacket(ClientboundLevelParticlesPacket packet) {
		if (packet.particle().getType() != ParticleTypes.POOF) return false;
		if (packet.count() != 1) return false;
		if (packet.xMaxSpeed() != 0.0f || packet.yMaxSpeed() != 0.0f || packet.zMaxSpeed() != 0.0f) return false;
		for (float offset : new float[]{packet.xDist(), packet.yDist(), packet.zDist()}) {
			if (offset == 0.0f || Math.abs(offset) >= 0.1f) return false;
		}
		return true;
	}
}
