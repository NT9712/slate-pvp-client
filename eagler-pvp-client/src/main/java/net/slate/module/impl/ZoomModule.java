package net.slate.module.impl;

import net.minecraft.client.Minecraft;
import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.NumberSetting;

/**
 * Hold a key to narrow the field of view. Mouse sensitivity is scaled down with the zoom so
 * aiming stays usable.
 */
public class ZoomModule extends Module {

	private static ZoomModule INSTANCE;

	private final NumberSetting level = add(new NumberSetting("Level", 4.0D, 1.5D, 12.0D, 0.5D, "x"));
	private final BoolSetting smooth = add(new BoolSetting("Smooth", true));
	private final BoolSetting slowMouse = add(new BoolSetting("Slow Mouse", true));

	private double progress = 1.0D;
	private double previous = 1.0D;

	public ZoomModule() {
		super("Zoom", "Hold a key to zoom in.", Category.VISUAL);
		INSTANCE = this;
		setKey(67); // C
	}

	public boolean isHoldKey() {
		return true;
	}

	public void onDisable() {
		progress = 1.0D;
		previous = 1.0D;
	}

	/** Smoothing runs on the tick clock so the zoom speed does not depend on frame rate. */
	public void onTick() {
		previous = progress;
		double target = active() ? 1.0D / level.get() : 1.0D;
		if (smooth.get()) {
			progress += (target - progress) * 0.45D;
			if (Math.abs(progress - target) < 0.0005D) {
				progress = target;
			}
		} else {
			progress = target;
		}
	}

	private boolean active() {
		Minecraft mc = Minecraft.getInstance();
		return isEnabled() && mc.currentScreen == null
				&& (isKeyHeld() || mc.gameSettings.keyBindZoom.isKeyDown());
	}

	/** True when the zoom trigger is being held - used by the scroll wheel handler. */
	public static boolean zoomKeyHeld() {
		ZoomModule z = INSTANCE;
		return z != null && z.active();
	}

	/** Scroll wheel adjusts the zoom level while zooming. */
	public static void scroll(double amount) {
		ZoomModule z = INSTANCE;
		if (z != null && z.active()) {
			z.level.set(z.level.get() + (amount > 0.0D ? 0.5D : -0.5D));
			net.slate.Config.markDirty();
		}
	}

	/** Multiplier applied to the vanilla FOV. 1.0 = no zoom. */
	public static double fovScale(float partialTicks) {
		ZoomModule z = INSTANCE;
		if (z == null) {
			return 1.0D;
		}
		return z.update(partialTicks);
	}

	private double update(float partialTicks) {
		return previous + (progress - previous) * Math.max(0f, Math.min(1f, partialTicks));
	}

	/**
	 * Multiplier applied to raw mouse movement so aiming stays controllable while zoomed.
	 * Applied to the delta, never to the saved sensitivity setting.
	 */
	public static double mouseScale() {
		ZoomModule z = INSTANCE;
		if (z == null || !z.isEnabled() || !z.slowMouse.get()) {
			return 1.0D;
		}
		return Math.max(0.2D, z.progress);
	}

}
