package autismclient.gui.ui.widgets;

import autismclient.util.cA;
import java.util.List;
import java.util.function.Consumer;

public final class DropdownMenu implements autismclient.gui.ui.UiLayerComponent {
   private static final int init_I = 4;
   private autismclient.gui.f.NetHelper init_NetHelper;
   private autismclient.gui.f.NetHelper load_NetHelper = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private List<String> init_List;
   private String init_Str;
   private final Consumer<String> init_Consumer;
   private int load_I;
   private int read_I;
   private boolean init_Z;
   private long init_J;
   private boolean load_Z;
   private int write_I;
   private ScrollbarRenderer.Inner_DataUtil init_UtilCore_DataUtil;
   private boolean read_Z = true;
   private int update = 1;
   private int sync = -1;
   private int flush = -1;
   private int close = -1;
   private int open = -1;
   private boolean write_Z;

   public DropdownMenu(autismclient.gui.f.NetHelper bounds, List<String> options, String selected, Consumer<String> onSelect) {
      this.init_NetHelper = bounds == null ? autismclient.gui.f.NetHelper.init(0, 0, 0, 0) : bounds;
      this.init_List = options == null ? List.of() : List.copyOf(options);
      this.init_Str = selected == null ? "" : selected;
      this.init_Consumer = onSelect == null ? ignored -> {} : onSelect;
   }

   @Override
   public autismclient.gui.f.NetHelper init_NetHelper() {
      return this.init_NetHelper;
   }

   @Override
   public autismclient.gui.f.NetHelper load_NetHelper() {
      return this.init_Z ? this.init_NetHelper.init(this.load_NetHelper) : this.init_NetHelper;
   }

   @Override
   public void init_V_NetHelper(autismclient.gui.f.NetHelper bounds) {
      if (bounds != null) {
         if (this.init_NetHelper.update() != bounds.update()) {
            this.read_Z = true;
         }

         this.init_NetHelper = bounds;
      }
   }

   public boolean init_Z_2() {
      return this.init_Z;
   }

   public autismclient.gui.f.NetHelper read_NetHelper() {
      this.read_V();
      return this.load_NetHelper;
   }

   public int init_I() {
      return this.load_I;
   }

   public void init(List<String> options) {
      List<String> next = options == null ? List.of() : List.copyOf(options);
      if (!this.init_List.equals(next)) {
         this.init_List = next;
         this.read_Z = true;
         if (!this.init_List.contains(this.init_Str)) {
            this.init_Str = this.init_List.isEmpty() ? "" : this.init_List.get(0);
         }

         this.load_I = init_I_I(this.load_I, 0, this.write_I());
         this.write_V();
      }
   }

   public void init(String selected) {
      String next = selected == null ? "" : selected;
      if (!this.init_Str.equals(next)) {
         this.init_Str = next;
         this.write_V();
      }
   }

   public boolean init_Z(int mouseX, int mouseY) {
      this.read_V();
      return this.init_Z && this.load_NetHelper.init_Z(mouseX, mouseY);
   }

   public void init_V_2() {
      this.init_Z = true;
      this.init_J = System.nanoTime();
      this.read_V();
      this.write_V();
   }

   public void load_V_2() {
      this.init_Z = false;
      this.init_J = 0L;
      this.load_Z = false;
   }

   public static void init_V_SyncManager(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean hovered, boolean open
   ) {
      init(context, bounds, label, hovered, open, false, true);
   }

   public static void init(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean hovered, boolean open, boolean enabled
   ) {
      init(context, bounds, label, hovered, open, false, enabled);
   }

   public static void load(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String label, boolean hovered, boolean open) {
      init(context, bounds, label, hovered, open, true, true);
   }

   private static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String label,
      boolean hovered,
      boolean open,
      boolean centered,
      boolean enabled
   ) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().init_JsonUtil();
      int radius = 3;
      int fill = open && enabled ? colors.tick : colors.pop;
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, radius, fill, open && enabled ? colors.clear : colors.set);
      float hoverT = autismclient.gui.ui.HoverTransitionCache.init(autismclient.gui.ui.HoverTransitionCache.init(bounds), hovered && enabled);
      if (hoverT > 0.001F) {
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(), bounds.init(1), Math.max(1, radius - 1), autismclient.gui.f.PacketUtil.init(colors.add, hoverT)
         );
      }

      int textColor = enabled ? colors.prev : colors.size;
      if (centered) {
         context.init_PoolEntry().load(context.init_GuiGraphicsExtract(), label, bounds, textColor);
      } else {
         context.init_PoolEntry()
            .load(
               context.init_GuiGraphicsExtract(),
               label,
               bounds.read() + 5,
               context.init_PoolEntry().init_I(bounds),
               Math.max(1, bounds.update() - 18),
               textColor
            );
      }

      autismclient.gui.f.PacketUtil.init(
         context.init_GuiGraphicsExtract(),
         autismclient.gui.f.NetHelper.init(bounds.init() - 10, bounds.write() + Math.max(0, (bounds.sync() - 8) / 2), 8, 8),
         open,
         textColor
      );
   }

   @Override
   public void init_V_SyncManager(autismclient.gui.f.SyncManager context) {
      if (this.init_Z && !this.init_List.isEmpty()) {
         int rowHeight = load_I();
         this.flush = context.init_I();
         this.close = context.load_I();
         this.open = rowHeight;
         this.read_I = this.init_I(context.load_I(), rowHeight);
         int width = this.init_I(context);
         this.init(context.init_I(), context.load_I(), rowHeight, width);
         autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().init_JsonUtil();
         float progress = this.init_J == 0L ? 1.0F : Math.max(0.0F, Math.min(1.0F, (float)(System.nanoTime() - this.init_J) / 1.6E8F));
         float fade = 1.0F - (1.0F - progress) * (1.0F - progress) * (1.0F - progress);
         int radius = 5;
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(), this.load_NetHelper, radius, autismclient.gui.f.PacketUtil.init(colors.get, fade)
         );
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(), this.load_NetHelper.init(1), Math.max(1, radius - 1), autismclient.gui.f.PacketUtil.init(colors.sync, fade)
         );
         int contentWidth = this.init_List.size() > this.read_I ? this.load_NetHelper.update() - 6 - 3 : this.load_NetHelper.update() - 2;

         for (int rowIndex = 0; rowIndex < this.read_I; rowIndex++) {
            int optionIndex = this.load_I + rowIndex;
            if (optionIndex >= this.init_List.size()) {
               break;
            }

            String option = this.init_List.get(optionIndex);
            autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(
               this.load_NetHelper.read() + 1, this.load_NetHelper.write() + 1 + rowIndex * rowHeight, Math.max(1, contentWidth), rowHeight
            );
            boolean hovered = row.init_Z(context.read(), context.write());
            boolean active = option.equals(this.init_Str);
            float hoverT = autismclient.gui.ui.HoverTransitionCache.init("dropdown:" + option, hovered);
            autismclient.gui.f.NetHelper pill = autismclient.gui.f.NetHelper.init(row.read() + 2, row.write(), Math.max(1, row.update() - 4), row.sync());
            if (active) {
               autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), pill, 4, autismclient.gui.f.PacketUtil.init(colors.start, fade));
            } else if (hoverT > 0.001F) {
               autismclient.gui.f.PacketUtil.load(
                  context.init_GuiGraphicsExtract(), pill, 4, autismclient.gui.f.PacketUtil.init(autismclient.gui.f.PacketUtil.init(colors.add, hoverT), fade)
               );
            }

            context.init_PoolEntry()
               .load(
                  context.init_GuiGraphicsExtract(),
                  option,
                  row.read() + 4,
                  context.init_PoolEntry().init_I(row),
                  Math.max(1, row.update() - 18),
                  autismclient.gui.f.PacketUtil.init(colors.prev, fade)
               );
            if (active) {
               autismclient.gui.f.PacketUtil.update(
                  context.init_GuiGraphicsExtract(),
                  autismclient.gui.f.NetHelper.init(row.init() - 11, row.write() + Math.max(0, (row.sync() - 8) / 2), 8, 8),
                  autismclient.gui.f.PacketUtil.init(colors.clear, fade)
               );
            }
         }

         if (this.init_List.size() > this.read_I) {
            autismclient.gui.f.NetHelper track = autismclient.gui.f.NetHelper.init(
               this.load_NetHelper.init() - 6 - 1, this.load_NetHelper.write() + 4, 6, Math.max(1, this.load_NetHelper.sync() - 8)
            );
            this.init_UtilCore_DataUtil = ScrollbarRenderer.init(track, this.init_List.size() * rowHeight, this.read_I * rowHeight, this.load_I * rowHeight);
            ScrollbarRenderer.init(
               context, this.init_UtilCore_DataUtil, this.init_UtilCore_DataUtil.init_NetHelper().init_Z(context.read(), context.write()), this.load_Z
            );
         } else {
            this.init_UtilCore_DataUtil = null;
         }
      }
   }

   @Override
   public autismclient.gui.ui.UiInputResult init_EventBus_I_2(int mouseX, int mouseY, int button) {
      this.read_V();
      if (button != 0) {
         if (!this.init_Z) {
            return autismclient.gui.ui.UiInputResult.IGNORED;
         }

         if (!this.init_NetHelper.init_Z(mouseX, mouseY) && !this.load_NetHelper.init_Z(mouseX, mouseY)) {
            this.load_V_2();
         }

         return autismclient.gui.ui.UiInputResult.HANDLED;
      } else if (this.init_Z) {
         if (this.load_NetHelper.init_Z(mouseX, mouseY)) {
            if (this.init_UtilCore_DataUtil != null && this.init_UtilCore_DataUtil.init_NetHelper().init_Z(mouseX, mouseY)) {
               this.write_I = this.init_UtilCore_DataUtil.load().init_Z(mouseX, mouseY)
                  ? mouseY - this.init_UtilCore_DataUtil.load().write()
                  : this.init_UtilCore_DataUtil.load().sync() / 2;
               this.load_Z = true;
               this.init(mouseY);
               return autismclient.gui.ui.UiInputResult.HANDLED;
            }

            int index = this.load_I + Math.max(0, (mouseY - this.load_NetHelper.write() - 1) / Math.max(1, this.read_I()));
            if (index >= 0 && index < this.init_List.size()) {
               this.init_Str = this.init_List.get(index);
               this.init_Consumer.accept(this.init_Str);
            }

            this.load_V_2();
            return autismclient.gui.ui.UiInputResult.HANDLED;
         } else if (this.init_NetHelper.init_Z(mouseX, mouseY)) {
            this.load_V_2();
            return autismclient.gui.ui.UiInputResult.HANDLED;
         } else {
            this.load_V_2();
            return autismclient.gui.ui.UiInputResult.HANDLED;
         }
      } else if (this.init_NetHelper.init_Z(mouseX, mouseY)) {
         this.init_V_2();
         return autismclient.gui.ui.UiInputResult.HANDLED;
      } else {
         return autismclient.gui.ui.UiInputResult.IGNORED;
      }
   }

   @Override
   public autismclient.gui.ui.UiInputResult load(int mouseX, int mouseY, int button) {
      this.read_V();
      if (!this.load_Z) {
         return this.init_Z ? autismclient.gui.ui.UiInputResult.HANDLED : autismclient.gui.ui.UiInputResult.IGNORED;
      }

      this.load_Z = false;
      return autismclient.gui.ui.UiInputResult.HANDLED;
   }

   @Override
   public autismclient.gui.ui.UiInputResult init_EventBus_I(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      this.read_V();
      if (this.load_Z && button == 0) {
         this.init(mouseY);
         return autismclient.gui.ui.UiInputResult.HANDLED;
      } else {
         return this.init_Z ? autismclient.gui.ui.UiInputResult.HANDLED : autismclient.gui.ui.UiInputResult.IGNORED;
      }
   }

   @Override
   public autismclient.gui.ui.UiInputResult init_EventBus_I(int mouseX, int mouseY, double amount) {
      this.read_V();
      if (!this.init_Z) {
         return autismclient.gui.ui.UiInputResult.IGNORED;
      }

      if (this.load_NetHelper.init_Z(mouseX, mouseY)) {
         int direction = amount < 0.0 ? 1 : (amount > 0.0 ? -1 : 0);
         this.load_I = init_I_I(this.load_I + direction, 0, this.write_I());
      }

      return autismclient.gui.ui.UiInputResult.HANDLED;
   }

   private int init_I(autismclient.gui.f.SyncManager context) {
      boolean overflow = this.init_List.size() > this.read_I;
      if (!this.read_Z && this.sync == context.init_I() && this.write_Z == overflow) {
         return this.update;
      }

      int chrome = 20 + (overflow ? 7 : 0);
      int width = Math.max(1, this.init_NetHelper.update());

      for (String option : this.init_List) {
         width = Math.max(width, context.init_PoolEntry().init(option) + chrome);
      }

      this.update = Math.min(width, Math.max(1, context.init_I() - 8));
      this.sync = context.init_I();
      this.write_Z = overflow;
      this.read_Z = false;
      return this.update;
   }

   private void read_V() {
      if (this.init_Z) {
         int screenWidth = this.flush > 0 ? this.flush : Math.max(1, cA.init_I());
         int screenHeight = this.close > 0 ? this.close : Math.max(1, cA.load_I());
         int rowHeight = this.open > 0 ? this.open : load_I();
         this.read_I = this.init_I(screenHeight, rowHeight);
         int width = Math.max(1, Math.min(this.update > 1 ? this.update : this.init_NetHelper.update(), screenWidth - 8));
         this.init(screenWidth, screenHeight, rowHeight, width);
      }
   }

   private void init(int screenWidth, int screenHeight, int rowHeight, int width) {
      this.read_I = Math.max(1, Math.min(this.init_List.size(), this.read_I <= 0 ? this.init_I(screenHeight, rowHeight) : this.read_I));
      this.load_I = init_I_I(this.load_I, 0, this.write_I());
      int height = Math.max(1, this.read_I * rowHeight + 2);
      int x = this.init_NetHelper.read();
      int below = this.init_NetHelper.load() + 1;
      int above = this.init_NetHelper.write() - height - 1;
      int availableBelow = screenHeight - 4 - below;
      int availableAbove = this.init_NetHelper.write() - 4;
      int y = availableBelow < height && availableBelow < availableAbove ? above : below;
      this.load_NetHelper = autismclient.gui.f.NetHelper.init(
         init_I_I(x, 4, Math.max(4, screenWidth - 4 - width)), init_I_I(y, 4, Math.max(4, screenHeight - 4 - height)), width, height
      );
   }

   private int init_I(int screenHeight, int rowHeight) {
      int below = Math.max(0, (screenHeight - 4 - this.init_NetHelper.load() - 1) / Math.max(1, rowHeight));
      int above = Math.max(0, (this.init_NetHelper.write() - 4 - 1) / Math.max(1, rowHeight));
      int cap = Math.max(3, screenHeight * 2 / 3 / Math.max(1, rowHeight));
      return Math.max(1, Math.min(this.init_List.size(), Math.min(cap, Math.max(below, above))));
   }

   private static int load_I() {
      return autismclient.gui.ui.UiDensityMetrics.init(12);
   }

   private int read_I() {
      return this.read_I <= 0 ? 1 : Math.max(1, (this.load_NetHelper.sync() - 2) / this.read_I);
   }

   private int write_I() {
      return Math.max(0, this.init_List.size() - Math.max(1, this.read_I));
   }

   private void write_V() {
      int index = this.init_List.indexOf(this.init_Str);
      if (index >= 0) {
         if (index < this.load_I) {
            this.load_I = index;
         }

         if (this.read_I > 0 && index >= this.load_I + this.read_I) {
            this.load_I = index - this.read_I + 1;
         }

         this.load_I = init_I_I(this.load_I, 0, this.write_I());
      }
   }

   private void init(int mouseY) {
      if (this.init_UtilCore_DataUtil != null && this.init_UtilCore_DataUtil.init_I() > 0) {
         int pixelScroll = ScrollbarRenderer.init_I_UtilCore_DataUtil(this.init_UtilCore_DataUtil, mouseY, this.write_I);
         this.load_I = init_I_I(Math.round((float)pixelScroll / Math.max(1, this.read_I())), 0, this.write_I());
      }
   }

   private static int init_I_I(int value, int min, int max) {
      return Math.max(min, Math.min(value, max));
   }
}
