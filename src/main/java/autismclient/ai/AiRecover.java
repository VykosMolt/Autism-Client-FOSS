package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

final class AiRecover {
   private static final int init_I = 12;
   private static final int load = 200;
   private static final double init_D = 1.5;

   private AiRecover() {
   }

   static AiOperation recover(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final AiDeathLog.DeathRecord death = AiDeathLog.lastDeath();
         if (death == null) {
            return AiGameOperations.immediate(AiOperation.Status.FAILED, "No death has been recorded this session; there is nothing to recover", AiDeathLog.statusJson());
         }

         final int timeoutSeconds = Math.max(15, Math.min(1800, AiJson.getInt(args, "timeoutSeconds", 300)));
         final boolean allowDig = AiJson.getBoolean(args, "allowDig", true);
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final Map<String, Integer> startCounts = this.inventoryCounts();
            final AiGuard.GuardPolicy guardPolicy = AiGuard.GuardPolicy.standard();
            AiOperation child;
            AiOperation guardOp;
            String guardReason = "";
            int guardInterrupts;
            int settleTicks;
            boolean atGrave;
            boolean cancelled;
            boolean read;

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.deadline) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Recovery deadline reached; " + this.recoveredText());
                  }

                  String dimension = mc.level.dimension().identifier().toString();
                  if (!dimension.equals(death.dimension())) {
                     return this.finishWith(
                        AiOperation.Status.FAILED,
                        "The grave is in " + death.dimension() + " and the player is in " + dimension + "; travel there first with game:dimension",
                        AiJson.objectOf("needsDimension", death.dimension())
                     );
                  }

                  if (death.expired() && !this.atGrave) {
                     if (AiTerrain.isLoaded(mc.level, death.position())) {
                        return this.finish(
                           AiOperation.Status.FAILED,
                           "The five-minute despawn window closed "
                              + (death.ageMillis() - 300000L) / 1000L
                              + "s ago with the grave chunk loaded; the drops are gone"
                        );
                     }

                     this.read = true;
                  }

                  AiOperation.Result guarded = this.tickGuard();
                  if (guarded != null) {
                     return guarded;
                  }

                  if (this.child != null) {
                     AiOperation.Result result = this.child.tick();
                     if (!result.terminal()) {
                        return this.running(result.message());
                     }

                     this.child.cancel();
                     this.child = null;
                     if (result.status() == AiOperation.Status.CANCELLED) {
                        return this.finish(AiOperation.Status.CANCELLED, "Recovery cancelled");
                     }

                     if (result.status() != AiOperation.Status.SUCCEEDED) {
                        return this.finish(AiOperation.Status.FAILED, "No route back to the grave: " + result.message());
                     }

                     this.atGrave = true;
                     return this.running("Arrived at the grave; sweeping drops");
                  } else if (!this.atGrave) {
                     if (mc.player.blockPosition().distSqr(death.position()) <= 4.0) {
                        this.atGrave = true;
                        return this.running("Already at the grave; sweeping drops");
                     } else {
                        this.child = AiTravelFar.travel(death.position(), 1.5, true, allowDig, true, true, Math.min(240, this.secondsLeft()));
                        return this.running(
                           "Travelling to the grave at "
                              + death.position().toShortString()
                              + (
                                 this.read
                                    ? " (the despawn window has passed but the grave chunk is unloaded; the drops may still be there)"
                                    : " (" + death.remainingMillis() / 1000L + "s before despawn)"
                              )
                        );
                     }
                  } else {
                     ItemEntity nearest = this.nearestDrop();
                     if (nearest != null && mc.player.distanceToSqr(nearest) > 2.25) {
                        this.child = AiGoto.travelToCompat(
                           nearest.blockPosition(), 1.0, false, false, true, Math.min(30, this.secondsLeft())
                        );
                        return this.running("Collecting a straggler drop");
                     }

                     if (nearest != null) {
                        this.settleTicks = 0;
                        return this.running("Standing on the drops");
                     }

                     if (++this.settleTicks < 20) {
                        return this.running("Waiting for pickups to settle");
                     }

                     AiDeathLog.init_V();
                     Map<String, Integer> recovered = this.recoveredCounts();
                     return this.finish(
                        recovered.isEmpty() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.SUCCEEDED,
                        recovered.isEmpty()
                           ? "Reached the grave but recovered nothing; the drops may have despawned or been taken"
                           : "Recovered " + recovered.size() + " item type(s); " + this.recoveredText()
                     );
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Recovery cancelled");
               }
            }

            AiOperation.Result tickGuard() {
               if (this.guardOp != null) {
                  AiOperation.Result result = this.guardOp.tick();
                  if (!result.terminal()) {
                     return this.running("Interrupted: " + this.guardReason + " - " + result.message());
                  }

                  this.guardOp.cancel();
                  this.guardOp = null;
                  return this.running("Resuming recovery after " + this.guardReason);
               } else {
                  AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                  AiOperation action = AiGuard.createResponseOperation(this.guardPolicy, vitals, this.secondsLeft());
                  if (action == null) {
                     return null;
                  }

                  this.guardReason = AiGuard.decisionReason(this.guardPolicy, vitals);
                  this.guardInterrupts++;
                  if (this.child != null) {
                     this.child.cancel();
                     this.child = null;
                  }

                  this.guardOp = action;
                  return this.running("Interrupted: " + this.guardReason);
               }
            }

            ItemEntity nearestDrop() {
               ItemEntity best = null;
               double bestDistance = 144.0;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity drop && drop.isAlive() && !(drop.blockPosition().distSqr(death.position()) > 144.0)) {
                     double distance = mc.player.distanceToSqr(drop);
                     if (distance < bestDistance) {
                        bestDistance = distance;
                        best = drop;
                     }
                  }
               }

               return best;
            }

            Map<String, Integer> inventoryCounts() {
               Map<String, Integer> counts = new LinkedHashMap<>();
               if (mc.player == null) {
                  return counts;
               }

               for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty()) {
                     counts.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
                  }
               }

               return counts;
            }

            Map<String, Integer> recoveredCounts() {
               Map<String, Integer> now = this.inventoryCounts();
               Map<String, Integer> gained = new LinkedHashMap<>();
               now.forEach((item, count) -> {
                  int delta = count - this.startCounts.getOrDefault(item, 0);
                  if (delta > 0) {
                     gained.put(item, delta);
                  }
               });
               return gained;
            }

            String recoveredText() {
               Map<String, Integer> gained = this.recoveredCounts();
               int missing = 0;

               for (Entry<String, Integer> entry : death.inventory().entrySet()) {
                  missing += Math.max(0, entry.getValue() - gained.getOrDefault(entry.getKey(), 0));
               }

               return gained.size() + " type(s) back, about " + missing + " item(s) still missing";
            }

            int secondsLeft() {
               return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
            }

            JsonObject progressJson() {
               JsonObject out = AiJson.objectOf("grave", AiDeathLog.statusJson(), "arrived", this.atGrave);
               if (this.read) {
                  out.addProperty(
                     "warning",
                     "The five-minute despawn window has passed on the wall clock, but the grave chunk is not loaded; item despawn timers only run in loaded chunks, so the drops may still be there"
                  );
               }

               JsonArray gained = new JsonArray();
               this.recoveredCounts().forEach((item, count) -> gained.add(AiJson.objectOf("item", item, "count", count)));
               out.add("recovered", gained);
               JsonArray stillMissing = new JsonArray();
               Map<String, Integer> have = this.recoveredCounts();
               death.inventory().forEach((item, count) -> {
                  int shortfall = count - have.getOrDefault(item, 0);
                  if (shortfall > 0) {
                     stillMissing.add(AiJson.objectOf("item", item, "count", shortfall));
                  }
               });
               out.add("stillMissing", stillMissing);
               out.add("survival", AiGuard.statusToJson(AiGuard.sampleStatus(), this.guardInterrupts, this.guardReason));
               return out;
            }

            AiOperation.Result running(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressJson().toString(), List.of());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               return this.finishWith(status, message, null);
            }

            AiOperation.Result finishWith(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.progressJson();
               if (extra != null) {
                  for (Entry<String, JsonElement> entry : extra.entrySet()) {
                     data.add(entry.getKey(), entry.getValue());
                  }
               }

               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of());
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  if (this.guardOp != null) {
                     this.guardOp.cancel();
                     this.guardOp = null;
                  }

                  if (this.child != null) {
                     this.child.cancel();
                     this.child = null;
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Recovery requires a connected living player");
      }
   }

   static AiOperation create() {
      AiDeathLog.DeathRecord death = AiDeathLog.lastDeath();
      return AiGameOperations.immediate(
         AiOperation.Status.SUCCEEDED,
         death == null
            ? "No death recorded this session"
            : "Death recorded "
               + death.ageMillis() / 1000L
               + "s ago at "
               + death.position().toShortString()
               + "; "
               + death.remainingMillis() / 1000L
               + "s before the drops despawn",
         AiDeathLog.statusJson()
      );
   }

   static BlockPos lastDeathPos() {
      AiDeathLog.DeathRecord death = AiDeathLog.lastDeath();
      return death == null ? null : death.position();
   }
}
