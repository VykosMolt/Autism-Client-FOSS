package autismclient.ai.a;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiBlockInteraction {
   private AiBlockInteraction() {
   }

   public static BlockHitResult init(Minecraft mc, BlockPos target) {
      return mc.player == null ? null : init(mc, target, mc.player.getEyePosition());
   }

   public static BlockHitResult init(Minecraft mc, BlockPos target, Direction face) {
      return mc.player != null && mc.level != null ? init(mc, target, face, mc.player.getEyePosition()) : null;
   }

   static BlockHitResult load(Minecraft mc, BlockPos target, Direction face) {
      BlockHitResult current = init(mc, target, face);
      if (current != null && mc.player.onClimbable() && !mc.player.onGround()) {
         return init(mc, target, face, init(mc)) != null ? current : null;
      } else {
         return current;
      }
   }

   static Vec3 init(Minecraft mc) {
      Vec3 eye = mc.player.getEyePosition();
      double y = init(eye.y, mc.player.getY(), mc.player.getDimensions(Pose.CROUCHING).eyeHeight(), mc.player.onClimbable(), mc.player.onGround());
      return new Vec3(eye.x, y, eye.z);
   }

   static double init(double actual, double feet, double crouchedHeight, boolean climbable, boolean grounded) {
      return climbable && !grounded ? Math.min(actual, feet + crouchedHeight) - 0.15 : actual;
   }

   private static BlockHitResult init(Minecraft mc, BlockPos target, Direction face, Vec3 eye) {
      if (face == null) {
         return init(mc, target, eye);
      }

      ArrayList<Vec3> points = new ArrayList<>();

      for (AABB box : mc.level.getBlockState(target).getShape(mc.level, target).toAabbs()) {
         points.addAll(init(box, target, face));
      }

      points.sort(Comparator.comparingDouble(eye::distanceToSqr));

      for (Vec3 point : points) {
         BlockHitResult hit = init(mc, target, point, eye);
         if (hit != null && hit.getDirection() == face) {
            return hit;
         }
      }

      return null;
   }

   static List<Vec3> init(AABB box, BlockPos target, Direction face) {
      ArrayList<Vec3> points = new ArrayList<>(9);

      for (double a : new double[]{0.2, 0.5, 0.8}) {
         for (double b : new double[]{0.2, 0.5, 0.8}) {
            double x = box.minX + (box.maxX - box.minX) * a;
            double y = box.minY + (box.maxY - box.minY) * a;
            double z = box.minZ + (box.maxZ - box.minZ) * b;
            switch (face.getAxis()) {
               case Y:
                  y = face == Direction.UP ? box.maxY - 0.001 : box.minY + 0.001;
                  break;
               case X:
                  x = face == Direction.EAST ? box.maxX - 0.001 : box.minX + 0.001;
                  break;
               case Z:
                  z = face == Direction.SOUTH ? box.maxZ - 0.001 : box.minZ + 0.001;
                  x = box.minX + (box.maxX - box.minX) * b;
            }

            points.add(new Vec3(target.getX() + x, target.getY() + y, target.getZ() + z));
         }
      }

      return points;
   }

   public static BlockHitResult init(Minecraft mc, BlockPos target, Vec3 eye) {
      if (mc.player != null && mc.level != null && mc.level.getChunkSource().getChunk(target.getX() >> 4, target.getZ() >> 4, ChunkStatus.FULL, false) != null) {
         VoxelShape shape = mc.level.getBlockState(target).getShape(mc.level, target);
         if (shape.isEmpty()) {
            return null;
         }

         AABB bounds = shape.bounds();
         double reach = mc.player.blockInteractionRange();
         if (!(eye.x < target.getX() + bounds.minX - reach)
            && !(eye.x > target.getX() + bounds.maxX + reach)
            && !(eye.y < target.getY() + bounds.minY - reach)
            && !(eye.y > target.getY() + bounds.maxY + reach)
            && !(eye.z < target.getZ() + bounds.minZ - reach)
            && !(eye.z > target.getZ() + bounds.maxZ + reach)) {
            Vec3 center = bounds.getCenter().add(target.getX(), target.getY(), target.getZ());
            BlockHitResult hit = init(mc, target, center, eye);
            if (hit != null) {
               return hit;
            }

            for (AABB box : shape.toAabbs()) {
               Vec3 c = box.getCenter().add(target.getX(), target.getY(), target.getZ());

               for (Direction face : Direction.values()) {
                  Vec3 point = c.add(
                     face.getStepX() * (box.maxX - box.minX) * 0.49,
                     face.getStepY() * (box.maxY - box.minY) * 0.49,
                     face.getStepZ() * (box.maxZ - box.minZ) * 0.49
                  );
                  hit = init(mc, target, point, eye);
                  if (hit != null) {
                     return hit;
                  }
               }
            }

            for (AABB box : shape.toAabbs()) {
               for (Direction face : Direction.values()) {
                  for (Vec3 point : init(box, target, face)) {
                     hit = init(mc, target, point, eye);
                     if (hit != null) {
                        return hit;
                     }
                  }
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static Vec3 init(Vec3 eye, Vec3 entry, Vec3 middle, Predicate<Vec3> visible) {
      if (visible.test(middle)) {
         return middle;
      }

      Vec3 towardMiddle = middle.subtract(entry);

      for (double fraction = 0.25; fraction >= 0.00390625; fraction *= 0.5) {
         Vec3 point = entry.add(towardMiddle.scale(fraction));
         if (visible.test(point)) {
            return point;
         }
      }

      return entry.add(entry.subtract(eye).normalize().scale(0.02));
   }

   private static BlockHitResult init(Minecraft mc, BlockPos target, Vec3 point, Vec3 eye) {
      BlockHitResult hit = mc.level.clip(new ClipContext(eye, point, Block.OUTLINE, Fluid.NONE, mc.player));
      return init(eye, target, hit, mc.player.blockInteractionRange()) ? hit : null;
   }

   static boolean init(Vec3 eye, BlockPos target, BlockHitResult hit, double reach) {
      return hit != null && hit.getType() == Type.BLOCK && hit.getBlockPos().equals(target) && eye.distanceToSqr(hit.getLocation()) <= reach * reach;
   }
}
