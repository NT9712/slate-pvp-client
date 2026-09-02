package net.slate.module;

import net.minecraft.client.Minecraft;
import net.slate.module.setting.NumberSetting;

/**
 * A module that draws something on the HUD.
 *
 * Position is stored as a pixel offset from an anchor edge rather than a fraction of the
 * screen, so elements keep their spacing and stay pinned to the edge they were placed
 * against no matter what the resolution or GUI scale is.
 */
public abstract class HudModule extends Module {

	public enum HAlign {
		LEFT, CENTRE, RIGHT
	}

	public enum VAlign {
		TOP, BOTTOM
	}

	private HAlign hAlign;
	private VAlign vAlign;
	private int ox, oy;

	protected final NumberSetting scale;

	private int width = 40;
	private int height = 10;

	protected HudModule(String name, String description, HAlign h, VAlign v, int ox, int oy) {
		this(name, description, Category.HUD, h, v, ox, oy);
	}

	protected HudModule(String name, String description, Category category, HAlign h, VAlign v, int ox, int oy) {
		super(name, description, category);
		this.hAlign = h;
		this.vAlign = v;
		this.ox = ox;
		this.oy = oy;
		this.scale = add(new NumberSetting("Scale", 1.0D, 0.5D, 2.0D, 0.1D, "x"));
	}

	/** Draw the element with its top-left corner at (0,0); the manager applies the transform. */
	public abstract void render(float partialTicks);

	/** Called every frame before render() so the element can report its current size. */
	public void updateLayout() {
	}

	protected final void setSize(int w, int h) {
		this.width = w;
		this.height = h;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public float getScale() {
		return scale.getFloat();
	}

	private int scaledWidth() {
		return Math.round(width * getScale());
	}

	private int scaledHeight() {
		return Math.round(height * getScale());
	}

	/** Absolute, unscaled screen position of the top-left corner. */
	public int screenX() {
		int sw = Minecraft.getInstance().mainWindow.getScaledWidth();
		int w = scaledWidth();
		int x;
		if (hAlign == HAlign.RIGHT) {
			x = sw - ox - w;
		} else if (hAlign == HAlign.CENTRE) {
			x = (sw - w) / 2 + ox;
		} else {
			x = ox;
		}
		return clamp(x, sw - w);
	}

	public int screenY() {
		int sh = Minecraft.getInstance().mainWindow.getScaledHeight();
		int h = scaledHeight();
		int y = vAlign == VAlign.BOTTOM ? sh - oy - h : oy;
		return clamp(y, sh - h);
	}

	private static int clamp(int v, int max) {
		if (max < 0) {
			max = 0;
		}
		return v < 0 ? 0 : (v > max ? max : v);
	}

	/** Moves the element and re-picks the anchor from where it landed. */
	public void setScreenPos(int px, int py) {
		Minecraft mc = Minecraft.getInstance();
		int sw = mc.mainWindow.getScaledWidth();
		int sh = mc.mainWindow.getScaledHeight();
		int w = scaledWidth();
		int h = scaledHeight();
		float cx = px + w * 0.5f;
		float cy = py + h * 0.5f;

		if (cx < sw * 0.34f) {
			hAlign = HAlign.LEFT;
			ox = px;
		} else if (cx > sw * 0.66f) {
			hAlign = HAlign.RIGHT;
			ox = sw - px - w;
		} else {
			hAlign = HAlign.CENTRE;
			ox = px - (sw - w) / 2;
		}

		if (cy < sh * 0.5f) {
			vAlign = VAlign.TOP;
			oy = py;
		} else {
			vAlign = VAlign.BOTTOM;
			oy = sh - py - h;
		}
	}

	// ------------------------------------------------------------ persistence

	public String writePosition() {
		return hAlign.name() + ":" + vAlign.name() + ":" + ox + ":" + oy;
	}

	public void readPosition(String value) {
		try {
			int a = value.indexOf(':');
			int b = value.indexOf(':', a + 1);
			int c = value.indexOf(':', b + 1);
			if (a < 0 || b < 0 || c < 0) {
				return;
			}
			hAlign = HAlign.valueOf(value.substring(0, a));
			vAlign = VAlign.valueOf(value.substring(a + 1, b));
			ox = Integer.parseInt(value.substring(b + 1, c));
			oy = Integer.parseInt(value.substring(c + 1));
		} catch (RuntimeException ignored) {
		}
	}
}
