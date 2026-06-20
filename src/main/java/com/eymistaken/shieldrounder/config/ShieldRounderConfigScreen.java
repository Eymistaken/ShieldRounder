package com.eymistaken.shieldrounder.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ShieldRounderConfigScreen extends Screen {
	private static final int BUTTON_WIDTH = 180;
	private static final int BUTTON_HEIGHT = 20;
	private static final int ROW_SPACING = 24;
	private final Screen parent;

	public ShieldRounderConfigScreen(Screen parent) {
		super(Text.translatable("shieldrounder.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int x = (this.width - BUTTON_WIDTH) / 2;
		int y = this.height / 2 - 36;

		this.addDrawableChild(ButtonWidget.builder(toggleText("shieldrounder.config.enabled", ShieldRounderConfig.get().enabled), button -> {
			ShieldRounderConfig.get().enabled = !ShieldRounderConfig.get().enabled;
			button.setMessage(toggleText("shieldrounder.config.enabled", ShieldRounderConfig.get().enabled));
			ShieldRounderConfig.save();
		}).dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

		this.addDrawableChild(ButtonWidget.builder(toggleText("shieldrounder.config.show_on_self", ShieldRounderConfig.get().includeSelf), button -> {
			ShieldRounderConfig.get().includeSelf = !ShieldRounderConfig.get().includeSelf;
			button.setMessage(toggleText("shieldrounder.config.show_on_self", ShieldRounderConfig.get().includeSelf));
			ShieldRounderConfig.save();
		}).dimensions(x, y + ROW_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT).build());

		this.addDrawableChild(ButtonWidget.builder(Text.translatable("shieldrounder.config.done"), button -> close())
			.dimensions(x, y + ROW_SPACING * 2, BUTTON_WIDTH, BUTTON_HEIGHT)
			.build());
	}

	@Override
	public void close() {
		MinecraftClient.getInstance().setScreen(parent);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 64, 0xFFFFFF);
	}

	private static Text toggleText(String key, boolean value) {
		return Text.translatable(key).append(Text.literal(": ")).append(Text.translatable(value ? "shieldrounder.config.on" : "shieldrounder.config.off"));
	}
}
