package com.example.client.mixin;

import com.example.client.ModConfig;
import com.example.client.ToolExpDisplay;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Selected Item Name Always visible: o nome do item selecionado (acima da hotbar) não some com o tempo.
 * O vanilla conta toolHighlightTimer para baixo a cada tick e o nome vai sumindo; aqui o timer fica
 * sempre num valor alto (opacidade cheia) enquanto houver item na mão.
 */
@Mixin(Hud.class)
public class MixinHudSelectedItemName {

	// Com 10 ou mais o nome já fica 100% opaco; 40 dá margem
	private static final int ckaddons$KEEP_VISIBLE = 40;

	@Shadow
	private int toolHighlightTimer;

	@Shadow
	private ItemStack lastToolHighlight;

	// Tool exp display: coloca a barra de exp da ferramenta na frente do nome
	@ModifyVariable(method = "extractSelectedItemName", at = @At("STORE"), ordinal = 0)
	private MutableComponent ckaddons$toolExpBar(MutableComponent name) {
		return ToolExpDisplay.decorate(this.lastToolHighlight, name);
	}

	@Inject(method = "tick()V", at = @At("RETURN"))
	private void ckaddons$keepSelectedItemName(CallbackInfo ci) {
		if (ModConfig.get().selectedItemNameAlwaysVisible && !this.lastToolHighlight.isEmpty()) {
			this.toolHighlightTimer = Math.max(this.toolHighlightTimer, ckaddons$KEEP_VISIBLE);
		}
	}
}
