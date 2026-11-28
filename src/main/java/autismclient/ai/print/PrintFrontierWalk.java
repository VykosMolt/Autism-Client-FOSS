package autismclient.ai.print;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;

final class PrintFrontierWalk {
   private final PrintNav.ReachSearch search;
   private final int chunkMinX;
   private final int chunkMinZ;
   private final int chunkMaxX;
   private final int chunkMaxZ;
   private final double startDistance;
   final PrintNav.ReachResult result;
   LongOpenHashSet avoidCells;
   boolean done;
   boolean noGoal;
   long matchedGoalKey = -9223372036854775808L;
   private long key = -9223372036854775808L;
   private double bestDistance = 1.0 / 0.0;
   private double cost = 1.0 / 0.0;

   PrintFrontierWalk(PrintView view, BlockPos start, PrintNav.SearchBounds searchBounds, int chunkMinX, int chunkMinZ, int chunkMaxX, int chunkMaxZ, int maxNodes) {
      this.chunkMinX = chunkMinX;
      this.chunkMinZ = chunkMinZ;
      this.chunkMaxX = chunkMaxX;
      this.chunkMaxZ = chunkMaxZ;
      this.startDistance = this.chunkDistance(start.asLong());
      boolean supported = !Double.isNaN(view.standingHeightAt(start.getX(), start.getY(), start.getZ()));
      this.search = new PrintNav.ReachSearch(view, start, searchBounds, maxNodes, null, null, this::acceptGoal);
      this.result = this.search.result;
      if (!supported) {
         this.done = this.noGoal = true;
      }
   }

   private double chunkDistance(long key) {
      int x = BlockPos.getX(key);
      int z = BlockPos.getZ(key);
      double dx = x < this.chunkMinX ? (double)this.chunkMinX - x : (x > this.chunkMaxX ? (double)x - this.chunkMaxX : 0.0);
      double dz = z < this.chunkMinZ ? (double)this.chunkMinZ - z : (z > this.chunkMaxZ ? (double)z - this.chunkMaxZ : 0.0);
      return Math.hypot(dx, dz);
   }

   private boolean acceptGoal(long key) {
      double distance = this.chunkDistance(key);
      double cost = this.search.result.costOf(key);
      if (distance < this.bestDistance - 1.0E-7 || Math.abs(distance - this.bestDistance) <= 1.0E-7 && cost < this.cost) {
         this.key = key;
         this.bestDistance = distance;
         this.cost = cost;
      }

      return distance == 0.0 || this.startDistance - distance >= 8.0;
   }

   void advance(long workBudget) {
      if (!this.done) {
         this.search.avoidCells = this.avoidCells;
         this.search.advance(workBudget);
         if (this.search.finished) {
            this.done = true;
            if (this.search.matchedGoalKey != -9223372036854775808L) {
               this.matchedGoalKey = this.search.matchedGoalKey;
            } else if (this.key != -9223372036854775808L && this.startDistance - this.bestDistance >= 2.0) {
               this.matchedGoalKey = this.key;
            }

            this.noGoal = this.matchedGoalKey == -9223372036854775808L;
         }
      }
   }
}
