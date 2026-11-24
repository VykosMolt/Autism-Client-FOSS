package autismclient.modules;

final class ValueSink {
   private static final long init_J = 50000000L;
   private static final long load_J = 5000000L;
   private long read;
   private long write;
   private long update;
   private boolean init_Z;
   private boolean load_Z;

   boolean init_Z(long now) {
      return !this.load_Z || now - this.update >= 295000000L;
   }

   void init_V(long now) {
      this.init_Z = true;
      this.read = now;
      this.write = now;
   }

   float init(float step, float damage, long now) {
      if (this.init_Z && step > 0.0F && Float.isFinite(step)) {
         this.write = this.read + (long)Math.ceil(1.0 / step) * 50000000L;
         if (damage + step >= 1.0F) {
            if (now < this.write - 5000000L) {
               return 0.0F;
            }

            this.update = now;
            this.load_Z = true;
            this.init_Z = false;
         }

         return step;
      } else {
         return step;
      }
   }

   double init_D(long now) {
      return this.init_Z ? Math.ceil(Math.max(0L, this.write - now - 5000000L) / 5.0E7) : 0.0;
   }

   double load(long now) {
      return !this.init_Z && this.load_Z ? Math.ceil(Math.max(0L, 295000000L - (now - this.update)) / 5.0E7) : 0.0;
   }

   void init() {
      this.init_Z = false;
   }

   void load() {
      this.init();
      this.load_Z = false;
   }
}
