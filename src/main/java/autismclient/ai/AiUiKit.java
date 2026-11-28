package autismclient.ai;

import autismclient.gui.ui.UiClipStack;
import autismclient.gui.f.PacketUtil;
import autismclient.gui.ui.widgets.ScrollbarMetricsHelper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

final class AiUiKit {
   static final int STATUS_OK_COLOR = -10497050;
   static final int STATUS_WARN_COLOR = -14249;
   static final int WHEEL_SCROLL_STEP_PX = 24;
   final autismclient.gui.f.b.CharEncoder theme = new autismclient.gui.f.b.CharEncoder();
   private final List<AiUiKit.HitRegion> hitRegions = new ArrayList<>();
   private final List<AiUiKit.ScrollRegion> scrollRegions = new ArrayList<>();
   private final List<AiUiKit.ScrollPane> activeScrollbars = new ArrayList<>();
   private final ArrayDeque<autismclient.gui.f.NetHelper> clipStack = new ArrayDeque<>();
   private AiUiKit.ScrollPane draggingScroll;
   GuiGraphicsExtractor graphics;
   autismclient.gui.f.SyncManager renderContext;
   autismclient.gui.ui.UiColorScheme colors;
   autismclient.gui.ui.layout.LayoutContext layoutContext;
   Font font;
   int mouseX;
   int mouseY;
   int rawMouseX;
   int rawMouseY;

   void beginFrame(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean pointerBlocked, float delta) {
      this.graphics = graphics;
      this.font = Minecraft.getInstance().font;
      this.rawMouseX = mouseX;
      this.rawMouseY = mouseY;
      this.mouseX = pointerBlocked ? -10000 : mouseX;
      this.mouseY = pointerBlocked ? -10000 : mouseY;
      this.renderContext = autismclient.gui.f.UiHelper.init(graphics, this.font, this.mouseX, this.mouseY);
      this.colors = this.renderContext.init_TableIndex().colors();
      this.layoutContext = new autismclient.gui.ui.layout.LayoutContext(
         graphics, this.font, autismclient.gui.ui.layout.LayoutViewport.create(2.0F), this.theme, this.mouseX, this.mouseY, delta
      );
      this.hitRegions.clear();
      this.scrollRegions.clear();
      this.activeScrollbars.clear();
      this.clipStack.clear();
   }

   boolean isInitialized() {
      return this.layoutContext != null;
   }

   static int toolbarHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   static int inputHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   static int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   static int lineHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(10);
   }

   int measureTextWidth(String text) {
      return this.renderContext.init_PoolEntry().measureWidth(text);
   }

   int measureButtonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(this.measureTextWidth(label));
   }

   static autismclient.gui.f.NetHelper intersectBounds(autismclient.gui.f.NetHelper a, autismclient.gui.f.NetHelper b) {
      int x = Math.max(a.read(), b.read());
      int y = Math.max(a.write(), b.write());
      int r = Math.min(a.init(), b.init());
      int btm = Math.min(a.load(), b.load());
      return autismclient.gui.f.NetHelper.init(x, y, Math.max(0, r - x), Math.max(0, btm - y));
   }

   void pushClip(autismclient.gui.f.NetHelper bounds) {
      autismclient.gui.f.NetHelper clip = this.clipStack.isEmpty() ? bounds : intersectBounds(this.clipStack.peek(), bounds);
      this.clipStack.push(clip);
      UiClipStack.getInstance().push(this.graphics, clip);
   }

   void popClip() {
      this.clipStack.pop();
      UiClipStack.getInstance().pop(this.graphics);
   }

   private autismclient.gui.f.NetHelper clipBounds(autismclient.gui.f.NetHelper bounds) {
      return this.clipStack.isEmpty() ? bounds : intersectBounds(this.clipStack.peek(), bounds);
   }

   boolean isHovered(autismclient.gui.f.NetHelper bounds) {
      autismclient.gui.f.NetHelper c = this.clipBounds(bounds);
      return c.update() > 0 && c.sync() > 0 && c.init_Z(this.mouseX, this.mouseY);
   }

   boolean isVisibleInClip(autismclient.gui.f.NetHelper bounds) {
      autismclient.gui.f.NetHelper c = this.clipBounds(bounds);
      return c.update() > 0 && c.sync() > 0;
   }

   void addClickRegion(autismclient.gui.f.NetHelper bounds, Runnable left) {
      this.addInteractiveRegion(bounds, left, null, null);
   }

   void addInteractiveRegion(autismclient.gui.f.NetHelper bounds, Runnable left, Runnable right, String tip) {
      autismclient.gui.f.NetHelper c = this.clipBounds(bounds);
      if (c.update() > 0 && c.sync() > 0) {
         this.hitRegions.add(new AiUiKit.HitRegion(c, left, right, tip, null));
      }
   }

   void blockClicks(autismclient.gui.f.NetHelper bounds) {
      this.addInteractiveRegion(bounds, () -> {}, () -> {}, null);
   }

   void addTooltipRegion(autismclient.gui.f.NetHelper bounds, String tip) {
      this.addInteractiveRegion(bounds, null, null, tip);
   }

   void registerScrollPane(AiUiKit.ScrollPane scroll) {
      this.scrollRegions.add(new AiUiKit.ScrollRegion(scroll.viewportBounds(), scroll));
      if (scroll.metrics != null && scroll.metrics.isScrollable()) {
         this.activeScrollbars.add(scroll);
      }
   }

   void blockScroll(autismclient.gui.f.NetHelper bounds) {
      this.scrollRegions.add(new AiUiKit.ScrollRegion(bounds, null));
   }

   void drawScrollbar(AiUiKit.ScrollPane scroll) {
      if (scroll.metrics != null) {
         ScrollbarMetricsHelper.render(
            this.graphics, scroll.metrics, scroll.metrics.containsTrack(this.mouseX, this.mouseY), scroll.isDragging()
         );
      }
   }

   void drawTextInput(autismclient.gui.ui.widgets.TextInputWidget field, autismclient.gui.f.NetHelper bounds) {
      field.setBounds(bounds.read(), bounds.write(), bounds.update(), bounds.sync());
      field.render(this.layoutContext);
      autismclient.gui.f.NetHelper c = this.clipBounds(bounds);
      if (c.update() > 0 && c.sync() > 0) {
         this.hitRegions.add(new AiUiKit.HitRegion(c, null, null, null, field));
      }
   }

   void drawButton(autismclient.gui.f.NetHelper bounds, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, Runnable action) {
      this.drawButton(bounds, label, tone, action, null);
   }

   void drawButton(autismclient.gui.f.NetHelper bounds, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, Runnable action, String tip) {
      boolean colored = tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.read
         || tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.write
         || tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.update;
      autismclient.gui.f.b.CachePool.init(this.renderContext, bounds, label, tone, this.isHovered(bounds), colored);
      this.addInteractiveRegion(bounds, action, null, tip);
   }

   void drawIconLabelButton(
      autismclient.gui.f.NetHelper bounds, Identifier icon, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, Runnable action
   ) {
      boolean colored = tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.read
         || tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.write
         || tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.update;
      autismclient.gui.f.b.CachePool.init(this.renderContext, bounds, label, icon, tone, this.isHovered(bounds), colored, colored ? 1.0F : 0.0F);
      this.addClickRegion(bounds, action);
   }

   void drawIconButton(autismclient.gui.f.NetHelper bounds, Identifier icon, boolean selected, boolean danger, String tip, Runnable action) {
      autismclient.gui.ui.widgets.IconButtonRenderer.renderIcon(this.graphics, this.font, bounds, icon, this.isHovered(bounds), true, danger);
      if (selected) {
         int radius = Math.min(3, Math.max(2, Math.min(bounds.update(), bounds.sync()) / 3));
         PacketUtil.load(this.graphics, bounds, radius, PacketUtil.init(this.colors.accent, 0.14F), this.colors.accent);
      }

      this.addInteractiveRegion(bounds, action, null, tip);
   }

   void drawToggleSwitch(autismclient.gui.f.NetHelper bounds, String label, boolean value, String key, Runnable action) {
      autismclient.gui.ui.widgets.ToggleSwitchRenderer.init(this.renderContext, bounds, label, value, this.isHovered(bounds), "ai:" + key);
      this.addClickRegion(bounds, action);
   }

   void drawText(String text, int x, int y, int color) {
      this.renderContext.init_PoolEntry().draw(this.graphics, text, x, y, color);
   }

   void drawEllipsizedText(String text, int x, int y, int maxWidth, int color) {
      this.renderContext.init_PoolEntry().drawEllipsized(this.graphics, text, x, y, maxWidth, color);
   }

   void drawCenteredRowText(String text, autismclient.gui.f.NetHelper row, int x, int maxWidth, int color) {
      this.renderContext.init_PoolEntry().drawEllipsized(this.graphics, text, x, this.renderContext.init_PoolEntry().centerTextY(row), maxWidth, color);
   }

   String ellipsizeLeft(String text, int maxWidth) {
      String flat = text.replace('\n', ' ').trim();
      if (this.measureTextWidth(flat) <= maxWidth) {
         return flat;
      }

      int lo = 0;
      int hi = flat.length();

      while (lo < hi) {
         int mid = lo + hi >>> 1;
         if (this.measureTextWidth("..." + flat.substring(mid)) <= maxWidth) {
            hi = mid;
         } else {
            lo = mid + 1;
         }
      }

      return "..." + flat.substring(Math.min(lo, flat.length()));
   }

   void drawStatusDot(int cx, int cy, float r, int color) {
      PacketUtil.init_V_GuiGraphicsExtract(this.graphics, cx, cy, r, color);
   }

   boolean mouseClicked(double x, double y, int button, List<autismclient.gui.ui.widgets.TextInputWidget> fields) {
      int ix = (int)x;
      int iy = (int)y;

      for (int i = this.activeScrollbars.size() - 1; i >= 0 && button == 0; i--) {
         AiUiKit.ScrollPane s = this.activeScrollbars.get(i);
         if (s.metrics.containsTrack(x, y)) {
            s.dragGrabOffset = s.metrics.containsThumb(x, y) ? iy - s.metrics.thumbY() : s.metrics.thumbHeight() / 2;
            this.draggingScroll = s;
            this.mouseDragged(y);
            return true;
         }
      }

      for (int i = this.hitRegions.size() - 1; i >= 0; i--) {
         AiUiKit.HitRegion hit = this.hitRegions.get(i);
         if (hit.bounds().init_Z(ix, iy)) {
            if (hit.field() != null) {
               for (autismclient.gui.ui.widgets.TextInputWidget other : fields) {
                  if (other != hit.field()) {
                     other.setFocused(false);
                  }
               }

               if (button == 0) {
                  hit.field().mouseClicked(this.layoutContext, (float)x, (float)y, button);
               }

               return true;
            }

            Runnable action = button == 1 ? hit.right() : (button == 0 ? hit.left() : null);
            if (action != null || hit.left() != null || hit.right() != null) {
               for (autismclient.gui.ui.widgets.TextInputWidget field : fields) {
                  field.setFocused(false);
               }

               if (action != null) {
                  action.run();
               }

               return true;
            }
         }
      }

      for (autismclient.gui.ui.widgets.TextInputWidget field : fields) {
         field.setFocused(false);
      }

      return false;
   }

   boolean mouseDragged(double y) {
      if (this.draggingScroll == null) {
         return false;
      }

      this.draggingScroll.offset = ScrollbarMetricsHelper.scrollOffsetForMouseY(this.draggingScroll.metrics, y, this.draggingScroll.dragGrabOffset);
      this.draggingScroll.followBottom = this.draggingScroll.offset >= this.draggingScroll.maxScroll();
      return true;
   }

   boolean mouseReleased() {
      if (this.draggingScroll == null) {
         return false;
      }

      this.draggingScroll.dragGrabOffset = -1;
      this.draggingScroll = null;
      return true;
   }

   boolean mouseScrolled(double x, double y, double amount) {
      for (int i = this.scrollRegions.size() - 1; i >= 0; i--) {
         AiUiKit.ScrollRegion s = this.scrollRegions.get(i);
         if (s.bounds().init_Z((int)x, (int)y)) {
            if (s.scroll() != null) {
               s.scroll().scrollByWheel(amount);
            }

            return true;
         }
      }

      return false;
   }

   void renderHoveredTooltip() {
      for (int i = this.hitRegions.size() - 1; i >= 0; i--) {
         AiUiKit.HitRegion hit = this.hitRegions.get(i);
         if (hit.bounds().init_Z(this.mouseX, this.mouseY)) {
            if (hit.tip() != null && !hit.tip().isBlank()) {
               autismclient.gui.ui.widgets.TooltipRenderer.render(this.renderContext, hit.tip(), this.mouseX, this.mouseY);
            }

            if (hit.tip() != null || hit.left() != null || hit.field() != null) {
               return;
            }
         }
      }
   }

   static autismclient.gui.sdf.MsdfFontFace fontFaceForStyle(int style) {
      if ((style & 1) != 0) {
         return autismclient.gui.sdf.MsdfFontFace.TITLE;
      } else {
         return (style & 2) != 0 ? autismclient.gui.sdf.MsdfFontFace.LABEL : autismclient.gui.sdf.MsdfFontFace.BODY;
      }
   }

   static float fontSizeForStyle(int style) {
      float body = autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(autismclient.gui.sdf.MsdfFontFace.BODY);
      return (style & 16) != 0 ? body * 1.15F : body;
   }

   void drawStyledSpan(String text, float x, int y, int style, int color) {
      if (!text.isEmpty()) {
         autismclient.gui.sdf.MsdfFontFace face = fontFaceForStyle(style);
         if (autismclient.gui.sdf.MsdfTextRenderer.canRender(face, text)) {
            float size = fontSizeForStyle(style);
            autismclient.gui.sdf.MsdfTextRenderer.draw(
               this.graphics, face, text, x, autismclient.gui.sdf.MsdfTextRenderer.alignToVanillaTop(face, y, size), size, color, false
            );
         } else {
            this.graphics.text(this.font, text, Math.round(x), y, color, false);
         }
      }
   }

   static AiTranscriptLayout.TextMeasurer createTextMeasurer(final Font font) {
      return new AiTranscriptLayout.TextMeasurer() {
         @Override
         public int measure(String text, int start, int end) {
            return this.measureStyled(text, start, end, 0);
         }

         @Override
         public int measureStyled(String text, int start, int end, int style) {
            String part = text.substring(start, end);
            autismclient.gui.sdf.MsdfFontFace face = AiUiKit.fontFaceForStyle(style);
            if (autismclient.gui.sdf.MsdfTextRenderer.canRender(face, part)) {
               autismclient.gui.sdf.MsdfFontAtlas msdf = face.atlas();
               if (msdf != null) {
                  return (int)Math.ceil(msdf.width(part, AiUiKit.fontSizeForStyle(style)) - 1.0E-4);
               }
            }

            return font.width(part);
         }
      };
   }

   static String formatRelativeAge(long at, long now) {
      long s = Math.max(0L, (now - at) / 1000L);
      if (s < 60L) {
         return "now";
      }

      long m = s / 60L;
      if (m < 60L) {
         return m + "m";
      }

      long h = m / 60L;
      return h < 48L ? h + "h" : h / 24L + "d";
   }

   static String formatCompactCount(long n) {
      if (n >= 1000000L) {
         return String.format(Locale.ROOT, "%.1fM", n / 1000000.0);
      } else if (n >= 10000L) {
         return n / 1000L + "k";
      } else {
         return n >= 1000L ? String.format(Locale.ROOT, "%.1fk", n / 1000.0) : Long.toString(n);
      }
   }

   static String flattenSingleLine(String text) {
      return text == null ? "" : text.replace('\n', ' ').trim();
   }

   private record ScrollRegion(autismclient.gui.f.NetHelper bounds, AiUiKit.ScrollPane scroll) {
   }

   private record HitRegion(autismclient.gui.f.NetHelper bounds, Runnable left, Runnable right, String tip, autismclient.gui.ui.widgets.TextInputWidget field) {
   }

   static final class ScrollPane {
      int offset;
      boolean followBottom;
      private int contentHeight;
      private int viewportHeight;
      private int dragGrabOffset = -1;
      private autismclient.gui.f.NetHelper viewportBounds = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
      private ScrollbarMetricsHelper.Metrics metrics;

      ScrollPane(boolean follow) {
         this.followBottom = follow;
      }

      int maxScroll() {
         return Math.max(0, this.contentHeight - this.viewportHeight);
      }

      void layout(autismclient.gui.f.NetHelper viewport, int content) {
         this.viewportBounds = viewport;
         this.contentHeight = content;
         this.viewportHeight = viewport.sync();
         this.offset = this.followBottom ? this.maxScroll() : Math.max(0, Math.min(this.offset, this.maxScroll()));
         this.metrics = ScrollbarMetricsHelper.compute(
            content, this.viewportHeight, viewport.init() - 6, viewport.write(), 6, viewport.sync(), this.offset
         );
      }

      void scrollByWheel(double amount) {
         this.offset = Math.max(0, Math.min(this.maxScroll(), this.offset - (int)Math.round(amount * 24.0)));
         this.followBottom = this.offset >= this.maxScroll();
      }

      void scrollToBottom() {
         this.followBottom = true;
         this.offset = 2147483647;
      }

      void scrollToTop() {
         this.followBottom = false;
         this.offset = 0;
      }

      boolean isDragging() {
         return this.dragGrabOffset >= 0;
      }

      autismclient.gui.f.NetHelper viewportBounds() {
         return this.viewportBounds;
      }
   }
}
