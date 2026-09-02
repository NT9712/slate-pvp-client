package net.slate;

import net.lax1dude.eaglercraft.EagRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.slate.module.Module;
import net.slate.module.ModuleManager;
import net.slate.ui.ClickGuiScreen;

/**
 * Slate - a small, focused PvP client for Eaglercraft 1.14.
 *
 * Single static entry point. Everything the vanilla code touches goes through here so the
 * number of edits to Minecraft classes stays minimal and obvious.
 */
public final class Slate {

	public static final String NAME = "Slate";
	public static final String VERSION = "1.0";

	private static ModuleManager modules;
	private static boolean initialised;

	/** GLFW key that opens the client menu. */
	public static int menuKey = 344; // RIGHT SHIFT

	private static long lastFrameTime;
	private static float frameDelta = 1f;

	private Slate() {
	}

	public static void init() {
		if (initialised) {
			return;
		}
		initialised = true;
		modules = new ModuleManager();
		modules.registerAll();
		modules.enableDefaults();
		Config.load();
	}

	public static boolean isReady() {
		return initialised;
	}

	public static ModuleManager modules() {
		return modules;
	}

	public static <T extends Module> T get(Class<T> type) {
		return modules == null ? null : modules.get(type);
	}

	// ------------------------------------------------------------------ loop

	/** Called once per client tick (20/s) from Minecraft.runTick(). */
	public static void onTick() {
		if (!initialised) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (!mc.isGameFocused()) {
			clearHeldKeys();
		}
		Object world = mc.world;
		if (world != lastWorld) {
			lastWorld = world;
			ClientEvents.reset();
			clearHeldKeys();
			net.minecraft.client.gui.screen.ChatScreen.clearDraft();
		}
		modules.onTick();
		Config.tick();
	}

	private static Object lastWorld;

	/** Key state, tracked from the keyboard event stream so it works on every platform. */
	private static final boolean[] held = new boolean[512];

	/** Set when a key goes from up to down, consumed by {@link #fireKeyEdge(int)}. */
	private static int pendingEdge;

	/**
	 * Called for every key event, before anything else can swallow it, so held state stays
	 * correct even when a screen eats the event. Only records the edge; the action is fired
	 * later from {@link #fireKeyEdge(int)} so opening a screen cannot re-consume this event.
	 */
	public static void trackKey(int glfwKey, boolean pressed) {
		if (glfwKey <= 0 || glfwKey >= held.length) {
			return;
		}
		if (pressed && !held[glfwKey]) {
			pendingEdge = glfwKey;
		}
		held[glfwKey] = pressed;
	}

	/** Called once the vanilla handlers have declined the event and no screen is open. */
	public static void fireKeyEdge(int glfwKey) {
		if (pendingEdge != glfwKey) {
			return;
		}
		pendingEdge = 0;
		onKeyPressed(glfwKey);
	}

	/** Key repeat and lost focus must not leave keys stuck down. */
	public static void clearHeldKeys() {
		for (int i = 0; i < held.length; i++) {
			held[i] = false;
		}
	}

	public static boolean isKeyHeld(int glfwKey) {
		return glfwKey > 0 && glfwKey < held.length && held[glfwKey];
	}

	private static void onKeyPressed(int glfwKey) {
		if (!initialised || glfwKey == 0) {
			return;
		}
		if (glfwKey == menuKey) {
			Minecraft.getInstance().displayGuiScreen(new ClickGuiScreen());
			return;
		}
		modules.onKeyPressed(glfwKey);
	}

	/** Called from IngameGui.renderGameOverlay() after the vanilla HUD. */
	public static void renderHud(float partialTicks) {
		if (!initialised) {
			return;
		}
		updateDelta();
		modules.renderHud(partialTicks);
	}

	/** Toasts, drawn last so nothing covers them. */
	public static void renderOverlay() {
		if (initialised) {
			modules.renderOverlay();
		}
	}

	/** Frame delta in "ticks of 60fps", clamped. Used for frame-rate independent animation. */
	public static float delta() {
		return frameDelta;
	}

	public static void updateDelta() {
		long now = EagRuntime.steadyTimeMillis();
		if (lastFrameTime == 0L) {
			lastFrameTime = now;
			return;
		}
		long dt = now - lastFrameTime;
		// several call sites hit this in the same frame; only the first one counts
		if (dt >= 2L) {
			frameDelta = Math.min(100L, dt) / 16.666f;
			lastFrameTime = now;
		}
	}

	// ---------------------------------------------------------------- events

	public static void onMouseButton(int button, boolean pressed) {
		if (initialised) {
			ClientEvents.onMouseButton(button, pressed);
		}
	}

	public static void onAttack(Entity target) {
		if (initialised) {
			ClientEvents.onAttack(target);
		}
	}

	public static void onHealthUpdate(float newHealth) {
		if (initialised) {
			ClientEvents.onHealthUpdate(newHealth);
		}
	}

	/** Returns true if the left mouse button is currently held down. */
	public static boolean isMouseDown() {
		Minecraft mc = Minecraft.getInstance();
		return mc != null && mc.mouseHelper != null && mc.mouseHelper.isLeftDown();
	}
}