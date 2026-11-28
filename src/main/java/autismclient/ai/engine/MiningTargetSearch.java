package autismclient.ai.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;

public final class MiningTargetSearch {
   private MiningTargetSearch() {
   }

   public static MiningTargetSearch.SearchResult search(
      AiPathfinder.GridView grid,
      BlockPos start,
      List<MiningTargetSearch.Candidate> candidates,
      AiPathfinder.PathOptions caps,
      double reach,
      double eyeHeight,
      long deadline
   ) {
      List<MiningTargetSearch.Candidate> ordered = new ArrayList<>(candidates);
      ordered.sort(Comparator.<MiningTargetSearch.Candidate>comparingDouble(c -> cost(start, c, reach, eyeHeight)).thenComparingLong(c -> c.cell.asLong()));
      BlockPos best = null;
      double bestCost = 1.0 / 0.0;
      boolean pending = false;

      label74:
      for (MiningTargetSearch.Candidate candidate : ordered) {
         if (!(cost(start, candidate, reach, eyeHeight) >= bestCost)) {
            if (!Thread.currentThread().isInterrupted() && System.nanoTime() < deadline) {
               AiPathfinder.PathGoal goal = new InteractionGoal(candidate.cell, reach, eyeHeight).bindToGrid(grid);
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

                  while (System.nanoTime() < deadline && !Thread.currentThread().isInterrupted() && !(finished = search.stepCompat(256))) {
                  }

                  if (!finished) {
                     return new MiningTargetSearch.SearchResult(best, bestCost, pending, true);
                  }

                  AiPathfinder.PathResult path = search.resultCompat();
                  pending |= path.hitPending();
                  if (path.movements().isEmpty()) {
                     continue label74;
                  }

                  for (AiPathfinder.PathStep move : path.movements()) {
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

            return new MiningTargetSearch.SearchResult(best, bestCost, pending, true);
         }
         break;
      }

      return new MiningTargetSearch.SearchResult(best, bestCost, pending, false);
   }

   private static double cost(BlockPos start, MiningTargetSearch.Candidate candidate, double reach, double eyeHeight) {
      return candidate.miningTicks + new InteractionGoal(candidate.cell, reach, eyeHeight).heuristic(start.getX(), start.getY(), start.getZ());
   }

   public record Candidate(BlockPos cell, double miningTicks) {
   }

   public record SearchResult(BlockPos target, double totalTicks, boolean pending, boolean timedOut) {
   }
}
