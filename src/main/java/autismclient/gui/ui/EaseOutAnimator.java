package autismclient.gui.ui;

import java.util.function.LongSupplier;

public final class EaseOutAnimator {
   public static final int init_I = 150;
   public static final int load_I = 180;
   private static final long slowmo = parseSlowmo(System.getenv("AUTISM_UI_SLOWMO"));
   private final long load_J;
   private final LongSupplier clock;
   private float init_F;
   private float initial;
   private long read;

   public EaseOutAnimator(float initial, int durationMillis) {
      this(initial, durationMillis, System::nanoTime);
   }

   EaseOutAnimator(float initial, int durationMillis, LongSupplier clock) {
      this(initial, durationMillis, clock, slowmo);
   }

   EaseOutAnimator(float initial, int durationMillis, LongSupplier clock, long slowmo) {
      this.clock = clock;
      this.init_F = this.initial = initial;
      this.load_J = Math.max(1, Math.min(200, durationMillis)) * 1000000L * Math.max(1L, slowmo);
   }

   static long parseSlowmo(String raw) {
      if (raw != null && !raw.isBlank()) {
         try {
            return Math.max(1L, Math.min(100L, Long.parseLong(raw.trim())));
         } catch (NumberFormatException ignored) {
            return 1L;
         }
      } else {
         return 1L;
      }
   }

   public float animateTo(float nextTarget) {
      long now = this.clock.getAsLong();
      if (Float.compare(nextTarget, this.initial) != 0) {
         this.init_F = this.valueAt(now);
         this.initial = nextTarget;
         this.read = now;
      }

      return this.valueAt(now);
   }

   public float value() {
      return this.valueAt(this.clock.getAsLong());
   }

   public float load() {
      return this.initial;
   }

   public void snapTo(float value) {
      this.init_F = this.initial = value;
      this.read = 0L;
   }

   public boolean isAnimating() {
      return this.init_F != this.initial && this.clock.getAsLong() - this.read < this.load_J;
   }

   private float valueAt(long now) {
      if (this.init_F != this.initial && now - this.read < this.load_J) {
         float progress = Math.max(0.0F, (float)(now - this.read) / (float)this.load_J);
         float remaining = 1.0F - progress;
         return this.init_F + (this.initial - this.init_F) * (1.0F - remaining * remaining * remaining);
      } else {
         return this.initial;
      }
   }
}
