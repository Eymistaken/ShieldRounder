package com.eymistaken.shieldrounder.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;

public final class ShieldRounderConfigScreen extends Screen {
	private static final int BUTTON_WIDTH = 220;
	private static final int BUTTON_HEIGHT = 20;
	private static final int ROW_SPACING = 24;
	private static final int CONTENT_TOP = 40;
	private static final int FOOTER_HEIGHT = 36;
	private static final int SETTING_ROWS = 14;
	private final Screen parent;
	private int scrollOffset;
	private int maxScroll;

	public ShieldRounderConfigScreen(Screen parent) {
		super(Text.translatable("shieldrounder.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int x = (this.width - BUTTON_WIDTH) / 2;
		int contentBottom = this.height - FOOTER_HEIGHT;
		int viewportHeight = contentBottom - CONTENT_TOP;
		int contentHeight = (SETTING_ROWS - 1) * ROW_SPACING + BUTTON_HEIGHT;
		maxScroll = Math.max(0, contentHeight - viewportHeight);
		scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScroll);
		int y = CONTENT_TOP - scrollOffset;
		int row = 0;

		addToggle(x, y + ROW_SPACING * row++, "shieldrounder.config.enabled", ShieldRounderConfig.get().enabled, button -> {
			ShieldRounderConfig.get().enabled = !ShieldRounderConfig.get().enabled;
			button.setMessage(toggleText("shieldrounder.config.enabled", ShieldRounderConfig.get().enabled));
			ShieldRounderConfig.save();
		});

		addToggle(x, y + ROW_SPACING * row++, "shieldrounder.config.show_on_self", ShieldRounderConfig.get().includeSelf, button -> {
			ShieldRounderConfig.get().includeSelf = !ShieldRounderConfig.get().includeSelf;
			button.setMessage(toggleText("shieldrounder.config.show_on_self", ShieldRounderConfig.get().includeSelf));
			ShieldRounderConfig.save();
		});

		addButton(x, y + ROW_SPACING * row++, renderModeText(), button -> {
			ShieldRounderConfig.get().renderMode = ShieldRounderConfig.get().renderMode.next();
			button.setMessage(renderModeText());
			ShieldRounderConfig.save();
		});

		addToggle(x, y + ROW_SPACING * row++, "shieldrounder.config.dynamic_durability_color", ShieldRounderConfig.get().dynamicDurabilityColor, button -> {
			ShieldRounderConfig.get().dynamicDurabilityColor = !ShieldRounderConfig.get().dynamicDurabilityColor;
			button.setMessage(toggleText("shieldrounder.config.dynamic_durability_color", ShieldRounderConfig.get().dynamicDurabilityColor));
			ShieldRounderConfig.save();
		});

		addToggle(x, y + ROW_SPACING * row++, "shieldrounder.config.block_flash", ShieldRounderConfig.get().blockFlash, button -> {
			ShieldRounderConfig.get().blockFlash = !ShieldRounderConfig.get().blockFlash;
			button.setMessage(toggleText("shieldrounder.config.block_flash", ShieldRounderConfig.get().blockFlash));
			ShieldRounderConfig.save();
		});

		addToggle(x, y + ROW_SPACING * row++, "shieldrounder.config.deployment_animation", ShieldRounderConfig.get().deploymentAnimation, button -> {
			ShieldRounderConfig.get().deploymentAnimation = !ShieldRounderConfig.get().deploymentAnimation;
			button.setMessage(toggleText("shieldrounder.config.deployment_animation", ShieldRounderConfig.get().deploymentAnimation));
			ShieldRounderConfig.save();
		});

		addToggle(x, y + ROW_SPACING * row++, "shieldrounder.config.enchantment_glint", ShieldRounderConfig.get().enchantmentGlint, button -> {
			ShieldRounderConfig.get().enchantmentGlint = !ShieldRounderConfig.get().enchantmentGlint;
			button.setMessage(toggleText("shieldrounder.config.enchantment_glint", ShieldRounderConfig.get().enchantmentGlint));
			ShieldRounderConfig.save();
		});

		addIntSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.red", ShieldRounderConfig.get().red, ShieldRounderConfig.MIN_COLOR, ShieldRounderConfig.MAX_COLOR, value -> ShieldRounderConfig.get().red = value);
		addIntSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.green", ShieldRounderConfig.get().green, ShieldRounderConfig.MIN_COLOR, ShieldRounderConfig.MAX_COLOR, value -> ShieldRounderConfig.get().green = value);
		addIntSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.blue", ShieldRounderConfig.get().blue, ShieldRounderConfig.MIN_COLOR, ShieldRounderConfig.MAX_COLOR, value -> ShieldRounderConfig.get().blue = value);
		addFloatSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.alpha", ShieldRounderConfig.get().alpha, ShieldRounderConfig.MIN_ALPHA, ShieldRounderConfig.MAX_ALPHA, "%.2f", value -> ShieldRounderConfig.get().alpha = value);
		addFloatSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.line_width", ShieldRounderConfig.get().lineWidth, ShieldRounderConfig.MIN_LINE_WIDTH, ShieldRounderConfig.MAX_LINE_WIDTH, "%.1f", value -> ShieldRounderConfig.get().lineWidth = value);
		addIntSlider(x, y + ROW_SPACING * row++, "shieldrounder.config.meridians", ShieldRounderConfig.get().meridians, ShieldRounderConfig.MIN_MERIDIANS, ShieldRounderConfig.MAX_MERIDIANS, value -> ShieldRounderConfig.get().meridians = value);
		addIntSlider(x, y + ROW_SPACING * row, "shieldrounder.config.rings", ShieldRounderConfig.get().rings, ShieldRounderConfig.MIN_RINGS, ShieldRounderConfig.MAX_RINGS, value -> ShieldRounderConfig.get().rings = value);

		this.addDrawableChild(ButtonWidget.builder(Text.translatable("shieldrounder.config.done"), button -> close())
			.dimensions(x, this.height - 28, BUTTON_WIDTH, BUTTON_HEIGHT)
			.build());
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (maxScroll <= 0) {
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
		int previousOffset = scrollOffset;
		scrollOffset = MathHelper.clamp(scrollOffset - (int) Math.round(verticalAmount * ROW_SPACING), 0, maxScroll);
		if (scrollOffset != previousOffset) {
			this.clearChildren();
			init();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void close() {
		MinecraftClient.getInstance().setScreen(parent);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
	}

	private void addButton(int x, int y, Text text, ButtonWidget.PressAction onPress) {
		if (!isSettingVisible(y)) {
			return;
		}
		this.addDrawableChild(ButtonWidget.builder(text, onPress).dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
	}

	private void addToggle(int x, int y, String key, boolean value, ButtonWidget.PressAction onPress) {
		addButton(x, y, toggleText(key, value), onPress);
	}

	private void addSlider(int x, int y, String key, double value, double min, double max, SliderApplier applier, SliderFormatter formatter) {
		if (!isSettingVisible(y)) {
			return;
		}
		this.addDrawableChild(new ConfigSlider(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, key, value, min, max, applier, formatter));
	}

	private void addIntSlider(int x, int y, String key, int value, int min, int max, IntSliderApplier applier) {
		addSlider(x, y, key, value, min, max, sliderValue -> applier.apply((int) Math.round(sliderValue)), sliderValue -> Integer.toString((int) Math.round(sliderValue)));
	}

	private void addFloatSlider(int x, int y, String key, float value, float min, float max, String format, FloatSliderApplier applier) {
		addSlider(x, y, key, value, min, max, sliderValue -> applier.apply((float) sliderValue), sliderValue -> String.format(Locale.ROOT, format, sliderValue));
	}

	private boolean isSettingVisible(int y) {
		return y + BUTTON_HEIGHT >= CONTENT_TOP && y + BUTTON_HEIGHT <= this.height - FOOTER_HEIGHT;
	}

	private static Text toggleText(String key, boolean value) {
		return Text.translatable(key).append(Text.literal(": ")).append(Text.translatable(value ? "shieldrounder.config.on" : "shieldrounder.config.off"));
	}

	private static Text renderModeText() {
		return Text.translatable("shieldrounder.config.render_mode")
				.append(Text.literal(": "))
				.append(Text.translatable(ShieldRounderConfig.get().renderMode.translationKey()));
	}

	private static Text sliderText(String key, String value) {
		return Text.translatable(key).append(Text.literal(": " + value));
	}

	private static double normalizedValue(double value, double min, double max) {
		return MathHelper.clamp((value - min) / (max - min), 0.0D, 1.0D);
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

	private static final class ConfigSlider extends SliderWidget {
		private final String key;
		private final double min;
		private final double max;
		private final SliderApplier applier;
		private final SliderFormatter formatter;

		private ConfigSlider(int x, int y, int width, int height, String key, double value, double min, double max, SliderApplier applier, SliderFormatter formatter) {
			super(x, y, width, height, Text.empty(), normalizedValue(value, min, max));
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
