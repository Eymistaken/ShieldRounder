package com.eymistaken.shieldrounder.config;

import com.eymistaken.shieldrounder.ShieldRounderClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ShieldRounderConfig {
	public static final int MIN_COLOR = 0;
	public static final int MAX_COLOR = 255;
	public static final float MIN_ALPHA = 0.0F;
	public static final float MAX_ALPHA = 1.0F;
	public static final float MIN_LINE_WIDTH = 0.5F;
	public static final float MAX_LINE_WIDTH = 5.0F;
	public static final int MIN_MERIDIANS = 4;
	public static final int MAX_MERIDIANS = 32;
	public static final int MIN_RINGS = 2;
	public static final int MAX_RINGS = 16;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(ShieldRounderClient.MOD_ID + ".json");
	private static ShieldRounderConfig instance = new ShieldRounderConfig();

	public boolean enabled = true;
	public boolean includeSelf = true;
	public RenderMode renderMode = RenderMode.WIREFRAME;
	public int red = 35;
	public int green = 235;
	public int blue = 220;
	public float alpha = 170.0F / 255.0F;
	public float lineWidth = 1.5F;
	public int meridians = 12;
	public int rings = 5;
	public boolean dynamicDurabilityColor = false;
	public boolean blockFlash = false;
	public boolean deploymentAnimation = false;
	public boolean enchantmentGlint = false;

	public static ShieldRounderConfig get() {
		return instance;
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			ShieldRounderConfig loaded = GSON.fromJson(reader, ShieldRounderConfig.class);
			instance = loaded == null ? new ShieldRounderConfig() : loaded;
		} catch (IOException | RuntimeException ignored) {
			instance = new ShieldRounderConfig();
		}
		instance.clampValues();
	}

	public static void save() {
		instance.clampValues();
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException ignored) {
		}
	}

	public int alphaByte() {
		return Math.round(alpha * MAX_COLOR);
	}

	private void clampValues() {
		if (renderMode == null) {
			renderMode = RenderMode.WIREFRAME;
		}
		red = clamp(red, MIN_COLOR, MAX_COLOR);
		green = clamp(green, MIN_COLOR, MAX_COLOR);
		blue = clamp(blue, MIN_COLOR, MAX_COLOR);
		alpha = clamp(alpha, MIN_ALPHA, MAX_ALPHA);
		lineWidth = clamp(lineWidth, MIN_LINE_WIDTH, MAX_LINE_WIDTH);
		meridians = clamp(meridians, MIN_MERIDIANS, MAX_MERIDIANS);
		rings = clamp(rings, MIN_RINGS, MAX_RINGS);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static float clamp(float value, float min, float max) {
		if (Float.isNaN(value)) {
			return min;
		}
		return Math.max(min, Math.min(max, value));
	}

	public enum RenderMode {
		WIREFRAME("shieldrounder.config.render_mode.wireframe"),
		SOLID("shieldrounder.config.render_mode.solid"),
		BOTH("shieldrounder.config.render_mode.both");

		private final String translationKey;

		RenderMode(String translationKey) {
			this.translationKey = translationKey;
		}

		public String translationKey() {
			return translationKey;
		}

		public RenderMode next() {
			RenderMode[] values = values();
			return values[(ordinal() + 1) % values.length];
		}
	}
}
