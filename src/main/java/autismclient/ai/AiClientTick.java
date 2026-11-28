package autismclient.ai;

import autismclient.AutismClientAddon;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;

public final class AiClientTick {
   private static final AiTickQueue QUEUE = new AiTickQueue();
   private static final Map<String, Long> auditLastFailureLog = new HashMap<>();

   private AiClientTick() {
   }

   public static boolean isOnTickThread() {
      return QUEUE.isOnTickThread();
   }

   static void execute(Runnable action) {
      QUEUE.execute(action);
   }

   static <T> CompletableFuture<T> supply(Supplier<T> action) {
      return QUEUE.supply(action);
   }

   public static void tick(Minecraft client) {
      QUEUE.runPhase(() -> {
         try {
            AiMacroTools.tick(client);
            AiDirectOps.tickClient(client);
            AiService service = AiService.getExistingInstance();
            if (service != null) {
               service.onClientTick();
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
