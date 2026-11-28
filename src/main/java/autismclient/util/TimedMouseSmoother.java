package autismclient.util;

public final class TimedMouseSmoother {
   private static final double init_D = 1.0E-9;
   private double remainingX;
   private double remainingY;
   private double carryX;
   private double carryY;
   private double remainingSeconds;
   private long lastStepX;
   private long lastStepY;
   private boolean varyUpX;
   private boolean varyUpY = true;

   public void push(double x, double y, double durationSeconds) {
      if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(durationSeconds)) {
         this.carryX = keepCarryIfSameDirection(this.carryX, this.remainingX, x);
         this.carryY = keepCarryIfSameDirection(this.carryY, this.remainingY, y);
         this.remainingX = x;
         this.remainingY = y;
         this.remainingSeconds = Math.max(0.001, durationSeconds);
      }
   }

   public FractionalCountAccumulator.WholeCounts drain(double elapsedSeconds) {
      if (Double.isFinite(elapsedSeconds) && !(elapsedSeconds <= 0.0) && !(this.remainingSeconds <= 0.0)) {
         double fraction = elapsedSeconds + 1.0E-9 >= this.remainingSeconds ? 1.0 : Math.min(1.0, elapsedSeconds / this.remainingSeconds);
         double sliceX = this.remainingX * fraction;
         double sliceY = this.remainingY * fraction;
         this.remainingX -= sliceX;
         this.remainingY -= sliceY;
         this.remainingSeconds = Math.max(0.0, this.remainingSeconds - elapsedSeconds);
         if (this.remainingSeconds == 0.0) {
            this.remainingX = 0.0;
            this.remainingY = 0.0;
         }

         double totalX = this.carryX + sliceX;
         double totalY = this.carryY + sliceY;
         long wholeX = truncateToWhole(totalX);
         long wholeY = truncateToWhole(totalY);
         if (this.remainingSeconds > 1.0E-9) {
            wholeX = avoidRepeatStep(wholeX, this.lastStepX, this.remainingX, this.varyUpX);
            wholeY = avoidRepeatStep(wholeY, this.lastStepY, this.remainingY, this.varyUpY);
            if (wholeX == this.lastStepX && wholeX != 0L) {
               this.varyUpX = !this.varyUpX;
            }

            if (wholeY == this.lastStepY && wholeY != 0L) {
               this.varyUpY = !this.varyUpY;
            }
         }

         this.carryX = totalX - wholeX;
         this.carryY = totalY - wholeY;
         this.lastStepX = wholeX;
         this.lastStepY = wholeY;
         return new FractionalCountAccumulator.WholeCounts(wholeX, wholeY);
      } else {
         return new FractionalCountAccumulator.WholeCounts(0L, 0L);
      }
   }

   public boolean isActive() {
      return this.remainingSeconds > 0.0;
   }

   public void reset() {
      this.remainingX = 0.0;
      this.remainingY = 0.0;
      this.carryX = 0.0;
      this.carryY = 0.0;
      this.remainingSeconds = 0.0;
      this.lastStepX = 0L;
      this.lastStepY = 0L;
      this.varyUpX = false;
      this.varyUpY = true;
   }

   private static double keepCarryIfSameDirection(double carry, double previous, double next) {
      if (Math.abs(next) < 1.0E-9) {
         return 0.0;
      }

      double direction = Math.abs(previous) >= 1.0E-9 ? previous : carry;
      return Math.abs(direction) >= 1.0E-9 && Math.signum(direction) != Math.signum(next) ? 0.0 : carry;
   }

   private static long truncateToWhole(double value) {
      if (Double.isFinite(value) && !(Math.abs(value) < 1.0)) {
         double nearest = Math.rint(value);
         if (Math.abs(value - nearest) < 1.0E-7) {
            value = nearest;
         }

         return value > 0.0 ? (long)Math.floor(value) : (long)Math.ceil(value);
      } else {
         return 0L;
      }
   }

   private static long avoidRepeatStep(long value, long previous, double future, boolean increase) {
      if (value != 0L && value == previous && !(Math.abs(future) < 1.0)) {
         long direction = Long.signum(value);
         long varied = value + (increase ? direction : -direction);
         return Long.signum(varied) == direction ? varied : value;
      } else {
         return value;
      }
   }
}
