package autismclient.gui.ui.layout;

public class FixedSpacerNode extends LayoutNode {
   public FixedSpacerNode(float width, float height) {
      this.read_F = Math.max(0.0F, width);
      this.write = Math.max(0.0F, height);
   }

   @Override
   public float init_F_SyncManager(LayoutContext context) {
      return context.init_CharEncoder().init_F_F(this.read_F);
   }

   @Override
   public float init_F_SyncManager_F(LayoutContext context, float availableWidth) {
      return context.init_CharEncoder().init_F_F(this.write);
   }
}
