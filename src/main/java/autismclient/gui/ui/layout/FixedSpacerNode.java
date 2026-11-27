package autismclient.gui.ui.layout;

public class FixedSpacerNode extends LayoutNode {
   public FixedSpacerNode(float width, float height) {
      this.nodeWidth = Math.max(0.0F, width);
      this.nodeHeight = Math.max(0.0F, height);
   }

   @Override
   public float measurePreferredWidth(LayoutContext context) {
      return context.theme().init_F_F(this.nodeWidth);
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      return context.theme().init_F_F(this.nodeHeight);
   }
}
