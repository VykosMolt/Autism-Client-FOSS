package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.UiLayoutMath;
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
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 300;
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
   private final Screen init_Screen;
   private final Consumer<String> init_Consumer;
   private final List<PickItemScreen.Inner_DataUtil> init_List;
   private final List<PickItemScreen.Inner_StreamUtil> load_List = new ArrayList<>();
   private String init_Str;
   private ThemedTextInputBox init_k;
   private int pop;
   private boolean init_Z;
   private int tick;
   private String load_Str;
   private List<PickItemScreen.Inner_DataUtil> read_List = List.of();

   private static int read() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-4743522, ThemePaletteManager.Inner_DataUtil.TEXT);
   }

   public PickItemScreen(Screen parent, String selectedId, Consumer<String> onPick) {
      super(Component.literal("Pick Item"));
      this.init_Screen = parent;
      this.init_Str = load(selectedId);
      this.init_Consumer = onPick == null ? ignored -> {} : onPick;
      this.init_List = this.load_List_7();
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
         this.load_List.clear();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), -872086265);
         int x = this.open();
         int y = this.poll();
         int panelW = this.push();
         int panelH = this.pop();
         this.load(graphics, x, y, panelW, panelH, mx, my);
         if (panelW < 180 || panelH < 100) {
            this.init(graphics, "Window too small.", x + 4, this.init_I_5(y + this.write(), 12), read(), Math.max(0, panelW - 8));
            return;
         }

         this.init_V(x + 4, this.flush(), panelW - 8, this.update());
         this.init_k.init_V(graphics, mx, my, delta);
         autismclient.gui.f.NetHelper list = this.init_NetHelper_5();
         int listX = list.read();
         int listY = list.write();
         int listW = list.update();
         int listH = list.sync();
         autismclient.gui.f.PacketUtil.load(graphics, list, 5, -1442248437, init_CharEncoder.set());
         autismclient.gui.f.NetHelper clip = autismclient.gui.f.NetHelper.init(listX + 1, listY + 1, Math.max(1, listW - 8), Math.max(1, listH - 2));
         autismclient.gui.f.UiClipStack.init_HashUtil().init(graphics, clip);

         try {
            this.init(graphics, listX + 3, listY + 3, listW - 9, Math.max(1, listH - 6), mx, my);
         } finally {
            autismclient.gui.f.UiClipStack.init_HashUtil().init(graphics);
         }

         autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil metrics = this.init_PoolEntry_DataUtil(listX, listY, listW, listH);
         autismclient.gui.f.b.ScrollbarMetricsHelper.init(graphics, metrics, metrics.init(mx, my), this.init_Z);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int mx, int my) {
      List<PickItemScreen.Inner_DataUtil> rows = this.init_List_12();
      int rowH = this.sync();
      int maxScroll = Math.max(0, this.close() - h);
      this.pop = init(this.pop, 0, maxScroll);
      if (rows.isEmpty()) {
         this.init(graphics, "No items matched.", x + 4, this.init_I_5(y, rowH), read(), w - 8);
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
      boolean selected = entry.init_Str.equals(this.init_Str);
      int rowH = this.sync();
      boolean over = mx >= x && mx < x + w && my >= y && my < y + rowH;
      autismclient.gui.f.b.PanelSurfaceHelper.init(graphics, x, y, w, rowH - 1, selected ? -1440345836 : (over ? -1441000940 : -1441722092));
      this.init(graphics, entry.init_ItemStack, x + 2, y + Math.max(0, (rowH - 1 - 16) / 2));
      int textX = x + 2 + 16 + 4;
      int textMax = Math.max(1, w - (textX - x) - 4);
      int textY = this.init_I_5(y, rowH - 1);
      this.init(graphics, entry.load, textX, textY, selected ? -50373 : -791321, textMax);
      if (!entry.init_Str.equals(entry.load)) {
         int labelW = Math.min(textMax, autismclient.gui.f.b.k.init(this.font, entry.load, init_CharEncoder.init_Identifier_l(l.read), 0));
         int idX = textX + labelW + 4;
         int idMax = textX + textMax - idX;
         if (idMax >= 16) {
            this.init(graphics, entry.init_Str, idX, textY, read(), idMax);
         }
      }

      this.load_List.add(new PickItemScreen.Inner_StreamUtil(PickItemScreen.Inner_CachePool.ITEM, x, y, w, rowH, entry.init_Str));
   }

   private void init(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
      if (stack != null && !stack.isEmpty()) {
         graphics.item(stack, x, y);
      }
   }

   private void load(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      int headerH = this.write();
      autismclient.gui.f.b.DialogFrameRenderer.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my), bounds, headerH, "Pick Item", mx >= x && mx < x + width && my >= y && my < y + headerH
      );
      autismclient.gui.f.NetHelper close = autismclient.gui.f.b.DialogFrameRenderer.init(bounds, headerH);
      this.load_List.add(new PickItemScreen.Inner_StreamUtil(PickItemScreen.Inner_CachePool.CLOSE, close.read(), close.write(), close.update(), close.sync(), ""));
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      if (this.init_k != null && this.init_k.init_Z_D_2(mx, my, Compat263.mouseEventGlfwButton(event))) {
         return true;
      }

      autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil metrics = this.init_PoolEntry_DataUtil_7();
      if (metrics.init_Z() && metrics.init(mx, my)) {
         this.init_Z = true;
         this.tick = metrics.load(mx, my) ? my - metrics.update() : metrics.sync() / 2;
         this.pop = autismclient.gui.f.b.ScrollbarMetricsHelper.init(metrics, my, this.tick);
         return true;
      }

      for (int i = this.load_List.size() - 1; i >= 0; i--) {
         PickItemScreen.Inner_StreamUtil hit = this.load_List.get(i);
         if (hit.init(mx, my) && (hit.init_TextFormat_CachePo != PickItemScreen.Inner_CachePool.ITEM || this.init_NetHelper_5().init_Z(mx, my))) {
            switch (hit.init_TextFormat_CachePo) {
               case CLOSE:
                  this.onClose();
                  break;
               case ITEM:
                  this.init_V_Str_9(hit.init_Str);
            }

            return true;
         }
      }

      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.init_Z = false;
      if (this.init_k != null) {
         this.init_k.load(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
      }

      return true;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.init_Z) {
         this.pop = autismclient.gui.f.b.ScrollbarMetricsHelper.init(this.init_PoolEntry_DataUtil_7(), my, this.tick);
         return true;
      } else {
         return this.init_k != null && this.init_k.init(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy) ? true : true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int amount = scrollY < 0.0 ? this.sync() : -this.sync();
      this.pop = init(this.pop + amount, 0, this.init_PoolEntry_DataUtil_7().flush());
      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (Compat263.keyEventGlfwKey(input) == 256) {
         this.onClose();
         return true;
      } else {
         return this.init_k != null && this.init_k.init_Z_KeyEvent(input) ? true : true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      if (this.init_k != null) {
         this.init_k.init_Z_CharacterEvent(input);
      }

      return true;
   }

   private void init_V(int x, int y, int w, int h) {
      if (this.init_k == null) {
         this.init_k = new ThemedTextInputBox(Minecraft.getInstance(), this.font, x, y, w, h, true);
         this.init_k.init_V_Component(Component.literal("Search item id or name"));
         this.init_k.init_V_Consumer(value -> this.pop = 0);
         this.init_k.init_V_Predicate(value -> {
            List<PickItemScreen.Inner_DataUtil> rows = this.init_List_12();
            if (!rows.isEmpty()) {
               this.init_V_Str_9(rows.get(0).init_Str);
            }

            return true;
         });
         this.init_k.push(true);
      }

      this.init_k.read(x);
      this.init_k.write(y);
      this.init_k.update(w);
      this.init_k.sync(h);
   }

   private String init_Str_10() {
      return this.init_k == null ? "" : this.init_k.load_Str();
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private void init_V_Str_9(String id) {
      this.init_Str = load(id);
      this.init_Consumer.accept(this.init_Str);
      this.onClose();
   }

   private List<PickItemScreen.Inner_DataUtil> init_List_12() {
      String needle = this.init_Str_10().trim().toLowerCase(Locale.ROOT);
      if (needle.equals(this.load_Str)) {
         return this.read_List;
      }

      this.load_Str = needle;
      if (needle.isEmpty()) {
         this.read_List = this.init_List;
         return this.read_List;
      }

      List<PickItemScreen.Inner_DataUtil> out = new ArrayList<>();

      for (PickItemScreen.Inner_DataUtil entry : this.init_List) {
         if (entry.init_Str.contains(needle) || entry.read.contains(needle)) {
            out.add(entry);
         }
      }

      this.read_List = List.copyOf(out);
      return this.read_List;
   }

   private List<PickItemScreen.Inner_DataUtil> load_List_7() {
      List<PickItemScreen.Inner_DataUtil> out = new ArrayList<>();
      BuiltInRegistries.ITEM.forEach(item -> {
         if (item != Items.AIR) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null) {
               ItemStack stack = DisplayItemStackFactory.init_ItemStack(item);
               String itemId = id.toString().toLowerCase(Locale.ROOT);
               out.add(new PickItemScreen.Inner_DataUtil(itemId, item.getName(item.getDefaultInstance()).getString(), stack));
            }
         }
      });
      out.sort(Comparator.comparing(PickItemScreen.Inner_DataUtil::read).thenComparing(entry -> entry.init_Str));
      return List.copyOf(out);
   }

   private autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil init_PoolEntry_DataUtil_7() {
      autismclient.gui.f.NetHelper list = this.init_NetHelper_5();
      return this.init_PoolEntry_DataUtil(list.read(), list.write(), list.update(), list.sync());
   }

   private autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil init_PoolEntry_DataUtil(int listX, int listY, int listW, int listH) {
      int viewH = Math.max(1, listH - 6);
      return autismclient.gui.f.b.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(this.close(), viewH, listX + listW - 6, listY + 1, 4, Math.max(1, listH - 2), this.pop);
   }

   private int write() {
      return autismclient.gui.f.UiDensityMetrics.init(14);
   }

   private int update() {
      return autismclient.gui.f.UiDensityMetrics.init(13);
   }

   private int sync() {
      return autismclient.gui.f.UiDensityMetrics.init(18);
   }

   private int flush() {
      return this.poll() + this.write() + 4;
   }

   private autismclient.gui.f.NetHelper init_NetHelper_5() {
      int listY = this.flush() + this.update() + 2;
      return autismclient.gui.f.NetHelper.init(this.open() + 4, listY, this.push() - 8, this.poll() + this.pop() - listY - 4);
   }

   private int close() {
      int size = this.init_List_12().size();
      return size == 0 ? 0 : size * this.sync() + 2;
   }

   private int init_I_5(int boxY, int boxH) {
      return UiLayoutMath.init(boxY, boxH, init_CharEncoder.init_I_l(l.read), init_CharEncoder.flush());
   }

   private int open() {
      return autismclient.gui.f.c.PanelLayoutMath.load(this.init_I(), this.push(), 6);
   }

   private int poll() {
      return autismclient.gui.f.c.PanelLayoutMath.load(this.load_I(), this.pop(), 6);
   }

   private int push() {
      return autismclient.gui.f.c.PanelLayoutMath.init(this.init_I(), 6, 300);
   }

   private int pop() {
      int rowH = this.sync();
      int minList = 4 * rowH;
      int maxList = Math.max(minList, (int)(this.load_I() * 0.65F));
      int list = init(this.init_List.size() * rowH + 2, minList, maxList);
      int chrome = this.write() + 4 + this.update() + 2 + 6 + 4;
      return autismclient.gui.f.c.PanelLayoutMath.init(this.load_I(), 6, chrome + list);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      String display = autismclient.gui.f.b.k.init(
         this.font, text == null ? "" : text, Math.max(0, maxWidth), init_CharEncoder.init_Identifier_l(l.read), color
      );
      autismclient.gui.f.b.k.init(graphics, this.font, display, init_CharEncoder.init_Identifier_l(l.read), color, x, y, false);
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

   private record Inner_DataUtil(String init_Str, String load, String read, ItemStack init_ItemStack) {
      private Inner_DataUtil(String id, String label, ItemStack icon) {
         this(id, label != null && !label.isBlank() ? label : id, (label == null ? id : label).toLowerCase(Locale.ROOT), icon == null ? ItemStack.EMPTY : icon);
      }
   }

   private record Inner_StreamUtil(PickItemScreen.Inner_CachePool init_TextFormat_CachePo, int init_I, int load, int read, int write, String init_Str) {
      boolean init(int mx, int my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write;
      }
   }
}
