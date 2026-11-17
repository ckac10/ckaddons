package com.example.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public class StaminaCrosshairRenderer {

    private static final int RADIUS = 6;
    private static final int THICKNESS = 2;

    private static final float OVAL_X = 2.5f; // horizontal
    private static final float OVAL_Y = 2.5f; // vertical

    // Cores sem transparência; a opacidade vem da config (staminaOpacity)
    private static final int COLOR_BAR = 0x00E87A; // verde
    private static final int COLOR_BG  = 0x000000; // preto

    // Hide bar out of combat: última vez (ms) que o player tomou dano
    private static long lastDamageMs = Long.MIN_VALUE;
    private static LocalPlayer trackedPlayer;
    private static float lastHealth;
    private static int lastHurtTime;

    public static void register() {
        Identifier id = Identifier.fromNamespaceAndPath("ckaddons", "stamina_crosshair");
        HudElementRegistry.addLast(id, StaminaCrosshairRenderer::render);

        ClientTickEvents.END_CLIENT_TICK.register(client -> trackDamage(client.player));
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();

        if (!ModConfig.get().staminaCrosshair) return;
        if (client.player == null) return;
        if (client.gui.hud.isHidden()) return;
        if (client.gui.screen() != null) return;
        if (ModConfig.get().staminaHideOutOfCombat && !inCombat()) return;

        float stamina = Mth.clamp(client.player.experienceProgress, 0.0f, 1.0f);

        int cx = client.getWindow().getGuiScaledWidth() / 2 - 1;
        int cy = client.getWindow().getGuiScaledHeight() / 2;

        float bottom = Mth.clamp(stamina / 0.5f, 0.0f, 1.0f);
        float top    = Mth.clamp((stamina - 0.5f) / 0.5f, 0.0f, 1.0f);

        drawSplitArc(graphics, cx, cy, 195, 345, bottom);
        drawSplitArc(graphics, cx, cy, 15, 165, top);
    }

    // Tomou dano: hurtTime recomeça (animação de dano) ou a vida caiu
    private static void trackDamage(LocalPlayer player) {
        if (player == null) {
            trackedPlayer = null;
            return;
        }
        if (player != trackedPlayer) { // entrou no mundo ou renasceu
            trackedPlayer = player;
            lastHealth = player.getHealth();
            lastHurtTime = player.hurtTime;
            return;
        }
        float health = player.getHealth();
        if (health < lastHealth || player.hurtTime > lastHurtTime) {
            lastDamageMs = System.currentTimeMillis();
        }
        lastHealth = health;
        lastHurtTime = player.hurtTime;
    }

    private static boolean inCombat() {
        if (lastDamageMs == Long.MIN_VALUE) return false;
        long seconds = Mth.clamp(ModConfig.get().staminaCombatTime, 1, 15);
        return System.currentTimeMillis() - lastDamageMs <= seconds * 1000L;
    }

    private static void drawSplitArc(GuiGraphicsExtractor graphics, int cx, int cy,
                                     float startDeg, float endDeg, float progress) {

        if (progress > 0.001f) {
            fillArc(graphics, cx, cy, startDeg, endDeg, 0.0f, progress, withAlpha(COLOR_BAR));
        }

        if (progress < 0.999f) {
            fillArc(graphics, cx, cy, startDeg, endDeg, progress, 1.0f, withAlpha(COLOR_BG));
        }
    }

    private static void fillArc(GuiGraphicsExtractor graphics, int cx, int cy,
                                float startDeg, float endDeg,
                                float fromProgress, float toProgress, int color) {

        if (toProgress - fromProgress <= 0.001f) return;

        float startRad = (float) Math.toRadians(startDeg);
        float spanRad  = (float) Math.toRadians(endDeg - startDeg);

        float from = normalize(startRad + spanRad * fromProgress);
        float to   = normalize(startRad + spanRad * toProgress);

        int outer = RADIUS + THICKNESS / 2;
        int inner = Math.max(1, RADIUS - THICKNESS / 2);

        int boundX = (int) (outer * OVAL_X) + 1;
        int boundY = (int) (outer * OVAL_Y) + 1;

        for (int y = -boundY; y <= boundY; y++) {
            for (int x = -boundX; x <= boundX; x++) {
                float nx = x / OVAL_X;
                float ny = y / OVAL_Y;
                float dist = (float) Math.sqrt(nx * nx + ny * ny);

                if (dist > outer || dist < inner) continue;

                float angle = normalize((float) Math.atan2(ny, nx));

                boolean inside;
                if (fromProgress > 0.001f) {
                    inside = isAngleInArcExclusiveStart(angle, from, to);
                } else {
                    inside = isAngleInArc(angle, from, to);
                }

                if (inside) {
                    graphics.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, color);
                }
            }
        }
    }

    // Aplica a opacidade da config (0-100%) a uma cor RGB
    private static int withAlpha(int rgb) {
        int opacity = Mth.clamp(ModConfig.get().staminaOpacity, 0, 100);
        int alpha = Math.round(opacity * 255 / 100.0f);
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    private static float normalize(float angle) {
        float twoPi = (float) (Math.PI * 2);
        angle %= twoPi;
        if (angle < 0) angle += twoPi;
        return angle;
    }

    private static boolean isAngleInArc(float angle, float start, float end) {
        if (start <= end) return angle >= start && angle <= end;
        return angle >= start || angle <= end;
    }

    private static boolean isAngleInArcExclusiveStart(float angle, float start, float end) {
        if (start <= end) return angle > start && angle <= end;
        return angle > start || angle <= end;
    }
}
