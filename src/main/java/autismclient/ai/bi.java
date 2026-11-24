package autismclient.ai;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public final class bi implements autismclient.ai.b.PathHelper {
   public static final bi init_bi = new bi();
   public static final String init_Str = "lit";

   private bi() {
   }

   public static List<autismclient.ai.b.ListPool> init_List_Path(Path file, BlockPos origin, int rotation, String mirror) {
      bP.Inner_BufferHelper loaded = bP.init_bP_BufferHelper(file, origin, rotation, mirror, false, 2097152);
      return init(TableIndex.init_TableIndex_DataUti(loaded.blueprint()));
   }

   public static bi.Inner_DataUtil init(Path file, BlockPos origin, int rotation, String mirror, Provider registries, boolean fixtureContents) {
      bP.Inner_BufferHelper loaded = bP.init_bP_BufferHelper(file, origin, rotation, mirror, false, 2097152);
      TableIndex.Inner_DataUtil normalized = TableIndex.init_TableIndex_DataUti(loaded.blueprint());
      bV machine = bV.init_bV_bP_BufferHelper_TableIndex_DataUti(loaded, loaded.blueprint()).init(normalized);
      List<bV.Inner_DataUtil> hoppers = machine.load(origin, registries);
      bV.Inner_StreamUtil actions = machine.init_bV_StreamUtil_BlockPos_HolderLookup_Provi(origin, registries);
      HashMap<Long, ListTag> contents = new HashMap<>();
      if (fixtureContents) {
         Set<Long> nativeContainers = actions.containers().stream().map(value -> value.position().asLong()).collect(Collectors.toSet());
         actions.clocks().forEach(clock -> clock.hoppers().forEach(position -> nativeContainers.add(position.asLong())));
         actions.unavailable().forEach(job -> nativeContainers.add(job.position().asLong()));
         loaded.containerContents().forEach((key, items) -> {
            long world = origin.offset(BlockPos.of(key)).asLong();
            if (!nativeContainers.contains(world)) {
               contents.put(world, items.copy());
            }
         });
      }

      return new bi.Inner_DataUtil(init(normalized), actions.vehicles(), hoppers, actions.outcomes(), Map.copyOf(contents), actions);
   }

   public static Map<Long, ListTag> init_Map_Path(Path file, BlockPos origin, int rotation, String mirror) {
      bP.Inner_BufferHelper loaded = bP.init_bP_BufferHelper(file, origin, rotation, mirror, false, 2097152);
      HashMap<Long, ListTag> out = new HashMap<>();
      BlockPos o = loaded.blueprint().origin();
      loaded.containerContents().forEach((k, v) -> out.put(o.offset(BlockPos.of(k)).asLong(), v));
      return out;
   }

   public static int init(List<autismclient.ai.b.ListPool> targets) {
      ArrayList<TableIndex.Inner_StreamUtil> cells = new ArrayList<>(targets.size());

      for (autismclient.ai.b.ListPool t : targets) {
         if (t.cell() instanceof TableIndex.Inner_StreamUtil cell) {
            cells.add(cell);
         }
      }

      int stacks = 0;

      for (Entry<Item, Integer> entry : bW.init_Map_List(cells).entrySet()) {
         int size = Math.max(1, entry.getKey().getDefaultMaxStackSize());
         stacks += (entry.getValue() + size - 1) / size;
      }

      return (stacks + 26) / 27;
   }

   public static List<Item> init_List(autismclient.ai.b.ListPool target) {
      return target.cell() instanceof TableIndex.Inner_StreamUtil cell ? bQ.read_List(cell) : List.of();
   }

   public static List<autismclient.ai.b.ListPool> init(Map<BlockPos, BlockState> states) {
      if (states.isEmpty()) {
         return List.of();
      }

      int minX = 2147483647;
      int minY = 2147483647;
      int minZ = 2147483647;

      for (BlockPos p : states.keySet()) {
         minX = Math.min(minX, p.getX());
         minY = Math.min(minY, p.getY());
         minZ = Math.min(minZ, p.getZ());
      }

      BlockPos origin = new BlockPos(minX, minY, minZ);
      List<TableIndex.Inner_StreamUtil> cells = new ArrayList<>();

      for (Entry<BlockPos, BlockState> e : states.entrySet()) {
         BlockState state = e.getValue();
         LinkedHashMap<String, String> props = new LinkedHashMap<>();

         for (Property<?> property : state.getProperties()) {
            props.put(property.getName(), init_Str_BlockState_Property(state, property));
         }

         BlockPos p = e.getKey();
         cells.add(
            new TableIndex.Inner_StreamUtil(
               p.getX() - minX, p.getY() - minY, p.getZ() - minZ, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(), props
            )
         );
      }

      return init(TableIndex.init_TableIndex_DataUti(new TableIndex.Inner_DataUtil(origin, cells)));
   }

   private static <T extends Comparable<T>> String init_Str_BlockState_Property(BlockState state, Property<T> property) {
      return property.getName(state.getValue(property));
   }

   static List<autismclient.ai.b.ListPool> init(TableIndex.Inner_DataUtil blueprint) {
      List<TableIndex.Inner_StreamUtil> cells = blueprint.cells();
      ArrayList<autismclient.ai.b.ListPool> targets = new ArrayList<>(cells.size());

      for (int i = 0; i < cells.size(); i++) {
         TableIndex.init();
         TableIndex.Inner_StreamUtil cell = cells.get(i);
         BlockState wanted = TableIndex.init_BlockState(cell);
         autismclient.ai.b.ListPool.Inner_DataUtil kind = init_ListPool_DataUtil(cell);
         Item item = init_Item(cell, kind, wanted);
         targets.add(new autismclient.ai.b.ListPool(i, blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()), wanted, kind, item, cell));
      }

      return Collections.unmodifiableList(targets);
   }

   private static autismclient.ai.b.ListPool.Inner_DataUtil init_ListPool_DataUtil(TableIndex.Inner_StreamUtil cell) {
      if (TableIndex.init_Fluid(cell) != null) {
         return TableIndex.load_Z(cell) ? autismclient.ai.b.ListPool.Inner_DataUtil.write : autismclient.ai.b.ListPool.Inner_DataUtil.read;
      }

      if (!bQ.read_Z(cell) && !"upper".equals(cell.properties().get("half"))) {
         String id = cell.block();
         if (id.equals("minecraft:farmland") || id.equals("minecraft:dirt_path")) {
            return autismclient.ai.b.ListPool.Inner_DataUtil.update;
         } else if (id.startsWith("minecraft:potted_")) {
            return autismclient.ai.b.ListPool.Inner_DataUtil.sync;
         } else if (bQ.write(cell)) {
            return autismclient.ai.b.ListPool.Inner_DataUtil.flush;
         } else {
            return bQ.load_Z(cell) ? autismclient.ai.b.ListPool.Inner_DataUtil.close : autismclient.ai.b.ListPool.Inner_DataUtil.init;
         }
      } else {
         return autismclient.ai.b.ListPool.Inner_DataUtil.load;
      }
   }

   private static Item init_Item(TableIndex.Inner_StreamUtil cell, autismclient.ai.b.ListPool.Inner_DataUtil kind, BlockState state) {
      Item var10000;
      switch (kind) {
         case init:
            var10000 = state.getBlock().asItem();
            break;
         case load:
         case write:
            var10000 = Items.AIR;
            break;
         case read:
            var10000 = cell.block().equals("minecraft:lava") ? Items.LAVA_BUCKET : Items.WATER_BUCKET;
            break;
         case update:
            var10000 = Items.DIRT;
            break;
         case sync:
            var10000 = Items.FLOWER_POT;
            break;
         case flush:
            var10000 = Items.CAULDRON;
            break;
         case close:
            var10000 = Items.COMPOSTER;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static TableIndex.Inner_StreamUtil init_TableIndex_StreamU(autismclient.ai.b.ListPool target) {
      return (TableIndex.Inner_StreamUtil)target.cell();
   }

   @Override
   public boolean init_Z_ListPool_BlockState(autismclient.ai.b.ListPool target, BlockState live) {
      TableIndex.Inner_StreamUtil cell = init_TableIndex_StreamU(target);
      if (target.kind() != autismclient.ai.b.ListPool.Inner_DataUtil.read && target.kind() != autismclient.ai.b.ListPool.Inner_DataUtil.write) {
         if ((!cell.block().equals("minecraft:dirt") || !cell.properties().isEmpty()) && !cell.block().equals("minecraft:grass_block")
            || !live.is(Blocks.DIRT) && !live.is(Blocks.GRASS_BLOCK)) {
            return init_Z_BlockState_BlockState(target.wanted(), live) ? false : TableIndex.write(cell, live);
         } else {
            return true;
         }
      } else {
         Fluid fluid = TableIndex.init_Fluid(cell);
         FluidState liveFluid = live.getFluidState();
         return fluid != null && !liveFluid.isEmpty() && liveFluid.getType().isSame(fluid)
            ? target.kind() == autismclient.ai.b.ListPool.Inner_DataUtil.write || liveFluid.isSource()
            : false;
      }
   }

   public static boolean init_Z_BlockState_BlockState(BlockState wanted, BlockState live) {
      return wanted.getBlock() instanceof CopperBulbBlock
         && wanted.getValue(CopperBulbBlock.LIT)
         && !wanted.getValue(CopperBulbBlock.POWERED)
         && live.getBlock() == wanted.getBlock()
         && !live.getValue(CopperBulbBlock.LIT);
   }

   @Override
   public boolean load_Z_ListPool_BlockState(autismclient.ai.b.ListPool target, BlockState live) {
      TableIndex.Inner_StreamUtil cell = init_TableIndex_StreamU(target);
      switch (target.kind()) {
         case update:
            return live.is(Blocks.DIRT) || TableIndex.write(cell, live);
         case sync:
            return live.getBlock() instanceof FlowerPotBlock;
         case flush:
            return live.getBlock() instanceof AbstractCauldronBlock;
         case close:
            return live.getBlock() instanceof ComposterBlock;
         default:
            if (!BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString().equals(cell.block())) {
               return false;
            } else {
               Block block = live.getBlock();
               if (block instanceof MultifaceBlock) {
                  return autismclient.ai.b.PathHelper.load_Z_BlockState_BlockState(target.wanted(), live);
               } else {
                  for (Entry<String, String> entry : cell.properties().entrySet()) {
                     String name = entry.getKey();
                     if (!TableIndex.read(block, name)) {
                        TableIndex.Inner_TaskQueue strategy = TableIndex.init_TableIndex_TaskQue(block, name);
                        if (strategy != TableIndex.Inner_TaskQueue.poll
                           && strategy != TableIndex.Inner_TaskQueue.push
                           && strategy != TableIndex.Inner_TaskQueue.pop
                           && (!name.equals("type") || !TableIndex.init_Z(cell, live))) {
                           if (!name.equals("type") || !(block instanceof SlabBlock) || !"double".equals(entry.getValue())) {
                              Property<?> property = block.getStateDefinition().getProperty(name);
                              if (property == null || !TableIndex.init(block, name, String.valueOf(live.getValue(property)), entry.getValue())) {
                                 return false;
                              }
                           } else if (live.getValue(SlabBlock.TYPE) == SlabType.TOP) {
                              return false;
                           }
                        }
                     }
                  }

                  return true;
               }
            }
      }
   }

   @Override
   public boolean read_Z_ListPool_BlockState(autismclient.ai.b.ListPool target, BlockState live) {
      BlockState wanted = target.wanted();
      BooleanProperty open = BlockStateProperties.OPEN;
      return TableIndex.load(wanted.getBlock())
         && live.is(wanted.getBlock())
         && this.init_Z_ListPool_BlockState(target, live.setValue(open, wanted.getValue(open)));
   }

   @Override
   public autismclient.ai.b.PathHelper.Inner_CachePool init_PathHelper_CachePo_ListPool_BlockState(autismclient.ai.b.ListPool target, BlockState live) {
      TableIndex.Inner_StreamUtil cell = init_TableIndex_StreamU(target);
      Block block = live.getBlock();
      if (init_Z_BlockState_BlockState(target.wanted(), live)) {
         return new autismclient.ai.b.PathHelper.Inner_CachePool(Items.REDSTONE_BLOCK, "lit");
      }

      if (TableIndex.write(cell, live)) {
         return null;
      }

      if (block instanceof MultifaceBlock) {
         if (!autismclient.ai.b.PathHelper.load_Z_BlockState_BlockState(target.wanted(), live)) {
            return null;
         }

         for (Direction side : Direction.values()) {
            if (MultifaceBlock.hasFace(target.wanted(), side) && !MultifaceBlock.hasFace(live, side)) {
               return new autismclient.ai.b.PathHelper.Inner_CachePool(block.asItem(), side.getName());
            }
         }

         return null;
      } else {
         switch (target.kind()) {
            case update:
               if (!live.is(Blocks.DIRT)) {
                  return null;
               }

               return new autismclient.ai.b.PathHelper.Inner_CachePool(
                  cell.block().equals("minecraft:dirt_path") ? Items.WOODEN_SHOVEL : Items.WOODEN_HOE, "soil"
               );
            case sync:
               if (!live.is(Blocks.FLOWER_POT)) {
                  return null;
               }

               Item plant = bQ.init(target.wanted().getBlock());
               return plant == Items.AIR ? null : new autismclient.ai.b.PathHelper.Inner_CachePool(plant, "plant");
            case flush:
               return !live.is(Blocks.CAULDRON) && !bQ.init_Z(cell, live) ? null : new autismclient.ai.b.PathHelper.Inner_CachePool(bQ.init_Item(cell), "fill");
            case close:
               int wanted = Integer.parseInt(cell.properties().getOrDefault("level", "0"));
               return live.getValue(ComposterBlock.LEVEL) < wanted ? new autismclient.ai.b.PathHelper.Inner_CachePool(Items.PUMPKIN_PIE, "level") : null;
            default:
               if (block instanceof SlabBlock && "double".equals(cell.properties().get("type")) && live.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
                  return new autismclient.ai.b.PathHelper.Inner_CachePool(block.asItem(), "type");
               } else {
                  for (Entry<String, String> entry : cell.properties().entrySet()) {
                     String name = entry.getKey();
                     if (!TableIndex.read(block, name) && TableIndex.init_TableIndex_TaskQue(block, name) == TableIndex.Inner_TaskQueue.poll) {
                        Property<?> property = block.getStateDefinition().getProperty(name);
                        if (property != null && !TableIndex.init(block, name, String.valueOf(live.getValue(property)), entry.getValue())) {
                           Item item = TableIndex.init_Item(block, name);
                           if (item == null && TableIndex.init_Z(block, name)) {
                              item = block.asItem();
                           }

                           return new autismclient.ai.b.PathHelper.Inner_CachePool(item == null ? Items.AIR : item, name);
                        }
                     }
                  }

                  return null;
               }
         }
      }
   }

   @Override
   public boolean init_Z_ListPool_SignalGetter(autismclient.ai.b.ListPool target, SignalGetter world) {
      BlockState state = target.wanted();
      Block block = state.getBlock();
      BlockPos at = target.pos();
      if (block instanceof NoteBlock
         && state.getValue(BlockStateProperties.NOTE) != 0
         && !world.getBlockState(at).is(Blocks.NOTE_BLOCK)
         && world.hasNeighborSignal(at)) {
         return false;
      }

      if (block instanceof PistonBaseBlock && !state.getValue(PistonBaseBlock.EXTENDED)) {
         Direction facing = state.getValue(PistonBaseBlock.FACING);

         for (Direction side : Direction.values()) {
            if (side != facing && world.hasSignal(at.relative(side), side)) {
               return false;
            }
         }

         if (world.hasSignal(at, Direction.DOWN)) {
            return false;
         }

         BlockPos above = at.above();

         for (Direction side : Direction.values()) {
            if (side != Direction.DOWN && world.hasSignal(above.relative(side), side)) {
               return false;
            }
         }
      }

      return block instanceof TntBlock && world.hasNeighborSignal(at)
         ? false
         : !(block instanceof DispenserBlock) && !(block instanceof CrafterBlock)
            || state.getValue(BlockStateProperties.TRIGGERED)
            || !world.hasNeighborSignal(at) && (!(block instanceof DispenserBlock) || !world.hasNeighborSignal(at.above()));
   }

   @Override
   public Set<BlockPos> init_Set_BlockGetter_BlockPos(BlockGetter world, BlockPos source) {
      return bl.init(world, source);
   }

   @Override
   public Map<BlockPos, autismclient.ai.b.PathHelper.Inner_DataUtil> init_Map(BlockGetter world, BlockPos source, Set<BlockPos> targets) {
      HashMap<BlockPos, autismclient.ai.b.PathHelper.Inner_DataUtil> result = new HashMap<>();
      bl.init_Map(world, source, targets).forEach((at, influence) -> result.put(at, autismclient.ai.b.PathHelper.Inner_DataUtil.valueOf(influence.name())));
      return result;
   }

   @Override
   public autismclient.ai.b.PathHelper.Inner_StreamUtil init_PathHelper_StreamU(BlockGetter world, BlockPos source, Set<BlockPos> targets) {
      bl.Inner_JsonUtil trace = bl.init_bl_JsonUtil(world, source, targets);
      HashMap<BlockPos, autismclient.ai.b.PathHelper.Inner_DataUtil> reached = new HashMap<>();
      trace.reached().forEach((at, result) -> reached.put(at, autismclient.ai.b.PathHelper.Inner_DataUtil.valueOf(result.name())));
      return new autismclient.ai.b.PathHelper.Inner_StreamUtil(reached, autismclient.ai.b.PathHelper.Inner_DataUtil.valueOf(trace.unseen().name()));
   }

   @Override
   public autismclient.ai.b.PathHelper.Inner_StreamUtil init_PathHelper_StreamU_BlockGetter(
      BlockGetter world, BlockPos source, Set<BlockPos> targets, Set<BlockPos> configurationTargets
   ) {
      autismclient.ai.b.PathHelper.Inner_StreamUtil normal = this.init_PathHelper_StreamU(world, source, targets);
      if (configurationTargets.isEmpty()) {
         return normal;
      }

      bl.Inner_JsonUtil construction = bl.load(world, source, configurationTargets);
      HashMap<BlockPos, autismclient.ai.b.PathHelper.Inner_DataUtil> reached = new HashMap<>(normal.reached());
      construction.reached()
         .forEach(
            (at, effect) -> reached.merge(
               at,
               autismclient.ai.b.PathHelper.Inner_DataUtil.valueOf(effect.name()),
               (before, after) -> (autismclient.ai.b.PathHelper.Inner_DataUtil)(before == autismclient.ai.b.PathHelper.Inner_DataUtil.init ? before : after)
            )
         );
      return new autismclient.ai.b.PathHelper.Inner_StreamUtil(
         Map.copyOf(reached), construction.unseen() == bl.Inner_ConfigStore.read ? autismclient.ai.b.PathHelper.Inner_DataUtil.read : normal.unseen()
      );
   }

   @Override
   public boolean write_Z_ListPool_BlockState(autismclient.ai.b.ListPool target, BlockState live) {
      TableIndex.Inner_StreamUtil cell = init_TableIndex_StreamU(target);
      return "true".equals(cell.properties().get("waterlogged"))
         && live.hasProperty(BlockStateProperties.WATERLOGGED)
         && !live.getValue(BlockStateProperties.WATERLOGGED);
   }

   public static boolean init_Z(autismclient.ai.b.ListPool target) {
      if (target.kind() == autismclient.ai.b.ListPool.Inner_DataUtil.init && target.cell() != null) {
         TableIndex.Inner_StreamUtil cell = init_TableIndex_StreamU(target);
         Block block = target.wanted().getBlock();
         BlockState defaults = block.defaultBlockState();

         for (Entry<String, String> entry : cell.properties().entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (!TableIndex.read(block, name)) {
               TableIndex.Inner_TaskQueue strategy = TableIndex.init_TableIndex_TaskQue(block, name);
               boolean var10000;
               switch (strategy) {
                  case update:
                  case sync:
                  case flush:
                  case tick:
                     var10000 = true;
                     break;
                  case init:
                     var10000 = value.equals("y");
                     break;
                  case load:
                     var10000 = value.equals("bottom");
                     break;
                  case close:
                     var10000 = value.equals("down");
                     break;
                  case open:
                     var10000 = value.equals("up");
                     break;
                  case run:
                     var10000 = value.startsWith("down_");
                     break;
                  case write:
                     var10000 = block instanceof HopperBlock ? value.equals("down") : block instanceof ShulkerBoxBlock && value.equals("up");
                     break;
                  case push:
                     var10000 = value.equals("false");
                     break;
                  case poll:
                  case get:
                     var10000 = false;
                     break;
                  default:
                     Property<?> property = block.getStateDefinition().getProperty(name);
                     var10000 = property != null && TableIndex.init(block, name, String.valueOf(defaults.getValue(property)), value);
               }

               boolean ok = var10000;
               if (!ok) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static autismclient.ai.a.u.Inner_DataUtil init_u_DataUtil_ListPool_BlockGetter(autismclient.ai.b.ListPool target, BlockGetter world) {
      if (target.cell() != null && target.kind() == autismclient.ai.b.ListPool.Inner_DataUtil.init) {
         try {
            return TableIndex.init(init_TableIndex_StreamU(target), target.wanted().getBlock(), target.pos(), world);
         } catch (RuntimeException e) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean init(Block block) {
      return block instanceof PistonBaseBlock
         || block instanceof DispenserBlock
         || block instanceof CrafterBlock
         || block instanceof TntBlock
         || block instanceof DoorBlock
         || block instanceof TrapDoorBlock
         || block instanceof FenceGateBlock;
   }

   public static boolean init(BlockState state) {
      Block b = state.getBlock();
      return b instanceof PistonBaseBlock
         || b instanceof DispenserBlock
         || b instanceof CrafterBlock
         || b instanceof TntBlock
         || b instanceof ObserverBlock
         || b instanceof RedstoneWireBlock
         || b instanceof DiodeBlock
         || b instanceof DoorBlock
         || b instanceof TrapDoorBlock
         || b instanceof FenceGateBlock
         || b instanceof NoteBlock
         || b instanceof BellBlock
         || b instanceof CommandBlock;
   }

   public record Inner_DataUtil(
      List<autismclient.ai.b.ListPool> targets,
      List<autismclient.ai.b.MethodUtil.Inner_StreamUtil> vehicles,
      List<bV.Inner_DataUtil> hoppers,
      Map<BlockPos, BlockState> outcomes,
      Map<Long, ListTag> fixtureContents,
      bV.Inner_StreamUtil actions
   ) {
      public Inner_DataUtil(
         List<autismclient.ai.b.ListPool> targets,
         List<autismclient.ai.b.MethodUtil.Inner_StreamUtil> vehicles,
         List<bV.Inner_DataUtil> hoppers,
         Map<BlockPos, BlockState> outcomes,
         Map<Long, ListTag> fixtureContents
      ) {
         this(targets, vehicles, hoppers, outcomes, fixtureContents, new bV.Inner_StreamUtil(vehicles, hoppers, outcomes));
      }
   }
}
