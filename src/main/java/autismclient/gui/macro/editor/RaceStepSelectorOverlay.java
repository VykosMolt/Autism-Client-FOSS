package autismclient.gui.macro.editor;

import autismclient.gui.ui.UiClipStack;
import autismclient.gui.f.NetHelper;
import autismclient.gui.ui.widgets.TextInputWidget;
import autismclient.gui.f.b.CharEncoder;
import autismclient.gui.ui.widgets.WindowHeaderHelper;
import autismclient.gui.ui.widgets.ListRowRenderer;
import autismclient.gui.ui.widgets.PanelSurfaceHelper;
import autismclient.gui.ui.widgets.SearchFilterList;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.gui.ui.layout.ScrollableListView;
import autismclient.gui.ui.layout.EdgeInsets;
import autismclient.gui.ui.layout.LabelNode;
import autismclient.gui.ui.layout.LayoutViewport;
import autismclient.gui.ui.layout.LayoutContext;
import autismclient.gui.ui.layout.LayoutTreeHost;
import autismclient.util.DraggableOverlayWindow;
import autismclient.util.OverlayManager;
import autismclient.util.OverlayBounds;
import autismclient.util.UiColorPalette;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class RaceStepSelectorOverlay extends DraggableOverlayWindow {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final int flush_I = 160;
   private static final int close_I = 260;
   private static final int open = 14;
   private static final int poll = 3;
   private static final int push = 12;
   private static final float init_F_2 = 0.65F;
   private static final int pop = 4;
   private static final int tick = 8;
   private static final int run = 12;
   private static final int get = 10;
   private static final int set = 3;
   private static final int put = 6;
   private final Font font;
   private final CharEncoder styleTheme = new CharEncoder();
   private final autismclient.gui.ui.layout.CollapsibleWindowNode windowNode = new autismclient.gui.ui.layout.CollapsibleWindowNode("Race Selector");
   private final LayoutTreeHost layoutHost = new LayoutTreeHost(this.styleTheme, this.windowNode);
   private final TextInputWidget searchInput = new TextInputWidget();
   private final LabelNode countLabel = new LabelNode("", l.write).setTrimToBounds(true);
   private final autismclient.gui.ui.layout.VerticalSpacerNode listSpacer = new autismclient.gui.ui.layout.VerticalSpacerNode();
   private ScrollableListView listView;
   private final SearchFilterList<RaceStepSelectorOverlay.Inner_DataUtil> search = new SearchFilterList<>(
      option -> option.load() + "\n" + option.read() + "\n" + option.init() + "\n" + option.write()
   );
   private List<RaceStepSelectorOverlay.Inner_DataUtil> options = List.of();
   private List<RaceStepSelectorOverlay.Inner_DataUtil> filteredOptions = List.of();
   private Consumer<RaceStepSelectorOverlay.Inner_DataUtil> onSelect;
   private boolean visible;
   private boolean update_Z;
   private boolean sync_Z;
   private boolean flush_Z;
   private float dragOffsetX;
   private float dragOffsetY;
   private float pressX;
   private float pressY;
   private int add;
   private int clear;
   private int reset = 160;
   private int start;
   private final autismclient.gui.ui.VisibilityTransition visibility = new autismclient.gui.ui.VisibilityTransition(false);

   public RaceStepSelectorOverlay(Font textRenderer) {
      this.font = textRenderer;
      this.windowNode.setCenterTitle(false);
      this.windowNode.setTitleTone(l.load);
      this.windowNode.setHeaderButtons(true, true);
      this.windowNode.setTitleInsets(8, pop_I_3());
      this.windowNode.body().init(2).init(EdgeInsets.uniform(4));
      this.searchInput.setPlaceholder("Search...").setFixedHeight(13).withGrowX(true).setOnChange(this::applyQuery);
      this.rebuildLayout();
   }

   public void openSelector(String title, List<RaceStepSelectorOverlay.Inner_DataUtil> options, Consumer<RaceStepSelectorOverlay.Inner_DataUtil> onSelect) {
      this.visible = true;
      this.update_Z = false;
      this.sync_Z = false;
      this.flush_Z = false;
      this.onSelect = onSelect;
      this.options = options == null ? List.of() : List.copyOf(options);
      this.search.setItems(this.options);
      this.search.setQuery("");
      this.filteredOptions = this.search.filtered();
      String shownTitle = title != null && !title.isBlank() ? title : "Race Selector";
      this.windowNode.setTitle(shownTitle);
      this.reset = this.preferredWidth(shownTitle);
      this.searchInput.setValue("");
      this.searchInput.setFocused(true);
      this.rebuildLayout();
      LayoutContext metrics = this.layoutHost.createMeasureContext();
      if (metrics != null) {
         this.start = Math.round(this.windowNode.measureHeight(metrics, this.reset));
      }

      LayoutViewport viewport = this.layoutHost.createViewport();
      this.add = Math.max(8, Math.round((viewport.width() - this.reset) / 2.0F));
      this.clear = Math.max(8, Math.round((viewport.height() - this.start) / 2.0F));
      OverlayBounds clamped = this.clampBounds(new OverlayBounds(this.add, this.clear, this.reset, this.start, this.visible, this.update_Z));
      this.add = clamped.x;
      this.clear = clamped.y;
      this.reset = clamped.width;
      this.start = clamped.height;
      this.windowNode.setShowBody(true);
      OverlayManager.getInstance().bringOwnerToFront(this);
   }

   public void dismiss() {
      this.visible = false;
      this.update_Z = false;
      this.sync_Z = false;
      this.flush_Z = false;
      this.onSelect = null;
      this.layoutHost.clearFocus();
      this.windowNode.setShowBody(true);
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.visible && this.searchInput.isFocused();
   }

   private void applyQuery(String query) {
      this.search.setItems(this.options);
      this.search.setQuery(query);
      this.filteredOptions = this.search.filtered();
      if (this.listView != null) {
         this.listView.setScrollOffset(0);
      }

      this.rebuildLayout();
   }

   private void rebuildLayout() {
      this.windowNode.body().clearChildren();
      this.windowNode.body().addChild(this.searchInput);
      this.listSpacer.init(this.init(this.filteredOptions.size()));
      this.windowNode.body().addChild(this.listSpacer);
      this.countLabel.init(this.filteredOptions.size() + " choices");
      this.windowNode.body().addChild(this.countLabel);
   }

   private int init(int count) {
      int rows = Math.max(3, Math.min(this.maxVisibleRows(), count));
      return rows * 14 + 2;
   }

   private int maxVisibleRows() {
      int screenH = Math.round(this.layoutHost.createViewport().height());
      if (screenH <= 0) {
         return 12;
      }

      int fit = (int)(screenH * 0.65F) / 14;
      return Math.max(3, Math.min(12, fit));
   }

   private static int pop_I_3() {
      return 41;
   }

   private int preferredWidth(String title) {
      int widest = 0;

      for (RaceStepSelectorOverlay.Inner_DataUtil option : this.options) {
         widest = Math.max(
            widest,
            k.init(
               this.font, option.init() + " / " + option.read(), this.styleTheme.init_Identifier_l(l.read), this.styleTheme.load(l.read)
            )
         );
      }

      int listW = widest + 10 + 6 + 3 + 8;
      int titleW = k.init(this.font, title, this.styleTheme.init_Identifier_l(l.load), this.styleTheme.load(l.load)) + 8 + pop_I_3();
      return Math.max(160, Math.min(260, Math.max(listW, titleW)));
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      this.visibility.animateTo(this.visible);
      if (this.visibility.shouldRender(this.visible) && MC != null && MC.font != null) {
         boolean closing = !this.visible;
         this.visibility
            .init(
               context,
               NetHelper.init(this.add, this.clear, this.reset, Math.max(this.styleTheme.load(), this.start)),
               true,
               () -> this.load(context, closing ? -10000 : mouseX, closing ? -10000 : mouseY, delta)
            );
      }
   }

   private void load(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      context.nextStratum();
      LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      LayoutContext metrics = new LayoutContext(context, MC.font, viewport, this.styleTheme, uiMouseX, uiMouseY, delta);
      this.windowNode.setExpanded(!this.update_Z);
      this.windowNode.setActive(true);
      this.windowNode.setHeaderHovered(this.isOverTitleBar(uiMouseX, uiMouseY));
      this.start = Math.max(this.styleTheme.load(), Math.round(this.windowNode.measureHeight(metrics, this.reset)));
      OverlayBounds clamped = this.clampBounds(new OverlayBounds(this.add, this.clear, this.reset, this.start, this.visible, this.update_Z));
      this.add = clamped.x;
      this.clear = clamped.y;
      this.reset = clamped.width;
      this.start = clamped.height;
      this.windowNode.setBounds(this.add, this.clear, this.reset, this.start);
      this.layoutHost.render(context, mouseX, mouseY, delta);
      context.nextStratum();
      if (this.windowNode.pushBodyClip(context)) {
         try {
            this.renderList(context, uiMouseX, uiMouseY);
         } finally {
            UiClipStack.getInstance().pop(context);
         }
      }
   }

   private void renderList(GuiGraphicsExtractor context, float uiMouseX, float uiMouseY) {
      int x = Math.round(this.listSpacer.x());
      int y = Math.round(this.listSpacer.y());
      int w = Math.round(this.listSpacer.width());
      int h = Math.round(this.listSpacer.height());
      if (w > 2 && h > 2) {
         if (this.filteredOptions.isEmpty()) {
            ListRowRenderer.drawEmptyPlaceholder(context, this.font, "Nothing to pick.", x, y, w - 6 - 1);
         } else {
            if (this.listView == null
               || this.listView.getX() != x
               || this.listView.getY() != y
               || this.listView.getWidth() != w
               || this.listView.getHeight() != h) {
               int oldScroll = this.listView == null ? 0 : this.listView.getScrollOffset();
               this.listView = new ScrollableListView(x, y, w, h, 14, 6);
               this.listView.setScrollOffset(oldScroll);
            }

            this.listView.setContentHeight(this.filteredOptions.size() * 14);
            this.listView.beginFrame(context, this.styleTheme.set(), this.styleTheme.reset());

            try {
               this.listView
                  .forEachVisibleRow(
                     context,
                     this.filteredOptions.size(),
                     (idx, bnd) -> this.init(context, this.filteredOptions.get(idx), bnd.x, bnd.y, bnd.width, idx, uiMouseX, uiMouseY)
                  );
            } finally {
               this.listView.endFrame(context);
            }

            this.listView.renderScrollbar(context, uiMouseX, uiMouseY);
         }
      }
   }

   private void init(
      GuiGraphicsExtractor context, RaceStepSelectorOverlay.Inner_DataUtil option, int x, int y, int width, int index, float mouseX, float mouseY
   ) {
      boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 14;
      int bg = hovered ? this.styleTheme.next() : this.styleTheme.stop();
      int accent = "Condition".equalsIgnoreCase(option.init()) ? UiColorPalette.cyan() : UiColorPalette.accent();
      PanelSurfaceHelper.fillRect(context, x, y, width, 14, bg);
      PanelSurfaceHelper.drawAccentBar(context, x, y, 2, 14, accent);
      String label = option.init() + " / " + option.read();
      String trimmed = k.init(
         this.font, label, Math.max(1, width - 10), this.styleTheme.init_Identifier_l(l.read), this.styleTheme.load(l.read)
      );
      int textY = UiLayoutMath.centerTextY(y, 14, this.styleTheme.init_I_l(l.read), this.styleTheme.flush());
      k.init(context, this.font, trimmed, this.styleTheme.init_Identifier_l(l.read), this.styleTheme.load(l.read), x + 6, textY, false);
      PanelSurfaceHelper.drawDividerColored(context, x + 4, y + 14 - 1, width - 8, UiColorPalette.subtleDivider());
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.visible) {
         return false;
      }

      LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (button == 0 && this.load_Z_F_F(uiMouseX, uiMouseY)) {
         this.dismiss();
         return true;
      }

      if (button == 0 && this.isOverTitleBar(uiMouseX, uiMouseY)) {
         this.sync_Z = true;
         this.flush_Z = false;
         this.dragOffsetX = uiMouseX - this.add;
         this.dragOffsetY = uiMouseY - this.clear;
         this.pressX = uiMouseX;
         this.pressY = uiMouseY;
         return true;
      }

      if (!this.update_Z && this.layoutHost.mouseClicked(mouseX, mouseY, button)) {
         return true;
      }

      if (!this.update_Z
         && button == 0
         && this.init(
            this.listSpacer.x(),
            this.listSpacer.y(),
            this.listSpacer.width(),
            this.listSpacer.height(),
            uiMouseX,
            uiMouseY
         )) {
         if (this.listView != null && this.listView.mouseClicked(uiMouseX, uiMouseY, button)) {
            return true;
         }

         int index = (int)((uiMouseY - this.listSpacer.y() + (this.listView == null ? 0 : this.listView.getScrollOffset())) / 14.0F);
         if (index >= 0 && index < this.filteredOptions.size()) {
            RaceStepSelectorOverlay.Inner_DataUtil selected = this.filteredOptions.get(index);
            if (this.onSelect != null) {
               this.onSelect.accept(selected);
            }

            this.dismiss();
            return true;
         }
      }

      if (!this.init(this.add, this.clear, this.reset, this.start, uiMouseX, uiMouseY)) {
         this.layoutHost.clearFocus();
         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (button == 0 && this.sync_Z) {
         this.sync_Z = false;
         if (!this.flush_Z
            && Math.abs(uiMouseX - this.pressX) < 3.0F
            && Math.abs(uiMouseY - this.pressY) < 3.0F
            && this.isOverTitleBar(uiMouseX, uiMouseY)
            && !this.load_Z_F_F(uiMouseX, uiMouseY)) {
            this.setCollapsed(!this.update_Z);
         }

         this.flush_Z = false;
         return true;
      } else {
         if (button == 0 && this.listView != null) {
            this.listView.mouseReleased();
         }

         return !this.update_Z && this.layoutHost.mouseReleased(mouseX, mouseY, button) ? true : this.visible;
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (!this.visible) {
         return false;
      }

      LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      if (this.sync_Z && button == 0) {
         if (!this.flush_Z && Math.abs(uiMouseX - this.pressX) < 3.0F && Math.abs(uiMouseY - this.pressY) < 3.0F) {
            return true;
         }

         this.flush_Z = true;
         int nextX = Math.round(uiMouseX - this.dragOffsetX);
         int nextY = Math.round(uiMouseY - this.dragOffsetY);
         if (nextX != this.add || nextY != this.clear) {
            this.flush_Z = true;
         }

         OverlayBounds clamped = this.clampBounds(new OverlayBounds(nextX, nextY, this.reset, this.start, this.visible, this.update_Z));
         this.add = clamped.x;
         this.clear = clamped.y;
         return true;
      } else if (!this.update_Z && this.listView != null && this.listView.isDraggingScrollbar()) {
         this.listView.mouseDragged(uiMouseX, uiMouseY);
         return true;
      } else {
         return !this.update_Z && this.layoutHost.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.visible && !this.update_Z && this.listView != null) {
         LayoutViewport viewport = this.layoutHost.createViewport();
         float uiMouseX = viewport.toUiX(mouseX);
         float uiMouseY = viewport.toUiY(mouseY);
         if (this.init(
            this.listSpacer.x(),
            this.listSpacer.y(),
            this.listSpacer.width(),
            this.listSpacer.height(),
            uiMouseX,
            uiMouseY
         )) {
            this.listView.mouseScrolled(uiMouseX, uiMouseY, amount);
            return true;
         } else {
            return this.visible;
         }
      } else {
         return this.visible;
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.visible) {
         return false;
      } else if (keyCode == 256) {
         this.dismiss();
         return true;
      } else {
         return this.update_Z ? false : this.layoutHost.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return this.visible && !this.update_Z && this.layoutHost.charTyped(chr, modifiers);
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      int renderedHeight = this.update_Z ? this.styleTheme.load() : this.start;
      return this.init(this.add, this.clear, this.reset, renderedHeight, uiMouseX, uiMouseY);
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mouseX);
      float uiMouseY = viewport.toUiY(mouseY);
      return this.isOverTitleBar(uiMouseX, uiMouseY) && !this.load_Z_F_F(uiMouseX, uiMouseY);
   }

   @Override
   public boolean isCollapsed() {
      return this.update_Z;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.update_Z != collapsed) {
         this.update_Z = collapsed;
         this.sync_Z = false;
         this.flush_Z = false;
         this.windowNode.setShowBody(!collapsed);
         if (collapsed) {
            this.clearFocusAndDismissMenu();
         }

         this.saveBounds();
      }
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.add, this.clear, this.reset, this.start, this.visible, this.update_Z);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampBounds(bounds);
         this.add = clamped.x;
         this.clear = clamped.y;
         this.reset = clamped.width;
         this.start = clamped.height;
         this.visible = clamped.visible;
         this.update_Z = clamped.collapsed;
         this.windowNode.setShowBody(!this.update_Z);
      }
   }

   @Override
   public void clearInputFocus() {
      this.layoutHost.clearFocus();
   }

   @Override
   public int getMinWidth() {
      return 160;
   }

   @Override
   public int getMinHeight() {
      return this.styleTheme.load() + 20;
   }

   private boolean isOverTitleBar(float x, float y) {
      return x >= this.add && x < this.add + this.reset && y >= this.clear && y < this.clear + this.styleTheme.load();
   }

   private boolean load_Z_F_F(float x, float y) {
      return WindowHeaderHelper.init(
         NetHelper.init(this.add, this.clear, this.reset, Math.max(this.styleTheme.load(), this.start)), this.styleTheme.load(), x, y
      );
   }

   private boolean init(float x, float y, float w, float h, float mx, float my) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   private OverlayBounds clampBounds(OverlayBounds layout) {
      LayoutViewport viewport = this.layoutHost.createViewport();
      int margin = 4;
      int viewportW = Math.round(viewport.width());
      int viewportH = Math.round(viewport.height());
      int availableW = Math.max(1, viewportW - margin * 2);
      int availableH = Math.max(this.styleTheme.load(), viewportH - margin * 2);
      int width = Math.max(Math.min(160, availableW), Math.min(layout.width, availableW));
      int minHeight = layout.collapsed ? this.styleTheme.load() : this.styleTheme.load() + 20;
      int height = Math.max(Math.min(minHeight, availableH), Math.min(layout.height, availableH));
      int renderedHeight = layout.collapsed ? this.styleTheme.load() : height;
      int x = Math.max(margin, Math.min(layout.x, Math.max(margin, viewportW - margin - width)));
      int y = Math.max(margin, Math.min(layout.y, Math.max(margin, viewportH - margin - renderedHeight)));
      return new OverlayBounds(x, y, width, height, layout.visible, layout.collapsed);
   }

   public record Inner_DataUtil(String init, String load, String read, String write) {
   }
}
