package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class RotationMath {
   private RotationMath() {
   }

   public static RotationMath.Rotation ofPlayer(LocalPlayer player) {
      return new RotationMath.Rotation(player.getYRot(), player.getXRot());
   }

   public static RotationMath.Rotation lookAt(Vec3 point, Vec3 from) {
      Vec3 diff = point.subtract(from);
      double horiz = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
      float yaw = (float)(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(diff.y, horiz)));
      return new RotationMath.Rotation(Mth.wrapDegrees(yaw), Mth.clamp(Mth.wrapDegrees(pitch), -90.0F, 90.0F));
   }

   public static RotationMath.Rotation stepToward(RotationMath.Rotation current, RotationMath.Rotation target, float horizontalFactor, float verticalFactor) {
      float yawDiff = angleDelta(target.yaw, current.yaw);
      float pitchDiff = angleDelta(target.pitch, current.pitch);
      double length = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
      if (length < 1.0E-9) {
         return target;
      }

      float yawStep = Math.abs((float)(yawDiff / length)) * horizontalFactor;
      float pitchStep = Math.abs((float)(pitchDiff / length)) * verticalFactor;
      return new RotationMath.Rotation(current.yaw + Mth.clamp(yawDiff, -yawStep, yawStep), current.pitch + Mth.clamp(pitchDiff, -pitchStep, pitchStep));
   }

   public static RotationMath.Rotation lerp(RotationMath.Rotation current, RotationMath.Rotation target, float factor) {
      float t = Mth.clamp(factor, 0.0F, 1.0F);
      return new RotationMath.Rotation(
         current.yaw + angleDelta(target.yaw, current.yaw) * t, Mth.clamp(current.pitch + angleDelta(target.pitch, current.pitch) * t, -90.0F, 90.0F)
      );
   }

   public static RotationMath.Rotation applyGcd(RotationMath.Rotation rotation, RotationMath.Rotation current) {
      double gcd = mouseGcd();
      if (gcd <= 0.0) {
         return rotation;
      }

      float yawDiff = angleDelta(rotation.yaw, current.yaw);
      float pitchDiff = angleDelta(rotation.pitch, current.pitch);
      float yaw = current.yaw + (float)(Math.round(yawDiff / gcd) * gcd);
      float pitch = current.pitch + (float)(Math.round(pitchDiff / gcd) * gcd);
      return new RotationMath.Rotation(yaw, Mth.clamp(pitch, -90.0F, 90.0F));
   }

   public static RotationMath.Rotation stepQuantized(RotationMath.Rotation current, RotationMath.Rotation goal, float yawBudget, float pitchBudget, double gcd) {
      if (current == null) {
         return goal;
      }

      if (goal == null) {
         return current;
      }

      double quantum = Double.isFinite(gcd) && gcd > 0.0 ? gcd : 0.15;
      return new RotationMath.Rotation(
         current.yaw() + quantizeStep(angleDelta(goal.yaw(), current.yaw()), yawBudget, quantum),
         Mth.clamp(current.pitch() + quantizeStep(goal.pitch() - current.pitch(), pitchBudget, quantum), -90.0F, 90.0F)
      );
   }

   public static RotationMath.Rotation stepQuantizedUnwrapped(RotationMath.Rotation current, RotationMath.Rotation goal, float yawBudget, float pitchBudget, double gcd) {
      if (current == null) {
         return goal;
      }

      if (goal == null) {
         return current;
      }

      double quantum = Double.isFinite(gcd) && gcd > 0.0 ? gcd : 0.15;
      return new RotationMath.Rotation(
         current.yaw() + quantizeStep(goal.yaw() - current.yaw(), yawBudget, quantum),
         Mth.clamp(current.pitch() + quantizeStep(goal.pitch() - current.pitch(), pitchBudget, quantum), -90.0F, 90.0F)
      );
   }

   private static float quantizeStep(double delta, float budget, double quantum) {
      if (Double.isFinite(delta) && Float.isFinite(budget) && !(budget <= 0.0F)) {
         long maximum = (long)Math.floor(budget / quantum + 1.0E-9);
         long counts = Math.max(-maximum, Math.min(maximum, Math.round(delta / quantum)));
         return (float)(counts * quantum);
      } else {
         return 0.0F;
      }
   }

   public static double gcdStep() {
      return mouseGcd();
   }

   public static double mouseGcd() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.options != null) {
         double f = mc.options.sensitivity().get() * 0.6 + 0.2;
         float sensitivityFactor = (float)(f * f * f * 8.0);
         return sensitivityFactor * 0.15F;
      } else {
         return 0.0;
      }
   }

   public static float angleDelta(float target, float current) {
      return Mth.wrapDegrees(target - current);
   }

   public static float angularDistance(RotationMath.Rotation current, RotationMath.Rotation target) {
      float yaw = angleDelta(target.yaw, current.yaw);
      float pitch = angleDelta(target.pitch, current.pitch);
      return (float)Math.min(180.0, Math.sqrt(yaw * yaw + pitch * pitch));
   }

   public static float vectorAngleDegrees(RotationMath.Rotation a, RotationMath.Rotation b) {
      Vec3 first = Vec3.directionFromRotation(a.pitch(), a.yaw());
      Vec3 second = Vec3.directionFromRotation(b.pitch(), b.yaw());
      double cosine = Mth.clamp(first.dot(second), -1.0, 1.0);
      return (float)Math.toDegrees(Math.acos(cosine));
   }

   public static void applyToPlayer(LocalPlayer player, RotationMath.Rotation rotation, boolean syncPrevious) {
      if (player != null && rotation != null) {
         float yaw = Mth.wrapDegrees(rotation.yaw);
         float pitch = Mth.clamp(rotation.pitch, -90.0F, 90.0F);
         if (syncPrevious) {
            player.yRotO = yaw;
            player.xRotO = pitch;
            player.setYHeadRot(yaw);
            player.yHeadRotO = yaw;
            player.setYBodyRot(yaw);
            player.yBodyRotO = yaw;
         } else {
            player.yRotO = player.getYRot();
            player.xRotO = player.getXRot();
         }

         player.setYRot(yaw);
         player.setXRot(pitch);
      }
   }

   public record Rotation(float yaw, float pitch) {
   }
}
