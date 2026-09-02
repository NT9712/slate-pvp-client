package net.slate.module.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.NumberSetting;

/**
 * Smart Animations - only animate blocks/entities in view to save CPU/GPU.
 * Reduces overhead from animating thousands of blocks/chunks that aren't visible.
 */
public class SmartAnimationsModule extends Module {

    private static SmartAnimationsModule INSTANCE;

    private final BoolSetting animatedBlocks = add(new BoolSetting("Animated Blocks", true));
    private final BoolSetting animatedEntities = add(new BoolSetting("Animated Entities", true));
    private final BoolSetting animatedTileEntities = add(new BoolSetting("Animated Tile Entities", true));
    private final NumberSetting cullDistance = add(new NumberSetting("Cull Distance", 64.0D, 16.0D, 256.0D, 8.0D));

    public SmartAnimationsModule() {
        super("Smart Animations", "Only animate blocks/entities in view to save performance.", Category.PERFORMANCE);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        // This module provides settings for other systems to check
        // Actual implementation would hook into ChunkRenderDispatcher and entity renderers
    }

    public static boolean shouldAnimateBlocks() {
        SmartAnimationsModule m = INSTANCE;
        return m != null && m.isEnabled() && m.animatedBlocks.get();
    }

    public static boolean shouldAnimateEntities() {
        SmartAnimationsModule m = INSTANCE;
        return m != null && m.isEnabled() && m.animatedEntities.get();
    }

    public static boolean shouldAnimateTileEntities() {
        SmartAnimationsModule m = INSTANCE;
        return m != null && m.isEnabled() && m.animatedTileEntities.get();
    }

    public static double getCullDistance() {
        SmartAnimationsModule m = INSTANCE;
        return m == null || !m.isEnabled() ? 256.0 : m.cullDistance.get();
    }
}