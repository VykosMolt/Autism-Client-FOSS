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

   static AiPrinterCircuit.ReachStatus reachStatus(BlockGetter world, BlockPos source, BlockPos target, boolean quasi) {
      return reachStatusWithin(world, source, target, quasi, 256);
   }

   static boolean reaches(BlockGetter world, BlockPos source, BlockPos target, boolean quasi) {
      return reachStatus(world, source, target, quasi) == AiPrinterCircuit.ReachStatus.REACHES;
   }

   static AiPrinterCircuit.ReachStatus reachStatusWithin(BlockGetter world, BlockPos source, BlockPos target, boolean quasi, int budget) {
      return source.equals(target)
         ? AiPrinterCircuit.ReachStatus.NO_PATH
         : new AiPrinterCircuit.SignalSearch(world, source.immutable(), target.immutable(), quasi, Math.max(0, budget)).search();
   }

   static Map<BlockPos, AiPrinterCircuit.ReachStatus> influences(BlockGetter world, BlockPos source, Set<BlockPos> targets) {
      return reachStatuses(world, source, targets, 256);
   }

   static Map<BlockPos, AiPrinterCircuit.ReachStatus> reachStatuses(BlockGetter world, BlockPos source, Set<BlockPos> targets, int budget) {
      AiPrinterCircuit.Trace trace = traceAll(world, source, targets, budget);
      HashMap<BlockPos, AiPrinterCircuit.ReachStatus> result = new HashMap<>();

      for (BlockPos at : targets) {
         result.put(at.immutable(), source.equals(at) ? AiPrinterCircuit.ReachStatus.NO_PATH : trace.reached().getOrDefault(at, trace.unseen()));
      }

      return Map.copyOf(result);
   }

   static AiPrinterCircuit.Trace trace(BlockGetter world, BlockPos source, Set<BlockPos> targets) {
      return traceAll(world, source, targets, 256);
   }

   static AiPrinterCircuit.Trace traceAll(BlockGetter world, BlockPos source, Set<BlockPos> targets, int budget) {
      return traceTargets(world, source, targets, budget, false);
   }

   static AiPrinterCircuit.Trace load(BlockGetter world, BlockPos source, Set<BlockPos> targets) {
      return traceTargets(world, source, targets, 256, true);
   }

   private static AiPrinterCircuit.Trace traceTargets(BlockGetter world, BlockPos source, Set<BlockPos> targets, int budget, boolean unfinishedWire) {
      AiPrinterCircuit.SignalSearch search = new AiPrinterCircuit.SignalSearch(world, source.immutable(), BlockPos.ZERO, false, Math.max(0, budget));
      search.targets = targets;
      search.unfinishedWire = unfinishedWire;
      search.search();
      search.results.remove(source);
      return new AiPrinterCircuit.Trace(Map.copyOf(search.results), search.read_Z ? AiPrinterCircuit.ReachStatus.UNKNOWN : AiPrinterCircuit.ReachStatus.NO_PATH);
   }

   private static AiPrinterCircuit.SignalRange combineSignals(AiPrinterCircuit.SignalRange a, AiPrinterCircuit.SignalRange b) {
      int low = Math.max(a.low, b.low);
      int high = Math.max(a.high, b.high);
      return new AiPrinterCircuit.SignalRange(low, high, low != high && (a.unknown || b.unknown));
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

   private record PowerQuery(BlockPos at, Direction direction, boolean direct) {
   }

   private record Node(BlockPos at, AiPrinterCircuit.NodeKind kind, int strength, boolean wireFed, boolean unknown) {
   }

   enum ReachStatus {
      REACHES,
      NO_PATH,
      UNKNOWN;
   }

   private record NodeKey(BlockPos at, AiPrinterCircuit.NodeKind kind, boolean wireFed, boolean unknown) {
   }

   record Trace(Map<BlockPos, AiPrinterCircuit.ReachStatus> reached, AiPrinterCircuit.ReachStatus unseen) {
   }

   private record SignalRange(int low, int high, boolean unknown) {
      static final AiPrinterCircuit.SignalRange ZERO = new AiPrinterCircuit.SignalRange(0, 0, false);
      static final AiPrinterCircuit.SignalRange VARIABLE = new AiPrinterCircuit.SignalRange(0, 15, false);
      static final AiPrinterCircuit.SignalRange UNKNOWN = new AiPrinterCircuit.SignalRange(0, 15, true);
   }

   private enum NodeKind {
      OUTPUT,
      CONDUCTOR,
      RAIL;
   }

   private static final class SignalSearch {
      final BlockGetter world;
      final BlockPos source;
      final BlockPos target;
      final boolean quasi;
      final int budget;
      final ArrayDeque<AiPrinterCircuit.Node> queue = new ArrayDeque<>();
      final Map<AiPrinterCircuit.NodeKey, Integer> bestStrength = new HashMap<>();
      final Map<AiPrinterCircuit.PowerQuery, AiPrinterCircuit.SignalRange> queryCache = new HashMap<>();
      final Set<AiPrinterCircuit.PowerQuery> activeQueries = new HashSet<>();
      int visitedCount;
      int queryCount;
      boolean load_Z;
      boolean read_Z;
      boolean unfinishedWire;
      Set<BlockPos> targets;
      final Map<BlockPos, AiPrinterCircuit.ReachStatus> results = new HashMap<>();

      SignalSearch(final BlockGetter original, BlockPos source, BlockPos target, boolean quasi, int budget) {
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

      AiPrinterCircuit.ReachStatus search() {
         BlockState state = this.world.getBlockState(this.source);
         AiPrinterCircuit.SignalRange initial = state.getBlock() instanceof DiodeBlock ? this.init_bl_NetHelper(this.source) : AiPrinterCircuit.SignalRange.VARIABLE;
         this.enqueue(
            this.source,
            state.getBlock() instanceof PoweredRailBlock ? AiPrinterCircuit.NodeKind.RAIL : AiPrinterCircuit.NodeKind.OUTPUT,
            state.getBlock() instanceof PoweredRailBlock ? 9 : initial.high,
            false,
            initial.unknown
         );

         while (!this.queue.isEmpty()) {
            AiPrinterCircuit.Node n = this.queue.remove();
            AiPrinterCircuit.NodeKey key = new AiPrinterCircuit.NodeKey(n.at, n.kind, n.wireFed, n.unknown);
            if (this.bestStrength.getOrDefault(key, 0) < n.strength) {
               if (++this.visitedCount > this.budget) {
                  this.read_Z = true;
                  return AiPrinterCircuit.ReachStatus.UNKNOWN;
               }

               this.bestStrength.put(key, n.strength);
               state = this.world.getBlockState(n.at);
               boolean wire = n.kind == AiPrinterCircuit.NodeKind.OUTPUT && state.getBlock() instanceof RedstoneWireBlock;
               boolean comparator = n.kind == AiPrinterCircuit.NodeKind.OUTPUT && state.getBlock() instanceof ComparatorBlock;
               BlockState signal = AiPrinterCircuit.asPowered(state, n.strength);

               for (Direction query : Direction.values()) {
                  BlockPos next = n.at.relative(query.getOpposite());
                  int weak = n.kind == AiPrinterCircuit.NodeKind.CONDUCTOR
                     ? n.strength
                     : (comparator ? (state.getValue(ComparatorBlock.FACING) == query ? n.strength : 0) : signal.getSignal(this.world, n.at, query));
                  int direct = n.kind == AiPrinterCircuit.NodeKind.CONDUCTOR ? 0 : (comparator ? weak : signal.getDirectSignal(this.world, n.at, query));
                  boolean possibleConnection = this.unfinishedWire && wire && query.getAxis().isHorizontal() && weak == 0;
                  AiPrinterCircuit.Node output = n;
                  if (possibleConnection) {
                     direct = n.strength;
                     weak = n.strength;
                     output = new AiPrinterCircuit.Node(n.at, n.kind, n.strength, n.wireFed, true);
                  }

                  boolean wireInput = this.isWire(next);
                  boolean canFeed = !wireInput || !wire && (n.kind != AiPrinterCircuit.NodeKind.CONDUCTOR || !n.wireFed);
                  if (weak > 0 && canFeed && this.init_Z(next, query, output.unknown)) {
                     if (!output.unknown) {
                        return AiPrinterCircuit.ReachStatus.REACHES;
                     }

                     this.load_Z = true;
                  }

                  this.powerBlock(output, next, query, weak, direct);
                  if (direct > 0 && this.world.getBlockState(next).isRedstoneConductor(this.world, next)) {
                     this.enqueue(next, AiPrinterCircuit.NodeKind.CONDUCTOR, direct, wire, output.unknown);
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
                                 return AiPrinterCircuit.ReachStatus.REACHES;
                              }

                              this.load_Z = true;
                           }

                           this.enqueue(receiver, AiPrinterCircuit.NodeKind.OUTPUT, n.strength - 1, false, n.unknown);
                        }
                     }
                  }
               }

               if (n.kind == AiPrinterCircuit.NodeKind.RAIL && n.strength > 1) {
                  for (Direction side : Plane.HORIZONTAL) {
                     for (int dy = -1; dy <= 1; dy++) {
                        BlockPos receiver = n.at.relative(side).offset(0, dy, 0);
                        if (this.railFeeds(receiver, n.at)) {
                           this.enqueue(receiver, AiPrinterCircuit.NodeKind.RAIL, n.strength - 1, false, n.unknown);
                        }
                     }
                  }
               }
            }
         }

         return this.load_Z ? AiPrinterCircuit.ReachStatus.UNKNOWN : AiPrinterCircuit.ReachStatus.NO_PATH;
      }

      void enqueue(BlockPos at, AiPrinterCircuit.NodeKind kind, int strength, boolean wireFed, boolean unknown) {
         if (strength > 0) {
            this.queue.add(new AiPrinterCircuit.Node(at.immutable(), kind, Math.min(15, strength), wireFed, unknown));
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
            AiPrinterCircuit.ReachStatus result = unknown ? AiPrinterCircuit.ReachStatus.UNKNOWN : AiPrinterCircuit.ReachStatus.REACHES;
            if (this.results.get(at) != AiPrinterCircuit.ReachStatus.REACHES) {
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

      void powerBlock(AiPrinterCircuit.Node n, BlockPos at, Direction input, int weak, int direct) {
         BlockState state = this.world.getBlockState(at);
         Block block = state.getBlock();
         if (block instanceof ObserverBlock) {
            if ((n.kind == AiPrinterCircuit.NodeKind.OUTPUT || n.kind == AiPrinterCircuit.NodeKind.RAIL) && at.relative(state.getValue(ObserverBlock.FACING)).equals(n.at)) {
               this.enqueue(at, AiPrinterCircuit.NodeKind.OUTPUT, 15, false, n.unknown);
            }
         } else if (block instanceof PoweredRailBlock) {
            if (weak > 0) {
               this.enqueue(at, AiPrinterCircuit.NodeKind.RAIL, 9, false, n.unknown);
            }
         } else if (block instanceof RedstoneTorchBlock) {
            BlockPos support = block instanceof RedstoneWallTorchBlock ? at.relative(state.getValue(RedstoneWallTorchBlock.FACING).getOpposite()) : at.below();
            if (weak > 0 && support.equals(n.at)) {
               this.enqueue(at, AiPrinterCircuit.NodeKind.OUTPUT, 15, false, n.unknown);
            }
         } else if (block instanceof RedstoneWireBlock) {
            if (weak > 0
               && !(this.world.getBlockState(n.at).getBlock() instanceof RedstoneWireBlock)
               && (n.kind != AiPrinterCircuit.NodeKind.CONDUCTOR || !n.wireFed)) {
               this.enqueue(at, AiPrinterCircuit.NodeKind.OUTPUT, weak, false, n.unknown);
            }
         } else if (block instanceof DiodeBlock) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            boolean rawDust = n.kind == AiPrinterCircuit.NodeKind.OUTPUT && this.world.getBlockState(n.at).getBlock() instanceof RedstoneWireBlock;
            if (input == facing) {
               int strength = Math.max(weak, rawDust ? n.strength : 0);
               if (strength <= 0) {
                  return;
               }

               if (block instanceof ComparatorBlock) {
                  AiPrinterCircuit.SignalRange side = this.diodeSideInput(at, state);
                  BlockState emitter = this.world.getBlockState(n.at);
                  boolean analog = emitter.hasAnalogOutputSignal();
                  if (n.kind == AiPrinterCircuit.NodeKind.CONDUCTOR) {
                     BlockPos beyond = n.at.relative(facing);
                     BlockState farther = this.world.getBlockState(beyond);
                     analog |= farther.hasAnalogOutputSignal() || !farther.isRedstoneConductor(this.world, beyond);
                  }

                  int rear = analog ? 15 : strength;
                  int output = state.getValue(ComparatorBlock.MODE) == ComparatorMode.SUBTRACT ? Math.max(0, rear - side.low) : (rear < side.low ? 0 : rear);
                  this.enqueue(at, AiPrinterCircuit.NodeKind.OUTPUT, output, false, n.unknown || analog || side.unknown && output > 0);
               } else {
                  this.enqueue(at, AiPrinterCircuit.NodeKind.OUTPUT, 15, false, n.unknown);
               }
            } else if (input.getAxis().isHorizontal() && input.getAxis() != facing.getAxis()) {
               BlockState emitter = this.world.getBlockState(n.at);
               boolean diode = n.kind == AiPrinterCircuit.NodeKind.OUTPUT && emitter.getBlock() instanceof DiodeBlock;
               int control = block instanceof ComparatorBlock
                  ? (rawDust || emitter.is(Blocks.REDSTONE_BLOCK) ? n.strength : (n.kind == AiPrinterCircuit.NodeKind.OUTPUT && emitter.isSignalSource() ? direct : 0))
                  : (diode ? direct : 0);
               if (control <= 0) {
                  return;
               }

               AiPrinterCircuit.SignalRange rear = this.diodeRearInput(at, state);
               if (block instanceof ComparatorBlock) {
                  AiPrinterCircuit.SignalRange other = this.init_bl_NetHelper(at.relative(input.getOpposite()), input.getOpposite(), false);
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

                  this.enqueue(at, AiPrinterCircuit.NodeKind.OUTPUT, output, false, n.unknown || rear.unknown || other.unknown);
               } else if (rear.high > 0 || state.getValue(BlockStateProperties.POWERED)) {
                  this.enqueue(at, AiPrinterCircuit.NodeKind.OUTPUT, 15, false, n.unknown || rear.unknown && !state.getValue(BlockStateProperties.POWERED));
               }
            }
         }
      }

      AiPrinterCircuit.SignalRange init_bl_NetHelper(BlockPos at) {
         BlockState state = this.world.getBlockState(at);
         AiPrinterCircuit.SignalRange rear = this.diodeRearInput(at, state);
         if (state.getBlock() instanceof ComparatorBlock) {
            AiPrinterCircuit.SignalRange side = this.diodeSideInput(at, state);
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
               ? AiPrinterCircuit.SignalRange.UNKNOWN
               : new AiPrinterCircuit.SignalRange(low, high, low != high && (rear.unknown || side.unknown));
         } else if (rear.high > 0) {
            return new AiPrinterCircuit.SignalRange(0, 15, rear.unknown);
         } else {
            return state.getValue(BlockStateProperties.POWERED) ? AiPrinterCircuit.SignalRange.VARIABLE : AiPrinterCircuit.SignalRange.ZERO;
         }
      }

      AiPrinterCircuit.SignalRange diodeRearInput(BlockPos at, BlockState diode) {
         Direction direction = diode.getValue(BlockStateProperties.HORIZONTAL_FACING);
         BlockPos rearPos = at.relative(direction);
         BlockState rear = this.world.getBlockState(rearPos);
         AiPrinterCircuit.SignalRange supply = this.signalInto(rearPos, direction, false);
         if (rear.getBlock() instanceof RedstoneWireBlock) {
            supply = AiPrinterCircuit.combineSignals(supply, AiPrinterCircuit.SignalRange.VARIABLE);
         }

         if (diode.getBlock() instanceof ComparatorBlock) {
            if (rear.hasAnalogOutputSignal()) {
               return AiPrinterCircuit.SignalRange.UNKNOWN;
            }

            if (rear.isRedstoneConductor(this.world, rearPos) && supply.high < 15) {
               BlockState farther = this.world.getBlockState(rearPos.relative(direction));
               if (farther.hasAnalogOutputSignal() || !farther.isRedstoneConductor(this.world, rearPos.relative(direction))) {
                  return AiPrinterCircuit.SignalRange.UNKNOWN;
               }
            }
         }

         return supply;
      }

      AiPrinterCircuit.SignalRange diodeSideInput(BlockPos at, BlockState diode) {
         Direction a = diode.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise();
         Direction b = a.getOpposite();
         boolean onlyDiodes = !(diode.getBlock() instanceof ComparatorBlock);
         return AiPrinterCircuit.combineSignals(this.init_bl_NetHelper(at.relative(a), a, onlyDiodes), this.init_bl_NetHelper(at.relative(b), b, onlyDiodes));
      }

      AiPrinterCircuit.SignalRange init_bl_NetHelper(BlockPos at, Direction direction, boolean onlyDiodes) {
         BlockState state = this.world.getBlockState(at);
         if (onlyDiodes) {
            return state.getBlock() instanceof DiodeBlock ? this.signalInto(at, direction, true) : AiPrinterCircuit.SignalRange.ZERO;
         } else if (state.is(Blocks.REDSTONE_BLOCK)) {
            return new AiPrinterCircuit.SignalRange(15, 15, false);
         } else if (state.getBlock() instanceof RedstoneWireBlock) {
            return AiPrinterCircuit.SignalRange.VARIABLE;
         } else {
            return state.isSignalSource() ? this.signalInto(at, direction, true) : AiPrinterCircuit.SignalRange.ZERO;
         }
      }

      AiPrinterCircuit.SignalRange signalInto(BlockPos at, Direction direction, boolean direct) {
         AiPrinterCircuit.PowerQuery query = new AiPrinterCircuit.PowerQuery(at, direction, direct);
         AiPrinterCircuit.SignalRange saved = this.queryCache.get(query);
         if (saved != null) {
            return saved;
         }

         if (this.activeQueries.size() < 16 && this.queryCount < 512 && this.activeQueries.add(query)) {
            this.queryCount++;
            BlockState state = this.world.getBlockState(at);
            AiPrinterCircuit.SignalRange result;
            if (state.getBlock() instanceof DiodeBlock) {
               result = state.getValue(BlockStateProperties.HORIZONTAL_FACING) == direction ? this.init_bl_NetHelper(at) : AiPrinterCircuit.SignalRange.ZERO;
            } else {
               BlockState on = AiPrinterCircuit.asPowered(state, 15);
               int high = direct ? on.getDirectSignal(this.world, at, direction) : on.getSignal(this.world, at, direction);
               result = new AiPrinterCircuit.SignalRange(state.is(Blocks.REDSTONE_BLOCK) ? high : 0, high, false);
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
            return AiPrinterCircuit.SignalRange.UNKNOWN;
         }
      }
   }
}
