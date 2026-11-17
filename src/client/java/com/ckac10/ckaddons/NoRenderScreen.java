package com.ckac10.ckaddons;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Sub-tela "No Render" da tela simples (usada quando o YACL não está instalado).
 * Toggle principal no topo e as opções em duas colunas; as opções ficam desativadas com o principal OFF.
 */
public class NoRenderScreen extends Screen {

	private static final int BUTTON_WIDTH = 150;
	private static final int BUTTON_HEIGHT = 20;
	private static final int SPACING = 24;

	private final Screen parent;

	public NoRenderScreen(Screen parent) {
		super(Component.literal("No Render"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ModConfig c = ModConfig.get();
		int left = this.width / 2 - BUTTON_WIDTH - 2;
		int right = this.width / 2 + 2;
		int y = this.height / 4 + 10;

		addRenderableWidget(CycleButton.onOffBuilder(c.noRender)
				.create(this.width / 2 - 100, y, 200, BUTTON_HEIGHT, Component.literal("No Render"), (b, v) -> {
					c.noRender = v;
					ModConfig.save();
					rebuildWidgets();
				}));
		y += SPACING + 6;

		toggle(left, y, "Fire overlay", c.noRenderFireOverlay, v -> c.noRenderFireOverlay = v);
		toggle(right, y, "Break particles", c.noRenderBreakParticles, v -> c.noRenderBreakParticles = v);
		y += SPACING;
		toggle(left, y, "Fog", c.disableFog, v -> c.disableFog = v);
		toggle(right, y, "Effect display", c.noRenderEffectDisplay, v -> c.noRenderEffectDisplay = v);
		y += SPACING;
		toggle(left, y, "Recipe book", c.hideRecipeBook, v -> c.hideRecipeBook = v);
		toggle(right, y, "Dead entities", c.noRenderDeadEntities, v -> c.noRenderDeadEntities = v);
		y += SPACING;
		toggle(left, y, "Dead poof", c.noRenderDeadPoof, v -> c.noRenderDeadPoof = v);
		toggle(right, y, "Vignette", c.noRenderVignette, v -> c.noRenderVignette = v);
		y += SPACING + 6;

		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
				.bounds(this.width / 2 - 100, y, 200, BUTTON_HEIGHT)
				.build());
	}

	private void toggle(int x, int y, String label, boolean value, Consumer<Boolean> setter) {
		CycleButton<Boolean> button = CycleButton.onOffBuilder(value)
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal(label), (b, v) -> {
					setter.accept(v);
					ModConfig.save();
				});
		button.active = ModConfig.get().noRender;
		addRenderableWidget(button);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, this.title, this.width / 2, this.height / 4 - 10, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		ModConfig.save();
		this.minecraft.gui.setScreen(parent);
	}
}
