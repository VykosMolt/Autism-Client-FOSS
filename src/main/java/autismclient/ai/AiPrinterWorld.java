package autismclient.ai;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class AiPrinterWorld {
   private static Long2ObjectOpenHashMap<BlockState> init_Long2ObjectOpenHas;
   private static Level init_Level;
   private static Thread init_Thread;
   private static LongOpenHashSet init_LongOpenHashSet;
   private static Level load_Level;
   private static Thread load_Thread;

   private AiPrinterWorld() {
   }

   public static BlockState init(Level reading, BlockPos pos) {
      if (init_LongOpenHashSet != null && reading == load_Level && Thread.currentThread() == load_Thread) {
         init_LongOpenHashSet.add(pos.asLong());
      }

      Long2ObjectOpenHashMap<BlockState> planned = init_Long2ObjectOpenHas;
      return planned != null && reading == init_Level && Thread.currentThread() == init_Thread ? planned.get(pos.asLong()) : null;
   }

   static void init(Level source, LongOpenHashSet into) {
      init_LongOpenHashSet = into;
      load_Level = source;
      load_Thread = into == null ? null : Thread.currentThread();
   }

   static void init(Level live, Long2ObjectOpenHashMap<BlockState> planned) {
      init_Level = live;
      init_Thread = Thread.currentThread();
      init_Long2ObjectOpenHas = planned;
   }

   static void init() {
      init_Long2ObjectOpenHas = null;
      init_Level = null;
      init_Thread = null;
   }
}
