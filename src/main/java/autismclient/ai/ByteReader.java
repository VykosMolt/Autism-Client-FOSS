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

final class ByteReader {
   private static final List<BlockPos> init_List = List.of(
      new BlockPos(0, -1, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)
   );
   private static final LevelReader init_LevelReader = init();

   private ByteReader() {
   }

   static IoWorker.Inner_CachePool init_IoWorker_CachePool(TableIndex.Inner_StreamUtil cell) {
      if (TableIndex.read_Z(cell)) {
         return IoWorker.Inner_CachePool.sync;
      } else if (TableIndex.update(cell)) {
         return IoWorker.Inner_CachePool.flush;
      } else if (TableIndex.init_Fluid(cell) != null) {
         return IoWorker.Inner_CachePool.write;
      } else if (!init_List(cell).isEmpty()) {
         return IoWorker.Inner_CachePool.flush;
      } else if (bQ.read_Z(cell) || "upper".equals(cell.properties().get("half")) || "head".equals(cell.properties().get("part"))) {
         return IoWorker.Inner_CachePool.update;
      } else if (bQ.init_Z(cell)) {
         return IoWorker.Inner_CachePool.read;
      } else if (write(cell)) {
         return IoWorker.Inner_CachePool.load;
      } else if (read_Z(cell)) {
         return IoWorker.Inner_CachePool.init;
      } else {
         return sync(cell) ? IoWorker.Inner_CachePool.load : IoWorker.Inner_CachePool.init;
      }
   }

   static Item init_Item(TableIndex.Inner_StreamUtil cell) {
      return init_Item(cell, init_IoWorker_CachePool(cell));
   }

   static Item init_Item(TableIndex.Inner_StreamUtil cell, IoWorker.Inner_CachePool kind) {
      if (kind == IoWorker.Inner_CachePool.sync || kind == IoWorker.Inner_CachePool.flush) {
         return Items.AIR;
      }

      if (kind == IoWorker.Inner_CachePool.write) {
         if (TableIndex.load_Z(cell)) {
            return Items.AIR;
         } else {
            return cell.block().equals("minecraft:lava") ? Items.LAVA_BUCKET : Items.WATER_BUCKET;
         }
      } else {
         if (kind == IoWorker.Inner_CachePool.read) {
            for (Item material : bQ.read_List(cell)) {
               if (material != Items.AIR) {
                  return material;
               }
            }
         }

         if (kind == IoWorker.Inner_CachePool.update) {
            return TableIndex.init_I(cell) == 0 ? Items.AIR : TableIndex.init_BlockState(cell).getBlock().asItem();
         } else {
            return TableIndex.init_BlockState(cell).getBlock().asItem();
         }
      }
   }

   static List<String> init_List(TableIndex.Inner_StreamUtil cell) {
      return List.copyOf(TableIndex.init_List(cell));
   }

   static List<BlockPos> load_List(TableIndex.Inner_StreamUtil cell) {
      return init_List(cell, init_IoWorker_CachePool(cell));
   }

   static List<BlockPos> init_List(TableIndex.Inner_StreamUtil cell, IoWorker.Inner_CachePool kind) {
      if (kind == IoWorker.Inner_CachePool.sync || kind == IoWorker.Inner_CachePool.flush) {
         return List.of();
      } else if (kind == IoWorker.Inner_CachePool.write) {
         return TableIndex.load_Z(cell) ? List.of() : init_List;
      } else if (kind == IoWorker.Inner_CachePool.update) {
         Direction provider = init_Direction(cell);
         return provider == null ? List.of() : List.of(init(provider));
      } else if (kind == IoWorker.Inner_CachePool.read) {
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

   static Float init_Float(TableIndex.Inner_StreamUtil cell) {
      IoWorker.Inner_CachePool kind = init_IoWorker_CachePool(cell);
      if (kind != IoWorker.Inner_CachePool.init && kind != IoWorker.Inner_CachePool.load) {
         return null;
      }

      autismclient.ai.a.u.Inner_DataUtil constraint = init_u_DataUtil(cell, BlockPos.ZERO);
      return constraint == null ? null : constraint.clickYaw();
   }

   static double init_D(TableIndex.Inner_StreamUtil cell) {
      autismclient.ai.a.u.Inner_DataUtil constraint = init_u_DataUtil(cell, BlockPos.ZERO);
      return constraint == null ? 35.0 : constraint.yawTolerance();
   }

   static Predicate<Direction> init_Predicate(TableIndex.Inner_StreamUtil cell, BlockPos target) {
      IoWorker.Inner_CachePool kind = init_IoWorker_CachePool(cell);
      if (kind != IoWorker.Inner_CachePool.init && kind != IoWorker.Inner_CachePool.load) {
         return null;
      } else {
         autismclient.ai.a.u.Inner_DataUtil constraint = init_u_DataUtil(cell, target == null ? BlockPos.ZERO : target);
         if (constraint == null) {
            return null;
         } else {
            return constraint.clickFace() == null && constraint.axis() == null && constraint.highHalf() == null && constraint.horizontalFace() == null
               ? null
               : constraint::acceptsFace;
         }
      }
   }

   static Boolean init_Boolean(TableIndex.Inner_StreamUtil cell) {
      IoWorker.Inner_CachePool kind = init_IoWorker_CachePool(cell);
      return kind != IoWorker.Inner_CachePool.init && kind != IoWorker.Inner_CachePool.load ? null : TableIndex.init_Boolean(cell, init_Block(cell));
   }

   static boolean init(TableIndex.Inner_StreamUtil cell, BlockState live) {
      return TableIndex.sync(cell, live);
   }

   static boolean load(TableIndex.Inner_StreamUtil cell, BlockState live) {
      return TableIndex.update(cell, live);
   }

   static boolean init(TableIndex.Inner_StreamUtil cell, BlockState live, Map<String, List<String>> substitutes) {
      if (init(cell, live)) {
         return true;
      } else {
         return substitutes != null && !substitutes.isEmpty() ? TableIndex.init_Z_TableIndex_StreamU_3(cell, live, substitutes) : false;
      }
   }

   static Float load_Float(TableIndex.Inner_StreamUtil cell) {
      return init_Float(cell);
   }

   static Direction init_Direction(TableIndex.Inner_StreamUtil cell, BlockPos target) {
      autismclient.ai.a.u.Inner_DataUtil constraint = init_u_DataUtil(cell, target == null ? BlockPos.ZERO : target);
      return constraint == null ? null : constraint.clickFace();
   }

   static boolean init_Z(TableIndex.Inner_StreamUtil cell) {
      IoWorker.Inner_CachePool kind = init_IoWorker_CachePool(cell);
      return kind == IoWorker.Inner_CachePool.init || kind == IoWorker.Inner_CachePool.load;
   }

   static boolean init(BlockPos target, BlockGetter world, Map<Long, TableIndex.Inner_StreamUtil> wanted) {
      if (target != null && world != null && wanted != null) {
         TableIndex.Inner_StreamUtil cell = wanted.get(target.asLong());
         if (cell == null) {
            return false;
         }

         if (TableIndex.init_Fluid(cell) != null && !TableIndex.load_Z(cell)) {
            try {
               return bQ.init(target, world, wanted) == null;
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

   static boolean read(TableIndex.Inner_StreamUtil cell, BlockState live) {
      if (cell != null && live != null) {
         try {
            return TableIndex.load(cell, live);
         } catch (RuntimeException unknown) {
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean load_Z(TableIndex.Inner_StreamUtil cell) {
      try {
         return TableIndex.write(init_Block(cell));
      } catch (RuntimeException unknown) {
         return false;
      }
   }

   static boolean read_Z(TableIndex.Inner_StreamUtil cell) {
      return cell.block().equals("minecraft:lever") && "true".equals(cell.properties().get("powered"));
   }

   static TableIndex.Inner_StreamUtil init_TableIndex_StreamU(TableIndex.Inner_StreamUtil cell) {
      if (!read_Z(cell)) {
         return cell;
      }

      LinkedHashMap<String, String> properties = new LinkedHashMap<>(cell.properties());
      properties.remove("powered");
      return new TableIndex.Inner_StreamUtil(cell.dx(), cell.dy(), cell.dz(), cell.block(), Collections.unmodifiableMap(properties));
   }

   static TableIndex.Inner_DataUtil init_TableIndex_DataUti(TableIndex.Inner_DataUtil blueprint) {
      return TableIndex.load_TableIndex_DataUti(blueprint);
   }

   static List<TableIndex.Inner_StreamUtil> init_List(TableIndex.Inner_DataUtil blueprint) {
      ArrayList<TableIndex.Inner_StreamUtil> controls = new ArrayList<>();

      for (TableIndex.Inner_StreamUtil cell : blueprint.cells()) {
         if (read_Z(cell)) {
            controls.add(cell);
         }
      }

      return List.copyOf(controls);
   }

   static autismclient.ai.a.u.Inner_DataUtil init_u_DataUtil(TableIndex.Inner_StreamUtil cell, BlockPos target) {
      try {
         return TableIndex.init(cell, init_Block(cell), target, init_BlockGetter(cell));
      } catch (RuntimeException e) {
         return null;
      }
   }

   private static BlockGetter init_BlockGetter(TableIndex.Inner_StreamUtil cell) {
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

   private static boolean write(TableIndex.Inner_StreamUtil cell) {
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

   private static Block init_Block(TableIndex.Inner_StreamUtil cell) {
      return TableIndex.init_BlockState(cell).getBlock();
   }

   private static Direction init_Direction(TableIndex.Inner_StreamUtil cell) {
      if (!bQ.read_Z(cell)) {
         return Direction.DOWN;
      }

      Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
      return facing != null && facing.getAxis().isHorizontal() ? facing.getOpposite() : null;
   }

   private static Direction init(TableIndex.Inner_StreamUtil cell, Block block) {
      if (TableIndex.init(block, cell)) {
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

   private static boolean update(TableIndex.Inner_StreamUtil cell) {
      try {
         return !TableIndex.init_BlockState(cell).canSurvive(init_LevelReader, BlockPos.ZERO);
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

   static List<String> read_List(TableIndex.Inner_StreamUtil cell) {
      Block block = init_Block(cell);
      ArrayList<String> out = new ArrayList<>(2);

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         String name = entry.getKey();
         if (!TableIndex.read(block, name)) {
            TableIndex.Inner_TaskQueue strategy = name.equals("facing")
               ? TableIndex.init_TableIndex_TaskQue(cell, block)
               : TableIndex.init_TableIndex_TaskQue(block, name);
            if ((strategy == TableIndex.Inner_TaskQueue.poll || strategy == TableIndex.Inner_TaskQueue.push) && !init(block, name, entry.getValue())) {
               out.add(name);
            }
         }
      }

      return List.copyOf(out);
   }

   private static boolean sync(TableIndex.Inner_StreamUtil cell) {
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
