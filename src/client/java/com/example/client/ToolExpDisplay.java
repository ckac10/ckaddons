package com.example.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Tool exp display: em picaretas e fishing spears do Dungeon Realms
 * (NBT com "dr:type":"PICKAXE" ou "dr:type":"FISHING_SPEAR"), o nome do item selecionado
 * acima da hotbar fica assim:
 *
 *   Nome do item [Lvl:12] |||||||||||||||||||||||||||||||||||||||||||||||||| [34%]
 *
 * - Lvl: valor de "dr:level" (0 a 100) no custom_data do item,
 *   ex: PublicBukkitValues:{"dr:level":12}
 * - Barra: linha da lore com 50 "|" (podem estar divididos em pedaços de cores diferentes),
 *   desenhada com as cores originais.
 * - Porcentagem: cada "|" verde (green) vale 2%, até 100%.
 */
public final class ToolExpDisplay {

	private static final String TYPE_KEY = "dr:type";
	private static final String LEVEL_KEY = "dr:level";
	private static final Set<String> TOOL_TYPES = Set.of("PICKAXE", "FISHING_SPEAR");
	private static final int BAR_LENGTH = 50;

	private static final int GRAY = TextColor.fromLegacyFormat(ChatFormatting.GRAY).getValue();
	private static final int GREEN = TextColor.fromLegacyFormat(ChatFormatting.GREEN).getValue();
	private static final int YELLOW = TextColor.fromLegacyFormat(ChatFormatting.YELLOW).getValue();

	// Estilos dos pedaços que o mod adiciona (italic false para não herdar o itálico de item renomeado)
	private static final Style BRACKET = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false);
	private static final Style VALUE = Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(false);
	private static final Style PERCENT = Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false);
	private static final Style MAX = Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(false);

	private ToolExpDisplay() {
	}

	/** Devolve "nome [Lvl:N] barra [P%]" se for uma ferramenta do DR; senão devolve o nome igual. */
	public static MutableComponent decorate(ItemStack stack, MutableComponent name) {
		if (!ModConfig.get().toolExpDisplay || stack.isEmpty()) return name;

		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) return name;
		CompoundTag tag = data.copyTag();
		if (!hasToolType(tag, 0)) return name;

		Integer level = findLevel(tag, 0);
		ItemLore lore = stack.get(DataComponents.LORE);
		Bar bar = lore == null ? null : findBar(lore.lines());
		if (level == null && bar == null) return name;

		// O nome vai como filho de um componente vazio: assim os pedaços novos não herdam a cor da raridade
		MutableComponent result = Component.empty().append(name);
		if (level != null) {
			result.append(Component.literal(" [Lvl:").setStyle(BRACKET))
					.append(Component.literal(String.valueOf(level)).setStyle(VALUE))
					.append(Component.literal("]").setStyle(BRACKET));
		}
		if (bar != null) {
			result.append(Component.literal(" ").setStyle(BRACKET))
					.append(bar.text)
					.append(Component.literal(" [").setStyle(BRACKET))
					// No nível máximo o DR deixa a barra toda amarela; 100% de verde também conta como MAX
					.append(bar.max
							? Component.literal("MAX").setStyle(MAX)
							: Component.literal(bar.percent + "%").setStyle(PERCENT))
					.append(Component.literal("]").setStyle(BRACKET));
		}
		return result;
	}

	// ===== "dr:type" =====

	// Procura "dr:type" em qualquer nível do custom_data (pode estar dentro de outro compound)
	private static boolean hasToolType(Tag tag, int depth) {
		if (depth > 8) return false;
		if (tag instanceof CompoundTag compound) {
			Tag type = compound.get(TYPE_KEY);
			if (type != null && type.asString().map(TOOL_TYPES::contains).orElse(false)) return true;
			for (String key : compound.keySet()) {
				if (hasToolType(compound.get(key), depth + 1)) return true;
			}
		} else if (tag instanceof ListTag list) {
			for (int i = 0; i < list.size(); i++) {
				if (hasToolType(list.get(i), depth + 1)) return true;
			}
		}
		return false;
	}

	// ===== Lvl =====

	// Procura "dr:level" (0 a 100) em qualquer nível do custom_data, ex: PublicBukkitValues:{"dr:level":63}
	private static Integer findLevel(Tag tag, int depth) {
		if (depth > 8) return null;
		if (tag instanceof CompoundTag compound) {
			Tag value = compound.get(LEVEL_KEY);
			if (value != null) {
				Integer level = value.asInt()
						.or(() -> value.asString().map(ToolExpDisplay::parseLevel))
						.filter(n -> n >= 0 && n <= 100)
						.orElse(null);
				if (level != null) return level;
			}
			for (String key : compound.keySet()) {
				Integer level = findLevel(compound.get(key), depth + 1);
				if (level != null) return level;
			}
		} else if (tag instanceof ListTag list) {
			for (int i = 0; i < list.size(); i++) {
				Integer level = findLevel(list.get(i), depth + 1);
				if (level != null) return level;
			}
		}
		return null;
	}

	private static Integer parseLevel(String text) {
		String trimmed = text.trim();
		if (trimmed.isEmpty() || trimmed.length() > 3) return null;
		for (int i = 0; i < trimmed.length(); i++) {
			if (!Character.isDigit(trimmed.charAt(i))) return null;
		}
		int value = Integer.parseInt(trimmed);
		return value <= 100 ? value : null;
	}

	private static boolean hasColor(Style style, int rgb) {
		TextColor color = style.getColor();
		return color != null && color.getValue() == rgb;
	}

	// ===== Barra de exp =====

	private record Bar(Component text, int percent, boolean max) {
	}

	/**
	 * Procura uma linha da lore com pelo menos 50 "|" e devolve só os "|", com as cores originais
	 * (ignora o resto do texto da linha). Cada "|" verde vale 2%.
	 */
	private static Bar findBar(List<Component> lines) {
		for (Component line : lines) {
			if (countPipes(line.getString()) < BAR_LENGTH) continue;

			MutableComponent bar = Component.empty();
			int[] green = {0};
			int[] yellow = {0};
			int[] total = {0};
			line.visit((Style style, String text) -> {
				// O texto pode ter códigos de cor antigos (§a, §7...) no meio: aplica eles "|" por "|"
				Style current = style;
				for (int i = 0; i < text.length(); i++) {
					char c = text.charAt(i);
					if (c == '§' && i + 1 < text.length()) {
						ChatFormatting format = ChatFormatting.getByCode(text.charAt(++i));
						if (format != null) {
							current = format == ChatFormatting.RESET ? style : current.applyLegacyFormat(format);
						}
					} else if (c == '|') {
						bar.append(Component.literal("|").setStyle(current.withItalic(false)));
						total[0]++;
					if (isGreen(current)) green[0]++;
					else if (hasColor(current, YELLOW)) yellow[0]++;
					}
				}
				return Optional.empty();
			}, Style.EMPTY);
			int percent = Math.min(100, green[0] * 2);
			// MAX: barra inteira amarela (como o DR mostra no nível máximo) ou 100% verde
			boolean max = percent >= 100 || (total[0] > 0 && yellow[0] == total[0]);
			return new Bar(bar, percent, max);
		}
		return null;
	}

	/**
	 * "Verde": o green do Minecraft (§a) ou qualquer outro tom de verde (dark_green, hex #00FF00...),
	 * ou seja, cor em que o verde é bem mais forte que o vermelho e o azul.
	 */
	private static boolean isGreen(Style style) {
		TextColor color = style.getColor();
		if (color == null) return false;
		int rgb = color.getValue();
		if (rgb == GREEN) return true;
		int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
		return g >= 0x80 && g - r >= 0x40 && g - b >= 0x40;
	}

	private static int countPipes(String text) {
		int count = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '|') count++;
		}
		return count;
	}
}
