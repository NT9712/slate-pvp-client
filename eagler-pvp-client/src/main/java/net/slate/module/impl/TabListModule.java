package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;

/** Makes the player list readable at a glance. */
public class TabListModule extends Module {

	private static TabListModule INSTANCE;

	private final BoolSetting numericPing = add(new BoolSetting("Ping In ms", true));
	private final BoolSetting hideHeader = add(new BoolSetting("Hide Header/Footer", false));

	public TabListModule() {
		super("Tab List", "Shows exact ping and trims the player list.", Category.MISC);
		INSTANCE = this;
	}

	public static boolean numericPing() {
		TabListModule m = INSTANCE;
		return m != null && m.isEnabled() && m.numericPing.get();
	}

	public static boolean hideHeader() {
		TabListModule m = INSTANCE;
		return m != null && m.isEnabled() && m.hideHeader.get();
	}
}
