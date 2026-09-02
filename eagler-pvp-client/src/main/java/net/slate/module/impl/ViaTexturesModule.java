package net.slate.module.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.ModeSetting;

import java.util.HashMap;
import java.util.Map;

/**
 * ViaTextures - Texture compatibility layer for modern items in 1.14.
 * Provides fallback mappings for modern item/block textures to 1.14 equivalents.
 * Works with the modern-assets resource pack to provide full item coverage.
 */
public class ViaTexturesModule extends Module {

    private static ViaTexturesModule INSTANCE;

    private final BoolSetting enabled = add(new BoolSetting("Enabled", true));
    private final BoolSetting mapNetherite = add(new BoolSetting("Map Netherite Items", true));
    private final BoolSetting mapMace = add(new BoolSetting("Map Mace", true));
    private final BoolSetting mapRespawnAnchor = add(new BoolSetting("Map Respawn Anchor", true));
    private final BoolSetting mapLodestone = add(new BoolSetting("Map Lodestone", true));
    private final BoolSetting mapCopper = add(new BoolSetting("Map Copper Variants", true));
    private final BoolSetting mapAmethyst = add(new BoolSetting("Map Amethyst Items", true));
    private final ModeSetting fallbackMode = add(new ModeSetting("Fallback Mode", 0, "Vanilla", "MissingTexture", "PinkChecker"));
    private final BoolSetting debugLog = add(new BoolSetting("Debug Log", false));

    // Texture mappings: modern texture path -> 1.14 vanilla texture path
    private static final Map<String, String> ITEM_MAPPINGS = new HashMap<>();
    private static final Map<String, String> BLOCK_MAPPINGS = new HashMap<>();

    static {
        // Modern -> 1.14 item texture mappings
        ITEM_MAPPINGS.put("textures/item/netherite_sword.png", "textures/item/diamond_sword.png");
        ITEM_MAPPINGS.put("textures/item/netherite_pickaxe.png", "textures/item/diamond_pickaxe.png");
        ITEM_MAPPINGS.put("textures/item/netherite_axe.png", "textures/item/diamond_axe.png");
        ITEM_MAPPINGS.put("textures/item/netherite_shovel.png", "textures/item/diamond_shovel.png");
        ITEM_MAPPINGS.put("textures/item/netherite_hoe.png", "textures/item/diamond_hoe.png");
        ITEM_MAPPINGS.put("textures/item/netherite_helmet.png", "textures/item/diamond_helmet.png");
        ITEM_MAPPINGS.put("textures/item/netherite_chestplate.png", "textures/item/diamond_chestplate.png");
        ITEM_MAPPINGS.put("textures/item/netherite_leggings.png", "textures/item/diamond_leggings.png");
        ITEM_MAPPINGS.put("textures/item/netherite_boots.png", "textures/item/diamond_boots.png");
        ITEM_MAPPINGS.put("textures/item/netherite_ingot.png", "textures/item/gold_ingot.png");
        ITEM_MAPPINGS.put("textures/item/netherite_scrap.png", "textures/item/iron_ingot.png");
        ITEM_MAPPINGS.put("textures/item/netherite_upgrade_smithing_template.png", "textures/item/iron_ingot.png");
        ITEM_MAPPINGS.put("textures/item/netherite_horse_armor.png", "textures/item/diamond_horse_armor.png");
        
        ITEM_MAPPINGS.put("textures/item/mace.png", "textures/item/diamond_sword.png");
        ITEM_MAPPINGS.put("textures/item/respawn_anchor.png", "textures/item/obsidian.png");
        ITEM_MAPPINGS.put("textures/item/lodestone.png", "textures/item/compass.png");
        ITEM_MAPPINGS.put("textures/item/bell.png", "textures/item/iron_ingot.png");
        
        // Copper variants
        ITEM_MAPPINGS.put("textures/item/waxed_copper_block.png", "textures/item/iron_block.png");
        ITEM_MAPPINGS.put("textures/item/waxed_exposed_copper.png", "textures/item/iron_block.png");
        ITEM_MAPPINGS.put("textures/item/waxed_weathered_copper.png", "textures/item/iron_block.png");
        ITEM_MAPPINGS.put("textures/item/waxed_oxidized_copper.png", "textures/item/iron_block.png");
        
        // Amethyst
        ITEM_MAPPINGS.put("textures/item/amethyst_shard.png", "textures/item/diamond.png");
        ITEM_MAPPINGS.put("textures/item/amethyst_block.png", "textures/item/diamond_block.png");
        ITEM_MAPPINGS.put("textures/item/budding_amethyst.png", "textures/item/diamond_ore.png");
        ITEM_MAPPINGS.put("textures/item/amethyst_cluster.png", "textures/item/diamond_block.png");
        
        // Block mappings
        BLOCK_MAPPINGS.put("textures/block/netherite_block.png", "textures/block/diamond_block.png");
        BLOCK_MAPPINGS.put("textures/block/ancient_debris.png", "textures/block/obsidian.png");
        BLOCK_MAPPINGS.put("textures/block/respawn_anchor.png", "textures/block/obsidian.png");
        BLOCK_MAPPINGS.put("textures/block/lodestone.png", "textures/block/iron_block.png");
        BLOCK_MAPPINGS.put("textures/block/copper_block.png", "textures/block/iron_block.png");
        BLOCK_MAPPINGS.put("textures/block/exposed_copper.png", "textures/block/iron_block.png");
        BLOCK_MAPPINGS.put("textures/block/weathered_copper.png", "textures/block/iron_block.png");
        BLOCK_MAPPINGS.put("textures/block/oxidized_copper.png", "textures/block/iron_block.png");
        BLOCK_MAPPINGS.put("textures/block/amethyst_block.png", "textures/block/diamond_block.png");
        BLOCK_MAPPINGS.put("textures/block/budding_amethyst.png", "textures/block/diamond_ore.png");
        BLOCK_MAPPINGS.put("textures/block/amethyst_cluster.png", "textures/block/diamond_block.png");
    }

    public ViaTexturesModule() {
        super("ViaTextures", "Maps modern item/block textures to 1.14 equivalents.", Category.VISUAL);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (debugLog.get()) {
            System.out.println("[ViaTextures] Enabled - providing fallback mappings for modern textures");
        }
    }

    @Override
    public void onDisable() {
        if (debugLog.get()) {
            System.out.println("[ViaTextures] Disabled");
        }
    }

    public static boolean isModuleEnabled() {
        ViaTexturesModule m = INSTANCE;
        return m != null && m.isEnabled() && m.enabled.get();
    }

    public static String getMappedTexture(String modernPath) {
        ViaTexturesModule m = INSTANCE;
        if (m == null || !m.isEnabled() || !m.enabled.get()) {
            return modernPath;
        }
        return ITEM_MAPPINGS.getOrDefault(modernPath, BLOCK_MAPPINGS.getOrDefault(modernPath, modernPath));
    }

    public static boolean shouldMapNetherite() {
        ViaTexturesModule m = INSTANCE;
        return m != null && m.isEnabled() && m.enabled.get() && m.mapNetherite.get();
    }

    public static boolean shouldMapMace() {
        ViaTexturesModule m = INSTANCE;
        return m != null && m.isEnabled() && m.enabled.get() && m.mapMace.get();
    }

    public static boolean shouldMapRespawnAnchor() {
        ViaTexturesModule m = INSTANCE;
        return m != null && m.isEnabled() && m.enabled.get() && m.mapRespawnAnchor.get();
    }

    public static boolean shouldMapLodestone() {
        ViaTexturesModule m = INSTANCE;
        return m != null && m.isEnabled() && m.enabled.get() && m.mapLodestone.get();
    }

    public static boolean shouldMapCopper() {
        ViaTexturesModule m = INSTANCE;
        return m != null && m.isEnabled() && m.enabled.get() && m.mapCopper.get();
    }

    public static boolean shouldMapAmethyst() {
        ViaTexturesModule m = INSTANCE;
        return m != null && m.isEnabled() && m.enabled.get() && m.mapAmethyst.get();
    }
}