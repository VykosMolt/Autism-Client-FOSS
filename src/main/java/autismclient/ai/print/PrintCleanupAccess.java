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
   final PrintCore init_UiHelper;
   final Long2ObjectOpenHashMap<PrintCleanupAccess.Inner_StreamUtil> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   final Int2ObjectOpenHashMap<LongOpenHashSet> init_Int2ObjectOpenHash = new Int2ObjectOpenHashMap<>();
   final Long2ObjectOpenHashMap<LongOpenHashSet> load_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
   final LongOpenHashSet load_LongOpenHashSet = new LongOpenHashSet();
   final LongOpenHashSet read_LongOpenHashSet = new LongOpenHashSet();
   long init_J;
   long load_J;
   long read_J;
   private long write;
   private long update;
   private long sync;
   private boolean init_Z;
   private final LinkedHashMap<Long, PrintCleanupAccess.Inner_CachePool> init_LinkedHashMap = new LinkedHashMap<>(16, 0.75F, true);

   PrintCleanupAccess(PrintCore core) {
      this.init_UiHelper = core;
   }

   void init(long key, int owner) {
      PrintCleanupAccess.Inner_StreamUtil entry = this.init_Long2ObjectOpenHas.get(key);
      if (entry != null && entry.init_I == owner) {
         if (this.init_UiHelper.env.isCreativeMode()) {
            entry.init_Z = true;
         }
      } else {
         if (entry != null) {
            this.load(key);
         }

         this.init_Long2ObjectOpenHas.put(key, new PrintCleanupAccess.Inner_StreamUtil(key, owner));
         this.init_Long2ObjectOpenHas.get(key).init_Z = this.init_UiHelper.env.isCreativeMode();
         this.init_Long2ObjectOpenHas.get(key).read = this.write;
         this.init_Int2ObjectOpenHash.computeIfAbsent(owner, ignored -> new LongOpenHashSet()).add(key);
         this.markEntryDirty(key);
         this.init_J++;
      }
   }

   void markEntryDirty(long key) {
      PrintCleanupAccess.Inner_StreamUtil entry = this.init_Long2ObjectOpenHas.get(key);
      if (entry != null && this.init_UiHelper.env.isCreativeMode()) {
         entry.init_Z = true;
      }

      if (entry != null && this.init_Z_BufferHelper_Strea(entry)) {
         this.init_LongOpenHashSet.add(key);
         this.init_J++;
      }
   }

   void markOwnerEntriesDirty(int owner) {
      LongOpenHashSet keys = this.init_Int2ObjectOpenHash.get(owner);
      if (keys != null) {
         for (long key : keys) {
            this.init_LongOpenHashSet.add(key);
         }

         this.init_J++;
      }
   }

   void load(long key) {
      PrintCleanupAccess.Inner_StreamUtil entry = this.init_Long2ObjectOpenHas.remove(key);
      if (entry != null) {
         this.init_V_BufferHelper_Strea(entry);
         LongOpenHashSet keys = this.init_Int2ObjectOpenHash.get(entry.init_I);
         if (keys != null && keys.remove(key) && keys.isEmpty()) {
            this.init_Int2ObjectOpenHash.remove(entry.init_I);
         }

         this.init_LongOpenHashSet.remove(key);
         this.load_LongOpenHashSet.remove(key);
         this.read_LongOpenHashSet.remove(key);
         this.init_J++;
      }
   }

   void read(long key) {
      LongOpenHashSet keys = this.load_Long2ObjectOpenHas.get(key);
      if (keys != null) {
         this.init_LongOpenHashSet.addAll(keys);
         this.init_J++;
      }
   }

   void init() {
      this.update++;
      this.init_J++;
   }

   private boolean init_Z_BufferHelper_Strea(PrintCleanupAccess.Inner_StreamUtil entry) {
      return entry.init_I >= 0
         && entry.init_I < this.init_UiHelper.cellStates.length
         && this.init_UiHelper.cellStates[entry.init_I] == 3
         && this.init_UiHelper.temporaryBlocks.containsKey(entry.cellKey);
   }

   private void init_V_BufferHelper_Strea(PrintCleanupAccess.Inner_StreamUtil entry) {
      if (entry.init_BufferHelper_DataU != null) {
         for (long key : entry.init_BufferHelper_DataU.reads.keySet()) {
            LongOpenHashSet keys = this.load_Long2ObjectOpenHas.get(key);
            if (keys != null && keys.remove(entry.cellKey) && keys.isEmpty()) {
               this.load_Long2ObjectOpenHas.remove(key);
            }
         }
      }
   }

   private void init(PrintCleanupAccess.Inner_StreamUtil entry, PrintCleanupAccess.Inner_DataUtil certificate) {
      this.init_V_BufferHelper_Strea(entry);
      entry.init_BufferHelper_DataU = certificate;

      for (long key : certificate.reads.keySet()) {
         this.load_Long2ObjectOpenHas.computeIfAbsent(key, ignored -> new LongOpenHashSet()).add(entry.cellKey);
      }

      this.init_LongOpenHashSet.remove(entry.cellKey);
      this.read_LongOpenHashSet.remove(entry.cellKey);
      if (certificate.stable) {
         this.load_LongOpenHashSet.remove(entry.cellKey);
      } else {
         this.load_LongOpenHashSet.add(entry.cellKey);
      }
   }

   PrintCleanupAccess.Inner_CachePool newCleanupProof(BlockPos position, BlockState state) {
      if (this.init_Z != this.init_UiHelper.env.isCreativeMode()) {
         this.init_Z = this.init_UiHelper.env.isCreativeMode();
         if (this.init_Z) {
            this.update++;
            this.write++;
         }

         this.init_J++;
      }

      return new PrintCleanupAccess.Inner_CachePool(position, state);
   }

   boolean wouldBlockCleanup(BlockPos position, BlockState state) {
      if (this.init_Long2ObjectOpenHas.isEmpty()) {
         return false;
      }

      PrintCleanupAccess.Inner_CachePool guard = this.init_LinkedHashMap.get(position.asLong());
      if (guard == null || guard.init_BlockState != state || !guard.init()) {
         guard = this.newCleanupProof(position, state);
         if (this.init_LinkedHashMap.size() >= 64) {
            this.init_LinkedHashMap.remove(this.init_LinkedHashMap.keySet().iterator().next());
         }

         this.init_LinkedHashMap.put(position.asLong(), guard);
      }

      if (!guard.update && !this.init_UiHelper.isBudgetSpent()) {
         guard.init(this.init_UiHelper.workBudget());
      }

      return guard.flush || !guard.update || guard.sync;
   }

   private static boolean init_Z_BlockState(BlockState state) {
      Block block = state.getBlock();
      return block instanceof FenceBlock
         || block instanceof WallBlock
         || block instanceof StairBlock
         || block instanceof IronBarsBlock
         || block instanceof FenceGateBlock
         || block instanceof ChestBlock;
   }

   final class Inner_BufferHelper {
      final long init_J;
      final PrintCleanupAccess.Inner_DataUtil init_BufferHelper_DataU;
      final Long2ObjectOpenHashMap<BlockState> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
      final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
      LongIterator init_LongIterator;
      final PrintView.Inner_StreamUtil init_ArrayUtil_StreamUt = PrintCleanupAccess.this.init_UiHelper.view.new Inner_StreamUtil();
      int init_I;
      int load_I;
      Vec3 init_Vec3;
      boolean init_Z;
      boolean load_Z;
      boolean read;
      boolean write = true;

      Inner_BufferHelper(long key, PrintCleanupAccess.Inner_DataUtil certificate) {
         this.init_J = key;
         this.init_BufferHelper_DataU = certificate;
         PrintCleanupAccess.this.read_J++;
      }

      PrintCleanupAccess.Inner_DataUtil init() {
         return new PrintCleanupAccess.Inner_DataUtil(
            this.init_BufferHelper_DataU.point,
            this.init_BufferHelper_DataU.before,
            this.init_BufferHelper_DataU.after,
            this.init_Long2ObjectOpenHas,
            this.init_LongOpenHashSet,
            this.write
         );
      }

      void init(long allowance) {
         if (!this.init_Z) {
            if (!this.init_ArrayUtil_StreamUt.verifyStillValid()) {
               this.read = this.init_Z = true;
            } else {
               try {
                  this.load(allowance);
               } finally {
                  if (!this.init_ArrayUtil_StreamUt.verifyStillValid()) {
                     this.load_Z = false;
                     this.read = this.init_Z = true;
                  }
               }
            }
         }
      }

      private void load(long allowance) {
         long started = PrintCleanupAccess.this.init_UiHelper.view.workUnits();
         boolean dependencyOnly = this.init_I == 3;

         try (
            PrintView.Inner_TaskQueue capture = PrintCleanupAccess.this.init_UiHelper.view.openReadTracker();
            PrintView.Inner_StreamUtil.Inner_StreamUtil_2 geometry = this.init_ArrayUtil_StreamUt.attach();
         ) {
            try {
               for (int steps = 0; steps < 128 && PrintCleanupAccess.this.init_UiHelper.view.workUnits() - started < Math.max(1L, allowance); steps++) {
                  if (this.init_I == 3) {
                     if (this.init_LongIterator == null) {
                        this.init_LongIterator = this.init_LongOpenHashSet.iterator();
                     }

                     if (!this.init_LongIterator.hasNext()) {
                        this.load_Z = PrintCleanupAccess.this.init_UiHelper.load_Z_BlockPos(BlockPos.of(this.init_J));
                        this.init_Z = true;
                        return;
                     }

                     PrintCleanupAccess.this.init_UiHelper.view.getBlockState(BlockPos.of(this.init_LongIterator.nextLong()));
                  } else if (this.init_I == 0) {
                     BlockPos pos = BlockPos.of(this.init_J);
                     if (!PrintCleanupAccess.this.init_UiHelper.env.hasChunkAt(pos)
                        || !PrintCleanupAccess.this.init_UiHelper.temporaryBlocks.containsKey(this.init_J)
                        || !PrintFlight.isFlightPointSafe(PrintCleanupAccess.this.init_UiHelper.view, this.init_BufferHelper_DataU.point)
                        || !PrintCleanupAccess.this.init_UiHelper.load_Z_BlockPos(pos)) {
                        this.init_Z = true;
                        return;
                     }

                     PrintTarget target = new PrintTarget(
                        -1, pos, PrintCleanupAccess.this.init_UiHelper.view.getBlockState(pos), PrintTarget.Inner_DataUtil.PLACE, Items.AIR, null
                     );
                     if (PrintClicks.findClick(
                           PrintCleanupAccess.this.init_UiHelper.view,
                           PrintCleanupAccess.this.init_UiHelper.env,
                           PrintCleanupAccess.this.init_UiHelper.rules,
                           target,
                           Items.DIAMOND_PICKAXE,
                           PrintClicks.Inner_CachePool.DIG,
                           this.init_BufferHelper_DataU.point,
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
                     this.init_Vec3 = this.init_BufferHelper_DataU.point;
                  } else {
                     PrintFlight.Inner_BufferHelper route = this.init_I == 1 ? this.init_BufferHelper_DataU.before : this.init_BufferHelper_DataU.after;
                     boolean removed = this.init_I == 2;
                     if (removed) {
                        PrintCleanupAccess.this.init_UiHelper.view.pushHypothesis(BlockPos.of(this.init_J), Blocks.AIR.defaultBlockState());
                     }

                     try {
                        if (this.load_I < route.points().size()) {
                           Vec3 next = route.points().get(this.load_I++);
                           if (!PrintFlight.isFlightPointSafe(PrintCleanupAccess.this.init_UiHelper.view, this.init_Vec3)
                              || !PrintFlight.isFlightSegmentClear(PrintCleanupAccess.this.init_UiHelper.view, this.init_Vec3, next)) {
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
                           this.init_Vec3 = this.init_BufferHelper_DataU.point;
                        }
                     } finally {
                        if (removed) {
                           PrintCleanupAccess.this.init_UiHelper.view.popHypothesis(BlockPos.of(this.init_J));
                        }
                     }
                  }
               }
            } finally {
               this.write = this.write & capture.isComplete();

               for (Entry<BlockState> read : capture.recordedStates.long2ObjectEntrySet()) {
                  if (this.init_Long2ObjectOpenHas.size() >= 8192 && !this.init_Long2ObjectOpenHas.containsKey(read.getLongKey())) {
                     this.write = false;
                     break;
                  }

                  BlockState prior = this.init_Long2ObjectOpenHas.putIfAbsent(read.getLongKey(), read.getValue());
                  if (read.getValue() == PrintView.UNLOADED_SENTINEL || prior != null && prior != read.getValue()) {
                     this.load_Z = false;
                     this.read = this.init_Z = true;
                  }

                  if (!dependencyOnly && PrintCleanupAccess.init_Z_BlockState(read.getValue())) {
                     for (Direction direction : Direction.values()) {
                        if (this.init_LongOpenHashSet.size() >= 8192) {
                           this.write = false;
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
      final BlockPos init_BlockPos;
      final BlockState init_BlockState;
      final long init_J = PrintCleanupAccess.this.init_UiHelper.view.worldMutationRevision();
      final long load_J = PrintCleanupAccess.this.init_J;
      final long read_J = PrintCleanupAccess.this.update;
      final boolean init_Z = PrintCleanupAccess.this.init_UiHelper.env.isCreativeMode();
      final boolean load_Z;
      final PrintView.Inner_StreamUtil init_ArrayUtil_StreamUt;
      final Long2ObjectOpenHashMap<BlockState> init_Long2ObjectOpenHas;
      final LongOpenHashSet init_LongOpenHashSet;
      final LongOpenHashSet load_LongOpenHashSet;
      final LongArrayList candidateKeys;
      final boolean read_Z;
      LongIterator init_LongIterator;
      LongIterator load_LongIterator;
      int init_I;
      int candidateCursor;
      int stage;
      PrintCleanupAccess.Inner_StreamUtil currentEntry;
      PrintCleanupAccess.Inner_ConfigStore init_BufferHelper_Confi;
      PrintCleanupAccess.Inner_BufferHelper init_BufferHelper_Buffe;
      PrintCleanupAccess.Inner_DataUtil init_BufferHelper_DataU;
      boolean write;
      boolean update;
      boolean sync;
      boolean flush;

      Inner_CachePool(BlockPos position, BlockState state) {
         this.load_Z = PrintCleanupAccess.this.init_Long2ObjectOpenHas.isEmpty();
         this.init_ArrayUtil_StreamUt = PrintCleanupAccess.this.init_UiHelper.view.new Inner_StreamUtil();
         this.init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
         this.init_LongOpenHashSet = new LongOpenHashSet();
         this.load_LongOpenHashSet = new LongOpenHashSet();
         this.candidateKeys = new LongArrayList();
         this.init_BlockPos = position;
         this.init_BlockState = state;
         PrintCleanupAccess.this.init_UiHelper.view.copyHypothesesInto(this.init_Long2ObjectOpenHas);
         PrintCleanupAccess.this.init_UiHelper.view.copyOwnedTemporariesInto(this.init_LongOpenHashSet);
         this.read_Z = this.init_Long2ObjectOpenHas.isEmpty() && this.init_LongOpenHashSet.isEmpty();
         this.update = this.load_Z;
      }

      boolean init() {
         return !this.flush
            && this.load_J == PrintCleanupAccess.this.init_J
            && (
               this.load_Z
                  || this.init_J == PrintCleanupAccess.this.init_UiHelper.view.worldMutationRevision()
                     && this.read_J == PrintCleanupAccess.this.update
                     && this.init_Z == PrintCleanupAccess.this.init_UiHelper.env.isCreativeMode()
                     && PrintCleanupAccess.this.init_UiHelper.view.matchesHypotheses(this.init_Long2ObjectOpenHas)
                     && PrintCleanupAccess.this.init_UiHelper.view.matchesOwnedTemporaries(this.init_LongOpenHashSet)
                     && this.init_ArrayUtil_StreamUt.verifyStillValid()
            );
      }

      private boolean init(PrintCleanupAccess.Inner_DataUtil certificate) {
         for (long key : this.init_Long2ObjectOpenHas.keySet()) {
            if (certificate.reads.containsKey(key)) {
               return true;
            }
         }

         for (long key : this.init_LongOpenHashSet) {
            if (certificate.reads.containsKey(key)) {
               return true;
            }
         }

         return false;
      }

      void init(long allowance) {
         if (!this.init()) {
            this.flush = true;
         } else if (!this.update) {
            try (PrintView.Inner_StreamUtil.Inner_StreamUtil_2 geometry = this.init_ArrayUtil_StreamUt.attach()) {
               this.load(allowance);
            } finally {
               if (!this.init_ArrayUtil_StreamUt.verifyStillValid()) {
                  this.flush = true;
               }
            }
         }
      }

      private void load(long allowance) {
         long started = PrintCleanupAccess.this.init_UiHelper.view.workUnits();
         long deadline = System.nanoTime() + 2000000L;

         for (int steps = 0;
            steps < 128
               && PrintCleanupAccess.this.init_UiHelper.view.workUnits() - started < Math.max(1L, allowance)
               && (steps == 0 || System.nanoTime() < deadline);
            steps++
         ) {
            long left = Math.min(8192L, Math.max(1L, allowance - (PrintCleanupAccess.this.init_UiHelper.view.workUnits() - started)));
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
                           var10001 = (PrintCleanupAccess.this.sync != this.read_J
                                 ? PrintCleanupAccess.this.init_Long2ObjectOpenHas.keySet()
                                 : PrintCleanupAccess.this.init_LongOpenHashSet)
                              .iterator();
                           break;
                        case 1:
                           var10001 = PrintCleanupAccess.this.load_Long2ObjectOpenHas.getOrDefault(this.init_BlockPos.asLong(), new LongOpenHashSet()).iterator();
                           break;
                        case 2:
                           var10001 = PrintCleanupAccess.this.load_LongOpenHashSet.iterator();
                           break;
                        case 3:
                           var10001 = PrintCleanupAccess.this.read_LongOpenHashSet.iterator();
                           break;
                        case 4:
                           var10001 = this.init_Long2ObjectOpenHas.keySet().iterator();
                           break;
                        default:
                           var10001 = this.init_LongOpenHashSet.iterator();
                     }

                     this.init_LongIterator = var10001;
                  }

                  if (!this.init_LongIterator.hasNext()) {
                     this.init_LongIterator = null;
                     this.init_I++;
                  } else {
                     long key = this.init_LongIterator.nextLong();
                     if (this.init_I >= 4) {
                        LongOpenHashSet linked = PrintCleanupAccess.this.load_Long2ObjectOpenHas.get(key);
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
                     if (this.read_Z) {
                        PrintCleanupAccess.this.sync = this.read_J;
                     }

                     this.update = true;
                     return;
                  }

                  this.currentEntry = PrintCleanupAccess.this.init_Long2ObjectOpenHas.get(this.candidateKeys.getLong(this.candidateCursor++));
                  if (this.currentEntry == null
                     || !PrintCleanupAccess.this.init_Z_BufferHelper_Strea(this.currentEntry)
                     || this.currentEntry.cellKey == this.init_BlockPos.asLong()) {
                     this.currentEntry = null;
                     continue;
                  }

                  if (this.init_Z) {
                     this.currentEntry.init_Z = true;
                  }

                  this.init_BufferHelper_DataU = this.currentEntry.init_BufferHelper_DataU;
                  this.write = this.init_BufferHelper_DataU != null
                     && (
                        !this.init_BufferHelper_DataU.stable
                           || this.init_BufferHelper_DataU.reads.containsKey(this.init_BlockPos.asLong())
                           || this.init(this.init_BufferHelper_DataU)
                     );
                  if (!this.init_Z
                     && this.init_BufferHelper_DataU == null
                     && !this.currentEntry.init_Z
                     && this.currentEntry.read >= PrintCleanupAccess.this.write) {
                     this.currentEntry = null;
                     continue;
                  }

                  if (this.init_BufferHelper_DataU == null
                     && this.currentEntry.load == this.init_J
                     && !PrintCleanupAccess.this.init_LongOpenHashSet.contains(this.currentEntry.cellKey)) {
                     this.sync = this.update = true;
                     PrintCleanupAccess.this.init_UiHelper.cleanupCandidates.add(this.currentEntry.cellKey);
                     return;
                  }

                  this.stage = 0;
                  if (this.init_BufferHelper_DataU != null) {
                     this.init_BufferHelper_Buffe = PrintCleanupAccess.this.new Inner_BufferHelper(this.currentEntry.cellKey, this.init_BufferHelper_DataU);
                  } else {
                     this.init_BufferHelper_Confi = PrintCleanupAccess.this.new Inner_ConfigStore(this.currentEntry.cellKey, null);
                  }
               }

               if (this.stage == 0) {
                  if (this.init_BufferHelper_Buffe != null) {
                     this.init_BufferHelper_Buffe.init(left);
                     if (this.init_BufferHelper_Buffe.read) {
                        this.flush = true;
                        return;
                     }

                     if (!this.init_BufferHelper_Buffe.init_Z) {
                        continue;
                     }

                     if (this.init_BufferHelper_Buffe.load_Z) {
                        this.init_BufferHelper_DataU = this.init_BufferHelper_Buffe.init();
                     } else {
                        this.init_BufferHelper_Confi = PrintCleanupAccess.this.new Inner_ConfigStore(
                           this.currentEntry.cellKey, this.init_BufferHelper_DataU.point
                        );
                        this.init_BufferHelper_DataU = null;
                     }

                     this.init_BufferHelper_Buffe = null;
                  }

                  if (this.init_BufferHelper_Confi != null) {
                     this.init_BufferHelper_Confi.init(left);
                     if (this.init_BufferHelper_Confi.read_Z) {
                        this.flush = true;
                        return;
                     }

                     if (!this.init_BufferHelper_Confi.init_Z) {
                        continue;
                     }

                     this.init_BufferHelper_DataU = this.init_BufferHelper_Confi.init_BufferHelper_DataU;
                     if (this.init_BufferHelper_DataU == null) {
                        if (this.read_Z && !this.init_BufferHelper_Confi.load_Z) {
                           this.currentEntry.load = this.init_J;
                           PrintCleanupAccess.this.read_LongOpenHashSet.add(this.currentEntry.cellKey);
                           PrintCleanupAccess.this.init_LongOpenHashSet.remove(this.currentEntry.cellKey);
                        }

                        this.sync = this.update = true;
                        PrintCleanupAccess.this.init_UiHelper.cleanupCandidates.add(this.currentEntry.cellKey);
                        return;
                     }

                     this.init_BufferHelper_Confi = null;
                  }

                  if (this.read_Z) {
                     PrintCleanupAccess.this.init(this.currentEntry, this.init_BufferHelper_DataU);
                  }

                  if (PrintCleanupAccess.this.init_UiHelper.view.getBlockState(this.init_BlockPos) != this.init_BlockState
                     && (
                        this.init_BufferHelper_DataU.neighbors.contains(this.init_BlockPos.asLong())
                           || !this.init_BufferHelper_DataU.stable && this.init_BufferHelper_DataU.reads.size() >= 8192
                     )) {
                     this.sync = this.update = true;
                     PrintCleanupAccess.this.init_UiHelper.cleanupCandidates.add(this.currentEntry.cellKey);
                     return;
                  }

                  if (this.init_BufferHelper_DataU.stable && !this.init_BufferHelper_DataU.reads.containsKey(this.init_BlockPos.asLong())) {
                     this.currentEntry = null;
                  } else {
                     this.stage = 1;
                     this.init_BufferHelper_Buffe = PrintCleanupAccess.this.new Inner_BufferHelper(this.currentEntry.cellKey, this.init_BufferHelper_DataU);
                  }
               } else {
                  PrintCleanupAccess.this.init_UiHelper.view.pushHypothesis(this.init_BlockPos, this.init_BlockState);

                  try {
                     if (this.init_BufferHelper_Buffe != null) {
                        this.init_BufferHelper_Buffe.init(left);
                        if (!this.init_BufferHelper_Buffe.read) {
                           if (this.init_BufferHelper_Buffe.init_Z) {
                              if (this.init_BufferHelper_Buffe.load_Z) {
                                 this.init_BufferHelper_Buffe = null;
                                 this.currentEntry = null;
                              } else {
                                 this.init_BufferHelper_Buffe = null;
                                 this.init_BufferHelper_Confi = PrintCleanupAccess.this.new Inner_ConfigStore(
                                    this.currentEntry.cellKey, this.init_BufferHelper_DataU.point
                                 );
                              }
                           }
                           continue;
                        }

                        this.flush = true;
                        return;
                     }

                     this.init_BufferHelper_Confi.init(left);
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
                     PrintCleanupAccess.this.init_UiHelper.cleanupCandidates.add(this.currentEntry.cellKey);
                  } finally {
                     PrintCleanupAccess.this.init_UiHelper.view.popHypothesis(this.init_BlockPos);
                  }

                  return;
               }
            }
         }
      }
   }

   final class Inner_ConfigStore {
      final long init_J;
      final PrintTarget init_ListPool;
      final ArrayList<Long> init_ArrayList = new ArrayList<>();
      final PrintClicks.Inner_DataUtil init_ConfigStore_DataUt = new PrintClicks.Inner_DataUtil(18000);
      List<Vec3> init_List;
      Vec3 init_Vec3;
      PrintCore.h init_UiHelper_h;
      PrintCore.h load_UiHelper_h;
      PrintCleanupAccess.Inner_BufferHelper init_BufferHelper_Buffe;
      int init_I;
      int load_I;
      int read_I;
      final PrintView.Inner_StreamUtil init_ArrayUtil_StreamUt = PrintCleanupAccess.this.init_UiHelper.view.new Inner_StreamUtil();
      boolean init_Z;
      boolean load_Z;
      boolean read_Z;
      boolean write;
      PrintCleanupAccess.Inner_DataUtil init_BufferHelper_DataU;

      Inner_ConfigStore(long key, Vec3 preferred) {
         this.init_J = key;
         BlockPos pos = BlockPos.of(key);
         this.init_ListPool = new PrintTarget(
            -1, pos, PrintCleanupAccess.this.init_UiHelper.view.getBlockState(pos), PrintTarget.Inner_DataUtil.PLACE, Items.AIR, null
         );
         this.init_ArrayList.addAll(PrintFlightWorkstations.init(pos));
         this.init_ArrayList.sort(Comparator.<Long>comparingDouble(k -> BlockPos.of(k).distToCenterSqr(Vec3.atCenterOf(pos))).thenComparingLong(k -> k));
         if (preferred == null) {
            PrintCore.at recovery = PrintCleanupAccess.this.init_UiHelper.recoveries.get(key);
            if (recovery != null) {
               preferred = recovery.work();
            }
         }

         if (preferred != null) {
            this.init_List = List.of(preferred);
         }

         PrintCleanupAccess.this.load_J++;
      }

      void init(long allowance) {
         if (!this.init_ArrayUtil_StreamUt.verifyStillValid()) {
            this.read_Z = this.init_Z = true;
         } else {
            long transientBefore = PrintCleanupAccess.this.init_UiHelper.view.untrackedReadCount();

            try (PrintView.Inner_StreamUtil.Inner_StreamUtil_2 geometry = this.init_ArrayUtil_StreamUt.attach()) {
               this.load(allowance);
            } finally {
               this.load_Z = this.load_Z | PrintCleanupAccess.this.init_UiHelper.view.untrackedReadCount() != transientBefore;
               if (!this.init_ArrayUtil_StreamUt.verifyStillValid()) {
                  this.read_Z = this.init_Z = true;
                  this.init_BufferHelper_DataU = null;
               }
            }
         }
      }

      private void load(long allowance) {
         long started = PrintCleanupAccess.this.init_UiHelper.view.workUnits();
         if (!this.write) {
            this.write = true;
            if (!PrintCleanupAccess.this.init_UiHelper.load_Z_BlockPos(this.init_ListPool.pos())) {
               this.init_Z = true;
               return;
            }
         }

         for (int steps = 0; !this.init_Z && steps < 128 && PrintCleanupAccess.this.init_UiHelper.view.workUnits() - started < Math.max(1L, allowance); steps++) {
            long left = Math.max(1L, allowance - (PrintCleanupAccess.this.init_UiHelper.view.workUnits() - started));
            if (this.init_BufferHelper_Buffe != null) {
               this.init_BufferHelper_Buffe.init(left);
               if (this.init_BufferHelper_Buffe.read) {
                  this.read_Z = this.init_Z = true;
                  return;
               }

               if (!this.init_BufferHelper_Buffe.init_Z) {
                  return;
               }

               if (this.init_BufferHelper_Buffe.load_Z) {
                  this.init_BufferHelper_DataU = this.init_BufferHelper_Buffe.init();
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
                  if (this.init_I >= this.init_ArrayList.size() || this.init_ConfigStore_DataUt.isExhausted()) {
                     this.load_Z = this.load_Z | this.init_ConfigStore_DataUt.isExhausted();
                     this.init_Z = true;
                     return;
                  }

                  long at = this.init_ArrayList.get(this.init_I++);
                  if (PrintCleanupAccess.this.init_UiHelper.view.getBlockState(BlockPos.of(at)) == PrintView.UNLOADED_SENTINEL) {
                     this.load_Z = true;
                  }

                  this.init_List = PrintFlightWorkstations.init(PrintCleanupAccess.this.init_UiHelper.view, this.init_ListPool, PrintClicks.Inner_CachePool.DIG, at);
                  this.load_I = 0;
                  continue;
               }

               this.init_Vec3 = this.init_List.get(this.load_I++);
               if (!PrintFlight.isFlightPointSafe(PrintCleanupAccess.this.init_UiHelper.view, this.init_Vec3)) {
                  this.init_Vec3 = null;
                  continue;
               }

               PrintClicks.Inner_StreamUtil click = PrintClicks.findClick(
                  PrintCleanupAccess.this.init_UiHelper.view,
                  PrintCleanupAccess.this.init_UiHelper.env,
                  PrintCleanupAccess.this.init_UiHelper.rules,
                  this.init_ListPool,
                  Items.DIAMOND_PICKAXE,
                  PrintClicks.Inner_CachePool.DIG,
                  this.init_Vec3,
                  false,
                  false,
                  this.init_ConfigStore_DataUt,
                  4.1000000000000005
               );
               if (click == null) {
                  this.init_Vec3 = null;
                  continue;
               }

               this.init_UiHelper_h = PrintCleanupAccess.this.init_UiHelper.new h(this.init_Vec3);
               this.read_I = 1;
            }

            if (this.read_I == 1) {
               this.init_UiHelper_h.init(left);
               if (!this.init_UiHelper_h.done) {
                  return;
               }

               if (this.init_UiHelper_h.result == null) {
                  this.load_Z = this.load_Z | this.init(this.init_UiHelper_h);
                  this.init_Vec3 = null;
                  this.init_UiHelper_h = null;
               } else {
                  this.read_I = 2;
               }
            } else {
               PrintCleanupAccess.this.init_UiHelper.view.pushHypothesis(this.init_ListPool.pos(), Blocks.AIR.defaultBlockState());

               try {
                  if (!PrintFlight.isFlightPointSafe(PrintCleanupAccess.this.init_UiHelper.view, this.init_Vec3)) {
                     this.init_Vec3 = null;
                     this.init_UiHelper_h = this.load_UiHelper_h = null;
                     this.read_I = 0;
                     continue;
                  }

                  if (this.load_UiHelper_h == null) {
                     this.load_UiHelper_h = PrintCleanupAccess.this.init_UiHelper.new h(this.init_Vec3);
                  }

                  this.load_UiHelper_h.init(left);
                  if (!this.load_UiHelper_h.done) {
                     return;
                  }
               } finally {
                  PrintCleanupAccess.this.init_UiHelper.view.popHypothesis(this.init_ListPool.pos());
               }

               if (this.load_UiHelper_h.result != null) {
                  this.init_BufferHelper_Buffe = PrintCleanupAccess.this.new Inner_BufferHelper(
                     this.init_J,
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
                  this.load_Z = this.load_Z | this.init(this.load_UiHelper_h);
                  this.init_Vec3 = null;
                  this.init_UiHelper_h = this.load_UiHelper_h = null;
                  this.read_I = 0;
               }
            }
         }
      }

      private boolean init(PrintCore.h exit) {
         return exit.search != null
            && exit.search.result == null
            && !exit.search.openQueue.isEmpty()
            && exit.search.expandedNodes >= exit.search.maxExpansions;
      }
   }

   record Inner_DataUtil(
      Vec3 point,
      PrintFlight.Inner_BufferHelper before,
      PrintFlight.Inner_BufferHelper after,
      Long2ObjectOpenHashMap<BlockState> reads,
      LongOpenHashSet neighbors,
      boolean stable
   ) {
   }

   static final class Inner_StreamUtil {
      final long cellKey;
      int init_I;
      PrintCleanupAccess.Inner_DataUtil init_BufferHelper_DataU;
      long load = -9223372036854775808L;
      long read;
      boolean init_Z;

      Inner_StreamUtil(long key, int owner) {
         this.cellKey = key;
         this.init_I = owner;
      }
   }
}
