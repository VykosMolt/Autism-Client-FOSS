package autismclient.util;

import java.util.List;

public final class FarmActionScheduler {
   private FarmActionScheduler() {
   }

   public static <T> T choose(List<FarmActionScheduler.ActionOption<T>> options, float yaw, float pitch, int selected, T previous) {
      boolean urgent = options.stream().anyMatch(FarmActionScheduler.ActionOption::urgent);
      boolean planting = options.stream().anyMatch(o -> init(o.kind()));
      T best = null;
      double bestCost = 1.0 / 0.0;

      for (FarmActionScheduler.ActionOption<T> first : options) {
         if (!urgent || first.urgent()) {
            double cost = init(yaw, pitch, selected, first) + init(first.kind(), planting);
            int nextSlot = first.slot() < 0 ? selected : first.slot();
            double next = 1.0 / 0.0;

            for (FarmActionScheduler.ActionOption<T> second : options) {
               if (first != second && !first.target().equals(second.target())) {
                  next = Math.min(next, init(first.yaw(), first.pitch(), nextSlot, second));
               }
            }

            if (Double.isFinite(next)) {
               cost += 0.65 * next;
            }

            if (first.target().equals(previous)) {
               cost -= 0.6;
            }

            if (cost < bestCost) {
               bestCost = cost;
               best = first.target();
            }
         }
      }

      return best;
   }

   private static boolean init(FarmActionScheduler.ActionKind kind) {
      return kind == FarmActionScheduler.ActionKind.REPLANT || kind == FarmActionScheduler.ActionKind.REPLANT_RECENT;
   }

   private static double init(FarmActionScheduler.ActionKind kind, boolean planting) {
      return kind == FarmActionScheduler.ActionKind.TILL ? 8.0 : (kind == FarmActionScheduler.ActionKind.BONEMEAL && planting ? 6.0 : 0.0);
   }

   private static double init(float yaw, float pitch, int selected, FarmActionScheduler.ActionOption<?> next) {
      double yawDelta = Math.abs(Math.IEEEremainder(next.yaw() - yaw, 360.0));
      double turn = Math.hypot(yawDelta, next.pitch() - pitch) / 20.0;
      double change = next.slot() >= 0 && next.slot() != selected ? 2.0 : 0.0;
      double use = next.kind() == FarmActionScheduler.ActionKind.HARVEST ? 1.0 : 4.0;
      return turn + change + use;
   }

   public enum ActionKind {
      HARVEST,
      REPLANT_RECENT,
      BONEMEAL,
      REPLANT,
      TILL;
   }

   public record ActionOption<T>(T target, FarmActionScheduler.ActionKind kind, float yaw, float pitch, int slot, boolean urgent) {
   }
}
