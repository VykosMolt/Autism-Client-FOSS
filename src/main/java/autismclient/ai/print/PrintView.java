package autismclient.ai.print;

import autismclient.ai.engine.AiPhysics;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class PrintView implements SignalGetter {
   static final BlockState UNLOADED_SENTINEL = Blocks.BARRIER.defaultBlockState();
   private final PrintEnv env;
   private final Long2ObjectOpenHashMap<BlockState> hypotheticalBlocks = new Long2ObjectOpenHashMap<>();
   private final Long2LongOpenHashMap hypotheticalColumnMasks = new Long2LongOpenHashMap();
   private final Long2ObjectOpenHashMap<ArrayDeque<PrintView.HypothesisUndo>> hypotheticalUndoStacks = new Long2ObjectOpenHashMap<>();
   private final LongOpenHashSet ownedTemporaryBlocks = new LongOpenHashSet();
   private final MutableBlockPos scratchPos = new MutableBlockPos();
   private final Long2ObjectOpenHashMap<BlockState> snapshotStateCache = new Long2ObjectOpenHashMap<>();
   private boolean snapshotCacheActive;
   static final int MAX_SNAPSHOT_STATES = 32768;
   static final int MAX_STANDABLE_CACHE = 65536;
   static final int MAX_CLICK_POINTS_CACHE = 8192;
   private final Long2ObjectLinkedOpenHashMap<PrintView.StandableHeightEntry> standableHeightCache = new Long2ObjectLinkedOpenHashMap<>();
   static final int MAX_RECEIPT_CELLS = 512;
   static final int DEFAULT_RECEIPT_BUDGET = 1048576;
   private static final int MAX_FORWARDED_RECEIPTS = 256;
   private final int geometryInputBudget;
   private int retainedReceiptCells;
   long standableCacheHits;
   long standableCacheMisses;
   private long viewRevision;
   private long worldMutationRevision;
   private final ArrayList<PrintView.ReadTracker> activeReadTrackers = new ArrayList<>();
   private final ArrayList<PrintView.NativeReadTracker> activeNativeTrackers = new ArrayList<>();
   private final ArrayList<PrintView.DynamicShapeTracker> activeDynamicShapeTrackers = new ArrayList<>();
   private boolean suppressDynamicShapeTracking;
   private final IdentityHashMap<BlockState, List<AABB>> collisionBoxesCache = new IdentityHashMap<>();
   private long untrackedReadCount;
   final Map<PrintNav.JumpKey, List<PrintNav.DiagonalJumpProof>> diagonalJumpProofCache = new LinkedHashMap<>();
   final Long2ObjectOpenHashMap<Direction> requiredInteractFaces = new Long2ObjectOpenHashMap<>();
   long diagonalJumpCacheMisses;
   long diagonalJumpCacheHits;
   final Long2LongOpenHashMap bucketRemovalRevision = new Long2LongOpenHashMap();
   final Long2LongOpenHashMap bucketAdditionRevision = new Long2LongOpenHashMap();
   final Long2ObjectOpenHashMap<LongArrayList> bucketAdditionLogs = new Long2ObjectOpenHashMap<>();
   final Long2LongOpenHashMap bucketOverflowRevision = new Long2LongOpenHashMap();
   static final int MAX_BUCKET_LOG_ENTRIES = 64;
   private long liveChangeRevision;
   private long ringChangeRevision;
   private final long[] recentChangedRing = new long[4096];
   private int recentChangedHead;
   private int recentChangedCount;
   private long ringResetRevision;
   static final boolean FEET_CACHE_ENABLED = !"false".equals(System.getProperty("autism.ai.print.feetCache", "true"));
   private static final int CLICK_CACHE_XY_RADIUS = 1;
   private static final int CLICK_CACHE_Y_RADIUS = 3;
   private final Long2ObjectLinkedOpenHashMap<PrintView.ClickPointsEntry> coarseClickCache = new Long2ObjectLinkedOpenHashMap<>();
   private final Long2ObjectLinkedOpenHashMap<PrintView.ClickPointsEntry> detailedClickCache = new Long2ObjectLinkedOpenHashMap<>();
   long clickCacheHits;
   long clickCacheMisses;
   private long workUnits;
   private int workAccountingPauseDepth;
   private long raycastCount;
   static final int RAYCAST_WORK_COST = 24;
   final LongOpenHashSet observedLavaCells = new LongOpenHashSet();
   private LevelReader survivalReaderProxy;
   public boolean requireDropOvershootSafety;
   static final double MIN_SUPPORT_AREA = 0.03;
   static final boolean USE_VANILLA_RAYS = Boolean.getBoolean("autism.ai.print.vanillaRays");
   private final IdentityHashMap<BlockState, List<AABB>> outlineBoxesCache = new IdentityHashMap<>();
   private final IdentityHashMap<BlockState, List<AABB>> interactionBoxesCache = new IdentityHashMap<>();

   PrintView.SnapshotScope openSnapshotScope() {
      return new PrintView.SnapshotScope();
   }

   long untrackedReadCount() {
      return this.untrackedReadCount;
   }

   Direction getRequiredInteractFace(BlockPos pos) {
      return this.requiredInteractFaces.get(pos.asLong());
   }

   public long viewRevision() {
      return this.viewRevision;
   }

   long worldMutationRevision() {
      return this.worldMutationRevision;
   }

   boolean matchesHypotheses(Long2ObjectMap<BlockState> expected) {
      return mapsEqual(this.hypotheticalBlocks, expected);
   }

   static boolean mapsEqual(Long2ObjectMap<BlockState> actual, Long2ObjectMap<BlockState> expected) {
      if (actual == expected) {
         return true;
      }

      if (actual.size() != expected.size()) {
         return false;
      }

      ObjectIterator<Entry<BlockState>> entries = Long2ObjectMaps.fastIterator(expected);

      while (entries.hasNext()) {
         Entry<BlockState> entry = entries.next();
         long key = entry.getLongKey();
         BlockState wanted = entry.getValue();
         if (!Objects.equals(actual.get(key), wanted) || wanted == null && !actual.containsKey(key)) {
            return false;
         }
      }

      return true;
   }

   boolean hasHypotheses() {
      return !this.hypotheticalBlocks.isEmpty();
   }

   boolean matchesHypothesesNear(Long2ObjectMap<BlockState> expected, BlockPos c, int radius) {
      if (this.hypotheticalBlocks.isEmpty() && expected.isEmpty()) {
         return true;
      }

      for (Entry<BlockState> e : this.hypotheticalBlocks.long2ObjectEntrySet()) {
         if (isWithinChebyshev(e.getLongKey(), c, radius) && expected.get(e.getLongKey()) != e.getValue()) {
            return false;
         }
      }

      for (Entry<BlockState> e : expected.long2ObjectEntrySet()) {
         if (isWithinChebyshev(e.getLongKey(), c, radius) && this.hypotheticalBlocks.get(e.getLongKey()) != e.getValue()) {
            return false;
         }
      }

      return true;
   }

   private static boolean isWithinChebyshev(long key, BlockPos c, int radius) {
      return Math.abs(BlockPos.getX(key) - c.getX()) <= radius
         && Math.abs(BlockPos.getY(key) - c.getY()) <= radius
         && Math.abs(BlockPos.getZ(key) - c.getZ()) <= radius;
   }

   public void copyHypothesesInto(Long2ObjectMap<BlockState> into) {
      into.putAll(this.hypotheticalBlocks);
   }

   long hashHypothesesNear(BlockPos c, int radius) {
      if (this.hypotheticalBlocks.isEmpty()) {
         return 0L;
      }

      long hash = 0L;

      for (Entry<BlockState> e : this.hypotheticalBlocks.long2ObjectEntrySet()) {
         if (isWithinChebyshev(e.getLongKey(), c, radius)) {
            long h = e.getLongKey() * -4658895280553007687L + Block.getId(e.getValue());
            hash += (h ^ h >>> 31) * -7723592293110705685L;
         }
      }

      return hash;
   }

   void copyOwnedTemporariesInto(LongSet into) {
      into.addAll(this.ownedTemporaryBlocks);
   }

   boolean matchesOwnedTemporaries(LongSet expected) {
      return this.ownedTemporaryBlocks.equals(expected);
   }

   public PrintView(PrintEnv env) {
      this(env, 1048576);
   }

   PrintView(PrintEnv env, int geometryInputBudget) {
      if (geometryInputBudget >= 0 && geometryInputBudget <= 1048576) {
         this.env = env;
         this.geometryInputBudget = geometryInputBudget;
      } else {
         throw new IllegalArgumentException("Invalid native geometry receipt budget");
      }
   }

   public void onWorldBlockRemoved(long key) {
      this.viewRevision++;
      this.worldMutationRevision++;
      this.recordLiveChange(key);
      this.invalidateAround(key);
      this.recordBucketMutation(key, false);
   }

   public void onWorldBlockAdded(long key) {
      this.viewRevision++;
      this.worldMutationRevision++;
      this.recordLiveChange(key);
      this.invalidateAround(key);
      this.recordBucketMutation(key, true);
   }

   private void recordBucketMutation(long key, boolean addition) {
      long bucket = BlockPos.asLong(BlockPos.getX(key) >> 2, BlockPos.getY(key) >> 2, BlockPos.getZ(key) >> 2);
      (addition ? this.bucketAdditionRevision : this.bucketRemovalRevision).put(bucket, this.worldMutationRevision);
      if (addition) {
         LongArrayList log = this.bucketAdditionLogs.computeIfAbsent(bucket, k -> new LongArrayList());
         log.add(this.worldMutationRevision);
         log.add(key);
         if (log.size() > 128) {
            this.bucketOverflowRevision.put(bucket, Math.max(this.bucketOverflowRevision.get(bucket), log.getLong(0)));
            log.removeElements(0, 2);
         }
      }
   }

   public void onLiveStateChanged(long key) {
      this.recordLiveChange(key);
      this.snapshotStateCache.remove(key);
   }

   private void recordLiveChange(long key) {
      this.liveChangeRevision++;
      this.appendChangeRing(key);
   }

   void appendChangeRing(long key) {
      this.ringChangeRevision++;
      this.recentChangedRing[this.recentChangedHead] = key;
      this.recentChangedHead = (this.recentChangedHead + 1) % this.recentChangedRing.length;
      this.recentChangedCount = Math.min(this.recentChangedCount + 1, this.recentChangedRing.length);
   }

   long liveChangeRevision() {
      return this.liveChangeRevision;
   }

   long ringChangeRevision() {
      return this.ringChangeRevision;
   }

   boolean areInputsStillValid(long since, Long2ObjectMap<BlockState> inputs) {
      long changes = this.ringChangeRevision - since;
      if (since >= 0L && changes >= 0L) {
         if (since >= this.ringResetRevision && changes <= this.recentChangedCount) {
            int cursor = this.recentChangedHead;

            for (long changed = 0L; changed < changes; changed++) {
               cursor = (cursor + this.recentChangedRing.length - 1) % this.recentChangedRing.length;
               long key = this.recentChangedRing[cursor];
               BlockState wanted = inputs.get(key);
               if (wanted != null && this.getBlockState(BlockPos.of(key)) != wanted) {
                  return false;
               }
            }

            return true;
         } else {
            for (Entry<BlockState> entry : inputs.long2ObjectEntrySet()) {
               if (this.getBlockState(BlockPos.of(entry.getLongKey())) != entry.getValue()) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public void invalidateAround(long key) {
      this.snapshotStateCache.remove(key);
      int x = BlockPos.getX(key);
      int y = BlockPos.getY(key);
      int z = BlockPos.getZ(key);

      for (int dy = -3; dy <= 2; dy++) {
         this.evictStandableEntry(BlockPos.asLong(x, y + dy, z));
      }

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = -4; dy <= 1; dy++) {
               this.evictStandableEntry(BlockPos.asLong(x + dx, y + dy, z + dz));
            }
         }
      }

      if (!this.coarseClickCache.isEmpty() || !this.detailedClickCache.isEmpty()) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               for (int dy = -3; dy <= 3; dy++) {
                  long s = BlockPos.asLong(x + dx, y + dy, z + dz);
                  this.evictClickEntry(this.coarseClickCache, s);
                  this.evictClickEntry(this.detailedClickCache, s);
               }
            }
         }
      }
   }

   public void clearGeometryCaches() {
      this.standableHeightCache.clear();
      this.coarseClickCache.clear();
      this.detailedClickCache.clear();
      this.retainedReceiptCells = 0;
   }

   void resetAllCaches() {
      this.snapshotStateCache.clear();
      this.viewRevision++;
      this.worldMutationRevision++;
      this.liveChangeRevision++;
      this.ringResetRevision = ++this.ringChangeRevision;
      this.recentChangedHead = this.recentChangedCount = 0;
      this.clearGeometryCaches();
      this.diagonalJumpProofCache.clear();
      this.bucketRemovalRevision.clear();
      this.bucketAdditionRevision.clear();
      this.bucketAdditionLogs.clear();
      this.bucketOverflowRevision.clear();
      this.bucketRemovalRevision.defaultReturnValue(this.worldMutationRevision);
   }

   @Nullable List<Vec3> getCachedClickPoints(long key, double feetY, boolean detailed) {
      int x = BlockPos.getX(key);
      int y = BlockPos.getY(key);
      int z = BlockPos.getZ(key);
      if (!this.hypotheticalBlocks.isEmpty() && this.hasHypothesisNearClickCell(x, y, z)) {
         return null;
      }

      Long2ObjectLinkedOpenHashMap<PrintView.ClickPointsEntry> map = detailed ? this.detailedClickCache : this.coarseClickCache;
      PrintView.ClickPointsEntry entry = map.getAndMoveToLast(key);
      boolean needsInputs = this.needsGeometryInputTracking();
      if (entry != null && entry.feetY() == feetY && entry.points() != null) {
         if (entry.inputs() == null && !needsInputs || entry.inputs() != null && this.validateAndForwardReceipt(entry.inputs())) {
            this.clickCacheHits++;
            return entry.points();
         }

         if (entry.inputs() != null) {
            return null;
         }
      }

      if (entry != null && entry.feetY() == feetY) {
         if (!needsInputs) {
            long before = this.untrackedReadCount;
            List<Vec3> all = PrintClicks.computeCandidateFeetPositions(this, key, feetY, detailed);
            this.clickCacheMisses++;
            if (this.untrackedReadCount == before) {
               this.putClickCacheEntry(map, key, feetY, all, null);
            } else {
               this.evictClickEntry(map, key);
            }

            return all;
         } else {
            try (PrintView.NativeReadTracker reads = new PrintView.NativeReadTracker()) {
               List<Vec3> all = PrintClicks.computeCandidateFeetPositions(this, key, feetY, detailed);
               this.clickCacheMisses++;
               if (reads.isPure()) {
                  this.putClickCacheEntry(map, key, feetY, all, this.buildReceiptFromTracker(reads));
               } else if (reads.startUntrackedCount != this.untrackedReadCount) {
                  this.evictClickEntry(map, key);
               }

               return all;
            }
         }
      } else {
         if (entry == null && map.size() >= 8192) {
            this.evictClickEntry(map, map.firstLongKey());
         }

         this.evictClickEntry(map, key);
         map.putAndMoveToLast(key, new PrintView.ClickPointsEntry(feetY, null, null));
         return null;
      }
   }

   private boolean needsGeometryInputTracking() {
      return !this.hypotheticalBlocks.isEmpty() || !this.activeReadTrackers.isEmpty() || !this.activeNativeTrackers.isEmpty();
   }

   private void putClickCacheEntry(
      Long2ObjectLinkedOpenHashMap<PrintView.ClickPointsEntry> map, long key, double feetY, List<Vec3> points, PrintView.@Nullable ReadReceipt inputs
   ) {
      this.evictClickEntry(map, key);
      if (inputs != null && !this.ensureReceiptBudget(inputs.cells().length)) {
         inputs = null;
      }

      map.putAndMoveToLast(key, new PrintView.ClickPointsEntry(feetY, points, inputs));
      if (inputs != null) {
         this.retainedReceiptCells = this.retainedReceiptCells + inputs.cells().length;
      }
   }

   private void putStandableCacheEntry(long key, double height, PrintView.@Nullable ReadReceipt inputs) {
      this.evictStandableEntry(key);
      if (this.standableHeightCache.size() >= 65536) {
         this.evictStandableEntry(this.standableHeightCache.firstLongKey());
      }

      if (inputs != null && !this.ensureReceiptBudget(inputs.cells().length)) {
         inputs = null;
      }

      this.standableHeightCache.putAndMoveToLast(key, new PrintView.StandableHeightEntry(height, inputs));
      if (inputs != null) {
         this.retainedReceiptCells = this.retainedReceiptCells + inputs.cells().length;
      }
   }

   private void evictStandableEntry(long key) {
      PrintView.StandableHeightEntry removed = this.standableHeightCache.remove(key);
      if (removed != null && removed.inputs() != null) {
         this.retainedReceiptCells = this.retainedReceiptCells - removed.inputs().cells().length;
      }
   }

   private void evictClickEntry(Long2ObjectLinkedOpenHashMap<PrintView.ClickPointsEntry> map, long key) {
      PrintView.ClickPointsEntry removed = map.remove(key);
      if (removed != null && removed.inputs() != null) {
         this.retainedReceiptCells = this.retainedReceiptCells - removed.inputs().cells().length;
      }
   }

   private boolean ensureReceiptBudget(int count) {
      if (count > this.geometryInputBudget) {
         return false;
      }

      for (int evictions = 0; this.retainedReceiptCells + count > this.geometryInputBudget && evictions < 32; evictions++) {
         if (!this.standableHeightCache.isEmpty()) {
            this.evictStandableEntry(this.standableHeightCache.firstLongKey());
         } else if (this.detailedClickCache.isEmpty() || !this.coarseClickCache.isEmpty() && (evictions & 1) != 0) {
            if (this.coarseClickCache.isEmpty()) {
               return false;
            }

            this.evictClickEntry(this.coarseClickCache, this.coarseClickCache.firstLongKey());
         } else {
            this.evictClickEntry(this.detailedClickCache, this.detailedClickCache.firstLongKey());
         }
      }

      return this.retainedReceiptCells + count <= this.geometryInputBudget;
   }

   private PrintView.@Nullable ReadReceipt buildReceiptFromTracker(PrintView.NativeReadTracker reads) {
      if (!reads.canBuildReceipt()) {
         return null;
      }

      long[] cells = reads.readCells.toLongArray();
      Arrays.sort(cells);
      return new PrintView.ReadReceipt(cells);
   }

   private boolean receiptOverlapsHypotheses(PrintView.ReadReceipt inputs) {
      if (this.hypotheticalBlocks.isEmpty()) {
         return false;
      }

      if (this.hypotheticalBlocks.size() < inputs.cells().length) {
         for (long key : this.hypotheticalBlocks.keySet()) {
            if (Arrays.binarySearch(inputs.cells(), key) >= 0) {
               return true;
            }
         }
      } else {
         for (long key : inputs.cells()) {
            if (this.hypotheticalBlocks.containsKey(key)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean validateAndForwardReceipt(PrintView.@Nullable ReadReceipt inputs) {
      if (inputs == null) {
         return false;
      }

      if (this.receiptOverlapsHypotheses(inputs)) {
         return false;
      }

      if (this.activeReadTrackers.isEmpty() && this.activeNativeTrackers.isEmpty()) {
         return true;
      }

      boolean forwarding = false;

      for (int i = 0; i < this.activeReadTrackers.size(); i++) {
         if (!this.activeReadTrackers.get(i).hasForwarded(inputs)) {
            forwarding = true;
            break;
         }
      }

      if (!forwarding) {
         for (int i = 0; i < this.activeNativeTrackers.size(); i++) {
            if (!this.activeNativeTrackers.get(i).hasForwarded(inputs)) {
               forwarding = true;
               break;
            }
         }
      }

      if (!forwarding) {
         return true;
      }

      for (long key : inputs.cells()) {
         boolean needed = false;

         for (int i = 0; i < this.activeReadTrackers.size(); i++) {
            PrintView.ReadTracker reads = this.activeReadTrackers.get(i);
            if (!reads.hasForwarded(inputs) && !reads.recordedStates.containsKey(key)) {
               needed = true;
               break;
            }
         }

         if (!needed) {
            for (int i = 0; i < this.activeNativeTrackers.size(); i++) {
               PrintView.NativeReadTracker reads = this.activeNativeTrackers.get(i);
               if (!reads.hasForwarded(inputs) && !reads.readCells.contains(key)) {
                  needed = true;
                  break;
               }
            }
         }

         if (needed) {
            BlockPos pos = BlockPos.of(key);
            if (this.workAccountingPauseDepth == 0) {
               this.workUnits++;
            }

            BlockState live = this.getLiveBlockState(pos);
            if (live == UNLOADED_SENTINEL) {
               return false;
            }

            for (int i = 0; i < this.activeReadTrackers.size(); i++) {
               PrintView.ReadTracker reads = this.activeReadTrackers.get(i);
               if (!reads.hasForwarded(inputs)) {
                  reads.recordPosWithLive(pos, live);
               }
            }

            for (int i = 0; i < this.activeNativeTrackers.size(); i++) {
               PrintView.NativeReadTracker reads = this.activeNativeTrackers.get(i);
               if (!reads.hasForwarded(inputs)) {
                  reads.recordRead(key);
               }
            }
         }
      }

      for (int i = 0; i < this.activeReadTrackers.size(); i++) {
         this.activeReadTrackers.get(i).markForwarded(inputs);
      }

      for (int i = 0; i < this.activeNativeTrackers.size(); i++) {
         this.activeNativeTrackers.get(i).markForwarded(inputs);
      }

      return true;
   }

   private boolean hasHypothesisNearClickCell(int x, int y, int z) {
      if (this.hypotheticalBlocks.size() <= 32) {
         for (long k : this.hypotheticalBlocks.keySet()) {
            int dx = BlockPos.getX(k) - x;
            int dy = BlockPos.getY(k) - y;
            int dz = BlockPos.getZ(k) - z;
            if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1 && Math.abs(dy) <= 3) {
               return true;
            }
         }

         return false;
      } else {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               for (int dy = -3; dy <= 3; dy++) {
                  if (this.hypotheticalBlocks.containsKey(BlockPos.asLong(x + dx, y + dy, z + dz))) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   public long workUnits() {
      return this.workUnits;
   }

   public long raycastCount() {
      return this.raycastCount;
   }

   public void pauseWorkAccounting() {
      this.workAccountingPauseDepth++;
   }

   public void resumeWorkAccounting() {
      this.workAccountingPauseDepth--;
   }

   @Override
   public BlockState getBlockState(BlockPos pos) {
      if (this.workAccountingPauseDepth == 0) {
         this.workUnits++;
      }

      for (int i = 0; i < this.activeReadTrackers.size(); i++) {
         this.activeReadTrackers.get(i).recordPos(pos);
      }

      for (int i = 0; i < this.activeNativeTrackers.size(); i++) {
         this.activeNativeTrackers.get(i).recordRead(pos.asLong());
      }

      if (!this.hypotheticalBlocks.isEmpty()) {
         BlockState assumed = this.hypotheticalBlocks.get(pos.asLong());
         if (assumed != null) {
            return assumed;
         }
      }

      return this.getLiveBlockState(pos);
   }

   private BlockState getLiveBlockState(BlockPos pos) {
      if (pos.getY() < this.env.getMinY() || pos.getY() >= this.env.getMaxY()) {
         return pos.getY() < this.env.getMinY() ? UNLOADED_SENTINEL : Blocks.AIR.defaultBlockState();
      }

      if (!this.env.hasChunkAt(pos)) {
         this.snapshotStateCache.remove(pos.asLong());
         this.untrackedReadCount++;
         return UNLOADED_SENTINEL;
      }

      if (!this.snapshotCacheActive) {
         return this.trackLavaObservation(pos, this.env.getBlockState(pos));
      }

      long key = pos.asLong();
      BlockState state = this.snapshotStateCache.get(key);
      if (state == null) {
         state = this.trackLavaObservation(pos, this.env.getBlockState(pos));
         if (this.snapshotStateCache.size() >= 32768) {
            this.snapshotStateCache.clear();
         }

         this.snapshotStateCache.put(key, state);
      }

      return state;
   }

   private BlockState trackLavaObservation(BlockPos pos, BlockState state) {
      if (state.getFluidState().getType().isSame(Fluids.LAVA)) {
         this.observedLavaCells.add(pos.asLong());
      } else if (!this.observedLavaCells.isEmpty()) {
         this.observedLavaCells.remove(pos.asLong());
      }

      return state;
   }

   PrintView.ReadTracker openReadTracker() {
      PrintView.ReadTracker reads = new PrintView.ReadTracker(false);
      this.activeReadTrackers.add(reads);
      return reads;
   }

   PrintView.ReadTracker createRetainedReadTracker() {
      return new PrintView.ReadTracker(true);
   }

   void replayTrackedReads(PrintView.ReadTracker inputs) {
      if (!this.activeReadTrackers.isEmpty() || !this.activeNativeTrackers.isEmpty()) {
         for (long key : inputs.recordedStates.keySet()) {
            this.getBlockState(BlockPos.of(key));
         }
      }
   }

   private void recordDynamicShapeRead(BlockState state, BlockPos pos) {
      if (!this.suppressDynamicShapeTracking && !this.activeDynamicShapeTrackers.isEmpty() && state != UNLOADED_SENTINEL) {
         Block block = state.getBlock();
         if ((block instanceof MovingPistonBlock || block instanceof ShulkerBoxBlock)
            && (!this.hasHypothesisAt(pos) || state == this.env.getBlockState(pos))) {
            for (int i = 0; i < this.activeDynamicShapeTrackers.size(); i++) {
               this.activeDynamicShapeTrackers.get(i).pendingDynamicCells.add(pos.asLong());
            }
         }
      }
   }

   public BlockState getBlockStateAt(int x, int y, int z) {
      return this.getBlockState(this.scratchPos.set(x, y, z));
   }

   @Override
   public FluidState getFluidState(BlockPos pos) {
      return this.getBlockState(pos).getFluidState();
   }

   @Override
   public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
      this.untrackedReadCount++;
      if (!this.env.hasChunkAt(pos)) {
         return null;
      }

      BlockState assumed = this.hypotheticalBlocks.get(pos.asLong());
      if (assumed != null && assumed != this.env.getBlockState(pos)) {
         return null;
      }

      this.recordDynamicShapeRead(assumed == null ? this.env.getBlockState(pos) : assumed, pos);
      return this.env.getBlockEntity(pos);
   }

   @Override
   public int getHeight() {
      return this.env.getMaxY() - this.env.getMinY();
   }

   @Override
   public int getMinY() {
      return this.env.getMinY();
   }

   private BlockState requireKnownBlockState(BlockPos pos) {
      BlockState state = this.getBlockState(pos);
      if (state == UNLOADED_SENTINEL) {
         throw new UnsupportedOperationException("Unknown hypothetical support");
      } else {
         return state;
      }
   }

   boolean canHypotheticallySurvive(BlockState state, BlockPos pos) {
      if (this.survivalReaderProxy == null) {
         this.survivalReaderProxy = (LevelReader)Proxy.newProxyInstance(
            LevelReader.class.getClassLoader(),
            new Class[]{LevelReader.class},
            (proxy, method, args) -> {
               Object var10000;
               switch (method.getName()) {
                  case "getBlockState":
                     var10000 = this.requireKnownBlockState((BlockPos)args[0]);
                     break;
                  case "getFluidState":
                     var10000 = this.requireKnownBlockState((BlockPos)args[0]).getFluidState();
                     break;
                  case "getBlockEntity":
                     if (method.getParameterCount() != 1) {
                        var10000 = InvocationHandler.invokeDefault(proxy, method, args);
                     } else {
                        this.requireKnownBlockState((BlockPos)args[0]);
                        var10000 = this.getBlockEntity((BlockPos)args[0]);
                     }
                     break;
                  case "isEmptyBlock":
                     var10000 = this.requireKnownBlockState((BlockPos)args[0]).isAir();
                     break;
                  case "hasChunkAt":
                     var10000 = args.length == 1
                        ? this.env.hasChunkAt((BlockPos)args[0])
                        : this.env.hasChunkAt(new BlockPos((Integer)args[0], this.env.getMinY(), (Integer)args[1]));
                     break;
                  case "isUnobstructed":
                     throw new UnsupportedOperationException("No hypothetical entity clearance");
                  case "getMinY":
                     var10000 = this.getMinY();
                     break;
                  case "getHeight":
                     if (args != null && args.length != 0) {
                        throw new UnsupportedOperationException("No hypothetical heightmap");
                     }

                     var10000 = this.getHeight();
                     break;
                  case "toString":
                     var10000 = "PrintView.survivalReader";
                     break;
                  case "hashCode":
                     var10000 = System.identityHashCode(proxy);
                     break;
                  case "equals":
                     var10000 = proxy == args[0];
                     break;
                  default:
                     if (!method.isDefault()) {
                        throw new UnsupportedOperationException("No hypothetical " + method.getName());
                     }

                     var10000 = InvocationHandler.invokeDefault(proxy, method, args);
               }

               return var10000;
            }
         );
      }

      try {
         return state.canSurvive(this.survivalReaderProxy, pos);
      } catch (UnsupportedOperationException unavailable) {
         return false;
      }
   }

   public void pushHypothesis(BlockPos pos, BlockState state) {
      this.pushHypothesisInternal(pos, state, false);
   }

   public void pushTemporaryHypothesis(BlockPos pos, BlockState state) {
      this.pushHypothesisInternal(pos, state, true);
   }

   public boolean hasHypothesisAt(BlockPos pos) {
      return this.hypotheticalBlocks.containsKey(pos.asLong());
   }

   public boolean isOwnedTemporaryAt(BlockPos pos) {
      return this.ownedTemporaryBlocks.contains(pos.asLong());
   }

   private void pushHypothesisInternal(BlockPos pos, BlockState state, boolean ownedTemporary) {
      this.viewRevision++;
      long key = pos.asLong();
      BlockState old = this.hypotheticalBlocks.put(key, state);
      if (old == null) {
         this.updateHypothesisColumnMask(key, true);
      }

      if (old != null) {
         this.hypotheticalUndoStacks
            .computeIfAbsent(key, ignored -> new ArrayDeque<>())
            .push(new PrintView.HypothesisUndo(old, this.ownedTemporaryBlocks.contains(key)));
      }

      if (ownedTemporary) {
         this.ownedTemporaryBlocks.add(key);
      } else {
         this.ownedTemporaryBlocks.remove(key);
      }
   }

   public boolean hasNoHypothesisInNeighborhood(BlockPos pos) {
      if (this.hypotheticalBlocks.isEmpty()) {
         return true;
      }

      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (this.hypotheticalBlocks.containsKey(BlockPos.asLong(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz))) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   public void popHypothesis(BlockPos pos) {
      this.viewRevision++;
      long key = pos.asLong();
      ArrayDeque<PrintView.HypothesisUndo> stack = this.hypotheticalUndoStacks.get(key);
      if (stack == null) {
         if (this.hypotheticalBlocks.remove(key) != null) {
            this.updateHypothesisColumnMask(key, false);
         }

         this.ownedTemporaryBlocks.remove(key);
      } else {
         PrintView.HypothesisUndo old = stack.pop();
         this.hypotheticalBlocks.put(key, old.state());
         if (old.temporary()) {
            this.ownedTemporaryBlocks.add(key);
         } else {
            this.ownedTemporaryBlocks.remove(key);
         }

         if (stack.isEmpty()) {
            this.hypotheticalUndoStacks.remove(key);
         }
      }
   }

   public void clearHypotheses() {
      if (!this.hypotheticalBlocks.isEmpty()) {
         this.viewRevision++;
      }

      this.hypotheticalBlocks.clear();
      this.hypotheticalColumnMasks.clear();
      this.hypotheticalUndoStacks.clear();
      this.ownedTemporaryBlocks.clear();
   }

   private void updateHypothesisColumnMask(long key, boolean present) {
      int y = BlockPos.getY(key);
      long column = BlockPos.asLong(BlockPos.getX(key), y >> 6, BlockPos.getZ(key));
      long bit = 1L << y;
      long before = this.hypotheticalColumnMasks.get(column);
      long after = present ? before | bit : before & ~bit;
      if (after == 0L) {
         this.hypotheticalColumnMasks.remove(column);
      } else {
         this.hypotheticalColumnMasks.put(column, after);
      }
   }

   public int hypothesisCount() {
      return this.hypotheticalBlocks.size();
   }

   public boolean hasAnyHypothesis() {
      return !this.hypotheticalBlocks.isEmpty();
   }

   public VoxelShape getCollisionShape(BlockPos pos) {
      BlockState state = this.getBlockState(pos);
      this.recordDynamicShapeRead(state, pos);
      return state.getCollisionShape(this, pos);
   }

   List<AABB> getCollisionBoxesForState(BlockState state, BlockPos pos) {
      if (state.getBlock().hasDynamicShape()) {
         this.untrackedReadCount++;
         this.recordDynamicShapeRead(state, pos);
         return state.getCollisionShape(this, pos).toAabbs();
      } else {
         return this.collisionBoxesCache.computeIfAbsent(state, key -> List.copyOf(key.getCollisionShape(this, pos).toAabbs()));
      }
   }

   List<AABB> getCollisionBoxesAt(BlockPos pos) {
      return this.getCollisionBoxesForState(this.getBlockState(pos), pos);
   }

   public VoxelShape getOutlineShape(BlockPos pos) {
      return this.getBlockState(pos).getShape(this, pos);
   }

   public boolean isBoxClear(AABB box) {
      AABB inset = box.deflate(1.0E-6);
      int x0 = floorInt(box.minX);
      int x1 = floorInt(box.maxX - 1.0E-7);
      int y0 = floorInt(box.minY);
      int y1 = floorInt(box.maxY - 1.0E-7);
      int z0 = floorInt(box.minZ);
      int z1 = floorInt(box.maxZ - 1.0E-7);
      MutableBlockPos p = new MutableBlockPos();

      for (int x = x0; x <= x1; x++) {
         for (int y = y0 - 1; y <= y1; y++) {
            for (int z = z0; z <= z1; z++) {
               p.set(x, y, z);
               BlockState state = this.getBlockState(p);
               if (!state.isAir()) {
                  for (AABB b : this.getCollisionBoxesForState(state, p)) {
                     if (b.move(x, y, z).intersects(inset)) {
                        return false;
                     }
                  }
               }
            }
         }
      }

      return true;
   }

   public List<AABB> collectCollisions(AABB query) {
      List<AABB> out = new ArrayList<>();
      int x0 = floorInt(query.minX);
      int x1 = floorInt(query.maxX);
      int y0 = floorInt(query.minY) - 1;
      int y1 = floorInt(query.maxY);
      int z0 = floorInt(query.minZ);
      int z1 = floorInt(query.maxZ);
      MutableBlockPos p = new MutableBlockPos();

      for (int x = x0; x <= x1; x++) {
         for (int y = y0; y <= y1; y++) {
            for (int z = z0; z <= z1; z++) {
               p.set(x, y, z);
               BlockState state = this.getBlockState(p);
               if (!state.isAir()) {
                  for (AABB b : this.getCollisionBoxesForState(state, p)) {
                     AABB moved = b.move(x, y, z);
                     if (moved.intersects(query)) {
                        out.add(moved);
                     }
                  }
               }
            }
         }
      }

      return out;
   }

   public boolean intersectsHazard(AABB box) {
      AABB b = box.deflate(0.001);

      for (BlockPos q : BlockPos.betweenClosed(BlockPos.containing(b.minX, b.minY, b.minZ), BlockPos.containing(b.maxX, b.maxY, b.maxZ))) {
         BlockState s = this.getBlockState(q);
         if (!s.isAir()) {
            FluidState fluid = s.getFluidState();
            if (fluid.getType().isSame(Fluids.LAVA)) {
               if (b.minY < q.getY() + fluid.getHeight(this, q)) {
                  return true;
               }
            } else if (isHazardBlock(s)) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isHazardBlock(BlockState state) {
      if (state.isAir()) {
         return false;
      }

      if (state.getFluidState().getType().isSame(Fluids.LAVA)) {
         return true;
      }

      Block b = state.getBlock();
      return b instanceof BaseFireBlock
         || b instanceof CactusBlock
         || b instanceof SweetBerryBushBlock
         || b instanceof WebBlock
         || b instanceof PowderSnowBlock
         || b instanceof MagmaBlock
         || b instanceof CampfireBlock
         || b instanceof WitherRoseBlock
         || b instanceof PointedDripstoneBlock;
   }

   public double standingHeightAt(int x, int y, int z) {
      if (!this.hypotheticalBlocks.isEmpty() && this.hasHypothesisNearStandCell(x, y, z)) {
         return this.computeStandingHeight(x, y, z);
      }

      long key = BlockPos.asLong(x, y, z);
      PrintView.StandableHeightEntry cached = this.standableHeightCache.getAndMoveToLast(key);
      boolean needsInputs = this.needsGeometryInputTracking();
      if (cached != null) {
         if (cached.inputs() == null && !needsInputs || cached.inputs() != null && this.validateAndForwardReceipt(cached.inputs())) {
            this.standableCacheHits++;
            return cached.height();
         }

         if (cached.inputs() != null) {
            return this.computeStandingHeight(x, y, z);
         }
      }

      if (!needsInputs) {
         long before = this.untrackedReadCount;
         double height = this.computeStandingHeight(x, y, z);
         this.standableCacheMisses++;
         if (this.untrackedReadCount == before) {
            this.putStandableCacheEntry(key, height, null);
         }

         return height;
      } else {
         try (PrintView.NativeReadTracker reads = new PrintView.NativeReadTracker()) {
            double h = this.computeStandingHeight(x, y, z);
            this.standableCacheMisses++;
            if (reads.isPure()) {
               this.putStandableCacheEntry(key, h, this.buildReceiptFromTracker(reads));
            } else if (reads.startUntrackedCount != this.untrackedReadCount) {
               this.evictStandableEntry(key);
            }

            return h;
         }
      }
   }

   private boolean hasHypothesisNearStandCell(int x, int y, int z) {
      if (this.hypotheticalBlocks.size() <= 2) {
         for (long k : this.hypotheticalBlocks.keySet()) {
            int dx = BlockPos.getX(k) - x;
            int dy = BlockPos.getY(k) - y;
            int dz = BlockPos.getZ(k) - z;
            if (dx >= -1 && dx <= 1 && dz >= -1 && dz <= 1 && dy >= -2 && dy <= 4) {
               return true;
            }
         }

         return false;
      } else {
         int low = y - 2;
         int bit = low & 63;
         int count = Math.min(7, 64 - bit);
         long lowMask = -1L >>> 64 - count << bit;
         long highMask = count == 7 ? 0L : (1L << 7 - count) - 1L;
         int lowSection = BlockPos.getY(BlockPos.asLong(0, low, 0)) >> 6;
         int highSection = BlockPos.getY(BlockPos.asLong(0, low + count, 0)) >> 6;

         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               if ((this.hypotheticalColumnMasks.get(BlockPos.asLong(x + dx, lowSection, z + dz)) & lowMask) != 0L) {
                  return true;
               }

               if (highMask != 0L && (this.hypotheticalColumnMasks.get(BlockPos.asLong(x + dx, highSection, z + dz)) & highMask) != 0L) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private double computeStandingHeight(int x, int y, int z) {
      MutableBlockPos p = new MutableBlockPos();
      double cx = x + 0.5;
      double cz = z + 0.5;
      double top = -1.0 / 0.0;
      double area = 0.0;

      for (int dy = -1; dy <= 0; dy++) {
         p.set(x, y + dy, z);
         BlockState state = this.getBlockState(p);
         if (!state.isAir()) {
            if (state == UNLOADED_SENTINEL) {
               return 0.0 / 0.0;
            }

            if (isHazardBlock(state)) {
               return 0.0 / 0.0;
            }

            for (AABB b : this.getCollisionBoxesForState(state, p)) {
               if (!(b.maxX + x <= cx - 0.3) && !(b.minX + x >= cx + 0.3) && !(b.maxZ + z <= cz - 0.3) && !(b.minZ + z >= cz + 0.3)) {
                  double bt = b.maxY + y + dy;
                  double ox = Math.min(b.maxX + x, cx + 0.3) - Math.max(b.minX + x, cx - 0.3);
                  double oz = Math.min(b.maxZ + z, cz + 0.3) - Math.max(b.minZ + z, cz - 0.3);
                  if (bt > top + 1.0E-6) {
                     top = bt;
                     area = ox * oz;
                  } else if (Math.abs(bt - top) <= 1.0E-6) {
                     area += ox * oz;
                  }
               }
            }
         }
      }

      if (top < y || top >= y + 1) {
         return 0.0 / 0.0;
      }

      if (area < 0.03) {
         return 0.0 / 0.0;
      }

      if (!this.isBoxClear(PrintGeometry.init(cx, top, cz, false)) && !this.isBoxClear(PrintGeometry.init(cx, top, cz, true))) {
         return 0.0 / 0.0;
      }

      for (int dy = 0; dy <= 2; dy++) {
         p.set(x, y + dy, z);
         BlockState state = this.getBlockState(p);
         if (isHazardBlock(state) || !state.getFluidState().isEmpty() || state == UNLOADED_SENTINEL) {
            return 0.0 / 0.0;
         }
      }

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = 0; dy <= 3; dy++) {
               p.set(x + dx, y + dy, z + dz);
               if (this.getBlockState(p).getFluidState().getType().isSame(Fluids.LAVA)) {
                  return 0.0 / 0.0;
               }
            }
         }
      }

      return top;
   }

   public boolean isStandable(int x, int y, int z) {
      return !Double.isNaN(this.standingHeightAt(x, y, z));
   }

   public double supportAreaAtCell(int x, int z, double feet) {
      MutableBlockPos p = new MutableBlockPos();
      double cx = x + 0.5;
      double cz = z + 0.5;
      double area = 0.0;
      int cy = (int)Math.floor(feet - 1.0E-6);

      for (int y = cy - 1; y <= cy; y++) {
         p.set(x, y, z);
         BlockState state = this.getBlockState(p);
         if (!state.isAir() && state != UNLOADED_SENTINEL) {
            for (AABB b : this.getCollisionBoxesForState(state, p)) {
               double top = b.maxY + y;
               if (!(top > feet + 1.0E-6) && !(top < feet - 0.6)) {
                  double ox = Math.min(b.maxX + x, cx + 0.3) - Math.max(b.minX + x, cx - 0.3);
                  double oz = Math.min(b.maxZ + z, cz + 0.3) - Math.max(b.minZ + z, cz - 0.3);
                  if (ox > 0.0 && oz > 0.0) {
                     area += ox * oz;
                  }
               }
            }
         }
      }

      return area;
   }

   double supportAreaAtPoint(double cx, double cz, double feet) {
      double area = 0.0;
      int cy = floorInt(feet - 1.0E-6);

      for (int x = floorInt(cx - 0.3); x <= floorInt(cx + 0.3); x++) {
         for (int z = floorInt(cz - 0.3); z <= floorInt(cz + 0.3); z++) {
            for (int y = cy - 1; y <= cy; y++) {
               BlockPos p = new BlockPos(x, y, z);
               BlockState state = this.getBlockState(p);
               if (!state.isAir() && state != UNLOADED_SENTINEL) {
                  for (AABB b : this.getCollisionBoxesForState(state, p)) {
                     double top = b.maxY + y;
                     if (!(Math.abs(top - feet) > 1.0E-6)) {
                        double ox = Math.min(b.maxX + x, cx + 0.3) - Math.max(b.minX + x, cx - 0.3);
                        double oz = Math.min(b.maxZ + z, cz + 0.3) - Math.max(b.minZ + z, cz - 0.3);
                        if (ox > 0.0 && oz > 0.0) {
                           area += ox * oz;
                        }
                     }
                  }
               }
            }
         }
      }

      return area;
   }

   public boolean isFullSolidBlock(BlockPos pos) {
      BlockState state = this.getBlockState(pos);
      return state != UNLOADED_SENTINEL && state.isCollisionShapeFullBlock(this, pos);
   }

   public boolean isPassableCell(BlockPos pos) {
      BlockState state = this.getBlockState(pos);
      if (state == UNLOADED_SENTINEL) {
         return false;
      } else {
         return state.isAir() ? true : state.getCollisionShape(this, pos).isEmpty() && state.getFluidState().isEmpty() && !isHazardBlock(state);
      }
   }

   public boolean isEmptyOutlineCell(BlockPos pos) {
      if (!this.isPassableCell(pos)) {
         return false;
      }

      BlockState state = this.getBlockState(pos);
      return state.isAir() || state.getShape(this, pos).isEmpty();
   }

   public @Nullable BlockHitResult raycastBlock(Vec3 eye, Vec3 to) {
      if (this.workAccountingPauseDepth == 0) {
         this.raycastCount++;
         this.workUnits += 24L;
      }

      return USE_VANILLA_RAYS ? this.raycastVanilla(eye, to) : this.raycastFastDda(eye, to);
   }

   @Nullable BlockHitResult raycastVanilla(Vec3 eye, Vec3 to) {
      BlockHitResult hit = this.clip(new ClipContext(eye, to, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, CollisionContext.empty()));
      return hit.getType() == Type.MISS ? null : hit;
   }

   @Nullable BlockHitResult raycastFastDda(Vec3 from, Vec3 to) {
      if (from.equals(to)) {
         return null;
      }

      double toX = Mth.lerp(-1.0E-7, to.x, from.x);
      double toY = Mth.lerp(-1.0E-7, to.y, from.y);
      double toZ = Mth.lerp(-1.0E-7, to.z, from.z);
      double fromX = Mth.lerp(-1.0E-7, from.x, to.x);
      double fromY = Mth.lerp(-1.0E-7, from.y, to.y);
      double fromZ = Mth.lerp(-1.0E-7, from.z, to.z);
      int x = Mth.floor(fromX);
      int y = Mth.floor(fromY);
      int z = Mth.floor(fromZ);
      MutableBlockPos pos = new MutableBlockPos(x, y, z);
      BlockHitResult first = this.clipCell(from, to, pos);
      if (first != null) {
         return first;
      }

      double dx = toX - fromX;
      double dy = toY - fromY;
      double dz = toZ - fromZ;
      int signX = Mth.sign(dx);
      int signY = Mth.sign(dy);
      int signZ = Mth.sign(dz);
      double tDeltaX = signX == 0 ? 1.7976931348623157E308 : signX / dx;
      double tDeltaY = signY == 0 ? 1.7976931348623157E308 : signY / dy;
      double tDeltaZ = signZ == 0 ? 1.7976931348623157E308 : signZ / dz;
      double tX = tDeltaX * (signX > 0 ? 1.0 - Mth.frac(fromX) : Mth.frac(fromX));
      double tY = tDeltaY * (signY > 0 ? 1.0 - Mth.frac(fromY) : Mth.frac(fromY));
      double tZ = tDeltaZ * (signZ > 0 ? 1.0 - Mth.frac(fromZ) : Mth.frac(fromZ));

      while (tX <= 1.0 || tY <= 1.0 || tZ <= 1.0) {
         if (tX < tY) {
            if (tX < tZ) {
               x += signX;
               tX += tDeltaX;
            } else {
               z += signZ;
               tZ += tDeltaZ;
            }
         } else if (tY < tZ) {
            y += signY;
            tY += tDeltaY;
         } else {
            z += signZ;
            tZ += tDeltaZ;
         }

         BlockHitResult hit = this.clipCell(from, to, pos.set(x, y, z));
         if (hit != null) {
            return hit;
         }
      }

      return null;
   }

   public List<AABB> getOutlineBoxes(BlockState state, BlockPos pos) {
      if (hasDynamicOutlineShape(state)) {
         return state.getShape(this, pos).toAabbs();
      }

      List<AABB> boxes = this.outlineBoxesCache.get(state);
      if (boxes == null) {
         boxes = state.getShape(this, pos).toAabbs();
         this.outlineBoxesCache.put(state, boxes);
      }

      return boxes;
   }

   private static boolean hasDynamicOutlineShape(BlockState state) {
      Block block = state.getBlock();
      return state.hasOffsetFunction() || block instanceof MovingPistonBlock || block instanceof ShulkerBoxBlock;
   }

   private @Nullable BlockHitResult clipCell(Vec3 from, Vec3 to, MutableBlockPos cursor) {
      BlockState state = this.getBlockState(cursor);
      if (state.isAir()) {
         return null;
      }

      BlockPos pos = cursor.immutable();
      if (hasDynamicOutlineShape(state)) {
         BlockHitResult result = state.getShape(this, pos).clip(from, to, pos);
         if (result != null) {
            BlockHitResult hitOverride = state.getInteractionShape(this, pos).clip(from, to, pos);
            if (hitOverride != null && hitOverride.getLocation().subtract(from).lengthSqr() < result.getLocation().subtract(from).lengthSqr()) {
               return result.withDirection(hitOverride.getDirection());
            }
         }

         return result;
      } else {
         BlockHitResult result = clipBoxes(this.getOutlineBoxes(state, pos), from, to, pos);
         if (result != null) {
            List<AABB> interaction = this.interactionBoxesCache.get(state);
            if (interaction == null) {
               interaction = state.getInteractionShape(this, pos).toAabbs();
               this.interactionBoxesCache.put(state, interaction);
            }

            BlockHitResult hitOverride = clipBoxes(interaction, from, to, pos);
            if (hitOverride != null && hitOverride.getLocation().subtract(from).lengthSqr() < result.getLocation().subtract(from).lengthSqr()) {
               return result.withDirection(hitOverride.getDirection());
            }
         }

         return result;
      }
   }

   private static @Nullable BlockHitResult clipBoxes(List<AABB> boxes, Vec3 from, Vec3 to, BlockPos pos) {
      if (boxes.isEmpty()) {
         return null;
      }

      Vec3 diff = to.subtract(from);
      if (diff.lengthSqr() < 1.0E-7) {
         return null;
      }

      Vec3 test = from.add(diff.scale(0.001));
      double tx = test.x - pos.getX();
      double ty = test.y - pos.getY();
      double tz = test.z - pos.getZ();

      for (AABB b : boxes) {
         if (b.minX <= tx && tx < b.maxX && b.minY <= ty && ty < b.maxY && b.minZ <= tz && tz < b.maxZ) {
            return new BlockHitResult(test, Direction.getApproximateNearest(diff.x, diff.y, diff.z).getOpposite(), pos, true);
         }
      }

      return AABB.clip(boxes, from, to, pos);
   }

   public @Nullable BlockHitResult raycastOutline(Vec3 eye, Vec3 to) {
      return this.raycastBlock(eye, to);
   }

   public AiPhysics.PhysicsWorld asPhysicsWorld() {
      final PrintView view = this;
      return new AiPhysics.PhysicsWorld() {
         private final Long2ObjectOpenHashMap<List<AABB>> init = new Long2ObjectOpenHashMap<>();

         private List<AABB> cellBoxes(int x, int y, int z) {
            return this.init.computeIfAbsent(BlockPos.asLong(x, y, z), key -> {
               BlockPos at = BlockPos.of(key);
               BlockState state = view.getBlockState(at);
               return state.isAir() ? List.of() : view.getCollisionBoxesForState(state, at).stream().map(box -> box.move(at)).toList();
            });
         }

         @Override
         public List<AABB> collisionBoxes(AABB query) {
            List<AABB> out = new ArrayList<>();

            for (int x = PrintView.floorInt(query.minX); x <= PrintView.floorInt(query.maxX); x++) {
               for (int y = PrintView.floorInt(query.minY) - 1; y <= PrintView.floorInt(query.maxY); y++) {
                  for (int z = PrintView.floorInt(query.minZ); z <= PrintView.floorInt(query.maxZ); z++) {
                     for (AABB box : this.cellBoxes(x, y, z)) {
                        if (box.intersects(query)) {
                           out.add(box);
                        }
                     }
                  }
               }
            }

            return out;
         }

         @Override
         public double blockFriction(int x, int y, int z) {
            return view.getBlockStateAt(x, y, z).getBlock().getFriction();
         }

         @Override
         public double speedFactor(int x, int y, int z) {
            return view.getBlockStateAt(x, y, z).getBlock().getSpeedFactor();
         }

         @Override
         public double jumpFactor(int x, int y, int z) {
            return view.getBlockStateAt(x, y, z).getBlock().getJumpFactor();
         }

         @Override
         public boolean sync(int x, int y, int z) {
            return view.getBlockStateAt(x, y, z).is(BlockTags.CLIMBABLE);
         }

         @Override
         public double bounceFactor(int x, int y, int z) {
            Block block = view.getBlockStateAt(x, y, z).getBlock();
            return block instanceof SlimeBlock ? 1.0 : (block instanceof BedBlock ? 0.75 : 0.0);
         }

         @Override
         public boolean tracksStuckBlocks() {
            return true;
         }

         @Override
         public int stuckBlockKindIn(AABB box) {
            int hold = 0;

            for (int x = PrintView.floorInt(box.minX); x <= PrintView.floorInt(box.maxX); x++) {
               for (int y = PrintView.floorInt(box.minY); y <= PrintView.floorInt(box.maxY); y++) {
                  for (int z = PrintView.floorInt(box.minZ); z <= PrintView.floorInt(box.maxZ); z++) {
                     Block block = view.getBlockStateAt(x, y, z).getBlock();
                     if (block instanceof WebBlock) {
                        return 1;
                     }

                     if (block instanceof SweetBerryBushBlock) {
                        hold = 2;
                     }
                  }
               }
            }

            return hold;
         }

         @Override
         public boolean isLavaOrUnloaded(int x, int y, int z) {
            return view.getBlockStateAt(x, y, z).getFluidState().getType().isSame(Fluids.LAVA);
         }

         @Override
         public boolean isWater(int x, int y, int z) {
            return view.getBlockStateAt(x, y, z).getFluidState().getType().isSame(Fluids.WATER);
         }

         @Override
         public boolean isInvisibleLightBlock(int x, int y, int z) {
            return PrintView.isHazardBlock(view.getBlockStateAt(x, y, z));
         }
      };
   }

   public static int floorInt(double v) {
      return (int)Math.floor(v);
   }

   public static Vec3 blockCenter(BlockPos p) {
      return new Vec3(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
   }

   private record ReadReceipt(long[] cells) {
   }

   private record ClickPointsEntry(double feetY, @Nullable List<Vec3> points, PrintView.@Nullable ReadReceipt inputs) {
   }

   private final class NativeReadTracker implements AutoCloseable {
      private final long startUntrackedCount = PrintView.this.untrackedReadCount;
      final LongOpenHashSet readCells = new LongOpenHashSet();
      private @Nullable IdentityHashMap<PrintView.ReadReceipt, Boolean> forwardedReceipts;
      boolean overflowed;
      boolean touchedHypothesis;

      NativeReadTracker() {
         PrintView.this.activeNativeTrackers.add(this);
      }

      void recordRead(long key) {
         if (PrintView.this.hypotheticalBlocks.containsKey(key)) {
            this.touchedHypothesis = true;
         }

         if (!this.overflowed && !this.readCells.contains(key)) {
            if (this.readCells.size() >= 512) {
               this.overflowed = true;
            } else {
               this.readCells.add(key);
            }
         }
      }

      boolean isPure() {
         return !this.touchedHypothesis && PrintView.this.untrackedReadCount == this.startUntrackedCount;
      }

      boolean canBuildReceipt() {
         return !this.overflowed && this.isPure();
      }

      boolean hasForwarded(PrintView.ReadReceipt inputs) {
         return this.overflowed || this.forwardedReceipts != null && this.forwardedReceipts.containsKey(inputs);
      }

      void markForwarded(PrintView.ReadReceipt inputs) {
         if (!this.overflowed) {
            if (this.forwardedReceipts == null) {
               this.forwardedReceipts = new IdentityHashMap<>();
            }

            if (this.forwardedReceipts.size() < 256) {
               this.forwardedReceipts.put(inputs, Boolean.TRUE);
            }
         }
      }

      @Override
      public void close() {
         PrintView.this.activeNativeTrackers.remove(this);
      }
   }

   private record HypothesisUndo(BlockState state, boolean temporary) {
   }

   final class SnapshotScope implements AutoCloseable {
      private SnapshotScope() {
         if (PrintView.this.snapshotCacheActive) {
            throw new IllegalStateException("Nested planner snapshot");
         }

         PrintView.this.snapshotStateCache.clear();
         PrintView.this.snapshotCacheActive = true;
      }

      @Override
      public void close() {
         PrintView.this.snapshotCacheActive = false;
         PrintView.this.snapshotStateCache.clear();
      }
   }

   private record StandableHeightEntry(double height, PrintView.@Nullable ReadReceipt inputs) {
   }

   final class DynamicShapeTracker {
      private final Long2ObjectOpenHashMap<PrintView.DynamicShapeTracker.ShapeSnapshot> verifiedDynamicShapes = new Long2ObjectOpenHashMap<>();
      private final LongOpenHashSet pendingDynamicCells = new LongOpenHashSet();
      private boolean invalidated;

      PrintView.DynamicShapeTracker.ShapeAttachment attach() {
         return new PrintView.DynamicShapeTracker.ShapeAttachment();
      }

      boolean verifyStillValid() {
         if (this.invalidated) {
            return false;
         }

         boolean previous = PrintView.this.suppressDynamicShapeTracking;
         PrintView.this.suppressDynamicShapeTracking = true;

         try {
            for (Entry<PrintView.DynamicShapeTracker.ShapeSnapshot> entry : this.verifiedDynamicShapes.long2ObjectEntrySet()) {
               if (!entry.getValue().equals(this.snapshotDynamicCell(entry.getLongKey()))) {
                  return !(this.invalidated = true);
               }
            }

            for (long key : this.pendingDynamicCells) {
               PrintView.DynamicShapeTracker.ShapeSnapshot now = this.snapshotDynamicCell(key);
               if (now == null) {
                  return !(this.invalidated = true);
               }

               this.verifiedDynamicShapes.putIfAbsent(key, now);
            }

            this.pendingDynamicCells.clear();
            return true;
         } finally {
            PrintView.this.suppressDynamicShapeTracking = previous;
         }
      }

      private PrintView.DynamicShapeTracker.ShapeSnapshot snapshotDynamicCell(long key) {
         BlockPos pos = BlockPos.of(key);
         BlockState state = PrintView.this.getBlockState(pos);
         return state == PrintView.UNLOADED_SENTINEL
            ? null
            : new PrintView.DynamicShapeTracker.ShapeSnapshot(state, List.copyOf(state.getCollisionShape(PrintView.this, pos).toAabbs()));
      }

      private record ShapeSnapshot(BlockState state, List<AABB> boxes) {
      }

      final class ShapeAttachment implements AutoCloseable {
         final boolean added = !PrintView.this.activeDynamicShapeTrackers.contains(DynamicShapeTracker.this);

         ShapeAttachment() {
            if (this.added) {
               PrintView.this.activeDynamicShapeTrackers.add(DynamicShapeTracker.this);
            }
         }

         @Override
         public void close() {
            if (this.added) {
               PrintView.this.activeDynamicShapeTrackers.remove(DynamicShapeTracker.this);
            }
         }
      }
   }

   final class ReadTracker implements AutoCloseable {
      private final long startUntrackedCount = PrintView.this.untrackedReadCount;
      final Long2ObjectOpenHashMap<BlockState> recordedStates = new Long2ObjectOpenHashMap<>();
      final Long2ObjectOpenHashMap<BlockState> capturedHypotheses = new Long2ObjectOpenHashMap<>(PrintView.this.hypotheticalBlocks);
      private final @Nullable LongOpenHashSet capturedTemporaries;
      private @Nullable IdentityHashMap<PrintView.ReadReceipt, Boolean> forwardedReceipts;
      boolean overflowed;

      ReadTracker(boolean retained) {
         this.capturedTemporaries = retained ? new LongOpenHashSet(PrintView.this.ownedTemporaryBlocks) : null;
      }

      PrintView.ReadTracker.TrackerAttachment attach() {
         return new PrintView.ReadTracker.TrackerAttachment();
      }

      boolean isComplete() {
         return !this.overflowed && PrintView.this.untrackedReadCount == this.startUntrackedCount;
      }

      private void recordPos(BlockPos pos) {
         this.recordPosWithLive(pos, null);
      }

      private void recordPosWithLive(BlockPos pos, @Nullable BlockState live) {
         long key = pos.asLong();
         if (!this.overflowed && !this.recordedStates.containsKey(key)) {
            if (this.recordedStates.size() >= 8192) {
               this.overflowed = true;
            } else {
               BlockState state = this.capturedHypotheses.get(key);
               this.recordedStates.put(key, state != null ? state : (live != null ? live : PrintView.this.getLiveBlockState(pos)));
            }
         }
      }

      boolean hasForwarded(PrintView.ReadReceipt inputs) {
         return this.overflowed || this.forwardedReceipts != null && this.forwardedReceipts.containsKey(inputs);
      }

      void markForwarded(PrintView.ReadReceipt inputs) {
         if (!this.overflowed) {
            if (this.forwardedReceipts == null) {
               this.forwardedReceipts = new IdentityHashMap<>();
            }

            if (this.forwardedReceipts.size() < 256) {
               this.forwardedReceipts.put(inputs, Boolean.TRUE);
            }
         }
      }

      @Override
      public void close() {
         PrintView.this.activeReadTrackers.remove(this);
      }

      final class TrackerAttachment implements AutoCloseable {
         private final boolean added;
         private boolean closed;

         private TrackerAttachment() {
            if (ReadTracker.this.capturedTemporaries != null
               && PrintView.this.matchesHypotheses(ReadTracker.this.capturedHypotheses)
               && PrintView.this.matchesOwnedTemporaries(ReadTracker.this.capturedTemporaries)) {
               this.added = !PrintView.this.activeReadTrackers.contains(ReadTracker.this);
               if (this.added) {
                  PrintView.this.activeReadTrackers.add(ReadTracker.this);
               }
            } else {
               throw new IllegalStateException("A retained native receipt requires its original outer hypotheses");
            }
         }

         @Override
         public void close() {
            if (!this.closed && this.added) {
               PrintView.this.activeReadTrackers.remove(ReadTracker.this);
            }

            this.closed = true;
         }
      }
   }
}
