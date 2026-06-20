package com.eymistaken.shieldrounder;

import com.eymistaken.shieldrounder.config.ShieldRounderConfig;
import com.eymistaken.shieldrounder.render.ShieldHemisphereRenderer;
import net.fabricmc.api.ClientModInitializer;

public final class ShieldRounderClient implements ClientModInitializer {
	public static final String MOD_ID = "shieldrounder";

	@Override
	public void onInitializeClient() {
		ShieldRounderConfig.load();
		ShieldHemisphereRenderer.register();
	}
}
