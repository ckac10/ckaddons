package com.ckac10.ckaddons;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Sub-tela "Energy Bar Render" da tela simples (usada quando o YACL não está instalado):
 * barra no crosshair, opacidade, "Hide bar out of combat" e o tempo de combate.
 */
public class EnergyBarScreen extends Screen {

	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int SPACING = 24;

	private final Screen parent;

	public EnergyBarScreen(Screen parent) {
		super(Component.literal("Energy Bar Render"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ModConfig c = ModConfig.get();
		int x = this.width / 2 - BUTTON_WIDTH / 2;
		int y = this.height / 4 + 10;

		addRenderableWidget(CycleButton.onOffBuilder(c.staminaCrosshair)
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Energy bar on crosshair"), (b, v) -> {
					c.staminaCrosshair = v;
					ModConfig.save();
					rebuildWidgets();
				}));
		y += SPACING;

		if (c.staminaCrosshair) {
			addRenderableWidget(new ConfigScreen.OpacitySlider(x, y, "Energy bar opacity",
					c.staminaOpacity, v -> c.staminaOpacity = v));
			y += SPACING;

			addRenderableWidget(CycleButton.onOffBuilder(c.staminaHideOutOfCombat)
					.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Hide bar out of combat"), (b, v) -> {
						c.staminaHideOutOfCombat = v;
						ModConfig.save();
						rebuildWidgets(); // mostra/esconde o slider de tempo
					}));
			y += SPACING;

			if (c.staminaHideOutOfCombat) {
				addRenderableWidget(new CombatTimeSlider(x, y, c.staminaCombatTime));
				y += SPACING;
			}
		}
		y += 6;

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

	// Slider de 1 a 15 segundos
	private static class CombatTimeSlider extends net.minecraft.client.gui.components.AbstractSliderButton {

		private static final int MIN = 1;
		private static final int MAX = 15;

		CombatTimeSlider(int x, int y, int initial) {
			super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), (initial - MIN) / (double) (MAX - MIN));
			updateMessage();
		}

		private int seconds() {
			return MIN + (int) Math.round(this.value * (MAX - MIN));
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.literal("Combat time: " + seconds() + "s"));
		}

		@Override
		protected void applyValue() {
			ModConfig.get().staminaCombatTime = seconds();
		}
	}
}
