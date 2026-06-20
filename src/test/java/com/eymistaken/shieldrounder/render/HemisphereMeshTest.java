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
}
