package net.slate.module;

import java.util.ArrayList;
import java.util.List;

import net.slate.hud.HudManager;
import net.slate.module.impl.*;
import net.slate.module.impl.NoDynamicFOVModule;
import net.slate.module.impl.TimingOptimiserModule;
import net.slate.module.impl.SmartAnimationsModule;
import net.slate.module.impl.SodiumOptimizationsModule;
import net.slate.module.impl.EntityCullingModule;
import net.slate.module.impl.MemoryCleanerModule;
import net.slate.module.impl.AnimatedTextureOptimizerModule;
import net.slate.module.impl.FogOptimizerModule;
import net.slate.module.impl.ShaderPackModule;
import net.slate.module.impl.ViaTexturesModule;
import net.slate.ui.Notifications;

public final class ModuleManager {

	private final List<Module> modules = new ArrayList<Module>();

	public void registerAll() {
		// Combat
		add(new CrosshairModule());
		add(new HitMarkerModule());
		add(new TargetInfoModule());
		add(new ComboModule());
		add(new ReachModule());

		// HUD
		add(new FpsModule());
		add(new CpsModule());
		add(new PingModule());
		add(new CoordsModule());
		add(new KeystrokesModule());
		add(new ArmourModule());
		add(new EffectsModule());

		// Visual
		add(new ZoomModule());
		add(new HurtCamModule());
		add(new LowFireModule());
		add(new FullBrightModule());
		add(new CleanScoreboardModule());
		add(new ShaderPackModule());
		add(new ViaTexturesModule());

		// Player
		add(new ToggleSprintModule());
		add(new ToggleSneakModule());
		add(new FreelookModule());
		add(new NoDynamicFOVModule());

		// Performance
		add(new PowerSaverModule());
		add(new ParticlesModule());
		add(new TimingOptimiserModule());
		add(new SmartAnimationsModule());
		add(new SodiumOptimizationsModule());
		add(new EntityCullingModule());
		add(new MemoryCleanerModule());
		add(new AnimatedTextureOptimizerModule());
		add(new FogOptimizerModule());

		// Misc
		add(new ChatModule());
		add(new TabListModule());
		add(new NotificationsModule());
	}

	/** A sensible out-of-the-box loadout, applied before the saved config is read. */
	public void enableDefaults() {
		String[] on = { "FPS", "CPS", "Ping", "Coordinates", "Keystrokes", "Durability", "Effects", "Combo",
				"Hit Marker", "Zoom", "Low Fire", "Toggle Sprint", "Chat", "Tab List", "Notifications",
				"Power Saver" };
		for (int i = 0; i < on.length; i++) {
			Module m = byName(on[i]);
			if (m != null) {
				m.setEnabled(true);
			}
		}
	}

	private void add(Module m) {
		modules.add(m);
	}

	public List<Module> all() {
		return modules;
	}

	public List<Module> byCategory(Category c) {
		List<Module> out = new ArrayList<Module>();
		for (int i = 0; i < modules.size(); i++) {
			if (modules.get(i).category == c) {
				out.add(modules.get(i));
			}
		}
		return out;
	}

	public Module byName(String name) {
		for (int i = 0; i < modules.size(); i++) {
			if (modules.get(i).name.equals(name)) {
				return modules.get(i);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T extends Module> T get(Class<T> type) {
		for (int i = 0; i < modules.size(); i++) {
			if (type.isInstance(modules.get(i))) {
				return (T) modules.get(i);
			}
		}
		return null;
	}

	public void onTick() {
		for (int i = 0; i < modules.size(); i++) {
			Module m = modules.get(i);
			if (m.isEnabled()) {
				m.onTick();
			}
		}
	}

	public void onKeyPressed(int glfwKey) {
		for (int i = 0; i < modules.size(); i++) {
			Module m = modules.get(i);
			if (m.getKey() == glfwKey) {
				m.keyPressed();
			}
		}
	}

	public void renderHud(float partialTicks) {
		HudManager.render(partialTicks);
	}

	public void renderOverlay() {
		Notifications.render();
	}
}
