package autismclient.gui.ui.layout;

public class ColumnLayout extends LayoutContainer {
   private EdgeInsets padding;
   private int spacing;

   public ColumnLayout() {
      this.padding = EdgeInsets.ZERO;
      this.spacing = 0;
   }

   public ColumnLayout withPadding(EdgeInsets padding) {
      EdgeInsets next = padding == null ? EdgeInsets.ZERO : padding;
      if (!this.padding.equals(next)) {
         this.padding = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public ColumnLayout withGap(int gap) {
      int next = Math.max(0, gap);
      if (this.spacing != next) {
         this.spacing = next;
         this.markLayoutDirty();
      }

      return this;
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.padding);
      int scaledGap = context.theme().load(this.spacing);
      float innerWidth = Math.max(0.0F, availableWidth - scaledPadding.init_I());
      float total = scaledPadding.write() + scaledPadding.bottom();
      boolean first = true;

      for (LayoutNode child : this.children) {
         if (child != null && child.isVisible()) {
            if (!first) {
               total += scaledGap;
            }

            total += child.measureHeight(context, innerWidth);
            first = false;
         }
      }

      return total;
   }

   @Override
   protected void layoutChildren(LayoutContext context) {
      EdgeInsets scaledPadding = context.theme().init_IoWorker_IoWorker(this.padding);
      int scaledGap = context.theme().load(this.spacing);
      float innerX = this.nodeX + scaledPadding.read();
      float cursorY = this.nodeY + scaledPadding.write();
      float innerWidth = Math.max(0.0F, this.nodeWidth - scaledPadding.init_I());

      for (LayoutNode child : this.children) {
         if (child != null && child.isVisible()) {
            float childHeight = child.measureHeight(context, innerWidth);
            child.setBounds(innerX, cursorY, innerWidth, childHeight);
            cursorY += childHeight + scaledGap;
         }
      }
   }
}
