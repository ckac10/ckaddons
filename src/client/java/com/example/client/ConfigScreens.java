package com.example.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

/**
 * Escolhe qual tela de configuração abrir: a do YACL (se instalado) ou a tela simples do mod.
 */
public final class ConfigScreens {

	public static final String TITLE = "ckaddons Options";

	public static Screen create(Screen parent) {
		if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
			return YaclConfigScreen.create(parent);
		}
		return new ConfigScreen(parent);
	}

	private ConfigScreens() {
	}
}
