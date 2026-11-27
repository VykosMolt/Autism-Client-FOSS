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
   public static final List<StructureStorageFilter.Inner_DataUtil> init = List.of(
      init_aq_DataUtil_Str(
         "hide-trial-chambers", "Hide Trial Chambers", Blocks.TRIAL_SPAWNER, Blocks.VAULT, Blocks.CHISELED_TUFF, Blocks.TUFF_BRICKS, Blocks.POLISHED_TUFF
      ),
      init_aq_DataUtil_Str(
         "hide-ancient-cities",
         "Hide Ancient Cities",
         Blocks.REINFORCED_DEEPSLATE,
         Blocks.SCULK_SHRIEKER,
         Blocks.SCULK_CATALYST,
         Blocks.DEEPSLATE_TILES,
         Blocks.DEEPSLATE_BRICKS
      ),
      init_aq_DataUtil_Str("hide-monuments", "Hide Ocean Monuments", Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE, Blocks.SEA_LANTERN),
      init_aq_DataUtil_Str("hide-fortresses", "Hide Nether Fortresses", Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE),
      init_aq_DataUtil_Str(
         "hide-bastions",
         "Hide Bastions",
         Blocks.GILDED_BLACKSTONE,
         Blocks.POLISHED_BLACKSTONE_BRICKS,
         Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS,
         Blocks.CHISELED_POLISHED_BLACKSTONE
      ),
      init_aq_DataUtil_Str("hide-end-cities", "Hide End Cities", Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR, Blocks.END_STONE_BRICKS),
      init_aq_DataUtil_Str(
         "hide-strongholds", "Hide Strongholds", Blocks.END_PORTAL_FRAME, Blocks.INFESTED_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS
      ),
      init_aq_DataUtil_Str("hide-mineshafts", "Hide Mineshafts", Blocks.COBWEB, Blocks.RAIL, Blocks.POWERED_RAIL, Blocks.DETECTOR_RAIL, Blocks.OAK_FENCE),
      init_aq_DataUtil_Str("hide-dungeons", "Hide Dungeons", Blocks.SPAWNER, Blocks.MOSSY_COBBLESTONE),
      init_aq_DataUtil_Str("hide-desert-temples", "Hide Desert Temples", Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.TNT),
      init_aq_DataUtil_Str("hide-jungle-temples", "Hide Jungle Temples", Blocks.TRIPWIRE_HOOK, Blocks.CHISELED_STONE_BRICKS),
      init_aq_DataUtil_Str("hide-igloos", "Hide Igloos", Blocks.SNOW_BLOCK, Blocks.PACKED_ICE),
      init_aq_DataUtil_Str("hide-mansions", "Hide Woodland Mansions", Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_FENCE),
      init_aq_DataUtil_Str(
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
      init_aq_DataUtil_Str("hide-swamp-huts", "Hide Swamp Huts", Blocks.CAULDRON, Blocks.SPRUCE_PLANKS, Blocks.CRAFTING_TABLE)
   );

   private static StructureStorageFilter.Inner_DataUtil init_aq_DataUtil_Str(String settingId, String label, Block... markers) {
      return new StructureStorageFilter.Inner_DataUtil(settingId, label, Set.of(markers));
   }

   private StructureStorageFilter() {
   }

   public static long hideMask(Module module) {
      if (module == null) {
         return 0L;
      }

      long mask = 0L;

      for (int i = 0; i < init.size(); i++) {
         if (module.value(init.get(i).id()).equals("true")) {
            mask |= 1L << i;
         }
      }

      return mask;
   }

   public static boolean isInHiddenStructure(LevelChunk chunk, BlockPos pos, long mask, Map<Integer, Long> sectionCache) {
      if (mask != 0L && chunk != null && pos != null) {
         int index = chunk.getSectionIndex(pos.getY());
         Long cached = sectionCache.get(index);
         long found = cached != null ? cached : init_J_LevelChunk(chunk, index, mask);
         if (cached == null) {
            sectionCache.put(index, found);
         }

         return (found & mask) != 0L;
      } else {
         return false;
      }
   }

   private static long init_J_LevelChunk(LevelChunk chunk, int index, long mask) {
      long found = 0L;
      LevelChunkSection[] sections = chunk.getSections();

      for (int i = index - 1; i <= index + 1; i++) {
         if (i >= 0 && i < sections.length) {
            LevelChunkSection section = sections[i];
            if (section != null && !section.hasOnlyAir()) {
               found |= init_J_LevelChunkSection_J(section, mask);
            }
         }
      }

      return found;
   }

   private static long init_J_LevelChunkSection_J(LevelChunkSection section, long mask) {
      long found = 0L;

      for (int i = 0; i < init.size(); i++) {
         if ((mask & 1L << i) != 0L) {
            Set<Block> markers = init.get(i).init_Set();
            if (section.maybeHas(state -> init_Z_Set_BlockState(markers, state))) {
               found |= 1L << i;
            }
         }
      }

      return found;
   }

   private static boolean init_Z_Set_BlockState(Set<Block> markers, BlockState state) {
      return state != null && markers.contains(state.getBlock());
   }

   public record Inner_DataUtil(String id, String load, Set<Block> init_Set) {
   }
}
