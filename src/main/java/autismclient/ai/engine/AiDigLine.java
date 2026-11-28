package autismclient.ai.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiDigLine {
   static final double init_D = 0.06;
   static final double[] HEAD_GRID = new double[]{0.12, 0.31, 0.5, 0.69, 0.88};
   static final double load_D = 60.0;
   static final double read_D = 0.6;
   static final int init_I = 5;
   static final int load_I = 6;
   static final double[] DEEP_GRID = new double[]{0.2, 0.5, 0.8};
   static final int read_I = 8;

   private AiDigLine() {
   }

   public static AiDigLine.Inner_DataUtil planLineFor(
      Vec3 eye, float yaw, float pitch, Collection<BlockPos> targets, AiDigLine.Inner_CachePool shapes, double reach
   ) {
      return planLine(eye, yaw, pitch, targets, targets, shapes, reach);
   }

   public static AiDigLine.Inner_DataUtil planLine(
      Vec3 eye, float yaw, float pitch, Collection<BlockPos> heads, Collection<BlockPos> targets, AiDigLine.Inner_CachePool shapes, double reach
   ) {
      return planLine(eye, yaw, pitch, heads, targets, shapes, reach, 0.0F / 0.0F);
   }

   public static AiDigLine.Inner_DataUtil planLine(
      Vec3 eye, float yaw, float pitch, Collection<BlockPos> heads, Collection<BlockPos> targets, AiDigLine.Inner_CachePool shapes, double reach, float pathYaw
   ) {
      Set<BlockPos> set = new HashSet<>(targets);
      AiDigLine.Inner_DataUtil best = null;
      double bestScore = -1.7976931348623157E308;

      for (BlockPos cell : targets) {
         VoxelShape shape = shapes.outline(cell);
         if (!shape.isEmpty()) {
            AABB box = shape.bounds().move(cell);

            for (Direction face : Direction.values()) {
               if (init(eye, box, face)) {
                  for (double a : HEAD_GRID) {
                     for (double b : HEAD_GRID) {
                        Vec3 point = init(box, face, a, b);
                        Vec3 d = point.subtract(eye);
                        if (!(d.length() > reach) && !(d.lengthSqr() < 1.0E-8)) {
                           Vec3 dir = d.normalize();
                           double[] margin = new double[]{0.0};
                           List<BlockPos> chain = rayChain(eye, dir, reach, set, shapes, margin);
                           if (!chain.isEmpty() && heads.contains(chain.get(0))) {
                              float ly = init(dir);
                              float lp = load(dir);
                              double turn = Math.abs(init(ly - yaw)) + Math.abs(lp - pitch);
                              double score = chain.size() * 60.0 - turn + Math.min(margin[0], 0.3) * 10.0;
                              if (heads.size() > 1 && chain.containsAll(heads)) {
                                 score += 60.0;
                              }

                              if (!Float.isNaN(pathYaw)) {
                                 score -= Math.abs(init(ly - pathYaw)) * 0.6;
                              }

                              if (score > bestScore) {
                                 bestScore = score;
                                 best = new AiDigLine.Inner_DataUtil(ly, lp, List.copyOf(chain), margin[0]);
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

   public static List<BlockPos> rayChain(Vec3 eye, Vec3 dir, double reach, Set<BlockPos> targets, AiDigLine.Inner_CachePool shapes, double[] marginOut) {
      Set<BlockPos> removed = new HashSet<>();
      List<BlockPos> chain = new ArrayList<>();
      double min = 1.7976931348623157E308;

      while (chain.size() < targets.size()) {
         BlockHitResult hit = init(eye, dir, reach, removed, shapes);
         if (hit == null) {
            break;
         }

         BlockPos pos = hit.getBlockPos().immutable();
         if (!targets.contains(pos)) {
            break;
         }

         double m = init(hit, shapes.outline(pos).bounds().move(pos));
         if (m < 0.06) {
            break;
         }

         chain.add(pos);
         removed.add(pos);
         min = Math.min(min, m);
      }

      if (marginOut != null) {
         marginOut[0] = chain.isEmpty() ? 0.0 : min;
      }

      return chain;
   }

   public static BlockHitResult init(Vec3 eye, Vec3 dir, double reach, Set<BlockPos> removed, AiDigLine.Inner_CachePool shapes) {
      Vec3 end = eye.add(dir.scale(reach));
      return BlockGetter.traverseBlocks(eye, end, removed, (gone, pos) -> {
         if (gone.contains(pos)) {
            return null;
         }

         VoxelShape shape = shapes.outline(pos);
         return shape.isEmpty() ? null : shape.clip(eye, end, pos);
      }, gone -> null);
   }

   public static AiDigLine.Inner_DataUtil relatch(
      AiDigLine.Inner_DataUtil latched,
      boolean changed,
      Vec3 eye,
      float yaw,
      float pitch,
      Collection<BlockPos> remaining,
      AiDigLine.Inner_CachePool shapes,
      double reach
   ) {
      return relatch(latched, changed, eye, yaw, pitch, remaining, remaining, shapes, reach);
   }

   public static AiDigLine.Inner_DataUtil relatch(
      AiDigLine.Inner_DataUtil latched,
      boolean changed,
      Vec3 eye,
      float yaw,
      float pitch,
      Collection<BlockPos> remaining,
      Collection<BlockPos> targets,
      AiDigLine.Inner_CachePool shapes,
      double reach
   ) {
      return updateLatched(latched, changed, eye, yaw, pitch, remaining, targets, shapes, reach, 0.0F / 0.0F);
   }

   public static AiDigLine.Inner_DataUtil updateLatched(
      AiDigLine.Inner_DataUtil latched,
      boolean changed,
      Vec3 eye,
      float yaw,
      float pitch,
      Collection<BlockPos> remaining,
      Collection<BlockPos> targets,
      AiDigLine.Inner_CachePool shapes,
      double reach,
      float pathYaw
   ) {
      if (latched == null || init(latched, eye, remaining, shapes, reach) == null) {
         return planLine(eye, yaw, pitch, remaining, targets, shapes, reach, pathYaw);
      } else if (!changed) {
         return latched;
      } else {
         List<BlockPos> keptChain = rayChain(eye, latched.direction(), reach, new HashSet<>(targets), shapes, null);
         AiDigLine.Inner_DataUtil best = planLine(eye, yaw, pitch, remaining, targets, shapes, reach, pathYaw);
         if (best == null) {
            return latched;
         } else {
            boolean bestFinishes = remaining.size() > 1 && best.chain().containsAll(remaining);
            boolean keptFinishes = remaining.size() > 1 && keptChain.containsAll(remaining);
            if (bestFinishes != keptFinishes) {
               return bestFinishes ? best : latched;
            } else {
               return best.chain().size() > keptChain.size() ? best : latched;
            }
         }
      }
   }

   public static List<AiDigLine.Inner_DataUtil> candidateLines(
      Vec3 eye, float yaw, float pitch, Collection<BlockPos> heads, Collection<BlockPos> targets, AiDigLine.Inner_CachePool shapes, double reach
   ) {
      Map<BlockPos, VoxelShape> cache = new HashMap<>();
      AiDigLine.Inner_CachePool cached = pos -> cache.computeIfAbsent(pos.immutable(), shapes::outline);
      Set<BlockPos> all = new LinkedHashSet<>(targets);
      all.addAll(heads);
      List<AiDigLine.Inner_StreamUtil> beam = List.of(new AiDigLine.Inner_StreamUtil(List.of(), Set.of(), yaw, pitch, 0.0));
      AiDigLine.Inner_StreamUtil best = null;
      int depthCap = Math.min(all.size(), 8);

      for (int depth = 0; depth < depthCap && !beam.isEmpty(); depth++) {
         List<AiDigLine.Inner_StreamUtil> next = new ArrayList<>();

         for (AiDigLine.Inner_StreamUtil state : beam) {
            List<AiDigLine.Inner_DataUtil> options = candidateLines(
               eye, state.yaw(), state.pitch(), depth == 0 ? heads : all, all, state.gone(), cached, reach, depth == 0 ? HEAD_GRID : DEEP_GRID
            );
            if (options.isEmpty()) {
               if (!state.lines().isEmpty() && (best == null || state.score() < best.score())) {
                  best = state;
               }
            } else {
               for (AiDigLine.Inner_DataUtil line : options) {
                  List<AiDigLine.Inner_DataUtil> lines = new ArrayList<>(state.lines());
                  lines.add(line);
                  Set<BlockPos> gone = new HashSet<>(state.gone());
                  gone.addAll(line.chain());
                  next.add(
                     new AiDigLine.Inner_StreamUtil(List.copyOf(lines), gone, line.yaw(), line.pitch(), state.turn() + init(state.yaw(), state.pitch(), line))
                  );
               }
            }
         }

         next.sort(Comparator.comparingDouble(AiDigLine.Inner_StreamUtil::score));
         List<AiDigLine.Inner_StreamUtil> kept = new ArrayList<>();
         Set<Set<BlockPos>> seen = new HashSet<>();

         for (AiDigLine.Inner_StreamUtil state : next) {
            if (kept.size() >= 5) {
               break;
            }

            if (seen.add(state.gone())) {
               kept.add(state);
            }
         }

         beam = kept;

         for (AiDigLine.Inner_StreamUtil state : beam) {
            if ((state.gone().size() == all.size() || depth + 1 == depthCap) && (best == null || state.score() < best.score())) {
               best = state;
            }
         }
      }

      return best == null ? List.of() : best.lines();
   }

   public static List<AiDigLine.Inner_DataUtil> planLines(
      Vec3 eye,
      float yaw,
      float pitch,
      Collection<BlockPos> heads,
      Collection<BlockPos> targets,
      AiDigLine.Inner_CachePool shapes,
      double reach,
      int remaining,
      ToDoubleFunction<BlockPos> breakTicks
   ) {
      Map<BlockPos, VoxelShape> cache = new HashMap<>();
      AiDigLine.Inner_CachePool cached = pos -> cache.computeIfAbsent(pos.immutable(), shapes::outline);
      Set<BlockPos> all = new LinkedHashSet<>(targets);
      all.addAll(heads);
      Set<BlockPos> gone = new HashSet<>();
      List<AiDigLine.Inner_DataUtil> plan = new ArrayList<>();
      float lookYaw = yaw;
      float lookPitch = pitch;
      int needed = Math.max(1, remaining);

      for (int depth = 0; depth < Math.min(all.size(), 8) && needed > 0; depth++) {
         Collection<BlockPos> visible = depth == 0 ? heads : all;
         List<AiDigLine.Inner_DataUtil> options = new ArrayList<>(
            candidateLines(eye, lookYaw, lookPitch, visible, all, gone, cached, reach, depth == 0 ? HEAD_GRID : DEEP_GRID, 2147483647)
         );
         Vec3 direction = Vec3.directionFromRotation(lookPitch, lookYaw);
         double[] margin = new double[]{0.0};
         List<BlockPos> alreadyAimed = chainAlong(eye, direction, reach, all, gone, cached, margin);
         if (!alreadyAimed.isEmpty() && visible.contains(alreadyAimed.get(0))) {
            options.add(new AiDigLine.Inner_DataUtil(lookYaw, lookPitch, List.copyOf(alreadyAimed), margin[0]));
         }

         List<AiMiningTargetCost.TargetChoice> choices = new ArrayList<>();

         for (int i = 0; i < options.size(); i++) {
            AiDigLine.Inner_DataUtil line = options.get(i);
            List<Double> costs = new ArrayList<>();

            for (BlockPos cell : line.chain()) {
               costs.add(breakTicks.applyAsDouble(cell));
            }

            double turn = init(lookYaw, lookPitch, line);
            double time = AiMiningTargetCost.estimateTicks(costs, needed, 0.0, turn > 1.0E-6 ? 1.0 : 0.0);
            BlockPos first = line.chain().get(0);
            choices.add(new AiMiningTargetCost.TargetChoice(i, time, turn, first.distToCenterSqr(eye), first.asLong()));
         }

         int chosen = AiMiningTargetCost.pickQuickest(choices);
         if (chosen < 0) {
            break;
         }

         AiDigLine.Inner_DataUtil line = options.get(chosen);
         plan.add(line);
         gone.addAll(line.chain());
         needed -= line.chain().size();
         lookYaw = line.yaw();
         lookPitch = line.pitch();
      }

      return List.copyOf(plan);
   }

   static double init(float yaw, float pitch, AiDigLine.Inner_DataUtil line) {
      return Math.abs(init(line.yaw() - yaw)) + Math.abs(line.pitch() - pitch);
   }

   static List<AiDigLine.Inner_DataUtil> candidateLines(
      Vec3 eye,
      float yaw,
      float pitch,
      Collection<BlockPos> heads,
      Set<BlockPos> targets,
      Set<BlockPos> gone,
      AiDigLine.Inner_CachePool shapes,
      double reach,
      double[] grid
   ) {
      return candidateLines(eye, yaw, pitch, heads, targets, gone, shapes, reach, grid, 6);
   }

   private static List<AiDigLine.Inner_DataUtil> candidateLines(
      Vec3 eye,
      float yaw,
      float pitch,
      Collection<BlockPos> heads,
      Set<BlockPos> targets,
      Set<BlockPos> gone,
      AiDigLine.Inner_CachePool shapes,
      double reach,
      double[] grid,
      int limit
   ) {
      Map<List<BlockPos>, AiDigLine.Inner_DataUtil> byChain = new HashMap<>();
      Map<List<BlockPos>, Double> keyOf = new HashMap<>();
      Set<BlockPos> left = new HashSet<>(targets);
      left.removeAll(gone);

      for (BlockPos cell : heads) {
         if (!gone.contains(cell)) {
            VoxelShape shape = shapes.outline(cell);
            if (!shape.isEmpty()) {
               AABB box = shape.bounds().move(cell);

               for (Direction face : Direction.values()) {
                  if (init(eye, box, face)) {
                     for (double a : grid) {
                        for (double b : grid) {
                           Vec3 point = init(box, face, a, b);
                           Vec3 d = point.subtract(eye);
                           if (!(d.length() > reach) && !(d.lengthSqr() < 1.0E-8)) {
                              Vec3 dir = d.normalize();
                              double[] margin = new double[]{0.0};
                              List<BlockPos> chain = chainAlong(eye, dir, reach, left, gone, shapes, margin);
                              if (!chain.isEmpty() && heads.contains(chain.get(0))) {
                                 float ly = init(dir);
                                 float lp = load(dir);
                                 double key = Math.abs(init(ly - yaw)) + Math.abs(lp - pitch) - Math.min(margin[0], 0.3) * 10.0;
                                 Double had = keyOf.get(chain);
                                 if (had == null || key < had) {
                                    keyOf.put(chain, key);
                                    byChain.put(chain, new AiDigLine.Inner_DataUtil(ly, lp, List.copyOf(chain), margin[0]));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      List<AiDigLine.Inner_DataUtil> out = new ArrayList<>(byChain.values());
      out.sort(Comparator.comparingDouble(line -> keyOf.get(line.chain()) - line.chain().size() * 60.0));
      return out.size() > limit ? List.copyOf(out.subList(0, limit)) : out;
   }

   static List<BlockPos> chainAlong(
      Vec3 eye, Vec3 dir, double reach, Set<BlockPos> targets, Set<BlockPos> gone, AiDigLine.Inner_CachePool shapes, double[] marginOut
   ) {
      if (gone.isEmpty()) {
         return rayChain(eye, dir, reach, targets, shapes, marginOut);
      }

      Set<BlockPos> removed = new HashSet<>(gone);
      List<BlockPos> chain = new ArrayList<>();
      double min = 1.7976931348623157E308;

      while (chain.size() < targets.size()) {
         BlockHitResult hit = init(eye, dir, reach, removed, shapes);
         if (hit == null) {
            break;
         }

         BlockPos pos = hit.getBlockPos().immutable();
         if (!targets.contains(pos)) {
            break;
         }

         double m = init(hit, shapes.outline(pos).bounds().move(pos));
         if (m < 0.06) {
            break;
         }

         chain.add(pos);
         removed.add(pos);
         min = Math.min(min, m);
      }

      if (marginOut != null) {
         marginOut[0] = chain.isEmpty() ? 0.0 : min;
      }

      return chain;
   }

   public static BlockPos init(AiDigLine.Inner_DataUtil line, Vec3 eye, Collection<BlockPos> remaining, AiDigLine.Inner_CachePool shapes, double reach) {
      BlockHitResult hit = init(eye, line.direction(), reach, Set.of(), shapes);
      if (hit == null) {
         return null;
      } else {
         BlockPos pos = hit.getBlockPos().immutable();
         if (!remaining.contains(pos)) {
            return null;
         } else {
            return init(hit, shapes.outline(pos).bounds().move(pos)) >= 0.06 ? pos : null;
         }
      }
   }

   public static Vec3 init(AiDigLine.Inner_DataUtil line, Vec3 eye, AiDigLine.Inner_CachePool shapes, double reach) {
      Vec3 dir = line.direction();
      BlockHitResult hit = init(eye, dir, reach, Set.of(), shapes);
      double t = hit == null ? 2.0 : hit.getLocation().distanceTo(eye) + 0.02;
      return eye.add(dir.scale(t));
   }

   static boolean init(Vec3 eye, AABB box, Direction face) {
      double eps = 0.001;
      boolean var10000;
      switch (face) {
         case EAST:
            var10000 = eye.x > box.maxX + eps;
            break;
         case WEST:
            var10000 = eye.x < box.minX - eps;
            break;
         case UP:
            var10000 = eye.y > box.maxY + eps;
            break;
         case DOWN:
            var10000 = eye.y < box.minY - eps;
            break;
         case SOUTH:
            var10000 = eye.z > box.maxZ + eps;
            break;
         case NORTH:
            var10000 = eye.z < box.minZ - eps;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static Vec3 init(AABB box, Direction face, double a, double b) {
      double x = box.minX + (box.maxX - box.minX) * a;
      double y = box.minY + (box.maxY - box.minY) * a;
      double zb = box.minZ + (box.maxZ - box.minZ) * b;
      double yb = box.minY + (box.maxY - box.minY) * b;
      Vec3 var10000;
      switch (face) {
         case EAST:
            var10000 = new Vec3(box.maxX, y, zb);
            break;
         case WEST:
            var10000 = new Vec3(box.minX, y, zb);
            break;
         case UP:
            var10000 = new Vec3(x, box.maxY, zb);
            break;
         case DOWN:
            var10000 = new Vec3(x, box.minY, zb);
            break;
         case SOUTH:
            var10000 = new Vec3(x, yb, box.maxZ);
            break;
         case NORTH:
            var10000 = new Vec3(x, yb, box.minZ);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static double init(BlockHitResult hit, AABB box) {
      Vec3 p = hit.getLocation();
      double var10000;
      switch (hit.getDirection().getAxis()) {
         case X:
            var10000 = Math.min(Math.min(p.y - box.minY, box.maxY - p.y), Math.min(p.z - box.minZ, box.maxZ - p.z));
            break;
         case Y:
            var10000 = Math.min(Math.min(p.x - box.minX, box.maxX - p.x), Math.min(p.z - box.minZ, box.maxZ - p.z));
            break;
         case Z:
            var10000 = Math.min(Math.min(p.x - box.minX, box.maxX - p.x), Math.min(p.y - box.minY, box.maxY - p.y));
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public static float init(Vec3 dir) {
      return (float)(Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0);
   }

   public static float load(Vec3 dir) {
      return (float)(-Math.toDegrees(Math.atan2(dir.y, Math.hypot(dir.x, dir.z))));
   }

   static float init(float degrees) {
      float d = degrees % 360.0F;
      if (d >= 180.0F) {
         d -= 360.0F;
      }

      if (d < -180.0F) {
         d += 360.0F;
      }

      return d;
   }

   public interface Inner_CachePool {
      VoxelShape outline(BlockPos var1);
   }

   public record Inner_DataUtil(float yaw, float pitch, List<BlockPos> chain, double margin) {
      public Vec3 direction() {
         return Vec3.directionFromRotation(this.pitch, this.yaw);
      }
   }

   private record Inner_StreamUtil(List<AiDigLine.Inner_DataUtil> lines, Set<BlockPos> gone, float yaw, float pitch, double turn) {
      double score() {
         return this.turn - this.gone.size() * 60.0;
      }
   }
}
