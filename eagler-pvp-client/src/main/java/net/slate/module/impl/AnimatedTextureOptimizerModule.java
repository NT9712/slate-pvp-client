package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.NumberSetting;

/**
 * Animated Texture Optimizer - reduces overhead from animated textures.
 * - Lowers animation frame rate for distant/out-of-view textures
 * - Disables animation for textures not in view
 * - Limits max animated textures
 */
public class AnimatedTextureOptimizerModule extends Module {

    private static AnimatedTextureOptimizerModule INSTANCE;

    private final BoolSetting reduceDistantFPS = add(new BoolSetting("Reduce Distant Animation FPS", true));
    private final BoolSetting cullOutOfView = add(new BoolSetting("Cull Out-of-View Animations", true));
    private final NumberSetting maxAnimatedTextures = add(new NumberSetting("Max Animated Textures", 256.0D, 32.0D, 1024.0D, 32.0D));
    private final NumberSetting distantFPS = add(new NumberSetting("Distant Animation FPS", 5.0D, 1.0D, 20.0D, 1.0D));
    private final BoolSetting disableWaterAnimation = add(new BoolSetting("Disable Water Animation", false));
    private final BoolSetting disableLavaAnimation = add(new BoolSetting("Disable Lava Animation", false));
    private final BoolSetting disableFireAnimation = add(new BoolSetting("Disable Fire Animation", false));
    private final BoolSetting disablePortalAnimation = add(new BoolSetting("Disable Portal Animation", false));

    public AnimatedTextureOptimizerModule() {
        super("Animated Texture Optimizer", "Reduces overhead from animated textures.", Category.PERFORMANCE);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        // Settings for TextureManager/TextureAtlasSprite hooks to check
    }

    public static boolean reduceDistantFPS() {
        AnimatedTextureOptimizerModule m = INSTANCE;
        return m != null && m.isEnabled() && m.reduceDistantFPS.get();
    }

    public static boolean cullOutOfView() {
        AnimatedTextureOptimizerModule m = INSTANCE;
        return m != null && m.isEnabled() && m.cullOutOfView.get();
    }

    public static int getMaxAnimatedTextures() {
        AnimatedTextureOptimizerModule m = INSTANCE;
        return m == null || !m.isEnabled() ? 1024 : m.maxAnimatedTextures.getInt();
    }

    public static int getDistantFPS() {
        AnimatedTextureOptimizerModule m = INSTANCE;
        return m == null || !m.isEnabled() ? 20 : m.distantFPS.getInt();
    }

    public static boolean disableWaterAnimation() {
        AnimatedTextureOptimizerModule m = INSTANCE;
        return m != null && m.isEnabled() && m.disableWaterAnimation.get();
    }

    public static boolean disableLavaAnimation() {
        AnimatedTextureOptimizerModule m = INSTANCE;
        return m != null && m.isEnabled() && m.disableLavaAnimation.get();
    }

    public static boolean disableFireAnimation() {
        AnimatedTextureOptimizerModule m = INSTANCE;
        return m != null && m.isEnabled() && m.disableFireAnimation.get();
    }

    public static boolean disablePortalAnimation() {
        AnimatedTextureOptimizerModule m = INSTANCE;
        return m != null && m.isEnabled() && m.disablePortalAnimation.get();
    }
}