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
   private static final List<BlockPos> init_List = List.of(
      new BlockPos(0, -1, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)
   );
   private static final LevelReader init_LevelReader = init();

   private AiBuildIntent() {
   }

   static AiBuildSite.Inner_CachePool init_IoWorker_CachePool(AiBuilder.Inner_StreamUtil cell) {
      if (AiBuilder.read_Z(cell)) {
         return AiBuildSite.Inner_CachePool.sync;
      } else if (AiBuilder.update(cell)) {
         return AiBuildSite.Inner_CachePool.flush;
      } else if (AiBuilder.init_Fluid(cell) != null) {
         return AiBuildSite.Inner_CachePool.write;
      } else if (!init_List(cell).isEmpty()) {
         return AiBuildSite.Inner_CachePool.flush;
      } else if (AiSchematicActions.read_Z(cell) || "upper".equals(cell.properties().get("half")) || "head".equals(cell.properties().get("part"))) {
         return AiBuildSite.Inner_CachePool.update;
      } else if (AiSchematicActions.init_Z(cell)) {
         return AiBuildSite.Inner_CachePool.read;
      } else if (write(cell)) {
         return AiBuildSite.Inner_CachePool.load;
      } else if (read_Z(cell)) {
         return AiBuildSite.Inner_CachePool.init;
      } else {
         return sync(cell) ? AiBuildSite.Inner_CachePool.load : AiBuildSite.Inner_CachePool.init;
      }
   }

   static Item init_Item(AiBuilder.Inner_StreamUtil cell) {
      return init_Item(cell, init_IoWorker_CachePool(cell));
   }

   static Item init_Item(AiBuilder.Inner_StreamUtil cell, AiBuildSite.Inner_CachePool kind) {
      if (kind == AiBuildSite.Inner_CachePool.sync || kind == AiBuildSite.Inner_CachePool.flush) {
         return Items.AIR;
      }

      if (kind == AiBuildSite.Inner_CachePool.write) {
         if (AiBuilder.load_Z(cell)) {
            return Items.AIR;
         } else {
            return cell.block().equals("minecraft:lava") ? Items.LAVA_BUCKET : Items.WATER_BUCKET;
         }
      } else {
         if (kind == AiBuildSite.Inner_CachePool.read) {
            for (Item material : AiSchematicActions.read_List(cell)) {
               if (material != Items.AIR) {
                  return material;
               }
            }
         }

         if (kind == AiBuildSite.Inner_CachePool.update) {
            return AiBuilder.init_I(cell) == 0 ? Items.AIR : AiBuilder.init_BlockState(cell).getBlock().asItem();
         } else {
            return AiBuilder.init_BlockState(cell).getBlock().asItem();
         }
      }
   }

   static List<String> init_List(AiBuilder.Inner_StreamUtil cell) {
      return List.copyOf(AiBuilder.init_List(cell));
   }

   static List<BlockPos> load_List(AiBuilder.Inner_StreamUtil cell) {
      return init_List(cell, init_IoWorker_CachePool(cell));
   }

   static List<BlockPos> init_List(AiBuilder.Inner_StreamUtil cell, AiBuildSite.Inner_CachePool kind) {
      if (kind == AiBuildSite.Inner_CachePool.sync || kind == AiBuildSite.Inner_CachePool.flush) {
         return List.of();
      } else if (kind == AiBuildSite.Inner_CachePool.write) {
         return AiBuilder.load_Z(cell) ? List.of() : init_List;
      } else if (kind == AiBuildSite.Inner_CachePool.update) {
         Direction provider = init_Direction(cell);
         return provider == null ? List.of() : List.of(init(provider));
      } else if (kind == AiBuildSite.Inner_CachePool.read) {
         return List.of();
      } else {
         Block block = init_Block(cell);
         Direction support = init(cell, block);
         if (support != null) {
            return List.of(init(support));
         } else if (block instanceof FallingBlock) {
            return List.of(init(Direction.DOWN));
         } else if (block instanceof BeaconBlock) {
            return List.of(init(Direction.DOWN));
         } else if (!(block instanceof DoorBlock)
            && !(block instanceof BedBlock)
            && !(block instanceof DoublePlantBlock)
            && !(block instanceof FlowerPotBlock)
            && !(block instanceof CarpetBlock)) {
            return update(cell) ? List.of(init(Direction.DOWN)) : List.of();
         } else {
            return List.of(init(Direction.DOWN));
         }
      }
   }

   static Float init_Float(AiBuilder.Inner_StreamUtil cell) {
      AiBuildSite.Inner_CachePool kind = init_IoWorker_CachePool(cell);
      if (kind != AiBuildSite.Inner_CachePool.init && kind != AiBuildSite.Inner_CachePool.load) {
         return null;
      }

      autismclient.ai.engine.PlacementGoal.Inner_DataUtil constraint = init_u_DataUtil(cell, BlockPos.ZERO);
      return constraint == null ? null : constraint.clickYaw();
   }

   static double init_D(AiBuilder.Inner_StreamUtil cell) {
      autismclient.ai.engine.PlacementGoal.Inner_DataUtil constraint = init_u_DataUtil(cell, BlockPos.ZERO);
      return constraint == null ? 35.0 : constraint.yawTolerance();
   }

   static Predicate<Direction> init_Predicate(AiBuilder.Inner_StreamUtil cell, BlockPos target) {
      AiBuildSite.Inner_CachePool kind = init_IoWorker_CachePool(cell);
      if (kind != AiBuildSite.Inner_CachePool.init && kind != AiBuildSite.Inner_CachePool.load) {
         return null;
      } else {
         autismclient.ai.engine.PlacementGoal.Inner_DataUtil constraint = init_u_DataUtil(cell, target == null ? BlockPos.ZERO : target);
         if (constraint == null) {
            return null;
         } else {
            return constraint.clickFace() == null && constraint.axis() == null && constraint.highHalf() == null && constraint.horizontalFace() == null
               ? null
               : constraint::acceptsFace;
         }
      }
   }

   static Boolean init_Boolean(AiBuilder.Inner_StreamUtil cell) {
      AiBuildSite.Inner_CachePool kind = init_IoWorker_CachePool(cell);
      return kind != AiBuildSite.Inner_CachePool.init && kind != AiBuildSite.Inner_CachePool.load ? null : AiBuilder.init_Boolean(cell, init_Block(cell));
   }

   static boolean init(AiBuilder.Inner_StreamUtil cell, BlockState live) {
      return AiBuilder.sync(cell, live);
   }

   static boolean load(AiBuilder.Inner_StreamUtil cell, BlockState live) {
      return AiBuilder.update(cell, live);
   }

   static boolean init(AiBuilder.Inner_StreamUtil cell, BlockState live, Map<String, List<String>> substitutes) {
      if (init(cell, live)) {
         return true;
      } else {
         return substitutes != null && !substitutes.isEmpty() ? AiBuilder.init_Z_TableIndex_StreamU_3(cell, live, substitutes) : false;
      }
   }

   static Float load_Float(AiBuilder.Inner_StreamUtil cell) {
      return init_Float(cell);
   }

   static Direction init_Direction(AiBuilder.Inner_StreamUtil cell, BlockPos target) {
      autismclient.ai.engine.PlacementGoal.Inner_DataUtil constraint = init_u_DataUtil(cell, target == null ? BlockPos.ZERO : target);
      return constraint == null ? null : constraint.clickFace();
   }

   static boolean init_Z(AiBuilder.Inner_StreamUtil cell) {
      AiBuildSite.Inner_CachePool kind = init_IoWorker_CachePool(cell);
      return kind == AiBuildSite.Inner_CachePool.init || kind == AiBuildSite.Inner_CachePool.load;
   }

   static boolean init(BlockPos target, BlockGetter world, Map<Long, AiBuilder.Inner_StreamUtil> wanted) {
      if (target != null && world != null && wanted != null) {
         AiBuilder.Inner_StreamUtil cell = wanted.get(target.asLong());
         if (cell == null) {
            return false;
         }

         if (AiBuilder.init_Fluid(cell) != null && !AiBuilder.load_Z(cell)) {
            try {
               return AiSchematicActions.init(target, world, wanted) == null;
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

   static boolean read(AiBuilder.Inner_StreamUtil cell, BlockState live) {
      if (cell != null && live != null) {
         try {
            return AiBuilder.load(cell, live);
         } catch (RuntimeException unknown) {
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean load_Z(AiBuilder.Inner_StreamUtil cell) {
      try {
         return AiBuilder.write(init_Block(cell));
      } catch (RuntimeException unknown) {
         return false;
      }
   }

   static boolean read_Z(AiBuilder.Inner_StreamUtil cell) {
      return cell.block().equals("minecraft:lever") && "true".equals(cell.properties().get("powered"));
   }

   static AiBuilder.Inner_StreamUtil init_TableIndex_StreamU(AiBuilder.Inner_StreamUtil cell) {
      if (!read_Z(cell)) {
         return cell;
      }

      LinkedHashMap<String, String> properties = new LinkedHashMap<>(cell.properties());
      properties.remove("powered");
      return new AiBuilder.Inner_StreamUtil(cell.dx(), cell.dy(), cell.dz(), cell.block(), Collections.unmodifiableMap(properties));
   }

   static AiBuilder.Inner_DataUtil init_TableIndex_DataUti(AiBuilder.Inner_DataUtil blueprint) {
      return AiBuilder.load_TableIndex_DataUti(blueprint);
   }

   static List<AiBuilder.Inner_StreamUtil> init_List(AiBuilder.Inner_DataUtil blueprint) {
      ArrayList<AiBuilder.Inner_StreamUtil> controls = new ArrayList<>();

      for (AiBuilder.Inner_StreamUtil cell : blueprint.cells()) {
         if (read_Z(cell)) {
            controls.add(cell);
         }
      }

      return List.copyOf(controls);
   }

   static autismclient.ai.engine.PlacementGoal.Inner_DataUtil init_u_DataUtil(AiBuilder.Inner_StreamUtil cell, BlockPos target) {
      try {
         return AiBuilder.init(cell, init_Block(cell), target, init_BlockGetter(cell));
      } catch (RuntimeException e) {
         return null;
      }
   }

   private static BlockGetter init_BlockGetter(AiBuilder.Inner_StreamUtil cell) {
      if (!write(cell)) {
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

   private static boolean write(AiBuilder.Inner_StreamUtil cell) {
      String type = cell.properties().get("type");
      if (type != null && !type.equals("single") && cell.block().endsWith("chest")) {
         try {
            return init_Block(cell) instanceof ChestBlock;
         } catch (RuntimeException unknown) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static Block init_Block(AiBuilder.Inner_StreamUtil cell) {
      return AiBuilder.init_BlockState(cell).getBlock();
   }

   private static Direction init_Direction(AiBuilder.Inner_StreamUtil cell) {
      if (!AiSchematicActions.read_Z(cell)) {
         return Direction.DOWN;
      }

      Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
      return facing != null && facing.getAxis().isHorizontal() ? facing.getOpposite() : null;
   }

   private static Direction init(AiBuilder.Inner_StreamUtil cell, Block block) {
      if (AiBuilder.init(block, cell)) {
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

   private static boolean update(AiBuilder.Inner_StreamUtil cell) {
      try {
         return !AiBuilder.init_BlockState(cell).canSurvive(init_LevelReader, BlockPos.ZERO);
      } catch (RuntimeException e) {
         return true;
      }
   }

   private static LevelReader init() {
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

   static List<String> read_List(AiBuilder.Inner_StreamUtil cell) {
      Block block = init_Block(cell);
      ArrayList<String> out = new ArrayList<>(2);

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         String name = entry.getKey();
         if (!AiBuilder.read(block, name)) {
            AiBuilder.Inner_TaskQueue strategy = name.equals("facing")
               ? AiBuilder.init_TableIndex_TaskQue(cell, block)
               : AiBuilder.init_TableIndex_TaskQue(block, name);
            if ((strategy == AiBuilder.Inner_TaskQueue.poll || strategy == AiBuilder.Inner_TaskQueue.push) && !init(block, name, entry.getValue())) {
               out.add(name);
            }
         }
      }

      return List.copyOf(out);
   }

   private static boolean sync(AiBuilder.Inner_StreamUtil cell) {
      return !read_List(cell).isEmpty();
   }

   private static boolean init(Block block, String propertyName, String requested) {
      Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)block.getStateDefinition().getProperty(propertyName);
      return property != null && String.valueOf(block.defaultBlockState().getValue(property)).equalsIgnoreCase(requested);
   }

   private static BlockPos init(Direction direction) {
      return new BlockPos(direction.getStepX(), direction.getStepY(), direction.getStepZ());
   }
}
