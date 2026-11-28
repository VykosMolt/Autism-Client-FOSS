package autismclient.ai;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;

final class AiPrinterCircuit {
   private static final int init = 256;
   private static final int load = 16;
   private static final int read = 512;

   private AiPrinterCircuit() {
   }

   static Set<BlockPos> init(BlockGetter world, BlockPos source) {
      BlockState state = world.getBlockState(source);
      if (!(state.getBlock() instanceof ComparatorBlock)) {
         return Set.of();
      }

      Direction facing = state.getValue(ComparatorBlock.FACING);
      BlockPos rear = source.relative(facing);
      LinkedHashSet<BlockPos> inputs = new LinkedHashSet<>();
      inputs.add(rear);
      BlockState rearState = world.getBlockState(rear);
      if (!rearState.hasAnalogOutputSignal() && rearState.isRedstoneConductor(world, rear)) {
         inputs.add(rear.relative(facing));
      }

      return Collections.unmodifiableSet(inputs);
   }

   static AiPrinterCircuit.Inner_ConfigStore reachStatus(BlockGetter world, BlockPos source, BlockPos target, boolean quasi) {
      return reachStatusWithin(world, source, target, quasi, 256);
   }

   static boolean reaches(BlockGetter world, BlockPos source, BlockPos target, boolean quasi) {
      return reachStatus(world, source, target, quasi) == AiPrinterCircuit.Inner_ConfigStore.REACHES;
   }

   static AiPrinterCircuit.Inner_ConfigStore reachStatusWithin(BlockGetter world, BlockPos source, BlockPos target, boolean quasi, int budget) {
      return source.equals(target)
         ? AiPrinterCircuit.Inner_ConfigStore.NO_PATH
         : new AiPrinterCircuit.Inner_TaskQueue(world, source.immutable(), target.immutable(), quasi, Math.max(0, budget)).search();
   }

   static Map<BlockPos, AiPrinterCircuit.Inner_ConfigStore> influences(BlockGetter world, BlockPos source, Set<BlockPos> targets) {
      return reachStatuses(world, source, targets, 256);
   }

   static Map<BlockPos, AiPrinterCircuit.Inner_ConfigStore> reachStatuses(BlockGetter world, BlockPos source, Set<BlockPos> targets, int budget) {
      AiPrinterCircuit.Inner_JsonUtil trace = traceAll(world, source, targets, budget);
      HashMap<BlockPos, AiPrinterCircuit.Inner_ConfigStore> result = new HashMap<>();

      for (BlockPos at : targets) {
         result.put(at.immutable(), source.equals(at) ? AiPrinterCircuit.Inner_ConfigStore.NO_PATH : trace.reached().getOrDefault(at, trace.unseen()));
      }

      return Map.copyOf(result);
   }

   static AiPrinterCircuit.Inner_JsonUtil trace(BlockGetter world, BlockPos source, Set<BlockPos> targets) {
      return traceAll(world, source, targets, 256);
   }

   static AiPrinterCircuit.Inner_JsonUtil traceAll(BlockGetter world, BlockPos source, Set<BlockPos> targets, int budget) {
      return traceTargets(world, source, targets, budget, false);
   }

   static AiPrinterCircuit.Inner_JsonUtil load(BlockGetter world, BlockPos source, Set<BlockPos> targets) {
      return traceTargets(world, source, targets, 256, true);
   }

   private static AiPrinterCircuit.Inner_JsonUtil traceTargets(BlockGetter world, BlockPos source, Set<BlockPos> targets, int budget, boolean unfinishedWire) {
      AiPrinterCircuit.Inner_TaskQueue search = new AiPrinterCircuit.Inner_TaskQueue(world, source.immutable(), BlockPos.ZERO, false, Math.max(0, budget));
      search.targets = targets;
      search.unfinishedWire = unfinishedWire;
      search.search();
      search.results.remove(source);
      return new AiPrinterCircuit.Inner_JsonUtil(Map.copyOf(search.results), search.read_Z ? AiPrinterCircuit.Inner_ConfigStore.UNKNOWN : AiPrinterCircuit.Inner_ConfigStore.NO_PATH);
   }

   private static AiPrinterCircuit.Inner_NetHelper combineSignals(AiPrinterCircuit.Inner_NetHelper a, AiPrinterCircuit.Inner_NetHelper b) {
      int low = Math.max(a.low, b.low);
      int high = Math.max(a.high, b.high);
      return new AiPrinterCircuit.Inner_NetHelper(low, high, low != high && (a.unknown || b.unknown));
   }

   private static BlockState asPowered(BlockState state, int strength) {
      if (state.hasProperty(BlockStateProperties.POWERED)) {
         state = state.setValue(BlockStateProperties.POWERED, true);
      }

      if (state.hasProperty(BlockStateProperties.LIT)) {
         state = state.setValue(BlockStateProperties.LIT, true);
      }

      if (state.hasProperty(BlockStateProperties.POWER)) {
         state = state.setValue(BlockStateProperties.POWER, strength);
      }

      return state;
   }

   static boolean isPistonOrDispenser(BlockState state) {
      return state.getBlock() instanceof PistonBaseBlock || state.getBlock() instanceof DispenserBlock;
   }

   private record Inner_BufferHelper(BlockPos at, Direction direction, boolean direct) {
   }

   private record Inner_CachePool(BlockPos at, AiPrinterCircuit.Inner_StreamUtil kind, int strength, boolean wireFed, boolean unknown) {
   }

   enum Inner_ConfigStore {
      REACHES,
      NO_PATH,
      UNKNOWN;
   }

   private record Inner_DataUtil(BlockPos at, AiPrinterCircuit.Inner_StreamUtil kind, boolean wireFed, boolean unknown) {
   }

   record Inner_JsonUtil(Map<BlockPos, AiPrinterCircuit.Inner_ConfigStore> reached, AiPrinterCircuit.Inner_ConfigStore unseen) {
   }

   private record Inner_NetHelper(int low, int high, boolean unknown) {
      static final AiPrinterCircuit.Inner_NetHelper ZERO = new AiPrinterCircuit.Inner_NetHelper(0, 0, false);
      static final AiPrinterCircuit.Inner_NetHelper VARIABLE = new AiPrinterCircuit.Inner_NetHelper(0, 15, false);
      static final AiPrinterCircuit.Inner_NetHelper UNKNOWN = new AiPrinterCircuit.Inner_NetHelper(0, 15, true);
   }

   private enum Inner_StreamUtil {
      OUTPUT,
      CONDUCTOR,
      RAIL;
   }

   private static final class Inner_TaskQueue {
      final BlockGetter world;
      final BlockPos source;
      final BlockPos target;
      final boolean quasi;
      final int budget;
      final ArrayDeque<AiPrinterCircuit.Inner_CachePool> queue = new ArrayDeque<>();
      final Map<AiPrinterCircuit.Inner_DataUtil, Integer> bestStrength = new HashMap<>();
      final Map<AiPrinterCircuit.Inner_BufferHelper, AiPrinterCircuit.Inner_NetHelper> queryCache = new HashMap<>();
      final Set<AiPrinterCircuit.Inner_BufferHelper> activeQueries = new HashSet<>();
      int visitedCount;
      int queryCount;
      boolean load_Z;
      boolean read_Z;
      boolean unfinishedWire;
      Set<BlockPos> targets;
      final Map<BlockPos, AiPrinterCircuit.Inner_ConfigStore> results = new HashMap<>();

      Inner_TaskQueue(final BlockGetter original, BlockPos source, BlockPos target, boolean quasi, int budget) {
         this.source = source;
         this.target = target;
         this.quasi = quasi;
         this.budget = budget;
         this.world = new BlockGetter() {
            final Map<BlockPos, BlockState> stateCache = new HashMap<>();

            @Override
            public BlockState getBlockState(BlockPos p) {
               return this.stateCache.computeIfAbsent(p.immutable(), original::getBlockState);
            }

            @Override
            public FluidState getFluidState(BlockPos p) {
               return this.getBlockState(p).getFluidState();
            }

            @Override
            public BlockEntity getBlockEntity(BlockPos p) {
               return original.getBlockEntity(p);
            }

            @Override
            public int getHeight() {
               return original.getHeight();
            }

            @Override
            public int getMinY() {
               return original.getMinY();
            }
         };
      }

      AiPrinterCircuit.Inner_ConfigStore search() {
         BlockState state = this.world.getBlockState(this.source);
         AiPrinterCircuit.Inner_NetHelper initial = state.getBlock() instanceof DiodeBlock ? this.init_bl_NetHelper(this.source) : AiPrinterCircuit.Inner_NetHelper.VARIABLE;
         this.enqueue(
            this.source,
            state.getBlock() instanceof PoweredRailBlock ? AiPrinterCircuit.Inner_StreamUtil.RAIL : AiPrinterCircuit.Inner_StreamUtil.OUTPUT,
            state.getBlock() instanceof PoweredRailBlock ? 9 : initial.high,
            false,
            initial.unknown
         );

         while (!this.queue.isEmpty()) {
            AiPrinterCircuit.Inner_CachePool n = this.queue.remove();
            AiPrinterCircuit.Inner_DataUtil key = new AiPrinterCircuit.Inner_DataUtil(n.at, n.kind, n.wireFed, n.unknown);
            if (this.bestStrength.getOrDefault(key, 0) < n.strength) {
               if (++this.visitedCount > this.budget) {
                  this.read_Z = true;
                  return AiPrinterCircuit.Inner_ConfigStore.UNKNOWN;
               }

               this.bestStrength.put(key, n.strength);
               state = this.world.getBlockState(n.at);
               boolean wire = n.kind == AiPrinterCircuit.Inner_StreamUtil.OUTPUT && state.getBlock() instanceof RedstoneWireBlock;
               boolean comparator = n.kind == AiPrinterCircuit.Inner_StreamUtil.OUTPUT && state.getBlock() instanceof ComparatorBlock;
               BlockState signal = AiPrinterCircuit.asPowered(state, n.strength);

               for (Direction query : Direction.values()) {
                  BlockPos next = n.at.relative(query.getOpposite());
                  int weak = n.kind == AiPrinterCircuit.Inner_StreamUtil.CONDUCTOR
                     ? n.strength
                     : (comparator ? (state.getValue(ComparatorBlock.FACING) == query ? n.strength : 0) : signal.getSignal(this.world, n.at, query));
                  int direct = n.kind == AiPrinterCircuit.Inner_StreamUtil.CONDUCTOR ? 0 : (comparator ? weak : signal.getDirectSignal(this.world, n.at, query));
                  boolean possibleConnection = this.unfinishedWire && wire && query.getAxis().isHorizontal() && weak == 0;
                  AiPrinterCircuit.Inner_CachePool output = n;
                  if (possibleConnection) {
                     direct = n.strength;
                     weak = n.strength;
                     output = new AiPrinterCircuit.Inner_CachePool(n.at, n.kind, n.strength, n.wireFed, true);
                  }

                  boolean wireInput = this.isWire(next);
                  boolean canFeed = !wireInput || !wire && (n.kind != AiPrinterCircuit.Inner_StreamUtil.CONDUCTOR || !n.wireFed);
                  if (weak > 0 && canFeed && this.init_Z(next, query, output.unknown)) {
                     if (!output.unknown) {
                        return AiPrinterCircuit.Inner_ConfigStore.REACHES;
                     }

                     this.load_Z = true;
                  }

                  this.powerBlock(output, next, query, weak, direct);
                  if (direct > 0 && this.world.getBlockState(next).isRedstoneConductor(this.world, next)) {
                     this.enqueue(next, AiPrinterCircuit.Inner_StreamUtil.CONDUCTOR, direct, wire, output.unknown);
                  }
               }

               if (wire && n.strength > 1) {
                  for (Direction side : Plane.HORIZONTAL) {
                     BlockPos adjacent = n.at.relative(side);

                     for (int dy = -1; dy <= 1; dy++) {
                        BlockPos receiver = adjacent.offset(0, dy, 0);
                        if (this.isWire(receiver) && this.wireConnectsTo(receiver, n.at)) {
                           if (this.targets != null) {
                              if (this.targets.contains(receiver)) {
                                 this.recordReach(receiver, n.unknown);
                              }
                           } else if (receiver.equals(this.target)) {
                              if (!n.unknown) {
                                 return AiPrinterCircuit.Inner_ConfigStore.REACHES;
                              }

                              this.load_Z = true;
                           }

                           this.enqueue(receiver, AiPrinterCircuit.Inner_StreamUtil.OUTPUT, n.strength - 1, false, n.unknown);
                        }
                     }
                  }
               }

               if (n.kind == AiPrinterCircuit.Inner_StreamUtil.RAIL && n.strength > 1) {
                  for (Direction side : Plane.HORIZONTAL) {
                     for (int dy = -1; dy <= 1; dy++) {
                        BlockPos receiver = n.at.relative(side).offset(0, dy, 0);
                        if (this.railFeeds(receiver, n.at)) {
                           this.enqueue(receiver, AiPrinterCircuit.Inner_StreamUtil.RAIL, n.strength - 1, false, n.unknown);
                        }
                     }
                  }
               }
            }
         }

         return this.load_Z ? AiPrinterCircuit.Inner_ConfigStore.UNKNOWN : AiPrinterCircuit.Inner_ConfigStore.NO_PATH;
      }

      void enqueue(BlockPos at, AiPrinterCircuit.Inner_StreamUtil kind, int strength, boolean wireFed, boolean unknown) {
         if (strength > 0) {
            this.queue.add(new AiPrinterCircuit.Inner_CachePool(at.immutable(), kind, Math.min(15, strength), wireFed, unknown));
         }
      }

      boolean isWire(BlockPos p) {
         return this.world.getBlockState(p).getBlock() instanceof RedstoneWireBlock;
      }

      boolean railFeeds(BlockPos receiver, BlockPos emitter) {
         BlockState rail = this.world.getBlockState(receiver);
         BlockState feed = this.world.getBlockState(emitter);
         if (rail.getBlock() instanceof PoweredRailBlock && feed.is(rail.getBlock())) {
            RailShape shape = rail.getValue(PoweredRailBlock.SHAPE);
            RailShape feedShape = feed.getValue(PoweredRailBlock.SHAPE);
            boolean var10000;
            switch (shape) {
               case EAST_WEST:
               case ASCENDING_EAST:
               case ASCENDING_WEST:
                  var10000 = true;
                  break;
               default:
                  var10000 = false;
            }

            boolean eastWest = var10000;
            switch (feedShape) {
               case EAST_WEST:
               case ASCENDING_EAST:
               case ASCENDING_WEST:
                  var10000 = true;
                  break;
               default:
                  var10000 = false;
            }

            boolean feedEastWest = var10000;
            if (eastWest != feedEastWest) {
               return false;
            }

            for (Direction side : eastWest ? new Direction[]{Direction.EAST, Direction.WEST} : new Direction[]{Direction.NORTH, Direction.SOUTH}) {
               switch (shape) {
                  case ASCENDING_EAST:
                     var10000 = side == Direction.EAST;
                     break;
                  case ASCENDING_WEST:
                     var10000 = side == Direction.WEST;
                     break;
                  case ASCENDING_NORTH:
                     var10000 = side == Direction.NORTH;
                     break;
                  case ASCENDING_SOUTH:
                     var10000 = side == Direction.SOUTH;
                     break;
                  default:
                     var10000 = false;
               }

               boolean up = var10000;
               BlockPos next = receiver.relative(side);
               if (up ? next.above().equals(emitter) : next.equals(emitter) || next.below().equals(emitter)) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      boolean wireConnectsTo(BlockPos receiver, BlockPos emitter) {
         for (Direction side : Plane.HORIZONTAL) {
            BlockPos next = receiver.relative(side);
            if (next.equals(emitter)) {
               return true;
            }

            boolean solid = this.world.getBlockState(next).isRedstoneConductor(this.world, next);
            if (solid && !this.world.getBlockState(receiver.above()).isRedstoneConductor(this.world, receiver.above())) {
               if (next.above().equals(emitter)) {
                  return true;
               }
            } else if (!solid && next.below().equals(emitter)) {
               return true;
            }
         }

         return false;
      }

      boolean init_Z(BlockPos cell, Direction query, boolean unknown) {
         if (this.targets != null) {
            for (BlockPos at : new BlockPos[]{cell, cell.below(), cell.above()}) {
               if (this.targets.contains(at) && this.cellPowersTarget(at, AiPrinterCircuit.isPistonOrDispenser(this.world.getBlockState(at)), cell, query)) {
                  this.recordReach(at, unknown);
               }
            }

            return false;
         } else {
            return this.cellPowersTarget(this.target, this.quasi, cell, query);
         }
      }

      private void recordReach(BlockPos at, boolean unknown) {
         if (!at.equals(this.source)) {
            AiPrinterCircuit.Inner_ConfigStore result = unknown ? AiPrinterCircuit.Inner_ConfigStore.UNKNOWN : AiPrinterCircuit.Inner_ConfigStore.REACHES;
            if (this.results.get(at) != AiPrinterCircuit.Inner_ConfigStore.REACHES) {
               this.results.put(at.immutable(), result);
            }
         }
      }

      boolean cellPowersTarget(BlockPos target, boolean quasi, BlockPos cell, Direction query) {
         BlockState receiver = this.world.getBlockState(target);
         if (cell.equals(target)) {
            return !(receiver.getBlock() instanceof PistonBaseBlock) || receiver.getValue(PistonBaseBlock.FACING) != query;
         }

         if (receiver.getBlock() instanceof DoorBlock) {
            BlockPos other = receiver.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? target.above() : target.below();
            if (cell.equals(other)) {
               return true;
            }
         }

         return quasi && cell.equals(target.above()) && (!(receiver.getBlock() instanceof PistonBaseBlock) || query != Direction.DOWN);
      }

      void powerBlock(AiPrinterCircuit.Inner_CachePool n, BlockPos at, Direction input, int weak, int direct) {
         BlockState state = this.world.getBlockState(at);
         Block block = state.getBlock();
         if (block instanceof ObserverBlock) {
            if ((n.kind == AiPrinterCircuit.Inner_StreamUtil.OUTPUT || n.kind == AiPrinterCircuit.Inner_StreamUtil.RAIL) && at.relative(state.getValue(ObserverBlock.FACING)).equals(n.at)) {
               this.enqueue(at, AiPrinterCircuit.Inner_StreamUtil.OUTPUT, 15, false, n.unknown);
            }
         } else if (block instanceof PoweredRailBlock) {
            if (weak > 0) {
               this.enqueue(at, AiPrinterCircuit.Inner_StreamUtil.RAIL, 9, false, n.unknown);
            }
         } else if (block instanceof RedstoneTorchBlock) {
            BlockPos support = block instanceof RedstoneWallTorchBlock ? at.relative(state.getValue(RedstoneWallTorchBlock.FACING).getOpposite()) : at.below();
            if (weak > 0 && support.equals(n.at)) {
               this.enqueue(at, AiPrinterCircuit.Inner_StreamUtil.OUTPUT, 15, false, n.unknown);
            }
         } else if (block instanceof RedstoneWireBlock) {
            if (weak > 0
               && !(this.world.getBlockState(n.at).getBlock() instanceof RedstoneWireBlock)
               && (n.kind != AiPrinterCircuit.Inner_StreamUtil.CONDUCTOR || !n.wireFed)) {
               this.enqueue(at, AiPrinterCircuit.Inner_StreamUtil.OUTPUT, weak, false, n.unknown);
            }
         } else if (block instanceof DiodeBlock) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            boolean rawDust = n.kind == AiPrinterCircuit.Inner_StreamUtil.OUTPUT && this.world.getBlockState(n.at).getBlock() instanceof RedstoneWireBlock;
            if (input == facing) {
               int strength = Math.max(weak, rawDust ? n.strength : 0);
               if (strength <= 0) {
                  return;
               }

               if (block instanceof ComparatorBlock) {
                  AiPrinterCircuit.Inner_NetHelper side = this.diodeSideInput(at, state);
                  BlockState emitter = this.world.getBlockState(n.at);
                  boolean analog = emitter.hasAnalogOutputSignal();
                  if (n.kind == AiPrinterCircuit.Inner_StreamUtil.CONDUCTOR) {
                     BlockPos beyond = n.at.relative(facing);
                     BlockState farther = this.world.getBlockState(beyond);
                     analog |= farther.hasAnalogOutputSignal() || !farther.isRedstoneConductor(this.world, beyond);
                  }

                  int rear = analog ? 15 : strength;
                  int output = state.getValue(ComparatorBlock.MODE) == ComparatorMode.SUBTRACT ? Math.max(0, rear - side.low) : (rear < side.low ? 0 : rear);
                  this.enqueue(at, AiPrinterCircuit.Inner_StreamUtil.OUTPUT, output, false, n.unknown || analog || side.unknown && output > 0);
               } else {
                  this.enqueue(at, AiPrinterCircuit.Inner_StreamUtil.OUTPUT, 15, false, n.unknown);
               }
            } else if (input.getAxis().isHorizontal() && input.getAxis() != facing.getAxis()) {
               BlockState emitter = this.world.getBlockState(n.at);
               boolean diode = n.kind == AiPrinterCircuit.Inner_StreamUtil.OUTPUT && emitter.getBlock() instanceof DiodeBlock;
               int control = block instanceof ComparatorBlock
                  ? (rawDust || emitter.is(Blocks.REDSTONE_BLOCK) ? n.strength : (n.kind == AiPrinterCircuit.Inner_StreamUtil.OUTPUT && emitter.isSignalSource() ? direct : 0))
                  : (diode ? direct : 0);
               if (control <= 0) {
                  return;
               }

               AiPrinterCircuit.Inner_NetHelper rear = this.diodeRearInput(at, state);
               if (block instanceof ComparatorBlock) {
                  AiPrinterCircuit.Inner_NetHelper other = this.init_bl_NetHelper(at.relative(input.getOpposite()), input.getOpposite(), false);
                  int output = 0;
                  if (state.getValue(ComparatorBlock.MODE) == ComparatorMode.SUBTRACT) {
                     if (control > other.low) {
                        output = Math.max(0, rear.high - other.low);
                     }
                  } else {
                     int minimumRear = Math.max(1, Math.max(rear.low, other.low));
                     if (rear.high >= minimumRear && control > minimumRear) {
                        output = Math.min(rear.high, control - 1);
                     }
                  }

                  this.enqueue(at, AiPrinterCircuit.Inner_StreamUtil.OUTPUT, output, false, n.unknown || rear.unknown || other.unknown);
               } else if (rear.high > 0 || state.getValue(BlockStateProperties.POWERED)) {
                  this.enqueue(at, AiPrinterCircuit.Inner_StreamUtil.OUTPUT, 15, false, n.unknown || rear.unknown && !state.getValue(BlockStateProperties.POWERED));
               }
            }
         }
      }

      AiPrinterCircuit.Inner_NetHelper init_bl_NetHelper(BlockPos at) {
         BlockState state = this.world.getBlockState(at);
         AiPrinterCircuit.Inner_NetHelper rear = this.diodeRearInput(at, state);
         if (state.getBlock() instanceof ComparatorBlock) {
            AiPrinterCircuit.Inner_NetHelper side = this.diodeSideInput(at, state);
            int low;
            int high;
            if (state.getValue(ComparatorBlock.MODE) == ComparatorMode.SUBTRACT) {
               low = Math.max(0, rear.low - side.high);
               high = Math.max(0, rear.high - side.low);
            } else {
               low = side.high <= rear.low ? rear.low : 0;
               high = rear.high >= side.low ? rear.high : 0;
            }

            return high == 0 && state.getValue(BlockStateProperties.POWERED)
               ? AiPrinterCircuit.Inner_NetHelper.UNKNOWN
               : new AiPrinterCircuit.Inner_NetHelper(low, high, low != high && (rear.unknown || side.unknown));
         } else if (rear.high > 0) {
            return new AiPrinterCircuit.Inner_NetHelper(0, 15, rear.unknown);
         } else {
            return state.getValue(BlockStateProperties.POWERED) ? AiPrinterCircuit.Inner_NetHelper.VARIABLE : AiPrinterCircuit.Inner_NetHelper.ZERO;
         }
      }

      AiPrinterCircuit.Inner_NetHelper diodeRearInput(BlockPos at, BlockState diode) {
         Direction direction = diode.getValue(BlockStateProperties.HORIZONTAL_FACING);
         BlockPos rearPos = at.relative(direction);
         BlockState rear = this.world.getBlockState(rearPos);
         AiPrinterCircuit.Inner_NetHelper supply = this.signalInto(rearPos, direction, false);
         if (rear.getBlock() instanceof RedstoneWireBlock) {
            supply = AiPrinterCircuit.combineSignals(supply, AiPrinterCircuit.Inner_NetHelper.VARIABLE);
         }

         if (diode.getBlock() instanceof ComparatorBlock) {
            if (rear.hasAnalogOutputSignal()) {
               return AiPrinterCircuit.Inner_NetHelper.UNKNOWN;
            }

            if (rear.isRedstoneConductor(this.world, rearPos) && supply.high < 15) {
               BlockState farther = this.world.getBlockState(rearPos.relative(direction));
               if (farther.hasAnalogOutputSignal() || !farther.isRedstoneConductor(this.world, rearPos.relative(direction))) {
                  return AiPrinterCircuit.Inner_NetHelper.UNKNOWN;
               }
            }
         }

         return supply;
      }

      AiPrinterCircuit.Inner_NetHelper diodeSideInput(BlockPos at, BlockState diode) {
         Direction a = diode.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise();
         Direction b = a.getOpposite();
         boolean onlyDiodes = !(diode.getBlock() instanceof ComparatorBlock);
         return AiPrinterCircuit.combineSignals(this.init_bl_NetHelper(at.relative(a), a, onlyDiodes), this.init_bl_NetHelper(at.relative(b), b, onlyDiodes));
      }

      AiPrinterCircuit.Inner_NetHelper init_bl_NetHelper(BlockPos at, Direction direction, boolean onlyDiodes) {
         BlockState state = this.world.getBlockState(at);
         if (onlyDiodes) {
            return state.getBlock() instanceof DiodeBlock ? this.signalInto(at, direction, true) : AiPrinterCircuit.Inner_NetHelper.ZERO;
         } else if (state.is(Blocks.REDSTONE_BLOCK)) {
            return new AiPrinterCircuit.Inner_NetHelper(15, 15, false);
         } else if (state.getBlock() instanceof RedstoneWireBlock) {
            return AiPrinterCircuit.Inner_NetHelper.VARIABLE;
         } else {
            return state.isSignalSource() ? this.signalInto(at, direction, true) : AiPrinterCircuit.Inner_NetHelper.ZERO;
         }
      }

      AiPrinterCircuit.Inner_NetHelper signalInto(BlockPos at, Direction direction, boolean direct) {
         AiPrinterCircuit.Inner_BufferHelper query = new AiPrinterCircuit.Inner_BufferHelper(at, direction, direct);
         AiPrinterCircuit.Inner_NetHelper saved = this.queryCache.get(query);
         if (saved != null) {
            return saved;
         }

         if (this.activeQueries.size() < 16 && this.queryCount < 512 && this.activeQueries.add(query)) {
            this.queryCount++;
            BlockState state = this.world.getBlockState(at);
            AiPrinterCircuit.Inner_NetHelper result;
            if (state.getBlock() instanceof DiodeBlock) {
               result = state.getValue(BlockStateProperties.HORIZONTAL_FACING) == direction ? this.init_bl_NetHelper(at) : AiPrinterCircuit.Inner_NetHelper.ZERO;
            } else {
               BlockState on = AiPrinterCircuit.asPowered(state, 15);
               int high = direct ? on.getDirectSignal(this.world, at, direction) : on.getSignal(this.world, at, direction);
               result = new AiPrinterCircuit.Inner_NetHelper(state.is(Blocks.REDSTONE_BLOCK) ? high : 0, high, false);
            }

            if (!direct && state.isRedstoneConductor(this.world, at)) {
               for (Direction side : Direction.values()) {
                  result = AiPrinterCircuit.combineSignals(result, this.signalInto(at.relative(side), side, true));
               }
            }

            this.activeQueries.remove(query);
            this.queryCache.put(query, result);
            return result;
         } else {
            return AiPrinterCircuit.Inner_NetHelper.UNKNOWN;
         }
      }
   }
}
