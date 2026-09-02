package net.slate.module.impl;

import net.lax1dude.eaglercraft.EagRuntime;
import net.lax1dude.eaglercraft.HString;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.slate.hud.HudDraw;
import net.slate.module.Category;
import net.slate.module.HudModule;
import net.slate.module.HudModule.HAlign;
import net.slate.module.HudModule.VAlign;
import net.slate.ui.Draw;
import net.slate.ui.Theme;

/** Name, distance and health of the player you are aiming at. */
public final class TargetInfoModule extends HudModule {

	private static final long KEEP = 3000L;
	private static final int BAR_BG = 0x60000000;

	private LivingEntity target;
	private long lastSeen;
	private long lastFrame;
	private float fill;

	private String name = "";
	private String distance = "0.0";
	private String health = "0.0";
	private boolean showing;

	public TargetInfoModule() {
		super("Target", "Name, health and distance of whatever you are aiming at.", Category.COMBAT, HAlign.CENTRE, VAlign.TOP, 0, 14);
	}

	private static LivingEntity aimedAt(Minecraft mc) {
		if (mc.pointedEntity instanceof LivingEntity) {
			return (LivingEntity) mc.pointedEntity;
		}
		RayTraceResult hit = mc.objectMouseOver;
		if (hit != null && hit.getType() == RayTraceResult.Type.ENTITY) {
			Entity e = ((EntityRayTraceResult) hit).getEntity();
			if (e instanceof LivingEntity) {
				return (LivingEntity) e;
			}
		}
		return null;
	}

	@Override
	public void updateLayout() {
		Minecraft mc = Minecraft.getInstance();
		long now = EagRuntime.steadyTimeMillis();
		if (mc.player == null || mc.world == null) {
			target = null;
		} else {
			LivingEntity aimed = aimedAt(mc);
			if (aimed != null) {
				if (aimed != target) {
					fill = 0f;
				}
				target = aimed;
				lastSeen = now;
			} else if (target != null && (now - lastSeen > KEEP || !target.isAlive())) {
				target = null;
			}
		}

		showing = target != null && mc.player != null;
		if (!showing) {
			setSize(0, 0);
			lastFrame = now;
			return;
		}

		float hp = target.getHealth();
		float max = target.getMaxHealth();
		name = target.getName().getFormattedText();
		distance = HString.format("%.1f", Float.valueOf(mc.player.getDistance(target)));
		health = HString.format("%.1f", Float.valueOf(hp));

		float delta = lastFrame == 0L ? 1f : (now - lastFrame) / 50f;
		lastFrame = now;
		fill = Draw.approach(fill, max <= 0f ? 0f : Math.min(1f, hp / max), 0.35f, delta);

		setSize(Math.max(80, Draw.width(name) + 8 + Draw.width(distance)), 26);
	}

	@Override
	public void render(float partialTicks) {
		if (!showing) {
			return;
		}
		int w = width();
		Draw.textOutlined(name, 0f, 0f, HudDraw.VALUE);
		Draw.textOutlinedRight(distance, w, 0f, HudDraw.LABEL);

		Draw.rect(0f, 12f, w, 2f, BAR_BG);
		int colour = fill > 0.6f ? Theme.GOOD : (fill > 0.3f ? Theme.WARN : Theme.BAD);
		Draw.rect(0f, 12f, w * fill, 2f, colour);

		HudDraw.line("HP", health, 0f, 17f);
	}

}
