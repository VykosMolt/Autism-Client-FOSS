package autismclient.util;

import autismclient.gui.f.b.j;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class AutismSharePickerOverlay extends bj {
   private static final int flush_I = 4;
   private static final int close_I = 4;
   private static final int open = 140;
   private static final int poll = 320;
   private static final int push = -14249;
   private static final int pop = -855310;
   private static final int tick = -6645094;
   private static final int run = 872415231;
   private final Font init_Font;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.f.b.BitSetUtil init_BitSetUtil = new autismclient.gui.f.b.BitSetUtil();
   private String init_Str_2 = "Share";
   private Function<String, List<AutismSharePickerOverlay.Inner_DataUtil>> init_Function = s -> List.of();
   private final List<AutismSharePickerOverlay.Inner_DataUtil> init_List = new ArrayList<>();
   private int get;
   private long init_J = -9223372036854775808L;
   private int set;
   private boolean write_Z;
   private boolean update_Z;
   private int put;
   private double init_D;
   private double load_D;

   public AutismSharePickerOverlay(Font font) {
      super("autism-share-picker", 280, 260);
      this.init_Font = font;
      this.init_I_2 = 140;
      this.load_I = 44;
      this.init_BitSetUtil.init_BitSetUtil_I(48).init_BitSetUtil_Consumer(t -> {
         this.write_V_25();
         this.set = 0;
      });
   }

   public void init_V_Str_11(String title, String placeholder, Function<String, List<AutismSharePickerOverlay.Inner_DataUtil>> builder) {
      this.init_Str_2 = title;
      this.init_Function = builder;
      this.init_BitSetUtil.load_BitSetUtil_Str(placeholder).init_BitSetUtil_Str("");
      this.write_V_25();
      this.set = 0;
      bk.init_bk().init_V(this);
      this.init_V_Z(true);
      this.init_V_cH(new cH(this.init_I_2, this.load_I, this.init(this.init_List), this.load(this.init_List.size()), true, false));
      bk.init_bk().update_V(this);
      this.init_BitSetUtil.init_V_Z(true);
   }

   private void write_V_25() {
      this.init_List.clear();
      this.init_List.addAll(this.init_Function.apply(this.init_BitSetUtil.init_Str()));
      this.get = this.init_List.size() * this.push_I_25();
      this.init_J = aN.init_aN().init_J();
      this.set = Math.max(0, Math.min(this.set, this.add_I_18()));
   }

   @Override
   public int init_I() {
      return 140;
   }

   @Override
   public int load_I() {
      return this.run_I_23() - this.load_I + this.push_I_25() * 3 + 4;
   }

   @Override
   public cX.Inner_DataUtil init_cX_DataUtil() {
      return cX.Inner_DataUtil.write;
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public boolean write_Z() {
      return this.init_BitSetUtil.load_Z();
   }

   @Override
   public void push_V() {
      this.init_BitSetUtil.init_V_Z(false);
   }

   private int push_I_25() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private int pop_I_25() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int tick_I_23() {
      return this.load_I + 14 + 4;
   }

   private int run_I_23() {
      return this.tick_I_23() + this.pop_I_25() + 2;
   }

   private int get_I_21() {
      return Math.max(this.push_I_25(), this.load_I + this.write - 4 - this.run_I_23());
   }

   private int set_I_21() {
      return this.init_I_2 + 2;
   }

   private int put() {
      return this.init_I_2 + this.read_I - 6 - 2;
   }

   private int init(int y) {
      return j.init(y, this.push_I_25(), 9, 0);
   }

   private int init(List<AutismSharePickerOverlay.Inner_DataUtil> all) {
      int textW = this.init_Font.width(this.init_Str_2) + 40;
      int widest = 0;
      int widestSub = 0;

      for (AutismSharePickerOverlay.Inner_DataUtil r : all) {
         widest = Math.max(widest, this.init_Font.width(r.init_Str() == null ? "" : r.init_Str()) + (r.init_Boolean() != null ? 14 : 0));
         if (r.load() != null && !r.load().isEmpty()) {
            widestSub = Math.max(widestSub, this.init_Font.width(r.load()));
         }
      }

      int rowW = 4 + widest + (widestSub > 0 ? 8 + widestSub : 0) + 6 + 4;
      return Math.max(140, Math.min(320, Math.max(textW, rowW)));
   }

   private int load(int rowCount) {
      int chrome = 18 + this.pop_I_25() + 2 + 4;
      int screenH = Minecraft.getInstance().getWindow() != null ? cA.load_I() : 360;
      int maxRows = Math.max(4, ((int)(screenH * 0.6F) - chrome) / this.push_I_25());
      return chrome + this.push_I_25() * Math.max(4, Math.min(maxRows, rowCount));
   }

   private int add_I_18() {
      return Math.max(0, this.get - this.get_I_21());
   }

   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil_8() {
      return autismclient.gui.f.b.PoolEntry.init_PoolEntry_DataUtil_I_2(
         this.get, this.get_I_21(), this.init_I_2 + this.read_I - 5, this.run_I_23(), 3, this.get_I_21(), this.set
      );
   }

   private autismclient.gui.f.c.SyncManager init_SyncManager_GuiGraphicsExtract_3(GuiGraphicsExtractor g, double mx, double my, float delta) {
      return new autismclient.gui.f.c.SyncManager(
         g, this.init_Font, autismclient.gui.f.c.RingBuffer.init_RingBuffer_F(1.0F), this.init_CharEncoder, (int)Math.round(mx), (int)Math.round(my), delta
      );
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      if (this.init_Z || this.a__Z()) {
         if (this.init_J != aN.init_aN().init_J()) {
            this.write_V_25();
         }

         cH bounds = this.init_cH_cX(this);
         this.init_I_2 = bounds.init_I;
         this.load_I = bounds.load_I;
         this.read_I = bounds.read;
         this.write = bounds.write;
         this.init_V_GuiGraphicsExtract_6(ctx, mx, my, this.init_cH(), this.init_Str_2, this.load_Z, this.write_Z);
         if (!this.init_Z_Z(this.load_Z)) {
            boolean clipped = this.init_Z_GuiGraphicsExtract(ctx, this.init_cH(), this.load_Z);
            this.init_BitSetUtil.init_TableIndex_F(this.init_I_2 + 4, this.tick_I_23(), this.read_I - 8, this.pop_I_25());
            this.init_BitSetUtil.init_V_SyncManager(this.init_SyncManager_GuiGraphicsExtract_3(ctx, mx, my, delta));
            this.set = Math.max(0, Math.min(this.set, this.add_I_18()));
            int lt = this.run_I_23();
            int areaH = this.get_I_21();
            autismclient.gui.f.HashUtil.init_HashUtil()
               .init(ctx, autismclient.gui.f.NetHelper.init(this.init_I_2 + 1, lt, Math.max(0, this.read_I - 2), areaH));
            int y = lt - this.set;

            for (int i = 0; i < this.init_List.size(); i++) {
               AutismSharePickerOverlay.Inner_DataUtil r = this.init_List.get(i);
               if (y + this.push_I_25() > lt && y < lt + areaH) {
                  if (r.init_Boolean() != null) {
                     autismclient.gui.f.NetHelper rowBounds = autismclient.gui.f.NetHelper.init(
                        this.set_I_21(), y, Math.max(1, this.put() - this.set_I_21()), this.push_I_25()
                     );
                     autismclient.gui.f.b.TaskQueue.load(
                        autismclient.gui.f.UiHelper.init(ctx, this.init_Font, mx, my), rowBounds, r.init_Str(), r.init_Boolean(), rowBounds.init_Z(mx, my)
                     );
                  } else if (r.init_Z()) {
                     ctx.text(this.init_Font, r.init_Str(), this.init_I_2 + 4, this.init(y), -14249, false);
                  } else {
                     boolean hover = mx >= this.set_I_21() && mx < this.put() && my >= y && my < y + this.push_I_25() && my >= lt && my < lt + areaH;
                     if (hover) {
                        autismclient.gui.f.PacketUtil.init(
                           ctx, autismclient.gui.f.NetHelper.init(this.set_I_21(), y, Math.max(0, this.put() - this.set_I_21()), this.push_I_25()), 872415231
                        );
                     }

                     ctx.text(this.init_Font, r.init_Str(), this.init_I_2 + 4, this.init(y), -855310, false);
                     if (r.load() != null && !r.load().isEmpty()) {
                        int sw = this.init_Font.width(r.load());
                        ctx.text(this.init_Font, r.load(), this.init_I_2 + this.read_I - 4 - 6 - sw, this.init(y), -6645094, false);
                     }
                  }
               }

               y += this.push_I_25();
            }

            autismclient.gui.f.HashUtil.init_HashUtil().init(ctx);
            autismclient.gui.f.b.PoolEntry.Inner_DataUtil sb = this.init_PoolEntry_DataUtil_8();
            autismclient.gui.f.b.PoolEntry.init(ctx, sb, sb.init(mx, my), this.update_Z);
            this.init_V_GuiGraphicsExtract_Z(ctx, clipped);
         }
      }
   }

   @Override
   public boolean init_Z_D_3(double mx, double my, int button) {
      if (!this.init_Z) {
         return false;
      }

      cH bounds = this.init_cH();
      if (this.write(mx, my, bounds)) {
         this.init_V_Z(false);
         this.write_Z = false;
         return true;
      }

      autismclient.gui.f.b.PoolEntry.Inner_DataUtil sb = this.init_PoolEntry_DataUtil_8();
      if (button == 0 && sb.load(mx, my)) {
         this.update_Z = true;
         this.put = (int)Math.round(my) - sb.update();
         return true;
      }

      if (button == 0 && this.load_Z_D_D(mx, my)) {
         this.write_Z = true;
         this.init_D = mx - this.init_I_2;
         this.load_D = my - this.load_I;
         return true;
      }

      if (this.init_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager_GuiGraphicsExtract_3(null, mx, my, 0.0F), (float)mx, (float)my, button)) {
         return true;
      }

      if (button == 0) {
         int lt = this.run_I_23();
         int areaH = this.get_I_21();
         int y = lt - this.set;

         for (int i = 0; i < this.init_List.size(); i++) {
            int ry = y;
            y += this.push_I_25();
            if (ry + this.push_I_25() > lt
               && ry < lt + areaH
               && mx >= this.set_I_21()
               && mx < this.put()
               && my >= Math.max(lt, ry)
               && my < Math.min(lt + areaH, ry + this.push_I_25())) {
               AutismSharePickerOverlay.Inner_DataUtil r = this.init_List.get(i);
               if (r.init_Runnable() != null) {
                  try {
                     r.init_Runnable().run();
                  } catch (Throwable var15) {
                  }

                  if (r.init_Z()) {
                     this.write_V_25();
                  } else {
                     this.init_V_Z(false);
                  }

                  return true;
               }
            }
         }
      }

      return this.init_Z_D_D(mx, my);
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
         return this.init_BitSetUtil.load_Z_SyncManager(this.init_SyncManager_GuiGraphicsExtract_3(null, mx, my, 0.0F), (float)mx, (float)my, button);
      }
   }

   @Override
   public boolean init_Z_D(double mx, double my, int button, double dx, double dy) {
      if (this.update_Z) {
         this.set = autismclient.gui.f.b.PoolEntry.init(this.init_PoolEntry_DataUtil_8(), my, this.put);
         return true;
      } else if (this.write_Z) {
         cH c = this.init_cH_cX_cH(
            this, new cH((int)Math.round(mx - this.init_D), (int)Math.round(my - this.load_D), this.read_I, this.write, this.init_Z, this.load_Z)
         );
         this.init_I_2 = c.init_I;
         this.load_I = c.load_I;
         return true;
      } else {
         return this.init_BitSetUtil
            .init_Z_SyncManager(this.init_SyncManager_GuiGraphicsExtract_3(null, mx, my, 0.0F), (float)mx, (float)my, button, (float)dx, (float)dy);
      }
   }

   @Override
   public boolean init_Z_D_2(double mx, double my, double amount) {
      if (this.init_Z && !this.load_Z && this.init_Z_D_D(mx, my)) {
         this.set = Math.max(0, Math.min(this.add_I_18(), this.set - (int)Math.signum(amount) * this.push_I_25() * 2));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (!this.init_Z) {
         return false;
      } else if (keyCode == 256 && !this.init_BitSetUtil.load_Z()) {
         this.init_V_Z(false);
         return true;
      } else {
         return this.init_BitSetUtil.init_Z_SyncManager_3(this.init_SyncManager_GuiGraphicsExtract_3(null, 0.0, 0.0, 0.0F), keyCode, scanCode, modifiers);
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      return !this.init_Z ? false : this.init_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager_GuiGraphicsExtract_3(null, 0.0, 0.0, 0.0F), chr, modifiers);
   }

   public record Inner_DataUtil(boolean init_Z, String init_Str, String load, Runnable init_Runnable, Boolean init_Boolean) {
      public Inner_DataUtil(boolean header, String text, String sub, Runnable action) {
         this(header, text, sub, action, null);
      }

      public static AutismSharePickerOverlay.Inner_DataUtil init(cY.Inner_DataUtil row, Runnable action) {
         return new AutismSharePickerOverlay.Inner_DataUtil(true, row.init_Str(), null, action, row.load_Z());
      }

      public static AutismSharePickerOverlay.Inner_DataUtil init(String text) {
         return new AutismSharePickerOverlay.Inner_DataUtil(true, text, null, null);
      }

      public static AutismSharePickerOverlay.Inner_DataUtil init(String text, String sub, Runnable action) {
         return new AutismSharePickerOverlay.Inner_DataUtil(false, text, sub, action);
      }
   }
}
