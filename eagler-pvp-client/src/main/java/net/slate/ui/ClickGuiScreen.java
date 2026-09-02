package net.slate.ui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;
import net.slate.Config;
import net.slate.Slate;
import net.slate.module.Category;
import net.slate.module.Animated;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.ColorSetting;
import net.slate.module.setting.ModeSetting;
import net.slate.module.setting.NumberSetting;
import net.slate.module.setting.Setting;

/** The client menu. Opened with the menu key (right shift by default). */
public class ClickGuiScreen extends Screen {

	// Layout, all in GUI pixels and all on a 4px grid.
	private static final int W = 340;
	private static final int HEADER = 28;
	private static final int FOOTER = 18;
	private static final int RAIL = 92;
	private static final int PAD = 12;
	private static final int ROW_H = 20;
	private static final int SETTING_H = 16;
	private static final int BLOCK_PAD = 4;
	private static final int H = 186;

	/** Right hand control block of a settings row: slider track, value or toggle. */
	private static final int SLIDER_W = 96;
	private static final int TOGGLE_W = 24;
	private static final int TOGGLE_H = 12;

	/** Remembered between openings so the menu feels like it stays where you left it. */
	private static Category category = Category.COMBAT;
	private static final List<Module> expanded = new ArrayList<Module>();
	private static String query = "";

	private final ColorPicker picker = new ColorPicker();
	private ColorSetting pickerTarget;

	private TextFieldWidget search;
	private final List<Row> rows = new ArrayList<Row>();

	private float open;
	private float scroll;
	private float scrollTarget;
	private Module selected;
	private Setting dragSetting;
	private Module bindingModule;
	private boolean bindingMenuKey;
	private float categoryIndicator = -1f;

	private int panelX, panelY;
	private boolean searching;

	public ClickGuiScreen() {
		super(new StringTextComponent("Slate"));
	}

	public boolean isPauseScreen() {
		return false;
	}

	protected void init() {
		panelX = (width - W) / 2;
		panelY = (height - H) / 2;
		search = new TextFieldWidget(font, panelX + RAIL + 18, panelY + 10, W - RAIL - 30, 10, "");
		search.setEnableBackgroundDrawing(false);
		search.setMaxStringLength(24);
		search.setTextColor(Theme.TEXT);
		search.setText(query);
		search.setCanLoseFocus(true);
		search.setFocused2(false);
		children.add(search);
	}

	public void removed() {
		query = search == null ? "" : search.getText();
		Config.save();
	}

	// ------------------------------------------------------------------ rows

	private static final int DESC_H = 14;

	private static final class Row {
		final Module module;
		final Setting setting;
		final boolean keybind;
		boolean description;
		int y;
		int height;

		Row(Module m, Setting s, boolean keybind, int height) {
			this.module = m;
			this.setting = s;
			this.keybind = keybind;
			this.height = height;
		}
	}

	private int rowSignature = Integer.MIN_VALUE;

	private void buildRowsIfNeeded() {
		int sig = (searchText().hashCode() * 31 + category.ordinal()) * 31 + expanded.size();
		sig = sig * 31 + (pickerTarget == null ? 0 : System.identityHashCode(pickerTarget));
		for (int i = 0; i < expanded.size(); i++) {
			sig = sig * 31 + System.identityHashCode(expanded.get(i)) + expanded.get(i).settings().size();
			List<Setting> st = expanded.get(i).settings();
			for (int j = 0; j < st.size(); j++) {
				sig = sig * 2 + (st.get(j).isVisible() ? 1 : 0);
			}
		}
		if (sig != rowSignature) {
			rowSignature = sig;
			buildRows();
		}
	}

	private String searchText() {
		return search == null ? "" : search.getText();
	}

	private void buildRows() {
		rows.clear();
		String q = searchText().trim().toLowerCase();
		searching = !q.isEmpty();
		List<Module> source = searching ? Slate.modules().all() : Slate.modules().byCategory(category);
		int y = 0;
		for (int i = 0; i < source.size(); i++) {
			Module m = source.get(i);
			if (searching && !m.name.toLowerCase().contains(q) && !m.description.toLowerCase().contains(q)) {
				continue;
			}
			Row r = new Row(m, null, false, ROW_H);
			r.y = y;
			rows.add(r);
			y += ROW_H;
			if (!expanded.contains(m)) {
				continue;
			}
			Row desc = new Row(m, null, false, DESC_H);
			desc.description = true;
			desc.y = y;
			rows.add(desc);
			y += DESC_H;
			List<Setting> settings = m.settings();
			for (int j = 0; j < settings.size(); j++) {
				Setting s = settings.get(j);
				if (!s.isVisible()) {
					continue;
				}
				int h = SETTING_H + (s == pickerTarget ? ColorPicker.HEIGHT + 4 : 0);
				Row sr = new Row(m, s, false, h);
				sr.y = y;
				rows.add(sr);
				y += h;
			}
			Row kb = new Row(m, null, true, SETTING_H + BLOCK_PAD);
			kb.y = y;
			rows.add(kb);
			y += SETTING_H + BLOCK_PAD;
		}
	}

	private int contentHeight() {
		return rows.isEmpty() ? 0 : rows.get(rows.size() - 1).y + rows.get(rows.size() - 1).height;
	}

	private int viewHeight() {
		return H - HEADER - FOOTER;
	}

	// ---------------------------------------------------------------- render

	public void render(int mouseX, int mouseY, float partialTicks) {
		Slate.updateDelta();
		float d = Slate.delta();
		open = Draw.approach(open, 1f, 0.3f, d);
		float e = Draw.easeOut(Math.min(1f, open));

		Draw.rect(0, 0, width, height, Theme.alpha(Theme.BACKDROP, e));

		buildRows();

		int h = H;
		panelY = (height - H) / 2;
		int px = panelX;
		int py = panelY + (int) ((1f - e) * 6f);

		scroll = Draw.approach(scroll, scrollTarget, 0.4f, d);
		scrollTarget = clamp(scrollTarget, 0f, Math.max(0, contentHeight() - viewHeight()));

		Draw.roundRect(px, py, W, h, Theme.RADIUS, Theme.alpha(Theme.PANEL, e));
		Draw.roundRect(px, py, RAIL, h, Theme.RADIUS, Theme.alpha(Theme.RAIL, e));
		Draw.rect(px + RAIL, py + 1, 1, h - 2, Theme.alpha(Theme.DIVIDER, e));

		renderHeader(px, py, e);
		renderRail(px, py, h, mouseX, mouseY, e, d);
		renderList(px, py, h, mouseX, mouseY, e, d);
		renderFooter(px, py, h, e);

		if (e > 0.9f) {
			search.y = py + 10;
			search.render(mouseX, mouseY, partialTicks);
		}
	}

	private void renderHeader(int px, int py, float a) {
		Draw.text(Slate.NAME, px + 16, py + 10, Theme.alpha(Theme.TEXT, a));
		Draw.rect(px, py + HEADER - 1, W, 1, Theme.alpha(Theme.DIVIDER, a));

		float sx = px + RAIL + PAD;
		float sw = W - RAIL - PAD * 2;
		Draw.roundRect(sx, py + 7, sw, 15, 3f,
				Theme.alpha(search != null && search.isFocused() ? 0x1FFFFFFF : 0x12FFFFFF, a));
		if (search != null && search.getText().isEmpty() && !search.isFocused()) {
			Draw.text("Search modules", sx + 6, py + 10, Theme.alpha(Theme.TEXT_FAINT, a));
		}
	}

	private void renderRail(int px, int py, int h, int mouseX, int mouseY, float a, float d) {
		Category[] cats = Category.values();
		int y = py + HEADER + 1;
		int active = y;
		for (int i = 0; i < cats.length; i++) {
			if (cats[i] == category && !searching) {
				active = y;
			}
			y += 20;
		}
		if (categoryIndicator < 0f) {
			categoryIndicator = active;
		}
		categoryIndicator = Draw.approach(categoryIndicator, active, 0.35f, d);
		if (!searching) {
			Draw.roundRect(px + 6, categoryIndicator, RAIL - 12, 20, 3f, Theme.alpha(Theme.ROW_ACTIVE, a));
		}

		y = py + HEADER + 1;
		for (int i = 0; i < cats.length; i++) {
			boolean sel = cats[i] == category && !searching;
			boolean hover = inside(mouseX, mouseY, px + 6, y, RAIL - 12, 20);
			int c = sel ? Theme.TEXT : (hover ? Theme.TEXT_DIM : Theme.TEXT_MUTED);
			Draw.text(cats[i].label, px + 18, y + 5.5f, Theme.alpha(c, a));
			y += 20;
		}

		float ky = py + h - FOOTER + 5;
		Draw.textSmall("Menu", px + 16, ky + 2, Theme.alpha(Theme.TEXT_FAINT, a));
		String label = bindingMenuKey ? "press a key" : keyName(Slate.menuKey);
		int kw = Draw.smallWidth(label) + 8;
		Draw.roundRect(px + RAIL - 8 - kw, ky, kw, 10, 2f, Theme.alpha(0x14FFFFFF, a));
		Draw.textSmall(label, px + RAIL - 4 - kw, ky + 2,
				Theme.alpha(bindingMenuKey ? Theme.ACCENT : Theme.TEXT_MUTED, a));
	}

	private void renderList(int px, int py, int panelHeight, int mouseX, int mouseY, float a, float d) {
		int x = px + RAIL + 1;
		int y = py + HEADER;
		int w = W - RAIL - 1;
		int viewH = panelHeight - HEADER - FOOTER;

		if (rows.isEmpty()) {
			Draw.text("Nothing matches that", x + 18, y + 14, Theme.alpha(Theme.TEXT_MUTED, a));
			return;
		}

		Draw.scissorStart(x, y, w, viewH);
		for (int i = 0; i < rows.size(); i++) {
			Row r = rows.get(i);
			float ry = y + r.y - scroll;
			if (ry + r.height < y || ry > y + viewH) {
				continue;
			}
			if (r.setting == null && !r.keybind && !r.description) {
				renderModuleRow(r, x, ry, w, mouseX, mouseY, a, d);
			} else {
				float sa = a * (r.module.isEnabled() ? 1f : 0.6f);
				Draw.rect(x, ry, w, r.height, Theme.alpha(Theme.SUB_SURFACE, a));
				Draw.rect(x + 14, ry, 1, r.height, Theme.alpha(Theme.GUIDE, a));
				if (r.description) {
					Draw.textSmall(Draw.fit(r.module.description, (w - 38) * 2), x + 26, ry + 5,
							Theme.alpha(Theme.TEXT_MUTED, a));
				} else if (r.keybind) {
					renderKeybindRow(r, x, ry, w, mouseX, mouseY, sa);
				} else {
					renderSettingRow(r, x, ry, w, mouseX, mouseY, sa, d);
				}
			}
		}
		Draw.scissorEnd();

		// soft edges so clipped rows read as "more below" rather than a glitch
		int total = contentHeight();
		if (total > viewH) {
			float track = viewH - 8;
			float thumb = Math.max(16f, track * viewH / total);
			float pos = (scroll / (total - viewH)) * (track - thumb);
			Draw.roundRect(px + W - 8, y + 4, 2, track, 1f, Theme.alpha(0x12FFFFFF, a));
			Draw.roundRect(px + W - 8, y + 4 + pos, 2, thumb, 1f, Theme.alpha(0x40FFFFFF, a));
		}
	}

	private void renderModuleRow(Row r, int x, float y, int w, int mouseX, int mouseY, float a, float d) {
		Module m = r.module;
		boolean hover = inside(mouseX, mouseY, x, (int) y, w, r.height) && overList(mouseY);
		boolean sel = m == selected;
		if (hover) {
			Draw.rect(x, y, w, r.height, Theme.alpha(Theme.ROW_HOVER, a));
		}
		if (sel) {
			Draw.rect(x, y, w, r.height, Theme.alpha(0x0AFFFFFF, a));
			Draw.rect(x, y + 3, 2, r.height - 6, Theme.alpha(Theme.TEXT_DIM, a));
		}

		float ty = y + (r.height - Draw.TEXT_H) * 0.5f;
		if (m.hasVisibleSettings()) {
			Draw.chevron(x + 8, y + (r.height - 5) * 0.5f, expanded.contains(m),
					Theme.alpha(expanded.contains(m) ? Theme.TEXT_DIM : Theme.TEXT_MUTED, a));
		}
		Draw.text(m.name, x + 18, ty, Theme.alpha(m.isEnabled() ? Theme.TEXT : Theme.TEXT_DIM, a));

		String status = searching ? m.category.label : m.status();
		if (status != null) {
			float right = x + w - PAD - TOGGLE_W - 8;
			int room = (int) (right - (x + 18 + Draw.width(m.name) + 8));
			if (room > 20) {
				Draw.textRight(Draw.fit(status, room), right, ty,
						Theme.alpha(Theme.TEXT_MUTED, a * (m.isEnabled() ? 1f : 0.6f)));
			}
		}
		toggle(x + w - PAD - TOGGLE_W, y + (r.height - TOGGLE_H) * 0.5f, m.isEnabled(), a, d, m);
	}

	private float anim(Animated key, boolean on, float d) {
		key.anim = Draw.approach(key.anim, on ? 1f : 0f, 0.4f, d);
		return key.anim;
	}

	private void toggle(float x, float y, boolean on, float a, float d, Animated key) {
		float t = anim(key, on, d);
		Draw.roundRect(x, y, TOGGLE_W, TOGGLE_H, TOGGLE_H * 0.5f,
				Theme.alpha(Theme.mix(Theme.OFF, Theme.ACCENT, t), a));
		float knob = TOGGLE_H - 4f;
		Draw.roundRect(x + 2f + t * (TOGGLE_W - knob - 4f), y + 2f, knob, knob, knob * 0.5f,
				Theme.alpha(Theme.mix(Theme.KNOB_OFF, Theme.KNOB_ON, t), a));
	}

	private void smallToggle(float x, float y, boolean on, float a, float d, Animated key) {
		float t = anim(key, on, d);
		Draw.roundRect(x, y, 18, 9, 4.5f, Theme.alpha(Theme.mix(Theme.OFF, subAccent(), t), a));
		Draw.roundRect(x + 1.5f + t * 8f, y + 1.5f, 6, 6, 3f,
				Theme.alpha(Theme.mix(Theme.KNOB_OFF, Theme.KNOB_ON, t), a));
	}

	/** Sub-settings use a quieter accent so they never out-shout their module. */
	private static int subAccent() {
		return Theme.mix(Theme.ACCENT, Theme.PANEL, 0.3f);
	}

	private void renderSettingRow(Row r, int x, float y, int w, int mouseX, int mouseY, float a, float d) {
		Setting s = r.setting;
		float ty = y + (SETTING_H - Draw.TEXT_H) * 0.5f;
		float controlLeft = x + w - PAD - SLIDER_W - 26;
		Draw.text(Draw.fit(s.name, (int) (controlLeft - (x + 26))), x + 26, ty, Theme.alpha(Theme.TEXT_MUTED, a));

		float right = x + w - PAD;
		if (s instanceof BoolSetting) {
			smallToggle(right - 18, y + (SETTING_H - 9) * 0.5f, ((BoolSetting) s).get(), a, d, s);
		} else if (s instanceof NumberSetting) {
			NumberSetting n = (NumberSetting) s;
			float by = y + SETTING_H * 0.5f - 1.5f;
			float bx = right - SLIDER_W;
			float f = n.fraction();
			Draw.roundRect(bx, by, SLIDER_W, 3, 1.5f, Theme.alpha(0x24FFFFFF, a));
			Draw.roundRect(bx, by, Math.max(2f, SLIDER_W * f), 3, 1.5f, Theme.alpha(subAccent(), a));
			Draw.roundRect(bx + SLIDER_W * f - 2.5f, by - 2f, 5, 7, 2.5f, Theme.alpha(Theme.KNOB_ON, a));
			Draw.textSmallRight(n.display(), bx - 10, y + (SETTING_H - Draw.smallHeight()) * 0.5f,
					Theme.alpha(Theme.TEXT_DIM, a));
		} else if (s instanceof ModeSetting) {
			ModeSetting m = (ModeSetting) s;
			Draw.textRight(">", right, ty, Theme.alpha(Theme.TEXT_MUTED, a));
			Draw.text("<", right - SLIDER_W, ty, Theme.alpha(Theme.TEXT_MUTED, a));
			float centre = right - SLIDER_W * 0.5f;
			String v = Draw.fit(m.get(), (SLIDER_W - 20) * 2);
			Draw.textSmall(v, centre - Draw.smallWidth(v) * 0.5f,
					y + (SETTING_H - Draw.smallHeight()) * 0.5f, Theme.alpha(Theme.TEXT_DIM, a));
		} else if (s instanceof ColorSetting) {
			ColorSetting c = (ColorSetting) s;
			float sw = right - 22;
			float sy2 = y + (SETTING_H - 9) * 0.5f;
			Draw.checker(sw, sy2, 22, 9, 3);
			Draw.rect(sw, sy2, 22, 9, c.get());
			Draw.outline(sw, sy2, 22, 9, Theme.alpha(0xFF2A2D33, a));
			if (c == pickerTarget) {
				picker.render(c, x + 26, y + SETTING_H, a);
			}
		}
	}

	private void renderKeybindRow(Row r, int x, float y, int w, int mouseX, int mouseY, float a) {
		float ty = y + (SETTING_H - Draw.TEXT_H) * 0.5f;
		Draw.text("Keybind", x + 26, ty, Theme.alpha(Theme.TEXT_MUTED, a));
		String label;
		int col;
		if (bindingModule == r.module) {
			label = "press a key";
			col = Theme.ACCENT;
		} else if (r.module.getKey() == 0) {
			label = "none";
			col = Theme.TEXT_FAINT;
		} else {
			label = keyName(r.module.getKey());
			col = Theme.TEXT_DIM;
		}
		int kw = Draw.smallWidth(label) + 8;
		Draw.roundRect(x + w - PAD - kw, y + (SETTING_H - 10) * 0.5f, kw, 10, 2f, Theme.alpha(0x14FFFFFF, a));
		Draw.textSmall(label, x + w - PAD - kw + 4, y + (SETTING_H - Draw.smallHeight()) * 0.5f,
				Theme.alpha(col, a));
	}

	private void renderFooter(int px, int py, int h, float a) {
		int y = py + h - FOOTER;
		int x = px + RAIL + 1;
		int w = W - RAIL - 1;
		Draw.rect(x, y, w, 1, Theme.alpha(Theme.DIVIDER, a));
		float ty = y + 7;
		float cursor = px + W - PAD;
		cursor = hint(keyName(Slate.menuKey), "close", cursor, ty, a);
		cursor = hint("E", "hud editor", cursor - 10, ty, a);
		hint("Enter", "toggle", cursor - 10, ty, a);
	}

	/** key + action pair, key in a chip so the two read as different things. */
	private float hint(String key, String action, float right, float y, float a) {
		float x = right - Draw.smallWidth(action);
		Draw.textSmall(action, x, y, Theme.alpha(Theme.TEXT_FAINT, a));
		int kw = Draw.smallWidth(key) + 6;
		x -= 4 + kw;
		Draw.roundRect(x, y - 2, kw, 9, 2f, Theme.alpha(0x14FFFFFF, a));
		Draw.textSmall(key, x + 3, y, Theme.alpha(Theme.TEXT_MUTED, a));
		return x;
	}

	private boolean overList(int mouseY) {
		return mouseY >= panelY + HEADER && mouseY < panelY + H - FOOTER;
	}

	private static boolean inside(int mx, int my, float x, float y, float w, float h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	// ----------------------------------------------------------------- input

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (bindingModule != null || bindingMenuKey) {
			bindingModule = null; // click anywhere to cancel
			bindingMenuKey = false;
			return true;
		}
		if (search.mouseClicked(mouseX, mouseY, button)) {
			search.setFocused2(true);
			scrollTarget = 0f;
			return true;
		}
		search.setFocused2(false);

		int px = panelX, py = panelY;
		int h = H;
		if (mouseX >= px && mouseX < px + RAIL && mouseY >= py + h - FOOTER) {
			bindingMenuKey = true;
			return true;
		}
		if (mouseX >= px && mouseX < px + RAIL && mouseY >= py + HEADER && mouseY < py + h - FOOTER) {
			Category[] cats = Category.values();
			int y = py + HEADER + 1;
			for (int i = 0; i < cats.length; i++) {
				if (mouseY >= y && mouseY < y + 20) {
					selectCategory(cats[i]);
					return true;
				}
				y += 20;
			}
			return true;
		}

		int lx = px + RAIL + 1;
		int lw = W - RAIL - 1;
		if (mouseX < lx || mouseX > px + W || !overList((int) mouseY)) {
			if (mouseX < px || mouseX > px + W || mouseY < py || mouseY > py + h) {
				onClose();
			}
			return true;
		}

		float listTop = py + HEADER;
		for (int i = 0; i < rows.size(); i++) {
			Row r = rows.get(i);
			float ry = listTop + r.y - scroll;
			if (mouseY < ry || mouseY >= ry + r.height) {
				continue;
			}
			if (r.description) {
				return true;
			}
			if (r.keybind) {
				if (button == 1) {
					r.module.setKey(0);
				} else {
					bindingModule = r.module;
				}
			} else if (r.setting != null) {
				clickSetting(r, lx, ry, lw, mouseX, mouseY, button);
			} else {
				selected = r.module;
				if (mouseX >= lx + lw - PAD - TOGGLE_W - 4) {
					r.module.toggle();
				} else if (r.module.hasVisibleSettings()) {
					if (expanded.contains(r.module)) {
						expanded.remove(r.module);
						pickerTarget = null;
					} else {
						expanded.add(r.module);
						rowSignature = Integer.MIN_VALUE;
						buildRowsIfNeeded();
						ensureBlockVisible(r.module);
					}
				} else {
					r.module.toggle();
				}
			}
			return true;
		}
		return true;
	}

	private void selectCategory(Category c) {
		if (category != c || searching) {
			category = c;
			search.setText("");
			scrollTarget = 0f;
			scroll = 0f;
			selected = null;
		}
	}

	private void clickSetting(Row r, int x, float y, int w, double mouseX, double mouseY, int button) {
		Setting s = r.setting;
		if (button == 1 && !(s instanceof ColorSetting)) {
			s.reset();
			Config.markDirty();
			return;
		}
		Config.markDirty();
		if (s instanceof BoolSetting) {
			((BoolSetting) s).toggle();
		} else if (s instanceof NumberSetting) {
			dragSetting = s;
			dragNumber((NumberSetting) s, x, w, mouseX);
		} else if (s instanceof ModeSetting) {
			ModeSetting m = (ModeSetting) s;
			m.cycle(mouseX < x + w - PAD - SLIDER_W * 0.5f ? -1 : 1);
		} else if (s instanceof ColorSetting) {
			ColorSetting c = (ColorSetting) s;
			if (c == pickerTarget && mouseY > y + SETTING_H) {
				picker.click(c, x + 26, y + SETTING_H, mouseX, mouseY);
			} else {
				picker.close();
				pickerTarget = (pickerTarget == c) ? null : c;
				if (pickerTarget != null) {
					rowSignature = Integer.MIN_VALUE;
					buildRowsIfNeeded();
					for (int i = 0; i < rows.size(); i++) {
						if (rows.get(i).setting == c) {
							ensureVisible(rows.get(i));
							break;
						}
					}
				}
			}
		}
	}

	private void dragNumber(NumberSetting n, int x, int w, double mouseX) {
		float bx = x + w - PAD - SLIDER_W;
		n.setFraction((mouseX - bx) / SLIDER_W);
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		dragSetting = null;
		picker.release();
		return true;
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
		if (dragSetting instanceof NumberSetting) {
			dragNumber((NumberSetting) dragSetting, panelX + RAIL + 1, W - RAIL - 1, mouseX);
			return true;
		}
		if (picker.dragging() && pickerTarget != null) {
			float listTop = panelY + HEADER;
			for (int i = 0; i < rows.size(); i++) {
				Row r = rows.get(i);
				if (r.setting == pickerTarget) {
					picker.drag(pickerTarget, panelX + RAIL + 27, listTop + r.y - scroll + SETTING_H, mouseX, mouseY);
					break;
				}
			}
			return true;
		}
		return false;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		scrollTarget = snap(scrollTarget - (float) delta * ROW_H);
		return true;
	}

	/** Keeps the viewport aligned to a row boundary so no row is ever half drawn. */
	private float snap(float value) {
		return snap(value, false);
	}

	/**
	 * @param up when true, never snap to a boundary above the requested value - otherwise a row
	 *           taller than one line can end up scrolled off the bottom again.
	 */
	private float snap(float value, boolean up) {
		int max = Math.max(0, contentHeight() - viewHeight());
		float v = clamp(value, 0f, max);
		float best = max;
		float bestDist = Math.abs(max - v);
		for (int i = 0; i < rows.size(); i++) {
			float candidate = clamp(rows.get(i).y, 0f, max);
			if (up && candidate < v - 0.5f) {
				continue;
			}
			float dist = Math.abs(candidate - v);
			if (dist < bestDist) {
				bestDist = dist;
				best = candidate;
			}
		}
		return best;
	}

	private static float clamp(float v, float lo, float hi) {
		return v < lo ? lo : (v > hi ? hi : v);
	}

	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if (pickerTarget != null && picker.editing() && picker.keyTyped(pickerTarget, (char) 0, key)) {
			return true;
		}
		if (bindingMenuKey) {
			if (key != 256) {
				Slate.menuKey = key;
				Config.markDirty();
			}
			bindingMenuKey = false;
			return true;
		}
		if (bindingModule != null) {
			if (key != Slate.menuKey) {
				bindingModule.setKey(key == 256 ? 0 : key);
			}
			bindingModule = null;
			return true;
		}
		if (search.isFocused()) {
			if (key == 256) {
				search.setText("");
				search.setFocused2(false);
				return true;
			}
			if (key == 257) {
				search.setFocused2(false);
				return true;
			}
			return search.keyPressed(key, scanCode, modifiers);
		}
		if (key == 256 || key == Slate.menuKey) {
			onClose();
			return true;
		}
		if (key == 47) { // '/' focuses search
			search.setFocused2(true);
			return true;
		}
		if (key == 69) { // E - hud editor
			mc.displayGuiScreen(new HudEditorScreen());
			return true;
		}
		if (key == 265 || key == 264) {
			moveSelection(key == 264 ? 1 : -1);
			return true;
		}
		if (key == 263 || key == 262) {
			Category[] cats = Category.values();
			selectCategory(cats[(category.ordinal() + (key == 262 ? 1 : -1) + cats.length) % cats.length]);
			return true;
		}
		if (key == 257 || key == 32) {
			Row r = selectedRow();
			if (r != null && r.setting == null && !r.keybind && !r.description) {
				r.module.toggle();
			}
			return true;
		}
		if (key == 61 || key == 45) {
			Row r = selectedRow();
			if (r != null && r.module.hasVisibleSettings()) {
				if (expanded.contains(r.module)) {
					expanded.remove(r.module);
				} else {
					expanded.add(r.module);
				}
			}
			return true;
		}
		return super.keyPressed(key, scanCode, modifiers);
	}

	private Row selectedRow() {
		for (int i = 0; i < rows.size(); i++) {
			Row r = rows.get(i);
			if (r.module == selected && r.setting == null && !r.keybind && !r.description) {
				return r;
			}
		}
		return null;
	}

	/** Scrolls so a module and all of its settings are on screen, if they fit. */
	private void ensureBlockVisible(Module m) {
		int top = -1;
		int bottom = -1;
		for (int i = 0; i < rows.size(); i++) {
			Row r = rows.get(i);
			if (r.module != m) {
				continue;
			}
			if (top < 0) {
				top = r.y;
			}
			bottom = r.y + r.height;
		}
		if (top < 0) {
			return;
		}
		if (bottom - top <= viewHeight()) {
			scrollTarget = snap(bottom - viewHeight());
			if (top < scrollTarget) {
				scrollTarget = snap(top);
			}
		} else {
			scrollTarget = snap(top);
		}
	}

	/** Scrolls just enough to bring a row (and anything it expands) into view. */
	private void ensureVisible(Row r) {
		if (r.y < scrollTarget) {
			scrollTarget = snap(r.y);
			return;
		}
		if (r.y + r.height > scrollTarget + viewHeight()) {
			scrollTarget = snap(r.y + r.height - viewHeight(), true);
			// a tall row (a module with its colour picker open) may still not fit below;
			// aligning its top always does when it is shorter than the viewport
			if (r.y + r.height > scrollTarget + viewHeight()) {
				scrollTarget = snap(r.y);
			}
		}
	}

	private void moveSelection(int dir) {
		if (rows.isEmpty()) {
			return;
		}
		int i = -1;
		for (int j = 0; j < rows.size(); j++) {
			Row row = rows.get(j);
			if (row.module == selected && row.setting == null && !row.keybind && !row.description) {
				i = j;
				break;
			}
		}
		for (int guard = 0; guard < rows.size() + 1; guard++) {
			i += dir;
			if (i < 0) {
				i = rows.size() - 1;
			}
			if (i >= rows.size()) {
				i = 0;
			}
			Row candidate = rows.get(i);
			if (candidate.setting == null && !candidate.keybind && !candidate.description) {
				selected = candidate.module;
				break;
			}
		}
		Row r = selectedRow();
		if (r != null) {
			ensureVisible(r);
		}
	}

	public boolean charTyped(char c, int modifiers) {
		if (pickerTarget != null && picker.editing()) {
			return picker.keyTyped(pickerTarget, c, 0);
		}
		return search.isFocused() && search.charTyped(c, modifiers);
	}

	public void tick() {
		if (search != null) {
			search.tick();
		}
	}

	public void onClose() {
		Config.save();
		mc.displayGuiScreen(null);
	}

	static String keyName(int glfw) {
		if (glfw == 0) {
			return "none";
		}
		int eagler = net.lax1dude.eaglercraft.KeyboardConstants.getEaglerKeyFromGLFW(glfw);
		String s = net.lax1dude.eaglercraft.Keyboard.getKeyName(eagler);
		return s == null || s.length() == 0 ? "?" : s.replace(". ", "");
	}
}
