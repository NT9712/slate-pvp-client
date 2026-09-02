package net.slate.module.impl;

import net.slate.ClientEvents;
import net.slate.hud.HudDraw;
import net.slate.module.HudModule;
import net.slate.module.HudModule.HAlign;
import net.slate.module.HudModule.VAlign;
import net.slate.module.setting.BoolSetting;
import net.slate.ui.Draw;

/** Left (and optionally right) clicks per second. */
public final class CpsModule extends HudModule {

	private static final int GAP = 8;

	private final BoolSetting rightClick = add(new BoolSetting("Right Click", true));

	private String left = "0";
	private String right = "0";

	public CpsModule() {
		super("CPS", "Click rate over the last second, per button.", HAlign.LEFT, VAlign.TOP, 4, 14);
	}

	@Override
	public void updateLayout() {
		left = Integer.toString(ClientEvents.leftCps());
		if (!rightClick.get()) {
			setSize(HudDraw.lineWidth("CPS", left), Draw.TEXT_H);
			return;
		}
		right = Integer.toString(ClientEvents.rightCps());
		setSize(HudDraw.lineWidth("LMB", left) + GAP + HudDraw.lineWidth("RMB", right), Draw.TEXT_H);
	}

	@Override
	public void render(float partialTicks) {
		if (!rightClick.get()) {
			HudDraw.line("CPS", left, 0f, 0f);
			return;
		}
		float x = HudDraw.line("LMB", left, 0f, 0f) + GAP;
		HudDraw.line("RMB", right, x, 0f);
	}
}
