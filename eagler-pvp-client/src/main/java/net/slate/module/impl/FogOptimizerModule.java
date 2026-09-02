package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.NumberSetting;

/**
 * Fog Optimizer - optimizes fog rendering for better performance.
 * - Simplifies fog calculations
 * - Reduces fog updates per frame
 * - Optional: flat fog for maximum performance
 */
public class FogOptimizerModule extends Module {

    private static FogOptimizerModule INSTANCE;

    private final BoolSetting simplifyFog = add(new BoolSetting("Simplify Fog Math", true));
    private final BoolSetting reduceUpdates = add(new BoolSetting("Reduce Fog Updates", true));
    private final NumberSetting updateInterval = add(new NumberSetting("Fog Update Interval (ticks)", 4.0D, 1.0D, 20.0D, 1.0D));
    private final BoolSetting flatFog = add(new BoolSetting("Flat Fog (Fastest)", false));
    private final BoolSetting disableVoidFog = add(new BoolSetting("Disable Void Fog", true));
    private final BoolSetting disableNetherFog = add(new BoolSetting("Disable Nether Fog", false));

    private int tickCounter = 0;

    public FogOptimizerModule() {
        super("Fog Optimizer", "Optimizes fog rendering for better performance.", Category.PERFORMANCE);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        tickCounter++;
    }

    public static boolean shouldSimplifyFog() {
        FogOptimizerModule m = INSTANCE;
        return m != null && m.isEnabled() && m.simplifyFog.get();
    }

    public static boolean shouldReduceUpdates() {
        FogOptimizerModule m = INSTANCE;
        return m != null && m.isEnabled() && m.reduceUpdates.get();
    }

    public static int getUpdateInterval() {
        FogOptimizerModule m = INSTANCE;
        return m == null || !m.isEnabled() ? 1 : m.updateInterval.getInt();
    }

    public static boolean shouldUpdateThisTick(int tickCounter) {
        FogOptimizerModule m = INSTANCE;
        if (m == null || !m.isEnabled() || !m.reduceUpdates.get()) {
            return true;
        }
        return tickCounter % m.updateInterval.getInt() == 0;
    }

    public static boolean useFlatFog() {
        FogOptimizerModule m = INSTANCE;
        return m != null && m.isEnabled() && m.flatFog.get();
    }

    public static boolean disableVoidFog() {
        FogOptimizerModule m = INSTANCE;
        return m != null && m.isEnabled() && m.disableVoidFog.get();
    }

    public static boolean disableNetherFog() {
        FogOptimizerModule m = INSTANCE;
        return m != null && m.isEnabled() && m.disableNetherFog.get();
    }
}