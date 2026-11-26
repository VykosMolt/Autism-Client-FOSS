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

final class AiCellMaterials {
   private AiCellMaterials() {
   }

   static List<AiCellMaterials.Inner_CachePool> init(AiBuilder.CellSpec cell) {
      return init(cell, null, false);
   }

   static List<AiCellMaterials.Inner_CachePool> init(AiBuilder.CellSpec cell, BlockState live, boolean waterAbove) {
      if (!AiBuilder.read_Z(cell) && (live == null || !AiBuilder.update(cell, live))) {
         if (!"upper".equals(cell.properties().get("half")) && !"head".equals(cell.properties().get("part"))) {
            ArrayList<AiCellMaterials.Inner_CachePool> out = new ArrayList<>();
            String id = cell.block();
            if (id.equals("minecraft:grass_block")) {
               init(out, AiCellMaterials.Inner_DataUtil.PLACE, Items.DIRT, 1, AiCellMaterials.Inner_BufferHelper.CONSUME);
               return List.copyOf(out);
            }

            if (!id.equals("minecraft:farmland") && !id.equals("minecraft:dirt_path")) {
               if (AiBuilder.init_Fluid(cell) != null) {
                  if (!AiBuilder.load_Z(cell)) {
                     init(
                        out,
                        AiCellMaterials.Inner_DataUtil.POUR,
                        id.equals("minecraft:lava") ? Items.LAVA_BUCKET : Items.WATER_BUCKET,
                        1,
                        AiCellMaterials.Inner_BufferHelper.REUSE
                     );
                  }

                  return List.copyOf(out);
               } else if (AiSchematicActions.load_Z(cell)) {
                  boolean present = live != null && live.is(Blocks.COMPOSTER);
                  int wanted = Integer.parseInt(cell.properties().get("level"));
                  int current = present ? live.getValue(ComposterBlock.LEVEL) : 0;
                  if (!present || current > wanted && current < 8) {
                     init(out, AiCellMaterials.Inner_DataUtil.PLACE, Items.COMPOSTER, 1, AiCellMaterials.Inner_BufferHelper.CONSUME);
                  }

                  if (current > wanted) {
                     current = 0;
                  }

                  int feeds = Math.max(0, (wanted == 8 ? 7 : wanted) - current);
                  init(out, AiCellMaterials.Inner_DataUtil.COMPOST, Items.PUMPKIN_PIE, feeds, AiCellMaterials.Inner_BufferHelper.CONSUME);
                  return List.copyOf(out);
               } else {
                  if (AiSchematicActions.write(cell)) {
                     init(cell, live, out);
                     return List.copyOf(out);
                  }

                  if (id.startsWith("minecraft:potted_")) {
                     if (live == null || !live.is(Blocks.FLOWER_POT)) {
                        init(out, AiCellMaterials.Inner_DataUtil.PLACE, Items.FLOWER_POT, 1, AiCellMaterials.Inner_BufferHelper.CONSUME);
                     }

                     Item plant = AiSchematicActions.init(AiBuilder.init_BlockState(cell).getBlock());
                     init(out, AiCellMaterials.Inner_DataUtil.PLANT, plant, 1, AiCellMaterials.Inner_BufferHelper.CONSUME);
                     return List.copyOf(out);
                  } else {
                     Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
                     boolean same = live != null && live.getBlock() == block;
                     Item item = block.asItem();
                     boolean existingDoubleSlab = same && block instanceof SlabBlock && live.getValue(SlabBlock.TYPE) == SlabType.DOUBLE;
                     String countProperty = null;

                     for (String property : cell.properties().keySet()) {
                        if (AiBuilder.init_Z(block, property)) {
                           countProperty = property;
                           break;
                        }
                     }

                     boolean replacing = false;
                     if (block instanceof MultifaceBlock) {
                        BlockState wanted = AiBuilder.init_BlockState(cell);
                        boolean partial = same && autismclient.ai.print.PrintRules.load_Z_BlockState_BlockState(wanted, live);
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
                     } else if (AiBuilder.init_Z(cell)) {
                        int remaining = !same ? 2 : (live.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 0 : 1);
                        init(out, item, same, remaining);
                     } else {
                        replacing = !same || !existingDoubleSlab && init(cell, live, block);
                        if (replacing) {
                           init(out, AiCellMaterials.Inner_DataUtil.PLACE, item, 1, AiCellMaterials.Inner_BufferHelper.CONSUME);
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
                        init(out, AiCellMaterials.Inner_DataUtil.INTERACT, Items.REDSTONE_BLOCK, 1, AiCellMaterials.Inner_BufferHelper.REUSE);
                     }

                     return List.copyOf(out);
                  }
               }
            } else {
               if (!init(id, live)) {
                  init(out, AiCellMaterials.Inner_DataUtil.PLACE, Items.DIRT, 1, AiCellMaterials.Inner_BufferHelper.CONSUME);
               }

               boolean farmland = id.equals("minecraft:farmland");
               init(
                  out,
                  AiCellMaterials.Inner_DataUtil.CONVERT,
                  farmland ? "hoe" : "shovel",
                  farmland ? Items.WOODEN_HOE : Items.WOODEN_SHOVEL,
                  AiCellMaterials.Inner_BufferHelper.REUSE
               );
               if (waterAbove) {
                  init(out, AiCellMaterials.Inner_DataUtil.DRAIN, Items.BUCKET, 1, AiCellMaterials.Inner_BufferHelper.REUSE);
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

   static List<AiCellMaterials.Inner_CachePool> init(List<AiBuilder.CellSpec> cells, BlockPos origin, LevelReader world) {
      HashSet<Long> sourceCells = new HashSet<>();

      for (AiBuilder.CellSpec cell : cells) {
         sourceCells.add(origin.offset(cell.dx(), cell.dy(), cell.dz()).asLong());
      }

      ArrayList<AiCellMaterials.Inner_CachePool> out = new ArrayList<>();
      HashSet<BlockPos> foundations = new HashSet<>();

      for (AiBuilder.CellSpec cell : cells) {
         if (!AiBuilder.read_Z(cell)) {
            BlockPos target = origin.offset(cell.dx(), cell.dy(), cell.dz());
            BlockState live = world.getBlockState(target);
            if (!AiBuilder.update(cell, live)) {
               out.addAll(init(cell, live, world.getFluidState(target.above()).is(FluidTags.WATER)));
               BlockPos foundation = AiSchematicActions.init_BlockPos_TableIndex_StreamU_2(cell, target, world, sourceCells);
               if (foundation != null && foundations.add(foundation)) {
                  init(out, AiCellMaterials.Inner_DataUtil.PLACE, Items.DIRT, 1, AiCellMaterials.Inner_BufferHelper.CONSUME);
               }
            }
         }
      }

      return List.copyOf(out);
   }

   static List<AiCellMaterials.Inner_CachePool> init_List(Iterable<AiCellMaterials.Inner_CachePool> requirements) {
      LinkedHashMap<AiCellMaterials.Inner_StreamUtil, AiCellMaterials.Inner_CachePool> combined = new LinkedHashMap<>();

      for (AiCellMaterials.Inner_CachePool requirement : requirements) {
         AiCellMaterials.Inner_StreamUtil key = new AiCellMaterials.Inner_StreamUtil(requirement.itemSpec(), requirement.use());
         AiCellMaterials.Inner_CachePool previous = combined.get(key);
         if (previous == null) {
            combined.put(key, requirement);
         } else if (requirement.use() == AiCellMaterials.Inner_BufferHelper.CONSUME) {
            combined.put(
               key,
               new AiCellMaterials.Inner_CachePool(previous.action(), previous.itemSpec(), previous.item(), previous.count() + requirement.count(), previous.use())
            );
         }
      }

      return List.copyOf(combined.values());
   }

   static Map<Item, Integer> init_Map(Iterable<AiCellMaterials.Inner_CachePool> requirements) {
      LinkedHashMap<Item, Integer> out = new LinkedHashMap<>();

      for (AiCellMaterials.Inner_CachePool requirement : init_List(requirements)) {
         out.merge(requirement.item(), requirement.count(), Integer::sum);
      }

      return out;
   }

   private static void init(AiBuilder.CellSpec cell, BlockState live, List<AiCellMaterials.Inner_CachePool> out) {
      boolean present = live != null && live.getBlock() instanceof AbstractCauldronBlock;
      if (!present) {
         init(out, AiCellMaterials.Inner_DataUtil.PLACE, Items.CAULDRON, 1, AiCellMaterials.Inner_BufferHelper.CONSUME);
      }

      Block wantedBlock = BuiltInRegistries.BLOCK.getValue(Identifier.parse(cell.block()));
      boolean water = cell.block().equals("minecraft:water_cauldron");
      boolean powderSnow = cell.block().equals("minecraft:powder_snow_cauldron");
      int wanted = !water && !powderSnow ? 3 : Integer.parseInt(cell.properties().getOrDefault("level", "3"));
      boolean wrongContents = present && !live.is(Blocks.CAULDRON) && live.getBlock() != wantedBlock;
      if (wrongContents) {
         init(out, AiCellMaterials.Inner_DataUtil.DRAIN, Items.BUCKET, 1, AiCellMaterials.Inner_BufferHelper.REUSE);
      }

      boolean partialFill = AiSchematicActions.init_Z(cell, live);
      boolean fill = !present || live.is(Blocks.CAULDRON) || wrongContents || partialFill;
      if (fill) {
         init(out, AiCellMaterials.Inner_DataUtil.FILL, AiSchematicActions.init_Item(cell), 1, AiCellMaterials.Inner_BufferHelper.CONSUME);
      }

      if (!water) {
         if (powderSnow && !partialFill && present && live.getBlock() == wantedBlock && live.getValue(LayeredCauldronBlock.LEVEL) != wanted) {
            init(out, AiCellMaterials.Inner_DataUtil.CONVERT, Items.CAULDRON, 1, AiCellMaterials.Inner_BufferHelper.REUSE);
         }
      } else if (present && live.getBlock() == wantedBlock) {
         int current = live.getValue(LayeredCauldronBlock.LEVEL);
         if (current > wanted) {
            init(out, AiCellMaterials.Inner_DataUtil.DRAIN, Items.GLASS_BOTTLE, current - wanted, AiCellMaterials.Inner_BufferHelper.CONSUME);
         } else if (current < wanted) {
            init(out, AiCellMaterials.Inner_DataUtil.DRAIN, Items.GLASS_BOTTLE, 3 - wanted, AiCellMaterials.Inner_BufferHelper.CONSUME);
         }
      } else if (fill) {
         init(out, AiCellMaterials.Inner_DataUtil.DRAIN, Items.GLASS_BOTTLE, Math.max(0, 3 - wanted), AiCellMaterials.Inner_BufferHelper.CONSUME);
      }
   }

   private static boolean init(AiBuilder.CellSpec cell, BlockState live, Block block) {
      for (Entry<String, String> entry : cell.properties().entrySet()) {
         String key = entry.getKey();
         if (!key.equals("waterlogged") && !AiBuilder.read(block, key)) {
            Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(key);
            if (property != null && !AiBuilder.init(block, key, String.valueOf(live.getValue(property)), entry.getValue())) {
               AiBuilder.OrientationKind strategy = AiBuilder.init_TableIndex_TaskQue(block, key);
               if (strategy != AiBuilder.OrientationKind.POST_INTERACT && strategy != AiBuilder.OrientationKind.FLUID_INTERACT) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static void init(AiBuilder.CellSpec cell, BlockState live, Block block, boolean same, List<AiCellMaterials.Inner_CachePool> out) {
      for (Entry<String, String> entry : cell.properties().entrySet()) {
         Item needed = AiBuilder.init_Item(block, entry.getKey());
         if (needed != null) {
            Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)block.getStateDefinition().getProperty(entry.getKey());
            if (property != null) {
               BlockState initial = same ? live : block.defaultBlockState();
               String actual = String.valueOf(initial.getValue(property));
               String requested = entry.getValue();
               if (!AiBuilder.init(block, entry.getKey(), actual, requested)) {
                  if (needed == Items.GLOWSTONE) {
                     int remaining = Math.max(0, Integer.parseInt(requested) - Integer.parseInt(actual));
                     init(out, AiCellMaterials.Inner_DataUtil.INTERACT, needed, remaining, AiCellMaterials.Inner_BufferHelper.CONSUME);
                  } else if (needed == Items.ENDER_EYE) {
                     if (Boolean.parseBoolean(requested) && !Boolean.parseBoolean(actual)) {
                        init(out, AiCellMaterials.Inner_DataUtil.INTERACT, needed, 1, AiCellMaterials.Inner_BufferHelper.CONSUME);
                     }
                  } else if (needed == Items.DIAMOND_SHOVEL) {
                     init(out, AiCellMaterials.Inner_DataUtil.INTERACT, "shovel", needed, AiCellMaterials.Inner_BufferHelper.REUSE);
                  } else {
                     init(
                        out,
                        AiCellMaterials.Inner_DataUtil.INTERACT,
                        needed,
                        1,
                        needed == Items.FLINT_AND_STEEL ? AiCellMaterials.Inner_BufferHelper.REUSE : AiCellMaterials.Inner_BufferHelper.CONSUME
                     );
                  }
               }
            }
         }
      }
   }

   private static void load(AiBuilder.CellSpec cell, BlockState live, Block block, boolean same, List<AiCellMaterials.Inner_CachePool> out) {
      if (cell.properties().containsKey("waterlogged") && block.defaultBlockState().hasProperty(BlockStateProperties.WATERLOGGED)) {
         boolean wanted = Boolean.parseBoolean(cell.properties().get("waterlogged"));
         boolean current = (same ? live : block.defaultBlockState()).getValue(BlockStateProperties.WATERLOGGED);
         if (wanted != current) {
            init(
               out,
               wanted ? AiCellMaterials.Inner_DataUtil.POUR : AiCellMaterials.Inner_DataUtil.DRAIN,
               wanted ? Items.WATER_BUCKET : Items.BUCKET,
               1,
               AiCellMaterials.Inner_BufferHelper.REUSE
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

   private static void init(List<AiCellMaterials.Inner_CachePool> out, Item item, boolean existing, int remaining) {
      if (remaining != 0) {
         if (existing) {
            init(out, AiCellMaterials.Inner_DataUtil.ADD, item, remaining, AiCellMaterials.Inner_BufferHelper.CONSUME);
         } else {
            init(out, AiCellMaterials.Inner_DataUtil.PLACE, item, 1, AiCellMaterials.Inner_BufferHelper.CONSUME);
            init(out, AiCellMaterials.Inner_DataUtil.ADD, item, remaining - 1, AiCellMaterials.Inner_BufferHelper.CONSUME);
         }
      }
   }

   private static void init(List<AiCellMaterials.Inner_CachePool> out, AiCellMaterials.Inner_DataUtil action, Item item, int count, AiCellMaterials.Inner_BufferHelper use) {
      if (item != Items.AIR && count != 0) {
         init(out, action, BuiltInRegistries.ITEM.getKey(item).toString(), item, count, use);
      }
   }

   private static void init(
      List<AiCellMaterials.Inner_CachePool> out, AiCellMaterials.Inner_DataUtil action, String itemSpec, Item item, int count, AiCellMaterials.Inner_BufferHelper use
   ) {
      out.add(new AiCellMaterials.Inner_CachePool(action, itemSpec, item, count, use));
   }

   private static void init(
      List<AiCellMaterials.Inner_CachePool> out, AiCellMaterials.Inner_DataUtil action, String itemSpec, Item item, AiCellMaterials.Inner_BufferHelper use
   ) {
      init(out, action, itemSpec, item, 1, use);
   }

   enum Inner_BufferHelper {
      CONSUME,
      REUSE;
   }

   record Inner_CachePool(AiCellMaterials.Inner_DataUtil action, String itemSpec, Item item, int count, AiCellMaterials.Inner_BufferHelper use) {
      Inner_CachePool {
         if (count < 1) {
            throw new IllegalArgumentException("A material requirement needs a positive count");
         }
      }
   }

   enum Inner_DataUtil {
      PLACE,
      ADD,
      POUR,
      DRAIN,
      CONVERT,
      COMPOST,
      FILL,
      PLANT,
      INTERACT;
   }

   private record Inner_StreamUtil(String itemSpec, AiCellMaterials.Inner_BufferHelper use) {
   }
}
