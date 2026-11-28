package autismclient.modules;

final class WTapTimingMachine {
   private static final long init_J = 900000000L;
   private static final long load_J = 250000000L;
   private WTapTimingMachine.Phase phase = WTapTimingMachine.Phase.IDLE;
   private long hitExpiresAt;
   private long releaseAt;
   private long releaseNanos;
   private long releaseEndsAt;
   private int releaseTick;
   private int releaseTicks;
   private int lastFinishTick = -2147483648;
   private static final long flush = 50000000L;
   private long tapCount;

   boolean arm(long now, int tick, long delay, long duration, boolean selected) {
      this.expireStaleHit(now);
      if (selected
         && this.phase == WTapTimingMachine.Phase.IDLE
         && tick > this.lastFinishTick
         && delay >= 0L
         && delay <= 200000000L
         && duration >= 1L
         && duration <= 200000000L) {
         this.phase = WTapTimingMachine.Phase.HIT;
         this.hitExpiresAt = now + 900000000L;
         this.releaseAt = delay;
         this.releaseNanos = duration;
         return true;
      } else {
         return false;
      }
   }

   boolean beginDelay(long now) {
      this.expireStaleHit(now);
      if (this.phase != WTapTimingMachine.Phase.HIT) {
         return false;
      }

      this.phase = WTapTimingMachine.Phase.DELAY;
      this.releaseAt += now;
      return true;
   }

   boolean shouldRelease(long now, int tick, boolean eligible) {
      if (!eligible) {
         this.reset();
         return false;
      }

      this.expireStaleHit(now);
      if (this.phase == WTapTimingMachine.Phase.DELAY && now >= this.releaseAt) {
         if (now - this.releaseAt > 250000000L) {
            this.reset();
            return false;
         }

         this.phase = WTapTimingMachine.Phase.RELEASE;
         this.releaseEndsAt = now + this.releaseNanos;
         this.releaseTick = tick;
         this.releaseTicks = (int)Math.max(1L, (this.releaseNanos + 50000000L - 1L) / 50000000L);
         this.tapCount++;
      }

      if (this.phase != WTapTimingMachine.Phase.RELEASE) {
         return false;
      } else if (tick != this.releaseTick && (now >= this.releaseEndsAt || tick - this.releaseTick >= this.releaseTicks)) {
         this.reset();
         this.lastFinishTick = tick;
         return false;
      } else {
         return true;
      }
   }

   private void expireStaleHit(long now) {
      if (this.phase == WTapTimingMachine.Phase.HIT && now > this.hitExpiresAt) {
         this.reset();
      }
   }

   boolean isActive() {
      return this.phase != WTapTimingMachine.Phase.IDLE;
   }

   boolean activeAt(long now) {
      this.expireStaleHit(now);
      return this.isActive();
   }

   boolean isReleasing() {
      return this.phase == WTapTimingMachine.Phase.RELEASE;
   }

   long init_J() {
      return this.tapCount;
   }

   void reset() {
      this.phase = WTapTimingMachine.Phase.IDLE;
      this.hitExpiresAt = this.releaseAt = this.releaseNanos = this.releaseEndsAt = 0L;
   }

   void resetAll() {
      this.reset();
      this.lastFinishTick = -2147483648;
      this.tapCount = 0L;
   }

   private enum Phase {
      IDLE,
      HIT,
      DELAY,
      RELEASE;
   }
}
