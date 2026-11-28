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
   boolean init_Z;
   int init_I;
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

            if (this.init_Z_I_B(i, core.cellStates[i])) {
               chunk.load++;
            }
         }
      }
   }

   static long chunkKey(BlockPos pos) {
      return (long)(pos.getX() >> 4) << 32 | pos.getZ() >> 4 & 4294967295L;
   }

   private boolean init_Z_I_B(int i, byte state) {
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

      if (this.init_Z_I_B(i, before)) {
         chunk.load--;
      }

      if (this.init_Z_I_B(i, after)) {
         chunk.load++;
      }

      if (chunk.init_Z
         || i != this.lastIndex && chunk.scanned > 0 && Arrays.binarySearch(chunk.cells.elements(), 0, chunk.cells.size(), i) < chunk.scanned) {
         chunk.init_V();
      }
   }

   void resetAudit() {
      this.init_Z = false;
   }

   boolean init_Z_CachePool_StreamUt(PrintChunkWork.ChunkEntry chunk) {
      return this.core.env.hasChunkAt(this.core.targets[chunk.cells.getInt(0)].pos());
   }

   void markChunkDirty(long key) {
      PrintChunkWork.ChunkEntry chunk = this.chunks.get(key);
      if (chunk != null && this.init_Z_CachePool_StreamUt(chunk)) {
         chunk.init_V();
      }
   }

   void load() {
      for (PrintChunkWork.ChunkEntry chunk : this.chunks.values()) {
         if (this.init_Z_CachePool_StreamUt(chunk)) {
            chunk.init_V();
         }
      }
   }

   boolean hasPendingChunks() {
      for (PrintChunkWork.ChunkEntry chunk : this.chunks.values()) {
         if (chunk.load > 0 && this.init_Z_CachePool_StreamUt(chunk)) {
            return true;
         }
      }

      return false;
   }

   PrintChunkWork.ChunkEntry nearestChunk(Vec3 position, boolean verification) {
      PrintChunkWork.ChunkEntry best = null;

      for (PrintChunkWork.ChunkEntry chunk : this.chunks.values()) {
         if (!this.init_Z_CachePool_StreamUt(chunk)
            && (verification ? !this.init_Z || !chunk.init_Z : chunk.load != 0)
            && (best == null || chunk.distSqTo(position) < best.distSqTo(position) || chunk.distSqTo(position) == best.distSqTo(position) && chunk.chunkKey < best.chunkKey)) {
            best = chunk;
         }
      }

      return best;
   }

   int verifiedChunkCount() {
      if (!this.init_Z) {
         return 0;
      }

      int count = 0;

      for (PrintChunkWork.ChunkEntry chunk : this.chunks.values()) {
         if (chunk.init_Z) {
            count++;
         }
      }

      return count;
   }

   PrintChunkWork.ChunkAudit auditChunks(IntConsumer refresh, int budget) {
      if (!this.init_Z) {
         this.init_Z = true;
         this.unusedVisits++;

         for (PrintChunkWork.ChunkEntry chunk : this.chunks.values()) {
            chunk.init_V();
         }
      }

      for (PrintChunkWork.ChunkEntry chunk : this.chunks.values()) {
         if (!chunk.init_Z && this.init_Z_CachePool_StreamUt(chunk)) {
            while (chunk.scanned < chunk.cells.size() && budget-- > 0) {
               int i = chunk.cells.getInt(chunk.scanned++);
               this.lastIndex = i;

               try {
                  refresh.accept(i);
               } finally {
                  this.lastIndex = -1;
               }

               this.init_I++;
               if (this.core.cellStates[i] != 3) {
                  if (this.core.targets[i].kind() == PrintTarget.TargetKind.FLOW) {
                     chunk.flowCells++;
                  } else if (this.core.cellStates[i] == 5) {
                     chunk.update++;
                  } else {
                     chunk.write++;
                  }
               }
            }

            if (chunk.scanned == chunk.cells.size()) {
               if (chunk.write + chunk.update + chunk.flowCells > 0) {
                  PrintChunkWork.ChunkAudit result = new PrintChunkWork.ChunkAudit(false, chunk.write, chunk.update, chunk.flowCells);
                  chunk.init_V();
                  return result;
               }

               chunk.init_Z = true;
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
      int load;
      int scanned;
      int write;
      int update;
      int flowCells;
      boolean init_Z;

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

      void init_V() {
         this.init_Z = false;
         this.scanned = this.write = this.update = this.flowCells = 0;
      }
   }
}
