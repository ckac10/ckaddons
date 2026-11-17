package com.example.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

// Só é carregada se o Mod Menu estiver instalado (entrypoint "modmenu" no fabric.mod.json)
public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigScreens::create;
	}
}
