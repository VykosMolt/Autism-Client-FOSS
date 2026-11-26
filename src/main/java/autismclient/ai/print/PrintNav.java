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
   static final double init_D = 4.6;
   static final double load = 6.5;
   static final double read = 5.0;
   static final double write = 1.5;
   private static final int[][] init_IArr2 = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
   static final double update = 10.0;
   static final double sync = 0.2;
   static final double flush = 2.1;
   static final double close = 0.3;

   static PrintNav.Inner_TaskQueue init_TokenStore_TaskQue_ArrayUtil(PrintView view, BlockPos start, PrintNav.Inner_DataUtil bounds, int maxNodes) {
      return init_TokenStore_TaskQue_ArrayUtil(view, start, bounds, maxNodes, null);
   }

   static PrintNav.Inner_TaskQueue init_TokenStore_TaskQue_ArrayUtil(
      PrintView view, BlockPos start, PrintNav.Inner_DataUtil bounds, int maxNodes, Predicate<BlockPos> rideable
   ) {
      return init_TokenStore_TaskQue_ArrayUtil(view, start, bounds, maxNodes, rideable, null);
   }

   static PrintNav.Inner_TaskQueue init_TokenStore_TaskQue_ArrayUtil(
      PrintView view, BlockPos start, PrintNav.Inner_DataUtil bounds, int maxNodes, Predicate<BlockPos> rideable, LongOpenHashSet avoid
   ) {
      return init_TokenStore_TaskQue_ArrayUtil(view, start, bounds, maxNodes, rideable, avoid, null);
   }

   static PrintNav.Inner_TaskQueue init_TokenStore_TaskQue_ArrayUtil(
      PrintView view,
      BlockPos start,
      PrintNav.Inner_DataUtil bounds,
      int maxNodes,
      Predicate<BlockPos> rideable,
      LongOpenHashSet avoid,
      PrintNav.Inner_StreamUtil blockedEdges
   ) {
      PrintNav.Inner_NetHelper search = new PrintNav.Inner_NetHelper(view, start, bounds, maxNodes, rideable, avoid);
      search.init_TokenStore_StreamU = blockedEdges;
      search.init(9223372036854775807L);
      return search.init_TokenStore_TaskQue;
   }

   static double init_D_ArrayUtil(PrintView view, int x, int z, double h, int nx, int nz, double h2, boolean diagonal) {
      double cost = load(view, x, z, h, nx, nz, h2, diagonal);
      if (!Double.isNaN(cost)) {
         return cost;
      } else {
         return diagonal && h2 - h > 0.65 && h2 - h <= 1.250001 && load(view, x, z, h, nx, nz, h2) ? 11.5 : 0.0 / 0.0;
      }
   }

   private static boolean load(PrintView view, int x, int z, double h, int nx, int nz, double h2) {
      PrintNav.Inner_CachePool key = new PrintNav.Inner_CachePool(
         BlockPos.asLong(x, PrintView.init_I_D(h), z), BlockPos.asLong(nx, PrintView.init_I_D(h2), nz), Double.doubleToLongBits(h), Double.doubleToLongBits(h2)
      );
      List<PrintNav.Inner_BufferHelper> proofs = view.init_Map.get(key);
      if (proofs != null) {
         for (PrintNav.Inner_BufferHelper proof : proofs) {
            if (proof.matches(view)) {
               view.write_J++;
               return proof.result();
            }
         }
      }

      view.read_J++;

      try (PrintView.Inner_TaskQueue reads = view.init_ArrayUtil_TaskQueu()) {
         boolean result = read(view, x, z, h, nx, nz, h2);
         if (reads.init_Z()) {
            if (view.init_Map.size() >= 2048 && !view.init_Map.containsKey(key)) {
               view.init_Map.remove(view.init_Map.keySet().iterator().next());
            }

            List<PrintNav.Inner_BufferHelper> saved = view.init_Map.computeIfAbsent(key, ignored -> new ArrayList<>());
            if (saved.size() >= 2) {
               saved.remove(0);
            }

            saved.add(new PrintNav.Inner_BufferHelper(new Long2ObjectOpenHashMap<>(reads.init_Long2ObjectOpenHas), result));
         }

         return result;
      }
   }

   private static boolean read(PrintView view, int x, int z, double h, int nx, int nz, double h2) {
      if (!view.init_Z_AABB(new AABB(x + 0.2, h, z + 0.2, x + 0.8, h + 1.8 + 1.25, z + 0.8))) {
         return false;
      }

      AiPhysics.Inner_StreamUtil from = AiPhysics.Inner_StreamUtil.standing(x + 0.5, h, z + 0.5);
      float yaw = AiPhysics.init_F_D(x + 0.5, z + 0.5, nx + 0.5, nz + 0.5);
      autismclient.ai.engine.AiJumpSolver.UiHelper jump = autismclient.ai.engine.AiJumpSolver.init_TableIndex_UiHelpe_NodeWalker_CachePo(
         view.init_NodeWalker_CachePo(), from, yaw, false, new BlockPos(nx, PrintView.init_I_D(h2), nz), h2, 6
      );
      return jump != null && jump.margin() >= -0.04;
   }

   private static double load(PrintView view, int x, int z, double h, int nx, int nz, double h2, boolean diagonal) {
      double cx = x + 0.5;
      double cz = z + 0.5;
      double nxc = nx + 0.5;
      double nzc = nz + 0.5;
      double rim = init_D_ArrayUtil(view, x, z, h);
      if (rim > h + 1.0E-6) {
         if (!view.init_Z_AABB(PrintGeometry.init(cx, rim, cz, true))) {
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
         if (!view.init_Z_AABB(sweep)) {
            bodyHeight = 1.5;
            sweep = new AABB(sweep.minX, sweep.minY, sweep.minZ, sweep.maxX, top + bodyHeight, sweep.maxZ);
            if (!view.init_Z_AABB(sweep)) {
               return 0.0 / 0.0;
            }

            base /= 0.3;
         }

         if (Math.abs(rise) > 0.600001 && view.init_D_I(nx, nz, h2) < 0.3) {
            return 0.0 / 0.0;
         }

         if (!(rise > 0.600001)) {
            if (rise < -0.600001) {
               if (!view.init_Z_AABB(new AABB(nxc - 0.3, h2, nzc - 0.3, nxc + 0.3, h + bodyHeight, nzc + 0.3))) {
                  return 0.0 / 0.0;
               } else if (!view.init_Z_AABB(sweep)) {
                  return 0.0 / 0.0;
               } else if (diagonal) {
                  return 0.0 / 0.0;
               } else {
                  return view.load_Z && rise < -2.1 && !init_Z_ArrayUtil(view, nx + (nx - x), nz + (nz - z), h, h2, bodyHeight)
                     ? 0.0 / 0.0
                     : base + 1.5 * -rise;
               }
            } else if (!view.init_Z_AABB(sweep)) {
               return 0.0 / 0.0;
            } else {
               return diagonal && !init_Z_ArrayUtil_2(view, x, z, nx, nz, top, bodyHeight) ? 0.0 / 0.0 : base;
            }
         } else {
            if (!init_Z_ArrayUtil(view, cx, cz, nxc, nzc, h, top, bodyHeight)) {
               if (bodyHeight != 1.8 || !init_Z_ArrayUtil(view, cx, cz, nxc, nzc, h, top, 1.5)) {
                  return 0.0 / 0.0;
               }

               bodyHeight = 1.5;
               base = (diagonal ? 6.5 : 4.6) / 0.3;
            }

            return diagonal && !init_Z_ArrayUtil_2(view, x, z, nx, nz, top, bodyHeight) ? 0.0 / 0.0 : base + 5.0;
         }
      } else {
         return 0.0 / 0.0;
      }
   }

   static boolean init_Z_ArrayUtil(PrintView view, int x, int z, double h, int nx, int nz, double h2) {
      if (x != nx && z != nz && h2 - h > 0.65 && Double.isNaN(load(view, x, z, h, nx, nz, h2, true)) && load(view, x, z, h, nx, nz, h2)) {
         return false;
      }

      double top = Math.max(h, h2);
      return !view.init_Z_AABB(new AABB(Math.min(x, nx) + 0.2, top, Math.min(z, nz) + 0.2, Math.max(x, nx) + 0.8, top + 1.8, Math.max(z, nz) + 0.8))
         ? true
         : h2 - h > 0.600001 && !init_Z_ArrayUtil(view, x + 0.5, z + 0.5, nx + 0.5, nz + 0.5, h, top, 1.8);
   }

   private static boolean init_Z_ArrayUtil(PrintView view, double cx, double cz, double nxc, double nzc, double h, double top, double bodyHeight) {
      return view.init_Z_AABB(new AABB(cx - 0.3, h, cz - 0.3, cx + 0.3, top + bodyHeight + 0.2, cz + 0.3))
         && view.init_Z_AABB(
            new AABB(Math.min(cx, nxc) - 0.3, top, Math.min(cz, nzc) - 0.3, Math.max(cx, nxc) + 0.3, top + bodyHeight + 0.2, Math.max(cz, nzc) + 0.3)
         );
   }

   static boolean init_Z_ArrayUtil(PrintView view, int ox, int oz, double h, double h2) {
      return init_Z_ArrayUtil(view, ox, oz, h, h2, 1.8);
   }

   private static boolean init_Z_ArrayUtil(PrintView view, int ox, int oz, double h, double h2, double bodyHeight) {
      if (!view.init_Z_AABB(new AABB(ox + 0.2, h2 + 0.1, oz + 0.2, ox + 0.8, h2 + bodyHeight, oz + 0.8))) {
         return true;
      }

      for (int y = (int)Math.floor(h2 + 1.0E-6); y >= (int)Math.floor(h - 3.0 - 1.0E-6); y--) {
         double top = view.init_D_I_2(ox, y, oz);
         if (!Double.isNaN(top)) {
            return h - top <= 3.000001;
         }

         if (!view.update(new BlockPos(ox, y, oz))) {
            return false;
         }
      }

      return false;
   }

   static int init_I_ArrayUtil(PrintView view, int x, int z, double feet) {
      return PrintView.init_I_D(init_D_ArrayUtil(view, x, z, feet) + 1.25 + 1.0E-6);
   }

   static double init_D_ArrayUtil(PrintView view, int x, int z, double h) {
      int cy = (int)Math.floor(h + 1.0E-6);
      BlockPos cell = new BlockPos(x, cy, z);
      double best = h;

      for (AABB b : view.init_List_BlockPos(cell)) {
         double top = b.maxY + cy;
         if (top > h + 1.0E-6 && top <= h + 0.6 + 1.0E-6) {
            best = Math.max(best, top);
         }
      }

      return best;
   }

   private static boolean init_Z_ArrayUtil_2(PrintView view, int x, int z, int nx, int nz, double y, double bodyHeight) {
      for (int[] c : new int[][]{{nx, z}, {x, nz}}) {
         AABB box = new AABB(c[0] + 0.2, y + 0.01, c[1] + 0.2, c[0] + 0.8, y + bodyHeight, c[1] + 0.8);
         if (!view.init_Z_AABB(box)) {
            return false;
         }

         if (Double.isNaN(view.init_D_I_2(c[0], (int)Math.floor(y + 1.0E-6), c[1]))
            && Double.isNaN(view.init_D_I_2(c[0], (int)Math.floor(y + 1.0E-6) + 1, c[1]))) {
            return false;
         }
      }

      return true;
   }

   static BlockPos init(PrintView view, Vec3 feet) {
      int y = PrintView.init_I_D(feet.y + 0.001);
      int cx = PrintView.init_I_D(feet.x);
      int cz = PrintView.init_I_D(feet.z);
      BlockPos best = null;
      double bestDist = 1.0 / 0.0;
      double bestH = 0.0 / 0.0;
      BlockPos[] others = null;
      double[] otherH = null;
      int count = 0;

      for (int x = PrintView.init_I_D(feet.x - 0.3); x <= PrintView.init_I_D(feet.x + 0.3); x++) {
         for (int z = PrintView.init_I_D(feet.z - 0.3); z <= PrintView.init_I_D(feet.z + 0.3); z++) {
            for (int dy : new int[]{0, -1, 1}) {
               double h = view.init_D_I_2(x, y + dy, z);
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
         int bestExits = init(view, best, bestH);

         for (int k = 0; k < count; k++) {
            if (!others[k].equals(best) && !(Math.abs(otherH[k] - bestH) > 0.001)) {
               int e = init(view, others[k], otherH[k]);
               if (e > bestExits) {
                  bestExits = e;
                  best = others[k];
               }
            }
         }
      }

      return best;
   }

   private static int init(PrintView view, BlockPos at, double h) {
      int n = 0;

      for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}}) {
         int nx = at.getX() + d[0];
         int nz = at.getZ() + d[1];

         for (int ny = at.getY() + 1; ny >= at.getY() - 3; ny--) {
            double h2 = view.init_D_I_2(nx, ny, nz);
            if (!Double.isNaN(h2)) {
               if (!Double.isNaN(init_D_ArrayUtil(view, at.getX(), at.getZ(), h, nx, nz, h2, d[0] != 0 && d[1] != 0))) {
                  n++;
               }
               break;
            }
         }
      }

      return n;
   }

   static Vec3 init(long key, double feetY) {
      return new Vec3(BlockPos.getX(key) + 0.5, feetY, BlockPos.getZ(key) + 0.5);
   }

   record Inner_BufferHelper(Long2ObjectMap<BlockState> inputs, boolean result) {
      boolean matches(PrintView view) {
         for (Entry<BlockState> entry : this.inputs.long2ObjectEntrySet()) {
            if (view.getBlockState(BlockPos.of(entry.getLongKey())) != entry.getValue()) {
               return false;
            }
         }

         return true;
      }
   }

   record Inner_CachePool(long from, long to, long fromY, long toY) {
   }

   private record Inner_ConfigStore(double cost, long cell) {
   }

   record Inner_DataUtil(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      boolean contains(int x, int y, int z) {
         return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
      }
   }

   static final class Inner_NetHelper {
      final PrintView init_ArrayUtil;
      final PrintNav.Inner_DataUtil init_TokenStore_DataUti;
      final int init_I;
      final Predicate<BlockPos> init_Predicate;
      LongOpenHashSet init_LongOpenHashSet;
      PrintNav.Inner_StreamUtil init_TokenStore_StreamU;
      final LongPredicate init_LongPredicate;
      final PrintNav.Inner_TaskQueue init_TokenStore_TaskQue;
      final PriorityQueue<PrintNav.Inner_ConfigStore> init_PriorityQueue = new PriorityQueue<>((a, b) -> Double.compare(a.cost(), b.cost()));
      boolean init_Z;
      long init_J = -9223372036854775808L;
      int load;
      int read;
      int write;
      int update;
      int sync;
      PrintNav.Inner_ConfigStore init_TokenStore_ConfigS;
      int flush;
      int close;
      int open;
      int poll;
      double init_D;

      Inner_NetHelper(PrintView view, BlockPos start, PrintNav.Inner_DataUtil bounds, int maxNodes) {
         this(view, start, bounds, maxNodes, null, null);
      }

      Inner_NetHelper(PrintView view, BlockPos start, PrintNav.Inner_DataUtil bounds, int maxNodes, Predicate<BlockPos> rideable, LongOpenHashSet avoid) {
         this(view, start, bounds, maxNodes, rideable, avoid, null);
      }

      Inner_NetHelper(
         PrintView view,
         BlockPos start,
         PrintNav.Inner_DataUtil bounds,
         int maxNodes,
         Predicate<BlockPos> rideable,
         LongOpenHashSet avoid,
         LongPredicate stopAt
      ) {
         this.init_ArrayUtil = view;
         this.init_TokenStore_DataUti = bounds;
         this.init_I = maxNodes;
         this.init_Predicate = rideable;
         this.init_LongOpenHashSet = avoid;
         this.init_LongPredicate = stopAt;
         long key = start.asLong();
         this.init_TokenStore_TaskQue = new PrintNav.Inner_TaskQueue(key);
         double height = view.init_D_I_2(start.getX(), start.getY(), start.getZ());
         if (Double.isNaN(height)) {
            height = start.getY();
         }

         this.init_TokenStore_TaskQue.init_Long2DoubleOpenHas.put(key, 0.0);
         this.init_TokenStore_TaskQue.load_Long2DoubleOpenHas.put(key, height);
         this.init_PriorityQueue.add(new PrintNav.Inner_ConfigStore(0.0, key));
      }

      void init(long workBudget) {
         long started = this.init_ArrayUtil.sync();

         while (!this.init_Z && this.init_ArrayUtil.sync() - started < workBudget) {
            if (this.init_TokenStore_ConfigS == null) {
               if (this.init_PriorityQueue.isEmpty()) {
                  this.init_Z = true;
                  return;
               }

               PrintNav.Inner_ConfigStore top = this.init_PriorityQueue.poll();
               long key = top.cell();
               if (!(top.cost() > this.init_TokenStore_TaskQue.init_Long2DoubleOpenHas.get(key) + 1.0E-9)) {
                  if (++this.load > this.init_I) {
                     this.init_TokenStore_TaskQue.init_Z = true;
                     this.init_Z = true;
                     return;
                  }

                  if (this.init_LongPredicate != null && this.init_LongPredicate.test(key)) {
                     this.init_J = key;
                     this.init_Z = true;
                     return;
                  }

                  this.init_TokenStore_ConfigS = top;
                  this.flush = BlockPos.getX(key);
                  this.close = BlockPos.getY(key);
                  this.open = BlockPos.getZ(key);
                  this.init_D = this.init_TokenStore_TaskQue.load_Long2DoubleOpenHas.get(key);
                  this.poll = this.init_TokenStore_TaskQue.init_Long2IntOpenHashMa.get(key);
                  this.update = PrintNav.init_I_ArrayUtil(this.init_ArrayUtil, this.flush, this.open, this.init_D);
                  this.sync = PrintView.init_I_D(this.init_D - 3.0);
                  this.read = 0;
                  this.write = this.update;
                  if (this.init_Predicate != null && this.init_D == this.close && this.init_TokenStore_DataUti.contains(this.flush, this.close + 1, this.open)) {
                     BlockPos cell = new BlockPos(this.flush, this.close, this.open);
                     if (this.init_Predicate.test(cell) && this.init_ArrayUtil.sync(cell.above()) && this.init_ArrayUtil.sync(cell.above(2))) {
                        long next = BlockPos.asLong(this.flush, this.close + 1, this.open);
                        double cost = top.cost() + 10.0;
                        if (Double.isNaN(this.init_TokenStore_TaskQue.init_Long2DoubleOpenHas.get(next))
                           || cost < this.init_TokenStore_TaskQue.init_Long2DoubleOpenHas.get(next) - 1.0E-9) {
                           this.init_TokenStore_TaskQue.init_Long2DoubleOpenHas.put(next, cost);
                           this.init_TokenStore_TaskQue.init_Long2LongOpenHashM.put(next, key);
                           this.init_TokenStore_TaskQue.load_Long2DoubleOpenHas.put(next, this.close + 1);
                           this.init_TokenStore_TaskQue.init_LongOpenHashSet.add(next);
                           this.init_TokenStore_TaskQue.init_Long2IntOpenHashMa.put(next, this.poll + 1);
                           this.init_PriorityQueue.add(new PrintNav.Inner_ConfigStore(cost, next));
                        }
                     }
                  }
               }
            } else if (this.read == PrintNav.init_IArr2.length) {
               this.init_TokenStore_ConfigS = null;
            } else if (this.write < this.sync) {
               this.read++;
               this.write = this.update;
            } else {
               int[] delta = PrintNav.init_IArr2[this.read];
               int nx = this.flush + delta[0];
               int nz = this.open + delta[1];
               int ny = this.write--;
               if (this.init_TokenStore_DataUti.contains(nx, ny, nz)) {
                  double height = this.init_ArrayUtil.init_D_I_2(nx, ny, nz);
                  if (!Double.isNaN(height)) {
                     double step = PrintNav.init_D_ArrayUtil(
                        this.init_ArrayUtil, this.flush, this.open, this.init_D, nx, nz, height, delta[0] != 0 && delta[1] != 0
                     );
                     this.read++;
                     this.write = this.update;
                     if (!Double.isNaN(step)) {
                        long next = BlockPos.asLong(nx, ny, nz);
                        if ((this.init_LongOpenHashSet == null || !this.init_LongOpenHashSet.contains(next))
                           && (
                              this.init_TokenStore_StreamU == null
                                 || !this.init_TokenStore_StreamU.blocked(this.init_TokenStore_ConfigS.cell(), this.init_D, next, height)
                           )) {
                           double cost = this.init_TokenStore_ConfigS.cost() + step;
                           if (Double.isNaN(this.init_TokenStore_TaskQue.init_Long2DoubleOpenHas.get(next))
                              || cost < this.init_TokenStore_TaskQue.init_Long2DoubleOpenHas.get(next) - 1.0E-9) {
                              this.init_TokenStore_TaskQue.init_Long2DoubleOpenHas.put(next, cost);
                              this.init_TokenStore_TaskQue.init_Long2LongOpenHashM.put(next, this.init_TokenStore_ConfigS.cell());
                              this.init_TokenStore_TaskQue.load_Long2DoubleOpenHas.put(next, height);
                              this.init_TokenStore_TaskQue.init_LongOpenHashSet.remove(next);
                              this.init_TokenStore_TaskQue.init_Long2IntOpenHashMa.put(next, this.poll);
                              this.init_PriorityQueue.add(new PrintNav.Inner_ConfigStore(cost, next));
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
   interface Inner_StreamUtil {
      boolean blocked(long var1, double var3, long var5, double var7);
   }

   static final class Inner_TaskQueue {
      final Long2DoubleOpenHashMap init_Long2DoubleOpenHas = new Long2DoubleOpenHashMap();
      final Long2LongOpenHashMap init_Long2LongOpenHashM = new Long2LongOpenHashMap();
      final Long2DoubleOpenHashMap load_Long2DoubleOpenHas = new Long2DoubleOpenHashMap();
      final long init_J;
      final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
      final Long2IntOpenHashMap init_Long2IntOpenHashMa = new Long2IntOpenHashMap();
      boolean init_Z;
      private long[] init_JArr;
      private int init_I;
      private int load_I;
      private int read;
      private int write;
      private int update;
      private int sync;
      private int flush = -1;

      Inner_TaskQueue(long start) {
         this.init_J = start;
         this.init_Long2DoubleOpenHas.defaultReturnValue(0.0 / 0.0);
         this.load_Long2DoubleOpenHas.defaultReturnValue(0.0 / 0.0);
      }

      boolean init_Z(long key) {
         if (this.init_Long2DoubleOpenHas.size() != this.flush) {
            this.init_V();
         }

         if (this.init_JArr == null) {
            return !Double.isNaN(this.init_Long2DoubleOpenHas.get(key));
         } else {
            int x = BlockPos.getX(key) - this.init_I;
            int y = BlockPos.getY(key) - this.load_I;
            int z = BlockPos.getZ(key) - this.read;
            if (x >= 0 && y >= 0 && z >= 0 && x < this.write && y < this.update && z < this.sync) {
               int i = (x * this.update + y) * this.sync + z;
               return (this.init_JArr[i >>> 6] & 1L << i) != 0L;
            } else {
               return false;
            }
         }
      }

      private void init_V() {
         this.flush = this.init_Long2DoubleOpenHas.size();
         this.init_JArr = null;
         if (this.flush >= 64) {
            int x0 = 2147483647;
            int y0 = 2147483647;
            int z0 = 2147483647;
            int x1 = -2147483648;
            int y1 = -2147483648;
            int z1 = -2147483648;
            LongIterator it = this.init_Long2DoubleOpenHas.keySet().iterator();

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
               this.init_I = x0;
               this.load_I = y0;
               this.read = z0;
               this.write = x1 - x0 + 1;
               this.update = y1 - y0 + 1;
               this.sync = z1 - z0 + 1;
               long[] set = new long[(int)(volume + 63L >>> 6)];
               LongIterator itx = this.init_Long2DoubleOpenHas.keySet().iterator();

               while (itx.hasNext()) {
                  long k = itx.nextLong();
                  int i = ((BlockPos.getX(k) - x0) * this.update + (BlockPos.getY(k) - y0)) * this.sync + (BlockPos.getZ(k) - z0);
                  set[i >>> 6] = set[i >>> 6] | 1L << i;
               }

               this.init_JArr = set;
            }
         }
      }

      double init_D(long key) {
         return this.init_Long2DoubleOpenHas.get(key);
      }

      double load(long key) {
         return this.load_Long2DoubleOpenHas.get(key);
      }

      int init_I() {
         return this.init_Long2DoubleOpenHas.size();
      }

      LongArrayList init_LongArrayList(long key) {
         LongArrayList out = new LongArrayList();
         long at = key;
         int guard = 0;

         while (at != this.init_J && guard++ < 100000 && this.init_Long2LongOpenHashM.containsKey(at)) {
            if (this.init_LongOpenHashSet.contains(at)) {
               out.add(BlockPos.offset(at, Direction.DOWN));
            }

            at = this.init_Long2LongOpenHashM.get(at);
         }

         return out;
      }

      List<Long> init_List(long key) {
         List<Long> out = new ArrayList<>();
         long at = key;
         int guard = 0;

         while (at != this.init_J && guard++ < 100000) {
            out.add(at);
            if (!this.init_Long2LongOpenHashM.containsKey(at)) {
               break;
            }

            at = this.init_Long2LongOpenHashM.get(at);
         }

         Collections.reverse(out);
         return out;
      }
   }
}
