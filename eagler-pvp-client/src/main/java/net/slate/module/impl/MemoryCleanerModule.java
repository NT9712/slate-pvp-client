package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.NumberSetting;

/**
 * Memory Cleaner - periodic garbage collection and memory optimization.
 * Helps prevent memory leaks and reduces GC spikes during gameplay.
 */
public class MemoryCleanerModule extends Module {

    private static MemoryCleanerModule INSTANCE;

    private final BoolSetting periodicGC = add(new BoolSetting("Periodic GC", true));
    private final NumberSetting gcInterval = add(new NumberSetting("GC Interval (seconds)", 30.0D, 10.0D, 300.0D, 5.0D));
    private final BoolSetting clearTextureCache = add(new BoolSetting("Clear Unused Texture Cache", false));
    private final BoolSetting clearModelCache = add(new BoolSetting("Clear Unused Model Cache", false));
    private final BoolSetting logMemory = add(new BoolSetting("Log Memory Usage", false));

    private long lastGC = 0;
    private long lastLog = 0;

    public MemoryCleanerModule() {
        super("Memory Cleaner", "Periodic garbage collection and memory optimization.", Category.PERFORMANCE);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        long now = System.currentTimeMillis();
        
        long intervalMs = (long) (gcInterval.get() * 1000.0);
        if (periodicGC.get() && (now - lastGC) > intervalMs) {
            lastGC = now;
            System.gc();
            if (logMemory.get()) {
                Runtime rt = Runtime.getRuntime();
                long used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
                long max = 1024; // TeaVM doesn't expose maxMemory(), use default 1GB
                System.out.println("[MemoryCleaner] Memory: " + used + "MB / " + max + "MB");
            }
        }

        // Clear caches less frequently
        if (clearTextureCache.get() && (now - lastGC) > intervalMs * 2) {
            // TextureManager would need a hook to clear unused textures
        }
    }

    public static boolean shouldPeriodicGC() {
        MemoryCleanerModule m = INSTANCE;
        return m != null && m.isEnabled() && m.periodicGC.get();
    }
}