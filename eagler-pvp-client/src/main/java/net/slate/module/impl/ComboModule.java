package net.slate.module.impl;

import net.slate.ClientEvents;
import net.slate.module.Category;
import net.slate.hud.HudDraw;
import net.slate.module.HudModule;
import net.slate.module.HudModule.HAlign;
import net.slate.module.HudModule.VAlign;
import net.slate.ui.Draw;
import net.slate.ui.Theme;

/** Hits landed in a row on the same player. */
public final class ComboModule extends HudModule {

	private static final String SUFFIX = " Combo";

	private String count = "0";
	private boolean showing;

	public ComboModule() {
		super("Combo", "Hits landed in a row.", Category.COMBAT, HAlign.CENTRE, VAlign.TOP, 0, 100);
	}

	@Override
	public void updateLayout() {
		int combo = ClientEvents.combo();
		showing = combo > 0;
		if (!showing) {
			setSize(0, 0);
			return;
		}
		count = String.valueOf(combo);
		setSize(Draw.width(count) + Draw.width(SUFFIX), Draw.TEXT_H);
	}

	@Override
	public void render(float partialTicks) {
		if (!showing) {
			return;
		}
		Draw.textOutlined(count, 0f, 0f, Theme.ACCENT);
		float x = Draw.width(count);
		Draw.textOutlined(SUFFIX, x, 0f, HudDraw.LABEL);
	}

}
