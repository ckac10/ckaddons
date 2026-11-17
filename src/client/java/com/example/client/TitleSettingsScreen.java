package com.example.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Sub-tela "Screen Title/Subtitle Render" da tela simples (usada quando o YACL não está instalado):
 * Render Title/Subtitle e o botão "Move GUI" (abre o editor de posição/tamanho).
 */
public class TitleSettingsScreen extends Screen {

	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int SPACING = 24;

	private final Screen parent;

	public TitleSettingsScreen(Screen parent) {
		super(Component.literal("Screen Title/Subtitle Render"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ModConfig c = ModConfig.get();
		int x = this.width / 2 - BUTTON_WIDTH / 2;
		int y = this.height / 4 + 10;

		addRenderableWidget(CycleButton.onOffBuilder(c.renderTitle)
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Render Title/Subtitle"), (b, v) -> {
					c.renderTitle = v;
					ModConfig.save();
					rebuildWidgets(); // ativa/desativa o Move GUI
				}));
		y += SPACING;

		Button moveGui = Button.builder(Component.literal("Move GUI..."),
						b -> this.minecraft.gui.setScreen(new TitleEditorScreen(this)))
				.bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		moveGui.active = c.renderTitle;
		addRenderableWidget(moveGui);
		y += SPACING + 6;

		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
				.bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, this.title, this.width / 2, this.height / 4 - 12, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		ModConfig.save();
		this.minecraft.gui.setScreen(parent);
	}
}
