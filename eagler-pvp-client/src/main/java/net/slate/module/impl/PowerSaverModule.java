package net.slate.module.impl;

import net.minecraft.client.Minecraft;
import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.NumberSetting;

/**
 * Caps the frame rate when the game is not being played, which matters a lot in a browser
 * tab. Does not touch the in-game frame rate.
 */
public class PowerSaverModule extends Module {

	private static PowerSaverModule INSTANCE;

	private final NumberSetting menuFps = add(new NumberSetting("Menu FPS", 60.0D, 15.0D, 120.0D, 5.0D));
	private final NumberSetting unfocusedFps = add(new NumberSetting("Unfocused FPS", 20.0D, 5.0D, 60.0D, 5.0D));
	private final BoolSetting whileInMenus = add(new BoolSetting("Limit In Menus", true));

	public PowerSaverModule() {
		super("Power Saver", "Limits the frame rate when the game is idle.", Category.PERFORMANCE);
		INSTANCE = this;
	}

	/**
	 * Returns the frame rate the client should be limited to, or -1 to leave it alone.
	 */
	public static int frameLimit() {
		PowerSaverModule m = INSTANCE;
		if (m == null || !m.isEnabled()) {
			return -1;
		}
		Minecraft mc = Minecraft.getInstance();
		if (!mc.isGameFocused()) {
			return m.unfocusedFps.getInt();
		}
		if (m.whileInMenus.get() && mc.currentScreen != null && mc.world != null) {
			return m.menuFps.getInt();
		}
		return -1;
	}

}
