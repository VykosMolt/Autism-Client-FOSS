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
   private final int[] order;
   private final int[] load;
   private final int[] read;
   private final int[] write;
   private final int[] update;
   private final Int2IntOpenHashMap sparseIndex;
   private PrintSpatialIndex.Inner_TaskQueue root;
   private final boolean[] eligible;
   private final PrintSpatialIndex.Inner_TaskQueue[] leafOf;
   private long revision;

   PrintSpatialIndex(int size, IntFunction<BlockPos> positions) {
      this(size, false);
      PrintSpatialIndex.Inner_DataUtil build = new PrintSpatialIndex.Inner_DataUtil(this, null, positions, i -> true);

      while (!build.done) {
         build.init(2147483647);
      }
   }

   PrintSpatialIndex(IntArrayList source, IntFunction<BlockPos> positions) {
      this(source.size(), true);
      PrintSpatialIndex.Inner_DataUtil build = new PrintSpatialIndex.Inner_DataUtil(this, source, positions, i -> true);

      while (!build.done) {
         build.init(2147483647);
      }
   }

   private PrintSpatialIndex(int size, boolean sparse) {
      this.load = new int[size];
      this.order = new int[size];
      this.read = new int[size];
      this.write = new int[size];
      this.update = new int[size];
      this.eligible = new boolean[size];
      this.leafOf = new PrintSpatialIndex.Inner_TaskQueue[size];
      this.sparseIndex = sparse ? new Int2IntOpenHashMap(size) : null;
      if (this.sparseIndex != null) {
         this.sparseIndex.defaultReturnValue(-1);
      }
   }

   private int coordinate(int point, int axis) {
      return axis == 0 ? this.read[point] : (axis == 1 ? this.write[point] : this.update[point]);
   }

   private int init(int a, int b, int axis) {
      int result = Integer.compare(this.coordinate(a, axis), this.coordinate(b, axis));
      return result != 0 ? result : Integer.compare(this.load[a], this.load[b]);
   }

   private void swap(int a, int b) {
      int value = this.order[a];
      this.order[a] = this.order[b];
      this.order[b] = value;
   }

   void setEligible(int id, boolean value) {
      int point = this.sparseIndex == null ? id : this.sparseIndex.get(id);
      if (point >= 0 && point < this.eligible.length) {
         if (this.eligible[point] != value) {
            this.eligible[point] = value;
            this.revision++;

            for (PrintSpatialIndex.Inner_TaskQueue node = this.leafOf[point]; node != null; node = node.parent) {
               this.init(node);
            }
         }
      } else {
         throw new IllegalArgumentException("Unknown spatial point " + id);
      }
   }

   private void init(PrintSpatialIndex.Inner_TaskQueue node) {
      node.open = 0;
      node.minX = node.write = node.update = 2147483647;
      node.sync = node.flush = node.close = -2147483648;
      if (node.left == null) {
         for (int i = node.start; i < node.end; i++) {
            if (this.eligible[this.order[i]]) {
               int point = this.order[i];
               node.open++;
               node.minX = Math.min(node.minX, this.read[point]);
               node.sync = Math.max(node.sync, this.read[point]);
               node.write = Math.min(node.write, this.write[point]);
               node.flush = Math.max(node.flush, this.write[point]);
               node.update = Math.min(node.update, this.update[point]);
               node.close = Math.max(node.close, this.update[point]);
            }
         }
      } else {
         mergeBounds(node, node.left);
         mergeBounds(node, node.right);
      }
   }

   private static void mergeBounds(PrintSpatialIndex.Inner_TaskQueue node, PrintSpatialIndex.Inner_TaskQueue child) {
      if (child.open != 0) {
         node.open = node.open + child.open;
         node.minX = Math.min(node.minX, child.minX);
         node.sync = Math.max(node.sync, child.sync);
         node.write = Math.min(node.write, child.write);
         node.flush = Math.max(node.flush, child.flush);
         node.update = Math.min(node.update, child.update);
         node.close = Math.max(node.close, child.close);
      }
   }

   IntArrayList nearest(Vec3 from, int top, int limit, IntPredicate eligible) {
      PrintSpatialIndex.Inner_ConfigStore search = this.startSearch(from, top, limit, eligible);

      while (!search.done) {
         search.init(2147483647);
      }

      return search.results;
   }

   PrintSpatialIndex.Inner_ConfigStore startSearch(Vec3 from, int top, int limit, IntPredicate eligible) {
      return new PrintSpatialIndex.Inner_ConfigStore(from, top, limit, eligible);
   }

   double init(int x, int y, int z, PrintSpatialIndex.Inner_StreamUtil cost) {
      return this.root != null && this.root.open != 0
         ? this.init(this.root, x, y, z, cost, 1.0 / 0.0)
         : 1.0 / 0.0;
   }

   private double init(PrintSpatialIndex.Inner_TaskQueue node, int x, int y, int z, PrintSpatialIndex.Inner_StreamUtil cost, double best) {
      if (node.open == 0 || init(node, x, y, z, cost) >= best) {
         return best;
      }

      if (node.left == null) {
         for (int i = node.start; i < node.end && best > 0.0; i++) {
            int point = this.order[i];
            if (this.eligible[point]) {
               best = Math.min(best, cost.at(this.read[point] - x, this.write[point] - y, this.update[point] - z));
            }
         }

         return best;
      } else {
         PrintSpatialIndex.Inner_TaskQueue first = node.left;
         PrintSpatialIndex.Inner_TaskQueue second = node.right;
         if (first.open == 0 || second.open > 0 && init(first, x, y, z, cost) > init(second, x, y, z, cost)) {
            first = node.right;
            second = node.left;
         }

         best = this.init(first, x, y, z, cost, best);
         return best == 0.0 ? 0.0 : this.init(second, x, y, z, cost, best);
      }
   }

   private static double init(PrintSpatialIndex.Inner_TaskQueue n, int x, int y, int z, PrintSpatialIndex.Inner_StreamUtil cost) {
      return cost.at(Math.clamp(x, n.minX, n.sync) - x, Math.clamp(y, n.write, n.flush) - y, Math.clamp(z, n.update, n.close) - z);
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
               && PrintSpatialIndex.this.init(PrintSpatialIndex.this.order[this.init + child], PrintSpatialIndex.this.order[this.init + child + 1], this.load) < 0) {
               child++;
            }

            if (PrintSpatialIndex.this.init(PrintSpatialIndex.this.order[this.init + this.update], PrintSpatialIndex.this.order[this.init + child], this.load) >= 0) {
               this.update = -1;
               return false;
            } else {
               PrintSpatialIndex.this.swap(this.init + this.update, this.init + child);
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

            PrintSpatialIndex.this.swap(this.init, this.init + --this.read);
            this.update = 0;
            return false;
         }
      }
   }

   private record Inner_CachePool(double distance, PrintSpatialIndex.Inner_TaskQueue node, int id) {
   }

   final class Inner_ConfigStore {
      final IntArrayList results = new IntArrayList();
      private final Vec3 origin;
      private final int top;
      private final int limit;
      private final IntPredicate eligible;
      private final long revision;
      private final PriorityQueue<PrintSpatialIndex.Inner_CachePool> frontier;
      private PrintSpatialIndex.Inner_TaskQueue currentNode;
      private int read;
      long init_J;
      boolean done;
      boolean exhausted;

      private Inner_ConfigStore(Vec3 from, int top, int limit, IntPredicate eligible) {
         this.revision = PrintSpatialIndex.this.revision;
         this.frontier = new PriorityQueue<>(
            Comparator.comparingDouble(PrintSpatialIndex.Inner_CachePool::distance).thenComparingInt(e -> e.node != null ? -2147483648 : PrintSpatialIndex.this.load[e.id])
         );
         this.origin = from;
         this.top = top;
         this.limit = limit;
         this.eligible = eligible;
         if (PrintSpatialIndex.this.root != null
            && PrintSpatialIndex.this.root.open != 0
            && limit > 0
            && PrintSpatialIndex.this.root.write < top) {
            this.frontier
               .add(new PrintSpatialIndex.Inner_CachePool(PrintSpatialIndex.this.root.init(from), PrintSpatialIndex.this.root, -1));
         } else {
            this.done = true;
         }
      }

      int init(int maxSteps) {
         return this.init(maxSteps, null, 9223372036854775807L);
      }

      int init(int maxSteps, LongSupplier clock, long allowance) {
         if (this.init() && !this.done && maxSteps > 0) {
            int used = 0;
            long started = clock == null ? 0L : clock.getAsLong();

            while (!this.done && used < maxSteps && this.init() && (used <= 0 || clock == null || clock.getAsLong() - started < Math.max(1L, allowance))) {
               used++;
               this.init_J++;
               if (this.currentNode != null) {
                  int point = PrintSpatialIndex.this.order[this.read++];
                  if (PrintSpatialIndex.this.eligible[point] && PrintSpatialIndex.this.write[point] < this.top) {
                     double dx = PrintSpatialIndex.this.read[point] + 0.5 - this.origin.x;
                     double dy = PrintSpatialIndex.this.write[point] + 0.5 - this.origin.y;
                     double dz = PrintSpatialIndex.this.update[point] + 0.5 - this.origin.z;
                     this.frontier.add(new PrintSpatialIndex.Inner_CachePool(dx * dx + dy * dy + dz * dz, null, point));
                  }

                  if (this.read == this.currentNode.end) {
                     this.currentNode = null;
                  }
               } else {
                  PrintSpatialIndex.Inner_CachePool entry = this.frontier.poll();
                  PrintSpatialIndex.Inner_TaskQueue node = entry.node;
                  if (node == null) {
                     boolean accepted = this.eligible.test(PrintSpatialIndex.this.load[entry.id]);
                     if (!this.init()) {
                        break;
                     }

                     if (accepted) {
                        this.results.add(PrintSpatialIndex.this.load[entry.id]);
                     }
                  } else if (node.left != null) {
                     if (node.left.open > 0 && node.left.write < this.top) {
                        this.frontier
                           .add(new PrintSpatialIndex.Inner_CachePool(node.left.init(this.origin), node.left, -1));
                     }

                     if (node.right.open > 0 && node.right.write < this.top) {
                        this.frontier
                           .add(new PrintSpatialIndex.Inner_CachePool(node.right.init(this.origin), node.right, -1));
                     }
                  } else {
                     this.currentNode = node;
                     this.read = node.start;
                  }
               }

               if (this.results.size() >= this.limit || this.currentNode == null && this.frontier.isEmpty()) {
                  this.done = true;
               }
            }

            return used;
         } else {
            return 0;
         }
      }

      private boolean init() {
         if (!this.exhausted && this.revision == PrintSpatialIndex.this.revision) {
            return true;
         }

         this.exhausted = this.done = true;
         this.results.clear();
         return false;
      }
   }

   static final class Inner_DataUtil {
      final PrintSpatialIndex result;
      private final IntArrayList source;
      private final IntFunction<BlockPos> positions;
      private final IntPredicate initiallyActive;
      private final ArrayDeque<PrintSpatialIndex.Inner_DataUtil.Inner_DataUtil_2> stack = new ArrayDeque<>();
      private int cursor;
      long init_J;
      boolean done;

      Inner_DataUtil(int size, IntFunction<BlockPos> positions, IntPredicate initiallyActive) {
         this(new PrintSpatialIndex(size, false), null, positions, initiallyActive);
      }

      private Inner_DataUtil(PrintSpatialIndex result, IntArrayList source, IntFunction<BlockPos> positions, IntPredicate initiallyActive) {
         this.result = result;
         this.source = source;
         this.positions = positions;
         this.initiallyActive = initiallyActive;
      }

      int init(int budget) {
         int used = 0;

         while (!this.done && used < budget) {
            used++;
            this.init_J++;
            if (this.cursor < this.result.order.length) {
               int i = this.cursor++;
               int id = this.source == null ? i : this.source.getInt(i);
               this.result.order[i] = i;
               this.result.load[i] = id;
               BlockPos p = this.positions.apply(id);
               this.result.read[i] = p.getX();
               this.result.write[i] = p.getY();
               this.result.update[i] = p.getZ();
               this.result.eligible[i] = this.initiallyActive.test(id);
               if (this.result.sparseIndex != null && this.result.sparseIndex.put(id, i) != -1) {
                  throw new IllegalArgumentException("Duplicate spatial point " + id);
               }

               if (this.cursor == this.result.order.length) {
                  this.result.root = new PrintSpatialIndex.Inner_TaskQueue(0, this.cursor, 0, 0, 0, 0, 0, 0);
                  this.stack.push(new PrintSpatialIndex.Inner_DataUtil.Inner_DataUtil_2(this.result.root));
               }
            } else if (this.stack.isEmpty()) {
               this.done = true;
            } else {
               PrintSpatialIndex.Inner_DataUtil.Inner_DataUtil_2 frame = this.stack.peek();
               PrintSpatialIndex.Inner_TaskQueue node = frame.node;
               if (frame.load == 0) {
                  if (frame.cursor < node.end) {
                     int point = this.result.order[frame.cursor++];
                     node.minX = Math.min(node.minX, this.result.read[point]);
                     node.sync = Math.max(node.sync, this.result.read[point]);
                     node.write = Math.min(node.write, this.result.write[point]);
                     node.flush = Math.max(node.flush, this.result.write[point]);
                     node.update = Math.min(node.update, this.result.update[point]);
                     node.close = Math.max(node.close, this.result.update[point]);
                  } else if (node.end - node.start <= 12) {
                     for (int i = node.start; i < node.end; i++) {
                        this.result.leafOf[this.result.order[i]] = node;
                     }

                     this.result.init(node);
                     this.stack.pop();
                  } else {
                     int axis = node.sync - node.minX >= node.flush - node.write && node.sync - node.minX >= node.close - node.update
                        ? 0
                        : (node.flush - node.write >= node.close - node.update ? 1 : 2);
                     frame.partition = this.result.new Inner_NetHelper(node.start, node.end, node.start + node.end >>> 1, axis);
                     frame.load = 1;
                  }
               } else if (frame.load == 1) {
                  if (frame.partition.init()) {
                     int middle = node.start + node.end >>> 1;
                     node.left = new PrintSpatialIndex.Inner_TaskQueue(node.start, middle, 0, 0, 0, 0, 0, 0);
                     node.left.parent = node;
                     node.right = new PrintSpatialIndex.Inner_TaskQueue(middle, node.end, 0, 0, 0, 0, 0, 0);
                     node.right.parent = node;
                     frame.partition = null;
                     frame.load = 2;
                     this.stack.push(new PrintSpatialIndex.Inner_DataUtil.Inner_DataUtil_2(node.left));
                  }
               } else if (frame.load == 2) {
                  frame.load = 3;
                  this.stack.push(new PrintSpatialIndex.Inner_DataUtil.Inner_DataUtil_2(node.right));
               } else {
                  this.result.init(node);
                  this.stack.pop();
               }
            }
         }

         return used;
      }

      private static final class Inner_DataUtil_2 {
         final PrintSpatialIndex.Inner_TaskQueue node;
         int cursor;
         int load;
         PrintSpatialIndex.Inner_NetHelper partition;

         Inner_DataUtil_2(PrintSpatialIndex.Inner_TaskQueue node) {
            this.node = node;
            this.cursor = node.start;
            node.minX = node.write = node.update = 2147483647;
            node.sync = node.flush = node.close = -2147483648;
         }
      }
   }

   private final class Inner_NetHelper {
      int start;
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
         this.start = start;
         this.load = end;
         this.flush = middle;
         this.close = axis;
         this.read = 2 * (32 - Integer.numberOfLeadingZeros(end - start));
      }

      boolean init() {
         if (this.init_RefQueue_BufferHel != null) {
            return this.init_RefQueue_BufferHel.init();
         }

         if (this.load - this.start <= 1) {
            return true;
         }

         if (!this.init_Z) {
            if (--this.read < 0) {
               this.init_RefQueue_BufferHel = PrintSpatialIndex.this.new Inner_BufferHelper(this.start, this.load, this.close);
               return false;
            } else {
               int a = PrintSpatialIndex.this.order[this.start];
               int b = PrintSpatialIndex.this.order[this.start + this.load >>> 1];
               int c = PrintSpatialIndex.this.order[this.load - 1];
               this.open = PrintSpatialIndex.this.init(a, b, this.close) < 0
                  ? (PrintSpatialIndex.this.init(b, c, this.close) < 0 ? b : (PrintSpatialIndex.this.init(a, c, this.close) < 0 ? c : a))
                  : (PrintSpatialIndex.this.init(a, c, this.close) < 0 ? a : (PrintSpatialIndex.this.init(b, c, this.close) < 0 ? c : b));
               this.write = this.update = this.start;
               this.sync = this.load;
               this.init_Z = true;
               return false;
            }
         } else if (this.update < this.sync) {
            int comparison = PrintSpatialIndex.this.init(PrintSpatialIndex.this.order[this.update], this.open, this.close);
            if (comparison < 0) {
               PrintSpatialIndex.this.swap(this.write++, this.update++);
            } else if (comparison > 0) {
               PrintSpatialIndex.this.swap(this.update, --this.sync);
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

               this.start = this.sync;
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
      final int start;
      final int end;
      int minX;
      int write;
      int update;
      int sync;
      int flush;
      int close;
      int open;
      PrintSpatialIndex.Inner_TaskQueue left;
      PrintSpatialIndex.Inner_TaskQueue right;
      PrintSpatialIndex.Inner_TaskQueue parent;

      Inner_TaskQueue(int start, int end, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
         this.start = start;
         this.end = end;
         this.open = end - start;
         this.minX = minX;
         this.write = minY;
         this.update = minZ;
         this.sync = maxX;
         this.flush = maxY;
         this.close = maxZ;
      }

      double init(Vec3 from) {
         double dx = init(from.x, this.minX + 0.5, this.sync + 0.5);
         double dy = init(from.y, this.write + 0.5, this.flush + 0.5);
         double dz = init(from.z, this.update + 0.5, this.close + 0.5);
         return dx * dx + dy * dy + dz * dz;
      }

      private static double init(double value, double min, double max) {
         return value < min ? min - value : (value > max ? value - max : 0.0);
      }
   }
}
