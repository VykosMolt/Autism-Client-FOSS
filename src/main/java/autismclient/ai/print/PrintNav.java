package autismclient.ai.print;

import autismclient.ai.engine.AiPhysics;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class PrintNav {
   static final double CARDINAL_STEP_COST = 4.6;
   static final double DIAGONAL_STEP_COST = 6.5;
   static final double JUMP_EXTRA_COST = 5.0;
   static final double DROP_COST_PER_BLOCK = 1.5;
   private static final int[][] HORIZONTAL_OFFSETS = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
   static final double PILLAR_STEP_COST = 10.0;
   static final double JUMP_HEADROOM_MARGIN = 0.2;
   static final double OVERSHOOT_CHECK_DROP_THRESHOLD = 2.1;
   static final double CROUCH_SPEED_FACTOR = 0.3;

   static PrintNav.ReachResult searchReachable(PrintView view, BlockPos start, PrintNav.SearchBounds bounds, int maxNodes) {
      return searchReachable(view, start, bounds, maxNodes, null);
   }

   static PrintNav.ReachResult searchReachable(
      PrintView view, BlockPos start, PrintNav.SearchBounds bounds, int maxNodes, Predicate<BlockPos> rideable
   ) {
      return searchReachable(view, start, bounds, maxNodes, rideable, null);
   }

   static PrintNav.ReachResult searchReachable(
      PrintView view, BlockPos start, PrintNav.SearchBounds bounds, int maxNodes, Predicate<BlockPos> rideable, LongOpenHashSet avoid
   ) {
      return searchReachable(view, start, bounds, maxNodes, rideable, avoid, null);
   }

   static PrintNav.ReachResult searchReachable(
      PrintView view,
      BlockPos start,
      PrintNav.SearchBounds bounds,
      int maxNodes,
      Predicate<BlockPos> rideable,
      LongOpenHashSet avoid,
      PrintNav.EdgeFilter blockedEdges
   ) {
      PrintNav.ReachSearch search = new PrintNav.ReachSearch(view, start, bounds, maxNodes, rideable, avoid);
      search.blockedEdges = blockedEdges;
      search.advance(9223372036854775807L);
      return search.result;
   }

   static double stepCost(PrintView view, int x, int z, double h, int nx, int nz, double h2, boolean diagonal) {
      double cost = directStepCost(view, x, z, h, nx, nz, h2, diagonal);
      if (!Double.isNaN(cost)) {
         return cost;
      } else {
         return diagonal && h2 - h > 0.65 && h2 - h <= 1.250001 && canDiagonalJumpCached(view, x, z, h, nx, nz, h2) ? 11.5 : 0.0 / 0.0;
      }
   }

   private static boolean canDiagonalJumpCached(PrintView view, int x, int z, double h, int nx, int nz, double h2) {
      PrintNav.JumpKey key = new PrintNav.JumpKey(
         BlockPos.asLong(x, PrintView.floorInt(h), z), BlockPos.asLong(nx, PrintView.floorInt(h2), nz), Double.doubleToLongBits(h), Double.doubleToLongBits(h2)
      );
      List<PrintNav.DiagonalJumpProof> proofs = view.diagonalJumpProofCache.get(key);
      if (proofs != null) {
         for (PrintNav.DiagonalJumpProof proof : proofs) {
            if (proof.matches(view)) {
               view.diagonalJumpCacheHits++;
               return proof.result();
            }
         }
      }

      view.diagonalJumpCacheMisses++;

      try (PrintView.ReadTracker reads = view.openReadTracker()) {
         boolean result = simulateDiagonalJump(view, x, z, h, nx, nz, h2);
         if (reads.isComplete()) {
            if (view.diagonalJumpProofCache.size() >= 2048 && !view.diagonalJumpProofCache.containsKey(key)) {
               view.diagonalJumpProofCache.remove(view.diagonalJumpProofCache.keySet().iterator().next());
            }

            List<PrintNav.DiagonalJumpProof> saved = view.diagonalJumpProofCache.computeIfAbsent(key, ignored -> new ArrayList<>());
            if (saved.size() >= 2) {
               saved.remove(0);
            }

            saved.add(new PrintNav.DiagonalJumpProof(new Long2ObjectOpenHashMap<>(reads.recordedStates), result));
         }

         return result;
      }
   }

   private static boolean simulateDiagonalJump(PrintView view, int x, int z, double h, int nx, int nz, double h2) {
      if (!view.isBoxClear(new AABB(x + 0.2, h, z + 0.2, x + 0.8, h + 1.8 + 1.25, z + 0.8))) {
         return false;
      }

      AiPhysics.MotionState from = AiPhysics.MotionState.standing(x + 0.5, h, z + 0.5);
      float yaw = AiPhysics.yawTowards(x + 0.5, z + 0.5, nx + 0.5, nz + 0.5);
      autismclient.ai.engine.AiJumpSolver.TakeoffTiming jump = autismclient.ai.engine.AiJumpSolver.solveTakeoffCompat(
         view.asPhysicsWorld(), from, yaw, false, new BlockPos(nx, PrintView.floorInt(h2), nz), h2, 6
      );
      return jump != null && jump.margin() >= -0.04;
   }

   private static double directStepCost(PrintView view, int x, int z, double h, int nx, int nz, double h2, boolean diagonal) {
      double cx = x + 0.5;
      double cz = z + 0.5;
      double nxc = nx + 0.5;
      double nzc = nz + 0.5;
      double rim = stepUpRimHeight(view, x, z, h);
      if (rim > h + 1.0E-6) {
         if (!view.isBoxClear(PrintGeometry.init(cx, rim, cz, true))) {
            return 0.0 / 0.0;
         }

         h = rim;
      }

      double rise = h2 - h;
      double base = diagonal ? 6.5 : 4.6;
      double top = Math.max(h, h2);
      if (!(rise > 1.250001) && !(rise < -3.000001)) {
         AABB sweep = new AABB(Math.min(cx, nxc) - 0.3, top, Math.min(cz, nzc) - 0.3, Math.max(cx, nxc) + 0.3, top + 1.8, Math.max(cz, nzc) + 0.3);
         double bodyHeight = 1.8;
         if (!view.isBoxClear(sweep)) {
            bodyHeight = 1.5;
            sweep = new AABB(sweep.minX, sweep.minY, sweep.minZ, sweep.maxX, top + bodyHeight, sweep.maxZ);
            if (!view.isBoxClear(sweep)) {
               return 0.0 / 0.0;
            }

            base /= 0.3;
         }

         if (Math.abs(rise) > 0.600001 && view.supportAreaAtCell(nx, nz, h2) < 0.3) {
            return 0.0 / 0.0;
         }

         if (!(rise > 0.600001)) {
            if (rise < -0.600001) {
               if (!view.isBoxClear(new AABB(nxc - 0.3, h2, nzc - 0.3, nxc + 0.3, h + bodyHeight, nzc + 0.3))) {
                  return 0.0 / 0.0;
               } else if (!view.isBoxClear(sweep)) {
                  return 0.0 / 0.0;
               } else if (diagonal) {
                  return 0.0 / 0.0;
               } else {
                  return view.requireDropOvershootSafety && rise < -2.1 && !isDropOvershootSafe(view, nx + (nx - x), nz + (nz - z), h, h2, bodyHeight)
                     ? 0.0 / 0.0
                     : base + 1.5 * -rise;
               }
            } else if (!view.isBoxClear(sweep)) {
               return 0.0 / 0.0;
            } else {
               return diagonal && !hasDiagonalCornerClearance(view, x, z, nx, nz, top, bodyHeight) ? 0.0 / 0.0 : base;
            }
         } else {
            if (!hasJumpClearance(view, cx, cz, nxc, nzc, h, top, bodyHeight)) {
               if (bodyHeight != 1.8 || !hasJumpClearance(view, cx, cz, nxc, nzc, h, top, 1.5)) {
                  return 0.0 / 0.0;
               }

               bodyHeight = 1.5;
               base = (diagonal ? 6.5 : 4.6) / 0.3;
            }

            return diagonal && !hasDiagonalCornerClearance(view, x, z, nx, nz, top, bodyHeight) ? 0.0 / 0.0 : base + 5.0;
         }
      } else {
         return 0.0 / 0.0;
      }
   }

   static boolean requiresCrouchForStep(PrintView view, int x, int z, double h, int nx, int nz, double h2) {
      if (x != nx && z != nz && h2 - h > 0.65 && Double.isNaN(directStepCost(view, x, z, h, nx, nz, h2, true)) && canDiagonalJumpCached(view, x, z, h, nx, nz, h2)) {
         return false;
      }

      double top = Math.max(h, h2);
      return !view.isBoxClear(new AABB(Math.min(x, nx) + 0.2, top, Math.min(z, nz) + 0.2, Math.max(x, nx) + 0.8, top + 1.8, Math.max(z, nz) + 0.8))
         ? true
         : h2 - h > 0.600001 && !hasJumpClearance(view, x + 0.5, z + 0.5, nx + 0.5, nz + 0.5, h, top, 1.8);
   }

   private static boolean hasJumpClearance(PrintView view, double cx, double cz, double nxc, double nzc, double h, double top, double bodyHeight) {
      return view.isBoxClear(new AABB(cx - 0.3, h, cz - 0.3, cx + 0.3, top + bodyHeight + 0.2, cz + 0.3))
         && view.isBoxClear(
            new AABB(Math.min(cx, nxc) - 0.3, top, Math.min(cz, nzc) - 0.3, Math.max(cx, nxc) + 0.3, top + bodyHeight + 0.2, Math.max(cz, nzc) + 0.3)
         );
   }

   static boolean isDropOvershootSafe(PrintView view, int ox, int oz, double h, double h2) {
      return isDropOvershootSafe(view, ox, oz, h, h2, 1.8);
   }

   private static boolean isDropOvershootSafe(PrintView view, int ox, int oz, double h, double h2, double bodyHeight) {
      if (!view.isBoxClear(new AABB(ox + 0.2, h2 + 0.1, oz + 0.2, ox + 0.8, h2 + bodyHeight, oz + 0.8))) {
         return true;
      }

      for (int y = (int)Math.floor(h2 + 1.0E-6); y >= (int)Math.floor(h - 3.0 - 1.0E-6); y--) {
         double top = view.standingHeightAt(ox, y, oz);
         if (!Double.isNaN(top)) {
            return h - top <= 3.000001;
         }

         if (!view.isPassableCell(new BlockPos(ox, y, oz))) {
            return false;
         }
      }

      return false;
   }

   static int maxReachableStepY(PrintView view, int x, int z, double feet) {
      return PrintView.floorInt(stepUpRimHeight(view, x, z, feet) + 1.25 + 1.0E-6);
   }

   static double stepUpRimHeight(PrintView view, int x, int z, double h) {
      int cy = (int)Math.floor(h + 1.0E-6);
      BlockPos cell = new BlockPos(x, cy, z);
      double best = h;

      for (AABB b : view.getCollisionBoxesAt(cell)) {
         double top = b.maxY + cy;
         if (top > h + 1.0E-6 && top <= h + 0.6 + 1.0E-6) {
            best = Math.max(best, top);
         }
      }

      return best;
   }

   private static boolean hasDiagonalCornerClearance(PrintView view, int x, int z, int nx, int nz, double y, double bodyHeight) {
      for (int[] c : new int[][]{{nx, z}, {x, nz}}) {
         AABB box = new AABB(c[0] + 0.2, y + 0.01, c[1] + 0.2, c[0] + 0.8, y + bodyHeight, c[1] + 0.8);
         if (!view.isBoxClear(box)) {
            return false;
         }

         if (Double.isNaN(view.standingHeightAt(c[0], (int)Math.floor(y + 1.0E-6), c[1]))
            && Double.isNaN(view.standingHeightAt(c[0], (int)Math.floor(y + 1.0E-6) + 1, c[1]))) {
            return false;
         }
      }

      return true;
   }

   static BlockPos resolveStandingCell(PrintView view, Vec3 feet) {
      int y = PrintView.floorInt(feet.y + 0.001);
      int cx = PrintView.floorInt(feet.x);
      int cz = PrintView.floorInt(feet.z);
      BlockPos best = null;
      double bestDist = 1.0 / 0.0;
      double bestH = 0.0 / 0.0;
      BlockPos[] others = null;
      double[] otherH = null;
      int count = 0;

      for (int x = PrintView.floorInt(feet.x - 0.3); x <= PrintView.floorInt(feet.x + 0.3); x++) {
         for (int z = PrintView.floorInt(feet.z - 0.3); z <= PrintView.floorInt(feet.z + 0.3); z++) {
            for (int dy : new int[]{0, -1, 1}) {
               double h = view.standingHeightAt(x, y + dy, z);
               if (!Double.isNaN(h) && !(h - feet.y > 0.13) && !(feet.y - h > 0.600001)) {
                  if (others == null) {
                     others = new BlockPos[12];
                     otherH = new double[12];
                  }

                  if (count < others.length) {
                     others[count] = new BlockPos(x, y + dy, z);
                     otherH[count++] = h;
                  }

                  double dx = x + 0.5 - feet.x;
                  double dz = z + 0.5 - feet.z;
                  double dh = h - feet.y;
                  double d = dx * dx + dz * dz + dh * dh + (x == cx && z == cz ? 0.0 : 0.01);
                  if (d < bestDist) {
                     bestDist = d;
                     best = new BlockPos(x, y + dy, z);
                     bestH = h;
                  }
               }
            }
         }
      }

      if (best == null) {
         return new BlockPos(cx, y, cz);
      }

      if (count > 1) {
         int bestExits = countValidExits(view, best, bestH);

         for (int k = 0; k < count; k++) {
            if (!others[k].equals(best) && !(Math.abs(otherH[k] - bestH) > 0.001)) {
               int e = countValidExits(view, others[k], otherH[k]);
               if (e > bestExits) {
                  bestExits = e;
                  best = others[k];
               }
            }
         }
      }

      return best;
   }

   private static int countValidExits(PrintView view, BlockPos at, double h) {
      int n = 0;

      for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}}) {
         int nx = at.getX() + d[0];
         int nz = at.getZ() + d[1];

         for (int ny = at.getY() + 1; ny >= at.getY() - 3; ny--) {
            double h2 = view.standingHeightAt(nx, ny, nz);
            if (!Double.isNaN(h2)) {
               if (!Double.isNaN(stepCost(view, at.getX(), at.getZ(), h, nx, nz, h2, d[0] != 0 && d[1] != 0))) {
                  n++;
               }
               break;
            }
         }
      }

      return n;
   }

   static Vec3 cellCenterAtY(long key, double feetY) {
      return new Vec3(BlockPos.getX(key) + 0.5, feetY, BlockPos.getZ(key) + 0.5);
   }

   record DiagonalJumpProof(Long2ObjectMap<BlockState> inputs, boolean result) {
      boolean matches(PrintView view) {
         for (Entry<BlockState> entry : this.inputs.long2ObjectEntrySet()) {
            if (view.getBlockState(BlockPos.of(entry.getLongKey())) != entry.getValue()) {
               return false;
            }
         }

         return true;
      }
   }

   record JumpKey(long from, long to, long fromY, long toY) {
   }

   private record QueueEntry(double cost, long cell) {
   }

   record SearchBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      boolean contains(int x, int y, int z) {
         return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
      }
   }

   static final class ReachSearch {
      final PrintView view;
      final PrintNav.SearchBounds bounds;
      final int maxNodes;
      final Predicate<BlockPos> pillarablePredicate;
      LongOpenHashSet avoidCells;
      PrintNav.EdgeFilter blockedEdges;
      final LongPredicate stopPredicate;
      final PrintNav.ReachResult result;
      final PriorityQueue<PrintNav.QueueEntry> openQueue = new PriorityQueue<>((a, b) -> Double.compare(a.cost(), b.cost()));
      boolean finished;
      long matchedGoalKey = -9223372036854775808L;
      int expandedCount;
      int dirIndex;
      int scanY;
      int scanMaxY;
      int scanMinY;
      PrintNav.QueueEntry currentNode;
      int curX;
      int curY;
      int curZ;
      int curPillarCount;
      double curHeight;

      ReachSearch(PrintView view, BlockPos start, PrintNav.SearchBounds bounds, int maxNodes) {
         this(view, start, bounds, maxNodes, null, null);
      }

      ReachSearch(PrintView view, BlockPos start, PrintNav.SearchBounds bounds, int maxNodes, Predicate<BlockPos> rideable, LongOpenHashSet avoid) {
         this(view, start, bounds, maxNodes, rideable, avoid, null);
      }

      ReachSearch(
         PrintView view,
         BlockPos start,
         PrintNav.SearchBounds bounds,
         int maxNodes,
         Predicate<BlockPos> rideable,
         LongOpenHashSet avoid,
         LongPredicate stopAt
      ) {
         this.view = view;
         this.bounds = bounds;
         this.maxNodes = maxNodes;
         this.pillarablePredicate = rideable;
         this.avoidCells = avoid;
         this.stopPredicate = stopAt;
         long key = start.asLong();
         this.result = new PrintNav.ReachResult(key);
         double height = view.standingHeightAt(start.getX(), start.getY(), start.getZ());
         if (Double.isNaN(height)) {
            height = start.getY();
         }

         this.result.costByCell.put(key, 0.0);
         this.result.feetYByCell.put(key, height);
         this.openQueue.add(new PrintNav.QueueEntry(0.0, key));
      }

      void advance(long workBudget) {
         long started = this.view.workUnits();

         while (!this.finished && this.view.workUnits() - started < workBudget) {
            if (this.currentNode == null) {
               if (this.openQueue.isEmpty()) {
                  this.finished = true;
                  return;
               }

               PrintNav.QueueEntry top = this.openQueue.poll();
               long key = top.cell();
               if (!(top.cost() > this.result.costByCell.get(key) + 1.0E-9)) {
                  if (++this.expandedCount > this.maxNodes) {
                     this.result.truncatedByNodeLimit = true;
                     this.finished = true;
                     return;
                  }

                  if (this.stopPredicate != null && this.stopPredicate.test(key)) {
                     this.matchedGoalKey = key;
                     this.finished = true;
                     return;
                  }

                  this.currentNode = top;
                  this.curX = BlockPos.getX(key);
                  this.curY = BlockPos.getY(key);
                  this.curZ = BlockPos.getZ(key);
                  this.curHeight = this.result.feetYByCell.get(key);
                  this.curPillarCount = this.result.pillarCountByCell.get(key);
                  this.scanMaxY = PrintNav.maxReachableStepY(this.view, this.curX, this.curZ, this.curHeight);
                  this.scanMinY = PrintView.floorInt(this.curHeight - 3.0);
                  this.dirIndex = 0;
                  this.scanY = this.scanMaxY;
                  if (this.pillarablePredicate != null && this.curHeight == this.curY && this.bounds.contains(this.curX, this.curY + 1, this.curZ)) {
                     BlockPos cell = new BlockPos(this.curX, this.curY, this.curZ);
                     if (this.pillarablePredicate.test(cell) && this.view.isEmptyOutlineCell(cell.above()) && this.view.isEmptyOutlineCell(cell.above(2))) {
                        long next = BlockPos.asLong(this.curX, this.curY + 1, this.curZ);
                        double cost = top.cost() + 10.0;
                        if (Double.isNaN(this.result.costByCell.get(next))
                           || cost < this.result.costByCell.get(next) - 1.0E-9) {
                           this.result.costByCell.put(next, cost);
                           this.result.parentByCell.put(next, key);
                           this.result.feetYByCell.put(next, this.curY + 1);
                           this.result.pillarCells.add(next);
                           this.result.pillarCountByCell.put(next, this.curPillarCount + 1);
                           this.openQueue.add(new PrintNav.QueueEntry(cost, next));
                        }
                     }
                  }
               }
            } else if (this.dirIndex == PrintNav.HORIZONTAL_OFFSETS.length) {
               this.currentNode = null;
            } else if (this.scanY < this.scanMinY) {
               this.dirIndex++;
               this.scanY = this.scanMaxY;
            } else {
               int[] delta = PrintNav.HORIZONTAL_OFFSETS[this.dirIndex];
               int nx = this.curX + delta[0];
               int nz = this.curZ + delta[1];
               int ny = this.scanY--;
               if (this.bounds.contains(nx, ny, nz)) {
                  double height = this.view.standingHeightAt(nx, ny, nz);
                  if (!Double.isNaN(height)) {
                     double step = PrintNav.stepCost(
                        this.view, this.curX, this.curZ, this.curHeight, nx, nz, height, delta[0] != 0 && delta[1] != 0
                     );
                     this.dirIndex++;
                     this.scanY = this.scanMaxY;
                     if (!Double.isNaN(step)) {
                        long next = BlockPos.asLong(nx, ny, nz);
                        if ((this.avoidCells == null || !this.avoidCells.contains(next))
                           && (
                              this.blockedEdges == null
                                 || !this.blockedEdges.blocked(this.currentNode.cell(), this.curHeight, next, height)
                           )) {
                           double cost = this.currentNode.cost() + step;
                           if (Double.isNaN(this.result.costByCell.get(next))
                              || cost < this.result.costByCell.get(next) - 1.0E-9) {
                              this.result.costByCell.put(next, cost);
                              this.result.parentByCell.put(next, this.currentNode.cell());
                              this.result.feetYByCell.put(next, height);
                              this.result.pillarCells.remove(next);
                              this.result.pillarCountByCell.put(next, this.curPillarCount);
                              this.openQueue.add(new PrintNav.QueueEntry(cost, next));
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @FunctionalInterface
   interface EdgeFilter {
      boolean blocked(long var1, double var3, long var5, double var7);
   }

   static final class ReachResult {
      final Long2DoubleOpenHashMap costByCell = new Long2DoubleOpenHashMap();
      final Long2LongOpenHashMap parentByCell = new Long2LongOpenHashMap();
      final Long2DoubleOpenHashMap feetYByCell = new Long2DoubleOpenHashMap();
      final long startKey;
      final LongOpenHashSet pillarCells = new LongOpenHashSet();
      final Long2IntOpenHashMap pillarCountByCell = new Long2IntOpenHashMap();
      boolean truncatedByNodeLimit;
      private long[] reachableBitset;
      private int bitsetMinX;
      private int bitsetMinY;
      private int bitsetMinZ;
      private int bitsetSizeX;
      private int bitsetSizeY;
      private int bitsetSizeZ;
      private int indexedSize = -1;

      ReachResult(long start) {
         this.startKey = start;
         this.costByCell.defaultReturnValue(0.0 / 0.0);
         this.feetYByCell.defaultReturnValue(0.0 / 0.0);
      }

      boolean isReachable(long key) {
         if (this.costByCell.size() != this.indexedSize) {
            this.rebuildReachableBitset();
         }

         if (this.reachableBitset == null) {
            return !Double.isNaN(this.costByCell.get(key));
         } else {
            int x = BlockPos.getX(key) - this.bitsetMinX;
            int y = BlockPos.getY(key) - this.bitsetMinY;
            int z = BlockPos.getZ(key) - this.bitsetMinZ;
            if (x >= 0 && y >= 0 && z >= 0 && x < this.bitsetSizeX && y < this.bitsetSizeY && z < this.bitsetSizeZ) {
               int i = (x * this.bitsetSizeY + y) * this.bitsetSizeZ + z;
               return (this.reachableBitset[i >>> 6] & 1L << i) != 0L;
            } else {
               return false;
            }
         }
      }

      private void rebuildReachableBitset() {
         this.indexedSize = this.costByCell.size();
         this.reachableBitset = null;
         if (this.indexedSize >= 64) {
            int x0 = 2147483647;
            int y0 = 2147483647;
            int z0 = 2147483647;
            int x1 = -2147483648;
            int y1 = -2147483648;
            int z1 = -2147483648;
            LongIterator it = this.costByCell.keySet().iterator();

            while (it.hasNext()) {
               long k = it.nextLong();
               int x = BlockPos.getX(k);
               int y = BlockPos.getY(k);
               int z = BlockPos.getZ(k);
               if (x < x0) {
                  x0 = x;
               }

               if (x > x1) {
                  x1 = x;
               }

               if (y < y0) {
                  y0 = y;
               }

               if (y > y1) {
                  y1 = y;
               }

               if (z < z0) {
                  z0 = z;
               }

               if (z > z1) {
                  z1 = z;
               }
            }

            long volume = (long)(x1 - x0 + 1) * (y1 - y0 + 1) * (z1 - z0 + 1);
            if (volume <= 16000000L) {
               this.bitsetMinX = x0;
               this.bitsetMinY = y0;
               this.bitsetMinZ = z0;
               this.bitsetSizeX = x1 - x0 + 1;
               this.bitsetSizeY = y1 - y0 + 1;
               this.bitsetSizeZ = z1 - z0 + 1;
               long[] set = new long[(int)(volume + 63L >>> 6)];
               LongIterator itx = this.costByCell.keySet().iterator();

               while (itx.hasNext()) {
                  long k = itx.nextLong();
                  int i = ((BlockPos.getX(k) - x0) * this.bitsetSizeY + (BlockPos.getY(k) - y0)) * this.bitsetSizeZ + (BlockPos.getZ(k) - z0);
                  set[i >>> 6] = set[i >>> 6] | 1L << i;
               }

               this.reachableBitset = set;
            }
         }
      }

      double costOf(long key) {
         return this.costByCell.get(key);
      }

      double feetYOf(long key) {
         return this.feetYByCell.get(key);
      }

      int reachableCount() {
         return this.costByCell.size();
      }

      LongArrayList collectPillarBlocks(long key) {
         LongArrayList out = new LongArrayList();
         long at = key;
         int guard = 0;

         while (at != this.startKey && guard++ < 100000 && this.parentByCell.containsKey(at)) {
            if (this.pillarCells.contains(at)) {
               out.add(BlockPos.offset(at, Direction.DOWN));
            }

            at = this.parentByCell.get(at);
         }

         return out;
      }

      List<Long> reconstructPath(long key) {
         List<Long> out = new ArrayList<>();
         long at = key;
         int guard = 0;

         while (at != this.startKey && guard++ < 100000) {
            out.add(at);
            if (!this.parentByCell.containsKey(at)) {
               break;
            }

            at = this.parentByCell.get(at);
         }

         Collections.reverse(out);
         return out;
      }
   }
}
