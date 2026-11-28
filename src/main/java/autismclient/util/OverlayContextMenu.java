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
   private final int lineHeight;
   private boolean visible;
   private int menuX;
   private int menuY;
   private T target;
   private String[] items = new String[0];
   private int cachedWidth = -1;
   private int cachedHeight = -1;
   private int cachedScreenWidth = -1;
   private int cachedScreenHeight = -1;

   public OverlayContextMenu(autismclient.gui.f.b.CharEncoder theme, Font font, Function<T, String[]> itemProvider, int lineHeight) {
      this.theme = theme;
      this.font = font;
      this.itemProvider = itemProvider;
      this.lineHeight = Math.max(12, lineHeight);
   }

   public boolean isOpen() {
      return this.visible && this.target != null;
   }

   public T getTarget() {
      return this.target;
   }

   public void open(int mouseX, int mouseY, T target) {
      this.target = target;
      this.menuX = mouseX;
      this.menuY = mouseY;
      this.visible = true;
      String[] items = this.itemProvider.apply(target);
      this.items = items == null ? new String[0] : items;
      this.invalidateLayout();
      this.clampToScreen();
   }

   public void close() {
      this.visible = false;
      this.target = null;
      this.items = new String[0];
      this.invalidateLayout();
   }

   private String[] visibleItems() {
      return this.visible && this.target != null ? this.items : new String[0];
   }

   private int menuWidth(String[] items) {
      this.ensureLayout(items);
      return this.cachedWidth;
   }

   private int menuHeight(String[] items) {
      this.ensureLayout(items);
      return this.cachedHeight;
   }

   private void ensureLayout(String[] items) {
      int screenWidth = Math.max(1, cA.init_I());
      int screenHeight = Math.max(1, cA.load_I());
      if (this.cachedWidth < 0 || this.cachedHeight < 0 || this.cachedScreenWidth != screenWidth || this.cachedScreenHeight != screenHeight) {
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

         this.cachedWidth = Math.min(Math.max(1, screenWidth - 4), maxW + 8);
         this.cachedHeight = this.visibleRowCount(items) * this.lineHeight + 4;
         this.cachedScreenWidth = screenWidth;
         this.cachedScreenHeight = screenHeight;
      }
   }

   private void invalidateLayout() {
      this.cachedWidth = -1;
      this.cachedHeight = -1;
      this.cachedScreenWidth = -1;
      this.cachedScreenHeight = -1;
   }

   private int visibleRowCount(String[] items) {
      int available = Math.max(1, cA.load_I() - 4 - 4);
      return Math.max(0, Math.min(items.length, available / Math.max(1, this.lineHeight)));
   }

   private void clampToScreen() {
      if (this.visible) {
         String[] items = this.visibleItems();
         int w = this.menuWidth(items);
         int h = this.menuHeight(items);
         int sw = Math.max(1, cA.init_I());
         int sh = Math.max(1, cA.load_I());
         this.menuX = Math.max(2, Math.min(this.menuX, sw - w - 2));
         this.menuY = Math.max(2, Math.min(this.menuY, sh - h - 2));
      }
   }

   public boolean containsPoint(double mx, double my) {
      if (!this.isOpen()) {
         return false;
      }

      String[] items = this.visibleItems();
      int w = this.menuWidth(items);
      int h = this.menuHeight(items);
      return mx >= this.menuX && mx < this.menuX + w && my >= this.menuY && my < this.menuY + h;
   }

   public void render(GuiGraphicsExtractor ctx, int mx, int my) {
      if (this.isOpen()) {
         this.clampToScreen();
         String[] items = this.visibleItems();
         int w = this.menuWidth(items);
         int h = this.menuHeight(items);
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_3(
            ctx,
            autismclient.gui.f.NetHelper.init(this.menuX - 1, this.menuY - 1, w + 2, h + 2),
            this.theme.poll(),
            this.theme.get(),
            this.theme.run()
         );
         int visibleItems = this.visibleRowCount(items);

         for (int i = 0; i < visibleItems; i++) {
            int iy = this.menuY + 2 + i * this.lineHeight;
            boolean hov = mx >= this.menuX && mx < this.menuX + w && my >= iy && my < iy + this.lineHeight;
            if (hov) {
               autismclient.gui.ui.widgets.PanelSurfaceHelper.fillRect(ctx, this.menuX + 1, iy, w - 2, this.lineHeight, UiColorPalette.contextMenuHoverFill());
            }

            int textY = UiLayoutMath.centerTextY(iy, this.lineHeight, this.theme.init_I_l(autismclient.gui.f.b.l.read), this.theme.flush());
            String label = items[i];
            int tab = label.indexOf(9);
            int labelRight = this.menuX + w - 4;
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
               this.font, label, Math.max(1, labelRight - this.menuX - 4), this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.menuItemText()
            );
            autismclient.gui.f.b.k.init(
               ctx,
               this.font,
               display,
               this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
               hov ? -1 : UiColorPalette.menuItemText(),
               this.menuX + 4,
               textY,
               false
            );
         }
      }
   }

   public boolean handleClick(double mouseX, double mouseY, int button, OverlayContextMenu.PickHandler<T> onPick) {
      if (!this.isOpen()) {
         return false;
      }

      if (button != 0) {
         this.close();
         return true;
      }

      String[] items = this.visibleItems();
      int w = this.menuWidth(items);
      boolean inX = mouseX >= this.menuX && mouseX < this.menuX + w;
      if (inX) {
         int visibleItems = this.visibleRowCount(items);

         for (int i = 0; i < visibleItems; i++) {
            int iy = this.menuY + 2 + i * this.lineHeight;
            if (mouseY >= iy && mouseY < iy + this.lineHeight) {
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
