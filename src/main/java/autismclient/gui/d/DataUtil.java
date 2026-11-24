package autismclient.gui.d;

import autismclient.ai.AiToolSettings;
import autismclient.ai.o;
import autismclient.compat263.Compat263;
import autismclient.gui.f.b.g;
import autismclient.util.bi;
import autismclient.util.cA;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class DataUtil extends Screen {
   private static final int init_I = 300;
   private static final int load_I = 200;
   private static final int read_I = 12;
   private static final int write_I = 96;
   private static final String[] init_StrArr = new String[]{
      "Allow dig (navigation)",
      "Allow pillar (navigation)",
      "Allow bridge (navigation)",
      "Restock from backpack (navigation)",
      "AI silent aim (combat rotations)",
      "Use shield by default (fight)"
   };
   private final Screen init_Screen;
   private final AiToolSettings init_AiToolSettings;
   private boolean init_Z;
   private final autismclient.gui.f.b.RefQueue init_RefQueue;
   private final autismclient.gui.f.b.RefQueue load_RefQueue;
   private autismclient.gui.f.NetHelper init_NetHelper;
   private autismclient.gui.f.NetHelper load_NetHelper;
   private autismclient.gui.f.NetHelper read_NetHelper;
   private autismclient.gui.f.NetHelper write_NetHelper;
   private autismclient.gui.f.NetHelper update;
   private autismclient.gui.f.NetHelper sync;
   private final autismclient.gui.f.NetHelper[] init_NetHelperArr = new autismclient.gui.f.NetHelper[6];
   private boolean load_Z;
   private boolean read_Z;

   public DataUtil(Screen parent) {
      super(Component.literal("AI Tools"));
      this.init_Screen = parent;
      this.init_AiToolSettings = o.init_AiToolSettings().copy();
      this.init_Z = o.init_AiSettings().aiSilentAim;
      this.init_RefQueue = new autismclient.gui.f.b.RefQueue(
         autismclient.gui.f.NetHelper.init(0, 0, 0, 0),
         List.of("fastest", "safest", "saver"),
         this.init_AiToolSettings.strategy,
         value -> this.init_AiToolSettings.strategy = value
      );
      this.load_RefQueue = new autismclient.gui.f.b.RefQueue(
         autismclient.gui.f.NetHelper.init(0, 0, 0, 0),
         List.of("quiet", "normal", "verbose"),
         this.init_AiToolSettings.verbosity,
         value -> this.init_AiToolSettings.verbosity = value
      );
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
         int sw = cA.init_I();
         int sh = cA.load_I();
         autismclient.gui.f.SyncManager ctx = autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), -1610021111);
         String toolsCommand = autismclient.commands.DataUtil.init_Str() + "tools";
         String title = "AI TOOLS  ·  " + toolsCommand + " defaults";
         String help = "Read when a " + toolsCommand + " op starts; Esc discards, Done saves.";
         int margin = 6;
         int panelW = Math.min(sw - margin * 2, this.init(ctx, title, help));
         int panelH = this.init(ctx, title);
         autismclient.gui.f.NetHelper panel = autismclient.gui.f.NetHelper.init(
            (sw - panelW) / 2, Math.max(margin, (sh - panelH) / 2), panelW, Math.min(panelH, sh - margin * 2)
         );
         autismclient.gui.f.b.BufferHelper.init_V(ctx, panel, title);
         int pad = 4;
         int rowH = this.init_I();
         int labelH = this.load_I();
         int rowStep = rowH + 2;
         int sectionGap = 4;
         int x = panel.read() + pad;
         int y = autismclient.gui.f.b.BufferHelper.init_I(ctx, panel, title) + 2;
         int w = panel.update() - pad * 2;
         autismclient.gui.f.JsonUtil colors = ctx.init_TableIndex().init_JsonUtil();
         this.init_NetHelper = autismclient.gui.f.NetHelper.init(x, y, w, rowH);
         this.init(ctx, this.init_NetHelper, "Pathfinding strategy", this.init_RefQueue, this.init_AiToolSettings.strategy, mx, my);
         y += rowStep;

         for (int i = 0; i < 4; i++) {
            this.init_NetHelperArr[i] = autismclient.gui.f.NetHelper.init(x, y, w, rowH);
            y += rowStep;
         }

         g.init(ctx, this.init_NetHelperArr[0], init_StrArr[0], this.init_AiToolSettings.allowDig, this.init_NetHelperArr[0].init_Z(mx, my), "aitools:allowDig");
         g.init(
            ctx,
            this.init_NetHelperArr[1],
            init_StrArr[1],
            this.init_AiToolSettings.allowPillar,
            this.init_NetHelperArr[1].init_Z(mx, my),
            "aitools:allowPillar"
         );
         g.init(
            ctx,
            this.init_NetHelperArr[2],
            init_StrArr[2],
            this.init_AiToolSettings.allowBridge,
            this.init_NetHelperArr[2].init_Z(mx, my),
            "aitools:allowBridge"
         );
         g.init(
            ctx,
            this.init_NetHelperArr[3],
            init_StrArr[3],
            this.init_AiToolSettings.allowInventory,
            this.init_NetHelperArr[3].init_Z(mx, my),
            "aitools:allowInventory"
         );
         y += sectionGap;
         String reluctanceLabel = "Dig reluctance (per block)";
         String reluctanceValue = this.init_AiToolSettings.digReluctance + "t";
         int reluctanceTextY = ctx.init_PoolEntry().init_I(autismclient.gui.f.NetHelper.init(x, y, w, labelH));
         int reluctanceValueW = ctx.init_PoolEntry().init(reluctanceValue);
         ctx.init_PoolEntry().load(graphics, reluctanceLabel, x, reluctanceTextY, Math.max(1, w - reluctanceValueW - sectionGap), colors.valid);
         ctx.init_PoolEntry().load(graphics, reluctanceValue, x + w - reluctanceValueW, reluctanceTextY, reluctanceValueW, colors.prev);
         y += labelH;
         this.write_NetHelper = autismclient.gui.f.NetHelper.init(x, y, w, 12);
         autismclient.gui.f.b.c.init(
            ctx,
            this.write_NetHelper,
            autismclient.gui.f.b.c.init_D_D(this.init_AiToolSettings.digReluctance, 0.0, 60.0),
            this.write_NetHelper.init_Z(mx, my) || this.read_Z
         );
         y += 12 + sectionGap;
         this.init_NetHelperArr[4] = autismclient.gui.f.NetHelper.init(x, y, w, rowH);
         y += rowStep;
         this.init_NetHelperArr[5] = autismclient.gui.f.NetHelper.init(x, y, w, rowH);
         y += rowStep;
         g.init(ctx, this.init_NetHelperArr[4], init_StrArr[4], this.init_Z, this.init_NetHelperArr[4].init_Z(mx, my), "aitools:silentAim");
         g.init(
            ctx, this.init_NetHelperArr[5], init_StrArr[5], this.init_AiToolSettings.useShield, this.init_NetHelperArr[5].init_Z(mx, my), "aitools:useShield"
         );
         y += sectionGap;
         String timeoutLabel = "Default timeout";
         String timeoutValue = this.init_AiToolSettings.timeoutSeconds + "s";
         int timeoutTextY = ctx.init_PoolEntry().init_I(autismclient.gui.f.NetHelper.init(x, y, w, labelH));
         int valueW = ctx.init_PoolEntry().init(timeoutValue);
         ctx.init_PoolEntry().load(graphics, timeoutLabel, x, timeoutTextY, Math.max(1, w - valueW - sectionGap), colors.valid);
         ctx.init_PoolEntry().load(graphics, timeoutValue, x + w - valueW, timeoutTextY, valueW, colors.prev);
         y += labelH;
         this.read_NetHelper = autismclient.gui.f.NetHelper.init(x, y, w, 12);
         autismclient.gui.f.b.c.init(
            ctx,
            this.read_NetHelper,
            autismclient.gui.f.b.c.init_D_D(this.init_AiToolSettings.timeoutSeconds, 30.0, 1800.0),
            this.read_NetHelper.init_Z(mx, my) || this.load_Z
         );
         y += 12 + sectionGap;
         this.load_NetHelper = autismclient.gui.f.NetHelper.init(x, y, w, rowH);
         this.init(ctx, this.load_NetHelper, "Chat verbosity", this.load_RefQueue, this.init_AiToolSettings.verbosity, mx, my);
         y += rowH + sectionGap;
         ctx.init_PoolEntry().load(graphics, help, x, ctx.init_PoolEntry().init_I(autismclient.gui.f.NetHelper.init(x, y, w, labelH)), w, colors.valid);
         int ctrlH = this.read();
         int footerY = panel.load() - pad - ctrlH;
         int bw = (w - 2) / 2;
         this.update = autismclient.gui.f.NetHelper.init(x, footerY, bw, ctrlH);
         this.sync = autismclient.gui.f.NetHelper.init(x + bw + 2, footerY, w - bw - 2, ctrlH);
         autismclient.gui.f.b.CachePool.init(ctx, this.update, "Reset", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.update.init_Z(mx, my), false);
         autismclient.gui.f.b.CachePool.init(ctx, this.sync, "Done", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this.sync.init_Z(mx, my), false);
         this.init_RefQueue.init_V_SyncManager(ctx);
         this.load_RefQueue.init_V_SyncManager(ctx);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private int init_I() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private int load_I() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private int read() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int write() {
      int rowStep = this.init_I() + 2;
      int sectionGap = 4;
      int labelH = this.load_I();
      return 2
         + rowStep * 5
         + sectionGap
         + labelH
         + 12
         + sectionGap
         + rowStep * 2
         + sectionGap
         + labelH
         + 12
         + sectionGap
         + this.init_I()
         + sectionGap
         + labelH
         + sectionGap
         + this.read()
         + 4;
   }

   private int init(autismclient.gui.f.SyncManager ctx, String title) {
      int headerOffset = autismclient.gui.f.b.BufferHelper.init_I(ctx, autismclient.gui.f.NetHelper.init(0, 0, 300, 100), title);
      return headerOffset + this.write();
   }

   private int init(autismclient.gui.f.SyncManager ctx, String title, String help) {
      int widest = ctx.init_PoolEntry().init(help);
      widest = Math.max(widest, ctx.init_PoolEntry().init(title) + 20);

      for (String label : init_StrArr) {
         widest = Math.max(widest, ctx.init_PoolEntry().init(label) + 40);
      }

      widest = Math.max(widest, ctx.init_PoolEntry().init("Pathfinding strategy") + 96 + 8);
      return Math.max(200, Math.min(300, widest + 8 + 2));
   }

   private void init(
      autismclient.gui.f.SyncManager ctx,
      autismclient.gui.f.NetHelper row,
      String label,
      autismclient.gui.f.b.RefQueue dropdown,
      String selected,
      int mx,
      int my
   ) {
      autismclient.gui.f.JsonUtil colors = ctx.init_TableIndex().init_JsonUtil();
      autismclient.gui.f.NetHelper control = autismclient.gui.f.NetHelper.init(row.init() - 96, row.write(), 96, row.sync());
      ctx.init_PoolEntry()
         .load(
            ctx.init_GuiGraphicsExtract(), label, row.read() + 1, ctx.init_PoolEntry().init_I(row), Math.max(1, control.read() - row.read() - 8), colors.prev
         );
      dropdown.init_V_NetHelper(control);
      autismclient.gui.f.b.RefQueue.init_V_SyncManager(ctx, control, selected, control.init_Z(mx, my), dropdown.init_Z_2());
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      if (this.init_RefQueue.init_EventBus_I_2(mx, my, Compat263.mouseEventGlfwButton(event)) == autismclient.gui.f.EventBus.load) {
         return true;
      }

      if (this.load_RefQueue.init_EventBus_I_2(mx, my, Compat263.mouseEventGlfwButton(event)) == autismclient.gui.f.EventBus.load) {
         return true;
      }

      for (int i = 0; i < this.init_NetHelperArr.length; i++) {
         if (this.init_NetHelperArr[i] != null && this.init_NetHelperArr[i].init_Z(mx, my)) {
            this.init(i);
            return true;
         }
      }

      if (this.read_NetHelper != null && this.read_NetHelper.init_Z(mx, my)) {
         this.load_Z = true;
         this.load(mx);
         return true;
      } else if (this.write_NetHelper != null && this.write_NetHelper.init_Z(mx, my)) {
         this.read_Z = true;
         this.read(mx);
         return true;
      } else if (this.update != null && this.update.init_Z(mx, my)) {
         AiToolSettings defaults = new AiToolSettings();
         this.init_AiToolSettings.strategy = defaults.strategy;
         this.init_AiToolSettings.allowDig = defaults.allowDig;
         this.init_AiToolSettings.allowPillar = defaults.allowPillar;
         this.init_AiToolSettings.allowBridge = defaults.allowBridge;
         this.init_AiToolSettings.allowInventory = defaults.allowInventory;
         this.init_AiToolSettings.digReluctance = defaults.digReluctance;
         this.init_AiToolSettings.useShield = defaults.useShield;
         this.init_AiToolSettings.timeoutSeconds = defaults.timeoutSeconds;
         this.init_AiToolSettings.verbosity = defaults.verbosity;
         this.init_RefQueue.init(this.init_AiToolSettings.strategy);
         this.load_RefQueue.init(this.init_AiToolSettings.verbosity);
         return true;
      } else if (this.sync != null && this.sync.init_Z(mx, my)) {
         this.load_V();
         this.onClose();
         return true;
      } else {
         return true;
      }
   }

   private void init(int row) {
      switch (row) {
         case 0:
            this.init_AiToolSettings.allowDig = !this.init_AiToolSettings.allowDig;
            break;
         case 1:
            this.init_AiToolSettings.allowPillar = !this.init_AiToolSettings.allowPillar;
            break;
         case 2:
            this.init_AiToolSettings.allowBridge = !this.init_AiToolSettings.allowBridge;
            break;
         case 3:
            this.init_AiToolSettings.allowInventory = !this.init_AiToolSettings.allowInventory;
            break;
         case 4:
            this.init_Z = !this.init_Z;
            break;
         case 5:
            this.init_AiToolSettings.useShield = !this.init_AiToolSettings.useShield;
      }
   }

   private void load(int mx) {
      this.init_AiToolSettings.timeoutSeconds = (int)autismclient.gui.f.b.c.init(
         mx, this.read_NetHelper.read(), this.read_NetHelper.update(), 30.0, 1800.0, 30.0
      );
   }

   private void read(int mx) {
      this.init_AiToolSettings.digReluctance = (int)autismclient.gui.f.b.c.init(mx, this.write_NetHelper.read(), this.write_NetHelper.update(), 0.0, 60.0, 1.0);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      if (this.load_Z) {
         this.load(cA.init_I_D(event.x()));
         return true;
      } else if (this.read_Z) {
         this.read(cA.init_I_D(event.x()));
         return true;
      } else if (this.init_RefQueue.init_EventBus_I(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event), dx, dy)
         == autismclient.gui.f.EventBus.load) {
         return true;
      } else {
         return this.load_RefQueue.init_EventBus_I(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event), dx, dy)
               == autismclient.gui.f.EventBus.load
            ? true
            : true;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.load_Z = false;
      this.read_Z = false;
      this.init_RefQueue.load(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
      this.load_RefQueue.load(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
      return true;
   }

   private void load_V() {
      o.init(this.init_AiToolSettings.copy());
      o.init(settings -> settings.aiSilentAim = this.init_Z);
      bi.load("AI tool settings saved");
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.init_Screen);
   }
}
