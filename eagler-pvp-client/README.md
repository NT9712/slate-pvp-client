# Slate — InfinityMC

A small, focused PvP client built into the Eaglercraft 1.14.4 workspace, branded for
**InfinityMC** (`wss://infinitymc.oops.wtf`).

**Right Shift** opens the client menu.

## Title screen

The vanilla title screen has been replaced with a single-purpose one:

* The Minecraft logo, splash text and edition strip are gone, replaced by an InfinityMC
  wordmark (red/blue) over a dimmed panorama.
* **Play** connects straight to `wss://infinitymc.oops.wtf`.
* **Options...** is kept, because video, controls, sound and language settings still need a home.
* Removed: Singleplayer, Multiplayer, Credits, Edit Profile, the language button, the friends
  button and the update banner.
* Buttons are flat and use the client's own palette rather than the vanilla stone texture.

The server is also registered as the default entry in the server list, shown as red/blue bold
`InfinityMC`, for both the desktop runtime and the browser build.

Removing Singleplayer means saved worlds are no longer reachable from the UI. The world data
itself is untouched, so restoring the button restores access.

Slate lives entirely in `src/main/java/net/slate` plus a short list of small, clearly marked
hooks in the vanilla classes. See [EAGLERCRAFTX_README.md](EAGLERCRAFTX_README.md) for the
original workspace documentation.

---

## Building

Java 17 or greater is required. Import the whole folder into your IDE as a Gradle project.

| Target | Command |
| --- | --- |
| JavaScript client + offline download | `./gradlew target_teavm_javascript:makeMainOfflineDownload` |
| WASM-GC client | `./gradlew target_teavm_wasm_gc:makeMainWasmClientBundle` |
| Desktop debug runtime | `./gradlew target_lwjgl_desktop:eaglercraftDebugRuntime` |

The desktop runtime is for debugging only. Always test the browser build before releasing.

### Build fixes that were needed to get here

The workspace as cloned could not build either browser target. Four separate defects, all
predating this client, were fixed:

* `target_teavm_javascript` compiled `rootProject`, but the root project's source set is
  missing the `platform-api` sources it declares, so it failed with ~200 errors. The JS target
  now compiles `../src/main/java` directly, exactly like the WASM-GC target already did.
* `target_teavm_javascript` pointed `mainClass` at `net.lax1dude.eaglercraft.v1_8.internal.teavm.MainClass`;
  this fork's class is `net.lax1dude.eaglercraft.internal.teavm.MainClass`.
* `src/teavm` had drifted from `src/main`: `PlatformWebRTC` imported a `log4j` package that does
  not exist here, `PlatformRuntime` used `FixWebMDurationJS` without importing it,
  `TeaVMClientConfigAdapter.getRelays()` carried an `@Override` for a method that is not on the
  interface, and `PlatformOpenGL` was missing `_wglDrawRangeElements`.
* `target_teavm_wasm_gc` had no task to compile `src/wasm-gc-teavm-bootstrap`, so
  `makeMainWasmClientBundle` could never find `javascript_dist/bootstrap.js`. A
  `compileWasmBootstrap` task now produces it.

With those in place the WASM-GC target compiles the whole client, including Slate, through
TeaVM. The plain JavaScript target still fails inside TeaVM 0.9.2 because the vanilla 1.14 code
in this fork uses `ThreadLocal.withInitial`, `CopyOnWriteArrayList` and `WeakHashMap`, which that
TeaVM version's class library does not implement. That is a property of the fork, not of Slate,
and it needs a TeaVM upgrade to resolve.

### Changes to existing options

`Show FPS` and `Show XYZ` have been removed from Video Settings and from `options.txt`. Their
overlay was a fixed two line block in the top left with no configuration; the **FPS** and
**Coordinates** modules replace it and can be moved, scaled and styled. Old `showFps` /
`showXYZ` lines in an existing `options.txt` are ignored and dropped on the next save.

The singleplayer server timing readout now only draws while the F3 debug screen is open,
instead of permanently occupying the top right corner where the client HUD lives.

---

## What is in the client

25 modules across six categories. Everything is off or on from one menu, everything with a
position can be dragged in the HUD editor, and everything persists.

### Combat
| Module | What it does |
| --- | --- |
| Crosshair | Replaces the vanilla crosshair. Cross, dot, cross+dot or circle, with size, gap, thickness, colour, outline and a dynamic mode that opens the gap while you move or while your attack is on cooldown. |
| Hit Marker | Marks the crosshair when you land a hit. Duration, size, colour, fade. |
| Target | Name, animated health bar and distance for whatever you are aiming at; stays up for three seconds so it does not flicker. |
| Combo | Hits landed in a row on the same target. |
| Reach | Distance of your last hit, measured to the hit position. |

### HUD
| Module | What it does |
| --- | --- |
| FPS | Frames per second, optional colour thresholds. |
| CPS | Left and right clicks per second, separately labelled. |
| Ping | Round trip time from the player list, optional colour thresholds. |
| Coordinates | Inline or stacked, optional decimals, facing and nether conversion. |
| Keystrokes | WASD block with optional mouse buttons and space bar, animated presses. |
| Durability | Armour and held item. Full gear shows only icons; a piece that is wearing out gets a percentage that turns amber then red. |
| Effects | Active potion effects with amplifier and a countdown that warns as it runs out. Replaces the vanilla icon row while it is on. |

### Visual
| Module | What it does |
| --- | --- |
| Zoom | Hold to zoom, smooth, adjustable level, scroll to change it while zoomed, mouse movement is scaled down to match. |
| Hurt Cam | Scales down the camera shake you get when damaged. |
| Low Fire | Lowers the first person fire overlay so you can see while burning. |
| Full Bright | Raises the light map beyond the vanilla brightness slider's ceiling. Allowed on every major PvP server, but it is doing something the vanilla slider cannot, so it is named plainly here. Your saved brightness setting is never written to. |
| Clean Scoreboard | Hides the red numbers and, optionally, the background of the sidebar. |

### Player
| Module | What it does |
| --- | --- |
| Toggle Sprint | Toggle or always-on sprint. |
| Toggle Sneak | Sneak stays on until you press again; optionally cancels on jump. |
| Freelook | Hold to look around without turning. Third person or front view, optional crosshair hiding. A small number of strict servers class perspective mods as free-cam; check the rules where you play. |
| No Dynamic FOV | Disables the FOV change when sprinting for a consistent view. |

### Performance
| Module | What it does |
| --- | --- |
| Power Saver | Caps the frame rate when the window is unfocused or a menu is open. Matters a lot in a browser tab. |
| Particle Limit | Caps how many particles can be alive at once. |
| Timing Optimiser | Caps chunk uploads per frame for smoother frame pacing. |

### Misc
| Module | What it does |
| --- | --- |
| Chat | Longer history, duplicate messages stack with a counter, and an unsent message is kept when you close the chat. |
| Tab List | Exact ping in milliseconds instead of bars, optional header/footer hiding. |
| Notifications | Small toast when something is toggled. |

---

## Fair play

Slate does not automate anything, does not touch a single packet, and shows no information the
vanilla client does not already have. There is no autoclicker, no reach extension, no ESP and no
velocity module, and nothing here changes what the server sees.

Two modules are worth knowing about before you join a strict server: **Freelook** decouples the
camera from your body, and **Full Bright** raises the light map past the vanilla brightness
slider. Both are allowed on every major PvP server and neither is detectable, but a handful of
competitive servers ban them by rule.

### Declined: 40% Aim Assist

**40% aim assist is explicitly declined and will never be implemented.**

Rationale:
* Aim assist is a combat cheat that artificially improves targeting accuracy.
* It violates the fair-play guarantees this client is built on: no automation, no packet manipulation, no information the vanilla client doesn't have.
* Using aim assist on competitive PvP servers carries a high ban risk (anti-cheat detection via heuristic analysis of aim patterns).
* Slate's design philosophy: every module must be a legitimate QoL or visual enhancement, never a combat advantage that replaces player skill.

If you want aim assist, this is not the client for you.

## Default keys

| Key | Action |
| --- | --- |
| Right Shift | Client menu |
| C | Zoom (hold) |
| Left Alt | Freelook (hold) |
| V | Toggle Sprint |
| B | Toggle Sneak |

Every module keybind is rebindable from its row in the menu. Open a module, click the
keybind chip and press a key; right click the chip to clear it.

---

## Menu

* Left rail selects a category, the list shows that category's modules.
* Click a module name to expand its settings, click the toggle to turn it on and off.
* Type in the search box to search every category at once.
* Right click any setting to put it back to its default.
* Arrow keys move, Enter toggles, `E` opens the HUD editor, Right Shift closes.
* The menu key itself is rebindable from the chip at the bottom of the left rail.

## HUD editor

Opened with `E` from the menu.

* Drag elements to move them; they snap to screen edges and the centre.
* Scroll over an element to resize it, arrow keys nudge by a pixel.
* Hidden elements stay on screen as ghosts; right click any element to show or hide it.
* `B` toggles element backgrounds, `R` resets every element's scale.

Positions are stored as an offset from whichever edge you dropped the element against, so a
resolution or GUI scale change keeps the layout intact.

---

## Where the code is

```
net/slate/
  Slate.java            entry point: init, tick, key events, HUD hook
  ClientEvents.java     shared combat state (CPS, combo, reach, hit and hurt times)
  Config.java           key=value persistence through EagRuntime storage
  module/               Module, HudModule, ModuleManager, Category, settings
  module/impl/          the 25 modules
  hud/                  HudManager (layout + draw), HudDraw (HUD typography)
  ui/                   Theme, Draw, ClickGuiScreen, HudEditorScreen, ColorPicker, Notifications
```

Hooks in vanilla classes, all one or two lines and all delegating to a static method:

| File | Hook |
| --- | --- |
| `Minecraft` | `Slate.init()`, `Slate.onTick()`, attack notification, client frame limit |
| `KeyboardListener` | key state tracking and the key-down edge |
| `MouseHelper` | click counting, zoom mouse scaling, freelook look capture |
| `IngameGui` | HUD draw, custom crosshair, hit marker, toasts, clean scoreboard |
| `GameRenderer` | zoom FOV, hurt camera scale |
| `ActiveRenderInfo` | freelook camera offsets |
| `FirstPersonRenderer` | low fire offset |
| `ClientPlayerEntity`, `MovementInputFromOptions` | toggle sprint and sneak |
| `NewChatGui`, `ChatScreen` | chat history, duplicate stacking, saved draft |
| `PlayerTabOverlayGui` | numeric ping |
| `ParticleManager` | particle limit and explosion culling |
| `LightTexture` | full bright |

Everything is TeaVM safe: no reflection, no threads, no file IO, `HString.format` instead of
`String.format`, and `EagRuntime.steadyTimeMillis()` instead of the system clock.
