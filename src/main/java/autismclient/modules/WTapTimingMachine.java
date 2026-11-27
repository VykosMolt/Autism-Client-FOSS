package autismclient.modules;

final class WTapTimingMachine {
   private static final long init_J = 900000000L;
   private static final long load_J = 250000000L;
   private WTapTimingMachine.Inner_DataUtil init_be_DataUtil = WTapTimingMachine.Inner_DataUtil.IDLE;
   private long read_J;
   private long write;
   private long update;
   private long sync;
   private int init_I;
   private int load_I;
   private int read_I = -2147483648;
   private static final long flush = 50000000L;
   private long close;

   boolean init(long now, int tick, long delay, long duration, boolean selected) {
      this.init_V(now);
      if (selected
         && this.init_be_DataUtil == WTapTimingMachine.Inner_DataUtil.IDLE
         && tick > this.read_I
         && delay >= 0L
         && delay <= 200000000L
         && duration >= 1L
         && duration <= 200000000L) {
         this.init_be_DataUtil = WTapTimingMachine.Inner_DataUtil.HIT;
         this.read_J = now + 900000000L;
         this.write = delay;
         this.update = duration;
         return true;
      } else {
         return false;
      }
   }

   boolean init_Z(long now) {
      this.init_V(now);
      if (this.init_be_DataUtil != WTapTimingMachine.Inner_DataUtil.HIT) {
         return false;
      }

      this.init_be_DataUtil = WTapTimingMachine.Inner_DataUtil.DELAY;
      this.write += now;
      return true;
   }

   boolean init(long now, int tick, boolean eligible) {
      if (!eligible) {
         this.reset();
         return false;
      }

      this.init_V(now);
      if (this.init_be_DataUtil == WTapTimingMachine.Inner_DataUtil.DELAY && now >= this.write) {
         if (now - this.write > 250000000L) {
            this.reset();
            return false;
         }

         this.init_be_DataUtil = WTapTimingMachine.Inner_DataUtil.RELEASE;
         this.sync = now + this.update;
         this.init_I = tick;
         this.load_I = (int)Math.max(1L, (this.update + 50000000L - 1L) / 50000000L);
         this.close++;
      }

      if (this.init_be_DataUtil != WTapTimingMachine.Inner_DataUtil.RELEASE) {
         return false;
      } else if (tick != this.init_I && (now >= this.sync || tick - this.init_I >= this.load_I)) {
         this.reset();
         this.read_I = tick;
         return false;
      } else {
         return true;
      }
   }

   private void init_V(long now) {
      if (this.init_be_DataUtil == WTapTimingMachine.Inner_DataUtil.HIT && now > this.read_J) {
         this.reset();
      }
   }

   boolean isActive() {
      return this.init_be_DataUtil != WTapTimingMachine.Inner_DataUtil.IDLE;
   }

   boolean load(long now) {
      this.init_V(now);
      return this.isActive();
   }

   boolean isReleasing() {
      return this.init_be_DataUtil == WTapTimingMachine.Inner_DataUtil.RELEASE;
   }

   long init_J() {
      return this.close;
   }

   void reset() {
      this.init_be_DataUtil = WTapTimingMachine.Inner_DataUtil.IDLE;
      this.read_J = this.write = this.update = this.sync = 0L;
   }

   void resetAll() {
      this.reset();
      this.read_I = -2147483648;
      this.close = 0L;
   }

   private enum Inner_DataUtil {
      IDLE,
      HIT,
      DELAY,
      RELEASE;
   }
}
