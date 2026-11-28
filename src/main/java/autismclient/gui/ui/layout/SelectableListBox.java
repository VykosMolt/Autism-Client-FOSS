package autismclient.gui.ui.layout;

import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.util.ThemedTextHelper;
import autismclient.util.UiColorPalette;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SelectableListBox<T> {
   private final ScrollableListView init_EventBus;
   private final Function<T, String> labelExtractor;
   private final BiConsumer<T, Boolean> selectionHandler;
   private final List<T> init_List = new ArrayList<>();
   private int init_I = -1;
   private boolean enabled = true;
   private int border = UiColorPalette.inputSurface();
   private int background = UiColorPalette.listBoxSurface();
   private int selectedText = UiColorPalette.selectedText();
   private int update = -1;
   private int sync = UiColorPalette.textSecondary();
   private Function<T, String> load_Function;
   private Function<T, Integer> colorExtractor;
   private Function<T, String> write_Function;
   private int flush = UiColorPalette.textSecondary();

   public SelectableListBox(
      int x, int y, int width, int height, int rowHeight, int scrollbarWidth, Function<T, String> labelExtractor, BiConsumer<T, Boolean> selectionHandler
   ) {
      this.init_EventBus = new ScrollableListView(x, y, width, height, rowHeight, scrollbarWidth);
      this.labelExtractor = labelExtractor;
      this.selectionHandler = selectionHandler;
   }

   public void init(List<T> items) {
      this.init_List.clear();
      if (items != null) {
         this.init_List.addAll(items);
      }

      this.write_V();
   }

   public void init(T[] items) {
      this.init_List.clear();
      if (items != null) {
         for (T item : items) {
            this.init_List.add(item);
         }
      }

      this.write_V();
   }

   public void init(T item) {
      this.init_List.add(item);
      this.write_V();
   }

   public void init_V() {
      this.init_List.clear();
      this.init_I = -1;
      this.write_V();
   }

   private void write_V() {
      int contentHeight = this.init_List.size() * this.init_EventBus.getRowHeight();
      this.init_EventBus.setContentHeight(contentHeight);
   }

   public void select(int index) {
      if (index >= -1 && index < this.init_List.size()) {
         this.init_I = index;
         if (index >= 0 && this.selectionHandler != null) {
            this.selectionHandler.accept(this.init_List.get(index), true);
         }
      }
   }

   public int init_I() {
      return this.init_I;
   }

   public T selected() {
      return this.init_I >= 0 && this.init_I < this.init_List.size() ? this.init_List.get(this.init_I) : null;
   }

   public void init(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void init(Function<T, String> extractor, Function<T, Integer> colorExtractor) {
      this.load_Function = extractor;
      this.colorExtractor = colorExtractor;
   }

   public void init(Function<T, String> extractor) {
      this.write_Function = extractor;
   }

   public void init_V_I(int border, int background, int selectedText, int normalText, int disabledText) {
      this.border = border;
      this.background = background;
      this.selectedText = selectedText;
      this.update = normalText;
      this.sync = disabledText;
   }

   public void init(GuiGraphicsExtractor ctx, Font textRenderer, double mouseX, double mouseY) {
      this.init_EventBus.beginFrame(ctx, this.border, this.background);

      try {
         this.load(ctx, textRenderer, mouseX, mouseY);
      } finally {
         this.init_EventBus.endFrame(ctx);
      }

      this.init_EventBus.renderScrollbar(ctx, mouseX, mouseY);
   }

   public void init_V_GuiGraphicsExtract(
      GuiGraphicsExtractor ctx, Font textRenderer, double mouseX, double mouseY, SelectableListBox.RowRenderer<T> customRenderer
   ) {
      this.init_EventBus.beginFrame(ctx, this.border, this.background);

      try {
         this.init_EventBus.forEachVisibleRow(ctx, this.init_List.size(), (index, bounds) -> {
            T item = this.init_List.get(index);
            boolean isSelected = index == this.init_I;
            boolean isHovered = this.enabled && this.init(mouseX, mouseY, bounds.x, bounds.y, bounds.width, bounds.height);
            int originalY = this.init_EventBus.getRowY(index);
            customRenderer.init(ctx, textRenderer, item, index, isSelected, isHovered, bounds.x, bounds.y, bounds.width, bounds.height, originalY);
         });
      } finally {
         this.init_EventBus.endFrame(ctx);
      }

      this.init_EventBus.renderScrollbar(ctx, mouseX, mouseY);
   }

   private void load(GuiGraphicsExtractor ctx, Font textRenderer, double mouseX, double mouseY) {
      this.init_EventBus.forEachVisibleRow(ctx, this.init_List.size(), (index, bounds) -> {
         T item = this.init_List.get(index);
         boolean isSelected = index == this.init_I;
         boolean isHovered = this.enabled && this.init(mouseX, mouseY, bounds.x, bounds.y, bounds.width, bounds.height);
         autismclient.gui.ui.widgets.PanelSurfaceHelper.drawCardRow(ctx, bounds.x, bounds.y, bounds.width, bounds.height, isHovered, isSelected);
         int textColor;
         if (!this.enabled) {
            textColor = this.sync;
         } else if (isSelected) {
            textColor = this.selectedText;
         } else {
            textColor = this.update;
         }

         int availableWidth = bounds.width - 12;
         int textX = bounds.x + 6;
         int textY = UiLayoutMath.centerTextY(bounds.y, bounds.height, 9, 0);
         String label = this.labelExtractor.apply(item);
         if (label != null && !label.isEmpty()) {
            int reservedWidth = 0;
            String badge = this.load_Function != null ? this.load_Function.apply(item) : null;
            String secondary = this.write_Function != null ? this.write_Function.apply(item) : null;
            if (badge != null && !badge.isEmpty()) {
               reservedWidth += textRenderer.width(badge) + 8;
            }

            if (secondary != null && !secondary.isEmpty()) {
               reservedWidth += textRenderer.width(secondary) + 8;
            }

            int labelMaxWidth = Math.max(20, availableWidth - reservedWidth);
            String trimmedLabel = ThemedTextHelper.init(textRenderer, label, labelMaxWidth, ThemedTextHelper.TextTone.BODY);
            ThemedTextHelper.init(ctx, textRenderer, trimmedLabel, textColor, textX, textY, false);
            if (badge != null && !badge.isEmpty()) {
               int labelWidth = ThemedTextHelper.init(textRenderer, trimmedLabel, ThemedTextHelper.TextTone.BODY);
               int badgeX = textX + labelWidth + 4;
               int badgeColor = this.colorExtractor != null ? this.colorExtractor.apply(item) : -22016;
               ThemedTextHelper.init(ctx, textRenderer, badge, badgeColor, badgeX, textY, false);
            }

            if (secondary != null && !secondary.isEmpty()) {
               int secondaryWidth = textRenderer.width(secondary);
               int secondaryX = bounds.x + bounds.width - secondaryWidth - 6;
               ThemedTextHelper.init(ctx, textRenderer, secondary, this.flush, secondaryX, textY, false);
            }
         }
      });
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.enabled) {
         return false;
      }

      if (this.init_EventBus.mouseClicked(mouseX, mouseY, button)) {
         return true;
      }

      if (this.init_EventBus.containsPoint(mouseX, mouseY)) {
         int relativeY = (int)Math.round(mouseY) - this.init_EventBus.getY() - 1 + this.init_EventBus.getScrollOffset();
         int rowIndex = relativeY / this.init_EventBus.getRowHeight();
         if (rowIndex >= 0 && rowIndex < this.init_List.size()) {
            this.select(rowIndex);
            return true;
         }
      }

      return false;
   }

   public void mouseReleased() {
      this.init_EventBus.mouseReleased();
   }

   public void mouseDragged(double mouseX, double mouseY) {
      this.init_EventBus.mouseDragged(mouseX, mouseY);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.init_EventBus.mouseScrolled(mouseX, mouseY, amount);
   }

   public void read_V() {
      if (this.init_I >= 0) {
         int rowTop = this.init_I * this.init_EventBus.getRowHeight();
         int rowBottom = rowTop + this.init_EventBus.getRowHeight();
         int viewTop = this.init_EventBus.getScrollOffset();
         int viewHeight = this.init_EventBus.getHeight() - 2;
         int viewBottom = viewTop + viewHeight;
         if (rowTop < viewTop) {
            this.init_EventBus.setScrollOffset(rowTop);
         } else if (rowBottom > viewBottom) {
            this.init_EventBus.setScrollOffset(rowBottom - viewHeight);
         }
      }
   }

   public T init_Obj(int index) {
      return index >= 0 && index < this.init_List.size() ? this.init_List.get(index) : null;
   }

   public int load_I() {
      return this.init_List.size();
   }

   public boolean load_Z() {
      return this.init_List.isEmpty();
   }

   private boolean init(double mouseX, double mouseY, int x, int y, int width, int height) {
      return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
   }

   public int getX() {
      return this.init_EventBus.getX();
   }

   public int getY() {
      return this.init_EventBus.getY();
   }

   public int update() {
      return this.init_EventBus.getWidth();
   }

   public int sync() {
      return this.init_EventBus.getHeight();
   }

   public int flush() {
      return this.init_EventBus.getRowWidthWithScrollbar();
   }

   public int close() {
      return this.init_EventBus.getRowHeight();
   }

   public int open() {
      return this.init_EventBus.getScrollOffset();
   }

   public void load(int offset) {
      this.init_EventBus.setScrollOffset(offset);
   }

   public void read(int rows) {
      this.init_EventBus.scrollByRows(rows);
   }

   public boolean containsPoint(double x, double y) {
      return this.init_EventBus.containsPoint(x, y);
   }

   public boolean isDraggingScrollbar() {
      return this.init_EventBus.isDraggingScrollbar();
   }

   public boolean load(double mouseX, double mouseY) {
      return this.init_EventBus.isOverScrollbar(mouseX, mouseY);
   }

   @FunctionalInterface
   public interface RowRenderer<T> {
      void init(GuiGraphicsExtractor var1, Font var2, T var3, int var4, boolean var5, boolean var6, int var7, int var8, int var9, int var10, int var11);
   }
}
