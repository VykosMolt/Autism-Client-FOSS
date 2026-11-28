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
   private final ScrollableListView listView;
   private final Function<T, String> labelExtractor;
   private final BiConsumer<T, Boolean> selectionHandler;
   private final List<T> items = new ArrayList<>();
   private int selectedIndex = -1;
   private boolean enabled = true;
   private int border = UiColorPalette.inputSurface();
   private int background = UiColorPalette.listBoxSurface();
   private int selectedText = UiColorPalette.selectedText();
   private int normalTextColor = -1;
   private int disabledTextColor = UiColorPalette.textSecondary();
   private Function<T, String> badgeExtractor;
   private Function<T, Integer> colorExtractor;
   private Function<T, String> secondaryExtractor;
   private int secondaryTextColor = UiColorPalette.textSecondary();

   public SelectableListBox(
      int x, int y, int width, int height, int rowHeight, int scrollbarWidth, Function<T, String> labelExtractor, BiConsumer<T, Boolean> selectionHandler
   ) {
      this.listView = new ScrollableListView(x, y, width, height, rowHeight, scrollbarWidth);
      this.labelExtractor = labelExtractor;
      this.selectionHandler = selectionHandler;
   }

   public void setItems(List<T> items) {
      this.items.clear();
      if (items != null) {
         this.items.addAll(items);
      }

      this.updateContentHeight();
   }

   public void setItemsArray(T[] items) {
      this.items.clear();
      if (items != null) {
         for (T item : items) {
            this.items.add(item);
         }
      }

      this.updateContentHeight();
   }

   public void appendItem(T item) {
      this.items.add(item);
      this.updateContentHeight();
   }

   public void clear() {
      this.items.clear();
      this.selectedIndex = -1;
      this.updateContentHeight();
   }

   private void updateContentHeight() {
      int contentHeight = this.items.size() * this.listView.getRowHeight();
      this.listView.setContentHeight(contentHeight);
   }

   public void select(int index) {
      if (index >= -1 && index < this.items.size()) {
         this.selectedIndex = index;
         if (index >= 0 && this.selectionHandler != null) {
            this.selectionHandler.accept(this.items.get(index), true);
         }
      }
   }

   public int getSelectedIndex() {
      return this.selectedIndex;
   }

   public T selected() {
      return this.selectedIndex >= 0 && this.selectedIndex < this.items.size() ? this.items.get(this.selectedIndex) : null;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setBadge(Function<T, String> extractor, Function<T, Integer> colorExtractor) {
      this.badgeExtractor = extractor;
      this.colorExtractor = colorExtractor;
   }

   public void addItem(Function<T, String> extractor) {
      this.secondaryExtractor = extractor;
   }

   public void setColors(int border, int background, int selectedText, int normalText, int disabledText) {
      this.border = border;
      this.background = background;
      this.selectedText = selectedText;
      this.normalTextColor = normalText;
      this.disabledTextColor = disabledText;
   }

   public void render(GuiGraphicsExtractor ctx, Font textRenderer, double mouseX, double mouseY) {
      this.listView.beginFrame(ctx, this.border, this.background);

      try {
         this.renderRows(ctx, textRenderer, mouseX, mouseY);
      } finally {
         this.listView.endFrame(ctx);
      }

      this.listView.renderScrollbar(ctx, mouseX, mouseY);
   }

   public void renderWith(
      GuiGraphicsExtractor ctx, Font textRenderer, double mouseX, double mouseY, SelectableListBox.RowRenderer<T> customRenderer
   ) {
      this.listView.beginFrame(ctx, this.border, this.background);

      try {
         this.listView.forEachVisibleRow(ctx, this.items.size(), (index, bounds) -> {
            T item = this.items.get(index);
            boolean isSelected = index == this.selectedIndex;
            boolean isHovered = this.enabled && this.isInside(mouseX, mouseY, bounds.x, bounds.y, bounds.width, bounds.height);
            int originalY = this.listView.getRowY(index);
            customRenderer.renderRow(ctx, textRenderer, item, index, isSelected, isHovered, bounds.x, bounds.y, bounds.width, bounds.height, originalY);
         });
      } finally {
         this.listView.endFrame(ctx);
      }

      this.listView.renderScrollbar(ctx, mouseX, mouseY);
   }

   private void renderRows(GuiGraphicsExtractor ctx, Font textRenderer, double mouseX, double mouseY) {
      this.listView.forEachVisibleRow(ctx, this.items.size(), (index, bounds) -> {
         T item = this.items.get(index);
         boolean isSelected = index == this.selectedIndex;
         boolean isHovered = this.enabled && this.isInside(mouseX, mouseY, bounds.x, bounds.y, bounds.width, bounds.height);
         autismclient.gui.ui.widgets.PanelSurfaceHelper.drawCardRow(ctx, bounds.x, bounds.y, bounds.width, bounds.height, isHovered, isSelected);
         int textColor;
         if (!this.enabled) {
            textColor = this.disabledTextColor;
         } else if (isSelected) {
            textColor = this.selectedText;
         } else {
            textColor = this.normalTextColor;
         }

         int availableWidth = bounds.width - 12;
         int textX = bounds.x + 6;
         int textY = UiLayoutMath.centerTextY(bounds.y, bounds.height, 9, 0);
         String label = this.labelExtractor.apply(item);
         if (label != null && !label.isEmpty()) {
            int reservedWidth = 0;
            String badge = this.badgeExtractor != null ? this.badgeExtractor.apply(item) : null;
            String secondary = this.secondaryExtractor != null ? this.secondaryExtractor.apply(item) : null;
            if (badge != null && !badge.isEmpty()) {
               reservedWidth += textRenderer.width(badge) + 8;
            }

            if (secondary != null && !secondary.isEmpty()) {
               reservedWidth += textRenderer.width(secondary) + 8;
            }

            int labelMaxWidth = Math.max(20, availableWidth - reservedWidth);
            String trimmedLabel = ThemedTextHelper.ellipsize(textRenderer, label, labelMaxWidth, ThemedTextHelper.TextTone.BODY);
            ThemedTextHelper.drawColor(ctx, textRenderer, trimmedLabel, textColor, textX, textY, false);
            if (badge != null && !badge.isEmpty()) {
               int labelWidth = ThemedTextHelper.width(textRenderer, trimmedLabel, ThemedTextHelper.TextTone.BODY);
               int badgeX = textX + labelWidth + 4;
               int badgeColor = this.colorExtractor != null ? this.colorExtractor.apply(item) : -22016;
               ThemedTextHelper.drawColor(ctx, textRenderer, badge, badgeColor, badgeX, textY, false);
            }

            if (secondary != null && !secondary.isEmpty()) {
               int secondaryWidth = textRenderer.width(secondary);
               int secondaryX = bounds.x + bounds.width - secondaryWidth - 6;
               ThemedTextHelper.drawColor(ctx, textRenderer, secondary, this.secondaryTextColor, secondaryX, textY, false);
            }
         }
      });
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.enabled) {
         return false;
      }

      if (this.listView.mouseClicked(mouseX, mouseY, button)) {
         return true;
      }

      if (this.listView.containsPoint(mouseX, mouseY)) {
         int relativeY = (int)Math.round(mouseY) - this.listView.getY() - 1 + this.listView.getScrollOffset();
         int rowIndex = relativeY / this.listView.getRowHeight();
         if (rowIndex >= 0 && rowIndex < this.items.size()) {
            this.select(rowIndex);
            return true;
         }
      }

      return false;
   }

   public void mouseReleased() {
      this.listView.mouseReleased();
   }

   public void mouseDragged(double mouseX, double mouseY) {
      this.listView.mouseDragged(mouseX, mouseY);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.listView.mouseScrolled(mouseX, mouseY, amount);
   }

   public void ensureSelectedVisible() {
      if (this.selectedIndex >= 0) {
         int rowTop = this.selectedIndex * this.listView.getRowHeight();
         int rowBottom = rowTop + this.listView.getRowHeight();
         int viewTop = this.listView.getScrollOffset();
         int viewHeight = this.listView.getHeight() - 2;
         int viewBottom = viewTop + viewHeight;
         if (rowTop < viewTop) {
            this.listView.setScrollOffset(rowTop);
         } else if (rowBottom > viewBottom) {
            this.listView.setScrollOffset(rowBottom - viewHeight);
         }
      }
   }

   public T getItem(int index) {
      return index >= 0 && index < this.items.size() ? this.items.get(index) : null;
   }

   public int size() {
      return this.items.size();
   }

   public boolean isEmpty() {
      return this.items.isEmpty();
   }

   private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
      return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
   }

   public int getX() {
      return this.listView.getX();
   }

   public int getY() {
      return this.listView.getY();
   }

   public int width() {
      return this.listView.getWidth();
   }

   public int height() {
      return this.listView.getHeight();
   }

   public int getRowWidth() {
      return this.listView.getRowWidthWithScrollbar();
   }

   public int getRowHeight() {
      return this.listView.getRowHeight();
   }

   public int getScrollOffset() {
      return this.listView.getScrollOffset();
   }

   public void setScrollOffset(int offset) {
      this.listView.setScrollOffset(offset);
   }

   public void scrollByRows(int rows) {
      this.listView.scrollByRows(rows);
   }

   public boolean containsPoint(double x, double y) {
      return this.listView.containsPoint(x, y);
   }

   public boolean isDraggingScrollbar() {
      return this.listView.isDraggingScrollbar();
   }

   public boolean isOverScrollbar(double mouseX, double mouseY) {
      return this.listView.isOverScrollbar(mouseX, mouseY);
   }

   @FunctionalInterface
   public interface RowRenderer<T> {
      void renderRow(GuiGraphicsExtractor var1, Font var2, T var3, int var4, boolean var5, boolean var6, int var7, int var8, int var9, int var10, int var11);
   }
}
