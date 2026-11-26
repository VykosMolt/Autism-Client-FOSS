package autismclient.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class ActionCooldownScheduler {
   private final Map<String, Long> init_Map = new HashMap<>();
   private int init_I = -2147483648;

   public boolean init_Z_Str(String kind, int tick, long nowNanos) {
      if (tick == this.init_I) {
         return false;
      }

      Long next = this.init_Map.get(kind);
      return next == null || nowNanos >= next;
   }

   public boolean init(int tick) {
      return tick == this.init_I;
   }

   public boolean init_Z_Str(String primary, String other, int tick, long nowNanos) {
      return this.init_Z_Str(primary, tick, nowNanos) || !this.init_Z_Str(other, tick, nowNanos);
   }

   public void init_V_Str(String kind, int tick, long nowNanos, long delayNanos) {
      this.init_I = tick;
      this.init_Map.put(kind, nowNanos + Math.max(0L, delayNanos));
   }

   public void init() {
      this.init_Map.clear();
      this.init_I = -2147483648;
   }

   public static long init_J_I(int baseMs, int jitterMs, Random random) {
      int extra = jitterMs > 0 ? random.nextInt(jitterMs + 1) : 0;
      return ((long)Math.max(0, baseMs) + extra) * 1000000L;
   }

   public static int init(int placeMs, int chargeMs, int detonateMs, int switchMs, int latencyMs, boolean shielded) {
      long configured = Math.max(0, placeMs) + Math.max(0, chargeMs) + Math.max(0, detonateMs) + (shielded ? 3L : 2L) * Math.max(0, switchMs);
      long budgetMs = configured + Math.max(0, latencyMs) + 250L;
      return Math.max(20, Math.min(200, (int)Math.ceil(budgetMs / 50.0) + 8));
   }

   public static int init_I_I(int placeMs, int switchMs, int latencyMs) {
      long budgetMs = Math.max(0, placeMs) + Math.max(0, switchMs) + Math.max(0, latencyMs) + 250L;
      return Math.max(25, Math.min(100, (int)Math.ceil(budgetMs / 50.0) + 6));
   }
}
