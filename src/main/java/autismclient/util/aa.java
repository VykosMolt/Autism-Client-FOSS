package autismclient.util;

import java.util.List;

public final class aa {
   private aa() {
   }

   public static <T> T init(List<aa.Inner_StreamUtil<T>> options, float yaw, float pitch, int selected, T previous) {
      boolean urgent = options.stream().anyMatch(aa.Inner_StreamUtil::init_Z);
      boolean planting = options.stream().anyMatch(o -> init(o.init_aa_DataUtil()));
      T best = null;
      double bestCost = 1.0 / 0.0;

      for (aa.Inner_StreamUtil<T> first : options) {
         if (!urgent || first.init_Z()) {
            double cost = init(yaw, pitch, selected, first) + init(first.init_aa_DataUtil(), planting);
            int nextSlot = first.init_I() < 0 ? selected : first.init_I();
            double next = 1.0 / 0.0;

            for (aa.Inner_StreamUtil<T> second : options) {
               if (first != second && !first.init_Obj().equals(second.init_Obj())) {
                  next = Math.min(next, init(first.init_F(), first.load(), nextSlot, second));
               }
            }

            if (Double.isFinite(next)) {
               cost += 0.65 * next;
            }

            if (first.init_Obj().equals(previous)) {
               cost -= 0.6;
            }

            if (cost < bestCost) {
               bestCost = cost;
               best = first.init_Obj();
            }
         }
      }

      return best;
   }

   private static boolean init(aa.Inner_DataUtil kind) {
      return kind == aa.Inner_DataUtil.REPLANT || kind == aa.Inner_DataUtil.REPLANT_RECENT;
   }

   private static double init(aa.Inner_DataUtil kind, boolean planting) {
      return kind == aa.Inner_DataUtil.TILL ? 8.0 : (kind == aa.Inner_DataUtil.BONEMEAL && planting ? 6.0 : 0.0);
   }

   private static double init(float yaw, float pitch, int selected, aa.Inner_StreamUtil<?> next) {
      double yawDelta = Math.abs(Math.IEEEremainder(next.init_F() - yaw, 360.0));
      double turn = Math.hypot(yawDelta, next.load() - pitch) / 20.0;
      double change = next.init_I() >= 0 && next.init_I() != selected ? 2.0 : 0.0;
      double use = next.init_aa_DataUtil() == aa.Inner_DataUtil.HARVEST ? 1.0 : 4.0;
      return turn + change + use;
   }

   public enum Inner_DataUtil {
      HARVEST,
      REPLANT_RECENT,
      BONEMEAL,
      REPLANT,
      TILL;
   }

   public record Inner_StreamUtil<T>(T init_Obj, aa.Inner_DataUtil init_aa_DataUtil, float init_F, float load, int init_I, boolean init_Z) {
   }
}
