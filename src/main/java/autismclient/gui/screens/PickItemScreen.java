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
   private static final int update = -872086265;
   private static final int sync = -1441722092;
   private static final int flush = -1441000940;
   private static final int close = -1440345836;
   private static final int open = -791321;
   private static final int poll = -4743522;
   private static final int push = -50373;
   private final Screen parent;
   private final Consumer<String> onPick;
   private final List<PickItemScreen.Inner_DataUtil> allItems;
   private final List<PickItemScreen.Inner_StreamUtil> rowHits = new ArrayList<>();
   private String selectedId;
   private ThemedTextInputBox searchBox;
   private int pop;
   private boolean init_Z;
   private int tick;
   private String cachedNeedle;
   private List<PickItemScreen.Inner_DataUtil> cachedMatches = List.of();

   private static int read() {
      return ThemePaletteManager.recolor(-4743522, ThemePaletteManager.ColorRole.TEXT);
   }

   public PickItemScreen(Screen parent, String selectedId, Consumer<String> onPick) {
      super(Component.literal("Pick Item"));
      this.parent = parent;
      this.selectedId = load(selectedId);
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
         int x = this.open();
         int y = this.poll();
         int panelW = this.push();
         int panelH = this.pop();
         this.load(graphics, x, y, panelW, panelH, mx, my);
         if (panelW < 180 || panelH < 100) {
            this.init(graphics, "Window too small.", x + 4, this.centeredTextY(y + this.write(), 12), read(), Math.max(0, panelW - 8));
            return;
         }

         this.ensureSearchBox(x + 4, this.flush(), panelW - 8, this.update());
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
            this.init(graphics, listX + 3, listY + 3, listW - 9, Math.max(1, listH - 6), mx, my);
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(graphics);
         }

         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.scrollbarMetricsFor(listX, listY, listW, listH);
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(graphics, metrics, metrics.containsTrack(mx, my), this.init_Z);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int mx, int my) {
      List<PickItemScreen.Inner_DataUtil> rows = this.filteredItems();
      int rowH = this.sync();
      int maxScroll = Math.max(0, this.close() - h);
      this.pop = init(this.pop, 0, maxScroll);
      if (rows.isEmpty()) {
         this.init(graphics, "No items matched.", x + 4, this.centeredTextY(y, rowH), read(), w - 8);
      } else {
         int first = init(this.pop / rowH, 0, Math.max(0, rows.size() - 1));
         int last = init((this.pop + h - 1) / rowH + 1, 0, rows.size() - 1);

         for (int i = first; i <= last; i++) {
            PickItemScreen.Inner_DataUtil entry = rows.get(i);
            int rowY = y - this.pop + i * rowH;
            if (rowY + rowH > y && rowY < y + h) {
               this.init(graphics, entry, x, rowY, w, mx, my);
            }
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, PickItemScreen.Inner_DataUtil entry, int x, int y, int w, int mx, int my) {
      boolean selected = entry.id.equals(this.selectedId);
      int rowH = this.sync();
      boolean over = mx >= x && mx < x + w && my >= y && my < y + rowH;
      autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(graphics, x, y, w, rowH - 1, selected ? -1440345836 : (over ? -1441000940 : -1441722092));
      this.init(graphics, entry.stack, x + 2, y + Math.max(0, (rowH - 1 - 16) / 2));
      int textX = x + 2 + 16 + 4;
      int textMax = Math.max(1, w - (textX - x) - 4);
      int textY = this.centeredTextY(y, rowH - 1);
      this.init(graphics, entry.load, textX, textY, selected ? -50373 : -791321, textMax);
      if (!entry.id.equals(entry.load)) {
         int labelW = Math.min(textMax, autismclient.gui.f.b.k.init(this.font, entry.load, THEME.init_Identifier_l(l.read), 0));
         int idX = textX + labelW + 4;
         int idMax = textX + textMax - idX;
         if (idMax >= 16) {
            this.init(graphics, entry.id, idX, textY, read(), idMax);
         }
      }

      this.rowHits.add(new PickItemScreen.Inner_StreamUtil(PickItemScreen.Inner_CachePool.ITEM, x, y, w, rowH, entry.id));
   }

   private void init(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
      if (stack != null && !stack.isEmpty()) {
         graphics.item(stack, x, y);
      }
   }

   private void load(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      int headerH = this.write();
      autismclient.gui.ui.widgets.DialogFrameRenderer.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my), bounds, headerH, "Pick Item", mx >= x && mx < x + width && my >= y && my < y + headerH
      );
      autismclient.gui.f.NetHelper close = autismclient.gui.ui.widgets.DialogFrameRenderer.init(bounds, headerH);
      this.rowHits.add(new PickItemScreen.Inner_StreamUtil(PickItemScreen.Inner_CachePool.CLOSE, close.read(), close.write(), close.update(), close.sync(), ""));
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
         this.tick = metrics.containsThumb(mx, my) ? my - metrics.thumbY() : metrics.thumbHeight() / 2;
         this.pop = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, my, this.tick);
         return true;
      }

      for (int i = this.rowHits.size() - 1; i >= 0; i--) {
         PickItemScreen.Inner_StreamUtil hit = this.rowHits.get(i);
         if (hit.init(mx, my) && (hit.kind != PickItemScreen.Inner_CachePool.ITEM || this.listBounds().init_Z(mx, my))) {
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
         this.pop = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(this.scrollbarMetrics(), my, this.tick);
         return true;
      } else {
         return this.searchBox != null && this.searchBox.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy) ? true : true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int amount = scrollY < 0.0 ? this.sync() : -this.sync();
      this.pop = init(this.pop + amount, 0, this.scrollbarMetrics().maxScroll());
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
         this.searchBox.setChangedListener(value -> this.pop = 0);
         this.searchBox.setSubmitHandler(value -> {
            List<PickItemScreen.Inner_DataUtil> rows = this.filteredItems();
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
      this.selectedId = load(id);
      this.onPick.accept(this.selectedId);
      this.onClose();
   }

   private List<PickItemScreen.Inner_DataUtil> filteredItems() {
      String needle = this.searchText().trim().toLowerCase(Locale.ROOT);
      if (needle.equals(this.cachedNeedle)) {
         return this.cachedMatches;
      }

      this.cachedNeedle = needle;
      if (needle.isEmpty()) {
         this.cachedMatches = this.allItems;
         return this.cachedMatches;
      }

      List<PickItemScreen.Inner_DataUtil> out = new ArrayList<>();

      for (PickItemScreen.Inner_DataUtil entry : this.allItems) {
         if (entry.id.contains(needle) || entry.read.contains(needle)) {
            out.add(entry);
         }
      }

      this.cachedMatches = List.copyOf(out);
      return this.cachedMatches;
   }

   private List<PickItemScreen.Inner_DataUtil> loadItems() {
      List<PickItemScreen.Inner_DataUtil> out = new ArrayList<>();
      BuiltInRegistries.ITEM.forEach(item -> {
         if (item != Items.AIR) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null) {
               ItemStack stack = DisplayItemStackFactory.displayStack(item);
               String itemId = id.toString().toLowerCase(Locale.ROOT);
               out.add(new PickItemScreen.Inner_DataUtil(itemId, item.getName(item.getDefaultInstance()).getString(), stack));
            }
         }
      });
      out.sort(Comparator.comparing(PickItemScreen.Inner_DataUtil::read).thenComparing(entry -> entry.id));
      return List.copyOf(out);
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics() {
      autismclient.gui.f.NetHelper list = this.listBounds();
      return this.scrollbarMetricsFor(list.read(), list.write(), list.update(), list.sync());
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetricsFor(int listX, int listY, int listW, int listH) {
      int viewH = Math.max(1, listH - 6);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(this.close(), viewH, listX + listW - 6, listY + 1, 4, Math.max(1, listH - 2), this.pop);
   }

   private int write() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int update() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int sync() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(18);
   }

   private int flush() {
      return this.poll() + this.write() + 4;
   }

   private autismclient.gui.f.NetHelper listBounds() {
      int listY = this.flush() + this.update() + 2;
      return autismclient.gui.f.NetHelper.init(this.open() + 4, listY, this.push() - 8, this.poll() + this.pop() - listY - 4);
   }

   private int close() {
      int size = this.filteredItems().size();
      return size == 0 ? 0 : size * this.sync() + 2;
   }

   private int centeredTextY(int boxY, int boxH) {
      return UiLayoutMath.centerTextY(boxY, boxH, THEME.init_I_l(l.read), THEME.flush());
   }

   private int open() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualWidth(), this.push(), 6);
   }

   private int poll() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualHeight(), this.pop(), 6);
   }

   private int push() {
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualWidth(), 6, 300);
   }

   private int pop() {
      int rowH = this.sync();
      int minList = 4 * rowH;
      int maxList = Math.max(minList, (int)(this.virtualHeight() * 0.65F));
      int list = init(this.allItems.size() * rowH + 2, minList, maxList);
      int chrome = this.write() + 4 + this.update() + 2 + 6 + 4;
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualHeight(), 6, chrome + list);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      String display = autismclient.gui.f.b.k.init(
         this.font, text == null ? "" : text, Math.max(0, maxWidth), THEME.init_Identifier_l(l.read), color
      );
      autismclient.gui.f.b.k.init(graphics, this.font, display, THEME.init_Identifier_l(l.read), color, x, y, false);
   }

   private static String load(String id) {
      return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
   }

   private static int init(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private enum Inner_CachePool {
      CLOSE,
      ITEM;
   }

   private record Inner_DataUtil(String id, String load, String read, ItemStack stack) {
      private Inner_DataUtil(String id, String label, ItemStack icon) {
         this(id, label != null && !label.isBlank() ? label : id, (label == null ? id : label).toLowerCase(Locale.ROOT), icon == null ? ItemStack.EMPTY : icon);
      }
   }

   private record Inner_StreamUtil(PickItemScreen.Inner_CachePool kind, int x, int load, int read, int write, String id) {
      boolean init(int mx, int my) {
         return mx >= this.x && mx < this.x + this.read && my >= this.load && my < this.load + this.write;
      }
   }
}
