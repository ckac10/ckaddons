package com.example.client.mixin;

import com.example.client.ModConfig;
import com.example.client.TitleHud;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Screen Title/Subtitle Render (baseado no VanillaHUD):
 * - Render Title/Subtitle OFF: não desenha title nem subtitle.
 * - Posição/tamanho mudados no "Move GUI": o vanilla é chamado duas vezes, uma com a transformação do title
 *   (desenhando só o title) e outra com a do subtitle (desenhando só o subtitle).
 */
@Mixin(Hud.class)
public class MixinHudTitle {

	@WrapMethod(method = "extractTitle")
	private void ckaddons$titleTransform(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
		ModConfig config = ModConfig.get();
		if (!config.renderTitle) return;
		if (TitleHud.isVanilla(config)) {
			original.call(graphics, deltaTracker);
			return;
		}

		float width = graphics.guiWidth();
		float height = graphics.guiHeight();
		try {
			for (TitleHud.Element element : TitleHud.Element.values()) {
				TitleHud.pass = element == TitleHud.Element.TITLE ? TitleHud.PASS_TITLE : TitleHud.PASS_SUBTITLE;
				graphics.pose().pushMatrix();
				try {
					TitleHud.apply(graphics.pose(), element, width, height);
					original.call(graphics, deltaTracker);
				} finally {
					graphics.pose().popMatrix();
				}
			}
		} finally {
			TitleHud.pass = TitleHud.PASS_BOTH;
		}
	}

	// Primeiro textWithBackdrop = title
	@WrapOperation(method = "extractTitle", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V",
			ordinal = 0))
	private void ckaddons$title(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int width,
								int color, Operation<Void> original) {
		if (TitleHud.pass != TitleHud.PASS_SUBTITLE) original.call(graphics, font, text, x, y, width, color);
	}

	// Segundo textWithBackdrop = subtitle
	@WrapOperation(method = "extractTitle", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V",
			ordinal = 1))
	private void ckaddons$subtitle(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int width,
								   int color, Operation<Void> original) {
		if (TitleHud.pass != TitleHud.PASS_TITLE) original.call(graphics, font, text, x, y, width, color);
	}
}
