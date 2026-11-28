package autismclient.gui.screens;

import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.cA;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ForceOpPreviewScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = -1442840576;
   private static final int unusedThemeColor = THEME.poll();
   private static final int read = THEME.set();
   private static final int write = THEME.run();
   private static final int update = THEME.load(l.read);
   private static final int sync = THEME.load(l.write);
   private static final int MAX_PANEL_WIDTH = 300;
   private static final int MIN_ROWS = 3;
   private static final int open = 2;
   private final Screen parent;
   private final String[] passwords;
   private final List<String> passwordList;
   private int scrollOffset = 0;
   private boolean scrollbarDragging = false;
   private double dragOffset = 0.0;
   private final List<ForceOpPreviewScreen.HitRegion> hitRegions = new ArrayList<>();

   public ForceOpPreviewScreen(Screen parent, String[] passwords) {
      super(Component.literal("ForceOp Preview"));
      this.parent = parent;
      this.passwords = passwords;
      this.passwordList = new ArrayList<>();
      int limit = Math.min(passwords.length, 500);

      for (int i = 0; i < limit; i++) {
         this.passwordList.add(passwords[i]);
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   private int panelWidth() {
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualWidth(), 6, 300);
   }

   private int panelHeight() {
      int rowH = this.rowHeight();
      int maxRows = Math.max(3, (int)(this.virtualHeight() * 0.65F) / rowH);
      int rows = Math.max(3, Math.min(maxRows, this.passwordList.size()));
      int chrome = this.headerHeight() + 8 + 4;
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualHeight(), 6, chrome + rows * rowH);
   }

   private int headerHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int contentHeight() {
      return this.passwordList.size() * this.rowHeight();
   }

   private autismclient.gui.f.NetHelper listBounds() {
      int x = this.panelX();
      int y = this.panelY();
      int headerH = this.headerHeight();
      return autismclient.gui.f.NetHelper.init(x + 4, y + headerH + 4, this.panelWidth() - 8, this.panelHeight() - headerH - 8);
   }

   private autismclient.gui.f.NetHelper innerBounds() {
      autismclient.gui.f.NetHelper list = this.listBounds();
      return autismclient.gui.f.NetHelper.init(list.read() + 2, list.write() + 2, list.update() - 4, list.sync() - 4);
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics() {
      autismclient.gui.f.NetHelper inner = this.innerBounds();
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(this.contentHeight(), inner.sync(), inner.init() - 6, inner.write(), 6, inner.sync(), this.scrollOffset);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         this.hitRegions.clear();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -1442840576);
         int x = this.panelX();
         int y = this.panelY();
         int w = this.panelWidth();
         int h = this.panelHeight();
         this.drawFrame(graphics, x, y, w, h, "Password Preview (First " + this.passwordList.size() + ")", mx, my);
         if (w < 100 || h < this.headerHeight() + 8 + 4 + this.rowHeight()) {
            return;
         }

         autismclient.gui.f.NetHelper list = this.listBounds();
         autismclient.gui.f.PacketUtil.load(graphics, list, 5, -1442248437, read);
         autismclient.gui.f.NetHelper inner = this.innerBounds();
         int innerX = inner.read();
         int innerY = inner.write();
         int innerW = inner.update();
         int innerH = inner.sync();
         int rowH = this.rowHeight();
         int maxScroll = Math.max(0, this.contentHeight() - innerH);
         this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset));
         autismclient.gui.ui.UiClipStack.getInstance()
            .push(graphics, autismclient.gui.f.NetHelper.init(innerX, innerY, Math.max(0, innerW), Math.max(0, innerH)));

         try {
            int contentY = innerY - this.scrollOffset;

            for (int i = 0; i < this.passwordList.size(); i++) {
               int rowY = contentY + i * rowH;
               if (rowY + rowH > innerY && rowY < innerY + innerH) {
                  int color = i % 2 == 0 ? -10046721 : -24474;
                  autismclient.gui.ui.widgets.PanelSurfaceHelper.drawCardRow(graphics, innerX, rowY, innerW - 8, rowH, false, false);
                  String text = k.init(this.font, this.passwordList.get(i), innerW - 8 - 8, THEME.init_Identifier_l(l.read), color);
                  this.drawText(graphics, text, innerX + 4, UiLayoutMath.centerTextY(rowY, rowH, THEME.init_I_l(l.read), THEME.flush()), color, innerW - 8 - 8);
               }
            }
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(graphics);
         }

         if (maxScroll > 0) {
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.scrollbarMetrics();
            boolean overBar = metrics.containsTrack(mx, my);
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(graphics, metrics, overBar, this.scrollbarDragging);
            this.hitRegions.add(new ForceOpPreviewScreen.HitRegion(ForceOpPreviewScreen.HitKind.SCROLLBAR, innerX + innerW - 6, innerY, 6, innerH));
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void drawFrame(GuiGraphicsExtractor graphics, int x, int y, int width, int height, String title, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      int headerH = this.headerHeight();
      autismclient.gui.ui.widgets.DialogFrameRenderer.draw(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my), bounds, headerH, title, mx >= x && mx < x + width && my >= y && my < y + headerH
      );
      autismclient.gui.f.NetHelper close = autismclient.gui.ui.widgets.DialogFrameRenderer.headerBounds(bounds, headerH);
      this.hitRegions.add(new ForceOpPreviewScreen.HitRegion(ForceOpPreviewScreen.HitKind.CLOSE, close.read(), close.write(), close.update(), close.sync()));
   }

   private void unusedDrawCenteredText(GuiGraphicsExtractor graphics, String text, int x, int y, int w, int h, int color) {
      String display = k.init(this.font, text, Math.max(0, w - 8), THEME.init_Identifier_l(l.load), color);
      int tw = k.init(this.font, display, THEME.init_Identifier_l(l.load), color);
      int th = THEME.init_I_l(l.load);
      k.init(
         graphics,
         this.font,
         display,
         THEME.init_Identifier_l(l.load),
         color,
         x + Math.max(2, (w - tw) / 2),
         y + Math.max(1, (h - th + 1) / 2 + 1),
         false
      );
   }

   private void drawText(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      k.init(graphics, this.font, text, THEME.init_Identifier_l(l.read), color, x, y, false);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean allowBypass) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());

      for (ForceOpPreviewScreen.HitRegion hit : this.hitRegions) {
         if (hit.contains(mx, my)) {
            if (hit.kind == ForceOpPreviewScreen.HitKind.CLOSE && Compat263.mouseEventGlfwButton(event) == 0) {
               this.onClose();
               return true;
            }

            if (hit.kind == ForceOpPreviewScreen.HitKind.SCROLLBAR && Compat263.mouseEventGlfwButton(event) == 0) {
               autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.scrollbarMetrics();
               if (metrics.containsThumb(mx, my)) {
                  this.scrollbarDragging = true;
                  this.dragOffset = my - metrics.thumbY();
               } else {
                  this.scrollOffset = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, my, metrics.thumbHeight() / 2);
               }

               return true;
            }
         }
      }

      if (mx >= this.panelX() && mx <= this.panelX() + this.panelWidth() && my >= this.panelY() && my <= this.panelY() + this.panelHeight()) {
         return super.mouseClicked(event, allowBypass);
      }

      this.onClose();
      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.scrollbarDragging = false;
      return super.mouseReleased(event);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.scrollbarDragging) {
         this.scrollOffset = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(this.scrollbarMetrics(), my, (int)this.dragOffset);
         return true;
      } else {
         return super.mouseDragged(event, dragX, dragY);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      autismclient.gui.f.NetHelper list = this.listBounds();
      if (list.init_Z(mx, my)) {
         this.scrollOffset = (int)(this.scrollOffset - scrollY * this.rowHeight() * 2.0);
         int maxScroll = Math.max(0, this.contentHeight() - this.innerBounds().sync());
         this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset));
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      }
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (Compat263.keyEventGlfwKey(event) == 256) {
         this.onClose();
         return true;
      } else {
         return super.keyPressed(event);
      }
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
      }
   }

   private int panelX() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualWidth(), this.panelWidth(), 6);
   }

   private int panelY() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualHeight(), this.panelHeight(), 6);
   }

   private record HitRegion(ForceOpPreviewScreen.HitKind kind, int x, int y, int width, int height) {
      boolean contains(int mx, int my) {
         return mx >= this.x && mx < this.x + this.width && my >= this.y && my < this.y + this.height;
      }
   }

   private enum HitKind {
      CLOSE,
      SCROLLBAR;
   }
}
