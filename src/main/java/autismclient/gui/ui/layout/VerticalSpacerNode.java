package autismclient.gui.ui.layout;

public class VerticalSpacerNode extends LayoutNode {
   private float sync = 48.0F;

   public VerticalSpacerNode init(float preferredHeight) {
      float next = Math.max(0.0F, preferredHeight);
      if (Float.compare(this.sync, next) != 0) {
         this.sync = next;
         this.markLayoutDirty();
      }

      return this;
   }

   @Override
   public float measureHeight(LayoutContext context, float availableWidth) {
      return context.theme().init_F_F(this.sync);
   }
}
