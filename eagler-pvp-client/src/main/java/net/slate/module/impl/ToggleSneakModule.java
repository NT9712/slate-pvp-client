package net.slate.module.impl;

import net.minecraft.client.Minecraft;
import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.ui.Notifications;

/** Sneak stays on until you press the key again. */
public class ToggleSneakModule extends Module {

	private static ToggleSneakModule INSTANCE;

	private final BoolSetting releaseOnJump = add(new BoolSetting("Cancel On Jump", true));

	private boolean toggled;

	public ToggleSneakModule() {
		super("Toggle Sneak", "Sneak without holding the key.", Category.PLAYER);
		INSTANCE = this;
		setKey(66); // B
	}

	protected void onKeyPressed() {
		if (!isEnabled()) {
			toggle();
			return;
		}
		toggled = !toggled;
			Notifications.push("Sneak", toggled);
	}

	public void onDisable() {
		toggled = false;
	}

	public void onTick() {
		if (!toggled) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			toggled = false;
			return;
		}
		if (releaseOnJump.get() && mc.gameSettings.keyBindJump.isKeyDown()) {
			toggled = false;
		}
	}

	/** True when the client should behave as if the sneak key is held. */
	public static boolean sneakHeld() {
		ToggleSneakModule m = INSTANCE;
		return m != null && m.isEnabled() && m.toggled;
	}

	public String status() {
		return isEnabled() ? (toggled ? "on" : "off") : null;
	}
}
