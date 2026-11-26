package autismclient.gui.macro.editor;

import autismclient.gui.f.UiClipStack;
import autismclient.gui.f.NetHelper;
import autismclient.gui.f.b.TextInputWidget;
import autismclient.gui.f.b.CharEncoder;
import autismclient.gui.f.b.WindowHeaderHelper;
import autismclient.gui.f.b.ListRowRenderer;
import autismclient.gui.f.b.PanelSurfaceHelper;
import autismclient.gui.f.b.SearchFilterList;
import autismclient.gui.f.b.UiLayoutMath;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.gui.f.c.ScrollableListView;
import autismclient.gui.f.c.EdgeInsets;
import autismclient.gui.f.c.LabelNode;
import autismclient.gui.f.c.LayoutViewport;
import autismclient.gui.f.c.LayoutContext;
import autismclient.gui.f.c.LayoutTreeHost;
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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
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
   private final Font init_Font;
   private final CharEncoder init_CharEncoder_2 = new CharEncoder();
   private final autismclient.gui.f.c.CollapsibleWindowNode init_CharEncoder_3 = new autismclient.gui.f.c.CollapsibleWindowNode("Race Selector");
   private final LayoutTreeHost init_TextFormat = new LayoutTreeHost(this.init_CharEncoder_2, this.init_CharEncoder_3);
   private final TextInputWidget init_BitSetUtil = new TextInputWidget();
   private final LabelNode init_PoolEntry = new LabelNode("", l.write).load_PoolEntry(true);
   private final autismclient.gui.f.c.VerticalSpacerNode init_BitSetUtil_2 = new autismclient.gui.f.c.VerticalSpacerNode();
   private ScrollableListView init_EventBus;
   private final SearchFilterList<RaceStepSelectorOverlay.Inner_DataUtil> init_a = new SearchFilterList<>(
      option -> option.load() + "\n" + option.read() + "\n" + option.init() + "\n" + option.write()
   );
   private List<RaceStepSelectorOverlay.Inner_DataUtil> init_List = List.of();
   private List<RaceStepSelectorOverlay.Inner_DataUtil> load_List = List.of();
   private Consumer<RaceStepSelectorOverlay.Inner_DataUtil> init_Consumer;
   private boolean write_Z;
   private boolean update_Z;
   private boolean sync_Z;
   private boolean flush_Z;
   private float load_F;
   private float read_F;
   private float write_F;
   private float update_F;
   private int add;
   private int clear;
   private int reset = 160;
   private int start;
   private final autismclient.gui.f.VisibilityTransition init_CharEncoder = new autismclient.gui.f.VisibilityTransition(false);

   public RaceStepSelectorOverlay(Font textRenderer) {
      this.init_Font = textRenderer;
      this.init_CharEncoder_3.flush(false);
      this.init_CharEncoder_3.init(l.load);
      this.init_CharEncoder_3.init_CharEncoder_Z_Z(true, true);
      this.init_CharEncoder_3.init_CharEncoder_I_I(8, pop_I_3());
      this.init_CharEncoder_3.init_PacketUtil().init(2).init(EdgeInsets.init(4));
      this.init_BitSetUtil.load_BitSetUtil_Str("Search...").read_BitSetUtil_I(13).close(true).init_BitSetUtil_Consumer(this::init_V_2);
      this.write_V_4();
   }

   public void init_V_Str_4(String title, List<RaceStepSelectorOverlay.Inner_DataUtil> options, Consumer<RaceStepSelectorOverlay.Inner_DataUtil> onSelect) {
      this.write_Z = true;
      this.update_Z = false;
      this.sync_Z = false;
      this.flush_Z = false;
      this.init_Consumer = onSelect;
      this.init_List = options == null ? List.of() : List.copyOf(options);
      this.init_a.init_V_List(this.init_List);
      this.init_a.init_V_Str("");
      this.load_List = this.init_a.init_List();
      String shownTitle = title != null && !title.isBlank() ? title : "Race Selector";
      this.init_CharEncoder_3.init(shownTitle);
      this.reset = this.init_I(shownTitle);
      this.init_BitSetUtil.init_BitSetUtil_Str("");
      this.init_BitSetUtil.init_V_Z(true);
      this.write_V_4();
      LayoutContext metrics = this.init_TextFormat.init_SyncManager();
      if (metrics != null) {
         this.start = Math.round(this.init_CharEncoder_3.init_F_SyncManager_F(metrics, this.reset));
      }

      LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      this.add = Math.max(8, Math.round((viewport.init() - this.reset) / 2.0F));
      this.clear = Math.max(8, Math.round((viewport.load() - this.start) / 2.0F));
      OverlayBounds clamped = this.init_cH(new OverlayBounds(this.add, this.clear, this.reset, this.start, this.write_Z, this.update_Z));
      this.add = clamped.init_I;
      this.clear = clamped.load_I;
      this.reset = clamped.read;
      this.start = clamped.write;
      this.init_CharEncoder_3.write(true);
      OverlayManager.init_bk().init(this);
   }

   public void load_V_3() {
      this.write_Z = false;
      this.update_Z = false;
      this.sync_Z = false;
      this.flush_Z = false;
      this.init_Consumer = null;
      this.init_TextFormat.load();
      this.init_CharEncoder_3.write(true);
   }

   @Override
   public boolean sync_Z() {
      return this.write_Z;
   }

   @Override
   public boolean write_Z() {
      return this.write_Z && this.init_BitSetUtil.load_Z();
   }

   private void init_V_2(String query) {
      this.init_a.init_V_List(this.init_List);
      this.init_a.init_V_Str(query);
      this.load_List = this.init_a.init_List();
      if (this.init_EventBus != null) {
         this.init_EventBus.load_V(0);
      }

      this.write_V_4();
   }

   private void write_V_4() {
      this.init_CharEncoder_3.init_PacketUtil().init_V_2();
      this.init_CharEncoder_3.init_PacketUtil().init_TableIndex_TableIndex(this.init_BitSetUtil);
      this.init_BitSetUtil_2.init(this.init(this.load_List.size()));
      this.init_CharEncoder_3.init_PacketUtil().init_TableIndex_TableIndex(this.init_BitSetUtil_2);
      this.init_PoolEntry.init(this.load_List.size() + " choices");
      this.init_CharEncoder_3.init_PacketUtil().init_TableIndex_TableIndex(this.init_PoolEntry);
   }

   private int init(int count) {
      int rows = Math.max(3, Math.min(this.push_I_3(), count));
      return rows * 14 + 2;
   }

   private int push_I_3() {
      int screenH = Math.round(this.init_TextFormat.init_RingBuffer().load());
      if (screenH <= 0) {
         return 12;
      }

      int fit = (int)(screenH * 0.65F) / 14;
      return Math.max(3, Math.min(12, fit));
   }

   private static int pop_I_3() {
      return 41;
   }

   private int init_I(String title) {
      int widest = 0;

      for (RaceStepSelectorOverlay.Inner_DataUtil option : this.init_List) {
         widest = Math.max(
            widest,
            k.init(
               this.init_Font, option.init() + " / " + option.read(), this.init_CharEncoder_2.init_Identifier_l(l.read), this.init_CharEncoder_2.load(l.read)
            )
         );
      }

      int listW = widest + 10 + 6 + 3 + 8;
      int titleW = k.init(this.init_Font, title, this.init_CharEncoder_2.init_Identifier_l(l.load), this.init_CharEncoder_2.load(l.load)) + 8 + pop_I_3();
      return Math.max(160, Math.min(260, Math.max(listW, titleW)));
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      this.init_CharEncoder.init_V(this.write_Z);
      if (this.init_CharEncoder.load_Z(this.write_Z) && init_Minecraft != null && init_Minecraft.font != null) {
         boolean closing = !this.write_Z;
         this.init_CharEncoder
            .init(
               context,
               NetHelper.init(this.add, this.clear, this.reset, Math.max(this.init_CharEncoder_2.load(), this.start)),
               true,
               () -> this.load(context, closing ? -10000 : mouseX, closing ? -10000 : mouseY, delta)
            );
      }
   }

   private void load(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      context.nextStratum();
      LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.init_F_D(mouseX);
      float uiMouseY = viewport.load(mouseY);
      LayoutContext metrics = new LayoutContext(context, init_Minecraft.font, viewport, this.init_CharEncoder_2, uiMouseX, uiMouseY, delta);
      this.init_CharEncoder_3.load_CharEncoder(!this.update_Z);
      this.init_CharEncoder_3.update(true);
      this.init_CharEncoder_3.sync(this.init_Z_F_F(uiMouseX, uiMouseY));
      this.start = Math.max(this.init_CharEncoder_2.load(), Math.round(this.init_CharEncoder_3.init_F_SyncManager_F(metrics, this.reset)));
      OverlayBounds clamped = this.init_cH(new OverlayBounds(this.add, this.clear, this.reset, this.start, this.write_Z, this.update_Z));
      this.add = clamped.init_I;
      this.clear = clamped.load_I;
      this.reset = clamped.read;
      this.start = clamped.write;
      this.init_CharEncoder_3.init_TableIndex_F(this.add, this.clear, this.reset, this.start);
      this.init_TextFormat.init(context, mouseX, mouseY, delta);
      context.nextStratum();
      if (this.init_CharEncoder_3.init(context)) {
         try {
            this.init_V_GuiGraphicsExtract(context, uiMouseX, uiMouseY);
         } finally {
            UiClipStack.init_HashUtil().init(context);
         }
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context, float uiMouseX, float uiMouseY) {
      int x = Math.round(this.init_BitSetUtil_2.load_F());
      int y = Math.round(this.init_BitSetUtil_2.read_F());
      int w = Math.round(this.init_BitSetUtil_2.write_F());
      int h = Math.round(this.init_BitSetUtil_2.update_F());
      if (w > 2 && h > 2) {
         if (this.load_List.isEmpty()) {
            ListRowRenderer.init(context, this.init_Font, "Nothing to pick.", x, y, w - 6 - 1);
         } else {
            if (this.init_EventBus == null
               || this.init_EventBus.init_I() != x
               || this.init_EventBus.load_I() != y
               || this.init_EventBus.read_I() != w
               || this.init_EventBus.write_I() != h) {
               int oldScroll = this.init_EventBus == null ? 0 : this.init_EventBus.open();
               this.init_EventBus = new ScrollableListView(x, y, w, h, 14, 6);
               this.init_EventBus.load_V(oldScroll);
            }

            this.init_EventBus.init_V(this.load_List.size() * 14);
            this.init_EventBus.init_V_GuiGraphicsExtract_2(context, this.init_CharEncoder_2.set(), this.init_CharEncoder_2.reset());

            try {
               this.init_EventBus
                  .init_V_GuiGraphicsExtract_3(
                     context,
                     this.load_List.size(),
                     (idx, bnd) -> this.init(context, this.load_List.get(idx), bnd.init, bnd.load, bnd.read, idx, uiMouseX, uiMouseY)
                  );
            } finally {
               this.init_EventBus.init(context);
            }

            this.init_EventBus.init_V_GuiGraphicsExtract(context, uiMouseX, uiMouseY);
         }
      }
   }

   private void init(
      GuiGraphicsExtractor context, RaceStepSelectorOverlay.Inner_DataUtil option, int x, int y, int width, int index, float mouseX, float mouseY
   ) {
      boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 14;
      int bg = hovered ? this.init_CharEncoder_2.next() : this.init_CharEncoder_2.stop();
      int accent = "Condition".equalsIgnoreCase(option.init()) ? UiColorPalette.node() : UiColorPalette.init();
      PanelSurfaceHelper.init(context, x, y, width, 14, bg);
      PanelSurfaceHelper.read(context, x, y, 2, 14, accent);
      String label = option.init() + " / " + option.read();
      String trimmed = k.init(
         this.init_Font, label, Math.max(1, width - 10), this.init_CharEncoder_2.init_Identifier_l(l.read), this.init_CharEncoder_2.load(l.read)
      );
      int textY = UiLayoutMath.init(y, 14, this.init_CharEncoder_2.init_I_l(l.read), this.init_CharEncoder_2.flush());
      k.init(context, this.init_Font, trimmed, this.init_CharEncoder_2.init_Identifier_l(l.read), this.init_CharEncoder_2.load(l.read), x + 6, textY, false);
      PanelSurfaceHelper.init(context, x + 4, y + 14 - 1, width - 8, UiColorPalette.mark());
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.write_Z) {
         return false;
      }

      LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.init_F_D(mouseX);
      float uiMouseY = viewport.load(mouseY);
      if (button == 0 && this.load_Z_F_F(uiMouseX, uiMouseY)) {
         this.load_V_3();
         return true;
      }

      if (button == 0 && this.init_Z_F_F(uiMouseX, uiMouseY)) {
         this.sync_Z = true;
         this.flush_Z = false;
         this.load_F = uiMouseX - this.add;
         this.read_F = uiMouseY - this.clear;
         this.write_F = uiMouseX;
         this.update_F = uiMouseY;
         return true;
      }

      if (!this.update_Z && this.init_TextFormat.init_Z_D_2(mouseX, mouseY, button)) {
         return true;
      }

      if (!this.update_Z
         && button == 0
         && this.init(
            this.init_BitSetUtil_2.load_F(),
            this.init_BitSetUtil_2.read_F(),
            this.init_BitSetUtil_2.write_F(),
            this.init_BitSetUtil_2.update_F(),
            uiMouseX,
            uiMouseY
         )) {
         if (this.init_EventBus != null && this.init_EventBus.init_Z_D_2(uiMouseX, uiMouseY, button)) {
            return true;
         }

         int index = (int)((uiMouseY - this.init_BitSetUtil_2.read_F() + (this.init_EventBus == null ? 0 : this.init_EventBus.open())) / 14.0F);
         if (index >= 0 && index < this.load_List.size()) {
            RaceStepSelectorOverlay.Inner_DataUtil selected = this.load_List.get(index);
            if (this.init_Consumer != null) {
               this.init_Consumer.accept(selected);
            }

            this.load_V_3();
            return true;
         }
      }

      if (!this.init(this.add, this.clear, this.reset, this.start, uiMouseX, uiMouseY)) {
         this.init_TextFormat.load();
         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.init_F_D(mouseX);
      float uiMouseY = viewport.load(mouseY);
      if (button == 0 && this.sync_Z) {
         this.sync_Z = false;
         if (!this.flush_Z
            && Math.abs(uiMouseX - this.write_F) < 3.0F
            && Math.abs(uiMouseY - this.update_F) < 3.0F
            && this.init_Z_F_F(uiMouseX, uiMouseY)
            && !this.load_Z_F_F(uiMouseX, uiMouseY)) {
            this.write_V_Z(!this.update_Z);
         }

         this.flush_Z = false;
         return true;
      } else {
         if (button == 0 && this.init_EventBus != null) {
            this.init_EventBus.read_V();
         }

         return !this.update_Z && this.init_TextFormat.load(mouseX, mouseY, button) ? true : this.write_Z;
      }
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (!this.write_Z) {
         return false;
      }

      LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.init_F_D(mouseX);
      float uiMouseY = viewport.load(mouseY);
      if (this.sync_Z && button == 0) {
         if (!this.flush_Z && Math.abs(uiMouseX - this.write_F) < 3.0F && Math.abs(uiMouseY - this.update_F) < 3.0F) {
            return true;
         }

         this.flush_Z = true;
         int nextX = Math.round(uiMouseX - this.load_F);
         int nextY = Math.round(uiMouseY - this.read_F);
         if (nextX != this.add || nextY != this.clear) {
            this.flush_Z = true;
         }

         OverlayBounds clamped = this.init_cH(new OverlayBounds(nextX, nextY, this.reset, this.start, this.write_Z, this.update_Z));
         this.add = clamped.init_I;
         this.clear = clamped.load_I;
         return true;
      } else if (!this.update_Z && this.init_EventBus != null && this.init_EventBus.init_Z()) {
         this.init_EventBus.init_V_D_D(uiMouseX, uiMouseY);
         return true;
      } else {
         return !this.update_Z && this.init_TextFormat.init(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      if (this.write_Z && !this.update_Z && this.init_EventBus != null) {
         LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
         float uiMouseX = viewport.init_F_D(mouseX);
         float uiMouseY = viewport.load(mouseY);
         if (this.init(
            this.init_BitSetUtil_2.load_F(),
            this.init_BitSetUtil_2.read_F(),
            this.init_BitSetUtil_2.write_F(),
            this.init_BitSetUtil_2.update_F(),
            uiMouseX,
            uiMouseY
         )) {
            this.init_EventBus.init_Z_D(uiMouseX, uiMouseY, amount);
            return true;
         } else {
            return this.write_Z;
         }
      } else {
         return this.write_Z;
      }
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (!this.write_Z) {
         return false;
      } else if (keyCode == 256) {
         this.load_V_3();
         return true;
      } else {
         return this.update_Z ? false : this.init_TextFormat.init_Z_I(keyCode, scanCode, modifiers);
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      return this.write_Z && !this.update_Z && this.init_TextFormat.init(chr, modifiers);
   }

   @Override
   public boolean init_Z_D_D(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.init_F_D(mouseX);
      float uiMouseY = viewport.load(mouseY);
      int renderedHeight = this.update_Z ? this.init_CharEncoder_2.load() : this.start;
      return this.init(this.add, this.clear, this.reset, renderedHeight, uiMouseX, uiMouseY);
   }

   @Override
   public boolean load_Z_D_D(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.init_F_D(mouseX);
      float uiMouseY = viewport.load(mouseY);
      return this.init_Z_F_F(uiMouseX, uiMouseY) && !this.load_Z_F_F(uiMouseX, uiMouseY);
   }

   @Override
   public boolean flush_Z() {
      return this.update_Z;
   }

   @Override
   public void write_V_Z(boolean collapsed) {
      if (this.update_Z != collapsed) {
         this.update_Z = collapsed;
         this.sync_Z = false;
         this.flush_Z = false;
         this.init_CharEncoder_3.write(!collapsed);
         if (collapsed) {
            this.get_V();
         }

         this.a__V();
      }
   }

   @Override
   public OverlayBounds init_cH() {
      return new OverlayBounds(this.add, this.clear, this.reset, this.start, this.write_Z, this.update_Z);
   }

   @Override
   public void init_V_cH(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.init_cH(bounds);
         this.add = clamped.init_I;
         this.clear = clamped.load_I;
         this.reset = clamped.read;
         this.start = clamped.write;
         this.write_Z = clamped.init_Z;
         this.update_Z = clamped.load_Z;
         this.init_CharEncoder_3.write(!this.update_Z);
      }
   }

   @Override
   public void push_V() {
      this.init_TextFormat.load();
   }

   @Override
   public int init_I() {
      return 160;
   }

   @Override
   public int load_I() {
      return this.init_CharEncoder_2.load() + 20;
   }

   private boolean init_Z_F_F(float x, float y) {
      return x >= this.add && x < this.add + this.reset && y >= this.clear && y < this.clear + this.init_CharEncoder_2.load();
   }

   private boolean load_Z_F_F(float x, float y) {
      return WindowHeaderHelper.init(
         NetHelper.init(this.add, this.clear, this.reset, Math.max(this.init_CharEncoder_2.load(), this.start)), this.init_CharEncoder_2.load(), x, y
      );
   }

   private boolean init(float x, float y, float w, float h, float mx, float my) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   private OverlayBounds init_cH(OverlayBounds layout) {
      LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      int margin = 4;
      int viewportW = Math.round(viewport.init());
      int viewportH = Math.round(viewport.load());
      int availableW = Math.max(1, viewportW - margin * 2);
      int availableH = Math.max(this.init_CharEncoder_2.load(), viewportH - margin * 2);
      int width = Math.max(Math.min(160, availableW), Math.min(layout.read, availableW));
      int minHeight = layout.load_Z ? this.init_CharEncoder_2.load() : this.init_CharEncoder_2.load() + 20;
      int height = Math.max(Math.min(minHeight, availableH), Math.min(layout.write, availableH));
      int renderedHeight = layout.load_Z ? this.init_CharEncoder_2.load() : height;
      int x = Math.max(margin, Math.min(layout.init_I, Math.max(margin, viewportW - margin - width)));
      int y = Math.max(margin, Math.min(layout.load_I, Math.max(margin, viewportH - margin - renderedHeight)));
      return new OverlayBounds(x, y, width, height, layout.init_Z, layout.load_Z);
   }

   public record Inner_DataUtil(String init, String load, String read, String write) {
   }
}
