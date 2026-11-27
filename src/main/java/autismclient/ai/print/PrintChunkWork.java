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
   final Long2ObjectLinkedOpenHashMap<PrintChunkWork.Inner_StreamUtil> chunks = new Long2ObjectLinkedOpenHashMap<>();
   boolean init_Z;
   int init_I;
   int load;
   private int read = -1;

   PrintChunkWork(PrintCore core) {
      this.core = core;
   }

   PrintChunkWork(PrintCore core, Long2ObjectMap<IntArrayList> groups) {
      this(core);
      long[] keys = groups.keySet().toLongArray();
      Arrays.sort(keys);

      for (long key : keys) {
         PrintChunkWork.Inner_StreamUtil chunk = new PrintChunkWork.Inner_StreamUtil(key, groups.get(key));
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
      PrintTarget.Inner_DataUtil kind = this.core.targets[i].kind();
      return (state == 0 || state == 2 || state == 1) && kind != PrintTarget.Inner_DataUtil.FLOW && kind != PrintTarget.Inner_DataUtil.PARTNER;
   }

   void init(int i, byte before, byte after) {
      if (this.core.phase != PrintCore.J.VERIFY) {
         this.resetAudit();
      }

      PrintChunkWork.Inner_StreamUtil chunk = this.chunks.get(chunkKey(this.core.targets[i].pos()));
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
         || i != this.read && chunk.read > 0 && Arrays.binarySearch(chunk.cells.elements(), 0, chunk.cells.size(), i) < chunk.read) {
         chunk.init_V();
      }
   }

   void resetAudit() {
      this.init_Z = false;
   }

   boolean init_Z_CachePool_StreamUt(PrintChunkWork.Inner_StreamUtil chunk) {
      return this.core.env.hasChunkAt(this.core.targets[chunk.cells.getInt(0)].pos());
   }

   void markChunkDirty(long key) {
      PrintChunkWork.Inner_StreamUtil chunk = this.chunks.get(key);
      if (chunk != null && this.init_Z_CachePool_StreamUt(chunk)) {
         chunk.init_V();
      }
   }

   void load() {
      for (PrintChunkWork.Inner_StreamUtil chunk : this.chunks.values()) {
         if (this.init_Z_CachePool_StreamUt(chunk)) {
            chunk.init_V();
         }
      }
   }

   boolean hasPendingChunks() {
      for (PrintChunkWork.Inner_StreamUtil chunk : this.chunks.values()) {
         if (chunk.load > 0 && this.init_Z_CachePool_StreamUt(chunk)) {
            return true;
         }
      }

      return false;
   }

   PrintChunkWork.Inner_StreamUtil nearestChunk(Vec3 position, boolean verification) {
      PrintChunkWork.Inner_StreamUtil best = null;

      for (PrintChunkWork.Inner_StreamUtil chunk : this.chunks.values()) {
         if (!this.init_Z_CachePool_StreamUt(chunk)
            && (verification ? !this.init_Z || !chunk.init_Z : chunk.load != 0)
            && (best == null || chunk.init(position) < best.init(position) || chunk.init(position) == best.init(position) && chunk.chunkKey < best.chunkKey)) {
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

      for (PrintChunkWork.Inner_StreamUtil chunk : this.chunks.values()) {
         if (chunk.init_Z) {
            count++;
         }
      }

      return count;
   }

   PrintChunkWork.Inner_DataUtil auditChunks(IntConsumer refresh, int budget) {
      if (!this.init_Z) {
         this.init_Z = true;
         this.load++;

         for (PrintChunkWork.Inner_StreamUtil chunk : this.chunks.values()) {
            chunk.init_V();
         }
      }

      for (PrintChunkWork.Inner_StreamUtil chunk : this.chunks.values()) {
         if (!chunk.init_Z && this.init_Z_CachePool_StreamUt(chunk)) {
            while (chunk.read < chunk.cells.size() && budget-- > 0) {
               int i = chunk.cells.getInt(chunk.read++);
               this.read = i;

               try {
                  refresh.accept(i);
               } finally {
                  this.read = -1;
               }

               this.init_I++;
               if (this.core.cellStates[i] != 3) {
                  if (this.core.targets[i].kind() == PrintTarget.Inner_DataUtil.FLOW) {
                     chunk.sync++;
                  } else if (this.core.cellStates[i] == 5) {
                     chunk.update++;
                  } else {
                     chunk.write++;
                  }
               }
            }

            if (chunk.read == chunk.cells.size()) {
               if (chunk.write + chunk.update + chunk.sync > 0) {
                  PrintChunkWork.Inner_DataUtil result = new PrintChunkWork.Inner_DataUtil(false, chunk.write, chunk.update, chunk.sync);
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

      return new PrintChunkWork.Inner_DataUtil(this.verifiedChunkCount() == this.chunks.size(), 0, 0, 0);
   }

   record Inner_DataUtil(boolean complete, int missing, int failed, int flowing) {
   }

   static final class Inner_StreamUtil {
      final long chunkKey;
      final IntArrayList cells;
      int unfinishedCells;
      int load;
      int read;
      int write;
      int update;
      int sync;
      boolean init_Z;

      Inner_StreamUtil(long key) {
         this(key, new IntArrayList());
      }

      Inner_StreamUtil(long key, IntArrayList cells) {
         this.chunkKey = key;
         this.cells = cells;
      }

      int minBlockX() {
         return (int)(this.chunkKey >> 32) * 16;
      }

      int load() {
         return (int)this.chunkKey * 16;
      }

      double init(Vec3 p) {
         double dx = Math.max(Math.max(this.minBlockX() - p.x, p.x - (this.minBlockX() + 15)), 0.0);
         double dz = Math.max(Math.max(this.load() - p.z, p.z - (this.load() + 15)), 0.0);
         return dx * dx + dz * dz;
      }

      void init_V() {
         this.init_Z = false;
         this.read = this.write = this.update = this.sync = 0;
      }
   }
}
