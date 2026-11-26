package autismclient.ai.engine;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public record AiJumpManeuver(
   List<AiPhysics.MoveInput> inputs, AiPhysics.MotionState start, AiPhysics.MotionState landing, double margin, int takeoffTick
) {
   public static final double BACKUP = 4.0;
   private static final int RUN_TICKS = 48;
   private static final int[] RELEASES = new int[]{2147483647, 0, 3, 6, 9, 12};
   private static final int[] COMMON_RELEASES = new int[]{2147483647, 3, 6, 9};
   static final int[] RECOVERY_RELEASES = new int[]{2147483647, 0, 1, 3, 5, 6, 7, 9, 12};
   private static final int[] STRAFES = new int[]{0, 1, -1};
   private static final float[] AIR_OFFSETS = new float[]{0.0F, -20.0F, 20.0F, -40.0F, 40.0F, -60.0F, 60.0F};
   private static final float[] COMMON_AIR_OFFSETS = new float[]{0.0F, -20.0F, 20.0F, -40.0F, 40.0F};
   private static final float[] LAUNCH_TURNS = new float[]{1.0F, 0.0F, 0.5F};
   private static final float[] DROP_LAUNCH_TURNS = new float[]{1.0F, 0.0F};
   private static final float[] NARROW_HEADINGS = new float[]{
      -16.0F, 16.0F, -20.0F, 20.0F, -24.0F, 24.0F, -28.0F, 28.0F, -33.0F, 33.0F, -8.0F, 8.0F, -4.0F, 4.0F, 0.0F
   };
   private static final float[] COMMON_NARROW_HEADINGS = new float[]{-16.0F, 16.0F, -24.0F, 24.0F, -33.0F, 33.0F, -8.0F, 8.0F, 0.0F};
   private static final int[] NARROW_DELAYS = new int[]{2, 4, 0, 6, 8};
   private static final int[] COMMON_NARROW_DELAYS = new int[]{2, 4, 0};
   private static final ThreadLocal<AiJumpManeuver.Inner_CachePool> FLIGHT_WORKSPACE = ThreadLocal.withInitial(AiJumpManeuver.Inner_CachePool::new);

   public AiJumpManeuver(
      List<AiPhysics.MoveInput> inputs, AiPhysics.MotionState start, AiPhysics.MotionState landing, double margin, int takeoffTick
   ) {
      inputs = List.copyOf(inputs);
      this.inputs = inputs;
      this.start = start;
      this.landing = landing;
      this.margin = margin;
      this.takeoffTick = takeoffTick;
   }

   static AiPhysics.MoveInput settleInput(AiPhysics.MotionState var0, float var1) {
      return settleInput(null, var0, var1);
   }

   static AiPhysics.MoveInput settleInput(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2) {
      double var3 = var1.speed();
      double var5 = 0.0;
      double var7 = 0.0;
      boolean var9 = var0 != null
         && var1.onGround()
         && var0.putAt_D_I((int)Math.floor(var1.x()), (int)Math.floor(var1.y() - 0.5), (int)Math.floor(var1.z())) >= 0.9;
      if (var3 >= (var9 ? 0.025 : 0.1)) {
         double var10 = Math.toRadians(var2);
         double var12 = (-var1.vx() * Math.sin(var10) + var1.vz() * Math.cos(var10)) / var3;
         double var14 = (var1.vx() * Math.cos(var10) + var1.vz() * Math.sin(var10)) / var3;
         var5 = Math.abs(var12) > 0.38 ? -Math.signum(var12) : 0.0;
         var7 = Math.abs(var14) > 0.38 ? -Math.signum(var14) : 0.0;
      }

      boolean var18 = true;
      if (var0 != null && var1.onGround()) {
         AiPhysics.MoveInput var11 = new AiPhysics.MoveInput(var5, var7, false, false, false, var2);
         AiPhysics.MotionState var19 = var1;
         boolean var13 = true;

         for (int var20 = 0; var20 < (var9 ? 22 : 8); var20++) {
            AiPhysics.MoveInput var15 = var19.speed() > (var9 ? 0.024 : 0.03)
               ? var11
               : new AiPhysics.MoveInput(0.0, 0.0, false, false, false, var2);
            AiPhysics.MotionState var16 = AiPhysics.step(var19, var15, var0);
            if (AiPhysics.hitsStrictHazard(var0, var19, var16) || !var16.onGround() || !AiGroundContinuity.putAt(var16.y(), var1.y())) {
               var13 = false;
               break;
            }

            var19 = var16;
            if (var19.speed() <= 0.02) {
               AiPhysics.MotionState var17 = AiPhysics.step(var19, AiPhysics.MoveInput.NONE, var0);
               var13 = var17.onGround() && AiGroundContinuity.putAt(var17.y(), var1.y());
               break;
            }
         }

         if (var13) {
            var18 = false;
         }
      }

      return new AiPhysics.MoveInput(var5, var7, false, false, var18, var2);
   }

   static List<AiPhysics.MoveInput> crossRunwayObstacle(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2
   ) {
      if (var1.onGround() && !(Math.abs(var1.y() - var2.y()) > 0.02)) {
         double var3 = var2.x() - var1.x();
         double var5 = var2.z() - var1.z();
         double var7 = Math.hypot(var3, var5);
         if (!(var7 < 0.25) && !(var7 > 1.35)) {
            ArrayList var9 = new ArrayList();
            if (walkToRunway(var0, var1, var2, var9) != null) {
               return List.copyOf(var9);
            }

            List var10 = hopOntoRunway(var0, var1, var2);
            if (!var10.isEmpty() && var10.size() <= 12) {
               return var10;
            }

            for (double var14 : new double[]{0.75, -0.75, 1.0, -1.0, 1.3, -1.3}) {
               double var16 = Math.abs(var3) < Math.abs(var5) ? var14 : 0.0;
               double var18 = Math.abs(var3) < Math.abs(var5) ? 0.0 : var14;
               AiPhysics.MotionState var20 = AiPhysics.MotionState.standing(var1.x() + var16, var1.y(), var1.z() + var18);
               AiPhysics.MotionState var21 = AiPhysics.MotionState.standing(var2.x() + var16, var2.y(), var2.z() + var18);
               if (AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var20.x(), var20.y(), var20.z())
                  && AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var21.x(), var21.y(), var21.z())) {
                  ArrayList var22 = new ArrayList();
                  AiPhysics.MotionState var23 = walkToRunway(var0, var1, var20, var22);
                  if (var23 != null && (var10.isEmpty() || var22.size() + 5 < var10.size())) {
                     var var24 = hopOntoRunway(var0, var23, var2);
                     if (!var24.isEmpty() && (var10.isEmpty() || var22.size() + var24.size() < var10.size())) {
                        ArrayList var25 = new ArrayList(var22);
                        var25.addAll(var24);
                        var10 = List.copyOf(var25);
                     }

                     var var29 = hopOntoRunway(var0, var23, var21);
                     if (!var29.isEmpty()) {
                        AiPhysics.MotionState var26 = var23;

                        for (AiPhysics.MoveInput var28 : var29) {
                           var26 = AiPhysics.step(var26, var28, var0);
                        }

                        var22.addAll(var29);
                        if (walkToRunway(var0, var26, var2, var22) != null && (var10.isEmpty() || var22.size() < var10.size())) {
                           var10 = List.copyOf(var22);
                        }
                     }
                  }
               }
            }

            return var10;
         } else {
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private static AiPhysics.MotionState walkToRunway(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2, List<AiPhysics.MoveInput> var3
   ) {
      float var4 = AiPhysics.yawBetween(var1.x(), var1.z(), var2.x(), var2.z());
      double var5 = Math.toRadians(var4);
      double var7 = -Math.sin(var5);
      double var9 = Math.cos(var5);

      for (int var11 = 0; var11 < 24; var11++) {
         if (onRunway(var0, var1, var2)) {
            return var1;
         }

         double var12 = (var2.x() - var1.x()) * var7 + (var2.z() - var1.z()) * var9;
         double var14 = (var1.vx() * var7 + var1.vz() * var9) * 1.4;
         double var16 = Math.abs(var12 - var14) <= 0.07 ? 0.0 : Math.signum(var12 - var14);
         AiPhysics.MoveInput var18 = new AiPhysics.MoveInput(var16, 0.0, false, false, false, var4);
         AiPhysics.MotionState var19 = AiPhysics.step(var1, var18, var0);
         if (var19.equals(var1) || !AiGroundContinuity.putAt(var19.y(), var2.y()) || !var19.onGround() || AiPhysics.hitsStrictHazard(var0, var1, var19)) {
            return null;
         }

         var3.add(var18);
         var1 = var19;
      }

      return null;
   }

   private static List<AiPhysics.MoveInput> hopOntoRunway(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2
   ) {
      float var3 = AiPhysics.yawBetween(var1.x(), var1.z(), var2.x(), var2.z());
      List var4 = List.of();

      for (float var8 : new float[]{var3, Math.round(var3 / 90.0F) * 90.0F}) {
         AiPhysics.MoveInput var9 = AiPhysics.MoveInput.NONE.withYaw(var8);
         AiPhysics.MoveInput var10 = AiPhysics.MoveInput.run(var8, false);

         for (int var11 = 2; var11 <= 5; var11++) {
            AiPhysics.MotionState var12 = var1;

            for (int var13 = 0; var13 < var11; var13++) {
               var12 = AiPhysics.step(var12, var9.withJump(var13 == 0), var0);
            }

            if (!AiPhysics.hitsStrictHazard(var0, var1, var12) && !var12.onGround()) {
               AiPhysics.MotionState var22 = var12;

               for (int var14 = 1; var14 <= 10; var14++) {
                  AiPhysics.MotionState var15 = AiPhysics.step(var22, var10, var0);
                  if (AiPhysics.hitsStrictHazard(var0, var22, var15) || var15.y() < var2.y() - 0.04) {
                     break;
                  }

                  var22 = var15;
                  if (!(Math.hypot(var22.x() - var2.x(), var22.z() - var2.z()) > 0.34)) {
                     for (int var16 = 0; var16 <= 5 && (var4.isEmpty() || var11 + var14 + 1 < var4.size()); var16++) {
                        ArrayList var17 = new ArrayList(var11 + var14 + 12);

                        for (int var18 = 0; var18 < var11; var18++) {
                           var17.add(var9.withJump(var18 == 0));
                        }

                        for (int var23 = 0; var23 < var14; var23++) {
                           var17.add(var10);
                        }

                        AiPhysics.MotionState var24 = var22;

                        for (int var19 = 0; var19 < 12; var19++) {
                           AiPhysics.MoveInput var20 = var19 < var16 ? new AiPhysics.MoveInput(-1.0, 0.0, false, false, false, var8) : var9;
                           AiPhysics.MotionState var21 = AiPhysics.step(var24, var20, var0);
                           if (var21.y() < var2.y() - 0.04 || AiPhysics.hitsStrictHazard(var0, var24, var21)) {
                              break;
                           }

                           var17.add(var20);
                           var24 = var21;
                           if (onRunway(var0, var24, var2)) {
                              if (var4.isEmpty() || var17.size() < var4.size()) {
                                 var4 = List.copyOf(var17);
                              }
                              break;
                           }

                           if (!var4.isEmpty() && var17.size() >= var4.size()) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return var4;
   }

   static boolean runwayCrossingStillSafe(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, List<AiPhysics.MoveInput> var2, int var3, AiPhysics.MotionState var4
   ) {
      for (int var5 = var3; var5 < var2.size(); var5++) {
         AiPhysics.MotionState var6 = AiPhysics.step(var1, (AiPhysics.MoveInput)var2.get(var5), var0);
         if (var6.y() < var4.y() - 0.04 || AiPhysics.hitsStrictHazard(var0, var1, var6)) {
            return false;
         }

         var1 = var6;
      }

      return onRunway(var0, var1, var4);
   }

   static List<AiPhysics.MoveInput> finishRunwayCrossing(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2
   ) {
      if (!var1.onGround() && !(var1.y() < var2.y() - 0.02)) {
         float var3 = AiPhysics.yawBetween(var1.x(), var1.z(), var2.x(), var2.z());

         for (int var7 : new int[]{0, 1, -1}) {
            AiPhysics.MoveInput var8 = new AiPhysics.MoveInput(var7, 0.0, false, false, false, var3);
            AiPhysics.MotionState var9 = var1;
            ArrayList<AiPhysics.MoveInput> var10 = new ArrayList<>();

            for (int var11 = 0; var11 < 10; var11++) {
               AiPhysics.MotionState var12 = AiPhysics.step(var9, var8, var0);
               if (var12.y() < var2.y() - 0.02 || AiPhysics.hitsStrictHazard(var0, var9, var12)) {
                  break;
               }

               var9 = var12;
               var10.add(var8);
               if (AiJumpSolver.putAt(var9)) {
                  if (Math.abs(var9.y() - var2.y()) <= 0.02
                     && Math.hypot(var9.x() - var2.x(), var9.z() - var2.z()) < 0.7
                     && walkToRunway(var0, var9, var2, var10) != null) {
                     return List.copyOf(var10);
                  }
                  break;
               }
            }
         }

         return List.of();
      } else {
         return List.of();
      }
   }

   private static boolean onRunway(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2) {
      if (var1.onGround()
         && !(Math.abs(var1.y() - var2.y()) > 0.02)
         && !(var1.speed() > 0.075)
         && !(Math.hypot(var1.x() - var2.x(), var1.z() - var2.z()) > 0.22)) {
         for (int var3 = 0; var3 < 2; var3++) {
            var1 = AiPhysics.step(var1, AiPhysics.MoveInput.NONE, var0);
            if (!var1.onGround() || Math.abs(var1.y() - var2.y()) > 0.02) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public AiJumpManeuver shifted(int var1, int var2, int var3) {
      return new AiJumpManeuver(this.inputs, shifted(this.start, var1, var2, var3), shifted(this.landing, var1, var2, var3), this.margin, this.takeoffTick);
   }

   private static AiPhysics.MotionState shifted(AiPhysics.MotionState var0, int var1, int var2, int var3) {
      return new AiPhysics.MotionState(
         var0.x() + var1,
         var0.y() + var2,
         var0.z() + var3,
         var0.vx(),
         var0.vy(),
         var0.vz(),
         var0.onGround(),
         var0.jumpDelay(),
         var0.horizontalCollision(),
         var0.verticalCollision(),
         var0.support() == -9223372036854775808L ? -9223372036854775808L : BlockPos.of(var0.support()).offset(var1, var2, var3).asLong(),
         var0.sprintCut(),
         var0.pose(),
         var0.stuck()
      );
   }

   public double replay(AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, int var3, BlockPos var4, double var5) {
      return this.replay(var1, var2, var3, var4, var5, -1, null);
   }

   boolean provesStandingRelease(AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, int var3, BlockPos var4, double var5) {
      if (var2.pose() == 3 && !this.inputs.get(var3).sneak() && AiJumpSolver.putAt_Z_TokenCore_TaskStat(var1, var2.x(), var2.y(), var2.z())) {
         AiPhysics.MotionState var7 = new AiPhysics.MotionState(
            var2.x(),
            var2.y(),
            var2.z(),
            var2.vx(),
            var2.vy(),
            var2.vz(),
            var2.onGround(),
            var2.jumpDelay(),
            var2.horizontalCollision(),
            var2.verticalCollision(),
            var2.support(),
            var2.sprintCut(),
            0,
            var2.stuck()
         );
         return this.replay(var1, var7, var3, var4, var5) >= -0.12;
      } else {
         return true;
      }
   }

   private double replay(
      AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, int var3, BlockPos var4, double var5, int var7, AiPhysics.MoveInput var8
   ) {
      if (AiJumpSolver.putAt(var1, var2)) {
         return -1.0;
      }

      double var9 = var2.y();

      for (int var11 = var3; var11 < this.inputs.size(); var11++) {
         AiPhysics.MotionState var12 = AiPhysics.step(var2, var11 == var7 ? var8 : this.inputs.get(var11), var1);
         if (AiPhysics.hitsStrictHazard(var1, var2, var12) || AiJumpSolver.putAt(var1, var12) || var12.y() < var5 - 4.0) {
            return -1.0;
         }

         var9 = Math.max(var9, var12.y());
         if (AiJumpSolver.putAt(var12)) {
            if (var9 - var12.y() > var1.putAt_D() + 1.0E-5) {
               return -1.0;
            }

            var9 = var12.y();
         }

         var2 = var12;
      }

      return AiJumpSolver.putAt(var2) && !(Math.abs(var2.y() - var5) > 0.02)
         ? AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var1, var2, var4, var5, this.inputs.get(this.inputs.size() - 1).yaw())
         : -1.0;
   }

   AiJumpManeuver fitFlight(AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, int var3, BlockPos var4, double var5) {
      int var7 = Math.min(var3 + 4, this.inputs.size() - 1);
      double var8 = -0.04;
      AiPhysics.MoveInput var10 = null;
      int var11 = -1;

      for (int var12 = var3; var12 < var7; var12++) {
         AiPhysics.MoveInput var13 = this.inputs.get(var12);
         if (!var13.jump() && !var13.sneak()) {
            for (int var14 = -1; var14 <= 1; var14++) {
               for (int var15 = -1; var15 <= 1; var15++) {
                  if (var14 != var13.forward() || var15 != var13.strafe()) {
                     AiPhysics.MoveInput var16 = new AiPhysics.MoveInput(var14, var15, false, var14 > 0 && var13.sprint(), false, var13.yaw());
                     double var17 = this.replay(var1, var2, var3, var4, var5, var12, var16);
                     if (!(var17 < var8)) {
                        var8 = var17;
                        var11 = var12;
                        var10 = var16;
                     }
                  }
               }
            }
         }
      }

      if (var10 == null) {
         return null;
      }

      var var19 = new ArrayList<>(this.inputs.subList(var3, this.inputs.size()));
      var19.set(var11 - var3, var10);
      AiPhysics.MotionState var20 = var2;

      for (AiPhysics.MoveInput var22 : var19) {
         var20 = AiPhysics.step(var20, var22, var1);
      }

      return new AiJumpManeuver(var19, var2, var20, var8, 0);
   }

   static boolean coastsIntoLanding(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, double var3, float var5) {
      for (int var6 = 0; var6 < 4 && var1.speed() > 0.01; var6++) {
         AiPhysics.MotionState var7 = AiPhysics.step(var1, AiPhysics.MoveInput.NONE, var0);
         if (!AiJumpSolver.putAt(var7) || Math.abs(var7.y() - var3) > 0.02 || AiPhysics.hitsStrictHazard(var0, var1, var7)) {
            return false;
         }

         if ((int)Math.floor(var7.x()) == var2.getX()
            && (int)Math.floor(var7.z()) == var2.getZ()
            && AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var0, var7, var2, var3, var5) >= -0.12) {
            return true;
         }

         var1 = var7;
      }

      return false;
   }

   AiJumpManeuver joinRunUp(AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, BlockPos var3, double var4) {
      if (var2.onGround() && var2.pose() == 0 && !(Math.abs(var2.y() - this.start.y()) > 0.02) && !(var2.speed() < 0.075)) {
         ArrayList<AiPhysics.MotionState> var6 = new ArrayList<>();
         var6.add(this.start);

         for (int var7 = 0; var7 < Math.min(this.takeoffTick, 5); var7++) {
            var6.add(AiPhysics.step((AiPhysics.MotionState)var6.getLast(), this.inputs.get(var7), var1));
         }

         List var26 = List.of(new AiJumpManeuver.Inner_TaskQueue(var2, List.of(), 0.0));
         AiJumpManeuver var8 = null;

         for (int var9 = 0; var9 <= 2; var9++) {
            ArrayList<AiJumpManeuver.Inner_TaskQueue> var10 = new ArrayList<>();

            for (AiJumpManeuver.Inner_TaskQueue var12 : (Iterable<AiJumpManeuver.Inner_TaskQueue>)(Iterable<?>)(var26)) {
               for (int var13 = var6.size() - 1; var13 >= 0; var13--) {
                  if (!(nearPhase(var12.state(), (AiPhysics.MotionState)var6.get(var13)) > 0.48)) {
                     int var14 = var12.prefix().size() + this.inputs.size() - var13;
                     if (var8 == null || var14 < var8.inputs().size()) {
                        double var15 = this.replay(var1, var12.state(), var13, var3, var4);
                        if (!(var15 < -0.04)) {
                           var var17 = new ArrayList<>(var12.prefix());
                           var17.addAll(this.inputs.subList(var13, this.inputs.size()));
                           var8 = new AiJumpManeuver(var17, var2, this.landing, var15, var9 + this.takeoffTick - var13);
                        }
                     }
                  }
               }

               if (var9 != 2) {
                  float var27 = this.inputs.getFirst().yaw();

                  for (float var30 : new float[]{0.0F, -25.0F, 25.0F, -50.0F, 50.0F}) {
                     for (int var18 = -1; var18 <= 1; var18++) {
                        AiPhysics.MoveInput var19 = new AiPhysics.MoveInput(1.0, var18, false, true, false, var27 + var30);
                        AiPhysics.MotionState var20 = AiPhysics.step(var12.state(), var19, var1);
                        if (var20.onGround()
                           && AiGroundContinuity.putAt(var20.y(), var2.y())
                           && !var20.horizontalCollision()
                           && !AiPhysics.hitsStrictHazard(var1, var12.state(), var20)) {
                           var var21 = new ArrayList<>(var12.prefix());
                           var21.add(var19);
                           double var22 = 1.0 / 0.0;

                           for (AiPhysics.MotionState var25 : var6) {
                              var22 = Math.min(var22, nearPhase(var20, var25));
                           }

                           var10.add(new AiJumpManeuver.Inner_TaskQueue(var20, var21, var22));
                        }
                     }
                  }
               }
            }

            var10.sort(Comparator.comparingDouble(AiJumpManeuver.Inner_TaskQueue::distance));
            var26 = var10.subList(0, Math.min(6, var10.size()));
         }

         return var8;
      } else {
         return null;
      }
   }

   private static double nearPhase(AiPhysics.MotionState var0, AiPhysics.MotionState var1) {
      return Math.hypot(var0.x() - var1.x(), var0.z() - var1.z()) + 0.8 * Math.hypot(var0.vx() - var1.vx(), var0.vz() - var1.vz());
   }

   AiJumpManeuver fitApproach(AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, BlockPos var3, double var4, float var6) {
      if (var2.onGround() && !(Math.abs(var2.y() - this.start.y()) > 0.02) && !(Math.hypot(var2.x() - this.start.x(), var2.z() - this.start.z()) > 0.35)) {
         List var7 = List.of(new AiJumpManeuver.Inner_TaskQueue(var2, List.of(), 0.0));
         float[] var8 = new float[]{0.0F, -3.0F, 3.0F, -8.0F, 8.0F, -16.0F, 16.0F};

         for (int var9 = 0; var9 < 5; var9++) {
            ArrayList<AiJumpManeuver.Inner_TaskQueue> var10 = new ArrayList<>();

            for (AiJumpManeuver.Inner_TaskQueue var12 : (Iterable<AiJumpManeuver.Inner_TaskQueue>)(Iterable<?>)(var7)) {
               for (boolean var16 : new boolean[]{false, true}) {
                  for (int var17 = -1; var17 <= 1; var17++) {
                     for (int var18 = -1; var18 <= 1; var18++) {
                        for (float var22 : var8) {
                           if (var17 != 0 || var18 != 0 || var22 == 0.0F) {
                              AiPhysics.MoveInput var23 = new AiPhysics.MoveInput(var17, var18, false, false, var16, var6 + var22);
                              AiPhysics.MotionState var24 = AiPhysics.step(var12.state(), var23, var1);
                              if (var24.onGround()
                                 && !(Math.abs(var24.y() - var2.y()) > 0.02)
                                 && !AiPhysics.hitsStrictHazard(var1, var12.state(), var24)
                                 && (this.start.pose() != 0 || AiJumpSolver.putAt_Z_TokenCore_TaskStat(var1, var24.x(), var24.y(), var24.z()))) {
                                 double var25 = Math.hypot(var24.x() - this.start.x(), var24.z() - this.start.z());
                                 if (!(var25 > 0.32)) {
                                    double var27 = var25 <= 0.09 && var24.pose() == this.start.pose() ? this.replay(var1, var24, 0, var3, var4) : -1.0;
                                    var var29 = new ArrayList<>(var12.prefix());
                                    var29.add(var23);
                                    if (var27 >= -0.04) {
                                       return this.withApproach(var1, var2, var24, var29, var27);
                                    }

                                    var10.add(new AiJumpManeuver.Inner_TaskQueue(var24, var29, var25 + var24.speed() * 0.35 + (var16 ? 0.025 : 0.0)));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            var10.sort(Comparator.comparingDouble(AiJumpManeuver.Inner_TaskQueue::distance));
            if (var10.isEmpty()) {
               return null;
            }

            var7 = var10.subList(0, Math.min(18, var10.size()));

            for (AiJumpManeuver.Inner_TaskQueue var31 : (Iterable<AiJumpManeuver.Inner_TaskQueue>)(Iterable<?>)(var7)) {
               if (var31.state().pose() == this.start.pose()) {
                  double var32 = this.replay(var1, var31.state(), 0, var3, var4);
                  if (!(var32 < -0.04)) {
                     return this.withApproach(var1, var2, var31.state(), var31.prefix(), var32);
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private AiJumpManeuver withApproach(
      AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, AiPhysics.MotionState var3, List<AiPhysics.MoveInput> var4, double var5
   ) {
      var var7 = new ArrayList<>(var4);
      var7.addAll(this.inputs);

      for (AiPhysics.MoveInput var9 : this.inputs) {
         var3 = AiPhysics.step(var3, var9, var1);
      }

      return new AiJumpManeuver(var7, var2, var3, var5, this.takeoffTick + var4.size());
   }

   static AiJumpSolver.JumpPlan solve(AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7) {
      return solve(var0, var1, var2, var4, var5, var7, true, false);
   }

   static AiJumpSolver.JumpPlan solve(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7, boolean var8, boolean var9
   ) {
      return solve(var0, var1, var2, var4, var5, var7, var8, var9, null);
   }

   static AiJumpSolver.JumpPlan solve(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7, boolean var8, boolean var9, int[] var10
   ) {
      double var11 = var1.getX() + 0.5;
      double var13 = var1.getZ() + 0.5;
      float var15 = AiPhysics.yawBetween(var11, var13, var4.getX() + 0.5, var4.getZ() + 0.5);
      var var16 = headings(var15);
      if (var8) {
         if (!AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var11, var2, var13)) {
            AiJumpSolver.JumpPlan var17 = narrowExit(var0, var1, var2, var4, var5, var7, var9);
            if (var17 != null) {
               return var17;
            }
         }

         AiJumpSolver.JumpPlan var41 = offsetExit(var0, var1, var2, var4, var5, var7, var15);
         if (var41 != null) {
            return var41;
         }

         if (var2 - var5 >= 1.5
            && Math.hypot(var4.getX() - var1.getX(), var4.getZ() - var1.getZ()) >= 4.0
            && AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var11, var2, var13)) {
            AiPhysics.MotionState var18 = AiPhysics.MotionState.standing(var11, var2, var13);

            for (float var22 : new float[]{35.0F, -35.0F, 37.5F, -37.5F}) {
               float var23 = var15 + var22;
               AiJumpManeuver var24 = search(var0, var18, var1, var4, var5, var23, var7, var9, var9 ? COMMON_RELEASES : RECOVERY_RELEASES);
               if (var24 != null && var24.margin() >= 0.04) {
                  return plan(var24, var23, var7, 0.0);
               }
            }
         }
      }

      AiJumpSolver.JumpPlan var42 = null;
      boolean var43 = AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var11, var2, var13);
      List var44 = null;
      if (!var43 && var8) {
         var44 = new ArrayList<>(AiJumpSolver.putAt_List_TokenCore_TaskStat(var0, var1, var2));
         addPanelEdges(var0, var1, var2, var44);
         var44 = AiJumpSolver.putAt(var44);
      }

      for (float var48 : var16) {
         double var49 = Math.toRadians(var48);
         double var50 = -Math.sin(var49);
         double var26 = Math.cos(var49);
         if (var43) {
            double var51 = backRoom(var0, var11, var13, var2, var50, var26);
            double[] var52 = var8 && var51 > 0.8 ? new double[]{0.0, 0.6, var51} : (var51 > 0.3 ? new double[]{0.0, var51} : new double[]{0.0});

            for (double var55 : var52) {
               AiPhysics.MotionState var56 = new AiPhysics.MotionState(
                  var11 - var50 * var55, var2, var13 - var26 * var55, 0.0, -0.0784, 0.0, true, 0, false, false
               );
               AiJumpManeuver var37 = search(var0, var56, var1, var4, var5, var48, var7, var9, var9 ? COMMON_RELEASES : (var10 == null ? RELEASES : var10));
               if (var37 != null) {
                  AiJumpSolver.JumpPlan var57 = plan(var37, var48, var7, var55);
                  if (var42 == null || var37.margin() > var42.margin()) {
                     var42 = var57;
                  }

                  if (var37.margin() >= 0.08) {
                     return var57;
                  }
               }
            }
         } else if (var44 != null) {
            for (AiJumpSolver.Point2D var29 : (Iterable<AiJumpSolver.Point2D>)(Iterable<?>)(var44)) {
               double var30 = backRoom(var0, var29.x(), var29.z(), var2, var50, var26);
               double[] var32 = var30 > 0.3 ? new double[]{0.0, var30} : new double[]{0.0};

               for (double var36 : var32) {
                  AiPhysics.MotionState var38 = new AiPhysics.MotionState(
                     var29.x() - var50 * var36, var2, var29.z() - var26 * var36, 0.0, -0.0784, 0.0, true, 0, false, false
                  );
                  AiJumpManeuver var39 = search(var0, var38, var1, var4, var5, var48, var7, var9, var9 ? COMMON_RELEASES : (var10 == null ? RELEASES : var10));
                  if (var39 != null) {
                     AiJumpSolver.JumpPlan var40 = plan(var39, var48, var7, Math.hypot(var38.x() - var11, var38.z() - var13));
                     if (var42 == null || var39.margin() > var42.margin()) {
                        var42 = var40;
                     }

                     if (var39.margin() >= 0.08) {
                        return var40;
                     }
                  }
               }
            }
         }
      }

      if (var42 != null && var42.margin() >= -0.04) {
         return var42;
      }

      if (!var8) {
         return null;
      }

      if (var5 - var2 > 0.6 && Math.hypot(var4.getX() - var1.getX(), var4.getZ() - var1.getZ()) >= 3.5) {
         AiJumpSolver.JumpPlan var47 = risingOffsetExit(var0, var1, var2, var4, var5, var7, var15);
         if (var47 != null) {
            return var47;
         }
      }

      return solveAroundWall(var0, var1, var2, var4, var5, var7, var15, var16, var9);
   }

   private static AiJumpSolver.JumpPlan offsetExit(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7, float var8
   ) {
      float var9 = Math.round(var8 / 90.0F) * 90.0F;
      float var10 = Mth.wrapDegrees(var8 - var9);
      if (!(Math.abs(var10) < 8.0F) && !(Math.abs(var10) > 40.0F)) {
         int var11 = var10 < 0.0F ? 1 : -1;
         AiPhysics.MotionState var12 = AiPhysics.MotionState.standing(var1.getX() + 0.5, var2, var1.getZ() + 0.5);
         AiPhysics.MotionState var13 = var12;
         ArrayList<AiPhysics.MoveInput> var14 = new ArrayList<>();

         for (int var15 = 0; var15 <= 3; var15++) {
            AiJumpManeuver.Inner_ConfigStore var16 = prepareLaunch(var0, var13, var9, var9, var7);
            if (var16 != null) {
               for (int var20 : new int[]{3, 5, 7, 1}) {
                  for (int var24 : new int[]{7, 9, 11, 2147483647, 5}) {
                     AiJumpManeuver var25 = flight(var0, var12, var14, var4, var5, var16, var9, var7, 0, var24, var11, var20);
                     if (var25 != null) {
                        return plan(var25, var9, var7, 0.0);
                     }
                  }
               }
            }

            AiPhysics.MoveInput var26 = AiPhysics.MoveInput.run(var9, var7);
            AiPhysics.MotionState var27 = AiPhysics.step(var13, var26, var0);
            if (!var27.onGround() || var27.horizontalCollision() || AiPhysics.hitsStrictHazard(var0, var13, var27)) {
               break;
            }

            var14.add(var26);
            var13 = var27;
         }

         return null;
      } else {
         return null;
      }
   }

   private static AiJumpSolver.JumpPlan narrowExit(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7, boolean var8
   ) {
      var var9 = new ArrayList<>(AiJumpSolver.putAt_List_TokenCore_TaskStat(var0, var1, var2));
      boolean var10 = var5 > var2 + 0.6;
      boolean var11 = var10 || var1.distSqr(var4) >= 7.0;
      if (var10) {
         for (double[] var15 : new double[][]{{0.3, 0.06}, {0.34, 0.18}}) {
            for (int var19 : new int[]{-1, 1}) {
               for (int var23 : new int[]{-1, 1}) {
                  for (int var24 = 0; var24 < 2; var24++) {
                     double var25 = var19 * var15[0];
                     double var27 = var23 * var15[1];
                     double var29 = var1.getX() + 0.5 + (var24 == 0 ? var25 : var27);
                     double var31 = var1.getZ() + 0.5 + (var24 == 0 ? var27 : var25);
                     if (AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var29, var2, var31)) {
                        var9.add(new AiJumpSolver.Point2D(var29, var31));
                     }
                  }
               }
            }
         }
      }

      if (var11) {
         addPanelEdges(var0, var1, var2, var9);
      }

      float[] var46 = var8 ? COMMON_NARROW_HEADINGS : NARROW_HEADINGS;
      int[] var47 = var8 ? COMMON_NARROW_DELAYS : NARROW_DELAYS;
      AiJumpManeuver.Inner_CachePool var48 = new AiJumpManeuver.Inner_CachePool(128);

      for (AiJumpSolver.Point2D var50 : var8 ? AiJumpSolver.putAt(var9) : var9) {
         if (Thread.currentThread().isInterrupted()) {
            return null;
         }

         AiPhysics.MotionState var51 = AiPhysics.MotionState.standing(var50.x(), var2, var50.z());
         float var52 = AiPhysics.yawBetween(var51.x(), var51.z(), var4.getX() + 0.5, var4.getZ() + 0.5);

         for (float var56 : var46) {
            float var57 = Math.round(var52 + var56);
            int var58 = var56 < 0.0F ? -1 : 1;
            AiPhysics.MotionState var59 = var51;
            ArrayList<AiPhysics.MoveInput> var26 = new ArrayList<>();
            AiPhysics.MoveInput var60 = AiPhysics.MoveInput.run(var57, var7);

            for (int var28 = 0; var28 <= (var11 ? 4 : 0); var28++) {
               for (boolean var32 : var10 && var7 ? new boolean[]{true, false} : new boolean[]{var7}) {
                  AiJumpManeuver.Inner_ConfigStore var33 = prepareLaunch(var0, var59, var57, var57, var32);
                  if (var33 != null) {
                     int[] var34 = var10 ? new int[]{0, 2, 4, 6, 8, 2147483647} : new int[]{11, 8, 5, 2147483647};

                     for (int var38 : var47) {
                        AiJumpManeuver.Inner_BufferHelper var39 = new AiJumpManeuver.Inner_BufferHelper();
                        AiJumpManeuver var40 = flight(var0, var51, var26, var4, var5, var33, var57, var32, 0, 2147483647, var58, var38, var39, var48, var8);

                        for (int var44 : var34) {
                           AiJumpManeuver var45 = var44 == 2147483647
                              ? var40
                              : flight(var0, var51, var26, var4, var5, var33, var57, var32, 0, var44, var58, var38, var39, var48, var8);
                           if (var45 != null) {
                              return plan(var45, var57, var7, Math.hypot(var50.x() - var1.getX() - 0.5, var50.z() - var1.getZ() - 0.5));
                           }
                        }
                     }
                  }
               }

               AiPhysics.MotionState var62 = AiPhysics.step(var59, var60, var0);
               if (!var62.onGround() || var62.horizontalCollision() || Math.abs(var62.y() - var2) > 0.02 || AiPhysics.hitsStrictHazard(var0, var59, var62)) {
                  break;
               }

               var26.add(var60);
               var59 = var62;
            }
         }
      }

      return var7 && var5 >= var2 - 1.5 && var1.distSqr(var4) >= 9.0 ? AiPanelWrapJump.putAt(var0, var1, var2, var4, var5) : null;
   }

   private static void addPanelEdges(AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, List<AiJumpSolver.Point2D> var4) {
      double var5 = var1.getX();
      double var7 = var1.getZ();
      AABB var9 = new AABB(var5, var2 - 0.02, var7, var5 + 1.0, var2 - 0.001, var7 + 1.0);

      for (AABB var11 : var0.collisions(var9)) {
         if (!(Math.abs(var11.maxY - var2) > 0.01)) {
            for (int var12 = 0; var12 < 2; var12++) {
               double var13 = var12 == 0 ? var11.getXsize() : var11.getZsize();
               double var15 = var12 == 0 ? var11.getZsize() : var11.getXsize();
               if (!(var13 > 0.25) && !(var15 < 0.75)) {
                  double var17 = var12 == 0 ? var11.minX : var11.minZ;
                  double var19 = var12 == 0 ? var11.maxX : var11.maxZ;
                  double var21 = var12 == 0 ? var7 : var5;

                  for (double var26 : new double[]{0.288, 0.225}) {
                     for (double var31 : new double[]{var17 - var26, var19 + var26}) {
                        if (!(var31 < (var12 == 0 ? var5 : var7)) && !(var31 >= (var12 == 0 ? var5 : var7) + 1.0)) {
                           for (double var36 : new double[]{0.44, 0.56, -0.265, 1.265}) {
                              double var38 = var12 == 0 ? var31 : var21 + var36;
                              double var40 = var12 == 0 ? var21 + var36 : var31;
                              if (AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var38, var2, var40)) {
                                 var4.add(new AiJumpSolver.Point2D(var38, var40));
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

   private static AiJumpSolver.JumpPlan plan(AiJumpManeuver var0, float var1, boolean var2, double var3) {
      AiPhysics.MotionState var5 = var0.start();
      AiPhysics.MotionState var6 = var0.landing();
      return new AiJumpSolver.JumpPlan(
         var1,
         var0.takeoffTick(),
         var2,
         2147483647,
         var0.inputs().size() - var0.takeoffTick(),
         var5.x(),
         var5.z(),
         var6.x(),
         var6.y(),
         var6.z(),
         var6.speed(),
         var0.margin(),
         var3,
         0.0,
         var0
      );
   }

   static AiJumpSolver.JumpPlan roofedLanding(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7, boolean var8
   ) {
      double[] var9 = var8 ? new double[]{var2 - 0.5, var2} : new double[]{var2};
      double[] var10 = new double[]{0.0, -0.38, 0.38, -0.2, 0.2};
      double var11 = var4.getX() + 0.5;
      double var13 = var4.getZ() + 0.5;
      if (!AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var11, var5, var13)) {
         var var15 = AiJumpSolver.putAt_List_TokenCore_TaskStat(var0, var4, var5);
         if (!var15.isEmpty()) {
            var11 = ((AiJumpSolver.Point2D)var15.getFirst()).x();
            var13 = ((AiJumpSolver.Point2D)var15.getFirst()).z();
         }
      }

      for (double var18 : var9) {
         for (double var23 : var10) {
            for (double var28 : var10) {
               if (Thread.currentThread().isInterrupted()) {
                  return null;
               }

               double var30 = var1.getX() + 0.5 + var23;
               double var32 = var1.getZ() + 0.5 + var28;
               if (AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var30, var18, var32)) {
                  AiPhysics.MotionState var34 = AiPhysics.MotionState.standing(var30, var18, var32);
                  float var35 = AiPhysics.yawBetween(var30, var32, var11, var13);

                  for (float var39 : new float[]{0.0F, -6.0F, 6.0F, -15.0F, 15.0F}) {
                     AiPhysics.MoveInput var40 = AiPhysics.MoveInput.run(var35 + var39, var7);
                     AiPhysics.MotionState var41 = var34;
                     ArrayList<AiPhysics.MoveInput> var42 = new ArrayList<>();

                     for (int var43 = 0; var43 <= 6; var43++) {
                        for (int var44 = 0; var44 < 6; var44++) {
                           boolean var45 = var44 == 5;
                           boolean var46 = var44 >= 4;
                           AiPhysics.MotionState var47 = var41;
                           var var48 = new ArrayList<>(var42);
                           float var49 = var40.yaw();
                           byte var50 = 0;

                           for (int var51 = 0; var51 < 20; var51++) {
                              boolean var10000;
                              switch (var44) {
                                 case 0:
                                    var10000 = var51 > 0 && var47.vy() < 0.0 && var47.y() <= var5 + 0.6;
                                    break;
                                 case 1:
                                    var10000 = var51 > 0 && var47.vy() < 0.0 && var47.y() <= var5 + 1.05;
                                    break;
                                 case 2:
                                    var10000 = var51 >= 6;
                                    break;
                                 case 3:
                                    var10000 = var51 >= 4;
                                    break;
                                 case 4:
                                    var10000 = var51 >= 1;
                                    break;
                                 default:
                                    var10000 = true;
                              }

                              boolean var52 = var10000;
                              if (var51 > 0 && var46) {
                                 float var53 = AiPhysics.yawBetween(var47.x(), var47.z(), var11, var13);
                                 var49 += Mth.clamp(Mth.wrapDegrees(var53 - var49), -30.0F, 30.0F);
                              }

                              AiPhysics.MoveInput var63 = var51 == 0
                                 ? new AiPhysics.MoveInput(1.0, 0.0, true, !var52 && var7, var52, var49)
                                 : (
                                    var52
                                       ? new AiPhysics.MoveInput(1.0, 0.0, false, false, true, var49)
                                       : new AiPhysics.MoveInput(1.0, 0.0, false, var7, false, var49)
                                 );
                              AiPhysics.MotionState var54 = AiPhysics.step(var47, var63, var0);
                              if (AiPhysics.hitsStrictHazard(var0, var47, var54) || AiJumpSolver.putAt(var0, var54)) {
                                 break;
                              }

                              var50 = (byte)(var63.sneak() ? var50 + 1 : 0);
                              if (var50 < 3 && var54.pose() != 0 && AiPaceScript.putAt_Z_TokenCore_TaskStat_2(var0, var47, var54)) {
                                 break;
                              }

                              var48.add(var63);
                              if (AiJumpSolver.putAt(var54)) {
                                 if (!(Math.abs(var54.y() - var5) <= 0.02)) {
                                    break;
                                 }

                                 boolean var55 = (int)Math.floor(var54.x()) == var4.getX() && (int)Math.floor(var54.z()) == var4.getZ();
                                 if (var55) {
                                    double var56 = AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var0, var54, var4, var5, var49);
                                    if (var56 >= -0.04) {
                                       AiJumpManeuver var58 = new AiJumpManeuver(var48, var34, var54, var56, var43);
                                       if (var58.replay(var0, var34, 0, var4, var5) >= -0.04) {
                                          return plan(var58, var40.yaw(), var7, Math.hypot(var23, var28));
                                       }
                                    }
                                 }

                                 double var64 = Math.max(0.0, Math.max(var4.getX() - var54.x(), var54.x() - (var4.getX() + 1)));
                                 double var65 = Math.max(0.0, Math.max(var4.getZ() - var54.z(), var54.z() - (var4.getZ() + 1)));
                                 if (Math.hypot(var64, var65) <= 0.45) {
                                    AiJumpSolver.JumpPlan var60 = settleRoofedTouchdown(
                                       var0, var34, var48, var54, var4, var5, var11, var13, var49, var40.yaw(), var7, var43, Math.hypot(var23, var28), var50
                                    );
                                    if (var60 != null) {
                                       return var60;
                                    }
                                 }
                                 break;
                              }

                              var47 = var54;
                           }
                        }

                        AiPhysics.MotionState var62 = AiPhysics.step(var41, var40, var0);
                        if (!var62.onGround() || Math.abs(var62.y() - var18) > 0.02 || AiPhysics.hitsStrictHazard(var0, var41, var62)) {
                           break;
                        }

                        var42.add(var40);
                        var41 = var62;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private static AiJumpSolver.JumpPlan settleRoofedTouchdown(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      List<AiPhysics.MoveInput> var2,
      AiPhysics.MotionState var3,
      BlockPos var4,
      double var5,
      double var7,
      double var9,
      float var11,
      float var12,
      boolean var13,
      int var14,
      double var15,
      int var17
   ) {
      for (int var18 = 0; var18 < 4; var18++) {
         AiPhysics.MotionState var19 = var3;
         ArrayList var20 = new ArrayList(var2.size() + 4);
         var20.addAll(var2);
         float var21 = var11;
         int var22 = var17;

         for (int var23 = 0; var23 < 4; var23++) {
            float var24 = AiPhysics.yawBetween(var19.x(), var19.z(), var7, var9);
            var21 += Mth.clamp(Mth.wrapDegrees(var24 - var21), -30.0F, 30.0F);
            boolean var25 = (int)Math.floor(var19.x()) == var4.getX() && (int)Math.floor(var19.z()) == var4.getZ();
            AiPhysics.MoveInput var10000;
            switch (var18) {
               case 0:
                  var10000 = var25
                     ? new AiPhysics.MoveInput(0.0, 0.0, false, false, true, var21)
                     : new AiPhysics.MoveInput(1.0, 0.0, false, false, true, var21);
                  break;
               case 1:
                  var10000 = AiPhysics.MoveInput.NONE.withYaw(var21);
                  break;
               case 2:
                  var10000 = new AiPhysics.MoveInput(0.0, 0.0, false, false, true, var21);
                  break;
               default:
                  var10000 = var25
                     ? new AiPhysics.MoveInput(-1.0, 0.0, false, false, true, var21)
                     : new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var21);
            }

            AiPhysics.MoveInput var26 = var10000;
            AiPhysics.MotionState var27 = AiPhysics.step(var19, var26, var0);
            if (!AiJumpSolver.putAt(var27) || Math.abs(var27.y() - var5) > 0.02 || AiPhysics.hitsStrictHazard(var0, var19, var27) || AiJumpSolver.putAt(var0, var27)) {
               break;
            }

            var22 = var26.sneak() ? var22 + 1 : 0;
            if (var22 < 3 && var27.pose() != 0 && AiPaceScript.putAt_Z_TokenCore_TaskStat_2(var0, var19, var27)) {
               break;
            }

            var20.add(var26);
            var19 = var27;
            if ((int)Math.floor(var19.x()) == var4.getX() && (int)Math.floor(var19.z()) == var4.getZ()) {
               double var28 = AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var0, var19, var4, var5, var21);
               if (var28 >= -0.04) {
                  AiJumpManeuver var30 = new AiJumpManeuver(var20, var1, var19, var28, var14);
                  if (var30.replay(var0, var1, 0, var4, var5) >= -0.04) {
                     return plan(var30, var12, var13, var15);
                  }
               }
            }
         }
      }

      return null;
   }

   static AiJumpSolver.JumpPlan cornerDeparture(AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7) {
      double var8 = var1.getX() + 0.5;
      double var10 = var1.getZ() + 0.5;
      float var12 = AiPhysics.yawBetween(var8, var10, var4.getX() + 0.5, var4.getZ() + 0.5);
      float var13 = Math.round(var12 / 90.0F) * 90.0F;

      for (int var17 : new int[]{1, -1}) {
         float var18 = var13 + var17 * 90;
         double var19 = Math.toRadians(var18);

         for (double var24 : new double[]{0.68, 0.59, 0.5}) {
            AiPhysics.MotionState var26 = AiPhysics.MotionState.standing(var8 - Math.sin(var19) * var24, var2, var10 + Math.cos(var19) * var24);
            if (AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var26.x(), var2, var26.z())) {
               for (float var30 : new float[]{0.0F, -6.0F, 6.0F}) {
                  AiPhysics.MoveInput var31 = AiPhysics.MoveInput.run(var18 + var30, var7);
                  AiPhysics.MotionState var32 = AiPhysics.step(var26, var31, var0);
                  if (var32.onGround() && !AiPhysics.hitsStrictHazard(var0, var26, var32)) {
                     float var33 = AiPhysics.yawBetween(var32.x(), var32.z(), var4.getX() + 0.5, var4.getZ() + 0.5);

                     for (float var37 : new float[]{0.0F, -15.0F, 15.0F, -25.0F, 25.0F}) {
                        AiPhysics.MoveInput var38 = AiPhysics.MoveInput.jump(var33 + var37, var7);
                        AiPhysics.MoveInput var39 = AiPhysics.MoveInput.run(var33, var7);
                        AiPhysics.MotionState var40 = AiPhysics.step(var32, var38, var0);
                        if (!var40.onGround() && !(var40.y() <= var32.y()) && !AiPhysics.hitsStrictHazard(var0, var32, var40)) {
                           for (double var44 : new double[]{0.2, 0.45, 0.7}) {
                              AiPhysics.MotionState var46 = var40;
                              var var47 = new ArrayList<>(List.of(var31, var38));

                              for (int var48 = 0; var48 < 16; var48++) {
                                 AiPhysics.MoveInput var49 = var46.vy() < 0.0 && var46.y() <= var5 + var44
                                    ? new AiPhysics.MoveInput(1.0, 0.0, false, false, true, var33)
                                    : var39;
                                 AiPhysics.MotionState var50 = AiPhysics.step(var46, var49, var0);
                                 if (AiPhysics.hitsStrictHazard(var0, var46, var50) || var50.y() < var5 - 0.55) {
                                    break;
                                 }

                                 var47.add(var49);
                                 if (AiJumpSolver.putAt(var50)) {
                                    BlockPos var51 = BlockPos.containing(var50.x(), var4.getY(), var50.z());
                                    if (var51.equals(var4) && Math.abs(var50.y() - var5) <= 0.02) {
                                       AiJumpManeuver var52 = new AiJumpManeuver(var47, var26, var50, 0.0, 1);
                                       double var53 = var52.replay(var0, var26, 0, var4, var5);
                                       if (var53 >= -0.04) {
                                          return plan(new AiJumpManeuver(var47, var26, var50, var53, 1), var31.yaw(), var7, var24);
                                       }
                                    }
                                    break;
                                 }

                                 var46 = var50;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      if (var5 > var2 + 0.4 && var1.distSqr(var4) <= 5.0) {
         double var63 = var4.getX() + 0.5;
         double var64 = var4.getZ() + 0.5;
         if (!AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var63, var5, var64)) {
            var var65 = AiJumpSolver.putAt_List_TokenCore_TaskStat(var0, var4, var5);
            if (!var65.isEmpty()) {
               var63 = ((AiJumpSolver.Point2D)var65.getFirst()).x();
               var64 = ((AiJumpSolver.Point2D)var65.getFirst()).z();
            }
         }

         var var66 = new ArrayList<>(AiJumpSolver.putAt_List_TokenCore_TaskStat(var0, var1, var2));

         for (int var72 : new int[]{1, -1}) {
            float var74 = var13 + var72 * 90;
            double var76 = Math.toRadians(var74);
            double var78 = Math.toRadians(var13);

            for (double var87 : new double[]{0.42, 0.62, 0.74}) {
               for (double var97 : new double[]{0.0, -0.38, -0.68}) {
                  double var102 = var8 - Math.sin(var76) * var87 - Math.sin(var78) * var97;
                  double var105 = var10 + Math.cos(var76) * var87 + Math.cos(var78) * var97;
                  if (AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var102, var2, var105)) {
                     var66.add(new AiJumpSolver.Point2D(var102, var105));
                  }
               }
            }
         }

         for (AiJumpSolver.Point2D var69 : AiJumpSolver.putAt(var66)) {
            AiPhysics.MotionState var71 = AiPhysics.MotionState.standing(var69.x(), var2, var69.z());

            for (int var25 : new int[]{1, -1}) {
               for (float var84 : new float[]{15.0F, 30.0F, 45.0F, 60.0F, 75.0F, 90.0F, 105.0F}) {
                  float var86 = var13 + var25 * var84;

                  for (int var88 = 0; var88 <= 2; var88++) {
                     AiPhysics.MotionState var89 = var71;
                     ArrayList<AiPhysics.MoveInput> var91 = new ArrayList<>();
                     boolean var93 = true;

                     for (int var95 = 0; var95 < var88; var95++) {
                        AiPhysics.MoveInput var98 = new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var86);
                        AiPhysics.MotionState var100 = AiPhysics.step(var89, var98, var0);
                        if (!var100.onGround() || Math.abs(var100.y() - var2) > 0.02 || AiPhysics.hitsStrictHazard(var0, var89, var100)) {
                           var93 = false;
                           break;
                        }

                        var91.add(var98);
                        var89 = var100;
                     }

                     if (var93) {
                        for (boolean var103 : var7 ? new boolean[]{true, false} : new boolean[]{false}) {
                           for (boolean var108 : new boolean[]{false, true}) {
                              AiPhysics.MoveInput var109 = new AiPhysics.MoveInput(1.0, 0.0, true, var103 && !var108, var108, var86);
                              AiPhysics.MotionState var110 = AiPhysics.step(var89, var109, var0);
                              if (!var110.onGround() && !(var110.y() <= var2 + 0.15) && !AiPhysics.hitsStrictHazard(var0, var89, var110)) {
                                 for (int var113 : new int[]{0, 1, 2, 3}) {
                                    for (boolean var117 : new boolean[]{false, true}) {
                                       AiPhysics.MotionState var118 = var110;
                                       var var54 = new ArrayList<>(var91);
                                       var54.add(var109);
                                       float var55 = var86;

                                       for (int var56 = 1; var56 <= 16; var56++) {
                                          float var57 = var56 <= var113 ? var86 : AiPhysics.yawBetween(var118.x(), var118.z(), var63, var64);
                                          var55 += Mth.clamp(Mth.wrapDegrees(var57 - var55), -30.0F, 30.0F);
                                          AiPhysics.MoveInput var58 = new AiPhysics.MoveInput(1.0, 0.0, false, !var117 && var7, var117, var55);
                                          AiPhysics.MotionState var59 = AiPhysics.step(var118, var58, var0);
                                          if (AiPhysics.hitsStrictHazard(var0, var118, var59) || var59.y() < var2 - 0.55) {
                                             break;
                                          }

                                          var54.add(var58);
                                          var118 = var59;
                                          if (AiJumpSolver.putAt(var118)) {
                                             if (Math.abs(var118.y() - var5) <= 0.02
                                                && (int)Math.floor(var118.x()) == var4.getX()
                                                && (int)Math.floor(var118.z()) == var4.getZ()) {
                                                double var60 = AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var0, var118, var4, var5, var55);
                                                if (var60 >= -0.04) {
                                                   AiJumpManeuver var62 = new AiJumpManeuver(var54, var71, var118, var60, var88);
                                                   if (var62.replay(var0, var71, 0, var4, var5) >= -0.04) {
                                                      return plan(var62, var86, var103, Math.hypot(var69.x() - var8, var69.z() - var10));
                                                   }
                                                }
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
            }
         }
      }

      return null;
   }

   static AiJumpSolver.JumpPlan lowClearance(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7, boolean var8, boolean var9
   ) {
      double var10 = var1.getX() + 0.5;
      double var12 = var1.getZ() + 0.5;
      AiPhysics.MotionState var14 = new AiPhysics.MotionState(
         var10, var2, var12, 0.0, -0.0784, 0.0, true, 0, false, false, -9223372036854775808L, false, var7 ? 1 : 0
      );
      if (!var0.collisions(var14.box(false)).isEmpty()) {
         return null;
      }

      float var15 = AiPhysics.yawBetween(var10, var12, var4.getX() + 0.5, var4.getZ() + 0.5);

      for (float var19 : new float[]{0.0F, -10.0F, 10.0F}) {
         float var20 = var15 + var19;
         ArrayList<AiPhysics.MoveInput> var21 = new ArrayList<>();
         AiPhysics.MotionState var22 = var14;

         for (int var23 = 0; var23 < 36; var23++) {
            AiPhysics.MoveInput var24 = new AiPhysics.MoveInput(var8 && var23 == 0 ? 0.0 : 1.0, 0.0, false, !var8 && var9, var8, var20);
            AiPhysics.MotionState var25 = AiPhysics.step(var22, var24, var0);
            if (AiPhysics.hitsStrictHazard(var0, var22, var25) || AiJumpSolver.putAt(var0, var25) || var25.y() < var5 - 0.6) {
               break;
            }

            var21.add(var24);
            var22 = var25;
            if (AiJumpSolver.putAt(var22)
               && !(Math.abs(var22.y() - var5) > 0.02)
               && (int)Math.floor(var22.x()) == var4.getX()
               && (int)Math.floor(var22.z()) == var4.getZ()) {
               double var26 = AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var0, var22, var4, var5, var20);
               if (var26 >= -0.04) {
                  return plan(new AiJumpManeuver(var21, var14, var22, var26, var21.size()), var20, var9 && !var8, 0.0);
               }
            }
         }
      }

      return null;
   }

   private static AiJumpSolver.JumpPlan risingOffsetExit(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7, float var8
   ) {
      if (!var7) {
         return null;
      }

      float var9 = Math.round(var8 / 90.0F) * 90.0F;
      float var10 = Mth.wrapDegrees(var8 - var9);
      if (!(Math.abs(var10) < 5.0F) && !(Math.abs(var10) > 40.0F)) {
         int var11 = var10 < 0.0F ? -1 : 1;
         double var12 = Math.toRadians(var9);
         double var14 = -Math.sin(var12);
         double var16 = Math.cos(var12);

         for (double var21 : new double[]{0.45, 0.35, 0.55}) {
            for (double var26 : new double[]{0.4, 0.48, 0.3}) {
               double var28 = var1.getX() + 0.5 - var14 * var26 + var16 * var21 * var11;
               double var30 = var1.getZ() + 0.5 - var16 * var26 - var14 * var21 * var11;
               if (AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var28, var2, var30)) {
                  AiPhysics.MotionState var32 = AiPhysics.MotionState.standing(var28, var2, var30);

                  for (float var36 : new float[]{15.0F, 12.0F, 18.0F}) {
                     float var37 = Math.round(var8) + var11 * var36;
                     AiPhysics.MoveInput var38 = AiPhysics.MoveInput.run(var37, true);
                     AiPhysics.MotionState var39 = var32;
                     ArrayList<AiPhysics.MoveInput> var40 = new ArrayList<>();

                     for (int var41 = 0; var41 <= 9; var41++) {
                        if (var39.speed() >= 0.13) {
                           AiJumpManeuver.Inner_ConfigStore var42 = prepareLaunch(var0, var39, var37, var37, true);
                           if (var42 != null) {
                              for (int var46 : new int[]{2, 4, 0}) {
                                 for (int var50 : new int[]{8, 7, 9}) {
                                    AiJumpManeuver var51 = flight(var0, var32, var40, var4, var5, var42, var37, true, 0, var50, var11, var46);
                                    if (var51 != null) {
                                       return plan(var51, var37, true, Math.hypot(var21, var26));
                                    }
                                 }
                              }
                           }
                        }

                        AiPhysics.MotionState var52 = AiPhysics.step(var39, var38, var0);
                        if (!var52.onGround() || var52.horizontalCollision() || Math.abs(var52.y() - var2) > 0.02 || AiPhysics.hitsStrictHazard(var0, var39, var52)) {
                           break;
                        }

                        var40.add(var38);
                        var39 = var52;
                     }
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static AiJumpSolver.JumpPlan solveAroundWall(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7, float var8, List<Float> var9, boolean var10
   ) {
      double var11 = Math.toRadians(var8);
      double var13 = -Math.sin(var11);
      double var15 = Math.cos(var11);
      double var17 = var1.getX() + 0.5;
      double var19 = var1.getZ() + 0.5;
      AABB var21 = new AABB(
         var17 + Math.min(var13 * 0.6, var13 * 1.35) - 0.2,
         var2 + 0.3,
         var19 + Math.min(var15 * 0.6, var15 * 1.35) - 0.2,
         var17 + Math.max(var13 * 0.6, var13 * 1.35) + 0.2,
         var2 + 1.65,
         var19 + Math.max(var15 * 0.6, var15 * 1.35) + 0.2
      );
      if (var0.collisions(var21).isEmpty()) {
         return null;
      }

      for (double var25 : new double[]{-0.45, 0.45}) {
         for (double var30 : new double[]{0.25, 0.5}) {
            double var32 = var17 - var13 * var30 + var15 * var25;
            double var34 = var19 - var15 * var30 - var13 * var25;
            if (AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var32, var2, var34)
               && AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, (var32 + var17) * 0.5, var2, (var34 + var19) * 0.5)) {
               AiPhysics.MotionState var36 = AiPhysics.MotionState.standing(var32, var2, var34);

               for (float var38 : var9) {
                  AiJumpManeuver var39 = search(var0, var36, var1, var4, var5, var38, var7, var10);
                  if (var39 != null) {
                     return plan(var39, var38, var7, Math.hypot(var25, var30));
                  }
               }
            }
         }
      }

      return Math.abs(var2 - var5) <= 0.25 && var1.distSqr(var4) <= 9.0 ? shoulderJump(var0, var1, var2, var4, var5, var7, var8) : null;
   }

   private static AiJumpManeuver recordFlight(
      AiPhysics.MotionState var0,
      AiPhysics.MotionState var1,
      AiPhysics.MoveInput var2,
      AiJumpManeuver.Inner_StreamUtil var3,
      double var4,
      int var6
   ) {
      ArrayDeque var7 = new ArrayDeque();
      var7.add(var2);

      for (AiJumpManeuver.Inner_StreamUtil var8 = var3; var8 != null; var8 = var8.parent()) {
         var7.addFirst(var8.input());
      }

      return new AiJumpManeuver(List.copyOf(var7), var0, var1, var4, var6);
   }

   private static List<AiJumpManeuver.Inner_StreamUtil> retainFlight(List<AiJumpManeuver.Inner_StreamUtil> var0, double var1, double var3, int var5) {
      var0.sort(Comparator.comparingDouble(AiJumpManeuver.Inner_StreamUtil::distance));
      HashSet<Long> var6 = new HashSet<>();
      ArrayList var7 = new ArrayList(var5);

      for (AiJumpManeuver.Inner_StreamUtil var9 : var0) {
         AiPhysics.MotionState var10 = var9.state();
         long var11 = (Math.round((var10.x() - var1) * 40.0) & 511L) << 21
            | (Math.round((var10.z() - var3) * 40.0) & 511L) << 12
            | (Math.round(var10.vx() * 80.0) & 63L) << 6
            | Math.round(var10.vz() * 80.0) & 63L;
         if (var6.add(var11)) {
            var7.add(var9);
         }

         if (var7.size() == var5) {
            break;
         }
      }

      return var7;
   }

   private static AiJumpSolver.JumpPlan shoulderJump(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7, float var8
   ) {
      double var9 = Math.toRadians(var8);
      double var11 = -Math.sin(var9);
      double var13 = Math.cos(var9);
      double var15 = var1.getX() + 0.5;
      double var17 = var1.getZ() + 0.5;
      boolean var19 = AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var15 + var13 * 0.6, var2, var17 - var11 * 0.6)
         || AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var15 - var13 * 0.6, var2, var17 + var11 * 0.6);
      double[] var20 = var19 ? new double[]{-0.77, 0.77, -0.71, 0.71, -0.6, 0.6} : new double[]{-0.5, 0.5, -0.4, 0.4, -0.3, 0.3, -0.18, 0.18};
      double[] var21 = var19 ? new double[]{0.25, 0.0, 0.45, 0.65, -0.25} : new double[]{0.35, 0.2, 0.0, 0.45, -0.15};
      boolean[] var22 = !var7 || var19 && !(var1.distSqr(var4) <= 5.0) ? new boolean[]{var7} : new boolean[]{true, false};

      for (boolean var26 : var22) {
         for (double var30 : var20) {
            for (double var35 : var21) {
               double var37 = var15 - var11 * var35 + var13 * var30;
               double var39 = var17 - var13 * var35 - var11 * var30;
               if (AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var37, var2, var39)
                  && AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, (var37 + var15) * 0.5, var2, (var39 + var17) * 0.5)) {
                  AiPhysics.MotionState var41 = AiPhysics.MotionState.standing(var37, var2, var39);

                  for (float var45 : new float[]{7.0F, 12.0F, 20.0F, 30.0F, 42.0F}) {
                     float var46 = var8 - (float)Math.copySign(var45, var30);
                     AiPhysics.MotionState var47 = var41;
                     AiJumpManeuver.Inner_StreamUtil var48 = null;

                     for (int var49 = 0; var49 <= 3; var49++) {
                        AiPhysics.MoveInput var50 = AiPhysics.MoveInput.jump(var46, var26);
                        AiPhysics.MotionState var51 = AiPhysics.step(var47, var50, var0);
                        if (!var51.onGround() && !AiPhysics.hitsStrictHazard(var0, var47, var51)) {
                           AiJumpManeuver var52 = steerAroundWall(
                              var0, var41, new AiJumpManeuver.Inner_StreamUtil(var51, var50, var48, false, 0.0), var1, var4, var5, var46, var26, var30, var49
                           );
                           if (var52 != null) {
                              return plan(var52, var46, var26, Math.hypot(var30, var35));
                           }
                        }

                        if (var49 >= 3) {
                           break;
                        }

                        AiPhysics.MoveInput var54 = AiPhysics.MoveInput.run(var46, var26);
                        AiPhysics.MotionState var53 = AiPhysics.step(var47, var54, var0);
                        if (!var53.onGround() || var53.horizontalCollision() || AiPhysics.hitsStrictHazard(var0, var47, var53)) {
                           break;
                        }

                        var48 = new AiJumpManeuver.Inner_StreamUtil(var53, var54, var48, false, 0.0);
                        var47 = var53;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private static AiJumpManeuver steerAroundWall(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      AiJumpManeuver.Inner_StreamUtil var2,
      BlockPos var3,
      BlockPos var4,
      double var5,
      float var7,
      boolean var8,
      double var9,
      int var11
   ) {
      List var12 = List.of(var2);
      double var13 = var4.getX() + 0.5;
      double var15 = var4.getZ() + 0.5;
      double var17 = var13 - (var3.getX() + 0.5);
      double var19 = var15 - (var3.getZ() + 0.5);
      double var21 = Math.hypot(var17, var19);
      double var23 = var17 / var21;
      double var25 = var19 / var21;
      double var27 = 0.5;
      double var29 = 1.5;
      AABB var31 = new AABB(
         var3.getX() + 0.5 + Math.min(var23 * 0.55, var23 * 1.45) - 0.35,
         var5 + 0.3,
         var3.getZ() + 0.5 + Math.min(var25 * 0.55, var25 * 1.45) - 0.35,
         var3.getX() + 0.5 + Math.max(var23 * 0.55, var23 * 1.45) + 0.35,
         var5 + 1.65,
         var3.getZ() + 0.5 + Math.max(var25 * 0.55, var25 * 1.45) + 0.35
      );
      double var32 = -Math.signum(var9);
      double var34 = -1.0 / 0.0;
      double var36 = -1.0 / 0.0;

      for (AABB var39 : var0.collisions(var31)) {
         double var40 = var39.minX - (var3.getX() + 0.5);
         double var42 = var39.maxX - (var3.getX() + 0.5);
         double var44 = var39.minZ - (var3.getZ() + 0.5);
         double var46 = var39.maxZ - (var3.getZ() + 0.5);

         for (double var51 : new double[]{var40, var42}) {
            for (double var56 : new double[]{var44, var46}) {
               double var58 = (var51 * -var25 + var56 * var23) * var32;
               if (var58 > var34) {
                  var34 = var58;
               }

               double var60 = var51 * var23 + var56 * var25;
               if (var60 > var36) {
                  var36 = var60;
               }
            }
         }
      }

      if (var34 > 0.0 && var34 < 0.5) {
         var27 = var34;
      }

      if (var36 > 0.5 && var36 < 1.5) {
         var29 = var36;
      }

      double var65 = Math.clamp(var27 + 0.3 + 0.03, 0.45, 0.83);
      double var66 = Math.max(0.13, var21 - (var29 + 0.3 + 0.02));

      for (int var67 = 0; var67 < 22; var67++) {
         if (Thread.currentThread().isInterrupted()) {
            return null;
         }

         ArrayList<AiJumpManeuver.Inner_StreamUtil> var43 = new ArrayList<>();

         for (AiJumpManeuver.Inner_StreamUtil var45 : (Iterable<AiJumpManeuver.Inner_StreamUtil>)(Iterable<?>)(var12)) {
            for (int var69 = -1; var69 <= 1; var69++) {
               for (int var47 = -1; var47 <= 1; var47++) {
                  AiPhysics.MoveInput var70 = new AiPhysics.MoveInput(
                     var69, var47, false, var8 && var69 > 0 && !var45.state().onGround(), false, var7
                  );
                  AiPhysics.MotionState var71 = AiPhysics.step(var45.state(), var70, var0);
                  if (!(var71.y() < var5 - 0.08) && !AiPhysics.hitsStrictHazard(var0, var45.state(), var71) && !AiJumpSolver.putAt(var0, var71)) {
                     if (AiJumpSolver.putAt(var71)) {
                        if (Math.abs(var71.y() - var5) > 0.02) {
                           continue;
                        }

                        double var72 = AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var0, var71, var4, var5, var7);
                        if (var72 >= -0.04) {
                           return recordFlight(var1, var71, var70, var45, var72, var11);
                        }

                        if (Math.hypot(var13 - var71.x(), var15 - var71.z()) > 1.35) {
                           continue;
                        }
                     }

                     double var73 = var13 - var71.x();
                     double var52 = var15 - var71.z();
                     double var74 = var73 * var23 + var52 * var25;
                     boolean var75 = var45.cleared() || var74 < var66;
                     double var57 = var71.vx() * var23 + var71.vz() * var25;
                     double var59 = var75 ? 0.0 : Math.copySign(var65, var9);
                     double var61 = var73 * var25 - var52 * var23 + var59;
                     double var63 = Math.hypot(var74 - 0.8 * var57, var61 * 1.6) + (var75 ? 0.0 : 1.4) + (var71.horizontalCollision() ? 0.12 : 0.0);
                     var43.add(new AiJumpManeuver.Inner_StreamUtil(var71, var70, var45, var75, var63));
                  }
               }
            }
         }

         if (var43.isEmpty()) {
            break;
         }

         var12 = retainFlight(var43, var13, var15, 16);
      }

      return null;
   }

   static AiJumpManeuver live(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, BlockPos var3, double var4, float var6, boolean var7) {
      return search(var0, var1, var2, var3, var4, var6, var7, false, RECOVERY_RELEASES);
   }

   static AiJumpSolver.JumpPlan livePlan(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, BlockPos var3, double var4, float var6, boolean var7
   ) {
      AiJumpManeuver var8 = live(var0, var1, BlockPos.containing(var1.x(), var1.y(), var1.z()), var3, var4, var6, var7);
      return var8 == null ? null : plan(var8, var6, var7, 0.0);
   }

   private static List<Float> headings(float var0) {
      ArrayList<Float> var1 = new ArrayList<>();

      for (int var2 = 0; var2 < 8; var2++) {
         float var3 = var2 * 45.0F;
         if (Math.abs(Mth.wrapDegrees(var3 - var0)) <= 76.0F) {
            var1.add(var3);
         }
      }

      var1.sort(Comparator.comparingDouble(var1x -> Math.abs(Mth.wrapDegrees(var1x - var0))));
      return var1;
   }

   private static double backRoom(AiPhysics.PhysicsWorld var0, double var1, double var3, double var5, double var7, double var9) {
      double var11 = 0.0;

      for (double var13 = 0.1; var13 <= 4.000001 && AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var1 - var7 * var13, var5, var3 - var9 * var13); var13 += 0.1) {
         AiPhysics.MotionState var15 = new AiPhysics.MotionState(var1 - var7 * var11, var5, var3 - var9 * var11, 0.0, 0.0, 0.0, true, 0, false, false);
         AiPhysics.MotionState var16 = new AiPhysics.MotionState(var1 - var7 * var13, var5, var3 - var9 * var13, 0.0, 0.0, 0.0, true, 0, false, false);
         if (AiPhysics.hitsStrictHazard(var0, var15, var16)) {
            break;
         }

         var11 = var13;
      }

      return Math.max(0.0, var11 - 0.1);
   }

   private static AiJumpManeuver search(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, BlockPos var3, double var4, float var6, boolean var7, boolean var8
   ) {
      int[] var9 = var8 ? COMMON_RELEASES : RELEASES;
      return search(var0, var1, var2, var3, var4, var6, var7, var8, var9);
   }

   static double maxFlightReach(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, double var2, boolean var4) {
      double var5 = var1.y();
      double var7 = Math.max(0.42, var1.vy());

      int var9;
      for (var9 = 0; var9 < 40 && (var7 > 0.0 || var5 >= var2 - 0.03); var9++) {
         var5 += var7;
         var7 = (var7 - 0.08) * 0.98;
      }

      if (var9 == 0) {
         return -1.0;
      }

      double var10 = var0.movementSpeed() * (var4 ? 1.3 : 1.0) * 1.02;
      double var12 = var1.speed() + (var4 ? 0.2 : 0.0) + var10;
      double var14 = var12;
      int var16 = AiPhysics.supportBlockX(var1.support(), var1.x());
      int var17 = (int)Math.floor(var1.y() - 0.5000001);
      int var18 = AiPhysics.supportBlockZ(var1.support(), var1.z());
      double var19 = Math.max(0.6, var0.putAt_D_I(var16, var17, var18));
      var12 *= var19 * 0.91;
      double var21 = var4 ? 0.026 : 0.02;

      for (int var23 = 1; var23 < var9; var23++) {
         var12 += var21;
         var14 += var12;
         var12 *= 0.91;
      }

      return var14;
   }

   private static AiJumpManeuver search(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      BlockPos var2,
      BlockPos var3,
      double var4,
      float var6,
      boolean var7,
      boolean var8,
      int[] var9
   ) {
      AiJumpManeuver var10 = null;
      AiJumpManeuver.Inner_CachePool var11 = FLIGHT_WORKSPACE.get();
      var11.putAt();
      float[] var12 = var8 && var4 < var1.y() - 0.01 ? DROP_LAUNCH_TURNS : LAUNCH_TURNS;
      boolean var13 = var8 || var0 instanceof AiPlannerWorld var14 && var14.fill();
      float[] var76 = var8 ? COMMON_AIR_OFFSETS : AIR_OFFSETS;
      float var15 = var13 ? 20.0F : 0.5F;
      double var16 = var2.getX() + 0.5;
      double var18 = var2.getZ() + 0.5;
      double var20 = var3.getX() + 0.5;
      double var22 = var3.getZ() + 0.5;
      double var24 = Math.toRadians(var6);
      double var26 = -Math.sin(var24);
      double var28 = Math.cos(var24);
      LongOpenHashSet var30 = new LongOpenHashSet(32);
      IntOpenHashSet var31 = new IntOpenHashSet(16);
      HashMap var32 = new HashMap(16);
      AiJumpManeuver.Inner_BufferHelper[] var33 = new AiJumpManeuver.Inner_BufferHelper[STRAFES.length];

      for (int var34 = 0; var34 < STRAFES.length; var34++) {
         var33[var34] = new AiJumpManeuver.Inner_BufferHelper();
      }

      for (boolean var37 : var7 ? new boolean[]{false, true} : new boolean[]{false}) {
         AiPhysics.MotionState var38 = var1;
         ArrayList<AiPhysics.MoveInput> var39 = new ArrayList<>();

         for (int var40 = 0; var40 <= 48; var40++) {
            if (Thread.currentThread().isInterrupted()) {
               return null;
            }

            if ((!var37 || var40 != 0) && var38.onGround() && var38.jumpDelay() == 0 && Math.hypot(var38.x() - var16, var38.z() - var18) < 1.25) {
               double var41 = Math.hypot(var20 - var38.x(), var22 - var38.z());
               double var43 = maxFlightReach(var0, var38, var4, var7);
               if (var43 >= 0.0 && var41 <= var43 + 1.85) {
                  float var45 = AiPhysics.yawBetween(var38.x(), var38.z(), var20, var22);
                  var32.clear();
                  var30.clear();
                  var31.clear();

                  for (float var49 : var12) {
                     for (float var53 : var76) {
                        float var54 = var45 + var53;
                        if (!(Math.abs(Mth.wrapDegrees(var54 - var6)) > 100.0F)) {
                           float var55 = var6 + Mth.wrapDegrees(var54 - var6) * var49;
                           int var56 = Float.floatToRawIntBits(var55);
                           long var57 = (long)var56 << 32 | Float.floatToRawIntBits(var54) & 4294967295L;
                           if (var30.add(var57)) {
                              if (!var32.containsKey(var55)) {
                                 var32.put(var55, prepareLaunch(var0, var38, var6, var55, var7));
                              }

                              AiJumpManeuver.Inner_ConfigStore var59 = (AiJumpManeuver.Inner_ConfigStore)var32.get(var55);
                              if (var59 != null) {
                                 double var60 = Math.toRadians(var54);
                                 double var62 = -Math.sin(var60);
                                 double var64 = Math.cos(var60);
                                 double var66 = (var20 - var38.x()) * var62 + (var22 - var38.z()) * var64;
                                 int var68 = Math.abs(Mth.wrapDegrees(var54 - var59.yaw())) <= var15 ? 0 : 2;

                                 for (byte var69 = 0; var69 <= var68; var69 += 2) {
                                    for (int var70 = 0; var70 < STRAFES.length; var70++) {
                                       var33[var70].putAt(var38.x(), var38.z(), var62, var64);
                                    }

                                    for (int var73 : var9) {
                                       for (int var74 = 0; var74 < STRAFES.length; var74++) {
                                          if ((var73 != 0 || var74 != 0 || var31.add(var56))
                                             && (var73 == 2147483647 || var33[var74].putAt_Z || !(var33[var74].prevEx < var66 - 1.55))) {
                                             AiJumpManeuver var75 = flight(
                                                var0, var1, var39, var3, var4, var59, var54, var7, var69, var73, STRAFES[var74], 0, var33[var74], var11, var8
                                             );
                                             if (var75 != null) {
                                                if (var10 == null || var75.margin() > var10.margin()) {
                                                   var10 = var75;
                                                }

                                                if (var10.margin() >= 0.08) {
                                                   return var10;
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
                  }
               }
            }

            if ((var38.x() - var16) * var26 + (var38.z() - var18) * var28 > 1.35) {
               break;
            }

            AiPhysics.MoveInput var78 = new AiPhysics.MoveInput(1.0, 0.0, var37 && var38.onGround(), var7, false, var6);
            AiPhysics.MotionState var42 = AiPhysics.step(var38, var78, var0);
            if (AiPhysics.hitsStrictHazard(var0, var38, var42)
               || AiJumpSolver.putAt(var0, var42)
               || var42.y() < var1.y() - 0.02
               || var42.horizontalCollision()
               || !var37 && !var42.onGround()) {
               break;
            }

            var39.add(var78);
            var38 = var42;
         }
      }

      return var10;
   }

   private static AiJumpManeuver.Inner_ConfigStore prepareLaunch(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, float var3, boolean var4) {
      if (AiJumpSolver.putAt(var0, var1)) {
         return null;
      }

      ArrayList<AiPhysics.MoveInput> var5 = new ArrayList<>();
      AiPhysics.MotionState var6 = var1;
      float var7 = var2;
      if (Math.abs(Mth.wrapDegrees(var3 - var2)) > 60.0F) {
         for (int var8 = 0; Math.abs(Mth.wrapDegrees(var3 - var7)) > 1.0F && var8 < 8; var8++) {
            float var9 = var6.speed() > 0.03 ? var7 : var3;
            float var10 = var7 + AiAim.stepAngleForProfile(Mth.wrapDegrees(var9 - var7), AiAim.AimProfile.FLOW);
            AiPhysics.MoveInput var11 = settleInput(var0, var6, var9).withYaw(var10);
            AiPhysics.MotionState var12 = AiPhysics.step(var6, var11, var0);
            if (AiPhysics.hitsStrictHazard(var0, var6, var12) || AiJumpSolver.putAt(var0, var12) || !var12.onGround() || Math.abs(var12.y() - var1.y()) > 0.02) {
               return null;
            }

            var5.add(var11);
            var6 = var12;
            var7 = var10;
         }

         if (Math.abs(Mth.wrapDegrees(var3 - var7)) > 1.0F) {
            return null;
         }
      }

      AiPhysics.MoveInput var13 = AiPhysics.MoveInput.jump(var3, var4);
      AiPhysics.MotionState var14 = AiPhysics.step(var6, var13, var0);
      if (!AiPhysics.hitsStrictHazard(var0, var6, var14) && !AiJumpSolver.putAt(var0, var14) && !AiJumpSolver.putAt(var14)) {
         var5.add(var13);
         return new AiJumpManeuver.Inner_ConfigStore(var14, var5, Math.max(var14.y(), var1.y()), var3);
      } else {
         return null;
      }
   }

   private static AiJumpManeuver flight(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      List<AiPhysics.MoveInput> var2,
      BlockPos var3,
      double var4,
      AiJumpManeuver.Inner_ConfigStore var6,
      float var7,
      boolean var8,
      int var9,
      int var10,
      int var11,
      int var12
   ) {
      return flight(var0, var1, var2, var3, var4, var6, var7, var8, var9, var10, var11, var12, null);
   }

   private static AiJumpManeuver flight(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      List<AiPhysics.MoveInput> var2,
      BlockPos var3,
      double var4,
      AiJumpManeuver.Inner_ConfigStore var6,
      float var7,
      boolean var8,
      int var9,
      int var10,
      int var11,
      int var12,
      AiJumpManeuver.Inner_BufferHelper var13
   ) {
      return flight(var0, var1, var2, var3, var4, var6, var7, var8, var9, var10, var11, var12, var13, null, false);
   }

   private static AiJumpManeuver flight(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      List<AiPhysics.MoveInput> var2,
      BlockPos var3,
      double var4,
      AiJumpManeuver.Inner_ConfigStore var6,
      float var7,
      boolean var8,
      int var9,
      int var10,
      int var11,
      int var12,
      AiJumpManeuver.Inner_BufferHelper var13,
      AiJumpManeuver.Inner_CachePool var14,
      boolean var15
   ) {
      boolean var16 = var13 != null && var10 == 2147483647;
      if (var13 != null && !var16 && var10 >= var13.flowState) {
         return var13.putAt_TaskStore;
      }

      boolean var17 = var15 || var0 instanceof AiPlannerWorld var18 && var18.fill();
      AiJumpManeuver var19 = simulateFlight(var0, var1, var2, var3, var4, var6, var7, var8, var9, var10, var11, var12, var13, var16, var14, var17);
      if (var16) {
         var13.putAt_TaskStore = var19;
      }

      return var19;
   }

   private static AiJumpManeuver simulateFlight(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      List<AiPhysics.MoveInput> var2,
      BlockPos var3,
      double var4,
      AiJumpManeuver.Inner_ConfigStore var6,
      float var7,
      boolean var8,
      int var9,
      int var10,
      int var11,
      int var12,
      AiJumpManeuver.Inner_BufferHelper var13,
      boolean var14,
      AiJumpManeuver.Inner_CachePool var15,
      boolean var16
   ) {
      AiPhysics.MotionState var17 = var6.state();
      double var18 = var6.peak();
      float var20 = var6.yaw();
      int var21 = 0;
      int var22 = var13 != null && !var14 ? Math.min(var10, var13.putAt_List.size()) : 0;
      if (var22 > 0) {
         AiJumpManeuver.Inner_DataUtil var23 = var13.putAt_List.get(var22 - 1);
         var17 = var23.state();
         var18 = var23.peak();
         var20 = var23.yaw();
      }

      if (var14) {
         var13.putAt_Z = var17.horizontalCollision() || var17.verticalCollision() && !var17.onGround();
         var13.prevEx = (var17.x() - var13.putAt_D) * var13.readEx + (var17.z() - var13.fill) * var13.value;
      }

      for (int var38 = var22 + 1; var38 <= 40; var38++) {
         if (var14) {
            var13.flowState = var38;
         }

         if (var38 > var9) {
            var20 += Mth.clamp(Mth.wrapDegrees(var7 - var20), -30.0F, 30.0F);
         }

         AiPhysics.MoveInput var24 = flightInput(var38, var20, var8, var10, var11, var12);
         AiPhysics.MotionState var25 = var15 != null && var38 <= 8 ? var15.putAt(var0, var17, var24, var4) : flightStep(var0, var17, var24, var4);
         if (var25 == null) {
            if (var14) {
               var13.putAt_Z = true;
            }

            return null;
         }

         if (var14) {
            if (var25.horizontalCollision() || var25.verticalCollision() && !var25.onGround()) {
               var13.putAt_Z = true;
            }

            double var26 = (var25.x() - var13.putAt_D) * var13.readEx + (var25.z() - var13.fill) * var13.value;
            if (var26 > var13.prevEx) {
               var13.prevEx = var26;
            }
         }

         if (!var25.onGround() && !var25.verticalCollision() && var25.vy() <= 0.0 && var25.y() < var4 - 0.02
            || var16 && var25.vy() < 0.0 && var25.y() < var4 - 0.6 - 0.01) {
            return null;
         }

         if (var38 >= 3 && !var25.onGround() && var25.vy() < -0.15) {
            double var39 = Math.hypot(var3.getX() + 0.5 - var25.x(), var3.getZ() + 0.5 - var25.z());
            if (var39 > 1.35) {
               double var28 = var25.y();
               double var30 = var25.vy();
               double var32 = Math.hypot(var25.vx(), var25.vz());
               double var34 = 0.0;
               int var36 = 40 - var38;

               for (int var37 = 0; var37 < var36 && var28 >= var4 - 0.02; var37++) {
                  var32 = var32 * 0.91 + 0.03;
                  var34 += var32;
                  var30 = (var30 - 0.08) * 0.98;
                  var28 += var30;
               }

               if (var39 > var34 + 1.15) {
                  return null;
               }
            }
         }

         var17 = var25;
         var18 = Math.max(var18, var17.y());
         if (!AiJumpSolver.putAt(var17)) {
            if (var14 && var13.putAt_List.size() == var38 - 1 && var38 <= 12) {
               var13.putAt_List.add(new AiJumpManeuver.Inner_DataUtil(var17, var20, var18));
            }
         } else {
            if (Math.abs(var17.y() - var4) > 0.02 || var18 - var17.y() > var0.putAt_D() + 1.0E-5) {
               return null;
            }

            double var40 = AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var0, var17, var3, var4, var20);
            if (!(var40 < -0.04)) {
               ArrayList var43 = new ArrayList(var2.size() + var6.controls().size() + var38 + 4);
               var43.addAll(var2);
               var43.addAll(var6.controls());
               float var29 = var6.yaw();

               for (int var44 = 1; var44 <= var38; var44++) {
                  if (var44 > var9) {
                     var29 += Mth.clamp(Mth.wrapDegrees(var7 - var29), -30.0F, 30.0F);
                  }

                  var43.add(flightInput(var44, var29, var8, var10, var11, var12));
               }

               if (AiJumpSolver.putAt_D_TokenCore_TaskStat(var0, var17.x(), var17.z(), var3, var4) < -0.04) {
                  AiPhysics.MoveInput var45 = new AiPhysics.MoveInput(0.0, 0.0, false, false, false, var20);
                  AiPhysics.MotionState var31 = var17;

                  for (int var48 = 1; var48 <= 6; var48++) {
                     AiPhysics.MotionState var33 = AiPhysics.step(var31, var45, var0);
                     if (!AiJumpSolver.putAt(var33) || Math.abs(var33.y() - var4) > 0.02 || AiPhysics.hitsStrictHazard(var0, var31, var33)) {
                        break;
                     }

                     var31 = var33;
                     if (AiJumpSolver.putAt_D_TokenCore_TaskStat(var0, var31.x(), var31.z(), var3, var4) >= -0.04
                        && AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var0, var31, var3, var4, var20) >= -0.04) {
                        for (int var51 = 0; var51 < var48; var51++) {
                           var43.add(var45);
                        }

                        var17 = var31;
                        break;
                     }
                  }
               }

               for (int var46 = 0; var46 < 10 && AiJumpSolver.putAt_D_TokenCore_TaskStat(var0, var17.x(), var17.z(), var3, var4) < -0.04; var46++) {
                  float var47 = AiPhysics.yawBetween(var17.x(), var17.z(), var3.getX() + 0.5, var3.getZ() + 0.5);
                  var20 += Mth.clamp(Mth.wrapDegrees(var47 - var20), -30.0F, 30.0F);
                  AiPhysics.MoveInput var49 = new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var20);
                  AiPhysics.MotionState var50 = AiPhysics.step(var17, var49, var0);
                  if (!AiJumpSolver.putAt(var50) || Math.abs(var50.y() - var4) > 0.02 || AiPhysics.hitsStrictHazard(var0, var17, var50) || AiJumpSolver.putAt(var0, var50)) {
                     return null;
                  }

                  var43.add(var49);
                  var17 = var50;
               }

               if (AiJumpSolver.putAt_D_TokenCore_TaskStat(var0, var17.x(), var17.z(), var3, var4) < -0.04) {
                  return null;
               }

               var40 = AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var0, var17, var3, var4, var20);
               if (var40 < -0.04) {
                  return null;
               }

               return new AiJumpManeuver(var43, var1, var17, var40, var2.size() + var6.controls().size() - 1);
            }

            double var42 = AiJumpSolver.putAt_D_TokenCore_TaskStat(var0, var17.x(), var17.z(), var3, var4);
            if (!(var42 >= -0.6) || ++var21 > 4) {
               return null;
            }
         }
      }

      return null;
   }

   private static AiPhysics.MoveInput flightInput(int var0, float var1, boolean var2, int var3, int var4, int var5) {
      boolean var6 = var0 <= var3;
      return new AiPhysics.MoveInput(var6 ? 1.0 : 0.0, var0 > var5 ? var4 : 0.0, false, var6 && var2, false, var1);
   }

   private static AiPhysics.MotionState flightStep(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MoveInput var2, double var3
   ) {
      AiPhysics.MotionState var5 = AiPhysics.step(var1, var2, var0);
      return !(var5.y() < var3 - 4.0) && !AiPhysics.hitsStrictHazard(var0, var1, var5) && !AiJumpSolver.putAt(var0, var5) ? var5 : null;
   }

   private record Inner_ConfigStore(AiPhysics.MotionState state, List<AiPhysics.MoveInput> controls, double peak, float yaw) {
   }

   private record Inner_DataUtil(AiPhysics.MotionState state, float yaw, double peak) {
   }

   private static final class Inner_CachePool {
      private final AiPhysics.MotionState[] putAt_TokenCore_TableCacArr;
      private final AiPhysics.MotionState[] fill;
      private final long[] putAt_JArr;
      private final int[] putAt_IArr;
      private int flowState = 1;

      Inner_CachePool() {
         this(2048);
      }

      Inner_CachePool(int var1) {
         this.putAt_TokenCore_TableCacArr = new AiPhysics.MotionState[var1];
         this.fill = new AiPhysics.MotionState[var1];
         this.putAt_JArr = new long[var1];
         this.putAt_IArr = new int[var1];
      }

      void putAt() {
         if (++this.flowState == 0) {
            Arrays.fill(this.putAt_IArr, 0);
            this.flowState = 1;
         }
      }

      AiPhysics.MotionState putAt(AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, AiPhysics.MoveInput var3, double var4) {
         long var6 = (long)Float.floatToIntBits(var3.yaw()) << 8 | (long)(var3.strafe() + 1.0) << 2 | (var3.forward() > 0.0 ? 2 : 0) | (var3.sprint() ? 1 : 0);
         long var8 = Double.doubleToRawLongBits(var2.x())
            ^ Long.rotateLeft(Double.doubleToRawLongBits(var2.y()), 19)
            ^ Long.rotateLeft(Double.doubleToRawLongBits(var2.z()), 41);
         long var10 = Double.doubleToRawLongBits(var2.vx()) ^ Long.rotateLeft(Double.doubleToRawLongBits(var2.vz()), 29);
         int var12 = (int)HashCommon.mix(var6 ^ var8 ^ var10) & this.putAt_TokenCore_TableCacArr.length - 1;
         if (this.putAt_IArr[var12] == this.flowState && this.putAt_TokenCore_TableCacArr[var12] == var2 && this.putAt_JArr[var12] == var6) {
            return this.fill[var12];
         }

         AiPhysics.MotionState var13 = AiJumpManeuver.flightStep(var1, var2, var3, var4);
         this.putAt_TokenCore_TableCacArr[var12] = var2;
         this.fill[var12] = var13;
         this.putAt_JArr[var12] = var6;
         this.putAt_IArr[var12] = this.flowState;
         return var13;
      }
   }

   private record Inner_TaskQueue(AiPhysics.MotionState state, List<AiPhysics.MoveInput> prefix, double distance) {
   }

   private record Inner_StreamUtil(
      AiPhysics.MotionState state, AiPhysics.MoveInput input, AiJumpManeuver.Inner_StreamUtil parent, boolean cleared, double distance
   ) {
   }

   private static final class Inner_BufferHelper {
      final List<AiJumpManeuver.Inner_DataUtil> putAt_List = new ArrayList<>(12);
      double putAt_D;
      double fill;
      double readEx;
      double value;
      int flowState;
      boolean putAt_Z;
      double prevEx = -1.0 / 0.0;
      AiJumpManeuver putAt_TaskStore;

      Inner_BufferHelper() {
         this(0.0, 0.0, 0.0, 0.0);
      }

      Inner_BufferHelper(double var1, double var3, double var5, double var7) {
         this.putAt_D = var1;
         this.fill = var3;
         this.readEx = var5;
         this.value = var7;
      }

      void putAt(double var1, double var3, double var5, double var7) {
         this.putAt_List.clear();
         this.putAt_D = var1;
         this.fill = var3;
         this.readEx = var5;
         this.value = var7;
         this.flowState = 0;
         this.putAt_Z = false;
         this.prevEx = -1.0 / 0.0;
         this.putAt_TaskStore = null;
      }
   }
}
