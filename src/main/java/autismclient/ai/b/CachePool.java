package autismclient.ai.b;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Arrays;
import java.util.function.IntConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

final class CachePool {
   private final UiHelper init_UiHelper;
   final Long2ObjectLinkedOpenHashMap<CachePool.Inner_StreamUtil> init_Long2ObjectLinkedO = new Long2ObjectLinkedOpenHashMap<>();
   boolean init_Z;
   int init_I;
   int load;
   private int read = -1;

   CachePool(UiHelper core) {
      this.init_UiHelper = core;
   }

   CachePool(UiHelper core, Long2ObjectMap<IntArrayList> groups) {
      this(core);
      long[] keys = groups.keySet().toLongArray();
      Arrays.sort(keys);

      for (long key : keys) {
         CachePool.Inner_StreamUtil chunk = new CachePool.Inner_StreamUtil(key, groups.get(key));
         this.init_Long2ObjectLinkedO.put(key, chunk);

         for (int i : chunk.init_IntArrayList) {
            if (core.init_BArr[i] != 3) {
               chunk.init_I++;
            }

            if (this.init_Z_I_B(i, core.init_BArr[i])) {
               chunk.load++;
            }
         }
      }
   }

   static long init_J_BlockPos(BlockPos pos) {
      return (long)(pos.getX() >> 4) << 32 | pos.getZ() >> 4 & 4294967295L;
   }

   private boolean init_Z_I_B(int i, byte state) {
      ListPool.Inner_DataUtil kind = this.init_UiHelper.init_ListPoolArr[i].kind();
      return (state == 0 || state == 2 || state == 1) && kind != ListPool.Inner_DataUtil.write && kind != ListPool.Inner_DataUtil.load;
   }

   void init(int i, byte before, byte after) {
      if (this.init_UiHelper.init_UiHelper_J != UiHelper.J.write) {
         this.init_V();
      }

      CachePool.Inner_StreamUtil chunk = this.init_Long2ObjectLinkedO.get(init_J_BlockPos(this.init_UiHelper.init_ListPoolArr[i].pos()));
      if (before != 3) {
         chunk.init_I--;
      }

      if (after != 3) {
         chunk.init_I++;
      }

      if (this.init_Z_I_B(i, before)) {
         chunk.load--;
      }

      if (this.init_Z_I_B(i, after)) {
         chunk.load++;
      }

      if (chunk.init_Z
         || i != this.read && chunk.read > 0 && Arrays.binarySearch(chunk.init_IntArrayList.elements(), 0, chunk.init_IntArrayList.size(), i) < chunk.read) {
         chunk.init_V();
      }
   }

   void init_V() {
      this.init_Z = false;
   }

   boolean init_Z_CachePool_StreamUt(CachePool.Inner_StreamUtil chunk) {
      return this.init_UiHelper.init_EventBus.load_Z(this.init_UiHelper.init_ListPoolArr[chunk.init_IntArrayList.getInt(0)].pos());
   }

   void init_V_J(long key) {
      CachePool.Inner_StreamUtil chunk = this.init_Long2ObjectLinkedO.get(key);
      if (chunk != null && this.init_Z_CachePool_StreamUt(chunk)) {
         chunk.init_V();
      }
   }

   void load() {
      for (CachePool.Inner_StreamUtil chunk : this.init_Long2ObjectLinkedO.values()) {
         if (this.init_Z_CachePool_StreamUt(chunk)) {
            chunk.init_V();
         }
      }
   }

   boolean init_Z() {
      for (CachePool.Inner_StreamUtil chunk : this.init_Long2ObjectLinkedO.values()) {
         if (chunk.load > 0 && this.init_Z_CachePool_StreamUt(chunk)) {
            return true;
         }
      }

      return false;
   }

   CachePool.Inner_StreamUtil init_CachePool_StreamUt_Vec3_Z(Vec3 position, boolean verification) {
      CachePool.Inner_StreamUtil best = null;

      for (CachePool.Inner_StreamUtil chunk : this.init_Long2ObjectLinkedO.values()) {
         if (!this.init_Z_CachePool_StreamUt(chunk)
            && (verification ? !this.init_Z || !chunk.init_Z : chunk.load != 0)
            && (best == null || chunk.init(position) < best.init(position) || chunk.init(position) == best.init(position) && chunk.init_J < best.init_J)) {
            best = chunk;
         }
      }

      return best;
   }

   int init_I() {
      if (!this.init_Z) {
         return 0;
      }

      int count = 0;

      for (CachePool.Inner_StreamUtil chunk : this.init_Long2ObjectLinkedO.values()) {
         if (chunk.init_Z) {
            count++;
         }
      }

      return count;
   }

   CachePool.Inner_DataUtil init_CachePool_DataUtil_IntConsumer_I(IntConsumer refresh, int budget) {
      if (!this.init_Z) {
         this.init_Z = true;
         this.load++;

         for (CachePool.Inner_StreamUtil chunk : this.init_Long2ObjectLinkedO.values()) {
            chunk.init_V();
         }
      }

      for (CachePool.Inner_StreamUtil chunk : this.init_Long2ObjectLinkedO.values()) {
         if (!chunk.init_Z && this.init_Z_CachePool_StreamUt(chunk)) {
            while (chunk.read < chunk.init_IntArrayList.size() && budget-- > 0) {
               int i = chunk.init_IntArrayList.getInt(chunk.read++);
               this.read = i;

               try {
                  refresh.accept(i);
               } finally {
                  this.read = -1;
               }

               this.init_I++;
               if (this.init_UiHelper.init_BArr[i] != 3) {
                  if (this.init_UiHelper.init_ListPoolArr[i].kind() == ListPool.Inner_DataUtil.write) {
                     chunk.sync++;
                  } else if (this.init_UiHelper.init_BArr[i] == 5) {
                     chunk.update++;
                  } else {
                     chunk.write++;
                  }
               }
            }

            if (chunk.read == chunk.init_IntArrayList.size()) {
               if (chunk.write + chunk.update + chunk.sync > 0) {
                  CachePool.Inner_DataUtil result = new CachePool.Inner_DataUtil(false, chunk.write, chunk.update, chunk.sync);
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

      return new CachePool.Inner_DataUtil(this.init_I() == this.init_Long2ObjectLinkedO.size(), 0, 0, 0);
   }

   record Inner_DataUtil(boolean complete, int missing, int failed, int flowing) {
   }

   static final class Inner_StreamUtil {
      final long init_J;
      final IntArrayList init_IntArrayList;
      int init_I;
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
         this.init_J = key;
         this.init_IntArrayList = cells;
      }

      int init_I() {
         return (int)(this.init_J >> 32) * 16;
      }

      int load() {
         return (int)this.init_J * 16;
      }

      double init(Vec3 p) {
         double dx = Math.max(Math.max(this.init_I() - p.x, p.x - (this.init_I() + 15)), 0.0);
         double dz = Math.max(Math.max(this.load() - p.z, p.z - (this.load() + 15)), 0.0);
         return dx * dx + dz * dz;
      }

      void init_V() {
         this.init_Z = false;
         this.read = this.write = this.update = this.sync = 0;
      }
   }
}
