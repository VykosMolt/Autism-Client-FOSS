package autismclient.ai.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;

public final class AiWallJump {
   private static final int flowState = 12;
   private static final int fill = 96;
   private static final double putAt_D = 0.8;

   private AiWallJump() {
   }

   static AiJumpSolver.JumpPlan planWallJump(AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, List<BlockPos> var4) {
      return planWithDefaults(var0, AiPhysics.MotionState.standing(var1.getX() + 0.5, var2, var1.getZ() + 0.5), var1, var4);
   }

   static AiJumpSolver.JumpPlan planWithDefaults(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, List<BlockPos> var3) {
      return planVariants(var0, var1, var2, var3, false, 0.0F / 0.0F, 180.0F);
   }

   static AiJumpSolver.JumpPlan planWallJump(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, List<BlockPos> var3, float var4, float var5
   ) {
      return planVariants(var0, var1, var2, var3, false, var4, var5);
   }

   static AiJumpSolver.JumpPlan planWallJumpBetween(
      AiPhysics.PhysicsWorld var0, BlockPos var1, double var2, BlockPos var4, double var5
   ) {
      AiJumpSolver.JumpPlan var7 = planVariants(
         var0, AiPhysics.MotionState.standing(var1.getX() + 0.5, var2, var1.getZ() + 0.5), var1, List.of(var4), true, 0.0F / 0.0F, 180.0F
      );
      if (var7 == null && !(var5 < 0.1)) {
         int var8 = Integer.signum(var4.getX() - var1.getX());
         int var9 = Integer.signum(var4.getZ() - var1.getZ());
         double var10 = 0.27;
         var5 = Math.min(var5, 0.153);
         AiPhysics.MotionState var12 = new AiPhysics.MotionState(
            var1.getX() + 0.5 - var8 * var10, var2, var1.getZ() + 0.5 - var9 * var10, var8 * var5, -0.0784, var9 * var5, true, 0, false, false
         );
         return !AiJumpSolver.canStandAt(var0, var12.x(), var2, var12.z())
            ? null
            : planVariants(var0, var12, var1, List.of(var4), true, 0.0F / 0.0F, 180.0F);
      } else {
         return var7;
      }
   }

   private static AiJumpSolver.JumpPlan planVariants(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, List<BlockPos> var3, boolean var4, float var5, float var6
   ) {
      if (var4) {
         AiJumpSolver.JumpPlan var13 = searchSides(var0, var1, var2, var3, true, false, false, 0.0F / 0.0F, 180.0F);
         if (var13 != null && !passesPaceChecks(var0, var13)) {
            AiJumpSolver.JumpPlan var14 = searchSides(var0, var1, var2, var3, true, true, false, 0.0F / 0.0F, 180.0F);
            return var14 != null ? var14 : var13;
         } else {
            return var13;
         }
      } else {
         AiJumpSolver.JumpPlan var7 = searchSides(var0, var1, var2, var3, false, true, false, var5, var6);
         if (var7 != null) {
            return var7;
         }

         double var8 = Math.hypot(var1.x() - (var2.getX() + 0.5), var1.z() - (var2.getZ() + 0.5));
         if (var8 <= 0.015) {
            AiJumpSolver.JumpPlan var15 = searchSides(var0, var1, var2, var3, false, false, false, var5, var6);
            if (var15 != null) {
               return var15;
            }

            AiJumpSolver.JumpPlan var17 = searchSides(var0, var1, var2, var3, false, true, true, var5, var6);
            return var17 != null ? var17 : searchSides(var0, var1, var2, var3, false, false, true, var5, var6);
         } else {
            AiJumpSolver.JumpPlan var10 = searchSides(var0, var1, var2, var3, false, true, true, var5, var6);
            if (var10 != null) {
               return var10;
            }

            if (Float.isNaN(var5)) {
               AiJumpSolver.JumpPlan var11 = planFromStanding(var0, var1, var2, var3, true);
               if (var11 != null) {
                  return var11;
               }
            }

            AiJumpSolver.JumpPlan var16 = searchSides(var0, var1, var2, var3, false, false, false, var5, var6);
            if (var16 != null) {
               return var16;
            } else {
               AiJumpSolver.JumpPlan var12 = searchSides(var0, var1, var2, var3, false, false, true, var5, var6);
               if (var12 != null) {
                  return var12;
               } else {
                  return Float.isNaN(var5) ? planFromStanding(var0, var1, var2, var3, false) : null;
               }
            }
         }
      }
   }

   private static boolean passesPaceChecks(AiPhysics.PhysicsWorld var0, AiJumpSolver.JumpPlan var1) {
      if (var1 != null && var1.maneuver() != null) {
         AiPhysics.MotionState var2 = var1.maneuver().start();

         for (AiPhysics.MoveInput var4 : var1.maneuver().inputs()) {
            AiPhysics.MotionState var5 = AiPhysics.step(var2, var4, var0);
            if (AiPaceScript.value(var0, var5) || AiPaceScript.readEx(var0, var2, var5) || AiPaceScript.value(var0, var2, var5)) {
               return false;
            }

            var2 = var5;
         }

         return true;
      } else {
         return true;
      }
   }

   private static AiJumpSolver.JumpPlan searchSidesDefault(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, List<BlockPos> var3, boolean var4, boolean var5, boolean var6
   ) {
      return searchSides(var0, var1, var2, var3, var4, var5, var6, 0.0F / 0.0F, 180.0F);
   }

   private static AiJumpSolver.JumpPlan searchSides(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      BlockPos var2,
      List<BlockPos> var3,
      boolean var4,
      boolean var5,
      boolean var6,
      float var7,
      float var8
   ) {
      BlockPos var9 = (BlockPos)var3.getLast();
      double var10 = Integer.signum(var9.getX() - var2.getX());
      double var12 = Integer.signum(var9.getZ() - var2.getZ());
      float var14 = AiPhysics.yawBetween(0.0, 0.0, var10, var12);
      AiJumpSolver.JumpPlan var15 = null;

      for (int var19 : new int[]{-1, 1}) {
         AiWallJump.WallFrame var20 = new AiWallJump.WallFrame(var2.getX() + 0.5, var2.getZ() + 0.5, var10, var12, var14, var19);
         List var21 = List.of(new AiWallJump.SearchNode(var1, null, null, 0.0));
         int var22 = 0;

         for (BlockPos var24 : var3) {
            var22++;
            boolean var25 = var22 == var3.size();
            var21 = expandCell(var0, var1.y(), var20, var21, var24, var22, var25, var4, var5, var6, var22 == 1 ? var7 : 0.0F / 0.0F, var8);
            if (var21.isEmpty()) {
               break;
            }

            if (var25) {
               AiJumpSolver.JumpPlan var26 = toJumpPlan(var0, var1, (AiWallJump.SearchNode)var21.getFirst(), var24, var14);
               if (var15 == null || var26.ticks() < var15.ticks() || var26.ticks() == var15.ticks() && var26.margin() > var15.margin()) {
                  var15 = var26;
               }
            }
         }
      }

      return var15;
   }

   private static List<AiWallJump.SearchNode> expandCell(
      AiPhysics.PhysicsWorld var0,
      double var1,
      AiWallJump.WallFrame var3,
      List<AiWallJump.SearchNode> var4,
      BlockPos var5,
      int var6,
      boolean var7,
      boolean var8,
      boolean var9,
      boolean var10,
      float var11,
      float var12
   ) {
      double var13 = (var5.getX() + 0.5 - var3.x()) * var3.ux() + (var5.getZ() + 0.5 - var3.z()) * var3.uz();
      ArrayList var15 = new ArrayList();
      boolean var16 = var0.movementSpeed() > 0.125;
      boolean[] var17 = var16 ? new boolean[]{true, false} : new boolean[]{true};
      boolean var18 = var8 && var1 != Math.floor(var1);

      for (AiWallJump.SearchNode var20 : var4) {
         addJumpsWithin(var0, var1, var3, var20, var15, var9, !Float.isNaN(var11), var10 && var6 == 1, var18, var11, var12);
         if (var8) {
            expandWallApproach(var0, var1, var3, var20, var15, var9);
         } else if (var6 == 1) {
            float[] var56 = var10
               ? new float[]{-30.0F, -24.0F, -18.0F, -12.0F, -6.0F, 0.0F, 6.0F, 12.0F, 18.0F, 24.0F, 30.0F}
               : new float[]{0.0F, -12.0F, -24.0F, 12.0F};
            int var58 = var10 ? 6 : 5;

            for (boolean var66 : var17) {
               for (float var77 : var56) {
                  float var79 = var3.yaw() + var77 * var3.side();
                  float var81 = var11;
                  AiWallJump.SearchNode var83 = var20;

                  for (int var85 = 0; var85 < var58; var85++) {
                     var81 = Float.isNaN(var81) ? var79 : var81 + Math.clamp(wrapDegrees(var79 - var81), -var12, var12);
                     AiPhysics.MoveInput var87 = AiPhysics.MoveInput.running(var81, var66);
                     var83 = putAt_RingCodec_TableCac_TokenCore_TaskStat(var0, var1, var83, var87, var9);
                     if (var83 == null || !var83.body().onGround()) {
                        break;
                     }

                     addJumpsWithin(var0, var1, var3, var83, var15, var9, true, false, false, var81, Math.min(30.0F, var12));
                  }
               }
            }
         } else {
            int[] var21 = var16 ? new int[]{1, 0} : new int[]{1};
            int[] var22 = var10 ? new int[]{var3.side(), 0, -var3.side()} : new int[]{var3.side(), 0};

            for (int var26 : var21) {
               for (boolean var30 : var26 > 0 ? var17 : new boolean[]{false}) {
                  for (int var34 : var22) {
                     AiPhysics.MoveInput var35 = new AiPhysics.MoveInput(var26, var34, false, var30, false, var3.yaw());
                     AiWallJump.SearchNode var36 = putAt_RingCodec_TableCac_TokenCore_TaskStat(var0, var1, var20, var35, var9);
                     if (var36 != null && var36.body().onGround()) {
                        addJumpsAnyYaw(var0, var1, var3, var36, var15, var9, false, var10, false);
                        if (var10) {
                           AiWallJump.SearchNode var37 = putAt_RingCodec_TableCac_TokenCore_TaskStat(var0, var1, var36, var35, var9);
                           if (var37 != null && var37.body().onGround()) {
                              addJumpsAnyYaw(var0, var1, var3, var37, var15, var9, false, true, false);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      List var54 = var15;
      ArrayList<AiWallJump.SearchNode> var55 = new ArrayList<>();

      for (int var57 = 1; var57 <= 18 && !var54.isEmpty(); var57++) {
         if (Thread.currentThread().isInterrupted()) {
            return List.of();
         }

         ArrayList<AiWallJump.SearchNode> var59 = new ArrayList<>();

         for (AiWallJump.SearchNode var63 : (Iterable<AiWallJump.SearchNode>)(Iterable<?>)(var54)) {
            AiPhysics.MotionState var65 = var63.body();
            if (var65.onGround()) {
               if (Math.abs(var3.along(var65) - var13) > 1.0) {
                  continue;
               }

               if (var7) {
                  double var67 = AiJumpSolver.settledLandingMargin(var0, var65, var5, var1, var3.yaw());
                  if (var67 >= 0.01) {
                     var55.add(new AiWallJump.SearchNode(var65, var63.input(), var63.parent(), -var67));
                     continue;
                  }
               } else {
                  double var68 = wallAlignCost(var3, var13, var65, var9);
                  double var73 = var10 && var6 == 1 ? 0.5 : 0.6;
                  if (var3.advance(var65) >= 0.17 && var3.across(var65) >= var73) {
                     var55.add(new AiWallJump.SearchNode(var65, var63.input(), var63.parent(), var68));
                     continue;
                  }

                  if (var57 >= 13) {
                     continue;
                  }
               }
            }

            boolean var69 = var18 && var7 && var65.onGround();

            for (int var78 : new int[]{1, 0, -1}) {
               for (int var80 = -1; var80 <= 1; var80++) {
                  for (boolean var88 : var69 ? new boolean[]{true, false} : new boolean[]{false}) {
                     AiPhysics.MoveInput var89 = new AiPhysics.MoveInput(var78, var80, false, var78 > 0 && !var69 && !var88, var88, var3.yaw());
                     AiWallJump.SearchNode var90 = putAt_RingCodec_TableCac_TokenCore_TaskStat(var0, var1, var63, var89, var9);
                     if (var90 != null && (var90.body().onGround() || var57 < 12)) {
                        AiPhysics.MotionState var38 = var90.body();
                        double var39 = var3.along(var38);
                        double var41 = var3.across(var38);
                        double var43 = var3.advance(var38);
                        int var45 = Math.max(0, 12 - var57 - 1);
                        double var46 = var39 + var45 * (0.55 * var43 + 0.125);
                        double var48 = Math.abs(var13 + 0.21 - var46);
                        double var50 = var7 && var45 <= 3 ? 0.5700000000000001 : 0.74;
                        if (var6 > 1) {
                           var48 = 3.0 * Math.max(0.0, var13 + 0.2 - var46) + 0.3 * Math.max(0.0, var46 - var13 - 0.2);
                           var50 = var45 <= 1 ? (var9 && !var7 ? 0.808 : 0.788) : 0.8200000000000001;
                        }

                        if (var8) {
                           if (var18) {
                              var48 = Math.abs(var13 - var46);
                              var50 = var39 >= var13 - 1.0 ? 0.0 : 0.58;
                           } else {
                              var48 = 3.0 * Math.max(0.0, var13 + 0.2 - var46) + 0.3 * Math.max(0.0, var46 - var13 - 0.2);
                              var50 = var39 >= var13 - 1.0 ? 0.53 : 0.8200000000000001;
                           }
                        }

                        if (var7 && var38.onGround()) {
                           var48 = Math.abs((var18 ? var13 : var13 + 0.1) - var39 - 1.2 * var43);
                           var50 = var18 ? 0.0 : 0.16;
                        }

                        double var52 = var48
                           + 1.5 * Math.abs(var41 + 1.25 * var3.drift(var38) - var50)
                           + (var18 && var7 && var38.onGround() ? 0.8 * var38.speed() : -0.5 * var43);
                        if (!var7 && var45 <= 1) {
                           if (!var38.onGround() && var9) {
                              var52 = 1.7 * Math.abs(var39 + var43 - var13 - 0.14)
                                 + 2.5 * Math.abs(var41 - 0.79)
                                 + 2.5 * Math.abs(var41 + var3.drift(var38) - 0.812)
                                 - 2.0 * Math.min(var43, 0.3);
                           } else {
                              var52 = wallAlignCost(var3, var13, var38, var9);
                           }
                        }

                        var59.add(new AiWallJump.SearchNode(var38, var89, var63, var52));
                     }
                  }
               }
            }
         }

         if (var7 && !var55.isEmpty()) {
            break;
         }

         var54 = topCandidates(var59, var10 ? 192 : 96);
      }

      return topCandidates(var55, var7 ? 1 : (var10 ? 80 : 40));
   }

   private static void expandRunUp(
      AiPhysics.PhysicsWorld var0,
      double var1,
      AiWallJump.WallFrame var3,
      double var4,
      AiWallJump.SearchNode var6,
      List<AiWallJump.SearchNode> var7,
      boolean[] var8,
      boolean var9
   ) {
      float[] var10 = new float[]{0.0F, -12.0F, -24.0F, -36.0F, 12.0F, 24.0F, 36.0F};
      List var11 = List.of(var6);

      for (int var12 = 0; var12 < 7 && !var11.isEmpty(); var12++) {
         ArrayList<AiWallJump.SearchNode> var13 = new ArrayList<>();

         for (AiWallJump.SearchNode var15 : (Iterable<AiWallJump.SearchNode>)(Iterable<?>)(var11)) {
            float var16 = var15.input() != null ? var15.input().yaw() : 0.0F / 0.0F;

            for (boolean var20 : var8) {
               for (float var24 : var10) {
                  float var25 = var3.yaw() + var24 * var3.side();
                  if (Float.isNaN(var16) || !(Math.abs(wrapDegrees(var25 - var16)) > 30.01F)) {
                     AiPhysics.MoveInput var26 = AiPhysics.MoveInput.running(var25, var20);
                     AiWallJump.SearchNode var27 = putAt_RingCodec_TableCac_TokenCore_TaskStat(var0, var1, var15, var26, var9);
                     if (var27 != null && var27.body().onGround()) {
                        AiPhysics.MotionState var28 = var27.body();
                        double var29 = var3.along(var28);
                        double var31 = var3.across(var28);
                        double var33 = var3.advance(var28);
                        double var35 = 0.18;
                        double var37 = 0.4 * Math.abs(var4 - 1.2 - var29) + 1.8 * Math.abs(var31 - var35) - 1.2 * var33;
                        var13.add(new AiWallJump.SearchNode(var28, var26, var15, var37));
                        addJumpsFollowing(var0, var1, var3, var27, var7, var9, true);
                     }
                  }
               }
            }
         }

         var11 = topCandidates(var13, 20);
      }
   }

   private static void expandWallApproach(
      AiPhysics.PhysicsWorld var0,
      double var1,
      AiWallJump.WallFrame var3,
      AiWallJump.SearchNode var4,
      List<AiWallJump.SearchNode> var5,
      boolean var6
   ) {
      List var7 = List.of(var4);
      boolean var8 = var1 != Math.floor(var1);
      boolean[] var9 = var8 ? new boolean[]{true, false} : new boolean[]{true};

      for (int var10 = 0; var10 < 7 && !var7.isEmpty(); var10++) {
         ArrayList<AiWallJump.SearchNode> var11 = new ArrayList<>();

         for (AiWallJump.SearchNode var13 : (Iterable<AiWallJump.SearchNode>)(Iterable<?>)(var7)) {
            for (boolean var17 : var9) {
               for (float var21 : new float[]{0.0F, -30.0F, -45.0F, -60.0F, -80.0F, -100.0F, -120.0F, 30.0F}) {
                  AiPhysics.MoveInput var22 = AiPhysics.MoveInput.running(var3.yaw() + var21 * var3.side(), var17);
                  AiWallJump.SearchNode var23 = putAt_RingCodec_TableCac_TokenCore_TaskStat(var0, var1, var13, var22, var6);
                  if (var23 != null && var23.body().onGround()) {
                     AiPhysics.MotionState var24 = var23.body();
                     double var25 = var3.advance(var24);
                     double var27 = Math.abs(0.15 - var3.along(var24) - 0.7 * var25)
                        + 1.7 * Math.abs(0.67 - var3.across(var24) - 0.7 * var3.drift(var24))
                        - 0.3 * var25;
                     var11.add(new AiWallJump.SearchNode(var24, var22, var13, var27));
                     addJumpsAnyYaw(var0, var1, var3, var23, var5, var6, false, false, var8);
                  }
               }
            }
         }

         var7 = topCandidates(var11, 24);
      }
   }

   private static double wallAlignCost(AiWallJump.WallFrame var0, double var1, AiPhysics.MotionState var3, boolean var4) {
      double var5 = var0.across(var3);
      double var7 = var4 ? 8.0 * Math.max(0.0, 0.8045 - var5) : 0.0;
      return 1.7 * Math.abs(var0.along(var3) - var1 - 0.14)
         + 2.5 * Math.abs(var5 - (var4 ? 0.81 : 0.8300000000000001))
         + var7
         - 2.0 * Math.min(var0.advance(var3), 0.3)
         + 0.5 * Math.abs(var0.drift(var3));
   }

   private static void addJumpsPaced(
      AiPhysics.PhysicsWorld var0,
      double var1,
      AiWallJump.WallFrame var3,
      AiWallJump.SearchNode var4,
      List<AiWallJump.SearchNode> var5,
      boolean var6
   ) {
      addJumpsAnyYaw(var0, var1, var3, var4, var5, var6, false, false, false);
   }

   private static void addJumpsFollowing(
      AiPhysics.PhysicsWorld var0,
      double var1,
      AiWallJump.WallFrame var3,
      AiWallJump.SearchNode var4,
      List<AiWallJump.SearchNode> var5,
      boolean var6,
      boolean var7
   ) {
      addJumpsAnyYaw(var0, var1, var3, var4, var5, var6, var7, false, false);
   }

   private static void addJumpsFine(
      AiPhysics.PhysicsWorld var0,
      double var1,
      AiWallJump.WallFrame var3,
      AiWallJump.SearchNode var4,
      List<AiWallJump.SearchNode> var5,
      boolean var6,
      boolean var7,
      boolean var8
   ) {
      addJumpsAnyYaw(var0, var1, var3, var4, var5, var6, var7, var8, false);
   }

   private static void addJumpsAnyYaw(
      AiPhysics.PhysicsWorld var0,
      double var1,
      AiWallJump.WallFrame var3,
      AiWallJump.SearchNode var4,
      List<AiWallJump.SearchNode> var5,
      boolean var6,
      boolean var7,
      boolean var8,
      boolean var9
   ) {
      addJumpsWithin(var0, var1, var3, var4, var5, var6, var7, var8, var9, 0.0F / 0.0F, 30.0F);
   }

   private static void addJumpsWithin(
      AiPhysics.PhysicsWorld var0,
      double var1,
      AiWallJump.WallFrame var3,
      AiWallJump.SearchNode var4,
      List<AiWallJump.SearchNode> var5,
      boolean var6,
      boolean var7,
      boolean var8,
      boolean var9,
      float var10,
      float var11
   ) {
      boolean var12 = var0.movementSpeed() > 0.125;
      float[] var13 = var8
         ? new float[]{0.0F, -4.0F, -8.0F, -12.0F, -16.0F, -20.0F, -24.0F, -28.0F, -30.0F, 4.0F, 8.0F, 12.0F, 16.0F, 20.0F, 24.0F, 28.0F}
         : (
            !var12 && !var9
               ? new float[]{0.0F, -7.0F, -14.0F, -21.0F, 7.0F, 14.0F, -30.0F}
               : new float[]{0.0F, -7.0F, -14.0F, -21.0F, 7.0F, 14.0F, -30.0F, -45.0F, -60.0F}
         );
      float var14 = !Float.isNaN(var10) ? var10 : (var7 && var4.input() != null ? var4.input().yaw() : 0.0F / 0.0F);
      float var15 = !Float.isNaN(var10) ? var11 + 0.01F : 30.01F;

      for (float var19 : var13) {
         float var20 = var3.yaw() + var19 * var3.side();
         if (Float.isNaN(var14) || !(Math.abs(wrapDegrees(var20 - var14)) > var15)) {
            AiPhysics.MoveInput var21 = AiPhysics.MoveInput.jump(var20, true);
            AiWallJump.SearchNode var22 = putAt_RingCodec_TableCac_TokenCore_TaskStat(var0, var1, var4, var21, var6);
            if (var22 != null && !var22.body().onGround() && var22.body().y() > var1 + 0.2) {
               var5.add(var22);
            }

            if (var12 || var9) {
               AiPhysics.MoveInput var23 = AiPhysics.MoveInput.jump(var20, false);
               AiWallJump.SearchNode var24 = putAt_RingCodec_TableCac_TokenCore_TaskStat(var0, var1, var4, var23, var6);
               if (var24 != null && !var24.body().onGround() && var24.body().y() > var1 + 0.2) {
                  var5.add(var24);
               }
            }
         }
      }
   }

   private static AiWallJump.SearchNode putAt_RingCodec_TableCac_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0, double var1, AiWallJump.SearchNode var3, AiPhysics.MoveInput var4, boolean var5
   ) {
      AiPhysics.MotionState var6 = var3.body();
      AiPhysics.MotionState var7 = AiPhysics.step(var6, var4, var0);
      if (!var7.horizontalCollision()
         && !(var7.y() < var1 - 0.02)
         && (var4.sneak() || var7.pose() == 0)
         && !AiPhysics.hitsStrictHazard(var0, var6, var7)
         && !AiJumpSolver.isInFluid(var0, var7)) {
         return !var5 || !AiPaceScript.value(var0, var7) && !AiPaceScript.readEx(var0, var6, var7) && !AiPaceScript.value(var0, var6, var7)
            ? new AiWallJump.SearchNode(var7, var4, var3, 0.0)
            : null;
      } else {
         return null;
      }
   }

   private static List<AiWallJump.SearchNode> topCandidates(List<AiWallJump.SearchNode> var0, int var1) {
      var0.sort(Comparator.comparingDouble(AiWallJump.SearchNode::score));
      ArrayList var2 = new ArrayList(var1);
      HashSet<Long> var3 = new HashSet<>();

      for (AiWallJump.SearchNode var5 : var0) {
         AiPhysics.MotionState var6 = var5.body();
         long var7 = (Math.round(var6.x() * 64.0) & 2047L) << 23
            | (Math.round(var6.z() * 64.0) & 2047L) << 12
            | (Math.round(var6.vx() * 96.0) & 63L) << 6
            | Math.round(var6.vz() * 96.0) & 63L;
         if (var3.add(var7)) {
            var2.add(var5);
            if (var2.size() >= var1) {
               break;
            }
         }
      }

      return var2;
   }

   private static float wrapDegrees(float var0) {
      float var1 = var0 % 360.0F;
      if (var1 >= 180.0F) {
         var1 -= 360.0F;
      }

      if (var1 < -180.0F) {
         var1 += 360.0F;
      }

      return var1;
   }

   private static AiJumpSolver.JumpPlan planFromStanding(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, List<BlockPos> var3, boolean var4
   ) {
      if (var1.onGround() && var1.pose() == 0) {
         double var5 = var2.getX() + 0.5;
         double var7 = var2.getZ() + 0.5;
         if (!AiJumpSolver.canStandAt(var0, var5, var1.y(), var7)) {
            return null;
         }

         BlockPos var9 = (BlockPos)var3.getLast();
         double var10 = Integer.signum(var9.getX() - var2.getX());
         double var12 = Integer.signum(var9.getZ() - var2.getZ());
         float var14 = Math.hypot(var1.x() - var5, var1.z() - var7) > 0.012
            ? AiPhysics.yawBetween(var1.x(), var1.z(), var5, var7)
            : AiPhysics.yawBetween(0.0, 0.0, -var12, var10);
         AiPhysics.MoveInput[] var15 = new AiPhysics.MoveInput[]{
            AiPhysics.MoveInput.NONE.withYaw(var14),
            new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var14),
            new AiPhysics.MoveInput(-1.0, 0.0, false, false, false, var14),
            new AiPhysics.MoveInput(1.0, 0.0, false, false, true, var14),
            new AiPhysics.MoveInput(-1.0, 0.0, false, false, true, var14),
            new AiPhysics.MoveInput(0.0, 1.0, false, false, true, var14),
            new AiPhysics.MoveInput(0.0, -1.0, false, false, true, var14)
         };
         List var16 = List.of(new AiWallJump.SearchNode(var1, null, null, Math.hypot(var1.x() - var5, var1.z() - var7)));
         ArrayList<AiWallJump.SearchNode> var17 = new ArrayList<>(16);
         HashSet<Long> var18 = new HashSet<>();

         for (int var19 = 0; var19 < 24 && var17.size() < 14 && !var16.isEmpty(); var19++) {
            ArrayList<AiWallJump.SearchNode> var20 = new ArrayList<>();

            for (AiWallJump.SearchNode var22 : (Iterable<AiWallJump.SearchNode>)(Iterable<?>)(var16)) {
               AiPhysics.MotionState var23 = var22.body();

               for (AiPhysics.MoveInput var27 : var15) {
                  AiPhysics.MotionState var28 = AiPhysics.step(var23, var27, var0);
                  if (var28.onGround()
                     && !(Math.abs(var28.y() - var1.y()) > 0.02)
                     && !var28.horizontalCollision()
                     && !AiPhysics.hitsStrictHazard(var0, var23, var28)
                     && !AiJumpSolver.isInFluid(var0, var28)
                     && (!var4 || !AiPaceScript.value(var0, var28) && !AiPaceScript.readEx(var0, var23, var28) && !AiPaceScript.value(var0, var23, var28))) {
                     double var29 = Math.hypot(var28.x() - var5, var28.z() - var7);
                     double var31 = var28.speed();
                     double var33 = Math.hypot(var28.x() + var28.vx() * 1.2026 - var5, var28.z() + var28.vz() * 1.2026 - var7);
                     AiWallJump.SearchNode var35 = new AiWallJump.SearchNode(
                        var28, var27, var22, var33 + 0.12 * var31 + (var28.pose() == 0 ? 0.0 : 0.004)
                     );
                     if (var29 <= 0.035 && var31 <= 0.005 && var28.pose() == 0) {
                        long var36 = (Math.round((var28.x() - var5) * 200.0) & 1023L) << 10 | Math.round((var28.z() - var7) * 200.0) & 1023L;
                        if (var18.add(var36)) {
                           var17.add(var35);
                        }
                     }

                     var20.add(var35);
                  }
               }
            }

            var20.sort(Comparator.comparingDouble(AiWallJump.SearchNode::score));
            HashSet<Long> var41 = new HashSet<>();
            ArrayList<AiWallJump.SearchNode> var43 = new ArrayList<>(32);
            var var45 = var20.iterator();

            while (true) {
               if (var45.hasNext()) {
                  AiWallJump.SearchNode var47 = (AiWallJump.SearchNode)var45.next();
                  AiPhysics.MotionState var49 = var47.body();
                  long var51 = (Math.round((var49.x() - var5) * 250.0) & 2047L) << 33
                     | (Math.round((var49.z() - var7) * 250.0) & 2047L) << 22
                     | (Math.round(var49.vx() * 200.0) & 1023L) << 12
                     | (Math.round(var49.vz() * 200.0) & 1023L) << 2
                     | var49.pose() & 3L;
                  if (!var41.add(var51)) {
                     continue;
                  }

                  var43.add(var47);
                  if (var43.size() < 32) {
                     continue;
                  }
               }

               var16 = var43;
               break;
            }
         }

         if (var17.isEmpty()) {
            return null;
         }

         var17.sort(Comparator.comparingDouble(var4x -> Math.hypot(var4x.body().x() - var5, var4x.body().z() - var7)));

         for (int var39 = 0; var39 < 2; var39++) {
            boolean var40 = var39 == 1;
            int var42 = var40 ? Math.min(var17.size(), 2) : Math.min(var17.size(), 8);

            for (int var44 = 0; var44 < var42; var44++) {
               AiWallJump.SearchNode var46 = (AiWallJump.SearchNode)var17.get(var44);
               ArrayList<AiPhysics.MoveInput> var48 = new ArrayList<>(28);
               ArrayDeque var50 = new ArrayDeque();

               for (AiWallJump.SearchNode var52 = var46; var52.input() != null; var52 = var52.parent()) {
                  var50.addFirst(var52.input());
               }

               var48.addAll(var50);
               AiPhysics.MotionState var53 = var46.body();
               boolean var54 = true;

               for (int var55 = 0; var55 < 5 && var53.speed() > 1.0E-4; var55++) {
                  AiPhysics.MoveInput var57 = AiPhysics.MoveInput.NONE.withYaw(var14);
                  AiPhysics.MotionState var30 = AiPhysics.step(var53, var57, var0);
                  if (!var30.onGround() || Math.abs(var30.y() - var1.y()) > 0.02) {
                     var54 = false;
                     break;
                  }

                  var48.add(var57);
                  var53 = var30;
               }

               if (var54 && var53.pose() == 0) {
                  AiJumpSolver.JumpPlan var56 = searchSidesDefault(var0, var53, var2, var3, false, var4, var40);
                  if (var56 != null && var56.maneuver() != null) {
                     var var58 = var56.maneuver().inputs();
                     if (!var58.isEmpty()) {
                        float var59 = ((AiPhysics.MoveInput)var58.getFirst()).yaw();
                        float var60 = var14;

                        for (int var32 = 0; var32 < 8 && Math.abs(wrapDegrees(var59 - var60)) > 30.0F; var32++) {
                           var60 += Math.clamp(wrapDegrees(var59 - var60), -30.0F, 30.0F);
                           AiPhysics.MoveInput var62 = AiPhysics.MoveInput.NONE.withYaw(var60);
                           AiPhysics.MotionState var34 = AiPhysics.step(var53, var62, var0);
                           if (!var34.onGround() || Math.abs(var34.y() - var1.y()) > 0.02) {
                              var54 = false;
                              break;
                           }

                           var48.add(var62);
                           var53 = var34;
                        }

                        if (var54) {
                           ArrayList var61 = new ArrayList(var48.size() + var58.size());
                           var61.addAll(var48);
                           var61.addAll(var58);
                           AiPhysics.MotionState var63 = var1;
                           int var64 = var48.size();
                           int var65 = 0;

                           while (var65 < var61.size()) {
                              AiPhysics.MoveInput var67 = (AiPhysics.MoveInput)var61.get(var65);
                              AiPhysics.MotionState var37 = AiPhysics.step(var63, var67, var0);
                              if (!var37.horizontalCollision()
                                 && !(var37.y() < var1.y() - 0.02)
                                 && (var65 < var64 || var37.pose() == 0)
                                 && !AiPhysics.hitsStrictHazard(var0, var63, var37)
                                 && !AiJumpSolver.isInFluid(var0, var37)) {
                                 if (!var4 || !AiPaceScript.value(var0, var37) && !AiPaceScript.readEx(var0, var63, var37) && !AiPaceScript.value(var0, var63, var37)) {
                                    var63 = var37;
                                    var65++;
                                    continue;
                                 }

                                 var54 = false;
                                 break;
                              }

                              var54 = false;
                              break;
                           }

                           if (var54 && var63.onGround()) {
                              double var66 = AiJumpSolver.settledLandingMargin(var0, var63, var9, var1.y(), var56.yaw());
                              if (!(var66 < 0.01)) {
                                 int var68 = 0;

                                 while (var68 < var61.size() && !((AiPhysics.MoveInput)var61.get(var68)).jump()) {
                                    var68++;
                                 }

                                 if (var68 < var61.size()) {
                                    AiJumpManeuver var38 = new AiJumpManeuver(var61, var1, var63, var66, var68);
                                    return new AiJumpSolver.JumpPlan(
                                       var56.yaw(),
                                       var68,
                                       true,
                                       2147483647,
                                       var61.size() - var68,
                                       var1.x(),
                                       var1.z(),
                                       var63.x(),
                                       var63.y(),
                                       var63.z(),
                                       var63.speed(),
                                       var66,
                                       0.0,
                                       var1.speed(),
                                       var38
                                    );
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
      } else {
         return null;
      }
   }

   private static AiJumpSolver.JumpPlan toJumpPlan(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiWallJump.SearchNode var2, BlockPos var3, float var4
   ) {
      ArrayDeque var5 = new ArrayDeque();

      for (AiWallJump.SearchNode var6 = var2; var6.input() != null; var6 = var6.parent()) {
         var5.addFirst(var6.input());
      }

      var var12 = List.copyOf(var5);
      int var7 = 0;

      while (!((AiPhysics.MoveInput)var12.get(var7)).jump()) {
         var7++;
      }

      AiPhysics.MotionState var8 = var2.body();
      double var9 = AiJumpSolver.settledLandingMargin(var0, var8, var3, var1.y(), var4);
      AiJumpManeuver var11 = new AiJumpManeuver(var12, var1, var8, var9, var7);
      return new AiJumpSolver.JumpPlan(
         var4, var7, true, 2147483647, var12.size() - var7, var1.x(), var1.z(), var8.x(), var8.y(), var8.z(), var8.speed(), var9, 0.0, var1.speed(), var11
      );
   }

   private record WallFrame(double x, double z, double ux, double uz, float yaw, int side) {
      double along(AiPhysics.MotionState var1) {
         return (var1.x() - this.x) * this.ux + (var1.z() - this.z) * this.uz;
      }

      double across(AiPhysics.MotionState var1) {
         return ((var1.x() - this.x) * this.uz - (var1.z() - this.z) * this.ux) * this.side;
      }

      double advance(AiPhysics.MotionState var1) {
         return var1.vx() * this.ux + var1.vz() * this.uz;
      }

      double drift(AiPhysics.MotionState var1) {
         return (var1.vx() * this.uz - var1.vz() * this.ux) * this.side;
      }
   }

   private record SearchNode(AiPhysics.MotionState body, AiPhysics.MoveInput input, AiWallJump.SearchNode parent, double score) {
   }
}
