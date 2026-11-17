package com.example.client;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Tela de configuração no estilo YACL (mesmo layout do Cubes Without Borders):
 * abas no topo, lista de opções à esquerda e descrição da opção à direita.
 * Só é carregada se o YACL estiver instalado (veja {@link ConfigScreens}).
 */
final class YaclConfigScreen {

	static Screen create(Screen parent) {
		ModConfig config = ModConfig.get();
		ModConfig defaults = new ModConfig();

		// ===== Aba "Dungeon Realms QoL" =====
		Option<Integer> rarityOpacity = Option.<Integer>createBuilder()
				.name(Component.literal("Rarity overlay opacity"))
				.description(OptionDescription.of(Component.literal("How opaque the rarity overlay drawn behind items is.")))
				.binding(defaults.rarityOpacity, () -> config.rarityOpacity, v -> config.rarityOpacity = v)
				.controller(opt -> IntegerSliderControllerBuilder.create(opt)
						.range(0, 100).step(1)
						.formatValue(v -> Component.literal(v + "%")))
				.available(config.rarityUnderlay)
				.build();

		Option<Boolean> rarityUnderlay = Option.<Boolean>createBuilder()
				.name(Component.literal("Rarity overlay"))
				.description(OptionDescription.of(Component.literal("Draws a colored overlay behind items based on their rarity.")))
				.binding(defaults.rarityUnderlay, () -> config.rarityUnderlay, v -> config.rarityUnderlay = v)
				.controller(TickBoxControllerBuilder::create)
				.addListener((opt, event) -> rarityOpacity.setAvailable(opt.pendingValue()))
				.build();

		Option<Integer> staminaOpacity = Option.<Integer>createBuilder()
				.name(Component.literal("Energy bar opacity"))
				.description(OptionDescription.of(Component.literal("How opaque the energy bar on the crosshair is.")))
				.binding(defaults.staminaOpacity, () -> config.staminaOpacity, v -> config.staminaOpacity = v)
				.controller(opt -> IntegerSliderControllerBuilder.create(opt)
						.range(0, 100).step(1)
						.formatValue(v -> Component.literal(v + "%")))
				.available(config.staminaCrosshair)
				.build();

		Option<Integer> staminaCombatTime = Option.<Integer>createBuilder()
				.name(Component.literal("Combat time"))
				.description(OptionDescription.of(Component.literal(
						"How many seconds after taking damage you count as in combat (the bar stays visible).")))
				.binding(defaults.staminaCombatTime, () -> config.staminaCombatTime, v -> config.staminaCombatTime = v)
				.controller(opt -> IntegerSliderControllerBuilder.create(opt)
						.range(1, 15).step(1)
						.formatValue(v -> Component.literal(v + "s")))
				.available(config.staminaCrosshair && config.staminaHideOutOfCombat)
				.build();

		Option<Boolean> staminaHideOutOfCombat = Option.<Boolean>createBuilder()
				.name(Component.literal("Hide bar out of combat"))
				.description(OptionDescription.of(Component.literal(
						"The energy bar only shows if you took damage in the last few seconds (Combat time); "
								+ "otherwise it is hidden.")))
				.binding(defaults.staminaHideOutOfCombat, () -> config.staminaHideOutOfCombat, v -> config.staminaHideOutOfCombat = v)
				.controller(TickBoxControllerBuilder::create)
				.available(config.staminaCrosshair)
				.build();

		Option<Boolean> staminaCrosshair = Option.<Boolean>createBuilder()
				.name(Component.literal("Energy bar on crosshair"))
				.description(OptionDescription.of(Component.literal("Shows your energy (stamina) as a small bar next to the crosshair.")))
				.binding(defaults.staminaCrosshair, () -> config.staminaCrosshair, v -> config.staminaCrosshair = v)
				.controller(TickBoxControllerBuilder::create)
				.build();

		// Opacidade e "Hide bar out of combat" dependem da barra ligada; "Combat time" depende dos dois
		Runnable updateEnergyBar = () -> {
			boolean on = staminaCrosshair.pendingValue();
			staminaOpacity.setAvailable(on);
			staminaHideOutOfCombat.setAvailable(on);
			staminaCombatTime.setAvailable(on && staminaHideOutOfCombat.pendingValue());
		};
		staminaCrosshair.addListener((opt, event) -> updateEnergyBar.run());
		staminaHideOutOfCombat.addListener((opt, event) -> updateEnergyBar.run());

		Option<Integer> chestsWaypointTime = Option.<Integer>createBuilder()
				.name(Component.literal("Chests waypoint time"))
				.description(OptionDescription.of(Component.literal(
						"How long (in seconds) a vault chest waypoint stays on screen. "
								+ "0 keeps it until you change worlds.")))
				.binding(defaults.chestsWaypointTime, () -> config.chestsWaypointTime, v -> config.chestsWaypointTime = v)
				.controller(opt -> IntegerSliderControllerBuilder.create(opt)
						.range(0, 60).step(1)
						.formatValue(v -> Component.literal(v + "s")))
				.available(config.renderChestsWaypoints)
				.build();

		Option<Boolean> renderChestsWaypoints = Option.<Boolean>createBuilder()
				.name(Component.literal("Render chests waypoints"))
				.description(OptionDescription.of(Component.literal(
						"When the message \"The key unlocks a nearby vault at X, Y, Z!\" appears, "
								+ "shows a \"Chest\" waypoint at that position.")))
				.binding(defaults.renderChestsWaypoints, () -> config.renderChestsWaypoints, v -> config.renderChestsWaypoints = v)
				.controller(TickBoxControllerBuilder::create)
				.addListener((opt, event) -> chestsWaypointTime.setAvailable(opt.pendingValue()))
				.build();

		ConfigCategory dungeonRealms = ConfigCategory.createBuilder()
				.name(Component.literal("Dungeon Realms QoL"))
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Energy Bar Render"))
						.description(OptionDescription.of(Component.literal(
								"Energy (stamina) bar next to the crosshair.")))
						.option(staminaCrosshair)
						.option(staminaOpacity)
						.option(staminaHideOutOfCombat)
						.option(staminaCombatTime)
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Render Coords Waypoint"))
						.description(OptionDescription.of(Component.literal(
								"Waypoints shown through walls for vault chests announced in chat.")))
						.option(renderChestsWaypoints)
						.option(chestsWaypointTime)
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Misc."))
						.description(OptionDescription.of(Component.literal(
								"Rarity overlay, beacon highlight and tool exp display.")))
						.option(rarityUnderlay)
						.option(rarityOpacity)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Highlight Beacons"))
								.description(OptionDescription.of(Component.literal(
										"Makes beacons buffs have glowing so its easy to spot.")))
								.binding(defaults.highlightBeacons, () -> config.highlightBeacons, v -> config.highlightBeacons = v)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Tool exp display"))
								.description(OptionDescription.of(Component.literal(
										"Shows the exp bar of your pickaxe or fishing spear in front of the selected item name, above the hotbar. "
												+ "Pairs well with \"Selected Item Name Always visible\".")))
								.binding(defaults.toolExpDisplay, () -> config.toolExpDisplay, v -> config.toolExpDisplay = v)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.build())
				.build();

		// ===== Aba "Vanilla QoL" =====
		Option<Integer> compactChatTime = Option.<Integer>createBuilder()
				.name(Component.literal("Compact chat time"))
				.description(OptionDescription.of(Component.literal(
						"How long (in seconds) a message is remembered. If the same message arrives again "
								+ "within this time, it is compacted. 0 disables compacting.")))
				.binding(defaults.compactChatTime, () -> config.compactChatTime, v -> config.compactChatTime = v)
				.controller(opt -> IntegerSliderControllerBuilder.create(opt)
						.range(0, 60).step(1)
						.formatValue(v -> Component.literal(v + "s")))
				.available(config.compactChat)
				.build();

		Option<Boolean> compactChat = Option.<Boolean>createBuilder()
				.name(Component.literal("Compact chat"))
				.description(OptionDescription.of(Component.literal(
						"Repeated chat messages are merged into one, with a counter like (2), (3)...")))
				.binding(defaults.compactChat, () -> config.compactChat, v -> config.compactChat = v)
				.controller(TickBoxControllerBuilder::create)
				.addListener((opt, event) -> compactChatTime.setAvailable(opt.pendingValue()))
				.build();

		// Move GUI só fica clicável com Render Title/Subtitle ligado
		ButtonOption moveGui = ButtonOption.createBuilder()
				.name(Component.literal("Move GUI"))
				.description(OptionDescription.of(Component.literal(
						"Opens an editor where you drag the title and subtitle to move them "
								+ "and scroll over them to change their size.")))
				.text(Component.literal("Open"))
				.action((screen, opt) -> Minecraft.getInstance().gui.setScreen(new TitleEditorScreen(screen)))
				.available(config.renderTitle)
				.build();

		Option<Boolean> renderTitle = Option.<Boolean>createBuilder()
				.name(Component.literal("Render Title/Subtitle"))
				.description(OptionDescription.of(Component.literal(
						"Shows the title and subtitle text in the middle of the screen. Turn off to hide them.")))
				.binding(defaults.renderTitle, () -> config.renderTitle, v -> config.renderTitle = v)
				.controller(TickBoxControllerBuilder::create)
				.addListener((opt, event) -> moveGui.setAvailable(opt.pendingValue()))
				.build();

		ConfigCategory vanilla = ConfigCategory.createBuilder()
				.name(Component.literal("Vanilla QoL"))
				.group(noRenderGroup(config, defaults))
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Screen Title/Subtitle Render"))
						.description(OptionDescription.of(Component.literal(
								"Show or hide the big title/subtitle text, and change where it appears and how big it is.")))
						.option(renderTitle)
						.option(moveGui)
						.build())
				.group(OptionGroup.createBuilder()
						.name(Component.literal("Misc."))
						.description(OptionDescription.of(Component.literal(
								"Fullbright, compact chat and selected item name.")))
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Fullbright"))
								.description(OptionDescription.of(Component.literal("Makes everything fully lit, even in the dark.")))
								.binding(defaults.fullbright, () -> config.fullbright, v -> config.fullbright = v)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(compactChat)
						.option(compactChatTime)
						.option(Option.<Boolean>createBuilder()
								.name(Component.literal("Selected Item Name Always visible"))
								.description(OptionDescription.of(Component.literal(
										"The name of the selected item above the hotbar stays visible instead of fading out.")))
								.binding(defaults.selectedItemNameAlwaysVisible, () -> config.selectedItemNameAlwaysVisible,
										v -> config.selectedItemNameAlwaysVisible = v)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.build())
				.build();

		// ===== Aba "Debug" =====
		// A tecla é a mesma do menu Controles do jogo (fica salva no options.txt)
		Option<InputConstants.Key> copyNbtKeybind = keybindOption(MainClass.getCopyNbtKey(),
				"Copy item NBT keybind",
				"Key used to copy the item NBT. Click, then press a key or mouse button. "
						+ "Backspace/Delete unbinds it. Same setting as in Controls.",
				config.copyNbt);

		ConfigCategory debug = ConfigCategory.createBuilder()
				.name(Component.literal("Debug"))
				.option(Option.<Boolean>createBuilder()
						.name(Component.literal("Copy item NBT"))
						.description(OptionDescription.of(Component.literal(
								"Press the keybind to copy an item's NBT (SNBT) to the clipboard: "
										+ "the item in your hand, or the item under the mouse when an inventory is open.")))
						.binding(defaults.copyNbt, () -> config.copyNbt, v -> config.copyNbt = v)
						.controller(TickBoxControllerBuilder::create)
						.addListener((opt, event) -> copyNbtKeybind.setAvailable(opt.pendingValue()))
						.build())
				.option(copyNbtKeybind)
				.build();

		return YetAnotherConfigLib.createBuilder()
				.title(Component.literal(ConfigScreens.TITLE))
				.category(dungeonRealms)
				.category(vanilla)
				.category(debug)
				.save(ModConfig::save)
				.build()
				.generateScreen(parent);
	}

	// Opção de tecla ligada a um KeyMapping do jogo (fica salva no options.txt, igual ao menu Controles)
	private static Option<InputConstants.Key> keybindOption(KeyMapping mapping, String name, String description,
															 boolean available) {
		return Option.<InputConstants.Key>createBuilder()
				.name(Component.literal(name))
				.description(OptionDescription.of(Component.literal(description)))
				.binding(mapping.getDefaultKey(),
						() -> InputConstants.getKey(mapping.saveString()),
						key -> {
							mapping.setKey(key);
							KeyMapping.resetMapping();
							Minecraft.getInstance().options.save();
						})
				.customController(KeybindController::new)
				.available(available)
				.build();
	}

	// Grupo "No Render": toggle principal + uma opção para cada coisa a esconder
	private static OptionGroup noRenderGroup(ModConfig config, ModConfig defaults) {
		List<Option<Boolean>> subOptions = List.of(
				subOption("Fire overlay", "Removes the fire overlay on your screen when you are burning.",
						defaults.noRenderFireOverlay, () -> config.noRenderFireOverlay, v -> config.noRenderFireOverlay = v, config),
				subOption("Breaking block particles", "Removes the particles that appear when mining and breaking blocks.",
						defaults.noRenderBreakParticles, () -> config.noRenderBreakParticles, v -> config.noRenderBreakParticles = v, config),
				subOption("Fog", "Removes every kind of fog: terrain (render distance), atmospheric (Overworld sky and Nether), "
								+ "lava, water, powder snow, blindness and darkness.",
						defaults.disableFog, () -> config.disableFog, v -> config.disableFog = v, config),
				subOption("Effect display", "Removes the potion effect display from the inventory and the top right of the screen.",
						defaults.noRenderEffectDisplay, () -> config.noRenderEffectDisplay, v -> config.noRenderEffectDisplay = v, config),
				subOption("Recipe book", "Removes the recipe book button from inventories and the \"new recipe\" toasts.",
						defaults.hideRecipeBook, () -> config.hideRecipeBook, v -> config.hideRecipeBook = v, config),
				subOption("Dead entities", "Hides mobs that are playing their death animation.",
						defaults.noRenderDeadEntities, () -> config.noRenderDeadEntities, v -> config.noRenderDeadEntities = v, config),
				subOption("Dead poof", "Hides the smoke \"poof\" particles that appear when a mob dies.",
						defaults.noRenderDeadPoof, () -> config.noRenderDeadPoof, v -> config.noRenderDeadPoof = v, config),
				subOption("Vignette", "Removes the dark vignette around the edges of the screen (and the red world border warning).",
						defaults.noRenderVignette, () -> config.noRenderVignette, v -> config.noRenderVignette = v, config)
		);

		Option<Boolean> master = Option.<Boolean>createBuilder()
				.name(Component.literal("No Render"))
				.description(OptionDescription.of(Component.literal(
						"Hides visual elements. Turn this on, then choose below what to hide.")))
				.binding(defaults.noRender, () -> config.noRender, v -> config.noRender = v)
				.controller(TickBoxControllerBuilder::create)
				.addListener((opt, event) -> subOptions.forEach(o -> o.setAvailable(opt.pendingValue())))
				.build();

		return OptionGroup.createBuilder()
				.name(Component.literal("No Render"))
				.description(OptionDescription.of(Component.literal("Hide fire, particles, fog, effects and more.")))
				.option(master)
				.options(subOptions)
				.build();
	}

	private static Option<Boolean> subOption(String name, String description, boolean def,
											 Supplier<Boolean> getter, Consumer<Boolean> setter, ModConfig config) {
		return Option.<Boolean>createBuilder()
				.name(Component.literal(name))
				.description(OptionDescription.of(Component.literal(description)))
				.binding(def, getter, setter)
				.controller(TickBoxControllerBuilder::create)
				.available(config.noRender)
				.build();
	}

	private YaclConfigScreen() {
	}
}
