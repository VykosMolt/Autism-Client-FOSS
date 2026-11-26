package autismclient.modules;

import autismclient.mixin.accessor.AutismDangerProjectileAccessor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class IncomingProjectileScanner {
   static IncomingProjectileScanner.Inner_StreamUtil init(Entity entity) {
      if (entity instanceof Fireball) {
         return IncomingProjectileScanner.Inner_StreamUtil.FIREBALL;
      } else if (entity instanceof AbstractArrow) {
         return IncomingProjectileScanner.Inner_StreamUtil.ARROW;
      } else if (entity instanceof Snowball) {
         return IncomingProjectileScanner.Inner_StreamUtil.SNOWBALL;
      } else {
         return entity instanceof ThrownEnderpearl ? IncomingProjectileScanner.Inner_StreamUtil.PEARL : null;
      }
   }

   static IncomingProjectileScanner.Inner_StreamUtil init(ItemStack stack) {
      if (stack.is(Items.BOW) || stack.is(Items.CROSSBOW)) {
         return IncomingProjectileScanner.Inner_StreamUtil.ARROW;
      } else if (stack.is(Items.SNOWBALL)) {
         return IncomingProjectileScanner.Inner_StreamUtil.SNOWBALL;
      } else if (stack.is(Items.ENDER_PEARL)) {
         return IncomingProjectileScanner.Inner_StreamUtil.PEARL;
      } else {
         return stack.is(Items.FIRE_CHARGE) ? IncomingProjectileScanner.Inner_StreamUtil.FIREBALL : null;
      }
   }

   static IncomingProjectileScanner.Inner_CachePool init(Projectile projectile, IncomingProjectileScanner.Inner_StreamUtil kind) {
      boolean gravity = !projectile.isNoGravity();
      IncomingProjectileScanner.Inner_CachePool var10000;
      switch (kind) {
         case FIREBALL:
            var10000 = new IncomingProjectileScanner.Inner_CachePool(0.0, 0.949999988079071, 0.800000011920929, ((Fireball)projectile).accelerationPower, true);
            break;
         case ARROW:
            var10000 = new IncomingProjectileScanner.Inner_CachePool(gravity ? 0.05 : 0.0, 0.9900000095367432, 0.6000000238418579, 0.0, false);
            break;
         default:
            var10000 = new IncomingProjectileScanner.Inner_CachePool(gravity ? 0.03 : 0.0, 0.9900000095367432, 0.800000011920929, 0.0, true);
      }

      return var10000;
   }

   static IncomingProjectileScanner.Inner_DataUtil init(Level level, Projectile projectile, IncomingProjectileScanner.Inner_StreamUtil kind) {
      return init_A_DataUtil_A_ConfigStore(
         init_A_ConfigStore_Level(level, projectile, projectile.getOwner(), projectile.tickCount, projectile.getBbWidth()),
         projectile.position(),
         projectile.getDeltaMovement(),
         init(projectile, kind),
         120
      );
   }

   static IncomingProjectileScanner.Inner_DataUtil init(Level level, Player owner, IncomingProjectileScanner.Inner_StreamUtil kind, ProjectileBallistics.Inner_BufferHelper shot) {
      ProjectileBallistics.Inner_DataUtil info = shot.init_aX_DataUtil();
      Vec3 velocity = ProjectileBallistics.init(owner.getYRot(), owner.getXRot(), shot);
      if (info.init_Z()) {
         Vec3 inherited = ProjectileBallistics.init(owner);
         if (owner instanceof RemotePlayer && owner.tickCount > 1) {
            Vec3 observed = owner.position().subtract(new Vec3(owner.xOld, owner.yOld, owner.zOld));
            if (init(observed) && observed.lengthSqr() < 9.0) {
               inherited = new Vec3(observed.x, owner.onGround() ? 0.0 : observed.y, observed.z);
            }
         }

         velocity = velocity.add(inherited);
      }

      IncomingProjectileScanner.Inner_CachePool physics = kind == IncomingProjectileScanner.Inner_StreamUtil.FIREBALL
         ? new IncomingProjectileScanner.Inner_CachePool(0.0, 0.949999988079071, 0.800000011920929, 0.1, true)
         : new IncomingProjectileScanner.Inner_CachePool(info.init_D(), info.write(), info.update(), 0.0, kind != IncomingProjectileScanner.Inner_StreamUtil.ARROW);
      return init_A_DataUtil_A_ConfigStore(
         init_A_ConfigStore_Level(level, owner, owner, 0, kind == IncomingProjectileScanner.Inner_StreamUtil.FIREBALL ? 1.0 : 0.25),
         new Vec3(owner.getX(), owner.getEyeY() - 0.1, owner.getZ()),
         velocity,
         physics,
         120
      );
   }

   private static IncomingProjectileScanner.Inner_ConfigStore init_A_ConfigStore_Level(final Level level, final Entity source, final Entity owner, final int age, final double width) {
      final MutableBlockPos cursor = new MutableBlockPos();
      final Set<Entity> pierced = new HashSet<>();
      final int pierce = source instanceof AbstractArrow arrow ? arrow.getPierceLevel() : 0;
      return new IncomingProjectileScanner.Inner_ConfigStore() {
         @Override
         public boolean init(Vec3 at) {
            cursor.set(at.x, at.y, at.z);
            return at.y >= level.getMinY() && at.y <= level.getMaxY() && level.getChunkSource().hasChunk(cursor.getX() >> 4, cursor.getZ() >> 4);
         }

         @Override
         public boolean load(Vec3 at) {
            cursor.set(at.x, at.y, at.z);
            return level.getFluidState(cursor).is(FluidTags.WATER);
         }

         @Override
         public HitResult init(Vec3 from, Vec3 to, int tick) {
            HitResult block = level.clipIncludingBorder(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, source));
            Vec3 end = block.getType() == Type.MISS ? to : block.getLocation();
            AABB sweep = new AABB(from, end).inflate(width * 0.5 + 1.0);

            for (int pass = 0; pass <= pierce; pass++) {
               EntityHitResult entity = ProjectileUtil.getEntityHitResult(
                  level,
                  source,
                  from,
                  end,
                  sweep,
                  target -> !pierced.contains(target)
                     && (
                        source instanceof Projectile projectile
                           ? ((AutismDangerProjectileAccessor)projectile).autism$canHit(target)
                           : target != owner
                              && target.isAlive()
                              && target.canBeHitByProjectile()
                              && !target.isPassengerOfSameVehicle(owner)
                              && !target.noPhysics
                     ),
                  Math.max(0.0F, Math.min(0.3F, (age + tick - 2) / 20.0F))
               );
               if (entity == null) {
                  break;
               }

               if (pierced.size() >= pierce) {
                  return entity;
               }

               pierced.add(entity.getEntity());
            }

            return block.getType() == Type.MISS ? null : block;
         }
      };
   }

   static IncomingProjectileScanner.Inner_DataUtil init_A_DataUtil_A_ConfigStore(IncomingProjectileScanner.Inner_ConfigStore world, Vec3 position, Vec3 velocity, IncomingProjectileScanner.Inner_CachePool physics, int maxTicks) {
      if (init(position) && init(velocity)) {
         List<Vec3> points = new ArrayList<>(Math.min(maxTicks + 1, 121));
         points.add(position);

         for (int tick = 0; tick < Math.min(120, maxTicks) && world.init(position); tick++) {
            boolean water = world.load(position);
            if (physics.init_Z()) {
               velocity = init(velocity, physics, water);
            } else if (water) {
               velocity = velocity.scale(physics.read());
            }

            Vec3 next = position.add(velocity);
            if (!init(next) || !world.init(next)) {
               break;
            }

            HitResult hit = world.init(position, next, tick);
            if (hit != null) {
               points.add(hit.getLocation());
               double fraction = velocity.lengthSqr() < 1.0E-10 ? 0.0 : Math.min(1.0, position.distanceTo(hit.getLocation()) / velocity.length());
               return new IncomingProjectileScanner.Inner_DataUtil(List.copyOf(points), hit, position, tick + fraction);
            }

            points.add(next);
            position = next;
            if (!physics.init_Z()) {
               velocity = new Vec3(
                  velocity.x * (water ? 1.0 : physics.load()),
                  velocity.y * (water ? 1.0 : physics.load()) - physics.init_D(),
                  velocity.z * (water ? 1.0 : physics.load())
               );
            }
         }

         return new IncomingProjectileScanner.Inner_DataUtil(List.copyOf(points), null, null, points.size() - 1);
      } else {
         return new IncomingProjectileScanner.Inner_DataUtil(List.of(), null, null, 0.0);
      }
   }

   static Vec3 init(Vec3 velocity, IncomingProjectileScanner.Inner_CachePool physics, boolean water) {
      if (physics.write() != 0.0) {
         velocity = velocity.add(velocity.normalize().scale(physics.write()));
      }

      return velocity.add(0.0, -physics.init_D(), 0.0).scale(water ? physics.read() : physics.load());
   }

   static boolean init(IncomingProjectileScanner.Inner_DataUtil flight, IncomingProjectileScanner.Inner_StreamUtil kind, Vec3 player, double radius) {
      double range = radius;
      Vec3 landing = kind == IncomingProjectileScanner.Inner_StreamUtil.PEARL ? flight.load() : flight.init_Vec3();
      if (landing != null && landing.distanceToSqr(player) <= range * range) {
         return true;
      }

      if (kind != IncomingProjectileScanner.Inner_StreamUtil.PEARL && flight.init_List().size() >= 2) {
         List<Vec3> points = flight.init_List();
         double initial = points.getFirst().distanceToSqr(player);

         for (int i = 1; i < points.size(); i++) {
            Vec3 a = points.get(i - 1);
            Vec3 delta = points.get(i).subtract(a);
            double t = delta.lengthSqr() < 1.0E-10 ? 0.0 : Math.clamp(player.subtract(a).dot(delta) / delta.lengthSqr(), 0.0, 1.0);
            double distance = a.add(delta.scale(t)).distanceToSqr(player);
            if (distance <= range * range && distance + 0.01 < initial) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static boolean init(IncomingProjectileScanner.Inner_BufferHelper warning) {
      return warning != null
         && warning.init_A_StreamUtil() == IncomingProjectileScanner.Inner_StreamUtil.PEARL
         && init(warning.init_A_DataUtil())
         && warning.init_Player() != null
         && warning.init_Player().isAlive()
         && !warning.init_Entity().isRemoved();
   }

   static boolean init(IncomingProjectileScanner.Inner_DataUtil flight) {
      return flight != null && flight.init_Vec3() != null && flight.load() != null && init(flight.load());
   }

   private static boolean init(Vec3 v) {
      return Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z);
   }

   public record Inner_BufferHelper(
      Entity init_Entity, Player init_Player, IncomingProjectileScanner.Inner_StreamUtil init_A_StreamUtil, boolean init_Z, IncomingProjectileScanner.Inner_DataUtil init_A_DataUtil, int init_I
   ) {
      Vec3 init_Vec3() {
         return this.init_Entity.position().add(0.0, this.init_Z ? this.init_Entity.getEyeHeight() : this.init_Entity.getBbHeight() * 0.5, 0.0);
      }
   }

   record Inner_CachePool(double init_D, double load, double read, double write, boolean init_Z) {
   }

   interface Inner_ConfigStore {
      boolean init(Vec3 var1);

      boolean load(Vec3 var1);

      HitResult init(Vec3 var1, Vec3 var2, int var3);
   }

   public record Inner_DataUtil(List<Vec3> init_List, HitResult init_HitResult, Vec3 init_Vec3, double init_D) {
      public Vec3 init_Vec3() {
         return this.init_HitResult == null ? null : this.init_HitResult.getLocation();
      }

      public Vec3 load() {
         return this.init_Vec3;
      }
   }

   public enum Inner_StreamUtil {
      FIREBALL("fireball", "Fireballs"),
      ARROW("arrow", "Arrows"),
      SNOWBALL("snowball", "Snowballs"),
      PEARL("pearl", "Ender Pearls");

      private final String init_Str;
      private final String load_Str;
      private ItemStack init_ItemStack;

      Inner_StreamUtil(String key, String group) {
         this.init_Str = key;
         this.load_Str = group;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public String load() {
         return this.load_Str;
      }

      ItemStack init_ItemStack() {
         if (this.init_ItemStack == null) {
            Item var10003;
            switch (this) {
               case FIREBALL:
                  var10003 = Items.FIRE_CHARGE;
                  break;
               case ARROW:
                  var10003 = Items.ARROW;
                  break;
               case SNOWBALL:
                  var10003 = Items.SNOWBALL;
                  break;
               case PEARL:
                  var10003 = Items.ENDER_PEARL;
                  break;
               default:
                  throw new MatchException(null, null);
            }

            this.init_ItemStack = new ItemStack(var10003);
         }

         return this.init_ItemStack;
      }
   }
}
