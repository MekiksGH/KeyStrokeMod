package com.mekiks.keystrokemod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * KeyStrokeMod — a movable, resizable keystrokes overlay.
 *
 * <p>Renders the WASD keys, the space bar and LMB/RMB, each lighting up while
 * held. A keybind (registered under its own "KeyStrokeMod" category in
 * Controls) opens an editor where the overlay can be dragged to any position
 * and resized with a corner grip.</p>
 */
public class KeyStrokeMod implements ClientModInitializer {

    public static final String MOD_ID = "keystrokemod";

    private static KeyBinding openEditorKey;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        KeyStrokeConfig.load();

        // Custom category so the binding shows under "KeyStrokeMod" in Controls.
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
        openEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.keystrokemod.open_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                category));

        // A single toggle keybind (unbound by default) that shows/hides the overlay.
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.keystrokemod.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                category));

        HudElementRegistry.addLast(Identifier.of(MOD_ID, "keystrokes"), (context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden || !KeyStrokeConfig.enabled) {
                return;
            }
            KeystrokesRenderer.render(context, client.textRenderer,
                    KeyStrokeConfig.x, KeyStrokeConfig.y, KeyStrokeConfig.scale,
                    currentStates(client.options));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openEditorKey.wasPressed()) {
                client.setScreen(new KeyStrokeScreen());
            }
            while (toggleKey.wasPressed()) {
                KeyStrokeConfig.enabled = !KeyStrokeConfig.enabled;
                KeyStrokeConfig.save();
            }
        });
    }

    /** Live pressed states, in the order the renderer expects. */
    static boolean[] currentStates(GameOptions o) {
        return new boolean[]{
                o.forwardKey.isPressed(),
                o.leftKey.isPressed(),
                o.backKey.isPressed(),
                o.rightKey.isPressed(),
                o.jumpKey.isPressed(),
                o.attackKey.isPressed(),
                o.useKey.isPressed(),
        };
    }
}
