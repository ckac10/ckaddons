package com.ckac10.ckaddons;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Controle de keybind para o YACL (o YACL não tem um pronto).
 * Clique para escutar, depois aperte uma tecla ou botão do mouse.
 * Backspace/Delete desvincula a tecla.
 */
final class KeybindController implements Controller<InputConstants.Key> {

	private final Option<InputConstants.Key> option;

	KeybindController(Option<InputConstants.Key> option) {
		this.option = option;
	}

	@Override
	public Option<InputConstants.Key> option() {
		return option;
	}

	@Override
	public Component formatValue() {
		InputConstants.Key key = option.pendingValue();
		if (key.equals(InputConstants.UNKNOWN)) {
			return Component.literal("Not bound").withStyle(ChatFormatting.GRAY);
		}
		return key.getDisplayName();
	}

	@Override
	public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
		return new Widget(this, screen, widgetDimension);
	}

	private static final class Widget extends ControllerWidget<KeybindController> {

		private boolean listening = false;

		Widget(KeybindController control, YACLScreen screen, Dimension<Integer> dim) {
			super(control, screen, dim);
		}

		@Override
		protected Component getValueText() {
			if (listening) {
				return Component.literal("> ")
						.append(control.formatValue().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
						.append(" <")
						.withStyle(ChatFormatting.YELLOW);
			}
			return control.formatValue();
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			if (listening) {
				// Qualquer botão do mouse vira a nova tecla
				set(InputConstants.Type.MOUSE.getOrCreate(event.button()));
				return true;
			}
			if (!isMouseOver(event.x(), event.y()) || !isAvailable()) return false;
			listening = true;
			playDownSound();
			return true;
		}

		@Override
		public boolean keyPressed(KeyEvent event) {
			if (!listening) {
				// Com o foco no controle (Tab), Enter/Espaço começa a escutar
				if (focused && isAvailable() && (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_SPACE)) {
					listening = true;
					return true;
				}
				return false;
			}
			if (event.key() == InputConstants.KEY_BACKSPACE || event.key() == InputConstants.KEY_DELETE) {
				set(InputConstants.UNKNOWN);
			} else {
				set(InputConstants.getKey(event));
			}
			return true;
		}

		private void set(InputConstants.Key key) {
			control.option().requestSet(key);
			listening = false;
		}

		@Override
		public void unfocus() {
			super.unfocus();
			listening = false;
		}

		@Override
		protected int getHoveredControlWidth() {
			return getUnhoveredControlWidth();
		}

		@Override
		protected int getUnhoveredControlWidth() {
			return textRenderer.width(getValueText());
		}
	}
}
