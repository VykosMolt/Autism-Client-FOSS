package autismclient.modules;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public final class StructureStorageFilter {
   public static final List<StructureStorageFilter.StructureFilter> STRUCTURES = List.of(
      filter(
         "hide-trial-chambers", "Hide Trial Chambers", Blocks.TRIAL_SPAWNER, Blocks.VAULT, Blocks.CHISELED_TUFF, Blocks.TUFF_BRICKS, Blocks.POLISHED_TUFF
      ),
      filter(
         "hide-ancient-cities",
         "Hide Ancient Cities",
         Blocks.REINFORCED_DEEPSLATE,
         Blocks.SCULK_SHRIEKER,
         Blocks.SCULK_CATALYST,
         Blocks.DEEPSLATE_TILES,
         Blocks.DEEPSLATE_BRICKS
      ),
      filter("hide-monuments", "Hide Ocean Monuments", Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE, Blocks.SEA_LANTERN),
      filter("hide-fortresses", "Hide Nether Fortresses", Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE),
      filter(
         "hide-bastions",
         "Hide Bastions",
         Blocks.GILDED_BLACKSTONE,
         Blocks.POLISHED_BLACKSTONE_BRICKS,
         Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS,
         Blocks.CHISELED_POLISHED_BLACKSTONE
      ),
      filter("hide-end-cities", "Hide End Cities", Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.END_STONE_BRICKS),
      filter(
         "hide-strongholds", "Hide Strongholds", Blocks.END_PORTAL_FRAME, Blocks.INFESTED_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS
      ),
      filter("hide-mineshafts", "Hide Mineshafts", Blocks.COBWEB, Blocks.RAIL, Blocks.POWERED_RAIL, Blocks.DETECTOR_RAIL, Blocks.OAK_FENCE),
      filter("hide-dungeons", "Hide Dungeons", Blocks.SPAWNER, Blocks.MOSSY_COBBLESTONE),
      filter("hide-desert-temples", "Hide Desert Temples", Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.TNT),
      filter("hide-jungle-temples", "Hide Jungle Temples", Blocks.TRIPWIRE_HOOK, Blocks.CHISELED_STONE_BRICKS),
      filter("hide-igloos", "Hide Igloos", Blocks.SNOW_BLOCK, Blocks.PACKED_ICE),
      filter("hide-mansions", "Hide Woodland Mansions", Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_FENCE),
      filter(
         "hide-villages",
         "Hide Villages",
         Blocks.BELL,
         Blocks.COMPOSTER,
         Blocks.LECTERN,
         Blocks.FLETCHING_TABLE,
         Blocks.CARTOGRAPHY_TABLE,
         Blocks.SMITHING_TABLE,
         Blocks.GRINDSTONE,
         Blocks.LOOM,
         Blocks.HAY_BLOCK
      ),
      filter("hide-swamp-huts", "Hide Swamp Huts", Blocks.CAULDRON, Blocks.SPRUCE_PLANKS, Blocks.CRAFTING_TABLE)
   );

   private static StructureStorageFilter.StructureFilter filter(String settingId, String label, Block... markers) {
      return new StructureStorageFilter.StructureFilter(settingId, label, Set.of(markers));
   }

   private StructureStorageFilter() {
   }

   public static long hideMask(Module module) {
      if (module == null) {
         return 0L;
      }

      long mask = 0L;

      for (int i = 0; i < STRUCTURES.size(); i++) {
         if (module.value(STRUCTURES.get(i).id()).equals("true")) {
            mask |= 1L << i;
         }
      }

      return mask;
   }

   public static boolean isInHiddenStructure(LevelChunk chunk, BlockPos pos, long mask, Map<Integer, Long> sectionCache) {
      if (mask != 0L && chunk != null && pos != null) {
         int index = chunk.getSectionIndex(pos.getY());
         Long cached = sectionCache.get(index);
         long found = cached != null ? cached : scanChunk(chunk, index, mask);
         if (cached == null) {
            sectionCache.put(index, found);
         }

         return (found & mask) != 0L;
      } else {
         return false;
      }
   }

   private static long scanChunk(LevelChunk chunk, int index, long mask) {
      long found = 0L;
      LevelChunkSection[] sections = chunk.getSections();

      for (int i = index - 1; i <= index + 1; i++) {
         if (i >= 0 && i < sections.length) {
            LevelChunkSection section = sections[i];
            if (section != null && !section.hasOnlyAir()) {
               found |= scanSection(section, mask);
            }
         }
      }

      return found;
   }

   private static long scanSection(LevelChunkSection section, long mask) {
      long found = 0L;

      for (int i = 0; i < STRUCTURES.size(); i++) {
         if ((mask & 1L << i) != 0L) {
            Set<Block> markers = STRUCTURES.get(i).init_Set();
            if (section.maybeHas(state -> isMarker(markers, state))) {
               found |= 1L << i;
            }
         }
      }

      return found;
   }

   private static boolean isMarker(Set<Block> markers, BlockState state) {
      return state != null && markers.contains(state.getBlock());
   }

   public record StructureFilter(String id, String label, Set<Block> init_Set) {
   }
}
