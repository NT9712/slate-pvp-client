package net.slate.module.impl;

import net.minecraft.client.Minecraft;
import net.slate.hud.HudDraw;
import net.slate.module.HudModule;
import net.slate.module.HudModule.HAlign;
import net.slate.module.HudModule.VAlign;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.ModeSetting;
import net.slate.ui.Draw;
import net.slate.ui.Theme;

/** Frames per second, optionally coloured by how healthy the frame rate is. */
public final class FpsModule extends HudModule {

	private final ModeSetting style = add(new ModeSetting("Style", 0, "Label", "Compact"));
	private final BoolSetting colour = add(new BoolSetting("Colour", false));

	public FpsModule() {
		super("FPS", "Frame rate, with optional colour thresholds.", HAlign.LEFT, VAlign.TOP, 4, 4);
	}

	private String value() {
		int fps = Minecraft.getDebugFPS();
		return style.is("Compact") ? (fps + " fps") : String.valueOf(fps);
	}

	private int colour() {
		if (!colour.get()) {
			return HudDraw.VALUE;
		}
		int fps = Minecraft.getDebugFPS();
		return fps >= 60 ? Theme.GOOD : (fps >= 30 ? Theme.WARN : Theme.BAD);
	}

	@Override
	public void updateLayout() {
		setSize(HudDraw.lineWidth(style.is("Compact") ? null : "FPS", value()), Draw.TEXT_H);
	}

	@Override
	public void render(float partialTicks) {
		HudDraw.line(style.is("Compact") ? null : "FPS", value(), 0f, 0f, colour());
	}

}
