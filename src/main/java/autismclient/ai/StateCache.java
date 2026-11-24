package autismclient.ai;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class StateCache {
   private StateCache() {
   }

   static Float init(Minecraft mc, Creeper primary) {
      if (mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable() && !mc.player.isUsingItem() && !mc.player.isShiftKeyDown()) {
         Vec3 start = mc.player.position();
         Vec3 away = start.subtract(primary.position());
         float direct = (float)Math.toDegrees(Math.atan2(-away.x, away.z));
         ArrayList<Creeper> blasts = new ArrayList<>();

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Creeper creeper && creeper.isAlive() && creeper.getSwellDir() > 0 && creeper.distanceToSqr(mc.player) <= 144.0) {
               blasts.add(creeper);
            }
         }

         Float best = null;
         double bestScore = -1.0 / 0.0;

         for (int offset : new int[]{0, 1, -1, 2, -2, 3, -3, 4, -4}) {
            float yaw = direct + offset * 22.5F;
            List<AABB> trace = autismclient.ai.a.SyncManager.init(mc, yaw, 1.0, 0.0, 12, 4, mc.player.getFoodData().getFoodLevel() > 6);
            if (trace != null && !trace.isEmpty()) {
               Vec3 end = trace.getLast().getBottomCenter();
               double initial = start.distanceTo(primary.position());
               double finalDistance = end.distanceTo(primary.position());
               if (!(finalDistance < initial + 0.35)) {
                  double clearance = finalDistance;

                  for (Creeper creeper : blasts) {
                     for (AABB box : trace) {
                        double d = box.getBottomCenter().distanceTo(creeper.position());
                        if (d < 5.0 && d + 0.2 < start.distanceTo(creeper.position())) {
                           clearance = -100.0;
                           break;
                        }
                     }

                     clearance = Math.min(clearance, end.distanceTo(creeper.position()));
                  }

                  if (!(clearance < 0.0)) {
                     double score = clearance * 2.0 + finalDistance - Math.abs(CacheEntry.init(yaw - mc.player.getYRot())) * 0.002;
                     if (score > bestScore) {
                        bestScore = score;
                        best = yaw;
                     }
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   static void init(Minecraft mc, float yaw) {
      if (mc.player.isUsingItem()) {
         mc.gameMode.releaseUsingItem(mc.player);
      }

      mc.options.keyUse.setDown(false);
      mc.player.setYRot(yaw);
      mc.options.keyUp.setDown(true);
      mc.options.keyDown.setDown(false);
      mc.options.keyLeft.setDown(false);
      mc.options.keyRight.setDown(false);
      mc.options.keyJump.setDown(false);
      mc.options.keyShift.setDown(false);
      mc.options.keySprint.setDown(mc.player.getFoodData().getFoodLevel() > 6);
   }
}
