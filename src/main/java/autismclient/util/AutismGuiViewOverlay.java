package autismclient.util;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class AutismGuiViewOverlay extends bj {
   private static final int flush_I = 18;
   private static final int close_I = 4;
   private static final int open = 6;
   private static final int poll = 12;
   private static final int push = 6;
   private static final int pop = 4;
   private static final int tick = -3750202;
   private static final int run = -7631989;
   private static final int get = -13158601;
   private static final int set = -1;
   private static final int put = -2130706433;
   private final Font init_Font;
   private Component init_Component = Component.literal("GUI");
   private List<autismclient.util.mm.ConfigStore.Inner_BufferHelper> init_List = List.of();
   private int add;
   private int clear;
   private int reset;
   private boolean write_Z;
   private boolean update_Z;
   private int start;
   private double init_D;
   private double load_D;

   public AutismGuiViewOverlay(Font font) {
      super("autism-gui-view", 180, 180);
      this.init_Font = font;
      this.init_I_2 = 120;
      this.load_I = 50;
   }

   public boolean init(autismclient.util.mm.b.DataUtil.Inner_DataUtil blob) {
      autismclient.util.mm.ConfigStore.Inner_StreamUtil snap = autismclient.util.mm.ConfigStore.init_ConfigStore_Stream(blob);
      if (snap == null) {
         bi.init("Could not read shared GUI.", -42149);
         return false;
      }

      this.init_Component = snap.init_Component() == null ? Component.literal("GUI") : snap.init_Component();
      this.init_List = snap.init_List();
      this.add = Math.max(18, snap.init_I());
      this.clear = Math.max(18, snap.load());
      this.reset = 0;
      bk.init_bk().init_V(this);
      this.init_V_Z(true);
      int wantW = this.add + 2 + 8;
      int wantH = 32 + this.clear + 2 + 4;
      if (wantH > cA.load_I() - 8) {
         wantW += 6;
      }

      this.init_V_cH(new cH(this.init_I_2, this.load_I, wantW, wantH, true, false));
      bk.init_bk().update_V(this);
      return true;
   }

   @Override
   public int init_I() {
      return 62;
   }

   @Override
   public int load_I() {
      return 54;
   }

   @Override
   public cX.Inner_DataUtil init_cX_DataUtil() {
      return cX.Inner_DataUtil.write;
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   private int push_I_9() {
      return this.init_I_2 + 4 + 1;
   }

   private int pop_I_9() {
      return this.load_I + 14 + 12 + 6;
   }

   private int tick_I_7() {
      return Math.max(18, this.load_I + this.write - 4 - this.pop_I_9());
   }

   private int run_I_7() {
      return Math.max(0, this.clear + 2 - this.tick_I_7());
   }

   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil_3() {
      return autismclient.gui.f.b.PoolEntry.init_PoolEntry_DataUtil_I_2(
         this.clear + 2, this.tick_I_7(), this.init_I_2 + this.read_I - 5, this.pop_I_9(), 3, this.tick_I_7(), this.reset
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
         this.init_V_GuiGraphicsExtract_6(ctx, mx, my, this.init_cH(), "Shared GUI", this.load_Z, this.write_Z);
         if (!this.init_Z_Z(this.load_Z)) {
            boolean clipped = this.init_Z_GuiGraphicsExtract(ctx, this.init_cH(), this.load_Z);
            ctx.text(this.init_Font, this.init_Component.getVisualOrderText(), this.init_I_2 + 4, this.load_I + 14 + 3, -855310, false);
            this.reset = Math.max(0, Math.min(this.reset, this.run_I_7()));
            int gl = this.push_I_9();
            int gt = this.pop_I_9() - this.reset;
            autismclient.gui.f.NetHelper gridArea = autismclient.gui.f.NetHelper.init(
               this.init_I_2 + 4 - 1, this.pop_I_9() - 4, this.add + 4, this.tick_I_7() + 4
            );
            autismclient.gui.f.PacketUtil.init(ctx, gridArea, -3750202);
            autismclient.gui.f.HashUtil.init_HashUtil().init(ctx, gridArea);
            ItemStack hoveredStack = ItemStack.EMPTY;

            for (autismclient.util.mm.ConfigStore.Inner_BufferHelper slot : this.init_List) {
               int itemX = gl + slot.init_I();
               int itemY = gt + slot.load();
               if (itemY + 18 >= this.pop_I_9() && itemY <= this.pop_I_9() + this.tick_I_7()) {
                  this.init_V_GuiGraphicsExtract_2(ctx, itemX - 1, itemY - 1);
                  ItemStack st = slot.init_ItemStack();
                  if (st != null && !st.isEmpty()) {
                     try {
                        ctx.item(st, itemX, itemY);
                        ctx.itemDecorations(this.init_Font, st, itemX, itemY);
                     } catch (Throwable var17) {
                     }
                  }

                  boolean hover = mx >= itemX
                     && mx < itemX + 16
                     && my >= itemY
                     && my < itemY + 16
                     && my >= this.pop_I_9()
                     && my <= this.pop_I_9() + this.tick_I_7();
                  if (hover) {
                     autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(itemX, itemY, 16, 16), -2130706433);
                     if (st != null && !st.isEmpty()) {
                        hoveredStack = st;
                     }
                  }
               }
            }

            autismclient.gui.f.HashUtil.init_HashUtil().init(ctx);
            autismclient.gui.f.b.PoolEntry.Inner_DataUtil sb = this.init_PoolEntry_DataUtil_3();
            autismclient.gui.f.b.PoolEntry.init(ctx, sb, sb.init(mx, my), this.update_Z);
            this.init_V_GuiGraphicsExtract_Z(ctx, clipped);
            if (!hoveredStack.isEmpty()) {
               this.init_V_GuiGraphicsExtract_2(ctx, hoveredStack, mx, my);
            }
         }
      }
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, ItemStack stack, int mx, int my) {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc == null) {
            return;
         }

         List<Component> lines = Screen.getTooltipFromItem(mc, stack);
         if (lines == null || lines.isEmpty()) {
            return;
         }

         int w = 0;

         for (Component c : lines) {
            w = Math.max(w, this.init_Font.width(c));
         }

         int h = lines.size() == 1 ? 8 : lines.size() * 10 - 2;
         int sw = cA.init_I();
         int sh = cA.load_I();
         int x = mx + 12;
         int y = my - 12;
         if (x + w + 4 > sw) {
            x = Math.max(4, mx - w - 16);
         }

         if (y + h + 4 > sh) {
            y = sh - h - 4;
         }

         if (y < 4) {
            y = 4;
         }

         ctx.nextStratum();
         autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x - 3, y - 3, w + 6, h + 6), -267386864);
         autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(x - 3, y - 3, w + 6, h + 6), 0, 1347420320);
         int ty = y;

         for (Component c : lines) {
            ctx.text(this.init_Font, c.getVisualOrderText(), x, ty, -1, true);
            ty += 10;
         }
      } catch (Throwable var16) {
      }
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor ctx, int cx, int cy) {
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(cx, cy, 18, 18), -7631989);
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(cx, cy, 18, 1), -13158601);
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(cx, cy, 1, 18), -13158601);
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(cx, cy + 18 - 1, 18, 1), -1);
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(cx + 18 - 1, cy, 1, 18), -1);
   }

   private ItemStack init_ItemStack_D_D(double mx, double my) {
      if (!(my < this.pop_I_9()) && !(my > this.pop_I_9() + this.tick_I_7())) {
         int gl = this.push_I_9();
         int gt = this.pop_I_9() - this.reset;

         for (autismclient.util.mm.ConfigStore.Inner_BufferHelper slot : this.init_List) {
            int itemX = gl + slot.init_I();
            int itemY = gt + slot.load();
            if (mx >= itemX && mx < itemX + 16 && my >= itemY && my < itemY + 16) {
               return slot.init_ItemStack() == null ? ItemStack.EMPTY : slot.init_ItemStack();
            }
         }

         return ItemStack.EMPTY;
      } else {
         return ItemStack.EMPTY;
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

      autismclient.gui.f.b.PoolEntry.Inner_DataUtil sb = this.init_PoolEntry_DataUtil_3();
      if (button == 0 && sb.load(mx, my)) {
         this.update_Z = true;
         this.start = (int)Math.round(my) - sb.update();
         return true;
      }

      if (button == 0 && this.load_Z_D_D(mx, my)) {
         this.write_Z = true;
         this.init_D = mx - this.init_I_2;
         this.load_D = my - this.load_I;
         return true;
      }

      if (this.load_Z) {
         return false;
      }

      ItemStack st = this.init_ItemStack_D_D(mx, my);
      if (st != null && !st.isEmpty()) {
         AutismItemNbtInspectOverlay ov = AutismItemNbtInspectOverlay.init_AutismItemNbtInspe_Font(this.init_Font);
         if (ov != null) {
            ov.init_V_ItemStack(st, (int)mx + 8, (int)my);
            bk.init_bk().init_V_cX_cX_DataUtil(ov, cX.Inner_DataUtil.write);
            bk.init_bk().update_V(ov);
         }

         return true;
      } else {
         return this.init_Z_D_D(mx, my);
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
         this.reset = autismclient.gui.f.b.PoolEntry.init(this.init_PoolEntry_DataUtil_3(), my, this.start);
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
         this.reset = Math.max(0, Math.min(this.run_I_7(), this.reset - (int)Math.signum(amount) * 18));
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
}
