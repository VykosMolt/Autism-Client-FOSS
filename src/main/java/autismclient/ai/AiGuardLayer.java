package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class AiGuardLayer {
   private final AiGuard.GuardPolicy policy;
   private final String ownerLabel;
   private AiOperation activeGuardOp;
   private AiFireControl fireControl;
   private AiOperation waterDouseOp;
   private String activeReason = "";
   private long lastFireStepFailNanos;
   private boolean activeCombat;
   private boolean activeFlee;
   private boolean loggedSprintPast;
   private boolean activeHazardEscape;
   private boolean activeLavaEscape;
   private boolean activeBreatheEscape;
   private boolean activePowderEscape;
   private boolean reconciliationPending;
   private AiGuard.GuardAction activeGuardAction = AiGuard.GuardAction.NONE;
   private Object trackedLevel;
   private Object trackedPlayer;
   private final Vec3[] recentPositions = new Vec3[10];
   private int positionRingIndex;
   private long lastPositionSampleTick = -1L;

   AiGuardLayer(AiGuard.GuardPolicy policy, String owner) {
      this.policy = policy;
      this.ownerLabel = owner;
   }

   boolean isActive() {
      return this.activeGuardOp != null || this.fireControl != null || this.waterDouseOp != null;
   }

   boolean canInterruptNow() {
      return !this.reconciliationPending
         && (this.activeGuardOp == null || this.activeGuardOp.canInterruptNow())
         && (this.fireControl == null || this.fireControl.canInterrupt())
         && (this.waterDouseOp == null || this.waterDouseOp.canInterruptNow());
   }

   boolean requiresReconciliation() {
      return this.reconciliationPending
         || this.activeGuardOp != null && this.activeGuardOp.requiresReconciliationAfterEmergency()
         || this.fireControl != null && this.fireControl.isBusy()
         || this.waterDouseOp != null && this.waterDouseOp.requiresReconciliationAfterEmergency();
   }

   AiOperation.Result tick(AiOperation leg, Runnable cancelLeg, IntSupplier remainingSeconds) {
      AiGuard.suppressBriefly();
      Minecraft zoneClient = Minecraft.getInstance();
      if (zoneClient.player != null && zoneClient.level != null && zoneClient.player.isAlive()) {
         if (this.trackedLevel != zoneClient.level || this.trackedPlayer != zoneClient.player) {
            boolean reconcile = this.requiresReconciliation() || this.reconciliationPending;
            this.cancel();
            this.trackedLevel = zoneClient.level;
            this.trackedPlayer = zoneClient.player;
            if (reconcile) {
               return wrapUncertainResult(
                  true, "world or player changed", runningResult("Observe pending changes in the new context before restarting work")
               );
            }
         }

         Vec3 motion = this.sampleAverageVelocity(zoneClient.player.position());
         boolean environmentDamage = AiService.isPlayerVulnerable(zoneClient.player);
         boolean interruptedMeal = false;
         if (environmentDamage && !AiThreats.isLegacyCombatEnabled() && !this.activeLavaEscape && zoneClient.player != null && AiLavaEscape.isInLava(zoneClient)) {
            this.preemptForHazard(leg, cancelLeg);
            this.activeFlee = false;
            this.activeCombat = false;
            this.activeReason = "in lava";
            this.activeGuardOp = AiRunDiagnostics.trace("survival", this.ownerLabel + ": " + this.activeReason, AiLavaEscape.escape());
            this.activeHazardEscape = true;
            this.activeLavaEscape = true;
            AiSpeedrun.log("[AI] {}: in lava at {}; swimming out", this.ownerLabel, zoneClient.player.blockPosition().toShortString());
            return this.tickActiveGuardOp();
         }

         if (environmentDamage && !AiThreats.isLegacyCombatEnabled() && !this.activeBreatheEscape && !this.activeLavaEscape && zoneClient.player != null && AiBreathe.needsAir(zoneClient.player)) {
            this.preemptForHazard(leg, cancelLeg);
            this.activeFlee = false;
            this.activeCombat = false;
            this.activeReason = "out of air";
            this.activeGuardOp = AiRunDiagnostics.trace(
               "survival", this.ownerLabel + ": " + this.activeReason, AiBreathe.create(AiJson.objectOf("timeoutSeconds", 25))
            );
            this.activeHazardEscape = true;
            this.activeBreatheEscape = true;
            AiSpeedrun.log(
               "[AI] {}: out of air at {} (air {}); swimming up",
               this.ownerLabel,
               zoneClient.player.blockPosition().toShortString(),
               zoneClient.player.getAirSupply()
            );
            return this.tickActiveGuardOp();
         }

         if (environmentDamage && !AiThreats.isLegacyCombatEnabled() && !this.activeLavaEscape && !this.activeBreatheEscape && !this.activePowderEscape && AiPowderEscape.isInPowderSnow(zoneClient)) {
            this.preemptForHazard(leg, cancelLeg);
            this.activeFlee = this.activeCombat = false;
            this.activeReason = "in powder snow";
            this.activeGuardOp = AiRunDiagnostics.trace("survival", this.ownerLabel + ": " + this.activeReason, AiPowderEscape.escape());
            this.activeHazardEscape = this.activePowderEscape = true;
            return this.tickActiveGuardOp();
         }

         if (!AiThreats.isLegacyCombatEnabled() && !this.activeHazardEscape && zoneClient.player != null) {
            AiOperation out = AiDragonZones.isDragonPerched(zoneClient) ? AiDragonZones.leave(zoneClient) : null;
            boolean cloud = false;
            if (out == null && !zoneClient.player.isInWater() && AiDragon.isInBreath(zoneClient)) {
               out = AiDragon.escapeBreath(zoneClient);
               if (out == null) {
                  out = AiDragonZones.dropOffPillar(zoneClient);
               }

               cloud = true;
            }

            if (out != null) {
               this.preemptForHazard(leg, cancelLeg);
               this.activeFlee = false;
               this.activeCombat = false;
               this.activeReason = cloud ? "in a lingering cloud" : "in the dragon's wing push";
               this.activeGuardOp = AiRunDiagnostics.trace("survival", this.ownerLabel + ": " + this.activeReason, out);
               this.activeHazardEscape = true;
               AiSpeedrun.log("[AI] {}: {} at {}; stepping out", this.ownerLabel, this.activeReason, zoneClient.player.blockPosition().toShortString());
               return this.tickActiveGuardOp();
            }
         }

         if (environmentDamage
            && this.waterDouseOp == null
            && zoneClient.player != null
            && !zoneClient.player.isInWater()
            && (zoneClient.player.isOnFire() || AiEndRun.isInFire(zoneClient))) {
            this.waterDouseOp = AiEndRun.douseFire(zoneClient);
         }

         if (this.waterDouseOp != null) {
            AiOperation.Result doused = this.waterDouseOp.tick();
            if (!doused.terminal()) {
               return AiGameOperations.result(
                  AiOperation.Status.RUNNING,
                  "Dousing body fire before " + this.ownerLabel + ": " + doused.message(),
                  AiJson.objectOf("legPreserved", true)
               );
            }

            this.waterDouseOp.cancel();
            this.waterDouseOp = null;
            if (doused.status() != AiOperation.Status.SUCCEEDED) {
               AiSpeedrun.log("[AI] guard: water douse failed ({}); falling back to fire control", doused.message());
            }
         }

         boolean pendingBodyFire = this.fireControl != null && this.fireControl.hasPendingFire();
         if (!AiThreats.isLegacyCombatEnabled()
            && !this.activeHazardEscape
            && zoneClient.player != null
            && !zoneClient.player.isInWater()
            && (
               pendingBodyFire
                  || zoneClient.player.containerMenu == zoneClient.player.inventoryMenu
                     && (leg == null || !leg.requiresReconciliationAfterEmergency())
                     && (this.activeGuardOp == null || !this.activeGuardOp.requiresReconciliationAfterEmergency())
                     && zoneClient.player.getHealth() >= 8.0F
                     && autismclient.ai.engine.AiMotionSafety.isPlayerSupported()
                     && AiEndRun.isInFire(zoneClient)
            )) {
            if (this.fireControl == null) {
               this.fireControl = new AiFireControl(true);
            }

            if (this.fireControl.init(false, null)) {
               return AiGameOperations.result(
                  AiOperation.Status.RUNNING,
                  "Extinguish body fire before resuming " + this.ownerLabel + ": " + this.fireControl.status(),
                  AiJson.objectOf("firesExtinguished", this.fireControl.firesExtinguished(), "legPreserved", true)
               );
            }
         }

         if (this.fireControl != null && !this.fireControl.hasPendingFire()) {
            this.fireControl.reset();
            this.fireControl = null;
         }

         if (environmentDamage && !AiThreats.isLegacyCombatEnabled() && !this.activeHazardEscape && zoneClient.player != null && !zoneClient.player.isInWater() && AiEndRun.isInFire(zoneClient)) {
            boolean stepFailedLately = System.nanoTime() - this.lastFireStepFailNanos < 3000000000L;
            AiOperation out = stepFailedLately ? null : AiEndRun.escapeFireOp(zoneClient);
            if (out != null) {
               this.preemptForHazard(leg, cancelLeg);
               this.activeFlee = false;
               this.activeCombat = false;
               this.activeReason = "standing in fire";
               this.activeGuardOp = AiRunDiagnostics.trace("survival", this.ownerLabel + ": " + this.activeReason, out);
               this.activeHazardEscape = true;
               return this.tickActiveGuardOp();
            }

            if (AiEndRun.extinguishFire(zoneClient)) {
               return runningResult("Interrupted: putting out the fire");
            }
         }

         Minecraft client = Minecraft.getInstance();
         if (environmentDamage && !AiThreats.isLegacyCombatEnabled() && !this.activeHazardEscape && !client.player.isInWater() && (AiEndRun.isInFire(client) || client.player.isOnFire())) {
            AiOperation out = client.player.isOnFire() ? AiEndRun.douseFire(client) : null;
            if (out == null && AiEndRun.isInFire(client) && System.nanoTime() - this.lastFireStepFailNanos >= 3000000000L) {
               out = AiEndRun.escapeFireOp(client);
            }

            if (out == null && AiEndRun.isInFire(client) && AiEndRun.extinguishFire(client)) {
               this.preemptForHazard(leg, cancelLeg);
               return runningResult("Interrupted: putting out the fire");
            }

            if (out != null) {
               this.activeFlee = false;
               this.activeCombat = false;
               this.activeReason = "on fire";
               this.preemptForHazard(leg, cancelLeg);
               this.activeHazardEscape = true;
               this.activeGuardOp = AiRunDiagnostics.trace("survival", this.ownerLabel + ": " + this.activeReason, out);
               return this.tickActiveGuardOp();
            }
         }

         if (!AiThreats.isLegacyCombatEnabled() && this.activeGuardOp != null && !this.activeHazardEscape && AiCombatPriority.isLowPriority(this.activeGuardAction) && AiDodge.hasImminentThreat(zoneClient, false)) {
            this.preemptForHazard(leg, cancelLeg);
            this.activeCombat = this.activeFlee = false;
            this.activeReason = "incoming projectile during maintenance";
            this.activeGuardOp = AiRunDiagnostics.trace(
               "survival", this.ownerLabel + ": " + this.activeReason, AiDodge.dodge(AiJson.objectOf("deflect", true, "shield", true, "timeoutSeconds", 6))
            );
            return this.tickActiveGuardOp();
         }

         if (this.activeGuardOp != null
            && !this.activeHazardEscape
            && this.activeGuardOp.canInterruptNow()
            && !this.activeGuardOp.requiresReconciliationAfterEmergency()
            && autismclient.ai.engine.AiMotionSafety.isPlayerSupported()) {
            AiGuard.StatusSnapshot live = AiGuard.sampleStatus();
            AiGuard.GuardAction next = AiGuard.decide(this.effectivePolicy(zoneClient), live).action();
            if (AiCombatPriority.shouldPreempt(this.activeGuardAction, next, live.hostilesNear())
               || this.activeGuardAction == AiGuard.GuardAction.EAT && AiSurvival.nearestThreat(zoneClient) != null) {
               AiRunTrace.emitServiceEvent(
                  "survival_reprioritize",
                  AiJson.objectOf(
                     "owner", this.ownerLabel, "from", this.activeGuardAction.name(), "to", next.name(), "health", live.health(), "hostiles", live.hostilesNear()
                  )
               );
               interruptedMeal = this.activeGuardAction == AiGuard.GuardAction.EAT && AiSurvival.nearestThreat(zoneClient) != null;
               this.activeGuardOp.cancel();
               this.activeGuardOp = null;
               this.activeGuardAction = AiGuard.GuardAction.NONE;
               this.activeCombat = this.activeFlee = false;
            }
         }

         if (this.activeGuardOp != null) {
            return this.tickActiveGuardOp();
         }

         if (autismclient.ai.engine.AiMotionSafety.isPlayerSupported() && (leg == null || leg.canInterruptNow())) {
            AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
            boolean moving = motion.horizontalDistance() >= 0.12;
            if (!AiThreats.isLegacyCombatEnabled() && !AiCover.isTakingCover(Minecraft.getInstance()) && !AiBlazeFight.init() && AiDodge.hasImminentThreat(Minecraft.getInstance(), moving)) {
               this.activeFlee = false;
               this.activeCombat = false;
               this.activeReason = "incoming projectile";
               if (leg != null) {
                  this.reconciliationPending = this.reconciliationPending | leg.requiresReconciliationAfterEmergency();
                  cancelLeg.run();
               }

               this.activeGuardOp = AiRunDiagnostics.trace(
                  "survival", this.ownerLabel + ": projectile dodge", AiDodge.dodge(AiJson.objectOf("deflect", true, "shield", true, "timeoutSeconds", 6))
               );
               return this.tickActiveGuardOp();
            } else if (!AiThreats.isLegacyCombatEnabled() && vitals.hostilesNear() > 0 && leg != null && vitals.health() >= 8.0F && AiGuard.canSprintPastThreats(Minecraft.getInstance(), motion)) {
               if (!this.loggedSprintPast) {
                  AiSpeedrun.log("[AI] {}: guard leaves {} behind (on the move)", this.ownerLabel, AiGuard.threatSummary);
               }

               this.loggedSprintPast = true;
               return null;
            } else {
               this.loggedSprintPast = false;
               if (!AiThreats.isLegacyCombatEnabled() && AiDragon.isInBreath(Minecraft.getInstance())) {
                  AiOperation out = AiDragon.escapeBreath(Minecraft.getInstance());
                  if (out != null) {
                     this.activeFlee = false;
                     this.activeCombat = false;
                     this.activeReason = "in a lingering cloud";
                     if (leg != null) {
                        this.reconciliationPending = this.reconciliationPending | leg.requiresReconciliationAfterEmergency();
                        cancelLeg.run();
                     }

                     this.activeGuardOp = AiRunDiagnostics.trace("survival", this.ownerLabel + ": " + this.activeReason, out);
                     return this.tickActiveGuardOp();
                  }
               }

               Minecraft mc = Minecraft.getInstance();
               AiGuard.GuardPolicy policy = this.effectivePolicy(mc);
               boolean unsafeMeal = interruptedMeal || AiGuard.decide(policy, vitals).action() == AiGuard.GuardAction.EAT && AiSurvival.nearestThreat(mc) != null;
               AiOperation action = unsafeMeal
                  ? AiSurvival.defend(mc, policy, remainingSeconds.getAsInt())
                  : AiGuard.createResponseOperation(policy, vitals, remainingSeconds.getAsInt());
               if (action == null) {
                  return null;
               }

               this.activeGuardAction = unsafeMeal ? AiGuard.GuardAction.FIGHT : AiGuard.decide(policy, vitals).action();
               this.activeFlee = AiGuard.fleeActive;
               if (this.activeFlee) {
                  this.activeGuardAction = AiGuard.GuardAction.FLEE;
               }

               this.activeCombat = !this.activeFlee && this.activeGuardAction == AiGuard.GuardAction.FIGHT;
               this.activeReason = unsafeMeal ? "nearby enemy prevents a safe meal" : AiGuard.decisionReason(policy, vitals);
               AiSpeedrun.log("[AI] {}: guard steps in ({}, health {})", this.ownerLabel, this.activeReason, (int)vitals.health());
               if (leg != null) {
                  this.reconciliationPending = this.reconciliationPending | leg.requiresReconciliationAfterEmergency();
                  cancelLeg.run();
               }

               this.activeGuardOp = AiRunDiagnostics.trace("survival", this.ownerLabel + ": " + this.activeReason, action);
               return this.tickActiveGuardOp();
            }
         } else {
            return null;
         }
      } else {
         boolean reconcile = this.requiresReconciliation();
         this.cancel();
         AiOperation.Result ended = AiGameOperations.result(AiOperation.Status.CANCELLED, "Survival layer lost the living player", new JsonObject());
         return reconcile ? wrapUncertainResult(true, "player unavailable", ended) : ended;
      }
   }

   private AiGuard.GuardPolicy effectivePolicy(Minecraft mc) {
      return mc.level.dimension() == Level.OVERWORLD ? this.policy : this.policy.withoutFlee();
   }

   private Vec3 sampleAverageVelocity(Vec3 here) {
      long tick = AiThreats.currentClientTick();
      Vec3 previous = this.recentPositions[(this.positionRingIndex + this.recentPositions.length - 1) % this.recentPositions.length];
      if (tick < this.lastPositionSampleTick || tick - this.lastPositionSampleTick > 1L || previous != null && here.distanceToSqr(previous) > 64.0) {
         Arrays.fill(this.recentPositions, null);
      }

      if (tick != this.lastPositionSampleTick) {
         this.recentPositions[this.positionRingIndex] = here;
         this.positionRingIndex = (this.positionRingIndex + 1) % this.recentPositions.length;
         this.lastPositionSampleTick = tick;
      }

      Vec3 past = this.recentPositions[this.positionRingIndex];
      return past == null ? Vec3.ZERO : here.subtract(past).scale(1.0 / (this.recentPositions.length - 1));
   }

   private void preemptForHazard(AiOperation leg, Runnable cancelLeg) {
      this.reconciliationPending = this.reconciliationPending | this.requiresReconciliation();
      if (this.activeGuardOp != null) {
         this.activeGuardOp.cancel();
         this.activeGuardOp = null;
      } else if (leg != null) {
         this.reconciliationPending = this.reconciliationPending | leg.requiresReconciliationAfterEmergency();
         cancelLeg.run();
      }

      if (this.fireControl != null) {
         this.fireControl.reset();
         this.fireControl = null;
      }

      this.activeGuardAction = AiGuard.GuardAction.NONE;
      this.activeHazardEscape = this.activeLavaEscape = this.activeBreatheEscape = this.activePowderEscape = false;
   }

   private AiOperation.Result tickActiveGuardOp() {
      AiOperation.Result r = this.activeGuardOp.tick();
      if (!r.terminal()) {
         return runningResult("Interrupted: " + this.activeReason + " - " + r.message());
      }

      this.activeGuardOp.cancel();
      this.activeGuardOp = null;
      this.activeGuardAction = AiGuard.GuardAction.NONE;
      if (!this.reconciliationPending && r.status() != AiOperation.Status.UNCERTAIN) {
         if (this.activeHazardEscape && "standing in fire".equals(this.activeReason) && r.status() != AiOperation.Status.SUCCEEDED) {
            this.lastFireStepFailNanos = System.nanoTime();
         }

         if (this.activeHazardEscape) {
            this.activeHazardEscape = false;
            this.activeLavaEscape = false;
            this.activeBreatheEscape = false;
            this.activePowderEscape = false;
            return runningResult("Back to work after " + this.activeReason);
         }

         if (r.status() != AiOperation.Status.SUCCEEDED) {
            AiGuard.noteBurrowInterrupted();
         }

         if (AiGuard.perchCombatActive && r.status() != AiOperation.Status.SUCCEEDED) {
            AiGuard.notePerchBackoff();
         } else if (this.activeCombat && r.status() != AiOperation.Status.SUCCEEDED && !AiGuard.burrowActive && !AiGuard.coverActive) {
            AiGuard.noteCombatBackoff();
         }

         if (this.activeFlee && (r.status() != AiOperation.Status.SUCCEEDED || AiGuard.sampleStatus().hostilesNear() > 0)) {
            AiGuard.noteRetreatBlocked();
         }

         AiSpeedrun.log("[AI] {}: guard {} after {}: {}", this.ownerLabel, r.status(), this.activeReason, AiSpeedrun.clip(r.message()));
         return runningResult("Back to work after " + this.activeReason);
      } else {
         AiOperation.Result uncertain = wrapUncertainResult(this.reconciliationPending, this.activeReason, r);
         this.reconciliationPending = false;
         this.activeHazardEscape = this.activeLavaEscape = this.activeBreatheEscape = this.activePowderEscape = false;
         return uncertain;
      }
   }

   void cancel() {
      if (this.activeGuardOp != null) {
         this.activeGuardOp.cancel();
         this.activeGuardOp = null;
      }

      if (this.fireControl != null) {
         this.fireControl.reset();
         this.fireControl = null;
      }

      if (this.waterDouseOp != null) {
         this.waterDouseOp.cancel();
         this.waterDouseOp = null;
      }

      this.activeGuardAction = AiGuard.GuardAction.NONE;
      this.activeCombat = this.activeFlee = this.loggedSprintPast = this.activeHazardEscape = this.activeLavaEscape = this.activeBreatheEscape = this.activePowderEscape = this.reconciliationPending = false;
      this.activeReason = "";
      this.lastFireStepFailNanos = 0L;
      Arrays.fill(this.recentPositions, null);
      this.positionRingIndex = 0;
      this.lastPositionSampleTick = -1L;
   }

   static AiOperation.Result wrapUncertainResult(boolean pending, String reason, AiOperation.Result result) {
      JsonObject data = AiRunResources.parseEvidenceJson(result.dataJson());
      data.addProperty("survivalReason", reason);
      data.addProperty("survivalStatus", result.status().name());
      data.addProperty("interruptedActionNeedsReconciliation", pending);
      String message = pending
         ? "Interrupted action needs reconciliation after " + reason + ": " + result.message()
         : "Survival action (" + reason + ") is uncertain: " + result.message();
      return new AiOperation.Result(AiOperation.Status.UNCERTAIN, message, data.toString(), result.evidence(), result.failure());
   }

   static AiOperation.Result wrapPendingTermination(boolean pending, AiOperation.Result result) {
      if (!result.terminal()) {
         throw new IllegalArgumentException("A terminal result is required");
      }

      if (!pending) {
         return result;
      }

      JsonObject data = AiRunResources.parseEvidenceJson(result.dataJson());
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

   static AiOperation createEmergencyEscapeOp(Minecraft mc) {
      if (mc.player == null || mc.level == null || !AiService.isPlayerVulnerable(mc.player) || AiThreats.isLegacyCombatEnabled()) {
         return null;
      } else if (AiLavaEscape.isInLava(mc)) {
         return AiLavaEscape.escape();
      } else if (AiBreathe.needsAir(mc.player)) {
         return AiBreathe.create(AiJson.objectOf("timeoutSeconds", 25));
      } else if (AiPowderEscape.isInPowderSnow(mc)) {
         return AiPowderEscape.escape();
      } else if (mc.player.isInWater()) {
         return null;
      } else if (AiEndRun.isInFire(mc)) {
         return AiEndRun.escapeFireOp(mc);
      } else {
         return mc.player.isOnFire() ? AiEndRun.douseFire(mc) : null;
      }
   }

   private static AiOperation.Result runningResult(String message) {
      return AiGameOperations.result(AiOperation.Status.RUNNING, message, new JsonObject());
   }
}
