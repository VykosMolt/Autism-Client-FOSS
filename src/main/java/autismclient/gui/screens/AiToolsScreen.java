package autismclient.gui.screens;

import autismclient.ai.AiToolSettings;
import autismclient.ai.AiDirectOps;
import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.ToggleSwitchRenderer;
import autismclient.util.bi;
import autismclient.util.cA;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class AiToolsScreen extends Screen {
   private static final int init_I = 300;
   private static final int load_I = 200;
   private static final int read_I = 12;
   private static final int write_I = 96;
   private static final String[] TOGGLE_LABELS = new String[]{
      "Allow dig (navigation)",
      "Allow pillar (navigation)",
      "Allow bridge (navigation)",
      "Restock from backpack (navigation)",
      "AI silent aim (combat rotations)",
      "Use shield by default (fight)"
   };
   private final Screen parent;
   private final AiToolSettings settings;
   private boolean silentAim;
   private final autismclient.gui.ui.widgets.DropdownMenu strategyDropdown;
   private final autismclient.gui.ui.widgets.DropdownMenu verbosityDropdown;
   private autismclient.gui.f.NetHelper strategyRow;
   private autismclient.gui.f.NetHelper verbosityRow;
   private autismclient.gui.f.NetHelper digReluctanceSlider;
   private autismclient.gui.f.NetHelper timeoutSlider;
   private autismclient.gui.f.NetHelper resetButton;
   private autismclient.gui.f.NetHelper doneButton;
   private final autismclient.gui.f.NetHelper[] toggleRows = new autismclient.gui.f.NetHelper[6];
   private boolean draggingTimeout;
   private boolean draggingDigReluctance;

   public AiToolsScreen(Screen parent) {
      super(Component.literal("AI Tools"));
      this.parent = parent;
      this.settings = AiDirectOps.toolSettings().copy();
      this.silentAim = AiDirectOps.settings().aiSilentAim;
      this.strategyDropdown = new autismclient.gui.ui.widgets.DropdownMenu(
         autismclient.gui.f.NetHelper.init(0, 0, 0, 0),
         List.of("fastest", "safest", "saver"),
         this.settings.strategy,
         value -> this.settings.strategy = value
      );
      this.verbosityDropdown = new autismclient.gui.ui.widgets.DropdownMenu(
         autismclient.gui.f.NetHelper.init(0, 0, 0, 0),
         List.of("quiet", "normal", "verbose"),
         this.settings.verbosity,
         value -> this.settings.verbosity = value
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
         String toolsCommand = autismclient.commands.CommandManager.getPrefix() + "tools";
         String title = "AI TOOLS  ·  " + toolsCommand + " defaults";
         String help = "Read when a " + toolsCommand + " op starts; Esc discards, Done saves.";
         int margin = 6;
         int panelW = Math.min(sw - margin * 2, this.panelWidthFor(ctx, title, help));
         int panelH = this.init(ctx, title);
         autismclient.gui.f.NetHelper panel = autismclient.gui.f.NetHelper.init(
            (sw - panelW) / 2, Math.max(margin, (sh - panelH) / 2), panelW, Math.min(panelH, sh - margin * 2)
         );
         autismclient.gui.f.b.BufferHelper.init_V(ctx, panel, title);
         int pad = 4;
         int rowH = this.rowHeight();
         int labelH = this.labelHeight();
         int rowStep = rowH + 2;
         int sectionGap = 4;
         int x = panel.read() + pad;
         int y = autismclient.gui.f.b.BufferHelper.init_I(ctx, panel, title) + 2;
         int w = panel.update() - pad * 2;
         autismclient.gui.ui.UiColorScheme colors = ctx.init_TableIndex().colors();
         this.strategyRow = autismclient.gui.f.NetHelper.init(x, y, w, rowH);
         this.renderDropdownRow(ctx, this.strategyRow, "Pathfinding strategy", this.strategyDropdown, this.settings.strategy, mx, my);
         y += rowStep;

         for (int i = 0; i < 4; i++) {
            this.toggleRows[i] = autismclient.gui.f.NetHelper.init(x, y, w, rowH);
            y += rowStep;
         }

         ToggleSwitchRenderer.drawToggle(ctx, this.toggleRows[0], TOGGLE_LABELS[0], this.settings.allowDig, this.toggleRows[0].init_Z(mx, my), "aitools:allowDig");
         ToggleSwitchRenderer.drawToggle(
            ctx,
            this.toggleRows[1],
            TOGGLE_LABELS[1],
            this.settings.allowPillar,
            this.toggleRows[1].init_Z(mx, my),
            "aitools:allowPillar"
         );
         ToggleSwitchRenderer.drawToggle(
            ctx,
            this.toggleRows[2],
            TOGGLE_LABELS[2],
            this.settings.allowBridge,
            this.toggleRows[2].init_Z(mx, my),
            "aitools:allowBridge"
         );
         ToggleSwitchRenderer.drawToggle(
            ctx,
            this.toggleRows[3],
            TOGGLE_LABELS[3],
            this.settings.allowInventory,
            this.toggleRows[3].init_Z(mx, my),
            "aitools:allowInventory"
         );
         y += sectionGap;
         String reluctanceLabel = "Dig reluctance (per block)";
         String reluctanceValue = this.settings.digReluctance + "t";
         int reluctanceTextY = ctx.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(x, y, w, labelH));
         int reluctanceValueW = ctx.init_PoolEntry().measureWidth(reluctanceValue);
         ctx.init_PoolEntry().drawFitted(graphics, reluctanceLabel, x, reluctanceTextY, Math.max(1, w - reluctanceValueW - sectionGap), colors.textSecondary);
         ctx.init_PoolEntry().drawFitted(graphics, reluctanceValue, x + w - reluctanceValueW, reluctanceTextY, reluctanceValueW, colors.textPrimary);
         y += labelH;
         this.timeoutSlider = autismclient.gui.f.NetHelper.init(x, y, w, 12);
         autismclient.gui.ui.widgets.SliderRenderer.draw(
            ctx,
            this.timeoutSlider,
            autismclient.gui.ui.widgets.SliderRenderer.ratio(this.settings.digReluctance, 0.0, 60.0),
            this.timeoutSlider.init_Z(mx, my) || this.draggingDigReluctance
         );
         y += 12 + sectionGap;
         this.toggleRows[4] = autismclient.gui.f.NetHelper.init(x, y, w, rowH);
         y += rowStep;
         this.toggleRows[5] = autismclient.gui.f.NetHelper.init(x, y, w, rowH);
         y += rowStep;
         ToggleSwitchRenderer.drawToggle(ctx, this.toggleRows[4], TOGGLE_LABELS[4], this.silentAim, this.toggleRows[4].init_Z(mx, my), "aitools:silentAim");
         ToggleSwitchRenderer.drawToggle(
            ctx, this.toggleRows[5], TOGGLE_LABELS[5], this.settings.useShield, this.toggleRows[5].init_Z(mx, my), "aitools:useShield"
         );
         y += sectionGap;
         String timeoutLabel = "Default timeout";
         String timeoutValue = this.settings.timeoutSeconds + "s";
         int timeoutTextY = ctx.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(x, y, w, labelH));
         int valueW = ctx.init_PoolEntry().measureWidth(timeoutValue);
         ctx.init_PoolEntry().drawFitted(graphics, timeoutLabel, x, timeoutTextY, Math.max(1, w - valueW - sectionGap), colors.textSecondary);
         ctx.init_PoolEntry().drawFitted(graphics, timeoutValue, x + w - valueW, timeoutTextY, valueW, colors.textPrimary);
         y += labelH;
         this.digReluctanceSlider = autismclient.gui.f.NetHelper.init(x, y, w, 12);
         autismclient.gui.ui.widgets.SliderRenderer.draw(
            ctx,
            this.digReluctanceSlider,
            autismclient.gui.ui.widgets.SliderRenderer.ratio(this.settings.timeoutSeconds, 30.0, 1800.0),
            this.digReluctanceSlider.init_Z(mx, my) || this.draggingTimeout
         );
         y += 12 + sectionGap;
         this.verbosityRow = autismclient.gui.f.NetHelper.init(x, y, w, rowH);
         this.renderDropdownRow(ctx, this.verbosityRow, "Chat verbosity", this.verbosityDropdown, this.settings.verbosity, mx, my);
         y += rowH + sectionGap;
         ctx.init_PoolEntry().drawFitted(graphics, help, x, ctx.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(x, y, w, labelH)), w, colors.textSecondary);
         int ctrlH = this.controlHeight();
         int footerY = panel.load() - pad - ctrlH;
         int bw = (w - 2) / 2;
         this.resetButton = autismclient.gui.f.NetHelper.init(x, footerY, bw, ctrlH);
         this.doneButton = autismclient.gui.f.NetHelper.init(x + bw + 2, footerY, w - bw - 2, ctrlH);
         autismclient.gui.f.b.CachePool.init(ctx, this.resetButton, "Reset", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.resetButton.init_Z(mx, my), false);
         autismclient.gui.f.b.CachePool.init(ctx, this.doneButton, "Done", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this.doneButton.init_Z(mx, my), false);
         this.strategyDropdown.render(ctx);
         this.verbosityDropdown.render(ctx);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int labelHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int controlHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int contentHeight() {
      int rowStep = this.rowHeight() + 2;
      int sectionGap = 4;
      int labelH = this.labelHeight();
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
         + this.rowHeight()
         + sectionGap
         + labelH
         + sectionGap
         + this.controlHeight()
         + 4;
   }

   private int init(autismclient.gui.f.SyncManager ctx, String title) {
      int headerOffset = autismclient.gui.f.b.BufferHelper.init_I(ctx, autismclient.gui.f.NetHelper.init(0, 0, 300, 100), title);
      return headerOffset + this.contentHeight();
   }

   private int panelWidthFor(autismclient.gui.f.SyncManager ctx, String title, String help) {
      int widest = ctx.init_PoolEntry().measureWidth(help);
      widest = Math.max(widest, ctx.init_PoolEntry().measureWidth(title) + 20);

      for (String label : TOGGLE_LABELS) {
         widest = Math.max(widest, ctx.init_PoolEntry().measureWidth(label) + 40);
      }

      widest = Math.max(widest, ctx.init_PoolEntry().measureWidth("Pathfinding strategy") + 96 + 8);
      return Math.max(200, Math.min(300, widest + 8 + 2));
   }

   private void renderDropdownRow(
      autismclient.gui.f.SyncManager ctx,
      autismclient.gui.f.NetHelper row,
      String label,
      autismclient.gui.ui.widgets.DropdownMenu dropdown,
      String selected,
      int mx,
      int my
   ) {
      autismclient.gui.ui.UiColorScheme colors = ctx.init_TableIndex().colors();
      autismclient.gui.f.NetHelper control = autismclient.gui.f.NetHelper.init(row.init() - 96, row.write(), 96, row.sync());
      ctx.init_PoolEntry()
         .drawFitted(
            ctx.init_GuiGraphicsExtract(), label, row.read() + 1, ctx.init_PoolEntry().centerTextY(row), Math.max(1, control.read() - row.read() - 8), colors.textPrimary
         );
      dropdown.setBounds(control);
      autismclient.gui.ui.widgets.DropdownMenu.renderTrigger(ctx, control, selected, control.init_Z(mx, my), dropdown.isOpen());
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      if (this.strategyDropdown.onMouseClicked(mx, my, Compat263.mouseEventGlfwButton(event)) == autismclient.gui.ui.UiInputResult.HANDLED) {
         return true;
      }

      if (this.verbosityDropdown.onMouseClicked(mx, my, Compat263.mouseEventGlfwButton(event)) == autismclient.gui.ui.UiInputResult.HANDLED) {
         return true;
      }

      for (int i = 0; i < this.toggleRows.length; i++) {
         if (this.toggleRows[i] != null && this.toggleRows[i].init_Z(mx, my)) {
            this.toggleRow(i);
            return true;
         }
      }

      if (this.digReluctanceSlider != null && this.digReluctanceSlider.init_Z(mx, my)) {
         this.draggingTimeout = true;
         this.setTimeoutFromMouse(mx);
         return true;
      } else if (this.timeoutSlider != null && this.timeoutSlider.init_Z(mx, my)) {
         this.draggingDigReluctance = true;
         this.setDigReluctanceFromMouse(mx);
         return true;
      } else if (this.resetButton != null && this.resetButton.init_Z(mx, my)) {
         AiToolSettings defaults = new AiToolSettings();
         this.settings.strategy = defaults.strategy;
         this.settings.allowDig = defaults.allowDig;
         this.settings.allowPillar = defaults.allowPillar;
         this.settings.allowBridge = defaults.allowBridge;
         this.settings.allowInventory = defaults.allowInventory;
         this.settings.digReluctance = defaults.digReluctance;
         this.settings.useShield = defaults.useShield;
         this.settings.timeoutSeconds = defaults.timeoutSeconds;
         this.settings.verbosity = defaults.verbosity;
         this.strategyDropdown.setSelectedOption(this.settings.strategy);
         this.verbosityDropdown.setSelectedOption(this.settings.verbosity);
         return true;
      } else if (this.doneButton != null && this.doneButton.init_Z(mx, my)) {
         this.applyAndClose();
         this.onClose();
         return true;
      } else {
         return true;
      }
   }

   private void toggleRow(int row) {
      switch (row) {
         case 0:
            this.settings.allowDig = !this.settings.allowDig;
            break;
         case 1:
            this.settings.allowPillar = !this.settings.allowPillar;
            break;
         case 2:
            this.settings.allowBridge = !this.settings.allowBridge;
            break;
         case 3:
            this.settings.allowInventory = !this.settings.allowInventory;
            break;
         case 4:
            this.silentAim = !this.silentAim;
            break;
         case 5:
            this.settings.useShield = !this.settings.useShield;
      }
   }

   private void setTimeoutFromMouse(int mx) {
      this.settings.timeoutSeconds = (int)autismclient.gui.ui.widgets.SliderRenderer.valueAt(
         mx, this.digReluctanceSlider.read(), this.digReluctanceSlider.update(), 30.0, 1800.0, 30.0
      );
   }

   private void setDigReluctanceFromMouse(int mx) {
      this.settings.digReluctance = (int)autismclient.gui.ui.widgets.SliderRenderer.valueAt(mx, this.timeoutSlider.read(), this.timeoutSlider.update(), 0.0, 60.0, 1.0);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      if (this.draggingTimeout) {
         this.setTimeoutFromMouse(cA.init_I_D(event.x()));
         return true;
      } else if (this.draggingDigReluctance) {
         this.setDigReluctanceFromMouse(cA.init_I_D(event.x()));
         return true;
      } else if (this.strategyDropdown.onMouseDragged(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event), dx, dy)
         == autismclient.gui.ui.UiInputResult.HANDLED) {
         return true;
      } else {
         return this.verbosityDropdown.onMouseDragged(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event), dx, dy)
               == autismclient.gui.ui.UiInputResult.HANDLED
            ? true
            : true;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.draggingTimeout = false;
      this.draggingDigReluctance = false;
      this.strategyDropdown.onMouseReleased(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
      this.verbosityDropdown.onMouseReleased(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
      return true;
   }

   private void applyAndClose() {
      AiDirectOps.saveToolSettings(this.settings.copy());
      AiDirectOps.editSettings(settings -> settings.aiSilentAim = this.silentAim);
      bi.load("AI tool settings saved");
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.parent);
   }
}
