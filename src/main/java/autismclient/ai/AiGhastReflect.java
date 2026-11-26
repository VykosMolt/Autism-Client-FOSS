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
   private static final Map<UUID, AiGhastReflect> init_Map = new LinkedHashMap<>();
   private static final Map<UUID, JsonObject> load_Map = new LinkedHashMap<>();
   private final Minecraft init_Minecraft;
   private final Object init_Obj;
   private final Object load_Obj;
   private final Object read_Obj;
   private final Set<UUID> init_Set = new HashSet<>();
   private final Set<UUID> load_Set = new HashSet<>();
   private final Map<Integer, UUID> read_Map = new HashMap<>();
   private final Map<UUID, Integer> write_Map = new HashMap<>();
   private final JsonArray init_JsonArray = new JsonArray();
   private final Map<String, Integer> update_Map = new LinkedHashMap<>();
   private int read_I = -1;
   private int write_I = -1;
   private int update_I = -1;
   private int sync;
   private int flush;
   private int close;
   private int open;
   private int poll;
   private int push;
   private UUID init_UUID;
   private UUID load_UUID;
   private Vec3 init_Vec3;
   private Vec3 load_Vec3;
   private Vec3 read_Vec3 = Vec3.ZERO;
   private Vec3 write_Vec3;
   private int pop;
   private int tick;
   private int run;
   private int get;
   private int set;
   private long init_J;
   private boolean init_Z;
   private boolean load_Z;
   private AiGhastReflect.Inner_StreamUtil init_V_StreamUtil;
   private JsonObject init_JsonObject;
   private String init_Str = "idle";
   private String load_Str = "No incoming ghast fireball";

   AiGhastReflect(Minecraft mc) {
      this.init_Minecraft = mc;
      this.init_Obj = mc.level;
      this.load_Obj = mc.player;
      this.read_Obj = mc.getConnection();
   }

   boolean init_Z() {
      return this.read_I >= 0 && !this.init_Z;
   }

   int init_I() {
      return this.flush;
   }

   boolean load_Z() {
      return this.init_Z && this.update_I >= 0;
   }

   boolean init_Z_I(int id) {
      UUID identity = this.read_Map.get(id);
      Entity current = this.init_Minecraft.level == null ? null : this.init_Minecraft.level.getEntity(id);
      return identity != null && (current == null || current.getUUID().equals(identity));
   }

   static JsonObject init_JsonObject_UUID(UUID projectile) {
      AiGhastReflect active = init_Map.get(projectile);
      return active == null ? load_Map.get(projectile) : active.init_JsonObject();
   }

   static void init_V() {
      for (AiGhastReflect observer : List.copyOf(init_Map.values())) {
         observer.read();
      }
   }

   private void read() {
      if (this.update_I >= 0) {
         if (this.init_Minecraft.level == this.init_Obj
            && this.init_Minecraft.player == this.load_Obj
            && this.init_Minecraft.getConnection() == this.read_Obj
            && this.init_Minecraft.player.isAlive()) {
            Entity source = this.init_Minecraft.level.getEntity(this.write_I);
            Entity projectile = this.init_Minecraft.level.getEntity(this.read_I);
            Ghast ghast = source instanceof Ghast g && g.getUUID().equals(this.load_UUID) ? g : null;
            LargeFireball ball = projectile instanceof LargeFireball b && b.getUUID().equals(this.init_UUID) ? b : null;
            AiServerEvidence.Inner_StreamUtil motion = AiServerEvidence.lastEntityVelocity(this.read_I);
            if (!this.init_Z
               && motion != null
               && motion.identity().equals(this.init_UUID)
               && motion.revision() > this.init_J
               && init_Z_Vec3_3(this.init_Vec3, this.load_Vec3, motion.velocity())) {
               this.init_Z = true;
               this.flush++;
               this.read_Map.put(this.read_I, this.init_UUID);
               this.push = this.init_Minecraft.player.tickCount - this.update_I;
               if (this.load_Z) {
                  this.poll++;
               }

               this.init(
                  "returning", (this.load_Z ? "Server confirms defensive bat" : "Server confirms return") + " after " + this.push + " ticks; objective resumes"
               );
            }

            if (ghast != null && !ghast.isAlive()) {
               if (this.init_Z) {
                  this.close++;
               }

               this.load(this.init_Z ? "ghast_death_observed" : "death_without_motion_confirmation");
            } else {
               int age = this.init_Minecraft.player.tickCount - this.update_I;
               if (!this.init_Z && age >= 5) {
                  this.open++;
                  this.load("no_server_redirection; emergency_defense");
               } else if (this.init_Z && (ball == null || !ball.isAlive()) || age > this.tick + 8 || this.write_I >= 0 && ghast == null) {
                  if (this.init_Z && !this.load_Z && ghast != null && ghast.isAlive()) {
                     this.open++;
                  }

                  this.load(
                     this.write_I >= 0 && ghast == null
                        ? "target_unavailable"
                        : (this.load_Z ? "defensive_bat; no_observed_kill" : "returned_shot_ended_without_observed_kill")
                  );
               }
            }
         } else {
            this.load("world_or_player_changed; outcome_unknown");
         }
      }
   }

   String init_Str_List(List<AiProjectiles.IncomingProjectile> threats) {
      if (this.read_I < 0) {
         this.init_V_List(threats);
      }

      if (this.read_I < 0) {
         return null;
      }

      Entity source = this.init_Minecraft.level.getEntity(this.write_I);
      Entity projectile = this.init_Minecraft.level.getEntity(this.read_I);
      Ghast ghast = source instanceof Ghast g && g.getUUID().equals(this.load_UUID) ? g : null;
      LargeFireball ball = projectile instanceof LargeFireball b && b.getUUID().equals(this.init_UUID) ? b : null;
      if (this.update_I >= 0) {
         this.read();
         return this.init_Z() && threats.isEmpty() ? "Awaiting server fireball redirection" : null;
      }

      if (ball != null && ball.isAlive()) {
         if (ghast != null && ghast.isAlive()) {
            this.init_V_Ghast(ghast);
         }

         Vec3 eye = this.init_Minecraft.player.getEyePosition();
         if (threats.stream().noneMatch(t -> t.entityId() == this.read_I)) {
            this.load("fireball_no_longer_threatens_body; objective_resumes");
            return null;
         }

         this.init_V_StreamUtil = null;
         this.load_Z = false;
         if (this.init_Minecraft.player.isWithinAttackRange(this.init_Minecraft.player.getMainHandItem(), ball.getBoundingBox(), 0.0)
            && !this.init_Minecraft.player.cannotAttackWithItem(this.init_Minecraft.player.getMainHandItem(), 0)) {
            AiGhastReflect.Inner_DataUtil immediate = ghast != null && ghast.isAlive()
               ? init(
                  eye,
                  ball.position(),
                  ball.getBoundingBox().inflate(ball.getPickRadius()),
                  ghast.getBoundingBox(),
                  this.read_Vec3,
                  0,
                  AiDefense.init_D(this.init_Minecraft)
               )
               : null;
            if (immediate != null && this.init_Z_Vec3(ball.position(), immediate, ball)) {
               this.init_V_StreamUtil = new AiGhastReflect.Inner_StreamUtil(immediate, 0, 0, 0, init_F_Vec3(immediate.direction), 0.0);
            } else {
               AiGhastReflect.Inner_DataUtil bat = this.init_V_DataUtil_LargeFireball(ball);
               if (bat != null) {
                  this.load_Z = true;
                  this.init_V_StreamUtil = new AiGhastReflect.Inner_StreamUtil(bat, 0, 0, 0, init_F_Vec3(bat.direction), 0.0);
               }
            }
         }

         if (this.init_V_StreamUtil == null
            && !this.load_Set.contains(this.init_UUID)
            && ghast != null
            && ghast.isAlive()
            && this.init_Minecraft.player.onGround()
            && !this.init_Minecraft.player.isInWater()
            && !this.init_Minecraft.player.onClimbable()
            && this.init_Z_AABB(this.init_Minecraft.player.getBoundingBox())) {
            this.init_Minecraft.options.keySprint.setDown(false);
            this.init_Minecraft.player.setSprinting(false);
            this.init_V_StreamUtil = this.init_V_StreamUtil_LargeFireball(ball, ghast, threats);
         }

         if (this.init_V_StreamUtil == null) {
            this.init("evading", "No safe ghast return; shield/dodge until defensive hit is reachable: " + this.update_Map);
            this.load_Set.add(this.init_UUID);
            this.read_I = -1;
            return null;
         }

         AiGameOperations.snapLookAt(eye.add(this.init_V_StreamUtil.flight.direction.scale(16.0)));
         if (this.init_V_StreamUtil.interceptTicks == 0 && AiDefense.load(this.init_Minecraft, ball)) {
            autismclient.util.ServerRotationTracker.RotationSnapshot wire = autismclient.util.ServerRotationTracker.init_ch_CachePool();
            if (!wire.init_Z()) {
               return null;
            }

            Vec3 direction = Vec3.directionFromRotation(wire.write(), wire.read());
            if (!init_Z_Vec3(eye, direction, ball.getBoundingBox().inflate(ball.getPickRadius()), AiDefense.init_D(this.init_Minecraft))) {
               return null;
            }

            AiGhastReflect.Inner_DataUtil actual = init(ball.position(), direction, ghast == null ? null : ghast.getBoundingBox(), this.read_Vec3, this.load_Z);
            if (!this.load_Z && (actual == null || !this.init_Z_Vec3(ball.position(), actual, ball))) {
               AiGhastReflect.Inner_DataUtil bat = init(ball.position(), direction, null, Vec3.ZERO, true);
               if (bat != null
                  && ball.getDeltaMovement().normalize().dot(direction) < 0.98
                  && this.init_Z_Vec3(ball.position(), bat, ball)
                  && this.init(ball, direction)) {
                  actual = bat;
                  this.load_Z = true;
               }
            }

            if (actual != null && this.init_Z_Vec3(ball.position(), actual, ball) && (!this.load_Z || this.init(ball, direction))) {
               this.write();
               this.update();
               this.init_Minecraft.options.keySprint.setDown(false);
               this.init_Vec3 = ball.getDeltaMovement();
               this.load_Vec3 = direction;
               AiServerEvidence.Inner_StreamUtil evidence = AiServerEvidence.lastEntityVelocity(this.read_I);
               this.init_J = evidence == null ? 0L : evidence.revision();
               this.update_I = this.init_Minecraft.player.tickCount;
               this.tick = actual.ticks;
               this.get = this.update_I - this.run;
               this.init_Set.add(this.init_UUID);
               init_Map.put(this.init_UUID, this);

               while (init_Map.size() > 16) {
                  init_Map.values().iterator().next().load("observation_capacity; outcome_unknown");
               }

               this.init_Minecraft.gameMode.attack(this.init_Minecraft.player, ball);
               Compat263.swing(this.init_Minecraft.player, InteractionHand.MAIN_HAND);
               this.init(
                  "struck",
                  (this.load_Z ? "Batting threatening fireball away; no kill solution required" : "Returning the fireball along the predicted ghast intercept")
                     + "; "
                     + this.get
                     + " ticks since threat acquisition"
               );
               return this.load_Str;
            } else {
               return this.init_Str();
            }
         } else if (this.init_V_StreamUtil.interceptTicks == 0) {
            if (this.init_Minecraft.player.cannotAttackWithItem(this.init_Minecraft.player.getMainHandItem(), 0)) {
               return null;
            }

            if (this.init_Minecraft.player.isWithinAttackRange(this.init_Minecraft.player.getMainHandItem(), ball.getBoundingBox(), 0.0)
               && init_Z_Vec3(eye, this.init_V_StreamUtil.flight.direction, ball.getBoundingBox().inflate(ball.getPickRadius()), AiDefense.init_D(this.init_Minecraft))
               )
             {
               return this.init_Str();
            }

            this.init_JsonObject = AiJson.objectOf(
               "alive",
               ball.isAlive(),
               "charged",
               !this.init_Minecraft.player.cannotAttackWithItem(this.init_Minecraft.player.getMainHandItem(), 0),
               "range",
               this.init_Minecraft.player.isWithinAttackRange(this.init_Minecraft.player.getMainHandItem(), ball.getBoundingBox(), 0.0),
               "ray",
               autismclient.ai.engine.AiAim.isLookingAtEntity(ball),
               "eye",
               init_JsonObject_Vec3(this.init_Minecraft.player.getEyePosition()),
               "look",
               init_JsonObject_Vec3(this.init_Minecraft.player.getLookAngle()),
               "intended",
               init_JsonObject_Vec3(this.init_V_StreamUtil.flight.direction),
               "projectileBox",
               ball.getBoundingBox().toString(),
               "reach",
               AiDefense.init_D(this.init_Minecraft)
            );
            AiSpeedrun.init_V_Str_ObjArr("[AI] ghast reflection rejected attack: {}", this.init_JsonObject);
            this.init_Set.add(this.init_UUID);
            this.load("live_attack_ray_or_range_rejected; emergency defense");
            return null;
         } else {
            if (this.init_Minecraft.player.isUsingItem()) {
               this.write();
               this.update();
               return "Preparing interception; rechecking movement after shield slowdown";
            }

            this.update();
            this.init_Minecraft.options.keySprint.setDown(false);
            this.init_Minecraft.options.keyUp.setDown(this.init_V_StreamUtil.forward > 0);
            this.init_Minecraft.options.keyDown.setDown(this.init_V_StreamUtil.forward < 0);
            this.init_Minecraft.options.keyLeft.setDown(this.init_V_StreamUtil.strafe > 0);
            this.init_Minecraft.options.keyRight.setDown(this.init_V_StreamUtil.strafe < 0);
            if (this.init_V_StreamUtil.forward != 0 || this.init_V_StreamUtil.strafe != 0) {
               this.sync++;
            }

            this.init("aligning", "Moving into the fireball's predicted return line");
            return this.load_Str;
         }
      } else {
         this.load("incoming_shot_unavailable");
         return null;
      }
   }

   private String init_Str() {
      this.update();
      this.init("facing", "Publishing the defensive look through ordinary movement before striking");
      return this.load_Str;
   }

   private boolean init(LargeFireball ball, Vec3 direction) {
      for (Entity entity : this.init_Minecraft.level.entitiesForRendering()) {
         if (entity != this.init_Minecraft.player
            && entity instanceof LivingEntity
            && entity.isAlive()
            && init(ball.position(), direction, entity.getBoundingBox(), init_Vec3_Entity(entity), 0) > 0) {
            return false;
         }
      }

      return true;
   }

   private void init_V_List(List<AiProjectiles.IncomingProjectile> threats) {
      AiProjectiles.IncomingProjectile first = null;

      for (AiProjectiles.IncomingProjectile threat : threats) {
         if (this.init_Minecraft.level.getEntity(threat.entityId()) instanceof LargeFireball ball
            && ball.isAlive()
            && !this.init_Set.contains(ball.getUUID())
            && ball.is(EntityTypeTags.REDIRECTABLE_PROJECTILE)
            && (first == null || threat.secondsToClosest() < first.secondsToClosest())) {
            first = threat;
         }
      }

      if (first != null) {
         LargeFireball ball = (LargeFireball)this.init_Minecraft.level.getEntity(first.entityId());
         Ghast ghast = ball.getOwner() instanceof Ghast g && g.isAlive() && g.distanceToSqr(this.init_Minecraft.player) <= 4096.0 ? g : null;
         this.read_I = ball.getId();
         this.init_UUID = ball.getUUID();
         this.write_I = ghast == null ? -1 : ghast.getId();
         this.load_UUID = ghast == null ? null : ghast.getUUID();
         this.run = this.write_Map.computeIfAbsent(this.init_UUID, ignored -> this.init_Minecraft.player.tickCount);
         this.push = this.get = this.set = 0;
         this.update_I = -1;
         this.init_Z = false;
         this.init_V_StreamUtil = null;
         this.write_Vec3 = null;
         this.read_Vec3 = ghast == null ? Vec3.ZERO : init_Vec3_Entity(ghast);
         if (!this.load_Set.contains(this.init_UUID)) {
            this.init("acquired", "Predicting incoming fireball and moving ghast");
         }
      }
   }

   private AiGhastReflect.Inner_DataUtil init_V_DataUtil_LargeFireball(LargeFireball ball) {
      Vec3 eye = this.init_Minecraft.player.getEyePosition();
      AABB pick = ball.getBoundingBox().inflate(ball.getPickRadius());
      AiGhastReflect.Inner_DataUtil best = null;
      double score = -1.7976931348623157E308;

      for (Vec3 direction : init_List_Vec3(eye, pick, AiDefense.init_D(this.init_Minecraft))) {
         if (this.init_Z_Vec3_2(eye, eye.add(direction.scale(AiDefense.init_D(this.init_Minecraft))), ball)) {
            Vec3 position = ball.position();
            Vec3 velocity = direction;
            boolean safe = true;

            for (int tick = 1; tick <= 8; tick++) {
               velocity = velocity.add(velocity.normalize().scale(0.1)).scale(0.949999988079071);
               Vec3 next = position.add(velocity);
               if (!this.init_Z_Vec3(next) || !this.init_Z_Vec3_2(position, next, ball)) {
                  safe = false;
                  break;
               }

               position = next;
               if (position.distanceToSqr(this.init_Minecraft.player.getBoundingBox().getCenter()) > 36.0) {
                  break;
               }
            }

            if (safe) {
               double candidate = direction.y;
               if (candidate > score) {
                  score = candidate;
                  best = new AiGhastReflect.Inner_DataUtil(direction, 24, position);
               }
            }
         }
      }

      return best;
   }

   static List<Vec3> init_List_Vec3(Vec3 eye, AABB pick, double reach) {
      ArrayList<Vec3> directions = new ArrayList<>();

      for (int x : new int[]{0, -1, 1}) {
         for (int y : new int[]{1, 0, -1}) {
            for (int z : new int[]{0, -1, 1}) {
               Vec3 point = pick.getCenter().add(x * pick.getXsize() * 0.32, y * pick.getYsize() * 0.32, z * pick.getZsize() * 0.32);
               Vec3 direction = point.subtract(eye).normalize();
               if (init_Z_Vec3(eye, direction, pick, reach)) {
                  directions.add(direction);
               }
            }
         }
      }

      return directions;
   }

   private void init_V_Ghast(Ghast ghast) {
      int tick = this.init_Minecraft.player.tickCount;
      Vec3 motion = init_Vec3_Entity(ghast);
      if (this.write_Vec3 != null && tick > this.pop) {
         Vec3 observed = ghast.position().subtract(this.write_Vec3).scale(1.0 / (tick - this.pop));
         if (observed.lengthSqr() < 1.0) {
            motion = observed;
         }
      }

      this.read_Vec3 = motion;
      this.write_Vec3 = ghast.position();
      this.pop = tick;
   }

   private static Vec3 init_Vec3_Entity(Entity target) {
      Vec3 observed = new Vec3(target.getX() - target.xo, target.getY() - target.yo, target.getZ() - target.zo);
      if (observed.lengthSqr() < 1.0E-8) {
         observed = target.getDeltaMovement();
      }

      return observed.lengthSqr() <= 1.0 ? observed : Vec3.ZERO;
   }

   private AiGhastReflect.Inner_StreamUtil init_V_StreamUtil_LargeFireball(LargeFireball ball, Ghast ghast, List<AiProjectiles.IncomingProjectile> threats) {
      this.update_Map.clear();
      Vec3 eye = this.init_Minecraft.player.getEyePosition();
      double reach = AiDefense.init_D(this.init_Minecraft);
      AiGhastReflect.Inner_DataUtil now = init(eye, ball.position(), ball.getBoundingBox().inflate(ball.getPickRadius()), ghast.getBoundingBox(), this.read_Vec3, 0, reach);
      if (now != null
         && this.init_Minecraft.player.isWithinAttackRange(this.init_Minecraft.player.getMainHandItem(), ball.getBoundingBox(), 0.0)
         && this.init_Z_Vec3(ball.position(), now, ball)
         && autismclient.ai.engine.AiCombatMotion.init(this.init_Minecraft, 0.0, 0.0, 4) != null
         && this.init_Z_LargeFireball(ball, this.init_Minecraft.player.getBoundingBox(), 0, threats)) {
         return new AiGhastReflect.Inner_StreamUtil(now, 0, 0, 0, init_F_Vec3(now.direction), 0.0);
      }

      AiGhastReflect.Inner_DataUtil lead = init_V_DataUtil_Vec3(ball.position(), ghast.getBoundingBox(), this.read_Vec3, 0);
      if (lead == null) {
         this.init_V_Str("target_beyond_prediction_horizon");
         return null;
      }

      float facing = init_F_Vec3(lead.direction);
      Vec3 position = ball.position();
      Vec3 velocity = ball.getDeltaMovement();
      Vec3[] inbound = new Vec3[33];
      inbound[0] = position;

      for (int tick = 1; tick <= 32; tick++) {
         boolean water = this.init_Minecraft.level.getFluidState(BlockPos.containing(position)).is(FluidTags.WATER);
         velocity = velocity.add(velocity.normalize().scale(ball.accelerationPower)).scale(water ? 0.800000011920929 : 0.949999988079071);
         Vec3 next = position.add(velocity);
         if (!this.init_Z_Vec3(next) || !this.init_Z_Vec3_2(position, next, ball)) {
            this.init_V_Str("incoming_terrain_or_unloaded");
            break;
         }

         inbound[tick] = next;
         position = next;
      }

      AiGhastReflect.Inner_StreamUtil best = null;

      for (int forward = -1; forward <= 1; forward++) {
         for (int strafe = -1; strafe <= 1; strafe++) {
            for (int horizon : new int[]{4, 8, 12}) {
               List<AABB> trace = autismclient.ai.engine.AiCombatMotion.init(this.init_Minecraft, facing, forward, strafe, horizon, 4);
               if (trace != null && !trace.stream().anyMatch(bodyx -> !this.init_Z_AABB(bodyx))) {
                  for (int tick = 1; tick <= 32 && inbound[tick] != null; tick++) {
                     AABB body = trace.get(Math.min(tick - 1, trace.size() - 1));
                     AABB previous = tick == 1 ? this.init_Minecraft.player.getBoundingBox() : trace.get(Math.min(tick - 2, trace.size() - 1));
                     if (body.inflate(0.15).contains(inbound[tick]) || previous.inflate(0.15).clip(inbound[tick - 1], inbound[tick]).isPresent()) {
                        this.init_V_Str("incoming_body_collision_before_attack");
                        break;
                     }

                     Vec3 shift = body.getCenter().subtract(this.init_Minecraft.player.getBoundingBox().getCenter());
                     Vec3 futureEye = eye.add(shift);
                     AABB actualBall = ball.getBoundingBox().move(inbound[tick].subtract(ball.position()));
                     if (actualBall.distanceToSqr(futureEye) > (reach - 0.2) * (reach - 0.2)) {
                        this.init_V_Str("outside_physical_attack_range");
                     } else {
                        AABB futureBall = actualBall.inflate(ball.getPickRadius());
                        AiGhastReflect.Inner_DataUtil flight = init(futureEye, inbound[tick], futureBall, ghast.getBoundingBox(), this.read_Vec3, tick, reach - 0.2);
                        if (flight == null) {
                           this.init_V_Str("no_common_attack_and_ghast_intercept_ray");
                        } else if (!this.init_Z_Vec3_2(futureEye, futureEye.add(flight.direction.scale(reach)), ball)) {
                           this.init_V_Str("attack_ray_blocked");
                        } else if (!this.init_Z_Vec3(inbound[tick], flight, ball)) {
                           this.init_V_Str("returned_shot_terrain_water_or_unloaded");
                        } else {
                           if (this.init_Z_LargeFireball(ball, body, tick, threats)) {
                              double error = shift.horizontalDistance();
                              if (best == null || tick < best.interceptTicks || tick == best.interceptTicks && error < best.error) {
                                 best = new AiGhastReflect.Inner_StreamUtil(flight, tick, forward, strafe, facing, error);
                              }
                              break;
                           }

                           this.init_V_Str("competing_threat_before_return");
                        }
                     }
                  }

                  if (best != null && best.interceptTicks <= horizon) {
                     break;
                  }
               } else {
                  this.init_V_Str("unsafe_footing_or_coast");
               }
            }
         }
      }

      return best;
   }

   private void init_V_Str(String reason) {
      this.update_Map.merge(reason, 1, Integer::sum);
   }

   private boolean init_Z_LargeFireball(LargeFireball ball, AABB landing, int ticks, List<AiProjectiles.IncomingProjectile> threats) {
      for (AiProjectiles.IncomingProjectile threat : threats) {
         if (threat.entityId() != ball.getId()) {
            Entity other = this.init_Minecraft.level.getEntity(threat.entityId());
            if (other != null) {
               AiProjectiles.ApproachMetrics danger = AiProjectiles.init(this.init_Minecraft, other, landing, ticks);
               if (danger != null && danger.closestApproach() <= 0.5 && danger.secondsToClosest() * 20.0 <= ticks + 5) {
                  return false;
               }
            }
         }
      }

      for (Entity mob : this.init_Minecraft.level.entitiesForRendering()) {
         if (mob instanceof LivingEntity
            && mob != this.init_Minecraft.player
            && AiThreats.init_Z(this.init_Minecraft, mob)
            && landing.intersects(mob.getBoundingBox().inflate(1.2))) {
            return false;
         }
      }

      return true;
   }

   static AiGhastReflect.Inner_DataUtil init(Vec3 eye, Vec3 launch, AABB ball, AABB target, Vec3 motion, int delay, double reach) {
      if (ball.distanceToSqr(eye) > reach * reach) {
         return null;
      }

      AiGhastReflect.Inner_DataUtil centre = init_V_DataUtil_Vec3(launch, target, motion, delay);
      if (centre == null) {
         return null;
      }

      if (init_Z_Vec3(eye, centre.direction, ball, reach)) {
         return centre;
      }

      Vec3 middle = ball.getCenter();

      for (int x : new int[]{0, -1, 1}) {
         for (int y : new int[]{0, -1, 1}) {
            for (int z : new int[]{0, -1, 1}) {
               Vec3 point = middle.add(x * ball.getXsize() * 0.32, y * ball.getYsize() * 0.32, z * ball.getZsize() * 0.32);
               Vec3 direction = point.subtract(eye).normalize();
               if (init_Z_Vec3(eye, direction, ball, reach)) {
                  int hit = init(launch, direction, target, motion, delay);
                  if (hit > 0) {
                     return new AiGhastReflect.Inner_DataUtil(direction, hit, target.getCenter().add(motion.scale(delay + hit)));
                  }
               }
            }
         }
      }

      return null;
   }

   static AiGhastReflect.Inner_DataUtil init_V_DataUtil_Vec3(Vec3 launch, AABB target, Vec3 motion, int delay) {
      double distance = 0.0;
      double speed = 1.0;

      for (int tick = 1; tick <= 60; tick++) {
         speed = (speed + 0.1) * 0.949999988079071;
         distance += speed;
         Vec3 point = target.getCenter().add(motion.scale(delay + tick));
         if (!(distance < point.distanceTo(launch))) {
            Vec3 direction = point.subtract(launch).normalize();
            int hit = init(launch, direction, target, motion, delay);
            return hit > 0 ? new AiGhastReflect.Inner_DataUtil(direction, hit, point) : null;
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

   static AiGhastReflect.Inner_DataUtil init(Vec3 launch, Vec3 direction, AABB target, Vec3 motion, boolean defensive) {
      if (direction == null || direction.lengthSqr() < 0.9) {
         return null;
      }

      if (defensive) {
         return new AiGhastReflect.Inner_DataUtil(direction, 24, launch.add(direction.scale(24.0)));
      }

      if (target == null) {
         return null;
      }

      int ticks = init(launch, direction, target, motion, 0);
      return ticks > 0 ? new AiGhastReflect.Inner_DataUtil(direction, ticks, target.getCenter().add(motion.scale(ticks))) : null;
   }

   static boolean init_Z_Vec3(Vec3 eye, Vec3 direction, AABB ball, double reach) {
      return direction.lengthSqr() > 0.9 && !ball.contains(eye) && ball.clip(eye, eye.add(direction.scale(reach))).isPresent();
   }

   static boolean init_Z_Vec3_3(Vec3 incoming, Vec3 sent, Vec3 applied) {
      return applied.lengthSqr() > 0.01 && applied.normalize().dot(sent) > 0.98 && incoming.normalize().dot(sent) < 0.98;
   }

   private static float init_F_Vec3(Vec3 d) {
      return (float)(Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0);
   }

   private boolean init_Z_Vec3(Vec3 position, AiGhastReflect.Inner_DataUtil flight, Entity ball) {
      Vec3 velocity = flight.direction;

      for (int tick = 1; tick <= flight.ticks; tick++) {
         if (this.init_Minecraft.level.getFluidState(BlockPos.containing(position)).is(FluidTags.WATER)) {
            return false;
         }

         velocity = velocity.add(velocity.normalize().scale(0.1)).scale(0.949999988079071);
         Vec3 next = position.add(velocity);
         if (!this.init_Z_Vec3(next) || !this.init_Z_Vec3_2(position, next, ball)) {
            return false;
         }

         position = next;
      }

      return true;
   }

   private boolean init_Z_Vec3(Vec3 point) {
      return AiTerrain.isLoaded(this.init_Minecraft.level, BlockPos.containing(point));
   }

   private boolean init_Z_AABB(AABB body) {
      for (double x : new double[]{body.minX + 0.04, body.maxX - 0.04}) {
         for (double z : new double[]{body.minZ + 0.04, body.maxZ - 0.04}) {
            AABB foot = new AABB(x - 0.01, body.minY - 0.03, z - 0.01, x + 0.01, body.minY, z + 0.01);
            if (!this.init_Minecraft.level.getBlockCollisions(this.init_Minecraft.player, foot).iterator().hasNext()) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean init_Z_Vec3_2(Vec3 from, Vec3 to, Entity context) {
      return this.init_Minecraft.level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, context)).getType() == Type.MISS;
   }

   private void init(String next, String why) {
      if (!this.init_Str.equals(next)) {
         AiSpeedrun.init_V_Str_ObjArr("[AI] ghast reflection: {} ball={} ghast={} ({})", next, this.read_I, this.write_I, why);
      }

      this.init_Str = next;
      this.load_Str = why;
   }

   private void load(String outcome) {
      if (this.update_I >= 0 && this.init_Minecraft.player == this.load_Obj) {
         this.set = this.init_Minecraft.player.tickCount - this.update_I;
      }

      if (this.init_JsonArray.size() < 8) {
         this.init_JsonArray
            .add(
               AiJson.objectOf(
                  "projectileId",
                  this.read_I,
                  "ghastId",
                  this.write_I,
                  "serverRedirected",
                  this.init_Z,
                  "outcome",
                  outcome,
                  "strikeTick",
                  this.update_I,
                  "attackDelayTicks",
                  this.get,
                  "acknowledgementTicks",
                  this.push,
                  "outcomeObservationTicks",
                  this.set
               )
            );
      }

      this.init("resolved", outcome);
      this.read_I = -1;
      this.update_I = -1;
      this.init_V_StreamUtil = null;
      if (this.init_UUID != null) {
         init_Map.remove(this.init_UUID);
         load_Map.put(this.init_UUID, this.init_JsonObject());

         while (load_Map.size() > 32) {
            load_Map.remove(load_Map.keySet().iterator().next());
         }
      }
   }

   JsonObject init_JsonObject() {
      JsonObject out = AiJson.objectOf(
         "phase",
         this.init_Str,
         "reason",
         this.load_Str,
         "projectileId",
         this.read_I,
         "ghastId",
         this.write_I,
         "movementTicks",
         this.sync,
         "serverConfirmedReflections",
         this.flush,
         "ghastDeathsObserved",
         this.close,
         "missedAttempts",
         this.open,
         "defensiveBats",
         this.poll,
         "outcomePending",
         this.update_I >= 0,
         "acknowledgementTicks",
         this.push,
         "attackDelayTicks",
         this.get,
         "threatToAcknowledgementTicks",
         this.get + this.push,
         "outcomeObservationTicks",
         this.set,
         "targetVelocity",
         init_JsonObject_Vec3(this.read_Vec3),
         "outcomes",
         this.init_JsonArray
      );
      out.add("rejections", AiJson.GSON.toJsonTree(this.update_Map));
      if (this.init_V_StreamUtil != null) {
         out.add(
            "plan",
            AiJson.objectOf(
               "interceptTicks",
               this.init_V_StreamUtil.interceptTicks,
               "returnFlightTicks",
               this.init_V_StreamUtil.flight.ticks,
               "predictedGhast",
               init_JsonObject_Vec3(this.init_V_StreamUtil.flight.target),
               "aimDirection",
               init_JsonObject_Vec3(this.init_V_StreamUtil.flight.direction),
               "forward",
               this.init_V_StreamUtil.forward,
               "strafe",
               this.init_V_StreamUtil.strafe,
               "plannedTravel",
               this.init_V_StreamUtil.error
            )
         );
      }

      if (this.init_JsonObject != null) {
         out.add("attackGate", this.init_JsonObject);
      }

      return out;
   }

   private static JsonObject init_JsonObject_Vec3(Vec3 v) {
      return AiJson.objectOf("x", v.x, "y", v.y, "z", v.z);
   }

   private void write() {
      if (this.init_Minecraft.player.isUsingItem()) {
         this.init_Minecraft.gameMode.releaseUsingItem(this.init_Minecraft.player);
      }

      this.init_Minecraft.options.keyUse.setDown(false);
   }

   private void update() {
      for (KeyMapping key : List.of(
         this.init_Minecraft.options.keyUp,
         this.init_Minecraft.options.keyDown,
         this.init_Minecraft.options.keyLeft,
         this.init_Minecraft.options.keyRight,
         this.init_Minecraft.options.keySprint
      )) {
         key.setDown(autismclient.util.KeyMappingControl.of(key).autism$physicalForBody());
      }
   }

   void load_V() {
      this.update();
   }

   record Inner_DataUtil(Vec3 direction, int ticks, Vec3 target) {
   }

   private record Inner_StreamUtil(AiGhastReflect.Inner_DataUtil flight, int interceptTicks, int forward, int strafe, float yaw, double error) {
   }
}
