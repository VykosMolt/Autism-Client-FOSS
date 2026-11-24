package autismclient.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class cR {
   public static final long init_J = 360000L;
   private final Map<String, cR.Inner_DataUtil> init_Map = new LinkedHashMap<>(8, 0.75F, true);
   private long load = -1L;

   public cR.Inner_DataUtil init(String server, String dimension) {
      String key = server + "\n" + dimension;
      cR.Inner_DataUtil current = this.init_Map.get(key);
      if (current != null) {
         return current;
      }

      if (this.init_Map.size() >= 8) {
         this.init_Map.remove(this.init_Map.keySet().iterator().next());
      }

      current = new cR.Inner_DataUtil();
      this.init_Map.put(key, current);
      return current;
   }

   public void init(long now) {
      if (this.load < 0L) {
         this.load = now;
      }
   }

   public void load(long now) {
      if (this.load >= 0L && now - this.load >= 360000L) {
         this.init_Map.clear();
      }
   }

   public void read(long now) {
      this.load(now);
      this.load = -1L;
   }

   public boolean init_Z() {
      return !this.init_Map.isEmpty();
   }

   public int init_I() {
      return this.init_Map.size();
   }

   public static final class Inner_DataUtil {
      public final cS init_cS = new cS();
      public final Map<Long, cS.Inner_CachePool> init_Map = new LinkedHashMap<>();

      public void init(cS.Inner_CachePool frame) {
         if (this.init_Map.size() >= 512 && !this.init_Map.containsKey(frame.init_BlockPos().asLong())) {
            this.init_Map.remove(this.init_Map.keySet().iterator().next());
         }

         this.init_Map.put(frame.init_BlockPos().asLong(), frame);
      }
   }
}
