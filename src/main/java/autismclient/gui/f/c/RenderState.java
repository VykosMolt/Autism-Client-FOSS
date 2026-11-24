package autismclient.gui.f.c;

import autismclient.gui.f.b.j;
import autismclient.util.cs;
import autismclient.util.p;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class RenderState<T> {
   private final EventBus init_EventBus;
   private final Function<T, String> init_Function;
   private final BiConsumer<T, Boolean> init_BiConsumer;
   private final List<T> init_List = new ArrayList<>();
   private int init_I = -1;
   private boolean init_Z = true;
   private int load_I = p.update();
   private int read_I = p.count();
   private int write_I = p.table();
   private int update = -1;
   private int sync = p.bind();
   private Function<T, String> load_Function;
   private Function<T, Integer> read_Function;
   private Function<T, String> write_Function;
   private int flush = p.bind();

   public RenderState(
      int x, int y, int width, int height, int rowHeight, int scrollbarWidth, Function<T, String> labelExtractor, BiConsumer<T, Boolean> selectionHandler
   ) {
      this.init_EventBus = new EventBus(x, y, width, height, rowHeight, scrollbarWidth);
      this.init_Function = labelExtractor;
      this.init_BiConsumer = selectionHandler;
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
      int contentHeight = this.init_List.size() * this.init_EventBus.flush();
      this.init_EventBus.init_V(contentHeight);
   }

   public void init_V(int index) {
      if (index >= -1 && index < this.init_List.size()) {
         this.init_I = index;
         if (index >= 0 && this.init_BiConsumer != null) {
            this.init_BiConsumer.accept(this.init_List.get(index), true);
         }
      }
   }

   public int init_I() {
      return this.init_I;
   }

   public T init_Obj() {
      return this.init_I >= 0 && this.init_I < this.init_List.size() ? this.init_List.get(this.init_I) : null;
   }

   public void init(boolean enabled) {
      this.init_Z = enabled;
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public void init(Function<T, String> extractor, Function<T, Integer> colorExtractor) {
      this.load_Function = extractor;
      this.read_Function = colorExtractor;
   }

   public void init(Function<T, String> extractor) {
      this.write_Function = extractor;
   }

   public void init_V_I(int border, int background, int selectedText, int normalText, int disabledText) {
      this.load_I = border;
      this.read_I = background;
      this.write_I = selectedText;
      this.update = normalText;
      this.sync = disabledText;
   }

   public void init(GuiGraphicsExtractor ctx, Font textRenderer, double mouseX, double mouseY) {
      this.init_EventBus.init_V_GuiGraphicsExtract_2(ctx, this.load_I, this.read_I);

      try {
         this.load(ctx, textRenderer, mouseX, mouseY);
      } finally {
         this.init_EventBus.init(ctx);
      }

      this.init_EventBus.init_V_GuiGraphicsExtract(ctx, mouseX, mouseY);
   }

   public void init_V_GuiGraphicsExtract(
      GuiGraphicsExtractor ctx, Font textRenderer, double mouseX, double mouseY, RenderState.Inner_DataUtil<T> customRenderer
   ) {
      this.init_EventBus.init_V_GuiGraphicsExtract_2(ctx, this.load_I, this.read_I);

      try {
         this.init_EventBus.init_V_GuiGraphicsExtract_3(ctx, this.init_List.size(), (index, bounds) -> {
            T item = this.init_List.get(index);
            boolean isSelected = index == this.init_I;
            boolean isHovered = this.init_Z && this.init(mouseX, mouseY, bounds.init, bounds.load, bounds.read, bounds.write);
            int originalY = this.init_EventBus.init_I(index);
            customRenderer.init(ctx, textRenderer, item, index, isSelected, isHovered, bounds.init, bounds.load, bounds.read, bounds.write, originalY);
         });
      } finally {
         this.init_EventBus.init(ctx);
      }

      this.init_EventBus.init_V_GuiGraphicsExtract(ctx, mouseX, mouseY);
   }

   private void load(GuiGraphicsExtractor ctx, Font textRenderer, double mouseX, double mouseY) {
      this.init_EventBus.init_V_GuiGraphicsExtract_3(ctx, this.init_List.size(), (index, bounds) -> {
         T item = this.init_List.get(index);
         boolean isSelected = index == this.init_I;
         boolean isHovered = this.init_Z && this.init(mouseX, mouseY, bounds.init, bounds.load, bounds.read, bounds.write);
         autismclient.gui.f.b.TableIndex.init(ctx, bounds.init, bounds.load, bounds.read, bounds.write, isHovered, isSelected);
         int textColor;
         if (!this.init_Z) {
            textColor = this.sync;
         } else if (isSelected) {
            textColor = this.write_I;
         } else {
            textColor = this.update;
         }

         int availableWidth = bounds.read - 12;
         int textX = bounds.init + 6;
         int textY = j.init(bounds.load, bounds.write, 9, 0);
         String label = this.init_Function.apply(item);
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
            String trimmedLabel = cs.init(textRenderer, label, labelMaxWidth, cs.Inner_DataUtil.read);
            cs.init(ctx, textRenderer, trimmedLabel, textColor, textX, textY, false);
            if (badge != null && !badge.isEmpty()) {
               int labelWidth = cs.init(textRenderer, trimmedLabel, cs.Inner_DataUtil.read);
               int badgeX = textX + labelWidth + 4;
               int badgeColor = this.read_Function != null ? this.read_Function.apply(item) : -22016;
               cs.init(ctx, textRenderer, badge, badgeColor, badgeX, textY, false);
            }

            if (secondary != null && !secondary.isEmpty()) {
               int secondaryWidth = textRenderer.width(secondary);
               int secondaryX = bounds.init + bounds.read - secondaryWidth - 6;
               cs.init(ctx, textRenderer, secondary, this.flush, secondaryX, textY, false);
            }
         }
      });
   }

   public boolean init_Z_D_2(double mouseX, double mouseY, int button) {
      if (!this.init_Z) {
         return false;
      }

      if (this.init_EventBus.init_Z_D_2(mouseX, mouseY, button)) {
         return true;
      }

      if (this.init_EventBus.init_Z_D_D(mouseX, mouseY)) {
         int relativeY = (int)Math.round(mouseY) - this.init_EventBus.load_I() - 1 + this.init_EventBus.open();
         int rowIndex = relativeY / this.init_EventBus.flush();
         if (rowIndex >= 0 && rowIndex < this.init_List.size()) {
            this.init_V(rowIndex);
            return true;
         }
      }

      return false;
   }

   public void load_V() {
      this.init_EventBus.read_V();
   }

   public void init_V(double mouseX, double mouseY) {
      this.init_EventBus.init_V_D_D(mouseX, mouseY);
   }

   public boolean init_Z_D(double mouseX, double mouseY, double amount) {
      return this.init_EventBus.init_Z_D(mouseX, mouseY, amount);
   }

   public void read_V() {
      if (this.init_I >= 0) {
         int rowTop = this.init_I * this.init_EventBus.flush();
         int rowBottom = rowTop + this.init_EventBus.flush();
         int viewTop = this.init_EventBus.open();
         int viewHeight = this.init_EventBus.write_I() - 2;
         int viewBottom = viewTop + viewHeight;
         if (rowTop < viewTop) {
            this.init_EventBus.load_V(rowTop);
         } else if (rowBottom > viewBottom) {
            this.init_EventBus.load_V(rowBottom - viewHeight);
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

   public int read_I() {
      return this.init_EventBus.init_I();
   }

   public int write_I() {
      return this.init_EventBus.load_I();
   }

   public int update() {
      return this.init_EventBus.read_I();
   }

   public int sync() {
      return this.init_EventBus.write_I();
   }

   public int flush() {
      return this.init_EventBus.update();
   }

   public int close() {
      return this.init_EventBus.flush();
   }

   public int open() {
      return this.init_EventBus.open();
   }

   public void load(int offset) {
      this.init_EventBus.load_V(offset);
   }

   public void read(int rows) {
      this.init_EventBus.read_V(rows);
   }

   public boolean init_Z(double x, double y) {
      return this.init_EventBus.init_Z_D_D(x, y);
   }

   public boolean read_Z() {
      return this.init_EventBus.init_Z();
   }

   public boolean load(double mouseX, double mouseY) {
      return this.init_EventBus.load(mouseX, mouseY);
   }

   @FunctionalInterface
   public interface Inner_DataUtil<T> {
      void init(GuiGraphicsExtractor var1, Font var2, T var3, int var4, boolean var5, boolean var6, int var7, int var8, int var9, int var10, int var11);
   }
}
