package autismclient.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class AiPlanRunner {
   private AiPlanRunner() {
   }

   static List<AiPlanRunner.Inner_BufferHelper> orderSteps(List<AiPlanRunner.Inner_BufferHelper> steps) {
      Map<String, AiPlanRunner.Inner_BufferHelper> byId = new LinkedHashMap<>();

      for (AiPlanRunner.Inner_BufferHelper step : steps) {
         if (step.id().isBlank()) {
            throw new IllegalArgumentException("Every plan step needs a non-empty id");
         }

         if (byId.putIfAbsent(step.id(), step) != null) {
            throw new IllegalArgumentException("Duplicate plan step id '" + step.id() + "'");
         }
      }

      for (AiPlanRunner.Inner_BufferHelper step : steps) {
         for (String need : step.needs()) {
            if (!byId.containsKey(need)) {
               throw new IllegalArgumentException("Step '" + step.id() + "' needs unknown step '" + need + "'");
            }

            if (need.equals(step.id())) {
               throw new IllegalArgumentException("Step '" + step.id() + "' cannot depend on itself");
            }
         }
      }

      Map<String, Integer> remaining = new HashMap<>();
      Map<String, List<String>> dependents = new HashMap<>();

      for (AiPlanRunner.Inner_BufferHelper step : steps) {
         remaining.put(step.id(), step.needs().size());

         for (String need : step.needs()) {
            dependents.computeIfAbsent(need, key -> new ArrayList<>()).add(step.id());
         }
      }

      Deque<String> ready = new ArrayDeque<>();

      for (AiPlanRunner.Inner_BufferHelper step : steps) {
         if (remaining.get(step.id()) == 0) {
            ready.addLast(step.id());
         }
      }

      List<AiPlanRunner.Inner_BufferHelper> ordered = new ArrayList<>(steps.size());

      while (!ready.isEmpty()) {
         String id = ready.pollFirst();
         ordered.add(byId.get(id));

         for (String dependent : dependents.getOrDefault(id, List.of())) {
            if (remaining.merge(dependent, -1, Integer::sum) == 0) {
               ready.addLast(dependent);
            }
         }
      }

      if (ordered.size() != steps.size()) {
         Set<String> stuck = new LinkedHashSet<>(byId.keySet());
         ordered.forEach(step -> stuck.remove(step.id()));
         throw new IllegalArgumentException("Plan steps form a dependency cycle: " + String.join(", ", stuck));
      } else {
         return List.copyOf(ordered);
      }
   }

   static AiPlanRunner.Inner_StreamUtil nextRunnable(List<AiPlanRunner.Inner_StreamUtil> plan) {
      Map<String, AiPlanRunner.Inner_StreamUtil> byId = new LinkedHashMap<>();

      for (AiPlanRunner.Inner_StreamUtil progress : plan) {
         byId.put(progress.step.id(), progress);
      }

      for (AiPlanRunner.Inner_StreamUtil progress : plan) {
         if (progress.state == AiPlanRunner.Inner_CachePool.PENDING) {
            for (String need : progress.step.needs()) {
               AiPlanRunner.Inner_StreamUtil dependency = byId.get(need);
               if (dependency != null
                  && (dependency.state == AiPlanRunner.Inner_CachePool.FAILED || dependency.state == AiPlanRunner.Inner_CachePool.SKIPPED)) {
                  progress.state = AiPlanRunner.Inner_CachePool.SKIPPED;
                  progress.message = "skipped: step '" + need + "' it depends on did not complete";
                  break;
               }
            }
         }
      }

      for (AiPlanRunner.Inner_StreamUtil progress : plan) {
         if (progress.state == AiPlanRunner.Inner_CachePool.PENDING) {
            boolean blocked = false;

            for (String need : progress.step.needs()) {
               AiPlanRunner.Inner_StreamUtil dependency = byId.get(need);
               if (dependency != null && dependency.state != AiPlanRunner.Inner_CachePool.DONE) {
                  blocked = true;
               }
            }

            if (!blocked) {
               return progress;
            }
         }
      }

      return null;
   }

   static boolean isFinished(List<AiPlanRunner.Inner_StreamUtil> plan) {
      for (AiPlanRunner.Inner_StreamUtil progress : plan) {
         if (progress.state == AiPlanRunner.Inner_CachePool.PENDING || progress.state == AiPlanRunner.Inner_CachePool.RUNNING) {
            return false;
         }
      }

      return true;
   }

   static boolean recordResult(AiPlanRunner.Inner_StreamUtil progress, boolean succeeded, String message, AiPlanRunner.Inner_DataUtil policy) {
      progress.message = message;
      if (succeeded) {
         progress.state = AiPlanRunner.Inner_CachePool.DONE;
         return false;
      } else if (policy == AiPlanRunner.Inner_DataUtil.RETRY && progress.attempts < 2) {
         progress.state = AiPlanRunner.Inner_CachePool.PENDING;
         return false;
      } else {
         progress.state = AiPlanRunner.Inner_CachePool.FAILED;
         return policy == AiPlanRunner.Inner_DataUtil.STOP;
      }
   }

   static boolean init(AiPlanRunner.Inner_StreamUtil progress, String message, AiPlanRunner.Inner_DataUtil policy) {
      progress.message = message;
      progress.state = AiPlanRunner.Inner_CachePool.FAILED;
      return policy == AiPlanRunner.Inner_DataUtil.STOP;
   }

   static Map<String, Integer> tally(List<AiPlanRunner.Inner_StreamUtil> plan) {
      Map<String, Integer> counts = new LinkedHashMap<>();

      for (AiPlanRunner.Inner_CachePool state : AiPlanRunner.Inner_CachePool.values()) {
         counts.put(state.name().toLowerCase(Locale.ROOT), 0);
      }

      for (AiPlanRunner.Inner_StreamUtil progress : plan) {
         counts.merge(progress.state.name().toLowerCase(Locale.ROOT), 1, Integer::sum);
      }

      return counts;
   }

   static void requireGameCapability(String capability) {
      if (capability == null || !capability.startsWith("game:")) {
         throw new IllegalArgumentException(
            "Plan steps may only call game: capabilities; '" + capability + "' is not one. Use perform directly for modules, macros and commands."
         );
      }
   }

   static Set<String> pendingIds(List<AiPlanRunner.Inner_StreamUtil> plan) {
      Set<String> ids = new LinkedHashSet<>();

      for (AiPlanRunner.Inner_StreamUtil progress : plan) {
         if (progress.state == AiPlanRunner.Inner_CachePool.PENDING || progress.state == AiPlanRunner.Inner_CachePool.SKIPPED) {
            ids.add(progress.step.id());
         }
      }

      return ids;
   }

   static Set<String> doneIds(List<AiPlanRunner.Inner_StreamUtil> plan) {
      Set<String> ids = new HashSet<>();

      for (AiPlanRunner.Inner_StreamUtil progress : plan) {
         if (progress.state == AiPlanRunner.Inner_CachePool.DONE) {
            ids.add(progress.step.id());
         }
      }

      return ids;
   }

   record Inner_BufferHelper(String id, String capability, String argumentsJson, List<String> needs) {
      Inner_BufferHelper {
         needs = List.copyOf(needs);
      }
   }

   enum Inner_CachePool {
      PENDING,
      RUNNING,
      DONE,
      FAILED,
      SKIPPED;
   }

   enum Inner_DataUtil {
      STOP,
      CONTINUE,
      RETRY;
   }

   static final class Inner_StreamUtil {
      final AiPlanRunner.Inner_BufferHelper step;
      AiPlanRunner.Inner_CachePool state = AiPlanRunner.Inner_CachePool.PENDING;
      String message = "";
      int attempts;

      Inner_StreamUtil(AiPlanRunner.Inner_BufferHelper step) {
         this.step = step;
      }
   }
}
