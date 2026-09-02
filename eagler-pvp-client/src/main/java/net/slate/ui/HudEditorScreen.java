package net.slate.ui;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.slate.Config;
import net.slate.Slate;
import net.slate.hud.HudManager;
import net.slate.module.HudModule;

/** Drag HUD elements around. Opened from the client menu with E. */
public class HudEditorScreen extends Screen {

	private static final int SNAP = 4;

	private HudModule dragging;
	private int grabX, grabY;
	private HudModule hovered;
	private int guideX = -1, guideY = -1;

	public HudEditorScreen() {
		super(new StringTextComponent("HUD Editor"));
	}

	public boolean isPauseScreen() {
		return false;
	}

	public void render(int mouseX, int mouseY, float partialTicks) {
		Slate.updateDelta();
		Draw.rect(0, 0, width, height, 0x66000000);

		List<HudModule> list = elements();
		hovered = null;

		for (int i = 0; i < list.size(); i++) {
			HudModule m = list.get(i);
			int[] b = box(m);
			if (!m.isEnabled()) {
				// hidden elements stay visible as ghosts so they can be brought back
				Draw.rect(b[0], b[1], b[2], b[3], 0x18FFFFFF);
				Draw.text(m.name, b[0] + 4, b[1] + (b[3] - Draw.TEXT_H) * 0.5f, Theme.TEXT_FAINT);
				Draw.outline(b[0] - 2, b[1] - 2, b[2] + 4, b[3] + 4, 0x1AFFFFFF);
				if (mouseX >= b[0] - 2 && mouseX <= b[0] + b[2] + 2 && mouseY >= b[1] - 2
						&& mouseY <= b[1] + b[3] + 2 && dragging == null) {
					hovered = m;
				}
				continue;
			}
			boolean hover = mouseX >= b[0] - 2 && mouseX <= b[0] + b[2] + 2 && mouseY >= b[1] - 2
					&& mouseY <= b[1] + b[3] + 2;
			if (hover && dragging == null) {
				hovered = m;
			}
			if (m.width() > 0 && m.height() > 0) {
				HudManager.draw(m, partialTicks);
			} else {
				// nothing to show right now (no combo, no target) - draw a stand-in so it can still be placed
				Draw.rect(b[0], b[1], b[2], b[3], 0x30FFFFFF);
				Draw.text(m.name, b[0] + 4, b[1] + 3, Theme.TEXT_DIM);
			}
			boolean active = dragging == m || (dragging == null && hover);
			Draw.outline(b[0] - 2, b[1] - 2, b[2] + 4, b[3] + 4, active ? Theme.ACCENT : 0x33FFFFFF);
		}

		if (guideX >= 0) {
			Draw.rect(guideX, 0, 1, height, 0x66FFFFFF);
		}
		if (guideY >= 0) {
			Draw.rect(0, guideY, width, 1, 0x66FFFFFF);
		}

		renderBar();
	}

	/** Bounds of an element, with a stand-in size when it currently draws nothing. */
	private int[] box(HudModule m) {
		int[] b = HudManager.bounds(m);
		if (b[2] <= 0 || b[3] <= 0) {
			b[2] = Draw.width(m.name) + 8;
			b[3] = 15;
		}
		return b;
	}

	private static final String[][] HINTS = { { "Drag", "move" }, { "Scroll", "size" }, { "Arrows", "nudge" },
			{ "Right click", "show/hide" }, { "B", "backgrounds" }, { "R", "reset" }, { "Esc", "back" } };

	private void renderBar() {
		int w = 24;
		for (int i = 0; i < HINTS.length; i++) {
			w += Draw.smallWidth(HINTS[i][0]) + 6 + 4 + Draw.smallWidth(HINTS[i][1]) + 10;
		}
		float x = (width - w) * 0.5f;
		float y = height - 64;
		Draw.roundRect(x, y, w, 18, 4f, Theme.PANEL);
		float cx = x + 12;
		for (int i = 0; i < HINTS.length; i++) {
			int kw = Draw.smallWidth(HINTS[i][0]) + 6;
			Draw.roundRect(cx, y + 5, kw, 9, 2f, 0x14FFFFFF);
			Draw.textSmall(HINTS[i][0], cx + 3, y + 7, Theme.TEXT_MUTED);
			cx += kw + 4;
			Draw.textSmall(HINTS[i][1], cx, y + 7, Theme.TEXT_FAINT);
			cx += Draw.smallWidth(HINTS[i][1]) + 10;
		}
		if (HudManager.backgrounds) {
			Draw.textSmall("Backgrounds on", x + 12, y - 8, Theme.TEXT_FAINT);
		}
	}

	private List<HudModule> cachedElements;

	private List<HudModule> elements() {
		if (cachedElements == null) {
			cachedElements = HudManager.all();
		}
		return cachedElements;
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		List<HudModule> list = elements();
		for (int i = list.size() - 1; i >= 0; i--) {
			HudModule m = list.get(i);
			int[] b = box(m);
			if (mouseX >= b[0] - 2 && mouseX <= b[0] + b[2] + 2 && mouseY >= b[1] - 2 && mouseY <= b[1] + b[3] + 2) {
				if (button == 1) {
					m.setEnabled(!m.isEnabled());
					net.slate.Config.markDirty();
					return true;
				}
				if (!m.isEnabled()) {
					return true;
				}
				dragging = m;
				grabX = (int) mouseX - b[0];
				grabY = (int) mouseY - b[1];
				return true;
			}
		}
		return true;
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
		if (dragging == null) {
			return false;
		}
		int[] b = box(dragging);
		int x = (int) mouseX - grabX;
		int y = (int) mouseY - grabY;
		guideX = guideY = -1;

		// snap to screen edges and centre
		x = snap(x, 0, b[2], width, true);
		y = snap(y, 0, b[3], height, false);

		dragging.setScreenPos(x, y);
		return true;
	}

	private int snap(int v, int min, int size, int screen, boolean horizontal) {
		int[] targets = new int[] { 2, (screen - size) / 2, screen - size - 2 };
		for (int i = 0; i < targets.length; i++) {
			if (Math.abs(v - targets[i]) <= SNAP) {
				if (horizontal) {
					guideX = i == 1 ? screen / 2 : (i == 0 ? 2 : screen - 2);
				} else {
					guideY = i == 1 ? screen / 2 : (i == 0 ? 2 : screen - 2);
				}
				return targets[i];
			}
		}
		return v;
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		dragging = null;
		guideX = guideY = -1;
		Config.save();
		return true;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		HudModule m = hovered != null ? hovered : dragging;
		if (m == null) {
			return false;
		}
		for (int i = 0; i < m.settings().size(); i++) {
			if ("Scale".equals(m.settings().get(i).name)) {
				net.slate.module.setting.NumberSetting n = (net.slate.module.setting.NumberSetting) m.settings().get(i);
				n.set(n.get() + (delta > 0 ? 0.05D : -0.05D));
				break;
			}
		}
		return true;
	}

	public boolean keyPressed(int key, int scanCode, int modifiers) {
		HudModule m = hovered != null ? hovered : dragging;
		if (key == 66) { // B
			HudManager.backgrounds = !HudManager.backgrounds;
			Config.save();
			return true;
		}
		if (key == 82) { // R
			List<HudModule> list = elements();
			for (int i = 0; i < list.size(); i++) {
				for (int j = 0; j < list.get(i).settings().size(); j++) {
					if ("Scale".equals(list.get(i).settings().get(j).name)) {
						((net.slate.module.setting.NumberSetting) list.get(i).settings().get(j)).set(1.0D);
					}
				}
			}
			Config.save();
			return true;
		}
		if (m != null && key >= 262 && key <= 265) {
			int[] b = box(m);
			int dx = key == 262 ? 1 : (key == 263 ? -1 : 0);
			int dy = key == 264 ? 1 : (key == 265 ? -1 : 0);
			m.setScreenPos(b[0] + dx, b[1] + dy);
			return true;
		}
		if (key == 256) {
			mc.displayGuiScreen(new ClickGuiScreen());
			return true;
		}
		return super.keyPressed(key, scanCode, modifiers);
	}

	public void removed() {
		Config.save();
	}
}
