package autismclient.ai.a;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public final class AiJumpSolver {
   public static final int flowState = 12;
   public static final int flowTemp = 40;
   public static final int readEx_I = 100;
   static final int value_I = 40;
   static final double putAt_D = 4.0;
   public static final double fill_D = -0.12;
   public static final double readEx_D = -0.04;
   public static final double value_D = 0.55;
   public static final int prevEx_I = 2147483647;
   public static final double prevEx_D = 0.28;
   public static final double prev_D = 0.14;
   private static final int[] putAt_IArr = new int[]{2147483647, 1, 3, 5, 7, 9};
   private static final int[] fill_IArr = new int[]{2147483647, 1, 3, 5, 7, 9};
   private static final float[] putAt_FArr = new float[]{-4.0F, 4.0F, -8.0F, 8.0F, -12.0F, 12.0F, -16.0F, 16.0F, -20.0F, 20.0F};
   private static final int[] readEx_IArr = new int[]{0, -18, 18, -36, 36, -60, 60, -84, 84};
   private static final int[] value_IArr = new int[]{0, -7, 7, -14, 14};
   private static final int[] prevEx_IArr = new int[]{0, -7, 7, -14, 14, -21, 21};
   private static final int[] prev_IArr = new int[]{4, 6, 8};
   private static final float[] fill_FArr = new float[]{0.0F, -8.0F, 8.0F, -16.0F, 16.0F, -24.0F, 24.0F};
   private static final ConcurrentHashMap<Integer, Double> putAt_ConcurrentHashMap = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<AiJumpSolver.Inner_EventBus, Double> fill_ConcurrentHashMap = new ConcurrentHashMap<>();

   private AiJumpSolver() {
   }

   static AiJumpSolver.Inner_TableCache putAt_IoUtil_TableCache_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, BlockPos var3, double var4, AiJumpSolver.Inner_BufferHelper var6
   ) {
      AiJumpSolver.Inner_JsonUtil var7 = var6.nextJump();
      if (var7 != null && var7.sprint() && var3.equals(var7.from())) {
         float var8 = Mth.wrapDegrees(var7.yaw() - var2);
         if (!(Math.abs(var8) > 105.0F) && !(Math.abs(var8) < 3.0F)) {
            AiJumpSolver.Inner_TableCache var9 = null;
            double var10 = Math.toRadians(var7.yaw());
            double var12 = -Math.sin(var10);
            double var14 = Math.cos(var10);

            for (int var16 = 0; var16 < 5; var16++) {
               float var17 = var2 + Math.clamp(var8, -23.0F, 23.0F) * (var16 == 0 ? 0.0F : (var16 == 1 ? 0.5F : 1.0F));
               double var18 = var16 == 3 ? 1.0 : (var16 == 4 ? -1.0 : 0.0);
               AiPhysics.Inner_DataUtil var20 = AiPhysics.Inner_DataUtil.run(var17, true).withStrafe(var18);
               AiPhysics.Inner_StreamUtil var21 = var1;

               for (int var22 = 0; var22 < 9; var22++) {
                  AiPhysics.Inner_StreamUtil var23 = AiPhysics.putAt(var21, var20, var0);
                  if (var23.horizontalCollision()
                     || var23.verticalCollision() && !var23.onGround()
                     || AiPhysics.fill(var0, var21, var23)
                     || putAt(var0, var23)
                     || var23.y() < var4 - 0.55) {
                     break;
                  }

                  if (putAt(var23)) {
                     double var24 = putAt_D_TokenCore_TaskStat_2(var0, var23, var3, var4, var17);
                     if (var24 < 0.08) {
                        var24 = putAt_D_TokenCore_TaskStat_2(var0, var23, var3, var4);
                     }

                     if (var24 < 0.08 || !putAt_Z_TokenCore_TableCac(var23, var3, var4)) {
                        break;
                     }

                     AiJumpSolver.UiHelper var26 = readEx(var0, var23, var7.yaw(), true, var6.cell(), var6.surface());
                     if (var26.margin() < 0.07 || var26.runTicks() > 5) {
                        break;
                     }

                     int var27 = var26.runTicks();
                     double var28 = var23.vx() * var12 + var23.vz() * var14;
                     if (var9 == null || var27 < var9.nextRunTicks() || var27 == var9.nextRunTicks() && var28 > var9.exitSpeed() + 0.009) {
                        var9 = new AiJumpSolver.Inner_TableCache(var17, var18, var24, var27, var28);
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

   static AiJumpSolver.Inner_JsonEntry putAt_IoUtil_JsonEntry_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, float var3, BlockPos var4, double var5
   ) {
      if (!(var1.speed() < 0.1) && var1.jumpDelay() == 0 && !var1.horizontalCollision()) {
         double var7 = var4.getX() + 0.5 - var1.x();
         double var9 = var4.getZ() + 0.5 - var1.z();
         double var11 = Math.hypot(var7, var9);
         if (!(var11 < 1.7) && !(var11 > 6.2) && !(Math.abs(var5 - var1.y()) > 2.1)) {
            AiJumpSolver.Inner_NetHelper var13 = new AiJumpSolver.Inner_NetHelper(var1.x(), var1.z(), var7 / var11, var9 / var11, var11);
            AiJumpSolver.Inner_JsonEntry var14 = null;

            for (float var18 : new float[]{0.0F, -7.0F, 7.0F, -14.0F, 14.0F, -20.0F, 20.0F}) {
               float var19 = var2 + var18;
               if (!(Math.abs(Mth.wrapDegrees(var19 - var3)) > 32.0F)) {
                  AiPhysics.Inner_DataUtil var20 = AiPhysics.Inner_DataUtil.run(var19, true);
                  AiJumpSolver.Inner_StreamUtil var21 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(var0, var1, var20, var20, 2147483647, var13, var4, var5);
                  double var22 = putAt_D_TokenCore_TaskStat(var0, var21, var4, var5);
                  if (var22 >= 0.065) {
                     AiJumpSolver.UiHelper var24 = new AiJumpSolver.UiHelper(0, 0.0, 2147483647, var22, true, var21.ticks() + 1);
                     if (var14 == null || var22 > var14.takeoff().margin()) {
                        var14 = new AiJumpSolver.Inner_JsonEntry(var19, var24);
                     }

                     if (var22 >= 0.14) {
                        break;
                     }
                  } else if (var14 == null && var21 != null && var21.along() > var11 + 0.18 && Math.abs(var18) <= 14.0F) {
                     for (int var27 : new int[]{8, 5, 2}) {
                        AiJumpSolver.Inner_StreamUtil var28 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(var0, var1, var20, var20, var27, var13, var4, var5);
                        var22 = putAt_D_TokenCore_TaskStat(var0, var28, var4, var5);
                        if (!(var22 < 0.08)) {
                           var14 = new AiJumpSolver.Inner_JsonEntry(var19, new AiJumpSolver.UiHelper(0, 0.0, var27, var22, true, var28.ticks() + 1));
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

   private static double putAt_D_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, AiJumpSolver.Inner_StreamUtil var1, BlockPos var2, double var3) {
      if (var1 != null && !var1.bumped() && var1.settled() && putAt_Z_TokenCore_TableCac(var1.end(), var2, var3)) {
         return var1.margin() >= 0.065 ? var1.margin() : putAt_D_TokenCore_TaskStat_2(var0, var1.end(), var2, var3);
      } else {
         return -1.0;
      }
   }

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat_3(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, BlockPos var3, double var4
   ) {
      if (putAt(var1) && var1.jumpDelay() == 0 && !var1.horizontalCollision() && var1.pose() == 0) {
         double var6 = var3.getX() + 0.5 - var1.x();
         double var8 = var3.getZ() + 0.5 - var1.z();
         double var10 = Math.hypot(var6, var8);
         if (!(var10 < 1.6) && !(var10 > 6.7) && !(Math.abs(var4 - var1.y()) > 3.1)) {
            AiJumpSolver.Inner_NetHelper var12 = new AiJumpSolver.Inner_NetHelper(var1.x(), var1.z(), var6 / var10, var8 / var10, var10);
            float var13 = AiPhysics.putAt_F_D(var1.x(), var1.z(), var3.getX() + 0.5, var3.getZ() + 0.5);
            float var14 = Mth.wrapDegrees(var13 - var2);
            if (Math.abs(var14) > 78.0F) {
               return null;
            }

            float[] var15 = new float[]{var2 + Math.clamp(var14, -28.0F, 28.0F), var2 + Math.clamp(var14, -38.0F, 38.0F)};

            for (float var19 : var15) {
               float var20 = var19 + Math.clamp(Mth.wrapDegrees(var13 - var19), -20.0F, 20.0F);

               for (int var24 : new int[]{0, -1, 1}) {
                  AiPhysics.Inner_DataUtil var25 = AiPhysics.Inner_DataUtil.jump(var19, true).withStrafe(var24);
                  AiPhysics.Inner_DataUtil var26 = AiPhysics.Inner_DataUtil.run(var20, true);
                  AiJumpSolver.Inner_StreamUtil var27 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(var0, var1, var25, var26, 2147483647, var12, var3, var4);
                  if (var27 != null && !var27.bumped() && var27.settled() && !(var27.margin() < 0.085) && putAt_Z_TokenCore_TableCac(var27.end(), var3, var4)) {
                     ArrayList var28 = new ArrayList(var27.ticks() + 1);
                     var28.add(var25);

                     for (int var29 = 0; var29 < var27.ticks(); var29++) {
                        var28.add(var26);
                     }

                     AiPhysics.Inner_StreamUtil var31 = var27.end();
                     AiJumpManeuver var30 = new AiJumpManeuver(var28, var1, var31, var27.margin(), 0);
                     return new AiJumpSolver.Inner_StateCache(
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

   private static boolean putAt_Z_TokenCore_TableCac(AiPhysics.Inner_StreamUtil var0, BlockPos var1, double var2) {
      return Math.abs(var0.y() - var2) <= 0.02 && (int)Math.floor(var0.x()) == var1.getX() && (int)Math.floor(var0.z()) == var1.getZ();
   }

   static double putAt_D_TokenCore_TaskStat_2(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, double var3) {
      if (putAt(var1) && putAt_Z_TokenCore_TableCac(var1, var2, var3) && putAt_Z_TokenCore_TaskStat_2(var0, var2.getX(), var2.getZ(), var3)) {
         AiPhysics.Inner_StreamUtil var5 = var1;
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

            if (var12 < 0.24 || !putAt(var5) || Math.abs(var5.y() - var3) > 0.02) {
               return -1.0;
            }

            var6 = Math.min(var6, var12);
            if (var5.speed() <= 0.008) {
               return var6 - 0.17;
            }

            AiPhysics.Inner_StreamUtil var20 = AiPhysics.putAt(var5, AiPhysics.Inner_DataUtil.NONE, var0);
            if (AiPhysics.fill(var0, var5, var20)) {
               return -1.0;
            }

            var5 = var20;
         }

         return -1.0;
      } else {
         return -1.0;
      }
   }

   public static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat_2(
      AiPhysics.Inner_CachePool var0, BlockPos var1, double var2, BlockPos var4, double var5, double var7, boolean var9
   ) {
      return putAt_IoUtil_TextState_TokenCore_TaskStat_2(var0, var1, var2, var4, var5, var7, var9, true);
   }

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat_2(
      AiPhysics.Inner_CachePool var0, BlockPos var1, double var2, BlockPos var4, double var5, double var7, boolean var9, boolean var10
   ) {
      return putAt(var0, var1, var2, var4, var5, var7, var9, var10, true);
   }

   static AiJumpSolver.Inner_StateCache putAt(
      AiPhysics.Inner_CachePool var0, BlockPos var1, double var2, BlockPos var4, double var5, double var7, boolean var9, boolean var10, boolean var11
   ) {
      return putAt(var0, var1, var2, var4, var5, var7, var9, var10, var11, false);
   }

   static AiJumpSolver.Inner_StateCache putAt(
      AiPhysics.Inner_CachePool var0,
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
      return putAt(var0, var1, var2, var4, var5, var7, var9, var10, var11, var12, false);
   }

   static AiJumpSolver.Inner_StateCache putAt(
      AiPhysics.Inner_CachePool var0,
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

      if (var10 && var11 && !putAt_Z_TokenCore_TaskStat(var0, var14, var2, var16)) {
         AiJumpSolver.Inner_StateCache var40 = putAt_IoUtil_TextState_TokenCore_TaskStat_3(var0, var1, var2, var4, var5, var9, var12);
         return var40 != null ? var40 : AiJumpManeuver.solve(var0, var1, var2, var4, var5, var9, true, var12);
      }

      double var28 = var22 / var26;
      double var30 = var24 / var26;
      double var32 = var7 > 0.0 ? var14 - var28 * 0.2 : var14;
      double var34 = var7 > 0.0 ? var16 - var30 * 0.2 : var16;
      AiJumpSolver.Inner_StateCache var36 = putAt(var0, var32, var34, var2, var4, var5, var28, var30, var7, var9, 0.0, var13);
      if (var7 > 0.0 && (var36 == null || var36.margin() < 0.14)) {
         AiJumpSolver.Inner_StateCache var37 = putAt(var0, var1, var2, var4, var5, 0.0, var9, false, false, false, var13);
         if (var37 != null && (var36 == null || var36.margin() < -0.04 || var37.margin() > var36.margin() + 0.04)) {
            var36 = var37;
         }
      }

      if ((var36 == null || var36.margin() < 0.0) && var7 <= 0.0) {
         double var41 = putAt_D_TokenCore_TaskStat(var0, var14, var16, var2, var28, var30);
         if (var41 >= 0.25) {
            AiJumpSolver.Inner_StateCache var39 = putAt(var0, var14 - var28 * var41, var16 - var30 * var41, var2, var4, var5, var28, var30, 0.0, var9, var41, var13);
            if (var39 != null && (var36 == null || putAt(var39, var36))) {
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
         AiJumpSolver.Inner_StateCache var43 = putAt_IoUtil_TextState_TokenCore_TaskStat(var0, var2, var4, var5, var14, var16, var18, var20);
         if (var43 != null) {
            return var43;
         }
      }

      if (var10 && (var36 == null || var36.margin() < -0.04)) {
         if (var11) {
            AiJumpSolver.Inner_StateCache var44 = putAt_IoUtil_TextState_TokenCore_TaskStat_3(var0, var1, var2, var4, var5, var9, var12);
            if (var44 != null) {
               return var44;
            }
         }

         AiJumpSolver.Inner_StateCache var45 = AiJumpManeuver.solve(var0, var1, var2, var4, var5, var9, var11, var12);
         if (var45 != null && (var36 == null || var45.margin() > var36.margin())) {
            return var45;
         }
      }

      return var36;
   }

   private static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, double var1, BlockPos var3, double var4, double var6, double var8, double var10, double var12
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
            double var42 = putAt_D_TokenCore_TaskStat(var0, var32, var34, var1, var38, var40);
            var32 -= var38 * var42;
            var34 -= var40 * var42;
            AiJumpSolver.Inner_StateCache var44 = putAt(
               var0, var32, var34, var1, var3, var4, var23, var25, var38, var40, 0.0, true, Math.hypot(var32 - var6, var34 - var8), false
            );
            if (var44 != null && var44.margin() >= -0.04) {
               return var44;
            }
         }
      }

      return null;
   }

   private static AiJumpSolver.Inner_StateCache putAt(
      AiPhysics.Inner_CachePool var0,
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
      return putAt(var0, var1, var3, var5, var7, var8, var7.getX() + 0.5, var7.getZ() + 0.5, var10, var12, var14, var16, var17, var19);
   }

   private static AiJumpSolver.Inner_StateCache putAt(
      AiPhysics.Inner_CachePool var0,
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
      if (!putAt_Z_TokenCore_TaskStat(var0, var1, var5, var3)) {
         return null;
      }

      float var24 = AiPhysics.putAt_F_D(var1, var3, var10, var12);
      AiJumpSolver.Inner_NetHelper var25 = new AiJumpSolver.Inner_NetHelper(var1, var3, var14, var16, Math.hypot(var10 - var1, var12 - var3));
      AiJumpSolver.Inner_StateCache var26 = null;

      for (boolean var30 : var20 ? new boolean[]{true, false} : new boolean[]{false}) {
         AiPhysics.Inner_StreamUtil var31 = new AiPhysics.Inner_StreamUtil(var1, var5, var3, var14 * var18, -0.0784, var16 * var18, true, 0, false, false);
         AiPhysics.Inner_DataUtil var32 = AiPhysics.Inner_DataUtil.run(var24, var30);
         AiJumpSolver.Inner_CachePool var33 = putAt(var0, putAt_List_TokenCore_TaskStat_2(var0, var31, var32, 12), var32, var32, var25, var7, var8, false, var23);
         if (var33 != null && !(var33.arc().margin() < -0.12)) {
            AiJumpSolver.Inner_StreamUtil var34 = var33.arc();
            AiJumpSolver.Inner_StateCache var35 = new AiJumpSolver.Inner_StateCache(
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
            if (var26 == null || putAt(var35, var26)) {
               var26 = var35;
            }

            if (var30 && var35.margin() >= 0.0) {
               break;
            }
         }
      }

      return var26;
   }

   static List<AiJumpSolver.Inner_SyncManager> putAt_List_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, BlockPos var1, double var2) {
      double var4 = var1.getX() + 0.5;
      double var6 = var1.getZ() + 0.5;
      if (putAt_Z_TokenCore_TaskStat(var0, var4, var2, var6)) {
         return List.of(new AiJumpSolver.Inner_SyncManager(var4, var6));
      }

      ArrayList<AiJumpSolver.Inner_SyncManager> var8 = new ArrayList<>(4);

      for (double var12 : new double[]{0.38, 0.4, 0.22, 0.12}) {
         for (int var14 = 0; var14 < 2; var14++) {
            for (int var18 : new int[]{-1, 1}) {
               double var19 = var4 + (var14 == 0 ? var12 * var18 : 0.0);
               double var21 = var6 + (var14 == 1 ? var12 * var18 : 0.0);
               if (putAt_Z_TokenCore_TaskStat(var0, var19, var2, var21)) {
                  var8.add(new AiJumpSolver.Inner_SyncManager(var19, var21));
               }
            }
         }
      }

      for (double var29 : new double[]{0.38, 0.4, 0.42}) {
         for (int var33 : new int[]{-1, 1}) {
            for (int var36 : new int[]{-1, 1}) {
               double var22 = var4 + var33 * var29;
               double var24 = var6 + var36 * var29;
               if (putAt_Z_TokenCore_TaskStat(var0, var22, var2, var24)) {
                  var8.add(new AiJumpSolver.Inner_SyncManager(var22, var24));
               }
            }
         }
      }

      return var8;
   }

   static boolean putAt_Z_TokenCore_TaskStat_2(AiPhysics.Inner_CachePool var0, BlockPos var1, double var2) {
      return !putAt_List_TokenCore_TaskStat(var0, var1, var2).isEmpty();
   }

   static List<AiJumpSolver.Inner_SyncManager> putAt(List<AiJumpSolver.Inner_SyncManager> var0) {
      if (var0.size() < 2) {
         return var0;
      }

      ArrayList<AiJumpSolver.Inner_SyncManager> var1 = new ArrayList<>(var0.size());

      for (AiJumpSolver.Inner_SyncManager var3 : var0) {
         boolean var4 = false;

         for (AiJumpSolver.Inner_SyncManager var6 : var1) {
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

   private static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat_3(
      AiPhysics.Inner_CachePool var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7, boolean var8
   ) {
      List var9 = putAt_List_TokenCore_TaskStat(var0, var1, var2);
      List var10 = putAt_List_TokenCore_TaskStat(var0, var4, var5);
      if (var8) {
         var9 = putAt(var9);
         var10 = putAt(var10);
      }

      if (!var9.isEmpty() && !var10.isEmpty()) {
         if (((AiJumpSolver.Inner_SyncManager)var9.getFirst()).x() == var1.getX() + 0.5
            && ((AiJumpSolver.Inner_SyncManager)var9.getFirst()).z() == var1.getZ() + 0.5
            && ((AiJumpSolver.Inner_SyncManager)var10.getFirst()).x() == var4.getX() + 0.5
            && ((AiJumpSolver.Inner_SyncManager)var10.getFirst()).z() == var4.getZ() + 0.5) {
            return null;
         }

         AiJumpSolver.Inner_StateCache var11 = null;

         for (AiJumpSolver.Inner_SyncManager var13 : (Iterable<AiJumpSolver.Inner_SyncManager>)(Iterable<?>)(var9)) {
            for (AiJumpSolver.Inner_SyncManager var15 : (Iterable<AiJumpSolver.Inner_SyncManager>)(Iterable<?>)(var10)) {
               double var16 = var15.x() - var13.x();
               double var18 = var15.z() - var13.z();
               double var20 = Math.hypot(var16, var18);
               if (!(var20 < 0.05)) {
                  double var22 = var16 / var20;
                  double var24 = var18 / var20;
                  AiJumpSolver.Inner_StateCache var26 = putAt(
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
                  if (var26 != null && var26.margin() >= -0.04 && (var11 == null || putAt(var26, var11))) {
                     var11 = var26;
                  }

                  if (var26 == null || var26.margin() < 0.0) {
                     double var27 = putAt_D_TokenCore_TaskStat(var0, var13.x(), var13.z(), var2, var22, var24);
                     if (var27 >= 0.2) {
                        double var29 = var13.x() - var22 * var27;
                        double var31 = var13.z() - var24 * var27;
                        AiJumpSolver.Inner_StateCache var33 = putAt(
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
                        if (var33 != null && var33.margin() >= -0.04 && (var11 == null || putAt(var33, var11))) {
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

   static double putAt_D_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, double var1, double var3, double var5, double var7, double var9) {
      double var11 = 0.0;

      for (double var13 = 0.05; var13 <= 1.500000001 && putAt_Z_TokenCore_TaskStat(var0, var1 - var7 * var13, var5, var3 - var9 * var13); var13 += 0.05) {
         var11 = var13;
      }

      return Math.max(0.0, var11 - 0.15);
   }

   static boolean putAt_Z_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, double var1, double var3, double var5) {
      AiPhysics.Inner_StreamUtil var7 = new AiPhysics.Inner_StreamUtil(var1, var3, var5, 0.0, 0.0, 0.0, true, 0, false, false);
      if (AiPhysics.fill(var0, var7, var7)) {
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

   private static boolean putAt(AiJumpSolver.Inner_StateCache var0, AiJumpSolver.Inner_StateCache var1) {
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

   private static List<AiPhysics.Inner_StreamUtil> putAt_List_TokenCore_TaskStat_2(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, AiPhysics.Inner_DataUtil var2, int var3
   ) {
      ArrayList var4 = new ArrayList(var3 + 1);
      var4.add(var1);
      AiPhysics.Inner_StreamUtil var5 = var1;

      for (int var6 = 1; var6 <= var3; var6++) {
         AiPhysics.Inner_StreamUtil var7 = AiPhysics.putAt(var5, var2, var0);
         if (AiPhysics.fill(var0, var5, var7) || !var7.onGround() || var7.equals(var5)) {
            break;
         }

         var5 = var7;
         var4.add(var5);
      }

      return var4;
   }

   private static AiJumpSolver.Inner_CachePool putAt(
      AiPhysics.Inner_CachePool var0,
      List<AiPhysics.Inner_StreamUtil> var1,
      AiPhysics.Inner_DataUtil var2,
      AiPhysics.Inner_DataUtil var3,
      AiJumpSolver.Inner_NetHelper var4,
      BlockPos var5,
      double var6,
      boolean var8,
      boolean var9
   ) {
      AiJumpSolver.Inner_TaskQueue var10 = new AiJumpSolver.Inner_TaskQueue(var0, var1, var2, var3, var4, var5, var6);
      AiJumpSolver.Inner_CachePool var11 = putAt_IoUtil_TaskState(var10);
      if (var11 != null && var11.arc().margin() >= -0.04) {
         return var11;
      }

      if (!var9 && putAt_Z(var10)) {
         AiJumpSolver.Inner_CachePool var12 = putAt(var10, var8);
         if (var12 == null) {
            return var11;
         } else {
            return var11 != null && !(var12.arc().margin() > var11.arc().margin()) ? var11 : var12;
         }
      } else {
         return var11;
      }
   }

   private static boolean putAt_Z(AiJumpSolver.Inner_TaskQueue var0) {
      int var1 = var0.runs().size() - 1;
      AiJumpSolver.Inner_StreamUtil var2 = var0.fly(var1, 2147483647);
      if (var2 != null && var2.bumped()) {
         return true;
      }

      AiJumpSolver.Inner_StreamUtil var3 = var1 == 0 ? null : var0.fly(0, 2147483647);
      return var3 != null && var3.bumped();
   }

   private static AiJumpSolver.Inner_CachePool putAt(AiJumpSolver.Inner_TaskQueue var0, boolean var1) {
      AiJumpSolver.Inner_CachePool var2 = null;
      int var3 = Math.min(var0.runs().size() - 1, 15);
      if (!var1) {
         for (int var4 = 0; var4 <= var3; var4++) {
            AiJumpSolver.Inner_StreamUtil var5 = var0.fly(var4, 2147483647);
            if (var5 == null) {
               break;
            }

            if (var2 == null || var5.margin() > var2.arc().margin()) {
               var2 = new AiJumpSolver.Inner_CachePool(var4, var5);
            }

            if (var2.arc().margin() >= 0.15) {
               return var2;
            }
         }

         if (var2 != null && var2.arc().margin() >= -0.04) {
            int var12 = var2.runTicks();

            for (int var14 = Math.max(0, var12 - 2); var14 <= Math.min(var3, var12 + 2); var14++) {
               for (int var18 : putAt_IArr) {
                  if (var18 != 2147483647) {
                     AiJumpSolver.Inner_StreamUtil var10 = var0.fly(var14, var18);
                     if (var10 == null) {
                        break;
                     }

                     if (var10.margin() > var2.arc().margin()) {
                        var2 = new AiJumpSolver.Inner_CachePool(var14, var10);
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
         for (int var8 : var1 ? fill_IArr : putAt_IArr) {
            AiJumpSolver.Inner_StreamUtil var9 = var0.fly(var11, var8);
            if (var9 == null) {
               break;
            }

            if (var2 == null || var9.margin() > var2.arc().margin()) {
               var2 = new AiJumpSolver.Inner_CachePool(var11, var9);
            }

            if (var2.arc().margin() >= 0.15) {
               return var2;
            }
         }
      }

      return var2;
   }

   private static AiJumpSolver.Inner_CachePool putAt_IoUtil_TaskState(AiJumpSolver.Inner_TaskQueue var0) {
      int var1 = var0.runs().size() - 1;
      AiJumpSolver.Inner_StreamUtil var2 = var0.fly(var1, 2147483647);
      if (var2 == null) {
         return null;
      }

      if (var2.along() <= var0.line().target()) {
         return new AiJumpSolver.Inner_CachePool(var1, var2);
      }

      AiJumpSolver.Inner_StreamUtil var3 = var1 == 0 ? var2 : var0.fly(0, 2147483647);
      if (var3 != null && var3.along() > var0.line().target()) {
         return var3.settled() && !var3.bumped() && var3.margin() >= 0.14 && putAt_D_D(var3.end().x(), var3.end().z(), var0.to()) >= 0.08
            ? new AiJumpSolver.Inner_CachePool(0, var3)
            : new AiJumpSolver.Inner_CachePool(0, var0.releaseEarlier(0, var3));
      }

      int var4 = 0;
      AiJumpSolver.Inner_StreamUtil var5 = var3;
      AiJumpSolver.Inner_StreamUtil var6 = var2;

      while (var1 - var4 > 1) {
         int var7 = (var4 + var1) / 2;
         AiJumpSolver.Inner_StreamUtil var8 = var0.fly(var7, 2147483647);
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

      AiJumpSolver.Inner_CachePool var10 = var5 == null ? null : new AiJumpSolver.Inner_CachePool(var4, var5);
      AiJumpSolver.Inner_CachePool var11 = new AiJumpSolver.Inner_CachePool(var1, var6);
      if (var6.margin() < -0.12 && (var5 == null || var5.margin() < -0.12)) {
         AiJumpSolver.Inner_StreamUtil var9 = var0.releaseEarlier(var1, var6);
         if (var9.margin() > var6.margin()) {
            var11 = new AiJumpSolver.Inner_CachePool(var1, var9);
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

   private static AiJumpSolver.Inner_StreamUtil putAt_IoUtil_JsonCache_TokenCore_TaskStat_2(
      AiPhysics.Inner_CachePool var0,
      AiPhysics.Inner_StreamUtil var1,
      AiPhysics.Inner_DataUtil var2,
      AiPhysics.Inner_DataUtil var3,
      AiJumpSolver.Inner_StreamUtil var4,
      AiJumpSolver.Inner_NetHelper var5,
      BlockPos var6,
      double var7
   ) {
      AiJumpSolver.Inner_StreamUtil var9 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(var0, var1, var2, var3, 0, var5, var6, var7);
      if (var9 == null) {
         return var4;
      }

      if (var9.along() >= var5.target()) {
         return var9.margin() > var4.margin() ? var9 : var4;
      }

      int var10 = 0;
      int var11 = var4.ticks();
      AiJumpSolver.Inner_StreamUtil var12 = var9;
      AiJumpSolver.Inner_StreamUtil var13 = var4;

      for (int var14 = 0; var14 < 6 && var11 - var10 > 1; var14++) {
         int var15 = (var10 + var11) / 2;
         AiJumpSolver.Inner_StreamUtil var16 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(var0, var1, var2, var3, var15, var5, var6, var7);
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

   private static AiJumpSolver.Inner_StreamUtil putAt_IoUtil_JsonCache_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0,
      AiPhysics.Inner_StreamUtil var1,
      AiPhysics.Inner_DataUtil var2,
      AiPhysics.Inner_DataUtil var3,
      int var4,
      AiJumpSolver.Inner_NetHelper var5,
      BlockPos var6,
      double var7
   ) {
      if (putAt(var0, var1)) {
         return null;
      }

      AiPhysics.Inner_StreamUtil var9 = AiPhysics.putAt(var1, var2.withJump(true), var0);
      if (AiPhysics.fill(var0, var1, var9)) {
         return new AiJumpSolver.Inner_StreamUtil(var9, 0, false, var4, var5.along(var9), -1.0, true, true);
      }

      if (putAt(var0, var9)) {
         return null;
      }

      if (var9.onGround() && var9.y() <= var1.y() + 1.0E-6) {
         return null;
      }

      AiPhysics.Inner_DataUtil var10 = new AiPhysics.Inner_DataUtil(0.0, 0.0, false, false, false, var3.yaw());
      boolean var11 = var9.horizontalCollision();
      boolean var12 = var11 || var9.verticalCollision() && !var9.onGround();
      double var13 = Math.max(var1.y(), var9.y());

      for (int var15 = 1; var15 <= 40; var15++) {
         AiPhysics.Inner_StreamUtil var16 = AiPhysics.putAt(var9, var15 <= var4 ? var3 : var10, var0);
         if (AiPhysics.fill(var0, var9, var16)) {
            return new AiJumpSolver.Inner_StreamUtil(var16, var15, false, var4, var5.along(var16), -1.0, true, true);
         }

         var9 = var16;
         var13 = Math.max(var13, var9.y());
         if (putAt(var0, var9)) {
            return null;
         }

         var11 |= var9.horizontalCollision();
         var12 |= var11 || var9.verticalCollision() && !var9.onGround();
         if (putAt(var9)) {
            if (var13 - var9.y() > var0.putAt_D() + 1.0E-5) {
               return new AiJumpSolver.Inner_StreamUtil(var9, var15, true, var4, var5.along(var9), -1.0, var12, var11);
            }

            double var17 = var9.y() <= var7 + 0.02 && var9.y() >= var7 - 0.55 ? putAt_D_TokenCore_TaskStat_2(var0, var9, var6, var7, var3.yaw()) : -1.0;
            if (var17 >= -0.04
               && var12
               && var15 <= 4
               && !putAt_Z_D(var9.x(), var9.y(), var9.z(), var6, var7)
               && !putAt_Z_TokenCore_TaskStat_4(var0, var9, var6, var7)) {
               var17 = -1.0;
            }

            return new AiJumpSolver.Inner_StreamUtil(var9, var15, true, var4, var5.along(var9), var17, var12, var11);
         }

         if (!var9.onGround() && var9.y() < var7 - 4.0) {
            return new AiJumpSolver.Inner_StreamUtil(var9, var15, false, var4, var5.along(var9), -1.0, var12, var11);
         }
      }

      return new AiJumpSolver.Inner_StreamUtil(var9, 40, false, var4, var5.along(var9), -1.0, var12, var11);
   }

   static boolean putAt(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1) {
      return var0.state((int)Math.floor(var1.x()), (int)Math.floor(var1.y()), (int)Math.floor(var1.z()));
   }

   public static double putAt_D_TokenCore_TaskStat_2(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, double var3, float var5) {
      if (AiPhysics.fill(var0, var1, var1)) {
         return -1.0;
      }

      double var6 = putAt_D_TokenCore_TaskStat(var0, var1.x(), var1.z(), var2, var3);
      if (var6 >= -0.04) {
         AiPhysics.Inner_StreamUtil var26 = var1;
         AiPhysics.Inner_DataUtil var27 = new AiPhysics.Inner_DataUtil(0.0, 0.0, false, false, false, var5);

         for (int var10 = 0; var10 < 64 && (var26.speed() > 0.01 || !var26.onGround()); var10++) {
            AiPhysics.Inner_StreamUtil var28 = AiPhysics.putAt(var26, var27, var0);
            if (AiPhysics.fill(var0, var26, var28) || Math.abs(var28.y() - var1.y()) > 0.61) {
               return -1.0;
            }

            var26 = var28;
         }

         if (var26.onGround() && !(var26.speed() > 0.01)) {
            return putAt_Z_TokenCore_TaskStat_2(var0, (int)Math.floor(var26.x()), (int)Math.floor(var26.z()), var26.y())
               ? var6
               : Math.min(var6, putAt_D_TokenCore_TaskStat(var0, var26.x(), var26.z(), var2, var26.y()));
         } else {
            return -1.0;
         }
      } else {
         BlockPos var8 = putAt_BlockPos_TokenCore_TaskStat(var0, var1.x(), var1.z(), var2, var3);
         double var9 = -Math.sin(Math.toRadians(var5));
         double var11 = Math.cos(Math.toRadians(var5));
         boolean var13 = Math.abs(var9) >= Math.abs(var11);
         int var14 = var13 ? (int)Math.signum(var9) : 0;
         int var15 = var13 ? 0 : (int)Math.signum(var11);
         boolean var16 = putAt_Z_TokenCore_TaskStat_2(var0, var8.getX() + var14, var8.getZ() + var15, var3);
         float var17 = AiPhysics.putAt_F_D(0.0, 0.0, var14, var15);
         AiPhysics.Inner_DataUtil var18 = var16
            ? new AiPhysics.Inner_DataUtil(1.0, 0.0, false, false, false, var17)
            : new AiPhysics.Inner_DataUtil(0.0, 0.0, false, false, false, var5);
         AiPhysics.Inner_StreamUtil var19 = var1;
         double var20 = var6;

         for (int var22 = 0; var22 < 6; var22++) {
            AiPhysics.Inner_StreamUtil var23 = AiPhysics.putAt(var19, var18, var0);
            if (AiPhysics.fill(var0, var19, var23)) {
               return -1.0;
            }

            var19 = var23;
            if (!var19.onGround() || Math.abs(var19.y() - var1.y()) > 0.02) {
               return -1.0;
            }

            double var24 = putAt_D_TokenCore_TaskStat(var0, var19.x(), var19.z(), var2, var3);
            if (var16) {
               var20 = Math.max(var20, var24);
               if (var20 >= 0.1) {
                  return putAt_D_TokenCore_TaskStat_2(var0, var19, var2, var3, var5);
               }
            } else {
               if (Math.hypot(var19.vx(), var19.vz()) < 0.01) {
                  return var24;
               }

               var20 = var24;
            }
         }

         return putAt_D_TokenCore_TaskStat(var0, var19.x(), var19.z(), var2, var3) >= -0.04
            ? Math.min(var20, putAt_D_TokenCore_TaskStat_2(var0, var19, var2, var3, var5))
            : -1.0;
      }
   }

   static double putAt_D_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, double var1, double var3, BlockPos var5, double var6) {
      BlockPos var8 = putAt_BlockPos_TokenCore_TaskStat(var0, var1, var3, var5, var6);
      int var9 = var8.getX();
      int var10 = var8.getZ();
      double var11 = 0.3;
      double var13 = var1 - var11 - var9;
      double var15 = var9 + 1 - (var1 + var11);
      double var17 = var3 - var11 - var10;
      double var19 = var10 + 1 - (var3 + var11);
      if (var13 < 0.5 && putAt_Z_TokenCore_TaskStat_2(var0, var9 - 1, var10, var6)) {
         var13++;
      }

      if (var15 < 0.5 && putAt_Z_TokenCore_TaskStat_2(var0, var9 + 1, var10, var6)) {
         var15++;
      }

      if (var17 < 0.5 && putAt_Z_TokenCore_TaskStat_2(var0, var9, var10 - 1, var6)) {
         var17++;
      }

      if (var19 < 0.5 && putAt_Z_TokenCore_TaskStat_2(var0, var9, var10 + 1, var6)) {
         var19++;
      }

      double var21 = Math.min(Math.min(var13, var15), Math.min(var17, var19));
      if (!(var21 >= -0.04) && (int)Math.floor(var1) == var5.getX() && (int)Math.floor(var3) == var5.getZ()) {
         boolean var23 = !putAt_Z_TokenCore_TaskStat(var0, var5.getX() + 0.5, var6, var5.getZ() + 0.5);
         AABB var24 = new AABB(var1 - var11, var6 - 0.02, var3 - var11, var1 + var11, var6 - 0.001, var3 + var11);

         for (AABB var26 : var0.collisions(var24)) {
            if (!(Math.abs(var26.maxY - var6) > 0.01)) {
               boolean var27 = var26.getXsize() >= 0.99 && var26.getZsize() >= 0.99;
               if ((!var27 || var23) && putAt_Z_TokenCore_TaskStat(var0, var1, var6, var3)) {
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

   private static BlockPos putAt_BlockPos_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, double var1, double var3, BlockPos var5, double var6) {
      int var8 = (int)Math.floor(var1);
      int var9 = (int)Math.floor(var3);
      if (var8 == var5.getX() && var9 == var5.getZ()) {
         return var5;
      }

      boolean var10 = Math.abs(var8 - var5.getX()) <= 1 && Math.abs(var9 - var5.getZ()) <= 1;
      return var10 && putAt_Z_TokenCore_TaskStat_2(var0, var8, var9, var6) ? new BlockPos(var8, var5.getY(), var9) : var5;
   }

   static boolean putAt_Z_TokenCore_TaskStat_4(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, double var3) {
      double var5 = var2.getX() + 0.5;
      double var7 = var2.getZ() + 0.5;
      return putAt_Z_TokenCore_TaskStat(var0, var1.x(), var1.z(), var5, var7, var3)
         ? true
         : putAt_Z_TokenCore_TaskStat(var0, var1.x(), var1.z(), var5, var1.z(), var3) && putAt_Z_TokenCore_TaskStat(var0, var5, var1.z(), var5, var7, var3)
            || putAt_Z_TokenCore_TaskStat(var0, var1.x(), var1.z(), var1.x(), var7, var3) && putAt_Z_TokenCore_TaskStat(var0, var1.x(), var7, var5, var7, var3);
   }

   private static boolean putAt_Z_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, double var1, double var3, double var5, double var7, double var9) {
      int var11 = Math.max(1, (int)Math.ceil(Math.hypot(var5 - var1, var7 - var3) / 0.15));

      for (int var12 = 1; var12 <= var11; var12++) {
         double var13 = (double)var12 / var11;
         if (!putAt_Z_TokenCore_TaskStat(var0, var1 + (var5 - var1) * var13, var9, var3 + (var7 - var3) * var13)) {
            return false;
         }
      }

      return true;
   }

   public static boolean putAt_Z_TokenCore_TaskStat_2(AiPhysics.Inner_CachePool var0, int var1, int var2, double var3) {
      return var0 instanceof AiPlannerWorld var5 ? var5.putAt_Z_I(var1, var2, var3) : fill(var0, var1, var2, var3);
   }

   static boolean fill(AiPhysics.Inner_CachePool var0, int var1, int var2, double var3) {
      AiPhysics.Inner_StreamUtil var5 = new AiPhysics.Inner_StreamUtil(var1 + 0.5, var3, var2 + 0.5, 0.0, 0.0, 0.0, true, 0, false, false);
      if (AiPhysics.fill(var0, var5, var5)) {
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

   static boolean putAt(AiPhysics.Inner_StreamUtil var0) {
      return var0.onGround() && var0.vy() <= 0.0;
   }

   public static boolean putAt_Z_D(double var0, double var2, double var4, BlockPos var6, double var7) {
      return !(var2 > var7 + 0.02) && !(var2 < var7 - 0.55) ? putAt_D_D(var0, var4, var6) >= -0.12 : false;
   }

   public static double putAt_D_D(double var0, double var2, BlockPos var4) {
      double var5 = 0.3;
      double var7 = var0 - var5 - var4.getX();
      double var9 = var4.getX() + 1 - (var0 + var5);
      double var11 = var2 - var5 - var4.getZ();
      double var13 = var4.getZ() + 1 - (var2 + var5);
      return Math.min(Math.min(var7, var9), Math.min(var11, var13));
   }

   public static AiJumpSolver.UiHelper putAt_IoUtil_IoPool_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, boolean var3, BlockPos var4, double var5
   ) {
      return putAt_IoUtil_IoPool_TokenCore_TaskStat(var0, var1, var2, var3, var4, var5, 40);
   }

   static AiJumpSolver.UiHelper fill(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, boolean var3, BlockPos var4, double var5) {
      if (var5 - var1.y() <= 0.6) {
         return null;
      }

      AiPhysics.Inner_DataUtil var7 = AiPhysics.Inner_DataUtil.run(var2, var3);
      if (!AiPhysics.putAt(var1, var7, var0).horizontalCollision()) {
         return null;
      }

      double var8 = var4.getX() + 0.5 - var1.x();
      double var10 = var4.getZ() + 0.5 - var1.z();
      double var12 = Math.hypot(var8, var10);
      if (var12 < 1.0E-6) {
         return null;
      }

      AiJumpSolver.Inner_NetHelper var14 = new AiJumpSolver.Inner_NetHelper(var1.x(), var1.z(), var8 / var12, var10 / var12, var12);
      AiJumpSolver.Inner_StreamUtil var15 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(var0, var1, var7, var7, 2147483647, var14, var4, var5);
      if (var15 != null && var15.settled() && !(var15.margin() < -0.04)) {
         if (!(var15.margin() < 0.14)
            || var1.horizontalCollision() && var1.speed() < 0.025 && putAt_Z_TokenCore_TaskStat_2(var0, var4.getX(), var4.getZ(), var5)) {
            return putAt_Z_TokenCore_TaskStat_4(var0, var15.end(), var4, var5)
               ? new AiJumpSolver.UiHelper(0, 0.0, 2147483647, var15.margin(), var3, var15.ticks() + 1)
               : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static double putAt_D_TokenCore_TaskStat_2(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, AiJumpSolver.UiHelper var3, BlockPos var4, double var5
   ) {
      return putAt_D_TokenCore_TaskStat_2(var0, var1, var2, var3, var4, var5, false);
   }

   static double putAt_D_TokenCore_TaskStat_2(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, AiJumpSolver.UiHelper var3, BlockPos var4, double var5, boolean var7
   ) {
      double var8 = var4.getX() + 0.5 - var1.x();
      double var10 = var4.getZ() + 0.5 - var1.z();
      double var12 = Math.hypot(var8, var10);
      if (var12 < 1.0E-6) {
         return -1.0;
      } else {
         AiJumpSolver.Inner_NetHelper var14 = new AiJumpSolver.Inner_NetHelper(var1.x(), var1.z(), var8 / var12, var10 / var12, var12);
         AiPhysics.Inner_DataUtil var15 = AiPhysics.Inner_DataUtil.run(var2, var3.sprint());
         AiJumpSolver.Inner_StreamUtil var16 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(
            var0, var1, var15.withStrafe(var3.strafe()), var15, var3.coastAfter(), var14, var4, var5
         );
         if (var16 != null && var16.settled()) {
            return var7 && !var16.bumped() && var16.margin() < 0.065 ? putAt_D_TokenCore_TaskStat_2(var0, var16.end(), var4, var5) : var16.margin();
         } else {
            return -1.0;
         }
      }
   }

   static boolean putAt_Z_TokenCore_TaskStat_3(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, AiJumpSolver.UiHelper var3) {
      AiPhysics.Inner_DataUtil var4 = AiPhysics.Inner_DataUtil.run(var2, var3.sprint());
      AiPhysics.Inner_DataUtil var5 = new AiPhysics.Inner_DataUtil(0.0, 0.0, false, false, false, var2);
      AiPhysics.Inner_StreamUtil var6 = AiPhysics.putAt(var1, var4.withStrafe(var3.strafe()).withJump(true), var0);

      for (int var7 = 1; var7 <= 40; var7++) {
         if (var6.horizontalCollision() && !var6.onGround()) {
            return true;
         }

         if (putAt(var6)) {
            return false;
         }

         var6 = AiPhysics.putAt(var6, var7 <= var3.coastAfter() ? var4 : var5, var0);
      }

      return false;
   }

   static float putAt_F_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, AiJumpSolver.UiHelper var3, BlockPos var4, double var5
   ) {
      if (!putAt_Z_TokenCore_TaskStat_3(var0, var1, var2, var3)) {
         return 0.0F / 0.0F;
      }

      for (float var10 : putAt_FArr) {
         float var11 = var2 + var10;
         if (!putAt_Z_TokenCore_TaskStat_3(var0, var1, var11, var3) && putAt_D_TokenCore_TaskStat_2(var0, var1, var11, var3, var4, var5) >= -0.04) {
            return var11;
         }
      }

      return 0.0F / 0.0F;
   }

   static double putAt_D_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, AiJumpSolver.UiHelper var3, int var4, BlockPos var5, double var6
   ) {
      AiPhysics.Inner_StreamUtil var8 = var1;
      AiPhysics.Inner_DataUtil var9 = AiPhysics.Inner_DataUtil.run(var2, var3.sprint()).withStrafe(var3.strafe());

      for (int var10 = 0; var10 < var4; var10++) {
         var8 = AiPhysics.putAt(var8, var9, var0);
         if (!var8.onGround() || AiPhysics.fill(var0, var1, var8)) {
            return -1.0;
         }

         var1 = var8;
      }

      AiJumpSolver.UiHelper var11 = new AiJumpSolver.UiHelper(0, var3.strafe(), var3.coastAfter(), 0.0, var3.sprint(), var3.flightTicks());
      return putAt_D_TokenCore_TaskStat_2(var0, var8, var2, var11, var5, var6);
   }

   static boolean putAt_Z_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, AiJumpSolver.Inner_StateCache var1, double var2) {
      double var4 = Math.toRadians(var1.yaw());
      AiPhysics.Inner_StreamUtil var6 = new AiPhysics.Inner_StreamUtil(
         var1.startX(), var2, var1.startZ(), -Math.sin(var4) * var1.approach(), -0.0784, Math.cos(var4) * var1.approach(), true, 0, false, false
      );
      AiPhysics.Inner_DataUtil var7 = AiPhysics.Inner_DataUtil.run(var1.yaw(), var1.sprint());

      for (int var8 = 0; var8 < var1.runTicks(); var8++) {
         var6 = AiPhysics.putAt(var6, var7, var0);
         if (!var6.onGround()
            || var6.horizontalCollision()
            || Math.abs(var6.y() - var2) > 0.02
            || !putAt_Z_TokenCore_TaskStat(var0, var6.x(), var6.y(), var6.z())) {
            return false;
         }
      }

      return true;
   }

   static AiJumpSolver.UiHelper readEx(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, boolean var3, BlockPos var4, double var5) {
      return putAt_IoUtil_IoPool_TokenCore_TaskStat(var0, var1, var2, var3, var4, var5, 40, true);
   }

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, double var3
   ) {
      if (var1.onGround() && var1.pose() == 0 && !(Math.abs(var1.y() - var3) > 0.55)) {
         double var5 = var2.getX() + 0.5;
         double var7 = var2.getZ() + 0.5;
         double var9 = var5 - var1.x();
         double var11 = var7 - var1.z();
         double var13 = Math.hypot(var9, var11);
         if (!(var13 < 2.0) && !(var13 > 8.0)) {
            float var15 = AiPhysics.putAt_F_D(var1.x(), var1.z(), var5, var7);
            if (var1.vx() * var9 + var1.vz() * var11 >= -0.035 * var13) {
               AiJumpSolver.Inner_TokenCodec[] var16 = new AiJumpSolver.Inner_TokenCodec[readEx_IArr.length];

               for (int var17 = 0; var17 < var16.length; var17++) {
                  var16[var17] = new AiJumpSolver.Inner_TokenCodec(null, 0, AiPhysics.Inner_DataUtil.run(var15 + readEx_IArr[var17], true));
               }

               AiJumpSolver.Inner_StateCache var35 = putAt_IoUtil_TextState_TokenCore_TaskStat(var0, var1, var2, var3, var16, 17, false);
               if (var35 != null) {
                  return var35;
               }
            }

            if (var1.speed() < 0.09) {
               return null;
            }

            double var34 = (var1.vx() * var11 - var1.vz() * var9) / var13;
            float var18 = var34 <= 0.0 ? -1.0F : 1.0F;
            AiJumpSolver.Inner_TokenCodec[] var19 = new AiJumpSolver.Inner_TokenCodec[24];
            int var20 = 0;

            for (int var24 : new int[]{4, 7, 10, 12}) {
               for (int var28 : new int[]{75, 138, 156}) {
                  AiPhysics.Inner_DataUtil var29 = AiPhysics.Inner_DataUtil.run(var15 + var18 * var28, true);

                  for (int var33 : new int[]{-5, 10}) {
                     var19[var20++] = new AiJumpSolver.Inner_TokenCodec(var29, var24, AiPhysics.Inner_DataUtil.run(var15 + var18 * var33, true));
                  }
               }
            }

            return putAt_IoUtil_TextState_TokenCore_TaskStat(var0, var1, var2, var3, var19, 33, true);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, double var3, AiJumpSolver.Inner_TokenCodec[] var5, int var6, boolean var7
   ) {
      double var8 = var2.getX() + 0.5;
      double var10 = var2.getZ() + 0.5;
      AiPhysics.Inner_StreamUtil[] var12 = new AiPhysics.Inner_StreamUtil[var5.length];
      Arrays.fill(var12, var1);
      AiJumpSolver.Inner_StateCache var13 = null;

      for (int var14 = 0; var14 <= var6; var14++) {
         boolean var15 = false;

         for (int var16 = 0; var16 < var12.length; var16++) {
            AiPhysics.Inner_StreamUtil var17 = var12[var16];
            if (var17 != null) {
               var15 = true;
               if ((var14 != 0 || var16 == 0) && var17.speed() >= 0.095) {
                  double var18 = var8 - var17.x();
                  double var20 = var10 - var17.z();
                  double var22 = Math.hypot(var18, var20);
                  if (var22 > 1.6 && var22 <= 5.2) {
                     AiJumpSolver.Inner_NetHelper var24 = new AiJumpSolver.Inner_NetHelper(var17.x(), var17.z(), var18 / var22, var20 / var22, var22);
                     double var25 = var18 - 8.0 * var17.vx();
                     double var27 = var20 - 8.0 * var17.vz();
                     if (var25 * var25 + var27 * var27 > 28.09) {
                        var12[var16] = putAt_TokenCore_TableCac_TokenCore_TaskStat(var0, var17, var5[var16].at(var14), var1.y());
                        continue;
                     }

                     float var29 = AiPhysics.putAt_F_D(0.0, 0.0, var25, var27);

                     for (int var33 : var7 ? prevEx_IArr : value_IArr) {
                        AiPhysics.Inner_DataUtil var34 = AiPhysics.Inner_DataUtil.run(var29 + var33, true);
                        AiJumpSolver.Inner_StreamUtil var35 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(var0, var17, var34, var34, 2147483647, var24, var2, var3);
                        if (var35 != null && var35.settled() && !(Math.abs(var35.end().y() - var3) > 0.02)) {
                           if (var35.margin() < -0.04) {
                              if (var13 != null) {
                                 continue;
                              }

                              if (var35.along() >= var22) {
                                 var35 = putAt_IoUtil_JsonCache_TokenCore_TaskStat_2(var0, var17, var34, var34, var35, var24, var2, var3);
                              }

                              if (var7 && !putAt(var35, var3)) {
                                 for (int var39 : prev_IArr) {
                                    AiJumpSolver.Inner_StreamUtil var40 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(
                                       var0, var17, var34, var34, var39, var24, var2, var3
                                    );
                                    if (putAt(var40, var3)) {
                                       var35 = var40;
                                       break;
                                    }
                                 }
                              }

                              if (!putAt(var35, var3)) {
                                 continue;
                              }
                           }

                           ArrayList var41 = new ArrayList(var14 + var35.ticks() + 1);

                           for (int var42 = 0; var42 < var14; var42++) {
                              var41.add(var5[var16].at(var42));
                           }

                           var41.add(var34.withJump(true));

                           for (int var43 = 1; var43 <= var35.ticks(); var43++) {
                              var41.add(var43 <= var35.coastAfter() ? var34 : AiPhysics.Inner_DataUtil.NONE.withYaw(var34.yaw()));
                           }

                           AiPhysics.Inner_StreamUtil var44 = var35.end();
                           AiJumpManeuver var45 = new AiJumpManeuver(var41, var1, var44, var35.margin(), var14);
                           if (!(var45.replay(var0, var1, 0, var2, var3) < -0.04)) {
                              AiJumpSolver.Inner_StateCache var46 = new AiJumpSolver.Inner_StateCache(
                                 ((AiPhysics.Inner_DataUtil)var41.getFirst()).yaw(),
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

               var12[var16] = putAt_TokenCore_TableCac_TokenCore_TaskStat(var0, var17, var5[var16].at(var14), var1.y());
            }
         }

         if (!var15 || var13 != null && var14 >= var13.runTicks() + 3) {
            break;
         }
      }

      return var13;
   }

   private static boolean putAt(AiJumpSolver.Inner_StreamUtil var0, double var1) {
      return var0 != null && var0.settled() && Math.abs(var0.end().y() - var1) <= 0.02 && var0.margin() >= -0.04;
   }

   private static AiPhysics.Inner_StreamUtil putAt_TokenCore_TableCac_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, AiPhysics.Inner_DataUtil var2, double var3
   ) {
      if (!putAt_Z_TokenCore_TaskStat(var0, var1.x(), var1.y(), var1.z())) {
         return null;
      }

      AiPhysics.Inner_StreamUtil var5 = AiPhysics.putAt(var1, var2, var0);
      return var5.onGround() && Math.abs(var5.y() - var3) <= 0.02 && !AiPhysics.fill(var0, var1, var5) ? var5 : null;
   }

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat_4(
      AiPhysics.Inner_CachePool var0, BlockPos var1, double var2, BlockPos var4, double var5, boolean var7
   ) {
      if (!var7) {
         return null;
      }

      double var8 = var1.getX() + 0.5;
      double var10 = var1.getZ() + 0.5;
      float var12 = AiPhysics.putAt_F_D(var8, var10, var4.getX() + 0.5, var4.getZ() + 0.5);
      float var13 = Math.round(var12 / 45.0F) * 45.0F;
      float var14 = Math.round(var12 / 5.0F) * 5.0F;
      double var15 = Math.toRadians(var13);
      double var17 = -Math.sin(var15);
      double var19 = Math.cos(var15);
      AiJumpSolver.Inner_StateCache var21 = null;

      for (double var25 : new double[]{0.0, 0.65, 1.0, 1.3, 1.6}) {
         double var27 = var8 - var17 * var25;
         double var29 = var10 - var19 * var25;
         if (putAt_Z_TokenCore_TaskStat(var0, var8, var10, var27, var29, var2)) {
            AiPhysics.Inner_StreamUtil var31 = AiPhysics.Inner_StreamUtil.standing(var27, var2, var29);

            for (float var35 : new float[]{0.0F, -5.0F, 5.0F, -10.0F, 10.0F, -15.0F, 15.0F}) {
               if (Thread.currentThread().isInterrupted()) {
                  return null;
               }

               AiPhysics.Inner_DataUtil var36 = AiPhysics.Inner_DataUtil.run(var14 + var35, true);
               AiPhysics.Inner_StreamUtil var37 = var31;

               for (int var38 = 0; var38 <= 20 && var37.onGround(); var38++) {
                  if (var37.speed() >= 0.11 && Math.hypot(var37.x() - var8, var37.z() - var10) <= 1.4) {
                     double var39 = var4.getX() + 0.5 - var37.x();
                     double var41 = var4.getZ() + 0.5 - var37.z();
                     double var43 = Math.hypot(var39, var41);
                     if (var43 > 2.0 && var43 <= 5.1) {
                        AiJumpSolver.Inner_NetHelper var45 = new AiJumpSolver.Inner_NetHelper(var37.x(), var37.z(), var39 / var43, var41 / var43, var43);

                        for (float var49 : new float[]{0.0F, -5.0F, 5.0F, -10.0F, 10.0F, -15.0F, 15.0F}) {
                           AiPhysics.Inner_DataUtil var50 = var36.withYaw(var36.yaw() + var49);
                           AiJumpSolver.Inner_StreamUtil var51 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(var0, var37, var50, var50, 2147483647, var45, var4, var5);
                           if (var51 != null) {
                              if (var51.along() > var43 && var51.margin() < 0.14) {
                                 var51 = putAt_IoUtil_JsonCache_TokenCore_TaskStat_2(var0, var37, var50, var50, var51, var45, var4, var5);
                              }

                              if (var51 != null && var51.settled() && !(var51.margin() < -0.04)) {
                                 ArrayList var52 = new ArrayList();

                                 for (int var53 = 0; var53 < var38; var53++) {
                                    var52.add(var36);
                                 }

                                 var52.add(var50.withJump(true));

                                 for (int var57 = 1; var57 <= var51.ticks(); var57++) {
                                    var52.add(var57 <= var51.coastAfter() ? var50 : AiPhysics.Inner_DataUtil.NONE.withYaw(var50.yaw()));
                                 }

                                 AiJumpManeuver var58 = new AiJumpManeuver(var52, var31, var51.end(), var51.margin(), var38);
                                 if (!(var58.replay(var0, var31, 0, var4, var5) < -0.04)) {
                                    AiPhysics.Inner_StreamUtil var54 = var51.end();
                                    AiJumpSolver.Inner_StateCache var55 = new AiJumpSolver.Inner_StateCache(
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
                                    if (var21 == null || putAt(var55, var21)) {
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

                  AiPhysics.Inner_StreamUtil var56 = AiPhysics.putAt(var37, var36, var0);
                  if (AiPhysics.fill(var0, var37, var56)) {
                     break;
                  }

                  var37 = var56;
               }
            }
         }
      }

      return var21;
   }

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat_6(
      AiPhysics.Inner_CachePool var0, BlockPos var1, double var2, BlockPos var4, double var5
   ) {
      double var7 = var1.getX() + 0.5;
      double var9 = var1.getZ() + 0.5;
      float var11 = AiPhysics.putAt_F_D(var7, var9, var4.getX() + 0.5, var4.getZ() + 0.5);
      float var12 = Math.round(var11 / 90.0F) * 90.0F;
      double var13 = Math.toRadians(var12);

      for (double var18 : new double[]{-0.45, 0.45}) {
         for (double var23 : new double[]{0.2, 0.0, -0.2}) {
            double var25 = var7 - Math.sin(var13) * var23 + Math.cos(var13) * var18;
            double var27 = var9 + Math.cos(var13) * var23 + Math.sin(var13) * var18;
            if (putAt_Z_TokenCore_TaskStat(var0, var25, var2, var27)) {
               AiPhysics.Inner_StreamUtil var29 = AiPhysics.Inner_StreamUtil.standing(var25, var2, var27);

               for (float var33 : new float[]{0.0F, -8.0F, 8.0F}) {
                  if (Thread.currentThread().isInterrupted()) {
                     return null;
                  }

                  AiPhysics.Inner_DataUtil var34 = AiPhysics.Inner_DataUtil.run(var12 + var33, true);
                  AiPhysics.Inner_StreamUtil var35 = var29;

                  for (int var36 = 0; var36 <= 8 && var35.onGround(); var36++) {
                     double var37 = var4.getX() + 0.5 - var35.x();
                     double var39 = var4.getZ() + 0.5 - var35.z();
                     double var41 = Math.hypot(var37, var39);
                     if (var41 >= 1.0 && var35.speed() >= 0.08) {
                        AiJumpSolver.Inner_NetHelper var43 = new AiJumpSolver.Inner_NetHelper(var35.x(), var35.z(), var37 / var41, var39 / var41, var41);
                        float var44 = Mth.wrapDegrees(var11 - var12);

                        for (float var48 : new float[]{30.0F, 40.0F, 50.0F}) {
                           AiPhysics.Inner_DataUtil var49 = var34.withYaw(var34.yaw() + Math.copySign(var48, var44));
                           AiJumpSolver.Inner_StreamUtil var50 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(var0, var35, var49, var49, 2147483647, var43, var4, var5);
                           if (var50 != null && var50.settled()) {
                              if (var50.margin() < -0.04 && var50.along() >= var41) {
                                 var50 = putAt_IoUtil_JsonCache_TokenCore_TaskStat_2(var0, var35, var49, var49, var50, var43, var4, var5);
                              }

                              if (var50 != null && !(var50.margin() < -0.04)) {
                                 ArrayList var51 = new ArrayList();

                                 for (int var52 = 0; var52 < var36; var52++) {
                                    var51.add(var34);
                                 }

                                 var51.add(var49.withJump(true));

                                 for (int var55 = 1; var55 <= var50.ticks(); var55++) {
                                    var51.add(var55 <= var50.coastAfter() ? var49 : AiPhysics.Inner_DataUtil.NONE.withYaw(var49.yaw()));
                                 }

                                 AiPhysics.Inner_StreamUtil var56 = var50.end();
                                 AiJumpManeuver var53 = new AiJumpManeuver(var51, var29, var56, var50.margin(), var36);
                                 if (var53.replay(var0, var29, 0, var4, var5) >= -0.04) {
                                    return new AiJumpSolver.Inner_StateCache(
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

                     AiPhysics.Inner_StreamUtil var54 = AiPhysics.putAt(var35, var34, var0);
                     if (AiPhysics.fill(var0, var35, var54)) {
                        break;
                     }

                     var35 = var54;
                  }
               }
            }
         }
      }

      return Math.abs(Mth.wrapDegrees(var11 - var12)) < 12.0F ? putAt_IoUtil_TextState_TokenCore_TaskStat_3(var0, var1, var2, var4, var5, var12) : null;
   }

   private static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat_3(
      AiPhysics.Inner_CachePool var0, BlockPos var1, double var2, BlockPos var4, double var5, float var7
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
            if (putAt_Z_TokenCore_TaskStat(var0, var28, var2, var30)) {
               AiPhysics.Inner_StreamUtil var32 = AiPhysics.Inner_StreamUtil.standing(var28, var2, var30);

               for (float var36 : new float[]{-8.0F, -4.0F, 0.0F, 4.0F, 8.0F}) {
                  if (Thread.currentThread().isInterrupted()) {
                     return null;
                  }

                  AiPhysics.Inner_DataUtil var37 = AiPhysics.Inner_DataUtil.run(var7 + var36, true);
                  AiPhysics.Inner_StreamUtil var38 = var32;
                  ArrayList<AiPhysics.Inner_DataUtil> var39 = new ArrayList<>();

                  for (int var40 = 0; var40 < 14 && var38.onGround(); var40++) {
                     if (var38.speed() >= 0.1) {
                        double var41 = AiJumpManeuver.maxFlightReach(var0, var38, var5, true);
                        if (var41 >= 0.0 && Math.hypot(var14 - var38.x(), var16 - var38.z()) <= var41 + 1.15) {
                           for (float var46 : new float[]{38.0F, 28.0F, 20.0F}) {
                              AiPhysics.Inner_DataUtil var47 = var37.withYaw(var37.yaw() + Math.copySign(var46, (float)var21)).withJump(true);
                              AiPhysics.Inner_StreamUtil var48 = AiPhysics.putAt(var38, var47, var0);
                              if (!var48.onGround() && !(var48.y() < var2 + 0.1) && !AiPhysics.fill(var0, var38, var48)) {
                                 AiPhysics.Inner_DataUtil var49 = var47.withJump(false);
                                 AiPhysics.Inner_StreamUtil[] var50 = new AiPhysics.Inner_StreamUtil[5];
                                 var50[0] = var48;

                                 for (int var51 = 1; var51 <= 4; var51++) {
                                    AiPhysics.Inner_StreamUtil var52 = var50[var51 - 1];
                                    if (var52 == null || putAt(var52)) {
                                       break;
                                    }

                                    AiPhysics.Inner_StreamUtil var53 = AiPhysics.putAt(var52, var49, var0);
                                    if (var53.y() < var5 - 0.02 || AiPhysics.fill(var0, var52, var53)) {
                                       break;
                                    }

                                    var50[var51] = var53;
                                 }

                                 for (int var54 : new int[]{2, 0, 4}) {
                                    AiPhysics.Inner_StreamUtil var55 = var50[var54];
                                    if (var55 != null) {
                                       if (putAt(var55)) {
                                          BlockPos var73 = BlockPos.containing(var55.x(), var4.getY(), var55.z());
                                          if (var73.equals(var4) && Math.abs(var55.y() - var5) <= 0.02) {
                                             double var74 = putAt_D_TokenCore_TaskStat_2(var0, var55, var4, var5, var49.yaw());
                                             if (var74 >= -0.04) {
                                                var var75 = new ArrayList<>(var39);
                                                var75.add(var47);

                                                for (int var76 = 0; var76 < var54; var76++) {
                                                   var75.add(var49);
                                                }

                                                AiJumpManeuver var77 = new AiJumpManeuver(var75, var32, var55, var74, var40);
                                                if (var77.replay(var0, var32, 0, var4, var5) >= -0.04) {
                                                   return new AiJumpSolver.Inner_StateCache(
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
                                             AiPhysics.Inner_DataUtil var60 = var37.withYaw(var37.yaw() - Math.copySign(var59, (float)var21));
                                             AiPhysics.Inner_StreamUtil var61 = var55;

                                             for (int var62 = var54; var62 < 18; var62++) {
                                                AiPhysics.Inner_StreamUtil var63 = AiPhysics.putAt(var61, var60, var0);
                                                if (var63.y() < var5 - 0.02 || AiPhysics.fill(var0, var61, var63)) {
                                                   break;
                                                }

                                                var61 = var63;
                                                if (putAt(var61)) {
                                                   BlockPos var64 = BlockPos.containing(var61.x(), var4.getY(), var61.z());
                                                   if (!var64.equals(var4) || !(Math.abs(var61.y() - var5) <= 0.02)) {
                                                      break;
                                                   }

                                                   double var65 = putAt_D_TokenCore_TaskStat_2(var0, var61, var4, var5, var60.yaw());
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
                                                      return new AiJumpSolver.Inner_StateCache(
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

                     AiPhysics.Inner_StreamUtil var69 = AiPhysics.putAt(var38, var37, var0);
                     if (AiPhysics.fill(var0, var38, var69)) {
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

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat_2(
      AiPhysics.Inner_CachePool var0, AiJumpSolver.Inner_StateCache var1, BlockPos var2, double var3, boolean var5
   ) {
      double var6 = Math.toRadians(var1.yaw());
      AiPhysics.Inner_StreamUtil var8 = var1.maneuver() == null
         ? new AiPhysics.Inner_StreamUtil(
            var1.landX(), var1.landY(), var1.landZ(), -Math.sin(var6) * var1.landSpeed(), -0.0784, Math.cos(var6) * var1.landSpeed(), true, 0, false, true
         )
         : var1.maneuver().landing();
      return putAt_IoUtil_TextState_TokenCore_TaskStat_5(var0, var8, var2, var3, var5);
   }

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat_5(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, double var3, boolean var5
   ) {
      return putAt_IoUtil_TextState_TokenCore_TaskStat_2(var0, var1, var2, var3, var5, 0.1);
   }

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat_4(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, double var3, float var5
   ) {
      return var1.speed() < 0.075 ? null : putAt_IoUtil_TextState_TokenCore_TaskStat(var0, var1, var2, var3, var5, 2);
   }

   static AiJumpSolver.Inner_StateCache fill_IoUtil_TextState_TokenCore_TaskStat_2(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, double var3, float var5
   ) {
      float var6 = AiPhysics.putAt_F_D(var1.x(), var1.z(), var2.getX() + 0.5, var2.getZ() + 0.5);
      if (Math.abs(Mth.wrapDegrees(var6 - var5)) > 30.0F) {
         return null;
      }

      AiPhysics.Inner_DataUtil var7 = AiPhysics.Inner_DataUtil.run(var6, true);
      AiStride.Inner_TableCache var8 = AiStride.putAt(var0, var1, var7);
      if (var8 == null) {
         return null;
      }

      AiJumpSolver.Inner_StateCache var9 = putAt_IoUtil_TextState_TokenCore_TaskStat(var0, var8.at(), var2, var3, var6, 5);
      if (var9 == null) {
         return null;
      }

      AiJumpSolver.UiHelper var10 = readEx(var0, var1, var6, true, var2, var3);
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
         : new AiJumpSolver.Inner_StateCache(
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

   static List<AiJumpSolver.Inner_StateCache> putAt_List_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, List<AiJumpSolver.Inner_HashPool> var3
   ) {
      List var4 = List.of(new AiJumpSolver.Inner_IoHelper(0, var2, var1, List.of()));

      for (int var5 = 0; var5 < var3.size(); var5++) {
         if (Thread.currentThread().isInterrupted()) {
            return List.of();
         }

         AiJumpSolver.Inner_HashPool var6 = (AiJumpSolver.Inner_HashPool)var3.get(var5);
         AiJumpSolver.Inner_HashPool var7 = var5 + 1 < var3.size() ? (AiJumpSolver.Inner_HashPool)var3.get(var5 + 1) : null;
         ArrayList<AiJumpSolver.Inner_IoHelper> var8 = new ArrayList<>();

         for (AiJumpSolver.Inner_IoHelper var10 : (Iterable<AiJumpSolver.Inner_IoHelper>)(Iterable<?>)(var4)) {
            for (AiJumpSolver.Inner_StateCache var12 : putAt_List_TokenCore_TaskStat(var0, var10.state(), var10.yaw(), var6.cell(), var6.surface(), var7)) {
               var var13 = new ArrayList<>(var10.flights());
               var13.add(var12);
               AiJumpManeuver var14 = var12.maneuver();
               var8.add(new AiJumpSolver.Inner_IoHelper(var10.cost() + var12.ticks(), var14.inputs().getLast().yaw(), var14.landing(), var13));
            }
         }

         if (var8.isEmpty()) {
            return List.of();
         }

         var8.sort(Comparator.comparingDouble(var1x -> var1x.cost() + putAt_D_TokenCore_TableCac(var1x.state(), var1x.yaw(), var7)));
         ArrayList var15 = new ArrayList(5);

         for (AiJumpSolver.Inner_IoHelper var17 : var8) {
            AiPhysics.Inner_StreamUtil var18 = var17.state();
            if (!var15.stream()
               .anyMatch(
                  var1x -> Math.hypot(((AiJumpSolver.Inner_IoHelper)var1x).state().x() - var18.x(), ((AiJumpSolver.Inner_IoHelper)var1x).state().z() - var18.z()) < 0.16
                     && Math.hypot(((AiJumpSolver.Inner_IoHelper)var1x).state().vx() - var18.vx(), ((AiJumpSolver.Inner_IoHelper)var1x).state().vz() - var18.vz()) < 0.04
               )) {
               var15.add(var17);
               if (var15.size() >= 5 || var7 == null) {
                  break;
               }
            }
         }

         var4 = var15;
      }

      return ((AiJumpSolver.Inner_IoHelper)var4.getFirst()).flights();
   }

   static AiJumpSolver.Inner_StateCache fill_IoUtil_TextState_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, BlockPos var3, double var4
   ) {
      if (putAt(var1) && !(var1.speed() < 0.1) && var1.pose() == 0) {
         var var6 = putAt_List_TokenCore_TaskStat(var0, var1, var2, var3, var4, null);
         AiJumpSolver.Inner_StateCache var7 = null;

         for (AiJumpSolver.Inner_StateCache var9 : var6) {
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

   private static double putAt_D_TokenCore_TableCac(AiPhysics.Inner_StreamUtil var0, float var1, AiJumpSolver.Inner_HashPool var2) {
      if (var2 == null) {
         return 0.0;
      }

      double var3 = var2.cell().getX() + 0.5 - var0.x();
      double var5 = var2.cell().getZ() + 0.5 - var0.z();
      double var7 = Math.hypot(var3, var5);
      double var9 = var7 > 0.01 ? (var0.vx() * var3 + var0.vz() * var5) / var7 : 0.0;
      float var11 = Mth.wrapDegrees(AiPhysics.putAt_F_D(0.0, 0.0, var3, var5) - var1);
      return 2.0 * var7 - 8.0 * var9 + 0.025 * Math.abs(var11);
   }

   private static List<AiJumpSolver.Inner_StateCache> putAt_List_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, BlockPos var3, double var4, AiJumpSolver.Inner_HashPool var6
   ) {
      double var7 = var3.getX() + 0.5 - var1.x();
      double var9 = var3.getZ() + 0.5 - var1.z();
      double var11 = Math.hypot(var7, var9);
      if (!(var11 < 1.35) && !(var11 > 7.7) && !(var4 - var1.y() > 1.29) && !(var1.y() - var4 > 2.25)) {
         AiJumpSolver.Inner_NetHelper var13 = new AiJumpSolver.Inner_NetHelper(var1.x(), var1.z(), var7 / var11, var9 / var11, var11);
         float var14 = AiPhysics.putAt_F_D(0.0, 0.0, var7, var9);
         ArrayList<AiJumpSolver.Inner_StateCache> var15 = new ArrayList<>();

         for (float var19 : fill_FArr) {
            float var20 = var14 + var19;
            AiPhysics.Inner_DataUtil var21 = AiPhysics.Inner_DataUtil.run(var20, true);
            AiPhysics.Inner_StreamUtil var22 = var1;
            ArrayList<AiPhysics.Inner_DataUtil> var23 = new ArrayList<>(8);
            float var24 = var2;
            float var25 = var6 == null ? 0.0F : Mth.wrapDegrees(AiPhysics.putAt_F_D(var3.getX(), var3.getZ(), var6.cell().getX(), var6.cell().getZ()) - var20);
            float var26 = var20 + Math.clamp(var25, -22.0F, 22.0F);
            var var27 = List.of(var21, AiPhysics.Inner_DataUtil.run(var26, true), var21.withStrafe(-1.0), var21.withStrafe(1.0));

            for (int var28 = 0; var28 <= 8; var28++) {
               if (Math.abs(Mth.wrapDegrees(var20 - var24)) <= 28.0F) {
                  for (AiPhysics.Inner_DataUtil var30 : var27) {
                     AiJumpSolver.Inner_StreamUtil var31 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(var0, var22, var21, var30, 2147483647, var13, var3, var4);
                     if (var31 != null && var31.along() > var11 && var31.margin() < 0.075) {
                        var31 = putAt_IoUtil_JsonCache_TokenCore_TaskStat_2(var0, var22, var21, var30, var31, var13, var3, var4);
                     }

                     if (var31 != null
                        && var31.settled()
                        && !var31.wall()
                        && !(var31.margin() < 0.075)
                        && !(Math.hypot(var31.end().x() - (var3.getX() + 0.5), var31.end().z() - (var3.getZ() + 0.5)) > 0.74)) {
                        ArrayList var32 = new ArrayList(var28 + var31.ticks() + 1);
                        var32.addAll(var23);
                        var32.add(var21.withJump(true));
                        AiPhysics.Inner_DataUtil var33 = AiPhysics.Inner_DataUtil.NONE.withYaw(var30.yaw());

                        for (int var34 = 0; var34 < var31.ticks(); var34++) {
                           var32.add(var34 < var31.coastAfter() ? var30 : var33);
                        }

                        AiPhysics.Inner_StreamUtil var38 = var31.end();
                        AiJumpManeuver var35 = new AiJumpManeuver(var32, var1, var38, var31.margin(), var28);
                        var15.add(
                           new AiJumpSolver.Inner_StateCache(
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
               AiPhysics.Inner_DataUtil var36 = AiPhysics.Inner_DataUtil.run(var24, true);
               AiPhysics.Inner_StreamUtil var37 = AiPhysics.putAt(var22, var36, var0);
               if (!var37.onGround() || var37.horizontalCollision() || Math.abs(var37.y() - var1.y()) > 0.02 || AiPhysics.fill(var0, var22, var37)) {
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

   static AiJumpSolver.Inner_StateCache readEx(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, double var3, float var5) {
      if (putAt(var1) && var1.pose() == 0 && !(var1.speed() < 0.1)) {
         float var6 = AiPhysics.putAt_F_D(var1.x(), var1.z(), var2.getX() + 0.5, var2.getZ() + 0.5);
         float var7 = AiPhysics.putAt_F_D(0.0, 0.0, var1.vx(), var1.vz());
         float var8 = Math.abs(Mth.wrapDegrees(var6 - var7));
         if (!(var8 < 45.0F) && !(var8 > 100.0F)) {
            ArrayList<AiPhysics.Inner_DataUtil> var9 = new ArrayList<>(2);
            AiPhysics.Inner_StreamUtil var10 = var1;
            float var11 = var5;

            for (int var12 = 0; var12 < 2; var12++) {
               var11 += Math.clamp(Mth.wrapDegrees(var6 - var11), -28.0F, 28.0F);
               float var13 = Mth.wrapDegrees(var6 - var11);
               AiPhysics.Inner_DataUtil var14 = AiPhysics.Inner_DataUtil.run(var11, true).withStrafe(Math.abs(var13) > 22.5 ? -Math.signum(var13) : 0.0);
               AiPhysics.Inner_StreamUtil var15 = AiPhysics.putAt(var10, var14, var0);
               if (!var15.onGround()
                  || var15.horizontalCollision()
                  || Math.abs(var15.y() - var1.y()) > 0.02
                  || !putAt_Z_TokenCore_TaskStat(var0, var15.x(), var15.y(), var15.z())
                  || AiPhysics.fill(var0, var10, var15)) {
                  break;
               }

               var9.add(var14);
               var10 = var15;
               AiJumpSolver.Inner_StateCache var16 = putAt_IoUtil_TextState_TokenCore_TaskStat(var0, var10, var2, var3, var11, 3 - var12);
               if (var16 != null && !(Math.abs(Mth.wrapDegrees(var16.yaw() - var11)) > 34.0F)) {
                  int var17 = var16.runTicks() + var9.size();
                  var var18 = new ArrayList<>(var9);
                  var18.addAll(var16.maneuver().inputs());
                  AiJumpManeuver var19 = new AiJumpManeuver(var18, var1, var16.maneuver().landing(), var16.margin(), var17);
                  if (var19.replay(var0, var1, 0, var2, var3) >= 0.075) {
                     return new AiJumpSolver.Inner_StateCache(
                        ((AiPhysics.Inner_DataUtil)var9.getFirst()).yaw(),
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

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, double var3, float var5, int var6
   ) {
      if (putAt(var1) && var1.pose() == 0 && !var1.horizontalCollision()) {
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

            float var19 = AiPhysics.putAt_F_D(var1.x(), var1.z(), var2.getX() + 0.5, var2.getZ() + 0.5);
            AiJumpSolver.Inner_NetHelper var20 = new AiJumpSolver.Inner_NetHelper(var1.x(), var1.z(), var13, var15, var11);
            float var21 = Mth.wrapDegrees(var5 - var19);
            float[] var22 = new float[]{var19 + Math.clamp(var21, -15.0F, 15.0F), var19, var19 - 9.0F, var19 + 9.0F};
            AiJumpSolver.Inner_StateCache var23 = null;

            for (float var27 : var22) {
               AiPhysics.Inner_DataUtil var28 = AiPhysics.Inner_DataUtil.run(var27, true);
               var var29 = putAt_List_TokenCore_TaskStat_2(var0, var1, var28, var6);

               for (int var30 = 0; var30 < var29.size() && (var23 == null || var30 <= var23.runTicks()); var30++) {
                  AiPhysics.Inner_StreamUtil var31 = (AiPhysics.Inner_StreamUtil)var29.get(var30);
                  if (var31.horizontalCollision() || var31.speed() < var1.speed() * 0.7) {
                     break;
                  }

                  if (!(var31.vx() * var13 + var31.vz() * var15 < 0.035)) {
                     for (int var35 : new int[]{2147483647, 5, 8}) {
                        AiJumpSolver.Inner_StreamUtil var36 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(var0, var31, var28, var28, var35, var20, var2, var3);
                        if (var36 != null
                           && var36.settled()
                           && !var36.bumped()
                           && !(var36.margin() < 0.075)
                           && putAt_Z_D(var36.end().x(), var36.end().y(), var36.end().z(), var2, var3)
                           && (var23 == null || var30 != var23.runTicks() || !(var36.end().speed() <= var23.landSpeed() + 0.015))) {
                           ArrayList var37 = new ArrayList();

                           for (int var38 = 0; var38 < var30; var38++) {
                              var37.add(var28);
                           }

                           var37.add(var28.withJump(true));

                           for (int var40 = 1; var40 <= var36.ticks(); var40++) {
                              var37.add(var40 <= var36.coastAfter() ? var28 : AiPhysics.Inner_DataUtil.NONE.withYaw(var27));
                           }

                           AiPhysics.Inner_StreamUtil var41 = var36.end();
                           AiJumpManeuver var39 = new AiJumpManeuver(var37, var1, var41, var36.margin(), var30);
                           if (!(var39.replay(var0, var1, 0, var2, var3) < 0.075)) {
                              var23 = new AiJumpSolver.Inner_StateCache(
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

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat_2(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, double var3, boolean var5, double var6
   ) {
      double var8 = var2.getX() + 0.5 - var1.x();
      double var10 = var2.getZ() + 0.5 - var1.z();
      double var12 = Math.hypot(var8, var10);
      if (putAt(var1) && !(var12 < 2.0) && !((var1.vx() * var8 + var1.vz() * var10) / var12 < var6)) {
         AiJumpSolver.Inner_NetHelper var14 = new AiJumpSolver.Inner_NetHelper(var1.x(), var1.z(), var8 / var12, var10 / var12, var12);
         float var15 = AiPhysics.putAt_F_D(var1.x(), var1.z(), var2.getX() + 0.5, var2.getZ() + 0.5);

         for (float var19 : new float[]{0.0F, -5.0F, 5.0F, -10.0F, 10.0F, -15.0F, 15.0F}) {
            float var20 = var15 + var19;
            AiJumpSolver.UiHelper var21 = putAt_IoUtil_IoPool_TokenCore_TaskStat(var0, var1, var20, var5, var2, var3, 3);
            if (!(var21.margin() < -0.04)) {
               AiPhysics.Inner_DataUtil var22 = AiPhysics.Inner_DataUtil.run(var20, var5);
               AiPhysics.Inner_DataUtil var23 = var22.withStrafe(var21.strafe());
               var var24 = putAt_List_TokenCore_TaskStat_2(var0, var1, var23, var21.runTicks());
               if (var24.size() > var21.runTicks()) {
                  AiJumpSolver.Inner_StreamUtil var25 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(
                     var0, (AiPhysics.Inner_StreamUtil)var24.getLast(), var23, var22, var21.coastAfter(), var14, var2, var3
                  );
                  if (var25 != null && var25.settled() && !(var25.margin() < -0.04)) {
                     ArrayList var26 = new ArrayList();

                     for (int var27 = 0; var27 < var21.runTicks(); var27++) {
                        var26.add(var23);
                     }

                     var26.add(var23.withJump(true));

                     for (int var28 = 1; var28 <= var25.ticks(); var28++) {
                        var26.add(var28 <= var25.coastAfter() ? var22 : AiPhysics.Inner_DataUtil.NONE.withYaw(var20));
                     }

                     AiJumpManeuver var29 = new AiJumpManeuver(var26, var1, var25.end(), var25.margin(), var21.runTicks());
                     return new AiJumpSolver.Inner_StateCache(
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

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiJumpSolver.Inner_StateCache var1, BlockPos var2, double var3, double var5
   ) {
      if (var1.maneuver() == null && var1.sprint() && !var1.running() && var1.coastAfter() != 2147483647) {
         AiPhysics.Inner_DataUtil var7 = AiPhysics.Inner_DataUtil.run(var1.yaw(), true);
         AiPhysics.Inner_StreamUtil var8 = AiPhysics.Inner_StreamUtil.standing(var1.startX(), var3, var1.startZ());
         var var9 = putAt_List_TokenCore_TaskStat_2(var0, var8, var7, var1.runTicks());
         if (var9.size() <= var1.runTicks()) {
            return null;
         }

         double var10 = var2.getX() + 0.5 - var8.x();
         double var12 = var2.getZ() + 0.5 - var8.z();
         double var14 = Math.hypot(var10, var12);
         if (var14 < 2.0) {
            return null;
         }

         AiJumpSolver.Inner_NetHelper var16 = new AiJumpSolver.Inner_NetHelper(var8.x(), var8.z(), var10 / var14, var12 / var14, var14);
         AiJumpSolver.Inner_StreamUtil var17 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(
            var0, (AiPhysics.Inner_StreamUtil)var9.getLast(), var7, var7, 2147483647, var16, var2, var5
         );
         if (var17 != null
            && var17.settled()
            && !(var17.end().speed() < var1.landSpeed() + 0.04)
            && !(Math.abs(var17.end().y() - var5) > 0.02)
            && putAt_Z_D(var17.end().x(), var17.end().y(), var17.end().z(), var2, var5)
            && putAt_Z_TokenCore_TaskStat(var0, var17.end().x(), var5, var17.end().z())) {
            ArrayList var18 = new ArrayList();

            for (int var19 = 0; var19 < var1.runTicks(); var19++) {
               var18.add(var7);
            }

            var18.add(var7.withJump(true));

            for (int var20 = 0; var20 < var17.ticks(); var20++) {
               var18.add(var7);
            }

            AiJumpManeuver var21 = new AiJumpManeuver(var18, var8, var17.end(), var17.margin(), var1.runTicks());
            return new AiJumpSolver.Inner_StateCache(
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

   public static AiJumpSolver.UiHelper putAt_IoUtil_IoPool_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, boolean var3, BlockPos var4, double var5, int var7
   ) {
      return putAt_IoUtil_IoPool_TokenCore_TaskStat(var0, var1, var2, var3, var4, var5, var7, false);
   }

   private static AiJumpSolver.UiHelper putAt_IoUtil_IoPool_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, boolean var3, BlockPos var4, double var5, int var7, boolean var8
   ) {
      double var9 = var4.getX() + 0.5;
      double var11 = var4.getZ() + 0.5;
      double var13 = var9 - var1.x();
      double var15 = var11 - var1.z();
      double var17 = Math.hypot(var13, var15);
      if (var17 < 1.0E-6) {
         return new AiJumpSolver.UiHelper(0, 0.0, 2147483647, -1.0, var3);
      }

      AiJumpSolver.Inner_NetHelper var19 = new AiJumpSolver.Inner_NetHelper(var1.x(), var1.z(), var13 / var17, var15 / var17, var17);
      AiPhysics.Inner_DataUtil var20 = AiPhysics.Inner_DataUtil.run(var2, var3);
      AiJumpSolver.Inner_StreamUtil var21 = putAt_IoUtil_JsonCache_TokenCore_TaskStat(var0, var1, var20, var20, 2147483647, var19, var4, var5);
      if (var21 != null && var21.settled() && !var21.bumped() && var21.margin() >= 0.08 && putAt_D_D(var21.end().x(), var21.end().z(), var4) >= 0.08) {
         return new AiJumpSolver.UiHelper(0, 0.0, 2147483647, var21.margin(), var3, var21.ticks() + 1);
      }

      AiJumpSolver.UiHelper var22 = null;
      double var23 = -1.0 / 0.0;

      for (double var28 : var8 ? new double[]{0.0} : new double[]{0.0, 1.0, -1.0}) {
         AiPhysics.Inner_DataUtil var30 = var20.withStrafe(var28);
         AiJumpSolver.Inner_CachePool var31 = putAt(
            var0, putAt_List_TokenCore_TaskStat_2(var0, var1, var30, Math.clamp(var7, 0, 40)), var30, var20, var19, var4, var5, true, false
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

   public static AiJumpSolver.Inner_DataUtil putAt_IoUtil_CacheHelper_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, boolean var3, BlockPos var4, double var5
   ) {
      return putAt_IoUtil_CacheHelper_TokenCore_TaskStat_2(var0, var1, var2, var3, var4, var5, (AiJumpSolver.Inner_BufferHelper)null);
   }

   public static AiJumpSolver.Inner_DataUtil putAt_IoUtil_CacheHelper_TokenCore_TaskStat_3(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, boolean var3, BlockPos var4, double var5, BlockPos var7
   ) {
      return putAt_IoUtil_CacheHelper_TokenCore_TaskStat_2(var0, var1, var2, var3, var4, var5, var7 == null ? null : AiJumpSolver.Inner_BufferHelper.walk(var7, var5));
   }

   public static AiJumpSolver.Inner_DataUtil putAt_IoUtil_CacheHelper_TokenCore_TaskStat_2(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, boolean var3, BlockPos var4, double var5, AiJumpSolver.Inner_BufferHelper var7
   ) {
      return putAt_IoUtil_CacheHelper_TokenCore_TaskStat(var0, var1, var2, var3, var4, var5, var7, false);
   }

   static AiJumpSolver.Inner_DataUtil putAt_IoUtil_CacheHelper_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0,
      AiPhysics.Inner_StreamUtil var1,
      float var2,
      boolean var3,
      BlockPos var4,
      double var5,
      AiJumpSolver.Inner_BufferHelper var7,
      boolean var8
   ) {
      AiJumpSolver.Inner_DataUtil var9 = null;
      double var10 = -1.0 / 0.0;

      for (double var15 : new double[]{1.0, 0.0, -1.0}) {
         if (var15 < 0.0 && var9 != null && var9.margin() >= -0.12) {
            break;
         }

         for (double var20 : new double[]{0.0, 1.0, -1.0}) {
            AiPhysics.Inner_DataUtil var22 = new AiPhysics.Inner_DataUtil(var15, var20, false, var3 && var15 > 0.0, false, var2);
            AiPhysics.Inner_StreamUtil var23 = var1;
            double var24 = -1.0;
            boolean var26 = false;
            boolean var27 = false;

            for (int var28 = 1; var28 <= 100; var28++) {
               AiPhysics.Inner_StreamUtil var29 = AiPhysics.putAt(var23, var22, var0);
               if (AiPhysics.fill(var0, var23, var29)) {
                  var26 = true;
                  break;
               }

               var23 = var29;
               var27 |= var23.horizontalCollision() && !var23.onGround();
               if (putAt(var0, var23)) {
                  break;
               }

               if (putAt(var23)) {
                  var24 = var23.y() <= var5 + 0.02 && var23.y() >= var5 - 0.55 ? putAt_D_TokenCore_TaskStat_2(var0, var23, var4, var5, var2) : -1.0;
                  if (var8 && var24 < 0.065) {
                     var24 = putAt_D_TokenCore_TaskStat_2(var0, var23, var4, var5);
                  }
                  break;
               }

               if (!var23.onGround() && var23.vy() < 0.0 && var23.y() < var5 - 4.0) {
                  break;
               }
            }

            if (!var26) {
               boolean var36 = putAt_Z_D(var23.x(), var23.y(), var23.z(), var4, var5) || var8 && putAt_Z_TokenCore_TableCac(var23, var4, var5);
               boolean var37 = var7 != null && var24 >= -0.12 && (var7.nextJump() == null || var15 > 0.0 && var20 == 0.0) && var7.proves(var0, var23);
               if (var37) {
                  var24 = Math.max(var24, 0.14);
               }

               double var30 = !(var24 >= -0.12) || !var36 && !var37 ? 0.0 : 2.0;
               if (var15 > 0.0 && var20 == 0.0 && var24 >= 0.08 && (var36 || var37) && !var27) {
                  return new AiJumpSolver.Inner_DataUtil(var15, var20, var24);
               }

               boolean var32 = var24 >= -0.12;
               double var33 = var30 + var24 - (var20 == 0.0 ? 0.0 : 0.02) - (var15 > 0.0 ? 0.0 : 0.01) - (var27 && var32 ? 2.5 : 0.0);
               boolean var35 = var9 != null && var9.margin() >= -0.12;
               if (var32 != var35 ? var32 : var33 > var10) {
                  var10 = var33;
                  var9 = new AiJumpSolver.Inner_DataUtil(var15, var20, var24);
               }
            }
         }
      }

      return var9 == null ? new AiJumpSolver.Inner_DataUtil(0.0, 0.0, -1.0) : var9;
   }

   public static AiJumpSolver.Inner_DataUtil putAt_IoUtil_CacheHelper_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, boolean var3, BlockPos var4, double var5, int var7
   ) {
      if (var7 == 2147483647) {
         return null;
      }

      AiPhysics.Inner_StreamUtil var8 = var1;

      for (int var9 = 1; var9 <= 100; var9++) {
         AiPhysics.Inner_DataUtil var10 = new AiPhysics.Inner_DataUtil(var9 <= var7 ? 1.0 : 0.0, 0.0, false, var3 && var9 <= var7, false, var2);
         AiPhysics.Inner_StreamUtil var11 = AiPhysics.putAt(var8, var10, var0);
         if (AiPhysics.fill(var0, var8, var11) || putAt(var0, var11)) {
            return null;
         }

         var8 = var11;
         if (putAt(var8)) {
            double var12 = var8.y() <= var5 + 0.02 && var8.y() >= var5 - 0.55 ? putAt_D_TokenCore_TaskStat_2(var0, var8, var4, var5, var2) : -1.0;
            return var12 >= -0.12 ? new AiJumpSolver.Inner_DataUtil(var7 > 0 ? 1.0 : 0.0, 0.0, var12) : null;
         }

         if (var8.y() < var5 - 4.0) {
            return null;
         }
      }

      return null;
   }

   public static double putAt_D_D_2(double var0, boolean var2, boolean var3) {
      return putAt_D_D(var0, var2, var3, 0.1);
   }

   static double putAt_D_D(double var0, boolean var2, boolean var3, double var4) {
      int var6 = (int)Math.round(var0 * 4.0) * 8 + (var2 ? 4 : 0) + (var3 ? 2 : 0) + 1;
      if (Math.abs(var4 - 0.1) > 1.0E-6) {
         if (fill_ConcurrentHashMap.size() > 4096) {
            fill_ConcurrentHashMap.clear();
         }

         double var11 = Math.ceil(var4 * 10000.0 - 1.0E-6) / 10000.0;
         int var9 = (int)Math.floor(var0 * 16.0);
         AiJumpSolver.Inner_EventBus var10 = new AiJumpSolver.Inner_EventBus(var9 * 4 + (var2 ? 2 : 0) + (var3 ? 1 : 0), var11);
         return fill_ConcurrentHashMap.computeIfAbsent(var10, var5 -> putAt_D_D(var9 / 16.0, var2, var3, true, var11));
      } else {
         Double var7 = putAt_ConcurrentHashMap.get(var6);
         if (var7 != null) {
            return var7;
         }

         double var8 = putAt_D_D(var0, var2, var3, true, 0.1);
         putAt_ConcurrentHashMap.put(var6, var8);
         return var8;
      }
   }

   public static double putAt(double var0) {
      int var2 = (int)Math.round(var0 * 4.0) * 8;
      Double var3 = putAt_ConcurrentHashMap.get(var2);
      if (var3 != null) {
         return var3;
      }

      double var4 = putAt_D_D(var0, false, false, false, 0.1);
      putAt_ConcurrentHashMap.put(var2, var4);
      return var4;
   }

   private static double putAt_D_D(double var0, boolean var2, boolean var3, boolean var4, double var5) {
      AiJumpSolver.Inner_RingUtil var7 = new AiJumpSolver.Inner_RingUtil(var5);
      double var8 = var3 ? 0.3 : 0.5;
      double var10 = var3 ? 0.28 * (var5 / 0.1) : 0.0;
      AiPhysics.Inner_StreamUtil var12 = new AiPhysics.Inner_StreamUtil(var8, 64.0, 0.5, var10, -0.0784, 0.0, true, 0, false, false);
      AiPhysics.Inner_DataUtil var13 = AiPhysics.Inner_DataUtil.run(-90.0F, var2);
      var var14 = putAt_List_TokenCore_TaskStat_2(var7, var12, var13, 12);
      AiPhysics.Inner_StreamUtil var15 = var4 ? (AiPhysics.Inner_StreamUtil)var14.get(var14.size() - 1) : (AiPhysics.Inner_StreamUtil)var14.get(0);
      AiPhysics.Inner_StreamUtil var16 = AiPhysics.putAt(var15, var13.withJump(true), var7);
      AiPhysics.Inner_DataUtil var17 = var4 ? var13 : new AiPhysics.Inner_DataUtil(0.0, 0.0, false, false, false, -90.0F);
      double var18 = 64.0 + var0;
      double var20 = -1.0;

      for (int var22 = 1; var22 <= 100; var22++) {
         AiPhysics.Inner_StreamUtil var23 = AiPhysics.putAt(var16, var17, var7);
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

   record Inner_SyncManager(double x, double z) {
   }

   public record Inner_BufferHelper(BlockPos cell, double surface, AiJumpSolver.Inner_JsonUtil nextJump) {
      static AiJumpSolver.Inner_BufferHelper walk(BlockPos var0, double var1) {
         return new AiJumpSolver.Inner_BufferHelper(var0, var1, null);
      }

      static AiJumpSolver.Inner_BufferHelper jump(BlockPos var0, double var1, BlockPos var3, double var4, float var6, boolean var7) {
         return new AiJumpSolver.Inner_BufferHelper(var3, var4, new AiJumpSolver.Inner_JsonUtil(var0, var1, var6, var7));
      }

      boolean proves(AiPhysics.Inner_CachePool var1, AiPhysics.Inner_StreamUtil var2) {
         if (!AiJumpSolver.putAt(var2)) {
            return false;
         } else if (this.nextJump != null) {
            return !atCell(var2, this.nextJump.from(), this.nextJump.fromSurface())
               ? false
               : AiJumpSolver.putAt_IoUtil_IoPool_TokenCore_TaskStat(var1, var2, this.nextJump.yaw(), this.nextJump.sprint(), this.cell, this.surface, 6).margin()
                  >= -0.04;
         } else {
            return atCell(var2, this.cell, this.surface) && AiJumpSolver.putAt_Z_TokenCore_TaskStat_2(var1, this.cell.getX(), this.cell.getZ(), this.surface);
         }
      }

      private static boolean atCell(AiPhysics.Inner_StreamUtil var0, BlockPos var1, double var2) {
         return var0.y() <= var2 + 0.02 && var0.y() >= var2 - 0.55 && (int)Math.floor(var0.x()) == var1.getX() && (int)Math.floor(var0.z()) == var1.getZ();
      }
   }

   public record Inner_DataUtil(double forward, double strafe, double margin) {
   }

   private record Inner_EventBus(int arc, double speed) {
   }

   record Inner_HashPool(BlockPos cell, double surface) {
   }

   private record Inner_IoHelper(int cost, float yaw, AiPhysics.Inner_StreamUtil state, List<AiJumpSolver.Inner_StateCache> flights) {
   }

   public record UiHelper(int runTicks, double strafe, int coastAfter, double margin, boolean sprint, int flightTicks) {
      public UiHelper(int var1, double var2, int var4, double var5, boolean var7) {
         this(var1, var2, var4, var5, var7, 0);
      }
   }

   private record Inner_StreamUtil(
      AiPhysics.Inner_StreamUtil end, int ticks, boolean settled, int coastAfter, double along, double margin, boolean bumped, boolean wall
   ) {
   }

   record Inner_JsonEntry(float yaw, AiJumpSolver.UiHelper takeoff) {
   }

   public record Inner_JsonUtil(BlockPos from, double fromSurface, float yaw, boolean sprint) {
   }

   private record Inner_RingUtil(double movementSpeed) implements AiPhysics.Inner_CachePool {
      private static final AABB BLOCK = new AABB(-32.0, 63.0, -32.0, 1.0, 64.0, 32.0);

      @Override
      public List<AABB> collisions(AABB var1) {
         return BLOCK.intersects(var1) ? List.of(BLOCK) : List.of();
      }
   }

   private record Inner_NetHelper(double ox, double oz, double ux, double uz, double target) {
      double along(AiPhysics.Inner_StreamUtil var1) {
         return (var1.x() - this.ox) * this.ux + (var1.z() - this.oz) * this.uz;
      }
   }

   record Inner_TableCache(float yaw, double strafe, double margin, int nextRunTicks, double exitSpeed) {
   }

   private record Inner_CachePool(int runTicks, AiJumpSolver.Inner_StreamUtil arc) {
   }

   public record Inner_StateCache(
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
      public Inner_StateCache(
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

      public Inner_StateCache(
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

      public AiJumpSolver.Inner_StateCache shifted(int var1, int var2, int var3) {
         return new AiJumpSolver.Inner_StateCache(
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

   private record Inner_TaskQueue(
      AiPhysics.Inner_CachePool world,
      List<AiPhysics.Inner_StreamUtil> runs,
      AiPhysics.Inner_DataUtil press,
      AiPhysics.Inner_DataUtil air,
      AiJumpSolver.Inner_NetHelper line,
      BlockPos to,
      double surface,
      AiJumpSolver.Inner_StreamUtil[] held
   ) {
      Inner_TaskQueue(
         AiPhysics.Inner_CachePool var1,
         List<AiPhysics.Inner_StreamUtil> var2,
         AiPhysics.Inner_DataUtil var3,
         AiPhysics.Inner_DataUtil var4,
         AiJumpSolver.Inner_NetHelper var5,
         BlockPos var6,
         double var7
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, new AiJumpSolver.Inner_StreamUtil[var2.size()]);
      }

      AiJumpSolver.Inner_StreamUtil fly(int var1, int var2) {
         if (var2 != 2147483647) {
            return AiJumpSolver.putAt_IoUtil_JsonCache_TokenCore_TaskStat(
               this.world, this.runs.get(var1), this.press, this.air, var2, this.line, this.to, this.surface
            );
         }

         AiJumpSolver.Inner_StreamUtil var3 = this.held[var1];
         return var3 != null
            ? var3
            : (
               this.held[var1] = AiJumpSolver.putAt_IoUtil_JsonCache_TokenCore_TaskStat(
                  this.world, this.runs.get(var1), this.press, this.air, 2147483647, this.line, this.to, this.surface
               )
            );
      }

      AiJumpSolver.Inner_StreamUtil releaseEarlier(int var1, AiJumpSolver.Inner_StreamUtil var2) {
         return AiJumpSolver.putAt_IoUtil_JsonCache_TokenCore_TaskStat_2(
            this.world, this.runs.get(var1), this.press, this.air, var2, this.line, this.to, this.surface
         );
      }
   }

   private record Inner_TokenCodec(AiPhysics.Inner_DataUtil entry, int turnTick, AiPhysics.Inner_DataUtil exit) {
      AiPhysics.Inner_DataUtil at(int var1) {
         return var1 < this.turnTick ? this.entry : this.exit;
      }
   }

   // 5.3 external ABI bridges for ai/b/TokenStore.java
   public static AiJumpSolver.UiHelper init_TableIndex_UiHelpe_NodeWalker_CachePo(
      AiPhysics.Inner_CachePool world, AiPhysics.Inner_StreamUtil state, float yaw, boolean sprint, BlockPos to, double toSurface
   ) {
      return putAt_IoUtil_IoPool_TokenCore_TaskStat(world, state, yaw, sprint, to, toSurface);
   }

   public static AiJumpSolver.UiHelper init_TableIndex_UiHelpe_NodeWalker_CachePo(
      AiPhysics.Inner_CachePool world, AiPhysics.Inner_StreamUtil state, float yaw, boolean sprint, BlockPos to, double toSurface, int maxRunTicks
   ) {
      return putAt_IoUtil_IoPool_TokenCore_TaskStat(world, state, yaw, sprint, to, toSurface, maxRunTicks);
   }
}
