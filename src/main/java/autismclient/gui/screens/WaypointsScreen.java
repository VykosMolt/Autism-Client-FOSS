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
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
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
   private static final int[] PRESET_COLORS = new int[]{-50373, -24806, -5829, -13248397, -12855297, -11895553, -4957185, -37177, -1, -7566196};
   private final Screen parent;
   private final String scope;
   private final List<WaypointsScreen.HitRegion> init_List = new ArrayList<>();
   private final Map<String, ThemedTextInputBox> renameInputs = new LinkedHashMap<>();
   private final List<String> load_List = new ArrayList<>();
   private ThemedTextInputBox nameInput;
   private ThemedTextInputBox hexInput;
   private int listScroll;
   private boolean draggingScrollbar;
   private int scrollbarGrabOffset;
   private String colorEditName;
   private String focusedName;
   private boolean load_Z;
   private autismclient.gui.f.NetHelper colorPanel;
   private long revision = -9223372036854775808L;
   private List<AutismWaypoints.Waypoint> cachedWaypoints = List.of();

   private static int labelTextColor() {
      return ThemePaletteManager.recolor(-4743522, ThemePaletteManager.ColorRole.TEXT);
   }

   public WaypointsScreen(Screen parent) {
      super(Component.literal("Waypoints"));
      this.parent = parent;
      this.scope = AuditWaypointScope.current(Minecraft.getInstance());
   }

   @Override
   protected void init() {
      this.ensureNameInput();
      this.layoutNameInput();
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
         this.colorPanel = null;
         this.load_List.clear();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -872086265);
         int x = this.panelX();
         int y = this.panelY();
         int panelW = this.panelWidth();
         int panelH = this.panelHeight();
         this.ensureNameInput();
         this.layoutNameInput();
         this.syncNameInputs();
         this.init_V_GuiGraphicsExtract_7(graphics, x, y, panelW, panelH, mx, my);
         if (panelW < 180 || this.listHeight() < this.rowHeight()) {
            this.drawTrimmed(graphics, "Window too small.", x + 4, this.centeredTextY(y + this.headerHeight() + 4, 12), labelTextColor(), Math.max(0, panelW - 8));
            return;
         }

         int ctl = this.controlHeight();
         int fieldX = x + 4;
         int fieldY = this.nameFieldY();
         this.nameInput.render(graphics, mx, my, delta);
         int addW = this.addButtonWidth();
         this.renderToneButton(
            graphics,
            "Add",
            x + panelW - 4 - addW,
            fieldY,
            addW,
            ctl,
            autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
            WaypointsScreen.HitKind.ADD,
            "",
            mx,
            my
         );
         int scopeY = fieldY + ctl + 2;
         String scopeLabel = "unknown".equals(this.scope)
            ? "Scope: unknown (join a server or world)"
            : "Scope: " + AuditWaypointScope.display(this.scope);
         this.drawTrimmed(graphics, scopeLabel, fieldX, this.centeredTextY(scopeY, 12), labelTextColor(), panelW - 8);
         if (this.colorEditName != null) {
            this.renderColorPanel(graphics, fieldX, scopeY + 12 + 2, panelW - 8, delta, mx, my);
         }

         int listY = this.listTop();
         int listH = this.listHeight();
         autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.listLayout(listY, listH);
         this.drawBox(graphics, fieldX, listY, panelW - 8, listH, -1442248437, THEME.set());
         listLayout.pushContentClip(graphics);

         try {
            this.renderList(graphics, listLayout, delta, mx, my);
         } finally {
            listLayout.popContentClip(graphics);
         }

         listLayout.renderScrollbar(graphics, mx, my, this.draggingScrollbar);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void renderList(GuiGraphicsExtractor graphics, autismclient.gui.ui.widgets.ScrollListLayout.ListLayout layout, float delta, int mx, int my) {
      List<AutismWaypoints.Waypoint> rows = this.waypointRows();
      this.listScroll = this.clamp(this.listScroll, 0, layout.maxScroll());
      int x = layout.x() + layout.contentInset();
      int w = layout.contentWidth();
      int rowH = this.rowHeight();
      if (rows.isEmpty()) {
         this.drawTrimmed(graphics, "No waypoints for this scope.", x + 4, this.centeredTextY(layout.y() + layout.contentInset(), rowH), labelTextColor(), w - 8);
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
                  !swatchOver && !waypoint.name().equals(this.colorEditName) ? THEME.set() : ThemePaletteManager.recolor(-50373, ThemePaletteManager.ColorRole.OUTLINE)
               );
               int nameX = swatchX + 18 + 4;
               ThemedTextInputBox nameField = this.renameInputs.get(waypoint.name());
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
               this.drawTrimmed(graphics, waypoint.x() + " " + waypoint.y() + " " + waypoint.z(), coordsX, this.centeredTextY(rowY, rowH), labelTextColor(), delX - 4 - coordsX);
               autismclient.gui.ui.widgets.ListRowRenderer.drawDeleteButton(graphics, delX, delY, del, del, over);
               this.init_List.add(new WaypointsScreen.HitRegion(WaypointsScreen.HitKind.ROW, x, rowY, w, rowH, waypoint.name()));
               this.init_List.add(new WaypointsScreen.HitRegion(WaypointsScreen.HitKind.SWATCH, swatchX, swatchY, 18, 10, waypoint.name()));
               this.init_List.add(new WaypointsScreen.HitRegion(WaypointsScreen.HitKind.REMOVE, delX, delY, del, del, waypoint.name()));
            }
         );
      }
   }

   private void renderColorPanel(GuiGraphicsExtractor graphics, int x, int y, int w, float delta, int mx, int my) {
      int ph = this.colorPanelHeight();
      this.colorPanel = autismclient.gui.f.NetHelper.init(x, y, w, ph);
      this.drawBox(graphics, x, y, w, ph, -301266162, THEME.set());
      int current = AutismWaypoints.getInstance().colorOf(this.scope, this.colorEditName, defaultWaypointColor());
      int swatchY = y + 4;
      int swatchX = x + 4;

      for (int i = 0; i < PRESET_COLORS.length; i++) {
         int px = swatchX + i * 22;
         if (px + 18 > x + w - 4) {
            break;
         }

         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(px, swatchY, 18, 10), PRESET_COLORS[i]);
         boolean over = mx >= px && mx < px + 18 && my >= swatchY && my < swatchY + 10;
         autismclient.gui.f.PacketUtil.load(
            graphics, autismclient.gui.f.NetHelper.init(px, swatchY, 18, 10), !over && PRESET_COLORS[i] != current ? THEME.set() : -1
         );
         this.init_List.add(new WaypointsScreen.HitRegion(WaypointsScreen.HitKind.PRESET, px, swatchY, 18, 10, Integer.toHexString(i)));
      }

      int ctl = this.controlHeight();
      int hexY = swatchY + 10 + 4;
      int hexX = x + 4;
      this.hexInput.setX(hexX);
      this.hexInput.setY(hexY);
      this.hexInput.setWidth(64);
      this.hexInput.setHeight(ctl);
      this.hexInput.render(graphics, mx, my, delta);
      int previewX = hexX + 64 + 4;
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(previewX, hexY, 18, ctl), current);
      autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(previewX, hexY, 18, ctl), THEME.set());
      int hintX = previewX + 18 + 8;
      this.drawTrimmed(graphics, "# hex applies live", hintX, this.centeredTextY(hexY, ctl), labelTextColor(), x + w - hintX - 4);
   }

   private void renderToneButton(
      GuiGraphicsExtractor graphics,
      String label,
      int x,
      int y,
      int w,
      int h,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      WaypointsScreen.HitKind type,
      String value,
      int mx,
      int my
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(graphics, this.font, x, y, w, h, label, variant, true, mx, my);
      this.init_List.add(new WaypointsScreen.HitRegion(type, x, y, w, h, value));
   }

   private void init_V_GuiGraphicsExtract_7(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      int headerH = this.headerHeight();
      autismclient.gui.ui.widgets.DialogFrameRenderer.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my), bounds, headerH, "Waypoints", mx >= x && mx < x + width && my >= y && my < y + headerH
      );
      autismclient.gui.f.NetHelper close = autismclient.gui.ui.widgets.DialogFrameRenderer.init(bounds, headerH);
      this.init_List.add(new WaypointsScreen.HitRegion(WaypointsScreen.HitKind.CLOSE, close.read(), close.write(), close.update(), close.sync(), ""));
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      ThemedTextInputBox focused = this.focusedName == null ? null : this.renameInputs.get(this.focusedName);
      if (focused != null && !isInsideField(focused, mx, my)) {
         this.commitRename();
      }

      autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.scrollbarMetrics();
      if (metrics.isScrollable() && metrics.containsTrack(mx, my)) {
         this.draggingScrollbar = true;
         this.scrollbarGrabOffset = metrics.containsThumb(mx, my) ? my - metrics.thumbY() : metrics.thumbHeight() / 2;
         this.listScroll = this.snapToRow(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, my, this.scrollbarGrabOffset));
         return true;
      }

      this.ensureNameInput();
      if (this.colorEditName != null && this.hexInput.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
         this.nameInput.setFocused(false);
         return true;
      }

      if (this.nameInput.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
         this.hexInput.setFocused(false);
         return true;
      }

      for (String name : this.load_List) {
         ThemedTextInputBox rowField = this.renameInputs.get(name);
         if (rowField != null && rowField.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
            this.beginRename(name);
            return true;
         }
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         WaypointsScreen.HitRegion hit = this.init_List.get(i);
         if (hit.contains(mx, my)
            && (
               hit.type != WaypointsScreen.HitKind.REMOVE
                     && hit.type != WaypointsScreen.HitKind.SWATCH
                     && hit.type != WaypointsScreen.HitKind.ROW
                  || this.isOverList(mx, my)
            )) {
            switch (hit.type) {
               case ADD:
                  this.addCurrentWaypoint();
                  break;
               case SWATCH:
                  this.openColorEditor(hit.value);
                  break;
               case PRESET:
                  this.applyPresetColor(hit.value);
                  break;
               case REMOVE:
                  this.init_V_Str_10(hit.value);
                  break;
               case ROW:
                  this.copyCoordinates(hit.value);
                  break;
               case CLOSE:
                  this.onClose();
            }

            return true;
         }
      }

      if (this.colorEditName != null && (this.colorPanel == null || !this.colorPanel.init_Z(mx, my))) {
         this.closeColorEditor();
      }

      this.nameInput.setFocused(false);
      this.hexInput.setFocused(false);
      return true;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      if (!this.isOverList(mx, my)) {
         return true;
      }

      this.listScroll = this.listScroll + (scrollY < 0.0 ? this.rowPitch() : -this.rowPitch());
      this.listScroll = this.clamp(this.listScroll, 0, this.scrollbarMetrics().maxScroll());
      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      this.draggingScrollbar = false;
      ThemedTextInputBox focused = this.focusedName == null ? null : this.renameInputs.get(this.focusedName);
      if (focused != null && focused.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event))) {
         return true;
      } else if (this.nameInput != null && this.nameInput.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event))) {
         return true;
      } else {
         return this.hexInput != null && this.hexInput.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event)) ? true : true;
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      ThemedTextInputBox focused = this.focusedName == null ? null : this.renameInputs.get(this.focusedName);
      if (focused != null && focused.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
         return true;
      }

      if (this.nameInput != null && this.nameInput.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
         return true;
      }

      if (this.hexInput != null && this.hexInput.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
         return true;
      }

      if (!this.draggingScrollbar) {
         return true;
      }

      this.listScroll = this.snapToRow(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(this.scrollbarMetrics(), my, this.scrollbarGrabOffset));
      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      this.ensureNameInput();
      if (Compat263.keyEventGlfwKey(input) == 256) {
         if (this.focusedName != null) {
            this.cancelRename();
         } else if (this.colorEditName != null) {
            this.closeColorEditor();
         } else {
            this.onClose();
         }

         return true;
      } else if (Compat263.keyEventGlfwKey(input) != 257 && Compat263.keyEventGlfwKey(input) != 335) {
         ThemedTextInputBox focused = this.focusedName == null ? null : this.renameInputs.get(this.focusedName);
         if (focused != null && focused.keyPressed(input)) {
            return true;
         } else if (this.nameInput.keyPressed(input)) {
            return true;
         } else {
            return this.colorEditName != null && this.hexInput.keyPressed(input) ? true : true;
         }
      } else {
         if (this.focusedName != null) {
            this.commitRename();
         } else if (this.nameInput.isFocused()) {
            this.addCurrentWaypoint();
         }

         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      this.ensureNameInput();
      ThemedTextInputBox focused = this.focusedName == null ? null : this.renameInputs.get(this.focusedName);
      if (focused != null && focused.charTyped(input)) {
         return true;
      } else if (this.nameInput.charTyped(input)) {
         return true;
      } else {
         return this.colorEditName != null && this.hexInput.charTyped(input) ? true : true;
      }
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
      }
   }

   private void addCurrentWaypoint() {
      if (AuditWaypointScope.canAdd(Minecraft.getInstance(), this.scope)) {
         if (this.minecraft != null && this.minecraft.player != null) {
            AutismWaypoints store = AutismWaypoints.getInstance();
            String name = this.nameInputText().trim();
            if (name.isEmpty()) {
               name = store.nextName(this.scope, "Waypoint");
            }

            int x = (int)Math.floor(this.minecraft.player.getX());
            int y = (int)Math.floor(this.minecraft.player.getY());
            int z = (int)Math.floor(this.minecraft.player.getZ());
            store.init(this.scope, new AutismWaypoints.Waypoint(name, x, y, z, defaultWaypointColor(), System.currentTimeMillis()));
            this.setNameInput("");
            this.clampScroll();
         } else {
            bi.read("Join a world to add a waypoint here.");
         }
      }
   }

   private void init_V_Str_10(String name) {
      AutismWaypoints.getInstance().remove(this.scope, name);
      if (name != null && name.equals(this.colorEditName)) {
         this.closeColorEditor();
      }

      if (name != null && name.equals(this.focusedName)) {
         this.focusedName = null;
      }

      this.clampScroll();
   }

   private void copyCoordinates(String name) {
      for (AutismWaypoints.Waypoint waypoint : this.waypointRows()) {
         if (waypoint.name().equals(name)) {
            String coords = waypoint.x() + " " + waypoint.y() + " " + waypoint.z();
            Minecraft.getInstance().keyboardHandler.setClipboard(coords);
            bi.init("Copied coordinates: " + coords + ".");
            return;
         }
      }
   }

   private void syncNameInputs() {
      List<AutismWaypoints.Waypoint> rows = this.waypointRows();
      Map<String, ThemedTextInputBox> alive = new LinkedHashMap<>();

      for (AutismWaypoints.Waypoint waypoint : rows) {
         ThemedTextInputBox field = this.renameInputs.get(waypoint.name());
         if (field == null) {
            field = new ThemedTextInputBox(this.minecraft, this.font, 0, 0, 1, this.rowHeight() - 2, false);
            field.setMaxLength(64);
            field.setValue(waypoint.name());
         } else if (!waypoint.name().equals(this.focusedName) && !waypoint.name().equals(field.getValue())) {
            field.setValue(waypoint.name());
         }

         alive.put(waypoint.name(), field);
      }

      if (this.focusedName != null && !alive.containsKey(this.focusedName)) {
         this.focusedName = null;
      }

      this.renameInputs.clear();
      this.renameInputs.putAll(alive);
   }

   private void beginRename(String name) {
      if (this.focusedName != null && !this.focusedName.equals(name)) {
         this.commitRename();
      }

      this.focusedName = name;
      ThemedTextInputBox field = this.renameInputs.get(name);
      if (field != null) {
         field.setFocused(true);
         field.setSelectionEnd(field.getValue().length());
      }

      if (this.nameInput != null) {
         this.nameInput.setFocused(false);
      }

      if (this.hexInput != null) {
         this.hexInput.setFocused(false);
      }
   }

   private void commitRename() {
      if (this.focusedName != null) {
         String original = this.focusedName;
         ThemedTextInputBox field = this.renameInputs.get(original);
         this.focusedName = null;
         if (field != null) {
            field.setFocused(false);
            String renamed = field.getValue().trim();
            if (renamed.equals(original)) {
               field.setValue(original);
            } else if (AutismWaypoints.getInstance().init(this.scope, original, renamed)) {
               this.renameInputs.remove(original);
               this.renameInputs.put(renamed, field);
               field.setValue(renamed);
               if (original.equals(this.colorEditName)) {
                  this.colorEditName = renamed;
               }
            } else {
               field.setValue(original);
            }
         }
      }
   }

   private void cancelRename() {
      if (this.focusedName != null) {
         ThemedTextInputBox field = this.renameInputs.get(this.focusedName);
         if (field != null) {
            field.setValue(this.focusedName);
            field.setFocused(false);
         }

         this.focusedName = null;
      }
   }

   private static boolean isInsideField(ThemedTextInputBox field, int mx, int my) {
      return mx >= field.getX() && mx < field.getX() + field.getWidth() && my >= field.getY() && my < field.getY() + field.getHeight();
   }

   private void openColorEditor(String name) {
      this.colorEditName = name;
      this.load_Z = true;

      try {
         this.hexInput
            .setValue(String.format(Locale.ROOT, "%06X", AutismWaypoints.getInstance().colorOf(this.scope, name, defaultWaypointColor()) & 16777215));
      } finally {
         this.load_Z = false;
      }

      this.hexInput.setFocused(true);
      this.nameInput.setFocused(false);
   }

   private void closeColorEditor() {
      this.colorEditName = null;
      if (this.hexInput != null) {
         this.hexInput.setFocused(false);
      }
   }

   private void applyPresetColor(String hexIndex) {
      if (this.colorEditName != null) {
         int index;
         try {
            index = Integer.parseInt(hexIndex, 16);
         } catch (NumberFormatException e) {
            return;
         }

         if (index >= 0 && index < PRESET_COLORS.length) {
            AutismWaypoints.getInstance().setColor(this.scope, this.colorEditName, PRESET_COLORS[index]);
            this.load_Z = true;

            try {
               this.hexInput.setValue(String.format(Locale.ROOT, "%06X", PRESET_COLORS[index] & 16777215));
            } finally {
               this.load_Z = false;
            }
         }
      }
   }

   private void onHexChanged(String raw) {
      if (!this.load_Z && this.colorEditName != null) {
         Integer color = parseHexColor(raw);
         if (color != null) {
            AutismWaypoints.getInstance().setColor(this.scope, this.colorEditName, color);
         }
      }
   }

   private static Integer parseHexColor(String raw) {
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

   private static int defaultWaypointColor() {
      return al.init_Module_Str("waypoints") instanceof WaypointsModule waypoints ? waypoints.defaultColor() : -12855297;
   }

   private List<AutismWaypoints.Waypoint> waypointRows() {
      AutismWaypoints store = AutismWaypoints.getInstance();
      long revision = store.revision();
      if (revision != this.revision) {
         this.revision = revision;
         this.cachedWaypoints = store.list(this.scope);
      }

      return this.cachedWaypoints;
   }

   private void ensureNameInput() {
      if (this.nameInput == null) {
         this.nameInput = new ThemedTextInputBox(this.minecraft, this.font, 0, 0, 1, this.controlHeight(), false);
         this.nameInput.setMaxLength(64);
         this.nameInput.setPlaceholder(Component.literal("Waypoint name"));
      }

      if (this.hexInput == null) {
         this.hexInput = new ThemedTextInputBox(this.minecraft, this.font, 0, 0, 1, this.controlHeight(), false);
         this.hexInput.setMaxLength(7);
         this.hexInput.setPlaceholder(Component.literal("RRGGBB"));
         this.hexInput.setFilter(text -> {
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
         this.hexInput.setChangedListener(this::onHexChanged);
      }
   }

   private void layoutNameInput() {
      if (this.nameInput != null) {
         this.nameInput.setX(this.panelX() + 4);
         this.nameInput.setY(this.nameFieldY());
         this.nameInput.setWidth(Math.max(40, this.panelWidth() - this.addButtonWidth() - 8 - 2));
         this.nameInput.setHeight(this.controlHeight());
      }
   }

   private int addButtonWidth() {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(autismclient.gui.f.b.k.init(this.font, "Add", THEME.init_Identifier_l(l.read), 0));
   }

   private String nameInputText() {
      return this.nameInput == null ? "" : this.nameInput.getValue();
   }

   private void setNameInput(String value) {
      this.ensureNameInput();
      this.nameInput.setValue(value == null ? "" : value);
      this.nameInput.setSelectionEnd(this.nameInput.getValue().length());
   }

   private int headerHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int controlHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int rowPitch() {
      return this.rowHeight() + 2;
   }

   private int colorPanelHeight() {
      return 18 + this.controlHeight() + 4;
   }

   private int nameFieldY() {
      return this.panelY() + this.headerHeight() + 4;
   }

   private int listTopOffset() {
      int y = this.headerHeight() + 4 + this.controlHeight() + 2 + 12 + 2;
      return this.colorEditName != null ? y + this.colorPanelHeight() + 2 : y;
   }

   private int listTop() {
      return this.panelY() + this.listTopOffset();
   }

   private int listHeight() {
      return Math.max(1, this.panelY() + this.panelHeight() - 4 - this.listTop());
   }

   private autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout(int listY, int listH) {
      return autismclient.gui.ui.widgets.ScrollListLayout.compute(
         this.panelX() + 4, listY, this.panelWidth() - 8, listH, this.waypointRows().size(), this.rowHeight(), this.rowPitch(), this.listScroll, 4, 8, 2, 6, false
      );
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics() {
      return this.listLayout(this.listTop(), this.listHeight()).scrollbar();
   }

   private boolean isOverList(int mx, int my) {
      autismclient.gui.ui.widgets.ScrollListLayout.ListLayout listLayout = this.listLayout(this.listTop(), this.listHeight());
      return mx >= listLayout.x() + listLayout.contentInset()
         && mx < listLayout.x() + listLayout.contentInset() + listLayout.contentWidth()
         && my >= listLayout.y() + listLayout.contentInset()
         && my < listLayout.y() + listLayout.contentInset() + listLayout.viewHeight();
   }

   private void clampScroll() {
      this.listScroll = this.clamp(this.listScroll, 0, this.scrollbarMetrics().maxScroll());
   }

   private int snapToRow(int offset) {
      return Math.max(0, offset / this.rowPitch()) * this.rowPitch();
   }

   private int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private int panelX() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualWidth(), this.panelWidth(), 6);
   }

   private int panelY() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualHeight(), this.panelHeight(), 6);
   }

   private int panelWidth() {
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualWidth(), 6, 340);
   }

   private int panelHeight() {
      int cap = Math.max(3, this.virtualHeight() * 65 / 100 / this.rowPitch());
      int shown = Math.max(3, Math.min(cap, this.waypointRows().size()));
      int content = this.listTopOffset() + shown * this.rowPitch() + 4 + 4;
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualHeight(), 6, content);
   }

   private void drawBox(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int fill, int border) {
      autismclient.gui.f.PacketUtil.init(
         graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), fill, ThemePaletteManager.recolor(-1721357268, ThemePaletteManager.ColorRole.OUTLINE)
      );
   }

   private int centeredTextY(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).centerTextY(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private void drawTrimmed(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      String display = autismclient.gui.f.b.k.init(
         this.font, text == null ? "" : text, Math.max(0, maxWidth), THEME.init_Identifier_l(l.read), color
      );
      autismclient.gui.f.b.k.init(graphics, this.font, display, THEME.init_Identifier_l(l.read), color, x, y, false);
   }

   private record HitRegion(WaypointsScreen.HitKind type, int x, int load, int read, int write, String value) {
      boolean contains(int mx, int my) {
         return mx >= this.x && mx < this.x + this.read && my >= this.load && my < this.load + this.write;
      }
   }

   private enum HitKind {
      ADD,
      SWATCH,
      PRESET,
      REMOVE,
      ROW,
      CLOSE;
   }
}
