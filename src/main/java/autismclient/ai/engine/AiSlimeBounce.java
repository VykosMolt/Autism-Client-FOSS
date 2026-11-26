package autismclient.ai.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public final class AiSlimeBounce {
   static final int flowState = 24;
   private static final int flowTemp = 76;
   private static final float putAt_F = 30.0F;
   private static final float[] putAt_FArr = new float[]{0.0F, -5.0F, 5.0F, -10.0F, 10.0F, -15.0F, 15.0F, -20.0F, 20.0F, -25.0F, 25.0F};
   private static final float[] fill_FArr = new float[]{0.0F, -15.0F, 15.0F, -30.0F, 30.0F, -45.0F, 45.0F, -60.0F, 60.0F};
   private static final int[] putAt_IArr = new int[]{3, 6, 9, 11};
   private static final float[] readEx_FArr = new float[]{-60.0F, 60.0F, -45.0F, 45.0F, -30.0F, 30.0F};
   private static final float[] value = new float[]{0.0F, -15.0F, 15.0F, -30.0F, 30.0F};
   private static final int[] fill_IArr = new int[]{2, 0, -1, 1, 3, 4, 5, 6, 7};
   private static final int[] readEx_IArr = new int[]{2, 0, -1, 1, 3, 4, 5, 6, 7, 8};

   private AiSlimeBounce() {
   }

   static List<AiSlimeBounce.Inner_JsonCache> putAt_List_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, BlockPos var3, boolean var4
   ) {
      LinkedHashMap var5 = new LinkedHashMap();
      HashMap var6 = new HashMap();
      ArrayList<AiSlimeBounce.Inner_TaskState> var7 = new ArrayList<>();
      HashSet<AiSlimeBounce.Inner_BitCore> var8 = new HashSet<>();
      int[] var9 = var1.speed() > 0.05 ? readEx_IArr : fill_IArr;

      for (float var13 : putAt_FArr) {
         for (int var17 : var9) {
            AiSlimeBounce.Inner_TaskState var18 = putAt_StreamCore_TaskSta_TokenCore_TaskStat(var0, var1, var2 + var13, var17, var4);
            if (var18 != null && var8.add(new AiSlimeBounce.Inner_BitCore(var18.state(), var18.yaw(), var18.approach().size()))) {
               var7.add(var18);
            }
         }
      }

      boolean var43 = var3 != null && var0.state(var3.getX(), var3.getY(), var3.getZ());
      int var44 = var43 ? putAt_I_TokenCore_TaskStat_BlockPos(var0, var3) : (var3 != null ? var3.getY() : 0);
      AiSlimeBounce.Inner_JsonCache var45 = null;
      double var46 = 1.7976931348623157E308;
      boolean var47 = false;
      record Inner_CacheHelper(AiSlimeBounce.Inner_TaskState rebound, float turn, boolean boost) {
      }
      ArrayList<Inner_CacheHelper> var48 = new ArrayList<>();
      HashSet<Long> var49 = new HashSet<>();

      for (float var21 : fill_FArr) {
         int var22 = 0;
         int var23 = 0;

         for (AiSlimeBounce.Inner_TaskState var25 : var7) {
            for (int var26 = 1; var26 >= 0; var26--) {
               AiSlimeBounce.Inner_TableCache var27 = putAt_StreamCore_TableCa_TokenCore_TaskStat(
                  var0, var1, var25, var21, var4, var26 != 0, -1, 0.0F, 0.0F, false
               );
               byte var28 = (byte)(var26 != 0 ? var22 : var23);
               if (var27.highClear() && var27.hadObstacleOrSecondBounce() && var28 < 5 && var48.size() < 56) {
                  AiPhysics.MotionState var29 = var25.state();
                  long var30 = (Math.round(var29.x() * 4.0) & 255L) << 20
                     | (Math.round(var29.z() * 4.0) & 255L) << 12
                     | (Math.round(var21 / 15.0F) & 15L) << 4
                     | var26 & 1L;
                  if (var49.add(var30)) {
                     var48.add(new Inner_CacheHelper(var25, var21, var26 != 0));
                     if (var26 != 0) {
                        var22++;
                     } else {
                        var23++;
                     }
                  }
               }

               AiSlimeBounce.Inner_JsonCache var62 = var27.landing();
               if (var62 != null) {
                  if (var3 != null) {
                     if (var62.cell().equals(var3) || putAt(var0, var62.cell(), var3)) {
                        double var65 = putAt_D_StreamCore_JsonCac(var62, var43, var44, var27.headBonked(), var27.wallHits());
                        if (var65 < var46) {
                           var46 = var65;
                           var45 = var62;
                        }

                        if (var27.clean()) {
                           var47 = true;
                        }

                        if (var27.clean() && (!var43 || var62.plan().landY() >= var44 - 2.0)) {
                           return List.of(var45);
                        }
                     }
                  } else {
                     boolean var66 = var0.state(var62.cell().getX(), var62.cell().getY(), var62.cell().getZ());
                     int var31 = var66 ? putAt_I_TokenCore_TaskStat_BlockPos(var0, var62.cell()) : var62.cell().getY();
                     double var32 = putAt_D_StreamCore_JsonCac(var62, var66, var31, var27.headBonked(), var27.wallHits());
                     if (var27.clean()) {
                        var47 = true;
                     }

                     Double var34 = (Double)var6.get(var62.cell());
                     if (var34 == null || var32 < var34) {
                        var6.put(var62.cell(), var32);
                        var5.put(var62.cell(), var62.plan());
                     }
                  }
               }
            }
         }

         if (var3 == null && var47 && !var5.isEmpty() && !putAt(var0, var5, var1.y())) {
            break;
         }
      }

      if (!var48.isEmpty() && (var3 == null || var43)) {
         for (Inner_CacheHelper var52 : var48) {
            for (int var56 : putAt_IArr) {
               for (float var60 : readEx_FArr) {
                  AiSlimeBounce.Inner_RingUtil var61 = putAt(var0, var1, var52.rebound(), var52.turn(), var4, var52.boost(), var56, var60);
                  if (var61 != null) {
                     if (var61.landingOut() != null) {
                        AiSlimeBounce.Inner_TableCache var64 = var61.landingOut();
                        AiSlimeBounce.Inner_JsonCache var68 = var64.landing();
                        if (var68 != null) {
                           if (var3 != null) {
                              if (var68.cell().equals(var3) || putAt(var0, var68.cell(), var3)) {
                                 double var71 = putAt_D_StreamCore_JsonCac(var68, var43, var44, var64.headBonked(), var64.wallHits());
                                 if (var71 < var46) {
                                    var46 = var71;
                                    var45 = var68;
                                 }

                                 if (var64.clean()) {
                                    var47 = true;
                                 }
                              }
                           } else {
                              boolean var70 = var0.state(var68.cell().getX(), var68.cell().getY(), var68.cell().getZ());
                              int var73 = var70 ? putAt_I_TokenCore_TaskStat_BlockPos(var0, var68.cell()) : var68.cell().getY();
                              double var74 = putAt_D_StreamCore_JsonCac(var68, var70, var73, var64.headBonked(), var64.wallHits());
                              Double var76 = (Double)var6.get(var68.cell());
                              if (var76 == null || var74 < var76) {
                                 var6.put(var68.cell(), var74);
                                 var5.put(var68.cell(), var68.plan());
                              }
                           }
                        }
                     } else {
                        AiSlimeBounce.Inner_JsonEntry var63 = var61.secondBounce();
                        if (var63 != null && (!var47 || !var63.headBonked() && var63.wallHits() <= 0)) {
                           float var67 = var52.rebound().yaw() + var52.turn();

                           for (float var75 : value) {
                              for (int var35 = 1; var35 >= 0; var35--) {
                                 AiSlimeBounce.Inner_TableCache var36 = putAt(var0, var1, var52.rebound(), var63, var67 + var75, var4, var35 != 0);
                                 AiSlimeBounce.Inner_JsonCache var37 = var36.landing();
                                 if (var37 != null) {
                                    if (var3 != null) {
                                       if (var37.cell().equals(var3) || putAt(var0, var37.cell(), var3)) {
                                          double var38 = putAt_D_StreamCore_JsonCac(var37, var43, var44, var36.headBonked(), var36.wallHits());
                                          if (var38 < var46) {
                                             var46 = var38;
                                             var45 = var37;
                                          }

                                          if (var36.clean()) {
                                             var47 = true;
                                          }
                                       }
                                    } else {
                                       boolean var77 = var0.state(var37.cell().getX(), var37.cell().getY(), var37.cell().getZ());
                                       int var39 = var77 ? putAt_I_TokenCore_TaskStat_BlockPos(var0, var37.cell()) : var37.cell().getY();
                                       double var40 = putAt_D_StreamCore_JsonCac(var37, var77, var39, var36.headBonked(), var36.wallHits());
                                       Double var42 = (Double)var6.get(var37.cell());
                                       if (var42 == null || var40 < var42) {
                                          var6.put(var37.cell(), var40);
                                          var5.put(var37.cell(), var37.plan());
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

      if (var3 != null) {
         return var45 == null ? List.of() : List.of(var45);
      } else {
         return var5.entrySet()
            .stream()
            .map(var0x -> new AiSlimeBounce.Inner_JsonCache((BlockPos)((java.util.Map.Entry)var0x).getKey(), (AiJumpSolver.JumpPlan)((java.util.Map.Entry)var0x).getValue()))
            .toList();
      }
   }

   private static int putAt_I_TokenCore_TaskStat_BlockPos(AiPhysics.PhysicsWorld var0, BlockPos var1) {
      int var2 = var1.getY();

      while (var0.state(var1.getX(), var2 + 1, var1.getZ())) {
         var2++;
      }

      return var2 + 1;
   }

   private static double putAt_D_StreamCore_JsonCac(AiSlimeBounce.Inner_JsonCache var0, boolean var1, int var2, boolean var3, int var4) {
      double var5 = var1 ? Math.max(0.0, var2 - var0.plan().landY()) * 8.5 : 0.0;
      double var7 = var3 ? 120.0 : 0.0;
      double var9 = var4 > 0 ? 5.0 + var4 * 2.0 : 0.0;
      return var0.plan().ticks() + var5 + var7 + var9;
   }

   private static boolean putAt(AiPhysics.PhysicsWorld var0, Map<BlockPos, AiJumpSolver.JumpPlan> var1, double var2) {
      for (BlockPos var5 : var1.keySet()) {
         if (!var0.state(var5.getX(), var5.getY(), var5.getZ())) {
            return false;
         }

         if (!var0.state(var5.getX(), var5.getY() + 2, var5.getZ()) && var5.getY() >= var2 - 4.0) {
            return false;
         }
      }

      return true;
   }

   private static float putAt_F_F_F(float var0, float var1) {
      float var2 = Mth.wrapDegrees(var1 - var0);
      return var0 + Math.clamp(var2, -30.0F, 30.0F);
   }

   private static AiSlimeBounce.Inner_TaskState putAt_StreamCore_TaskSta_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, int var3, boolean var4
   ) {
      ArrayList<AiPhysics.MoveInput> var5 = new ArrayList<>();
      AiPhysics.MotionState var6 = var1;
      double var7 = var1.y();
      AiPhysics.MoveInput var9 = AiPhysics.MoveInput.run(var2, var4);
      AiPhysics.MoveInput var10 = var9.withJump(true);

      for (int var11 = 0; var11 < 38; var11++) {
         AiPhysics.MoveInput var12 = var11 == var3 ? var10 : var9;
         AiPhysics.MotionState var13 = AiPhysics.putAt(var6, var12, var0);
         if (!putAt_Z_TokenCore_TaskStat(var0, var6, var13, var7, var1.y()) || AiJumpSolver.putAt(var0, var13)) {
            return null;
         }

         var5.add(var12);
         if (putAt_Z_TokenCore_TaskStat_TokenCore_TableCac(var0, var13)) {
            return new AiSlimeBounce.Inner_TaskState(var13, var5, var2, var3);
         }

         if (var13.horizontalCollision() && var13.speed() < 0.01 && var13.y() >= var1.y() - 0.5) {
            return null;
         }

         var7 = Math.max(var7, var13.y());
         var6 = var13;
      }

      return null;
   }

   private static AiSlimeBounce.Inner_TableCache putAt_StreamCore_TableCa_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      AiSlimeBounce.Inner_TaskState var2,
      float var3,
      boolean var4,
      boolean var5,
      int var6,
      float var7,
      float var8,
      boolean var9
   ) {
      AiPhysics.MotionState var10 = var2.state();
      double var11 = var10.y();
      double var13 = var11;
      double var15 = var11;
      float var17 = var2.yaw();
      float var18 = var2.yaw() + var3;
      float var19 = var18 + var7;
      float var20 = var18 + var8;
      int var21 = var2.approach().size();
      ArrayList var22 = new ArrayList(76 - var21);
      boolean var23 = false;
      boolean var24 = false;
      boolean var25 = false;
      int var26 = 0;
      boolean var27 = false;

      for (int var28 = var21; var28 < 76; var28++) {
         int var29 = var28 - var21;
         float var30;
         if (var23) {
            var30 = var20;
         } else if (var6 >= 0 && var29 >= var6) {
            var30 = var19;
         } else {
            var30 = var18;
         }

         var17 = putAt_F_F_F(var17, var30);
         boolean var31 = var28 == var21 && var5 || var23 && var9 && var10.onGround() && var10.vy() > 0.2;
         AiPhysics.MoveInput var32 = new AiPhysics.MoveInput(1.0, 0.0, var31, var4, false, var17);
         AiPhysics.MotionState var33 = AiPhysics.putAt(var10, var32, var0);
         if (!putAt_Z_TokenCore_TaskStat(var0, var10, var33, var13, var1.y())) {
            return new AiSlimeBounce.Inner_TableCache(null, !var24 && var15 > var11 + 4.5, var27, var25, var26);
         }

         var22.add(var32);
         boolean var34 = var33.verticalCollision() && !var33.onGround();
         if (var34) {
            var25 = true;
         }

         boolean var35 = AiJumpSolver.putAt(var0, var33);
         if (!var35 && (var33.horizontalCollision() || AiPaceScript.value(var0, var33))) {
            var26++;
         }

         if (!var23) {
            if (var34) {
               var24 = true;
            }

            if (var33.horizontalCollision()) {
               var27 = true;
            }

            var15 = Math.max(var15, var33.y());
         }

         boolean var36 = putAt_Z_TokenCore_TaskStat_TokenCore_TableCac(var0, var33);
         if (var36) {
            var13 = var33.y();
            if (!var23) {
               var23 = true;
               var27 = true;
            }
         } else {
            var13 = Math.max(var13, var33.y());
         }

         boolean var37 = AiJumpSolver.putAt(var33);
         if (!var35 && !var37) {
            var10 = var33;
         } else {
            AiSlimeBounce.Inner_JsonCache var38 = putAt_StreamCore_JsonCac_TokenCore_TaskStat(var0, var1, var2, var22, var33, var11, var17, var4, var35, var37);
            if (var38 != null) {
               return new AiSlimeBounce.Inner_TableCache(var38, !var24 && var15 > var11 + 4.5, var27, var25, var26);
            }

            if (var35 || var37) {
               return new AiSlimeBounce.Inner_TableCache(null, !var24 && var15 > var11 + 4.5, var27, var25, var26);
            }

            var10 = var33;
         }
      }

      return new AiSlimeBounce.Inner_TableCache(null, !var24 && var15 > var11 + 4.5, var27, var25, var26);
   }

   private static AiSlimeBounce.Inner_RingUtil putAt(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      AiSlimeBounce.Inner_TaskState var2,
      float var3,
      boolean var4,
      boolean var5,
      int var6,
      float var7
   ) {
      AiPhysics.MotionState var8 = var2.state();
      double var9 = var8.y();
      double var11 = var9;
      double var13 = var9;
      float var15 = var2.yaw();
      float var16 = var2.yaw() + var3;
      float var17 = var16 + var7;
      int var18 = var2.approach().size();
      ArrayList var19 = new ArrayList(76 - var18);
      boolean var20 = false;
      int var21 = 0;

      for (int var22 = var18; var22 < 76; var22++) {
         int var23 = var22 - var18;
         float var24 = var23 >= var6 ? var17 : var16;
         var15 = putAt_F_F_F(var15, var24);
         AiPhysics.MoveInput var25 = new AiPhysics.MoveInput(1.0, 0.0, var22 == var18 && var5, var4, false, var15);
         AiPhysics.MotionState var26 = AiPhysics.putAt(var8, var25, var0);
         if (!putAt_Z_TokenCore_TaskStat(var0, var8, var26, var11, var1.y())) {
            return null;
         }

         var19.add(var25);
         if (var26.verticalCollision() && !var26.onGround()) {
            var20 = true;
         }

         boolean var27 = AiJumpSolver.putAt(var0, var26);
         if (!var27 && (var26.horizontalCollision() || AiPaceScript.value(var0, var26))) {
            var21++;
         }

         var13 = Math.max(var13, var26.y());
         if (putAt_Z_TokenCore_TaskStat_TokenCore_TableCac(var0, var26)) {
            return new AiSlimeBounce.Inner_RingUtil(null, new AiSlimeBounce.Inner_JsonEntry(var26, var19, var15, var26.y(), var20, var21));
         }

         var11 = Math.max(var11, var26.y());
         boolean var28 = AiJumpSolver.putAt(var26);
         if (var27 || var28) {
            AiSlimeBounce.Inner_JsonCache var29 = putAt_StreamCore_JsonCac_TokenCore_TaskStat(var0, var1, var2, var19, var26, var9, var15, var4, var27, var28);
            if (var29 == null) {
               return null;
            }

            return new AiSlimeBounce.Inner_RingUtil(new AiSlimeBounce.Inner_TableCache(var29, !var20 && var13 > var9 + 4.5, true, var20, var21), null);
         }

         var8 = var26;
      }

      return null;
   }

   private static AiSlimeBounce.Inner_TableCache putAt(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      AiSlimeBounce.Inner_TaskState var2,
      AiSlimeBounce.Inner_JsonEntry var3,
      float var4,
      boolean var5,
      boolean var6
   ) {
      AiPhysics.MotionState var7 = var3.state();
      double var8 = var2.state().y();
      double var10 = var3.peak();
      float var12 = var3.cam();
      int var13 = var2.approach().size();
      int var14 = var13 + var3.prefixTail().size();
      ArrayList var15 = new ArrayList(Math.max(4, 76 - var14));
      boolean var16 = var3.headBonked();
      int var17 = var3.wallHits();

      for (int var18 = var14; var18 < 76; var18++) {
         var12 = putAt_F_F_F(var12, var4);
         boolean var19 = var6 && var7.onGround() && var7.vy() > 0.2;
         AiPhysics.MoveInput var20 = new AiPhysics.MoveInput(1.0, 0.0, var19, var5, false, var12);
         AiPhysics.MotionState var21 = AiPhysics.putAt(var7, var20, var0);
         if (!putAt_Z_TokenCore_TaskStat(var0, var7, var21, var10, var1.y())) {
            return new AiSlimeBounce.Inner_TableCache(null, false, true, var16, var17);
         }

         var15.add(var20);
         if (var21.verticalCollision() && !var21.onGround()) {
            var16 = true;
         }

         boolean var22 = AiJumpSolver.putAt(var0, var21);
         if (!var22 && (var21.horizontalCollision() || AiPaceScript.value(var0, var21))) {
            var17++;
         }

         var10 = putAt_Z_TokenCore_TaskStat_TokenCore_TableCac(var0, var21) ? var21.y() : Math.max(var10, var21.y());
         boolean var23 = AiJumpSolver.putAt(var21);
         if (var22 || var23) {
            ArrayList var24 = new ArrayList(var3.prefixTail().size() + var15.size());
            var24.addAll(var3.prefixTail());
            var24.addAll(var15);
            AiSlimeBounce.Inner_JsonCache var25 = putAt_StreamCore_JsonCac_TokenCore_TaskStat(var0, var1, var2, var24, var21, var8, var12, var5, var22, var23);
            if (var25 != null) {
               return new AiSlimeBounce.Inner_TableCache(var25, !var16, true, var16, var17);
            }

            return new AiSlimeBounce.Inner_TableCache(null, false, true, var16, var17);
         }

         var7 = var21;
      }

      return new AiSlimeBounce.Inner_TableCache(null, false, true, var16, var17);
   }

   private static AiSlimeBounce.Inner_JsonCache putAt_StreamCore_JsonCac_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      AiSlimeBounce.Inner_TaskState var2,
      List<AiPhysics.MoveInput> var3,
      AiPhysics.MotionState var4,
      double var5,
      float var7,
      boolean var8,
      boolean var9,
      boolean var10
   ) {
      BlockPos var11 = BlockPos.containing(var4.x(), var9 ? var4.y() : Math.ceil(var4.y() - 1.0E-6), var4.z());
      if (var9 && AiLadderJump.putAt(var0, var11) && !AiLadderJump.putAt_Z_TokenCore_TaskStat(var0, var4, var11, var7)) {
         return null;
      }

      double var12 = var9 ? 0.1 : -1.0;
      if (var10 && var4.y() > var5 + 0.5 && var4.y() < var1.y() - 0.05 && AiJumpSolver.putAt_Z_D(var4.x(), var4.y(), var4.z(), var11, var4.y())) {
         var12 = AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var0, var4, var11, var4.y(), var7);
      }

      if (var12 < -0.04) {
         return null;
      }

      ArrayList var14 = new ArrayList(var2.approach().size() + var3.size());
      var14.addAll(var2.approach());
      var14.addAll(var3);
      AiJumpManeuver var15 = new AiJumpManeuver(var14, var1, var4, var12, Math.max(0, var2.takeoff()));
      return new AiSlimeBounce.Inner_JsonCache(
         var11,
         new AiJumpSolver.JumpPlan(
            var2.yaw(), 0, var8, 2147483647, var14.size(), var1.x(), var1.z(), var4.x(), var4.y(), var4.z(), var4.speed(), var15.margin(), 0.0, 0.0, var15
         )
      );
   }

   static boolean putAt(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiJumpManeuver var2, int var3, BlockPos var4, double var5) {
      for (int var7 = var3; var7 < var2.inputs().size() + 4; var7++) {
         AiPhysics.MotionState var8 = AiPhysics.putAt(var1, putAt_TokenCore_CacheHel_TaskStore_I(var2, var7), var0);
         if (!putAt_Z_TokenCore_TaskStat(var0, var1, var8, var5, var2.start().y())) {
            return false;
         }

         if (AiJumpSolver.putAt(var0, var8)) {
            BlockPos var9 = BlockPos.containing(var8.x(), var8.y(), var8.z());
            return putAt(var0, var9, var4)
               && (
                  !AiLadderJump.putAt(var0, var9)
                     || AiLadderJump.putAt_Z_TokenCore_TaskStat(var0, var8, var4, putAt_TokenCore_CacheHel_TaskStore_I(var2, var7).yaw())
               );
         }

         if (AiJumpSolver.putAt(var8)
            && Math.abs(var8.y() - var2.landing().y()) <= 0.02
            && AiJumpSolver.putAt_Z_D(var8.x(), var8.y(), var8.z(), var4, var2.landing().y())
            && AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var0, var8, var4, var2.landing().y(), putAt_TokenCore_CacheHel_TaskStore_I(var2, var7).yaw()) >= -0.04) {
            return true;
         }

         var5 = putAt_Z_TokenCore_TaskStat_TokenCore_TableCac(var0, var8) ? var8.y() : Math.max(var5, var8.y());
         var1 = var8;
      }

      return false;
   }

   static AiPhysics.MoveInput putAt_TokenCore_CacheHel_TaskStore_I(AiJumpManeuver var0, int var1) {
      return var1 < var0.inputs().size() ? var0.inputs().get(var1) : var0.inputs().getLast().withJump(false);
   }

   static boolean putAt(AiPhysics.PhysicsWorld var0, BlockPos var1, BlockPos var2) {
      if (var1.getX() == var2.getX() && var1.getZ() == var2.getZ()) {
         if (Math.abs(var1.getY() - var2.getY()) > 4) {
            return false;
         }

         for (int var3 = Math.min(var1.getY(), var2.getY()); var3 <= Math.max(var1.getY(), var2.getY()); var3++) {
            if (!var0.state(var1.getX(), var3, var1.getZ())) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean putAt_Z_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2, double var3, double var5
   ) {
      return var2.y() >= var5 - 24.0
         && !AiPhysics.fill(var0, var1, var2)
         && (!var2.onGround() || putAt_Z_TokenCore_TaskStat_TokenCore_TableCac(var0, var2) || var3 - var2.y() <= var0.putAt_D());
   }

   static boolean putAt_Z_TokenCore_TaskStat_TokenCore_TableCac(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1) {
      if (var1.onGround() && !(var1.vy() < 0.2)) {
         long var2 = var1.support() == -9223372036854775808L ? AiPhysics.putAt_J_AABB(var1.box(false), var1.x(), var1.y(), var1.z(), var0) : var1.support();
         BlockPos var4 = var2 == -9223372036854775808L ? BlockPos.containing(var1.x(), var1.y() - 0.15, var1.z()) : BlockPos.of(var2);
         return var0.readEx_D(var4.getX(), var4.getY(), var4.getZ()) >= 0.9;
      } else {
         return false;
      }
   }

   private record Inner_BitCore(AiPhysics.MotionState state, float yaw, int ticks) {
   }

   record Inner_JsonCache(BlockPos cell, AiJumpSolver.JumpPlan plan) {
   }

   private record Inner_JsonEntry(
      AiPhysics.MotionState state, List<AiPhysics.MoveInput> prefixTail, float cam, double peak, boolean headBonked, int wallHits
   ) {
   }

   private record Inner_RingUtil(AiSlimeBounce.Inner_TableCache landingOut, AiSlimeBounce.Inner_JsonEntry secondBounce) {
   }

   private record Inner_TableCache(AiSlimeBounce.Inner_JsonCache landing, boolean highClear, boolean hadObstacleOrSecondBounce, boolean headBonked, int wallHits) {
      boolean clean() {
         return !this.headBonked && this.wallHits == 0;
      }
   }

   private record Inner_TaskState(AiPhysics.MotionState state, List<AiPhysics.MoveInput> approach, float yaw, int takeoff) {
   }
}
