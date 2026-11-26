package autismclient.gui.ui.layout;

public class StretchRowLayout extends LayoutContainer {
   private EdgeInsets init_IoWorker;
   private int init_I;

   public StretchRowLayout() {
      this.init_IoWorker = EdgeInsets.init_IoWorker;
      this.init_I = 0;
   }

   public StretchRowLayout init(EdgeInsets padding) {
      EdgeInsets next = padding == null ? EdgeInsets.init_IoWorker : padding;
      if (!this.init_IoWorker.equals(next)) {
         this.init_IoWorker = next;
         this.load_V();
      }

      return this;
   }

   public StretchRowLayout init(int gap) {
      int next = Math.max(0, gap);
      if (this.init_I != next) {
         this.init_I = next;
         this.load_V();
      }

      return this;
   }

   @Override
   public float init_F_SyncManager_F(LayoutContext context, float availableWidth) {
      EdgeInsets scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      float tallest = 0.0F;
      float innerWidth = Math.max(0.0F, availableWidth - scaledPadding.init_I());

      for (LayoutNode child : this.init_List) {
         if (child != null && child.write_Z()) {
            tallest = Math.max(tallest, child.init_F_SyncManager_F(context, innerWidth));
         }
      }

      return scaledPadding.write() + tallest + scaledPadding.sync();
   }

   @Override
   protected void load_V(LayoutContext context) {
      EdgeInsets scaledPadding = context.init_CharEncoder().init_IoWorker_IoWorker(this.init_IoWorker);
      int scaledGap = context.init_CharEncoder().load(this.init_I);
      float innerX = this.init_F + scaledPadding.read();
      float innerY = this.load_F + scaledPadding.write();
      float innerWidth = Math.max(0.0F, this.read_F - scaledPadding.init_I());
      float innerHeight = Math.max(0.0F, this.write - scaledPadding.load());
      float fixedWidth = 0.0F;
      int growCount = 0;
      int visibleCount = 0;

      for (LayoutNode child : this.init_List) {
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

      for (LayoutNode child : this.init_List) {
         if (child != null && child.write_Z()) {
            float childWidth = child.sync_Z() ? growWidth : child.init_F_SyncManager(context);
            float childHeight = Math.max(innerHeight, child.init_F_SyncManager_F(context, childWidth));
            child.init_TableIndex_F(cursorX, innerY, childWidth, childHeight);
            cursorX += childWidth + scaledGap;
         }
      }
   }
}
