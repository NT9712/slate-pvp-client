package net.slate.module.setting;

/** ARGB colour with an optional animated "chroma" mode. */
public final class ColorSetting extends Setting {

	private int rgb;
	private int alpha;
	private boolean chroma;
	public final int defaultRgb;

	public ColorSetting(String name, int argb) {
		super(name);
		this.rgb = argb & 0xFFFFFF;
		this.alpha = (argb >>> 24) == 0 ? 255 : (argb >>> 24);
		this.defaultRgb = argb;
	}

	public int rgb() {
		return rgb;
	}

	public int alpha() {
		return alpha;
	}

	public boolean chroma() {
		return chroma;
	}

	public void setChroma(boolean c) {
		this.chroma = c;
	}

	public void setRgb(int v) {
		this.rgb = v & 0xFFFFFF;
	}

	public void setAlpha(int a) {
		this.alpha = a < 0 ? 0 : (a > 255 ? 255 : a);
	}

	/** Resolved colour for this frame. */
	public int get() {
		if (chroma) {
			float hue = (net.lax1dude.eaglercraft.EagRuntime.steadyTimeMillis() % 3000L) / 3000.0f;
			return (alpha << 24) | (hsb(hue, 0.65f, 1.0f) & 0xFFFFFF);
		}
		return (alpha << 24) | rgb;
	}

	/** Colour with a custom alpha applied (0..255). */
	public int get(int a) {
		return (Math.max(0, Math.min(255, a)) << 24) | (get() & 0xFFFFFF);
	}

	public static int hsb(float h, float s, float b) {
		h = h - (float) Math.floor(h);
		int i = (int) (h * 6.0f);
		float f = h * 6.0f - i;
		float p = b * (1.0f - s);
		float q = b * (1.0f - s * f);
		float t = b * (1.0f - s * (1.0f - f));
		float r, g, bl;
		switch (i) {
		case 0: r = b; g = t; bl = p; break;
		case 1: r = q; g = b; bl = p; break;
		case 2: r = p; g = b; bl = t; break;
		case 3: r = p; g = q; bl = b; break;
		case 4: r = t; g = p; bl = b; break;
		default: r = b; g = p; bl = q; break;
		}
		return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (bl * 255);
	}

	/** Returns hue, saturation, brightness of the current rgb value. */
	public float[] toHsb() {
		int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
		float max = Math.max(r, Math.max(g, b)) / 255f;
		float min = Math.min(r, Math.min(g, b)) / 255f;
		float d = max - min;
		float h = 0f;
		if (d > 0.0001f) {
			float rf = r / 255f, gf = g / 255f, bf = b / 255f;
			if (max == rf) {
				h = ((gf - bf) / d) / 6f;
			} else if (max == gf) {
				h = (2f + (bf - rf) / d) / 6f;
			} else {
				h = (4f + (rf - gf) / d) / 6f;
			}
			if (h < 0f) {
				h += 1f;
			}
		}
		return new float[] { h, max <= 0f ? 0f : d / max, max };
	}

	public String write() {
		return (chroma ? "chroma:" : "") + Integer.toHexString(alpha) + ":" + Integer.toHexString(rgb);
	}

	public void read(String v) {
		try {
			if (v.startsWith("chroma:")) {
				chroma = true;
				v = v.substring(7);
			} else {
				chroma = false;
			}
			int sep = v.indexOf(':');
			if (sep > 0) {
				alpha = Integer.parseInt(v.substring(0, sep), 16);
				rgb = Integer.parseInt(v.substring(sep + 1), 16) & 0xFFFFFF;
			}
		} catch (RuntimeException ignored) {
		}
	}

	public void reset() {
		rgb = defaultRgb & 0xFFFFFF;
		alpha = (defaultRgb >>> 24) == 0 ? 255 : (defaultRgb >>> 24);
		chroma = false;
	}
}
