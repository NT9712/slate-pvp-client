package net.slate.module;

import java.util.ArrayList;
import java.util.List;

import net.slate.module.setting.Setting;
import net.slate.ui.Notifications;

public abstract class Module extends Animated {

	public final String name;
	public final String description;
	public final Category category;

	private boolean enabled;
	/** GLFW key code, 0 = unbound. */
	private int key;

	protected final List<Setting> settings = new ArrayList<Setting>();

	protected Module(String name, String description, Category category) {
		this.name = name;
		this.description = description;
		this.category = category;
	}

	protected <T extends Setting> T add(T setting) {
		settings.add(setting);
		return setting;
	}

	public List<Setting> settings() {
		return settings;
	}

	public boolean hasVisibleSettings() {
		for (int i = 0; i < settings.size(); i++) {
			if (settings.get(i).isVisible()) {
				return true;
			}
		}
		return false;
	}

	// ---------------------------------------------------------------- state

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean value) {
		if (this.enabled == value) {
			return;
		}
		this.enabled = value;
		if (value) {
			onEnable();
		} else {
			onDisable();
		}
	}

	public void toggle() {
		setEnabled(!enabled);
		Notifications.moduleToggled(this);
		net.slate.Config.markDirty();
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
		net.slate.Config.markDirty();
	}

	/** Called by the manager when this module's bind is pressed. */
	final void keyPressed() {
		if (!isHoldKey()) {
			onKeyPressed();
		}
	}

	/** True when the bind is meant to be held down rather than tapped (zoom, freelook). */
	public boolean isHoldKey() {
		return false;
	}

	/** Is the module's bind currently held? Safe to call every frame. */
	public final boolean isKeyHeld() {
		return net.slate.Slate.isKeyHeld(key);
	}

	/** Default keybind behaviour is to toggle the module. */
	protected void onKeyPressed() {
		toggle();
	}

	// ----------------------------------------------------------- extension

	public void onEnable() {
	}

	public void onDisable() {
	}

	/** Called every client tick while enabled. */
	public void onTick() {
	}

	/**
	 * Extra text shown next to the module name in the menu, e.g. the current mode.
	 * Return null for none.
	 */
	public String status() {
		return null;
	}
}
