# GUI & Screen Subsystem (`autismclient.gui.*`)

AUTISM Client renders its custom menus, editors, and HUD screens through a virtual-scaled (`autismclient.util.cA`) layout system built on top of MC 26.3's `Screen` and `GuiGraphicsExtractor` APIs. Phase 2 renamed `17` self-titled screens in `autismclient.gui.d` (`13/30` base/dynamic-title/widget files remain for Phases 3–4).

## Package Layout

| Current Package | Files (Obf/Tot) | LOC | Planned Target | Role |
|---|---:|---:|---|---|
| `autismclient.gui` | 3 / 3 | 508 | `autismclient.gui` | Theme-applying overlay, custom inventory screen, loading overlay |
| `autismclient.gui.c` | 2 / 2 | 1,029 | `autismclient.gui.profiles` | Profile / server library browser & server icon cache |
| `autismclient.gui.d` | 13 / 30 | 18,714 | `autismclient.gui.screens` | Full-screen GUI screens (`17` named `*Screen` + base/dynamic helpers) |
| `autismclient.gui.e` | 10 / 10 | 2,543 | `autismclient.gui.assets` | Custom UI icons & dynamic texture prewarmers |
| `autismclient.gui.f` (+ `.a/.b/.c/.d`) | 107 / 107 | 17,531 | `autismclient.gui.ui.*` | Widget, theme, typography, scrollbar, dropdown, and layout toolkit |
| `autismclient.gui.macro.editor` + `.a` | 6 / 11 | 19,672 | `autismclient.gui.macro.editor` | `ActionEditorOverlay`, `ActionFieldRegistry`, `ActionFieldSchema`, `FieldDef`, `RaceStepSelectorOverlay`, live capture |

## Frozen & ABI-Pinned GUI Symbols (see [ABI_AND_HAZARDS.md](../ABI_AND_HAZARDS.md))
- **Hard-frozen files:** `gui/a/DataUtil.java`, `gui/d/JsonUtil.java` (never inspected or modified).
- **Compile-time ABI-pinned enums:** `gui/f/b/CachePool$Inner_DataUtil` and `gui/f/b/l` (referenced directly by untouched `gui/d/JsonUtil.java`).

## Named Screens (`autismclient.gui.d`, `17` Phase 2 Screens)

| Class | Screen Title | Superclass | LOC | Role |
|---|---|---|---:|---|
| `AddonsScreen` | `"Addons"` | `KeyHolder` | 777 | Loaded addon list & management |
| `AiToolsScreen` | `"AI Tools"` | `Screen` | 384 | AI tool toggles & permissions |
| `AutismHudEditorScreen` | `"Autism HUD Editor"` | `Screen` | 616 | Drag-and-drop HUD element editor |
| `AutismModulesScreen` | `"Autism Modules"` | `KeyHolder` | 995 | Main categorized module browser & settings GUI |
| `AutismOverlaysScreen` | `"Autism Overlays"` | `Screen` | 214 | Overlay enablement & ordering |
| `CrosshairEditorScreen` | `"Crosshair Editor"` | `KeyHolder` | 1,264 | Custom crosshair shape/color editor |
| `ForceOpPreviewScreen` | `"ForceOp Preview"` | `KeyHolder` | 283 | Preview screen for ForceOp payloads |
| `HudElementSettingsScreen` | `"HUD Element Settings"` | `Screen` | 1,328 | Per-element HUD config (uses stock SDL image dialog for GIF/PNG picker) |
| `JoinMacroScreen` | `"Join Macro"` | `KeyHolder` | 809 | Server join macro list & `Password` launcher |
| `LoginPasswordScreen` | `"Login Password"` | `KeyHolder` | 308 | Single-account `{secret.password}` editor (`Show`/`Hide`, `SecureRandom` generator, `Clear`, `Save`, `Back`) |
| `PickItemScreen` | `"Pick Item"` | `KeyHolder` | 428 | Item registry picker |
| `PluginLibraryScreen` | `"Plugin Library"` | `KeyHolder` | 861 | Cached server plugin database browser |
| `ProxiesScreen` | `"Proxies"` | `KeyHolder` | 1,418 | SOCKS/HTTP proxy list & active selection |
| `ProxyConfigScreen` | `"Proxy Config"` | `KeyHolder` | 499 | Individual proxy add/edit form |
| `SimpleVoiceChatDetectedScreen` | `"Simple Voice Chat detected"` | `Screen` | 329 | Warning/notice screen for Simple Voice Chat compatibility |
| `ThemeColorScreen` | `"Theme Color"` | `Screen` | 1,303 | Client UI theme color & preset editor |
| `WaypointsScreen` | `"Waypoints"` | `KeyHolder` | 846 | Per-server waypoint manager |

## Remaining `gui/d` Base, Dynamic-Title & Widget Classes (`13`)
- **Base & Marker Types:** `KeyHolder` (`42` LOC, abstract virtual-scaled `Screen` base), `TaskQueue` (`7` LOC, custom menu marker interface), `ValueSink` (`32` LOC, UI palette color constants).
- **Widget Subclasses:** `StateCache` (`229` LOC, styled `EditBox`), `StreamGate` (`55` LOC, styled `Button`).
- **Dynamic-Title Editors & Dialogs:** `BufferHelper` (`541` LOC, `"Edit <mode>"`), `ConfigStore` (`872` LOC, `"Edit <mode>"`), `DataPipe` (`1,195` LOC, `"Edit <option>"` list editor), `NodeWalker` (`1,202` LOC, `"Select <option>"` selector), `PacketUtil` (`214` LOC, generic text/rename dialog).
- **Menu Screens:** `ListPool` (`126` LOC, pause menu), `RefQueue` (`216` LOC, vanilla-style title screen), `UtilCore` (`1,321` LOC, AUTISM main menu screen).

## Notes on `LoginPasswordScreen`
Replaced the removed multi-account secrets table (`0bbe8b5`) with a single-account password form backed by `util/JoinMacroController.java`:
- Masks input by default (`Show`/`Hide` toggle) and generates 9–16 char passwords via `SecureRandom`.
- Overrides `Screen.setInitialFocus()` so MC 26.3 keyboard navigation and window resize (`rebuildWidgets()`) keep focus on the password `EditBox`.
- Uses adaptive margins and width clamping so compact/high-scale virtual screens never clip or overlap widgets.
