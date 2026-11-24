package autismclient.ai.b;

import autismclient.ai.af;
import autismclient.ai.bt;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

final class MapStore {
   private static final List<Item> init = List.of(
      Items.DIRT,
      Items.COBBLESTONE,
      Items.NETHERRACK,
      Items.COBBLED_DEEPSLATE,
      Items.STONE,
      Items.ANDESITE,
      Items.DIORITE,
      Items.GRANITE,
      Items.SANDSTONE,
      Items.OAK_PLANKS,
      Items.SPRUCE_PLANKS,
      Items.BIRCH_PLANKS
   );

   private MapStore() {
   }

   static Item init_Item(EventBus env, BlockState state) {
      MapStore.Inner_StreamUtil best = init_MapStore_StreamUti(env, state);
      return best != null && !best.stack().isEmpty() ? best.stack().getItem() : null;
   }

   static MapStore.Inner_StreamUtil init_MapStore_StreamUti(EventBus env, BlockState state) {
      return init(env, state, null);
   }

   static MapStore.Inner_StreamUtil init(EventBus env, BlockState state, Item wantedDrop) {
      MapStore.Inner_StreamUtil best = null;

      for (int slot = 0; slot <= 40; slot++) {
         if (slot != 36 && slot != 37 && slot != 38 && slot != 39) {
            ItemStack stack = env.init_ItemStack_I(slot);
            if (!stack.isEmpty()
               && af.init(stack, 2)
               && (!state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state))
               && (wantedDrop == null || init_Item(env, state, stack) == wantedDrop)) {
               float speed = stack.getDestroySpeed(state);
               if (best == null || speed > best.speed()) {
                  best = new MapStore.Inner_StreamUtil(stack, speed);
               }
            }
         }
      }

      if (wantedDrop != null && init_Item(env, state, ItemStack.EMPTY) == wantedDrop) {
         float speed = ItemStack.EMPTY.getDestroySpeed(state);
         if (best == null || speed >= best.speed()) {
            return new MapStore.Inner_StreamUtil(ItemStack.EMPTY, speed);
         }
      }

      return best;
   }

   static Item init_Item(EventBus env, BlockState state, ItemStack tool) {
      Item blockItem = state.getBlock().asItem();
      if (blockItem == Items.AIR) {
         return Items.AIR;
      }

      if (env.update_Z()) {
         return blockItem;
      }

      ItemStack stack = tool == null ? ItemStack.EMPTY : tool;
      if (!state.requiresCorrectToolForDrops() || !stack.isEmpty() && stack.isCorrectToolForDrops(state)) {
         Block block = state.getBlock();
         if (block == Blocks.BEE_NEST) {
            return env.init_Z_ItemStack(stack) ? blockItem : Items.AIR;
         }

         if (!(block instanceof LeavesBlock)) {
            Item infested = init(block);
            if (infested != null) {
               return env.init_Z_ItemStack(stack) ? infested : Items.AIR;
            }

            if (env.init_Z_ItemStack(stack)) {
               return blockItem;
            }

            if (block == Blocks.STONE) {
               return Items.COBBLESTONE;
            }

            if (block == Blocks.DEEPSLATE) {
               return Items.COBBLED_DEEPSLATE;
            }

            if (block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM || block == Blocks.PODZOL) {
               return Items.DIRT;
            }

            if (block == Blocks.CRIMSON_NYLIUM || block == Blocks.WARPED_NYLIUM) {
               return Items.NETHERRACK;
            }

            if (block == Blocks.CLAY) {
               return Items.CLAY_BALL;
            }

            if (block == Blocks.SNOW_BLOCK) {
               return Items.SNOWBALL;
            }

            if (block == Blocks.GLOWSTONE) {
               return Items.GLOWSTONE_DUST;
            }

            if (block == Blocks.SEA_LANTERN) {
               return Items.PRISMARINE_CRYSTALS;
            }

            if (block == Blocks.MELON) {
               return Items.MELON_SLICE;
            }

            if (block == Blocks.BOOKSHELF) {
               return Items.BOOK;
            }

            Item ore = load(block);
            if (ore != null) {
               return ore;
            }

            String id = BuiltInRegistries.ITEM.getKey(blockItem).toString();
            return af.init_Z(id) ? Items.AIR : blockItem;
         } else {
            return !env.init_Z_ItemStack(stack) && !stack.is(Items.SHEARS) ? Items.AIR : blockItem;
         }
      } else {
         return Items.AIR;
      }
   }

   private static Item init(Block block) {
      if (block == Blocks.INFESTED_STONE) {
         return Items.STONE;
      } else if (block == Blocks.INFESTED_COBBLESTONE) {
         return Items.COBBLESTONE;
      } else if (block == Blocks.INFESTED_STONE_BRICKS) {
         return Items.STONE_BRICKS;
      } else if (block == Blocks.INFESTED_MOSSY_STONE_BRICKS) {
         return Items.MOSSY_STONE_BRICKS;
      } else if (block == Blocks.INFESTED_CRACKED_STONE_BRICKS) {
         return Items.CRACKED_STONE_BRICKS;
      } else if (block == Blocks.INFESTED_CHISELED_STONE_BRICKS) {
         return Items.CHISELED_STONE_BRICKS;
      } else {
         return block == Blocks.INFESTED_DEEPSLATE ? Items.DEEPSLATE : null;
      }
   }

   private static Item load(Block block) {
      if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) {
         return Items.COAL;
      } else if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
         return Items.RAW_IRON;
      } else if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
         return Items.RAW_COPPER;
      } else if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
         return Items.RAW_GOLD;
      } else if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
         return Items.DIAMOND;
      } else if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) {
         return Items.EMERALD;
      } else if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
         return Items.REDSTONE;
      } else if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) {
         return Items.LAPIS_LAZULI;
      } else if (block == Blocks.NETHER_QUARTZ_ORE) {
         return Items.QUARTZ;
      } else {
         return block != Blocks.NETHER_GOLD_ORE && block != Blocks.GILDED_BLACKSTONE ? null : Items.GOLD_NUGGET;
      }
   }

   static int init_I(EventBus env, BlockState state, ItemStack tool) {
      Item dropped = init_Item(env, state, tool);
      if (dropped == Items.AIR) {
         return 0;
      } else {
         return !state.is(Blocks.CLAY) && !state.is(Blocks.SNOW_BLOCK) ? 1 : 4;
      }
   }

   static MapStore.Inner_DataUtil init(EventBus env, Set<Item> schematicItems, ToIntFunction<Item> stock, ToIntFunction<Item> open, Set<Item> avoid) {
      MapStore.Inner_DataUtil best = null;
      int bestRank = 2147483647;
      HashSet<Item> seen = new HashSet<>();

      for (int slot = 0; slot <= 40; slot++) {
         if (slot != 36 && slot != 37 && slot != 38 && slot != 39) {
            ItemStack stack = env.init_ItemStack_I(slot);
            if (!stack.isEmpty()) {
               Item item = stack.getItem();
               if (!avoid.contains(item) && stock.applyAsInt(item) > 0 && seen.add(item) && item instanceof BlockItem blockItem) {
                  BlockState state = blockItem.getBlock().defaultBlockState();
                  if (bt.init(state)) {
                     boolean cheap = init.contains(item);
                     if (cheap || schematicItems.contains(item)) {
                        int needed = open.applyAsInt(item);
                        MapStore.Inner_StreamUtil recovery = init(env, state, item);
                        boolean sameItem = recovery != null;
                        if (!init(item, state, sameItem)) {
                           int held = stock.applyAsInt(item);
                           if (needed <= 0 || sameItem || held > needed) {
                              MapStore.Inner_StreamUtil fastest = init(env, state, null);
                              float speed = fastest == null ? 1.0F : fastest.speed();
                              float hardness = state.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                              if (!(hardness < 0.0F)) {
                                 boolean correct = !state.requiresCorrectToolForDrops() || fastest != null && fastest.stack().isCorrectToolForDrops(state);
                                 float progress = hardness == 0.0F ? 1.0F : speed / hardness / (correct ? 30.0F : 100.0F);
                                 if (env.update_Z() || !(progress < 0.005F)) {
                                    int rank = init(item, needed, sameItem, state, speed);
                                    if (best == null || rank < bestRank) {
                                       best = new MapStore.Inner_DataUtil(
                                          item, state, recovery == null ? ItemStack.EMPTY : recovery.stack(), speed, sameItem, needed > 0
                                       );
                                       bestRank = rank;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   private static int init(Item item, int needed, boolean sameItem, BlockState state, float speed) {
      int cheap = init.indexOf(item);
      int rank = cheap >= 0 ? cheap * 20 : 1000;
      if (needed > 0) {
         rank += sameItem ? 2000 : 5000;
      } else if (!sameItem) {
         rank += 1000;
      }

      if (state.requiresCorrectToolForDrops() && speed <= 1.0F) {
         rank += 400;
      }

      rank += (int)(Math.max(0.0F, state.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) * 20.0F);
      return rank - Math.min(100, (int)(speed * 4.0F));
   }

   private static boolean init(Item item, BlockState state, boolean sameItem) {
      String path = BuiltInRegistries.ITEM.getKey(item).getPath();
      boolean scarce = path.endsWith("_ore")
         || path.equals("ancient_debris")
         || path.startsWith("raw_") && path.endsWith("_block")
         || state.is(BlockTags.BEACON_BASE_BLOCKS);
      return scarce && !sameItem;
   }

   record Inner_DataUtil(Item item, BlockState state, ItemStack tool, float speed, boolean sameItemDrop, boolean loan) {
   }

   record Inner_StreamUtil(ItemStack stack, float speed) {
   }
}
