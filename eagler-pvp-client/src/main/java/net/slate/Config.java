package net.slate;

import java.io.UnsupportedEncodingException;
import java.util.List;

import net.lax1dude.eaglercraft.EagRuntime;
import net.slate.hud.HudManager;
import net.slate.module.HudModule;
import net.slate.module.Module;
import net.slate.module.setting.Setting;

/**
 * Flat key=value persistence, stored through EagRuntime so it works in the browser
 * (localStorage) and on the desktop runtime (a file) without any extra code.
 */
public final class Config {

	private static final String KEY = "slate";
	private static boolean loading;
	private static boolean dirty;
	private static long lastSave;

	private Config() {
	}

	/** Marks the config as needing a write; the actual write is debounced. */
	public static void markDirty() {
		dirty = true;
	}

	/** Called every client tick. */
	public static void tick() {
		if (dirty && EagRuntime.steadyTimeMillis() - lastSave > 1000L) {
			save();
		}
	}

	public static void save() {
		if (loading || !Slate.isReady()) {
			return;
		}
		dirty = false;
		lastSave = EagRuntime.steadyTimeMillis();
		StringBuilder sb = new StringBuilder(2048);
		sb.append("version=1\n");
		sb.append("menuKey=").append(Slate.menuKey).append('\n');
		sb.append("hudBackgrounds=").append(HudManager.backgrounds).append('\n');
		List<Module> mods = Slate.modules().all();
		for (int i = 0; i < mods.size(); i++) {
			Module m = mods.get(i);
			String p = m.name.replace(' ', '_');
			sb.append(p).append(".on=").append(m.isEnabled()).append('\n');
			if (m.getKey() != 0) {
				sb.append(p).append(".key=").append(m.getKey()).append('\n');
			}
			if (m instanceof HudModule) {
				sb.append(p).append(".pos=").append(((HudModule) m).writePosition()).append('\n');
			}
			List<Setting> settings = m.settings();
			for (int j = 0; j < settings.size(); j++) {
				Setting s = settings.get(j);
				sb.append(p).append('.').append(s.name.replace(' ', '_')).append('=').append(s.write()).append('\n');
			}
		}
		try {
			EagRuntime.setStorage(KEY, sb.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			EagRuntime.setStorage(KEY, sb.toString().getBytes());
		}
	}

	public static void load() {
		byte[] data = EagRuntime.getStorage(KEY);
		if (data == null || data.length == 0) {
			return;
		}
		loading = true;
		try {
			String text;
			try {
				text = new String(data, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				text = new String(data);
			}
			int start = 0;
			while (start < text.length()) {
				int nl = text.indexOf('\n', start);
				if (nl < 0) {
					nl = text.length();
				}
				try {
					apply(text.substring(start, nl));
				} catch (RuntimeException lineError) {
					EagRuntime.debugPrintStackTrace(lineError);
				}
				start = nl + 1;
			}
		} catch (RuntimeException e) {
			EagRuntime.debugPrintStackTrace(e);
		} finally {
			loading = false;
		}
	}

	private static void apply(String line) {
		int eq = line.indexOf('=');
		if (eq <= 0) {
			return;
		}
		String key = line.substring(0, eq);
		String value = line.substring(eq + 1);
		if ("version".equals(key)) {
			return;
		}
		if ("menuKey".equals(key)) {
			try {
				Slate.menuKey = Integer.parseInt(value);
			} catch (NumberFormatException ignored) {
			}
			return;
		}
		if ("hudBackgrounds".equals(key)) {
			HudManager.backgrounds = "true".equals(value);
			return;
		}
		int dot = key.indexOf('.');
		if (dot <= 0) {
			return;
		}
		Module m = Slate.modules().byName(key.substring(0, dot).replace('_', ' '));
		if (m == null) {
			return;
		}
		String field = key.substring(dot + 1);
		if ("on".equals(field)) {
			m.setEnabled("true".equals(value));
			return;
		}
		if ("key".equals(field)) {
			try {
				m.setKey(Integer.parseInt(value));
			} catch (NumberFormatException ignored) {
			}
			return;
		}
		if (m instanceof HudModule && "pos".equals(field)) {
			((HudModule) m).readPosition(value);
			return;
		}
		String plain = field.replace('_', ' ');
		List<Setting> settings = m.settings();
		for (int i = 0; i < settings.size(); i++) {
			if (settings.get(i).name.equals(plain)) {
				settings.get(i).read(value);
				return;
			}
		}
	}
}
