package autismclient.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public final class AutismMatchmakingOverlay extends bj implements autismclient.gui.a.DataUtil.BufferHelper {
   private static final String init_Str_2 = "autism-matchmaking";
   private final autismclient.gui.a.DataUtil init_DataUtil;
   private final Font init_Font;
   private boolean write_Z;
   private double init_D;
   private double load_D;
   private boolean update_Z;
   private int flush_I;
   private int close_I;
   private int open;
   private int poll;
   private static final int push = 360;
   private static final int pop = 2;

   public AutismMatchmakingOverlay(Font font) {
      super("autism-matchmaking", 280, 120);
      this.init_Font = font;
      this.init_DataUtil = new autismclient.gui.a.DataUtil(this, font);
      this.init_I_2 = 80;
      this.load_I = 36;
   }

   public void write_V_14() {
      this.load(false);
      this.init_V_Z(!this.init_Z);
      if (this.init_Z) {
         bk.init_bk().update_V(this);
      }
   }

   public void load(boolean v) {
      if (v != this.update_Z) {
         if (v) {
            this.flush_I = this.init_I_2;
            this.close_I = this.load_I;
            this.open = this.read_I;
            this.poll = this.write;
            this.load_Z = false;
         } else {
            this.init_I_2 = this.flush_I;
            this.load_I = this.close_I;
            this.read_I = this.open;
            this.write = this.poll;
         }

         this.update_Z = v;
      }
   }

   @Override
   public void init_V_Z(boolean v) {
      if (this.init_Z == v) {
         if (v) {
            this.init_DataUtil.init_V();
         }
      } else {
         if (!v) {
            this.init_DataUtil.load_V();
         }

         if (!v && this.update_Z) {
            this.init_I_2 = this.flush_I;
            this.load_I = this.close_I;
            this.read_I = this.open;
            this.write = this.poll;
            this.update_Z = false;
         }

         super.init_V_Z(v);
         if (v) {
            this.init_DataUtil.init_V();
         }
      }
   }

   public void update_V_12() {
      this.load(false);
      this.init_V_Z(true);
   }

   @Override
   public int init_I() {
      return this.init_DataUtil.read_I() + 4;
   }

   @Override
   public int load_I() {
      return Math.max(60, this.init_DataUtil.load_I() + 14 + 2);
   }

   @Override
   public boolean read_Z() {
      return !this.update_Z;
   }

   @Override
   public cX.Inner_DataUtil init_cX_DataUtil() {
      return cX.Inner_DataUtil.write;
   }

   @Override
   public boolean load_Z() {
      return !this.update_Z;
   }

   @Override
   public boolean write_Z() {
      return this.init_DataUtil.load_Z();
   }

   @Override
   public void push_V() {
      this.init_DataUtil.read_V();
   }

   @Override
   public Screen init_Screen() {
      return null;
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      if (this.init_Z || this.a__Z()) {
         if (this.update_Z) {
            this.load(ctx, mx, my, delta);
         } else {
            this.read_I = this.init_I_I_I(this.init_I(), this.init_I());
            if (!this.load_Z) {
               int want = this.init_DataUtil.init_I() + 14 + 2;
               int maxH = Math.min(cA.load_I() - 16, 360);
               int auto = Math.max(60, Math.min(want, maxH));
               this.write = this.init_DataUtil.init_Z() ? auto : this.load_I_I_I(auto, this.load_I());
            }

            cH bounds = this.init_cH_cX(this);
            this.init_I_2 = bounds.init_I;
            this.load_I = bounds.load_I;
            this.read_I = bounds.read;
            this.write = bounds.write;
            this.init_V_GuiGraphicsExtract_6(ctx, mx, my, this.init_cH(), "Matchmaking", this.load_Z, this.write_Z);
            if (!this.init_Z_Z(this.load_Z)) {
               boolean clipped = this.init_Z_GuiGraphicsExtract(ctx, this.init_cH(), this.load_Z);
               this.init_DataUtil.init_V_GuiGraphicsExtract(ctx, this.init_I_2 + 2, this.load_I + 14, this.read_I - 4, this.write - 14 - 2, mx, my, delta);
               this.init_V_GuiGraphicsExtract_Z(ctx, clipped);
            }
         }
      }
   }

   private void load(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      int sw = cA.init_I();
      int sh = cA.load_I();
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), -1073741824);
      int margin = 6;
      int pw = Math.max(1, Math.min(sw - margin * 2, this.init_DataUtil.read_I()));
      int px = (sw - pw) / 2;
      int py = 26;
      int ph = Math.max(1, Math.min(sh - py - 44, Math.max(this.load_I(), this.init_DataUtil.init_I())));
      this.init_I_2 = px;
      this.load_I = py;
      this.read_I = pw;
      this.write = ph;
      String title = "Matchmaking";
      ctx.text(this.init_Font, title, (sw - this.init_Font.width(title)) / 2, 9, -1, true);
      autismclient.gui.f.b.BufferHelper.init(
         autismclient.gui.f.UiHelper.init(ctx, this.init_Font, mx, my), autismclient.gui.f.NetHelper.init(px - 2, py - 2, pw + 4, ph + 4)
      );
      autismclient.gui.f.HashUtil.init_HashUtil().init(ctx, autismclient.gui.f.NetHelper.init(px, py, pw, ph));
      this.init_DataUtil.init_V_GuiGraphicsExtract(ctx, px, py, pw, ph, mx, my, delta);
      autismclient.gui.f.HashUtil.init_HashUtil().init(ctx);
   }

   @Override
   public boolean init_Z_D_3(double mx, double my, int button) {
      if (!this.init_Z) {
         return false;
      } else {
         cH bounds = this.init_cH();
         if (!this.update_Z && this.write(mx, my, bounds)) {
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
            return this.init_DataUtil.init_Z_I_2((int)Math.round(mx), (int)Math.round(my), button) ? true : this.init_Z_D_D(mx, my);
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
         return this.init_DataUtil.load((int)Math.round(mx), (int)Math.round(my), button);
      }
   }

   @Override
   public boolean init_Z_D(double mx, double my, int button, double dx, double dy) {
      if (this.write_Z) {
         cH c = this.init_cH_cX_cH(
            this, new cH((int)Math.round(mx - this.init_D), (int)Math.round(my - this.load_D), this.read_I, this.write, this.init_Z, this.load_Z)
         );
         this.init_I_2 = c.init_I;
         this.load_I = c.load_I;
         return true;
      } else {
         return this.load_Z ? false : this.init_DataUtil.init_Z_I((int)Math.round(mx), (int)Math.round(my), button, dx, dy);
      }
   }

   @Override
   public boolean init_Z_D_2(double mx, double my, double amount) {
      return this.init_Z && !this.load_Z && this.init_Z_D_D(mx, my) ? this.init_DataUtil.init_Z_I((int)Math.round(mx), (int)Math.round(my), amount) : false;
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      return this.init_Z && !this.load_Z && this.init_DataUtil.read(keyCode, scanCode, modifiers);
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      return this.init_Z && !this.load_Z && this.init_DataUtil.init_Z_C_I(chr, modifiers);
   }
}
