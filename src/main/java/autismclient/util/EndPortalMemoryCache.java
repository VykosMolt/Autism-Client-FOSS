package autismclient.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EndPortalMemoryCache {
   public static final long init_J = 360000L;
   private final Map<String, EndPortalMemoryCache.DimensionMemory> memories = new LinkedHashMap<>(8, 0.75F, true);
   private long load = -1L;

   public EndPortalMemoryCache.DimensionMemory memoryFor(String server, String dimension) {
      String key = server + "\n" + dimension;
      EndPortalMemoryCache.DimensionMemory current = this.memories.get(key);
      if (current != null) {
         return current;
      }

      if (this.memories.size() >= 8) {
         this.memories.remove(this.memories.keySet().iterator().next());
      }

      current = new EndPortalMemoryCache.DimensionMemory();
      this.memories.put(key, current);
      return current;
   }

   public void start(long now) {
      if (this.load < 0L) {
         this.load = now;
      }
   }

   public void expire(long now) {
      if (this.load >= 0L && now - this.load >= 360000L) {
         this.memories.clear();
      }
   }

   public void stop(long now) {
      this.expire(now);
      this.load = -1L;
   }

   public boolean hasMemory() {
      return !this.memories.isEmpty();
   }

   public int size() {
      return this.memories.size();
   }

   public static final class DimensionMemory {
      public final StrongholdTriangulator triangulator = new StrongholdTriangulator();
      public final Map<Long, StrongholdTriangulator.PortalFrame> frames = new LinkedHashMap<>();

      public void init(StrongholdTriangulator.PortalFrame frame) {
         if (this.frames.size() >= 512 && !this.frames.containsKey(frame.pos().asLong())) {
            this.frames.remove(this.frames.keySet().iterator().next());
         }

         this.frames.put(frame.pos().asLong(), frame);
      }
   }
}
