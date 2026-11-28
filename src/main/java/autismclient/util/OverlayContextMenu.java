package autismclient.util;

import autismclient.gui.ui.widgets.UiLayoutMath;
import java.util.function.Function;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class OverlayContextMenu<T> {
   private static final int init_I = 12;
   private static final int load = 4;
   private static final int read = 2;
   private static final int write = 1;
   private final autismclient.gui.f.b.CharEncoder theme;
   private final Font font;
   private final Function<T, String[]> itemProvider;
   private final int update;
   private boolean init_Z;
   private int sync;
   private int flush;
   private T target;
   private String[] init_StrArr = new String[0];
   private int close = -1;
   private int open = -1;
   private int poll = -1;
   private int push = -1;

   public OverlayContextMenu(autismclient.gui.f.b.CharEncoder theme, Font font, Function<T, String[]> itemProvider, int lineHeight) {
      this.theme = theme;
      this.font = font;
      this.itemProvider = itemProvider;
      this.update = Math.max(12, lineHeight);
   }

   public boolean isOpen() {
      return this.init_Z && this.target != null;
   }

   public T getTarget() {
      return this.target;
   }

   public void init(int mouseX, int mouseY, T target) {
      this.target = target;
      this.sync = mouseX;
      this.flush = mouseY;
      this.init_Z = true;
      String[] items = this.itemProvider.apply(target);
      this.init_StrArr = items == null ? new String[0] : items;
      this.load();
      this.read();
   }

   public void close() {
      this.init_Z = false;
      this.target = null;
      this.init_StrArr = new String[0];
      this.load();
   }

   private String[] visibleItems() {
      return this.init_Z && this.target != null ? this.init_StrArr : new String[0];
   }

   private int init_I(String[] items) {
      this.init_V(items);
      return this.close;
   }

   private int load(String[] items) {
      this.init_V(items);
      return this.open;
   }

   private void init_V(String[] items) {
      int screenWidth = Math.max(1, cA.init_I());
      int screenHeight = Math.max(1, cA.load_I());
      if (this.close < 0 || this.open < 0 || this.poll != screenWidth || this.push != screenHeight) {
         int maxW = 0;

         for (String s : items) {
            int tab = s.indexOf(9);
            int w = tab < 0
               ? autismclient.gui.f.b.k.init(this.font, s, this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), -1)
               : autismclient.gui.f.b.k.init(this.font, s.substring(0, tab), this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), -1)
                  + 12
                  + autismclient.gui.f.b.k.init(this.font, s.substring(tab + 1), this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), -1);
            maxW = Math.max(maxW, w);
         }

         this.close = Math.min(Math.max(1, screenWidth - 4), maxW + 8);
         this.open = this.read(items) * this.update + 4;
         this.poll = screenWidth;
         this.push = screenHeight;
      }
   }

   private void load() {
      this.close = -1;
      this.open = -1;
      this.poll = -1;
      this.push = -1;
   }

   private int read(String[] items) {
      int available = Math.max(1, cA.load_I() - 4 - 4);
      return Math.max(0, Math.min(items.length, available / Math.max(1, this.update)));
   }

   private void read() {
      if (this.init_Z) {
         String[] items = this.visibleItems();
         int w = this.init_I(items);
         int h = this.load(items);
         int sw = Math.max(1, cA.init_I());
         int sh = Math.max(1, cA.load_I());
         this.sync = Math.max(2, Math.min(this.sync, sw - w - 2));
         this.flush = Math.max(2, Math.min(this.flush, sh - h - 2));
      }
   }

   public boolean init(double mx, double my) {
      if (!this.isOpen()) {
         return false;
      }

      String[] items = this.visibleItems();
      int w = this.init_I(items);
      int h = this.load(items);
      return mx >= this.sync && mx < this.sync + w && my >= this.flush && my < this.flush + h;
   }

   public void init(GuiGraphicsExtractor ctx, int mx, int my) {
      if (this.isOpen()) {
         this.read();
         String[] items = this.visibleItems();
         int w = this.init_I(items);
         int h = this.load(items);
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_3(
            ctx,
            autismclient.gui.f.NetHelper.init(this.sync - 1, this.flush - 1, w + 2, h + 2),
            this.theme.poll(),
            this.theme.get(),
            this.theme.run()
         );
         int visibleItems = this.read(items);

         for (int i = 0; i < visibleItems; i++) {
            int iy = this.flush + 2 + i * this.update;
            boolean hov = mx >= this.sync && mx < this.sync + w && my >= iy && my < iy + this.update;
            if (hov) {
               autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(ctx, this.sync + 1, iy, w - 2, this.update, UiColorPalette.contextMenuHoverFill());
            }

            int textY = UiLayoutMath.centerTextY(iy, this.update, this.theme.init_I_l(autismclient.gui.f.b.l.read), this.theme.flush());
            String label = items[i];
            int tab = label.indexOf(9);
            int labelRight = this.sync + w - 4;
            if (tab >= 0) {
               String hint = label.substring(tab + 1);
               label = label.substring(0, tab);
               int hintW = autismclient.gui.f.b.k.init(this.font, hint, this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), -1);
               autismclient.gui.f.b.k.init(
                  ctx,
                  this.font,
                  hint,
                  this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
                  this.theme.load(autismclient.gui.f.b.l.write),
                  labelRight - hintW,
                  textY,
                  false
               );
               labelRight -= hintW + 12;
            }

            String display = autismclient.gui.f.b.k.init(
               this.font, label, Math.max(1, labelRight - this.sync - 4), this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.menuItemText()
            );
            autismclient.gui.f.b.k.init(
               ctx,
               this.font,
               display,
               this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
               hov ? -1 : UiColorPalette.menuItemText(),
               this.sync + 4,
               textY,
               false
            );
         }
      }
   }

   public boolean init(double mouseX, double mouseY, int button, OverlayContextMenu.PickHandler<T> onPick) {
      if (!this.isOpen()) {
         return false;
      }

      if (button != 0) {
         this.close();
         return true;
      }

      String[] items = this.visibleItems();
      int w = this.init_I(items);
      boolean inX = mouseX >= this.sync && mouseX < this.sync + w;
      if (inX) {
         int visibleItems = this.read(items);

         for (int i = 0; i < visibleItems; i++) {
            int iy = this.flush + 2 + i * this.update;
            if (mouseY >= iy && mouseY < iy + this.update) {
               T t = this.target;
               String label = items[i];
               int idx = i;
               this.close();
               if (onPick != null) {
                  onPick.onPicked(t, label, idx);
               }

               return true;
            }
         }
      }

      this.close();
      return true;
   }

   @FunctionalInterface
   public interface PickHandler<T> {
      void onPicked(T var1, String var2, int var3);
   }
}
