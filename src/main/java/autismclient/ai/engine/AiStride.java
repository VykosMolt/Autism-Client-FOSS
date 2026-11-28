package autismclient.ai.engine;

import java.util.function.Function;
import java.util.function.LongPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

final class AiStride {
   static final int flowState = 30;
   static final int fill = -2;
   static final int readEx = 8;
   static final int value = 8;
   static final double putAt_D = 2.2;
   private static final int[] putAt_IArr = new int[]{0, -12, 12, -24, 24};

   private AiStride() {
   }

   static long putAt_J_D_D(double var0, double var2) {
      return putAt_J_I_I((int)Math.floor(var0), (int)Math.floor(var2));
   }

   static long putAt_J_I_I(int var0, int var1) {
      return (long)var0 << 32 ^ var1 & 4294967295L;
   }

   static boolean putAt(AiPhysics.MotionState var0, LongPredicate var1, AiPhysics.PhysicsWorld var2) {
      AiPhysics.MotionState var3 = var0;
      double var4 = var0.y();
      AiPhysics.MoveInput var6 = new AiPhysics.MoveInput(0.0, 0.0, false, false, false, 0.0F);

      for (int var7 = 0; var7 < 8; var7++) {
         AiPhysics.MotionState var8 = AiPhysics.step(var3, var6, var2);
         if (AiPhysics.hitsHazard(var2, var3, var8)) {
            return false;
         }

         if (!AiGroundContinuity.putAt(var2, var3, var8, var4) || !var1.test(putAt_J_D_D(var8.x(), var8.z()))) {
            return false;
         }

         var3 = var8;
         if (var3.onGround()) {
            var4 = var3.y();
         }
      }

      return var3.onGround();
   }

   static boolean putAt_Z_TokenCore_TableCac_2(
      AiPhysics.MotionState var0, AiPhysics.MoveInput var1, double var2, LongPredicate var4, AiPhysics.PhysicsWorld var5
   ) {
      AiPhysics.MotionState var6 = var0;
      AiPhysics.MoveInput var7 = var1.withJump(false);

      for (int var8 = 0; var8 < 8; var8++) {
         AiPhysics.MotionState var9 = AiPhysics.step(var6, var7, var5);
         if (AiPhysics.hitsHazard(var5, var6, var9)) {
            return false;
         }

         var6 = var9;
         if (!var6.onGround() || Math.abs(var6.y() - var2) > 0.01) {
            return false;
         }

         if (var4.test(putAt_J_D_D(var6.x(), var6.z()))) {
            return putAt(var6, var4, var5);
         }
      }

      return false;
   }

   static boolean putAt_Z_TokenCore_TableCac(
      AiPhysics.MotionState var0,
      double var1,
      LongPredicate var3,
      AiPhysics.PhysicsWorld var4,
      Function<AiPhysics.MotionState, AiPhysics.MoveInput> var5
   ) {
      if (!var0.onGround()) {
         return false;
      }

      double var6 = var0.y();
      int var8 = 0;

      while (var8 < 8) {
         AiPhysics.MotionState var9 = AiPhysics.step(var0, AiPhysics.MoveInput.NONE, var4);
         if (AiGroundContinuity.putAt(var4, var0, var9, var6) && !AiPhysics.hitsHazard(var4, var0, var9)) {
            if (var9.onGround()) {
               var6 = var9.y();
            }

            AiPhysics.MoveInput var10 = (AiPhysics.MoveInput)var5.apply(var9);
            if (putAt_I_TokenCore_TableCac(var9, var10, 0, var1, var3, var4) <= 0 && putAt_I_TokenCore_TableCac(var9, var10, 1, var1, var3, var4) <= 0) {
               var0 = var9;
               var8++;
               continue;
            }

            return true;
         }

         return false;
      }

      return false;
   }

   static AiStride.FallResult putAt(AiPhysics.MotionState var0, AiPhysics.MoveInput var1, boolean var2, AiPhysics.PhysicsWorld var3) {
      AiPhysics.MotionState var4 = var0;
      boolean var5 = false;
      boolean var6 = false;
      boolean var7 = !var0.onGround();

      for (int var8 = 1; var8 <= 30; var8++) {
         AiPhysics.MotionState var9 = AiPhysics.step(var4, var1.withJump(var2 && var8 == 1), var3);
         if (AiPhysics.hitsHazard(var3, var4, var9)) {
            return new AiStride.FallResult(null, var8, var5, var6);
         }

         var6 |= var2 && var9.verticalCollision() && !var9.onGround() && (var8 == 1 || var4.vy() > 0.0);
         var4 = var9;
         var5 |= var4.horizontalCollision();
         if (!var4.onGround()) {
            var7 = true;
         } else if (var7) {
            return new AiStride.FallResult(var4, var8, var5, var6);
         }
      }

      return new AiStride.FallResult(null, 30, var5, var6);
   }

   static AiStride.FallResult putAt(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MoveInput var2) {
      if (var1.jumpDelay() <= 1 && var1.onGround()) {
         AiPhysics.MotionState var3 = AiPhysics.step(var1, var2.withJump(true), var0);
         if (var3.verticalCollision() && !var3.onGround() && !var3.horizontalCollision() && !AiPhysics.hitsStrictHazard(var0, var1, var3)) {
            AiStride.FallResult var4 = putAt(var1, var2, true, var0);
            return var4.down()
                  && var4.ceiling()
                  && !var4.wall()
                  && var4.ticks() <= 4
                  && Math.abs(var4.at().y() - var1.y()) <= 0.02
                  && !AiPhysics.hitsStrictHazard(var0, var3, var4.at())
                  && putAt_Z_TokenCore_TableCac_3(var1, var2, var2.yaw(), var4, var0)
               ? var4
               : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static boolean putAt_Z_TokenCore_TableCac_3(
      AiPhysics.MotionState var0, AiPhysics.MoveInput var1, float var2, AiStride.FallResult var3, AiPhysics.PhysicsWorld var4
   ) {
      if (!var3.down()) {
         return false;
      }

      AiPhysics.MotionState var5 = var0;

      for (int var6 = 0; var6 < var3.ticks(); var6++) {
         var5 = AiPhysics.step(var5, var1.withJump(false), var4);
      }

      double var16 = Math.toRadians(var2);
      double var8 = -Math.sin(var16);
      double var10 = Math.cos(var16);
      double var12 = (var3.at().x() - var0.x()) * var8 + (var3.at().z() - var0.z()) * var10 + (var3.at().vx() * var8 + var3.at().vz() * var10) * 2.2;
      double var14 = (var5.x() - var0.x()) * var8 + (var5.z() - var0.z()) * var10 + (var5.vx() * var8 + var5.vz() * var10) * 2.2;
      return var12 > var14 + 0.02;
   }

   static int putAt_I_TokenCore_TableCac(
      AiPhysics.MotionState var0, AiPhysics.MoveInput var1, int var2, double var3, LongPredicate var5, AiPhysics.PhysicsWorld var6
   ) {
      return putAt_UiCore_TaskState_TokenCore_TableCac(var0, var1, var2, var3, var5, var6).ticks();
   }

   static AiStride.ContactTicks putAt_UiCore_TaskState_TokenCore_TableCac(
      AiPhysics.MotionState var0, AiPhysics.MoveInput var1, int var2, double var3, LongPredicate var5, AiPhysics.PhysicsWorld var6
   ) {
      AiPhysics.MotionState var7 = var0;
      boolean var8 = false;
      boolean var9 = false;

      for (int var10 = 1; var10 <= 30; var10++) {
         boolean var11 = !var8 && var10 > var2 && var7.onGround();
         AiPhysics.MotionState var12 = AiPhysics.step(var7, var1.withJump(var11), var6);
         if (AiPhysics.hitsHazard(var6, var7, var12)) {
            return new AiStride.ContactTicks(-2, var9);
         }

         var7 = var12;
         var9 |= var7.horizontalCollision();
         var8 |= var11;
         if (var7.onGround()) {
            if (var7.y() >= var3 - 0.001) {
               return new AiStride.ContactTicks(!putAt(var7, var5, var6) && !putAt_Z_TokenCore_TableCac_2(var7, var1, var3, var5, var6) ? -1 : var10, var9);
            }

            if (var8 && var10 > var2 + 1) {
               return new AiStride.ContactTicks(-1, var9);
            }
         }
      }

      return new AiStride.ContactTicks(-1, var9);
   }

   static boolean putAt_Z_I_I(int var0, int var1) {
      return var0 >= 0 && (var1 < 0 || var0 <= var1);
   }

   static AiPhysics.MoveInput putAt_TokenCore_CacheHel_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3, float var5, boolean var6
   ) {
      double var7 = Math.hypot(var2.getX() + 0.5 - var1.x(), var2.getZ() + 0.5 - var1.z());
      if (AiJumpSolver.isGroundedNotRising(var1) && var1.pose() == 0 && !(var7 > 2.75) && !(var3 - var1.y() > 1.125) && !(var1.y() > var3 + 0.02)) {
         float var9 = AiPhysics.yawBetween(var1.x(), var1.z(), var2.getX() + 0.5, var2.getZ() + 0.5);
         AiPhysics.MoveInput var10 = null;
         int var11 = 10;

         for (float var15 : new float[]{var5, var9, var9 - 15.0F, var9 + 15.0F, Math.round(var9 / 90.0F) * 90.0F}) {
            if (!(Math.abs(Mth.wrapDegrees(var15 - var9)) > 55.0F)) {
               for (boolean var19 : new boolean[]{var6, false}) {
                  AiPhysics.MoveInput var20 = AiPhysics.MoveInput.run(var15, var19);
                  AiPhysics.MotionState var21 = var1;

                  for (int var22 = 1; var22 < var11; var22++) {
                     AiPhysics.MotionState var23 = AiPhysics.step(var21, var20, var0);
                     if (!var23.onGround()
                        || var23.y() < var21.y() - 0.02
                        || var23.y() > var3 + 0.02
                        || AiPhysics.hitsStrictHazard(var0, var21, var23)
                        || AiJumpSolver.isInFluid(var0, var23)) {
                        break;
                     }

                     if (AiJumpSolver.isLandedOnCell(var23.x(), var23.y(), var23.z(), var2, var3)
                        && AiJumpSolver.settledLandingMargin(var0, var23, var2, var3, var15) >= 0.04) {
                        var10 = var20;
                        var11 = var22;
                        break;
                     }

                     var21 = var23;
                  }

                  if (!var6) {
                     break;
                  }
               }
            }
         }

         return var10;
      } else {
         return null;
      }
   }

   static AiStride.StridePlan putAt_UiCore_JsonCache_TokenCore_TableCac(
      AiPhysics.MotionState var0, BlockPos var1, double var2, LongPredicate var4, AiPhysics.PhysicsWorld var5
   ) {
      return putAt_UiCore_JsonCache_TokenCore_TableCac(var0, var1, var2, var4, var5, false);
   }

   static AiStride.StridePlan putAt_UiCore_JsonCache_TokenCore_TableCac(
      AiPhysics.MotionState var0, BlockPos var1, double var2, LongPredicate var4, AiPhysics.PhysicsWorld var5, boolean var6
   ) {
      if (var0.onGround() && var0.pose() == 0 && var0.jumpDelay() <= 1 && !(var2 - var0.y() <= 0.6) && !(var2 - var0.y() > 1.25)) {
         double var7 = var1.getX() + 0.5 - var0.x();
         double var9 = var1.getZ() + 0.5 - var0.z();
         double var11 = var7 * var7 + var9 * var9;
         if (!(var11 < 1.0E-6) && !(var11 > 6.002500000000001)) {
            double var13 = Math.sqrt(var11);
            float var15 = AiPhysics.yawBetween(0.0, 0.0, var7, var9);
            AiStride.StridePlan var16 = null;

            for (int var20 : putAt_IArr) {
               AiPhysics.MoveInput var21 = AiPhysics.MoveInput.run(var15 + var20, true);

               for (int var22 = 0; var22 < 3; var22++) {
                  AiStride.StridePlan var23 = new AiStride.StridePlan(null, var21, var22 == 0, var22 == 2 ? 2 : 0, 0, 0.0);
                  AiStride.FallResult var24 = putAt(var0, var23, 0, var2, var4, var5, var6);
                  if (var24 != null) {
                     AiPhysics.MotionState var25 = var24.at();
                     double var26 = (var25.vx() * var7 + var25.vz() * var9) / var13;
                     if (var16 == null || var24.ticks() < var16.ticks() || var24.ticks() == var16.ticks() && var26 > var16.exitSpeed() + 0.015) {
                        var16 = new AiStride.StridePlan(null, var21, var23.boost(), var23.coast(), var24.ticks(), var26, var6);
                     }
                  }
               }

               if (var16 != null && var16.boost() && var16.exitSpeed() > 0.18) {
                  break;
               }
            }

            return var16;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static AiStride.StridePlan fill(AiPhysics.MotionState var0, BlockPos var1, double var2, LongPredicate var4, AiPhysics.PhysicsWorld var5) {
      if (!(var0.speed() < 0.06) && var0.onGround() && var0.pose() == 0) {
         float var6 = AiPhysics.yawBetween(0.0, 0.0, var0.vx(), var0.vz());

         for (float var10 : new float[]{var6, var6 - 25.0F, var6 + 25.0F}) {
            AiPhysics.MoveInput var11 = new AiPhysics.MoveInput(-1.0, 0.0, false, false, false, var10);
            AiPhysics.MotionState var12 = AiPhysics.step(var0, var11, var5);
            if (var12.onGround()
               && !(Math.abs(var12.y() - var0.y()) > 0.02)
               && !var12.horizontalCollision()
               && !(var12.speed() >= var0.speed())
               && !AiPhysics.hitsStrictHazard(var5, var0, var12)
               && AiJumpSolver.canStandAt(var5, var12.x(), var12.y(), var12.z())) {
               AiStride.StridePlan var13 = putAt_UiCore_JsonCache_TokenCore_TableCac(var12, var1, var2, var4, var5);
               if (var13 != null) {
                  return new AiStride.StridePlan(var11, var13.drive(), var13.boost(), var13.coast(), var13.ticks() + 1, var13.exitSpeed(), var13.touching());
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static AiStride.FallResult putAt_UiCore_TableCache_TokenCore_TableCac(
      AiPhysics.MotionState var0, AiStride.StridePlan var1, int var2, double var3, LongPredicate var5, AiPhysics.PhysicsWorld var6
   ) {
      return putAt(var0, var1, var2, var3, var5, var6, false);
   }

   static AiStride.FallResult putAt(
      AiPhysics.MotionState var0, AiStride.StridePlan var1, int var2, double var3, LongPredicate var5, AiPhysics.PhysicsWorld var6, boolean var7
   ) {
      for (int var8 = var2; var8 < 14; var8++) {
         AiPhysics.MotionState var9 = AiPhysics.step(var0, var1.input(var8), var6);
         if (var9.horizontalCollision() && !var7 || AiPhysics.hitsStrictHazard(var6, var0, var9) || AiJumpSolver.isInFluid(var6, var9)) {
            return null;
         }

         if (var9.onGround()) {
            if (!(Math.abs(var9.y() - var3) > 0.02) && !(var9.vy() > 0.0) && var5.test(putAt_J_D_D(var9.x(), var9.z())) && putAt(var9, var5, var6)) {
               return new AiStride.FallResult(var9, var8 + 1, false, false);
            }

            return null;
         }

         var0 = var9;
      }

      return null;
   }

   static AiStride.StrideAction putAt(
      AiPhysics.MotionState var0,
      AiPhysics.MoveInput var1,
      AiPhysics.MoveInput var2,
      AiPhysics.MoveInput var3,
      LongPredicate var4,
      boolean var5,
      double var6,
      double var8,
      AiPhysics.PhysicsWorld var10
   ) {
      AiStride.StrideAction var11 = null;
      double var12 = 1.7976931348623157E308;
      AiStride.StrideAction var14 = AiStride.StrideAction.NONE;
      double var15 = 1.7976931348623157E308;

      for (AiStride.StrideAction var20 : AiStride.StrideAction.values()) {
         AiPhysics.MoveInput var21 = var20 == AiStride.StrideAction.GO
            ? var1
            : (var20 == AiStride.StrideAction.NONE ? var2 : var3);
         AiStride.FallResult var22 = putAt(var0, var21, false, var10);
         if (var22.down()) {
            double var23 = Math.hypot(var22.at().x() - var6, var22.at().z() - var8);
            if (var23 < var15) {
               var15 = var23;
               var14 = var20;
            }

            if (var4.test(putAt_J_D_D(var22.at().x(), var22.at().z())) && putAt(var22.at(), var4, var10)) {
               if (var5) {
                  return var20;
               }

               if (var23 < var12) {
                  var12 = var23;
                  var11 = var20;
               }
            }
         }
      }

      return var11 != null ? var11 : var14;
   }

   enum StrideAction {
      GO,
      NONE,
      BRAKE;
   }

   record StridePlan(
      AiPhysics.MoveInput setup, AiPhysics.MoveInput drive, boolean boost, int coast, int ticks, double exitSpeed, boolean touching
   ) {
      StridePlan(AiPhysics.MoveInput var1, AiPhysics.MoveInput var2, boolean var3, int var4, int var5, double var6) {
         this(var1, var2, var3, var4, var5, var6, false);
      }

      AiPhysics.MoveInput input(int var1) {
         if (this.setup != null) {
            if (var1 == 0) {
               return this.setup;
            }

            var1--;
         }

         if (var1 == 0) {
            return new AiPhysics.MoveInput(this.drive.forward(), this.drive.strafe(), true, this.boost, false, this.drive.yaw());
         } else {
            return var1 <= this.coast ? AiPhysics.MoveInput.NONE.withYaw(this.drive.yaw()) : this.drive;
         }
      }
   }

   record FallResult(AiPhysics.MotionState at, int ticks, boolean wall, boolean ceiling) {
      boolean down() {
         return this.at != null;
      }

      boolean struck() {
         return this.wall || this.ceiling;
      }
   }

   record ContactTicks(int ticks, boolean struck) {
   }
}
