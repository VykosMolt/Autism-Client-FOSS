package autismclient.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class AutismProfilesOverlay extends DraggableOverlayWindow {
   private static final String init_Str_2 = "autism-profiles";
   private static final String load_Str = "Profiles Library";
   private static final int flush_I = 380;
   private static final int close_I = 1;
   private final autismclient.gui.profiles.ProfilesLibraryView init_StreamUtil;
   private final Font init_Font;
   private boolean write_Z;
   private double init_D;
   private double load_D;
   private boolean update_Z;
   private int open;
   private int poll;
   private int push;
   private int pop;

   public AutismProfilesOverlay(Font font) {
      super("autism-profiles", 300, 170);
      this.init_Font = font;
      this.init_StreamUtil = new autismclient.gui.profiles.ProfilesLibraryView(font);
      this.init_I_2 = 90;
      this.load_I = 40;
   }

   public void write_V_22() {
      this.load(false);
      this.init_V_Z(!this.init_Z);
      if (this.init_Z) {
         OverlayManager.getInstance().bringToFront(this);
      }
   }

   public void update_V_19() {
      this.load(false);
      this.init_V_Z(true);
   }

   public void load(boolean v) {
      if (v != this.update_Z) {
         if (v) {
            this.open = this.init_I_2;
            this.poll = this.load_I;
            this.push = this.read_I;
            this.pop = this.write;
            this.load_Z = false;
         } else {
            this.init_I_2 = this.open;
            this.load_I = this.poll;
            this.read_I = this.push;
            this.write = this.pop;
         }

         this.update_Z = v;
      }
   }

   @Override
   public void init_V_Z(boolean v) {
      if (!v && this.init_Z) {
         this.init_StreamUtil.init_V();
      }

      if (!v && this.update_Z) {
         this.init_I_2 = this.open;
         this.load_I = this.poll;
         this.read_I = this.push;
         this.write = this.pop;
         this.update_Z = false;
      }

      super.init_V_Z(v);
   }

   @Override
   public int init_I() {
      return 160;
   }

   @Override
   public int load_I() {
      return 100;
   }

   @Override
   public AutismOverlay.RenderLayer init_cX_DataUtil() {
      return AutismOverlay.RenderLayer.HOST_SCREEN;
   }

   @Override
   public boolean f_() {
      return false;
   }

   @Override
   public boolean load_Z() {
      return !this.update_Z;
   }

   @Override
   public boolean write_Z() {
      return this.init_StreamUtil.load_Z();
   }

   @Override
   public void push_V() {
      this.init_StreamUtil.load_V();
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      if (this.init_Z || this.a__Z()) {
         if (this.update_Z) {
            this.load(ctx, mx, my, delta);
         } else {
            this.read_I = this.init_StreamUtil.init_I() + 2;
            int want = this.init_StreamUtil.init(this.read_I - 2) + 14 + 1;
            int maxH = Math.min(cA.load_I() - 16, 380);
            this.write = Math.max(this.load_I(), Math.min(want, maxH));
            OverlayBounds bounds = this.clampCurrentBounds(this);
            this.init_I_2 = bounds.x;
            this.load_I = bounds.y;
            this.read_I = bounds.width;
            this.write = bounds.height;
            autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(ctx, this.init_Font, mx, my).init_SyncManager();
            this.renderWindowFrame(context, this.init_cH(), "Profiles Library", this.load_Z, this.write_Z);
            if (this.init_Z && !this.isVisuallyCollapsed(this.load_Z)) {
               boolean clipped = this.beginBodyClip(ctx, this.init_cH(), this.load_Z);

               try {
                  this.init_V_SyncManager_2(context, 14, delta);
               } finally {
                  this.endBodyClip(ctx, clipped);
               }
            }
         }
      }
   }

   private void load(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      int sw = cA.init_I();
      int sh = cA.load_I();
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), -1073741824);
      int margin = 6;
      int headerHeight = autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
      int availableHeight = Math.max(1, sh - autismclient.gui.ui.UiDensityMetrics.scaledHeight(44));
      this.read_I = Math.max(1, Math.min(sw - margin * 2, this.init_StreamUtil.init_I() + 2));
      this.write = Math.min(availableHeight, this.init_StreamUtil.init(this.read_I - 2) + headerHeight + 1);
      this.init_I_2 = (sw - this.read_I) / 2;
      this.load_I = (availableHeight - this.write) / 2;
      autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(ctx, this.init_Font, mx, my).init_SyncManager();
      autismclient.gui.ui.widgets.WindowFrameRenderer.init(
         context,
         autismclient.gui.f.NetHelper.init(this.init_I_2, this.load_I, this.read_I, this.write),
         "Profiles Library",
         false,
         false,
         false,
         false,
         true,
         4,
         4,
         headerHeight
      );
      this.init_V_SyncManager_2(context, headerHeight, delta);
   }

   private void init_V_SyncManager_2(autismclient.gui.f.SyncManager context, int headerHeight, float delta) {
      autismclient.gui.f.NetHelper body = autismclient.gui.f.NetHelper.init(this.init_I_2, this.load_I, this.read_I, this.write).load(1, headerHeight, 1, 1);
      if (body.update() != 0 && body.sync() != 0) {
         autismclient.gui.ui.UiClipStack.getInstance().push(context.init_GuiGraphicsExtract(), body);

         try {
            this.init_StreamUtil.init_V_SyncManager(context, body, delta);
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(context.init_GuiGraphicsExtract());
         }
      }
   }

   @Override
   public boolean init_Z_D_3(double mx, double my, int button) {
      if (!this.init_Z) {
         return false;
      } else {
         OverlayBounds bounds = this.init_cH();
         if (!this.update_Z && this.isOverHeaderCloseButton(mx, my, bounds)) {
            this.init_V_Z(false);
            this.write_Z = false;
            return true;
         } else if (!this.update_Z && button == 0 && this.load_Z_D_D(mx, my)) {
            this.write_Z = true;
            this.init_D = mx - this.init_I_2;
            this.load_D = my - this.load_I;
            return true;
         } else if (this.load_Z) {
            return false;
         } else {
            return this.init_StreamUtil.init_Z_I_2((int)Math.round(mx), (int)Math.round(my), button) ? true : this.init_Z_D_D(mx, my);
         }
      }
   }

   @Override
   public boolean load_Z_D_3(double mx, double my, int button) {
      if (this.write_Z) {
         this.write_Z = false;
         this.a__V();
         return true;
      } else {
         return this.init_StreamUtil.load((int)Math.round(mx), (int)Math.round(my), button);
      }
   }

   @Override
   public boolean init_Z_D(double mx, double my, int button, double dx, double dy) {
      if (this.write_Z) {
         OverlayBounds c = this.clampToScreen(
            this, new OverlayBounds((int)Math.round(mx - this.init_D), (int)Math.round(my - this.load_D), this.read_I, this.write, this.init_Z, this.load_Z)
         );
         this.init_I_2 = c.x;
         this.load_I = c.y;
         return true;
      } else {
         return this.load_Z ? false : this.init_StreamUtil.init_Z_I((int)Math.round(mx), (int)Math.round(my), button, dx, dy);
      }
   }

   @Override
   public boolean init_Z_D_2(double mx, double my, double amount) {
      return this.init_Z && !this.load_Z && this.init_Z_D_D(mx, my) ? this.init_StreamUtil.init_Z_I((int)Math.round(mx), (int)Math.round(my), amount) : false;
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      return this.init_Z && !this.load_Z && this.init_StreamUtil.read(keyCode, scanCode, modifiers);
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      return this.init_Z && !this.load_Z && this.init_StreamUtil.init_Z_C_I(chr, modifiers);
   }
}
