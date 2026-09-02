package net.slate.ui;

/**
 * The single source of truth for client colours and spacing.
 * Everything the client draws must come from here so the menu and the HUD look related.
 */
public final class Theme {

	private Theme() {
	}

	// Surfaces
	public static final int BACKDROP = 0xCC0A0C10; // dimming behind the menu (darker, more opaque)
	public static final int PANEL = 0xFF0D1117;      // main panel background
	public static final int PANEL_LIGHT = 0xFF161B22; // lighter panel for cards
	public static final int RAIL = 0xFF0B0E11;
	public static final int SUB_SURFACE = 0x1AFFFFFF; // expanded settings block
	public static final int ROW_HOVER = 0x1FFFFFFF;
	public static final int ROW_ACTIVE = 0x2FFFFFFF;
	public static final int DIVIDER = 0xFF2F363E;
	public static final int GUIDE = 0x33FFFFFF;
	public static final int BORDER = 0xFF30363D;
	public static final int BORDER_LIGHT = 0xFF484F58;

	// Text - four steps, used consistently
	public static final int TEXT = 0xFFF0F6FC;
	public static final int TEXT_DIM = 0xFF8B949E;
	public static final int TEXT_MUTED = 0xFF6E7681;
	public static final int TEXT_FAINT = 0xFF484F58;

	// Accent - CS2-style blue
	public static final int ACCENT = 0xFF00A8FF;
	public static final int ACCENT_HOVER = 0xFF33BBFF;
	public static final int ACCENT_PRESS = 0xFF0088CC;
	public static final int ACCENT_MUTED = 0x3300A8FF;
	public static final int OFF = 0xFF21262D;
	public static final int KNOB_ON = 0xFFFFFFFF;
	public static final int KNOB_OFF = 0xFF6E7681;

	// Status scale - only ever used for good/warning/bad readings
	public static final int GOOD = 0xFF3FB950;
	public static final int WARN = 0xFFD29922;
	public static final int BAD = 0xFFF85149;

	// HUD
	public static final int HUD_BG = 0x800A0C10;

	// Metrics
	public static final int PAD = 16;
	public static final int PAD_SM = 8;
	public static final int RADIUS = 6;
	public static final int RADIUS_SM = 4;
	public static final int RADIUS_LG = 8;

	/** Blends two ARGB colours. */
	public static int mix(int a, int b, float t) {
		if (t <= 0f) {
			return a;
		}
		if (t >= 1f) {
			return b;
		}
		int aa = (a >>> 24), ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
		int ba = (b >>> 24), br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
		return ((int) (aa + (ba - aa) * t) << 24) | ((int) (ar + (br - ar) * t) << 16)
				| ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
	}

	/** Same colour, different alpha (0..1). */
	public static int alpha(int color, float a) {
		int v = (int) ((color >>> 24) * Math.max(0f, Math.min(1f, a)));
		return (v << 24) | (color & 0xFFFFFF);
	}
}