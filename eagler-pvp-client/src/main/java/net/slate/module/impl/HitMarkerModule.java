package net.slate.module.impl;

import net.slate.ClientEvents;
import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.ColorSetting;
import net.slate.module.setting.NumberSetting;
import net.slate.ui.Draw;
import net.slate.ui.Theme;

/** Draws a short lived "X" over the crosshair whenever an attack lands. */
public final class HitMarkerModule extends Module {

	private static HitMarkerModule INSTANCE;

	private static final float GAP = 2f;

	private final NumberSetting duration = add(new NumberSetting("Duration", 300.0D, 100.0D, 800.0D, 50.0D, "ms"));
	private final ColorSetting colour = add(new ColorSetting("Colour", 0xFFFFFFFF));
	private final NumberSetting size = add(new NumberSetting("Size", 5.0D, 3.0D, 10.0D, 1.0D));
	private final BoolSetting fade = add(new BoolSetting("Fade", true));

	public HitMarkerModule() {
		super("Hit Marker", "Marks the crosshair when you land a hit.", Category.COMBAT);
		INSTANCE = this;
	}

	public static void render(int screenWidth, int screenHeight) {
		HitMarkerModule m = INSTANCE;
		if (m == null || !m.isEnabled()) {
			return;
		}
		m.draw(screenWidth * 0.5f, screenHeight * 0.5f);
	}

	private void draw(float cx, float cy) {
		long life = (long) duration.get();
		long since = ClientEvents.sinceHitMarker();
		if (since < 0L || since >= life) {
			return;
		}
		float alpha = fade.get() ? 1f - since / (float) life : 1f;
		int c = Theme.alpha(colour.get(), alpha);
		int len = size.getInt();
		Draw.batchStart();
		for (int i = 0; i <= len; i++) {
			float o = GAP + i;
			Draw.batchRect(cx + o, cy + o, 1f, 1f, c);
			Draw.batchRect(cx - o, cy + o, 1f, 1f, c);
			Draw.batchRect(cx + o, cy - o, 1f, 1f, c);
			Draw.batchRect(cx - o, cy - o, 1f, 1f, c);
		}
		Draw.batchEnd();
	}
}
