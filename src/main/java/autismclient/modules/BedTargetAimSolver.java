package autismclient.modules;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class BedTargetAimSolver {
   private BedTargetAimSolver() {
   }

   static Vec3 init(
      autismclient.ai.engine.AiPhysics.MotionState state,
      autismclient.ai.engine.AiPhysics.MoveInput input,
      autismclient.ai.engine.AiPhysics.PhysicsWorld world,
      double eyeHeight
   ) {
      return autismclient.ai.engine.AiPhysics.stepCompat(state, input, world).position().add(0.0, eyeHeight, 0.0);
   }

   static boolean init(List<AABB> boxes, Vec3 eye, double reach) {
      return boxes.stream().anyMatch(box -> box.distanceToSqr(eye) <= reach * reach);
   }

   static Vec3 visibleAimPoint(List<AABB> boxes, List<AABB> blockers, Vec3 eye, double reach, Vec3 preferred) {
      for (AABB box : boxes) {
         if (init_Z(boxes, blockers, eye, reach, box.getCenter())) {
            return box.getCenter();
         }
      }

      LinkedHashSet<Vec3> points = new LinkedHashSet<>();

      for (AABB box : boxes) {
         points.add(
            new Vec3(
               Math.clamp(eye.x, box.minX + 1.0E-6, box.maxX - 1.0E-6),
               Math.clamp(eye.y, box.minY + 1.0E-6, box.maxY - 1.0E-6),
               Math.clamp(eye.z, box.minZ + 1.0E-6, box.maxZ - 1.0E-6)
            )
         );

         for (Direction side : Direction.values()) {
            points.addAll(FaceVisibilitySolver.visiblePoints(eye, box, side, blockers, preferred));
         }
      }

      return points.stream().filter(p -> init_Z(boxes, blockers, eye, reach, p)).min(Comparator.comparingDouble(p -> p.distanceToSqr(preferred))).orElse(null);
   }

   private static boolean init_Z(List<AABB> boxes, List<AABB> blockers, Vec3 eye, double reach, Vec3 point) {
      Vec3 delta = point.subtract(eye);
      if (delta.lengthSqr() < 1.0E-12) {
         return boxes.stream().anyMatch(b -> b.contains(eye));
      }

      Vec3 end = eye.add(delta.normalize().scale(reach));
      double target = init(boxes, eye, end);
      return Double.isFinite(target) && init(blockers, eye, end) > target + 1.0E-9;
   }

   private static double init(List<AABB> boxes, Vec3 eye, Vec3 end) {
      double closest = 1.0 / 0.0;

      for (AABB box : boxes) {
         if (box.contains(eye)) {
            return 0.0;
         }

         Optional<Vec3> hit = box.clip(eye, end);
         if (hit.isPresent()) {
            closest = Math.min(closest, eye.distanceToSqr(hit.get()));
         }
      }

      return closest;
   }
}
