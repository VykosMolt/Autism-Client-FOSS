package autismclient.gui.ui.layout;

public class StretchRowLayout extends LayoutContainer {
   private EdgeInsets padding;
   private int init_I;

   public StretchRowLayout() {
      this.padding = EdgeInsets.ZERO;
      this.init_I = 0;
   }

   public StretchRowLayout withPadding(EdgeInsets padding) {
      EdgeInsets next = padding == null ? EdgeInsets.ZERO : padding;
      if (!this.padding.equals(next)) {
         this.padding = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public StretchRowLayout withGap(int gap) {
      int next = Math.max(0, gap);
      if (this.init_I != next) {
         this.init_I = next;
         this.markLayoutDirty();
      }

      return this;
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.padding);
      float tallest = 0.0F;
      float innerWidth = Math.max(0.0F, availableWidth - scaledPadding.init_I());

      for (LayoutNode child : this.children) {
         if (child != null && child.isVisible()) {
            tallest = Math.max(tallest, child.measureHeight(context, innerWidth));
         }
      }

      return scaledPadding.write() + tallest + scaledPadding.bottom();
   }

   @Override
   protected void layoutChildren(LayoutContext context) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.padding);
      int scaledGap = context.theme().load(this.init_I);
      float innerX = this.nodeX + scaledPadding.read();
      float innerY = this.nodeY + scaledPadding.write();
      float innerWidth = Math.max(0.0F, this.nodeWidth - scaledPadding.init_I());
      float innerHeight = Math.max(0.0F, this.nodeHeight - scaledPadding.load());
      float fixedWidth = 0.0F;
      int growCount = 0;
      int visibleCount = 0;

      for (LayoutNode child : this.children) {
         if (child != null && child.isVisible()) {
            visibleCount++;
            if (child.isGrowX()) {
               growCount++;
            } else {
               fixedWidth += child.measurePreferredWidth(context);
            }
         }
      }

      float totalGap = Math.max(0, visibleCount - 1) * scaledGap;
      float freeWidth = Math.max(0.0F, innerWidth - fixedWidth - totalGap);
      float growWidth = growCount > 0 ? freeWidth / growCount : 0.0F;
      float cursorX = innerX;

      for (LayoutNode child : this.children) {
         if (child != null && child.isVisible()) {
            float childWidth = child.isGrowX() ? growWidth : child.measurePreferredWidth(context);
            float childHeight = Math.max(innerHeight, child.measureHeight(context, childWidth));
            child.setBounds(cursorX, innerY, childWidth, childHeight);
            cursorX += childWidth + scaledGap;
         }
      }
   }
}
