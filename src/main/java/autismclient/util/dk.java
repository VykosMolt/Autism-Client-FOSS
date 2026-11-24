package autismclient.util;

public final class dk {
   private static final double init_D = 1.0E-9;
   private double load_D;
   private double read;
   private double write;
   private double update;
   private double sync;
   private long init_J;
   private long load_J;
   private boolean init_Z;
   private boolean load_Z = true;

   public void init_V_D(double x, double y, double durationSeconds) {
      if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(durationSeconds)) {
         this.write = init_D_D(this.write, this.load_D, x);
         this.update = init_D_D(this.update, this.read, y);
         this.load_D = x;
         this.read = y;
         this.sync = Math.max(0.001, durationSeconds);
      }
   }

   public dh.Inner_DataUtil init_dh_DataUtil_D(double elapsedSeconds) {
      if (Double.isFinite(elapsedSeconds) && !(elapsedSeconds <= 0.0) && !(this.sync <= 0.0)) {
         double fraction = elapsedSeconds + 1.0E-9 >= this.sync ? 1.0 : Math.min(1.0, elapsedSeconds / this.sync);
         double sliceX = this.load_D * fraction;
         double sliceY = this.read * fraction;
         this.load_D -= sliceX;
         this.read -= sliceY;
         this.sync = Math.max(0.0, this.sync - elapsedSeconds);
         if (this.sync == 0.0) {
            this.load_D = 0.0;
            this.read = 0.0;
         }

         double totalX = this.write + sliceX;
         double totalY = this.update + sliceY;
         long wholeX = init_J_D(totalX);
         long wholeY = init_J_D(totalY);
         if (this.sync > 1.0E-9) {
            wholeX = init(wholeX, this.init_J, this.load_D, this.init_Z);
            wholeY = init(wholeY, this.load_J, this.read, this.load_Z);
            if (wholeX == this.init_J && wholeX != 0L) {
               this.init_Z = !this.init_Z;
            }

            if (wholeY == this.load_J && wholeY != 0L) {
               this.load_Z = !this.load_Z;
            }
         }

         this.write = totalX - wholeX;
         this.update = totalY - wholeY;
         this.init_J = wholeX;
         this.load_J = wholeY;
         return new dh.Inner_DataUtil(wholeX, wholeY);
      } else {
         return new dh.Inner_DataUtil(0L, 0L);
      }
   }

   public boolean init_Z() {
      return this.sync > 0.0;
   }

   public void init_V() {
      this.load_D = 0.0;
      this.read = 0.0;
      this.write = 0.0;
      this.update = 0.0;
      this.sync = 0.0;
      this.init_J = 0L;
      this.load_J = 0L;
      this.init_Z = false;
      this.load_Z = true;
   }

   private static double init_D_D(double carry, double previous, double next) {
      if (Math.abs(next) < 1.0E-9) {
         return 0.0;
      }

      double direction = Math.abs(previous) >= 1.0E-9 ? previous : carry;
      return Math.abs(direction) >= 1.0E-9 && Math.signum(direction) != Math.signum(next) ? 0.0 : carry;
   }

   private static long init_J_D(double value) {
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

   private static long init(long value, long previous, double future, boolean increase) {
      if (value != 0L && value == previous && !(Math.abs(future) < 1.0)) {
         long direction = Long.signum(value);
         long varied = value + (increase ? direction : -direction);
         return Long.signum(varied) == direction ? varied : value;
      } else {
         return value;
      }
   }
}
