package autismclient.ai.print;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;

final class PrintFrontierWalk {
   private final PrintNav.Inner_NetHelper init_TokenStore_NetHelp;
   private final int init_I;
   private final int load_I;
   private final int read_I;
   private final int write;
   private final double init_D;
   final PrintNav.Inner_TaskQueue result;
   LongOpenHashSet avoidCells;
   boolean done;
   boolean load_Z;
   long matchedGoalKey = -9223372036854775808L;
   private long load_J = -9223372036854775808L;
   private double load_D = 1.0 / 0.0;
   private double read_D = 1.0 / 0.0;

   PrintFrontierWalk(PrintView view, BlockPos start, PrintNav.Inner_DataUtil searchBounds, int chunkMinX, int chunkMinZ, int chunkMaxX, int chunkMaxZ, int maxNodes) {
      this.init_I = chunkMinX;
      this.load_I = chunkMinZ;
      this.read_I = chunkMaxX;
      this.write = chunkMaxZ;
      this.init_D = this.init_D(start.asLong());
      boolean supported = !Double.isNaN(view.standingHeightAt(start.getX(), start.getY(), start.getZ()));
      this.init_TokenStore_NetHelp = new PrintNav.Inner_NetHelper(view, start, searchBounds, maxNodes, null, null, this::init_Z);
      this.result = this.init_TokenStore_NetHelp.result;
      if (!supported) {
         this.done = this.load_Z = true;
      }
   }

   private double init_D(long key) {
      int x = BlockPos.getX(key);
      int z = BlockPos.getZ(key);
      double dx = x < this.init_I ? (double)this.init_I - x : (x > this.read_I ? (double)x - this.read_I : 0.0);
      double dz = z < this.load_I ? (double)this.load_I - z : (z > this.write ? (double)z - this.write : 0.0);
      return Math.hypot(dx, dz);
   }

   private boolean init_Z(long key) {
      double distance = this.init_D(key);
      double cost = this.init_TokenStore_NetHelp.result.costOf(key);
      if (distance < this.load_D - 1.0E-7 || Math.abs(distance - this.load_D) <= 1.0E-7 && cost < this.read_D) {
         this.load_J = key;
         this.load_D = distance;
         this.read_D = cost;
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
            } else if (this.load_J != -9223372036854775808L && this.init_D - this.load_D >= 2.0) {
               this.matchedGoalKey = this.load_J;
            }

            this.load_Z = this.matchedGoalKey == -9223372036854775808L;
         }
      }
   }
}
