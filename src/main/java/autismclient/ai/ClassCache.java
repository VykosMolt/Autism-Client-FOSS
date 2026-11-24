package autismclient.ai;

import autismclient.AutismClientAddon;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;

public final class ClassCache {
   private static final cw init = new cw();
   private static final Map<String, Long> auditLastFailureLog = new HashMap<>();

   private ClassCache() {
   }

   public static boolean init() {
      return init.init();
   }

   static void init_V_Runnable(Runnable action) {
      init.init_V_Runnable(action);
   }

   static <T> CompletableFuture<T> init_CompletableFuture_Supplier(Supplier<T> action) {
      return init.init_CompletableFuture_Supplier(action);
   }

   public static void init_V_Minecraft(Minecraft client) {
      init.init(() -> {
         try {
            am.init_V_Minecraft(client);
            o.init(client);
            cc service = cc.load_cc();
            if (service != null) {
               service.flush_V();
            }
         } catch (Throwable error) {
            auditLogFailure("AI tick failed", error);
         }
      }, error -> auditLogFailure("queued client action failed", error));
   }

   // Client thread only; at most one log line per source every 5 s.
   private static void auditLogFailure(String what, Throwable error) {
      long now = System.currentTimeMillis();
      Long last = auditLastFailureLog.get(what);
      if (last == null || now - last >= 5000L) {
         auditLastFailureLog.put(what, now);
         AutismClientAddon.LOG.warn("[AI] " + what, error);
      }
   }
}
