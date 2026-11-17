package com.example.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

/**
 * Tela de configuração do ckaddons (aberta pelo Mod Menu ou por /ckaddons).
 * Os botões ON/OFF salvam na hora; os sliders de opacidade salvam ao fechar a tela.
 * Cada slider só aparece quando a opção dele está ON, e a tela se reorganiza sem deixar buraco.
 */
public class ConfigScreen extends Screen {

	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int SPACING = 24;

	// Lembra a última aba aberta enquanto o jogo estiver aberto
	private static Tab currentTab = Tab.DUNGEON_REALMS;

	private final Screen parent;

	public ConfigScreen(Screen parent) {
		super(Component.literal(ConfigScreens.TITLE));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int x = this.width / 2 - BUTTON_WIDTH / 2;
		int tabY = this.height / 4 + 20;

		// Abas: a aba selecionada fica desativada (aparece "apertada")
		int tabWidth = 106;
		int tabsX = this.width / 2 - (tabWidth * Tab.values().length + 4 * (Tab.values().length - 1)) / 2;
		for (Tab tab : Tab.values()) {
			int tabX = tabsX + tab.ordinal() * (tabWidth + 4);
			Button tabButton = Button.builder(Component.literal(tab.label), button -> {
				currentTab = tab;
				rebuildWidgets();
			}).bounds(tabX, tabY, tabWidth, BUTTON_HEIGHT).build();
			tabButton.active = tab != currentTab;
			addRenderableWidget(tabButton);
		}

		int y = tabY + SPACING + 8;
		switch (currentTab) {
			case DUNGEON_REALMS -> initDungeonRealms(x, y);
			case VANILLA -> initVanilla(x, y);
			case DEBUG -> initDebug(x, y);
		}

		// Concluído (posição fixa, igual nas duas abas)
		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
				.bounds(x, Math.min(y + SPACING * 7, this.height - 26), BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());
	}

	// Aba "Dungeon Realms QoL": cada subcategoria abre uma sub-tela
	private void initDungeonRealms(int x, int y) {
		subScreenButton(x, y, "Energy Bar Render...", () -> new EnergyBarScreen(this));
		y += SPACING;
		subScreenButton(x, y, "Render Coords Waypoint...", () -> new WaypointScreen(this));
		y += SPACING;
		subScreenButton(x, y, "Misc...", () -> new MiscScreen(this));
	}

	private void subScreenButton(int x, int y, String label, java.util.function.Supplier<Screen> screen) {
		addRenderableWidget(Button.builder(Component.literal(label),
						button -> this.minecraft.gui.setScreen(screen.get()))
				.bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());
	}

	// Aba "Vanilla QoL": cada subcategoria abre uma sub-tela
	private void initVanilla(int x, int y) {
		subScreenButton(x, y, "No Render...", () -> new NoRenderScreen(this));
		y += SPACING;
		subScreenButton(x, y, "Screen Title/Subtitle Render...", () -> new TitleSettingsScreen(this));
		y += SPACING;
		subScreenButton(x, y, "Misc...", () -> new VanillaMiscScreen(this));
	}

	// Aba "Debug": Copy item NBT + botão que abre o menu de teclas do jogo para trocar a tecla
	private void initDebug(int x, int y) {
		ModConfig config = ModConfig.get();

		addRenderableWidget(CycleButton.onOffBuilder(config.copyNbt)
				.create(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Copy item NBT"), (button, value) -> {
					config.copyNbt = value;
					ModConfig.save();
					rebuildWidgets(); // ativa/desativa o botão da tecla
				}));
		y += SPACING;

		Button keybindButton = Button.builder(
						Component.literal("Keybind: ").append(MainClass.getCopyNbtKey().getTranslatedKeyMessage()),
						button -> this.minecraft.gui.setScreen(new KeyBindsScreen(this, this.minecraft.options)))
				.bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		keybindButton.active = config.copyNbt;
		addRenderableWidget(keybindButton);
	}

	private enum Tab {
		DUNGEON_REALMS("Dungeon Realms QoL"),
		VANILLA("Vanilla QoL"),
		DEBUG("Debug");

		final String label;

		Tab(String label) {
			this.label = label;
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, this.title, this.width / 2, this.height / 4, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		ModConfig.save();
		this.minecraft.gui.setScreen(parent);
	}

	// Slider de 0 a max segundos (Compact chat time)
	static class SecondsSlider extends AbstractSliderButton {

		private final String label;
		private final int max;
		private final IntConsumer setter;

		SecondsSlider(int x, int y, String label, int max, int initial, IntConsumer setter) {
			this(x, y, BUTTON_WIDTH, label, max, initial, setter);
		}

		SecondsSlider(int x, int y, int width, String label, int max, int initial, IntConsumer setter) {
			super(x, y, width, BUTTON_HEIGHT, Component.empty(), initial / (double) max);
			this.label = label;
			this.max = max;
			this.setter = setter;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.literal(label + ": " + Math.round(this.value * max) + "s"));
		}

		@Override
		protected void applyValue() {
			setter.accept((int) Math.round(this.value * max));
		}
	}

	// Slider de 0 a 100% reaproveitado pelas opções de opacidade
	static class OpacitySlider extends AbstractSliderButton {

		private final String label;
		private final IntConsumer setter;

		OpacitySlider(int x, int y, String label, int initialPercent, IntConsumer setter) {
			super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), initialPercent / 100.0);
			this.label = label;
			this.setter = setter;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.literal(label + ": " + Math.round(this.value * 100) + "%"));
		}

		@Override
		protected void applyValue() {
			setter.accept((int) Math.round(this.value * 100));
		}
	}
}
