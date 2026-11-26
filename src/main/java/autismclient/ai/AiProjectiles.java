package autismclient.ai;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiProjectiles {
   static final double init_D = 0.05;
   static final double load_D = 0.03;
   static final double read = 0.05;
   static final double write = 0.07;
   static final double update = 0.06;
   static final double sync = 0.03;
   static final double flush = 0.99;
   static final double close = 0.95;
   static final double open = 0.8999999999999999;
   static final double poll = 32.0;
   static final double push = 5.0;
   static final double pop = 1.2;
   static final double tick = 2.5;
   static final int init_I = 1;
   private static List<AiProjectiles.Inner_StreamUtil> init_List = List.of();
   private static LocalPlayer init_LocalPlayer;
   private static Object init_Obj;
   private static int load_I = -2;

   private AiProjectiles() {
   }

   static boolean init_Z(Entity entity) {
      return entity instanceof AbstractArrow
         || entity instanceof AbstractHurtingProjectile
         || entity instanceof ThrowableProjectile
         || entity instanceof ShulkerBullet
         || entity instanceof LlamaSpit
         || entity instanceof FireworkRocketEntity;
   }

   static double init_D(Entity entity) {
      if (entity instanceof FireworkRocketEntity) {
         return 0.0;
      } else if (entity instanceof AbstractArrow) {
         return 0.05;
      } else if (entity instanceof AbstractThrownPotion) {
         return 0.05;
      } else if (entity instanceof ThrownExperienceBottle) {
         return 0.07;
      } else if (entity instanceof LlamaSpit) {
         return 0.06;
      } else {
         return !(entity instanceof ShulkerBullet) && !(entity instanceof AbstractHurtingProjectile) ? 0.03 : 0.0;
      }
   }

   static List<AiProjectiles.Inner_StreamUtil> init(Minecraft mc) {
      try {
         if (mc.player != null && mc.level != null) {
            int now = autismclient.util.AutismSessionState.init_ck().state_I();
            if (init_LocalPlayer == mc.player && init_Obj == mc.level && now - load_I < 1) {
               return init_List;
            }

            Vec3 eye = mc.player.getEyePosition();
            List<AiProjectiles.Inner_StreamUtil> out = new ArrayList<>();

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (init_Z(entity)
                  && !(entity.distanceToSqr(mc.player) > 1024.0)
                  && !(entity instanceof Projectile projectile && projectile.getOwner() == mc.player)) {
                  Vec3 velocity = entity.getDeltaMovement();
                  if (!(velocity.lengthSqr() < 1.0E-6)) {
                     double gravity = init_D(entity);
                     AiProjectiles.Inner_DataUtil prediction = init(mc, entity, mc.player.getBoundingBox(), 0);
                     out.add(
                        new AiProjectiles.Inner_StreamUtil(
                           entity.getId(),
                           BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(),
                           entity.getX(),
                           entity.getY(),
                           entity.getZ(),
                           velocity.x,
                           velocity.y,
                           velocity.z,
                           gravity,
                           prediction != null ? prediction.closestApproach() : entity.position().distanceTo(eye) - 0.8999999999999999,
                           prediction != null ? prediction.secondsToClosest() : -1.0
                        )
                     );
                  }
               }
            }

            List<AiProjectiles.Inner_StreamUtil> result = List.copyOf(out);
            init_List = result;
            init_LocalPlayer = mc.player;
            init_Obj = mc.level;
            load_I = now;
            return result;
         } else {
            return List.of();
         }
      } catch (Throwable t) {
         return List.of();
      }
   }

   static AiProjectiles.Inner_DataUtil init(Minecraft mc, Entity projectile, AABB destination, int travelTicks) {
      double gravity = init_D(projectile);
      Vec3 position = projectile.position();
      Vec3 velocity = projectile.getDeltaMovement();
      Vec3 bodyVelocity = mc.player.getDeltaMovement();
      Vec3 displacement = destination.getCenter().subtract(mc.player.getBoundingBox().getCenter());
      double best = 1.7976931348623157E308;
      int bestTick = 0;
      double hazardRadius = load(projectile);

      for (int tick = 1; tick <= 60; tick++) {
         boolean water = mc.level.getFluidState(BlockPos.containing(position)).is(FluidTags.WATER);
         if (projectile instanceof AbstractHurtingProjectile hurting) {
            double inertia = projectile instanceof AbstractWindCharge
               ? 1.0
               : (water ? 0.8 : (projectile instanceof WitherSkull skull && skull.isDangerous() ? 0.73 : 0.95));
            velocity = velocity.add(velocity.normalize().scale(hurting.accelerationPower)).scale(inertia);
         } else if (projectile instanceof ThrowableProjectile) {
            velocity = velocity.add(0.0, -gravity, 0.0).scale(water ? 0.8 : 0.99);
         }

         Vec3 next = position.add(velocity);
         Vec3 bodyOffset = travelTicks > 0 ? displacement.scale(Math.min(1.0, (double)tick / travelTicks)) : bodyVelocity.scale(Math.min(tick, 10));
         AABB body = mc.player.getBoundingBox().move(bodyOffset).inflate(0.3 + Math.min(0.8, tick * 0.015));
         BlockHitResult terrain = mc.level.clip(new ClipContext(position, next, Block.COLLIDER, Fluid.NONE, projectile));
         Vec3 endpoint = terrain.getType() == Type.BLOCK ? terrain.getLocation() : next;
         double clearance = init(body, position, endpoint);
         if (clearance < best) {
            best = clearance;
            bestTick = tick;
         }

         if (terrain.getType() == Type.BLOCK) {
            double impact = Math.sqrt(body.distanceToSqr(endpoint)) - hazardRadius;
            if (impact < best) {
               best = impact;
               bestTick = tick;
            }
            break;
         }

         if (clearance <= 0.0) {
            break;
         }

         if (projectile instanceof AbstractArrow) {
            double drag = water ? (projectile instanceof ThrownTrident ? 0.99 : 0.6) : 0.99;
            velocity = velocity.scale(drag).add(0.0, -gravity, 0.0);
         } else if (!(projectile instanceof AbstractHurtingProjectile) && !(projectile instanceof ThrowableProjectile)) {
            velocity = velocity.scale(gravity > 0.0 ? 0.99 : 1.0).add(0.0, -gravity, 0.0);
         }

         position = next;
      }

      return new AiProjectiles.Inner_DataUtil(best, bestTick / 20.0);
   }

   private static double init(AABB box, Vec3 start, Vec3 end) {
      if (!box.contains(start) && !box.clip(start, end).isPresent()) {
         Vec3 delta = end.subtract(start);
         double lo = 0.0;
         double hi = 1.0;

         for (int i = 0; i < 14; i++) {
            double a = lo + (hi - lo) / 3.0;
            double b = hi - (hi - lo) / 3.0;
            if (box.distanceToSqr(start.add(delta.scale(a))) < box.distanceToSqr(start.add(delta.scale(b)))) {
               hi = b;
            } else {
               lo = a;
            }
         }

         return Math.max(0.0, Math.sqrt(box.distanceToSqr(start.add(delta.scale((lo + hi) / 2.0)))) - 0.03);
      } else {
         return 0.0;
      }
   }

   static double load(Entity projectile) {
      String id = BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType()).getPath();
      double var10000;
      switch (id) {
         case "fireball":
         case "wither_skull":
         case "wind_charge":
         case "breeze_wind_charge":
            var10000 = 3.0;
            break;
         case "firework_rocket":
            var10000 = 5.0;
            break;
         case "dragon_fireball":
            var10000 = 4.0;
            break;
         case "splash_potion":
         case "lingering_potion":
            var10000 = 4.0;
            break;
         case "small_fireball":
            var10000 = 1.0;
            break;
         default:
            var10000 = 0.0;
      }

      return var10000;
   }

   static AiProjectiles.Inner_DataUtil init_by_DataUtil(Vec3 pos, Vec3 vel, double gravity, Vec3 target, double maxSeconds) {
      return init(pos, vel, gravity, 0.0, target, maxSeconds);
   }

   static AiProjectiles.Inner_DataUtil init(Vec3 pos, Vec3 vel, double gravity, double accel, Vec3 target, double maxSeconds) {
      int ticks = (int)Math.min(100.0, Math.ceil(Math.max(0.0, maxSeconds) * 20.0));
      double drag = gravity > 0.0 ? 0.99 : 1.0;
      double x = pos.x;
      double y = pos.y;
      double z = pos.z;
      double vx = vel.x;
      double vy = vel.y;
      double vz = vel.z;
      double best = Math.sqrt(init(x - target.x) + init(y - target.y) + init(z - target.z)) - 0.8999999999999999;
      double last = best;
      int bestTick = 0;
      boolean approachingAtHorizon = false;

      for (int tick = 1; tick <= ticks; tick++) {
         if (accel > 0.0) {
            double speed = Math.sqrt(vx * vx + vy * vy + vz * vz);
            if (speed > 1.0E-9) {
               vx = (vx + vx / speed * accel) * 0.95;
               vy = (vy + vy / speed * accel) * 0.95;
               vz = (vz + vz / speed * accel) * 0.95;
            }

            x += vx;
            y += vy;
            z += vz;
            double previous = last;
            last = Math.sqrt(init(x - target.x) + init(y - target.y) + init(z - target.z)) - 0.8999999999999999;
            if (last < best) {
               best = last;
               bestTick = tick;
            }

            if (tick == ticks) {
               approachingAtHorizon = last < previous;
            }
         } else {
            x += vx;
            y += vy;
            z += vz;
            vx *= drag;
            vy *= drag;
            vz *= drag;
            vy -= gravity;
            double var36 = last;
            last = Math.sqrt(init(x - target.x) + init(y - target.y) + init(z - target.z)) - 0.8999999999999999;
            if (last < best) {
               best = last;
               bestTick = tick;
            }

            if (tick == ticks) {
               approachingAtHorizon = last < var36;
            }
         }
      }

      if (bestTick == 0) {
         return null;
      } else {
         return bestTick == ticks && approachingAtHorizon ? null : new AiProjectiles.Inner_DataUtil(best, bestTick / 20.0);
      }
   }

   static boolean init(AiProjectiles.Inner_StreamUtil t) {
      return t.secondsToClosest() >= 0.0 && t.secondsToClosest() <= 2.5 && t.closestApproach() <= 1.2;
   }

   static boolean init(List<AiProjectiles.Inner_StreamUtil> scan) {
      for (AiProjectiles.Inner_StreamUtil t : scan) {
         if (init(t)) {
            return false;
         }
      }

      return true;
   }

   static Vec3 init_Vec3(Vec3 projPos, Vec3 projVel, double gravity, Vec3 playerEye, double secondsAvailable) {
      if (projVel.x * projVel.x + projVel.z * projVel.z < 1.0E-6) {
         double ax = playerEye.x - projPos.x;
         double az = playerEye.z - projPos.z;
         double length = Math.hypot(ax, az);
         return length < 0.001 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(ax / length, 0.0, az / length);
      } else {
         return init(projVel, init_I(projPos, projVel, gravity, playerEye, secondsAvailable));
      }
   }

   static int init_I(Vec3 projPos, Vec3 projVel, double gravity, Vec3 playerEye, double secondsAvailable) {
      double shift = Math.min(2.0, 5.6 * Math.max(0.0, secondsAvailable));
      AiProjectiles.Inner_DataUtil left = init_by_DataUtil(projPos, projVel, gravity, playerEye.add(init(projVel, 1).scale(shift)), 2.5);
      AiProjectiles.Inner_DataUtil right = init_by_DataUtil(projPos, projVel, gravity, playerEye.add(init(projVel, -1).scale(shift)), 2.5);
      double l = left == null ? 1.7976931348623157E308 : left.closestApproach();
      double r = right == null ? 1.7976931348623157E308 : right.closestApproach();
      if (l != r) {
         return l > r ? 1 : -1;
      }

      double denom = projVel.lengthSqr();
      double t = denom < 1.0E-12 ? 0.0 : Math.max(0.0, playerEye.subtract(projPos).dot(projVel) / denom);
      return init(projVel, 1).dot(playerEye.subtract(projPos.add(projVel.scale(t)))) >= 0.0 ? 1 : -1;
   }

   static Vec3 init(Vec3 projVel, int side) {
      double length = Math.hypot(projVel.x, projVel.z);
      return length < 1.0E-9 ? Vec3.ZERO : new Vec3(projVel.z * side / length, 0.0, -projVel.x * side / length);
   }

   private static double init(double v) {
      return v * v;
   }

   record Inner_DataUtil(double closestApproach, double secondsToClosest) {
   }

   record Inner_StreamUtil(
      int entityId, String type, double x, double y, double z, double vx, double vy, double vz, double gravity, double closestApproach, double secondsToClosest
   ) {
   }
}
