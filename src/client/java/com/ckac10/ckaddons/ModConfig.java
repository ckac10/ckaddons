package com.ckac10.ckaddons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configurações do mod, salvas em .minecraft/config/ckaddons.json.
 */
public class ModConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ckaddons.json");

	private static ModConfig instance;

	// ===== OPÇÕES =====
	public boolean fullbright = true;
	public boolean rarityUnderlay = true;
	public int rarityOpacity = 70;  // 0 a 100 (%)
	public boolean staminaCrosshair = true;
	public int staminaOpacity = 50; // 0 a 100 (%)
	public boolean staminaHideOutOfCombat = false; // só mostra a barra se tomou dano nos últimos segundos
	public int staminaCombatTime = 5; // 1 a 15 (segundos)
	public boolean highlightBeacons = true; // brilho azul (só no cliente) nos end crystals
	public boolean toolExpDisplay = false; // barra de exp da picareta/fishing spear no nome do item selecionado
	public boolean renderChestsWaypoints = true; // waypoint no vault da mensagem "The key unlocks a nearby vault at X, Y, Z!"
	public int chestsWaypointTime = 30; // 0 a 60 (segundos); 0 = até trocar de mundo
	public boolean compactChat = true;
	public int compactChatTime = 60; // 0 a 60 (segundos)
	public boolean selectedItemNameAlwaysVisible = false;

	// Screen Title/Subtitle Render (posição em fração da tela, -1 = posição do vanilla; veja TitleHud.java)
	public boolean renderTitle = true;
	public float titleX = -1f;
	public float titleY = -1f;
	public float titleScale = 1f;
	public float subtitleX = -1f;
	public float subtitleY = -1f;
	public float subtitleScale = 1f;
	public boolean copyNbt = true; // a tecla fica salva no options.txt do jogo (menu Controles)

	// No Render (cada opção só vale com noRender ligado; veja NoRender.java)
	// hideRecipeBook e disableFog mantêm o nome antigo para não perder o que já estava salvo
	public boolean noRender = true;
	public boolean noRenderFireOverlay = false;
	public boolean noRenderBreakParticles = false;
	public boolean disableFog = false;
	public boolean noRenderEffectDisplay = false;
	public boolean hideRecipeBook = true;
	public boolean noRenderDeadEntities = false;
	public boolean noRenderDeadPoof = false;
	public boolean noRenderVignette = false;

	public static ModConfig get() {
		if (instance == null) load();
		return instance;
	}

	public static void load() {
		instance = null;
		if (Files.exists(PATH)) {
			try {
				instance = GSON.fromJson(Files.readString(PATH), ModConfig.class);
			} catch (Exception e) {
				System.err.println("[ckaddons] Erro ao ler config, usando padrão: " + e.getMessage());
			}
		}
		if (instance == null) instance = new ModConfig();
		save(); // cria o arquivo na primeira vez e adiciona opções novas
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			Files.writeString(PATH, GSON.toJson(instance));
		} catch (Exception e) {
			System.err.println("[ckaddons] Erro ao salvar config: " + e.getMessage());
		}
	}
}
