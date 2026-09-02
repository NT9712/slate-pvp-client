package net.slate.module.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.ColorSetting;
import net.slate.module.setting.ModeSetting;
import net.slate.module.setting.NumberSetting;
import net.slate.ui.Draw;

/** Custom crosshair drawn in place of the vanilla one. */
public final class CrosshairModule extends Module {

	private static CrosshairModule INSTANCE;

	private static final int OUTLINE = 0xFF000000;

	private final ModeSetting style = add(new ModeSetting("Style", 0, "Cross", "Dot", "Cross + Dot", "Circle"));
	private final NumberSetting size = add(new NumberSetting("Size", 4.0D, 1.0D, 10.0D, 1.0D));
	private final NumberSetting gap = add(new NumberSetting("Gap", 2.0D, 0.0D, 6.0D, 1.0D));
	private final NumberSetting thickness = add(new NumberSetting("Thickness", 1.0D, 1.0D, 3.0D, 1.0D));
	private final ColorSetting colour = add(new ColorSetting("Colour", 0xFFFFFFFF));
	private final BoolSetting outline = add(new BoolSetting("Outline", true));
	private final BoolSetting dynamic = add(new BoolSetting("Dynamic", false));

	public CrosshairModule() {
		super("Crosshair", "Replaces the vanilla crosshair.", Category.COMBAT);
		INSTANCE = this;
	}


	/** Returns true when it drew, in which case the vanilla crosshair should be skipped. */
	public static boolean renderCrosshair(int screenWidth, int screenHeight) {
		CrosshairModule m = INSTANCE;
		if (m == null || !m.isEnabled()) {
			return false;
		}
		m.draw(screenWidth * 0.5f, screenHeight * 0.5f);
		return true;
	}

	private void draw(float cx, float cy) {
		Draw.batchStart();
		drawShape(cx, cy);
		Draw.batchEnd();
	}

	private void drawShape(float cx, float cy) {
		int c = colour.get();
		float t = thickness.getFloat();
		float g = gap.getFloat() + dynamicGap();
		float len = size.getFloat();

		if (style.is("Circle")) {
			float r = len + g;
			for (int i = 0; i < 16; i++) {
				double a = Math.PI * 2.0 * i / 16.0;
				box(cx + (float) Math.cos(a) * r - t * 0.5f, cy + (float) Math.sin(a) * r - t * 0.5f, t, t, c);
			}
			return;
		}

		if (!style.is("Dot")) {
			box(cx - t * 0.5f, cy - g - len, t, len, c);
			box(cx - t * 0.5f, cy + g, t, len, c);
			box(cx - g - len, cy - t * 0.5f, len, t, c);
			box(cx + g, cy - t * 0.5f, len, t, c);
		}
		if (style.is("Dot") || style.is("Cross + Dot")) {
			box(cx - t * 0.5f, cy - t * 0.5f, t, t, c);
		}
	}

	private void box(float x, float y, float w, float h, int c) {
		if (outline.get()) {
			Draw.batchRect(x - 1f, y - 1f, w + 2f, h + 2f, OUTLINE);
		}
		Draw.batchRect(x, y, w, h, c);
	}

	private float dynamicGap() {
		if (!dynamic.get()) {
			return 0f;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return 0f;
		}
		Vec3d motion = mc.player.getMotion();
		float speed = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
		float f = Math.min(1f, speed / 0.25f);
		float cooldown = 1f - mc.player.getCooledAttackStrength(0f);
		if (cooldown > f) {
			f = cooldown;
		}
		return 3f * Math.max(0f, Math.min(1f, f));
	}
}
