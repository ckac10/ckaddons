package com.ckac10.ckaddons;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * "Move GUI" do Screen Title/Subtitle Render (estilo editor de HUD do VanillaHUD):
 * mostra um title e um subtitle de exemplo; arraste para mover e use o scroll em cima para mudar o tamanho.
 * Perto do centro horizontal a posição gruda no meio da tela.
 */
public class TitleEditorScreen extends Screen {

	private static final Component TITLE_SAMPLE = Component.literal("Title");
	private static final Component SUBTITLE_SAMPLE = Component.literal("Subtitle");
	private static final int SNAP_DISTANCE = 4;
	private static final float SCALE_STEP = 0.1f;

	private final Screen parent;

	private TitleHud.Element dragging;
	private double grabOffsetX;
	private double grabOffsetY;

	public TitleEditorScreen(Screen parent) {
		super(Component.literal("Move GUI"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int y = this.height - 28;
		addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
					TitleHud.reset(ModConfig.get());
					ModConfig.save();
				})
				.bounds(this.width / 2 - 154, y, 150, 20)
				.build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
				.bounds(this.width / 2 + 4, y, 150, 20)
				.build());
	}

	// ===== Desenho =====

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		// Fundo leve (sem blur) para dar para ver o jogo atrás
		graphics.fill(0, 0, this.width, this.height, 0x60000000);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		// Guia do centro horizontal
		graphics.fill(this.width / 2, 0, this.width / 2 + 1, this.height, 0x20FFFFFF);

		drawElement(graphics, TitleHud.Element.TITLE, TITLE_SAMPLE, mouseX, mouseY);
		drawElement(graphics, TitleHud.Element.SUBTITLE, SUBTITLE_SAMPLE, mouseX, mouseY);

		graphics.centeredText(this.font, Component.literal("Drag to move, scroll to resize"),
				this.width / 2, 8, 0xFFFFFFFF);
		ModConfig c = ModConfig.get();
		graphics.centeredText(this.font, Component.literal(String.format("Title: %d%%   Subtitle: %d%%",
						Math.round(c.titleScale * 100), Math.round(c.subtitleScale * 100))),
				this.width / 2, 20, 0xFFAAAAAA);
	}

	private void drawElement(GuiGraphicsExtractor graphics, TitleHud.Element element, Component text,
							 int mouseX, int mouseY) {
		ModConfig c = ModConfig.get();
		float drawScale = element.vanillaScale * element.scale(c);
		float x = element.posX(c, this.width);
		float y = element.posY(c, this.height);
		int textWidth = this.font.width(text);

		// Caixa em volta (mais clara quando o mouse está em cima ou arrastando)
		int[] box = box(element, text);
		boolean hovered = dragging == element || (dragging == null && contains(box, mouseX, mouseY));
		graphics.fill(box[0], box[1], box[2], box[3], hovered ? 0x40FFFFFF : 0x20FFFFFF);
		graphics.outline(box[0], box[1], box[2] - box[0], box[3] - box[1], hovered ? 0xFFFFFFFF : 0x80FFFFFF);

		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(drawScale, drawScale);
		graphics.text(this.font, text, -textWidth / 2, -4, 0xFFFFFFFF, true);
		graphics.pose().popMatrix();
	}

	/** {x0, y0, x1, y1} da área do elemento na tela. */
	private int[] box(TitleHud.Element element, Component text) {
		ModConfig c = ModConfig.get();
		float drawScale = element.vanillaScale * element.scale(c);
		float halfW = this.font.width(text) * drawScale / 2f + 2;
		float halfH = 9 * drawScale / 2f + 2;
		float x = element.posX(c, this.width);
		float y = element.posY(c, this.height);
		return new int[]{Mth.floor(x - halfW), Mth.floor(y - halfH), Mth.ceil(x + halfW), Mth.ceil(y + halfH)};
	}

	private static boolean contains(int[] box, double x, double y) {
		return x >= box[0] && x < box[2] && y >= box[1] && y < box[3];
	}

	private TitleHud.Element elementAt(double x, double y) {
		// Subtitle primeiro: ele é menor e pode ficar por cima do title
		if (contains(box(TitleHud.Element.SUBTITLE, SUBTITLE_SAMPLE), x, y)) return TitleHud.Element.SUBTITLE;
		if (contains(box(TitleHud.Element.TITLE, TITLE_SAMPLE), x, y)) return TitleHud.Element.TITLE;
		return null;
	}

	// ===== Mouse =====

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) return true; // botões Reset/Done
		if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) return false; // no 26.3 o botão esquerdo é 1, não 0

		TitleHud.Element element = elementAt(event.x(), event.y());
		if (element == null) return false;

		ModConfig c = ModConfig.get();
		dragging = element;
		grabOffsetX = event.x() - element.posX(c, this.width);
		grabOffsetY = event.y() - element.posY(c, this.height);
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging == null) return super.mouseDragged(event, dragX, dragY);

		ModConfig c = ModConfig.get();
		double x = Mth.clamp(event.x() - grabOffsetX, 0, this.width);
		double y = Mth.clamp(event.y() - grabOffsetY, 0, this.height);
		if (Math.abs(x - this.width / 2.0) <= SNAP_DISTANCE) x = this.width / 2.0; // gruda no centro
		dragging.set(c, (float) (x / this.width), (float) (y / this.height), dragging.scale(c));
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging != null) {
			dragging = null;
			ModConfig.save();
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		TitleHud.Element element = elementAt(mouseX, mouseY);
		if (element == null || scrollY == 0) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

		ModConfig c = ModConfig.get();
		float scale = element.scale(c) + (scrollY > 0 ? SCALE_STEP : -SCALE_STEP);
		scale = Math.round(Mth.clamp(scale, TitleHud.MIN_SCALE, TitleHud.MAX_SCALE) * 100) / 100f;
		// Mantém a posição atual (se ainda era a do vanilla, salva ela como fração)
		element.set(c, element.posX(c, this.width) / this.width, element.posY(c, this.height) / this.height, scale);
		ModConfig.save();
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		ModConfig.save();
		this.minecraft.gui.setScreen(parent);
	}
}
