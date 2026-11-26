package autismclient.ai.a;

import java.util.Arrays;
import net.minecraft.core.BlockPos;

final class NavigationOpenSet {
   private NavigationOpenSet.Inner_DataUtil[] init_t_DataUtilArr = new NavigationOpenSet.Inner_DataUtil[128];
   private int init_I;
   private double init_D = 1.0;

   void init(double weight) {
      this.init_D = weight;
   }

   int init_I() {
      return this.init_I;
   }

   boolean init_Z() {
      return this.init_I == 0;
   }

   double init(double cost, double heuristic) {
      return cost + this.init_D * heuristic;
   }

   double init_D() {
      return this.init_Z() ? 1.0 / 0.0 : this.init(this.init_t_DataUtilArr[0].load_D, this.init_t_DataUtilArr[0].init_D);
   }

   private boolean init(NavigationOpenSet.Inner_DataUtil a, NavigationOpenSet.Inner_DataUtil b) {
      int order = Double.compare(this.init(a.load_D, a.init_D), this.init(b.load_D, b.init_D));
      if (order == 0) {
         order = Double.compare(a.init_D, b.init_D);
      }

      if (order == 0) {
         order = Long.compare(a.init_BlockPos.asLong(), b.init_BlockPos.asLong());
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

   NavigationOpenSet.Inner_DataUtil init_t_DataUtil() {
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
      final BlockPos init_BlockPos;
      final double init_D;
      double load_D = 1.0 / 0.0;
      NavigationOpenSet.Inner_DataUtil init_t_DataUtil;
      AiPathfinder.UiHelper init_ClassCache_UiHelpe;
      int init_I = -1;
      int load_I;
      NavigationOpenSet.Inner_DataUtil load_t_DataUtil;
      boolean init_Z;

      Inner_DataUtil(BlockPos cell, double heuristic) {
         this.init_BlockPos = cell;
         this.init_D = heuristic;
      }
   }
}
