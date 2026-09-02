package net.slate.module.impl;

import net.lax1dude.eaglercraft.HString;
import net.slate.ClientEvents;
import net.slate.hud.HudDraw;
import net.slate.module.Category;
import net.slate.module.HudModule;
import net.slate.module.HudModule.HAlign;
import net.slate.module.HudModule.VAlign;
import net.slate.ui.Draw;

/** Distance of your most recent attack, in blocks. */
public final class ReachModule extends HudModule {

	private static final long HIDE_AFTER = 4000L;

	private String value = "0.00";
	private boolean showing;

	public ReachModule() {
		super("Reach", "How far away your last hit landed.", Category.COMBAT, HAlign.CENTRE, VAlign.TOP, 0, 116);
	}

	@Override
	public void updateLayout() {
		showing = ClientEvents.sinceLastReach() <= HIDE_AFTER;
		if (!showing) {
			setSize(0, 0);
			return;
		}
		value = HString.format("%.2f", ClientEvents.lastReach());
		setSize(HudDraw.lineWidth("Reach", value), Draw.TEXT_H);
	}

	@Override
	public void render(float partialTicks) {
		if (!showing) {
			return;
		}
		HudDraw.line("Reach", value, 0f, 0f);
	}
}
