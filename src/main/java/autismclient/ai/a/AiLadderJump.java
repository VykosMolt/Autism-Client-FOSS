package autismclient.ai.a;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public final class AiLadderJump {
   private static final int flowState = 27;
   private static final double putAt_D = 0.15;
   private static final int fill = 2;
   private static final int[] putAt_IArr = new int[]{2, 0, -1, 1};

   private AiLadderJump() {
   }

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, float var2, BlockPos var3, double var4
   ) {
      for (float var9 : new float[]{0.0F, -12.0F, 12.0F, -25.0F, 25.0F}) {
         AiPhysics.Inner_DataUtil var10 = AiPhysics.Inner_DataUtil.run(var2 + var9, false);
         ArrayList<AiPhysics.Inner_DataUtil> var11 = new ArrayList<>();
         AiPhysics.Inner_StreamUtil var12 = var1;
         boolean var13 = false;
         double var14 = var1.y();

         for (int var16 = 0; var16 < 27; var16++) {
            AiPhysics.Inner_DataUtil var17 = var10.withJump(!var12.onGround() || var12.y() < var4 - 0.02);
            AiPhysics.Inner_StreamUtil var18 = AiPhysics.fill(var12, var17, var0);
            if (AiPhysics.fill(var0, var12, var18) || var14 - var18.y() > var0.putAt_D()) {
               break;
            }

            var14 = Math.max(var14, var18.y());
            var11.add(var17);
            var13 |= !AiJumpSolver.putAt(var0, var18);
            if (var13 && AiJumpSolver.putAt(var18)) {
               double var19 = putAt_D_TokenCore_TaskStat(var0, var18, var3, var4, var17.yaw());
               if (var19 >= -0.04) {
                  AiJumpManeuver var22 = new AiJumpManeuver(var11, var1, var18, var19, 0);
                  return new AiJumpSolver.Inner_StateCache(
                     var17.yaw(),
                     0,
                     false,
                     2147483647,
                     var11.size(),
                     var1.x(),
                     var1.z(),
                     var18.x(),
                     var18.y(),
                     var18.z(),
                     var18.speed(),
                     var19,
                     0.0,
                     0.0,
                     var22
                  );
               }

               if (var18.support() == -9223372036854775808L) {
                  break;
               }

               BlockPos var21 = BlockPos.of(var18.support());
               if (!var0.state(var21.getX(), var21.getY(), var21.getZ())
                  && (var21.getX() != var3.getX() || var21.getZ() != var3.getZ() || !(var4 - var18.y() <= 0.51) || !(var18.y() <= var4 + 0.02))) {
                  break;
               }
            }

            var12 = var18;
         }
      }

      return null;
   }

   static boolean putAt_Z_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, AiJumpManeuver var2, int var3, BlockPos var4, double var5
   ) {
      AiPhysics.Inner_StreamUtil var7 = var1;
      double var8 = var7.y();

      for (int var10 = var3; var10 < var2.inputs().size() + 3; var10++) {
         AiPhysics.Inner_DataUtil var11 = AiSlimeBounce.putAt_TokenCore_CacheHel_TaskStore_I(var2, var10);
         AiPhysics.Inner_StreamUtil var12 = AiPhysics.fill(var7, var11, var0);
         if (AiPhysics.fill(var0, var7, var12) || var8 - var12.y() > var0.putAt_D()) {
            return false;
         }

         var8 = Math.max(var8, var12.y());
         if (putAt_D_TokenCore_TaskStat(var0, var12, var4, var5, var11.yaw()) >= -0.04) {
            return true;
         }

         var7 = var12;
      }

      return false;
   }

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, boolean var3
   ) {
      if (!var1.onGround() && !AiJumpSolver.putAt(var0, var1)) {
         return null;
      }

      float var4 = AiPhysics.putAt_F_D(var1.x(), var1.z(), var2.getX() + 0.5, var2.getZ() + 0.5);
      int var5 = var1.onGround() ? 10 : 0;

      for (float var9 : new float[]{0.0F, -6.0F, 6.0F, -12.0F, 12.0F}) {
         float var10 = var4 + var9;
         AiPhysics.Inner_DataUtil var11 = AiPhysics.Inner_DataUtil.run(var10, var3);
         AiPhysics.Inner_StreamUtil var12 = var1;
         ArrayList<AiPhysics.Inner_DataUtil> var13 = new ArrayList<>();

         for (int var14 = 0; var14 <= var5; var14++) {
            if (Thread.currentThread().isInterrupted()) {
               return null;
            }

            AiPhysics.Inner_StreamUtil var15 = var12;
            var var16 = new ArrayList<>(var13);
            boolean var17 = false;

            for (int var18 = 0; var18 < 24; var18++) {
               AiPhysics.Inner_DataUtil var19 = var11.withJump(var18 == 0 || !var17 && var5 == 0);
               AiPhysics.Inner_StreamUtil var20 = AiPhysics.fill(var15, var19, var0);
               if (AiPhysics.fill(var0, var15, var20) || var20.y() < var2.getY() - 4) {
                  break;
               }

               var16.add(var19);
               boolean var21 = AiJumpSolver.putAt(var0, var20);
               var17 |= !var21 && !var20.onGround();
               if (var17 && var21) {
                  if (AiSlimeBounce.putAt(var0, BlockPos.containing(var20.x(), var20.y(), var20.z()), var2)
                     && putAt_Z_TokenCore_TaskStat(var0, var20, var2, var10)) {
                     AiJumpManeuver var22 = new AiJumpManeuver(var16, var1, var20, 0.1, var14);
                     return new AiJumpSolver.Inner_StateCache(
                        var10,
                        var14,
                        var3,
                        2147483647,
                        var16.size() - var14,
                        var1.x(),
                        var1.z(),
                        var20.x(),
                        var20.y(),
                        var20.z(),
                        var20.speed(),
                        0.1,
                        0.0,
                        0.0,
                        var22
                     );
                  }
                  break;
               }

               if (var20.onGround() && (var17 || var18 == 0 && var5 > 0)) {
                  break;
               }

               var15 = var20;
            }

            if (var14 == var5) {
               break;
            }

            AiPhysics.Inner_StreamUtil var23 = AiPhysics.putAt(var12, var11, var0);
            if (!var23.onGround() || AiPhysics.fill(var0, var12, var23)) {
               break;
            }

            var13.add(var11);
            var12 = var23;
         }
      }

      return null;
   }

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat_2(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, BlockPos var3, boolean var4
   ) {
      boolean var5 = AiJumpSolver.putAt(var0, var1);
      AiJumpSolver.Inner_StateCache var6 = putAt_IoUtil_TextState_TokenCore_TaskStat(var0, var1, var3, var4 && !var5);
      if (var6 == null && !var0.state(var2.getX(), var2.getY() + 1, var2.getZ())) {
         for (int var10 : putAt_IArr) {
            AiLadderJump.Inner_CacheHelper var11 = putAt_TableCore_CacheHel_TokenCore_TaskStat(var0, var1, var2, var10);
            if (var11 != null) {
               AiJumpSolver.Inner_StateCache var12 = putAt_IoUtil_TextState_TokenCore_TaskStat(var0, var11.state(), var3, var4);
               if (var12 != null) {
                  return putAt(var1, var11.inputs(), var12);
               }
            }
         }

         return null;
      } else {
         return var6;
      }
   }

   static AiLadderJump.Inner_CacheHelper putAt_TableCore_CacheHel(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2) {
      return putAt_TableCore_CacheHel_TokenCore_TaskStat(var0, var1, var2, 2);
   }

   private static AiLadderJump.Inner_CacheHelper putAt_TableCore_CacheHel_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, int var3
   ) {
      if (var1.onGround() && var1.support() == var2.asLong()) {
         return new AiLadderJump.Inner_CacheHelper(List.of(), var1);
      }

      boolean var4 = var1.y() >= var2.getY() + 1
         && var1.y() <= var2.getY() + 1.25
         && Math.hypot(var1.x() - var2.getX() - 0.5, var1.z() - var2.getZ() - 0.5) <= 0.65;
      if (!AiJumpSolver.putAt(var0, var1) && !var4) {
         return null;
      }

      ArrayList<AiPhysics.Inner_DataUtil> var5 = new ArrayList<>();
      AiPhysics.Inner_StreamUtil var6 = var1;
      float var7 = putAt(var0, var2, 0.0F);

      for (int var8 = 0; var8 < 16; var8++) {
         AiPhysics.Inner_DataUtil var9 = AiJumpSolver.putAt(var0, var6)
            ? putAt_TokenCore_CacheHel_TokenCore_TaskStat(var0, var6, var2, var7)
            : AiPhysics.Inner_DataUtil.run(var7, false);
         if (var8 == 0 && var3 != 2) {
            var9 = var9.withStrafe(var3);
         }

         AiPhysics.Inner_StreamUtil var10 = AiPhysics.fill(var6, var9, var0);
         if (AiPhysics.fill(var0, var6, var10) || var10.y() < var2.getY()) {
            return null;
         }

         var5.add(var9);
         if (var10.onGround() && var10.support() == var2.asLong()) {
            return new AiLadderJump.Inner_CacheHelper(var5, var10);
         }

         var6 = var10;
      }

      return null;
   }

   static AiJumpSolver.Inner_StateCache putAt_IoUtil_TextState_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, AiLadderJump.Inner_CacheHelper var2, BlockPos var3, double var4, boolean var6
   ) {
      AiJumpSolver.Inner_StateCache var7 = AiJumpSolver.putAt_IoUtil_TextState_TokenCore_TaskStat_2(var0, var2.state(), var3, var4, var6, -0.2);
      return var7 != null && AiJumpSolver.putAt_Z_D(var7.landX(), var7.landY(), var7.landZ(), var3, var4) ? putAt(var1, var2.inputs(), var7) : null;
   }

   private static AiJumpSolver.Inner_StateCache putAt(AiPhysics.Inner_StreamUtil var0, List<AiPhysics.Inner_DataUtil> var1, AiJumpSolver.Inner_StateCache var2) {
      if (var2 != null && !var1.isEmpty()) {
         var var3 = new ArrayList<>(var1);
         var3.addAll(var2.maneuver().inputs());
         int var4 = var1.size() + var2.maneuver().takeoffTick();
         AiJumpManeuver var5 = new AiJumpManeuver(var3, var0, var2.maneuver().landing(), var2.margin(), var4);
         return new AiJumpSolver.Inner_StateCache(
            ((AiPhysics.Inner_DataUtil)var1.getFirst()).yaw(),
            var4,
            var2.sprint(),
            var2.coastAfter(),
            var2.flightTicks(),
            var0.x(),
            var0.z(),
            var2.landX(),
            var2.landY(),
            var2.landZ(),
            var2.landSpeed(),
            var2.margin(),
            0.0,
            0.0,
            var5
         );
      } else {
         return var2;
      }
   }

   static boolean putAt_Z_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, AiJumpManeuver var2, int var3, BlockPos var4) {
      var var5 = var2.inputs();

      for (int var6 = var3; var6 < var5.size() + 3; var6++) {
         if (AiSlimeBounce.putAt(var0, BlockPos.containing(var1.x(), var1.y(), var1.z()), var4)) {
            return putAt_Z_TokenCore_TaskStat(var0, var1, var4, ((AiPhysics.Inner_DataUtil)var5.getLast()).yaw());
         }

         AiPhysics.Inner_StreamUtil var7 = AiPhysics.fill(var1, AiSlimeBounce.putAt_TokenCore_CacheHel_TaskStore_I(var2, var6), var0);
         if (AiPhysics.fill(var0, var1, var7)) {
            return false;
         }

         if (var6 > var2.takeoffTick() && var7.onGround()) {
            return false;
         }

         var1 = var7;
      }

      return false;
   }

   static AiPhysics.Inner_DataUtil putAt_TokenCore_CacheHel_TokenCore_TaskStat_2(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, boolean var3
   ) {
      if (!var1.onGround() && !(var1.y() < var2.getY() - 4) && !(Math.hypot(var1.x() - var2.getX() - 0.5, var1.z() - var2.getZ() - 0.5) > 3.0)) {
         float var4 = AiPhysics.putAt_F_D(var1.x(), var1.z(), var2.getX() + 0.5, var2.getZ() + 0.5);

         for (int var8 : new int[]{0, -15, 15, -30, 30}) {
            float var9 = var4 + var8;

            for (int var13 : new int[]{1, 0, -1}) {
               AiPhysics.Inner_DataUtil var14 = new AiPhysics.Inner_DataUtil(var13, 0.0, true, var3 && var13 > 0, false, var9);
               AiPhysics.Inner_StreamUtil var15 = var1;

               for (int var16 = 0; var16 < 12; var16++) {
                  AiPhysics.Inner_StreamUtil var17 = AiPhysics.fill(var15, var14, var0);
                  if (AiPhysics.fill(var0, var15, var17) || var17.y() < var2.getY() - 4) {
                     break;
                  }

                  if (AiSlimeBounce.putAt(var0, BlockPos.containing(var17.x(), var17.y(), var17.z()), var2)) {
                     if (putAt_Z_TokenCore_TaskStat(var0, var17, var2, var9)) {
                        return var14;
                     }
                     break;
                  }

                  if (var17.onGround()) {
                     break;
                  }

                  var15 = var17;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static AiPhysics.Inner_DataUtil putAt_TokenCore_CacheHel_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, AiJumpManeuver var2, BlockPos var3, boolean var4, float var5
   ) {
      AiPhysics.Inner_StreamUtil var6 = var2.start();
      double var7 = var1.y() - var6.y();
      if (var6.onGround()
         && !var1.onGround()
         && !(var1.vy() >= 0.0)
         && !(var7 < -0.02)
         && !(var7 > 0.8)
         && !(Math.hypot(var1.x() - var6.x(), var1.z() - var6.z()) > 1.4)) {
         AiPhysics.Inner_DataUtil var9 = AiPhysics.Inner_DataUtil.run(var2.inputs().getFirst().yaw(), var4);

         for (AiPhysics.Inner_DataUtil var13 : new AiPhysics.Inner_DataUtil[]{
            var9, AiPhysics.Inner_DataUtil.run(var5, var4), AiPhysics.Inner_DataUtil.NONE.withYaw(var5)
         }) {
            AiPhysics.Inner_StreamUtil var14 = var1;
            int var15 = 0;

            while (var15 < 4) {
               AiPhysics.Inner_StreamUtil var16 = AiPhysics.fill(var14, var13, var0);
               if (!(var16.y() < var6.y() - 0.02) && !AiPhysics.fill(var0, var14, var16)) {
                  var14 = var16;
                  if (!AiJumpSolver.putAt(var14)) {
                     var15++;
                     continue;
                  } else if (Math.abs(var14.y() - var6.y()) <= 0.02
                     && AiJumpSolver.putAt_Z_TokenCore_TaskStat(var0, var14.x(), var14.y(), var14.z())
                     && (putAt_Z_TokenCore_TaskStat(var0, var14, var2, 0, var3) || putAt_IoUtil_TextState_TokenCore_TaskStat(var0, var14, var3, var4) != null)) {
                     return var13;
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static boolean putAt_Z_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, float var3) {
      boolean var4 = putAt_Z(var0, var1, BlockPos.containing(var1.x(), var1.y(), var1.z()));
      int var5 = 0;

      while (var5 < (var4 ? 8 : 4)) {
         AiPhysics.Inner_StreamUtil var6 = AiPhysics.fill(var1, putAt_TokenCore_CacheHel_TokenCore_TaskStat(var0, var1, var2, var3), var0);
         if (!AiPhysics.fill(var0, var1, var6) && AiSlimeBounce.putAt(var0, BlockPos.containing(var6.x(), var6.y(), var6.z()), var2)) {
            if (!(var6.vy() > 0.0)
               || !(Math.abs(var6.vx()) < 0.06)
               || !(Math.abs(var6.vz()) < 0.06)
               || var4
                  && (
                     !(Math.abs(var6.vx()) < 0.035)
                        || !(Math.abs(var6.vz()) < 0.035)
                        || !(Math.abs(var6.x() + var6.vx() - var2.getX() - 0.5) < 0.49)
                        || !(Math.abs(var6.z() + var6.vz() - var2.getZ() - 0.5) < 0.49)
                  )) {
               var1 = var6;
               var5++;
               continue;
            }

            return true;
         }

         return false;
      }

      return !var4 && var1.vy() > 0.0;
   }

   static AiPhysics.Inner_DataUtil putAt_TokenCore_CacheHel_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, float var3
   ) {
      BlockPos var4 = BlockPos.containing(var1.x(), var1.y(), var1.z());
      if (putAt(var0, var4)) {
         return putAt_TokenCore_CacheHel_TokenCore_TaskStat(var0, var1, var2, true, var3);
      }

      float var5 = putAt(var0, var4, var3);
      double var6 = Math.toRadians(var5);
      double var8 = var1.vx() * Math.cos(var6) + var1.vz() * Math.sin(var6);
      double var10 = (var1.x() - var2.getX() - 0.5) * Math.cos(var6) + (var1.z() - var2.getZ() - 0.5) * Math.sin(var6) + var8 * 3.0;
      boolean var12 = Math.abs(var10) > 0.24 && var8 * Math.signum(var10) > 0.045;
      return new AiPhysics.Inner_DataUtil(
         var12 ? 0.0 : 1.0, Math.abs(var10) <= 0.08 ? 0.0 : -Math.signum(var10), true, false, putAt_Z(var0, var1, var4), var5
      );
   }

   static AiPhysics.Inner_DataUtil putAt_TokenCore_CacheHel_TokenCore_TaskStat(
      AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, boolean var3, float var4
   ) {
      BlockPos var5 = BlockPos.containing(var1.x(), var1.y(), var1.z());
      boolean var6 = putAt_Z(var0, var1, var5);
      double var7 = var2.getX() + 0.5 - var1.x() - Math.clamp(var1.vx(), -0.15, 0.15) * 4.0;
      double var9 = var2.getZ() + 0.5 - var1.z() - Math.clamp(var1.vz(), -0.15, 0.15) * 4.0;
      double var11 = Math.toRadians(var4);
      double var13 = var7 * -Math.sin(var11) + var9 * Math.cos(var11);
      double var15 = var7 * Math.cos(var11) + var9 * Math.sin(var11);
      return new AiPhysics.Inner_DataUtil(
         Math.abs(var13) > 0.13 ? Math.signum(var13) : 0.0, Math.abs(var15) > 0.13 ? Math.signum(var15) : 0.0, var3, false, var6, var4
      );
   }

   static boolean putAt(AiPhysics.Inner_CachePool var0, BlockPos var1) {
      return var0.state(var1.getX(), var1.getY(), var1.getZ()) && !var0.putAt_Z_AABB(new AABB(var1).deflate(0.01));
   }

   static boolean fill(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, float var3) {
      double var4 = var1.x() + Math.clamp(var1.vx(), -0.15, 0.15) * 2.0;
      double var6 = var1.z() + Math.clamp(var1.vz(), -0.15, 0.15) * 2.0;
      return Math.abs(var4 - var2.getX() - 0.5) <= 0.42 && Math.abs(var6 - var2.getZ() - 0.5) <= 0.42 && putAt_Z_TokenCore_TaskStat(var0, var1, var2, var3);
   }

   private static boolean putAt_Z(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2) {
      return var1.vy() < 0.0 && var1.y() - var2.getY() < 0.2 && !var0.state(var2.getX(), var2.getY() - 1, var2.getZ());
   }

   private static float putAt(AiPhysics.Inner_CachePool var0, BlockPos var1, float var2) {
      var var3 = var0.collisions(new AABB(var1).deflate(0.01));
      if (var3.size() != 1) {
         return var2;
      } else {
         AABB var4 = (AABB)var3.getFirst();
         if (var4.getXsize() <= 0.25) {
            return var4.minX > var1.getX() + 0.5 ? -90.0F : 90.0F;
         } else if (var4.getZsize() <= 0.25) {
            return var4.minZ > var1.getZ() + 0.5 ? 0.0F : 180.0F;
         } else {
            return var2;
         }
      }
   }

   private static double putAt_D_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, AiPhysics.Inner_StreamUtil var1, BlockPos var2, double var3, float var5) {
      return AiJumpSolver.putAt(var1) && !(Math.abs(var1.y() - var3) > 0.02) && AiJumpSolver.putAt_Z_D(var1.x(), var1.y(), var1.z(), var2, var3)
         ? AiJumpSolver.putAt_D_TokenCore_TaskStat_2(var0, var1, var2, var3, var5)
         : -1.0;
   }

   record Inner_CacheHelper(List<AiPhysics.Inner_DataUtil> inputs, AiPhysics.Inner_StreamUtil state) {
   }
}
