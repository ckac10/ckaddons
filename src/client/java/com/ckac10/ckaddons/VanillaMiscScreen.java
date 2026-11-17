package com.ckac10.ckaddons;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Sub-tela "Misc." da aba Vanilla QoL na tela simples (usada quando o YACL não está instalado):
 * Fullbright, Compact chat (+ tempo) e Selected Item Name Always visible.
 */
public class VanillaMiscScreen extends Screen {

	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int SPACING = 24;

	private final Screen parent;

	public VanillaMiscScreen(Screen parent) {
		super(Component.literal("Misc."));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ModConfig c = ModConfig.get();
		int x = this.width / 2 - BUTTON_WIDTH / 2;
		int y = this.height / 4 + 10;

		// Fullbright
		addRenderableWidget(CycleButton.onOffBuilder(c.fullbright)
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Fullbright"), (b, v) -> {
					c.fullbright = v;
					ModConfig.save();
				}));
		y += SPACING;

		// Compact chat + tempo (0 a 60 segundos)
		addRenderableWidget(CycleButton.onOffBuilder(c.compactChat)
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Compact chat"), (b, v) -> {
					c.compactChat = v;
					ModConfig.save();
					rebuildWidgets(); // mostra/esconde o slider
				}));
		y += SPACING;

		if (c.compactChat) {
			addRenderableWidget(new ConfigScreen.SecondsSlider(x, y, "Compact chat time", 60,
					c.compactChatTime, v -> c.compactChatTime = v));
			y += SPACING;
		}

		// Selected Item Name Always visible (nome do item acima da hotbar não some)
		addRenderableWidget(CycleButton.onOffBuilder(c.selectedItemNameAlwaysVisible)
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Selected Item Name Always visible"), (b, v) -> {
					c.selectedItemNameAlwaysVisible = v;
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
