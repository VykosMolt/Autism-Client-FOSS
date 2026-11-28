package autismclient.gui.screens;

import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.l;
import autismclient.util.DisplayItemStackFactory;
import autismclient.util.cA;
import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemedTextInputBox;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class PickItemScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int MAX_PANEL_WIDTH = 300;
   private static final int load_I = 4;
   private static final int read_I = 3;
   private static final int write = 16;
   private static final int BACKDROP_COLOR = -872086265;
   private static final int ROW_COLOR = -1441722092;
   private static final int ROW_HOVER_COLOR = -1441000940;
   private static final int ROW_SELECTED_COLOR = -1440345836;
   private static final int TEXT_COLOR = -791321;
   private static final int MUTED_TEXT_COLOR = -4743522;
   private static final int SELECTED_TEXT_COLOR = -50373;
   private final Screen parent;
   private final Consumer<String> onPick;
   private final List<PickItemScreen.ItemEntry> allItems;
   private final List<PickItemScreen.RowHit> rowHits = new ArrayList<>();
   private String selectedId;
   private ThemedTextInputBox searchBox;
   private int scrollOffset;
   private boolean init_Z;
   private int scrollGrabOffset;
   private String cachedNeedle;
   private List<PickItemScreen.ItemEntry> cachedMatches = List.of();

   private static int mutedColor() {
      return ThemePaletteManager.recolor(-4743522, ThemePaletteManager.ColorRole.TEXT);
   }

   public PickItemScreen(Screen parent, String selectedId, Consumer<String> onPick) {
      super(Component.literal("Pick Item"));
      this.parent = parent;
      this.selectedId = normalizeId(selectedId);
      this.onPick = onPick == null ? ignored -> {} : onPick;
      this.allItems = this.loadItems();
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         this.rowHits.clear();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -872086265);
         int x = this.panelX();
         int y = this.panelY();
         int panelW = this.panelWidth();
         int panelH = this.panelHeight();
         this.drawPanel(graphics, x, y, panelW, panelH, mx, my);
         if (panelW < 180 || panelH < 100) {
            this.drawText(graphics, "Window too small.", x + 4, this.centeredTextY(y + this.headerHeight(), 12), mutedColor(), Math.max(0, panelW - 8));
            return;
         }

         this.ensureSearchBox(x + 4, this.searchY(), panelW - 8, this.searchHeight());
         this.searchBox.render(graphics, mx, my, delta);
         autismclient.gui.f.NetHelper list = this.listBounds();
         int listX = list.read();
         int listY = list.write();
         int listW = list.update();
         int listH = list.sync();
         autismclient.gui.f.PacketUtil.load(graphics, list, 5, -1442248437, THEME.set());
         autismclient.gui.f.NetHelper clip = autismclient.gui.f.NetHelper.init(listX + 1, listY + 1, Math.max(1, listW - 8), Math.max(1, listH - 2));
         autismclient.gui.ui.UiClipStack.getInstance().push(graphics, clip);

         try {
            this.drawList(graphics, listX + 3, listY + 3, listW - 9, Math.max(1, listH - 6), mx, my);
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(graphics);
         }

         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.scrollbarMetricsFor(listX, listY, listW, listH);
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(graphics, metrics, metrics.containsTrack(mx, my), this.init_Z);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void drawList(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int mx, int my) {
      List<PickItemScreen.ItemEntry> rows = this.filteredItems();
      int rowH = this.rowHeight();
      int maxScroll = Math.max(0, this.contentHeight() - h);
      this.scrollOffset = clamp(this.scrollOffset, 0, maxScroll);
      if (rows.isEmpty()) {
         this.drawText(graphics, "No items matched.", x + 4, this.centeredTextY(y, rowH), mutedColor(), w - 8);
      } else {
         int first = clamp(this.scrollOffset / rowH, 0, Math.max(0, rows.size() - 1));
         int last = clamp((this.scrollOffset + h - 1) / rowH + 1, 0, rows.size() - 1);

         for (int i = first; i <= last; i++) {
            PickItemScreen.ItemEntry entry = rows.get(i);
            int rowY = y - this.scrollOffset + i * rowH;
            if (rowY + rowH > y && rowY < y + h) {
               this.drawRow(graphics, entry, x, rowY, w, mx, my);
            }
         }
      }
   }

   private void drawRow(GuiGraphicsExtractor graphics, PickItemScreen.ItemEntry entry, int x, int y, int w, int mx, int my) {
      boolean selected = entry.id.equals(this.selectedId);
      int rowH = this.rowHeight();
      boolean over = mx >= x && mx < x + w && my >= y && my < y + rowH;
      autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(graphics, x, y, w, rowH - 1, selected ? -1440345836 : (over ? -1441000940 : -1441722092));
      this.drawItem(graphics, entry.stack, x + 2, y + Math.max(0, (rowH - 1 - 16) / 2));
      int textX = x + 2 + 16 + 4;
      int textMax = Math.max(1, w - (textX - x) - 4);
      int textY = this.centeredTextY(y, rowH - 1);
      this.drawText(graphics, entry.label, textX, textY, selected ? -50373 : -791321, textMax);
      if (!entry.id.equals(entry.label)) {
         int labelW = Math.min(textMax, autismclient.gui.f.b.k.init(this.font, entry.label, THEME.init_Identifier_l(l.read), 0));
         int idX = textX + labelW + 4;
         int idMax = textX + textMax - idX;
         if (idMax >= 16) {
            this.drawText(graphics, entry.id, idX, textY, mutedColor(), idMax);
         }
      }

      this.rowHits.add(new PickItemScreen.RowHit(PickItemScreen.HitKind.ITEM, x, y, w, rowH, entry.id));
   }

   private void drawItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
      if (stack != null && !stack.isEmpty()) {
         graphics.item(stack, x, y);
      }
   }

   private void drawPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      int headerH = this.headerHeight();
      autismclient.gui.ui.widgets.DialogFrameRenderer.draw(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my), bounds, headerH, "Pick Item", mx >= x && mx < x + width && my >= y && my < y + headerH
      );
      autismclient.gui.f.NetHelper close = autismclient.gui.ui.widgets.DialogFrameRenderer.headerBounds(bounds, headerH);
      this.rowHits.add(new PickItemScreen.RowHit(PickItemScreen.HitKind.CLOSE, close.read(), close.write(), close.update(), close.sync(), ""));
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      if (this.searchBox != null && this.searchBox.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
         return true;
      }

      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.scrollbarMetrics();
      if (metrics.isScrollable() && metrics.containsTrack(mx, my)) {
         this.init_Z = true;
         this.scrollGrabOffset = metrics.containsThumb(mx, my) ? my - metrics.thumbY() : metrics.thumbHeight() / 2;
         this.scrollOffset = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, my, this.scrollGrabOffset);
         return true;
      }

      for (int i = this.rowHits.size() - 1; i >= 0; i--) {
         PickItemScreen.RowHit hit = this.rowHits.get(i);
         if (hit.contains(mx, my) && (hit.kind != PickItemScreen.HitKind.ITEM || this.listBounds().init_Z(mx, my))) {
            switch (hit.kind) {
               case CLOSE:
                  this.onClose();
                  break;
               case ITEM:
                  this.pick(hit.id);
            }

            return true;
         }
      }

      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.init_Z = false;
      if (this.searchBox != null) {
         this.searchBox.mouseReleased(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
      }

      return true;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.init_Z) {
         this.scrollOffset = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(this.scrollbarMetrics(), my, this.scrollGrabOffset);
         return true;
      } else {
         return this.searchBox != null && this.searchBox.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy) ? true : true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int amount = scrollY < 0.0 ? this.rowHeight() : -this.rowHeight();
      this.scrollOffset = clamp(this.scrollOffset + amount, 0, this.scrollbarMetrics().maxScroll());
      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (Compat263.keyEventGlfwKey(input) == 256) {
         this.onClose();
         return true;
      } else {
         return this.searchBox != null && this.searchBox.keyPressed(input) ? true : true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      if (this.searchBox != null) {
         this.searchBox.charTyped(input);
      }

      return true;
   }

   private void ensureSearchBox(int x, int y, int w, int h) {
      if (this.searchBox == null) {
         this.searchBox = new ThemedTextInputBox(Minecraft.getInstance(), this.font, x, y, w, h, true);
         this.searchBox.setPlaceholder(Component.literal("Search item id or name"));
         this.searchBox.setChangedListener(value -> this.scrollOffset = 0);
         this.searchBox.setSubmitHandler(value -> {
            List<PickItemScreen.ItemEntry> rows = this.filteredItems();
            if (!rows.isEmpty()) {
               this.pick(rows.get(0).id);
            }

            return true;
         });
         this.searchBox.setFocused(true);
      }

      this.searchBox.setX(x);
      this.searchBox.setY(y);
      this.searchBox.setWidth(w);
      this.searchBox.setHeight(h);
   }

   private String searchText() {
      return this.searchBox == null ? "" : this.searchBox.getValue();
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
      }
   }

   private void pick(String id) {
      this.selectedId = normalizeId(id);
      this.onPick.accept(this.selectedId);
      this.onClose();
   }

   private List<PickItemScreen.ItemEntry> filteredItems() {
      String needle = this.searchText().trim().toLowerCase(Locale.ROOT);
      if (needle.equals(this.cachedNeedle)) {
         return this.cachedMatches;
      }

      this.cachedNeedle = needle;
      if (needle.isEmpty()) {
         this.cachedMatches = this.allItems;
         return this.cachedMatches;
      }

      List<PickItemScreen.ItemEntry> out = new ArrayList<>();

      for (PickItemScreen.ItemEntry entry : this.allItems) {
         if (entry.id.contains(needle) || entry.searchKey.contains(needle)) {
            out.add(entry);
         }
      }

      this.cachedMatches = List.copyOf(out);
      return this.cachedMatches;
   }

   private List<PickItemScreen.ItemEntry> loadItems() {
      List<PickItemScreen.ItemEntry> out = new ArrayList<>();
      BuiltInRegistries.ITEM.forEach(item -> {
         if (item != Items.AIR) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null) {
               ItemStack stack = DisplayItemStackFactory.displayStack(item);
               String itemId = id.toString().toLowerCase(Locale.ROOT);
               out.add(new PickItemScreen.ItemEntry(itemId, item.getName(item.getDefaultInstance()).getString(), stack));
            }
         }
      });
      out.sort(Comparator.comparing(PickItemScreen.ItemEntry::searchKey).thenComparing(entry -> entry.id));
      return List.copyOf(out);
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics() {
      autismclient.gui.f.NetHelper list = this.listBounds();
      return this.scrollbarMetricsFor(list.read(), list.write(), list.update(), list.sync());
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetricsFor(int listX, int listY, int listW, int listH) {
      int viewH = Math.max(1, listH - 6);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(this.contentHeight(), viewH, listX + listW - 6, listY + 1, 4, Math.max(1, listH - 2), this.scrollOffset);
   }

   private int headerHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int searchHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(18);
   }

   private int searchY() {
      return this.panelY() + this.headerHeight() + 4;
   }

   private autismclient.gui.f.NetHelper listBounds() {
      int listY = this.searchY() + this.searchHeight() + 2;
      return autismclient.gui.f.NetHelper.init(this.panelX() + 4, listY, this.panelWidth() - 8, this.panelY() + this.panelHeight() - listY - 4);
   }

   private int contentHeight() {
      int size = this.filteredItems().size();
      return size == 0 ? 0 : size * this.rowHeight() + 2;
   }

   private int centeredTextY(int boxY, int boxH) {
      return UiLayoutMath.centerTextY(boxY, boxH, THEME.init_I_l(l.read), THEME.flush());
   }

   private int panelX() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualWidth(), this.panelWidth(), 6);
   }

   private int panelY() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualHeight(), this.panelHeight(), 6);
   }

   private int panelWidth() {
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualWidth(), 6, 300);
   }

   private int panelHeight() {
      int rowH = this.rowHeight();
      int minList = 4 * rowH;
      int maxList = Math.max(minList, (int)(this.virtualHeight() * 0.65F));
      int list = clamp(this.allItems.size() * rowH + 2, minList, maxList);
      int chrome = this.headerHeight() + 4 + this.searchHeight() + 2 + 6 + 4;
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualHeight(), 6, chrome + list);
   }

   private void drawText(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      String display = autismclient.gui.f.b.k.init(
         this.font, text == null ? "" : text, Math.max(0, maxWidth), THEME.init_Identifier_l(l.read), color
      );
      autismclient.gui.f.b.k.init(graphics, this.font, display, THEME.init_Identifier_l(l.read), color, x, y, false);
   }

   private static String normalizeId(String id) {
      return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private enum HitKind {
      CLOSE,
      ITEM;
   }

   private record ItemEntry(String id, String label, String searchKey, ItemStack stack) {
      private ItemEntry(String id, String label, ItemStack icon) {
         this(id, label != null && !label.isBlank() ? label : id, (label == null ? id : label).toLowerCase(Locale.ROOT), icon == null ? ItemStack.EMPTY : icon);
      }
   }

   private record RowHit(PickItemScreen.HitKind kind, int x, int y, int width, int height, String id) {
      boolean contains(int mx, int my) {
         return mx >= this.x && mx < this.x + this.width && my >= this.y && my < this.y + this.height;
      }
   }
}
