package autismclient.ai;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class AiPrinterWorld {
   private static Long2ObjectOpenHashMap<BlockState> plannedStates;
   private static Level plannedLevel;
   private static Thread plannedThread;
   private static LongOpenHashSet init_LongOpenHashSet;
   private static Level load_Level;
   private static Thread load_Thread;

   private AiPrinterWorld() {
   }

   public static BlockState init(Level reading, BlockPos pos) {
      if (init_LongOpenHashSet != null && reading == load_Level && Thread.currentThread() == load_Thread) {
         init_LongOpenHashSet.add(pos.asLong());
      }

      Long2ObjectOpenHashMap<BlockState> planned = plannedStates;
      return planned != null && reading == plannedLevel && Thread.currentThread() == plannedThread ? planned.get(pos.asLong()) : null;
   }

   static void init(Level source, LongOpenHashSet into) {
      init_LongOpenHashSet = into;
      load_Level = source;
      load_Thread = into == null ? null : Thread.currentThread();
   }

   static void init(Level live, Long2ObjectOpenHashMap<BlockState> planned) {
      plannedLevel = live;
      plannedThread = Thread.currentThread();
      plannedStates = planned;
   }

   static void init() {
      plannedStates = null;
      plannedLevel = null;
      plannedThread = null;
   }
}
