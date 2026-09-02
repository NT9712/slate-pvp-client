package net.slate.module.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.slate.module.Category;
import net.slate.module.Module;

/**
 * Disables the dynamic FOV change when sprinting.
 * In vanilla, sprinting increases FOV by ~30% (fovModifier goes from 1.0 to 1.3).
 * This module locks fovModifierHand to 1.0 for a consistent field of view.
 * TeaVM-safe: uses GameRenderer.setDisableDynamicFOV() instead of reflection.
 */
public class NoDynamicFOVModule extends Module {

    private static NoDynamicFOVModule INSTANCE;

    public NoDynamicFOVModule() {
        super("No Dynamic FOV", "Disables the FOV change when sprinting for a consistent view.", Category.PLAYER);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        // Disable dynamic FOV via TeaVM-safe method
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.gameRenderer != null) {
            mc.gameRenderer.setDisableDynamicFOV(true);
        }
    }

    @Override
    public void onDisable() {
        // Re-enable vanilla dynamic FOV
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.gameRenderer != null) {
            mc.gameRenderer.setDisableDynamicFOV(false);
        }
    }

    @Override
    public void onTick() {
        // Ensure dynamic FOV stays disabled
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.gameRenderer != null) {
            mc.gameRenderer.setDisableDynamicFOV(true);
        }
    }

    public static boolean isModuleEnabled() {
        NoDynamicFOVModule m = INSTANCE;
        return m != null && m.isEnabled();
    }
}