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

   static String init(Minecraft mc) {
      if (mc.player == null || mc.level == null) {
         return null;
      } else if (AiLavaEscape.init_Z(mc)) {
         return "in lava";
      } else if (AiBreathe.init(mc.player)) {
         return "out of air";
      } else if (AiPowderEscape.init_Z(mc)) {
         return "in powder snow";
      } else if (!mc.player.isInWater() && AiEndRun.init_Z(mc)) {
         return "standing in fire";
      } else {
         return mc.player.isOnFire()
               && !mc.player.hasEffect(MobEffects.FIRE_RESISTANCE)
               && !mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)
               && AiGameState.init_I_Str("minecraft:water_bucket") > 0
            ? "on fire"
            : null;
      }
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      final int seconds = Math.max(1, Math.min(30, AiJson.init_I_JsonObject(args, "timeoutSeconds", 25)));
      final Object level = mc.level;
      final Object player = mc.player;
      final Object connection = mc.getConnection();
      return new AiOperation() {
         final AiGuardLayer init_Y = new AiGuardLayer(new AiGuard.GuardPolicy(false, false, false, 16.0F, 7.0F, 14, false, Set.of()), "hazard escape");
         final long init_J = System.nanoTime() + seconds * 1000000000L;
         boolean init_Z;
         String init_Str = "";

         @Override
         public AiOperation.Result tick() {
            if (this.init_Z || mc.level != level || mc.player != player || mc.getConnection() != connection || mc.player == null || !mc.player.isAlive()) {
               return this.init(AiOperation.Status.CANCELLED, "Hazard escape context changed");
            }

            if (System.nanoTime() >= this.init_J) {
               return this.init(AiOperation.Status.TIMED_OUT, "Hazard escape deadline reached: " + this.init_Str);
            }

            String next = AiHazardEscape.init(mc);
            if (next == null && !this.init_Y.init_Z()) {
               return this.init(AiOperation.Status.SUCCEEDED, "Immediate environmental danger cleared");
            }

            if (next != null) {
               this.init_Str = next;
            }

            AiOperation.Result result = this.init_Y
               .init_AiOperation_Result_AiOperation(null, () -> {}, () -> Math.max(1, (int)((this.init_J - System.nanoTime()) / 1000000000L)));
            if (result != null) {
               if (result.terminal()) {
                  this.init_Y.init_V();
               }

               return result;
            } else {
               return this.init(AiOperation.Status.FAILED, "No safe environmental escape available: " + this.init_Str);
            }
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            boolean pending = this.init_Y.read();
            this.init_Y.init_V();
            return AiGuardLayer.init_AiOperation_Result_Z_AiOperation_Result(
               pending, AiGameOperations.init_AiOperation_Result(status, message, AiJson.init_JsonObject_ObjArr("hazard", this.init_Str, "reobserveBeforeResuming", true))
            );
         }

         @Override
         public boolean canInterruptNow() {
            return this.init_Y.load();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_Y.read();
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            this.init_Y.init_V();
         }
      };
   }
}
