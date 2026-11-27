package autismclient.gui.ui;

import java.util.function.LongSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import autismclient.gui.f.NetHelper;

public final class VisibilityTransition {
   private static final float init_F = 0.8F;
   private final EaseOutAnimator init_RingBuffer;
   private boolean init_Z;

   public VisibilityTransition(boolean visible) {
      this.init_RingBuffer = new EaseOutAnimator(visible ? 1.0F : 0.0F, 150);
   }

   VisibilityTransition(boolean visible, LongSupplier clock) {
      this.init_RingBuffer = new EaseOutAnimator(visible ? 1.0F : 0.0F, 150, clock);
   }

   public void animateTo(boolean visible) {
      if (this.init_Z) {
         this.init_Z = false;
         if (visible) {
            this.init_RingBuffer.snapTo(0.0F);
         }
      }

      this.init_RingBuffer.animateTo(visible ? 1.0F : 0.0F);
   }

   public void snapTo(boolean visible) {
      if (this.init_Z) {
         this.init_Z = false;
         if (visible) {
            this.init_RingBuffer.snapTo(0.0F);
            this.init_RingBuffer.animateTo(1.0F);
            return;
         }
      }

      this.init_RingBuffer.snapTo(visible ? 1.0F : 0.0F);
   }

   public void restart() {
      this.init_Z = true;
   }

   public float init_F() {
      return this.init_RingBuffer.value();
   }

   public boolean isFadingOut(boolean visible) {
      return !visible && this.init_RingBuffer.value() > 0.001F;
   }

   public boolean shouldRender(boolean visible) {
      return visible || this.isFadingOut(visible);
   }

   public boolean init_Z() {
      return this.init_RingBuffer.isAnimating();
   }

   public float load() {
      return 0.8F + 0.19999999F * this.init_F();
   }

   public float read() {
      return this.init_F();
   }

   public void init(GuiGraphicsExtractor graphics, NetHelper bounds, boolean zoom, Runnable draw) {
      float value = this.init_F();
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
