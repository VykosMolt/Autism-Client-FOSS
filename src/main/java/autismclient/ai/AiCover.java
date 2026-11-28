package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

final class AiCover {
   static final int HIDDEN_TICKS_TO_SUCCEED = 80;
   static final int STANCE_HORIZONTAL_RADIUS = 7;
   static final int read_I = 3;
   static final int STANCE_MAX_DROP = 12;
   static final int MAX_STANCES = 48;
   static final double FIGHT_RANGE = 4.5;
   static final int sync_I = 6;
   static final double REPLAN_SHOOTER_MOVE = 2.5;
   static final double read_D = 3.0;
   static final double write_D = 3.0;
   static final double CREEPER_KEEP_AWAY = 8.0;
   static final double sync_D = 6.0;
   private static int lastCoverTick = -1073741824;

   private AiCover() {
   }

   static boolean isTakingCover(Minecraft mc) {
      return mc.player != null && mc.player.tickCount - lastCoverTick <= 1;
   }

   static AiOperation create(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(10, Math.min(120, AiJson.getInt(args, "timeoutSeconds", 45)));
         final float healTo = (float)AiJson.getDouble(args, "healTo", 0.0);
         final boolean sealOnly = AiJson.getBoolean(args, "sealOnly", false);
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final Object level = mc.level;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            int offTargetTicks;
            final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer().fast();
            final Set<Long> rejectedStances = new HashSet<>();
            final Set<Integer> seenShooters = new HashSet<>();
            AiGoto.GotoOp coverRoute;
            Vec3 plannedFromEye;
            BlockPos startPos;
            int hiddenTicks;
            int routeFailures;
            int shieldsPlaced;
            int coverPlans;
            int retryCooldown;
            int healTicks;
            int eatAttempts;
            AiOperation eatOp;
            boolean placingShield;
            boolean shieldColumnActive;
            boolean jumpHeld;
            boolean cancelled;
            String shooterName = "shooter";
            Vec3 creeperSightedAt;
            boolean backingOff;

            {
               this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.FAST);
            }

            @Override
            public AiOperation.Result tick() {
               if (mc.player != null) {
                  AiCover.lastCoverTick = mc.player.tickCount;
               }

               if (this.cancelled || mc.player == null || mc.level == null) {
                  return this.finish(AiOperation.Status.CANCELLED, "Cover cancelled");
               }

               if (mc.level != this.level) {
                  return this.finish(AiOperation.Status.FAILED, "World changed while taking cover");
               }

               if (System.nanoTime() > this.deadline) {
                  return this.hiddenTicks > 0
                     ? this.finish(AiOperation.Status.SUCCEEDED, "Hidden at the deadline")
                     : this.failBackoff("No cover reached in " + timeoutSeconds + " s");
               }

               if (this.retryCooldown > 0) {
                  this.retryCooldown--;
               }

               List<Entity> shooters = this.nearbyShooters();
               if (shooters.isEmpty()) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "No shooter or creeper left near");
               }

               Vec3 eye = mc.player.getEyePosition();
               Entity seer = null;
               double seerDistance = 1.7976931348623157E308;
               boolean creeperSees = false;

               for (Entity shooter : shooters) {
                  double d = shooter.distanceTo(mc.player);
                  if (!(d > 16.5)
                     && autismclient.ai.engine.AiSight.hasLineOfSight(mc.level, shooter.getEyePosition(), eye, -9223372036854775808L, -9223372036854775808L)) {
                     this.seenShooters.add(shooter.getId());
                     if (shooter instanceof Creeper) {
                        creeperSees = true;
                     }

                     if (d < seerDistance) {
                        seerDistance = d;
                        seer = shooter;
                     }
                  }
               }

               if (creeperSees) {
                  this.creeperSightedAt = eye;
               }

               Entity striker = this.meleeThreat();
               if (!sealOnly && striker != null) {
                  this.cancelEat();
                  if (this.placingShield && this.placer.needsReconciliation()) {
                     return this.tickShieldPlacement();
                  }

                  if (this.coverRoute == null
                     || !this.coverRoute.requiresReconciliationAfterEmergency() && autismclient.ai.engine.AiMotionSafety.isPlayerSupported()) {
                     this.cancelCoverRoute();
                     if (this.placingShield) {
                        this.placer.reset();
                        this.placingShield = false;
                     }

                     this.releaseJump();
                     this.shieldColumnActive = false;
                     this.offTargetTicks = this.aimAndStrike(striker) ? 0 : this.offTargetTicks + 1;
                     if (this.offTargetTicks > 10) {
                        if (this.stepBackFrom(striker)) {
                           return this.running("Stepping back from the " + BuiltInRegistries.ENTITY_TYPE.getKey(striker.getType()).getPath());
                        }
                     } else {
                        this.stopBackingOff();
                     }

                     return this.running("Defending cover against the " + BuiltInRegistries.ENTITY_TYPE.getKey(striker.getType()).getPath());
                  } else {
                     AiOperation.Result settling = this.coverRoute.tick();
                     if (settling.status() == AiOperation.Status.UNCERTAIN) {
                        return this.finish(AiOperation.Status.UNCERTAIN, settling.message());
                     }

                     if (settling.terminal()) {
                        this.cancelCoverRoute();
                     }

                     return this.running("Settling before defending cover");
                  }
               } else {
                  boolean baitExposed = seer == null
                     && this.creeperSightedAt != null
                     && this.creeperNearby(shooters)
                     && (
                        this.creeperSightedAt.distanceTo(eye) < 6.0
                           || autismclient.ai.engine.AiSight.hasLineOfSight(mc.level, this.creeperSightedAt, eye, -9223372036854775808L, -9223372036854775808L)
                     );
                  if (seer != null || !sealOnly && baitExposed) {
                     this.hiddenTicks = 0;
                     this.cancelEat();
                     if (seer == null) {
                        seer = this.firstCreeperOrNearest(shooters);
                        seerDistance = seer.distanceTo(mc.player);
                     }

                     this.shooterName = BuiltInRegistries.ENTITY_TYPE.getKey(seer.getType()).getPath();
                     if (seerDistance <= 3.0 && !(seer instanceof Creeper)) {
                        this.cancelCoverRoute();
                        Entity mount = seer.getVehicle() instanceof Mob m && m.isAlive() ? m : null;
                        Entity melee = this.meleeThreat();
                        Entity strike = mount != null ? mount : (melee != null && !autismclient.ai.engine.AiSight.init(melee) ? melee : seer);
                        this.aimAndStrike(strike);
                        return this.running("Hitting the " + BuiltInRegistries.ENTITY_TYPE.getKey(strike.getType()).getPath());
                     }

                     if (!(seerDistance <= 4.5)
                        || !(Math.abs(seer.getY() - mc.player.getY()) <= 3.0)
                        || AiCombat.init(seer)
                        || seer instanceof Creeper
                        || this.visibleShooterCount(shooters) > 1
                        || seer instanceof Blaze && !AiThreats.isLegacyCombatEnabled() && (mc.player.getHealth() < 16.0F || AiWorldScan.findEntitiesByType("minecraft:blaze", 16.0).size() > 1)) {
                        if (this.placingShield) {
                           return this.tickShieldPlacement();
                        }

                        if (this.shieldColumnActive) {
                           if (this.shieldsPlaced < 6 && this.shieldBlockItem() != null && this.shieldCellAgainst(seer) != null) {
                              return this.shieldFrom(seer, "finishing the shield column");
                           }

                           this.shieldColumnActive = false;
                        }

                        this.releaseJump();
                        if (sealOnly) {
                           return this.shieldFrom(seer, "no safe walking escape");
                        }

                        if (this.coverRoute != null
                           && this.plannedFromEye != null
                           && seer.getEyePosition().distanceTo(this.plannedFromEye) > 2.5
                           && this.retryCooldown == 0) {
                           this.cancelCoverRoute();
                        }

                        if (this.coverRoute == null) {
                           if (!mc.player.onGround() && !mc.player.isInWater()) {
                              return this.running("Landing before taking cover");
                           }

                           BlockPos feet = mc.player.blockPosition();
                           List<Vec3> eyes = this.shooterEyes(shooters);
                           List<Vec3> keepAway = new ArrayList<>();

                           for (Entity e : shooters) {
                              if (e instanceof Creeper) {
                                 keepAway.add(e.position());
                              }
                           }

                           if (!keepAway.isEmpty() && this.creeperSightedAt != null) {
                              eyes.add(this.creeperSightedAt);
                              keepAway.add(this.creeperSightedAt);
                           }

                           boolean creeper = !keepAway.isEmpty();
                           List<BlockPos> stances = this.routeFailures >= 3
                              ? List.of()
                              : autismclient.ai.engine.AiSight.init(
                                 mc.level,
                                 feet,
                                 eyes,
                                 keepAway,
                                 creeper ? 8.0 : 0.0,
                                 creeper,
                                 7 + (creeper ? 7 : 0),
                                 3,
                                 creeper ? 3 : 12,
                                 this.rejectedStances,
                                 48,
                                 mc.player.isInWater()
                              );
                           if (creeper && stances.isEmpty()) {
                              return this.failBackoff("No open ground out of the creeper's sight within 14 blocks");
                           }

                           if (stances.isEmpty()) {
                              return this.shieldFrom(seer, "no stance out of its sight within 7 blocks");
                           }

                           BlockPos nearest = stances.get(0);
                           boolean wetDig = mc.player.isInWater()
                              && (
                                 !mc.level.getBlockState(nearest).getCollisionShape(mc.level, nearest).isEmpty()
                                    || !mc.level.getBlockState(nearest.above()).getCollisionShape(mc.level, nearest.above()).isEmpty()
                              );
                           boolean far = wetDig || autismclient.ai.engine.AiSight.init(mc.level, feet, nearest) > 3.0;
                           if ((far || this.enemyNearby())
                              && keepAway.isEmpty()
                              && this.shieldsPlaced < 6
                              && this.shieldBlockItem() != null
                              && this.shieldCellAgainst(seer) != null) {
                              return this.shieldFrom(seer, far ? "the cover is far" : "mobs on the way to cover");
                           }

                           this.coverPlans++;
                           this.retryCooldown = 10;
                           this.plannedFromEye = seer.getEyePosition();
                           this.startPos = feet;
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] cover: from the {} at {} ({} m), {} stances, nearest {}",
                                 this.shooterName,
                                 seer.blockPosition().toShortString(),
                                 (int)seerDistance,
                                 stances.size(),
                                 stances.get(0).toShortString()
                              );
                           this.coverRoute = AiGoto.createGotoOpCompat(
                              feet,
                              0.6,
                              !creeper,
                              false,
                              false,
                              20,
                              autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                              false,
                              2.0,
                              new autismclient.ai.engine.CoverGoal(stances),
                              new AiGoto.GotoOptions(true, true, 3, !mc.player.isInWater(), true, true),
                              false
                           );
                        }

                        AiOperation.Result r = this.coverRoute.tick();
                        if (r.terminal()) {
                           this.cancelCoverRoute();
                           if (r.status() == AiOperation.Status.SUCCEEDED) {
                              this.rejectedStances.add(mc.player.blockPosition().asLong());
                           } else {
                              this.routeFailures++;
                              AutismClientAddon.LOG.info("[AI] cover: route failed ({}): {}", this.routeFailures, r.message());
                           }
                        }

                        return this.running("Taking cover from the " + this.shooterName + " (" + (int)seerDistance + " m)");
                     } else {
                        return this.finish(
                           AiOperation.Status.SUCCEEDED, "The " + this.shooterName + " came within " + (int)Math.ceil(seerDistance) + " blocks: fight it"
                        );
                     }
                  } else {
                     if (sealOnly) {
                        if (this.placingShield) {
                           AiOperation.Result progress = this.tickShieldPlacement();
                           if (this.placingShield) {
                              return progress;
                           }
                        }

                        if (autismclient.ai.engine.AiMotionSafety.isPlayerSupported()) {
                           return this.finish(AiOperation.Status.SUCCEEDED, "Escape tunnel sealed out of the creeper's sight");
                        }
                     }

                     this.releaseJump();
                     this.shieldColumnActive = false;
                     this.hiddenTicks++;
                     if (this.placingShield) {
                        this.placer.reset();
                        this.placingShield = false;
                     }

                     if (this.coverRoute != null) {
                        BlockPos feet = mc.player.blockPosition();
                        if (!mc.player.onGround()
                           || autismclient.ai.engine.AiSight.isVisibleFrom(mc.level, this.shooterEyes(shooters), feet.getX(), feet.getY(), feet.getZ())) {
                           AiOperation.Result r = this.coverRoute.tick();
                           if (r.terminal()) {
                              this.cancelCoverRoute();
                           }

                           return this.running("In cover from the " + this.shooterName + ", settling in");
                        }

                        this.cancelCoverRoute();
                     }

                     Entity melee = this.meleeThreat();
                     if (melee != null) {
                        this.cancelEat();
                        this.aimAndStrike(melee);
                     } else if (healTo > 0.0F
                        && mc.player.getHealth() < healTo
                        && (
                           this.eatOp != null
                              || mc.player.getFoodData().getFoodLevel() < 18 && this.eatAttempts < 3 && AiSurvival.bestFoodSlot(mc.player.getInventory()) >= 0
                        )) {
                        if (this.eatOp == null) {
                           this.eatAttempts++;
                           this.eatOp = AiSurvival.eat(AiJson.objectOf("food", 20, "timeoutSeconds", 10));
                        }

                        AiOperation.Result r = this.eatOp.tick();
                        if (!r.terminal()) {
                           return this.running("Eating in cover");
                        }

                        this.eatOp = null;
                     }

                     boolean healing = healTo > 0.0F && mc.player.getHealth() < healTo && mc.player.getFoodData().getFoodLevel() >= 18 && ++this.healTicks < 1200;
                     return this.hiddenTicks >= 80 && !healing
                        ? this.finish(AiOperation.Status.SUCCEEDED, "Out of sight of " + this.seenShooters.size() + " shooter(s) for " + this.hiddenTicks / 20 + " s")
                        : this.running(healing ? "Healing in cover" : "In cover from the " + this.shooterName + " (" + this.hiddenTicks / 20 + " s)");
                  }
               }
            }

            AiOperation.Result tickShieldPlacement() {
               this.prepareForPlacement();
               autismclient.ai.engine.AiPlacer.PlaceStatus s = this.placer.tick();
               if (s == autismclient.ai.engine.AiPlacer.PlaceStatus.WORKING) {
                  return this.running("Putting a block between the " + this.shooterName + " and me");
               }

               this.placingShield = false;
               if (s == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                  this.shieldsPlaced++;
                  AutismClientAddon.LOG
                     .info("[AI] cover: shield block at {} against the {}", this.placer.targetPos().toShortString(), this.shooterName);
               } else {
                  AutismClientAddon.LOG.info("[AI] cover: shield placement failed: {}", this.placer.statusSummary());
                  this.shieldColumnActive = false;
                  this.shieldsPlaced = 6;
               }

               return this.running("Checking the shield");
            }

            int visibleShooterCount(List<Entity> shooters) {
               int n = 0;

               for (Entity e : shooters) {
                  if (e.isAlive() && mc.player.hasLineOfSight(e)) {
                     n++;
                  }
               }

               return n;
            }

            AiOperation.Result shieldFrom(Entity seer, String why) {
               if (this.shieldsPlaced >= 6) {
                  return this.failBackoff("No cover from the " + this.shooterName + ": " + why + ", and no shield block would go down");
               }

               Item block = this.shieldBlockItem();
               BlockPos cell = block == null ? null : this.shieldCellAgainst(seer);
               if (cell == null) {
                  return this.failBackoff(
                     "No cover from the " + this.shooterName + ": " + why + (block == null ? ", and no building block to shield with" : ", and no cell to shield")
                  );
               }

               this.placer.start(cell, block);
               this.placingShield = true;
               this.shieldColumnActive = true;
               this.prepareForPlacement();
               return this.running("Putting a block between the " + this.shooterName + " and me");
            }

            void prepareForPlacement() {
               mc.options.keySprint.setDown(false);
               mc.player.setSprinting(false);
               if (mc.player.isInWater()) {
                  mc.options.keyJump.setDown(true);
                  this.jumpHeld = true;
               } else {
                  this.releaseJump();
               }
            }

            void releaseJump() {
               if (this.jumpHeld) {
                  mc.options.keyJump.setDown(false);
                  this.jumpHeld = false;
               }
            }

            BlockPos shieldCellAgainst(Entity seer) {
               return AiShield.init(mc, seer);
            }

            Item shieldBlockItem() {
               return AiShield.init(mc);
            }

            boolean enemyNearby() {
               return AiShield.hasEnemyWithin(mc, 6.0);
            }

            List<Vec3> shooterEyes(List<Entity> shooters) {
               List<Vec3> eyes = new ArrayList<>();

               for (Entity shooter : shooters) {
                  eyes.add(shooter.getEyePosition());
               }

               return eyes;
            }

            List<Entity> nearbyShooters() {
               List<Entity> found = new ArrayList<>();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity.isAlive() && (autismclient.ai.engine.AiSight.init(entity) || entity instanceof Creeper) && entity.distanceTo(mc.player) <= 20.0) {
                     found.add(entity);
                  }
               }

               found.sort(Comparator.comparingDouble(e -> e.distanceToSqr(mc.player)));
               return found;
            }

            boolean creeperNearby(List<Entity> watched) {
               for (Entity e : watched) {
                  if (e instanceof Creeper && e.distanceTo(mc.player) <= 16.0) {
                     return true;
                  }
               }

               return false;
            }

            Entity firstCreeperOrNearest(List<Entity> watched) {
               for (Entity e : watched) {
                  if (e instanceof Creeper) {
                     return e;
                  }
               }

               return watched.get(0);
            }

            Entity meleeThreat() {
               Entity best = null;
               double bestDistance = 3.0;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof Enemy && entity.isAlive() && !(entity instanceof Creeper) && AiThreats.isActiveThreat(mc, entity)) {
                     double d = entity.distanceTo(mc.player);
                     if (d <= bestDistance && mc.player.hasLineOfSight(entity)) {
                        best = entity;
                        bestDistance = d;
                     }
                  }
               }

               return best;
            }

            boolean aimAndStrike(Entity mob) {
               int slot = AiItems.bestHotbarWeaponSlot();
               if (slot >= 0 && mc.player.getInventory().getSelectedSlot() != slot) {
                  autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, slot);
               }

               this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(mob));
               this.aim.tick();
               boolean onTarget = this.aim.isAimedAtEntity(mob);
               if (onTarget && mc.player.getAttackStrengthScale(0.0F) >= 0.92F) {
                  AiRunTrace.emitServiceEvent(
                     "cover_attack",
                     AiJson.objectOf(
                        "targetId",
                        mob.getId(),
                        "targetUuid",
                        mob.getUUID().toString(),
                        "targetType",
                        BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString(),
                        "targetHealth",
                        mob instanceof LivingEntity living ? living.getHealth() : -1,
                        "playerHealth",
                        mc.player.getHealth(),
                        "charge",
                        mc.player.getAttackStrengthScale(0.0F),
                        "position",
                        mc.player.blockPosition().toShortString()
                     )
                  );
                  mc.gameMode.attack(mc.player, mob);
                  Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                  mc.player.resetAttackStrengthTicker();
               }

               return onTarget;
            }

            boolean stepBackFrom(Entity mob) {
               boolean safe = mc.player.onGround() && autismclient.ai.engine.AiCombatMotion.init(mc, -1.0, 0.0, 6) != null;
               mc.options.keyUp.setDown(false);
               mc.options.keyDown.setDown(safe);
               mc.options.keyLeft.setDown(false);
               mc.options.keyRight.setDown(false);
               mc.options.keySprint.setDown(false);
               this.backingOff = safe;
               return safe;
            }

            void stopBackingOff() {
               if (this.backingOff) {
                  this.backingOff = false;
                  mc.options.keyDown.setDown(false);
               }
            }

            void cancelCoverRoute() {
               if (this.coverRoute != null) {
                  this.coverRoute.cancel();
                  this.coverRoute = null;
               }
            }

            void cancelEat() {
               if (this.eatOp != null) {
                  this.eatOp.cancel();
                  this.eatOp = null;
               }
            }

            JsonObject progressData() {
               return AiJson.objectOf(
                  "shooters",
                  this.seenShooters.size(),
                  "plans",
                  this.coverPlans,
                  "shields",
                  this.shieldsPlaced,
                  "hiddenTicks",
                  this.hiddenTicks,
                  "startedAt",
                  this.startPos == null ? "" : this.startPos.toShortString()
               );
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressData());
            }

            AiOperation.Result failBackoff(String message) {
               AiGuard.noteCoverBackoff();
               return this.finish(AiOperation.Status.FAILED, message);
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               this.releaseJump();
               this.stopBackingOff();
               this.cancelCoverRoute();
               this.cancelEat();
               if (this.placingShield) {
                  this.placer.reset();
                  this.placingShield = false;
               }

               this.aim.clear();
               return AiGameOperations.result(status, message, this.progressData());
            }

            @Override
            public boolean canInterruptNow() {
               return (!this.placingShield || !this.placer.needsReconciliation())
                  && (this.coverRoute == null || this.coverRoute.canInterruptNow())
                  && (this.eatOp == null || this.eatOp.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.placingShield && this.placer.needsReconciliation()
                  || this.coverRoute != null && this.coverRoute.requiresReconciliationAfterEmergency()
                  || this.eatOp != null && this.eatOp.requiresReconciliationAfterEmergency();
            }

            @Override
            public void cancel() {
               this.releaseJump();
               this.stopBackingOff();
               this.cancelled = true;
               this.cancelCoverRoute();
               this.cancelEat();
               this.placer.reset();
               this.aim.clear();
            }
         };
      } else {
         throw new IllegalArgumentException("Taking cover requires a connected living player");
      }
   }
}
