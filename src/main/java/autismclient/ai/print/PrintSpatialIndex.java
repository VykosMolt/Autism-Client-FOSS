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
   private final int[] ids;
   private final int[] xs;
   private final int[] ys;
   private final int[] zs;
   private final Int2IntOpenHashMap sparseIndex;
   private PrintSpatialIndex.Node root;
   private final boolean[] eligible;
   private final PrintSpatialIndex.Node[] leafOf;
   private long revision;

   PrintSpatialIndex(int size, IntFunction<BlockPos> positions) {
      this(size, false);
      PrintSpatialIndex.IndexBuild build = new PrintSpatialIndex.IndexBuild(this, null, positions, i -> true);

      while (!build.done) {
         build.advanceBuild(2147483647);
      }
   }

   PrintSpatialIndex(IntArrayList source, IntFunction<BlockPos> positions) {
      this(source.size(), true);
      PrintSpatialIndex.IndexBuild build = new PrintSpatialIndex.IndexBuild(this, source, positions, i -> true);

      while (!build.done) {
         build.advanceBuild(2147483647);
      }
   }

   private PrintSpatialIndex(int size, boolean sparse) {
      this.ids = new int[size];
      this.order = new int[size];
      this.xs = new int[size];
      this.ys = new int[size];
      this.zs = new int[size];
      this.eligible = new boolean[size];
      this.leafOf = new PrintSpatialIndex.Node[size];
      this.sparseIndex = sparse ? new Int2IntOpenHashMap(size) : null;
      if (this.sparseIndex != null) {
         this.sparseIndex.defaultReturnValue(-1);
      }
   }

   private int coordinate(int point, int axis) {
      return axis == 0 ? this.xs[point] : (axis == 1 ? this.ys[point] : this.zs[point]);
   }

   private int compareOnAxis(int a, int b, int axis) {
      int result = Integer.compare(this.coordinate(a, axis), this.coordinate(b, axis));
      return result != 0 ? result : Integer.compare(this.ids[a], this.ids[b]);
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

            for (PrintSpatialIndex.Node node = this.leafOf[point]; node != null; node = node.parent) {
               this.refreshBounds(node);
            }
         }
      } else {
         throw new IllegalArgumentException("Unknown spatial point " + id);
      }
   }

   private void refreshBounds(PrintSpatialIndex.Node node) {
      node.activeCount = 0;
      node.minX = node.minY = node.minZ = 2147483647;
      node.maxX = node.maxY = node.maxZ = -2147483648;
      if (node.left == null) {
         for (int i = node.start; i < node.end; i++) {
            if (this.eligible[this.order[i]]) {
               int point = this.order[i];
               node.activeCount++;
               node.minX = Math.min(node.minX, this.xs[point]);
               node.maxX = Math.max(node.maxX, this.xs[point]);
               node.minY = Math.min(node.minY, this.ys[point]);
               node.maxY = Math.max(node.maxY, this.ys[point]);
               node.minZ = Math.min(node.minZ, this.zs[point]);
               node.maxZ = Math.max(node.maxZ, this.zs[point]);
            }
         }
      } else {
         mergeBounds(node, node.left);
         mergeBounds(node, node.right);
      }
   }

   private static void mergeBounds(PrintSpatialIndex.Node node, PrintSpatialIndex.Node child) {
      if (child.activeCount != 0) {
         node.activeCount = node.activeCount + child.activeCount;
         node.minX = Math.min(node.minX, child.minX);
         node.maxX = Math.max(node.maxX, child.maxX);
         node.minY = Math.min(node.minY, child.minY);
         node.maxY = Math.max(node.maxY, child.maxY);
         node.minZ = Math.min(node.minZ, child.minZ);
         node.maxZ = Math.max(node.maxZ, child.maxZ);
      }
   }

   IntArrayList nearest(Vec3 from, int top, int limit, IntPredicate eligible) {
      PrintSpatialIndex.NearestSearch search = this.startSearch(from, top, limit, eligible);

      while (!search.done) {
         search.advance(2147483647);
      }

      return search.results;
   }

   PrintSpatialIndex.NearestSearch startSearch(Vec3 from, int top, int limit, IntPredicate eligible) {
      return new PrintSpatialIndex.NearestSearch(from, top, limit, eligible);
   }

   double queryNearestCost(int x, int y, int z, PrintSpatialIndex.CostFunction cost) {
      return this.root != null && this.root.activeCount != 0
         ? this.nearestCost(this.root, x, y, z, cost, 1.0 / 0.0)
         : 1.0 / 0.0;
   }

   private double nearestCost(PrintSpatialIndex.Node node, int x, int y, int z, PrintSpatialIndex.CostFunction cost, double best) {
      if (node.activeCount == 0 || lowerBound(node, x, y, z, cost) >= best) {
         return best;
      }

      if (node.left == null) {
         for (int i = node.start; i < node.end && best > 0.0; i++) {
            int point = this.order[i];
            if (this.eligible[point]) {
               best = Math.min(best, cost.at(this.xs[point] - x, this.ys[point] - y, this.zs[point] - z));
            }
         }

         return best;
      } else {
         PrintSpatialIndex.Node first = node.left;
         PrintSpatialIndex.Node second = node.right;
         if (first.activeCount == 0 || second.activeCount > 0 && lowerBound(first, x, y, z, cost) > lowerBound(second, x, y, z, cost)) {
            first = node.right;
            second = node.left;
         }

         best = this.nearestCost(first, x, y, z, cost, best);
         return best == 0.0 ? 0.0 : this.nearestCost(second, x, y, z, cost, best);
      }
   }

   private static double lowerBound(PrintSpatialIndex.Node n, int x, int y, int z, PrintSpatialIndex.CostFunction cost) {
      return cost.at(Math.clamp(x, n.minX, n.maxX) - x, Math.clamp(y, n.minY, n.maxY) - y, Math.clamp(z, n.minZ, n.maxZ) - z);
   }

   private final class HeapSortTask {
      final int base;
      final int axis;
      int heapSize;
      int nextHeapify;
      int siftIndex = -1;

      HeapSortTask(int start, int end, int axis) {
         this.base = start;
         this.axis = axis;
         this.heapSize = end - start;
         this.nextHeapify = this.heapSize / 2 - 1;
      }

      boolean step() {
         if (this.siftIndex >= 0) {
            int child = this.siftIndex * 2 + 1;
            if (child >= this.heapSize) {
               this.siftIndex = -1;
               return false;
            }

            if (child + 1 < this.heapSize
               && PrintSpatialIndex.this.compareOnAxis(PrintSpatialIndex.this.order[this.base + child], PrintSpatialIndex.this.order[this.base + child + 1], this.axis) < 0) {
               child++;
            }

            if (PrintSpatialIndex.this.compareOnAxis(PrintSpatialIndex.this.order[this.base + this.siftIndex], PrintSpatialIndex.this.order[this.base + child], this.axis) >= 0) {
               this.siftIndex = -1;
               return false;
            } else {
               PrintSpatialIndex.this.swap(this.base + this.siftIndex, this.base + child);
               this.siftIndex = child;
               return false;
            }
         } else {
            if (this.nextHeapify >= 0) {
               this.siftIndex = this.nextHeapify--;
               return false;
            }

            if (this.heapSize <= 1) {
               return true;
            }

            PrintSpatialIndex.this.swap(this.base, this.base + --this.heapSize);
            this.siftIndex = 0;
            return false;
         }
      }
   }

   private record FrontierEntry(double distance, PrintSpatialIndex.Node node, int id) {
   }

   final class NearestSearch {
      final IntArrayList results = new IntArrayList();
      private final Vec3 origin;
      private final int top;
      private final int limit;
      private final IntPredicate eligible;
      private final long revision;
      private final PriorityQueue<PrintSpatialIndex.FrontierEntry> frontier;
      private PrintSpatialIndex.Node currentNode;
      private int leafCursor;
      long unusedStepCount;
      boolean done;
      boolean exhausted;

      private NearestSearch(Vec3 from, int top, int limit, IntPredicate eligible) {
         this.revision = PrintSpatialIndex.this.revision;
         this.frontier = new PriorityQueue<>(
            Comparator.comparingDouble(PrintSpatialIndex.FrontierEntry::distance).thenComparingInt(e -> e.node != null ? -2147483648 : PrintSpatialIndex.this.ids[e.id])
         );
         this.origin = from;
         this.top = top;
         this.limit = limit;
         this.eligible = eligible;
         if (PrintSpatialIndex.this.root != null
            && PrintSpatialIndex.this.root.activeCount != 0
            && limit > 0
            && PrintSpatialIndex.this.root.minY < top) {
            this.frontier
               .add(new PrintSpatialIndex.FrontierEntry(PrintSpatialIndex.this.root.distanceSq(from), PrintSpatialIndex.this.root, -1));
         } else {
            this.done = true;
         }
      }

      int advance(int maxSteps) {
         return this.runBudgeted(maxSteps, null, 9223372036854775807L);
      }

      int runBudgeted(int maxSteps, LongSupplier clock, long allowance) {
         if (this.isCurrent() && !this.done && maxSteps > 0) {
            int used = 0;
            long started = clock == null ? 0L : clock.getAsLong();

            while (!this.done && used < maxSteps && this.isCurrent() && (used <= 0 || clock == null || clock.getAsLong() - started < Math.max(1L, allowance))) {
               used++;
               this.unusedStepCount++;
               if (this.currentNode != null) {
                  int point = PrintSpatialIndex.this.order[this.leafCursor++];
                  if (PrintSpatialIndex.this.eligible[point] && PrintSpatialIndex.this.ys[point] < this.top) {
                     double dx = PrintSpatialIndex.this.xs[point] + 0.5 - this.origin.x;
                     double dy = PrintSpatialIndex.this.ys[point] + 0.5 - this.origin.y;
                     double dz = PrintSpatialIndex.this.zs[point] + 0.5 - this.origin.z;
                     this.frontier.add(new PrintSpatialIndex.FrontierEntry(dx * dx + dy * dy + dz * dz, null, point));
                  }

                  if (this.leafCursor == this.currentNode.end) {
                     this.currentNode = null;
                  }
               } else {
                  PrintSpatialIndex.FrontierEntry entry = this.frontier.poll();
                  PrintSpatialIndex.Node node = entry.node;
                  if (node == null) {
                     boolean accepted = this.eligible.test(PrintSpatialIndex.this.ids[entry.id]);
                     if (!this.isCurrent()) {
                        break;
                     }

                     if (accepted) {
                        this.results.add(PrintSpatialIndex.this.ids[entry.id]);
                     }
                  } else if (node.left != null) {
                     if (node.left.activeCount > 0 && node.left.minY < this.top) {
                        this.frontier
                           .add(new PrintSpatialIndex.FrontierEntry(node.left.distanceSq(this.origin), node.left, -1));
                     }

                     if (node.right.activeCount > 0 && node.right.minY < this.top) {
                        this.frontier
                           .add(new PrintSpatialIndex.FrontierEntry(node.right.distanceSq(this.origin), node.right, -1));
                     }
                  } else {
                     this.currentNode = node;
                     this.leafCursor = node.start;
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

      private boolean isCurrent() {
         if (!this.exhausted && this.revision == PrintSpatialIndex.this.revision) {
            return true;
         }

         this.exhausted = this.done = true;
         this.results.clear();
         return false;
      }
   }

   static final class IndexBuild {
      final PrintSpatialIndex result;
      private final IntArrayList source;
      private final IntFunction<BlockPos> positions;
      private final IntPredicate initiallyActive;
      private final ArrayDeque<PrintSpatialIndex.IndexBuild.BuildFrame> stack = new ArrayDeque<>();
      private int cursor;
      long unusedStepCount;
      boolean done;

      IndexBuild(int size, IntFunction<BlockPos> positions, IntPredicate initiallyActive) {
         this(new PrintSpatialIndex(size, false), null, positions, initiallyActive);
      }

      private IndexBuild(PrintSpatialIndex result, IntArrayList source, IntFunction<BlockPos> positions, IntPredicate initiallyActive) {
         this.result = result;
         this.source = source;
         this.positions = positions;
         this.initiallyActive = initiallyActive;
      }

      int advanceBuild(int budget) {
         int used = 0;

         while (!this.done && used < budget) {
            used++;
            this.unusedStepCount++;
            if (this.cursor < this.result.order.length) {
               int i = this.cursor++;
               int id = this.source == null ? i : this.source.getInt(i);
               this.result.order[i] = i;
               this.result.ids[i] = id;
               BlockPos p = this.positions.apply(id);
               this.result.xs[i] = p.getX();
               this.result.ys[i] = p.getY();
               this.result.zs[i] = p.getZ();
               this.result.eligible[i] = this.initiallyActive.test(id);
               if (this.result.sparseIndex != null && this.result.sparseIndex.put(id, i) != -1) {
                  throw new IllegalArgumentException("Duplicate spatial point " + id);
               }

               if (this.cursor == this.result.order.length) {
                  this.result.root = new PrintSpatialIndex.Node(0, this.cursor, 0, 0, 0, 0, 0, 0);
                  this.stack.push(new PrintSpatialIndex.IndexBuild.BuildFrame(this.result.root));
               }
            } else if (this.stack.isEmpty()) {
               this.done = true;
            } else {
               PrintSpatialIndex.IndexBuild.BuildFrame frame = this.stack.peek();
               PrintSpatialIndex.Node node = frame.node;
               if (frame.phase == 0) {
                  if (frame.cursor < node.end) {
                     int point = this.result.order[frame.cursor++];
                     node.minX = Math.min(node.minX, this.result.xs[point]);
                     node.maxX = Math.max(node.maxX, this.result.xs[point]);
                     node.minY = Math.min(node.minY, this.result.ys[point]);
                     node.maxY = Math.max(node.maxY, this.result.ys[point]);
                     node.minZ = Math.min(node.minZ, this.result.zs[point]);
                     node.maxZ = Math.max(node.maxZ, this.result.zs[point]);
                  } else if (node.end - node.start <= 12) {
                     for (int i = node.start; i < node.end; i++) {
                        this.result.leafOf[this.result.order[i]] = node;
                     }

                     this.result.refreshBounds(node);
                     this.stack.pop();
                  } else {
                     int axis = node.maxX - node.minX >= node.maxY - node.minY && node.maxX - node.minX >= node.maxZ - node.minZ
                        ? 0
                        : (node.maxY - node.minY >= node.maxZ - node.minZ ? 1 : 2);
                     frame.partition = this.result.new Partition(node.start, node.end, node.start + node.end >>> 1, axis);
                     frame.phase = 1;
                  }
               } else if (frame.phase == 1) {
                  if (frame.partition.step()) {
                     int middle = node.start + node.end >>> 1;
                     node.left = new PrintSpatialIndex.Node(node.start, middle, 0, 0, 0, 0, 0, 0);
                     node.left.parent = node;
                     node.right = new PrintSpatialIndex.Node(middle, node.end, 0, 0, 0, 0, 0, 0);
                     node.right.parent = node;
                     frame.partition = null;
                     frame.phase = 2;
                     this.stack.push(new PrintSpatialIndex.IndexBuild.BuildFrame(node.left));
                  }
               } else if (frame.phase == 2) {
                  frame.phase = 3;
                  this.stack.push(new PrintSpatialIndex.IndexBuild.BuildFrame(node.right));
               } else {
                  this.result.refreshBounds(node);
                  this.stack.pop();
               }
            }
         }

         return used;
      }

      private static final class BuildFrame {
         final PrintSpatialIndex.Node node;
         int cursor;
         int phase;
         PrintSpatialIndex.Partition partition;

         BuildFrame(PrintSpatialIndex.Node node) {
            this.node = node;
            this.cursor = node.start;
            node.minX = node.minY = node.minZ = 2147483647;
            node.maxX = node.maxY = node.maxZ = -2147483648;
         }
      }
   }

   private final class Partition {
      int start;
      int end;
      int depthLimit;
      int lessEnd;
      int scan;
      int greaterStart;
      final int middle;
      final int axis;
      int pivot;
      boolean pivotChosen;
      PrintSpatialIndex.HeapSortTask heapSort;

      Partition(int start, int end, int middle, int axis) {
         this.start = start;
         this.end = end;
         this.middle = middle;
         this.axis = axis;
         this.depthLimit = 2 * (32 - Integer.numberOfLeadingZeros(end - start));
      }

      boolean step() {
         if (this.heapSort != null) {
            return this.heapSort.step();
         }

         if (this.end - this.start <= 1) {
            return true;
         }

         if (!this.pivotChosen) {
            if (--this.depthLimit < 0) {
               this.heapSort = PrintSpatialIndex.this.new HeapSortTask(this.start, this.end, this.axis);
               return false;
            } else {
               int a = PrintSpatialIndex.this.order[this.start];
               int b = PrintSpatialIndex.this.order[this.start + this.end >>> 1];
               int c = PrintSpatialIndex.this.order[this.end - 1];
               this.pivot = PrintSpatialIndex.this.compareOnAxis(a, b, this.axis) < 0
                  ? (PrintSpatialIndex.this.compareOnAxis(b, c, this.axis) < 0 ? b : (PrintSpatialIndex.this.compareOnAxis(a, c, this.axis) < 0 ? c : a))
                  : (PrintSpatialIndex.this.compareOnAxis(a, c, this.axis) < 0 ? a : (PrintSpatialIndex.this.compareOnAxis(b, c, this.axis) < 0 ? c : b));
               this.lessEnd = this.scan = this.start;
               this.greaterStart = this.end;
               this.pivotChosen = true;
               return false;
            }
         } else if (this.scan < this.greaterStart) {
            int comparison = PrintSpatialIndex.this.compareOnAxis(PrintSpatialIndex.this.order[this.scan], this.pivot, this.axis);
            if (comparison < 0) {
               PrintSpatialIndex.this.swap(this.lessEnd++, this.scan++);
            } else if (comparison > 0) {
               PrintSpatialIndex.this.swap(this.scan, --this.greaterStart);
            } else {
               this.scan++;
            }

            return false;
         } else {
            if (this.middle < this.lessEnd) {
               this.end = this.lessEnd;
            } else {
               if (this.middle < this.greaterStart) {
                  return true;
               }

               this.start = this.greaterStart;
            }

            this.pivotChosen = false;
            return false;
         }
      }
   }

   @FunctionalInterface
   interface CostFunction {
      double at(int var1, int var2, int var3);
   }

   private static final class Node {
      final int start;
      final int end;
      int minX;
      int minY;
      int minZ;
      int maxX;
      int maxY;
      int maxZ;
      int activeCount;
      PrintSpatialIndex.Node left;
      PrintSpatialIndex.Node right;
      PrintSpatialIndex.Node parent;

      Node(int start, int end, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
         this.start = start;
         this.end = end;
         this.activeCount = end - start;
         this.minX = minX;
         this.minY = minY;
         this.minZ = minZ;
         this.maxX = maxX;
         this.maxY = maxY;
         this.maxZ = maxZ;
      }

      double distanceSq(Vec3 from) {
         double dx = axisGap(from.x, this.minX + 0.5, this.maxX + 0.5);
         double dy = axisGap(from.y, this.minY + 0.5, this.maxY + 0.5);
         double dz = axisGap(from.z, this.minZ + 0.5, this.maxZ + 0.5);
         return dx * dx + dy * dy + dz * dz;
      }

      private static double axisGap(double value, double min, double max) {
         return value < min ? min - value : (value > max ? value - max : 0.0);
      }
   }
}
