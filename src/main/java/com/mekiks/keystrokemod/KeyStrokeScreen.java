package com.mekiks.keystrokemod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * The editor screen: drag the overlay to move it, drag the bottom-right grip to
 * resize it, and press Done to save. Shows a live preview the whole time.
 */
public class KeyStrokeScreen extends Screen {

    private static final int HANDLE = 8;             // screen-space size of the resize grip
    private static final int ACCENT = 0xFFFFDD55;    // outline / grip colour

    private enum Mode { NONE, MOVE, RESIZE }

    private Mode mode = Mode.NONE;
    private double grabOffsetX;
    private double grabOffsetY;

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

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        MinecraftClient client = MinecraftClient.getInstance();
        boolean[] states = KeyStrokeMod.currentStates(client.options);
        KeystrokesRenderer.render(ctx, this.textRenderer,
                KeyStrokeConfig.x, KeyStrokeConfig.y, KeyStrokeConfig.scale, states);

        int x = Math.round(KeyStrokeConfig.x);
        int y = Math.round(KeyStrokeConfig.y);
        int w = Math.round(frameW());
        int h = Math.round(frameH());
        KeystrokesRenderer.outline(ctx, x - 1, y - 1, w + 2, h + 2, ACCENT);
        ctx.fill(x + w - HANDLE, y + h - HANDLE, x + w, y + h, ACCENT);

        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("screen.keystrokemod.hint"), this.width / 2, 16, 0xFFFFFFFF);
    }

    private boolean overHandle(double mx, double my) {
        double hx = KeyStrokeConfig.x + frameW();
        double hy = KeyStrokeConfig.y + frameH();
        return mx >= hx - HANDLE && mx <= hx && my >= hy - HANDLE && my <= hy;
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
            KeyStrokeConfig.x = clampX((float) (mx - grabOffsetX));
            KeyStrokeConfig.y = clampY((float) (my - grabOffsetY));
            return true;
        }
        if (mode == Mode.RESIZE) {
            float byWidth = (float) (mx - KeyStrokeConfig.x) / KeystrokesRenderer.FRAME_W;
            KeyStrokeConfig.scale = KeyStrokeConfig.clampScale(byWidth);
            KeyStrokeConfig.x = clampX(KeyStrokeConfig.x);
            KeyStrokeConfig.y = clampY(KeyStrokeConfig.y);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        mode = Mode.NONE;
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
