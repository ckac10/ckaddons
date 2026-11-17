package com.ckac10.ckaddons;

import com.ckac10.ckaddons.mixin.AbstractContainerScreenAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MainClass implements ClientModInitializer {

	private static KeyMapping copyNbtKey;

	// Usada pela aba Debug da config para trocar a tecla
	public static KeyMapping getCopyNbtKey() {
		return copyNbtKey;
	}

	// O chat fecha logo depois de rodar o comando, então a tela abre no tick seguinte
	private static boolean openConfigNextTick = false;

	@Override
	public void onInitializeClient() {
		ModConfig.load();

		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("ckaddons", "general")
		);

		copyNbtKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.ckaddons.copynbt",
				InputConstants.Type.KEYBOARD,
				InputConstants.KEY_N,
				category
		));

		// Sem tela aberta: a tecla chega pelo tick normal
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (copyNbtKey.consumeClick()) {
				if (ModConfig.get().copyNbt) copyHeldItemNbt(client);
			}

			if (openConfigNextTick) {
				openConfigNextTick = false;
				client.gui.setScreen(ConfigScreens.create(null));
			}
		});

		// /ckaddons abre a tela de configuração
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) ->
				dispatcher.register(ClientCommands.literal("ckaddons").executes(context -> {
					openConfigNextTick = true;
					return 1;
				}))
		);

		// Com inventário/baú aberto: a tecla vai para a tela, então escutamos ali
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (!(screen instanceof AbstractContainerScreen<?>)) return;

			ScreenKeyboardEvents.afterKeyPress(screen).register((s, key) -> {
				if (!ModConfig.get().copyNbt || !copyNbtKey.matches(key)) return;
				if (s.getFocused() instanceof EditBox) return; // digitando na busca do criativo

				copyHoveredItemNbt(client, (AbstractContainerScreenAccessor) s);
			});
		});

		// Render chests waypoints: lê a coordenada do vault nas mensagens do servidor
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (!overlay) CoordsWaypoint.onGameMessage(message);
		});

		StaminaCrosshairRenderer.register();
	}

	// Inventário aberto: copia o item que está embaixo do mouse
	private static void copyHoveredItemNbt(Minecraft client, AbstractContainerScreenAccessor screen) {
		LocalPlayer player = client.player;
		if (player == null) return;

		Slot slot = screen.ckaddons$getHoveredSlot();
		if (slot == null || !slot.hasItem()) {
			player.sendSystemMessage(Component.literal("§cNenhum item embaixo do mouse"));
			return;
		}

		copyItemNbt(client, player, slot.getItem());
	}

	// Sem tela aberta: copia o item da mão
	private static void copyHeldItemNbt(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) return;

		ItemStack stack = player.getMainHandItem();
		if (stack.isEmpty()) {
			player.sendSystemMessage(Component.literal("§cNenhum item na mão"));
			return;
		}

		copyItemNbt(client, player, stack);
	}

	// Copia o item (id, quantidade e data components) em SNBT para a área de transferência
	private static void copyItemNbt(Minecraft client, LocalPlayer player, ItemStack stack) {
		ItemStack.CODEC
				.encodeStart(player.registryAccess().createSerializationContext(NbtOps.INSTANCE), stack)
				.result()
				.ifPresentOrElse(tag -> {
					client.keyboardHandler.setClipboard(tag.toString());
					player.sendSystemMessage(Component.literal("§aNBT COPIADO: §f").append(stack.getHoverName()));
				}, () -> player.sendSystemMessage(Component.literal("§cErro ao copiar NBT do item")));
	}
}
