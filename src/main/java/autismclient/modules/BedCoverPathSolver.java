package autismclient.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.phys.Vec3;

final class BedCoverPathSolver {
   static final int init_I = 0;
   static final int load_I = 1;
   static final int read = 2;
   static final int write = 3;
   static final int update = 5;
   private static final Direction[] DIRECTIONS = Direction.values();
   private static final BitSet NO_REMOVED = new BitSet();
   private static final BedCoverPathSolver.GridNode OUT_OF_BOUNDS_NODE = new BedCoverPathSolver.GridNode(1, 1.0 / 0.0, -1);
   private static final BedCoverPathSolver.GridNode UNUSED_EMPTY_NODE = new BedCoverPathSolver.GridNode(0, 0.0, -1);
   private static final double[] EYE_HEIGHTS = new double[]{1.62, 1.27};
   private static final int[] STEP_DYS = new int[]{0, 1, -1};

   static BedCoverPathSolver.CoverPlan plan(BedCoverPathSolver.CellGrid g, Vec3 observer, double reach) {
      return plan(g, observer, reach, minimalCover(g));
   }

   static BedCoverPathSolver.CoverPlan plan(
      BedCoverPathSolver.CellGrid g, Vec3 observer, double reach, BedCoverPathSolver.MinimalCover cover
   ) {
      double[] distance = new double[g.cellCount];
      Arrays.fill(distance, 1.0 / 0.0);
      int[] parent = new int[g.cellCount];
      Arrays.fill(parent, -1);

      record Inner_DataUtil(int init_I, double init_D) {
      }

      PriorityQueue<Inner_DataUtil> queue = new PriorityQueue<>(Comparator.comparingDouble(Inner_DataUtil::init_D).thenComparingInt(Inner_DataUtil::init_I));
      int own = g.indexOf(BlockPos.containing(observer));
      int seed = -1;
      double nearest = 1.0 / 0.0;
      if (g.canStand(own, false)) {
         seed = own;
      } else {
         for (int i = 0; i < g.cellCount; i++) {
            if (g.canStand(i, false) && (g.xOf(i) == 0 || g.xOf(i) == g.sx - 1 || g.zOf(i) == 0 || g.zOf(i) == g.sizeZ - 1)) {
               double d = g.posAt(i).distToCenterSqr(observer);
               if (d < nearest) {
                  nearest = d;
                  seed = i;
               }
            }
         }
      }

      if (seed >= 0) {
         distance[seed] = 0.0;
         queue.add(new Inner_DataUtil(seed, 0.0));
      }

      List<Vec3> targets = new ArrayList<>();

      for (int bed : g.init_List) {
         targets.addAll(bedAimPoints(g.posAt(bed)));
      }

      double best = 1.0 / 0.0;
      BedCoverPathSolver.BreakRoute winner = null;
      Vec3 winnerEye = null;
      Vec3 winnerAim = null;

      while (!queue.isEmpty()) {
         Inner_DataUtil visit = queue.remove();
         int i = visit.init_I;
         if (visit.init_D == distance[i] && !(visit.init_D >= best)) {
            BlockPos feet = g.posAt(i);

            for (double height : EYE_HEIGHTS) {
               Vec3 eye = new Vec3(feet.getX() + 0.5, feet.getY() + height, feet.getZ() + 0.5);

               for (Vec3 target : targets) {
                  if (!(eye.distanceToSqr(target) > reach * reach)) {
                     List<Integer> ray = traceRay(g, eye, target);
                     if (ray != null) {
                        double cost = visit.init_D;

                        for (int block : ray) {
                           if (block != i && block != g.neighbor(i, Direction.UP)) {
                              cost += g.nodeAt(block).breakCost + 5.0;
                           }
                        }

                        if (!(cost >= best)) {
                           BedCoverPathSolver.BreakRoute proof = planBreakRoute(g, parent, i, reach);
                           if (proof != null) {
                              List<Integer> remaining = traceRay(g, eye, target, -1, proof.removed);
                              if (remaining != null) {
                                 for (int block : remaining) {
                                    if (!proof.removed.get(block)) {
                                       proof.removed.set(block);
                                       proof.breakBlocks.add(g.posAt(block));
                                    }
                                 }

                                 if (canStandAt(g, i, proof.removed)) {
                                    double actual = proof.breakBlocks.stream().mapToDouble(p -> g.nodeAt(g.indexOf(p)).breakCost).sum()
                                       + Math.max(0, proof.breakBlocks.size() - 1) * 5;
                                    double score = actual + (proof.breakBlocks.isEmpty() ? 0 : 5) + proof.route.size() * 0.001;
                                    if (score < best) {
                                       best = score;
                                       winner = proof;
                                       winnerEye = eye;
                                       winnerAim = target;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            for (Direction d : Plane.HORIZONTAL) {
               for (int dy : STEP_DYS) {
                  int n = g.index(g.xOf(i) + d.getStepX(), g.yOf(i) + dy, g.zOf(i) + d.getStepZ());
                  if (g.canStand(n, true) && g.nodeAt(n).kind != 3 && g.nodeAt(g.neighbor(n, Direction.UP)).kind != 3) {
                     int extra = stepUpHeadCell(g, i, n);
                     if (extra < 0 || g.isBreakable(extra)) {
                        double cost = visit.init_D + 0.001;

                        for (int block : new int[]{n, g.neighbor(n, Direction.UP), extra}) {
                           if (block >= 0 && block != i && block != g.neighbor(i, Direction.UP) && g.nodeAt(block).kind == 2) {
                              cost += g.nodeAt(block).breakCost + 5.0;
                           }
                        }

                        if (cost < distance[n]) {
                           distance[n] = cost;
                           parent[n] = i;
                           queue.add(new Inner_DataUtil(n, cost));
                        }
                     }
                  }
               }
            }
         }
      }

      if (winner == null) {
         return new BedCoverPathSolver.CoverPlan(cover.cells, List.of(), List.of(), 1.0 / 0.0, false, cover.init_I, null, null);
      }

      double ticks = winner.breakBlocks.stream().mapToDouble(p -> g.nodeAt(g.indexOf(p)).breakCost).sum()
         + Math.max(0, winner.breakBlocks.size() - 1) * 5;
      return new BedCoverPathSolver.CoverPlan(
         cover.cells, List.copyOf(winner.breakBlocks), List.copyOf(winner.route), ticks, true, cover.init_I, winnerEye, winnerAim
      );
   }

   private static int stepUpHeadCell(BedCoverPathSolver.CellGrid g, int from, int to) {
      return g.yOf(to) > g.yOf(from)
         ? g.index(g.xOf(from), g.yOf(from) + 2, g.zOf(from))
         : (g.yOf(to) < g.yOf(from) ? g.index(g.xOf(to), g.yOf(from) + 1, g.zOf(to)) : -1);
   }

   private static boolean isOpenCell(BedCoverPathSolver.CellGrid g, int i, BitSet removed) {
      return i >= 0 && (g.nodeAt(i).kind == 0 || removed.get(i));
   }

   private static boolean canStandAt(BedCoverPathSolver.CellGrid g, int i, BitSet removed) {
      int floor = g.neighbor(i, Direction.DOWN);
      return isOpenCell(g, i, removed)
         && isOpenCell(g, g.neighbor(i, Direction.UP), removed)
         && floor >= 0
         && !removed.get(floor)
         && g.nodeAt(floor).kind != 0
         && g.nodeAt(floor).kind != 3;
   }

   private static BedCoverPathSolver.BreakRoute planBreakRoute(BedCoverPathSolver.CellGrid g, int[] parent, int finish, double reach) {
      ArrayList<Integer> route = new ArrayList<>();
      int i = finish;

      while (i >= 0) {
         route.add(i);
         i = parent[i];
      }

      Collections.reverse(route);
      BedCoverPathSolver.BreakRoute proof = new BedCoverPathSolver.BreakRoute(new BitSet(g.cellCount), new LinkedHashSet<>(), new ArrayList<>());

      for (int at = 0; at < route.size(); at++) {
         int ix = route.get(at);
         if (at > 0) {
            int previous = route.get(at - 1);
            ArrayList<Integer> pending = new ArrayList<>();

            for (int block : new int[]{stepUpHeadCell(g, previous, ix), g.neighbor(ix, Direction.UP), ix}) {
               if (block >= 0 && g.nodeAt(block).kind == 2 && !proof.removed.get(block) && !pending.contains(block)) {
                  pending.add(block);
               }
            }

            while (!pending.isEmpty()) {
               int selected = -1;

               for (int block : pending) {
                  if (canReachBlock(g, previous, block, reach, proof.removed)) {
                     selected = block;
                     break;
                  }
               }

               if (selected < 0) {
                  return null;
               }

               proof.removed.set(selected);
               proof.breakBlocks.add(g.posAt(selected));
               pending.remove(Integer.valueOf(selected));
            }
         }

         if (!canStandAt(g, ix, proof.removed)) {
            return null;
         }

         proof.route.add(g.posAt(ix));
      }

      return proof;
   }

   private static boolean canReachBlock(BedCoverPathSolver.CellGrid g, int stance, int block, double reach, BitSet removed) {
      BlockPos feet = g.posAt(stance);
      BlockPos target = g.posAt(block);

      for (double height : EYE_HEIGHTS) {
         Vec3 eye = new Vec3(feet.getX() + 0.5, feet.getY() + height, feet.getZ() + 0.5);

         for (Vec3 point : bedAimPoints(target)) {
            if (!(eye.distanceToSqr(point) > reach * reach)) {
               List<Integer> hit = traceRay(g, eye, point, block, removed);
               if (hit != null && hit.size() == 1 && hit.getFirst() == block) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static List<Vec3> bedAimPoints(BlockPos p) {
      return List.of(
         new Vec3(p.getX() + 0.5, p.getY() + 0.52, p.getZ() + 0.5),
         new Vec3(p.getX() + 0.08, p.getY() + 0.4, p.getZ() + 0.5),
         new Vec3(p.getX() + 0.92, p.getY() + 0.4, p.getZ() + 0.5),
         new Vec3(p.getX() + 0.5, p.getY() + 0.4, p.getZ() + 0.08),
         new Vec3(p.getX() + 0.5, p.getY() + 0.4, p.getZ() + 0.92)
      );
   }

   static List<Integer> traceRay(BedCoverPathSolver.CellGrid g, Vec3 start, Vec3 end) {
      return traceRay(g, start, end, -1, NO_REMOVED);
   }

   private static List<Integer> traceRay(BedCoverPathSolver.CellGrid g, Vec3 start, Vec3 end, int goal, BitSet removed) {
      Vec3 delta = end.subtract(start);
      int x = (int)Math.floor(start.x);
      int y = (int)Math.floor(start.y);
      int z = (int)Math.floor(start.z);
      int dx = Double.compare(delta.x, 0.0);
      int dy = Double.compare(delta.y, 0.0);
      int dz = Double.compare(delta.z, 0.0);
      double tx = firstBoundaryT(start.x, delta.x, x, dx);
      double ty = firstBoundaryT(start.y, delta.y, y, dy);
      double tz = firstBoundaryT(start.z, delta.z, z, dz);
      double ix = dx == 0 ? 1.0 / 0.0 : Math.abs(1.0 / delta.x);
      double iy = dy == 0 ? 1.0 / 0.0 : Math.abs(1.0 / delta.y);
      double iz = dz == 0 ? 1.0 / 0.0 : Math.abs(1.0 / delta.z);
      ArrayList<Integer> result = new ArrayList<>();

      for (int step = 0; step < 80; step++) {
         int i = g.index(x - g.origin.getX(), y - g.origin.getY(), z - g.origin.getZ());
         BedCoverPathSolver.GridNode cell = g.nodeAt(i);
         if (cell.kind == 1) {
            return null;
         }

         if (cell.kind == 3) {
            return goal < 0 ? result : null;
         }

         if (cell.kind == 2 && !removed.get(i) && !result.contains(i)) {
            result.add(i);
         }

         if (i == goal) {
            return result;
         }

         double t = Math.min(tx, Math.min(ty, tz));
         if (t > 1.0) {
            return null;
         }

         boolean crossX = tx <= t + 1.0E-10;
         boolean crossY = ty <= t + 1.0E-10;
         boolean crossZ = tz <= t + 1.0E-10;
         int mask = (crossX ? 1 : 0) | (crossY ? 2 : 0) | (crossZ ? 4 : 0);
         if (Integer.bitCount(mask) > 1) {
            for (int subset = 1; subset < 8; subset++) {
               if ((subset & mask) == subset && subset != mask) {
                  int side = g.index(
                     x + ((subset & 1) != 0 ? dx : 0) - g.origin.getX(),
                     y + ((subset & 2) != 0 ? dy : 0) - g.origin.getY(),
                     z + ((subset & 4) != 0 ? dz : 0) - g.origin.getZ()
                  );
                  BedCoverPathSolver.GridNode edge = g.nodeAt(side);
                  if (edge.kind == 1) {
                     return null;
                  }

                  if (edge.kind == 2 && !removed.get(side) && !result.contains(side)) {
                     result.add(side);
                  }
               }
            }
         }

         if (tx <= t + 1.0E-10) {
            x += dx;
            tx += ix;
         }

         if (ty <= t + 1.0E-10) {
            y += dy;
            ty += iy;
         }

         if (tz <= t + 1.0E-10) {
            z += dz;
            tz += iz;
         }
      }

      return null;
   }

   private static double firstBoundaryT(double start, double delta, int cell, int sign) {
      return sign == 0 ? 1.0 / 0.0 : ((sign > 0 ? cell + 1 : cell) - start) / delta;
   }

   static Set<BlockPos> coverCells(BedCoverPathSolver.CellGrid g) {
      return minimalCover(g).cells;
   }

   static BedCoverPathSolver.MinimalCover minimalCover(BedCoverPathSolver.CellGrid g) {
      BitSet removed = new BitSet(g.cellCount);
      Set<BlockPos> result = new LinkedHashSet<>();
      int layers = 0;
      int infinity = 10000;

      for (int layer = 0; layer < 16; layer++) {
         BedCoverPathSolver.FlowNetwork flow = new BedCoverPathSolver.FlowNetwork(g.cellCount * 2 + 2);
         int source = g.cellCount * 2;
         int sink = source + 1;

         for (int i = 0; i < g.cellCount; i++) {
            if (g.nodeAt(i).kind != 1) {
               flow.addEdge(i * 2, i * 2 + 1, g.nodeAt(i).kind == 2 && !removed.get(i) ? 1 : 10000);
               if (g.nodeAt(i).kind == 3) {
                  flow.addEdge(source, i * 2, 10000);
               }

               if (g.isBoundary(i)) {
                  flow.addEdge(i * 2 + 1, sink, 10000);
               }

               for (Direction d : DIRECTIONS) {
                  int n = g.neighbor(i, d);
                  if (n >= 0 && g.nodeAt(n).kind != 1) {
                     flow.addEdge(i * 2 + 1, n * 2, 10000);
                  }
               }
            }
         }

         int cut = flow.maxFlow(source, sink, 10000);
         if (cut == 0 || cut >= 10000) {
            break;
         }

         boolean[] reachable = flow.reachableFrom(source);
         int added = 0;

         for (int i = 0; i < g.cellCount; i++) {
            if (g.nodeAt(i).kind == 2 && reachable[i * 2] && !reachable[i * 2 + 1] && !removed.get(i)) {
               removed.set(i);
               result.add(g.posAt(i));
               added++;
            }
         }

         if (added == 0) {
            break;
         }

         layers++;
      }

      return new BedCoverPathSolver.MinimalCover(Collections.unmodifiableSet(result), layers);
   }

   private static final class FlowNetwork {
      final int[] head;
      final int[] level;
      final int[] currentArc;
      final int[] queue;
      int[] edgeTo;
      int[] nextEdge;
      int[] capacity;
      int edgeCount;

      FlowNetwork(int size) {
         this.head = new int[size];
         Arrays.fill(this.head, -1);
         this.level = new int[size];
         this.currentArc = new int[size];
         this.queue = new int[size];
         this.edgeTo = new int[size * 8];
         this.nextEdge = new int[this.edgeTo.length];
         this.capacity = new int[this.edgeTo.length];
      }

      void addEdge(int a, int b, int cap) {
         if (this.edgeCount + 2 > this.edgeTo.length) {
            int size = this.edgeTo.length * 2;
            this.edgeTo = Arrays.copyOf(this.edgeTo, size);
            this.nextEdge = Arrays.copyOf(this.nextEdge, size);
            this.capacity = Arrays.copyOf(this.capacity, size);
         }

         this.addArc(a, b, cap);
         this.addArc(b, a, 0);
      }

      void addArc(int a, int b, int cap) {
         this.edgeTo[this.edgeCount] = b;
         this.capacity[this.edgeCount] = cap;
         this.nextEdge[this.edgeCount] = this.head[a];
         this.head[a] = this.edgeCount++;
      }

      boolean[] reachableFrom(int source) {
         boolean[] seen = new boolean[this.head.length];
         int read = 0;
         int write = 0;
         this.queue[write++] = source;
         seen[source] = true;

         while (read < write) {
            for (int e = this.head[this.queue[read++]]; e >= 0; e = this.nextEdge[e]) {
               if (this.capacity[e] > 0 && !seen[this.edgeTo[e]]) {
                  seen[this.edgeTo[e]] = true;
                  this.queue[write++] = this.edgeTo[e];
               }
            }
         }

         return seen;
      }

      int maxFlow(int source, int sink, int limit) {
         int total = 0;

         while (total < limit) {
            Arrays.fill(this.level, -1);
            int read = 0;
            int write = 0;
            this.queue[write++] = source;
            this.level[source] = 0;

            while (read < write) {
               int at = this.queue[read++];

               for (int e = this.head[at]; e >= 0; e = this.nextEdge[e]) {
                  if (this.capacity[e] > 0 && this.level[this.edgeTo[e]] < 0) {
                     this.level[this.edgeTo[e]] = this.level[at] + 1;
                     this.queue[write++] = this.edgeTo[e];
                  }
               }
            }

            if (this.level[sink] < 0) {
               break;
            }

            System.arraycopy(this.head, 0, this.currentArc, 0, this.head.length);

            int sent;
            while (total < limit && (sent = this.pushFlow(source, sink, limit - total)) > 0) {
               total += sent;
            }
         }

         return total;
      }

      int pushFlow(int at, int sink, int amount) {
         if (at == sink) {
            return amount;
         }

         for (int e = this.currentArc[at]; e >= 0; this.currentArc[at] = e = this.nextEdge[e]) {
            if (this.capacity[e] > 0 && this.level[this.edgeTo[e]] == this.level[at] + 1) {
               int sent = this.pushFlow(this.edgeTo[e], sink, Math.min(amount, this.capacity[e]));
               if (sent > 0) {
                  this.capacity[e] = this.capacity[e] - sent;
                  this.capacity[e ^ 1] = this.capacity[e ^ 1] + sent;
                  return sent;
               }
            }
         }

         return 0;
      }
   }

   record MinimalCover(Set<BlockPos> cells, int init_I) {
   }

   static final class CellGrid {
      final BlockPos origin;
      final int sx;
      final int sizeY;
      final int sizeZ;
      final int cellCount;
      final BedCoverPathSolver.GridNode[] nodes;
      final List<Integer> init_List = new ArrayList<>();

      CellGrid(BlockPos origin, int sx, int sy, int sz) {
         this.origin = origin;
         this.sx = sx;
         this.sizeY = sy;
         this.sizeZ = sz;
         this.cellCount = sx * sy * sz;
         this.nodes = new BedCoverPathSolver.GridNode[this.cellCount];
         Arrays.fill(this.nodes, BedCoverPathSolver.OUT_OF_BOUNDS_NODE);
      }

      int index(int x, int y, int z) {
         return x >= 0 && y >= 0 && z >= 0 && x < this.sx && y < this.sizeY && z < this.sizeZ ? (y * this.sizeZ + z) * this.sx + x : -1;
      }

      int indexOf(BlockPos p) {
         return this.index(p.getX() - this.origin.getX(), p.getY() - this.origin.getY(), p.getZ() - this.origin.getZ());
      }

      int xOf(int i) {
         return i % this.sx;
      }

      int yOf(int i) {
         return i / (this.sx * this.sizeZ);
      }

      int zOf(int i) {
         return i / this.sx % this.sizeZ;
      }

      BlockPos posAt(int i) {
         return this.origin.offset(this.xOf(i), this.yOf(i), this.zOf(i));
      }

      int neighbor(int i, Direction d) {
         return this.index(this.xOf(i) + d.getStepX(), this.yOf(i) + d.getStepY(), this.zOf(i) + d.getStepZ());
      }

      boolean isBoundary(int i) {
         return this.xOf(i) == 0
            || this.xOf(i) == this.sx - 1
            || this.zOf(i) == 0
            || this.zOf(i) == this.sizeZ - 1
            || this.yOf(i) == 0
            || this.yOf(i) == this.sizeY - 1;
      }

      BedCoverPathSolver.GridNode nodeAt(int i) {
         return i < 0 ? BedCoverPathSolver.OUT_OF_BOUNDS_NODE : this.nodes[i];
      }

      boolean load_Z(int i) {
         return i >= 0 && (this.nodes[i].kind == 0 || this.nodes[i].kind == 3);
      }

      boolean isBreakable(int i) {
         return i >= 0 && this.nodes[i].kind != 1 && Double.isFinite(this.nodes[i].breakCost);
      }

      boolean canStand(int i, boolean dig) {
         if (i >= 0 && this.yOf(i) != 0 && this.yOf(i) < this.sizeY - 1) {
            int head = this.neighbor(i, Direction.UP);
            int floor = this.neighbor(i, Direction.DOWN);
            return (dig ? this.isBreakable(i) && this.isBreakable(head) : this.nodeAt(i).kind == 0 && this.nodeAt(head).kind == 0)
               && this.nodeAt(floor).kind != 0
               && this.nodeAt(floor).kind != 3;
         } else {
            return false;
         }
      }
   }

   record CoverPlan(
      Set<BlockPos> init_Set, List<BlockPos> path, List<BlockPos> load_List, double breakTicks, boolean init_Z, int coverCut, Vec3 init_Vec3, Vec3 load_Vec3
   ) {
      boolean init_Z_2() {
         return this.init_Z && this.coverCut > 1 && this.path.size() > 1;
      }

      public boolean load_Z() {
         return this.init_Z;
      }
   }

   record GridNode(int kind, double breakCost, int load) {
   }

   private record BreakRoute(BitSet removed, LinkedHashSet<BlockPos> breakBlocks, List<BlockPos> route) {
   }
}
