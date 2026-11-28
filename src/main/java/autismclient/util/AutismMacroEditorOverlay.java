package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.api.macro.MacroActionEntry;
import autismclient.api.macro.MacroActionRegistry;
import autismclient.api.macro.MacroPresetRegistry;
import autismclient.compat263.Compat263;
import autismclient.compat263.CompatGLFW;
import autismclient.gui.ui.widgets.SearchFilterList;
import autismclient.gui.ui.widgets.TooltipRenderer;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.macro.editor.ActionEditorOverlay;
import autismclient.util.macro.MacroAction;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.DynamicBindingManager;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class AutismMacroEditorOverlay extends DraggableOverlayWindow {
   private static final Minecraft MC = Minecraft.getInstance();
   private static AutismMacroEditorOverlay instance;
   private static final int flush_I = 30;
   private static final int close_I = 12;
   private static final int open_I = 10;
   private static final int poll_I = 3;
   private static final float init_F_2 = 3.0F;
   private static final int push_I = 3;
   private static final float MAX_LIST_SCREEN_FRACTION = 0.6F;
   private static final int pop_I = 110;
   private final Font font;
   private final autismclient.gui.f.b.CharEncoder styleTheme = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.layout.CollapsibleWindowNode windowNode = new autismclient.gui.ui.layout.CollapsibleWindowNode("Macro Editor");
   private final autismclient.gui.ui.layout.LayoutTreeHost layoutHost = new autismclient.gui.ui.layout.LayoutTreeHost(this.styleTheme, this.windowNode);
   private final autismclient.gui.ui.layout.VerticalSpacerNode bodySpacer = new autismclient.gui.ui.layout.VerticalSpacerNode();
   private boolean visible = false;
   private AutismMacro macro;
   private AutismMacro closingGhostMacro;
   private boolean isNewMacro = false;
   private boolean reopenMacroListOnClose = false;
   private AutismMacroEditorOverlay.EditorMode editorMode = AutismMacroEditorOverlay.EditorMode.NORMAL;
   private Consumer<AutismMacro> onSaveComplete = null;
   private int editorX;
   private int editorY;
   private int editorWidth = 258;
   private int editorHeight = 308;
   private List<String> buttonLabels = new ArrayList<>();
   private List<int[]> buttonBounds = new ArrayList<>();
   private List<Runnable> buttonActions = new ArrayList<>();
   private List<Runnable> buttonSecondaryActions = new ArrayList<>();
   private List<Boolean> buttonEnabled = new ArrayList<>();
   private int runButtonIndex = -1;
   private final List<autismclient.gui.ui.widgets.DropdownWidget> dropdowns = new ArrayList<>();
   private ThemedTextInputBox nameField;
   private ThemedTextInputBox loopCountField;
   private boolean listeningForKeybind = false;
   private int loopMode = 0;
   private int listScroll = 0;
   private static final float read_F = 3.0F;
   private static final long DOUBLE_CLICK_MS = 350L;
   private static final int reset_I = 18;
   private static final int start = 12;
   private int draggingRowIndex = -1;
   private int pressedRowIndex = -1;
   private int dragTargetIndex = -1;
   private int dragGrabOffsetY = 0;
   private int dragMouseY = 0;
   private double pressX = 0.0;
   private double pressY = 0.0;
   private boolean pressedRowWasSelected = false;
   private boolean pressCanDrag = false;
   private int lastClickedStableId = -1;
   private long lastClickTime = 0L;
   private boolean headerPressed = false;
   private double dragOffsetX = 0.0;
   private double dragOffsetY = 0.0;
   private double headerPressX = 0.0;
   private double headerPressY = 0.0;
   private int headerPressEditorX = 0;
   private int headerPressEditorY = 0;
   private boolean headerMoved = false;
   private boolean collapsed = false;
   private static final int state = 8;
   private static final int queue = 14;
   private String unusedText = "";
   private List<String> unusedLines = null;
   private AutismMacroEditorOverlay.PaletteSectionKind paletteMode = null;
   private List<AutismMacroEditorOverlay.PaletteCategory> paletteCategories = Collections.emptyList();
   private List<AutismMacroEditorOverlay.PaletteEntry> filteredPaletteEntries = Collections.emptyList();
   private Map<String, List<AutismMacroEditorOverlay.PaletteEntry>> paletteEntriesByCategory = Collections.emptyMap();
   private final SearchFilterList<AutismMacroEditorOverlay.PaletteEntry> paletteSearch = new SearchFilterList<>(entry -> entry.categoryId + "\n" + entry.label + "\n" + entry.description);
   private ThemedTextInputBox paletteSearchField;
   private final AutismMacroEditorOverlay.OverlayAdapter paletteOverlay = new AutismMacroEditorOverlay.OverlayAdapter();
   private int pickerScroll = 0;
   private final HashMap<String, Integer> labelWidthCache = new HashMap<>();
   private long paletteRevision = 0L;
   private long cachedLayoutRevision = -1L;
   private int cachedPickerScreenW = -1;
   private int cachedPickerScreenH = -1;
   private int cachedPickerViewportWidth = -1;
   private int scrollbarDragMode = 0;
   private int scrollbarGrabOffset = 0;
   private final OverlayContextMenu<Integer> contextMenu;
   private static final int count = 0;
   private static final int flags = 1;
   private static final int level = 2;
   private static final float write_F = 0.14F;
   private static final float update_F = 0.14F;
   private static final float sync_F = 0.12F;
   private static final int frame = 18;
   private static final int block = 20;
   private String hoverTooltip = null;
   private int tooltipX;
   private int tooltipY;
   private final Deque<AutismMacroEditorOverlay.UndoSnapshot> undoStack = new ArrayDeque<>();
   private final Deque<AutismMacroEditorOverlay.UndoSnapshot> redoStack = new ArrayDeque<>();
   private AutismMacro originalMacro;
   private boolean tempSavedForRun = false;
   private boolean tempSavedAsNew = false;
   private CompoundTag preRunBackup = null;
   private boolean stepListLocked = false;
   private boolean configurationOnly = false;
   private long oneShotRunId = -1L;
   private long runStartedAt = 0L;
   private static final long STEP_LIST_LOCK_DELAY_MS = 100L;
   private long lastRunStateRevision = -9223372036854775808L;
   private long lastRunStateRunId = -9223372036854775808L;
   private boolean running = false;
   private boolean rebuildPending = false;
   private final IdentityHashMap<MacroAction, Integer> stableIds = new IdentityHashMap<>();
   private final AutismMacroEditorOverlay.RowSelectionSet selection = new AutismMacroEditorOverlay.RowSelectionSet();
   private Integer selectionAnchorId;
   private final List<AutismMacroEditorOverlay.ActionRowItem> rowSnapshots = new ArrayList<>();
   private final List<AutismMacroEditorOverlay.RowTransitionAnim> rowAnimations = new ArrayList<>();
   private int nextStableId = 1;
   private long lastAnimationTick = System.nanoTime();
   private static final int drain = -5609729;
   private static final int fill = 88;

   public static AutismMacroEditorOverlay getInstance() {
      if (instance == null) {
         instance = new AutismMacroEditorOverlay(Minecraft.getInstance().font);
         instance.loadBounds();
         OverlayManager.getInstance().register(instance);
      }

      instance.loadSharedPosition();
      return instance;
   }

   public void setConfigurationOnly(boolean configurationOnly) {
      if (this.configurationOnly != configurationOnly) {
         this.configurationOnly = configurationOnly;
         ActionEditorOverlay actionEditor = ActionEditorOverlay.getExisting();
         if (actionEditor != null) {
            actionEditor.setConfigurationOnly(configurationOnly);
            actionEditor.setCaptureAllowed(this.isCaptureAllowed());
         }

         if (this.visible) {
            this.rebuildButtons();
         }
      }
   }

   private autismclient.gui.ui.widgets.OverlayActionButton createActionButton(
      int x, int y, int w, int h, String label, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant, boolean enabled, Runnable action
   ) {
      return this.createActionButton(x, y, w, h, label, variant, enabled, action, null);
   }

   private autismclient.gui.ui.widgets.OverlayActionButton createActionButton(
      int x,
      int y,
      int w,
      int h,
      String label,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      boolean enabled,
      Runnable action,
      Runnable secondaryAction
   ) {
      autismclient.gui.ui.widgets.OverlayActionButton button = autismclient.gui.ui.widgets.OverlayActionButton.create(x, y, w, h, Component.literal(label), ignored -> {
         if (action != null) {
            action.run();
         }
      }, secondaryAction == null ? null : ignored -> secondaryAction.run());
      button.setWidth(w);
      button.setTone(variant);
      button.active = enabled;
      return button;
   }

   private void renderToneButton(
      GuiGraphicsExtractor context,
      int x,
      int y,
      int w,
      int h,
      String label,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      boolean enabled,
      int mouseX,
      int mouseY
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, this.font, x, y, w, h, label, variant, enabled, mouseX, mouseY);
   }

   private void renderToggleButton(
      GuiGraphicsExtractor context, int x, int y, int w, int h, String label, boolean toggleState, String animationKey, boolean enabled, int mouseX, int mouseY
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, this.font, x, y, w, h, label, toggleState, animationKey, enabled, mouseX, mouseY);
   }

   private void renderRunButton(GuiGraphicsExtractor context, int x, int y, int w, int h, boolean running, boolean enabled, int mouseX, int mouseY) {
      boolean hovered = enabled && mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
      autismclient.gui.f.b.CachePool.init(
         autismclient.gui.f.UiHelper.init(context, this.font, mouseX, mouseY),
         autismclient.gui.f.NetHelper.init(x, y, w, h),
         "",
         running ? autismclient.gui.f.b.CachePool.Inner_DataUtil.update : autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
         hovered,
         true
      );
      int icon = 8;
      GuiIconAtlas.draw(context, running ? GuiIconAtlas.STOP : GuiIconAtlas.PLAY, x + (w - icon) / 2, y + (h - icon) / 2, icon, enabled ? -218103809 : 1728053247);
   }

   private void renderIconButton(
      GuiGraphicsExtractor context,
      int x,
      int y,
      int w,
      int h,
      String label,
      Identifier iconId,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      boolean enabled,
      int mouseX,
      int mouseY
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, this.font, x, y, w, h, "", variant, enabled, mouseX, mouseY);
      int icon = 8;
      int gap = 3;
      int textW = autismclient.gui.f.b.k.init(
         this.font, label, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), this.styleTheme.load(autismclient.gui.f.b.l.read)
      );
      int groupW = icon + gap + textW;
      int startX = x + Math.max(2, (w - groupW) / 2);
      GuiIconAtlas.draw(context, iconId, startX, y + (h - icon) / 2, icon, enabled ? -218103809 : 1728053247);
      autismclient.gui.f.b.k.init(
         context,
         this.font,
         label,
         this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
         enabled ? this.styleTheme.load(autismclient.gui.f.b.l.read) : this.styleTheme.load(autismclient.gui.f.b.l.write),
         startX + icon + gap,
         UiLayoutMath.centerTextY(y, h, this.styleTheme.init_I_l(autismclient.gui.f.b.l.read), this.styleTheme.flush()),
         false
      );
   }

   public AutismMacroEditorOverlay(Font textRenderer) {
      this.font = textRenderer;
      this.contextMenu = new OverlayContextMenu<>(this.styleTheme, textRenderer, this::contextMenuItems, 12);
      this.setActiveContextMenu(this.contextMenu);
      this.editorWidth = this.minWindowWidth();
      this.editorHeight = this.initialWindowHeight();
      this.windowNode.setCenterTitle(false);
      this.windowNode.setTitleTone(autismclient.gui.f.b.l.load);
      this.windowNode.setHeaderButtons(true, true);
      this.windowNode.setTitleInsets(this.titleInsetLeft(), this.titleInsetRight());
      this.windowNode.body().withGap(0).withPadding(autismclient.gui.ui.layout.EdgeInsets.ZERO);
      this.windowNode.body().addChild(this.bodySpacer);
   }

   private int builderLabelY() {
      return this.builderTopY() + this.builderLabelOffset();
   }

   private int builderButtonY() {
      return this.builderLabelY() + this.styleTheme.init_I_l_I(autismclient.gui.f.b.l.load, 1) + this.builderLabelGap();
   }

   private int listLabelY() {
      return this.builderButtonY() + this.buttonHeight() + this.listLabelOffset();
   }

   private int listTopY() {
      return this.listLabelY() + this.styleTheme.init_I_l_I(autismclient.gui.f.b.l.load, 1) + 1;
   }

   private int contentX() {
      return this.editorX + this.contentInset();
   }

   private int contentWidth() {
      return this.editorWidth - this.contentInset() * 2;
   }

   private int listClipLeft() {
      return this.contentX() + 1;
   }

   private int rowButtonCount(MacroAction action) {
      return this.rowButtonCount(action, true, true, true);
   }

   private int rowButtonCount(MacroAction action, boolean allowDelete) {
      return this.rowButtonCount(action, true, true, allowDelete);
   }

   private int rowButtonCount(MacroAction action, boolean allowMove, boolean allowDuplicate, boolean allowDelete) {
      int count = 0;
      if (allowMove) {
         count += 2;
      }

      if (allowDuplicate) {
         count++;
      }

      if (action != null && this.isEditable(action)) {
         count++;
      }

      if (allowDelete) {
         count++;
      }

      return count <= 0 ? 0 : count * this.rowButtonSize() + (count - 1) * this.rowButtonGap();
   }

   private int rowButtonsX(MacroAction action) {
      return this.rowButtonsRight() - this.rowButtonCount(action) - 1;
   }

   private int rowButtonsX(MacroAction action, boolean allowDelete) {
      return this.rowButtonsRight() - this.rowButtonCount(action, allowDelete) - 1;
   }

   private int rowButtonsX(MacroAction action, boolean allowMove, boolean allowDuplicate, boolean allowDelete) {
      int width = this.rowButtonCount(action, allowMove, allowDuplicate, allowDelete);
      return width <= 0 ? this.rowButtonsRight() : this.rowButtonsRight() - width - 1;
   }

   private int listBottomY() {
      return this.listTopY() + this.listAreaHeight() + this.listBottomGap();
   }

   private int listAreaHeight() {
      return Math.max(0, this.listViewportHeight() + 2);
   }

   private int listViewportHeight() {
      int rowH = this.rowHeight();
      int rows = this.macro == null ? 0 : this.macro.actions.size();
      int autoList = rowH * Math.max(3, Math.min(rows, this.maxVisibleRows()));
      if (!this.hasCustomStoredSize()) {
         return autoList;
      }

      int height = this.resolveWindowHeight(this.chromeHeight() + autoList, this.getResizeMinHeight());
      return Math.max(rowH * 3, height - this.chromeHeight());
   }

   private int chromeHeight() {
      return this.listTopY() - this.editorY + 2 + this.listBottomGap() + this.buttonHeight() + this.bottomPadding();
   }

   @Override
   public boolean isResizable() {
      return true;
   }

   @Override
   protected int getResizeMinHeight() {
      return this.chromeHeight() + this.rowHeight() * 3;
   }

   private int preferredWindowWidth() {
      int content = this.preferredContentWidth();
      return this.resolveWindowWidth(content, content);
   }

   private int maxVisibleRows() {
      int screenH = MC.getWindow() != null ? cA.load_I() : 360;
      int chrome = this.chromeHeight();
      int cap = Math.min((int)(screenH * 0.6F), screenH - 8 - chrome);
      return Math.max(3, cap / Math.max(1, this.rowHeight()));
   }

   private int preferredWindowHeight() {
      return this.listBottomY() + this.buttonHeight() + this.bottomPadding() - this.editorY;
   }

   private int preferredContentWidth() {
      if (this.font == null) {
         return 240;
      }

      int gap = this.toolbarGap();
      int topRow = this.buttonWidth("Press Key/Mouse...") + gap + this.runButtonWidth() + gap + this.loopButtonWidth();
      if (this.loopMode == 1) {
         topRow += this.loopCountFieldWidth() + gap;
      }

      if (!this.configurationOnly && !this.isNewMacro && this.macro != null && LanSyncManager.getInstance().isInSession()) {
         topRow += this.lanButtonWidth() + gap;
      }

      int builderRow = this.paletteButtonWidth("Add Action") + this.paletteButtonWidth("Add Conditional") + this.textButtonWidth("Presets") + this.builderWidthGap() * 2;
      int footerRow = this.textButtonWidth("Save")
         + this.textButtonWidth("Cancel")
         + this.textButtonWidth("Undo")
         + this.textButtonWidth("Redo")
         + this.footerWidthGap() * 3
         + 4
         + autismclient.gui.f.b.k.init(this.font, "Name Required", this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), 0);
      int stepRow = 140 + this.rowButtonCount(null, true, true, true) + this.rowButtonStride() + 1 + this.rowRightGutter() + 2;
      int title = autismclient.gui.f.b.k.init(
            this.font, "Create Macro [RUN] (000 steps)", this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load), 0
         )
         + this.titleInsetLeft()
         + this.titleInsetRight()
         - this.contentInset() * 2;
      int content = Math.max(Math.max(topRow, builderRow), Math.max(Math.max(footerRow, stepRow), title));
      return content + this.contentInset() * 2;
   }

   private int paletteButtonWidth(String label) {
      String shown = label.startsWith("Add ") ? label.substring(4) : label;
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(this.font, shown, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), 0) + 11
      );
   }

   private int buttonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(
            this.font,
            label,
            this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
            this.styleTheme.load(autismclient.gui.f.b.l.read)
         )
      );
   }

   private int loopButtonWidth() {
      int loopTextW = Math.max(
         autismclient.gui.f.b.k.init(this.font, "Loop Inf", this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), 0),
         autismclient.gui.f.b.k.init(this.font, "Loop #", this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
      );
      return Math.max(40, loopTextW + 22);
   }

   private int builderWidthGap() {
      return 2;
   }

   private int footerWidthGap() {
      return 2;
   }

   private int listRowsTopY() {
      return this.listTopY() + 1;
   }

   private int listRowsBottomY() {
      return this.listRowsTopY() + this.listViewportHeight();
   }

   private int maxListScroll() {
      return this.macro == null ? 0 : Math.max(0, this.macro.actions.size() * this.rowHeight() - this.listViewportHeight());
   }

   private boolean isRowVisible(int rowY) {
      return rowY >= this.listTopY() && rowY + this.rowHeight() <= this.listRowsBottomY();
   }

   private int minWindowWidth() {
      return this.preferredContentWidth();
   }

   private int initialWindowHeight() {
      return this.preferredWindowHeight();
   }

   private int minWindowHeight() {
      return this.preferredWindowHeight();
   }

   private int titleInsetLeft() {
      return 10;
   }

   private int titleInsetRight() {
      return 39;
   }

   private int builderLabelOffset() {
      return 4;
   }

   private int builderLabelGap() {
      return 1;
   }

   private int listLabelOffset() {
      return 4;
   }

   private int rowButtonSize() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int rowButtonGap() {
      return 2;
   }

   private int rowRightGutter() {
      return 6;
   }

   private int rowButtonsRight() {
      return this.contentX() + this.contentWidth() - this.rowRightGutter() - 1;
   }

   private int rowTextRight(MacroAction action) {
      int buttonsX = this.rowButtonsX(action);
      return buttonsX - 1;
   }

   private int rowTextRight(MacroAction action, boolean allowDelete) {
      int buttonsX = this.rowButtonsX(action, allowDelete);
      return buttonsX - 1;
   }

   private int rowContentRight(MacroAction action, boolean allowMove, boolean allowDuplicate, boolean allowDelete) {
      int width = this.rowButtonCount(action, allowMove, allowDuplicate, allowDelete);
      if (width <= 0) {
         return this.rowButtonsRight();
      }

      int buttonsX = this.rowButtonsX(action, allowMove, allowDuplicate, allowDelete);
      return buttonsX - 1;
   }

   private int scrollbarX() {
      return this.contentX() + this.contentWidth() - 4;
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics listScrollbarMetrics() {
      this.quantizeListScroll();
      int listHeight = this.listAreaHeight();
      int viewHeight = Math.max(1, this.listViewportHeight());
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         this.macro.actions.size() * this.rowHeight(), viewHeight, this.scrollbarX(), this.listRowsTopY(), 3, Math.max(1, listHeight - 2), this.listScroll
      );
   }

   private int contentInset() {
      return 4;
   }

   private int bottomPadding() {
      return 4;
   }

   private int listBottomGap() {
      return 4;
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14) + 2;
   }

   private int screenRightMargin() {
      return 16;
   }

   private int defaultTopY() {
      return 30;
   }

   private int buttonHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int toolbarGap() {
      return 2;
   }

   private int deleteButtonWidth() {
      return this.buttonWidth("Delete");
   }

   private int nameFieldHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int nameRowHeight() {
      return this.nameFieldHeight() + 2;
   }

   private int e_I() {
      return 10;
   }

   private int textButtonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         autismclient.gui.f.b.k.init(this.font, label, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
      );
   }

   private int runButtonWidth() {
      return this.buttonHeight();
   }

   private String runButtonLabel(boolean localRunning) {
      return localRunning ? "Stop" : "Run";
   }

   private int lanButtonWidth() {
      return 28;
   }

   private int loopCountFieldWidth() {
      return 36;
   }

   private int loopCountFieldHeight() {
      return this.buttonHeight();
   }

   private int builderButtonGap() {
      return 2;
   }

   private int saveButtonWidth() {
      return 49;
   }

   private int undoButtonWidth() {
      return 44;
   }

   private int footerButtonGap() {
      return 3;
   }

   private int nameRowTopOffset() {
      return this.styleTheme.load() + 4;
   }

   private int builderTopY() {
      return this.editorY + this.nameRowTopOffset() + this.nameRowHeight() + this.buttonHeight();
   }

   private int p() {
      return 32;
   }

   private int q() {
      return 3;
   }

   private int r() {
      return 9;
   }

   private int s() {
      return 7;
   }

   private int t() {
      return 5;
   }

   private int rowButtonStride() {
      return this.rowButtonSize() + this.rowButtonGap();
   }

   private int rowTextY(int rowY) {
      return UiLayoutMath.centerTextY(rowY, this.rowHeight(), this.styleTheme.init_I_l(autismclient.gui.f.b.l.read), this.styleTheme.flush());
   }

   private int rowButtonY(int rowY) {
      return UiLayoutMath.centerStart(rowY, this.rowHeight(), this.rowButtonSize());
   }

   private int stableId(MacroAction action) {
      if (action == null) {
         return -1;
      }

      Integer existing = this.stableIds.get(action);
      if (existing != null) {
         return existing;
      }

      int created = this.nextStableId++;
      this.stableIds.put(action, created);
      return created;
   }

   private AutismMacroEditorOverlay.IndexRange selectionUnitAt(int index) {
      if (this.macro != null && index >= 0 && index < this.macro.actions.size()) {
         MacroAction action = this.macro.actions.get(index);
         AutismMacroEditorOverlay.BlockGroupLayout group = this.init_AutismMacroEditorO_I_MacroAction(index, action);
         if (group.multi && !group.header) {
            return new AutismMacroEditorOverlay.IndexRange(index, index + 1);
         }

         int start = group.hasHeader() ? group.headerIndex : index;
         int end = group.hasHeader() ? group.headerIndex + group.childCount + 1 : index + 1;
         start = Math.max(0, Math.min(start, this.macro.actions.size()));
         end = Math.max(start, Math.min(end, this.macro.actions.size()));
         return new AutismMacroEditorOverlay.IndexRange(start, end);
      } else {
         return AutismMacroEditorOverlay.IndexRange.EMPTY;
      }
   }

   private void selectOnly(int index) {
      this.selection.clear();
      this.selectUnit(index);
   }

   private void toggleUnitSelection(int index) {
      AutismMacroEditorOverlay.IndexRange unit = this.selectionUnitAt(index);
      if (!unit.isEmpty()) {
         boolean allSelected = true;

         for (int i = unit.start; i < unit.end; i++) {
            if (!this.selection.contains(this.stableId(this.macro.actions.get(i)))) {
               allSelected = false;
               break;
            }
         }

         if (allSelected) {
            for (int i = unit.start; i < unit.end; i++) {
               this.selection.deselect(this.stableId(this.macro.actions.get(i)));
            }
         } else {
            for (int i = unit.start; i < unit.end; i++) {
               this.selection.add(this.stableId(this.macro.actions.get(i)));
            }
         }
      }
   }

   private boolean selectRangeToAnchor(int index) {
      if (this.macro != null && this.selectionAnchorId != null) {
         int anchorIndex = -1;

         for (int i = 0; i < this.macro.actions.size(); i++) {
            if (this.stableId(this.macro.actions.get(i)) == this.selectionAnchorId) {
               anchorIndex = i;
               break;
            }
         }

         if (anchorIndex < 0) {
            return false;
         }

         this.selection.clear();

         for (int i = Math.min(anchorIndex, index); i <= Math.max(anchorIndex, index); i++) {
            this.selectUnit(i);
         }

         return true;
      } else {
         return false;
      }
   }

   private void handleRowClick(int index, boolean wasSelectedAtPress) {
      if (!wasSelectedAtPress && !this.isSelected(index)) {
         this.selectOnly(index);
      } else {
         this.toggleUnitSelection(index);
      }
   }

   private void selectUnit(int index) {
      AutismMacroEditorOverlay.IndexRange unit = this.selectionUnitAt(index);
      if (!unit.isEmpty() && this.macro != null) {
         for (int i = unit.start; i < unit.end; i++) {
            this.selection.add(this.stableId(this.macro.actions.get(i)));
         }
      }
   }

   private boolean isSelected(int index) {
      return this.macro != null && index >= 0 && index < this.macro.actions.size()
         ? this.selection.contains(this.stableId(this.macro.actions.get(index)))
         : false;
   }

   private void clearSelection() {
      this.selection.clear();
   }

   private List<AutismMacroEditorOverlay.IndexRange> selectedUnits() {
      List<AutismMacroEditorOverlay.IndexRange> ranges = new ArrayList<>();
      if (this.macro != null && !this.selection.isEmpty()) {
         for (int i = 0; i < this.macro.actions.size(); i++) {
            if (this.isSelected(i)) {
               AutismMacroEditorOverlay.IndexRange unit = this.selectionUnitAt(i);
               if (!unit.isEmpty()) {
                  ranges.add(unit);
               }
            }
         }

         return init_List_List_2(ranges);
      } else {
         return ranges;
      }
   }

   private static List<AutismMacroEditorOverlay.IndexRange> init_List_List_2(List<AutismMacroEditorOverlay.IndexRange> units) {
      if (units != null && !units.isEmpty()) {
         List<AutismMacroEditorOverlay.IndexRange> sorted = new ArrayList<>(units);
         sorted.sort((a, b) -> Integer.compare(a.start, b.start));
         List<AutismMacroEditorOverlay.IndexRange> merged = new ArrayList<>();

         for (AutismMacroEditorOverlay.IndexRange unit : sorted) {
            if (unit != null && !unit.isEmpty()) {
               if (merged.isEmpty()) {
                  merged.add(unit);
               } else {
                  AutismMacroEditorOverlay.IndexRange previous = merged.get(merged.size() - 1);
                  if (unit.start <= previous.end) {
                     merged.set(merged.size() - 1, new AutismMacroEditorOverlay.IndexRange(previous.start, Math.max(previous.end, unit.end)));
                  } else {
                     merged.add(unit);
                  }
               }
            }
         }

         return merged;
      } else {
         return List.of();
      }
   }

   private List<MacroAction> selectedActions() {
      List<MacroAction> out = new ArrayList<>();
      if (this.macro == null) {
         return out;
      }

      for (AutismMacroEditorOverlay.IndexRange unit : this.selectedUnits()) {
         for (int i = unit.start; i < unit.end && i < this.macro.actions.size(); i++) {
            out.add(this.macro.actions.get(i));
         }
      }

      return out;
   }

   private List<MacroAction> read_List_2() {
      List<MacroAction> out = new ArrayList<>();
      if (this.macro != null && !this.selection.isEmpty()) {
         for (MacroAction action : this.macro.actions) {
            if (this.selection.contains(this.stableId(action))) {
               out.add(action);
            }
         }

         return out;
      } else {
         return out;
      }
   }

   private CompoundTag serializeOrNull(MacroAction action) {
      if (action == null) {
         return null;
      }

      try {
         return AutismMacro.serializeAction(action);
      } catch (Throwable ignored) {
         return null;
      }
   }

   private boolean matchesSerialized(MacroAction action, CompoundTag expected) {
      CompoundTag actual = this.serializeOrNull(action);
      return actual != null && expected != null && actual.equals(expected);
   }

   private void selectSimilar(int index) {
      if (this.macro != null && index >= 0 && index < this.macro.actions.size()) {
         CompoundTag expected = this.serializeOrNull(this.macro.actions.get(index));
         if (expected != null) {
            this.selection.clear();

            for (MacroAction candidate : this.macro.actions) {
               if (this.matchesSerialized(candidate, expected)) {
                  this.selection.add(this.stableId(candidate));
               }
            }
         }
      }
   }

   private void openRowContextMenu(int index, int mouseX, int mouseY) {
      if (this.macro != null && index >= 0 && index < this.macro.actions.size()) {
         int stableId = this.stableId(this.macro.actions.get(index));
         long now = System.currentTimeMillis();
         boolean doubleClick = stableId == this.lastClickedStableId && now - this.lastClickTime >= 0L && now - this.lastClickTime <= 350L;
         if (doubleClick) {
            this.contextMenu.close();
            this.selectSimilar(index);
            this.lastClickedStableId = -1;
            this.lastClickTime = 0L;
         } else {
            if (!this.isSelected(index)) {
               this.selectOnly(index);
            }

            this.lastClickedStableId = stableId;
            this.lastClickTime = now;
            this.contextMenu.open(mouseX, mouseY, index);
         }
      }
   }

   private String[] contextMenuItems(Integer index) {
      return index != null && this.macro != null && index >= 0 && index < this.macro.actions.size()
         ? new String[]{
            "Play From Here",
            "Play This Step",
            "Select Similar",
            "Delete Similar",
            "Copy\tCtrl+C",
            "Paste\tCtrl+V",
            "Duplicate\tCtrl+D",
            "Delete\tDel",
            "Undo\tCtrl+Z",
            "Redo\tCtrl+R"
         }
         : new String[0];
   }

   private void onContextMenuItem(int stepIndex, int itemIndex) {
      switch (itemIndex) {
         case 0:
            this.playFrom(stepIndex, false);
            break;
         case 1:
            this.playFrom(stepIndex, true);
            break;
         case 2:
            this.selectSimilar(stepIndex);
            break;
         case 3:
            this.selectSimilar(stepIndex);
            this.prev_Z_2();
            break;
         case 4:
            this.copySelection();
            break;
         case 5:
            this.pasteClipboard();
            break;
         case 6:
            this.duplicateSelection();
            break;
         case 7:
            this.prev_Z_2();
            break;
         case 8:
            this.undo();
            break;
         case 9:
            this.redo();
      }
   }

   private void playFrom(int startIndex, boolean singleStep) {
      if (this.macro != null && startIndex >= 0 && startIndex < this.macro.actions.size()) {
         int endExclusive = this.macro.actions.size();
         if (singleStep) {
            AutismMacroEditorOverlay.BlockGroupLayout group = this.init_AutismMacroEditorO_I_MacroAction(startIndex, this.macro.actions.get(startIndex));
            endExclusive = group.hasHeader() && group.header ? Math.min(this.macro.actions.size(), startIndex + 1 + group.childCount) : startIndex + 1;
         }

         for (int i = startIndex; i < endExclusive; i++) {
            if (this.macro.actions.get(i) instanceof autismclient.util.macro.InventoryAuditAction auditAction && !auditAction.isConfigured()) {
               AutismClientMessaging.sendPrefixed("Inventory Audit needs at least one target.");
               return;
            }
         }

         this.stopRun();
         AutismMacro oneShot = new AutismMacro();
         oneShot.name = this.macro.name != null && !this.macro.name.isBlank() ? this.macro.name : "(Unsaved)";
         oneShot.loop = false;
         oneShot.loopCount = -1;

         for (int i = startIndex; i < endExclusive; i++) {
            MacroAction copy = AutismMacro.deserializeAction(this.macro.actions.get(i).toTag());
            if (copy != null) {
               oneShot.actions.add(copy);
            }
         }

         if (!oneShot.actions.isEmpty()) {
            this.cancelInteractions();
            this.oneShotRunId = oneShot.executeWithRunId();
            this.invalidateRunState();
            if (this.oneShotRunId >= 0L) {
               this.runStartedAt = System.currentTimeMillis();
            }

            this.rebuildButtons();
         }
      }
   }

   private boolean init_Z_MacroAction_2(MacroAction action) {
      return !(action instanceof autismclient.util.macro.RaceAction)
         && !(action instanceof autismclient.util.macro.ReportAction)
         && !(action instanceof autismclient.util.macro.PacketGateAction)
         && !(action instanceof autismclient.util.macro.EndPacketGateAction);
   }

   private List<MacroAction> editTargets(MacroAction primary) {
      if (primary == null) {
         return List.of();
      }

      if (this.containsAction(primary) && this.init_Z_MacroAction_2(primary)) {
         if (!this.selection.contains(this.stableId(primary))) {
            return List.of(primary);
         }

         List<MacroAction> selected = this.read_List_2();
         if (selected.size() <= 1) {
            return List.of(primary);
         }

         CompoundTag expected = this.serializeOrNull(primary);
         if (expected == null) {
            return List.of(primary);
         }

         for (MacroAction action : selected) {
            if (!this.init_Z_MacroAction_2(action) || !this.matchesSerialized(action, expected)) {
               return List.of(primary);
            }
         }

         return List.copyOf(selected);
      } else {
         return List.of(primary);
      }
   }

   private boolean containsAction(MacroAction action) {
      if (this.macro != null && action != null) {
         for (MacroAction candidate : this.macro.actions) {
            if (candidate == action) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void editAction(int index, MacroAction primary) {
      if (primary != null && this.isEditable(primary)) {
         List<MacroAction> targets = this.editTargets(primary);
         ActionEditorOverlay actionEditor = ActionEditorOverlay.getInstance();
         actionEditor.setCaptureAllowed(this.isCaptureAllowed());
         actionEditor.setContext(this.macro, index);
         actionEditor.openAction(primary, this::recordUndo, () -> this.applyEditToTargets(primary, targets), targets.size());
      }
   }

   private void applyEditToTargets(MacroAction primary, List<MacroAction> targets) {
      if (primary != null) {
         CompoundTag edited = this.serializeOrNull(primary);
         if (edited != null && targets != null && targets.size() > 1) {
            for (MacroAction target : targets) {
               if (target != primary && this.containsAction(target)) {
                  CompoundTag copy = edited.copy();
                  target.fromTag(copy);
                  DynamicBindingManager.init(target, copy);
               }
            }
         }

         this.load_V_MacroAction_2(primary);
      }
   }

   private List<MacroAction> cloneActions(List<MacroAction> actions) {
      List<MacroAction> clones = new ArrayList<>();
      if (actions == null) {
         return clones;
      }

      for (MacroAction action : actions) {
         if (action != null) {
            MacroAction copy = AutismMacro.deserializeAction(action.toTag());
            if (copy != null) {
               clones.add(copy);
            }
         }
      }

      this.init_V_List_3(clones);
      return clones;
   }

   private void init_V_List_3(List<MacroAction> actions) {
      if (actions != null && !actions.isEmpty()) {
         String pendingGateId = null;

         for (MacroAction action : actions) {
            if (action instanceof autismclient.util.macro.PacketGateAction gate) {
               gate.gateId = UUID.randomUUID().toString();
               pendingGateId = gate.scope == autismclient.util.macro.PacketGateAction.GateScope.MACRO_PASS ? null : gate.gateId;
            } else if (action instanceof autismclient.util.macro.EndPacketGateAction endGate && pendingGateId != null) {
               endGate.gateId = pendingGateId;
               pendingGateId = null;
            }
         }
      }
   }

   private int pasteInsertIndex() {
      List<AutismMacroEditorOverlay.IndexRange> units = this.selectedUnits();
      if (units.isEmpty()) {
         return this.macro == null ? 0 : this.macro.actions.size();
      } else {
         return Math.max(0, Math.min(this.macro.actions.size(), units.get(units.size() - 1).end));
      }
   }

   private void selectActions(List<MacroAction> pasted) {
      this.selection.clear();
      if (pasted != null) {
         for (MacroAction action : pasted) {
            if (action != null) {
               this.selection.add(this.stableId(action));
            }
         }
      }
   }

   private void pruneSelection() {
      if (this.macro != null && !this.selection.isEmpty()) {
         Set<Integer> liveIds = new LinkedHashSet<>();

         for (MacroAction action : this.macro.actions) {
            liveIds.add(this.stableId(action));
         }

         this.selection.retainLive(liveIds);
      } else {
         this.selection.clear();
      }
   }

   private boolean isTextFieldFocused() {
      return this.nameField != null && this.nameField.isFocused()
         || this.loopMode == 1 && this.loopCountField != null && this.loopCountField.isFocused()
         || this.paletteSearchField != null && this.paletteSearchField.isFocused();
   }

   private boolean isCtrlDown() {
      if (MC != null && MC.getWindow() != null) {
         long handle = MC.getWindow().handle();
         return CompatGLFW.glfwGetKey(handle, 341) == 1 || CompatGLFW.glfwGetKey(handle, 345) == 1;
      } else {
         return false;
      }
   }

   private boolean hasCtrl(int modifiers) {
      return (modifiers & 2) != 0 || this.isCtrlDown();
   }

   private boolean isShiftDown() {
      if (MC != null && MC.getWindow() != null) {
         long handle = MC.getWindow().handle();
         return CompatGLFW.glfwGetKey(handle, 340) == 1 || CompatGLFW.glfwGetKey(handle, 344) == 1;
      } else {
         return false;
      }
   }

   private boolean hasShift(int modifiers) {
      return (modifiers & 1) != 0 || this.isShiftDown();
   }

   private static String stepWord(int count) {
      return count == 1 ? "step" : "steps";
   }

   private boolean copySelection() {
      if (this.isStepListLocked()) {
         return true;
      }

      List<MacroAction> selected = this.selectedActions();
      if (selected.isEmpty()) {
         bi.read("Select macro steps first.");
         return true;
      }

      if (MacroClipboardCodec.copySteps(selected)) {
         bi.init("Copied " + selected.size() + " macro " + stepWord(selected.size()) + ".");
      } else {
         bi.write("Failed to copy macro steps.");
      }

      return true;
   }

   private boolean pasteClipboard() {
      if (this.isStepListLocked()) {
         return true;
      }

      if (this.macro == null) {
         return false;
      }

      List<MacroAction> pasted = MacroClipboardCodec.pasteSteps();
      if (pasted != null && !pasted.isEmpty()) {
         this.init_V_List_3(pasted);
      } else {
         pasted = this.cloneActions(this.selectedActions());
      }

      if (pasted != null && !pasted.isEmpty()) {
         int insertIndex = this.pasteInsertIndex();
         this.recordUndo();
         this.macro.actions.addAll(insertIndex, pasted);
         this.onActionsChanged();
         this.selectActions(pasted);
         bi.init("Pasted " + pasted.size() + " macro " + stepWord(pasted.size()) + ".", -13248397);
         return true;
      } else {
         bi.read("Clipboard has no macro steps.");
         return true;
      }
   }

   private boolean start_Z_2() {
      if (this.isStepListLocked()) {
         return true;
      }

      List<MacroAction> selected = this.selectedActions();
      if (selected.isEmpty()) {
         bi.read("Select macro steps first.");
         return true;
      }

      if (!MacroClipboardCodec.copySteps(selected)) {
         bi.write("Failed to copy macro steps.");
         return true;
      }

      int count = selected.size();
      if (this.load_Z_Z(false)) {
         bi.init("Cut " + count + " macro " + stepWord(count) + ".");
      }

      return true;
   }

   private boolean selectAll() {
      if (this.macro != null && !this.macro.actions.isEmpty()) {
         this.selectActions(this.macro.actions);
         return true;
      } else {
         return false;
      }
   }

   private boolean duplicateSelection() {
      if (this.isStepListLocked()) {
         return true;
      }

      if (this.macro != null && !this.selection.isEmpty()) {
         List<MacroAction> copies = this.cloneActions(this.selectedActions());
         if (copies.isEmpty()) {
            return false;
         }

         int insertIndex = this.pasteInsertIndex();
         this.recordUndo();
         this.macro.actions.addAll(insertIndex, copies);
         this.onActionsChanged();
         this.selectActions(copies);
         bi.init("Duplicated " + copies.size() + " macro " + stepWord(copies.size()) + ".", -13248397);
         return true;
      } else {
         return false;
      }
   }

   private boolean prev_Z_2() {
      return this.load_Z_Z(true);
   }

   private boolean load_Z_Z(boolean announce) {
      if (this.isStepListLocked()) {
         return true;
      }

      if (this.macro != null && !this.selection.isEmpty()) {
         List<AutismMacroEditorOverlay.IndexRange> units = new ArrayList<>(this.selectedUnits());
         if (units.isEmpty()) {
            this.selection.clear();
            return false;
         }

         ActionEditorOverlay editor = ActionEditorOverlay.getExisting();
         this.recordUndo();
         TreeSet<Integer> deleteIndices = new TreeSet<>();

         for (AutismMacroEditorOverlay.IndexRange unit : units) {
            for (int i = unit.start; i < unit.end && i < this.macro.actions.size(); i++) {
               deleteIndices.add(i);
            }
         }

         for (int i : new ArrayList<>(deleteIndices)) {
            if (this.macro.actions.get(i) instanceof autismclient.util.macro.MultiAction multi) {
               int count = multi.init(this.macro.actions, i);

               for (int j = i + 1; j <= i + count; j++) {
                  deleteIndices.remove(j);
               }
            }
         }

         Map<autismclient.util.macro.MultiAction, List<MacroAction>> multiCoverage = this.init_Map();
         int removed = 0;

         for (int i : deleteIndices.descendingSet()) {
            if (i >= 0 && i < this.macro.actions.size()) {
               if (editor != null) {
                  editor.closeIfEditing(this.macro.actions.get(i));
               }

               this.macro.actions.remove(i);
               removed++;
            }
         }

         this.init_V_Map_Set(multiCoverage, null);
         this.selection.clear();
         this.onActionsChanged();
         if (removed > 0 && announce) {
            bi.init("Deleted " + removed + " macro " + stepWord(removed) + ".", -42149);
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean moveSelection(int direction) {
      if (this.isStepListLocked()) {
         return true;
      }

      if (this.macro != null && !this.selection.isEmpty() && direction != 0) {
         List<AutismMacroEditorOverlay.IndexRange> orderedUnits = new ArrayList<>();
         int i = 0;

         while (i < this.macro.actions.size()) {
            AutismMacroEditorOverlay.IndexRange unit = this.selectionUnitAt(i);
            if (unit.isEmpty()) {
               i++;
            } else {
               orderedUnits.add(unit);
               i = unit.end;
            }
         }

         if (orderedUnits.size() < 2) {
            return false;
         }

         boolean moved = false;
         if (direction < 0) {
            for (int ix = 1; ix < orderedUnits.size(); ix++) {
               if (this.init_Z_AutismMacroEditorO(orderedUnits.get(ix)) && !this.init_Z_AutismMacroEditorO(orderedUnits.get(ix - 1))) {
                  AutismMacroEditorOverlay.IndexRange unit = orderedUnits.remove(ix);
                  orderedUnits.add(ix - 1, unit);
                  moved = true;
               }
            }
         } else {
            for (int ix = orderedUnits.size() - 2; ix >= 0; ix--) {
               if (this.init_Z_AutismMacroEditorO(orderedUnits.get(ix)) && !this.init_Z_AutismMacroEditorO(orderedUnits.get(ix + 1))) {
                  AutismMacroEditorOverlay.IndexRange unit = orderedUnits.remove(ix);
                  orderedUnits.add(ix + 1, unit);
                  moved = true;
               }
            }
         }

         if (!moved) {
            return false;
         }

         Map<autismclient.util.macro.MultiAction, List<MacroAction>> multiCoverage = this.init_Map();
         Set<MacroAction> movedRefs = Collections.newSetFromMap(new IdentityHashMap<>());
         movedRefs.addAll(this.selectedActions());
         List<MacroAction> rebuilt = new ArrayList<>(this.macro.actions.size());

         for (AutismMacroEditorOverlay.IndexRange unit : orderedUnits) {
            for (int ix = unit.start; ix < unit.end && ix < this.macro.actions.size(); ix++) {
               rebuilt.add(this.macro.actions.get(ix));
            }
         }

         this.recordUndo();
         this.macro.actions.clear();
         this.macro.actions.addAll(rebuilt);
         this.init_V_Map_Set(multiCoverage, movedRefs);
         this.onActionsChanged();
         return true;
      } else {
         return false;
      }
   }

   private boolean init_Z_AutismMacroEditorO(AutismMacroEditorOverlay.IndexRange unit) {
      if (unit != null && !unit.isEmpty() && this.macro != null) {
         for (int i = unit.start; i < unit.end && i < this.macro.actions.size(); i++) {
            if (this.selection.contains(this.stableId(this.macro.actions.get(i)))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private List<AutismMacroEditorOverlay.ActionRowItem> captureRowSnapshots() {
      List<AutismMacroEditorOverlay.ActionRowItem> snapshots = new ArrayList<>();
      if (this.macro == null) {
         return snapshots;
      }

      for (int index = 0; index < this.macro.actions.size(); index++) {
         MacroAction action = this.macro.actions.get(index);
         snapshots.add(new AutismMacroEditorOverlay.ActionRowItem(action, this.stableId(action), action.getDisplayName(), this.isWaitAction(action), index));
      }

      return snapshots;
   }

   private AutismMacroEditorOverlay.ActionRowItem findSnapshot(List<AutismMacroEditorOverlay.ActionRowItem> snapshots, int stableId) {
      for (AutismMacroEditorOverlay.ActionRowItem snapshot : snapshots) {
         if (snapshot.stableId == stableId) {
            return snapshot;
         }
      }

      return null;
   }

   private AutismMacroEditorOverlay.RowTransitionAnim findAnimation(int stableId) {
      for (AutismMacroEditorOverlay.RowTransitionAnim motion : this.rowAnimations) {
         if (motion.stableId == stableId) {
            return motion;
         }
      }

      return null;
   }

   private void cancelAnimation(int stableId) {
      this.rowAnimations.removeIf(motion -> motion.stableId == stableId);
   }

   private void refreshRowAnimations(boolean animateChanges) {
      List<AutismMacroEditorOverlay.ActionRowItem> current = this.captureRowSnapshots();
      if (!animateChanges) {
         this.rowAnimations.clear();
         this.rowSnapshots.clear();
         this.rowSnapshots.addAll(current);
         this.lastAnimationTick = System.nanoTime();
      } else {
         this.rowAnimations.clear();

         for (AutismMacroEditorOverlay.ActionRowItem snapshot : current) {
            AutismMacroEditorOverlay.ActionRowItem previous = this.findSnapshot(this.rowSnapshots, snapshot.stableId);
            if (previous == null) {
               this.cancelAnimation(snapshot.stableId);
               this.rowAnimations
                  .add(
                     new AutismMacroEditorOverlay.RowTransitionAnim(
                        snapshot.stableId, snapshot.label, snapshot.conditional, snapshot.index, snapshot.index, AutismMacroEditorOverlay.RowAnimKind.ENTER
                     )
                  );
            } else if (previous.index != snapshot.index) {
               this.cancelAnimation(snapshot.stableId);
               this.rowAnimations
                  .add(
                     new AutismMacroEditorOverlay.RowTransitionAnim(
                        snapshot.stableId, snapshot.label, snapshot.conditional, previous.index, snapshot.index, AutismMacroEditorOverlay.RowAnimKind.MOVE
                     )
                  );
            }
         }

         for (AutismMacroEditorOverlay.ActionRowItem snapshot : this.rowSnapshots) {
            if (this.findSnapshot(current, snapshot.stableId) == null) {
               this.cancelAnimation(snapshot.stableId);
               this.rowAnimations
                  .add(
                     new AutismMacroEditorOverlay.RowTransitionAnim(
                        snapshot.stableId, snapshot.label, snapshot.conditional, snapshot.index, snapshot.index, AutismMacroEditorOverlay.RowAnimKind.EXIT
                     )
                  );
            }
         }

         this.rowSnapshots.clear();
         this.rowSnapshots.addAll(current);
      }
   }

   private void resetRowState() {
      this.selection.clear();
      this.stableIds.clear();
      this.rowSnapshots.clear();
      this.rowAnimations.clear();
      this.nextStableId = 1;
      this.lastAnimationTick = System.nanoTime();
      if (this.macro != null) {
         this.rowSnapshots.addAll(this.captureRowSnapshots());
      }
   }

   private float advanceAnimations() {
      long now = System.nanoTime();
      float delta = Math.max(0.0F, Math.min(0.05F, (float)(now - this.lastAnimationTick) / 1.0E9F));
      this.lastAnimationTick = now;
      if (this.rowAnimations.isEmpty()) {
         return delta;
      }

      this.rowAnimations.removeIf(motion -> {
         float var10000;
         switch (motion.kind) {
            case ENTER:
               var10000 = 0.14F;
               break;
            case MOVE:
               var10000 = 0.14F;
               break;
            case EXIT:
               var10000 = 0.12F;
               break;
            default:
               throw new MatchException(null, null);
         }

         float duration = var10000;
         motion.progress = Math.min(1.0F, motion.progress + (duration <= 0.0F ? 1.0F : delta / duration));
         return motion.progress >= 0.999F;
      });
      return delta;
   }

   private float init_F_I(int stableId) {
      AutismMacroEditorOverlay.RowTransitionAnim motion = this.findAnimation(stableId);
      return motion != null && motion.kind == AutismMacroEditorOverlay.RowAnimKind.ENTER ? this.easeOutCubic(motion.progress) : 1.0F;
   }

   private int animatedRowY(AutismMacroEditorOverlay.RowTransitionAnim motion, int baseListY) {
      float eased = this.easeOutCubic(motion.progress);
      int fromY = baseListY + motion.fromRowIndex * this.rowHeight();
      int toY = baseListY + motion.toRowIndex * this.rowHeight();
      return Math.round(fromY + (toY - fromY) * eased);
   }

   private int init_I_AutismMacroEditorO(AutismMacroEditorOverlay.RowTransitionAnim motion) {
      if (motion == null) {
         return 0;
      }

      float eased = this.easeOutCubic(motion.progress);
      int var10000;
      switch (motion.kind) {
         case ENTER:
            var10000 = Math.round((1.0F - eased) * -18.0F);
            break;
         case MOVE:
            var10000 = 0;
            break;
         case EXIT:
            var10000 = Math.round(eased * 20.0F);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private float init_F_AutismMacroEditorO(AutismMacroEditorOverlay.RowTransitionAnim motion) {
      if (motion == null) {
         return 1.0F;
      }

      float eased = this.easeOutCubic(motion.progress);
      float var10000;
      switch (motion.kind) {
         case ENTER:
            var10000 = 0.45F + 0.55F * eased;
            break;
         case MOVE:
            var10000 = 1.0F;
            break;
         case EXIT:
            var10000 = Math.max(0.0F, 1.0F - eased);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private float easeOutCubic(float value) {
      float clamped = Math.max(0.0F, Math.min(1.0F, value));
      return 1.0F - (float)Math.pow(1.0F - clamped, 3.0);
   }

   private void quantizeListScroll() {
      this.listScroll = this.quantizeScrollOffset(this.listScroll, this.rowHeight(), this.maxListScroll());
   }

   private int dragRowTop() {
      return this.dragMouseY - this.dragGrabOffsetY;
   }

   private int dropInsertSlot(int sourceIndex, int targetIndex) {
      if (sourceIndex >= 0 && targetIndex >= 0) {
         return targetIndex > sourceIndex ? targetIndex + 1 : targetIndex;
      } else {
         return -1;
      }
   }

   private int shiftedIndex(int index, int sourceIndex, int targetIndex) {
      if (sourceIndex < 0 || targetIndex < 0 || sourceIndex == targetIndex) {
         return index;
      } else if (sourceIndex < targetIndex) {
         return index > sourceIndex && index <= targetIndex ? index - 1 : index;
      } else {
         return index >= targetIndex && index < sourceIndex ? index + 1 : index;
      }
   }

   private void beginRowPress(int index, double mouseX, double mouseY, boolean canDrag) {
      if (!this.isStepListLocked()) {
         this.pressedRowIndex = index;
         this.dragTargetIndex = index;
         this.dragGrabOffsetY = (int)Math.round(mouseY) - (this.listTopY() - this.listScroll + index * this.rowHeight());
         this.dragMouseY = (int)Math.round(mouseY);
         this.pressX = mouseX;
         this.pressY = mouseY;
         this.pressedRowWasSelected = this.isSelected(index);
         this.pressCanDrag = canDrag;
      }
   }

   private void updateRowDrag(double mouseX, double mouseY) {
      if (this.isStepListLocked()) {
         this.resetDragState();
      } else if (this.draggingRowIndex < 0 && this.pressedRowIndex >= 0 && this.pressCanDrag) {
         boolean movedEnough = Math.abs(mouseX - this.pressX) >= 3.0 || Math.abs(mouseY - this.pressY) >= 3.0;
         if (movedEnough) {
            this.draggingRowIndex = this.pressedRowIndex;
            this.dragTargetIndex = this.pressedRowIndex;
            this.dragMouseY = (int)Math.round(mouseY);
         }
      }
   }

   private void dragRowTo(double mouseY) {
      if (this.draggingRowIndex >= 0 && this.macro != null && !this.macro.actions.isEmpty()) {
         this.dragMouseY = (int)Math.round(mouseY);
         this.autoScrollWhileDragging(mouseY);
         double draggedRowTop = mouseY - this.dragGrabOffsetY;
         double draggedRowCenter = draggedRowTop - this.listTopY() + this.listScroll + this.rowHeight() / 2.0;
         this.dragTargetIndex = this.dropTargetIndex(draggedRowCenter);
      }
   }

   private int dropTargetIndex(double draggedRowCenter) {
      if (this.draggingRowIndex >= 0 && this.macro != null && !this.macro.actions.isEmpty()) {
         int rowHeight = this.rowHeight();
         int targetIndex = this.draggingRowIndex;
         double sourceCenter = this.draggingRowIndex * rowHeight + rowHeight / 2.0;
         if (draggedRowCenter < sourceCenter) {
            while (targetIndex > 0) {
               double previousCenter = (targetIndex - 1) * rowHeight + rowHeight / 2.0;
               if (!(draggedRowCenter < previousCenter)) {
                  break;
               }

               targetIndex--;
            }
         } else if (draggedRowCenter > sourceCenter) {
            for (int maxIndex = this.macro.actions.size() - 1; targetIndex < maxIndex; targetIndex++) {
               double nextCenter = (targetIndex + 1) * rowHeight + rowHeight / 2.0;
               if (!(draggedRowCenter > nextCenter)) {
                  break;
               }
            }
         }

         return targetIndex;
      } else {
         return -1;
      }
   }

   private void autoScrollWhileDragging(double mouseY) {
      int maxScroll = this.maxListScroll();
      if (maxScroll > 0) {
         int listTop = this.listTopY();
         int listBottom = listTop + this.listAreaHeight();
         int delta = 0;
         if (mouseY < listTop + 18) {
            double depth = listTop + 18 - mouseY;
            delta = -Math.min(12, Math.max(1, (int)Math.ceil(depth / 3.0)));
         } else if (mouseY > listBottom - 18) {
            double depth = mouseY - (listBottom - 18);
            delta = Math.min(12, Math.max(1, (int)Math.ceil(depth / 3.0)));
         }

         if (delta != 0) {
            this.listScroll = Math.max(0, Math.min(maxScroll, this.listScroll + delta));
         }
      }
   }

   private void resetDragState() {
      this.draggingRowIndex = -1;
      this.pressedRowIndex = -1;
      this.dragTargetIndex = -1;
      this.dragGrabOffsetY = 0;
      this.dragMouseY = 0;
      this.pressX = 0.0;
      this.pressY = 0.0;
      this.pressedRowWasSelected = false;
      this.pressCanDrag = false;
   }

   private void moveAction(int fromIndex, int toIndex) {
      if (!this.isStepListLocked()
         && this.macro != null
         && fromIndex >= 0
         && toIndex >= 0
         && fromIndex < this.macro.actions.size()
         && toIndex < this.macro.actions.size()
         && fromIndex != toIndex) {
         MacroAction source = this.macro.actions.get(fromIndex);
         if (this.flush_Z(fromIndex)) {
            return;
         }

         int moveCount = 1;
         if (source instanceof autismclient.util.macro.RaceAction race) {
            moveCount = race.countBodyActions(this.macro.actions, fromIndex) + 1;
         } else if (source instanceof autismclient.util.macro.ReportAction report) {
            moveCount = report.coveredCount(this.macro.actions, fromIndex) + 1;
         } else if (source instanceof autismclient.util.macro.MultiAction multi) {
            moveCount = multi.init(this.macro.actions, fromIndex) + 1;
         } else if (source instanceof autismclient.util.macro.PacketGateAction) {
            moveCount = this.sync_I(fromIndex) + 1;
         } else if (source instanceof autismclient.util.macro.EndPacketGateAction) {
            this.sync_V_2(fromIndex, toIndex);
            return;
         }

         AutismMacroEditorOverlay.BlockGroupLayout targetGroup = this.init_AutismMacroEditorO_I_MacroAction(toIndex, this.macro.actions.get(toIndex));
         if (targetGroup.hasHeader() && targetGroup.headerIndex == fromIndex) {
            return;
         }

         if (targetGroup.multi) {
            targetGroup = AutismMacroEditorOverlay.BlockGroupLayout.NONE;
         }

         int insertIndex;
         if (targetGroup.hasHeader()) {
            insertIndex = toIndex > fromIndex ? targetGroup.headerIndex + targetGroup.childCount + 1 : targetGroup.headerIndex;
         } else if (toIndex > fromIndex) {
            insertIndex = toIndex + 1;
         } else {
            insertIndex = toIndex;
         }

         Map<autismclient.util.macro.MultiAction, List<MacroAction>> multiCoverage = this.init_Map();
         List<MacroAction> moved = new ArrayList<>(this.macro.actions.subList(fromIndex, fromIndex + moveCount));
         this.macro.actions.subList(fromIndex, fromIndex + moveCount).clear();
         if (insertIndex > fromIndex) {
            insertIndex -= moveCount;
         }

         insertIndex = Math.max(0, Math.min(insertIndex, this.macro.actions.size()));
         this.macro.actions.addAll(insertIndex, moved);
         Set<MacroAction> movedSet = Collections.newSetFromMap(new IdentityHashMap<>());
         movedSet.addAll(moved);
         this.init_V_Map_Set(multiCoverage, movedSet);
         this.onActionsChanged();
      }
   }

   private void finishRowPress() {
      if (this.isStepListLocked()) {
         this.resetDragState();
      } else {
         int sourceIndex = this.draggingRowIndex;
         int pressIndex = this.pressedRowIndex;
         int targetIndex = this.dragTargetIndex;
         boolean wasSelectedAtPress = this.pressedRowWasSelected;
         boolean dragged = sourceIndex >= 0;
         this.resetDragState();
         if (dragged) {
            if (this.macro != null && targetIndex >= 0 && sourceIndex != targetIndex) {
               this.recordUndo();
               if (this.isSelected(sourceIndex) && this.selectedActions().size() > 1) {
                  this.moveSelectedBlock(sourceIndex, targetIndex);
               } else {
                  this.moveAction(sourceIndex, targetIndex);
               }
            }
         } else if (pressIndex >= 0 && !this.isCtrlDown() && !this.isShiftDown()) {
            this.handleRowClick(pressIndex, wasSelectedAtPress);
         }
      }
   }

   private void moveSelectedBlock(int sourceIndex, int targetIndex) {
      if (this.macro != null) {
         List<MacroAction> refs = this.selectedActions();
         if (refs.size() > 1 && !this.containsPacketGate(refs)) {
            List<MacroAction> actions = this.macro.actions;
            Set<MacroAction> selectedSet = Collections.newSetFromMap(new IdentityHashMap<>());
            selectedSet.addAll(refs);
            int insertPos = this.init_I_Set(selectedSet, sourceIndex, targetIndex);
            if (insertPos >= 0) {
               Map<autismclient.util.macro.MultiAction, List<MacroAction>> multiCoverage = this.init_Map();
               List<MacroAction> remaining = new ArrayList<>(actions.size());

               for (MacroAction action : actions) {
                  if (!selectedSet.contains(action)) {
                     remaining.add(action);
                  }
               }

               insertPos = Math.max(0, Math.min(remaining.size(), insertPos));
               remaining.addAll(insertPos, refs);
               actions.clear();
               actions.addAll(remaining);
               this.init_V_Map_Set(multiCoverage, selectedSet);
            }
         } else {
            this.moveAction(sourceIndex, targetIndex);
         }
      }
   }

   private boolean containsPacketGate(List<MacroAction> refs) {
      if (refs == null) {
         return false;
      }

      for (MacroAction ref : refs) {
         if (ref instanceof autismclient.util.macro.PacketGateAction || ref instanceof autismclient.util.macro.EndPacketGateAction) {
            return true;
         }
      }

      return false;
   }

   private int init_I_Set(Set<MacroAction> selectedSet, int sourceIndex, int targetIndex) {
      if (this.macro == null) {
         return -1;
      }

      List<MacroAction> actions = this.macro.actions;
      if (targetIndex >= 0 && targetIndex < actions.size()) {
         MacroAction anchor = actions.get(targetIndex);
         if (selectedSet.contains(anchor)) {
            return -1;
         }

         boolean movingDown = targetIndex > sourceIndex;
         AutismMacroEditorOverlay.BlockGroupLayout group = this.init_AutismMacroEditorO_I_MacroAction(targetIndex, anchor);
         if (group.multi) {
            group = AutismMacroEditorOverlay.BlockGroupLayout.NONE;
         }

         int anchorReal;
         boolean insertAfter;
         if (group.hasHeader()) {
            int lastChild = Math.min(actions.size() - 1, group.headerIndex + group.childCount);
            anchorReal = movingDown ? lastChild : group.headerIndex;
            insertAfter = movingDown;
         } else {
            anchorReal = targetIndex;
            insertAfter = movingDown;
         }

         if (anchorReal >= 0 && anchorReal < actions.size() && !selectedSet.contains(actions.get(anchorReal))) {
            int selectedBefore = 0;

            for (int j = 0; j < anchorReal; j++) {
               if (selectedSet.contains(actions.get(j))) {
                  selectedBefore++;
               }
            }

            int compactedAnchorPos = anchorReal - selectedBefore;
            return insertAfter ? compactedAnchorPos + 1 : compactedAnchorPos;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private int init_I_I_2(int realIndex, Set<MacroAction> selectedSet, int insertCompact, int blockSize) {
      List<MacroAction> actions = this.macro.actions;
      int selectedBefore = 0;

      for (int j = 0; j < realIndex; j++) {
         if (selectedSet.contains(actions.get(j))) {
            selectedBefore++;
         }
      }

      int compactedPos = realIndex - selectedBefore;
      if (insertCompact >= 0 && compactedPos >= insertCompact) {
         compactedPos += blockSize;
      }

      return compactedPos;
   }

   private void renderActionRow(GuiGraphicsExtractor context, MacroAction action, int top, int number) {
      autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(
         context, this.contentX() + 1, top + 1, this.rowButtonsRight() - this.contentX() - 1, this.rowHeight() - 2, this.styleTheme.read(4607322)
      );
      int badgeColor = this.isWaitAction(action) ? -30720 : -12268476;
      autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(context, this.contentX() + 2, top + 3, 3, this.rowHeight() - 6, badgeColor);
      autismclient.gui.f.b.k.init(
         context,
         this.font,
         String.valueOf(number),
         this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
         UiColorPalette.textSecondary(),
         this.contentX() + 8,
         this.rowTextY(top),
         false
      );
      int nameX = this.contentX() + 22;
      int controlsX = this.rowButtonsX(action);
      int maxNameW = controlsX - nameX - 8;
      String displayName = autismclient.gui.f.b.k.init(
         this.font, action.getDisplayName(), maxNameW, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), -1
      );
      autismclient.gui.f.b.k.init(
         context, this.font, displayName, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), -1, nameX, this.rowTextY(top), false
      );
   }

   private int pickerWidth() {
      return this.pickerViewportWidth() + 19;
   }

   private int cachedLabelWidth(String label) {
      Integer cached = this.labelWidthCache.get(label);
      if (cached != null) {
         return cached;
      }

      int w = autismclient.gui.f.b.k.init(
         this.font, label, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), this.styleTheme.load(autismclient.gui.f.b.l.read)
      );
      w = Math.min(88, w);
      this.labelWidthCache.put(label, w);
      return w;
   }

   private int pickerViewportWidth() {
      int screenW = MC.getWindow() != null ? cA.init_I() : 320;
      int screenH = MC.getWindow() != null ? cA.load_I() : 320;
      if (this.cachedPickerViewportWidth >= 0 && this.cachedLayoutRevision == this.paletteRevision && this.cachedPickerScreenW == screenW && this.cachedPickerScreenH == screenH) {
         return this.cachedPickerViewportWidth;
      }

      int result = this.computePickerViewportWidth(screenW, screenH);
      this.cachedLayoutRevision = this.paletteRevision;
      this.cachedPickerScreenW = screenW;
      this.cachedPickerScreenH = screenH;
      this.cachedPickerViewportWidth = result;
      return result;
   }

   private int computePickerViewportWidth(int screenW, int screenH) {
      int maxViewportWidth = Math.max(1, Math.min(456, screenW - 20));
      int minViewportWidth = Math.min(132, maxViewportWidth);
      int maxContentHeight = Math.max(1, screenH - 36);
      List<AutismMacroEditorOverlay.PaletteCategoryLayout> fallbackLayouts = null;
      int fallbackOverflow = 2147483647;
      int fallbackWidth = maxViewportWidth;

      for (int candidateWidth = minViewportWidth; candidateWidth <= maxViewportWidth; candidateWidth += 6) {
         List<AutismMacroEditorOverlay.PaletteCategoryLayout> layouts = this.layoutPaletteCategories(0, 0, candidateWidth);
         int contentHeight = this.layoutsBottom(layouts);
         if (contentHeight <= maxContentHeight) {
            return this.clampViewportWidth(this.load_I_List(layouts), minViewportWidth, maxViewportWidth);
         }

         int overflow = contentHeight - maxContentHeight;
         if (overflow < fallbackOverflow) {
            fallbackOverflow = overflow;
            fallbackLayouts = layouts;
            fallbackWidth = candidateWidth;
         }
      }

      return fallbackLayouts != null ? this.clampViewportWidth(this.load_I_List(fallbackLayouts), minViewportWidth, fallbackWidth) : maxViewportWidth;
   }

   private int clampViewportWidth(int width, int minViewportWidth, int maxViewportWidth) {
      int widenedWidth = width + 8;
      return Math.max(minViewportWidth, Math.min(maxViewportWidth, widenedWidth));
   }

   private int paletteContentHeight() {
      List<AutismMacroEditorOverlay.PaletteCategoryLayout> layouts = this.layoutPaletteCategories(0, 0, this.pickerViewportWidth());
      return this.layoutsBottom(layouts);
   }

   private int pickerHeight() {
      int screenH = MC.getWindow() != null ? cA.load_I() : 320;
      int desiredHeight = 48 + this.paletteContentHeight();
      return Math.max(1, Math.min(desiredHeight, screenH - 8));
   }

   private int pickerX() {
      int pickerW = this.pickerWidth();
      int screenW = MC.getWindow() != null ? cA.init_I() : this.editorX + this.editorWidth + pickerW + 8;
      if (this.editorX + this.editorWidth + pickerW + 5 <= screenW) {
         return this.editorX + this.editorWidth + 5;
      } else {
         return this.editorX - pickerW - 5 >= 0
            ? this.editorX - pickerW - 5
            : Math.max(4, Math.min(screenW - pickerW - 4, this.editorX + (this.editorWidth - pickerW) / 2));
      }
   }

   private int pickerY() {
      int pickerH = this.pickerHeight();
      int screenH = MC.getWindow() != null ? cA.load_I() : this.editorY + pickerH + 8;
      int preferredY = this.editorY + 42;
      return Math.max(4, Math.min(screenH - pickerH - 4, preferredY));
   }

   private int paletteCloseX() {
      return this.paletteHeaderRect().read();
   }

   private int paletteCloseY() {
      return this.paletteHeaderRect().write();
   }

   private autismclient.gui.f.NetHelper paletteHeaderRect() {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.load(autismclient.gui.f.NetHelper.init(this.pickerX(), this.pickerY(), this.pickerWidth(), this.pickerHeight()), 16);
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics paletteScrollbarMetrics() {
      int pickerX = this.pickerX();
      int pickerY = this.pickerY();
      int pickerW = this.pickerWidth();
      int pickerH = this.pickerHeight();
      int gridX = pickerX + 6;
      int gridY = pickerY + 42;
      int gridW = Math.max(1, pickerW - 19);
      int gridH = Math.max(1, pickerH - 48);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(this.paletteContentHeight(), gridH, pickerX + pickerW - 9, gridY, 3, gridH, this.pickerScroll);
   }

   private boolean isOverPalette(int mouseX, int mouseY) {
      if (!this.isPaletteOpen()) {
         return false;
      }

      int pickerX = this.pickerX();
      int pickerY = this.pickerY();
      int pickerW = this.pickerWidth();
      int pickerH = this.pickerHeight();
      return mouseX >= pickerX && mouseX < pickerX + pickerW && mouseY >= pickerY && mouseY < pickerY + pickerH;
   }

   private boolean isPaletteOpen() {
      return this.paletteMode != null;
   }

   private void closePalette() {
      OverlayManager.getInstance().unregister(this.paletteOverlay);
      this.paletteMode = null;
      this.paletteCategories = Collections.emptyList();
      this.filteredPaletteEntries = Collections.emptyList();
      this.paletteEntriesByCategory = Collections.emptyMap();
      this.paletteSearch.setItems(Collections.emptyList());
      if (this.paletteSearchField != null) {
         this.paletteSearchField.setFocused(false);
      }

      this.paletteSearchField = null;
      this.pickerScroll = 0;
   }

   private void addPaletteEntry(List<AutismMacroEditorOverlay.PaletteEntry> entries, String categoryId, String label, String description, Runnable action) {
      entries.add(new AutismMacroEditorOverlay.PaletteEntry(categoryId, label, description, action));
   }

   private void addRotateAction() {
      if (MC.player != null) {
         this.appendAction(new autismclient.util.macro.RotateAction(MC.player.getYRot(), MC.player.getXRot(), false, 6));
      } else {
         this.appendAction(new autismclient.util.macro.RotateAction(0.0F, 0.0F, false, 6));
      }
   }

   private void addLookAtBlockAction() {
      autismclient.util.macro.LookAtBlockAction action = new autismclient.util.macro.LookAtBlockAction();
      if (MC.hitResult != null && MC.hitResult.getType() == Type.ENTITY && MC.crosshairPickEntity != null) {
         action.targetMode = autismclient.util.macro.LookAtBlockAction.TargetMode.ENTITY;
         action.entityIds.add(autismclient.util.macro.LookAtBlockAction.entityRef(MC.crosshairPickEntity));
      } else if (MC.hitResult != null && MC.hitResult.getType() == Type.BLOCK) {
         BlockHitResult hit = (BlockHitResult)MC.hitResult;
         action.targetMode = autismclient.util.macro.LookAtBlockAction.TargetMode.SPECIFIC;
         action.blockX = hit.getBlockPos().getX();
         action.blockY = hit.getBlockPos().getY();
         action.blockZ = hit.getBlockPos().getZ();
      }

      this.appendAction(action);
   }

   private boolean isEditable(MacroAction action) {
      return ActionEditorOverlay.isEditable(action);
   }

   private boolean isWaitAction(MacroAction action) {
      return action instanceof autismclient.util.macro.WaitForHealthAction
         || action instanceof autismclient.util.macro.WaitForBlockAction
         || action instanceof autismclient.util.macro.WaitForPacketAction
         || action instanceof autismclient.util.macro.WaitForGuiAction
         || action instanceof autismclient.util.macro.TickSyncAction
         || action instanceof autismclient.util.macro.RevisionSyncAction
         || action instanceof autismclient.util.macro.ServerTickSyncAction
         || action instanceof autismclient.util.macro.WaitForCooldownAction
         || action instanceof autismclient.util.macro.WaitPosAction
         || action instanceof autismclient.util.macro.WaitForChatAction
         || action instanceof autismclient.util.macro.WaitForEntityAction
         || action instanceof autismclient.util.macro.WaitForSlotChangeAction
         || action instanceof autismclient.util.macro.WaitForSoundAction
         || action instanceof autismclient.util.macro.WaitForWorldChangeAction
         || action instanceof autismclient.util.macro.WaitForPositionDeltaAction
         || action instanceof autismclient.util.macro.WaitForTeleportAction
         || MacroActionRegistry.isCondition(action.getTypeId());
   }

   private int read_I_2(int bodyIndex) {
      if (this.macro != null && bodyIndex > 0 && bodyIndex < this.macro.actions.size()) {
         for (int i = bodyIndex - 1; i >= 0; i--) {
            MacroAction action = this.macro.actions.get(i);
            if (action instanceof autismclient.util.macro.RaceAction race) {
               int end = i + race.countBodyActions(this.macro.actions, i);
               return bodyIndex <= end ? i : -1;
            }

            if (action instanceof autismclient.util.macro.ReportAction) {
               return -1;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private int write_I_2(int bodyIndex) {
      if (this.macro != null && bodyIndex > 0 && bodyIndex < this.macro.actions.size()) {
         for (int i = bodyIndex - 1; i >= 0; i--) {
            MacroAction action = this.macro.actions.get(i);
            if (action instanceof autismclient.util.macro.ReportAction report) {
               int end = i + report.coveredCount(this.macro.actions, i);
               return bodyIndex <= end ? i : -1;
            }

            if (action instanceof autismclient.util.macro.RaceAction) {
               return -1;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private boolean flush_Z(int index) {
      return this.read_I_2(index) >= 0 || this.write_I_2(index) >= 0;
   }

   private int update_I_2(int bodyIndex) {
      if (this.macro != null && bodyIndex > 0 && bodyIndex < this.macro.actions.size()) {
         for (int i = bodyIndex - 1; i >= 0; i--) {
            MacroAction action = this.macro.actions.get(i);
            if (action instanceof autismclient.util.macro.MultiAction multi) {
               int end = i + multi.init(this.macro.actions, i);
               return bodyIndex <= end ? i : -1;
            }

            if (action instanceof autismclient.util.macro.RaceAction || action instanceof autismclient.util.macro.ReportAction) {
               return -1;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private Map<autismclient.util.macro.MultiAction, List<MacroAction>> init_Map() {
      Map<autismclient.util.macro.MultiAction, List<MacroAction>> coverage = new IdentityHashMap<>();
      if (this.macro == null) {
         return coverage;
      }

      List<MacroAction> actions = this.macro.actions;

      for (int i = 0; i < actions.size(); i++) {
         if (actions.get(i) instanceof autismclient.util.macro.MultiAction multi) {
            int count = multi.init(actions, i);
            coverage.put(multi, new ArrayList<>(actions.subList(i + 1, i + 1 + count)));
         }
      }

      return coverage;
   }

   private void init_V_Map_Set(Map<autismclient.util.macro.MultiAction, List<MacroAction>> before, Set<MacroAction> moved) {
      if (this.macro != null && before != null && !before.isEmpty()) {
         List<MacroAction> actions = this.macro.actions;

         for (Entry<autismclient.util.macro.MultiAction, List<MacroAction>> entry : before.entrySet()) {
            autismclient.util.macro.MultiAction multi = entry.getKey();
            int headerIndex = actions.indexOf(multi);
            if (headerIndex >= 0) {
               List<MacroAction> members = entry.getValue();
               int windowEnd = headerIndex + multi.init(actions, headerIndex);
               int lost = 0;

               for (MacroAction member : members) {
                  int pos = actions.indexOf(member);
                  if (pos <= headerIndex || pos > windowEnd) {
                     lost++;
                  }
               }

               int gained = 0;
               if (moved != null) {
                  for (MacroAction action : moved) {
                     if (!members.contains(action)
                        && !(action instanceof autismclient.util.macro.RaceAction)
                        && !(action instanceof autismclient.util.macro.ReportAction)
                        && !(action instanceof autismclient.util.macro.MultiAction)) {
                        int pos = actions.indexOf(action);
                        if (pos > headerIndex && pos <= windowEnd) {
                           gained++;
                        }
                     }
                  }
               }

               multi.stepCount = Math.max(0, multi.stepCount - lost + gained);
            }
         }
      }
   }

   private int sync_I(int headerIndex) {
      if (this.macro != null && headerIndex >= 0 && headerIndex < this.macro.actions.size()) {
         if (!(this.macro.actions.get(headerIndex) instanceof autismclient.util.macro.PacketGateAction gate)) {
            return 0;
         } else {
            if (gate.scope == autismclient.util.macro.PacketGateAction.GateScope.MACRO_PASS) {
               return 0;
            }

            for (int i = headerIndex + 1; i < this.macro.actions.size(); i++) {
               MacroAction action = this.macro.actions.get(i);
               if (action instanceof autismclient.util.macro.EndPacketGateAction) {
                  return i - headerIndex;
               }

               if (action instanceof autismclient.util.macro.PacketGateAction || action instanceof autismclient.util.macro.RaceAction || action instanceof autismclient.util.macro.ReportAction) {
                  break;
               }
            }

            return 0;
         }
      } else {
         return 0;
      }
   }

   private int flush_I(int bodyIndex) {
      if (this.macro != null && bodyIndex > 0 && bodyIndex < this.macro.actions.size()) {
         for (int i = bodyIndex - 1; i >= 0; i--) {
            MacroAction action = this.macro.actions.get(i);
            if (action instanceof autismclient.util.macro.PacketGateAction) {
               int end = i + this.sync_I(i);
               return bodyIndex <= end ? i : -1;
            }

            if (action instanceof autismclient.util.macro.EndPacketGateAction
               || action instanceof autismclient.util.macro.RaceAction
               || action instanceof autismclient.util.macro.ReportAction) {
               return -1;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private boolean close_Z(int index) {
      return this.macro != null
         && index >= 0
         && index < this.macro.actions.size()
         && this.macro.actions.get(index) instanceof autismclient.util.macro.EndPacketGateAction
         && this.flush_I(index) >= 0;
   }

   private void sync_V_2(int fromIndex, int toIndex) {
      if (this.macro != null && fromIndex >= 0 && fromIndex < this.macro.actions.size()) {
         int header = this.flush_I(fromIndex);
         if (header >= 0) {
            int target = Math.max(header + 1, Math.min(toIndex, this.macro.actions.size() - 1));
            if (target != fromIndex) {
               MacroAction end = this.macro.actions.remove(fromIndex);
               target = Math.max(header + 1, Math.min(target, this.macro.actions.size()));
               this.macro.actions.add(target, end);
               this.size_V();
               this.onActionsChanged();
            }
         }
      }
   }

   private void init_V_B(autismclient.util.macro.PacketGateAction gate) {
      if (this.macro != null && gate != null) {
         int index = this.macro.actions.indexOf(gate);
         if (index >= 0) {
            int end = index + this.sync_I(index);
            if (gate.scope == autismclient.util.macro.PacketGateAction.GateScope.MACRO_PASS) {
               if (end > index && end < this.macro.actions.size() && this.macro.actions.get(end) instanceof autismclient.util.macro.EndPacketGateAction) {
                  this.macro.actions.remove(end);
               }
            } else {
               if (end <= index || end >= this.macro.actions.size() || !(this.macro.actions.get(end) instanceof autismclient.util.macro.EndPacketGateAction)) {
                  this.macro.actions.add(index + 1, new autismclient.util.macro.EndPacketGateAction(gate.gateId));
               }

               this.size_V();
            }
         }
      }
   }

   private void size_V() {
      if (this.macro != null) {
         for (int i = 0; i < this.macro.actions.size(); i++) {
            if (this.macro.actions.get(i) instanceof autismclient.util.macro.PacketGateAction gate) {
               int end = i + this.sync_I(i);
               if (end > i && end < this.macro.actions.size() && this.macro.actions.get(end) instanceof autismclient.util.macro.EndPacketGateAction endGate) {
                  endGate.gateId = gate.gateId != null && !gate.gateId.isBlank() ? gate.gateId : "auto";
                  endGate.flushOnDisable = gate.flushOnDisable;
               }
            }
         }
      }
   }

   private AutismMacroEditorOverlay.BlockGroupLayout init_AutismMacroEditorO_I_MacroAction(int index, MacroAction action) {
      if (this.macro == null || action == null || index < 0 || index >= this.macro.actions.size()) {
         return AutismMacroEditorOverlay.BlockGroupLayout.NONE;
      } else if (action instanceof autismclient.util.macro.RaceAction race) {
         return new AutismMacroEditorOverlay.BlockGroupLayout(true, false, false, index, race.countBodyActions(this.macro.actions, index), 0, "RACE");
      } else if (action instanceof autismclient.util.macro.ReportAction report) {
         return new AutismMacroEditorOverlay.BlockGroupLayout(true, true, false, index, report.coveredCount(this.macro.actions, index), 0, "REPORT");
      } else if (action instanceof autismclient.util.macro.PacketGateAction) {
         return new AutismMacroEditorOverlay.BlockGroupLayout(true, false, true, index, this.sync_I(index), 0, "GATE START");
      } else if (action instanceof autismclient.util.macro.MultiAction multi) {
         return new AutismMacroEditorOverlay.BlockGroupLayout(true, false, false, true, index, multi.init(this.macro.actions, index), 0, "MULTI");
      } else {
         int raceHeader = this.read_I_2(index);
         if (raceHeader >= 0 && this.macro.actions.get(raceHeader) instanceof autismclient.util.macro.RaceAction race) {
            int childCount = race.countBodyActions(this.macro.actions, raceHeader);
            int conditionCount = race.load(this.macro.actions, raceHeader);
            int offset = index - raceHeader;
            return new AutismMacroEditorOverlay.BlockGroupLayout(
               false, false, false, raceHeader, childCount, offset, offset <= conditionCount ? "CONDITION" : "ACTION"
            );
         } else {
            int reportHeader = this.write_I_2(index);
            if (reportHeader >= 0 && this.macro.actions.get(reportHeader) instanceof autismclient.util.macro.ReportAction report) {
               int childCount = report.coveredCount(this.macro.actions, reportHeader);
               int offset = index - reportHeader;
               return new AutismMacroEditorOverlay.BlockGroupLayout(false, true, false, reportHeader, childCount, offset, offset == 1 ? "START" : "END");
            } else {
               int gateHeader = this.flush_I(index);
               if (gateHeader >= 0) {
                  int childCount = this.sync_I(gateHeader);
                  int offset = index - gateHeader;
                  String role = action instanceof autismclient.util.macro.EndPacketGateAction ? "GATE END" : "GATED";
                  return new AutismMacroEditorOverlay.BlockGroupLayout(false, false, true, gateHeader, childCount, offset, role);
               } else {
                  int multiHeader = this.update_I_2(index);
                  if (multiHeader >= 0 && this.macro.actions.get(multiHeader) instanceof autismclient.util.macro.MultiAction multi) {
                     int childCount = multi.init(this.macro.actions, multiHeader);
                     int offset = index - multiHeader;
                     return new AutismMacroEditorOverlay.BlockGroupLayout(false, false, false, true, multiHeader, childCount, offset, "SKIP");
                  } else {
                     return AutismMacroEditorOverlay.BlockGroupLayout.NONE;
                  }
               }
            }
         }
      }
   }

   private boolean opensBlock(MacroAction action) {
      if (action == null || !action.isEnabled()) {
         return false;
      } else if (action instanceof autismclient.util.macro.FpsAction fps) {
         return fps.getEndMode() != autismclient.util.macro.FpsAction.Inner_DataUtil.TIMEOUT;
      } else {
         return action instanceof autismclient.util.macro.SPingAction sping ? sping.getEndMode() != autismclient.util.macro.SPingAction.Inner_DataUtil.TIMEOUT : false;
      }
   }

   private int close_I(int headerIndex) {
      if (this.macro != null && headerIndex >= 0 && headerIndex < this.macro.actions.size()) {
         MacroAction action = this.macro.actions.get(headerIndex);
         int last = this.macro.actions.size() - 1;
         int covered;
         if (action instanceof autismclient.util.macro.FpsAction fps) {
            covered = fps.getEndMode() == autismclient.util.macro.FpsAction.Inner_DataUtil.END ? last - headerIndex : fps.getStepCount();
         } else {
            if (!(action instanceof autismclient.util.macro.SPingAction sping)) {
               return headerIndex;
            }

            covered = sping.getEndMode() == autismclient.util.macro.SPingAction.Inner_DataUtil.END ? last - headerIndex : sping.getStepCount();
         }

         long end = (long)headerIndex + Math.max(0, covered);
         return (int)Math.max(headerIndex, Math.min(last, end));
      } else {
         return headerIndex;
      }
   }

   private int blockHeaderIndex(int index) {
      if (this.macro != null && index >= 0 && index < this.macro.actions.size()) {
         for (int h = index; h >= 0; h--) {
            MacroAction a = this.macro.actions.get(h);
            if (this.opensBlock(a) && (h == index || index <= this.close_I(h))) {
               return h;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private AutismMacroEditorOverlay.RowHighlightFlags init_AutismMacroEditorO_I(int index) {
      int header = this.blockHeaderIndex(index);
      if (header < 0) {
         return AutismMacroEditorOverlay.RowHighlightFlags.NONE;
      }

      boolean ping = this.macro.actions.get(header) instanceof autismclient.util.macro.SPingAction;
      boolean isHeaderRow = header == index;
      boolean first = index == 0 || this.blockHeaderIndex(index - 1) < 0;
      boolean last = index >= this.macro.actions.size() - 1 || this.blockHeaderIndex(index + 1) < 0;
      return new AutismMacroEditorOverlay.RowHighlightFlags(true, isHeaderRow, ping, first, last);
   }

   private void addOpenContainerAction() {
      autismclient.util.macro.OpenContainerAction action = new autismclient.util.macro.OpenContainerAction();
      if (MC.hitResult instanceof EntityHitResult) {
         action.captureCrosshairTarget(MC);
      } else if (MC.hitResult instanceof BlockHitResult hit) {
         action.blockPos = hit.getBlockPos();
      }

      this.appendAction(action);
   }

   private void addInteractEntityAction() {
      autismclient.util.macro.InteractEntityAction action = new autismclient.util.macro.InteractEntityAction();
      action.captureCrosshairTarget(MC);
      this.appendAction(action);
   }

   private void addOpenInventoryAction() {
      autismclient.util.macro.InventoryAction action = new autismclient.util.macro.InventoryAction();
      action.mode = autismclient.util.macro.InventoryAction.InventoryMode.OPEN;
      this.appendAction(action);
   }

   private void addDelayPacketsAction() {
      autismclient.util.macro.DelayPacketsAction action = new autismclient.util.macro.DelayPacketsAction();
      if (AutismSessionState.getInstance().isUseCustomPackets()) {
         action.write();
      } else {
         action.resetDefaults();
      }

      this.appendAction(action);
   }

   private void addWaitForBlockAction() {
      autismclient.util.macro.WaitForBlockAction action = new autismclient.util.macro.WaitForBlockAction();
      if (MC.hitResult != null && MC.hitResult.getType() == Type.BLOCK) {
         BlockHitResult hit = (BlockHitResult)MC.hitResult;
         action.blockPos = hit.getBlockPos();
      }

      this.appendAction(action);
   }

   private void addWaitForEntityAction() {
      autismclient.util.macro.WaitForEntityAction action = new autismclient.util.macro.WaitForEntityAction();
      if (MC.player != null) {
         action.f_x_D = Math.round(MC.player.getX());
         action.f_y_D = Math.round(MC.player.getY());
         action.f_z_D = Math.round(MC.player.getZ());
      }

      this.appendAction(action);
   }

   private void addInstaBreakAction() {
      autismclient.util.macro.InstaBreakAction action = new autismclient.util.macro.InstaBreakAction();
      if (MC.hitResult != null && MC.hitResult.getType() == Type.BLOCK) {
         BlockHitResult hit = (BlockHitResult)MC.hitResult;
         action.blockPos = hit.getBlockPos();
         action.direction = hit.getDirection();
      }

      this.appendAction(action);
   }

   private void addBreakAction() {
      autismclient.util.macro.BreakAction action = new autismclient.util.macro.BreakAction();
      if (MC.hitResult != null && MC.hitResult.getType() == Type.BLOCK) {
         BlockHitResult hit = (BlockHitResult)MC.hitResult;
         action.blockPos = hit.getBlockPos();
         action.direction = hit.getDirection();
      }

      this.appendAction(action);
   }

   private List<MacroAction> update_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.AssertAction assertBundle = new autismclient.util.macro.AssertAction();
      assertBundle.check = autismclient.util.macro.AssertAction.CheckKind.HAS_BUNDLE;
      assertBundle.message = "Must have a bundle";
      actions.add(assertBundle);
      autismclient.util.macro.DisconnectAction dupe = new autismclient.util.macro.DisconnectAction();
      dupe.mode = autismclient.util.macro.DisconnectAction.DisconnectMode.KICK_DUPE;
      dupe.lagMethod = autismclient.util.macro.DisconnectAction.LagMethod.CLICK_SLOT;
      dupe.kickMethod = autismclient.util.macro.DisconnectAction.KickMethod.HURT;
      dupe.packetCount = 200;
      actions.add(dupe);
      return actions;
   }

   private List<MacroAction> rollbackPreset() {
      return new ArrayList<>(List.of(new autismclient.util.macro.RollbackAction()));
   }

   private List<MacroAction> flush_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.SelectSlotAction selectSlotOne = new autismclient.util.macro.SelectSlotAction();
      selectSlotOne.slot = 0;
      actions.add(selectSlotOne);
      autismclient.util.macro.AssertAction assertBundle = new autismclient.util.macro.AssertAction();
      assertBundle.check = autismclient.util.macro.AssertAction.CheckKind.BUNDLE_V2_READY;
      assertBundle.message = "Slot 1 must contain a one-item bundle and no container GUI can be open";
      actions.add(assertBundle);
      actions.add(new autismclient.util.macro.BundleDupeV2Action());
      return actions;
   }

   private List<MacroAction> close_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.NbtBookAction book = new autismclient.util.macro.NbtBookAction();
      book.pages = 1;
      book.customComponent = "DupersUnited?";
      book.title = "NeedToHave42CharsInHereHAHA123456789012345";
      book.delayTicks = 0;
      book.bookCount = 1;
      book.requireHeldWritableBook = true;
      book.dropInventoryBefore = true;
      book.disconnectAfter = true;
      actions.add(book);
      return actions;
   }

   private List<MacroAction> open_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.WaitForGuiAction waitGui = new autismclient.util.macro.WaitForGuiAction();
      waitGui.guiType = "CONTAINER";
      actions.add(waitGui);
      autismclient.util.macro.InstaBreakAction mine = new autismclient.util.macro.InstaBreakAction();
      if (MC.hitResult != null && MC.hitResult.getType() == Type.BLOCK) {
         BlockHitResult hit = (BlockHitResult)MC.hitResult;
         mine.blockPos = hit.getBlockPos();
         mine.direction = hit.getDirection();
      }

      mine.autoPickaxe = true;
      mine.times = 1;
      mine.delayTicks = 0;
      actions.add(mine);
      autismclient.util.macro.ContainerClickSequenceAction clicks = new autismclient.util.macro.ContainerClickSequenceAction();
      clicks.slotSource = autismclient.util.macro.ContainerClickSequenceAction.Inner_StreamUtil.RANGE;
      clicks.startSlot = 100;
      clicks.endSlot = 126;
      clicks.containerInput = "QUICK_MOVE";
      actions.add(clicks);
      return actions;
   }

   private List<MacroAction> poll_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.SelectSlotAction select = new autismclient.util.macro.SelectSlotAction();
      select.itemName = "Trident or bow";
      select.strategy = autismclient.util.macro.SelectSlotAction.Inner_DataUtil.BEST_DURABILITY;
      actions.add(select);
      autismclient.util.macro.UseItemPhaseAction phase = new autismclient.util.macro.UseItemPhaseAction();
      phase.phase = autismclient.util.macro.UseItemPhaseAction.UsePhase.START_USE;
      phase.holdTicks = 100;
      phase.gateDuringHold = true;
      phase.gatePlayerActions = true;
      phase.gateContainerClicks = true;
      phase.useCustomSlotMapping = true;
      phase.swapSlotBeforeRelease = 103;
      phase.swapButton = 0;
      phase.releaseAfterHold = true;
      phase.dropSlotAfterRelease = true;
      phase.dropSlot = 8;
      phase.gateId = "trident-dupe";
      actions.add(phase);
      return actions;
   }

   private List<MacroAction> push_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.SignEditAction sign = new autismclient.util.macro.SignEditAction();
      sign.guiName = "SIGN";
      sign.waitForGuiBefore = true;
      sign.line1 = "1";
      sign.sendCommandAfter = true;
      sign.commandAfter = "a";
      actions.add(sign);
      return actions;
   }

   private List<MacroAction> pop_List_2() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.SignEditAction sign = new autismclient.util.macro.SignEditAction();
      sign.guiName = "SIGN";
      sign.waitForGuiBefore = true;
      sign.line1 = "e";
      sign.sendClosePacketAfter = true;
      sign.closePacketContainerId = 0;
      actions.add(sign);
      return actions;
   }

   private List<MacroAction> tick_List() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.AssertAction assertBundle = new autismclient.util.macro.AssertAction();
      assertBundle.check = autismclient.util.macro.AssertAction.CheckKind.HELD_ITEM;
      assertBundle.itemName = "Bundle";
      assertBundle.message = "Must hold a bundle";
      actions.add(assertBundle);
      autismclient.util.macro.PacketBurstAction burst = new autismclient.util.macro.PacketBurstAction();
      burst.mode = autismclient.util.macro.PacketBurstAction.BurstMode.BUNDLE_SELECT;
      burst.count = 1;
      burst.slot = -1;
      burst.bundleIndex = -1337;
      actions.add(burst);
      autismclient.util.macro.UseItemPhaseAction use = new autismclient.util.macro.UseItemPhaseAction();
      use.phase = autismclient.util.macro.UseItemPhaseAction.UsePhase.USE_ONCE;
      use.itemName = "Bundle";
      actions.add(use);
      return actions;
   }

   private List<MacroAction> villagerDupePreset() {
      List<MacroAction> actions = new ArrayList<>();
      autismclient.util.macro.VillagerDupeAction dupe = new autismclient.util.macro.VillagerDupeAction();
      dupe.count = 0;
      actions.add(dupe);
      return actions;
   }

   private void init_V_List_Str_2(List<MacroAction> actions, String gateId) {
      autismclient.util.macro.FinallyAction finallyAction = new autismclient.util.macro.FinallyAction();
      finallyAction.bodyCount = 1;
      actions.add(finallyAction);
      autismclient.util.macro.EndPacketGateAction disable = new autismclient.util.macro.EndPacketGateAction(gateId);
      actions.add(disable);
   }

   private List<AutismMacroEditorOverlay.PaletteEntry> paletteEntriesFor(AutismMacroEditorOverlay.PaletteSectionKind mode) {
      List<AutismMacroEditorOverlay.PaletteEntry> entries = new ArrayList<>();
      if (mode == AutismMacroEditorOverlay.PaletteSectionKind.ACTION) {
         this.addPaletteEntry(entries, "flow", "Chat", "Send a chat message or slash command.", () -> this.appendAction(new autismclient.util.macro.SendChatAction()));
         this.addPaletteEntry(
            entries, "flow", "Delay", "Pause for a time in ms or ticks.", () -> this.appendAction(new autismclient.util.macro.DelayAction(1000))
         );
         this.addPaletteEntry(
            entries,
            "flow",
            "FPS",
            "Cap max FPS (0 = freeze). End on a timer, after N following steps, or at the end of the macro.",
            () -> this.appendAction(new autismclient.util.macro.FpsAction())
         );
         this.addPaletteEntry(
            entries,
            "flow",
            "Ping",
            "Spoof your ping by adding latency (ms). End on a timer, after N following steps, or at the end of the macro.",
            () -> this.appendAction(new autismclient.util.macro.SPingAction())
         );
         this.addPaletteEntry(
            entries, "flow", "Repeat", "Repeat the next steps a chosen number of times.", () -> this.appendAction(new autismclient.util.macro.RepeatAction())
         );
         this.addPaletteEntry(
            entries,
            "flow",
            "If",
            "Run THEN steps when a condition holds, otherwise the ELSE steps.",
            () -> this.appendAction(new autismclient.util.macro.IfAction())
         );
         this.addPaletteEntry(
            entries,
            "flow",
            "Goto",
            "Jump: skip ahead, loop back, restart at top, go to a Label, or stop.",
            () -> this.appendAction(new autismclient.util.macro.FlowAction())
         );
         this.addPaletteEntry(
            entries,
            "flow",
            "Label",
            "Named anchor that Goto can jump to (build loops with Label + Goto).",
            () -> this.appendAction(new autismclient.util.macro.LabelAction())
         );
         this.addPaletteEntry(entries, "flow", "Stop", "Stop the macro immediately.", () -> this.appendAction(new autismclient.util.macro.StopMacroAction()));
         this.addPaletteEntry(entries, "network", "Send Toggle", "Turn packet sending on or off.", () -> this.appendAction(new autismclient.util.macro.SendToggleAction()));
         this.addPaletteEntry(entries, "network", "Delay Packets", "Queue packets until you flush them.", this::addDelayPacketsAction);
         this.addPaletteEntry(
            entries, "flow", "Branch", "Run or skip compact step groups.", () -> this.appendAction(new autismclient.util.macro.BranchAction())
         );
         this.addPaletteEntry(entries, "flow", "Finally", "Register cleanup rows.", () -> this.appendAction(new autismclient.util.macro.FinallyAction()));
         this.addPaletteEntry(entries, "flow", "Variables", "Set macro variables.", () -> this.appendAction(new autismclient.util.macro.MacroVariablesAction()));
         this.addPaletteEntry(entries, "flow", "Capture Value", "Reuse game text later.", () -> this.appendAction(new autismclient.util.macro.CaptureValueAction()));
         this.addPaletteEntry(
            entries,
            "flow",
            "GM",
            "Change game mode for real, or fake it client-side.",
            () -> this.appendAction(new autismclient.util.macro.FakeGamemodeAction())
         );
         this.addPaletteEntry(
            entries, "flow", "Save GUI", "Remember the current GUI for later restore.", () -> this.appendAction(new autismclient.util.macro.SaveGuiAction())
         );
         this.addPaletteEntry(entries, "flow", "Restore GUI", "Reopen the last saved GUI state.", () -> this.appendAction(new autismclient.util.macro.RestoreGuiAction()));
         this.addPaletteEntry(entries, "movement", "Rotate", "Set player yaw and pitch.", this::addRotateAction);
         this.addPaletteEntry(entries, "movement", "Look At", "Face a specific position, nearest block, or nearest entity.", this::addLookAtBlockAction);
         this.addPaletteEntry(entries, "movement", "Sneak", "Hold or release sneak.", () -> this.appendAction(new autismclient.util.macro.SneakAction()));
         this.addPaletteEntry(
            entries, "movement", "Jump", "Press jump for a chosen duration.", () -> this.appendAction(new autismclient.util.macro.JumpAction())
         );
         this.addPaletteEntry(entries, "movement", "Sprint", "Toggle sprint state.", () -> this.appendAction(new autismclient.util.macro.SprintAction()));
         this.addPaletteEntry(entries, "movement", "Move", "Walk in a direction for N ticks.", () -> this.appendAction(new autismclient.util.macro.MoveAction()));
         this.addPaletteEntry(
            entries, "movement", "VClip", "Vertical clip with configurable packet behavior.", () -> this.appendAction(new autismclient.util.macro.VClipAction())
         );
         this.addPaletteEntry(
            entries,
            "movement",
            "HClip",
            "Horizontal clip forward with configurable packet behavior.",
            () -> this.appendAction(new autismclient.util.macro.HClipAction())
         );
         this.addPaletteEntry(entries, "movement", "TP", "Ground-first HClip teleport.", () -> this.appendAction(new autismclient.util.macro.PacedTpAction()));
         this.addPaletteEntry(entries, "movement", "Go To", "Pathfind to a target.", () -> this.appendAction(new autismclient.util.macro.GoToAction()));
         if (!AutismLiteVariant.enabled()) {
            this.addPaletteEntry(
               entries, "movement", "Tools", "Run a tool, wait for it, or control it.", () -> this.appendAction(new autismclient.util.macro.ToolsAction())
            );
         }

         this.addPaletteEntry(entries, "movement", "Mine", "Mine target blocks autonomously.", () -> this.appendAction(new autismclient.util.macro.MineAction()));
         this.addPaletteEntry(
            entries,
            "interaction",
            "Item Click",
            "Click items or slots in open inventories.",
            () -> this.appendAction(new autismclient.util.macro.ItemAction())
         );
         this.addPaletteEntry(
            entries,
            "interaction",
            "Pick Up All",
            "Pick up matching stacks into the carried item.",
            () -> this.appendAction(new autismclient.util.macro.PickUpAllAction())
         );
         this.addPaletteEntry(
            entries, "interaction", "Use Item", "Use the held item or select one by name.", () -> this.appendAction(new autismclient.util.macro.UseItemAction())
         );
         this.addPaletteEntry(
            entries, "interaction", "Use Phase", "Start, release, swing, or use once.", () -> this.appendAction(new autismclient.util.macro.UseItemPhaseAction())
         );
         this.addPaletteEntry(entries, "inventory", "Open Inventory", "Open the player inventory screen.", this::addOpenInventoryAction);
         this.addPaletteEntry(
            entries, "inventory", "Slot", "Select a hotbar slot or item by name.", () -> this.appendAction(new autismclient.util.macro.SelectSlotAction())
         );
         this.addPaletteEntry(
            entries,
            "interaction",
            "Click Sequence",
            "Click slots, ranges, or lists.",
            () -> this.appendAction(new autismclient.util.macro.ContainerClickSequenceAction())
         );
         this.addPaletteEntry(
            entries,
            "inventory",
            "XCarry",
            "Store four grid items plus one cursor item, or cursor-only when selected.",
            () -> this.appendAction(new autismclient.util.macro.XCarryAction())
         );
         this.addPaletteEntry(
            entries, "inventory", "Drop", "Drop items from chosen slots.", () -> this.appendAction(new autismclient.util.macro.DropAction())
         );
         this.addPaletteEntry(entries, "inventory", "Swap", "Swap two inventory slots.", () -> this.appendAction(new autismclient.util.macro.SwapSlotsAction()));
         this.addPaletteEntry(entries, "interaction", "Open Container", "Open a targeted container block.", this::addOpenContainerAction);
         this.addPaletteEntry(entries, "interaction", "Interact Entity", "Right-click a captured entity or block.", this::addInteractEntityAction);
         this.addPaletteEntry(
            entries, "interaction", "Villager Dupe", "Shear villager trade items", () -> this.appendAction(new autismclient.util.macro.VillagerDupeAction())
         );
         this.addPaletteEntry(
            entries, "interaction", "Wait Villager Item", "Wait for a shearable item", () -> this.appendAction(new autismclient.util.macro.WaitVillagerItemAction())
         );
         this.addPaletteEntry(
            entries,
            "interaction",
            "Place",
            "Place a selected item at a captured block position and face.",
            () -> this.appendAction(new autismclient.util.macro.PlaceAction())
         );
         this.addPaletteEntry(
            entries, "inventory", "Store", "Move items between player and containers.", () -> this.appendAction(new autismclient.util.macro.StoreItemAction())
         );
         this.addPaletteEntry(
            entries,
            "inventory",
            "Inventory Audit",
            "Snapshot slots, compare after reopen, or run automated dupe tests.",
            () -> this.appendAction(new autismclient.util.macro.InventoryAuditAction())
         );
         this.addPaletteEntry(
            entries, "inventory", "Rollback", "Duplicate selected local container items.", () -> this.appendAction(new autismclient.util.macro.RollbackAction())
         );
         this.addPaletteEntry(
            entries, "inventory", "Trade", "Trade in the open villager screen.", () -> this.appendAction(new autismclient.util.macro.TradeAction())
         );
         this.addPaletteEntry(
            entries, "inventory", "Craft", "Craft one or more recipes.", () -> this.appendAction(new autismclient.util.macro.CraftAction())
         );
         this.addPaletteEntry(
            entries,
            "interaction",
            "Mouse Click",
            "Simulate mouse clicks in world.",
            () -> this.appendAction(new autismclient.util.macro.ClickAction(autismclient.util.macro.ClickAction.MouseButton.RIGHT))
         );
         this.addPaletteEntry(
            entries, "network", "Packet", "Send captured packets from the queue.", () -> this.appendAction(new autismclient.util.macro.SendPacketAction())
         );
         this.addPaletteEntry(entries, "network", "Packet Gate", "Cancel, delay, or allow packet groups.", () -> {
            autismclient.util.macro.PacketGateAction g = new autismclient.util.macro.PacketGateAction();
            g.gateId = UUID.randomUUID().toString();
            boolean wholePassDefault = this.macro != null && this.macro.loop && this.macro.actions.isEmpty();
            if (wholePassDefault) {
               g.scope = autismclient.util.macro.PacketGateAction.GateScope.MACRO_PASS;
            }

            this.appendAction(g);
            if (!wholePassDefault) {
               this.appendAction(new autismclient.util.macro.EndPacketGateAction(g.gateId));
            }
         });
         this.addPaletteEntry(entries, "network", "Packet Burst", "Generate packet bursts.", () -> this.appendAction(new autismclient.util.macro.PacketBurstAction()));
         this.addPaletteEntry(
            entries,
            "network",
            "Custom Menu",
            "Fill and submit a server-defined dialog or form.",
            () -> this.appendAction(new autismclient.util.macro.CustomMenuAction())
         );
         this.addPaletteEntry(
            entries,
            "network",
            "Payload",
            "Send and edit custom payload packets with raw bytes or runtime Java.",
            () -> this.appendAction(new autismclient.util.macro.PayloadAction())
         );
         this.addPaletteEntry(
            entries,
            "network",
            "Close GUI",
            "Close the current screen with optional no-packet mode.",
            () -> this.appendAction(new autismclient.util.macro.CloseGuiAction())
         );
         this.addPaletteEntry(
            entries,
            "network",
            "Desync",
            "Create GUI desync without closing locally.",
            () -> this.appendAction(new autismclient.util.macro.DesyncAction())
         );
         this.addPaletteEntry(entries, "network", "NBT Book", "Write large custom books.", () -> this.appendAction(new autismclient.util.macro.NbtBookAction()));
         this.addPaletteEntry(entries, "network", "Sign Edit", "Send sign update lines.", () -> this.appendAction(new autismclient.util.macro.SignEditAction()));
         this.addPaletteEntry(
            entries,
            "network",
            "Disconnect",
            "Disconnect or kick using selected mode.",
            () -> this.appendAction(new autismclient.util.macro.DisconnectAction())
         );
         this.addPaletteEntry(
            entries,
            "network",
            "Join",
            "Join a server (blank = last) and continue once in (Playable by default).",
            () -> this.appendAction(new autismclient.util.macro.JoinAction())
         );
         this.addPaletteEntry(entries, "flow", "Module", "Toggle AUTISM modules.", () -> this.appendAction(new autismclient.util.macro.ak()));
         this.addPaletteEntry(entries, "flow", "Assert", "Validate preconditions.", () -> this.appendAction(new autismclient.util.macro.AssertAction()));
         this.addPaletteEntry(entries, "flow", "Pay", "Send payments to a player list.", () -> this.appendAction(new autismclient.util.macro.PayAction()));
         this.addPaletteEntry(entries, "interaction", "InstaBreak", "Instant rebreak for one captured block.", this::addInstaBreakAction);
         this.addPaletteEntry(entries, "interaction", "Break", "Legit vanilla break with break-progress tracking + optional GUI-race interact.", this::addBreakAction);
         this.addPaletteEntry(
            entries, "flow", "Race", "Run grouped actions when a condition fires.", () -> this.appendAction(new autismclient.util.macro.RaceAction())
         );
         this.addPaletteEntry(
            entries,
            "flow",
            "Report",
            "Measure time between macro, packet, GUI, teleport, or position events.",
            () -> this.appendAction(new autismclient.util.macro.ReportAction())
         );
         this.addPaletteEntry(
            entries, "flow", "Start Macro", "Start another macro from the macro library.", () -> this.appendAction(new autismclient.util.macro.StartMacroAction())
         );
         this.addPaletteEntry(
            entries, "flow", "Stop Macro", "Stop this, all, or a selected macro.", () -> this.appendAction(new autismclient.util.macro.StopMacroAction())
         );
      } else if (mode == AutismMacroEditorOverlay.PaletteSectionKind.PRESET) {
         this.addPaletteEntry(entries, "singleplayer", "Rollback", "Local container duplicate", () -> this.insertActions(this.rollbackPreset()));
         this.addPaletteEntry(entries, "dupe", "Bundle Dupe V3", "Paper / Spigot", () -> this.insertActions(this.update_List_2()));
         this.addPaletteEntry(entries, "dupe", "Bundle Dupe V2", "Paper / Spigot", () -> this.insertActions(this.flush_List_2()));
         this.addPaletteEntry(entries, "dupe", "Book Dupe", "Paper < 1.21.1", () -> this.insertActions(this.close_List_2()));
         this.addPaletteEntry(entries, "dupe", "Shulker Dupe", "Vanilla 1.19 & below", () -> this.insertActions(this.open_List_2()));
         this.addPaletteEntry(entries, "dupe", "Trident Dupe", "Vanilla < 1.19", () -> this.insertActions(this.poll_List_2()));
         this.addPaletteEntry(entries, "dupe", "Villager Dupe", "Saddle / armor / carpet", () -> this.insertActions(this.villagerDupePreset()));
         this.addPaletteEntry(entries, "trade", "AxTrade Sign", "Patched", () -> this.insertActions(this.push_List_2()));
         this.addPaletteEntry(entries, "trade", "LoverFella Sign", "Patched", () -> this.insertActions(this.pop_List_2()));
         this.addPaletteEntry(entries, "crash", "Bundle Crash", "1.21.4", () -> this.insertActions(this.tick_List()));
      } else {
         this.addPaletteEntry(
            entries,
            "player",
            "Health",
            "Pause until your health drops below or rises above a target.",
            () -> this.appendAction(new autismclient.util.macro.WaitForHealthAction(20.0F, true))
         );
         this.addPaletteEntry(
            entries, "player", "Cooldown", "Pause until an item cooldown is ready.", () -> this.appendAction(new autismclient.util.macro.WaitForCooldownAction())
         );
         this.addPaletteEntry(
            entries,
            "player",
            "Gamemode",
            "Pause until real or optionally fake gamemode changes.",
            () -> this.appendAction(new autismclient.util.macro.WaitGamemodeChangeAction())
         );
         this.addPaletteEntry(
            entries,
            "inventory",
            "Slot or Item",
            "Pause until slots or inventory contents change.",
            () -> this.appendAction(new autismclient.util.macro.WaitForSlotChangeAction())
         );
         this.addPaletteEntry(
            entries,
            "inventory",
            "Durability",
            "Pause until a held, selected, or slotted item reaches durability.",
            () -> this.appendAction(new autismclient.util.macro.WaitDurabilityAction())
         );
         this.addPaletteEntry(
            entries,
            "inventory",
            "Free Slots",
            "Pause until free or filled inventory slots match.",
            () -> this.appendAction(new autismclient.util.macro.WaitFreeSlotsAction())
         );
         this.addPaletteEntry(
            entries,
            "world",
            "Movement",
            "Pause until a position, move-distance, world change, or teleport — pick the type inside.",
            () -> this.appendAction(new autismclient.util.macro.WaitMovementAction())
         );
         this.addPaletteEntry(entries, "world", "Block", "Pause until a block is placed or broken.", this::addWaitForBlockAction);
         this.addPaletteEntry(entries, "world", "Entity", "Pause until a matching entity appears.", this::addWaitForEntityAction);
         this.addPaletteEntry(entries, "world", "Sound", "Pause until a matching sound plays.", () -> this.appendAction(new autismclient.util.macro.WaitForSoundAction()));
         this.addPaletteEntry(entries, "gui_chat", "GUI", "Pause until a GUI opens or closes.", () -> this.appendAction(new autismclient.util.macro.WaitForGuiAction()));
         this.addPaletteEntry(entries, "gui_chat", "Chat", "Pause until chat matches a pattern.", () -> this.appendAction(new autismclient.util.macro.WaitForChatAction()));
         this.addPaletteEntry(entries, "sync", "LAN", "Pause until a LAN peer reaches a step.", () -> this.appendAction(new autismclient.util.macro.WaitForLanStepAction()));
         this.addPaletteEntry(
            entries,
            "sync",
            "Macro",
            "Pause until another macro starts, completes a step, or finishes.",
            () -> this.appendAction(new autismclient.util.macro.WaitForMacroStepAction())
         );
         this.addPaletteEntry(
            entries,
            "packets",
            "Packet",
            "Pause until a chosen C2S or S2C packet appears.",
            () -> this.appendAction(new autismclient.util.macro.WaitForPacketAction(""))
         );
         this.addPaletteEntry(
            entries, "packets", "Packet Match", "Pause for packet plus field predicate.", () -> this.appendAction(new autismclient.util.macro.WaitPacketMatchAction())
         );
         this.addPaletteEntry(
            entries,
            "inventory",
            "Inventory",
            "Pause for item, count, slot, cursor, or fullness.",
            () -> this.appendAction(new autismclient.util.macro.WaitInventoryPredicateAction())
         );
         this.addPaletteEntry(entries, "sync", "Tick Sync", "Wait for the next client tick.", () -> this.appendAction(new autismclient.util.macro.TickSyncAction()));
         this.addPaletteEntry(entries, "sync", "Revision Sync", "Wait for handler revision sync.", () -> this.appendAction(new autismclient.util.macro.RevisionSyncAction()));
         this.addPaletteEntry(
            entries, "sync", "Server Sync", "Wait for the server tick tracker.", () -> this.appendAction(new autismclient.util.macro.ServerTickSyncAction())
         );
      }

      if (mode == AutismMacroEditorOverlay.PaletteSectionKind.ACTION) {
         this.addRegistryEntries(entries, false);
      } else if (mode == AutismMacroEditorOverlay.PaletteSectionKind.PRESET) {
         this.addPresetEntries(entries);
      } else {
         this.addRegistryEntries(entries, true);
      }

      return entries;
   }

   private void addRegistryEntries(List<AutismMacroEditorOverlay.PaletteEntry> entries, boolean conditions) {
      for (MacroActionEntry entry : MacroActionRegistry.entries()) {
         if (entry.hasPicker() && entry.isCondition() == conditions) {
            Supplier<MacroAction> factory = entry.factory();
            this.addPaletteEntry(entries, entry.pickerCategory(), entry.pickerLabel(), entry.pickerTip(), () -> {
               MacroAction action;
               try {
                  action = factory.get();
               } catch (Throwable t) {
                  AutismClientAddon.LOG.warn("[MacroActions] Addon step '{}' factory failed", entry.pickerLabel(), t);
                  return;
               }

               if (action != null) {
                  this.appendAction(action);
               }
            });
         }
      }
   }

   private void addPresetEntries(List<AutismMacroEditorOverlay.PaletteEntry> entries) {
      for (MacroPresetRegistry.PresetEntry preset : MacroPresetRegistry.entries()) {
         Supplier<List<MacroAction>> builder = preset.builder();
         this.addPaletteEntry(entries, preset.categoryId(), preset.label(), preset.tip(), () -> {
            List<MacroAction> stack;
            try {
               stack = builder.get();
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[MacroPresets] Addon preset '{}' failed to build", preset.label(), t);
               AutismClientMessaging.sendPrefixed("§cAddon preset '" + preset.label() + "' failed.");
               return;
            }

            if (stack != null && !stack.isEmpty()) {
               this.insertActions(stack);
            }
         });
      }
   }

   private List<AutismMacroEditorOverlay.PaletteCategory> groupIntoCategories(
      AutismMacroEditorOverlay.PaletteSectionKind mode, List<AutismMacroEditorOverlay.PaletteEntry> entries
   ) {
      List<AutismMacroEditorOverlay.PaletteCategory> categories = new ArrayList<>();
      Consumer<AutismMacroEditorOverlay.PaletteCategory> addCategory = category -> {
         for (AutismMacroEditorOverlay.PaletteEntry entry : entries) {
            if (entry.categoryId.equals(category.id)) {
               categories.add(category);
               return;
            }
         }
      };
      if (mode == AutismMacroEditorOverlay.PaletteSectionKind.ACTION) {
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("flow", "Flow", -11352981));
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("movement", "Movement", -42406));
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("interaction", "Interaction", -12268476));
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("inventory", "Inventory", -11182));
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("network", "Packets", -10835457));
      } else if (mode == AutismMacroEditorOverlay.PaletteSectionKind.PRESET) {
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("singleplayer", "Singleplayer", -12268476));
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("dupe", "Duplication", -10835457));
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("trade", "Trade", -11182));
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("crash", "Crash", -4555521));
      } else {
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("player", "Player", -42406));
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("inventory", "Inventory", -11182));
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("world", "World", -11352981));
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("gui_chat", "GUI / Chat", -11182));
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("packets", "Packets", -10835457));
         addCategory.accept(new AutismMacroEditorOverlay.PaletteCategory("sync", "Sync", -10835457));
      }

      Set<String> known = new HashSet<>();

      for (AutismMacroEditorOverlay.PaletteCategory c : categories) {
         known.add(c.id);
      }

      LinkedHashSet<String> extra = new LinkedHashSet<>();

      for (AutismMacroEditorOverlay.PaletteEntry entry : entries) {
         if (!known.contains(entry.categoryId)) {
            extra.add(entry.categoryId);
         }
      }

      for (String catId : extra) {
         MacroActionRegistry.ActionCategory actionCat = MacroActionRegistry.category(catId);
         String label;
         int color;
         if (actionCat != null) {
            label = actionCat.label();
            color = actionCat.color();
         } else {
            String preset = MacroPresetRegistry.categoryLabel(catId);
            label = preset != null ? preset : load_Str_Str_2(catId);
            color = -5609729;
         }

         categories.add(new AutismMacroEditorOverlay.PaletteCategory(catId, label, color));
      }

      return categories;
   }

   private static String load_Str_Str_2(String id) {
      if (id != null && !id.isBlank()) {
         if (id.startsWith("addon:")) {
            String addonId = id.substring("addon:".length());
            int slash = addonId.indexOf(47);
            if (slash >= 0) {
               addonId = addonId.substring(0, slash);
            }

            autismclient.b.DataUtil.StreamUtil report = autismclient.b.DataUtil.init_DataUtil_StreamUti(addonId);
            return report != null && report.load_Str() != null && !report.load_Str().isBlank()
               ? autismclient.b.DataUtil.load_Str(report.load_Str(), addonId)
               : autismclient.b.DataUtil.load_Str(addonId, "Addon");
         } else {
            String[] parts = id.replace('_', ' ').replace('-', ' ').trim().split("\\s+");
            StringBuilder sb = new StringBuilder();

            for (String part : parts) {
               if (!part.isEmpty()) {
                  if (sb.length() > 0) {
                     sb.append(' ');
                  }

                  sb.append(Character.toUpperCase(part.charAt(0)));
                  if (part.length() > 1) {
                     sb.append(part.substring(1));
                  }
               }
            }

            return sb.length() == 0 ? "Addon" : sb.toString();
         }
      } else {
         return "Addon";
      }
   }

   private void openPalette(AutismMacroEditorOverlay.PaletteSectionKind mode) {
      if (!this.isStepListLocked() && mode != null) {
         this.resetDragState();
         this.paletteMode = mode;
         List<AutismMacroEditorOverlay.PaletteEntry> entries = this.paletteEntriesFor(mode);
         this.paletteSearch.setItems(entries);
         this.paletteSearch.setQuery("");
         this.paletteSearchField = new ThemedTextInputBox(MC, this.font, 0, 0, 1, 16, false);
         this.paletteSearchField.setPlaceholder(Component.literal("Search..."));
         this.paletteSearchField.setChangedListener(ignored -> this.refilterPalette());
         this.refilterPalette();
         this.pickerScroll = 0;
         this.listeningForKeybind = false;
         if (this.nameField != null) {
            this.nameField.setFocused(false);
         }

         if (this.loopCountField != null) {
            this.loopCountField.setFocused(false);
         }

         OverlayManager manager = OverlayManager.getInstance();
         manager.register(this.paletteOverlay);
         manager.bringToFront(this.paletteOverlay);
      }
   }

   private List<AutismMacroEditorOverlay.PaletteEntry> visiblePaletteEntries() {
      return !this.isPaletteOpen() ? Collections.emptyList() : this.filteredPaletteEntries;
   }

   private List<AutismMacroEditorOverlay.PaletteEntry> entriesInCategory(String categoryId) {
      return categoryId != null && !categoryId.isEmpty() ? this.paletteEntriesByCategory.getOrDefault(categoryId, Collections.emptyList()) : Collections.emptyList();
   }

   private void refilterPalette() {
      String query = this.paletteSearchField == null ? "" : this.paletteSearchField.getValue();
      this.paletteSearch.setQuery(query);
      this.filteredPaletteEntries = this.paletteSearch.filtered();
      this.paletteCategories = this.groupIntoCategories(this.paletteMode, this.filteredPaletteEntries);
      Map<String, List<AutismMacroEditorOverlay.PaletteEntry>> grouped = new LinkedHashMap<>();

      for (AutismMacroEditorOverlay.PaletteEntry entry : this.filteredPaletteEntries) {
         grouped.computeIfAbsent(entry.categoryId, ignored -> new ArrayList<>()).add(entry);
      }

      Map<String, List<AutismMacroEditorOverlay.PaletteEntry>> frozen = new LinkedHashMap<>();

      for (Entry<String, List<AutismMacroEditorOverlay.PaletteEntry>> entry : grouped.entrySet()) {
         frozen.put(entry.getKey(), List.copyOf(entry.getValue()));
      }

      this.paletteEntriesByCategory = Map.copyOf(frozen);
      this.paletteRevision++;
      this.pickerScroll = 0;
   }

   private void layoutPaletteSearchField() {
      if (this.paletteSearchField != null && this.isPaletteOpen()) {
         int pickerX = this.pickerX();
         int pickerY = this.pickerY();
         int pickerW = this.pickerWidth();
         this.paletteSearchField.setX(pickerX + 6);
         this.paletteSearchField.setY(pickerY + 21);
         this.paletteSearchField.setWidth(Math.max(1, pickerW - 12));
         this.paletteSearchField.setHeight(16);
      }
   }

   private int paletteColumnCount(int gridW) {
      int columnGap = 6;
      int minColumnW = 128;
      int maxColumns = 1;
      return Math.max(1, Math.min(maxColumns, (gridW + columnGap) / (minColumnW + columnGap)));
   }

   private int categoryHeight(List<AutismMacroEditorOverlay.PaletteEntry> entries, int columnW) {
      int cardGap = 3;
      int rowGap = 1;
      int cardH = 14;
      int sectionLabelH = this.sectionLabelHeight();
      int buttonW = Math.max(1, (columnW - cardGap) / 2);
      int buttonX = 0;
      int buttonY = sectionLabelH + 2;

      for (AutismMacroEditorOverlay.PaletteEntry entry : entries) {
         if (buttonX > 0 && buttonX + buttonW > columnW) {
            buttonX = 0;
            buttonY += cardH + rowGap;
         }

         buttonX += buttonW + cardGap;
      }

      return entries.isEmpty() ? sectionLabelH : buttonY + cardH;
   }

   private List<AutismMacroEditorOverlay.PaletteCategoryLayout> layoutPaletteCategories(int gridX, int startY, int gridW) {
      int cardGap = 3;
      int rowGap = 1;
      int cardH = 14;
      int sectionLabelH = this.sectionLabelHeight();
      int sectionGap = 3;
      int columnGap = 6;
      int columnCount = this.paletteColumnCount(gridW);
      int columnW = Math.max(1, (gridW - columnGap * (columnCount - 1)) / columnCount);
      int targetColumnHeight = Math.max(96, (MC.getWindow() != null ? cA.load_I() : 320) - 96);
      List<AutismMacroEditorOverlay.PaletteCategoryLayout> layouts = new ArrayList<>();
      int column = 0;
      int[] columnY = new int[columnCount];
      Arrays.fill(columnY, startY);

      for (AutismMacroEditorOverlay.PaletteCategory category : this.paletteCategories) {
         List<AutismMacroEditorOverlay.PaletteEntry> categoryEntries = this.entriesInCategory(category.id);
         if (!categoryEntries.isEmpty()) {
            int blockHeight = this.categoryHeight(categoryEntries, columnW);
            if (column < columnCount - 1 && columnY[column] > startY && columnY[column] + blockHeight > startY + targetColumnHeight) {
               column++;
            }

            int sectionX = gridX + column * (columnW + columnGap);
            int sectionY = columnY[column];
            int firstRowY = sectionY + sectionLabelH + 2;
            List<AutismMacroEditorOverlay.PaletteButtonBounds> buttons = this.layoutPaletteButtons(categoryEntries, sectionX, firstRowY, columnW, cardGap, cardH, rowGap);
            int bottomY = buttons.isEmpty() ? sectionY + sectionLabelH : buttons.get(buttons.size() - 1).y + cardH;
            layouts.add(new AutismMacroEditorOverlay.PaletteCategoryLayout(category, sectionX, sectionY, columnW, buttons, bottomY));
            columnY[column] = bottomY + sectionGap;
         }
      }

      return layouts;
   }

   private List<AutismMacroEditorOverlay.PaletteButtonBounds> layoutPaletteButtons(
      List<AutismMacroEditorOverlay.PaletteEntry> entries, int sectionX, int firstRowY, int columnW, int cardGap, int cardH, int rowGap
   ) {
      List<AutismMacroEditorOverlay.PaletteButtonBounds> buttons = new ArrayList<>();
      if (entries != null && !entries.isEmpty()) {
         List<AutismMacroEditorOverlay.PaletteEntry> ordered = this.read_List(entries);
         int minBtn = Math.min(34, Math.max(1, (columnW - cardGap) / 2));
         int y = firstRowY;

         for (int i = 0; i < ordered.size(); i += 2) {
            if (i + 1 >= ordered.size()) {
               buttons.add(new AutismMacroEditorOverlay.PaletteButtonBounds(ordered.get(i), sectionX, y, columnW, cardH));
            } else {
               int avail = Math.max(2 * minBtn, columnW - cardGap);
               int n0 = this.cachedLabelWidth(ordered.get(i).label) + 10;
               int n1 = this.cachedLabelWidth(ordered.get(i + 1).label) + 10;
               int total = Math.max(1, n0 + n1);
               int w0 = Math.round((float)avail * n0 / total);
               w0 = Math.max(minBtn, Math.min(avail - minBtn, w0));
               int w1 = avail - w0;
               buttons.add(new AutismMacroEditorOverlay.PaletteButtonBounds(ordered.get(i), sectionX, y, w0, cardH));
               buttons.add(new AutismMacroEditorOverlay.PaletteButtonBounds(ordered.get(i + 1), sectionX + w0 + cardGap, y, w1, cardH));
            }

            y += cardH + rowGap;
         }

         return buttons;
      } else {
         return buttons;
      }
   }

   private List<AutismMacroEditorOverlay.PaletteEntry> read_List(List<AutismMacroEditorOverlay.PaletteEntry> entries) {
      if (entries.size() <= 2) {
         return new ArrayList<>(entries);
      }

      List<AutismMacroEditorOverlay.PaletteEntry> sorted = new ArrayList<>(entries);
      sorted.sort(Comparator.comparingInt(e -> this.cachedLabelWidth(e.label)));
      List<AutismMacroEditorOverlay.PaletteEntry> ordered = new ArrayList<>(entries.size());
      int lo = 0;
      int hi = sorted.size() - 1;

      while (lo <= hi) {
         if (lo == hi) {
            ordered.add(sorted.get(lo));
            break;
         }

         ordered.add(sorted.get(lo++));
         ordered.add(sorted.get(hi--));
      }

      return ordered;
   }

   private int layoutsBottom(List<AutismMacroEditorOverlay.PaletteCategoryLayout> layouts) {
      int maxBottom = 0;
      if (layouts != null) {
         for (AutismMacroEditorOverlay.PaletteCategoryLayout layout : layouts) {
            maxBottom = Math.max(maxBottom, layout.bottomY);
         }
      }

      return Math.max(0, maxBottom);
   }

   private int load_I_List(List<AutismMacroEditorOverlay.PaletteCategoryLayout> layouts) {
      if (layouts != null && !layouts.isEmpty()) {
         int maxRight = 0;
         int minLeft = 2147483647;
         Identifier pickerFont = this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load);
         int pickerColor = this.styleTheme.load(autismclient.gui.f.b.l.read);

         for (AutismMacroEditorOverlay.PaletteCategoryLayout section : layouts) {
            minLeft = Math.min(minLeft, section.x);
            maxRight = Math.max(
               maxRight,
               section.x
                  + Math.min(section.width, autismclient.gui.f.b.k.init(this.font, section.category.label, pickerFont, pickerColor))
            );

            for (AutismMacroEditorOverlay.PaletteButtonBounds button : section.buttons) {
               minLeft = Math.min(minLeft, button.x);
               maxRight = Math.max(maxRight, button.x + button.width);
            }
         }

         if (minLeft == 2147483647) {
            minLeft = 0;
         }

         return Math.max(118, maxRight - minLeft);
      } else {
         return 118;
      }
   }

   @Override
   public int getMinWidth() {
      return this.minWindowWidth();
   }

   @Override
   public int getMinHeight() {
      return this.minWindowHeight();
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.editorX, this.editorY, this.editorWidth, this.editorHeight, this.visible, this.collapsed);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampToScreen(this, new OverlayBounds(bounds.x, bounds.y, this.preferredWindowWidth(), this.preferredWindowHeight(), bounds.visible, bounds.collapsed));
         this.editorX = clamped.x;
         this.editorY = clamped.y;
         this.editorWidth = clamped.width;
         this.editorHeight = clamped.height;
         this.visible = clamped.visible;
         this.collapsed = clamped.collapsed;
         if (this.macro != null) {
            this.rebuildButtons();
         }
      }
   }

   private CompoundTag macroTag() {
      return this.macro == null ? null : this.macro.toTag();
   }

   private static boolean tagsEqual(CompoundTag a, CompoundTag b) {
      if (a == b) {
         return true;
      } else {
         return a != null && b != null ? a.equals(b) : false;
      }
   }

   private AutismMacroEditorOverlay.UndoSnapshot captureUndoSnapshot() {
      CompoundTag snapshot = this.macroTag();
      if (snapshot == null) {
         return null;
      }

      List<Integer> selectedIndices = new ArrayList<>();
      if (this.macro != null && !this.selection.isEmpty()) {
         for (int i = 0; i < this.macro.actions.size(); i++) {
            if (this.isSelected(i)) {
               selectedIndices.add(i);
            }
         }
      }

      return new AutismMacroEditorOverlay.UndoSnapshot(snapshot, List.copyOf(selectedIndices));
   }

   private void pushHistory(Deque<AutismMacroEditorOverlay.UndoSnapshot> history, AutismMacroEditorOverlay.UndoSnapshot state) {
      if (history != null && state != null && state.macroTag != null) {
         AutismMacroEditorOverlay.UndoSnapshot head = history.peekFirst();
         if (head != null && tagsEqual(head.macroTag, state.macroTag)) {
            history.removeFirst();
         }

         history.addFirst(state);

         while (history.size() > 30) {
            history.removeLast();
         }
      }
   }

   private void clearHistory() {
      this.undoStack.clear();
      this.redoStack.clear();
   }

   private void recordUndo() {
      this.pushHistory(this.undoStack, this.captureUndoSnapshot());
      this.redoStack.clear();
   }

   private boolean canUndo() {
      return this.macro != null && !this.undoStack.isEmpty();
   }

   private boolean canRedo() {
      return this.macro != null && !this.redoStack.isEmpty();
   }

   private void restoreSnapshot(AutismMacroEditorOverlay.UndoSnapshot state) {
      if (this.macro != null && state != null && state.macroTag != null) {
         this.macro.fromTag(state.macroTag);
         this.onActionsChanged();
         this.selection.clear();

         for (int index : state.selectedIndices) {
            if (index >= 0 && index < this.macro.actions.size()) {
               this.selection.add(this.stableId(this.macro.actions.get(index)));
            }
         }

         this.pruneSelection();
      }
   }

   private void undo() {
      if (!this.isStepListLocked() && this.canUndo()) {
         AutismMacroEditorOverlay.UndoSnapshot previous = this.undoStack.pollFirst();
         if (previous != null) {
            this.pushHistory(this.redoStack, this.captureUndoSnapshot());
            this.restoreSnapshot(previous);
         }
      }
   }

   private void redo() {
      if (!this.isStepListLocked() && this.canRedo()) {
         AutismMacroEditorOverlay.UndoSnapshot next = this.redoStack.pollFirst();
         if (next != null) {
            this.pushHistory(this.undoStack, this.captureUndoSnapshot());
            this.restoreSnapshot(next);
         }
      }
   }

   public void openEditor(AutismMacro macroToEdit) {
      this.openReturningToList(macroToEdit, false);
   }

   public void openReturningToList(AutismMacro macroToEdit, boolean reopenMacroListOnClose) {
      this.openEditor(macroToEdit, reopenMacroListOnClose, AutismMacroEditorOverlay.EditorMode.NORMAL, null, false);
   }

   public void openSharedMacro(AutismMacro sharedMacro) {
      this.openEditor(sharedMacro, false, AutismMacroEditorOverlay.EditorMode.NORMAL, null, true);
   }

   public void openForJoinMacro(AutismMacro macroToEdit, Consumer<AutismMacro> onSaveComplete) {
      this.openEditor(macroToEdit, false, AutismMacroEditorOverlay.EditorMode.JOIN_MACRO_MENU, onSaveComplete, false);
   }

   public void openForEdit(AutismMacro macroToEdit, Consumer<AutismMacro> onSaveComplete) {
      this.openEditor(macroToEdit, false, AutismMacroEditorOverlay.EditorMode.NORMAL, onSaveComplete, false);
   }

   private void openEditor(
      AutismMacro macroToEdit, boolean reopenMacroListOnClose, AutismMacroEditorOverlay.EditorMode context, Consumer<AutismMacro> onSaveComplete, boolean forceNew
   ) {
      this.closingGhostMacro = null;
      if (this.visible) {
         this.closePalette();
         this.listeningForKeybind = false;
      }

      for (AutismOverlay overlay : OverlayManager.getInstance().getOverlays()) {
         if (overlay instanceof AutismMacroListOverlay macroListOverlay) {
            macroListOverlay.setVisible(false);
         }
      }

      this.originalMacro = null;
      this.macro = null;
      this.reopenMacroListOnClose = reopenMacroListOnClose;
      this.editorMode = context == null ? AutismMacroEditorOverlay.EditorMode.NORMAL : context;
      this.onSaveComplete = onSaveComplete;
      this.visible = true;
      AutismSessionState.getInstance().setMacroEditorVisible(true);
      ActionEditorOverlay.getInstance().setCaptureAllowed(this.isCaptureAllowed());
      if (macroToEdit == null) {
         this.macro = new AutismMacro("");
         this.originalMacro = null;
         this.isNewMacro = true;
      } else if (forceNew) {
         this.originalMacro = null;
         this.macro = new AutismMacro();
         this.macro.fromTag(macroToEdit.toTag());
         this.macro.name = "";
         this.macro.keyCode = -1;
         this.isNewMacro = true;
      } else {
         this.originalMacro = macroToEdit;
         this.macro = new AutismMacro();
         this.macro.fromTag(macroToEdit.toTag());
         this.isNewMacro = false;
      }

      AutismSessionState.getInstance().setEditingMacro(this.macro);
      this.oneShotRunId = -1L;
      this.invalidateRunState();
      this.tempSavedForRun = false;
      this.tempSavedAsNew = false;
      this.preRunBackup = null;
      this.stepListLocked = false;
      this.clearHistory();
      this.resetDragState();
      this.clearSelection();
      this.listScroll = 0;
      if (!this.macro.loop) {
         this.loopMode = 0;
      } else if (this.macro.loopCount == -1) {
         this.loopMode = 2;
      } else {
         this.loopMode = 1;
      }

      this.resetRowState();
      this.placeDefaultPosition();
      this.windowNode.setExpandedImmediate(!this.collapsed);
      OverlayManager.getInstance().bringToFront(this);
   }

   public void discardAndClose() {
      this.stopEditedRun();
      this.revertTempSave();
      this.update_V_10();
   }

   private boolean isJoinMacroMenuMode() {
      return this.editorMode == AutismMacroEditorOverlay.EditorMode.JOIN_MACRO_MENU;
   }

   private boolean isCaptureAllowed() {
      boolean inWorld = MC != null && MC.player != null && MC.level != null;
      return (inWorld || !this.configurationOnly) && !this.isJoinMacroMenuMode();
   }

   private void revertTempSave() {
      if (this.tempSavedForRun) {
         if (this.tempSavedAsNew) {
            String name = this.macro == null ? "" : this.macro.name;
            AutismMacro savedMacro = name != null && !name.isBlank() ? MacroLibraryManager.getInstance().findByName(name) : null;
            if (savedMacro == null && this.originalMacro != null) {
               savedMacro = this.originalMacro;
            }

            if (savedMacro != null) {
               MacroLibraryManager.getInstance().getMacros().remove(savedMacro);
               MacroLibraryManager.getInstance().save();
            }
         } else if (this.originalMacro != null && this.preRunBackup != null) {
            this.originalMacro.fromTag(this.preRunBackup);
            MacroLibraryManager.getInstance().save();
         }

         this.tempSavedForRun = false;
         this.tempSavedAsNew = false;
         this.preRunBackup = null;
         this.stepListLocked = false;
      }
   }

   private void stopEditedRun() {
      AutismMacro runningMacro = this.runningMacro();
      if (runningMacro == null || !this.tempSavedForRun && this.isLibraryMacro(runningMacro)) {
         if (this.macro != null && this.macro.name != null && !this.macro.name.isBlank()) {
            AutismMacro persisted = MacroLibraryManager.getInstance().findByName(this.macro.name);
            if (this.tempSavedForRun || persisted == null) {
               MacroExecutor.stopMacroByName(this.macro.name);
            }
         }

         if (this.originalMacro != null && this.originalMacro.name != null && !this.originalMacro.name.isBlank()) {
            AutismMacro persisted = MacroLibraryManager.getInstance().findByName(this.originalMacro.name);
            if (this.tempSavedForRun || persisted == null) {
               MacroExecutor.stopMacroByName(this.originalMacro.name);
            }
         }
      } else {
         MacroExecutor.stopMacro(runningMacro);
      }
   }

   private boolean isLibraryMacro(AutismMacro candidate) {
      if (candidate != null && candidate.name != null && !candidate.name.isBlank()) {
         AutismMacro persisted = MacroLibraryManager.getInstance().findByName(candidate.name);
         return persisted == candidate;
      } else {
         return false;
      }
   }

   public void update_V_10() {
      this.stopEditedRun();
      if (this.tempSavedForRun) {
         this.revertTempSave();
      }

      boolean shouldReopenMacroList = this.reopenMacroListOnClose;
      this.clearActionEditorActions();
      this.closingGhostMacro = this.visible ? this.macro : null;
      this.visible = false;
      AutismSessionState.getInstance().setMacroEditorVisible(false);
      AutismSessionState.getInstance().setEditingMacro(null);
      this.macro = null;
      this.originalMacro = null;
      this.oneShotRunId = -1L;
      this.invalidateRunState();
      this.tempSavedForRun = false;
      this.tempSavedAsNew = false;
      this.preRunBackup = null;
      this.reopenMacroListOnClose = false;
      this.editorMode = AutismMacroEditorOverlay.EditorMode.NORMAL;
      this.onSaveComplete = null;
      ActionEditorOverlay.getInstance().setCaptureAllowed(true);
      this.closePalette();
      this.clearInputFocus();
      this.listeningForKeybind = false;
      this.clearHistory();
      this.resetDragState();
      this.clearSelection();
      this.resetRowState();
      if (shouldReopenMacroList) {
         for (AutismOverlay overlay : OverlayManager.getInstance().getOverlays()) {
            if (overlay instanceof AutismMacroListOverlay macroListOverlay) {
               macroListOverlay.setVisible(true);
               OverlayManager.getInstance().bringToFront(macroListOverlay);
               break;
            }
         }
      }
   }

   private boolean isRunning() {
      this.refreshRunState();
      return this.running;
   }

   private boolean isStepListLocked() {
      this.refreshRunState();
      return !this.running ? false : System.currentTimeMillis() - this.runStartedAt >= 100L;
   }

   private void invalidateRunState() {
      this.lastRunStateRevision = -9223372036854775808L;
      this.lastRunStateRunId = -9223372036854775808L;
   }

   private void refreshRunState() {
      long revision = MacroExecutor.getRunEpoch();
      long runId = this.oneShotRunId;
      if (this.lastRunStateRevision != revision || this.lastRunStateRunId != runId) {
         boolean running = runId >= 0L && MacroExecutor.isRunActive(runId);
         if (!running && runId >= 0L) {
            this.oneShotRunId = -1L;
            runId = -1L;
         }

         if (!running && this.runningMacro() != null) {
            running = true;
         }

         this.lastRunStateRevision = revision;
         this.lastRunStateRunId = runId;
         this.running = running;
      }
   }

   private void cancelInteractions() {
      this.closePalette();
      this.notifyActionEditor();
      this.contextMenu.close();
      this.clearSelection();
      this.resetDragState();
      if (this.nameField != null) {
         this.nameField.setFocused(false);
      }

      if (this.loopCountField != null) {
         this.loopCountField.setFocused(false);
      }

      this.listeningForKeybind = false;
   }

   private void syncStepListLock() {
      boolean locked = this.isStepListLocked();
      if (locked != this.stepListLocked) {
         this.stepListLocked = locked;
         if (locked) {
            this.cancelInteractions();
         } else {
            this.resetDragState();
         }

         this.rebuildButtons();
      }
   }

   private AutismMacro runningMacro() {
      if (this.originalMacro != null && MacroExecutor.isMacroRunning(this.originalMacro)) {
         return this.originalMacro;
      } else if (this.macro != null && MacroExecutor.isMacroRunning(this.macro)) {
         return this.macro;
      } else if (this.macro != null && this.macro.name != null && !this.macro.name.isBlank() && MacroExecutor.isMacroNameRunning(this.macro.name)) {
         return this.macro;
      } else {
         return this.originalMacro != null && this.originalMacro.name != null && !this.originalMacro.name.isBlank() && MacroExecutor.isMacroNameRunning(this.originalMacro.name)
            ? this.originalMacro
            : null;
      }
   }

   private void onActionsChanged() {
      if (this.macro != null) {
         this.notifyActionEditor();
         if (this.macro.actions.isEmpty() && this.isRunning()) {
            MacroExecutor.stopMacroByName(this.macro.name);
         }

         this.refreshRowAnimations(true);
         this.pruneSelection();
         this.quantizeListScroll();
         this.resetDragState();
         this.rebuildButtons();
      }
   }

   private void notifyActionEditor() {
      ActionEditorOverlay overlay = ActionEditorOverlay.getExisting();
      if (overlay != null && this.macro != null) {
         overlay.onActionsChanged(this.macro.actions);
      }
   }

   private void clearActionEditorActions() {
      ActionEditorOverlay overlay = ActionEditorOverlay.getExisting();
      if (overlay != null && this.macro != null) {
         overlay.onActionsChanged(List.of());
      }
   }

   private static String profileName(PlayerInfo entry) {
      if (entry != null && entry.getProfile() != null) {
         String name = entry.getProfile().name();
         return name == null ? "" : name.trim();
      } else {
         return "";
      }
   }

   private static <E extends Enum<E>> E cycleEnum(E current, int step) {
      E[] values = (E[])current.getDeclaringClass().getEnumConstants();
      int index = (current.ordinal() + step) % values.length;
      if (index < 0) {
         index += values.length;
      }

      return values[index];
   }

   private static double cycleSpeed(double current, int step) {
      double[] speeds = new double[]{1.0, 2.0, 3.0, 5.0, 10.0};
      int index = 0;

      for (int i = 0; i < speeds.length; i++) {
         if (current <= speeds[i] + 1.0E-4) {
            index = i;
            break;
         }

         index = speeds.length - 1;
      }

      index = (index + step) % speeds.length;
      if (index < 0) {
         index += speeds.length;
      }

      return speeds[index];
   }

   public boolean isEditing(AutismMacro macroToCheck) {
      return macroToCheck != null && this.originalMacro != null
         ? this.originalMacro == macroToCheck || this.originalMacro.name != null && this.originalMacro.name.equals(macroToCheck.name)
         : false;
   }

   public void loadSharedPosition() {
      AutismSessionState shared = AutismSessionState.getInstance();
      this.editorX = shared.getMacroEditorX();
      this.editorY = shared.getMacroEditorY();
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
      AutismSessionState.getInstance().setMacroEditorVisible(visible);
      if (visible) {
         this.windowNode.setExpandedImmediate(!this.collapsed);
         OverlayManager.getInstance().bringToFront(this);
      } else {
         this.clearInputFocus();
      }
   }

   public static void onLeave() {
      OverlayScreenController.captureRetainedForegroundIfNeeded(MC);
   }

   public static void onJoin() {
   }

   @Override
   public boolean isCollapsed() {
      return this.collapsed;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.collapsed != collapsed) {
         this.collapsed = collapsed;
         this.headerPressed = false;
         this.headerMoved = false;
         this.resetDragState();
         if (this.nameField != null) {
            this.nameField.setFocused(false);
         }

         if (this.loopCountField != null) {
            this.loopCountField.setFocused(false);
         }

         if (collapsed) {
            this.closePalette();
            this.clearFocusAndDismissMenu();
         }

         this.windowNode.setShowBody(!collapsed);
         this.saveState();
      }
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      if (!this.dropdowns.isEmpty() && autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
         return true;
      }

      int height = this.layoutWindowHeight();
      if (mouseX >= this.editorX && mouseX <= this.editorX + this.editorWidth && mouseY >= this.editorY && mouseY <= this.editorY + height) {
         return true;
      }

      if (this.isPaletteOpen()) {
         int pickerX = this.pickerX();
         int pickerY = this.pickerY();
         int pickerW = this.pickerWidth();
         int pickerH = this.pickerHeight();
         if (mouseX >= pickerX && mouseX <= pickerX + pickerW && mouseY >= pickerY && mouseY <= pickerY + pickerH) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      return !this.visible
         ? false
         : mouseX >= this.editorX
            && mouseX <= this.editorX + this.editorWidth
            && mouseY >= this.editorY
            && mouseY <= this.editorY + this.styleTheme.load()
            && !this.update_Z_D_D_2(mouseX, mouseY);
   }

   @Override
   public boolean hasFocusedTextInput() {
      return !this.visible
         ? false
         : this.nameField != null && this.nameField.isFocused() || this.loopCountField != null && this.loopCountField.isFocused() || this.paletteSearchField != null && this.paletteSearchField.isFocused();
   }

   @Override
   public void clearInputFocus() {
      if (this.nameField != null) {
         this.nameField.setFocused(false);
      }

      if (this.loopCountField != null) {
         this.loopCountField.setFocused(false);
      }

      if (this.paletteSearchField != null) {
         this.paletteSearchField.setFocused(false);
      }
   }

   @Override
   public boolean isCapturingKeybind() {
      return this.visible && this.listeningForKeybind;
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      boolean dropdownReleased = autismclient.gui.ui.widgets.DropdownWidget.handleMouseRelease(this.dropdowns);
      if (this.paletteSearchField != null && this.paletteSearchField.mouseReleased(mouseX, mouseY, button)) {
         return true;
      }

      if (this.nameField != null && this.nameField.mouseReleased(mouseX, mouseY, button)) {
         return true;
      }

      if (this.loopCountField != null && this.loopCountField.mouseReleased(mouseX, mouseY, button)) {
         return true;
      }

      if (button != 0 || this.draggingRowIndex < 0 && this.pressedRowIndex < 0) {
         if (button == 0 && this.scrollbarDragMode != 0) {
            this.scrollbarDragMode = 0;
            this.saveState();
            return true;
         }

         if (dropdownReleased) {
            return true;
         }

         if (button != 0) {
            return false;
         }

         if (!this.headerPressed) {
            return false;
         }

         boolean headerPressed = this.headerPressed;
         boolean moved = this.headerMoved
            || Math.abs(mouseX - this.headerPressX) >= 3.0
            || Math.abs(mouseY - this.headerPressY) >= 3.0
            || this.editorX != this.headerPressEditorX
            || this.editorY != this.headerPressEditorY;
         this.headerPressed = false;
         if (headerPressed && !moved && this.isOverTitleBar(mouseX, mouseY) && !this.update_Z_D_D_2(mouseX, mouseY)) {
            this.setCollapsed(!this.collapsed);
         }

         this.saveState();
         this.headerMoved = false;
         return true;
      } else {
         this.finishRowPress();
         return true;
      }
   }

   public void saveState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      shared.setMacroEditorVisible(this.visible);
      shared.setMacroEditorX(this.editorX);
      shared.setMacroEditorY(this.editorY);
      shared.setEditingMacro(this.macro);
      this.saveBounds();
   }

   public void restoreState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      this.loadBounds();
      this.visible = shared.isMacroEditorVisible();
      this.editorX = shared.getMacroEditorX();
      this.editorY = shared.getMacroEditorY();
      this.macro = shared.getEditingMacro();
      this.windowNode.setExpandedImmediate(!this.collapsed);
      if (this.visible && this.macro != null) {
         this.resetRowState();
         this.rebuildButtons();
      }
   }

   private void placeDefaultPosition() {
      if (MC.getWindow() != null) {
         int screenWidth = cA.init_I();
         int screenHeight = cA.load_I();
         if (this.editorX == 0 && this.editorY == 0) {
            this.editorX = screenWidth - this.editorWidth - this.screenRightMargin();
            this.editorY = this.defaultTopY();
         }

         this.rebuildButtons();
      }
   }

   private boolean init_Z_Str_3(String name) {
      if (name != null && !name.trim().isEmpty()) {
         return !this.isNewMacro && name.equals(MacroLibraryManager.getInstance().findByName(name) != null ? name : "") ? true : MacroLibraryManager.getInstance().findByName(name) == null;
      } else {
         return false;
      }
   }

   private void rebuildButtons() {
      this.buttonLabels.clear();
      this.buttonBounds.clear();
      this.buttonActions.clear();
      this.buttonSecondaryActions.clear();
      this.buttonEnabled.clear();
      this.dropdowns.clear();
      this.runButtonIndex = -1;
      boolean stepListLocked = this.isStepListLocked();
      this.stepListLocked = stepListLocked;
      int bh = this.buttonHeight();
      int gap = this.toolbarGap();
      int row1Y = this.editorY + this.nameRowTopOffset();
      int deleteTopW = !this.isNewMacro ? this.deleteButtonWidth() : 0;
      int nameFieldW = this.editorWidth - this.contentInset() * 2 - (deleteTopW > 0 ? deleteTopW + gap : 0);
      if (this.nameField == null) {
         this.nameField = new ThemedTextInputBox(MC, this.font, this.editorX + this.contentInset(), row1Y, nameFieldW, this.nameFieldHeight(), false);
         this.nameField.setPlaceholder(Component.literal("Macro Name"));
         this.nameField.setChangedListener(text -> {
            this.macro.name = text;
            this.requestRebuild();
         });
      }

      this.nameField.setX(this.editorX + this.contentInset());
      this.nameField.setY(row1Y);
      this.nameField.setWidth(nameFieldW);
      this.nameField.setValue(this.macro.name);
      if (!this.isNewMacro) {
         this.addButton("Delete", this.editorX + this.editorWidth - this.contentInset() - deleteTopW, row1Y, deleteTopW, bh, () -> {
            if (this.isJoinMacroMenuMode() && this.macro != null && this.macro.name != null && this.macro.name.equalsIgnoreCase(JoinMacroController.macroName())) {
               JoinMacroController.setMacroName("");
            }

            MacroLibraryManager.getInstance().removeMacro(this.macro);
            this.update_V_10();
         }, !stepListLocked);
      }

      int row2Y = row1Y + this.nameRowHeight();
      int bx = this.editorX + this.contentInset();
      String keyText = this.macro.keyCode == -1 ? "Bind Key" : "Key: " + ValueSink.init_Str(this.macro.keyCode);
      if (this.listeningForKeybind) {
         keyText = "Press Key/Mouse...";
      }

      int keyW = this.buttonWidth(keyText);
      this.addButton(keyText, bx, row2Y, keyW, bh, () -> {
         this.listeningForKeybind = !this.listeningForKeybind;
         this.rebuildButtons();
      }, !stepListLocked);
      bx += keyW + gap;
      if (!this.isJoinMacroMenuMode() && !this.configurationOnly) {
         this.addButton(this.runButtonLabel(this.isRunning()), bx, row2Y, this.runButtonWidth(), bh, this::toggleRun, this.isRunning() || this.canRun());
         this.runButtonIndex = this.buttonLabels.size() - 1;
         bx += this.runButtonWidth() + gap;
      }

      if (!this.configurationOnly && LanSyncManager.getInstance().isInSession() && !this.isNewMacro && this.macro != null) {
         this.addButton("LAN", bx, row2Y, this.lanButtonWidth(), bh, () -> {
            if (this.macro != null) {
               LanSyncManager.getInstance().triggerMacroSync(this.macro.name);
            }
         }, !stepListLocked);
         bx += this.lanButtonWidth() + gap;
      }

      List<String> loopOptions = List.of("Once", "Loop #", "Loop Inf");
      int loopAvailable = this.editorX + this.editorWidth - this.contentInset() - bx - (this.loopMode == 1 ? this.loopCountFieldWidth() + gap : 0);
      int loopW = Math.max(40, Math.min(this.loopButtonWidth(), loopAvailable));
      autismclient.gui.ui.widgets.DropdownWidget loopDropdown = new autismclient.gui.ui.widgets.DropdownWidget(
         bx, row2Y, loopW, bh, loopOptions, Math.max(0, Math.min(this.loopMode, loopOptions.size() - 1)), idx -> {
            this.loopMode = Math.max(0, Math.min(idx, loopOptions.size() - 1));
            if (this.loopMode != 1 && this.loopCountField != null) {
               this.loopCountField.setFocused(false);
            }

            this.applyLoopSettings();
            this.rebuildButtons();
         }
      );
      loopDropdown.active = !stepListLocked;
      this.dropdowns.add(loopDropdown);
      bx += loopW + gap;
      if (this.loopMode == 1) {
         if (this.loopCountField == null) {
            this.loopCountField = new ThemedTextInputBox(MC, this.font, bx, row2Y, this.loopCountFieldWidth(), this.loopCountFieldHeight(), false);
            this.loopCountField.setChangedListener(text -> {
               try {
                  this.macro.loopCount = Integer.parseInt(text.replaceAll("[^0-9-]", ""));
               } catch (NumberFormatException var3x) {
               }
            });
         }

         this.loopCountField.setX(Math.min(bx, this.editorX + this.editorWidth - this.contentInset() - this.loopCountFieldWidth()));
         this.loopCountField.setY(row2Y);
         this.loopCountField.setWidth(this.loopCountFieldWidth());
         this.loopCountField.setValue(String.valueOf(this.macro.loopCount == -1 ? 1 : this.macro.loopCount));
      }

      int builderButtonY = this.builderButtonY();
      int builderGap = this.builderButtonGap();
      int builderX = this.editorX + this.contentInset();
      int builderAvailableWidth = this.editorWidth - this.contentInset() * 2;
      int addActionW = this.paletteButtonWidth("Add Action");
      int addConditionalW = this.paletteButtonWidth("Add Conditional");
      int presetsW = this.textButtonWidth("Presets");
      int builderGroupGap = this.builderWidthGap();
      int builderContentWidth = addActionW + addConditionalW + presetsW + builderGroupGap * 2;
      if (builderContentWidth <= builderAvailableWidth) {
         int groupX = builderX;
         this.addButton(
            "Add Action",
            groupX,
            builderButtonY,
            addActionW,
            bh,
            () -> this.openPalette(AutismMacroEditorOverlay.PaletteSectionKind.ACTION),
            !stepListLocked
         );
         groupX += addActionW + builderGroupGap;
         this.addButton(
            "Add Conditional",
            groupX,
            builderButtonY,
            addConditionalW,
            bh,
            () -> this.openPalette(AutismMacroEditorOverlay.PaletteSectionKind.CONDITIONAL),
            !stepListLocked
         );
         groupX += addConditionalW + builderGroupGap;
         this.addButton(
            "Presets",
            groupX,
            builderButtonY,
            presetsW,
            bh,
            () -> this.openPalette(AutismMacroEditorOverlay.PaletteSectionKind.PRESET),
            !stepListLocked
         );
      } else {
         int builderTotalWidth = builderAvailableWidth - builderGap * 2;
         int conditionalExtra = Math.min(18, Math.max(0, builderTotalWidth / 14));
         int sideBuilderWidth = Math.max(1, (builderTotalWidth - conditionalExtra) / 3);
         int conditionalBuilderWidth = Math.max(1, builderTotalWidth - sideBuilderWidth * 2);
         this.addButton(
            "Add Action",
            builderX,
            builderButtonY,
            sideBuilderWidth,
            bh,
            () -> this.openPalette(AutismMacroEditorOverlay.PaletteSectionKind.ACTION),
            !stepListLocked
         );
         this.addButton(
            "Add Conditional",
            builderX + sideBuilderWidth + builderGap,
            builderButtonY,
            conditionalBuilderWidth,
            bh,
            () -> this.openPalette(AutismMacroEditorOverlay.PaletteSectionKind.CONDITIONAL),
            !stepListLocked
         );
         this.addButton(
            "Presets",
            builderX + sideBuilderWidth + builderGap + conditionalBuilderWidth + builderGap,
            builderButtonY,
            sideBuilderWidth,
            bh,
            () -> this.openPalette(AutismMacroEditorOverlay.PaletteSectionKind.PRESET),
            !stepListLocked
         );
      }

      int bottomY = this.listBottomY();
      bx = this.editorX + this.contentInset();
      boolean nameEntered = this.macro.name != null && !this.macro.name.trim().isEmpty();
      int footerAvailableWidth = this.editorWidth - this.contentInset() * 2;
      int saveW = this.textButtonWidth("Save");
      int cancelW = this.textButtonWidth("Cancel");
      int undoW = this.textButtonWidth("Undo");
      int redoW = this.textButtonWidth("Redo");
      int footerGroupGap = this.footerWidthGap();
      if (saveW + cancelW + undoW + redoW + footerGroupGap * 3 <= footerAvailableWidth) {
         this.addButton("Save", bx, bottomY, saveW, bh, this::saveMacro, !stepListLocked && nameEntered);
         bx += saveW + footerGroupGap;
         this.addButton("Cancel", bx, bottomY, cancelW, bh, this::discardAndClose);
         bx += cancelW + footerGroupGap;
         this.addButton("Undo", bx, bottomY, undoW, bh, this::undo, !stepListLocked && this.canUndo());
         bx += undoW + footerGroupGap;
         this.addButton("Redo", bx, bottomY, redoW, bh, this::redo, !stepListLocked && this.canRedo());
      } else {
         this.addButton("Save", bx, bottomY, this.saveButtonWidth(), bh, this::saveMacro, !stepListLocked && nameEntered);
         bx += this.saveButtonWidth() + this.footerButtonGap();
         this.addButton("Cancel", bx, bottomY, this.saveButtonWidth(), bh, this::discardAndClose);
         bx += this.saveButtonWidth() + this.footerButtonGap();
         this.addButton("Undo", bx, bottomY, this.undoButtonWidth(), bh, this::undo, !stepListLocked && this.canUndo());
         bx += this.undoButtonWidth() + this.footerButtonGap();
         this.addButton("Redo", bx, bottomY, this.undoButtonWidth(), bh, this::redo, !stepListLocked && this.canRedo());
      }
   }

   private void addButton(String label, int x, int y, int w, int h, Runnable action) {
      this.addButton(label, x, y, w, h, action, true);
   }

   private void addButton(String label, int x, int y, int w, int h, Runnable action, boolean enabled) {
      this.addButton(label, x, y, w, h, action, null, enabled);
   }

   private void addDualActionButton(String label, int x, int y, int w, int h, Runnable action, Runnable secondaryAction) {
      this.addButton(label, x, y, w, h, action, secondaryAction, true);
   }

   private void addButton(String label, int x, int y, int w, int h, Runnable action, Runnable secondaryAction, boolean enabled) {
      this.buttonLabels.add(label);
      this.buttonBounds.add(new int[]{x, y, w, h});
      this.buttonActions.add(action);
      this.buttonSecondaryActions.add(secondaryAction);
      this.buttonEnabled.add(enabled);
   }

   private void refreshUndoRedoButtons() {
      for (int i = 0; i < this.buttonLabels.size(); i++) {
         String label = this.buttonLabels.get(i);
         if ("Undo".equals(label)) {
            this.buttonEnabled.set(i, !this.isStepListLocked() && this.canUndo());
         } else if ("Redo".equals(label)) {
            this.buttonEnabled.set(i, !this.isStepListLocked() && this.canRedo());
         }
      }
   }

   private void refreshRunButton() {
      if (this.runButtonIndex >= 0 && this.runButtonIndex < this.buttonEnabled.size()) {
         this.buttonEnabled.set(this.runButtonIndex, this.isRunning() || this.canRun());
      }
   }

   private void toggleRun() {
      if (this.macro == null) {
         AutismClientMessaging.sendPrefixed("No macro to run!");
      } else if (this.runActive()) {
         this.stopRun();
         this.rebuildButtons();
      } else if (this.macro.actions.isEmpty()) {
         AutismClientMessaging.sendPrefixed("Macro has no actions!");
         this.rebuildButtons();
      } else if (!this.canRun()) {
         AutismClientMessaging.sendPrefixed(this.runBlocker());
         this.rebuildButtons();
      } else {
         this.saveAndRun();
      }
   }

   private boolean runActive() {
      return this.isRunning();
   }

   private void stopRun() {
      if (this.oneShotRunId >= 0L) {
         MacroExecutor.stopRun(this.oneShotRunId);
      }

      AutismMacro runningMacro = this.runningMacro();
      if (runningMacro != null) {
         MacroExecutor.stopMacro(runningMacro);
      } else {
         String name = this.macro != null ? this.macro.name : "";
         if (name != null && !name.isBlank()) {
            MacroExecutor.stopMacroByName(name);
         }
      }

      this.oneShotRunId = -1L;
      this.invalidateRunState();
   }

   private void saveAndRun() {
      boolean hasValidName = this.macro.name != null && !this.macro.name.trim().isEmpty();
      if (hasValidName) {
         AutismMacro existingWithName = MacroLibraryManager.getInstance().findByName(this.macro.name);
         boolean isNameConflict = this.isNewMacro && existingWithName != null;
         if (isNameConflict) {
            AutismClientMessaging.sendPrefixed("Name already exists: " + this.macro.name);
            return;
         }

         if (this.isNewMacro) {
            MacroLibraryManager.getInstance().saveMacro(this.macro);
            this.originalMacro = MacroLibraryManager.getInstance().findByName(this.macro.name);
            this.isNewMacro = false;
            this.tempSavedForRun = true;
            this.tempSavedAsNew = true;
         } else {
            if (!this.tempSavedForRun && this.originalMacro != null) {
               this.preRunBackup = this.originalMacro.toTag();
            }

            if (this.originalMacro != null) {
               String liveFolder = this.originalMacro.folder;
               this.originalMacro.fromTag(this.macro.toTag());
               this.originalMacro.folder = liveFolder;
            }

            MacroLibraryManager.getInstance().save();
            this.tempSavedForRun = true;
            this.tempSavedAsNew = false;
         }

         AutismMacro savedMacro = MacroLibraryManager.getInstance().findByName(this.macro.name);
         this.cancelInteractions();
         this.oneShotRunId = savedMacro != null ? savedMacro.executeWithRunId() : this.macro.executeWithRunId();
         this.invalidateRunState();
      } else {
         String originalName = this.macro.name;
         this.macro.name = "(Unsaved)";
         this.cancelInteractions();
         this.oneShotRunId = this.macro.executeWithRunId();
         this.invalidateRunState();
         this.macro.name = originalName;
      }

      if (this.oneShotRunId >= 0L) {
         this.runStartedAt = System.currentTimeMillis();
      }

      this.rebuildButtons();
   }

   private boolean canRun() {
      return this.runBlocker() == null;
   }

   private String runBlocker() {
      if (this.macro == null) {
         return "No macro to run!";
      }

      for (MacroAction action : this.macro.actions) {
         if (action instanceof autismclient.util.macro.InventoryAuditAction auditAction && !auditAction.isConfigured()) {
            return "Inventory Audit needs at least one target.";
         }
      }

      return null;
   }

   private void requestRebuild() {
      this.rebuildPending = true;
   }

   private void applyLoopSettings() {
      if (this.loopMode == 0) {
         this.macro.loop = false;
      } else if (this.loopMode == 1) {
         this.macro.loop = true;

         try {
            if (this.loopCountField != null) {
               this.macro.loopCount = Integer.parseInt(this.loopCountField.getValue());
            }
         } catch (Exception var2) {
            this.macro.loopCount = 1;
         }
      } else {
         this.macro.loop = true;
         this.macro.loopCount = -1;
      }
   }

   public void appendAction(MacroAction action) {
      if (!this.isStepListLocked()) {
         this.recordUndo();
         this.macro.actions.add(action);
         this.onActionsChanged();
         if (this.isEditable(action)) {
            ActionEditorOverlay actionEditor = ActionEditorOverlay.getInstance();
            actionEditor.setCaptureAllowed(this.isCaptureAllowed());
            actionEditor.setContext(this.macro, this.macro.actions.indexOf(action));
            actionEditor.openAction(action, this::recordUndo, () -> this.load_V_MacroAction_2(action));
         }
      }
   }

   private void insertActions(List<MacroAction> actions) {
      if (!this.isStepListLocked() && actions != null && !actions.isEmpty()) {
         this.recordUndo();

         for (MacroAction action : actions) {
            if (action != null) {
               this.macro.actions.add(action);
            }
         }

         this.onActionsChanged();
      }
   }

   private void load_V_MacroAction_2(MacroAction action) {
      if (!this.isStepListLocked()) {
         if (action instanceof autismclient.util.macro.RaceAction race) {
            this.init_V_L(race);
         } else if (action instanceof autismclient.util.macro.ReportAction report) {
            this.init_V_O(report);
         } else {
            if (action instanceof autismclient.util.macro.PacketGateAction) {
               this.init_V_B((autismclient.util.macro.PacketGateAction)action);
            }

            this.onActionsChanged();
         }
      }
   }

   private void init_V_L(autismclient.util.macro.RaceAction race) {
      if (this.macro != null && race != null) {
         int index = this.macro.actions.indexOf(race);
         if (index < 0) {
            this.onActionsChanged();
         } else {
            int oldCount = race.countBodyActions(this.macro.actions, index);
            List<MacroAction> replacement = new ArrayList<>();
            boolean[] reused = new boolean[Math.max(0, oldCount)];

            for (String step : race.read()) {
               boolean conditionStep = autismclient.util.macro.RaceAction.isConditionStep(step);
               String typeName = autismclient.util.macro.RaceAction.stepType(step);
               MacroAction kept = null;

               for (int offset = 1; offset <= oldCount && index + offset < this.macro.actions.size(); offset++) {
                  int reuseIndex = offset - 1;
                  if (reuseIndex < reused.length && !reused[reuseIndex]) {
                     MacroAction oldAction = this.macro.actions.get(index + offset);
                     if (oldAction != null) {
                        boolean sameType = conditionStep
                           ? autismclient.util.macro.RaceAction.isRaceCondition(oldAction)
                              && autismclient.util.macro.RaceAction.conditionTypeOf(oldAction).name().equalsIgnoreCase(typeName)
                           : autismclient.util.macro.RaceAction.load(oldAction) && oldAction.getTypeId().equalsIgnoreCase(typeName);
                        if (sameType) {
                           kept = oldAction;
                           reused[reuseIndex] = true;
                           break;
                        }
                     }
                  }
               }

               replacement.add(kept != null ? kept : autismclient.util.macro.RaceAction.createStepAction(step));
            }

            if (oldCount > 0) {
               this.macro.actions.subList(index + 1, index + 1 + oldCount).clear();
            }

            this.macro.actions.addAll(index + 1, replacement);
            race.raceSteps = new ArrayList<>(race.read());
            race.syncTypesFromSteps();
            race.conditionType = race.conditionTypes.isEmpty() ? "" : race.conditionTypes.get(0);
            race.triggerAction = race.conditionTypes.isEmpty() ? new autismclient.util.macro.DelayAction(0) : race.createConditionAction();
            race.actionType = race.actionTypes.isEmpty() ? autismclient.util.macro.MacroActionType.PACKET_CLICK.name() : race.actionTypes.get(0);
            race.bodyCount = replacement.size();
            this.onActionsChanged();
         }
      }
   }

   private void init_V_O(autismclient.util.macro.ReportAction report) {
      if (this.macro != null && report != null) {
         int index = this.macro.actions.indexOf(report);
         if (index < 0) {
            this.onActionsChanged();
         } else {
            int oldCount = report.coveredCount(this.macro.actions, index);
            MacroAction oldStart = oldCount >= 1 && index + 1 < this.macro.actions.size() ? this.macro.actions.get(index + 1) : null;
            MacroAction oldEnd = oldCount >= 2 && index + 2 < this.macro.actions.size() ? this.macro.actions.get(index + 2) : null;
            MacroAction newStart = report.createStartAction();
            if (oldStart != null && autismclient.util.macro.RaceAction.load(oldStart) && oldStart.getTypeId().equalsIgnoreCase(report.startActionType)) {
               newStart = oldStart;
            }

            MacroAction newEnd = report.createEndCondition();
            if (oldEnd != null
               && autismclient.util.macro.RaceAction.isRaceCondition(oldEnd)
               && autismclient.util.macro.RaceAction.conditionTypeOf(oldEnd).name().equalsIgnoreCase(report.endConditionType)) {
               newEnd = oldEnd;
            }

            if (oldCount > 0) {
               this.macro.actions.subList(index + 1, index + 1 + oldCount).clear();
            }

            this.macro.actions.add(index + 1, newStart);
            this.macro.actions.add(index + 2, newEnd);
            report.conditionCount = 2;
            this.onActionsChanged();
         }
      }
   }

   private void saveMacro() {
      if (!this.isStepListLocked()) {
         if (this.macro != null && this.macro.actions.isEmpty() && this.originalMacro != null && !this.originalMacro.actions.isEmpty()) {
            AutismClientMessaging.sendPrefixed("Refusing to save 0 steps over \"" + this.macro.name + "\".");
         } else {
            String trimmedName = this.macro != null && this.macro.name != null ? this.macro.name.trim() : "";
            if (trimmedName.isEmpty()) {
               bi.init("Name your macro before saving.", -2075846);
            } else {
               AutismMacro nameOwner = MacroLibraryManager.getInstance().findByName(trimmedName);
               if (nameOwner != null && nameOwner != this.originalMacro) {
                  bi.init("A macro named \"" + trimmedName + "\" already exists.", -2075846);
               } else if (this.init_Z_Str_3(this.macro.name)) {
                  String previousName = !this.isNewMacro && this.originalMacro != null && this.originalMacro.name != null ? this.originalMacro.name : "";
                  AutismMacro savedOrUpdated = this.macro;
                  if (this.isNewMacro) {
                     MacroLibraryManager.getInstance().saveMacro(this.macro);
                     savedOrUpdated = this.macro;
                  } else {
                     if (this.originalMacro != null) {
                        this.originalMacro.name = this.macro.name;
                        this.originalMacro.description = this.macro.description;
                        this.originalMacro.loop = this.macro.loop;
                        this.originalMacro.loopCount = this.macro.loopCount;
                        this.originalMacro.keyCode = this.macro.keyCode;
                        this.originalMacro.actions = new ArrayList<>(this.macro.actions);
                        savedOrUpdated = this.originalMacro;
                     }

                     MacroLibraryManager.getInstance().save();
                  }

                  if (!previousName.isBlank() && MacroNameNormalizer.changed(previousName, savedOrUpdated.name)) {
                     if (!MacroNameNormalizer.sameName(previousName, savedOrUpdated.name) && LanSyncManager.getInstance().isInSession()) {
                        LanSyncManager.getInstance().notifyMacroDeleted(previousName);
                     }
                  }

                  this.tempSavedForRun = false;
                  this.tempSavedAsNew = false;
                  this.preRunBackup = null;
                  Consumer<AutismMacro> callback = this.onSaveComplete;
                  if (callback != null) {
                     callback.accept(savedOrUpdated);
                  }

                  this.update_V_10();
               }
            }
         }
      }
   }

   private MacroAction cloneAction(MacroAction original) {
      if (original == null) {
         return null;
      }

      try {
         return AutismMacro.deserializeAction(original.toTag());
      } catch (Exception var3) {
         return null;
      }
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      boolean ghost = !this.visible && this.macro == null && this.closingGhostMacro != null && this.isClosing();
      if (!ghost && !this.visible) {
         this.closingGhostMacro = null;
      }

      if (ghost) {
         this.macro = this.closingGhostMacro;
      }

      try {
         this.renderEditorFrame(context, mouseX, mouseY, delta);
      } finally {
         if (ghost) {
            this.macro = null;
         }
      }
   }

   private void renderEditorFrame(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.rebuildPending) {
         this.rebuildPending = false;
         this.rebuildButtons();
      }

      this.syncStepListLock();
      this.refreshUndoRedoButtons();
      this.refreshRunButton();
      if (this.visible || this.isClosing()) {
         boolean stepListLocked = this.isStepListLocked();
         int panelMouseX = mouseX;
         int panelMouseY = mouseY;
         if (this.isOverPalette(mouseX, mouseY)
            || this.contextMenu.handleClick(mouseX, mouseY)
            || !this.dropdowns.isEmpty() && autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
            panelMouseX = -10000;
            panelMouseY = -10000;
         }

         String editorTitle = this.isNewMacro ? "Create Macro" : "Edit Macro";
         boolean isRunning = this.isRunning();
         if (isRunning) {
            editorTitle = editorTitle + " [RUN]";
         }

         if (this.macro != null) {
            editorTitle = editorTitle + " (" + this.macro.actions.size() + " steps)";
         }

         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         OverlayBounds clamped = this.clampToScreen(this, new OverlayBounds(this.editorX, this.editorY, this.preferredWindowWidth(), this.preferredWindowHeight(), this.visible, this.collapsed));
         if (clamped.x != this.editorX || clamped.y != this.editorY || clamped.width != this.editorWidth || clamped.height != this.editorHeight) {
            this.editorX = clamped.x;
            this.editorY = clamped.y;
            this.editorWidth = clamped.width;
            this.editorHeight = clamped.height;
            this.rebuildButtons();
         }

         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         boolean active = OverlayManager.getInstance().isFocused(this) || OverlayManager.getInstance().isTopmost(this);
         boolean headerHovered = uiMouseX >= this.editorX
            && uiMouseX < this.editorX + this.editorWidth
            && uiMouseY >= this.editorY
            && uiMouseY < this.editorY + this.styleTheme.load();
         this.windowNode.setTitle(editorTitle);
         this.windowNode.setExpanded(!this.collapsed);
         this.windowNode.setActive(active);
         this.windowNode.setHeaderHovered(headerHovered);
         int frameHeight = this.layoutWindowHeight();
         this.bodySpacer.withHeight(Math.max(0, frameHeight - this.styleTheme.load()));
         this.windowNode.setBounds(this.editorX, this.editorY, this.editorWidth, frameHeight);
         this.layoutHost.render(context, mouseX, mouseY, delta);
         this.renderPinButton(context, mouseX, mouseY, this.load_cH());
         int bodyClipTop = this.editorY + this.styleTheme.load();
         int bodyClipBottom = this.windowNode.drawnBottomY();
         if (bodyClipBottom > bodyClipTop + 1) {
            viewport.pushClip(context, this.editorX, bodyClipTop, this.editorX + this.editorWidth, bodyClipBottom);

            try {
               if (this.nameField != null) {
                  this.nameField.render(context, panelMouseX, panelMouseY, delta);
               }

               if (this.loopMode == 1 && this.loopCountField != null) {
                  this.loopCountField.render(context, panelMouseX, panelMouseY, delta);
               }

               if (!this.init_Z_Str_3(this.macro.name)) {
                  String warning = this.macro.name.isEmpty() ? "Name Required" : "Name Taken";
                  int warningW = autismclient.gui.f.b.k.init(
                     this.font, warning, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), -43691
                  );
                  autismclient.gui.f.b.k.init(
                     context,
                     this.font,
                     warning,
                     this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
                     -43691,
                     this.editorX + this.editorWidth - this.contentInset() - warningW,
                     UiLayoutMath.centerTextY(this.listBottomY(), this.buttonHeight(), this.styleTheme.init_I_l(autismclient.gui.f.b.l.read), this.styleTheme.flush()),
                     false
                  );
               }

               int builderLabelY = this.builderLabelY();
               autismclient.gui.f.b.k.init(
                  context,
                  this.font,
                  "Step Builder",
                  this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load),
                  UiColorPalette.textBody(),
                  this.editorX + this.contentInset() + 2,
                  UiLayoutMath.centerTextY(
                     builderLabelY,
                     this.styleTheme.init_I_l_I(autismclient.gui.f.b.l.load, 1),
                     this.styleTheme.init_I_l(autismclient.gui.f.b.l.load),
                     this.styleTheme.flush()
                  ),
                  false
               );
               int listLabelY = this.listLabelY();
               int listY = this.listTopY();
               int listHeight = this.listAreaHeight();
               int listClipTop = this.listRowsTopY();
               int listClipBottom = this.listRowsBottomY();
               autismclient.gui.f.b.k.init(
                  context,
                  this.font,
                  "Steps (" + this.macro.actions.size() + ")",
                  this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load),
                  UiColorPalette.textBody(),
                  this.editorX + this.contentInset() + 2,
                  UiLayoutMath.centerTextY(
                     listLabelY,
                     this.styleTheme.init_I_l_I(autismclient.gui.f.b.l.load, 1),
                     this.styleTheme.init_I_l(autismclient.gui.f.b.l.load),
                     this.styleTheme.flush()
                  ),
                  false
               );
               if (stepListLocked) {
                  String lockText = "Running - list locked";
                  int lockColor = autismclient.gui.ui.layout.LayoutContext.scaleAlpha(-24416, 0.86F);
                  int lockW = autismclient.gui.f.b.k.init(
                     this.font, lockText, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load), lockColor
                  );
                  autismclient.gui.f.b.k.init(
                     context,
                     this.font,
                     lockText,
                     this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load),
                     lockColor,
                     Math.max(this.editorX + this.contentInset() + 2, this.rowButtonsRight() - lockW),
                     UiLayoutMath.centerTextY(
                        listLabelY,
                        this.styleTheme.init_I_l_I(autismclient.gui.f.b.l.load, 1),
                        this.styleTheme.init_I_l(autismclient.gui.f.b.l.load),
                        this.styleTheme.flush()
                     ),
                     false
                  );
               }

               this.quantizeListScroll();
               UiColorPalette.drawInputFrame(context, this.contentX(), listY, this.contentWidth(), listHeight, false);
               autismclient.gui.ui.UiClipStack.getInstance()
                  .push(
                     context,
                     autismclient.gui.f.NetHelper.init(
                        this.listClipLeft(), listClipTop, Math.max(0, this.rowButtonsRight() - this.listClipLeft()), Math.max(0, listClipBottom - listClipTop)
                     )
                  );

               try {
                  this.advanceAnimations();
                  int y = listY - this.listScroll;
                  int activeDragIndex = stepListLocked ? -1 : this.draggingRowIndex;
                  int activeDragTarget = stepListLocked ? -1 : this.dragTargetIndex;
                  List<MacroAction> dragBlock = null;
                  List<Integer> dragBlockIndices = null;
                  Set<MacroAction> dragSet = null;
                  int dragInsertCompact = -1;
                  int dragPrimaryPos = 0;
                  if (activeDragIndex >= 0 && activeDragIndex < this.macro.actions.size() && this.isSelected(activeDragIndex)) {
                     List<MacroAction> refs = this.selectedActions();
                     if (refs.size() > 1 && !this.containsPacketGate(refs)) {
                        dragBlock = new ArrayList<>();
                        dragBlockIndices = new ArrayList<>();
                        dragSet = Collections.newSetFromMap(new IdentityHashMap<>());
                        dragSet.addAll(refs);

                        for (int i = 0; i < this.macro.actions.size(); i++) {
                           MacroAction a = this.macro.actions.get(i);
                           if (dragSet.contains(a)) {
                              dragBlock.add(a);
                              dragBlockIndices.add(i);
                           }
                        }

                        dragInsertCompact = this.init_I_Set(dragSet, activeDragIndex, activeDragTarget);

                        for (int p = 0; p < dragBlockIndices.size(); p++) {
                           if (dragBlockIndices.get(p) == activeDragIndex) {
                              dragPrimaryPos = p;
                              break;
                           }
                        }
                     }
                  }

                  boolean multiDrag = dragBlock != null;

                  for (int i = 0; i < this.macro.actions.size(); i++) {
                     MacroAction action = this.macro.actions.get(i);
                     if (multiDrag ? !dragSet.contains(action) : i != activeDragIndex) {
                        int visualIndex = multiDrag
                           ? this.init_I_I_2(i, dragSet, dragInsertCompact, dragBlock.size())
                           : this.shiftedIndex(i, activeDragIndex, activeDragTarget);
                        int actionY = y + visualIndex * this.rowHeight();
                        if (this.isRowVisible(actionY)) {
                           AutismMacroEditorOverlay.RowTransitionAnim motion = this.findAnimation(this.stableId(action));
                           int animatedY = motion != null && motion.kind == AutismMacroEditorOverlay.RowAnimKind.MOVE
                              ? this.animatedRowY(motion, y)
                              : actionY;
                           int enterOffset = this.init_I_AutismMacroEditorO(motion);
                           float enterAlpha = this.init_F_AutismMacroEditorO(motion);
                           this.renderActionRowContent(
                              context,
                              action,
                              action.getDisplayName(),
                              this.isWaitAction(action),
                              i,
                              animatedY,
                              panelMouseX,
                              panelMouseY,
                              enterOffset,
                              stepListLocked ? enterAlpha * 0.54F : enterAlpha,
                              !stepListLocked
                           );
                        }
                     }
                  }

                  for (AutismMacroEditorOverlay.RowTransitionAnim motion : this.rowAnimations) {
                     if (motion.kind == AutismMacroEditorOverlay.RowAnimKind.EXIT) {
                        int actionY = this.animatedRowY(motion, y);
                        if (this.isRowVisible(actionY)) {
                           this.renderActionRowContent(
                              context,
                              null,
                              motion.label,
                              motion.conditional,
                              motion.fromRowIndex,
                              actionY,
                              -10000,
                              -10000,
                              this.init_I_AutismMacroEditorO(motion),
                              this.init_F_AutismMacroEditorO(motion),
                              false
                           );
                        }
                     }
                  }

                  if (!stepListLocked && activeDragIndex >= 0 && activeDragIndex < this.macro.actions.size()) {
                     int insertSlot = multiDrag ? dragInsertCompact : this.dropInsertSlot(activeDragIndex, activeDragTarget);
                     if (insertSlot >= 0) {
                        int insertY = y + insertSlot * this.rowHeight();
                        if (insertY >= listClipTop - 2 && insertY <= listClipBottom + 2) {
                           autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(
                              context, this.contentX() + 2, insertY - 1, this.rowButtonsRight() - this.contentX() - 3, 2, this.styleTheme.run()
                           );
                        }
                     }

                     int dragRowTop = this.dragRowTop();
                     if (multiDrag) {
                        for (int j = 0; j < dragBlock.size(); j++) {
                           int top = dragRowTop + (j - dragPrimaryPos) * this.rowHeight();
                           if (this.isRowVisible(top)) {
                              this.renderActionRow(context, dragBlock.get(j), top, dragBlockIndices.get(j) + 1);
                           }
                        }
                     } else if (this.isRowVisible(dragRowTop)) {
                        this.renderActionRow(context, this.macro.actions.get(activeDragIndex), dragRowTop, activeDragIndex + 1);
                     }
                  }
               } finally {
                  autismclient.gui.ui.UiClipStack.getInstance().pop(context);
               }

               autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics var531 = this.listScrollbarMetrics();
               autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(context, var531, var531.containsTrack(panelMouseX, panelMouseY), this.scrollbarDragMode == 1);
               this.hoverTooltip = null;
               boolean macroCurrentlyRunning = this.isRunning();

               for (int ix = 0; ix < this.buttonLabels.size(); ix++) {
                  int[] b = this.buttonBounds.get(ix);
                  boolean enabled = this.buttonEnabled.get(ix);
                  String lbl = ix == this.runButtonIndex ? this.runButtonLabel(macroCurrentlyRunning) : this.buttonLabels.get(ix);
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant = autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY;
                  if (ix == this.runButtonIndex) {
                     variant = macroCurrentlyRunning
                        ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER
                        : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS;
                  } else if ("Save".equals(lbl)) {
                     variant = autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS;
                  }

                  if (ix == this.runButtonIndex) {
                     this.renderRunButton(context, b[0], b[1], b[2], b[3], macroCurrentlyRunning, enabled, panelMouseX, panelMouseY);
                  } else if ("Multi".equals(lbl)) {
                     this.renderIconButton(context, b[0], b[1], b[2], b[3], lbl, GuiIconAtlas.PLAY, variant, enabled, panelMouseX, panelMouseY);
                  } else if (!"Add Action".equals(lbl) && !"Add Conditional".equals(lbl)) {
                     this.renderToneButton(context, b[0], b[1], b[2], b[3], lbl, variant, enabled, panelMouseX, panelMouseY);
                  } else {
                     this.renderIconButton(context, b[0], b[1], b[2], b[3], lbl.substring(4), GuiIconAtlas.PLUS, variant, enabled, panelMouseX, panelMouseY);
                  }

                  if (panelMouseX >= b[0] && panelMouseX < b[0] + b[2] && panelMouseY >= b[1] && panelMouseY < b[1] + b[3]) {
                     String tip = this.buttonTooltip(lbl);
                     if (tip != null) {
                        this.hoverTooltip = tip;
                        this.tooltipX = panelMouseX + 8;
                        this.tooltipY = panelMouseY + 12;
                     }
                  }
               }

               if (!this.dropdowns.isEmpty()) {
                  autismclient.gui.ui.widgets.DropdownWidget.renderTriggers(context, this.font, this.dropdowns, mouseX, mouseY);
               }
            } finally {
               viewport.popClip(context);
            }

            if (!this.collapsed && !this.dropdowns.isEmpty() && autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
               context.nextStratum();
               autismclient.gui.ui.widgets.DropdownWidget.renderOpenPopup(context, this.font, this.dropdowns, mouseX, mouseY);
            }

            if (!this.collapsed && this.hoverTooltip != null) {
               context.nextStratum();
               TooltipRenderer.init(autismclient.gui.f.UiHelper.init(context, this.font, this.tooltipX, this.tooltipY), this.hoverTooltip, this.tooltipX, this.tooltipY, 220);
            }

            if (!this.collapsed && this.contextMenu.isOpen()) {
               context.nextStratum();
               this.contextMenu.render(context, mouseX, mouseY);
            }
         }
      }
   }

   private void renderRowTextButton(GuiGraphicsExtractor context, int x, int y, int width, int height, boolean hovered, boolean danger, String label) {
      autismclient.gui.ui.widgets.IconButtonRenderer.init(
         context, this.font, autismclient.gui.f.NetHelper.init(x, y, width, height), label == null ? "" : label, hovered, true, danger
      );
   }

   private void renderRowIconButton(GuiGraphicsExtractor context, int x, int y, int width, int height, boolean hovered, boolean danger, Identifier icon) {
      autismclient.gui.ui.widgets.IconButtonRenderer.init(context, this.font, autismclient.gui.f.NetHelper.init(x, y, width, height), icon, hovered, true, danger);
   }

   private boolean isPaletteButtonLabel(String label) {
      return "Add Action".equals(label) || "Add Conditional".equals(label) || "Presets".equals(label);
   }

   private void renderPaletteCard(
      GuiGraphicsExtractor context,
      AutismMacroEditorOverlay.PaletteCategory category,
      AutismMacroEditorOverlay.PaletteEntry entry,
      int x,
      int y,
      int width,
      int height,
      boolean hovered,
      int mouseX,
      int mouseY
   ) {
      float hover = hovered ? 1.0F : 0.0F;
      float emphasis = hover * 0.85F;
      int categoryRgb = category.color & 16777215;
      int fillColor = UiLayoutMath.lerpColor(134217728, 570425344 | categoryRgb, emphasis);
      int borderColor = UiLayoutMath.lerpColor(category.color, -1, Math.min(0.22F, hover * 0.1F));
      int textColor = UiLayoutMath.lerpColor(this.styleTheme.load(autismclient.gui.f.b.l.read), -1, Math.min(0.32F, hover * 0.12F));
      autismclient.gui.f.PacketUtil.init(context, autismclient.gui.f.NetHelper.init(x, y, width, height), fillColor, borderColor);
      if (emphasis > 0.0F) {
         int hoverTint = (int)Math.min(30.0F, hover * 18.0F) << 24 | categoryRgb;
         autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(context, x + 1, y + 1, width - 2, height - 2, hoverTint);
      }

      String label = entry.label == null ? "" : entry.label;
      int maxLabelW = Math.max(1, width - 6);
      int labelY = UiLayoutMath.centerTextY(y, height, this.styleTheme.init_I_l(autismclient.gui.f.b.l.read), 0);
      this.drawTrimmedLabel(context, label, x + 3, labelY, maxLabelW, textColor);
   }

   private void drawTrimmedLabel(GuiGraphicsExtractor context, String label, int x, int y, int maxWidth, int color) {
      String safe = label == null ? "" : label;
      if (!safe.isBlank() && maxWidth > 0) {
         Identifier font = this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read);
         int measured = autismclient.gui.f.b.k.init(this.font, safe, font, color);
         if (measured <= maxWidth) {
            int labelX = x + Math.max(0, (maxWidth - measured) / 2);
            autismclient.gui.f.b.k.init(context, this.font, safe, font, color, labelX, y, false);
         } else {
            float scale = Math.max(0.72F, Math.min(1.0F, (float)maxWidth / Math.max(1, measured)));
            if (scale > 0.72F) {
               int scaledWidth = Math.round(measured * scale);
               int labelX = x + Math.max(0, (maxWidth - scaledWidth) / 2);
               context.pose().pushMatrix();

               try {
                  context.pose().scale(scale, scale);
                  autismclient.gui.f.b.k.init(context, this.font, safe, font, color, Math.round(labelX / scale), Math.round(y / scale), false);
               } finally {
                  context.pose().popMatrix();
               }
            } else {
               String displayLabel = autismclient.gui.f.b.k.load(this.font, safe, Math.round(maxWidth / scale), font, color);
               int scaledWidth = Math.round(autismclient.gui.f.b.k.init(this.font, displayLabel, font, color) * scale);
               int labelX = x + Math.max(0, (maxWidth - scaledWidth) / 2);
               context.pose().pushMatrix();

               try {
                  context.pose().scale(scale, scale);
                  autismclient.gui.f.b.k.init(context, this.font, displayLabel, font, color, Math.round(labelX / scale), Math.round(y / scale), false);
               } finally {
                  context.pose().popMatrix();
               }
            }
         }
      }
   }

   private void renderActionRowContent(
      GuiGraphicsExtractor context,
      MacroAction action,
      String label,
      boolean conditional,
      int actionIndex,
      int actionY,
      int mouseX,
      int mouseY,
      int xOffset,
      float alpha,
      boolean interactive
   ) {
      int rowLeft = this.contentX() + 1 + xOffset;
      int rowRight = this.rowButtonsRight() + xOffset;
      AutismMacroEditorOverlay.BlockGroupLayout group = this.init_AutismMacroEditorO_I_MacroAction(actionIndex, action);
      boolean groupBody = group.hasHeader() && !group.header;
      boolean multiBody = group.multi && !group.header;
      boolean groupHeader = group.hasHeader() && group.header;
      boolean gateEnd = group.gate && action instanceof autismclient.util.macro.EndPacketGateAction;
      boolean allowMove = !groupBody || multiBody || gateEnd;
      boolean allowDuplicate = (!group.hasHeader() || multiBody)
         && !(action instanceof autismclient.util.macro.PacketGateAction)
         && !(action instanceof autismclient.util.macro.EndPacketGateAction);
      boolean allowDelete = (!groupBody || multiBody) && !(action instanceof autismclient.util.macro.EndPacketGateAction);
      int bodyIndent = groupBody ? 10 : 0;
      boolean hoverRow = interactive && mouseX >= rowLeft - 1 && mouseX <= rowRight + 1 && mouseY >= actionY && mouseY < actionY + this.rowHeight();
      boolean selectedRow = interactive && this.isSelected(actionIndex);
      int bgColor = selectedRow ? ThemePaletteManager.recolor(1715082778, ThemePaletteManager.ColorRole.ACCENT) : (hoverRow ? UiColorPalette.rowHover() : UiColorPalette.rowSurface());
      autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(
         context, rowLeft, actionY + 1, rowRight - rowLeft, this.rowHeight() - 2, autismclient.gui.ui.layout.LayoutContext.scaleAlpha(bgColor, alpha)
      );
      AutismMacroEditorOverlay.RowHighlightFlags impact = this.init_AutismMacroEditorO_I(actionIndex);
      if (impact.impacted) {
         int rh = this.rowHeight();
         int rgb = (impact.ping ? UiColorPalette.blue() : UiColorPalette.amber()) & 16777215;
         int tint = autismclient.gui.ui.layout.LayoutContext.scaleAlpha((impact.header ? 77 : 51) << 24 | rgb, alpha);
         int edge = autismclient.gui.ui.layout.LayoutContext.scaleAlpha(-805306368 | rgb, alpha);
         autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(context, rowLeft, actionY + 1, rowRight - rowLeft, rh - 2, tint);
         autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(context, rowLeft, actionY + 1, 1, rh - 2, edge);
         if (impact.first) {
            autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(context, rowLeft, actionY + 1, rowRight - rowLeft, 1, edge);
         }

         if (impact.last) {
            autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(context, rowLeft, actionY + rh - 2, rowRight - rowLeft, 1, edge);
         }
      }

      if (selectedRow) {
         autismclient.gui.f.PacketUtil.load(
            context,
            autismclient.gui.f.NetHelper.init(rowLeft, actionY + 1, rowRight - rowLeft, this.rowHeight() - 2),
            autismclient.gui.ui.layout.LayoutContext.scaleAlpha(ThemePaletteManager.recolor(-42149, ThemePaletteManager.ColorRole.OUTLINE), alpha)
         );
      }

      if (group.hasHeader()) {
         int railX = this.contentX() + 2 + xOffset;
         int railTop = group.isHeaderRow() ? actionY + 3 : actionY;
         int railBottom = group.isLastRow() ? actionY + this.rowHeight() - 3 : actionY + this.rowHeight();
         int railColor = autismclient.gui.ui.layout.LayoutContext.scaleAlpha(group.gate ? UiColorPalette.cyan() : this.styleTheme.run(), alpha);
         autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(context, railX, railTop, 2, railBottom - railTop, railColor);
      }

      int badgeColor = group.gate ? UiColorPalette.cyan() : (groupHeader ? this.styleTheme.run() : (conditional ? -30720 : -12268476));
      if (!groupHeader) {
         autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(
            context, this.contentX() + 2 + xOffset + bodyIndent, actionY + 3, 3, this.rowHeight() - 6, autismclient.gui.ui.layout.LayoutContext.scaleAlpha(badgeColor, alpha)
         );
      }

      autismclient.gui.f.b.k.init(
         context,
         this.font,
         String.valueOf(actionIndex + 1),
         this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
         autismclient.gui.ui.layout.LayoutContext.scaleAlpha(UiColorPalette.textSecondary(), alpha),
         this.contentX() + 8 + xOffset + bodyIndent,
         this.rowTextY(actionY),
         false
      );
      int nameX = this.contentX() + 22 + xOffset + bodyIndent;
      int controlsX = this.rowButtonsX(action, allowMove, allowDuplicate, allowDelete) + xOffset;
      int contentRight = this.rowContentRight(action, allowMove, allowDuplicate, allowDelete) + xOffset;
      int maxNameW = Math.max(1, contentRight - nameX - 8);
      if (group.hasHeader() && !group.roleLabel.isEmpty()) {
         int roleColor = autismclient.gui.ui.layout.LayoutContext.scaleAlpha(group.gate ? UiColorPalette.cyan() : (groupHeader ? -10281 : UiColorPalette.textBody()), alpha);
         String role = group.roleLabel;
         int roleW = autismclient.gui.f.b.k.init(this.font, role, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load), roleColor);
         autismclient.gui.f.b.k.init(
            context,
            this.font,
            role,
            this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load),
            roleColor,
            nameX,
            this.rowTextY(actionY),
            false
         );
         nameX += roleW + 6;
         maxNameW = Math.max(1, contentRight - nameX - 8);
      }

      int nameColor = action instanceof autismclient.util.macro.MissingAddonAction ? -6645094 : -1;
      String displayName = autismclient.gui.f.b.k.init(
         this.font,
         label,
         maxNameW,
         this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
         autismclient.gui.ui.layout.LayoutContext.scaleAlpha(nameColor, alpha)
      );
      autismclient.gui.f.b.k.init(
         context,
         this.font,
         displayName,
         this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
         autismclient.gui.ui.layout.LayoutContext.scaleAlpha(nameColor, alpha),
         nameX,
         this.rowTextY(actionY),
         false
      );
      if (interactive && action != null) {
         int controlY = this.rowButtonY(actionY);
         int cx = controlsX;
         if (allowMove) {
            this.renderRowIconButton(
               context,
               cx,
               controlY,
               this.rowButtonSize(),
               this.rowButtonSize(),
               mouseX >= cx && mouseX < cx + this.rowButtonSize() && mouseY >= controlY && mouseY < controlY + this.rowButtonSize(),
               false,
               GuiIconAtlas.CHEVRON_UP
            );
            cx += this.rowButtonStride();
            this.renderRowIconButton(
               context,
               cx,
               controlY,
               this.rowButtonSize(),
               this.rowButtonSize(),
               mouseX >= cx && mouseX < cx + this.rowButtonSize() && mouseY >= controlY && mouseY < controlY + this.rowButtonSize(),
               false,
               GuiIconAtlas.CHEVRON_DOWN
            );
            cx += this.rowButtonStride();
         }

         if (allowDuplicate) {
            this.renderRowIconButton(
               context,
               cx,
               controlY,
               this.rowButtonSize(),
               this.rowButtonSize(),
               mouseX >= cx && mouseX < cx + this.rowButtonSize() && mouseY >= controlY && mouseY < controlY + this.rowButtonSize(),
               false,
               GuiIconAtlas.DUPLICATE
            );
            cx += this.rowButtonStride();
         }

         boolean hasEditor = this.isEditable(action);
         if (hasEditor) {
            this.renderRowIconButton(
               context,
               cx,
               controlY,
               this.rowButtonSize(),
               this.rowButtonSize(),
               mouseX >= cx && mouseX < cx + this.rowButtonSize() && mouseY >= controlY && mouseY < controlY + this.rowButtonSize(),
               false,
               GuiIconAtlas.EDIT
            );
            cx += this.rowButtonStride();
         }

         if (allowDelete) {
            this.renderRowIconButton(
               context,
               cx,
               controlY,
               this.rowButtonSize(),
               this.rowButtonSize(),
               mouseX >= cx && mouseX < cx + this.rowButtonSize() && mouseY >= controlY && mouseY < controlY + this.rowButtonSize(),
               true,
               GuiIconAtlas.DELETE
            );
         }
      }
   }

   private void drawText(GuiGraphicsExtractor context, String text, int x, int y, int color) {
      autismclient.gui.f.b.k.init(context, this.font, text, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), color, x, y, false);
   }

   private void drawThemedText(GuiGraphicsExtractor context, String text, int x, int y, int color) {
      autismclient.gui.f.b.k.init(context, this.font, text, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load), color, x, y, false);
   }

   private void renderDeleteIcon(GuiGraphicsExtractor context, int x, int y, boolean hovered) {
      this.renderRowIconButton(context, x, y, 14, 12, hovered, true, GuiIconAtlas.DELETE);
   }

   private void renderSelectableRow(GuiGraphicsExtractor context, int x, int y, int width, String label, boolean hovered, boolean selected) {
      int bg = selected ? UiColorPalette.rowSelected() : (hovered ? UiColorPalette.rowHover() : UiColorPalette.rowSurface());
      autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(context, x, y, width, 12, bg);
      int textX = x + 3;
      this.drawText(context, label, textX, y + 2, selected ? UiColorPalette.selectedText() : UiColorPalette.menuItemText());
   }

   private boolean isOverTitleBar(double mouseX, double mouseY) {
      return mouseX >= this.editorX && mouseX <= this.editorX + this.editorWidth && mouseY >= this.editorY && mouseY <= this.editorY + this.styleTheme.load();
   }

   private boolean update_Z_D_D_2(double mouseX, double mouseY) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.init(
         autismclient.gui.f.NetHelper.init(this.editorX, this.editorY, this.editorWidth, this.layoutWindowHeight()), this.styleTheme.load(), mouseX, mouseY
      );
   }

   @Override
   protected boolean supportsPinning() {
      return true;
   }

   @Override
   protected autismclient.gui.f.NetHelper getPinButtonRect(OverlayBounds bounds) {
      autismclient.gui.f.NetHelper window = autismclient.gui.f.NetHelper.init(bounds.x, bounds.y, bounds.width, this.styleTheme.load());
      autismclient.gui.f.NetHelper bar = autismclient.gui.ui.widgets.WindowHeaderHelper.init(window, this.styleTheme.load());
      autismclient.gui.f.NetHelper close = autismclient.gui.ui.widgets.WindowHeaderHelper.load(window, this.styleTheme.load());
      int size = Math.min(12, Math.max(9, this.styleTheme.load() - 4));
      int x = close.read() - 3 - size;
      int y = bar.write() + Math.max(1, (bar.sync() - size) / 2);
      return autismclient.gui.f.NetHelper.init(x, y, size, size);
   }

   private OverlayBounds load_cH() {
      return new OverlayBounds(this.editorX, this.editorY, this.editorWidth, this.layoutWindowHeight(), this.visible, this.collapsed);
   }

   private int layoutWindowHeight() {
      this.bodySpacer.withHeight(Math.max(0, this.editorHeight - this.styleTheme.load()));
      this.windowNode.setExpanded(!this.collapsed);
      autismclient.gui.ui.layout.LayoutContext metrics = this.layoutHost.createMeasureContext();
      if (metrics == null) {
         return this.collapsed ? this.styleTheme.load() : this.editorHeight;
      } else {
         return Math.max(this.styleTheme.load(), Math.round(this.windowNode.measureHeight(metrics, this.editorWidth)));
      }
   }

   private void renderPalette(GuiGraphicsExtractor context, int mouseX, int mouseY) {
      int pickerX = this.pickerX();
      int pickerY = this.pickerY();
      int pickerW = this.pickerWidth();
      int pickerH = this.pickerHeight();
      String title = this.paletteMode == AutismMacroEditorOverlay.PaletteSectionKind.ACTION
         ? "Add Action"
         : (this.paletteMode == AutismMacroEditorOverlay.PaletteSectionKind.PRESET ? "Presets" : "Add Conditional");
      autismclient.gui.ui.widgets.WindowFrameRenderer.init(
         autismclient.gui.f.UiHelper.init(context, this.font, mouseX, mouseY),
         autismclient.gui.f.NetHelper.init(pickerX, pickerY, pickerW, pickerH),
         title,
         false,
         false,
         true,
         mouseX >= pickerX && mouseX < pickerX + pickerW && mouseY >= pickerY && mouseY < pickerY + 16,
         true,
         4,
         4,
         16
      );
      this.layoutPaletteSearchField();
      if (this.paletteSearchField != null) {
         this.paletteSearchField.render(context, mouseX, mouseY, 0.0F);
      }

      int contentX = pickerX + 6;
      int contentY = pickerY + 42;
      int scrollbarGutter = 7;
      int contentW = Math.max(1, pickerW - 12 - scrollbarGutter);
      int contentH = Math.max(1, pickerH - 48);
      int gridY = contentY;
      int gridH = contentH;
      List<AutismMacroEditorOverlay.PaletteCategoryLayout> layouts = this.layoutPaletteCategories(contentX, contentY - this.pickerScroll, contentW);
      int contentHeight = this.paletteContentHeight();
      int maxScroll = Math.max(0, contentHeight - contentH);
      this.pickerScroll = Math.max(0, Math.min(this.pickerScroll, maxScroll));
      autismclient.gui.ui.UiClipStack.getInstance()
         .push(context, autismclient.gui.f.NetHelper.init(contentX, contentY, Math.max(0, contentW), Math.max(0, contentH)));

      try {
         for (AutismMacroEditorOverlay.PaletteCategoryLayout section : layouts) {
            int sectionTop = section.headerY;
            int sectionBottom = section.bottomY;
            if (sectionBottom >= gridY && sectionTop <= gridY + gridH) {
               String headerLabel = autismclient.gui.f.b.k.load(
                  this.font,
                  section.category.label,
                  Math.max(1, section.width - 4),
                  this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load),
                  section.category.color
               );
               autismclient.gui.f.b.k.init(
                  context,
                  this.font,
                  headerLabel,
                  this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load),
                  section.category.color,
                  section.x,
                  UiLayoutMath.centerTextY(sectionTop, this.sectionLabelHeight(), this.styleTheme.init_I_l(autismclient.gui.f.b.l.load), 0),
                  false
               );
            }

            for (AutismMacroEditorOverlay.PaletteButtonBounds button : section.buttons) {
               int cardX = button.x;
               int cardY = button.y;
               int cardW = button.width;
               int cardH = button.height;
               if (cardY + cardH >= gridY && cardY <= gridY + gridH) {
                  boolean hovered = mouseX >= cardX && mouseX < cardX + cardW && mouseY >= cardY && mouseY < cardY + cardH;
                  this.renderPaletteCard(context, section.category, button.entry, cardX, cardY, cardW, cardH, hovered, mouseX, mouseY);
                  if (hovered) {
                     this.hoverTooltip = button.entry.description;
                     this.tooltipX = mouseX + 8;
                     this.tooltipY = mouseY + 12;
                  }
               }
            }
         }
      } finally {
         autismclient.gui.ui.UiClipStack.getInstance().pop(context);
      }

      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics var33 = this.paletteScrollbarMetrics();
      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(context, var33, var33.containsTrack(mouseX, mouseY), this.scrollbarDragMode == 2);
   }

   private int sectionLabelHeight() {
      return Math.max(11, this.styleTheme.init_I_l(autismclient.gui.f.b.l.load));
   }

   private boolean paletteMouseClicked(double mouseX, double mouseY, int button) {
      int pickerX = this.pickerX();
      int pickerY = this.pickerY();
      int pickerW = this.pickerWidth();
      int pickerH = this.pickerHeight();
      int closeX = this.paletteCloseX();
      int closeY = this.paletteCloseY();
      boolean inside = mouseX >= pickerX && mouseX < pickerX + pickerW && mouseY >= pickerY && mouseY < pickerY + pickerH;
      if (!inside) {
         this.closePalette();
         return true;
      }

      if (button != 0) {
         return true;
      }

      if (mouseX >= closeX && mouseX < closeX + 12 && mouseY >= closeY && mouseY < closeY + 12) {
         this.closePalette();
         return true;
      }

      int contentX = pickerX + 6;
      int contentY = pickerY + 42;
      int contentW = pickerW - 19;
      int contentH = pickerH - 48;
      this.layoutPaletteSearchField();
      if (this.paletteSearchField != null && this.paletteSearchField.mouseClicked(mouseX, mouseY, button)) {
         return true;
      }

      if (this.paletteSearchField != null) {
         this.paletteSearchField.setFocused(false);
      }

      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics stepPickerScrollbar = this.paletteScrollbarMetrics();
      if (stepPickerScrollbar.isScrollable() && stepPickerScrollbar.containsTrack((int)mouseX, (int)mouseY)) {
         this.scrollbarDragMode = 2;
         boolean onThumb = mouseY >= stepPickerScrollbar.thumbY() && mouseY < stepPickerScrollbar.thumbY() + stepPickerScrollbar.thumbHeight();
         this.scrollbarGrabOffset = onThumb ? Math.max(0, (int)mouseY - stepPickerScrollbar.thumbY()) : stepPickerScrollbar.thumbHeight() / 2;
         this.pickerScroll = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(stepPickerScrollbar, (int)mouseY, this.scrollbarGrabOffset);
         return true;
      }

      if (!(mouseX < contentX) && !(mouseX >= contentX + contentW) && !(mouseY < contentY) && !(mouseY >= contentY + contentH)) {
         for (AutismMacroEditorOverlay.PaletteCategoryLayout section : this.layoutPaletteCategories(contentX, contentY - this.pickerScroll, contentW)) {
            for (AutismMacroEditorOverlay.PaletteButtonBounds buttonLayout : section.buttons) {
               int cardX = buttonLayout.x;
               int cardY = buttonLayout.y;
               int cardW = buttonLayout.width;
               int cardH = buttonLayout.height;
               if (mouseX >= cardX && mouseX < cardX + cardW && mouseY >= cardY && mouseY < cardY + cardH) {
                  this.closePalette();
                  buttonLayout.entry.action.run();
                  return true;
               }
            }
         }

         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.visible) {
         return false;
      }

      if (this.contextMenu.isOpen()) {
         if (button == 0) {
            if (this.contextMenu.init(mouseX, mouseY, button, (idx, label, i) -> this.onContextMenuItem(idx, i))) {
               return true;
            }
         } else if (button == 1 && !this.contextMenu.handleClick(mouseX, mouseY)) {
            this.contextMenu.close();
         }
      }

      if (!this.dropdowns.isEmpty()) {
         if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(this.dropdowns, mouseX, mouseY, button)) {
            return true;
         }

         if (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
            return true;
         }
      }

      if (this.listeningForKeybind && ValueSink.load_Z(button)) {
         this.macro.keyCode = ValueSink.init_I(button);
         this.listeningForKeybind = false;
         this.rebuildButtons();
         this.clearSelection();
         return true;
      }

      if (button == 0 && this.isOverTitleBar(mouseX, mouseY)) {
         if (this.isOverPinButton(mouseX, mouseY, this.load_cH())) {
            this.togglePinned();
            return true;
         } else if (this.update_Z_D_D_2(mouseX, mouseY)) {
            this.update_V_10();
            return true;
         } else {
            this.clearSelection();
            this.headerPressed = true;
            this.headerMoved = false;
            this.dragOffsetX = mouseX - this.editorX;
            this.dragOffsetY = mouseY - this.editorY;
            this.headerPressX = mouseX;
            this.headerPressY = mouseY;
            this.headerPressEditorX = this.editorX;
            this.headerPressEditorY = this.editorY;
            return true;
         }
      } else {
         if (this.collapsed) {
            return false;
         }

         if (this.isPaletteOpen()) {
            return this.paletteMouseClicked(mouseX, mouseY, button);
         }

         MouseButtonEvent click = new MouseButtonEvent(mouseX, mouseY, Compat263.newGlfwMouseButtonInfo(button, 0));
         boolean fieldClicked = false;
         boolean stepListLocked = this.isStepListLocked();
         if (!stepListLocked && this.nameField.mouseClicked(click, false)) {
            this.nameField.setFocused(true);
            if (this.loopCountField != null) {
               this.loopCountField.setFocused(false);
            }

            fieldClicked = true;
         } else if (!stepListLocked && this.loopMode == 1 && this.loopCountField != null && this.loopCountField.mouseClicked(click, false)) {
            this.loopCountField.setFocused(true);
            this.nameField.setFocused(false);
            fieldClicked = true;
         }

         if (fieldClicked) {
            this.clearSelection();
            return true;
         }

         this.nameField.setFocused(false);
         if (this.loopCountField != null) {
            this.loopCountField.setFocused(false);
         }

         this.refreshUndoRedoButtons();

         for (int i = 0; i < this.buttonLabels.size(); i++) {
            int[] b = this.buttonBounds.get(i);
            if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
               this.clearSelection();
               boolean enabled = this.buttonEnabled.get(i);
               boolean macroCurrentlyRunning = this.isRunning();
               String label = i == this.runButtonIndex ? this.runButtonLabel(macroCurrentlyRunning) : this.buttonLabels.get(i);
               autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant = autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY;
               if (i == this.runButtonIndex) {
                  variant = macroCurrentlyRunning
                     ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER
                     : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS;
               }

               Runnable secondary = i < this.buttonSecondaryActions.size() ? this.buttonSecondaryActions.get(i) : null;
               autismclient.gui.ui.widgets.OverlayActionButton buttonView = this.createActionButton(b[0], b[1], b[2], b[3], label, variant, enabled, this.buttonActions.get(i), secondary);
               autismclient.gui.ui.widgets.OverlayActionButton.handleClick(buttonView, mouseX, mouseY, button);
               return true;
            }
         }

         int listY = this.listTopY();
         int listHeight = this.listAreaHeight();
         if (mouseY >= listY && mouseY <= listY + listHeight && mouseX >= this.contentX() && mouseX <= this.contentX() + this.contentWidth()) {
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics stepListScrollbar = this.listScrollbarMetrics();
            if (button == 0 && stepListScrollbar.isScrollable() && stepListScrollbar.containsTrack((int)mouseX, (int)mouseY)) {
               this.clearSelection();
               this.scrollbarDragMode = 1;
               boolean onThumb = mouseY >= stepListScrollbar.thumbY() && mouseY < stepListScrollbar.thumbY() + stepListScrollbar.thumbHeight();
               this.scrollbarGrabOffset = onThumb ? Math.max(0, (int)mouseY - stepListScrollbar.thumbY()) : stepListScrollbar.thumbHeight() / 2;
               this.listScroll = this.quantizeScrollOffset(
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(stepListScrollbar, (int)mouseY, this.scrollbarGrabOffset), this.rowHeight(), stepListScrollbar.maxScroll()
               );
               return true;
            }

            int y = listY - this.listScroll;
            int controlSize = this.rowButtonSize();
            int controlStride = this.rowButtonStride();
            int index = (int)((mouseY - y) / this.rowHeight());
            if (this.isStepListLocked()) {
               this.clearSelection();
               this.resetDragState();
               if (button == 1 && index >= 0 && index < this.macro.actions.size()) {
                  int lockedRowY = y + index * this.rowHeight();
                  if (this.isRowVisible(lockedRowY) && mouseX < this.rowButtonsRight()) {
                     this.contextMenu.open((int)mouseX, (int)mouseY, index);
                  }
               }

               return true;
            }

            if (index >= 0 && index < this.macro.actions.size()) {
               MacroAction action = this.macro.actions.get(index);
               AutismMacroEditorOverlay.BlockGroupLayout group = this.init_AutismMacroEditorO_I_MacroAction(index, action);
               boolean groupBody = group.hasHeader() && !group.header;
               boolean multiBody = group.multi && !group.header;
               boolean gateEnd = group.gate && action instanceof autismclient.util.macro.EndPacketGateAction;
               boolean allowMove = !groupBody || multiBody || gateEnd;
               boolean allowDuplicate = (!group.hasHeader() || multiBody)
                  && !(action instanceof autismclient.util.macro.PacketGateAction)
                  && !(action instanceof autismclient.util.macro.EndPacketGateAction);
               boolean allowDelete = (!groupBody || multiBody) && !(action instanceof autismclient.util.macro.EndPacketGateAction);
               int actionY = y + index * this.rowHeight();
               if (!this.isRowVisible(actionY)) {
                  return true;
               }

               int controlY = this.rowButtonY(actionY);
               int cx = this.rowButtonsX(action, allowMove, allowDuplicate, allowDelete);
               if (allowMove) {
                  if (mouseX >= cx && mouseX < cx + controlSize) {
                     if (this.isSelected(index) && this.moveSelection(-1)) {
                        return true;
                     }

                     if (index > 0) {
                        this.recordUndo();
                        this.moveAction(index, index - 1);
                     }

                     return true;
                  }

                  cx += controlStride;
                  if (mouseX >= cx && mouseX < cx + controlSize) {
                     if (this.isSelected(index) && this.moveSelection(1)) {
                        return true;
                     }

                     if (index < this.macro.actions.size() - 1) {
                        this.recordUndo();
                        int target = index + 1;
                        if (action instanceof autismclient.util.macro.RaceAction race) {
                           target = Math.min(this.macro.actions.size() - 1, index + race.countBodyActions(this.macro.actions, index) + 1);
                        } else if (action instanceof autismclient.util.macro.ReportAction report) {
                           target = Math.min(this.macro.actions.size() - 1, index + report.coveredCount(this.macro.actions, index) + 1);
                        } else if (action instanceof autismclient.util.macro.MultiAction multi) {
                           target = Math.min(this.macro.actions.size() - 1, index + multi.init(this.macro.actions, index) + 1);
                        } else if (action instanceof autismclient.util.macro.PacketGateAction) {
                           target = Math.min(this.macro.actions.size() - 1, index + this.sync_I(index) + 1);
                        }

                        this.moveAction(index, target);
                     }

                     return true;
                  }

                  cx += controlStride;
               }

               if (allowDuplicate && mouseX >= cx && mouseX < cx + controlSize) {
                  this.sync_V(index);
                  this.clearSelection();
                  return true;
               }

               if (allowDuplicate) {
                  cx += controlStride;
               }

               if (mouseX >= cx && mouseX < cx + controlSize && this.isEditable(action)) {
                  this.editAction(index, action);
                  return true;
               }

               if (this.isEditable(action)) {
                  cx += controlStride;
               }

               if (allowDelete && mouseX >= cx && mouseX < cx + controlSize) {
                  if (this.isSelected(index)) {
                     this.prev_Z_2();
                  } else {
                     this.recordUndo();
                     this.flush_V(index);
                  }

                  return true;
               }

               if (button == 0 && mouseX < this.rowButtonsRight()) {
                  boolean shiftDown = this.isShiftDown();
                  boolean controlDown = this.isCtrlDown();
                  if (shiftDown && this.selectRangeToAnchor(index)) {
                     return true;
                  }

                  this.selectionAnchorId = this.stableId(this.macro.actions.get(index));
                  if (!controlDown && !shiftDown) {
                     this.beginRowPress(index, mouseX, mouseY, allowMove);
                  } else {
                     this.toggleUnitSelection(index);
                  }

                  return true;
               }

               if (button == 1 && mouseX < this.rowButtonsRight()) {
                  this.openRowContextMenu(index, (int)mouseX, (int)mouseY);
                  return true;
               }
            }

            if (button == 0) {
               this.clearSelection();
               return true;
            }
         }

         if (button == 0) {
            this.clearSelection();
         }

         return false;
      }
   }

   private void sync_V(int index) {
      if (!this.isStepListLocked() && index >= 0 && index < this.macro.actions.size()) {
         AutismMacroEditorOverlay.BlockGroupLayout group = this.init_AutismMacroEditorO_I_MacroAction(index, this.macro.actions.get(index));
         if (group.hasHeader() && !group.multi) {
            return;
         }

         if (group.multi && group.header) {
            return;
         }

         MacroAction original = this.macro.actions.get(index);
         this.recordUndo();
         CompoundTag tag = original.toTag();
         MacroAction copy = AutismMacro.deserializeAction(tag);
         if (copy != null) {
            this.macro.actions.add(index + 1, copy);
            int header = this.read_I_2(index);
            if (header >= 0 && this.macro.actions.get(header) instanceof autismclient.util.macro.RaceAction race) {
               race.bodyCount = Math.max(0, race.bodyCount + 1);
            } else {
               int reportHeader = this.write_I_2(index);
               if (reportHeader >= 0 && this.macro.actions.get(reportHeader) instanceof autismclient.util.macro.ReportAction report) {
                  report.conditionCount = Math.max(0, report.conditionCount + 1);
               } else {
                  int multiHeader = this.update_I_2(index);
                  if (multiHeader >= 0 && this.macro.actions.get(multiHeader) instanceof autismclient.util.macro.MultiAction multi) {
                     multi.stepCount = Math.max(0, multi.stepCount + 1);
                  }
               }
            }

            this.onActionsChanged();
         }
      }
   }

   private void flush_V(int index) {
      if (!this.isStepListLocked() && this.macro != null && index >= 0 && index < this.macro.actions.size() && !this.flush_Z(index)) {
         MacroAction action = this.macro.actions.get(index);
         ActionEditorOverlay editor = ActionEditorOverlay.getExisting();
         if (action instanceof autismclient.util.macro.RaceAction race) {
            int count = race.countBodyActions(this.macro.actions, index);
            int end = Math.min(this.macro.actions.size(), index + count + 1);
            if (editor != null) {
               for (int i = index; i < end; i++) {
                  editor.closeIfEditing(this.macro.actions.get(i));
               }
            }

            this.macro.actions.subList(index, end).clear();
            this.onActionsChanged();
         } else if (action instanceof autismclient.util.macro.ReportAction report) {
            int count = report.coveredCount(this.macro.actions, index);
            int end = Math.min(this.macro.actions.size(), index + count + 1);
            if (editor != null) {
               for (int i = index; i < end; i++) {
                  editor.closeIfEditing(this.macro.actions.get(i));
               }
            }

            this.macro.actions.subList(index, end).clear();
            this.onActionsChanged();
         } else if (action instanceof autismclient.util.macro.PacketGateAction) {
            int count = this.sync_I(index);
            int end = Math.min(this.macro.actions.size(), index + count + 1);
            if (editor != null) {
               for (int i = index; i < end; i++) {
                  editor.closeIfEditing(this.macro.actions.get(i));
               }
            }

            this.macro.actions.subList(index, end).clear();
            this.onActionsChanged();
         } else if (action instanceof autismclient.util.macro.MultiAction) {
            if (editor != null) {
               editor.closeIfEditing(action);
            }

            this.macro.actions.remove(index);
            this.onActionsChanged();
         } else {
            if (editor != null) {
               editor.closeIfEditing(action);
            }

            int header = this.read_I_2(index);
            int reportHeader = header < 0 ? this.write_I_2(index) : -1;
            int multiHeader = header < 0 && reportHeader < 0 ? this.update_I_2(index) : -1;
            this.macro.actions.remove(index);
            if (header >= 0 && header < this.macro.actions.size() && this.macro.actions.get(header) instanceof autismclient.util.macro.RaceAction race) {
               race.bodyCount = Math.max(0, race.bodyCount - 1);
            } else if (reportHeader >= 0
               && reportHeader < this.macro.actions.size()
               && this.macro.actions.get(reportHeader) instanceof autismclient.util.macro.ReportAction report) {
               report.conditionCount = Math.max(0, report.conditionCount - 1);
            } else if (multiHeader >= 0
               && multiHeader < this.macro.actions.size()
               && this.macro.actions.get(multiHeader) instanceof autismclient.util.macro.MultiAction multi) {
               multi.stepCount = Math.max(0, multi.stepCount - 1);
            }

            this.onActionsChanged();
         }
      }
   }

   private String buttonTooltip(String label) {
      String clean = label.replaceAll("§.", "");
      String var10000;
      switch (clean) {
         case "Save":
            if (this.macro == null) {
               var10000 = "Save this macro";
            } else {
               String name = this.macro.name == null ? "" : this.macro.name.trim();
               var10000 = name.isEmpty()
                  ? "Give the macro a name first"
                  : (!this.init_Z_Str_3(this.macro.name) ? "Pick a unique macro name before saving" : "Save this macro");
            }
            break;
         case "Run":
            String blockedReason = this.runBlocker();
            var10000 = blockedReason != null ? blockedReason : "Run this macro";
            break;
         case "Stop":
            var10000 = "Stop the running macro";
            break;
         case "Add Action":
            var10000 = "Open categorized action picker";
            break;
         case "Add Conditional":
            var10000 = "Open categorized wait/condition picker";
            break;
         case "Presets":
            var10000 = "Append editable preset steps";
            break;
         case "Undo":
            var10000 = "Undo the last macro edit";
            break;
         case "Redo":
            var10000 = "Redo the last undone macro edit";
            break;
         case "Chat":
            var10000 = "Send a chat message or /command";
            break;
         case "Delay":
            var10000 = "Wait N milliseconds or ticks";
            break;
         case "Packet":
            var10000 = "Send a captured network packet";
            break;
         case "Item Click":
            var10000 = "Click inventory items or slots";
            break;
         case "Pick Up All":
            var10000 = "Pick up matching stacks into the carried item";
            break;
         case "Mouse Clk":
            var10000 = "Simulate L/R/M click in world";
            break;
         case "Rotate":
            var10000 = "Set player yaw & pitch";
            break;
         case "Use Item":
            var10000 = "Use held item (right-click action)";
            break;
         case "Open Inv":
            var10000 = "Open player inventory screen";
            break;
         case "Slot":
            var10000 = "Select a hotbar slot (1-9)";
            break;
         case "Drop":
            var10000 = "Drop items from slot";
            break;
         case "Close GUI":
            var10000 = "Close the current screen/GUI";
            break;
         case "Swap":
            var10000 = "Swap two inventory slots";
            break;
         case "Wait HP":
            var10000 = "Pause until health crosses the selected threshold";
            break;
         case "Wait Block":
            var10000 = "Pause until block changes";
            break;
         case "Wait Pkt":
            var10000 = "Pause until packet received";
            break;
         case "Wait Item":
            var10000 = "Pause until item in inventory";
            break;
         case "WaitGUI":
         case "Wait GUI":
            var10000 = "Pause until screen opens";
            break;
         case "Tick Sync":
            var10000 = "Wait for next client tick";
            break;
         case "Rev Sync":
            var10000 = "Sync handler revision";
            break;
         case "Srv Sync":
            var10000 = "Wait for server tick timing";
            break;
         case "Go To":
            var10000 = "Pathfind to a target position";
            break;
         case "Disconn":
            var10000 = "Disconnect / Kick (lag+kick) / Kick Dupe (lag+bundle/action+kick)";
            break;
         case "NBT Book":
            var10000 = "Sign large-data books (random/custom text, multi-book, delay)";
            break;
         case "Wait LAN":
            var10000 = "Wait for LAN sync peer to reach a macro step";
            break;
         case "Module":
            var10000 = "Toggle AUTISM modules";
            break;
         case "Sneak":
            var10000 = "Hold or release sneak key";
            break;
         case "Jump":
            var10000 = "Press jump key for N ticks";
            break;
         case "Sprint":
            var10000 = "Toggle sprint on/off";
            break;
         case "Move":
            var10000 = "Walk in direction for N ticks";
            break;
         case "Look At":
            var10000 = "Look at a block position";
            break;
         case "Repeat":
            var10000 = "Repeat next N steps M times";
            break;
         case "Open Cont":
            var10000 = "Open a container at a block position";
            break;
         case "Desync":
            var10000 = "Send close packet without closing GUI (desync server state)";
            break;
         case "Cls w/o Pkt":
            var10000 = "Close GUI locally without sending close packet to server";
            break;
         case "Restore GUI":
            var10000 = "Restore a previously saved GUI screen and handler";
            break;
         case "Save GUI":
            var10000 = "Save the current GUI screen and handler for later restore";
            break;
         case "Send Toggle":
            var10000 = "Toggle whether packets are sent to server (on/off)";
            break;
         case "Delay Pkts":
            var10000 = "Toggle packet delay mode (queues outgoing packets)";
            break;
         case "Store":
            var10000 = "Loot/store items between container and player, or move slot->slot";
            break;
         case "Wait CD":
            var10000 = "Pause until cooldown expires";
            break;
         case "Wait Pos":
            var10000 = "Pause until position reached";
            break;
         case "Wait Chat":
            var10000 = "Pause until chat message received";
            break;
         case "Wait Ent":
            var10000 = "Pause until entity appears nearby";
            break;
         case "Wait Slot":
            var10000 = "Pause until slot content changes";
            break;
         case "Wait Snd":
            var10000 = "Pause until a matching sound plays";
            break;
         case "Mine":
            var10000 = "Mine target blocks autonomously until stop conditions are met";
            break;
         case "Pay":
            var10000 = "Pay a list of players with a configurable command, amount, and delay";
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.visible) {
         return false;
      }

      if (this.collapsed) {
         return false;
      }

      if (this.listeningForKeybind) {
         if (keyCode == 256) {
            this.macro.keyCode = -1;
         } else {
            this.macro.keyCode = keyCode;
         }

         this.listeningForKeybind = false;
         this.rebuildButtons();
         return true;
      } else {
         KeyEvent keyInput = Compat263.newGlfwKeyEvent(keyCode, scanCode, modifiers);
         if (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns)) {
            if (keyCode == 256 || keyCode == 257) {
               autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns);
            }

            return true;
         } else if (this.isPaletteOpen()) {
            if (this.paletteSearchField != null && this.paletteSearchField.isFocused()) {
               if (keyCode == 256) {
                  this.paletteSearchField.setFocused(false);
                  return true;
               }

               if (this.paletteSearchField.keyPressed(keyInput)) {
                  return true;
               }
            }

            if (keyCode == 256 || keyCode == 257) {
               this.closePalette();
            }

            return true;
         } else if (this.isTextFieldFocused()
            || !this.isStepListLocked()
            || (!this.hasCtrl(modifiers) || keyCode != 67 && keyCode != 86 && keyCode != 88 && keyCode != 68 && keyCode != 90 && keyCode != 82)
               && keyCode != 261
               && keyCode != 259) {
            if (!this.isTextFieldFocused() && this.hasCtrl(modifiers) && keyCode == 90) {
               this.undo();
               return true;
            }

            if (!this.isTextFieldFocused() && this.hasCtrl(modifiers) && keyCode == 82) {
               this.redo();
               return true;
            }

            if (!this.isTextFieldFocused() && this.hasCtrl(modifiers) && keyCode == 88) {
               return this.start_Z_2();
            }

            if (!this.isTextFieldFocused() && this.hasCtrl(modifiers) && keyCode == 65) {
               return this.selectAll();
            }

            if (!this.isTextFieldFocused() && this.hasCtrl(modifiers) && keyCode == 68) {
               this.duplicateSelection();
               return true;
            }

            if (!this.isTextFieldFocused() && this.hasCtrl(modifiers) && keyCode == 67) {
               return this.copySelection();
            }

            if (!this.isTextFieldFocused() && this.hasCtrl(modifiers) && keyCode == 86) {
               return this.pasteClipboard();
            }

            if (!this.isTextFieldFocused() && (keyCode == 261 || keyCode == 259) && this.prev_Z_2()) {
               return true;
            }

            if (keyCode == 256) {
               boolean anyFocused = this.nameField.isFocused() || this.loopMode == 1 && this.loopCountField != null && this.loopCountField.isFocused();
               if (anyFocused) {
                  this.nameField.setFocused(false);
                  if (this.loopCountField != null) {
                     this.loopCountField.setFocused(false);
                  }

                  return true;
               } else {
                  if (this.configurationOnly) {
                     this.update_V_10();
                     return true;
                  }

                  if (this.isJoinMacroMenuMode()) {
                     this.update_V_10();
                     return true;
                  }

                  if (MC.gui.screen() != null) {
                     MC.execute(() -> MC.gui.setScreen(null));
                  } else {
                     this.update_V_10();
                  }

                  return true;
               }
            } else {
               if (keyCode == 257) {
                  if (this.nameField.isFocused()) {
                     this.nameField.setFocused(false);
                     return true;
                  }

                  if (this.loopMode == 1 && this.loopCountField != null && this.loopCountField.isFocused()) {
                     this.loopCountField.setFocused(false);
                     return true;
                  }
               }

               if (this.nameField.keyPressed(keyInput)) {
                  return true;
               } else {
                  return this.loopMode == 1 && this.loopCountField != null && this.loopCountField.keyPressed(keyInput)
                     ? true
                     : this.nameField.isFocused() || this.loopMode == 1 && this.loopCountField != null && this.loopCountField.isFocused();
               }
            }
         } else {
            return true;
         }
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      if (!this.visible) {
         return false;
      }

      if (this.collapsed) {
         return false;
      }

      if (this.isPaletteOpen()) {
         if (this.paletteSearchField != null && this.paletteSearchField.isFocused()) {
            CharacterEvent charInput = new CharacterEvent(chr);
            if (this.paletteSearchField.charTyped(charInput)) {
               return true;
            }

            this.paletteSearchField.insertTextIfFocused(String.valueOf(chr));
         }

         return true;
      } else {
         CharacterEvent charInput = new CharacterEvent(chr);
         if (this.nameField.charTyped(charInput)) {
            return true;
         }

         if (this.nameField.isFocused()) {
            this.nameField.insertTextIfFocused(String.valueOf(chr));
            return true;
         }

         if (this.loopMode == 1 && this.loopCountField != null) {
            if (this.loopCountField.charTyped(charInput)) {
               return true;
            }

            if (this.loopCountField.isFocused()) {
               this.loopCountField.insertTextIfFocused(String.valueOf(chr));
               return true;
            }
         }

         return true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (!this.visible) {
         return false;
      }

      if (!this.dropdowns.isEmpty()
         && (autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns) || autismclient.gui.ui.widgets.DropdownWidget.isPointInAnyPopup(this.dropdowns, mouseX, mouseY))
         && autismclient.gui.ui.widgets.DropdownWidget.handleMouseScroll(this.dropdowns, mouseX, mouseY, amount)) {
         return true;
      }

      if (this.isPaletteOpen()) {
         int pickerX = this.pickerX();
         int pickerY = this.pickerY();
         int pickerW = this.pickerWidth();
         int pickerH = this.pickerHeight();
         int contentX = pickerX + 6;
         int contentY = pickerY + 42;
         int contentW = pickerW - 19;
         int contentH = pickerH - 48;
         if (mouseX >= contentX && mouseX < contentX + contentW && mouseY >= contentY && mouseY < contentY + contentH) {
            int contentHeight = this.paletteContentHeight();
            int maxScroll = Math.max(0, contentHeight - contentH);
            this.pickerScroll = Math.max(0, Math.min(maxScroll, this.pickerScroll - (int)(Math.signum(amount) * 36.0)));
            return true;
         } else {
            return true;
         }
      } else {
         int listY = this.listTopY();
         int listHeight = this.listAreaHeight();
         if (mouseY >= listY && mouseY <= listY + listHeight && mouseX >= this.contentX() + 2 && mouseX <= this.contentX() + this.contentWidth() - 2) {
            int maxScroll = this.maxListScroll();
            this.listScroll = this.quantizeScrollOffset(this.listScroll - (int)(Math.signum(amount) * this.rowHeight()), this.rowHeight(), maxScroll);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseDrag(this.dropdowns, mouseX, mouseY, button)) {
         return true;
      }

      if (this.paletteSearchField != null && this.paletteSearchField.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      }

      if (this.nameField != null && this.nameField.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      }

      if (this.loopCountField != null && this.loopCountField.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      }

      if (this.scrollbarDragMode == 2) {
         this.pickerScroll = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(this.paletteScrollbarMetrics(), (int)mouseY, this.scrollbarGrabOffset);
         return true;
      }

      if (this.scrollbarDragMode == 1) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.listScrollbarMetrics();
         this.listScroll = this.quantizeScrollOffset(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, (int)mouseY, this.scrollbarGrabOffset), this.rowHeight(), metrics.maxScroll());
         return true;
      }

      if (!this.isStepListLocked() || this.draggingRowIndex < 0 && this.pressedRowIndex < 0) {
         if (button != 0 || this.draggingRowIndex < 0 && this.pressedRowIndex < 0) {
            if (!this.headerPressed || button != 0) {
               return false;
            }

            if (!this.headerMoved && Math.abs(mouseX - this.headerPressX) < 3.0 && Math.abs(mouseY - this.headerPressY) < 3.0) {
               return true;
            }

            this.headerMoved = true;
            this.editorX = (int)(mouseX - this.dragOffsetX);
            this.editorY = (int)(mouseY - this.dragOffsetY);
            int screenW = cA.init_I();
            int screenH = cA.load_I();
            int minVisibleWidth = Math.min(this.editorWidth, 96);
            this.editorX = Math.max(Math.min(0, screenW - this.editorWidth), Math.min(this.editorX, Math.max(0, screenW - minVisibleWidth)));
            this.editorY = Math.max(0, Math.min(screenH - 14, this.editorY));
            if (this.editorX != this.headerPressEditorX || this.editorY != this.headerPressEditorY || Math.abs(mouseX - this.headerPressX) >= 3.0 || Math.abs(mouseY - this.headerPressY) >= 3.0) {
               this.headerMoved = true;
            }

            this.rebuildButtons();
            return true;
         } else {
            this.updateRowDrag(mouseX, mouseY);
            if (this.draggingRowIndex >= 0) {
               this.dragRowTo(mouseY);
            }

            return true;
         }
      } else {
         this.resetDragState();
         return true;
      }
   }

   public boolean poll_Z_8() {
      return false;
   }

   public String load_Str_7() {
      return "";
   }

   public String read_Str_5() {
      return "";
   }

   public String init_Str_Slot_3(Slot slot, String itemName, String registryId) {
      return "";
   }

   public boolean push_Z_7() {
      return false;
   }

   public boolean init_Z_Slot_2(Slot slot, String itemName, String registryId) {
      return false;
   }

   private static final class RowHighlightFlags {
      private static final AutismMacroEditorOverlay.RowHighlightFlags NONE = new AutismMacroEditorOverlay.RowHighlightFlags(
         false, false, false, false, false
      );
      private final boolean impacted;
      private final boolean header;
      private final boolean ping;
      private final boolean first;
      private final boolean last;

      private RowHighlightFlags(boolean impacted, boolean header, boolean ping, boolean first, boolean last) {
         this.impacted = impacted;
         this.header = header;
         this.ping = ping;
         this.first = first;
         this.last = last;
      }
   }

   private record UndoSnapshot(CompoundTag macroTag, List<Integer> selectedIndices) {
   }

   private static final class PaletteButtonBounds {
      private final AutismMacroEditorOverlay.PaletteEntry entry;
      private final int x;
      private final int y;
      private final int width;
      private final int height;

      private PaletteButtonBounds(AutismMacroEditorOverlay.PaletteEntry entry, int x, int y, int width, int height) {
         this.entry = entry;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
      }
   }

   public enum EditorMode {
      NORMAL,
      JOIN_MACRO_MENU;
   }

   private static final class ActionRowItem {
      private final MacroAction action;
      private final int stableId;
      private final String label;
      private final boolean conditional;
      private final int index;

      private ActionRowItem(MacroAction action, int stableId, String label, boolean conditional, int index) {
         this.action = action;
         this.stableId = stableId;
         this.label = label;
         this.conditional = conditional;
         this.index = index;
      }
   }

   private static final class RowSelectionSet {
      private final LinkedHashSet<Integer> stableIds = new LinkedHashSet<>();

      private void clear() {
         this.stableIds.clear();
      }

      private boolean isEmpty() {
         return this.stableIds.isEmpty();
      }

      private boolean contains(int stableId) {
         return stableId >= 0 && this.stableIds.contains(stableId);
      }

      private void add(int stableId) {
         if (stableId >= 0) {
            this.stableIds.add(stableId);
         }
      }

      private void deselect(int stableId) {
         if (stableId >= 0) {
            this.stableIds.remove(stableId);
         }
      }

      private void retainLive(Set<Integer> liveStableIds) {
         if (liveStableIds != null && !liveStableIds.isEmpty()) {
            this.stableIds.removeIf(stableId -> !liveStableIds.contains(stableId));
         } else {
            this.stableIds.clear();
         }
      }
   }

   private enum PaletteSectionKind {
      ACTION,
      CONDITIONAL,
      PRESET;
   }

   private static final class PaletteEntry {
      private final String categoryId;
      private final String label;
      private final String description;
      private final Runnable action;

      private PaletteEntry(String categoryId, String label, String description, Runnable action) {
         this.categoryId = categoryId;
         this.label = label;
         this.description = description;
         this.action = action;
      }
   }

   private enum RowAnimKind {
      ENTER,
      MOVE,
      EXIT;
   }

   private final class OverlayAdapter implements AutismOverlay {
      @Override
      public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
         if (this.isVisible()) {
            context.nextStratum();
            AutismMacroEditorOverlay.this.renderPalette(context, mouseX, mouseY);
         }
      }

      @Override
      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         return AutismMacroEditorOverlay.this.paletteMouseClicked(mouseX, mouseY, button);
      }

      @Override
      public boolean mouseReleased(double mouseX, double mouseY, int button) {
         return AutismMacroEditorOverlay.this.mouseReleased(mouseX, mouseY, button);
      }

      @Override
      public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
         return AutismMacroEditorOverlay.this.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }

      @Override
      public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
         return AutismMacroEditorOverlay.this.mouseScrolled(mouseX, mouseY, amount);
      }

      @Override
      public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         return AutismMacroEditorOverlay.this.keyPressed(keyCode, scanCode, modifiers);
      }

      @Override
      public boolean charTyped(char chr, int modifiers) {
         return AutismMacroEditorOverlay.this.charTyped(chr, modifiers);
      }

      @Override
      public boolean isVisible() {
         return AutismMacroEditorOverlay.this.visible && AutismMacroEditorOverlay.this.isPaletteOpen();
      }

      @Override
      public void setVisible(boolean visible) {
         if (!visible) {
            AutismMacroEditorOverlay.this.closePalette();
         }
      }

      @Override
      public boolean containsPoint(double mouseX, double mouseY) {
         return this.isVisible();
      }

      @Override
      public boolean isOverHeader(double mouseX, double mouseY) {
         return false;
      }

      @Override
      public String getOverlayId() {
         return "macro-step-picker";
      }

      @Override
      public OverlayBounds getBounds() {
         int width = AutismMacroEditorOverlay.MC.getWindow() == null ? 640 : cA.init_I();
         int height = AutismMacroEditorOverlay.MC.getWindow() == null ? 360 : cA.load_I();
         return new OverlayBounds(0, 0, width, height, this.isVisible(), false);
      }

      @Override
      public int getMinWidth() {
         return 1;
      }

      @Override
      public int getMinHeight() {
         return 1;
      }
   }

   private static final class BlockGroupLayout {
      private static final AutismMacroEditorOverlay.BlockGroupLayout NONE = new AutismMacroEditorOverlay.BlockGroupLayout(
         false, false, false, -1, 0, 0, ""
      );
      private final boolean header;
      private final boolean report;
      private final boolean gate;
      private final boolean multi;
      private final int headerIndex;
      private final int childCount;
      private final int offsetFromHeader;
      private final String roleLabel;

      private BlockGroupLayout(boolean header, boolean report, boolean gate, int headerIndex, int childCount, int offsetFromHeader, String roleLabel) {
         this(header, report, gate, false, headerIndex, childCount, offsetFromHeader, roleLabel);
      }

      private BlockGroupLayout(
         boolean header, boolean report, boolean gate, boolean multi, int headerIndex, int childCount, int offsetFromHeader, String roleLabel
      ) {
         this.header = header;
         this.report = report;
         this.gate = gate;
         this.multi = multi;
         this.headerIndex = headerIndex;
         this.childCount = Math.max(0, childCount);
         this.offsetFromHeader = Math.max(0, offsetFromHeader);
         this.roleLabel = roleLabel == null ? "" : roleLabel;
      }

      private boolean hasHeader() {
         return this.headerIndex >= 0;
      }

      private boolean isHeaderRow() {
         return this.offsetFromHeader == 0;
      }

      private boolean isLastRow() {
         return this.offsetFromHeader >= this.childCount;
      }
   }

   private static final class PaletteCategoryLayout {
      private final AutismMacroEditorOverlay.PaletteCategory category;
      private final int x;
      private final int headerY;
      private final int width;
      private final List<AutismMacroEditorOverlay.PaletteButtonBounds> buttons;
      private final int bottomY;

      private PaletteCategoryLayout(
         AutismMacroEditorOverlay.PaletteCategory category,
         int x,
         int headerY,
         int width,
         List<AutismMacroEditorOverlay.PaletteButtonBounds> buttons,
         int bottomY
      ) {
         this.category = category;
         this.x = x;
         this.headerY = headerY;
         this.width = width;
         this.buttons = buttons;
         this.bottomY = bottomY;
      }
   }

   private static final class PaletteCategory {
      private final String id;
      private final String label;
      private final int color;

      private PaletteCategory(String id, String label, int color) {
         this.id = id;
         this.label = label;
         this.color = color;
      }
   }

   private static final class IndexRange {
      private static final AutismMacroEditorOverlay.IndexRange EMPTY = new AutismMacroEditorOverlay.IndexRange(-1, -1);
      private final int start;
      private final int end;

      private IndexRange(int start, int endExclusive) {
         this.start = start;
         this.end = endExclusive;
      }

      private boolean isEmpty() {
         return this.start < 0 || this.end <= this.start;
      }
   }

   private static final class RowTransitionAnim {
      private final int stableId;
      private final String label;
      private final boolean conditional;
      private final int fromRowIndex;
      private final int toRowIndex;
      private final AutismMacroEditorOverlay.RowAnimKind kind;
      private float progress;

      private RowTransitionAnim(int stableId, String label, boolean conditional, int fromRowIndex, int toRowIndex, AutismMacroEditorOverlay.RowAnimKind kind) {
         this.stableId = stableId;
         this.label = label;
         this.conditional = conditional;
         this.fromRowIndex = fromRowIndex;
         this.toRowIndex = toRowIndex;
         this.kind = kind;
      }
   }
}
