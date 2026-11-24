package autismclient.ai.a;

import autismclient.AutismClientAddon;
import autismclient.api.ai.AiOperation;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.FluidState;

public final class h {
   private static final int[][] init_IArr2 = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
   private static final int init_I = 8;

   private h() {
   }

   public static List<BlockPos> init_List_BlockPos(BlockPos start, BlockPos target, int maxSteps, Predicate<BlockPos> blocked) {
      return init_List_BlockPos(start, target, maxSteps, blocked, cell -> false);
   }

   public static List<BlockPos> init_List_BlockPos(BlockPos start, BlockPos target, int maxSteps, Predicate<BlockPos> blocked, Predicate<BlockPos> noFloor) {
      List<BlockPos> out = new ArrayList<>();
      Set<Long> floors = new HashSet<>();
      floors.add(start.below().asLong());
      int x = start.getX();
      int y = start.getY();
      int z = start.getZ();
      int[] last = null;
      boolean alongX = Math.abs(target.getX() - x) >= Math.abs(target.getZ() - z);
      boolean spiral = false;
      int n = Math.min(maxSteps, y - (target.getY() + 1));

      for (int i = 0; i < n; i++) {
         int rx = target.getX() - x;
         int rz = target.getZ() - z;
         List<int[]> options = new ArrayList<>();
         if (rx == 0 && rz == 0) {
            spiral = true;
         }

         if (spiral) {
            int k = last == null ? 0 : init(last);

            for (int t = 1; t <= 4; t++) {
               options.add(init_IArr2[(k + t) % 4]);
            }
         } else {
            if (alongX && rx == 0) {
               alongX = false;
            }

            if (!alongX && rz == 0) {
               alongX = true;
            }

            options.add(alongX ? new int[]{Integer.signum(rx), 0} : new int[]{0, Integer.signum(rz)});
            if (alongX && rz != 0) {
               options.add(new int[]{0, Integer.signum(rz)});
            }

            if (!alongX && rx != 0) {
               options.add(new int[]{Integer.signum(rx), 0});
            }

            for (int[] d : init_IArr2) {
               options.add(d);
            }
         }

         int[] chosen = null;
         int bestRun = -1;
         int horizon = Math.min(8, n - i);

         for (int[] d : options) {
            if (last == null || d[0] != -last[0] || d[1] != -last[1]) {
               BlockPos feet = new BlockPos(x + d[0], y - 1, z + d[1]);
               if (init_Z_BlockPos_2(feet, floors, blocked) && !noFloor.test(feet.below())) {
                  floors.add(feet.below().asLong());
                  int run = 1 + init_I_BlockPos(feet, d, horizon - 1, floors, blocked, noFloor);
                  floors.remove(feet.below().asLong());
                  if (run > bestRun) {
                     chosen = d;
                     bestRun = run;
                  }

                  if (run == horizon) {
                     break;
                  }
               }
            }
         }

         if (chosen == null) {
            break;
         }

         x += chosen[0];
         z += chosen[1];
         BlockPos feet = new BlockPos(x, --y, z);
         out.add(feet);
         floors.add(feet.below().asLong());
         last = chosen;
      }

      return out;
   }

   private static int init_I_BlockPos(BlockPos from, int[] last, int left, Set<Long> floors, Predicate<BlockPos> blocked, Predicate<BlockPos> noFloor) {
      if (left == 0) {
         return 0;
      }

      int best = 0;

      for (int[] d : init_IArr2) {
         if (d[0] != -last[0] || d[1] != -last[1]) {
            BlockPos next = from.offset(d[0], -1, d[1]);
            if (init_Z_BlockPos_2(next, floors, blocked) && !noFloor.test(next.below())) {
               floors.add(next.below().asLong());
               int run = 1 + init_I_BlockPos(next, d, left - 1, floors, blocked, noFloor);
               floors.remove(next.below().asLong());
               best = Math.max(best, run);
               if (best == left) {
                  break;
               }
            }
         }
      }

      return best;
   }

   private static int init(int[] d) {
      for (int i = 0; i < 4; i++) {
         if (init_IArr2[i][0] == d[0] && init_IArr2[i][1] == d[1]) {
            return i;
         }
      }

      return 0;
   }

   public static List<BlockPos> load(BlockPos start, BlockPos target, int maxSteps, Predicate<BlockPos> blocked, Predicate<BlockPos> noFloor) {
      List<BlockPos> out = new ArrayList<>();
      BlockPos at = start;

      for (int i = 0; i < maxSteps; i++) {
         int dx = target.getX() - at.getX();
         int dz = target.getZ() - at.getZ();
         if (dx == 0 && dz == 0) {
            break;
         }

         int[][] directions = Math.abs(dx) >= Math.abs(dz)
            ? new int[][]{{Integer.signum(dx), 0}, {0, Integer.signum(dz)}}
            : new int[][]{{0, Integer.signum(dz)}, {Integer.signum(dx), 0}};
         BlockPos chosen = null;

         for (int[] d : directions) {
            if (d[0] != 0 || d[1] != 0) {
               BlockPos feet = at.offset(d[0], 0, d[1]);
               if (!noFloor.test(feet.below()) && init_Z_BlockPos_2(feet, Set.of(), blocked)) {
                  chosen = feet;
                  break;
               }
            }
         }

         if (chosen == null) {
            break;
         }

         out.add(chosen);
         at = chosen;
      }

      return out;
   }

   public static List<BlockPos> read(BlockPos start, BlockPos target, int maxSteps, Predicate<BlockPos> blocked, Predicate<BlockPos> noFloor) {
      List<BlockPos> down = init_List_BlockPos(start, target, maxSteps, blocked, noFloor);
      if (!down.isEmpty()) {
         return down;
      }

      List<BlockPos> headings = new ArrayList<>();
      headings.add(target);

      for (int[] d : init_IArr2) {
         headings.add(start.offset(d[0] * 8, 0, d[1] * 8));
      }

      for (BlockPos heading : headings) {
         List<BlockPos> level = load(start, heading, Math.min(8, maxSteps - 1), blocked, noFloor);
         Set<Long> floors = new HashSet<>();
         floors.add(start.below().asLong());

         for (int i = 0; i < level.size(); i++) {
            BlockPos at = level.get(i);
            floors.add(at.below().asLong());
            List<BlockPos> continuation = init_List_BlockPos(
               at, target, maxSteps - i - 1, cell -> floors.contains(cell.asLong()) || blocked.test(cell), noFloor
            );
            if (!continuation.isEmpty()) {
               List<BlockPos> route = new ArrayList<>(level.subList(0, i + 1));
               route.addAll(continuation);
               return route;
            }
         }
      }

      return load(start, target, Math.min(8, maxSteps), blocked, noFloor);
   }

   private static boolean init_Z_BlockPos_2(BlockPos feet, Set<Long> floors, Predicate<BlockPos> blocked) {
      for (int h = 0; h < 3; h++) {
         BlockPos cell = feet.above(h);
         if (floors.contains(cell.asLong()) || blocked.test(cell)) {
            return false;
         }
      }

      return true;
   }

   public static Set<Long> init(List<BlockPos> steps) {
      Set<Long> out = new HashSet<>();

      for (BlockPos f : steps) {
         for (int h = 0; h < 3; h++) {
            out.add(f.above(h).asLong());
         }
      }

      return out;
   }

   public static BlockPos init_BlockPos_Level_3(Level level, BlockPos feet, BlockPos ore, Predicate<BlockPos> blocked, int maxDepth) {
      return init_BlockPos_BlockGetter(level, feet, ore, blocked, maxDepth, (x, z) -> level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z));
   }

   static BlockPos init_BlockPos_BlockGetter(
      BlockGetter level, BlockPos feet, BlockPos ore, Predicate<BlockPos> blocked, int maxDepth, IntBinaryOperator surfaceHeight
   ) {
      BlockPos best = null;
      int bestDistance = 2147483647;

      for (Direction side : Plane.HORIZONTAL) {
         int x = ore.getX() + side.getStepX();
         int z = ore.getZ() + side.getStepZ();
         int distance = Math.abs(x - feet.getX()) + Math.abs(z - feet.getZ());
         BlockPos entry = new BlockPos(x, feet.getY(), z);
         boolean adjacent = distance == 1;

         for (int h = 0; h <= 1 && adjacent; h++) {
            BlockPos cell = entry.above(h);
            BlockState state = level.getBlockState(cell);
            adjacent = state.getFluidState().isEmpty() && state.getCollisionShape(level, cell).isEmpty() && !blocked.test(cell);
         }

         int top = distance != 0 && !adjacent ? surfaceHeight.applyAsInt(x, z) : feet.getY();
         if (top - ore.getY() <= maxDepth && top > ore.getY() + 1 && level.getFluidState(new BlockPos(x, top, z)).isEmpty()) {
            boolean solid = true;

            for (int y = ore.getY(); y < top && solid; y++) {
               BlockPos cell = new BlockPos(x, y, z);
               BlockState state = level.getBlockState(cell);
               solid = !state.isAir() && state.getFluidState().isEmpty() && !state.getCollisionShape(level, cell).isEmpty() && !blocked.test(cell);
            }

            if (solid
               && !level.getBlockState(new BlockPos(x, ore.getY() - 1, z)).getCollisionShape(level, new BlockPos(x, ore.getY() - 1, z)).isEmpty()
               && distance < bestDistance) {
               bestDistance = distance;
               best = new BlockPos(x, top, z);
            }
         }
      }

      return best;
   }

   public static AiOperation init_AiOperation_BlockPos(final BlockPos top, final int bottomY, final int timeoutSeconds, final BooleanSupplier enough) {
      final Minecraft mc = Minecraft.getInstance();
      final h.Inner_DataUtil goal = new h.Inner_DataUtil(top, bottomY);
      final long deadline = System.nanoTime() + Math.max(10, timeoutSeconds) * 1000000000L;
      return new AiOperation() {
         AiOperation init_AiOperation = mc.player.blockPosition().equals(top)
            ? null
            : HashUtil.init_AiOperation_BlockPos(top, 0.5, true, false, false, Math.max(10, timeoutSeconds / 2));
         HashUtil.Inner_CachePool init_HashUtil_CachePool;

         @Override
         public AiOperation.Result tick() {
            if (enough.getAsBoolean() && this.canInterruptNow()) {
               this.cancel();
               return new AiOperation.Result(
                  AiOperation.Status.SUCCEEDED, "Shaft dug to " + mc.player.blockPosition().toShortString() + ", enough held", "{}", List.of()
               );
            }

            if (this.init_AiOperation != null) {
               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, "Walking to where the shaft starts: " + r.message(), "{}", List.of());
               }

               this.init_AiOperation = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return r;
               }
            }

            if (this.init_HashUtil_CachePool == null) {
               AutismClientAddon.LOG.info("[AI] shaft: from {} down to y {}", mc.player.blockPosition().toShortString(), bottomY);
               this.init_HashUtil_CachePool = HashUtil.init_HashUtil_CachePool_BlockPos(
                  mc.player.blockPosition(), goal, (int)Math.max(10L, (deadline - System.nanoTime()) / 1000000000L)
               );
            }

            if (this.init_HashUtil_CachePool.init_Z_13()) {
               this.init_HashUtil_CachePool.cancel();
               return new AiOperation.Result(AiOperation.Status.SUCCEEDED, "Shaft dug to y " + bottomY, "{}", List.of());
            } else {
               return this.init_HashUtil_CachePool.tick();
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.init_AiOperation != null
               ? this.init_AiOperation.canInterruptNow()
               : this.init_HashUtil_CachePool == null || this.init_HashUtil_CachePool.load_Z_8();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_AiOperation != null
               ? this.init_AiOperation.requiresReconciliationAfterEmergency()
               : this.init_HashUtil_CachePool != null && this.init_HashUtil_CachePool.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }

            if (this.init_HashUtil_CachePool != null) {
               this.init_HashUtil_CachePool.cancel();
            }
         }
      };
   }

   public static Predicate<BlockPos> init(Level level, Predicate<BlockState> keep) {
      return cell -> {
         BlockState state = level.getBlockState(cell);
         if (!state.isAir() && init(level, cell)) {
            return true;
         } else {
            return state.isAir()
               ? false
               : state.hasBlockEntity()
                  || state.is(Blocks.CRAFTING_TABLE)
                  || state.getDestroySpeed(level, cell) < 0.0F
                  || !state.getFluidState().isEmpty()
                  || keep.test(state);
         }
      };
   }

   static boolean init(Level level, BlockPos cell) {
      for (Direction side : Direction.values()) {
         FluidState fluid = level.getFluidState(cell.relative(side));
         if (fluid.is(FluidTags.LAVA)) {
            return true;
         }

         if (side != Direction.DOWN && fluid.is(FluidTags.WATER)) {
            return true;
         }
      }

      return false;
   }

   public static Predicate<BlockPos> init(Level level) {
      return cell -> level.getBlockState(cell).getCollisionShape(level, cell).isEmpty() || !level.getFluidState(cell).isEmpty();
   }

   public static Predicate<BlockState> init(String family) {
      return state -> TokenStore.init(family, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
   }

   public static BlockPos init_BlockPos_Level(Level level, BlockPos from, int radius, Predicate<BlockState> matches) {
      return init_BlockPos_Level_2(level, from, radius, matches, p -> p.distSqr(from));
   }

   public static BlockPos init_BlockPos_Level(Level level, BlockPos from, int radius, int minDrop, Predicate<BlockState> matches) {
      return init_BlockPos_Level(level, from, radius, minDrop, matches, p -> true);
   }

   public static BlockPos init_BlockPos_Level(Level level, BlockPos from, int radius, int minDrop, Predicate<BlockState> matches, Predicate<BlockPos> usable) {
      return init_BlockPos_Level_2(level, from, radius, matches, p -> {
         int drop = from.getY() - p.getY();
         return drop < minDrop ? 1.7976931348623157E308 : Math.max(Math.abs(p.getX() - from.getX()) + Math.abs(p.getZ() - from.getZ()), drop) + drop * 0.01;
      }, usable);
   }

   public static BlockPos load(Level level, BlockPos from, int radius, int minDrop, Predicate<BlockState> matches, Predicate<BlockPos> usable) {
      return init_BlockPos_Level_2(
         level, from, radius, matches, p -> from.getY() - p.getY() < minDrop ? 1.7976931348623157E308 : BufferHelper.init(level, from, p), usable
      );
   }

   public static BlockPos init(Level level, BlockPos from, int radius, int minDrop, Predicate<BlockState> matches, Predicate<BlockPos> usable, int batch) {
      if (batch <= 1) {
         return load(level, from, radius, minDrop, matches, usable);
      }

      BlockPos first = load(level, from, radius, minDrop, matches, usable);
      if (first == null) {
         return null;
      }

      Predicate<BlockPos> usableOre = cell -> matches.test(level.getBlockState(cell)) && usable.test(cell);
      int missing = batch - init_I_BlockPos(first, batch, usableOre);
      if (missing <= 0) {
         return first;
      }

      BlockPos complete = init_BlockPos_Level_2(
         level,
         from,
         radius,
         matches,
         p -> from.getY() - p.getY() < minDrop ? 1.7976931348623157E308 : BufferHelper.init(level, from, p),
         p -> usable.test(p) && init_Z_BlockPos(p, batch, usableOre)
      );
      if (complete == null) {
         return first;
      }

      if (!init(init_I_Level(level, from, first), init_I_Level(level, from, complete))) {
         return first;
      }

      BlockPos remainder = init_BlockPos_Level_2(
         level,
         from,
         radius,
         matches,
         p -> BufferHelper.init(level, first, p),
         p -> usable.test(p) && init(first, p) && init_Z_BlockPos(p, missing, cell -> init(first, cell) && usableOre.test(cell))
      );
      return remainder != null
            && !init_Z_D(BufferHelper.init(level, from, complete), BufferHelper.init(level, from, first), BufferHelper.init(level, first, remainder))
         ? first
         : complete;
   }

   static boolean init_Z_D(double completeCost, double firstCost, double remainderCost) {
      return completeCost <= firstCost + remainderCost;
   }

   static boolean init(int firstDepth, int completeDepth) {
      return completeDepth <= firstDepth;
   }

   private static int init_I_Level(Level level, BlockPos from, BlockPos ore) {
      int surface = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, ore.getX(), ore.getZ());
      return Math.max(0, Math.min(from.getY(), surface) - ore.getY());
   }

   private static boolean init(BlockPos first, BlockPos cell) {
      return Math.abs(cell.getX() - first.getX()) > 2 || Math.abs(cell.getY() - first.getY()) > 2 || Math.abs(cell.getZ() - first.getZ()) > 2;
   }

   static boolean init_Z_BlockPos(BlockPos target, int batch, Predicate<BlockPos> usableOre) {
      return batch <= 0 || init_I_BlockPos(target, batch, usableOre) >= batch;
   }

   private static int init_I_BlockPos(BlockPos target, int cap, Predicate<BlockPos> usableOre) {
      int count = usableOre.test(target) ? 1 : 0;
      if (count >= cap) {
         return count;
      }

      for (int dy = -2; dy <= 2; dy++) {
         for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
               if ((dx != 0 || dy != 0 || dz != 0) && usableOre.test(target.offset(dx, dy, dz))) {
                  if (++count >= cap) {
                     return count;
                  }
               }
            }
         }
      }

      return count;
   }

   private static BlockPos init_BlockPos_Level_2(Level level, BlockPos from, int radius, Predicate<BlockState> matches, ToDoubleFunction<BlockPos> score) {
      return init_BlockPos_Level_2(level, from, radius, matches, score, p -> true);
   }

   private static BlockPos init_BlockPos_Level_2(
      Level level, BlockPos from, int radius, Predicate<BlockState> matches, ToDoubleFunction<BlockPos> score, Predicate<BlockPos> usable
   ) {
      BlockPos best = null;
      double bestSq = 1.7976931348623157E308;
      int cr = radius + 15 >> 4;

      for (int cx = (from.getX() >> 4) - cr; cx <= (from.getX() >> 4) + cr; cx++) {
         for (int cz = (from.getZ() >> 4) - cr; cz <= (from.getZ() >> 4) + cr; cz++) {
            if (level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) instanceof LevelChunk chunk) {
               LevelChunkSection[] sections = chunk.getSections();

               for (int si = 0; si < sections.length; si++) {
                  LevelChunkSection section = sections[si];
                  if (section != null && !section.hasOnlyAir() && section.maybeHas(matches::test)) {
                     int baseY = level.getSectionYFromSectionIndex(si) << 4;

                     for (int i = 0; i < 4096; i++) {
                        int x = i & 15;
                        int z = i >> 4 & 15;
                        int y = i >> 8;
                        if (matches.test(section.getBlockState(x, y, z))) {
                           BlockPos p = new BlockPos((cx << 4) + x, baseY + y, (cz << 4) + z);
                           double dx = p.getX() - from.getX();
                           double dz = p.getZ() - from.getZ();
                           if (!(dx * dx + dz * dz > (double)radius * radius)) {
                              double sq = score.applyAsDouble(p);
                              if (sq < bestSq && usable.test(p)) {
                                 bestSq = sq;
                                 best = p;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   public static AiOperation init(List<BlockPos> steps, int timeoutSeconds) {
      return init_AiOperation_List(steps, timeoutSeconds, () -> false);
   }

   public static AiOperation init_AiOperation_List(List<BlockPos> steps, int timeoutSeconds, BooleanSupplier enough) {
      return init_AiOperation_List(steps, timeoutSeconds, enough, null);
   }

   public static AiOperation init_AiOperation_List(final List<BlockPos> steps, final int timeoutSeconds, final BooleanSupplier enough, final BlockPos approach) {
      final Minecraft mc = Minecraft.getInstance();
      final h.Inner_StreamUtil goal = new h.Inner_StreamUtil(steps);
      final long deadline = System.nanoTime() + Math.max(10, timeoutSeconds) * 1000000000L;
      return new AiOperation() {
         AiOperation init_AiOperation = approach != null && !mc.player.blockPosition().equals(approach)
            ? HashUtil.init_AiOperation_BlockPos(approach, 0.5, true, false, false, Math.max(10, timeoutSeconds / 2))
            : null;
         HashUtil.Inner_CachePool init_HashUtil_CachePool;

         @Override
         public AiOperation.Result tick() {
            if (enough.getAsBoolean() && this.canInterruptNow()) {
               this.cancel();
               return new AiOperation.Result(
                  AiOperation.Status.SUCCEEDED, "Staircase dug to " + mc.player.blockPosition().toShortString() + ", enough held", "{}", List.of()
               );
            }

            if (this.init_AiOperation != null) {
               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, "Walking to where the staircase starts: " + r.message(), "{}", List.of());
               }

               this.init_AiOperation = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return r;
               }
            }

            if (this.init_HashUtil_CachePool == null) {
               AutismClientAddon.LOG
                  .info("[AI] staircase: {} steps from {} to {}", steps.size(), mc.player.blockPosition().toShortString(), goal.init_BlockPos.toShortString());
               this.init_HashUtil_CachePool = HashUtil.init_HashUtil_CachePool_BlockPos(
                  mc.player.blockPosition(), goal, (int)Math.max(10L, (deadline - System.nanoTime()) / 1000000000L)
               );
            }

            if (this.init_HashUtil_CachePool.init_Z_13()) {
               this.init_HashUtil_CachePool.cancel();
               return new AiOperation.Result(AiOperation.Status.SUCCEEDED, "Staircase dug to " + goal.init_BlockPos.toShortString(), "{}", List.of());
            } else {
               return this.init_HashUtil_CachePool.tick();
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.init_AiOperation != null
               ? this.init_AiOperation.canInterruptNow()
               : this.init_HashUtil_CachePool == null || this.init_HashUtil_CachePool.load_Z_8();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_AiOperation != null
               ? this.init_AiOperation.requiresReconciliationAfterEmergency()
               : this.init_HashUtil_CachePool != null && this.init_HashUtil_CachePool.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }

            if (this.init_HashUtil_CachePool != null) {
               this.init_HashUtil_CachePool.cancel();
            }
         }
      };
   }

   public static BlockPos init_BlockPos_Level(Level level, BlockPos feet, BlockPos target) {
      BlockPos start = feet;

      for (int pass = 0; pass < 2; pass++) {
         int dx = target.getX() - feet.getX();
         int dz = target.getZ() - feet.getZ();
         int depth = start.getY() - (target.getY() + 1);
         int walk = Math.abs(dx) + Math.abs(dz) - depth - 1;
         if (walk < 2) {
            return pass == 0 ? null : start;
         }

         int alongX = Math.abs(dx) >= Math.abs(dz) ? Math.min(Math.abs(dx), walk) : Math.max(0, walk - Math.abs(dz));
         int alongZ = Math.min(Math.abs(dz), walk - alongX);
         int x = feet.getX() + Integer.signum(dx) * alongX;
         int z = feet.getZ() + Integer.signum(dz) * alongZ;
         int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
         BlockPos at = new BlockPos(x, y, z);
         if (!level.getFluidState(at).isEmpty() || !level.getFluidState(at.below()).isEmpty()) {
            return null;
         }

         if (Math.abs(y - feet.getY()) > 6) {
            return null;
         }

         start = at;
      }

      return start;
   }

   public static final class Inner_DataUtil implements ClassCache.Inner_JsonUtil {
      final int init;
      final int load;
      final int read;
      final int write;

      public Inner_DataUtil(BlockPos top, int bottomY) {
         this.init = top.getX();
         this.load = top.getZ();
         this.read = top.getY();
         this.write = bottomY;
      }

      @Override
      public boolean isInGoal(int x, int y, int z) {
         return x == this.init && z == this.load && y == this.write;
      }

      @Override
      public double heuristic(int x, int y, int z) {
         return ClassCache.Inner_CachePool.init_D_I(this.init - x, this.write - y, this.load - z);
      }

      private boolean update(BlockPos cell) {
         return cell.getX() == this.init && cell.getZ() == this.load && cell.getY() >= this.write && cell.getY() <= this.read + 1;
      }

      @Override
      public boolean load(BlockPos cell) {
         return this.update(cell);
      }

      @Override
      public boolean init_Z(BlockPos cell) {
         return this.update(cell);
      }

      @Override
      public boolean read(BlockPos cell) {
         return false;
      }

      @Override
      public Set<ClassCache.Inner_SyncManager> init_Set() {
         return EnumSet.of(ClassCache.Inner_SyncManager.tick);
      }

      @Override
      public String toString() {
         return "shaft(" + this.init + ", " + this.load + " from y " + this.read + " to " + this.write + ")";
      }
   }

   public static final class Inner_StreamUtil implements ClassCache.Inner_JsonUtil {
      final Set<Long> init_Set;
      final Set<Long> load = new HashSet<>();
      final BlockPos init_BlockPos;

      public Inner_StreamUtil(List<BlockPos> steps) {
         this.init_Set = h.init(steps);

         for (BlockPos f : steps) {
            this.load.add(f.below().asLong());
         }

         this.init_BlockPos = steps.get(steps.size() - 1);
      }

      @Override
      public boolean isInGoal(int x, int y, int z) {
         return x == this.init_BlockPos.getX() && y == this.init_BlockPos.getY() && z == this.init_BlockPos.getZ();
      }

      @Override
      public double heuristic(int x, int y, int z) {
         return ClassCache.Inner_CachePool.init_D_I(this.init_BlockPos.getX() - x, this.init_BlockPos.getY() - y, this.init_BlockPos.getZ() - z);
      }

      @Override
      public boolean load(BlockPos cell) {
         return this.init_Set.contains(cell.asLong());
      }

      @Override
      public boolean init_Z(BlockPos cell) {
         return this.init_Set.contains(cell.asLong());
      }

      @Override
      public boolean read(BlockPos cell) {
         return this.load.contains(cell.asLong());
      }

      @Override
      public Set<ClassCache.Inner_SyncManager> init_Set() {
         return EnumSet.of(ClassCache.Inner_SyncManager.push, ClassCache.Inner_SyncManager.tick);
      }

      @Override
      public String toString() {
         return "staircase(" + this.init_Set.size() / 3 + " steps to " + this.init_BlockPos.toShortString() + ")";
      }
   }
}
