package autismclient.gui.screens;

import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.modules.Module;
import autismclient.util.AutismHudManager;
import autismclient.util.cA;
import autismclient.util.ThemePaletteManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class AutismHudEditorScreen extends Screen {
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = -791321;
   private static final int load_I = -4743522;
   private static final int read_I = -10682470;
   private static final int write = -50373;
   private static final String[] CATEGORY_TITLES = new String[]{"World", "Player", "Client"};
   private static final String[][] CATEGORY_ELEMENTS = new String[][]{
      {"coordinates", "nether_coords", "compass", "looking_at", "biome", "weather", "world_time", "real_time", "server", "server_ip", "server_brand"},
      {"speed", "rotation", "game_mode", "armor", "inventory", "durability", "item_counter", "potion_timers", "breaking_progress"},
      {"active_modules", "watermark", "fps", "ping", "tps", "cps", "keystrokes", "anti_vanish", "memory", "fps_graph", "bed_danger", "gif"}
   };
   private static final int update = 10;
   private static final int sync = 6;
   private static final int flush = 0;
   private final Screen parent;
   private String selectedId;
   private String hit;
   private int dragOffsetX;
   private int dragOffsetY;
   private boolean dragSnapped;
   private boolean pickerOpen;
   private int pickerX;
   private int pickerY;
   private Integer guideX;
   private Integer guideY;
   private int resizeCorner = -1;
   private int resizeStartWidth;
   private int resizeStartHeight;
   private int resizeStartMouseX;
   private int resizeStartMouseY;
   private int resizeOriginX;
   private int resizeOriginY;
   private int[] cachedHelpPos;
   private boolean helpPosCached;
   private int cachedScreenW;
   private int cachedScreenH;
   private int cachedHelpW;
   private int cachedHelpH;
   private int cachedRevision;

   private static int textColor() {
      return ThemePaletteManager.recolor(-4743522, ThemePaletteManager.ColorRole.TEXT);
   }

   private static int accentColor() {
      return ThemePaletteManager.recolor(-50373, ThemePaletteManager.ColorRole.ACCENT);
   }

   public AutismHudEditorScreen(Screen parent) {
      super(Component.literal("Autism HUD Editor"));
      this.parent = parent;
      AutismHudManager.ensureInitialized();
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
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), 1426063360);
         if (this.gridEnabled()) {
            this.drawGrid(graphics, sw, sh);
         }

         this.drawGuides(graphics, sw, sh);
         AutismHudManager.render(graphics, this.font, true, this.selectedId, mx, my);
         if ("gif".equals(this.selectedId) && AutismHudManager.getState(this.selectedId).enabled) {
            this.drawGifHandles(graphics);
         }

         this.drawHelpBox(graphics, sw, sh, mx, my);
         if (this.pickerOpen) {
            this.drawPicker(graphics, mx, my);
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void drawHelpBox(GuiGraphicsExtractor graphics, int sw, int sh, int mx, int my) {
      String help = "Drag elements, right click edits/adds, Delete disables, arrows nudge.";
      int titleW = k.init(this.font, "Autism HUD Editor", THEME.init_Identifier_l(l.read), -791321) + 8;
      int helpW = k.init(this.font, help, THEME.init_Identifier_l(l.read), textColor()) + 8;
      int w = Math.min(sw - 12, Math.min(430, Math.max(titleW, helpW)));
      int headerH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
      int lineH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
      int h = headerH + lineH + 2;
      int[] pos = this.helpBoxPosition(sw, sh, w, h);
      if (pos != null) {
         int x = pos[0];
         int y = pos[1];
         autismclient.gui.ui.widgets.WindowFrameRenderer.init(
            autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my),
            autismclient.gui.f.NetHelper.init(x, y, w, h),
            "Autism HUD Editor",
            false,
            false,
            false,
            this.inside(mx, my, x, y, w, headerH),
            true,
            4,
            4,
            headerH
         );
         this.drawTrimmedText(graphics, help, x + 4, this.centeredTextY(y + headerH, lineH), textColor(), w - 8);
      }
   }

   private int[] helpBoxPosition(int sw, int sh, int w, int h) {
      int rev = AutismHudManager.revision();
      if (this.helpPosCached && sw == this.cachedScreenW && sh == this.cachedScreenH && w == this.cachedHelpW && h == this.cachedHelpH && rev == this.cachedRevision) {
         return this.cachedHelpPos;
      }

      int pad = 6;
      int[][] candidates = new int[][]{
         {6, 6}, {Math.max(pad, sw - w - pad), pad}, {pad, Math.max(pad, sh - h - pad)}, {Math.max(pad, sw - w - pad), Math.max(pad, sh - h - pad)}
      };
      int[] pos = null;

      for (int[] candidate : candidates) {
         if (!this.overlapsElement(candidate[0], candidate[1], w, h)) {
            pos = candidate;
            break;
         }
      }

      this.cachedHelpPos = pos;
      this.helpPosCached = true;
      this.cachedScreenW = sw;
      this.cachedScreenH = sh;
      this.cachedHelpW = w;
      this.cachedHelpH = h;
      this.cachedRevision = rev;
      return pos;
   }

   private boolean overlapsElement(int x, int y, int w, int h) {
      for (String id : AutismHudManager.elementIds()) {
         if (AutismHudManager.getState(id).enabled) {
            AutismHudManager.HudElementBounds bounds = AutismHudManager.bounds(id, this.font);
            if (this.rectsOverlap(x, y, w, h, bounds.read(), bounds.write(), bounds.update(), bounds.sync())) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean rectsOverlap(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
      return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
   }

   private void drawGrid(GuiGraphicsExtractor graphics, int sw, int sh) {
      int step = this.gridStep();

      for (int x = 0; x < sw; x += step) {
         boolean major = x / step % 5 == 0;
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, 0, 1, sh), major ? 605098260 : 336662804);
      }

      for (int y = 0; y < sh; y += step) {
         boolean major = y / step % 5 == 0;
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, y, sw, 1), major ? 605098260 : 336662804);
      }

      autismclient.gui.f.PacketUtil.init(
         graphics, autismclient.gui.f.NetHelper.init(sw / 2, 0, 1, sh), ThemePaletteManager.recolor(1157577531, ThemePaletteManager.ColorRole.ACCENT)
      );
      autismclient.gui.f.PacketUtil.init(
         graphics, autismclient.gui.f.NetHelper.init(0, sh / 2, sw, 1), ThemePaletteManager.recolor(1157577531, ThemePaletteManager.ColorRole.ACCENT)
      );
   }

   private AutismHudEditorScreen.PickerLayout pickerLayout() {
      int pad = 4;
      int gap = 4;
      int titleH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
      int catH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12) + 2;
      int rowH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12) + 2;
      int longest = 0;

      for (String[] cat : CATEGORY_ELEMENTS) {
         for (String id : cat) {
            longest = Math.max(longest, k.init(this.font, AutismHudManager.displayName(id), THEME.init_Identifier_l(l.read), -791321));
         }
      }

      int sw = cA.init_I();
      int sh = cA.load_I();
      int cols = CATEGORY_ELEMENTS.length;
      int maxCol = (sw - 8 - pad * 2 - (cols - 1) * gap) / cols;
      int colW = Math.max(60, Math.min(longest + 12, Math.max(60, maxCol)));
      int w = pad * 2 + cols * colW + (cols - 1) * gap;
      int maxRows = 0;

      for (String[] cat : CATEGORY_ELEMENTS) {
         maxRows = Math.max(maxRows, cat.length);
      }

      int h = titleH + catH + maxRows * rowH + pad;
      int x = this.clamp(this.pickerX, 4, Math.max(4, sw - w - 4));
      int y = this.clamp(this.pickerY, 4, Math.max(4, sh - h - 4));
      return new AutismHudEditorScreen.PickerLayout(x, y, w, h, colW, titleH, catH, rowH, gap, pad);
   }

   private void drawPicker(GuiGraphicsExtractor graphics, int mx, int my) {
      AutismHudEditorScreen.PickerLayout p = this.pickerLayout();
      autismclient.gui.ui.widgets.WindowFrameRenderer.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my),
         autismclient.gui.f.NetHelper.init(p.x, p.y, p.width, p.height),
         "Add Element",
         false,
         false,
         false,
         this.inside(mx, my, p.x, p.y, p.width, p.titleHeight),
         true,
         4,
         4,
         p.titleHeight
      );
      int underline = ThemePaletteManager.recolor(1728002875, ThemePaletteManager.ColorRole.ACCENT);

      for (int col = 0; col < CATEGORY_ELEMENTS.length; col++) {
         int colX = p.x + p.padding + col * (p.columnWidth + p.gap);
         int headerY = p.y + p.titleHeight;
         this.drawTrimmedText(graphics, CATEGORY_TITLES[col], colX + 2, this.centeredTextY(headerY, p.categoryHeight - 2), accentColor(), p.columnWidth - 4);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(colX, headerY + p.categoryHeight - 2, p.columnWidth, 1), underline);
         int cy = headerY + p.categoryHeight;

         for (String id : CATEGORY_ELEMENTS[col]) {
            boolean over = this.inside(mx, my, colX, cy, p.columnWidth, p.rowHeight - 2);
            boolean enabled = AutismHudManager.getState(id).enabled;
            autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(graphics, colX, cy, p.columnWidth, p.rowHeight - 2, over ? 1714756383 : 856888344);
            if (enabled) {
               autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(graphics, colX + 1, cy + 1, 3, p.rowHeight - 4, -10682470);
            }

            this.drawTrimmedText(graphics, AutismHudManager.displayName(id), colX + 7, this.centeredTextY(cy, p.rowHeight - 2), enabled ? -791321 : textColor(), p.columnWidth - 9);
            cy += p.rowHeight;
         }
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.pickerOpen) {
         String picked = this.pickerElementAt(mx, my);
         if (picked != null) {
            AutismHudManager.setEnabled(picked, true);
            int[] snapped = this.snapPosition(picked, mx, my);
            AutismHudManager.setPosition(picked, snapped[0], snapped[1], cA.init_I(), cA.load_I());
            this.selectedId = picked;
            this.pickerOpen = false;
            return true;
         } else {
            this.pickerOpen = false;
            return true;
         }
      } else {
         String hit = AutismHudManager.elementAt(this.font, mx, my);
         if (Compat263.mouseEventGlfwButton(event) == 1) {
            if (hit != null) {
               this.minecraft.gui.setScreen(new HudElementSettingsScreen(this, hit));
            } else {
               this.pickerX = mx;
               this.pickerY = my;
               this.pickerOpen = true;
            }

            return true;
         } else {
            if (Compat263.mouseEventGlfwButton(event) == 0 && "gif".equals(this.selectedId) && AutismHudManager.getState(this.selectedId).enabled) {
               int corner = this.gifHandleAt(mx, my);
               if (corner >= 0) {
                  this.startGifResize(corner, mx, my);
                  return true;
               }
            }

            if (Compat263.mouseEventGlfwButton(event) == 0 && hit != null) {
               this.selectedId = hit;
               this.hit = hit;
               AutismHudManager.HudElementBounds bounds = AutismHudManager.bounds(hit, this.font);
               this.dragOffsetX = mx - bounds.read();
               this.dragOffsetY = my - bounds.write();
               this.dragSnapped = false;
               return true;
            } else {
               this.selectedId = null;
               return true;
            }
         }
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.hit = null;
      this.resizeCorner = -1;
      this.guideX = null;
      this.guideY = null;
      return true;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.resizeCorner >= 0) {
         this.updateGifResize(mx, my, event.hasShiftDown());
         return true;
      }

      if (this.hit == null) {
         return true;
      }

      int[] snapped = this.snapPosition(this.hit, mx - this.dragOffsetX, my - this.dragOffsetY);
      AutismHudManager.setPosition(this.hit, snapped[0], snapped[1], cA.init_I(), cA.load_I());
      this.dragSnapped = true;
      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (Compat263.keyEventGlfwKey(input) == 256) {
         this.minecraft.gui.setScreen(this.parent);
         return true;
      }

      if (this.selectedId != null && Compat263.keyEventGlfwKey(input) == 261) {
         AutismHudManager.setEnabled(this.selectedId, false);
         return true;
      }

      if (this.selectedId != null) {
         int step = (Compat263.keyEventGlfwModifiers(input) & 2) != 0 ? 10 : 1;
         AutismHudManager.HudElementBounds bounds = AutismHudManager.bounds(this.selectedId, this.font);
         int x = bounds.read();
         int y = bounds.write();
         boolean moved = true;
         switch (Compat263.keyEventGlfwKey(input)) {
            case 262:
               x += step;
               break;
            case 263:
               x -= step;
               break;
            case 264:
               y += step;
               break;
            case 265:
               y -= step;
               break;
            default:
               moved = false;
         }

         if (moved) {
            int[] snapped = this.snapPosition(this.selectedId, x, y);
            AutismHudManager.setPosition(this.selectedId, snapped[0], snapped[1], cA.init_I(), cA.load_I());
            return true;
         }
      }

      return true;
   }

   private int[] snapPosition(String id, int x, int y) {
      AutismHudManager.HudElementBounds bounds = AutismHudManager.bounds(id, this.font);
      int sw = cA.init_I();
      int sh = cA.load_I();
      int outX = x;
      int outY = y;
      int maxX = Math.max(0, sw - bounds.update());
      int maxY = Math.max(0, sh - bounds.sync());
      this.guideX = null;
      this.guideY = null;
      AutismHudEditorScreen.SnapResult snappedX = this.bestSnap(id, x, bounds.update(), sw, true);
      AutismHudEditorScreen.SnapResult snappedY = this.bestSnap(id, y, bounds.sync(), sh, false);
      if (snappedX.snapped()) {
         outX = snappedX.candidatePosition();
         this.guideX = snappedX.guide();
      }

      if (snappedY.snapped()) {
         outY = snappedY.candidatePosition();
         this.guideY = snappedY.guide();
      }

      outX = this.clamp(outX, 0, maxX);
      outY = this.clamp(outY, 0, maxY);
      return new int[]{outX, outY};
   }

   private AutismHudEditorScreen.SnapResult bestSnap(String id, int value, int size, int screenSize, boolean horizontal) {
      int max = Math.max(0, screenSize - size);
      AutismHudEditorScreen.SnapResult best = AutismHudEditorScreen.SnapResult.none();
      best = this.closerSnap(best, value, 0, 0);
      best = this.closerSnap(best, value, max, screenSize);
      best = this.closerSnap(best, value, (screenSize - size) / 2, screenSize / 2);

      for (String other : AutismHudManager.elementIds()) {
         if (!other.equals(id) && AutismHudManager.getState(other).enabled) {
            AutismHudManager.HudElementBounds ob = AutismHudManager.bounds(other, this.font);
            int otherStart = horizontal ? ob.read() : ob.write();
            int otherSize = horizontal ? ob.update() : ob.sync();
            int otherEnd = otherStart + otherSize;
            int otherCenter = otherStart + otherSize / 2;
            best = this.closerSnap(best, value, otherStart, otherStart);
            best = this.closerSnap(best, value, otherEnd - size, otherEnd);
            best = this.closerSnap(best, value, otherCenter - size / 2, otherCenter);
            best = this.closerSnap(best, value, otherStart - size - 0, otherStart);
            best = this.closerSnap(best, value, otherEnd + 0, otherEnd);
         }
      }

      return best;
   }

   private AutismHudEditorScreen.SnapResult closerSnap(AutismHudEditorScreen.SnapResult current, int value, int candidatePosition, int guide) {
      int distance = Math.abs(value - candidatePosition);
      if (distance > 6) {
         return current;
      } else {
         return current.snapped() && distance >= current.distance() ? current : new AutismHudEditorScreen.SnapResult(candidatePosition, guide, distance, true);
      }
   }

   private int gridStep() {
      return 10;
   }

   private String pickerElementAt(int mx, int my) {
      AutismHudEditorScreen.PickerLayout p = this.pickerLayout();

      for (int col = 0; col < CATEGORY_ELEMENTS.length; col++) {
         int colX = p.x + p.padding + col * (p.columnWidth + p.gap);
         int cy = p.y + p.titleHeight + p.categoryHeight;

         for (String id : CATEGORY_ELEMENTS[col]) {
            if (this.inside(mx, my, colX, cy, p.columnWidth, p.rowHeight - 2)) {
               return id;
            }

            cy += p.rowHeight;
         }
      }

      return null;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      return true;
   }

   private boolean gridEnabled() {
      Module hud = autismclient.modules.al.init_Module_Str("hud");
      return hud == null || Boolean.parseBoolean(hud.value("editor-grid"));
   }

   private int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private void drawGuides(GuiGraphicsExtractor graphics, int sw, int sh) {
      if (this.guideX != null) {
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(this.guideX, 0, 1, sh), ThemePaletteManager.recolor(-1426113733, ThemePaletteManager.ColorRole.ACCENT)
         );
      }

      if (this.guideY != null) {
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(0, this.guideY, sw, 1), ThemePaletteManager.recolor(-1426113733, ThemePaletteManager.ColorRole.ACCENT)
         );
      }
   }

   private void drawGifHandles(GuiGraphicsExtractor graphics) {
      for (int[] handle : this.gifHandles()) {
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(graphics, handle[0], handle[1], 3.0F, accentColor());
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(graphics, handle[0], handle[1], 2.0F, -791321);
      }
   }

   private int[][] gifHandles() {
      AutismHudManager.HudElementBounds b = AutismHudManager.bounds("gif", this.font);
      return new int[][]{
         {b.read(), b.write()}, {b.read() + b.update(), b.write()}, {b.read() + b.update(), b.write() + b.sync()}, {b.read(), b.write() + b.sync()}
      };
   }

   private int gifHandleAt(int mx, int my) {
      int[][] handles = this.gifHandles();

      for (int i = 0; i < handles.length; i++) {
         if (Math.abs(mx - handles[i][0]) <= 4 && Math.abs(my - handles[i][1]) <= 4) {
            return i;
         }
      }

      return -1;
   }

   private void startGifResize(int corner, int mx, int my) {
      String id = "gif";
      this.resizeCorner = corner;
      AutismHudManager.HudElementBounds b = AutismHudManager.bounds(id, this.font);
      int pad = this.clamp(AutismHudManager.intSetting(id, "padding", 1), 0, 16);
      this.resizeOriginX = b.read() + pad;
      this.resizeOriginY = b.write() + pad;
      this.resizeStartWidth = this.clamp(AutismHudManager.intSetting(id, "gif-width", 120), 16, 512);
      this.resizeStartHeight = this.clamp(AutismHudManager.intSetting(id, "gif-height", 120), 16, 512);
      this.resizeStartMouseX = mx;
      this.resizeStartMouseY = my;
   }

   private void updateGifResize(int mx, int my, boolean shiftDown) {
      String id = "gif";
      if (!id.equals(this.selectedId)) {
         this.resizeCorner = -1;
      } else {
         int signX = this.resizeCorner != 0 && this.resizeCorner != 3 ? 1 : -1;
         int signY = this.resizeCorner != 0 && this.resizeCorner != 1 ? 1 : -1;
         int w = this.resizeStartWidth + (mx - this.resizeStartMouseX) * signX;
         int h = this.resizeStartHeight + (my - this.resizeStartMouseY) * signY;
         boolean keepRatio = AutismHudManager.boolSetting(id, "gif-keep-aspect") != shiftDown;
         if (keepRatio) {
            double scale = Math.max((double)w / this.resizeStartWidth, (double)h / this.resizeStartHeight);
            w = Math.max(1, (int)Math.round(this.resizeStartWidth * scale));
            h = Math.max(1, (int)Math.round(this.resizeStartHeight * scale));
         }

         w = this.clamp(w, 16, 512);
         h = this.clamp(h, 16, 512);
         int pad = this.clamp(AutismHudManager.intSetting(id, "padding", 1), 0, 16);
         int anchorX = signX < 0 ? this.resizeOriginX + this.resizeStartWidth : this.resizeOriginX;
         int anchorY = signY < 0 ? this.resizeOriginY + this.resizeStartHeight : this.resizeOriginY;
         int sw = cA.init_I();
         int sh = cA.load_I();
         int maxW = Math.max(16, signX < 0 ? anchorX - pad : sw - anchorX - pad);
         int maxH = Math.max(16, signY < 0 ? anchorY - pad : sh - anchorY - pad);
         w = Math.min(w, maxW);
         h = Math.min(h, maxH);
         if (keepRatio) {
            int hFromW = Math.max(16, (int)Math.round((double)(w * this.resizeStartHeight) / this.resizeStartWidth));
            if (hFromW <= maxH) {
               h = hFromW;
            } else {
               w = this.clamp((int)Math.round((double)(h * this.resizeStartWidth) / this.resizeStartHeight), 16, maxW);
            }
         }

         AutismHudManager.setSetting(id, "gif-width", Integer.toString(w));
         AutismHudManager.setSetting(id, "gif-height", Integer.toString(h));
         AutismHudManager.setSetting(id, "gif-natural", "false");
         int contentX = signX < 0 ? anchorX - w : anchorX;
         int contentY = signY < 0 ? anchorY - h : anchorY;
         AutismHudManager.setPosition(id, contentX - pad, contentY - pad, sw, sh);
      }
   }

   private int centeredTextY(int boxY, int boxH) {
      return UiLayoutMath.centerTextY(boxY, boxH, THEME.init_I_l(l.read), THEME.flush());
   }

   private boolean inside(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   private void drawTrimmedText(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxW) {
      String trimmed = k.init(this.font, text, maxW, THEME.init_Identifier_l(l.read), color);
      k.init(graphics, this.font, trimmed, THEME.init_Identifier_l(l.read), color, x, y, false);
   }

   private record PickerLayout(int x, int y, int width, int height, int columnWidth, int titleHeight, int categoryHeight, int rowHeight, int gap, int padding) {
   }

   private record SnapResult(int candidatePosition, int guide, int distance, boolean snapped) {
      static AutismHudEditorScreen.SnapResult none() {
         return new AutismHudEditorScreen.SnapResult(0, 0, 2147483647, false);
      }
   }
}
