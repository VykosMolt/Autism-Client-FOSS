package autismclient.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class AiCombatPriority {
   static final int init = 12;

   private AiCombatPriority() {
   }

   static boolean keepTarget(boolean alive, boolean inRadius, boolean deferred) {
      return alive && inRadius && !deferred;
   }

   static boolean shouldSwitchTarget(double currentGap, double otherGap, int heldTicks, boolean currentHittable, boolean otherHittable) {
      if (!otherHittable) {
         return false;
      } else if (!currentHittable) {
         return true;
      } else {
         return currentGap >= 1.0 && otherGap < 0.5 ? true : heldTicks >= 12 && otherGap + 0.35 < currentGap;
      }
   }

   static boolean isKillGoalMet(int killed, int requested, boolean untilClear, boolean noThreatsObserved) {
      return killed >= requested || untilClear && noThreatsObserved;
   }

   static boolean canReach(double standingGap, double jumpGap, double reach, boolean seen, double horizontal) {
      return seen && (standingGap <= reach || horizontal < 3.5 && jumpGap <= reach);
   }

   static boolean init(AiGuard.GuardAction action) {
      return action == AiGuard.GuardAction.EAT || action == AiGuard.GuardAction.TIDY;
   }

   static boolean init(int hostiles, int ranged) {
      return hostiles > 0 || ranged > 0;
   }

   static boolean init(AiThreats.Inner_DataUtil profile) {
      boolean var10000;
      switch (profile) {
         case ZOMBIE:
         case BABY_ZOMBIE:
         case SKELETON:
         case CROSSBOW:
         case CREEPER:
         case BLAZE:
         case WITHER_SKELETON:
         case BRUTE:
         case CUBE:
         case SMALL:
         case WITCH:
         case PHANTOM:
         case OTHER_HOSTILE:
         case AVOID:
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   static boolean init_Z_X_DataUtil(AiGuard.GuardAction active, AiGuard.GuardAction next, int hostiles) {
      return init(active) && hostiles > 0 || active == AiGuard.GuardAction.FIGHT && next == AiGuard.GuardAction.FLEE;
   }

   static float init_F_F(float current, Float preferred, List<Float> shots, double halfCone) {
      if (shots.isEmpty()) {
         return preferred == null ? current : preferred;
      }

      if (halfCone > 0.0 && halfCone < 90.0 && Float.isFinite(current) && !shots.stream().anyMatch(yaw -> yaw == null || !Float.isFinite(yaw))) {
         if (preferred != null && Float.isFinite(preferred) && coversAll(preferred, shots, halfCone)) {
            return preferred;
         }

         if (coversAll(current, shots, halfCone)) {
            return current;
         }

         double anchor = shots.getFirst().floatValue();
         List<AiCombatPriority.Inner_DataUtil> edges = new ArrayList<>(shots.size() * 2 + 2);
         edges.add(new AiCombatPriority.Inner_DataUtil(-halfCone, 0, 0));
         edges.add(new AiCombatPriority.Inner_DataUtil(halfCone, 0, 0));

         for (float shot : shots) {
            double relative = init(shot - anchor);

            for (int turn = -1; turn <= 1; turn++) {
               double low = Math.max(-halfCone, relative + turn * 360 - halfCone);
               double high = Math.min(halfCone, relative + turn * 360 + halfCone);
               if (low <= high) {
                  edges.add(new AiCombatPriority.Inner_DataUtil(low, 1, 0));
                  edges.add(new AiCombatPriority.Inner_DataUtil(high, 0, 1));
               }
            }
         }

         edges.sort(Comparator.comparingDouble(AiCombatPriority.Inner_DataUtil::at));
         double best = 0.0;
         double bestTurn = 1.7976931348623157E308;
         int active = 0;
         int bestCount = -1;
         int i = 0;

         while (i < edges.size()) {
            double at = edges.get(i).at();
            int starts = 0;
            int ends = 0;

            while (i < edges.size() && edges.get(i).at() == at) {
               starts += edges.get(i).starts();
               ends += edges.get(i).ends();
               i++;
            }

            active += starts;
            double distance = Math.abs(init(anchor + at - current));
            if (active > bestCount || active == bestCount && distance < bestTurn) {
               bestCount = active;
               bestTurn = distance;
               best = at;
            }

            active -= ends;
            if (i < edges.size()) {
               double end = edges.get(i).at();

               for (int turn = -1; turn <= 1; turn++) {
                  double nearest = Math.max(at, Math.min(end, init(current - anchor) + turn * 360));
                  distance = Math.abs(init(anchor + nearest - current));
                  if (active > bestCount || active == bestCount && distance < bestTurn) {
                     bestCount = active;
                     bestTurn = distance;
                     best = nearest;
                  }
               }
            }
         }

         return (float)init(anchor + best);
      } else {
         throw new IllegalArgumentException("Finite shot yaws and a shield half cone between 0 and 90 required");
      }
   }

   static boolean coversAll(float yaw, List<Float> shots, double halfCone) {
      for (float shot : shots) {
         if (Math.abs(init(shot - yaw)) > halfCone + 1.0E-5) {
            return false;
         }
      }

      return true;
   }

   static double init(double degrees) {
      double angle = degrees % 360.0;
      return angle >= 180.0 ? angle - 360.0 : (angle < -180.0 ? angle + 360.0 : angle);
   }

   private record Inner_DataUtil(double at, int starts, int ends) {
   }
}
