package net.slate.ui;

import net.minecraft.client.Minecraft;

/**
 * The client's look applied to the vanilla screens.
 *
 * These are called from a handful of base widgets (Screen, Widget, AbstractSlider,
 * AbstractList, TextFieldWidget) rather than from individual screens, so every menu in the
 * game picks up the same flat style without touching thirty files.
 */
public final class Skin {

	private Skin() {
	}

	/** Flat replacement for the tiled dirt background. */
	public static void screenBackground(int width, int height) {
		Draw.gradient(0, 0, width, height, 0xFF14171C, 0xFF0D1013);
	}

	/** Flat replacement for the widgets.png button sprite. */
	public static void button(int x, int y, int width, int height, boolean hovered, boolean active, float alpha) {
		int fill;
		int border;
		if (!active) {
			fill = 0x14FFFFFF;
			border = 0x18FFFFFF;
		} else if (hovered) {
			fill = 0x38FFFFFF;
			border = 0x59FFFFFF;
		} else {
			fill = 0x24FFFFFF;
			border = 0x30FFFFFF;
		}
		Draw.roundRect(x, y, width, height, 3f, Theme.alpha(fill, alpha));
		Draw.outline(x, y, width, height, Theme.alpha(border, alpha));
	}

	/** Text colour matching the button states above. */
	public static int buttonText(boolean hovered, boolean active) {
		if (!active) {
			return Theme.TEXT_MUTED;
		}
		return hovered ? 0xFFFFFFFF : Theme.TEXT;
	}

	/**
	 * Flat slider. The value is shown as a fill across the whole control with a thin marker,
	 * rather than a knob, so it never sits on top of the label the button draws afterwards.
	 */
	public static void slider(int x, int y, int width, int height, double value, boolean hovered, float alpha) {
		float fill = (float) (value * (width - 2));
		if (fill > 1f) {
			Draw.roundRect(x + 1, y + 1, fill, height - 2, 2f,
					Theme.alpha(hovered ? 0x4D4E9BFF : 0x384E9BFF, alpha));
		}
		Draw.rect(x + 1 + Math.max(0f, fill - 1f), y + 1, 2, height - 2,
				Theme.alpha(hovered ? 0xFFFFFFFF : 0xC0FFFFFF, alpha));
	}

	/** Flat replacement for a scrolling list's tiled background. */
	public static void listBackground(int x0, int y0, int x1, int y1) {
		Draw.rect(x0, y0, x1 - x0, y1 - y0, 0x40000000);
	}

	/** Flat replacement for the dirt strips above and below a list. */
	public static void listHole(int width, int top, int bottom) {
		Draw.gradient(0, top, width, bottom - top, 0xFF14171C, 0xFF0D1013);
	}

	/** Thin separators where vanilla drew a shadow gradient. */
	public static void listEdges(int x0, int x1, int y0, int y1) {
		Draw.rect(x0, y0, x1 - x0, 1, 0x1AFFFFFF);
		Draw.rect(x0, y1 - 1, x1 - x0, 1, 0x1AFFFFFF);
	}

	/** Flat scrollbar: a faint track with a rounded thumb. */
	public static void scrollbar(int x, int trackY, int width, int trackHeight, int thumbY, int thumbHeight) {
		float w = Math.max(2f, width - 4f);
		float cx = x + (width - w) * 0.5f;
		Draw.roundRect(cx, trackY, w, trackHeight, w * 0.5f, 0x14FFFFFF);
		Draw.roundRect(cx, thumbY, w, thumbHeight, w * 0.5f, 0x4DFFFFFF);
	}

	/** Flat text field. */
	public static void textField(int x, int y, int width, int height, boolean focused) {
		Draw.roundRect(x - 2, y - 2, width + 4, height + 4, 3f, focused ? 0x24FFFFFF : 0x18FFFFFF);
		Draw.outline(x - 2, y - 2, width + 4, height + 4, focused ? Theme.ACCENT : 0x30FFFFFF);
	}

	/** True while a Slate screen is drawing, so vanilla chrome can stay out of the way. */
	public static boolean inGame() {
		return Minecraft.getInstance().world != null;
	}
}
