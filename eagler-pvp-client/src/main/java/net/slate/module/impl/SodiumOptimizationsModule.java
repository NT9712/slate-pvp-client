package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.NumberSetting;

/**
 * Sodium-style Optimizations - various rendering optimizations inspired by Sodium/CaffeineMC.
 * - Better face culling
 * - Reduced state changes
 * - Optimized vertex format
 */
public class SodiumOptimizationsModule extends Module {

    private static SodiumOptimizationsModule INSTANCE;

    private final BoolSetting betterCulling = add(new BoolSetting("Better Face Culling", true));
    private final BoolSetting reduceStateChanges = add(new BoolSetting("Reduce State Changes", true));
    private final BoolSetting mergeDrawCalls = add(new BoolSetting("Merge Draw Calls", false));
    private final BoolSetting fastChunkRendering = add(new BoolSetting("Fast Chunk Rendering", true));
    private final NumberSetting maxChunkSections = add(new NumberSetting("Max Chunk Sections/Frame", 64.0D, 16.0D, 256.0D, 8.0D));

    public SodiumOptimizationsModule() {
        super("Sodium Optimizations", "Various rendering optimizations inspired by Sodium.", Category.PERFORMANCE);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        // Settings for rendering hooks to check
    }

    public static boolean useBetterCulling() {
        SodiumOptimizationsModule m = INSTANCE;
        return m != null && m.isEnabled() && m.betterCulling.get();
    }

    public static boolean useReduceStateChanges() {
        SodiumOptimizationsModule m = INSTANCE;
        return m != null && m.isEnabled() && m.reduceStateChanges.get();
    }

    public static boolean useMergeDrawCalls() {
        SodiumOptimizationsModule m = INSTANCE;
        return m != null && m.isEnabled() && m.mergeDrawCalls.get();
    }

    public static boolean useFastChunkRendering() {
        SodiumOptimizationsModule m = INSTANCE;
        return m != null && m.isEnabled() && m.fastChunkRendering.get();
    }

    public static int getMaxChunkSections() {
        SodiumOptimizationsModule m = INSTANCE;
        return m == null || !m.isEnabled() ? 256 : m.maxChunkSections.getInt();
    }
}