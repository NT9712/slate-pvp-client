package net.slate.module.impl;

import net.minecraft.client.Minecraft;
import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.ModeSetting;
import net.slate.ui.Notifications;

/**
 * Keeps sprinting without holding the key. "Toggle" flips on a key press, "Always" sprints
 * whenever you are moving forward.
 */
public class ToggleSprintModule extends Module {

	private static ToggleSprintModule INSTANCE;

	private final ModeSetting mode = add(new ModeSetting("Mode", 0, "Toggle", "Always"));

	private boolean toggled;

	public ToggleSprintModule() {
		super("Toggle Sprint", "Sprint without holding the key.", Category.PLAYER);
		INSTANCE = this;
		setKey(86); // V
	}

	protected void onKeyPressed() {
		if (!isEnabled()) {
			toggle();
			return;
		}
		if (mode.is("Toggle")) {
			toggled = !toggled;
			Notifications.push("Sprint", toggled);
		}
	}

	public void onDisable() {
		toggled = false;
	}

	/** True when the client should behave as if the sprint key is held. */
	public static boolean sprintHeld() {
		ToggleSprintModule m = INSTANCE;
		if (m == null || !m.isEnabled()) {
			return false;
		}
		if (m.mode.is("Always")) {
			Minecraft mc = Minecraft.getInstance();
			return mc.player != null && !mc.player.isSneaking();
		}
		return m.toggled;
	}

	public String status() {
		if (!isEnabled()) {
			return null;
		}
		return mode.is("Always") ? "always" : (toggled ? "on" : "off");
	}
}
