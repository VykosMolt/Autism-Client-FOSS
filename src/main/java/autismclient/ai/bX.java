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

final class bX {
   private static final bX.Inner_JsonUtil init_bX_JsonUtil = new bX.Inner_JsonUtil();
   private static final int init_I = 1048576;
   private static final int[][] init_IArr2 = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

   private bX() {
   }

   static String init(bX.Inner_TaskQueue report) {
      if (!report.ready() && !report.issues().isEmpty()) {
         bX.Inner_StreamUtil first = report.issues().getFirst();
         TableIndex.Inner_StreamUtil cell = first.cell();
         String block = cell.block().startsWith("minecraft:") ? cell.block().substring("minecraft:".length()) : cell.block();
         String var10000;
         switch (first.kind()) {
            case init:
            case load:
               var10000 = first.detail();
               break;
            case read:
               var10000 = "its other half is a different saved block";
               break;
            case write:
               var10000 = "it needs blocks outside the schematic";
               break;
            case update:
               var10000 = first.detail().replace(" in the finished schematic, which cannot hold it; vanilla breaks it off", "") + ", which cannot hold it";
               break;
            case sync:
               var10000 = "nothing holds it up, so it falls";
               break;
            case flush:
               var10000 = "flowing with no source, so it drains";
               break;
            default:
               throw new MatchException(null, null);
         }

         String why = var10000;
         return first.kind() != bX.Inner_CachePool.init && first.kind() != bX.Inner_CachePool.load
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

   static List<bX.Inner_StreamUtil> init(TableIndex.Inner_DataUtil blueprint) {
      return init(blueprint, 2147483647).issues();
   }

   static bX.Inner_TaskQueue init(TableIndex.Inner_DataUtil blueprint, int exampleLimit) {
      return init_bX_TaskQueue(blueprint, exampleLimit, null);
   }

   static bX.Inner_TaskQueue init_bX_TaskQueue(TableIndex.Inner_DataUtil blueprint, int exampleLimit, Map<Long, BlockState> context) {
      return init(blueprint, exampleLimit, context, null);
   }

   static bX.Inner_DataUtil init_bX_DataUtil(TableIndex.Inner_DataUtil requested, int exampleLimit, Map<Long, BlockState> context) {
      TableIndex.Inner_DataUtil blueprint = requested;
      LongOpenHashSet omitted = new LongOpenHashSet();
      ArrayList<bX.Inner_StreamUtil> examples = new ArrayList<>();
      long issueCount = 0L;
      int distinctStates = 0;
      Map<Long, BlockState> observed = context;

      ArrayList<TableIndex.Inner_StreamUtil> kept;
      do {
         TableIndex.init();
         omitted.clear();
         bX.Inner_TaskQueue audit = init(blueprint, exampleLimit - examples.size(), observed, omitted);
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

         for (TableIndex.Inner_StreamUtil cell : blueprint.cells()) {
            TableIndex.init();
            if (omitted.contains(init(cell.dx(), cell.dy(), cell.dz()))) {
               observed.putIfAbsent(blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()).asLong(), Blocks.AIR.defaultBlockState());
            } else {
               kept.add(cell);
            }
         }

         blueprint = new TableIndex.Inner_DataUtil(blueprint.origin(), List.copyOf(kept));
      } while (!kept.isEmpty());

      return new bX.Inner_DataUtil(blueprint, requested.cells().size() - blueprint.cells().size(), new bX.Inner_TaskQueue(issueCount, examples, distinctStates));
   }

   private static bX.Inner_TaskQueue init(TableIndex.Inner_DataUtil blueprint, int exampleLimit, Map<Long, BlockState> context, LongSet omitted) {
      if (exampleLimit < 0) {
         throw new IllegalArgumentException("negative readiness example limit");
      }

      ArrayList<bX.Inner_StreamUtil> issues = new ArrayList<>();
      HashMap<bX.Inner_NetHelper, List<bX.Inner_ConfigStore>> states = new HashMap<>();
      HashMap<bX.Inner_NetHelper, Optional<bX.Inner_BufferHelper>> pairPlans = new HashMap<>();
      Long2ObjectOpenHashMap<TableIndex.Inner_StreamUtil> partners = init(blueprint, pairPlans);
      long issueCount = 0L;

      for (TableIndex.Inner_StreamUtil cell : blueprint.cells()) {
         TableIndex.init();
         List<bX.Inner_ConfigStore> problems = states.computeIfAbsent(new bX.Inner_NetHelper(cell.block(), cell.properties()), ignored -> init_List(cell));
         issueCount += problems.size();
         if (omitted != null && !problems.isEmpty()) {
            omitted.add(init(cell.dx(), cell.dy(), cell.dz()));
         }

         for (bX.Inner_ConfigStore problem : problems) {
            if (issues.size() >= exampleLimit) {
               break;
            }

            issues.add(new bX.Inner_StreamUtil(problem.kind(), cell, problem.detail()));
         }

         bX.Inner_BufferHelper pair = init(cell, pairPlans);
         if (pair != null) {
            BlockPos at = new BlockPos(cell.dx(), cell.dy(), cell.dz()).relative(pair.direction());
            TableIndex.Inner_StreamUtil partner = partners.get(TableIndex.init_J_I(at.getX(), at.getY(), at.getZ()));
            BlockState actual = partner == null
               ? (context == null ? null : context.get(blueprint.origin().offset(at).asLong()))
               : TableIndex.init_BlockState(partner);
            if ((partner != null || pair.needsExisting() && context != null) && (actual == null || !init(pair.wanted(), actual))) {
               issueCount++;
               if (omitted != null) {
                  omitted.add(init(cell.dx(), cell.dy(), cell.dz()));
               }

               if (issues.size() < exampleLimit) {
                  issues.add(
                     new bX.Inner_StreamUtil(
                        partner == null ? bX.Inner_CachePool.write : bX.Inner_CachePool.read,
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
      return new bX.Inner_TaskQueue(issueCount, issues, states.size());
   }

   private static long init(int x, int y, int z) {
      return (long)(x + 1048576) << 42 | (long)(y + 1048576) << 21 | z + 1048576;
   }

   private static long init(
      TableIndex.Inner_DataUtil blueprint,
      Map<Long, BlockState> context,
      Map<bX.Inner_NetHelper, Optional<bX.Inner_BufferHelper>> pairPlans,
      int exampleLimit,
      List<bX.Inner_StreamUtil> issues,
      LongSet omitted
   ) {
      List<TableIndex.Inner_StreamUtil> cells = blueprint.cells();
      if (cells.isEmpty()) {
         return 0L;
      }

      HashMap<bX.Inner_NetHelper, BlockState> resolved = new HashMap<>();
      Long2ObjectOpenHashMap<BlockState> placed = new Long2ObjectOpenHashMap<>(cells.size());
      int minX = 2147483647;
      int minY = 2147483647;
      int minZ = 2147483647;
      int maxX = -2147483648;
      int maxY = -2147483648;
      int maxZ = -2147483648;
      HashMap<BlockState, Boolean> needs = new HashMap<>();
      ArrayList<TableIndex.Inner_StreamUtil> checked = new ArrayList<>();

      for (TableIndex.Inner_StreamUtil cell : cells) {
         TableIndex.init();
         BlockState state = resolved.computeIfAbsent(new bX.Inner_NetHelper(cell.block(), cell.properties()), ignored -> TableIndex.init_BlockState(cell));
         placed.put(init(cell.dx(), cell.dy(), cell.dz()), state);
         minX = Math.min(minX, cell.dx());
         maxX = Math.max(maxX, cell.dx());
         minY = Math.min(minY, cell.dy());
         maxY = Math.max(maxY, cell.dy());
         minZ = Math.min(minZ, cell.dz());
         maxZ = Math.max(maxZ, cell.dz());
         if (needs.computeIfAbsent(state, bX::init)) {
            checked.add(cell);
         }
      }

      for (TableIndex.Inner_StreamUtil cell : cells) {
         BlockState state = resolved.get(new bX.Inner_NetHelper(cell.block(), cell.properties()));
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

      for (TableIndex.Inner_StreamUtil cell : checked) {
         TableIndex.init();
         bX.Inner_BufferHelper plan = init(cell, pairPlans);
         if (plan == null || !plan.needsExisting()) {
            BlockState state = resolved.get(new bX.Inner_NetHelper(cell.block(), cell.properties()));
            BlockPos pos = origin.offset(cell.dx(), cell.dy(), cell.dz());
            bX.Inner_ConfigStore problem = null;

            try {
               if (state.getBlock() instanceof FallingBlock) {
                  BlockState below = finished.apply(pos.below());
                  if (FallingBlock.isFree(below)) {
                     problem = new bX.Inner_ConfigStore(
                        bX.Inner_CachePool.sync, "rests on " + BuiltInRegistries.BLOCK.getKey(below.getBlock()) + " in the finished schematic, so it falls"
                     );
                  }
               } else if (!state.canSurvive(reader, pos)) {
                  problem = new bX.Inner_ConfigStore(
                     bX.Inner_CachePool.update, init(state, pos, origin, finished) + " in the finished schematic, which cannot hold it; vanilla breaks it off"
                  );
               }
            } catch (bX.Inner_JsonUtil inconclusive) {
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
                  issues.add(new bX.Inner_StreamUtil(problem.kind(), cell, problem.detail()));
               }
            }
         }
      }

      return count + init(cells, resolved, placed, x0, y0, z0, x1, y1, z1, origin, context, exampleLimit, issues, omitted);
   }

   private static long init(
      List<TableIndex.Inner_StreamUtil> cells,
      Map<bX.Inner_NetHelper, BlockState> resolved,
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
      List<bX.Inner_StreamUtil> issues,
      LongSet omitted
   ) {
      LongOpenHashSet seen = new LongOpenHashSet();
      LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
      LongArrayList component = omitted == null ? null : new LongArrayList();
      long count = 0L;

      for (TableIndex.Inner_StreamUtil cell : cells) {
         TableIndex.init();
         FluidState fluid = resolved.get(new bX.Inner_NetHelper(cell.block(), cell.properties())).getFluidState();
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
                        ? new bX.Inner_StreamUtil(
                           bX.Inner_CachePool.write, cell, body + " fed from outside the schematic, where the site has none; vanilla drains it"
                        )
                        : new bX.Inner_StreamUtil(bX.Inner_CachePool.flush, cell, body + " with no source in the schematic; vanilla drains it")
                  );
               }
            }
         }
      }

      return count;
   }

   static List<BlockPos> load(TableIndex.Inner_DataUtil blueprint) {
      List<TableIndex.Inner_StreamUtil> cells = blueprint.cells();
      HashMap<bX.Inner_NetHelper, BlockState> states = new HashMap<>();
      Long2ObjectOpenHashMap<BlockState> placed = new Long2ObjectOpenHashMap<>();
      int minX = 2147483647;
      int minY = 2147483647;
      int minZ = 2147483647;
      int maxX = -2147483648;
      int maxY = -2147483648;
      int maxZ = -2147483648;

      for (TableIndex.Inner_StreamUtil cell : cells) {
         TableIndex.init();
         minX = Math.min(minX, cell.dx());
         maxX = Math.max(maxX, cell.dx());
         minY = Math.min(minY, cell.dy());
         maxY = Math.max(maxY, cell.dy());
         minZ = Math.min(minZ, cell.dz());
         maxZ = Math.max(maxZ, cell.dz());
         if (cell.block().equals("minecraft:water") || cell.block().equals("minecraft:lava")) {
            placed.put(
               init(cell.dx(), cell.dy(), cell.dz()),
               states.computeIfAbsent(new bX.Inner_NetHelper(cell.block(), cell.properties()), ignored -> TableIndex.init_BlockState(cell))
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
      } catch (bX.Inner_JsonUtil outside) {
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

   private static Long2ObjectOpenHashMap<TableIndex.Inner_StreamUtil> init(
      TableIndex.Inner_DataUtil blueprint, Map<bX.Inner_NetHelper, Optional<bX.Inner_BufferHelper>> plans
   ) {
      Long2ObjectOpenHashMap<TableIndex.Inner_StreamUtil> required = new Long2ObjectOpenHashMap<>();

      for (TableIndex.Inner_StreamUtil cell : blueprint.cells()) {
         TableIndex.init();
         bX.Inner_BufferHelper pair = init(cell, plans);
         if (pair != null) {
            BlockPos at = new BlockPos(cell.dx(), cell.dy(), cell.dz()).relative(pair.direction());
            required.put(TableIndex.init_J_I(at.getX(), at.getY(), at.getZ()), null);
         }
      }

      if (!required.isEmpty()) {
         for (TableIndex.Inner_StreamUtil cell : blueprint.cells()) {
            TableIndex.init();
            long key = TableIndex.init_J_I(cell.dx(), cell.dy(), cell.dz());
            if (required.containsKey(key)) {
               required.put(key, cell);
            }
         }
      }

      return required;
   }

   static List<BlockPos> read(TableIndex.Inner_DataUtil blueprint) {
      HashMap<bX.Inner_NetHelper, Optional<bX.Inner_BufferHelper>> plans = new HashMap<>();
      Long2ObjectOpenHashMap<TableIndex.Inner_StreamUtil> partners = init(blueprint, plans);
      LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();

      for (TableIndex.Inner_StreamUtil cell : blueprint.cells()) {
         TableIndex.init();
         bX.Inner_BufferHelper pair = init(cell, plans);
         if (pair != null && pair.needsExisting()) {
            BlockPos at = new BlockPos(cell.dx(), cell.dy(), cell.dz()).relative(pair.direction());
            if (partners.get(TableIndex.init_J_I(at.getX(), at.getY(), at.getZ())) == null) {
               positions.add(blueprint.origin().offset(at));
            }
         }
      }

      return List.copyOf(positions);
   }

   private static bX.Inner_BufferHelper init(TableIndex.Inner_StreamUtil cell, Map<bX.Inner_NetHelper, Optional<bX.Inner_BufferHelper>> plans) {
      return plans.computeIfAbsent(
            new bX.Inner_NetHelper(cell.block(), cell.properties()),
            ignored -> {
               BlockState state = TableIndex.init_BlockState(cell);
               if (state.getBlock() instanceof BedBlock) {
                  boolean head = state.getValue(BedBlock.PART) == BedPart.HEAD;
                  return Optional.of(
                     new bX.Inner_BufferHelper(
                        head ? state.getValue(BedBlock.FACING).getOpposite() : state.getValue(BedBlock.FACING), state.cycle(BedBlock.PART), head
                     )
                  );
               }

               if (!(state.getBlock() instanceof DoorBlock) && !(state.getBlock() instanceof DoublePlantBlock)) {
                  return Optional.empty();
               }

               boolean upper = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;
               return Optional.of(new bX.Inner_BufferHelper(upper ? Direction.DOWN : Direction.UP, state.cycle(BlockStateProperties.DOUBLE_BLOCK_HALF), upper));
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

   private static List<bX.Inner_ConfigStore> init_List(TableIndex.Inner_StreamUtil cell) {
      ArrayList<bX.Inner_ConfigStore> problems = new ArrayList<>();

      for (String gap : TableIndex.init_List(cell)) {
         problems.add(new bX.Inner_ConfigStore(bX.Inner_CachePool.init, gap));
      }

      if (!init_Z(cell) && bQ.load_List(cell).isEmpty()) {
         problems.add(new bX.Inner_ConfigStore(bX.Inner_CachePool.load, "required work has no placement, material, or verified interaction action"));
      }

      return List.copyOf(problems);
   }

   private static boolean init_Z(TableIndex.Inner_StreamUtil cell) {
      return TableIndex.read_Z(cell)
         || TableIndex.load_Z(cell)
         || "upper".equals(cell.properties().get("half"))
         || "head".equals(cell.properties().get("part"));
   }

   private record Inner_BufferHelper(Direction direction, BlockState wanted, boolean needsExisting) {
   }

   enum Inner_CachePool {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush;
   }

   private record Inner_ConfigStore(bX.Inner_CachePool kind, String detail) {
   }

   record Inner_DataUtil(TableIndex.Inner_DataUtil blueprint, int skipped, bX.Inner_TaskQueue report) {
   }

   private static final class Inner_JsonUtil extends RuntimeException {
      Inner_JsonUtil() {
         super(null, null, false, false);
      }
   }

   private record Inner_NetHelper(String block, Map<String, String> properties) {
   }

   record Inner_StreamUtil(bX.Inner_CachePool kind, TableIndex.Inner_StreamUtil cell, String detail) {
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

   record Inner_TaskQueue(long issueCount, List<bX.Inner_StreamUtil> issues, int distinctStates) {
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
