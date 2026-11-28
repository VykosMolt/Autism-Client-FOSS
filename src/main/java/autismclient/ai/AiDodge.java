package autismclient.ai;

import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiDodge {
   static final double init_D = 1.2;
   static final double load_D = 5.0;
   static final int init_I = 12;
   static final int load_I = 3;
   static final int read = 5;

   private AiDodge() {
   }

   static boolean hasImminentThreat(Minecraft mc, boolean moving) {
      if (mc.player == null) {
         return false;
      }

      for (AiProjectiles.IncomingProjectile t : AiProjectiles.incoming(mc)) {
         if (AiProjectiles.isImminent(t)) {
            Entity e = mc.level.getEntity(t.entityId());
            return true;
         }
      }

      return false;
   }

   static boolean isImminent(AiProjectiles.IncomingProjectile t, double distance) {
      return t.secondsToClosest() >= 0.0 && t.secondsToClosest() <= 1.2 && distance <= 5.0;
   }

   static AiOperation dodge(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final boolean deflect = AiJson.getBoolean(args, "deflect", true);
         final boolean shieldOk = AiJson.getBoolean(args, "shield", false);
         final long deadline = System.nanoTime() + Math.min(10, Math.max(1, AiJson.getInt(args, "timeoutSeconds", 4))) * 1000000000L;
         return new AiOperation() {
            final Minecraft mc = Minecraft.getInstance();
            final Object startLevel = this.mc.level;
            final Object startConnection = this.mc.getConnection();
            final Object startPlayer = this.mc.player;
            AiGhastReflect ghastReflect = new AiGhastReflect(this.mc);
            final List<AiGhastReflect> reflectors = new ArrayList<>(List.of(this.ghastReflect));
            final Set<Integer> observedProjectiles = new HashSet<>();
            final Set<Integer> deflectedIds = new HashSet<>();
            final Map<Integer, UUID> threatUuids = new HashMap<>();
            boolean cancelled;
            boolean load_Z;
            boolean shielding;
            int blockedCount;
            int shieldTicks;
            int defenseTicks;
            final long startNanos = System.nanoTime();
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            int dodgeRetargetTicks;
            int trackedFireballId;
            int fireballWaitTicks;
            int dodgedCount;
            Vec3 dodgeTarget;
            Vec3 fireballVelocity;
            int lastSwingTick;
            final Set<Integer> swungFireballs;
            final float startHealth;
            float lastHealth;
            float damageTaken;

            {
               this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.FAST);
               this.trackedFireballId = -1;
               this.lastSwingTick = -100;
               this.swungFireballs = new HashSet<>();
               this.startHealth = this.mc.player.getHealth();
               this.lastHealth = this.startHealth;
            }

            @Override
            public AiOperation.Result tick() {
               this.defenseTicks++;
               if (!this.cancelled && this.mc.player != null && this.mc.level != null) {
                  if (this.mc.level == this.startLevel
                     && this.mc.getConnection() == this.startConnection
                     && this.mc.player == this.startPlayer
                     && this.mc.player.isAlive()) {
                     List<AiProjectiles.IncomingProjectile> threats = new ArrayList<>();

                     for (AiProjectiles.IncomingProjectile t : AiProjectiles.incoming(this.mc)) {
                        if (AiProjectiles.isImminent(t)) {
                           threats.add(t);
                        }
                     }

                     for (AiProjectiles.IncomingProjectile t : threats) {
                        Entity observed = this.mc.level.getEntity(t.entityId());
                        if (observed != null) {
                           UUID previous = this.threatUuids.put(t.entityId(), observed.getUUID());
                           if (previous != null && !previous.equals(observed.getUUID())) {
                              this.deflectedIds.remove(t.entityId());
                              this.swungFireballs.remove(t.entityId());
                              if (this.trackedFireballId == t.entityId()) {
                                 this.trackedFireballId = -1;
                              }
                           }

                           this.observedProjectiles.add(t.entityId());
                        }
                     }

                     this.damageTaken = this.damageTaken + Math.max(0.0F, this.lastHealth - this.mc.player.getHealth());
                     this.lastHealth = this.mc.player.getHealth();
                     this.checkDeflectedFireball();
                     if (this.load_Z) {
                        this.read();
                     }

                     this.dodgedCount = 0;

                     for (int id : this.observedProjectiles) {
                        boolean stillThreat = false;

                        for (AiProjectiles.IncomingProjectile t : threats) {
                           if (t.entityId() == id) {
                              stillThreat = true;
                              break;
                           }
                        }

                        if (!stillThreat && !this.deflectedIds.contains(id) && this.reflectors.stream().noneMatch(r -> r.isReflected(id)) && this.damageTaken == 0.0F) {
                           this.dodgedCount++;
                        }
                     }

                     if (System.nanoTime() > deadline) {
                        return this.finish(AiOperation.Status.TIMED_OUT, "Still under fire at the deadline", threats);
                     }

                     if (deflect) {
                        if (this.ghastReflect.load_Z()
                           && threats.stream()
                              .anyMatch(
                                 tx -> this.mc.level.getEntity(tx.entityId()) instanceof LargeFireball && !this.ghastReflect.isReflected(tx.entityId())
                              )) {
                           this.ghastReflect = new AiGhastReflect(this.mc);
                           this.reflectors.add(this.ghastReflect);
                        }

                        String reflection = this.ghastReflect.plan(threats);
                        threats.removeIf(tx -> this.reflectors.stream().anyMatch(r -> r.isReflected(tx.entityId())));
                        if (this.shielding && !this.mc.options.keyUse.isDown()) {
                           this.shielding = false;
                        }

                        if (reflection != null) {
                           if (this.shielding) {
                              this.mc.options.keyUse.setDown(false);
                              this.mc.gameMode.releaseUsingItem(this.mc.player);
                           }

                           this.shielding = false;
                           this.dodgeTarget = null;
                           this.dodgeRetargetTicks = 0;
                           return this.running(reflection, threats);
                        }
                     }

                     if (threats.isEmpty() && !this.ghastReflect.isTracking()) {
                        return this.finish(
                           AiOperation.Status.SUCCEEDED,
                           this.observedProjectiles.isEmpty() && this.deflectedIds.isEmpty()
                              ? "No incoming projectiles"
                              : (this.damageTaken > 0.0F ? "Incoming threats resolved; damage was received" : "Incoming threats resolved without observed damage"),
                           List.of()
                        );
                     }

                     if (threats.isEmpty()) {
                        return this.running("Awaiting server fireball redirection", threats);
                     }

                     if (deflect) {
                        Entity fireball = this.findDeflectableFireball(threats);
                        if (fireball != null) {
                           if (this.trackedFireballId != fireball.getId()) {
                              this.trackedFireballId = fireball.getId();
                              this.fireballVelocity = fireball.getDeltaMovement();
                              this.fireballWaitTicks = 0;
                           }

                           Entity owner = fireball instanceof Projectile projectile ? projectile.getOwner() : null;
                           Vec3 eye = this.mc.player.getEyePosition();
                           Vec3 back = owner != null && owner.isAlive() ? owner.getBoundingBox().getCenter() : null;
                           boolean onLine = back != null
                              && fireball.getBoundingBox().inflate(0.2).clip(eye, eye.add(back.subtract(eye).normalize().scale(6.0))).isPresent();
                           AiGameOperations.snapLookAt(onLine ? back : fireball.getBoundingBox().getCenter());
                           double reach = AiDefense.attackReach(this.mc);
                           BlockHitResult blockRay = this.mc
                              .level
                              .clip(new ClipContext(eye, fireball.getBoundingBox().getCenter(), Block.COLLIDER, Fluid.NONE, this.mc.player));
                           if (this.mc.player.tickCount - this.lastSwingTick >= 4
                              && blockRay.getType() == Type.MISS
                              && fireball.getBoundingBox().distanceToSqr(eye) <= reach * reach
                              && AiDefense.canSwingAt(this.mc, fireball)) {
                              this.mc.gameMode.attack(this.mc.player, fireball);
                              this.swungFireballs.add(fireball.getId());
                              this.lastSwingTick = this.mc.player.tickCount;
                              Compat263.swing(this.mc.player, InteractionHand.MAIN_HAND);
                              return this.running(onLine ? "Sending the fireball back" : "Batting the fireball away", threats);
                           }
                        }
                     }

                     AiProjectiles.IncomingProjectile dominant = threats.get(0);

                     for (AiProjectiles.IncomingProjectile t : threats) {
                        if (t.secondsToClosest() < dominant.secondsToClosest()) {
                           dominant = t;
                        }
                     }

                     if (shieldOk
                        && AiDefense.canBlock(this.mc, this.mc.level.getEntity(dominant.entityId()))
                        && AiDefense.shieldReadyFor(this.mc, dominant.secondsToClosest() * 20.0)
                        && this.canShieldAll(dominant, threats)) {
                        this.releaseKeys();
                        this.aim.aimAt(new Vec3(dominant.x(), dominant.y(), dominant.z()));
                        this.aim.tick();
                        if (!this.shielding) {
                           this.shielding = true;
                           this.mc.options.keyUse.setDown(true);
                        }

                        this.shieldTicks++;
                        return this.running("Blocking " + dominant.type(), threats);
                     } else {
                        if (this.shielding) {
                           this.mc.options.keyUse.setDown(false);
                           this.shielding = false;
                        }

                        if (this.dodgeTarget == null || this.dodgeRetargetTicks % 3 == 0) {
                           this.dodgeTarget = this.chooseDodgeTarget(dominant);
                        }

                        this.dodgeRetargetTicks++;
                        if (this.dodgeRetargetTicks > 12) {
                           this.releaseKeys();
                           this.dodgeRetargetTicks = 0;
                           this.dodgeTarget = null;
                           return this.running("Re-assessing the dodge", threats);
                        } else if (this.dodgeTarget == null) {
                           this.releaseKeys();
                           return this.running("Holding ground; no safe escape direction", threats);
                        } else {
                           this.releaseKeys();
                           this.mc.player.setYRot((float)(Math.toDegrees(Math.atan2(this.dodgeTarget.z, this.dodgeTarget.x)) - 90.0));
                           this.mc.options.keyUp.setDown(true);
                           return this.running("Dodging " + dominant.type(), threats);
                        }
                     }
                  } else {
                     return this.finish(AiOperation.Status.CANCELLED, "Dodge world or player changed", List.of());
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Dodge cancelled", List.of());
               }
            }

            void checkDeflectedFireball() {
               if (this.trackedFireballId >= 0) {
                  Entity e = this.mc.level.getEntity(this.trackedFireballId);
                  if (!this.swungFireballs.contains(this.trackedFireballId)
                     || e == null
                     || !e.getUUID().equals(this.threatUuids.get(this.trackedFireballId))
                     || !e.isAlive()
                     || !(e instanceof Projectile projectile && projectile.getOwner() == this.mc.player)
                        && (this.fireballVelocity == null || !(e.getDeltaMovement().dot(this.fireballVelocity) < 0.0))) {
                     if (++this.fireballWaitTicks > 20) {
                        this.trackedFireballId = -1;
                        this.fireballVelocity = null;
                     }
                  } else {
                     this.deflectedIds.add(this.trackedFireballId);
                     this.trackedFireballId = -1;
                     this.fireballVelocity = null;
                  }
               }
            }

            Vec3 chooseDodgeTarget(AiProjectiles.IncomingProjectile dominant) {
               Vec3 dir = AiProjectiles.dodgeDirection(
                  new Vec3(dominant.x(), dominant.y(), dominant.z()),
                  new Vec3(dominant.vx(), dominant.vy(), dominant.vz()),
                  dominant.gravity(),
                  this.mc.player.getEyePosition(),
                  dominant.secondsToClosest()
               );
               Vec3 best = null;
               double score = -1.7976931348623157E308;
               if (dir != null) {
                  for (Vec3 candidate : List.of(
                     dir,
                     dir.scale(-1.0),
                     this.mc.player.position().subtract(new Vec3(dominant.x(), this.mc.player.getY(), dominant.z())).normalize()
                  )) {
                     if (this.isSafeDodgeDirection(candidate)) {
                        double yaw = Math.toRadians(this.mc.player.getYRot());
                        double forward = -candidate.x * Math.sin(yaw) + candidate.z * Math.cos(yaw);
                        double strafe = candidate.x * Math.cos(yaw) + candidate.z * Math.sin(yaw);
                        int travelTicks = Math.max(1, Math.min(12, (int)Math.ceil(dominant.secondsToClosest() * 20.0)));
                        AABB landing = autismclient.ai.engine.AiCombatMotion.predictEndBox(this.mc, forward, strafe, travelTicks);
                        if (landing != null) {
                           Vec3 displacement = landing.getCenter().subtract(this.mc.player.getBoundingBox().getCenter());
                           boolean contact = false;

                           for (Entity mob : this.mc.level.entitiesForRendering()) {
                              if (mob instanceof LivingEntity && AiThreats.isActiveThreat(this.mc, mob) && landing.intersects(mob.getBoundingBox().inflate(1.2))) {
                                 contact = true;
                                 break;
                              }
                           }

                           if (!contact) {
                              double clearance = 1.7976931348623157E308;

                              for (AiProjectiles.IncomingProjectile shot : AiProjectiles.incoming(this.mc)) {
                                 if (AiProjectiles.isImminent(shot)) {
                                    Entity projectile = this.mc.level.getEntity(shot.entityId());
                                    if (projectile != null) {
                                       AiProjectiles.ApproachMetrics predicted = AiProjectiles.approach(this.mc, projectile, landing, travelTicks);
                                       clearance = Math.min(clearance, predicted == null ? 10.0 : predicted.closestApproach());
                                    }
                                 }
                              }

                              if (clearance > score) {
                                 score = clearance;
                                 best = candidate;
                              }
                           }
                        }
                     }
                  }
               }

               return best;
            }

            boolean canShieldAll(AiProjectiles.IncomingProjectile main, List<AiProjectiles.IncomingProjectile> threats) {
               Vec3 facing = new Vec3(main.x() - this.mc.player.getX(), 0.0, main.z() - this.mc.player.getZ()).normalize();

               for (AiProjectiles.IncomingProjectile shot : threats) {
                  if (!AiDefense.canBlock(this.mc, this.mc.level.getEntity(shot.entityId()))) {
                     return false;
                  }

                  Vec3 direction = new Vec3(shot.x() - this.mc.player.getX(), 0.0, shot.z() - this.mc.player.getZ()).normalize();
                  if (facing.dot(direction) < 0.5) {
                     return false;
                  }
               }

               return true;
            }

            boolean isSafeDodgeDirection(Vec3 dir) {
               BlockPos feet = this.mc.player.blockPosition();

               for (int ahead = 1; ahead <= 3; ahead++) {
                  BlockPos cell = feet.offset((int)Math.round(dir.x * ahead), 0, (int)Math.round(dir.z * ahead));
                  if (!AiCombat.isSafeStep(this.mc, cell)) {
                     return false;
                  }

                  if (this.mc.level.getBlockState(cell.below()).getCollisionShape(this.mc.level, cell.below()).isEmpty()) {
                     return false;
                  }
               }

               return true;
            }

            Entity findDeflectableFireball(List<AiProjectiles.IncomingProjectile> threats) {
               Entity best = null;
               double bestTime = 1.7976931348623157E308;

               for (AiProjectiles.IncomingProjectile t : threats) {
                  Entity e = this.mc.level.getEntity(t.entityId());
                  if (e instanceof Projectile
                     && e.isAlive()
                     && e.is(EntityTypeTags.REDIRECTABLE_PROJECTILE)
                     && !(e instanceof LargeFireball)
                     && AiDodge.isImminent(t, Math.sqrt(e.distanceToSqr(this.mc.player)))
                     && t.secondsToClosest() < bestTime) {
                     best = e;
                     bestTime = t.secondsToClosest();
                  }
               }

               return best;
            }

            JsonObject progressJson(List<AiProjectiles.IncomingProjectile> threats) {
               JsonArray remaining = new JsonArray();

               for (AiProjectiles.IncomingProjectile t : threats) {
                  remaining.add(
                     AiJson.objectOf(
                        "id", t.entityId(), "type", t.type(), "closestApproach", t.closestApproach(), "secondsToClosest", t.secondsToClosest()
                     )
                  );
               }

               JsonObject reflection = this.ghastReflect.statusJson();
               JsonArray shots = new JsonArray();

               for (AiGhastReflect controller : this.reflectors) {
                  shots.add(controller.statusJson());
               }

               for (String counter : List.of("serverConfirmedReflections", "ghastDeathsObserved", "missedAttempts", "defensiveBats", "movementTicks")) {
                  reflection.addProperty(counter, this.reflectors.stream().mapToInt(r -> AiJson.getInt(r.statusJson(), counter, 0)).sum());
               }

               reflection.add("shots", shots);
               return AiJson.objectOf(
                  "defenseTicks",
                  this.defenseTicks,
                  "defenseMs",
                  (System.nanoTime() - this.startNanos) / 1000000L,
                  "dodged",
                  this.dodgedCount,
                  "deflected",
                  this.deflectedIds.size() + this.reflectors.stream().mapToInt(AiGhastReflect::init_I).sum(),
                  "blocked",
                  this.blockedCount,
                  "shieldTicks",
                  this.shieldTicks,
                  "projectilesObserved",
                  this.observedProjectiles.size(),
                  "ghastReflection",
                  reflection,
                  "healthLost",
                  this.damageTaken,
                  "outcomeEvidence",
                  "client health and live projectile motion; shield block events unavailable",
                  "threats",
                  remaining
               );
            }

            AiOperation.Result running(String message, List<AiProjectiles.IncomingProjectile> threats) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressJson(threats));
            }

            AiOperation.Result finish(AiOperation.Status status, String message, List<AiProjectiles.IncomingProjectile> threats) {
               this.cancel();
               return AiGameOperations.result(status, message, this.progressJson(threats));
            }

            void releaseKeys() {
               if (this.mc.options != null) {
                  for (KeyMapping key : List.of(
                     this.mc.options.keyUp,
                     this.mc.options.keyDown,
                     this.mc.options.keyLeft,
                     this.mc.options.keyRight,
                     this.mc.options.keySprint
                  )) {
                     key.setDown(autismclient.util.KeyMappingControl.of(key).autism$physicalForBody());
                  }
               }
            }

            void read() {
               if (this.mc.options != null && this.load_Z) {
                  this.load_Z = false;
                  this.mc.options.keyAttack.setDown(autismclient.util.KeyMappingControl.of(this.mc.options.keyAttack).autism$physicalForBody());
               }
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  this.releaseKeys();
                  this.read();
                  this.ghastReflect.tick();
                  if (this.shielding && this.mc.options != null) {
                     this.mc.options.keyUse.setDown(false);
                  }

                  this.shielding = false;
                  this.aim.clear();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Dodging requires a connected living player");
      }
   }
}
