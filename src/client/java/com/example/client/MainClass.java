package com.example.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class MainClass implements ClientModInitializer {

	private static KeyMapping fullbrightKey;
	public static boolean fullbrightAtivo = true;

	@Override
	public void onInitializeClient() {
		registerFullbright();
		StaminaCrosshairRenderer.register();
	}

	private static void registerFullbright() {
		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("ckaddons", "general")
		);

		fullbrightKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.modid.fullbright",
				InputConstants.Type.KEYBOARD,
				InputConstants.KEY_B,
				category
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (fullbrightKey.consumeClick()) {
				fullbrightAtivo = !fullbrightAtivo;

				if (client.player != null) {
					client.player.sendSystemMessage(Component.literal(
							fullbrightAtivo ? "§aFullbright ATIVADO" : "§cFullbright DESATIVADO"
					));
				}
			}
		});
	}
}
