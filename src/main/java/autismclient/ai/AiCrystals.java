package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.ai.engine.AiFallGuard;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiCrystals {
   static final double init_D = 64.0;
   static final float init_F = 4.0F;
   static final double load_D = 24.0;
   static final double read_D = 14.0;
   static final double write_D = 6.0;
   static final double update_D = 0.7;
   static final int init_I = 3;
   static final int load_I = 3;
   static final int read_I = 40;
   static final int write_I = 25;
   static final int update_I = 40;
   static final int sync = 2;

   private AiCrystals() {
   }

   static boolean isTowerPhase(AiCrystals.CrystalPhase phase, boolean towerRoute) {
      return phase == AiCrystals.CrystalPhase.PILLAR
         || phase == AiCrystals.CrystalPhase.OPEN_CAGE
         || phase == AiCrystals.CrystalPhase.DESCEND
         || towerRoute && (phase == AiCrystals.CrystalPhase.MELEE || phase == AiCrystals.CrystalPhase.SHOOT);
   }

   static boolean isTolerableBlastDamage(double damage, double health, boolean lethal, boolean voidFall) {
      return !lethal && !voidFall && Double.isFinite(damage) && damage >= 0.0 && damage <= 4.0 && damage < health;
   }

   static boolean blocksBlast(boolean fullCollision, AABB cover, Vec3 blast, Vec3 body) {
      return fullCollision && cover.clip(blast, body).isPresent();
   }

   static boolean isBackOnGround(int feetY, int groundY, boolean supported) {
      return supported && feetY <= groundY + 1;
   }

   static boolean canInterruptClear(AiCrystals.CrystalPhase phase, boolean towerRoute, boolean settling, boolean legSafe, boolean guardSafe) {
      return !isTowerPhase(phase, towerRoute) && !settling && legSafe && guardSafe;
   }

   static boolean needsReconcile(boolean settling, boolean legPending, boolean guardPending) {
      return settling || legPending || guardPending;
   }

   static boolean isCaged(Minecraft mc, BlockPos cell) {
      List<String> nearby = new ArrayList<>(32);

      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -2; dy <= 2; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               if (dx != 0 || dy != 0 || dz != 0) {
                  nearby.add(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(cell.offset(dx, dy, dz)).getBlock()).getPath());
               }
            }
         }
      }

      return hasIronBars(nearby);
   }

   static boolean hasIronBars(List<String> nearbyBlockPaths) {
      for (String path : nearbyBlockPaths) {
         if ("iron_bars".equals(path)) {
            return true;
         }
      }

      return false;
   }

   static List<AiCrystals.CrystalTarget> orderTargets(List<AiCrystals.CrystalTarget> targets) {
      List<AiCrystals.CrystalTarget> ordered = new ArrayList<>(targets);
      ordered.sort(
         Comparator.comparing(AiCrystals.CrystalTarget::caged).thenComparingDouble(AiCrystals.CrystalTarget::distanceSqr).thenComparingInt(AiCrystals.CrystalTarget::entityId)
      );
      return ordered;
   }

   static boolean hasRangedOption(int bows, int crossbows, int arrows, int tridents, int throwables) {
      return bows + crossbows > 0 && arrows > 0 || tridents > 0 || throwables > 0;
   }

   static String clearMethod(boolean caged, boolean meleeAdjacent, boolean blastSafe, boolean ranged) {
      if (caged) {
         return "cage";
      } else if (meleeAdjacent && blastSafe) {
         return "melee";
      } else if (ranged) {
         return "shoot";
      } else {
         return AiThreats.isLegacyCombatEnabled() ? null : "cage";
      }
   }

   static boolean needsApproach(double horizontal, double dy, double distance) {
      return distance > 64.0 || horizontal < Math.max(6.0, dy * 0.7);
   }

   static int[] safeDistancePoint(int cx, int cz, double dx, double dz, double dy) {
      double len = Math.hypot(dx, dz);
      double ux = len < 1.0E-6 ? 1.0 : dx / len;
      double uz = len < 1.0E-6 ? 0.0 : dz / len;
      double d = Math.max(12.0, dy + 6.0);
      return new int[]{(int)Math.round(cx + ux * d), (int)Math.round(cz + uz * d)};
   }

   static int[] dominantAxisStep(double dx, double dz) {
      return Math.abs(dx) >= Math.abs(dz) ? new int[]{dx >= 0.0 ? 1 : -1, 0} : new int[]{0, dz >= 0.0 ? 1 : -1};
   }

   static String fallRecoveryMode(List<String> hotbarItemPaths) {
      return hotbarItemPaths.contains("water_bucket") ? "mlg" : "dig";
   }

   static JsonObject rangedKitMissing() {
      JsonObject missing = new JsonObject();
      missing.addProperty("minecraft:bow", 1);
      missing.addProperty("minecraft:arrow", 8);
      return missing;
   }

   private static List<String> hotbarItemPaths(LocalPlayer player) {
      List<String> out = new ArrayList<>(9);

      for (int i = 0; i < 9; i++) {
         out.add(BuiltInRegistries.ITEM.getKey(player.getInventory().getItem(i).getItem()).getPath());
      }

      return out;
   }

   static AiOperation create(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(30, Math.min(1800, AiJson.getInt(args, "timeoutSeconds", 600)));
         final boolean skipCaged = AiJson.getBoolean(args, "skipCaged", false);
         AiService service = AiService.getExistingInstance();
         final boolean silentAim = service != null && service.getSettings().aiSilentAim;
         return new AiOperation() {
            final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final AiGuard.GuardPolicy guardPolicy = AiGuard.GuardPolicy.standard();
            final JsonArray outcomes = new JsonArray();
            final Set<Integer> handledCrystalIds = new LinkedHashSet<>();
            final AiCrystals.SkipCounters skipReasons = new AiCrystals.SkipCounters();
            AiOperation stepOp;
            AiOperation survivalOp;
            AiCrystals.CrystalTarget currentTarget;
            AiCrystals.CrystalPhase clearPhase = AiCrystals.CrystalPhase.SELECT;
            String plannedMethod = "";
            String survivalReason = "";
            String failureCode;
            JsonObject missingItems;
            BlockPos cagePillarStand;
            BlockPos cageOpening;
            BlockPos openingCell;
            boolean openingSealed;
            int clearedCount;
            int survivalInterruptions;
            int missedShots;
            int meleeMisses;
            int shotWaitTicks;
            int aimSettleTicks;
            int groundY;
            int stepOffTicks;
            int scaffoldShortage;
            int pillarLegFailures;
            int stepOffFailures;
            int remainingCrystals;
            int sealedTick;
            boolean towerRoute;
            boolean attackPending;
            boolean cancelled;
            boolean mlgActive;

            {
               this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.CALM);
               this.aim.setSilent(silentAim);
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.deadlineNanos) {
                     return this.finish(
                        this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                        AiFailure.TIMEOUT,
                        "Crystal clearing deadline reached with " + this.clearedCount + " cleared"
                     );
                  }

                  if (!AiThreats.isLegacyCombatEnabled()
                     && this.inTowerPhase()
                     && this.clearPhase != AiCrystals.CrystalPhase.DESCEND
                     && this.currentTarget != null
                     && mc.player.getHealth() < 10.0F) {
                     this.stopStep();
                     return this.skipTarget(this.currentTarget, "heal_first");
                  }

                  if (!this.inTowerPhase()) {
                     if (mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                        return this.fail(AiFailure.LOW_HEALTH, AiGuard.formatLowHealthAbort("crystal clearing", mc.player.getHealth(), mc.player.getMaxHealth()));
                     }

                     AiOperation.Result guarded = this.tickSurvivalGuard();
                     if (guarded != null) {
                        return guarded;
                     }
                  }

                  if (!AiThreats.isLegacyCombatEnabled()
                     && this.towerRoute
                     && this.currentTarget != null
                     && mc.player.getY() > this.groundY + 4
                     && (
                        this.clearPhase == AiCrystals.CrystalPhase.PILLAR
                           || this.clearPhase == AiCrystals.CrystalPhase.OPEN_CAGE
                           || this.clearPhase == AiCrystals.CrystalPhase.MELEE
                     )
                     && this.isDragonThreat(14.0, 40)) {
                     this.stopStep();
                     AiSpeedrun.log("[AI] crystals: the dragon is near; off the tower at {}", mc.player.blockPosition().toShortString());
                     return this.skipTarget(this.currentTarget, "dragon_near");
                  }

                  AiOperation.Result var10000;
                  switch (this.clearPhase) {
                     case SELECT:
                        var10000 = this.tickSelectPhase();
                        break;
                     case APPROACH:
                        var10000 = this.tickApproachPhase();
                        break;
                     case SHOOT:
                        var10000 = this.tickShootPhase();
                        break;
                     case MELEE:
                        var10000 = this.tickMeleePhase();
                        break;
                     case PILLAR:
                        var10000 = this.tickPillarPhase();
                        break;
                     case OPEN_CAGE:
                        var10000 = this.tickOpenCagePhase();
                        break;
                     case DESCEND:
                        var10000 = this.tickDescendPhase();
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  return var10000;
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, AiFailure.CANCELLED, "Crystal clearing cancelled");
               }
            }

            boolean inTowerPhase() {
               return AiCrystals.isTowerPhase(this.clearPhase, this.towerRoute && this.currentTarget != null);
            }

            AiOperation.Result tickSurvivalGuard() {
               if (this.survivalOp != null) {
                  AiOperation.Result result = this.survivalOp.tick();
                  if (!result.terminal()) {
                     return this.running("Interrupted: " + this.survivalReason + " - " + result.message());
                  }

                  this.survivalOp.cancel();
                  this.survivalOp = null;
                  return this.running("Resuming after " + this.survivalReason);
               } else {
                  AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                  AiOperation action = AiGuard.createResponseOperation(this.guardPolicy, vitals, this.secondsLeft());
                  if (action == null) {
                     return null;
                  }

                  this.survivalReason = AiGuard.decisionReason(this.guardPolicy, vitals);
                  this.survivalInterruptions++;
                  this.stopStep();
                  this.survivalOp = action;
                  return this.running("Interrupted: " + this.survivalReason);
               }
            }

            AiOperation.Result tickSelectPhase() {
               List<AiCrystals.CrystalTarget> targets = AiCrystals.orderTargets(this.scanCrystals());
               this.remainingCrystals = targets.size();
               AiCrystals.CrystalTarget next = null;

               for (AiCrystals.CrystalTarget t : targets) {
                  if (!this.handledCrystalIds.contains(t.entityId())) {
                     next = t;
                     break;
                  }
               }

               if (next == null) {
                  if (targets.isEmpty()) {
                     if (this.clearedCount > 0) {
                        AiRunTrace.recordGlobalSplit("clear_crystals", this.clearedCount + " cleared");
                     }

                     return this.finish(
                        AiOperation.Status.SUCCEEDED, null, this.clearedCount == 0 ? "No End crystals observed" : "All End crystals cleared (" + this.clearedCount + ")"
                     );
                  } else {
                     return this.finishNoneReachable();
                  }
               } else {
                  this.currentTarget = next;
                  this.plannedMethod = "";
                  this.towerRoute = false;
                  this.attackPending = false;
                  this.missedShots = 0;
                  this.meleeMisses = 0;
                  this.shotWaitTicks = 0;
                  this.aimSettleTicks = 0;
                  this.pillarLegFailures = 0;
                  this.stepOffFailures = 0;
                  this.cagePillarStand = null;
                  this.cageOpening = null;
                  this.openingCell = null;
                  this.openingSealed = false;
                  this.sealedTick = -1;
                  Entity entity = this.liveEntityFor(next);
                  if (entity == null) {
                     this.handledCrystalIds.add(next.entityId());
                     return this.running("Crystal vanished; re-selecting");
                  }

                  String chosen = AiCrystals.clearMethod(next.caged(), this.isInMeleeReach(entity), this.isBlastSafe(), this.hasRanged());
                  AiSpeedrun.log(
                     "[AI] crystals: target #{} at {} {} {} caged={} invulnerable={} method={} arrows={}",
                     next.entityId(),
                     next.x(),
                     next.y(),
                     next.z(),
                     next.caged(),
                     entity.isInvulnerable(),
                     chosen,
                     AiRunResources.totalArrowCount()
                  );
                  if (chosen == null) {
                     return this.skipTarget(next, "shoot", "no_ranged");
                  }

                  if ("cage".equals(chosen) && skipCaged) {
                     return this.skipTarget(next, "cage", "caged_skipped");
                  }

                  if ("cage".equals(chosen) && !AiThreats.isLegacyCombatEnabled() && mc.player.getHealth() < 16.0F) {
                     return this.skipTarget(next, "cage", "heal_first");
                  }

                  if ("cage".equals(chosen) && !AiThreats.isLegacyCombatEnabled() && this.isDragonThreat(24.0, 60)) {
                     return this.skipTarget(next, "cage", "dragon_near");
                  }

                  if ("cage".equals(chosen)) {
                     this.towerRoute = true;
                     this.groundY = mc.player.blockPosition().getY();
                     int shortage = next.y() + 2 - this.groundY - this.scaffoldBlockCount();
                     if (shortage > 0) {
                        this.scaffoldShortage = Math.max(this.scaffoldShortage, shortage);
                        return this.skipTarget(next, "cage", "no_blocks");
                     } else {
                        this.clearPhase = AiCrystals.CrystalPhase.PILLAR;
                        return this.running("Pillaring up beside the cage at " + next.x() + " " + next.y() + " " + next.z());
                     }
                  } else {
                     this.plannedMethod = chosen;
                     if ("melee".equals(chosen)) {
                        this.clearPhase = AiCrystals.CrystalPhase.MELEE;
                        return this.running("Punching the adjacent crystal");
                     } else {
                        this.clearPhase = this.needsApproachTo(entity) ? AiCrystals.CrystalPhase.APPROACH : AiCrystals.CrystalPhase.SHOOT;
                        return this.running("Shooting crystal #" + next.entityId());
                     }
                  }
               }
            }

            AiOperation.Result tickApproachPhase() {
               if (this.currentTarget == null) {
                  this.clearPhase = AiCrystals.CrystalPhase.SELECT;
                  return this.running("Re-selecting");
               }

               Entity entity = this.liveEntityFor(this.currentTarget);
               if (entity == null) {
                  return this.recordClearedTarget();
               }

               if (this.stepOp == null) {
                  BlockPos stand = this.approachStandFor(entity);
                  if (stand == null) {
                     this.clearPhase = AiCrystals.CrystalPhase.SHOOT;
                     return this.running("No firing cell in loaded terrain; shooting from here");
                  }

                  this.cagePillarStand = stand;
                  this.stepOp = AiGoto.travelToCompat(
                     stand, 1.5, false, true, true, this.legSeconds(120), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true
                  );
               }

               AiOperation.Result r = this.stepOp.tick();
               if (!r.terminal()) {
                  return this.running("Moving to a firing position");
               } else {
                  this.stopStep();
                  if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.clearPhase = AiCrystals.CrystalPhase.SHOOT;
                     return this.running("In position; solving the arc");
                  } else {
                     return this.failTarget(this.currentTarget, "shoot", "unreachable");
                  }
               }
            }

            AiOperation.Result tickShootPhase() {
               if (this.currentTarget == null) {
                  this.clearPhase = AiCrystals.CrystalPhase.SELECT;
                  return this.running("Re-selecting");
               }

               if (this.liveEntityFor(this.currentTarget) == null) {
                  return this.recordClearedTarget();
               }

               if (this.attackPending) {
                  if (++this.shotWaitTicks > 40) {
                     this.attackPending = false;
                     return ++this.missedShots >= 3
                        ? this.failTarget(this.currentTarget, this.plannedMethod, "shots_missed")
                        : this.running("Shot did not land; firing again (" + this.missedShots + "/3)");
                  } else {
                     return this.running("Waiting for the shot to land");
                  }
               } else {
                  if (this.stepOp == null) {
                     this.stepOp = AiRanged.create(
                        AiJson.objectOf("entityId", this.currentTarget.entityId(), "count", 1, "timeoutSeconds", Math.min(8, this.legSeconds(45)))
                     );
                  }

                  AiOperation.Result r = this.stepOp.tick();
                  if (!r.terminal()) {
                     return this.running("Shooting crystal #" + this.currentTarget.entityId() + ": " + r.message());
                  } else {
                     AiSpeedrun.log("[AI] crystals: shot #{} {} - {}", this.currentTarget.entityId(), r.status(), r.message());
                     this.stopStep();
                     if (this.liveEntityFor(this.currentTarget) == null) {
                        return this.recordClearedTarget();
                     } else if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.finish(AiOperation.Status.CANCELLED, AiFailure.CANCELLED, "Crystal clearing cancelled");
                     } else if (r.status() == AiOperation.Status.SUCCEEDED || r.status() == AiOperation.Status.UNCERTAIN) {
                        this.attackPending = true;
                        this.shotWaitTicks = 0;
                        return this.running("Shot away");
                     } else if (r.dataJson() != null && r.dataJson().contains("\"missing\"")) {
                        return this.towerRoute ? this.skipTarget(this.currentTarget, "no_ranged") : this.skipTarget(this.currentTarget, "shoot", "no_ranged");
                     } else {
                        return ++this.missedShots >= 3
                           ? this.failTarget(this.currentTarget, this.plannedMethod, "shots_missed")
                           : this.running("Shot failed (" + r.message() + "); retrying");
                     }
                  }
               }
            }

            AiOperation.Result tickMeleePhase() {
               if (this.currentTarget == null) {
                  this.clearPhase = AiCrystals.CrystalPhase.SELECT;
                  return this.running("Re-selecting");
               }

               Entity entity = this.liveEntityFor(this.currentTarget);
               if (entity == null) {
                  return this.recordClearedTarget();
               }

               if (this.attackPending) {
                  if (++this.shotWaitTicks > 40) {
                     this.attackPending = false;
                     return ++this.meleeMisses >= 3
                        ? this.failTarget(this.currentTarget, this.plannedMethod, "melee_failed")
                        : this.running("The swing did not register; re-swinging");
                  } else {
                     return this.running("Waiting for the pop");
                  }
               } else if (!this.isInMeleeReach(entity)) {
                  if (this.towerRoute) {
                     return this.failTarget(this.currentTarget, this.plannedMethod, "unreachable");
                  } else if (this.hasRanged()) {
                     this.plannedMethod = "shoot";
                     this.clearPhase = AiCrystals.CrystalPhase.APPROACH;
                     return this.running("No longer adjacent; switching to the bow");
                  } else {
                     return this.skipTarget(this.currentTarget, "melee", "no_ranged");
                  }
               } else if (!this.isBlastSafe() || !AiThreats.isLegacyCombatEnabled() && this.towerRoute && !this.isShielded(entity)) {
                  if (!this.hasRanged() || this.towerRoute && !AiThreats.isLegacyCombatEnabled()) {
                     return this.towerRoute ? this.skipTarget(this.currentTarget, "lethal_blast") : this.skipTarget(this.currentTarget, "melee", "lethal_blast");
                  }

                  this.plannedMethod = this.towerRoute ? "cage_shoot" : "shoot";
                  this.clearPhase = this.towerRoute ? AiCrystals.CrystalPhase.SHOOT : AiCrystals.CrystalPhase.APPROACH;
                  return this.running("Blast would be lethal point-blank; switching to the bow");
               } else {
                  this.aim.aimAt(autismclient.ai.engine.AiAim.defaultEntityAimPoint(entity));
                  this.aim.tick();
                  if (!this.aim.isSettled() && ++this.aimSettleTicks < 25) {
                     return this.running("Aiming at the crystal");
                  }

                  mc.gameMode.attack(mc.player, entity);
                  Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                  mc.player.resetAttackStrengthTicker();
                  this.attackPending = true;
                  this.shotWaitTicks = 0;
                  return this.running("Struck the crystal");
               }
            }

            AiOperation.Result tickPillarPhase() {
               if (this.currentTarget == null) {
                  this.clearPhase = AiCrystals.CrystalPhase.SELECT;
                  return this.running("Re-selecting");
               }

               Entity entity = this.liveEntityFor(this.currentTarget);
               if (entity == null) {
                  return this.recordClearedAs("cage");
               }

               if (this.stepOp == null) {
                  int[] side = AiCrystals.dominantAxisStep(mc.player.getX() - (this.currentTarget.x() + 0.5), mc.player.getZ() - (this.currentTarget.z() + 0.5));
                  this.cagePillarStand = new BlockPos(this.currentTarget.x() + side[0] * 3, this.currentTarget.y(), this.currentTarget.z() + side[1] * 3);
                  this.cageOpening = new BlockPos(this.currentTarget.x() + side[0] * 2, this.currentTarget.y(), this.currentTarget.z() + side[1] * 2);
                  this.stepOp = AiGoto.travelToCompat(
                     this.cagePillarStand, 0.7, false, true, true, this.legSeconds(120), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true
                  );
               }

               AiOperation.Result r = this.stepOp.tick();
               if (!r.terminal()) {
                  return this.running("Pillaring up beside the cage");
               } else {
                  this.stopStep();
                  if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.clearPhase = AiCrystals.CrystalPhase.OPEN_CAGE;
                     return this.running("Beside the cage; opening the bars");
                  } else {
                     return ++this.pillarLegFailures >= 2
                        ? this.failTarget(this.currentTarget, "cage", "unreachable")
                        : this.running("Pillar leg failed (" + r.message() + "); retrying");
                  }
               }
            }

            AiOperation.Result tickOpenCagePhase() {
               if (this.currentTarget == null) {
                  this.clearPhase = AiCrystals.CrystalPhase.SELECT;
                  return this.running("Re-selecting");
               }

               Entity entity = this.liveEntityFor(this.currentTarget);
               if (entity == null) {
                  return this.recordClearedAs("cage");
               }

               if (this.cageOpening != null && this.openingCell == null) {
                  this.openingCell = this.cageOpening;
               }

               if (this.cageOpening != null && !this.isIronBarsAt(this.cageOpening)) {
                  this.cageOpening = null;
               }

               if (this.cageOpening != null) {
                  if (this.stepOp == null) {
                     this.stepOp = AiGameOperations.blockAction(
                        "break",
                        AiJson.objectOf(
                           "x",
                           this.cageOpening.getX(),
                           "y",
                           this.cageOpening.getY(),
                           "z",
                           this.cageOpening.getZ(),
                           "timeoutSeconds",
                           this.legSeconds(30)
                        )
                     );
                  }

                  AiOperation.Result r = this.stepOp.tick();
                  if (!r.terminal()) {
                     return this.running("Breaking the iron bars");
                  }

                  this.stopStep();
                  this.cageOpening = null;
               }

               if (!AiThreats.isLegacyCombatEnabled() && this.openingCell != null) {
                  BlockPos eyeBar = this.openingCell.above();
                  if (this.isIronBarsAt(eyeBar)) {
                     if (this.stepOp == null) {
                        this.stepOp = AiGameOperations.blockAction(
                           "break",
                           AiJson.objectOf("x", eyeBar.getX(), "y", eyeBar.getY(), "z", eyeBar.getZ(), "timeoutSeconds", this.legSeconds(30))
                        );
                     }

                     AiOperation.Result r = this.stepOp.tick();
                     if (!r.terminal()) {
                        return this.running("Breaking the bars at eye level");
                     }

                     this.stopStep();
                     if (this.isIronBarsAt(eyeBar)) {
                        this.openingCell = null;
                     }
                  }

                  if (this.openingCell != null && !this.openingSealed && mc.level.getBlockState(this.openingCell).canBeReplaced()) {
                     if (this.stepOp == null) {
                        String block = this.bestScaffoldBlock();
                        if (block != null && AiItems.equipHotbar(AiItems.resolveItem(block)) >= 0) {
                           this.stepOp = AiGameOperations.blockAction(
                              "place",
                              AiJson.objectOf(
                                 "x", this.openingCell.getX(), "y", this.openingCell.getY() - 1, "z", this.openingCell.getZ(), "timeoutSeconds", 6
                              )
                           );
                        } else {
                           this.openingSealed = true;
                           this.sealedTick = mc.player.tickCount;
                        }
                     }

                     if (this.stepOp != null) {
                        AiOperation.Result r = this.stepOp.tick();
                        if (!r.terminal()) {
                           return this.running("Setting a block in the opening");
                        }

                        this.stopStep();
                        this.openingSealed = true;
                        this.sealedTick = mc.player.tickCount;
                     }
                  }

                  if (this.openingSealed && mc.player.tickCount - this.sealedTick < 2) {
                     return this.running("Verifying crystal cover and blast damage");
                  }

                  if (this.isShielded(entity) && this.isBlastSafe()) {
                     this.plannedMethod = "cage_melee";
                     this.clearPhase = AiCrystals.CrystalPhase.MELEE;
                     return this.running("Cover set; striking the crystal over it");
                  }
               }

               if (!this.isBlastSafe() || !AiThreats.isLegacyCombatEnabled() && this.towerRoute && !this.isShielded(entity)) {
                  if (AiThreats.isLegacyCombatEnabled() && this.hasRanged()) {
                     this.plannedMethod = "cage_shoot";
                     this.clearPhase = AiCrystals.CrystalPhase.SHOOT;
                     return this.running("Blast lethal point-blank; shooting through the opening");
                  } else {
                     return this.skipTarget(this.currentTarget, "lethal_blast");
                  }
               } else {
                  this.plannedMethod = "cage_melee";
                  this.clearPhase = AiCrystals.CrystalPhase.MELEE;
                  return this.running("Bars open; punching the crystal");
               }
            }

            AiOperation.Result tickDescendPhase() {
               BlockPos feet = mc.player.blockPosition();
               if (this.mlgActive) {
                  return this.tickStepOff(feet);
               } else if (this.isBackOnGroundNow()) {
                  return this.finishDescent();
               } else {
                  return this.stepOffFailures < 2 && ("mlg".equals(AiCrystals.fallRecoveryMode(AiCrystals.hotbarItemPaths(mc.player))) || !AiThreats.isLegacyCombatEnabled() && AiGameState.countInventoryItem("minecraft:water_bucket") > 0)
                     ? this.tickStepOff(feet)
                     : this.tickBreakDown(feet);
               }
            }

            AiOperation.Result tickStepOff(BlockPos feet) {
               if (this.stepOp == null) {
                  if (mc.player.onGround() && feet.getY() > this.groundY + 1) {
                     if (++this.stepOffTicks > 40) {
                        this.stepOffFailures++;
                        this.stopStep();
                        return this.running("Could not step off cleanly; digging down instead");
                     }

                     this.aim.aimAt(this.stepOffAimPoint());
                     this.aim.tick();
                     AiFallGuard.stampTick();
                     mc.options.keyUp.setDown(true);
                     return this.running("Stepping off for the water clutch");
                  }

                  mc.options.keyUp.setDown(false);
                  this.stepOp = AiMlg.create(AiJson.objectOf("method", "auto", "collect", true, "timeoutSeconds", 15));
                  this.mlgActive = true;
               }

               AiOperation.Result r = this.stepOp.tick();
               if (!r.terminal()) {
                  return this.running("Falling with the water clutch");
               }

               mc.options.keyUp.setDown(false);
               this.stopStep();
               if (r.status() == AiOperation.Status.SUCCEEDED) {
                  return this.isBackOnGroundNow() ? this.finishDescent() : this.running("Landed short; continuing down");
               }

               this.stepOffFailures++;
               return this.running("Clutch unavailable (" + r.message() + "); digging down instead");
            }

            AiOperation.Result tickBreakDown(BlockPos feet) {
               if (!mc.player.onGround()) {
                  return this.running("Stepping down the tower");
               }

               BlockPos below = feet.below();
               if (mc.level.getBlockState(below).isAir()) {
                  return this.running("Stepping down the tower");
               }

               String path = BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(below).getBlock()).getPath();
               if (!"obsidian".equals(path) && !"bedrock".equals(path)) {
                  if (this.stepOp == null) {
                     this.stepOp = autismclient.ai.engine.AiBreakBlock.breakBlock(below, 20);
                  }

                  AiOperation.Result r = this.stepOp.tick();
                  if (!r.terminal()) {
                     return this.running("Digging down the tower");
                  }

                  this.stopStep();
                  return r.status() == AiOperation.Status.SUCCEEDED
                     ? this.running("Digging down the tower")
                     : this.finish(AiOperation.Status.UNCERTAIN, r.failure(), "Tower descent failed before safe ground: " + r.message());
               } else {
                  return this.finish(
                     AiOperation.Status.UNCERTAIN, AiFailure.ENVIRONMENTAL_HAZARD, "Clutch unavailable and the remaining tower cannot be dug down; safe ground is not reached"
                  );
               }
            }

            boolean isBackOnGroundNow() {
               return AiCrystals.isBackOnGround(mc.player.blockPosition().getY(), this.groundY, mc.player.onGround() || mc.player.isInWater() || mc.player.onClimbable());
            }

            AiOperation.Result finishDescent() {
               this.stopStep();
               this.currentTarget = null;
               this.attackPending = false;
               this.clearPhase = AiCrystals.CrystalPhase.SELECT;
               return this.running("Back on the ground");
            }

            AiOperation.Result recordClearedTarget() {
               AiCrystals.CrystalTarget t = this.currentTarget;
               this.attackPending = false;
               this.outcomes
                  .add(
                     AiJson.objectOf(
                        "id", t.entityId(), "caged", t.caged(), "method", this.plannedMethod.isEmpty() ? "shoot" : this.plannedMethod, "result", "cleared"
                     )
                  );
               this.clearedCount++;
               AiRunTrace.recordGlobalSplit("crystal", (this.plannedMethod.isEmpty() ? "shoot" : this.plannedMethod) + " #" + t.entityId());
               if (this.towerRoute && !this.isBackOnGroundNow()) {
                  this.clearPhase = AiCrystals.CrystalPhase.DESCEND;
                  this.stepOffTicks = 0;
                  return this.running("Crystal cleared; getting down");
               } else {
                  this.currentTarget = null;
                  this.clearPhase = AiCrystals.CrystalPhase.SELECT;
                  return this.running("Crystal cleared (" + this.clearedCount + ")");
               }
            }

            AiOperation.Result recordClearedAs(String planned) {
               AiCrystals.CrystalTarget t = this.currentTarget;
               this.attackPending = false;
               this.outcomes.add(AiJson.objectOf("id", t.entityId(), "caged", t.caged(), "method", planned, "result", "cleared"));
               this.clearedCount++;
               if (!this.isBackOnGroundNow()) {
                  this.clearPhase = AiCrystals.CrystalPhase.DESCEND;
                  this.stepOffTicks = 0;
                  return this.running("Crystal gone; getting down");
               } else {
                  this.currentTarget = null;
                  this.clearPhase = AiCrystals.CrystalPhase.SELECT;
                  return this.running("Crystal gone; re-selecting");
               }
            }

            AiOperation.Result skipTarget(AiCrystals.CrystalTarget t, String planned, String reason) {
               this.stopStep();
               this.handledCrystalIds.add(t.entityId());
               this.outcomes.add(AiJson.objectOf("id", t.entityId(), "caged", t.caged(), "method", planned, "result", "skipped:" + reason));
               this.skipReasons.countSkip(reason);
               this.currentTarget = null;
               this.clearPhase = AiCrystals.CrystalPhase.SELECT;
               return this.running("Crystal #" + t.entityId() + " skipped: " + reason);
            }

            AiOperation.Result skipTarget(AiCrystals.CrystalTarget t, String reason) {
               this.handledCrystalIds.add(t.entityId());
               this.outcomes
                  .add(
                     AiJson.objectOf(
                        "id", t.entityId(), "caged", t.caged(), "method", this.plannedMethod.isEmpty() ? "cage" : this.plannedMethod, "result", "skipped:" + reason
                     )
                  );
               this.skipReasons.countSkip(reason);
               this.clearPhase = AiCrystals.CrystalPhase.DESCEND;
               this.stepOffTicks = 0;
               return this.running("Crystal #" + t.entityId() + " skipped: " + reason + "; getting down");
            }

            AiOperation.Result failTarget(AiCrystals.CrystalTarget t, String planned, String reason) {
               this.stopStep();
               this.handledCrystalIds.add(t.entityId());
               this.outcomes.add(AiJson.objectOf("id", t.entityId(), "caged", t.caged(), "method", planned, "result", "failed:" + reason));
               if (this.towerRoute && mc.player.blockPosition().getY() > this.groundY + 1) {
                  this.clearPhase = AiCrystals.CrystalPhase.DESCEND;
                  this.stepOffTicks = 0;
                  return this.running("Crystal #" + t.entityId() + " failed: " + reason + "; getting down");
               } else {
                  this.currentTarget = null;
                  this.clearPhase = AiCrystals.CrystalPhase.SELECT;
                  return this.running("Crystal #" + t.entityId() + " failed: " + reason);
               }
            }

            AiOperation.Result finishNoneReachable() {
               String suffix = this.clearedCount > 0 ? " after clearing " + this.clearedCount : "";
               if (this.skipReasons.noRangedSkips > 0) {
                  this.failureCode = "NO_RANGED";
                  this.missingItems = AiCrystals.rangedKitMissing();
                  return this.fail(AiFailure.NO_RANGED, "No ranged weapon or arrows for the remaining uncaged crystals" + suffix);
               } else if (this.skipReasons.noBlocksSkips > 0) {
                  this.missingItems = AiJson.objectOf("minecraft:dirt", Math.max(1, this.scaffoldShortage));
                  return this.fail(AiFailure.MISSING_MATERIALS, "No throwaway construction blocks for pillaring to the caged crystals" + suffix);
               } else {
                  return this.fail(AiFailure.EXECUTION_FAILED, this.skipReasons.unreachableSummary(this.clearedCount));
               }
            }

            List<AiCrystals.CrystalTarget> scanCrystals() {
               List<AiCrystals.CrystalTarget> out = new ArrayList<>();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof EndCrystal && !entity.isRemoved()) {
                     BlockPos cell = entity.blockPosition();
                     out.add(
                        new AiCrystals.CrystalTarget(
                           entity.getId(), cell.getX(), cell.getY(), cell.getZ(), mc.player.distanceToSqr(entity), this.isCaged(cell)
                        )
                     );
                  }
               }

               return out;
            }

            boolean isCaged(BlockPos cell) {
               return AiCrystals.isCaged(mc, cell);
            }

            boolean hasIronBarsNear(BlockPos cell) {
               List<String> nearby = new ArrayList<>(32);

               for (int dx = -2; dx <= 2; dx++) {
                  for (int dy = -2; dy <= 2; dy++) {
                     for (int dz = -2; dz <= 2; dz++) {
                        if (dx != 0 || dy != 0 || dz != 0) {
                           nearby.add(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(cell.offset(dx, dy, dz)).getBlock()).getPath());
                        }
                     }
                  }
               }

               return AiCrystals.hasIronBars(nearby);
            }

            boolean isIronBarsAt(BlockPos pos) {
               return "iron_bars".equals(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(pos).getBlock()).getPath());
            }

            Entity liveEntityFor(AiCrystals.CrystalTarget target) {
               Entity entity = mc.level.getEntity(target.entityId());
               return entity != null && !entity.isRemoved() ? entity : null;
            }

            boolean isInMeleeReach(Entity entity) {
               double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)) - 0.1;
               return entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= reach * reach;
            }

            boolean isDragonFlyingWithin(double distance) {
               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof EnderDragon dragon && dragon.isAlive()) {
                     DragonPhaseInstance phase = dragon.getPhaseManager().getCurrentPhase();
                     if (!phase.isSitting() && phase.getPhase() != EnderDragonPhase.LANDING && dragon.distanceTo(mc.player) < distance) {
                        return true;
                     }
                  }
               }

               return false;
            }

            boolean isDragonThreat(double within, int ticks) {
               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof EnderDragon dragon && dragon.isAlive()) {
                     DragonPhaseInstance phase = dragon.getPhaseManager().getCurrentPhase();
                     if (!phase.isSitting() && phase.getPhase() != EnderDragonPhase.LANDING) {
                        EnderDragonPhase<?> kind = phase.getPhase();
                        double d = dragon.distanceTo(mc.player);
                        if ((kind == EnderDragonPhase.CHARGING_PLAYER || kind == EnderDragonPhase.STRAFE_PLAYER) && d < 96.0) {
                           return true;
                        }

                        if (d < within) {
                           return true;
                        }

                        double vx = dragon.getX() - dragon.xo;
                        double vy = dragon.getY() - dragon.yo;
                        double vz = dragon.getZ() - dragon.zo;

                        for (int t = 5; t <= ticks; t += 5) {
                           double x = dragon.getX() + vx * t - mc.player.getX();
                           double y = dragon.getY() + vy * t - mc.player.getY();
                           double z = dragon.getZ() + vz * t - mc.player.getZ();
                           if (x * x + y * y + z * z < within * within) {
                              return true;
                           }
                        }
                     }
                  }
               }

               return false;
            }

            boolean isBlastSafe() {
               if (AiThreats.isLegacyCombatEnabled()) {
                  return !AiDanger.assess(mc).lethal();
               }

               AiDanger.Assessment danger = AiDanger.assess(mc);
               Entity crystal = this.currentTarget == null ? null : this.liveEntityFor(this.currentTarget);
               if (crystal == null) {
                  return false;
               }

               double damage = autismclient.util.ExplosionDamageCalculator.calculateDamage(
                  mc.player,
                  crystal.position(),
                  6.0F,
                  autismclient.util.ExplosionDamageCalculator.SimOptions.DEFAULT.withDamageSource(Explosion.getDefaultDamageSource(mc.level, crystal))
               );
               return AiCrystals.isTolerableBlastDamage(damage, mc.player.getHealth() + mc.player.getAbsorptionAmount(), danger.lethal(), danger.voidFall())
                  && danger.worstSource() <= 4.0F;
            }

            boolean isShielded(Entity crystal) {
               if (this.openingSealed && this.openingCell != null && crystal != null) {
                  BlockState state = mc.level.getBlockState(this.openingCell);
                  boolean solid = state.isCollisionShapeFullBlock(mc.level, this.openingCell);
                  Vec3 body = mc.player.position().add(0.0, 0.4, 0.0);
                  return state.getCollisionShape(mc.level, this.openingCell)
                     .toAabbs()
                     .stream()
                     .anyMatch(box -> AiCrystals.blocksBlast(solid, box.move(this.openingCell), crystal.position(), body));
               } else {
                  return false;
               }
            }

            boolean hasRanged() {
               return AiCrystals.hasRangedOption(
                  AiGameState.countInventoryItem("minecraft:bow"),
                  AiGameState.countInventoryItem("minecraft:crossbow"),
                  AiRunResources.totalArrowCount(),
                  AiGameState.countInventoryItem("minecraft:trident"),
                  AiGameState.countInventoryItem("minecraft:snowball") + AiGameState.countInventoryItem("minecraft:egg")
               );
            }

            boolean needsApproachTo(Entity entity) {
               double dx = entity.getX() - mc.player.getX();
               double dz = entity.getZ() - mc.player.getZ();
               double horizontal = Math.hypot(dx, dz);
               double dy = entity.getY() - mc.player.getEyePosition().y;
               return AiCrystals.needsApproach(horizontal, dy, Math.sqrt(horizontal * horizontal + dy * dy));
            }

            BlockPos approachStandFor(Entity entity) {
               double dx = mc.player.getX() - entity.getX();
               double dz = mc.player.getZ() - entity.getZ();
               double dy = Math.max(0.0, entity.getY() - mc.player.getEyePosition().y);
               int[] xz = AiCrystals.safeDistancePoint(this.currentTarget.x(), this.currentTarget.z(), dx, dz, dy);

               for (int y = this.currentTarget.y(); y > mc.level.getMinY(); y--) {
                  BlockPos p = new BlockPos(xz[0], y, xz[1]);
                  if (!mc.level.getBlockState(p).isAir()) {
                     return p.above();
                  }
               }

               return null;
            }

            String bestScaffoldBlock() {
               String best = null;
               int bestRank = 2147483647;

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty()) {
                     String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                     int rank = autismclient.ai.engine.AiRestock.scaffoldRank(id);
                     if (rank >= 0 && rank < bestRank) {
                        bestRank = rank;
                        best = id;
                     }
                  }
               }

               return best;
            }

            int scaffoldBlockCount() {
               int total = 0;

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && autismclient.ai.engine.AiRestock.isScaffoldBlockId(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                     total += stack.getCount();
                  }
               }

               return total + AiPlanks.plankYield(mc.player.getInventory());
            }

            Vec3 stepOffAimPoint() {
               double cx = this.currentTarget == null ? mc.player.getX() : this.currentTarget.x() + 0.5;
               double cz = this.currentTarget == null ? mc.player.getZ() : this.currentTarget.z() + 0.5;
               double dx = mc.player.getX() - cx;
               double dz = mc.player.getZ() - cz;
               double len = Math.hypot(dx, dz);
               if (len < 1.0E-6) {
                  dx = 1.0;
                  dz = 0.0;
                  len = 1.0;
               }

               return new Vec3(mc.player.getX() + dx / len * 2.0, mc.player.getEyePosition().y, mc.player.getZ() + dz / len * 2.0);
            }

            int legSeconds(int cap) {
               return Math.max(10, Math.min(cap, this.secondsLeft()));
            }

            int secondsLeft() {
               return (int)Math.max(1L, (this.deadlineNanos - System.nanoTime()) / 1000000000L);
            }

            void stopStep() {
               if (this.stepOp != null) {
                  this.stepOp.cancel();
                  this.stepOp = null;
               }

               this.mlgActive = false;
               mc.options.keyUp.setDown(false);
            }

            JsonObject statusData() {
               JsonObject out = AiJson.objectOf(
                  "cleared",
                  this.clearedCount,
                  "remaining",
                  this.remainingCrystals,
                  "skipped",
                  this.handledCrystalIds.size(),
                  "phase",
                  this.clearPhase.name().toLowerCase(Locale.ROOT),
                  "towerCommitted",
                  this.inTowerPhase()
               );
               out.add("outcomes", this.outcomes);
               if (this.currentTarget != null) {
                  out.add(
                     "current",
                     AiJson.objectOf(
                        "id",
                        this.currentTarget.entityId(),
                        "caged",
                        this.currentTarget.caged(),
                        "method",
                        this.plannedMethod,
                        "x",
                        this.currentTarget.x(),
                        "y",
                        this.currentTarget.y(),
                        "z",
                        this.currentTarget.z()
                     )
                  );
               }

               out.add("survival", AiGuard.statusToJson(AiGuard.sampleStatus(), this.survivalInterruptions, this.survivalReason));
               if (this.missingItems != null) {
                  out.add("missing", this.missingItems);
               }

               if (this.failureCode != null) {
                  out.addProperty("code", this.failureCode);
               }

               return out;
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.statusData());
            }

            AiOperation.Result finish(AiOperation.Status status, AiFailure failure, String message) {
               JsonObject data = this.statusData();
               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of(), failure);
            }

            AiOperation.Result fail(AiFailure failure, String message) {
               return this.finish(AiOperation.Status.FAILED, failure, message);
            }

            @Override
            public boolean canInterruptNow() {
               return AiCrystals.canInterruptClear(
                  this.clearPhase,
                  this.towerRoute && this.currentTarget != null,
                  this.attackPending,
                  this.stepOp == null || this.stepOp.canInterruptNow(),
                  this.survivalOp == null || this.survivalOp.canInterruptNow()
               );
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return AiCrystals.needsReconcile(
                  this.attackPending,
                  this.stepOp != null && this.stepOp.requiresReconciliationAfterEmergency(),
                  this.survivalOp != null && this.survivalOp.requiresReconciliationAfterEmergency()
               );
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  this.stopStep();
                  if (this.survivalOp != null) {
                     this.survivalOp.cancel();
                     this.survivalOp = null;
                  }

                  this.aim.clear();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Crystal clearing requires a connected living player");
      }
   }

   static final class SkipCounters {
      int noRangedSkips;
      int noBlocksSkips;
      int dragonNearSkips;

      void countSkip(String reason) {
         if ("no_ranged".equals(reason)) {
            this.noRangedSkips++;
         }

         if ("no_blocks".equals(reason)) {
            this.noBlocksSkips++;
         }

         if ("dragon_near".equals(reason)) {
            this.dragonNearSkips++;
         }
      }

      String unreachableSummary(int cleared) {
         return "Every remaining crystal is unreachable; see outcomes"
            + (cleared > 0 ? " after clearing " + cleared : "")
            + (this.dragonNearSkips > 0 ? " (dragon near the towers)" : "");
      }
   }

   record CrystalTarget(int entityId, int x, int y, int z, double distanceSqr, boolean caged) {
   }

   enum CrystalPhase {
      SELECT,
      APPROACH,
      SHOOT,
      MELEE,
      PILLAR,
      OPEN_CAGE,
      DESCEND;
   }
}
