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

final class bJ {
   private static bJ init_bJ;
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

   bJ(BiConsumer<String, JsonObject> sink, LongSupplier clock) {
      this.init_BiConsumer = sink;
      this.init_LongSupplier = clock;
   }

   static bJ init_bJ() {
      if (init_bJ != null) {
         init_bJ.init_Z = true;
      }

      init_bJ = new bJ(bL::init_V_Str_JsonObject, System::currentTimeMillis);
      bL trace = bL.init_bL();
      if (trace != null) {
         trace.load();
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
            if (this.init_J == 0L && !bJ.this.init_Z) {
               this.init_J = ++bJ.this.init_J;
               this.load_J = bJ.this.init_LongSupplier.getAsLong();
               JsonObject entry = ag.init_JsonObject_ObjArr(
                  "operationId",
                  this.init_J,
                  "parentOperationId",
                  bJ.this.load,
                  "controller",
                  controller,
                  "decision",
                  bJ.init(decision),
                  "startedAtMs",
                  this.load_J
               );
               bJ.this.init_Map.put(this.init_J, entry);
               bJ.this.init("speedrun_operation_start", entry);
            }
         }

         @Override
         public AiOperation.Result tick() {
            try {
               this.init();
            } catch (RuntimeException failure) {
               bJ.this.init("speedrun_diagnostic_error", ag.init_JsonObject_ObjArr("message", bJ.init(failure.toString())));
            }

            long previous = bJ.this.load;
            bJ.this.load = this.init_J;

            try {
               AiOperation.Result result = delegate.tick();

               try {
                  if (this.init_J != 0L && !bJ.this.init_Z && !this.init_Z) {
                     JsonObject entry = bJ.this.init_Map.get(this.init_J);
                     if (entry != null) {
                        entry.addProperty("message", bJ.init(result.message()));
                     }

                     long now = bJ.this.init_LongSupplier.getAsLong();
                     if (result.terminal() || now >= this.read) {
                        JsonObject event = ag.init_JsonObject_ObjArr(
                           "operationId",
                           this.init_J,
                           "controller",
                           controller,
                           "decision",
                           bJ.init(decision),
                           "status",
                           result.status().name(),
                           "message",
                           bJ.init(result.message()),
                           "durationMs",
                           Math.max(0L, now - this.load_J),
                           "failureCode",
                           result.failure().name(),
                           "result",
                           bK.load_JsonObject(result.dataJson())
                        );
                        if (result.terminal() && result.status() != AiOperation.Status.SUCCEEDED) {
                           Minecraft client = Minecraft.getInstance();
                           if (client != null && client.player != null && client.level != null) {
                              event.add("body", bJ.this.load(client));
                              event.add("localTerrain", bJ.init(client));
                           }
                        }

                        if (entry != null) {
                           entry.add("result", event.get("result").deepCopy());
                        }

                        bJ.this.init(result.terminal() ? "speedrun_operation_end" : "speedrun_operation_progress", event);
                        this.read = now + 5000L;
                     }

                     if (result.terminal()) {
                        this.init_Z = true;
                        bJ.this.init_Map.remove(this.init_J);
                     }
                  }
               } catch (RuntimeException failure) {
                  if (result.terminal()) {
                     this.init_Z = true;
                     bJ.this.init_Map.remove(this.init_J);
                  }

                  bJ.this.init("speedrun_diagnostic_error", ag.init_JsonObject_ObjArr("operationId", this.init_J, "message", bJ.init(failure.toString())));
               }

               return result;
            } catch (RuntimeException | Error failure) {
               JsonObject event = ag.init_JsonObject_ObjArr(
                  "operationId",
                  this.init_J,
                  "controller",
                  controller,
                  "decision",
                  bJ.init(decision),
                  "exception",
                  failure.getClass().getName(),
                  "message",
                  bJ.init(String.valueOf(failure.getMessage()))
               );
               StringWriter stack = new StringWriter();
               failure.printStackTrace(new PrintWriter(stack));
               event.addProperty("stack", bJ.init(stack.toString()));
               bJ.this.init("speedrun_operation_exception", event);
               throw failure;
            } finally {
               bJ.this.load = previous;
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
                           bJ.this.init(
                              "speedrun_operation_cancel",
                              ag.init_JsonObject_ObjArr(
                                 "operationId",
                                 this.init_J,
                                 "controller",
                                 controller,
                                 "decision",
                                 bJ.init(decision),
                                 "durationMs",
                                 Math.max(0L, bJ.this.init_LongSupplier.getAsLong() - this.load_J)
                              )
                           );
                        }
                     } catch (RuntimeException var7) {
                     }

                     bJ.this.init_Map.remove(this.init_J);
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
            cc service = cc.load_cc();
            if (service != null) {
               service.read_V_JsonObject(snapshot);
            }
         } catch (RuntimeException failure) {
            this.init("speedrun_diagnostic_error", ag.init_JsonObject_ObjArr("message", init(failure.toString())));
         }
      }
   }

   JsonObject init_JsonObject_Str(String status, String phase, String message) {
      JsonObject snapshot;
      try {
         snapshot = this.init_JsonObject_Str_2(phase, message, true);
      } catch (RuntimeException failure) {
         snapshot = ag.init_JsonObject_ObjArr("diagnosticError", init(failure.toString()));
      }

      snapshot.addProperty("status", status);
      this.init("speedrun_final_snapshot", snapshot);
      cc service = cc.load_cc();
      if (service != null) {
         service.read_V_JsonObject(snapshot);
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
      List<String> nav = autismclient.ai.a.ListPool.init(Math.max(0L, autismclient.ai.a.ListPool.init_J() - 24L));
      nav.forEach(navigation::add);
      JsonObject out = ag.init_JsonObject_ObjArr(
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
         bK.init_JsonObject()
      );
      if (mc.player != null && mc.level != null) {
         out.add("body", this.load(mc));
         BlockPos feet = mc.player.blockPosition();
         out.add(
            "terrain",
            ag.init_JsonObject_ObjArr(
               "feet",
               mc.level.getBlockState(feet).toString(),
               "head",
               mc.level.getBlockState(feet.above()).toString(),
               "floor",
               mc.level.getBlockState(feet.below()).toString(),
               "loaded",
               mc.level.hasChunkAt(feet),
               "feetRevision",
               ca.init_J_BlockPos(feet),
               "floorRevision",
               ca.init_J_BlockPos(feet.below())
            )
         );
         out.add("localTerrain", init(mc));

         for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
               slots.add(
                  ag.init_JsonObject_ObjArr(
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
         out.addProperty("containerRevision", ca.init_J_I(mc.player.containerMenu.containerId));
         ArrayList<Entity> candidates = new ArrayList<>();

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity != mc.player && entity.isAlive() && !(entity.distanceToSqr(mc.player) > 1024.0)) {
               candidates.add(entity);
            }
         }

         candidates.sort(Comparator.comparingDouble(entityx -> entityx.distanceToSqr(mc.player)));

         for (Entity entity : candidates.subList(0, Math.min(16, candidates.size()))) {
            JsonObject info = ag.init_JsonObject_ObjArr(
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
               ag.init_JsonObject_ObjArr(
                  "dx", cell.getX() - origin.getX(), "dy", cell.getY() - origin.getY(), "dz", cell.getZ() - origin.getZ(), "state", state
               )
            );
         }
      }

      return ag.init_JsonObject_ObjArr("origin", origin.toShortString(), "horizontalRadius", 2, "minDy", -1, "maxDy", 3, "omittedCells", "air", "cells", cells);
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
      return ag.init_JsonObject_ObjArr(
         "atMs",
         this.init_LongSupplier.getAsLong(),
         "tick",
         p.tickCount,
         "dimension",
         cL.load(mc),
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
         ag.init_JsonObject_ObjArr(
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
