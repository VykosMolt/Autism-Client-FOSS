package autismclient.util;

public final class FractionalCountAccumulator {
   private double pendingX;
   private double pendingY;
   private double remainderX;
   private double remainderY;

   public void add(double x, double y) {
      if (Double.isFinite(x)) {
         this.pendingX += x;
      }

      if (Double.isFinite(y)) {
         this.pendingY += y;
      }
   }

   public void set(double x, double y) {
      this.pendingX = Double.isFinite(x) ? x : 0.0;
      this.pendingY = Double.isFinite(y) ? y : 0.0;
   }

   public FractionalCountAccumulator.WholeCounts takeWhole() {
      double totalX = this.pendingX + this.remainderX;
      double totalY = this.pendingY + this.remainderY;
      long wholeX = wholePart(totalX);
      long wholeY = wholePart(totalY);
      this.remainderX = totalX - wholeX;
      this.remainderY = totalY - wholeY;
      this.pendingX = 0.0;
      this.pendingY = 0.0;
      return new FractionalCountAccumulator.WholeCounts(wholeX, wholeY);
   }

   public void reset() {
      this.pendingX = 0.0;
      this.pendingY = 0.0;
      this.remainderX = 0.0;
      this.remainderY = 0.0;
   }

   private static long wholePart(double value) {
      if (Double.isFinite(value) && !(Math.abs(value) < 1.0)) {
         return value > 0.0 ? (long)Math.floor(value) : (long)Math.ceil(value);
      } else {
         return 0L;
      }
   }

   public record WholeCounts(long x, long y) {
   }
}
