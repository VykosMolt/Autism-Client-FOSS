package autismclient.ai.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public final class AiJumpSolver {
   public static final int MAX_RUN_TICKS = 12;
   public static final int MAX_FLIGHT_TICKS = 40;
   public static final int MAX_SIM_TICKS = 100;
   static final int DEFAULT_APPROACH_TICKS = 40;
   static final double REACH_QUANT_SCALE = 4.0;
   public static final double MIN_ACCEPTABLE_MARGIN = -0.12;
   public static final double SAFE_MARGIN_FLOOR = -0.04;
   public static final double MAX_LANDING_DROP_TOLERANCE = 0.55;
   public static final int NO_COAST_LIMIT = 2147483647;
   public static final double SPRINT_APPROACH_SPEED = 0.28;
   public static final double COMFORTABLE_MARGIN = 0.14;
   private static final int[] RELEASE_TICK_CANDIDATES = new int[]{2147483647, 1, 3, 5, 7, 9};
   private static final int[] ALT_RELEASE_TICK_CANDIDATES = new int[]{2147483647, 1, 3, 5, 7, 9};
   private static final float[] CLEAR_COLLISION_YAW_OFFSETS = new float[]{-4.0F, 4.0F, -8.0F, 8.0F, -12.0F, 12.0F, -16.0F, 16.0F, -20.0F, 20.0F};
   private static final int[] CURVE_RUNUP_YAW_OFFSETS = new int[]{0, -18, 18, -36, 36, -60, 60, -84, 84};
   private static final int[] CURVE_ENTRY_ANGLES = new int[]{0, -7, 7, -14, 14};
   private static final int[] WIDE_CURVE_ANGLES = new int[]{0, -7, 7, -14, 14, -21, 21};
   private static final int[] CURVE_COAST_AFTER_TICKS = new int[]{4, 6, 8};
   private static final float[] PACE_YAW_OFFSETS = new float[]{0.0F, -8.0F, 8.0F, -16.0F, 16.0F, -24.0F, 24.0F};
   private static final ConcurrentHashMap<Integer, Double> DEFAULT_REACH_CACHE = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<AiJumpSolver.ArcSpeed, Double> SPEED_REACH_CACHE = new ConcurrentHashMap<>();

   private AiJumpSolver() {
   }

   static AiJumpSolver.ExitOption solveChainedExit(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, BlockPos var3, double var4, AiJumpSolver.LandingTarget var6
   ) {
      AiJumpSolver.NextJump var7 = var6.nextJump();
      if (var7 != null && var7.sprint() && var3.equals(var7.from())) {
         float var8 = Mth.wrapDegrees(var7.yaw() - var2);
         if (!(Math.abs(var8) > 105.0F) && !(Math.abs(var8) < 3.0F)) {
            AiJumpSolver.ExitOption var9 = null;
            double var10 = Math.toRadians(var7.yaw());
            double var12 = -Math.sin(var10);
            double var14 = Math.cos(var10);

            for (int var16 = 0; var16 < 5; var16++) {
               float var17 = var2 + Math.clamp(var8, -23.0F, 23.0F) * (var16 == 0 ? 0.0F : (var16 == 1 ? 0.5F : 1.0F));
               double var18 = var16 == 3 ? 1.0 : (var16 == 4 ? -1.0 : 0.0);
               AiPhysics.MoveInput var20 = AiPhysics.MoveInput.run(var17, true).withStrafe(var18);
               AiPhysics.MotionState var21 = var1;

               for (int var22 = 0; var22 < 9; var22++) {
                  AiPhysics.MotionState var23 = AiPhysics.step(var21, var20, var0);
                  if (var23.horizontalCollision()
                     || var23.verticalCollision() && !var23.onGround()
                     || AiPhysics.hitsStrictHazard(var0, var21, var23)
                     || isInFluid(var0, var23)
                     || var23.y() < var4 - 0.55) {
                     break;
                  }

                  if (isGroundedNotRising(var23)) {
                     double var24 = settledLandingMargin(var0, var23, var3, var4, var17);
                     if (var24 < 0.08) {
                        var24 = measureCoastingSupportMargin(var0, var23, var3, var4);
                     }

                     if (var24 < 0.08 || !isInsideLandingCell(var23, var3, var4)) {
                        break;
                     }

                     AiJumpSolver.UiHelper var26 = solveTakeoffAllowEarlyCoast(var0, var23, var7.yaw(), true, var6.cell(), var6.surface());
                     if (var26.margin() < 0.07 || var26.runTicks() > 5) {
                        break;
                     }

                     int var27 = var26.runTicks();
                     double var28 = var23.vx() * var12 + var23.vz() * var14;
                     if (var9 == null || var27 < var9.nextRunTicks() || var27 == var9.nextRunTicks() && var28 > var9.exitSpeed() + 0.009) {
                        var9 = new AiJumpSolver.ExitOption(var17, var18, var24, var27, var28);
                     }
                     break;
                  }

                  var21 = var23;
               }
            }

            return var9;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static AiJumpSolver.TakeoffCandidate solveLiveTakeoff(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, float var3, BlockPos var4, double var5
   ) {
      if (!(var1.speed() < 0.1) && var1.jumpDelay() == 0 && !var1.horizontalCollision()) {
         double var7 = var4.getX() + 0.5 - var1.x();
         double var9 = var4.getZ() + 0.5 - var1.z();
         double var11 = Math.hypot(var7, var9);
         if (!(var11 < 1.7) && !(var11 > 6.2) && !(Math.abs(var5 - var1.y()) > 2.1)) {
            AiJumpSolver.JumpAxis var13 = new AiJumpSolver.JumpAxis(var1.x(), var1.z(), var7 / var11, var9 / var11, var11);
            AiJumpSolver.TakeoffCandidate var14 = null;

            for (float var18 : new float[]{0.0F, -7.0F, 7.0F, -14.0F, 14.0F, -20.0F, 20.0F}) {
               float var19 = var2 + var18;
               if (!(Math.abs(Mth.wrapDegrees(var19 - var3)) > 32.0F)) {
                  AiPhysics.MoveInput var20 = AiPhysics.MoveInput.run(var19, true);
                  AiJumpSolver.ArcResult var21 = simulateJumpArc(var0, var1, var20, var20, 2147483647, var13, var4, var5);
                  double var22 = evaluateArcMargin(var0, var21, var4, var5);
                  if (var22 >= 0.065) {
                     AiJumpSolver.UiHelper var24 = new AiJumpSolver.UiHelper(0, 0.0, 2147483647, var22, true, var21.ticks() + 1);
                     if (var14 == null || var22 > var14.takeoff().margin()) {
                        var14 = new AiJumpSolver.TakeoffCandidate(var19, var24);
                     }

                     if (var22 >= 0.14) {
                        break;
                     }
                  } else if (var14 == null && var21 != null && var21.along() > var11 + 0.18 && Math.abs(var18) <= 14.0F) {
                     for (int var27 : new int[]{8, 5, 2}) {
                        AiJumpSolver.ArcResult var28 = simulateJumpArc(var0, var1, var20, var20, var27, var13, var4, var5);
                        var22 = evaluateArcMargin(var0, var28, var4, var5);
                        if (!(var22 < 0.08)) {
                           var14 = new AiJumpSolver.TakeoffCandidate(var19, new AiJumpSolver.UiHelper(0, 0.0, var27, var22, true, var28.ticks() + 1));
                           break;
                        }
                     }
                  }
               }
            }

            return var14;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static double evaluateArcMargin(AiPhysics.PhysicsWorld var0, AiJumpSolver.ArcResult var1, BlockPos var2, double var3) {
      if (var1 != null && !var1.bumped() && var1.settled() && isInsideLandingCell(var1.end(), var2, var3)) {
         return var1.margin() >= 0.065 ? var1.margin() : measureCoastingSupportMargin(var0, var1.end(), var2, var3);
      } else {
         return -1.0;
      }
   }

   static AiJumpSolver.JumpPlan solveCurvedLiveJump(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, BlockPos var3, double var4
   ) {
      if (isGroundedNotRising(var1) && var1.jumpDelay() == 0 && !var1.horizontalCollision() && var1.pose() == 0) {
         double var6 = var3.getX() + 0.5 - var1.x();
         double var8 = var3.getZ() + 0.5 - var1.z();
         double var10 = Math.hypot(var6, var8);
         if (!(var10 < 1.6) && !(var10 > 6.7) && !(Math.abs(var4 - var1.y()) > 3.1)) {
            AiJumpSolver.JumpAxis var12 = new AiJumpSolver.JumpAxis(var1.x(), var1.z(), var6 / var10, var8 / var10, var10);
            float var13 = AiPhysics.yawBetween(var1.x(), var1.z(), var3.getX() + 0.5, var3.getZ() + 0.5);
            float var14 = Mth.wrapDegrees(var13 - var2);
            if (Math.abs(var14) > 78.0F) {
               return null;
            }

            float[] var15 = new float[]{var2 + Math.clamp(var14, -28.0F, 28.0F), var2 + Math.clamp(var14, -38.0F, 38.0F)};

            for (float var19 : var15) {
               float var20 = var19 + Math.clamp(Mth.wrapDegrees(var13 - var19), -20.0F, 20.0F);

               for (int var24 : new int[]{0, -1, 1}) {
                  AiPhysics.MoveInput var25 = AiPhysics.MoveInput.jump(var19, true).withStrafe(var24);
                  AiPhysics.MoveInput var26 = AiPhysics.MoveInput.run(var20, true);
                  AiJumpSolver.ArcResult var27 = simulateJumpArc(var0, var1, var25, var26, 2147483647, var12, var3, var4);
                  if (var27 != null && !var27.bumped() && var27.settled() && !(var27.margin() < 0.085) && isInsideLandingCell(var27.end(), var3, var4)) {
                     ArrayList var28 = new ArrayList(var27.ticks() + 1);
                     var28.add(var25);

                     for (int var29 = 0; var29 < var27.ticks(); var29++) {
                        var28.add(var26);
                     }

                     AiPhysics.MotionState var31 = var27.end();
                     AiJumpManeuver var30 = new AiJumpManeuver(var28, var1, var31, var27.margin(), 0);
                     return new AiJumpSolver.JumpPlan(
                        var19,
                        0,
                        true,
                        2147483647,
                        var27.ticks(),
                        var1.x(),
                        var1.z(),
                        var31.x(),
                        var31.y(),
                        var31.z(),
                        var31.speed(),
                        var27.margin(),
                        0.0,
                        var1.speed(),
                        var30
                     );
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

   private static boolean isInsideLandingCell(AiPhysics.MotionState var0, BlockPos var1, double var2) {
      return Math.abs(var0.y() - var2) <= 0.02 && (int)Math.floor(var0.x()) == var1.getX() && (int)Math.floor(var0.z()) == var1.getZ();
   }

   static double measureCoastingSupportMargin(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3) {
      if (isGroundedNotRising(var1) && isInsideLandingCell(var1, var2, var3) && isFullFlatSupport(var0, var2.getX(), var2.getZ(), var3)) {
         AiPhysics.MotionState var5 = var1;
         double var6 = 1.0 / 0.0;

         for (int var8 = 0; var8 <= 8; var8++) {
            double var9 = 0.3;
            AABB var11 = new AABB(var5.x() - var9, var3 - 0.02, var5.z() - var9, var5.x() + var9, var3 - 0.001, var5.z() + var9);
            double var12 = 0.0;

            for (AABB var15 : var0.collisions(var11)) {
               if (!(Math.abs(var15.maxY - var3) > 0.01)) {
                  double var16 = Math.min(Math.min(var15.maxX, var11.maxX), var2.getX() + 1) - Math.max(Math.max(var15.minX, var11.minX), var2.getX());
                  double var18 = Math.min(Math.min(var15.maxZ, var11.maxZ), var2.getZ() + 1) - Math.max(Math.max(var15.minZ, var11.minZ), var2.getZ());
                  var12 = Math.max(var12, Math.min(var16, var18));
               }
            }

            if (var12 < 0.24 || !isGroundedNotRising(var5) || Math.abs(var5.y() - var3) > 0.02) {
               return -1.0;
            }

            var6 = Math.min(var6, var12);
            if (var5.speed() <= 0.008) {
               return var6 - 0.17;
            }

            AiPhysics.MotionState var20 = AiPhysics.step(var5, AiPhysics.MoveInput.NONE, var0);
            if (AiPhysics.hitsStrictHazard(var0, var5, var20)) {
               return -1.0;
            }

            var5 = var20;
         }

         return -1.0;
      } else {
         return -1.0;
      }
   }

   public static AiJumpSolver.JumpPlan solve(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, double var7, boolean var9
   ) {
      return solve(var0, var1, var2, var4, var5, var7, var9, true);
   }

   static AiJumpSolver.JumpPlan solve(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, double var7, boolean var9, boolean var10
   ) {
      return solve(var0, var1, var2, var4, var5, var7, var9, var10, true);
   }

   static AiJumpSolver.JumpPlan solve(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, double var7, boolean var9, boolean var10, boolean var11
   ) {
      return solve(var0, var1, var2, var4, var5, var7, var9, var10, var11, false);
   }

   static AiJumpSolver.JumpPlan solve(
      AiPhysics.PhysicsWorld var0,
      BlockPos var1,
      double var2,
      BlockPos var4,
      double var5,
      double var7,
      boolean var9,
      boolean var10,
      boolean var11,
      boolean var12
   ) {
      return solve(var0, var1, var2, var4, var5, var7, var9, var10, var11, var12, false);
   }

   static AiJumpSolver.JumpPlan solve(
      AiPhysics.PhysicsWorld var0,
      BlockPos var1,
      double var2,
      BlockPos var4,
      double var5,
      double var7,
      boolean var9,
      boolean var10,
      boolean var11,
      boolean var12,
      boolean var13
   ) {
      double var14 = var1.getX() + 0.5;
      double var16 = var1.getZ() + 0.5;
      double var18 = var4.getX() + 0.5;
      double var20 = var4.getZ() + 0.5;
      double var22 = var18 - var14;
      double var24 = var20 - var16;
      double var26 = Math.hypot(var22, var24);
      if (var26 < 1.0E-6) {
         return null;
      }

      if (var10 && var11 && !canStandAt(var0, var14, var2, var16)) {
         AiJumpSolver.JumpPlan var40 = solveOffCenterJump(var0, var1, var2, var4, var5, var9, var12);
         return var40 != null ? var40 : AiJumpManeuver.solve(var0, var1, var2, var4, var5, var9, true, var12);
      }

      double var28 = var22 / var26;
      double var30 = var24 / var26;
      double var32 = var7 > 0.0 ? var14 - var28 * 0.2 : var14;
      double var34 = var7 > 0.0 ? var16 - var30 * 0.2 : var16;
      AiJumpSolver.JumpPlan var36 = solveFromPoint(var0, var32, var34, var2, var4, var5, var28, var30, var7, var9, 0.0, var13);
      if (var7 > 0.0 && (var36 == null || var36.margin() < 0.14)) {
         AiJumpSolver.JumpPlan var37 = solve(var0, var1, var2, var4, var5, 0.0, var9, false, false, false, var13);
         if (var37 != null && (var36 == null || var36.margin() < -0.04 || var37.margin() > var36.margin() + 0.04)) {
            var36 = var37;
         }
      }

      if ((var36 == null || var36.margin() < 0.0) && var7 <= 0.0) {
         double var41 = measureBackupDistance(var0, var14, var16, var2, var28, var30);
         if (var41 >= 0.25) {
            AiJumpSolver.JumpPlan var39 = solveFromPoint(var0, var14 - var28 * var41, var16 - var30 * var41, var2, var4, var5, var28, var30, 0.0, var9, var41, var13);
            if (var39 != null && (var36 == null || isBetterJumpPlan(var39, var36))) {
               var36 = var39;
            }
         }
      }

      double var42 = var5 - var2;
      if (var11
         && var9
         && var42 > 0.6
         && var42 <= 1.25
         && Math.abs(var22 * var24) == 8.0
         && Math.max(Math.abs(var22), Math.abs(var24)) == 4.0
         && (var36 == null || var36.margin() < -0.04)) {
         AiJumpSolver.JumpPlan var43 = solveDiagonalOffsetJump(var0, var2, var4, var5, var14, var16, var18, var20);
         if (var43 != null) {
            return var43;
         }
      }

      if (var10 && (var36 == null || var36.margin() < -0.04)) {
         if (var11) {
            AiJumpSolver.JumpPlan var44 = solveOffCenterJump(var0, var1, var2, var4, var5, var9, var12);
            if (var44 != null) {
               return var44;
            }
         }

         AiJumpSolver.JumpPlan var45 = AiJumpManeuver.solve(var0, var1, var2, var4, var5, var9, var11, var12);
         if (var45 != null && (var36 == null || var45.margin() > var36.margin())) {
            return var45;
         }
      }

      return var36;
   }

   private static AiJumpSolver.JumpPlan solveDiagonalOffsetJump(
      AiPhysics.PhysicsWorld var0, double var1, BlockPos var3, double var4, double var6, double var8, double var10, double var12
   ) {
      double var14 = Math.signum(var10 - var6);
      double var16 = Math.signum(var12 - var8);

      for (double var21 : new double[]{0.34, 0.24}) {
         double var23 = var10 - var14 * var21;
         double var25 = var12 - var16 * var21;

         for (double var30 : new double[]{0.0, -0.25, 0.25}) {
            double var32 = var6 + var30 * var16;
            double var34 = var8 - var30 * var14;
            double var36 = Math.hypot(var23 - var32, var25 - var34);
            double var38 = (var23 - var32) / var36;
            double var40 = (var25 - var34) / var36;
            double var42 = measureBackupDistance(var0, var32, var34, var1, var38, var40);
            var32 -= var38 * var42;
            var34 -= var40 * var42;
            AiJumpSolver.JumpPlan var44 = solveFromPoint(
               var0, var32, var34, var1, var3, var4, var23, var25, var38, var40, 0.0, true, Math.hypot(var32 - var6, var34 - var8), false
            );
            if (var44 != null && var44.margin() >= -0.04) {
               return var44;
            }
         }
      }

      return null;
   }

   private static AiJumpSolver.JumpPlan solveFromPoint(
      AiPhysics.PhysicsWorld var0,
      double var1,
      double var3,
      double var5,
      BlockPos var7,
      double var8,
      double var10,
      double var12,
      double var14,
      boolean var16,
      double var17,
      boolean var19
   ) {
      return solveFromPoint(var0, var1, var3, var5, var7, var8, var7.getX() + 0.5, var7.getZ() + 0.5, var10, var12, var14, var16, var17, var19);
   }

   private static AiJumpSolver.JumpPlan solveFromPoint(
      AiPhysics.PhysicsWorld var0,
      double var1,
      double var3,
      double var5,
      BlockPos var7,
      double var8,
      double var10,
      double var12,
      double var14,
      double var16,
      double var18,
      boolean var20,
      double var21,
      boolean var23
   ) {
      if (!canStandAt(var0, var1, var5, var3)) {
         return null;
      }

      float var24 = AiPhysics.yawBetween(var1, var3, var10, var12);
      AiJumpSolver.JumpAxis var25 = new AiJumpSolver.JumpAxis(var1, var3, var14, var16, Math.hypot(var10 - var1, var12 - var3));
      AiJumpSolver.JumpPlan var26 = null;

      for (boolean var30 : var20 ? new boolean[]{true, false} : new boolean[]{false}) {
         AiPhysics.MotionState var31 = new AiPhysics.MotionState(var1, var5, var3, var14 * var18, -0.0784, var16 * var18, true, 0, false, false);
         AiPhysics.MoveInput var32 = AiPhysics.MoveInput.run(var24, var30);
         AiJumpSolver.RunArc var33 = findBestRunArc(var0, simulateRunupStates(var0, var31, var32, 12), var32, var32, var25, var7, var8, false, var23);
         if (var33 != null && !(var33.arc().margin() < -0.12)) {
            AiJumpSolver.ArcResult var34 = var33.arc();
            AiJumpSolver.JumpPlan var35 = new AiJumpSolver.JumpPlan(
               var24,
               var33.runTicks(),
               var30,
               var34.coastAfter(),
               var34.ticks(),
               var1,
               var3,
               var34.end().x(),
               var34.end().y(),
               var34.end().z(),
               var34.end().speed(),
               var34.margin(),
               var21,
               var18
            );
            if (var26 == null || isBetterJumpPlan(var35, var26)) {
               var26 = var35;
            }

            if (var30 && var35.margin() >= 0.0) {
               break;
            }
         }
      }

      return var26;
   }

   static List<AiJumpSolver.Point2D> findStandablePoints(AiPhysics.PhysicsWorld var0, BlockPos var1, double var2) {
      double var4 = var1.getX() + 0.5;
      double var6 = var1.getZ() + 0.5;
      if (canStandAt(var0, var4, var2, var6)) {
         return List.of(new AiJumpSolver.Point2D(var4, var6));
      }

      ArrayList<AiJumpSolver.Point2D> var8 = new ArrayList<>(4);

      for (double var12 : new double[]{0.38, 0.4, 0.22, 0.12}) {
         for (int var14 = 0; var14 < 2; var14++) {
            for (int var18 : new int[]{-1, 1}) {
               double var19 = var4 + (var14 == 0 ? var12 * var18 : 0.0);
               double var21 = var6 + (var14 == 1 ? var12 * var18 : 0.0);
               if (canStandAt(var0, var19, var2, var21)) {
                  var8.add(new AiJumpSolver.Point2D(var19, var21));
               }
            }
         }
      }

      for (double var29 : new double[]{0.38, 0.4, 0.42}) {
         for (int var33 : new int[]{-1, 1}) {
            for (int var36 : new int[]{-1, 1}) {
               double var22 = var4 + var33 * var29;
               double var24 = var6 + var36 * var29;
               if (canStandAt(var0, var22, var2, var24)) {
                  var8.add(new AiJumpSolver.Point2D(var22, var24));
               }
            }
         }
      }

      return var8;
   }

   static boolean hasStandablePoint(AiPhysics.PhysicsWorld var0, BlockPos var1, double var2) {
      return !findStandablePoints(var0, var1, var2).isEmpty();
   }

   static List<AiJumpSolver.Point2D> deduplicatePoints(List<AiJumpSolver.Point2D> var0) {
      if (var0.size() < 2) {
         return var0;
      }

      ArrayList<AiJumpSolver.Point2D> var1 = new ArrayList<>(var0.size());

      for (AiJumpSolver.Point2D var3 : var0) {
         boolean var4 = false;

         for (AiJumpSolver.Point2D var6 : var1) {
            double var7 = var3.x() - var6.x();
            double var9 = var3.z() - var6.z();
            if (var7 * var7 + var9 * var9 <= 0.003025) {
               var4 = true;
               break;
            }
         }

         if (!var4) {
            var1.add(var3);
         }
      }

      return var1;
   }

   private static AiJumpSolver.JumpPlan solveOffCenterJump(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7, boolean var8
   ) {
      List var9 = findStandablePoints(var0, var1, var2);
      List var10 = findStandablePoints(var0, var4, var5);
      if (var8) {
         var9 = deduplicatePoints(var9);
         var10 = deduplicatePoints(var10);
      }

      if (!var9.isEmpty() && !var10.isEmpty()) {
         if (((AiJumpSolver.Point2D)var9.getFirst()).x() == var1.getX() + 0.5
            && ((AiJumpSolver.Point2D)var9.getFirst()).z() == var1.getZ() + 0.5
            && ((AiJumpSolver.Point2D)var10.getFirst()).x() == var4.getX() + 0.5
            && ((AiJumpSolver.Point2D)var10.getFirst()).z() == var4.getZ() + 0.5) {
            return null;
         }

         AiJumpSolver.JumpPlan var11 = null;

         for (AiJumpSolver.Point2D var13 : (Iterable<AiJumpSolver.Point2D>)(Iterable<?>)(var9)) {
            for (AiJumpSolver.Point2D var15 : (Iterable<AiJumpSolver.Point2D>)(Iterable<?>)(var10)) {
               double var16 = var15.x() - var13.x();
               double var18 = var15.z() - var13.z();
               double var20 = Math.hypot(var16, var18);
               if (!(var20 < 0.05)) {
                  double var22 = var16 / var20;
                  double var24 = var18 / var20;
                  AiJumpSolver.JumpPlan var26 = solveFromPoint(
                     var0,
                     var13.x(),
                     var13.z(),
                     var2,
                     var4,
                     var5,
                     var15.x(),
                     var15.z(),
                     var22,
                     var24,
                     0.0,
                     var7,
                     Math.hypot(var13.x() - var1.getX() - 0.5, var13.z() - var1.getZ() - 0.5),
                     false
                  );
                  if (var26 != null && var26.margin() >= -0.04 && (var11 == null || isBetterJumpPlan(var26, var11))) {
                     var11 = var26;
                  }

                  if (var26 == null || var26.margin() < 0.0) {
                     double var27 = measureBackupDistance(var0, var13.x(), var13.z(), var2, var22, var24);
                     if (var27 >= 0.2) {
                        double var29 = var13.x() - var22 * var27;
                        double var31 = var13.z() - var24 * var27;
                        AiJumpSolver.JumpPlan var33 = solveFromPoint(
                           var0,
                           var29,
                           var31,
                           var2,
                           var4,
                           var5,
                           var15.x(),
                           var15.z(),
                           var22,
                           var24,
                           0.0,
                           var7,
                           Math.hypot(var29 - var1.getX() - 0.5, var31 - var1.getZ() - 0.5),
                           false
                        );
                        if (var33 != null && var33.margin() >= -0.04 && (var11 == null || isBetterJumpPlan(var33, var11))) {
                           var11 = var33;
                        }
                     }
                  }
               }
            }
         }

         return var11;
      } else {
         return null;
      }
   }

   static double measureBackupDistance(AiPhysics.PhysicsWorld var0, double var1, double var3, double var5, double var7, double var9) {
      double var11 = 0.0;

      for (double var13 = 0.05; var13 <= 1.500000001 && canStandAt(var0, var1 - var7 * var13, var5, var3 - var9 * var13); var13 += 0.05) {
         var11 = var13;
      }

      return Math.max(0.0, var11 - 0.15);
   }

   static boolean canStandAt(AiPhysics.PhysicsWorld var0, double var1, double var3, double var5) {
      AiPhysics.MotionState var7 = new AiPhysics.MotionState(var1, var3, var5, 0.0, 0.0, 0.0, true, 0, false, false);
      if (AiPhysics.hitsStrictHazard(var0, var7, var7)) {
         return false;
      }

      double var8 = 0.3;
      boolean var10 = false;

      for (AABB var12 : var0.collisions(new AABB(var1 - var8, var3 - 0.02, var5 - var8, var1 + var8, var3 - 0.001, var5 + var8))) {
         if (var12.maxY >= var3 - 0.01) {
            var10 = true;
            break;
         }
      }

      return var10 && !var0.putAt_Z_AABB(new AABB(var1 - var8, var3 + 0.001, var5 - var8, var1 + var8, var3 + 1.8 - 0.01, var5 + var8));
   }

   private static boolean isBetterJumpPlan(AiJumpSolver.JumpPlan var0, AiJumpSolver.JumpPlan var1) {
      boolean var2 = var0.margin() >= 0.0;
      boolean var3 = var1.margin() >= 0.0;
      if (var2 != var3) {
         return var2;
      }

      if (var2 && var0.sprint() != var1.sprint()) {
         return var0.sprint();
      }

      boolean var4 = var0.margin() >= 0.14;
      boolean var5 = var1.margin() >= 0.14;
      if (var4 && var5) {
         int var6 = var0.ticks();
         int var7 = var1.ticks();
         if (var6 != var7) {
            return var6 < var7;
         }

         if (Math.abs(var0.landSpeed() - var1.landSpeed()) > 0.02) {
            return var0.landSpeed() > var1.landSpeed();
         }
      }

      return var0.margin() > var1.margin();
   }

   private static List<AiPhysics.MotionState> simulateRunupStates(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MoveInput var2, int var3
   ) {
      ArrayList var4 = new ArrayList(var3 + 1);
      var4.add(var1);
      AiPhysics.MotionState var5 = var1;

      for (int var6 = 1; var6 <= var3; var6++) {
         AiPhysics.MotionState var7 = AiPhysics.step(var5, var2, var0);
         if (AiPhysics.hitsStrictHazard(var0, var5, var7) || !var7.onGround() || var7.equals(var5)) {
            break;
         }

         var5 = var7;
         var4.add(var5);
      }

      return var4;
   }

   private static AiJumpSolver.RunArc findBestRunArc(
      AiPhysics.PhysicsWorld var0,
      List<AiPhysics.MotionState> var1,
      AiPhysics.MoveInput var2,
      AiPhysics.MoveInput var3,
      AiJumpSolver.JumpAxis var4,
      BlockPos var5,
      double var6,
      boolean var8,
      boolean var9
   ) {
      AiJumpSolver.JumpSearchContext var10 = new AiJumpSolver.JumpSearchContext(var0, var1, var2, var3, var4, var5, var6);
      AiJumpSolver.RunArc var11 = binarySearchRunArc(var10);
      if (var11 != null && var11.arc().margin() >= -0.04) {
         return var11;
      }

      if (!var9 && hitsObstacleOnEndpoints(var10)) {
         AiJumpSolver.RunArc var12 = scanAllRunTicks(var10, var8);
         if (var12 == null) {
            return var11;
         } else {
            return var11 != null && !(var12.arc().margin() > var11.arc().margin()) ? var11 : var12;
         }
      } else {
         return var11;
      }
   }

   private static boolean hitsObstacleOnEndpoints(AiJumpSolver.JumpSearchContext var0) {
      int var1 = var0.runs().size() - 1;
      AiJumpSolver.ArcResult var2 = var0.fly(var1, 2147483647);
      if (var2 != null && var2.bumped()) {
         return true;
      }

      AiJumpSolver.ArcResult var3 = var1 == 0 ? null : var0.fly(0, 2147483647);
      return var3 != null && var3.bumped();
   }

   private static AiJumpSolver.RunArc scanAllRunTicks(AiJumpSolver.JumpSearchContext var0, boolean var1) {
      AiJumpSolver.RunArc var2 = null;
      int var3 = Math.min(var0.runs().size() - 1, 15);
      if (!var1) {
         for (int var4 = 0; var4 <= var3; var4++) {
            AiJumpSolver.ArcResult var5 = var0.fly(var4, 2147483647);
            if (var5 == null) {
               break;
            }

            if (var2 == null || var5.margin() > var2.arc().margin()) {
               var2 = new AiJumpSolver.RunArc(var4, var5);
            }

            if (var2.arc().margin() >= 0.15) {
               return var2;
            }
         }

         if (var2 != null && var2.arc().margin() >= -0.04) {
            int var12 = var2.runTicks();

            for (int var14 = Math.max(0, var12 - 2); var14 <= Math.min(var3, var12 + 2); var14++) {
               for (int var18 : RELEASE_TICK_CANDIDATES) {
                  if (var18 != 2147483647) {
                     AiJumpSolver.ArcResult var10 = var0.fly(var14, var18);
                     if (var10 == null) {
                        break;
                     }

                     if (var10.margin() > var2.arc().margin()) {
                        var2 = new AiJumpSolver.RunArc(var14, var10);
                     }

                     if (var2.arc().margin() >= 0.15) {
                        return var2;
                     }
                  }
               }
            }

            return var2;
         }

         var2 = null;
      }

      for (int var11 = 0; var11 <= var3; var11++) {
         for (int var8 : var1 ? ALT_RELEASE_TICK_CANDIDATES : RELEASE_TICK_CANDIDATES) {
            AiJumpSolver.ArcResult var9 = var0.fly(var11, var8);
            if (var9 == null) {
               break;
            }

            if (var2 == null || var9.margin() > var2.arc().margin()) {
               var2 = new AiJumpSolver.RunArc(var11, var9);
            }

            if (var2.arc().margin() >= 0.15) {
               return var2;
            }
         }
      }

      return var2;
   }

   private static AiJumpSolver.RunArc binarySearchRunArc(AiJumpSolver.JumpSearchContext var0) {
      int var1 = var0.runs().size() - 1;
      AiJumpSolver.ArcResult var2 = var0.fly(var1, 2147483647);
      if (var2 == null) {
         return null;
      }

      if (var2.along() <= var0.line().target()) {
         return new AiJumpSolver.RunArc(var1, var2);
      }

      AiJumpSolver.ArcResult var3 = var1 == 0 ? var2 : var0.fly(0, 2147483647);
      if (var3 != null && var3.along() > var0.line().target()) {
         return var3.settled() && !var3.bumped() && var3.margin() >= 0.14 && cellEdgeMargin(var3.end().x(), var3.end().z(), var0.to()) >= 0.08
            ? new AiJumpSolver.RunArc(0, var3)
            : new AiJumpSolver.RunArc(0, var0.releaseEarlier(0, var3));
      }

      int var4 = 0;
      AiJumpSolver.ArcResult var5 = var3;
      AiJumpSolver.ArcResult var6 = var2;

      while (var1 - var4 > 1) {
         int var7 = (var4 + var1) / 2;
         AiJumpSolver.ArcResult var8 = var0.fly(var7, 2147483647);
         if (var8 == null) {
            break;
         }

         if (var8.along() <= var0.line().target()) {
            var4 = var7;
            var5 = var8;
         } else {
            var1 = var7;
            var6 = var8;
         }
      }

      AiJumpSolver.RunArc var10 = var5 == null ? null : new AiJumpSolver.RunArc(var4, var5);
      AiJumpSolver.RunArc var11 = new AiJumpSolver.RunArc(var1, var6);
      if (var6.margin() < -0.12 && (var5 == null || var5.margin() < -0.12)) {
         AiJumpSolver.ArcResult var9 = var0.releaseEarlier(var1, var6);
         if (var9.margin() > var6.margin()) {
            var11 = new AiJumpSolver.RunArc(var1, var9);
         }
      }

      if (var10 == null) {
         return var11;
      } else if (var10.arc().margin() >= 0.14 && var11.arc().margin() >= 0.14) {
         return var10;
      } else {
         return var11.arc().margin() >= var10.arc().margin() ? var11 : var10;
      }
   }

   private static AiJumpSolver.ArcResult optimizeCoastRelease(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      AiPhysics.MoveInput var2,
      AiPhysics.MoveInput var3,
      AiJumpSolver.ArcResult var4,
      AiJumpSolver.JumpAxis var5,
      BlockPos var6,
      double var7
   ) {
      AiJumpSolver.ArcResult var9 = simulateJumpArc(var0, var1, var2, var3, 0, var5, var6, var7);
      if (var9 == null) {
         return var4;
      }

      if (var9.along() >= var5.target()) {
         return var9.margin() > var4.margin() ? var9 : var4;
      }

      int var10 = 0;
      int var11 = var4.ticks();
      AiJumpSolver.ArcResult var12 = var9;
      AiJumpSolver.ArcResult var13 = var4;

      for (int var14 = 0; var14 < 6 && var11 - var10 > 1; var14++) {
         int var15 = (var10 + var11) / 2;
         AiJumpSolver.ArcResult var16 = simulateJumpArc(var0, var1, var2, var3, var15, var5, var6, var7);
         if (var16 == null) {
            break;
         }

         if (var16.along() < var5.target()) {
            var10 = var15;
            var12 = var16;
         } else {
            var11 = var15;
            var13 = var16;
         }
      }

      if (var13.margin() < var12.margin() + 0.02) {
         return var12;
      } else {
         return var13.margin() >= var12.margin() ? var13 : var12;
      }
   }

   private static AiJumpSolver.ArcResult simulateJumpArc(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      AiPhysics.MoveInput var2,
      AiPhysics.MoveInput var3,
      int var4,
      AiJumpSolver.JumpAxis var5,
      BlockPos var6,
      double var7
   ) {
      if (isInFluid(var0, var1)) {
         return null;
      }

      AiPhysics.MotionState var9 = AiPhysics.step(var1, var2.withJump(true), var0);
      if (AiPhysics.hitsStrictHazard(var0, var1, var9)) {
         return new AiJumpSolver.ArcResult(var9, 0, false, var4, var5.along(var9), -1.0, true, true);
      }

      if (isInFluid(var0, var9)) {
         return null;
      }

      if (var9.onGround() && var9.y() <= var1.y() + 1.0E-6) {
         return null;
      }

      AiPhysics.MoveInput var10 = new AiPhysics.MoveInput(0.0, 0.0, false, false, false, var3.yaw());
      boolean var11 = var9.horizontalCollision();
      boolean var12 = var11 || var9.verticalCollision() && !var9.onGround();
      double var13 = Math.max(var1.y(), var9.y());

      for (int var15 = 1; var15 <= 40; var15++) {
         AiPhysics.MotionState var16 = AiPhysics.step(var9, var15 <= var4 ? var3 : var10, var0);
         if (AiPhysics.hitsStrictHazard(var0, var9, var16)) {
            return new AiJumpSolver.ArcResult(var16, var15, false, var4, var5.along(var16), -1.0, true, true);
         }

         var9 = var16;
         var13 = Math.max(var13, var9.y());
         if (isInFluid(var0, var9)) {
            return null;
         }

         var11 |= var9.horizontalCollision();
         var12 |= var11 || var9.verticalCollision() && !var9.onGround();
         if (isGroundedNotRising(var9)) {
            if (var13 - var9.y() > var0.putAt_D() + 1.0E-5) {
               return new AiJumpSolver.ArcResult(var9, var15, true, var4, var5.along(var9), -1.0, var12, var11);
            }

            double var17 = var9.y() <= var7 + 0.02 && var9.y() >= var7 - 0.55 ? settledLandingMargin(var0, var9, var6, var7, var3.yaw()) : -1.0;
            if (var17 >= -0.04
               && var12
               && var15 <= 4
               && !isLandedOnCell(var9.x(), var9.y(), var9.z(), var6, var7)
               && !canReachCellCenter(var0, var9, var6, var7)) {
               var17 = -1.0;
            }

            return new AiJumpSolver.ArcResult(var9, var15, true, var4, var5.along(var9), var17, var12, var11);
         }

         if (!var9.onGround() && var9.y() < var7 - 4.0) {
            return new AiJumpSolver.ArcResult(var9, var15, false, var4, var5.along(var9), -1.0, var12, var11);
         }
      }

      return new AiJumpSolver.ArcResult(var9, 40, false, var4, var5.along(var9), -1.0, var12, var11);
   }

   static boolean isInFluid(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1) {
      return var0.state((int)Math.floor(var1.x()), (int)Math.floor(var1.y()), (int)Math.floor(var1.z()));
   }

   public static double settledLandingMargin(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3, float var5) {
      if (AiPhysics.hitsStrictHazard(var0, var1, var1)) {
         return -1.0;
      }

      double var6 = expandedCellMargin(var0, var1.x(), var1.z(), var2, var3);
      if (var6 >= -0.04) {
         AiPhysics.MotionState var26 = var1;
         AiPhysics.MoveInput var27 = new AiPhysics.MoveInput(0.0, 0.0, false, false, false, var5);

         for (int var10 = 0; var10 < 64 && (var26.speed() > 0.01 || !var26.onGround()); var10++) {
            AiPhysics.MotionState var28 = AiPhysics.step(var26, var27, var0);
            if (AiPhysics.hitsStrictHazard(var0, var26, var28) || Math.abs(var28.y() - var1.y()) > 0.61) {
               return -1.0;
            }

            var26 = var28;
         }

         if (var26.onGround() && !(var26.speed() > 0.01)) {
            return isFullFlatSupport(var0, (int)Math.floor(var26.x()), (int)Math.floor(var26.z()), var26.y())
               ? var6
               : Math.min(var6, expandedCellMargin(var0, var26.x(), var26.z(), var2, var26.y()));
         } else {
            return -1.0;
         }
      } else {
         BlockPos var8 = resolveAdjacentLandingCell(var0, var1.x(), var1.z(), var2, var3);
         double var9 = -Math.sin(Math.toRadians(var5));
         double var11 = Math.cos(Math.toRadians(var5));
         boolean var13 = Math.abs(var9) >= Math.abs(var11);
         int var14 = var13 ? (int)Math.signum(var9) : 0;
         int var15 = var13 ? 0 : (int)Math.signum(var11);
         boolean var16 = isFullFlatSupport(var0, var8.getX() + var14, var8.getZ() + var15, var3);
         float var17 = AiPhysics.yawBetween(0.0, 0.0, var14, var15);
         AiPhysics.MoveInput var18 = var16
            ? new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var17)
            : new AiPhysics.MoveInput(0.0, 0.0, false, false, false, var5);
         AiPhysics.MotionState var19 = var1;
         double var20 = var6;

         for (int var22 = 0; var22 < 6; var22++) {
            AiPhysics.MotionState var23 = AiPhysics.step(var19, var18, var0);
            if (AiPhysics.hitsStrictHazard(var0, var19, var23)) {
               return -1.0;
            }

            var19 = var23;
            if (!var19.onGround() || Math.abs(var19.y() - var1.y()) > 0.02) {
               return -1.0;
            }

            double var24 = expandedCellMargin(var0, var19.x(), var19.z(), var2, var3);
            if (var16) {
               var20 = Math.max(var20, var24);
               if (var20 >= 0.1) {
                  return settledLandingMargin(var0, var19, var2, var3, var5);
               }
            } else {
               if (Math.hypot(var19.vx(), var19.vz()) < 0.01) {
                  return var24;
               }

               var20 = var24;
            }
         }

         return expandedCellMargin(var0, var19.x(), var19.z(), var2, var3) >= -0.04
            ? Math.min(var20, settledLandingMargin(var0, var19, var2, var3, var5))
            : -1.0;
      }
   }

   static double expandedCellMargin(AiPhysics.PhysicsWorld var0, double var1, double var3, BlockPos var5, double var6) {
      BlockPos var8 = resolveAdjacentLandingCell(var0, var1, var3, var5, var6);
      int var9 = var8.getX();
      int var10 = var8.getZ();
      double var11 = 0.3;
      double var13 = var1 - var11 - var9;
      double var15 = var9 + 1 - (var1 + var11);
      double var17 = var3 - var11 - var10;
      double var19 = var10 + 1 - (var3 + var11);
      if (var13 < 0.5 && isFullFlatSupport(var0, var9 - 1, var10, var6)) {
         var13++;
      }

      if (var15 < 0.5 && isFullFlatSupport(var0, var9 + 1, var10, var6)) {
         var15++;
      }

      if (var17 < 0.5 && isFullFlatSupport(var0, var9, var10 - 1, var6)) {
         var17++;
      }

      if (var19 < 0.5 && isFullFlatSupport(var0, var9, var10 + 1, var6)) {
         var19++;
      }

      double var21 = Math.min(Math.min(var13, var15), Math.min(var17, var19));
      if (!(var21 >= -0.04) && (int)Math.floor(var1) == var5.getX() && (int)Math.floor(var3) == var5.getZ()) {
         boolean var23 = !canStandAt(var0, var5.getX() + 0.5, var6, var5.getZ() + 0.5);
         AABB var24 = new AABB(var1 - var11, var6 - 0.02, var3 - var11, var1 + var11, var6 - 0.001, var3 + var11);

         for (AABB var26 : var0.collisions(var24)) {
            if (!(Math.abs(var26.maxY - var6) > 0.01)) {
               boolean var27 = var26.getXsize() >= 0.99 && var26.getZsize() >= 0.99;
               if ((!var27 || var23) && canStandAt(var0, var1, var6, var3)) {
                  if (var27) {
                     var21 = Math.max(var21, Math.min(Math.min(var13, var15), Math.min(var17, var19)) + var11 - 0.08);
                  } else {
                     double var28 = Math.min(var24.maxX, var26.maxX) - Math.max(var24.minX, var26.minX);
                     double var30 = Math.min(var24.maxZ, var26.maxZ) - Math.max(var24.minZ, var26.minZ);
                     var21 = Math.max(var21, Math.min(var28, var30) - 0.055);
                  }
               }
            }
         }

         return var21;
      } else {
         return var21;
      }
   }

   private static BlockPos resolveAdjacentLandingCell(AiPhysics.PhysicsWorld var0, double var1, double var3, BlockPos var5, double var6) {
      int var8 = (int)Math.floor(var1);
      int var9 = (int)Math.floor(var3);
      if (var8 == var5.getX() && var9 == var5.getZ()) {
         return var5;
      }

      boolean var10 = Math.abs(var8 - var5.getX()) <= 1 && Math.abs(var9 - var5.getZ()) <= 1;
      return var10 && isFullFlatSupport(var0, var8, var9, var6) ? new BlockPos(var8, var5.getY(), var9) : var5;
   }

   static boolean canReachCellCenter(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3) {
      double var5 = var2.getX() + 0.5;
      double var7 = var2.getZ() + 0.5;
      return isWalkLineStandable(var0, var1.x(), var1.z(), var5, var7, var3)
         ? true
         : isWalkLineStandable(var0, var1.x(), var1.z(), var5, var1.z(), var3) && isWalkLineStandable(var0, var5, var1.z(), var5, var7, var3)
            || isWalkLineStandable(var0, var1.x(), var1.z(), var1.x(), var7, var3) && isWalkLineStandable(var0, var1.x(), var7, var5, var7, var3);
   }

   private static boolean isWalkLineStandable(AiPhysics.PhysicsWorld var0, double var1, double var3, double var5, double var7, double var9) {
      int var11 = Math.max(1, (int)Math.ceil(Math.hypot(var5 - var1, var7 - var3) / 0.15));

      for (int var12 = 1; var12 <= var11; var12++) {
         double var13 = (double)var12 / var11;
         if (!canStandAt(var0, var1 + (var5 - var1) * var13, var9, var3 + (var7 - var3) * var13)) {
            return false;
         }
      }

      return true;
   }

   public static boolean isFullFlatSupport(AiPhysics.PhysicsWorld var0, int var1, int var2, double var3) {
      return var0 instanceof AiPlannerWorld var5 ? var5.putAt_Z_I(var1, var2, var3) : computeFullFlatSupport(var0, var1, var2, var3);
   }

   static boolean computeFullFlatSupport(AiPhysics.PhysicsWorld var0, int var1, int var2, double var3) {
      AiPhysics.MotionState var5 = new AiPhysics.MotionState(var1 + 0.5, var3, var2 + 0.5, 0.0, 0.0, 0.0, true, 0, false, false);
      if (AiPhysics.hitsStrictHazard(var0, var5, var5)) {
         return false;
      }

      boolean var6 = false;

      for (AABB var8 : var0.collisions(new AABB(var1 + 0.05, var3 - 0.02, var2 + 0.05, var1 + 0.95, var3 - 0.001, var2 + 0.95))) {
         if (var8.maxY >= var3 - 0.01
            && var8.maxY <= var3 + 0.01
            && var8.minX <= var1 + 0.01
            && var8.maxX >= var1 + 0.99
            && var8.minZ <= var2 + 0.01
            && var8.maxZ >= var2 + 0.99) {
            var6 = true;
            break;
         }
      }

      return var6 && !var0.putAt_Z_AABB(new AABB(var1 + 0.05, var3 + 0.001, var2 + 0.05, var1 + 0.95, var3 + 1.8 - 0.01, var2 + 0.95));
   }

   static boolean isGroundedNotRising(AiPhysics.MotionState var0) {
      return var0.onGround() && var0.vy() <= 0.0;
   }

   public static boolean isLandedOnCell(double var0, double var2, double var4, BlockPos var6, double var7) {
      return !(var2 > var7 + 0.02) && !(var2 < var7 - 0.55) ? cellEdgeMargin(var0, var4, var6) >= -0.12 : false;
   }

   public static double cellEdgeMargin(double var0, double var2, BlockPos var4) {
      double var5 = 0.3;
      double var7 = var0 - var5 - var4.getX();
      double var9 = var4.getX() + 1 - (var0 + var5);
      double var11 = var2 - var5 - var4.getZ();
      double var13 = var4.getZ() + 1 - (var2 + var5);
      return Math.min(Math.min(var7, var9), Math.min(var11, var13));
   }

   public static AiJumpSolver.UiHelper solveTakeoff(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, boolean var3, BlockPos var4, double var5
   ) {
      return solveTakeoff(var0, var1, var2, var3, var4, var5, 40);
   }

   static AiJumpSolver.UiHelper solveWallBlockedTakeoff(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, boolean var3, BlockPos var4, double var5) {
      if (var5 - var1.y() <= 0.6) {
         return null;
      }

      AiPhysics.MoveInput var7 = AiPhysics.MoveInput.run(var2, var3);
      if (!AiPhysics.step(var1, var7, var0).horizontalCollision()) {
         return null;
      }

      double var8 = var4.getX() + 0.5 - var1.x();
      double var10 = var4.getZ() + 0.5 - var1.z();
      double var12 = Math.hypot(var8, var10);
      if (var12 < 1.0E-6) {
         return null;
      }

      AiJumpSolver.JumpAxis var14 = new AiJumpSolver.JumpAxis(var1.x(), var1.z(), var8 / var12, var10 / var12, var12);
      AiJumpSolver.ArcResult var15 = simulateJumpArc(var0, var1, var7, var7, 2147483647, var14, var4, var5);
      if (var15 != null && var15.settled() && !(var15.margin() < -0.04)) {
         if (!(var15.margin() < 0.14)
            || var1.horizontalCollision() && var1.speed() < 0.025 && isFullFlatSupport(var0, var4.getX(), var4.getZ(), var5)) {
            return canReachCellCenter(var0, var15.end(), var4, var5)
               ? new AiJumpSolver.UiHelper(0, 0.0, 2147483647, var15.margin(), var3, var15.ticks() + 1)
               : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static double verifyTakeoffMargin(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, AiJumpSolver.UiHelper var3, BlockPos var4, double var5
   ) {
      return verifyTakeoffMargin(var0, var1, var2, var3, var4, var5, false);
   }

   static double verifyTakeoffMargin(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, AiJumpSolver.UiHelper var3, BlockPos var4, double var5, boolean var7
   ) {
      double var8 = var4.getX() + 0.5 - var1.x();
      double var10 = var4.getZ() + 0.5 - var1.z();
      double var12 = Math.hypot(var8, var10);
      if (var12 < 1.0E-6) {
         return -1.0;
      } else {
         AiJumpSolver.JumpAxis var14 = new AiJumpSolver.JumpAxis(var1.x(), var1.z(), var8 / var12, var10 / var12, var12);
         AiPhysics.MoveInput var15 = AiPhysics.MoveInput.run(var2, var3.sprint());
         AiJumpSolver.ArcResult var16 = simulateJumpArc(
            var0, var1, var15.withStrafe(var3.strafe()), var15, var3.coastAfter(), var14, var4, var5
         );
         if (var16 != null && var16.settled()) {
            return var7 && !var16.bumped() && var16.margin() < 0.065 ? measureCoastingSupportMargin(var0, var16.end(), var4, var5) : var16.margin();
         } else {
            return -1.0;
         }
      }
   }

   static boolean hitsAirborneWall(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, AiJumpSolver.UiHelper var3) {
      AiPhysics.MoveInput var4 = AiPhysics.MoveInput.run(var2, var3.sprint());
      AiPhysics.MoveInput var5 = new AiPhysics.MoveInput(0.0, 0.0, false, false, false, var2);
      AiPhysics.MotionState var6 = AiPhysics.step(var1, var4.withStrafe(var3.strafe()).withJump(true), var0);

      for (int var7 = 1; var7 <= 40; var7++) {
         if (var6.horizontalCollision() && !var6.onGround()) {
            return true;
         }

         if (isGroundedNotRising(var6)) {
            return false;
         }

         var6 = AiPhysics.step(var6, var7 <= var3.coastAfter() ? var4 : var5, var0);
      }

      return false;
   }

   static float findWallClearingYaw(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, AiJumpSolver.UiHelper var3, BlockPos var4, double var5
   ) {
      if (!hitsAirborneWall(var0, var1, var2, var3)) {
         return 0.0F / 0.0F;
      }

      for (float var10 : CLEAR_COLLISION_YAW_OFFSETS) {
         float var11 = var2 + var10;
         if (!hitsAirborneWall(var0, var1, var11, var3) && verifyTakeoffMargin(var0, var1, var11, var3, var4, var5) >= -0.04) {
            return var11;
         }
      }

      return 0.0F / 0.0F;
   }

   static double verifyDelayedTakeoffMargin(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, AiJumpSolver.UiHelper var3, int var4, BlockPos var5, double var6
   ) {
      AiPhysics.MotionState var8 = var1;
      AiPhysics.MoveInput var9 = AiPhysics.MoveInput.run(var2, var3.sprint()).withStrafe(var3.strafe());

      for (int var10 = 0; var10 < var4; var10++) {
         var8 = AiPhysics.step(var8, var9, var0);
         if (!var8.onGround() || AiPhysics.hitsStrictHazard(var0, var1, var8)) {
            return -1.0;
         }

         var1 = var8;
      }

      AiJumpSolver.UiHelper var11 = new AiJumpSolver.UiHelper(0, var3.strafe(), var3.coastAfter(), 0.0, var3.sprint(), var3.flightTicks());
      return verifyTakeoffMargin(var0, var8, var2, var11, var5, var6);
   }

   static boolean verifyRunupPath(AiPhysics.PhysicsWorld var0, AiJumpSolver.JumpPlan var1, double var2) {
      double var4 = Math.toRadians(var1.yaw());
      AiPhysics.MotionState var6 = new AiPhysics.MotionState(
         var1.startX(), var2, var1.startZ(), -Math.sin(var4) * var1.approach(), -0.0784, Math.cos(var4) * var1.approach(), true, 0, false, false
      );
      AiPhysics.MoveInput var7 = AiPhysics.MoveInput.run(var1.yaw(), var1.sprint());

      for (int var8 = 0; var8 < var1.runTicks(); var8++) {
         var6 = AiPhysics.step(var6, var7, var0);
         if (!var6.onGround()
            || var6.horizontalCollision()
            || Math.abs(var6.y() - var2) > 0.02
            || !canStandAt(var0, var6.x(), var6.y(), var6.z())) {
            return false;
         }
      }

      return true;
   }

   static AiJumpSolver.UiHelper solveTakeoffAllowEarlyCoast(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, boolean var3, BlockPos var4, double var5) {
      return solveTakeoff(var0, var1, var2, var3, var4, var5, 40, true);
   }

   static AiJumpSolver.JumpPlan solveCurvedSprintJump(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3
   ) {
      if (var1.onGround() && var1.pose() == 0 && !(Math.abs(var1.y() - var3) > 0.55)) {
         double var5 = var2.getX() + 0.5;
         double var7 = var2.getZ() + 0.5;
         double var9 = var5 - var1.x();
         double var11 = var7 - var1.z();
         double var13 = Math.hypot(var9, var11);
         if (!(var13 < 2.0) && !(var13 > 8.0)) {
            float var15 = AiPhysics.yawBetween(var1.x(), var1.z(), var5, var7);
            if (var1.vx() * var9 + var1.vz() * var11 >= -0.035 * var13) {
               AiJumpSolver.TurnSchedule[] var16 = new AiJumpSolver.TurnSchedule[CURVE_RUNUP_YAW_OFFSETS.length];

               for (int var17 = 0; var17 < var16.length; var17++) {
                  var16[var17] = new AiJumpSolver.TurnSchedule(null, 0, AiPhysics.MoveInput.run(var15 + CURVE_RUNUP_YAW_OFFSETS[var17], true));
               }

               AiJumpSolver.JumpPlan var35 = solveScheduledTurnJump(var0, var1, var2, var3, var16, 17, false);
               if (var35 != null) {
                  return var35;
               }
            }

            if (var1.speed() < 0.09) {
               return null;
            }

            double var34 = (var1.vx() * var11 - var1.vz() * var9) / var13;
            float var18 = var34 <= 0.0 ? -1.0F : 1.0F;
            AiJumpSolver.TurnSchedule[] var19 = new AiJumpSolver.TurnSchedule[24];
            int var20 = 0;

            for (int var24 : new int[]{4, 7, 10, 12}) {
               for (int var28 : new int[]{75, 138, 156}) {
                  AiPhysics.MoveInput var29 = AiPhysics.MoveInput.run(var15 + var18 * var28, true);

                  for (int var33 : new int[]{-5, 10}) {
                     var19[var20++] = new AiJumpSolver.TurnSchedule(var29, var24, AiPhysics.MoveInput.run(var15 + var18 * var33, true));
                  }
               }
            }

            return solveScheduledTurnJump(var0, var1, var2, var3, var19, 33, true);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static AiJumpSolver.JumpPlan solveScheduledTurnJump(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3, AiJumpSolver.TurnSchedule[] var5, int var6, boolean var7
   ) {
      double var8 = var2.getX() + 0.5;
      double var10 = var2.getZ() + 0.5;
      AiPhysics.MotionState[] var12 = new AiPhysics.MotionState[var5.length];
      Arrays.fill(var12, var1);
      AiJumpSolver.JumpPlan var13 = null;

      for (int var14 = 0; var14 <= var6; var14++) {
         boolean var15 = false;

         for (int var16 = 0; var16 < var12.length; var16++) {
            AiPhysics.MotionState var17 = var12[var16];
            if (var17 != null) {
               var15 = true;
               if ((var14 != 0 || var16 == 0) && var17.speed() >= 0.095) {
                  double var18 = var8 - var17.x();
                  double var20 = var10 - var17.z();
                  double var22 = Math.hypot(var18, var20);
                  if (var22 > 1.6 && var22 <= 5.2) {
                     AiJumpSolver.JumpAxis var24 = new AiJumpSolver.JumpAxis(var17.x(), var17.z(), var18 / var22, var20 / var22, var22);
                     double var25 = var18 - 8.0 * var17.vx();
                     double var27 = var20 - 8.0 * var17.vz();
                     if (var25 * var25 + var27 * var27 > 28.09) {
                        var12[var16] = stepOnFlatSurface(var0, var17, var5[var16].at(var14), var1.y());
                        continue;
                     }

                     float var29 = AiPhysics.yawBetween(0.0, 0.0, var25, var27);

                     for (int var33 : var7 ? WIDE_CURVE_ANGLES : CURVE_ENTRY_ANGLES) {
                        AiPhysics.MoveInput var34 = AiPhysics.MoveInput.run(var29 + var33, true);
                        AiJumpSolver.ArcResult var35 = simulateJumpArc(var0, var17, var34, var34, 2147483647, var24, var2, var3);
                        if (var35 != null && var35.settled() && !(Math.abs(var35.end().y() - var3) > 0.02)) {
                           if (var35.margin() < -0.04) {
                              if (var13 != null) {
                                 continue;
                              }

                              if (var35.along() >= var22) {
                                 var35 = optimizeCoastRelease(var0, var17, var34, var34, var35, var24, var2, var3);
                              }

                              if (var7 && !isSettledAtSurface(var35, var3)) {
                                 for (int var39 : CURVE_COAST_AFTER_TICKS) {
                                    AiJumpSolver.ArcResult var40 = simulateJumpArc(
                                       var0, var17, var34, var34, var39, var24, var2, var3
                                    );
                                    if (isSettledAtSurface(var40, var3)) {
                                       var35 = var40;
                                       break;
                                    }
                                 }
                              }

                              if (!isSettledAtSurface(var35, var3)) {
                                 continue;
                              }
                           }

                           ArrayList var41 = new ArrayList(var14 + var35.ticks() + 1);

                           for (int var42 = 0; var42 < var14; var42++) {
                              var41.add(var5[var16].at(var42));
                           }

                           var41.add(var34.withJump(true));

                           for (int var43 = 1; var43 <= var35.ticks(); var43++) {
                              var41.add(var43 <= var35.coastAfter() ? var34 : AiPhysics.MoveInput.NONE.withYaw(var34.yaw()));
                           }

                           AiPhysics.MotionState var44 = var35.end();
                           AiJumpManeuver var45 = new AiJumpManeuver(var41, var1, var44, var35.margin(), var14);
                           if (!(var45.replay(var0, var1, 0, var2, var3) < -0.04)) {
                              AiJumpSolver.JumpPlan var46 = new AiJumpSolver.JumpPlan(
                                 ((AiPhysics.MoveInput)var41.getFirst()).yaw(),
                                 var14,
                                 true,
                                 var35.coastAfter(),
                                 var35.ticks(),
                                 var1.x(),
                                 var1.z(),
                                 var44.x(),
                                 var44.y(),
                                 var44.z(),
                                 var44.speed(),
                                 var35.margin(),
                                 0.0,
                                 var1.speed(),
                                 var45
                              );
                              if (var35.coastAfter() == 2147483647) {
                                 return var46;
                              }

                              var13 = var46;
                           }
                        }
                     }
                  }
               }

               var12[var16] = stepOnFlatSurface(var0, var17, var5[var16].at(var14), var1.y());
            }
         }

         if (!var15 || var13 != null && var14 >= var13.runTicks() + 3) {
            break;
         }
      }

      return var13;
   }

   private static boolean isSettledAtSurface(AiJumpSolver.ArcResult var0, double var1) {
      return var0 != null && var0.settled() && Math.abs(var0.end().y() - var1) <= 0.02 && var0.margin() >= -0.04;
   }

   private static AiPhysics.MotionState stepOnFlatSurface(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MoveInput var2, double var3
   ) {
      if (!canStandAt(var0, var1.x(), var1.y(), var1.z())) {
         return null;
      }

      AiPhysics.MotionState var5 = AiPhysics.step(var1, var2, var0);
      return var5.onGround() && Math.abs(var5.y() - var3) <= 0.02 && !AiPhysics.hitsStrictHazard(var0, var1, var5) ? var5 : null;
   }

   static AiJumpSolver.JumpPlan solveIceRunupJump(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7
   ) {
      if (!var7) {
         return null;
      }

      double var8 = var1.getX() + 0.5;
      double var10 = var1.getZ() + 0.5;
      float var12 = AiPhysics.yawBetween(var8, var10, var4.getX() + 0.5, var4.getZ() + 0.5);
      float var13 = Math.round(var12 / 45.0F) * 45.0F;
      float var14 = Math.round(var12 / 5.0F) * 5.0F;
      double var15 = Math.toRadians(var13);
      double var17 = -Math.sin(var15);
      double var19 = Math.cos(var15);
      AiJumpSolver.JumpPlan var21 = null;

      for (double var25 : new double[]{0.0, 0.65, 1.0, 1.3, 1.6}) {
         double var27 = var8 - var17 * var25;
         double var29 = var10 - var19 * var25;
         if (isWalkLineStandable(var0, var8, var10, var27, var29, var2)) {
            AiPhysics.MotionState var31 = AiPhysics.MotionState.standing(var27, var2, var29);

            for (float var35 : new float[]{0.0F, -5.0F, 5.0F, -10.0F, 10.0F, -15.0F, 15.0F}) {
               if (Thread.currentThread().isInterrupted()) {
                  return null;
               }

               AiPhysics.MoveInput var36 = AiPhysics.MoveInput.run(var14 + var35, true);
               AiPhysics.MotionState var37 = var31;

               for (int var38 = 0; var38 <= 20 && var37.onGround(); var38++) {
                  if (var37.speed() >= 0.11 && Math.hypot(var37.x() - var8, var37.z() - var10) <= 1.4) {
                     double var39 = var4.getX() + 0.5 - var37.x();
                     double var41 = var4.getZ() + 0.5 - var37.z();
                     double var43 = Math.hypot(var39, var41);
                     if (var43 > 2.0 && var43 <= 5.1) {
                        AiJumpSolver.JumpAxis var45 = new AiJumpSolver.JumpAxis(var37.x(), var37.z(), var39 / var43, var41 / var43, var43);

                        for (float var49 : new float[]{0.0F, -5.0F, 5.0F, -10.0F, 10.0F, -15.0F, 15.0F}) {
                           AiPhysics.MoveInput var50 = var36.withYaw(var36.yaw() + var49);
                           AiJumpSolver.ArcResult var51 = simulateJumpArc(var0, var37, var50, var50, 2147483647, var45, var4, var5);
                           if (var51 != null) {
                              if (var51.along() > var43 && var51.margin() < 0.14) {
                                 var51 = optimizeCoastRelease(var0, var37, var50, var50, var51, var45, var4, var5);
                              }

                              if (var51 != null && var51.settled() && !(var51.margin() < -0.04)) {
                                 ArrayList var52 = new ArrayList();

                                 for (int var53 = 0; var53 < var38; var53++) {
                                    var52.add(var36);
                                 }

                                 var52.add(var50.withJump(true));

                                 for (int var57 = 1; var57 <= var51.ticks(); var57++) {
                                    var52.add(var57 <= var51.coastAfter() ? var50 : AiPhysics.MoveInput.NONE.withYaw(var50.yaw()));
                                 }

                                 AiJumpManeuver var58 = new AiJumpManeuver(var52, var31, var51.end(), var51.margin(), var38);
                                 if (!(var58.replay(var0, var31, 0, var4, var5) < -0.04)) {
                                    AiPhysics.MotionState var54 = var51.end();
                                    AiJumpSolver.JumpPlan var55 = new AiJumpSolver.JumpPlan(
                                       var36.yaw(),
                                       var38,
                                       true,
                                       var51.coastAfter(),
                                       var51.ticks(),
                                       var27,
                                       var29,
                                       var54.x(),
                                       var54.y(),
                                       var54.z(),
                                       var54.speed(),
                                       var51.margin(),
                                       var25,
                                       0.0,
                                       var58
                                    );
                                    if (var21 == null || isBetterJumpPlan(var55, var21)) {
                                       var21 = var55;
                                    }

                                    if (var21.margin() >= 0.14) {
                                       return var21;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  AiPhysics.MotionState var56 = AiPhysics.step(var37, var36, var0);
                  if (AiPhysics.hitsStrictHazard(var0, var37, var56)) {
                     break;
                  }

                  var37 = var56;
               }
            }
         }
      }

      return var21;
   }

   static AiJumpSolver.JumpPlan solveEdgeSetupJump(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5
   ) {
      double var7 = var1.getX() + 0.5;
      double var9 = var1.getZ() + 0.5;
      float var11 = AiPhysics.yawBetween(var7, var9, var4.getX() + 0.5, var4.getZ() + 0.5);
      float var12 = Math.round(var11 / 90.0F) * 90.0F;
      double var13 = Math.toRadians(var12);

      for (double var18 : new double[]{-0.45, 0.45}) {
         for (double var23 : new double[]{0.2, 0.0, -0.2}) {
            double var25 = var7 - Math.sin(var13) * var23 + Math.cos(var13) * var18;
            double var27 = var9 + Math.cos(var13) * var23 + Math.sin(var13) * var18;
            if (canStandAt(var0, var25, var2, var27)) {
               AiPhysics.MotionState var29 = AiPhysics.MotionState.standing(var25, var2, var27);

               for (float var33 : new float[]{0.0F, -8.0F, 8.0F}) {
                  if (Thread.currentThread().isInterrupted()) {
                     return null;
                  }

                  AiPhysics.MoveInput var34 = AiPhysics.MoveInput.run(var12 + var33, true);
                  AiPhysics.MotionState var35 = var29;

                  for (int var36 = 0; var36 <= 8 && var35.onGround(); var36++) {
                     double var37 = var4.getX() + 0.5 - var35.x();
                     double var39 = var4.getZ() + 0.5 - var35.z();
                     double var41 = Math.hypot(var37, var39);
                     if (var41 >= 1.0 && var35.speed() >= 0.08) {
                        AiJumpSolver.JumpAxis var43 = new AiJumpSolver.JumpAxis(var35.x(), var35.z(), var37 / var41, var39 / var41, var41);
                        float var44 = Mth.wrapDegrees(var11 - var12);

                        for (float var48 : new float[]{30.0F, 40.0F, 50.0F}) {
                           AiPhysics.MoveInput var49 = var34.withYaw(var34.yaw() + Math.copySign(var48, var44));
                           AiJumpSolver.ArcResult var50 = simulateJumpArc(var0, var35, var49, var49, 2147483647, var43, var4, var5);
                           if (var50 != null && var50.settled()) {
                              if (var50.margin() < -0.04 && var50.along() >= var41) {
                                 var50 = optimizeCoastRelease(var0, var35, var49, var49, var50, var43, var4, var5);
                              }

                              if (var50 != null && !(var50.margin() < -0.04)) {
                                 ArrayList var51 = new ArrayList();

                                 for (int var52 = 0; var52 < var36; var52++) {
                                    var51.add(var34);
                                 }

                                 var51.add(var49.withJump(true));

                                 for (int var55 = 1; var55 <= var50.ticks(); var55++) {
                                    var51.add(var55 <= var50.coastAfter() ? var49 : AiPhysics.MoveInput.NONE.withYaw(var49.yaw()));
                                 }

                                 AiPhysics.MotionState var56 = var50.end();
                                 AiJumpManeuver var53 = new AiJumpManeuver(var51, var29, var56, var50.margin(), var36);
                                 if (var53.replay(var0, var29, 0, var4, var5) >= -0.04) {
                                    return new AiJumpSolver.JumpPlan(
                                       var34.yaw(),
                                       var36,
                                       true,
                                       var50.coastAfter(),
                                       var50.ticks(),
                                       var25,
                                       var27,
                                       var56.x(),
                                       var56.y(),
                                       var56.z(),
                                       var56.speed(),
                                       var50.margin(),
                                       Math.hypot(var18, var23),
                                       0.0,
                                       var53
                                    );
                                 }
                              }
                           }
                        }
                     }

                     AiPhysics.MotionState var54 = AiPhysics.step(var35, var34, var0);
                     if (AiPhysics.hitsStrictHazard(var0, var35, var54)) {
                        break;
                     }

                     var35 = var54;
                  }
               }
            }
         }
      }

      return Math.abs(Mth.wrapDegrees(var11 - var12)) < 12.0F ? solveLateralRunupJump(var0, var1, var2, var4, var5, var12) : null;
   }

   private static AiJumpSolver.JumpPlan solveLateralRunupJump(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, float var7
   ) {
      if (var5 > var2 + 1.26) {
         return null;
      }

      double var8 = Math.toRadians(var7);
      double var10 = -Math.sin(var8);
      double var12 = Math.cos(var8);
      double var14 = var4.getX() + 0.5;
      double var16 = var4.getZ() + 0.5;

      for (double var21 : new double[]{-0.7, 0.7, -0.6, 0.6, -0.5, 0.5}) {
         for (double var26 : new double[]{0.9, 1.3, 1.8}) {
            double var28 = var1.getX() + 0.5 + var12 * var21 - var10 * var26;
            double var30 = var1.getZ() + 0.5 - var10 * var21 - var12 * var26;
            if (canStandAt(var0, var28, var2, var30)) {
               AiPhysics.MotionState var32 = AiPhysics.MotionState.standing(var28, var2, var30);

               for (float var36 : new float[]{-8.0F, -4.0F, 0.0F, 4.0F, 8.0F}) {
                  if (Thread.currentThread().isInterrupted()) {
                     return null;
                  }

                  AiPhysics.MoveInput var37 = AiPhysics.MoveInput.run(var7 + var36, true);
                  AiPhysics.MotionState var38 = var32;
                  ArrayList<AiPhysics.MoveInput> var39 = new ArrayList<>();

                  for (int var40 = 0; var40 < 14 && var38.onGround(); var40++) {
                     if (var38.speed() >= 0.1) {
                        double var41 = AiJumpManeuver.maxFlightReach(var0, var38, var5, true);
                        if (var41 >= 0.0 && Math.hypot(var14 - var38.x(), var16 - var38.z()) <= var41 + 1.15) {
                           for (float var46 : new float[]{38.0F, 28.0F, 20.0F}) {
                              AiPhysics.MoveInput var47 = var37.withYaw(var37.yaw() + Math.copySign(var46, (float)var21)).withJump(true);
                              AiPhysics.MotionState var48 = AiPhysics.step(var38, var47, var0);
                              if (!var48.onGround() && !(var48.y() < var2 + 0.1) && !AiPhysics.hitsStrictHazard(var0, var38, var48)) {
                                 AiPhysics.MoveInput var49 = var47.withJump(false);
                                 AiPhysics.MotionState[] var50 = new AiPhysics.MotionState[5];
                                 var50[0] = var48;

                                 for (int var51 = 1; var51 <= 4; var51++) {
                                    AiPhysics.MotionState var52 = var50[var51 - 1];
                                    if (var52 == null || isGroundedNotRising(var52)) {
                                       break;
                                    }

                                    AiPhysics.MotionState var53 = AiPhysics.step(var52, var49, var0);
                                    if (var53.y() < var5 - 0.02 || AiPhysics.hitsStrictHazard(var0, var52, var53)) {
                                       break;
                                    }

                                    var50[var51] = var53;
                                 }

                                 for (int var54 : new int[]{2, 0, 4}) {
                                    AiPhysics.MotionState var55 = var50[var54];
                                    if (var55 != null) {
                                       if (isGroundedNotRising(var55)) {
                                          BlockPos var73 = BlockPos.containing(var55.x(), var4.getY(), var55.z());
                                          if (var73.equals(var4) && Math.abs(var55.y() - var5) <= 0.02) {
                                             double var74 = settledLandingMargin(var0, var55, var4, var5, var49.yaw());
                                             if (var74 >= -0.04) {
                                                var var75 = new ArrayList<>(var39);
                                                var75.add(var47);

                                                for (int var76 = 0; var76 < var54; var76++) {
                                                   var75.add(var49);
                                                }

                                                AiJumpManeuver var77 = new AiJumpManeuver(var75, var32, var55, var74, var40);
                                                if (var77.replay(var0, var32, 0, var4, var5) >= -0.04) {
                                                   return new AiJumpSolver.JumpPlan(
                                                      var37.yaw(),
                                                      var40,
                                                      true,
                                                      2147483647,
                                                      var54,
                                                      var28,
                                                      var30,
                                                      var55.x(),
                                                      var55.y(),
                                                      var55.z(),
                                                      var55.speed(),
                                                      var74,
                                                      Math.hypot(var21, var26),
                                                      0.0,
                                                      var77
                                                   );
                                                }
                                             }
                                          }
                                       } else {
                                          for (float var59 : new float[]{65.0F, 50.0F, 35.0F}) {
                                             AiPhysics.MoveInput var60 = var37.withYaw(var37.yaw() - Math.copySign(var59, (float)var21));
                                             AiPhysics.MotionState var61 = var55;

                                             for (int var62 = var54; var62 < 18; var62++) {
                                                AiPhysics.MotionState var63 = AiPhysics.step(var61, var60, var0);
                                                if (var63.y() < var5 - 0.02 || AiPhysics.hitsStrictHazard(var0, var61, var63)) {
                                                   break;
                                                }

                                                var61 = var63;
                                                if (isGroundedNotRising(var61)) {
                                                   BlockPos var64 = BlockPos.containing(var61.x(), var4.getY(), var61.z());
                                                   if (!var64.equals(var4) || !(Math.abs(var61.y() - var5) <= 0.02)) {
                                                      break;
                                                   }

                                                   double var65 = settledLandingMargin(var0, var61, var4, var5, var60.yaw());
                                                   if (!(var65 >= -0.04)) {
                                                      break;
                                                   }

                                                   var var67 = new ArrayList<>(var39);
                                                   var67.add(var47);

                                                   for (int var68 = 0; var68 <= var62; var68++) {
                                                      var67.add(var68 < var54 ? var49 : var60);
                                                   }

                                                   AiJumpManeuver var78 = new AiJumpManeuver(var67, var32, var61, var65, var40);
                                                   if (var78.replay(var0, var32, 0, var4, var5) >= -0.04) {
                                                      return new AiJumpSolver.JumpPlan(
                                                         var37.yaw(),
                                                         var40,
                                                         true,
                                                         2147483647,
                                                         var62 + 1,
                                                         var28,
                                                         var30,
                                                         var61.x(),
                                                         var61.y(),
                                                         var61.z(),
                                                         var61.speed(),
                                                         var65,
                                                         Math.hypot(var21, var26),
                                                         0.0,
                                                         var78
                                                      );
                                                   }
                                                   break;
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

                     AiPhysics.MotionState var69 = AiPhysics.step(var38, var37, var0);
                     if (AiPhysics.hitsStrictHazard(var0, var38, var69)) {
                        break;
                     }

                     var39.add(var37);
                     var38 = var69;
                  }
               }
            }
         }
      }

      return null;
   }

   static AiJumpSolver.JumpPlan solveFollowUpJump(
      AiPhysics.PhysicsWorld var0, AiJumpSolver.JumpPlan var1, BlockPos var2, double var3, boolean var5
   ) {
      double var6 = Math.toRadians(var1.yaw());
      AiPhysics.MotionState var8 = var1.maneuver() == null
         ? new AiPhysics.MotionState(
            var1.landX(), var1.landY(), var1.landZ(), -Math.sin(var6) * var1.landSpeed(), -0.0784, Math.cos(var6) * var1.landSpeed(), true, 0, false, true
         )
         : var1.maneuver().landing();
      return solveMomentumJump(var0, var8, var2, var3, var5);
   }

   static AiJumpSolver.JumpPlan solveMomentumJump(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3, boolean var5
   ) {
      return solveMomentumJump(var0, var1, var2, var3, var5, 0.1);
   }

   static AiJumpSolver.JumpPlan solveRunningTurnJump(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3, float var5
   ) {
      return var1.speed() < 0.075 ? null : solveRunningTurnJump(var0, var1, var2, var3, var5, 2);
   }

   static AiJumpSolver.JumpPlan solveStrideJump(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3, float var5
   ) {
      float var6 = AiPhysics.yawBetween(var1.x(), var1.z(), var2.getX() + 0.5, var2.getZ() + 0.5);
      if (Math.abs(Mth.wrapDegrees(var6 - var5)) > 30.0F) {
         return null;
      }

      AiPhysics.MoveInput var7 = AiPhysics.MoveInput.run(var6, true);
      AiStride.Inner_TableCache var8 = AiStride.putAt(var0, var1, var7);
      if (var8 == null) {
         return null;
      }

      AiJumpSolver.JumpPlan var9 = solveRunningTurnJump(var0, var8.at(), var2, var3, var6, 5);
      if (var9 == null) {
         return null;
      }

      AiJumpSolver.UiHelper var10 = solveTakeoffAllowEarlyCoast(var0, var1, var6, true, var2, var3);
      int var11 = var8.ticks() + var9.ticks();
      if (var10.margin() >= -0.12 && var11 >= var10.runTicks() + var10.flightTicks()) {
         return null;
      }

      ArrayList var12 = new ArrayList(var11);
      var12.add(var7.withJump(true));

      for (int var13 = 1; var13 < var8.ticks(); var13++) {
         var12.add(var7);
      }

      var12.addAll(var9.maneuver().inputs());
      AiJumpManeuver var14 = new AiJumpManeuver(var12, var1, var9.maneuver().landing(), var9.margin(), var8.ticks() + var9.runTicks());
      return var14.replay(var0, var1, 0, var2, var3) < -0.04
         ? null
         : new AiJumpSolver.JumpPlan(
            var6,
            var8.ticks() + var9.runTicks(),
            true,
            var9.coastAfter(),
            var9.flightTicks(),
            var1.x(),
            var1.z(),
            var9.landX(),
            var9.landY(),
            var9.landZ(),
            var9.landSpeed(),
            var9.margin(),
            0.0,
            var1.speed(),
            var14
         );
   }

   static List<AiJumpSolver.JumpPlan> solveJumpChain(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, List<AiJumpSolver.SurfaceCell> var3
   ) {
      List var4 = List.of(new AiJumpSolver.ChainNode(0, var2, var1, List.of()));

      for (int var5 = 0; var5 < var3.size(); var5++) {
         if (Thread.currentThread().isInterrupted()) {
            return List.of();
         }

         AiJumpSolver.SurfaceCell var6 = (AiJumpSolver.SurfaceCell)var3.get(var5);
         AiJumpSolver.SurfaceCell var7 = var5 + 1 < var3.size() ? (AiJumpSolver.SurfaceCell)var3.get(var5 + 1) : null;
         ArrayList<AiJumpSolver.ChainNode> var8 = new ArrayList<>();

         for (AiJumpSolver.ChainNode var10 : (Iterable<AiJumpSolver.ChainNode>)(Iterable<?>)(var4)) {
            for (AiJumpSolver.JumpPlan var12 : solvePaceHopCandidates(var0, var10.state(), var10.yaw(), var6.cell(), var6.surface(), var7)) {
               var var13 = new ArrayList<>(var10.flights());
               var13.add(var12);
               AiJumpManeuver var14 = var12.maneuver();
               var8.add(new AiJumpSolver.ChainNode(var10.cost() + var12.ticks(), var14.inputs().getLast().yaw(), var14.landing(), var13));
            }
         }

         if (var8.isEmpty()) {
            return List.of();
         }

         var8.sort(Comparator.comparingDouble(var1x -> var1x.cost() + scoreChainContinuation(var1x.state(), var1x.yaw(), var7)));
         ArrayList var15 = new ArrayList(5);

         for (AiJumpSolver.ChainNode var17 : var8) {
            AiPhysics.MotionState var18 = var17.state();
            if (!var15.stream()
               .anyMatch(
                  var1x -> Math.hypot(((AiJumpSolver.ChainNode)var1x).state().x() - var18.x(), ((AiJumpSolver.ChainNode)var1x).state().z() - var18.z()) < 0.16
                     && Math.hypot(((AiJumpSolver.ChainNode)var1x).state().vx() - var18.vx(), ((AiJumpSolver.ChainNode)var1x).state().vz() - var18.vz()) < 0.04
               )) {
               var15.add(var17);
               if (var15.size() >= 5 || var7 == null) {
                  break;
               }
            }
         }

         var4 = var15;
      }

      return ((AiJumpSolver.ChainNode)var4.getFirst()).flights();
   }

   static AiJumpSolver.JumpPlan solveFastPaceJump(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, BlockPos var3, double var4
   ) {
      if (isGroundedNotRising(var1) && !(var1.speed() < 0.1) && var1.pose() == 0) {
         var var6 = solvePaceHopCandidates(var0, var1, var2, var3, var4, null);
         AiJumpSolver.JumpPlan var7 = null;

         for (AiJumpSolver.JumpPlan var9 : var6) {
            if (var9.runTicks() <= 5
               && var9.ticks() <= 20
               && !(var9.landSpeed() < 0.09)
               && !(var9.maneuver().replay(var0, var1, 0, var3, var4) < 0.09)
               && (var7 == null || var9.ticks() < var7.ticks() || var9.ticks() == var7.ticks() && var9.landSpeed() > var7.landSpeed())) {
               var7 = var9;
            }
         }

         return var7;
      } else {
         return null;
      }
   }

   private static double scoreChainContinuation(AiPhysics.MotionState var0, float var1, AiJumpSolver.SurfaceCell var2) {
      if (var2 == null) {
         return 0.0;
      }

      double var3 = var2.cell().getX() + 0.5 - var0.x();
      double var5 = var2.cell().getZ() + 0.5 - var0.z();
      double var7 = Math.hypot(var3, var5);
      double var9 = var7 > 0.01 ? (var0.vx() * var3 + var0.vz() * var5) / var7 : 0.0;
      float var11 = Mth.wrapDegrees(AiPhysics.yawBetween(0.0, 0.0, var3, var5) - var1);
      return 2.0 * var7 - 8.0 * var9 + 0.025 * Math.abs(var11);
   }

   private static List<AiJumpSolver.JumpPlan> solvePaceHopCandidates(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, BlockPos var3, double var4, AiJumpSolver.SurfaceCell var6
   ) {
      double var7 = var3.getX() + 0.5 - var1.x();
      double var9 = var3.getZ() + 0.5 - var1.z();
      double var11 = Math.hypot(var7, var9);
      if (!(var11 < 1.35) && !(var11 > 7.7) && !(var4 - var1.y() > 1.29) && !(var1.y() - var4 > 2.25)) {
         AiJumpSolver.JumpAxis var13 = new AiJumpSolver.JumpAxis(var1.x(), var1.z(), var7 / var11, var9 / var11, var11);
         float var14 = AiPhysics.yawBetween(0.0, 0.0, var7, var9);
         ArrayList<AiJumpSolver.JumpPlan> var15 = new ArrayList<>();

         for (float var19 : PACE_YAW_OFFSETS) {
            float var20 = var14 + var19;
            AiPhysics.MoveInput var21 = AiPhysics.MoveInput.run(var20, true);
            AiPhysics.MotionState var22 = var1;
            ArrayList<AiPhysics.MoveInput> var23 = new ArrayList<>(8);
            float var24 = var2;
            float var25 = var6 == null ? 0.0F : Mth.wrapDegrees(AiPhysics.yawBetween(var3.getX(), var3.getZ(), var6.cell().getX(), var6.cell().getZ()) - var20);
            float var26 = var20 + Math.clamp(var25, -22.0F, 22.0F);
            var var27 = List.of(var21, AiPhysics.MoveInput.run(var26, true), var21.withStrafe(-1.0), var21.withStrafe(1.0));

            for (int var28 = 0; var28 <= 8; var28++) {
               if (Math.abs(Mth.wrapDegrees(var20 - var24)) <= 28.0F) {
                  for (AiPhysics.MoveInput var30 : var27) {
                     AiJumpSolver.ArcResult var31 = simulateJumpArc(var0, var22, var21, var30, 2147483647, var13, var3, var4);
                     if (var31 != null && var31.along() > var11 && var31.margin() < 0.075) {
                        var31 = optimizeCoastRelease(var0, var22, var21, var30, var31, var13, var3, var4);
                     }

                     if (var31 != null
                        && var31.settled()
                        && !var31.wall()
                        && !(var31.margin() < 0.075)
                        && !(Math.hypot(var31.end().x() - (var3.getX() + 0.5), var31.end().z() - (var3.getZ() + 0.5)) > 0.74)) {
                        ArrayList var32 = new ArrayList(var28 + var31.ticks() + 1);
                        var32.addAll(var23);
                        var32.add(var21.withJump(true));
                        AiPhysics.MoveInput var33 = AiPhysics.MoveInput.NONE.withYaw(var30.yaw());

                        for (int var34 = 0; var34 < var31.ticks(); var34++) {
                           var32.add(var34 < var31.coastAfter() ? var30 : var33);
                        }

                        AiPhysics.MotionState var38 = var31.end();
                        AiJumpManeuver var35 = new AiJumpManeuver(var32, var1, var38, var31.margin(), var28);
                        var15.add(
                           new AiJumpSolver.JumpPlan(
                              var20,
                              var28,
                              true,
                              var31.coastAfter(),
                              var31.ticks(),
                              var1.x(),
                              var1.z(),
                              var38.x(),
                              var38.y(),
                              var38.z(),
                              var38.speed(),
                              var31.margin(),
                              0.0,
                              var1.speed(),
                              var35
                           )
                        );
                     }
                  }
               }

               var24 += Math.clamp(Mth.wrapDegrees(var20 - var24), -28.0F, 28.0F);
               AiPhysics.MoveInput var36 = AiPhysics.MoveInput.run(var24, true);
               AiPhysics.MotionState var37 = AiPhysics.step(var22, var36, var0);
               if (!var37.onGround() || var37.horizontalCollision() || Math.abs(var37.y() - var1.y()) > 0.02 || AiPhysics.hitsStrictHazard(var0, var22, var37)) {
                  break;
               }

               var22 = var37;
               var23.add(var36);
            }
         }

         return var15;
      } else {
         return List.of();
      }
   }

   static AiJumpSolver.JumpPlan solveSharpCornerJump(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3, float var5) {
      if (isGroundedNotRising(var1) && var1.pose() == 0 && !(var1.speed() < 0.1)) {
         float var6 = AiPhysics.yawBetween(var1.x(), var1.z(), var2.getX() + 0.5, var2.getZ() + 0.5);
         float var7 = AiPhysics.yawBetween(0.0, 0.0, var1.vx(), var1.vz());
         float var8 = Math.abs(Mth.wrapDegrees(var6 - var7));
         if (!(var8 < 45.0F) && !(var8 > 100.0F)) {
            ArrayList<AiPhysics.MoveInput> var9 = new ArrayList<>(2);
            AiPhysics.MotionState var10 = var1;
            float var11 = var5;

            for (int var12 = 0; var12 < 2; var12++) {
               var11 += Math.clamp(Mth.wrapDegrees(var6 - var11), -28.0F, 28.0F);
               float var13 = Mth.wrapDegrees(var6 - var11);
               AiPhysics.MoveInput var14 = AiPhysics.MoveInput.run(var11, true).withStrafe(Math.abs(var13) > 22.5 ? -Math.signum(var13) : 0.0);
               AiPhysics.MotionState var15 = AiPhysics.step(var10, var14, var0);
               if (!var15.onGround()
                  || var15.horizontalCollision()
                  || Math.abs(var15.y() - var1.y()) > 0.02
                  || !canStandAt(var0, var15.x(), var15.y(), var15.z())
                  || AiPhysics.hitsStrictHazard(var0, var10, var15)) {
                  break;
               }

               var9.add(var14);
               var10 = var15;
               AiJumpSolver.JumpPlan var16 = solveRunningTurnJump(var0, var10, var2, var3, var11, 3 - var12);
               if (var16 != null && !(Math.abs(Mth.wrapDegrees(var16.yaw() - var11)) > 34.0F)) {
                  int var17 = var16.runTicks() + var9.size();
                  var var18 = new ArrayList<>(var9);
                  var18.addAll(var16.maneuver().inputs());
                  AiJumpManeuver var19 = new AiJumpManeuver(var18, var1, var16.maneuver().landing(), var16.margin(), var17);
                  if (var19.replay(var0, var1, 0, var2, var3) >= 0.075) {
                     return new AiJumpSolver.JumpPlan(
                        ((AiPhysics.MoveInput)var9.getFirst()).yaw(),
                        var17,
                        true,
                        var16.coastAfter(),
                        var16.flightTicks(),
                        var1.x(),
                        var1.z(),
                        var16.landX(),
                        var16.landY(),
                        var16.landZ(),
                        var16.landSpeed(),
                        var16.margin(),
                        0.0,
                        var1.speed(),
                        var19
                     );
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

   static AiJumpSolver.JumpPlan solveRunningTurnJump(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3, float var5, int var6
   ) {
      if (isGroundedNotRising(var1) && var1.pose() == 0 && !var1.horizontalCollision()) {
         double var7 = var2.getX() + 0.5 - var1.x();
         double var9 = var2.getZ() + 0.5 - var1.z();
         double var11 = Math.hypot(var7, var9);
         if (!(var11 < 1.35) && !(var11 > 5.65) && !(var3 - var1.y() > 1.25) && !(var1.y() - var3 > 2.1)) {
            double var13 = var7 / var11;
            double var15 = var9 / var11;
            double var17 = var1.vx() * var13 + var1.vz() * var15;
            if (var17 < -0.025) {
               return null;
            }

            float var19 = AiPhysics.yawBetween(var1.x(), var1.z(), var2.getX() + 0.5, var2.getZ() + 0.5);
            AiJumpSolver.JumpAxis var20 = new AiJumpSolver.JumpAxis(var1.x(), var1.z(), var13, var15, var11);
            float var21 = Mth.wrapDegrees(var5 - var19);
            float[] var22 = new float[]{var19 + Math.clamp(var21, -15.0F, 15.0F), var19, var19 - 9.0F, var19 + 9.0F};
            AiJumpSolver.JumpPlan var23 = null;

            for (float var27 : var22) {
               AiPhysics.MoveInput var28 = AiPhysics.MoveInput.run(var27, true);
               var var29 = simulateRunupStates(var0, var1, var28, var6);

               for (int var30 = 0; var30 < var29.size() && (var23 == null || var30 <= var23.runTicks()); var30++) {
                  AiPhysics.MotionState var31 = (AiPhysics.MotionState)var29.get(var30);
                  if (var31.horizontalCollision() || var31.speed() < var1.speed() * 0.7) {
                     break;
                  }

                  if (!(var31.vx() * var13 + var31.vz() * var15 < 0.035)) {
                     for (int var35 : new int[]{2147483647, 5, 8}) {
                        AiJumpSolver.ArcResult var36 = simulateJumpArc(var0, var31, var28, var28, var35, var20, var2, var3);
                        if (var36 != null
                           && var36.settled()
                           && !var36.bumped()
                           && !(var36.margin() < 0.075)
                           && isLandedOnCell(var36.end().x(), var36.end().y(), var36.end().z(), var2, var3)
                           && (var23 == null || var30 != var23.runTicks() || !(var36.end().speed() <= var23.landSpeed() + 0.015))) {
                           ArrayList var37 = new ArrayList();

                           for (int var38 = 0; var38 < var30; var38++) {
                              var37.add(var28);
                           }

                           var37.add(var28.withJump(true));

                           for (int var40 = 1; var40 <= var36.ticks(); var40++) {
                              var37.add(var40 <= var36.coastAfter() ? var28 : AiPhysics.MoveInput.NONE.withYaw(var27));
                           }

                           AiPhysics.MotionState var41 = var36.end();
                           AiJumpManeuver var39 = new AiJumpManeuver(var37, var1, var41, var36.margin(), var30);
                           if (!(var39.replay(var0, var1, 0, var2, var3) < 0.075)) {
                              var23 = new AiJumpSolver.JumpPlan(
                                 var27,
                                 var30,
                                 true,
                                 var36.coastAfter(),
                                 var36.ticks(),
                                 var1.x(),
                                 var1.z(),
                                 var41.x(),
                                 var41.y(),
                                 var41.z(),
                                 var41.speed(),
                                 var36.margin(),
                                 0.0,
                                 var1.speed(),
                                 var39
                              );
                              if (var30 == 0 && var36.coastAfter() == 2147483647 && var41.speed() >= 0.15) {
                                 return var23;
                              }
                           }
                        }
                     }
                  }
               }
            }

            return var23;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static AiJumpSolver.JumpPlan solveMomentumJump(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3, boolean var5, double var6
   ) {
      double var8 = var2.getX() + 0.5 - var1.x();
      double var10 = var2.getZ() + 0.5 - var1.z();
      double var12 = Math.hypot(var8, var10);
      if (isGroundedNotRising(var1) && !(var12 < 2.0) && !((var1.vx() * var8 + var1.vz() * var10) / var12 < var6)) {
         AiJumpSolver.JumpAxis var14 = new AiJumpSolver.JumpAxis(var1.x(), var1.z(), var8 / var12, var10 / var12, var12);
         float var15 = AiPhysics.yawBetween(var1.x(), var1.z(), var2.getX() + 0.5, var2.getZ() + 0.5);

         for (float var19 : new float[]{0.0F, -5.0F, 5.0F, -10.0F, 10.0F, -15.0F, 15.0F}) {
            float var20 = var15 + var19;
            AiJumpSolver.UiHelper var21 = solveTakeoff(var0, var1, var20, var5, var2, var3, 3);
            if (!(var21.margin() < -0.04)) {
               AiPhysics.MoveInput var22 = AiPhysics.MoveInput.run(var20, var5);
               AiPhysics.MoveInput var23 = var22.withStrafe(var21.strafe());
               var var24 = simulateRunupStates(var0, var1, var23, var21.runTicks());
               if (var24.size() > var21.runTicks()) {
                  AiJumpSolver.ArcResult var25 = simulateJumpArc(
                     var0, (AiPhysics.MotionState)var24.getLast(), var23, var22, var21.coastAfter(), var14, var2, var3
                  );
                  if (var25 != null && var25.settled() && !(var25.margin() < -0.04)) {
                     ArrayList var26 = new ArrayList();

                     for (int var27 = 0; var27 < var21.runTicks(); var27++) {
                        var26.add(var23);
                     }

                     var26.add(var23.withJump(true));

                     for (int var28 = 1; var28 <= var25.ticks(); var28++) {
                        var26.add(var28 <= var25.coastAfter() ? var22 : AiPhysics.MoveInput.NONE.withYaw(var20));
                     }

                     AiJumpManeuver var29 = new AiJumpManeuver(var26, var1, var25.end(), var25.margin(), var21.runTicks());
                     return new AiJumpSolver.JumpPlan(
                        var20,
                        var21.runTicks(),
                        var5,
                        var25.coastAfter(),
                        var25.ticks(),
                        var1.x(),
                        var1.z(),
                        var25.end().x(),
                        var25.end().y(),
                        var25.end().z(),
                        var25.end().speed(),
                        var25.margin(),
                        0.0,
                        var1.speed(),
                        var29
                     );
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static AiJumpSolver.JumpPlan solveUncoastedChainedJump(
      AiPhysics.PhysicsWorld var0, AiJumpSolver.JumpPlan var1, BlockPos var2, double var3, double var5
   ) {
      if (var1.maneuver() == null && var1.sprint() && !var1.running() && var1.coastAfter() != 2147483647) {
         AiPhysics.MoveInput var7 = AiPhysics.MoveInput.run(var1.yaw(), true);
         AiPhysics.MotionState var8 = AiPhysics.MotionState.standing(var1.startX(), var3, var1.startZ());
         var var9 = simulateRunupStates(var0, var8, var7, var1.runTicks());
         if (var9.size() <= var1.runTicks()) {
            return null;
         }

         double var10 = var2.getX() + 0.5 - var8.x();
         double var12 = var2.getZ() + 0.5 - var8.z();
         double var14 = Math.hypot(var10, var12);
         if (var14 < 2.0) {
            return null;
         }

         AiJumpSolver.JumpAxis var16 = new AiJumpSolver.JumpAxis(var8.x(), var8.z(), var10 / var14, var12 / var14, var14);
         AiJumpSolver.ArcResult var17 = simulateJumpArc(
            var0, (AiPhysics.MotionState)var9.getLast(), var7, var7, 2147483647, var16, var2, var5
         );
         if (var17 != null
            && var17.settled()
            && !(var17.end().speed() < var1.landSpeed() + 0.04)
            && !(Math.abs(var17.end().y() - var5) > 0.02)
            && isLandedOnCell(var17.end().x(), var17.end().y(), var17.end().z(), var2, var5)
            && canStandAt(var0, var17.end().x(), var5, var17.end().z())) {
            ArrayList var18 = new ArrayList();

            for (int var19 = 0; var19 < var1.runTicks(); var19++) {
               var18.add(var7);
            }

            var18.add(var7.withJump(true));

            for (int var20 = 0; var20 < var17.ticks(); var20++) {
               var18.add(var7);
            }

            AiJumpManeuver var21 = new AiJumpManeuver(var18, var8, var17.end(), var17.margin(), var1.runTicks());
            return new AiJumpSolver.JumpPlan(
               var1.yaw(),
               var1.runTicks(),
               true,
               2147483647,
               var17.ticks(),
               var8.x(),
               var8.z(),
               var17.end().x(),
               var17.end().y(),
               var17.end().z(),
               var17.end().speed(),
               var17.margin(),
               var1.backup(),
               0.0,
               var21
            );
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static AiJumpSolver.UiHelper solveTakeoff(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, boolean var3, BlockPos var4, double var5, int var7
   ) {
      return solveTakeoff(var0, var1, var2, var3, var4, var5, var7, false);
   }

   private static AiJumpSolver.UiHelper solveTakeoff(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, boolean var3, BlockPos var4, double var5, int var7, boolean var8
   ) {
      double var9 = var4.getX() + 0.5;
      double var11 = var4.getZ() + 0.5;
      double var13 = var9 - var1.x();
      double var15 = var11 - var1.z();
      double var17 = Math.hypot(var13, var15);
      if (var17 < 1.0E-6) {
         return new AiJumpSolver.UiHelper(0, 0.0, 2147483647, -1.0, var3);
      }

      AiJumpSolver.JumpAxis var19 = new AiJumpSolver.JumpAxis(var1.x(), var1.z(), var13 / var17, var15 / var17, var17);
      AiPhysics.MoveInput var20 = AiPhysics.MoveInput.run(var2, var3);
      AiJumpSolver.ArcResult var21 = simulateJumpArc(var0, var1, var20, var20, 2147483647, var19, var4, var5);
      if (var21 != null && var21.settled() && !var21.bumped() && var21.margin() >= 0.08 && cellEdgeMargin(var21.end().x(), var21.end().z(), var4) >= 0.08) {
         return new AiJumpSolver.UiHelper(0, 0.0, 2147483647, var21.margin(), var3, var21.ticks() + 1);
      }

      AiJumpSolver.UiHelper var22 = null;
      double var23 = -1.0 / 0.0;

      for (double var28 : var8 ? new double[]{0.0} : new double[]{0.0, 1.0, -1.0}) {
         AiPhysics.MoveInput var30 = var20.withStrafe(var28);
         AiJumpSolver.RunArc var31 = findBestRunArc(
            var0, simulateRunupStates(var0, var1, var30, Math.clamp(var7, 0, 40)), var30, var20, var19, var4, var5, true, false
         );
         double var32 = var31 == null ? -1.0 : var31.arc().margin();
         int var34 = var31 == null ? 0 : var31.runTicks();
         double var35 = var32 - 0.01 * var34 - (var28 == 0.0 ? 0.0 : 0.03);
         if (var35 > var23) {
            var23 = var35;
            var22 = new AiJumpSolver.UiHelper(
               var34, var28, var31 == null ? 2147483647 : var31.arc().coastAfter(), var32, var3, var31 == null ? 0 : var31.arc().ticks() + 1
            );
         }

         if (var28 == 0.0 && var32 >= 0.0) {
            break;
         }
      }

      return var22;
   }

   public static AiJumpSolver.ApproachOffset solveAirControl(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, boolean var3, BlockPos var4, double var5
   ) {
      return solveAirControlForTarget(var0, var1, var2, var3, var4, var5, (AiJumpSolver.LandingTarget)null);
   }

   public static AiJumpSolver.ApproachOffset solveAirControlToWalkTarget(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, boolean var3, BlockPos var4, double var5, BlockPos var7
   ) {
      return solveAirControlForTarget(var0, var1, var2, var3, var4, var5, var7 == null ? null : AiJumpSolver.LandingTarget.walk(var7, var5));
   }

   public static AiJumpSolver.ApproachOffset solveAirControlForTarget(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, boolean var3, BlockPos var4, double var5, AiJumpSolver.LandingTarget var7
   ) {
      return solveAirControlForTarget(var0, var1, var2, var3, var4, var5, var7, false);
   }

   static AiJumpSolver.ApproachOffset solveAirControlForTarget(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      float var2,
      boolean var3,
      BlockPos var4,
      double var5,
      AiJumpSolver.LandingTarget var7,
      boolean var8
   ) {
      AiJumpSolver.ApproachOffset var9 = null;
      double var10 = -1.0 / 0.0;

      for (double var15 : new double[]{1.0, 0.0, -1.0}) {
         if (var15 < 0.0 && var9 != null && var9.margin() >= -0.12) {
            break;
         }

         for (double var20 : new double[]{0.0, 1.0, -1.0}) {
            AiPhysics.MoveInput var22 = new AiPhysics.MoveInput(var15, var20, false, var3 && var15 > 0.0, false, var2);
            AiPhysics.MotionState var23 = var1;
            double var24 = -1.0;
            boolean var26 = false;
            boolean var27 = false;

            for (int var28 = 1; var28 <= 100; var28++) {
               AiPhysics.MotionState var29 = AiPhysics.step(var23, var22, var0);
               if (AiPhysics.hitsStrictHazard(var0, var23, var29)) {
                  var26 = true;
                  break;
               }

               var23 = var29;
               var27 |= var23.horizontalCollision() && !var23.onGround();
               if (isInFluid(var0, var23)) {
                  break;
               }

               if (isGroundedNotRising(var23)) {
                  var24 = var23.y() <= var5 + 0.02 && var23.y() >= var5 - 0.55 ? settledLandingMargin(var0, var23, var4, var5, var2) : -1.0;
                  if (var8 && var24 < 0.065) {
                     var24 = measureCoastingSupportMargin(var0, var23, var4, var5);
                  }
                  break;
               }

               if (!var23.onGround() && var23.vy() < 0.0 && var23.y() < var5 - 4.0) {
                  break;
               }
            }

            if (!var26) {
               boolean var36 = isLandedOnCell(var23.x(), var23.y(), var23.z(), var4, var5) || var8 && isInsideLandingCell(var23, var4, var5);
               boolean var37 = var7 != null && var24 >= -0.12 && (var7.nextJump() == null || var15 > 0.0 && var20 == 0.0) && var7.proves(var0, var23);
               if (var37) {
                  var24 = Math.max(var24, 0.14);
               }

               double var30 = !(var24 >= -0.12) || !var36 && !var37 ? 0.0 : 2.0;
               if (var15 > 0.0 && var20 == 0.0 && var24 >= 0.08 && (var36 || var37) && !var27) {
                  return new AiJumpSolver.ApproachOffset(var15, var20, var24);
               }

               boolean var32 = var24 >= -0.12;
               double var33 = var30 + var24 - (var20 == 0.0 ? 0.0 : 0.02) - (var15 > 0.0 ? 0.0 : 0.01) - (var27 && var32 ? 2.5 : 0.0);
               boolean var35 = var9 != null && var9.margin() >= -0.12;
               if (var32 != var35 ? var32 : var33 > var10) {
                  var10 = var33;
                  var9 = new AiJumpSolver.ApproachOffset(var15, var20, var24);
               }
            }
         }
      }

      return var9 == null ? new AiJumpSolver.ApproachOffset(0.0, 0.0, -1.0) : var9;
   }

   public static AiJumpSolver.ApproachOffset solveAirControlWithCoast(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, boolean var3, BlockPos var4, double var5, int var7
   ) {
      if (var7 == 2147483647) {
         return null;
      }

      AiPhysics.MotionState var8 = var1;

      for (int var9 = 1; var9 <= 100; var9++) {
         AiPhysics.MoveInput var10 = new AiPhysics.MoveInput(var9 <= var7 ? 1.0 : 0.0, 0.0, false, var3 && var9 <= var7, false, var2);
         AiPhysics.MotionState var11 = AiPhysics.step(var8, var10, var0);
         if (AiPhysics.hitsStrictHazard(var0, var8, var11) || isInFluid(var0, var11)) {
            return null;
         }

         var8 = var11;
         if (isGroundedNotRising(var8)) {
            double var12 = var8.y() <= var5 + 0.02 && var8.y() >= var5 - 0.55 ? settledLandingMargin(var0, var8, var4, var5, var2) : -1.0;
            return var12 >= -0.12 ? new AiJumpSolver.ApproachOffset(var7 > 0 ? 1.0 : 0.0, 0.0, var12) : null;
         }

         if (var8.y() < var5 - 4.0) {
            return null;
         }
      }

      return null;
   }

   public static double maxJumpReach(double var0, boolean var2, boolean var3) {
      return maxJumpReach(var0, var2, var3, 0.1);
   }

   static double maxJumpReach(double var0, boolean var2, boolean var3, double var4) {
      int var6 = (int)Math.round(var0 * 4.0) * 8 + (var2 ? 4 : 0) + (var3 ? 2 : 0) + 1;
      if (Math.abs(var4 - 0.1) > 1.0E-6) {
         if (SPEED_REACH_CACHE.size() > 4096) {
            SPEED_REACH_CACHE.clear();
         }

         double var11 = Math.ceil(var4 * 10000.0 - 1.0E-6) / 10000.0;
         int var9 = (int)Math.floor(var0 * 16.0);
         AiJumpSolver.ArcSpeed var10 = new AiJumpSolver.ArcSpeed(var9 * 4 + (var2 ? 2 : 0) + (var3 ? 1 : 0), var11);
         return SPEED_REACH_CACHE.computeIfAbsent(var10, var5 -> simulateMaxReach(var9 / 16.0, var2, var3, true, var11));
      } else {
         Double var7 = DEFAULT_REACH_CACHE.get(var6);
         if (var7 != null) {
            return var7;
         }

         double var8 = simulateMaxReach(var0, var2, var3, true, 0.1);
         DEFAULT_REACH_CACHE.put(var6, var8);
         return var8;
      }
   }

   public static double standingJumpReach(double var0) {
      int var2 = (int)Math.round(var0 * 4.0) * 8;
      Double var3 = DEFAULT_REACH_CACHE.get(var2);
      if (var3 != null) {
         return var3;
      }

      double var4 = simulateMaxReach(var0, false, false, false, 0.1);
      DEFAULT_REACH_CACHE.put(var2, var4);
      return var4;
   }

   private static double simulateMaxReach(double var0, boolean var2, boolean var3, boolean var4, double var5) {
      AiJumpSolver.FlatBlockWorld var7 = new AiJumpSolver.FlatBlockWorld(var5);
      double var8 = var3 ? 0.3 : 0.5;
      double var10 = var3 ? 0.28 * (var5 / 0.1) : 0.0;
      AiPhysics.MotionState var12 = new AiPhysics.MotionState(var8, 64.0, 0.5, var10, -0.0784, 0.0, true, 0, false, false);
      AiPhysics.MoveInput var13 = AiPhysics.MoveInput.run(-90.0F, var2);
      var var14 = simulateRunupStates(var7, var12, var13, 12);
      AiPhysics.MotionState var15 = var4 ? (AiPhysics.MotionState)var14.get(var14.size() - 1) : (AiPhysics.MotionState)var14.get(0);
      AiPhysics.MotionState var16 = AiPhysics.step(var15, var13.withJump(true), var7);
      AiPhysics.MoveInput var17 = var4 ? var13 : new AiPhysics.MoveInput(0.0, 0.0, false, false, false, -90.0F);
      double var18 = 64.0 + var0;
      double var20 = -1.0;

      for (int var22 = 1; var22 <= 100; var22++) {
         AiPhysics.MotionState var23 = AiPhysics.step(var16, var17, var7);
         if (var23.vy() < 0.0 && var16.y() >= var18 && var23.y() < var18) {
            var20 = var23.x() - var8;
            break;
         }

         if (var23.y() < var18 - 2.0 && var23.vy() < 0.0) {
            break;
         }

         var16 = var23;
      }

      return var20;
   }

   record Point2D(double x, double z) {
   }

   public record LandingTarget(BlockPos cell, double surface, AiJumpSolver.NextJump nextJump) {
      static AiJumpSolver.LandingTarget walk(BlockPos var0, double var1) {
         return new AiJumpSolver.LandingTarget(var0, var1, null);
      }

      static AiJumpSolver.LandingTarget jump(BlockPos var0, double var1, BlockPos var3, double var4, float var6, boolean var7) {
         return new AiJumpSolver.LandingTarget(var3, var4, new AiJumpSolver.NextJump(var0, var1, var6, var7));
      }

      boolean proves(AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2) {
         if (!AiJumpSolver.isGroundedNotRising(var2)) {
            return false;
         } else if (this.nextJump != null) {
            return !atCell(var2, this.nextJump.from(), this.nextJump.fromSurface())
               ? false
               : AiJumpSolver.solveTakeoff(var1, var2, this.nextJump.yaw(), this.nextJump.sprint(), this.cell, this.surface, 6).margin()
                  >= -0.04;
         } else {
            return atCell(var2, this.cell, this.surface) && AiJumpSolver.isFullFlatSupport(var1, this.cell.getX(), this.cell.getZ(), this.surface);
         }
      }

      private static boolean atCell(AiPhysics.MotionState var0, BlockPos var1, double var2) {
         return var0.y() <= var2 + 0.02 && var0.y() >= var2 - 0.55 && (int)Math.floor(var0.x()) == var1.getX() && (int)Math.floor(var0.z()) == var1.getZ();
      }
   }

   public record ApproachOffset(double forward, double strafe, double margin) {
   }

   private record ArcSpeed(int arc, double speed) {
   }

   record SurfaceCell(BlockPos cell, double surface) {
   }

   private record ChainNode(int cost, float yaw, AiPhysics.MotionState state, List<AiJumpSolver.JumpPlan> flights) {
   }

   public record UiHelper(int runTicks, double strafe, int coastAfter, double margin, boolean sprint, int flightTicks) {
      public UiHelper(int var1, double var2, int var4, double var5, boolean var7) {
         this(var1, var2, var4, var5, var7, 0);
      }
   }

   private record ArcResult(
      AiPhysics.MotionState end, int ticks, boolean settled, int coastAfter, double along, double margin, boolean bumped, boolean wall
   ) {
   }

   record TakeoffCandidate(float yaw, AiJumpSolver.UiHelper takeoff) {
   }

   public record NextJump(BlockPos from, double fromSurface, float yaw, boolean sprint) {
   }

   private record FlatBlockWorld(double movementSpeed) implements AiPhysics.PhysicsWorld {
      private static final AABB BLOCK = new AABB(-32.0, 63.0, -32.0, 1.0, 64.0, 32.0);

      @Override
      public List<AABB> collisions(AABB var1) {
         return BLOCK.intersects(var1) ? List.of(BLOCK) : List.of();
      }
   }

   private record JumpAxis(double ox, double oz, double ux, double uz, double target) {
      double along(AiPhysics.MotionState var1) {
         return (var1.x() - this.ox) * this.ux + (var1.z() - this.oz) * this.uz;
      }
   }

   record ExitOption(float yaw, double strafe, double margin, int nextRunTicks, double exitSpeed) {
   }

   private record RunArc(int runTicks, AiJumpSolver.ArcResult arc) {
   }

   public record JumpPlan(
      float yaw,
      int runTicks,
      boolean sprint,
      int coastAfter,
      int flightTicks,
      double startX,
      double startZ,
      double landX,
      double landY,
      double landZ,
      double landSpeed,
      double margin,
      double backup,
      double approach,
      AiJumpManeuver maneuver
   ) {
      public JumpPlan(
         float var1,
         int var2,
         boolean var3,
         int var4,
         int var5,
         double var6,
         double var8,
         double var10,
         double var12,
         double var14,
         double var16,
         double var18,
         double var20,
         double var22
      ) {
         this(var1, var2, var3, var4, var5, var6, var8, var10, var12, var14, var16, var18, var20, var22, null);
      }

      public JumpPlan(
         float var1, int var2, boolean var3, int var4, int var5, double var6, double var8, double var10, double var12, double var14, double var16, double var18
      ) {
         this(var1, var2, var3, var4, var5, var6, var8, var10, var12, var14, var16, var18, 0.0, 0.0);
      }

      public boolean running() {
         return this.approach > 0.0;
      }

      public int ticks() {
         return this.backupTicks() + (this.maneuver == null ? this.runTicks + this.flightTicks + 1 : this.maneuver.inputs().size());
      }

      public int backupTicks() {
         return this.backup > 0.0 ? (int)Math.ceil(this.backup / 0.1) + 4 : 0;
      }

      public AiJumpSolver.JumpPlan shifted(int var1, int var2, int var3) {
         return new AiJumpSolver.JumpPlan(
            this.yaw,
            this.runTicks,
            this.sprint,
            this.coastAfter,
            this.flightTicks,
            this.startX + var1,
            this.startZ + var3,
            this.landX + var1,
            this.landY + var2,
            this.landZ + var3,
            this.landSpeed,
            this.margin,
            this.backup,
            this.approach,
            this.maneuver == null ? null : this.maneuver.shifted(var1, var2, var3)
         );
      }
   }

   private record JumpSearchContext(
      AiPhysics.PhysicsWorld world,
      List<AiPhysics.MotionState> runs,
      AiPhysics.MoveInput press,
      AiPhysics.MoveInput air,
      AiJumpSolver.JumpAxis line,
      BlockPos to,
      double surface,
      AiJumpSolver.ArcResult[] held
   ) {
      JumpSearchContext(
         AiPhysics.PhysicsWorld var1,
         List<AiPhysics.MotionState> var2,
         AiPhysics.MoveInput var3,
         AiPhysics.MoveInput var4,
         AiJumpSolver.JumpAxis var5,
         BlockPos var6,
         double var7
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, new AiJumpSolver.ArcResult[var2.size()]);
      }

      AiJumpSolver.ArcResult fly(int var1, int var2) {
         if (var2 != 2147483647) {
            return AiJumpSolver.simulateJumpArc(
               this.world, this.runs.get(var1), this.press, this.air, var2, this.line, this.to, this.surface
            );
         }

         AiJumpSolver.ArcResult var3 = this.held[var1];
         return var3 != null
            ? var3
            : (
               this.held[var1] = AiJumpSolver.simulateJumpArc(
                  this.world, this.runs.get(var1), this.press, this.air, 2147483647, this.line, this.to, this.surface
               )
            );
      }

      AiJumpSolver.ArcResult releaseEarlier(int var1, AiJumpSolver.ArcResult var2) {
         return AiJumpSolver.optimizeCoastRelease(
            this.world, this.runs.get(var1), this.press, this.air, var2, this.line, this.to, this.surface
         );
      }
   }

   private record TurnSchedule(AiPhysics.MoveInput entry, int turnTick, AiPhysics.MoveInput exit) {
      AiPhysics.MoveInput at(int var1) {
         return var1 < this.turnTick ? this.entry : this.exit;
      }
   }

   // 5.3 external ABI bridges for ai/b/TokenStore.java
   public static AiJumpSolver.UiHelper solveTakeoffCompat(
      AiPhysics.PhysicsWorld world, AiPhysics.MotionState state, float yaw, boolean sprint, BlockPos to, double toSurface
   ) {
      return solveTakeoff(world, state, yaw, sprint, to, toSurface);
   }

   public static AiJumpSolver.UiHelper solveTakeoffCompat(
      AiPhysics.PhysicsWorld world, AiPhysics.MotionState state, float yaw, boolean sprint, BlockPos to, double toSurface, int maxRunTicks
   ) {
      return solveTakeoff(world, state, yaw, sprint, to, toSurface, maxRunTicks);
   }
}
