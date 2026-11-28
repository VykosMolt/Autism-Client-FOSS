package autismclient.ai.print;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class PrintCleanupAccess {
   private static final int init_I = 128;
   private static final int load_I = 8192;
   final PrintCore core;
   final Long2ObjectOpenHashMap<PrintCleanupAccess.Inner_StreamUtil> entries = new Long2ObjectOpenHashMap<>();
   final Int2ObjectOpenHashMap<LongOpenHashSet> keysByOwner = new Int2ObjectOpenHashMap<>();
   final Long2ObjectOpenHashMap<LongOpenHashSet> dependentsByRead = new Long2ObjectOpenHashMap<>();
   final LongOpenHashSet dirtyKeys = new LongOpenHashSet();
   final LongOpenHashSet load_LongOpenHashSet = new LongOpenHashSet();
   final LongOpenHashSet read_LongOpenHashSet = new LongOpenHashSet();
   long revision;
   long load_J;
   long read_J;
   private long creativeEpoch;
   private long invalidationEpoch;
   private long fullScanEpoch;
   private boolean creativeMode;
   private final LinkedHashMap<Long, PrintCleanupAccess.Inner_CachePool> blockGuards = new LinkedHashMap<>(16, 0.75F, true);

   PrintCleanupAccess(PrintCore core) {
      this.core = core;
   }

   void claim(long key, int owner) {
      PrintCleanupAccess.Inner_StreamUtil entry = this.entries.get(key);
      if (entry != null && entry.owner == owner) {
         if (this.core.env.isCreativeMode()) {
            entry.init_Z = true;
         }
      } else {
         if (entry != null) {
            this.release(key);
         }

         this.entries.put(key, new PrintCleanupAccess.Inner_StreamUtil(key, owner));
         this.entries.get(key).init_Z = this.core.env.isCreativeMode();
         this.entries.get(key).createdCreativeEpoch = this.creativeEpoch;
         this.keysByOwner.computeIfAbsent(owner, ignored -> new LongOpenHashSet()).add(key);
         this.markEntryDirty(key);
         this.revision++;
      }
   }

   void markEntryDirty(long key) {
      PrintCleanupAccess.Inner_StreamUtil entry = this.entries.get(key);
      if (entry != null && this.core.env.isCreativeMode()) {
         entry.init_Z = true;
      }

      if (entry != null && this.isOwnedTemporary(entry)) {
         this.dirtyKeys.add(key);
         this.revision++;
      }
   }

   void markOwnerEntriesDirty(int owner) {
      LongOpenHashSet keys = this.keysByOwner.get(owner);
      if (keys != null) {
         for (long key : keys) {
            this.dirtyKeys.add(key);
         }

         this.revision++;
      }
   }

   void release(long key) {
      PrintCleanupAccess.Inner_StreamUtil entry = this.entries.remove(key);
      if (entry != null) {
         this.unlinkCertificate(entry);
         LongOpenHashSet keys = this.keysByOwner.get(entry.owner);
         if (keys != null && keys.remove(key) && keys.isEmpty()) {
            this.keysByOwner.remove(entry.owner);
         }

         this.dirtyKeys.remove(key);
         this.load_LongOpenHashSet.remove(key);
         this.read_LongOpenHashSet.remove(key);
         this.revision++;
      }
   }

   void markReadDirty(long key) {
      LongOpenHashSet keys = this.dependentsByRead.get(key);
      if (keys != null) {
         this.dirtyKeys.addAll(keys);
         this.revision++;
      }
   }

   void bumpRevision() {
      this.invalidationEpoch++;
      this.revision++;
   }

   private boolean isOwnedTemporary(PrintCleanupAccess.Inner_StreamUtil entry) {
      return entry.owner >= 0
         && entry.owner < this.core.cellStates.length
         && this.core.cellStates[entry.owner] == 3
         && this.core.temporaryBlocks.containsKey(entry.cellKey);
   }

   private void unlinkCertificate(PrintCleanupAccess.Inner_StreamUtil entry) {
      if (entry.certificate != null) {
         for (long key : entry.certificate.reads.keySet()) {
            LongOpenHashSet keys = this.dependentsByRead.get(key);
            if (keys != null && keys.remove(entry.cellKey) && keys.isEmpty()) {
               this.dependentsByRead.remove(key);
            }
         }
      }
   }

   private void attachCertificate(PrintCleanupAccess.Inner_StreamUtil entry, PrintCleanupAccess.Inner_DataUtil certificate) {
      this.unlinkCertificate(entry);
      entry.certificate = certificate;

      for (long key : certificate.reads.keySet()) {
         this.dependentsByRead.computeIfAbsent(key, ignored -> new LongOpenHashSet()).add(entry.cellKey);
      }

      this.dirtyKeys.remove(entry.cellKey);
      this.read_LongOpenHashSet.remove(entry.cellKey);
      if (certificate.stable) {
         this.load_LongOpenHashSet.remove(entry.cellKey);
      } else {
         this.load_LongOpenHashSet.add(entry.cellKey);
      }
   }

   PrintCleanupAccess.Inner_CachePool newCleanupProof(BlockPos position, BlockState state) {
      if (this.creativeMode != this.core.env.isCreativeMode()) {
         this.creativeMode = this.core.env.isCreativeMode();
         if (this.creativeMode) {
            this.invalidationEpoch++;
            this.creativeEpoch++;
         }

         this.revision++;
      }

      return new PrintCleanupAccess.Inner_CachePool(position, state);
   }

   boolean wouldBlockCleanup(BlockPos position, BlockState state) {
      if (this.entries.isEmpty()) {
         return false;
      }

      PrintCleanupAccess.Inner_CachePool guard = this.blockGuards.get(position.asLong());
      if (guard == null || guard.state != state || !guard.proofValid()) {
         guard = this.newCleanupProof(position, state);
         if (this.blockGuards.size() >= 64) {
            this.blockGuards.remove(this.blockGuards.keySet().iterator().next());
         }

         this.blockGuards.put(position.asLong(), guard);
      }

      if (!guard.update && !this.core.isBudgetSpent()) {
         guard.advanceProof(this.core.workBudget());
      }

      return guard.flush || !guard.update || guard.sync;
   }

   private static boolean isConnectingBlock(BlockState state) {
      Block block = state.getBlock();
      return block instanceof FenceBlock
         || block instanceof WallBlock
         || block instanceof StairBlock
         || block instanceof IronBarsBlock
         || block instanceof FenceGateBlock
         || block instanceof ChestBlock;
   }

   final class Inner_BufferHelper {
      final long key;
      final PrintCleanupAccess.Inner_DataUtil certificate;
      final Long2ObjectOpenHashMap<BlockState> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
      final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
      LongIterator init_LongIterator;
      final PrintView.DynamicShapeTracker init_ArrayUtil_StreamUt = PrintCleanupAccess.this.core.view.new DynamicShapeTracker();
      int init_I;
      int load_I;
      Vec3 init_Vec3;
      boolean init_Z;
      boolean load_Z;
      boolean invalidated;
      boolean stable = true;

      Inner_BufferHelper(long key, PrintCleanupAccess.Inner_DataUtil certificate) {
         this.key = key;
         this.certificate = certificate;
         PrintCleanupAccess.this.read_J++;
      }

      PrintCleanupAccess.Inner_DataUtil toCertificate() {
         return new PrintCleanupAccess.Inner_DataUtil(
            this.certificate.point,
            this.certificate.before,
            this.certificate.after,
            this.init_Long2ObjectOpenHas,
            this.init_LongOpenHashSet,
            this.stable
         );
      }

      void advance(long allowance) {
         if (!this.init_Z) {
            if (!this.init_ArrayUtil_StreamUt.verifyStillValid()) {
               this.invalidated = this.init_Z = true;
            } else {
               try {
                  this.advanceSteps(allowance);
               } finally {
                  if (!this.init_ArrayUtil_StreamUt.verifyStillValid()) {
                     this.load_Z = false;
                     this.invalidated = this.init_Z = true;
                  }
               }
            }
         }
      }

      private void advanceSteps(long allowance) {
         long started = PrintCleanupAccess.this.core.view.workUnits();
         boolean dependencyOnly = this.init_I == 3;

         try (
            PrintView.ReadTracker capture = PrintCleanupAccess.this.core.view.openReadTracker();
            PrintView.DynamicShapeTracker.ShapeAttachment geometry = this.init_ArrayUtil_StreamUt.attach();
         ) {
            try {
               for (int steps = 0; steps < 128 && PrintCleanupAccess.this.core.view.workUnits() - started < Math.max(1L, allowance); steps++) {
                  if (this.init_I == 3) {
                     if (this.init_LongIterator == null) {
                        this.init_LongIterator = this.init_LongOpenHashSet.iterator();
                     }

                     if (!this.init_LongIterator.hasNext()) {
                        this.load_Z = PrintCleanupAccess.this.core.temporaryRemovalKeepsNeighbors(BlockPos.of(this.key));
                        this.init_Z = true;
                        return;
                     }

                     PrintCleanupAccess.this.core.view.getBlockState(BlockPos.of(this.init_LongIterator.nextLong()));
                  } else if (this.init_I == 0) {
                     BlockPos pos = BlockPos.of(this.key);
                     if (!PrintCleanupAccess.this.core.env.hasChunkAt(pos)
                        || !PrintCleanupAccess.this.core.temporaryBlocks.containsKey(this.key)
                        || !PrintFlight.isFlightPointSafe(PrintCleanupAccess.this.core.view, this.certificate.point)
                        || !PrintCleanupAccess.this.core.temporaryRemovalKeepsNeighbors(pos)) {
                        this.init_Z = true;
                        return;
                     }

                     PrintTarget target = new PrintTarget(
                        -1, pos, PrintCleanupAccess.this.core.view.getBlockState(pos), PrintTarget.TargetKind.PLACE, Items.AIR, null
                     );
                     if (PrintClicks.findClick(
                           PrintCleanupAccess.this.core.view,
                           PrintCleanupAccess.this.core.env,
                           PrintCleanupAccess.this.core.rules,
                           target,
                           Items.DIAMOND_PICKAXE,
                           PrintClicks.Inner_CachePool.DIG,
                           this.certificate.point,
                           false,
                           false,
                           new PrintClicks.Inner_DataUtil(2000),
                           4.1000000000000005
                        )
                        == null) {
                        this.init_Z = true;
                        return;
                     }

                     this.init_I = 1;
                     this.init_Vec3 = this.certificate.point;
                  } else {
                     PrintFlight.FlightPath route = this.init_I == 1 ? this.certificate.before : this.certificate.after;
                     boolean removed = this.init_I == 2;
                     if (removed) {
                        PrintCleanupAccess.this.core.view.pushHypothesis(BlockPos.of(this.key), Blocks.AIR.defaultBlockState());
                     }

                     try {
                        if (this.load_I < route.points().size()) {
                           Vec3 next = route.points().get(this.load_I++);
                           if (!PrintFlight.isFlightPointSafe(PrintCleanupAccess.this.core.view, this.init_Vec3)
                              || !PrintFlight.isFlightSegmentClear(PrintCleanupAccess.this.core.view, this.init_Vec3, next)) {
                              this.init_Z = true;
                              return;
                           }

                           this.init_Vec3 = next;
                        } else {
                           if (this.init_I != 1) {
                              this.init_I = 3;
                              return;
                           }

                           this.init_I = 2;
                           this.load_I = 0;
                           this.init_Vec3 = this.certificate.point;
                        }
                     } finally {
                        if (removed) {
                           PrintCleanupAccess.this.core.view.popHypothesis(BlockPos.of(this.key));
                        }
                     }
                  }
               }
            } finally {
               this.stable = this.stable & capture.isComplete();

               for (Entry<BlockState> read : capture.recordedStates.long2ObjectEntrySet()) {
                  if (this.init_Long2ObjectOpenHas.size() >= 8192 && !this.init_Long2ObjectOpenHas.containsKey(read.getLongKey())) {
                     this.stable = false;
                     break;
                  }

                  BlockState prior = this.init_Long2ObjectOpenHas.putIfAbsent(read.getLongKey(), read.getValue());
                  if (read.getValue() == PrintView.UNLOADED_SENTINEL || prior != null && prior != read.getValue()) {
                     this.load_Z = false;
                     this.invalidated = this.init_Z = true;
                  }

                  if (!dependencyOnly && PrintCleanupAccess.isConnectingBlock(read.getValue())) {
                     for (Direction direction : Direction.values()) {
                        if (this.init_LongOpenHashSet.size() >= 8192) {
                           this.stable = false;
                           break;
                        }

                        this.init_LongOpenHashSet.add(BlockPos.offset(read.getLongKey(), direction));
                     }
                  }
               }
            }
         }
      }
   }

   final class Inner_CachePool {
      final BlockPos position;
      final BlockState state;
      final long worldRevision = PrintCleanupAccess.this.core.view.worldMutationRevision();
      final long cleanupRevision = PrintCleanupAccess.this.revision;
      final long read_J = PrintCleanupAccess.this.invalidationEpoch;
      final boolean creativeMode = PrintCleanupAccess.this.core.env.isCreativeMode();
      final boolean load_Z;
      final PrintView.DynamicShapeTracker init_ArrayUtil_StreamUt;
      final Long2ObjectOpenHashMap<BlockState> hypotheses;
      final LongOpenHashSet ownedTemporaries;
      final LongOpenHashSet load_LongOpenHashSet;
      final LongArrayList candidateKeys;
      final boolean viewClean;
      LongIterator init_LongIterator;
      LongIterator load_LongIterator;
      int init_I;
      int candidateCursor;
      int stage;
      PrintCleanupAccess.Inner_StreamUtil currentEntry;
      PrintCleanupAccess.Inner_ConfigStore init_BufferHelper_Confi;
      PrintCleanupAccess.Inner_BufferHelper init_BufferHelper_Buffe;
      PrintCleanupAccess.Inner_DataUtil certificate;
      boolean needsRecheck;
      boolean update;
      boolean sync;
      boolean flush;

      Inner_CachePool(BlockPos position, BlockState state) {
         this.load_Z = PrintCleanupAccess.this.entries.isEmpty();
         this.init_ArrayUtil_StreamUt = PrintCleanupAccess.this.core.view.new DynamicShapeTracker();
         this.hypotheses = new Long2ObjectOpenHashMap<>();
         this.ownedTemporaries = new LongOpenHashSet();
         this.load_LongOpenHashSet = new LongOpenHashSet();
         this.candidateKeys = new LongArrayList();
         this.position = position;
         this.state = state;
         PrintCleanupAccess.this.core.view.copyHypothesesInto(this.hypotheses);
         PrintCleanupAccess.this.core.view.copyOwnedTemporariesInto(this.ownedTemporaries);
         this.viewClean = this.hypotheses.isEmpty() && this.ownedTemporaries.isEmpty();
         this.update = this.load_Z;
      }

      boolean proofValid() {
         return !this.flush
            && this.cleanupRevision == PrintCleanupAccess.this.revision
            && (
               this.load_Z
                  || this.worldRevision == PrintCleanupAccess.this.core.view.worldMutationRevision()
                     && this.read_J == PrintCleanupAccess.this.invalidationEpoch
                     && this.creativeMode == PrintCleanupAccess.this.core.env.isCreativeMode()
                     && PrintCleanupAccess.this.core.view.matchesHypotheses(this.hypotheses)
                     && PrintCleanupAccess.this.core.view.matchesOwnedTemporaries(this.ownedTemporaries)
                     && this.init_ArrayUtil_StreamUt.verifyStillValid()
            );
      }

      private boolean readsSpeculativeState(PrintCleanupAccess.Inner_DataUtil certificate) {
         for (long key : this.hypotheses.keySet()) {
            if (certificate.reads.containsKey(key)) {
               return true;
            }
         }

         for (long key : this.ownedTemporaries) {
            if (certificate.reads.containsKey(key)) {
               return true;
            }
         }

         return false;
      }

      void advanceProof(long allowance) {
         if (!this.proofValid()) {
            this.flush = true;
         } else if (!this.update) {
            try (PrintView.DynamicShapeTracker.ShapeAttachment geometry = this.init_ArrayUtil_StreamUt.attach()) {
               this.advanceSteps(allowance);
            } finally {
               if (!this.init_ArrayUtil_StreamUt.verifyStillValid()) {
                  this.flush = true;
               }
            }
         }
      }

      private void advanceSteps(long allowance) {
         long started = PrintCleanupAccess.this.core.view.workUnits();
         long deadline = System.nanoTime() + 2000000L;

         for (int steps = 0;
            steps < 128
               && PrintCleanupAccess.this.core.view.workUnits() - started < Math.max(1L, allowance)
               && (steps == 0 || System.nanoTime() < deadline);
            steps++
         ) {
            long left = Math.min(8192L, Math.max(1L, allowance - (PrintCleanupAccess.this.core.view.workUnits() - started)));
            if (this.init_I < 6) {
               if (this.load_LongIterator != null) {
                  if (!this.load_LongIterator.hasNext()) {
                     this.load_LongIterator = null;
                  } else {
                     long key = this.load_LongIterator.nextLong();
                     if (this.load_LongOpenHashSet.add(key)) {
                        this.candidateKeys.add(key);
                     }
                  }
               } else {
                  if (this.init_LongIterator == null) {
                     LongIterator var10001;
                     switch (this.init_I) {
                        case 0:
                           var10001 = (PrintCleanupAccess.this.fullScanEpoch != this.read_J
                                 ? PrintCleanupAccess.this.entries.keySet()
                                 : PrintCleanupAccess.this.dirtyKeys)
                              .iterator();
                           break;
                        case 1:
                           var10001 = PrintCleanupAccess.this.dependentsByRead.getOrDefault(this.position.asLong(), new LongOpenHashSet()).iterator();
                           break;
                        case 2:
                           var10001 = PrintCleanupAccess.this.load_LongOpenHashSet.iterator();
                           break;
                        case 3:
                           var10001 = PrintCleanupAccess.this.read_LongOpenHashSet.iterator();
                           break;
                        case 4:
                           var10001 = this.hypotheses.keySet().iterator();
                           break;
                        default:
                           var10001 = this.ownedTemporaries.iterator();
                     }

                     this.init_LongIterator = var10001;
                  }

                  if (!this.init_LongIterator.hasNext()) {
                     this.init_LongIterator = null;
                     this.init_I++;
                  } else {
                     long key = this.init_LongIterator.nextLong();
                     if (this.init_I >= 4) {
                        LongOpenHashSet linked = PrintCleanupAccess.this.dependentsByRead.get(key);
                        if (linked != null) {
                           this.load_LongIterator = linked.iterator();
                        }
                     } else if (this.load_LongOpenHashSet.add(key)) {
                        this.candidateKeys.add(key);
                     }
                  }
               }
            } else {
               if (this.currentEntry == null) {
                  if (this.candidateCursor >= this.candidateKeys.size()) {
                     if (this.viewClean) {
                        PrintCleanupAccess.this.fullScanEpoch = this.read_J;
                     }

                     this.update = true;
                     return;
                  }

                  this.currentEntry = PrintCleanupAccess.this.entries.get(this.candidateKeys.getLong(this.candidateCursor++));
                  if (this.currentEntry == null
                     || !PrintCleanupAccess.this.isOwnedTemporary(this.currentEntry)
                     || this.currentEntry.cellKey == this.position.asLong()) {
                     this.currentEntry = null;
                     continue;
                  }

                  if (this.creativeMode) {
                     this.currentEntry.init_Z = true;
                  }

                  this.certificate = this.currentEntry.certificate;
                  this.needsRecheck = this.certificate != null
                     && (
                        !this.certificate.stable
                           || this.certificate.reads.containsKey(this.position.asLong())
                           || this.readsSpeculativeState(this.certificate)
                     );
                  if (!this.creativeMode
                     && this.certificate == null
                     && !this.currentEntry.init_Z
                     && this.currentEntry.createdCreativeEpoch >= PrintCleanupAccess.this.creativeEpoch) {
                     this.currentEntry = null;
                     continue;
                  }

                  if (this.certificate == null
                     && this.currentEntry.cleanWorldRevision == this.worldRevision
                     && !PrintCleanupAccess.this.dirtyKeys.contains(this.currentEntry.cellKey)) {
                     this.sync = this.update = true;
                     PrintCleanupAccess.this.core.cleanupCandidates.add(this.currentEntry.cellKey);
                     return;
                  }

                  this.stage = 0;
                  if (this.certificate != null) {
                     this.init_BufferHelper_Buffe = PrintCleanupAccess.this.new Inner_BufferHelper(this.currentEntry.cellKey, this.certificate);
                  } else {
                     this.init_BufferHelper_Confi = PrintCleanupAccess.this.new Inner_ConfigStore(this.currentEntry.cellKey, null);
                  }
               }

               if (this.stage == 0) {
                  if (this.init_BufferHelper_Buffe != null) {
                     this.init_BufferHelper_Buffe.advance(left);
                     if (this.init_BufferHelper_Buffe.invalidated) {
                        this.flush = true;
                        return;
                     }

                     if (!this.init_BufferHelper_Buffe.init_Z) {
                        continue;
                     }

                     if (this.init_BufferHelper_Buffe.load_Z) {
                        this.certificate = this.init_BufferHelper_Buffe.toCertificate();
                     } else {
                        this.init_BufferHelper_Confi = PrintCleanupAccess.this.new Inner_ConfigStore(
                           this.currentEntry.cellKey, this.certificate.point
                        );
                        this.certificate = null;
                     }

                     this.init_BufferHelper_Buffe = null;
                  }

                  if (this.init_BufferHelper_Confi != null) {
                     this.init_BufferHelper_Confi.advance(left);
                     if (this.init_BufferHelper_Confi.read_Z) {
                        this.flush = true;
                        return;
                     }

                     if (!this.init_BufferHelper_Confi.init_Z) {
                        continue;
                     }

                     this.certificate = this.init_BufferHelper_Confi.init_BufferHelper_DataU;
                     if (this.certificate == null) {
                        if (this.viewClean && !this.init_BufferHelper_Confi.load_Z) {
                           this.currentEntry.cleanWorldRevision = this.worldRevision;
                           PrintCleanupAccess.this.read_LongOpenHashSet.add(this.currentEntry.cellKey);
                           PrintCleanupAccess.this.dirtyKeys.remove(this.currentEntry.cellKey);
                        }

                        this.sync = this.update = true;
                        PrintCleanupAccess.this.core.cleanupCandidates.add(this.currentEntry.cellKey);
                        return;
                     }

                     this.init_BufferHelper_Confi = null;
                  }

                  if (this.viewClean) {
                     PrintCleanupAccess.this.attachCertificate(this.currentEntry, this.certificate);
                  }

                  if (PrintCleanupAccess.this.core.view.getBlockState(this.position) != this.state
                     && (
                        this.certificate.neighbors.contains(this.position.asLong())
                           || !this.certificate.stable && this.certificate.reads.size() >= 8192
                     )) {
                     this.sync = this.update = true;
                     PrintCleanupAccess.this.core.cleanupCandidates.add(this.currentEntry.cellKey);
                     return;
                  }

                  if (this.certificate.stable && !this.certificate.reads.containsKey(this.position.asLong())) {
                     this.currentEntry = null;
                  } else {
                     this.stage = 1;
                     this.init_BufferHelper_Buffe = PrintCleanupAccess.this.new Inner_BufferHelper(this.currentEntry.cellKey, this.certificate);
                  }
               } else {
                  PrintCleanupAccess.this.core.view.pushHypothesis(this.position, this.state);

                  try {
                     if (this.init_BufferHelper_Buffe != null) {
                        this.init_BufferHelper_Buffe.advance(left);
                        if (!this.init_BufferHelper_Buffe.invalidated) {
                           if (this.init_BufferHelper_Buffe.init_Z) {
                              if (this.init_BufferHelper_Buffe.load_Z) {
                                 this.init_BufferHelper_Buffe = null;
                                 this.currentEntry = null;
                              } else {
                                 this.init_BufferHelper_Buffe = null;
                                 this.init_BufferHelper_Confi = PrintCleanupAccess.this.new Inner_ConfigStore(
                                    this.currentEntry.cellKey, this.certificate.point
                                 );
                              }
                           }
                           continue;
                        }

                        this.flush = true;
                        return;
                     }

                     this.init_BufferHelper_Confi.advance(left);
                     if (this.init_BufferHelper_Confi.read_Z) {
                        this.flush = true;
                        return;
                     }

                     if (!this.init_BufferHelper_Confi.init_Z) {
                        continue;
                     }

                     if (this.init_BufferHelper_Confi.init_BufferHelper_DataU != null) {
                        this.init_BufferHelper_Confi = null;
                        this.currentEntry = null;
                        continue;
                     }

                     this.sync = this.update = true;
                     PrintCleanupAccess.this.core.cleanupCandidates.add(this.currentEntry.cellKey);
                  } finally {
                     PrintCleanupAccess.this.core.view.popHypothesis(this.position);
                  }

                  return;
               }
            }
         }
      }
   }

   final class Inner_ConfigStore {
      final long key;
      final PrintTarget target;
      final ArrayList<Long> workstations = new ArrayList<>();
      final PrintClicks.Inner_DataUtil clickBudget = new PrintClicks.Inner_DataUtil(18000);
      List<Vec3> init_List;
      Vec3 init_Vec3;
      PrintCore.FlightExitSearch init_UiHelper_h;
      PrintCore.FlightExitSearch load_UiHelper_h;
      PrintCleanupAccess.Inner_BufferHelper init_BufferHelper_Buffe;
      int init_I;
      int load_I;
      int read_I;
      final PrintView.DynamicShapeTracker init_ArrayUtil_StreamUt = PrintCleanupAccess.this.core.view.new DynamicShapeTracker();
      boolean init_Z;
      boolean load_Z;
      boolean read_Z;
      boolean started;
      PrintCleanupAccess.Inner_DataUtil init_BufferHelper_DataU;

      Inner_ConfigStore(long key, Vec3 preferred) {
         this.key = key;
         BlockPos pos = BlockPos.of(key);
         this.target = new PrintTarget(
            -1, pos, PrintCleanupAccess.this.core.view.getBlockState(pos), PrintTarget.TargetKind.PLACE, Items.AIR, null
         );
         this.workstations.addAll(PrintFlightWorkstations.init(pos));
         this.workstations.sort(Comparator.<Long>comparingDouble(k -> BlockPos.of(k).distToCenterSqr(Vec3.atCenterOf(pos))).thenComparingLong(k -> k));
         if (preferred == null) {
            PrintCore.SupportRecovery recovery = PrintCleanupAccess.this.core.recoveries.get(key);
            if (recovery != null) {
               preferred = recovery.work();
            }
         }

         if (preferred != null) {
            this.init_List = List.of(preferred);
         }

         PrintCleanupAccess.this.load_J++;
      }

      void advance(long allowance) {
         if (!this.init_ArrayUtil_StreamUt.verifyStillValid()) {
            this.read_Z = this.init_Z = true;
         } else {
            long transientBefore = PrintCleanupAccess.this.core.view.untrackedReadCount();

            try (PrintView.DynamicShapeTracker.ShapeAttachment geometry = this.init_ArrayUtil_StreamUt.attach()) {
               this.advanceSteps(allowance);
            } finally {
               this.load_Z = this.load_Z | PrintCleanupAccess.this.core.view.untrackedReadCount() != transientBefore;
               if (!this.init_ArrayUtil_StreamUt.verifyStillValid()) {
                  this.read_Z = this.init_Z = true;
                  this.init_BufferHelper_DataU = null;
               }
            }
         }
      }

      private void advanceSteps(long allowance) {
         long started = PrintCleanupAccess.this.core.view.workUnits();
         if (!this.started) {
            this.started = true;
            if (!PrintCleanupAccess.this.core.temporaryRemovalKeepsNeighbors(this.target.pos())) {
               this.init_Z = true;
               return;
            }
         }

         for (int steps = 0; !this.init_Z && steps < 128 && PrintCleanupAccess.this.core.view.workUnits() - started < Math.max(1L, allowance); steps++) {
            long left = Math.max(1L, allowance - (PrintCleanupAccess.this.core.view.workUnits() - started));
            if (this.init_BufferHelper_Buffe != null) {
               this.init_BufferHelper_Buffe.advance(left);
               if (this.init_BufferHelper_Buffe.invalidated) {
                  this.read_Z = this.init_Z = true;
                  return;
               }

               if (!this.init_BufferHelper_Buffe.init_Z) {
                  return;
               }

               if (this.init_BufferHelper_Buffe.load_Z) {
                  this.init_BufferHelper_DataU = this.init_BufferHelper_Buffe.toCertificate();
                  this.init_Z = true;
                  return;
               }

               this.init_BufferHelper_Buffe = null;
               this.init_Vec3 = null;
               this.init_UiHelper_h = this.load_UiHelper_h = null;
               this.read_I = 0;
            }

            if (this.init_Vec3 == null) {
               if (this.init_List == null || this.load_I >= this.init_List.size()) {
                  if (this.init_I >= this.workstations.size() || this.clickBudget.isExhausted()) {
                     this.load_Z = this.load_Z | this.clickBudget.isExhausted();
                     this.init_Z = true;
                     return;
                  }

                  long at = this.workstations.get(this.init_I++);
                  if (PrintCleanupAccess.this.core.view.getBlockState(BlockPos.of(at)) == PrintView.UNLOADED_SENTINEL) {
                     this.load_Z = true;
                  }

                  this.init_List = PrintFlightWorkstations.init(PrintCleanupAccess.this.core.view, this.target, PrintClicks.Inner_CachePool.DIG, at);
                  this.load_I = 0;
                  continue;
               }

               this.init_Vec3 = this.init_List.get(this.load_I++);
               if (!PrintFlight.isFlightPointSafe(PrintCleanupAccess.this.core.view, this.init_Vec3)) {
                  this.init_Vec3 = null;
                  continue;
               }

               PrintClicks.Inner_StreamUtil click = PrintClicks.findClick(
                  PrintCleanupAccess.this.core.view,
                  PrintCleanupAccess.this.core.env,
                  PrintCleanupAccess.this.core.rules,
                  this.target,
                  Items.DIAMOND_PICKAXE,
                  PrintClicks.Inner_CachePool.DIG,
                  this.init_Vec3,
                  false,
                  false,
                  this.clickBudget,
                  4.1000000000000005
               );
               if (click == null) {
                  this.init_Vec3 = null;
                  continue;
               }

               this.init_UiHelper_h = PrintCleanupAccess.this.core.new FlightExitSearch(this.init_Vec3);
               this.read_I = 1;
            }

            if (this.read_I == 1) {
               this.init_UiHelper_h.stepSearch(left);
               if (!this.init_UiHelper_h.done) {
                  return;
               }

               if (this.init_UiHelper_h.result == null) {
                  this.load_Z = this.load_Z | this.isSearchBudgetExhausted(this.init_UiHelper_h);
                  this.init_Vec3 = null;
                  this.init_UiHelper_h = null;
               } else {
                  this.read_I = 2;
               }
            } else {
               PrintCleanupAccess.this.core.view.pushHypothesis(this.target.pos(), Blocks.AIR.defaultBlockState());

               try {
                  if (!PrintFlight.isFlightPointSafe(PrintCleanupAccess.this.core.view, this.init_Vec3)) {
                     this.init_Vec3 = null;
                     this.init_UiHelper_h = this.load_UiHelper_h = null;
                     this.read_I = 0;
                     continue;
                  }

                  if (this.load_UiHelper_h == null) {
                     this.load_UiHelper_h = PrintCleanupAccess.this.core.new FlightExitSearch(this.init_Vec3);
                  }

                  this.load_UiHelper_h.stepSearch(left);
                  if (!this.load_UiHelper_h.done) {
                     return;
                  }
               } finally {
                  PrintCleanupAccess.this.core.view.popHypothesis(this.target.pos());
               }

               if (this.load_UiHelper_h.result != null) {
                  this.init_BufferHelper_Buffe = PrintCleanupAccess.this.new Inner_BufferHelper(
                     this.key,
                     new PrintCleanupAccess.Inner_DataUtil(
                        this.init_Vec3,
                        this.init_UiHelper_h.result,
                        this.load_UiHelper_h.result,
                        new Long2ObjectOpenHashMap<>(),
                        new LongOpenHashSet(),
                        false
                     )
                  );
               } else {
                  this.load_Z = this.load_Z | this.isSearchBudgetExhausted(this.load_UiHelper_h);
                  this.init_Vec3 = null;
                  this.init_UiHelper_h = this.load_UiHelper_h = null;
                  this.read_I = 0;
               }
            }
         }
      }

      private boolean isSearchBudgetExhausted(PrintCore.FlightExitSearch exit) {
         return exit.search != null
            && exit.search.result == null
            && !exit.search.openQueue.isEmpty()
            && exit.search.expandedNodes >= exit.search.maxExpansions;
      }
   }

   record Inner_DataUtil(
      Vec3 point,
      PrintFlight.FlightPath before,
      PrintFlight.FlightPath after,
      Long2ObjectOpenHashMap<BlockState> reads,
      LongOpenHashSet neighbors,
      boolean stable
   ) {
   }

   static final class Inner_StreamUtil {
      final long cellKey;
      int owner;
      PrintCleanupAccess.Inner_DataUtil certificate;
      long cleanWorldRevision = -9223372036854775808L;
      long createdCreativeEpoch;
      boolean init_Z;

      Inner_StreamUtil(long key, int owner) {
         this.cellKey = key;
         this.owner = owner;
      }
   }
}
