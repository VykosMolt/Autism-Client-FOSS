package autismclient.util;

import autismclient.compat263.CompatGLFW;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class AutismMacroListOverlay extends DraggableOverlayWindow {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final long init_J = 2000L;
   private static final int flush_I = 1;
   private static final int close_I = 60;
   private static final int open_I = 150;
   private static final int poll_I = 10;
   private static final float init_F_2 = 3.0F;
   private static final int push_I = 18;
   private static final int pop_I = 12;
   private final Font font;
   private final AutismMacroEditorOverlay editor;
   private final autismclient.gui.f.b.CharEncoder styleTheme = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.layout.CollapsibleWindowNode windowNode = new autismclient.gui.ui.layout.CollapsibleWindowNode("Macro Library");
   private final autismclient.gui.ui.layout.LayoutTreeHost layoutHost = new autismclient.gui.ui.layout.LayoutTreeHost(this.styleTheme, this.windowNode);
   private final autismclient.gui.ui.widgets.TextInputWidget searchInput = new autismclient.gui.ui.widgets.TextInputWidget();
   private final autismclient.gui.ui.widgets.TextInputWidget pasteNameInput = new autismclient.gui.ui.widgets.TextInputWidget();
   private final autismclient.gui.ui.widgets.TextInputWidget folderNameInput = new autismclient.gui.ui.widgets.TextInputWidget();
   private boolean folderDialogOpen;
   private String renamingFolder;
   private autismclient.gui.ui.widgets.DropdownMenu openDropdown;
   private final OverlayContextMenu<AutismMacroListOverlay.ListItem> contextMenu;
   private autismclient.gui.ui.layout.ButtonNode moveButton;
   private autismclient.gui.ui.layout.ButtonNode copyButton;
   private final Set<String> init_Set = new HashSet<>();
   private long lastRemoteRevision = -9223372036854775808L;
   private final autismclient.gui.ui.layout.VerticalSpacerNode listSpacer = new autismclient.gui.ui.layout.VerticalSpacerNode();
   private int overlayX = 500;
   private int overlayY = 250;
   private int overlayWidth = 240;
   private int measuredWidth = 240;
   private int overlayHeight = 220;
   private int chromeHeight;
   private boolean visible = false;
   private boolean collapsed = false;
   private boolean draggingWindow = false;
   private float dragOffsetX;
   private float dragOffsetY;
   private final autismclient.gui.ui.widgets.ScrollOffsetController scroller = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private boolean pasteDialogOpen = false;
   private boolean scrollbarDragging = false;
   private int thumbGrabOffset = 0;
   private final LinkedHashSet<String> selectedKeys = new LinkedHashSet<>();
   private String selectionAnchorKey;
   private String pendingDeleteSignature;
   private long pendingDeleteDeadline;
   private int contextMenuX;
   private int contextMenuY;
   private long write_J;
   private float write_F;
   private final autismclient.gui.ui.layout.LabelNode statusLabel = new autismclient.gui.ui.layout.LabelNode("", autismclient.gui.f.b.l.write);
   private String pressedFolder;
   private double folderPressX;
   private double folderPressY;
   private float folderGrabOffsetY;
   private boolean draggingFolder;
   private boolean push_Z;
   private boolean pop_Z;
   private String update_Str;
   private String sync_Str;
   private int folderDropLineY = -2147483648;
   private String pressedMacroKey;
   private boolean pressedMacroWasSelected;
   private double macroPressX;
   private double macroPressY;
   private float macroGrabOffsetY;
   private boolean draggingMacros;
   private List<AutismMacro> draggedMacros = List.of();
   private final Set<String> draggedKeys = new HashSet<>();
   private int pressedDragIndex;
   private boolean get_Z;
   private boolean set_Z;
   private boolean put_Z;
   private String dropTargetFolder = "";
   private String dropInsertBefore;
   private List<String> dropFoldersAbove = List.of();
   private int macroDropLineY = -2147483648;
   private int layoutMacroCount = -1;
   private long lastMacroRevision = -9223372036854775808L;
   private int lastMacroCount = -2147483648;
   private String title = "";
   private boolean add_Z = false;
   private boolean layoutDirty = true;
   private boolean configurationOnly;
   private final List<AutismMacroListOverlay.HitRegion> hitRegions = new ArrayList<>();
   private final List<AutismMacroListOverlay.RowButton> rowButtons = new ArrayList<>(4);
   private List<AutismMacroListOverlay.ListItem> items = List.of();
   private long cachedCountRevision = -9223372036854775808L;
   private int cachedMacroCount;

   public AutismMacroListOverlay(Font textRenderer) {
      this(textRenderer, null);
   }

   public AutismMacroListOverlay(Font textRenderer, AutismMacroEditorOverlay activeEditor) {
      this.font = textRenderer;
      this.editor = activeEditor;
      this.contextMenu = new OverlayContextMenu<>(this.styleTheme, textRenderer, ignored -> this.contextMenuItems(), 12);
      this.setActiveContextMenu(this.contextMenu);
      this.flush_V_10();
   }

   private void flush_V_10() {
      this.overlayWidth = this.run_I_13();
      this.windowNode.setCenterTitle(false);
      this.windowNode.setTitleTone(autismclient.gui.f.b.l.load);
      this.windowNode.setHeaderButtons(true, true);
      this.windowNode.setTitleInsets(this.add_I_11() + 2, this.add_I_11() + this.next_I_4() + this.prev_I_3() + this.valid_I_2() + 12);
      this.windowNode.body().withGap(this.clear_I_5()).withPadding(autismclient.gui.ui.layout.EdgeInsets.uniform(this.add_I_11()));
      this.searchInput.setPlaceholder("Search macros...").setFixedHeight(this.reset_I_4()).withGrowX(true).setOnChange(text -> {
         this.scroller.jumpTo(0, 0);
         this.layoutDirty = true;
      });
      this.pasteNameInput
         .setPlaceholder("New macro name...")
         .setFixedHeight(this.reset_I_4())
         .withGrowX(true)
         .setOnSubmit(text -> this.confirmPaste());
      this.folderNameInput
         .setPlaceholder("Folder name...")
         .setMaxLength(64)
         .setFixedHeight(this.reset_I_4())
         .withGrowX(true)
         .setOnSubmit(text -> this.confirmFolderDialog());
      this.rebuildLayout();
   }

   private void refreshItems() {
      LanSyncManager sync = LanSyncManager.getInstance();
      this.items = this.buildItems(MacroLibraryManager.getInstance().getMacros(), sync.getPeerMacros(), sync);
      if (!this.selectedKeys.isEmpty()) {
         Set<String> visibleKeys = new HashSet<>();

         for (AutismMacroListOverlay.ListItem item : this.items) {
            if (item.kind == AutismMacroListOverlay.ItemKind.LOCAL_MACRO && item.macro != null) {
               visibleKeys.add(MacroNameNormalizer.normalize(item.macro.name));
            }
         }

         this.selectedKeys.retainAll(visibleKeys);
      }
   }

   private void rebuildLayout() {
      this.windowNode.body().clearChildren();
      this.moveButton = null;
      this.copyButton = null;
      this.refreshItems();
      this.measuredWidth = this.measurePreferredWidth();
      this.overlayWidth = this.run_I_13();
      this.windowNode.setTitle("Macro Library [" + this.macroCount() + "]");
      this.windowNode.body().addChild(this.searchInput);
      if (this.items.isEmpty()) {
         this.windowNode
            .body()
            .addChild(
               new autismclient.gui.ui.layout.LabelNode("No macros match the current filter.", autismclient.gui.f.b.l.write).setTrimToBounds(true)
            );
      } else {
         this.listSpacer.withHeight(this.listHeightFor(this.items.size()));
         this.windowNode.body().addChild(this.listSpacer);
      }

      if (this.folderDialogOpen) {
         this.windowNode.body().addChild(this.folderNameInput);
         String confirmLabel = this.renamingFolder == null ? "Create Folder" : "Rename";
         boolean grow = !this.buttonsFit(confirmLabel, "Cancel");
         autismclient.gui.ui.layout.StretchRowLayout actions = new autismclient.gui.ui.layout.StretchRowLayout().withGap(this.start_I_10());
         actions.addChild(
            this.newButton(confirmLabel, autismclient.gui.ui.layout.ButtonNode.Variant.SUCCESS, this::confirmFolderDialog, grow)
         );
         actions.addChild(this.newButton("Cancel", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::closeFolderDialog, grow));
         this.windowNode.body().addChild(actions);
      } else if (this.pasteDialogOpen) {
         this.windowNode.body().addChild(this.pasteNameInput);
         boolean grow = !this.buttonsFit("Create", "Cancel");
         autismclient.gui.ui.layout.StretchRowLayout actions = new autismclient.gui.ui.layout.StretchRowLayout().withGap(this.start_I_10());
         actions.addChild(this.newButton("Create", autismclient.gui.ui.layout.ButtonNode.Variant.SUCCESS, this::confirmPaste, grow));
         actions.addChild(this.newButton("Cancel", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::closePasteDialog, grow));
         this.windowNode.body().addChild(actions);
         this.windowNode
            .body()
            .addChild(
               new autismclient.gui.ui.layout.LabelNode("Name the new macro made from the copied steps.", autismclient.gui.f.b.l.write).setTrimToBounds(true)
            );
      } else {
         boolean grow = !this.buttonsFit("Create New", "Paste", "Folder", "Move", "Copy");
         autismclient.gui.ui.layout.StretchRowLayout actions = new autismclient.gui.ui.layout.StretchRowLayout().withGap(this.start_I_10());
         actions.addChild(
            this.newButton("Create New", autismclient.gui.ui.layout.ButtonNode.Variant.SUCCESS, this::openNewMacro, grow)
         );
         actions.addChild(this.newButton("Paste", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::beginPaste, grow));
         actions.addChild(
            this.newButton("Folder", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> this.openFolderDialog(null), grow)
         );
         this.moveButton = this.newButton("Move", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::moveSelectedToFolder, grow);
         this.copyButton = this.newButton("Copy", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::copySelected, grow);
         actions.addChild(this.moveButton);
         actions.addChild(this.copyButton);
         this.windowNode.body().addChild(actions);
         if (!this.items.isEmpty()) {
            this.windowNode.body().addChild(this.statusLabel.setTrimToBounds(true));
         }

         this.refreshSelectionButtons();
      }
   }

   private autismclient.gui.ui.layout.ButtonNode newButton(
      String label, autismclient.gui.ui.layout.ButtonNode.Variant variant, Runnable onPress, boolean grow
   ) {
      autismclient.gui.ui.layout.ButtonNode button = new autismclient.gui.ui.layout.ButtonNode(label, variant, onPress).setFixedHeight(this.stop_I_4());
      button.setHorizontalPadding(5);
      if (grow) {
         button.withGrowX(true);
      } else {
         button.setMinWidth(24.0F);
      }

      return button;
   }

   private int buttonsWidth(String... labels) {
      int required = this.start_I_10() * (labels.length - 1);

      for (String label : labels) {
         required += this.buttonWidth(label);
      }

      return required;
   }

   private boolean buttonsFit(String... labels) {
      return this.font == null ? false : this.buttonsWidth(labels) <= this.overlayWidth - this.add_I_11() * 2;
   }

   private int buttonWidth(String label) {
      return this.font == null
         ? 24
         : autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
            autismclient.gui.f.b.k.init(
               this.font,
               label,
               this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
               this.styleTheme.load(autismclient.gui.f.b.l.read)
            )
         );
   }

   private int listHeightFor(int itemCount) {
      int visibleRows = Math.max(3, Math.min(this.maxVisibleRows(), itemCount));
      return visibleRows * this.rowPitch() + 2;
   }

   private int measurePreferredWidth() {
      if (this.font == null) {
         return 240;
      }

      Identifier font = this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read);
      int color = this.styleTheme.load(autismclient.gui.f.b.l.read);
      int nameWidth = 0;
      int stepsWidth = autismclient.gui.f.b.k.init(this.font, stepCountLabel(0), font, color);

      for (AutismMacro macro : MacroLibraryManager.getInstance().getMacros()) {
         if (!AutoFishMacroPresets.isStopMacro(macro)) {
            nameWidth = Math.max(nameWidth, autismclient.gui.f.b.k.init(this.font, macro.name, font, color));
            stepsWidth = Math.max(stepsWidth, autismclient.gui.f.b.k.init(this.font, stepCountLabel(macro.actions.size()), font, color));
         }
      }

      nameWidth = Math.max(60, Math.min(150, nameWidth));
      int icon = this.size_I_2();
      int buttons = (this.configurationOnly ? 0 : icon + this.iconGap())
         + icon
         + this.iconGap()
         + this.buttonWidth("Move")
         + this.iconGap()
         + this.buttonWidth("COPY")
         + this.iconGap()
         + icon;
      int row = this.rowPadding() + 10 + this.entry() + this.nameGap() + nameWidth + 8 + stepsWidth + 2 + this.iconGap() + buttons + this.rowPadding();
      int list = row + 2 + 6;
      int actions = this.buttonsWidth("Create New", "Paste", "Folder", "Move", "Copy");
      int title = autismclient.gui.f.b.k.init(
            this.font,
            "Macro Library [" + this.macroCount() + "]",
            this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load),
            this.styleTheme.load(autismclient.gui.f.b.l.load)
         )
         + this.add_I_11() * 2
         + 2
         + this.next_I_4()
         + this.prev_I_3()
         + this.valid_I_2()
         + 12;
      return Math.max(title, Math.max(list, actions) + this.add_I_11() * 2);
   }

   public AutismMacroEditorOverlay getEditor() {
      return this.editor;
   }

   public void setConfigurationOnly(boolean configurationOnly) {
      if (this.configurationOnly != configurationOnly) {
         this.configurationOnly = configurationOnly;
         this.layoutDirty = true;
      }
   }

   public void saveState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      shared.setMacroListOverlayVisible(this.visible);
      shared.setMacroListOverlayX(this.overlayX);
      shared.setMacroListOverlayY(this.overlayY);
      shared.setMacroListScrollOffset(this.scroller.getOffset());
      shared.setMacroListSearchQuery(this.searchInput.getValue());
      this.saveBounds();
   }

   public void restoreState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      this.loadBounds();
      this.visible = shared.isMacroListOverlayVisible();
      this.overlayX = shared.getMacroListOverlayX();
      this.overlayY = shared.getMacroListOverlayY();
      this.scroller.setOffsetUnclamped(shared.getMacroListScrollOffset());
      this.searchInput.setValue(shared.getMacroListSearchQuery());
      this.layoutDirty = true;
      this.windowNode.setExpandedImmediate(!this.collapsed);
   }

   @Override
   public String getOverlayId() {
      return "autism-macrolist";
   }

   @Override
   public int getMinWidth() {
      return this.run_I_13();
   }

   @Override
   public int getMinHeight() {
      return this.styleTheme.load() + this.reset_I_4() + this.stop_I_4() + this.add_I_11() * 2 + this.clear_I_5();
   }

   @Override
   public boolean isResizable() {
      return true;
   }

   @Override
   protected int getResizeMinHeight() {
      return this.chromeHeight > 0 ? this.chromeHeight + this.listHeightFor(3) : this.getMinHeight();
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.overlayX, this.overlayY, this.overlayWidth, this.overlayHeight, this.visible, this.collapsed);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampBounds(
            new OverlayBounds(bounds.x, bounds.y, Math.max(this.run_I_13(), bounds.width), bounds.height, bounds.visible, bounds.collapsed)
         );
         this.overlayX = clamped.x;
         this.overlayY = clamped.y;
         this.overlayWidth = clamped.width;
         this.overlayHeight = clamped.height;
         this.visible = clamped.visible;
         this.collapsed = clamped.collapsed;
      }
   }

   @Override
   public void setVisible(boolean visible) {
      if (visible && this.editor != null && this.editor.isVisible()) {
         this.visible = false;
         this.saveState();
         OverlayManager.getInstance().bringToFront(this.editor);
      } else {
         this.visible = visible;
         if (!visible) {
            this.layoutHost.clearFocus();
            this.draggingWindow = false;
            this.openDropdown = null;
            this.contextMenu.close();
            this.resetMacroDrag();
            this.resetFolderDrag();
         } else {
            this.layoutDirty = true;
            this.windowNode.setExpandedImmediate(!this.collapsed);
            OverlayManager.getInstance().bringToFront(this);
         }

         this.saveState();
      }
   }

   public void toggleVisible() {
      this.setVisible(!this.visible);
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public boolean isCollapsed() {
      return this.collapsed;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      this.collapsed = collapsed;
      this.draggingWindow = false;
      if (collapsed) {
         this.layoutHost.clearFocus();
         this.openDropdown = null;
         this.contextMenu.close();
         this.resetMacroDrag();
         this.resetFolderDrag();
      }

      this.saveState();
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.layoutHost.hasFocusedInput();
   }

   @Override
   public void clearInputFocus() {
      this.layoutHost.clearFocus();
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      } else {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         float uiX = viewport.toUiX(mouseX);
         float uiY = viewport.toUiY(mouseY);
         if (this.contextMenu.handleClick(uiX, uiY)) {
            return true;
         } else {
            return this.openDropdown != null && this.openDropdown.isOpen() && this.openDropdown.containsPopupPoint(Math.round(uiX), Math.round(uiY))
               ? true
               : uiX >= this.overlayX && uiX <= this.overlayX + this.overlayWidth && uiY >= this.overlayY && uiY <= this.overlayY + this.overlayHeight;
         }
      }
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiX = viewport.toUiX(mouseX);
      float uiY = viewport.toUiY(mouseY);
      return this.isOverTitleBar(uiX, uiY) && !this.load_Z_F_F_3(uiX, uiY);
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if ((this.visible || this.isClosing()) && MC != null && MC.font != null) {
         long remoteRevision = LanSyncManager.getInstance().getPeerMacrosRevision();
         if (remoteRevision != this.lastRemoteRevision) {
            this.lastRemoteRevision = remoteRevision;
            this.layoutDirty = true;
         }

         long currentMacroRevision = MacroLibraryManager.getInstance().getRevision();
         int currentMacroCount = this.macroCount();
         if (currentMacroRevision != this.lastMacroRevision) {
            this.layoutDirty = true;
         }

         if (!this.layoutDirty && currentMacroCount == this.layoutMacroCount && this.pasteDialogOpen == this.add_Z) {
            if (currentMacroCount != this.lastMacroCount) {
               this.lastMacroCount = currentMacroCount;
               this.title = "Macro Library [" + currentMacroCount + "]";
            }

            this.windowNode.setTitle(this.title);
         } else {
            this.rebuildLayout();
            this.layoutMacroCount = currentMacroCount;
            this.lastMacroRevision = currentMacroRevision;
            this.add_Z = this.pasteDialogOpen;
            this.layoutDirty = false;
         }

         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         boolean active = OverlayManager.getInstance().isFocused(this) || OverlayManager.getInstance().isTopmost(this);
         boolean headerHovered = this.isOverTitleBar(uiMouseX, uiMouseY);
         autismclient.gui.ui.layout.LayoutContext metrics = new autismclient.gui.ui.layout.LayoutContext(
            context, MC.font, viewport, this.styleTheme, uiMouseX, uiMouseY, delta
         );
         this.windowNode.setExpanded(!this.collapsed);
         this.windowNode.setActive(active);
         this.windowNode.setHeaderHovered(headerHovered);
         this.overlayWidth = this.resolveWindowWidth(this.run_I_13(), this.run_I_13());
         int autoList = this.listHeightFor(this.items.size());
         if (!this.items.isEmpty()) {
            this.listSpacer.withHeight(autoList);
         }

         this.overlayHeight = Math.round(this.windowNode.measureHeight(metrics, this.overlayWidth));
         if (!this.items.isEmpty() && !this.collapsed) {
            this.chromeHeight = this.overlayHeight - autoList;
            if (this.hasCustomStoredSize()) {
               int want = this.resolveWindowHeight(this.overlayHeight, this.getResizeMinHeight());
               this.listSpacer.withHeight(Math.max(this.listHeightFor(3), want - this.chromeHeight));
               this.overlayHeight = Math.round(this.windowNode.measureHeight(metrics, this.overlayWidth));
            }
         }

         this.overlayHeight = Math.max(this.styleTheme.load(), this.overlayHeight);
         OverlayBounds clamped = this.clampBounds(new OverlayBounds(this.overlayX, this.overlayY, this.overlayWidth, this.overlayHeight, this.visible, this.collapsed));
         this.overlayX = clamped.x;
         this.overlayY = clamped.y;
         this.overlayWidth = clamped.width;
         this.overlayHeight = clamped.height;
         this.windowNode.setBounds(this.overlayX, this.overlayY, this.overlayWidth, this.overlayHeight);
         this.statusLabel
            .text(
               this.selectedKeys.isEmpty()
                  ? "Click to select, drag to move. Right-click for more."
                  : this.selectedKeys.size() + " selected. Drag to move, right-click for more."
            );
         this.layoutHost.render(context, mouseX, mouseY, delta);
         if (!this.items.isEmpty() && this.windowNode.pushBodyClip(context)) {
            try {
               this.renderList(context, viewport, uiMouseX, uiMouseY, delta, active);
            } finally {
               autismclient.gui.ui.UiClipStack.getInstance().pop(context);
            }
         } else {
            this.hitRegions.clear();
         }

         if (this.openDropdown != null && !this.collapsed) {
            this.openDropdown.render(autismclient.gui.f.UiHelper.init(context, this.font, Math.round(uiMouseX), Math.round(uiMouseY)));
         }

         if (this.contextMenu.isOpen() && !this.collapsed) {
            context.nextStratum();
            this.contextMenu.render(context, Math.round(uiMouseX), Math.round(uiMouseY));
         }
      }
   }

   private void renderList(
      GuiGraphicsExtractor context, autismclient.gui.ui.layout.LayoutViewport viewport, float uiMouseX, float uiMouseY, float delta, boolean active
   ) {
      this.hitRegions.clear();
      int viewX = Math.round(this.listSpacer.x());
      int viewY = Math.round(this.listSpacer.y());
      int viewW = Math.round(this.listSpacer.width());
      int viewH = Math.round(this.listSpacer.height());
      if (viewW > 2 && viewH > 2) {
         if (this.draggingMacros || this.draggingFolder) {
            this.autoScrollDrag(uiMouseY, viewX, viewY, viewW, viewH);
         }

         autismclient.gui.ui.widgets.ScrollListLayout.ListLayout baseLayout = this.listLayout(viewX, viewY, viewW, viewH, 0);
         int drawScroll = this.scroller.tickAndClamp(delta, baseLayout.maxScroll());
         autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.listLayout(viewX, viewY, viewW, viewH, drawScroll);
         if (this.draggingMacros) {
            this.updateMacroDropTarget(listLayout, uiMouseX, uiMouseY);
         }

         if (this.draggingFolder) {
            this.updateFolderDropTarget(listLayout, uiMouseX, uiMouseY);
         }

         float alpha = active ? 1.0F : 0.56F;
         autismclient.gui.f.PacketUtil.init(
            context,
            autismclient.gui.f.NetHelper.init(viewX, viewY, viewW, viewH),
            autismclient.gui.ui.layout.LayoutContext.scaleAlpha(this.styleTheme.reset(), alpha),
            autismclient.gui.ui.layout.LayoutContext.scaleAlpha(this.styleTheme.set(), alpha)
         );
         listLayout.pushContentClip(context);

         try {
            autismclient.gui.ui.layout.LayoutContext rowContext = new autismclient.gui.ui.layout.LayoutContext(
               context, this.font, viewport, this.styleTheme, uiMouseX, uiMouseY, delta, alpha
            );
            listLayout.forEachVisibleRow(
               this.items.size(),
               (i, rowY) -> {
                  AutismMacroListOverlay.ListItem item = this.items.get(i);
                  if (item.kind == AutismMacroListOverlay.ItemKind.FOLDER) {
                     this.renderFolderRow(context, rowContext, item, viewX + 1, rowY, listLayout.contentWidth());
                  } else if (item.kind == AutismMacroListOverlay.ItemKind.SECTION_HEADER) {
                     this.init_V_GuiGraphicsExtract_6(context, rowContext, item, viewX + 1, rowY, listLayout.contentWidth());
                  } else {
                     this.renderMacroRow(context, rowContext, item, viewX + 1, rowY, listLayout.contentWidth());
                     if (item.kind == AutismMacroListOverlay.ItemKind.LOCAL_MACRO
                        && item.macro != null
                        && item.macro.folder != null
                        && !item.macro.folder.isEmpty()) {
                        boolean continues = i + 1 < this.items.size()
                           && this.items.get(i + 1).kind == AutismMacroListOverlay.ItemKind.LOCAL_MACRO
                           && this.items.get(i + 1).macro != null
                           && MacroFolderLayout.key(this.items.get(i + 1).macro.folder).equals(MacroFolderLayout.key(item.macro.folder));
                        int railTop = rowY
                           - (
                              i > 0 && this.items.get(i - 1).kind == AutismMacroListOverlay.ItemKind.FOLDER
                                 ? this.rowPitch() - this.rowHeight()
                                 : 0
                           );
                        int railBottom = rowY + (continues ? this.rowPitch() : this.rowHeight());
                        autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(
                           context,
                           viewX + 1 + this.rowPadding() + 2,
                           railTop,
                           2,
                           railBottom - railTop,
                           autismclient.gui.ui.layout.LayoutContext.scaleAlpha(this.styleTheme.run() & 16777215 | -1728053248, alpha)
                        );
                     }
                  }
               }
            );
            if (this.draggingMacros) {
               if (this.get_Z && !this.put_Z && !this.set_Z && this.macroDropLineY != -2147483648) {
                  autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(
                     context, viewX + 1 + 2, this.macroDropLineY - 1, listLayout.contentWidth() - 3, 2, autismclient.gui.ui.layout.LayoutContext.scaleAlpha(this.styleTheme.run(), alpha)
                  );
               }

               int ghostTop = Math.round(uiMouseY - this.macroGrabOffsetY);

               for (int j = 0; j < this.draggedMacros.size(); j++) {
                  this.renderDraggedMacro(
                     context, this.draggedMacros.get(j), viewX + 1, ghostTop + (j - this.pressedDragIndex) * this.rowPitch(), listLayout.contentWidth(), alpha
                  );
               }
            }

            if (this.draggingFolder) {
               if (this.push_Z && !this.pop_Z && this.folderDropLineY != -2147483648) {
                  autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(
                     context, viewX + 1 + 2, this.folderDropLineY - 1, listLayout.contentWidth() - 3, 2, autismclient.gui.ui.layout.LayoutContext.scaleAlpha(this.styleTheme.run(), alpha)
                  );
               }

               this.renderDraggedFolder(context, viewX + 1, Math.round(uiMouseY - this.folderGrabOffsetY), listLayout.contentWidth());
            }
         } finally {
            listLayout.popContentClip(context);
         }

         listLayout.renderScrollbar(context, uiMouseX, uiMouseY, this.scrollbarDragging);
      }
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics listScrollbarMetrics(int viewX, int viewY, int viewW, int viewH) {
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout baseLayout = this.listLayout(viewX, viewY, viewW, viewH, 0);
      return this.listLayout(viewX, viewY, viewW, viewH, this.scroller.tickAndClamp(0.0F, baseLayout.maxScroll())).scrollbar();
   }

   private autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout(int viewX, int viewY, int viewW, int viewH, int scrollOffset) {
      return autismclient.gui.ui.widgets.ScrollListLayout.compute(
         viewX, viewY, viewW, viewH, this.items.size(), this.rowHeight(), this.rowPitch(), scrollOffset, 6, 6, 1, 7, true
      );
   }

   private void init_V_GuiGraphicsExtract_6(
      GuiGraphicsExtractor context, autismclient.gui.ui.layout.LayoutContext rowContext, AutismMacroListOverlay.ListItem item, int x, int y, int width
   ) {
      int textY = UiLayoutMath.centerTextY(y, this.rowHeight(), this.styleTheme.init_I_l(autismclient.gui.f.b.l.load), this.styleTheme.flush());
      autismclient.gui.ui.widgets.PanelSurfaceHelper.drawDividerColored(context, x + this.dividerInset(), y + this.rowHeight() - 2, width - this.dividerInset() * 2, rowContext.applyAlpha(1124073471));
      autismclient.gui.f.b.k.init(
         context,
         this.font,
         item.label,
         this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.load),
         rowContext.applyAlpha(item.init_I),
         x + this.dividerInset(),
         textY,
         false
      );
   }

   private void renderMacroRow(
      GuiGraphicsExtractor context, autismclient.gui.ui.layout.LayoutContext rowContext, AutismMacroListOverlay.ListItem item, int x, int y, int width
   ) {
      boolean localRow = item.kind == AutismMacroListOverlay.ItemKind.LOCAL_MACRO && item.macro != null;
      boolean draggedRow = localRow
         && (this.draggingMacros && this.draggedKeys.contains(MacroNameNormalizer.normalize(item.macro.name)) || this.draggingFolder && this.isPressedFolder(item.macro.folder));
      if (draggedRow) {
         autismclient.gui.ui.UiAlphaStack.push(0.35F);

         try {
            this.init_V_GuiGraphicsExtract_9(context, rowContext, item, x, y, width, localRow, true);
         } finally {
            autismclient.gui.ui.UiAlphaStack.pop();
         }
      } else {
         this.init_V_GuiGraphicsExtract_9(context, rowContext, item, x, y, width, localRow, false);
      }
   }

   private void init_V_GuiGraphicsExtract_9(
      GuiGraphicsExtractor context,
      autismclient.gui.ui.layout.LayoutContext rowContext,
      AutismMacroListOverlay.ListItem item,
      int x,
      int y,
      int width,
      boolean localRow,
      boolean draggedRow
   ) {
      boolean hovered = !this.draggingMacros && !this.draggingFolder && this.contains(x, y, width, this.rowHeight(), rowContext.mouseX(), rowContext.mouseY());
      boolean selectedRow = localRow && this.selectedKeys.contains(MacroNameNormalizer.normalize(item.macro.name));
      if (selectedRow && !draggedRow) {
         autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(
            context, x, y, width, this.rowHeight(), rowContext.applyAlpha(ThemePaletteManager.recolor(1715082778, ThemePaletteManager.ColorRole.ACCENT))
         );
         autismclient.gui.f.PacketUtil.load(
            context,
            autismclient.gui.f.NetHelper.init(x, y, width, this.rowHeight()),
            rowContext.applyAlpha(ThemePaletteManager.recolor(-42149, ThemePaletteManager.ColorRole.OUTLINE))
         );
      } else if (hovered && !draggedRow) {
         autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(
            context, x, y, width, this.rowHeight(), rowContext.applyAlpha(ThemePaletteManager.recolor(452938314, ThemePaletteManager.ColorRole.ACCENT))
         );
      }

      List<AutismMacroListOverlay.RowButton> buttons = this.rowButtonsFor(item);
      int buttonCursor = x + width - this.rowPadding();
      int buttonY = y + this.rowButtonInset();

      for (int i = buttons.size() - 1; i >= 0; i--) {
         AutismMacroListOverlay.RowButton button = buttons.get(i);
         buttonCursor -= button.width;
         this.renderRowButton(context, rowContext, buttonCursor, buttonY, button.width, button);
         this.hitRegions
            .add(new AutismMacroListOverlay.HitRegion(buttonCursor, buttonY, button.width, this.size_I_2(), item, button.action));
         buttonCursor -= this.iconGap();
      }

      int textLeft = x + this.rowPadding() + (item.macro != null && !item.macro.folder.isEmpty() ? 10 : 0);
      if (item.kind == AutismMacroListOverlay.ItemKind.LOCAL_MACRO) {
         boolean running = isMacroRunning(item.macro.name);
         int dotColor = running ? rowContext.applyAlpha(-13379224) : rowContext.applyAlpha(-10132123);
         int dotTop = y + UiLayoutMath.centerStart(0, this.rowHeight(), this.entry());
         autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(context, textLeft, dotTop, this.entry(), this.entry(), dotColor);
         textLeft += this.entry() + this.nameGap();
      }

      int rightLimit = Math.max(textLeft + 20, buttonCursor - 2);
      int labelWidth = Math.max(20, rightLimit - textLeft);
      String var10000;
      switch (item.kind) {
         case LOCAL_MACRO:
            var10000 = item.macro.name;
            break;
         case REMOTE_MACRO:
            var10000 = item.label;
            break;
         default:
            var10000 = item.label;
      }

      String primaryText = var10000;
      switch (item.kind) {
         case LOCAL_MACRO:
            var10000 = stepCountLabel(item.macro.actions.size());
            break;
         case REMOTE_MACRO:
            var10000 = item.source != null && !item.source.isBlank() ? item.source : "";
            break;
         default:
            var10000 = "";
      }

      String secondaryText = var10000;
      int var37;
      switch (item.kind) {
         case LOCAL_MACRO:
            var37 = rowContext.applyAlpha(this.styleTheme.load(autismclient.gui.f.b.l.read));
            break;
         case REMOTE_MACRO:
            var37 = rowContext.applyAlpha(-12122);
            break;
         default:
            var37 = rowContext.applyAlpha(this.styleTheme.load(autismclient.gui.f.b.l.read));
      }

      int primaryColor = var37;
      int secondaryColor = rowContext.applyAlpha(this.styleTheme.load(autismclient.gui.f.b.l.write));
      int textY = UiLayoutMath.centerTextY(y, this.rowHeight(), this.styleTheme.init_I_l(autismclient.gui.f.b.l.read), this.styleTheme.flush());
      if (item.kind == AutismMacroListOverlay.ItemKind.LOCAL_MACRO) {
         int secondaryWidth = autismclient.gui.f.b.k.init(
            this.font, secondaryText, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), secondaryColor
         );
         int secondaryX = Math.max(textLeft + 10, rightLimit - secondaryWidth);
         int primaryMaxWidth = Math.max(20, secondaryX - textLeft - 8);
         String trimmedPrimary = autismclient.gui.f.b.k.init(
            this.font, primaryText, primaryMaxWidth, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), primaryColor
         );
         autismclient.gui.f.b.k.init(
            context, this.font, trimmedPrimary, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), primaryColor, textLeft, textY, false
         );
         autismclient.gui.f.b.k.init(
            context,
            this.font,
            secondaryText,
            this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
            secondaryColor,
            secondaryX,
            textY,
            false
         );
      } else {
         String trimmedPrimary = autismclient.gui.f.b.k.init(
            this.font, primaryText, labelWidth, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), primaryColor
         );
         autismclient.gui.f.b.k.init(
            context, this.font, trimmedPrimary, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), primaryColor, textLeft, textY, false
         );
         if (!secondaryText.isEmpty()) {
            int secondaryX = textLeft
               + Math.min(
                  labelWidth - 10,
                  Math.max(
                     autismclient.gui.f.b.k.init(
                           this.font, trimmedPrimary, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), primaryColor
                        )
                        + 10,
                     labelWidth - 90
                  )
               );
            int secondaryWidth = Math.max(0, rightLimit - secondaryX);
            if (secondaryWidth > 18) {
               String trimmedSecondary = autismclient.gui.f.b.k.init(
                  this.font, secondaryText, secondaryWidth, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), secondaryColor
               );
               autismclient.gui.f.b.k.init(
                  context,
                  this.font,
                  trimmedSecondary,
                  this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
                  secondaryColor,
                  secondaryX,
                  textY,
                  false
               );
            }
         }
      }
   }

   private static String stepCountLabel(int count) {
      return count + (count == 1 ? " step" : " steps");
   }

   private void renderRowButton(
      GuiGraphicsExtractor context, autismclient.gui.ui.layout.LayoutContext rowContext, int x, int y, int width, AutismMacroListOverlay.RowButton button
   ) {
      if (button.action == AutismMacroListOverlay.RowAction.DELETE) {
         int size = this.size_I_2();
         autismclient.gui.ui.widgets.ListRowRenderer.drawDeleteButtonSquare(
            context, x + Math.max(0, (width - size) / 2), y, size, this.contains(x, y, width, this.size_I_2(), rowContext.mouseX(), rowContext.mouseY())
         );
      } else {
         int h = this.size_I_2();
         boolean hovered = this.contains(x, y, width, h, rowContext.mouseX(), rowContext.mouseY());
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(
            rowContext.graphics(), rowContext.font(), Math.round(rowContext.mouseX()), Math.round(rowContext.mouseY())
         );
         autismclient.gui.f.b.CachePool.init(
            ui,
            autismclient.gui.f.NetHelper.init(x, y, width, h),
            button.label,
            autismclient.gui.ui.layout.ButtonNode.toButtonTone(button.variant),
            hovered,
            false
         );
         if (button.icon != null) {
            int iconSize = 8;
            GuiIconAtlas.draw(context, button.icon, x + (width - iconSize) / 2, y + (h - iconSize) / 2, iconSize, -218103809);
         }
      }
   }

   private int clampedButtonWidth(String label, int maxWidth) {
      return Math.min(maxWidth, this.buttonWidth(label));
   }

   private List<AutismMacroListOverlay.RowButton> rowButtonsFor(AutismMacroListOverlay.ListItem item) {
      this.rowButtons.clear();
      switch (item.kind) {
         case LOCAL_MACRO:
            boolean running = isMacroRunning(item.macro.name);
            if (!this.configurationOnly) {
               this.rowButtons
                  .add(
                     new AutismMacroListOverlay.RowButton(
                        "",
                        running ? autismclient.gui.ui.layout.ButtonNode.Variant.DANGER : autismclient.gui.ui.layout.ButtonNode.Variant.SUCCESS,
                        this.data_I_2(),
                        AutismMacroListOverlay.RowAction.RUN_TOGGLE,
                        running ? GuiIconAtlas.STOP : GuiIconAtlas.PLAY
                     )
                  );
            }

            this.rowButtons
               .add(
                  new AutismMacroListOverlay.RowButton(
                     "", autismclient.gui.ui.layout.ButtonNode.Variant.DANGER, this.data_I_2(), AutismMacroListOverlay.RowAction.EDIT, GuiIconAtlas.EDIT
                  )
               );
            this.rowButtons
               .add(
                  new AutismMacroListOverlay.RowButton(
                     "Move",
                     autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY,
                     this.clampedButtonWidth("Move", 44),
                     AutismMacroListOverlay.RowAction.MOVE
                  )
               );
            this.rowButtons
               .add(
                  new AutismMacroListOverlay.RowButton(
                     "Copy",
                     autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY,
                     this.clampedButtonWidth("Copy", 44),
                     AutismMacroListOverlay.RowAction.COPY
                  )
               );
            this.rowButtons
               .add(
                  new AutismMacroListOverlay.RowButton(
                     "X", autismclient.gui.ui.layout.ButtonNode.Variant.DANGER, this.data_I_2(), AutismMacroListOverlay.RowAction.DELETE
                  )
               );
            break;
         case REMOTE_MACRO:
            this.rowButtons
               .add(
                  new AutismMacroListOverlay.RowButton(
                     "IMPORT",
                     autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY,
                     this.clampedButtonWidth("IMPORT", 58),
                     AutismMacroListOverlay.RowAction.IMPORT_REMOTE
                  )
               );
            break;
      }

      return this.rowButtons;
   }

   private void onRowButton(AutismMacroListOverlay.ListItem item, AutismMacroListOverlay.RowAction action) {
      if (item != null && action != null) {
         switch (action) {
            case TOGGLE_FOLDER:
               if (item.source == null) {
                  MacroLibraryManager.getInstance().toggleFolderCollapsed(item.label);
               } else {
                  String key = item.source + "\u0000" + MacroFolderLayout.key(item.label);
                  if (!this.init_Set.remove(key)) {
                     this.init_Set.add(key);
                  }
               }

               this.scrollbarDragging = false;
               this.layoutDirty = true;
               break;
            case RENAME_FOLDER:
               this.openFolderDialog(item.label);
               break;
            case REMOVE_FOLDER:
               MacroLibraryManager.getInstance().deleteFolder(item.label);
               bi.load("Folder removed; macros kept.");
               this.layoutDirty = true;
               break;
            case MOVE:
               this.openMoveMenu(item);
               break;
            case RUN_TOGGLE:
               if (this.configurationOnly) {
                  return;
               }

               if (item.macro == null) {
                  return;
               }

               if (isMacroRunning(item.macro.name)) {
                  MacroExecutor.stopMacroByName(item.macro.name);
               } else {
                  item.macro.execute();
               }
               break;
            case EDIT:
               if (item.macro == null) {
                  return;
               }

               this.setVisible(false);
               AutismMacroEditorOverlay.getInstance().openReturningToList(item.macro, true);
               break;
            case COPY:
               if (item.macro == null) {
                  return;
               }

               if (MacroClipboardCodec.copyMacros(List.of(item.macro), false)) {
                  bi.init("Copied macro: " + item.macro.name);
               } else {
                  bi.write("Failed to copy macro.");
               }
               break;
            case DELETE:
               if (item.macro != null) {
                  MacroLibraryManager.getInstance().removeMacro(item.macro);
               }
               break;
            case IMPORT_REMOTE:
               this.init_V_AutismMacroListOve(item);
               break;
         }
      }
   }

   private static boolean isMacroRunning(String name) {
      return MacroExecutor.isMacroNameRunning(name);
   }

   public void init_V_I_7(int count) {
      List<AutismMacro> picked = new ArrayList<>();
      AutismMacroListOverlay.ListItem first = null;

      for (AutismMacroListOverlay.ListItem item : this.items) {
         if (item.kind == AutismMacroListOverlay.ItemKind.LOCAL_MACRO && item.macro != null) {
            if (first == null) {
               first = item;
            }

            if (picked.size() < count) {
               picked.add(item.macro);
            }
         }
      }

      if (first != null) {
         this.selectMacros(picked);
         int menuX = Math.round(this.listSpacer.x()) + 40;
         int menuY = Math.round(this.listSpacer.y()) + this.rowPitch() * Math.max(1, picked.size());
         this.contextMenuX = menuX;
         this.contextMenuY = menuY;
         this.contextMenu.open(menuX, menuY, first);
      }
   }

   private void init_V_AutismMacroListOve(AutismMacroListOverlay.ListItem item) {
      if (item.source != null && !item.source.isBlank()) {
         LanSyncManager sync = LanSyncManager.getInstance();
         Map<String, Map<String, AutismMacro>> allRemote = sync.getPeerMacros();
         Map<String, AutismMacro> remoteMacros = allRemote.get(item.source);
         AutismMacro source = remoteMacros != null ? remoteMacros.get(MacroNameNormalizer.normalize(item.label)) : null;
         if (source == null) {
            AutismClientMessaging.sendPrefixed("§cRemote macro is no longer available: " + item.label);
         } else {
            AutismMacro imported = MacroLibraryManager.getInstance().importMacro(source, source.name);
            if (imported == null) {
               AutismClientMessaging.sendPrefixed("§cFailed to import macro: " + item.label);
            } else {
               AutismClientMessaging.sendPrefixed("§aImported macro as: " + imported.name);
               this.layoutDirty = true;
            }
         }
      }
   }

   private void openNewMacro() {
      this.setVisible(false);
      AutismMacroEditorOverlay.getInstance().openReturningToList(null, true);
   }

   private void beginPaste() {
      switch (MacroClipboardCodec.clipboardKind()) {
         case MACROS:
            this.pasteMacros();
            break;
         case STEPS:
            this.pasteDialogOpen = true;
            this.pasteNameInput.setValue("");
            this.scroller.jumpTo(0, 0);
            this.layoutDirty = true;
            this.pasteNameInput.setFocused(true);
            break;
         default:
            bi.read("Clipboard has no macros or steps.");
      }
   }

   private void closePasteDialog() {
      this.pasteDialogOpen = false;
      this.pasteNameInput.setValue("");
      this.pasteNameInput.setFocused(false);
      this.layoutDirty = true;
   }

   private boolean confirmPaste() {
      String name = this.pasteNameInput.getValue().trim();
      if (name.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cEnter a name for the pasted macro.");
         return true;
      } else if (MacroLibraryManager.getInstance().findByName(name) != null) {
         AutismClientMessaging.sendPrefixed("§cA macro with that name already exists.");
         return true;
      } else {
         AutismMacro pasted = MacroClipboardCodec.pasteMacro();
         if (pasted == null) {
            bi.write("Clipboard has no macro steps.");
            return true;
         } else {
            pasted.name = name;
            MacroLibraryManager.getInstance().saveMacro(pasted);
            bi.load("Created macro: " + pasted.name);
            this.searchInput.setValue("");
            this.selectMacros(List.of(pasted));
            this.closePasteDialog();
            return true;
         }
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.visible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (this.contextMenu.isOpen()) {
         if (button == 0) {
            if (this.contextMenu.init(uiMouseX, uiMouseY, button, (item, label, index) -> this.onContextMenuItem(item, index))) {
               return true;
            }
         } else if (button == 1 && !this.contextMenu.handleClick(uiMouseX, uiMouseY)) {
            this.contextMenu.close();
         } else if (button == 1) {
            return true;
         }
      }

      if (this.openDropdown != null && this.openDropdown.isOpen()) {
         this.openDropdown.onMouseClicked(Math.round(uiMouseX), Math.round(uiMouseY), button);
         return true;
      }

      if (button == 0 && this.load_Z_F_F_3(uiMouseX, uiMouseY)) {
         this.setVisible(false);
         return true;
      }

      if (button == 0 && this.isOverTitleBar(uiMouseX, uiMouseY)) {
         this.draggingWindow = true;
         this.dragOffsetX = uiMouseX - this.overlayX;
         this.dragOffsetY = uiMouseY - this.overlayY;
         return true;
      }

      if (!this.collapsed && this.layoutHost.mouseClicked(mouseX, mouseY, button)) {
         return true;
      }

      if (!this.collapsed
         && button == 0
         && this.contains(
            this.listSpacer.x(),
            this.listSpacer.y(),
            this.listSpacer.width(),
            this.listSpacer.height(),
            uiMouseX,
            uiMouseY
         )) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.listScrollbarMetrics(
            Math.round(this.listSpacer.x()),
            Math.round(this.listSpacer.y()),
            Math.round(this.listSpacer.width()),
            Math.round(this.listSpacer.height())
         );
         if (metrics.isScrollable() && metrics.containsTrack(uiMouseX, uiMouseY)) {
            this.scrollbarDragging = true;
            this.thumbGrabOffset = metrics.containsThumb(uiMouseX, uiMouseY) ? Math.round(uiMouseY) - metrics.thumbY() : metrics.thumbHeight() / 2;
            this.scroller.dragToMouseY(metrics, uiMouseY, this.thumbGrabOffset);
            return true;
         }
      }

      if (!this.collapsed
         && (button == 0 || button == 1)
         && this.contains(
            this.listSpacer.x(),
            this.listSpacer.y(),
            this.listSpacer.width(),
            this.listSpacer.height(),
            uiMouseX,
            uiMouseY
         )) {
         boolean overRegion = false;

         for (int i = this.hitRegions.size() - 1; i >= 0; i--) {
            if (this.hitRegions.get(i).contains(uiMouseX, uiMouseY)) {
               overRegion = true;
               break;
            }
         }

         if (!overRegion) {
            autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.currentListLayout();
            int rowIndex = this.rowIndexAt(uiMouseY, listLayout);
            AutismMacroListOverlay.ListItem rowItem = rowIndex >= 0 ? this.items.get(rowIndex) : null;
            if (rowItem != null && rowItem.kind == AutismMacroListOverlay.ItemKind.LOCAL_MACRO && rowItem.macro != null) {
               if (button == 1) {
                  this.openContextMenu(rowItem, Math.round(uiMouseX), Math.round(uiMouseY));
                  return true;
               }

               if (this.isShiftDown()) {
                  this.selectRangeTo(rowItem.macro);
               } else if (this.isCtrlDown()) {
                  this.toggleSelected(rowItem.macro);
               } else {
                  this.beginMacroPress(rowItem, uiMouseX, uiMouseY, listLayout, rowIndex);
               }

               return true;
            }

            if (button == 0) {
               if (rowIndex < 0) {
                  this.clearSelection();
               }

               return true;
            }
         }
      }

      if (!this.collapsed
         && button == 0
         && this.contains(
            this.listSpacer.x(),
            this.listSpacer.y(),
            this.listSpacer.width(),
            this.listSpacer.height(),
            uiMouseX,
            uiMouseY
         )) {
         for (int i = this.hitRegions.size() - 1; i >= 0; i--) {
            AutismMacroListOverlay.HitRegion region = this.hitRegions.get(i);
            if (region.contains(uiMouseX, uiMouseY)) {
               if (region.action == AutismMacroListOverlay.RowAction.TOGGLE_FOLDER && region.item.source == null) {
                  this.pressedFolder = region.item.label;
                  this.folderPressX = uiMouseX;
                  this.folderPressY = uiMouseY;
                  this.folderGrabOffsetY = uiMouseY - region.y;
                  return true;
               }

               this.onRowButton(region.item, region.action);
               return true;
            }
         }
      }

      return this.containsPoint(mouseX, mouseY);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (this.openDropdown != null && this.openDropdown.isOpen()) {
         this.openDropdown.onMouseReleased(Math.round(uiMouseX), Math.round(uiMouseY), button);
         return true;
      }

      if (button == 0 && this.draggingWindow) {
         this.draggingWindow = false;
         this.saveState();
         return true;
      }

      if (button == 0 && this.scrollbarDragging) {
         this.scrollbarDragging = false;
         this.saveState();
         return true;
      }

      if (button == 0 && this.pressedFolder != null) {
         this.finishFolderDrag();
         return true;
      }

      if (button != 0 || !this.draggingMacros && this.pressedMacroKey == null) {
         return !this.collapsed && this.layoutHost.mouseReleased(mouseX, mouseY, button) ? true : this.containsPoint(mouseX, mouseY);
      }

      this.finishMacroDrag();
      return true;
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (!this.visible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (this.openDropdown != null && this.openDropdown.isOpen()) {
         this.openDropdown.onMouseDragged(Math.round(uiMouseX), Math.round(uiMouseY), button, deltaX, deltaY);
         return true;
      }

      if (this.draggingWindow && button == 0) {
         int nextX = Math.round(uiMouseX - this.dragOffsetX);
         int nextY = Math.round(uiMouseY - this.dragOffsetY);
         OverlayBounds clamped = this.clampBounds(new OverlayBounds(nextX, nextY, this.overlayWidth, this.overlayHeight, this.visible, this.collapsed));
         this.overlayX = clamped.x;
         this.overlayY = clamped.y;
         return true;
      }

      if (this.scrollbarDragging && button == 0) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.listScrollbarMetrics(
            Math.round(this.listSpacer.x()),
            Math.round(this.listSpacer.y()),
            Math.round(this.listSpacer.width()),
            Math.round(this.listSpacer.height())
         );
         this.scroller.dragToMouseY(metrics, uiMouseY, this.thumbGrabOffset);
         return true;
      }

      if (button == 0 && this.pressedFolder != null) {
         if (!this.draggingFolder && (Math.abs(uiMouseX - this.folderPressX) >= 3.0 || Math.abs(uiMouseY - this.folderPressY) >= 3.0)) {
            this.draggingFolder = true;
            this.write_J = 0L;
            this.write_F = 0.0F;
         }

         return true;
      } else if (button == 0 && this.pressedMacroKey != null) {
         this.updateMacroDrag(uiMouseX, uiMouseY);
         return true;
      } else {
         return !this.collapsed && this.layoutHost.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) ? true : this.containsPoint(mouseX, mouseY);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.visible && !this.collapsed) {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         if (this.openDropdown != null && this.openDropdown.isOpen()) {
            this.openDropdown.onMouseScrolled(Math.round(uiMouseX), Math.round(uiMouseY), amount);
            return true;
         } else if (this.layoutHost.mouseScrolled(mouseX, mouseY, amount)) {
            return true;
         } else if (this.contains(
            this.listSpacer.x(),
            this.listSpacer.y(),
            this.listSpacer.width(),
            this.listSpacer.height(),
            uiMouseX,
            uiMouseY
         )) {
            autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.listLayout(
               Math.round(this.listSpacer.x()),
               Math.round(this.listSpacer.y()),
               Math.round(this.listSpacer.width()),
               Math.round(this.listSpacer.height()),
               0
            );
            this.scroller.scrollByWheel(amount, this.rowPitch(), listLayout.maxScroll());
            return true;
         } else {
            return this.containsPoint(mouseX, mouseY);
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.visible) {
         return false;
      }

      if (this.collapsed) {
         return false;
      }

      if (keyCode == 256) {
         if (this.draggingMacros) {
            this.resetMacroDrag();
            return true;
         }

         if (this.pressedFolder != null) {
            this.resetFolderDrag();
            return true;
         }

         if (this.contextMenu.isOpen()) {
            this.contextMenu.close();
            return true;
         }

         if (this.openDropdown != null && this.openDropdown.isOpen()) {
            this.openDropdown.close();
            return true;
         }

         if (this.folderDialogOpen) {
            this.closeFolderDialog();
            return true;
         }
      }

      if (!this.layoutHost.hasFocusedInput()) {
         boolean control = this.hasCtrl(modifiers);
         if (control && keyCode == 65) {
            this.selectAllLocal();
            return true;
         }

         if (control && keyCode == 67) {
            return this.copySelected();
         }

         if (control && keyCode == 88) {
            return this.cutSelected();
         }

         if (control && keyCode == 86) {
            return this.pasteMacros();
         }

         if (control && keyCode == 68) {
            return this.duplicateSelected();
         }

         if (keyCode == 261 || keyCode == 259) {
            return this.deleteSelected(false);
         }
      }

      if (this.layoutHost.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (keyCode == 256 && this.pasteDialogOpen) {
         this.closePasteDialog();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return this.visible && !this.collapsed && this.layoutHost.charTyped(chr, modifiers);
   }

   private List<AutismMacroListOverlay.ListItem> buildItems(
      List<AutismMacro> localMacros, Map<String, Map<String, AutismMacro>> remoteMacros, LanSyncManager sync
   ) {
      String filter = this.searchInput.getValue().trim().toLowerCase(Locale.ROOT);
      List<AutismMacroListOverlay.ListItem> items = new ArrayList<>();

      for (MacroFolderLayout.Inner_DataUtil row : MacroLibraryManager.getInstance().getOrderedFolderEntries(filter, true, macro -> !AutoFishMacroPresets.isStopMacro(macro))) {
         items.add(row.init_Z() ? AutismMacroListOverlay.ListItem.folderItem(row, null) : AutismMacroListOverlay.ListItem.localMacroItem(row.macro()));
      }

      if (sync.isInSession()) {
         for (Entry<String, Map<String, AutismMacro>> entry : remoteMacros.entrySet()) {
            List<AutismMacro> available = entry.getValue().values().stream().filter(m -> !AutoFishMacroPresets.isStopMacro(m)).filter(m -> MacroLibraryManager.getInstance().findByName(m.name) == null).toList();
            List<MacroFolderLayout.Inner_DataUtil> remoteRows = MacroFolderLayout.rows(
               available, List.of(), folder -> this.init_Set.contains(entry.getKey() + "\u0000" + MacroFolderLayout.key(folder)), filter, false
            );
            if (!remoteRows.isEmpty()) {
               items.add(AutismMacroListOverlay.ListItem.sectionHeaderItem(entry.getKey(), -12122));
            }

            for (MacroFolderLayout.Inner_DataUtil row : remoteRows) {
               items.add(
                  row.init_Z()
                     ? AutismMacroListOverlay.ListItem.folderItem(row, entry.getKey())
                     : AutismMacroListOverlay.ListItem.remoteMacroItem(row.macro().name, entry.getKey())
               );
            }
         }
      }

      return items;
   }

   private void renderFolderRow(
      GuiGraphicsExtractor graphics, autismclient.gui.ui.layout.LayoutContext context, AutismMacroListOverlay.ListItem item, int x, int y, int width
   ) {
      if (this.draggingFolder && item.source == null && this.isPressedFolder(item.label)) {
         autismclient.gui.ui.UiAlphaStack.push(0.35F);

         try {
            this.renderFolderControls(graphics, context, item, x, y, width);
         } finally {
            autismclient.gui.ui.UiAlphaStack.pop();
         }
      } else {
         this.renderFolderControls(graphics, context, item, x, y, width);
      }
   }

   private void renderFolderControls(
      GuiGraphicsExtractor graphics, autismclient.gui.ui.layout.LayoutContext context, AutismMacroListOverlay.ListItem item, int x, int y, int width
   ) {
      int renameWidth = this.clampedButtonWidth("Rename", 56);
      int removeWidth = this.clampedButtonWidth("Remove", 56);
      int buttonY = y + this.rowButtonInset();
      int controls = item.source == null ? renameWidth + this.iconGap() + removeWidth + this.rowPadding() : 0;
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, Math.max(1, width - controls), this.rowHeight());
      autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, this.font, Math.round(context.mouseX()), Math.round(context.mouseY()));
      boolean anyDrag = this.draggingMacros || this.draggingFolder;
      autismclient.gui.ui.widgets.GroupHeaderRenderer.load(
         ui,
         bounds,
         item.folderRow.init_Str(),
         item.folderRow.isFolder(),
         !anyDrag && bounds.init_Z(Math.round(context.mouseX()), Math.round(context.mouseY()))
      );
      this.hitRegions
         .add(new AutismMacroListOverlay.HitRegion(x, y, bounds.update(), this.rowHeight(), item, AutismMacroListOverlay.RowAction.TOGGLE_FOLDER));
      if (this.draggingMacros && this.get_Z && this.set_Z && item.source == null && MacroFolderLayout.key(item.label).equals(MacroFolderLayout.key(this.dropTargetFolder))) {
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(x, y, bounds.update(), this.rowHeight()),
            context.applyAlpha(ThemePaletteManager.recolor(-42149, ThemePaletteManager.ColorRole.OUTLINE))
         );
      }

      if (item.source == null) {
         AutismMacroListOverlay.RowButton rename = new AutismMacroListOverlay.RowButton(
            "Rename", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, renameWidth, AutismMacroListOverlay.RowAction.RENAME_FOLDER
         );
         AutismMacroListOverlay.RowButton remove = new AutismMacroListOverlay.RowButton(
            "Remove", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, removeWidth, AutismMacroListOverlay.RowAction.REMOVE_FOLDER
         );
         int renameX = x + width - controls;
         int removeX = renameX + renameWidth + this.iconGap();
         this.renderRowButton(graphics, context, renameX, buttonY, renameWidth, rename);
         this.renderRowButton(graphics, context, removeX, buttonY, removeWidth, remove);
         this.hitRegions
            .add(
               new AutismMacroListOverlay.HitRegion(renameX, buttonY, renameWidth, this.size_I_2(), item, AutismMacroListOverlay.RowAction.RENAME_FOLDER)
            );
         this.hitRegions
            .add(
               new AutismMacroListOverlay.HitRegion(removeX, buttonY, removeWidth, this.size_I_2(), item, AutismMacroListOverlay.RowAction.REMOVE_FOLDER)
            );
      }
   }

   private void openFolderDialog(String folder) {
      this.folderDialogOpen = true;
      this.pasteDialogOpen = false;
      this.renamingFolder = folder;
      this.folderNameInput.setValue(folder == null ? "" : folder);
      this.layoutDirty = true;
      this.folderNameInput.setFocused(true);
   }

   private void closeFolderDialog() {
      this.folderDialogOpen = false;
      this.renamingFolder = null;
      this.folderNameInput.setFocused(false);
      this.layoutDirty = true;
   }

   private boolean confirmFolderDialog() {
      MacroLibraryManager manager = MacroLibraryManager.getInstance();
      boolean saved = this.renamingFolder == null
         ? manager.createFolder(this.folderNameInput.getValue())
         : manager.renameFolder(this.renamingFolder, this.folderNameInput.getValue());
      if (!saved) {
         bi.read("Use a unique, nonempty folder name.");
         return true;
      } else {
         this.searchInput.setValue("");
         this.closeFolderDialog();
         return true;
      }
   }

   private void openMoveMenu(AutismMacroListOverlay.ListItem item) {
      if (item.macro != null) {
         List<String> folders = new ArrayList<>();
         folders.add("");
         folders.addAll(MacroLibraryManager.getInstance().getFolders());
         List<String> labels = folders.stream().map(f -> f.isEmpty() ? "Unfiled" : "Folder: " + f).toList();
         AutismMacroListOverlay.HitRegion anchor = this.hitRegions
            .stream()
            .filter(r -> r.item == item && r.action == AutismMacroListOverlay.RowAction.MOVE)
            .findFirst()
            .orElse(null);
         if (anchor != null) {
            int menuWidth = Math.max(
               56,
               UiLayoutMath.maxTextWidth(
                     this.font,
                     this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
                     this.styleTheme.load(autismclient.gui.f.b.l.read),
                     labels
                  )
                  + 26
            );
            this.openDropdown = new autismclient.gui.ui.widgets.DropdownMenu(
               autismclient.gui.f.NetHelper.init(anchor.x, anchor.y, menuWidth, this.size_I_2()),
               labels,
               item.macro.folder.isEmpty() ? "Unfiled" : "Folder: " + item.macro.folder,
               choice -> {
                  int index = labels.indexOf(choice);
                  if (index >= 0) {
                     MacroLibraryManager.getInstance().moveMacroToFolder(item.macro, folders.get(index));
                  }

                  this.layoutDirty = true;
               }
            );
            this.openDropdown.open();
         }
      }
   }

   private void moveSelectedToFolder() {
      List<AutismMacro> selected = this.selectedMacros();
      if (selected.isEmpty()) {
         bi.read("Select macros first.");
      } else {
         List<String> folders = new ArrayList<>();
         folders.add("");
         folders.addAll(MacroLibraryManager.getInstance().getFolders());
         List<String> labels = folders.stream().map(f -> f.isEmpty() ? "Unfiled" : "Folder: " + f).toList();
         int menuWidth = Math.max(
            56,
            UiLayoutMath.maxTextWidth(
                  this.font,
                  this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
                  this.styleTheme.load(autismclient.gui.f.b.l.read),
                  labels
               )
               + 26
         );
         String common = null;

         for (AutismMacro macro : selected) {
            String folder = macro.folder == null ? "" : macro.folder;
            if (common == null) {
               common = folder;
            } else if (!MacroFolderLayout.key(common).equals(MacroFolderLayout.key(folder))) {
               common = null;
               break;
            }
         }

         String current = common == null ? "" : (common.isEmpty() ? "Unfiled" : "Folder: " + common);
         autismclient.gui.f.NetHelper anchor = this.moveButton != null
            ? autismclient.gui.f.NetHelper.init(
               Math.round(this.moveButton.x()), Math.round(this.moveButton.y()), menuWidth, this.size_I_2()
            )
            : autismclient.gui.f.NetHelper.init(this.contextMenuX, this.contextMenuY, menuWidth, this.size_I_2());
         this.openDropdown = new autismclient.gui.ui.widgets.DropdownMenu(anchor, labels, current, choice -> {
            int index = labels.indexOf(choice);
            if (index >= 0) {
               String target = folders.get(index);
               List<AutismMacro> moving = new ArrayList<>();

               for (AutismMacro macro : this.selectedMacros()) {
                  if (!MacroFolderLayout.key(macro.folder).equals(MacroFolderLayout.key(target))) {
                     moving.add(macro);
                  }
               }

               if (moving.isEmpty()) {
                  bi.init("Already in " + (target.isEmpty() ? "Unfiled" : target) + ".", -5195580);
               } else if (MacroLibraryManager.getInstance().moveMacros(moving, target, null)) {
                  bi.load("Moved " + macroCountLabel(moving.size()) + " to " + (target.isEmpty() ? "Unfiled" : target) + ".");
               }
            }

            this.layoutDirty = true;
         });
         this.openDropdown.open();
      }
   }

   private boolean isSelected(AutismMacro macro) {
      return macro != null && this.selectedKeys.contains(MacroNameNormalizer.normalize(macro.name));
   }

   private void addToSelection(AutismMacro macro) {
      if (macro != null && macro.name != null && !macro.name.isBlank()) {
         this.selectedKeys.add(MacroNameNormalizer.normalize(macro.name));
      }
   }

   private void selectOnly(AutismMacro macro) {
      this.selectedKeys.clear();
      this.addToSelection(macro);
      this.selectionAnchorKey = macro != null && macro.name != null ? MacroNameNormalizer.normalize(macro.name) : null;
      this.refreshSelectionButtons();
   }

   private void selectMacros(List<AutismMacro> macros) {
      this.selectedKeys.clear();

      for (AutismMacro macro : macros) {
         this.addToSelection(macro);
      }

      this.selectionAnchorKey = !macros.isEmpty() && macros.get(macros.size() - 1).name != null ? MacroNameNormalizer.normalize(macros.get(macros.size() - 1).name) : null;
      this.refreshSelectionButtons();
   }

   private void toggleSelected(AutismMacro macro) {
      if (macro != null && macro.name != null && !macro.name.isBlank()) {
         String key = MacroNameNormalizer.normalize(macro.name);
         if (!this.selectedKeys.remove(key)) {
            this.selectedKeys.add(key);
         }

         this.selectionAnchorKey = key;
         this.refreshSelectionButtons();
      }
   }

   private void selectRangeTo(AutismMacro macro) {
      if (macro != null && macro.name != null) {
         String key = MacroNameNormalizer.normalize(macro.name);
         int from = -1;
         int to = -1;

         for (int i = 0; i < this.items.size(); i++) {
            AutismMacroListOverlay.ListItem item = this.items.get(i);
            if (item.kind == AutismMacroListOverlay.ItemKind.LOCAL_MACRO && item.macro != null) {
               String itemKey = MacroNameNormalizer.normalize(item.macro.name);
               if (itemKey.equals(this.selectionAnchorKey)) {
                  from = i;
               }

               if (itemKey.equals(key)) {
                  to = i;
               }
            }
         }

         if (from >= 0 && to >= 0) {
            this.selectedKeys.clear();

            for (int i = Math.min(from, to); i <= Math.max(from, to); i++) {
               AutismMacroListOverlay.ListItem item = this.items.get(i);
               if (item.kind == AutismMacroListOverlay.ItemKind.LOCAL_MACRO) {
                  this.addToSelection(item.macro);
               }
            }

            this.refreshSelectionButtons();
         } else {
            this.selectOnly(macro);
         }
      }
   }

   private void clearSelection() {
      if (!this.selectedKeys.isEmpty()) {
         this.selectedKeys.clear();
         this.refreshSelectionButtons();
      }
   }

   private void selectAllLocal() {
      for (AutismMacroListOverlay.ListItem item : this.items) {
         if (item.kind == AutismMacroListOverlay.ItemKind.LOCAL_MACRO) {
            this.addToSelection(item.macro);
         }
      }

      this.refreshSelectionButtons();
   }

   private List<AutismMacro> selectedMacros() {
      List<AutismMacro> out = new ArrayList<>();
      if (this.selectedKeys.isEmpty()) {
         return out;
      }

      for (AutismMacro macro : MacroLibraryManager.getInstance().getMacros()) {
         if (macro != null && this.selectedKeys.contains(MacroNameNormalizer.normalize(macro.name))) {
            out.add(macro);
         }
      }

      return out;
   }

   private void refreshSelectionButtons() {
      boolean hasSelection = !this.selectedKeys.isEmpty();
      if (this.moveButton != null) {
         this.moveButton.setEnabled(hasSelection);
      }

      if (this.copyButton != null) {
         this.copyButton.setEnabled(hasSelection);
      }
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

   private boolean copySelected() {
      List<AutismMacro> selected = this.selectedMacros();
      if (selected.isEmpty()) {
         bi.read("Select macros first.");
         return true;
      }

      if (MacroClipboardCodec.copyMacros(selected, false)) {
         bi.init("Copied " + macroCountLabel(selected.size()) + ".");
      } else {
         bi.write("Failed to copy macros.");
      }

      return true;
   }

   private boolean cutSelected() {
      List<AutismMacro> selected = this.selectedMacros();
      if (selected.isEmpty()) {
         bi.read("Select macros first.");
         return true;
      } else if (!MacroClipboardCodec.copyMacros(selected, true)) {
         bi.write("Failed to copy macros.");
         return true;
      } else {
         int removed = MacroLibraryManager.getInstance().deleteMacros(selected);
         this.clearSelection();
         bi.init("Cut " + macroCountLabel(removed) + ". Paste to put them back.");
         return true;
      }
   }

   private boolean pasteMacros() {
      MacroClipboardCodec.PastedMacros paste = MacroClipboardCodec.pasteMacros();
      if (paste.macros().isEmpty()) {
         bi.read("Clipboard has no macros.");
         return true;
      } else {
         List<AutismMacro> selected = this.selectedMacros();
         AutismMacro anchor = selected.isEmpty() ? null : selected.get(selected.size() - 1);
         List<AutismMacro> added = MacroLibraryManager.getInstance().insertMacros(paste.macros(), !paste.fromInternalCopy(), anchor);
         if (added.isEmpty()) {
            bi.write("Failed to paste macros.");
            return true;
         } else {
            this.searchInput.setValue("");
            this.selectMacros(added);
            this.layoutDirty = true;
            bi.load("Pasted " + macroCountLabel(added.size()) + ".");
            return true;
         }
      }
   }

   private boolean duplicateSelected() {
      List<AutismMacro> selected = this.selectedMacros();
      if (selected.isEmpty()) {
         bi.read("Select macros first.");
         return true;
      }

      List<AutismMacro> copies = MacroLibraryManager.getInstance().duplicateMacros(selected);
      if (!copies.isEmpty()) {
         this.selectMacros(copies);
         this.layoutDirty = true;
         bi.load("Duplicated " + macroCountLabel(copies.size()) + ".");
      }

      return true;
   }

   private boolean deleteSelected(boolean confirmed) {
      List<AutismMacro> selected = this.selectedMacros();
      if (selected.isEmpty()) {
         return false;
      }

      String signature = String.join("\u0000", this.selectedKeys);
      long now = System.currentTimeMillis();
      if (confirmed || signature.equals(this.pendingDeleteSignature) && now < this.pendingDeleteDeadline) {
         this.pendingDeleteSignature = null;
         int removed = MacroLibraryManager.getInstance().deleteMacros(selected);
         this.clearSelection();
         if (removed > 0) {
            bi.load("Deleted " + macroCountLabel(removed) + ".");
         }

         return true;
      } else {
         this.pendingDeleteSignature = signature;
         this.pendingDeleteDeadline = now + 3000L;
         bi.read("Press Delete again to delete " + macroCountLabel(selected.size()) + ".");
         return true;
      }
   }

   private static String macroCountLabel(int count) {
      return count + (count == 1 ? " macro" : " macros");
   }

   private String[] contextMenuItems() {
      return new String[]{"Cut\tCtrl+X", "Copy\tCtrl+C", "Paste\tCtrl+V", "Duplicate\tCtrl+D", "Move to Folder...", "Delete\tDel", "Select All\tCtrl+A"};
   }

   private void onContextMenuItem(AutismMacroListOverlay.ListItem item, int itemIndex) {
      switch (itemIndex) {
         case 0:
            this.cutSelected();
            break;
         case 1:
            this.copySelected();
            break;
         case 2:
            this.pasteMacros();
            break;
         case 3:
            this.duplicateSelected();
            break;
         case 4:
            this.moveSelectedToFolder();
            break;
         case 5:
            this.deleteSelected(true);
            break;
         case 6:
            this.selectAllLocal();
      }
   }

   private void openContextMenu(AutismMacroListOverlay.ListItem item, int mouseX, int mouseY) {
      if (item.macro != null) {
         if (!this.isSelected(item.macro)) {
            this.selectOnly(item.macro);
         }

         this.openDropdown = null;
         this.contextMenuX = mouseX;
         this.contextMenuY = mouseY;
         this.contextMenu.open(mouseX, mouseY, item);
      }
   }

   private autismclient.gui.ui.widgets.ScrollListLayout.ListLayout currentListLayout() {
      int viewX = Math.round(this.listSpacer.x());
      int viewY = Math.round(this.listSpacer.y());
      int viewW = Math.round(this.listSpacer.width());
      int viewH = Math.round(this.listSpacer.height());
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout baseLayout = this.listLayout(viewX, viewY, viewW, viewH, 0);
      return this.listLayout(viewX, viewY, viewW, viewH, this.scroller.tickAndClamp(0.0F, baseLayout.maxScroll()));
   }

   private int rowIndexAt(float uiMouseY, autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout) {
      if (this.items.isEmpty()) {
         return -1;
      }

      int index = (int)Math.floor((double)(uiMouseY - (listLayout.y() + listLayout.contentInset()) + listLayout.clampedScroll()) / listLayout.rowStep());
      return index >= 0 && index < this.items.size() ? index : -1;
   }

   private int rowY(autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout, int index) {
      return listLayout.y() + listLayout.contentInset() - listLayout.clampedScroll() + index * listLayout.rowStep();
   }

   private void beginMacroPress(
      AutismMacroListOverlay.ListItem item, float uiMouseX, float uiMouseY, autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout, int rowIndex
   ) {
      this.pressedMacroKey = MacroNameNormalizer.normalize(item.macro.name);
      this.pressedMacroWasSelected = this.selectedKeys.contains(this.pressedMacroKey);
      this.macroPressX = uiMouseX;
      this.macroPressY = uiMouseY;
      this.macroGrabOffsetY = uiMouseY - this.rowY(listLayout, rowIndex);
   }

   private void updateMacroDrag(float uiMouseX, float uiMouseY) {
      if (!this.draggingMacros && this.pressedMacroKey != null && (!(Math.abs(uiMouseX - this.macroPressX) < 3.0) || !(Math.abs(uiMouseY - this.macroPressY) < 3.0))) {
         AutismMacro pressed = MacroLibraryManager.getInstance().findByName(this.pressedMacroKey);
         if (pressed == null) {
            this.resetMacroDrag();
         } else {
            List<AutismMacro> dragged;
            if (this.selectedKeys.contains(this.pressedMacroKey)) {
               dragged = this.selectedMacros();
            } else {
               this.selectOnly(pressed);
               dragged = List.of(pressed);
            }

            if (dragged.isEmpty()) {
               this.resetMacroDrag();
            } else {
               this.draggingMacros = true;
               this.draggedMacros = dragged;
               this.draggedKeys.clear();

               for (AutismMacro macro : dragged) {
                  this.draggedKeys.add(MacroNameNormalizer.normalize(macro.name));
               }

               this.pressedDragIndex = Math.max(0, dragged.indexOf(pressed));
               this.get_Z = false;
               this.set_Z = false;
               this.put_Z = false;
               this.dropInsertBefore = null;
            }
         }
      }
   }

   private void resetMacroDrag() {
      this.pressedMacroKey = null;
      this.pressedMacroWasSelected = false;
      this.draggingMacros = false;
      this.draggedMacros = List.of();
      this.draggedKeys.clear();
      this.pressedDragIndex = 0;
      this.get_Z = false;
      this.set_Z = false;
      this.put_Z = false;
      this.dropInsertBefore = null;
      this.dropFoldersAbove = List.of();
      this.macroDropLineY = -2147483648;
      this.write_J = 0L;
      this.write_F = 0.0F;
   }

   private void finishMacroDrag() {
      if (this.draggingMacros) {
         List<AutismMacro> dragged = this.draggedMacros;
         boolean apply = this.get_Z && !this.put_Z;
         String targetFolder = this.dropTargetFolder;
         String insertBefore = this.dropInsertBefore;
         List<String> foldersAbove = this.dropFoldersAbove;
         this.resetMacroDrag();
         if (apply && !dragged.isEmpty()) {
            MacroLibraryManager.getInstance().moveMacros(dragged, targetFolder, insertBefore, foldersAbove);
         }
      } else {
         String pressedKey = this.pressedMacroKey;
         boolean wasSelected = this.pressedMacroWasSelected;
         this.resetMacroDrag();
         if (pressedKey != null) {
            AutismMacro pressed = MacroLibraryManager.getInstance().findByName(pressedKey);
            if (pressed != null) {
               if (wasSelected && this.selectedKeys.size() == 1) {
                  this.clearSelection();
               } else {
                  this.selectOnly(pressed);
               }
            }
         }
      }
   }

   private void autoScrollDrag(float uiMouseY, int viewX, int viewY, int viewW, int viewH) {
      int maxScroll = this.listLayout(viewX, viewY, viewW, viewH, 0).maxScroll();
      if (maxScroll > 0) {
         int listTop = viewY + 1;
         int listBottom = viewY + viewH - 1;
         int delta = 0;
         if (uiMouseY < listTop + 18) {
            float depth = listTop + 18 - uiMouseY;
            delta = -Math.min(12, Math.max(1, (int)Math.ceil(depth / 3.0F)));
         } else if (uiMouseY > listBottom - 18) {
            float depth = uiMouseY - (listBottom - 18);
            delta = Math.min(12, Math.max(1, (int)Math.ceil(depth / 3.0F)));
         }

         long now = System.nanoTime();
         float frames = this.write_J == 0L ? 1.0F : Math.min(6.0F, (float)(now - this.write_J) / 1.6666667E7F);
         this.write_J = now;
         if (delta == 0) {
            this.write_F = 0.0F;
         } else {
            float px = delta * frames + this.write_F;
            int whole = (int)px;
            this.write_F = px - whole;
            if (whole != 0) {
               this.scroller.scrollByWheel(-whole, 1.0F, maxScroll);
            }
         }
      }
   }

   private void updateMacroDropTarget(autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout, float uiMouseX, float uiMouseY) {
      this.get_Z = false;
      this.set_Z = false;
      this.put_Z = false;
      this.dropTargetFolder = "";
      this.dropInsertBefore = null;
      this.dropFoldersAbove = List.of();
      this.macroDropLineY = -2147483648;
      if (!this.items.isEmpty()
         && this.contains(
            this.listSpacer.x(),
            this.listSpacer.y(),
            this.listSpacer.width(),
            this.listSpacer.height(),
            uiMouseX,
            uiMouseY
         )) {
         int lastLocal = this.pop_I_15();
         if (lastLocal >= 0) {
            int index = Math.max(0, (int)Math.floor((double)(uiMouseY - (listLayout.y() + listLayout.contentInset()) + listLayout.clampedScroll()) / listLayout.rowStep()));
            if (index > lastLocal) {
               this.init_V_TextFormat_DataUti_2(listLayout, lastLocal + 1, lastLocal);
            } else {
               AutismMacroListOverlay.ListItem item = this.items.get(index);
               int rowTop = this.rowY(listLayout, index);
               if (item.kind == AutismMacroListOverlay.ItemKind.FOLDER) {
                  if (uiMouseY < rowTop + listLayout.rowHeight() / 3.0F) {
                     this.init_V_TextFormat_DataUti_2(listLayout, index, lastLocal);
                  } else {
                     this.dropTargetFolder = item.label;
                     this.set_Z = true;
                     this.get_Z = true;
                  }
               } else {
                  boolean before = uiMouseY < rowTop + listLayout.rowHeight() / 2.0F;
                  String folder = item.macro.folder == null ? "" : item.macro.folder;
                  if (folder.isEmpty()) {
                     this.init_V_TextFormat_DataUti_2(listLayout, before ? index : index + 1, lastLocal);
                  } else {
                     this.dropTargetFolder = folder;
                     this.macroDropLineY = before ? rowTop : rowTop + listLayout.rowStep();
                     this.dropInsertBefore = this.init_Str_I_Str(before ? index : index + 1, folder);
                     this.get_Z = true;
                     this.put_Z = this.init_Z_Str_Str_2(folder, this.dropInsertBefore);
                  }
               }
            }
         }
      }
   }

   private void init_V_TextFormat_DataUti_2(autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout, int gap, int lastLocal) {
      this.dropTargetFolder = "";
      this.macroDropLineY = gap <= lastLocal
         ? this.rowY(listLayout, gap)
         : this.rowY(listLayout, lastLocal) + listLayout.rowStep();
      String next = null;

      for (int i = gap; i <= lastLocal; i++) {
         AutismMacroListOverlay.ListItem row = this.items.get(i);
         if (init_Z_AutismMacroListOve(row) && !this.draggedKeys.contains(MacroNameNormalizer.normalize(row.macro.name))) {
            next = row.macro.name;
            break;
         }
      }

      this.dropInsertBefore = next;
      List<String> above = new ArrayList<>();

      for (int i = gap - 1; i >= 0; i--) {
         AutismMacroListOverlay.ListItem row = this.items.get(i);
         if (init_Z_AutismMacroListOve(row)) {
            if (!this.draggedKeys.contains(MacroNameNormalizer.normalize(row.macro.name))) {
               break;
            }
         } else if (row.kind == AutismMacroListOverlay.ItemKind.FOLDER && row.source == null) {
            above.add(0, row.label);
         }
      }

      this.dropFoldersAbove = List.copyOf(above);
      this.get_Z = true;
      boolean foldersAlreadyThere = true;
      String first = this.draggedMacros.isEmpty() ? null : MacroNameNormalizer.normalize(this.draggedMacros.get(0).name);

      for (String folder : above) {
         if (first == null || !first.equals(MacroLibraryManager.getInstance().getFolderAnchor(folder))) {
            foldersAlreadyThere = false;
            break;
         }
      }

      this.put_Z = foldersAlreadyThere && this.init_Z_Str_Str_2("", next);
   }

   private static boolean init_Z_AutismMacroListOve(AutismMacroListOverlay.ListItem row) {
      return row.kind == AutismMacroListOverlay.ItemKind.LOCAL_MACRO
         && row.macro != null
         && (row.macro.folder == null || row.macro.folder.isEmpty());
   }

   private int pop_I_15() {
      int last = -1;

      for (int i = 0; i < this.items.size(); last = i++) {
         AutismMacroListOverlay.ListItem item = this.items.get(i);
         if (item.kind != AutismMacroListOverlay.ItemKind.LOCAL_MACRO
            && (item.kind != AutismMacroListOverlay.ItemKind.FOLDER || item.source != null)) {
            break;
         }
      }

      return last;
   }

   private String init_Str_I_Str(int from, String folder) {
      String folderKey = MacroFolderLayout.key(folder);

      for (int i = Math.max(0, from); i < this.items.size(); i++) {
         AutismMacroListOverlay.ListItem candidate = this.items.get(i);
         if (candidate.kind != AutismMacroListOverlay.ItemKind.LOCAL_MACRO || candidate.macro == null) {
            return null;
         }

         if (!MacroFolderLayout.key(candidate.macro.folder).equals(folderKey)) {
            return null;
         }

         if (!this.draggedKeys.contains(MacroNameNormalizer.normalize(candidate.macro.name))) {
            return candidate.macro.name;
         }
      }

      return null;
   }

   private boolean init_Z_Str_Str_2(String folder, String insertBefore) {
      String folderKey = MacroFolderLayout.key(folder);
      List<AutismMacro> run = new ArrayList<>();

      for (AutismMacro macro : MacroLibraryManager.getInstance().getMacros()) {
         if (macro != null && MacroFolderLayout.key(macro.folder).equals(folderKey)) {
            run.add(macro);
         }
      }

      int first = -1;
      int last = -1;
      int count = 0;

      for (int i = 0; i < run.size(); i++) {
         if (this.draggedKeys.contains(MacroNameNormalizer.normalize(run.get(i).name))) {
            if (first < 0) {
               first = i;
            }

            last = i;
            count++;
         }
      }

      if (count == this.draggedKeys.size() && last - first + 1 == count) {
         String follower = last + 1 < run.size() ? run.get(last + 1).name : null;
         return insertBefore == null ? follower == null : follower != null && MacroNameNormalizer.sameName(follower, insertBefore);
      } else {
         return false;
      }
   }

   private boolean isPressedFolder(String folder) {
      return this.pressedFolder != null && folder != null && !folder.isEmpty() && MacroFolderLayout.key(folder).equals(MacroFolderLayout.key(this.pressedFolder));
   }

   private void resetFolderDrag() {
      this.pressedFolder = null;
      this.draggingFolder = false;
      this.push_Z = false;
      this.pop_Z = false;
      this.update_Str = null;
      this.sync_Str = null;
      this.folderDropLineY = -2147483648;
      this.write_J = 0L;
      this.write_F = 0.0F;
   }

   private void finishFolderDrag() {
      String folder = this.pressedFolder;
      boolean dragged = this.draggingFolder;
      boolean apply = this.push_Z && !this.pop_Z;
      String before = this.update_Str;
      String anchor = this.sync_Str;
      this.resetFolderDrag();
      if (folder != null) {
         if (!dragged) {
            MacroLibraryManager.getInstance().toggleFolderCollapsed(folder);
            this.scrollbarDragging = false;
            this.layoutDirty = true;
         } else if (apply && MacroLibraryManager.getInstance().reorderFolderWithAnchor(folder, before, anchor)) {
            this.layoutDirty = true;
         }
      }
   }

   private void updateFolderDropTarget(autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout, float uiMouseX, float uiMouseY) {
      this.push_Z = false;
      this.pop_Z = false;
      this.update_Str = null;
      this.sync_Str = null;
      this.folderDropLineY = -2147483648;
      if (!this.items.isEmpty()
         && this.contains(
            this.listSpacer.x(),
            this.listSpacer.y(),
            this.listSpacer.width(),
            this.listSpacer.height(),
            uiMouseX,
            uiMouseY
         )) {
         int lastLocal = this.pop_I_15();
         if (lastLocal >= 0) {
            int index = Math.max(0, (int)Math.floor((double)(uiMouseY - (listLayout.y() + listLayout.contentInset()) + listLayout.clampedScroll()) / listLayout.rowStep()));
            int gap;
            if (index > lastLocal) {
               gap = lastLocal + 1;
            } else {
               AutismMacroListOverlay.ListItem item = this.items.get(index);
               boolean topHalf = uiMouseY < this.rowY(listLayout, index) + listLayout.rowHeight() / 2.0F;
               if (item.kind == AutismMacroListOverlay.ItemKind.FOLDER) {
                  gap = topHalf ? index : this.folderEndIndex(index) + 1;
               } else if (init_Z_AutismMacroListOve(item)) {
                  gap = topHalf ? index : index + 1;
               } else {
                  gap = this.folderEndIndex(this.load_I_I_4(index)) + 1;
               }
            }

            int own = -1;

            for (int i = 0; i <= lastLocal; i++) {
               AutismMacroListOverlay.ListItem item = this.items.get(i);
               if (item.kind == AutismMacroListOverlay.ItemKind.FOLDER && item.source == null && this.isPressedFolder(item.label)) {
                  own = i;
                  break;
               }
            }

            this.push_Z = true;
            this.folderDropLineY = gap <= lastLocal
               ? this.rowY(listLayout, gap)
               : this.rowY(listLayout, lastLocal) + listLayout.rowStep();
            if (own < 0 || gap != own && gap != this.folderEndIndex(own) + 1) {
               for (int i = gap; i <= lastLocal; i++) {
                  AutismMacroListOverlay.ListItem item = this.items.get(i);
                  if (this.sync_Str == null && init_Z_AutismMacroListOve(item)) {
                     this.sync_Str = item.macro.name;
                  }

                  if (this.update_Str == null
                     && item.kind == AutismMacroListOverlay.ItemKind.FOLDER
                     && item.source == null
                     && !this.isPressedFolder(item.label)) {
                     this.update_Str = item.label;
                  }
               }
            } else {
               this.pop_Z = true;
            }
         }
      }
   }

   private int load_I_I_4(int index) {
      AutismMacroListOverlay.ListItem item = this.items.get(index);
      if (item.kind != AutismMacroListOverlay.ItemKind.FOLDER && item.macro != null) {
         String key = MacroFolderLayout.key(item.macro.folder);

         for (int i = index - 1; i >= 0; i--) {
            AutismMacroListOverlay.ListItem row = this.items.get(i);
            if (row.kind == AutismMacroListOverlay.ItemKind.FOLDER && row.source == null && MacroFolderLayout.key(row.label).equals(key)) {
               return i;
            }
         }

         return index;
      } else {
         return index;
      }
   }

   private int folderEndIndex(int header) {
      AutismMacroListOverlay.ListItem head = this.items.get(header);
      if (head.kind != AutismMacroListOverlay.ItemKind.FOLDER) {
         return header;
      }

      String key = MacroFolderLayout.key(head.label);
      int end = header;

      for (int i = header + 1; i < this.items.size(); end = i++) {
         AutismMacroListOverlay.ListItem row = this.items.get(i);
         if (row.kind != AutismMacroListOverlay.ItemKind.LOCAL_MACRO
            || row.macro == null
            || !MacroFolderLayout.key(row.macro.folder).equals(key)) {
            break;
         }
      }

      return end;
   }

   private void renderDraggedFolder(GuiGraphicsExtractor context, int x, int y, int width) {
      AutismMacroListOverlay.ListItem folder = null;

      for (AutismMacroListOverlay.ListItem item : this.items) {
         if (item.kind == AutismMacroListOverlay.ItemKind.FOLDER && item.source == null && this.isPressedFolder(item.label)) {
            folder = item;
            break;
         }
      }

      if (folder != null) {
         autismclient.gui.ui.UiAlphaStack.push(0.9F);

         try {
            autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, this.rowHeight());
            autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(context, this.font, 0, 0);
            autismclient.gui.ui.widgets.GroupHeaderRenderer.load(ui, bounds, folder.folderRow.init_Str(), folder.folderRow.isFolder(), true);
            autismclient.gui.f.PacketUtil.load(context, bounds, ThemePaletteManager.recolor(-42149, ThemePaletteManager.ColorRole.OUTLINE));
         } finally {
            autismclient.gui.ui.UiAlphaStack.pop();
         }
      }
   }

   private void renderDraggedMacro(GuiGraphicsExtractor context, AutismMacro macro, int x, int y, int width, float alpha) {
      int fill = autismclient.gui.ui.layout.LayoutContext.scaleAlpha(ThemePaletteManager.recolor(1715082778, ThemePaletteManager.ColorRole.ACCENT), alpha * 0.85F);
      int edge = autismclient.gui.ui.layout.LayoutContext.scaleAlpha(ThemePaletteManager.recolor(-42149, ThemePaletteManager.ColorRole.OUTLINE), alpha * 0.85F);
      autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(context, x, y, width, this.rowHeight(), fill);
      autismclient.gui.f.PacketUtil.load(context, autismclient.gui.f.NetHelper.init(x, y, width, this.rowHeight()), edge);
      int textLeft = x + this.rowPadding() + (macro.folder != null && !macro.folder.isEmpty() ? 10 : 0) + this.entry() + this.nameGap();
      int textY = UiLayoutMath.centerTextY(y, this.rowHeight(), this.styleTheme.init_I_l(autismclient.gui.f.b.l.read), this.styleTheme.flush());
      int color = autismclient.gui.ui.layout.LayoutContext.scaleAlpha(this.styleTheme.load(autismclient.gui.f.b.l.read), alpha * 0.85F);
      String label = autismclient.gui.f.b.k.init(
         this.font,
         macro.name,
         Math.max(20, x + width - this.rowPadding() - textLeft),
         this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read),
         color
      );
      autismclient.gui.f.b.k.init(
         context, this.font, label, this.styleTheme.init_Identifier_l(autismclient.gui.f.b.l.read), color, textLeft, textY, false
      );
   }

   private int macroCount() {
      long revision = MacroLibraryManager.getInstance().getRevision();
      if (revision == this.cachedCountRevision) {
         return this.cachedMacroCount;
      }

      int count = 0;

      for (AutismMacro macro : MacroLibraryManager.getInstance().getMacros()) {
         if (!AutoFishMacroPresets.isStopMacro(macro)) {
            count++;
         }
      }

      this.cachedCountRevision = revision;
      this.cachedMacroCount = count;
      return this.cachedMacroCount;
   }

   private boolean matchesFilter(String name, String filter) {
      return filter.isEmpty() || name != null && name.toLowerCase(Locale.ROOT).contains(filter);
   }

   private boolean isOverTitleBar(float uiMouseX, float uiMouseY) {
      return uiMouseX >= this.overlayX && uiMouseX < this.overlayX + this.overlayWidth && uiMouseY >= this.overlayY && uiMouseY < this.overlayY + this.styleTheme.load();
   }

   private boolean load_Z_F_F_3(float uiMouseX, float uiMouseY) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.overClose(
         autismclient.gui.f.NetHelper.init(this.overlayX, this.overlayY, this.overlayWidth, Math.max(this.styleTheme.load(), this.overlayHeight)),
         this.styleTheme.load(),
         uiMouseX,
         uiMouseY
      );
   }

   private OverlayBounds clampBounds(OverlayBounds bounds) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      int margin = 4;
      int viewportW = Math.round(viewport.width());
      int viewportH = Math.round(viewport.height());
      int availableW = Math.max(1, viewportW - margin * 2);
      int availableH = Math.max(this.styleTheme.load(), viewportH - margin * 2);
      int width = Math.max(Math.min(this.getMinWidth(), availableW), Math.min(bounds.width, availableW));
      int minHeight = bounds.collapsed ? this.styleTheme.load() : this.getMinHeight();
      int height = Math.max(Math.min(minHeight, availableH), Math.min(bounds.height, availableH));
      int renderedHeight = bounds.collapsed ? this.styleTheme.load() : height;
      int x = Math.max(margin, Math.min(bounds.x, Math.max(margin, viewportW - margin - width)));
      int y = Math.max(margin, Math.min(bounds.y, Math.max(margin, viewportH - margin - renderedHeight)));
      return new OverlayBounds(x, y, width, height, bounds.visible, bounds.collapsed);
   }

   private boolean contains(float x, float y, float width, float height, float mouseX, float mouseY) {
      return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
   }

   private int run_I_13() {
      return this.measuredWidth;
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14) + 2;
   }

   private int rowPitch() {
      return this.rowHeight() + 2;
   }

   private int maxVisibleRows() {
      int screenH = Math.round(this.layoutHost.createViewport().height());
      return Math.max(4, (int)(screenH * 0.6F) / this.rowPitch());
   }

   private int add_I_11() {
      return 4;
   }

   private int clear_I_5() {
      return 2;
   }

   private int reset_I_4() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int start_I_10() {
      return 2;
   }

   private int stop_I_4() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int next_I_4() {
      return 12;
   }

   private int prev_I_3() {
      return 10;
   }

   private int valid_I_2() {
      return 3;
   }

   private int size_I_2() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13) - 1;
   }

   private int data_I_2() {
      return this.size_I_2();
   }

   private int iconGap() {
      return 2;
   }

   private int rowButtonInset() {
      return Math.max(0, (this.rowHeight() - this.size_I_2()) / 2);
   }

   private int rowPadding() {
      return 4;
   }

   private int nameGap() {
      return 4;
   }

   private int dividerInset() {
      return 4;
   }

   private int entry() {
      return 4;
   }

   private enum RowAction {
      TOGGLE_FOLDER,
      RENAME_FOLDER,
      REMOVE_FOLDER,
      MOVE,
      RUN_TOGGLE,
      EDIT,
      COPY,
      DELETE,
      IMPORT_REMOTE;
   }

   private enum ItemKind {
      FOLDER,
      SECTION_HEADER,
      LOCAL_MACRO,
      REMOTE_MACRO;
   }

   private static final class RowButton {
      private final String label;
      private final autismclient.gui.ui.layout.ButtonNode.Variant variant;
      private final int width;
      private final AutismMacroListOverlay.RowAction action;
      private final Identifier icon;

      private RowButton(
         String label, autismclient.gui.ui.layout.ButtonNode.Variant variant, int width, AutismMacroListOverlay.RowAction action
      ) {
         this(label, variant, width, action, null);
      }

      private RowButton(
         String label, autismclient.gui.ui.layout.ButtonNode.Variant variant, int width, AutismMacroListOverlay.RowAction action, Identifier icon
      ) {
         this.label = label;
         this.variant = variant;
         this.width = width;
         this.action = action;
         this.icon = icon;
      }
   }

   private static final class HitRegion {
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      private final AutismMacroListOverlay.ListItem item;
      private final AutismMacroListOverlay.RowAction action;

      private HitRegion(
         int x, int y, int width, int height, AutismMacroListOverlay.ListItem item, AutismMacroListOverlay.RowAction action
      ) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.item = item;
         this.action = action;
      }

      private boolean contains(float mouseX, float mouseY) {
         return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
      }
   }

   private static final class ListItem {
      private AutismMacroListOverlay.ItemKind kind;
      private String label;
      private int init_I;
      private AutismMacro macro;
      private String source;
      private MacroFolderLayout.Inner_DataUtil folderRow;

      private static AutismMacroListOverlay.ListItem folderItem(MacroFolderLayout.Inner_DataUtil row, String source) {
         AutismMacroListOverlay.ListItem item = new AutismMacroListOverlay.ListItem();
         item.kind = AutismMacroListOverlay.ItemKind.FOLDER;
         item.label = row.folderName();
         item.folderRow = row;
         item.source = source;
         return item;
      }

      private static AutismMacroListOverlay.ListItem sectionHeaderItem(String label, int color) {
         AutismMacroListOverlay.ListItem item = new AutismMacroListOverlay.ListItem();
         item.kind = AutismMacroListOverlay.ItemKind.SECTION_HEADER;
         item.label = label;
         item.init_I = color;
         return item;
      }

      private static AutismMacroListOverlay.ListItem localMacroItem(AutismMacro macro) {
         AutismMacroListOverlay.ListItem item = new AutismMacroListOverlay.ListItem();
         item.kind = AutismMacroListOverlay.ItemKind.LOCAL_MACRO;
         item.macro = macro;
         return item;
      }

      private static AutismMacroListOverlay.ListItem remoteMacroItem(String label, String source) {
         AutismMacroListOverlay.ListItem item = new AutismMacroListOverlay.ListItem();
         item.kind = AutismMacroListOverlay.ItemKind.REMOTE_MACRO;
         item.label = label;
         item.source = source;
         return item;
      }
   }
}
