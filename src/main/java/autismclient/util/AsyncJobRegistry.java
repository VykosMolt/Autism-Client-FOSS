package autismclient.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.LongSupplier;

public final class AsyncJobRegistry {
   private static final int init_I = 64;
   private final LongSupplier init_LongSupplier;
   private final int load;
   private final Map<Long, AsyncJobRegistry.Inner_StreamUtil> init_Map = new LinkedHashMap<>();
   private int read;

   public AsyncJobRegistry(LongSupplier clock) {
      this(clock, 64);
   }

   AsyncJobRegistry(LongSupplier clock, int terminalRetention) {
      this.init_LongSupplier = clock;
      this.load = Math.max(0, terminalRetention);
   }

   public synchronized AsyncJobRegistry.Inner_StreamUtil init(long id, String owner, long timeoutMs) {
      if (this.init_Map.containsKey(id)) {
         throw new IllegalArgumentException("duplicate job id: " + id);
      }

      AsyncJobRegistry.Inner_StreamUtil state = new AsyncJobRegistry.Inner_StreamUtil(
         id, owner == null ? "" : owner, AsyncJobRegistry.Inner_DataUtil.PENDING, true, false, this.init_LongSupplier.getAsLong() + Math.max(1L, timeoutMs)
      );
      this.init_Map.put(id, state);
      return state;
   }

   public synchronized boolean requestCancel(long id) {
      AsyncJobRegistry.Inner_StreamUtil state = this.init_Map.get(id);
      if (state != null && !state.init_Z()) {
         this.init_Map.put(id, new AsyncJobRegistry.Inner_StreamUtil(state.id(), state.label(), AsyncJobRegistry.Inner_DataUtil.PENDING, false, true, state.deadline()));
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean init(long id, AsyncJobRegistry.Inner_DataUtil completion) {
      AsyncJobRegistry.Inner_StreamUtil state = this.init_Map.get(id);
      if (state != null && !state.init_Z() && completion != null && completion != AsyncJobRegistry.Inner_DataUtil.PENDING) {
         this.init_Map.put(id, new AsyncJobRegistry.Inner_StreamUtil(state.id(), state.label(), completion, false, false, state.deadline()));
         this.read++;
         this.init_V();
         return true;
      } else {
         return false;
      }
   }

   private void init_V() {
      Iterator<Entry<Long, AsyncJobRegistry.Inner_StreamUtil>> iterator = this.init_Map.entrySet().iterator();

      while (this.read > this.load && iterator.hasNext()) {
         if (iterator.next().getValue().init_Z()) {
            iterator.remove();
            this.read--;
         }
      }
   }

   public synchronized AsyncJobRegistry.Inner_StreamUtil state(long id) {
      AsyncJobRegistry.Inner_StreamUtil state = this.init_Map.get(id);
      return state == null ? new AsyncJobRegistry.Inner_StreamUtil(id, "", AsyncJobRegistry.Inner_DataUtil.FAILED, false, false, 0L) : state;
   }

   public synchronized List<Long> expireOverdue() {
      long now = this.init_LongSupplier.getAsLong();
      List<Long> expired = new ArrayList<>();

      for (AsyncJobRegistry.Inner_StreamUtil state : this.init_Map.values()) {
         if (!state.init_Z() && now >= state.deadline()) {
            expired.add(state.id());
         }
      }

      return expired;
   }

   public synchronized List<Long> load() {
      List<Long> pending = new ArrayList<>();

      for (AsyncJobRegistry.Inner_StreamUtil state : this.init_Map.values()) {
         if (!state.init_Z()) {
            pending.add(state.id());
         }
      }

      return pending;
   }

   public enum Inner_DataUtil {
      PENDING,
      SUCCEEDED,
      FAILED,
      CANCELLED;
   }

   public record Inner_StreamUtil(long id, String label, AsyncJobRegistry.Inner_DataUtil status, boolean init_Z, boolean load_Z, long deadline) {
      public boolean init_Z() {
         return this.status != AsyncJobRegistry.Inner_DataUtil.PENDING;
      }

      public boolean load_Z() {
         return this.init_Z;
      }

      public boolean read() {
         return this.load_Z;
      }
   }
}
