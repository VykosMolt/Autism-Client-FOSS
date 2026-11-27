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
   private static final int load_I = THEME.poll();
   private static final int read = THEME.set();
   private static final int write = THEME.run();
   private static final int update = THEME.load(l.read);
   private static final int sync = THEME.load(l.write);
   private static final int flush = 300;
   private static final int close = 3;
   private static final int open = 2;
   private final Screen parent;
   private final String[] passwords;
   private final List<String> init_List;
   private int poll = 0;
   private boolean init_Z = false;
   private double dragOffset = 0.0;
   private final List<ForceOpPreviewScreen.Inner_DataUtil> hitRegions = new ArrayList<>();

   public ForceOpPreviewScreen(Screen parent, String[] passwords) {
      super(Component.literal("ForceOp Preview"));
      this.parent = parent;
      this.passwords = passwords;
      this.init_List = new ArrayList<>();
      int limit = Math.min(passwords.length, 500);

      for (int i = 0; i < limit; i++) {
         this.init_List.add(passwords[i]);
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   private int read() {
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualWidth(), 6, 300);
   }

   private int write() {
      int rowH = this.sync();
      int maxRows = Math.max(3, (int)(this.virtualHeight() * 0.65F) / rowH);
      int rows = Math.max(3, Math.min(maxRows, this.init_List.size()));
      int chrome = this.update() + 8 + 4;
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualHeight(), 6, chrome + rows * rowH);
   }

   private int update() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int sync() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int flush() {
      return this.init_List.size() * this.sync();
   }

   private autismclient.gui.f.NetHelper listBounds() {
      int x = this.close();
      int y = this.open();
      int headerH = this.update();
      return autismclient.gui.f.NetHelper.init(x + 4, y + headerH + 4, this.read() - 8, this.write() - headerH - 8);
   }

   private autismclient.gui.f.NetHelper innerBounds() {
      autismclient.gui.f.NetHelper list = this.listBounds();
      return autismclient.gui.f.NetHelper.init(list.read() + 2, list.write() + 2, list.update() - 4, list.sync() - 4);
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics() {
      autismclient.gui.f.NetHelper inner = this.innerBounds();
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(this.flush(), inner.sync(), inner.init() - 6, inner.write(), 6, inner.sync(), this.poll);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         this.hitRegions.clear();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -1442840576);
         int x = this.close();
         int y = this.open();
         int w = this.read();
         int h = this.write();
         this.init(graphics, x, y, w, h, "Password Preview (First " + this.init_List.size() + ")", mx, my);
         if (w < 100 || h < this.update() + 8 + 4 + this.sync()) {
            return;
         }

         autismclient.gui.f.NetHelper list = this.listBounds();
         autismclient.gui.f.PacketUtil.load(graphics, list, 5, -1442248437, read);
         autismclient.gui.f.NetHelper inner = this.innerBounds();
         int innerX = inner.read();
         int innerY = inner.write();
         int innerW = inner.update();
         int innerH = inner.sync();
         int rowH = this.sync();
         int maxScroll = Math.max(0, this.flush() - innerH);
         this.poll = Math.max(0, Math.min(maxScroll, this.poll));
         autismclient.gui.ui.UiClipStack.getInstance()
            .push(graphics, autismclient.gui.f.NetHelper.init(innerX, innerY, Math.max(0, innerW), Math.max(0, innerH)));

         try {
            int contentY = innerY - this.poll;

            for (int i = 0; i < this.init_List.size(); i++) {
               int rowY = contentY + i * rowH;
               if (rowY + rowH > innerY && rowY < innerY + innerH) {
                  int color = i % 2 == 0 ? -10046721 : -24474;
                  autismclient.gui.ui.widgets.PanelSurfaceHelper.drawCardRow(graphics, innerX, rowY, innerW - 8, rowH, false, false);
                  String text = k.init(this.font, this.init_List.get(i), innerW - 8 - 8, THEME.init_Identifier_l(l.read), color);
                  this.init(graphics, text, innerX + 4, UiLayoutMath.centerTextY(rowY, rowH, THEME.init_I_l(l.read), THEME.flush()), color, innerW - 8 - 8);
               }
            }
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(graphics);
         }

         if (maxScroll > 0) {
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.scrollbarMetrics();
            boolean overBar = metrics.containsTrack(mx, my);
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(graphics, metrics, overBar, this.init_Z);
            this.hitRegions.add(new ForceOpPreviewScreen.Inner_DataUtil(ForceOpPreviewScreen.Inner_StreamUtil.SCROLLBAR, innerX + innerW - 6, innerY, 6, innerH));
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int width, int height, String title, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      int headerH = this.update();
      autismclient.gui.ui.widgets.DialogFrameRenderer.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my), bounds, headerH, title, mx >= x && mx < x + width && my >= y && my < y + headerH
      );
      autismclient.gui.f.NetHelper close = autismclient.gui.ui.widgets.DialogFrameRenderer.init(bounds, headerH);
      this.hitRegions.add(new ForceOpPreviewScreen.Inner_DataUtil(ForceOpPreviewScreen.Inner_StreamUtil.CLOSE, close.read(), close.write(), close.update(), close.sync()));
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int w, int h, int color) {
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

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      k.init(graphics, this.font, text, THEME.init_Identifier_l(l.read), color, x, y, false);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean allowBypass) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());

      for (ForceOpPreviewScreen.Inner_DataUtil hit : this.hitRegions) {
         if (hit.init(mx, my)) {
            if (hit.kind == ForceOpPreviewScreen.Inner_StreamUtil.CLOSE && Compat263.mouseEventGlfwButton(event) == 0) {
               this.onClose();
               return true;
            }

            if (hit.kind == ForceOpPreviewScreen.Inner_StreamUtil.SCROLLBAR && Compat263.mouseEventGlfwButton(event) == 0) {
               autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics metrics = this.scrollbarMetrics();
               if (metrics.containsThumb(mx, my)) {
                  this.init_Z = true;
                  this.dragOffset = my - metrics.thumbY();
               } else {
                  this.poll = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(metrics, my, metrics.thumbHeight() / 2);
               }

               return true;
            }
         }
      }

      if (mx >= this.close() && mx <= this.close() + this.read() && my >= this.open() && my <= this.open() + this.write()) {
         return super.mouseClicked(event, allowBypass);
      }

      this.onClose();
      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.init_Z = false;
      return super.mouseReleased(event);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.init_Z) {
         this.poll = autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(this.scrollbarMetrics(), my, (int)this.dragOffset);
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
         this.poll = (int)(this.poll - scrollY * this.sync() * 2.0);
         int maxScroll = Math.max(0, this.flush() - this.innerBounds().sync());
         this.poll = Math.max(0, Math.min(maxScroll, this.poll));
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

   private int close() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualWidth(), this.read(), 6);
   }

   private int open() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualHeight(), this.write(), 6);
   }

   private record Inner_DataUtil(ForceOpPreviewScreen.Inner_StreamUtil kind, int x, int load, int read, int write) {
      boolean init(int mx, int my) {
         return mx >= this.x && mx < this.x + this.read && my >= this.load && my < this.load + this.write;
      }
   }

   private enum Inner_StreamUtil {
      CLOSE,
      SCROLLBAR;
   }
}
