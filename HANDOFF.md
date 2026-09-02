# Slate — InfinityMC PvP Client Handoff Document

## Project Overview
A modern PvP client built into Eaglercraft 1.14.4 workspace, branded for **InfinityMC** (`wss://infinitymc.oops.wtf`). Features 32 modules, CS2-style GUI, modern asset compatibility (pack_format 69 → 4), and a complete shader pack.

**Live Deployment:** https://slate-client-glb1kxed6-nixeltide-8187s-projects.vercel.app (WASM-GC)

---

## Architecture

```
net/slate/
  Slate.java              Entry point: init, tick, key events, HUD hook
  ClientEvents.java       Shared combat state (CPS, combo, reach, hit/hurt times)
  Config.java             Key=value persistence through EagRuntime storage
  module/                 Module, HudModule, ModuleManager, Category, settings
  module/impl/            32 modules across 6 categories
  hud/                    HudManager (layout + draw), HudDraw (HUD typography)
  ui/                     Theme, Draw, ClickGuiScreen, HudEditorScreen, ColorPicker, Notifications
  Branding.java           InfinityMC wordmark (red/blue), server list registration
  Config.java             Persistent settings
```

### Vanilla Hooks (minimal, delegating to static methods)
| File | Hook |
|------|------|
| `Minecraft` | `Slate.init()`, `Slate.onTick()`, attack notification, client frame limit |
| `KeyboardListener` | Key state tracking and key-down edge |
| `MouseHelper` | Click counting, zoom mouse scaling, freelook look capture |
| `IngameGui` | HUD draw, custom crosshair, hit marker, toasts, clean scoreboard |
| `GameRenderer` | Zoom FOV, hurt camera scale, **dynamic FOV disable** |
| `ActiveRenderInfo` | Freelook camera offsets |
| `FirstPersonRenderer` | Low fire offset |
| `ClientPlayerEntity`, `MovementInputFromOptions` | Toggle sprint/sneak |
| `NewChatGui`, `ChatScreen` | Chat history, duplicate stacking, saved draft |
| `PlayerTabOverlayGui` | Numeric ping |
| `ParticleManager` | Particle limit and explosion culling |
| `LightTexture` | Full bright |

---

## Modules (32 total)

### Combat (5)
- **Crosshair** — Cross/dot/circle, dynamic gap, color, outline
- **Hit Marker** — Crosshair flash on hit, duration/size/color/fade
- **Target** — Name, animated health bar, distance (3s persistence)
- **Combo** — Consecutive hits on same target
- **Reach** — Last hit distance to hit position

### HUD (7)
- **FPS** — Frame rate with color thresholds
- **CPS** — Left/right click counters
- **Ping** — Exact ms from player list
- **Coordinates** — Inline/stacked, decimals, facing, nether conversion
- **Keystrokes** — WASD + mouse/space, animated presses
- **Durability** — Armor + held item, percentage warnings
- **Effects** — Potion icons with amplifier + countdown warnings

### Visual (7)
- **Zoom** — Hold to zoom, smooth, scroll to adjust, mouse scaling
- **Hurt Cam** — Scales damage shake
- **Low Fire** — Lowers first-person fire overlay
- **Full Bright** — Lightmap beyond vanilla slider (allowed on major PvP servers)
- **Clean Scoreboard** — Hides sidebar numbers/background
- **Shader Pack** — 5 presets + custom GLSL (bloom, color grading, SSAO, motion blur)

### Player (4)
- **Toggle Sprint** — Toggle/always-on sprint
- **Toggle Sneak** — Hold sneak, optional jump cancel
- **Freelook** — Camera decouple, 3rd person/front view, crosshair hide
- **No Dynamic FOV** — Disables sprint FOV boost (locks fovModifierHand=1.0)

### Performance (9)
- **Power Saver** — Caps FPS when unfocused/in menus
- **Particle Limit** — Max particles, optional explosion culling
- **Timing Optimiser** — Chunk upload cap per frame
- **Smart Animations** — Only animate visible blocks/entities
- **Sodium Optimizations** — Better culling, state change reduction
- **Entity Culling** — Frustum + occlusion culling
- **Memory Cleaner** — Periodic GC, memory logging
- **Animated Texture Optimizer** — Reduces distant/out-of-view animation FPS
- **Fog Optimizer** — Simplifies fog math, reduces updates

### Misc (3)
- **Chat** — Longer history, duplicate stacking, unsent message draft
- **Tab List** — Exact ping in ms, header/footer hiding
- **Notifications** — Toast on module toggle

---

## Asset Pipeline

### Modern Assets (pack_format 69 → 4)
**Source:** CurseForge mod 1401394 (Modern UI / Modern Assets)
**Output:** `/home/nixel_tide/projects/eagler-pvp-client/modern-assets/`
**EPK:** `desktopRuntime/filesystem/resourcePacks/run/modern-assets.epk`

### Converted Assets (1039 files)
| Type | Status |
|------|--------|
| Items (trident, totem, shield, mace, bell, honey, suspicious stew) | ✅ Full (icons + models) |
| Netherite tools/armor (16 items) | ⚠️ Inventory only (no 1.14 registry) |
| Enchanted glint (item + armor) | ✅ PNG + .mcmeta |
| Particles (glint, enchanted hit, vibration) | ✅ PNG + .mcmeta |
| Mob effect icons (40) | ✅ Full set with animations |
| GUI (shield slot, containers, sprites) | ✅ Complete |
| Armor entity textures | ✅ Converted: `humanoid` → `layer_1`, `humanoid_leggings` → `layer_2` |
| Painting, colormap, environment, font, map | ✅ All directories copied |

### Build
```bash
cd /home/nixel_tide/projects/modern-converter && python3 convert.py
cd /home/nixel_tide/projects/eagler-pvp-client
java -jar target_teavm_javascript/buildtools/CompileEPK.jar modern-assets desktopRuntime/filesystem/resourcePacks/run/modern-assets.epk
```

---

## Shader Pack (Visual Category)

### Presets
| Preset | Base Shader | Effect |
|--------|-------------|--------|
| Vanilla | — | No post-processing |
| BslLite | `fxaa.json` | FXAA + subtle bloom |
| ComplementaryLite | `art.json` | Artistic color grading |
| SildursLite | `color_convolve.json` | Color grading |
| ChocapicLite | `phosphor.json` | Phosphor/retro look |

### Custom GLSL Shaders (`desktopRuntime/resources/assets/minecraft/shaders/`)
- **bloom.fsh** — Threshold extraction, 5×5 Gaussian blur, Reinhard tone mapping
- **color_grading.fsh** — Contrast, saturation, brightness, temperature, tint, vignette, film grain, ACES filmic
- **ssao.fsh** — 16-sample hemisphere, noise dithering, bilateral blur
- **motion_blur.fsh** — Velocity buffer, 16-tap temporal accumulation

### Shader JSON Definitions
`post/bloom.json`, `post/color_grading.json`, `post/ssao.json`, `post/motion_blur.json`

---

## Build Commands

| Target | Command |
|--------|---------|
| Desktop debug runtime | `./gradlew target_lwjgl_desktop:compileJava` |
| WASM-GC client bundle | `./gradlew target_teavm_wasm_gc:makeMainWasmClientBundle` |
| JS client + offline | `./gradlew target_teavm_javascript:makeMainOfflineDownload` |

### Deploy
```bash
cd /home/nixel_tide/projects/deploy
rm -rf public/*
cp ../eagler-pvp-client/target_teavm_wasm_gc/javascript_dist/* public/
vercel --prod
```

---

## Configuration Files

### `/home/nixel_tide/projects/eagler-pvp-client/desktopRuntime/filesystem/resourcePacks/run/options.txt`
```ini
resourcePacks:["modern-assets"]
# ... other settings
```

### `vercel.json` (in `/home/nixel_tide/projects/deploy/`)
```json
{
  "$schema": "https://openapi.vercel.sh/vercel.json",
  "headers": [
    { "source": "/", "headers": [{ "key": "Cache-Control", "value": "public, max-age=0, must-revalidate" }] },
    { "source": "/favicon.png", "headers": [{ "key": "Cache-Control", "value": "public, max-age=604800, immutable" }] }
  ]
}
```

---

## Key Technical Decisions

### NoDynamicFOV (TeaVM-safe)
- Uses `GameRenderer.setDisableDynamicFOV(boolean)` instead of reflection
- Hook added to `GameRenderer.tick()` to force `fovModifierHand=1.0f` every frame when enabled

### Timing Optimiser
- Stores setting only; core hook needed in `ChunkRenderDispatcher.runChunkUploads()` to respect limit

### Asset Conversion
- Armor: `textures/entity/equipment/humanoid/<mat>.png` → `textures/models/armor/<mat>_layer_1.png`
- Leggings: `textures/entity/equipment/humanoid_leggings/<mat>.png` → `textures/models/armor/<mat>_layer_2.png`
- Netherite: Layer 1 from modern, Layer 2 blank (no 1.14 equivalent)

### Fair Play Declaration
**40% aim assist explicitly declined** — documented in README with rationale:
- Combat cheat, violates fair-play guarantees
- High ban risk on competitive servers (heuristic aim detection)
- Design philosophy: QoL/visual only, never combat advantage

---

## Default Keybinds

| Key | Action |
|-----|--------|
| Right Shift | Client menu |
| C | Zoom (hold) |
| Left Alt | Freelook (hold) |
| V | Toggle Sprint |
| B | Toggle Sneak |
| E | HUD Editor (from menu) |

All module keybinds rebindable from menu (click chip → press key, right-click to clear).

---

## Directory Structure

```
/home/nixel_tide/projects/
├── eagler-pvp-client/          # Main workspace
│   ├── src/main/java/net/slate/
│   ├── desktopRuntime/
│   │   ├── filesystem/resourcePacks/run/modern-assets.epk
│   │   └── resources/assets/minecraft/shaders/
│   ├── modern-assets/          # Converted resource pack (pack_format 4)
│   └── MODERN_ASSETS.md        # Asset compatibility docs
├── modern-converter/
│   └── convert.py              # Pack_format 69 → 4 converter
├── modern-src/                 # CurseForge source (10,308 files)
└── deploy/
    ├── public/                 # Vercel deploy files
    └── vercel.json
```

---

## Known Issues / TODO

1. **Timing Optimiser** — Needs `ChunkRenderDispatcher` hook to enforce `maxChunkUploads`
2. **Smart Animations / Entity Culling** — Settings only, need render pipeline hooks
3. **Shader Pack** — Custom shader creation incomplete; uses built-in shaders for now
4. **OptionsScreen** — Difficulty/Performance inline; other categories launch sub-screens
5. **Desktop testing** — Xvfb unstable in CI; manual testing needed
6. **Netherite armor layer_2** — Blank; may look wrong on leggings in 3rd person
7. **Item models** — Modern display contexts may not map 1:1 to 1.14

---

## Vercel Project
- **Project:** `slate-client` (nixeltide-8187s-projects)
- **Production URL:** https://slate-client-glb1kxed6-nixeltide-8187s-projects.vercel.app
- **No aliases** (all custom domains removed per request)

---

## Git History
- All changes in `/home/nixel_tide/projects/eagler-pvp-client/`
- Converter script in `/home/nixel_tide/projects/modern-converter/`
- Deploy artifacts in `/home/nixel_tide/projects/deploy/public/`