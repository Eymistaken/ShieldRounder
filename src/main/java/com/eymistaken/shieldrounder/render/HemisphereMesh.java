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
		Basis basis = Basis.fromForward(forward);

		List<LineSegment> segments = new ArrayList<>();
		Vector3f centerPoint = new Vector3f();

		for (int ring = 1; ring <= rings; ring++) {
			float polar = (float) (ring * Math.PI * 0.5D / rings);
			Vector3f previous = null;
			Vector3f first = null;

			for (int meridian = 0; meridian < meridians; meridian++) {
				float azimuth = (float) (meridian * Math.PI * 2.0D / meridians);
				Vector3f point = pointOnHemisphere(basis, radius, polar, azimuth);
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
				Vector3f point = pointOnHemisphere(basis, radius, polar, azimuth);
				segments.add(new LineSegment(previous, point));
				previous = point;
			}
		}

		return List.copyOf(segments);
	}

	public static List<Quad> createFaces(Vector3f forward, float radius, int meridians, int rings) {
		Basis basis = Basis.fromForward(forward);
		List<Quad> quads = new ArrayList<>();
		Vector3f centerPoint = new Vector3f();

		for (int ring = 1; ring <= rings; ring++) {
			float innerPolar = (float) ((ring - 1) * Math.PI * 0.5D / rings);
			float outerPolar = (float) (ring * Math.PI * 0.5D / rings);

			for (int meridian = 0; meridian < meridians; meridian++) {
				float azimuth = (float) (meridian * Math.PI * 2.0D / meridians);
				float nextAzimuth = (float) ((meridian + 1) * Math.PI * 2.0D / meridians);
				Vector3f innerStart = ring == 1 ? new Vector3f(centerPoint) : pointOnHemisphere(basis, radius, innerPolar, azimuth);
				Vector3f innerEnd = ring == 1 ? new Vector3f(centerPoint) : pointOnHemisphere(basis, radius, innerPolar, nextAzimuth);
				Vector3f outerEnd = pointOnHemisphere(basis, radius, outerPolar, nextAzimuth);
				Vector3f outerStart = pointOnHemisphere(basis, radius, outerPolar, azimuth);
				quads.add(new Quad(innerStart, innerEnd, outerEnd, outerStart));
			}
		}

		return List.copyOf(quads);
	}

	private static Vector3f pointOnHemisphere(Basis basis, float radius, float polar, float azimuth) {
		float forwardAmount = (float) Math.cos(polar);
		float sideAmount = (float) Math.sin(polar);
		float horizontal = (float) Math.cos(azimuth) * sideAmount;
		float vertical = (float) Math.sin(azimuth) * sideAmount;

		return new Vector3f(basis.forward).mul(forwardAmount * radius)
			.add(new Vector3f(basis.right).mul(horizontal * radius))
			.add(new Vector3f(basis.up).mul(vertical * radius));
	}

	public record LineSegment(Vector3f start, Vector3f end) {
	}

	public record Quad(Vector3f first, Vector3f second, Vector3f third, Vector3f fourth) {
	}

	private record Basis(Vector3f forward, Vector3f right, Vector3f up) {
		private static Basis fromForward(Vector3f forward) {
			Vector3f normalizedForward = new Vector3f(forward).normalize();
			Vector3f worldUp = new Vector3f(0.0F, 1.0F, 0.0F);
			Vector3f right = worldUp.cross(normalizedForward, new Vector3f());
			if (right.lengthSquared() < 1.0E-6F) {
				right.set(1.0F, 0.0F, 0.0F);
			} else {
				right.normalize();
			}
			Vector3f up = normalizedForward.cross(right, new Vector3f()).normalize();
			return new Basis(normalizedForward, right, up);
		}
	}
}
