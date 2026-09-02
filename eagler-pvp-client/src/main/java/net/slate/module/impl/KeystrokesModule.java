package net.slate.module.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.slate.ClientEvents;
import net.slate.Slate;
import net.slate.module.HudModule;
import net.slate.module.HudModule.HAlign;
import net.slate.module.HudModule.VAlign;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.ModeSetting;
import net.slate.module.setting.NumberSetting;
import net.slate.ui.Draw;
import net.slate.ui.Theme;

/** Classic WASD keystroke display with optional mouse buttons and space bar. */
public final class KeystrokesModule extends HudModule {

	private static final int KEY = 16;
	private static final int GAP = 2;
	private static final int RELEASED_BG = 0x73000000;

	private final ModeSetting layout = add(new ModeSetting("Layout", 1, "WASD", "WASD + Mouse", "Full"));
	private final BoolSetting cps = add(new BoolSetting("CPS", false));
	private final NumberSetting roundness = add(new NumberSetting("Roundness", 2.0D, 0.0D, 4.0D, 1.0D));

	/** W A S D LMB RMB SPACE */
	private final float[] anim = new float[7];
	private final KeyBinding[] move = new KeyBinding[4];

	public KeystrokesModule() {
		super("Keystrokes", "Shows the movement and click keys you press.", HAlign.LEFT, VAlign.BOTTOM, 4, 56);
	}


	@Override
	public void updateLayout() {
		setSize(52, layout.is("WASD") ? 34 : (layout.is("Full") ? 64 : 52));
	}

	@Override
	public void render(float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gameSettings == null) {
			return;
		}
		KeyBinding[] move = this.move;
		move[0] = mc.gameSettings.keyBindForward;
		move[1] = mc.gameSettings.keyBindLeft;
		move[2] = mc.gameSettings.keyBindBack;
		move[3] = mc.gameSettings.keyBindRight;
		float d = Slate.delta();
		for (int i = 0; i < 4; i++) {
			anim[i] = Draw.approach(anim[i], move[i].isKeyDown() ? 1f : 0f, 0.35f, d);
		}
		anim[4] = Draw.approach(anim[4], mc.gameSettings.keyBindAttack.isKeyDown() ? 1f : 0f, 0.35f, d);
		anim[5] = Draw.approach(anim[5], mc.gameSettings.keyBindUseItem.isKeyDown() ? 1f : 0f, 0.35f, d);
		anim[6] = Draw.approach(anim[6], mc.gameSettings.keyBindJump.isKeyDown() ? 1f : 0f, 0.35f, d);

		key(18f, 0f, KEY, KEY, letter(move[0]), anim[0]);
		key(0f, 18f, KEY, KEY, letter(move[1]), anim[1]);
		key(18f, 18f, KEY, KEY, letter(move[2]), anim[2]);
		key(36f, 18f, KEY, KEY, letter(move[3]), anim[3]);

		if (layout.is("WASD")) {
			return;
		}
		key(0f, 36f, 25, KEY, mouseLabel("LMB", ClientEvents.leftCps()), anim[4]);
		key(27f, 36f, 25, KEY, mouseLabel("RMB", ClientEvents.rightCps()), anim[5]);

		if (layout.is("Full")) {
			String space = mc.gameSettings.keyBindJump.getLocalizedName();
			if (Draw.width(space) > 46) {
				space = letter(mc.gameSettings.keyBindJump);
			}
			key(0f, 54f, 52, 10, space, anim[6]);
		}
	}

	private String mouseLabel(String name, int clicks) {
		return cps.get() && clicks > 0 ? Integer.toString(clicks) : name;
	}

	private void key(float x, float y, int w, int h, String label, float a) {
		int bg = Theme.mix(RELEASED_BG, Theme.alpha(Theme.ACCENT, 0.9f), a);
		int fg = Theme.mix(Theme.alpha(Theme.TEXT, 0.75f), 0xFFFFFFFF, a);
		Draw.roundRect(x, y, w, h, roundness.getFloat(), bg);
		Draw.textCentred(label, x + w * 0.5f, y + (h - Draw.TEXT_H) * 0.5f + 1f, fg);
	}

	private static String letter(KeyBinding bind) {
		String name = bind.getLocalizedName();
		if (name == null || name.length() == 0) {
			return "?";
		}
		return name.length() > 3 ? name.substring(0, 1).toUpperCase() : name;
	}
}
