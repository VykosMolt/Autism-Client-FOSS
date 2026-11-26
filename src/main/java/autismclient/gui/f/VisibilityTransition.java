package autismclient.gui.f;

import java.util.function.LongSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;

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

   public void init_V(boolean visible) {
      if (this.init_Z) {
         this.init_Z = false;
         if (visible) {
            this.init_RingBuffer.init_V_F(0.0F);
         }
      }

      this.init_RingBuffer.init_F_F(visible ? 1.0F : 0.0F);
   }

   public void load_V(boolean visible) {
      if (this.init_Z) {
         this.init_Z = false;
         if (visible) {
            this.init_RingBuffer.init_V_F(0.0F);
            this.init_RingBuffer.init_F_F(1.0F);
            return;
         }
      }

      this.init_RingBuffer.init_V_F(visible ? 1.0F : 0.0F);
   }

   public void init_V() {
      this.init_Z = true;
   }

   public float init_F() {
      return this.init_RingBuffer.init_F();
   }

   public boolean init_Z(boolean visible) {
      return !visible && this.init_RingBuffer.init_F() > 0.001F;
   }

   public boolean load_Z(boolean visible) {
      return visible || this.init_Z(visible);
   }

   public boolean init_Z() {
      return this.init_RingBuffer.init_Z();
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
         int alphaDepth = UiAlphaStack.init_I();
         graphics.pose().pushMatrix();

         try {
            if (zoom && bounds != null) {
               float cx = bounds.read() + bounds.update() / 2.0F;
               float cy = bounds.write() + bounds.sync() / 2.0F;
               graphics.pose().translate(cx, cy);
               graphics.pose().scale(scale, scale);
               graphics.pose().translate(-cx, -cy);
            }

            UiAlphaStack.init_V_F(value);
            draw.run();
         } finally {
            UiAlphaStack.init_V_I(alphaDepth);
            graphics.pose().popMatrix();
         }
      }
   }
}
