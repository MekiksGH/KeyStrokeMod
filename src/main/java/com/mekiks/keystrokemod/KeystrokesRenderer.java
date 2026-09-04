package com.mekiks.keystrokemod;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Draws the keystrokes overlay. The layout is defined in base (unscaled) pixels
 * with its origin at the top-left of the outer frame; the caller supplies the
 * on-screen position and scale, applied via the GUI matrix stack.
 *
 * <p>Pressed-state array order: {@code [W, A, S, D, Space, LMB, RMB]}.</p>
 */
public final class KeystrokesRenderer {

    private static final int PAD = 5;                       // frame padding
    private static final int KEY = 22;                      // square key size
    private static final int GAP = 2;                       // gap between keys
    private static final int BLOCK_W = 3 * KEY + 2 * GAP;   // 70
    private static final int SPACE_H = 10;                  // space bar height
    private static final int BTN_W = (BLOCK_W - GAP) / 2;   // 34 (LMB / RMB)
    private static final int BLOCK_H = 2 * (KEY + GAP) + SPACE_H + GAP + KEY; // 82

    /** Full frame dimensions (unscaled) — used by the editor for hit-testing. */
    public static final int FRAME_W = BLOCK_W + 2 * PAD;    // 80
    public static final int FRAME_H = BLOCK_H + 2 * PAD;    // 92

    private static final int FRAME_BORDER = 0x80FFFFFF;
    private static final int BG_UP = 0xC0303030;
    private static final int BG_DOWN = 0xF0FFFFFF;
    private static final int BORDER_UP = 0x50FFFFFF;
    private static final int BORDER_DOWN = 0xFFFFFFFF;
    private static final int TEXT_UP = 0xFFFFFFFF;
    private static final int TEXT_DOWN = 0xFF1E1E1E;

    private KeystrokesRenderer() {
    }

    public static void render(DrawContext ctx, TextRenderer tr, float x, float y, float scale, boolean[] p) {
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(scale, scale);

        outline(ctx, 0, 0, FRAME_W, FRAME_H, FRAME_BORDER);

        int row1 = PAD + KEY + GAP;
        int row2 = PAD + 2 * (KEY + GAP);
        int row3 = row2 + SPACE_H + GAP;

        key(ctx, tr, "W", PAD + KEY + GAP, PAD, KEY, KEY, p[0]);
        key(ctx, tr, "A", PAD, row1, KEY, KEY, p[1]);
        key(ctx, tr, "S", PAD + KEY + GAP, row1, KEY, KEY, p[2]);
        key(ctx, tr, "D", PAD + 2 * (KEY + GAP), row1, KEY, KEY, p[3]);
        key(ctx, tr, null, PAD, row2, BLOCK_W, SPACE_H, p[4]);
        key(ctx, tr, "LMB", PAD, row3, BTN_W, KEY, p[5]);
        key(ctx, tr, "RMB", PAD + BTN_W + GAP, row3, BTN_W, KEY, p[6]);

        m.popMatrix();
    }

    private static void key(DrawContext ctx, TextRenderer tr, String label,
                            int kx, int ky, int kw, int kh, boolean pressed) {
        ctx.fill(kx, ky, kx + kw, ky + kh, pressed ? BG_DOWN : BG_UP);
        outline(ctx, kx, ky, kw, kh, pressed ? BORDER_DOWN : BORDER_UP);
        if (label != null) {
            int tx = kx + (kw - tr.getWidth(label)) / 2;
            int ty = ky + (kh - tr.fontHeight) / 2 + 1;
            ctx.drawText(tr, label, tx, ty, pressed ? TEXT_DOWN : TEXT_UP, !pressed);
        }
    }

    /** Draws a 1px rectangle outline using edge fills ({@code drawBorder} was removed in 1.21.11). */
    public static void outline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);                 // top
        ctx.fill(x, y + h - 1, x + w, y + h, color);         // bottom
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);         // left
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color); // right
    }
}
