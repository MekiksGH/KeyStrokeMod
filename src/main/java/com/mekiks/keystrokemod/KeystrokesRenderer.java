package com.mekiks.keystrokemod;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Draws the keystrokes overlay: transparent dark keys with no borders, bigger
 * centred labels, and the space bar shown as a solid centred bar. Layout is in
 * base (unscaled) pixels with its origin at the top-left of the (invisible)
 * frame; the caller supplies the on-screen position and scale, applied via the
 * GUI matrix stack.
 *
 * <p>Pressed-state array order: {@code [W, A, S, D, Space, LMB, RMB]}.</p>
 */
public final class KeystrokesRenderer {

    private static final int PAD = 5;                       // frame padding (invisible, for hit area)
    private static final int KEY = 22;                      // square key size
    private static final int GAP = 2;                       // gap between keys
    private static final int BLOCK_W = 3 * KEY + 2 * GAP;   // 70
    private static final int SPACE_H = 10;                  // space bar height
    private static final int BTN_W = (BLOCK_W - GAP) / 2;   // 34 (LMB / RMB)
    private static final int BLOCK_H = 2 * (KEY + GAP) + SPACE_H + GAP + KEY; // 82

    /** Full frame dimensions (unscaled) — used by the editor for hit-testing. */
    public static final int FRAME_W = BLOCK_W + 2 * PAD;    // 80
    public static final int FRAME_H = BLOCK_H + 2 * PAD;    // 92

    private static final float TEXT_SCALE = 1.3f;           // labels a touch bigger than the base font
    private static final int BG_UP = 0x80000000;            // more transparent dark
    private static final int BG_DOWN = 0xF0FFFFFF;          // pressed = light
    private static final int FG_UP = 0xFFFFFFFF;            // text / space bar
    private static final int FG_DOWN = 0xFF1E1E1E;

    private KeystrokesRenderer() {
    }

    public static void render(DrawContext ctx, TextRenderer tr, float x, float y, float scale, boolean[] p) {
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(scale, scale);

        int row1 = PAD + KEY + GAP;
        int row2 = PAD + 2 * (KEY + GAP);
        int row3 = row2 + SPACE_H + GAP;

        key(ctx, tr, "W", PAD + KEY + GAP, PAD, KEY, KEY, p[0]);
        key(ctx, tr, "A", PAD, row1, KEY, KEY, p[1]);
        key(ctx, tr, "S", PAD + KEY + GAP, row1, KEY, KEY, p[2]);
        key(ctx, tr, "D", PAD + 2 * (KEY + GAP), row1, KEY, KEY, p[3]);
        spaceBar(ctx, PAD, row2, BLOCK_W, SPACE_H, p[4]);
        key(ctx, tr, "LMB", PAD, row3, BTN_W, KEY, p[5]);
        key(ctx, tr, "RMB", PAD + BTN_W + GAP, row3, BTN_W, KEY, p[6]);

        m.popMatrix();
    }

    private static void key(DrawContext ctx, TextRenderer tr, String label,
                            int kx, int ky, int kw, int kh, boolean pressed) {
        ctx.fill(kx, ky, kx + kw, ky + kh, pressed ? BG_DOWN : BG_UP);
        label(ctx, tr, label, kx + kw / 2f, ky + kh / 2f, pressed);
    }

    /** The space bar: a flat key with a thick, solid, centred bar. */
    private static void spaceBar(DrawContext ctx, int kx, int ky, int kw, int kh, boolean pressed) {
        ctx.fill(kx, ky, kx + kw, ky + kh, pressed ? BG_DOWN : BG_UP);
        int lineW = kw * 7 / 10;
        int lx = kx + (kw - lineW) / 2;
        int ly = ky + kh / 2 - 1;
        ctx.fill(lx, ly, lx + lineW, ly + 2, pressed ? FG_DOWN : FG_UP);
    }

    /** Draws a label centred on (cx, cy), scaled up by {@link #TEXT_SCALE}. */
    private static void label(DrawContext ctx, TextRenderer tr, String text, float cx, float cy, boolean pressed) {
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(cx, cy);
        m.scale(TEXT_SCALE, TEXT_SCALE);
        int tx = -tr.getWidth(text) / 2;
        int ty = -tr.fontHeight / 2;
        ctx.drawText(tr, text, tx, ty, pressed ? FG_DOWN : FG_UP, !pressed);
        m.popMatrix();
    }

    /** Draws a 1px rectangle outline using edge fills (used only by the editor). */
    public static void outline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }
}
