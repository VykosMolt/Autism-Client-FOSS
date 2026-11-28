package autismclient.ai.print;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Arrays;
import java.util.function.IntConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

final class PrintChunkWork {
   private final PrintCore core;
   final Long2ObjectLinkedOpenHashMap<PrintChunkWork.ChunkEntry> chunks = new Long2ObjectLinkedOpenHashMap<>();
   boolean auditStarted;
   int auditedCells;
   int unusedVisits;
   private int lastIndex = -1;

   PrintChunkWork(PrintCore core) {
      this.core = core;
   }

   PrintChunkWork(PrintCore core, Long2ObjectMap<IntArrayList> groups) {
      this(core);
      long[] keys = groups.keySet().toLongArray();
      Arrays.sort(keys);

      for (long key : keys) {
         PrintChunkWork.ChunkEntry chunk = new PrintChunkWork.ChunkEntry(key, groups.get(key));
         this.chunks.put(key, chunk);

         for (int i : chunk.cells) {
            if (core.cellStates[i] != 3) {
               chunk.unfinishedCells++;
            }

            if (this.countsAsPending(i, core.cellStates[i])) {
               chunk.pendingCells++;
            }
         }
      }
   }

   static long chunkKey(BlockPos pos) {
      return (long)(pos.getX() >> 4) << 32 | pos.getZ() >> 4 & 4294967295L;
   }

   private boolean countsAsPending(int i, byte state) {
      PrintTarget.TargetKind kind = this.core.targets[i].kind();
      return (state == 0 || state == 2 || state == 1) && kind != PrintTarget.TargetKind.FLOW && kind != PrintTarget.TargetKind.PARTNER;
   }

   void onCellChanged(int i, byte before, byte after) {
      if (this.core.phase != PrintCore.PrintPhase.VERIFY) {
         this.resetAudit();
      }

      PrintChunkWork.ChunkEntry chunk = this.chunks.get(chunkKey(this.core.targets[i].pos()));
      if (before != 3) {
         chunk.unfinishedCells--;
      }

      if (after != 3) {
         chunk.unfinishedCells++;
      }

      if (this.countsAsPending(i, before)) {
         chunk.pendingCells--;
      }

      if (this.countsAsPending(i, after)) {
         chunk.pendingCells++;
      }

      if (chunk.verified
         || i != this.lastIndex && chunk.scanned > 0 && Arrays.binarySearch(chunk.cells.elements(), 0, chunk.cells.size(), i) < chunk.scanned) {
         chunk.resetScan();
      }
   }

   void resetAudit() {
      this.auditStarted = false;
   }

   boolean isLoaded(PrintChunkWork.ChunkEntry chunk) {
      return this.core.env.hasChunkAt(this.core.targets[chunk.cells.getInt(0)].pos());
   }

   void markChunkDirty(long key) {
      PrintChunkWork.ChunkEntry chunk = this.chunks.get(key);
      if (chunk != null && this.isLoaded(chunk)) {
         chunk.resetScan();
      }
   }

   void rescanLoadedChunks() {
      for (PrintChunkWork.ChunkEntry chunk : this.chunks.values()) {
         if (this.isLoaded(chunk)) {
            chunk.resetScan();
         }
      }
   }

   boolean hasPendingChunks() {
      for (PrintChunkWork.ChunkEntry chunk : this.chunks.values()) {
         if (chunk.pendingCells > 0 && this.isLoaded(chunk)) {
            return true;
         }
      }

      return false;
   }

   PrintChunkWork.ChunkEntry nearestChunk(Vec3 position, boolean verification) {
      PrintChunkWork.ChunkEntry best = null;

      for (PrintChunkWork.ChunkEntry chunk : this.chunks.values()) {
         if (!this.isLoaded(chunk)
            && (verification ? !this.auditStarted || !chunk.verified : chunk.pendingCells != 0)
            && (best == null || chunk.distSqTo(position) < best.distSqTo(position) || chunk.distSqTo(position) == best.distSqTo(position) && chunk.chunkKey < best.chunkKey)) {
            best = chunk;
         }
      }

      return best;
   }

   int verifiedChunkCount() {
      if (!this.auditStarted) {
         return 0;
      }

      int count = 0;

      for (PrintChunkWork.ChunkEntry chunk : this.chunks.values()) {
         if (chunk.verified) {
            count++;
         }
      }

      return count;
   }

   PrintChunkWork.ChunkAudit auditChunks(IntConsumer refresh, int budget) {
      if (!this.auditStarted) {
         this.auditStarted = true;
         this.unusedVisits++;

         for (PrintChunkWork.ChunkEntry chunk : this.chunks.values()) {
            chunk.resetScan();
         }
      }

      for (PrintChunkWork.ChunkEntry chunk : this.chunks.values()) {
         if (!chunk.verified && this.isLoaded(chunk)) {
            while (chunk.scanned < chunk.cells.size() && budget-- > 0) {
               int i = chunk.cells.getInt(chunk.scanned++);
               this.lastIndex = i;

               try {
                  refresh.accept(i);
               } finally {
                  this.lastIndex = -1;
               }

               this.auditedCells++;
               if (this.core.cellStates[i] != 3) {
                  if (this.core.targets[i].kind() == PrintTarget.TargetKind.FLOW) {
                     chunk.flowCells++;
                  } else if (this.core.cellStates[i] == 5) {
                     chunk.failed++;
                  } else {
                     chunk.missing++;
                  }
               }
            }

            if (chunk.scanned == chunk.cells.size()) {
               if (chunk.missing + chunk.failed + chunk.flowCells > 0) {
                  PrintChunkWork.ChunkAudit result = new PrintChunkWork.ChunkAudit(false, chunk.missing, chunk.failed, chunk.flowCells);
                  chunk.resetScan();
                  return result;
               }

               chunk.verified = true;
            }

            if (budget <= 0) {
               break;
            }
         }
      }

      return new PrintChunkWork.ChunkAudit(this.verifiedChunkCount() == this.chunks.size(), 0, 0, 0);
   }

   record ChunkAudit(boolean complete, int missing, int failed, int flowing) {
   }

   static final class ChunkEntry {
      final long chunkKey;
      final IntArrayList cells;
      int unfinishedCells;
      int pendingCells;
      int scanned;
      int missing;
      int failed;
      int flowCells;
      boolean verified;

      ChunkEntry(long key) {
         this(key, new IntArrayList());
      }

      ChunkEntry(long key, IntArrayList cells) {
         this.chunkKey = key;
         this.cells = cells;
      }

      int minBlockX() {
         return (int)(this.chunkKey >> 32) * 16;
      }

      int minBlockZ() {
         return (int)this.chunkKey * 16;
      }

      double distSqTo(Vec3 p) {
         double dx = Math.max(Math.max(this.minBlockX() - p.x, p.x - (this.minBlockX() + 15)), 0.0);
         double dz = Math.max(Math.max(this.minBlockZ() - p.z, p.z - (this.minBlockZ() + 15)), 0.0);
         return dx * dx + dz * dz;
      }

      void resetScan() {
         this.verified = false;
         this.scanned = this.missing = this.failed = this.flowCells = 0;
      }
   }
}
