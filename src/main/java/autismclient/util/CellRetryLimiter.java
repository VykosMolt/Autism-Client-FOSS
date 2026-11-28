package autismclient.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class CellRetryLimiter<T> {
   private final Map<Long, CellRetryLimiter.RetryEntry<T>> entries = new LinkedHashMap<>();

   public boolean init(long cell, T state, int tick, int limit) {
      CellRetryLimiter.RetryEntry<T> old = this.entries.get(cell);
      int elapsed = old == null ? 0 : tick - old.tick();
      int count = old != null && Objects.equals(old.state(), state) && elapsed >= 0 && elapsed <= 20 ? old.load() + (elapsed == 0 ? 0 : 1) : 1;
      if (count > Math.max(1, limit)) {
         this.entries.remove(cell);
         return false;
      }

      this.entries.put(cell, new CellRetryLimiter.RetryEntry<>(state, tick, count));
      if (this.entries.size() > 256) {
         this.entries.remove(this.entries.keySet().iterator().next());
      }

      return true;
   }

   public void clear() {
      this.entries.clear();
   }

   private record RetryEntry<T>(T state, int tick, int load) {
   }
}
