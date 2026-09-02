package net.slate.module.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.slate.hud.HudDraw;
import net.slate.module.HudModule;
import net.slate.module.HudModule.HAlign;
import net.slate.module.HudModule.VAlign;
import net.slate.module.setting.BoolSetting;
import net.slate.ui.Draw;
import net.slate.ui.Theme;

/** Round trip time to the server, as reported by the player list. */
public final class PingModule extends HudModule {

	private final BoolSetting colour = add(new BoolSetting("Colour", false));

	private int cachedPing = -1;
	private String cachedValue = "-";

	public PingModule() {
		super("Ping", "Your ping, straight from the player list.", HAlign.LEFT, VAlign.TOP, 4, 24);
	}

	/** -1 when the ping is not known yet. */
	private int ping() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return -1;
		}
		ClientPlayNetHandler connection = mc.getConnection();
		if (connection == null) {
			return -1;
		}
		NetworkPlayerInfo info = connection.getPlayerInfo(mc.player.getUniqueID());
		return info == null ? -1 : info.getResponseTime();
	}

	private static String value(int ping) {
		return ping < 0 ? "-" : (ping + "ms");
	}

	private int colour(int ping) {
		if (!colour.get() || ping < 0) {
			return HudDraw.VALUE;
		}
		return ping < 80 ? Theme.GOOD : (ping < 200 ? Theme.WARN : Theme.BAD);
	}

	@Override
	public void updateLayout() {
		cachedPing = ping();
		cachedValue = value(cachedPing);
		setSize(HudDraw.lineWidth("Ping", cachedValue), Draw.TEXT_H);
	}

	@Override
	public void render(float partialTicks) {
		HudDraw.line("Ping", cachedValue, 0f, 0f, colour(cachedPing));
	}

}
