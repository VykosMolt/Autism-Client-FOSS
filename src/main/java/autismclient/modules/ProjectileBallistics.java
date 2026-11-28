package autismclient.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class ProjectileBallistics {
   private static final ProjectileBallistics.ProjectileInfo GENERIC_THROWABLE = new ProjectileBallistics.ProjectileInfo(0.03, 0.125, 1.5, 0.9900000095367432, 0.800000011920929, 0.0F, true, true, false);
   private static final ProjectileBallistics.ProjectileInfo POTION = new ProjectileBallistics.ProjectileInfo(0.05, 0.125, 0.5, 0.9900000095367432, 0.800000011920929, -20.0F, true, true, false);
   private static final ProjectileBallistics.ProjectileInfo EXPERIENCE_BOTTLE = new ProjectileBallistics.ProjectileInfo(0.07, 0.125, 0.7, 0.9900000095367432, 0.800000011920929, -20.0F, true, true, false);
   private static final ProjectileBallistics.ProjectileInfo FISHING = new ProjectileBallistics.ProjectileInfo(0.03, 0.125, 1.1, 0.92, 0.92, 0.0F, false, true, true, ProjectileBallistics.ProjectileKind.FISHING);
   private static final ProjectileBallistics.ProjectileInfo TRIDENT = new ProjectileBallistics.ProjectileInfo(0.05, 0.25, 2.5, 0.9900000095367432, 0.9900000095367432, 0.0F, true, false, false);
   private static final ProjectileBallistics.ProjectileInfo BOW = new ProjectileBallistics.ProjectileInfo(0.05, 0.25, 3.0, 0.9900000095367432, 0.6000000238418579, 0.0F, true, false, false);
   private static final ProjectileBallistics.ProjectileInfo CROSSBOW_ARROW = new ProjectileBallistics.ProjectileInfo(
      0.05, 0.25, 3.1500000953674316, 0.9900000095367432, 0.6000000238418579, 0.0F, false, false, false
   );
   private static final ProjectileBallistics.ProjectileInfo CROSSBOW_ROCKET = new ProjectileBallistics.ProjectileInfo(
      0.0, 0.125, 1.600000023841858, 1.0, 1.0, 0.0F, false, false, false, ProjectileBallistics.ProjectileKind.ROCKET
   );
   private static final ProjectileBallistics.ProjectileInfo FIREBALL = new ProjectileBallistics.ProjectileInfo(
      0.0, 0.5, 1.5, 0.949999988079071, 0.800000011920929, 0.0F, true, true, false, ProjectileBallistics.ProjectileKind.FIREBALL
   );
   private static final ProjectileBallistics.ProjectileInfo WIND_CHARGE = new ProjectileBallistics.ProjectileInfo(0.0, 0.15625, 1.5, 1.0, 1.0, 0.0F, true, true, false, ProjectileBallistics.ProjectileKind.WIND);

   private ProjectileBallistics() {
   }

   public static Vec3 inheritedVelocity(Entity owner) {
      if (owner == null) {
         return Vec3.ZERO;
      }

      Vec3 delta = owner.getKnownMovement();
      return new Vec3(delta.x, owner.onGround() ? 0.0 : delta.y, delta.z);
   }

   static ProjectileBallistics.ProjectileInfo infoByName(String name) {
      ProjectileBallistics.ProjectileInfo var10000;
      switch (name) {
         case "fishing":
            var10000 = FISHING;
            break;
         case "generic":
            var10000 = GENERIC_THROWABLE;
            break;
         case "bow":
            var10000 = BOW;
            break;
         case "potion":
            var10000 = POTION;
            break;
         case "wind":
            var10000 = WIND_CHARGE;
            break;
         case "fireball":
            var10000 = FIREBALL;
            break;
         default:
            throw new IllegalArgumentException(name);
      }

      return var10000;
   }

   public static List<ProjectileBallistics.ProjectileShot> shots(Player player, ItemStack stack, boolean alwaysBow, boolean multiShot) {
      if (player == null || stack == null || stack.isEmpty()) {
         return List.of();
      }

      if (stack.getItem() instanceof BowItem) {
         boolean drawing = player.isUsingItem() && player.getUseItem() == stack;
         float power = drawing ? BowItem.getPowerForTime(player.getTicksUsingItem()) : (alwaysBow ? 1.0F : 0.0F);
         return power < 0.1F ? List.of() : List.of(new ProjectileBallistics.ProjectileShot(BOW.withVelocity(power * 3.0F), 0.0F));
      }

      if (stack.getItem() instanceof CrossbowItem) {
         ChargedProjectiles charged = stack.get(DataComponents.CHARGED_PROJECTILES);
         List<ItemStackTemplate> loaded = charged == null ? List.of() : charged.items();
         int count = Math.min(16, multiShot ? Math.max(1, loaded.size()) : 1);
         float[] spread = spreadAngles(count);
         List<ProjectileBallistics.ProjectileShot> result = new ArrayList<>(count);

         for (int i = 0; i < count; i++) {
            ItemStack ammo = loaded.isEmpty() ? ItemStack.EMPTY : loaded.get(i).create();
            if (loaded.isEmpty() || !ammo.isEmpty()) {
               boolean rocket = ammo.is(Items.FIREWORK_ROCKET);
               Fireworks fireworks = ammo.get(DataComponents.FIREWORKS);
               int ticks = rocket ? Math.min(240, 11 + 10 * (fireworks == null ? 0 : fireworks.flightDuration())) : 240;
               result.add(new ProjectileBallistics.ProjectileShot(rocket ? CROSSBOW_ROCKET : CROSSBOW_ARROW, spread[i], ticks));
            }
         }

         return List.copyOf(result);
      } else if (!(stack.getItem() instanceof TridentItem)
         || !stack.nextDamageWillBreak() && !(EnchantmentHelper.getTridentSpinAttackStrength(stack, player) > 0.0F)) {
         if (stack.getItem() instanceof FishingRodItem && player.fishing != null) {
            return List.of();
         }

         ProjectileBallistics.ProjectileInfo info = infoFor(stack);
         return info == null ? List.of() : List.of(new ProjectileBallistics.ProjectileShot(info, 0.0F));
      } else {
         return List.of();
      }
   }

   private static ProjectileBallistics.ProjectileInfo infoFor(ItemStack stack) {
      ProjectileBallistics.ProjectileInfo var10000;
      switch (stack.getItem()) {
         case FishingRodItem ignored:
            var10000 = FISHING;
            break;
         case ThrowablePotionItem ignored:
            var10000 = POTION;
            break;
         case TridentItem ignored:
            var10000 = TRIDENT;
            break;
         case SnowballItem ignored:
            var10000 = GENERIC_THROWABLE;
            break;
         case EnderpearlItem ignored:
            var10000 = GENERIC_THROWABLE;
            break;
         case EggItem ignored:
            var10000 = GENERIC_THROWABLE;
            break;
         case ExperienceBottleItem ignored:
            var10000 = EXPERIENCE_BOTTLE;
            break;
         case FireChargeItem ignored:
            var10000 = FIREBALL;
            break;
         case WindChargeItem ignored:
            var10000 = WIND_CHARGE;
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   static float[] spreadAngles(int count) {
      if (count <= 1) {
         return new float[]{0.0F};
      }

      float step = 20.0F / (count - 1);
      float offset = (count - 1) % 2 * step / 2.0F;
      float[] result = new float[count];

      for (int i = 0; i < count; i++) {
         result[i] = offset + (i % 2 == 0 ? 1 : -1) * ((i + 1) / 2) * step;
      }

      return result;
   }

   static Vec3 launchVelocity(float yaw, float pitch, ProjectileBallistics.ProjectileInfo info) {
      float yr = yaw * 0.017453292F;
      float pr = pitch * 0.017453292F;
      if (info.kind() == ProjectileBallistics.ProjectileKind.FISHING) {
         Vec3 direction = new Vec3(-Mth.sin(yr), Mth.clamp(-Math.tan(pr), -5.0, 5.0), Mth.cos(yr));
         return direction.scale(0.6 / direction.length() + 0.5);
      } else {
         return new Vec3(-Mth.sin(yr) * Mth.cos(pr), -Mth.sin((pitch + info.init_F()) * 0.017453292F), Mth.cos(yr) * Mth.cos(pr))
            .normalize()
            .scale(info.read_D());
      }
   }

   static Vec3 shotVelocity(float yaw, float pitch, ProjectileBallistics.ProjectileShot shot) {
      Vec3 velocity = launchVelocity(yaw, pitch, shot.info());
      if (shot.init_F() == 0.0F) {
         return velocity;
      }

      Vec3 up = Vec3.directionFromRotation(pitch - 90.0F, yaw);
      Vector3f rotated = velocity.toVector3f().rotate(new Quaternionf().setAngleAxis(shot.init_F() * 0.017453292F, up.x, up.y, up.z));
      return new Vec3(rotated);
   }

   static Vec3 launchStart(Entity owner, float yaw, ProjectileBallistics.ProjectileInfo info) {
      if (info.kind() == ProjectileBallistics.ProjectileKind.FISHING) {
         double angle = yaw * 0.017453292F;
         return new Vec3(owner.getX() - Math.sin(angle) * 0.3, owner.getEyeY(), owner.getZ() + Math.cos(angle) * 0.3);
      } else {
         double offset = info.kind() == ProjectileBallistics.ProjectileKind.WIND
            ? 0.0
            : (info.kind() == ProjectileBallistics.ProjectileKind.ROCKET ? 0.15000000596046448 : 0.10000000149011612);
         return new Vec3(owner.getX(), owner.getEyeY() - offset, owner.getZ());
      }
   }

   static Vec3 applyDrag(Vec3 velocity, ProjectileBallistics.ProjectileInfo info, boolean water) {
      double drag = water ? info.waterDrag() : info.airDrag();
      if (info.kind() == ProjectileBallistics.ProjectileKind.FIREBALL) {
         return velocity.add(velocity.normalize().scale(0.1)).scale(drag);
      } else {
         return info.kind() == ProjectileBallistics.ProjectileKind.ARROW
            ? velocity.scale(drag).add(0.0, -info.init_D(), 0.0)
            : velocity.add(0.0, -info.init_D(), 0.0).scale(drag);
      }
   }

   public static ProjectileBallistics.Trajectory simulate(Level level, Entity owner, ProjectileBallistics.ProjectileInfo info, float yaw, float pitch, int maxTicks, Vec3 inherited) {
      return simulate(level, owner, new ProjectileBallistics.ProjectileShot(info, 0.0F, maxTicks), yaw, pitch, inherited);
   }

   public static ProjectileBallistics.Trajectory simulate(Level level, Entity owner, ProjectileBallistics.ProjectileShot shot, float yaw, float pitch, Vec3 inherited) {
      if (level != null && owner != null) {
         Vec3 velocity = shotVelocity(yaw, pitch, shot);
         if (shot.info().init_Z() && inherited != null) {
            velocity = velocity.add(inherited);
         }

         return simulate(worldFor(level, owner, shot.info()), launchStart(owner, yaw, shot.info()), velocity, shot.info(), shot.init_I());
      } else {
         return new ProjectileBallistics.Trajectory(List.of(), null);
      }
   }

   static ProjectileBallistics.Trajectory simulate(ProjectileBallistics.TraceWorld world, Vec3 start, Vec3 velocity, ProjectileBallistics.ProjectileInfo info, int maxTicks) {
      List<Vec3> points = new ArrayList<>();
      if (isFinite(start) && isFinite(velocity) && world.isLoaded(start)) {
         Vec3 pos = start;
         points.add(start);

         for (int tick = 0; tick < Math.min(240, maxTicks); tick++) {
            boolean water = world.isWater(pos);
            Vec3 collisionVelocity = velocity;
            if (info.kind() == ProjectileBallistics.ProjectileKind.ARROW) {
               if (water) {
                  velocity = velocity.scale(info.waterDrag());
               }
            } else if (info.kind() == ProjectileBallistics.ProjectileKind.FISHING) {
               velocity = velocity.add(0.0, -info.init_D(), 0.0);
            } else {
               velocity = applyDrag(velocity, info, water);
               collisionVelocity = velocity;
            }

            Vec3 next = pos.add(info.kind() == ProjectileBallistics.ProjectileKind.ARROW ? collisionVelocity : velocity);
            if (!isFinite(next) || !world.isLoaded(next)) {
               break;
            }

            HitResult hit = world.clip(pos, pos.add(collisionVelocity), tick);
            if (hit != null && hit.getType() != Type.MISS) {
               points.add(hit.getLocation());
               return new ProjectileBallistics.Trajectory(List.copyOf(points), hit);
            }

            points.add(next);
            pos = next;
            if (info.kind() == ProjectileBallistics.ProjectileKind.ARROW) {
               if (!world.isWater(pos)) {
                  velocity = velocity.scale(info.airDrag());
               }

               velocity = velocity.add(0.0, -info.init_D(), 0.0);
            } else if (info.kind() == ProjectileBallistics.ProjectileKind.FISHING) {
               velocity = velocity.scale(info.airDrag());
            }
         }

         return new ProjectileBallistics.Trajectory(List.copyOf(points), null);
      } else {
         return new ProjectileBallistics.Trajectory(List.of(), null);
      }
   }

   private static ProjectileBallistics.TraceWorld worldFor(final Level level, final Entity owner, final ProjectileBallistics.ProjectileInfo info) {
      final MutableBlockPos cursor = new MutableBlockPos();
      return new ProjectileBallistics.TraceWorld() {
         @Override
         public boolean isLoaded(Vec3 at) {
            cursor.set(at.x, at.y, at.z);
            return at.y >= level.getMinY() && at.y <= level.getMaxY() && level.getChunkSource().hasChunk(cursor.getX() >> 4, cursor.getZ() >> 4);
         }

         @Override
         public boolean isWater(Vec3 at) {
            cursor.set(at.x, at.y, at.z);
            return level.getFluidState(cursor).is(FluidTags.WATER);
         }

         @Override
         public HitResult clip(Vec3 from, Vec3 to, int tick) {
            BlockHitResult block = level.clipIncludingBorder(
               new ClipContext(from, to, Block.COLLIDER, info.read_Z() ? Fluid.WATER : Fluid.NONE, CollisionContext.positionContext(from.y))
            );
            Vec3 end = block.getType() == Type.MISS ? to : block.getLocation();
            float margin = Math.max(0.0F, Math.min(0.3F, (tick - 2) / 20.0F));
            EntityHitResult entity = ProjectileUtil.getEntityHitResult(
               level,
               owner,
               from,
               end,
               new AABB(from, end).inflate(info.load_D() + 1.0),
               target -> !target.isSpectator()
                  && target.isAlive()
                  && !target.noPhysics
                  && target.canBeHitByProjectile()
                  && !owner.isPassengerOfSameVehicle(target),
               margin
            );
            return entity != null ? entity : (block.getType() == Type.MISS ? null : block);
         }
      };
   }

   private static boolean isFinite(Vec3 v) {
      return Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z);
   }

   public record ProjectileShot(ProjectileBallistics.ProjectileInfo info, float init_F, int init_I) {
      public ProjectileShot(ProjectileBallistics.ProjectileInfo info, float offset) {
         this(info, offset, 240);
      }
   }

   public record Trajectory(List<Vec3> points, HitResult hit) {
   }

   interface TraceWorld {
      boolean isLoaded(Vec3 var1);

      boolean isWater(Vec3 var1);

      HitResult clip(Vec3 var1, Vec3 var2, int var3);
   }

   public record ProjectileInfo(
      double init_D,
      double load_D,
      double read_D,
      double airDrag,
      double waterDrag,
      float init_F,
      boolean init_Z,
      boolean load_Z,
      boolean read_Z,
      ProjectileBallistics.ProjectileKind kind
   ) {
      public ProjectileInfo(
         double gravity,
         double hitboxRadius,
         double initialVelocity,
         double drag,
         double dragInWater,
         float roll,
         boolean copiesPlayerVelocity,
         boolean beforeMove,
         boolean stopsOnFluid
      ) {
         this(
            gravity,
            hitboxRadius,
            initialVelocity,
            drag,
            dragInWater,
            roll,
            copiesPlayerVelocity,
            beforeMove,
            stopsOnFluid,
            beforeMove ? ProjectileBallistics.ProjectileKind.THROWABLE : ProjectileBallistics.ProjectileKind.ARROW
         );
      }

      ProjectileBallistics.ProjectileInfo withVelocity(double velocity) {
         return new ProjectileBallistics.ProjectileInfo(
            this.init_D, this.load_D, velocity, this.airDrag, this.waterDrag, this.init_F, this.init_Z, this.load_Z, this.read_Z, this.kind
         );
      }
   }

   public enum ProjectileKind {
      THROWABLE,
      ARROW,
      FISHING,
      FIREBALL,
      WIND,
      ROCKET;
   }
}
