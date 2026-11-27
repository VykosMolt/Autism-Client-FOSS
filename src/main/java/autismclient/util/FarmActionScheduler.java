package autismclient.util;

import java.util.List;

public final class FarmActionScheduler {
   private FarmActionScheduler() {
   }

   public static <T> T init(List<FarmActionScheduler.Inner_StreamUtil<T>> options, float yaw, float pitch, int selected, T previous) {
      boolean urgent = options.stream().anyMatch(FarmActionScheduler.Inner_StreamUtil::urgent);
      boolean planting = options.stream().anyMatch(o -> init(o.kind()));
      T best = null;
      double bestCost = 1.0 / 0.0;

      for (FarmActionScheduler.Inner_StreamUtil<T> first : options) {
         if (!urgent || first.urgent()) {
            double cost = init(yaw, pitch, selected, first) + init(first.kind(), planting);
            int nextSlot = first.slot() < 0 ? selected : first.slot();
            double next = 1.0 / 0.0;

            for (FarmActionScheduler.Inner_StreamUtil<T> second : options) {
               if (first != second && !first.target().equals(second.target())) {
                  next = Math.min(next, init(first.yaw(), first.load(), nextSlot, second));
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

   private static boolean init(FarmActionScheduler.Inner_DataUtil kind) {
      return kind == FarmActionScheduler.Inner_DataUtil.REPLANT || kind == FarmActionScheduler.Inner_DataUtil.REPLANT_RECENT;
   }

   private static double init(FarmActionScheduler.Inner_DataUtil kind, boolean planting) {
      return kind == FarmActionScheduler.Inner_DataUtil.TILL ? 8.0 : (kind == FarmActionScheduler.Inner_DataUtil.BONEMEAL && planting ? 6.0 : 0.0);
   }

   private static double init(float yaw, float pitch, int selected, FarmActionScheduler.Inner_StreamUtil<?> next) {
      double yawDelta = Math.abs(Math.IEEEremainder(next.yaw() - yaw, 360.0));
      double turn = Math.hypot(yawDelta, next.load() - pitch) / 20.0;
      double change = next.slot() >= 0 && next.slot() != selected ? 2.0 : 0.0;
      double use = next.kind() == FarmActionScheduler.Inner_DataUtil.HARVEST ? 1.0 : 4.0;
      return turn + change + use;
   }

   public enum Inner_DataUtil {
      HARVEST,
      REPLANT_RECENT,
      BONEMEAL,
      REPLANT,
      TILL;
   }

   public record Inner_StreamUtil<T>(T target, FarmActionScheduler.Inner_DataUtil kind, float yaw, float load, int slot, boolean urgent) {
   }
}
