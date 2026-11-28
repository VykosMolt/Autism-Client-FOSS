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
   private static final AiBuildSite.CellKind[] KINDS = AiBuildSite.CellKind.values();
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
   private final byte[] dependencyMasks;
   private final int[] columnDepths;
   private final BitSet standableCells;
   private final BitSet walkableCells;
   private final long[] walkablePositions;
   private final int[] walkableByRank;
   private final BitSet frontierCells;
   private int frontierScanCursor;
   private final long[] sortedKeys;
   private final int[] byPosition;
   private final int[] indexByRank;
   private final int[] rankByIndex;
   private final Map<Long, int[]> indicesBySection;
   private final BitSet pending;
   private final BitSet placed;
   private final BitSet failed;
   private final BitSet flowingFluidCells;
   private final int[] observedIndices;
   private int pendingTotal;
   private int placedTotal;
   private int failedTotal;
   private Map<Integer, String> failureReasons;
   private List<Long> missingSupportsCache;
   private Set<Long> cellKeySetCache;
   private Set<Long> walkableKeySetCache;
   private int[] poweredLeverCache = EMPTY_INTS;
   private int firstPendingRank;
   private int revision;
   private int materialsRevision = -1;
   private int materialsMax = -1;
   private Map<String, Integer> materialsCache = Map.of();
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
      this.dependencyMasks = new byte[this.cellCount];
      this.columnDepths = new int[this.cellCount];
      this.pending = new BitSet(this.cellCount);
      this.placed = new BitSet(this.cellCount);
      this.failed = new BitSet(this.cellCount);
      this.flowingFluidCells = new BitSet(this.cellCount);
      this.standableCells = new BitSet(this.cellCount);
      this.frontierCells = new BitSet(this.cellCount);
      HashMap<String, Map<Map<String, String>, AiBuildSite.CellClass>> classified = new HashMap<>();
      AiBuildSite.IntList observedIndices = new AiBuildSite.IntList();
      int classificationFailures = 0;

      for (int i = 0; i < this.cellCount; i++) {
         AiBuilder.CellSpec cell = input.get(i);
         this.specs[i] = cell;
         this.deltas[i] = packDelta(cell.dx(), cell.dy(), cell.dz());
         this.positions[i] = BlockPos.asLong(this.originX + cell.dx(), this.originY + cell.dy(), this.originZ + cell.dz());
         AiBuildSite.CellClass entry = classified.computeIfAbsent(cell.block(), ignored -> new HashMap<>()).get(cell.properties());
         if (entry == null) {
            boolean standable = false;

            AiBuildSite.CellKind kind;
            Item item;
            byte dependencies;
            try {
               kind = AiBuildIntent.kindOf(cell);
               item = AiBuildIntent.itemFor(cell, kind);
               dependencies = offsetMask(cell, kind);
               standable = isStandableCell(cell);
            } catch (RuntimeException unknown) {
               if (classificationFailures++ == 0) {
                  AutismClientAddon.LOG.warn("[AI] build site could not classify {}, treating it as SKIP", cell, unknown);
               }

               kind = AiBuildSite.CellKind.SKIP;
               item = Items.AIR;
               dependencies = 0;
            }

            entry = new AiBuildSite.CellClass((byte)kind.ordinal(), item, dependencies, standable);
            classified.get(cell.block()).put(cell.properties(), entry);
         }

         this.kindOrdinals[i] = entry.kindOrdinal();
         this.items[i] = entry.item();
         this.dependencyMasks[i] = entry.dependencies();
         if (entry.standable()) {
            this.standableCells.set(i);
         }

         boolean placeable = this.kindOrdinals[i] != (byte)AiBuildSite.CellKind.DERIVED.ordinal()
            && this.kindOrdinals[i] != (byte)AiBuildSite.CellKind.SKIP.ordinal();
         if (placeable) {
            this.pending.set(i);
            this.pendingTotal++;
            if (AiBuilder.isFlowingFluidCell(cell)) {
               this.flowingFluidCells.set(i);
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

      this.walkableCells = new BitSet(this.cellCount);

      for (int ix = 0; ix < this.cellCount; ix++) {
         if (this.standableCells.get(ix) && this.hasHeadroom(ix)) {
            this.walkableCells.set(ix);
         }
      }

      i = this.walkableCells.cardinality();
      this.walkablePositions = new long[i];
      int at = 0;

      for (int rank = 0; rank < this.cellCount && at < i; rank++) {
         int index = this.byPosition[rank];
         if (this.walkableCells.get(index)) {
            this.walkablePositions[at++] = this.positions[index];
         }
      }

      this.computeColumnDepths();
      this.indicesBySection = this.groupBySection();
      int[] ordered = this.buildSweepOrder();
      this.indexByRank = this.orderProvidersFirst(ordered);
      this.rankByIndex = new int[this.cellCount];
      int rank = 0;

      while (rank < this.cellCount) {
         this.rankByIndex[this.indexByRank[rank]] = rank++;
      }

      this.walkableByRank = new int[i];
      rank = 0;

      for (int rankx = 0; rankx < this.cellCount && rank < i; rankx++) {
         int index = this.indexByRank[rankx];
         if (this.walkableCells.get(index)) {
            this.walkableByRank[rank++] = index;
         }
      }

      this.advanceFirstPending();
   }

   static boolean isStandableCell(AiBuilder.CellSpec cell) {
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

   int unusedCellCount() {
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

   AiBuildSite.CellKind kindAt(int index) {
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

   int unusedColumnDepthAt(int index) {
      return this.columnDepths[index];
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

   int unusedChestPartnerAt(int index) {
      return this.chestPartnerOf(index);
   }

   boolean hasChestPartner(int index) {
      return this.chestPartnerOf(index) >= 0;
   }

   boolean isPlaceableNonFluid(int index) {
      AiBuildSite.CellKind kind = this.kindAt(index);
      return kind != AiBuildSite.CellKind.DERIVED && kind != AiBuildSite.CellKind.SKIP && !this.flowingFluidCells.get(index);
   }

   boolean unusedIsFlowingFluid(int index) {
      return this.flowingFluidCells.get(index);
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
            if (isLoaded(level, at) && this.matchesLive(index, level.getBlockState(at))) {
               this.markPlaced(index);
               reconciled++;
            }
         }
      }

      return reconciled;
   }

   int[] unusedPoweredLevers() {
      if (this.poweredLeverCache.length == 0) {
         AiBuildSite.IntList found = new AiBuildSite.IntList();

         for (int i = 0; i < this.cellCount; i++) {
            if (AiBuildIntent.isPoweredLever(this.specs[i])) {
               found.add(i);
            }
         }

         this.poweredLeverCache = found.toArray();
      }

      return this.poweredLeverCache;
   }

   boolean unusedMatchesWorld(int index) {
      LevelReader level = this.currentLevel();
      if (level == null) {
         return this.placed.get(index);
      }

      BlockPos at = this.posAt(index);
      return isLoaded(level, at) && AiBuildIntent.matches(this.specs[index], level.getBlockState(at));
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
         this.addWalkableNeighboursToFrontier(index);
         int partner = this.partnerOf(index);
         if (partner >= 0 && !this.placed.get(partner)) {
            this.syncPlacedFromWorld(partner);
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
            if (isLoaded(level, at) && this.matchesLive(i, level.getBlockState(at))) {
               this.markPlaced(i);
               adopted++;
            }
         }
      }

      return adopted;
   }

   boolean syncPlacedFromWorld(int index) {
      LevelReader level = this.currentLevel();
      if (level == null) {
         return this.placed.get(index);
      }

      BlockPos at = this.posAt(index);
      if (isLoaded(level, at) && this.matchesLive(index, level.getBlockState(at))) {
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

   boolean unusedSupportsReady(int index) {
      return this.supportsReady(index, this.currentLevel());
   }

   int[] unusedReadyCellsNear(Vec3 eye, double radius, int max) {
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
            if (this.supportsReady(index, level)) {
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

   int[] unusedCellsNear(Vec3 center, double radius, int max) {
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

   Map<String, Integer> unusedRemainingMaterials(int max) {
      if (max <= 0 || this.cellCount == 0) {
         return Map.of();
      }

      if (this.materialsRevision == this.revision && this.materialsMax == max) {
         return this.materialsCache;
      }

      Map<String, Integer> out = new LinkedHashMap<>();
      int limit = Math.min(this.cellCount, this.firstPendingRank + 4096);

      for (int rank = this.firstPendingRank; rank < limit && out.size() < max; rank++) {
         int index = this.indexByRank[rank];
         if (this.pending.get(index) && !this.flowingFluidCells.get(index)) {
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
      this.materialsCache = Collections.unmodifiableMap(out);
      return this.materialsCache;
   }

   Set<Long> unusedCellKeySet() {
      if (this.cellKeySetCache == null) {
         this.cellKeySetCache = new AiBuildSite.CellKeySet();
      }

      return this.cellKeySetCache;
   }

   List<Long> unusedMissingSupports() {
      if (this.missingSupportsCache != null) {
         return this.missingSupportsCache;
      }

      LevelReader level = this.currentLevel();
      if (level == null) {
         this.missingSupportsCache = List.of();
         return this.missingSupportsCache;
      }

      LinkedHashSet<Long> out = new LinkedHashSet<>();
      int limit = Math.min(this.cellCount, this.firstPendingRank + 8192);

      for (int rank = this.firstPendingRank; rank < limit; rank++) {
         int index = this.indexByRank[rank];
         if (this.pending.get(index) && this.isPlaceableNonFluid(index)) {
            int mask = this.dependencyMasks[index] & 255;
            if (mask != 0) {
               boolean fluid = this.kindAt(index) == AiBuildSite.CellKind.FLUID;
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

      this.missingSupportsCache = List.copyOf(out);
      return this.missingSupportsCache;
   }

   boolean unusedIsWalkable(int index) {
      return index >= 0 && index < this.cellCount && this.walkableCells.get(index);
   }

   boolean standsOnSiteCell(int index) {
      if (index >= 0 && index < this.cellCount) {
         BlockPos at = BlockPos.of(this.positions[index]).below();
         int below = this.indexOfKey(at.asLong());
         return below >= 0 && this.standableCells.get(below);
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

   private int standSortClass(int index) {
      if (this.walkableCells.get(index)) {
         return 0;
      } else {
         return this.hasHeadroom(index) ? 1 : 2;
      }
   }

   int[] unusedFrontierAfter(int fromIndex, int max) {
      if (max > 0 && this.walkableByRank.length != 0) {
         int lowerRank = fromIndex >= 0 && fromIndex < this.cellCount ? this.rankByIndex[fromIndex] : 0;
         int[] found = this.frontierFromRank(lowerRank, max);
         if (found.length == 0 && this.frontierScanCursor < this.walkableByRank.length) {
            this.scanFrontier();
            found = this.frontierFromRank(lowerRank, max);
         }

         return found;
      } else {
         return EMPTY_INTS;
      }
   }

   private int[] frontierFromRank(int lowerRank, int max) {
      int low = 0;
      int high = this.walkableByRank.length - 1;
      int start = this.walkableByRank.length;

      while (low <= high) {
         int middle = low + high >>> 1;
         if (this.rankByIndex[this.walkableByRank[middle]] < lowerRank) {
            low = middle + 1;
         } else {
            start = middle;
            high = middle - 1;
         }
      }

      AiBuildSite.IntList out = new AiBuildSite.IntList();
      int budget = Math.max(512, max * 64);

      for (int at = start; at < this.walkableByRank.length && out.size() < max && budget-- > 0; at++) {
         int index = this.walkableByRank[at];
         if (this.pending.get(index) && !this.placed.get(index) && this.frontierCells.get(index)) {
            out.add(index);
         }
      }

      return out.toArray();
   }

   private void scanFrontier() {
      int budget = Math.max(4096, this.walkableByRank.length >>> 3);

      while (this.frontierScanCursor < this.walkableByRank.length && budget-- > 0) {
         int index = this.walkableByRank[this.frontierScanCursor++];
         if (this.pending.get(index) && !this.placed.get(index) && this.hasPlacedNeighbour(index)) {
            this.frontierCells.set(index);
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

   private void addWalkableNeighboursToFrontier(int index) {
      BlockPos base = this.posAt(index);

      for (Direction direction : DIRECTIONS) {
         int other = this.indexOfKey(base.relative(direction).asLong());
         if (other >= 0 && !this.placed.get(other) && this.pending.get(other) && this.walkableCells.get(other)) {
            this.frontierCells.set(other);
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
         if (other >= 0 && this.walkableCells.get(other)) {
            found[count++] = other;
         }
      }

      if (count > 1) {
         this.mergeSort(found, count);
      }

      return count == 0 ? EMPTY_INTS : (count == found.length ? found : Arrays.copyOf(found, count));
   }

   Set<Long> unusedWalkableKeySet() {
      if (this.walkableKeySetCache == null) {
         this.walkableKeySetCache = new AiBuildSite.WalkableKeySet();
      }

      return this.walkableKeySetCache;
   }

   long[] unusedWalkablePositions() {
      return this.walkablePositions;
   }

   boolean canStandAt(BlockPos feet) {
      LevelReader level = this.currentLevel();
      if (level == null || feet == null) {
         return false;
      } else if (isLoaded(level, feet.below()) && isLoaded(level, feet) && isLoaded(level, feet.above())) {
         return this.indexOfKey(feet.asLong()) < 0 && this.indexOfKey(feet.above().asLong()) < 0
            ? this.isSolidFloor(level, feet.below())
               && isPassable(level, feet)
               && isPassable(level, feet.above())
            : false;
      } else {
         return false;
      }
   }

   private boolean isSolidFloor(LevelReader level, BlockPos floorPos) {
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
         return this.placed.get(index) && this.standableCells.get(index);
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

   BlockPos[] unusedStandSpotsNear(Vec3 center, double radius, int max) {
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
                     && (count != best.length || isCloserStand(distance, feet, bestDistance[count - 1], best[count - 1]))
                     && this.canStandAt(feet)) {
                     int at;
                     for (at = count < best.length ? count : best.length - 1; at > 0 && isCloserStand(distance, feet, bestDistance[at - 1], best[at - 1]); at--) {
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

   private static boolean isCloserStand(double distance, BlockPos feet, double otherDistance, BlockPos other) {
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
                  var12 = serpentineSortX(unpackDx(delta), unpackDz(delta));
                  break;
               case 1:
                  var12 = clampSortKey(unpackDz(delta));
                  break;
               case 2:
                  var12 = kindSortClass(this.kindOrdinals[index] & 255);
                  break;
               case 3:
                  var12 = clampSortKey(this.columnDepths[index]);
                  break;
               case 4:
                  var12 = this.standSortClass(index);
                  break;
               default:
                  var12 = clampSortKey(unpackDy(delta));
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

   private int[] orderProvidersFirst(int[] base) {
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
         AiBuildSite.CellKind kind = this.kindAt(index);
         if (kind == AiBuildSite.CellKind.PARTNER) {
            int found = this.partnerProvider(index, this.dependencyMasks[index] & 255, baseRank, 2147483647);
            if (found >= 0) {
               provider[index] = found;
            }
         } else if (kind == AiBuildSite.CellKind.SIMPLE || kind == AiBuildSite.CellKind.INTERACT) {
            int found = this.horizontalProvider(index, baseRank);
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

   private int partnerProvider(int index, int mask, int[] baseRank, int ignore) {
      int best = -1;
      int bestRank = 2147483647;
      BlockPos base = this.posAt(index);

      for (int ordinal = 0; ordinal < DIRECTIONS.length; ordinal++) {
         if ((mask & 1 << ordinal) != 0) {
            int found = this.indexOfKey(base.relative(DIRECTIONS[ordinal]).asLong());
            if (found >= 0 && found != index) {
               AiBuildSite.CellKind kind = this.kindAt(found);
               if (kind != AiBuildSite.CellKind.FLUID && kind != AiBuildSite.CellKind.DERIVED && kind != AiBuildSite.CellKind.SKIP && found != ignore
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

   private int horizontalProvider(int index, int[] baseRank) {
      int mask = this.dependencyMasks[index] & 255;
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
                  AiBuildSite.CellKind kind = this.kindAt(found);
                  if (kind != AiBuildSite.CellKind.FLUID && kind != AiBuildSite.CellKind.DERIVED && kind != AiBuildSite.CellKind.SKIP) {
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

   private int partnerOf(int index) {
      int rank = this.rankByIndex[index];
      if (rank + 1 < this.cellCount) {
         int next = this.indexByRank[rank + 1];
         if (this.dependsOn(next, index)) {
            return next;
         }
      }

      if (rank > 0) {
         int previous = this.indexByRank[rank - 1];
         if (this.dependsOn(index, previous)) {
            return previous;
         }
      }

      return -1;
   }

   private boolean dependsOn(int dependent, int provider) {
      if (dependent == provider) {
         return false;
      }

      if (this.kindAt(dependent) == AiBuildSite.CellKind.PARTNER) {
         int mask = this.dependencyMasks[dependent] & 255;
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

   private static int kindSortClass(int kindOrdinal) {
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

   private static int serpentineSortX(int dx, int dz) {
      return clampSortKey((dz & 1) == 0 ? dx : 4095 - dx);
   }

   private static int clampSortKey(int value) {
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

   private static byte offsetMask(AiBuilder.CellSpec cell, AiBuildSite.CellKind kind) {
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

   private void computeColumnDepths() {
      Arrays.fill(this.columnDepths, -1);
      int[] chain = new int[Math.min(this.cellCount, 64)];

      for (int i = 0; i < this.cellCount; i++) {
         if (this.columnDepths[i] < 0) {
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

               if (this.columnDepths[below] >= 0) {
                  base = this.columnDepths[below] + 1;
                  break;
               }

               current = below;
            }

            int depth = base;

            for (int slot = length - 1; slot >= 0; slot--) {
               this.columnDepths[chain[slot]] = clampSortKey(depth++);
            }
         }
      }
   }

   private Map<Long, int[]> groupBySection() {
      if (this.cellCount == 0) {
         return Map.of();
      }

      HashMap<Long, AiBuildSite.IntList> grouped = new HashMap<>();

      for (int i = 0; i < this.cellCount; i++) {
         long delta = this.deltas[i];
         long key = sectionKey(this.originX + unpackDx(delta), this.originY + unpackDy(delta), this.originZ + unpackDz(delta));
         grouped.computeIfAbsent(key, ignored -> new AiBuildSite.IntList()).add(i);
      }

      HashMap<Long, int[]> out = new HashMap<>(Math.max(16, grouped.size() * 2));

      for (Entry<Long, AiBuildSite.IntList> entry : grouped.entrySet()) {
         out.put(entry.getKey(), entry.getValue().toArray());
      }

      return out;
   }

   private static long sectionKey(int x, int y, int z) {
      return BlockPos.asLong(x >> 4, y >> 4, z >> 4);
   }

   private int[] indicesNear(Vec3 center, double radius) {
      AiBuildSite.IntList found = new AiBuildSite.IntList();
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
               int[] indices = this.indicesBySection.get(BlockPos.asLong(sectionX, sectionY, sectionZ));
               if (indices != null) {
                  for (int index : indices) {
                     if (this.pending.get(index) && !this.flowingFluidCells.get(index)) {
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

   private boolean supportsReady(int index, LevelReader level) {
      if (index >= 0 && index < this.cellCount) {
         int mask = this.dependencyMasks[index] & 255;
         if (mask != 0 && level != null) {
            BlockPos base = this.posAt(index);
            boolean fluid = this.kindAt(index) == AiBuildSite.CellKind.FLUID;

            for (int ordinal = 0; ordinal < DIRECTIONS.length; ordinal++) {
               if ((mask & 1 << ordinal) != 0) {
                  BlockPos at = base.relative(DIRECTIONS[ordinal]);
                  int other = this.indexOfKey(at.asLong());
                  if (other >= 0) {
                     AiBuildSite.CellKind otherKind = this.kindAt(other);
                     if (otherKind != AiBuildSite.CellKind.FLUID
                        && otherKind != AiBuildSite.CellKind.DERIVED
                        && otherKind != AiBuildSite.CellKind.SKIP
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

   private boolean matchesLive(int index, BlockState live) {
      AiBuilder.CellSpec cell = this.specs[index];
      if (AiBuildIntent.matches(cell, live)) {
         return true;
      } else {
         return AiBuildIntent.isPoweredLever(cell) && AiBuildIntent.matches(AiBuildIntent.unpoweredLever(cell), live) ? true : AiBuildIntent.matches(cell, live, this.substitutes);
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

   private final class CellKeySet extends AbstractSet<Long> {
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

   enum CellKind {
      SIMPLE,
      INTERACT,
      SPECIAL,
      FLUID,
      PARTNER,
      DERIVED,
      SKIP;
   }

   private final class WalkableKeySet extends AbstractSet<Long> {
      @Override
      public int size() {
         return AiBuildSite.this.walkablePositions.length;
      }

      @Override
      public boolean contains(Object candidate) {
         if (candidate instanceof Long key) {
            int low = 0;
            int high = AiBuildSite.this.walkablePositions.length - 1;
            long wanted = key;

            while (low <= high) {
               int middle = low + high >>> 1;
               long value = AiBuildSite.this.walkablePositions[middle];
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
               return this.cursor < AiBuildSite.this.walkablePositions.length;
            }

            public Long next() {
               return this.nextKey();
            }

            public Long nextKey() {
               if (this.cursor >= AiBuildSite.this.walkablePositions.length) {
                  throw new NoSuchElementException();
               } else {
                  return AiBuildSite.this.walkablePositions[this.cursor++];
               }
            }
         };
      }
   }

   private record CellClass(byte kindOrdinal, Item item, byte dependencies, boolean standable) {
   }

   private static final class IntList {
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
