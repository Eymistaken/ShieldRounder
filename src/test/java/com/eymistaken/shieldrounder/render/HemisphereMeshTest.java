package com.eymistaken.shieldrounder.render;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HemisphereMeshTest {
	@Test
	void generatedPointsStayInForwardHemisphere() {
		Vector3f forward = new Vector3f(0.0F, 0.0F, 1.0F);
		List<HemisphereMesh.LineSegment> segments = HemisphereMesh.create(forward, 1.25F, 12, 5);

		for (HemisphereMesh.LineSegment segment : segments) {
			assertTrue(segment.start().dot(forward) >= -1.0E-5F);
			assertTrue(segment.end().dot(forward) >= -1.0E-5F);
		}
	}

	@Test
	void generatedPointsStayInHorizontalForwardHemisphere() {
		Vector3f forward = new Vector3f(0.25F, 0.0F, 0.35F).normalize();
		List<HemisphereMesh.LineSegment> segments = HemisphereMesh.create(forward, 1.25F, 12, 5);

		for (HemisphereMesh.LineSegment segment : segments) {
			assertTrue(segment.start().dot(forward) >= -1.0E-5F);
			assertTrue(segment.end().dot(forward) >= -1.0E-5F);
		}
	}

	@Test
	void generatedPointsRemainLevelWhenUsingHorizontalForward() {
		Vector3f forward = new Vector3f(0.0F, 0.0F, 1.0F);
		List<HemisphereMesh.LineSegment> segments = HemisphereMesh.create(forward, 1.25F, 12, 5);

		for (HemisphereMesh.LineSegment segment : segments) {
			assertTrue(Math.abs(segment.start().x) <= 1.25F + 1.0E-5F);
			assertTrue(Math.abs(segment.end().x) <= 1.25F + 1.0E-5F);
		}
	}

	@Test
	void generatedLineCountMatchesRingsAndMeridians() {
		List<HemisphereMesh.LineSegment> segments = HemisphereMesh.create(new Vector3f(0.0F, 0.0F, 1.0F), 1.25F, 12, 5);

		assertEquals(120, segments.size());
	}

	@Test
	void generatedLineCountRespondsToCustomRingsAndMeridians() {
		List<HemisphereMesh.LineSegment> segments = HemisphereMesh.create(new Vector3f(0.0F, 0.0F, 1.0F), 1.25F, 8, 3);

		assertEquals(48, segments.size());
	}

	@Test
	void generatedFaceCountMatchesRingsAndMeridians() {
		List<HemisphereMesh.Quad> faces = HemisphereMesh.createFaces(new Vector3f(0.0F, 0.0F, 1.0F), 1.25F, 8, 3);

		assertEquals(24, faces.size());
	}

	@Test
	void generatedFacePointsStayInForwardHemisphere() {
		Vector3f forward = new Vector3f(0.0F, 0.0F, 1.0F);
		List<HemisphereMesh.Quad> faces = HemisphereMesh.createFaces(forward, 1.25F, 8, 3);

		for (HemisphereMesh.Quad face : faces) {
			assertTrue(face.first().dot(forward) >= -1.0E-5F);
			assertTrue(face.second().dot(forward) >= -1.0E-5F);
			assertTrue(face.third().dot(forward) >= -1.0E-5F);
			assertTrue(face.fourth().dot(forward) >= -1.0E-5F);
		}
	}

	@Test
	void durabilityColorUsesGreenYellowRedStops() {
		ShieldHemisphereRenderer.RenderColor full = ShieldHemisphereRenderer.durabilityColor(1.0F, 170);
		ShieldHemisphereRenderer.RenderColor half = ShieldHemisphereRenderer.durabilityColor(0.5F, 170);
		ShieldHemisphereRenderer.RenderColor low = ShieldHemisphereRenderer.durabilityColor(0.0F, 170);

		assertEquals(new ShieldHemisphereRenderer.RenderColor(40, 235, 80, 170), full);
		assertEquals(new ShieldHemisphereRenderer.RenderColor(255, 230, 45, 170), half);
		assertEquals(new ShieldHemisphereRenderer.RenderColor(255, 45, 45, 170), low);
	}

	@Test
	void flashAmountFadesOverFourTicks() {
		assertEquals(0.75F, ShieldHemisphereRenderer.flashAmount(10L, 11L, 0.0F), 1.0E-5F);
		assertEquals(0.0F, ShieldHemisphereRenderer.flashAmount(10L, 14L, 0.0F), 1.0E-5F);
		assertEquals(0.0F, ShieldHemisphereRenderer.flashAmount(-1L, 11L, 0.0F), 1.0E-5F);
	}

	@Test
	void deploymentProgressClampsAfterThreeTicks() {
		assertEquals(0.5F, ShieldHemisphereRenderer.deploymentProgress(10L, 11L, 0.5F), 1.0E-5F);
		assertEquals(1.0F, ShieldHemisphereRenderer.deploymentProgress(10L, 13L, 0.0F), 1.0E-5F);
	}

	@Test
	void glintLineColorIsVisibleAndPurple() {
		ShieldHemisphereRenderer.RenderColor color = ShieldHemisphereRenderer.glintColor(new Vector3f(0.2F, 0.4F, 0.6F), 20L, 0.5F, 170);

		assertTrue(color.alpha() >= 90);
		assertTrue(color.blue() > color.green());
		assertTrue(color.red() > color.green());
	}

	@Test
	void glintSurfaceColorIsSubtleAndPurple() {
		ShieldHemisphereRenderer.RenderColor color = ShieldHemisphereRenderer.glintSurfaceColor(new Vector3f(0.2F, 0.4F, 0.6F), 20L, 0.5F, 170);

		assertTrue(color.alpha() >= 35);
		assertTrue(color.alpha() <= 95);
		assertTrue(color.blue() > color.green());
		assertTrue(color.red() > color.green());
	}
}
