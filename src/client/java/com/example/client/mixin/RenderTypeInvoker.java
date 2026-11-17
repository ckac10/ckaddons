package com.example.client.mixin;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** RenderType.create não é público: usado pelo Render Coords Waypoint para criar os próprios RenderTypes. */
@Mixin(RenderType.class)
public interface RenderTypeInvoker {

	@Invoker("create")
	static RenderType ckaddons$create(String name, RenderSetup setup) {
		throw new AssertionError();
	}
}
