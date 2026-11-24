package autismclient.ai.a;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;

public final class s {
   private s() {
   }

   public static s.Inner_StreamUtil init(
      ClassCache.Inner_StateCache grid,
      BlockPos start,
      List<s.Inner_DataUtil> candidates,
      ClassCache.Inner_DataUtil caps,
      double reach,
      double eyeHeight,
      long deadline
   ) {
      List<s.Inner_DataUtil> ordered = new ArrayList<>(candidates);
      ordered.sort(Comparator.<s.Inner_DataUtil>comparingDouble(c -> init(start, c, reach, eyeHeight)).thenComparingLong(c -> c.cell.asLong()));
      BlockPos best = null;
      double bestCost = 1.0 / 0.0;
      boolean pending = false;

      label74:
      for (s.Inner_DataUtil candidate : ordered) {
         if (!(init(start, candidate, reach, eyeHeight) >= bestCost)) {
            if (!Thread.currentThread().isInterrupted() && System.nanoTime() < deadline) {
               ClassCache.Inner_JsonUtil goal = new q(candidate.cell, reach, eyeHeight).init(grid);
               BlockPos from = start;
               double cost = candidate.miningTicks;
               int constructionRemaining = caps.constructionBlocks();

               for (int segment = 0; segment < 5 && cost < bestCost; segment++) {
                  if (goal.isInGoal(from.getX(), from.getY(), from.getZ())) {
                     best = candidate.cell;
                     bestCost = cost;
                     continue label74;
                  }

                  ClassCache.Inner_TextFormat search = new ClassCache.Inner_TextFormat(
                     grid, from, goal, caps.withConstructionBlocks(constructionRemaining), Set.of()
                  );
                  boolean finished = false;

                  while (System.nanoTime() < deadline && !Thread.currentThread().isInterrupted() && !(finished = search.init_Z_I(256))) {
                  }

                  if (!finished) {
                     return new s.Inner_StreamUtil(best, bestCost, pending, true);
                  }

                  ClassCache.Inner_PacketUtil path = search.init_ClassCache_PacketU();
                  pending |= path.hitPending();
                  if (path.movements().isEmpty()) {
                     continue label74;
                  }

                  for (ClassCache.UiHelper move : path.movements()) {
                     cost += move.costTicks();
                     constructionRemaining -= move.cellsToPlace().size();
                  }

                  from = path.destination();
                  if (path.reachesGoal() && cost < bestCost) {
                     best = candidate.cell;
                     bestCost = cost;
                     continue label74;
                  }

                  if (path.hitPending()) {
                     continue label74;
                  }
               }
               continue;
            }

            return new s.Inner_StreamUtil(best, bestCost, pending, true);
         }
         break;
      }

      return new s.Inner_StreamUtil(best, bestCost, pending, false);
   }

   private static double init(BlockPos start, s.Inner_DataUtil candidate, double reach, double eyeHeight) {
      return candidate.miningTicks + new q(candidate.cell, reach, eyeHeight).heuristic(start.getX(), start.getY(), start.getZ());
   }

   public record Inner_DataUtil(BlockPos cell, double miningTicks) {
   }

   public record Inner_StreamUtil(BlockPos target, double totalTicks, boolean pending, boolean timedOut) {
   }
}
