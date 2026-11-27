package autismclient.ai.print;

import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.LongSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class PrintAccess {
   static final double init_D = 12.0;
   static final double load_D = 16.0;
   static final double read_D = 15.0;
   static final double write_D = 200.0;
   boolean mayDigBuild;
   static final double update_D = 3.033333333333333;
   static final int init_I = 12;
   private static final int read_I = 1024;
   static final double sync_D = 60.0;
   static final double flush_D = 2.0;
   boolean allowBury;
   double close_D = 1.0;
   boolean alternateGuide;
   boolean write_Z;
   private boolean open_Z;
   private double open_D = -1.0 / 0.0;
   private double poll_D = 1.0 / 0.0;
   private final Int2IntOpenHashMap neighborCountCache = new Int2IntOpenHashMap();
   boolean update_Z;
   boolean freshClickRetry;
   private static final int[][] NEIGHBOR_OFFSETS_8 = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
   private final PrintCore core;
   private final PrintView view;
   private final double tempCost;
   private PrintNav.Inner_DataUtil bounds;
   private final Long2BooleanOpenHashMap init_Long2BooleanOpenHa = new Long2BooleanOpenHashMap();
   private long rootKey;
   private Boolean rootCanEscape;
   private Boolean load_Boolean;
   String failureDetail = "";
   long init_J = 20000000L;
   static final long load_J = 20000000L;
   boolean flush_Z;
   private boolean poll_Z;
   private boolean push_Z;
   private boolean pop;
   private int write_I = 2147483647;
   LongArrayList init_LongArrayList;
   long foundGoalKey = -9223372036854775808L;
   LongOpenHashSet init_LongOpenHashSet;
   LongArrayList load_LongArrayList;
   private BlockState init_BlockState;
   private Item init_Item;
   private PrintClicks.Inner_StreamUtil init_ConfigStore_Stream;
   private boolean tick;
   private boolean run;
   private LongOpenHashSet load_LongOpenHashSet;
   private long avoidSwap = -9223372036854775808L;
   private PrintClicks.Inner_StreamUtil load_ConfigStore_Stream;
   boolean retrying;
   private IntArrayList indexedGoals;
   private PrintSpatialIndex goalIndex;
   private LongCollection indexedCells;
   private PrintSpatialIndex cellIndex;
   private BlockState tempState;
   private boolean get;
   int lowestGoalY = -2147483648;

   private boolean init_Z_BlockPos_BlockState(BlockPos cell, BlockState material) {
      if (this.core.sync_Z_J(cell.asLong()) || !this.core.load_Z_BlockPos_BlockState(cell, material)) {
         return false;
      }

      if (!this.flush_Z && this.init_LongArrayList == null) {
         long key = cell.asLong();
         int i = this.core.cellIndexByPos.get(key);
         if (this.core.phase != PrintCore.J.FLUIDS
            || i < 0
            || this.core.targets[i].kind() != PrintTarget.Inner_DataUtil.FLUID
               && this.core.targets[i].kind() != PrintTarget.Inner_DataUtil.FLOW) {
            if (!this.init_Long2BooleanOpenHa.containsKey(key)) {
               this.init_Long2BooleanOpenHa.put(key, this.core.read_Z_BlockPos_BlockPos(cell, null));
            }

            return !this.init_Long2BooleanOpenHa.get(key) || this.core.close_Z_BlockPos(cell);
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   PrintAccess(PrintCore core, double tempCost) {
      this.core = core;
      this.view = core.view;
      this.tempCost = tempCost;
      this.bounds = core.bounds;
   }

   private boolean init_Z_StreamUtil_CachePo(PrintAccess.Inner_CachePool node) {
      return this.poll_Z
         && !this.push_Z
         && (
            this.update_Z
               ? this.core.init_Z_BlockPos(BlockPos.of(node.key), node.feet, node.temps)
               : this.core.init_Z_BlockPos(BlockPos.of(node.key), node.feet, node.temps, this.write_I)
         );
   }

   private int init_I_I(int a, int b, double x, double y, double z) {
      if (this.write_Z) {
         int load = Integer.compare(
            this.neighborCountCache.computeIfAbsent(a, this.core::neighborCount),
            this.neighborCountCache.computeIfAbsent(b, this.core::neighborCount)
         );
         if (load != 0) {
            return load;
         }
      }

      return Double.compare(
         this.core.targets[a].pos().distToCenterSqr(x, y, z), this.core.targets[b].pos().distToCenterSqr(x, y, z)
      );
   }

   private long init_J_LongArrayList_BlockPos(LongArrayList assumed, BlockPos goal) {
      if (assumed != null && !assumed.isEmpty()) {
         long h = 17L;

         for (int k = 0; k < assumed.size(); k++) {
            long key = assumed.getLong(k);
            if (Math.abs(BlockPos.getX(key) - goal.getX()) <= 7
               && Math.abs(BlockPos.getY(key) - goal.getY()) <= 7
               && Math.abs(BlockPos.getZ(key) - goal.getZ()) <= 7) {
               h = h * -7046029254386353131L + key;
               h = h * 31L + Block.getId(this.view.getBlockState(BlockPos.of(key)));
            }
         }

         return h;
      } else {
         return 0L;
      }
   }

   PrintAccess.Inner_ConfigStore startSearch(BlockPos start, double feet, IntArrayList goals, BlockState tempState) {
      return new PrintAccess.Inner_ConfigStore(start, feet, goals, tempState, this.view::workUnits);
   }

   PrintAccess.Inner_ConfigStore startFocusedSearch(BlockPos start, double feet, IntArrayList goals) {
      this.push_Z = true;
      this.write_Z = true;
      return this.startSearch(start, feet, goals, null);
   }

   PrintAccess.Inner_ConfigStore startBandSearch(BlockPos start, double feet, int goal, BlockState temporary) {
      int height = this.core.targets[goal].pos().getY();
      this.open_D = height - 8;
      this.poll_D = height + 8;
      this.close_D = 2.0;
      return this.startSearch(start, feet, IntArrayList.of(goal), temporary);
   }

   PrintAccess.Inner_ConfigStore startSearchFromReach(
      BlockPos start, double feet, IntArrayList goals, BlockState tempState, PrintNav.Inner_TaskQueue reach
   ) {
      if (reach.startKey != start.asLong()) {
         throw new IllegalArgumentException("Column reach starts elsewhere");
      }

      this.open_Z = true;
      this.alternateGuide = true;
      this.write_Z = true;
      PrintAccess.Inner_ConfigStore session = this.startSearch(start, feet, goals, tempState);
      session.openQueue.clear();
      session.bestCost.clear();
      session.reach = reach;
      session.reachCells = reach.costByCell.keySet().iterator();
      return session;
   }

   PrintAccess.Inner_ConfigStore init_StreamUtil_ConfigS_BlockPos_D(BlockPos start, double feet) {
      this.flush_Z = true;
      this.push_Z = true;
      this.pop = true;
      this.write_I = 0;
      return new PrintAccess.Inner_ConfigStore(start, feet, new IntArrayList(), null, this.view::workUnits);
   }

   PrintAccess.Inner_ConfigStore startExitSearch(BlockPos start, double feet, BlockState tempState, boolean groundOnly, int maxTemps) {
      this.flush_Z = true;
      this.poll_Z = true;
      this.push_Z = groundOnly;
      this.write_I = maxTemps;
      return new PrintAccess.Inner_ConfigStore(start, feet, new IntArrayList(), tempState, this.view::workUnits);
   }

   PrintAccess.Inner_ConfigStore startRemovalSearch(
      BlockPos start, double feet, LongArrayList temps, BlockState tempState, boolean standOn, long avoidSwap
   ) {
      this.init_LongArrayList = new LongArrayList(temps);
      this.tick = standOn;
      this.run = false;
      this.load_LongOpenHashSet = standOn ? new LongOpenHashSet(temps) : null;
      this.avoidSwap = avoidSwap;
      return new PrintAccess.Inner_ConfigStore(start, feet, new IntArrayList(), tempState, this.view::workUnits);
   }

   PrintAccess.Inner_ConfigStore startStandingRemovalSearch(
      BlockPos start, double feet, LongArrayList temps, LongArrayList standingTemps, BlockState tempState, long avoidSwap
   ) {
      this.init_LongArrayList = new LongArrayList(temps);
      this.load_LongOpenHashSet = new LongOpenHashSet(standingTemps);
      this.tick = false;
      this.run = true;
      this.avoidSwap = avoidSwap;
      return new PrintAccess.Inner_ConfigStore(start, feet, new IntArrayList(), tempState, this.view::workUnits);
   }

   PrintAccess.Inner_BufferHelper init_StreamUtil_BufferH_BlockPos(BlockPos start, double startFeet, IntArrayList goals, BlockState tempState, int maxNodes) {
      this.resetSearchCaches();
      if (goals.isEmpty() && !this.flush_Z && this.init_LongArrayList == null && this.load_LongArrayList == null && this.init_LongOpenHashSet == null) {
         return null;
      }

      if (!goals.isEmpty()) {
         this.lowestGoalY = lowestGoalY(this.core, goals);
      }

      this.tempState = tempState;
      this.get = tempState != null;
      Long2DoubleOpenHashMap best = new Long2DoubleOpenHashMap();
      best.defaultReturnValue(1.0 / 0.0);
      Long2ObjectOpenHashMap<Boolean> goalCache = new Long2ObjectOpenHashMap<>();
      PriorityQueue<Object[]> open = new PriorityQueue<>((a, b) -> Double.compare((Double)a[0], (Double)b[0]));
      PrintAccess.Inner_CachePool root = new PrintAccess.Inner_CachePool(start.asLong(), startFeet, 0.0, null, null, 0);
      this.rootKey = root.key;
      this.rootCanEscape = null;
      this.load_Boolean = null;
      best.put(root.key, 0.0);
      open.add(new Object[]{this.close_D * this.init_D_J_IntArrayList(root.key, goals), root});
      PrintClicks.Inner_DataUtil budget = new PrintClicks.Inner_DataUtil(120000);
      int expanded = 0;
      PrintAccess.Inner_CachePool closest = root;
      double closestH = this.init_D_J_IntArrayList(root.key, goals);
      long work0 = this.view.workUnits();

      while (!open.isEmpty()) {
         PrintAccess.Inner_CachePool n = (PrintAccess.Inner_CachePool)open.poll()[1];
         if (!(n.g > best.get(n.key) + 1.0E-9)) {
            if (!this.poll_Z && this.view.workUnits() - work0 > this.init_J) {
               this.failureDetail = "work budget after " + expanded + " nodes, closest " + BlockPos.of(closest.key).toShortString() + " h " + (int)closestH;
               return null;
            }

            if (++expanded > maxNodes || budget.isExhausted()) {
               this.failureDetail = (budget.isExhausted() ? "click budget" : "node budget")
                  + " after "
                  + expanded
                  + " nodes, closest "
                  + BlockPos.of(closest.key).toShortString()
                  + " h "
                  + (int)closestH;
               return null;
            }

            double nh = this.init_D_J_IntArrayList(n.key, goals);
            if (nh < closestH) {
               closestH = nh;
               closest = n;
            }

            if (n.parent != null || this.poll_Z || this.init_LongArrayList != null && this.freshClickRetry) {
               if (this.init_LongArrayList != null) {
                  long t = this.load(n, budget);
                  if (t != -9223372036854775808L && this.init_Z_StreamUtil_CachePo_D(n, startFeet)) {
                     this.foundGoalKey = t;
                     return this.init_StreamUtil_BufferH_StreamUtil_CachePo_I(n, -1);
                  }
               } else if (this.load_LongArrayList != null) {
                  long t = this.init_J_StreamUtil_CachePo_ConfigStore_DataUt(n, budget);
                  if (t != -9223372036854775808L) {
                     this.foundGoalKey = t;
                     return this.init_StreamUtil_BufferH_StreamUtil_CachePo_I(n, -1);
                  }
               } else if (this.init_LongOpenHashSet != null) {
                  if (this.init_LongOpenHashSet.contains(n.key) && this.init_Z_StreamUtil_CachePo_D(n, startFeet)) {
                     this.foundGoalKey = n.key;
                     return this.init_StreamUtil_BufferH_StreamUtil_CachePo_I(n, -1);
                  }
               } else if (this.flush_Z) {
                  int x = BlockPos.getX(n.key);
                  int y = BlockPos.getY(n.key);
                  int z = BlockPos.getZ(n.key);
                  if (this.push_Z
                     ? this.read(n)
                     : this.core.isWithinFootprint(x, z) && this.read(n) || y > this.core.maxTargetY() || this.init_Z_StreamUtil_CachePo(n)) {
                     return this.init_StreamUtil_BufferH_StreamUtil_CachePo_I(n, -1);
                  }
               } else {
                  int g = this.init_I_StreamUtil_CachePo(n, goals, goalCache, budget);
                  if (g >= 0 && this.init_Z_StreamUtil_CachePo_D(n, startFeet)) {
                     return this.init_StreamUtil_BufferH_StreamUtil_CachePo_I(n, g);
                  }
               }
            }

            this.init_V_StreamUtil_CachePo(n, goals, tempState, open, best);
         }
      }

      this.failureDetail = "exhausted after " + expanded + " nodes, closest " + BlockPos.of(closest.key).toShortString() + " h " + (int)closestH;
      return null;
   }

   PrintAccess.Inner_BufferHelper planExit(
      BlockPos start, double feet, BlockState tempState, int maxNodes, boolean groundOnly, int maxTemps
   ) {
      this.flush_Z = true;
      this.poll_Z = true;
      this.push_Z = groundOnly;
      this.write_I = maxTemps;

      try {
         return this.init_StreamUtil_BufferH_BlockPos(start, feet, new IntArrayList(), tempState, maxNodes);
      } finally {
         this.flush_Z = false;
         this.poll_Z = false;
         this.push_Z = false;
         this.write_I = 2147483647;
      }
   }

   PrintAccess.Inner_BufferHelper planAccess(BlockPos start, double startFeet, BlockState tempState, int maxNodes) {
      this.flush_Z = true;

      try {
         return this.init_StreamUtil_BufferH_BlockPos(start, startFeet, new IntArrayList(), tempState, maxNodes);
      } finally {
         this.flush_Z = false;
      }
   }

   PrintAccess.Inner_BufferHelper init_StreamUtil_BufferH_BlockPos(
      BlockPos start, double startFeet, LongArrayList temps, BlockState tempState, int maxNodes, boolean standOn
   ) {
      this.init_LongArrayList = temps;
      this.tick = standOn;
      this.run = false;
      if (standOn) {
         this.load_LongOpenHashSet = new LongOpenHashSet(temps);
      }

      try {
         return this.init_StreamUtil_BufferH_BlockPos(start, startFeet, new IntArrayList(), tempState, maxNodes);
      } finally {
         this.init_LongArrayList = null;
         this.load_LongOpenHashSet = null;
      }
   }

   PrintAccess.Inner_BufferHelper planToStances(
      BlockPos start, double startFeet, LongOpenHashSet stances, BlockState tempState, int maxNodes
   ) {
      this.init_LongOpenHashSet = stances;

      try {
         return this.init_StreamUtil_BufferH_BlockPos(start, startFeet, new IntArrayList(), tempState, maxNodes);
      } finally {
         this.init_LongOpenHashSet = null;
      }
   }

   PrintAccess.Inner_ConfigStore startCellSearch(BlockPos start, double feet, LongArrayList cells, BlockState state, Item item) {
      return this.startCellSearch(start, feet, cells, state, item, state);
   }

   PrintAccess.Inner_ConfigStore startCellSearch(
      BlockPos start, double feet, LongArrayList cells, BlockState state, Item item, BlockState pathState
   ) {
      this.load_LongArrayList = new LongArrayList(cells);
      this.init_BlockState = state;
      this.init_Item = item;
      return new PrintAccess.Inner_ConfigStore(start, feet, new IntArrayList(), pathState, this.view::workUnits);
   }

   PrintAccess.Inner_BufferHelper planToCells(
      BlockPos start, double startFeet, LongArrayList cells, BlockState tempState, Item item, int maxNodes
   ) {
      return this.init(start, startFeet, cells, tempState, item, tempState, maxNodes);
   }

   PrintAccess.Inner_BufferHelper init(BlockPos start, double startFeet, LongArrayList cells, BlockState tempState, Item item, BlockState pathTemp, int maxNodes) {
      this.load_LongArrayList = cells;
      this.init_BlockState = tempState;
      this.init_Item = item;

      try {
         return this.init_StreamUtil_BufferH_BlockPos(start, startFeet, new IntArrayList(), pathTemp, maxNodes);
      } finally {
         this.load_LongArrayList = null;
      }
   }

   private long init_J_StreamUtil_CachePo_ConfigStore_DataUt(PrintAccess.Inner_CachePool n, PrintClicks.Inner_DataUtil budget) {
      this.init_ConfigStore_Stream = null;
      long started = this.view.workUnits();
      int x = BlockPos.getX(n.key);
      int y = BlockPos.getY(n.key);
      int z = BlockPos.getZ(n.key);
      LongArrayList assumed = this.init_LongArrayList_StreamUtil_CachePo(n);

      try {
         List<Vec3> points = PrintClicks.getCandidateFeetPositions(this.view, n.key, n.feet);

         for (long t : this.load_LongArrayList) {
            int tx = BlockPos.getX(t);
            int ty = BlockPos.getY(t);
            int tz = BlockPos.getZ(t);
            if (Math.abs(tx - x) <= 4 && Math.abs(tz - z) <= 4 && ty - y <= 4 && y - ty <= 5 && (tx != x || tz != z || ty != y && ty != y + 1)) {
               BlockPos p = BlockPos.of(t);
               if (this.view.getBlockState(p).canBeReplaced()) {
                  PrintTarget probe = new PrintTarget(-1, p, this.init_BlockState, PrintTarget.Inner_DataUtil.PLACE, this.init_Item, null);

                  for (Vec3 feet : points) {
                     PrintClicks.Inner_StreamUtil click = PrintClicks.findClick(
                        this.view,
                        this.core.env,
                        this.core.rules,
                        probe,
                        this.init_Item,
                        PrintClicks.Inner_CachePool.PLACE,
                        feet,
                        true,
                        false,
                        budget,
                        4.1000000000000005
                     );
                     if (click != null) {
                        PrintCore.P proof = this.core.new P(click, this.core.reachable);

                        while (!proof.complete && this.view.workUnits() - started <= this.init_J && !budget.isExhausted()) {
                           proof.init(1L);
                        }

                        if (!proof.complete || proof.read) {
                           return -9223372036854775808L;
                        }

                        if (!proof.load) {
                           this.init_ConfigStore_Stream = click;
                           return t;
                        }

                        if (!proof.write) {
                           break;
                        }
                     }
                  }
               }
            }
         }

         return -9223372036854775808L;
      } finally {
         this.popHypotheses(assumed);
      }
   }

   PrintClicks.Inner_StreamUtil init_ConfigStore_Stream() {
      return this.load_ConfigStore_Stream;
   }

   private long load(PrintAccess.Inner_CachePool n, PrintClicks.Inner_DataUtil budget) {
      this.load_ConfigStore_Stream = null;
      if (this.retrying && n.key == this.rootKey) {
         return -9223372036854775808L;
      }

      int x = BlockPos.getX(n.key);
      int y = BlockPos.getY(n.key);
      int z = BlockPos.getZ(n.key);
      if (this.tick || this.run) {
         long floor = BlockPos.asLong(x, y - 1, z);
         BlockPos under = BlockPos.of(floor);
         if ((n.temps <= 0 || floor != this.avoidSwap)
            && n.feet == y
            && this.load_LongOpenHashSet.contains(floor)
            && !this.core.temporaryBlocks.containsKey(BlockPos.asLong(x, y - 2, z))
            && n.via != null
            && n.via.kind == PrintAccess.Inner_StreamUtil.WALK
            && !this.core.sync_Z_J(floor)
            && this.core.update_Z_BlockPos(under)
            && this.core.write_Z_BlockPos_BlockPos(under, under)) {
            return floor;
         }

         if (this.tick) {
            return -9223372036854775808L;
         }
      }

      LongArrayList assumed = this.init_LongArrayList_StreamUtil_CachePo(n);

      try {
         for (long t : this.init_LongArrayList) {
            int tx = BlockPos.getX(t);
            int ty = BlockPos.getY(t);
            int tz = BlockPos.getZ(t);
            if (Math.abs(tx - x) <= 4
               && Math.abs(tz - z) <= 4
               && ty - y <= 4
               && y - ty <= 5
               && !this.core.sync_Z_J(t)
               && (tx != x || tz != z || ty != y - 1)
               && !this.core.failedDigStances.contains(PrintCore.cellStanceKey((int)(t & 2147483647L), n.key))) {
               BlockPos p = BlockPos.of(t);
               if (!this.view.getBlockState(p).isAir()
                  && (n.temps <= 0 || t != this.avoidSwap || this.core.canEscape(BlockPos.of(n.key), p, Blocks.AIR.defaultBlockState(), true))) {
                  PrintTarget probe = new PrintTarget(-1, p, this.view.getBlockState(p), PrintTarget.Inner_DataUtil.PLACE, Items.AIR, null);
                  PrintClicks.Inner_StreamUtil click = this.core.findClickFrom(n.key, n.feet, probe, budget, n.key == this.rootKey);
                  if (click != null && this.core.write_Z_BlockPos_BlockPos(BlockPos.of(n.key), p)) {
                     this.load_ConfigStore_Stream = click;
                     return t;
                  }
               }
            }
         }

         return -9223372036854775808L;
      } finally {
         this.popHypotheses(assumed);
      }
   }

   private void resetSearchCaches() {
      this.neighborCountCache.clear();
      this.indexedGoals = null;
      this.goalIndex = null;
      this.indexedCells = null;
      this.cellIndex = null;
   }

   private PrintSpatialIndex cellSpatialIndex(LongCollection goals) {
      if (this.indexedCells != goals) {
         long[] cells = goals.toLongArray();
         BlockPos[] positions = new BlockPos[cells.length];

         for (int i = 0; i < cells.length; i++) {
            positions[i] = BlockPos.of(cells[i]);
         }

         this.cellIndex = new PrintSpatialIndex(positions.length, ix -> positions[ix]);
         this.indexedCells = goals;
      }

      return this.cellIndex;
   }

   private double init_D_J_IntArrayList(long key, IntArrayList goals) {
      if (this.init_LongOpenHashSet != null) {
         int x = BlockPos.getX(key);
         int y = BlockPos.getY(key);
         int z = BlockPos.getZ(key);
         if (this.init_LongOpenHashSet.size() >= 32) {
            return this.cellSpatialIndex(this.init_LongOpenHashSet).init(x, y, z, (dx, dy, dz) -> init(0, 0, 0, dx, dy, dz, 0.0, 1.0));
         }

         double best = 1.0 / 0.0;

         for (long t : this.init_LongOpenHashSet) {
            best = Math.min(best, init(x, y, z, BlockPos.getX(t), BlockPos.getY(t), BlockPos.getZ(t), 0.0, 1.0));
            if (best == 0.0) {
               return 0.0;
            }
         }

         return best;
      } else if (this.init_LongArrayList == null && this.load_LongArrayList == null) {
         if (this.flush_Z) {
            int x = BlockPos.getX(key);
            int y = BlockPos.getY(key);
            int z = BlockPos.getZ(key);
            if (this.push_Z) {
               return Math.max(0, y - this.core.baseY) * 3.033333333333333;
            }

            PrintNav.Inner_DataUtil bounds = this.core.bounds;
            double edge = Math.max(
               0, Math.min(Math.min(x - (bounds.minX() + 6) + 1, bounds.maxX() - 6 - x + 1), Math.min(z - (bounds.minZ() + 6) + 1, bounds.maxZ() - 6 - z + 1))
            );
            double up = Math.max(0, this.core.maxTargetY() - y) / 1.85;
            return Math.min(edge, up) * 4.6;
         } else {
            int x = BlockPos.getX(key);
            int y = BlockPos.getY(key);
            int z = BlockPos.getZ(key);
            if (goals.size() >= 32) {
               if (this.indexedGoals != goals) {
                  this.indexedGoals = goals;
                  this.goalIndex = new PrintSpatialIndex(goals, i -> this.core.targets[i].pos());
               }

               return this.goalIndex.init(x, y, z, (dx, dy, dz) -> this.init_D_I(0, 0, 0, dx, dy, dz));
            } else {
               double best = 1.0 / 0.0;

               for (int g : goals) {
                  BlockPos p = this.core.targets[g].pos();
                  best = Math.min(best, this.init_D_I(x, y, z, p.getX(), p.getY(), p.getZ()));
                  if (best == 0.0) {
                     return 0.0;
                  }
               }

               return best;
            }
         }
      } else {
         int x = BlockPos.getX(key);
         int y = BlockPos.getY(key);
         int z = BlockPos.getZ(key);
         LongArrayList cells = this.init_LongArrayList != null ? this.init_LongArrayList : this.load_LongArrayList;
         if (cells.size() >= 32) {
            PrintSpatialIndex geometry = this.cellSpatialIndex(cells);
            if (this.tick && this.init_LongArrayList != null) {
               return geometry.init(x, y - 1, z, (dx, dy, dz) -> init(0, 0, 0, dx, dy, dz, 1.0, 1.0));
            } else {
               return this.alternateGuide && this.init_LongArrayList != null
                  ? geometry.init(x, y, z, (dx, dy, dz) -> this.init_D_I(0, 0, 0, dx, dy, dz))
                  : geometry.init(x, y, z, (dx, dy, dz) -> load(0, 0, 0, dx, dy, dz));
            }
         } else {
            double best = 1.0 / 0.0;

            for (long t : cells) {
               if (this.tick && this.init_LongArrayList != null) {
                  best = Math.min(best, init(x, y, z, BlockPos.getX(t), BlockPos.getY(t) + 1, BlockPos.getZ(t), 1.0, 1.0));
                  if (best == 0.0) {
                     return 0.0;
                  }
               } else {
                  best = Math.min(
                     best,
                     this.alternateGuide && this.init_LongArrayList != null
                        ? this.init_D_I(x, y, z, BlockPos.getX(t), BlockPos.getY(t), BlockPos.getZ(t))
                        : init_D_I(x, y, z, BlockPos.of(t))
                  );
                  if (best == 0.0) {
                     return 0.0;
                  }
               }
            }

            return best;
         }
      }
   }

   private double init_D_I(int x, int y, int z, int tx, int ty, int tz) {
      double relaxed = init(x, y, z, tx, ty, tz, 6.4 + Math.sqrt(2.0) * 0.75, 8.02);
      return this.alternateGuide ? Math.max(relaxed, Math.max(0, ty - y - 3) * (12.0 + this.tempCost)) : relaxed;
   }

   private static double init_D_I(int x, int y, int z, BlockPos target) {
      return load(x, y, z, target.getX(), target.getY(), target.getZ());
   }

   private static double load(int x, int y, int z, int tx, int ty, int tz) {
      double horizontal = 6.4 + Math.sqrt(2.0) * 0.75;
      double vertical = 8.02;
      return init(x, y, z, tx, ty, tz, horizontal, vertical);
   }

   private static double init(int x, int y, int z, int tx, int ty, int tz, double horizontalSlack, double verticalSlack) {
      double across = Math.max(0.0, Math.hypot(tx - x, tz - z) - horizontalSlack) * Math.min(4.6, 6.5 / Math.sqrt(2.0));
      double up = Math.max(0.0, ty - y - verticalSlack) * Math.min(12.0, 2.486486486486486);
      double down = Math.max(0.0, y - ty - verticalSlack) * Math.min(15.0, 3.033333333333333);
      return Math.max(across, Math.max(up, down));
   }

   private PrintAccess.Inner_BufferHelper init_StreamUtil_BufferH_StreamUtil_CachePo_I(PrintAccess.Inner_CachePool n, int goal) {
      List<PrintAccess.Inner_NetHelper> steps = new ArrayList<>();
      double cost = n.g;
      int temps = n.temps;

      for (PrintAccess.Inner_CachePool at = n; at.via != null; at = at.parent) {
         steps.add(at.via);
      }

      Collections.reverse(steps);
      return new PrintAccess.Inner_BufferHelper(steps, cost, temps, goal);
   }

   private LongArrayList init_LongArrayList_StreamUtil_CachePo(PrintAccess.Inner_CachePool n) {
      ArrayDeque<PrintAccess.Inner_NetHelper> steps = new ArrayDeque<>();

      for (PrintAccess.Inner_CachePool at = n; at != null && at.via != null; at = at.parent) {
         steps.push(at.via);
      }

      LongArrayList assumed = new LongArrayList();

      for (PrintAccess.Inner_NetHelper s : steps) {
         if (s.kind == PrintAccess.Inner_StreamUtil.PILLAR || s.kind == PrintAccess.Inner_StreamUtil.BRIDGE) {
            int i = this.core.cellIndexByPos.get(s.block);
            if (s.own && i >= 0) {
               this.view.pushHypothesis(BlockPos.of(s.block), this.core.targets[i].wanted());
            } else {
               this.view.pushTemporaryHypothesis(BlockPos.of(s.block), this.tempState());
            }

            assumed.add(s.block);
         } else if (s.kind == PrintAccess.Inner_StreamUtil.DIG || s.kind == PrintAccess.Inner_StreamUtil.BREAK) {
            this.view.pushHypothesis(BlockPos.of(s.block), Blocks.AIR.defaultBlockState());
            assumed.add(s.block);
         } else if (s.kind == PrintAccess.Inner_StreamUtil.TUNNEL) {
            long head = BlockPos.offset(s.block, Direction.UP);
            this.view.pushHypothesis(BlockPos.of(s.block), Blocks.AIR.defaultBlockState());
            assumed.add(s.block);
            this.view.pushHypothesis(BlockPos.of(head), Blocks.AIR.defaultBlockState());
            assumed.add(head);
         }
      }

      return assumed;
   }

   private void popHypotheses(LongArrayList assumed) {
      for (int i = assumed.size() - 1; i >= 0; i--) {
         this.view.popHypothesis(BlockPos.of(assumed.getLong(i)));
      }
   }

   private boolean init_Z_StreamUtil_CachePo_D(PrintAccess.Inner_CachePool n, double startFeet) {
      Boolean back = null;
      if (!this.flush_Z
         && (
            this.init_LongArrayList != null
               || this.core.phase == PrintCore.J.TEARDOWN
               || this.core.isAboveBase(BlockPos.of(n.key))
               || this.core.push_Z() && this.core.isAboveBase(BlockPos.of(this.rootKey))
         )) {
         if (this.load_Boolean == null) {
            this.load_Boolean = this.core.canEscape(BlockPos.of(this.rootKey), null, null, true);
         }

         if (this.load_Boolean && !(back = this.load(n))) {
            LongArrayList assumed = this.init_LongArrayList_StreamUtil_CachePo(n);

            try {
               if (!this.core.canEscape(BlockPos.of(n.key), null, null, true)) {
                  return false;
               }
            } finally {
               this.popHypotheses(assumed);
            }
         }
      }

      boolean drops = n.feet < startFeet - 0.5;

      for (PrintAccess.Inner_CachePool at = n; !drops && at != null && at.parent != null; at = at.parent) {
         if (at.via != null && at.via.kind == PrintAccess.Inner_StreamUtil.WALK && at.feet < at.parent.feet - 0.6 - 1.0E-6) {
            drops = true;
         }
      }

      if (!drops) {
         return true;
      }

      if (this.rootCanEscape == null) {
         this.rootCanEscape = this.core.canEscape(BlockPos.of(this.rootKey), null, null);
      }

      if (!this.rootCanEscape) {
         return true;
      }

      if (back == null) {
         back = this.load(n);
      }

      if (back) {
         return true;
      }

      LongArrayList assumed = this.init_LongArrayList_StreamUtil_CachePo(n);

      try {
         return this.core.canEscape(BlockPos.of(n.key), null, null);
      } finally {
         this.popHypotheses(assumed);
      }
   }

   private boolean load(PrintAccess.Inner_CachePool n) {
      LongArrayList assumed = this.init_LongArrayList_StreamUtil_CachePo(n);

      try {
         for (PrintAccess.Inner_CachePool at = n; at != null && at.parent != null; at = at.parent) {
            PrintAccess.Inner_NetHelper s = at.via;
            PrintAccess.Inner_CachePool from = at.parent;
            int fx = BlockPos.getX(from.key);
            int fy = BlockPos.getY(from.key);
            int fz = BlockPos.getZ(from.key);
            if (s.kind == PrintAccess.Inner_StreamUtil.WALK) {
               int tx = BlockPos.getX(at.key);
               int tz = BlockPos.getZ(at.key);
               if (Double.isNaN(PrintNav.stepCost(this.view, tx, tz, at.feet, fx, fz, from.feet, tx != fx && tz != fz))) {
                  return false;
               }
            } else {
               if (s.kind != PrintAccess.Inner_StreamUtil.PILLAR || s.own || assumed.isEmpty() || assumed.getLong(assumed.size() - 1) != s.block) {
                  return false;
               }

               this.view.popHypothesis(BlockPos.of(assumed.removeLong(assumed.size() - 1)));
            }

            double h = this.view.standingHeightAt(fx, fy, fz);
            if (Double.isNaN(h) || Math.abs(h - from.feet) > 0.001) {
               return false;
            }
         }

         return true;
      } finally {
         this.popHypotheses(assumed);
      }
   }

   private boolean read(PrintAccess.Inner_CachePool n) {
      long floor = BlockPos.asLong(BlockPos.getX(n.key), (int)Math.floor(n.feet - 0.001), BlockPos.getZ(n.key));
      int k = 0;

      for (PrintAccess.Inner_CachePool at = n; at != null && at.via != null && k < 64; k++) {
         if ((at.via.kind == PrintAccess.Inner_StreamUtil.PILLAR || at.via.kind == PrintAccess.Inner_StreamUtil.BRIDGE) && at.via.block == floor) {
            return false;
         }

         at = at.parent;
      }

      return this.core.pop_Z_BlockPos(BlockPos.of(floor));
   }

   private static boolean init_Z_StreamUtil_CachePo_BlockPos(PrintAccess.Inner_CachePool n, BlockPos cell) {
      int k = 0;

      for (PrintAccess.Inner_CachePool at = n; at != null && at.via != null && k < 64; k++) {
         PrintAccess.Inner_NetHelper s = at.via;
         if ((s.kind == PrintAccess.Inner_StreamUtil.PILLAR || s.kind == PrintAccess.Inner_StreamUtil.BRIDGE) && !s.own) {
            long b = s.block;
            if (BlockPos.getX(b) == cell.getX() && BlockPos.getZ(b) == cell.getZ() && BlockPos.getY(b) < cell.getY() && BlockPos.getY(b) >= cell.getY() - 3) {
               return true;
            }
         }

         at = at.parent;
      }

      return false;
   }

   private BlockState tempState() {
      return this.tempState;
   }

   private int init_I_StreamUtil_CachePo(
      PrintAccess.Inner_CachePool n, IntArrayList goals, Long2ObjectOpenHashMap<Boolean> cache, PrintClicks.Inner_DataUtil budget
   ) {
      int x = BlockPos.getX(n.key);
      int y = BlockPos.getY(n.key);
      int z = BlockPos.getZ(n.key);
      boolean near = false;

      for (int g : goals) {
         BlockPos p = this.core.targets[g].pos();
         if (Math.abs(p.getX() - x) <= 5 && Math.abs(p.getZ() - z) <= 5 && p.getY() - y <= 4 && y - p.getY() <= 6) {
            near = true;
            break;
         }
      }

      if (!near) {
         return -1;
      }

      if (n.via != null && n.via.own) {
         int i = this.core.cellIndexByPos.get(n.via.block);
         if (i >= 0 && goals.contains(i)) {
            return i;
         }
      }

      double ex = x + 0.5;
      double ey = n.feet + 1.62;
      double ez = z + 0.5;
      IntArrayList inReach = new IntArrayList();

      for (int g : goals) {
         BlockPos p = this.core.targets[g].pos();
         if (!(p.distToCenterSqr(ex, ey, ez) > square(5.6000000000000005))
            && Math.abs(x - p.getX()) <= 4
            && Math.abs(z - p.getZ()) <= 4
            && y - p.getY() >= -5
            && y - p.getY() <= 3
            && !this.core.refusedCellStances.contains(PrintCore.cellStanceKey(g, n.key))
            && !this.core.init_Z_I(g, n.feet, n.key)) {
            inReach.add(g);
         }
      }

      if (inReach.isEmpty()) {
         return -1;
      }

      inReach.sort((a1, b1) -> this.init_I_I(a1, b1, ex, ey, ez));
      if (inReach.size() > 12) {
         inReach.size(12);
      }

      LongArrayList assumed = this.init_LongArrayList_StreamUtil_CachePo(n);

      try {
         for (int g : inReach) {
            BlockPos p = this.core.targets[g].pos();
            PrintTarget tg = this.core.targets[g];
            List<Vec3> points = PrintClicks.getCandidateFeetPositions(
               this.view, n.key, n.feet, PrintClicks.isOrientationSensitive(tg, this.core.constraintFor(g))
            );
            boolean pour = tg.kind() == PrintTarget.Inner_DataUtil.FLUID;
            boolean waterlog = !pour && this.core.cellStates[g] == 2;
            if (!pour && !waterlog || this.core.init_Z_J_ListPool(n.key, tg)) {
               for (Vec3 feet : points) {
                  boolean exact = !pour && !waterlog && this.view.hasNoHypothesisInNeighborhood(p);
                  PrintClicks.Inner_StreamUtil geo = !pour && !waterlog
                     ? PrintClicks.findPlacementClick(
                        this.view,
                        this.core.env,
                        this.core.rules,
                        tg,
                        feet,
                        exact,
                        budget,
                        4.1000000000000005,
                        this.core.constraintFor(g)
                     )
                     : PrintClicks.findClick(
                        this.view,
                        this.core.env,
                        this.core.rules,
                        tg,
                        PrintClicks.getFluidBucketItem(tg),
                        waterlog ? PrintClicks.Inner_CachePool.WATERLOG : PrintClicks.Inner_CachePool.POUR,
                        feet,
                        !waterlog,
                        false,
                        budget,
                        4.1000000000000005
                     );
                  if (geo != null) {
                     return g;
                  }
               }

               if (!pour && !waterlog && this.init_Z_ListPool(tg, p, points, budget)) {
                  return g;
               }
            }
         }

         return -1;
      } finally {
         this.popHypotheses(assumed);
      }
   }

   private boolean init_Z_ListPool(PrintTarget tg, BlockPos p, List<Vec3> points, PrintClicks.Inner_DataUtil budget) {
      if (!this.get) {
         return false;
      }

      BlockState tempState = this.tempState();
      Item temp = tempState == null ? null : tempState.getBlock().asItem();

      for (Direction d : Direction.values()) {
         BlockPos sn = p.relative(d);
         if (this.core.init_Z_BlockPos_BlockPos(sn, p)) {
            PrintTarget probe = new PrintTarget(-1, sn, tempState, PrintTarget.Inner_DataUtil.PLACE, temp, null);

            for (Vec3 feet : points) {
               if (PrintClicks.findClick(
                     this.view,
                     this.core.env,
                     this.core.rules,
                     probe,
                     temp,
                     PrintClicks.Inner_CachePool.PLACE,
                     feet,
                     true,
                     false,
                     budget,
                     4.1000000000000005
                  )
                  != null) {
                  this.view.pushHypothesis(sn, tempState);

                  try {
                     PrintClicks.Inner_StreamUtil targetClick = PrintClicks.findPlacementClick(
                        this.view,
                        this.core.env,
                        this.core.rules,
                        tg,
                        feet,
                        false,
                        budget,
                        4.1000000000000005,
                        this.core.constraintFor(tg.index()),
                        sn
                     );
                     Vec3 from = feet;

                     for (int k = 0; targetClick == null && k < points.size(); k++) {
                        Vec3 other = points.get(k);
                        if (!other.equals(feet)) {
                           targetClick = PrintClicks.findPlacementClick(
                              this.view,
                              this.core.env,
                              this.core.rules,
                              tg,
                              other,
                              false,
                              budget,
                              4.1000000000000005,
                              this.core.constraintFor(tg.index()),
                              sn
                           );
                           from = other;
                        }
                     }

                     if (targetClick != null) {
                        if (this.core.trace) {
                           this.core
                              .env
                              .logTrace(
                                 "access support " + sn.toShortString() + " for " + p.toShortString() + " from " + from + " crouch=" + targetClick.crouch()
                              );
                        }

                        return true;
                     }
                     break;
                  } finally {
                     this.view.popHypothesis(sn);
                  }
               }
            }
         }
      }

      return false;
   }

   private boolean init_Z_StreamUtil_CachePo_IntArrayList(PrintAccess.Inner_CachePool node, IntArrayList goals) {
      double eye = node.feet + 1.62;
      int x = BlockPos.getX(node.key);
      int z = BlockPos.getZ(node.key);
      double radius = 6.1000000000000005;

      for (int goal : goals) {
         BlockPos target = this.core.targets[goal].pos();
         if (Math.abs(target.getY() + 0.5 - eye) <= 4.1000000000000005 && Math.hypot(target.getX() - x, target.getZ() - z) <= radius) {
            return true;
         }
      }

      return false;
   }

   private void init_V_StreamUtil_CachePo(
      PrintAccess.Inner_CachePool n, IntArrayList goals, BlockState tempState, PriorityQueue<Object[]> open, Long2DoubleOpenHashMap best
   ) {
      int x = BlockPos.getX(n.key);
      int y = BlockPos.getY(n.key);
      int z = BlockPos.getZ(n.key);
      double h = n.feet;
      PrintNav.Inner_DataUtil bounds = this.bounds;
      LongArrayList assumed = this.init_LongArrayList_StreamUtil_CachePo(n);

      try {
         int highestWalk = PrintNav.maxReachableStepY(this.view, x, z, h);
         int lowestWalk = PrintView.floorInt(h - 3.0);
         if (!this.open_Z || this.init_Z_StreamUtil_CachePo_IntArrayList(n, goals)) {
            for (int[] d : NEIGHBOR_OFFSETS_8) {
               int nx = x + d[0];
               int nz = z + d[1];

               for (int ny = highestWalk; ny >= lowestWalk; ny--) {
                  if (bounds.contains(nx, ny, nz)) {
                     double h2 = this.view.standingHeightAt(nx, ny, nz);
                     if (!Double.isNaN(h2)) {
                        double step = PrintNav.stepCost(this.view, x, z, h, nx, nz, h2, d[0] != 0 && d[1] != 0);
                        if (!Double.isNaN(step)) {
                           this.init_V_StreamUtil_CachePo(
                              new PrintAccess.Inner_CachePool(
                                 BlockPos.asLong(nx, ny, nz),
                                 h2,
                                 n.g + step,
                                 new PrintAccess.Inner_NetHelper(
                                    PrintAccess.Inner_StreamUtil.WALK, n.key, BlockPos.asLong(nx, ny, nz), h2, -9223372036854775808L, false, null
                                 ),
                                 n,
                                 n.temps
                              ),
                              goals,
                              open,
                              best
                           );
                        }
                        break;
                     }
                  }
               }
            }
         }

         boolean canConstruct = !this.push_Z && h >= this.open_D && h <= this.poll_D;
         boolean fullFloor = canConstruct
            && h == y
            && this.view.getBlockState(new BlockPos(x, y - 1, z)).isFaceSturdy(this.view, new BlockPos(x, y - 1, z), Direction.UP);
         BlockPos feetCell = this.view.getBlockState(new BlockPos(x, y, z)).isAir() ? new BlockPos(x, y, z) : new BlockPos(x, y + 1, z);
         int py = feetCell.getY();
         if (canConstruct
            && this.core.init_Z_I_2(x, py, z)
            && bounds.contains(x, py + 1, z)
            && this.view.isEmptyOutlineCell(feetCell.above())
            && this.view.isEmptyOutlineCell(feetCell.above(2))
            && !this.core.refusedPillarCells.contains(feetCell.asLong())) {
            int i = this.core.cellIndexByPos.get(feetCell.asLong());
            boolean own = i >= 0
               && this.core.init_Z_BlockPos_IntSet(feetCell, this.init_IntSet_StreamUtil_CachePo(n))
               && !init_Z_StreamUtil_CachePo_BlockPos(n, feetCell);
            boolean buries = !own && !this.init_Z_BlockPos(feetCell);
            boolean temp = !own
               && n.temps < this.write_I
               && this.core.pop_Z_I(i)
               && tempState != null
               && (this.allowBury || !buries)
               && !this.load(n, feetCell)
               && !this.init_Z_BlockPos_Z(feetCell, false)
               && this.init_Z_BlockPos_BlockState(feetCell, tempState);
            double bury = temp && buries ? 60.0 : 0.0;
            if (own || temp) {
               this.init_V_StreamUtil_CachePo(
                  new PrintAccess.Inner_CachePool(
                     BlockPos.asLong(x, py + 1, z),
                     py + 1,
                     n.g + 12.0 + (temp ? this.tempCost + bury : 0.0),
                     new PrintAccess.Inner_NetHelper(
                        PrintAccess.Inner_StreamUtil.PILLAR, n.key, BlockPos.asLong(x, py + 1, z), py + 1, feetCell.asLong(), own, Direction.UP
                     ),
                     n,
                     n.temps + (temp ? 1 : 0)
                  ),
                  goals,
                  open,
                  best
               );
            }
         }

         if (!this.open_Z || this.init_Z_StreamUtil_CachePo_IntArrayList(n, goals)) {
            BlockPos floor = new BlockPos(x, y - 1, z);
            if (fullFloor) {
               for (Direction d : Plane.HORIZONTAL) {
                  BlockPos cell = floor.relative(d);
                  if (!this.core.abandonedCells.contains(cell.asLong())) {
                     BlockPos stand = cell.above();
                     if (bounds.contains(stand.getX(), stand.getY(), stand.getZ())) {
                        BlockState there = this.view.getBlockState(cell);
                        if ((there.isAir() || there.canBeReplaced())
                           && this.view.isPassableCell(stand)
                           && this.view.isPassableCell(stand.above())
                           && this.view.getBlockState(floor).isFaceSturdy(this.view, floor, d)) {
                           double off = 0.75;
                           AABB lean = PrintGeometry.init(x + 0.5 + d.getStepX() * off, h, z + 0.5 + d.getStepZ() * off, true);
                           if (this.view.isBoxClear(lean)) {
                              int i = this.core.cellIndexByPos.get(cell.asLong());
                              boolean own = i >= 0 && this.core.cellStates[i] == 0 && this.init_Z_I_Direction(i, d) && this.core.read_Z_I(i);
                              boolean buries = !own && !this.init_Z_BlockPos(cell);
                              if (own || !this.init_Z_I(x, y, z)) {
                                 boolean temp = !own
                                    && n.temps < this.write_I
                                    && this.core.pop_Z_I(i)
                                    && tempState != null
                                    && (this.allowBury || !buries)
                                    && !this.load(n, cell)
                                    && !this.init_Z_BlockPos_Z(cell, true)
                                    && this.init_Z_BlockPos_BlockState(cell, tempState);
                                 double bury = temp && buries ? 60.0 : 0.0;
                                 if (own || temp) {
                                    this.init_V_StreamUtil_CachePo(
                                       new PrintAccess.Inner_CachePool(
                                          stand.asLong(),
                                          stand.getY(),
                                          n.g + 16.0 + (temp ? this.tempCost + bury : 0.0),
                                          new PrintAccess.Inner_NetHelper(
                                             PrintAccess.Inner_StreamUtil.BRIDGE, n.key, stand.asLong(), stand.getY(), cell.asLong(), own, d
                                          ),
                                          n,
                                          n.temps + (temp ? 1 : 0)
                                       ),
                                       goals,
                                       open,
                                       best
                                    );
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (canConstruct && tempState != null && n.temps < this.write_I && this.view.supportAreaAtCell(x, z, h) >= 0.3) {
               for (Direction d : Plane.HORIZONTAL) {
                  BlockPos cell = new BlockPos(x, PrintView.floorInt(h - 0.001) - 1, z).relative(d);
                  BlockPos stand = cell.above();
                  int i = this.core.cellIndexByPos.get(cell.asLong());
                  if (bounds.contains(stand.getX(), stand.getY(), stand.getZ())
                     && this.core.pop_Z_I(i)
                     && !this.core.abandonedCells.contains(cell.asLong())
                     && this.view.getBlockState(cell).isAir()
                     && this.view.isPassableCell(stand)
                     && this.view.isPassableCell(stand.above())
                     && this.init_Z_BlockPos(cell)
                     && !this.init_Z_BlockPos_Z(cell, true)
                     && this.init_Z_BlockPos_BlockState(cell, tempState)) {
                     double off = 0.75;
                     Vec3 lean = new Vec3(x + 0.5 + d.getStepX() * off, h, z + 0.5 + d.getStepZ() * off);
                     PrintTarget probe = new PrintTarget(-1, cell, tempState, PrintTarget.Inner_DataUtil.PLACE, tempState.getBlock().asItem(), null);
                     if (PrintClicks.findClick(
                           this.view,
                           this.core.env,
                           this.core.rules,
                           probe,
                           probe.item(),
                           PrintClicks.Inner_CachePool.PLACE,
                           lean,
                           true,
                           false,
                           new PrintClicks.Inner_DataUtil(1000),
                           4.1000000000000005
                        )
                        != null) {
                        this.init_V_StreamUtil_CachePo(
                           new PrintAccess.Inner_CachePool(
                              stand.asLong(),
                              stand.getY(),
                              n.g + 16.0 + 1.5 + this.tempCost,
                              new PrintAccess.Inner_NetHelper(PrintAccess.Inner_StreamUtil.BRIDGE, n.key, stand.asLong(), stand.getY(), cell.asLong(), false, d),
                              n,
                              n.temps + 1
                           ),
                           goals,
                           open,
                           best
                        );
                     }
                  }
               }
            }

            long under = BlockPos.asLong(x, y - 1, z);
            if (h == y
               && (this.core.temporaryBlocks.containsKey(under) || this.view.isOwnedTemporaryAt(BlockPos.of(under)))
               && (this.poll_Z || !this.core.isFailedDig(under, n.key))
               && !this.core.sync_Z_J(under)
               && (!this.pop || this.core.init_Z_BlockPos(BlockPos.of(under)))
               && this.core.floorBelow(BlockPos.of(under)) != null) {
               this.view.pushHypothesis(BlockPos.of(under), Blocks.AIR.defaultBlockState());

               try {
                  for (int ny = y - 1; ny >= y - 3; ny--) {
                     double landing = this.view.standingHeightAt(x, ny, z);
                     if (!Double.isNaN(landing)) {
                        if (!(h - landing > 3.000001)) {
                           long to = BlockPos.asLong(x, ny, z);
                           this.init_V_StreamUtil_CachePo(
                              new PrintAccess.Inner_CachePool(
                                 to,
                                 landing,
                                 n.g + 15.0 + 1.5 * Math.max(0.0, h - landing - 1.0),
                                 new PrintAccess.Inner_NetHelper(PrintAccess.Inner_StreamUtil.DIG, n.key, to, landing, under, false, Direction.DOWN),
                                 n,
                                 n.temps
                              ),
                              goals,
                              open,
                              best
                           );
                        }
                        break;
                     }
                  }
               } finally {
                  this.view.popHypothesis(BlockPos.of(under));
               }
            }

            if (this.mayDigBuild) {
               this.init(n, x, y, z, h, bounds, goals, open, best);
            }

            return;
         }
      } finally {
         this.popHypotheses(assumed);
      }
   }

   private void init(
      PrintAccess.Inner_CachePool n,
      int x,
      int y,
      int z,
      double h,
      PrintNav.Inner_DataUtil bounds,
      IntArrayList goals,
      PriorityQueue<Object[]> open,
      Long2DoubleOpenHashMap best
   ) {
      BlockState air = Blocks.AIR.defaultBlockState();

      for (Direction d : Plane.HORIZONTAL) {
         BlockPos f = new BlockPos(x + d.getStepX(), y, z + d.getStepZ());
         BlockPos head = f.above();
         if (bounds.contains(f.getX(), f.getY(), f.getZ())) {
            boolean fs = !this.view.getCollisionShape(f).isEmpty() || !this.view.getOutlineShape(f).isEmpty();
            boolean hs = !this.view.getCollisionShape(head).isEmpty() || !this.view.getOutlineShape(head).isEmpty();
            if ((fs || hs) && (!fs || this.core.open_Z_BlockPos(f)) && (!hs || this.core.open_Z_BlockPos(head))) {
               this.view.pushHypothesis(f, air);
               this.view.pushHypothesis(head, air);

               try {
                  double h2 = this.view.standingHeightAt(f.getX(), f.getY(), f.getZ());
                  if (!Double.isNaN(h2) && !(Math.abs(h2 - h) > 0.6)) {
                     double step = PrintNav.stepCost(this.view, x, z, h, f.getX(), f.getZ(), h2, false);
                     if (!Double.isNaN(step)) {
                        this.init_V_StreamUtil_CachePo(
                           new PrintAccess.Inner_CachePool(
                              f.asLong(),
                              h2,
                              n.g + step + 200.0 * ((fs ? 1 : 0) + (hs ? 1 : 0)),
                              new PrintAccess.Inner_NetHelper(PrintAccess.Inner_StreamUtil.TUNNEL, n.key, f.asLong(), h2, f.asLong(), false, d),
                              n,
                              n.temps
                           ),
                           goals,
                           open,
                           best
                        );
                     }
                  }
               } finally {
                  this.view.popHypothesis(head);
                  this.view.popHypothesis(f);
               }
            }
         }
      }

      BlockPos under = new BlockPos(x, y - 1, z);
      if (h == y
         && !this.core.temporaryBlocks.containsKey(under.asLong())
         && !this.view.isOwnedTemporaryAt(under)
         && this.core.open_Z_BlockPos(under)
         && (this.poll_Z || !this.core.isFailedDig(under.asLong(), n.key))) {
         this.view.pushHypothesis(under, air);

         try {
            for (int yy = y - 1; yy >= y - 1 - 3; yy--) {
               double h2 = this.view.standingHeightAt(x, yy, z);
               if (!Double.isNaN(h2)) {
                  if (!(h - h2 > 3.000001)) {
                     this.init_V_StreamUtil_CachePo(
                        new PrintAccess.Inner_CachePool(
                           BlockPos.asLong(x, yy, z),
                           h2,
                           n.g + 200.0 + 1.5 * (h - h2),
                           new PrintAccess.Inner_NetHelper(
                              PrintAccess.Inner_StreamUtil.BREAK, n.key, BlockPos.asLong(x, yy, z), h2, under.asLong(), false, Direction.DOWN
                           ),
                           n,
                           n.temps
                        ),
                        goals,
                        open,
                        best
                     );
                  }
                  break;
               }

               if (!this.view.isPassableCell(new BlockPos(x, yy, z))) {
                  break;
               }
            }
         } finally {
            this.view.popHypothesis(under);
         }
      }
   }

   private boolean init_Z_I(int x, int y, int z) {
      BlockPos head = new BlockPos(x, y + 1, z);
      int i = this.core.cellIndexByPos.get(head.asLong());
      if (i >= 0
         && this.core.isOpenTargetCell(head)
         && this.core.targets[i].kind() == PrintTarget.Inner_DataUtil.PLACE
         && !this.core.targets[i].wanted().getCollisionShape(this.view, head).isEmpty()) {
         int top = this.core.maxTargetY();

         for (int yy = y + 2; yy <= top; yy++) {
            int k = this.core.cellIndexByPos.get(BlockPos.asLong(x, yy, z));
            if (k >= 0
               && yy >= this.core.baseY + 8
               && this.core.write_Z_I(k)
               && this.core.isOpenTargetCell(BlockPos.of(BlockPos.asLong(x, yy, z)))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private IntSet init_IntSet_StreamUtil_CachePo(PrintAccess.Inner_CachePool node) {
      IntOpenHashSet completed = new IntOpenHashSet();

      for (PrintAccess.Inner_CachePool at = node; at != null && at.via != null; at = at.parent) {
         if (at.via.own) {
            int i = this.core.cellIndexByPos.get(at.via.block);
            if (i >= 0) {
               completed.add(i);
            }
         }
      }

      return completed;
   }

   private boolean load(PrintAccess.Inner_CachePool n, BlockPos cell) {
      if (!this.flush_Z && !this.core.confirmedAirCells.contains(cell.asLong())) {
         boolean floorAbove = false;

         for (int k = 1; k <= 10; k++) {
            BlockPos above = cell.above(k);
            int i = this.core.cellIndexByPos.get(above.asLong());
            if (i >= 0
               && this.core.isOpenTargetCell(above)
               && this.core.targets[i].kind() == PrintTarget.Inner_DataUtil.PLACE
               && !this.core.targets[i].wanted().getCollisionShape(this.view, above).isEmpty()) {
               floorAbove = k >= 4;
               break;
            }

            if (!this.view.getBlockState(above).isAir()) {
               break;
            }
         }

         if (!floorAbove) {
            return false;
         }

         for (int d = 1; d <= 3; d++) {
            BlockPos q = cell.below(d);
            if (!this.view.getCollisionShape(q).isEmpty() || init_Z_StreamUtil_CachePo_BlockPos(n, cell) && read(n, q)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static int lowestGoalY(PrintCore core, IntArrayList goals) {
      int y = 2147483647;

      for (int g : goals) {
         PrintTarget t = core.targets[g];
         if (t.kind() == PrintTarget.Inner_DataUtil.PLACE && !t.wanted().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty()) {
            y = Math.min(y, t.pos().getY());
         }
      }

      return y == 2147483647 ? -2147483648 : y;
   }

   private boolean init_Z_BlockPos_Z(BlockPos cell, boolean bridge) {
      if (!this.flush_Z && !this.core.confirmedAirCells.contains(cell.asLong())) {
         if (this.lowestGoalY == -2147483648) {
            int top = this.core.columnTop(cell);
            if (top == -2147483648) {
               return false;
            }

            for (int y = cell.getY() + 1; y <= top; y++) {
               BlockPos above = new BlockPos(cell.getX(), y, cell.getZ());
               if (y == top) {
                  return true;
               }

               if (!this.view.getBlockState(above).isAir()) {
                  return false;
               }
            }

            return false;
         } else {
            if (cell.getY() > this.lowestGoalY - 1) {
               return false;
            }

            for (int y = cell.getY() + 1; y <= this.lowestGoalY; y++) {
               BlockPos above = new BlockPos(cell.getX(), y, cell.getZ());
               if (y == this.lowestGoalY) {
                  int i = this.core.cellIndexByPos.get(above.asLong());
                  return i >= 0 && this.core.isOpenTargetCell(above) && this.core.write_Z_I(i);
               }

               if (!this.view.getBlockState(above).isAir()) {
                  return false;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean read(PrintAccess.Inner_CachePool n, BlockPos cell) {
      int k = 0;

      for (PrintAccess.Inner_CachePool at = n; at != null && at.via != null && k < 64; k++) {
         if ((at.via.kind == PrintAccess.Inner_StreamUtil.PILLAR || at.via.kind == PrintAccess.Inner_StreamUtil.BRIDGE)
            && !at.via.own
            && at.via.block == cell.asLong()) {
            return true;
         }

         at = at.parent;
      }

      return false;
   }

   boolean init_Z_BlockPos(BlockPos cell) {
      if (!this.flush_Z && !this.core.confirmedAirCells.contains(cell.asLong())) {
         for (int k = 1; k <= 3; k++) {
            if (this.core.isOpenTargetCell(cell.above(k))) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   boolean init_Z_I_Direction(int i, Direction d) {
      if (!this.core.push_Z_I(i)) {
         return false;
      }

      BlockState w = this.core.targets[i].wanted();
      if (w.hasProperty(BlockStateProperties.AXIS) && w.getValue(BlockStateProperties.AXIS) != d.getAxis()) {
         return false;
      }

      for (Property<?> property : w.getProperties()) {
         if (!w.getValue(property).equals(w.getBlock().defaultBlockState().getValue(property))) {
            return false;
         }
      }

      return true;
   }

   private void init_V_StreamUtil_CachePo(PrintAccess.Inner_CachePool node, IntArrayList goals, PriorityQueue<Object[]> open, Long2DoubleOpenHashMap best) {
      if (!(node.g >= best.get(node.key) - 1.0E-9)
         && (
            this.flush_Z
               || node.parent == null
               || node.via == null
               || node.via.kind != PrintAccess.Inner_StreamUtil.WALK
               || !this.core.isFailedWalk(node.parent.key, node.parent.feet, node.key, node.feet)
         )
         && !this.core.failedWalkStances.contains(node.key)) {
         best.put(node.key, node.g);
         open.add(new Object[]{node.g + this.close_D * this.init_D_J_IntArrayList(node.key, goals), node});
      }
   }

   private static double square(double v) {
      return v * v;
   }

   record Inner_BufferHelper(List<PrintAccess.Inner_NetHelper> steps, double cost, int temps, int goal) {
   }

   private record Inner_CachePool(long key, double feet, double g, PrintAccess.Inner_NetHelper via, PrintAccess.Inner_CachePool parent, int temps) {
   }

   final class Inner_ConfigStore {
      private final IntArrayList goals;
      private final IntOpenHashSet goalSet;
      private final BlockState init_BlockState;
      private final double startFeet;
      private final long update;
      private final LongSupplier clock;
      private final Long2DoubleOpenHashMap bestCost = new Long2DoubleOpenHashMap();
      private final PriorityQueue<Object[]> openQueue = new PriorityQueue<>(
         (a, bx) -> {
            int priority = Double.compare((Double)a[0], (Double)bx[0]);
            return priority == 0 && PrintAccess.this.alternateGuide
               ? Double.compare(((PrintAccess.Inner_CachePool)bx[1]).g, ((PrintAccess.Inner_CachePool)a[1]).g)
               : priority;
         }
      );
      private PrintAccess.Inner_CachePool init_StreamUtil_CachePo;
      private PrintAccess.Inner_ConfigStore.Inner_StreamUtil init_StreamUtil_ConfigS_2;
      private PrintAccess.Inner_TaskQueue status = PrintAccess.Inner_TaskQueue.SEARCHING;
      private PrintAccess.Inner_BufferHelper result;
      private PrintAccess.Inner_BufferHelper bestCandidate;
      private long sync;
      private PrintCore.b init_UiHelper_b;
      private PrintCore.e init_UiHelper_e;
      private PrintCore.e load_UiHelper_e;
      private PrintAccess.Inner_ConfigStore.Inner_TaskQueue init_StreamUtil_ConfigS_3;
      private PrintAccess.Inner_ConfigStore.Inner_TaskQueue load_StreamUtil_ConfigS;
      private PrintAccess.Inner_ConfigStore.Inner_ConfigStore_2 init_StreamUtil_ConfigS;
      PrintClicks.Inner_StreamUtil click;
      long stanceKey;
      boolean init_Z;
      long expandedNodes;
      long read;
      final long[] init_JArr = new long[6];
      private final Int2BooleanOpenHashMap init_Int2BooleanOpenHas = new Int2BooleanOpenHashMap();
      long write;
      private PrintNav.Inner_TaskQueue reach;
      private LongIterator reachCells;
      private List<Long> reachPath;
      private int reachPathIndex;
      private PrintAccess.Inner_CachePool load_StreamUtil_CachePo;

      Inner_ConfigStore(BlockPos start, double feet, IntArrayList goals, BlockState tempState, LongSupplier clock) {
         PrintAccess.this.resetSearchCaches();
         this.goals = new IntArrayList(goals);
         this.goalSet = new IntOpenHashSet(goals);
         this.init_BlockState = tempState;
         this.startFeet = feet;
         this.clock = clock;
         PrintNav.Inner_DataUtil b = PrintAccess.this.core.bounds;
         PrintAccess.this.bounds = new PrintNav.Inner_DataUtil(
            Math.min(b.minX(), start.getX()),
            Math.min(b.minY(), start.getY()),
            Math.min(b.minZ(), start.getZ()),
            Math.max(b.maxX(), start.getX()),
            Math.max(b.maxY(), start.getY()),
            Math.max(b.maxZ(), start.getZ())
         );
         this.update = PrintAccess.this.view.worldMutationRevision();
         if (!goals.isEmpty()) {
            PrintAccess.this.lowestGoalY = PrintAccess.lowestGoalY(PrintAccess.this.core, goals);
         }

         PrintAccess.this.tempState = tempState;
         PrintAccess.this.get = tempState != null;
         PrintAccess.Inner_CachePool root = new PrintAccess.Inner_CachePool(start.asLong(), feet, 0.0, null, null, 0);
         PrintAccess.this.rootKey = root.key;
         PrintAccess.this.rootCanEscape = null;
         PrintAccess.this.load_Boolean = null;
         this.bestCost.defaultReturnValue(1.0 / 0.0);
         this.bestCost.put(root.key, 0.0);
         this.openQueue.add(new Object[]{PrintAccess.this.init_D_J_IntArrayList(root.key, this.goals), root});
      }

      PrintAccess.Inner_TaskQueue getStatus() {
         return this.status;
      }

      private boolean init(PrintAccess.Inner_CachePool n) {
         PrintNav.Inner_TaskQueue reach = PrintAccess.this.core.reachable;
         if (reach != null && n.temps == 0 && reach.isReachable(n.key) && !(Math.abs(reach.feetYOf(n.key) - n.feet) > 1.0E-6)) {
            for (PrintAccess.Inner_CachePool at = n; at != null && at.via != null; at = at.parent) {
               if (at.via.kind != PrintAccess.Inner_StreamUtil.WALK || at.via.block != -9223372036854775808L) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      }

      PrintAccess.Inner_BufferHelper getPlan() {
         return this.result;
      }

      int pendingNodes() {
         return this.openQueue.size() + (this.init_StreamUtil_CachePo == null ? 0 : 1);
      }

      boolean init_Z() {
         return this.init_StreamUtil_ConfigS_3 != null;
      }

      String describe() {
         StringBuilder out = new StringBuilder("expanded=")
            .append(this.expandedNodes)
            .append(" queued=")
            .append(this.pendingNodes())
            .append(" work=")
            .append(Arrays.toString(this.init_JArr))
            .append(" walkSkips=")
            .append(this.write);
         if (this.init_StreamUtil_CachePo != null) {
            out.append(" node=")
               .append(BlockPos.of(this.init_StreamUtil_CachePo.key).toShortString())
               .append(" feet=")
               .append(this.init_StreamUtil_CachePo.feet);
         }

         if (this.init_StreamUtil_ConfigS_3 != null) {
            out.append(" wayOutStage=").append(this.init_StreamUtil_ConfigS_3.init_I);
         }

         if (this.init_StreamUtil_ConfigS_2 != null) {
            out.append(" candidate=").append(this.init_StreamUtil_ConfigS_2.init_I).append('/').append(this.init_StreamUtil_ConfigS_2.init_IntArrayList.size());
            PrintAccess.Inner_ConfigStore.Inner_BufferHelper placement = this.init_StreamUtil_ConfigS_2.init_StreamUtil_ConfigS;
            if (placement == null && this.init_StreamUtil_ConfigS_2.init_StreamUtil_ConfigS_3 != null) {
               placement = this.init_StreamUtil_ConfigS_2.init_StreamUtil_ConfigS_3.init_StreamUtil_ConfigS;
            }

            if (placement != null) {
               out.append(" placing=").append(placement.click.target().pos().toShortString());
               out.append(" pose=").append(placement.click.feet());
               if (placement.init_UiHelper_aI != null) {
                  out.append(' ').append(PrintAccess.this.core.describeTrap(placement.init_UiHelper_aI));
               }

               out.append(" neighbor=").append(placement.init_UiHelper_D.update).append('/').append(placement.init_UiHelper_D.neighbors.size());
            }
         }

         return out.toString();
      }

      PrintAccess.Inner_TaskQueue init(long allowanceWork) {
         if (this.status != PrintAccess.Inner_TaskQueue.SEARCHING) {
            return this.status;
         }

         if (PrintAccess.this.view.worldMutationRevision() != this.update) {
            return this.status = PrintAccess.Inner_TaskQueue.INVALIDATED;
         }

         if (this.init_UiHelper_b != null && !this.init_UiHelper_b.init()) {
            return this.status = PrintAccess.Inner_TaskQueue.INVALIDATED;
         }

         long started = this.clock.getAsLong();
         boolean worked = false;
         int cursorSteps = 0;

         while (!worked || cursorSteps < 1024 && this.clock.getAsLong() - started < Math.max(1L, allowanceWork)) {
            cursorSteps++;
            if (this.reachCells != null) {
               this.init_V();
               worked = true;
            } else {
               if (this.init_StreamUtil_CachePo == null) {
                  if (this.bestCandidate != null
                     && (this.openQueue.isEmpty() || (Double)this.openQueue.peek()[0] >= this.bestCandidate.cost())) {
                     if (this.load_StreamUtil_ConfigS != null && !this.load_StreamUtil_ConfigS.init_Z()
                        || this.init_StreamUtil_ConfigS != null && !this.init_StreamUtil_ConfigS.init()) {
                        return this.status = PrintAccess.Inner_TaskQueue.INVALIDATED;
                     }

                     this.result = this.bestCandidate;
                     this.stanceKey = this.sync;
                     this.init_Z = true;
                     return this.status = PrintAccess.Inner_TaskQueue.FOUND;
                  }

                  while (!this.openQueue.isEmpty()) {
                     PrintAccess.Inner_CachePool next = (PrintAccess.Inner_CachePool)this.openQueue.poll()[1];
                     if (next.g <= this.bestCost.get(next.key) + 1.0E-9) {
                        this.init_StreamUtil_CachePo = next;
                        break;
                     }
                  }

                  if (this.init_StreamUtil_CachePo == null) {
                     if (this.bestCandidate == null) {
                        PrintAccess.this.failureDetail = "exhausted after " + this.expandedNodes + " nodes";
                        return this.status = PrintAccess.Inner_TaskQueue.EXHAUSTED;
                     }

                     if (this.load_StreamUtil_ConfigS != null && !this.load_StreamUtil_ConfigS.init_Z()
                        || this.init_StreamUtil_ConfigS != null && !this.init_StreamUtil_ConfigS.init()) {
                        return this.status = PrintAccess.Inner_TaskQueue.INVALIDATED;
                     }

                     this.result = this.bestCandidate;
                     this.stanceKey = this.sync;
                     this.init_Z = true;
                     return this.status = PrintAccess.Inner_TaskQueue.FOUND;
                  }

                  this.init_StreamUtil_ConfigS_2 = new PrintAccess.Inner_ConfigStore.Inner_StreamUtil(this.init_StreamUtil_CachePo);
               }

               if (this.init_StreamUtil_ConfigS_3 == null) {
                  long w0 = PrintAccess.this.view.workUnits();
                  if (PrintAccess.this.init_LongArrayList != null) {
                     this.init_StreamUtil_ConfigS_2.init(Math.max(1L, allowanceWork - (this.clock.getAsLong() - started)));
                     this.read++;
                  } else {
                     LongArrayList assumed = PrintAccess.this.init_LongArrayList_StreamUtil_CachePo(this.init_StreamUtil_CachePo);
                     this.init_StreamUtil_ConfigS_2.init_LongArrayList = assumed;

                     try {
                        this.init_StreamUtil_ConfigS_2.init(Math.max(1L, allowanceWork - (this.clock.getAsLong() - started)));
                        this.read++;
                     } finally {
                        PrintAccess.this.popHypotheses(assumed);
                     }
                  }

                  this.init_JArr[this.init_StreamUtil_ConfigS_2.read_I] = this.init_JArr[this.init_StreamUtil_ConfigS_2.read_I]
                     + (PrintAccess.this.view.workUnits() - w0);
                  PrintAccess.this.core.accessPhaseWork[this.init_StreamUtil_ConfigS_2.read_I] = PrintAccess.this.core.accessPhaseWork[this.init_StreamUtil_ConfigS_2
                        .read_I]
                     + (PrintAccess.this.view.workUnits() - w0);
               }

               worked = true;
               if (this.status != PrintAccess.Inner_TaskQueue.SEARCHING) {
                  return this.status;
               }

               boolean safe = PrintAccess.this.flush_Z;
               if (this.init_StreamUtil_ConfigS_2.load_I >= 0 && !PrintAccess.this.flush_Z) {
                  if (!this.init_StreamUtil_ConfigS_2.init_Z()) {
                     return this.status = PrintAccess.Inner_TaskQueue.INVALIDATED;
                  }

                  if (this.init_StreamUtil_ConfigS_3 == null) {
                     this.init_StreamUtil_ConfigS_3 = new PrintAccess.Inner_ConfigStore.Inner_TaskQueue(this.init_StreamUtil_CachePo);
                  }

                  if (this.init_UiHelper_b == null && (this.init_StreamUtil_ConfigS_3.init_Z || this.init_StreamUtil_ConfigS_3.load_Z)) {
                     this.init_UiHelper_b = PrintAccess.this.core.new b();
                  }

                  long w1 = PrintAccess.this.view.workUnits();
                  PrintAccess.Inner_TaskQueue proof = this.init_StreamUtil_ConfigS_3
                     .init(Math.max(1L, allowanceWork - (this.clock.getAsLong() - started)));
                  this.init_JArr[4] = this.init_JArr[4] + (PrintAccess.this.view.workUnits() - w1);
                  PrintAccess.this.core.accessPhaseWork[4] = PrintAccess.this.core.accessPhaseWork[4] + (PrintAccess.this.view.workUnits() - w1);
                  if (proof == PrintAccess.Inner_TaskQueue.SEARCHING) {
                     return this.status;
                  }

                  if (proof == PrintAccess.Inner_TaskQueue.INVALIDATED) {
                     return this.status = proof;
                  }

                  safe = proof == PrintAccess.Inner_TaskQueue.FOUND;
               }

               if (this.init_StreamUtil_ConfigS_2.load_I >= 0 && safe) {
                  PrintAccess.Inner_BufferHelper candidate = PrintAccess.this.init_StreamUtil_BufferH_StreamUtil_CachePo_I(
                     this.init_StreamUtil_CachePo,
                     PrintAccess.this.init_LongArrayList == null && PrintAccess.this.load_LongArrayList == null && !PrintAccess.this.flush_Z
                        ? this.init_StreamUtil_ConfigS_2.load_I
                        : -1
                  );
                  if (!this.init_StreamUtil_ConfigS_2.load_Z) {
                     this.result = candidate;
                     this.click = this.init_StreamUtil_ConfigS_2.init_ConfigStore_Stream;
                     this.stanceKey = this.init_StreamUtil_CachePo.key;
                     return this.status = PrintAccess.Inner_TaskQueue.FOUND;
                  }

                  candidate = new PrintAccess.Inner_BufferHelper(
                     candidate.steps(), candidate.cost() + 16.0 + PrintAccess.this.tempCost, candidate.temps() + 1, candidate.goal()
                  );
                  if (this.bestCandidate == null || candidate.cost() < this.bestCandidate.cost()) {
                     this.bestCandidate = candidate;
                     this.sync = this.init_StreamUtil_CachePo.key;
                     this.load_StreamUtil_ConfigS = this.init_StreamUtil_ConfigS_3;
                     this.init_StreamUtil_ConfigS = this.init_StreamUtil_ConfigS_2.init_StreamUtil_ConfigS();
                  }

                  this.init_StreamUtil_ConfigS_2.load_I = -1;
                  this.init_StreamUtil_ConfigS_2.load_Z = false;
                  this.init_StreamUtil_ConfigS_2.init_UiHelper_b = null;
                  this.init_StreamUtil_ConfigS_2.init_StreamUtil_ConfigS = null;
                  this.init_StreamUtil_ConfigS_2.init_ConfigStore_Stream = null;
                  this.init_StreamUtil_ConfigS_3 = null;
               }

               if (this.init_StreamUtil_ConfigS_2.load_I >= 0 || this.init_StreamUtil_ConfigS_2.init_Z) {
                  long w2 = PrintAccess.this.view.workUnits();
                  PrintAccess.this.init_V_StreamUtil_CachePo(
                     this.init_StreamUtil_CachePo, this.goals, this.init_BlockState, this.openQueue, this.bestCost
                  );
                  this.init_JArr[0] = this.init_JArr[0] + (PrintAccess.this.view.workUnits() - w2);
                  PrintAccess.this.core.accessPhaseWork[0] = PrintAccess.this.core.accessPhaseWork[0] + (PrintAccess.this.view.workUnits() - w2);
                  this.expandedNodes++;
                  this.init_StreamUtil_CachePo = null;
                  this.init_StreamUtil_ConfigS_2 = null;
                  this.init_StreamUtil_ConfigS_3 = null;
               }
            }
         }

         return this.status;
      }

      private void init_V() {
         if (this.reachPath == null) {
            if (!this.reachCells.hasNext()) {
               this.reachCells = null;
               this.reach = null;
               return;
            }

            long key = this.reachCells.nextLong();
            if (this.reach.pillarCountByCell.get(key) != 0 || PrintAccess.this.core.failedWalkStances.contains(key)) {
               return;
            }

            double height = this.reach.feetYOf(key);
            int x = BlockPos.getX(key);
            int z = BlockPos.getZ(key);
            boolean relevant = false;
            double radius = 5.8500000000000005;
            IntListIterator cost = this.goals.iterator();

            while (true) {
               if (cost.hasNext()) {
                  int goal = cost.next();
                  BlockPos target = PrintAccess.this.core.targets[goal].pos();
                  if (!(Math.hypot(target.getX() - x, target.getZ() - z) <= radius) || !(target.getY() + radius >= height + 1.62)) {
                     continue;
                  }

                  relevant = true;
               }

               if (!relevant || Double.isNaN(height)) {
                  return;
               }

               this.reachPath = this.reach.reconstructPath(key);
               this.reachPathIndex = 0;
               this.load_StreamUtil_CachePo = new PrintAccess.Inner_CachePool(PrintAccess.this.rootKey, this.startFeet, 0.0, null, null, 0);
               break;
            }
         }

         if (this.reachPathIndex == this.reachPath.size()) {
            PrintAccess.this.init_V_StreamUtil_CachePo(
               this.load_StreamUtil_CachePo, this.goals, this.openQueue, this.bestCost
            );
            this.reachPath = null;
         } else {
            long to = this.reachPath.get(this.reachPathIndex++);
            int x = BlockPos.getX(this.load_StreamUtil_CachePo.key);
            int z = BlockPos.getZ(this.load_StreamUtil_CachePo.key);
            int nx = BlockPos.getX(to);
            int ny = BlockPos.getY(to);
            int nz = BlockPos.getZ(to);
            double height = PrintAccess.this.view.standingHeightAt(nx, ny, nz);
            if (Math.abs(nx - x) <= 1
               && Math.abs(nz - z) <= 1
               && (nx != x || nz != z)
               && PrintAccess.this.bounds.contains(nx, ny, nz)
               && !Double.isNaN(height)
               && !(Math.abs(height - this.reach.feetYOf(to)) > 1.0E-6)
               && !PrintAccess.this.core.isFailedWalk(this.load_StreamUtil_CachePo.key, this.load_StreamUtil_CachePo.feet, to, height)
               && !PrintAccess.this.core.failedWalkStances.contains(to)) {
               double cost = PrintNav.stepCost(
                  PrintAccess.this.view, x, z, this.load_StreamUtil_CachePo.feet, nx, nz, height, nx != x && nz != z
               );
               if (Double.isNaN(cost)) {
                  this.reachPath = null;
               } else {
                  this.load_StreamUtil_CachePo = new PrintAccess.Inner_CachePool(
                     to,
                     height,
                     this.load_StreamUtil_CachePo.g + cost,
                     new PrintAccess.Inner_NetHelper(
                        PrintAccess.Inner_StreamUtil.WALK, this.load_StreamUtil_CachePo.key, to, height, -9223372036854775808L, false, null
                     ),
                     this.load_StreamUtil_CachePo,
                     0
                  );
               }
            } else {
               this.reachPath = null;
            }
         }
      }

      private final class Inner_BufferHelper {
         final PrintClicks.Inner_StreamUtil click;
         final BlockPos support;
         final BlockState init_BlockState;
         final PrintCore.D init_UiHelper_D;
         final PrintCore.aI init_UiHelper_aI;
         boolean init_Z;
         boolean load;
         boolean read;
         boolean write;

         Inner_BufferHelper(PrintClicks.Inner_StreamUtil click, BlockPos support) {
            this(click, support, null);
         }

         Inner_BufferHelper(PrintClicks.Inner_StreamUtil click, BlockPos support, PrintCore.f starts) {
            this.click = click;
            this.support = support;
            this.init_BlockState = support == null ? null : PrintAccess.this.tempState();
            if (support != null) {
               PrintAccess.this.view.pushTemporaryHypothesis(support, this.init_BlockState);
            }

            try {
               BlockState proposed = click.predicted() == null ? click.target().wanted() : click.predicted();
               this.init_UiHelper_aI = PrintAccess.this.core.env.canUseFlight()
                  ? null
                  : PrintAccess.this.core.new aI(
                     click.feet(), PrintAccess.this.core.reachable, click.target().pos(), proposed, starts
                  );
               this.init_UiHelper_D = PrintAccess.this.core.new D(click.target().pos(), proposed);
            } finally {
               if (support != null) {
                  PrintAccess.this.view.popHypothesis(support);
               }
            }
         }

         boolean init() {
            if (this.read) {
               return false;
            }

            if (this.support != null) {
               PrintAccess.this.view.pushTemporaryHypothesis(this.support, this.init_BlockState);
            }

            try {
               return (this.init_UiHelper_aI == null || this.init_UiHelper_aI.init()) && this.init_UiHelper_D.init();
            } finally {
               if (this.support != null) {
                  PrintAccess.this.view.popHypothesis(this.support);
               }
            }
         }

         void init(long allowance) {
            if (!this.init()) {
               this.read = this.init_Z = true;
            } else if (!this.init_Z) {
               long started = PrintAccess.this.view.workUnits();
               if (this.support != null) {
                  PrintAccess.this.view.pushTemporaryHypothesis(this.support, this.init_BlockState);
               }

               try {
                  if (this.init_UiHelper_aI != null) {
                     if (!this.init_UiHelper_aI.complete) {
                        this.init_UiHelper_aI.init(allowance);
                     }

                     if (this.init_UiHelper_aI.invalidated) {
                        this.read = this.init_Z = true;
                        return;
                     }

                     if (!this.init_UiHelper_aI.complete) {
                        return;
                     }

                     if (this.init_UiHelper_aI.blocked) {
                        this.write = this.load = this.init_Z = true;
                        return;
                     }
                  }

                  this.init_UiHelper_D.init(Math.max(1L, allowance - (PrintAccess.this.view.workUnits() - started)));
                  if (!this.init_UiHelper_D.invalidated) {
                     if (this.init_UiHelper_D.complete) {
                        this.load = this.init_UiHelper_D.blocked;
                        this.init_Z = true;
                        return;
                     }
                  } else {
                     this.read = this.init_Z = true;
                  }

                  return;
               } finally {
                  if (this.support != null) {
                     PrintAccess.this.view.popHypothesis(this.support);
                  }
               }
            }
         }
      }

      private final class Inner_CachePool {
         final PrintAccess.Inner_ConfigStore.Inner_DataUtil init_StreamUtil_ConfigS;
         final long init_J;
         final BlockPos init_BlockPos;
         final BlockState init_BlockState;
         final PrintCore.b init_UiHelper_b = PrintAccess.this.core.new b();
         PrintCore.e init_UiHelper_e;
         int init_I;
         PrintAccess.Inner_TaskQueue init_StreamUtil_TaskQue = PrintAccess.Inner_TaskQueue.SEARCHING;

         Inner_CachePool(PrintAccess.Inner_ConfigStore.Inner_DataUtil kind, long stance, BlockPos changed, BlockState placed) {
            this.init_StreamUtil_ConfigS = kind;
            this.init_J = stance;
            this.init_BlockPos = changed;
            this.init_BlockState = placed;
            this.init_I = kind == PrintAccess.Inner_ConfigStore.Inner_DataUtil.GENERAL ? 2 : (kind == PrintAccess.Inner_ConfigStore.Inner_DataUtil.DESCENT_AFTER ? 1 : 0);
         }

         PrintAccess.Inner_TaskQueue init() {
            if (!this.init_UiHelper_b.init()) {
               return this.init_StreamUtil_TaskQue = PrintAccess.Inner_TaskQueue.INVALIDATED;
            }

            if (this.init_StreamUtil_TaskQue != PrintAccess.Inner_TaskQueue.SEARCHING) {
               return this.init_StreamUtil_TaskQue;
            }

            if (this.init_UiHelper_e == null) {
               this.init_UiHelper_e = PrintAccess.this.core.new e(
                  this.init_UiHelper_b,
                  this.init_J,
                  this.init_I < 2,
                  (this.init_I & 1) == 1 ? this.init_BlockPos : null,
                  (this.init_I & 1) == 1 ? this.init_BlockState : null
               );
            }

            PrintAccess.Inner_TaskQueue result = this.init_UiHelper_e.init(1L);
            if (result != PrintAccess.Inner_TaskQueue.SEARCHING && result != PrintAccess.Inner_TaskQueue.INVALIDATED) {
               if ((this.init_I & 1) == 1) {
                  return this.init_StreamUtil_TaskQue = result;
               }

               if (this.init_I == 2 && result == PrintAccess.Inner_TaskQueue.EXHAUSTED) {
                  return this.init_StreamUtil_TaskQue = PrintAccess.Inner_TaskQueue.FOUND;
               }

               this.init_I = this.init_I == 0 ? (result == PrintAccess.Inner_TaskQueue.FOUND ? 1 : 2) : 3;
               this.init_UiHelper_e = null;
               return this.init_StreamUtil_TaskQue;
            } else {
               return this.init_StreamUtil_TaskQue = result;
            }
         }
      }

      private final class Inner_ConfigStore_2 {
         final PrintAccess.Inner_CachePool init_StreamUtil_CachePo;
         final boolean init_Z;
         final PrintAccess.Inner_ConfigStore.Inner_CachePool init_StreamUtil_ConfigS_2;
         final PrintCore.b init_UiHelper_b;
         final PrintAccess.Inner_ConfigStore.Inner_BufferHelper init_StreamUtil_ConfigS;
         final PrintClicks.Inner_StreamUtil init_ConfigStore_Stream;

         Inner_ConfigStore_2(
            PrintAccess.Inner_CachePool node,
            boolean withoutPath,
            PrintAccess.Inner_ConfigStore.Inner_CachePool escape,
            PrintCore.b support,
            PrintAccess.Inner_ConfigStore.Inner_BufferHelper placement,
            PrintClicks.Inner_StreamUtil dig
         ) {
            this.init_StreamUtil_CachePo = node;
            this.init_Z = withoutPath;
            this.init_StreamUtil_ConfigS_2 = escape;
            this.init_UiHelper_b = support;
            this.init_StreamUtil_ConfigS = placement;
            this.init_ConfigStore_Stream = dig;
         }

         boolean init() {
            LongArrayList path = this.init_Z ? null : PrintAccess.this.init_LongArrayList_StreamUtil_CachePo(this.init_StreamUtil_CachePo);

            try {
               if (this.init_StreamUtil_ConfigS_2 != null && this.init_StreamUtil_ConfigS_2.init() != PrintAccess.Inner_TaskQueue.FOUND) {
                  return false;
               } else if (this.init_UiHelper_b != null && !this.init_UiHelper_b.init()) {
                  return false;
               } else {
                  return this.init_StreamUtil_ConfigS == null
                        || this.init_StreamUtil_ConfigS.init() && this.init_StreamUtil_ConfigS.init_Z && !this.init_StreamUtil_ConfigS.load
                     ? this.init_ConfigStore_Stream == null
                        || !PrintAccess.this.core
                           .failedClicks
                           .contains(
                              new PrintCore.a(
                                 this.init_ConfigStore_Stream.target().pos().asLong(),
                                 this.init_StreamUtil_CachePo.key,
                                 this.init_ConfigStore_Stream.feet(),
                                 this.init_ConfigStore_Stream.crouch()
                              )
                           )
                     : false;
               }
            } finally {
               if (path != null) {
                  PrintAccess.this.popHypotheses(path);
               }
            }
         }
      }

      private enum Inner_DataUtil {
         REMOVAL,
         GENERAL,
         DESCENT_AFTER;
      }

      private final class Inner_StreamUtil {
         final PrintAccess.Inner_CachePool init_StreamUtil_CachePo;
         final IntArrayList init_IntArrayList = new IntArrayList();
         int init_I;
         int load_I = -1;
         boolean init_Z;
         boolean load_Z;
         PrintClicks.Inner_StreamUtil init_ConfigStore_Stream;
         List<Vec3> init_List;
         List<Vec3> load_List;
         List<Vec3> read_List;
         final PrintClicks.Inner_DataUtil init_ConfigStore_DataUt = new PrintClicks.Inner_DataUtil();
         Iterator<Vec3> init_Iterator;
         LongArrayList init_LongArrayList;
         long init_J;
         boolean read_Z;
         int read_I = 1;
         PrintAccess.Inner_ConfigStore.Inner_BufferHelper init_StreamUtil_ConfigS;
         PrintCore.f init_UiHelper_f;
         PrintAccess.Inner_ConfigStore.Inner_CachePool init_StreamUtil_ConfigS_2;
         PrintAccess.Inner_ConfigStore.Inner_StreamUtil.Inner_DataUtil init_StreamUtil_ConfigS_4;
         PrintAccess.Inner_ConfigStore.Inner_StreamUtil.Inner_StreamUtil_2 init_StreamUtil_ConfigS_5;
         PrintAccess.Inner_ConfigStore.Inner_StreamUtil.Inner_CachePool init_StreamUtil_ConfigS_3;
         PrintAccess.Inner_ConfigStore.Inner_CachePool load_StreamUtil_ConfigS;
         PrintCore.b init_UiHelper_b;
         boolean write;
         final boolean update;

         private PrintAccess.Inner_ConfigStore.Inner_BufferHelper init(PrintClicks.Inner_StreamUtil click) {
            if (this.init_UiHelper_f == null) {
               this.init_UiHelper_f = PrintAccess.this.core.new f();
            }

            return Inner_ConfigStore.this.new Inner_BufferHelper(click, null, this.init_UiHelper_f);
         }

         boolean init_Z() {
            return this.init_StreamUtil_ConfigS().init();
         }

         PrintAccess.Inner_ConfigStore.Inner_ConfigStore_2 init_StreamUtil_ConfigS() {
            return Inner_ConfigStore.this.new Inner_ConfigStore_2(
               this.init_StreamUtil_CachePo,
               this.write,
               this.load_StreamUtil_ConfigS,
               this.init_UiHelper_b,
               this.init_StreamUtil_ConfigS,
               this.init_StreamUtil_ConfigS_4 == null ? null : this.init_ConfigStore_Stream
            );
         }

         Inner_StreamUtil(PrintAccess.Inner_CachePool node) {
            this.init_StreamUtil_CachePo = node;
            this.update = Inner_ConfigStore.this.init(node);
            if (PrintAccess.this.init_LongArrayList == null && PrintAccess.this.load_LongArrayList == null && !PrintAccess.this.flush_Z) {
               if (node.via != null && node.via.own) {
                  int i = PrintAccess.this.core.cellIndexByPos.get(node.via.block);
                  if (Inner_ConfigStore.this.goalSet.contains(i)) {
                     this.load_I = i;
                     return;
                  }
               }

               double ex = BlockPos.getX(node.key) + 0.5;
               double ey = node.feet + 1.62;
               double ez = BlockPos.getZ(node.key) + 0.5;
               int radius = (int)Math.ceil(5.3500000000000005);
               int side = radius * 2 + 1;
               if (Inner_ConfigStore.this.goals.size() < side * side * side) {
                  for (int g : Inner_ConfigStore.this.goals) {
                     this.init(g, ex, ey, ez);
                  }
               } else {
                  for (int dx = -radius; dx <= radius; dx++) {
                     for (int dz = -radius; dz <= radius; dz++) {
                        for (int dy = -radius; dy <= radius; dy++) {
                           int g = PrintAccess.this.core
                              .cellIndexByPos
                              .get(BlockPos.asLong(BlockPos.getX(node.key) + dx, BlockPos.getY(node.key) + dy, BlockPos.getZ(node.key) + dz));
                           if (g >= 0 && Inner_ConfigStore.this.goalSet.contains(g)) {
                              this.init(g, ex, ey, ez);
                           }
                        }
                     }
                  }
               }

               this.init_IntArrayList.sort((a, b) -> PrintAccess.this.init_I_I(a, b, ex, ey, ez));
            }
         }

         private void init(int g, double x, double y, double z) {
            if (this.update && Inner_ConfigStore.this.init_Int2BooleanOpenHas.computeIfAbsent(g, PrintAccess.this.core::close_Z_I)) {
               Inner_ConfigStore.this.write++;
            } else if (!PrintAccess.this.core.init_Z_I(g, this.init_StreamUtil_CachePo.feet, this.init_StreamUtil_CachePo.key)
               && PrintAccess.this.core.targets[g].pos().distToCenterSqr(x, y, z) <= PrintAccess.square(5.8500000000000005)) {
               this.init_IntArrayList.add(g);
            }
         }

         void init(long allowance) {
            this.read_I = PrintAccess.this.init_LongArrayList == null && PrintAccess.this.load_LongArrayList == null && this.init_StreamUtil_ConfigS_2 == null
               ? (this.init_StreamUtil_ConfigS != null ? 2 : (this.init_StreamUtil_ConfigS_3 != null ? 3 : 1))
               : 5;
            if (this.load_I < 0 && !this.init_Z) {
               if (PrintAccess.this.flush_Z) {
                  int x = BlockPos.getX(this.init_StreamUtil_CachePo.key);
                  int y = BlockPos.getY(this.init_StreamUtil_CachePo.key);
                  int z = BlockPos.getZ(this.init_StreamUtil_CachePo.key);
                  if (PrintAccess.this.push_Z
                     ? PrintAccess.this.read(this.init_StreamUtil_CachePo)
                     : PrintAccess.this.core.isWithinFootprint(x, z) && PrintAccess.this.read(this.init_StreamUtil_CachePo)
                        || y > PrintAccess.this.core.maxTargetY()
                        || PrintAccess.this.init_Z_StreamUtil_CachePo(this.init_StreamUtil_CachePo)) {
                     this.load_I = 0;
                  }

                  this.init_Z = true;
               } else if (PrintAccess.this.init_LongArrayList != null) {
                  if (this.init_StreamUtil_CachePo.parent == null && !PrintAccess.this.freshClickRetry) {
                     this.init_Z = true;
                  } else {
                     if (this.init_StreamUtil_ConfigS_4 == null) {
                        this.init_StreamUtil_ConfigS_4 = new PrintAccess.Inner_ConfigStore.Inner_StreamUtil.Inner_DataUtil();
                     }

                     this.init_StreamUtil_ConfigS_4.init();
                  }
               } else if (PrintAccess.this.load_LongArrayList != null) {
                  if (this.init_StreamUtil_ConfigS_5 == null) {
                     this.init_StreamUtil_ConfigS_5 = new PrintAccess.Inner_ConfigStore.Inner_StreamUtil.Inner_StreamUtil_2();
                  }

                  this.init_StreamUtil_ConfigS_5.init(allowance);
               } else if (this.init_I >= this.init_IntArrayList.size()) {
                  this.init_Z = true;
               } else {
                  int g = this.init_IntArrayList.getInt(this.init_I);
                  PrintTarget target = PrintAccess.this.core.targets[g];
                  if (this.init_StreamUtil_ConfigS_2 != null) {
                     PrintAccess.Inner_TaskQueue result = this.init_StreamUtil_ConfigS_2.init();
                     if (result == PrintAccess.Inner_TaskQueue.INVALIDATED) {
                        Inner_ConfigStore.this.status = result;
                     } else if (result != PrintAccess.Inner_TaskQueue.SEARCHING) {
                        if (result == PrintAccess.Inner_TaskQueue.FOUND) {
                           this.load_StreamUtil_ConfigS = this.init_StreamUtil_ConfigS_2;
                           this.load_I = g;
                        } else {
                           this.init_StreamUtil_ConfigS_2 = null;
                           this.init_ConfigStore_Stream = null;
                           this.init_I++;
                           this.init_Iterator = null;
                           this.init_List = null;
                        }
                     }
                  } else if (this.init_StreamUtil_ConfigS != null) {
                     this.init_StreamUtil_ConfigS.init(allowance);
                     if (this.init_StreamUtil_ConfigS.read) {
                        Inner_ConfigStore.this.status = PrintAccess.Inner_TaskQueue.INVALIDATED;
                     } else if (this.init_StreamUtil_ConfigS.init_Z) {
                        if (!this.init_StreamUtil_ConfigS.load) {
                           this.load_I = g;
                        } else {
                           boolean anotherPose = this.init_StreamUtil_ConfigS.write;
                           this.read_Z = true;
                           this.init_StreamUtil_ConfigS = null;
                           this.init_ConfigStore_Stream = null;
                           if (!anotherPose) {
                              this.init_I++;
                              this.init_Iterator = null;
                              this.init_List = null;
                           }
                        }
                     }
                  } else {
                     boolean pour = target.kind() == PrintTarget.Inner_DataUtil.FLUID;
                     boolean waterlog = !pour
                        && PrintAccess.this.core.cellStates[g] == 2
                        && PrintAccess.this.core
                           .rules
                           .isWaterlogMissing(target, PrintAccess.this.view.getBlockState(target.pos()));
                     if (this.init_Iterator == null) {
                        this.init_J = PrintAccess.this.init_J_LongArrayList_BlockPos(this.init_LongArrayList, target.pos());
                        if (this.init_StreamUtil_CachePo.parent != null && !PrintAccess.this.get && !pour && !waterlog) {
                           this.init_J ^= 4423348608673777095L;
                        }

                        if (PrintAccess.this.core.machineJobAt(g) == null
                           && PrintAccess.this.core
                              .init_Z_J(
                                 this.init_StreamUtil_CachePo.key,
                                 this.init_StreamUtil_CachePo.feet,
                                 g,
                                 this.init_StreamUtil_CachePo.parent != null,
                                 this.init_J
                              )) {
                           this.init_I++;
                           return;
                        }

                        this.read_Z = false;
                        if (PrintClicks.isOrientationSensitive(target, PrintAccess.this.core.constraintFor(g))) {
                           if (this.load_List == null) {
                              this.load_List = PrintClicks.getCandidateFeetPositions(
                                 PrintAccess.this.view, this.init_StreamUtil_CachePo.key, this.init_StreamUtil_CachePo.feet, true
                              );
                           }

                           this.init_List = this.load_List;
                        } else {
                           if (this.read_List == null) {
                              this.read_List = PrintClicks.getCandidateFeetPositions(
                                 PrintAccess.this.view, this.init_StreamUtil_CachePo.key, this.init_StreamUtil_CachePo.feet, false
                              );
                           }

                           this.init_List = this.read_List;
                        }

                        PrintMachineJob job = PrintAccess.this.core.machineJobAt(g);
                        boolean directPlacement = !pour
                           && !waterlog
                           && job == null
                           && (
                              PrintAccess.this.core.cellStates[g] != 2
                                 || PrintAccess.this.core
                                       .rules
                                       .nextInteraction(target, PrintAccess.this.view.getBlockState(target.pos()))
                                    == null
                           );
                        if (directPlacement
                           && !PrintClicks.hasPlaceableSupport(
                              PrintAccess.this.view,
                              target,
                              PrintAccess.this.view.hasNoHypothesisInNeighborhood(target.pos()) ? null : PrintAccess.this.core.constraintFor(g),
                              null
                           )) {
                           long pair = PrintCore.cellStanceKey(g, this.init_StreamUtil_CachePo.key);
                           this.read_Z = PrintAccess.this.core.refusedCellStances.contains(pair)
                              || PrintAccess.this.core.open_LongOpenHashSet.contains(pair);
                           this.init_Iterator = Collections.emptyIterator();
                        } else {
                           this.init_Iterator = this.init_List.iterator();
                        }
                     }

                     if (this.init_Iterator.hasNext()) {
                        Vec3 position = this.init_Iterator.next();
                        if (!pour && !waterlog) {
                           boolean exact = PrintAccess.this.view.hasNoHypothesisInNeighborhood(target.pos());
                           PrintMachineJob job = PrintAccess.this.core.machineJobAt(g);
                           PrintRules.Inner_CachePool interaction = job != null
                              ? new PrintRules.Inner_CachePool(job.item(), job.description())
                              : (
                                 PrintAccess.this.core.cellStates[g] == 2
                                    ? PrintAccess.this.core
                                       .rules
                                       .nextInteraction(target, PrintAccess.this.view.getBlockState(target.pos()))
                                    : null
                              );
                           if (interaction != null) {
                              if (!PrintAccess.this.core.refusedCellStances.contains(PrintCore.cellStanceKey(g, this.init_StreamUtil_CachePo.key))) {
                                 this.init_ConfigStore_Stream = PrintAccess.this.core
                                    .findInteractionClick(target, interaction, position, this.init_ConfigStore_DataUt);
                              } else {
                                 this.read_Z = true;
                              }

                              if (this.init_ConfigStore_Stream != null) {
                                 this.load_I = g;
                              }
                           } else {
                              if (!PrintAccess.this.core.refusedCellStances.contains(PrintCore.cellStanceKey(g, this.init_StreamUtil_CachePo.key))) {
                                 this.init_ConfigStore_Stream = PrintClicks.findClick(
                                    PrintAccess.this.view,
                                    PrintAccess.this.core.env,
                                    PrintAccess.this.core.rules,
                                    target,
                                    target.item(),
                                    PrintClicks.Inner_CachePool.PLACE,
                                    position,
                                    true,
                                    exact,
                                    this.init_ConfigStore_DataUt,
                                    4.1000000000000005,
                                    PrintAccess.this.core.constraintFor(g)
                                 );
                              } else {
                                 this.read_Z = true;
                              }

                              if (PrintAccess.this.core.open_LongOpenHashSet.contains(PrintCore.cellStanceKey(g, this.init_StreamUtil_CachePo.key))) {
                                 this.read_Z = true;
                              }

                              if (this.init_ConfigStore_Stream == null
                                 && !PrintAccess.this.core.open_LongOpenHashSet.contains(PrintCore.cellStanceKey(g, this.init_StreamUtil_CachePo.key))) {
                                 this.init_ConfigStore_Stream = PrintClicks.findClick(
                                    PrintAccess.this.view,
                                    PrintAccess.this.core.env,
                                    PrintAccess.this.core.rules,
                                    target,
                                    target.item(),
                                    PrintClicks.Inner_CachePool.PLACE,
                                    position,
                                    false,
                                    exact,
                                    this.init_ConfigStore_DataUt,
                                    4.1000000000000005,
                                    PrintAccess.this.core.constraintFor(g)
                                 );
                              }

                              if (this.init_ConfigStore_Stream != null) {
                                 this.init_StreamUtil_ConfigS = this.init(this.init_ConfigStore_Stream);
                              }
                           }
                        } else if (!PrintAccess.this.core.refusedCellStances.contains(PrintCore.cellStanceKey(g, this.init_StreamUtil_CachePo.key))) {
                           this.init_ConfigStore_Stream = PrintClicks.findClick(
                              PrintAccess.this.view,
                              PrintAccess.this.core.env,
                              PrintAccess.this.core.rules,
                              target,
                              PrintClicks.getFluidBucketItem(target),
                              waterlog ? PrintClicks.Inner_CachePool.WATERLOG : PrintClicks.Inner_CachePool.POUR,
                              position,
                              !waterlog,
                              false,
                              this.init_ConfigStore_DataUt,
                              4.1000000000000005
                           );
                           if (this.init_ConfigStore_Stream != null
                              && !PrintAccess.this.core.push_Z_BlockPos(BlockPos.of(this.init_StreamUtil_CachePo.key))) {
                              if (target.kind() != PrintTarget.Inner_DataUtil.FLUID) {
                                 this.load_I = g;
                              } else {
                                 this.init_StreamUtil_ConfigS_2 = Inner_ConfigStore.this.new Inner_CachePool(
                                    PrintAccess.Inner_ConfigStore.Inner_DataUtil.GENERAL,
                                    this.init_StreamUtil_CachePo.key,
                                    target.pos(),
                                    target.wanted().getFluidState().createLegacyBlock()
                                 );
                              }
                           }
                        } else {
                           this.read_Z = true;
                        }
                     } else {
                        if (!pour
                           && !waterlog
                           && PrintAccess.this.core.machineJobAt(g) == null
                           && this.init_StreamUtil_CachePo.parent != null
                           && PrintAccess.this.get) {
                           if (this.init_StreamUtil_ConfigS_3 == null) {
                              this.init_StreamUtil_ConfigS_3 = new PrintAccess.Inner_ConfigStore.Inner_StreamUtil.Inner_CachePool(target, this.init_List);
                           }

                           this.init_StreamUtil_ConfigS_3.init(allowance);
                           if (Inner_ConfigStore.this.status != PrintAccess.Inner_TaskQueue.SEARCHING || !this.init_StreamUtil_ConfigS_3.init_Z) {
                              return;
                           }

                           if (this.init_StreamUtil_ConfigS_3.load_Z) {
                              this.init_UiHelper_b = this.init_StreamUtil_ConfigS_3.init_UiHelper_b;
                              this.init_StreamUtil_ConfigS = this.init_StreamUtil_ConfigS_3.init_StreamUtil_ConfigS;
                              this.init_ConfigStore_Stream = this.init_StreamUtil_ConfigS.click;
                              this.load_I = g;
                              this.load_Z = true;
                           } else if (!this.read_Z) {
                              PrintAccess.this.core.init_V_J(this.init_StreamUtil_CachePo.key, this.init_StreamUtil_CachePo.feet, g, true, this.init_J);
                           }

                           this.init_StreamUtil_ConfigS_3 = null;
                        } else if (!this.read_Z && PrintAccess.this.core.machineJobAt(g) == null) {
                           PrintAccess.this.core
                              .init_V_J(
                                 this.init_StreamUtil_CachePo.key,
                                 this.init_StreamUtil_CachePo.feet,
                                 g,
                                 this.init_StreamUtil_CachePo.parent != null,
                                 this.init_J
                              );
                        }

                        this.init_I++;
                        this.init_Iterator = null;
                        this.init_List = null;
                     }
                  }
               }
            }
         }

         private final class Inner_CachePool {
            final PrintTarget init_ListPool;
            final List<Vec3> init_List;
            final PrintCore.b init_UiHelper_b = PrintAccess.this.core.new b();
            final BlockState init_BlockState = PrintAccess.this.tempState();
            final Item init_Item = this.init_BlockState == null ? null : this.init_BlockState.getBlock().asItem();
            int init_I;
            int load_I;
            BlockPos init_BlockPos;
            PrintAccess.Inner_ConfigStore.Inner_BufferHelper init_StreamUtil_ConfigS;
            PrintCore.f init_UiHelper_f;
            final IdentityHashMap<BlockState, PrintCore.D> init_IdentityHashMap = new IdentityHashMap<>();
            boolean init_Z;
            boolean load_Z;

            Inner_CachePool(PrintTarget target, List<Vec3> points) {
               this.init_ListPool = target;
               this.init_List = points;
            }

            void init(long allowance) {
               if (!this.init_UiHelper_b.init()) {
                  Inner_ConfigStore.this.status = PrintAccess.Inner_TaskQueue.INVALIDATED;
               } else {
                  if (this.init_StreamUtil_ConfigS != null) {
                     this.init_StreamUtil_ConfigS.init(allowance);
                     if (this.init_StreamUtil_ConfigS.read) {
                        Inner_ConfigStore.this.status = PrintAccess.Inner_TaskQueue.INVALIDATED;
                        return;
                     }

                     if (!this.init_StreamUtil_ConfigS.init_Z) {
                        return;
                     }

                     if (!this.init_StreamUtil_ConfigS.load) {
                        this.load_Z = this.init_Z = true;
                        return;
                     }

                     Inner_StreamUtil.this.read_Z = true;
                     if (!this.init_StreamUtil_ConfigS.write
                        && this.init_StreamUtil_ConfigS.init_UiHelper_D.complete
                        && this.init_StreamUtil_ConfigS.init_UiHelper_D.blocked) {
                        if (this.init_IdentityHashMap.size() >= 16) {
                           this.init_IdentityHashMap.clear();
                        }

                        this.init_IdentityHashMap
                           .put(this.init_StreamUtil_ConfigS.init_UiHelper_D.state, this.init_StreamUtil_ConfigS.init_UiHelper_D);
                     }

                     this.init_StreamUtil_ConfigS = null;
                  }

                  try (PrintView.Inner_StreamUtil.Inner_StreamUtil_2 geometry = this.init_UiHelper_b.geometry.attach()) {
                     if (this.init_BlockPos == null) {
                        if (this.init_I == Direction.values().length) {
                           this.init_Z = true;
                           return;
                        }

                        BlockPos next = this.init_ListPool.pos().relative(Direction.values()[this.init_I++]);
                        if (!PrintAccess.this.core.init_Z_BlockPos_BlockPos(next, this.init_ListPool.pos())) {
                           return;
                        }

                        this.init_BlockPos = next;
                        this.load_I = 0;
                        this.init_UiHelper_f = null;
                        this.init_IdentityHashMap.clear();
                        return;
                     }

                     if (this.load_I == this.init_List.size()) {
                        this.init_BlockPos = null;
                        return;
                     }

                     Vec3 feet = this.init_List.get(this.load_I++);
                     PrintTarget probe = new PrintTarget(-1, this.init_BlockPos, this.init_BlockState, PrintTarget.Inner_DataUtil.PLACE, this.init_Item, null);
                     if (PrintClicks.findClick(
                           PrintAccess.this.view,
                           PrintAccess.this.core.env,
                           PrintAccess.this.core.rules,
                           probe,
                           this.init_Item,
                           PrintClicks.Inner_CachePool.PLACE,
                           feet,
                           true,
                           false,
                           Inner_StreamUtil.this.init_ConfigStore_DataUt,
                           4.1000000000000005
                        )
                        == null) {
                        return;
                     }

                     PrintAccess.this.view.pushTemporaryHypothesis(this.init_BlockPos, this.init_BlockState);

                     PrintClicks.Inner_StreamUtil sourceClick;
                     try {
                        sourceClick = PrintClicks.findPlacementClick(
                           PrintAccess.this.view,
                           PrintAccess.this.core.env,
                           PrintAccess.this.core.rules,
                           this.init_ListPool,
                           feet,
                           false,
                           Inner_StreamUtil.this.init_ConfigStore_DataUt,
                           4.1000000000000005,
                           PrintAccess.this.core.constraintFor(this.init_ListPool.index()),
                           this.init_BlockPos
                        );
                        if (sourceClick != null) {
                           BlockState predicted = sourceClick.predicted() == null ? this.init_ListPool.wanted() : sourceClick.predicted();
                           PrintCore.D prior = this.init_IdentityHashMap.get(predicted);
                           if (prior != null) {
                              if (!prior.init()) {
                                 Inner_ConfigStore.this.status = PrintAccess.Inner_TaskQueue.INVALIDATED;
                              }

                              return;
                           }

                           if (this.init_UiHelper_f == null) {
                              this.init_UiHelper_f = PrintAccess.this.core.new f();
                           }
                        }
                     } finally {
                        PrintAccess.this.view.popHypothesis(this.init_BlockPos);
                     }

                     if (sourceClick != null) {
                        this.init_StreamUtil_ConfigS = Inner_ConfigStore.this.new Inner_BufferHelper(sourceClick, this.init_BlockPos, this.init_UiHelper_f);
                     }
                  }

                  if (!this.init_UiHelper_b.geometry.verifyStillValid()) {
                     Inner_ConfigStore.this.status = PrintAccess.Inner_TaskQueue.INVALIDATED;
                  }
               }
            }
         }

         private final class Inner_DataUtil {
            int init_I;
            int load_I;
            boolean init_Z;
            boolean load_Z;
            long init_J = -9223372036854775808L;
            PrintClicks.Inner_StreamUtil init_ConfigStore_Stream;
            PrintAccess.Inner_ConfigStore.Inner_CachePool init_StreamUtil_ConfigS;

            void init() {
               if (!this.init_Z) {
                  this.init_Z = true;
                  int x = BlockPos.getX(Inner_StreamUtil.this.init_StreamUtil_CachePo.key);
                  int y = BlockPos.getY(Inner_StreamUtil.this.init_StreamUtil_CachePo.key);
                  int z = BlockPos.getZ(Inner_StreamUtil.this.init_StreamUtil_CachePo.key);
                  long floor = BlockPos.asLong(x, y - 1, z);
                  if ((PrintAccess.this.tick || PrintAccess.this.run)
                     && (Inner_StreamUtil.this.init_StreamUtil_CachePo.temps <= 0 || floor != PrintAccess.this.avoidSwap)
                     && Inner_StreamUtil.this.init_StreamUtil_CachePo.feet == y
                     && PrintAccess.this.load_LongOpenHashSet.contains(floor)
                     && !PrintAccess.this.core.temporaryBlocks.containsKey(BlockPos.asLong(x, y - 2, z))
                     && Inner_StreamUtil.this.init_StreamUtil_CachePo.via != null
                     && Inner_StreamUtil.this.init_StreamUtil_CachePo.via.kind == PrintAccess.Inner_StreamUtil.WALK
                     && !PrintAccess.this.core.sync_Z_J(floor)
                     && PrintAccess.this.core.update_Z_BlockPos(BlockPos.of(floor))) {
                     this.init_J = floor;
                     this.load_Z = true;
                     this.init_StreamUtil_ConfigS = Inner_ConfigStore.this.new Inner_CachePool(
                        PrintAccess.Inner_ConfigStore.Inner_DataUtil.REMOVAL, floor, BlockPos.of(floor), Blocks.AIR.defaultBlockState()
                     );
                     return;
                  }

                  if (PrintAccess.this.tick) {
                     Inner_StreamUtil.this.init_Z = true;
                     return;
                  }
               }

               if (this.load_Z) {
                  PrintAccess.Inner_TaskQueue result = this.init_StreamUtil_ConfigS.init();
                  if (result != PrintAccess.Inner_TaskQueue.SEARCHING) {
                     if (result == PrintAccess.Inner_TaskQueue.INVALIDATED) {
                        Inner_ConfigStore.this.status = result;
                     } else if (result == PrintAccess.Inner_TaskQueue.FOUND) {
                        this.init(this.init_StreamUtil_ConfigS, true);
                     } else {
                        this.load_Z = false;
                        this.init_StreamUtil_ConfigS = null;
                        this.init_J = -9223372036854775808L;
                        if (PrintAccess.this.tick) {
                           Inner_StreamUtil.this.init_Z = true;
                        }
                     }
                  }
               } else {
                  LongArrayList path = PrintAccess.this.init_LongArrayList_StreamUtil_CachePo(Inner_StreamUtil.this.init_StreamUtil_CachePo);

                  try {
                     this.load();
                  } finally {
                     PrintAccess.this.popHypotheses(path);
                  }
               }
            }

            private void load() {
               if (this.init_StreamUtil_ConfigS != null) {
                  PrintAccess.Inner_TaskQueue result = this.init_StreamUtil_ConfigS.init();
                  if (result == PrintAccess.Inner_TaskQueue.SEARCHING) {
                     return;
                  }

                  if (result == PrintAccess.Inner_TaskQueue.INVALIDATED) {
                     Inner_ConfigStore.this.status = result;
                     return;
                  }

                  PrintAccess.Inner_ConfigStore.Inner_CachePool completed = this.init_StreamUtil_ConfigS;
                  this.init_StreamUtil_ConfigS = null;
                  if (result == PrintAccess.Inner_TaskQueue.EXHAUSTED) {
                     this.init_J = -9223372036854775808L;
                     this.init_ConfigStore_Stream = null;
                     return;
                  }

                  if (this.load_I == 3) {
                     this.init(completed, false);
                     return;
                  }

                  this.load_I = 2;
               }

               if (this.init_J == -9223372036854775808L) {
                  if (this.init_I == PrintAccess.this.init_LongArrayList.size()) {
                     Inner_StreamUtil.this.init_Z = true;
                     return;
                  }

                  long candidate = PrintAccess.this.init_LongArrayList.getLong(this.init_I++);
                  int x = BlockPos.getX(Inner_StreamUtil.this.init_StreamUtil_CachePo.key);
                  int y = BlockPos.getY(Inner_StreamUtil.this.init_StreamUtil_CachePo.key);
                  int z = BlockPos.getZ(Inner_StreamUtil.this.init_StreamUtil_CachePo.key);
                  int tx = BlockPos.getX(candidate);
                  int ty = BlockPos.getY(candidate);
                  int tz = BlockPos.getZ(candidate);
                  if (Math.abs(tx - x) > 4
                     || Math.abs(tz - z) > 4
                     || ty - y > 4
                     || y - ty > 5
                     || PrintAccess.this.core.sync_Z_J(candidate)
                     || tx == x && tz == z && ty == y - 1
                     || PrintAccess.this.core
                        .failedDigStances
                        .contains(PrintCore.cellStanceKey((int)(candidate & 2147483647L), Inner_StreamUtil.this.init_StreamUtil_CachePo.key))) {
                     return;
                  }

                  BlockPos pos = BlockPos.of(candidate);
                  if (PrintAccess.this.view.getBlockState(pos).isAir()) {
                     return;
                  }

                  this.init_J = candidate;
                  if (Inner_StreamUtil.this.init_StreamUtil_CachePo.temps > 0 && candidate == PrintAccess.this.avoidSwap) {
                     this.load_I = 1;
                     this.init_StreamUtil_ConfigS = Inner_ConfigStore.this.new Inner_CachePool(
                        PrintAccess.Inner_ConfigStore.Inner_DataUtil.DESCENT_AFTER,
                        Inner_StreamUtil.this.init_StreamUtil_CachePo.key,
                        pos,
                        Blocks.AIR.defaultBlockState()
                     );
                     return;
                  }

                  this.load_I = 2;
               }

               BlockPos pos = BlockPos.of(this.init_J);
               BlockState targetState = PrintAccess.this.view.getBlockState(pos);
               PrintTarget probe = new PrintTarget(-1, pos, targetState, PrintTarget.Inner_DataUtil.PLACE, Items.AIR, null);
               this.init_ConfigStore_Stream = PrintAccess.this.core
                  .findClickFrom(
                     Inner_StreamUtil.this.init_StreamUtil_CachePo.key,
                     Inner_StreamUtil.this.init_StreamUtil_CachePo.feet,
                     probe,
                     Inner_StreamUtil.this.init_ConfigStore_DataUt,
                     Inner_StreamUtil.this.init_StreamUtil_CachePo.key == PrintAccess.this.rootKey
                  );
               if (this.init_ConfigStore_Stream == null) {
                  this.init_J = -9223372036854775808L;
               } else {
                  this.load_I = 3;
                  this.init_StreamUtil_ConfigS = Inner_ConfigStore.this.new Inner_CachePool(
                     PrintAccess.Inner_ConfigStore.Inner_DataUtil.REMOVAL, Inner_StreamUtil.this.init_StreamUtil_CachePo.key, pos, Blocks.AIR.defaultBlockState()
                  );
               }
            }

            private void init(PrintAccess.Inner_ConfigStore.Inner_CachePool proof, boolean withoutPath) {
               Inner_StreamUtil.this.load_StreamUtil_ConfigS = proof;
               Inner_StreamUtil.this.write = withoutPath;
               PrintAccess.this.foundGoalKey = this.init_J;
               PrintAccess.this.load_ConfigStore_Stream = this.init_ConfigStore_Stream;
               Inner_StreamUtil.this.init_ConfigStore_Stream = this.init_ConfigStore_Stream;
               Inner_StreamUtil.this.load_I = 0;
            }
         }

         private final class Inner_StreamUtil_2 {
            int init_I;
            int load;
            long init_J;
            PrintTarget init_ListPool;
            List<Vec3> init_List;

            void init(long allowance) {
               if (Inner_StreamUtil.this.init_StreamUtil_ConfigS != null) {
                  Inner_StreamUtil.this.init_StreamUtil_ConfigS.init(allowance);
                  if (Inner_StreamUtil.this.init_StreamUtil_ConfigS.read) {
                     Inner_ConfigStore.this.status = PrintAccess.Inner_TaskQueue.INVALIDATED;
                  } else if (Inner_StreamUtil.this.init_StreamUtil_ConfigS.init_Z) {
                     if (!Inner_StreamUtil.this.init_StreamUtil_ConfigS.load) {
                        PrintAccess.this.foundGoalKey = this.init_J;
                        PrintAccess.this.init_ConfigStore_Stream = Inner_StreamUtil.this.init_ConfigStore_Stream;
                        Inner_StreamUtil.this.load_I = 0;
                     } else {
                        boolean anotherPose = Inner_StreamUtil.this.init_StreamUtil_ConfigS.write;
                        Inner_StreamUtil.this.init_StreamUtil_ConfigS = null;
                        Inner_StreamUtil.this.init_ConfigStore_Stream = null;
                        if (!anotherPose) {
                           this.init_I++;
                           this.load = 0;
                           this.init_ListPool = null;
                        }
                     }
                  }
               } else if (this.init_I != PrintAccess.this.load_LongArrayList.size() && !Inner_StreamUtil.this.init_ConfigStore_DataUt.isExhausted()) {
                  if (this.init_List == null) {
                     this.init_List = PrintClicks.getCandidateFeetPositions(
                        PrintAccess.this.view, Inner_StreamUtil.this.init_StreamUtil_CachePo.key, Inner_StreamUtil.this.init_StreamUtil_CachePo.feet
                     );
                  }

                  if (this.init_ListPool == null) {
                     this.init_J = PrintAccess.this.load_LongArrayList.getLong(this.init_I);
                     int x = BlockPos.getX(Inner_StreamUtil.this.init_StreamUtil_CachePo.key);
                     int y = BlockPos.getY(Inner_StreamUtil.this.init_StreamUtil_CachePo.key);
                     int z = BlockPos.getZ(Inner_StreamUtil.this.init_StreamUtil_CachePo.key);
                     int tx = BlockPos.getX(this.init_J);
                     int ty = BlockPos.getY(this.init_J);
                     int tz = BlockPos.getZ(this.init_J);
                     if (Math.abs(tx - x) > 4
                        || Math.abs(tz - z) > 4
                        || ty - y > 4
                        || y - ty > 5
                        || tx == x && tz == z && (ty == y || ty == y + 1)
                        || !PrintAccess.this.view.getBlockState(BlockPos.of(this.init_J)).canBeReplaced()) {
                        this.init_I++;
                        return;
                     }

                     this.init_ListPool = new PrintTarget(
                        -1, BlockPos.of(this.init_J), PrintAccess.this.init_BlockState, PrintTarget.Inner_DataUtil.PLACE, PrintAccess.this.init_Item, null
                     );
                  }

                  if (this.load == this.init_List.size()) {
                     this.init_I++;
                     this.load = 0;
                     this.init_ListPool = null;
                  } else {
                     Inner_StreamUtil.this.init_ConfigStore_Stream = PrintClicks.findClick(
                        PrintAccess.this.view,
                        PrintAccess.this.core.env,
                        PrintAccess.this.core.rules,
                        this.init_ListPool,
                        PrintAccess.this.init_Item,
                        PrintClicks.Inner_CachePool.PLACE,
                        this.init_List.get(this.load++),
                        true,
                        false,
                        Inner_StreamUtil.this.init_ConfigStore_DataUt,
                        4.1000000000000005
                     );
                     if (Inner_StreamUtil.this.init_ConfigStore_Stream != null) {
                        Inner_StreamUtil.this.init_StreamUtil_ConfigS = Inner_StreamUtil.this.init(Inner_StreamUtil.this.init_ConfigStore_Stream);
                     }
                  }
               } else {
                  Inner_StreamUtil.this.init_Z = true;
               }
            }
         }
      }

      private final class Inner_TaskQueue {
         final PrintAccess.Inner_CachePool init_StreamUtil_CachePo;
         final boolean init_Z;
         final boolean load_Z;
         PrintCore.b init_UiHelper_b;
         PrintCore.e init_UiHelper_e;
         PrintCore.e load_UiHelper_e;
         PrintAccess.Inner_CachePool load_StreamUtil_CachePo;
         Boolean init_Boolean;
         int init_I;

         Inner_TaskQueue(PrintAccess.Inner_CachePool node) {
            this.init_StreamUtil_CachePo = node;
            this.init_Z = PrintAccess.this.init_LongArrayList != null
               || PrintAccess.this.core.phase == PrintCore.J.TEARDOWN
               || PrintAccess.this.core.isAboveBase(BlockPos.of(node.key))
               || PrintAccess.this.core.push_Z() && PrintAccess.this.core.isAboveBase(BlockPos.of(PrintAccess.this.rootKey));
            boolean lowered = node.feet < Inner_ConfigStore.this.startFeet - 0.5;

            for (PrintAccess.Inner_CachePool at = node; !lowered && at != null && at.parent != null; at = at.parent) {
               if (at.via != null && at.via.kind == PrintAccess.Inner_StreamUtil.WALK && at.feet < at.parent.feet - 0.6 - 1.0E-6) {
                  lowered = true;
               }
            }

            this.load_Z = lowered;
            this.load_StreamUtil_CachePo = node;
         }

         boolean init_Z() {
            if (Inner_ConfigStore.this.init_UiHelper_b != null && !Inner_ConfigStore.this.init_UiHelper_b.init()) {
               return false;
            }

            if (this.init_UiHelper_b == null) {
               return true;
            }

            LongArrayList assumed = PrintAccess.this.init_LongArrayList_StreamUtil_CachePo(this.init_StreamUtil_CachePo);

            try {
               return this.init_UiHelper_b.init();
            } finally {
               PrintAccess.this.popHypotheses(assumed);
            }
         }

         PrintAccess.Inner_TaskQueue init(long allowance) {
            if (!this.init_Z()) {
               return PrintAccess.Inner_TaskQueue.INVALIDATED;
            }

            long started = PrintAccess.this.view.workUnits();

            do {
               long left = Math.max(1L, allowance - (PrintAccess.this.view.workUnits() - started));
               if (this.init_I == 0) {
                  if (!this.init_Z) {
                     this.init_I = 3;
                  } else {
                     if (Inner_ConfigStore.this.init_UiHelper_e == null) {
                        Inner_ConfigStore.this.init_UiHelper_e = PrintAccess.this.core.new e(
                           Inner_ConfigStore.this.init_UiHelper_b, PrintAccess.this.rootKey, true
                        );
                     }

                     PrintAccess.Inner_TaskQueue result = Inner_ConfigStore.this.init_UiHelper_e.init(left);
                     if (result == PrintAccess.Inner_TaskQueue.SEARCHING || result == PrintAccess.Inner_TaskQueue.INVALIDATED) {
                        return result;
                     }

                     this.init_I = result == PrintAccess.Inner_TaskQueue.FOUND ? 1 : 3;
                  }
               } else if (this.init_I != 1 && this.init_I != 4) {
                  if (this.init_I != 2 && this.init_I != 5) {
                     if (!this.load_Z) {
                        return PrintAccess.Inner_TaskQueue.FOUND;
                     }

                     if (Inner_ConfigStore.this.load_UiHelper_e == null) {
                        Inner_ConfigStore.this.load_UiHelper_e = PrintAccess.this.core.new e(
                           Inner_ConfigStore.this.init_UiHelper_b, PrintAccess.this.rootKey, false
                        );
                     }

                     PrintAccess.Inner_TaskQueue result = Inner_ConfigStore.this.load_UiHelper_e.init(left);
                     if (result == PrintAccess.Inner_TaskQueue.SEARCHING || result == PrintAccess.Inner_TaskQueue.INVALIDATED) {
                        return result;
                     }

                     if (result == PrintAccess.Inner_TaskQueue.EXHAUSTED) {
                        return PrintAccess.Inner_TaskQueue.FOUND;
                     }

                     this.init_I = 4;
                  } else {
                     PrintAccess.Inner_TaskQueue result = this.init(this.init_I == 2, left);
                     if (result != PrintAccess.Inner_TaskQueue.FOUND) {
                        return result;
                     }

                     if (this.init_I == 5) {
                        return PrintAccess.Inner_TaskQueue.FOUND;
                     }

                     this.init_I = 3;
                  }
               } else {
                  if (this.init_Boolean == null) {
                     this.init_V();
                  }

                  if (!Inner_ConfigStore.this.init_UiHelper_b.init()) {
                     return PrintAccess.Inner_TaskQueue.INVALIDATED;
                  }

                  if (this.init_Boolean != null) {
                     if (this.init_I == 1) {
                        this.init_I = this.init_Boolean ? 3 : 2;
                     } else {
                        if (this.init_Boolean) {
                           return PrintAccess.Inner_TaskQueue.FOUND;
                        }

                        this.init_I = 5;
                     }
                  }
               }
            } while (PrintAccess.this.view.workUnits() - started < allowance);

            return PrintAccess.Inner_TaskQueue.SEARCHING;
         }

         private PrintAccess.Inner_TaskQueue init(boolean down, long allowance) {
            LongArrayList assumed = PrintAccess.this.init_LongArrayList_StreamUtil_CachePo(this.init_StreamUtil_CachePo);

            try {
               if (this.init_UiHelper_b == null) {
                  this.init_UiHelper_b = PrintAccess.this.core.new b();
               }

               if (down) {
                  if (this.init_UiHelper_e == null) {
                     this.init_UiHelper_e = PrintAccess.this.core.new e(this.init_UiHelper_b, this.init_StreamUtil_CachePo.key, true);
                  }

                  return this.init_UiHelper_e.init(allowance);
               } else {
                  if (this.load_UiHelper_e == null) {
                     this.load_UiHelper_e = PrintAccess.this.core.new e(this.init_UiHelper_b, this.init_StreamUtil_CachePo.key, false);
                  }

                  return this.load_UiHelper_e.init(allowance);
               }
            } finally {
               PrintAccess.this.popHypotheses(assumed);
            }
         }

         private void init_V() {
            if (this.load_StreamUtil_CachePo.parent == null) {
               this.init_Boolean = true;
            } else {
               LongArrayList assumed = PrintAccess.this.init_LongArrayList_StreamUtil_CachePo(this.load_StreamUtil_CachePo);

               try (PrintView.Inner_StreamUtil.Inner_StreamUtil_2 geometry = Inner_ConfigStore.this.init_UiHelper_b
                     .geometry
                     .attach()) {
                  PrintAccess.Inner_NetHelper step = this.load_StreamUtil_CachePo.via;
                  PrintAccess.Inner_CachePool from = this.load_StreamUtil_CachePo.parent;
                  int fx = BlockPos.getX(from.key);
                  int fy = BlockPos.getY(from.key);
                  int fz = BlockPos.getZ(from.key);
                  if (step.kind != PrintAccess.Inner_StreamUtil.WALK) {
                     if (step.kind != PrintAccess.Inner_StreamUtil.PILLAR || step.own || assumed.isEmpty() || assumed.getLong(assumed.size() - 1) != step.block) {
                        this.init_Boolean = false;
                        return;
                     }

                     PrintAccess.this.view.popHypothesis(BlockPos.of(assumed.removeLong(assumed.size() - 1)));
                  } else {
                     int tx = BlockPos.getX(this.load_StreamUtil_CachePo.key);
                     int tz = BlockPos.getZ(this.load_StreamUtil_CachePo.key);
                     if (Double.isNaN(
                        PrintNav.stepCost(
                           PrintAccess.this.view, tx, tz, this.load_StreamUtil_CachePo.feet, fx, fz, from.feet, tx != fx && tz != fz
                        )
                     )) {
                        this.init_Boolean = false;
                        return;
                     }
                  }

                  double height = PrintAccess.this.view.standingHeightAt(fx, fy, fz);
                  if (!Double.isNaN(height) && !(Math.abs(height - from.feet) > 0.001)) {
                     this.load_StreamUtil_CachePo = from;
                     if (this.load_StreamUtil_CachePo.parent == null) {
                        this.init_Boolean = true;
                     }
                  } else {
                     this.init_Boolean = false;
                  }
               } finally {
                  PrintAccess.this.popHypotheses(assumed);
               }
            }
         }
      }
   }

   final class Inner_DataUtil {
      final PrintAccess.Inner_BufferHelper init_StreamUtil_BufferH;
      final BlockState init_BlockState;
      PrintAccess.Inner_CachePool init_StreamUtil_CachePo;
      int init_I;
      boolean done;
      boolean load;

      Inner_DataUtil(BlockPos start, double feet, PrintAccess.Inner_BufferHelper plan, BlockState temporary) {
         this.init_StreamUtil_BufferH = plan;
         this.init_BlockState = temporary;
         this.init_StreamUtil_CachePo = new PrintAccess.Inner_CachePool(start.asLong(), feet, 0.0, null, null, 0);
      }

      void init(long allowance) {
         long started = PrintAccess.this.view.workUnits();

         while (!this.done && PrintAccess.this.view.workUnits() - started < allowance) {
            if (this.init_I == this.init_StreamUtil_BufferH.steps().size()) {
               LongArrayList assumed = PrintAccess.this.init_LongArrayList_StreamUtil_CachePo(this.init_StreamUtil_CachePo);

               try {
                  int x = BlockPos.getX(this.init_StreamUtil_CachePo.key);
                  int y = BlockPos.getY(this.init_StreamUtil_CachePo.key);
                  int z = BlockPos.getZ(this.init_StreamUtil_CachePo.key);
                  this.load = PrintAccess.this.push_Z
                     ? PrintAccess.this.read(this.init_StreamUtil_CachePo)
                     : PrintAccess.this.core.isWithinFootprint(x, z) && PrintAccess.this.read(this.init_StreamUtil_CachePo)
                        || y > PrintAccess.this.core.maxTargetY()
                        || PrintAccess.this.init_Z_StreamUtil_CachePo(this.init_StreamUtil_CachePo);
               } finally {
                  PrintAccess.this.popHypotheses(assumed);
               }

               this.done = true;
               return;
            }

            PrintAccess.Inner_NetHelper expected = this.init_StreamUtil_BufferH.steps().get(this.init_I++);
            PriorityQueue<Object[]> next = new PriorityQueue<>((a, b) -> Double.compare((Double)a[0], (Double)b[0]));
            Long2DoubleOpenHashMap costs = new Long2DoubleOpenHashMap();
            costs.defaultReturnValue(1.0 / 0.0);
            PrintAccess.this.init_V_StreamUtil_CachePo(this.init_StreamUtil_CachePo, new IntArrayList(), this.init_BlockState, next, costs);
            PrintAccess.Inner_CachePool found = null;
            Iterator var9 = next.iterator();

            while (true) {
               if (var9.hasNext()) {
                  Object[] entry = (Object[])var9.next();
                  PrintAccess.Inner_CachePool candidate = (PrintAccess.Inner_CachePool)entry[1];
                  if (!expected.equals(candidate.via)) {
                     continue;
                  }

                  found = candidate;
               }

               if (found == null) {
                  this.done = true;
                  return;
               }

               this.init_StreamUtil_CachePo = found;
               break;
            }
         }
      }
   }

   record Inner_NetHelper(PrintAccess.Inner_StreamUtil kind, long from, long to, double toFeet, long block, boolean own, Direction dir) {
   }

   enum Inner_StreamUtil {
      WALK,
      PILLAR,
      BRIDGE,
      DIG,
      TUNNEL,
      BREAK;
   }

   enum Inner_TaskQueue {
      SEARCHING,
      FOUND,
      EXHAUSTED,
      INVALIDATED;
   }
}
