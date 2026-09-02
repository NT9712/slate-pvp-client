package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.ui.Notifications;

/** Small toast in the top centre when something is toggled. */
public class NotificationsModule extends Module {

	public NotificationsModule() {
		super("Notifications", "Shows a small toast when you toggle something.", Category.MISC);
	}

	public void onEnable() {
		Notifications.enabled = true;
	}

	public void onDisable() {
		Notifications.enabled = false;
	}
}
