package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.NumberSetting;

/** Scales down the camera shake you get when taking damage. */
public class HurtCamModule extends Module {

	private static HurtCamModule INSTANCE;

	private final NumberSetting strength = add(new NumberSetting("Strength", 0.0D, 0.0D, 100.0D, 5.0D, "%"));

	public HurtCamModule() {
		super("Hurt Cam", "Reduces the camera shake when you take damage.", Category.VISUAL);
		INSTANCE = this;
	}

	/** Multiplier applied to the vanilla hurt camera tilt. */
	public static float scale() {
		HurtCamModule m = INSTANCE;
		if (m == null || !m.isEnabled()) {
			return 1.0f;
		}
		return m.strength.getFloat() / 100.0f;
	}

}
