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
   private final LongSupplier clock;
   private final int load;
   private final Map<Long, AsyncJobRegistry.JobState> jobs = new LinkedHashMap<>();
   private int read;

   public AsyncJobRegistry(LongSupplier clock) {
      this(clock, 64);
   }

   AsyncJobRegistry(LongSupplier clock, int terminalRetention) {
      this.clock = clock;
      this.load = Math.max(0, terminalRetention);
   }

   public synchronized AsyncJobRegistry.JobState init(long id, String owner, long timeoutMs) {
      if (this.jobs.containsKey(id)) {
         throw new IllegalArgumentException("duplicate job id: " + id);
      }

      AsyncJobRegistry.JobState state = new AsyncJobRegistry.JobState(
         id, owner == null ? "" : owner, AsyncJobRegistry.JobStatus.PENDING, true, false, this.clock.getAsLong() + Math.max(1L, timeoutMs)
      );
      this.jobs.put(id, state);
      return state;
   }

   public synchronized boolean requestCancel(long id) {
      AsyncJobRegistry.JobState state = this.jobs.get(id);
      if (state != null && !state.init_Z()) {
         this.jobs.put(id, new AsyncJobRegistry.JobState(state.id(), state.label(), AsyncJobRegistry.JobStatus.PENDING, false, true, state.deadline()));
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean init(long id, AsyncJobRegistry.JobStatus completion) {
      AsyncJobRegistry.JobState state = this.jobs.get(id);
      if (state != null && !state.init_Z() && completion != null && completion != AsyncJobRegistry.JobStatus.PENDING) {
         this.jobs.put(id, new AsyncJobRegistry.JobState(state.id(), state.label(), completion, false, false, state.deadline()));
         this.read++;
         this.init_V();
         return true;
      } else {
         return false;
      }
   }

   private void init_V() {
      Iterator<Entry<Long, AsyncJobRegistry.JobState>> iterator = this.jobs.entrySet().iterator();

      while (this.read > this.load && iterator.hasNext()) {
         if (iterator.next().getValue().init_Z()) {
            iterator.remove();
            this.read--;
         }
      }
   }

   public synchronized AsyncJobRegistry.JobState state(long id) {
      AsyncJobRegistry.JobState state = this.jobs.get(id);
      return state == null ? new AsyncJobRegistry.JobState(id, "", AsyncJobRegistry.JobStatus.FAILED, false, false, 0L) : state;
   }

   public synchronized List<Long> expireOverdue() {
      long now = this.clock.getAsLong();
      List<Long> expired = new ArrayList<>();

      for (AsyncJobRegistry.JobState state : this.jobs.values()) {
         if (!state.init_Z() && now >= state.deadline()) {
            expired.add(state.id());
         }
      }

      return expired;
   }

   public synchronized List<Long> pendingIds() {
      List<Long> pending = new ArrayList<>();

      for (AsyncJobRegistry.JobState state : this.jobs.values()) {
         if (!state.init_Z()) {
            pending.add(state.id());
         }
      }

      return pending;
   }

   public enum JobStatus {
      PENDING,
      SUCCEEDED,
      FAILED,
      CANCELLED;
   }

   public record JobState(long id, String label, AsyncJobRegistry.JobStatus status, boolean init_Z, boolean load_Z, long deadline) {
      public boolean init_Z() {
         return this.status != AsyncJobRegistry.JobStatus.PENDING;
      }

      public boolean load_Z() {
         return this.init_Z;
      }

      public boolean read() {
         return this.load_Z;
      }
   }
}
