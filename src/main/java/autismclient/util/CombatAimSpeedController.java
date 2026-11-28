package autismclient.util;

import autismclient.api.module.DoubleSetting;
import net.minecraft.client.player.LocalPlayer;

public final class CombatAimSpeedController {
   public static final String init_Str = "rotation-speed-multiplier";
   private static final int init_I = 2;
   private final CombatAimEngine.Inner_CachePool aimState = new CombatAimEngine.Inner_CachePool();
   private Object trackedEnemy;
   private int boostUntilTick;

   public static DoubleSetting speedSetting() {
      return new DoubleSetting("rotation-speed-multiplier", "Rotation Speed", 1.0, 0.5, 2.5, 0.1)
         .group("Aim")
         .description("Relative to module default")
         .unit("×");
   }

   public static double clampMultiplier(double multiplier) {
      return Math.clamp(Double.isFinite(multiplier) ? multiplier : 1.0, 0.5, 2.5);
   }

   public static float turnCap(double scale) {
      return (float)Math.min(180.0, 72.0 * scale);
   }

   public void reset() {
      this.trackedEnemy = null;
      this.aimState.reset();
   }

   void boost(Object enemy, int tick, int ticks) {
      if (enemy != null) {
         if (this.trackedEnemy != enemy) {
            this.trackedEnemy = enemy;
            this.boostUntilTick = tick;
            this.aimState.reset();
         }

         if (this.boostUntilTick - tick < ticks) {
            this.boostUntilTick = tick + ticks;
         }
      }
   }

   boolean isBoosted(Object enemy, int tick) {
      return enemy != null && this.trackedEnemy == enemy && this.boostUntilTick - tick >= 0;
   }

   public void init(Object from, Object to) {
      if (from != null && this.trackedEnemy == from) {
         this.trackedEnemy = to;
      }
   }

   public void load(Object enemy, int tick, int latencyMs) {
      this.boost(enemy, tick, LatencyTickWindow.windowTicks(latencyMs) + 2);
   }

   public void init(String owner, int priority, LocalPlayer player, Object enemy, double speed, RotationMath.Rotation goal, boolean pin) {
      if (!owner.equals(SilentRotationManager.getActiveOwnerId()) || SilentRotationManager.isReturningToPlayer()) {
         this.reset();
      }

      SilentRotationManager.requestRotation(owner, priority, goal);
      if (owner.equals(SilentRotationManager.getActiveOwnerId())) {
         this.boost(enemy, AutismSessionState.getInstance().getClientTick(), 2);
         if (pin) {
            this.aimState.reset();
            SilentRotationManager.tickPinQuiet(owner, player, true);
         } else {
            double scale = 2.0 * clampMultiplier(speed);
            SilentRotationManager.setCustomHandStepper(owner, (at, yaw, pitch) -> this.aimState.stepToward(at, goal, yaw, pitch, scale));
            float cap = turnCap(scale);
            SilentRotationManager.tick(owner, player, cap, cap);
         }
      }
   }

   public boolean init(String owner, int priority, LocalPlayer player, Object enemy, RotationMath.Rotation wire) {
      if (wire != null && this.isBoosted(enemy, AutismSessionState.getInstance().getClientTick()) && owner.equals(SilentRotationManager.getActiveOwnerId()) && !SilentRotationManager.isReturningToPlayer()) {
         RotationMath.Rotation current = SilentRotationManager.getCurrentRotation();
         if (current == null || !SilentRotationManager.isVisibleAimActive() || player.getYRot() == current.yaw() && player.getXRot() == current.pitch()) {
            SilentRotationManager.requestRotation(owner, priority, current == null ? wire : current);
            SilentRotationManager.tickPinQuiet(owner, player, true);
            this.aimState.reset();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
