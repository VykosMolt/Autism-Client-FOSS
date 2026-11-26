# GUI & Screen Subsystem (`autismclient.gui.*`)

AUTISM Client renders its custom menus, editors, and HUD screens through a virtual-scaled (`autismclient.util.cA`) layout system and custom SDF/MSDF vector renderer built on top of MC 26.3's `Screen` and `GuiGraphicsExtractor` APIs. Alongside `4` pre-existing readable classes in `gui.macro.editor`, Phase 2 (`17` screens), Phase 3A (`1` registry), Phase 4A (`34` classes), and Phase 4B (`97` toolkit classes) bring non-excluded GUI deobfuscation to **153 / 163** classes (`100%` of renameable GUI classes; the remaining `10` classes in `autismclient.gui.f.*` are compile-time ABI-pinned by untouched `gui/d/JsonUtil.java`).

## Package Layout (Post-Phase-4B, Non-Excluded)

| Current Package | Files (Obf/Tot) | LOC | Planned Target | Role |
|---|---:|---:|---|---|
| `autismclient.gui` | **0 / 3** | 508 | `autismclient.gui` | **Phase 4A complete:** `ThemeApplyProgressOverlay`, `AutismGhostInventoryScreen`, `AutismLoadingOverlay` |
| `autismclient.gui.c` | **0 / 2** | 1,029 | `autismclient.gui.profiles` | **Phase 4A complete:** `ProfilesLibraryView`, `ProfileServerIconCache` |
| `autismclient.gui.d` | **0 / 30** | 18,714 | `autismclient.gui.screens` | **Phase 2 + 4A complete:** `17` self-titled `*Screen` GUIs + `13` base/menu/editor/widget classes |
| `autismclient.gui.e` | **0 / 10** | 2,543 | `autismclient.gui.sdf` | **Phase 4A complete:** SDF/MSDF vector shape, icon, font atlas/text rendering & subpixel scissor stack |
| `autismclient.gui.f` (+ `.a/.b/.c/.d`) | 10 / 107 | 17,531 | `autismclient.gui.ui.*` | **Phase 4B complete:** `97` UI toolkit classes renamed; `10` compile-time **ABI-pinned** (`gui/d/JsonUtil.java`) |
| `autismclient.gui.macro.editor` + `.a` | **0 / 11** | 19,672 | `autismclient.gui.macro.editor` | **Phase 3A + 4A complete:** `8` editor core/schema classes + `3` live-capture helpers |
| **Total (non-excluded GUI)** | **10 / 163** | **59,997** | — | **153 named**, **10 ABI-pinned** |

## Frozen & ABI-Pinned GUI Symbols (see [ABI_AND_HAZARDS.md](../ABI_AND_HAZARDS.md))
- **Hard-frozen files:** `gui/a/DataUtil.java`, `gui/d/JsonUtil.java` (never inspected or modified).
- **Compile-time ABI-pinned classes (`10`, all directly referenced by untouched `gui/d/JsonUtil.java` and enforced in `AstBatchRenamer.FROZEN_SYMBOL_EXACT`):**
  - `gui/f/NetHelper` (`UiRect`), `gui/f/PacketUtil` (`UiPrimitives`), `gui/f/SyncManager` (`UiRenderContext`), `gui/f/UiHelper` (`UiContextFactory`)
  - `gui/f/a/DataUtil` (`UiAssetIds`)
  - `gui/f/b/BufferHelper` (`CardSurfaceRenderer`), `gui/f/b/CachePool` (`ButtonRenderer` + frozen inner enum `CachePool$Inner_DataUtil`), `gui/f/b/CharEncoder` (`UiStyleTheme`), `gui/f/b/k` (`FontDrawHelper`), `gui/f/b/l` (`TextTone` frozen enum)

## Top-Level Overlays & Profiles (`autismclient.gui`, `autismclient.gui.c`)
- **`autismclient.gui` (`3`):** `ThemeApplyProgressOverlay` (`Overlay`, `190` LOC), `AutismGhostInventoryScreen` (`InventoryScreen`, `105` LOC — restored exact name from `source-contracts.json`), `AutismLoadingOverlay` (`LoadingOverlay`, `213` LOC).
- **`autismclient.gui.c` (`2`):** `ProfilesLibraryView` (`894` LOC, profile/server library browser), `ProfileServerIconCache` (`135` LOC, async server icon cache).

## Full-Screen GUIs & Widgets (`autismclient.gui.d`, `30` Classes)

### Phase 2 Self-Titled Screens (`17`)

| Class | Screen Title | Superclass | LOC | Role |
|---|---|---|---:|---|
| `AddonsScreen` | `"Addons"` | `VirtualScaledScreen` | 777 | Loaded addon list & management |
| `AiToolsScreen` | `"AI Tools"` | `Screen` | 384 | AI tool toggles & permissions |
| `AutismHudEditorScreen` | `"Autism HUD Editor"` | `Screen` | 616 | Drag-and-drop HUD element editor |
| `AutismModulesScreen` | `"Autism Modules"` | `VirtualScaledScreen` | 995 | Main categorized module browser & settings GUI |
| `AutismOverlaysScreen` | `"Autism Overlays"` | `Screen` | 214 | Overlay enablement & ordering |
| `CrosshairEditorScreen` | `"Crosshair Editor"` | `VirtualScaledScreen` | 1,264 | Custom crosshair shape/color editor |
| `ForceOpPreviewScreen` | `"ForceOp Preview"` | `VirtualScaledScreen` | 283 | Preview screen for ForceOp payloads |
| `HudElementSettingsScreen` | `"HUD Element Settings"` | `Screen` | 1,328 | Per-element HUD config (uses stock SDL image dialog for GIF/PNG picker) |
| `JoinMacroScreen` | `"Join Macro"` | `VirtualScaledScreen` | 809 | Server join macro list & `Password` launcher |
| `LoginPasswordScreen` | `"Login Password"` | `VirtualScaledScreen` | 308 | Single-account `{secret.password}` editor (`Show`/`Hide`, `SecureRandom` generator, `Clear`, `Save`, `Back`) |
| `PickItemScreen` | `"Pick Item"` | `VirtualScaledScreen` | 428 | Item registry picker |
| `PluginLibraryScreen` | `"Plugin Library"` | `VirtualScaledScreen` | 861 | Cached server plugin database browser |
| `ProxiesScreen` | `"Proxies"` | `VirtualScaledScreen` | 1,418 | SOCKS/HTTP proxy list & active selection |
| `ProxyConfigScreen` | `"Proxy Config"` | `VirtualScaledScreen` | 499 | Individual proxy add/edit form |
| `SimpleVoiceChatDetectedScreen` | `"Simple Voice Chat detected"` | `Screen` | 329 | Warning/notice screen for Simple Voice Chat compatibility |
| `ThemeColorScreen` | `"Theme Color"` | `Screen` | 1,303 | Client UI theme color & preset editor |
| `WaypointsScreen` | `"Waypoints"` | `VirtualScaledScreen` | 846 | Per-server waypoint manager |

### Phase 4A Base, Menu, Editor & Widget Classes (`13`)
- **Base & Marker Types:** `VirtualScaledScreen` (`Screen`, `42` LOC, abstract virtual-scaled screen base), `ScreenButtonBoundsProvider` (`interface`, `7` LOC, custom menu button-bounds provider), `GuiColorPalette` (`32` LOC, UI palette color constants).
- **Widget Subclasses:** `MsdfEditBox` (`EditBox`, `229` LOC, MSDF-rendered text input), `StyledButton` (`Button`, `55` LOC, custom-styled button).
- **Dynamic-Title Editors & Dialogs:** `NbtTextEditorScreen` (`VirtualScaledScreen`, `541` LOC), `EnchantmentAttributeEditorScreen` (`VirtualScaledScreen`, `872` LOC), `StringListEditorScreen` (`VirtualScaledScreen`, `1,195` LOC), `RegistrySelectorScreen` (`VirtualScaledScreen`, `1,202` LOC), `PromptDialogScreen` (`VirtualScaledScreen`, `214` LOC).
- **Menu Screens:** `StealthPauseScreen` (`Screen`, `126` LOC, stealth pause menu), `StealthTitleScreen` (`Screen`, `216` LOC, vanilla-style stealth title screen), `AutismTitleScreen` (`Screen`, `1,321` LOC, AUTISM main menu screen).

## SDF/MSDF Vector Rendering Engine (`autismclient.gui.e`, `10` Classes)
- **Pipelines & Quad State:** `SdfRenderPipelines` (`66` LOC), `TexturedQuadRenderState` (`record`, `134` LOC).
- **Vector Shapes & Icons:** `SdfShapeRenderer` (`551` LOC), `SdfIconRenderer` (`838` LOC), `SteppedRowContour` (`157` LOC).
- **MSDF Typography:** `MsdfFontAtlas` (`340` LOC), `MsdfFontFace` (`enum`, `68` LOC), `MsdfTextRenderer` (`253` LOC).
- **Scissor / Clipping:** `UiScissorStack` (`50` LOC), `SubpixelScissorTracker` (`86` LOC).

## Macro Action Editor & Live Capture (`autismclient.gui.macro.editor` + `.a`, `11` Classes)
- **`autismclient.gui.macro.editor` (`8`):** `ActionEditorOverlay` (`16,349` LOC), `ActionFieldRegistry` (`1,846` LOC), `ActionFieldSchema` (`296` LOC), `ToolsActionSchemaBuilder` (`157` LOC), `FieldDef` (`138` LOC), `FieldType` (`enum`, `16` LOC), `CaptureMode` (`enum`, `14` LOC), `RaceStepSelectorOverlay` (`562` LOC).
- **`autismclient.gui.macro.editor.a` (`3`):** `EditorListRowHelper` (`143` LOC), `CaptureButtonRenderer` (`100` LOC), `ActiveCaptureSession` (`51` LOC).

## Custom UI Toolkit (`autismclient.gui.f.*`, `107` Classes: `97` Named + `10` ABI-Pinned)
Full per-class mapping rows live in [DEOBFUSCATION_LEDGER.tsv](../DEOBFUSCATION_LEDGER.tsv) (`Phase4B`):
- **`autismclient.gui.f` (`4 / 24` obf, `1,643` LOC):** Core theme, metrics, animation, clipping, alpha, and layer routing (`UiTheme`, `UiColorScheme`, `UiDensityMetrics`, `UiSpacingMetrics`, `UiTypographyMetrics`, `UiTextRenderer`, `TextWrapLayout`, `UiClipStack`, `UiAlphaStack`, `UiLayer`, `UiLayerComponent`, `UiLayerHost`, `UiLayerInputRouter`, `UiInputResult`, `ForegroundScreenSlot`, `RetainedForegroundScreen`, `EaseOutAnimator`, `HoverTransitionCache`, `ToggleTransitionCache`, `VisibilityTransition`; ABI-pinned: `NetHelper`, `PacketUtil`, `SyncManager`, `UiHelper`).
- **`autismclient.gui.f.a` (`1 / 1` obf, `36` LOC):** UI asset resource IDs (ABI-pinned: `DataUtil`).
- **`autismclient.gui.f.b` (`5 / 53` obf, `8,051` LOC):** Immediate-mode widget renderers, scrollbars, popups, dropdowns, sliders, text inputs, toasts, tooltips, window/dialog frames, and splitters (`WidgetRenderFacade`, `ButtonDrawHelper`, `AnimatedToggleButtonRenderer`, `ConnectedButtonRenderer`, `IconButtonRenderer`, `KeybindButtonRenderer`, `OverlayActionButton`, `SimpleClickButton`, `ToggleSwitchRenderer`, `SliderRenderer`, `RangeSliderRenderer`, `ProgressBarRenderer`, `TextFieldRenderer`, `TextInputWidget`, `FocusableInput`, `Utf16TextCursor`, `DropdownWidget`, `DropdownMenu`, `ColorPickerPopup`, `SearchFilterList`, `ListRowRenderer`, `ScrollListLayout`, `ScrollOffsetController`, `ScrollbarRenderer`, `ScrollbarMetricsHelper`, `ScrollbarContentMath`, `SectionCardRenderer`, `GroupHeaderRenderer`, `TabStripRenderer`, `TitleBarRenderer`, `WindowFrameRenderer`, `WindowHeaderHelper`, `DialogFrameRenderer`, `OverlayFrameHelper`, `OverlayWindowLayerAdapter`, `PanelSurfaceHelper`, `PillBadgeRenderer`, `GlyphIndicatorRenderer`, `WorldIndicatorBadge`, `StatusToastCardRenderer`, `ToastNotificationStack`, `TooltipRenderer`, `ResizeGripRenderer`, `CrosshairPreviewRenderer`, `GridRowSplitter`, `RectSplitHelper`, `CellSplitHelper`, `UiLayoutMath`; ABI-pinned: `BufferHelper`, `CachePool`, `CharEncoder`, `k`, `l`).
- **`autismclient.gui.f.c` (`0 / 24` obf, `3,376` LOC):** Declarative layout tree nodes, containers & list/card helpers (`LayoutNode`, `LayoutContainer`, `LayoutContext`, `LayoutTreeHost`, `LayoutViewport`, `RowLayout`, `ColumnLayout`, `StretchRowLayout`, `SurfaceBox`, `EdgeInsets`, `PanelLayoutMath`, `CollapsibleWindowNode`, `CollapsibleSectionNode`, `ScrollableListView`, `SelectableListBox`, `ButtonNode`, `LabelNode`, `IconHeaderNode`, `InfoCardRenderer`, `KeyValueLabelNode`, `KeyValueRowNode`, `LabeledControlRow`, `FixedSpacerNode`, `VerticalSpacerNode`).
- **`autismclient.gui.f.d` (`0 / 5` obf, `4,425` LOC):** Module-menu & settings controllers (`ModulesMenuController`, `ModuleSettingsView`, `MacroPickerPopup`, `CrosshairPreviewLayout`, `InlineTextEditState`).

## Notes on `LoginPasswordScreen`
Replaced the removed multi-account secrets table (`0bbe8b5`) with a single-account password form backed by `util/JoinMacroController.java`:
- Masks input by default (`Show`/`Hide` toggle) and generates 9–16 char passwords via `SecureRandom`.
- Overrides `Screen.setInitialFocus()` so MC 26.3 keyboard navigation and window resize (`rebuildWidgets()`) keep focus on the password `EditBox`.
- Uses adaptive margins and width clamping so compact/high-scale virtual screens never clip or overlap widgets.
