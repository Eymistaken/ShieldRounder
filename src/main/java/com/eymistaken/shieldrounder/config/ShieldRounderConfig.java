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
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(ShieldRounderClient.MOD_ID + ".json");
	private static ShieldRounderConfig instance = new ShieldRounderConfig();

	public boolean enabled = true;
	public boolean includeSelf = true;

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
		} catch (IOException ignored) {
			instance = new ShieldRounderConfig();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException ignored) {
		}
	}
}
