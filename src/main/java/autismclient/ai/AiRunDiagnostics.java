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
   private static AiRunDiagnostics active;
   private final BiConsumer<String, JsonObject> sink;
   private final LongSupplier clock;
   private final Map<Long, JsonObject> openOperations = new LinkedHashMap<>();
   private final ArrayDeque<JsonObject> recentMotion = new ArrayDeque<>();
   private long nextOperationId;
   private long currentOperationId;
   private long throttleUntil;
   private long nextMotionSampleMs;
   private float lastHealth = -1.0F;
   private boolean stopped;

   AiRunDiagnostics(BiConsumer<String, JsonObject> sink, LongSupplier clock) {
      this.sink = sink;
      this.clock = clock;
   }

   static AiRunDiagnostics start() {
      if (active != null) {
         active.stopped = true;
      }

      active = new AiRunDiagnostics(AiRunTrace::emitServiceEvent, System::currentTimeMillis);
      AiRunTrace trace = AiRunTrace.current();
      if (trace != null) {
         trace.expandLimitForLongRun();
      }

      return active;
   }

   static AiOperation trace(String controller, String decision, AiOperation op) {
      return active != null && !active.stopped && op != null ? active.wrap(controller, decision, op) : op;
   }

   AiOperation wrap(final String controller, final String decision, final AiOperation delegate) {
      return new AiOperation() {
         long operationId;
         long startedAtMs;
         long nextProgressAtMs;
         boolean ended;
         boolean cancelled;

         void ensureStarted() {
            if (this.operationId == 0L && !AiRunDiagnostics.this.stopped) {
               this.operationId = ++AiRunDiagnostics.this.nextOperationId;
               this.startedAtMs = AiRunDiagnostics.this.clock.getAsLong();
               JsonObject entry = AiJson.objectOf(
                  "operationId",
                  this.operationId,
                  "parentOperationId",
                  AiRunDiagnostics.this.currentOperationId,
                  "controller",
                  controller,
                  "decision",
                  AiRunDiagnostics.truncate(decision),
                  "startedAtMs",
                  this.startedAtMs
               );
               AiRunDiagnostics.this.openOperations.put(this.operationId, entry);
               AiRunDiagnostics.this.emit("speedrun_operation_start", entry);
            }
         }

         @Override
         public AiOperation.Result tick() {
            try {
               this.ensureStarted();
            } catch (RuntimeException failure) {
               AiRunDiagnostics.this.emit("speedrun_diagnostic_error", AiJson.objectOf("message", AiRunDiagnostics.truncate(failure.toString())));
            }

            long previous = AiRunDiagnostics.this.currentOperationId;
            AiRunDiagnostics.this.currentOperationId = this.operationId;

            try {
               AiOperation.Result result = delegate.tick();

               try {
                  if (this.operationId != 0L && !AiRunDiagnostics.this.stopped && !this.ended) {
                     JsonObject entry = AiRunDiagnostics.this.openOperations.get(this.operationId);
                     if (entry != null) {
                        entry.addProperty("message", AiRunDiagnostics.truncate(result.message()));
                     }

                     long now = AiRunDiagnostics.this.clock.getAsLong();
                     if (result.terminal() || now >= this.nextProgressAtMs) {
                        JsonObject event = AiJson.objectOf(
                           "operationId",
                           this.operationId,
                           "controller",
                           controller,
                           "decision",
                           AiRunDiagnostics.truncate(decision),
                           "status",
                           result.status().name(),
                           "message",
                           AiRunDiagnostics.truncate(result.message()),
                           "durationMs",
                           Math.max(0L, now - this.startedAtMs),
                           "failureCode",
                           result.failure().name(),
                           "result",
                           AiRunResources.compactEvidenceJson(result.dataJson())
                        );
                        if (result.terminal() && result.status() != AiOperation.Status.SUCCEEDED) {
                           Minecraft client = Minecraft.getInstance();
                           if (client != null && client.player != null && client.level != null) {
                              event.add("body", AiRunDiagnostics.this.bodyJson(client));
                              event.add("localTerrain", AiRunDiagnostics.localTerrainJson(client));
                           }
                        }

                        if (entry != null) {
                           entry.add("result", event.get("result").deepCopy());
                        }

                        AiRunDiagnostics.this.emit(result.terminal() ? "speedrun_operation_end" : "speedrun_operation_progress", event);
                        this.nextProgressAtMs = now + 5000L;
                     }

                     if (result.terminal()) {
                        this.ended = true;
                        AiRunDiagnostics.this.openOperations.remove(this.operationId);
                     }
                  }
               } catch (RuntimeException failure) {
                  if (result.terminal()) {
                     this.ended = true;
                     AiRunDiagnostics.this.openOperations.remove(this.operationId);
                  }

                  AiRunDiagnostics.this.emit("speedrun_diagnostic_error", AiJson.objectOf("operationId", this.operationId, "message", AiRunDiagnostics.truncate(failure.toString())));
               }

               return result;
            } catch (RuntimeException | Error failure) {
               JsonObject event = AiJson.objectOf(
                  "operationId",
                  this.operationId,
                  "controller",
                  controller,
                  "decision",
                  AiRunDiagnostics.truncate(decision),
                  "exception",
                  failure.getClass().getName(),
                  "message",
                  AiRunDiagnostics.truncate(String.valueOf(failure.getMessage()))
               );
               StringWriter stack = new StringWriter();
               failure.printStackTrace(new PrintWriter(stack));
               event.addProperty("stack", AiRunDiagnostics.truncate(stack.toString()));
               AiRunDiagnostics.this.emit("speedrun_operation_exception", event);
               throw failure;
            } finally {
               AiRunDiagnostics.this.currentOperationId = previous;
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
            if (!this.cancelled) {
               try {
                  delegate.cancel();
                  this.cancelled = true;
               } finally {
                  if (this.cancelled) {
                     try {
                        if (this.operationId != 0L && !this.ended) {
                           AiRunDiagnostics.this.emit(
                              "speedrun_operation_cancel",
                              AiJson.objectOf(
                                 "operationId",
                                 this.operationId,
                                 "controller",
                                 controller,
                                 "decision",
                                 AiRunDiagnostics.truncate(decision),
                                 "durationMs",
                                 Math.max(0L, AiRunDiagnostics.this.clock.getAsLong() - this.startedAtMs)
                              )
                           );
                        }
                     } catch (RuntimeException var7) {
                     }

                     AiRunDiagnostics.this.openOperations.remove(this.operationId);
                  }
               }
            }
         }
      };
   }

   private void emit(String kind, JsonObject data) {
      if (!this.stopped) {
         try {
            this.sink.accept(kind, data.deepCopy());
         } catch (RuntimeException var4) {
         }
      }
   }

   void sample(String phase, String message) {
      if (!this.stopped) {
         try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
               return;
            }

            long now = this.clock.getAsLong();
            if (now >= this.nextMotionSampleMs) {
               this.nextMotionSampleMs = now + 500L;
               this.recentMotion.addLast(this.bodyJson(mc));

               while (this.recentMotion.size() > 120) {
                  this.recentMotion.removeFirst();
               }
            }

            boolean hurt = this.lastHealth >= 0.0F && mc.player.getHealth() < this.lastHealth - 0.01F;
            this.lastHealth = mc.player.getHealth();
            if (now < this.throttleUntil && !hurt) {
               return;
            }

            this.throttleUntil = now + 5000L;
            JsonObject snapshot = this.buildSnapshot(phase, message, false);
            if (hurt) {
               DamageSource damage = mc.player.getLastDamageSource();
               snapshot.addProperty("damageSource", damage == null ? "unavailable" : damage.getMsgId());
               snapshot.add("recentMotion", this.recentMotionJson());
            }

            this.emit(hurt ? "speedrun_damage_snapshot" : "speedrun_snapshot", snapshot);
            AiService service = AiService.getExistingInstance();
            if (service != null) {
               service.saveSpeedrunDiagnostics(snapshot);
            }
         } catch (RuntimeException failure) {
            this.emit("speedrun_diagnostic_error", AiJson.objectOf("message", truncate(failure.toString())));
         }
      }
   }

   JsonObject snapshot(String status, String phase, String message) {
      JsonObject snapshot;
      try {
         snapshot = this.buildSnapshot(phase, message, true);
      } catch (RuntimeException failure) {
         snapshot = AiJson.objectOf("diagnosticError", truncate(failure.toString()));
      }

      snapshot.addProperty("status", status);
      this.emit("speedrun_final_snapshot", snapshot);
      AiService service = AiService.getExistingInstance();
      if (service != null) {
         service.saveSpeedrunDiagnostics(snapshot);
      }

      this.stopped = true;
      if (active == this) {
         active = null;
      }

      return snapshot;
   }

   private JsonObject buildSnapshot(String phase, String message, boolean finalSnapshot) {
      Minecraft mc = Minecraft.getInstance();
      JsonArray operations = new JsonArray();
      JsonArray nearby = new JsonArray();
      JsonArray slots = new JsonArray();
      JsonArray navigation = new JsonArray();
      this.openOperations.values().forEach(entry -> operations.add(entry.deepCopy()));
      List<String> nav = autismclient.ai.engine.AiNavDebug.readSince(Math.max(0L, autismclient.ai.engine.AiNavDebug.cursor() - 24L));
      nav.forEach(navigation::add);
      JsonObject out = AiJson.objectOf(
         "observedAtMs",
         this.clock.getAsLong(),
         "phase",
         phase,
         "message",
         truncate(message),
         "operations",
         operations,
         "navigation",
         navigation,
         "resources",
         AiRunResources.inventorySnapshotJson()
      );
      if (mc.player != null && mc.level != null) {
         out.add("body", this.bodyJson(mc));
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
         out.add("localTerrain", localTerrainJson(mc));

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
         out.add("recentMotion", this.recentMotionJson());
      }

      return out;
   }

   private static JsonObject localTerrainJson(Minecraft mc) {
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

   private JsonArray recentMotionJson() {
      JsonArray out = new JsonArray();
      this.recentMotion.forEach(entry -> out.add(entry.deepCopy()));
      return out;
   }

   private JsonObject bodyJson(Minecraft mc) {
      LocalPlayer p = mc.player;
      Options keys = mc.options;
      Vec3 velocity = p.getDeltaMovement();
      return AiJson.objectOf(
         "atMs",
         this.clock.getAsLong(),
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

   static String truncate(String value) {
      return value.length() <= 12000 ? value : value.substring(0, 12000);
   }
}
