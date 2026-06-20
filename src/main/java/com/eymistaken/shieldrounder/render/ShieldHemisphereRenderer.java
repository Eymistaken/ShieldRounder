package com.eymistaken.shieldrounder.render;

import com.eymistaken.shieldrounder.config.ShieldRounderConfig;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.List;

public final class ShieldHemisphereRenderer {
	private static final int COLOR_RED = 35;
	private static final int COLOR_GREEN = 235;
	private static final int COLOR_BLUE = 220;
	private static final int COLOR_ALPHA = 170;
	private static final float LINE_WIDTH = 1.5F;
	private static final double CENTER_HEIGHT = 1.05D;
	private static final RenderLayer HEMISPHERE_LINES = RenderLayer.of(
			"shieldrounder_lines",
			RenderSetup.builder(RenderPipelines.LINES_TRANSLUCENT).translucent().expectedBufferSize(4096).build()
	);

	private ShieldHemisphereRenderer() {
	}

	public static void register() {
		WorldRenderEvents.AFTER_ENTITIES.register(ShieldHemisphereRenderer::render);
	}

	private static void render(WorldRenderContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!ShieldRounderConfig.get().enabled || client.world == null || client.player == null) {
			return;
		}

		float tickDelta = client.getRenderTickCounter().getTickProgress(false);
		Vec3d cameraPos = cameraPosition(context, client);
		VertexConsumer consumer = context.consumers().getBuffer(HEMISPHERE_LINES);

		for (PlayerEntity player : client.world.getPlayers()) {
			if (!shouldRenderFor(player, client)) {
				continue;
			}

			Vec3d center = interpolatedPosition(player, tickDelta).add(0.0D, CENTER_HEIGHT, 0.0D);
			Vector3f forward = horizontalShieldForward(player, tickDelta);
			List<HemisphereMesh.LineSegment> lines = HemisphereMesh.create(forward, HemisphereMesh.DEFAULT_RADIUS, HemisphereMesh.DEFAULT_MERIDIANS, HemisphereMesh.DEFAULT_RINGS);

			for (HemisphereMesh.LineSegment line : lines) {
				emitVertex(consumer, center, cameraPos, line.start());
				emitVertex(consumer, center, cameraPos, line.end());
			}
		}
	}

	private static boolean shouldRenderFor(PlayerEntity player, MinecraftClient client) {
		if (!player.isAlive() || player.isSpectator()) {
			return false;
		}
		if (player == client.player && !ShieldRounderConfig.get().includeSelf) {
			return false;
		}
		if (client.player != null && player.isInvisibleTo(client.player)) {
			return false;
		}
		ItemStack activeItem = player.getActiveItem();
		return player.isUsingItem() && activeItem != null && activeItem.isOf(Items.SHIELD);
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

	private static void emitVertex(VertexConsumer consumer, Vec3d center, Vec3d cameraPos, Vector3f offset) {
		float x = (float) (center.x - cameraPos.x + offset.x);
		float y = (float) (center.y - cameraPos.y + offset.y);
		float z = (float) (center.z - cameraPos.z + offset.z);
		consumer.vertex(x, y, z).color(COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_ALPHA).normal(0.0F, 1.0F, 0.0F).lineWidth(LINE_WIDTH);
	}
}
