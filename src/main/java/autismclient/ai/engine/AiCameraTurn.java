package autismclient.ai.engine;

import autismclient.util.RotationMath;
import java.lang.ref.WeakReference;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public final class AiCameraTurn {
   private static WeakReference<LocalPlayer> putAt_WeakReference = new WeakReference<>(null);
   private static AiCameraTurn.Inner_CacheHelper putAt_SyncPool_CacheHelp;

   private AiCameraTurn() {
   }

   public static void recordTurn(LocalPlayer var0, float var1, float var2) {
      if (var0 != null) {
         float var3 = var0.getYRot();
         float var4 = var0.getXRot();
         if (var3 != var1 || var4 != var2) {
            long var5 = System.nanoTime();
            if (putAt_WeakReference.get() != var0) {
               putAt_WeakReference = new WeakReference<>(var0);
               putAt_SyncPool_CacheHelp = null;
            }

            AiCameraTurn.Inner_CacheHelper var7 = putAt_SyncPool_CacheHelp;
            if (var7 != null && var7.hasTarget(var1, var2)) {
               var7.putAt_V_F(var3, var4, var5);
            } else if (Math.abs(Mth.wrapDegrees(var3 - var1)) < 0.12F && Math.abs(var4 - var2) < 0.12F) {
               putAt_SyncPool_CacheHelp = null;
            } else {
               putAt_SyncPool_CacheHelp = new AiCameraTurn.Inner_CacheHelper(var1, var2, var3, var4, var5);
            }
         }
      }
   }

   public static RotationMath.Rotation cameraRotation(LocalPlayer var0) {
      AiCameraTurn.Inner_CacheHelper var1 = putAt_SyncPool_CacheHelp;
      if (var1 == null) {
         return null;
      }

      if (var0 != null && putAt_WeakReference.get() == var0 && var1.hasTarget(var0.getYRot(), var0.getXRot())) {
         long var2 = System.nanoTime();
         RotationMath.Rotation var4 = var1.putAt_TaskUtil_CacheHelp_J(var2);
         if (var1.isSettled(var2)) {
            putAt_SyncPool_CacheHelp = null;
            return null;
         } else {
            return var4;
         }
      } else {
         putAt_SyncPool_CacheHelp = null;
         return null;
      }
   }

   static final class Inner_CacheHelper {
      static final double putAt_D = 23.0;
      static final double fill_D = 36.0;
      static final double readEx_D = 21.0;
      static final double value_D = 32.0;
      static final double prevEx_D = 32.0;
      static final double prev = 18.0;
      static final double chunkAt = 0.004;
      static final double state = 0.05;
      static final float putAt_F = 0.12F;
      static final float fill_F = 0.04F;
      static final float readEx_F = 1.2F;
      static final long putAt_J = 600000000L;
      private double cameraYaw;
      private double cameraPitch;
      private double guideYaw;
      private double guidePitch;
      private double yawVelocity;
      private double pitchVelocity;
      private float value_F;
      private float prevEx_F;
      private long lastStepNanos;
      private long lastTargetNanos;

      Inner_CacheHelper(float var1, float var2, float var3, float var4, long var5) {
         this.cameraYaw = Mth.wrapDegrees(var1);
         this.cameraPitch = Math.clamp(var2, -90.0, 90.0);
         this.value_F = var3;
         this.prevEx_F = Math.clamp(var4, -90.0F, 90.0F);
         double var7 = Mth.wrapDegrees(var3 - var1);
         double var9 = this.prevEx_F - this.cameraPitch;
         double var11 = Math.hypot(var7, var9);
         double var13 = var11 >= 18.0 ? 1.0 : Math.clamp((var11 - 4.0) / 14.0, 0.5, 1.0);
         this.guideYaw = Mth.wrapDegrees(this.cameraYaw + var7 * var13);
         this.guidePitch = Math.clamp(this.cameraPitch + var9 * var13, -90.0, 90.0);
         this.lastStepNanos = var5;
         this.lastTargetNanos = var5;
      }

      boolean hasTarget(float var1, float var2) {
         return this.value_F == var1 && this.prevEx_F == var2;
      }

      void putAt_V_F(float var1, float var2, long var3) {
         this.advanceTo(var3);
         float var5 = Math.clamp(var2, -90.0F, 90.0F);
         double var6 = Mth.wrapDegrees(var1 - this.value_F);
         double var8 = var5 - this.prevEx_F;
         double var10 = Mth.wrapDegrees(var1 - this.cameraYaw);
         double var12 = Math.hypot(var6, var8);
         this.value_F = var1;
         this.prevEx_F = var5;
         if (!(var12 >= 18.0) && !(Math.abs(var10) >= 35.0)) {
            double var14 = Math.clamp(var12 / 18.0, 0.5, 0.9);
            this.guideYaw = Mth.wrapDegrees(this.guideYaw + Mth.wrapDegrees(var1 - this.guideYaw) * var14);
            this.guidePitch = Math.clamp(this.guidePitch + (var5 - this.guidePitch) * var14, -90.0, 90.0);
         } else {
            this.guideYaw = Mth.wrapDegrees(var1);
            this.guidePitch = var5;
         }

         this.lastTargetNanos = var3;
      }

      RotationMath.Rotation putAt_TaskUtil_CacheHelp_J(long var1) {
         this.advanceTo(var1);
         return new RotationMath.Rotation(Mth.wrapDegrees((float)this.cameraYaw), Math.clamp((float)this.cameraPitch, -90.0F, 90.0F));
      }

      double yawSpeed() {
         return this.yawVelocity;
      }

      double pitchSpeed() {
         return this.pitchVelocity;
      }

      boolean isSettled(long var1) {
         if (var1 - this.lastTargetNanos >= 600000000L) {
            return true;
         }

         double var3 = Math.abs(Mth.wrapDegrees(this.cameraYaw - this.value_F));
         double var5 = Math.abs(this.cameraPitch - this.prevEx_F);
         return var3 < 0.03999999910593033
            && var5 < 0.03999999910593033
            && Math.abs(this.yawVelocity) < 1.2000000476837158
            && Math.abs(this.pitchVelocity) < 1.2000000476837158;
      }

      private void advanceTo(long var1) {
         long var3 = var1 - this.lastStepNanos;
         if (var3 > 0L) {
            this.lastStepNanos = var1;
            double var5 = Math.min(var3 * 1.0E-9, 0.05);

            while (var5 > 1.0E-9) {
               double var7 = Math.min(var5, 0.004);
               this.putAt_V_D(var7);
               var5 -= var7;
            }
         }
      }

      private void putAt_V_D(double var1) {
         double var3 = Mth.wrapDegrees(this.value_F - this.guideYaw);
         double var5 = this.prevEx_F - this.guidePitch;
         double var7 = Math.hypot(var3, var5);
         double var9 = var7 >= 18.0 ? 110.0 : 32.0 + var7 * 2.2;
         double var11 = 1.0 - Math.exp(-var9 * var1);
         this.guideYaw = Mth.wrapDegrees(this.guideYaw + var3 * var11);
         this.guidePitch = Math.clamp(this.guidePitch + var5 * var11, -90.0, 90.0);
         double var13 = Mth.wrapDegrees(this.cameraYaw - this.guideYaw);
         double var15 = this.cameraPitch - this.guidePitch;
         double var17 = Math.hypot(Math.abs(var13), Math.abs(var15));
         double var19 = 23.0 + 13.0 * smoothstep(10.0, 90.0, var17);
         double var21 = 21.0 + 11.0 * smoothstep(8.0, 75.0, Math.abs(var15));
         double var23 = -var19 * var19 * var13 - 2.0 * var19 * this.yawVelocity;
         this.yawVelocity += var23 * var1;
         this.cameraYaw = Mth.wrapDegrees(this.cameraYaw + this.yawVelocity * var1);
         double var25 = -var21 * var21 * var15 - 2.0 * var21 * this.pitchVelocity;
         this.pitchVelocity += var25 * var1;
         double var27 = this.cameraPitch + this.pitchVelocity * var1;
         if (!(var27 < -90.0) && !(var27 > 90.0)) {
            this.cameraPitch = var27;
         } else {
            this.cameraPitch = Math.clamp(var27, -90.0, 90.0);
            this.pitchVelocity = 0.0;
         }
      }

      private static double smoothstep(double var0, double var2, double var4) {
         double var6 = Math.clamp((var4 - var0) / (var2 - var0), 0.0, 1.0);
         return var6 * var6 * (3.0 - 2.0 * var6);
      }
   }
}
