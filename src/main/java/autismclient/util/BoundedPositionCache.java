package autismclient.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class BoundedPositionCache<T> {
   private final int capacity;
   private final Map<Long, T> entries;

   public BoundedPositionCache(int capacity) {
      if (capacity <= 0) {
         throw new IllegalArgumentException("capacity must be positive");
      }

      this.capacity = capacity;
      // At capacity the oldest position is dropped so new ones are still tracked.
      this.entries = new LinkedHashMap<>() {
         @Override
         protected boolean removeEldestEntry(Entry<Long, T> eldest) {
            return this.size() > BoundedPositionCache.this.capacity;
         }
      };
   }

   public boolean init(long position, T value) {
      if (value == null) {
         throw new NullPointerException("value");
      }

      this.entries.put(position, value);
      return true;
   }

   public T get(long position) {
      return this.entries.get(position);
   }

   public void remove(long position) {
      this.entries.remove(position);
   }

   public Collection<T> entries() {
      return this.entries.values();
   }

   public int size() {
      return this.entries.size();
   }

   public boolean isEmpty() {
      return this.entries.isEmpty();
   }

   public void clear() {
      this.entries.clear();
   }
}
