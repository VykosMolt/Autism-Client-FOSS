package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class Y {
   private final X.Inner_CachePool init_X_CachePool;
   private final String init_Str;
   private AiOperation init_AiOperation;
   private K init_K;
   private AiOperation load_AiOperation;
   private String load_Str = "";
   private long init_J;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read;
   private boolean write;
   private boolean update;
   private boolean sync;
   private boolean flush;
   private boolean close;
   private X.Inner_DataUtil init_X_DataUtil = X.Inner_DataUtil.init;
   private Object init_Obj;
   private Object load_Obj;
   private final Vec3[] init_Vec3Arr = new Vec3[10];
   private int init_I;
   private long load_J = -1L;

   Y(X.Inner_CachePool policy, String owner) {
      this.init_X_CachePool = policy;
      this.init_Str = owner;
   }

   boolean init_Z() {
      return this.init_AiOperation != null || this.init_K != null || this.load_AiOperation != null;
   }

   boolean load() {
      return !this.close
         && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
         && (this.init_K == null || this.init_K.load_Z())
         && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow());
   }

   boolean read() {
      return this.close
         || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
         || this.init_K != null && this.init_K.read()
         || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency();
   }

   AiOperation.Result init_AiOperation_Result_AiOperation(AiOperation leg, Runnable cancelLeg, IntSupplier remainingSeconds) {
      X.init_V();
      Minecraft zoneClient = Minecraft.getInstance();
      if (zoneClient.player != null && zoneClient.level != null && zoneClient.player.isAlive()) {
         if (this.init_Obj != zoneClient.level || this.load_Obj != zoneClient.player) {
            boolean reconcile = this.read() || this.close;
            this.init_V();
            this.init_Obj = zoneClient.level;
            this.load_Obj = zoneClient.player;
            if (reconcile) {
               return init_AiOperation_Result_Z(
                  true, "world or player changed", init_AiOperation_Result_Str("Observe pending changes in the new context before restarting work")
               );
            }
         }

         Vec3 motion = this.init_Vec3_Vec3(zoneClient.player.position());
         boolean environmentDamage = cc.init_Z_Player(zoneClient.player);
         boolean interruptedMeal = false;
         if (environmentDamage && !cv.init_Z() && !this.update && zoneClient.player != null && ak.init_Z(zoneClient)) {
            this.init_V_AiOperation_Runnable(leg, cancelLeg);
            this.load_Z = false;
            this.init_Z = false;
            this.load_Str = "in lava";
            this.init_AiOperation = bJ.init_AiOperation_Str("survival", this.init_Str + ": " + this.load_Str, ak.init());
            this.write = true;
            this.update = true;
            cl.init_V_Str_ObjArr("[AI] {}: in lava at {}; swimming out", this.init_Str, zoneClient.player.blockPosition().toShortString());
            return this.init_AiOperation_Result();
         }

         if (environmentDamage && !cv.init_Z() && !this.sync && !this.update && zoneClient.player != null && EventBus.init(zoneClient.player)) {
            this.init_V_AiOperation_Runnable(leg, cancelLeg);
            this.load_Z = false;
            this.init_Z = false;
            this.load_Str = "out of air";
            this.init_AiOperation = bJ.init_AiOperation_Str(
               "survival", this.init_Str + ": " + this.load_Str, EventBus.init(ag.init_JsonObject_ObjArr("timeoutSeconds", 25))
            );
            this.write = true;
            this.sync = true;
            cl.init_V_Str_ObjArr(
               "[AI] {}: out of air at {} (air {}); swimming up",
               this.init_Str,
               zoneClient.player.blockPosition().toShortString(),
               zoneClient.player.getAirSupply()
            );
            return this.init_AiOperation_Result();
         }

         if (environmentDamage && !cv.init_Z() && !this.update && !this.sync && !this.flush && ba.init_Z(zoneClient)) {
            this.init_V_AiOperation_Runnable(leg, cancelLeg);
            this.load_Z = this.init_Z = false;
            this.load_Str = "in powder snow";
            this.init_AiOperation = bJ.init_AiOperation_Str("survival", this.init_Str + ": " + this.load_Str, ba.init());
            this.write = this.flush = true;
            return this.init_AiOperation_Result();
         }

         if (!cv.init_Z() && !this.write && zoneClient.player != null) {
            AiOperation out = r.init_Z(zoneClient) ? r.load(zoneClient) : null;
            boolean cloud = false;
            if (out == null && !zoneClient.player.isInWater() && q.init_Z(zoneClient)) {
               out = q.init_AiOperation(zoneClient);
               if (out == null) {
                  out = r.init_AiOperation(zoneClient);
               }

               cloud = true;
            }

            if (out != null) {
               this.init_V_AiOperation_Runnable(leg, cancelLeg);
               this.load_Z = false;
               this.init_Z = false;
               this.load_Str = cloud ? "in a lingering cloud" : "in the dragon's wing push";
               this.init_AiOperation = bJ.init_AiOperation_Str("survival", this.init_Str + ": " + this.load_Str, out);
               this.write = true;
               cl.init_V_Str_ObjArr("[AI] {}: {} at {}; stepping out", this.init_Str, this.load_Str, zoneClient.player.blockPosition().toShortString());
               return this.init_AiOperation_Result();
            }
         }

         if (environmentDamage
            && this.load_AiOperation == null
            && zoneClient.player != null
            && !zoneClient.player.isInWater()
            && (zoneClient.player.isOnFire() || u.init_Z(zoneClient))) {
            this.load_AiOperation = u.init_AiOperation(zoneClient);
         }

         if (this.load_AiOperation != null) {
            AiOperation.Result doused = this.load_AiOperation.tick();
            if (!doused.terminal()) {
               return R.init_AiOperation_Result(
                  AiOperation.Status.RUNNING,
                  "Dousing body fire before " + this.init_Str + ": " + doused.message(),
                  ag.init_JsonObject_ObjArr("legPreserved", true)
               );
            }

            this.load_AiOperation.cancel();
            this.load_AiOperation = null;
            if (doused.status() != AiOperation.Status.SUCCEEDED) {
               cl.init_V_Str_ObjArr("[AI] guard: water douse failed ({}); falling back to fire control", doused.message());
            }
         }

         boolean pendingBodyFire = this.init_K != null && this.init_K.init_Z();
         if (!cv.init_Z()
            && !this.write
            && zoneClient.player != null
            && !zoneClient.player.isInWater()
            && (
               pendingBodyFire
                  || zoneClient.player.containerMenu == zoneClient.player.inventoryMenu
                     && (leg == null || !leg.requiresReconciliationAfterEmergency())
                     && (this.init_AiOperation == null || !this.init_AiOperation.requiresReconciliationAfterEmergency())
                     && zoneClient.player.getHealth() >= 8.0F
                     && autismclient.ai.a.RefQueue.flush_Z()
                     && u.init_Z(zoneClient)
            )) {
            if (this.init_K == null) {
               this.init_K = new K(true);
            }

            if (this.init_K.init(false, null)) {
               return R.init_AiOperation_Result(
                  AiOperation.Status.RUNNING,
                  "Extinguish body fire before resuming " + this.init_Str + ": " + this.init_K.init_Str(),
                  ag.init_JsonObject_ObjArr("firesExtinguished", this.init_K.init_I(), "legPreserved", true)
               );
            }
         }

         if (this.init_K != null && !this.init_K.init_Z()) {
            this.init_K.init_V();
            this.init_K = null;
         }

         if (environmentDamage && !cv.init_Z() && !this.write && zoneClient.player != null && !zoneClient.player.isInWater() && u.init_Z(zoneClient)) {
            boolean stepFailedLately = System.nanoTime() - this.init_J < 3000000000L;
            AiOperation out = stepFailedLately ? null : u.load_AiOperation(zoneClient);
            if (out != null) {
               this.init_V_AiOperation_Runnable(leg, cancelLeg);
               this.load_Z = false;
               this.init_Z = false;
               this.load_Str = "standing in fire";
               this.init_AiOperation = bJ.init_AiOperation_Str("survival", this.init_Str + ": " + this.load_Str, out);
               this.write = true;
               return this.init_AiOperation_Result();
            }

            if (u.load_Z(zoneClient)) {
               return init_AiOperation_Result_Str("Interrupted: putting out the fire");
            }
         }

         Minecraft client = Minecraft.getInstance();
         if (environmentDamage && !cv.init_Z() && !this.write && !client.player.isInWater() && (u.init_Z(client) || client.player.isOnFire())) {
            AiOperation out = client.player.isOnFire() ? u.init_AiOperation(client) : null;
            if (out == null && u.init_Z(client) && System.nanoTime() - this.init_J >= 3000000000L) {
               out = u.load_AiOperation(client);
            }

            if (out == null && u.init_Z(client) && u.load_Z(client)) {
               this.init_V_AiOperation_Runnable(leg, cancelLeg);
               return init_AiOperation_Result_Str("Interrupted: putting out the fire");
            }

            if (out != null) {
               this.load_Z = false;
               this.init_Z = false;
               this.load_Str = "on fire";
               this.init_V_AiOperation_Runnable(leg, cancelLeg);
               this.write = true;
               this.init_AiOperation = bJ.init_AiOperation_Str("survival", this.init_Str + ": " + this.load_Str, out);
               return this.init_AiOperation_Result();
            }
         }

         if (!cv.init_Z() && this.init_AiOperation != null && !this.write && CacheEntry.init(this.init_X_DataUtil) && p.init(zoneClient, false)) {
            this.init_V_AiOperation_Runnable(leg, cancelLeg);
            this.init_Z = this.load_Z = false;
            this.load_Str = "incoming projectile during maintenance";
            this.init_AiOperation = bJ.init_AiOperation_Str(
               "survival", this.init_Str + ": " + this.load_Str, p.init(ag.init_JsonObject_ObjArr("deflect", true, "shield", true, "timeoutSeconds", 6))
            );
            return this.init_AiOperation_Result();
         }

         if (this.init_AiOperation != null
            && !this.write
            && this.init_AiOperation.canInterruptNow()
            && !this.init_AiOperation.requiresReconciliationAfterEmergency()
            && autismclient.ai.a.RefQueue.flush_Z()) {
            X.Inner_BufferHelper live = X.init_X_BufferHelper();
            X.Inner_DataUtil next = X.init_X_StreamUtil(this.init_X_CachePool_Minecraft(zoneClient), live).action();
            if (CacheEntry.init_Z_X_DataUtil(this.init_X_DataUtil, next, live.hostilesNear())
               || this.init_X_DataUtil == X.Inner_DataUtil.write && cq.init_Entity(zoneClient) != null) {
               bL.init_V_Str_JsonObject(
                  "survival_reprioritize",
                  ag.init_JsonObject_ObjArr(
                     "owner", this.init_Str, "from", this.init_X_DataUtil.name(), "to", next.name(), "health", live.health(), "hostiles", live.hostilesNear()
                  )
               );
               interruptedMeal = this.init_X_DataUtil == X.Inner_DataUtil.write && cq.init_Entity(zoneClient) != null;
               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
               this.init_X_DataUtil = X.Inner_DataUtil.init;
               this.init_Z = this.load_Z = false;
            }
         }

         if (this.init_AiOperation != null) {
            return this.init_AiOperation_Result();
         }

         if (autismclient.ai.a.RefQueue.flush_Z() && (leg == null || leg.canInterruptNow())) {
            X.Inner_BufferHelper vitals = X.init_X_BufferHelper();
            boolean moving = motion.horizontalDistance() >= 0.12;
            if (!cv.init_Z() && !d.init(Minecraft.getInstance()) && !UiHelper.init() && p.init(Minecraft.getInstance(), moving)) {
               this.load_Z = false;
               this.init_Z = false;
               this.load_Str = "incoming projectile";
               if (leg != null) {
                  this.close = this.close | leg.requiresReconciliationAfterEmergency();
                  cancelLeg.run();
               }

               this.init_AiOperation = bJ.init_AiOperation_Str(
                  "survival", this.init_Str + ": projectile dodge", p.init(ag.init_JsonObject_ObjArr("deflect", true, "shield", true, "timeoutSeconds", 6))
               );
               return this.init_AiOperation_Result();
            } else if (!cv.init_Z() && vitals.hostilesNear() > 0 && leg != null && vitals.health() >= 8.0F && X.init(Minecraft.getInstance(), motion)) {
               if (!this.read) {
                  cl.init_V_Str_ObjArr("[AI] {}: guard leaves {} behind (on the move)", this.init_Str, X.init_Str);
               }

               this.read = true;
               return null;
            } else {
               this.read = false;
               if (!cv.init_Z() && q.init_Z(Minecraft.getInstance())) {
                  AiOperation out = q.init_AiOperation(Minecraft.getInstance());
                  if (out != null) {
                     this.load_Z = false;
                     this.init_Z = false;
                     this.load_Str = "in a lingering cloud";
                     if (leg != null) {
                        this.close = this.close | leg.requiresReconciliationAfterEmergency();
                        cancelLeg.run();
                     }

                     this.init_AiOperation = bJ.init_AiOperation_Str("survival", this.init_Str + ": " + this.load_Str, out);
                     return this.init_AiOperation_Result();
                  }
               }

               Minecraft mc = Minecraft.getInstance();
               X.Inner_CachePool policy = this.init_X_CachePool_Minecraft(mc);
               boolean unsafeMeal = interruptedMeal || X.init_X_StreamUtil(policy, vitals).action() == X.Inner_DataUtil.write && cq.init_Entity(mc) != null;
               AiOperation action = unsafeMeal
                  ? cq.init_AiOperation_Minecraft(mc, policy, remainingSeconds.getAsInt())
                  : X.init_AiOperation_X_CachePool(policy, vitals, remainingSeconds.getAsInt());
               if (action == null) {
                  return null;
               }

               this.init_X_DataUtil = unsafeMeal ? X.Inner_DataUtil.load : X.init_X_StreamUtil(policy, vitals).action();
               this.load_Z = X.read_Z;
               if (this.load_Z) {
                  this.init_X_DataUtil = X.Inner_DataUtil.read;
               }

               this.init_Z = !this.load_Z && this.init_X_DataUtil == X.Inner_DataUtil.load;
               this.load_Str = unsafeMeal ? "nearby enemy prevents a safe meal" : X.init_Str(policy, vitals);
               cl.init_V_Str_ObjArr("[AI] {}: guard steps in ({}, health {})", this.init_Str, this.load_Str, (int)vitals.health());
               if (leg != null) {
                  this.close = this.close | leg.requiresReconciliationAfterEmergency();
                  cancelLeg.run();
               }

               this.init_AiOperation = bJ.init_AiOperation_Str("survival", this.init_Str + ": " + this.load_Str, action);
               return this.init_AiOperation_Result();
            }
         } else {
            return null;
         }
      } else {
         boolean reconcile = this.read();
         this.init_V();
         AiOperation.Result ended = R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Survival layer lost the living player", new JsonObject());
         return reconcile ? init_AiOperation_Result_Z(true, "player unavailable", ended) : ended;
      }
   }

   private X.Inner_CachePool init_X_CachePool_Minecraft(Minecraft mc) {
      return mc.level.dimension() == Level.OVERWORLD ? this.init_X_CachePool : this.init_X_CachePool.withoutFlee();
   }

   private Vec3 init_Vec3_Vec3(Vec3 here) {
      long tick = cv.init_J();
      Vec3 previous = this.init_Vec3Arr[(this.init_I + this.init_Vec3Arr.length - 1) % this.init_Vec3Arr.length];
      if (tick < this.load_J || tick - this.load_J > 1L || previous != null && here.distanceToSqr(previous) > 64.0) {
         Arrays.fill(this.init_Vec3Arr, null);
      }

      if (tick != this.load_J) {
         this.init_Vec3Arr[this.init_I] = here;
         this.init_I = (this.init_I + 1) % this.init_Vec3Arr.length;
         this.load_J = tick;
      }

      Vec3 past = this.init_Vec3Arr[this.init_I];
      return past == null ? Vec3.ZERO : here.subtract(past).scale(1.0 / (this.init_Vec3Arr.length - 1));
   }

   private void init_V_AiOperation_Runnable(AiOperation leg, Runnable cancelLeg) {
      this.close = this.close | this.read();
      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
         this.init_AiOperation = null;
      } else if (leg != null) {
         this.close = this.close | leg.requiresReconciliationAfterEmergency();
         cancelLeg.run();
      }

      if (this.init_K != null) {
         this.init_K.init_V();
         this.init_K = null;
      }

      this.init_X_DataUtil = X.Inner_DataUtil.init;
      this.write = this.update = this.sync = this.flush = false;
   }

   private AiOperation.Result init_AiOperation_Result() {
      AiOperation.Result r = this.init_AiOperation.tick();
      if (!r.terminal()) {
         return init_AiOperation_Result_Str("Interrupted: " + this.load_Str + " - " + r.message());
      }

      this.init_AiOperation.cancel();
      this.init_AiOperation = null;
      this.init_X_DataUtil = X.Inner_DataUtil.init;
      if (!this.close && r.status() != AiOperation.Status.UNCERTAIN) {
         if (this.write && "standing in fire".equals(this.load_Str) && r.status() != AiOperation.Status.SUCCEEDED) {
            this.init_J = System.nanoTime();
         }

         if (this.write) {
            this.write = false;
            this.update = false;
            this.sync = false;
            this.flush = false;
            return init_AiOperation_Result_Str("Back to work after " + this.load_Str);
         }

         if (r.status() != AiOperation.Status.SUCCEEDED) {
            X.read_V();
         }

         if (X.write_Z && r.status() != AiOperation.Status.SUCCEEDED) {
            X.sync_V();
         } else if (this.init_Z && r.status() != AiOperation.Status.SUCCEEDED && !X.init_Z && !X.load_Z) {
            X.write_V();
         }

         if (this.load_Z && (r.status() != AiOperation.Status.SUCCEEDED || X.init_X_BufferHelper().hostilesNear() > 0)) {
            X.load_V();
         }

         cl.init_V_Str_ObjArr("[AI] {}: guard {} after {}: {}", this.init_Str, r.status(), this.load_Str, cl.init_Str_Str(r.message()));
         return init_AiOperation_Result_Str("Back to work after " + this.load_Str);
      } else {
         AiOperation.Result uncertain = init_AiOperation_Result_Z(this.close, this.load_Str, r);
         this.close = false;
         this.write = this.update = this.sync = this.flush = false;
         return uncertain;
      }
   }

   void init_V() {
      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
         this.init_AiOperation = null;
      }

      if (this.init_K != null) {
         this.init_K.init_V();
         this.init_K = null;
      }

      if (this.load_AiOperation != null) {
         this.load_AiOperation.cancel();
         this.load_AiOperation = null;
      }

      this.init_X_DataUtil = X.Inner_DataUtil.init;
      this.init_Z = this.load_Z = this.read = this.write = this.update = this.sync = this.flush = this.close = false;
      this.load_Str = "";
      this.init_J = 0L;
      Arrays.fill(this.init_Vec3Arr, null);
      this.init_I = 0;
      this.load_J = -1L;
   }

   static AiOperation.Result init_AiOperation_Result_Z(boolean pending, String reason, AiOperation.Result result) {
      JsonObject data = bK.init_JsonObject(result.dataJson());
      data.addProperty("survivalReason", reason);
      data.addProperty("survivalStatus", result.status().name());
      data.addProperty("interruptedActionNeedsReconciliation", pending);
      String message = pending
         ? "Interrupted action needs reconciliation after " + reason + ": " + result.message()
         : "Survival action (" + reason + ") is uncertain: " + result.message();
      return new AiOperation.Result(AiOperation.Status.UNCERTAIN, message, data.toString(), result.evidence(), result.failure());
   }

   static AiOperation.Result init_AiOperation_Result_Z_AiOperation_Result(boolean pending, AiOperation.Result result) {
      if (!result.terminal()) {
         throw new IllegalArgumentException("A terminal result is required");
      }

      if (!pending) {
         return result;
      }

      JsonObject data = bK.init_JsonObject(result.dataJson());
      data.addProperty("terminationStatus", result.status().name());
      data.addProperty("interruptedActionNeedsReconciliation", true);
      data.addProperty("reobserveBeforeResuming", true);
      return new AiOperation.Result(
         AiOperation.Status.UNCERTAIN,
         result.message() + "; pending changes need observation before restarting work",
         data.toString(),
         result.evidence(),
         result.failure()
      );
   }

   static AiOperation init_AiOperation_Minecraft(Minecraft mc) {
      if (mc.player == null || mc.level == null || !cc.init_Z_Player(mc.player) || cv.init_Z()) {
         return null;
      } else if (ak.init_Z(mc)) {
         return ak.init();
      } else if (EventBus.init(mc.player)) {
         return EventBus.init(ag.init_JsonObject_ObjArr("timeoutSeconds", 25));
      } else if (ba.init_Z(mc)) {
         return ba.init();
      } else if (mc.player.isInWater()) {
         return null;
      } else if (u.init_Z(mc)) {
         return u.load_AiOperation(mc);
      } else {
         return mc.player.isOnFire() ? u.init_AiOperation(mc) : null;
      }
   }

   private static AiOperation.Result init_AiOperation_Result_Str(String message) {
      return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, new JsonObject());
   }
}
