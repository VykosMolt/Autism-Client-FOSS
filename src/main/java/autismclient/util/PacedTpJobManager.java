package autismclient.util;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;

public final class PacedTpJobManager {
   private static final long init_J = 300000L;
   private static final AtomicLong init_AtomicLong = new AtomicLong();
   private static final AsyncJobRegistry init_dc = new AsyncJobRegistry(System::currentTimeMillis);

   private PacedTpJobManager() {
   }

   public static PacedTpJobManager.Inner_StreamUtil init(autismclient.util.macro.PacedTpAction action, String owner) {
      long id = init_AtomicLong.incrementAndGet();
      PacedTpJobManager.Inner_StreamUtil handle = new PacedTpJobManager.Inner_StreamUtil(id, owner == null ? "" : owner);
      init_dc.init(id, handle.init_Str(), 300000L);

      autismclient.util.g.PkgCls_a.Inner_StateCache result;
      try {
         result = autismclient.util.g.PkgCls_a.init_a_StateCache_w_bl_StreamUtil(action, handle);
      } catch (RuntimeException | Error error) {
         init_dc.init(id, AsyncJobRegistry.JobStatus.FAILED);
         throw error;
      }

      if (result != null && result.init_Z()) {
         init_dc.requestCancel(id);
      } else {
         init_dc.init(id, AsyncJobRegistry.JobStatus.FAILED);
      }

      return handle;
   }

   public static PacedTpJobManager.Inner_CachePool status(long id) {
      AsyncJobRegistry.JobState state = init_dc.state(id);
      return new PacedTpJobManager.Inner_CachePool(state.id(), state.label(), init(state.status()), state.load_Z(), state.read(), state.deadline());
   }

   public static PacedTpJobManager.Inner_DataUtil init_bl_DataUtil(long id) {
      return status(id).state();
   }

   public static void init(long id, String reason) {
      if (init_bl_DataUtil(id) == PacedTpJobManager.Inner_DataUtil.PENDING) {
         autismclient.util.g.PkgCls_a.init_V_J_Str(id, reason);
         init_dc.init(id, AsyncJobRegistry.JobStatus.CANCELLED);
      }
   }

   public static void init(Minecraft client) {
      for (long id : init_dc.expireOverdue()) {
         autismclient.util.g.PkgCls_a.load(id, "timeout");
         init_dc.init(id, AsyncJobRegistry.JobStatus.FAILED);
      }

      autismclient.util.g.PkgCls_a.init_V_Minecraft(client);
   }

   public static void init(String reason) {
      autismclient.util.g.PkgCls_a.init_V_Str(reason);

      for (long id : init_dc.pendingIds()) {
         init_dc.init(id, AsyncJobRegistry.JobStatus.CANCELLED);
      }
   }

   public static void init(long id, PacedTpJobManager.Inner_DataUtil completion) {
      init_dc.init(id, init(completion));
   }

   private static AsyncJobRegistry.JobStatus init(PacedTpJobManager.Inner_DataUtil completion) {
      return completion == null ? null : AsyncJobRegistry.JobStatus.valueOf(completion.name());
   }

   private static PacedTpJobManager.Inner_DataUtil init(AsyncJobRegistry.JobStatus completion) {
      return PacedTpJobManager.Inner_DataUtil.valueOf(completion.name());
   }

   public record Inner_CachePool(long init_J, String init_Str, PacedTpJobManager.Inner_DataUtil state, boolean init_Z, boolean load_Z, long load_J) {
   }

   public enum Inner_DataUtil {
      PENDING,
      SUCCEEDED,
      FAILED,
      CANCELLED;
   }

   public record Inner_StreamUtil(long id, String init_Str) {
   }
}
