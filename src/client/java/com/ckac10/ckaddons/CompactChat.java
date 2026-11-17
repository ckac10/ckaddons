package com.ckac10.ckaddons;

import com.ckac10.ckaddons.mixin.ChatComponentAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.HashMap;
import java.util.Map;

/**
 * Compact chat (baseado no mod Compacting de celestialfault, licença zlib).
 * Quando a mesma mensagem chega de novo dentro do tempo configurado (compactChatTime, em segundos),
 * a mensagem anterior é apagada do chat e a nova aparece com um contador cinza, ex: "Hello (3)".
 * Linhas divisórias (----- / ===== / ▬▬▬▬▬) nunca são compactadas.
 */
public final class CompactChat {

	private static final Style COUNTER_STYLE = Style.EMPTY.applyLegacyFormat(ChatFormatting.GRAY);
	private static final long PRUNE_EVERY_MS = 15_000;

	private static final Map<Component, Entry> entries = new HashMap<>();
	private static long lastPrune = 0;

	// Mensagem sendo adicionada agora (o mixin associa o GuiMessage criado a ela)
	private static Entry pending;

	private CompactChat() {
	}

	private static final class Entry {
		int timesSeen;
		long lastSeen;
		GuiMessage lastMessage;

		boolean isOld(long now) {
			return now - lastSeen >= ModConfig.get().compactChatTime * 1000L;
		}
	}

	/** Chamado no começo de ChatComponent.addMessage: devolve o texto a exibir (com contador, se repetido). */
	public static Component process(Component content) {
		pending = null;
		if (!ModConfig.get().compactChat || content == null || isDivider(content)) return content;

		long now = System.currentTimeMillis();
		prune(now);

		Entry entry = entries.get(content);
		if (entry == null || entry.isOld(now)) {
			entry = new Entry();
			entries.put(content.copy(), entry);
		}
		entry.timesSeen++;
		entry.lastSeen = now;
		pending = entry;

		if (entry.timesSeen < 2) return content;

		removeFromChat(entry.lastMessage);
		return content.copy().append(Component.literal(" (" + entry.timesSeen + ")").setStyle(COUNTER_STYLE));
	}

	/** Chamado logo depois de o GuiMessage ser criado em addMessage. */
	public static GuiMessage track(GuiMessage message) {
		if (pending != null) {
			pending.lastMessage = message;
			pending = null;
		}
		return message;
	}

	public static void clear() {
		entries.clear();
		pending = null;
	}

	private static void removeFromChat(GuiMessage message) {
		if (message == null) return;
		ChatComponentAccessor chat = (ChatComponentAccessor) Minecraft.getInstance().gui.hud.getChat();
		// GuiMessage é um record (equals por valor), então compara por identidade
		chat.ckaddons$getAllMessages().removeIf(m -> m == message);
		chat.ckaddons$getTrimmedMessages().removeIf(line -> line.parent() == message);
	}

	private static void prune(long now) {
		if (now - lastPrune < PRUNE_EVERY_MS) return;
		lastPrune = now;
		entries.values().removeIf(e -> e.isOld(now));
	}

	private static boolean isDivider(Component content) {
		String text = ChatFormatting.stripFormatting(content.getString());
		if (text == null || text.length() <= 5) return false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c != '-' && c != '=' && c != '▬') return false;
		}
		return true;
	}
}
