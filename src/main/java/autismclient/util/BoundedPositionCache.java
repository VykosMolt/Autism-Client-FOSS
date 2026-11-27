package autismclient.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class BoundedPositionCache<T> {
   private final int init_I;
   private final Map<Long, T> init_Map;

   public BoundedPositionCache(int capacity) {
      if (capacity <= 0) {
         throw new IllegalArgumentException("capacity must be positive");
      }

      this.init_I = capacity;
      // At capacity the oldest position is dropped so new ones are still tracked.
      this.init_Map = new LinkedHashMap<>() {
         @Override
         protected boolean removeEldestEntry(Entry<Long, T> eldest) {
            return this.size() > BoundedPositionCache.this.init_I;
         }
      };
   }

   public boolean init(long position, T value) {
      if (value == null) {
         throw new NullPointerException("value");
      }

      this.init_Map.put(position, value);
      return true;
   }

   public T get(long position) {
      return this.init_Map.get(position);
   }

   public void init_V(long position) {
      this.init_Map.remove(position);
   }

   public Collection<T> entries() {
      return this.init_Map.values();
   }

   public int size() {
      return this.init_Map.size();
   }

   public boolean init_Z() {
      return this.init_Map.isEmpty();
   }

   public void clear() {
      this.init_Map.clear();
   }
}
