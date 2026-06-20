package com.eymistaken.shieldrounder.render;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class HemisphereMesh {
	public static final float DEFAULT_RADIUS = 1.25F;
	public static final int DEFAULT_MERIDIANS = 12;
	public static final int DEFAULT_RINGS = 5;

	private HemisphereMesh() {
	}

	public static List<LineSegment> create(Vector3f forward, float radius, int meridians, int rings) {
		Vector3f normalizedForward = new Vector3f(forward).normalize();
		Vector3f worldUp = new Vector3f(0.0F, 1.0F, 0.0F);
		Vector3f right = worldUp.cross(normalizedForward, new Vector3f());
		if (right.lengthSquared() < 1.0E-6F) {
			right.set(1.0F, 0.0F, 0.0F);
		} else {
			right.normalize();
		}
		Vector3f up = normalizedForward.cross(right, new Vector3f()).normalize();

		List<LineSegment> segments = new ArrayList<>();
		Vector3f centerPoint = new Vector3f();

		for (int ring = 1; ring <= rings; ring++) {
			float polar = (float) (ring * Math.PI * 0.5D / rings);
			Vector3f previous = null;
			Vector3f first = null;

			for (int meridian = 0; meridian < meridians; meridian++) {
				float azimuth = (float) (meridian * Math.PI * 2.0D / meridians);
				Vector3f point = pointOnHemisphere(normalizedForward, right, up, radius, polar, azimuth);
				if (first == null) {
					first = point;
				}
				if (previous != null) {
					segments.add(new LineSegment(previous, point));
				}
				previous = point;
			}

			if (previous != null && first != null) {
				segments.add(new LineSegment(previous, first));
			}
		}

		for (int meridian = 0; meridian < meridians; meridian++) {
			float azimuth = (float) (meridian * Math.PI * 2.0D / meridians);
			Vector3f previous = centerPoint;
			for (int ring = 1; ring <= rings; ring++) {
				float polar = (float) (ring * Math.PI * 0.5D / rings);
				Vector3f point = pointOnHemisphere(normalizedForward, right, up, radius, polar, azimuth);
				segments.add(new LineSegment(previous, point));
				previous = point;
			}
		}

		return List.copyOf(segments);
	}

	private static Vector3f pointOnHemisphere(Vector3f forward, Vector3f right, Vector3f up, float radius, float polar, float azimuth) {
		float forwardAmount = (float) Math.cos(polar);
		float sideAmount = (float) Math.sin(polar);
		float horizontal = (float) Math.cos(azimuth) * sideAmount;
		float vertical = (float) Math.sin(azimuth) * sideAmount;

		return new Vector3f(forward).mul(forwardAmount * radius)
			.add(new Vector3f(right).mul(horizontal * radius))
			.add(new Vector3f(up).mul(vertical * radius));
	}

	public record LineSegment(Vector3f start, Vector3f end) {
	}
}
