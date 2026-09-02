package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.NumberSetting;

/**
 * Caps how many particles can be alive at once. Explosion and crit spam is the single
 * cheapest thing to give up in a fight.
 */
public class ParticlesModule extends Module {

	private static ParticlesModule INSTANCE;

	private final NumberSetting limit = add(new NumberSetting("Max Particles", 300.0D, 0.0D, 2000.0D, 50.0D));
	private final BoolSetting dropExplosions = add(new BoolSetting("Cut Explosions", false));

	public ParticlesModule() {
		super("Particle Limit", "Caps the number of particles for a steadier frame rate.", Category.PERFORMANCE);
		INSTANCE = this;
	}

	public static int maxParticles() {
		ParticlesModule m = INSTANCE;
		return m == null || !m.isEnabled() ? -1 : m.limit.getInt();
	}

	public static boolean cutExplosions() {
		ParticlesModule m = INSTANCE;
		return m != null && m.isEnabled() && m.dropExplosions.get();
	}

}
