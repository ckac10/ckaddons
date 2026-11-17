package com.ckac10.ckaddons;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Sub-tela "Render Coords Waypoint" da tela simples (usada quando o YACL não está instalado):
 * waypoints dos chests (toggle + tempo).
 */
public class WaypointScreen extends Screen {

	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int SPACING = 24;

	private final Screen parent;

	public WaypointScreen(Screen parent) {
		super(Component.literal("Render Coords Waypoint"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ModConfig c = ModConfig.get();
		int x = this.width / 2 - BUTTON_WIDTH / 2;
		int y = this.height / 4 + 10;

		// Waypoints dos chests (mensagem do vault no chat)
		addRenderableWidget(CycleButton.onOffBuilder(c.renderChestsWaypoints)
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Render chests waypoints"), (b, v) -> {
					c.renderChestsWaypoints = v;
					ModConfig.save();
					rebuildWidgets(); // mostra/esconde o slider
				}));
		y += SPACING;

		if (c.renderChestsWaypoints) {
			addRenderableWidget(new ConfigScreen.SecondsSlider(x, y, "Chests waypoint time", 60,
					c.chestsWaypointTime, v -> c.chestsWaypointTime = v));
		}
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
