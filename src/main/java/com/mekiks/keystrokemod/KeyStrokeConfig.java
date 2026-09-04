package com.mekiks.keystrokemod;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Persists the overlay's on-screen position (top-left of the frame, in scaled
 * GUI pixels) and its scale to {@code config/keystrokemod.properties}.
 */
public final class KeyStrokeConfig {

    public static final float MIN_SCALE = 0.5f;
    public static final float MAX_SCALE = 3.0f;

    private static final Path PATH =
            FabricLoader.getInstance().getConfigDir().resolve("keystrokemod.properties");

    public static float x = 10f;
    public static float y = 10f;
    public static float scale = 1.0f;
    public static boolean enabled = true;

    private KeyStrokeConfig() {
    }

    public static void load() {
        if (!Files.exists(PATH)) {
            save();
            return;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(PATH)) {
            p.load(in);
            x = Float.parseFloat(p.getProperty("x", "10"));
            y = Float.parseFloat(p.getProperty("y", "10"));
            scale = clampScale(Float.parseFloat(p.getProperty("scale", "1.0")));
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
        } catch (IOException | NumberFormatException e) {
            // keep defaults on any read/parse error
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("x", String.valueOf(x));
        p.setProperty("y", String.valueOf(y));
        p.setProperty("scale", String.valueOf(scale));
        p.setProperty("enabled", String.valueOf(enabled));
        try (OutputStream out = Files.newOutputStream(PATH)) {
            p.store(out, "KeyStrokeMod - overlay position (x,y) and scale");
        } catch (IOException ignored) {
        }
    }

    public static float clampScale(float s) {
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, s));
    }
}
