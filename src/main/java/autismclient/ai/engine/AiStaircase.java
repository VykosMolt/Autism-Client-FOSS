package autismclient.ai.engine;

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

public final class AiStaircase {
   private static final int[][] HORIZONTAL_DIRS = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
   private static final int init_I = 8;

   private AiStaircase() {
   }

   public static List<BlockPos> planSteps(BlockPos start, BlockPos target, int maxSteps, Predicate<BlockPos> blocked) {
      return planSteps(start, target, maxSteps, blocked, cell -> false);
   }

   public static List<BlockPos> planSteps(BlockPos start, BlockPos target, int maxSteps, Predicate<BlockPos> blocked, Predicate<BlockPos> noFloor) {
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
            int k = last == null ? 0 : dirIndex(last);

            for (int t = 1; t <= 4; t++) {
               options.add(HORIZONTAL_DIRS[(k + t) % 4]);
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

            for (int[] d : HORIZONTAL_DIRS) {
               options.add(d);
            }
         }

         int[] chosen = null;
         int bestRun = -1;
         int horizon = Math.min(8, n - i);

         for (int[] d : options) {
            if (last == null || d[0] != -last[0] || d[1] != -last[1]) {
               BlockPos feet = new BlockPos(x + d[0], y - 1, z + d[1]);
               if (isClearStand(feet, floors, blocked) && !noFloor.test(feet.below())) {
                  floors.add(feet.below().asLong());
                  int run = 1 + longestDescent(feet, d, horizon - 1, floors, blocked, noFloor);
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

   private static int longestDescent(BlockPos from, int[] last, int left, Set<Long> floors, Predicate<BlockPos> blocked, Predicate<BlockPos> noFloor) {
      if (left == 0) {
         return 0;
      }

      int best = 0;

      for (int[] d : HORIZONTAL_DIRS) {
         if (d[0] != -last[0] || d[1] != -last[1]) {
            BlockPos next = from.offset(d[0], -1, d[1]);
            if (isClearStand(next, floors, blocked) && !noFloor.test(next.below())) {
               floors.add(next.below().asLong());
               int run = 1 + longestDescent(next, d, left - 1, floors, blocked, noFloor);
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

   private static int dirIndex(int[] d) {
      for (int i = 0; i < 4; i++) {
         if (HORIZONTAL_DIRS[i][0] == d[0] && HORIZONTAL_DIRS[i][1] == d[1]) {
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
               if (!noFloor.test(feet.below()) && isClearStand(feet, Set.of(), blocked)) {
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

   public static List<BlockPos> planStairs(BlockPos start, BlockPos target, int maxSteps, Predicate<BlockPos> blocked, Predicate<BlockPos> noFloor) {
      List<BlockPos> down = planSteps(start, target, maxSteps, blocked, noFloor);
      if (!down.isEmpty()) {
         return down;
      }

      List<BlockPos> headings = new ArrayList<>();
      headings.add(target);

      for (int[] d : HORIZONTAL_DIRS) {
         headings.add(start.offset(d[0] * 8, 0, d[1] * 8));
      }

      for (BlockPos heading : headings) {
         List<BlockPos> level = load(start, heading, Math.min(8, maxSteps - 1), blocked, noFloor);
         Set<Long> floors = new HashSet<>();
         floors.add(start.below().asLong());

         for (int i = 0; i < level.size(); i++) {
            BlockPos at = level.get(i);
            floors.add(at.below().asLong());
            List<BlockPos> continuation = planSteps(
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

   private static boolean isClearStand(BlockPos feet, Set<Long> floors, Predicate<BlockPos> blocked) {
      for (int h = 0; h < 3; h++) {
         BlockPos cell = feet.above(h);
         if (floors.contains(cell.asLong()) || blocked.test(cell)) {
            return false;
         }
      }

      return true;
   }

   public static Set<Long> bodyCells(List<BlockPos> steps) {
      Set<Long> out = new HashSet<>();

      for (BlockPos f : steps) {
         for (int h = 0; h < 3; h++) {
            out.add(f.above(h).asLong());
         }
      }

      return out;
   }

   public static BlockPos findStairTop(Level level, BlockPos feet, BlockPos ore, Predicate<BlockPos> blocked, int maxDepth) {
      return findStairTopUsing(level, feet, ore, blocked, maxDepth, (x, z) -> level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z));
   }

   static BlockPos findStairTopUsing(
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

   public static AiOperation digDownOperation(final BlockPos top, final int bottomY, final int timeoutSeconds, final BooleanSupplier enough) {
      final Minecraft mc = Minecraft.getInstance();
      final AiStaircase.ShaftGoal goal = new AiStaircase.ShaftGoal(top, bottomY);
      final long deadline = System.nanoTime() + Math.max(10, timeoutSeconds) * 1000000000L;
      return new AiOperation() {
         AiOperation childOp = mc.player.blockPosition().equals(top)
            ? null
            : AiGoto.travelToCompat(top, 0.5, true, false, false, Math.max(10, timeoutSeconds / 2));
         AiGoto.GotoOp gotoOp;

         @Override
         public AiOperation.Result tick() {
            if (enough.getAsBoolean() && this.canInterruptNow()) {
               this.cancel();
               return new AiOperation.Result(
                  AiOperation.Status.SUCCEEDED, "Shaft dug to " + mc.player.blockPosition().toShortString() + ", enough held", "{}", List.of()
               );
            }

            if (this.childOp != null) {
               AiOperation.Result r = this.childOp.tick();
               if (!r.terminal()) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, "Walking to where the shaft starts: " + r.message(), "{}", List.of());
               }

               this.childOp = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return r;
               }
            }

            if (this.gotoOp == null) {
               AutismClientAddon.LOG.info("[AI] shaft: from {} down to y {}", mc.player.blockPosition().toShortString(), bottomY);
               this.gotoOp = AiGoto.createPersistentOpCompat(
                  mc.player.blockPosition(), goal, (int)Math.max(10L, (deadline - System.nanoTime()) / 1000000000L)
               );
            }

            if (this.gotoOp.isAtGoal()) {
               this.gotoOp.cancel();
               return new AiOperation.Result(AiOperation.Status.SUCCEEDED, "Shaft dug to y " + bottomY, "{}", List.of());
            } else {
               return this.gotoOp.tick();
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.childOp != null
               ? this.childOp.canInterruptNow()
               : this.gotoOp == null || this.gotoOp.canInterrupt();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.childOp != null
               ? this.childOp.requiresReconciliationAfterEmergency()
               : this.gotoOp != null && this.gotoOp.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            if (this.childOp != null) {
               this.childOp.cancel();
            }

            if (this.gotoOp != null) {
               this.gotoOp.cancel();
            }
         }
      };
   }

   public static Predicate<BlockPos> protectedCells(Level level, Predicate<BlockState> keep) {
      return cell -> {
         BlockState state = level.getBlockState(cell);
         if (!state.isAir() && touchesFluid(level, cell)) {
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

   static boolean touchesFluid(Level level, BlockPos cell) {
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

   public static Predicate<BlockPos> noFloor(Level level) {
      return cell -> level.getBlockState(cell).getCollisionShape(level, cell).isEmpty() || !level.getFluidState(cell).isEmpty();
   }

   public static Predicate<BlockState> matchesFamily(String family) {
      return state -> AiMine.matchesBlock(family, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
   }

   public static BlockPos findNearestBlock(Level level, BlockPos from, int radius, Predicate<BlockState> matches) {
      return init_BlockPos_Level_2(level, from, radius, matches, p -> p.distSqr(from));
   }

   public static BlockPos init_BlockPos_Level(Level level, BlockPos from, int radius, int minDrop, Predicate<BlockState> matches) {
      return init_BlockPos_Level(level, from, radius, minDrop, matches, p -> true);
   }

   public static BlockPos init_BlockPos_Level(Level level, BlockPos from, int radius, int minDrop, Predicate<BlockState> matches, Predicate<BlockPos> usable) {
      return findBestBlock(level, from, radius, matches, p -> {
         int drop = from.getY() - p.getY();
         return drop < minDrop ? 1.7976931348623157E308 : Math.max(Math.abs(p.getX() - from.getX()) + Math.abs(p.getZ() - from.getZ()), drop) + drop * 0.01;
      }, usable);
   }

   public static BlockPos findTarget(Level level, BlockPos from, int radius, int minDrop, Predicate<BlockState> matches, Predicate<BlockPos> usable) {
      return findBestBlock(
         level, from, radius, matches, p -> from.getY() - p.getY() < minDrop ? 1.7976931348623157E308 : AiBlockMine.travelCost(level, from, p), usable
      );
   }

   public static BlockPos findBatchTarget(Level level, BlockPos from, int radius, int minDrop, Predicate<BlockState> matches, Predicate<BlockPos> usable, int batch) {
      if (batch <= 1) {
         return findTarget(level, from, radius, minDrop, matches, usable);
      }

      BlockPos first = findTarget(level, from, radius, minDrop, matches, usable);
      if (first == null) {
         return null;
      }

      Predicate<BlockPos> usableOre = cell -> matches.test(level.getBlockState(cell)) && usable.test(cell);
      int missing = batch - countUsableOre(first, batch, usableOre);
      if (missing <= 0) {
         return first;
      }

      BlockPos complete = findBestBlock(
         level,
         from,
         radius,
         matches,
         p -> from.getY() - p.getY() < minDrop ? 1.7976931348623157E308 : AiBlockMine.travelCost(level, from, p),
         p -> usable.test(p) && init_Z_BlockPos(p, batch, usableOre)
      );
      if (complete == null) {
         return first;
      }

      if (!completeNoDeeper(depthBelowSurface(level, from, first), depthBelowSurface(level, from, complete))) {
         return first;
      }

      BlockPos remainder = findBestBlock(
         level,
         from,
         radius,
         matches,
         p -> AiBlockMine.travelCost(level, first, p),
         p -> usable.test(p) && outsideFirstBox(first, p) && init_Z_BlockPos(p, missing, cell -> outsideFirstBox(first, cell) && usableOre.test(cell))
      );
      return remainder != null
            && !init_Z_D(AiBlockMine.travelCost(level, from, complete), AiBlockMine.travelCost(level, from, first), AiBlockMine.travelCost(level, first, remainder))
         ? first
         : complete;
   }

   static boolean init_Z_D(double completeCost, double firstCost, double remainderCost) {
      return completeCost <= firstCost + remainderCost;
   }

   static boolean completeNoDeeper(int firstDepth, int completeDepth) {
      return completeDepth <= firstDepth;
   }

   private static int depthBelowSurface(Level level, BlockPos from, BlockPos ore) {
      int surface = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, ore.getX(), ore.getZ());
      return Math.max(0, Math.min(from.getY(), surface) - ore.getY());
   }

   private static boolean outsideFirstBox(BlockPos first, BlockPos cell) {
      return Math.abs(cell.getX() - first.getX()) > 2 || Math.abs(cell.getY() - first.getY()) > 2 || Math.abs(cell.getZ() - first.getZ()) > 2;
   }

   static boolean init_Z_BlockPos(BlockPos target, int batch, Predicate<BlockPos> usableOre) {
      return batch <= 0 || countUsableOre(target, batch, usableOre) >= batch;
   }

   private static int countUsableOre(BlockPos target, int cap, Predicate<BlockPos> usableOre) {
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
      return findBestBlock(level, from, radius, matches, score, p -> true);
   }

   private static BlockPos findBestBlock(
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

   public static AiOperation followStepsOperation(List<BlockPos> steps, int timeoutSeconds) {
      return followStepsOperation(steps, timeoutSeconds, () -> false);
   }

   public static AiOperation followStepsOperation(List<BlockPos> steps, int timeoutSeconds, BooleanSupplier enough) {
      return followStepsOperation(steps, timeoutSeconds, enough, null);
   }

   public static AiOperation followStepsOperation(final List<BlockPos> steps, final int timeoutSeconds, final BooleanSupplier enough, final BlockPos approach) {
      final Minecraft mc = Minecraft.getInstance();
      final AiStaircase.StaircaseGoal goal = new AiStaircase.StaircaseGoal(steps);
      final long deadline = System.nanoTime() + Math.max(10, timeoutSeconds) * 1000000000L;
      return new AiOperation() {
         AiOperation childOp = approach != null && !mc.player.blockPosition().equals(approach)
            ? AiGoto.travelToCompat(approach, 0.5, true, false, false, Math.max(10, timeoutSeconds / 2))
            : null;
         AiGoto.GotoOp gotoOp;

         @Override
         public AiOperation.Result tick() {
            if (enough.getAsBoolean() && this.canInterruptNow()) {
               this.cancel();
               return new AiOperation.Result(
                  AiOperation.Status.SUCCEEDED, "Staircase dug to " + mc.player.blockPosition().toShortString() + ", enough held", "{}", List.of()
               );
            }

            if (this.childOp != null) {
               AiOperation.Result r = this.childOp.tick();
               if (!r.terminal()) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, "Walking to where the staircase starts: " + r.message(), "{}", List.of());
               }

               this.childOp = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return r;
               }
            }

            if (this.gotoOp == null) {
               AutismClientAddon.LOG
                  .info("[AI] staircase: {} steps from {} to {}", steps.size(), mc.player.blockPosition().toShortString(), goal.lastStep.toShortString());
               this.gotoOp = AiGoto.createPersistentOpCompat(
                  mc.player.blockPosition(), goal, (int)Math.max(10L, (deadline - System.nanoTime()) / 1000000000L)
               );
            }

            if (this.gotoOp.isAtGoal()) {
               this.gotoOp.cancel();
               return new AiOperation.Result(AiOperation.Status.SUCCEEDED, "Staircase dug to " + goal.lastStep.toShortString(), "{}", List.of());
            } else {
               return this.gotoOp.tick();
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.childOp != null
               ? this.childOp.canInterruptNow()
               : this.gotoOp == null || this.gotoOp.canInterrupt();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.childOp != null
               ? this.childOp.requiresReconciliationAfterEmergency()
               : this.gotoOp != null && this.gotoOp.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            if (this.childOp != null) {
               this.childOp.cancel();
            }

            if (this.gotoOp != null) {
               this.gotoOp.cancel();
            }
         }
      };
   }

   public static BlockPos findStairStart(Level level, BlockPos feet, BlockPos target) {
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

   public static final class ShaftGoal implements AiPathfinder.PathGoal {
      final int shaftX;
      final int shaftZ;
      final int topY;
      final int bottomY;

      public ShaftGoal(BlockPos top, int bottomY) {
         this.shaftX = top.getX();
         this.shaftZ = top.getZ();
         this.topY = top.getY();
         this.bottomY = bottomY;
      }

      @Override
      public boolean isInGoal(int x, int y, int z) {
         return x == this.shaftX && z == this.shaftZ && y == this.bottomY;
      }

      @Override
      public double heuristic(int x, int y, int z) {
         return AiPathfinder.MoveCosts.estimateHeuristicCompat(this.shaftX - x, this.bottomY - y, this.shaftZ - z);
      }

      private boolean inShaft(BlockPos cell) {
         return cell.getX() == this.shaftX && cell.getZ() == this.shaftZ && cell.getY() >= this.bottomY && cell.getY() <= this.topY + 1;
      }

      @Override
      public boolean mayBreak(BlockPos cell) {
         return this.inShaft(cell);
      }

      @Override
      public boolean allowsCell(BlockPos cell) {
         return this.inShaft(cell);
      }

      @Override
      public boolean mayPlace(BlockPos cell) {
         return false;
      }

      @Override
      public Set<AiPathfinder.MoveType> disallowedMoves() {
         return EnumSet.of(AiPathfinder.MoveType.PILLAR);
      }

      @Override
      public String toString() {
         return "shaft(" + this.shaftX + ", " + this.shaftZ + " from y " + this.topY + " to " + this.bottomY + ")";
      }
   }

   public static final class StaircaseGoal implements AiPathfinder.PathGoal {
      final Set<Long> bodyCells;
      final Set<Long> floorCells = new HashSet<>();
      final BlockPos lastStep;

      public StaircaseGoal(List<BlockPos> steps) {
         this.bodyCells = AiStaircase.bodyCells(steps);

         for (BlockPos f : steps) {
            this.floorCells.add(f.below().asLong());
         }

         this.lastStep = steps.get(steps.size() - 1);
      }

      @Override
      public boolean isInGoal(int x, int y, int z) {
         return x == this.lastStep.getX() && y == this.lastStep.getY() && z == this.lastStep.getZ();
      }

      @Override
      public double heuristic(int x, int y, int z) {
         return AiPathfinder.MoveCosts.estimateHeuristicCompat(this.lastStep.getX() - x, this.lastStep.getY() - y, this.lastStep.getZ() - z);
      }

      @Override
      public boolean mayBreak(BlockPos cell) {
         return this.bodyCells.contains(cell.asLong());
      }

      @Override
      public boolean allowsCell(BlockPos cell) {
         return this.bodyCells.contains(cell.asLong());
      }

      @Override
      public boolean mayPlace(BlockPos cell) {
         return this.floorCells.contains(cell.asLong());
      }

      @Override
      public Set<AiPathfinder.MoveType> disallowedMoves() {
         return EnumSet.of(AiPathfinder.MoveType.DIG_DOWN_1, AiPathfinder.MoveType.PILLAR);
      }

      @Override
      public String toString() {
         return "staircase(" + this.bodyCells.size() / 3 + " steps to " + this.lastStep.toShortString() + ")";
      }
   }
}
