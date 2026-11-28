package autismclient.gui.ui;

import java.util.function.LongSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import autismclient.gui.f.NetHelper;

public final class VisibilityTransition {
   private static final float init_F = 0.8F;
   private final EaseOutAnimator animator;
   private boolean init_Z;

   public VisibilityTransition(boolean visible) {
      this.animator = new EaseOutAnimator(visible ? 1.0F : 0.0F, 150);
   }

   VisibilityTransition(boolean visible, LongSupplier clock) {
      this.animator = new EaseOutAnimator(visible ? 1.0F : 0.0F, 150, clock);
   }

   public void animateTo(boolean visible) {
      if (this.init_Z) {
         this.init_Z = false;
         if (visible) {
            this.animator.snapTo(0.0F);
         }
      }

      this.animator.animateTo(visible ? 1.0F : 0.0F);
   }

   public void snapTo(boolean visible) {
      if (this.init_Z) {
         this.init_Z = false;
         if (visible) {
            this.animator.snapTo(0.0F);
            this.animator.animateTo(1.0F);
            return;
         }
      }

      this.animator.snapTo(visible ? 1.0F : 0.0F);
   }

   public void restart() {
      this.init_Z = true;
   }

   public float progress() {
      return this.animator.value();
   }

   public boolean isFadingOut(boolean visible) {
      return !visible && this.animator.value() > 0.001F;
   }

   public boolean shouldRender(boolean visible) {
      return visible || this.isFadingOut(visible);
   }

   public boolean isAnimating() {
      return this.animator.isAnimating();
   }

   public float scale() {
      return 0.8F + 0.19999999F * this.progress();
   }

   public float opacity() {
      return this.progress();
   }

   public void init(GuiGraphicsExtractor graphics, NetHelper bounds, boolean zoom, Runnable draw) {
      float value = this.progress();
      if (value >= 0.999F) {
         draw.run();
      } else {
         float scale = 0.8F + 0.19999999F * value;
         int alphaDepth = UiAlphaStack.depth();
         graphics.pose().pushMatrix();

         try {
            if (zoom && bounds != null) {
               float cx = bounds.read() + bounds.update() / 2.0F;
               float cy = bounds.write() + bounds.sync() / 2.0F;
               graphics.pose().translate(cx, cy);
               graphics.pose().scale(scale, scale);
               graphics.pose().translate(-cx, -cy);
            }

            UiAlphaStack.push(value);
            draw.run();
         } finally {
            UiAlphaStack.popTo(alphaDepth);
            graphics.pose().popMatrix();
         }
      }
   }
}
