package autismclient.gui.screens;

import autismclient.api.hud.HudElements;
import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.ToggleSwitchRenderer;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.l;
import autismclient.modules.Module;
import autismclient.util.AutismHudManager;
import autismclient.util.NativeImageFilePicker;
import autismclient.util.cA;
import autismclient.util.RegistryDisplayNameCache;
import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemedTextInputBox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class HudElementSettingsScreen extends Screen {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = -791321;
   private static final int load_I = -4743522;
   private static final int read_I = -50373;
   private static final int write = -10682470;
   private static final int update = 368;
   private static final int sync = 220;
   private static final int flush = 150;
   private static final int close = 40;
   private static final int open = 20;
   private static final int poll = 10;
   private static final int push = 200;
   private static final int pop = 4;
   private final Screen init_Screen;
   private final String init_Str;
   private int tick;
   private boolean init_Z;
   private int run;
   private String load_Str;
   private ThemedTextInputBox init_k;
   private String read_Str;
   private autismclient.gui.ui.widgets.ColorPickerPopup init_NetHelper;
   private final List<autismclient.gui.ui.widgets.DropdownWidget> init_List = new ArrayList<>();
   private final Map<String, autismclient.gui.ui.widgets.DropdownWidget> init_Map = new HashMap<>();
   private boolean load_Z;
   private int get;
   private HudElementSettingsScreen.Inner_StreamUtil init_EventBus_StreamUti;
   private static final Map<String, HudElementSettingsScreen.Inner_DataUtil> load_Map = new HashMap<>();
   private List<HudElementSettingsScreen.Inner_StreamUtil> load_List;
   private int set = -1;
   private int put;

   private static int init_I() {
      return ThemePaletteManager.init_I_I_ct_DataUtil(-4743522, ThemePaletteManager.ColorRole.TEXT);
   }

   private static int init_I_I(int argb) {
      return ThemePaletteManager.init_I_I_ct_DataUtil(argb, ThemePaletteManager.ColorRole.ACCENT);
   }

   public HudElementSettingsScreen(Screen parent, String id) {
      super(Component.literal("HUD Element Settings"));
      this.init_Screen = parent;
      this.init_Str = id;
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
         int[] panel = this.init_IArr();
         int x = panel[0];
         int y = panel[1];
         int w = panel[2];
         int h = panel[3];
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), 1711276032);
         this.init(graphics, x, y, w, h, this.write_I(), AutismHudManager.init_Str_Str(this.init_Str), mx, my);
         if (this.init_Z_I_I(w, h)) {
            return;
         }

         int bodyTop = this.load(panel);
         int bodyBottom = this.read(panel);
         int viewH = this.write(panel);
         List<HudElementSettingsScreen.Inner_StreamUtil> rows = this.init_List();
         int contentH = this.init_I_List(rows);
         this.tick = this.init(this.tick, 0, Math.max(0, contentH - viewH));
         boolean menuOpen = autismclient.gui.ui.widgets.DropdownWidget.init_Z_List(this.init_List);
         int hoverX = menuOpen ? -2147483648 : mx;
         int hoverY = menuOpen ? -2147483648 : my;
         this.init_List.clear();
         autismclient.gui.ui.UiClipStack.init_HashUtil()
            .init(graphics, autismclient.gui.f.NetHelper.init(x + 2, bodyTop, Math.max(0, w - 9), Math.max(0, bodyBottom - bodyTop)));

         try {
            this.init(graphics, rows, this.sync(panel), bodyTop - this.tick, this.flush(panel), hoverX, hoverY, bodyTop, bodyBottom);
            autismclient.gui.ui.widgets.DropdownWidget.load(graphics, this.font, this.init_List, mx, my);
         } finally {
            autismclient.gui.ui.UiClipStack.init_HashUtil().init(graphics);
         }

         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
            contentH, viewH, x + w - 6, bodyTop, 4, viewH, this.tick
         );
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(graphics, metrics, metrics.init(hoverX, hoverY), this.init_Z);
         int footerY = this.update(panel);
         int ctrlH = this.update();
         this.init(graphics, "Back", x + 4, footerY, this.init_I_Str("Back"), ctrlH, hoverX, hoverY, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY);
         this.init(
            graphics, "Reset", this.load(x), footerY, this.init_I_Str("Reset"), ctrlH, hoverX, hoverY, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY
         );
         boolean enabled = AutismHudManager.init_AutismConfig_HudEl_Str(this.init_Str).enabled;
         int switchX = x + w - 4 - 20;
         int labelW = autismclient.gui.f.b.k.init(this.font, "Enabled", init_CharEncoder.init_Identifier_l(l.read), -791321);
         this.init(graphics, "Enabled", this.init_I_IArr(panel), this.init_I_I_I(footerY, ctrlH), enabled ? -791321 : init_I(), labelW + 2);
         this.init(graphics, enabled, switchX, footerY, ctrlH, "hud-elem-settings:" + this.init_Str + ":enabled");
         autismclient.gui.ui.widgets.DropdownWidget.read(graphics, this.font, this.init_List, mx, my);
         if (this.load_Z) {
            this.init(graphics, sw, sh, mx, my);
         }

         if (this.init_NetHelper != null) {
            graphics.nextStratum();
            this.init_NetHelper.init_V_SyncManager(autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my));
            if (!this.init_NetHelper.init_Z()) {
               this.load_V();
            }
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, List<HudElementSettingsScreen.Inner_StreamUtil> rows, int x, int y, int w, int mx, int my, int clipTop, int clipBottom) {
      int cy = y;
      int rowStep = this.open();
      int sectionStep = this.poll();

      for (HudElementSettingsScreen.Inner_StreamUtil row : rows) {
         if (row.init_Z()) {
            if (this.init_Z(cy, sectionStep, clipTop, clipBottom)) {
               this.init(graphics, row.load_Str, x, cy, w);
            }

            cy += sectionStep;
         } else {
            if (this.init_Z(cy, rowStep, clipTop, clipBottom)) {
               this.init(graphics, row, x, cy, w, mx, my);
            }

            cy += rowStep;
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, String label, int x, int y, int w) {
      int boxY = y + 2;
      int boxH = autismclient.gui.ui.UiDensityMetrics.init(12);
      autismclient.gui.ui.widgets.PanelSurfaceHelper.init(graphics, x, boxY, w, boxH, init_I_I(1142492176));
      autismclient.gui.ui.widgets.PanelSurfaceHelper.init(graphics, x, boxY, w, init_I_I(1720655652));
      autismclient.gui.ui.widgets.PanelSurfaceHelper.init(graphics, x, boxY + boxH - 1, w, init_I_I(1720655652));
      this.init(graphics, label, x + 4, this.init_I_I_I(boxY, boxH), init_I(), w - 8);
   }

   private void init(GuiGraphicsExtractor graphics, HudElementSettingsScreen.Inner_StreamUtil row, int x, int y, int w, int mx, int my) {
      int rowH = this.flush();
      int ctrlH = this.close();
      int ctrlY = y + (rowH - ctrlH) / 2;
      int textY = this.init_I_I_I(y, rowH);
      boolean over = this.init(mx, my, x, y, w, rowH);
      autismclient.gui.ui.widgets.PanelSurfaceHelper.init(graphics, x, y, w, rowH, over ? init_I_I(1714756383) : 856888344);
      int labelColW = this.push();
      this.init(graphics, row.load_Str, x + 4, textY, -791321, labelColW - 8);
      int valueX = x + labelColW;
      int valueW = this.read(w);
      if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.BOOL) {
         boolean on = this.init_Z_Str(row.init_Str);
         this.init(graphics, on, x + w - 4 - 20, y, rowH, "hud-elem-settings:" + this.init_Str + ":" + row.init_Str);
      } else if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.ENUM) {
         this.init(row, valueX, ctrlY, valueW, ctrlH);
      } else if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.COLOR) {
         int pickW = this.load("Pick");
         int color = AutismHudManager.init_I_Str_I(AutismHudManager.init_Str_Str_Str(this.init_Str, row.init_Str), this.read(row.init_Str));
         int swatchX = valueX;
         int swatchW = 24;
         autismclient.gui.f.PacketUtil.load(
            graphics, autismclient.gui.f.NetHelper.init(swatchX, ctrlY + 1, swatchW, ctrlH - 2), 3, color | 0xFF000000, init_CharEncoder.set()
         );
         int hexX = swatchX + swatchW + 4;
         this.init(graphics, "#" + String.format(Locale.ROOT, "%06X", color & 16777215), hexX, textY, -791321, Math.max(1, valueX + valueW - pickW - 4 - hexX));
         this.init(graphics, "Pick", valueX + valueW - pickW, ctrlY, pickW, ctrlH, mx, my, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY);
      } else if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.MODULE_LIST) {
         String editLabel = "Edit (" + this.read_List().size() + ")";
         int editW = this.load(editLabel);
         this.init(graphics, editLabel, valueX + valueW - editW, ctrlY, editW, ctrlH, mx, my, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY);
      } else if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.NUMBER) {
         double value = AutismHudManager.init_D_Str(this.init_Str, row.init_Str, row.init_D);
         this.init(
            graphics,
            row,
            valueX,
            ctrlY,
            valueW,
            ctrlH,
            value,
            row.init_Str != null && row.init_Str.equals(this.load_Str) || row == this.init_EventBus_StreamUti,
            mx,
            my
         );
      } else if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.ITEM) {
         int pickW = this.load("Pick");
         String raw = AutismHudManager.init_Str_Str_Str(this.init_Str, row.init_Str);
         String itemName = raw != null && !raw.isBlank() ? RegistryDisplayNameCache.init(raw.contains(":") ? raw : "minecraft:" + raw) : "None";
         this.init(graphics, itemName, valueX, textY, -791321, Math.max(1, valueW - pickW - 4));
         this.init(graphics, "Pick", valueX + valueW - pickW, ctrlY, pickW, ctrlH, mx, my, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY);
      } else if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.PICK) {
         String buttonLabel = row.init_List.isEmpty() ? "Pick…" : row.init_List.get(0);
         int pickW = this.load(buttonLabel);
         this.init(graphics, init_Str_Str(AutismHudManager.init_Str_Str_Str(this.init_Str, row.init_Str)), valueX, textY, -791321, Math.max(1, valueW - pickW - 4));
         this.init(graphics, buttonLabel, valueX + valueW - pickW, ctrlY, pickW, ctrlH, mx, my, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY);
      }
   }

   private void init(HudElementSettingsScreen.Inner_StreamUtil row, int x, int y, int w, int h) {
      List<String> choices = row.init_List;
      String current = AutismHudManager.init_Str_Str_Str(this.init_Str, row.init_Str);
      int selected = Math.max(0, choices.indexOf(current));
      int choiceW = 0;

      for (String choice : choices) {
         choiceW = Math.max(choiceW, autismclient.gui.f.b.k.init(this.font, choice, init_CharEncoder.init_Identifier_l(l.read), 0));
      }

      int dw = Math.min(w, Math.max(56, choiceW + 26));
      int dx = x + w - dw;
      autismclient.gui.ui.widgets.DropdownWidget dropdown = this.init_Map
         .computeIfAbsent(row.init_Str, ignored -> new autismclient.gui.ui.widgets.DropdownWidget(dx, y, dw, h, choices, selected, index -> {}));
      dropdown.init_SyncManager_I(dx, y, dw, h).init_SyncManager_List(choices).init_SyncManager_I(selected).init_SyncManager_IntConsumer(index -> {
         AutismHudManager.init_V_Str(this.init_Str, row.init_Str, choices.get(index));
         this.write_V();
      });
      this.init_List.add(dropdown);
   }

   private void init(GuiGraphicsExtractor graphics, HudElementSettingsScreen.Inner_StreamUtil row, int x, int y, int w, int h, double value, boolean editing, int mx, int my) {
      int fieldW = 40;
      int sliderW = this.write(w);
      int fieldX = x + sliderW + 4;
      double ratio = autismclient.gui.ui.widgets.SliderRenderer.init_D_D(value, row.init_D, row.load_D);
      autismclient.gui.ui.widgets.SliderRenderer.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, x, y), autismclient.gui.f.NetHelper.init(x, y, sliderW, h), ratio, editing
      );
      if (editing) {
         this.init_V(fieldX, y, fieldW, h);
         this.init_k.init_V(graphics, mx, my, 0.0F);
      } else {
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(fieldX, y, fieldW, h), 4, -15594224, init_CharEncoder.set());
         this.init(graphics, this.init_Str_EventBus_StreamUti_D(row, value), fieldX, y, fieldW, h, -791321);
      }
   }

   private void init_V(int x, int y, int w, int h) {
      if (this.init_k == null) {
         this.init_k = new ThemedTextInputBox(Minecraft.getInstance(), this.font, x, y, w, h, false);
      }

      this.init_k.read(x);
      this.init_k.write(y);
      this.init_k.update(w);
      this.init_k.sync(h);
   }

   private void init_V_Str_Str(String key, String value) {
      this.load_Str = key;
      if (this.init_k == null) {
         this.init_k = new ThemedTextInputBox(Minecraft.getInstance(), this.font, 0, 0, 20, this.close(), false);
      }

      this.init_k.init_V_Str(value == null ? "" : value);
      this.init_k.push(true);
      this.init_k.flush(this.init_k.load_Str().length());
   }

   private void init(GuiGraphicsExtractor graphics, boolean checked, int x, int y, int boxH, String animationKey) {
      int pillY = y + (boxH - 10) / 2;
      ToggleSwitchRenderer.init(autismclient.gui.f.UiHelper.init(graphics, this.font, x, pillY), autismclient.gui.f.NetHelper.init(x, pillY, 20, 10), checked, false, animationKey);
   }

   private void init(
      GuiGraphicsExtractor graphics, String label, int x, int y, int w, int h, int mx, int my, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(graphics, this.font, x, y, w, h, label, variant, true, mx, my);
   }

   private int init_I_Str(String label) {
      return this.load(label);
   }

   private int load(String label) {
      return autismclient.gui.ui.UiDensityMetrics.load(autismclient.gui.f.b.k.init(this.font, label, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private static String init_Str_Str(String path) {
      if (path != null && !path.isBlank()) {
         int slash = Math.max(path.lastIndexOf(47), path.lastIndexOf(92));
         return slash >= 0 ? path.substring(slash + 1) : path;
      } else {
         return "None";
      }
   }

   private int load(int panelX) {
      return panelX + 4 + this.init_I_Str("Back") + 2;
   }

   private int init_I_IArr(int[] panel) {
      int labelW = autismclient.gui.f.b.k.init(this.font, "Enabled", init_CharEncoder.init_Identifier_l(l.read), -791321);
      return panel[0] + panel[2] - 4 - 20 - 4 - labelW;
   }

   private void init(GuiGraphicsExtractor graphics, int sw, int sh, int mx, int my) {
      List<Module> modules = this.load_List();
      int[] picker = this.init_IArr_I(modules.size());
      int x = picker[0];
      int y = picker[1];
      int w = picker[2];
      int h = picker[3];
      int viewTop = picker[4];
      int viewBottom = picker[5];
      int rowStep = this.open();
      int rowH = this.flush();
      this.get = this.init(this.get, 0, this.load_I());
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), 1140850688);
      this.init(graphics, x, y, w, h, this.write_I(), "Hidden Modules", mx, my);
      autismclient.gui.ui.UiClipStack.init_HashUtil()
         .init(graphics, autismclient.gui.f.NetHelper.init(x + 2, viewTop, Math.max(0, w - 4), Math.max(0, viewBottom - viewTop)));

      try {
         List<String> hidden = this.read_List();
         int cy = viewTop - this.get;
         int rowX = x + 4;
         int rowW = w - 8;

         for (Module module : modules) {
            if (this.init_Z(cy, rowStep, viewTop, viewBottom)) {
               boolean selected = hidden.contains(module.id());
               boolean over = this.init(mx, my, rowX, cy, rowW, rowH);
               autismclient.gui.ui.widgets.PanelSurfaceHelper.init(graphics, rowX, cy, rowW, rowH, over ? init_I_I(1714756383) : 856888344);
               this.init(graphics, module.name(), rowX + 4, this.init_I_I_I(cy, rowH), selected ? -791321 : init_I(), rowW - 20 - 12);
               this.init(graphics, selected, rowX + rowW - 4 - 20, cy, rowH, "hud-hidden-modules:" + module.id());
            }

            cy += rowStep;
         }
      } finally {
         autismclient.gui.ui.UiClipStack.init_HashUtil().init(graphics);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      int[] panel = this.init_IArr();
      int x = panel[0];
      int y = panel[1];
      int w = panel[2];
      int h = panel[3];
      if (Compat263.mouseEventGlfwButton(event) != 0 && Compat263.mouseEventGlfwButton(event) != 1) {
         return true;
      }

      if (this.init_NetHelper != null) {
         this.init_NetHelper.init_Z_I_2(mx, my, Compat263.mouseEventGlfwButton(event));
         if (!this.init_NetHelper.init_Z()) {
            this.load_V();
         }

         return true;
      } else {
         if (this.load_Z) {
            this.load(mx, my);
            return true;
         }

         if (this.init_Z(mx, my, x, y, w, h, this.write_I())) {
            this.minecraft.gui.setScreen(this.init_Screen);
            return true;
         }

         if (autismclient.gui.ui.widgets.DropdownWidget.init_Z_List_2(this.init_List, mx, my, Compat263.mouseEventGlfwButton(event))) {
            this.write_V();
            return true;
         }

         if (autismclient.gui.ui.widgets.DropdownWidget.init_Z_List(this.init_List)) {
            return true;
         }

         if (this.load_Str != null && this.init_k != null && this.init_k.init_Z_D_2(mx, my, Compat263.mouseEventGlfwButton(event))) {
            return true;
         }

         int bodyTop = this.load(panel);
         int bodyBottom = this.read(panel);
         int viewH = this.write(panel);
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
            this.init_I_List(this.init_List()), viewH, x + w - 6, bodyTop, 4, viewH, this.tick
         );
         if (metrics.init_Z() && metrics.init(mx, my)) {
            this.init_Z = true;
            this.run = metrics.load(mx, my) ? my - metrics.update() : metrics.sync() / 2;
            this.tick = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(metrics, my, this.run);
            return true;
         }

         int footerY = this.update(panel);
         int ctrlH = this.update();
         if (this.init(mx, my, x + 4, footerY, this.init_I_Str("Back"), ctrlH)) {
            this.minecraft.gui.setScreen(this.init_Screen);
            return true;
         }

         if (this.init(mx, my, this.load(x), footerY, this.init_I_Str("Reset"), ctrlH)) {
            AutismHudManager.load_V(this.init_Str);
            this.write_V();
            return true;
         }

         int toggleX = this.init_I_IArr(panel);
         if (this.init(mx, my, toggleX, footerY, x + w - 4 - toggleX, ctrlH)) {
            AutismHudManager.init_V_Str(this.init_Str);
            return true;
         }

         if (my >= bodyTop && my < bodyBottom) {
            int rowsX = this.sync(panel);
            int rowsW = this.flush(panel);
            HudElementSettingsScreen.Inner_StreamUtil row = this.init_EventBus_StreamUti(mx, my, rowsX, bodyTop - this.tick, rowsW, bodyTop, bodyBottom);
            if (row != null && !row.init_Z()) {
               if (row.init_EventBus_CachePool != HudElementSettingsScreen.Inner_CachePool.ENUM) {
                  this.init(row, Compat263.mouseEventGlfwButton(event), mx, my, rowsX + this.push(), this.read(rowsW));
               }

               return true;
            } else {
               this.write_V();
               return true;
            }
         } else {
            this.write_V();
            return true;
         }
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.init_Z = false;
      this.init_EventBus_StreamUti = null;
      if (this.init_NetHelper != null) {
         int mx = cA.init_I_D(event.x());
         int my = cA.init_I_D(event.y());
         this.init_NetHelper.load(mx, my, Compat263.mouseEventGlfwButton(event));
         if (!this.init_NetHelper.init_Z()) {
            this.load_V();
         }

         return true;
      } else {
         if (autismclient.gui.ui.widgets.DropdownWidget.write(this.init_List)) {
            return true;
         }

         if (this.init_k != null) {
            this.init_k.load(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
         }

         return true;
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.init_EventBus_StreamUti != null) {
         int[] panel = this.init_IArr();
         int valueX = this.sync(panel) + this.push();
         int valueW = this.read(this.flush(panel));
         this.init_V_EventBus_StreamUti_D(
            this.init_EventBus_StreamUti,
            autismclient.gui.ui.widgets.SliderRenderer.init(
               mx, valueX, this.write(valueW), this.init_EventBus_StreamUti.init_D, this.init_EventBus_StreamUti.load_D, this.init_EventBus_StreamUti.read
            )
         );
         return true;
      }

      if (this.init_NetHelper != null) {
         this.init_NetHelper.init_Z_I(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy);
         if (!this.init_NetHelper.init_Z()) {
            this.load_V();
         }

         return true;
      } else {
         if (autismclient.gui.ui.widgets.DropdownWidget.load(this.init_List, mx, my, Compat263.mouseEventGlfwButton(event))) {
            return true;
         }

         if (this.load_Str != null && this.init_k != null && this.init_k.init(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
            return true;
         }

         if (!this.init_Z) {
            return true;
         }

         int[] panel = this.init_IArr();
         int bodyTop = this.load(panel);
         int viewH = this.write(panel);
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
            this.init_I_List(this.init_List()), viewH, panel[0] + panel[2] - 6, bodyTop, 4, viewH, this.tick
         );
         this.tick = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.init(metrics, my, this.run);
         return true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      double virtualX = cA.init_D_D(mouseX);
      double virtualY = cA.init_D_D(mouseY);
      if (this.init_NetHelper != null) {
         this.init_NetHelper.init_Z_I((int)Math.round(virtualX), (int)Math.round(virtualY), scrollY);
         return true;
      } else if (autismclient.gui.ui.widgets.DropdownWidget.init_Z_List(this.init_List, virtualX, virtualY, scrollY)) {
         return true;
      } else if (this.load_Z) {
         this.get = this.init(this.get + (scrollY < 0.0 ? this.open() : -this.open()), 0, this.load_I());
         return true;
      } else {
         int[] panel = this.init_IArr();
         int contentH = this.init_I_List(this.init_List());
         this.tick = this.init(this.tick + (scrollY < 0.0 ? this.open() : -this.open()), 0, Math.max(0, contentH - this.write(panel)));
         return true;
      }
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (this.init_NetHelper != null) {
         this.init_NetHelper.read(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input));
         if (!this.init_NetHelper.init_Z()) {
            this.load_V();
         }

         return true;
      } else if (Compat263.keyEventGlfwKey(input) == 256) {
         if (this.load_Z) {
            this.load_Z = false;
         } else if (this.load_Str != null) {
            this.write_V();
         } else {
            this.minecraft.gui.setScreen(this.init_Screen);
         }

         return true;
      } else {
         if (this.load_Str != null) {
            if (Compat263.keyEventGlfwKey(input) == 257 || Compat263.keyEventGlfwKey(input) == 335) {
               this.read_V();
               return true;
            }

            if (this.init_k != null && this.init_k.init_Z_KeyEvent(input)) {
               return true;
            }
         }

         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      if (this.init_NetHelper != null) {
         this.init_NetHelper.init_Z_C((char)input.codepoint());
         if (!this.init_NetHelper.init_Z()) {
            this.load_V();
         }

         return true;
      } else {
         if (this.load_Str != null && this.init_k != null) {
            this.init_k.init_Z_CharacterEvent(input);
         }

         return true;
      }
   }

   private void init(HudElementSettingsScreen.Inner_StreamUtil row, int button, int mx, int my, int valueX, int valueW) {
      int dir = button == 1 ? -1 : 1;
      if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.BOOL) {
         AutismHudManager.init_V_Str(this.init_Str, row.init_Str, Boolean.toString(!this.init_Z_Str(row.init_Str)));
         this.write_V();
      } else if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.COLOR) {
         this.init_V_Str_NetHelper(row.init_Str, autismclient.gui.f.NetHelper.init(valueX, Math.max(0, my - this.flush() / 2), valueW, this.flush()));
         this.write_V();
      } else if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.MODULE_LIST) {
         this.load_Z = true;
         this.get = 0;
         this.write_V();
      } else if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.NUMBER) {
         int fieldX = valueX + this.write(valueW) + 4;
         if (mx >= fieldX && mx < fieldX + 40) {
            this.init_V_Str_Str(row.init_Str, AutismHudManager.init_Str_Str_Str(this.init_Str, row.init_Str));
            return;
         }

         double value = autismclient.gui.ui.widgets.SliderRenderer.init(mx, valueX, this.write(valueW), row.init_D, row.load_D, row.read);
         this.init_V_EventBus_StreamUti_D(row, value);
         this.init_EventBus_StreamUti = row;
         this.write_V();
      } else if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.ITEM) {
         this.write_V();
         this.minecraft
            .gui
            .setScreen(new PickItemScreen(this, AutismHudManager.init_Str_Str_Str(this.init_Str, row.init_Str), valuex -> AutismHudManager.init_V_Str(this.init_Str, row.init_Str, valuex)));
      } else if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.PICK) {
         this.write_V();
         String elementId = this.init_Str;
         String settingId = row.init_Str;
         NativeImageFilePicker.pickImage(AutismHudManager.init_Str_Str_Str(elementId, settingId), picked -> {
            AutismHudManager.init_V_Str(elementId, settingId, picked);
            AutismHudManager.init_V_Str(elementId, "gif-natural", "true");
         });
      }
   }

   private void init_V_Str_NetHelper(String key, autismclient.gui.f.NetHelper anchor) {
      if (key != null && !key.isBlank()) {
         this.read_Str = key;
         this.load_Z = false;
         autismclient.gui.ui.widgets.DropdownWidget.read(this.init_List);
         this.write_V();
         int fallback = this.read(key);
         int initial = AutismHudManager.init_I_Str_I(AutismHudManager.init_Str_Str_Str(this.init_Str, key), fallback);
         this.init_NetHelper = new autismclient.gui.ui.widgets.ColorPickerPopup(anchor, initial, fallback, cA.init_I(), cA.load_I(), argb -> {
            AutismHudManager.init_V_Str(this.init_Str, key, String.format(Locale.ROOT, "%08X", argb));
            this.load_V();
         });
      }
   }

   private void load_V() {
      if (this.init_NetHelper != null) {
         this.init_NetHelper.init_V();
      }

      this.init_NetHelper = null;
      this.read_Str = null;
   }

   private int read(String key) {
      return AutismHudManager.init_I_Str_I(AutismHudManager.load_Str(this.init_Str, key), -1);
   }

   private void init_V_EventBus_StreamUti_D(HudElementSettingsScreen.Inner_StreamUtil row, double value) {
      double clamped = Math.max(row.init_D, Math.min(row.load_D, value));
      AutismHudManager.init_V_Str(this.init_Str, row.init_Str, this.init_Str_D_D(clamped, row.read));
   }

   private void read_V() {
      HudElementSettingsScreen.Inner_StreamUtil row = this.init_EventBus_StreamUti_Str(this.load_Str);
      if (row == null) {
         this.write_V();
      } else {
         String editingText = this.init_k == null ? "" : this.init_k.load_Str();
         if (row.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.NUMBER) {
            try {
               this.init_V_EventBus_StreamUti_D(row, Double.parseDouble(editingText.trim()));
            } catch (NumberFormatException var4) {
            }
         }

         this.write_V();
      }
   }

   private void write_V() {
      this.load_Str = null;
      if (this.init_k != null) {
         this.init_k.push(false);
         this.init_k.init_V_Str("");
      }
   }

   private HudElementSettingsScreen.Inner_StreamUtil init_EventBus_StreamUti(int mx, int my, int x, int y, int w, int clipTop, int clipBottom) {
      int cy = y;
      int rowH = this.flush();

      for (HudElementSettingsScreen.Inner_StreamUtil row : this.init_List()) {
         int h = row.init_Z() ? this.poll() : this.open();
         if (!row.init_Z() && my >= clipTop && my < clipBottom && this.init(mx, my, x, cy, w, rowH)) {
            return row;
         }

         cy += h;
      }

      return null;
   }

   private HudElementSettingsScreen.Inner_StreamUtil init_EventBus_StreamUti_Str(String key) {
      if (key == null) {
         return null;
      }

      for (HudElementSettingsScreen.Inner_StreamUtil row : this.init_List()) {
         if (key.equals(row.init_Str)) {
            return row;
         }
      }

      return null;
   }

   private List<HudElementSettingsScreen.Inner_StreamUtil> init_List() {
      long rev = init_J();
      HudElementSettingsScreen.Inner_DataUtil cached = load_Map.get(this.init_Str);
      if (cached != null && cached.init_J == rev) {
         return cached.init_List;
      }

      List<HudElementSettingsScreen.Inner_StreamUtil> rows = new ArrayList<>();
      if ("bed_danger".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Drag Bed Danger in the HUD Editor to position it."));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Distance and size: Danger > Bed Danger."));
         return List.copyOf(rows);
      }

      boolean isArrayList = "active_modules".equals(this.init_Str);
      boolean isCompass = "compass".equals(this.init_Str);
      boolean isKeystrokes = "keystrokes".equals(this.init_Str);
      boolean isSlots = "armor".equals(this.init_Str) || "inventory".equals(this.init_Str);
      boolean isAddon = HudElements.isAddon(this.init_Str);
      boolean background = AutismHudManager.init_Z_Str_Str(this.init_Str, "background");
      boolean outline = AutismHudManager.init_Z_Str_Str(this.init_Str, "outline");
      if (("tps".equals(this.init_Str) || "ping".equals(this.init_Str)) && AutismHudManager.init_AutismConfig_HudEl_Str("fps").enabled) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Shown in the FPS element while it is on"));
         List<HudElementSettingsScreen.Inner_StreamUtil> merged = List.copyOf(rows);
         load_Map.put(this.init_Str, new HudElementSettingsScreen.Inner_DataUtil(rev, merged));
         return merged;
      }

      boolean customMatters;
      if (isArrayList) {
         customMatters = false;
      } else if (isCompass || isKeystrokes || isAddon) {
         customMatters = true;
      } else if (!"watermark".equals(this.init_Str) && !"gif".equals(this.init_Str) && !isSlots) {
         if ("fps_graph".equals(this.init_Str)) {
            customMatters = background || outline || AutismHudManager.init_Z_Str_Str(this.init_Str, "show-current-fps");
         } else {
            customMatters = true;
         }
      } else {
         customMatters = background || outline;
      }

      boolean customColors = isArrayList || AutismHudManager.init_Z_Str_Str(this.init_Str, "use-custom-colors");
      rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Visibility"));
      if (!this.init_Z() && !"breaking_progress".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_2("alignment", "Alignment", "Left", "Center", "Right"));
      }

      if (customMatters) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("use-custom-colors", "Custom Colors"));
      }

      if ("watermark".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Logo"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("logo-width", "Width", HudElementSettingsScreen.Inner_CachePool.NUMBER, 48.0, 420.0, 1.0));
      } else if ("gif".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("GIF"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("gif-path", "Image", "Pick…"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("gif-width", "Width", HudElementSettingsScreen.Inner_CachePool.NUMBER, 16.0, 512.0, 1.0));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("gif-height", "Height", HudElementSettingsScreen.Inner_CachePool.NUMBER, 16.0, 512.0, 1.0));
         if (!AutismHudManager.init_Str_Str_Str(this.init_Str, "gif-path").isBlank()) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("gif-fps", "Framerate (FPS)", HudElementSettingsScreen.Inner_CachePool.NUMBER, 0.0, 60.0, 1.0));
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_2("gif-quality", "Quality", "Low", "Medium", "High", "Original"));
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("gif-natural", "Match File Aspect"));
         }

         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("gif-keep-aspect", "Lock Ratio When Resizing"));
      } else if (customColors && customMatters) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Text"));
         if (isCompass) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("label-color", "Letter Color"));
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("accent-color", "Facing Color"));
         } else if (isAddon) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("label-color", "Label Color"));
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("value-color", "Value Color"));
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("accent-color", "Accent Color"));
         } else if ("fps_graph".equals(this.init_Str)) {
            if (AutismHudManager.init_Z_Str_Str(this.init_Str, "show-current-fps")) {
               rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("value-color", "Value Color"));
            }
         } else if (!isKeystrokes && !isSlots) {
            boolean labelShown = !"item_counter".equals(this.init_Str) || AutismHudManager.init_Z_Str_Str(this.init_Str, "item-show-name");
            boolean valueOverridden = "tps".equals(this.init_Str) && AutismHudManager.init_Z_Str_Str(this.init_Str, "tps-color-threshold")
               || "ping".equals(this.init_Str) && AutismHudManager.init_Z_Str_Str(this.init_Str, "ping-color-threshold");
            if (labelShown) {
               rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("label-color", "Label Color"));
            }

            if (!valueOverridden) {
               rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("value-color", "Value Color"));
            }
         }
      }

      rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Background"));
      rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("background", "Background"));
      if (customColors && background) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("background-color", "Background Color"));
      }

      rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Outline"));
      rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("outline", "Outline"));
      if (outline) {
         if (customColors) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("outline-color", "Outline Color"));
         }

         boolean flatSlots = isSlots && "Flat".equals(AutismHudManager.init_Str_Str_Str(this.init_Str, "slot-style"));
         if (!flatSlots) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("outline-width", "Outline Width", HudElementSettingsScreen.Inner_CachePool.NUMBER, 1.0, 6.0, 1.0));
         }
      }

      if (isSlots) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Slots"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_2("slot-style", "Slot Style", "Textured", "Flat"));
      }

      if (isArrayList) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Active Modules"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("module-info", "Module Info"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-keybind", "Show Keybind"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_2("sort", "Sort", "Width", "Name", "Category"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_2("color-mode", "Color Mode", "Flat", "Random", "Rainbow", "Gradient"));
         String mode = AutismHudManager.init_Str_Str_Str(this.init_Str, "color-mode");
         if ("Flat".equals(mode)) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("flat-color", "Flat Color"));
         }

         if ("Gradient".equals(mode)) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("gradient-start-color", "Gradient Start"));
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("gradient-end-color", "Gradient End"));
         }

         if ("Rainbow".equals(mode)) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("value-color", "Rainbow Tint"));
         }

         if (AutismHudManager.init_Z_Str_Str(this.init_Str, "module-info") || AutismHudManager.init_Z_Str_Str(this.init_Str, "show-keybind")) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("module-info-color", "Info Color"));
         }

         if ("Rainbow".equals(mode) || "Gradient".equals(mode)) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("rainbow-speed", "Animation Speed", HudElementSettingsScreen.Inner_CachePool.NUMBER, 0.1, 2.0, 0.05));
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("rainbow-spread", "Row Spread", HudElementSettingsScreen.Inner_CachePool.NUMBER, 0.001, 0.12, 0.001));
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_2("rainbow-direction", "Direction", "Forward", "Reverse"));
         }

         if ("Rainbow".equals(mode)) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("rainbow-saturation", "Saturation", HudElementSettingsScreen.Inner_CachePool.NUMBER, 0.0, 0.35, 0.05));
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("rainbow-brightness", "Brightness", HudElementSettingsScreen.Inner_CachePool.NUMBER, 0.0, 1.0, 0.05));
         }

         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("stair-snap", "Stair Snap", HudElementSettingsScreen.Inner_CachePool.NUMBER, 0.0, 24.0, 1.0));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.write("hidden-modules", "Hidden Modules"));
      }

      if ("tps".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("TPS"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("tps-precise", "Precise"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("tps-color-threshold", "Color Thresholds"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("tps-show-jitter", "Show Min"));
      }

      if ("ping".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Ping"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("ping-color-threshold", "Color Thresholds"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("ping-show-jitter", "Show Jitter"));
      }

      if ("cps".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("CPS"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-total", "Show Total"));
      }

      if ("durability".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Durability"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-item-name", "Show Item Name"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("low-durability-warn", "Low Durability Warning"));
      }

      if ("looking_at".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Looking At"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-distance", "Show Distance"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-block-id", "Show Block ID"));
      }

      if ("breaking_progress".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Breaking Progress"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-block-name", "Show Block Name"));
      }

      if ("weather".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Weather"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-temperature", "Show Temperature"));
      }

      if ("world_time".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("World Time"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_2("world-time-format", "Format", "24h", "ticks"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-day", "Show Day"));
      }

      if ("real_time".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Real Time"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_2("real-time-format", "Format", "12h", "24h"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-date", "Show Date"));
      }

      if ("memory".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Memory"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_2("memory-format", "Format", "used/max", "used%"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-bar", "Show Bar"));
         if ("used/max".equals(AutismHudManager.init_Str_Str_Str(this.init_Str, "memory-format"))) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-percent", "Show Percentage"));
         }
      }

      if ("server_ip".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Server IP"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-port", "Show Port"));
      }

      if ("fps_graph".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("FPS Graph"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("graph-samples", "Samples", HudElementSettingsScreen.Inner_CachePool.NUMBER, 50.0, 200.0, 10.0));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("show-current-fps", "Show Current FPS"));
      }

      if ("item_counter".equals(this.init_Str)) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Item Counter"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("item-count-held", "Count Held Item"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("item-show-name", "Show Item Name"));
         if (!AutismHudManager.init_Z_Str_Str(this.init_Str, "item-count-held")) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.read("item-id", "Item"));
         }
      }

      if (isKeystrokes) {
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("Keystrokes"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str("keystroke-size", "Key Size", HudElementSettingsScreen.Inner_CachePool.NUMBER, 12.0, 40.0, 1.0));
         if (customColors) {
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("keystroke-active-color", "Fill Color"));
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("keystroke-idle-color", "Key Color"));
            rows.add(HudElementSettingsScreen.Inner_StreamUtil.load("keystroke-text-color", "Text Color"));
         }

         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("keystroke-show-space", "Show Space"));
         rows.add(HudElementSettingsScreen.Inner_StreamUtil.init_EventBus_StreamUti_Str_Str("keystroke-show-mouse", "Show Mouse"));
      }

      for (int i = rows.size() - 1; i >= 0; i--) {
         if (rows.get(i).init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.SECTION
            && (i == rows.size() - 1 || rows.get(i + 1).init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.SECTION)) {
            rows.remove(i);
         }
      }

      List<HudElementSettingsScreen.Inner_StreamUtil> frozen = List.copyOf(rows);
      load_Map.put(this.init_Str, new HudElementSettingsScreen.Inner_DataUtil(rev, frozen));
      return frozen;
   }

   private static long init_J() {
      return (long)AutismHudManager.load_I() << 32 | autismclient.modules.al.init_I() & 4294967295L;
   }

   private List<Module> load_List() {
      return new ArrayList<>(autismclient.modules.al.init_Collection());
   }

   private List<String> read_List() {
      List<String> ids = new ArrayList<>();
      String raw = AutismHudManager.init_Str_Str_Str("active_modules", "hidden-modules");
      if (raw != null && !raw.isBlank()) {
         for (String token : raw.split("\\|")) {
            String parsed = token.trim().toLowerCase(Locale.ROOT);
            if (!parsed.isBlank() && !ids.contains(parsed)) {
               ids.add(parsed);
            }
         }

         return ids;
      } else {
         return ids;
      }
   }

   private void init_V_Str(String moduleId) {
      if (moduleId != null && !moduleId.isBlank()) {
         String normalized = moduleId.trim().toLowerCase(Locale.ROOT);
         List<String> ids = this.read_List();
         if (ids.contains(normalized)) {
            ids.remove(normalized);
         } else {
            ids.add(normalized);
         }

         AutismHudManager.init_V_Str("active_modules", "hidden-modules", String.join("|", ids));
      }
   }

   private int[] init_IArr_I(int moduleCount) {
      int sw = cA.init_I();
      int sh = cA.load_I();
      int margin = 6;
      int w = autismclient.gui.ui.layout.PanelLayoutMath.init(sw, margin, 200);
      int chrome = this.write_I() + 8;
      int h = autismclient.gui.ui.layout.PanelLayoutMath.init(sh, margin, chrome + Math.max(4, moduleCount) * this.open());
      int x = autismclient.gui.ui.layout.PanelLayoutMath.load(sw, w, margin);
      int y = autismclient.gui.ui.layout.PanelLayoutMath.load(sh, h, margin);
      int viewTop = y + this.write_I() + 4;
      return new int[]{x, y, w, h, viewTop, y + h - 4};
   }

   private int load_I() {
      int count = this.load_List().size();
      int[] picker = this.init_IArr_I(count);
      return Math.max(0, count * this.open() - (picker[5] - picker[4]));
   }

   private void load(int mx, int my) {
      List<Module> modules = this.load_List();
      int[] picker = this.init_IArr_I(modules.size());
      int x = picker[0];
      int y = picker[1];
      int w = picker[2];
      int h = picker[3];
      int viewTop = picker[4];
      int viewBottom = picker[5];
      if (!this.init_Z(mx, my, x, y, w, h, this.write_I()) && this.init(mx, my, x, y, w, h)) {
         if (this.init(mx, my, x + 2, viewTop, w - 4, viewBottom - viewTop)) {
            int index = (my - viewTop + this.get) / this.open();
            if (index >= 0 && index < modules.size()) {
               this.init_V_Str(modules.get(index).id());
            }
         }
      } else {
         this.load_Z = false;
      }
   }

   private boolean init_Z() {
      return "armor".equals(this.init_Str)
         || "inventory".equals(this.init_Str)
         || "compass".equals(this.init_Str)
         || "watermark".equals(this.init_Str)
         || "keystrokes".equals(this.init_Str)
         || "gif".equals(this.init_Str);
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int headerHeight, String title, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      autismclient.gui.ui.widgets.DialogFrameRenderer.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my),
         bounds,
         headerHeight,
         title,
         mx >= x && mx < x + width && my >= y && my < y + headerHeight
      );
   }

   private boolean init_Z(int mx, int my, int x, int y, int width, int height, int headerHeight) {
      return autismclient.gui.ui.widgets.DialogFrameRenderer.init(autismclient.gui.f.NetHelper.init(x, y, width, height), headerHeight, mx, my);
   }

   private int read_I() {
      int sh = cA.load_I();
      int minBody = 4 * this.open();
      int maxBody = Math.max(minBody, (int)(sh * 0.65F));
      int body = this.init(this.init_I_List(this.init_List()), minBody, maxBody);
      return this.write_I() + 4 + body + this.sync();
   }

   private int init_I_List(List<HudElementSettingsScreen.Inner_StreamUtil> rows) {
      int h = 0;
      int rowStep = this.open();
      int sectionStep = this.poll();

      for (HudElementSettingsScreen.Inner_StreamUtil row : rows) {
         h += row.init_Z() ? sectionStep : rowStep;
      }

      return h;
   }

   private int[] init_IArr() {
      int sw = cA.init_I();
      int sh = cA.load_I();
      int margin = 6;
      int preferredW = Math.max(220, Math.min(368, 4 + this.push() + 150 + 10));
      int w = autismclient.gui.ui.layout.PanelLayoutMath.init(sw, margin, preferredW);
      int h = autismclient.gui.ui.layout.PanelLayoutMath.init(sh, margin, this.read_I());
      return new int[]{autismclient.gui.ui.layout.PanelLayoutMath.load(sw, w, margin), autismclient.gui.ui.layout.PanelLayoutMath.load(sh, h, margin), w, h};
   }

   private boolean init_Z_I_I(int width, int height) {
      return width < 220 || height < this.write_I() + this.sync() + this.open() * 2;
   }

   private int write_I() {
      return autismclient.gui.ui.UiDensityMetrics.init(14);
   }

   private int update() {
      return autismclient.gui.ui.UiDensityMetrics.init(13);
   }

   private int sync() {
      return this.update() + 8;
   }

   private int flush() {
      return autismclient.gui.ui.UiDensityMetrics.init(14);
   }

   private int close() {
      return this.flush() - 2;
   }

   private int open() {
      return this.flush() + 2;
   }

   private int poll() {
      return autismclient.gui.ui.UiDensityMetrics.init(12) + 4;
   }

   private int load(int[] panel) {
      return panel[1] + this.write_I() + 4;
   }

   private int read(int[] panel) {
      return panel[1] + panel[3] - this.sync();
   }

   private int write(int[] panel) {
      return Math.max(30, this.read(panel) - this.load(panel));
   }

   private int update(int[] panel) {
      return panel[1] + panel[3] - 4 - this.update();
   }

   private int sync(int[] panel) {
      return panel[0] + 4;
   }

   private int flush(int[] panel) {
      return panel[2] - 4 - 10;
   }

   private int push() {
      List<HudElementSettingsScreen.Inner_StreamUtil> rows = this.init_List();
      int scaleKey = Math.round(autismclient.gui.sdf.MsdfTextRenderer.init_F() * 100.0F);
      if (rows != this.load_List || scaleKey != this.set) {
         int widest = 0;

         for (HudElementSettingsScreen.Inner_StreamUtil row : rows) {
            if (!row.init_Z()) {
               widest = Math.max(widest, autismclient.gui.f.b.k.init(this.font, row.load_Str, init_CharEncoder.init_Identifier_l(l.read), 0));
            }
         }

         this.load_List = rows;
         this.set = scaleKey;
         this.put = Math.min(177, widest + 8);
      }

      return this.put;
   }

   private int read(int rowsW) {
      return Math.max(1, rowsW - this.push() - 4);
   }

   private int write(int valueW) {
      return Math.max(20, valueW - 40 - 4);
   }

   private int init_I_I_I(int boxY, int boxH) {
      return UiLayoutMath.init(boxY, boxH, init_CharEncoder.init_I_l(l.read), init_CharEncoder.flush());
   }

   private boolean init_Z_Str(String key) {
      return AutismHudManager.init_Z_Str_Str(this.init_Str, key);
   }

   private String init_Str_D_D(double value, double step) {
      return step >= 1.0 ? Integer.toString((int)Math.round(value)) : String.format(Locale.ROOT, step < 0.01 ? "%.3f" : "%.2f", value);
   }

   private String init_Str_EventBus_StreamUti_D(HudElementSettingsScreen.Inner_StreamUtil row, double value) {
      return "gif-fps".equals(row.init_Str) && value <= 0.0 ? "Original" : this.init_Str_D_D(value, row.read);
   }

   private boolean init_Z(int y, int h, int top, int bottom) {
      return y + h >= top && y <= bottom;
   }

   private int init(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private boolean init(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxW) {
      String trimmed = autismclient.gui.f.b.k.init(this.font, text, maxW, init_CharEncoder.init_Identifier_l(l.read), color);
      autismclient.gui.f.b.k.init(graphics, this.font, trimmed, init_CharEncoder.init_Identifier_l(l.read), color, x, y, false);
   }

   private int update(int h) {
      return Math.max(3, (h - init_CharEncoder.init_I_l(l.read) + 1) / 2 + 1);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int w, int h, int color) {
      String trimmed = autismclient.gui.f.b.k.init(this.font, text, w - 8, init_CharEncoder.init_Identifier_l(l.read), color);
      int tw = autismclient.gui.f.b.k.init(this.font, trimmed, init_CharEncoder.init_Identifier_l(l.read), color);
      autismclient.gui.f.b.k.init(
         graphics, this.font, trimmed, init_CharEncoder.init_Identifier_l(l.read), color, x + Math.max(4, (w - tw) / 2), y + this.update(h), false
      );
   }

   private enum Inner_CachePool {
      BOOL,
      ENUM,
      COLOR,
      NUMBER,
      ITEM,
      PICK,
      MODULE_LIST,
      SECTION;
   }

   private record Inner_DataUtil(long init_J, List<HudElementSettingsScreen.Inner_StreamUtil> init_List) {
   }

   private record Inner_StreamUtil(
      String init_Str, String load_Str, HudElementSettingsScreen.Inner_CachePool init_EventBus_CachePool, double init_D, double load_D, double read, List<String> init_List
   ) {
      static HudElementSettingsScreen.Inner_StreamUtil init_EventBus_StreamUti_Str(String label) {
         return new HudElementSettingsScreen.Inner_StreamUtil("", label, HudElementSettingsScreen.Inner_CachePool.SECTION, 0.0, 0.0, 1.0, List.of());
      }

      static HudElementSettingsScreen.Inner_StreamUtil init_EventBus_StreamUti_Str_Str(String key, String label) {
         return new HudElementSettingsScreen.Inner_StreamUtil(key, label, HudElementSettingsScreen.Inner_CachePool.BOOL, 0.0, 1.0, 1.0, List.of());
      }

      static HudElementSettingsScreen.Inner_StreamUtil init_EventBus_StreamUti_Str_2(String key, String label, String... choices) {
         return new HudElementSettingsScreen.Inner_StreamUtil(key, label, HudElementSettingsScreen.Inner_CachePool.ENUM, 0.0, 1.0, 1.0, List.of(choices));
      }

      static HudElementSettingsScreen.Inner_StreamUtil load(String key, String label) {
         return new HudElementSettingsScreen.Inner_StreamUtil(key, label, HudElementSettingsScreen.Inner_CachePool.COLOR, 0.0, 1.0, 1.0, List.of());
      }

      static HudElementSettingsScreen.Inner_StreamUtil init_EventBus_StreamUti_Str(String key, String label, HudElementSettingsScreen.Inner_CachePool type, double min, double max, double step) {
         return new HudElementSettingsScreen.Inner_StreamUtil(key, label, type, min, max, step, List.of());
      }

      static HudElementSettingsScreen.Inner_StreamUtil read(String key, String label) {
         return new HudElementSettingsScreen.Inner_StreamUtil(key, label, HudElementSettingsScreen.Inner_CachePool.ITEM, 0.0, 1.0, 1.0, List.of());
      }

      static HudElementSettingsScreen.Inner_StreamUtil init_EventBus_StreamUti_Str(String key, String label, String buttonLabel) {
         return new HudElementSettingsScreen.Inner_StreamUtil(key, label, HudElementSettingsScreen.Inner_CachePool.PICK, 0.0, 1.0, 1.0, List.of(buttonLabel));
      }

      static HudElementSettingsScreen.Inner_StreamUtil write(String key, String label) {
         return new HudElementSettingsScreen.Inner_StreamUtil(key, label, HudElementSettingsScreen.Inner_CachePool.MODULE_LIST, 0.0, 1.0, 1.0, List.of());
      }

      boolean init_Z() {
         return this.init_EventBus_CachePool == HudElementSettingsScreen.Inner_CachePool.SECTION;
      }
   }
}
