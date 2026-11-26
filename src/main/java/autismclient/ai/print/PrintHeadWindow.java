package autismclient.ai.print;

import autismclient.ai.engine.AiPlacementContext;
import net.minecraft.core.Direction;

final class PrintHeadWindow {
   private static final int init_I = 6;
   private final float[] init_FArr = new float[6];
   private final float[] load_FArr = new float[6];
   private int load_I;
   private int read_I;
   private long init_J = -9223372036854775808L;
   private long load_J = -9223372036854775808L;
   private long read_J;
   private Direction init_Direction;

   void init() {
      this.load_I = this.read_I = 0;
      this.init_J = this.load_J = -9223372036854775808L;
      this.init_Direction = null;
   }

   void init(float y, float p, long atTick, long now) {
      if (!Float.isFinite(y) || !Float.isFinite(p)) {
         this.init();
      } else if (atTick != this.init_J) {
         if (this.load_J != -9223372036854775808L && (now < this.load_J || now - this.load_J > 100000000L)) {
            this.init();
         }

         this.init_J = atTick;
         this.load_J = now;
         Direction facing = AiPlacementContext.init(y, p)[0];
         if (facing != this.init_Direction) {
            this.init_Direction = facing;
            this.read_J = now;
            this.load_I = this.read_I = 0;
         }

         this.init_FArr[this.read_I] = y;
         this.load_FArr[this.read_I] = p;
         this.read_I = (this.read_I + 1) % 6;
         this.load_I = Math.min(6, this.load_I + 1);
      }
   }

   boolean init(Direction wanted, long now) {
      if (this.load_I >= 6 && this.init_Direction == wanted && now - this.read_J >= 150000000L && now >= this.load_J && now - this.load_J <= 100000000L) {
         for (int y = 0; y < this.load_I; y++) {
            for (int p = 0; p < this.load_I; p++) {
               if (AiPlacementContext.init(this.init_FArr[y], this.load_FArr[p])[0] != wanted) {
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
