package autismclient.ai.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;

public final class MiningTargetSearch {
   private MiningTargetSearch() {
   }

   public static MiningTargetSearch.Inner_StreamUtil init(
      AiPathfinder.GridView grid,
      BlockPos start,
      List<MiningTargetSearch.Inner_DataUtil> candidates,
      AiPathfinder.PathOptions caps,
      double reach,
      double eyeHeight,
      long deadline
   ) {
      List<MiningTargetSearch.Inner_DataUtil> ordered = new ArrayList<>(candidates);
      ordered.sort(Comparator.<MiningTargetSearch.Inner_DataUtil>comparingDouble(c -> init(start, c, reach, eyeHeight)).thenComparingLong(c -> c.cell.asLong()));
      BlockPos best = null;
      double bestCost = 1.0 / 0.0;
      boolean pending = false;

      label74:
      for (MiningTargetSearch.Inner_DataUtil candidate : ordered) {
         if (!(init(start, candidate, reach, eyeHeight) >= bestCost)) {
            if (!Thread.currentThread().isInterrupted() && System.nanoTime() < deadline) {
               AiPathfinder.PathGoal goal = new InteractionGoal(candidate.cell, reach, eyeHeight).init(grid);
               BlockPos from = start;
               double cost = candidate.miningTicks;
               int constructionRemaining = caps.constructionBlocks();

               for (int segment = 0; segment < 5 && cost < bestCost; segment++) {
                  if (goal.isInGoal(from.getX(), from.getY(), from.getZ())) {
                     best = candidate.cell;
                     bestCost = cost;
                     continue label74;
                  }

                  AiPathfinder.SearchSession search = new AiPathfinder.SearchSession(
                     grid, from, goal, caps.withConstructionBlocks(constructionRemaining), Set.of()
                  );
                  boolean finished = false;

                  while (System.nanoTime() < deadline && !Thread.currentThread().isInterrupted() && !(finished = search.init_Z_I(256))) {
                  }

                  if (!finished) {
                     return new MiningTargetSearch.Inner_StreamUtil(best, bestCost, pending, true);
                  }

                  AiPathfinder.PathResult path = search.init_ClassCache_PacketU();
                  pending |= path.hitPending();
                  if (path.movements().isEmpty()) {
                     continue label74;
                  }

                  for (AiPathfinder.UiHelper move : path.movements()) {
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

            return new MiningTargetSearch.Inner_StreamUtil(best, bestCost, pending, true);
         }
         break;
      }

      return new MiningTargetSearch.Inner_StreamUtil(best, bestCost, pending, false);
   }

   private static double init(BlockPos start, MiningTargetSearch.Inner_DataUtil candidate, double reach, double eyeHeight) {
      return candidate.miningTicks + new InteractionGoal(candidate.cell, reach, eyeHeight).heuristic(start.getX(), start.getY(), start.getZ());
   }

   public record Inner_DataUtil(BlockPos cell, double miningTicks) {
   }

   public record Inner_StreamUtil(BlockPos target, double totalTicks, boolean pending, boolean timedOut) {
   }
}
