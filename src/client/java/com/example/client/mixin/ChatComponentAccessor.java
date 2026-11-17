package com.example.client.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

// Compact chat: acesso às listas de mensagens do chat para apagar a mensagem repetida anterior
@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {

	@Accessor("allMessages")
	List<GuiMessage> ckaddons$getAllMessages();

	@Accessor("trimmedMessages")
	List<GuiMessage.Line> ckaddons$getTrimmedMessages();
}
