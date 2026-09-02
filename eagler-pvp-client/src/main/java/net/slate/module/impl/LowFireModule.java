package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.NumberSetting;

/** Pulls the first person fire overlay down so it does not cover the screen. */
public class LowFireModule extends Module {

	private static LowFireModule INSTANCE;

	private final NumberSetting height = add(new NumberSetting("Height", 40.0D, 0.0D, 90.0D, 5.0D, "%"));

	public LowFireModule() {
		super("Low Fire", "Lowers the fire overlay so you can see while burning.", Category.VISUAL);
		INSTANCE = this;
	}

	/** Extra downward offset, in world units, applied to the first person fire quads. */
	public static float offset() {
		LowFireModule m = INSTANCE;
		if (m == null || !m.isEnabled()) {
			return 0.0f;
		}
		return m.height.getFloat() / 100.0f * 0.9f;
	}

}
