package autismclient.ai.engine;

import autismclient.util.KeyMappingControl;
import java.util.List;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AiMotionSafety {
   private static final boolean DEBUG_NAV = Boolean.getBoolean("autism.navigation.debug");
   static final double putAt_D = 0.5;
   private static LocalPlayer sprintSuppressedPlayer;
   private static int sprintSuppressedTick;
   private static volatile float pitchFloorDegrees;
   private static volatile long pitchFloorUntilNanos;

   private AiMotionSafety() {
   }

   static void setSprintAllowed(LocalPlayer var0, boolean var1) {
      sprintSuppressedPlayer = var1 ? null : var0;
      sprintSuppressedTick = var0 == null ? 0 : var0.tickCount;
   }

   public static boolean isSprintSuppressed() {
      LocalPlayer var0 = Minecraft.getInstance().player;
      if (var0 != null && var0 == sprintSuppressedPlayer) {
         int var1 = var0.tickCount - sprintSuppressedTick;
         return var1 >= 0 && var1 <= 1;
      } else {
         return false;
      }
   }

   public static void releaseMovementKeys() {
      sprintSuppressedPlayer = null;
      Minecraft var0 = Minecraft.getInstance();
      if (var0.options != null) {
         for (KeyMapping var2 : List.of(
            var0.options.keyUp,
            var0.options.keyDown,
            var0.options.keyLeft,
            var0.options.keyRight,
            var0.options.keyJump,
            var0.options.keySprint,
            var0.options.keyShift
         )) {
            var2.setDown(KeyMappingControl.of(var2).autism$physicalForBody());
         }
      }
   }

   public static AiPhysics.MotionState snapshotPlayer(LocalPlayer var0) {
      return AiPhysics.MotionState.of(var0);
   }

   public static boolean isPlayerSupported() {
      Minecraft var0 = Minecraft.getInstance();
      LocalPlayer var1 = var0.player;
      return var1 != null
         && (
            var1.isInWater()
               || var1.onClimbable()
               || var1.onGround()
                  && var0.level != null
                  && hasSupportBelowBox(var1.getBoundingBox(), var2 -> var0.level.getBlockCollisions(var1, var2).iterator().hasNext())
         );
   }

   static boolean hasSupportBelowBox(AABB var0, Predicate<AABB> var1) {
      return var1.test(new AABB(var0.minX, var0.minY - 1.0E-6, var0.minZ, var0.maxX, var0.minY, var0.maxZ));
   }

   static boolean hasSolidSupport(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1) {
      AABB var2 = var1.box(false);
      AABB var3 = new AABB(var2.minX, var2.minY - 0.001, var2.minZ, var2.maxX, var2.minY, var2.maxZ);

      for (AABB var5 : var0.collisions(var3)) {
         if (var5.intersects(var3)) {
            return true;
         }
      }

      return false;
   }

   static AiPhysics.MoveInput findSafeBrakeInput(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MoveInput var2, float var3, LongPredicate var4
   ) {
      double var5 = Math.toRadians(var2.yaw());
      double var7 = -Math.sin(var5) * var1.vx() + Math.cos(var5) * var1.vz();
      double var9 = Math.cos(var5) * var1.vx() + Math.sin(var5) * var1.vz();
      AiPhysics.MoveInput var11 = new AiPhysics.MoveInput(
         var7 > 0.005 ? -1.0 : (var7 < -0.005 ? 1.0 : 0.0), var9 > 0.005 ? -1.0 : (var9 < -0.005 ? 1.0 : 0.0), false, false, var2.sneak(), var2.yaw()
      );

      for (AiPhysics.MoveInput var15 : new AiPhysics.MoveInput[]{
         new AiPhysics.MoveInput(0.0, 0.0, false, false, var2.sneak(), var2.yaw()), var11
      }) {
         if (canRecoverFromInput(var0, var1, var15, var3, var4)) {
            return var15;
         }
      }

      return null;
   }

   static boolean canRecoverFromInput(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MoveInput var2, float var3, LongPredicate var4
   ) {
      for (int var5 = 0; var5 < 2; var5++) {
         AiPhysics.MotionState var6 = AiPhysics.step(var1, var2, var0);
         if (AiPhysics.hitsHazard(var0, var1, var6)) {
            return false;
         }

         var1 = var6;
         if (!var1.onGround()) {
            for (AiPhysics.MoveInput var10 : new AiPhysics.MoveInput[]{
               AiPhysics.MoveInput.running(var3, var2.sprint()), AiPhysics.MoveInput.NONE, AiPhysics.MoveInput.running(var3 + 180.0F, false)
            }) {
               AiStride.FallResult var11 = AiStride.putAt(var1, var10, false, var0);
               if (var11.down() && var4.test(AiStride.putAt_J_D_D(var11.at().x(), var11.at().z())) && AiStride.putAt(var11.at(), var4, var0)) {
                  return true;
               }
            }

            return false;
         }
      }

      return true;
   }

   static AiPhysics.MoveInput centerOnBlockInput(LocalPlayer var0) {
      double var1 = Math.floor(var0.getX()) + 0.5;
      double var3 = Math.floor(var0.getZ()) + 0.5;
      double var5 = var1 - var0.getX();
      double var7 = var3 - var0.getZ();
      if (var5 * var5 + var7 * var7 < 0.0064) {
         return null;
      }

      int[] var9 = relativeMoveAxes(var0.getYRot(), AiPhysics.yawBetween(var0.getX(), var0.getZ(), var1, var3));
      return new AiPhysics.MoveInput(var9[0], -var9[1], false, false, true, var0.getYRot());
   }

   static boolean isMoveSafe(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MoveInput var2, int var3) {
      boolean var4 = !var1.onGround();
      int var5 = var4 ? Math.max(var3, 64) : var3;

      for (int var6 = 0; var6 < var5; var6++) {
         AiPhysics.MotionState var7 = AiPhysics.step(var1, var6 == 0 ? var2 : var2.withJump(false), var0);
         if (AiPhysics.hitsHazard(var0, var1, var7)) {
            return false;
         }

         if (var4 && var7.onGround() && hasSolidSupport(var0, var7)) {
            return true;
         }

         var4 |= !var7.onGround();
         if (var4) {
            var5 = Math.max(var5, 64);
         }

         var1 = var7;
      }

      return !var4;
   }

   public static boolean isHorizontalMoveSafe(double var0, double var2) {
      return isPlayerMoveSafe(var0, var2, false, false);
   }

   public static boolean isPlayerMoveSafe(double var0, double var2, boolean var4, boolean var5) {
      Minecraft var6 = Minecraft.getInstance();
      if (var6.player != null && var6.level != null) {
         LocalPlayer var7 = var6.player;
         return isMoveSafe(
            new AiLiveWorld(var6.level, var7),
            AiPhysics.MotionState.of(var7),
            new AiPhysics.MoveInput(var0, var2, var4, var5, var7.isShiftKeyDown(), var7.getYRot()),
            var4 ? 30 : 2
         );
      } else {
         return false;
      }
   }

   public static boolean isSegmentHazardFree(Vec3 var0, Vec3 var1) {
      Minecraft var2 = Minecraft.getInstance();
      if (var2.level == null) {
         return false;
      }

      AiLiveWorld var3 = new AiLiveWorld(var2.level, var2.player);
      int var4 = Math.max(1, (int)Math.ceil(var0.distanceTo(var1) * 4.0));
      AiPhysics.MotionState var5 = new AiPhysics.MotionState(var0.x, var0.y, var0.z, 0.0, 0.0, 0.0, true, 0, false, false);

      for (int var6 = 1; var6 <= var4; var6++) {
         Vec3 var7 = var0.lerp(var1, (double)var6 / var4);
         AiPhysics.MotionState var8 = new AiPhysics.MotionState(var7.x, var7.y, var7.z, 0.0, 0.0, 0.0, true, 0, false, false);
         if (AiPhysics.hitsHazard(var3, var5, var8)) {
            return false;
         }

         var5 = var8;
      }

      return true;
   }

   public static void applyEmergencyBrake() {
      Minecraft var0 = Minecraft.getInstance();
      if (var0.player != null) {
         LocalPlayer var1 = var0.player;
         releaseMovementKeys();
         var1.setSprinting(false);
         var0.options.keyShift.setDown(true);
         double var2 = Math.toRadians(var1.getYRot());
         Vec3 var4 = var1.getDeltaMovement();
         double var5 = -Math.sin(var2) * var4.x + Math.cos(var2) * var4.z;
         double var7 = Math.cos(var2) * var4.x + Math.sin(var2) * var4.z;
         var0.options.keyDown.setDown(var5 > 0.005);
         var0.options.keyUp.setDown(var5 < -0.005);
         var0.options.keyRight.setDown(var7 > 0.005);
         var0.options.keyLeft.setDown(var7 < -0.005);
      }
   }

   static boolean canStopSafelyAfter(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MoveInput var2) {
      double var3 = var1.y();
      AiPhysics.MotionState var5 = AiPhysics.step(var1, var2, var0);
      if (AiPhysics.hitsHazard(var0, var1, var5)) {
         return false;
      }

      for (int var6 = 0; var6 < 64; var6++) {
         if (!AiGroundContinuity.putAt(var0, var1, var5, var3)) {
            return false;
         }

         if (var5.onGround()) {
            var3 = var5.y();
            if (var5.speed() < 0.01) {
               return true;
            }
         }

         var1 = var5;
         var5 = AiPhysics.step(var1, AiPhysics.MoveInput.NONE, var0);
         if (AiPhysics.hitsHazard(var0, var1, var5)) {
            return false;
         }
      }

      return false;
   }

   static boolean canCoastToSafeStop(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1) {
      return canCoastToSafeStopWithFall(var0, var1, 0.0);
   }

   static boolean canCoastToSafeStopWithFall(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, double var2) {
      double var4 = var1.y() + Math.max(0.0, var2);
      int var6 = 0;

      for (int var7 = 0; var7 < 64; var7++) {
         AiPhysics.MotionState var8 = AiPhysics.step(var1, AiPhysics.MoveInput.NONE, var0);
         if (AiPhysics.hitsHazard(var0, var1, var8)) {
            return false;
         }

         if (var8.onGround()) {
            if (var4 - var8.y() > 3.000001) {
               return false;
            }

            var4 = var8.y();
            if (++var6 >= 8 && var8.speed() < 0.01) {
               return true;
            }
         } else {
            var6 = 0;
            var4 = Math.max(var4, var8.y());
         }

         var1 = var8;
      }

      return false;
   }

   static boolean isSupportedByAnyBox(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, List<AABB> var2) {
      for (int var3 = 0; var3 < 64; var3++) {
         AABB var4 = var1.box(false);

         for (AABB var6 : var2) {
            if (Math.abs(var6.maxY - var4.minY) <= 0.08 && var6.maxX > var4.minX && var6.minX < var4.maxX && var6.maxZ > var4.minZ && var6.minZ < var4.maxZ) {
               return true;
            }
         }

         if (var1.onGround() && var1.speed() < 0.01 && var3 > 0) {
            return false;
         }

         var1 = AiPhysics.step(var1, AiPhysics.MoveInput.NONE, var0);
      }

      return false;
   }

   public static boolean isSafeToBreakSupport(BlockPos var0) {
      Minecraft var1 = Minecraft.getInstance();
      if (var1.player != null && var1.level != null) {
         var var2 = var1.level
            .getBlockState(var0)
            .getCollisionShape(var1.level, var0)
            .toAabbs()
            .stream()
            .map(var1x -> var1x.move(var0.getX(), var0.getY(), var0.getZ()))
            .filter(var1x -> var1x.maxY <= var1.player.getY() + 0.08)
            .toList();
         if (var2.isEmpty()) {
            return true;
         }

         AiPhysics.MotionState var3 = snapshotPlayer(var1.player);
         if (!isSupportedByAnyBox(new AiLiveWorld(var1.level, var1.player), var3, var2)) {
            return true;
         }

         boolean var4 = canCoastToSafeStopWithFall(new AiLiveWorld(var1.level, var1.player, var0), var3, var1.player.onGround() ? 0.0 : var1.player.fallDistance);
         if (DEBUG_NAV && !var4) {
            AiNavDebug.log("unsafe support removal target=" + var0 + " state=" + var3 + " fallen=" + var1.player.fallDistance);
         }

         return var4;
      } else {
         return false;
      }
   }

   static AiPhysics.MoveInput findSafeDropInput(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, double var3, LongPredicate var5
   ) {
      if (var1.y() - var3 > 3.00001) {
         return null;
      }

      AiPhysics.MoveInput var6 = AiPhysics.MoveInput.running(var2, false);
      AiPhysics.MoveInput[] var7 = new AiPhysics.MoveInput[]{
         var6, AiPhysics.MoveInput.NONE.withYaw(var2), new AiPhysics.MoveInput(-1.0, 0.0, false, false, false, var2)
      };
      int var8 = var1.onGround() ? 1 : var7.length;

      for (int var9 = 0; var9 < var8; var9++) {
         AiPhysics.MoveInput var10 = var7[var9];
         AiPhysics.MotionState var11 = var1;

         for (int var12 = 0; var12 < 24; var12++) {
            AiPhysics.MotionState var13 = AiPhysics.step(var11, var12 == 0 ? var10 : var6, var0);
            if (var13.y() < var3 - 0.02 || var13.y() > var1.y() + 0.02 || AiPhysics.hitsStrictHazard(var0, var11, var13)) {
               break;
            }

            if (isSafeLandingState(var0, var13, var3, var5)) {
               return var10;
            }

            if (!var13.onGround()) {
               for (AiPhysics.MoveInput var17 : var7) {
                  AiStride.FallResult var18 = AiStride.putAt(var13, var17, false, var0);
                  if (var18.down() && isSafeLandingState(var0, var18.at(), var3, var5)) {
                     return var10;
                  }
               }
            }

            var11 = var13;
         }
      }

      return null;
   }

   private static boolean isSafeLandingState(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, double var2, LongPredicate var4) {
      return var1.onGround()
         && Math.abs(var1.y() - var2) <= 0.02
         && var4.test(AiStride.putAt_J_D_D(var1.x(), var1.z()))
         && hasSolidSupport(var0, var1)
         && AiStride.putAt(var1, var4, var0);
   }

   public static int[] relativeMoveAxes(float var0, float var1) {
      double var2 = Math.toRadians(Mth.wrapDegrees(var1 - var0));
      double var4 = Math.cos(var2);
      double var6 = Math.sin(var2);
      double var8 = Math.cos(Math.toRadians(67.5));
      return new int[]{var4 > var8 ? 1 : (var4 < -var8 ? -1 : 0), var6 > var8 ? 1 : (var6 < -var8 ? -1 : 0)};
   }

   public static void setPitchFloor(float var0, long var1) {
      pitchFloorDegrees = var0;
      pitchFloorUntilNanos = var1 <= 0L ? 0L : System.nanoTime() + var1;
   }

   static Vec3 applyPitchFloor(LocalPlayer var0, Vec3 var1) {
      if (System.nanoTime() >= pitchFloorUntilNanos) {
         return var1;
      }

      Vec3 var2 = var0.getEyePosition();
      Vec3 var3 = var1.subtract(var2);
      double var4 = var3.length();
      return !(var4 < 0.001) && !(AiDigLine.load(var3) >= pitchFloorDegrees) ? var2.add(Vec3.directionFromRotation(pitchFloorDegrees, AiDigLine.init(var3)).scale(var4)) : var1;
   }

   static float activePitchFloor() {
      return System.nanoTime() < pitchFloorUntilNanos ? pitchFloorDegrees : 0.0F / 0.0F;
   }

   static float requiredPitchForTarget(Vec3 var0, float var1, float var2, Vec3 var3) {
      boolean var4 = var3.y < var0.y;
      int var5 = Mth.floor(var3.y);
      return requiredPitchForPlane(var0, var1, var2, var4, var4 ? var5 + 1 : var5);
   }

   static float requiredPitchForPlane(Vec3 var0, float var1, float var2, boolean var3, double var4) {
      double var6 = var3 ? var0.y - var4 : var4 - var0.y;
      if (var6 <= 0.0) {
         return var3 ? 90.0F : -90.0F;
      } else {
         double var8 = Math.toRadians(var1);
         double var10 = -Math.sin(var8);
         double var12 = Math.cos(var8);
         int var14 = Mth.floor(var0.x);
         int var15 = Mth.floor(var0.z);
         double var16 = var10 > 1.0E-9 ? (var14 + 1 - var0.x) / var10 : (var10 < -1.0E-9 ? (var14 - var0.x) / var10 : 1.7976931348623157E308);
         double var18 = var12 > 1.0E-9 ? (var15 + 1 - var0.z) / var12 : (var12 < -1.0E-9 ? (var15 - var0.z) / var12 : 1.7976931348623157E308);
         double var20 = Math.min(var16, var18);
         float var22 = (float)Math.toDegrees(Math.atan2(var6, Math.max(0.02, var20 - 0.1)));
         float var23 = (float)Math.min(90.0, Math.toDegrees(Math.atan2(var6, Math.max(0.02, var20 - 0.3))));
         if (var3) {
            return var2 >= var22 ? var2 : var23;
         } else {
            return -var2 >= var22 ? var2 : -var23;
         }
      }
   }
}
