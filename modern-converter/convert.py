#!/usr/bin/env python3
"""
Modern Assets to Eagler 1.14 Converter
Converts modern Minecraft resource pack (pack_format 69+) to Eagler 1.14 format (pack_format 4)
"""

import os
import json
import shutil
from pathlib import Path
from PIL import Image

MODERN_SRC = Path("/home/nixel_tide/projects/modern-src/assets/minecraft")
OUTPUT_DIR = Path("/home/nixel_tide/projects/eagler-pvp-client/modern-assets")

# Unique item files to copy (modern -> 1.14 same path)
ITEM_FILES = [
    "textures/item/totem_of_undying.png",
    "textures/item/trident.png",
    "textures/item/mace.png",
    "textures/item/netherite_sword.png",
    "textures/item/netherite_pickaxe.png",
    "textures/item/netherite_axe.png",
    "textures/item/netherite_shovel.png",
    "textures/item/netherite_hoe.png",
    "textures/item/netherite_helmet.png",
    "textures/item/netherite_chestplate.png",
    "textures/item/netherite_leggings.png",
    "textures/item/netherite_boots.png",
    "textures/item/netherite_ingot.png",
    "textures/item/netherite_scrap.png",
    "textures/item/netherite_upgrade_smithing_template.png",
    "textures/item/netherite_horse_armor.png",
    "textures/item/bell.png",
    "textures/item/honeycomb.png",
    "textures/item/honey_bottle.png",
    "textures/item/suspicious_stew.png",
    # Copper variants
    "textures/item/copper_ingot.png",
    "textures/item/raw_copper.png",
    "textures/item/copper_block.png",
    "textures/item/exposed_copper.png",
    "textures/item/weathered_copper.png",
    "textures/item/oxidized_copper.png",
    "textures/item/waxed_copper_block.png",
    "textures/item/waxed_exposed_copper.png",
    "textures/item/waxed_weathered_copper.png",
    "textures/item/waxed_oxidized_copper.png",
    "textures/item/cut_copper.png",
    "textures/item/exposed_cut_copper.png",
    "textures/item/weathered_cut_copper.png",
    "textures/item/oxidized_cut_copper.png",
    "textures/item/waxed_cut_copper.png",
    "textures/item/waxed_exposed_cut_copper.png",
    "textures/item/waxed_weathered_cut_copper.png",
    "textures/item/waxed_oxidized_cut_copper.png",
    # Amethyst
    "textures/item/amethyst_shard.png",
    "textures/item/amethyst_block.png",
    "textures/item/budding_amethyst.png",
    "textures/item/amethyst_cluster.png",
    "textures/item/large_amethyst_bud.png",
    "textures/item/medium_amethyst_bud.png",
    "textures/item/small_amethyst_bud.png",
    # Other modern items
    "textures/item/glow_ink_sac.png",
    "textures/item/glow_item_frame.png",
    "textures/item/golden_carrot.png",
    "textures/item/glistering_melon_slice.png",
    "textures/item/bundle.png",
    "textures/item/azalea.png",
    "textures/item/flowering_azalea.png",
    "textures/item/rooted_dirt.png",
    "textures/item/mud_bricks.png",
    "textures/item/packed_mud.png",
    "textures/item/mangrove_roots.png",
    "textures/item/mangrove_propagule.png",
    "textures/item/frogspawn.png",
    "textures/item/echo_shard.png",
    "textures/item/recovery_compass.png",
    "textures/item/disc_fragment_5.png",
]

# Entity textures
ENTITY_FILES = [
    "textures/entity/trident.png",
    "textures/entity/trident_riptide.png",
    "textures/entity/shield_base.png",
    "textures/entity/shield_base_nopattern.png",
]

# GUI files
GUI_FILES = [
    "textures/gui/sprites/container/slot/shield.png",
]

# Particle files (with .mcmeta if available)
PARTICLE_FILES = [
    "textures/particle/glint.png",
    "textures/particle/enchanted_hit.png",
    "textures/particle/vibration.png",
]

# Enchanted glint files
GLINT_FILES = [
    "textures/misc/enchanted_glint_item.png",
    "textures/misc/enchanted_glint_item.png.mcmeta",
    "textures/misc/enchanted_glint_armor.png",
    "textures/misc/enchanted_glint_armor.png.mcmeta",
]

# Effect icons directory
EFFECT_ICONS_DIR = "textures/mob_effect/"

# Item models to copy
ITEM_MODELS = [
    "models/item/trident.json",
    "models/item/trident_in_hand.json",
    "models/item/trident_throwing.json",
    "models/item/totem_of_undying.json",
    "models/item/shield.json",
    "models/item/shield_blocking.json",
    "models/item/mace.json",
    "models/item/handheld_mace.json",
]

# Additional directories to copy entirely (with .mcmeta)
ADDITIONAL_DIRS = [
    "textures/gui",
    "textures/particle",
    "textures/mob_effect",
    "textures/misc",
    "textures/painting",
    "textures/colormap",
    "textures/environment",
    "textures/font",
    "textures/map",
    "textures/block",
    "textures/trims",
    "textures/entity",
    "textures/effect",
    "models/block",
    "models/item",
]

def ensure_dir(path):
    path.parent.mkdir(parents=True, exist_ok=True)

def copy_file(src_rel, dst_rel):
    """Copy a single file from modern-src to output, including .mcmeta if exists"""
    src = MODERN_SRC / src_rel
    dst = OUTPUT_DIR / "assets/minecraft" / dst_rel
    
    if not src.exists():
        print(f"  WARNING: Source not found: {src_rel}")
        return False
    
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    
    # Also copy .mcmeta if exists
    mcmeta_src = src.with_suffix(src.suffix + ".mcmeta")
    if mcmeta_src.exists():
        mcmeta_dst = OUTPUT_DIR / "assets/minecraft" / (dst_rel + ".mcmeta")
        mcmeta_dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(mcmeta_src, mcmeta_dst)
        print(f"  Copied .mcmeta: {src_rel}.mcmeta")
    
    print(f"  Copied: {src_rel} -> {dst_rel}")
    return True

def copy_dir(src_rel, dst_rel):
    """Copy entire directory recursively with .mcmeta files"""
    src = MODERN_SRC / src_rel
    dst = OUTPUT_DIR / "assets/minecraft" / dst_rel
    
    if not src.exists():
        print(f"  WARNING: Source dir not found: {src_rel}")
        return 0
    
    count = 0
    for file in src.rglob("*"):
        if file.is_file() and not file.name.endswith(".mcmeta"):
            rel = file.relative_to(MODERN_SRC)
            dst_file = OUTPUT_DIR / "assets/minecraft" / rel
            dst_file.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(file, dst_file)
            
            # Copy .mcmeta if exists
            mcmeta = file.with_suffix(file.suffix + ".mcmeta")
            if mcmeta.exists():
                mcmeta_dst = OUTPUT_DIR / "assets/minecraft" / rel.with_suffix(rel.suffix + ".mcmeta")
                mcmeta_dst.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(mcmeta, mcmeta_dst)
            count += 1
    print(f"  Copied directory {src_rel}: {count} files")
    return count

def convert_armor_textures():
    """Convert modern armor textures to 1.14 format with proper layer_1 and layer_2"""
    print("\n=== Converting armor textures (humanoid + humanoid_leggings) ===")
    total = 0
    
    humanoid_src = MODERN_SRC / "textures/entity/equipment/humanoid"
    leggings_src = MODERN_SRC / "textures/entity/equipment/humanoid_leggings"
    dst_dir = OUTPUT_DIR / "assets/minecraft/textures/models/armor"
    dst_dir.mkdir(parents=True, exist_ok=True)
    
    if not humanoid_src.exists():
        print("  WARNING: humanoid source not found")
        return 0
    
    for file in humanoid_src.glob("*.png"):
        material = file.stem
        layer1_src = file
        layer2_src = leggings_src / f"{material}.png" if leggings_src.exists() else None
        
        # Layer 1 (main armor body)
        layer1_dst = dst_dir / f"{material}_layer_1.png"
        shutil.copy2(layer1_src, layer1_dst)
        
        # Copy .mcmeta if exists
        mcmeta = layer1_src.with_suffix(".mcmeta")
        if mcmeta.exists():
            shutil.copy2(mcmeta, dst_dir / f"{material}_layer_1.png.mcmeta")
        
        # Layer 2 (leggings)
        if layer2_src and layer2_src.exists():
            layer2_dst = dst_dir / f"{material}_layer_2.png"
            shutil.copy2(layer2_src, layer2_dst)
            mcmeta2 = layer2_src.with_suffix(".mcmeta")
            if mcmeta2.exists():
                shutil.copy2(mcmeta2, dst_dir / f"{material}_layer_2.png.mcmeta")
            print(f"  Converted {material}: layer_1 + layer_2 (from humanoid_leggings)")
        else:
            # Create blank layer 2
            img = Image.open(layer1_src).convert("RGBA")
            img_blank = Image.new("RGBA", img.size, (0, 0, 0, 0))
            img_blank.save(dst_dir / f"{material}_layer_2.png")
            print(f"  Converted {material}: layer_1 + blank layer_2 (no leggings)")
        
        # Handle leather overlay
        if material == "leather":
            overlay_src = humanoid_src / "leather_overlay.png"
            if overlay_src.exists():
                shutil.copy2(overlay_src, dst_dir / "leather_layer_1_overlay.png")
                mcmeta = overlay_src.with_suffix(".mcmeta")
                if mcmeta.exists():
                    shutil.copy2(mcmeta, dst_dir / "leather_layer_1_overlay.png.mcmeta")
                print(f"  Copied leather overlay")
        
        total += 2
    
    return total

def main():
    print("=== Creating output structure ===")
    dirs = [
        "assets/minecraft/textures/item",
        "assets/minecraft/textures/entity",
        "assets/minecraft/textures/mob_effect",
        "assets/minecraft/textures/gui",
        "assets/minecraft/textures/misc",
        "assets/minecraft/textures/particle",
        "assets/minecraft/models/item",
        "assets/minecraft/models/armor",
        "assets/minecraft/textures/models/armor",
    ]
    
    for d in dirs:
        os.makedirs(OUTPUT_DIR / d, exist_ok=True)
    
    # Create pack.mcmeta
    pack_meta = {
        "pack": {
            "pack_format": 4,
            "description": "Slate Modern Assets - Modern Minecraft assets for Eaglercraft 1.14"
        }
    }
    with open(OUTPUT_DIR / "pack.mcmeta", "w") as f:
        json.dump(pack_meta, f, indent=2)
    
    # Create pack.png (1x1 transparent placeholder)
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    img.save(OUTPUT_DIR / "pack.png")
    
    print("Created output structure")
    
    total_copied = 0
    
    print("\n=== Copying item textures ===")
    for f in ITEM_FILES:
        if copy_file(f, f):
            total_copied += 1
    
    print("\n=== Copying entity textures ===")
    for f in ENTITY_FILES:
        if copy_file(f, f):
            total_copied += 1
    
    print("\n=== Copying GUI files ===")
    for f in GUI_FILES:
        if copy_file(f, f):
            total_copied += 1
    
    print("\n=== Copying particle files ===")
    for f in PARTICLE_FILES:
        if copy_file(f, f):
            total_copied += 1
    
    print("\n=== Copying glint files ===")
    for f in GLINT_FILES:
        if copy_file(f, f):
            total_copied += 1
    
    print("\n=== Copying effect icons directory ===")
    total_copied += copy_dir(EFFECT_ICONS_DIR, EFFECT_ICONS_DIR)
    
    print("\n=== Copying item models ===")
    for f in ITEM_MODELS:
        if copy_file(f, f):
            total_copied += 1
    
    print("\n=== Converting armor textures ===")
    total_copied += convert_armor_textures()
    
    print("\n=== Copying additional directories ===")
    for d in ADDITIONAL_DIRS:
        total_copied += copy_dir(d, d)
    
    # Verify pack files
    print("\n=== Verifying pack files ===")
    with open(OUTPUT_DIR / "pack.mcmeta", "w") as f:
        json.dump(pack_meta, f, indent=2)
    
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    img.save(OUTPUT_DIR / "pack.png")
    
    print(f"\n=== Done! Total files copied: {total_copied} ===")

if __name__ == "__main__":
    main()