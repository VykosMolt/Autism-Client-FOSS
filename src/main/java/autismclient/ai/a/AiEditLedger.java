package autismclient.ai.a;

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
   private static final Map<Long, Long> init_Map = new HashMap<>();
   private static final Map<Long, Long> load_Map = new HashMap<>();
   private static final Set<Long> init_Set = new HashSet<>();
   private static final Set<Long> load_Set = new HashSet<>();
   private static final Set<Long> read = new HashSet<>();
   private static int init_I;

   private AiEditLedger() {
   }

   public static synchronized void init_V(BlockPos cell) {
      load_Map.remove(cell.asLong());
   }

   public static synchronized void load_V(BlockPos cell) {
      read.add(cell.asLong());
   }

   public static synchronized void read_V(BlockPos cell) {
      load_Set.add(cell.asLong());
      load_Map.remove(cell.asLong());
   }

   public static synchronized void write_V(BlockPos cell) {
      long now = System.nanoTime();
      Long brokeAt = load_Map.remove(cell.asLong());
      if (brokeAt != null && now - brokeAt < 20000000000L && !init_Set.contains(cell.asLong())) {
         init("placed a block back into " + cell.toShortString() + ", broken " + (now - brokeAt) / 1000000000L + " s ago");
      }

      init_Map.put(cell.asLong(), now);
      init(now);
   }

   public static synchronized void init(BlockPos cell, BlockState was) {
      boolean ownedSupport = AiScaffold.init_Block(cell) != null;
      boolean teardownExpected = read.remove(cell.asLong());
      AiScaffold.init_V(cell);
      long now = System.nanoTime();
      Long placedAt = init_Map.remove(cell.asLong());
      boolean station = was != null && (was.hasBlockEntity() || was.is(Blocks.CRAFTING_TABLE));
      boolean capped = load_Set.remove(cell.asLong());
      if (was != null && was.is(BlockTags.LOGS)) {
         init_Set.add(cell.asLong());
      } else {
         init_Set.remove(cell.asLong());
      }

      if (placedAt != null && now - placedAt < 120000000000L && !station && !capped && !teardownExpected && !ownedSupport) {
         init(
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

      load_Map.put(cell.asLong(), now);
      init(now);
   }

   public static synchronized boolean init_Z(BlockPos cell) {
      Long at = init_Map.get(cell.asLong());
      return at != null && System.nanoTime() - at < 120000000000L;
   }

   public static synchronized boolean load_Z(BlockPos cell) {
      Long at = init_Map.get(cell.asLong());
      return at != null && System.nanoTime() - at < 120000000000L && !load_Set.contains(cell.asLong());
   }

   public static synchronized boolean read_Z(BlockPos cell) {
      if (init_Set.contains(cell.asLong())) {
         return false;
      }

      Long at = load_Map.get(cell.asLong());
      return at != null && System.nanoTime() - at < 20000000000L;
   }

   public static synchronized boolean write_Z(BlockPos cell) {
      return load_Map.containsKey(cell.asLong());
   }

   public static synchronized boolean update(BlockPos cell) {
      if (load_Map.containsKey(cell.asLong())) {
         return true;
      }

      for (Direction side : Direction.values()) {
         if (load_Map.containsKey(cell.relative(side).asLong())) {
            return true;
         }
      }

      return false;
   }

   public static synchronized void init_V() {
      init_Map.clear();
      load_Map.clear();
      init_Set.clear();
      load_Set.clear();
   }

   public static synchronized int init_I() {
      return init_I;
   }

   private static void init(String what) {
      init_I++;
      AutismClientAddon.LOG.info("[AI] WASTE: {}", what);
   }

   private static void init(long now) {
      if (init_Map.size() + load_Map.size() >= 4096) {
         init_Map.values().removeIf(t -> now - t > 120000000000L);
         load_Map.values().removeIf(t -> now - t > 120000000000L);
         init_Set.removeIf(cell -> !load_Map.containsKey(cell));
      }
   }
}
