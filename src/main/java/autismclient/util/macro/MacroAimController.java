package autismclient.util.macro;

import autismclient.util.RotationMath;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MacroAimController {
   private static volatile RotationMath.Rotation init_ce_DataUtil;
   private static final double[] init_DArr = new double[]{0.5, 0.35, 0.65, 0.2, 0.8};
   private static final double init_D = 0.12;

   private MacroAimController() {
   }

   public static RotationMath.Rotation init_ce_DataUtil() {
      return init_ce_DataUtil;
   }

   public static boolean isActive() {
      return init_ce_DataUtil != null;
   }

   public static void setRotation(RotationMath.Rotation rotation) {
      init_ce_DataUtil = rotation;
   }

   public static void clear() {
      init_ce_DataUtil = null;
   }

   public static RotationMath.Rotation rotationTo(Minecraft mc, AimTargetAction.AimTarget target) {
      if (mc != null && mc.player != null && mc.level != null && target != null) {
         Vec3 eyes = mc.player.getEyePosition();
         if (target.entity() != null) {
            return aimAtEntity(mc, eyes, target.entity());
         } else if (target.blockPos() != null) {
            return aimAtBlock(mc, eyes, target.blockPos());
         } else {
            return target.point() == null ? null : RotationMath.lookAt(target.point(), eyes);
         }
      } else {
         return null;
      }
   }

   private static RotationMath.Rotation aimAtBlock(Minecraft mc, Vec3 eyes, BlockPos pos) {
      double reachSq = square(mc.player.blockInteractionRange());
      AABB box = blockBox(mc, pos);
      RotationMath.Rotation best = null;
      double bestDistanceSq = 1.0 / 0.0;

      for (Vec3 point : samplePoints(box)) {
         double distanceSq = eyes.distanceToSqr(point);
         if (!(distanceSq > reachSq) && !(distanceSq >= bestDistanceSq)) {
            BlockHitResult hit = mc.level.clip(new ClipContext(eyes, point, Block.OUTLINE, Fluid.NONE, mc.player));
            if (hit != null && hit.getType() == Type.BLOCK && hit.getBlockPos().equals(pos)) {
               best = RotationMath.lookAt(point, eyes);
               bestDistanceSq = distanceSq;
            }
         }
      }

      return best;
   }

   private static RotationMath.Rotation aimAtEntity(Minecraft mc, Vec3 eyes, Entity entity) {
      double reachSq = square(mc.player.entityInteractionRange());
      AABB box = entity.getBoundingBox();
      if (!(box.getXsize() <= 0.0) && !(box.getYsize() <= 0.0) && !(box.getZsize() <= 0.0)) {
         for (Vec3 point : samplePoints(box)) {
            if (!(eyes.distanceToSqr(point) > reachSq) && isVisible(mc, eyes, point)) {
               return RotationMath.lookAt(point, eyes);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean isVisible(Minecraft mc, Vec3 eyes, Vec3 point) {
      BlockHitResult hit = mc.level.clip(new ClipContext(eyes, point, Block.COLLIDER, Fluid.NONE, mc.player));
      return hit == null || hit.getType() == Type.MISS;
   }

   private static AABB blockBox(Minecraft mc, BlockPos pos) {
      VoxelShape shape = mc.level.getBlockState(pos).getShape(mc.level, pos);
      AABB local = shape.isEmpty() ? new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0) : shape.bounds();
      return local.move(pos.getX(), pos.getY(), pos.getZ());
   }

   private static List<Vec3> samplePoints(AABB box) {
      double insetX = Math.min(0.12, box.getXsize() / 4.0);
      double insetY = Math.min(0.12, box.getYsize() / 4.0);
      double insetZ = Math.min(0.12, box.getZsize() / 4.0);
      AABB inner = box.contract(0.0, 0.0, 0.0).inflate(-insetX, -insetY, -insetZ);
      if (inner.getXsize() <= 0.0 || inner.getYsize() <= 0.0 || inner.getZsize() <= 0.0) {
         inner = box;
      }

      List<Vec3> points = new ArrayList<>(init_DArr.length * init_DArr.length * init_DArr.length);

      for (double y : init_DArr) {
         for (double x : init_DArr) {
            for (double z : init_DArr) {
               points.add(new Vec3(inner.minX + inner.getXsize() * x, inner.minY + inner.getYsize() * y, inner.minZ + inner.getZsize() * z));
            }
         }
      }

      return points;
   }

   private static double square(double value) {
      return value * value;
   }

   public static float yaw(LocalPlayer player, float vanillaYaw) {
      RotationMath.Rotation rotation = activeRotationFor(player);
      return rotation == null ? vanillaYaw : rotation.yaw();
   }

   public static float pitch(LocalPlayer player, float vanillaPitch) {
      RotationMath.Rotation rotation = activeRotationFor(player);
      return rotation == null ? vanillaPitch : rotation.pitch();
   }

   public static Vec3 viewVector(LocalPlayer player, Vec3 vanillaVector) {
      RotationMath.Rotation rotation = activeRotationFor(player);
      return rotation == null ? vanillaVector : Vec3.directionFromRotation(rotation.pitch(), rotation.yaw());
   }

   private static RotationMath.Rotation activeRotationFor(LocalPlayer player) {
      Minecraft mc = Minecraft.getInstance();
      return player != null && mc != null && player == mc.player ? init_ce_DataUtil : null;
   }
}
