package com.eymistaken.shieldrounder.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Locale;

public final class ShieldRounderConfigScreen extends Screen {
	private static final int SETTING_WIDTH = 220;
	private static final int RESET_WIDTH = 58;
	private static final int ROW_GAP = 4;
	private static final int ROW_WIDTH = SETTING_WIDTH + ROW_GAP + RESET_WIDTH;
	private static final int BUTTON_HEIGHT = 20;
	private static final int ROW_SPACING = 24;
	private static final int CONTENT_TOP = 40;
	private static final int FOOTER_HEIGHT = 36;
	private static final int SETTING_ROWS = 15;
	private final Screen parent;
	private int scrollOffset;
	private int maxScroll;

	public ShieldRounderConfigScreen(Screen parent) {
		super(Component.translatable("shieldrounder.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int x = (this.width - ROW_WIDTH) / 2;
		int contentBottom = this.height - FOOTER_HEIGHT;
		int viewportHeight = contentBottom - CONTENT_TOP;
		int contentHeight = (SETTING_ROWS - 1) * ROW_SPACING + BUTTON_HEIGHT;
		maxScroll = Math.max(0, contentHeight - viewportHeight);
		scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);
		int y = CONTENT_TOP - scrollOffset;
		int row = 0;
		ShieldRounderConfig defaults = ShieldRounderConfig.defaults();

		addToggle(x, y + ROW_SPACING * row++, "shieldrounder.config.enabled", ShieldRounderConfig.get().enabled, button -> {
			ShieldRounderConfig.get().enabled = !ShieldRounderConfig.get().enabled;
			button.setMessage(toggleText("shieldrounder.config.enabled", ShieldRounderConfig.get().enabled));
			ShieldRounderConfig.save();
		}, () -> ShieldRounderConfig.get().enabled = defaults.enabled);

		addToggle(x, y + ROW_SPACING * row++, "shieldrounder.config.show_on_self", ShieldRounderConfig.get().includeSelf, button -> {
			ShieldRounderConfig.get().includeSelf = !ShieldRounderConfig.get().includeSelf;
			button.setMessage(toggleText("shieldrounder.config.show_on_self", ShieldRounderConfig.get().includeSelf));
			ShieldRounderConfig.save();
		}, () -> ShieldRounderConfig.get().includeSelf = defaults.includeSelf);

		addButton(x, y + ROW_SPACING * row++, renderModeText(), button -> {
			ShieldRounderConfig.get().renderMode = ShieldRounderConfig.get().renderMode.next();
			button.setMessage(renderModeText());
			ShieldRounderConfig.save();
		}, () -> ShieldRounderConfig.get().renderMode = defaults.renderMode);

		addToggle(x, y + ROW_SPACING * row++, "shieldrounder.config.dynamic_durability_color", ShieldRounderConfig.get().dynamicDurabilityColor, button -> {
			ShieldRounderConfig.get().dynamicDurabilityColor = !ShieldRounderConfig.get().dynamicDurabilityColor;
			button.setMessage(toggleText("shieldrounder.config.dynamic_durability_color", ShieldRounderConfig.get().dynamicDurabilityColor));
			ShieldRounderConfig.save();
		}, () -> ShieldRounderConfig.get().dynamicDurabilityColor = defaults.dynamicDurabilityColor);

		addToggle(x, y + ROW_SPACING * row++, "shieldrounder.config.block_flash", ShieldRounderConfig.get().blockFlash, button -> {
			ShieldRounderConfig.get().blockFlash = !ShieldRounderConfig.get().blockFlash;
			button.setMessage(toggleText("shieldrounder.config.block_flash", ShieldRounderConfig.get().blockFlash));
			ShieldRounderConfig.save();
		}, () -> ShieldRounderConfig.get().blockFlash = defaults.blockFlash);

		addToggle(x, y + ROW_SPACING * row++, "shieldrounder.config.deployment_animation", ShieldRounderConfig.get().deploymentAnimation, button -> {
			ShieldRounderConfig.get().deploymentAnimation = !ShieldRounderConfig.get().deploymentAnimation;
			button.setMessage(toggleText("shieldrounder.config.deployment_animation", ShieldRounderConfig.get().deploymentAnimation));
			ShieldRounderConfig.save();
		}, () -> ShieldRounderConfig.get().deploymentAnimation = defaults.deploymentAnimation);

		addToggle(x, y + ROW_SPACING * row++, "shieldrounder.config.enchantment_glint", ShieldRounderConfig.get().enchantmentGlint, button -> {
			ShieldRounderConfig.get().enchantmentGlint = !ShieldRounderConfig.get().enchantmentGlint;
			button.setMessage(toggleText("shieldrounder.config.enchantment_glint", ShieldRounderConfig.get().enchantmentGlint));
			ShieldRounderConfig.save();
		}, () -> ShieldRounderConfig.get().enchantmentGlint = defaults.enchantmentGlint);

		addIntSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.red", ShieldRounderConfig.get().red, ShieldRounderConfig.MIN_COLOR, ShieldRounderConfig.MAX_COLOR, value -> ShieldRounderConfig.get().red = value, () -> ShieldRounderConfig.get().red = defaults.red);
		addIntSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.green", ShieldRounderConfig.get().green, ShieldRounderConfig.MIN_COLOR, ShieldRounderConfig.MAX_COLOR, value -> ShieldRounderConfig.get().green = value, () -> ShieldRounderConfig.get().green = defaults.green);
		addIntSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.blue", ShieldRounderConfig.get().blue, ShieldRounderConfig.MIN_COLOR, ShieldRounderConfig.MAX_COLOR, value -> ShieldRounderConfig.get().blue = value, () -> ShieldRounderConfig.get().blue = defaults.blue);
		addFloatSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.alpha", ShieldRounderConfig.get().alpha, ShieldRounderConfig.MIN_ALPHA, ShieldRounderConfig.MAX_ALPHA, "%.2f", value -> ShieldRounderConfig.get().alpha = value, () -> ShieldRounderConfig.get().alpha = defaults.alpha);
		addFloatSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.line_width", ShieldRounderConfig.get().lineWidth, ShieldRounderConfig.MIN_LINE_WIDTH, ShieldRounderConfig.MAX_LINE_WIDTH, "%.1f", value -> ShieldRounderConfig.get().lineWidth = value, () -> ShieldRounderConfig.get().lineWidth = defaults.lineWidth);
		addIntSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.meridians", ShieldRounderConfig.get().meridians, ShieldRounderConfig.MIN_MERIDIANS, ShieldRounderConfig.MAX_MERIDIANS, value -> ShieldRounderConfig.get().meridians = value, () -> ShieldRounderConfig.get().meridians = defaults.meridians);
		addIntSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.rings", ShieldRounderConfig.get().rings, ShieldRounderConfig.MIN_RINGS, ShieldRounderConfig.MAX_RINGS, value -> ShieldRounderConfig.get().rings = value, () -> ShieldRounderConfig.get().rings = defaults.rings);

		addButton(x, y + ROW_SPACING * row, Component.translatable("shieldrounder.config.reset_settings"), button -> {
			ShieldRounderConfig.reset();
			refresh();
		});

		this.addRenderableWidget(Button.builder(Component.translatable("shieldrounder.config.done"), button -> onClose())
			.bounds(x, this.height - 28, ROW_WIDTH, BUTTON_HEIGHT)
			.build());
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (maxScroll <= 0) {
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
		int previousOffset = scrollOffset;
		scrollOffset = Mth.clamp(scrollOffset - (int) Math.round(verticalAmount * ROW_SPACING), 0, maxScroll);
		if (scrollOffset != previousOffset) {
			this.clearWidgets();
			init();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void refresh() {
		this.clearWidgets();
		init();
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().gui.setScreen(parent);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.extractRenderState(context, mouseX, mouseY, deltaTicks);
		context.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
	}

	private void addButton(int x, int y, Component text, Button.OnPress onPress) {
		if (!isSettingVisible(y)) {
			return;
		}
		this.addRenderableWidget(Button.builder(text, onPress).bounds(x, y, ROW_WIDTH, BUTTON_HEIGHT).build());
	}

	private void addButton(int x, int y, Component text, Button.OnPress onPress, Runnable resetAction) {
		if (!isSettingVisible(y)) {
			return;
		}
		this.addRenderableWidget(Button.builder(text, onPress).bounds(x, y, SETTING_WIDTH, BUTTON_HEIGHT).build());
		addResetButton(x, y, resetAction);
	}

	private void addToggle(int x, int y, String key, boolean value, Button.OnPress onPress, Runnable resetAction) {
		addButton(x, y, toggleText(key, value), onPress, resetAction);
	}

	private void addSlider(int x, int y, String key, double value, double min, double max, SliderApplier applier, SliderFormatter formatter, Runnable resetAction) {
		if (!isSettingVisible(y)) {
			return;
		}
		this.addRenderableWidget(new ConfigSlider(x, y, SETTING_WIDTH, BUTTON_HEIGHT, key, value, min, max, applier, formatter));
		addResetButton(x, y, resetAction);
	}

	private void addIntSlider(int x, int y, String key, int value, int min, int max, IntSliderApplier applier, Runnable resetAction) {
		addSlider(x, y, key, value, min, max, sliderValue -> applier.apply((int) Math.round(sliderValue)), sliderValue -> Integer.toString((int) Math.round(sliderValue)), resetAction);
	}

	private void addFloatSlider(int x, int y, String key, float value, float min, float max, String format, FloatSliderApplier applier, Runnable resetAction) {
		addSlider(x, y, key, value, min, max, sliderValue -> applier.apply((float) sliderValue), sliderValue -> String.format(Locale.ROOT, format, sliderValue), resetAction);
	}

	private void addResetButton(int x, int y, Runnable resetAction) {
		this.addRenderableWidget(Button.builder(Component.translatable("shieldrounder.config.reset"), button -> {
			resetAction.run();
			ShieldRounderConfig.save();
			refresh();
		}).bounds(x + SETTING_WIDTH + ROW_GAP, y, RESET_WIDTH, BUTTON_HEIGHT).build());
	}

	private boolean isSettingVisible(int y) {
		return y + BUTTON_HEIGHT >= CONTENT_TOP && y + BUTTON_HEIGHT <= this.height - FOOTER_HEIGHT;
	}

	private static Component toggleText(String key, boolean value) {
		return Component.translatable(key).append(Component.literal(": ")).append(Component.translatable(value ? "shieldrounder.config.on" : "shieldrounder.config.off"));
	}

	private static Component renderModeText() {
		return Component.translatable("shieldrounder.config.render_mode")
				.append(Component.literal(": "))
				.append(Component.translatable(ShieldRounderConfig.get().renderMode.translationKey()));
	}

	private static Component sliderText(String key, String value) {
		return Component.translatable(key).append(Component.literal(": " + value));
	}

	private static double normalizedValue(double value, double min, double max) {
		return Mth.clamp((value - min) / (max - min), 0.0D, 1.0D);
	}

	@FunctionalInterface
	private interface SliderApplier {
		void apply(double value);
	}

	@FunctionalInterface
	private interface SliderFormatter {
		String format(double value);
	}

	@FunctionalInterface
	private interface IntSliderApplier {
		void apply(int value);
	}

	@FunctionalInterface
	private interface FloatSliderApplier {
		void apply(float value);
	}

	private static final class ConfigSlider extends AbstractSliderButton {
		private final String key;
		private final double min;
		private final double max;
		private final SliderApplier applier;
		private final SliderFormatter formatter;

		private ConfigSlider(int x, int y, int width, int height, String key, double value, double min, double max, SliderApplier applier, SliderFormatter formatter) {
			super(x, y, width, height, Component.empty(), normalizedValue(value, min, max));
			this.key = key;
			this.min = min;
			this.max = max;
			this.applier = applier;
			this.formatter = formatter;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(sliderText(key, formatter.format(actualValue())));
		}

		@Override
		protected void applyValue() {
			applier.apply(actualValue());
			ShieldRounderConfig.save();
		}

		private double actualValue() {
			return min + (max - min) * value;
		}
	}
}
