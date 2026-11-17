package com.ckac10.ckaddons;

import com.ckac10.ckaddons.mixin.RenderTypeInvoker;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renderização adaptada do IQ (https://github.com/iqaddons/IQ, Apache License 2.0):
 * WorldRenderUtils/WorldRenderEvent/LevelRendererMixin, simplificada para este mod.
 *
 * Render chests waypoints: quando chega a mensagem
 * "The key unlocks a nearby vault at X, Y, Z!", mostra um waypoint "Chest" nessa coordenada por
 * chestsWaypointTime segundos (0 = até trocar de mundo). O waypoint aparece através das paredes, com caixa,
 * feixe de luz, nome e distância, e a cor muda com a distância (verde perto, vermelho longe).
 */
public final class CoordsWaypoint {

	private static final int BEAM_HEIGHT = 100;
	// Texto mais longe que isso é desenhado nessa distância (aumentado), para não sumir no limite da visão
	private static final double MAX_TEXT_DISTANCE = 48.0;

	// Render chests waypoints: "The key unlocks a nearby vault at -1089, 178, -762!"
	private static final Pattern VAULT_PATTERN = Pattern.compile(
			"unlocks a nearby vault at\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)", Pattern.CASE_INSENSITIVE);

	/** expiresAt em System.currentTimeMillis(); Long.MAX_VALUE = não expira. */
	private record ChestWaypoint(BlockPos pos, long expiresAt) {
	}

	private static final List<ChestWaypoint> chests = new ArrayList<>();
	private static ClientLevel chestsLevel;

	private CoordsWaypoint() {
	}

	// ===== Chat (Render chests waypoints) =====

	public static void onGameMessage(Component message) {
		if (!ModConfig.get().renderChestsWaypoints) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		String text = ChatFormatting.stripFormatting(message.getString());
		if (text == null) return;
		Matcher matcher = VAULT_PATTERN.matcher(text);
		if (!matcher.find()) return;

		BlockPos pos;
		try {
			pos = new BlockPos(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
					Integer.parseInt(matcher.group(3)));
		} catch (NumberFormatException e) {
			return;
		}

		if (chestsLevel != mc.level) {
			chests.clear();
			chestsLevel = mc.level;
		}
		int seconds = ModConfig.get().chestsWaypointTime;
		long expiresAt = seconds <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + seconds * 1000L;
		chests.removeIf(chest -> chest.pos().equals(pos)); // mesma mensagem de novo: só renova o tempo
		chests.add(new ChestWaypoint(pos, expiresAt));
	}

	// ===== Render (chamado pelo MixinLevelRendererWaypoint) =====

	public static void render(CameraRenderState camera, SubmitNodeCollector collector) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) return;

		// Waypoints dos vaults (somem depois do tempo escolhido ou ao trocar de mundo)
		if (chestsLevel != mc.level) chests.clear();
		long now = System.currentTimeMillis();
		chests.removeIf(chest -> now >= chest.expiresAt());
		if (ModConfig.get().renderChestsWaypoints) {
			for (ChestWaypoint chest : chests) {
				renderWaypoint(camera, collector, mc, chest.pos(), "Chest");
			}
		}
	}

	private static void renderWaypoint(CameraRenderState camera, SubmitNodeCollector collector, Minecraft mc,
									   BlockPos waypoint, String label) {
		Vec3 bottom = Vec3.atBottomCenterOf(waypoint);
		double distance = bottom.distanceTo(mc.player.position());
		int rgb = distanceColor(distance);

		AABB box = new AABB(waypoint);
		AABB beam = AABB.ofSize(new Vec3(bottom.x, box.maxY, bottom.z), 0.5, 0, 0.5).expandTowards(0, BEAM_HEIGHT, 0);

		// Caixa + feixe (posições relativas à câmera)
		Vec3 cam = camera.pos;
		PoseStack pose = new PoseStack();
		pose.translate(-cam.x, -cam.y, -cam.z);
		submitGeometry(collector, pose, Layers.FILLED, (entry, buffer) -> {
			filledBox(entry, buffer, box, rgb, 0.25f);
			filledBox(entry, buffer, beam, rgb, 0.45f);
		});
		submitGeometry(collector, pose, Layers.LINES, (entry, buffer) -> outlineBox(entry, buffer, box, 0xFF000000 | rgb, 2.0f));

		// Nome e distância acima da caixa
		Vec3 textPos = Vec3.atCenterOf(waypoint).add(0, 1.2, 0);
		drawText(collector, camera, textPos, Component.literal(label), 0xFF000000 | rgb, 0);
		drawText(collector, camera, textPos, Component.literal(String.format("%.1fm", distance)), 0xFFFFFFFF, 10);
	}

	private static int distanceColor(double distance) {
		if (distance < 10) return 0x55FF55;
		if (distance < 25) return 0x00AA00;
		if (distance < 50) return 0xFFFF55;
		if (distance < 100) return 0xFFAA00;
		if (distance < 200) return 0xFF5555;
		return 0xAA0000;
	}

	// ===== Envio para o renderizador (mesmo esquema do IQ: depois do terreno / texto see-through) =====

	private static void submitGeometry(SubmitNodeCollector collector, PoseStack pose, RenderType type,
									   SubmitNodeCollector.CustomGeometryRenderer renderer) {
		if (collector.order(0) instanceof SubmitNodeCollection nodes) {
			nodes.afterTerrain.submit(new CustomFeatureRenderer.Submit(pose.last().copy(), type, renderer));
		} else {
			collector.submitCustomGeometry(pose, type, renderer);
		}
	}

	private static void drawText(SubmitNodeCollector collector, CameraRenderState camera, Vec3 pos,
								 Component text, int argb, float lineOffset) {
		Vec3 offset = pos.subtract(camera.pos);
		double distance = offset.length();
		if (distance > MAX_TEXT_DISTANCE) {
			offset = offset.scale(MAX_TEXT_DISTANCE / distance);
			distance = MAX_TEXT_DISTANCE;
		}
		// Tamanho na tela mais ou menos constante, sem ficar gigante de perto
		float scale = (float) (0.025 * Math.max(1.0, distance / 8.0));

		PoseStack pose = new PoseStack();
		pose.translate(offset.x, offset.y, offset.z);
		pose.mulPose(new Matrix4f().rotation(camera.orientation));
		pose.scale(scale, -scale, scale);

		Font font = Minecraft.getInstance().font;
		FormattedCharSequence chars = text.getVisualOrderText();
		float x = -font.width(text) / 2f;
		int background = 0x40000000;

		if (collector.order(0) instanceof SubmitNodeCollection nodes) {
			var content = new TextFeatureRenderer.Content.Text(x, lineOffset, chars, false, argb, background, 0);
			nodes.seeThrough.submit(new TextFeatureRenderer.Submit(
					new Matrix4f(pose.last().pose()), Font.DisplayMode.SEE_THROUGH, LightCoordsUtil.FULL_BRIGHT, content));
		} else {
			collector.submitText(pose, x, lineOffset, chars, false, Font.DisplayMode.SEE_THROUGH,
					LightCoordsUtil.FULL_BRIGHT, argb, background, 0);
		}
	}

	// ===== Geometria =====

	private static void filledBox(PoseStack.Pose e, VertexConsumer b, AABB box, int rgb, float alpha) {
		float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
		float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;
		float r = ((rgb >> 16) & 0xFF) / 255f, g = ((rgb >> 8) & 0xFF) / 255f, bl = (rgb & 0xFF) / 255f;
		quad(b, e, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, r, g, bl, alpha);
		quad(b, e, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, bl, alpha);
		quad(b, e, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, r, g, bl, alpha);
		quad(b, e, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, bl, alpha);
		quad(b, e, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, r, g, bl, alpha);
		quad(b, e, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, bl, alpha);
	}

	private static void quad(VertexConsumer b, PoseStack.Pose e,
							 float x1, float y1, float z1, float x2, float y2, float z2,
							 float x3, float y3, float z3, float x4, float y4, float z4,
							 float r, float g, float bl, float a) {
		b.addVertex(e, x1, y1, z1).setColor(r, g, bl, a);
		b.addVertex(e, x2, y2, z2).setColor(r, g, bl, a);
		b.addVertex(e, x3, y3, z3).setColor(r, g, bl, a);
		b.addVertex(e, x4, y4, z4).setColor(r, g, bl, a);
	}

	private static void outlineBox(PoseStack.Pose e, VertexConsumer b, AABB box, int argb, float width) {
		float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
		float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;
		// base
		line(e, b, x0, y0, z0, x1, y0, z0, argb, width);
		line(e, b, x1, y0, z0, x1, y0, z1, argb, width);
		line(e, b, x1, y0, z1, x0, y0, z1, argb, width);
		line(e, b, x0, y0, z1, x0, y0, z0, argb, width);
		// topo
		line(e, b, x0, y1, z0, x1, y1, z0, argb, width);
		line(e, b, x1, y1, z0, x1, y1, z1, argb, width);
		line(e, b, x1, y1, z1, x0, y1, z1, argb, width);
		line(e, b, x0, y1, z1, x0, y1, z0, argb, width);
		// colunas
		line(e, b, x0, y0, z0, x0, y1, z0, argb, width);
		line(e, b, x1, y0, z0, x1, y1, z0, argb, width);
		line(e, b, x1, y0, z1, x1, y1, z1, argb, width);
		line(e, b, x0, y0, z1, x0, y1, z1, argb, width);
	}

	private static void line(PoseStack.Pose e, VertexConsumer b,
							 float x1, float y1, float z1, float x2, float y2, float z2, int argb, float width) {
		Vector3f normal = new Vector3f(x2 - x1, y2 - y1, z2 - z1).normalize();
		b.addVertex(e, x1, y1, z1).setColor(argb).setNormal(e, normal).setLineWidth(width);
		b.addVertex(e, x2, y2, z2).setColor(argb).setNormal(e, normal).setLineWidth(width);
	}

	// ===== Pipelines / RenderTypes que ignoram profundidade (aparecem através das paredes) =====

	private static final class Layers {

		private static final RenderPipeline FILLED_PIPELINE = RenderPipelines.register(
				RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
						.withLocation(Identifier.fromNamespaceAndPath("ckaddons", "pipeline/waypoint_filled"))
						.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
						.withPrimitiveTopology(PrimitiveTopology.QUADS)
						.withCull(false)
						.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
						.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
						.build());

		private static final RenderPipeline LINES_PIPELINE = RenderPipelines.register(
				RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
						.withLocation(Identifier.fromNamespaceAndPath("ckaddons", "pipeline/waypoint_lines"))
						.withCull(false)
						.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
						.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
						.build());

		static final RenderType FILLED = RenderTypeInvoker.ckaddons$create("ckaddons_waypoint_filled",
				RenderSetup.builder(FILLED_PIPELINE)
						.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
						.createRenderSetup());

		static final RenderType LINES = RenderTypeInvoker.ckaddons$create("ckaddons_waypoint_lines",
				RenderSetup.builder(LINES_PIPELINE)
						.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
						.createRenderSetup());
	}
}
