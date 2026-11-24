package autismclient.ai.a;

import java.util.function.Function;
import java.util.function.LongPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

final class l {
   static final int flowState = 30;
   static final int fill = -2;
   static final int readEx = 8;
   static final int value = 8;
   static final double putAt_D = 2.2;
   private static final int[] putAt_IArr = new int[]{0, -12, 12, -24, 24};

   private l() {
   }

   static long putAt_J_D_D(double var0, double var2) {
      return putAt_J_I_I((int)Math.floor(var0), (int)Math.floor(var2));
   }

   static long putAt_J_I_I(int var0, int var1) {
      return (long)var0 << 32 ^ var1 & 4294967295L;
   }

   static boolean putAt(NodeWalker.Inner_StreamUtil var0, LongPredicate var1, NodeWalker.Inner_CachePool var2) {
      NodeWalker.Inner_StreamUtil var3 = var0;
      double var4 = var0.y();
      NodeWalker.Inner_DataUtil var6 = new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, 0.0F);

      for (int var7 = 0; var7 < 8; var7++) {
         NodeWalker.Inner_StreamUtil var8 = NodeWalker.putAt(var3, var6, var2);
         if (NodeWalker.putAt_Z(var2, var3, var8)) {
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
      NodeWalker.Inner_StreamUtil var0, NodeWalker.Inner_DataUtil var1, double var2, LongPredicate var4, NodeWalker.Inner_CachePool var5
   ) {
      NodeWalker.Inner_StreamUtil var6 = var0;
      NodeWalker.Inner_DataUtil var7 = var1.withJump(false);

      for (int var8 = 0; var8 < 8; var8++) {
         NodeWalker.Inner_StreamUtil var9 = NodeWalker.putAt(var6, var7, var5);
         if (NodeWalker.putAt_Z(var5, var6, var9)) {
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
      NodeWalker.Inner_StreamUtil var0,
      double var1,
      LongPredicate var3,
      NodeWalker.Inner_CachePool var4,
      Function<NodeWalker.Inner_StreamUtil, NodeWalker.Inner_DataUtil> var5
   ) {
      if (!var0.onGround()) {
         return false;
      }

      double var6 = var0.y();
      int var8 = 0;

      while (var8 < 8) {
         NodeWalker.Inner_StreamUtil var9 = NodeWalker.putAt(var0, NodeWalker.Inner_DataUtil.NONE, var4);
         if (AiGroundContinuity.putAt(var4, var0, var9, var6) && !NodeWalker.putAt_Z(var4, var0, var9)) {
            if (var9.onGround()) {
               var6 = var9.y();
            }

            NodeWalker.Inner_DataUtil var10 = (NodeWalker.Inner_DataUtil)var5.apply(var9);
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

   static l.Inner_TableCache putAt(NodeWalker.Inner_StreamUtil var0, NodeWalker.Inner_DataUtil var1, boolean var2, NodeWalker.Inner_CachePool var3) {
      NodeWalker.Inner_StreamUtil var4 = var0;
      boolean var5 = false;
      boolean var6 = false;
      boolean var7 = !var0.onGround();

      for (int var8 = 1; var8 <= 30; var8++) {
         NodeWalker.Inner_StreamUtil var9 = NodeWalker.putAt(var4, var1.withJump(var2 && var8 == 1), var3);
         if (NodeWalker.putAt_Z(var3, var4, var9)) {
            return new l.Inner_TableCache(null, var8, var5, var6);
         }

         var6 |= var2 && var9.verticalCollision() && !var9.onGround() && (var8 == 1 || var4.vy() > 0.0);
         var4 = var9;
         var5 |= var4.horizontalCollision();
         if (!var4.onGround()) {
            var7 = true;
         } else if (var7) {
            return new l.Inner_TableCache(var4, var8, var5, var6);
         }
      }

      return new l.Inner_TableCache(null, 30, var5, var6);
   }

   static l.Inner_TableCache putAt(NodeWalker.Inner_CachePool var0, NodeWalker.Inner_StreamUtil var1, NodeWalker.Inner_DataUtil var2) {
      if (var1.jumpDelay() <= 1 && var1.onGround()) {
         NodeWalker.Inner_StreamUtil var3 = NodeWalker.putAt(var1, var2.withJump(true), var0);
         if (var3.verticalCollision() && !var3.onGround() && !var3.horizontalCollision() && !NodeWalker.fill(var0, var1, var3)) {
            l.Inner_TableCache var4 = putAt(var1, var2, true, var0);
            return var4.down()
                  && var4.ceiling()
                  && !var4.wall()
                  && var4.ticks() <= 4
                  && Math.abs(var4.at().y() - var1.y()) <= 0.02
                  && !NodeWalker.fill(var0, var3, var4.at())
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
      NodeWalker.Inner_StreamUtil var0, NodeWalker.Inner_DataUtil var1, float var2, l.Inner_TableCache var3, NodeWalker.Inner_CachePool var4
   ) {
      if (!var3.down()) {
         return false;
      }

      NodeWalker.Inner_StreamUtil var5 = var0;

      for (int var6 = 0; var6 < var3.ticks(); var6++) {
         var5 = NodeWalker.putAt(var5, var1.withJump(false), var4);
      }

      double var16 = Math.toRadians(var2);
      double var8 = -Math.sin(var16);
      double var10 = Math.cos(var16);
      double var12 = (var3.at().x() - var0.x()) * var8 + (var3.at().z() - var0.z()) * var10 + (var3.at().vx() * var8 + var3.at().vz() * var10) * 2.2;
      double var14 = (var5.x() - var0.x()) * var8 + (var5.z() - var0.z()) * var10 + (var5.vx() * var8 + var5.vz() * var10) * 2.2;
      return var12 > var14 + 0.02;
   }

   static int putAt_I_TokenCore_TableCac(
      NodeWalker.Inner_StreamUtil var0, NodeWalker.Inner_DataUtil var1, int var2, double var3, LongPredicate var5, NodeWalker.Inner_CachePool var6
   ) {
      return putAt_UiCore_TaskState_TokenCore_TableCac(var0, var1, var2, var3, var5, var6).ticks();
   }

   static l.Inner_StreamUtil putAt_UiCore_TaskState_TokenCore_TableCac(
      NodeWalker.Inner_StreamUtil var0, NodeWalker.Inner_DataUtil var1, int var2, double var3, LongPredicate var5, NodeWalker.Inner_CachePool var6
   ) {
      NodeWalker.Inner_StreamUtil var7 = var0;
      boolean var8 = false;
      boolean var9 = false;

      for (int var10 = 1; var10 <= 30; var10++) {
         boolean var11 = !var8 && var10 > var2 && var7.onGround();
         NodeWalker.Inner_StreamUtil var12 = NodeWalker.putAt(var7, var1.withJump(var11), var6);
         if (NodeWalker.putAt_Z(var6, var7, var12)) {
            return new l.Inner_StreamUtil(-2, var9);
         }

         var7 = var12;
         var9 |= var7.horizontalCollision();
         var8 |= var11;
         if (var7.onGround()) {
            if (var7.y() >= var3 - 0.001) {
               return new l.Inner_StreamUtil(!putAt(var7, var5, var6) && !putAt_Z_TokenCore_TableCac_2(var7, var1, var3, var5, var6) ? -1 : var10, var9);
            }

            if (var8 && var10 > var2 + 1) {
               return new l.Inner_StreamUtil(-1, var9);
            }
         }
      }

      return new l.Inner_StreamUtil(-1, var9);
   }

   static boolean putAt_Z_I_I(int var0, int var1) {
      return var0 >= 0 && (var1 < 0 || var0 <= var1);
   }

   static NodeWalker.Inner_DataUtil putAt_TokenCore_CacheHel_TokenCore_TaskStat(
      NodeWalker.Inner_CachePool var0, NodeWalker.Inner_StreamUtil var1, BlockPos var2, double var3, float var5, boolean var6
   ) {
      double var7 = Math.hypot(var2.getX() + 0.5 - var1.x(), var2.getZ() + 0.5 - var1.z());
      if (TableIndex.putAt(var1) && var1.pose() == 0 && !(var7 > 2.75) && !(var3 - var1.y() > 1.125) && !(var1.y() > var3 + 0.02)) {
         float var9 = NodeWalker.putAt_F_D(var1.x(), var1.z(), var2.getX() + 0.5, var2.getZ() + 0.5);
         NodeWalker.Inner_DataUtil var10 = null;
         int var11 = 10;

         for (float var15 : new float[]{var5, var9, var9 - 15.0F, var9 + 15.0F, Math.round(var9 / 90.0F) * 90.0F}) {
            if (!(Math.abs(Mth.wrapDegrees(var15 - var9)) > 55.0F)) {
               for (boolean var19 : new boolean[]{var6, false}) {
                  NodeWalker.Inner_DataUtil var20 = NodeWalker.Inner_DataUtil.run(var15, var19);
                  NodeWalker.Inner_StreamUtil var21 = var1;

                  for (int var22 = 1; var22 < var11; var22++) {
                     NodeWalker.Inner_StreamUtil var23 = NodeWalker.putAt(var21, var20, var0);
                     if (!var23.onGround()
                        || var23.y() < var21.y() - 0.02
                        || var23.y() > var3 + 0.02
                        || NodeWalker.fill(var0, var21, var23)
                        || TableIndex.putAt(var0, var23)) {
                        break;
                     }

                     if (TableIndex.putAt_Z_D(var23.x(), var23.y(), var23.z(), var2, var3)
                        && TableIndex.putAt_D_TokenCore_TaskStat_2(var0, var23, var2, var3, var15) >= 0.04) {
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

   static l.Inner_JsonCache putAt_UiCore_JsonCache_TokenCore_TableCac(
      NodeWalker.Inner_StreamUtil var0, BlockPos var1, double var2, LongPredicate var4, NodeWalker.Inner_CachePool var5
   ) {
      return putAt_UiCore_JsonCache_TokenCore_TableCac(var0, var1, var2, var4, var5, false);
   }

   static l.Inner_JsonCache putAt_UiCore_JsonCache_TokenCore_TableCac(
      NodeWalker.Inner_StreamUtil var0, BlockPos var1, double var2, LongPredicate var4, NodeWalker.Inner_CachePool var5, boolean var6
   ) {
      if (var0.onGround() && var0.pose() == 0 && var0.jumpDelay() <= 1 && !(var2 - var0.y() <= 0.6) && !(var2 - var0.y() > 1.25)) {
         double var7 = var1.getX() + 0.5 - var0.x();
         double var9 = var1.getZ() + 0.5 - var0.z();
         double var11 = var7 * var7 + var9 * var9;
         if (!(var11 < 1.0E-6) && !(var11 > 6.002500000000001)) {
            double var13 = Math.sqrt(var11);
            float var15 = NodeWalker.putAt_F_D(0.0, 0.0, var7, var9);
            l.Inner_JsonCache var16 = null;

            for (int var20 : putAt_IArr) {
               NodeWalker.Inner_DataUtil var21 = NodeWalker.Inner_DataUtil.run(var15 + var20, true);

               for (int var22 = 0; var22 < 3; var22++) {
                  l.Inner_JsonCache var23 = new l.Inner_JsonCache(null, var21, var22 == 0, var22 == 2 ? 2 : 0, 0, 0.0);
                  l.Inner_TableCache var24 = putAt(var0, var23, 0, var2, var4, var5, var6);
                  if (var24 != null) {
                     NodeWalker.Inner_StreamUtil var25 = var24.at();
                     double var26 = (var25.vx() * var7 + var25.vz() * var9) / var13;
                     if (var16 == null || var24.ticks() < var16.ticks() || var24.ticks() == var16.ticks() && var26 > var16.exitSpeed() + 0.015) {
                        var16 = new l.Inner_JsonCache(null, var21, var23.boost(), var23.coast(), var24.ticks(), var26, var6);
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

   static l.Inner_JsonCache fill(NodeWalker.Inner_StreamUtil var0, BlockPos var1, double var2, LongPredicate var4, NodeWalker.Inner_CachePool var5) {
      if (!(var0.speed() < 0.06) && var0.onGround() && var0.pose() == 0) {
         float var6 = NodeWalker.putAt_F_D(0.0, 0.0, var0.vx(), var0.vz());

         for (float var10 : new float[]{var6, var6 - 25.0F, var6 + 25.0F}) {
            NodeWalker.Inner_DataUtil var11 = new NodeWalker.Inner_DataUtil(-1.0, 0.0, false, false, false, var10);
            NodeWalker.Inner_StreamUtil var12 = NodeWalker.putAt(var0, var11, var5);
            if (var12.onGround()
               && !(Math.abs(var12.y() - var0.y()) > 0.02)
               && !var12.horizontalCollision()
               && !(var12.speed() >= var0.speed())
               && !NodeWalker.fill(var5, var0, var12)
               && TableIndex.putAt_Z_TokenCore_TaskStat(var5, var12.x(), var12.y(), var12.z())) {
               l.Inner_JsonCache var13 = putAt_UiCore_JsonCache_TokenCore_TableCac(var12, var1, var2, var4, var5);
               if (var13 != null) {
                  return new l.Inner_JsonCache(var11, var13.drive(), var13.boost(), var13.coast(), var13.ticks() + 1, var13.exitSpeed(), var13.touching());
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static l.Inner_TableCache putAt_UiCore_TableCache_TokenCore_TableCac(
      NodeWalker.Inner_StreamUtil var0, l.Inner_JsonCache var1, int var2, double var3, LongPredicate var5, NodeWalker.Inner_CachePool var6
   ) {
      return putAt(var0, var1, var2, var3, var5, var6, false);
   }

   static l.Inner_TableCache putAt(
      NodeWalker.Inner_StreamUtil var0, l.Inner_JsonCache var1, int var2, double var3, LongPredicate var5, NodeWalker.Inner_CachePool var6, boolean var7
   ) {
      for (int var8 = var2; var8 < 14; var8++) {
         NodeWalker.Inner_StreamUtil var9 = NodeWalker.putAt(var0, var1.input(var8), var6);
         if (var9.horizontalCollision() && !var7 || NodeWalker.fill(var6, var0, var9) || TableIndex.putAt(var6, var9)) {
            return null;
         }

         if (var9.onGround()) {
            if (!(Math.abs(var9.y() - var3) > 0.02) && !(var9.vy() > 0.0) && var5.test(putAt_J_D_D(var9.x(), var9.z())) && putAt(var9, var5, var6)) {
               return new l.Inner_TableCache(var9, var8 + 1, false, false);
            }

            return null;
         }

         var0 = var9;
      }

      return null;
   }

   static l.Inner_DataUtil putAt(
      NodeWalker.Inner_StreamUtil var0,
      NodeWalker.Inner_DataUtil var1,
      NodeWalker.Inner_DataUtil var2,
      NodeWalker.Inner_DataUtil var3,
      LongPredicate var4,
      boolean var5,
      double var6,
      double var8,
      NodeWalker.Inner_CachePool var10
   ) {
      l.Inner_DataUtil var11 = null;
      double var12 = 1.7976931348623157E308;
      l.Inner_DataUtil var14 = l.Inner_DataUtil.load;
      double var15 = 1.7976931348623157E308;

      for (l.Inner_DataUtil var20 : l.Inner_DataUtil.values()) {
         NodeWalker.Inner_DataUtil var21 = var20 == l.Inner_DataUtil.init
            ? var1
            : (var20 == l.Inner_DataUtil.load ? var2 : var3);
         l.Inner_TableCache var22 = putAt(var0, var21, false, var10);
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

   enum Inner_DataUtil {
      init,
      load,
      read;
   }

   record Inner_JsonCache(
      NodeWalker.Inner_DataUtil setup, NodeWalker.Inner_DataUtil drive, boolean boost, int coast, int ticks, double exitSpeed, boolean touching
   ) {
      Inner_JsonCache(NodeWalker.Inner_DataUtil var1, NodeWalker.Inner_DataUtil var2, boolean var3, int var4, int var5, double var6) {
         this(var1, var2, var3, var4, var5, var6, false);
      }

      NodeWalker.Inner_DataUtil input(int var1) {
         if (this.setup != null) {
            if (var1 == 0) {
               return this.setup;
            }

            var1--;
         }

         if (var1 == 0) {
            return new NodeWalker.Inner_DataUtil(this.drive.forward(), this.drive.strafe(), true, this.boost, false, this.drive.yaw());
         } else {
            return var1 <= this.coast ? NodeWalker.Inner_DataUtil.NONE.withYaw(this.drive.yaw()) : this.drive;
         }
      }
   }

   record Inner_TableCache(NodeWalker.Inner_StreamUtil at, int ticks, boolean wall, boolean ceiling) {
      boolean down() {
         return this.at != null;
      }

      boolean struck() {
         return this.wall || this.ceiling;
      }
   }

   record Inner_StreamUtil(int ticks, boolean struck) {
   }
}
