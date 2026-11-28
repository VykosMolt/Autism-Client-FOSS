package autismclient.ai.print;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;

final class PrintFrontierWalk {
   private final PrintNav.ReachSearch init_TokenStore_NetHelp;
   private final int chunkMinX;
   private final int chunkMinZ;
   private final int chunkMaxX;
   private final int write;
   private final double init_D;
   final PrintNav.ReachResult result;
   LongOpenHashSet avoidCells;
   boolean done;
   boolean load_Z;
   long matchedGoalKey = -9223372036854775808L;
   private long key = -9223372036854775808L;
   private double load_D = 1.0 / 0.0;
   private double cost = 1.0 / 0.0;

   PrintFrontierWalk(PrintView view, BlockPos start, PrintNav.SearchBounds searchBounds, int chunkMinX, int chunkMinZ, int chunkMaxX, int chunkMaxZ, int maxNodes) {
      this.chunkMinX = chunkMinX;
      this.chunkMinZ = chunkMinZ;
      this.chunkMaxX = chunkMaxX;
      this.write = chunkMaxZ;
      this.init_D = this.init_D(start.asLong());
      boolean supported = !Double.isNaN(view.standingHeightAt(start.getX(), start.getY(), start.getZ()));
      this.init_TokenStore_NetHelp = new PrintNav.ReachSearch(view, start, searchBounds, maxNodes, null, null, this::init_Z);
      this.result = this.init_TokenStore_NetHelp.result;
      if (!supported) {
         this.done = this.load_Z = true;
      }
   }

   private double init_D(long key) {
      int x = BlockPos.getX(key);
      int z = BlockPos.getZ(key);
      double dx = x < this.chunkMinX ? (double)this.chunkMinX - x : (x > this.chunkMaxX ? (double)x - this.chunkMaxX : 0.0);
      double dz = z < this.chunkMinZ ? (double)this.chunkMinZ - z : (z > this.write ? (double)z - this.write : 0.0);
      return Math.hypot(dx, dz);
   }

   private boolean init_Z(long key) {
      double distance = this.init_D(key);
      double cost = this.init_TokenStore_NetHelp.result.costOf(key);
      if (distance < this.load_D - 1.0E-7 || Math.abs(distance - this.load_D) <= 1.0E-7 && cost < this.cost) {
         this.key = key;
         this.load_D = distance;
         this.cost = cost;
      }

      return distance == 0.0 || this.init_D - distance >= 8.0;
   }

   void advance(long workBudget) {
      if (!this.done) {
         this.init_TokenStore_NetHelp.avoidCells = this.avoidCells;
         this.init_TokenStore_NetHelp.advance(workBudget);
         if (this.init_TokenStore_NetHelp.finished) {
            this.done = true;
            if (this.init_TokenStore_NetHelp.matchedGoalKey != -9223372036854775808L) {
               this.matchedGoalKey = this.init_TokenStore_NetHelp.matchedGoalKey;
            } else if (this.key != -9223372036854775808L && this.init_D - this.load_D >= 2.0) {
               this.matchedGoalKey = this.key;
            }

            this.load_Z = this.matchedGoalKey == -9223372036854775808L;
         }
      }
   }
}
