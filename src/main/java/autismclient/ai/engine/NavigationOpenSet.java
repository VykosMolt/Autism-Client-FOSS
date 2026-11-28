package autismclient.ai.engine;

import java.util.Arrays;
import net.minecraft.core.BlockPos;

final class NavigationOpenSet {
   private NavigationOpenSet.Inner_DataUtil[] init_t_DataUtilArr = new NavigationOpenSet.Inner_DataUtil[128];
   private int init_I;
   private double init_D = 1.0;

   void init(double weight) {
      this.init_D = weight;
   }

   int size() {
      return this.init_I;
   }

   boolean isEmpty() {
      return this.init_I == 0;
   }

   double init(double cost, double heuristic) {
      return cost + this.init_D * heuristic;
   }

   double peekPriority() {
      return this.isEmpty() ? 1.0 / 0.0 : this.init(this.init_t_DataUtilArr[0].cost, this.init_t_DataUtilArr[0].heuristic);
   }

   private boolean init(NavigationOpenSet.Inner_DataUtil a, NavigationOpenSet.Inner_DataUtil b) {
      int order = Double.compare(this.init(a.cost, a.heuristic), this.init(b.cost, b.heuristic));
      if (order == 0) {
         order = Double.compare(a.heuristic, b.heuristic);
      }

      if (order == 0) {
         order = Long.compare(a.pos.asLong(), b.pos.asLong());
      }

      if (order == 0) {
         order = Integer.compare(a.load_I, b.load_I);
      }

      return order < 0;
   }

   void init(NavigationOpenSet.Inner_DataUtil node) {
      int i = node.init_I;
      if (i < 0) {
         if (this.init_I == this.init_t_DataUtilArr.length) {
            this.init_t_DataUtilArr = Arrays.copyOf(this.init_t_DataUtilArr, this.init_I * 2);
         }

         i = this.init_I++;
      }

      while (i > 0) {
         int parent = i - 1 >>> 1;
         if (!this.init(node, this.init_t_DataUtilArr[parent])) {
            break;
         }

         this.init_t_DataUtilArr[i] = this.init_t_DataUtilArr[parent];
         this.init_t_DataUtilArr[i].init_I = i;
         i = parent;
      }

      this.init_t_DataUtilArr[i] = node;
      node.init_I = i;
   }

   NavigationOpenSet.Inner_DataUtil poll() {
      NavigationOpenSet.Inner_DataUtil result = this.init_t_DataUtilArr[0];
      NavigationOpenSet.Inner_DataUtil tail = this.init_t_DataUtilArr[--this.init_I];
      this.init_t_DataUtilArr[this.init_I] = null;
      result.init_I = -1;
      if (this.init_I == 0) {
         return result;
      }

      int i = 0;

      while (i * 2 + 1 < this.init_I) {
         int child = i * 2 + 1;
         if (child + 1 < this.init_I && this.init(this.init_t_DataUtilArr[child + 1], this.init_t_DataUtilArr[child])) {
            child++;
         }

         if (!this.init(this.init_t_DataUtilArr[child], tail)) {
            break;
         }

         this.init_t_DataUtilArr[i] = this.init_t_DataUtilArr[child];
         this.init_t_DataUtilArr[i].init_I = i;
         i = child;
      }

      this.init_t_DataUtilArr[i] = tail;
      tail.init_I = i;
      return result;
   }

   static final class Inner_DataUtil {
      final BlockPos pos;
      final double heuristic;
      double cost = 1.0 / 0.0;
      NavigationOpenSet.Inner_DataUtil parent;
      AiPathfinder.PathStep move;
      int init_I = -1;
      int load_I;
      NavigationOpenSet.Inner_DataUtil nextInCell;
      boolean closed;

      Inner_DataUtil(BlockPos cell, double heuristic) {
         this.pos = cell;
         this.heuristic = heuristic;
      }
   }
}
