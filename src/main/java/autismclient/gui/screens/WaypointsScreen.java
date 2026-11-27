package autismclient.gui.screens;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.l;
import autismclient.modules.al;
import autismclient.modules.WaypointsModule;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismWaypoints;
import autismclient.util.bi;
import autismclient.util.cA;
import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemedTextInputBox;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

public class WaypointsScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 340;
   private static final int load_I = 18;
   private static final int read_I = 10;
   private static final int write = 120;
   private static final int update = 64;
   private static final int sync = 2;
   private static final int flush = 3;
   private static final int close = -872086265;
   private static final int open = -1441722092;
   private static final int poll = -1441000940;
   private static final int push = -791321;
   private static final int pop = -4743522;
   private static final int tick = -50373;
   private static final int run = -12855297;
   private static final int[] init_IArr = new int[]{-50373, -24806, -5829, -13248397, -12855297, -11895553, -4957185, -37177, -1, -7566196};
   private final Screen init_Screen;
   private final String init_Str;
   private final List<WaypointsScreen.Inner_DataUtil> init_List = new ArrayList<>();
   private final Map<String, ThemedTextInputBox> init_Map = new LinkedHashMap<>();
   private final List<String> load_List = new ArrayList<>();
   private ThemedTextInputBox init_k;
   private ThemedTextInputBox load_k;
   private int get;
   private boolean init_Z;
   private int set;
   private String load_Str;
   private String read_Str;
   private boolean load_Z;
   private autismclient.gui.f.NetHelper init_NetHelper;
   private long init_J = -9223372036854775808L;
   private List<AutismWaypoints.Waypoint> read_List = List.of();

   private static int read_I_17() {
      return ThemePaletteManager.recolor(-4743522, ThemePaletteManager.ColorRole.TEXT);
   }

   public WaypointsScreen(Screen parent) {
      super(Component.literal("Waypoints"));
      this.init_Screen = parent;
      this.init_Str = AuditWaypointScope.current(Minecraft.getInstance());
   }

   @Override
   protected void init() {
      this.flush_V_9();
      this.close_V_7();
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
         this.init_NetHelper = null;
         this.load_List.clear();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -872086265);
         int x = this.get();
         int y = this.set();
         int panelW = this.put();
         int panelH = this.add();
         this.flush_V_9();
         this.close_V_7();
         this.read_V_17();
         this.init_V_GuiGraphicsExtract_7(graphics, x, y, panelW, panelH, mx, my);
         if (panelW < 180 || this.run() < this.close_I_7()) {
            this.init(graphics, "Window too small.", x + 4, this.init_I_6(y + this.sync_I_11() + 4, 12), read_I_17(), Math.max(0, panelW - 8));
            return;
         }

         int ctl = this.flush_I_9();
         int fieldX = x + 4;
         int fieldY = this.push();
         this.init_k.render(graphics, mx, my, delta);
         int addW = this.update_I_14();
         this.init(
            graphics,
            "Add",
            x + panelW - 4 - addW,
            fieldY,
            addW,
            ctl,
            autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
            WaypointsScreen.Inner_StreamUtil.ADD,
            "",
            mx,
            my
         );
         int scopeY = fieldY + ctl + 2;
         String scopeLabel = "unknown".equals(this.init_Str)
            ? "Scope: unknown (join a server or world)"
            : "Scope: " + AuditWaypointScope.display(this.init_Str);
         this.init(graphics, scopeLabel, fieldX, this.init_I_6(scopeY, 12), read_I_17(), panelW - 8);
         if (this.load_Str != null) {
            this.init_V_GuiGraphicsExtract_6(graphics, fieldX, scopeY + 12 + 2, panelW - 8, delta, mx, my);
         }

         int listY = this.tick_I_15();
         int listH = this.run();
         autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.init_TextFormat_DataUti(listY, listH);
         this.load(graphics, fieldX, listY, panelW - 8, listH, -1442248437, init_CharEncoder.set());
         listLayout.pushContentClip(graphics);

         try {
            this.init(graphics, listLayout, delta, mx, my);
         } finally {
            listLayout.popContentClip(graphics);
         }

         listLayout.renderScrollbar(graphics, mx, my, this.init_Z);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, autismclient.gui.ui.widgets.ScrollListLayout.ListLayout layout, float delta, int mx, int my) {
      List<AutismWaypoints.Waypoint> rows = this.init_List_14();
      this.get = this.init_I_I_4(this.get, 0, layout.maxScroll());
      int x = layout.x() + layout.contentInset();
      int w = layout.contentWidth();
      int rowH = this.close_I_7();
      if (rows.isEmpty()) {
         this.init(graphics, "No waypoints for this scope.", x + 4, this.init_I_6(layout.y() + layout.contentInset(), rowH), read_I_17(), w - 8);
      } else {
         int del = rowH - 2;
         layout.forEachVisibleRow(
            rows.size(),
            (i, rowY) -> {
               AutismWaypoints.Waypoint waypoint = rows.get(i);
               this.load_List.add(waypoint.name());
               boolean over = mx >= x && mx < x + w && my >= rowY && my < rowY + rowH;
               autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(graphics, x, rowY, w, rowH, over ? -1441000940 : -1441722092);
               int swatchX = x + 3;
               int swatchY = rowY + (rowH - 10) / 2;
               boolean swatchOver = mx >= swatchX && mx < swatchX + 18 && my >= swatchY && my < swatchY + 10;
               autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(swatchX, swatchY, 18, 10), waypoint.color());
               autismclient.gui.f.PacketUtil.load(
                  graphics,
                  autismclient.gui.f.NetHelper.init(swatchX, swatchY, 18, 10),
                  !swatchOver && !waypoint.name().equals(this.load_Str) ? init_CharEncoder.set() : ThemePaletteManager.recolor(-50373, ThemePaletteManager.ColorRole.OUTLINE)
               );
               int nameX = swatchX + 18 + 4;
               ThemedTextInputBox nameField = this.init_Map.get(waypoint.name());
               if (nameField != null) {
                  nameField.setX(nameX);
                  nameField.setY(rowY + 1);
                  nameField.setWidth(120);
                  nameField.setHeight(rowH - 2);
                  nameField.render(graphics, mx, my, delta);
               }

               int delX = x + w - 2 - del;
               int delY = rowY + (rowH - del) / 2;
               int coordsX = nameX + 120 + 8;
               this.init(graphics, waypoint.x() + " " + waypoint.y() + " " + waypoint.z(), coordsX, this.init_I_6(rowY, rowH), read_I_17(), delX - 4 - coordsX);
               autismclient.gui.ui.widgets.ListRowRenderer.drawDeleteButton(graphics, delX, delY, del, del, over);
               this.init_List.add(new WaypointsScreen.Inner_DataUtil(WaypointsScreen.Inner_StreamUtil.ROW, x, rowY, w, rowH, waypoint.name()));
               this.init_List.add(new WaypointsScreen.Inner_DataUtil(WaypointsScreen.Inner_StreamUtil.SWATCH, swatchX, swatchY, 18, 10, waypoint.name()));
               this.init_List.add(new WaypointsScreen.Inner_DataUtil(WaypointsScreen.Inner_StreamUtil.REMOVE, delX, delY, del, del, waypoint.name()));
            }
         );
      }
   }

   private void init_V_GuiGraphicsExtract_6(GuiGraphicsExtractor graphics, int x, int y, int w, float delta, int mx, int my) {
      int ph = this.poll();
      this.init_NetHelper = autismclient.gui.f.NetHelper.init(x, y, w, ph);
      this.load(graphics, x, y, w, ph, -301266162, init_CharEncoder.set());
      int current = AutismWaypoints.getInstance().colorOf(this.init_Str, this.load_Str, write_I_15());
      int swatchY = y + 4;
      int swatchX = x + 4;

      for (int i = 0; i < init_IArr.length; i++) {
         int px = swatchX + i * 22;
         if (px + 18 > x + w - 4) {
            break;
         }

         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(px, swatchY, 18, 10), init_IArr[i]);
         boolean over = mx >= px && mx < px + 18 && my >= swatchY && my < swatchY + 10;
         autismclient.gui.f.PacketUtil.load(
            graphics, autismclient.gui.f.NetHelper.init(px, swatchY, 18, 10), !over && init_IArr[i] != current ? init_CharEncoder.set() : -1
         );
         this.init_List.add(new WaypointsScreen.Inner_DataUtil(WaypointsScreen.Inner_StreamUtil.PRESET, px, swatchY, 18, 10, Integer.toHexString(i)));
      }

      int ctl = this.flush_I_9();
      int hexY = swatchY + 10 + 4;
      int hexX = x + 4;
      this.load_k.setX(hexX);
      this.load_k.setY(hexY);
      this.load_k.setWidth(64);
      this.load_k.setHeight(ctl);
      this.load_k.render(graphics, mx, my, delta);
      int previewX = hexX + 64 + 4;
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(previewX, hexY, 18, ctl), current);
      autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(previewX, hexY, 18, ctl), init_CharEncoder.set());
      int hintX = previewX + 18 + 8;
      this.init(graphics, "# hex applies live", hintX, this.init_I_6(hexY, ctl), read_I_17(), x + w - hintX - 4);
   }

   private void init(
      GuiGraphicsExtractor graphics,
      String label,
      int x,
      int y,
      int w,
      int h,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      WaypointsScreen.Inner_StreamUtil type,
      String value,
      int mx,
      int my
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(graphics, this.font, x, y, w, h, label, variant, true, mx, my);
      this.init_List.add(new WaypointsScreen.Inner_DataUtil(type, x, y, w, h, value));
   }

   private void init_V_GuiGraphicsExtract_7(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      int headerH = this.sync_I_11();
      autismclient.gui.ui.widgets.DialogFrameRenderer.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my), bounds, headerH, "Waypoints", mx >= x && mx < x + width && my >= y && my < y + headerH
      );
      autismclient.gui.f.NetHelper close = autismclient.gui.ui.widgets.DialogFrameRenderer.init(bounds, headerH);
      this.init_List.add(new WaypointsScreen.Inner_DataUtil(WaypointsScreen.Inner_StreamUtil.CLOSE, close.read(), close.write(), close.update(), close.sync(), ""));
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      ThemedTextInputBox focused = this.read_Str == null ? null : this.init_Map.get(this.read_Str);
      if (focused != null && !init_Z_k(focused, mx, my)) {
         this.write_V_15();
      }

      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.init_PoolEntry_DataUtil_8();
      if (metrics.isScrollable() && metrics.containsTrack(mx, my)) {
         this.init_Z = true;
         this.set = metrics.containsThumb(mx, my) ? my - metrics.thumbY() : metrics.thumbHeight() / 2;
         this.get = this.init_I_I_15(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, my, this.set));
         return true;
      }

      this.flush_V_9();
      if (this.load_Str != null && this.load_k.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
         this.init_k.setFocused(false);
         return true;
      }

      if (this.init_k.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
         this.load_k.setFocused(false);
         return true;
      }

      for (String name : this.load_List) {
         ThemedTextInputBox rowField = this.init_Map.get(name);
         if (rowField != null && rowField.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
            this.read(name);
            return true;
         }
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         WaypointsScreen.Inner_DataUtil hit = this.init_List.get(i);
         if (hit.init(mx, my)
            && (
               hit.init_b_StreamUtil != WaypointsScreen.Inner_StreamUtil.REMOVE
                     && hit.init_b_StreamUtil != WaypointsScreen.Inner_StreamUtil.SWATCH
                     && hit.init_b_StreamUtil != WaypointsScreen.Inner_StreamUtil.ROW
                  || this.init_Z(mx, my)
            )) {
            switch (hit.init_b_StreamUtil) {
               case ADD:
                  this.load_V_24();
                  break;
               case SWATCH:
                  this.write(hit.init_Str);
                  break;
               case PRESET:
                  this.update(hit.init_Str);
                  break;
               case REMOVE:
                  this.init_V_Str_10(hit.init_Str);
                  break;
               case ROW:
                  this.load(hit.init_Str);
                  break;
               case CLOSE:
                  this.onClose();
            }

            return true;
         }
      }

      if (this.load_Str != null && (this.init_NetHelper == null || !this.init_NetHelper.init_Z(mx, my))) {
         this.sync_V_11();
      }

      this.init_k.setFocused(false);
      this.load_k.setFocused(false);
      return true;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      if (!this.init_Z(mx, my)) {
         return true;
      }

      this.get = this.get + (scrollY < 0.0 ? this.open_I_6() : -this.open_I_6());
      this.get = this.init_I_I_4(this.get, 0, this.init_PoolEntry_DataUtil_8().maxScroll());
      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      this.init_Z = false;
      ThemedTextInputBox focused = this.read_Str == null ? null : this.init_Map.get(this.read_Str);
      if (focused != null && focused.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event))) {
         return true;
      } else if (this.init_k != null && this.init_k.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event))) {
         return true;
      } else {
         return this.load_k != null && this.load_k.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event)) ? true : true;
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      ThemedTextInputBox focused = this.read_Str == null ? null : this.init_Map.get(this.read_Str);
      if (focused != null && focused.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
         return true;
      }

      if (this.init_k != null && this.init_k.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
         return true;
      }

      if (this.load_k != null && this.load_k.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
         return true;
      }

      if (!this.init_Z) {
         return true;
      }

      this.get = this.init_I_I_15(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(this.init_PoolEntry_DataUtil_8(), my, this.set));
      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      this.flush_V_9();
      if (Compat263.keyEventGlfwKey(input) == 256) {
         if (this.read_Str != null) {
            this.update_V_14();
         } else if (this.load_Str != null) {
            this.sync_V_11();
         } else {
            this.onClose();
         }

         return true;
      } else if (Compat263.keyEventGlfwKey(input) != 257 && Compat263.keyEventGlfwKey(input) != 335) {
         ThemedTextInputBox focused = this.read_Str == null ? null : this.init_Map.get(this.read_Str);
         if (focused != null && focused.keyPressed(input)) {
            return true;
         } else if (this.init_k.keyPressed(input)) {
            return true;
         } else {
            return this.load_Str != null && this.load_k.keyPressed(input) ? true : true;
         }
      } else {
         if (this.read_Str != null) {
            this.write_V_15();
         } else if (this.init_k.isFocused()) {
            this.load_V_24();
         }

         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      this.flush_V_9();
      ThemedTextInputBox focused = this.read_Str == null ? null : this.init_Map.get(this.read_Str);
      if (focused != null && focused.charTyped(input)) {
         return true;
      } else if (this.init_k.charTyped(input)) {
         return true;
      } else {
         return this.load_Str != null && this.load_k.charTyped(input) ? true : true;
      }
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private void load_V_24() {
      if (AuditWaypointScope.canAdd(Minecraft.getInstance(), this.init_Str)) {
         if (this.minecraft != null && this.minecraft.player != null) {
            AutismWaypoints store = AutismWaypoints.getInstance();
            String name = this.init_Str_13().trim();
            if (name.isEmpty()) {
               name = store.nextName(this.init_Str, "Waypoint");
            }

            int x = (int)Math.floor(this.minecraft.player.getX());
            int y = (int)Math.floor(this.minecraft.player.getY());
            int z = (int)Math.floor(this.minecraft.player.getZ());
            store.init(this.init_Str, new AutismWaypoints.Waypoint(name, x, y, z, write_I_15(), System.currentTimeMillis()));
            this.flush("");
            this.open_V_6();
         } else {
            bi.read("Join a world to add a waypoint here.");
         }
      }
   }

   private void init_V_Str_10(String name) {
      AutismWaypoints.getInstance().remove(this.init_Str, name);
      if (name != null && name.equals(this.load_Str)) {
         this.sync_V_11();
      }

      if (name != null && name.equals(this.read_Str)) {
         this.read_Str = null;
      }

      this.open_V_6();
   }

   private void load(String name) {
      for (AutismWaypoints.Waypoint waypoint : this.init_List_14()) {
         if (waypoint.name().equals(name)) {
            String coords = waypoint.x() + " " + waypoint.y() + " " + waypoint.z();
            Minecraft.getInstance().keyboardHandler.setClipboard(coords);
            bi.init("Copied coordinates: " + coords + ".");
            return;
         }
      }
   }

   private void read_V_17() {
      List<AutismWaypoints.Waypoint> rows = this.init_List_14();
      Map<String, ThemedTextInputBox> alive = new LinkedHashMap<>();

      for (AutismWaypoints.Waypoint waypoint : rows) {
         ThemedTextInputBox field = this.init_Map.get(waypoint.name());
         if (field == null) {
            field = new ThemedTextInputBox(this.minecraft, this.font, 0, 0, 1, this.close_I_7() - 2, false);
            field.setMaxLength(64);
            field.setValue(waypoint.name());
         } else if (!waypoint.name().equals(this.read_Str) && !waypoint.name().equals(field.getValue())) {
            field.setValue(waypoint.name());
         }

         alive.put(waypoint.name(), field);
      }

      if (this.read_Str != null && !alive.containsKey(this.read_Str)) {
         this.read_Str = null;
      }

      this.init_Map.clear();
      this.init_Map.putAll(alive);
   }

   private void read(String name) {
      if (this.read_Str != null && !this.read_Str.equals(name)) {
         this.write_V_15();
      }

      this.read_Str = name;
      ThemedTextInputBox field = this.init_Map.get(name);
      if (field != null) {
         field.setFocused(true);
         field.setSelectionEnd(field.getValue().length());
      }

      if (this.init_k != null) {
         this.init_k.setFocused(false);
      }

      if (this.load_k != null) {
         this.load_k.setFocused(false);
      }
   }

   private void write_V_15() {
      if (this.read_Str != null) {
         String original = this.read_Str;
         ThemedTextInputBox field = this.init_Map.get(original);
         this.read_Str = null;
         if (field != null) {
            field.setFocused(false);
            String renamed = field.getValue().trim();
            if (renamed.equals(original)) {
               field.setValue(original);
            } else if (AutismWaypoints.getInstance().init(this.init_Str, original, renamed)) {
               this.init_Map.remove(original);
               this.init_Map.put(renamed, field);
               field.setValue(renamed);
               if (original.equals(this.load_Str)) {
                  this.load_Str = renamed;
               }
            } else {
               field.setValue(original);
            }
         }
      }
   }

   private void update_V_14() {
      if (this.read_Str != null) {
         ThemedTextInputBox field = this.init_Map.get(this.read_Str);
         if (field != null) {
            field.setValue(this.read_Str);
            field.setFocused(false);
         }

         this.read_Str = null;
      }
   }

   private static boolean init_Z_k(ThemedTextInputBox field, int mx, int my) {
      return mx >= field.getX() && mx < field.getX() + field.getWidth() && my >= field.getY() && my < field.getY() + field.getHeight();
   }

   private void write(String name) {
      this.load_Str = name;
      this.load_Z = true;

      try {
         this.load_k
            .setValue(String.format(Locale.ROOT, "%06X", AutismWaypoints.getInstance().colorOf(this.init_Str, name, write_I_15()) & 16777215));
      } finally {
         this.load_Z = false;
      }

      this.load_k.setFocused(true);
      this.init_k.setFocused(false);
   }

   private void sync_V_11() {
      this.load_Str = null;
      if (this.load_k != null) {
         this.load_k.setFocused(false);
      }
   }

   private void update(String hexIndex) {
      if (this.load_Str != null) {
         int index;
         try {
            index = Integer.parseInt(hexIndex, 16);
         } catch (NumberFormatException e) {
            return;
         }

         if (index >= 0 && index < init_IArr.length) {
            AutismWaypoints.getInstance().setColor(this.init_Str, this.load_Str, init_IArr[index]);
            this.load_Z = true;

            try {
               this.load_k.setValue(String.format(Locale.ROOT, "%06X", init_IArr[index] & 16777215));
            } finally {
               this.load_Z = false;
            }
         }
      }
   }

   private void sync(String raw) {
      if (!this.load_Z && this.load_Str != null) {
         Integer color = init_Integer_Str(raw);
         if (color != null) {
            AutismWaypoints.getInstance().setColor(this.init_Str, this.load_Str, color);
         }
      }
   }

   private static Integer init_Integer_Str(String raw) {
      if (raw == null) {
         return null;
      }

      String text = raw.trim();
      if (text.startsWith("#")) {
         text = text.substring(1);
      }

      if (text.length() != 6) {
         return null;
      }

      try {
         return 0xFF000000 | Integer.parseInt(text, 16);
      } catch (NumberFormatException e) {
         return null;
      }
   }

   private static int write_I_15() {
      return al.init_Module_Str("waypoints") instanceof WaypointsModule waypoints ? waypoints.init_I_23() : -12855297;
   }

   private List<AutismWaypoints.Waypoint> init_List_14() {
      AutismWaypoints store = AutismWaypoints.getInstance();
      long revision = store.revision();
      if (revision != this.init_J) {
         this.init_J = revision;
         this.read_List = store.list(this.init_Str);
      }

      return this.read_List;
   }

   private void flush_V_9() {
      if (this.init_k == null) {
         this.init_k = new ThemedTextInputBox(this.minecraft, this.font, 0, 0, 1, this.flush_I_9(), false);
         this.init_k.setMaxLength(64);
         this.init_k.setPlaceholder(Component.literal("Waypoint name"));
      }

      if (this.load_k == null) {
         this.load_k = new ThemedTextInputBox(this.minecraft, this.font, 0, 0, 1, this.flush_I_9(), false);
         this.load_k.setMaxLength(7);
         this.load_k.setPlaceholder(Component.literal("RRGGBB"));
         this.load_k.setFilter(text -> {
            if (text != null && !text.isEmpty()) {
               int start = text.startsWith("#") ? 1 : 0;
               if (text.length() - start > 6) {
                  return false;
               }

               for (int i = start; i < text.length(); i++) {
                  char chr = text.charAt(i);
                  boolean hex = chr >= '0' && chr <= '9' || chr >= 'a' && chr <= 'f' || chr >= 'A' && chr <= 'F';
                  if (!hex) {
                     return false;
                  }
               }

               return true;
            } else {
               return true;
            }
         });
         this.load_k.setChangedListener(this::sync);
      }
   }

   private void close_V_7() {
      if (this.init_k != null) {
         this.init_k.setX(this.get() + 4);
         this.init_k.setY(this.push());
         this.init_k.setWidth(Math.max(40, this.put() - this.update_I_14() - 8 - 2));
         this.init_k.setHeight(this.flush_I_9());
      }
   }

   private int update_I_14() {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(autismclient.gui.f.b.k.init(this.font, "Add", init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private String init_Str_13() {
      return this.init_k == null ? "" : this.init_k.getValue();
   }

   private void flush(String value) {
      this.flush_V_9();
      this.init_k.setValue(value == null ? "" : value);
      this.init_k.setSelectionEnd(this.init_k.getValue().length());
   }

   private int sync_I_11() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int flush_I_9() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int close_I_7() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int open_I_6() {
      return this.close_I_7() + 2;
   }

   private int poll() {
      return 18 + this.flush_I_9() + 4;
   }

   private int push() {
      return this.set() + this.sync_I_11() + 4;
   }

   private int pop() {
      int y = this.sync_I_11() + 4 + this.flush_I_9() + 2 + 12 + 2;
      return this.load_Str != null ? y + this.poll() + 2 : y;
   }

   private int tick_I_15() {
      return this.set() + this.pop();
   }

   private int run() {
      return Math.max(1, this.set() + this.add() - 4 - this.tick_I_15());
   }

   private autismclient.gui.ui.widgets.ScrollListLayout.ListLayout init_TextFormat_DataUti(int listY, int listH) {
      return autismclient.gui.ui.widgets.ScrollListLayout.compute(
         this.get() + 4, listY, this.put() - 8, listH, this.init_List_14().size(), this.close_I_7(), this.open_I_6(), this.get, 4, 8, 2, 6, false
      );
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil_8() {
      return this.init_TextFormat_DataUti(this.tick_I_15(), this.run()).scrollbar();
   }

   private boolean init_Z(int mx, int my) {
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.init_TextFormat_DataUti(this.tick_I_15(), this.run());
      return mx >= listLayout.x() + listLayout.contentInset()
         && mx < listLayout.x() + listLayout.contentInset() + listLayout.contentWidth()
         && my >= listLayout.y() + listLayout.contentInset()
         && my < listLayout.y() + listLayout.contentInset() + listLayout.viewHeight();
   }

   private void open_V_6() {
      this.get = this.init_I_I_4(this.get, 0, this.init_PoolEntry_DataUtil_8().maxScroll());
   }

   private int init_I_I_15(int offset) {
      return Math.max(0, offset / this.open_I_6()) * this.open_I_6();
   }

   private int init_I_I_4(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private int get() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualWidth(), this.put(), 6);
   }

   private int set() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualHeight(), this.add(), 6);
   }

   private int put() {
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualWidth(), 6, 340);
   }

   private int add() {
      int cap = Math.max(3, this.virtualHeight() * 65 / 100 / this.open_I_6());
      int shown = Math.max(3, Math.min(cap, this.init_List_14().size()));
      int content = this.pop() + shown * this.open_I_6() + 4 + 4;
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualHeight(), 6, content);
   }

   private void load(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int fill, int border) {
      autismclient.gui.f.PacketUtil.init(
         graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), fill, ThemePaletteManager.recolor(-1721357268, ThemePaletteManager.ColorRole.OUTLINE)
      );
   }

   private int init_I_6(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).centerTextY(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      String display = autismclient.gui.f.b.k.init(
         this.font, text == null ? "" : text, Math.max(0, maxWidth), init_CharEncoder.init_Identifier_l(l.read), color
      );
      autismclient.gui.f.b.k.init(graphics, this.font, display, init_CharEncoder.init_Identifier_l(l.read), color, x, y, false);
   }

   private record Inner_DataUtil(WaypointsScreen.Inner_StreamUtil init_b_StreamUtil, int init_I, int load, int read, int write, String init_Str) {
      boolean init(int mx, int my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write;
      }
   }

   private enum Inner_StreamUtil {
      ADD,
      SWATCH,
      PRESET,
      REMOVE,
      ROW,
      CLOSE;
   }
}
