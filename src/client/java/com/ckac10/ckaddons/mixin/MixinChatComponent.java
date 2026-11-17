package com.ckac10.ckaddons.mixin;

import com.ckac10.ckaddons.CompactChat;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Compact chat: junta mensagens repetidas em uma só com contador "(2)", "(3)"...
@Mixin(ChatComponent.class)
public class MixinChatComponent {

	@ModifyVariable(method = "addMessage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private Component ckaddons$compact(Component content) {
		return CompactChat.process(content);
	}

	@ModifyExpressionValue(
			method = "addMessage",
			at = @At(value = "NEW", target = "net/minecraft/client/multiplayer/chat/GuiMessage")
	)
	private GuiMessage ckaddons$trackMessage(GuiMessage message) {
		return CompactChat.track(message);
	}

	@Inject(method = "clearMessages", at = @At("TAIL"))
	private void ckaddons$clearCompactCache(boolean history, CallbackInfo ci) {
		CompactChat.clear();
	}
}
