package com.eymistaken.shieldrounder.render;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ShieldRotationTest {
	@Test
	void localShieldFollowsMouseRotationBeforeTheHeadUpdates() {
		Vector3f rightTurn = ShieldHemisphereRenderer.horizontalShieldForward(90.0F, 0.0F, true);
		Vector3f leftTurn = ShieldHemisphereRenderer.horizontalShieldForward(-90.0F, 0.0F, true);

		assertEquals(-1.0F, rightTurn.x, 1.0E-5F);
		assertEquals(0.0F, rightTurn.y, 1.0E-5F);
		assertEquals(0.0F, rightTurn.z, 1.0E-5F);
		assertEquals(1.0F, leftTurn.x, 1.0E-5F);
		assertEquals(0.0F, leftTurn.y, 1.0E-5F);
		assertEquals(0.0F, leftTurn.z, 1.0E-5F);
	}

	@Test
	void remoteShieldKeepsTheInterpolatedHeadDirection() {
		Vector3f forward = ShieldHemisphereRenderer.horizontalShieldForward(90.0F, 45.0F, false);
		float diagonal = (float) Math.sqrt(0.5D);

		assertEquals(-diagonal, forward.x, 1.0E-5F);
		assertEquals(0.0F, forward.y, 1.0E-5F);
		assertEquals(diagonal, forward.z, 1.0E-5F);
		assertEquals(1.0F, forward.length(), 1.0E-5F);
	}
}
