package com.ckac10.ckaddons;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.Optional;

/**
 * Desenha um fundo colorido atrás dos itens com raridade.
 * A raridade vem de uma linha da lore em itálico com o nome exato da raridade,
 * ex: {italic:1b,text:"Common"}. A cor do texto não importa.
 * O prefixo "Transmuted" é ignorado: "Transmuted Epic" conta como Epic.
 * Exceção: "Mythic" na cor dark_red usa underlay vermelho escuro em vez de dourado.
 */
public final class RarityUnderlay {

	// Cor do underlay (RGB; a opacidade vem da config, rarityOpacity) e texto esperado na lore
	private enum Rarity {
		COMMON(0xAAAAAA, "Common"),       // cinza
		UNCOMMON(0x55FF55, "Uncommon"),   // verde
		RARE(0x55FFFF, "Rare"),           // aqua
		EPIC(0xAA00AA, "Epic"),           // roxo escuro
		LEGENDARY(0xFFFF55, "Legendary"), // amarelo
		MYTHIC(0xFFAA00, "Mythic");       // dourado

		private static final Rarity[] VALUES = values();

		final int underlayColor;
		final String loreText;

		Rarity(int underlayColor, String loreText) {
			this.underlayColor = underlayColor;
			this.loreText = loreText;
		}
	}

	// Variante: texto "Mythic" na cor dark_red -> underlay vermelho escuro
	private static final TextColor DARK_RED = TextColor.fromLegacyFormat(ChatFormatting.DARK_RED);
	private static final int MYTHIC_DARK_RED_COLOR = 0xAA0000;

	private RarityUnderlay() {}

	public static void draw(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
		if (!ModConfig.get().rarityUnderlay || stack.isEmpty()) return;

		fromLore(stack).ifPresent(rgb -> graphics.fill(x, y, x + 16, y + 16, withAlpha(rgb)));
	}

	// Procura um trecho da lore em itálico com texto exato ("Common", "Rare"...) e devolve a cor do underlay.
	// visit() percorre o texto e os filhos já com o estilo herdado resolvido.
	private static Optional<Integer> fromLore(ItemStack stack) {
		ItemLore lore = stack.get(DataComponents.LORE);
		if (lore == null) return Optional.empty();

		for (Component line : lore.lines()) {
			Optional<Integer> found = line.visit((Style style, String text) -> {
				if (!style.isItalic()) return Optional.empty();

				String trimmed = stripPrefix(text.trim());
				for (Rarity rarity : Rarity.VALUES) {
					if (trimmed.equalsIgnoreCase(rarity.loreText)) {
						if (rarity == Rarity.MYTHIC && DARK_RED.equals(style.getColor())) {
							return Optional.of(MYTHIC_DARK_RED_COLOR);
						}
						return Optional.of(rarity.underlayColor);
					}
				}
				return Optional.empty();
			}, Style.EMPTY);

			if (found.isPresent()) return found;
		}
		return Optional.empty();
	}

	// Aplica a opacidade da config (0-100%) a uma cor RGB
	private static int withAlpha(int rgb) {
		int opacity = Math.clamp(ModConfig.get().rarityOpacity, 0, 100);
		int alpha = Math.round(opacity * 255 / 100.0f);
		return (alpha << 24) | (rgb & 0xFFFFFF);
	}

	// "Transmuted Epic" -> "Epic"
	private static final String TRANSMUTED = "transmuted ";

	private static String stripPrefix(String text) {
		if (text.regionMatches(true, 0, TRANSMUTED, 0, TRANSMUTED.length())) {
			return text.substring(TRANSMUTED.length()).trim();
		}
		return text;
	}
}
