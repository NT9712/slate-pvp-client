package net.slate.module.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.slate.hud.HudDraw;
import net.slate.module.HudModule;
import net.slate.module.HudModule.HAlign;
import net.slate.module.HudModule.VAlign;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.ModeSetting;
import net.slate.ui.Draw;
import net.slate.ui.Theme;

/** Active potion effects with their remaining duration. */
public final class EffectsModule extends HudModule {

	private static EffectsModule INSTANCE;

	private final BoolSetting duration = add(new BoolSetting("Duration", true));
	private final ModeSetting sort = add(new ModeSetting("Sort", 0, "Duration", "Name"));
	private final BoolSetting hideAmbient = add(new BoolSetting("Hide Ambient", false));

	private static final int DOT = 7;
	private static final int GAP = 12;

	private static final Comparator<EffectInstance> BY_NAME = new Comparator<EffectInstance>() {
		public int compare(EffectInstance a, EffectInstance b) {
			return name(a).compareTo(name(b));
		}
	};

	private static final Comparator<EffectInstance> BY_DURATION = new Comparator<EffectInstance>() {
		public int compare(EffectInstance a, EffectInstance b) {
			return Integer.compare(a.getDuration(), b.getDuration());
		}
	};

	private final List<EffectInstance> shown = new ArrayList<EffectInstance>();
	private final List<String> names = new ArrayList<String>();
	private final List<String> times = new ArrayList<String>();

	public EffectsModule() {
		super("Effects", "Active potion effects and their duration.", HAlign.RIGHT, VAlign.TOP, 4, 4);
		INSTANCE = this;
	}

	private long lastCollect;

	@Override
	public void onTick() {
		lastCollect = 0L;
	}

	@Override
	public void updateLayout() {
		long now = net.lax1dude.eaglercraft.EagRuntime.steadyTimeMillis();
		if (lastCollect == 0L) {
			lastCollect = now;
			collect();
		}
		if (shown.isEmpty()) {
			setSize(0, 0);
			return;
		}
		names.clear();
		times.clear();
		int nameW = 0;
		int timeW = 0;
		for (int i = 0; i < shown.size(); i++) {
			String n = name(shown.get(i));
			String t = duration.get() ? time(shown.get(i)) : "";
			names.add(n);
			times.add(t);
			nameW = Math.max(nameW, Draw.width(n));
			timeW = Math.max(timeW, Draw.width(t));
		}
		setSize(DOT + nameW + (duration.get() ? GAP + timeW : 0), shown.size() * 10);
	}

	@Override
	public void render(float partialTicks) {
		for (int i = 0; i < shown.size(); i++) {
			EffectInstance effect = shown.get(i);
			float y = i * 10f;
			// one small colour chip carries the potion colour; the text stays neutral and readable
			Draw.rect(0f, y + 3f, 3f, 3f, lighten(0xFF000000 | effect.getPotion().getLiquidColor()));
			Draw.textOutlined(names.get(i), DOT, y, HudDraw.LABEL);
			if (duration.get()) {
				int secs = effect.getDuration() / 20;
				Draw.textOutlinedRight(times.get(i), width(), y,
						secs <= 10 ? Theme.BAD : (secs <= 30 ? Theme.WARN : HudDraw.VALUE));
			}
		}
	}

	/** True when the client's own effect list is showing, so the vanilla icons can be skipped. */
	public static boolean replacesVanilla() {
		EffectsModule m = INSTANCE;
		return m != null && m.isEnabled();
	}

	private void collect() {
		shown.clear();
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}
		for (EffectInstance effect : mc.player.getActivePotionEffects()) {
			if (hideAmbient.get() && effect.isAmbient()) {
				continue;
			}
			shown.add(effect);
		}
		Collections.sort(shown, sort.is("Name") ? BY_NAME : BY_DURATION);
	}

	private static String name(EffectInstance effect) {
		return I18n.format(effect.getEffectName()) + level(effect.getAmplifier());
	}

	private static String time(EffectInstance effect) {
		return EffectUtils.getPotionDurationString(effect, 1.0F);
	}

	private static String level(int amplifier) {
		switch (amplifier) {
		case 0:
			return " I";
		case 1:
			return " II";
		case 2:
			return " III";
		case 3:
			return " IV";
		default:
			return " " + (amplifier + 1);
		}
	}

	/** Potion colours can be near black; pull them towards white until they read on the HUD. */
	private static int lighten(int colour) {
		int r = (colour >> 16) & 255, g = (colour >> 8) & 255, b = colour & 255;
		if (r * 30 + g * 59 + b * 11 < 9000) {
			return Theme.mix(colour, 0xFFFFFFFF, 0.35f);
		}
		return colour;
	}
}
