package net.slate.hud;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.slate.Slate;
import net.slate.module.HudModule;
import net.slate.module.Module;
import net.slate.ui.Draw;
import net.slate.ui.Theme;

/** Lays out and draws every enabled HUD element. */
public final class HudManager {

	/** Global look of HUD elements - one switch, applies to everything. */
	public static boolean backgrounds = false;

	private static final List<HudModule> visible = new ArrayList<HudModule>();

	private HudManager() {
	}

	public static List<HudModule> all() {
		List<HudModule> list = new ArrayList<HudModule>();
		for (Module m : Slate.modules().all()) {
			if (m instanceof HudModule) {
				list.add((HudModule) m);
			}
		}
		return list;
	}

	/** Elements that should be drawn right now. */
	public static List<HudModule> active() {
		visible.clear();
		for (Module m : Slate.modules().all()) {
			if (m.isEnabled() && m instanceof HudModule) {
				visible.add((HudModule) m);
			}
		}
		return visible;
	}

	public static void render(float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.gameSettings.hideGUI) {
			return;
		}
		// the client menu owns the screen while it is open
		if (mc.currentScreen instanceof net.slate.ui.ClickGuiScreen) {
			return;
		}
		List<HudModule> list = active();
		if (list.isEmpty()) {
			return;
		}
		GlStateManager.pushMatrix();
		GlStateManager.disableDepthTest();
		for (int i = 0; i < list.size(); i++) {
			draw(list.get(i), partialTicks);
		}
		GlStateManager.enableDepthTest();
		GlStateManager.popMatrix();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
	}

	public static void draw(HudModule m, float partialTicks) {
		m.updateLayout();
		if (m.width() <= 0 || m.height() <= 0) {
			return;
		}
		float s = m.getScale();
		int x = m.screenX();
		int y = m.screenY();
		GlStateManager.pushMatrix();
		GlStateManager.disableDepthTest();
		GlStateManager.translatef(x, y, 0f);
		if (s != 1f) {
			GlStateManager.scalef(s, s, 1f);
		}
		if (backgrounds) {
			Draw.roundRect(-3f, -2f, m.width() + 6f, m.height() + 4f, 3f, Theme.HUD_BG);
		}
		m.render(partialTicks);
		GlStateManager.popMatrix();
		GlStateManager.enableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableTexture();
		GlStateManager.enableDepthTest();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
	}

	private static final int[] boundsBuffer = new int[4];

	/** Scaled bounds of an element, for the editor. Returns a shared buffer. */
	public static int[] bounds(HudModule m) {
		m.updateLayout();
		float s = m.getScale();
		boundsBuffer[0] = m.screenX();
		boundsBuffer[1] = m.screenY();
		boundsBuffer[2] = Math.round(m.width() * s);
		boundsBuffer[3] = Math.round(m.height() * s);
		return boundsBuffer;
	}
}
