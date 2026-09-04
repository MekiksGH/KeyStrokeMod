package com.mekiks.keystrokemod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * The editor (opened with the keybind): the overlay's border and a small resize
 * grip are shown ONLY here, never during normal play. Drag the body to move it —
 * it snaps to the screen edges and centre with guide lines — and drag the grip
 * to resize it. The grip sits on whichever corner faces the screen centre, so it
 * stays reachable in any corner. Done saves.
 */
public class KeyStrokeScreen extends Screen {

    private static final int HANDLE = 6;             // small resize grip (screen px)
    private static final float SNAP = 6f;            // snap distance (screen px)
    private static final int FRAME = 0x90FFFFFF;     // editor-only border
    private static final int GRIP = 0xFFFFFFFF;      // resize grip
    private static final int GUIDE = 0xC066CCFF;     // snap guide lines

    private enum Mode { NONE, MOVE, RESIZE }

    private Mode mode = Mode.NONE;
    private double grabOffsetX;
    private double grabOffsetY;
    private boolean guideV;
    private boolean guideH;
    private float hover;   // 0..1 fade for the cursor-over-box highlight

    // resize anchor = the corner facing the screen edge, held fixed while resizing
    private double resizeAnchorX;
    private double resizeAnchorY;
    private boolean resizeRight;
    private boolean resizeBottom;

    public KeyStrokeScreen() {
        super(Text.translatable("screen.keystrokemod.title"));
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> close())
                .dimensions(this.width / 2 - 100, this.height - 28, 200, 20)
                .build());
    }

    private float frameW() {
        return KeystrokesRenderer.FRAME_W * KeyStrokeConfig.scale;
    }

    private float frameH() {
        return KeystrokesRenderer.FRAME_H * KeyStrokeConfig.scale;
    }

    // Which half of the screen the overlay sits in — decides the grip corner.
    private boolean overlayRight() {
        return KeyStrokeConfig.x + frameW() / 2f > this.width / 2f;
    }

    private boolean overlayBottom() {
        return KeyStrokeConfig.y + frameH() / 2f > this.height / 2f;
    }

    // Grip is on the corner nearest the screen centre (inner corner).
    private int handleLeft() {
        return overlayRight()
                ? Math.round(KeyStrokeConfig.x)
                : Math.round(KeyStrokeConfig.x + frameW()) - HANDLE;
    }

    private int handleTop() {
        return overlayBottom()
                ? Math.round(KeyStrokeConfig.y)
                : Math.round(KeyStrokeConfig.y + frameH()) - HANDLE;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        MinecraftClient client = MinecraftClient.getInstance();
        boolean[] states = KeyStrokeMod.currentStates(client.options);
        KeystrokesRenderer.render(ctx, this.textRenderer,
                KeyStrokeConfig.x, KeyStrokeConfig.y, KeyStrokeConfig.scale, states);

        // hover highlight — the box whitens a bit while the cursor is over it (fades in/out)
        boolean over = overBox(mouseX, mouseY);
        hover += ((over ? 1f : 0f) - hover) * 0.25f;
        if (hover > 0.02f) {
            int bx = Math.round(KeyStrokeConfig.x);
            int by = Math.round(KeyStrokeConfig.y);
            int bw = Math.round(frameW());
            int bh = Math.round(frameH());
            int a = (int) (hover * 0x33);
            ctx.fill(bx, by, bx + bw, by + bh, (a << 24) | 0x00FFFFFF);
        }

        if (mode == Mode.MOVE) {
            if (guideV) ctx.fill(this.width / 2, 0, this.width / 2 + 1, this.height, GUIDE);
            if (guideH) ctx.fill(0, this.height / 2, this.width, this.height / 2 + 1, GUIDE);
        }

        int x = Math.round(KeyStrokeConfig.x);
        int y = Math.round(KeyStrokeConfig.y);
        int w = Math.round(frameW());
        int h = Math.round(frameH());
        KeystrokesRenderer.outline(ctx, x, y, w, h, FRAME);
        int hl = handleLeft();
        int ht = handleTop();
        ctx.fill(hl, ht, hl + HANDLE, ht + HANDLE, GRIP);

        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("screen.keystrokemod.hint"), this.width / 2, 16, 0xFFFFFFFF);
    }

    private boolean overHandle(double mx, double my) {
        int hl = handleLeft();
        int ht = handleTop();
        return mx >= hl && mx <= hl + HANDLE && my >= ht && my <= ht + HANDLE;
    }

    private boolean overBox(double mx, double my) {
        return mx >= KeyStrokeConfig.x && mx <= KeyStrokeConfig.x + frameW()
                && my >= KeyStrokeConfig.y && my <= KeyStrokeConfig.y + frameH();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        if (click.button() == 0) {
            if (overHandle(mx, my)) {
                mode = Mode.RESIZE;
                resizeRight = overlayRight();
                resizeBottom = overlayBottom();
                resizeAnchorX = resizeRight ? KeyStrokeConfig.x + frameW() : KeyStrokeConfig.x;
                resizeAnchorY = resizeBottom ? KeyStrokeConfig.y + frameH() : KeyStrokeConfig.y;
                return true;
            }
            if (overBox(mx, my)) {
                mode = Mode.MOVE;
                grabOffsetX = mx - KeyStrokeConfig.x;
                grabOffsetY = my - KeyStrokeConfig.y;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        double mx = click.x();
        double my = click.y();

        if (mode == Mode.MOVE) {
            float w = frameW();
            float h = frameH();
            float rx = (float) (mx - grabOffsetX);
            float ry = (float) (my - grabOffsetY);
            float cx = this.width / 2f;
            float cy = this.height / 2f;
            guideV = false;
            guideH = false;

            if (Math.abs(rx) <= SNAP) {
                rx = 0;
            } else if (Math.abs(rx + w - this.width) <= SNAP) {
                rx = this.width - w;
            } else if (Math.abs(rx + w / 2f - cx) <= SNAP) {
                rx = cx - w / 2f;
                guideV = true;
            }

            if (Math.abs(ry) <= SNAP) {
                ry = 0;
            } else if (Math.abs(ry + h - this.height) <= SNAP) {
                ry = this.height - h;
            } else if (Math.abs(ry + h / 2f - cy) <= SNAP) {
                ry = cy - h / 2f;
                guideH = true;
            }

            KeyStrokeConfig.x = clampX(rx);
            KeyStrokeConfig.y = clampY(ry);
            return true;
        }

        if (mode == Mode.RESIZE) {
            float newFrameW = (float) Math.abs(mx - resizeAnchorX);
            float scale = KeyStrokeConfig.clampScale(newFrameW / KeystrokesRenderer.FRAME_W);
            KeyStrokeConfig.scale = scale;
            float fw = KeystrokesRenderer.FRAME_W * scale;
            float fh = KeystrokesRenderer.FRAME_H * scale;
            KeyStrokeConfig.x = clampX(resizeRight ? (float) resizeAnchorX - fw : (float) resizeAnchorX);
            KeyStrokeConfig.y = clampY(resizeBottom ? (float) resizeAnchorY - fh : (float) resizeAnchorY);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        mode = Mode.NONE;
        guideV = false;
        guideH = false;
        return super.mouseReleased(click);
    }

    private float clampX(float x) {
        return Math.max(0f, Math.min(this.width - frameW(), x));
    }

    private float clampY(float y) {
        return Math.max(0f, Math.min(this.height - frameH(), y));
    }

    @Override
    public void close() {
        KeyStrokeConfig.save();
        if (this.client != null) {
            this.client.setScreen(null);
        }
    }
}
