package net.slate.module.impl;

import net.minecraft.client.Minecraft;
import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.ModeSetting;

/**
 * Hold a key to look around without turning the player. Releasing the key snaps the camera
 * straight back, so it never changes where you are actually facing.
 */
public class FreelookModule extends Module {

	private static FreelookModule INSTANCE;

	private final ModeSetting view = add(new ModeSetting("View", 0, "Third Person", "Front"));
	private final BoolSetting hideCrosshair = add(new BoolSetting("Hide Crosshair", true));

	private boolean active;
	private int previousView;
	private float yawOffset;
	private float pitchOffset;

	public FreelookModule() {
		super("Freelook", "Hold a key to look around without turning.", Category.PLAYER);
		INSTANCE = this;
		setKey(342); // Left Alt
	}

	public boolean isHoldKey() {
		return true;
	}

	public void onDisable() {
		stop();
	}

	public void onTick() {
		Minecraft mc = Minecraft.getInstance();
		boolean want = isKeyHeld() && mc.player != null && mc.currentScreen == null;
		if (want && !active) {
			active = true;
			previousView = mc.gameSettings.thirdPersonView;
			mc.gameSettings.thirdPersonView = view.is("Front") ? 2 : 1;
			yawOffset = 0f;
			pitchOffset = 0f;
		} else if (!want && active) {
			stop();
		}
	}

	private void stop() {
		if (active) {
			Minecraft.getInstance().gameSettings.thirdPersonView = previousView;
			active = false;
		}
		yawOffset = 0f;
		pitchOffset = 0f;
	}

	public static boolean isActive() {
		FreelookModule m = INSTANCE;
		return m != null && m.isEnabled() && m.active;
	}

	public static boolean hidesCrosshair() {
		FreelookModule m = INSTANCE;
		return m != null && m.isEnabled() && m.active && m.hideCrosshair.get();
	}

	/** Consumes the mouse delta while free looking. Returns true if the player should not rotate. */
	public static boolean consumeLook(double dx, double dy) {
		FreelookModule m = INSTANCE;
		if (m == null || !m.isEnabled() || !m.active) {
			return false;
		}
		m.yawOffset += (float) dx * 0.15f;
		m.pitchOffset += (float) dy * 0.15f;
		if (m.yawOffset > 180f) {
			m.yawOffset -= 360f;
		}
		if (m.yawOffset < -180f) {
			m.yawOffset += 360f;
		}
		if (m.pitchOffset > 90f) {
			m.pitchOffset = 90f;
		}
		if (m.pitchOffset < -90f) {
			m.pitchOffset = -90f;
		}
		return true;
	}

	public static float yawOffset() {
		FreelookModule m = INSTANCE;
		return m != null && m.active ? m.yawOffset : 0f;
	}

	public static float pitchOffset() {
		FreelookModule m = INSTANCE;
		return m != null && m.active ? m.pitchOffset : 0f;
	}

	public String status() {
		return active ? "looking" : null;
	}
}
