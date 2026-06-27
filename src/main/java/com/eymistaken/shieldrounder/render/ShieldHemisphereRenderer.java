package com.eymistaken.shieldrounder.render;

import com.eymistaken.shieldrounder.config.ShieldRounderConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
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
	private static ClientLevel currentWorld;
	private static final RenderType HEMISPHERE_LINES = RenderTypes.linesTranslucent();
	private static final RenderType HEMISPHERE_SURFACE = RenderTypes.debugQuads();

	private ShieldHemisphereRenderer() {
	}

	public static void register() {
		ClientTickEvents.END_LEVEL_TICK.register(ShieldHemisphereRenderer::tick);
		LevelRenderEvents.COLLECT_SUBMITS.register(ShieldHemisphereRenderer::render);
	}

	private static void tick(ClientLevel world) {
		if (world != currentWorld) {
			PLAYER_STATES.clear();
			currentWorld = world;
		}

		Set<UUID> activePlayers = new HashSet<>();
		long worldTime = world.getLevelData().getGameTime();

		for (Player player : world.players()) {
			ItemStack shield = activeShield(player);
			if (shield.isEmpty()) {
				continue;
			}

			UUID uuid = player.getUUID();
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

	private static void render(LevelRenderContext context) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			PLAYER_STATES.clear();
			currentWorld = null;
			return;
		}
		if (!ShieldRounderConfig.get().enabled || client.player == null) {
			return;
		}

		ShieldRounderConfig config = ShieldRounderConfig.get();
		float tickDelta = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		long worldTime = client.level.getLevelData().getGameTime();
		Vec3 cameraPos = cameraPosition(context, client);
		boolean renderLines = config.renderMode != ShieldRounderConfig.RenderMode.SOLID;
		boolean renderSurface = config.renderMode != ShieldRounderConfig.RenderMode.WIREFRAME;

		for (Player player : client.level.players()) {
			ItemStack shield = activeShield(player);
			if (!shouldRenderFor(player, client, shield)) {
				continue;
			}

			ShieldRenderState state = PLAYER_STATES.computeIfAbsent(player.getUUID(), ignored -> new ShieldRenderState(worldTime, shieldDamage(shield)));
			RenderColor color = colorFor(config, shield);
			if (config.blockFlash) {
				color = color.flash(flashAmount(state.flashStartTick, worldTime, tickDelta));
			}
			RenderColor renderColor = color;
			float deploymentProgress = config.deploymentAnimation ? deploymentProgress(state.usingSinceTick, worldTime, tickDelta) : 1.0F;
			Vec3 center = interpolatedPosition(player, tickDelta).add(0.0D, CENTER_HEIGHT, 0.0D);
			Vector3f forward = horizontalShieldForward(player, tickDelta);
			boolean renderGlint = config.enchantmentGlint && hasShieldGlint(shield);

			if (renderSurface) {
				RenderColor surfaceColor = renderColor.withAlpha(Math.round(renderColor.alpha * 0.5F));
				List<HemisphereMesh.Quad> faces = HemisphereMesh.createFaces(forward, HemisphereMesh.DEFAULT_RADIUS, config.meridians, config.rings);
				context.submitNodeCollector().submitCustomGeometry(context.poseStack(), HEMISPHERE_SURFACE, (pose, consumer) -> {
					for (HemisphereMesh.Quad face : faces) {
						emitQuad(pose, consumer, center, cameraPos, face, surfaceColor, deploymentProgress);
					}
				});
				if (renderGlint) {
					context.submitNodeCollector().submitCustomGeometry(context.poseStack(), HEMISPHERE_SURFACE, (pose, consumer) -> {
						for (HemisphereMesh.Quad face : faces) {
							emitGlintQuad(pose, consumer, center, cameraPos, face, deploymentProgress, worldTime, tickDelta, renderColor.alpha);
						}
					});
				}
			}

			if (renderLines) {
				List<HemisphereMesh.LineSegment> lines = HemisphereMesh.create(forward, HemisphereMesh.DEFAULT_RADIUS, config.meridians, config.rings);
				context.submitNodeCollector().submitCustomGeometry(context.poseStack(), HEMISPHERE_LINES, (pose, consumer) -> {
					for (HemisphereMesh.LineSegment line : lines) {
						emitLine(pose, consumer, center, cameraPos, line, renderColor, config.lineWidth, deploymentProgress);
						if (renderGlint) {
							emitGlintLine(pose, consumer, center, cameraPos, line, config.lineWidth + 0.6F, deploymentProgress, worldTime, tickDelta, renderColor.alpha);
						}
					}
				});
			}
		}
	}

	private static boolean shouldRenderFor(Player player, Minecraft client, ItemStack shield) {
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

	private static Vec3 interpolatedPosition(Player player, float tickDelta) {
		return player.getPosition(tickDelta);
	}

	private static Vec3 cameraPosition(LevelRenderContext context, Minecraft client) {
		CameraRenderState cameraState = context.levelState().cameraRenderState;
		if (cameraState != null && cameraState.initialized && cameraState.pos != null) {
			return cameraState.pos;
		}
		return client.gameRenderer.mainCamera().position();
	}

	private static Vector3f horizontalShieldForward(Player player, float tickDelta) {
		float headYaw = Mth.rotLerp(tickDelta, player.yHeadRotO, player.yHeadRot);
		Vec3 direction = Vec3.directionFromRotation(0.0F, headYaw);
		return new Vector3f((float) direction.x, 0.0F, (float) direction.z).normalize();
	}

	private static ItemStack activeShield(Player player) {
		if (!player.isUsingItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack activeItem = player.getActiveItem();
		return activeItem.is(Items.SHIELD) ? activeItem : ItemStack.EMPTY;
	}

	private static int shieldDamage(ItemStack shield) {
		return shield.isDamageableItem() ? shield.getDamageValue() : 0;
	}

	private static boolean hasShieldGlint(ItemStack shield) {
		return shield.hasFoil() || shield.isEnchanted();
	}

	private static RenderColor colorFor(ShieldRounderConfig config, ItemStack shield) {
		if (config.dynamicDurabilityColor) {
			return durabilityColor(durabilityRatio(shield), config.alphaByte());
		}
		return new RenderColor(config.red, config.green, config.blue, config.alphaByte());
	}

	private static float durabilityRatio(ItemStack shield) {
		if (!shield.isDamageableItem() || shield.getMaxDamage() <= 0) {
			return 1.0F;
		}
		return Mth.clamp(1.0F - (float) shield.getDamageValue() / (float) shield.getMaxDamage(), 0.0F, 1.0F);
	}

	static RenderColor durabilityColor(float durabilityRatio, int alpha) {
		float clampedRatio = Mth.clamp(durabilityRatio, 0.0F, 1.0F);
		if (clampedRatio < 0.5F) {
			float progress = clampedRatio / 0.5F;
			return new RenderColor(255, Math.round(Mth.lerp(progress, 45, 230)), 45, alpha);
		}
		float progress = (clampedRatio - 0.5F) / 0.5F;
		return new RenderColor(Math.round(Mth.lerp(progress, 255, 40)), Math.round(Mth.lerp(progress, 230, 235)), Math.round(Mth.lerp(progress, 45, 80)), alpha);
	}

	static float flashAmount(long flashStartTick, long worldTime, float tickDelta) {
		if (flashStartTick == NO_FLASH) {
			return 0.0F;
		}
		float elapsedTicks = (float) (worldTime - flashStartTick) + tickDelta;
		return Mth.clamp(1.0F - elapsedTicks / FLASH_TICKS, 0.0F, 1.0F);
	}

	static float deploymentProgress(long usingSinceTick, long worldTime, float tickDelta) {
		float elapsedTicks = (float) (worldTime - usingSinceTick) + tickDelta;
		return Mth.clamp(elapsedTicks / DEPLOYMENT_TICKS, 0.0F, 1.0F);
	}

	private static void emitLine(PoseStack.Pose pose, VertexConsumer consumer, Vec3 center, Vec3 cameraPos, HemisphereMesh.LineSegment line, RenderColor color, float lineWidth, float scale) {
		emitVertex(pose, consumer, center, cameraPos, line.start(), color, lineWidth, scale);
		emitVertex(pose, consumer, center, cameraPos, line.end(), color, lineWidth, scale);
	}

	private static void emitGlintLine(PoseStack.Pose pose, VertexConsumer consumer, Vec3 center, Vec3 cameraPos, HemisphereMesh.LineSegment line, float lineWidth, float scale, long worldTime, float tickDelta, int baseAlpha) {
		emitVertex(pose, consumer, center, cameraPos, line.start(), glintColor(line.start(), worldTime, tickDelta, baseAlpha), lineWidth, scale);
		emitVertex(pose, consumer, center, cameraPos, line.end(), glintColor(line.end(), worldTime, tickDelta, baseAlpha), lineWidth, scale);
	}

	private static void emitQuad(PoseStack.Pose pose, VertexConsumer consumer, Vec3 center, Vec3 cameraPos, HemisphereMesh.Quad face, RenderColor color, float scale) {
		emitSurfaceVertex(pose, consumer, center, cameraPos, face.first(), color, scale);
		emitSurfaceVertex(pose, consumer, center, cameraPos, face.second(), color, scale);
		emitSurfaceVertex(pose, consumer, center, cameraPos, face.third(), color, scale);
		emitSurfaceVertex(pose, consumer, center, cameraPos, face.fourth(), color, scale);
	}

	private static void emitGlintQuad(PoseStack.Pose pose, VertexConsumer consumer, Vec3 center, Vec3 cameraPos, HemisphereMesh.Quad face, float scale, long worldTime, float tickDelta, int baseAlpha) {
		emitSurfaceVertex(pose, consumer, center, cameraPos, face.first(), glintSurfaceColor(face.first(), worldTime, tickDelta, baseAlpha), scale);
		emitSurfaceVertex(pose, consumer, center, cameraPos, face.second(), glintSurfaceColor(face.second(), worldTime, tickDelta, baseAlpha), scale);
		emitSurfaceVertex(pose, consumer, center, cameraPos, face.third(), glintSurfaceColor(face.third(), worldTime, tickDelta, baseAlpha), scale);
		emitSurfaceVertex(pose, consumer, center, cameraPos, face.fourth(), glintSurfaceColor(face.fourth(), worldTime, tickDelta, baseAlpha), scale);
	}

	static RenderColor glintColor(Vector3f offset, long worldTime, float tickDelta, int baseAlpha) {
		float wave = (Mth.sin((float) ((worldTime + tickDelta) * 0.45D + offset.x * 2.5F + offset.y * 1.5F - offset.z * 2.0F)) + 1.0F) * 0.5F;
		int alpha = Mth.clamp(Math.max(90, Math.round(baseAlpha * (0.35F + wave * 0.55F))), 0, 220);
		return new RenderColor(205, 95, 255, alpha);
	}

	static RenderColor glintSurfaceColor(Vector3f offset, long worldTime, float tickDelta, int baseAlpha) {
		float wave = (Mth.sin((float) ((worldTime + tickDelta) * 0.32D - offset.x * 1.8F + offset.y * 2.8F + offset.z * 1.2F)) + 1.0F) * 0.5F;
		int alpha = Mth.clamp(Math.max(35, Math.round(baseAlpha * (0.12F + wave * 0.22F))), 0, 95);
		return new RenderColor(180, 75, 255, alpha);
	}

	private static void emitVertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 center, Vec3 cameraPos, Vector3f offset, RenderColor color, float lineWidth, float scale) {
		float x = (float) (center.x - cameraPos.x + offset.x * scale);
		float y = (float) (center.y - cameraPos.y + offset.y * scale);
		float z = (float) (center.z - cameraPos.z + offset.z * scale);
		consumer.addVertex(pose, x, y, z).setColor(color.red, color.green, color.blue, color.alpha).setNormal(0.0F, 1.0F, 0.0F).setLineWidth(lineWidth);
	}

	private static void emitSurfaceVertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 center, Vec3 cameraPos, Vector3f offset, RenderColor color, float scale) {
		float x = (float) (center.x - cameraPos.x + offset.x * scale);
		float y = (float) (center.y - cameraPos.y + offset.y * scale);
		float z = (float) (center.z - cameraPos.z + offset.z * scale);
		consumer.addVertex(pose, x, y, z).setColor(color.red, color.green, color.blue, color.alpha);
	}

	record RenderColor(int red, int green, int blue, int alpha) {
		private RenderColor withAlpha(int alpha) {
			return new RenderColor(red, green, blue, Mth.clamp(alpha, 0, 255));
		}

		private RenderColor flash(float amount) {
			float clampedAmount = Mth.clamp(amount, 0.0F, 1.0F);
			return new RenderColor(
					Math.round(Mth.lerp(clampedAmount, red, 255)),
					Math.round(Mth.lerp(clampedAmount, green, 255)),
					Math.round(Mth.lerp(clampedAmount, blue, 255)),
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
