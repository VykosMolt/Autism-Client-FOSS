package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;

final class AiHazardEscape {
   private AiHazardEscape() {
   }

   static String currentHazard(Minecraft mc) {
      if (mc.player == null || mc.level == null) {
         return null;
      } else if (AiLavaEscape.isInLava(mc)) {
         return "in lava";
      } else if (AiBreathe.needsAir(mc.player)) {
         return "out of air";
      } else if (AiPowderEscape.isInPowderSnow(mc)) {
         return "in powder snow";
      } else if (!mc.player.isInWater() && AiEndRun.init_Z(mc)) {
         return "standing in fire";
      } else {
         return mc.player.isOnFire()
               && !mc.player.hasEffect(MobEffects.FIRE_RESISTANCE)
               && !mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)
               && AiGameState.countInventoryItem("minecraft:water_bucket") > 0
            ? "on fire"
            : null;
      }
   }

   static AiOperation create(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      final int seconds = Math.max(1, Math.min(30, AiJson.getInt(args, "timeoutSeconds", 25)));
      final Object level = mc.level;
      final Object player = mc.player;
      final Object connection = mc.getConnection();
      return new AiOperation() {
         final AiGuardLayer guard = new AiGuardLayer(new AiGuard.GuardPolicy(false, false, false, 16.0F, 7.0F, 14, false, Set.of()), "hazard escape");
         final long deadlineNanos = System.nanoTime() + seconds * 1000000000L;
         boolean cancelled;
         String hazard = "";

         @Override
         public AiOperation.Result tick() {
            if (this.cancelled || mc.level != level || mc.player != player || mc.getConnection() != connection || mc.player == null || !mc.player.isAlive()) {
               return this.finish(AiOperation.Status.CANCELLED, "Hazard escape context changed");
            }

            if (System.nanoTime() >= this.deadlineNanos) {
               return this.finish(AiOperation.Status.TIMED_OUT, "Hazard escape deadline reached: " + this.hazard);
            }

            String next = AiHazardEscape.currentHazard(mc);
            if (next == null && !this.guard.isActive()) {
               return this.finish(AiOperation.Status.SUCCEEDED, "Immediate environmental danger cleared");
            }

            if (next != null) {
               this.hazard = next;
            }

            AiOperation.Result result = this.guard
               .tick(null, () -> {}, () -> Math.max(1, (int)((this.deadlineNanos - System.nanoTime()) / 1000000000L)));
            if (result != null) {
               if (result.terminal()) {
                  this.guard.cancel();
               }

               return result;
            } else {
               return this.finish(AiOperation.Status.FAILED, "No safe environmental escape available: " + this.hazard);
            }
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            boolean pending = this.guard.requiresReconciliation();
            this.guard.cancel();
            return AiGuardLayer.wrapPendingTermination(
               pending, AiGameOperations.result(status, message, AiJson.objectOf("hazard", this.hazard, "reobserveBeforeResuming", true))
            );
         }

         @Override
         public boolean canInterruptNow() {
            return this.guard.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.guard.requiresReconciliation();
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            this.guard.cancel();
         }
      };
   }
}
