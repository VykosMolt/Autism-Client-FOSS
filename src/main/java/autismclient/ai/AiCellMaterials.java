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

   static List<AiCellMaterials.MaterialRequirement> init(AiBuilder.CellSpec cell) {
      return init(cell, null, false);
   }

   static List<AiCellMaterials.MaterialRequirement> init(AiBuilder.CellSpec cell, BlockState live, boolean waterAbove) {
      if (!AiBuilder.isTransientPistonCell(cell) && (live == null || !AiBuilder.matchesSchematicCell(cell, live))) {
         if (!"upper".equals(cell.properties().get("half")) && !"head".equals(cell.properties().get("part"))) {
            ArrayList<AiCellMaterials.MaterialRequirement> out = new ArrayList<>();
            String id = cell.block();
            if (id.equals("minecraft:grass_block")) {
               init(out, AiCellMaterials.CellAction.PLACE, Items.DIRT, 1, AiCellMaterials.MaterialUse.CONSUME);
               return List.copyOf(out);
            }

            if (!id.equals("minecraft:farmland") && !id.equals("minecraft:dirt_path")) {
               if (AiBuilder.fluidOfCell(cell) != null) {
                  if (!AiBuilder.isFlowingFluidCell(cell)) {
                     init(
                        out,
                        AiCellMaterials.CellAction.POUR,
                        id.equals("minecraft:lava") ? Items.LAVA_BUCKET : Items.WATER_BUCKET,
                        1,
                        AiCellMaterials.MaterialUse.REUSE
                     );
                  }

                  return List.copyOf(out);
               } else if (AiSchematicActions.isFilledComposter(cell)) {
                  boolean present = live != null && live.is(Blocks.COMPOSTER);
                  int wanted = Integer.parseInt(cell.properties().get("level"));
                  int current = present ? live.getValue(ComposterBlock.LEVEL) : 0;
                  if (!present || current > wanted && current < 8) {
                     init(out, AiCellMaterials.CellAction.PLACE, Items.COMPOSTER, 1, AiCellMaterials.MaterialUse.CONSUME);
                  }

                  if (current > wanted) {
                     current = 0;
                  }

                  int feeds = Math.max(0, (wanted == 8 ? 7 : wanted) - current);
                  init(out, AiCellMaterials.CellAction.COMPOST, Items.PUMPKIN_PIE, feeds, AiCellMaterials.MaterialUse.CONSUME);
                  return List.copyOf(out);
               } else {
                  if (AiSchematicActions.write(cell)) {
                     init(cell, live, out);
                     return List.copyOf(out);
                  }

                  if (id.startsWith("minecraft:potted_")) {
                     if (live == null || !live.is(Blocks.FLOWER_POT)) {
                        init(out, AiCellMaterials.CellAction.PLACE, Items.FLOWER_POT, 1, AiCellMaterials.MaterialUse.CONSUME);
                     }

                     Item plant = AiSchematicActions.init(AiBuilder.toBlockState(cell).getBlock());
                     init(out, AiCellMaterials.CellAction.PLANT, plant, 1, AiCellMaterials.MaterialUse.CONSUME);
                     return List.copyOf(out);
                  } else {
                     Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
                     boolean same = live != null && live.getBlock() == block;
                     Item item = block.asItem();
                     boolean existingDoubleSlab = same && block instanceof SlabBlock && live.getValue(SlabBlock.TYPE) == SlabType.DOUBLE;
                     String countProperty = null;

                     for (String property : cell.properties().keySet()) {
                        if (AiBuilder.isStackableCountProperty(block, property)) {
                           countProperty = property;
                           break;
                        }
                     }

                     boolean replacing = false;
                     if (block instanceof MultifaceBlock) {
                        BlockState wanted = AiBuilder.toBlockState(cell);
                        boolean partial = same && autismclient.ai.print.PrintRules.facesWithinSavedShape(wanted, live);
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
                     } else if (AiBuilder.isDoubleSlabCell(cell)) {
                        int remaining = !same ? 2 : (live.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 0 : 1);
                        init(out, item, same, remaining);
                     } else {
                        replacing = !same || !existingDoubleSlab && init(cell, live, block);
                        if (replacing) {
                           init(out, AiCellMaterials.CellAction.PLACE, item, 1, AiCellMaterials.MaterialUse.CONSUME);
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
                        init(out, AiCellMaterials.CellAction.INTERACT, Items.REDSTONE_BLOCK, 1, AiCellMaterials.MaterialUse.REUSE);
                     }

                     return List.copyOf(out);
                  }
               }
            } else {
               if (!init(id, live)) {
                  init(out, AiCellMaterials.CellAction.PLACE, Items.DIRT, 1, AiCellMaterials.MaterialUse.CONSUME);
               }

               boolean farmland = id.equals("minecraft:farmland");
               init(
                  out,
                  AiCellMaterials.CellAction.CONVERT,
                  farmland ? "hoe" : "shovel",
                  farmland ? Items.WOODEN_HOE : Items.WOODEN_SHOVEL,
                  AiCellMaterials.MaterialUse.REUSE
               );
               if (waterAbove) {
                  init(out, AiCellMaterials.CellAction.DRAIN, Items.BUCKET, 1, AiCellMaterials.MaterialUse.REUSE);
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

   static List<AiCellMaterials.MaterialRequirement> init(List<AiBuilder.CellSpec> cells, BlockPos origin, LevelReader world) {
      HashSet<Long> sourceCells = new HashSet<>();

      for (AiBuilder.CellSpec cell : cells) {
         sourceCells.add(origin.offset(cell.dx(), cell.dy(), cell.dz()).asLong());
      }

      ArrayList<AiCellMaterials.MaterialRequirement> out = new ArrayList<>();
      HashSet<BlockPos> foundations = new HashSet<>();

      for (AiBuilder.CellSpec cell : cells) {
         if (!AiBuilder.isTransientPistonCell(cell)) {
            BlockPos target = origin.offset(cell.dx(), cell.dy(), cell.dz());
            BlockState live = world.getBlockState(target);
            if (!AiBuilder.matchesSchematicCell(cell, live)) {
               out.addAll(init(cell, live, world.getFluidState(target.above()).is(FluidTags.WATER)));
               BlockPos foundation = AiSchematicActions.foundationFor(cell, target, world, sourceCells);
               if (foundation != null && foundations.add(foundation)) {
                  init(out, AiCellMaterials.CellAction.PLACE, Items.DIRT, 1, AiCellMaterials.MaterialUse.CONSUME);
               }
            }
         }
      }

      return List.copyOf(out);
   }

   static List<AiCellMaterials.MaterialRequirement> combine(Iterable<AiCellMaterials.MaterialRequirement> requirements) {
      LinkedHashMap<AiCellMaterials.RequirementKey, AiCellMaterials.MaterialRequirement> combined = new LinkedHashMap<>();

      for (AiCellMaterials.MaterialRequirement requirement : requirements) {
         AiCellMaterials.RequirementKey key = new AiCellMaterials.RequirementKey(requirement.itemSpec(), requirement.use());
         AiCellMaterials.MaterialRequirement previous = combined.get(key);
         if (previous == null) {
            combined.put(key, requirement);
         } else if (requirement.use() == AiCellMaterials.MaterialUse.CONSUME) {
            combined.put(
               key,
               new AiCellMaterials.MaterialRequirement(previous.action(), previous.itemSpec(), previous.item(), previous.count() + requirement.count(), previous.use())
            );
         }
      }

      return List.copyOf(combined.values());
   }

   static Map<Item, Integer> itemCounts(Iterable<AiCellMaterials.MaterialRequirement> requirements) {
      LinkedHashMap<Item, Integer> out = new LinkedHashMap<>();

      for (AiCellMaterials.MaterialRequirement requirement : combine(requirements)) {
         out.merge(requirement.item(), requirement.count(), Integer::sum);
      }

      return out;
   }

   private static void init(AiBuilder.CellSpec cell, BlockState live, List<AiCellMaterials.MaterialRequirement> out) {
      boolean present = live != null && live.getBlock() instanceof AbstractCauldronBlock;
      if (!present) {
         init(out, AiCellMaterials.CellAction.PLACE, Items.CAULDRON, 1, AiCellMaterials.MaterialUse.CONSUME);
      }

      Block wantedBlock = BuiltInRegistries.BLOCK.getValue(Identifier.parse(cell.block()));
      boolean water = cell.block().equals("minecraft:water_cauldron");
      boolean powderSnow = cell.block().equals("minecraft:powder_snow_cauldron");
      int wanted = !water && !powderSnow ? 3 : Integer.parseInt(cell.properties().getOrDefault("level", "3"));
      boolean wrongContents = present && !live.is(Blocks.CAULDRON) && live.getBlock() != wantedBlock;
      if (wrongContents) {
         init(out, AiCellMaterials.CellAction.DRAIN, Items.BUCKET, 1, AiCellMaterials.MaterialUse.REUSE);
      }

      boolean partialFill = AiSchematicActions.isPartialFill(cell, live);
      boolean fill = !present || live.is(Blocks.CAULDRON) || wrongContents || partialFill;
      if (fill) {
         init(out, AiCellMaterials.CellAction.FILL, AiSchematicActions.cauldronFillItem(cell), 1, AiCellMaterials.MaterialUse.CONSUME);
      }

      if (!water) {
         if (powderSnow && !partialFill && present && live.getBlock() == wantedBlock && live.getValue(LayeredCauldronBlock.LEVEL) != wanted) {
            init(out, AiCellMaterials.CellAction.CONVERT, Items.CAULDRON, 1, AiCellMaterials.MaterialUse.REUSE);
         }
      } else if (present && live.getBlock() == wantedBlock) {
         int current = live.getValue(LayeredCauldronBlock.LEVEL);
         if (current > wanted) {
            init(out, AiCellMaterials.CellAction.DRAIN, Items.GLASS_BOTTLE, current - wanted, AiCellMaterials.MaterialUse.CONSUME);
         } else if (current < wanted) {
            init(out, AiCellMaterials.CellAction.DRAIN, Items.GLASS_BOTTLE, 3 - wanted, AiCellMaterials.MaterialUse.CONSUME);
         }
      } else if (fill) {
         init(out, AiCellMaterials.CellAction.DRAIN, Items.GLASS_BOTTLE, Math.max(0, 3 - wanted), AiCellMaterials.MaterialUse.CONSUME);
      }
   }

   private static boolean init(AiBuilder.CellSpec cell, BlockState live, Block block) {
      for (Entry<String, String> entry : cell.properties().entrySet()) {
         String key = entry.getKey();
         if (!key.equals("waterlogged") && !AiBuilder.isAutoComputedProperty(block, key)) {
            Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(key);
            if (property != null && !AiBuilder.propertyValuesMatch(block, key, String.valueOf(live.getValue(property)), entry.getValue())) {
               AiBuilder.OrientationKind strategy = AiBuilder.orientationKindForProperty(block, key);
               if (strategy != AiBuilder.OrientationKind.POST_INTERACT && strategy != AiBuilder.OrientationKind.FLUID_INTERACT) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static void init(AiBuilder.CellSpec cell, BlockState live, Block block, boolean same, List<AiCellMaterials.MaterialRequirement> out) {
      for (Entry<String, String> entry : cell.properties().entrySet()) {
         Item needed = AiBuilder.postInteractToolItem(block, entry.getKey());
         if (needed != null) {
            Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)block.getStateDefinition().getProperty(entry.getKey());
            if (property != null) {
               BlockState initial = same ? live : block.defaultBlockState();
               String actual = String.valueOf(initial.getValue(property));
               String requested = entry.getValue();
               if (!AiBuilder.propertyValuesMatch(block, entry.getKey(), actual, requested)) {
                  if (needed == Items.GLOWSTONE) {
                     int remaining = Math.max(0, Integer.parseInt(requested) - Integer.parseInt(actual));
                     init(out, AiCellMaterials.CellAction.INTERACT, needed, remaining, AiCellMaterials.MaterialUse.CONSUME);
                  } else if (needed == Items.ENDER_EYE) {
                     if (Boolean.parseBoolean(requested) && !Boolean.parseBoolean(actual)) {
                        init(out, AiCellMaterials.CellAction.INTERACT, needed, 1, AiCellMaterials.MaterialUse.CONSUME);
                     }
                  } else if (needed == Items.DIAMOND_SHOVEL) {
                     init(out, AiCellMaterials.CellAction.INTERACT, "shovel", needed, AiCellMaterials.MaterialUse.REUSE);
                  } else {
                     init(
                        out,
                        AiCellMaterials.CellAction.INTERACT,
                        needed,
                        1,
                        needed == Items.FLINT_AND_STEEL ? AiCellMaterials.MaterialUse.REUSE : AiCellMaterials.MaterialUse.CONSUME
                     );
                  }
               }
            }
         }
      }
   }

   private static void load(AiBuilder.CellSpec cell, BlockState live, Block block, boolean same, List<AiCellMaterials.MaterialRequirement> out) {
      if (cell.properties().containsKey("waterlogged") && block.defaultBlockState().hasProperty(BlockStateProperties.WATERLOGGED)) {
         boolean wanted = Boolean.parseBoolean(cell.properties().get("waterlogged"));
         boolean current = (same ? live : block.defaultBlockState()).getValue(BlockStateProperties.WATERLOGGED);
         if (wanted != current) {
            init(
               out,
               wanted ? AiCellMaterials.CellAction.POUR : AiCellMaterials.CellAction.DRAIN,
               wanted ? Items.WATER_BUCKET : Items.BUCKET,
               1,
               AiCellMaterials.MaterialUse.REUSE
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

   private static void init(List<AiCellMaterials.MaterialRequirement> out, Item item, boolean existing, int remaining) {
      if (remaining != 0) {
         if (existing) {
            init(out, AiCellMaterials.CellAction.ADD, item, remaining, AiCellMaterials.MaterialUse.CONSUME);
         } else {
            init(out, AiCellMaterials.CellAction.PLACE, item, 1, AiCellMaterials.MaterialUse.CONSUME);
            init(out, AiCellMaterials.CellAction.ADD, item, remaining - 1, AiCellMaterials.MaterialUse.CONSUME);
         }
      }
   }

   private static void init(List<AiCellMaterials.MaterialRequirement> out, AiCellMaterials.CellAction action, Item item, int count, AiCellMaterials.MaterialUse use) {
      if (item != Items.AIR && count != 0) {
         init(out, action, BuiltInRegistries.ITEM.getKey(item).toString(), item, count, use);
      }
   }

   private static void init(
      List<AiCellMaterials.MaterialRequirement> out, AiCellMaterials.CellAction action, String itemSpec, Item item, int count, AiCellMaterials.MaterialUse use
   ) {
      out.add(new AiCellMaterials.MaterialRequirement(action, itemSpec, item, count, use));
   }

   private static void init(
      List<AiCellMaterials.MaterialRequirement> out, AiCellMaterials.CellAction action, String itemSpec, Item item, AiCellMaterials.MaterialUse use
   ) {
      init(out, action, itemSpec, item, 1, use);
   }

   enum MaterialUse {
      CONSUME,
      REUSE;
   }

   record MaterialRequirement(AiCellMaterials.CellAction action, String itemSpec, Item item, int count, AiCellMaterials.MaterialUse use) {
      MaterialRequirement {
         if (count < 1) {
            throw new IllegalArgumentException("A material requirement needs a positive count");
         }
      }
   }

   enum CellAction {
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

   private record RequirementKey(String itemSpec, AiCellMaterials.MaterialUse use) {
   }
}
