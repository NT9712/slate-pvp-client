# Modern Assets Compatibility — Supported & Unsupported

This document lists which modern Minecraft assets (from the CurseForge archive, pack_format 69 / 1.21+) are successfully converted to Eaglercraft 1.14 format (pack_format 4), and which are not.

## Conversion Approach

- **Source**: CurseForge mod 1401394 (Modern UI / Modern Assets pack), pack_format 69
- **Target**: Eaglercraft 1.14 (Slate client), pack_format 4
- **Fallback chain**: Modern texture → Mapped vanilla equivalent → Vanilla 1.14 texture → Missing texture (purple/black checkerboard)

---

## ✅ Fully Supported (Inventory + World Rendering)

| Asset Type | Items | Notes |
|------------|-------|-------|
| **Items (inventory icons)** | Trident, Totem of Undying, Shield, Mace, Bell, Honeycomb, Honey Bottle, Suspicious Stew | Textures + item models (handheld/throwing variants where applicable) |
| **Netherite tools/armor (icons)** | Sword, Pickaxe, Axe, Shovel, Hoe, Helmet, Chestplate, Leggings, Boots, Ingot, Scrap, Upgrade Template, Horse Armor | Inventory icons only — no 1.14 registry entries for world items |
| **Enchanted glint** | Item glint, Armor glint | Both PNG + .mcmeta animation |
| **Particles** | Glint, Enchanted hit | PNG + .mcmeta |
| **Mob effect icons** | All 40 modern effect icons (absorption, bad_omen, blindness, breath_of_the_nautilus, conduit_power, darkness, dolphins_grace, fire_resistance, glowing, haste, health_boost, hero_of_the_village, hunger, infested, instant_damage, instant_health, invisibility, jump_boost, levitation, luck, mining_fatigue, nausea, night_vision, oozing, poison, raid_omen, regeneration, resistance, saturation, slow_falling, slowness, speed, strength, trial_omen, unluck, water_breathing, weakness, weaving, wind_charged, wither) | Full set with animation .mcmeta where present |
| **GUI** | Shield slot sprite | Container slot overlay |
| **Entity textures** | Trident, Trident riptide, Shield base, Shield base (no pattern) | Used for rendering thrown/projectile entities |
| **Armor entity textures (converted)** | Leather, Chainmail, Iron, Gold, Diamond, Turtle, Netherite (placeholder) | Converted from modern `entity/equipment/humanoid/<material>.png` → `models/armor/<material>_layer_1.png` + `_layer_2.png` |

---

## ⚠️ Partially Supported (Inventory Only — No World Rendering)

| Asset | Reason |
|-------|--------|
| **Netherite tools/armor** | Textures + models provided for inventory icons. No 1.14 item registry entries, no block states, no world models. Will show in inventory/creative but cannot be crafted, held, or placed in world without protocol/registry work. |
| **Mace** | Texture + model provided. No 1.14 item registry entry. Cannot be used in world. |
| **Respawn Anchor** | Texture not found in source archive (WARNING during conversion). Would need manual addition. |
| **Lodestone** | Texture not found in source archive. |
| **Waxed copper variants** | Textures not found in source archive. |

---

## ❌ Unsupported (Require Registry/Protocol/Block Model Work)

| Category | Items | What's Missing |
|----------|-------|----------------|
| **New blocks** | Respawn Anchor, Lodestone, Copper variants, Amethyst, Calcite, Tuff, Deepslate variants, Sculk blocks, Mangrove wood, Cherry wood, Bamboo wood, Mud bricks, etc. | Block states, block models, block entities, world generation |
| **New entities** | Warden, Allay, Frog, Tadpole, Camel, Sniffer, Armadillo, Breeze, Bogged, etc. | Entity models, renderers, AI, spawn eggs |
| **New biomes** | Deep Dark, Mangrove Swamp, Cherry Grove, etc. | Biome data, world gen |
| **New mechanics** | Smithing table upgrades, Archaeology, Trial Chambers, Vault, etc. | Server-side protocol, block entities, loot tables |
| **Trim patterns** | Armor trims (16 patterns × 10 materials) | Item models, rendering logic, NBT handling |
| **Shield patterns** | Banner patterns on shields | Pattern rendering, NBT |

---

## Armor Format Conversion Details

### Modern (1.20+) Format
```
textures/entity/equipment/humanoid/
  leather.png
  chainmail.png
  iron.png
  gold.png
  diamond.png
  netherite.png
  turtle.png
  leather_overlay.png
```

### Eagler 1.14 Format
```
textures/models/armor/
  leather_layer_1.png
  leather_layer_2.png
  leather_layer_1_overlay.png
  chainmail_layer_1.png
  chainmail_layer_2.png
  iron_layer_1.png
  iron_layer_2.png
  gold_layer_1.png
  gold_layer_2.png
  diamond_layer_1.png
  diamond_layer_2.png
  netherite_layer_1.png  ← Created from modern netherite.png
  netherite_layer_2.png  ← Blank (transparent)
  turtle_layer_1.png
```

### Conversion Logic
1. Modern single texture → Layer 1 (full armor texture)
2. Layer 2 created as blank transparent (1.14 uses layer 2 for leggings overlay)
3. Leather overlay copied separately for dye support
4. **Netherite**: No 1.14 equivalent. Layer 1 = modern texture, Layer 2 = blank. Documented as placeholder.

---

## Item Model Conversion Notes

Modern item models use features not in 1.14:
- `handheld` / `throwing` display contexts
- `gui`, `ground`, `fixed` transforms differ

**Copied models (as-is from modern):**
- `trident.json`, `trident_in_hand.json`, `trident_throwing.json`
- `totem_of_undying.json`
- `shield.json`, `shield_blocking.json`
- `mace.json`, `handheld_mace.json`

These may render incorrectly in 1.14 because:
- Display context names differ (`handheld` → `thirdperson_righthand`, etc.)
- Transform values calibrated for 1.20+ model format

**Recommendation**: For production use, hand-author 1.14-compatible models using the modern textures as reference.

---

## Animation (.mcmeta) Preservation

All `.mcmeta` files found in source are copied alongside their textures:
- `enchanted_glint_item.png.mcmeta`
- `enchanted_glint_armor.png.mcmeta`
- `glint.png.mcmeta` (particle)
- `enchanted_hit.png.mcmeta` (particle)
- `vibration.png.mcmeta` (particle — copied if present)
- Mob effect icons: .mcmeta copied where present in source

Animation parameters (frametime, frames, interpolate) are preserved verbatim.

---

## Testing Checklist

- [ ] Load resource pack in desktop runtime (`resourcePacks:["modern-assets"]` in options.txt)
- [ ] Open creative inventory → verify netherite tools/armor/mace/trident/totem icons render
- [ ] Hold trident in hand → verify model renders (first person)
- [ ] Throw trident → verify entity texture renders
- [ ] Equip netherite armor (via creative/commands) → verify armor renders on player
- [ ] Apply potion effects → verify HUD effect icons render with animation
- [ ] Enchant item → verify glint animation plays
- [ ] Check for missing texture (purple/black) errors in logs

---

## Known Issues / TODOs

1. **Respawn Anchor / Lodestone textures**: Not in source archive — need manual extraction from vanilla 1.21 assets
2. **Waxed copper variants**: Not in source archive
3. **Netherite armor Layer 2**: Blank — may look wrong on leggings in third person
4. **Item model transforms**: Modern models may not align correctly in 1.14 first person
5. **Shield pattern rendering**: Modern shield base textures copied, but banner pattern overlay logic is 1.14 vanilla
6. **Missing mob effect .mcmeta**: Source archive has no .mcmeta for effect icons — animations won't play

---

## Building the EPK

```bash
cd /home/nixel_tide/projects/eagler-pvp-client
java -jar target_teavm_javascript/buildtools/CompileEPK.jar modern-assets desktopRuntime/filesystem/resourcePacks/run/modern-assets.epk
```

Then enable in `desktopRuntime/filesystem/resourcePacks/run/options.txt`:
```
resourcePacks:["modern-assets"]
```