package net.slate.module.impl;

import net.lax1dude.eaglercraft.HString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.slate.hud.HudDraw;
import net.slate.module.HudModule;
import net.slate.module.HudModule.HAlign;
import net.slate.module.HudModule.VAlign;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.ModeSetting;
import net.slate.ui.Draw;

/** Your position in the world, inline or stacked. */
public final class CoordsModule extends HudModule {


	private final ModeSetting style = add(new ModeSetting("Style", 0, "Inline", "Stacked"));
	private final BoolSetting decimals = add(new BoolSetting("Decimals", false));
	private final BoolSetting facing = add(new BoolSetting("Facing", false));
	private final BoolSetting nether = add(new BoolSetting("Nether", false));

	private String x = "0";
	private String y = "0";
	private String z = "0";
	private String netherLine = null;

	public CoordsModule() {
		super("Coordinates", "Position, optional facing and nether conversion.", HAlign.LEFT, VAlign.TOP, 4, 34);
	}

	private String inlineValue = "0 0 0";
	private String facingValue;

	/** Cardinal direction plus the axis it moves you along, which is what people actually use. */
	private static String facingOf(ClientPlayerEntity p) {
		switch (p.getHorizontalFacing()) {
		case NORTH:
			return "North -Z";
		case SOUTH:
			return "South +Z";
		case WEST:
			return "West -X";
		default:
			return "East +X";
		}
	}

	private String inline() {
		return inlineValue;
	}

	private String fmt(double v) {
		return decimals.get() ? HString.format("%.1f", v) : String.valueOf((int) Math.floor(v));
	}

	private void refresh() {
		ClientPlayerEntity p = Minecraft.getInstance().player;
		if (p == null) {
			x = y = z = "-";
			inlineValue = "-";
			facingValue = null;
			netherLine = null;
			return;
		}
		x = fmt(p.posX);
		y = fmt(p.posY);
		z = fmt(p.posZ);
		inlineValue = x + " " + y + " " + z;
		facingValue = facing.get() ? facingOf(p) : null;
		netherLine = nether.get() ? (fmt(p.posX / 8.0D) + ", " + fmt(p.posZ / 8.0D)) : null;
	}

	@Override
	public void updateLayout() {
		refresh();
		int extra = (netherLine == null ? 0 : HudDraw.LINE) + (facingValue == null ? 0 : HudDraw.LINE);
		if (style.is("Stacked")) {
			int w = HudDraw.lineWidth("X", x);
			w = Math.max(w, HudDraw.lineWidth("Y", y));
			w = Math.max(w, HudDraw.lineWidth("Z", z));
			if (facingValue != null) {
				w = Math.max(w, HudDraw.lineWidth("Facing", facingValue));
			}
			if (netherLine != null) {
				w = Math.max(w, HudDraw.lineWidth("Nether", netherLine));
			}
			setSize(w, HudDraw.LINE * 2 + Draw.TEXT_H + extra);
			return;
		}
		int w = HudDraw.lineWidth("XYZ", inline());
		if (facingValue != null) {
			w = Math.max(w, HudDraw.lineWidth("Facing", facingValue));
		}
		if (netherLine != null) {
			w = Math.max(w, HudDraw.lineWidth("Nether", netherLine));
		}
		setSize(w, extra == 0 ? Draw.TEXT_H : HudDraw.LINE + Draw.TEXT_H);
	}

	@Override
	public void render(float partialTicks) {
		float cy = 0f;
		if (style.is("Stacked")) {
			HudDraw.line("X", x, 0f, cy);
			cy += HudDraw.LINE;
			HudDraw.line("Y", y, 0f, cy);
			cy += HudDraw.LINE;
			HudDraw.line("Z", z, 0f, cy);
			cy += HudDraw.LINE;
		} else {
			HudDraw.line("XYZ", inline(), 0f, cy);
			cy += HudDraw.LINE;
		}
		if (facingValue != null) {
			HudDraw.line("Facing", facingValue, 0f, cy);
			cy += HudDraw.LINE;
		}
		if (netherLine != null) {
			HudDraw.line("Nether", netherLine, 0f, cy);
		}
	}

}
