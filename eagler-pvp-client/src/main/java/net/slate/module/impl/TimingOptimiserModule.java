package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.NumberSetting;

/**
 * Timing Optimiser - caps chunk uploads per frame for smoother frame pacing.
 * <p>
 * In vanilla Eaglercraft, chunk uploads are processed until the time budget is exhausted,
 * which can cause frame hitches when many chunks need rebuilding. This module provides a
 * configuration for the maximum chunk uploads per frame.
 * <p>
 * NOTE: This module only stores the setting. To take effect, ChunkRenderDispatcher.runChunkUploads()
 * would need to be modified to respect this limit. This is a legitimate performance/frame-pacing
 * module, NOT a combat timing manipulation.
 */
public class TimingOptimiserModule extends Module {

    private static TimingOptimiserModule INSTANCE;

    private final NumberSetting maxChunkUploads = add(new NumberSetting("Max Chunk Uploads/Frame", 8.0D, 1.0D, 32.0D, 1.0D));

    public TimingOptimiserModule() {
        super("Timing Optimiser", "Caps chunk uploads per frame for smoother frame pacing (requires core hook).", Category.PERFORMANCE);
        INSTANCE = this;
    }

    public static int getMaxChunkUploads() {
        TimingOptimiserModule m = INSTANCE;
        return m == null || !m.isEnabled() ? -1 : m.maxChunkUploads.getInt();
    }
}