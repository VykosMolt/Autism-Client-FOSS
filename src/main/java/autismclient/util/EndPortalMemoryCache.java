package autismclient.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EndPortalMemoryCache {
   public static final long init_J = 360000L;
   private final Map<String, EndPortalMemoryCache.Inner_DataUtil> init_Map = new LinkedHashMap<>(8, 0.75F, true);
   private long load = -1L;

   public EndPortalMemoryCache.Inner_DataUtil init(String server, String dimension) {
      String key = server + "\n" + dimension;
      EndPortalMemoryCache.Inner_DataUtil current = this.init_Map.get(key);
      if (current != null) {
         return current;
      }

      if (this.init_Map.size() >= 8) {
         this.init_Map.remove(this.init_Map.keySet().iterator().next());
      }

      current = new EndPortalMemoryCache.Inner_DataUtil();
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

   public boolean hasMemory() {
      return !this.init_Map.isEmpty();
   }

   public int init_I() {
      return this.init_Map.size();
   }

   public static final class Inner_DataUtil {
      public final StrongholdTriangulator triangulator = new StrongholdTriangulator();
      public final Map<Long, StrongholdTriangulator.Inner_CachePool> frames = new LinkedHashMap<>();

      public void init(StrongholdTriangulator.Inner_CachePool frame) {
         if (this.frames.size() >= 512 && !this.frames.containsKey(frame.pos().asLong())) {
            this.frames.remove(this.frames.keySet().iterator().next());
         }

         this.frames.put(frame.pos().asLong(), frame);
      }
   }
}
