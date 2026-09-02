package net.slate.hud;

import net.slate.ui.Draw;

/**
 * Consistent typography for HUD elements: a dim label followed by a bright value, both
 * outlined so they stay readable on snow, sand and a bright sky.
 * Every element uses this so the HUD reads as one thing.
 */
public final class HudDraw {

	public static final int LABEL = 0xFF98A3B4;
	public static final int VALUE = 0xFFFFFFFF;
	public static final int LINE = 10;

	private static final int GAP = 4;

	private HudDraw() {
	}

	/** Draws "Label value" at (x,y). Returns the total width. */
	public static int line(String label, String value, float x, float y) {
		return line(label, value, x, y, VALUE);
	}

	public static int line(String label, String value, float x, float y, int valueColor) {
		float cx = x;
		if (label != null && label.length() > 0) {
			Draw.textOutlined(label, cx, y, LABEL);
			cx += Draw.width(label) + GAP;
		}
		Draw.textOutlined(value, cx, y, valueColor);
		return (int) (cx - x) + Draw.width(value);
	}

	/** Width the above call would produce, without drawing. */
	public static int lineWidth(String label, String value) {
		int w = Draw.width(value);
		if (label != null && label.length() > 0) {
			w += Draw.width(label) + GAP;
		}
		return w;
	}
}
