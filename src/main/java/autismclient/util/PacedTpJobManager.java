package autismclient.util;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;

public final class PacedTpJobManager {
   private static final long init_J = 300000L;
   private static final AtomicLong JOB_IDS = new AtomicLong();
   private static final AsyncJobRegistry JOBS = new AsyncJobRegistry(System::currentTimeMillis);

   private PacedTpJobManager() {
   }

   public static PacedTpJobManager.JobHandle init(autismclient.util.macro.PacedTpAction action, String owner) {
      long id = JOB_IDS.incrementAndGet();
      PacedTpJobManager.JobHandle handle = new PacedTpJobManager.JobHandle(id, owner == null ? "" : owner);
      JOBS.register(id, handle.owner(), 300000L);

      autismclient.util.g.PkgCls_a.Inner_StateCache result;
      try {
         result = autismclient.util.g.PkgCls_a.init_a_StateCache_w_bl_StreamUtil(action, handle);
      } catch (RuntimeException | Error error) {
         JOBS.complete(id, AsyncJobRegistry.JobStatus.FAILED);
         throw error;
      }

      if (result != null && result.init_Z()) {
         JOBS.requestCancel(id);
      } else {
         JOBS.complete(id, AsyncJobRegistry.JobStatus.FAILED);
      }

      return handle;
   }

   public static PacedTpJobManager.JobStatus status(long id) {
      AsyncJobRegistry.JobState state = JOBS.state(id);
      return new PacedTpJobManager.JobStatus(state.id(), state.label(), fromRegistryStatus(state.status()), state.load_Z(), state.read(), state.deadline());
   }

   public static PacedTpJobManager.JobState stateOf(long id) {
      return status(id).state();
   }

   public static void cancel(long id, String reason) {
      if (stateOf(id) == PacedTpJobManager.JobState.PENDING) {
         autismclient.util.g.PkgCls_a.init_V_J_Str(id, reason);
         JOBS.complete(id, AsyncJobRegistry.JobStatus.CANCELLED);
      }
   }

   public static void tick(Minecraft client) {
      for (long id : JOBS.expireOverdue()) {
         autismclient.util.g.PkgCls_a.load(id, "timeout");
         JOBS.complete(id, AsyncJobRegistry.JobStatus.FAILED);
      }

      autismclient.util.g.PkgCls_a.init_V_Minecraft(client);
   }

   public static void cancelAll(String reason) {
      autismclient.util.g.PkgCls_a.init_V_Str(reason);

      for (long id : JOBS.pendingIds()) {
         JOBS.complete(id, AsyncJobRegistry.JobStatus.CANCELLED);
      }
   }

   public static void init(long id, PacedTpJobManager.JobState completion) {
      JOBS.complete(id, toRegistryStatus(completion));
   }

   private static AsyncJobRegistry.JobStatus toRegistryStatus(PacedTpJobManager.JobState completion) {
      return completion == null ? null : AsyncJobRegistry.JobStatus.valueOf(completion.name());
   }

   private static PacedTpJobManager.JobState fromRegistryStatus(AsyncJobRegistry.JobStatus completion) {
      return PacedTpJobManager.JobState.valueOf(completion.name());
   }

   public record JobStatus(long id, String label, PacedTpJobManager.JobState state, boolean init_Z, boolean load_Z, long deadline) {
   }

   public enum JobState {
      PENDING,
      SUCCEEDED,
      FAILED,
      CANCELLED;
   }

   public record JobHandle(long id, String owner) {
   }
}
