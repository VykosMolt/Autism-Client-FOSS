package autismclient.util;

import autismclient.gui.f.b.j;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class AutismFilterViewOverlay extends bj {
   private static final int flush_I = 12;
   private static final int close_I = 12;
   private static final int open = 4;
   private static final int poll = 220;
   private static final int push = -14249;
   private static final int pop = -7412064;
   private static final int tick = -7429889;
   private static final int run = -6645094;
   private final Font init_Font;
   private String init_Str_2 = "Packet Filter";
   private final List<AutismFilterViewOverlay.Inner_DataUtil> init_List = new ArrayList<>();
   private int get;
   private int set;
   private boolean write_Z;
   private boolean update_Z;
   private int put;
   private double init_D;
   private double load_D;

   public AutismFilterViewOverlay(Font font) {
      super("autism-filter-view", 220, 200);
      this.init_Font = font;
      this.init_I_2 = 130;
      this.load_I = 48;
   }

   public boolean init(autismclient.util.mm.b.DataUtil.Inner_DataUtil blob) {
      autismclient.util.mm.ConfigStore.Inner_DataUtil fv = autismclient.util.mm.ConfigStore.init_ConfigStore_DataUt(blob);
      if (fv == null) {
         bi.init("Could not read shared filter.", -42149);
         return false;
      }

      this.init_List.clear();
      this.init_List.add(new AutismFilterViewOverlay.Inner_DataUtil("Client → Server  (" + fv.init().size() + ")", -14249));
      if (fv.init().isEmpty()) {
         this.init_List.add(new AutismFilterViewOverlay.Inner_DataUtil("   (none)", -6645094));
      } else {
         for (String n : fv.init()) {
            this.init_List.add(new AutismFilterViewOverlay.Inner_DataUtil("   " + n, -7412064));
         }
      }

      this.init_List.add(new AutismFilterViewOverlay.Inner_DataUtil("", -6645094));
      this.init_List.add(new AutismFilterViewOverlay.Inner_DataUtil("Server → Client  (" + fv.load().size() + ")", -14249));
      if (fv.load().isEmpty()) {
         this.init_List.add(new AutismFilterViewOverlay.Inner_DataUtil("   (none)", -6645094));
      } else {
         for (String n : fv.load()) {
            this.init_List.add(new AutismFilterViewOverlay.Inner_DataUtil("   " + n, -7429889));
         }
      }

      this.init_Str_2 = "Packet Filter  (" + (fv.init().size() + fv.load().size()) + " types)";
      this.get = this.init_List.size() * 12;
      this.set = 0;
      bk.init_bk().init_V(this);
      this.init_V_Z(true);
      int wantW = Math.max(this.init_I(), this.push_I_8() + 8 + 6);
      int wantH = 30 + Math.min(this.get, 220) + 4;
      this.init_V_cH(new cH(this.init_I_2, this.load_I, wantW, wantH, true, false));
      bk.init_bk().update_V(this);
      return true;
   }

   private int push_I_8() {
      int w = this.init_Font.width(this.init_Str_2);

      for (AutismFilterViewOverlay.Inner_DataUtil l : this.init_List) {
         w = Math.max(w, this.init_Font.width(l.init_Str()));
      }

      return w;
   }

   @Override
   public int init_I() {
      return 170;
   }

   @Override
   public int load_I() {
      return 70;
   }

   @Override
   public cX.Inner_DataUtil init_cX_DataUtil() {
      return cX.Inner_DataUtil.write;
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   private int pop_I_8() {
      return this.load_I + 14 + 12 + 4;
   }

   private int tick_I_6() {
      return Math.max(12, this.load_I + this.write - 4 - this.pop_I_8());
   }

   private int run_I_6() {
      return Math.max(0, this.get - this.tick_I_6());
   }

   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil_2() {
      return autismclient.gui.f.b.PoolEntry.init_PoolEntry_DataUtil_I_2(
         this.get, this.tick_I_6(), this.init_I_2 + this.read_I - 5, this.pop_I_8(), 3, this.tick_I_6(), this.set
      );
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      if (this.init_Z || this.a__Z()) {
         cH bounds = this.init_cH_cX(this);
         this.init_I_2 = bounds.init_I;
         this.load_I = bounds.load_I;
         this.read_I = bounds.read;
         this.write = bounds.write;
         this.init_V_GuiGraphicsExtract_6(ctx, mx, my, this.init_cH(), "Shared Filter", this.load_Z, this.write_Z);
         if (!this.init_Z_Z(this.load_Z)) {
            boolean clipped = this.init_Z_GuiGraphicsExtract(ctx, this.init_cH(), this.load_Z);
            ctx.text(this.init_Font, this.init_Str_2, this.init_I_2 + 4, j.init(this.load_I + 14 + 2, 12, 9, 0), -855310, false);
            this.set = Math.max(0, Math.min(this.set, this.run_I_6()));
            int lt = this.pop_I_8();
            autismclient.gui.f.HashUtil.init_HashUtil()
               .init(ctx, autismclient.gui.f.NetHelper.init(this.init_I_2 + 1, lt, Math.max(0, this.read_I - 2), this.tick_I_6()));
            int y = lt - this.set;

            for (AutismFilterViewOverlay.Inner_DataUtil l : this.init_List) {
               if (y + 12 > lt && y < lt + this.tick_I_6() && !l.init_Str().isEmpty()) {
                  ctx.text(this.init_Font, l.init_Str(), this.init_I_2 + 4, j.init(y, 12, 9, 0), l.init_I(), false);
               }

               y += 12;
            }

            autismclient.gui.f.HashUtil.init_HashUtil().init(ctx);
            autismclient.gui.f.b.PoolEntry.Inner_DataUtil sb = this.init_PoolEntry_DataUtil_2();
            autismclient.gui.f.b.PoolEntry.init(ctx, sb, sb.init(mx, my), this.update_Z);
            this.init_V_GuiGraphicsExtract_Z(ctx, clipped);
         }
      }
   }

   @Override
   public boolean init_Z_D_3(double mx, double my, int button) {
      if (!this.init_Z) {
         return false;
      } else {
         cH bounds = this.init_cH();
         if (this.write(mx, my, bounds)) {
            this.init_V_Z(false);
            this.write_Z = false;
            return true;
         } else {
            autismclient.gui.f.b.PoolEntry.Inner_DataUtil sb = this.init_PoolEntry_DataUtil_2();
            if (button == 0 && sb.load(mx, my)) {
               this.update_Z = true;
               this.put = (int)Math.round(my) - sb.update();
               return true;
            } else if (button == 0 && this.load_Z_D_D(mx, my)) {
               this.write_Z = true;
               this.init_D = mx - this.init_I_2;
               this.load_D = my - this.load_I;
               return true;
            } else {
               return this.init_Z_D_D(mx, my);
            }
         }
      }
   }

   @Override
   public boolean load_Z_D_3(double mx, double my, int button) {
      if (this.update_Z) {
         this.update_Z = false;
         return true;
      } else if (this.write_Z) {
         this.write_Z = false;
         this.a__V();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_D(double mx, double my, int button, double dx, double dy) {
      if (this.update_Z) {
         this.set = autismclient.gui.f.b.PoolEntry.init(this.init_PoolEntry_DataUtil_2(), my, this.put);
         return true;
      } else if (this.write_Z) {
         cH c = this.init_cH_cX_cH(
            this, new cH((int)Math.round(mx - this.init_D), (int)Math.round(my - this.load_D), this.read_I, this.write, this.init_Z, this.load_Z)
         );
         this.init_I_2 = c.init_I;
         this.load_I = c.load_I;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_D_2(double mx, double my, double amount) {
      if (this.init_Z && !this.load_Z && this.init_Z_D_D(mx, my)) {
         this.set = Math.max(0, Math.min(this.run_I_6(), this.set - (int)Math.signum(amount) * 12 * 2));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      return false;
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      return false;
   }

   private record Inner_DataUtil(String init_Str, int init_I) {
   }
}
