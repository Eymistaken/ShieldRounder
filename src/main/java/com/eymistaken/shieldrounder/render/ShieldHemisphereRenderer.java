package com.eymistaken.shieldrounder.render;

import com.eymistaken.shieldrounder.config.ShieldRounderConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ShieldHemisphereRenderer {
	private static final double CENTER_HEIGHT = 1.05D;
	private static final int FLASH_TICKS = 4;
	private static final int DEPLOYMENT_TICKS = 3;
	private static final long NO_FLASH = -1L;
	private static final Map<UUID, ShieldRenderState> PLAYER_STATES = new HashMap<>();
	private static ClientWorld currentWorld;
	private static final RenderLayer HEMISPHERE_LINES = RenderLayer.of(
			"shieldrounder_lines",
			RenderSetup.builder(RenderPipelines.LINES_TRANSLUCENT).translucent().expectedBufferSize(32768).build()
	);
	private static final RenderLayer HEMISPHERE_SURFACE = RenderLayer.of(
			"shieldrounder_surface",
			RenderSetup.builder(RenderPipelines.DEBUG_QUADS).translucent().expectedBufferSize(32768).build()
	);

	private ShieldHemisphereRenderer() {
	}

	public static void register() {
		ClientTickEvents.END_WORLD_TICK.register(ShieldHemisphereRenderer::tick);
		WorldRenderEvents.AFTER_ENTITIES.register(ShieldHemisphereRenderer::render);
	}

	private static void tick(ClientWorld world) {
		if (world != currentWorld) {
			PLAYER_STATES.clear();
			currentWorld = world;
		}

		Set<UUID> activePlayers = new HashSet<>();
		long worldTime = world.getTime();

		for (PlayerEntity player : world.getPlayers()) {
			ItemStack shield = activeShield(player);
			if (shield.isEmpty()) {
				continue;
			}

			UUID uuid = player.getUuid();
			activePlayers.add(uuid);
			ShieldRenderState state = PLAYER_STATES.computeIfAbsent(uuid, ignored -> new ShieldRenderState(worldTime, shieldDamage(shield)));
			int shieldDamage = shieldDamage(shield);
			if (state.lastShieldDamage >= 0 && shieldDamage > state.lastShieldDamage) {
				state.flashStartTick = worldTime;
			}
			state.lastShieldDamage = shieldDamage;
		}

		PLAYER_STATES.keySet().removeIf(uuid -> !activePlayers.contains(uuid));
	}

	private static void render(WorldRenderContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null) {
			PLAYER_STATES.clear();
			currentWorld = null;
			return;
		}
		if (!ShieldRounderConfig.get().enabled || client.player == null) {
			return;
		}

		ShieldRounderConfig config = ShieldRounderConfig.get();
		float tickDelta = client.getRenderTickCounter().getTickProgress(false);
		long worldTime = client.world.getTime();
		Vec3d cameraPos = cameraPosition(context, client);
		boolean renderLines = config.renderMode != ShieldRounderConfig.RenderMode.SOLID;
		boolean renderSurface = config.renderMode != ShieldRounderConfig.RenderMode.WIREFRAME;
		VertexConsumer lineConsumer = renderLines ? context.consumers().getBuffer(HEMISPHERE_LINES) : null;
		VertexConsumer surfaceConsumer = renderSurface ? context.consumers().getBuffer(HEMISPHERE_SURFACE) : null;

		for (PlayerEntity player : client.world.getPlayers()) {
			ItemStack shield = activeShield(player);
			if (!shouldRenderFor(player, client, shield)) {
				continue;
			}

			ShieldRenderState state = PLAYER_STATES.computeIfAbsent(player.getUuid(), ignored -> new ShieldRenderState(worldTime, shieldDamage(shield)));
			RenderColor color = colorFor(config, shield);
			if (config.blockFlash) {
				color = color.flash(flashAmount(state.flashStartTick, worldTime, tickDelta));
			}
			float deploymentProgress = config.deploymentAnimation ? deploymentProgress(state.usingSinceTick, worldTime, tickDelta) : 1.0F;
			Vec3d center = interpolatedPosition(player, tickDelta).add(0.0D, CENTER_HEIGHT, 0.0D);
			Vector3f forward = horizontalShieldForward(player, tickDelta);

			if (renderSurface && surfaceConsumer != null) {
				RenderColor surfaceColor = color.withAlpha(Math.round(color.alpha * 0.5F));
				List<HemisphereMesh.Quad> faces = HemisphereMesh.createFaces(forward, HemisphereMesh.DEFAULT_RADIUS, config.meridians, config.rings);
				for (HemisphereMesh.Quad face : faces) {
					emitQuad(surfaceConsumer, center, cameraPos, face, surfaceColor, deploymentProgress);
				}
			}

			if (renderLines && lineConsumer != null) {
				List<HemisphereMesh.LineSegment> lines = HemisphereMesh.create(forward, HemisphereMesh.DEFAULT_RADIUS, config.meridians, config.rings);
				for (HemisphereMesh.LineSegment line : lines) {
					emitLine(lineConsumer, center, cameraPos, line, color, config.lineWidth, deploymentProgress);
					if (config.enchantmentGlint && hasShieldGlint(shield)) {
						emitGlintLine(lineConsumer, center, cameraPos, line, config.lineWidth, deploymentProgress, worldTime, tickDelta, color.alpha);
					}
				}
			}
		}
	}

	private static boolean shouldRenderFor(PlayerEntity player, MinecraftClient client, ItemStack shield) {
		if (!player.isAlive() || player.isSpectator()) {
			return false;
		}
		if (player == client.player && !ShieldRounderConfig.get().includeSelf) {
			return false;
		}
		if (client.player != null && player.isInvisibleTo(client.player)) {
			return false;
		}
		return !shield.isEmpty();
	}

	private static Vec3d interpolatedPosition(PlayerEntity player, float tickDelta) {
		return player.getLerpedPos(tickDelta);
	}

	private static Vec3d cameraPosition(WorldRenderContext context, MinecraftClient client) {
		CameraRenderState cameraState = context.worldState().cameraRenderState;
		if (cameraState != null && cameraState.initialized && cameraState.pos != null) {
			return cameraState.pos;
		}
		return client.gameRenderer.getCamera().getCameraPos();
	}

	private static Vector3f horizontalShieldForward(PlayerEntity player, float tickDelta) {
		float headYaw = MathHelper.lerpAngleDegrees(tickDelta, player.lastHeadYaw, player.getHeadYaw());
		Vec3d direction = player.getRotationVector(0.0F, headYaw);
		return new Vector3f((float) direction.x, 0.0F, (float) direction.z).normalize();
	}

	private static ItemStack activeShield(PlayerEntity player) {
		if (!player.isUsingItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack activeItem = player.getActiveItem();
		return activeItem.isOf(Items.SHIELD) ? activeItem : ItemStack.EMPTY;
	}

	private static int shieldDamage(ItemStack shield) {
		return shield.isDamageable() ? shield.getDamage() : 0;
	}

	private static boolean hasShieldGlint(ItemStack shield) {
		return shield.hasGlint() || shield.hasEnchantments();
	}

	private static RenderColor colorFor(ShieldRounderConfig config, ItemStack shield) {
		if (config.dynamicDurabilityColor) {
			return durabilityColor(durabilityRatio(shield), config.alphaByte());
		}
		return new RenderColor(config.red, config.green, config.blue, config.alphaByte());
	}

	private static float durabilityRatio(ItemStack shield) {
		if (!shield.isDamageable() || shield.getMaxDamage() <= 0) {
			return 1.0F;
		}
		return MathHelper.clamp(1.0F - (float) shield.getDamage() / (float) shield.getMaxDamage(), 0.0F, 1.0F);
	}

	static RenderColor durabilityColor(float durabilityRatio, int alpha) {
		float clampedRatio = MathHelper.clamp(durabilityRatio, 0.0F, 1.0F);
		if (clampedRatio < 0.5F) {
			float progress = clampedRatio / 0.5F;
			return new RenderColor(255, MathHelper.lerp(progress, 45, 230), 45, alpha);
		}
		float progress = (clampedRatio - 0.5F) / 0.5F;
		return new RenderColor(MathHelper.lerp(progress, 255, 40), MathHelper.lerp(progress, 230, 235), MathHelper.lerp(progress, 45, 80), alpha);
	}

	static float flashAmount(long flashStartTick, long worldTime, float tickDelta) {
		if (flashStartTick == NO_FLASH) {
			return 0.0F;
		}
		float elapsedTicks = (float) (worldTime - flashStartTick) + tickDelta;
		return MathHelper.clamp(1.0F - elapsedTicks / FLASH_TICKS, 0.0F, 1.0F);
	}

	static float deploymentProgress(long usingSinceTick, long worldTime, float tickDelta) {
		float elapsedTicks = (float) (worldTime - usingSinceTick) + tickDelta;
		return MathHelper.clamp(elapsedTicks / DEPLOYMENT_TICKS, 0.0F, 1.0F);
	}

	private static void emitLine(VertexConsumer consumer, Vec3d center, Vec3d cameraPos, HemisphereMesh.LineSegment line, RenderColor color, float lineWidth, float scale) {
		emitVertex(consumer, center, cameraPos, line.start(), color, lineWidth, scale);
		emitVertex(consumer, center, cameraPos, line.end(), color, lineWidth, scale);
	}

	private static void emitGlintLine(VertexConsumer consumer, Vec3d center, Vec3d cameraPos, HemisphereMesh.LineSegment line, float lineWidth, float scale, long worldTime, float tickDelta, int baseAlpha) {
		emitVertex(consumer, center, cameraPos, line.start(), glintColor(line.start(), worldTime, tickDelta, baseAlpha), lineWidth, scale);
		emitVertex(consumer, center, cameraPos, line.end(), glintColor(line.end(), worldTime, tickDelta, baseAlpha), lineWidth, scale);
	}

	private static void emitQuad(VertexConsumer consumer, Vec3d center, Vec3d cameraPos, HemisphereMesh.Quad face, RenderColor color, float scale) {
		emitSurfaceVertex(consumer, center, cameraPos, face.first(), color, scale);
		emitSurfaceVertex(consumer, center, cameraPos, face.second(), color, scale);
		emitSurfaceVertex(consumer, center, cameraPos, face.third(), color, scale);
		emitSurfaceVertex(consumer, center, cameraPos, face.fourth(), color, scale);
	}

	private static RenderColor glintColor(Vector3f offset, long worldTime, float tickDelta, int baseAlpha) {
		float wave = (MathHelper.sin((float) ((worldTime + tickDelta) * 0.45D + offset.x * 2.5F + offset.y * 1.5F - offset.z * 2.0F)) + 1.0F) * 0.5F;
		int alpha = MathHelper.clamp(Math.round(baseAlpha * (0.08F + wave * 0.16F)), 0, 80);
		return new RenderColor(190, 135, 255, alpha);
	}

	private static void emitVertex(VertexConsumer consumer, Vec3d center, Vec3d cameraPos, Vector3f offset, RenderColor color, float lineWidth, float scale) {
		float x = (float) (center.x - cameraPos.x + offset.x * scale);
		float y = (float) (center.y - cameraPos.y + offset.y * scale);
		float z = (float) (center.z - cameraPos.z + offset.z * scale);
		consumer.vertex(x, y, z).color(color.red, color.green, color.blue, color.alpha).normal(0.0F, 1.0F, 0.0F).lineWidth(lineWidth);
	}

	private static void emitSurfaceVertex(VertexConsumer consumer, Vec3d center, Vec3d cameraPos, Vector3f offset, RenderColor color, float scale) {
		float x = (float) (center.x - cameraPos.x + offset.x * scale);
		float y = (float) (center.y - cameraPos.y + offset.y * scale);
		float z = (float) (center.z - cameraPos.z + offset.z * scale);
		consumer.vertex(x, y, z).color(color.red, color.green, color.blue, color.alpha);
	}

	record RenderColor(int red, int green, int blue, int alpha) {
		private RenderColor withAlpha(int alpha) {
			return new RenderColor(red, green, blue, MathHelper.clamp(alpha, 0, 255));
		}

		private RenderColor flash(float amount) {
			float clampedAmount = MathHelper.clamp(amount, 0.0F, 1.0F);
			return new RenderColor(
					MathHelper.lerp(clampedAmount, red, 255),
					MathHelper.lerp(clampedAmount, green, 255),
					MathHelper.lerp(clampedAmount, blue, 255),
					alpha
			);
		}
	}

	private static final class ShieldRenderState {
		private final long usingSinceTick;
		private int lastShieldDamage;
		private long flashStartTick = NO_FLASH;

		private ShieldRenderState(long usingSinceTick, int lastShieldDamage) {
			this.usingSinceTick = usingSinceTick;
			this.lastShieldDamage = lastShieldDamage;
		}
	}
}
