package autismclient.ai.a;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AiBridger {
   public static final float putAt_F = 100.0F;
   static final double putAt_D = 1.62;
   static final double fill_D = 1.27;
   static final float fill_F = 179.5F;
   static final float readEx_F = 25.0F;
   static final double readEx_D = 0.14;
   static final double value_D = 0.04;
   static final float value_F = 12.0F;
   static final double prevEx = 0.3;
   static final int flowState = 30;
   static final int flowTemp = 8;
   static final int readEx_I = 6;
   private final AiBridger.Inner_CachePool putAt_TextStore_JsonCach;
   private final boolean putAt_Z;
   private AiBridger.Inner_ConfigStore putAt_TextStore_BitCore;
   private boolean fill_Z;
   private boolean readEx_Z;
   private boolean value_Z = true;
   public static final double prev = 4.5;

   public AiBridger(AiBridger.Inner_CachePool var1, AiBridger.Inner_ConfigStore var2) {
      this(var1, var2, false);
   }

   public AiBridger(AiBridger.Inner_CachePool var1, AiBridger.Inner_ConfigStore var2, boolean var3) {
      this.putAt_TextStore_JsonCach = var1;
      this.putAt_TextStore_BitCore = var2;
      this.putAt_Z = var3;
   }

   public AiBridger.Inner_CachePool putAt_TextStore_JsonCach() {
      return this.putAt_TextStore_JsonCach;
   }

   public AiBridger.Inner_ConfigStore putAt_TextStore_BitCore() {
      return this.putAt_TextStore_BitCore;
   }

   public void putAt_V_TextStore_BitCore(AiBridger.Inner_ConfigStore var1) {
      this.putAt_TextStore_BitCore = var1;
   }

   public int putAt_I_TokenCore_TaskStat(AiPhysics.Inner_CachePool var1) {
      for (int var2 = 1; var2 <= this.putAt_TextStore_JsonCach.length(); var2++) {
         if (!putAt_Z_TokenCore_TaskStat(
            var1, this.putAt_TextStore_JsonCach.cellX(var2), this.putAt_TextStore_JsonCach.floorY(), this.putAt_TextStore_JsonCach.cellZ(var2)
         )) {
            return var2;
         }
      }

      return this.putAt_TextStore_JsonCach.length() + 1;
   }

   static boolean putAt_Z_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, int var1, int var2, int var3) {
      AABB var4 = new AABB(var1, var2, var3, var1 + 1, var2 + 1, var3 + 1).deflate(1.0E-4);

      for (AABB var6 : var0.collisions(var4)) {
         if (var6.intersects(var4) && var6.maxY >= var2 + 1 - 0.001) {
            return true;
         }
      }

      return false;
   }

   public AiBridger.Inner_StreamUtil putAt_TextStore_TableCac_TokenCore_TableCac_2(
      AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, boolean var3, AiPhysics.Inner_CachePool var4
   ) {
      return this.putAt_TextStore_TableCac_TokenCore_TableCac_2(var1, var2, var3, var4, true);
   }

   public AiBridger.Inner_StreamUtil putAt_TextStore_TableCac_TokenCore_TableCac_2(
      AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, boolean var3, AiPhysics.Inner_CachePool var4, boolean var5
   ) {
      return this.putAt(var1, var2, var2, var3, var4, var5);
   }

   AiBridger.Inner_StreamUtil putAt(
      AiPhysics.Inner_StreamUtil var1,
      AiBridger.Inner_DataUtil var2,
      AiBridger.Inner_DataUtil var3,
      boolean var4,
      AiPhysics.Inner_CachePool var5,
      boolean var6
   ) {
      this.value_Z = var6;
      double var7 = this.putAt_TextStore_JsonCach.lateral(var1.x(), var1.z());
      if (Math.abs(var7) > 0.14) {
         this.readEx_Z = true;
      } else if (Math.abs(var7) < 0.04) {
         this.readEx_Z = false;
      }

      return this.putAt_TextStore_TableCac_TokenCore_TableCac(var1, var2, var3, var4, var5);
   }

   private AiBridger.Inner_StreamUtil putAt_TextStore_TableCac_TokenCore_TableCac(
      AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, AiBridger.Inner_DataUtil var3, boolean var4, AiPhysics.Inner_CachePool var5
   ) {
      int var6 = this.putAt_I_TokenCore_TaskStat(var5);
      double var7 = this.putAt_TextStore_JsonCach.along(var1.x(), var1.z());
      float var9 = this.putAt_TextStore_JsonCach.yaw();
      if (var1.onGround()) {
         this.fill_Z = false;
      }

      if (var6 <= this.putAt_TextStore_JsonCach.length()
         && (
            !var1.onGround()
               || !(var7 >= this.putAt_TextStore_JsonCach.length() + 1)
               || !putAt_Z_TokenCore_TableCac_2(var1, var5, this.putAt_TextStore_JsonCach.floorY())
         )) {
         if (!var1.onGround()) {
            return this.putAt(var1, var2, var3, var4, var5, var6);
         }

         if (this.putAt_TextStore_BitCore == AiBridger.Inner_ConfigStore.init) {
            AiBridger.Inner_DataUtil var10 = var2.stepToward(var9, 25.0F);
            if (putAt_Z_F_F(var2.yaw(), var9)) {
               if (this.putAt_Z_TokenCore_TableCac(var1, var10, var5)) {
                  return this.putAt_TextStore_TableCac_TokenCore_TableCac(var1, var10, true);
               }

               AiBridger.Inner_StreamUtil var11 = this.putAt_TextStore_TableCac_TokenCore_TableCac(var1, var10, false);
               AiPhysics.Inner_StreamUtil var12 = AiPhysics.putAt(var1, putAt_TokenCore_CacheHel_TextStore_TableCac_TextStore_CacheHel(var11, var10), var5);
               if (putAt_Z_TokenCore_TableCac_2(var12, var5, this.putAt_TextStore_JsonCach.floorY())
                  && (this.putAt_Z_TokenCore_TableCac(var12, var10, var5) || this.value(var12, var10, var5))) {
                  return var11;
               }
            } else if (this.readEx(var1, var2, var5)) {
               int[] var13 = this.putAt_IArr_F(var10.yaw(), var1, var9);
               AiBridger.Inner_StreamUtil var14 = new AiBridger.Inner_StreamUtil(var9, 25.0F, var13[0], var13[1], false, var13[0] > 0, false, -1, false);
               if (putAt_Z_F_F(var10.yaw(), var9)
                  && !putAt_Z_TokenCore_TableCac_2(
                     AiPhysics.putAt(var1, putAt_TokenCore_CacheHel_TextStore_TableCac_TextStore_CacheHel(var14, var10), var5),
                     var5,
                     this.putAt_TextStore_JsonCach.floorY()
                  )) {
                  return this.putAt_TextStore_TableCac_TokenCore_TableCac(var1, var10, true);
               }

               return var14;
            }
         }

         return this.fill(var1, var2, var3, var4, var5, var6);
      } else {
         return this.putAt_TextStore_TableCac_TokenCore_TableCac(var1, var2, var5, var7);
      }
   }

   private AiBridger.Inner_StreamUtil putAt_TextStore_TableCac_TokenCore_TableCac(
      AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, AiPhysics.Inner_CachePool var3, double var4
   ) {
      int var6 = this.putAt_TextStore_JsonCach.length();
      boolean var7 = !this.putAt_Z
         && fill(var3, this.putAt_TextStore_JsonCach.cellX(var6 + 1), this.putAt_TextStore_JsonCach.floorY(), this.putAt_TextStore_JsonCach.cellZ(var6 + 1));
      double var8 = var1.vx() * this.putAt_TextStore_JsonCach.dx() + var1.vz() * this.putAt_TextStore_JsonCach.dz();
      boolean var10 = this.putAt_Z ? var1.vx() * var1.vx() + var1.vz() * var1.vz() < 9.0E-4 : Math.abs(var8) < 0.03;
      if (!var1.onGround() || !(var4 >= var6) || !var7 && (this.putAt_Z || !(var4 >= var6 + 1)) && !var10) {
         if (!var1.onGround()) {
            if (!var7 && !this.prevEx(var1, var2, var3)) {
               int[] var21 = this.putAt_IArr_F(var2.yaw(), var1, this.putAt_TextStore_JsonCach.yaw() + 179.5F);
               return new AiBridger.Inner_StreamUtil(var2.yaw(), var2.pitch(), var21[0], var21[1], false, false, false, -1, false);
            } else {
               return AiBridger.Inner_StreamUtil.idle(var2.yaw(), var2.pitch());
            }
         } else {
            if (var4 < var6 + (var7 ? 0.0 : 0.2)) {
               int[] var20 = this.putAt_IArr_F(var2.yaw(), var1, this.putAt_TextStore_JsonCach.yaw());
               return new AiBridger.Inner_StreamUtil(var2.yaw(), var2.pitch(), var20[0], var20[1], false, false, !var7, -1, false);
            }

            int var11 = 0;
            int var12 = 0;
            double var13 = var1.vx() * var1.vx() + var1.vz() * var1.vz();

            for (int var15 = -1; var15 <= 1; var15++) {
               for (int var16 = -1; var16 <= 1; var16++) {
                  AiPhysics.Inner_StreamUtil var17 = AiPhysics.putAt(var1, new AiPhysics.Inner_DataUtil(var15, var16, false, false, true, var2.yaw()), var3);
                  double var18 = var17.vx() * var17.vx() + var17.vz() * var17.vz();
                  if (var18 < var13
                     && this.putAt_TextStore_JsonCach.along(var17.x(), var17.z()) >= var6
                     && putAt_Z_TokenCore_TableCac_2(var17, var3, this.putAt_TextStore_JsonCach.floorY())
                     && this.value(var17, var2, var3)) {
                     var13 = var18;
                     var11 = var15;
                     var12 = var16;
                  }
               }
            }

            return new AiBridger.Inner_StreamUtil(var2.yaw(), var2.pitch(), var11, var12, false, false, true, -1, false);
         }
      } else {
         return new AiBridger.Inner_StreamUtil(var2.yaw(), var2.pitch(), 0, 0, false, false, false, -1, true);
      }
   }

   private boolean prevEx(AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, AiPhysics.Inner_CachePool var3) {
      for (int var4 = 0; var4 < 30; var4++) {
         var1 = AiPhysics.putAt(var1, AiPhysics.Inner_DataUtil.NONE, var3);
         if (var1.y() < this.putAt_TextStore_JsonCach.floorY() + 1 - 0.01) {
            return false;
         }

         if (var1.onGround()) {
            return putAt_Z_TokenCore_TableCac_2(var1, var3, this.putAt_TextStore_JsonCach.floorY()) && this.value(var1, var2, var3);
         }
      }

      return false;
   }

   private AiBridger.Inner_StreamUtil putAt_TextStore_TableCac_TokenCore_TableCac(
      AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, boolean var3
   ) {
      int[] var4 = this.putAt_IArr_F(var2.yaw(), var1, this.putAt_TextStore_JsonCach.yaw());
      return new AiBridger.Inner_StreamUtil(this.putAt_TextStore_JsonCach.yaw(), 25.0F, var4[0], var4[1], var3, var4[0] > 0, false, -1, false);
   }

   private AiBridger.Inner_StreamUtil putAt(
      AiPhysics.Inner_StreamUtil var1,
      AiBridger.Inner_DataUtil var2,
      AiBridger.Inner_DataUtil var3,
      boolean var4,
      AiPhysics.Inner_CachePool var5,
      int var6
   ) {
      float var7 = this.putAt_TextStore_JsonCach.yaw();
      if (!this.fill_Z) {
         int var8 = this.putAt_Z_TokenCore_TableCac(var1, var3, var4, var5, var6) ? var6 : -1;
         if (var8 >= 0 || this.putAt_TextStore_BitCore != AiBridger.Inner_ConfigStore.init || !this.fill(var1, var2, var5)) {
            float[] var13 = this.putAt(var1, var2, var4, var5, var8 < 0 ? var6 : var6 + 1, 0.0 / 0.0, true);
            AiBridger.Inner_DataUtil var10 = var2.stepToward(var13[0], var13[1]);
            int[] var11 = this.putAt_IArr_F(var10.yaw(), var1, var7);
            return new AiBridger.Inner_StreamUtil(var13[0], var13[1], var11[0], var11[1], false, var11[0] > 0, false, var8, false);
         }

         this.fill_Z = true;
      }

      AiBridger.Inner_DataUtil var12 = var2.stepToward(var7, 25.0F);
      int[] var9 = this.putAt_IArr_F(var12.yaw(), var1, var7);
      return new AiBridger.Inner_StreamUtil(var7, 25.0F, var9[0], var9[1], false, var9[0] > 0, false, -1, false);
   }

   private AiBridger.Inner_StreamUtil fill(
      AiPhysics.Inner_StreamUtil var1,
      AiBridger.Inner_DataUtil var2,
      AiBridger.Inner_DataUtil var3,
      boolean var4,
      AiPhysics.Inner_CachePool var5,
      int var6
   ) {
      double var7 = var6 <= this.putAt_TextStore_JsonCach.length() ? this.putAt_D_TokenCore_TaskStat_I(var5, var6) : 0.0 / 0.0;
      int var9 = this.putAt_Z_TokenCore_TableCac(var1, var3, var4, var5, var6) ? var6 : -1;
      int var10 = var9 < 0 ? var6 : var6 + 1;
      float[] var11 = this.putAt(var1, var2, var4, var5, var10, var9 < 0 ? var7 : this.putAt_D_TokenCore_TaskStat_I(var5, var10), false);
      AiBridger.Inner_DataUtil var12 = var2.stepToward(var11[0], var11[1]);
      int[] var13 = this.putAt_IArr_F(var12.yaw(), var1, this.putAt_TextStore_JsonCach.yaw());
      if (!Double.isNaN(var7)
         && var9 < 0
         && !putAt_Z_TokenCore_TableCac_2(
            AiPhysics.putAt(var1, new AiPhysics.Inner_DataUtil(var13[0], var13[1], false, false, true, var12.yaw()), var5),
            var5,
            this.putAt_TextStore_JsonCach.floorY()
         )) {
         return new AiBridger.Inner_StreamUtil(var11[0], var11[1], 0, 0, false, false, true, -1, false);
      }

      boolean var14 = false;
      if (var9 < 0 || AiPathfinder.Inner_CachePool.putAt()) {
         AiPhysics.Inner_StreamUtil var15 = AiPhysics.putAt(var1, new AiPhysics.Inner_DataUtil(var13[0], var13[1], false, false, false, var12.yaw()), var5);
         var14 = !putAt_Z_TokenCore_TableCac_2(var15, var5, this.putAt_TextStore_JsonCach.floorY());
      }

      return new AiBridger.Inner_StreamUtil(var11[0], var11[1], var13[0], var13[1], false, false, var14, var9, false);
   }

   private float[] putAt(
      AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, boolean var3, AiPhysics.Inner_CachePool var4, int var5, double var6, boolean var8
   ) {
      float[] var9 = Double.isNaN(var6) ? this.putAt_FArr_TokenCore_TableCac(var1, var3, var5) : this.putAt_FArr_TokenCore_TableCac(var1, var3, var5, var6);
      AiBridger.Inner_DataUtil var10 = var2.stepToward(var9[0], var9[1]);
      int[] var11 = this.putAt_IArr_F(var10.yaw(), var1, this.putAt_TextStore_JsonCach.yaw());
      AiPhysics.Inner_StreamUtil var12 = AiPhysics.putAt(
         var1, new AiPhysics.Inner_DataUtil(var11[0], var11[1], false, var8 && var11[0] > 0, false, var10.yaw()), var4
      );
      return Double.isNaN(var6) ? this.putAt_FArr_TokenCore_TableCac(var12, var3, var5) : this.putAt_FArr_TokenCore_TableCac(var12, var3, var5, var6);
   }

   float[] putAt_FArr_TokenCore_TableCac(AiPhysics.Inner_StreamUtil var1, boolean var2, int var3) {
      double var4 = var1.y() + (var2 ? 1.27 : 1.62);
      double var6 = this.putAt_TextStore_JsonCach.lateral(var1.x(), var1.z());
      double var8 = Math.max(-0.35, Math.min(0.35, var6));
      double[] var10 = this.putAt_TextStore_JsonCach.point(var3, var8);
      double[] var11 = this.putAt_TextStore_JsonCach.along(var1.x(), var1.z()) - var3 < 0.12
         ? this.putAt_TextStore_JsonCach.point(var3 + 0.12, var6)
         : new double[]{var1.x(), var1.z()};
      double var12 = Math.hypot(var10[0] - var11[0], var10[1] - var11[1]);
      float var14 = !(Math.abs(var6 - var8) < 1.0E-6) && !(var12 < 1.0E-6)
         ? AiPhysics.putAt_F_D(var11[0], var11[1], var10[0], var10[1])
         : putAt_F_F(this.putAt_TextStore_JsonCach.yaw() + 179.5F);
      float var15 = (float)Math.toDegrees(Math.atan2(var4 - (this.putAt_TextStore_JsonCach.floorY() + 0.5), Math.max(0.12, var12)));
      return new float[]{var14, Math.min(89.5F, var15)};
   }

   float[] putAt_FArr_TokenCore_TableCac(AiPhysics.Inner_StreamUtil var1, boolean var2, int var3, double var4) {
      double var6 = var1.y() + (var2 ? 1.27 : 1.62);
      double var8 = this.putAt_TextStore_JsonCach.cellX(var3) + 0.5;
      double var10 = this.putAt_TextStore_JsonCach.cellZ(var3) + 0.5;
      float var12 = AiPhysics.putAt_F_D(var1.x(), var1.z(), var8, var10);
      float var13 = (float)Math.toDegrees(Math.atan2(var6 - var4, Math.max(0.12, Math.hypot(var8 - var1.x(), var10 - var1.z()))));
      return new float[]{var12, Math.min(89.5F, var13)};
   }

   boolean putAt_Z_TokenCore_TableCac(AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, boolean var3, AiPhysics.Inner_CachePool var4, int var5) {
      if (this.value_Z && var2 != null) {
         Vec3 var6 = new Vec3(var1.x(), var1.y() + (var3 ? 1.27 : 1.62), var1.z());
         int[] var7 = putAt_IArr_TokenCore_TaskStat(var4, var6, var2.yaw(), var2.pitch(), 4.5);
         if (var7 == null) {
            return false;
         } else {
            int var8 = var7[0] + var7[3];
            int var9 = var7[1] + var7[4];
            int var10 = var7[2] + var7[5];
            if (var8 == this.putAt_TextStore_JsonCach.cellX(var5)
               && var9 == this.putAt_TextStore_JsonCach.floorY()
               && var10 == this.putAt_TextStore_JsonCach.cellZ(var5)) {
               AABB var11 = new AABB(var8, var9, var10, var8 + 1, var9 + 1, var10 + 1);
               return !var11.intersects(var1.box(var3).deflate(1.0E-7));
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   boolean putAt_Z_TokenCore_TableCac(AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, AiPhysics.Inner_CachePool var3) {
      AiBridger.Inner_StreamUtil var4 = this.putAt_TextStore_TableCac_TokenCore_TableCac(var1, var2, true);
      AiPhysics.Inner_StreamUtil var5 = AiPhysics.putAt(var1, putAt_TokenCore_CacheHel_TextStore_TableCac_TextStore_CacheHel(var4, var2), var3);
      return this.putAt_Z_TokenCore_TableCac(var5, var2, false, var3);
   }

   boolean fill(AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, AiPhysics.Inner_CachePool var3) {
      float var4 = this.putAt_TextStore_JsonCach.yaw();
      AiBridger.Inner_DataUtil var5 = var2;
      AiPhysics.Inner_StreamUtil var6 = var1;

      for (int var7 = 0; var7 < 30; var7++) {
         var5 = var5.stepToward(var4, 25.0F);
         int[] var8 = this.putAt_IArr_F(var5.yaw(), var6, var4);
         var6 = AiPhysics.putAt(var6, new AiPhysics.Inner_DataUtil(var8[0], var8[1], false, var8[0] > 0, false, var5.yaw()), var3);
         if (var6.onGround()) {
            return this.putAt_Z_TokenCore_TableCac_TokenCore_TaskStat(var6, var3) && this.value(var6, var5, var3);
         }

         if (var6.y() < this.putAt_TextStore_JsonCach.floorY()) {
            return false;
         }
      }

      return false;
   }

   boolean readEx(AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, AiPhysics.Inner_CachePool var3) {
      float var4 = this.putAt_TextStore_JsonCach.yaw();
      AiBridger.Inner_DataUtil var5 = var2;
      AiPhysics.Inner_StreamUtil var6 = var1;

      for (int var7 = 0; var7 < 6; var7++) {
         var5 = var5.stepToward(var4, 25.0F);
         if (var6.onGround() && putAt_Z_F_F(var5.yaw(), var4) && this.putAt_Z_TokenCore_TableCac(var6, var5, var3)) {
            return true;
         }

         int[] var8 = this.putAt_IArr_F(var5.yaw(), var6, var4);
         var6 = AiPhysics.putAt(var6, new AiPhysics.Inner_DataUtil(var8[0], var8[1], false, var8[0] > 0, false, var5.yaw()), var3);
         if (!putAt_Z_TokenCore_TableCac_2(var6, var3, this.putAt_TextStore_JsonCach.floorY())) {
            return false;
         }
      }

      return false;
   }

   boolean value(AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, AiPhysics.Inner_CachePool var3) {
      AiPhysics.Inner_StreamUtil var4 = var1;

      for (int var5 = 0; var5 < 8; var5++) {
         var4 = AiPhysics.putAt(var4, new AiPhysics.Inner_DataUtil(0.0, 0.0, false, false, true, var2.yaw()), var3);
         if (!putAt_Z_TokenCore_TableCac_2(var4, var3, this.putAt_TextStore_JsonCach.floorY())) {
            return false;
         }
      }

      return true;
   }

   private boolean putAt_Z_TokenCore_TableCac(AiPhysics.Inner_StreamUtil var1, AiBridger.Inner_DataUtil var2, boolean var3, AiPhysics.Inner_CachePool var4) {
      AiBridger.Inner_BufferHelper var5 = new AiBridger.Inner_BufferHelper(var4);
      AiBridger var6 = new AiBridger(this.putAt_TextStore_JsonCach, this.putAt_TextStore_BitCore, this.putAt_Z);
      var6.readEx_Z = this.readEx_Z;
      AiBridger.Inner_DataUtil var7 = var2;

      for (int var8 = 0; var8 < 30; var8++) {
         if (var1.onGround()) {
            return this.putAt_Z_TokenCore_TableCac_TokenCore_TaskStat(var1, var5) && this.value(var1, var7, var5);
         }

         if (var1.y() < this.putAt_TextStore_JsonCach.floorY()) {
            return false;
         }

         int var9 = var6.putAt_I_TokenCore_TaskStat(var5);
         AiBridger.Inner_StreamUtil var10 = var9 > this.putAt_TextStore_JsonCach.length()
            ? var6.putAt_TextStore_TableCac_TokenCore_TableCac(var1, var7, var7, var3, var5)
            : var6.putAt(var1, var7, var7, var3, var5, var9);
         var7 = var7.stepToward(var10.yaw(), var10.pitch());
         if (var10.place() > 0) {
            var5.putAt_V(
               this.putAt_TextStore_JsonCach.cellX(var10.place()), this.putAt_TextStore_JsonCach.floorY(), this.putAt_TextStore_JsonCach.cellZ(var10.place())
            );
         }

         var1 = AiPhysics.putAt(var1, putAt_TokenCore_CacheHel_TextStore_TableCac_TextStore_CacheHel(var10, var7), var5);
      }

      return false;
   }

   private boolean putAt_Z_TokenCore_TableCac_TokenCore_TaskStat(AiPhysics.Inner_StreamUtil var1, AiPhysics.Inner_CachePool var2) {
      if (var1.onGround() && !(var1.y() < this.putAt_TextStore_JsonCach.floorY() + 1 - 0.01)) {
         int var3 = this.putAt_I_TokenCore_TaskStat(var2);
         double var4 = this.putAt_TextStore_JsonCach.along(var1.x(), var1.z());
         if (var3 <= this.putAt_TextStore_JsonCach.length() && !(var4 < var3 + 1)) {
            int var6 = (int)Math.floor(var4 - 0.3);
            return var6 > var3
               && fill(var2, this.putAt_TextStore_JsonCach.cellX(var6), this.putAt_TextStore_JsonCach.floorY(), this.putAt_TextStore_JsonCach.cellZ(var6));
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean putAt_Z_TokenCore_TableCac_2(AiPhysics.Inner_StreamUtil var0, AiPhysics.Inner_CachePool var1, int var2) {
      if (!(var0.y() < var2 + 1 - 0.01) && var0.onGround()) {
         AABB var3 = var0.box(false);
         AABB var4 = new AABB(var3.minX, var3.minY - 0.6, var3.minZ, var3.maxX, var3.minY, var3.maxZ);

         for (AABB var6 : var1.collisions(var4)) {
            if (var6.intersects(var4) && var6.maxY >= var3.minY - 0.001) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   double putAt_D_TokenCore_TaskStat_I(AiPhysics.Inner_CachePool var1, int var2) {
      int var3 = this.putAt_TextStore_JsonCach.cellX(var2);
      int var4 = this.putAt_TextStore_JsonCach.floorY();
      int var5 = this.putAt_TextStore_JsonCach.cellZ(var2);
      AABB var6 = new AABB(var3, var4, var5, var3 + 1, var4 + 1, var5 + 1).deflate(1.0E-4);
      double var7 = 0.0 / 0.0;

      for (AABB var10 : var1.collisions(var6)) {
         if (var10.intersects(var6) && var10.maxY < var4 + 1 - 0.001 && !(var7 >= var10.maxY)) {
            var7 = var10.maxY;
         }
      }

      return var7;
   }

   int[] putAt_IArr_F(float var1, AiPhysics.Inner_StreamUtil var2, float var3) {
      double var4 = this.putAt_TextStore_JsonCach.lateral(var2.x(), var2.z());
      double var6 = Math.toRadians(var3);
      double var8 = -Math.sin(var6);
      double var10 = Math.cos(var6);
      if (this.readEx_Z) {
         double var12 = Math.cos(Math.toRadians(this.putAt_TextStore_JsonCach.yaw()));
         double var14 = Math.sin(Math.toRadians(this.putAt_TextStore_JsonCach.yaw()));
         var8 -= Math.signum(var4) * var12 * 0.75;
         var10 -= Math.signum(var4) * var14 * 0.75;
      }

      double var30 = Math.toRadians(var1);
      double var31 = -Math.sin(var30);
      double var16 = Math.cos(var30);
      double var18 = Math.cos(var30);
      double var20 = Math.sin(var30);
      double var22 = var8 * var31 + var10 * var16;
      double var24 = var8 * var18 + var10 * var20;
      double var26 = Math.hypot(var8, var10);
      var22 /= var26;
      var24 /= var26;
      double var28 = Math.cos(Math.toRadians(67.5));
      return new int[]{var22 > var28 ? 1 : (var22 < -var28 ? -1 : 0), var24 > var28 ? 1 : (var24 < -var28 ? -1 : 0)};
   }

   static AiPhysics.Inner_DataUtil putAt_TokenCore_CacheHel_TextStore_TableCac_TextStore_CacheHel(
      AiBridger.Inner_StreamUtil var0, AiBridger.Inner_DataUtil var1
   ) {
      return new AiPhysics.Inner_DataUtil(var0.forward(), var0.left(), var0.jump(), var0.sprint(), var0.sneak(), var1.yaw());
   }

   static boolean putAt_Z_F_F(float var0, float var1) {
      return Math.abs(putAt_F_F(var0 - var1)) <= 12.0F;
   }

   static float putAt_F_F(float var0) {
      float var1 = var0 % 360.0F;
      if (var1 >= 180.0F) {
         var1 -= 360.0F;
      }

      if (var1 < -180.0F) {
         var1 += 360.0F;
      }

      return var1;
   }

   static float putAt_F_F_F(float var0, float var1) {
      return Math.max(-var1, Math.min(var1, var0));
   }

   static boolean fill(AiPhysics.Inner_CachePool var0, int var1, int var2, int var3) {
      AABB var4 = new AABB(var1, var2, var3, var1 + 1, var2 + 1, var3 + 1).deflate(1.0E-4);

      for (AABB var6 : var0.collisions(var4)) {
         if (var6.intersects(var4)) {
            return true;
         }
      }

      return false;
   }

   static int[] putAt_IArr_TokenCore_TaskStat(AiPhysics.Inner_CachePool var0, Vec3 var1, float var2, float var3, double var4) {
      double var6 = Math.toRadians(var2);
      double var8 = Math.toRadians(var3);
      double var10 = -Math.sin(var6) * Math.cos(var8);
      double var12 = -Math.sin(var8);
      double var14 = Math.cos(var6) * Math.cos(var8);
      Vec3 var16 = var1.add(var10 * var4, var12 * var4, var14 * var4);
      AABB var17 = new AABB(var1, var16).inflate(0.001);
      double var18 = 1.7976931348623157E308;
      int[] var20 = null;

      for (AABB var22 : var0.collisions(var17)) {
         double[] var23 = putAt_DArr_AABB(var22, var1, var10, var12, var14);
         if (var23 != null && !(var23[0] < 0.0) && !(var23[0] > var4) && !(var23[0] >= var18)) {
            var18 = var23[0];
            int var24 = (int)var23[1];
            int var25 = (int)var23[2];
            Vec3 var26 = var1.add(var10 * var18, var12 * var18, var14 * var18);
            double var27 = var26.x - (var24 == 0 ? var25 * 1.0E-4 : 0.0);
            double var29 = var26.y - (var24 == 1 ? var25 * 1.0E-4 : 0.0);
            double var31 = var26.z - (var24 == 2 ? var25 * 1.0E-4 : 0.0);
            var20 = new int[]{
               (int)Math.floor(var27), (int)Math.floor(var29), (int)Math.floor(var31), var24 == 0 ? var25 : 0, var24 == 1 ? var25 : 0, var24 == 2 ? var25 : 0
            };
         }
      }

      return var20;
   }

   private static double[] putAt_DArr_AABB(AABB var0, Vec3 var1, double var2, double var4, double var6) {
      double var8 = -1.7976931348623157E308;
      double var10 = 1.7976931348623157E308;
      int var12 = -1;
      byte var13 = 0;
      double[] var14 = new double[]{var1.x, var1.y, var1.z};
      double[] var15 = new double[]{var2, var4, var6};
      double[] var16 = new double[]{var0.minX, var0.minY, var0.minZ};
      double[] var17 = new double[]{var0.maxX, var0.maxY, var0.maxZ};

      for (int var18 = 0; var18 < 3; var18++) {
         if (Math.abs(var15[var18]) < 1.0E-12) {
            if (var14[var18] < var16[var18] || var14[var18] > var17[var18]) {
               return null;
            }
         } else {
            double var19 = (var16[var18] - var14[var18]) / var15[var18];
            double var21 = (var17[var18] - var14[var18]) / var15[var18];
            byte var23 = -1;
            if (var19 > var21) {
               double var24 = var19;
               var19 = var21;
               var21 = var24;
               var23 = 1;
            }

            if (var19 > var8) {
               var8 = var19;
               var12 = var18;
               var13 = var23;
            }

            var10 = Math.min(var10, var21);
            if (var8 > var10) {
               return null;
            }
         }
      }

      return var12 >= 0 && !(var10 < 0.0) ? new double[]{var8, var12, var13} : null;
   }

   public enum Inner_ConfigStore {
      init,
      load;
   }

   public record Inner_DataUtil(float yaw, float pitch) {
      AiBridger.Inner_DataUtil stepToward(float var1, float var2) {
         float var3 = AiBridger.putAt_F_F(var1 - this.yaw);
         float var4 = var2 - this.pitch;
         return new AiBridger.Inner_DataUtil(this.yaw + AiBridger.putAt_F_F_F(var3, 100.0F), this.pitch + AiBridger.putAt_F_F_F(var4, 100.0F));
      }
   }

   public record Inner_CachePool(int startX, int startZ, int floorY, int dx, int dz, int length) {
      public Inner_CachePool(int startX, int startZ, int floorY, int dx, int dz, int length) {
         if (Math.abs(dx) + Math.abs(dz) != 1) {
            throw new IllegalArgumentException("a lane is cardinal");
         }

         if (length < 1) {
            throw new IllegalArgumentException("a lane places at least one support");
         }

         this.startX = startX;
         this.startZ = startZ;
         this.floorY = floorY;
         this.dx = dx;
         this.dz = dz;
         this.length = length;
      }

      public int cellX(int var1) {
         return this.startX + this.dx * var1;
      }

      public int cellZ(int var1) {
         return this.startZ + this.dz * var1;
      }

      public float yaw() {
         return AiPhysics.putAt_F_D(0.0, 0.0, this.dx, this.dz);
      }

      public double along(double var1, double var3) {
         if (this.dx == 1) {
            return var1 - this.startX;
         } else if (this.dx == -1) {
            return this.startX + 1 - var1;
         } else {
            return this.dz == 1 ? var3 - this.startZ : this.startZ + 1 - var3;
         }
      }

      public double[] point(double var1, double var3) {
         double var5 = Math.cos(Math.toRadians(this.yaw()));
         double var7 = Math.sin(Math.toRadians(this.yaw()));
         return new double[]{this.startX + 0.5 + this.dx * (var1 - 0.5) + var5 * var3, this.startZ + 0.5 + this.dz * (var1 - 0.5) + var7 * var3};
      }

      public double lateral(double var1, double var3) {
         double var5 = Math.cos(Math.toRadians(this.yaw()));
         double var7 = Math.sin(Math.toRadians(this.yaw()));
         return (var1 - (this.startX + 0.5)) * var5 + (var3 - (this.startZ + 0.5)) * var7;
      }
   }

   public record Inner_StreamUtil(float yaw, float pitch, int forward, int left, boolean jump, boolean sprint, boolean sneak, int place, boolean done) {
      static AiBridger.Inner_StreamUtil idle(float var0, float var1) {
         return new AiBridger.Inner_StreamUtil(var0, var1, 0, 0, false, false, false, -1, false);
      }
   }

   static final class Inner_BufferHelper implements AiPhysics.Inner_CachePool {
      private final AiPhysics.Inner_CachePool putAt_TokenCore_TaskStat;
      private final List<AABB> putAt_List = new ArrayList<>();

      Inner_BufferHelper(AiPhysics.Inner_CachePool var1) {
         this.putAt_TokenCore_TaskStat = var1;
      }

      void putAt_V(int var1, int var2, int var3) {
         this.putAt_List.add(new AABB(var1, var2, var3, var1 + 1, var2 + 1, var3 + 1));
      }

      @Override
      public List<AABB> collisions(AABB var1) {
         var var2 = this.putAt_TokenCore_TaskStat.collisions(var1);
         if (this.putAt_List.isEmpty()) {
            return var2;
         }

         ArrayList var3 = new ArrayList(var2);

         for (AABB var5 : this.putAt_List) {
            if (var5.intersects(var1)) {
               var3.add(var5);
            }
         }

         return var3;
      }

      @Override
      public double putAt_D_I(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.putAt_D_I(var1, var2, var3);
      }

      @Override
      public double fill_D(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.fill_D(var1, var2, var3);
      }

      @Override
      public double readEx_D(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.readEx_D(var1, var2, var3);
      }

      @Override
      public double value_D(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.value_D(var1, var2, var3);
      }

      @Override
      public boolean putAt_Z_I_2(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.putAt_Z_I_2(var1, var2, var3);
      }

      @Override
      public boolean fill_Z(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.fill_Z(var1, var2, var3);
      }

      @Override
      public boolean readEx_Z(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.readEx_Z(var1, var2, var3);
      }

      @Override
      public boolean value_Z(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.value_Z(var1, var2, var3);
      }
   }

   // 5.3 external ABI bridges
   public AiBridger.Inner_CachePool init_JsonUtil_CachePool() { return this.putAt_TextStore_JsonCach(); }
   public AiBridger.Inner_ConfigStore init_JsonUtil_ConfigSto() { return this.putAt_TextStore_BitCore(); }
   public void init_V_JsonUtil_ConfigSto(AiBridger.Inner_ConfigStore value) { this.putAt_V_TextStore_BitCore(value); }
   public int init_I_NodeWalker_CachePo(AiPhysics.Inner_CachePool world) { return this.putAt_I_TokenCore_TaskStat(world); }
   public AiBridger.Inner_StreamUtil init_JsonUtil_StreamUti_NodeWalker_StreamU_2(AiPhysics.Inner_StreamUtil s, AiBridger.Inner_DataUtil r, boolean sneaking, AiPhysics.Inner_CachePool world) {
      return this.putAt_TextStore_TableCac_TokenCore_TableCac_2(s, r, sneaking, world);
   }
   public AiBridger.Inner_StreamUtil init_JsonUtil_StreamUti_NodeWalker_StreamU_2(AiPhysics.Inner_StreamUtil s, AiBridger.Inner_DataUtil r, boolean sneaking, AiPhysics.Inner_CachePool world, boolean forceSneak) {
      return this.putAt_TextStore_TableCac_TokenCore_TableCac_2(s, r, sneaking, world, forceSneak);
   }
}
