package autismclient.ai.print;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.util.LinkedHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

final class PrintFailedWalks {
   static final int init_I = 128;
   private final LinkedHashMap<PrintFailedWalks.Inner_DataUtil, Long2ObjectOpenHashMap<BlockState>> init_LinkedHashMap = new LinkedHashMap<>();

   int init_I() {
      return this.init_LinkedHashMap.size();
   }

   void init_V() {
      this.init_LinkedHashMap.clear();
   }

   boolean init(PrintView view, long from, double fromFeet, long to, double toFeet) {
      if (!view.hasAnyHypothesis()
         && Double.isFinite(fromFeet)
         && Double.isFinite(toFeet)
         && Math.abs(BlockPos.getX(from) - BlockPos.getX(to)) <= 1
         && Math.abs(BlockPos.getZ(from) - BlockPos.getZ(to)) <= 1
         && from != to
         && !(Math.abs(fromFeet - toFeet) > 3.0)) {
         try (PrintView.Inner_TaskQueue reads = view.openReadTracker()) {
            int minY = PrintView.floorInt(Math.min(fromFeet, toFeet)) - 1;
            int maxY = (int)Math.ceil(Math.max(fromFeet, toFeet) + 1.8 + 1.25);

            for (int x = Math.min(BlockPos.getX(from), BlockPos.getX(to)) - 1; x <= Math.max(BlockPos.getX(from), BlockPos.getX(to)) + 1; x++) {
               for (int z = Math.min(BlockPos.getZ(from), BlockPos.getZ(to)) - 1; z <= Math.max(BlockPos.getZ(from), BlockPos.getZ(to)) + 1; z++) {
                  for (int y = minY; y <= maxY; y++) {
                     view.getCollisionBoxesAt(new BlockPos(x, y, z));
                  }
               }
            }

            if (!reads.isComplete()) {
               return false;
            }

            PrintFailedWalks.Inner_DataUtil edge = PrintFailedWalks.Inner_DataUtil.of(from, fromFeet, to, toFeet);
            if (!this.init_LinkedHashMap.containsKey(edge) && this.init_LinkedHashMap.size() >= 128) {
               this.init_LinkedHashMap.remove(this.init_LinkedHashMap.keySet().iterator().next());
            }

            this.init_LinkedHashMap.put(edge, new Long2ObjectOpenHashMap<>(reads.recordedStates));
            return true;
         }
      } else {
         return false;
      }
   }

   boolean load(PrintView view, long from, double fromFeet, long to, double toFeet) {
      PrintFailedWalks.Inner_DataUtil edge = PrintFailedWalks.Inner_DataUtil.of(from, fromFeet, to, toFeet);
      Long2ObjectOpenHashMap<BlockState> reads = this.init_LinkedHashMap.get(edge);
      if (reads == null) {
         return false;
      }

      for (Entry<BlockState> entry : reads.long2ObjectEntrySet()) {
         if (view.getBlockState(BlockPos.of(entry.getLongKey())) != entry.getValue()) {
            if (!view.hasAnyHypothesis()) {
               this.init_LinkedHashMap.remove(edge);
            }

            return false;
         }
      }

      return true;
   }

   private record Inner_DataUtil(long from, long to, long fromHeight, long toHeight) {
      static PrintFailedWalks.Inner_DataUtil of(long from, double fromFeet, long to, double toFeet) {
         return new PrintFailedWalks.Inner_DataUtil(from, to, Double.doubleToLongBits(fromFeet), Double.doubleToLongBits(toFeet));
      }
   }
}
