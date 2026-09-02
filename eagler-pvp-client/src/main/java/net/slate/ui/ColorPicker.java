package net.slate.ui;

import net.slate.module.setting.ColorSetting;

/**
 * Compact inline colour picker: a saturation/brightness square, hue and alpha sliders, a
 * hex field and a row of presets. Sized to sit inside a settings row.
 */
public final class ColorPicker {

	public static final int WIDTH = 209;
	public static final int HEIGHT = 46;

	private static final int SV = 36;
	private static final int BAR_W = 6;
	private static final int PAD = 5;
	private static final int SV_X = PAD;
	private static final int HUE_X = SV_X + SV + 5;
	private static final int ALPHA_X = HUE_X + BAR_W + 4;
	private static final int RIGHT_X = ALPHA_X + BAR_W + 6;

	private static final int[] PRESETS = { 0xFFFFFF, 0x4E9BFF, 0x5BD07A, 0xE8B84B, 0xE8604B, 0xC46BFF, 0x36D7D0,
			0x9AA4B2 };

	private int dragging; // 0 none, 1 sv, 2 hue, 3 alpha
	private String hexEdit;

	public void render(ColorSetting setting, float x, float y, float a) {
		float[] hsb = setting.toHsb();
		float hue = hsb[0];

		Draw.roundRect(x, y, WIDTH, HEIGHT, 3f, Theme.alpha(0xFF171A20, a));
		Draw.outline(x, y, WIDTH, HEIGHT, Theme.alpha(Theme.BORDER, a));

		float sx = x + SV_X;
		float sy = y + PAD;

		Draw.rect(sx, sy, SV, SV, Theme.alpha(0xFF000000 | ColorSetting.hsb(hue, 1f, 1f), a));
		Draw.gradientH(sx, sy, SV, SV, Theme.alpha(0xFFFFFFFF, a), 0x00FFFFFF);
		Draw.gradient(sx, sy, SV, SV, 0x00000000, Theme.alpha(0xFF000000, a));
		float cx = sx + hsb[1] * SV;
		float cy = sy + (1f - hsb[2]) * SV;
		Draw.outline(cx - 3, cy - 3, 6, 6, Theme.alpha(0xB0000000, a));
		Draw.outline(cx - 2, cy - 2, 4, 4, Theme.alpha(0xFFFFFFFF, a));

		float hx = x + HUE_X;
		int steps = 18;
		for (int i = 0; i < steps; i++) {
			Draw.rect(hx, sy + i * (SV / (float) steps), BAR_W, SV / (float) steps + 0.6f,
					Theme.alpha(0xFF000000 | ColorSetting.hsb(i / (float) steps, 1f, 1f), a));
		}
		Draw.rect(hx - 1, sy + hue * SV - 1, BAR_W + 2, 2, Theme.alpha(0xFFFFFFFF, a));

		float ax = x + ALPHA_X;
		Draw.checker(ax, sy, BAR_W, SV, 3);
		Draw.gradient(ax, sy, BAR_W, SV, Theme.alpha(0xFF000000 | setting.rgb(), a), 0x00000000 | setting.rgb());
		Draw.rect(ax - 1, sy + (1f - setting.alpha() / 255f) * SV - 1, BAR_W + 2, 2, Theme.alpha(0xFFFFFFFF, a));

		// hex field
		float rx = x + RIGHT_X;
		float rw = WIDTH - RIGHT_X - PAD;
		boolean editing = hexEdit != null;
		Draw.roundRect(rx, sy, rw, 12, 2f, Theme.alpha(editing ? 0x2E4E9BFF : 0x18FFFFFF, a));
		String hex = editing ? hexEdit : hexOf(setting.rgb());
		Draw.textSmall("#" + hex + (editing ? "_" : ""), rx + 5, sy + 4,
				Theme.alpha(editing ? Theme.TEXT : Theme.TEXT_DIM, a));

		// rainbow switch
		float ry = sy + 16;
		boolean on = setting.chroma();
		Draw.roundRect(rx, ry, 16, 8, 4f, Theme.alpha(on ? Theme.ACCENT : Theme.OFF, a));
		Draw.roundRect(rx + (on ? 9f : 1f), ry + 1f, 6, 6, 3f, Theme.alpha(on ? Theme.KNOB_ON : Theme.KNOB_OFF, a));
		Draw.textSmall("Rainbow", rx + 20, ry + 2, Theme.alpha(on ? Theme.TEXT_DIM : Theme.TEXT_MUTED, a));

		// presets
		float py = sy + 29;
		for (int i = 0; i < PRESETS.length; i++) {
			float bx = rx + i * 12;
			boolean sel = !on && (setting.rgb() == PRESETS[i]);
			Draw.roundRect(bx, py, 10, 9, 2f, Theme.alpha(0xFF000000 | PRESETS[i], a));
			Draw.outline(bx, py, 10, 9, Theme.alpha(sel ? Theme.ACCENT : 0x30FFFFFF, a));
		}
	}

	private static String hexOf(int rgb) {
		String s = Integer.toHexString(rgb & 0xFFFFFF).toUpperCase();
		while (s.length() < 6) {
			s = "0" + s;
		}
		return s;
	}

	public boolean click(ColorSetting setting, float x, float y, double mouseX, double mouseY) {
		float sx = x + SV_X, sy = y + PAD;
		if (inside(mouseX, mouseY, sx, sy, SV, SV)) {
			dragging = 1;
			drag(setting, x, y, mouseX, mouseY);
			return true;
		}
		if (inside(mouseX, mouseY, x + HUE_X - 2, sy, BAR_W + 4, SV)) {
			dragging = 2;
			drag(setting, x, y, mouseX, mouseY);
			return true;
		}
		if (inside(mouseX, mouseY, x + ALPHA_X - 2, sy, BAR_W + 4, SV)) {
			dragging = 3;
			drag(setting, x, y, mouseX, mouseY);
			return true;
		}
		float rx = x + RIGHT_X;
		float rw = WIDTH - RIGHT_X - PAD;
		if (inside(mouseX, mouseY, rx, sy, rw, 12)) {
			hexEdit = "";
			return true;
		}
		hexEdit = null;
		if (inside(mouseX, mouseY, rx, sy + 16, 16, 8)) {
			setting.setChroma(!setting.chroma());
			return true;
		}
		if (inside(mouseX, mouseY, rx, sy + 29, PRESETS.length * 12, 9)) {
			int i = (int) ((mouseX - rx) / 12);
			if (i >= 0 && i < PRESETS.length) {
				setting.setRgb(PRESETS[i]);
				setting.setChroma(false);
			}
			return true;
		}
		return false;
	}

	/** Returns true when the picker consumed the key. */
	public boolean keyTyped(ColorSetting setting, char c, int key) {
		if (hexEdit == null) {
			return false;
		}
		if (key == 259) { // backspace
			if (hexEdit.length() > 0) {
				hexEdit = hexEdit.substring(0, hexEdit.length() - 1);
			}
			return true;
		}
		if (key == 257 || key == 256) { // enter / escape
			if (key == 257 && hexEdit.length() == 6) {
				try {
					setting.setRgb(Integer.parseInt(hexEdit, 16));
					setting.setChroma(false);
				} catch (NumberFormatException ignored) {
				}
			}
			hexEdit = null;
			return true;
		}
		if (c != 0 && hexEdit.length() < 6 && isHex(c)) {
			hexEdit = hexEdit + Character.toUpperCase(c);
			if (hexEdit.length() == 6) {
				try {
					setting.setRgb(Integer.parseInt(hexEdit, 16));
					setting.setChroma(false);
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return true;
	}

	private static boolean isHex(char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	public boolean editing() {
		return hexEdit != null;
	}

	public void close() {
		hexEdit = null;
		dragging = 0;
	}

	public void release() {
		dragging = 0;
	}

	public boolean dragging() {
		return dragging != 0;
	}

	public void drag(ColorSetting setting, float x, float y, double mouseX, double mouseY) {
		float sx = x + SV_X, sy = y + PAD;
		float[] hsb = setting.toHsb();
		if (dragging == 1) {
			float s = clamp((float) ((mouseX - sx) / SV));
			float v = 1f - clamp((float) ((mouseY - sy) / SV));
			setting.setRgb(ColorSetting.hsb(hsb[0], s, v));
		} else if (dragging == 2) {
			float t = clamp((float) ((mouseY - sy) / SV));
			setting.setRgb(ColorSetting.hsb(t, Math.max(0.02f, hsb[1]), Math.max(0.05f, hsb[2])));
		} else if (dragging == 3) {
			setting.setAlpha((int) ((1f - clamp((float) ((mouseY - sy) / SV))) * 255));
		}
	}

	private static float clamp(float v) {
		return v < 0f ? 0f : (v > 1f ? 1f : v);
	}

	private static boolean inside(double mx, double my, float x, float y, float w, float h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}
}
