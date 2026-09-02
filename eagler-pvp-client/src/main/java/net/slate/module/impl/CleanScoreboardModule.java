package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;

/** Tidies up the sidebar scoreboard that most servers use. */
public class CleanScoreboardModule extends Module {

	private static CleanScoreboardModule INSTANCE;

	private final BoolSetting numbers = add(new BoolSetting("Hide Numbers", true));
	private final BoolSetting background = add(new BoolSetting("Hide Background", false));

	public CleanScoreboardModule() {
		super("Clean Scoreboard", "Removes the red numbers and background from the sidebar.", Category.VISUAL);
		INSTANCE = this;
	}

	public static boolean hideNumbers() {
		CleanScoreboardModule m = INSTANCE;
		return m != null && m.isEnabled() && m.numbers.get();
	}

	public static boolean hideBackground() {
		CleanScoreboardModule m = INSTANCE;
		return m != null && m.isEnabled() && m.background.get();
	}
}
