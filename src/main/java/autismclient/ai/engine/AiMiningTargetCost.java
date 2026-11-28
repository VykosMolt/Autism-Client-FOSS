package autismclient.ai.engine;

import java.util.List;

public final class AiMiningTargetCost {
   private AiMiningTargetCost() {
   }

   public static double estimateTicks(List<Double> breaks, int remaining, double movementTicks, double startTicks) {
      int useful = Math.min(Math.max(0, remaining), breaks.size());
      if (useful == 0) {
         return 1.0 / 0.0;
      }

      double ticks = Math.max(0.0, movementTicks) + Math.max(0.0, startTicks);

      for (int i = 0; i < useful; i++) {
         double cost = breaks.get(i);
         if (!Double.isFinite(cost)) {
            return 1.0 / 0.0;
         }

         ticks += Math.max(1.0, cost);
      }

      return ticks / useful;
   }

   public static int pickQuickest(List<AiMiningTargetCost.TargetChoice> choices) {
      double quickest = 1.0 / 0.0;

      for (AiMiningTargetCost.TargetChoice choice : choices) {
         quickest = Math.min(quickest, choice.ticksPerItem());
      }

      AiMiningTargetCost.TargetChoice best = null;

      for (AiMiningTargetCost.TargetChoice choice : choices) {
         if (Double.isFinite(choice.ticksPerItem())
            && !(choice.ticksPerItem() > quickest + 1.0)
            && (
               best == null
                  || choice.turnDegrees() < best.turnDegrees() - 1.0E-6
                  || Math.abs(choice.turnDegrees() - best.turnDegrees()) <= 1.0E-6
                     && (
                        choice.distanceSq() < best.distanceSq() - 1.0E-6
                           || Math.abs(choice.distanceSq() - best.distanceSq()) <= 1.0E-6 && choice.stableKey() < best.stableKey()
                     )
            )) {
            best = choice;
         }
      }

      return best == null ? -1 : best.index();
   }

   public record TargetChoice(int index, double ticksPerItem, double turnDegrees, double distanceSq, long stableKey) {
   }
}
