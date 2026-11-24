package autismclient.ai.b;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.util.LinkedHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

final class FileCache {
   static final int init_I = 128;
   private final LinkedHashMap<FileCache.Inner_DataUtil, Long2ObjectOpenHashMap<BlockState>> init_LinkedHashMap = new LinkedHashMap<>();

   int init_I() {
      return this.init_LinkedHashMap.size();
   }

   void init_V() {
      this.init_LinkedHashMap.clear();
   }

   boolean init(ArrayUtil view, long from, double fromFeet, long to, double toFeet) {
      if (!view.load_Z()
         && Double.isFinite(fromFeet)
         && Double.isFinite(toFeet)
         && Math.abs(BlockPos.getX(from) - BlockPos.getX(to)) <= 1
         && Math.abs(BlockPos.getZ(from) - BlockPos.getZ(to)) <= 1
         && from != to
         && !(Math.abs(fromFeet - toFeet) > 3.0)) {
         try (ArrayUtil.Inner_TaskQueue reads = view.init_ArrayUtil_TaskQueu()) {
            int minY = ArrayUtil.init_I_D(Math.min(fromFeet, toFeet)) - 1;
            int maxY = (int)Math.ceil(Math.max(fromFeet, toFeet) + 1.8 + 1.25);

            for (int x = Math.min(BlockPos.getX(from), BlockPos.getX(to)) - 1; x <= Math.max(BlockPos.getX(from), BlockPos.getX(to)) + 1; x++) {
               for (int z = Math.min(BlockPos.getZ(from), BlockPos.getZ(to)) - 1; z <= Math.max(BlockPos.getZ(from), BlockPos.getZ(to)) + 1; z++) {
                  for (int y = minY; y <= maxY; y++) {
                     view.init_List_BlockPos(new BlockPos(x, y, z));
                  }
               }
            }

            if (!reads.init_Z()) {
               return false;
            }

            FileCache.Inner_DataUtil edge = FileCache.Inner_DataUtil.of(from, fromFeet, to, toFeet);
            if (!this.init_LinkedHashMap.containsKey(edge) && this.init_LinkedHashMap.size() >= 128) {
               this.init_LinkedHashMap.remove(this.init_LinkedHashMap.keySet().iterator().next());
            }

            this.init_LinkedHashMap.put(edge, new Long2ObjectOpenHashMap<>(reads.init_Long2ObjectOpenHas));
            return true;
         }
      } else {
         return false;
      }
   }

   boolean load(ArrayUtil view, long from, double fromFeet, long to, double toFeet) {
      FileCache.Inner_DataUtil edge = FileCache.Inner_DataUtil.of(from, fromFeet, to, toFeet);
      Long2ObjectOpenHashMap<BlockState> reads = this.init_LinkedHashMap.get(edge);
      if (reads == null) {
         return false;
      }

      for (Entry<BlockState> entry : reads.long2ObjectEntrySet()) {
         if (view.getBlockState(BlockPos.of(entry.getLongKey())) != entry.getValue()) {
            if (!view.load_Z()) {
               this.init_LinkedHashMap.remove(edge);
            }

            return false;
         }
      }

      return true;
   }

   private record Inner_DataUtil(long from, long to, long fromHeight, long toHeight) {
      static FileCache.Inner_DataUtil of(long from, double fromFeet, long to, double toFeet) {
         return new FileCache.Inner_DataUtil(from, to, Double.doubleToLongBits(fromFeet), Double.doubleToLongBits(toFeet));
      }
   }
}
