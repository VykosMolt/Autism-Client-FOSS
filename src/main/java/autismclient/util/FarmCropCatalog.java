package autismclient.util;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class FarmCropCatalog {
   private static final Set<Block> STACKING_CROPS = identitySet(Blocks.SUGAR_CANE, Blocks.CACTUS, Blocks.BAMBOO);
   private static final Set<Block> STACKED_CROPS = identitySet(Blocks.PUMPKIN, Blocks.MELON, Blocks.SUGAR_CANE, Blocks.CACTUS, Blocks.BAMBOO);
   private static final Map<Block, Item> SEEDS = Map.ofEntries(
      Map.entry(Blocks.WHEAT, Items.WHEAT_SEEDS),
      Map.entry(Blocks.CARROTS, Items.CARROT),
      Map.entry(Blocks.POTATOES, Items.POTATO),
      Map.entry(Blocks.BEETROOTS, Items.BEETROOT_SEEDS),
      Map.entry(Blocks.NETHER_WART, Items.NETHER_WART),
      Map.entry(Blocks.COCOA, Items.COCOA_BEANS),
      Map.entry(Blocks.SWEET_BERRY_BUSH, Items.SWEET_BERRIES)
   );
   private static final Set<Block> AGE_CROPS = identitySet(
      Blocks.WHEAT,
      Blocks.CARROTS,
      Blocks.POTATOES,
      Blocks.BEETROOTS,
      Blocks.NETHER_WART,
      Blocks.COCOA,
      Blocks.SWEET_BERRY_BUSH,
      Blocks.PUMPKIN,
      Blocks.MELON,
      Blocks.SUGAR_CANE,
      Blocks.CACTUS,
      Blocks.BAMBOO
   );

   private FarmCropCatalog() {
   }

   public static boolean isCrop(Block block) {
      return block != null && AGE_CROPS.contains(block);
   }

   public static boolean isStacking(Block block) {
      return block != null && STACKING_CROPS.contains(block);
   }

   public static boolean read(Block block) {
      return block != null && STACKED_CROPS.contains(block);
   }

   public static Set<Block> allCrops() {
      return AGE_CROPS;
   }

   public static boolean canGrowOn(Block crop, BlockState below) {
      if (crop == null || below == null) {
         return false;
      } else if (crop == Blocks.NETHER_WART) {
         return below.is(Blocks.SOUL_SAND) || below.is(BlockTags.SUPPORTS_NETHER_WART);
      } else if (crop == Blocks.COCOA) {
         return below.is(BlockTags.SUPPORTS_COCOA);
      } else if (crop == Blocks.SWEET_BERRY_BUSH) {
         return below.is(BlockTags.SUPPORTS_VEGETATION);
      } else {
         return !SEEDS.containsKey(crop) ? false : below.is(Blocks.FARMLAND) || below.is(BlockTags.SUPPORTS_CROPS);
      }
   }

   public static Item seedFor(Block crop) {
      return crop == null ? null : SEEDS.get(crop);
   }

   private static Set<Block> identitySet(Block... blocks) {
      Set<Block> set = Collections.newSetFromMap(new IdentityHashMap<>());
      Collections.addAll(set, blocks);
      return Collections.unmodifiableSet(set);
   }
}
