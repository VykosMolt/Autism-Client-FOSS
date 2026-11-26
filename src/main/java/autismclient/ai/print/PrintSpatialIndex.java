package autismclient.ai.print;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.LongSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

final class PrintSpatialIndex {
   private static final int init_I = 12;
   private final int[] init_IArr;
   private final int[] load;
   private final int[] read;
   private final int[] write;
   private final int[] update;
   private final Int2IntOpenHashMap init_Int2IntOpenHashMap;
   private PrintSpatialIndex.Inner_TaskQueue init_RefQueue_TaskQueue;
   private final boolean[] init_ZArr;
   private final PrintSpatialIndex.Inner_TaskQueue[] init_RefQueue_TaskQueueArr;
   private long init_J;

   PrintSpatialIndex(int size, IntFunction<BlockPos> positions) {
      this(size, false);
      PrintSpatialIndex.Inner_DataUtil build = new PrintSpatialIndex.Inner_DataUtil(this, null, positions, i -> true);

      while (!build.init_Z) {
         build.init(2147483647);
      }
   }

   PrintSpatialIndex(IntArrayList source, IntFunction<BlockPos> positions) {
      this(source.size(), true);
      PrintSpatialIndex.Inner_DataUtil build = new PrintSpatialIndex.Inner_DataUtil(this, source, positions, i -> true);

      while (!build.init_Z) {
         build.init(2147483647);
      }
   }

   private PrintSpatialIndex(int size, boolean sparse) {
      this.load = new int[size];
      this.init_IArr = new int[size];
      this.read = new int[size];
      this.write = new int[size];
      this.update = new int[size];
      this.init_ZArr = new boolean[size];
      this.init_RefQueue_TaskQueueArr = new PrintSpatialIndex.Inner_TaskQueue[size];
      this.init_Int2IntOpenHashMap = sparse ? new Int2IntOpenHashMap(size) : null;
      if (this.init_Int2IntOpenHashMap != null) {
         this.init_Int2IntOpenHashMap.defaultReturnValue(-1);
      }
   }

   private int init_I_I_I(int point, int axis) {
      return axis == 0 ? this.read[point] : (axis == 1 ? this.write[point] : this.update[point]);
   }

   private int init(int a, int b, int axis) {
      int result = Integer.compare(this.init_I_I_I(a, axis), this.init_I_I_I(b, axis));
      return result != 0 ? result : Integer.compare(this.load[a], this.load[b]);
   }

   private void init_V_I_I(int a, int b) {
      int value = this.init_IArr[a];
      this.init_IArr[a] = this.init_IArr[b];
      this.init_IArr[b] = value;
   }

   void init_V_I_Z(int id, boolean value) {
      int point = this.init_Int2IntOpenHashMap == null ? id : this.init_Int2IntOpenHashMap.get(id);
      if (point >= 0 && point < this.init_ZArr.length) {
         if (this.init_ZArr[point] != value) {
            this.init_ZArr[point] = value;
            this.init_J++;

            for (PrintSpatialIndex.Inner_TaskQueue node = this.init_RefQueue_TaskQueueArr[point]; node != null; node = node.read_RefQueue_TaskQueue) {
               this.init(node);
            }
         }
      } else {
         throw new IllegalArgumentException("Unknown spatial point " + id);
      }
   }

   private void init(PrintSpatialIndex.Inner_TaskQueue node) {
      node.open = 0;
      node.read_I = node.write = node.update = 2147483647;
      node.sync = node.flush = node.close = -2147483648;
      if (node.init_RefQueue_TaskQueue == null) {
         for (int i = node.init_I; i < node.load_I; i++) {
            if (this.init_ZArr[this.init_IArr[i]]) {
               int point = this.init_IArr[i];
               node.open++;
               node.read_I = Math.min(node.read_I, this.read[point]);
               node.sync = Math.max(node.sync, this.read[point]);
               node.write = Math.min(node.write, this.write[point]);
               node.flush = Math.max(node.flush, this.write[point]);
               node.update = Math.min(node.update, this.update[point]);
               node.close = Math.max(node.close, this.update[point]);
            }
         }
      } else {
         init_V_RefQueue_TaskQueue_RefQueue_TaskQueue(node, node.init_RefQueue_TaskQueue);
         init_V_RefQueue_TaskQueue_RefQueue_TaskQueue(node, node.load_RefQueue_TaskQueue);
      }
   }

   private static void init_V_RefQueue_TaskQueue_RefQueue_TaskQueue(PrintSpatialIndex.Inner_TaskQueue node, PrintSpatialIndex.Inner_TaskQueue child) {
      if (child.open != 0) {
         node.open = node.open + child.open;
         node.read_I = Math.min(node.read_I, child.read_I);
         node.sync = Math.max(node.sync, child.sync);
         node.write = Math.min(node.write, child.write);
         node.flush = Math.max(node.flush, child.flush);
         node.update = Math.min(node.update, child.update);
         node.close = Math.max(node.close, child.close);
      }
   }

   IntArrayList init_IntArrayList(Vec3 from, int top, int limit, IntPredicate eligible) {
      PrintSpatialIndex.Inner_ConfigStore search = this.init_RefQueue_ConfigSto(from, top, limit, eligible);

      while (!search.init_Z) {
         search.init(2147483647);
      }

      return search.init_IntArrayList;
   }

   PrintSpatialIndex.Inner_ConfigStore init_RefQueue_ConfigSto(Vec3 from, int top, int limit, IntPredicate eligible) {
      return new PrintSpatialIndex.Inner_ConfigStore(from, top, limit, eligible);
   }

   double init(int x, int y, int z, PrintSpatialIndex.Inner_StreamUtil cost) {
      return this.init_RefQueue_TaskQueue != null && this.init_RefQueue_TaskQueue.open != 0
         ? this.init(this.init_RefQueue_TaskQueue, x, y, z, cost, 1.0 / 0.0)
         : 1.0 / 0.0;
   }

   private double init(PrintSpatialIndex.Inner_TaskQueue node, int x, int y, int z, PrintSpatialIndex.Inner_StreamUtil cost, double best) {
      if (node.open == 0 || init(node, x, y, z, cost) >= best) {
         return best;
      }

      if (node.init_RefQueue_TaskQueue == null) {
         for (int i = node.init_I; i < node.load_I && best > 0.0; i++) {
            int point = this.init_IArr[i];
            if (this.init_ZArr[point]) {
               best = Math.min(best, cost.at(this.read[point] - x, this.write[point] - y, this.update[point] - z));
            }
         }

         return best;
      } else {
         PrintSpatialIndex.Inner_TaskQueue first = node.init_RefQueue_TaskQueue;
         PrintSpatialIndex.Inner_TaskQueue second = node.load_RefQueue_TaskQueue;
         if (first.open == 0 || second.open > 0 && init(first, x, y, z, cost) > init(second, x, y, z, cost)) {
            first = node.load_RefQueue_TaskQueue;
            second = node.init_RefQueue_TaskQueue;
         }

         best = this.init(first, x, y, z, cost, best);
         return best == 0.0 ? 0.0 : this.init(second, x, y, z, cost, best);
      }
   }

   private static double init(PrintSpatialIndex.Inner_TaskQueue n, int x, int y, int z, PrintSpatialIndex.Inner_StreamUtil cost) {
      return cost.at(Math.clamp(x, n.read_I, n.sync) - x, Math.clamp(y, n.write, n.flush) - y, Math.clamp(z, n.update, n.close) - z);
   }

   private final class Inner_BufferHelper {
      final int init;
      final int load;
      int read;
      int write;
      int update = -1;

      Inner_BufferHelper(int start, int end, int axis) {
         this.init = start;
         this.load = axis;
         this.read = end - start;
         this.write = this.read / 2 - 1;
      }

      boolean init() {
         if (this.update >= 0) {
            int child = this.update * 2 + 1;
            if (child >= this.read) {
               this.update = -1;
               return false;
            }

            if (child + 1 < this.read
               && PrintSpatialIndex.this.init(PrintSpatialIndex.this.init_IArr[this.init + child], PrintSpatialIndex.this.init_IArr[this.init + child + 1], this.load) < 0) {
               child++;
            }

            if (PrintSpatialIndex.this.init(PrintSpatialIndex.this.init_IArr[this.init + this.update], PrintSpatialIndex.this.init_IArr[this.init + child], this.load) >= 0) {
               this.update = -1;
               return false;
            } else {
               PrintSpatialIndex.this.init_V_I_I(this.init + this.update, this.init + child);
               this.update = child;
               return false;
            }
         } else {
            if (this.write >= 0) {
               this.update = this.write--;
               return false;
            }

            if (this.read <= 1) {
               return true;
            }

            PrintSpatialIndex.this.init_V_I_I(this.init, this.init + --this.read);
            this.update = 0;
            return false;
         }
      }
   }

   private record Inner_CachePool(double distance, PrintSpatialIndex.Inner_TaskQueue node, int id) {
   }

   final class Inner_ConfigStore {
      final IntArrayList init_IntArrayList = new IntArrayList();
      private final Vec3 init_Vec3;
      private final int init_I;
      private final int load_I;
      private final IntPredicate init_IntPredicate;
      private final long load_J;
      private final PriorityQueue<PrintSpatialIndex.Inner_CachePool> init_PriorityQueue;
      private PrintSpatialIndex.Inner_TaskQueue init_RefQueue_TaskQueue;
      private int read;
      long init_J;
      boolean init_Z;
      boolean load_Z;

      private Inner_ConfigStore(Vec3 from, int top, int limit, IntPredicate eligible) {
         this.load_J = PrintSpatialIndex.this.init_J;
         this.init_PriorityQueue = new PriorityQueue<>(
            Comparator.comparingDouble(PrintSpatialIndex.Inner_CachePool::distance).thenComparingInt(e -> e.node != null ? -2147483648 : PrintSpatialIndex.this.load[e.id])
         );
         this.init_Vec3 = from;
         this.init_I = top;
         this.load_I = limit;
         this.init_IntPredicate = eligible;
         if (PrintSpatialIndex.this.init_RefQueue_TaskQueue != null
            && PrintSpatialIndex.this.init_RefQueue_TaskQueue.open != 0
            && limit > 0
            && PrintSpatialIndex.this.init_RefQueue_TaskQueue.write < top) {
            this.init_PriorityQueue
               .add(new PrintSpatialIndex.Inner_CachePool(PrintSpatialIndex.this.init_RefQueue_TaskQueue.init(from), PrintSpatialIndex.this.init_RefQueue_TaskQueue, -1));
         } else {
            this.init_Z = true;
         }
      }

      int init(int maxSteps) {
         return this.init(maxSteps, null, 9223372036854775807L);
      }

      int init(int maxSteps, LongSupplier clock, long allowance) {
         if (this.init() && !this.init_Z && maxSteps > 0) {
            int used = 0;
            long started = clock == null ? 0L : clock.getAsLong();

            while (!this.init_Z && used < maxSteps && this.init() && (used <= 0 || clock == null || clock.getAsLong() - started < Math.max(1L, allowance))) {
               used++;
               this.init_J++;
               if (this.init_RefQueue_TaskQueue != null) {
                  int point = PrintSpatialIndex.this.init_IArr[this.read++];
                  if (PrintSpatialIndex.this.init_ZArr[point] && PrintSpatialIndex.this.write[point] < this.init_I) {
                     double dx = PrintSpatialIndex.this.read[point] + 0.5 - this.init_Vec3.x;
                     double dy = PrintSpatialIndex.this.write[point] + 0.5 - this.init_Vec3.y;
                     double dz = PrintSpatialIndex.this.update[point] + 0.5 - this.init_Vec3.z;
                     this.init_PriorityQueue.add(new PrintSpatialIndex.Inner_CachePool(dx * dx + dy * dy + dz * dz, null, point));
                  }

                  if (this.read == this.init_RefQueue_TaskQueue.load_I) {
                     this.init_RefQueue_TaskQueue = null;
                  }
               } else {
                  PrintSpatialIndex.Inner_CachePool entry = this.init_PriorityQueue.poll();
                  PrintSpatialIndex.Inner_TaskQueue node = entry.node;
                  if (node == null) {
                     boolean accepted = this.init_IntPredicate.test(PrintSpatialIndex.this.load[entry.id]);
                     if (!this.init()) {
                        break;
                     }

                     if (accepted) {
                        this.init_IntArrayList.add(PrintSpatialIndex.this.load[entry.id]);
                     }
                  } else if (node.init_RefQueue_TaskQueue != null) {
                     if (node.init_RefQueue_TaskQueue.open > 0 && node.init_RefQueue_TaskQueue.write < this.init_I) {
                        this.init_PriorityQueue
                           .add(new PrintSpatialIndex.Inner_CachePool(node.init_RefQueue_TaskQueue.init(this.init_Vec3), node.init_RefQueue_TaskQueue, -1));
                     }

                     if (node.load_RefQueue_TaskQueue.open > 0 && node.load_RefQueue_TaskQueue.write < this.init_I) {
                        this.init_PriorityQueue
                           .add(new PrintSpatialIndex.Inner_CachePool(node.load_RefQueue_TaskQueue.init(this.init_Vec3), node.load_RefQueue_TaskQueue, -1));
                     }
                  } else {
                     this.init_RefQueue_TaskQueue = node;
                     this.read = node.init_I;
                  }
               }

               if (this.init_IntArrayList.size() >= this.load_I || this.init_RefQueue_TaskQueue == null && this.init_PriorityQueue.isEmpty()) {
                  this.init_Z = true;
               }
            }

            return used;
         } else {
            return 0;
         }
      }

      private boolean init() {
         if (!this.load_Z && this.load_J == PrintSpatialIndex.this.init_J) {
            return true;
         }

         this.load_Z = this.init_Z = true;
         this.init_IntArrayList.clear();
         return false;
      }
   }

   static final class Inner_DataUtil {
      final PrintSpatialIndex init_RefQueue;
      private final IntArrayList init_IntArrayList;
      private final IntFunction<BlockPos> init_IntFunction;
      private final IntPredicate init_IntPredicate;
      private final ArrayDeque<PrintSpatialIndex.Inner_DataUtil.Inner_DataUtil_2> init_ArrayDeque = new ArrayDeque<>();
      private int init_I;
      long init_J;
      boolean init_Z;

      Inner_DataUtil(int size, IntFunction<BlockPos> positions, IntPredicate initiallyActive) {
         this(new PrintSpatialIndex(size, false), null, positions, initiallyActive);
      }

      private Inner_DataUtil(PrintSpatialIndex result, IntArrayList source, IntFunction<BlockPos> positions, IntPredicate initiallyActive) {
         this.init_RefQueue = result;
         this.init_IntArrayList = source;
         this.init_IntFunction = positions;
         this.init_IntPredicate = initiallyActive;
      }

      int init(int budget) {
         int used = 0;

         while (!this.init_Z && used < budget) {
            used++;
            this.init_J++;
            if (this.init_I < this.init_RefQueue.init_IArr.length) {
               int i = this.init_I++;
               int id = this.init_IntArrayList == null ? i : this.init_IntArrayList.getInt(i);
               this.init_RefQueue.init_IArr[i] = i;
               this.init_RefQueue.load[i] = id;
               BlockPos p = this.init_IntFunction.apply(id);
               this.init_RefQueue.read[i] = p.getX();
               this.init_RefQueue.write[i] = p.getY();
               this.init_RefQueue.update[i] = p.getZ();
               this.init_RefQueue.init_ZArr[i] = this.init_IntPredicate.test(id);
               if (this.init_RefQueue.init_Int2IntOpenHashMap != null && this.init_RefQueue.init_Int2IntOpenHashMap.put(id, i) != -1) {
                  throw new IllegalArgumentException("Duplicate spatial point " + id);
               }

               if (this.init_I == this.init_RefQueue.init_IArr.length) {
                  this.init_RefQueue.init_RefQueue_TaskQueue = new PrintSpatialIndex.Inner_TaskQueue(0, this.init_I, 0, 0, 0, 0, 0, 0);
                  this.init_ArrayDeque.push(new PrintSpatialIndex.Inner_DataUtil.Inner_DataUtil_2(this.init_RefQueue.init_RefQueue_TaskQueue));
               }
            } else if (this.init_ArrayDeque.isEmpty()) {
               this.init_Z = true;
            } else {
               PrintSpatialIndex.Inner_DataUtil.Inner_DataUtil_2 frame = this.init_ArrayDeque.peek();
               PrintSpatialIndex.Inner_TaskQueue node = frame.init_RefQueue_TaskQueue;
               if (frame.load == 0) {
                  if (frame.init_I < node.load_I) {
                     int point = this.init_RefQueue.init_IArr[frame.init_I++];
                     node.read_I = Math.min(node.read_I, this.init_RefQueue.read[point]);
                     node.sync = Math.max(node.sync, this.init_RefQueue.read[point]);
                     node.write = Math.min(node.write, this.init_RefQueue.write[point]);
                     node.flush = Math.max(node.flush, this.init_RefQueue.write[point]);
                     node.update = Math.min(node.update, this.init_RefQueue.update[point]);
                     node.close = Math.max(node.close, this.init_RefQueue.update[point]);
                  } else if (node.load_I - node.init_I <= 12) {
                     for (int i = node.init_I; i < node.load_I; i++) {
                        this.init_RefQueue.init_RefQueue_TaskQueueArr[this.init_RefQueue.init_IArr[i]] = node;
                     }

                     this.init_RefQueue.init(node);
                     this.init_ArrayDeque.pop();
                  } else {
                     int axis = node.sync - node.read_I >= node.flush - node.write && node.sync - node.read_I >= node.close - node.update
                        ? 0
                        : (node.flush - node.write >= node.close - node.update ? 1 : 2);
                     frame.init_RefQueue_NetHelper = this.init_RefQueue.new Inner_NetHelper(node.init_I, node.load_I, node.init_I + node.load_I >>> 1, axis);
                     frame.load = 1;
                  }
               } else if (frame.load == 1) {
                  if (frame.init_RefQueue_NetHelper.init()) {
                     int middle = node.init_I + node.load_I >>> 1;
                     node.init_RefQueue_TaskQueue = new PrintSpatialIndex.Inner_TaskQueue(node.init_I, middle, 0, 0, 0, 0, 0, 0);
                     node.init_RefQueue_TaskQueue.read_RefQueue_TaskQueue = node;
                     node.load_RefQueue_TaskQueue = new PrintSpatialIndex.Inner_TaskQueue(middle, node.load_I, 0, 0, 0, 0, 0, 0);
                     node.load_RefQueue_TaskQueue.read_RefQueue_TaskQueue = node;
                     frame.init_RefQueue_NetHelper = null;
                     frame.load = 2;
                     this.init_ArrayDeque.push(new PrintSpatialIndex.Inner_DataUtil.Inner_DataUtil_2(node.init_RefQueue_TaskQueue));
                  }
               } else if (frame.load == 2) {
                  frame.load = 3;
                  this.init_ArrayDeque.push(new PrintSpatialIndex.Inner_DataUtil.Inner_DataUtil_2(node.load_RefQueue_TaskQueue));
               } else {
                  this.init_RefQueue.init(node);
                  this.init_ArrayDeque.pop();
               }
            }
         }

         return used;
      }

      private static final class Inner_DataUtil_2 {
         final PrintSpatialIndex.Inner_TaskQueue init_RefQueue_TaskQueue;
         int init_I;
         int load;
         PrintSpatialIndex.Inner_NetHelper init_RefQueue_NetHelper;

         Inner_DataUtil_2(PrintSpatialIndex.Inner_TaskQueue node) {
            this.init_RefQueue_TaskQueue = node;
            this.init_I = node.init_I;
            node.read_I = node.write = node.update = 2147483647;
            node.sync = node.flush = node.close = -2147483648;
         }
      }
   }

   private final class Inner_NetHelper {
      int init_I;
      int load;
      int read;
      int write;
      int update;
      int sync;
      final int flush;
      final int close;
      int open;
      boolean init_Z;
      PrintSpatialIndex.Inner_BufferHelper init_RefQueue_BufferHel;

      Inner_NetHelper(int start, int end, int middle, int axis) {
         this.init_I = start;
         this.load = end;
         this.flush = middle;
         this.close = axis;
         this.read = 2 * (32 - Integer.numberOfLeadingZeros(end - start));
      }

      boolean init() {
         if (this.init_RefQueue_BufferHel != null) {
            return this.init_RefQueue_BufferHel.init();
         }

         if (this.load - this.init_I <= 1) {
            return true;
         }

         if (!this.init_Z) {
            if (--this.read < 0) {
               this.init_RefQueue_BufferHel = PrintSpatialIndex.this.new Inner_BufferHelper(this.init_I, this.load, this.close);
               return false;
            } else {
               int a = PrintSpatialIndex.this.init_IArr[this.init_I];
               int b = PrintSpatialIndex.this.init_IArr[this.init_I + this.load >>> 1];
               int c = PrintSpatialIndex.this.init_IArr[this.load - 1];
               this.open = PrintSpatialIndex.this.init(a, b, this.close) < 0
                  ? (PrintSpatialIndex.this.init(b, c, this.close) < 0 ? b : (PrintSpatialIndex.this.init(a, c, this.close) < 0 ? c : a))
                  : (PrintSpatialIndex.this.init(a, c, this.close) < 0 ? a : (PrintSpatialIndex.this.init(b, c, this.close) < 0 ? c : b));
               this.write = this.update = this.init_I;
               this.sync = this.load;
               this.init_Z = true;
               return false;
            }
         } else if (this.update < this.sync) {
            int comparison = PrintSpatialIndex.this.init(PrintSpatialIndex.this.init_IArr[this.update], this.open, this.close);
            if (comparison < 0) {
               PrintSpatialIndex.this.init_V_I_I(this.write++, this.update++);
            } else if (comparison > 0) {
               PrintSpatialIndex.this.init_V_I_I(this.update, --this.sync);
            } else {
               this.update++;
            }

            return false;
         } else {
            if (this.flush < this.write) {
               this.load = this.write;
            } else {
               if (this.flush < this.sync) {
                  return true;
               }

               this.init_I = this.sync;
            }

            this.init_Z = false;
            return false;
         }
      }
   }

   @FunctionalInterface
   interface Inner_StreamUtil {
      double at(int var1, int var2, int var3);
   }

   private static final class Inner_TaskQueue {
      final int init_I;
      final int load_I;
      int read_I;
      int write;
      int update;
      int sync;
      int flush;
      int close;
      int open;
      PrintSpatialIndex.Inner_TaskQueue init_RefQueue_TaskQueue;
      PrintSpatialIndex.Inner_TaskQueue load_RefQueue_TaskQueue;
      PrintSpatialIndex.Inner_TaskQueue read_RefQueue_TaskQueue;

      Inner_TaskQueue(int start, int end, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
         this.init_I = start;
         this.load_I = end;
         this.open = end - start;
         this.read_I = minX;
         this.write = minY;
         this.update = minZ;
         this.sync = maxX;
         this.flush = maxY;
         this.close = maxZ;
      }

      double init(Vec3 from) {
         double dx = init(from.x, this.read_I + 0.5, this.sync + 0.5);
         double dy = init(from.y, this.write + 0.5, this.flush + 0.5);
         double dz = init(from.z, this.update + 0.5, this.close + 0.5);
         return dx * dx + dy * dy + dz * dz;
      }

      private static double init(double value, double min, double max) {
         return value < min ? min - value : (value > max ? value - max : 0.0);
      }
   }
}
