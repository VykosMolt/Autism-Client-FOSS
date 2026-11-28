package autismclient.modules;

final class MineBreakPaceGuard {
   private static final long init_J = 50000000L;
   private static final long load_J = 5000000L;
   private long lastStepNanos;
   private long nextAllowedNanos;
   private long lastBreakNanos;
   private boolean init_Z;
   private boolean load_Z;

   boolean ready(long now) {
      return !this.load_Z || now - this.lastBreakNanos >= 295000000L;
   }

   void start(long now) {
      this.init_Z = true;
      this.lastStepNanos = now;
      this.nextAllowedNanos = now;
   }

   float pacedStep(float step, float damage, long now) {
      if (this.init_Z && step > 0.0F && Float.isFinite(step)) {
         this.nextAllowedNanos = this.lastStepNanos + (long)Math.ceil(1.0 / step) * 50000000L;
         if (damage + step >= 1.0F) {
            if (now < this.nextAllowedNanos - 5000000L) {
               return 0.0F;
            }

            this.lastBreakNanos = now;
            this.load_Z = true;
            this.init_Z = false;
         }

         return step;
      } else {
         return step;
      }
   }

   double ticksUntilBreak(long now) {
      return this.init_Z ? Math.ceil(Math.max(0L, this.nextAllowedNanos - now - 5000000L) / 5.0E7) : 0.0;
   }

   double cooldownTicks(long now) {
      return !this.init_Z && this.load_Z ? Math.ceil(Math.max(0L, 295000000L - (now - this.lastBreakNanos)) / 5.0E7) : 0.0;
   }

   void stop() {
      this.init_Z = false;
   }

   void reset() {
      this.stop();
      this.load_Z = false;
   }
}
