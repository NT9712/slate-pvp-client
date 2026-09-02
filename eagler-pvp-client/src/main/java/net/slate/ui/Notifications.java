package net.slate.ui;

import java.util.ArrayList;
import java.util.List;

import net.lax1dude.eaglercraft.EagRuntime;
import net.minecraft.client.Minecraft;
import net.slate.module.Module;

/**
 * Small toast shown when a module is toggled with a keybind.
 * Deliberately tiny: one line, top-centre, fades out.
 */
public final class Notifications {

	private static final long LIFETIME = 1600L;
	private static final int MAX = 4;
	private static final List<Toast> toasts = new ArrayList<Toast>();

	/** Turned off by the Notifications module when the user does not want them. */
	public static boolean enabled = true;

	private Notifications() {
	}

	public static void moduleToggled(Module m) {
		if (!enabled) {
			return;
		}
		push(m.name, m.isEnabled());
	}

	public static void push(String text, boolean on) {
		if (!enabled) {
			return;
		}
		for (int i = 0; i < toasts.size(); i++) {
			if (toasts.get(i).text.equals(text)) {
				toasts.remove(i);
				break;
			}
		}
		toasts.add(new Toast(text, on));
		while (toasts.size() > MAX) {
			toasts.remove(0);
		}
	}

	public static void render() {
		if (toasts.isEmpty()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		long now = EagRuntime.steadyTimeMillis();
		int cx = mc.mainWindow.getScaledWidth() / 2;
		float y = 6f;
		for (int i = toasts.size() - 1; i >= 0; i--) {
			Toast t = toasts.get(i);
			long age = now - t.born;
			if (age > LIFETIME) {
				toasts.remove(i);
				continue;
			}
			float in = Math.min(1f, age / 140f);
			float out = age > LIFETIME - 260L ? (LIFETIME - age) / 260f : 1f;
			float a = Draw.easeOut(Math.min(in, out));
			if (a <= 0.01f) {
				continue;
			}
			int w = Draw.width(t.text) + 22;
			float bx = cx - w * 0.5f;
			Draw.roundRect(bx, y, w, 15f, 3f, Theme.alpha(Theme.PANEL, a * 0.95f));
			Draw.rect(bx + 6f, y + 5f, 4f, 4f, Theme.alpha(t.on ? Theme.ACCENT : Theme.OFF, a));
			Draw.text(t.text, bx + 15f, y + 4f, Theme.alpha(Theme.TEXT, a));
			y += 18f;
		}
	}

	private static final class Toast {
		final String text;
		final boolean on;
		final long born;

		Toast(String text, boolean on) {
			this.text = text;
			this.on = on;
			this.born = EagRuntime.steadyTimeMillis();
		}
	}
}
