package autismclient.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class CellRetryLimiter<T> {
   private final Map<Long, CellRetryLimiter.RetryEntry<T>> init = new LinkedHashMap<>();

   public boolean init(long cell, T state, int tick, int limit) {
      CellRetryLimiter.RetryEntry<T> old = this.init.get(cell);
      int elapsed = old == null ? 0 : tick - old.init_I();
      int count = old != null && Objects.equals(old.init_Obj(), state) && elapsed >= 0 && elapsed <= 20 ? old.load() + (elapsed == 0 ? 0 : 1) : 1;
      if (count > Math.max(1, limit)) {
         this.init.remove(cell);
         return false;
      }

      this.init.put(cell, new CellRetryLimiter.RetryEntry<>(state, tick, count));
      if (this.init.size() > 256) {
         this.init.remove(this.init.keySet().iterator().next());
      }

      return true;
   }

   public void clear() {
      this.init.clear();
   }

   private record RetryEntry<T>(T init_Obj, int init_I, int load) {
   }
}
