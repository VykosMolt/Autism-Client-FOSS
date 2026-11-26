package autismclient.ai.engine;

import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AiPhysics {
   public static final double putAt_D = 0.6;
   public static final double fill_D = 1.8;
   public static final double readEx_D = 1.5;
   public static final double value_D = 0.6;
   public static final double prevEx_D = 0.6;
   public static final int flowState = 0;
   public static final int flowTemp = 1;
   public static final int readEx_I = 2;
   public static final int value_I = 3;
   public static final int prevEx_I = 4;
   public static final double prev_D = 0.08;
   public static final double chunkAt_D = 0.42;
   public static final double state = 0.2;
   public static final double closeOf = 0.1;
   public static final double pollAt = 1.3;
   public static final double pollOf = 0.02;
   public static final double tick = 0.026;
   public static final double flushOf = 0.6;
   public static final double stateOf = 0.98;
   public static final double store = 0.8;
   private static final double nextBy = 1.0E-7;
   public static final int prev_I = 1;
   public static final int chunkAt_I = 2;
   private static final ThreadLocal<AiPhysics.ScratchBuffers> SCRATCH_BUFFERS = ThreadLocal.withInitial(AiPhysics.ScratchBuffers::new);
   private static final double MINOR_COLLISION_COS_LIMIT = Math.cos(0.13962633907794952);

   private AiPhysics() {
   }

   public static AiPhysics.PhysicsWorld liveWorld(ClientLevel var0, Entity var1) {
      return new AiLiveWorld(var0, var1);
   }

   static boolean hitsHazard(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2) {
      return checkHazardSweep(var0, var1, var2, false);
   }

   static boolean hitsStrictHazard(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2) {
      return checkHazardSweep(var0, var1, var2, true);
   }

   static int[] findHazardCell(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2) {
      double var3 = 0.33999999999999997;
      double var5 = Math.min(var1.x(), var2.x()) - var3;
      double var7 = Math.max(var1.x(), var2.x()) + var3;
      double var9 = Math.min(var1.y(), var2.y());
      double var11 = var9 - 1.0E-5;
      double var13 = Math.max(var1.y(), var2.y()) + 1.8;
      int var15 = (int)Math.floor(var9 + 1.0E-5);
      double var16 = Math.min(var1.z(), var2.z()) - var3;
      double var18 = Math.max(var1.z(), var2.z()) + var3;

      for (int var20 = (int)Math.floor(var5); var20 <= (int)Math.floor(var7 - 1.0E-5); var20++) {
         for (int var21 = (int)Math.floor(var11); var21 <= (int)Math.floor(var13 - 1.0E-5); var21++) {
            int var22 = (int)Math.floor(var16);

            while (var22 <= (int)Math.floor(var18 - 1.0E-5)) {
               boolean var23 = bodyOverlapsCell(var1, var20, var21, var22);
               if (var23 || (var21 < var15 ? !var0.chunkAt(var20, var21, var22) : !var0.prev(var20, var21, var22))) {
                  if (var21 < var15 || !var0.putAt_Z_I_2(var20, var21, var22) || var23 && !bodyOverlapsCellStrict(var2, var20, var21, var22)) {
                     var22++;
                     continue;
                  }

                  return new int[]{var20, var21, var22, 0};
               }

               return new int[]{var20, var21, var22, 1};
            }
         }
      }

      double var35 = Math.min(var1.x(), var2.x()) - 0.04;
      double var36 = Math.max(var1.x(), var2.x()) + 0.04;
      double var24 = Math.min(var1.y(), var2.y()) + 0.01;
      double var26 = Math.max(var1.y(), var2.y()) + 0.06;
      double var28 = Math.min(var1.z(), var2.z()) - 0.04;
      double var30 = Math.max(var1.z(), var2.z()) + 0.04;

      for (int var32 = (int)Math.floor(var35); var32 <= (int)Math.floor(var36 - 1.0E-5); var32++) {
         for (int var33 = (int)Math.floor(var24); var33 <= (int)Math.floor(var26 - 1.0E-5); var33++) {
            for (int var34 = (int)Math.floor(var28); var34 <= (int)Math.floor(var30 - 1.0E-5); var34++) {
               if (var0.readEx_Z(var32, var33, var34)
                  && (!feetInsideCell(var1, var32, var33, var34) || feetInsideCell(var2, var32, var33, var34))) {
                  return new int[]{var32, var33, var34, 1};
               }
            }
         }
      }

      return null;
   }

   private static boolean feetInsideCell(AiPhysics.MotionState var0, int var1, int var2, int var3) {
      return var1 >= (int)Math.floor(var0.x() - 0.04)
         && var1 <= (int)Math.floor(var0.x() + 0.04 - 1.0E-5)
         && var3 >= (int)Math.floor(var0.z() - 0.04)
         && var3 <= (int)Math.floor(var0.z() + 0.04 - 1.0E-5)
         && var2 >= (int)Math.floor(var0.y() + 0.01)
         && var2 <= (int)Math.floor(var0.y() + 0.06 - 1.0E-5);
   }

   private static boolean bodyOverlapsCellStrict(AiPhysics.MotionState var0, int var1, int var2, int var3) {
      double var4 = 0.33999999999999997;
      return var1 >= (int)Math.floor(var0.x() - var4)
         && var1 <= (int)Math.floor(var0.x() + var4 - 1.0E-5)
         && var3 >= (int)Math.floor(var0.z() - var4)
         && var3 <= (int)Math.floor(var0.z() + var4 - 1.0E-5)
         && var2 >= (int)Math.floor(var0.y() - 0.001)
         && var2 <= (int)Math.floor(var0.y() + 1.8 - 1.0E-5);
   }

   private static boolean bodyOverlapsCell(AiPhysics.MotionState var0, int var1, int var2, int var3) {
      double var4 = 0.33999999999999997;
      return var1 >= (int)Math.floor(var0.x() - var4)
         && var1 <= (int)Math.floor(var0.x() + var4 - 1.0E-5)
         && var3 >= (int)Math.floor(var0.z() - var4)
         && var3 <= (int)Math.floor(var0.z() + var4 - 1.0E-5)
         && var2 >= (int)Math.floor(var0.y() - 0.001)
         && var2 <= (int)Math.floor(var0.y() + 1.8 - 1.0E-5);
   }

   private static boolean checkHazardSweep(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2, boolean var3
   ) {
      double var4 = 0.33999999999999997;
      double var6 = Math.min(var1.x(), var2.x()) - var4;
      double var8 = Math.max(var1.x(), var2.x()) + var4;
      double var10 = Math.min(var1.y(), var2.y());
      double var12 = var10 - 1.0E-5;
      double var14 = Math.max(var1.y(), var2.y()) + 1.8;
      int var16 = (int)Math.floor(var10 + 1.0E-5);
      double var17 = Math.min(var1.z(), var2.z()) - var4;
      double var19 = Math.max(var1.z(), var2.z()) + var4;
      int var21 = (int)Math.floor(var6);
      int var22 = (int)Math.floor(var8 - 1.0E-5);
      int var23 = (int)Math.floor(var12);
      int var24 = (int)Math.floor(var14 - 1.0E-5);
      int var25 = (int)Math.floor(var17);
      int var26 = (int)Math.floor(var19 - 1.0E-5);
      if (var0.putAt(var21, var23, var25, var22, var24, var26, var16)) {
         return false;
      }

      for (int var27 = var21; var27 <= var22; var27++) {
         for (int var28 = var23; var28 <= var24; var28++) {
            for (int var29 = var25; var29 <= var26; var29++) {
               if ((var28 < var16 ? var0.chunkAt(var27, var28, var29) : var0.prev(var27, var28, var29)) && (var3 || !bodyOverlapsCell(var1, var27, var28, var29))) {
                  return true;
               }

               if (var28 >= var16) {
                  if (var0.putAt_Z_I_2(var27, var28, var29) && (!bodyOverlapsCell(var1, var27, var28, var29) || bodyOverlapsCellStrict(var2, var27, var28, var29))) {
                     return true;
                  }

                  if (var3 && var0.fill_Z(var27, var28, var29)) {
                     return true;
                  }
               }
            }
         }
      }

      double var42 = Math.min(var1.x(), var2.x()) - 0.04;
      double var43 = Math.max(var1.x(), var2.x()) + 0.04;
      double var31 = Math.min(var1.y(), var2.y()) + 0.01;
      double var33 = Math.max(var1.y(), var2.y()) + 0.06;
      double var35 = Math.min(var1.z(), var2.z()) - 0.04;
      double var37 = Math.max(var1.z(), var2.z()) + 0.04;

      for (int var39 = (int)Math.floor(var42); var39 <= (int)Math.floor(var43 - 1.0E-5); var39++) {
         for (int var40 = (int)Math.floor(var31); var40 <= (int)Math.floor(var33 - 1.0E-5); var40++) {
            for (int var41 = (int)Math.floor(var35); var41 <= (int)Math.floor(var37 - 1.0E-5); var41++) {
               if (var0.readEx_Z(var39, var40, var41)
                  && (var3 || !feetInsideCell(var1, var39, var40, var41) || feetInsideCell(var2, var39, var40, var41))) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   static double stuckHorizontalScale(int var0) {
      return var0 == 1 ? 0.25 : (var0 == 2 ? 0.8 : 1.0);
   }

   static double stuckVerticalScale(int var0) {
      return var0 == 1 ? 0.05 : (var0 == 2 ? 0.75 : 1.0);
   }

   public static AiPhysics.MotionState step(AiPhysics.MotionState var0, AiPhysics.MoveInput var1, AiPhysics.PhysicsWorld var2) {
      return stepInternal(var0, var1, var2, true, false);
   }

   static AiPhysics.MotionState stepWithClimbable(AiPhysics.MotionState var0, AiPhysics.MoveInput var1, AiPhysics.PhysicsWorld var2) {
      return stepInternal(var0, var1, var2, true, true);
   }

   public static AiPhysics.MotionState stepUnclipped(AiPhysics.MotionState var0, AiPhysics.MoveInput var1, AiPhysics.PhysicsWorld var2) {
      return stepInternal(var0, var1, var2, false, false);
   }

   private static AiPhysics.MotionState stepInternal(
      AiPhysics.MotionState var0, AiPhysics.MoveInput var1, AiPhysics.PhysicsWorld var2, boolean var3, boolean var4
   ) {
      double var5 = 0.3;
      double var7 = var0.pose() == 2 ? 0.6 : (var0.pose() != 0 ? 1.5 : 1.8);
      double var9 = var0.x() - var5;
      double var11 = var0.y();
      double var13 = var0.z() - var5;
      double var15 = var0.x() + var5;
      double var17 = var0.y() + var7;
      double var19 = var0.z() + var5;
      long var21 = var0.onGround()
         ? (var0.support() != -9223372036854775808L ? var0.support() : findSupportBlockBounds(var9, var11, var13, var15, var19, var0.x(), var0.y(), var0.z(), var2))
         : -9223372036854775808L;
      int var23 = Math.max(0, var0.jumpDelay() - 1);
      double var24 = var0.vx();
      double var26 = var0.vy();
      double var28 = var0.vz();
      if (var24 * var24 + var28 * var28 < 9.0E-6) {
         var24 = 0.0;
         var28 = 0.0;
      }

      if (Math.abs(var26) < 0.003) {
         var26 = 0.0;
      }

      boolean var30 = var0.pose() != 0;
      boolean var31 = var1.sprint() && var1.forward() > 0.8 && !var1.sneak() && !var30 && !var0.sprintCut();
      double var32 = 0.0;
      double var34 = 0.0;
      boolean var36 = false;
      if (var1.jump()) {
         if (var0.onGround() && var23 == 0) {
            double var37 = var2.value_D((int)Math.floor(var0.x()), (int)Math.floor(var0.y()), (int)Math.floor(var0.z()));
            if (var37 == 1.0) {
               var37 = var2.value_D(supportBlockX(var21, var0.x()), (int)Math.floor(var0.y() - 0.5000001), supportBlockZ(var21, var0.z()));
            }

            var26 = Math.max(0.42 * var37, var26);
            if (var31) {
               double var39 = Math.toRadians(var1.yaw());
               var32 = Math.sin(var39);
               var34 = Math.cos(var39);
               var36 = true;
               var24 += -var32 * 0.2;
               var28 += var34 * 0.2;
            }

            var23 = 10;
         }
      } else {
         var23 = 0;
      }

      int var109 = supportBlockX(var21, var0.x());
      int var38 = (int)Math.floor(var0.y() - 0.5000001);
      int var110 = supportBlockZ(var21, var0.z());
      double var40 = var0.onGround() ? var2.putAt_D_I(var109, var38, var110) : 0.6;
      double var42 = var0.onGround() ? var40 * 0.91 : 0.91;
      double var44 = var0.onGround() ? var2.movementSpeed() * (var31 ? 1.3 : 1.0) * (0.21600002 / (var40 * var40 * var40)) : (var31 ? 0.026 : 0.02);
      double var46 = var1.forward() * 0.98;
      double var48 = var1.strafe() * 0.98;
      if (var0.pose() != 0) {
         var46 *= 0.3;
         var48 *= 0.3;
      }

      double var50 = var46 * var46 + var48 * var48;
      if (var50 >= 1.0E-7) {
         double var52 = var50 > 1.0 ? var44 / Math.sqrt(var50) : var44;
         double var54 = var48 * var52;
         double var56 = var46 * var52;
         if (!var36) {
            double var58 = Math.toRadians(var1.yaw());
            var32 = Math.sin(var58);
            var34 = Math.cos(var58);
         }

         var24 += var54 * var34 - var56 * var32;
         var28 += var56 * var34 + var54 * var32;
      }

      if (var4 && var2.state((int)Math.floor(var0.x()), (int)Math.floor(var0.y()), (int)Math.floor(var0.z()))) {
         var24 = Math.clamp(var24, -0.15, 0.15);
         var28 = Math.clamp(var28, -0.15, 0.15);
         var26 = Math.max(var1.sneak() ? 0.0 : -0.15, var26);
      }

      double var111 = var24;
      double var112 = var26;
      double var113 = var28;
      if (var0.stuck() != 0) {
         double var114 = stuckHorizontalScale(var0.stuck());
         var111 = var24 * var114;
         var112 = var26 * stuckVerticalScale(var0.stuck());
         var113 = var28 * var114;
      }

      AiPlannerWorld var115 = var2 instanceof AiPlannerWorld var59 ? var59 : null;
      double[] var60;
      float[] var61;
      double[] var116;
      if (var115 != null) {
         var116 = var115.putAt_DArr;
         var60 = var115.fill_DArr;
         var61 = var115.putAt_FArr;
      } else {
         AiPhysics.ScratchBuffers var62 = SCRATCH_BUFFERS.get();
         var116 = var62.moveResult;
         var60 = var62.stepCandidate;
         var61 = var62.stepHeights;
      }

      if (var3 && var1.sneak() && var0.onGround() && var112 <= 0.0) {
         clipSneakEdge(var116, var111, var112, var113, var9, var11, var13, var15, var19, var2);
         var111 = var116[0];
         var112 = var116[1];
         var113 = var116[2];
      }

      long var117 = collideAndStepRaw(var116, var60, var61, var115, var111, var112, var113, var9, var11, var13, var15, var17, var19, var0.onGround(), var2);
      double var64 = var116[0];
      double var66 = var116[1];
      double var68 = var116[2];
      double var70 = var0.x() + var64;
      double var72 = var0.y() + var66;
      double var74 = var0.z() + var68;
      boolean var76 = !isApproxEqual(var111, var64);
      boolean var77 = !isApproxEqual(var113, var68);
      boolean var78 = !isApproxEqual(var112, var66);
      boolean var79 = var78 && var112 < 0.0;
      if (var76) {
         var24 = 0.0;
      }

      if (var77) {
         var28 = 0.0;
      }

      long var80 = -9223372036854775808L;
      if (var79) {
         double var82 = var70 - var5;
         double var84 = var70 + var5;
         double var86 = var74 - var5;
         double var88 = var74 + var5;
         var80 = findSupportBlockBounds(var82, var72, var86, var84, var88, var70, var72, var74, var2);
         if (var80 == -9223372036854775808L) {
            var80 = findSupportBlockBounds(var82 - var64, var72, var86 - var68, var84 - var64, var88 - var68, var70, var72, var74, var2);
         }
      }

      int var118 = supportBlockX(var80, var70);
      int var83 = (int)Math.floor(var72 - 0.2);
      int var119 = supportBlockZ(var80, var74);
      double var85 = 0.0;
      boolean var87 = false;
      if (var78) {
         if (var79 && !var1.sneak() && -var112 >= 0.08) {
            var85 = var2.readEx_D(var118, var83, var119);
            var87 = true;
         }

         if (var85 > 0.0) {
            double var120 = var66 / var112;
            var26 = (var120 * 0.08 - var112) * (1.0 + var120 * -0.020000000000000018) * var85;
         } else {
            var26 = 0.0;
         }
      }

      if (var4 && (var76 || var77 || var1.jump()) && var2.state((int)Math.floor(var70), (int)Math.floor(var72), (int)Math.floor(var74))) {
         var26 = 0.2;
      }

      if (var79 && !var1.sneak() && Math.abs(var26) < 0.1) {
         double var121 = var87 ? var85 : var2.readEx_D(var118, var83, var119);
         if (var121 >= 0.9) {
            double var90 = 0.4 + Math.abs(var26) * 0.2;
            var24 *= var90;
            var28 *= var90;
         }
      }

      double var122 = var2.fill_D((int)Math.floor(var70), (int)Math.floor(var72), (int)Math.floor(var74));
      if (var122 == 1.0) {
         var122 = var2.fill_D(supportBlockX(var80, var70), (int)Math.floor(var72 - 0.5000001), supportBlockZ(var80, var74));
      }

      var24 *= var122;
      var28 *= var122;
      if (var0.stuck() != 0) {
         var24 = 0.0;
         var26 = 0.0;
         var28 = 0.0;
      }

      var26 -= 0.08;
      var24 *= var42;
      var26 *= 0.98;
      var28 *= var42;
      byte var123 = 0;
      if (var2.putAt_Z()) {
         double var91 = var9 + Math.min(0.0, var64) + 1.0E-5;
         double var93 = var15 + Math.max(0.0, var64) - 1.0E-5;
         double var95 = var11 + Math.min(0.0, var66) + 1.0E-5;
         double var97 = var17 + Math.max(0.0, var66) - 1.0E-5;
         double var99 = var13 + Math.min(0.0, var68) + 1.0E-5;
         double var101 = var19 + Math.max(0.0, var68) - 1.0E-5;
         var123 = (byte)(var115 != null
            ? var115.putAt_I_D(var91, var95, var99, var93, var97, var101)
            : var2.putAt_I_AABB(new AABB(var91, var95, var99, var93, var97, var101)));
      }

      int var124 = var0.pose();
      double var92 = var70 - var5 + 1.0E-7;
      double var94 = var70 + var5 - 1.0E-7;
      double var96 = var74 - var5 + 1.0E-7;
      double var98 = var74 + var5 - 1.0E-7;
      double var100 = var1.sneak() ? 1.5 : 1.8;
      if (var117 >= 0L && var7 >= var100 && var115 != null) {
         AABB[] var102 = var115.putAt_AABBArr();
         if (var117 != 0L && (var102 == null || intersectsAnyBitmask(var102, var117, var92, var72 + 1.0E-7, var96, var94, var72 + var100 - 1.0E-7, var98))) {
            if (var102 != null) {
               if (!var1.sneak() && !intersectsAnyBitmask(var102, var117, var92, var72 + 1.0E-7, var96, var94, var72 + 1.5 - 1.0E-7, var98)) {
                  var124 = 1;
               } else if (!intersectsAnyBitmask(var102, var117, var92, var72 + 1.0E-7, var96, var94, var72 + 0.6 - 1.0E-7, var98)) {
                  var124 = 2;
               }
            } else {
               var124 = resolveNextPose(var2, var1, var79, var124, var92, var72, var96, var94, var98, var100);
            }
         } else {
            var124 = var1.sneak() ? (var79 ? 4 : 1) : 0;
         }
      } else {
         var124 = resolveNextPose(var2, var1, var79, var124, var92, var72, var96, var94, var98, var100);
      }

      return new AiPhysics.MotionState(
         var70, var72, var74, var24, var26, var28, var79, var23, var76 || var77, var78, var80, (var76 || var77) && !isMinorHorizontalCollisionDelta(var1, var64, var68), var124, var123
      );
   }

   private static int resolveNextPose(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MoveInput var1,
      boolean var2,
      int var3,
      double var4,
      double var6,
      double var8,
      double var10,
      double var12,
      double var14
   ) {
      if (!var0.putAt_Z_D(var4, var6 + 1.0E-7, var8, var10, var6 + var14 - 1.0E-7, var12)) {
         return var1.sneak() ? (var2 ? 4 : 1) : 0;
      } else if (!var1.sneak() && !var0.putAt_Z_D(var4, var6 + 1.0E-7, var8, var10, var6 + 1.5 - 1.0E-7, var12)) {
         return 1;
      } else {
         return !var0.putAt_Z_D(var4, var6 + 1.0E-7, var8, var10, var6 + 0.6 - 1.0E-7, var12) ? 2 : var3;
      }
   }

   private static boolean intersectsAnyBitmask(AABB[] var0, long var1, double var3, double var5, double var7, double var9, double var11, double var13) {
      while (var1 != 0L) {
         if (var0[Long.numberOfTrailingZeros(var1)].intersects(var3, var5, var7, var9, var11, var13)) {
            return true;
         }

         var1 &= var1 - 1L;
      }

      return false;
   }

   static boolean isMinorHorizontalCollision(AiPhysics.MoveInput var0, Vec3 var1) {
      return isMinorHorizontalCollisionDelta(var0, var1.x, var1.z);
   }

   private static boolean isMinorHorizontalCollisionDelta(AiPhysics.MoveInput var0, double var1, double var3) {
      float var5 = var0.yaw() * 0.017453292F;
      double var6 = Math.sin(var5);
      double var8 = Math.cos(var5);
      double var10 = var0.strafe() * var8 - var0.forward() * var6;
      double var12 = var0.forward() * var8 + var0.strafe() * var6;
      double var14 = var10 * var10 + var12 * var12;
      double var16 = var1 * var1 + var3 * var3;
      if (!(var14 < 9.999999747378752E-6) && !(var16 < 9.999999747378752E-6)) {
         double var18 = (var10 * var1 + var12 * var3) / Math.sqrt(var14 * var16);
         return var18 > MINOR_COLLISION_COS_LIMIT && var18 <= 1.0;
      } else {
         return false;
      }
   }

   static int supportBlockX(long var0, double var2) {
      return var0 == -9223372036854775808L ? (int)Math.floor(var2) : BlockPos.getX(var0);
   }

   static int supportBlockZ(long var0, double var2) {
      return var0 == -9223372036854775808L ? (int)Math.floor(var2) : BlockPos.getZ(var0);
   }

   static long findSupportBlock(AABB var0, double var1, double var3, double var5, AiPhysics.PhysicsWorld var7) {
      return findSupportBlockBounds(var0.minX, var0.minY, var0.minZ, var0.maxX, var0.maxZ, var1, var3, var5, var7);
   }

   static long findSupportBlockBounds(double var0, double var2, double var4, double var6, double var8, double var10, double var12, double var14, AiPhysics.PhysicsWorld var16) {
      return var16.putAt(var0, var2 - 1.0E-6, var4, var6, var2, var8, var10, var12, var14);
   }

   static long findSupportInBoxes(AABB var0, double var1, double var3, double var5, List<AABB> var7, AABB[] var8) {
      return findSupportInBoxesRaw(var0.minX, var0.minY, var0.minZ, var0.maxX, var0.maxY, var0.maxZ, var1, var3, var5, var7, var8);
   }

   static long findSupportInBoxesRaw(
      double var0, double var2, double var4, double var6, double var8, double var10, double var12, double var14, double var16, List<AABB> var18, AABB[] var19
   ) {
      long var20 = -9223372036854775808L;
      double var22 = 1.7976931348623157E308;
      int var24 = 0;
      int var25 = 0;
      int var26 = 0;
      int var27 = var19 == null ? var18.size() : var19.length;

      for (int var28 = 0; var28 < var27; var28++) {
         AABB var29 = var19 == null ? (AABB)var18.get(var28) : var19[var28];
         if (var29.intersects(var0, var2, var4, var6, var8, var10)) {
            int var30 = (int)Math.floor((var29.minX + var29.maxX) / 2.0);
            int var31 = (int)Math.floor(var29.minY + 1.0E-7);
            int var32 = (int)Math.floor((var29.minZ + var29.maxZ) / 2.0);
            double var33 = var30 + 0.5 - var12;
            double var35 = var31 + 0.5 - var14;
            double var37 = var32 + 0.5 - var16;
            double var39 = var33 * var33 + var35 * var35 + var37 * var37;
            boolean var41 = var20 != -9223372036854775808L && (var31 != var25 ? var31 > var25 : (var32 != var26 ? var32 > var26 : var30 > var24));
            if (var39 < var22 || var39 == var22 && var41) {
               var20 = BlockPos.asLong(var30, var31, var32);
               var22 = var39;
               var24 = var30;
               var25 = var31;
               var26 = var32;
            }
         }
      }

      return var20;
   }

   private static boolean isApproxEqual(double var0, double var2) {
      return Math.abs(var2 - var0) < 1.0E-5;
   }

   static Vec3 collideAndStep(Vec3 var0, AABB var1, boolean var2, AiPhysics.PhysicsWorld var3) {
      if (var0.lengthSqr() == 0.0) {
         return var0;
      }

      AiPlannerWorld var4 = var3 instanceof AiPlannerWorld var5 ? var5 : null;
      double[] var6;
      float[] var7;
      double[] var9;
      if (var4 != null) {
         var9 = var4.putAt_DArr;
         var6 = var4.fill_DArr;
         var7 = var4.putAt_FArr;
      } else {
         AiPhysics.ScratchBuffers var8 = SCRATCH_BUFFERS.get();
         var9 = var8.moveResult;
         var6 = var8.stepCandidate;
         var7 = var8.stepHeights;
      }

      collideAndStepRaw(var9, var6, var7, var4, var0.x, var0.y, var0.z, var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ, var2, var3);
      return new Vec3(var9[0], var9[1], var9[2]);
   }

   private static long collideAndStepRaw(
      double[] var0,
      double[] var1,
      float[] var2,
      AiPlannerWorld var3,
      double var4,
      double var6,
      double var8,
      double var10,
      double var12,
      double var14,
      double var16,
      double var18,
      double var20,
      boolean var22,
      AiPhysics.PhysicsWorld var23
   ) {
      if (var4 == 0.0 && var6 == 0.0 && var8 == 0.0) {
         var0[0] = 0.0;
         var0[1] = 0.0;
         var0[2] = 0.0;
         return -1L;
      }

      double var24 = var10 + Math.min(0.0, var4) - 1.0E-4;
      double var26 = var12 + Math.min(0.0, var6) - 0.51 - 1.0E-4;
      double var28 = var14 + Math.min(0.0, var8) - 1.0E-4;
      double var30 = var16 + Math.max(0.0, var4) + 1.0E-4;
      double var32 = var18 + Math.max(0.0, var6) + 1.0E-4;
      double var34 = var20 + Math.max(0.0, var8) + 1.0E-4;
      long var36;
      if (var3 != null) {
         var36 = var3.putAt(var0, var4, var6, var8, var10, var12, var14, var16, var18, var20, var24, var26, var28, var30, var32, var34);
      } else {
         Vec3 var38 = var23.putAt(var4, var6, var8, var10, var12, var14, var16, var18, var20, var24, var26, var28, var30, var32, var34);
         var0[0] = var38.x;
         var0[1] = var38.y;
         var0[2] = var38.z;
         var36 = -1L;
      }

      double var75 = var0[0];
      double var40 = var0[1];
      double var42 = var0[2];
      boolean var44 = !isApproxEqual(var4, var75);
      boolean var45 = !isApproxEqual(var8, var42);
      boolean var46 = !isApproxEqual(var6, var40);
      boolean var47 = var46 && var6 < 0.0;
      if ((var22 || var47) && (var44 || var45)) {
         double var48 = var47 ? var12 + var40 : var12;
         double var50 = var47 ? var18 + var40 : var18;
         double var52 = var10 + Math.min(0.0, var4);
         double var54 = var16 + Math.max(0.0, var4);
         double var56 = var14 + Math.min(0.0, var8);
         double var58 = var20 + Math.max(0.0, var8);
         double var60 = var47 ? var48 : var48 - 1.0E-5;
         double var62 = var50 + 0.6;
         AABB[] var64 = null;
         long var65 = 0L;
         List var67 = null;
         if (var3 != null) {
            AABB[] var68 = var3.putAt_AABBArr_D(var52, var60, var56, var54, var62, var58);
            if (var68.length < 64) {
               var64 = var68;

               for (int var69 = 0; var69 < var68.length; var69++) {
                  if (var68[var69].intersects(var52, var60, var56, var54, var62, var58)) {
                     var65 |= 1L << var69;
                  }
               }
            }
         }

         if (var64 == null) {
            var67 = var23.collisions(new AABB(var52, var60, var56, var54, var62, var58));
         }

         int var76 = 0;
         float var79 = (float)var40;
         float var70 = 0.6F;
         if (var64 != null) {
            long var80 = var65;

            while (var80 != 0L) {
               AABB var84 = var64[Long.numberOfTrailingZeros(var80)];
               var80 &= var80 - 1L;
               var76 = appendUniqueStepHeight(var2, var76, (float)(var84.minY - var48), var79, var70);
               var76 = appendUniqueStepHeight(var2, var76, (float)(var84.maxY - var48), var79, var70);
            }
         } else {
            int var71 = 0;

            for (int var72 = var67.size(); var71 < var72; var71++) {
               AABB var73 = (AABB)var67.get(var71);
               var76 = appendUniqueStepHeight(var2, var76, (float)(var73.minY - var48), var79, var70);
               var76 = appendUniqueStepHeight(var2, var76, (float)(var73.maxY - var48), var79, var70);
            }
         }

         for (int var81 = 1; var81 < var76; var81++) {
            float var83 = var2[var81];

            int var85;
            for (var85 = var81 - 1; var85 >= 0 && var2[var85] > var83; var85--) {
               var2[var85 + 1] = var2[var85];
            }

            var2[var85 + 1] = var83;
         }

         double var82 = var75 * var75 + var42 * var42 + 1.0E-12;

         for (int var86 = 0; var86 < var76; var86++) {
            float var74 = var2[var86];
            if (var64 != null) {
               resolveCollisionsBitmask(var1, var4, var74, var8, var10, var48, var14, var16, var50, var20, var64, var65);
            } else {
               resolveCollisionsList(var1, var4, var74, var8, var10, var48, var14, var16, var50, var20, var67);
            }

            if (var1[0] * var1[0] + var1[2] * var1[2] > var82) {
               var0[0] = var1[0];
               var0[1] = var1[1] - (var12 - var48);
               var0[2] = var1[2];
               return -1L;
            }
         }
      }

      return var36;
   }

   private static int appendUniqueStepHeight(float[] var0, int var1, float var2, float var3, float var4) {
      if (!(var2 < 0.0F) && var2 != var3 && !(var2 > var4) && var1 < var0.length) {
         for (int var5 = 0; var5 < var1; var5++) {
            if (var0[var5] == var2) {
               return var1;
            }
         }

         var0[var1] = var2;
         return var1 + 1;
      } else {
         return var1;
      }
   }

   static Vec3 collideWithBoxes(Vec3 var0, AABB var1, List<AABB> var2) {
      if (var2.isEmpty()) {
         return var0;
      }

      double[] var3 = SCRATCH_BUFFERS.get().stepCandidate;
      resolveCollisionsList(var3, var0.x, var0.y, var0.z, var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ, var2);
      return new Vec3(var3[0], var3[1], var3[2]);
   }

   static Vec3 collideWithBitmaskBoxes(Vec3 var0, AABB var1, AABB[] var2, long var3) {
      if (var3 == 0L) {
         return var0;
      }

      double[] var5 = SCRATCH_BUFFERS.get().stepCandidate;
      resolveCollisionsBitmask(var5, var0.x, var0.y, var0.z, var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ, var2, var3);
      return new Vec3(var5[0], var5[1], var5[2]);
   }

   static Vec3 collideRawWithBitmask(
      double var0, double var2, double var4, double var6, double var8, double var10, double var12, double var14, double var16, AABB[] var18, long var19
   ) {
      if (var19 == 0L) {
         return new Vec3(var0, var2, var4);
      }

      double[] var21 = SCRATCH_BUFFERS.get().stepCandidate;
      resolveCollisionsBitmask(var21, var0, var2, var4, var6, var8, var10, var12, var14, var16, var18, var19);
      return new Vec3(var21[0], var21[1], var21[2]);
   }

   static void resolveCollisionsBitmask(
      double[] var0,
      double var1,
      double var3,
      double var5,
      double var7,
      double var9,
      double var11,
      double var13,
      double var15,
      double var17,
      AABB[] var19,
      long var20
   ) {
      if (var20 == 0L) {
         var0[0] = var1;
         var0[1] = var3;
         var0[2] = var5;
      } else {
         if (var3 != 0.0) {
            var3 = clipAxisY(var7, var9, var11, var13, var15, var17, null, var19, var20, var3);
            if (var3 != 0.0) {
               var9 += var3;
               var15 += var3;
            }
         }

         boolean var22 = Math.abs(var1) < Math.abs(var5);
         if (var22 && var5 != 0.0) {
            var5 = clipAxisZ(var7, var9, var11, var13, var15, var17, null, var19, var20, var5);
            if (var5 != 0.0) {
               var11 += var5;
               var17 += var5;
            }
         }

         if (var1 != 0.0) {
            var1 = clipAxisX(var7, var9, var11, var13, var15, var17, null, var19, var20, var1);
            if (!var22 && var1 != 0.0) {
               var7 += var1;
               var13 += var1;
            }
         }

         if (!var22 && var5 != 0.0) {
            var5 = clipAxisZ(var7, var9, var11, var13, var15, var17, null, var19, var20, var5);
         }

         var0[0] = var1;
         var0[1] = var3;
         var0[2] = var5;
      }
   }

   private static void resolveCollisionsList(
      double[] var0, double var1, double var3, double var5, double var7, double var9, double var11, double var13, double var15, double var17, List<AABB> var19
   ) {
      if (var19.isEmpty()) {
         var0[0] = var1;
         var0[1] = var3;
         var0[2] = var5;
      } else {
         if (var3 != 0.0) {
            var3 = clipAxisY(var7, var9, var11, var13, var15, var17, var19, null, 0L, var3);
            if (var3 != 0.0) {
               var9 += var3;
               var15 += var3;
            }
         }

         boolean var20 = Math.abs(var1) < Math.abs(var5);
         if (var20 && var5 != 0.0) {
            var5 = clipAxisZ(var7, var9, var11, var13, var15, var17, var19, null, 0L, var5);
            if (var5 != 0.0) {
               var11 += var5;
               var17 += var5;
            }
         }

         if (var1 != 0.0) {
            var1 = clipAxisX(var7, var9, var11, var13, var15, var17, var19, null, 0L, var1);
            if (!var20 && var1 != 0.0) {
               var7 += var1;
               var13 += var1;
            }
         }

         if (!var20 && var5 != 0.0) {
            var5 = clipAxisZ(var7, var9, var11, var13, var15, var17, var19, null, 0L, var5);
         }

         var0[0] = var1;
         var0[1] = var3;
         var0[2] = var5;
      }
   }

   private static double clipAxisX(
      double var0, double var2, double var4, double var6, double var8, double var10, List<AABB> var12, AABB[] var13, long var14, double var16
   ) {
      if (var13 == null) {
         int var21 = 0;

         for (int var19 = var12.size(); var21 < var19; var21++) {
            if (Math.abs(var16) < 1.0E-7) {
               return 0.0;
            }

            AABB var20 = (AABB)var12.get(var21);
            if (var20.maxY > var2 + 1.0E-7 && var20.minY < var8 - 1.0E-7 && var20.maxZ > var4 + 1.0E-7 && var20.minZ < var10 - 1.0E-7) {
               if (var16 > 0.0 && var20.minX >= var6 - 1.0E-7) {
                  var16 = Math.min(var16, var20.minX - var6);
               } else if (var16 < 0.0 && var20.maxX <= var0 + 1.0E-7) {
                  var16 = Math.max(var16, var20.maxX - var0);
               }
            }
         }
      } else {
         while (var14 != 0L) {
            if (Math.abs(var16) < 1.0E-7) {
               return 0.0;
            }

            AABB var18 = var13[Long.numberOfTrailingZeros(var14)];
            var14 &= var14 - 1L;
            if (var18.maxY > var2 + 1.0E-7 && var18.minY < var8 - 1.0E-7 && var18.maxZ > var4 + 1.0E-7 && var18.minZ < var10 - 1.0E-7) {
               if (var16 > 0.0 && var18.minX >= var6 - 1.0E-7) {
                  var16 = Math.min(var16, var18.minX - var6);
               } else if (var16 < 0.0 && var18.maxX <= var0 + 1.0E-7) {
                  var16 = Math.max(var16, var18.maxX - var0);
               }
            }
         }
      }

      return Math.abs(var16) < 1.0E-7 ? 0.0 : var16;
   }

   private static double clipAxisY(
      double var0, double var2, double var4, double var6, double var8, double var10, List<AABB> var12, AABB[] var13, long var14, double var16
   ) {
      if (var13 == null) {
         int var21 = 0;

         for (int var19 = var12.size(); var21 < var19; var21++) {
            if (Math.abs(var16) < 1.0E-7) {
               return 0.0;
            }

            AABB var20 = (AABB)var12.get(var21);
            if (var20.maxX > var0 + 1.0E-7 && var20.minX < var6 - 1.0E-7 && var20.maxZ > var4 + 1.0E-7 && var20.minZ < var10 - 1.0E-7) {
               if (var16 > 0.0 && var20.minY >= var8 - 1.0E-7) {
                  var16 = Math.min(var16, var20.minY - var8);
               } else if (var16 < 0.0 && var20.maxY <= var2 + 1.0E-7) {
                  var16 = Math.max(var16, var20.maxY - var2);
               }
            }
         }
      } else {
         while (var14 != 0L) {
            if (Math.abs(var16) < 1.0E-7) {
               return 0.0;
            }

            AABB var18 = var13[Long.numberOfTrailingZeros(var14)];
            var14 &= var14 - 1L;
            if (var18.maxX > var0 + 1.0E-7 && var18.minX < var6 - 1.0E-7 && var18.maxZ > var4 + 1.0E-7 && var18.minZ < var10 - 1.0E-7) {
               if (var16 > 0.0 && var18.minY >= var8 - 1.0E-7) {
                  var16 = Math.min(var16, var18.minY - var8);
               } else if (var16 < 0.0 && var18.maxY <= var2 + 1.0E-7) {
                  var16 = Math.max(var16, var18.maxY - var2);
               }
            }
         }
      }

      return Math.abs(var16) < 1.0E-7 ? 0.0 : var16;
   }

   private static double clipAxisZ(
      double var0, double var2, double var4, double var6, double var8, double var10, List<AABB> var12, AABB[] var13, long var14, double var16
   ) {
      if (var13 == null) {
         int var21 = 0;

         for (int var19 = var12.size(); var21 < var19; var21++) {
            if (Math.abs(var16) < 1.0E-7) {
               return 0.0;
            }

            AABB var20 = (AABB)var12.get(var21);
            if (var20.maxX > var0 + 1.0E-7 && var20.minX < var6 - 1.0E-7 && var20.maxY > var2 + 1.0E-7 && var20.minY < var8 - 1.0E-7) {
               if (var16 > 0.0 && var20.minZ >= var10 - 1.0E-7) {
                  var16 = Math.min(var16, var20.minZ - var10);
               } else if (var16 < 0.0 && var20.maxZ <= var4 + 1.0E-7) {
                  var16 = Math.max(var16, var20.maxZ - var4);
               }
            }
         }
      } else {
         while (var14 != 0L) {
            if (Math.abs(var16) < 1.0E-7) {
               return 0.0;
            }

            AABB var18 = var13[Long.numberOfTrailingZeros(var14)];
            var14 &= var14 - 1L;
            if (var18.maxX > var0 + 1.0E-7 && var18.minX < var6 - 1.0E-7 && var18.maxY > var2 + 1.0E-7 && var18.minY < var8 - 1.0E-7) {
               if (var16 > 0.0 && var18.minZ >= var10 - 1.0E-7) {
                  var16 = Math.min(var16, var18.minZ - var10);
               } else if (var16 < 0.0 && var18.maxZ <= var4 + 1.0E-7) {
                  var16 = Math.max(var16, var18.maxZ - var4);
               }
            }
         }
      }

      return Math.abs(var16) < 1.0E-7 ? 0.0 : var16;
   }

   private static void clipSneakEdge(
      double[] var0, double var1, double var3, double var5, double var7, double var9, double var11, double var13, double var15, AiPhysics.PhysicsWorld var17
   ) {
      while (var1 != 0.0 && !hasSupportBelowOffset(var7 + var1, var9, var11, var13 + var1, var15, var17)) {
         var1 = shrinkTowardZero(var1);
      }

      while (var5 != 0.0 && !hasSupportBelowOffset(var7, var9, var11 + var5, var13, var15 + var5, var17)) {
         var5 = shrinkTowardZero(var5);
      }

      while (var1 != 0.0 && var5 != 0.0 && !hasSupportBelowOffset(var7 + var1, var9, var11 + var5, var13 + var1, var15 + var5, var17)) {
         var1 = shrinkTowardZero(var1);
         var5 = shrinkTowardZero(var5);
      }

      var0[0] = var1;
      var0[1] = var3;
      var0[2] = var5;
   }

   private static double shrinkTowardZero(double var0) {
      return Math.abs(var0) <= 0.05 ? 0.0 : var0 - Math.copySign(0.05, var0);
   }

   private static boolean hasSupportBelowOffset(double var0, double var2, double var4, double var6, double var8, AiPhysics.PhysicsWorld var10) {
      return var10.putAt_Z_D(var0 + 1.0E-7, var2 - 0.6 - 1.0E-7, var4 + 1.0E-7, var6 - 1.0E-7, var2, var8 - 1.0E-7);
   }

   public static List<AiPhysics.MotionState> simulate(AiPhysics.MotionState var0, List<AiPhysics.MoveInput> var1, AiPhysics.PhysicsWorld var2) {
      ArrayList var3 = new ArrayList(var1.size() + 1);
      var3.add(var0);
      AiPhysics.MotionState var4 = var0;

      for (AiPhysics.MoveInput var6 : var1) {
         var4 = step(var4, var6, var2);
         var3.add(var4);
      }

      return var3;
   }

   public static float yawBetween(double var0, double var2, double var4, double var6) {
      return (float)(Math.toDegrees(Math.atan2(var6 - var2, var4 - var0)) - 90.0);
   }

   public record MoveInput(double forward, double strafe, boolean jump, boolean sprint, boolean sneak, float yaw) {
      public static final AiPhysics.MoveInput NONE = new AiPhysics.MoveInput(0.0, 0.0, false, false, false, 0.0F);

      public static AiPhysics.MoveInput run(float var0, boolean var1) {
         return new AiPhysics.MoveInput(1.0, 0.0, false, var1, false, var0);
      }

      public static AiPhysics.MoveInput jump(float var0, boolean var1) {
         return new AiPhysics.MoveInput(1.0, 0.0, true, var1, false, var0);
      }

      public AiPhysics.MoveInput withStrafe(double var1) {
         return new AiPhysics.MoveInput(this.forward, var1, this.jump, this.sprint, this.sneak, this.yaw);
      }

      public AiPhysics.MoveInput withJump(boolean var1) {
         return new AiPhysics.MoveInput(this.forward, this.strafe, var1, this.sprint, this.sneak, this.yaw);
      }

      public AiPhysics.MoveInput withYaw(float var1) {
         return new AiPhysics.MoveInput(this.forward, this.strafe, this.jump, this.sprint, this.sneak, var1);
      }
   }

   private static final class ScratchBuffers {
      final double[] moveResult = new double[3];
      final double[] stepCandidate = new double[3];
      final float[] stepHeights = new float[16];
   }

   public record MotionState(
      double x,
      double y,
      double z,
      double vx,
      double vy,
      double vz,
      boolean onGround,
      int jumpDelay,
      boolean horizontalCollision,
      boolean verticalCollision,
      long support,
      boolean sprintCut,
      int pose,
      int stuck
   ) {
      public static final long NO_SUPPORT = -9223372036854775808L;
      public static final double RESTING_VY = -0.0784;

      public MotionState(
         double var1,
         double var3,
         double var5,
         double var7,
         double var9,
         double var11,
         boolean var13,
         int var14,
         boolean var15,
         boolean var16,
         long var17,
         boolean var19,
         int var20
      ) {
         this(var1, var3, var5, var7, var9, var11, var13, var14, var15, var16, var17, var19, var20, 0);
      }

      public MotionState(
         double var1, double var3, double var5, double var7, double var9, double var11, boolean var13, int var14, boolean var15, boolean var16
      ) {
         this(var1, var3, var5, var7, var9, var11, var13, var14, var15, var16, -9223372036854775808L, false, 0);
      }

      public MotionState(
         double var1, double var3, double var5, double var7, double var9, double var11, boolean var13, int var14, boolean var15, boolean var16, long var17
      ) {
         this(var1, var3, var5, var7, var9, var11, var13, var14, var15, var16, var17, false, 0);
      }

      public MotionState(
         double var1,
         double var3,
         double var5,
         double var7,
         double var9,
         double var11,
         boolean var13,
         int var14,
         boolean var15,
         boolean var16,
         long var17,
         boolean var19
      ) {
         this(var1, var3, var5, var7, var9, var11, var13, var14, var15, var16, var17, var19, 0);
      }

      public static AiPhysics.MotionState of(Entity var0) {
         Vec3 var1 = var0.getDeltaMovement();
         boolean var2 = var0.getPose() == Pose.SWIMMING && !var0.isInWater();
         int var3;
         if (var0 instanceof LocalPlayer var4) {
            boolean var5 = var4.input != null && var4.input.keyPresses.shift();
            boolean var6 = var4.level().noCollision(var4, var4.getDimensions(Pose.STANDING).makeBoundingBox(var4.position()).deflate(1.0E-7));
            boolean var7 = var4.level().noCollision(var4, var4.getDimensions(Pose.CROUCHING).makeBoundingBox(var4.position()).deflate(1.0E-7));
            var3 = resolveLocalPose(
               var2, var4.getAbilities().flying, var4.isSwimming(), var4.isPassenger(), var4.isSleeping(), var5, var4.onGround(), var6, var7
            );
         } else {
            var3 = var2 ? 2 : (var0.getPose() == Pose.CROUCHING ? 3 : 0);
         }

         int var8 = var0 instanceof AutismLivingEntityAccessor var9 ? var9.autism$getNoJumpDelay() : 0;
         long var10 = var0.mainSupportingBlockPos.map(BlockPos::asLong).orElse(-9223372036854775808L);
         return new AiPhysics.MotionState(
            var0.getX(),
            var0.getY(),
            var0.getZ(),
            var1.x,
            var1.y,
            var1.z,
            var0.onGround(),
            var8,
            var0.horizontalCollision,
            var0.verticalCollision,
            var10,
            var0.horizontalCollision && !var0.minorHorizontalCollision,
            var3,
            stuckAt(var0)
         );
      }

      static int resolveLocalPose(boolean var0, boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, boolean var7, boolean var8) {
         if (var0) {
            return 2;
         } else {
            boolean var9 = !var1 && !var2 && !var3 && var8 && (var5 || !var4 && !var7);
            if (!var9) {
               return 0;
            } else {
               return var5 && var6 ? 4 : 1;
            }
         }
      }

      static int stuckAt(Entity var0) {
         AABB var1 = var0.getBoundingBox().deflate(1.0E-5);
         Level var2 = var0.level();
         byte var3 = 0;
         MutableBlockPos var4 = new MutableBlockPos();

         for (int var5 = (int)Math.floor(var1.minX); var5 <= (int)Math.floor(var1.maxX); var5++) {
            for (int var6 = (int)Math.floor(var1.minY); var6 <= (int)Math.floor(var1.maxY); var6++) {
               for (int var7 = (int)Math.floor(var1.minZ); var7 <= (int)Math.floor(var1.maxZ); var7++) {
                  Block var8 = var2.getBlockState(var4.set(var5, var6, var7)).getBlock();
                  if (var8 instanceof WebBlock) {
                     return 1;
                  }

                  if (var8 instanceof SweetBerryBushBlock) {
                     var3 = 2;
                  }
               }
            }
         }

         return var3;
      }

      public AiPhysics.MotionState atRest() {
         return new AiPhysics.MotionState(
            this.x, this.y, this.z, 0.0, -0.0784, 0.0, true, 0, false, false, -9223372036854775808L, false, this.pose, this.stuck
         );
      }

      public boolean crawling() {
         return this.pose == 2;
      }

      public static AiPhysics.MotionState standing(double var0, double var2, double var4) {
         return new AiPhysics.MotionState(var0, var2, var4, 0.0, -0.0784, 0.0, true, 0, false, false, -9223372036854775808L);
      }

      public Vec3 position() {
         return new Vec3(this.x, this.y, this.z);
      }

      public double speed() {
         return Math.hypot(this.vx, this.vz);
      }

      public AABB box(boolean var1) {
         double var2 = this.pose == 2 ? 0.6 : (!var1 && this.pose == 0 ? 1.8 : 1.5);
         return new AABB(this.x - 0.3, this.y, this.z - 0.3, this.x + 0.3, this.y + var2, this.z + 0.3);
      }
   }

   public interface PhysicsWorld {
      // 5.3 external ABI defaults (overridden by modules/i.java and ai/b/ArrayUtil.java)
      default double init_D() { return 4.0; }
      default double load() { return 0.1; }
      default double init_D_I(int x, int y, int z) { return 0.6; }
      default double load_D(int x, int y, int z) { return 1.0; }
      default boolean flush(int x, int y, int z) { return false; }
      default double read_D(int x, int y, int z) { return this.flush(x, y, z) ? 1.0 : 0.0; }
      default double write_D(int x, int y, int z) { return 1.0; }
      default boolean sync(int x, int y, int z) { return false; }
      default int init_I(AABB box) { return 0; }
      default boolean init_Z() { return false; }
      default boolean init_Z_I(int x, int y, int z) { return false; }
      default boolean load_Z(int x, int y, int z) { return false; }
      default boolean read_Z(int x, int y, int z) { return false; }
      default boolean write_Z(int x, int y, int z) { return false; }
      default boolean update(int x, int y, int z) { return false; }
      default boolean init_Z(AABB q) { return this.putAt_Z_AABB(q); }

      default double putAt_D() {
         return this.init_D();
      }

      default double movementSpeed() {
         return this.load();
      }

      default List<AABB> collisions(AABB var1) {
         return this.init_List(var1);
      }

      default List<AABB> init_List(AABB var1) {
         return this.collisions(var1);
      }

      default Vec3 putAt_Vec3_Vec3(Vec3 var1, AABB var2, AABB var3) {
         return AiPhysics.collideWithBoxes(var1, var2, this.collisions(var3));
      }

      default Vec3 putAt(
         double var1,
         double var3,
         double var5,
         double var7,
         double var9,
         double var11,
         double var13,
         double var15,
         double var17,
         double var19,
         double var21,
         double var23,
         double var25,
         double var27,
         double var29
      ) {
         return this.putAt_Vec3_Vec3(
            new Vec3(var1, var3, var5), new AABB(var7, var9, var11, var13, var15, var17), new AABB(var19, var21, var23, var25, var27, var29)
         );
      }

      default boolean putAt_Z_AABB(AABB var1) {
         for (AABB var3 : this.collisions(var1)) {
            if (var3.intersects(var1)) {
               return true;
            }
         }

         return false;
      }

      default boolean putAt_Z_D(double var1, double var3, double var5, double var7, double var9, double var11) {
         return this.putAt_Z_AABB(new AABB(var1, var3, var5, var7, var9, var11));
      }

      default long putAt_J_AABB(AABB var1, double var2, double var4, double var6) {
         return AiPhysics.findSupportInBoxesRaw(var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ, var2, var4, var6, this.collisions(var1), null);
      }

      default long putAt(double var1, double var3, double var5, double var7, double var9, double var11, double var13, double var15, double var17) {
         return this.putAt_J_AABB(new AABB(var1, var3, var5, var7, var9, var11), var13, var15, var17);
      }

      default double putAt_D_I(int var1, int var2, int var3) {
         return this.init_D_I(var1, var2, var3);
      }

      default double fill_D(int var1, int var2, int var3) {
         return this.load_D(var1, var2, var3);
      }

      default boolean closeOf(int var1, int var2, int var3) {
         return this.flush(var1, var2, var3);
      }

      default double readEx_D(int var1, int var2, int var3) {
         return this.closeOf(var1, var2, var3) ? 1.0 : this.read_D(var1, var2, var3);
      }

      default double value_D(int var1, int var2, int var3) {
         return this.write_D(var1, var2, var3);
      }

      default boolean state(int var1, int var2, int var3) {
         return this.sync(var1, var2, var3);
      }

      default int putAt_I_AABB(AABB var1) {
         return this.init_I(var1);
      }

      default boolean putAt_Z() {
         return this.init_Z();
      }

      default boolean putAt_Z_I_2(int var1, int var2, int var3) {
         return this.init_Z_I(var1, var2, var3);
      }

      default boolean fill_Z(int var1, int var2, int var3) {
         return this.load_Z(var1, var2, var3);
      }

      default boolean prevEx(int var1, int var2, int var3) {
         return this.update(var1, var2, var3);
      }

      default boolean prev(int var1, int var2, int var3) {
         return this.prevEx(var1, var2, var3);
      }

      default boolean chunkAt(int var1, int var2, int var3) {
         return this.prevEx(var1, var2, var3);
      }

      default boolean readEx_Z(int var1, int var2, int var3) {
         return this.read_Z(var1, var2, var3);
      }

      default boolean value_Z(int var1, int var2, int var3) {
         return this.write_Z(var1, var2, var3);
      }

      default boolean putAt(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
         return false;
      }
   }

   // 5.3 external ABI bridges
   public static AiPhysics.PhysicsWorld createLiveWorld(ClientLevel level, Entity body) {
      return liveWorld(level, body);
   }

   public static AiPhysics.MotionState stepCompat(AiPhysics.MotionState s, AiPhysics.MoveInput in, AiPhysics.PhysicsWorld world) {
      return step(s, in, world);
   }

   public static AiPhysics.MotionState stepUnclippedCompat(AiPhysics.MotionState s, AiPhysics.MoveInput in, AiPhysics.PhysicsWorld world) {
      return stepUnclipped(s, in, world);
   }

   public static List<AiPhysics.MotionState> simulateCompat(AiPhysics.MotionState start, List<AiPhysics.MoveInput> inputs, AiPhysics.PhysicsWorld world) {
      return simulate(start, inputs, world);
   }

   public static float yawTowards(double fromX, double fromZ, double toX, double toZ) {
      return yawBetween(fromX, fromZ, toX, toZ);
   }

   static boolean hitsHazardCompat(AiPhysics.PhysicsWorld world, AiPhysics.MotionState from, AiPhysics.MotionState to) {
      return hitsHazard(world, from, to);
   }
}
