package autismclient.compat;

import autismclient.compat263.Compat263;
import autismclient.gui.screens.ThemeColorScreen;
import autismclient.gui.ui.UiClipStack;
import autismclient.gui.f.NetHelper;
import autismclient.gui.f.PacketUtil;
import autismclient.gui.f.SyncManager;
import autismclient.gui.f.UiHelper;
import autismclient.gui.ui.widgets.DropdownMenu;
import autismclient.gui.ui.widgets.ScrollbarRenderer;
import autismclient.gui.f.b.k;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.util.AutismConfig;
import autismclient.util.ValueSink;
import autismclient.util.aJ;
import autismclient.util.cA;
import autismclient.util.ThemePaletteManager;
import autismclient.util.CommandPrefixHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class AutismSettingsScreen extends Screen {
   private static final int init_I = -791321;
   private static final int load_I = -4743522;
   private static final int read = 14;
   private static final int write = 2;
   private static final int update = 16;
   private static final int sync = 14;
   private static final int FOOTER_HEIGHT = 17;
   private static final int close = 4;
   private static final int open = 6;
   private static final int CONTROL_WIDTH = 110;
   private static final int MIN_PANEL_WIDTH = 200;
   private final Screen parent;
   private final List<AutismSettingsScreen.KeybindEntry> keybinds = new ArrayList<>();
   private int listeningKeybind = -1;
   private int scrollOffset;
   private boolean init_Z;
   private int scrollGrabOffset;
   private final List<AutismSettingsScreen.ClickRegion> clickRegions = new ArrayList<>();
   private NetHelper listBounds = NetHelper.init(0, 0, 0, 0);
   private int contentHeight;
   private int labelColumnWidth;
   private SyncManager ui;
   private DropdownMenu dropdown;

   public AutismSettingsScreen(Screen parent) {
      super(Component.literal("AUTISM Client Settings"));
      this.parent = parent;
      AutismConfig cfg = AutismConfig.getGlobal();
      this.keybinds.add(new AutismSettingsScreen.KeybindEntry("Module Menu", () -> cfg.keybindModuleMenu, v -> {
         cfg.keybindModuleMenu = v;
         cfg.save();
      }));
      this.keybinds.add(new AutismSettingsScreen.KeybindEntry("Load GUI", () -> cfg.keybindLoadGui, v -> {
         cfg.keybindLoadGui = v;
         cfg.save();
      }));
      this.keybinds.add(new AutismSettingsScreen.KeybindEntry("Flush Queue", () -> cfg.keybindFlushQueue, v -> {
         cfg.keybindFlushQueue = v;
         cfg.save();
      }));
      this.keybinds.add(new AutismSettingsScreen.KeybindEntry("Clear Queue", () -> cfg.keybindClearQueue, v -> {
         cfg.keybindClearQueue = v;
         cfg.save();
      }));
      this.keybinds.add(new AutismSettingsScreen.KeybindEntry("Toggle Logger", () -> cfg.keybindToggleLogger, v -> {
         cfg.keybindToggleLogger = v;
         cfg.save();
      }));
      this.keybinds.add(new AutismSettingsScreen.KeybindEntry("Toggle Send", () -> cfg.keybindToggleSend, v -> {
         cfg.keybindToggleSend = v;
         cfg.save();
      }));
      this.keybinds.add(new AutismSettingsScreen.KeybindEntry("Toggle Delay", () -> cfg.keybindToggleDelay, v -> {
         cfg.keybindToggleDelay = v;
         cfg.save();
      }));
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
         this.clickRegions.clear();
         this.ui = UiHelper.init(graphics, this.font, mx, my);
         int sw = cA.init_I();
         int sh = cA.load_I();
         PacketUtil.init(graphics, NetHelper.init(0, 0, sw, sh), -267843831);
         List<AutismSettingsScreen.SettingsRow> rows = this.buildRows();
         int margin = 6;
         int contentW = 6 + this.labelColumnWidth + 12 + 110 + 2 + 6;
         int titleW = k.init(this.font, "AUTISM Client Settings", autismclient.gui.f.a.DataUtil.read, -791321) + 16;
         int panelW = Math.min(sw - margin * 2, Math.max(200, Math.max(titleW, contentW + 8)));
         int panelH = Math.min(sh - margin * 2, 31 + rows.size() * 16 + 8);
         panelH = Math.max(panelH, 47);
         int px = (sw - panelW) / 2;
         int py = (sh - panelH) / 2;
         NetHelper panel = NetHelper.init(px, py, panelW, panelH);
         PacketUtil.load(graphics, panel, 5, -435549684, this.outlineColor());
         NetHelper header = NetHelper.init(px, py, panelW, 14);
         PacketUtil.read(graphics, NetHelper.init(px + 1, py + 1, panelW - 2, 13), 4, this.init(858003997, ThemePaletteManager.ColorRole.HEADER));
         this.drawCentered(graphics, "AUTISM Client Settings", autismclient.gui.f.a.DataUtil.read, this.init(-791321, ThemePaletteManager.ColorRole.TEXT), header);
         NetHelper footer = NetHelper.init(px, panel.load() - 17, panelW, 17);
         NetHelper done = NetHelper.init(px + 4, footer.write(), panelW - 8, 13);
         this.drawButton(graphics, done, "Done", done.init_Z(mx, my), true);
         this.addClickRegion(done, this::onClose, null);
         int contentTop = py + 14 + 4;
         int contentBottom = footer.write() - 4;
         this.listBounds = NetHelper.init(px + 4, contentTop, panelW - 8, Math.max(0, contentBottom - contentTop));
         this.contentHeight = rows.size() * 16;
         this.scrollOffset = this.clampScroll(this.scrollOffset);
         boolean needsBar = this.contentHeight > this.listBounds.sync();
         int rowW = this.listBounds.update() - (needsBar ? 6 : 0);
         UiClipStack.getInstance().push(graphics, this.listBounds);

         try {
            for (int i = 0; i < rows.size(); i++) {
               int ry = this.listBounds.write() - this.scrollOffset + i * 16;
               if (ry + 14 >= this.listBounds.write() && ry <= this.listBounds.load()) {
                  NetHelper rb = NetHelper.init(this.listBounds.read(), ry, rowW, 14);
                  boolean visible = ry >= this.listBounds.write() - 1 && ry + 14 <= this.listBounds.load() + 1;
                  rows.get(i).render(graphics, rb, mx, my, visible);
               }
            }
         } finally {
            UiClipStack.getInstance().pop(graphics);
         }

         if (needsBar) {
            ScrollbarRenderer.ScrollbarBounds metrics = this.scrollbarBounds();
            ScrollbarRenderer.init(this.ui, metrics, metrics.init(mx, my), this.init_Z);
            this.addClickRegion(metrics.track(), () -> this.init(metrics, my), null);
         }

         if (this.dropdown != null && this.dropdown.isOpen()) {
            this.dropdown.render(this.ui);
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private List<AutismSettingsScreen.SettingsRow> buildRows() {
      AutismConfig cfg = AutismConfig.getGlobal();
      ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
      List<AutismSettingsScreen.SettingsRow> out = new ArrayList<>();
      this.labelColumnWidth = this.labelWidth("Command Prefix");

      for (AutismSettingsScreen.KeybindEntry k : this.keybinds) {
         this.labelColumnWidth = Math.max(this.labelColumnWidth, this.labelWidth(k.label()));
      }

      out.add(this.keybindRow("Open Inside GUI", () -> cfg.keybindInsideGui, v -> {
         cfg.keybindInsideGui = v;
         cfg.save();
      }));
      out.add(this.keybindRow("Custom Main Menu", () -> cfg.customMainMenu, v -> {
         cfg.customMainMenu = v;
         cfg.save();
      }));
      out.add(this.keybindRow("Auto Probe Plugins", () -> cfg.autoProbePlugins, v -> {
         cfg.autoProbePlugins = v;
         cfg.save();
      }));
      out.add(this.keybindRow("InfiniChat", () -> cfg.infiniChat, v -> {
         cfg.infiniChat = v;
         cfg.save();
      }));
      out.add(this.keybindRow("Stop On Leave", () -> cfg.stopMacroOnLeave, v -> {
         cfg.stopMacroOnLeave = v;
         cfg.save();
      }));
      out.add(this.cycleRow("Overlay Scale", cA.init_Str(), () -> cA.init_V_D(cA.load_D()), () -> cA.init_V_D(cA.read())));
      List<String> prefixes = new ArrayList<>(CommandPrefixHelper.ALLOWED_PREFIXES);
      String cur = CommandPrefixHelper.currentPrefix();
      if (!prefixes.contains(cur)) {
         cur = prefixes.isEmpty() ? "." : prefixes.get(0);
      }

      out.add(this.prefixRow(module, prefixes, cur));

      for (int i = 0; i < this.keybinds.size(); i++) {
         out.add(this.init_DataUtil_BufferHel(i));
      }

      out.add(this.init("Theme Color", () -> this.minecraft.gui.setScreen(new ThemeColorScreen(this))));
      out.add(this.init("Addons", () -> this.minecraft.gui.setScreen(new autismclient.gui.screens.AddonsScreen(this))));
      out.add(this.init("Discord", () -> aJ.init_V("https://discord.com/invite/JZ7XgUCtBu")));
      return out;
   }

   private AutismSettingsScreen.SettingsRow keybindRow(String label, AutismSettingsScreen.BoolGetter getter, Consumer<Boolean> setter) {
      this.labelColumnWidth = Math.max(this.labelColumnWidth, this.labelWidth(label));
      return (g, rb, mx, my, clickable) -> {
         boolean on = getter.get();
         boolean hover = clickable && rb.init_Z(mx, my);
         PacketUtil.load(g, rb, 4, hover ? this.init(1075845661, ThemePaletteManager.ColorRole.BUTTON) : 639113758, this.init(1717972012, ThemePaletteManager.ColorRole.OUTLINE));
         this.drawText(g, label, autismclient.gui.f.a.DataUtil.load, this.init(-791321, ThemePaletteManager.ColorRole.TEXT), rb.read() + 6, this.centerTextY(rb));
         NetHelper tog = NetHelper.init(rb.init() - 26, rb.write() + 2, 22, rb.sync() - 4);
         PacketUtil.load(
            g, tog, 4, on ? this.init(-1474094563, ThemePaletteManager.ColorRole.TOGGLE) : 1074926358, on ? this.init(-39836, ThemePaletteManager.ColorRole.TOGGLE) : 1716936038
         );
         int knobX = on ? tog.init() - 10 : tog.read() + 2;
         PacketUtil.init(g, NetHelper.init(knobX, tog.write() + 2, 8, tog.sync() - 4), on ? this.init(-50373, ThemePaletteManager.ColorRole.TOGGLE) : -7697782);
         if (clickable) {
            this.addClickRegion(rb, () -> setter.accept(!getter.get()), null);
         }
      };
   }

   private AutismSettingsScreen.SettingsRow cycleRow(String label, String value, Runnable onLeft, Runnable onRight) {
      this.labelColumnWidth = Math.max(this.labelColumnWidth, this.labelWidth(label));
      return (g, rb, mx, my, clickable) -> {
         boolean hover = clickable && rb.init_Z(mx, my);
         PacketUtil.load(g, rb, 4, hover ? this.init(1075845661, ThemePaletteManager.ColorRole.BUTTON) : 639113758, this.init(1717972012, ThemePaletteManager.ColorRole.OUTLINE));
         this.drawText(g, label, autismclient.gui.f.a.DataUtil.load, this.init(-791321, ThemePaletteManager.ColorRole.TEXT), rb.read() + 6, this.centerTextY(rb));
         int vw = k.init(this.font, value, autismclient.gui.f.a.DataUtil.init, -4743522);
         this.drawText(g, value, autismclient.gui.f.a.DataUtil.init, this.init(-39836, ThemePaletteManager.ColorRole.ACCENT), rb.init() - vw - 6, this.centerTextY(rb));
         if (clickable) {
            this.addClickRegion(rb, onLeft, onRight);
         }
      };
   }

   private AutismSettingsScreen.SettingsRow prefixRow(ModuleRuntimeManager module, List<String> prefixes, String current) {
      if (this.dropdown == null) {
         this.dropdown = new DropdownMenu(NetHelper.init(0, 0, 0, 0), prefixes, current, module::setCommandPrefix);
      } else {
         this.dropdown.setOptions(prefixes);
         this.dropdown.setSelectedOption(current);
      }

      return (g, rb, mx, my, clickable) -> {
         PacketUtil.load(g, rb, 4, 639113758, this.init(1717972012, ThemePaletteManager.ColorRole.OUTLINE));
         this.drawText(g, "Command Prefix", autismclient.gui.f.a.DataUtil.load, this.init(-791321, ThemePaletteManager.ColorRole.TEXT), rb.read() + 6, this.centerTextY(rb));
         int ctrlW = Math.min(110, rb.update() / 2);
         NetHelper ctrl = NetHelper.init(rb.init() - ctrlW - 2, rb.write() + 1, ctrlW, rb.sync() - 2);
         this.dropdown.setBounds(ctrl);
         boolean hover = clickable && ctrl.init_Z(mx, my);
         DropdownMenu.renderTrigger(this.ui, ctrl, current, hover, this.dropdown.isOpen());
         if (clickable) {
            this.addClickRegion(ctrl, () -> {
               if (!this.dropdown.isOpen()) {
                  this.dropdown.open();
               }
            }, null);
         } else if (this.dropdown.isOpen()) {
            this.dropdown.close();
         }
      };
   }

   private AutismSettingsScreen.SettingsRow init_DataUtil_BufferHel(int idx) {
      return (g, rb, mx, my, clickable) -> {
         AutismSettingsScreen.KeybindEntry k = this.keybinds.get(idx);
         boolean hover = clickable && rb.init_Z(mx, my);
         boolean active = this.listeningKeybind == idx;
         PacketUtil.load(
            g,
            rb,
            4,
            active ? this.init(1428429341, ThemePaletteManager.ColorRole.ACCENT) : (hover ? this.init(1075845661, ThemePaletteManager.ColorRole.BUTTON) : 639113758),
            active ? this.init(-39836, ThemePaletteManager.ColorRole.ACCENT) : this.init(1717972012, ThemePaletteManager.ColorRole.OUTLINE)
         );
         this.drawText(g, k.label(), autismclient.gui.f.a.DataUtil.load, this.init(-791321, ThemePaletteManager.ColorRole.TEXT), rb.read() + 6, this.centerTextY(rb));
         String v = active ? "press a key..." : ValueSink.init_Str(k.getter().getAsInt());
         int vw = autismclient.gui.f.b.k.init(this.font, v, autismclient.gui.f.a.DataUtil.init, -4743522);
         this.drawText(g, v, autismclient.gui.f.a.DataUtil.init, this.init(-4743522, ThemePaletteManager.ColorRole.TEXT), rb.init() - vw - 6, this.centerTextY(rb));
         if (clickable) {
            this.addClickRegion(rb, () -> this.startCapture(idx), null);
         }
      };
   }

   private AutismSettingsScreen.SettingsRow init(String label, Runnable onClick) {
      return (g, rb, mx, my, clickable) -> {
         this.drawButton(g, rb, label, clickable && rb.init_Z(mx, my), false);
         if (clickable) {
            this.addClickRegion(rb, onClick, null);
         }
      };
   }

   private void addClickRegion(NetHelper bounds, Runnable onLeft, Runnable onRight) {
      this.clickRegions.add(new AutismSettingsScreen.ClickRegion(bounds, onLeft, onRight));
   }

   private int clampScroll(int value) {
      int max = Math.max(0, this.contentHeight - this.listBounds.sync());
      return Math.max(0, Math.min(value, max));
   }

   private ScrollbarRenderer.ScrollbarBounds scrollbarBounds() {
      NetHelper track = NetHelper.init(this.listBounds.init() - 6, this.listBounds.write(), 6, this.listBounds.sync());
      return ScrollbarRenderer.init(track, this.contentHeight, this.listBounds.sync(), this.scrollOffset);
   }

   private void init(ScrollbarRenderer.ScrollbarBounds metrics, int my) {
      this.init_Z = true;
      this.scrollGrabOffset = metrics.load(metrics.track().read() + 1, my) ? my - metrics.load().write() : metrics.load().sync() / 2;
      this.scrollOffset = this.clampScroll(ScrollbarRenderer.scrollForDrag(metrics, my, this.scrollGrabOffset));
   }

   private int init(int argb, ThemePaletteManager.ColorRole ch) {
      return ThemePaletteManager.recolor(argb, ch);
   }

   private int outlineColor() {
      return this.init(-5035221, ThemePaletteManager.ColorRole.OUTLINE);
   }

   private void drawButton(GuiGraphicsExtractor g, NetHelper b, String label, boolean hover, boolean primary) {
      autismclient.gui.f.b.CachePool.init(
         this.ui,
         b,
         label,
         primary ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         hover,
         false
      );
   }

   private void drawCentered(GuiGraphicsExtractor g, String text, Identifier fontId, int color, NetHelper area) {
      int tw = k.init(this.font, text, fontId, color);
      this.drawText(g, text, fontId, color, area.read() + (area.update() - tw) / 2, this.ui.init_PoolEntry().centerTextY(area));
   }

   private int centerTextY(NetHelper rb) {
      return this.ui.init_PoolEntry().centerTextY(rb);
   }

   private int labelWidth(String label) {
      return k.init(this.font, label, autismclient.gui.f.a.DataUtil.load, -791321);
   }

   private void drawText(GuiGraphicsExtractor g, String text, Identifier fontId, int color, int x, int y) {
      k.init(g, this.font, text, fontId, color, x, y, false);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      boolean right = Compat263.mouseEventGlfwButton(event) == 1;
      if (!right && Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      if (this.listeningKeybind >= 0) {
         this.bindListening(ValueSink.init_I(Compat263.mouseEventGlfwButton(event)));
         return true;
      }

      if (this.dropdown != null && this.dropdown.isOpen()) {
         this.dropdown.onMouseClicked(mx, my, Compat263.mouseEventGlfwButton(event));
         return true;
      }

      for (int i = this.clickRegions.size() - 1; i >= 0; i--) {
         AutismSettingsScreen.ClickRegion hit = this.clickRegions.get(i);
         if (hit.bounds().init_Z(mx, my)) {
            Runnable action = right ? hit.load() : hit.onLeft();
            if (action != null) {
               action.run();
            }

            return true;
         }
      }

      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.init_Z = false;
      if (this.dropdown != null && this.dropdown.isOpen()) {
         this.dropdown.onMouseReleased(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
         return true;
      } else {
         return super.mouseReleased(event);
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      if (this.init_Z) {
         this.scrollOffset = this.clampScroll(ScrollbarRenderer.scrollForDrag(this.scrollbarBounds(), cA.init_I_D(event.y()), this.scrollGrabOffset));
         return true;
      } else if (this.dropdown != null && this.dropdown.isOpen()) {
         this.dropdown.onMouseDragged(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event), dx, dy);
         return true;
      } else {
         return super.mouseDragged(event, dx, dy);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.dropdown != null && this.dropdown.isOpen()) {
         this.dropdown.onMouseScrolled(cA.init_I_D(mouseX), cA.init_I_D(mouseY), scrollY);
         return true;
      }

      if (scrollY != 0.0) {
         this.scrollOffset = this.clampScroll(this.scrollOffset - (int)Math.signum(scrollY) * 16 * 2);
      }

      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (this.listeningKeybind >= 0) {
         this.bindListening(Compat263.keyEventGlfwKey(event) == 256 ? -1 : Compat263.keyEventGlfwKey(event));
         return true;
      }

      if (Compat263.keyEventGlfwKey(event) == 256) {
         if (this.dropdown != null && this.dropdown.isOpen()) {
            this.dropdown.close();
            return true;
         } else {
            this.onClose();
            return true;
         }
      } else {
         return super.keyPressed(event);
      }
   }

   private void startCapture(int idx) {
      this.listeningKeybind = idx;
   }

   private void bindListening(int code) {
      if (this.listeningKeybind >= 0 && this.listeningKeybind < this.keybinds.size()) {
         this.keybinds.get(this.listeningKeybind).setter().accept(code);
         this.listeningKeybind = -1;
      }
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.parent);
   }

   private interface SettingsRow {
      void render(GuiGraphicsExtractor var1, NetHelper var2, int var3, int var4, boolean var5);
   }

   private record KeybindEntry(String label, IntSupplier getter, IntConsumer setter) {
   }

   private record ClickRegion(NetHelper bounds, Runnable onLeft, Runnable load) {
   }

   private interface BoolGetter {
      boolean get();
   }
}
