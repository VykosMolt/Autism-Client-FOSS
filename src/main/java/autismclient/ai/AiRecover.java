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

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final AiDeathLog.Inner_DataUtil death = AiDeathLog.init_j_DataUtil();
         if (death == null) {
            return AiGameOperations.init_AiOperation(AiOperation.Status.FAILED, "No death has been recorded this session; there is nothing to recover", AiDeathLog.init_JsonObject());
         }

         final int timeoutSeconds = Math.max(15, Math.min(1800, AiJson.init_I_JsonObject(args, "timeoutSeconds", 300)));
         final boolean allowDig = AiJson.init_Z_JsonObject(args, "allowDig", true);
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final Map<String, Integer> init_Map = this.init_Map_3();
            final AiGuard.Inner_CachePool init_X_CachePool = AiGuard.Inner_CachePool.standard();
            AiOperation init_AiOperation;
            AiOperation load_AiOperation;
            String init_Str = "";
            int init_I;
            int load_I;
            boolean init_Z;
            boolean load_Z;
            boolean read;

            @Override
            public AiOperation.Result tick() {
               if (!this.load_Z && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Recovery deadline reached; " + this.init_Str_15());
                  }

                  String dimension = mc.level.dimension().identifier().toString();
                  if (!dimension.equals(death.dimension())) {
                     return this.init(
                        AiOperation.Status.FAILED,
                        "The grave is in " + death.dimension() + " and the player is in " + dimension + "; travel there first with game:dimension",
                        AiJson.init_JsonObject_ObjArr("needsDimension", death.dimension())
                     );
                  }

                  if (death.expired() && !this.init_Z) {
                     if (AiTerrain.init(mc.level, death.position())) {
                        return this.init(
                           AiOperation.Status.FAILED,
                           "The five-minute despawn window closed "
                              + (death.ageMillis() - 300000L) / 1000L
                              + "s ago with the grave chunk loaded; the drops are gone"
                        );
                     }

                     this.read = true;
                  }

                  AiOperation.Result guarded = this.init_AiOperation_Result_23();
                  if (guarded != null) {
                     return guarded;
                  }

                  if (this.init_AiOperation != null) {
                     AiOperation.Result result = this.init_AiOperation.tick();
                     if (!result.terminal()) {
                        return this.init(result.message());
                     }

                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     if (result.status() == AiOperation.Status.CANCELLED) {
                        return this.init(AiOperation.Status.CANCELLED, "Recovery cancelled");
                     }

                     if (result.status() != AiOperation.Status.SUCCEEDED) {
                        return this.init(AiOperation.Status.FAILED, "No route back to the grave: " + result.message());
                     }

                     this.init_Z = true;
                     return this.init("Arrived at the grave; sweeping drops");
                  } else if (!this.init_Z) {
                     if (mc.player.blockPosition().distSqr(death.position()) <= 4.0) {
                        this.init_Z = true;
                        return this.init("Already at the grave; sweeping drops");
                     } else {
                        this.init_AiOperation = AiTravelFar.init(death.position(), 1.5, true, allowDig, true, true, Math.min(240, this.init_I_24()));
                        return this.init(
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
                     ItemEntity nearest = this.init_ItemEntity();
                     if (nearest != null && mc.player.distanceToSqr(nearest) > 2.25) {
                        this.init_AiOperation = AiGoto.init_AiOperation_BlockPos(
                           nearest.blockPosition(), 1.0, false, false, true, Math.min(30, this.init_I_24())
                        );
                        return this.init("Collecting a straggler drop");
                     }

                     if (nearest != null) {
                        this.load_I = 0;
                        return this.init("Standing on the drops");
                     }

                     if (++this.load_I < 20) {
                        return this.init("Waiting for pickups to settle");
                     }

                     AiDeathLog.init_V();
                     Map<String, Integer> recovered = this.load();
                     return this.init(
                        recovered.isEmpty() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.SUCCEEDED,
                        recovered.isEmpty()
                           ? "Reached the grave but recovered nothing; the drops may have despawned or been taken"
                           : "Recovered " + recovered.size() + " item type(s); " + this.init_Str_15()
                     );
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Recovery cancelled");
               }
            }

            AiOperation.Result init_AiOperation_Result_23() {
               if (this.load_AiOperation != null) {
                  AiOperation.Result result = this.load_AiOperation.tick();
                  if (!result.terminal()) {
                     return this.init("Interrupted: " + this.init_Str + " - " + result.message());
                  }

                  this.load_AiOperation.cancel();
                  this.load_AiOperation = null;
                  return this.init("Resuming recovery after " + this.init_Str);
               } else {
                  AiGuard.Inner_BufferHelper vitals = AiGuard.init_X_BufferHelper();
                  AiOperation action = AiGuard.init_AiOperation_X_CachePool(this.init_X_CachePool, vitals, this.init_I_24());
                  if (action == null) {
                     return null;
                  }

                  this.init_Str = AiGuard.init_Str(this.init_X_CachePool, vitals);
                  this.init_I++;
                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  this.load_AiOperation = action;
                  return this.init("Interrupted: " + this.init_Str);
               }
            }

            ItemEntity init_ItemEntity() {
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

            Map<String, Integer> init_Map_3() {
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

            Map<String, Integer> load() {
               Map<String, Integer> now = this.init_Map_3();
               Map<String, Integer> gained = new LinkedHashMap<>();
               now.forEach((item, count) -> {
                  int delta = count - this.init_Map.getOrDefault(item, 0);
                  if (delta > 0) {
                     gained.put(item, delta);
                  }
               });
               return gained;
            }

            String init_Str_15() {
               Map<String, Integer> gained = this.load();
               int missing = 0;

               for (Entry<String, Integer> entry : death.inventory().entrySet()) {
                  missing += Math.max(0, entry.getValue() - gained.getOrDefault(entry.getKey(), 0));
               }

               return gained.size() + " type(s) back, about " + missing + " item(s) still missing";
            }

            int init_I_24() {
               return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
            }

            JsonObject init_JsonObject_39() {
               JsonObject out = AiJson.init_JsonObject_ObjArr("grave", AiDeathLog.init_JsonObject(), "arrived", this.init_Z);
               if (this.read) {
                  out.addProperty(
                     "warning",
                     "The five-minute despawn window has passed on the wall clock, but the grave chunk is not loaded; item despawn timers only run in loaded chunks, so the drops may still be there"
                  );
               }

               JsonArray gained = new JsonArray();
               this.load().forEach((item, count) -> gained.add(AiJson.init_JsonObject_ObjArr("item", item, "count", count)));
               out.add("recovered", gained);
               JsonArray stillMissing = new JsonArray();
               Map<String, Integer> have = this.load();
               death.inventory().forEach((item, count) -> {
                  int shortfall = count - have.getOrDefault(item, 0);
                  if (shortfall > 0) {
                     stillMissing.add(AiJson.init_JsonObject_ObjArr("item", item, "count", shortfall));
                  }
               });
               out.add("stillMissing", stillMissing);
               out.add("survival", AiGuard.init_JsonObject_X_BufferHelper(AiGuard.init_X_BufferHelper(), this.init_I, this.init_Str));
               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_39().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.init_JsonObject_39();
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
               if (!this.load_Z) {
                  this.load_Z = true;
                  if (this.load_AiOperation != null) {
                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                  }

                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Recovery requires a connected living player");
      }
   }

   static AiOperation init_AiOperation() {
      AiDeathLog.Inner_DataUtil death = AiDeathLog.init_j_DataUtil();
      return AiGameOperations.init_AiOperation(
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
         AiDeathLog.init_JsonObject()
      );
   }

   static BlockPos init_BlockPos() {
      AiDeathLog.Inner_DataUtil death = AiDeathLog.init_j_DataUtil();
      return death == null ? null : death.position();
   }
}
