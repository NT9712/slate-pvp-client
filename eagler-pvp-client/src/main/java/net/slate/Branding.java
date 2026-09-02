package net.slate;

import net.slate.ui.Draw;
import net.slate.ui.Theme;

/** Client wordmark and the server the client ships pointed at. */
public final class Branding {

	/** Pinned default server entry (red/blue bold), used by ServerList. */
	public static final String SERVER_NAME = "\u00A7c\u00A7lInfinity\u00A79\u00A7lMC";
	public static final String SERVER_ADDRESS = "wss://infinitymc.oops.wtf";

	private static final String WORDMARK = "Slate";

	private Branding() {
	}

	/** Width of the wordmark at the given scale, in GUI pixels. */
	public static int wordmarkWidth(float scale) {
		return Math.round(Draw.width(WORDMARK) * scale);
	}

	/**
	 * Draws the wordmark centred on cx with an accent rule beneath it. Emboldened by
	 * overdrawing a pixel across rather than the font's own bold, which smears at scale.
	 */
	public static void drawWordmark(float cx, float y, float scale) {
		float w = wordmarkWidth(scale);
		float x = cx - w * 0.5f;

		com.mojang.blaze3d.platform.GlStateManager.pushMatrix();
		com.mojang.blaze3d.platform.GlStateManager.translatef(x, y, 0f);
		com.mojang.blaze3d.platform.GlStateManager.scalef(scale, scale, 1f);
		Draw.textNoShadow(WORDMARK, 0.6f, 0.6f, 0x66000000);
		Draw.textNoShadow(WORDMARK, 0f, 0f, Theme.TEXT);
		com.mojang.blaze3d.platform.GlStateManager.popMatrix();

		float ruleW = w * 0.55f;
		Draw.rect(cx - ruleW * 0.5f, y + 9f * scale + 6f, ruleW, 2f, Theme.ACCENT);
	}
}
