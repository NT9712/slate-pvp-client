package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.NumberSetting;

/**
 * Entity Culling - don't render entities that are behind walls or outside view frustum.
 * Significantly reduces entity rendering overhead in crowded areas.
 */
public class EntityCullingModule extends Module {

    private static EntityCullingModule INSTANCE;

    private final BoolSetting frustumCulling = add(new BoolSetting("Frustum Culling", true));
    private final BoolSetting occlusionCulling = add(new BoolSetting("Occlusion Culling (Walls)", true));
    private final BoolSetting playerCulling = add(new BoolSetting("Cull Other Players", true));
    private final BoolSetting mobCulling = add(new BoolSetting("Cull Mobs", true));
    private final BoolSetting itemCulling = add(new BoolSetting("Cull Items/Exp Orbs", true));
    private final NumberSetting maxDistance = add(new NumberSetting("Max Cull Distance", 128.0D, 32.0D, 512.0D, 16.0D));

    public EntityCullingModule() {
        super("Entity Culling", "Don't render entities behind walls or outside view.", Category.PERFORMANCE);
        INSTANCE = this;
    }

    public static boolean useFrustumCulling() {
        EntityCullingModule m = INSTANCE;
        return m != null && m.isEnabled() && m.frustumCulling.get();
    }

    public static boolean useOcclusionCulling() {
        EntityCullingModule m = INSTANCE;
        return m != null && m.isEnabled() && m.occlusionCulling.get();
    }

    public static boolean cullPlayers() {
        EntityCullingModule m = INSTANCE;
        return m != null && m.isEnabled() && m.playerCulling.get();
    }

    public static boolean cullMobs() {
        EntityCullingModule m = INSTANCE;
        return m != null && m.isEnabled() && m.mobCulling.get();
    }

    public static boolean cullItems() {
        EntityCullingModule m = INSTANCE;
        return m != null && m.isEnabled() && m.itemCulling.get();
    }

    public static double getMaxDistance() {
        EntityCullingModule m = INSTANCE;
        return m == null || !m.isEnabled() ? 512.0 : m.maxDistance.get();
    }
}