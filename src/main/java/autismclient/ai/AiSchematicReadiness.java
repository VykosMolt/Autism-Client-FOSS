package autismclient.ai;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

final class AiSchematicReadiness {
   private static final AiSchematicReadiness.InconclusiveCheck INCONCLUSIVE = new AiSchematicReadiness.InconclusiveCheck();
   private static final int init_I = 1048576;
   private static final int[][] SIDES = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

   private AiSchematicReadiness() {
   }

   static String blockingReason(AiSchematicReadiness.ReadinessReport report) {
      if (!report.ready() && !report.issues().isEmpty()) {
         AiSchematicReadiness.ReadinessIssue first = report.issues().getFirst();
         AiBuilder.CellSpec cell = first.cell();
         String block = cell.block().startsWith("minecraft:") ? cell.block().substring("minecraft:".length()) : cell.block();
         String var10000;
         switch (first.kind()) {
            case UNSUPPORTED_STATE:
            case NO_ACTION_PLAN:
               var10000 = first.detail();
               break;
            case CONFLICTING_PAIR:
               var10000 = "its other half is a different saved block";
               break;
            case MISSING_CONTEXT:
               var10000 = "it needs blocks outside the schematic";
               break;
            case NO_SUPPORT:
               var10000 = first.detail().replace(" in the finished schematic, which cannot hold it; vanilla breaks it off", "") + ", which cannot hold it";
               break;
            case FALLS:
               var10000 = "nothing holds it up, so it falls";
               break;
            case NO_SOURCE:
               var10000 = "flowing with no source, so it drains";
               break;
            default:
               throw new MatchException(null, null);
         }

         String why = var10000;
         return first.kind() != AiSchematicReadiness.IssueKind.UNSUPPORTED_STATE && first.kind() != AiSchematicReadiness.IssueKind.NO_ACTION_PLAN
            ? "Cannot build as saved: "
               + report.issueCount()
               + (report.issueCount() == 1L ? " block" : " blocks")
               + " cannot exist in vanilla. First: "
               + block
               + " at "
               + cell.dx()
               + ","
               + cell.dy()
               + ","
               + cell.dz()
               + " ("
               + why
               + ")"
            : "Cannot build as saved: the builder has no placement method for "
               + block
               + " at "
               + cell.dx()
               + ","
               + cell.dy()
               + ","
               + cell.dz()
               + " ("
               + why
               + ")";
      } else {
         return "";
      }
   }

   static List<AiSchematicReadiness.ReadinessIssue> issuesOf(AiBuilder.Blueprint blueprint) {
      return assessBlueprint(blueprint, 2147483647).issues();
   }

   static AiSchematicReadiness.ReadinessReport assessBlueprint(AiBuilder.Blueprint blueprint, int exampleLimit) {
      return assess(blueprint, exampleLimit, null);
   }

   static AiSchematicReadiness.ReadinessReport assess(AiBuilder.Blueprint blueprint, int exampleLimit, Map<Long, BlockState> context) {
      return assess(blueprint, exampleLimit, context, null);
   }

   static AiSchematicReadiness.BuildableAssessment assessBuildable(AiBuilder.Blueprint requested, int exampleLimit, Map<Long, BlockState> context) {
      AiBuilder.Blueprint blueprint = requested;
      LongOpenHashSet omitted = new LongOpenHashSet();
      ArrayList<AiSchematicReadiness.ReadinessIssue> examples = new ArrayList<>();
      long issueCount = 0L;
      int distinctStates = 0;
      Map<Long, BlockState> observed = context;

      ArrayList<AiBuilder.CellSpec> kept;
      do {
         AiBuilder.checkNotCancelled();
         omitted.clear();
         AiSchematicReadiness.ReadinessReport audit = assess(blueprint, exampleLimit - examples.size(), observed, omitted);
         issueCount += audit.issueCount();
         distinctStates = Math.max(distinctStates, audit.distinctStates());
         examples.addAll(audit.issues());
         if (omitted.isEmpty()) {
            break;
         }

         if (observed == context) {
            observed = new HashMap<>();
            if (context != null) {
               observed.putAll(context);
            }
         }

         kept = new ArrayList<>(blueprint.cells().size() - omitted.size());

         for (AiBuilder.CellSpec cell : blueprint.cells()) {
            AiBuilder.checkNotCancelled();
            if (omitted.contains(packKey(cell.dx(), cell.dy(), cell.dz()))) {
               observed.putIfAbsent(blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()).asLong(), Blocks.AIR.defaultBlockState());
            } else {
               kept.add(cell);
            }
         }

         blueprint = new AiBuilder.Blueprint(blueprint.origin(), List.copyOf(kept));
      } while (!kept.isEmpty());

      return new AiSchematicReadiness.BuildableAssessment(blueprint, requested.cells().size() - blueprint.cells().size(), new AiSchematicReadiness.ReadinessReport(issueCount, examples, distinctStates));
   }

   private static AiSchematicReadiness.ReadinessReport assess(AiBuilder.Blueprint blueprint, int exampleLimit, Map<Long, BlockState> context, LongSet omitted) {
      if (exampleLimit < 0) {
         throw new IllegalArgumentException("negative readiness example limit");
      }

      ArrayList<AiSchematicReadiness.ReadinessIssue> issues = new ArrayList<>();
      HashMap<AiSchematicReadiness.BlockStateKey, List<AiSchematicReadiness.StateProblem>> states = new HashMap<>();
      HashMap<AiSchematicReadiness.BlockStateKey, Optional<AiSchematicReadiness.PairPlan>> pairPlans = new HashMap<>();
      Long2ObjectOpenHashMap<AiBuilder.CellSpec> partners = requiredPairCells(blueprint, pairPlans);
      long issueCount = 0L;

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         AiBuilder.checkNotCancelled();
         List<AiSchematicReadiness.StateProblem> problems = states.computeIfAbsent(new AiSchematicReadiness.BlockStateKey(cell.block(), cell.properties()), ignored -> stateProblems(cell));
         issueCount += problems.size();
         if (omitted != null && !problems.isEmpty()) {
            omitted.add(packKey(cell.dx(), cell.dy(), cell.dz()));
         }

         for (AiSchematicReadiness.StateProblem problem : problems) {
            if (issues.size() >= exampleLimit) {
               break;
            }

            issues.add(new AiSchematicReadiness.ReadinessIssue(problem.kind(), cell, problem.detail()));
         }

         AiSchematicReadiness.PairPlan pair = pairPlanFor(cell, pairPlans);
         if (pair != null) {
            BlockPos at = new BlockPos(cell.dx(), cell.dy(), cell.dz()).relative(pair.direction());
            AiBuilder.CellSpec partner = partners.get(AiBuilder.packOffsetKey(at.getX(), at.getY(), at.getZ()));
            BlockState actual = partner == null
               ? (context == null ? null : context.get(blueprint.origin().offset(at).asLong()))
               : AiBuilder.toBlockState(partner);
            if ((partner != null || pair.needsExisting() && context != null) && (actual == null || !isPairPartner(pair.wanted(), actual))) {
               issueCount++;
               if (omitted != null) {
                  omitted.add(packKey(cell.dx(), cell.dy(), cell.dz()));
               }

               if (issues.size() < exampleLimit) {
                  issues.add(
                     new AiSchematicReadiness.ReadinessIssue(
                        partner == null ? AiSchematicReadiness.IssueKind.MISSING_CONTEXT : AiSchematicReadiness.IssueKind.CONFLICTING_PAIR,
                        cell,
                        "requires its native paired half at "
                           + at.toShortString()
                           + "; "
                           + (partner == null ? "outside-file context absent or not observed" : "saved cell is " + actual)
                     )
                  );
               }
            }
         }
      }

      issueCount += init(blueprint, context, pairPlans, exampleLimit, issues, omitted);
      return new AiSchematicReadiness.ReadinessReport(issueCount, issues, states.size());
   }

   private static long packKey(int x, int y, int z) {
      return (long)(x + 1048576) << 42 | (long)(y + 1048576) << 21 | z + 1048576;
   }

   private static long init(
      AiBuilder.Blueprint blueprint,
      Map<Long, BlockState> context,
      Map<AiSchematicReadiness.BlockStateKey, Optional<AiSchematicReadiness.PairPlan>> pairPlans,
      int exampleLimit,
      List<AiSchematicReadiness.ReadinessIssue> issues,
      LongSet omitted
   ) {
      List<AiBuilder.CellSpec> cells = blueprint.cells();
      if (cells.isEmpty()) {
         return 0L;
      }

      HashMap<AiSchematicReadiness.BlockStateKey, BlockState> resolved = new HashMap<>();
      Long2ObjectOpenHashMap<BlockState> placed = new Long2ObjectOpenHashMap<>(cells.size());
      int minX = 2147483647;
      int minY = 2147483647;
      int minZ = 2147483647;
      int maxX = -2147483648;
      int maxY = -2147483648;
      int maxZ = -2147483648;
      HashMap<BlockState, Boolean> needs = new HashMap<>();
      ArrayList<AiBuilder.CellSpec> checked = new ArrayList<>();

      for (AiBuilder.CellSpec cell : cells) {
         AiBuilder.checkNotCancelled();
         BlockState state = resolved.computeIfAbsent(new AiSchematicReadiness.BlockStateKey(cell.block(), cell.properties()), ignored -> AiBuilder.toBlockState(cell));
         placed.put(packKey(cell.dx(), cell.dy(), cell.dz()), state);
         minX = Math.min(minX, cell.dx());
         maxX = Math.max(maxX, cell.dx());
         minY = Math.min(minY, cell.dy());
         maxY = Math.max(maxY, cell.dy());
         minZ = Math.min(minZ, cell.dz());
         maxZ = Math.max(maxZ, cell.dz());
         if (needs.computeIfAbsent(state, AiSchematicReadiness::needsSupport)) {
            checked.add(cell);
         }
      }

      for (AiBuilder.CellSpec cell : cells) {
         BlockState state = resolved.get(new AiSchematicReadiness.BlockStateKey(cell.block(), cell.properties()));
         if (state.getBlock() instanceof PistonBaseBlock && state.getValue(PistonBaseBlock.EXTENDED)) {
            Direction facing = state.getValue(PistonBaseBlock.FACING);
            long head = packKey(cell.dx() + facing.getStepX(), cell.dy() + facing.getStepY(), cell.dz() + facing.getStepZ());
            if (!placed.containsKey(head)) {
               placed.put(
                  head,
                  Blocks.PISTON_HEAD
                     .defaultBlockState()
                     .setValue(PistonHeadBlock.FACING, facing)
                     .setValue(PistonHeadBlock.TYPE, state.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT)
               );
            }
         }
      }

      int x0 = minX;
      int y0 = minY;
      int z0 = minZ;
      int x1 = maxX;
      int y1 = maxY;
      int z1 = maxZ;
      BlockPos origin = blueprint.origin();
      Function<BlockPos, BlockState> finished = posx -> {
         int x = posx.getX() - origin.getX();
         int y = posx.getY() - origin.getY();
         int z = posx.getZ() - origin.getZ();
         BlockState statex = placed.get(packKey(x, y, z));
         if (statex != null) {
            return statex;
         } else if (x >= x0 && x <= x1 && y >= y0 && y <= y1 && z >= z0 && z <= z1) {
            return Blocks.AIR.defaultBlockState();
         } else {
            BlockState known = context == null ? null : context.get(posx.asLong());
            if (known == null) {
               throw INCONCLUSIVE;
            } else {
               return known;
            }
         }
      };
      LevelReader reader = levelReaderOf(finished);
      long count = 0L;

      for (AiBuilder.CellSpec cell : checked) {
         AiBuilder.checkNotCancelled();
         AiSchematicReadiness.PairPlan plan = pairPlanFor(cell, pairPlans);
         if (plan == null || !plan.needsExisting()) {
            BlockState state = resolved.get(new AiSchematicReadiness.BlockStateKey(cell.block(), cell.properties()));
            BlockPos pos = origin.offset(cell.dx(), cell.dy(), cell.dz());
            AiSchematicReadiness.StateProblem problem = null;

            try {
               if (state.getBlock() instanceof FallingBlock) {
                  BlockState below = finished.apply(pos.below());
                  if (FallingBlock.isFree(below)) {
                     problem = new AiSchematicReadiness.StateProblem(
                        AiSchematicReadiness.IssueKind.FALLS, "rests on " + BuiltInRegistries.BLOCK.getKey(below.getBlock()) + " in the finished schematic, so it falls"
                     );
                  }
               } else if (!state.canSurvive(reader, pos)) {
                  problem = new AiSchematicReadiness.StateProblem(
                     AiSchematicReadiness.IssueKind.NO_SUPPORT, supportProblem(state, pos, origin, finished) + " in the finished schematic, which cannot hold it; vanilla breaks it off"
                  );
               }
            } catch (AiSchematicReadiness.InconclusiveCheck inconclusive) {
               continue;
            } catch (RuntimeException unavailable) {
               continue;
            }

            if (problem != null) {
               count++;
               if (omitted != null) {
                  omitted.add(packKey(cell.dx(), cell.dy(), cell.dz()));
               }

               if (issues.size() < exampleLimit) {
                  issues.add(new AiSchematicReadiness.ReadinessIssue(problem.kind(), cell, problem.detail()));
               }
            }
         }
      }

      return count + init(cells, resolved, placed, x0, y0, z0, x1, y1, z1, origin, context, exampleLimit, issues, omitted);
   }

   private static long init(
      List<AiBuilder.CellSpec> cells,
      Map<AiSchematicReadiness.BlockStateKey, BlockState> resolved,
      Long2ObjectOpenHashMap<BlockState> placed,
      int x0,
      int y0,
      int z0,
      int x1,
      int y1,
      int z1,
      BlockPos origin,
      Map<Long, BlockState> context,
      int exampleLimit,
      List<AiSchematicReadiness.ReadinessIssue> issues,
      LongSet omitted
   ) {
      LongOpenHashSet seen = new LongOpenHashSet();
      LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
      LongArrayList component = omitted == null ? null : new LongArrayList();
      long count = 0L;

      for (AiBuilder.CellSpec cell : cells) {
         AiBuilder.checkNotCancelled();
         FluidState fluid = resolved.get(new AiSchematicReadiness.BlockStateKey(cell.block(), cell.properties())).getFluidState();
         long start = packKey(cell.dx(), cell.dy(), cell.dz());
         if (!fluid.isEmpty() && !seen.contains(start)) {
            boolean source = false;
            boolean fedOutside = false;
            boolean unknownOutside = false;
            boolean edge = false;
            if (component != null) {
               component.clear();
            }

            long size = 0L;
            seen.add(start);
            queue.enqueue(start);

            while (!queue.isEmpty()) {
               long at = queue.dequeueLong();
               if (component != null) {
                  component.add(at);
               }

               size++;
               int x = (int)(at >>> 42) - 1048576;
               int y = (int)(at >>> 21 & 2097151L) - 1048576;
               int z = (int)(at & 2097151L) - 1048576;
               FluidState here = placed.get(at).getFluidState();
               if (here.isSource()) {
                  source = true;
               }

               for (int[] side : SIDES) {
                  int nx = x + side[0];
                  int ny = y + side[1];
                  int nz = z + side[2];
                  long next = packKey(nx, ny, nz);
                  BlockState neighbour = placed.get(next);
                  if (neighbour == null) {
                     if (nx < x0 || nx > x1 || ny < y0 || ny > y1 || nz < z0 || nz > z1) {
                        edge = true;
                        BlockState outside = context == null ? null : context.get(origin.offset(nx, ny, nz).asLong());
                        if (outside == null) {
                           unknownOutside = true;
                        } else if (outside.getFluidState().getType().isSame(fluid.getType())) {
                           fedOutside = true;
                        }
                     }
                  } else if (neighbour.getFluidState().getType().isSame(fluid.getType()) && seen.add(next)) {
                     queue.enqueue(next);
                  }
               }
            }

            if (!source && !fedOutside && !unknownOutside) {
               if (omitted != null) {
                  omitted.addAll(component);
               }

               count += size;
               String body = size
                  + (size == 1L ? " cell" : " connected cells")
                  + " of flowing "
                  + BuiltInRegistries.FLUID.getKey(fluid.getType()).getPath().replace("flowing_", "");
               if (issues.size() < exampleLimit) {
                  issues.add(
                     edge
                        ? new AiSchematicReadiness.ReadinessIssue(
                           AiSchematicReadiness.IssueKind.MISSING_CONTEXT, cell, body + " fed from outside the schematic, where the site has none; vanilla drains it"
                        )
                        : new AiSchematicReadiness.ReadinessIssue(AiSchematicReadiness.IssueKind.NO_SOURCE, cell, body + " with no source in the schematic; vanilla drains it")
                  );
               }
            }
         }
      }

      return count;
   }

   static List<BlockPos> sourcelessFluidEdges(AiBuilder.Blueprint blueprint) {
      List<AiBuilder.CellSpec> cells = blueprint.cells();
      HashMap<AiSchematicReadiness.BlockStateKey, BlockState> states = new HashMap<>();
      Long2ObjectOpenHashMap<BlockState> placed = new Long2ObjectOpenHashMap<>();
      int minX = 2147483647;
      int minY = 2147483647;
      int minZ = 2147483647;
      int maxX = -2147483648;
      int maxY = -2147483648;
      int maxZ = -2147483648;

      for (AiBuilder.CellSpec cell : cells) {
         AiBuilder.checkNotCancelled();
         minX = Math.min(minX, cell.dx());
         maxX = Math.max(maxX, cell.dx());
         minY = Math.min(minY, cell.dy());
         maxY = Math.max(maxY, cell.dy());
         minZ = Math.min(minZ, cell.dz());
         maxZ = Math.max(maxZ, cell.dz());
         if (cell.block().equals("minecraft:water") || cell.block().equals("minecraft:lava")) {
            placed.put(
               packKey(cell.dx(), cell.dy(), cell.dz()),
               states.computeIfAbsent(new AiSchematicReadiness.BlockStateKey(cell.block(), cell.properties()), ignored -> AiBuilder.toBlockState(cell))
            );
         }
      }

      LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
      LongOpenHashSet seen = new LongOpenHashSet();
      LongArrayFIFOQueue queue = new LongArrayFIFOQueue();

      for (Entry<BlockState> entry : placed.long2ObjectEntrySet()) {
         if (!seen.contains(entry.getLongKey())) {
            Fluid fluid = entry.getValue().getFluidState().getType();
            boolean source = false;
            ArrayList<BlockPos> edge = new ArrayList<>();
            seen.add(entry.getLongKey());
            queue.enqueue(entry.getLongKey());

            while (!queue.isEmpty()) {
               long at = queue.dequeueLong();
               int x = (int)(at >>> 42) - 1048576;
               int y = (int)(at >>> 21 & 2097151L) - 1048576;
               int z = (int)(at & 2097151L) - 1048576;
               if (placed.get(at).getFluidState().isSource()) {
                  source = true;
               }

               for (int[] side : SIDES) {
                  int nx = x + side[0];
                  int ny = y + side[1];
                  int nz = z + side[2];
                  long next = packKey(nx, ny, nz);
                  BlockState neighbour = placed.get(next);
                  if (neighbour == null) {
                     if (nx < minX || nx > maxX || ny < minY || ny > maxY || nz < minZ || nz > maxZ) {
                        edge.add(blueprint.origin().offset(nx, ny, nz));
                     }
                  } else if (neighbour.getFluidState().getType().isSame(fluid) && seen.add(next)) {
                     queue.enqueue(next);
                  }
               }
            }

            if (!source) {
               out.addAll(edge);
            }
         }
      }

      return List.copyOf(out);
   }

   private static String supportProblem(BlockState state, BlockPos pos, BlockPos origin, Function<BlockPos, BlockState> finished) {
      BlockPos at = pos.below();
      String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
      if (state.hasProperty(BlockStateProperties.ATTACH_FACE)) {
         AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
         BlockPos var10000;
         switch (face) {
            case FLOOR:
               var10000 = pos.below();
               break;
            case CEILING:
               var10000 = pos.above();
               break;
            case WALL:
               var10000 = pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
               break;
            default:
               throw new MatchException(null, null);
         }

         at = var10000;
      } else if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
         || !id.contains("wall_") && !id.equals("ladder") && !id.equals("tripwire_hook") && !id.equals("cocoa")) {
         if (state.hasProperty(BlockStateProperties.HANGING) && state.getValue(BlockStateProperties.HANGING)) {
            at = pos.above();
         }
      } else {
         at = pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
      }

      String support;
      try {
         support = BuiltInRegistries.BLOCK.getKey(finished.apply(at).getBlock()).toString();
      } catch (AiSchematicReadiness.InconclusiveCheck outside) {
         support = "unknown ground";
      }

      BlockPos relative = at.subtract(origin);
      return (at.equals(pos.below()) ? "stands on " : "hangs on ") + support + " at " + relative.getX() + "," + relative.getY() + "," + relative.getZ();
   }

   private static boolean needsSupport(BlockState state) {
      if (state.getBlock() instanceof FallingBlock) {
         return true;
      }

      try {
         return !state.canSurvive(levelReaderOf(pos -> Blocks.AIR.defaultBlockState()), BlockPos.ZERO);
      } catch (RuntimeException unavailable) {
         return true;
      }
   }

   private static LevelReader levelReaderOf(Function<BlockPos, BlockState> states) {
      return (LevelReader)Proxy.newProxyInstance(LevelReader.class.getClassLoader(), new Class[]{LevelReader.class}, (proxy, method, args) -> {
         Object var10000;
         switch (method.getName()) {
            case "getBlockState":
               var10000 = states.apply((BlockPos)args[0]);
               break;
            case "getFluidState":
               var10000 = states.apply((BlockPos)args[0]).getFluidState();
               break;
            case "isEmptyBlock":
               var10000 = states.apply((BlockPos)args[0]).isAir();
               break;
            case "getBlockEntity":
               if (method.getParameterCount() != 1) {
                  throw INCONCLUSIVE;
               }

               var10000 = null;
               break;
            case "getMinY":
               var10000 = -2048;
               break;
            case "getHeight":
               if (args != null && args.length != 0) {
                  throw INCONCLUSIVE;
               }

               var10000 = 4096;
               break;
            case "toString":
               var10000 = "AiSchematicReadiness.finished";
               break;
            case "hashCode":
               var10000 = System.identityHashCode(proxy);
               break;
            case "equals":
               var10000 = proxy == args[0];
               break;
            default:
               if (!method.isDefault()) {
                  throw INCONCLUSIVE;
               }

               var10000 = InvocationHandler.invokeDefault(proxy, method, args);
         }

         return var10000;
      });
   }

   private static Long2ObjectOpenHashMap<AiBuilder.CellSpec> requiredPairCells(
      AiBuilder.Blueprint blueprint, Map<AiSchematicReadiness.BlockStateKey, Optional<AiSchematicReadiness.PairPlan>> plans
   ) {
      Long2ObjectOpenHashMap<AiBuilder.CellSpec> required = new Long2ObjectOpenHashMap<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         AiBuilder.checkNotCancelled();
         AiSchematicReadiness.PairPlan pair = pairPlanFor(cell, plans);
         if (pair != null) {
            BlockPos at = new BlockPos(cell.dx(), cell.dy(), cell.dz()).relative(pair.direction());
            required.put(AiBuilder.packOffsetKey(at.getX(), at.getY(), at.getZ()), null);
         }
      }

      if (!required.isEmpty()) {
         for (AiBuilder.CellSpec cell : blueprint.cells()) {
            AiBuilder.checkNotCancelled();
            long key = AiBuilder.packOffsetKey(cell.dx(), cell.dy(), cell.dz());
            if (required.containsKey(key)) {
               required.put(key, cell);
            }
         }
      }

      return required;
   }

   static List<BlockPos> requiredCounterpartCells(AiBuilder.Blueprint blueprint) {
      HashMap<AiSchematicReadiness.BlockStateKey, Optional<AiSchematicReadiness.PairPlan>> plans = new HashMap<>();
      Long2ObjectOpenHashMap<AiBuilder.CellSpec> partners = requiredPairCells(blueprint, plans);
      LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         AiBuilder.checkNotCancelled();
         AiSchematicReadiness.PairPlan pair = pairPlanFor(cell, plans);
         if (pair != null && pair.needsExisting()) {
            BlockPos at = new BlockPos(cell.dx(), cell.dy(), cell.dz()).relative(pair.direction());
            if (partners.get(AiBuilder.packOffsetKey(at.getX(), at.getY(), at.getZ())) == null) {
               positions.add(blueprint.origin().offset(at));
            }
         }
      }

      return List.copyOf(positions);
   }

   private static AiSchematicReadiness.PairPlan pairPlanFor(AiBuilder.CellSpec cell, Map<AiSchematicReadiness.BlockStateKey, Optional<AiSchematicReadiness.PairPlan>> plans) {
      return plans.computeIfAbsent(
            new AiSchematicReadiness.BlockStateKey(cell.block(), cell.properties()),
            ignored -> {
               BlockState state = AiBuilder.toBlockState(cell);
               if (state.getBlock() instanceof BedBlock) {
                  boolean head = state.getValue(BedBlock.PART) == BedPart.HEAD;
                  return Optional.of(
                     new AiSchematicReadiness.PairPlan(
                        head ? state.getValue(BedBlock.FACING).getOpposite() : state.getValue(BedBlock.FACING), state.cycle(BedBlock.PART), head
                     )
                  );
               }

               if (!(state.getBlock() instanceof DoorBlock) && !(state.getBlock() instanceof DoublePlantBlock)) {
                  return Optional.empty();
               }

               boolean upper = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;
               return Optional.of(new AiSchematicReadiness.PairPlan(upper ? Direction.DOWN : Direction.UP, state.cycle(BlockStateProperties.DOUBLE_BLOCK_HALF), upper));
            }
         )
         .orElse(null);
   }

   private static boolean isPairPartner(BlockState wanted, BlockState other) {
      if (other.getBlock() != wanted.getBlock()) {
         return false;
      } else if (wanted.getBlock() instanceof BedBlock) {
         return other.getValue(BlockStateProperties.BED_PART) == wanted.getValue(BlockStateProperties.BED_PART)
            && other.getValue(BlockStateProperties.HORIZONTAL_FACING) == wanted.getValue(BlockStateProperties.HORIZONTAL_FACING);
      } else {
         return other.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
               && other.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == wanted.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)
            ? !(wanted.getBlock() instanceof DoorBlock)
               || other.getValue(BlockStateProperties.HORIZONTAL_FACING) == wanted.getValue(BlockStateProperties.HORIZONTAL_FACING)
                  && other.getValue(BlockStateProperties.DOOR_HINGE) == wanted.getValue(BlockStateProperties.DOOR_HINGE)
            : false;
      }
   }

   private static List<AiSchematicReadiness.StateProblem> stateProblems(AiBuilder.CellSpec cell) {
      ArrayList<AiSchematicReadiness.StateProblem> problems = new ArrayList<>();

      for (String gap : AiBuilder.unsupportedProperties(cell)) {
         problems.add(new AiSchematicReadiness.StateProblem(AiSchematicReadiness.IssueKind.UNSUPPORTED_STATE, gap));
      }

      if (!isSecondaryCell(cell) && AiSchematicActions.materialPlans(cell).isEmpty()) {
         problems.add(new AiSchematicReadiness.StateProblem(AiSchematicReadiness.IssueKind.NO_ACTION_PLAN, "required work has no placement, material, or verified interaction action"));
      }

      return List.copyOf(problems);
   }

   private static boolean isSecondaryCell(AiBuilder.CellSpec cell) {
      return AiBuilder.isTransientPistonCell(cell)
         || AiBuilder.isFlowingFluidCell(cell)
         || "upper".equals(cell.properties().get("half"))
         || "head".equals(cell.properties().get("part"));
   }

   private record PairPlan(Direction direction, BlockState wanted, boolean needsExisting) {
   }

   enum IssueKind {
      UNSUPPORTED_STATE,
      NO_ACTION_PLAN,
      CONFLICTING_PAIR,
      MISSING_CONTEXT,
      NO_SUPPORT,
      FALLS,
      NO_SOURCE;
   }

   private record StateProblem(AiSchematicReadiness.IssueKind kind, String detail) {
   }

   record BuildableAssessment(AiBuilder.Blueprint blueprint, int skipped, AiSchematicReadiness.ReadinessReport report) {
   }

   private static final class InconclusiveCheck extends RuntimeException {
      InconclusiveCheck() {
         super(null, null, false, false);
      }
   }

   private record BlockStateKey(String block, Map<String, String> properties) {
   }

   record ReadinessIssue(AiSchematicReadiness.IssueKind kind, AiBuilder.CellSpec cell, String detail) {
      @Override
      public String toString() {
         return this.kind
            + " at "
            + this.cell.dx()
            + ","
            + this.cell.dy()
            + ","
            + this.cell.dz()
            + " "
            + this.cell.block()
            + this.cell.properties()
            + ": "
            + this.detail;
      }
   }

   record ReadinessReport(long issueCount, List<AiSchematicReadiness.ReadinessIssue> issues, int distinctStates) {
      ReadinessReport {
         issues = List.copyOf(issues);
      }

      boolean ready() {
         return this.issueCount == 0L;
      }

      boolean truncated() {
         return this.issueCount > this.issues.size();
      }
   }
}
