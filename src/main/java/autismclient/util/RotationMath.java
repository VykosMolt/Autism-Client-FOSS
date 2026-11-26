package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class RotationMath {
   private RotationMath() {
   }

   public static RotationMath.Rotation init(LocalPlayer player) {
      return new RotationMath.Rotation(player.getYRot(), player.getXRot());
   }

   public static RotationMath.Rotation init(Vec3 point, Vec3 from) {
      Vec3 diff = point.subtract(from);
      double horiz = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
      float yaw = (float)(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(diff.y, horiz)));
      return new RotationMath.Rotation(Mth.wrapDegrees(yaw), Mth.clamp(Mth.wrapDegrees(pitch), -90.0F, 90.0F));
   }

   public static RotationMath.Rotation init(RotationMath.Rotation current, RotationMath.Rotation target, float horizontalFactor, float verticalFactor) {
      float yawDiff = init(target.init, current.init);
      float pitchDiff = init(target.load, current.load);
      double length = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
      if (length < 1.0E-9) {
         return target;
      }

      float yawStep = Math.abs((float)(yawDiff / length)) * horizontalFactor;
      float pitchStep = Math.abs((float)(pitchDiff / length)) * verticalFactor;
      return new RotationMath.Rotation(current.init + Mth.clamp(yawDiff, -yawStep, yawStep), current.load + Mth.clamp(pitchDiff, -pitchStep, pitchStep));
   }

   public static RotationMath.Rotation init_ce_DataUtil_ce_DataUtil(RotationMath.Rotation current, RotationMath.Rotation target, float factor) {
      float t = Mth.clamp(factor, 0.0F, 1.0F);
      return new RotationMath.Rotation(
         current.init + init(target.init, current.init) * t, Mth.clamp(current.load + init(target.load, current.load) * t, -90.0F, 90.0F)
      );
   }

   public static RotationMath.Rotation init_ce_DataUtil(RotationMath.Rotation rotation, RotationMath.Rotation current) {
      double gcd = load();
      if (gcd <= 0.0) {
         return rotation;
      }

      float yawDiff = init(rotation.init, current.init);
      float pitchDiff = init(rotation.load, current.load);
      float yaw = current.init + (float)(Math.round(yawDiff / gcd) * gcd);
      float pitch = current.load + (float)(Math.round(pitchDiff / gcd) * gcd);
      return new RotationMath.Rotation(yaw, Mth.clamp(pitch, -90.0F, 90.0F));
   }

   public static RotationMath.Rotation init(RotationMath.Rotation current, RotationMath.Rotation goal, float yawBudget, float pitchBudget, double gcd) {
      if (current == null) {
         return goal;
      }

      if (goal == null) {
         return current;
      }

      double quantum = Double.isFinite(gcd) && gcd > 0.0 ? gcd : 0.15;
      return new RotationMath.Rotation(
         current.init() + init_F_D(init(goal.init(), current.init()), yawBudget, quantum),
         Mth.clamp(current.load() + init_F_D(goal.load() - current.load(), pitchBudget, quantum), -90.0F, 90.0F)
      );
   }

   public static RotationMath.Rotation load(RotationMath.Rotation current, RotationMath.Rotation goal, float yawBudget, float pitchBudget, double gcd) {
      if (current == null) {
         return goal;
      }

      if (goal == null) {
         return current;
      }

      double quantum = Double.isFinite(gcd) && gcd > 0.0 ? gcd : 0.15;
      return new RotationMath.Rotation(
         current.init() + init_F_D(goal.init() - current.init(), yawBudget, quantum),
         Mth.clamp(current.load() + init_F_D(goal.load() - current.load(), pitchBudget, quantum), -90.0F, 90.0F)
      );
   }

   private static float init_F_D(double delta, float budget, double quantum) {
      if (Double.isFinite(delta) && Float.isFinite(budget) && !(budget <= 0.0F)) {
         long maximum = (long)Math.floor(budget / quantum + 1.0E-9);
         long counts = Math.max(-maximum, Math.min(maximum, Math.round(delta / quantum)));
         return (float)(counts * quantum);
      } else {
         return 0.0F;
      }
   }

   public static double init() {
      return load();
   }

   public static double load() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.options != null) {
         double f = mc.options.sensitivity().get() * 0.6 + 0.2;
         float sensitivityFactor = (float)(f * f * f * 8.0);
         return sensitivityFactor * 0.15F;
      } else {
         return 0.0;
      }
   }

   public static float init(float target, float current) {
      return Mth.wrapDegrees(target - current);
   }

   public static float init_F(RotationMath.Rotation current, RotationMath.Rotation target) {
      float yaw = init(target.init, current.init);
      float pitch = init(target.load, current.load);
      return (float)Math.min(180.0, Math.sqrt(yaw * yaw + pitch * pitch));
   }

   public static float load(RotationMath.Rotation a, RotationMath.Rotation b) {
      Vec3 first = Vec3.directionFromRotation(a.load(), a.init());
      Vec3 second = Vec3.directionFromRotation(b.load(), b.init());
      double cosine = Mth.clamp(first.dot(second), -1.0, 1.0);
      return (float)Math.toDegrees(Math.acos(cosine));
   }

   public static void init_V_LocalPlayer(LocalPlayer player, RotationMath.Rotation rotation, boolean syncPrevious) {
      if (player != null && rotation != null) {
         float yaw = Mth.wrapDegrees(rotation.init);
         float pitch = Mth.clamp(rotation.load, -90.0F, 90.0F);
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

   public record Rotation(float init, float load) {
   }
}
