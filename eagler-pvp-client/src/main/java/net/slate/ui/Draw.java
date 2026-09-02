package net.slate.ui;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

/**
 * Small set of float-precision 2D drawing helpers shared by the menu and the HUD.
 * Deliberately tiny: rectangles, rounded rectangles, text and scissoring.
 */
public final class Draw {

	private Draw() {
	}

	private static Minecraft mc() {
		return Minecraft.getInstance();
	}

	public static FontRenderer font() {
		return Minecraft.getInstance().fontRenderer;
	}

	// --------------------------------------------------------------- state

	private static void begin() {
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
	}

	private static void end() {
		GlStateManager.enableAlphaTest();
		GlStateManager.enableTexture();
		GlStateManager.disableBlend();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
	}

	// ---------------------------------------------------------------- rect

	public static void rect(float x, float y, float w, float h, int color) {
		if (w <= 0f || h <= 0f || (color >>> 24) == 0) {
			return;
		}
		begin();
		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();
		b.begin(7, DefaultVertexFormats.POSITION_COLOR);
		quad(b, x, y, x + w, y + h, color);
		t.draw();
		end();
	}

	public static void gradient(float x, float y, float w, float h, int top, int bottom) {
		if (w <= 0f || h <= 0f) {
			return;
		}
		begin();
		GlStateManager.shadeModel(7425);
		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();
		b.begin(7, DefaultVertexFormats.POSITION_COLOR);
		put(b, x, y + h, bottom);
		put(b, x + w, y + h, bottom);
		put(b, x + w, y, top);
		put(b, x, y, top);
		t.draw();
		GlStateManager.shadeModel(7424);
		end();
	}

	private static boolean batching;

	/**
	 * Opens a single quad buffer. Everything drawn with {@link #batchRect} until
	 * {@link #batchEnd()} goes out in one draw call. Do not draw text inside a batch.
	 */
	public static void batchStart() {
		begin();
		Tessellator.getInstance().getBuffer().begin(7, DefaultVertexFormats.POSITION_COLOR);
		batching = true;
	}

	public static void batchRect(float x, float y, float w, float h, int color) {
		if (w <= 0f || h <= 0f || (color >>> 24) == 0) {
			return;
		}
		quad(Tessellator.getInstance().getBuffer(), x, y, x + w, y + h, color);
	}

	public static void batchEnd() {
		Tessellator.getInstance().draw();
		batching = false;
		end();
	}

	public static void gradientH(float x, float y, float w, float h, int left, int right) {
		if (w <= 0f || h <= 0f) {
			return;
		}
		begin();
		GlStateManager.shadeModel(7425);
		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();
		b.begin(7, DefaultVertexFormats.POSITION_COLOR);
		put(b, x, y + h, left);
		put(b, x + w, y + h, right);
		put(b, x + w, y, right);
		put(b, x, y, left);
		t.draw();
		GlStateManager.shadeModel(7424);
		end();
	}

	/** 1px (logical) outline drawn inside the given bounds, in one draw call. */
	public static void outline(float x, float y, float w, float h, int color) {
		outline(x, y, w, h, 0f, color);
	}

	/** 1px outline with optional corner radius. */
	public static void outline(float x, float y, float w, float h, float r, int color) {
		if ((color >>> 24) == 0) {
			return;
		}
		boolean own = !batching;
		if (own) {
			batchStart();
		}
		if (r <= 0.5f) {
			batchRect(x, y, w, 1f, color);
			batchRect(x, y + h - 1f, w, 1f, color);
			batchRect(x, y + 1f, 1f, h - 2f, color);
			batchRect(x + w - 1f, y + 1f, 1f, h - 2f, color);
		} else {
			// Draw outline with rounded corners - approximate with straight segments
			batchRect(x + r, y, w - 2 * r, 1f, color);
			batchRect(x + r, y + h - 1f, w - 2 * r, 1f, color);
			batchRect(x, y + r, 1f, h - 2 * r, color);
			batchRect(x + w - 1f, y + r, 1f, h - 2 * r, color);
		}
		if (own) {
			batchEnd();
		}
	}

	public static void roundRect(float x, float y, float w, float h, float r, int color) {
		if (w <= 0f || h <= 0f || (color >>> 24) == 0) {
			return;
		}
		if (r > w * 0.5f) {
			r = w * 0.5f;
		}
		if (r > h * 0.5f) {
			r = h * 0.5f;
		}
		begin();
		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();
		b.begin(7, DefaultVertexFormats.POSITION_COLOR);
		if (r <= 0.5f) {
			quad(b, x, y, x + w, y + h, color);
		} else {
			quad(b, x, y + r, x + w, y + h - r, color);
			int steps = (int) Math.ceil(r);
			for (int i = 0; i < steps; i++) {
				float y0 = i;
				float y1 = Math.min(r, i + 1f);
				float dy = r - (y0 + y1) * 0.5f;
				float dx = r - (float) Math.sqrt(Math.max(0f, r * r - dy * dy));
				quad(b, x + dx, y + y0, x + w - dx, y + y1, color);
				quad(b, x + dx, y + h - y1, x + w - dx, y + h - y0, color);
			}
		}
		t.draw();
		end();
	}

	public static void roundRectTop(float x, float y, float w, float h, float r, int color) {
		if (w <= 0f || h <= 0f || (color >>> 24) == 0) {
			return;
		}
		if (r > w * 0.5f) r = w * 0.5f;
		if (r > h * 0.5f) r = h * 0.5f;
		begin();
		Tessellator t = Tessellator.getInstance();
		BufferBuilder b = t.getBuffer();
		b.begin(7, DefaultVertexFormats.POSITION_COLOR);
		if (r <= 0.5f) {
			quad(b, x, y, x + w, y + h, color);
		} else {
			// Only draw top portion with rounded corners
			float drawH = Math.min(r * 2, h);
			quad(b, x, y + r, x + w, y + drawH - r, color);
			int steps = (int) Math.ceil(r);
			for (int i = 0; i < steps; i++) {
				float y0 = i;
				float y1 = Math.min(r, i + 1f);
				float dy = r - (y0 + y1) * 0.5f;
				float dx = r - (float) Math.sqrt(Math.max(0f, r * r - dy * dy));
				quad(b, x + dx, y + y0, x + w - dx, y + y1, color);
			}
		}
		t.draw();
		end();
	}

	private static void quad(BufferBuilder b, float x1, float y1, float x2, float y2, int color) {
		put(b, x1, y2, color);
		put(b, x2, y2, color);
		put(b, x2, y1, color);
		put(b, x1, y1, color);
	}

	private static void put(BufferBuilder b, float x, float y, int color) {
		b.pos(x, y, 0.0D).color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >>> 24)).endVertex();
	}

	// ---------------------------------------------------------------- text

	public static int text(String s, float x, float y, int color) {
		return font().drawStringWithShadow(s, x, y, color);
	}

	/**
	 * Text with a full outline. HUD text has to survive snow, sand and a bright sky, which a
	 * single pixel drop shadow does not manage.
	 */
	public static void textOutlined(String s, float x, float y, int color) {
		int shade = (int) ((color >>> 24) * 0.72f) << 24;
		font().drawString(s, x - 1f, y, shade);
		font().drawString(s, x + 1f, y, shade);
		font().drawString(s, x, y - 1f, shade);
		font().drawString(s, x, y + 1f, shade);
		font().drawString(s, x, y, color);
	}

	public static void textOutlinedRight(String s, float right, float y, int color) {
		textOutlined(s, right - width(s), y, color);
	}

	/** Half size text, used for menu chrome. Falls back to normal size when it would blur. */
	public static void textSmall(String s, float x, float y, int color) {
		if (Minecraft.getInstance().mainWindow.getGuiScaleFactor() < 2.0D) {
			font().drawStringWithShadow(s, x, y, color);
			return;
		}
		GlStateManager.pushMatrix();
		GlStateManager.translatef(x, y, 0f);
		GlStateManager.scalef(0.5f, 0.5f, 1f);
		font().drawString(s, 0f, 0f, color);
		GlStateManager.popMatrix();
	}

	/** Half size text with an outline, for small readings drawn over the world. */
	public static void textSmallOutlined(String s, float x, float y, int color) {
		if (Minecraft.getInstance().mainWindow.getGuiScaleFactor() < 2.0D) {
			textOutlined(s, x, y, color);
			return;
		}
		int shade = (int) ((color >>> 24) * 0.72f) << 24;
		GlStateManager.pushMatrix();
		GlStateManager.translatef(x, y, 0f);
		GlStateManager.scalef(0.5f, 0.5f, 1f);
		font().drawString(s, -1f, 0f, shade);
		font().drawString(s, 1f, 0f, shade);
		font().drawString(s, 0f, -1f, shade);
		font().drawString(s, 0f, 1f, shade);
		font().drawString(s, 0f, 0f, color);
		GlStateManager.popMatrix();
	}

	public static void textSmallRight(String s, float right, float y, int color) {
		textSmall(s, right - smallWidth(s), y, color);
	}

	public static int smallWidth(String s) {
		int w = width(s);
		return Minecraft.getInstance().mainWindow.getGuiScaleFactor() < 2.0D ? w : (w + 1) / 2;
	}

	public static int smallHeight() {
		return Minecraft.getInstance().mainWindow.getGuiScaleFactor() < 2.0D ? TEXT_H : 5;
	}

	public static int textNoShadow(String s, float x, float y, int color) {
		return font().drawString(s, x, y, color);
	}

	public static void textCentred(String s, float cx, float y, int color) {
		font().drawStringWithShadow(s, cx - font().getStringWidth(s) * 0.5f, y, color);
	}

	public static void textRight(String s, float right, float y, int color) {
		font().drawStringWithShadow(s, right - font().getStringWidth(s), y, color);
	}

	public static int width(String s) {
		return font().getStringWidth(s);
	}

	/** Trims to fit, adding an ellipsis when it has to cut. */
	public static String fit(String s, int maxWidth) {
		if (s == null || width(s) <= maxWidth) {
			return s;
		}
		String trimmed = font().trimStringToWidth(s, Math.max(0, maxWidth - width("..")));
		return trimmed + "..";
	}

	/** Small right-pointing (collapsed) or down-pointing (expanded) disclosure triangle. */
	public static void chevron(float x, float y, boolean expanded, int color) {
		if (expanded) {
			for (int i = 0; i < 3; i++) {
				rect(x + i, y + i, 5 - i * 2, 1, color);
			}
		} else {
			for (int i = 0; i < 3; i++) {
				rect(x + i, y + i, 1, 5 - i * 2, color);
			}
		}
	}

	/** Alpha checkerboard, so a transparent colour is readable. One draw call. */
	public static void checker(float x, float y, float w, float h, int size) {
		batchStart();
		batchRect(x, y, w, h, 0xFF3A3A3A);
		for (int i = 0; i * size < w; i++) {
			for (int j = 0; j * size < h; j++) {
				if (((i + j) & 1) == 0) {
					batchRect(x + i * size, y + j * size, Math.min(size, w - i * size),
							Math.min(size, h - j * size), 0xFF565656);
				}
			}
		}
		batchEnd();
	}

	/** Vanilla font height. */
	public static final int TEXT_H = 9;

	// ------------------------------------------------------------- scissor

	public static void scissorStart(double x, double y, double w, double h) {
		Minecraft mc = mc();
		double s = mc.mainWindow.getGuiScaleFactor();
		int fbH = mc.mainWindow.getFramebufferHeight();
		GlStateManager.enableScissorTest();
		GlStateManager.scissor((int) (x * s), (int) (fbH - (y + h) * s), (int) (w * s), (int) (h * s));
	}

	public static void scissorEnd() {
		GlStateManager.disableScissorTest();
	}

	// -------------------------------------------------------------- easing

	/** Frame-rate independent exponential approach. */
	public static float approach(float current, float target, float speed, float delta) {
		float f = 1f - (float) Math.pow(1f - speed, delta);
		return current + (target - current) * f;
	}

	public static float easeOut(float t) {
		float u = 1f - t;
		return 1f - u * u * u;
	}

	public static int getStringWidth(String s) {
		return width(s);
	}
}