package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

final class AiRunDiagnostics {
   private static AiRunDiagnostics init_bJ;
   private final BiConsumer<String, JsonObject> init_BiConsumer;
   private final LongSupplier init_LongSupplier;
   private final Map<Long, JsonObject> init_Map = new LinkedHashMap<>();
   private final ArrayDeque<JsonObject> init_ArrayDeque = new ArrayDeque<>();
   private long init_J;
   private long load;
   private long read;
   private long write;
   private float init_F = -1.0F;
   private boolean init_Z;

   AiRunDiagnostics(BiConsumer<String, JsonObject> sink, LongSupplier clock) {
      this.init_BiConsumer = sink;
      this.init_LongSupplier = clock;
   }

   static AiRunDiagnostics init_bJ() {
      if (init_bJ != null) {
         init_bJ.init_Z = true;
      }

      init_bJ = new AiRunDiagnostics(AiRunTrace::emitServiceEvent, System::currentTimeMillis);
      AiRunTrace trace = AiRunTrace.current();
      if (trace != null) {
         trace.expandLimitForLongRun();
      }

      return init_bJ;
   }

   static AiOperation init_AiOperation_Str(String controller, String decision, AiOperation op) {
      return init_bJ != null && !init_bJ.init_Z && op != null ? init_bJ.load(controller, decision, op) : op;
   }

   AiOperation load(final String controller, final String decision, final AiOperation delegate) {
      return new AiOperation() {
         long init_J;
         long load_J;
         long read;
         boolean init_Z;
         boolean load_Z;

         void init() {
            if (this.init_J == 0L && !AiRunDiagnostics.this.init_Z) {
               this.init_J = ++AiRunDiagnostics.this.init_J;
               this.load_J = AiRunDiagnostics.this.init_LongSupplier.getAsLong();
               JsonObject entry = AiJson.objectOf(
                  "operationId",
                  this.init_J,
                  "parentOperationId",
                  AiRunDiagnostics.this.load,
                  "controller",
                  controller,
                  "decision",
                  AiRunDiagnostics.init(decision),
                  "startedAtMs",
                  this.load_J
               );
               AiRunDiagnostics.this.init_Map.put(this.init_J, entry);
               AiRunDiagnostics.this.init("speedrun_operation_start", entry);
            }
         }

         @Override
         public AiOperation.Result tick() {
            try {
               this.init();
            } catch (RuntimeException failure) {
               AiRunDiagnostics.this.init("speedrun_diagnostic_error", AiJson.objectOf("message", AiRunDiagnostics.init(failure.toString())));
            }

            long previous = AiRunDiagnostics.this.load;
            AiRunDiagnostics.this.load = this.init_J;

            try {
               AiOperation.Result result = delegate.tick();

               try {
                  if (this.init_J != 0L && !AiRunDiagnostics.this.init_Z && !this.init_Z) {
                     JsonObject entry = AiRunDiagnostics.this.init_Map.get(this.init_J);
                     if (entry != null) {
                        entry.addProperty("message", AiRunDiagnostics.init(result.message()));
                     }

                     long now = AiRunDiagnostics.this.init_LongSupplier.getAsLong();
                     if (result.terminal() || now >= this.read) {
                        JsonObject event = AiJson.objectOf(
                           "operationId",
                           this.init_J,
                           "controller",
                           controller,
                           "decision",
                           AiRunDiagnostics.init(decision),
                           "status",
                           result.status().name(),
                           "message",
                           AiRunDiagnostics.init(result.message()),
                           "durationMs",
                           Math.max(0L, now - this.load_J),
                           "failureCode",
                           result.failure().name(),
                           "result",
                           AiRunResources.compactEvidenceJson(result.dataJson())
                        );
                        if (result.terminal() && result.status() != AiOperation.Status.SUCCEEDED) {
                           Minecraft client = Minecraft.getInstance();
                           if (client != null && client.player != null && client.level != null) {
                              event.add("body", AiRunDiagnostics.this.load(client));
                              event.add("localTerrain", AiRunDiagnostics.init(client));
                           }
                        }

                        if (entry != null) {
                           entry.add("result", event.get("result").deepCopy());
                        }

                        AiRunDiagnostics.this.init(result.terminal() ? "speedrun_operation_end" : "speedrun_operation_progress", event);
                        this.read = now + 5000L;
                     }

                     if (result.terminal()) {
                        this.init_Z = true;
                        AiRunDiagnostics.this.init_Map.remove(this.init_J);
                     }
                  }
               } catch (RuntimeException failure) {
                  if (result.terminal()) {
                     this.init_Z = true;
                     AiRunDiagnostics.this.init_Map.remove(this.init_J);
                  }

                  AiRunDiagnostics.this.init("speedrun_diagnostic_error", AiJson.objectOf("operationId", this.init_J, "message", AiRunDiagnostics.init(failure.toString())));
               }

               return result;
            } catch (RuntimeException | Error failure) {
               JsonObject event = AiJson.objectOf(
                  "operationId",
                  this.init_J,
                  "controller",
                  controller,
                  "decision",
                  AiRunDiagnostics.init(decision),
                  "exception",
                  failure.getClass().getName(),
                  "message",
                  AiRunDiagnostics.init(String.valueOf(failure.getMessage()))
               );
               StringWriter stack = new StringWriter();
               failure.printStackTrace(new PrintWriter(stack));
               event.addProperty("stack", AiRunDiagnostics.init(stack.toString()));
               AiRunDiagnostics.this.init("speedrun_operation_exception", event);
               throw failure;
            } finally {
               AiRunDiagnostics.this.load = previous;
            }
         }

         @Override
         public boolean canInterruptNow() {
            return delegate.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return delegate.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            if (!this.load_Z) {
               try {
                  delegate.cancel();
                  this.load_Z = true;
               } finally {
                  if (this.load_Z) {
                     try {
                        if (this.init_J != 0L && !this.init_Z) {
                           AiRunDiagnostics.this.init(
                              "speedrun_operation_cancel",
                              AiJson.objectOf(
                                 "operationId",
                                 this.init_J,
                                 "controller",
                                 controller,
                                 "decision",
                                 AiRunDiagnostics.init(decision),
                                 "durationMs",
                                 Math.max(0L, AiRunDiagnostics.this.init_LongSupplier.getAsLong() - this.load_J)
                              )
                           );
                        }
                     } catch (RuntimeException var7) {
                     }

                     AiRunDiagnostics.this.init_Map.remove(this.init_J);
                  }
               }
            }
         }
      };
   }

   private void init(String kind, JsonObject data) {
      if (!this.init_Z) {
         try {
            this.init_BiConsumer.accept(kind, data.deepCopy());
         } catch (RuntimeException var4) {
         }
      }
   }

   void init(String phase, String message) {
      if (!this.init_Z) {
         try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
               return;
            }

            long now = this.init_LongSupplier.getAsLong();
            if (now >= this.write) {
               this.write = now + 500L;
               this.init_ArrayDeque.addLast(this.load(mc));

               while (this.init_ArrayDeque.size() > 120) {
                  this.init_ArrayDeque.removeFirst();
               }
            }

            boolean hurt = this.init_F >= 0.0F && mc.player.getHealth() < this.init_F - 0.01F;
            this.init_F = mc.player.getHealth();
            if (now < this.read && !hurt) {
               return;
            }

            this.read = now + 5000L;
            JsonObject snapshot = this.init_JsonObject_Str_2(phase, message, false);
            if (hurt) {
               DamageSource damage = mc.player.getLastDamageSource();
               snapshot.addProperty("damageSource", damage == null ? "unavailable" : damage.getMsgId());
               snapshot.add("recentMotion", this.init_JsonArray());
            }

            this.init(hurt ? "speedrun_damage_snapshot" : "speedrun_snapshot", snapshot);
            AiService service = AiService.getExistingInstance();
            if (service != null) {
               service.saveSpeedrunDiagnostics(snapshot);
            }
         } catch (RuntimeException failure) {
            this.init("speedrun_diagnostic_error", AiJson.objectOf("message", init(failure.toString())));
         }
      }
   }

   JsonObject init_JsonObject_Str(String status, String phase, String message) {
      JsonObject snapshot;
      try {
         snapshot = this.init_JsonObject_Str_2(phase, message, true);
      } catch (RuntimeException failure) {
         snapshot = AiJson.objectOf("diagnosticError", init(failure.toString()));
      }

      snapshot.addProperty("status", status);
      this.init("speedrun_final_snapshot", snapshot);
      AiService service = AiService.getExistingInstance();
      if (service != null) {
         service.saveSpeedrunDiagnostics(snapshot);
      }

      this.init_Z = true;
      if (init_bJ == this) {
         init_bJ = null;
      }

      return snapshot;
   }

   private JsonObject init_JsonObject_Str_2(String phase, String message, boolean finalSnapshot) {
      Minecraft mc = Minecraft.getInstance();
      JsonArray operations = new JsonArray();
      JsonArray nearby = new JsonArray();
      JsonArray slots = new JsonArray();
      JsonArray navigation = new JsonArray();
      this.init_Map.values().forEach(entry -> operations.add(entry.deepCopy()));
      List<String> nav = autismclient.ai.engine.AiNavDebug.readSince(Math.max(0L, autismclient.ai.engine.AiNavDebug.cursor() - 24L));
      nav.forEach(navigation::add);
      JsonObject out = AiJson.objectOf(
         "observedAtMs",
         this.init_LongSupplier.getAsLong(),
         "phase",
         phase,
         "message",
         init(message),
         "operations",
         operations,
         "navigation",
         navigation,
         "resources",
         AiRunResources.inventorySnapshotJson()
      );
      if (mc.player != null && mc.level != null) {
         out.add("body", this.load(mc));
         BlockPos feet = mc.player.blockPosition();
         out.add(
            "terrain",
            AiJson.objectOf(
               "feet",
               mc.level.getBlockState(feet).toString(),
               "head",
               mc.level.getBlockState(feet.above()).toString(),
               "floor",
               mc.level.getBlockState(feet.below()).toString(),
               "loaded",
               mc.level.hasChunkAt(feet),
               "feetRevision",
               AiServerEvidence.blockRevision(feet),
               "floorRevision",
               AiServerEvidence.blockRevision(feet.below())
            )
         );
         out.add("localTerrain", init(mc));

         for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
               slots.add(
                  AiJson.objectOf(
                     "slot",
                     slot,
                     "item",
                     BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
                     "count",
                     stack.getCount(),
                     "damage",
                     stack.getDamageValue(),
                     "maxDamage",
                     stack.getMaxDamage()
                  )
               );
            }
         }

         out.add("slots", slots);
         out.addProperty("containerRevision", AiServerEvidence.containerRevision(mc.player.containerMenu.containerId));
         ArrayList<Entity> candidates = new ArrayList<>();

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity != mc.player && entity.isAlive() && !(entity.distanceToSqr(mc.player) > 1024.0)) {
               candidates.add(entity);
            }
         }

         candidates.sort(Comparator.comparingDouble(entityx -> entityx.distanceToSqr(mc.player)));

         for (Entity entity : candidates.subList(0, Math.min(16, candidates.size()))) {
            JsonObject info = AiJson.objectOf(
               "id",
               entity.getId(),
               "type",
               BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(),
               "x",
               entity.getX(),
               "y",
               entity.getY(),
               "z",
               entity.getZ(),
               "distance",
               entity.distanceTo(mc.player),
               "health",
               entity instanceof LivingEntity living ? living.getHealth() : -1
            );
            if (entity instanceof ItemEntity drop) {
               info.addProperty("item", BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString());
               info.addProperty("count", drop.getItem().getCount());
            }

            nearby.add(info);
         }

         out.add("nearby", nearby);
      }

      if (finalSnapshot) {
         out.add("recentMotion", this.init_JsonArray());
      }

      return out;
   }

   private static JsonObject init(Minecraft mc) {
      BlockPos origin = mc.player.blockPosition();
      JsonArray cells = new JsonArray();

      for (BlockPos cell : BlockPos.betweenClosed(origin.offset(-2, -1, -2), origin.offset(2, 3, 2))) {
         String state = mc.level.hasChunkAt(cell) ? mc.level.getBlockState(cell).toString() : "unloaded";
         if (!mc.level.hasChunkAt(cell) || !mc.level.getBlockState(cell).isAir()) {
            cells.add(
               AiJson.objectOf(
                  "dx", cell.getX() - origin.getX(), "dy", cell.getY() - origin.getY(), "dz", cell.getZ() - origin.getZ(), "state", state
               )
            );
         }
      }

      return AiJson.objectOf("origin", origin.toShortString(), "horizontalRadius", 2, "minDy", -1, "maxDy", 3, "omittedCells", "air", "cells", cells);
   }

   private JsonArray init_JsonArray() {
      JsonArray out = new JsonArray();
      this.init_ArrayDeque.forEach(entry -> out.add(entry.deepCopy()));
      return out;
   }

   private JsonObject load(Minecraft mc) {
      LocalPlayer p = mc.player;
      Options keys = mc.options;
      Vec3 velocity = p.getDeltaMovement();
      return AiJson.objectOf(
         "atMs",
         this.init_LongSupplier.getAsLong(),
         "tick",
         p.tickCount,
         "dimension",
         AiWorldIdentity.currentDimensionId(mc),
         "x",
         p.getX(),
         "y",
         p.getY(),
         "z",
         p.getZ(),
         "yaw",
         p.getYRot(),
         "pitch",
         p.getXRot(),
         "health",
         p.getHealth(),
         "food",
         p.getFoodData().getFoodLevel(),
         "saturation",
         p.getFoodData().getSaturationLevel(),
         "alive",
         p.isAlive(),
         "onGround",
         p.onGround(),
         "fallDistance",
         p.fallDistance,
         "onFire",
         p.isOnFire(),
         "air",
         p.getAirSupply(),
         "vx",
         velocity.x,
         "vy",
         velocity.y,
         "vz",
         velocity.z,
         "selectedSlot",
         p.getInventory().getSelectedSlot(),
         "mainHand",
         BuiltInRegistries.ITEM.getKey(p.getMainHandItem().getItem()).toString(),
         "keys",
         AiJson.objectOf(
            "forward",
            keys.keyUp.isDown(),
            "back",
            keys.keyDown.isDown(),
            "left",
            keys.keyLeft.isDown(),
            "right",
            keys.keyRight.isDown(),
            "jump",
            keys.keyJump.isDown(),
            "sneak",
            keys.keyShift.isDown(),
            "sprint",
            keys.keySprint.isDown(),
            "attack",
            keys.keyAttack.isDown(),
            "use",
            keys.keyUse.isDown()
         )
      );
   }

   static String init(String value) {
      return value.length() <= 12000 ? value : value.substring(0, 12000);
   }
}
