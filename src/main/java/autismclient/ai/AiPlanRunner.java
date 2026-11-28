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

   static List<AiPlanRunner.Step> orderSteps(List<AiPlanRunner.Step> steps) {
      Map<String, AiPlanRunner.Step> byId = new LinkedHashMap<>();

      for (AiPlanRunner.Step step : steps) {
         if (step.id().isBlank()) {
            throw new IllegalArgumentException("Every plan step needs a non-empty id");
         }

         if (byId.putIfAbsent(step.id(), step) != null) {
            throw new IllegalArgumentException("Duplicate plan step id '" + step.id() + "'");
         }
      }

      for (AiPlanRunner.Step step : steps) {
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

      for (AiPlanRunner.Step step : steps) {
         remaining.put(step.id(), step.needs().size());

         for (String need : step.needs()) {
            dependents.computeIfAbsent(need, key -> new ArrayList<>()).add(step.id());
         }
      }

      Deque<String> ready = new ArrayDeque<>();

      for (AiPlanRunner.Step step : steps) {
         if (remaining.get(step.id()) == 0) {
            ready.addLast(step.id());
         }
      }

      List<AiPlanRunner.Step> ordered = new ArrayList<>(steps.size());

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

   static AiPlanRunner.StepProgress nextRunnable(List<AiPlanRunner.StepProgress> plan) {
      Map<String, AiPlanRunner.StepProgress> byId = new LinkedHashMap<>();

      for (AiPlanRunner.StepProgress progress : plan) {
         byId.put(progress.step.id(), progress);
      }

      for (AiPlanRunner.StepProgress progress : plan) {
         if (progress.state == AiPlanRunner.StepState.PENDING) {
            for (String need : progress.step.needs()) {
               AiPlanRunner.StepProgress dependency = byId.get(need);
               if (dependency != null
                  && (dependency.state == AiPlanRunner.StepState.FAILED || dependency.state == AiPlanRunner.StepState.SKIPPED)) {
                  progress.state = AiPlanRunner.StepState.SKIPPED;
                  progress.message = "skipped: step '" + need + "' it depends on did not complete";
                  break;
               }
            }
         }
      }

      for (AiPlanRunner.StepProgress progress : plan) {
         if (progress.state == AiPlanRunner.StepState.PENDING) {
            boolean blocked = false;

            for (String need : progress.step.needs()) {
               AiPlanRunner.StepProgress dependency = byId.get(need);
               if (dependency != null && dependency.state != AiPlanRunner.StepState.DONE) {
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

   static boolean isFinished(List<AiPlanRunner.StepProgress> plan) {
      for (AiPlanRunner.StepProgress progress : plan) {
         if (progress.state == AiPlanRunner.StepState.PENDING || progress.state == AiPlanRunner.StepState.RUNNING) {
            return false;
         }
      }

      return true;
   }

   static boolean recordResult(AiPlanRunner.StepProgress progress, boolean succeeded, String message, AiPlanRunner.FailurePolicy policy) {
      progress.message = message;
      if (succeeded) {
         progress.state = AiPlanRunner.StepState.DONE;
         return false;
      } else if (policy == AiPlanRunner.FailurePolicy.RETRY && progress.attempts < 2) {
         progress.state = AiPlanRunner.StepState.PENDING;
         return false;
      } else {
         progress.state = AiPlanRunner.StepState.FAILED;
         return policy == AiPlanRunner.FailurePolicy.STOP;
      }
   }

   static boolean init(AiPlanRunner.StepProgress progress, String message, AiPlanRunner.FailurePolicy policy) {
      progress.message = message;
      progress.state = AiPlanRunner.StepState.FAILED;
      return policy == AiPlanRunner.FailurePolicy.STOP;
   }

   static Map<String, Integer> tally(List<AiPlanRunner.StepProgress> plan) {
      Map<String, Integer> counts = new LinkedHashMap<>();

      for (AiPlanRunner.StepState state : AiPlanRunner.StepState.values()) {
         counts.put(state.name().toLowerCase(Locale.ROOT), 0);
      }

      for (AiPlanRunner.StepProgress progress : plan) {
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

   static Set<String> pendingIds(List<AiPlanRunner.StepProgress> plan) {
      Set<String> ids = new LinkedHashSet<>();

      for (AiPlanRunner.StepProgress progress : plan) {
         if (progress.state == AiPlanRunner.StepState.PENDING || progress.state == AiPlanRunner.StepState.SKIPPED) {
            ids.add(progress.step.id());
         }
      }

      return ids;
   }

   static Set<String> doneIds(List<AiPlanRunner.StepProgress> plan) {
      Set<String> ids = new HashSet<>();

      for (AiPlanRunner.StepProgress progress : plan) {
         if (progress.state == AiPlanRunner.StepState.DONE) {
            ids.add(progress.step.id());
         }
      }

      return ids;
   }

   record Step(String id, String capability, String argumentsJson, List<String> needs) {
      Step {
         needs = List.copyOf(needs);
      }
   }

   enum StepState {
      PENDING,
      RUNNING,
      DONE,
      FAILED,
      SKIPPED;
   }

   enum FailurePolicy {
      STOP,
      CONTINUE,
      RETRY;
   }

   static final class StepProgress {
      final AiPlanRunner.Step step;
      AiPlanRunner.StepState state = AiPlanRunner.StepState.PENDING;
      String message = "";
      int attempts;

      StepProgress(AiPlanRunner.Step step) {
         this.step = step;
      }
   }
}
