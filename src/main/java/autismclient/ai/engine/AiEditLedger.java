package autismclient.ai.engine;

import autismclient.AutismClientAddon;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class AiEditLedger {
   static final long init_J = 120000000000L;
   static final long load_J = 20000000000L;
   private static final Map<Long, Long> placeTimes = new HashMap<>();
   private static final Map<Long, Long> breakTimes = new HashMap<>();
   private static final Set<Long> brokenLogCells = new HashSet<>();
   private static final Set<Long> cappedCells = new HashSet<>();
   private static final Set<Long> expectedTeardown = new HashSet<>();
   private static int wasteCount;

   private AiEditLedger() {
   }

   public static synchronized void forgetBreak(BlockPos cell) {
      breakTimes.remove(cell.asLong());
   }

   public static synchronized void expectTeardown(BlockPos cell) {
      expectedTeardown.add(cell.asLong());
   }

   public static synchronized void read_V(BlockPos cell) {
      cappedCells.add(cell.asLong());
      breakTimes.remove(cell.asLong());
   }

   public static synchronized void recordPlacement(BlockPos cell) {
      long now = System.nanoTime();
      Long brokeAt = breakTimes.remove(cell.asLong());
      if (brokeAt != null && now - brokeAt < 20000000000L && !brokenLogCells.contains(cell.asLong())) {
         recordWaste("placed a block back into " + cell.toShortString() + ", broken " + (now - brokeAt) / 1000000000L + " s ago");
      }

      placeTimes.put(cell.asLong(), now);
      prune(now);
   }

   public static synchronized void recordBreak(BlockPos cell, BlockState was) {
      boolean ownedSupport = AiScaffold.recordedBlock(cell) != null;
      boolean teardownExpected = expectedTeardown.remove(cell.asLong());
      AiScaffold.forget(cell);
      long now = System.nanoTime();
      Long placedAt = placeTimes.remove(cell.asLong());
      boolean station = was != null && (was.hasBlockEntity() || was.is(Blocks.CRAFTING_TABLE));
      boolean capped = cappedCells.remove(cell.asLong());
      if (was != null && was.is(BlockTags.LOGS)) {
         brokenLogCells.add(cell.asLong());
      } else {
         brokenLogCells.remove(cell.asLong());
      }

      if (placedAt != null && now - placedAt < 120000000000L && !station && !capped && !teardownExpected && !ownedSupport) {
         recordWaste(
            "broke its own block at "
               + cell.toShortString()
               + " ("
               + (was == null ? "?" : was.getBlock().getName().getString())
               + "), placed "
               + (now - placedAt) / 1000000000L
               + " s ago"
         );
      } else if (was != null && !was.isAir() && Boolean.getBoolean("autism.ai.schematics")) {
         String caller = StackWalker.getInstance()
            .walk(
               frames -> frames.skip(1L)
                  .limit(4L)
                  .map(f -> f.getClassName().substring(f.getClassName().lastIndexOf(46) + 1) + ":" + f.getLineNumber())
                  .reduce((a, b) -> a + " < " + b)
                  .orElse("?")
            );
         AutismClientAddon.LOG.info("[AI] broke {} at {} by {}", was.getBlock().getName().getString(), cell.toShortString(), caller);
      }

      breakTimes.put(cell.asLong(), now);
      prune(now);
   }

   public static synchronized boolean wasRecentlyPlaced(BlockPos cell) {
      Long at = placeTimes.get(cell.asLong());
      return at != null && System.nanoTime() - at < 120000000000L;
   }

   public static synchronized boolean load_Z(BlockPos cell) {
      Long at = placeTimes.get(cell.asLong());
      return at != null && System.nanoTime() - at < 120000000000L && !cappedCells.contains(cell.asLong());
   }

   public static synchronized boolean wasRecentlyBroken(BlockPos cell) {
      if (brokenLogCells.contains(cell.asLong())) {
         return false;
      }

      Long at = breakTimes.get(cell.asLong());
      return at != null && System.nanoTime() - at < 20000000000L;
   }

   public static synchronized boolean hasBreakRecord(BlockPos cell) {
      return breakTimes.containsKey(cell.asLong());
   }

   public static synchronized boolean isNearRecentBreak(BlockPos cell) {
      if (breakTimes.containsKey(cell.asLong())) {
         return true;
      }

      for (Direction side : Direction.values()) {
         if (breakTimes.containsKey(cell.relative(side).asLong())) {
            return true;
         }
      }

      return false;
   }

   public static synchronized void reset() {
      placeTimes.clear();
      breakTimes.clear();
      brokenLogCells.clear();
      cappedCells.clear();
   }

   public static synchronized int wasteCount() {
      return wasteCount;
   }

   private static void recordWaste(String what) {
      wasteCount++;
      AutismClientAddon.LOG.info("[AI] WASTE: {}", what);
   }

   private static void prune(long now) {
      if (placeTimes.size() + breakTimes.size() >= 4096) {
         placeTimes.values().removeIf(t -> now - t > 120000000000L);
         breakTimes.values().removeIf(t -> now - t > 120000000000L);
         brokenLogCells.removeIf(cell -> !breakTimes.containsKey(cell));
      }
   }
}
