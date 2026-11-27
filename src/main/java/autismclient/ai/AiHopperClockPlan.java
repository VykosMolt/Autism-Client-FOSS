package autismclient.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.FluidState;

final class AiHopperClockPlan {
   private AiHopperClockPlan() {
   }

   static List<autismclient.ai.print.PrintClockSpec> init(Map<BlockPos, BlockState> states, List<AiSchematicMachinePlan.ContainerPlan> contents) {
      return init(states, contents, at -> true);
   }

   static List<autismclient.ai.print.PrintClockSpec> init(Map<BlockPos, BlockState> states, List<AiSchematicMachinePlan.ContainerPlan> contents, Predicate<BlockPos> covered) {
      HashMap<BlockPos, AiSchematicMachinePlan.ContainerPlan> byPosition = new HashMap<>();
      contents.forEach(c -> byPosition.put(c.position(), c));
      ArrayList<autismclient.ai.print.PrintClockSpec> found = new ArrayList<>();
      HashSet<BlockPos> used = new HashSet<>();

      for (AiSchematicMachinePlan.ContainerPlan entry : contents) {
         AiBuilder.checkNotCancelled();
         BlockPos a = entry.position();
         BlockState source = states.get(a);
         if (!used.contains(a) && source != null && source.is(Blocks.HOPPER) && !source.getValue(BlockStateProperties.ENABLED)) {
            Direction direction = source.getValue(BlockStateProperties.FACING_HOPPER);
            if (direction.getAxis().isHorizontal()) {
               BlockPos b = a.relative(direction);
               BlockState second = states.get(b);
               AiSchematicMachinePlan.ContainerPlan saved = byPosition.get(b);
               if (saved != null
                  && !used.contains(b)
                  && second != null
                  && second.is(Blocks.HOPPER)
                  && second.getValue(BlockStateProperties.FACING_HOPPER) == direction.getOpposite()) {
                  ArrayList<ItemStack> stacks = new ArrayList<>();
                  stacks.addAll(entry.slots().values());
                  stacks.addAll(saved.slots().values());
                  if (!stacks.isEmpty()) {
                     ItemStack material = stacks.getFirst();
                     int total = stacks.stream().mapToInt(ItemStack::getCount).sum();
                     if (total > 0
                        && total <= 5 * material.getMaxStackSize()
                        && !stacks.stream().anyMatch(s -> !ItemStack.isSameItemSameComponents(material, s))) {
                        for (Direction side : List.of(direction.getClockWise(), direction.getCounterClockWise())) {
                           autismclient.ai.print.PrintClockSpec spec = init(states, a, b, direction, side, material, total, stacks.size());
                           if (spec != null
                              && spec.source().keySet().stream().allMatch(covered)
                              && found.stream()
                                 .noneMatch(
                                    existing -> existing.dynamicCells().stream().anyMatch(spec.source()::containsKey)
                                       || spec.dynamicCells().stream().anyMatch(existing.source()::containsKey)
                                 )) {
                              found.add(spec);
                              used.add(a);
                              used.add(b);
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return List.copyOf(found);
   }

   private static autismclient.ai.print.PrintClockSpec init(
      Map<BlockPos, BlockState> states, BlockPos a, BlockPos b, Direction dir, Direction side, ItemStack material, int total, int slots
   ) {
      AiHopperClockPlan.Inner_DataUtil world = new AiHopperClockPlan.Inner_DataUtil(states);
      BlockPos ca = a.relative(dir.getOpposite());
      BlockPos cb = b.relative(dir);
      BlockPos first = a.relative(side);
      BlockPos second = b.relative(side);
      BlockPos pa = first.relative(dir.getOpposite());
      BlockPos pb = second.relative(dir);
      BlockPos mount = b.relative(dir, 2);
      BlockPos lever = mount.above();
      if (init(world.getBlockState(ca), dir)
         && init(world.getBlockState(cb), dir.getOpposite())
         && load(world.getBlockState(pa), dir)
         && load(world.getBlockState(pb), dir.getOpposite())
         && world.getBlockState(first).is(Blocks.REDSTONE_BLOCK)
         && world.getBlockState(second).is(Blocks.PISTON_HEAD)
         && world.getBlockState(second).getValue(BlockStateProperties.FACING) == dir.getOpposite()
         && world.getBlockState(second).getValue(PistonHeadBlock.TYPE) == PistonType.STICKY
         && !world.getBlockState(second).getValue(PistonHeadBlock.SHORT)
         && init(world.getBlockState(a.above()))
         && init(world.getBlockState(b.above()))) {
         for (BlockPos hopper : List.of(a, b)) {
            for (Direction face : Direction.values()) {
               BlockPos neighbor = hopper.relative(face);
               if (!neighbor.equals(a) && !neighbor.equals(b)) {
                  BlockState state = world.getBlockState(neighbor);
                  if (state.is(Blocks.HOPPER) && neighbor.relative(state.getValue(BlockStateProperties.FACING_HOPPER)).equals(hopper)) {
                     return null;
                  }

                  if (state.getBlock() instanceof DispenserBlock || state.getBlock() instanceof CrafterBlock) {
                     return null;
                  }
               }
            }
         }

         BlockState control = world.getBlockState(lever);
         if (control.is(Blocks.LEVER)
            && control.getValue(LeverBlock.FACE) == AttachFace.FLOOR
            && !control.getValue(LeverBlock.POWERED)
            && world.getBlockState(mount).isRedstoneConductor(world, mount)
            && world.getBlockState(mount.relative(side)).is(Blocks.REDSTONE_WIRE)) {
            AiPrinterCircuit.Inner_JsonUtil controlTrace = AiPrinterCircuit.trace(world, lever, states.keySet());
            if (controlTrace.unseen() == AiPrinterCircuit.Inner_ConfigStore.NO_PATH && controlTrace.reached().get(pb) == AiPrinterCircuit.Inner_ConfigStore.REACHES) {
               HashSet<BlockPos> outputs = new HashSet<>();
               ArrayDeque<BlockPos> sources = new ArrayDeque<>();
               HashSet<BlockPos> visited = new HashSet<>();
               sources.add(lever);
               sources.add(ca);
               sources.add(cb);

               while (!sources.isEmpty()) {
                  if (visited.size() >= 16 || world.load.size() > 2048) {
                     return null;
                  }

                  BlockPos from = sources.remove();
                  if (visited.add(from)) {
                     if (!from.equals(lever)) {
                        outputs.add(from);
                     }

                     AiPrinterCircuit.Inner_JsonUtil trace = AiPrinterCircuit.trace(world, from, states.keySet());
                     if (trace.unseen() != AiPrinterCircuit.Inner_ConfigStore.NO_PATH) {
                        return null;
                     }

                     for (BlockPos position : trace.reached().keySet()) {
                        BlockState state = world.getBlockState(position);
                        Block block = state.getBlock();
                        if (block instanceof PistonBaseBlock && !position.equals(pa) && !position.equals(pb)
                           || block instanceof DispenserBlock
                           || block instanceof CrafterBlock
                           || block instanceof TntBlock) {
                           return null;
                        }

                        if (block instanceof TrapDoorBlock
                           || block instanceof DoorBlock
                           || block instanceof RedstoneWireBlock
                           || block instanceof DiodeBlock
                           || block instanceof ObserverBlock) {
                           outputs.add(position);
                        }

                        for (Direction face : Direction.values()) {
                           BlockPos watcher = position.relative(face);
                           BlockState watched = world.getBlockState(watcher);
                           if (watched.is(Blocks.OBSERVER) && watcher.relative(watched.getValue(ObserverBlock.FACING)).equals(position)) {
                              sources.add(watcher);
                           }
                        }
                     }
                  }
               }

               for (BlockPos at : List.of(a, b, ca, cb, pa, pb, first, second, mount, lever, a.above(), b.above())) {
                  world.getBlockState(at);

                  for (Direction face : Direction.values()) {
                     world.getBlockState(at.relative(face));
                  }
               }

               return world.load.size() > 2048
                  ? null
                  : new autismclient.ai.print.PrintClockSpec(a, b, pa, pb, first, second, lever, dir, material, total, slots, world.load, outputs);
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static boolean init(BlockState s) {
      return s.is(Blocks.COMPOSTER) && s.getValue(ComposterBlock.LEVEL) == 0;
   }

   private static boolean init(BlockState s, Direction d) {
      return s.is(Blocks.COMPARATOR) && s.getValue(ComparatorBlock.FACING) == d && s.getValue(ComparatorBlock.MODE) == ComparatorMode.COMPARE;
   }

   private static boolean load(BlockState s, Direction d) {
      return s.is(Blocks.STICKY_PISTON) && s.getValue(PistonBaseBlock.FACING) == d;
   }

   private static final class Inner_DataUtil implements BlockGetter {
      final Map<BlockPos, BlockState> init;
      final Map<BlockPos, BlockState> load = new HashMap<>();

      Inner_DataUtil(Map<BlockPos, BlockState> source) {
         this.init = source;
      }

      @Override
      public BlockState getBlockState(BlockPos p) {
         return this.load.computeIfAbsent(p.immutable(), at -> this.init.getOrDefault(at, Blocks.AIR.defaultBlockState()));
      }

      @Override
      public FluidState getFluidState(BlockPos p) {
         return this.getBlockState(p).getFluidState();
      }

      @Override
      public BlockEntity getBlockEntity(BlockPos p) {
         return null;
      }

      @Override
      public int getMinY() {
         return -4096;
      }

      @Override
      public int getHeight() {
         return 8192;
      }
   }
}
