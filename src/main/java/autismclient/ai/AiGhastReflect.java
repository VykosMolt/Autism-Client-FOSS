package autismclient.ai;

import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiGhastReflect {
   static final double init_D = 0.949999988079071;
   static final double load_D = 0.1;
   static final int init_I = 60;
   static final int load_I = 32;
   private static final Map<UUID, AiGhastReflect> ACTIVE_BY_PROJECTILE = new LinkedHashMap<>();
   private static final Map<UUID, JsonObject> FINISHED_STATUS = new LinkedHashMap<>();
   private final Minecraft mc;
   private final Object level;
   private final Object player;
   private final Object connection;
   private final Set<UUID> init_Set = new HashSet<>();
   private final Set<UUID> load_Set = new HashSet<>();
   private final Map<Integer, UUID> projectileIdentityById = new HashMap<>();
   private final Map<UUID, Integer> firstSeenTick = new HashMap<>();
   private final JsonArray init_JsonArray = new JsonArray();
   private final Map<String, Integer> rejections = new LinkedHashMap<>();
   private int projectileId = -1;
   private int ghastId = -1;
   private int update_I = -1;
   private int movementTicks;
   private int confirmedReflections;
   private int ghastDeaths;
   private int missedAttempts;
   private int defensiveBats;
   private int confirmTicks;
   private UUID projectileUuid;
   private UUID ghastUuid;
   private Vec3 incomingVelocity;
   private Vec3 sentDirection;
   private Vec3 ghastMotion = Vec3.ZERO;
   private Vec3 lastGhastPos;
   private int lastGhastSampleTick;
   private int predictedFlightTicks;
   private int threatSeenTick;
   private int reactionTicks;
   private int outcomeTicks;
   private long evidenceRevision;
   private boolean reflected;
   private boolean load_Z;
   private AiGhastReflect.InterceptPlan plan;
   private JsonObject lastRejection;
   private String state = "idle";
   private String reason = "No incoming ghast fireball";

   AiGhastReflect(Minecraft mc) {
      this.mc = mc;
      this.level = mc.level;
      this.player = mc.player;
      this.connection = mc.getConnection();
   }

   boolean isTracking() {
      return this.projectileId >= 0 && !this.reflected;
   }

   int getConfirmedReflections() {
      return this.confirmedReflections;
   }

   boolean hasReflected() {
      return this.reflected && this.update_I >= 0;
   }

   boolean isReflected(int id) {
      UUID identity = this.projectileIdentityById.get(id);
      Entity current = this.mc.level == null ? null : this.mc.level.getEntity(id);
      return identity != null && (current == null || current.getUUID().equals(identity));
   }

   static JsonObject statusFor(UUID projectile) {
      AiGhastReflect active = ACTIVE_BY_PROJECTILE.get(projectile);
      return active == null ? FINISHED_STATUS.get(projectile) : active.statusJson();
   }

   static void tickAll() {
      for (AiGhastReflect observer : List.copyOf(ACTIVE_BY_PROJECTILE.values())) {
         observer.read();
      }
   }

   private void read() {
      if (this.update_I >= 0) {
         if (this.mc.level == this.level
            && this.mc.player == this.player
            && this.mc.getConnection() == this.connection
            && this.mc.player.isAlive()) {
            Entity source = this.mc.level.getEntity(this.ghastId);
            Entity projectile = this.mc.level.getEntity(this.projectileId);
            Ghast ghast = source instanceof Ghast g && g.getUUID().equals(this.ghastUuid) ? g : null;
            LargeFireball ball = projectile instanceof LargeFireball b && b.getUUID().equals(this.projectileUuid) ? b : null;
            AiServerEvidence.EntityVelocity motion = AiServerEvidence.lastEntityVelocity(this.projectileId);
            if (!this.reflected
               && motion != null
               && motion.identity().equals(this.projectileUuid)
               && motion.revision() > this.evidenceRevision
               && isReflected(this.incomingVelocity, this.sentDirection, motion.velocity())) {
               this.reflected = true;
               this.confirmedReflections++;
               this.projectileIdentityById.put(this.projectileId, this.projectileUuid);
               this.confirmTicks = this.mc.player.tickCount - this.update_I;
               if (this.load_Z) {
                  this.defensiveBats++;
               }

               this.setState(
                  "returning", (this.load_Z ? "Server confirms defensive bat" : "Server confirms return") + " after " + this.confirmTicks + " ticks; objective resumes"
               );
            }

            if (ghast != null && !ghast.isAlive()) {
               if (this.reflected) {
                  this.ghastDeaths++;
               }

               this.recordOutcome(this.reflected ? "ghast_death_observed" : "death_without_motion_confirmation");
            } else {
               int age = this.mc.player.tickCount - this.update_I;
               if (!this.reflected && age >= 5) {
                  this.missedAttempts++;
                  this.recordOutcome("no_server_redirection; emergency_defense");
               } else if (this.reflected && (ball == null || !ball.isAlive()) || age > this.predictedFlightTicks + 8 || this.ghastId >= 0 && ghast == null) {
                  if (this.reflected && !this.load_Z && ghast != null && ghast.isAlive()) {
                     this.missedAttempts++;
                  }

                  this.recordOutcome(
                     this.ghastId >= 0 && ghast == null
                        ? "target_unavailable"
                        : (this.load_Z ? "defensive_bat; no_observed_kill" : "returned_shot_ended_without_observed_kill")
                  );
               }
            }
         } else {
            this.recordOutcome("world_or_player_changed; outcome_unknown");
         }
      }
   }

   String plan(List<AiProjectiles.IncomingProjectile> threats) {
      if (this.projectileId < 0) {
         this.init_V_List(threats);
      }

      if (this.projectileId < 0) {
         return null;
      }

      Entity source = this.mc.level.getEntity(this.ghastId);
      Entity projectile = this.mc.level.getEntity(this.projectileId);
      Ghast ghast = source instanceof Ghast g && g.getUUID().equals(this.ghastUuid) ? g : null;
      LargeFireball ball = projectile instanceof LargeFireball b && b.getUUID().equals(this.projectileUuid) ? b : null;
      if (this.update_I >= 0) {
         this.read();
         return this.isTracking() && threats.isEmpty() ? "Awaiting server fireball redirection" : null;
      }

      if (ball != null && ball.isAlive()) {
         if (ghast != null && ghast.isAlive()) {
            this.trackGhast(ghast);
         }

         Vec3 eye = this.mc.player.getEyePosition();
         if (threats.stream().noneMatch(t -> t.entityId() == this.projectileId)) {
            this.recordOutcome("fireball_no_longer_threatens_body; objective_resumes");
            return null;
         }

         this.plan = null;
         this.load_Z = false;
         if (this.mc.player.isWithinAttackRange(this.mc.player.getMainHandItem(), ball.getBoundingBox(), 0.0)
            && !this.mc.player.cannotAttackWithItem(this.mc.player.getMainHandItem(), 0)) {
            AiGhastReflect.FireballFlight immediate = ghast != null && ghast.isAlive()
               ? init(
                  eye,
                  ball.position(),
                  ball.getBoundingBox().inflate(ball.getPickRadius()),
                  ghast.getBoundingBox(),
                  this.ghastMotion,
                  0,
                  AiDefense.attackReach(this.mc)
               )
               : null;
            if (immediate != null && this.init_Z_Vec3(ball.position(), immediate, ball)) {
               this.plan = new AiGhastReflect.InterceptPlan(immediate, 0, 0, 0, yawOf(immediate.direction), 0.0);
            } else {
               AiGhastReflect.FireballFlight bat = this.batFlight(ball);
               if (bat != null) {
                  this.load_Z = true;
                  this.plan = new AiGhastReflect.InterceptPlan(bat, 0, 0, 0, yawOf(bat.direction), 0.0);
               }
            }
         }

         if (this.plan == null
            && !this.load_Set.contains(this.projectileUuid)
            && ghast != null
            && ghast.isAlive()
            && this.mc.player.onGround()
            && !this.mc.player.isInWater()
            && !this.mc.player.onClimbable()
            && this.init_Z_AABB(this.mc.player.getBoundingBox())) {
            this.mc.options.keySprint.setDown(false);
            this.mc.player.setSprinting(false);
            this.plan = this.interceptPlan(ball, ghast, threats);
         }

         if (this.plan == null) {
            this.setState("evading", "No safe ghast return; shield/dodge until defensive hit is reachable: " + this.rejections);
            this.load_Set.add(this.projectileUuid);
            this.projectileId = -1;
            return null;
         }

         AiGameOperations.snapLookAt(eye.add(this.plan.flight.direction.scale(16.0)));
         if (this.plan.interceptTicks == 0 && AiDefense.canSwingAt(this.mc, ball)) {
            autismclient.util.ServerRotationTracker.RotationSnapshot wire = autismclient.util.ServerRotationTracker.getSnapshot();
            if (!wire.initialized()) {
               return null;
            }

            Vec3 direction = Vec3.directionFromRotation(wire.currentPitch(), wire.currentYaw());
            if (!rayHitsBall(eye, direction, ball.getBoundingBox().inflate(ball.getPickRadius()), AiDefense.attackReach(this.mc))) {
               return null;
            }

            AiGhastReflect.FireballFlight actual = init(ball.position(), direction, ghast == null ? null : ghast.getBoundingBox(), this.ghastMotion, this.load_Z);
            if (!this.load_Z && (actual == null || !this.init_Z_Vec3(ball.position(), actual, ball))) {
               AiGhastReflect.FireballFlight bat = init(ball.position(), direction, null, Vec3.ZERO, true);
               if (bat != null
                  && ball.getDeltaMovement().normalize().dot(direction) < 0.98
                  && this.init_Z_Vec3(ball.position(), bat, ball)
                  && this.init(ball, direction)) {
                  actual = bat;
                  this.load_Z = true;
               }
            }

            if (actual != null && this.init_Z_Vec3(ball.position(), actual, ball) && (!this.load_Z || this.init(ball, direction))) {
               this.releaseUse();
               this.releaseMovementKeys();
               this.mc.options.keySprint.setDown(false);
               this.incomingVelocity = ball.getDeltaMovement();
               this.sentDirection = direction;
               AiServerEvidence.EntityVelocity evidence = AiServerEvidence.lastEntityVelocity(this.projectileId);
               this.evidenceRevision = evidence == null ? 0L : evidence.revision();
               this.update_I = this.mc.player.tickCount;
               this.predictedFlightTicks = actual.ticks;
               this.reactionTicks = this.update_I - this.threatSeenTick;
               this.init_Set.add(this.projectileUuid);
               ACTIVE_BY_PROJECTILE.put(this.projectileUuid, this);

               while (ACTIVE_BY_PROJECTILE.size() > 16) {
                  ACTIVE_BY_PROJECTILE.values().iterator().next().recordOutcome("observation_capacity; outcome_unknown");
               }

               this.mc.gameMode.attack(this.mc.player, ball);
               Compat263.swing(this.mc.player, InteractionHand.MAIN_HAND);
               this.setState(
                  "struck",
                  (this.load_Z ? "Batting threatening fireball away; no kill solution required" : "Returning the fireball along the predicted ghast intercept")
                     + "; "
                     + this.reactionTicks
                     + " ticks since threat acquisition"
               );
               return this.reason;
            } else {
               return this.init_Str();
            }
         } else if (this.plan.interceptTicks == 0) {
            if (this.mc.player.cannotAttackWithItem(this.mc.player.getMainHandItem(), 0)) {
               return null;
            }

            if (this.mc.player.isWithinAttackRange(this.mc.player.getMainHandItem(), ball.getBoundingBox(), 0.0)
               && rayHitsBall(eye, this.plan.flight.direction, ball.getBoundingBox().inflate(ball.getPickRadius()), AiDefense.attackReach(this.mc))
               )
             {
               return this.init_Str();
            }

            this.lastRejection = AiJson.objectOf(
               "alive",
               ball.isAlive(),
               "charged",
               !this.mc.player.cannotAttackWithItem(this.mc.player.getMainHandItem(), 0),
               "range",
               this.mc.player.isWithinAttackRange(this.mc.player.getMainHandItem(), ball.getBoundingBox(), 0.0),
               "ray",
               autismclient.ai.engine.AiAim.isLookingAtEntity(ball),
               "eye",
               vecJson(this.mc.player.getEyePosition()),
               "look",
               vecJson(this.mc.player.getLookAngle()),
               "intended",
               vecJson(this.plan.flight.direction),
               "projectileBox",
               ball.getBoundingBox().toString(),
               "reach",
               AiDefense.attackReach(this.mc)
            );
            AiSpeedrun.log("[AI] ghast reflection rejected attack: {}", this.lastRejection);
            this.init_Set.add(this.projectileUuid);
            this.recordOutcome("live_attack_ray_or_range_rejected; emergency defense");
            return null;
         } else {
            if (this.mc.player.isUsingItem()) {
               this.releaseUse();
               this.releaseMovementKeys();
               return "Preparing interception; rechecking movement after shield slowdown";
            }

            this.releaseMovementKeys();
            this.mc.options.keySprint.setDown(false);
            this.mc.options.keyUp.setDown(this.plan.forward > 0);
            this.mc.options.keyDown.setDown(this.plan.forward < 0);
            this.mc.options.keyLeft.setDown(this.plan.strafe > 0);
            this.mc.options.keyRight.setDown(this.plan.strafe < 0);
            if (this.plan.forward != 0 || this.plan.strafe != 0) {
               this.movementTicks++;
            }

            this.setState("aligning", "Moving into the fireball's predicted return line");
            return this.reason;
         }
      } else {
         this.recordOutcome("incoming_shot_unavailable");
         return null;
      }
   }

   private String init_Str() {
      this.releaseMovementKeys();
      this.setState("facing", "Publishing the defensive look through ordinary movement before striking");
      return this.reason;
   }

   private boolean init(LargeFireball ball, Vec3 direction) {
      for (Entity entity : this.mc.level.entitiesForRendering()) {
         if (entity != this.mc.player
            && entity instanceof LivingEntity
            && entity.isAlive()
            && init(ball.position(), direction, entity.getBoundingBox(), observedMotion(entity), 0) > 0) {
            return false;
         }
      }

      return true;
   }

   private void init_V_List(List<AiProjectiles.IncomingProjectile> threats) {
      AiProjectiles.IncomingProjectile first = null;

      for (AiProjectiles.IncomingProjectile threat : threats) {
         if (this.mc.level.getEntity(threat.entityId()) instanceof LargeFireball ball
            && ball.isAlive()
            && !this.init_Set.contains(ball.getUUID())
            && ball.is(EntityTypeTags.REDIRECTABLE_PROJECTILE)
            && (first == null || threat.secondsToClosest() < first.secondsToClosest())) {
            first = threat;
         }
      }

      if (first != null) {
         LargeFireball ball = (LargeFireball)this.mc.level.getEntity(first.entityId());
         Ghast ghast = ball.getOwner() instanceof Ghast g && g.isAlive() && g.distanceToSqr(this.mc.player) <= 4096.0 ? g : null;
         this.projectileId = ball.getId();
         this.projectileUuid = ball.getUUID();
         this.ghastId = ghast == null ? -1 : ghast.getId();
         this.ghastUuid = ghast == null ? null : ghast.getUUID();
         this.threatSeenTick = this.firstSeenTick.computeIfAbsent(this.projectileUuid, ignored -> this.mc.player.tickCount);
         this.confirmTicks = this.reactionTicks = this.outcomeTicks = 0;
         this.update_I = -1;
         this.reflected = false;
         this.plan = null;
         this.lastGhastPos = null;
         this.ghastMotion = ghast == null ? Vec3.ZERO : observedMotion(ghast);
         if (!this.load_Set.contains(this.projectileUuid)) {
            this.setState("acquired", "Predicting incoming fireball and moving ghast");
         }
      }
   }

   private AiGhastReflect.FireballFlight batFlight(LargeFireball ball) {
      Vec3 eye = this.mc.player.getEyePosition();
      AABB pick = ball.getBoundingBox().inflate(ball.getPickRadius());
      AiGhastReflect.FireballFlight best = null;
      double score = -1.7976931348623157E308;

      for (Vec3 direction : aimDirections(eye, pick, AiDefense.attackReach(this.mc))) {
         if (this.isClearPath(eye, eye.add(direction.scale(AiDefense.attackReach(this.mc))), ball)) {
            Vec3 position = ball.position();
            Vec3 velocity = direction;
            boolean safe = true;

            for (int tick = 1; tick <= 8; tick++) {
               velocity = velocity.add(velocity.normalize().scale(0.1)).scale(0.949999988079071);
               Vec3 next = position.add(velocity);
               if (!this.isLoaded(next) || !this.isClearPath(position, next, ball)) {
                  safe = false;
                  break;
               }

               position = next;
               if (position.distanceToSqr(this.mc.player.getBoundingBox().getCenter()) > 36.0) {
                  break;
               }
            }

            if (safe) {
               double candidate = direction.y;
               if (candidate > score) {
                  score = candidate;
                  best = new AiGhastReflect.FireballFlight(direction, 24, position);
               }
            }
         }
      }

      return best;
   }

   static List<Vec3> aimDirections(Vec3 eye, AABB pick, double reach) {
      ArrayList<Vec3> directions = new ArrayList<>();

      for (int x : new int[]{0, -1, 1}) {
         for (int y : new int[]{1, 0, -1}) {
            for (int z : new int[]{0, -1, 1}) {
               Vec3 point = pick.getCenter().add(x * pick.getXsize() * 0.32, y * pick.getYsize() * 0.32, z * pick.getZsize() * 0.32);
               Vec3 direction = point.subtract(eye).normalize();
               if (rayHitsBall(eye, direction, pick, reach)) {
                  directions.add(direction);
               }
            }
         }
      }

      return directions;
   }

   private void trackGhast(Ghast ghast) {
      int tick = this.mc.player.tickCount;
      Vec3 motion = observedMotion(ghast);
      if (this.lastGhastPos != null && tick > this.lastGhastSampleTick) {
         Vec3 observed = ghast.position().subtract(this.lastGhastPos).scale(1.0 / (tick - this.lastGhastSampleTick));
         if (observed.lengthSqr() < 1.0) {
            motion = observed;
         }
      }

      this.ghastMotion = motion;
      this.lastGhastPos = ghast.position();
      this.lastGhastSampleTick = tick;
   }

   private static Vec3 observedMotion(Entity target) {
      Vec3 observed = new Vec3(target.getX() - target.xo, target.getY() - target.yo, target.getZ() - target.zo);
      if (observed.lengthSqr() < 1.0E-8) {
         observed = target.getDeltaMovement();
      }

      return observed.lengthSqr() <= 1.0 ? observed : Vec3.ZERO;
   }

   private AiGhastReflect.InterceptPlan interceptPlan(LargeFireball ball, Ghast ghast, List<AiProjectiles.IncomingProjectile> threats) {
      this.rejections.clear();
      Vec3 eye = this.mc.player.getEyePosition();
      double reach = AiDefense.attackReach(this.mc);
      AiGhastReflect.FireballFlight now = init(eye, ball.position(), ball.getBoundingBox().inflate(ball.getPickRadius()), ghast.getBoundingBox(), this.ghastMotion, 0, reach);
      if (now != null
         && this.mc.player.isWithinAttackRange(this.mc.player.getMainHandItem(), ball.getBoundingBox(), 0.0)
         && this.init_Z_Vec3(ball.position(), now, ball)
         && autismclient.ai.engine.AiCombatMotion.predictEndBox(this.mc, 0.0, 0.0, 4) != null
         && this.init_Z_LargeFireball(ball, this.mc.player.getBoundingBox(), 0, threats)) {
         return new AiGhastReflect.InterceptPlan(now, 0, 0, 0, yawOf(now.direction), 0.0);
      }

      AiGhastReflect.FireballFlight lead = simulateFlight(ball.position(), ghast.getBoundingBox(), this.ghastMotion, 0);
      if (lead == null) {
         this.reject("target_beyond_prediction_horizon");
         return null;
      }

      float facing = yawOf(lead.direction);
      Vec3 position = ball.position();
      Vec3 velocity = ball.getDeltaMovement();
      Vec3[] inbound = new Vec3[33];
      inbound[0] = position;

      for (int tick = 1; tick <= 32; tick++) {
         boolean water = this.mc.level.getFluidState(BlockPos.containing(position)).is(FluidTags.WATER);
         velocity = velocity.add(velocity.normalize().scale(ball.accelerationPower)).scale(water ? 0.800000011920929 : 0.949999988079071);
         Vec3 next = position.add(velocity);
         if (!this.isLoaded(next) || !this.isClearPath(position, next, ball)) {
            this.reject("incoming_terrain_or_unloaded");
            break;
         }

         inbound[tick] = next;
         position = next;
      }

      AiGhastReflect.InterceptPlan best = null;

      for (int forward = -1; forward <= 1; forward++) {
         for (int strafe = -1; strafe <= 1; strafe++) {
            for (int horizon : new int[]{4, 8, 12}) {
               List<AABB> trace = autismclient.ai.engine.AiCombatMotion.predictTrace(this.mc, facing, forward, strafe, horizon, 4);
               if (trace != null && !trace.stream().anyMatch(bodyx -> !this.init_Z_AABB(bodyx))) {
                  for (int tick = 1; tick <= 32 && inbound[tick] != null; tick++) {
                     AABB body = trace.get(Math.min(tick - 1, trace.size() - 1));
                     AABB previous = tick == 1 ? this.mc.player.getBoundingBox() : trace.get(Math.min(tick - 2, trace.size() - 1));
                     if (body.inflate(0.15).contains(inbound[tick]) || previous.inflate(0.15).clip(inbound[tick - 1], inbound[tick]).isPresent()) {
                        this.reject("incoming_body_collision_before_attack");
                        break;
                     }

                     Vec3 shift = body.getCenter().subtract(this.mc.player.getBoundingBox().getCenter());
                     Vec3 futureEye = eye.add(shift);
                     AABB actualBall = ball.getBoundingBox().move(inbound[tick].subtract(ball.position()));
                     if (actualBall.distanceToSqr(futureEye) > (reach - 0.2) * (reach - 0.2)) {
                        this.reject("outside_physical_attack_range");
                     } else {
                        AABB futureBall = actualBall.inflate(ball.getPickRadius());
                        AiGhastReflect.FireballFlight flight = init(futureEye, inbound[tick], futureBall, ghast.getBoundingBox(), this.ghastMotion, tick, reach - 0.2);
                        if (flight == null) {
                           this.reject("no_common_attack_and_ghast_intercept_ray");
                        } else if (!this.isClearPath(futureEye, futureEye.add(flight.direction.scale(reach)), ball)) {
                           this.reject("attack_ray_blocked");
                        } else if (!this.init_Z_Vec3(inbound[tick], flight, ball)) {
                           this.reject("returned_shot_terrain_water_or_unloaded");
                        } else {
                           if (this.init_Z_LargeFireball(ball, body, tick, threats)) {
                              double error = shift.horizontalDistance();
                              if (best == null || tick < best.interceptTicks || tick == best.interceptTicks && error < best.error) {
                                 best = new AiGhastReflect.InterceptPlan(flight, tick, forward, strafe, facing, error);
                              }
                              break;
                           }

                           this.reject("competing_threat_before_return");
                        }
                     }
                  }

                  if (best != null && best.interceptTicks <= horizon) {
                     break;
                  }
               } else {
                  this.reject("unsafe_footing_or_coast");
               }
            }
         }
      }

      return best;
   }

   private void reject(String reason) {
      this.rejections.merge(reason, 1, Integer::sum);
   }

   private boolean init_Z_LargeFireball(LargeFireball ball, AABB landing, int ticks, List<AiProjectiles.IncomingProjectile> threats) {
      for (AiProjectiles.IncomingProjectile threat : threats) {
         if (threat.entityId() != ball.getId()) {
            Entity other = this.mc.level.getEntity(threat.entityId());
            if (other != null) {
               AiProjectiles.ApproachMetrics danger = AiProjectiles.approach(this.mc, other, landing, ticks);
               if (danger != null && danger.closestApproach() <= 0.5 && danger.secondsToClosest() * 20.0 <= ticks + 5) {
                  return false;
               }
            }
         }
      }

      for (Entity mob : this.mc.level.entitiesForRendering()) {
         if (mob instanceof LivingEntity
            && mob != this.mc.player
            && AiThreats.isActiveThreat(this.mc, mob)
            && landing.intersects(mob.getBoundingBox().inflate(1.2))) {
            return false;
         }
      }

      return true;
   }

   static AiGhastReflect.FireballFlight init(Vec3 eye, Vec3 launch, AABB ball, AABB target, Vec3 motion, int delay, double reach) {
      if (ball.distanceToSqr(eye) > reach * reach) {
         return null;
      }

      AiGhastReflect.FireballFlight centre = simulateFlight(launch, target, motion, delay);
      if (centre == null) {
         return null;
      }

      if (rayHitsBall(eye, centre.direction, ball, reach)) {
         return centre;
      }

      Vec3 middle = ball.getCenter();

      for (int x : new int[]{0, -1, 1}) {
         for (int y : new int[]{0, -1, 1}) {
            for (int z : new int[]{0, -1, 1}) {
               Vec3 point = middle.add(x * ball.getXsize() * 0.32, y * ball.getYsize() * 0.32, z * ball.getZsize() * 0.32);
               Vec3 direction = point.subtract(eye).normalize();
               if (rayHitsBall(eye, direction, ball, reach)) {
                  int hit = init(launch, direction, target, motion, delay);
                  if (hit > 0) {
                     return new AiGhastReflect.FireballFlight(direction, hit, target.getCenter().add(motion.scale(delay + hit)));
                  }
               }
            }
         }
      }

      return null;
   }

   static AiGhastReflect.FireballFlight simulateFlight(Vec3 launch, AABB target, Vec3 motion, int delay) {
      double distance = 0.0;
      double speed = 1.0;

      for (int tick = 1; tick <= 60; tick++) {
         speed = (speed + 0.1) * 0.949999988079071;
         distance += speed;
         Vec3 point = target.getCenter().add(motion.scale(delay + tick));
         if (!(distance < point.distanceTo(launch))) {
            Vec3 direction = point.subtract(launch).normalize();
            int hit = init(launch, direction, target, motion, delay);
            return hit > 0 ? new AiGhastReflect.FireballFlight(direction, hit, point) : null;
         }
      }

      return null;
   }

   static int init(Vec3 launch, Vec3 direction, AABB target, Vec3 motion, int delay) {
      Vec3 position = launch;
      Vec3 velocity = direction.normalize();

      for (int tick = 1; tick <= 60; tick++) {
         velocity = velocity.add(velocity.normalize().scale(0.1)).scale(0.949999988079071);
         Vec3 next = position.add(velocity);
         AABB box = target.move(motion.scale(delay + tick - 1)).inflate(0.3);
         if (box.contains(position) || box.clip(position, next.subtract(motion)).isPresent()) {
            return tick;
         }

         position = next;
      }

      return -1;
   }

   static AiGhastReflect.FireballFlight init(Vec3 launch, Vec3 direction, AABB target, Vec3 motion, boolean defensive) {
      if (direction == null || direction.lengthSqr() < 0.9) {
         return null;
      }

      if (defensive) {
         return new AiGhastReflect.FireballFlight(direction, 24, launch.add(direction.scale(24.0)));
      }

      if (target == null) {
         return null;
      }

      int ticks = init(launch, direction, target, motion, 0);
      return ticks > 0 ? new AiGhastReflect.FireballFlight(direction, ticks, target.getCenter().add(motion.scale(ticks))) : null;
   }

   static boolean rayHitsBall(Vec3 eye, Vec3 direction, AABB ball, double reach) {
      return direction.lengthSqr() > 0.9 && !ball.contains(eye) && ball.clip(eye, eye.add(direction.scale(reach))).isPresent();
   }

   static boolean isReflected(Vec3 incoming, Vec3 sent, Vec3 applied) {
      return applied.lengthSqr() > 0.01 && applied.normalize().dot(sent) > 0.98 && incoming.normalize().dot(sent) < 0.98;
   }

   private static float yawOf(Vec3 d) {
      return (float)(Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0);
   }

   private boolean init_Z_Vec3(Vec3 position, AiGhastReflect.FireballFlight flight, Entity ball) {
      Vec3 velocity = flight.direction;

      for (int tick = 1; tick <= flight.ticks; tick++) {
         if (this.mc.level.getFluidState(BlockPos.containing(position)).is(FluidTags.WATER)) {
            return false;
         }

         velocity = velocity.add(velocity.normalize().scale(0.1)).scale(0.949999988079071);
         Vec3 next = position.add(velocity);
         if (!this.isLoaded(next) || !this.isClearPath(position, next, ball)) {
            return false;
         }

         position = next;
      }

      return true;
   }

   private boolean isLoaded(Vec3 point) {
      return AiTerrain.isLoaded(this.mc.level, BlockPos.containing(point));
   }

   private boolean init_Z_AABB(AABB body) {
      for (double x : new double[]{body.minX + 0.04, body.maxX - 0.04}) {
         for (double z : new double[]{body.minZ + 0.04, body.maxZ - 0.04}) {
            AABB foot = new AABB(x - 0.01, body.minY - 0.03, z - 0.01, x + 0.01, body.minY, z + 0.01);
            if (!this.mc.level.getBlockCollisions(this.mc.player, foot).iterator().hasNext()) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean isClearPath(Vec3 from, Vec3 to, Entity context) {
      return this.mc.level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, context)).getType() == Type.MISS;
   }

   private void setState(String next, String why) {
      if (!this.state.equals(next)) {
         AiSpeedrun.log("[AI] ghast reflection: {} ball={} ghast={} ({})", next, this.projectileId, this.ghastId, why);
      }

      this.state = next;
      this.reason = why;
   }

   private void recordOutcome(String outcome) {
      if (this.update_I >= 0 && this.mc.player == this.player) {
         this.outcomeTicks = this.mc.player.tickCount - this.update_I;
      }

      if (this.init_JsonArray.size() < 8) {
         this.init_JsonArray
            .add(
               AiJson.objectOf(
                  "projectileId",
                  this.projectileId,
                  "ghastId",
                  this.ghastId,
                  "serverRedirected",
                  this.reflected,
                  "outcome",
                  outcome,
                  "strikeTick",
                  this.update_I,
                  "attackDelayTicks",
                  this.reactionTicks,
                  "acknowledgementTicks",
                  this.confirmTicks,
                  "outcomeObservationTicks",
                  this.outcomeTicks
               )
            );
      }

      this.setState("resolved", outcome);
      this.projectileId = -1;
      this.update_I = -1;
      this.plan = null;
      if (this.projectileUuid != null) {
         ACTIVE_BY_PROJECTILE.remove(this.projectileUuid);
         FINISHED_STATUS.put(this.projectileUuid, this.statusJson());

         while (FINISHED_STATUS.size() > 32) {
            FINISHED_STATUS.remove(FINISHED_STATUS.keySet().iterator().next());
         }
      }
   }

   JsonObject statusJson() {
      JsonObject out = AiJson.objectOf(
         "phase",
         this.state,
         "reason",
         this.reason,
         "projectileId",
         this.projectileId,
         "ghastId",
         this.ghastId,
         "movementTicks",
         this.movementTicks,
         "serverConfirmedReflections",
         this.confirmedReflections,
         "ghastDeathsObserved",
         this.ghastDeaths,
         "missedAttempts",
         this.missedAttempts,
         "defensiveBats",
         this.defensiveBats,
         "outcomePending",
         this.update_I >= 0,
         "acknowledgementTicks",
         this.confirmTicks,
         "attackDelayTicks",
         this.reactionTicks,
         "threatToAcknowledgementTicks",
         this.reactionTicks + this.confirmTicks,
         "outcomeObservationTicks",
         this.outcomeTicks,
         "targetVelocity",
         vecJson(this.ghastMotion),
         "outcomes",
         this.init_JsonArray
      );
      out.add("rejections", AiJson.GSON.toJsonTree(this.rejections));
      if (this.plan != null) {
         out.add(
            "plan",
            AiJson.objectOf(
               "interceptTicks",
               this.plan.interceptTicks,
               "returnFlightTicks",
               this.plan.flight.ticks,
               "predictedGhast",
               vecJson(this.plan.flight.target),
               "aimDirection",
               vecJson(this.plan.flight.direction),
               "forward",
               this.plan.forward,
               "strafe",
               this.plan.strafe,
               "plannedTravel",
               this.plan.error
            )
         );
      }

      if (this.lastRejection != null) {
         out.add("attackGate", this.lastRejection);
      }

      return out;
   }

   private static JsonObject vecJson(Vec3 v) {
      return AiJson.objectOf("x", v.x, "y", v.y, "z", v.z);
   }

   private void releaseUse() {
      if (this.mc.player.isUsingItem()) {
         this.mc.gameMode.releaseUsingItem(this.mc.player);
      }

      this.mc.options.keyUse.setDown(false);
   }

   private void releaseMovementKeys() {
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

   void tick() {
      this.releaseMovementKeys();
   }

   record FireballFlight(Vec3 direction, int ticks, Vec3 target) {
   }

   private record InterceptPlan(AiGhastReflect.FireballFlight flight, int interceptTicks, int forward, int strafe, float yaw, double error) {
   }
}
