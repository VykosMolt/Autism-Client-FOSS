package autismclient.util;

public final class FractionalCountAccumulator {
   private double init;
   private double load;
   private double read;
   private double write;

   public void init(double x, double y) {
      if (Double.isFinite(x)) {
         this.init += x;
      }

      if (Double.isFinite(y)) {
         this.load += y;
      }
   }

   public void load(double x, double y) {
      this.init = Double.isFinite(x) ? x : 0.0;
      this.load = Double.isFinite(y) ? y : 0.0;
   }

   public FractionalCountAccumulator.WholeCounts takeWhole() {
      double totalX = this.init + this.read;
      double totalY = this.load + this.write;
      long wholeX = init(totalX);
      long wholeY = init(totalY);
      this.read = totalX - wholeX;
      this.write = totalY - wholeY;
      this.init = 0.0;
      this.load = 0.0;
      return new FractionalCountAccumulator.WholeCounts(wholeX, wholeY);
   }

   public void reset() {
      this.init = 0.0;
      this.load = 0.0;
      this.read = 0.0;
      this.write = 0.0;
   }

   private static long init(double value) {
      if (Double.isFinite(value) && !(Math.abs(value) < 1.0)) {
         return value > 0.0 ? (long)Math.floor(value) : (long)Math.ceil(value);
      } else {
         return 0L;
      }
   }

   public record WholeCounts(long init, long load) {
   }
}
