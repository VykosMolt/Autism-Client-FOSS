package autismclient.gui.f.c;

import autismclient.gui.f.b.MethodUtil;
import autismclient.util.ct;

public class StreamUtil extends HashUtil {
   private IoWorker init_IoWorker;
   private int init_I;
   private boolean write_Z;
   private int load_I;
   private float sync;

   public StreamUtil() {
      this.init_IoWorker = IoWorker.init_IoWorker;
      this.init_I = 0;
      this.write_Z = false;
      this.load_I = -50373;
      this.sync = 0.0F;
   }

   public StreamUtil init(IoWorker padding) {
      IoWorker next = padding == null ? IoWorker.init_IoWorker : padding;
      if (!this.init_IoWorker.equals(next)) {
         this.init_IoWorker = next;
         this.load_V();
      }

      return this;
   }

   public StreamUtil init(int gap) {
      int next = Math.max(0, gap);
      if (this.init_I != next) {
         this.init_I = next;
         this.load_V();
      }

      return this;
   }

   public StreamUtil init_StreamUtil(boolean underlineOnHover) {
      this.write_Z = underlineOnHover;
      return this;
   }

   public StreamUtil load(int underlineColor) {
      this.load_I = underlineColor;
      return this;
   }

   @Override
   public float init_F_SyncManager(SyncManager context) {
      IoWorker scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      int scaledGap = context.init_CharEncoder().load(this.init_I);
      float total = scaledPadding.init_I();
      int visibleCount = 0;

      for (TableIndex child : this.init_List) {
         if (child != null && child.write_Z()) {
            total += child.init_F_SyncManager(context);
            visibleCount++;
         }
      }

      if (visibleCount > 1) {
         total += (visibleCount - 1) * scaledGap;
      }

      return total;
   }

   @Override
   public float init_F_SyncManager_F(SyncManager context, float availableWidth) {
      IoWorker scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      float tallest = 0.0F;

      for (TableIndex child : this.init_List) {
         if (child != null && child.write_Z()) {
            tallest = Math.max(tallest, child.init_F_SyncManager_F(context, availableWidth));
         }
      }

      return scaledPadding.write() + tallest + scaledPadding.sync();
   }

   @Override
   protected void load_V(SyncManager context) {
      IoWorker scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      int scaledGap = context.init_CharEncoder().load(this.init_I);
      float innerX = this.init_F + scaledPadding.read();
      float innerY = this.load_F + scaledPadding.write();
      float innerWidth = Math.max(0.0F, this.read_F - scaledPadding.init_I());
      float innerHeight = Math.max(0.0F, this.write - scaledPadding.load());
      float fixedWidth = 0.0F;
      int growCount = 0;
      int visibleCount = 0;

      for (TableIndex child : this.init_List) {
         if (child != null && child.write_Z()) {
            visibleCount++;
            if (child.sync_Z()) {
               growCount++;
            } else {
               fixedWidth += child.init_F_SyncManager(context);
            }
         }
      }

      float totalGap = Math.max(0, visibleCount - 1) * scaledGap;
      float freeWidth = Math.max(0.0F, innerWidth - fixedWidth - totalGap);
      float growWidth = growCount > 0 ? freeWidth / growCount : 0.0F;
      float cursorX = innerX;
      this.sync = 0.0F;

      for (TableIndex child : this.init_List) {
         if (child != null && child.write_Z()) {
            float childWidth = child.sync_Z() ? growWidth : child.init_F_SyncManager(context);
            float childHeight = Math.min(innerHeight, child.init_F_SyncManager_F(context, childWidth));
            float childY = innerY + Math.max(0.0F, (innerHeight - childHeight) / 2.0F);
            child.init_TableIndex_F(cursorX, childY, childWidth, childHeight);
            cursorX += childWidth + scaledGap;
         }
      }

      this.sync = Math.max(0.0F, cursorX - innerX - (visibleCount > 0 ? scaledGap : 0));
   }

   @Override
   public void init_V_SyncManager(SyncManager context) {
      if (this.init_Z) {
         super.init_V_SyncManager(context);
         this.write(context);
         if (this.write_Z) {
            float hover = this.init_F_Z_F(this.init_Z_F_F(context.init_F(), context.load()), context.read());
            if (hover <= 0.0F || context.init_GuiGraphicsExtract() == null) {
               return;
            }

            IoWorker scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
            int drawX = Math.round(this.init_F + scaledPadding.read());
            int drawY = Math.round(this.load_F + this.write - context.init_CharEncoder().load(1));
            float underlineAreaWidth = Math.max(this.sync, this.read_F - scaledPadding.init_I());
            int underlineWidth = Math.max(1, Math.round(underlineAreaWidth * hover));
            int underlineX = drawX + Math.max(0, Math.round((underlineAreaWidth - underlineWidth) / 2.0F));
            context.init_GuiGraphicsExtract()
               .fill(
                  underlineX,
                  drawY,
                  underlineX + underlineWidth,
                  drawY + context.init_CharEncoder().load(1),
                  ct.init_I_I_ct_DataUtil(this.load_I, ct.Inner_DataUtil.init)
               );
         }
      }
   }

   private void write(SyncManager context) {
      if (context.init_GuiGraphicsExtract() != null) {
         autismclient.gui.f.SyncManager ui = null;
         TableIndex previous = null;

         for (TableIndex child : this.init_List) {
            if (child != null && child.write_Z()) {
               if (previous instanceof ByteReader left
                  && child instanceof ByteReader right
                  && left.init_Z_2()
                  && right.init_Z_2()
                  && Math.abs(left.load_F() + left.write_F() - right.load_F()) <= 0.5F) {
                  if (ui == null) {
                     ui = autismclient.gui.f.UiHelper.init(
                        context.init_GuiGraphicsExtract(), context.init_Font(), Math.round(context.init_F()), Math.round(context.load())
                     );
                  }

                  int leftColor = left.init_I_SyncManager(ui);
                  int rightColor = right.init_I_SyncManager(ui);
                  float leftWeight = left.init_F();
                  float rightWeight = right.init_F();
                  int seamColor = MethodUtil.init(leftColor, leftWeight, rightColor, rightWeight);
                  int seamX = Math.round(right.load_F());
                  int seamY = Math.round(Math.max(left.read_F(), right.read_F()));
                  int seamBottom = Math.round(Math.min(left.read_F() + left.update_F(), right.read_F() + right.update_F()));
                  MethodUtil.init(ui, seamX, seamY, Math.max(0, seamBottom - seamY), seamColor);
               }

               previous = child;
            }
         }
      }
   }
}
