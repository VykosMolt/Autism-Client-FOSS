package autismclient.modules;

import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class aU {
   static final int init = -10099317;
   static final int load = -12971;
   static final int read = -44976;

   private aU() {
   }

   static aU.Inner_StreamUtil init(aU.Inner_CachePool world, AABB body, Vec3 velocity, double gravity) {
      double fallen = 0.0;
      int settled = 0;
      int impactTick = 2147483647;

      for (int tick = 0; tick < 240; tick++) {
         if (!world.init_Z(body.expandTowards(velocity))) {
            return new aU.Inner_StreamUtil(aU.Inner_DataUtil.UNKNOWN, fallen, 1.0F);
         }

         aU.Inner_DataUtil contact = world.init_aU_DataUtil(body);
         if (contact != null) {
            return new aU.Inner_StreamUtil(contact, fallen, 0.0F);
         }

         Vec3 moved = world.init(body, velocity);
         body = body.move(moved);
         fallen += Math.max(0.0, -moved.y);
         contact = world.init_aU_DataUtil(body);
         if (contact != null) {
            return new aU.Inner_StreamUtil(contact, fallen, 0.0F);
         }

         boolean grounded = velocity.y < 0.0 && moved.y > velocity.y + 1.0E-7;
         if (grounded && settled++ == 0) {
            impactTick = tick + 1;
         }

         if (grounded && velocity.horizontalDistance() < 1.0E-4) {
            return new aU.Inner_StreamUtil(aU.Inner_DataUtil.LAND, fallen, world.init_F(body), impactTick);
         }

         if (!grounded) {
            settled = 0;
         }

         if (body.maxY < world.init() - 16) {
            return new aU.Inner_StreamUtil(aU.Inner_DataUtil.VOID, fallen, 1.0F);
         }

         double drag = grounded ? world.init_D(body) : 0.91;
         velocity = new Vec3(
            Math.abs(moved.x - velocity.x) > 1.0E-7 ? 0.0 : velocity.x * drag,
            ((grounded ? 0.0 : velocity.y) - gravity) * 0.98,
            Math.abs(moved.z - velocity.z) > 1.0E-7 ? 0.0 : velocity.z * drag
         );
      }

      if (velocity.horizontalDistance() < 1.0E-4) {
         Vec3 down = new Vec3(0.0, world.init() - 17 - body.maxY, 0.0);
         if (down.y < 0.0 && world.init_Z(body.expandTowards(down))) {
            Vec3 moved = world.init(body, down);
            aU.Inner_DataUtil medium = world.init_aU_DataUtil(body.expandTowards(moved));
            if (medium != null) {
               return new aU.Inner_StreamUtil(medium, fallen - moved.y, 0.0F);
            }

            if (moved.y <= down.y + 1.0E-7) {
               return new aU.Inner_StreamUtil(aU.Inner_DataUtil.VOID, fallen - moved.y, 1.0F);
            }

            return new aU.Inner_StreamUtil(aU.Inner_DataUtil.LAND, fallen - moved.y, world.init_F(body.move(moved)), 241);
         }
      }

      return new aU.Inner_StreamUtil(aU.Inner_DataUtil.UNKNOWN, fallen, 1.0F);
   }

   static int init(int rawDamage, int ticks, boolean bypassCooldown) {
      return ticks < 10 && !bypassCooldown ? Math.max(0, rawDamage - 5) : rawDamage;
   }

   static int init(aU.Inner_DataUtil end, double damage, double health, double maxHealth, double thresholdPercent) {
      if (end != aU.Inner_DataUtil.VOID && end != aU.Inner_DataUtil.HAZARD && !(damage >= health)) {
         return end != aU.Inner_DataUtil.UNKNOWN && Double.isFinite(damage) && !(damage >= maxHealth * Math.clamp(thresholdPercent, 1.0, 100.0) / 100.0)
            ? -10099317
            : -12971;
      } else {
         return -44976;
      }
   }

   static int init(final Minecraft mc, Vec3 destination, double threshold) {
      if (mc.player != null && mc.level != null && destination != null) {
         final LocalPlayer player = mc.player;
         if (!player.getAbilities().invulnerable && !player.getAbilities().flying) {
            AABB body = player.getBoundingBox().move(destination.subtract(player.position()));
            aU.Inner_CachePool world = new aU.Inner_CachePool() {
               @Override
               public int init() {
                  return mc.level.getMinY();
               }

               @Override
               public boolean init_Z(AABB box) {
                  for (int x = BlockPos.containing(box.minX, 0.0, 0.0).getX() >> 4; x <= BlockPos.containing(box.maxX, 0.0, 0.0).getX() >> 4; x++) {
                     for (int z = BlockPos.containing(0.0, 0.0, box.minZ).getZ() >> 4; z <= BlockPos.containing(0.0, 0.0, box.maxZ).getZ() >> 4; z++) {
                        if (!mc.level.hasChunk(x, z)) {
                           return false;
                        }
                     }
                  }

                  return true;
               }

               @Override
               public Vec3 init(AABB box, Vec3 motion) {
                  return Entity.collideBoundingBox(player, motion, box, mc.level, List.of());
               }

               @Override
               public aU.Inner_DataUtil init_aU_DataUtil(AABB box) {
                  boolean water = false;

                  for (BlockPos at : BlockPos.betweenClosed(
                     BlockPos.containing(box.minX + 0.001, box.minY + 0.001, box.minZ + 0.001),
                     BlockPos.containing(box.maxX - 0.001, box.maxY - 0.001, box.maxZ - 0.001)
                  )) {
                     BlockState state = mc.level.getBlockState(at);
                     FluidState fluid = state.getFluidState();
                     if (fluid.is(FluidTags.LAVA)
                        || state.is(Blocks.FIRE)
                        || state.is(Blocks.SOUL_FIRE)
                        || state.is(Blocks.CACTUS)
                        || state.is(Blocks.SWEET_BERRY_BUSH)) {
                        return aU.Inner_DataUtil.HAZARD;
                     }

                     water |= fluid.is(FluidTags.WATER) && at.getY() + fluid.getHeight(mc.level, at) > box.minY;
                  }

                  return water ? aU.Inner_DataUtil.WATER : null;
               }

               @Override
               public float init_F(AABB box) {
                  BlockState state = mc.level.getBlockState(BlockPos.containing(box.getCenter().x, box.minY - 0.01, box.getCenter().z));
                  if (state.is(Blocks.SLIME_BLOCK) && !player.isSuppressingBounce()) {
                     return 0.0F;
                  } else if (!state.is(Blocks.HAY_BLOCK) && !state.is(Blocks.HONEY_BLOCK)) {
                     return state.getBlock() instanceof BedBlock ? 0.5F : 1.0F;
                  } else {
                     return 0.2F;
                  }
               }

               @Override
               public double init_D(AABB box) {
                  return mc.level.getBlockState(BlockPos.containing(box.getCenter().x, box.minY - 0.01, box.getCenter().z)).getBlock().getFriction() * 0.91;
               }
            };
            aU.Inner_StreamUtil fall = init(
               world, body, player.getDeltaMovement(), player.hasEffect(MobEffects.SLOW_FALLING) ? Math.min(0.01, player.getGravity()) : player.getGravity()
            );
            AutismLivingEntityAccessor access = (AutismLivingEntityAccessor)player;
            float pearlDamage = access.autism$getDamageAfterMagicAbsorb(player.damageSources().enderPearl(), 5.0F);
            int rawFall = access.autism$calculateFallDamage(fall.init_D(), fall.init_F());
            rawFall = init(rawFall, fall.init_I(), player.damageSources().fall().is(DamageTypeTags.BYPASSES_COOLDOWN));
            float fallDamage = player.hasEffect(MobEffects.SLOW_FALLING)
               ? 0.0F
               : access.autism$getDamageAfterMagicAbsorb(player.damageSources().fall(), rawFall);
            int tint = init(
               fall.init_aU_DataUtil(), pearlDamage + fallDamage, player.getHealth() + player.getAbsorptionAmount(), player.getMaxHealth(), threshold
            );
            return tint == -10099317 && !mc.level.noCollision(player, body.deflate(0.001)) ? -12971 : tint;
         } else {
            return -10099317;
         }
      } else {
         return -12971;
      }
   }

   interface Inner_CachePool {
      boolean init_Z(AABB var1);

      Vec3 init(AABB var1, Vec3 var2);

      aU.Inner_DataUtil init_aU_DataUtil(AABB var1);

      float init_F(AABB var1);

      default double init_D(AABB box) {
         return 0.546;
      }

      int init();
   }

   enum Inner_DataUtil {
      LAND,
      WATER,
      HAZARD,
      VOID,
      UNKNOWN;
   }

   record Inner_StreamUtil(aU.Inner_DataUtil init_aU_DataUtil, double init_D, float init_F, int init_I) {
      Inner_StreamUtil(aU.Inner_DataUtil end, double distance, float multiplier) {
         this(end, distance, multiplier, 2147483647);
      }
   }
}
