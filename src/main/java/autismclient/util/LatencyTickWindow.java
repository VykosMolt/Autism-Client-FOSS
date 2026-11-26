package autismclient.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class LatencyTickWindow<K> {
   private final Map<K, Integer> init_Map = new LinkedHashMap<>();
   private final int init_I;

   public LatencyTickWindow(int maximum) {
      this.init_I = Math.max(1, maximum);
   }

   public void init(K key, int tick, int latencyMs) {
      int expiry = tick + init_I_I(latencyMs);
      this.init_Map.put(key, expiry);
      this.init_V_I(tick);

      while (this.init_Map.size() > this.init_I) {
         this.init_Map.remove(this.init_Map.keySet().iterator().next());
      }
   }

   public boolean init(K key, int tick) {
      Integer expiry = this.init_Map.get(key);
      if (expiry == null) {
         return false;
      } else if (tick - expiry >= 0) {
         this.init_Map.remove(key);
         return false;
      } else {
         return true;
      }
   }

   public void init_V_Obj(K key) {
      this.init_Map.remove(key);
   }

   public void init_V_I(int tick) {
      this.init_Map.entrySet().removeIf(entry -> tick - entry.getValue() >= 0);
   }

   public int init_I() {
      return this.init_Map.size();
   }

   public Set<K> init_Set() {
      return this.init_Map.keySet();
   }

   public Set<K> init_Set_I(int tick) {
      this.init_V_I(tick);
      return this.init_Map.keySet();
   }

   public void init_V() {
      this.init_Map.clear();
   }

   public static int init_I_I(int latencyMs) {
      return Math.max(3, Math.min(16, (int)Math.ceil(Math.max(0, latencyMs) / 50.0) + 2));
   }
}
