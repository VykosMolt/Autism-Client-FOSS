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
   private static final AiSchematicReadiness.Inner_JsonUtil init_bX_JsonUtil = new AiSchematicReadiness.Inner_JsonUtil();
   private static final int init_I = 1048576;
   private static final int[][] init_IArr2 = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

   private AiSchematicReadiness() {
   }

   static String init(AiSchematicReadiness.Inner_TaskQueue report) {
      if (!report.ready() && !report.issues().isEmpty()) {
         AiSchematicReadiness.Inner_StreamUtil first = report.issues().getFirst();
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
         return first.kind() != AiSchematicReadiness.Inner_CachePool.UNSUPPORTED_STATE && first.kind() != AiSchematicReadiness.Inner_CachePool.NO_ACTION_PLAN
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

   static List<AiSchematicReadiness.Inner_StreamUtil> init(AiBuilder.Blueprint blueprint) {
      return init(blueprint, 2147483647).issues();
   }

   static AiSchematicReadiness.Inner_TaskQueue init(AiBuilder.Blueprint blueprint, int exampleLimit) {
      return assess(blueprint, exampleLimit, null);
   }

   static AiSchematicReadiness.Inner_TaskQueue assess(AiBuilder.Blueprint blueprint, int exampleLimit, Map<Long, BlockState> context) {
      return init(blueprint, exampleLimit, context, null);
   }

   static AiSchematicReadiness.Inner_DataUtil assessBuildable(AiBuilder.Blueprint requested, int exampleLimit, Map<Long, BlockState> context) {
      AiBuilder.Blueprint blueprint = requested;
      LongOpenHashSet omitted = new LongOpenHashSet();
      ArrayList<AiSchematicReadiness.Inner_StreamUtil> examples = new ArrayList<>();
      long issueCount = 0L;
      int distinctStates = 0;
      Map<Long, BlockState> observed = context;

      ArrayList<AiBuilder.CellSpec> kept;
      do {
         AiBuilder.checkNotCancelled();
         omitted.clear();
         AiSchematicReadiness.Inner_TaskQueue audit = init(blueprint, exampleLimit - examples.size(), observed, omitted);
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
            if (omitted.contains(init(cell.dx(), cell.dy(), cell.dz()))) {
               observed.putIfAbsent(blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()).asLong(), Blocks.AIR.defaultBlockState());
            } else {
               kept.add(cell);
            }
         }

         blueprint = new AiBuilder.Blueprint(blueprint.origin(), List.copyOf(kept));
      } while (!kept.isEmpty());

      return new AiSchematicReadiness.Inner_DataUtil(blueprint, requested.cells().size() - blueprint.cells().size(), new AiSchematicReadiness.Inner_TaskQueue(issueCount, examples, distinctStates));
   }

   private static AiSchematicReadiness.Inner_TaskQueue init(AiBuilder.Blueprint blueprint, int exampleLimit, Map<Long, BlockState> context, LongSet omitted) {
      if (exampleLimit < 0) {
         throw new IllegalArgumentException("negative readiness example limit");
      }

      ArrayList<AiSchematicReadiness.Inner_StreamUtil> issues = new ArrayList<>();
      HashMap<AiSchematicReadiness.Inner_NetHelper, List<AiSchematicReadiness.Inner_ConfigStore>> states = new HashMap<>();
      HashMap<AiSchematicReadiness.Inner_NetHelper, Optional<AiSchematicReadiness.Inner_BufferHelper>> pairPlans = new HashMap<>();
      Long2ObjectOpenHashMap<AiBuilder.CellSpec> partners = init(blueprint, pairPlans);
      long issueCount = 0L;

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         AiBuilder.checkNotCancelled();
         List<AiSchematicReadiness.Inner_ConfigStore> problems = states.computeIfAbsent(new AiSchematicReadiness.Inner_NetHelper(cell.block(), cell.properties()), ignored -> init_List(cell));
         issueCount += problems.size();
         if (omitted != null && !problems.isEmpty()) {
            omitted.add(init(cell.dx(), cell.dy(), cell.dz()));
         }

         for (AiSchematicReadiness.Inner_ConfigStore problem : problems) {
            if (issues.size() >= exampleLimit) {
               break;
            }

            issues.add(new AiSchematicReadiness.Inner_StreamUtil(problem.kind(), cell, problem.detail()));
         }

         AiSchematicReadiness.Inner_BufferHelper pair = init(cell, pairPlans);
         if (pair != null) {
            BlockPos at = new BlockPos(cell.dx(), cell.dy(), cell.dz()).relative(pair.direction());
            AiBuilder.CellSpec partner = partners.get(AiBuilder.packOffsetKey(at.getX(), at.getY(), at.getZ()));
            BlockState actual = partner == null
               ? (context == null ? null : context.get(blueprint.origin().offset(at).asLong()))
               : AiBuilder.toBlockState(partner);
            if ((partner != null || pair.needsExisting() && context != null) && (actual == null || !init(pair.wanted(), actual))) {
               issueCount++;
               if (omitted != null) {
                  omitted.add(init(cell.dx(), cell.dy(), cell.dz()));
               }

               if (issues.size() < exampleLimit) {
                  issues.add(
                     new AiSchematicReadiness.Inner_StreamUtil(
                        partner == null ? AiSchematicReadiness.Inner_CachePool.MISSING_CONTEXT : AiSchematicReadiness.Inner_CachePool.CONFLICTING_PAIR,
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
      return new AiSchematicReadiness.Inner_TaskQueue(issueCount, issues, states.size());
   }

   private static long init(int x, int y, int z) {
      return (long)(x + 1048576) << 42 | (long)(y + 1048576) << 21 | z + 1048576;
   }

   private static long init(
      AiBuilder.Blueprint blueprint,
      Map<Long, BlockState> context,
      Map<AiSchematicReadiness.Inner_NetHelper, Optional<AiSchematicReadiness.Inner_BufferHelper>> pairPlans,
      int exampleLimit,
      List<AiSchematicReadiness.Inner_StreamUtil> issues,
      LongSet omitted
   ) {
      List<AiBuilder.CellSpec> cells = blueprint.cells();
      if (cells.isEmpty()) {
         return 0L;
      }

      HashMap<AiSchematicReadiness.Inner_NetHelper, BlockState> resolved = new HashMap<>();
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
         BlockState state = resolved.computeIfAbsent(new AiSchematicReadiness.Inner_NetHelper(cell.block(), cell.properties()), ignored -> AiBuilder.toBlockState(cell));
         placed.put(init(cell.dx(), cell.dy(), cell.dz()), state);
         minX = Math.min(minX, cell.dx());
         maxX = Math.max(maxX, cell.dx());
         minY = Math.min(minY, cell.dy());
         maxY = Math.max(maxY, cell.dy());
         minZ = Math.min(minZ, cell.dz());
         maxZ = Math.max(maxZ, cell.dz());
         if (needs.computeIfAbsent(state, AiSchematicReadiness::init)) {
            checked.add(cell);
         }
      }

      for (AiBuilder.CellSpec cell : cells) {
         BlockState state = resolved.get(new AiSchematicReadiness.Inner_NetHelper(cell.block(), cell.properties()));
         if (state.getBlock() instanceof PistonBaseBlock && state.getValue(PistonBaseBlock.EXTENDED)) {
            Direction facing = state.getValue(PistonBaseBlock.FACING);
            long head = init(cell.dx() + facing.getStepX(), cell.dy() + facing.getStepY(), cell.dz() + facing.getStepZ());
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
         BlockState statex = placed.get(init(x, y, z));
         if (statex != null) {
            return statex;
         } else if (x >= x0 && x <= x1 && y >= y0 && y <= y1 && z >= z0 && z <= z1) {
            return Blocks.AIR.defaultBlockState();
         } else {
            BlockState known = context == null ? null : context.get(posx.asLong());
            if (known == null) {
               throw init_bX_JsonUtil;
            } else {
               return known;
            }
         }
      };
      LevelReader reader = init(finished);
      long count = 0L;

      for (AiBuilder.CellSpec cell : checked) {
         AiBuilder.checkNotCancelled();
         AiSchematicReadiness.Inner_BufferHelper plan = init(cell, pairPlans);
         if (plan == null || !plan.needsExisting()) {
            BlockState state = resolved.get(new AiSchematicReadiness.Inner_NetHelper(cell.block(), cell.properties()));
            BlockPos pos = origin.offset(cell.dx(), cell.dy(), cell.dz());
            AiSchematicReadiness.Inner_ConfigStore problem = null;

            try {
               if (state.getBlock() instanceof FallingBlock) {
                  BlockState below = finished.apply(pos.below());
                  if (FallingBlock.isFree(below)) {
                     problem = new AiSchematicReadiness.Inner_ConfigStore(
                        AiSchematicReadiness.Inner_CachePool.FALLS, "rests on " + BuiltInRegistries.BLOCK.getKey(below.getBlock()) + " in the finished schematic, so it falls"
                     );
                  }
               } else if (!state.canSurvive(reader, pos)) {
                  problem = new AiSchematicReadiness.Inner_ConfigStore(
                     AiSchematicReadiness.Inner_CachePool.NO_SUPPORT, init(state, pos, origin, finished) + " in the finished schematic, which cannot hold it; vanilla breaks it off"
                  );
               }
            } catch (AiSchematicReadiness.Inner_JsonUtil inconclusive) {
               continue;
            } catch (RuntimeException unavailable) {
               continue;
            }

            if (problem != null) {
               count++;
               if (omitted != null) {
                  omitted.add(init(cell.dx(), cell.dy(), cell.dz()));
               }

               if (issues.size() < exampleLimit) {
                  issues.add(new AiSchematicReadiness.Inner_StreamUtil(problem.kind(), cell, problem.detail()));
               }
            }
         }
      }

      return count + init(cells, resolved, placed, x0, y0, z0, x1, y1, z1, origin, context, exampleLimit, issues, omitted);
   }

   private static long init(
      List<AiBuilder.CellSpec> cells,
      Map<AiSchematicReadiness.Inner_NetHelper, BlockState> resolved,
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
      List<AiSchematicReadiness.Inner_StreamUtil> issues,
      LongSet omitted
   ) {
      LongOpenHashSet seen = new LongOpenHashSet();
      LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
      LongArrayList component = omitted == null ? null : new LongArrayList();
      long count = 0L;

      for (AiBuilder.CellSpec cell : cells) {
         AiBuilder.checkNotCancelled();
         FluidState fluid = resolved.get(new AiSchematicReadiness.Inner_NetHelper(cell.block(), cell.properties())).getFluidState();
         long start = init(cell.dx(), cell.dy(), cell.dz());
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

               for (int[] side : init_IArr2) {
                  int nx = x + side[0];
                  int ny = y + side[1];
                  int nz = z + side[2];
                  long next = init(nx, ny, nz);
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
                        ? new AiSchematicReadiness.Inner_StreamUtil(
                           AiSchematicReadiness.Inner_CachePool.MISSING_CONTEXT, cell, body + " fed from outside the schematic, where the site has none; vanilla drains it"
                        )
                        : new AiSchematicReadiness.Inner_StreamUtil(AiSchematicReadiness.Inner_CachePool.NO_SOURCE, cell, body + " with no source in the schematic; vanilla drains it")
                  );
               }
            }
         }
      }

      return count;
   }

   static List<BlockPos> load(AiBuilder.Blueprint blueprint) {
      List<AiBuilder.CellSpec> cells = blueprint.cells();
      HashMap<AiSchematicReadiness.Inner_NetHelper, BlockState> states = new HashMap<>();
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
               init(cell.dx(), cell.dy(), cell.dz()),
               states.computeIfAbsent(new AiSchematicReadiness.Inner_NetHelper(cell.block(), cell.properties()), ignored -> AiBuilder.toBlockState(cell))
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

               for (int[] side : init_IArr2) {
                  int nx = x + side[0];
                  int ny = y + side[1];
                  int nz = z + side[2];
                  long next = init(nx, ny, nz);
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

   private static String init(BlockState state, BlockPos pos, BlockPos origin, Function<BlockPos, BlockState> finished) {
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
      } catch (AiSchematicReadiness.Inner_JsonUtil outside) {
         support = "unknown ground";
      }

      BlockPos relative = at.subtract(origin);
      return (at.equals(pos.below()) ? "stands on " : "hangs on ") + support + " at " + relative.getX() + "," + relative.getY() + "," + relative.getZ();
   }

   private static boolean init(BlockState state) {
      if (state.getBlock() instanceof FallingBlock) {
         return true;
      }

      try {
         return !state.canSurvive(init(pos -> Blocks.AIR.defaultBlockState()), BlockPos.ZERO);
      } catch (RuntimeException unavailable) {
         return true;
      }
   }

   private static LevelReader init(Function<BlockPos, BlockState> states) {
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
                  throw init_bX_JsonUtil;
               }

               var10000 = null;
               break;
            case "getMinY":
               var10000 = -2048;
               break;
            case "getHeight":
               if (args != null && args.length != 0) {
                  throw init_bX_JsonUtil;
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
                  throw init_bX_JsonUtil;
               }

               var10000 = InvocationHandler.invokeDefault(proxy, method, args);
         }

         return var10000;
      });
   }

   private static Long2ObjectOpenHashMap<AiBuilder.CellSpec> init(
      AiBuilder.Blueprint blueprint, Map<AiSchematicReadiness.Inner_NetHelper, Optional<AiSchematicReadiness.Inner_BufferHelper>> plans
   ) {
      Long2ObjectOpenHashMap<AiBuilder.CellSpec> required = new Long2ObjectOpenHashMap<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         AiBuilder.checkNotCancelled();
         AiSchematicReadiness.Inner_BufferHelper pair = init(cell, plans);
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

   static List<BlockPos> read(AiBuilder.Blueprint blueprint) {
      HashMap<AiSchematicReadiness.Inner_NetHelper, Optional<AiSchematicReadiness.Inner_BufferHelper>> plans = new HashMap<>();
      Long2ObjectOpenHashMap<AiBuilder.CellSpec> partners = init(blueprint, plans);
      LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         AiBuilder.checkNotCancelled();
         AiSchematicReadiness.Inner_BufferHelper pair = init(cell, plans);
         if (pair != null && pair.needsExisting()) {
            BlockPos at = new BlockPos(cell.dx(), cell.dy(), cell.dz()).relative(pair.direction());
            if (partners.get(AiBuilder.packOffsetKey(at.getX(), at.getY(), at.getZ())) == null) {
               positions.add(blueprint.origin().offset(at));
            }
         }
      }

      return List.copyOf(positions);
   }

   private static AiSchematicReadiness.Inner_BufferHelper init(AiBuilder.CellSpec cell, Map<AiSchematicReadiness.Inner_NetHelper, Optional<AiSchematicReadiness.Inner_BufferHelper>> plans) {
      return plans.computeIfAbsent(
            new AiSchematicReadiness.Inner_NetHelper(cell.block(), cell.properties()),
            ignored -> {
               BlockState state = AiBuilder.toBlockState(cell);
               if (state.getBlock() instanceof BedBlock) {
                  boolean head = state.getValue(BedBlock.PART) == BedPart.HEAD;
                  return Optional.of(
                     new AiSchematicReadiness.Inner_BufferHelper(
                        head ? state.getValue(BedBlock.FACING).getOpposite() : state.getValue(BedBlock.FACING), state.cycle(BedBlock.PART), head
                     )
                  );
               }

               if (!(state.getBlock() instanceof DoorBlock) && !(state.getBlock() instanceof DoublePlantBlock)) {
                  return Optional.empty();
               }

               boolean upper = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;
               return Optional.of(new AiSchematicReadiness.Inner_BufferHelper(upper ? Direction.DOWN : Direction.UP, state.cycle(BlockStateProperties.DOUBLE_BLOCK_HALF), upper));
            }
         )
         .orElse(null);
   }

   private static boolean init(BlockState wanted, BlockState other) {
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

   private static List<AiSchematicReadiness.Inner_ConfigStore> init_List(AiBuilder.CellSpec cell) {
      ArrayList<AiSchematicReadiness.Inner_ConfigStore> problems = new ArrayList<>();

      for (String gap : AiBuilder.unsupportedProperties(cell)) {
         problems.add(new AiSchematicReadiness.Inner_ConfigStore(AiSchematicReadiness.Inner_CachePool.UNSUPPORTED_STATE, gap));
      }

      if (!init_Z(cell) && AiSchematicActions.materialPlans(cell).isEmpty()) {
         problems.add(new AiSchematicReadiness.Inner_ConfigStore(AiSchematicReadiness.Inner_CachePool.NO_ACTION_PLAN, "required work has no placement, material, or verified interaction action"));
      }

      return List.copyOf(problems);
   }

   private static boolean init_Z(AiBuilder.CellSpec cell) {
      return AiBuilder.isTransientPistonCell(cell)
         || AiBuilder.isFlowingFluidCell(cell)
         || "upper".equals(cell.properties().get("half"))
         || "head".equals(cell.properties().get("part"));
   }

   private record Inner_BufferHelper(Direction direction, BlockState wanted, boolean needsExisting) {
   }

   enum Inner_CachePool {
      UNSUPPORTED_STATE,
      NO_ACTION_PLAN,
      CONFLICTING_PAIR,
      MISSING_CONTEXT,
      NO_SUPPORT,
      FALLS,
      NO_SOURCE;
   }

   private record Inner_ConfigStore(AiSchematicReadiness.Inner_CachePool kind, String detail) {
   }

   record Inner_DataUtil(AiBuilder.Blueprint blueprint, int skipped, AiSchematicReadiness.Inner_TaskQueue report) {
   }

   private static final class Inner_JsonUtil extends RuntimeException {
      Inner_JsonUtil() {
         super(null, null, false, false);
      }
   }

   private record Inner_NetHelper(String block, Map<String, String> properties) {
   }

   record Inner_StreamUtil(AiSchematicReadiness.Inner_CachePool kind, AiBuilder.CellSpec cell, String detail) {
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

   record Inner_TaskQueue(long issueCount, List<AiSchematicReadiness.Inner_StreamUtil> issues, int distinctStates) {
      Inner_TaskQueue {
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
