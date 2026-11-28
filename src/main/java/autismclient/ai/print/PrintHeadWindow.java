package autismclient.ai.print;

import autismclient.ai.engine.AiPlacementContext;
import net.minecraft.core.Direction;

final class PrintHeadWindow {
   private static final int init_I = 6;
   private final float[] yawSamples = new float[6];
   private final float[] pitchSamples = new float[6];
   private int sampleCount;
   private int sampleCursor;
   private long lastTick = -9223372036854775808L;
   private long lastSampleNanos = -9223372036854775808L;
   private long facingSinceNanos;
   private Direction lastFacing;

   void reset() {
      this.sampleCount = this.sampleCursor = 0;
      this.lastTick = this.lastSampleNanos = -9223372036854775808L;
      this.lastFacing = null;
   }

   void observe(float y, float p, long atTick, long now) {
      if (!Float.isFinite(y) || !Float.isFinite(p)) {
         this.reset();
      } else if (atTick != this.lastTick) {
         if (this.lastSampleNanos != -9223372036854775808L && (now < this.lastSampleNanos || now - this.lastSampleNanos > 100000000L)) {
            this.reset();
         }

         this.lastTick = atTick;
         this.lastSampleNanos = now;
         Direction facing = AiPlacementContext.facingOrder(y, p)[0];
         if (facing != this.lastFacing) {
            this.lastFacing = facing;
            this.facingSinceNanos = now;
            this.sampleCount = this.sampleCursor = 0;
         }

         this.yawSamples[this.sampleCursor] = y;
         this.pitchSamples[this.sampleCursor] = p;
         this.sampleCursor = (this.sampleCursor + 1) % 6;
         this.sampleCount = Math.min(6, this.sampleCount + 1);
      }
   }

   boolean isStableFacing(Direction wanted, long now) {
      if (this.sampleCount >= 6 && this.lastFacing == wanted && now - this.facingSinceNanos >= 150000000L && now >= this.lastSampleNanos && now - this.lastSampleNanos <= 100000000L) {
         for (int y = 0; y < this.sampleCount; y++) {
            for (int p = 0; p < this.sampleCount; p++) {
               if (AiPlacementContext.facingOrder(this.yawSamples[y], this.pitchSamples[p])[0] != wanted) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
