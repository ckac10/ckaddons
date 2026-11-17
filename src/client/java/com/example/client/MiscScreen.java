package com.example.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Sub-tela "Misc." da tela simples (usada quando o YACL não está instalado):
 * Rarity overlay (+ opacidade), Highlight Beacons e Tool exp display.
 */
public class MiscScreen extends Screen {

	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int SPACING = 24;

	private final Screen parent;

	public MiscScreen(Screen parent) {
		super(Component.literal("Misc."));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ModConfig c = ModConfig.get();
		int x = this.width / 2 - BUTTON_WIDTH / 2;
		int y = this.height / 4 + 10;

		// Rarity overlay + opacidade
		addRenderableWidget(CycleButton.onOffBuilder(c.rarityUnderlay)
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Rarity overlay"), (b, v) -> {
					c.rarityUnderlay = v;
					ModConfig.save();
					rebuildWidgets(); // mostra/esconde o slider
				}));
		y += SPACING;

		if (c.rarityUnderlay) {
			addRenderableWidget(new ConfigScreen.OpacitySlider(x, y, "Rarity overlay opacity",
					c.rarityOpacity, v -> c.rarityOpacity = v));
			y += SPACING;
		}

		// Highlight Beacons (brilho azul nos end crystals)
		addRenderableWidget(CycleButton.onOffBuilder(c.highlightBeacons)
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Highlight Beacons"), (b, v) -> {
					c.highlightBeacons = v;
					ModConfig.save();
				}));
		y += SPACING;

		// Tool exp display (barra de exp da ferramenta no nome do item selecionado)
		addRenderableWidget(CycleButton.onOffBuilder(c.toolExpDisplay)
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Tool exp display"), (b, v) -> {
					c.toolExpDisplay = v;
					ModConfig.save();
				}));
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
