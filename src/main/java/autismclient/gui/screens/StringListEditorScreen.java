package autismclient.gui.screens;

import autismclient.api.module.DisplayMode;
import autismclient.api.module.Setting;
import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.l;
import autismclient.modules.Module;
import autismclient.modules.al;
import autismclient.util.OnlinePlayerCollector;
import autismclient.util.TabPlayerListParser;
import autismclient.util.bi;
import autismclient.util.cA;
import autismclient.util.ThemePaletteManager;
import autismclient.util.PipeDelimitedListUtil;
import autismclient.util.ThemedTextInputBox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

public class StringListEditorScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 300;
   private static final int load_I = 4;
   private static final int read_I = 3;
   private static final int write_I = -872086265;
   private static final int update = -1441722092;
   private static final int sync = -1441000940;
   private static final int flush = -791321;
   private static final int close = -4743522;
   private static final int open = -50373;
   private final Screen init_Screen;
   private final Module init_Module;
   private final Setting<?, ?> init_Setting;
   private final List<StringListEditorScreen.Inner_StreamUtil> init_List = new ArrayList<>();
   private ThemedTextInputBox init_k;
   private ThemedTextInputBox load_k;
   private int poll;
   private boolean init_Z;
   private int push;
   private int pop = -1;
   private int tick = 0;
   private String init_Str;
   private String load_Str;
   private List<String> load_List = List.of();
   private List<String> read_List = List.of();
   private final List<String> write_List = new ArrayList<>();
   private final Map<String, String> init_Map = new HashMap<>();
   private boolean load_Z;

   private static int read_I_6() {
      return ThemePaletteManager.recolor(-4743522, ThemePaletteManager.ColorRole.TEXT);
   }

   public StringListEditorScreen(Screen parent, Module module, Setting<?, ?> option) {
      super(Component.literal(option == null ? "Edit List" : "Edit " + option.label()));
      this.init_Screen = parent;
      this.init_Module = module;
      this.init_Setting = option;
   }

   @Override
   protected void init() {
      this.flush_V_4();
      this.close_V_3();
      this.close_V_3();
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
         this.init_List.clear();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), -872086265);
         int x = this.run();
         int y = this.get();
         int panelW = this.set();
         int panelH = this.put();
         this.flush_V_4();
         this.close_V_3();
         this.init(graphics, x, y, panelW, panelH, this.update_Str(), mx, my);
         if (panelW < 180 || panelH < 100) {
            this.init(graphics, "Window too small.", x + 4, this.init_I_I_I_4(y + this.sync_I_4(), 12), read_I_6(), Math.max(0, panelW - 8));
            return;
         }

         int fieldX = x + 4;
         int fieldY = this.open_I_2();
         int fieldH = this.flush_I_4();
         this.init_k.render(graphics, mx, my, delta);
         int addW = this.update_I_5();
         this.init(
            graphics,
            this.pop >= 0 ? "Save" : "Add",
            x + panelW - 4 - addW,
            fieldY,
            addW,
            fieldH,
            autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
            StringListEditorScreen.Inner_CachePool.ADD,
            -1,
            mx,
            my
         );
         this.load(graphics, fieldX, fieldY, this.write_I_5(), mx, my);
         this.load_k.render(graphics, mx, my, delta);
         int actionsY = this.push();
         int nextX = fieldX;
         int clearW = this.init_I_Str_8("Clear");
         this.init(
            graphics,
            "Clear",
            nextX,
            actionsY,
            clearW,
            fieldH,
            autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER,
            StringListEditorScreen.Inner_CachePool.CLEAR,
            -1,
            mx,
            my
         );
         nextX += clearW + 2;
         String importLabel = this.init_Str_4();
         if (importLabel != null) {
            int importW = this.init_I_Str_8(importLabel);
            this.init(
               graphics,
               importLabel,
               nextX,
               actionsY,
               importW,
               fieldH,
               autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY,
               StringListEditorScreen.Inner_CachePool.IMPORT,
               -1,
               mx,
               my
            );
            nextX += importW + 2;
         }

         if (this.read_Z() || this.write_Z()) {
            int scanW = this.init_I_Str_8("Scan");
            this.init(
               graphics,
               "Scan",
               nextX,
               actionsY,
               scanW,
               fieldH,
               autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY,
               StringListEditorScreen.Inner_CachePool.SCAN,
               -1,
               mx,
               my
            );
            nextX += scanW + 2;
         }

         int summaryX = nextX + 4;
         String summary = this.update_Z()
            ? this.write_List.size() + " scanned | " + this.read_List().size() + " picked"
            : this.init_List_4().size() + " shown | " + this.read_List().size() + " total";
         this.init(graphics, summary, summaryX, this.init_I_I_I_4(actionsY, fieldH), read_I_6(), x + panelW - 4 - summaryX);
         int listY = this.pop();
         int listH = this.tick_I_7();
         autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.init_TextFormat_DataUti_I_I(listY, listH);
         this.init(graphics, fieldX, listY, panelW - 8, listH, 5, -1442248437, init_CharEncoder.set());
         listLayout.init(graphics);

         try {
            this.init(graphics, listLayout, mx, my);
         } finally {
            listLayout.load(graphics);
         }

         listLayout.init(graphics, mx, my, this.init_Z);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, autismclient.gui.ui.widgets.ScrollListLayout.ListLayout layout, int mx, int my) {
      if (this.update_Z()) {
         this.load(graphics, layout, mx, my);
      } else {
         List<String> rows = this.init_List_4();
         this.poll = this.init(this.poll, 0, layout.open());
         int x = layout.init_I() + layout.pop();
         int w = layout.push();
         int rowH = layout.update();
         if (rows.isEmpty()) {
            this.init(graphics, "No entries.", x + 4, this.init_I_I_I_4(layout.load() + layout.pop(), rowH), read_I_6(), w - 8);
         } else {
            List<String> allValues = this.read_List();
            int delH = rowH - 2;
            int delW = delH + 4;
            int delX = x + w - 2 - delW;
            int textW = delX - 2 - x;
            layout.init(rows.size(), (i, rowY) -> {
               String value = rows.get(i);
               boolean over = mx >= x && mx < x + w && my >= rowY && my < rowY + rowH;
               autismclient.gui.ui.widgets.PanelSurfaceHelper.init(graphics, x, rowY, w, rowH - 1, over ? -1441000940 : -1441722092);
               int delY = rowY + (rowH - delH) / 2;
               this.init(graphics, value, x + 4, this.init_I_I_I_4(rowY, rowH - 1), -791321, textW - 4);
               autismclient.gui.ui.widgets.ListRowRenderer.load(graphics, delX, delY, delW, delH, over);
               int index = this.init(rows, allValues, i);
               this.init_List.add(new StringListEditorScreen.Inner_StreamUtil(StringListEditorScreen.Inner_CachePool.EDIT_ENTRY, x, rowY, Math.max(1, textW), rowH, index));
               this.init_List.add(new StringListEditorScreen.Inner_StreamUtil(StringListEditorScreen.Inner_CachePool.REMOVE, delX, delY, delW, delH, index));
            });
         }
      }
   }

   private void load(GuiGraphicsExtractor graphics, autismclient.gui.ui.widgets.ScrollListLayout.ListLayout layout, int mx, int my) {
      List<StringListEditorScreen.Inner_DataUtil> rows = this.write_List();
      this.poll = this.init(this.poll, 0, layout.open());
      int x = layout.init_I() + layout.pop();
      int w = layout.push();
      int rowH = layout.update();
      if (rows.isEmpty()) {
         this.init(graphics, "No players found.", x + 4, this.init_I_I_I_4(layout.load() + layout.pop(), rowH), read_I_6(), w - 8);
      } else {
         layout.init(rows.size(), (i, rowY) -> {
            StringListEditorScreen.Inner_DataUtil row = rows.get(i);
            boolean over = mx >= x && mx < x + w && my >= rowY && my < rowY + rowH;
            int ty = this.init_I_I_I_4(rowY, rowH - 1);
            if (row.load_Str() == null) {
               autismclient.gui.ui.widgets.PanelSurfaceHelper.init(graphics, x, rowY, w, rowH - 1, over ? -1441000940 : -1441327333);
               String count = row.init_I() + "/" + row.load_I();
               int countW = autismclient.gui.f.b.k.init(this.font, count, init_CharEncoder.init_Identifier_l(l.read), 0);
               this.init(graphics, row.init_Str(), x + 4, ty, -791321, w - countW - 12);
               this.init(graphics, count, x + w - 4 - countW, ty, read_I_6(), countW + 1);
               this.init_List.add(new StringListEditorScreen.Inner_StreamUtil(StringListEditorScreen.Inner_CachePool.TOGGLE_RANK, x, rowY, w, rowH, i));
            } else {
               boolean picked = row.init_I() > 0;
               autismclient.gui.ui.widgets.PanelSurfaceHelper.init(graphics, x, rowY, w, rowH - 1, over ? -1441000940 : -1441722092);
               this.init(graphics, (picked ? "[x] " : "[ ] ") + row.load_Str(), x + 8, ty, picked ? -791321 : read_I_6(), w - 12);
               this.init_List.add(new StringListEditorScreen.Inner_StreamUtil(StringListEditorScreen.Inner_CachePool.TOGGLE_PLAYER, x, rowY, w, rowH, i));
            }
         });
      }
   }

   private void load(GuiGraphicsExtractor graphics, int fieldX, int fieldY, int fieldW, int mx, int my) {
      if (this.load_Z() && this.init_k != null && this.init_k.isFocused()) {
         List<String> suggestions = this.update_List();
         if (!suggestions.isEmpty()) {
            int visible = Math.min(5, suggestions.size());
            int rowH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
            int popupH = visible * rowH + 2;
            int popupY = Math.max(4, fieldY - popupH - 2);
            int popupW = Math.min(fieldW, 170);
            int popupX = fieldX;
            this.init(graphics, popupX, popupY, popupW, popupH, 5, -301397749, ThemePaletteManager.recolor(-50373, ThemePaletteManager.ColorRole.OUTLINE));
            int start = Math.max(0, Math.min(this.tick - visible + 1, suggestions.size() - visible));

            for (int i = 0; i < visible; i++) {
               int idx = start + i;
               String suggestion = suggestions.get(idx);
               int y = popupY + 1 + i * rowH;
               boolean selected = idx == this.tick;
               boolean over = mx >= popupX && mx < popupX + popupW && my >= y && my < y + rowH;
               autismclient.gui.ui.widgets.PanelSurfaceHelper.init(graphics, popupX + 1, y, popupW - 2, rowH - 1, !selected && !over ? -1441722092 : -1441000940);
               this.init(graphics, suggestion, popupX + 4, this.init_I_I_I_4(y, rowH - 1), -791321, popupW - 8);
               this.init_List.add(new StringListEditorScreen.Inner_StreamUtil(StringListEditorScreen.Inner_CachePool.SUGGESTION, popupX + 1, y, popupW - 2, rowH, idx));
            }
         }
      }
   }

   private void init(
      GuiGraphicsExtractor graphics,
      String label,
      int x,
      int y,
      int w,
      int h,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      StringListEditorScreen.Inner_CachePool type,
      int index,
      int mx,
      int my
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(graphics, this.font, x, y, w, h, label, variant, true, mx, my);
      this.init_List.add(new StringListEditorScreen.Inner_StreamUtil(type, x, y, w, h, index));
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int width, int height, String title, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      int headerH = this.sync_I_4();
      autismclient.gui.ui.widgets.DialogFrameRenderer.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my), bounds, headerH, title, mx >= x && mx < x + width && my >= y && my < y + headerH
      );
      autismclient.gui.f.NetHelper close = autismclient.gui.ui.widgets.DialogFrameRenderer.init(bounds, headerH);
      this.init_List.add(new StringListEditorScreen.Inner_StreamUtil(StringListEditorScreen.Inner_CachePool.CLOSE, close.read(), close.write(), close.update(), close.sync(), -1));
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.init_PoolEntry_DataUtil_3();
      if (metrics.isScrollable() && metrics.containsTrack(mx, my)) {
         this.init_Z = true;
         this.push = metrics.containsThumb(mx, my) ? my - metrics.thumbY() : metrics.thumbHeight() / 2;
         this.poll = this.init_I_I_6(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, my, this.push));
         return true;
      }

      this.flush_V_4();
      if (this.init_k.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
         this.load_k.setFocused(false);
         return true;
      }

      if (this.load_k.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
         this.init_k.setFocused(false);
         return true;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         StringListEditorScreen.Inner_StreamUtil hit = this.init_List.get(i);
         if (hit.init(mx, my) && (hit.init_DataPipe_CachePool != StringListEditorScreen.Inner_CachePool.REMOVE || this.init_Z_I_I(mx, my))) {
            switch (hit.init_DataPipe_CachePool) {
               case ADD:
                  this.load_V_9();
                  break;
               case EDIT_ENTRY:
                  this.init_V_I_4(hit.update);
                  break;
               case REMOVE:
                  this.load(hit.update);
                  break;
               case SUGGESTION:
                  this.read(hit.update);
                  break;
               case CLEAR:
                  this.init_V_Str_2("");
                  this.read_V_6();
                  this.open_V_2();
                  break;
               case SCAN:
                  if (this.write_Z()) {
                     this.write_V_5();
                  } else {
                     this.update_V_5();
                  }
                  break;
               case TOGGLE_PLAYER:
                  List<StringListEditorScreen.Inner_DataUtil> rowsx = this.write_List();
                  if (hit.update >= 0 && hit.update < rowsx.size()) {
                     this.load_V(rowsx.get(hit.update).load_Str());
                  }
                  break;
               case TOGGLE_RANK:
                  List<StringListEditorScreen.Inner_DataUtil> rows = this.write_List();
                  if (hit.update >= 0 && hit.update < rows.size()) {
                     this.read_V(rows.get(hit.update).init_Str());
                  }
                  break;
               case IMPORT:
                  this.sync_V_4();
                  break;
               case CLOSE:
                  this.onClose();
            }

            return true;
         }
      }

      this.init_k.setFocused(false);
      this.load_k.setFocused(false);
      return true;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int my = cA.init_I_D(mouseY);
      int mx = cA.init_I_D(mouseX);
      if (!this.init_Z_I_I(mx, my)) {
         return true;
      }

      this.poll = this.poll + (scrollY < 0.0 ? this.close_I_3() : -this.close_I_3());
      this.poll = this.init(this.poll, 0, this.init_PoolEntry_DataUtil_3().maxScroll());
      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      this.init_Z = false;
      if (this.init_k != null && this.init_k.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event))) {
         return true;
      } else {
         return this.load_k != null && this.load_k.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event)) ? true : true;
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      int my = cA.init_I_D(event.y());
      int mx = cA.init_I_D(event.x());
      if (this.init_k != null && this.init_k.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
         return true;
      }

      if (this.load_k != null && this.load_k.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
         return true;
      }

      if (!this.init_Z) {
         return true;
      }

      this.poll = this.init_I_I_6(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(this.init_PoolEntry_DataUtil_3(), my, this.push));
      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      this.flush_V_4();
      if (Compat263.keyEventGlfwKey(input) == 256) {
         if (this.pop >= 0) {
            if (!this.read_Str().trim().isEmpty()) {
               this.load_V_9();
            } else {
               this.read_V_6();
            }

            return true;
         } else {
            this.onClose();
            return true;
         }
      } else if (this.init_k.isFocused() && this.load_Z() && Compat263.keyEventGlfwKey(input) == 258) {
         List<String> suggestions = this.update_List();
         if (!suggestions.isEmpty()) {
            this.tick = Math.floorMod(this.tick + ((Compat263.keyEventGlfwModifiers(input) & 1) != 0 ? -1 : 1), suggestions.size());
            this.write(this.init_Str_Str_Str_2(this.read_Str(), suggestions.get(this.tick)));
         }

         return true;
      } else if (Compat263.keyEventGlfwKey(input) != 257 && Compat263.keyEventGlfwKey(input) != 335) {
         if (this.init_k.keyPressed(input)) {
            return true;
         } else {
            return this.load_k.keyPressed(input) ? true : true;
         }
      } else {
         if (this.init_k.isFocused()) {
            this.load_V_9();
         }

         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      this.flush_V_4();
      if (this.init_k.charTyped(input)) {
         return true;
      } else {
         return this.load_k.charTyped(input) ? true : true;
      }
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private void load_V_9() {
      String value = this.read_Str().trim();
      if (!value.isEmpty()) {
         List<String> values = this.load_List_2();
         if (this.pop >= 0 && this.pop < values.size()) {
            values.set(this.pop, value);
         } else {
            values.add(value);
         }

         this.init_V_Str_2(PipeDelimitedListUtil.init(values));
         this.write("");
         this.pop = -1;
         this.tick = 0;
         this.open_V_2();
      }
   }

   private void init_V_I_4(int index) {
      List<String> values = this.read_List();
      if (index >= 0 && index < values.size()) {
         this.pop = index;
         this.write(values.get(index));
         if (this.init_k != null) {
            this.init_k.setFocused(true);
         }

         if (this.load_k != null) {
            this.load_k.setFocused(false);
         }

         this.tick = 0;
      }
   }

   private void read_V_6() {
      this.pop = -1;
      this.write("");
      this.tick = 0;
   }

   private void load(int index) {
      List<String> values = this.load_List_2();
      if (index >= 0 && index < values.size()) {
         values.remove(index);
         this.init_V_Str_2(PipeDelimitedListUtil.init(values));
         if (this.pop == index) {
            this.read_V_6();
         } else if (this.pop > index) {
            this.pop--;
         }

         this.open_V_2();
      }
   }

   private boolean init_Z_4() {
      return this.init_Screen instanceof AutismModulesScreen moduleScreen && moduleScreen.init_Z_5();
   }

   private void init_V_Str_2(String value) {
      if (this.init_Z_4()) {
         this.init_Module.setConfiguredValue(this.init_Setting.id(), value);
      } else {
         this.init_Module.setValue(this.init_Setting.id(), value);
      }
   }

   private List<String> init_List_4() {
      String needle = this.write_Str().trim().toLowerCase(Locale.ROOT);
      List<String> values = this.read_List();
      if (needle.equals(this.load_Str)) {
         return this.read_List;
      }

      this.load_Str = needle;
      if (needle.isEmpty()) {
         this.read_List = values;
         return this.read_List;
      }

      List<String> out = new ArrayList<>();

      for (String value : values) {
         if (value.toLowerCase(Locale.ROOT).contains(needle)) {
            out.add(value);
         }
      }

      this.read_List = List.copyOf(out);
      return this.read_List;
   }

   private List<String> load_List_2() {
      return new ArrayList<>(this.read_List());
   }

   private List<String> read_List() {
      String rawValue = this.init_Module.value(this.init_Setting.id());
      if (!rawValue.equals(this.init_Str)) {
         this.init_Str = rawValue;
         this.load_List = List.copyOf(PipeDelimitedListUtil.init(rawValue));
         this.load_Str = null;
         this.read_List = List.of();
      }

      return this.load_List;
   }

   private int init(List<String> rows, List<String> allValues, int filteredIndex) {
      if (filteredIndex >= 0 && filteredIndex < rows.size()) {
         String value = rows.get(filteredIndex);
         int occurrence = 0;

         for (int i = 0; i <= filteredIndex; i++) {
            if (value.equals(rows.get(i))) {
               occurrence++;
            }
         }

         for (int i = 0; i < allValues.size(); i++) {
            if (value.equals(allValues.get(i))) {
               if (--occurrence == 0) {
                  return i;
               }
            }
         }

         return allValues.indexOf(value);
      } else {
         return -1;
      }
   }

   private boolean load_Z() {
      return this.init_Setting != null && this.init_Setting.displayMode() == DisplayMode.PLAYER_NAME_LIST;
   }

   private boolean read_Z() {
      return this.init_Setting != null && this.init_Setting.displayMode() == DisplayMode.RANK_TAG_LIST;
   }

   private boolean write_Z() {
      return this.init_Setting != null && this.init_Setting.displayMode() == DisplayMode.PLAYER_RANK_PICKER;
   }

   private boolean update_Z() {
      return this.write_Z() && this.load_Z;
   }

   private void write_V_5() {
      if (this.minecraft != null && this.minecraft.getConnection() != null) {
         this.write_List.clear();
         this.init_Map.clear();
         this.load_Z = true;
         this.poll = 0;

         for (TabPlayerListParser.ParsedPlayer player : TabPlayerListParser.init(this.minecraft)) {
            if (player != null && player.init_Str() != null && !player.init_Str().isBlank() && !load(this.write_List, player.init_Str())) {
               this.write_List.add(player.init_Str());
               if (player.init_Z()) {
                  this.init_Map.put(player.init_Str().toLowerCase(Locale.ROOT), player.load().trim());
               }
            }
         }

         this.write_List.sort(String::compareToIgnoreCase);
         bi.init("Scanned " + this.write_List.size() + " players.", -13248397);
         autismclient.util.BackgroundTaskExecutor.init_Future_Str_Runnable("player-rank-scan", () -> {
            List<String> probed = OnlinePlayerCollector.init(this.minecraft, true);
            if (!probed.isEmpty()) {
               this.minecraft.execute(() -> {
                  boolean changed = false;

                  for (String name : probed) {
                     if (name != null && !name.isBlank() && !load(this.write_List, name)) {
                        this.write_List.add(name);
                        changed = true;
                     }
                  }

                  if (changed) {
                     this.write_List.sort(String::compareToIgnoreCase);
                  }
               });
            }
         });
      } else {
         bi.read("Join a server first.");
      }
   }

   private String load_Str(String name) {
      String rank = name == null ? "" : this.init_Map.getOrDefault(name.toLowerCase(Locale.ROOT), "");
      return rank != null && !rank.isBlank() ? rank : "No rank";
   }

   private List<StringListEditorScreen.Inner_DataUtil> write_List() {
      String needle = this.write_Str().trim().toLowerCase(Locale.ROOT);
      List<String> selected = this.read_List();
      LinkedHashMap<String, List<String>> groups = new LinkedHashMap<>();
      List<String> manual = new ArrayList<>();

      for (String value : selected) {
         if (value != null && !value.isBlank() && !load(this.write_List, value)) {
            manual.add(value);
         }
      }

      if (!manual.isEmpty()) {
         groups.put("Not online", manual);
      }

      TreeMap<String, List<String>> byRank = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      for (String name : this.write_List) {
         byRank.computeIfAbsent(this.load_Str(name), ignored -> new ArrayList<>()).add(name);
      }

      List<String> rankless = byRank.remove("No rank");
      groups.putAll(byRank);
      if (rankless != null) {
         groups.put("No rank", rankless);
      }

      List<StringListEditorScreen.Inner_DataUtil> rows = new ArrayList<>();
      groups.forEach((rank, members) -> {
         List<String> shown = new ArrayList<>();

         for (String name : members) {
            if (needle.isEmpty() || name.toLowerCase(Locale.ROOT).contains(needle) || rank.toLowerCase(Locale.ROOT).contains(needle)) {
               shown.add(name);
            }
         }

         if (!shown.isEmpty()) {
            int picked = 0;

            for (String name : shown) {
               if (load(selected, name)) {
                  picked++;
               }
            }

            rows.add(new StringListEditorScreen.Inner_DataUtil(rank, null, picked, shown.size()));

            for (String name : shown) {
               rows.add(new StringListEditorScreen.Inner_DataUtil(rank, name, load(selected, name) ? 1 : 0, 1));
            }
         }
      });
      return rows;
   }

   private void load_V(String name) {
      if (name != null && !name.isBlank()) {
         List<String> values = this.load_List_2();
         if (!init_Z_List_Str(values, name)) {
            values.add(name);
         }

         this.init_V_Str_2(PipeDelimitedListUtil.init(values));
      }
   }

   private void read_V(String rank) {
      if (rank != null) {
         List<String> members = new ArrayList<>();

         for (StringListEditorScreen.Inner_DataUtil row : this.write_List()) {
            if (row.load_Str() != null && row.init_Str().equals(rank)) {
               members.add(row.load_Str());
            }
         }

         if (!members.isEmpty()) {
            List<String> values = this.load_List_2();
            boolean allPicked = true;

            for (String name : members) {
               if (!load(values, name)) {
                  allPicked = false;
                  break;
               }
            }

            for (String name : members) {
               if (allPicked) {
                  init_Z_List_Str(values, name);
               } else if (!load(values, name)) {
                  values.add(name);
               }
            }

            this.init_V_Str_2(PipeDelimitedListUtil.init(values));
         }
      }
   }

   private static boolean init_Z_List_Str(List<String> values, String target) {
      for (int i = 0; i < values.size(); i++) {
         if (values.get(i).equalsIgnoreCase(target)) {
            values.remove(i);
            return true;
         }
      }

      return false;
   }

   private void update_V_5() {
      if (this.read_Z() && this.minecraft != null && this.minecraft.getConnection() != null) {
         List<String> values = this.load_List_2();
         int added = 0;

         for (TabPlayerListParser.ParsedPlayer player : TabPlayerListParser.init(this.minecraft)) {
            if (player != null && player.init_Z()) {
               String tag = player.load().trim();
               if (!tag.isEmpty() && !load(values, tag)) {
                  values.add(tag);
                  added++;
               }
            }
         }

         if (added > 0) {
            this.init_V_Str_2(PipeDelimitedListUtil.init(values));
            this.open_V_2();
            bi.init("Rank scan added " + added + " tag" + (added == 1 ? "." : "s."), -13248397);
         } else {
            bi.read("Rank scan found nothing.");
         }
      } else {
         bi.read("Join a server first.");
      }
   }

   private static boolean load(List<String> values, String target) {
      for (String value : values) {
         if (value.equalsIgnoreCase(target)) {
            return true;
         }
      }

      return false;
   }

   private String init_Str_4() {
      if (this.init_Module == null || this.init_Setting == null) {
         return null;
      } else if ("teams".equals(this.init_Module.id()) && "friends".equals(this.init_Setting.id())) {
         return "Import Names";
      } else {
         return "name-censor".equals(this.init_Module.id()) && "names".equals(this.init_Setting.id()) ? "Import Teams" : null;
      }
   }

   private void sync_V_4() {
      String sourceModuleId = null;
      String sourceOptionId = null;
      if (this.init_Module != null && this.init_Setting != null) {
         if ("teams".equals(this.init_Module.id()) && "friends".equals(this.init_Setting.id())) {
            sourceModuleId = "name-censor";
            sourceOptionId = "names";
         } else if ("name-censor".equals(this.init_Module.id()) && "names".equals(this.init_Setting.id())) {
            sourceModuleId = "teams";
            sourceOptionId = "friends";
         }
      }

      Module source = sourceModuleId == null ? null : al.init_Module_Str(sourceModuleId);
      if (source == null) {
         bi.read("Nothing to import.");
      } else {
         List<String> values = this.load_List_2();
         int added = 0;

         for (String entry : PipeDelimitedListUtil.init(source.value(sourceOptionId))) {
            if (entry != null && !entry.isBlank() && !load(values, entry)) {
               values.add(entry.trim());
               added++;
            }
         }

         if (added > 0) {
            this.init_V_Str_2(PipeDelimitedListUtil.init(values));
            this.open_V_2();
            bi.init("Imported " + added + " name" + (added == 1 ? "." : "s."), -13248397);
         } else {
            bi.read("Nothing new to import.");
         }
      }
   }

   private List<String> update_List() {
      if (this.load_Z() && this.minecraft != null && this.minecraft.getConnection() != null) {
         String query = this.read_Str(this.read_Str()).toLowerCase(Locale.ROOT);
         List<String> names = new ArrayList<>();

         for (PlayerInfo info : this.minecraft.getConnection().getOnlinePlayers()) {
            if (info != null && info.getProfile() != null && info.getProfile().name() != null) {
               String name = info.getProfile().name();
               if (this.init_Z_Str(name) && (query.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(query)) && !names.contains(name)) {
                  names.add(name);
               }
            }
         }

         if (this.minecraft.level != null) {
            for (AbstractClientPlayer player : this.minecraft.level.players()) {
               if (player != null && player.getGameProfile() != null && player.getGameProfile().name() != null) {
                  String name = player.getGameProfile().name();
                  if (this.init_Z_Str(name) && (query.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(query)) && !names.contains(name)) {
                     names.add(name);
                  }
               }
            }
         }

         names.sort(String.CASE_INSENSITIVE_ORDER);
         if (this.tick >= names.size()) {
            this.tick = Math.max(0, names.size() - 1);
         }

         return names;
      } else {
         return List.of();
      }
   }

   private boolean init_Z_Str(String name) {
      String trimmed = name == null ? "" : name.trim();
      if (trimmed.length() < 4) {
         return false;
      }

      String lower = trimmed.toLowerCase(Locale.ROOT);
      if (lower.startsWith("slot_") && lower.length() > 5) {
         for (int i = 5; i < lower.length(); i++) {
            if (!Character.isDigit(lower.charAt(i))) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private String read_Str(String entry) {
      String value = entry == null ? "" : entry;
      int eq = value.indexOf(61);
      if (eq >= 0) {
         value = value.substring(0, eq);
      }

      return value.trim();
   }

   private String init_Str_Str_Str_2(String entry, String suggestion) {
      String value = entry == null ? "" : entry;
      int eq = value.indexOf(61);
      return eq >= 0 ? suggestion + value.substring(eq) : suggestion;
   }

   private void read(int index) {
      List<String> suggestions = this.update_List();
      if (index >= 0 && index < suggestions.size()) {
         this.tick = index;
         this.write(this.init_Str_Str_Str_2(this.read_Str(), suggestions.get(index)));
         if (this.init_k != null) {
            this.init_k.setFocused(true);
         }

         if (this.load_k != null) {
            this.load_k.setFocused(false);
         }
      }
   }

   private void flush_V_4() {
      if (this.init_k == null) {
         this.init_k = new ThemedTextInputBox(this.minecraft, this.font, 0, 0, 1, this.flush_I_4(), false);
         this.init_k.setMaxLength(512);
         this.init_k.setChangedListener(value -> this.tick = 0);
      }

      if (this.load_k == null) {
         this.load_k = new ThemedTextInputBox(this.minecraft, this.font, 0, 0, 1, this.flush_I_4(), false);
         this.load_k.setMaxLength(128);
         this.load_k.setPlaceholder(Component.literal("Search"));
         this.load_k.setChangedListener(value -> {
            this.load_Str = null;
            this.open_V_2();
         });
      }
   }

   private void close_V_3() {
      if (this.init_k != null && this.load_k != null) {
         int fieldX = this.run() + 4;
         int fieldH = this.flush_I_4();
         this.init_k.setX(fieldX);
         this.init_k.setY(this.open_I_2());
         this.init_k.setWidth(this.write_I_5());
         this.init_k.setHeight(fieldH);
         this.init_k.setPlaceholder(Component.literal(this.pop >= 0 ? "Edit entry" : this.load_Str()));
         this.load_k.setX(fieldX);
         this.load_k.setY(this.poll());
         this.load_k.setWidth(this.set() - 8);
         this.load_k.setHeight(fieldH);
      }
   }

   private String load_Str() {
      return this.init_Module != null
            && this.init_Setting != null
            && "name-censor".equals(this.init_Module.id())
            && "custom-aliases".equals(this.init_Setting.id())
         ? "Player=Alias"
         : "New entry";
   }

   private int write_I_5() {
      return Math.max(40, this.set() - 8 - 2 - this.update_I_5());
   }

   private int update_I_5() {
      return this.init_I_Str_8(this.pop >= 0 ? "Save" : "Add");
   }

   private int init_I_Str_8(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(autismclient.gui.f.b.k.init(this.font, label, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private int sync_I_4() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int flush_I_4() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int close_I_3() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int open_I_2() {
      return this.get() + this.sync_I_4() + 4;
   }

   private int poll() {
      return this.open_I_2() + this.flush_I_4() + 2;
   }

   private int push() {
      return this.poll() + this.flush_I_4() + 2;
   }

   private int pop() {
      return this.push() + this.flush_I_4() + 2;
   }

   private int tick_I_7() {
      return this.get() + this.put() - this.pop() - 4;
   }

   private int init_I_I_I_4(int boxY, int boxH) {
      return UiLayoutMath.centerTextY(boxY, boxH, init_CharEncoder.init_I_l(l.read), init_CharEncoder.flush());
   }

   private String read_Str() {
      return this.init_k == null ? "" : this.init_k.getValue();
   }

   private void write(String value) {
      this.flush_V_4();
      this.init_k.setValue(value == null ? "" : value);
      this.init_k.setCursorToEnd(this.init_k.getValue().length());
   }

   private String write_Str() {
      return this.load_k == null ? "" : this.load_k.getValue();
   }

   private String update_Str() {
      return this.init_Setting == null ? "Edit List" : "Edit " + this.init_Setting.label();
   }

   private int run() {
      return autismclient.gui.ui.layout.PanelLayoutMath.load(this.init_I(), this.set(), 6);
   }

   private int get() {
      return autismclient.gui.ui.layout.PanelLayoutMath.load(this.load_I(), this.put(), 6);
   }

   private int set() {
      return autismclient.gui.ui.layout.PanelLayoutMath.init(this.init_I(), 6, 300);
   }

   private int put() {
      int rowH = this.close_I_3();
      int chrome = this.sync_I_4() + 4 + (this.flush_I_4() + 2) * 3 + 6 + 4;
      int maxRows = Math.max(4, (int)(this.load_I() * 0.65F) / rowH);
      int content = this.update_Z() ? this.write_List.size() + this.read_List().size() : this.read_List().size();
      int rows = Math.max(4, Math.min(maxRows, content));
      return autismclient.gui.ui.layout.PanelLayoutMath.init(this.load_I(), 6, chrome + rows * rowH);
   }

   private int add() {
      return this.update_Z() ? this.write_List().size() : this.init_List_4().size();
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil_3() {
      return this.init_PoolEntry_DataUtil_I_I(this.pop(), this.tick_I_7());
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil_I_I(int listY, int listH) {
      return this.init_TextFormat_DataUti_I_I(listY, listH).init_PoolEntry_DataUtil();
   }

   private autismclient.gui.ui.widgets.ScrollListLayout.ListLayout init_TextFormat_DataUti_I_I(int listY, int listH) {
      return autismclient.gui.ui.widgets.ScrollListLayout.init(
         this.run() + 4, listY, this.set() - 8, listH, this.add(), this.close_I_3(), this.close_I_3(), this.poll, 4, 6, 3, 6, false
      );
   }

   private boolean init_Z_I_I(int mx, int my) {
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.init_TextFormat_DataUti_I_I(this.pop(), this.tick_I_7());
      return mx >= listLayout.init_I() + listLayout.pop()
         && mx < listLayout.init_I() + listLayout.pop() + listLayout.push()
         && my >= listLayout.load() + listLayout.pop()
         && my < listLayout.load() + listLayout.pop() + listLayout.flush();
   }

   private void open_V_2() {
      this.poll = this.init(this.poll, 0, this.init_PoolEntry_DataUtil_3().maxScroll());
   }

   private int init_I_I_6(int offset) {
      int rowH = this.close_I_3();
      return Math.max(0, offset / rowH) * rowH;
   }

   private int init(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int radius, int fill, int border) {
      autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), radius, fill, border);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      String display = autismclient.gui.f.b.k.init(
         this.font, text == null ? "" : text, Math.max(0, maxWidth), init_CharEncoder.init_Identifier_l(l.read), color
      );
      autismclient.gui.f.b.k.init(graphics, this.font, display, init_CharEncoder.init_Identifier_l(l.read), color, x, y, false);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int w, int h, int color) {
      String display = autismclient.gui.f.b.k.init(this.font, text == null ? "" : text, Math.max(0, w - 4), init_CharEncoder.init_Identifier_l(l.read), color);
      int tw = autismclient.gui.f.b.k.init(this.font, display, init_CharEncoder.init_Identifier_l(l.read), color);
      int th = init_CharEncoder.init_I_l(l.read);
      int drawX = x + Math.max(2, (w - tw) / 2);
      int drawY = y + Math.max(1, (h - th + 1) / 2 + 1);
      autismclient.gui.f.b.k.init(graphics, this.font, display, init_CharEncoder.init_Identifier_l(l.read), color, drawX, drawY, false);
   }

   private enum Inner_CachePool {
      ADD,
      EDIT_ENTRY,
      REMOVE,
      SUGGESTION,
      CLEAR,
      SCAN,
      TOGGLE_PLAYER,
      TOGGLE_RANK,
      IMPORT,
      CLOSE;
   }

   private record Inner_DataUtil(String init_Str, String load_Str, int init_I, int load_I) {
   }

   private record Inner_StreamUtil(StringListEditorScreen.Inner_CachePool init_DataPipe_CachePool, int init_I, int load, int read, int write, int update) {
      boolean init(int mx, int my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write;
      }
   }
}
