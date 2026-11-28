package autismclient.ai;

import autismclient.AutismClientAddon;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.LongPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

final class AiBuildSite {
   private static final int DELTA_BITS = 21;
   private static final int DELTA_BIAS = 1048576;
   private static final long DELTA_MASK = 2097151L;
   private static final int Z_SHIFT = 0;
   private static final int Y_SHIFT = 21;
   private static final int X_SHIFT = 42;
   private static final int sync_I = 4096;
   private static final int flush_I = 4095;
   private static final int close_I = 4;
   private static final int open = 4096;
   private static final int poll = 8192;
   private static final int push = 8;
   private static final AiBuildSite.Inner_CachePool[] KINDS = AiBuildSite.Inner_CachePool.values();
   private static final Direction[] DIRECTIONS = Direction.values();
   private static final int[] EMPTY_INTS = new int[0];
   private final AiBuilder.Blueprint blueprint;
   private final Map<String, List<String>> substitutes;
   private final LevelReader world;
   private final int cellCount;
   private final BlockPos origin;
   private final int originX;
   private final int originY;
   private final int originZ;
   private final AiBuilder.CellSpec[] specs;
   private final long[] deltas;
   private final long[] positions;
   private final byte[] kindOrdinals;
   private final Item[] items;
   private final byte[] load_BArr;
   private final int[] load_IArr;
   private final BitSet init_BitSet;
   private final BitSet load_BitSet;
   private final long[] read_JArr;
   private final int[] read_IArr;
   private final BitSet read_BitSet;
   private int frontierScanCursor;
   private final long[] sortedKeys;
   private final int[] byPosition;
   private final int[] indexByRank;
   private final int[] rankByIndex;
   private final Map<Long, int[]> load_Map;
   private final BitSet pending;
   private final BitSet placed;
   private final BitSet failed;
   private final BitSet flush_BitSet;
   private final int[] observedIndices;
   private int pendingTotal;
   private int placedTotal;
   private int failedTotal;
   private Map<Integer, String> failureReasons;
   private List<Long> init_List;
   private Set<Long> init_Set;
   private Set<Long> load_Set;
   private int[] close_IArr = EMPTY_INTS;
   private int firstPendingRank;
   private int revision;
   private int materialsRevision = -1;
   private int materialsMax = -1;
   private Map<String, Integer> write_Map = Map.of();
   private int reconcileCountdown = 8;
   private static final double init_D = 8.0;
   private static final BlockPos[] EMPTY_POSITIONS = new BlockPos[0];

   AiBuildSite(AiBuilder.Blueprint blueprint, Map<String, List<String>> substitutes) {
      this(blueprint, substitutes, null);
   }

   AiBuildSite(AiBuilder.Blueprint blueprint, Map<String, List<String>> substitutes, LevelReader world) {
      if (blueprint == null) {
         throw new IllegalArgumentException("A build site needs a blueprint");
      }

      this.blueprint = blueprint;
      this.substitutes = substitutes == null ? Map.of() : substitutes;
      this.world = world;
      List<AiBuilder.CellSpec> input = blueprint.cells();
      this.cellCount = input.size();
      this.origin = blueprint.origin();
      this.originX = this.origin.getX();
      this.originY = this.origin.getY();
      this.originZ = this.origin.getZ();
      this.specs = new AiBuilder.CellSpec[this.cellCount];
      this.deltas = new long[this.cellCount];
      this.positions = new long[this.cellCount];
      this.kindOrdinals = new byte[this.cellCount];
      this.items = new Item[this.cellCount];
      this.load_BArr = new byte[this.cellCount];
      this.load_IArr = new int[this.cellCount];
      this.pending = new BitSet(this.cellCount);
      this.placed = new BitSet(this.cellCount);
      this.failed = new BitSet(this.cellCount);
      this.flush_BitSet = new BitSet(this.cellCount);
      this.init_BitSet = new BitSet(this.cellCount);
      this.read_BitSet = new BitSet(this.cellCount);
      HashMap<String, Map<Map<String, String>, AiBuildSite.Inner_DataUtil>> classified = new HashMap<>();
      AiBuildSite.Inner_StreamUtil observedIndices = new AiBuildSite.Inner_StreamUtil();
      int classificationFailures = 0;

      for (int i = 0; i < this.cellCount; i++) {
         AiBuilder.CellSpec cell = input.get(i);
         this.specs[i] = cell;
         this.deltas[i] = packDelta(cell.dx(), cell.dy(), cell.dz());
         this.positions[i] = BlockPos.asLong(this.originX + cell.dx(), this.originY + cell.dy(), this.originZ + cell.dz());
         AiBuildSite.Inner_DataUtil entry = classified.computeIfAbsent(cell.block(), ignored -> new HashMap<>()).get(cell.properties());
         if (entry == null) {
            boolean standable = false;

            AiBuildSite.Inner_CachePool kind;
            Item item;
            byte dependencies;
            try {
               kind = AiBuildIntent.kindOf(cell);
               item = AiBuildIntent.itemFor(cell, kind);
               dependencies = offsetMask(cell, kind);
               standable = init_Z_TableIndex_StreamU(cell);
            } catch (RuntimeException unknown) {
               if (classificationFailures++ == 0) {
                  AutismClientAddon.LOG.warn("[AI] build site could not classify {}, treating it as SKIP", cell, unknown);
               }

               kind = AiBuildSite.Inner_CachePool.SKIP;
               item = Items.AIR;
               dependencies = 0;
            }

            entry = new AiBuildSite.Inner_DataUtil((byte)kind.ordinal(), item, dependencies, standable);
            classified.get(cell.block()).put(cell.properties(), entry);
         }

         this.kindOrdinals[i] = entry.kindOrdinal();
         this.items[i] = entry.item();
         this.load_BArr[i] = entry.dependencies();
         if (entry.standable()) {
            this.init_BitSet.set(i);
         }

         boolean placeable = this.kindOrdinals[i] != (byte)AiBuildSite.Inner_CachePool.DERIVED.ordinal()
            && this.kindOrdinals[i] != (byte)AiBuildSite.Inner_CachePool.SKIP.ordinal();
         if (placeable) {
            this.pending.set(i);
            this.pendingTotal++;
            if (AiBuilder.isFlowingFluidCell(cell)) {
               this.flush_BitSet.set(i);
               observedIndices.add(i);
            }
         }
      }

      this.observedIndices = observedIndices.toArray();
      int[] byPosition = new int[this.cellCount];
      int i = 0;

      while (i < this.cellCount) {
         byPosition[i] = i++;
      }

      this.mergeSort(byPosition);
      this.byPosition = byPosition;
      this.sortedKeys = new long[this.cellCount];

      for (int rank = 0; rank < this.cellCount; rank++) {
         this.sortedKeys[rank] = this.positions[byPosition[rank]];
      }

      this.load_BitSet = new BitSet(this.cellCount);

      for (int ix = 0; ix < this.cellCount; ix++) {
         if (this.init_BitSet.get(ix) && this.hasHeadroom(ix)) {
            this.load_BitSet.set(ix);
         }
      }

      i = this.load_BitSet.cardinality();
      this.read_JArr = new long[i];
      int at = 0;

      for (int rank = 0; rank < this.cellCount && at < i; rank++) {
         int index = this.byPosition[rank];
         if (this.load_BitSet.get(index)) {
            this.read_JArr[at++] = this.positions[index];
         }
      }

      this.read_V();
      this.load_Map = this.load_Map();
      int[] ordered = this.buildSweepOrder();
      this.indexByRank = this.init_IArr_IArr(ordered);
      this.rankByIndex = new int[this.cellCount];
      int rank = 0;

      while (rank < this.cellCount) {
         this.rankByIndex[this.indexByRank[rank]] = rank++;
      }

      this.read_IArr = new int[i];
      rank = 0;

      for (int rankx = 0; rankx < this.cellCount && rank < i; rankx++) {
         int index = this.indexByRank[rankx];
         if (this.load_BitSet.get(index)) {
            this.read_IArr[rank++] = index;
         }
      }

      this.advanceFirstPending();
   }

   static boolean init_Z_TableIndex_StreamU(AiBuilder.CellSpec cell) {
      if (cell == null) {
         return false;
      }

      BlockState state;
      try {
         state = AiBuilder.toBlockState(cell);
      } catch (RuntimeException unknown) {
         return false;
      }

      if (!state.getFluidState().isEmpty()) {
         return false;
      }

      if (autismclient.ai.engine.AiPathfinder.LiveGridView.isFloorHazardCompat(state)) {
         return false;
      }

      VoxelShape shape = state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
      if (shape.isEmpty()) {
         return false;
      }

      for (AABB box : shape.toAabbs()) {
         if (!(Math.abs(box.maxY - 1.0) > 0.001) && box.minX <= 0.501 && box.maxX >= 0.499 && box.minZ <= 0.501 && box.maxZ >= 0.499) {
            return true;
         }
      }

      return false;
   }

   int init_I() {
      return this.cellCount;
   }

   long posKeyAt(int index) {
      return this.positions[index];
   }

   BlockPos posAt(int index) {
      return BlockPos.of(this.positions[index]);
   }

   AiBuilder.CellSpec specAt(int index) {
      return this.specs[index];
   }

   AiBuildSite.Inner_CachePool kindAt(int index) {
      return KINDS[this.kindOrdinals[index] & 0xFF];
   }

   Item itemAt(int index) {
      return this.items[index];
   }

   int deltaYAt(int index) {
      return unpackDy(this.deltas[index]);
   }

   int rankOf(int index) {
      return this.rankByIndex[index];
   }

   BlockPos getOrigin() {
      return this.origin;
   }

   AiBuilder.Blueprint getBlueprint() {
      return this.blueprint;
   }

   Map<String, List<String>> getSubstitutes() {
      return this.substitutes;
   }

   int indexOf(BlockPos position) {
      return position == null ? -1 : this.indexOfKey(position.asLong());
   }

   int indexOfKey(long positionKey) {
      int low = 0;
      int high = this.cellCount - 1;

      while (low <= high) {
         int middle = low + high >>> 1;
         long key = this.sortedKeys[middle];
         if (key < positionKey) {
            low = middle + 1;
         } else {
            if (key <= positionKey) {
               return this.byPosition[middle];
            }

            high = middle - 1;
         }
      }

      return -1;
   }

   int indexAtRank(int rank) {
      return this.indexByRank[rank];
   }

   int write_I_I(int index) {
      return this.load_IArr[index];
   }

   AiBuilder.CellSpec unpoweredLeverSpecAt(int index) {
      return AiBuildIntent.unpoweredLever(this.specs[index]);
   }

   boolean isPoweredLeverAt(int index) {
      return AiBuildIntent.isPoweredLever(this.specs[index]);
   }

   boolean isDoubleSlabAt(int index) {
      return AiBuilder.isDoubleSlabCell(this.specs[index]);
   }

   int update_I(int index) {
      return this.chestPartnerOf(index);
   }

   boolean hasChestPartner(int index) {
      return this.chestPartnerOf(index) >= 0;
   }

   boolean write_Z_I(int index) {
      AiBuildSite.Inner_CachePool kind = this.kindAt(index);
      return kind != AiBuildSite.Inner_CachePool.DERIVED && kind != AiBuildSite.Inner_CachePool.SKIP && !this.flush_BitSet.get(index);
   }

   boolean update_Z(int index) {
      return this.flush_BitSet.get(index);
   }

   int[] getObservedIndices() {
      return this.observedIndices;
   }

   int reconcileObserved() {
      if (this.observedIndices.length == 0) {
         return 0;
      }

      LevelReader level = this.currentLevel();
      if (level == null) {
         return 0;
      }

      int reconciled = 0;

      for (int index : this.observedIndices) {
         if (!this.placed.get(index)) {
            BlockPos at = this.posAt(index);
            if (isLoaded(level, at) && this.init_Z_I_BlockState(index, level.getBlockState(at))) {
               this.markPlaced(index);
               reconciled++;
            }
         }
      }

      return reconciled;
   }

   int[] load_IArr() {
      if (this.close_IArr.length == 0) {
         AiBuildSite.Inner_StreamUtil found = new AiBuildSite.Inner_StreamUtil();

         for (int i = 0; i < this.cellCount; i++) {
            if (AiBuildIntent.isPoweredLever(this.specs[i])) {
               found.add(i);
            }
         }

         this.close_IArr = found.toArray();
      }

      return this.close_IArr;
   }

   boolean sync_Z(int index) {
      LevelReader level = this.currentLevel();
      if (level == null) {
         return this.placed.get(index);
      }

      BlockPos at = this.posAt(index);
      return isLoaded(level, at) && AiBuildIntent.init(this.specs[index], level.getBlockState(at));
   }

   int pendingCount() {
      if (this.observedIndices.length > 0 && --this.reconcileCountdown <= 0) {
         this.reconcileCountdown = 8;
         this.reconcileObserved();
      }

      return this.pendingTotal;
   }

   int placedCount() {
      return this.placedTotal;
   }

   int failedCount() {
      return this.failedTotal;
   }

   String failureReason(int index) {
      return this.failureReasons == null ? null : this.failureReasons.get(index);
   }

   private void advanceFirstPending() {
      while (this.firstPendingRank < this.cellCount && !this.pending.get(this.indexByRank[this.firstPendingRank])) {
         this.firstPendingRank++;
      }
   }

   boolean isPending(int index) {
      return this.pending.get(index);
   }

   boolean isPlaced(int index) {
      return this.placed.get(index);
   }

   boolean isFailed(int index) {
      return this.failed.get(index);
   }

   boolean contains(long posKey) {
      return this.indexOfKey(posKey) >= 0;
   }

   LongPredicate containsPredicate() {
      return this::contains;
   }

   long[] init_JArr() {
      return this.sortedKeys;
   }

   void markPlaced(int index) {
      if (index >= 0 && index < this.cellCount) {
         boolean wasPending = this.pending.get(index);
         boolean wasFailed = this.failed.get(index);
         this.pending.clear(index);
         this.failed.clear(index);
         if (wasPending) {
            this.pendingTotal--;
         }

         if (wasFailed) {
            this.failedTotal--;
         }

         if (!this.placed.get(index)) {
            this.placed.set(index);
            this.placedTotal++;
         }

         if (this.failureReasons != null) {
            this.failureReasons.remove(index);
         }

         this.revision++;
         this.write_V_I(index);
         int partner = this.close_I(index);
         if (partner >= 0 && !this.placed.get(partner)) {
            this.poll_Z_I(partner);
         }
      }
   }

   void markPending(int index) {
      if (index >= 0 && index < this.cellCount) {
         if (this.placed.get(index)) {
            this.placed.clear(index);
            this.placedTotal--;
         }

         if (this.failed.get(index)) {
            this.failed.clear(index);
            this.failedTotal--;
         }

         if (!this.pending.get(index)) {
            this.pending.set(index);
            this.pendingTotal++;
            this.firstPendingRank = Math.min(this.firstPendingRank, this.rankByIndex[index]);
         }

         this.revision++;
      }
   }

   void markFailed(int index, String reason) {
      if (index >= 0 && index < this.cellCount) {
         if (this.placed.get(index)) {
            this.placed.clear(index);
            this.placedTotal--;
         }

         if (this.pending.get(index)) {
            this.pending.clear(index);
            this.pendingTotal--;
         }

         if (!this.failed.get(index)) {
            this.failed.set(index);
            this.failedTotal++;
         }

         if (this.failureReasons == null) {
            this.failureReasons = new LinkedHashMap<>();
         }

         this.failureReasons.put(index, reason == null ? "failed" : reason);
         this.revision++;
         this.advanceFirstPending();
      }
   }

   void requeue(int index) {
      this.markPending(index);
   }

   int adoptExistingBlocks() {
      LevelReader level = this.currentLevel();
      if (level == null) {
         return 0;
      }

      int adopted = 0;

      for (int i = 0; i < this.cellCount; i++) {
         if (!this.placed.get(i)) {
            BlockPos at = this.posAt(i);
            if (isLoaded(level, at) && this.init_Z_I_BlockState(i, level.getBlockState(at))) {
               this.markPlaced(i);
               adopted++;
            }
         }
      }

      return adopted;
   }

   boolean poll_Z_I(int index) {
      LevelReader level = this.currentLevel();
      if (level == null) {
         return this.placed.get(index);
      }

      BlockPos at = this.posAt(index);
      if (isLoaded(level, at) && this.init_Z_I_BlockState(index, level.getBlockState(at))) {
         this.markPlaced(index);
         return true;
      }

      if (this.placed.get(index)) {
         this.requeue(index);
      }

      return false;
   }

   boolean isOccupied(BlockPos pos) {
      LevelReader level = this.currentLevel();
      return level != null && pos != null && isLoaded(level, pos) ? !level.getBlockState(pos).isAir() : false;
   }

   boolean push(int index) {
      return this.init_Z_I_LevelReader(index, this.currentLevel());
   }

   int[] init_IArr_Vec3(Vec3 eye, double radius, int max) {
      if (eye != null && max > 0 && this.cellCount != 0) {
         int[] near = this.indicesNear(eye, radius);
         if (near.length == 0) {
            return EMPTY_INTS;
         }

         this.mergeSort(near, near.length);
         LevelReader level = this.currentLevel();
         int[] answer = new int[Math.min(max, near.length)];
         int count = 0;

         for (int index : near) {
            if (this.init_Z_I_LevelReader(index, level)) {
               answer[count++] = index;
               if (count == max) {
                  break;
               }
            }
         }

         return count == answer.length ? answer : (count == 0 ? EMPTY_INTS : Arrays.copyOf(answer, count));
      } else {
         return EMPTY_INTS;
      }
   }

   int[] load_IArr_Vec3(Vec3 center, double radius, int max) {
      if (center != null && max > 0 && this.cellCount != 0) {
         int[] near = this.indicesNear(center, radius);
         if (near.length == 0) {
            return EMPTY_INTS;
         }

         this.mergeSort(near, near.length);
         return near.length <= max ? near : Arrays.copyOf(near, max);
      } else {
         return EMPTY_INTS;
      }
   }

   Map<String, Integer> init_Map_I(int max) {
      if (max <= 0 || this.cellCount == 0) {
         return Map.of();
      }

      if (this.materialsRevision == this.revision && this.materialsMax == max) {
         return this.write_Map;
      }

      Map<String, Integer> out = new LinkedHashMap<>();
      int limit = Math.min(this.cellCount, this.firstPendingRank + 4096);

      for (int rank = this.firstPendingRank; rank < limit && out.size() < max; rank++) {
         int index = this.indexByRank[rank];
         if (this.pending.get(index) && !this.flush_BitSet.get(index)) {
            Item item = this.items[index];
            if (item != null && item != Items.AIR) {
               int cost;
               try {
                  cost = AiBuilder.itemCountForCell(this.specs[index]);
               } catch (RuntimeException unknown) {
                  cost = 1;
               }

               if (cost > 0) {
                  String id = BuiltInRegistries.ITEM.getKey(item).toString();
                  out.merge(id, cost, Integer::sum);
               }
            }
         }
      }

      this.materialsRevision = this.revision;
      this.materialsMax = max;
      this.write_Map = Collections.unmodifiableMap(out);
      return this.write_Map;
   }

   Set<Long> init_Set() {
      if (this.init_Set == null) {
         this.init_Set = new AiBuildSite.Inner_BufferHelper();
      }

      return this.init_Set;
   }

   List<Long> init_List() {
      if (this.init_List != null) {
         return this.init_List;
      }

      LevelReader level = this.currentLevel();
      if (level == null) {
         this.init_List = List.of();
         return this.init_List;
      }

      LinkedHashSet<Long> out = new LinkedHashSet<>();
      int limit = Math.min(this.cellCount, this.firstPendingRank + 8192);

      for (int rank = this.firstPendingRank; rank < limit; rank++) {
         int index = this.indexByRank[rank];
         if (this.pending.get(index) && this.write_Z_I(index)) {
            int mask = this.load_BArr[index] & 255;
            if (mask != 0) {
               boolean fluid = this.kindAt(index) == AiBuildSite.Inner_CachePool.FLUID;
               BlockPos base = this.posAt(index);

               for (int ordinal = 0; ordinal < DIRECTIONS.length; ordinal++) {
                  if ((mask & 1 << ordinal) != 0) {
                     BlockPos at = base.relative(DIRECTIONS[ordinal]);
                     if (!this.contains(at.asLong())) {
                        boolean held = fluid ? isFluidOrSolidAt(level, at) : isSolidAt(level, at);
                        if (!held) {
                           out.add(at.asLong());
                        }
                     }
                  }
               }
            }
         }
      }

      this.init_List = List.copyOf(out);
      return this.init_List;
   }

   boolean pop(int index) {
      return index >= 0 && index < this.cellCount && this.load_BitSet.get(index);
   }

   boolean standsOnSiteCell(int index) {
      if (index >= 0 && index < this.cellCount) {
         BlockPos at = BlockPos.of(this.positions[index]).below();
         int below = this.indexOfKey(at.asLong());
         return below >= 0 && this.init_BitSet.get(below);
      } else {
         return false;
      }
   }

   boolean hasHeadroom(int index) {
      if (index >= 0 && index < this.cellCount) {
         BlockPos at = BlockPos.of(this.positions[index]);
         return this.indexOfKey(BlockPos.asLong(at.getX(), at.getY() + 1, at.getZ())) >= 0
            ? false
            : this.indexOfKey(BlockPos.asLong(at.getX(), at.getY() + 2, at.getZ())) < 0;
      } else {
         return false;
      }
   }

   private int sync_I(int index) {
      if (this.load_BitSet.get(index)) {
         return 0;
      } else {
         return this.hasHeadroom(index) ? 1 : 2;
      }
   }

   int[] init_IArr_I_I(int fromIndex, int max) {
      if (max > 0 && this.read_IArr.length != 0) {
         int lowerRank = fromIndex >= 0 && fromIndex < this.cellCount ? this.rankByIndex[fromIndex] : 0;
         int[] found = this.load_IArr_I_I(lowerRank, max);
         if (found.length == 0 && this.frontierScanCursor < this.read_IArr.length) {
            this.load_V();
            found = this.load_IArr_I_I(lowerRank, max);
         }

         return found;
      } else {
         return EMPTY_INTS;
      }
   }

   private int[] load_IArr_I_I(int lowerRank, int max) {
      int low = 0;
      int high = this.read_IArr.length - 1;
      int start = this.read_IArr.length;

      while (low <= high) {
         int middle = low + high >>> 1;
         if (this.rankByIndex[this.read_IArr[middle]] < lowerRank) {
            low = middle + 1;
         } else {
            start = middle;
            high = middle - 1;
         }
      }

      AiBuildSite.Inner_StreamUtil out = new AiBuildSite.Inner_StreamUtil();
      int budget = Math.max(512, max * 64);

      for (int at = start; at < this.read_IArr.length && out.size() < max && budget-- > 0; at++) {
         int index = this.read_IArr[at];
         if (this.pending.get(index) && !this.placed.get(index) && this.read_BitSet.get(index)) {
            out.add(index);
         }
      }

      return out.toArray();
   }

   private void load_V() {
      int budget = Math.max(4096, this.read_IArr.length >>> 3);

      while (this.frontierScanCursor < this.read_IArr.length && budget-- > 0) {
         int index = this.read_IArr[this.frontierScanCursor++];
         if (this.pending.get(index) && !this.placed.get(index) && this.hasPlacedNeighbour(index)) {
            this.read_BitSet.set(index);
         }
      }
   }

   private boolean hasPlacedNeighbour(int index) {
      BlockPos base = this.posAt(index);

      for (Direction direction : DIRECTIONS) {
         int other = this.indexOfKey(base.relative(direction).asLong());
         if (other >= 0 && this.placed.get(other)) {
            return true;
         }
      }

      return false;
   }

   private void write_V_I(int index) {
      BlockPos base = this.posAt(index);

      for (Direction direction : DIRECTIONS) {
         int other = this.indexOfKey(base.relative(direction).asLong());
         if (other >= 0 && !this.placed.get(other) && this.pending.get(other) && this.load_BitSet.get(other)) {
            this.read_BitSet.set(other);
         }
      }
   }

   int[] neighborIndices(BlockPos position) {
      if (position == null) {
         return EMPTY_INTS;
      }

      int[] found = new int[DIRECTIONS.length];
      int count = 0;

      for (Direction direction : DIRECTIONS) {
         int other = this.indexOfKey(position.relative(direction).asLong());
         if (other >= 0 && this.load_BitSet.get(other)) {
            found[count++] = other;
         }
      }

      if (count > 1) {
         this.mergeSort(found, count);
      }

      return count == 0 ? EMPTY_INTS : (count == found.length ? found : Arrays.copyOf(found, count));
   }

   Set<Long> load_Set() {
      if (this.load_Set == null) {
         this.load_Set = new AiBuildSite.Inner_ConfigStore();
      }

      return this.load_Set;
   }

   long[] load_JArr() {
      return this.read_JArr;
   }

   boolean canStandAt(BlockPos feet) {
      LevelReader level = this.currentLevel();
      if (level == null || feet == null) {
         return false;
      } else if (isLoaded(level, feet.below()) && isLoaded(level, feet) && isLoaded(level, feet.above())) {
         return this.indexOfKey(feet.asLong()) < 0 && this.indexOfKey(feet.above().asLong()) < 0
            ? this.init_Z_LevelReader_BlockPos(level, feet.below())
               && isPassable(level, feet)
               && isPassable(level, feet.above())
            : false;
      } else {
         return false;
      }
   }

   private boolean init_Z_LevelReader_BlockPos(LevelReader level, BlockPos floorPos) {
      int index = this.indexOfKey(floorPos.asLong());
      if (index < 0) {
         BlockState state = level.getBlockState(floorPos);
         if (state.isAir() || state.canBeReplaced()) {
            return false;
         } else if (state.getCollisionShape(level, floorPos).isEmpty()) {
            return false;
         } else {
            return !state.getFluidState().isEmpty() ? false : !autismclient.ai.engine.AiPathfinder.LiveGridView.isFloorHazardCompat(state);
         }
      } else {
         return this.placed.get(index) && this.init_BitSet.get(index);
      }
   }

   private static boolean isPassable(LevelReader level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      if (!state.getCollisionShape(level, pos).isEmpty()) {
         return false;
      } else {
         return !state.getFluidState().isEmpty()
            ? false
            : !autismclient.ai.engine.AiPathfinder.LiveGridView.isFeetHazardCompat(state) && !autismclient.ai.engine.AiPathfinder.LiveGridView.isFloorHazardCompat(state);
      }
   }

   BlockPos[] init_BlockPosArr_Vec3(Vec3 center, double radius, int max) {
      if (center != null && max > 0 && this.cellCount != 0) {
         LevelReader level = this.currentLevel();
         if (level == null) {
            return EMPTY_POSITIONS;
         }

         double capped = Math.min(radius, 8.0);
         if (capped <= 0.0) {
            return EMPTY_POSITIONS;
         }

         double radiusSq = capped * capped;
         int baseX = (int)Math.floor(center.x);
         int baseY = (int)Math.floor(center.y);
         int baseZ = (int)Math.floor(center.z);
         int reach = (int)Math.ceil(capped);
         BlockPos[] best = new BlockPos[Math.min(max, 64)];
         double[] bestDistance = new double[best.length];
         int count = 0;

         for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -reach; dy <= reach; dy++) {
               for (int dz = -reach; dz <= reach; dz++) {
                  BlockPos feet = new BlockPos(baseX + dx, baseY + dy, baseZ + dz);
                  double distance = Vec3.atBottomCenterOf(feet).distanceToSqr(center);
                  if (!(distance > radiusSq)
                     && (count != best.length || init_Z_D(distance, feet, bestDistance[count - 1], best[count - 1]))
                     && this.canStandAt(feet)) {
                     int at;
                     for (at = count < best.length ? count : best.length - 1; at > 0 && init_Z_D(distance, feet, bestDistance[at - 1], best[at - 1]); at--) {
                        best[at] = best[at - 1];
                        bestDistance[at] = bestDistance[at - 1];
                     }

                     best[at] = feet;
                     bestDistance[at] = distance;
                     if (count < best.length) {
                        count++;
                     }
                  }
               }
            }
         }

         return count == 0 ? EMPTY_POSITIONS : Arrays.copyOf(best, count);
      } else {
         return EMPTY_POSITIONS;
      }
   }

   private static boolean init_Z_D(double distance, BlockPos feet, double otherDistance, BlockPos other) {
      if (distance < otherDistance) {
         return true;
      } else if (distance > otherDistance) {
         return false;
      } else if (feet.getY() != other.getY()) {
         return feet.getY() < other.getY();
      } else {
         return feet.getX() != other.getX() ? feet.getX() < other.getX() : feet.getZ() < other.getZ();
      }
   }

   private int[] buildSweepOrder() {
      int[] order = new int[this.cellCount];
      int i = 0;

      while (i < this.cellCount) {
         order[i] = i++;
      }

      int[] other = new int[this.cellCount];
      int[] field = new int[this.cellCount];

      for (int pass = 0; pass < 6; pass++) {
         for (int ix = 0; ix < this.cellCount; ix++) {
            int index = order[ix];
            long delta = this.deltas[index];
            int var12;
            switch (pass) {
               case 0:
                  var12 = init_I_I_I(unpackDx(delta), unpackDz(delta));
                  break;
               case 1:
                  var12 = poll_I_I(unpackDz(delta));
                  break;
               case 2:
                  var12 = open_I_I(this.kindOrdinals[index] & 255);
                  break;
               case 3:
                  var12 = poll_I_I(this.load_IArr[index]);
                  break;
               case 4:
                  var12 = this.sync_I(index);
                  break;
               default:
                  var12 = poll_I_I(unpackDy(delta));
            }

            field[ix] = var12;
         }

         int[] target = other;
         short var10000;
         switch (pass) {
            case 2:
               var10000 = 4;
               break;
            case 4:
               var10000 = 3;
               break;
            default:
               var10000 = 4096;
         }

         int range = var10000;
         countingSort(order, target, field, range);
         other = order;
         order = target;
      }

      return order;
   }

   private int[] init_IArr_IArr(int[] base) {
      if (this.cellCount < 2) {
         return base;
      }

      int[] baseRank = new int[this.cellCount];
      int rank = 0;

      while (rank < this.cellCount) {
         baseRank[base[rank]] = rank++;
      }

      int[] provider = new int[this.cellCount];
      Arrays.fill(provider, -1);

      for (int rankx = 0; rankx < this.cellCount; rankx++) {
         int index = base[rankx];
         AiBuildSite.Inner_CachePool kind = this.kindAt(index);
         if (kind == AiBuildSite.Inner_CachePool.PARTNER) {
            int found = this.init_I_I(index, this.load_BArr[index] & 255, baseRank, 2147483647);
            if (found >= 0) {
               provider[index] = found;
            }
         } else if (kind == AiBuildSite.Inner_CachePool.SIMPLE || kind == AiBuildSite.Inner_CachePool.INTERACT) {
            int found = this.init_I_I_IArr(index, baseRank);
            if (found >= 0) {
               provider[index] = found;
            }
         }
      }

      for (int rankx = 0; rankx < this.cellCount; rankx++) {
         int index = base[rankx];
         if (provider[index] < 0) {
            int sibling = this.chestPartnerOf(index);
            if (sibling >= 0 && baseRank[sibling] < rankx) {
               provider[index] = sibling;
            }
         }
      }

      int[] dependentCount = new int[this.cellCount];

      for (int index = 0; index < this.cellCount; index++) {
         int parent = provider[index];
         if (parent >= 0 && parent != index) {
            dependentCount[parent]++;
         }
      }

      int[] dependentStart = new int[this.cellCount + 1];
      int total = 0;

      for (int index = 0; index < this.cellCount; index++) {
         dependentStart[index] = total;
         total += dependentCount[index];
      }

      dependentStart[this.cellCount] = total;
      int[] dependents = new int[total];
      int[] cursor = Arrays.copyOf(dependentStart, this.cellCount);

      for (int rankx = 0; rankx < this.cellCount; rankx++) {
         int index = base[rankx];
         int parent = provider[index];
         if (parent >= 0 && parent != index) {
            dependents[cursor[parent]++] = index;
         }
      }

      int[] out = new int[this.cellCount];
      BitSet emitted = new BitSet(this.cellCount);
      BitSet onPath = new BitSet(this.cellCount);
      int[] stack = new int[Math.max(8, Math.min(this.cellCount, 64))];
      int emittedCount = 0;

      for (int rankx = 0; rankx < this.cellCount; rankx++) {
         int start = base[rankx];
         if (!emitted.get(start)) {
            int depth = 0;
            if (depth == stack.length) {
               stack = Arrays.copyOf(stack, stack.length * 2);
            }

            stack[depth++] = start;

            while (depth > 0) {
               int index = stack[depth - 1];
               int parent = provider[index];
               if (parent >= 0 && !emitted.get(parent)) {
                  if (!onPath.get(parent)) {
                     onPath.set(index);
                     if (depth == stack.length) {
                        stack = Arrays.copyOf(stack, stack.length * 2);
                     }

                     stack[depth++] = parent;
                     continue;
                  }

                  provider[index] = -1;
               }

               depth--;
               if (!emitted.get(index)) {
                  onPath.clear(index);
                  emitted.set(index);
                  out[emittedCount++] = index;
                  int from = dependentStart[index];
                  int to = from + dependentCount[index];

                  for (int slot = to - 1; slot >= from; slot--) {
                     int dependent = dependents[slot];
                     if (!emitted.get(dependent) && !onPath.get(dependent)) {
                        if (depth == stack.length) {
                           stack = Arrays.copyOf(stack, stack.length * 2);
                        }

                        stack[depth++] = dependent;
                     }
                  }
               }
            }
         }
      }

      return out;
   }

   private int init_I_I(int index, int mask, int[] baseRank, int ignore) {
      int best = -1;
      int bestRank = 2147483647;
      BlockPos base = this.posAt(index);

      for (int ordinal = 0; ordinal < DIRECTIONS.length; ordinal++) {
         if ((mask & 1 << ordinal) != 0) {
            int found = this.indexOfKey(base.relative(DIRECTIONS[ordinal]).asLong());
            if (found >= 0 && found != index) {
               AiBuildSite.Inner_CachePool kind = this.kindAt(found);
               if (kind != AiBuildSite.Inner_CachePool.FLUID && kind != AiBuildSite.Inner_CachePool.DERIVED && kind != AiBuildSite.Inner_CachePool.SKIP && found != ignore
                  )
                {
                  int rank = baseRank[found];
                  if (rank < bestRank) {
                     best = found;
                     bestRank = rank;
                  }
               }
            }
         }
      }

      return best;
   }

   private int init_I_I_IArr(int index, int[] baseRank) {
      int mask = this.load_BArr[index] & 255;
      if (mask == 0) {
         return -1;
      }

      int best = -1;
      int bestRank = 2147483647;
      BlockPos base = this.posAt(index);

      for (int ordinal = 0; ordinal < DIRECTIONS.length; ordinal++) {
         if ((mask & 1 << ordinal) != 0) {
            Direction direction = DIRECTIONS[ordinal];
            if (direction.getAxis().isHorizontal()) {
               int found = this.indexOfKey(base.relative(direction).asLong());
               if (found >= 0 && found != index) {
                  AiBuildSite.Inner_CachePool kind = this.kindAt(found);
                  if (kind != AiBuildSite.Inner_CachePool.FLUID && kind != AiBuildSite.Inner_CachePool.DERIVED && kind != AiBuildSite.Inner_CachePool.SKIP) {
                     int rank = baseRank[found];
                     if (rank < bestRank) {
                        best = found;
                        bestRank = rank;
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   private int chestPartnerOf(int index) {
      AiBuilder.CellSpec cell = this.specs[index];
      String type = cell.properties().get("type");
      if (type != null && !type.equals("single") && cell.block().endsWith("chest")) {
         Direction side = AiBuilder.chestPartnerDirection(cell);
         if (side == null) {
            return -1;
         } else {
            int sibling = this.indexOfKey(this.posAt(index).relative(side).asLong());
            if (sibling >= 0 && sibling != index) {
               return this.specs[sibling].block().equals(cell.block()) ? sibling : -1;
            } else {
               return -1;
            }
         }
      } else {
         return -1;
      }
   }

   private int close_I(int index) {
      int rank = this.rankByIndex[index];
      if (rank + 1 < this.cellCount) {
         int next = this.indexByRank[rank + 1];
         if (this.init_Z_I_I(next, index)) {
            return next;
         }
      }

      if (rank > 0) {
         int previous = this.indexByRank[rank - 1];
         if (this.init_Z_I_I(index, previous)) {
            return previous;
         }
      }

      return -1;
   }

   private boolean init_Z_I_I(int dependent, int provider) {
      if (dependent == provider) {
         return false;
      }

      if (this.kindAt(dependent) == AiBuildSite.Inner_CachePool.PARTNER) {
         int mask = this.load_BArr[dependent] & 255;
         BlockPos base = this.posAt(dependent);

         for (int ordinal = 0; ordinal < DIRECTIONS.length; ordinal++) {
            if ((mask & 1 << ordinal) != 0 && this.indexOfKey(base.relative(DIRECTIONS[ordinal]).asLong()) == provider) {
               return true;
            }
         }

         return false;
      } else {
         return this.chestPartnerOf(dependent) == provider;
      }
   }

   private static int open_I_I(int kindOrdinal) {
      byte var10000;
      switch (KINDS[kindOrdinal]) {
         case SIMPLE:
         case INTERACT:
         case PARTNER:
            var10000 = 0;
            break;
         case SPECIAL:
            var10000 = 1;
            break;
         case FLUID:
            var10000 = 2;
            break;
         case DERIVED:
         case SKIP:
            var10000 = 3;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static int init_I_I_I(int dx, int dz) {
      return poll_I_I((dz & 1) == 0 ? dx : 4095 - dx);
   }

   private static int poll_I_I(int value) {
      return Math.clamp(value, 0, 4095);
   }

   private static void countingSort(int[] src, int[] dst, int[] field, int range) {
      int[] counts = new int[range];

      for (int i = 0; i < src.length; i++) {
         counts[field[i]]++;
      }

      int sum = 0;

      for (int value = 0; value < range; value++) {
         int count = counts[value];
         counts[value] = sum;
         sum += count;
      }

      for (int i = 0; i < src.length; i++) {
         int index = src[i];
         dst[counts[field[i]]++] = index;
      }
   }

   private static byte offsetMask(AiBuilder.CellSpec cell, AiBuildSite.Inner_CachePool kind) {
      List<BlockPos> offsets = AiBuildIntent.offsetsFor(cell, kind);
      if (offsets.isEmpty()) {
         return 0;
      }

      int mask = 0;

      for (BlockPos offset : offsets) {
         Direction direction = Direction.getNearest(offset.getX(), offset.getY(), offset.getZ(), null);
         if (direction != null) {
            mask |= 1 << direction.ordinal();
         }
      }

      return (byte)mask;
   }

   private void read_V() {
      Arrays.fill(this.load_IArr, -1);
      int[] chain = new int[Math.min(this.cellCount, 64)];

      for (int i = 0; i < this.cellCount; i++) {
         if (this.load_IArr[i] < 0) {
            int length = 0;
            int current = i;
            int base = 0;

            while (true) {
               if (length == chain.length) {
                  chain = Arrays.copyOf(chain, chain.length * 2);
               }

               chain[length++] = current;
               int belowY = this.originY + unpackDy(this.deltas[current]) - 1;
               long belowKey = BlockPos.asLong(this.originX + unpackDx(this.deltas[current]), belowY, this.originZ + unpackDz(this.deltas[current]));
               int below = this.indexOfKey(belowKey);
               if (below < 0) {
                  base = 0;
                  break;
               }

               if (this.load_IArr[below] >= 0) {
                  base = this.load_IArr[below] + 1;
                  break;
               }

               current = below;
            }

            int depth = base;

            for (int slot = length - 1; slot >= 0; slot--) {
               this.load_IArr[chain[slot]] = poll_I_I(depth++);
            }
         }
      }
   }

   private Map<Long, int[]> load_Map() {
      if (this.cellCount == 0) {
         return Map.of();
      }

      HashMap<Long, AiBuildSite.Inner_StreamUtil> grouped = new HashMap<>();

      for (int i = 0; i < this.cellCount; i++) {
         long delta = this.deltas[i];
         long key = sectionKey(this.originX + unpackDx(delta), this.originY + unpackDy(delta), this.originZ + unpackDz(delta));
         grouped.computeIfAbsent(key, ignored -> new AiBuildSite.Inner_StreamUtil()).add(i);
      }

      HashMap<Long, int[]> out = new HashMap<>(Math.max(16, grouped.size() * 2));

      for (Entry<Long, AiBuildSite.Inner_StreamUtil> entry : grouped.entrySet()) {
         out.put(entry.getKey(), entry.getValue().toArray());
      }

      return out;
   }

   private static long sectionKey(int x, int y, int z) {
      return BlockPos.asLong(x >> 4, y >> 4, z >> 4);
   }

   private int[] indicesNear(Vec3 center, double radius) {
      AiBuildSite.Inner_StreamUtil found = new AiBuildSite.Inner_StreamUtil();
      double radiusSq = radius * radius;
      int minSectionX = (int)Math.floor(center.x - radius) >> 4;
      int maxSectionX = (int)Math.floor(center.x + radius) >> 4;
      int minSectionY = (int)Math.floor(center.y - radius) >> 4;
      int maxSectionY = (int)Math.floor(center.y + radius) >> 4;
      int minSectionZ = (int)Math.floor(center.z - radius) >> 4;
      int maxSectionZ = (int)Math.floor(center.z + radius) >> 4;

      for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
         for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
            for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
               int[] indices = this.load_Map.get(BlockPos.asLong(sectionX, sectionY, sectionZ));
               if (indices != null) {
                  for (int index : indices) {
                     if (this.pending.get(index) && !this.flush_BitSet.get(index)) {
                        long delta = this.deltas[index];
                        double dx = this.originX + unpackDx(delta) + 0.5 - center.x;
                        double dy = this.originY + unpackDy(delta) + 0.5 - center.y;
                        double dz = this.originZ + unpackDz(delta) + 0.5 - center.z;
                        if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                           found.add(index);
                        }
                     }
                  }
               }
            }
         }
      }

      return found.toArray();
   }

   private LevelReader currentLevel() {
      if (this.world != null) {
         return this.world;
      }

      try {
         Minecraft mc = Minecraft.getInstance();
         return mc == null ? null : mc.level;
      } catch (RuntimeException | LinkageError unavailable) {
         return null;
      }
   }

   private boolean init_Z_I_LevelReader(int index, LevelReader level) {
      if (index >= 0 && index < this.cellCount) {
         int mask = this.load_BArr[index] & 255;
         if (mask != 0 && level != null) {
            BlockPos base = this.posAt(index);
            boolean fluid = this.kindAt(index) == AiBuildSite.Inner_CachePool.FLUID;

            for (int ordinal = 0; ordinal < DIRECTIONS.length; ordinal++) {
               if ((mask & 1 << ordinal) != 0) {
                  BlockPos at = base.relative(DIRECTIONS[ordinal]);
                  int other = this.indexOfKey(at.asLong());
                  if (other >= 0) {
                     AiBuildSite.Inner_CachePool otherKind = this.kindAt(other);
                     if (otherKind != AiBuildSite.Inner_CachePool.FLUID
                        && otherKind != AiBuildSite.Inner_CachePool.DERIVED
                        && otherKind != AiBuildSite.Inner_CachePool.SKIP
                        && !this.placed.get(other)) {
                        return false;
                     }
                  } else if (fluid && !isFluidOrSolidAt(level, at)) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean isLoaded(LevelReader level, BlockPos at) {
      return !(level instanceof Level real && !real.isLoaded(at));
   }

   private static boolean isSolidAt(LevelReader level, BlockPos at) {
      if (!isLoaded(level, at)) {
         return false;
      }

      BlockState state = level.getBlockState(at);
      return !state.isAir() && !state.canBeReplaced() && !state.getShape(level, at).isEmpty();
   }

   private static boolean isFluidOrSolidAt(LevelReader level, BlockPos at) {
      if (!isLoaded(level, at)) {
         return false;
      }

      BlockState state = level.getBlockState(at);
      return !state.getFluidState().isEmpty() ? true : !state.isAir() && !state.canBeReplaced() && !state.getShape(level, at).isEmpty();
   }

   private boolean init_Z_I_BlockState(int index, BlockState live) {
      AiBuilder.CellSpec cell = this.specs[index];
      if (AiBuildIntent.init(cell, live)) {
         return true;
      } else {
         return AiBuildIntent.isPoweredLever(cell) && AiBuildIntent.init(AiBuildIntent.unpoweredLever(cell), live) ? true : AiBuildIntent.init(cell, live, this.substitutes);
      }
   }

   private void mergeSort(int[] values) {
      int size = values.length;
      if (size >= 2) {
         int[] buffer = new int[size];

         for (int width = 1; width < size; width <<= 1) {
            for (int low = 0; low < size; low += width * 2) {
               int middle = Math.min(low + width, size);
               int high = Math.min(low + width * 2, size);
               int left = low;
               int right = middle;
               int write = low;

               while (left < middle && right < high) {
                  if (this.positions[values[left]] <= this.positions[values[right]]) {
                     buffer[write++] = values[left++];
                  } else {
                     buffer[write++] = values[right++];
                  }
               }

               while (left < middle) {
                  buffer[write++] = values[left++];
               }

               while (right < high) {
                  buffer[write++] = values[right++];
               }
            }

            System.arraycopy(buffer, 0, values, 0, size);
         }
      }
   }

   private void mergeSort(int[] values, int size) {
      if (size >= 2) {
         int[] buffer = new int[size];

         for (int width = 1; width < size; width <<= 1) {
            for (int low = 0; low < size; low += width * 2) {
               int middle = Math.min(low + width, size);
               int high = Math.min(low + width * 2, size);
               int left = low;
               int right = middle;
               int write = low;

               while (left < middle && right < high) {
                  if (this.rankByIndex[values[left]] <= this.rankByIndex[values[right]]) {
                     buffer[write++] = values[left++];
                  } else {
                     buffer[write++] = values[right++];
                  }
               }

               while (left < middle) {
                  buffer[write++] = values[left++];
               }

               while (right < high) {
                  buffer[write++] = values[right++];
               }
            }

            System.arraycopy(buffer, 0, values, 0, size);
         }
      }
   }

   private static long packDelta(int dx, int dy, int dz) {
      long x = (long)(dx + 1048576) << 42;
      long y = (long)(dy + 1048576) << 21;
      long z = (long)(dz + 1048576) << 0;
      return x | y | z;
   }

   private static int unpackDx(long delta) {
      return (int)(delta >>> 42 & 2097151L) - 1048576;
   }

   private static int unpackDy(long delta) {
      return (int)(delta >>> 21 & 2097151L) - 1048576;
   }

   private static int unpackDz(long delta) {
      return (int)(delta >>> 0 & 2097151L) - 1048576;
   }

   private final class Inner_BufferHelper extends AbstractSet<Long> {
      @Override
      public int size() {
         return AiBuildSite.this.cellCount;
      }

      @Override
      public boolean contains(Object candidate) {
         return candidate instanceof Long key && AiBuildSite.this.indexOfKey(key) >= 0;
      }

      @Override
      public Iterator<Long> iterator() {
         return new Iterator<Long>() {
            private int cursor;

            @Override
            public boolean hasNext() {
               return this.cursor < AiBuildSite.this.cellCount;
            }

            public Long next() {
               return this.nextKey();
            }

            public Long nextKey() {
               if (this.cursor >= AiBuildSite.this.cellCount) {
                  throw new NoSuchElementException();
               } else {
                  return AiBuildSite.this.sortedKeys[this.cursor++];
               }
            }
         };
      }
   }

   enum Inner_CachePool {
      SIMPLE,
      INTERACT,
      SPECIAL,
      FLUID,
      PARTNER,
      DERIVED,
      SKIP;
   }

   private final class Inner_ConfigStore extends AbstractSet<Long> {
      @Override
      public int size() {
         return AiBuildSite.this.read_JArr.length;
      }

      @Override
      public boolean contains(Object candidate) {
         if (candidate instanceof Long key) {
            int low = 0;
            int high = AiBuildSite.this.read_JArr.length - 1;
            long wanted = key;

            while (low <= high) {
               int middle = low + high >>> 1;
               long value = AiBuildSite.this.read_JArr[middle];
               if (value < wanted) {
                  low = middle + 1;
               } else {
                  if (value <= wanted) {
                     return true;
                  }

                  high = middle - 1;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      @Override
      public Iterator<Long> iterator() {
         return new Iterator<Long>() {
            private int cursor;

            @Override
            public boolean hasNext() {
               return this.cursor < AiBuildSite.this.read_JArr.length;
            }

            public Long next() {
               return this.nextKey();
            }

            public Long nextKey() {
               if (this.cursor >= AiBuildSite.this.read_JArr.length) {
                  throw new NoSuchElementException();
               } else {
                  return AiBuildSite.this.read_JArr[this.cursor++];
               }
            }
         };
      }
   }

   private record Inner_DataUtil(byte kindOrdinal, Item item, byte dependencies, boolean standable) {
   }

   private static final class Inner_StreamUtil {
      private int[] buffer = new int[8];
      private int size;

      void add(int value) {
         if (this.size == this.buffer.length) {
            this.buffer = Arrays.copyOf(this.buffer, this.size * 2);
         }

         this.buffer[this.size++] = value;
      }

      int size() {
         return this.size;
      }

      int[] toArray() {
         return this.size == 0 ? AiBuildSite.EMPTY_INTS : Arrays.copyOf(this.buffer, this.size);
      }
   }
}
