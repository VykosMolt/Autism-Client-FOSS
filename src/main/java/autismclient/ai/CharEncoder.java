package autismclient.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;

final class CharEncoder {
   private CharEncoder() {
   }

   static List<CharEncoder.Inner_CachePool> init(TableIndex.Inner_StreamUtil cell) {
      return init(cell, null, false);
   }

   static List<CharEncoder.Inner_CachePool> init(TableIndex.Inner_StreamUtil cell, BlockState live, boolean waterAbove) {
      if (!TableIndex.read_Z(cell) && (live == null || !TableIndex.update(cell, live))) {
         if (!"upper".equals(cell.properties().get("half")) && !"head".equals(cell.properties().get("part"))) {
            ArrayList<CharEncoder.Inner_CachePool> out = new ArrayList<>();
            String id = cell.block();
            if (id.equals("minecraft:grass_block")) {
               init(out, CharEncoder.Inner_DataUtil.init, Items.DIRT, 1, CharEncoder.Inner_BufferHelper.init);
               return List.copyOf(out);
            }

            if (!id.equals("minecraft:farmland") && !id.equals("minecraft:dirt_path")) {
               if (TableIndex.init_Fluid(cell) != null) {
                  if (!TableIndex.load_Z(cell)) {
                     init(
                        out,
                        CharEncoder.Inner_DataUtil.read,
                        id.equals("minecraft:lava") ? Items.LAVA_BUCKET : Items.WATER_BUCKET,
                        1,
                        CharEncoder.Inner_BufferHelper.load
                     );
                  }

                  return List.copyOf(out);
               } else if (bQ.load_Z(cell)) {
                  boolean present = live != null && live.is(Blocks.COMPOSTER);
                  int wanted = Integer.parseInt(cell.properties().get("level"));
                  int current = present ? live.getValue(ComposterBlock.LEVEL) : 0;
                  if (!present || current > wanted && current < 8) {
                     init(out, CharEncoder.Inner_DataUtil.init, Items.COMPOSTER, 1, CharEncoder.Inner_BufferHelper.init);
                  }

                  if (current > wanted) {
                     current = 0;
                  }

                  int feeds = Math.max(0, (wanted == 8 ? 7 : wanted) - current);
                  init(out, CharEncoder.Inner_DataUtil.sync, Items.PUMPKIN_PIE, feeds, CharEncoder.Inner_BufferHelper.init);
                  return List.copyOf(out);
               } else {
                  if (bQ.write(cell)) {
                     init(cell, live, out);
                     return List.copyOf(out);
                  }

                  if (id.startsWith("minecraft:potted_")) {
                     if (live == null || !live.is(Blocks.FLOWER_POT)) {
                        init(out, CharEncoder.Inner_DataUtil.init, Items.FLOWER_POT, 1, CharEncoder.Inner_BufferHelper.init);
                     }

                     Item plant = bQ.init(TableIndex.init_BlockState(cell).getBlock());
                     init(out, CharEncoder.Inner_DataUtil.close, plant, 1, CharEncoder.Inner_BufferHelper.init);
                     return List.copyOf(out);
                  } else {
                     Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
                     boolean same = live != null && live.getBlock() == block;
                     Item item = block.asItem();
                     boolean existingDoubleSlab = same && block instanceof SlabBlock && live.getValue(SlabBlock.TYPE) == SlabType.DOUBLE;
                     String countProperty = null;

                     for (String property : cell.properties().keySet()) {
                        if (TableIndex.init_Z(block, property)) {
                           countProperty = property;
                           break;
                        }
                     }

                     boolean replacing = false;
                     if (block instanceof MultifaceBlock) {
                        BlockState wanted = TableIndex.init_BlockState(cell);
                        boolean partial = same && autismclient.ai.b.PathHelper.load_Z_BlockState_BlockState(wanted, live);
                        int remaining = 0;

                        for (Direction side : Direction.values()) {
                           if (MultifaceBlock.hasFace(wanted, side) && (!partial || !MultifaceBlock.hasFace(live, side))) {
                              remaining++;
                           }
                        }

                        replacing = !partial;
                        init(out, item, partial, remaining);
                     } else if (countProperty != null) {
                        int wanted = Math.max(1, Integer.parseInt(cell.properties().get(countProperty)));
                        int current = 0;
                        if (same) {
                           Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock()
                              .getStateDefinition()
                              .getProperty(countProperty);
                           if (property != null) {
                              current = Integer.parseInt(String.valueOf(live.getValue(property)));
                           }
                        }

                        init(out, item, same, Math.max(0, wanted - current));
                     } else if (TableIndex.init_Z(cell)) {
                        int remaining = !same ? 2 : (live.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 0 : 1);
                        init(out, item, same, remaining);
                     } else {
                        replacing = !same || !existingDoubleSlab && init(cell, live, block);
                        if (replacing) {
                           init(out, CharEncoder.Inner_DataUtil.init, item, 1, CharEncoder.Inner_BufferHelper.init);
                        }
                     }

                     if (!(block instanceof MultifaceBlock)) {
                        init(cell, live, block, same && !replacing, out);
                     }

                     load(cell, live, block, same && !replacing, out);
                     if (block instanceof CopperBulbBlock
                        && "true".equals(cell.properties().get("lit"))
                        && !"true".equals(cell.properties().get("powered"))
                        && (!same || !live.getValue(CopperBulbBlock.LIT))) {
                        init(out, CharEncoder.Inner_DataUtil.open, Items.REDSTONE_BLOCK, 1, CharEncoder.Inner_BufferHelper.load);
                     }

                     return List.copyOf(out);
                  }
               }
            } else {
               if (!init(id, live)) {
                  init(out, CharEncoder.Inner_DataUtil.init, Items.DIRT, 1, CharEncoder.Inner_BufferHelper.init);
               }

               boolean farmland = id.equals("minecraft:farmland");
               init(
                  out,
                  CharEncoder.Inner_DataUtil.update,
                  farmland ? "hoe" : "shovel",
                  farmland ? Items.WOODEN_HOE : Items.WOODEN_SHOVEL,
                  CharEncoder.Inner_BufferHelper.load
               );
               if (waterAbove) {
                  init(out, CharEncoder.Inner_DataUtil.write, Items.BUCKET, 1, CharEncoder.Inner_BufferHelper.load);
               }

               return List.copyOf(out);
            }
         } else {
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   static List<CharEncoder.Inner_CachePool> init(List<TableIndex.Inner_StreamUtil> cells, BlockPos origin, LevelReader world) {
      HashSet<Long> sourceCells = new HashSet<>();

      for (TableIndex.Inner_StreamUtil cell : cells) {
         sourceCells.add(origin.offset(cell.dx(), cell.dy(), cell.dz()).asLong());
      }

      ArrayList<CharEncoder.Inner_CachePool> out = new ArrayList<>();
      HashSet<BlockPos> foundations = new HashSet<>();

      for (TableIndex.Inner_StreamUtil cell : cells) {
         if (!TableIndex.read_Z(cell)) {
            BlockPos target = origin.offset(cell.dx(), cell.dy(), cell.dz());
            BlockState live = world.getBlockState(target);
            if (!TableIndex.update(cell, live)) {
               out.addAll(init(cell, live, world.getFluidState(target.above()).is(FluidTags.WATER)));
               BlockPos foundation = bQ.init_BlockPos_TableIndex_StreamU_2(cell, target, world, sourceCells);
               if (foundation != null && foundations.add(foundation)) {
                  init(out, CharEncoder.Inner_DataUtil.init, Items.DIRT, 1, CharEncoder.Inner_BufferHelper.init);
               }
            }
         }
      }

      return List.copyOf(out);
   }

   static List<CharEncoder.Inner_CachePool> init_List(Iterable<CharEncoder.Inner_CachePool> requirements) {
      LinkedHashMap<CharEncoder.Inner_StreamUtil, CharEncoder.Inner_CachePool> combined = new LinkedHashMap<>();

      for (CharEncoder.Inner_CachePool requirement : requirements) {
         CharEncoder.Inner_StreamUtil key = new CharEncoder.Inner_StreamUtil(requirement.itemSpec(), requirement.use());
         CharEncoder.Inner_CachePool previous = combined.get(key);
         if (previous == null) {
            combined.put(key, requirement);
         } else if (requirement.use() == CharEncoder.Inner_BufferHelper.init) {
            combined.put(
               key,
               new CharEncoder.Inner_CachePool(previous.action(), previous.itemSpec(), previous.item(), previous.count() + requirement.count(), previous.use())
            );
         }
      }

      return List.copyOf(combined.values());
   }

   static Map<Item, Integer> init_Map(Iterable<CharEncoder.Inner_CachePool> requirements) {
      LinkedHashMap<Item, Integer> out = new LinkedHashMap<>();

      for (CharEncoder.Inner_CachePool requirement : init_List(requirements)) {
         out.merge(requirement.item(), requirement.count(), Integer::sum);
      }

      return out;
   }

   private static void init(TableIndex.Inner_StreamUtil cell, BlockState live, List<CharEncoder.Inner_CachePool> out) {
      boolean present = live != null && live.getBlock() instanceof AbstractCauldronBlock;
      if (!present) {
         init(out, CharEncoder.Inner_DataUtil.init, Items.CAULDRON, 1, CharEncoder.Inner_BufferHelper.init);
      }

      Block wantedBlock = BuiltInRegistries.BLOCK.getValue(Identifier.parse(cell.block()));
      boolean water = cell.block().equals("minecraft:water_cauldron");
      boolean powderSnow = cell.block().equals("minecraft:powder_snow_cauldron");
      int wanted = !water && !powderSnow ? 3 : Integer.parseInt(cell.properties().getOrDefault("level", "3"));
      boolean wrongContents = present && !live.is(Blocks.CAULDRON) && live.getBlock() != wantedBlock;
      if (wrongContents) {
         init(out, CharEncoder.Inner_DataUtil.write, Items.BUCKET, 1, CharEncoder.Inner_BufferHelper.load);
      }

      boolean partialFill = bQ.init_Z(cell, live);
      boolean fill = !present || live.is(Blocks.CAULDRON) || wrongContents || partialFill;
      if (fill) {
         init(out, CharEncoder.Inner_DataUtil.flush, bQ.init_Item(cell), 1, CharEncoder.Inner_BufferHelper.init);
      }

      if (!water) {
         if (powderSnow && !partialFill && present && live.getBlock() == wantedBlock && live.getValue(LayeredCauldronBlock.LEVEL) != wanted) {
            init(out, CharEncoder.Inner_DataUtil.update, Items.CAULDRON, 1, CharEncoder.Inner_BufferHelper.load);
         }
      } else if (present && live.getBlock() == wantedBlock) {
         int current = live.getValue(LayeredCauldronBlock.LEVEL);
         if (current > wanted) {
            init(out, CharEncoder.Inner_DataUtil.write, Items.GLASS_BOTTLE, current - wanted, CharEncoder.Inner_BufferHelper.init);
         } else if (current < wanted) {
            init(out, CharEncoder.Inner_DataUtil.write, Items.GLASS_BOTTLE, 3 - wanted, CharEncoder.Inner_BufferHelper.init);
         }
      } else if (fill) {
         init(out, CharEncoder.Inner_DataUtil.write, Items.GLASS_BOTTLE, Math.max(0, 3 - wanted), CharEncoder.Inner_BufferHelper.init);
      }
   }

   private static boolean init(TableIndex.Inner_StreamUtil cell, BlockState live, Block block) {
      for (Entry<String, String> entry : cell.properties().entrySet()) {
         String key = entry.getKey();
         if (!key.equals("waterlogged") && !TableIndex.read(block, key)) {
            Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(key);
            if (property != null && !TableIndex.init(block, key, String.valueOf(live.getValue(property)), entry.getValue())) {
               TableIndex.Inner_TaskQueue strategy = TableIndex.init_TableIndex_TaskQue(block, key);
               if (strategy != TableIndex.Inner_TaskQueue.poll && strategy != TableIndex.Inner_TaskQueue.push) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static void init(TableIndex.Inner_StreamUtil cell, BlockState live, Block block, boolean same, List<CharEncoder.Inner_CachePool> out) {
      for (Entry<String, String> entry : cell.properties().entrySet()) {
         Item needed = TableIndex.init_Item(block, entry.getKey());
         if (needed != null) {
            Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)block.getStateDefinition().getProperty(entry.getKey());
            if (property != null) {
               BlockState initial = same ? live : block.defaultBlockState();
               String actual = String.valueOf(initial.getValue(property));
               String requested = entry.getValue();
               if (!TableIndex.init(block, entry.getKey(), actual, requested)) {
                  if (needed == Items.GLOWSTONE) {
                     int remaining = Math.max(0, Integer.parseInt(requested) - Integer.parseInt(actual));
                     init(out, CharEncoder.Inner_DataUtil.open, needed, remaining, CharEncoder.Inner_BufferHelper.init);
                  } else if (needed == Items.ENDER_EYE) {
                     if (Boolean.parseBoolean(requested) && !Boolean.parseBoolean(actual)) {
                        init(out, CharEncoder.Inner_DataUtil.open, needed, 1, CharEncoder.Inner_BufferHelper.init);
                     }
                  } else if (needed == Items.DIAMOND_SHOVEL) {
                     init(out, CharEncoder.Inner_DataUtil.open, "shovel", needed, CharEncoder.Inner_BufferHelper.load);
                  } else {
                     init(
                        out,
                        CharEncoder.Inner_DataUtil.open,
                        needed,
                        1,
                        needed == Items.FLINT_AND_STEEL ? CharEncoder.Inner_BufferHelper.load : CharEncoder.Inner_BufferHelper.init
                     );
                  }
               }
            }
         }
      }
   }

   private static void load(TableIndex.Inner_StreamUtil cell, BlockState live, Block block, boolean same, List<CharEncoder.Inner_CachePool> out) {
      if (cell.properties().containsKey("waterlogged") && block.defaultBlockState().hasProperty(BlockStateProperties.WATERLOGGED)) {
         boolean wanted = Boolean.parseBoolean(cell.properties().get("waterlogged"));
         boolean current = (same ? live : block.defaultBlockState()).getValue(BlockStateProperties.WATERLOGGED);
         if (wanted != current) {
            init(
               out,
               wanted ? CharEncoder.Inner_DataUtil.read : CharEncoder.Inner_DataUtil.write,
               wanted ? Items.WATER_BUCKET : Items.BUCKET,
               1,
               CharEncoder.Inner_BufferHelper.load
            );
         }
      }
   }

   private static boolean init(String id, BlockState live) {
      return live == null
         ? false
         : live.is(Blocks.DIRT)
            || live.is(Blocks.GRASS_BLOCK)
            || live.is(Blocks.DIRT_PATH)
            || live.is(Blocks.COARSE_DIRT)
            || live.is(Blocks.ROOTED_DIRT)
            || id.equals("minecraft:dirt_path") && (live.is(Blocks.MYCELIUM) || live.is(Blocks.PODZOL));
   }

   private static void init(List<CharEncoder.Inner_CachePool> out, Item item, boolean existing, int remaining) {
      if (remaining != 0) {
         if (existing) {
            init(out, CharEncoder.Inner_DataUtil.load, item, remaining, CharEncoder.Inner_BufferHelper.init);
         } else {
            init(out, CharEncoder.Inner_DataUtil.init, item, 1, CharEncoder.Inner_BufferHelper.init);
            init(out, CharEncoder.Inner_DataUtil.load, item, remaining - 1, CharEncoder.Inner_BufferHelper.init);
         }
      }
   }

   private static void init(List<CharEncoder.Inner_CachePool> out, CharEncoder.Inner_DataUtil action, Item item, int count, CharEncoder.Inner_BufferHelper use) {
      if (item != Items.AIR && count != 0) {
         init(out, action, BuiltInRegistries.ITEM.getKey(item).toString(), item, count, use);
      }
   }

   private static void init(
      List<CharEncoder.Inner_CachePool> out, CharEncoder.Inner_DataUtil action, String itemSpec, Item item, int count, CharEncoder.Inner_BufferHelper use
   ) {
      out.add(new CharEncoder.Inner_CachePool(action, itemSpec, item, count, use));
   }

   private static void init(
      List<CharEncoder.Inner_CachePool> out, CharEncoder.Inner_DataUtil action, String itemSpec, Item item, CharEncoder.Inner_BufferHelper use
   ) {
      init(out, action, itemSpec, item, 1, use);
   }

   enum Inner_BufferHelper {
      init,
      load;
   }

   record Inner_CachePool(CharEncoder.Inner_DataUtil action, String itemSpec, Item item, int count, CharEncoder.Inner_BufferHelper use) {
      Inner_CachePool {
         if (count < 1) {
            throw new IllegalArgumentException("A material requirement needs a positive count");
         }
      }
   }

   enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open;
   }

   private record Inner_StreamUtil(String itemSpec, CharEncoder.Inner_BufferHelper use) {
   }
}
