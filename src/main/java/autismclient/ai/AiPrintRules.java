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

public final class AiPrintRules implements autismclient.ai.print.PrintRules {
   public static final AiPrintRules init_bi = new AiPrintRules();
   public static final String init_Str = "lit";

   private AiPrintRules() {
   }

   public static List<autismclient.ai.print.PrintTarget> init_List_Path(Path file, BlockPos origin, int rotation, String mirror) {
      AiSchematic.Inner_BufferHelper loaded = AiSchematic.init_bP_BufferHelper(file, origin, rotation, mirror, false, 2097152);
      return init(AiBuilder.init_TableIndex_DataUti(loaded.blueprint()));
   }

   public static AiPrintRules.Inner_DataUtil init(Path file, BlockPos origin, int rotation, String mirror, Provider registries, boolean fixtureContents) {
      AiSchematic.Inner_BufferHelper loaded = AiSchematic.init_bP_BufferHelper(file, origin, rotation, mirror, false, 2097152);
      AiBuilder.Inner_DataUtil normalized = AiBuilder.init_TableIndex_DataUti(loaded.blueprint());
      AiSchematicMachinePlan machine = AiSchematicMachinePlan.init_bV_bP_BufferHelper_TableIndex_DataUti(loaded, loaded.blueprint()).init(normalized);
      List<AiSchematicMachinePlan.Inner_DataUtil> hoppers = machine.load(origin, registries);
      AiSchematicMachinePlan.Inner_StreamUtil actions = machine.init_bV_StreamUtil_BlockPos_HolderLookup_Provi(origin, registries);
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

      return new AiPrintRules.Inner_DataUtil(init(normalized), actions.vehicles(), hoppers, actions.outcomes(), Map.copyOf(contents), actions);
   }

   public static Map<Long, ListTag> init_Map_Path(Path file, BlockPos origin, int rotation, String mirror) {
      AiSchematic.Inner_BufferHelper loaded = AiSchematic.init_bP_BufferHelper(file, origin, rotation, mirror, false, 2097152);
      HashMap<Long, ListTag> out = new HashMap<>();
      BlockPos o = loaded.blueprint().origin();
      loaded.containerContents().forEach((k, v) -> out.put(o.offset(BlockPos.of(k)).asLong(), v));
      return out;
   }

   public static int init(List<autismclient.ai.print.PrintTarget> targets) {
      ArrayList<AiBuilder.Inner_StreamUtil> cells = new ArrayList<>(targets.size());

      for (autismclient.ai.print.PrintTarget t : targets) {
         if (t.cell() instanceof AiBuilder.Inner_StreamUtil cell) {
            cells.add(cell);
         }
      }

      int stacks = 0;

      for (Entry<Item, Integer> entry : AiSchematicPlacement.init_Map_List(cells).entrySet()) {
         int size = Math.max(1, entry.getKey().getDefaultMaxStackSize());
         stacks += (entry.getValue() + size - 1) / size;
      }

      return (stacks + 26) / 27;
   }

   public static List<Item> init_List(autismclient.ai.print.PrintTarget target) {
      return target.cell() instanceof AiBuilder.Inner_StreamUtil cell ? AiSchematicActions.read_List(cell) : List.of();
   }

   public static List<autismclient.ai.print.PrintTarget> init(Map<BlockPos, BlockState> states) {
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
      List<AiBuilder.Inner_StreamUtil> cells = new ArrayList<>();

      for (Entry<BlockPos, BlockState> e : states.entrySet()) {
         BlockState state = e.getValue();
         LinkedHashMap<String, String> props = new LinkedHashMap<>();

         for (Property<?> property : state.getProperties()) {
            props.put(property.getName(), init_Str_BlockState_Property(state, property));
         }

         BlockPos p = e.getKey();
         cells.add(
            new AiBuilder.Inner_StreamUtil(
               p.getX() - minX, p.getY() - minY, p.getZ() - minZ, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(), props
            )
         );
      }

      return init(AiBuilder.init_TableIndex_DataUti(new AiBuilder.Inner_DataUtil(origin, cells)));
   }

   private static <T extends Comparable<T>> String init_Str_BlockState_Property(BlockState state, Property<T> property) {
      return property.getName(state.getValue(property));
   }

   static List<autismclient.ai.print.PrintTarget> init(AiBuilder.Inner_DataUtil blueprint) {
      List<AiBuilder.Inner_StreamUtil> cells = blueprint.cells();
      ArrayList<autismclient.ai.print.PrintTarget> targets = new ArrayList<>(cells.size());

      for (int i = 0; i < cells.size(); i++) {
         AiBuilder.init();
         AiBuilder.Inner_StreamUtil cell = cells.get(i);
         BlockState wanted = AiBuilder.init_BlockState(cell);
         autismclient.ai.print.PrintTarget.Inner_DataUtil kind = init_ListPool_DataUtil(cell);
         Item item = init_Item(cell, kind, wanted);
         targets.add(new autismclient.ai.print.PrintTarget(i, blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()), wanted, kind, item, cell));
      }

      return Collections.unmodifiableList(targets);
   }

   private static autismclient.ai.print.PrintTarget.Inner_DataUtil init_ListPool_DataUtil(AiBuilder.Inner_StreamUtil cell) {
      if (AiBuilder.init_Fluid(cell) != null) {
         return AiBuilder.load_Z(cell) ? autismclient.ai.print.PrintTarget.Inner_DataUtil.FLOW : autismclient.ai.print.PrintTarget.Inner_DataUtil.FLUID;
      }

      if (!AiSchematicActions.read_Z(cell) && !"upper".equals(cell.properties().get("half"))) {
         String id = cell.block();
         if (id.equals("minecraft:farmland") || id.equals("minecraft:dirt_path")) {
            return autismclient.ai.print.PrintTarget.Inner_DataUtil.SOIL;
         } else if (id.startsWith("minecraft:potted_")) {
            return autismclient.ai.print.PrintTarget.Inner_DataUtil.POT;
         } else if (AiSchematicActions.write(cell)) {
            return autismclient.ai.print.PrintTarget.Inner_DataUtil.CAULDRON;
         } else {
            return AiSchematicActions.load_Z(cell) ? autismclient.ai.print.PrintTarget.Inner_DataUtil.COMPOSTER : autismclient.ai.print.PrintTarget.Inner_DataUtil.PLACE;
         }
      } else {
         return autismclient.ai.print.PrintTarget.Inner_DataUtil.PARTNER;
      }
   }

   private static Item init_Item(AiBuilder.Inner_StreamUtil cell, autismclient.ai.print.PrintTarget.Inner_DataUtil kind, BlockState state) {
      Item var10000;
      switch (kind) {
         case PLACE:
            var10000 = state.getBlock().asItem();
            break;
         case PARTNER:
         case FLOW:
            var10000 = Items.AIR;
            break;
         case FLUID:
            var10000 = cell.block().equals("minecraft:lava") ? Items.LAVA_BUCKET : Items.WATER_BUCKET;
            break;
         case SOIL:
            var10000 = Items.DIRT;
            break;
         case POT:
            var10000 = Items.FLOWER_POT;
            break;
         case CAULDRON:
            var10000 = Items.CAULDRON;
            break;
         case COMPOSTER:
            var10000 = Items.COMPOSTER;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static AiBuilder.Inner_StreamUtil init_TableIndex_StreamU(autismclient.ai.print.PrintTarget target) {
      return (AiBuilder.Inner_StreamUtil)target.cell();
   }

   @Override
   public boolean init_Z_ListPool_BlockState(autismclient.ai.print.PrintTarget target, BlockState live) {
      AiBuilder.Inner_StreamUtil cell = init_TableIndex_StreamU(target);
      if (target.kind() != autismclient.ai.print.PrintTarget.Inner_DataUtil.FLUID && target.kind() != autismclient.ai.print.PrintTarget.Inner_DataUtil.FLOW) {
         if ((!cell.block().equals("minecraft:dirt") || !cell.properties().isEmpty()) && !cell.block().equals("minecraft:grass_block")
            || !live.is(Blocks.DIRT) && !live.is(Blocks.GRASS_BLOCK)) {
            return init_Z_BlockState_BlockState(target.wanted(), live) ? false : AiBuilder.write(cell, live);
         } else {
            return true;
         }
      } else {
         Fluid fluid = AiBuilder.init_Fluid(cell);
         FluidState liveFluid = live.getFluidState();
         return fluid != null && !liveFluid.isEmpty() && liveFluid.getType().isSame(fluid)
            ? target.kind() == autismclient.ai.print.PrintTarget.Inner_DataUtil.FLOW || liveFluid.isSource()
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
   public boolean load_Z_ListPool_BlockState(autismclient.ai.print.PrintTarget target, BlockState live) {
      AiBuilder.Inner_StreamUtil cell = init_TableIndex_StreamU(target);
      switch (target.kind()) {
         case SOIL:
            return live.is(Blocks.DIRT) || AiBuilder.write(cell, live);
         case POT:
            return live.getBlock() instanceof FlowerPotBlock;
         case CAULDRON:
            return live.getBlock() instanceof AbstractCauldronBlock;
         case COMPOSTER:
            return live.getBlock() instanceof ComposterBlock;
         default:
            if (!BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString().equals(cell.block())) {
               return false;
            } else {
               Block block = live.getBlock();
               if (block instanceof MultifaceBlock) {
                  return autismclient.ai.print.PrintRules.load_Z_BlockState_BlockState(target.wanted(), live);
               } else {
                  for (Entry<String, String> entry : cell.properties().entrySet()) {
                     String name = entry.getKey();
                     if (!AiBuilder.read(block, name)) {
                        AiBuilder.Inner_TaskQueue strategy = AiBuilder.init_TableIndex_TaskQue(block, name);
                        if (strategy != AiBuilder.Inner_TaskQueue.POST_INTERACT
                           && strategy != AiBuilder.Inner_TaskQueue.FLUID_INTERACT
                           && strategy != AiBuilder.Inner_TaskQueue.POWER_STATE
                           && (!name.equals("type") || !AiBuilder.init_Z(cell, live))) {
                           if (!name.equals("type") || !(block instanceof SlabBlock) || !"double".equals(entry.getValue())) {
                              Property<?> property = block.getStateDefinition().getProperty(name);
                              if (property == null || !AiBuilder.init(block, name, String.valueOf(live.getValue(property)), entry.getValue())) {
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
   public boolean read_Z_ListPool_BlockState(autismclient.ai.print.PrintTarget target, BlockState live) {
      BlockState wanted = target.wanted();
      BooleanProperty open = BlockStateProperties.OPEN;
      return AiBuilder.load(wanted.getBlock())
         && live.is(wanted.getBlock())
         && this.init_Z_ListPool_BlockState(target, live.setValue(open, wanted.getValue(open)));
   }

   @Override
   public autismclient.ai.print.PrintRules.Inner_CachePool init_PathHelper_CachePo_ListPool_BlockState(autismclient.ai.print.PrintTarget target, BlockState live) {
      AiBuilder.Inner_StreamUtil cell = init_TableIndex_StreamU(target);
      Block block = live.getBlock();
      if (init_Z_BlockState_BlockState(target.wanted(), live)) {
         return new autismclient.ai.print.PrintRules.Inner_CachePool(Items.REDSTONE_BLOCK, "lit");
      }

      if (AiBuilder.write(cell, live)) {
         return null;
      }

      if (block instanceof MultifaceBlock) {
         if (!autismclient.ai.print.PrintRules.load_Z_BlockState_BlockState(target.wanted(), live)) {
            return null;
         }

         for (Direction side : Direction.values()) {
            if (MultifaceBlock.hasFace(target.wanted(), side) && !MultifaceBlock.hasFace(live, side)) {
               return new autismclient.ai.print.PrintRules.Inner_CachePool(block.asItem(), side.getName());
            }
         }

         return null;
      } else {
         switch (target.kind()) {
            case SOIL:
               if (!live.is(Blocks.DIRT)) {
                  return null;
               }

               return new autismclient.ai.print.PrintRules.Inner_CachePool(
                  cell.block().equals("minecraft:dirt_path") ? Items.WOODEN_SHOVEL : Items.WOODEN_HOE, "soil"
               );
            case POT:
               if (!live.is(Blocks.FLOWER_POT)) {
                  return null;
               }

               Item plant = AiSchematicActions.init(target.wanted().getBlock());
               return plant == Items.AIR ? null : new autismclient.ai.print.PrintRules.Inner_CachePool(plant, "plant");
            case CAULDRON:
               return !live.is(Blocks.CAULDRON) && !AiSchematicActions.init_Z(cell, live) ? null : new autismclient.ai.print.PrintRules.Inner_CachePool(AiSchematicActions.init_Item(cell), "fill");
            case COMPOSTER:
               int wanted = Integer.parseInt(cell.properties().getOrDefault("level", "0"));
               return live.getValue(ComposterBlock.LEVEL) < wanted ? new autismclient.ai.print.PrintRules.Inner_CachePool(Items.PUMPKIN_PIE, "level") : null;
            default:
               if (block instanceof SlabBlock && "double".equals(cell.properties().get("type")) && live.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
                  return new autismclient.ai.print.PrintRules.Inner_CachePool(block.asItem(), "type");
               } else {
                  for (Entry<String, String> entry : cell.properties().entrySet()) {
                     String name = entry.getKey();
                     if (!AiBuilder.read(block, name) && AiBuilder.init_TableIndex_TaskQue(block, name) == AiBuilder.Inner_TaskQueue.POST_INTERACT) {
                        Property<?> property = block.getStateDefinition().getProperty(name);
                        if (property != null && !AiBuilder.init(block, name, String.valueOf(live.getValue(property)), entry.getValue())) {
                           Item item = AiBuilder.init_Item(block, name);
                           if (item == null && AiBuilder.init_Z(block, name)) {
                              item = block.asItem();
                           }

                           return new autismclient.ai.print.PrintRules.Inner_CachePool(item == null ? Items.AIR : item, name);
                        }
                     }
                  }

                  return null;
               }
         }
      }
   }

   @Override
   public boolean init_Z_ListPool_SignalGetter(autismclient.ai.print.PrintTarget target, SignalGetter world) {
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
      return AiPrinterCircuit.init(world, source);
   }

   @Override
   public Map<BlockPos, autismclient.ai.print.PrintRules.Inner_DataUtil> init_Map(BlockGetter world, BlockPos source, Set<BlockPos> targets) {
      HashMap<BlockPos, autismclient.ai.print.PrintRules.Inner_DataUtil> result = new HashMap<>();
      AiPrinterCircuit.init_Map(world, source, targets).forEach((at, influence) -> result.put(at, autismclient.ai.print.PrintRules.Inner_DataUtil.valueOf(influence.name())));
      return result;
   }

   @Override
   public autismclient.ai.print.PrintRules.Inner_StreamUtil init_PathHelper_StreamU(BlockGetter world, BlockPos source, Set<BlockPos> targets) {
      AiPrinterCircuit.Inner_JsonUtil trace = AiPrinterCircuit.init_bl_JsonUtil(world, source, targets);
      HashMap<BlockPos, autismclient.ai.print.PrintRules.Inner_DataUtil> reached = new HashMap<>();
      trace.reached().forEach((at, result) -> reached.put(at, autismclient.ai.print.PrintRules.Inner_DataUtil.valueOf(result.name())));
      return new autismclient.ai.print.PrintRules.Inner_StreamUtil(reached, autismclient.ai.print.PrintRules.Inner_DataUtil.valueOf(trace.unseen().name()));
   }

   @Override
   public autismclient.ai.print.PrintRules.Inner_StreamUtil init_PathHelper_StreamU_BlockGetter(
      BlockGetter world, BlockPos source, Set<BlockPos> targets, Set<BlockPos> configurationTargets
   ) {
      autismclient.ai.print.PrintRules.Inner_StreamUtil normal = this.init_PathHelper_StreamU(world, source, targets);
      if (configurationTargets.isEmpty()) {
         return normal;
      }

      AiPrinterCircuit.Inner_JsonUtil construction = AiPrinterCircuit.load(world, source, configurationTargets);
      HashMap<BlockPos, autismclient.ai.print.PrintRules.Inner_DataUtil> reached = new HashMap<>(normal.reached());
      construction.reached()
         .forEach(
            (at, effect) -> reached.merge(
               at,
               autismclient.ai.print.PrintRules.Inner_DataUtil.valueOf(effect.name()),
               (before, after) -> (autismclient.ai.print.PrintRules.Inner_DataUtil)(before == autismclient.ai.print.PrintRules.Inner_DataUtil.REACHES ? before : after)
            )
         );
      return new autismclient.ai.print.PrintRules.Inner_StreamUtil(
         Map.copyOf(reached), construction.unseen() == AiPrinterCircuit.Inner_ConfigStore.UNKNOWN ? autismclient.ai.print.PrintRules.Inner_DataUtil.UNKNOWN : normal.unseen()
      );
   }

   @Override
   public boolean write_Z_ListPool_BlockState(autismclient.ai.print.PrintTarget target, BlockState live) {
      AiBuilder.Inner_StreamUtil cell = init_TableIndex_StreamU(target);
      return "true".equals(cell.properties().get("waterlogged"))
         && live.hasProperty(BlockStateProperties.WATERLOGGED)
         && !live.getValue(BlockStateProperties.WATERLOGGED);
   }

   public static boolean init_Z(autismclient.ai.print.PrintTarget target) {
      if (target.kind() == autismclient.ai.print.PrintTarget.Inner_DataUtil.PLACE && target.cell() != null) {
         AiBuilder.Inner_StreamUtil cell = init_TableIndex_StreamU(target);
         Block block = target.wanted().getBlock();
         BlockState defaults = block.defaultBlockState();

         for (Entry<String, String> entry : cell.properties().entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (!AiBuilder.read(block, name)) {
               AiBuilder.Inner_TaskQueue strategy = AiBuilder.init_TableIndex_TaskQue(block, name);
               boolean var10000;
               switch (strategy) {
                  case PLAYER_FACING:
                  case PLAYER_FACING_OPPOSITE:
                  case PLAYER_FACING_CLOCKWISE:
                  case PLAYER_ROTATION:
                     var10000 = true;
                     break;
                  case FACE_AXIS:
                     var10000 = value.equals("y");
                     break;
                  case CLICK_HEIGHT:
                     var10000 = value.equals("bottom");
                     break;
                  case PLAYER_DIRECTION:
                     var10000 = value.equals("down");
                     break;
                  case PLAYER_DIRECTION_OPPOSITE:
                     var10000 = value.equals("up");
                     break;
                  case PLAYER_ORIENTATION:
                     var10000 = value.startsWith("down_");
                     break;
                  case FACE_DERIVED:
                     var10000 = block instanceof HopperBlock ? value.equals("down") : block instanceof ShulkerBoxBlock && value.equals("up");
                     break;
                  case FLUID_INTERACT:
                     var10000 = value.equals("false");
                     break;
                  case POST_INTERACT:
                  case CHEST_PAIR:
                     var10000 = false;
                     break;
                  default:
                     Property<?> property = block.getStateDefinition().getProperty(name);
                     var10000 = property != null && AiBuilder.init(block, name, String.valueOf(defaults.getValue(property)), value);
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

   public static autismclient.ai.engine.PlacementGoal.Inner_DataUtil init_u_DataUtil_ListPool_BlockGetter(autismclient.ai.print.PrintTarget target, BlockGetter world) {
      if (target.cell() != null && target.kind() == autismclient.ai.print.PrintTarget.Inner_DataUtil.PLACE) {
         try {
            return AiBuilder.init(init_TableIndex_StreamU(target), target.wanted().getBlock(), target.pos(), world);
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
      List<autismclient.ai.print.PrintTarget> targets,
      List<autismclient.ai.print.PrintRailVehicle.Inner_StreamUtil> vehicles,
      List<AiSchematicMachinePlan.Inner_DataUtil> hoppers,
      Map<BlockPos, BlockState> outcomes,
      Map<Long, ListTag> fixtureContents,
      AiSchematicMachinePlan.Inner_StreamUtil actions
   ) {
      public Inner_DataUtil(
         List<autismclient.ai.print.PrintTarget> targets,
         List<autismclient.ai.print.PrintRailVehicle.Inner_StreamUtil> vehicles,
         List<AiSchematicMachinePlan.Inner_DataUtil> hoppers,
         Map<BlockPos, BlockState> outcomes,
         Map<Long, ListTag> fixtureContents
      ) {
         this(targets, vehicles, hoppers, outcomes, fixtureContents, new AiSchematicMachinePlan.Inner_StreamUtil(vehicles, hoppers, outcomes));
      }
   }
}
