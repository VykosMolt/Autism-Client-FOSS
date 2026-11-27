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
   private static final int flush = 17;
   private static final int close = 4;
   private static final int open = 6;
   private static final int poll = 110;
   private static final int push = 200;
   private final Screen init_Screen;
   private final List<AutismSettingsScreen.CachePool> init_List = new ArrayList<>();
   private int pop = -1;
   private int tick;
   private boolean init_Z;
   private int run;
   private final List<AutismSettingsScreen.Inner_DataUtil> load_List = new ArrayList<>();
   private NetHelper init_NetHelper = NetHelper.init(0, 0, 0, 0);
   private int get;
   private int set;
   private SyncManager init_SyncManager;
   private DropdownMenu init_RefQueue;

   public AutismSettingsScreen(Screen parent) {
      super(Component.literal("AUTISM Client Settings"));
      this.init_Screen = parent;
      AutismConfig cfg = AutismConfig.getGlobal();
      this.init_List.add(new AutismSettingsScreen.CachePool("Module Menu", () -> cfg.keybindModuleMenu, v -> {
         cfg.keybindModuleMenu = v;
         cfg.save();
      }));
      this.init_List.add(new AutismSettingsScreen.CachePool("Load GUI", () -> cfg.keybindLoadGui, v -> {
         cfg.keybindLoadGui = v;
         cfg.save();
      }));
      this.init_List.add(new AutismSettingsScreen.CachePool("Flush Queue", () -> cfg.keybindFlushQueue, v -> {
         cfg.keybindFlushQueue = v;
         cfg.save();
      }));
      this.init_List.add(new AutismSettingsScreen.CachePool("Clear Queue", () -> cfg.keybindClearQueue, v -> {
         cfg.keybindClearQueue = v;
         cfg.save();
      }));
      this.init_List.add(new AutismSettingsScreen.CachePool("Toggle Logger", () -> cfg.keybindToggleLogger, v -> {
         cfg.keybindToggleLogger = v;
         cfg.save();
      }));
      this.init_List.add(new AutismSettingsScreen.CachePool("Toggle Send", () -> cfg.keybindToggleSend, v -> {
         cfg.keybindToggleSend = v;
         cfg.save();
      }));
      this.init_List.add(new AutismSettingsScreen.CachePool("Toggle Delay", () -> cfg.keybindToggleDelay, v -> {
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
         this.load_List.clear();
         this.init_SyncManager = UiHelper.init(graphics, this.font, mx, my);
         int sw = cA.init_I();
         int sh = cA.load_I();
         PacketUtil.init(graphics, NetHelper.init(0, 0, sw, sh), -267843831);
         List<AutismSettingsScreen.BufferHelper> rows = this.init_List();
         int margin = 6;
         int contentW = 6 + this.set + 12 + 110 + 2 + 6;
         int titleW = k.init(this.font, "AUTISM Client Settings", autismclient.gui.f.a.DataUtil.read, -791321) + 16;
         int panelW = Math.min(sw - margin * 2, Math.max(200, Math.max(titleW, contentW + 8)));
         int panelH = Math.min(sh - margin * 2, 31 + rows.size() * 16 + 8);
         panelH = Math.max(panelH, 47);
         int px = (sw - panelW) / 2;
         int py = (sh - panelH) / 2;
         NetHelper panel = NetHelper.init(px, py, panelW, panelH);
         PacketUtil.load(graphics, panel, 5, -435549684, this.init_I());
         NetHelper header = NetHelper.init(px, py, panelW, 14);
         PacketUtil.read(graphics, NetHelper.init(px + 1, py + 1, panelW - 2, 13), 4, this.init(858003997, ThemePaletteManager.ColorRole.HEADER));
         this.init(graphics, "AUTISM Client Settings", autismclient.gui.f.a.DataUtil.read, this.init(-791321, ThemePaletteManager.ColorRole.TEXT), header);
         NetHelper footer = NetHelper.init(px, panel.load() - 17, panelW, 17);
         NetHelper done = NetHelper.init(px + 4, footer.write(), panelW - 8, 13);
         this.init(graphics, done, "Done", done.init_Z(mx, my), true);
         this.init_V_NetHelper(done, this::onClose, null);
         int contentTop = py + 14 + 4;
         int contentBottom = footer.write() - 4;
         this.init_NetHelper = NetHelper.init(px + 4, contentTop, panelW - 8, Math.max(0, contentBottom - contentTop));
         this.get = rows.size() * 16;
         this.tick = this.init_I(this.tick);
         boolean needsBar = this.get > this.init_NetHelper.sync();
         int rowW = this.init_NetHelper.update() - (needsBar ? 6 : 0);
         UiClipStack.getInstance().push(graphics, this.init_NetHelper);

         try {
            for (int i = 0; i < rows.size(); i++) {
               int ry = this.init_NetHelper.write() - this.tick + i * 16;
               if (ry + 14 >= this.init_NetHelper.write() && ry <= this.init_NetHelper.load()) {
                  NetHelper rb = NetHelper.init(this.init_NetHelper.read(), ry, rowW, 14);
                  boolean visible = ry >= this.init_NetHelper.write() - 1 && ry + 14 <= this.init_NetHelper.load() + 1;
                  rows.get(i).render(graphics, rb, mx, my, visible);
               }
            }
         } finally {
            UiClipStack.getInstance().pop(graphics);
         }

         if (needsBar) {
            ScrollbarRenderer.ScrollbarBounds metrics = this.init_UtilCore_DataUtil();
            ScrollbarRenderer.init(this.init_SyncManager, metrics, metrics.init(mx, my), this.init_Z);
            this.init_V_NetHelper(metrics.track(), () -> this.init(metrics, my), null);
         }

         if (this.init_RefQueue != null && this.init_RefQueue.isOpen()) {
            this.init_RefQueue.render(this.init_SyncManager);
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private List<AutismSettingsScreen.BufferHelper> init_List() {
      AutismConfig cfg = AutismConfig.getGlobal();
      ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
      List<AutismSettingsScreen.BufferHelper> out = new ArrayList<>();
      this.set = this.init("Command Prefix");

      for (AutismSettingsScreen.CachePool k : this.init_List) {
         this.set = Math.max(this.set, this.init(k.init_Str()));
      }

      out.add(this.init_DataUtil_BufferHel_Str("Open Inside GUI", () -> cfg.keybindInsideGui, v -> {
         cfg.keybindInsideGui = v;
         cfg.save();
      }));
      out.add(this.init_DataUtil_BufferHel_Str("Custom Main Menu", () -> cfg.customMainMenu, v -> {
         cfg.customMainMenu = v;
         cfg.save();
      }));
      out.add(this.init_DataUtil_BufferHel_Str("Auto Probe Plugins", () -> cfg.autoProbePlugins, v -> {
         cfg.autoProbePlugins = v;
         cfg.save();
      }));
      out.add(this.init_DataUtil_BufferHel_Str("InfiniChat", () -> cfg.infiniChat, v -> {
         cfg.infiniChat = v;
         cfg.save();
      }));
      out.add(this.init_DataUtil_BufferHel_Str("Stop On Leave", () -> cfg.stopMacroOnLeave, v -> {
         cfg.stopMacroOnLeave = v;
         cfg.save();
      }));
      out.add(this.init("Overlay Scale", cA.init_Str(), () -> cA.init_V_D(cA.load_D()), () -> cA.init_V_D(cA.read())));
      List<String> prefixes = new ArrayList<>(CommandPrefixHelper.ALLOWED_PREFIXES);
      String cur = CommandPrefixHelper.currentPrefix();
      if (!prefixes.contains(cur)) {
         cur = prefixes.isEmpty() ? "." : prefixes.get(0);
      }

      out.add(this.init_DataUtil_BufferHel_PoolEntry(module, prefixes, cur));

      for (int i = 0; i < this.init_List.size(); i++) {
         out.add(this.init_DataUtil_BufferHel(i));
      }

      out.add(this.init("Theme Color", () -> this.minecraft.gui.setScreen(new ThemeColorScreen(this))));
      out.add(this.init("Addons", () -> this.minecraft.gui.setScreen(new autismclient.gui.screens.AddonsScreen(this))));
      out.add(this.init("Discord", () -> aJ.init_V("https://discord.com/invite/JZ7XgUCtBu")));
      return out;
   }

   private AutismSettingsScreen.BufferHelper init_DataUtil_BufferHel_Str(String label, AutismSettingsScreen.StreamUtil getter, Consumer<Boolean> setter) {
      this.set = Math.max(this.set, this.init(label));
      return (g, rb, mx, my, clickable) -> {
         boolean on = getter.get();
         boolean hover = clickable && rb.init_Z(mx, my);
         PacketUtil.load(g, rb, 4, hover ? this.init(1075845661, ThemePaletteManager.ColorRole.BUTTON) : 639113758, this.init(1717972012, ThemePaletteManager.ColorRole.OUTLINE));
         this.init(g, label, autismclient.gui.f.a.DataUtil.load, this.init(-791321, ThemePaletteManager.ColorRole.TEXT), rb.read() + 6, this.init(rb));
         NetHelper tog = NetHelper.init(rb.init() - 26, rb.write() + 2, 22, rb.sync() - 4);
         PacketUtil.load(
            g, tog, 4, on ? this.init(-1474094563, ThemePaletteManager.ColorRole.TOGGLE) : 1074926358, on ? this.init(-39836, ThemePaletteManager.ColorRole.TOGGLE) : 1716936038
         );
         int knobX = on ? tog.init() - 10 : tog.read() + 2;
         PacketUtil.init(g, NetHelper.init(knobX, tog.write() + 2, 8, tog.sync() - 4), on ? this.init(-50373, ThemePaletteManager.ColorRole.TOGGLE) : -7697782);
         if (clickable) {
            this.init_V_NetHelper(rb, () -> setter.accept(!getter.get()), null);
         }
      };
   }

   private AutismSettingsScreen.BufferHelper init(String label, String value, Runnable onLeft, Runnable onRight) {
      this.set = Math.max(this.set, this.init(label));
      return (g, rb, mx, my, clickable) -> {
         boolean hover = clickable && rb.init_Z(mx, my);
         PacketUtil.load(g, rb, 4, hover ? this.init(1075845661, ThemePaletteManager.ColorRole.BUTTON) : 639113758, this.init(1717972012, ThemePaletteManager.ColorRole.OUTLINE));
         this.init(g, label, autismclient.gui.f.a.DataUtil.load, this.init(-791321, ThemePaletteManager.ColorRole.TEXT), rb.read() + 6, this.init(rb));
         int vw = k.init(this.font, value, autismclient.gui.f.a.DataUtil.init, -4743522);
         this.init(g, value, autismclient.gui.f.a.DataUtil.init, this.init(-39836, ThemePaletteManager.ColorRole.ACCENT), rb.init() - vw - 6, this.init(rb));
         if (clickable) {
            this.init_V_NetHelper(rb, onLeft, onRight);
         }
      };
   }

   private AutismSettingsScreen.BufferHelper init_DataUtil_BufferHel_PoolEntry(ModuleRuntimeManager module, List<String> prefixes, String current) {
      if (this.init_RefQueue == null) {
         this.init_RefQueue = new DropdownMenu(NetHelper.init(0, 0, 0, 0), prefixes, current, module::setCommandPrefix);
      } else {
         this.init_RefQueue.setOptions(prefixes);
         this.init_RefQueue.setSelectedOption(current);
      }

      return (g, rb, mx, my, clickable) -> {
         PacketUtil.load(g, rb, 4, 639113758, this.init(1717972012, ThemePaletteManager.ColorRole.OUTLINE));
         this.init(g, "Command Prefix", autismclient.gui.f.a.DataUtil.load, this.init(-791321, ThemePaletteManager.ColorRole.TEXT), rb.read() + 6, this.init(rb));
         int ctrlW = Math.min(110, rb.update() / 2);
         NetHelper ctrl = NetHelper.init(rb.init() - ctrlW - 2, rb.write() + 1, ctrlW, rb.sync() - 2);
         this.init_RefQueue.setBounds(ctrl);
         boolean hover = clickable && ctrl.init_Z(mx, my);
         DropdownMenu.renderTrigger(this.init_SyncManager, ctrl, current, hover, this.init_RefQueue.isOpen());
         if (clickable) {
            this.init_V_NetHelper(ctrl, () -> {
               if (!this.init_RefQueue.isOpen()) {
                  this.init_RefQueue.open();
               }
            }, null);
         } else if (this.init_RefQueue.isOpen()) {
            this.init_RefQueue.close();
         }
      };
   }

   private AutismSettingsScreen.BufferHelper init_DataUtil_BufferHel(int idx) {
      return (g, rb, mx, my, clickable) -> {
         AutismSettingsScreen.CachePool k = this.init_List.get(idx);
         boolean hover = clickable && rb.init_Z(mx, my);
         boolean active = this.pop == idx;
         PacketUtil.load(
            g,
            rb,
            4,
            active ? this.init(1428429341, ThemePaletteManager.ColorRole.ACCENT) : (hover ? this.init(1075845661, ThemePaletteManager.ColorRole.BUTTON) : 639113758),
            active ? this.init(-39836, ThemePaletteManager.ColorRole.ACCENT) : this.init(1717972012, ThemePaletteManager.ColorRole.OUTLINE)
         );
         this.init(g, k.init_Str(), autismclient.gui.f.a.DataUtil.load, this.init(-791321, ThemePaletteManager.ColorRole.TEXT), rb.read() + 6, this.init(rb));
         String v = active ? "press a key..." : ValueSink.init_Str(k.init_IntSupplier().getAsInt());
         int vw = autismclient.gui.f.b.k.init(this.font, v, autismclient.gui.f.a.DataUtil.init, -4743522);
         this.init(g, v, autismclient.gui.f.a.DataUtil.init, this.init(-4743522, ThemePaletteManager.ColorRole.TEXT), rb.init() - vw - 6, this.init(rb));
         if (clickable) {
            this.init_V_NetHelper(rb, () -> this.init_V(idx), null);
         }
      };
   }

   private AutismSettingsScreen.BufferHelper init(String label, Runnable onClick) {
      return (g, rb, mx, my, clickable) -> {
         this.init(g, rb, label, clickable && rb.init_Z(mx, my), false);
         if (clickable) {
            this.init_V_NetHelper(rb, onClick, null);
         }
      };
   }

   private void init_V_NetHelper(NetHelper bounds, Runnable onLeft, Runnable onRight) {
      this.load_List.add(new AutismSettingsScreen.Inner_DataUtil(bounds, onLeft, onRight));
   }

   private int init_I(int value) {
      int max = Math.max(0, this.get - this.init_NetHelper.sync());
      return Math.max(0, Math.min(value, max));
   }

   private ScrollbarRenderer.ScrollbarBounds init_UtilCore_DataUtil() {
      NetHelper track = NetHelper.init(this.init_NetHelper.init() - 6, this.init_NetHelper.write(), 6, this.init_NetHelper.sync());
      return ScrollbarRenderer.init(track, this.get, this.init_NetHelper.sync(), this.tick);
   }

   private void init(ScrollbarRenderer.ScrollbarBounds metrics, int my) {
      this.init_Z = true;
      this.run = metrics.load(metrics.track().read() + 1, my) ? my - metrics.load().write() : metrics.load().sync() / 2;
      this.tick = this.init_I(ScrollbarRenderer.scrollForDrag(metrics, my, this.run));
   }

   private int init(int argb, ThemePaletteManager.ColorRole ch) {
      return ThemePaletteManager.recolor(argb, ch);
   }

   private int init_I() {
      return this.init(-5035221, ThemePaletteManager.ColorRole.OUTLINE);
   }

   private void init(GuiGraphicsExtractor g, NetHelper b, String label, boolean hover, boolean primary) {
      autismclient.gui.f.b.CachePool.init(
         this.init_SyncManager,
         b,
         label,
         primary ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         hover,
         false
      );
   }

   private void init(GuiGraphicsExtractor g, String text, Identifier fontId, int color, NetHelper area) {
      int tw = k.init(this.font, text, fontId, color);
      this.init(g, text, fontId, color, area.read() + (area.update() - tw) / 2, this.init_SyncManager.init_PoolEntry().centerTextY(area));
   }

   private int init(NetHelper rb) {
      return this.init_SyncManager.init_PoolEntry().centerTextY(rb);
   }

   private int init(String label) {
      return k.init(this.font, label, autismclient.gui.f.a.DataUtil.load, -791321);
   }

   private void init(GuiGraphicsExtractor g, String text, Identifier fontId, int color, int x, int y) {
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

      if (this.pop >= 0) {
         this.load(ValueSink.init_I(Compat263.mouseEventGlfwButton(event)));
         return true;
      }

      if (this.init_RefQueue != null && this.init_RefQueue.isOpen()) {
         this.init_RefQueue.onMouseClicked(mx, my, Compat263.mouseEventGlfwButton(event));
         return true;
      }

      for (int i = this.load_List.size() - 1; i >= 0; i--) {
         AutismSettingsScreen.Inner_DataUtil hit = this.load_List.get(i);
         if (hit.init_NetHelper().init_Z(mx, my)) {
            Runnable action = right ? hit.load() : hit.init_Runnable();
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
      if (this.init_RefQueue != null && this.init_RefQueue.isOpen()) {
         this.init_RefQueue.onMouseReleased(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
         return true;
      } else {
         return super.mouseReleased(event);
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      if (this.init_Z) {
         this.tick = this.init_I(ScrollbarRenderer.scrollForDrag(this.init_UtilCore_DataUtil(), cA.init_I_D(event.y()), this.run));
         return true;
      } else if (this.init_RefQueue != null && this.init_RefQueue.isOpen()) {
         this.init_RefQueue.onMouseDragged(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event), dx, dy);
         return true;
      } else {
         return super.mouseDragged(event, dx, dy);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.init_RefQueue != null && this.init_RefQueue.isOpen()) {
         this.init_RefQueue.onMouseScrolled(cA.init_I_D(mouseX), cA.init_I_D(mouseY), scrollY);
         return true;
      }

      if (scrollY != 0.0) {
         this.tick = this.init_I(this.tick - (int)Math.signum(scrollY) * 16 * 2);
      }

      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (this.pop >= 0) {
         this.load(Compat263.keyEventGlfwKey(event) == 256 ? -1 : Compat263.keyEventGlfwKey(event));
         return true;
      }

      if (Compat263.keyEventGlfwKey(event) == 256) {
         if (this.init_RefQueue != null && this.init_RefQueue.isOpen()) {
            this.init_RefQueue.close();
            return true;
         } else {
            this.onClose();
            return true;
         }
      } else {
         return super.keyPressed(event);
      }
   }

   private void init_V(int idx) {
      this.pop = idx;
   }

   private void load(int code) {
      if (this.pop >= 0 && this.pop < this.init_List.size()) {
         this.init_List.get(this.pop).init_IntConsumer().accept(code);
         this.pop = -1;
      }
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.init_Screen);
   }

   private interface BufferHelper {
      void render(GuiGraphicsExtractor var1, NetHelper var2, int var3, int var4, boolean var5);
   }

   private record CachePool(String init_Str, IntSupplier init_IntSupplier, IntConsumer init_IntConsumer) {
   }

   private record Inner_DataUtil(NetHelper init_NetHelper, Runnable init_Runnable, Runnable load) {
   }

   private interface StreamUtil {
      boolean get();
   }
}
