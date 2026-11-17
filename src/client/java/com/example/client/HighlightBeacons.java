package com.example.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

/**
 * Highlight Beacons: end crystals ganham o efeito de glowing azul, só no cliente
 * (o servidor não fica sabendo, e outros jogadores não veem).
 */
public final class HighlightBeacons {

	// Mesma cor do §9 (ChatFormatting.BLUE)
	public static final int COLOR = 0x5555FF;

	private HighlightBeacons() {
	}

	public static boolean applies(Entity entity) {
		return entity instanceof EndCrystal && ModConfig.get().highlightBeacons;
	}
}
