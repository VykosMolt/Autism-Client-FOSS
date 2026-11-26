package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class DraggableOverlayWindow extends OverlayLayoutBase implements AutismOverlay {
   private final String init_Str;
   protected int init_I_2;
   protected int load_I;
   protected int read_I;
   protected int write;
   protected boolean init_Z;
   protected boolean load_Z;
   private OverlayContextMenu<?> init_F;
   protected boolean read_Z;
   private OverlayWindowSizeStore init_do;
   private int flush;
   private int close;

   protected DraggableOverlayWindow() {
      this(null, 0, 0);
   }

   protected DraggableOverlayWindow(String overlayId, int initialWidth, int initialHeight) {
      this.init_Str = overlayId;
      this.read_I = initialWidth;
      this.write = initialHeight;
      this.read_Z = AutismSessionState.init_ck().init_Z_Str(this.init_Str());
   }

   protected boolean d_() {
      return false;
   }

   @Override
   public boolean f_() {
      return this.read_Z;
   }

   @Override
   public AutismOverlay.RenderLayer init_cX_DataUtil() {
      return this.read_Z ? AutismOverlay.RenderLayer.BACKGROUND_STATUS : AutismOverlay.super.init_cX_DataUtil();
   }

   protected void pop_V() {
      this.read_Z = !this.read_Z;
      AutismSessionState.init_ck().init_V_Str_Z(this.init_Str(), this.read_Z);
      OverlayManager.init_bk().load_V(this);
      this.a__V();
   }

   public boolean get_Z() {
      return this.read_Z;
   }

   public void update_V_Z(boolean value) {
      if (this.read_Z != value) {
         this.pop_V();
      }
   }

   protected autismclient.gui.f.NetHelper init_NetHelper_cH(OverlayBounds bounds) {
      autismclient.gui.f.NetHelper window = autismclient.gui.f.NetHelper.init(bounds.init_I, bounds.load_I, bounds.read, 14);
      autismclient.gui.f.NetHelper bar = autismclient.gui.ui.widgets.WindowHeaderHelper.init(window, 14);
      autismclient.gui.f.NetHelper close = autismclient.gui.ui.widgets.WindowHeaderHelper.load(window, 14);
      int size = Math.min(12, Math.max(9, 10));
      int x = close.read() - 3 - size;
      int y = bar.write() + Math.max(1, (bar.sync() - size) / 2);
      return autismclient.gui.f.NetHelper.init(x, y, size, size);
   }

   protected boolean load_Z_D_2(double mouseX, double mouseY, OverlayBounds bounds) {
      return this.d_() && this.init_NetHelper_cH(bounds).init_Z((int)mouseX, (int)mouseY);
   }

   protected void init_V_GuiGraphicsExtract_13(GuiGraphicsExtractor context, int mouseX, int mouseY, OverlayBounds bounds) {
      if (this.d_()) {
         autismclient.gui.f.NetHelper pin = this.init_NetHelper_cH(bounds);
         autismclient.gui.ui.UiColorScheme colors = autismclient.gui.f.UiHelper.init(context, Minecraft.getInstance().font, mouseX, mouseY)
            .init_TableIndex()
            .init_JsonUtil();
         boolean hovered = pin.init_Z(mouseX, mouseY);
         int color = this.read_Z ? colors.clear : (hovered ? colors.prev : colors.valid);
         GuiIconAtlas.init(context, GuiIconAtlas.add, pin.read(), pin.write(), pin.update(), pin.sync(), color);
      }
   }

   @Override
   public String init_Str() {
      return this.init_Str != null ? this.init_Str : this.getClass().getSimpleName();
   }

   @Override
   public boolean sync_Z() {
      return this.init_Z;
   }

   @Override
   public void init_V_Z(boolean v) {
      this.init_Z = v;
      if (!v) {
         this.get_V();
      }

      this.a__V();
   }

   @Override
   public boolean flush_Z() {
      return this.load_Z;
   }

   @Override
   public void write_V_Z(boolean c) {
      if (this.load_Z != c) {
         this.load_Z = c;
         if (c) {
            this.get_V();
         }

         this.a__V();
      }
   }

   @Override
   public OverlayBounds init_cH() {
      return new OverlayBounds(this.init_I_2, this.load_I, this.read_I, this.write, this.init_Z, this.load_Z);
   }

   @Override
   public void init_V_cH(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds c = this.init_cH_cX_cH(this, bounds);
         this.init_I_2 = c.init_I;
         this.load_I = c.load_I;
         this.read_I = c.read;
         this.write = c.write;
         this.init_Z = c.init_Z;
         if (this.load_Z != c.load_Z) {
            this.sync_V_Z(c.load_Z);
         }

         this.load_Z = c.load_Z;
      }
   }

   protected final OverlayWindowSizeStore init_do() {
      if (this.init_do == null) {
         this.init_do = new OverlayWindowSizeStore(this.init_Str());
      }

      return this.init_do;
   }

   protected static int update_I() {
      return Math.max(1, cA.init_I() - 8);
   }

   protected static int sync_I() {
      return Math.max(1, cA.load_I() - 8);
   }

   protected int init_I_I_I(int autoWidth, int minWidth) {
      return this.read_Z() ? this.init_do().init(autoWidth, minWidth, update_I()) : autoWidth;
   }

   protected int load_I_I_I(int autoHeight, int minHeight) {
      return this.read_Z() ? this.init_do().load(autoHeight, minHeight, sync_I()) : autoHeight;
   }

   protected int flush_I() {
      return this.init_I();
   }

   protected int a__I() {
      return this.load_I();
   }

   protected boolean set_Z() {
      return this.read_Z() && this.init_do().init_Z();
   }

   @Override
   public void init_V_I_I(int width, int height) {
      if (!this.read_Z()) {
         AutismOverlay.super.init_V_I_I(width, height);
      } else {
         OverlayBounds b = this.init_cH();
         int screenW = cA.init_I();
         int screenH = cA.load_I();
         int minW = this.flush_I();
         int minH = this.a__I();
         int w = OverlayWindowSizeStore.read(width, minW, screenW > 0 ? screenW - 4 - b.init_I : width);
         int h = OverlayWindowSizeStore.read(height, minH, screenH > 0 ? screenH - 4 - b.load_I : height);
         OverlayBounds clamped = init_cH_cH(new OverlayBounds(b.init_I, b.load_I, w, h, b.init_Z, b.load_Z), minW, minH, screenW, screenH);
         this.init_do().init(clamped.read, clamped.write);
         this.init_V_cH(clamped);
         this.c_();
      }
   }

   @Override
   public void tick_V() {
      if (this.read_Z()) {
         this.init_do().init_V();
      }
   }

   @Override
   public void run_V() {
      if (this.read_Z()) {
         this.init_do().load();
         this.c_();
      }
   }

   protected void c_() {
   }

   @Override
   protected boolean read_Z_D_2(double mouseX, double mouseY, OverlayBounds bounds) {
      return super.read_Z_D_2(mouseX, mouseY, bounds) || this.load_Z_D_2(mouseX, mouseY, bounds);
   }

   @Override
   public boolean init_Z_D_D(double mx, double my) {
      if (!this.init_Z) {
         return false;
      }

      int frameH = this.close_I();
      boolean overPanel = mx >= this.init_I_2 && mx <= this.init_I_2 + this.read_I && my >= this.load_I && my <= this.load_I + frameH;
      return this.init_F != null && this.init_F.init(mx, my) ? true : overPanel;
   }

   @Override
   public boolean load_Z_D_D(double mx, double my) {
      if (!this.init_Z) {
         return false;
      } else if (!(mx < this.init_I_2) && !(mx > this.init_I_2 + this.read_I)) {
         return !(my < this.load_I) && !(my > this.load_I + 14) ? !this.read_Z_D_2(mx, my, this.init_cH()) : false;
      } else {
         return false;
      }
   }

   protected int close_I() {
      return this.load_Z ? 14 : this.write;
   }

   protected void init_V_F(OverlayContextMenu<?> menu) {
      this.init_F = menu;
   }

   protected OverlayContextMenu<?> init_F_3() {
      return this.init_F;
   }

   protected final void get_V() {
      this.push_V();
      if (this.init_F != null) {
         this.init_F.init_V();
      }
   }

   protected int read_I_I_I(int mouse, int axis) {
      if (this.init_F != null && this.init_F.init_Z()) {
         int otherAxis = axis == 0 ? this.close : this.flush;
         boolean covered = axis == 0 ? this.init_F.init(mouse, otherAxis) : this.init_F.init(otherAxis, mouse);
         return covered ? -10000 : mouse;
      } else {
         return mouse;
      }
   }

   protected final void load_V_I_I(int mx, int my) {
      this.flush = mx;
      this.close = my;
   }

   protected int write_I_I_I(int mx, int my) {
      return this.init_F != null && this.init_F.init(mx, my) ? -10000 : mx;
   }

   protected int update_I_I_I(int mx, int my) {
      return this.init_F != null && this.init_F.init(mx, my) ? -10000 : my;
   }
}
