package autismclient.ai;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeaconBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

final class AiBuildIntent {
   private static final List<BlockPos> NEIGHBOR_OFFSETS = List.of(
      new BlockPos(0, -1, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)
   );
   private static final LevelReader EMPTY_LEVEL = emptyLevel();

   private AiBuildIntent() {
   }

   static AiBuildSite.Inner_CachePool kindOf(AiBuilder.CellSpec cell) {
      if (AiBuilder.isTransientPistonCell(cell)) {
         return AiBuildSite.Inner_CachePool.DERIVED;
      } else if (AiBuilder.isOmittedSchematicCell(cell)) {
         return AiBuildSite.Inner_CachePool.SKIP;
      } else if (AiBuilder.fluidOfCell(cell) != null) {
         return AiBuildSite.Inner_CachePool.FLUID;
      } else if (!unsupportedProperties(cell).isEmpty()) {
         return AiBuildSite.Inner_CachePool.SKIP;
      } else if (AiSchematicActions.isBedHead(cell) || "upper".equals(cell.properties().get("half")) || "head".equals(cell.properties().get("part"))) {
         return AiBuildSite.Inner_CachePool.PARTNER;
      } else if (AiSchematicActions.needsAction(cell)) {
         return AiBuildSite.Inner_CachePool.SPECIAL;
      } else if (isDoubleChestCell(cell)) {
         return AiBuildSite.Inner_CachePool.INTERACT;
      } else if (isPoweredLever(cell)) {
         return AiBuildSite.Inner_CachePool.SIMPLE;
      } else {
         return needsInteraction(cell) ? AiBuildSite.Inner_CachePool.INTERACT : AiBuildSite.Inner_CachePool.SIMPLE;
      }
   }

   static Item itemOf(AiBuilder.CellSpec cell) {
      return itemFor(cell, kindOf(cell));
   }

   static Item itemFor(AiBuilder.CellSpec cell, AiBuildSite.Inner_CachePool kind) {
      if (kind == AiBuildSite.Inner_CachePool.DERIVED || kind == AiBuildSite.Inner_CachePool.SKIP) {
         return Items.AIR;
      }

      if (kind == AiBuildSite.Inner_CachePool.FLUID) {
         if (AiBuilder.isFlowingFluidCell(cell)) {
            return Items.AIR;
         } else {
            return cell.block().equals("minecraft:lava") ? Items.LAVA_BUCKET : Items.WATER_BUCKET;
         }
      } else {
         if (kind == AiBuildSite.Inner_CachePool.SPECIAL) {
            for (Item material : AiSchematicActions.materials(cell)) {
               if (material != Items.AIR) {
                  return material;
               }
            }
         }

         if (kind == AiBuildSite.Inner_CachePool.PARTNER) {
            return AiBuilder.itemCountForCell(cell) == 0 ? Items.AIR : AiBuilder.toBlockState(cell).getBlock().asItem();
         } else {
            return AiBuilder.toBlockState(cell).getBlock().asItem();
         }
      }
   }

   static List<String> unsupportedProperties(AiBuilder.CellSpec cell) {
      return List.copyOf(AiBuilder.unsupportedProperties(cell));
   }

   static List<BlockPos> offsetsOf(AiBuilder.CellSpec cell) {
      return offsetsFor(cell, kindOf(cell));
   }

   static List<BlockPos> offsetsFor(AiBuilder.CellSpec cell, AiBuildSite.Inner_CachePool kind) {
      if (kind == AiBuildSite.Inner_CachePool.DERIVED || kind == AiBuildSite.Inner_CachePool.SKIP) {
         return List.of();
      } else if (kind == AiBuildSite.Inner_CachePool.FLUID) {
         return AiBuilder.isFlowingFluidCell(cell) ? List.of() : NEIGHBOR_OFFSETS;
      } else if (kind == AiBuildSite.Inner_CachePool.PARTNER) {
         Direction provider = supportProviderFace(cell);
         return provider == null ? List.of() : List.of(offsetOf(provider));
      } else if (kind == AiBuildSite.Inner_CachePool.SPECIAL) {
         return List.of();
      } else {
         Block block = blockOf(cell);
         Direction support = clickFace(cell, block);
         if (support != null) {
            return List.of(offsetOf(support));
         } else if (block instanceof FallingBlock) {
            return List.of(offsetOf(Direction.DOWN));
         } else if (block instanceof BeaconBlock) {
            return List.of(offsetOf(Direction.DOWN));
         } else if (!(block instanceof DoorBlock)
            && !(block instanceof BedBlock)
            && !(block instanceof DoublePlantBlock)
            && !(block instanceof FlowerPotBlock)
            && !(block instanceof CarpetBlock)) {
            return needsSupport(cell) ? List.of(offsetOf(Direction.DOWN)) : List.of();
         } else {
            return List.of(offsetOf(Direction.DOWN));
         }
      }
   }

   static Float requiredYaw(AiBuilder.CellSpec cell) {
      AiBuildSite.Inner_CachePool kind = kindOf(cell);
      if (kind != AiBuildSite.Inner_CachePool.SIMPLE && kind != AiBuildSite.Inner_CachePool.INTERACT) {
         return null;
      }

      autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint = placeConstraint(cell, BlockPos.ZERO);
      return constraint == null ? null : constraint.clickYaw();
   }

   static double yawTolerance(AiBuilder.CellSpec cell) {
      autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint = placeConstraint(cell, BlockPos.ZERO);
      return constraint == null ? 35.0 : constraint.yawTolerance();
   }

   static Predicate<Direction> allowedFaces(AiBuilder.CellSpec cell, BlockPos target) {
      AiBuildSite.Inner_CachePool kind = kindOf(cell);
      if (kind != AiBuildSite.Inner_CachePool.SIMPLE && kind != AiBuildSite.Inner_CachePool.INTERACT) {
         return null;
      } else {
         autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint = placeConstraint(cell, target == null ? BlockPos.ZERO : target);
         if (constraint == null) {
            return null;
         } else {
            return constraint.clickFace() == null && constraint.axis() == null && constraint.highHalf() == null && constraint.horizontalFace() == null
               ? null
               : constraint::acceptsFace;
         }
      }
   }

   static Boolean needsUpperHalfClick(AiBuilder.CellSpec cell) {
      AiBuildSite.Inner_CachePool kind = kindOf(cell);
      return kind != AiBuildSite.Inner_CachePool.SIMPLE && kind != AiBuildSite.Inner_CachePool.INTERACT ? null : AiBuilder.requiresUpperHalfClick(cell, blockOf(cell));
   }

   static boolean matches(AiBuilder.CellSpec cell, BlockState live) {
      return AiBuilder.matchesStrictState(cell, live);
   }

   static boolean matchesCell(AiBuilder.CellSpec cell, BlockState live) {
      return AiBuilder.matchesSchematicCell(cell, live);
   }

   static boolean matches(AiBuilder.CellSpec cell, BlockState live, Map<String, List<String>> substitutes) {
      if (matches(cell, live)) {
         return true;
      } else {
         return substitutes != null && !substitutes.isEmpty() ? AiBuilder.matchesCellOrSubstitute(cell, live, substitutes) : false;
      }
   }

   static Float unusedRequiredYaw(AiBuilder.CellSpec cell) {
      return requiredYaw(cell);
   }

   static Direction constraintClickFace(AiBuilder.CellSpec cell, BlockPos target) {
      autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint = placeConstraint(cell, target == null ? BlockPos.ZERO : target);
      return constraint == null ? null : constraint.clickFace();
   }

   static boolean isPlainOrInteract(AiBuilder.CellSpec cell) {
      AiBuildSite.Inner_CachePool kind = kindOf(cell);
      return kind == AiBuildSite.Inner_CachePool.SIMPLE || kind == AiBuildSite.Inner_CachePool.INTERACT;
   }

   static boolean isSatisfied(BlockPos target, BlockGetter world, Map<Long, AiBuilder.CellSpec> wanted) {
      if (target != null && world != null && wanted != null) {
         AiBuilder.CellSpec cell = wanted.get(target.asLong());
         if (cell == null) {
            return false;
         }

         if (AiBuilder.fluidOfCell(cell) != null && !AiBuilder.isFlowingFluidCell(cell)) {
            try {
               return AiSchematicActions.spillReason(target, world, wanted) == null;
            } catch (RuntimeException unknown) {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean differsOnlyInAutoProperties(AiBuilder.CellSpec cell, BlockState live) {
      if (cell != null && live != null) {
         try {
            return AiBuilder.differsOnlyInAutoProperties(cell, live);
         } catch (RuntimeException unknown) {
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean isWallMounted(AiBuilder.CellSpec cell) {
      try {
         return AiBuilder.isWallMountedBlock(blockOf(cell));
      } catch (RuntimeException unknown) {
         return false;
      }
   }

   static boolean isPoweredLever(AiBuilder.CellSpec cell) {
      return cell.block().equals("minecraft:lever") && "true".equals(cell.properties().get("powered"));
   }

   static AiBuilder.CellSpec unpoweredLever(AiBuilder.CellSpec cell) {
      if (!isPoweredLever(cell)) {
         return cell;
      }

      LinkedHashMap<String, String> properties = new LinkedHashMap<>(cell.properties());
      properties.remove("powered");
      return new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), cell.block(), Collections.unmodifiableMap(properties));
   }

   static AiBuilder.Blueprint stripPoweredLevers(AiBuilder.Blueprint blueprint) {
      return AiBuilder.stripPoweredLevers(blueprint);
   }

   static List<AiBuilder.CellSpec> poweredLevers(AiBuilder.Blueprint blueprint) {
      ArrayList<AiBuilder.CellSpec> controls = new ArrayList<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         if (isPoweredLever(cell)) {
            controls.add(cell);
         }
      }

      return List.copyOf(controls);
   }

   static autismclient.ai.engine.PlacementGoal.PlaceConstraint placeConstraint(AiBuilder.CellSpec cell, BlockPos target) {
      try {
         return AiBuilder.buildPlaceConstraint(cell, blockOf(cell), target, constraintLevel(cell));
      } catch (RuntimeException e) {
         return null;
      }
   }

   private static BlockGetter constraintLevel(AiBuilder.CellSpec cell) {
      if (!isDoubleChestCell(cell)) {
         return EmptyBlockGetter.INSTANCE;
      }

      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.level != null) {
            return mc.level;
         }
      } catch (RuntimeException | LinkageError var2) {
      }

      return EmptyBlockGetter.INSTANCE;
   }

   private static boolean isDoubleChestCell(AiBuilder.CellSpec cell) {
      String type = cell.properties().get("type");
      if (type != null && !type.equals("single") && cell.block().endsWith("chest")) {
         try {
            return blockOf(cell) instanceof ChestBlock;
         } catch (RuntimeException unknown) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static Block blockOf(AiBuilder.CellSpec cell) {
      return AiBuilder.toBlockState(cell).getBlock();
   }

   private static Direction supportProviderFace(AiBuilder.CellSpec cell) {
      if (!AiSchematicActions.isBedHead(cell)) {
         return Direction.DOWN;
      }

      Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
      return facing != null && facing.getAxis().isHorizontal() ? facing.getOpposite() : null;
   }

   private static Direction clickFace(AiBuilder.CellSpec cell, Block block) {
      if (AiBuilder.usesWallClickFace(block, cell)) {
         Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
         return facing == null ? null : facing.getOpposite();
      }

      if (block instanceof LanternBlock) {
         return "true".equals(cell.properties().get("hanging")) ? Direction.UP : Direction.DOWN;
      }

      if (block instanceof FaceAttachedHorizontalDirectionalBlock) {
         Direction var10000;
         switch ((String)cell.properties().getOrDefault("face", "wall")) {
            case "floor":
               var10000 = Direction.DOWN;
               break;
            case "ceiling":
               var10000 = Direction.UP;
               break;
            default:
               Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
               var10000 = facing == null ? null : facing.getOpposite();
         }

         return var10000;
      } else if (block instanceof ShulkerBoxBlock && cell.properties().containsKey("facing")) {
         Direction facing = Direction.byName(cell.properties().get("facing"));
         return facing == null ? null : facing.getOpposite();
      } else if (block instanceof HopperBlock && cell.properties().containsKey("facing")) {
         Direction facing = Direction.byName(cell.properties().get("facing"));
         return facing != null && facing != Direction.DOWN ? facing : Direction.DOWN;
      } else {
         return null;
      }
   }

   private static boolean needsSupport(AiBuilder.CellSpec cell) {
      try {
         return !AiBuilder.toBlockState(cell).canSurvive(EMPTY_LEVEL, BlockPos.ZERO);
      } catch (RuntimeException e) {
         return true;
      }
   }

   private static LevelReader emptyLevel() {
      return (LevelReader)Proxy.newProxyInstance(LevelReader.class.getClassLoader(), new Class[]{LevelReader.class}, (proxy, method, args) -> {
         Object var10000;
         switch (method.getName()) {
            case "getBlockState":
               var10000 = Blocks.AIR.defaultBlockState();
               break;
            case "getFluidState":
               var10000 = Blocks.AIR.defaultBlockState().getFluidState();
               break;
            case "isEmptyBlock":
               var10000 = true;
               break;
            case "hasChunkAt":
            case "hasChunk":
               var10000 = true;
               break;
            case "getMinY":
               var10000 = -64;
               break;
            case "getMaxY":
               var10000 = 320;
               break;
            case "getHeight":
               var10000 = 384;
               break;
            case "getBlockEntity":
               var10000 = null;
               break;
            case "toString":
               var10000 = "AiBuildIntent.emptyLevel";
               break;
            case "hashCode":
               var10000 = System.identityHashCode(proxy);
               break;
            case "equals":
               var10000 = proxy == args[0];
               break;
            default:
               throw new UnsupportedOperationException("empty level has no " + method.getName());
         }

         return var10000;
      });
   }

   static List<String> orientationProperties(AiBuilder.CellSpec cell) {
      Block block = blockOf(cell);
      ArrayList<String> out = new ArrayList<>(2);

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         String name = entry.getKey();
         if (!AiBuilder.isAutoComputedProperty(block, name)) {
            AiBuilder.OrientationKind strategy = name.equals("facing")
               ? AiBuilder.facingOrientationKind(cell, block)
               : AiBuilder.orientationKindForProperty(block, name);
            if ((strategy == AiBuilder.OrientationKind.POST_INTERACT || strategy == AiBuilder.OrientationKind.FLUID_INTERACT) && !defaultPropertyIs(block, name, entry.getValue())) {
               out.add(name);
            }
         }
      }

      return List.copyOf(out);
   }

   private static boolean needsInteraction(AiBuilder.CellSpec cell) {
      return !orientationProperties(cell).isEmpty();
   }

   private static boolean defaultPropertyIs(Block block, String propertyName, String requested) {
      Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)block.getStateDefinition().getProperty(propertyName);
      return property != null && String.valueOf(block.defaultBlockState().getValue(property)).equalsIgnoreCase(requested);
   }

   private static BlockPos offsetOf(Direction direction) {
      return new BlockPos(direction.getStepX(), direction.getStepY(), direction.getStepZ());
   }
}
