package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.NumberSetting;

/**
 * Brightens dark areas. The vanilla brightness setting is never written to, so nothing is
 * persisted behind the player's back.
 */
public class FullBrightModule extends Module {

	private static FullBrightModule INSTANCE;

	private final NumberSetting amount = add(new NumberSetting("Brightness", 100.0D, 20.0D, 100.0D, 5.0D, "%"));

	public FullBrightModule() {
		super("Full Bright", "Brightens dark areas.", Category.VISUAL);
		INSTANCE = this;
	}

	/** Effective gamma for the light map: the user's own value unless the module overrides it. */
	public static double gamma(double vanilla) {
		FullBrightModule m = INSTANCE;
		if (m == null || !m.isEnabled()) {
			return vanilla;
		}
		return Math.max(vanilla, m.amount.get() / 100.0D * 10.0D);
	}

}
