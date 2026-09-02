package net.slate;

import net.lax1dude.eaglercraft.EagRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/**
 * Shared, read-only combat/input state that several modules need.
 * Kept in one place so we never count the same event twice.
 */
public final class ClientEvents {

	private static final int MAX_CLICKS = 32;

	private static final long[] leftClicks = new long[MAX_CLICKS];
	private static final long[] rightClicks = new long[MAX_CLICKS];
	private static int leftIndex, rightIndex;

	/** Combo state. */
	private static Entity comboTarget;
	private static int combo;
	private static long lastHitTime;

	/** Reach of the most recent attack, in blocks. */
	private static double lastReach;
	private static long lastReachTime;

	/** Set when we land a hit - used by the hit marker. */
	private static long hitMarkerTime;

	/** Set when the local player loses health. */
	private static long hurtTime;
	private static float lastHealth = -1f;

	private ClientEvents() {
	}

	public static void reset() {
		leftIndex = rightIndex = 0;
		for (int i = 0; i < MAX_CLICKS; i++) {
			leftClicks[i] = rightClicks[i] = 0L;
		}
		comboTarget = null;
		combo = 0;
		lastReach = 0.0D;
		lastReachTime = 0L;
		lastHitTime = 0L;
		hitMarkerTime = 0L;
		hurtTime = 0L;
		lastHealth = -1f;
	}

	public static void onMouseButton(int button, boolean pressed) {
		if (!pressed) {
			return;
		}
		long now = EagRuntime.steadyTimeMillis();
		if (button == 0) {
			leftClicks[leftIndex] = now;
			leftIndex = (leftIndex + 1) % MAX_CLICKS;
		} else if (button == 1) {
			rightClicks[rightIndex] = now;
			rightIndex = (rightIndex + 1) % MAX_CLICKS;
		}
	}

	public static int leftCps() {
		return countRecent(leftClicks);
	}

	public static int rightCps() {
		return countRecent(rightClicks);
	}

	private static int countRecent(long[] buf) {
		long cutoff = EagRuntime.steadyTimeMillis() - 1000L;
		int n = 0;
		for (int i = 0; i < MAX_CLICKS; i++) {
			if (buf[i] > cutoff) {
				n++;
			}
		}
		return n;
	}

	public static void onAttack(Entity target) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || target == null) {
			return;
		}
		long now = EagRuntime.steadyTimeMillis();

		double eyeY = mc.player.posY + mc.player.getEyeHeight();
		net.minecraft.util.math.RayTraceResult hit = mc.objectMouseOver;
		if (hit != null && hit.getHitVec() != null && hit.getType() == net.minecraft.util.math.RayTraceResult.Type.ENTITY) {
			net.minecraft.util.math.Vec3d v = hit.getHitVec();
			double dx = mc.player.posX - v.x;
			double dy = eyeY - v.y;
			double dz = mc.player.posZ - v.z;
			lastReach = Math.sqrt(dx * dx + dy * dy + dz * dz);
		} else {
			double dx = mc.player.posX - target.posX;
			double dy = eyeY - (target.posY + target.getHeight() * 0.5D);
			double dz = mc.player.posZ - target.posZ;
			lastReach = Math.sqrt(dx * dx + dy * dy + dz * dz);
		}
		lastReachTime = now;

		if (target instanceof LivingEntity) {
			if (comboTarget == target && now - lastHitTime < 4000L) {
				combo++;
			} else {
				comboTarget = target;
				combo = 1;
			}
			lastHitTime = now;
		}
		hitMarkerTime = now;
	}

	public static void onHealthUpdate(float newHealth) {
		if (lastHealth >= 0f && newHealth < lastHealth - 0.01f) {
			hurtTime = EagRuntime.steadyTimeMillis();
			// taking damage breaks our combo
			combo = 0;
			comboTarget = null;
		}
		lastHealth = newHealth;
	}

	public static int combo() {
		if (comboTarget != null && EagRuntime.steadyTimeMillis() - lastHitTime > 4000L) {
			combo = 0;
			comboTarget = null;
		}
		return combo;
	}

	public static Entity comboTarget() {
		return comboTarget;
	}

	public static double lastReach() {
		return lastReach;
	}

	public static long sinceLastReach() {
		return EagRuntime.steadyTimeMillis() - lastReachTime;
	}

	public static long sinceHitMarker() {
		return EagRuntime.steadyTimeMillis() - hitMarkerTime;
	}

	public static long sinceHurt() {
		return EagRuntime.steadyTimeMillis() - hurtTime;
	}
}
