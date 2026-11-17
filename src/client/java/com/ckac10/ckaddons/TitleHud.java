package com.ckac10.ckaddons;

import org.joml.Matrix3x2fStack;

/**
 * Screen Title/Subtitle Render (baseado no VanillaHUD): posição e tamanho do title e do subtitle.
 *
 * O vanilla desenha os dois juntos: centro da tela, title com escala 4 (y = -10) e subtitle com escala 2 (y = 5).
 * Aqui cada um tem um "âncora" (o centro do texto no vanilla) que é levado para a posição escolhida e
 * escalado em volta dela. Posição salva como fração da tela (0..1), -1 = posição do vanilla.
 */
public final class TitleHud {

	public static final int PASS_BOTH = 0;
	public static final int PASS_TITLE = 1;
	public static final int PASS_SUBTITLE = 2;

	public static final float MIN_SCALE = 0.25f;
	public static final float MAX_SCALE = 3.0f;

	// Qual linha o vanilla deve desenhar agora (o MixinHudTitle chama o vanilla uma vez para cada)
	public static int pass = PASS_BOTH;

	public enum Element {
		TITLE(4.0f, -10),
		SUBTITLE(2.0f, 5);

		/** Escala do vanilla e posição y do texto (já escalado) relativa ao centro da tela. */
		public final float vanillaScale;
		public final int vanillaY;

		Element(float vanillaScale, int vanillaY) {
			this.vanillaScale = vanillaScale;
			this.vanillaY = vanillaY;
		}

		public float anchorX(float guiWidth) {
			return guiWidth / 2f;
		}

		/** Centro vertical do texto no vanilla (texto tem 9 de altura). */
		public float anchorY(float guiHeight) {
			return guiHeight / 2f + (vanillaY + 4.5f) * vanillaScale;
		}

		public float x(ModConfig c) {
			return this == TITLE ? c.titleX : c.subtitleX;
		}

		public float y(ModConfig c) {
			return this == TITLE ? c.titleY : c.subtitleY;
		}

		public float scale(ModConfig c) {
			return this == TITLE ? c.titleScale : c.subtitleScale;
		}

		public void set(ModConfig c, float x, float y, float scale) {
			if (this == TITLE) {
				c.titleX = x;
				c.titleY = y;
				c.titleScale = scale;
			} else {
				c.subtitleX = x;
				c.subtitleY = y;
				c.subtitleScale = scale;
			}
		}

		/** Posição atual (centro do texto) na tela, em pixels da GUI. */
		public float posX(ModConfig c, float guiWidth) {
			float x = x(c);
			return x < 0 ? anchorX(guiWidth) : x * guiWidth;
		}

		public float posY(ModConfig c, float guiHeight) {
			float y = y(c);
			return y < 0 ? anchorY(guiHeight) : y * guiHeight;
		}
	}

	private TitleHud() {
	}

	/** true se nada foi mudado (aí o vanilla desenha normal, sem as duas passadas). */
	public static boolean isVanilla(ModConfig c) {
		return c.titleX < 0 && c.titleY < 0 && c.titleScale == 1f
				&& c.subtitleX < 0 && c.subtitleY < 0 && c.subtitleScale == 1f;
	}

	/** Leva o âncora do elemento para a posição escolhida e aplica a escala em volta dela. */
	public static void apply(Matrix3x2fStack pose, Element element, float guiWidth, float guiHeight) {
		ModConfig c = ModConfig.get();
		float scale = element.scale(c);
		pose.translate(element.posX(c, guiWidth), element.posY(c, guiHeight));
		pose.scale(scale, scale);
		pose.translate(-element.anchorX(guiWidth), -element.anchorY(guiHeight));
	}

	public static void reset(ModConfig c) {
		Element.TITLE.set(c, -1f, -1f, 1f);
		Element.SUBTITLE.set(c, -1f, -1f, 1f);
	}
}
