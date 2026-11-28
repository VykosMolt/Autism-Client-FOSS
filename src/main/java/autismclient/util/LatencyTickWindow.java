package autismclient.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class LatencyTickWindow<K> {
   private final Map<K, Integer> expiries = new LinkedHashMap<>();
   private final int capacity;

   public LatencyTickWindow(int maximum) {
      this.capacity = Math.max(1, maximum);
   }

   public void mark(K key, int tick, int latencyMs) {
      int expiry = tick + windowTicks(latencyMs);
      this.expiries.put(key, expiry);
      this.expire(tick);

      while (this.expiries.size() > this.capacity) {
         this.expiries.remove(this.expiries.keySet().iterator().next());
      }
   }

   public boolean isPending(K key, int tick) {
      Integer expiry = this.expiries.get(key);
      if (expiry == null) {
         return false;
      } else if (tick - expiry >= 0) {
         this.expiries.remove(key);
         return false;
      } else {
         return true;
      }
   }

   public void remove(K key) {
      this.expiries.remove(key);
   }

   public void expire(int tick) {
      this.expiries.entrySet().removeIf(entry -> tick - entry.getValue() >= 0);
   }

   public int size() {
      return this.expiries.size();
   }

   public Set<K> init_Set() {
      return this.expiries.keySet();
   }

   public Set<K> activeKeys(int tick) {
      this.expire(tick);
      return this.expiries.keySet();
   }

   public void clear() {
      this.expiries.clear();
   }

   public static int windowTicks(int latencyMs) {
      return Math.max(3, Math.min(16, (int)Math.ceil(Math.max(0, latencyMs) / 50.0) + 2));
   }
}
