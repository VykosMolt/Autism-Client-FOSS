package autismclient.gui.f;

import java.util.function.LongSupplier;

public final class EaseOutAnimator {
   public static final int init_I = 150;
   public static final int load_I = 180;
   private static final long init_J = init_J_Str(System.getenv("AUTISM_UI_SLOWMO"));
   private final long load_J;
   private final LongSupplier init_LongSupplier;
   private float init_F;
   private float load_F;
   private long read;

   public EaseOutAnimator(float initial, int durationMillis) {
      this(initial, durationMillis, System::nanoTime);
   }

   EaseOutAnimator(float initial, int durationMillis, LongSupplier clock) {
      this(initial, durationMillis, clock, init_J);
   }

   EaseOutAnimator(float initial, int durationMillis, LongSupplier clock, long slowmo) {
      this.init_LongSupplier = clock;
      this.init_F = this.load_F = initial;
      this.load_J = Math.max(1, Math.min(200, durationMillis)) * 1000000L * Math.max(1L, slowmo);
   }

   static long init_J_Str(String raw) {
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

   public float init_F_F(float nextTarget) {
      long now = this.init_LongSupplier.getAsLong();
      if (Float.compare(nextTarget, this.load_F) != 0) {
         this.init_F = this.init_F_J(now);
         this.load_F = nextTarget;
         this.read = now;
      }

      return this.init_F_J(now);
   }

   public float init_F() {
      return this.init_F_J(this.init_LongSupplier.getAsLong());
   }

   public float load() {
      return this.load_F;
   }

   public void init_V_F(float value) {
      this.init_F = this.load_F = value;
      this.read = 0L;
   }

   public boolean init_Z() {
      return this.init_F != this.load_F && this.init_LongSupplier.getAsLong() - this.read < this.load_J;
   }

   private float init_F_J(long now) {
      if (this.init_F != this.load_F && now - this.read < this.load_J) {
         float progress = Math.max(0.0F, (float)(now - this.read) / (float)this.load_J);
         float remaining = 1.0F - progress;
         return this.init_F + (this.load_F - this.init_F) * (1.0F - remaining * remaining * remaining);
      } else {
         return this.load_F;
      }
   }
}
