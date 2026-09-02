package net.slate.ui;

import net.minecraft.client.gui.widget.button.Button;

/** Modern flat button in CS2 style - used on all menus. */
public class FlatButton extends Button {

	private final boolean primary;
	private final boolean destructive;
	private float hover;
	private float press;

	public FlatButton(int x, int y, int width, int height, String text, boolean primary, IPressable onPress) {
		this(x, y, width, height, text, primary, false, onPress);
	}

	public FlatButton(int x, int y, int width, int height, String text, boolean primary, boolean destructive, IPressable onPress) {
		super(x, y, width, height, text, onPress);
		this.primary = primary;
		this.destructive = destructive;
	}

	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) return;
		
		boolean hovered = isHovered();
		hover = Draw.approach(hover, hovered ? 1f : 0f, 0.25f, net.slate.Slate.delta());
		press = Draw.approach(press, (hovered && net.slate.Slate.isMouseDown()) ? 1f : 0f, 0.35f, net.slate.Slate.delta());

		// Base colors
		int bgBase, bgHover, textColor, borderColor;
		
		if (destructive) {
			bgBase = Theme.OFF;
			bgHover = Theme.BAD;
			textColor = Theme.TEXT;
			borderColor = Theme.BORDER;
		} else if (primary) {
			bgBase = Theme.ACCENT;
			bgHover = Theme.ACCENT_HOVER;
			textColor = 0xFFFFFFFF;
			borderColor = Theme.ACCENT;
		} else {
			bgBase = Theme.PANEL_LIGHT;
			bgHover = Theme.ROW_HOVER;
			textColor = Theme.TEXT;
			borderColor = Theme.BORDER;
		}

		// Interpolate colors
		int bgColor = Theme.mix(bgBase, bgHover, hover);
		if (press > 0f) {
			int pressColor = primary ? Theme.ACCENT_PRESS : (destructive ? 0xFFB84030 : Theme.ROW_ACTIVE);
			bgColor = Theme.mix(bgColor, pressColor, press * 0.5f);
		}

		// Apply alpha
		bgColor = Theme.alpha(bgColor, alpha);
		borderColor = Theme.alpha(borderColor, alpha);
		textColor = Theme.alpha(textColor, alpha);

		// Draw button background with rounded corners
		Draw.roundRect(x, y, width, height, Theme.RADIUS, bgColor);
		
		// Draw border for non-primary buttons
		if (!primary && !destructive) {
			Draw.outline(x, y, width, height, Theme.RADIUS, borderColor);
		}

		// Draw subtle inner highlight on hover (top edge)
		if (hover > 0.1f && !primary) {
			int highlight = Theme.alpha(Theme.mix(0x00000000, 0x10FFFFFF, hover), alpha);
			Draw.roundRectTop(x, y, width, height, Theme.RADIUS, highlight);
		}

		// Draw text centered
		Draw.textCentred(getMessage(), x + width * 0.5f, y + (height - Draw.TEXT_H) * 0.5f + 1f, textColor);
	}

	public static FlatButton primary(int x, int y, int width, int height, String text, IPressable onPress) {
		return new FlatButton(x, y, width, height, text, true, onPress);
	}

	public static FlatButton secondary(int x, int y, int width, int height, String text, IPressable onPress) {
		return new FlatButton(x, y, width, height, text, false, onPress);
	}

	public static FlatButton destructive(int x, int y, int width, int height, String text, IPressable onPress) {
		return new FlatButton(x, y, width, height, text, false, true, onPress);
	}

	public static FlatButton compact(int x, int y, String text, IPressable onPress) {
		int w = Math.max(80, Draw.width(text) + 24);
		return new FlatButton(x, y, w, 28, text, false, onPress);
	}
}